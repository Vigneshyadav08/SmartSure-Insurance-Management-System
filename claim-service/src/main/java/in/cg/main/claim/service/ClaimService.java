package in.cg.main.claim.service;

import in.cg.main.claim.entity.Claim;
import in.cg.main.claim.repository.ClaimRepository;
import in.cg.main.claim.exception.ResourceNotFoundException;
import in.cg.main.claim.config.RabbitMQConfig;
import in.cg.main.claim.dto.ClaimRequestDTO;
import in.cg.main.claim.dto.ClaimResponseDTO;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.cg.main.claim.client.PolicyClient;
import in.cg.main.claim.dto.PurchasedPolicyStatusResponseDTO;

@Service
public class ClaimService {
	private static final Logger log = LoggerFactory.getLogger(ClaimService.class);
	private final ClaimRepository repository;
	private final RabbitTemplate rabbitTemplate;
	private final ModelMapper modelMapper;
	private final PolicyClient policyClient;

	public ClaimService(ClaimRepository repository, RabbitTemplate rabbitTemplate, 
                       ModelMapper modelMapper, PolicyClient policyClient) {
		this.repository = repository;
		this.rabbitTemplate = rabbitTemplate;
		this.modelMapper = modelMapper;
		this.policyClient = policyClient;
	}

	private static final String UPLOAD_DIR = "uploads/";
	private static final String CLAIM_NOT_FOUND = "Claim not found";

	@CacheEvict(value = "claims", allEntries = true)
	public ClaimResponseDTO submitClaim(ClaimRequestDTO req, MultipartFile file) throws IOException {
		// Verify policy is ACTIVE before claiming
		PurchasedPolicyStatusResponseDTO policyStatus = policyClient.getPurchasedPolicyStatus(
				req.getCustomerUsername(), req.getPolicyId());
		
		if (!"ACTIVE".equalsIgnoreCase(policyStatus.getStatus())) {
			throw new IllegalStateException("Cannot submit claim: Policy status is " + policyStatus.getStatus() + ". Only ACTIVE policies are eligible for claims.");
		}

		Claim claim = modelMapper.map(req, Claim.class);
		
		// Set deductible details from policy record
		claim.setDeductibleAmount(policyStatus.getDeductibleAmount());
		double payout = Math.max(0, req.getClaimAmount() - policyStatus.getDeductibleAmount());
		if(payout<0)
			payout = 0;
		claim.setPayoutAmount(payout);
		
		claim.setStatus("PENDING");
		claim.setClaimDate(java.time.LocalDate.now());

		// First save the claim to get an ID
		claim = repository.save(claim);

		// Then handle the file if present
		if (file != null && !file.isEmpty()) {
			saveFile(claim, file);
			claim = repository.save(claim);
		}

		ClaimResponseDTO saved = modelMapper.map(claim, ClaimResponseDTO.class);
		log.info("Claim submitted successfully: ID={}, Payout={}, User={}", saved.getId(), saved.getPayoutAmount(), saved.getCustomerUsername());
		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.CLAIM_ROUTING_KEY,
				"Claim submitted ID: " + saved.getId() + " by " + saved.getCustomerUsername() + " | Final Payout: " + saved.getPayoutAmount());
		return saved;
	}

	private void saveFile(Claim claim, MultipartFile file) throws IOException {
		Path uploadPath = Paths.get(UPLOAD_DIR);
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		String fileName = claim.getId() + "_" + file.getOriginalFilename();
		Path filePath = uploadPath.resolve(fileName);
		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
		claim.setDocumentPath(filePath.toString());
	}

	private Claim getClaimOrThrow(Long id) {
		return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(CLAIM_NOT_FOUND));
	}

	@Cacheable(value = "claims", key = "#username")
	public List<ClaimResponseDTO> getClaims(String username) {
		List<Claim> claims = repository.findByCustomerUsername(username);
		List<ClaimResponseDTO> claimsResponse = new ArrayList<>();
		for (Claim claim : claims) {
			claimsResponse.add(modelMapper.map(claim, ClaimResponseDTO.class));
		}
		return claimsResponse;
	}

	@Cacheable(value = "claim", key = "#id")
	public ClaimResponseDTO getClaimById(Long id) {
		return modelMapper.map(getClaimOrThrow(id), ClaimResponseDTO.class);
	}

	@CacheEvict(value = { "claims", "claim" }, allEntries = true)
	public ClaimResponseDTO updateClaimStatus(Long id, String status) {
		Claim c = getClaimOrThrow(id);
		c.setStatus(status);
		return modelMapper.map(repository.save(c), ClaimResponseDTO.class);
	}

	@CacheEvict(value = { "claims", "claim" }, allEntries = true)
	public byte[] downloadDocument(Long id, boolean isAdmin) throws IOException {
		Claim claim = getClaimOrThrow(id);
		if (claim.getDocumentPath() == null) {
			throw new IllegalStateException("No document found for this claim");
		}

		// When admin downloads the document for validation, status moves to
		// UNDER_REVIEW
		if (isAdmin && !"APPROVED".equals(claim.getStatus()) && !"REJECTED".equals(claim.getStatus())) {
			claim.setStatus("UNDER_REVIEW");
			repository.save(claim);
			log.info("Claim status moved to UNDER_REVIEW for ID: {}", id);
		}

		return Files.readAllBytes(Paths.get(claim.getDocumentPath()));
	}

	@CacheEvict(value = { "claims", "claim" }, allEntries = true)
	public ClaimResponseDTO closeClaim(Long id) {
		// WHY: Terminal lifecycle stage where a claim is officially archived
		// after being approved or rejected.
		Claim claim = getClaimOrThrow(id);
		if (!"APPROVED".equals(claim.getStatus()) && !"REJECTED".equals(claim.getStatus())) {
			throw new IllegalStateException("Claim must be APPROVED or REJECTED before closing");
		}
		claim.setStatus("CLOSED");
		return modelMapper.map(repository.save(claim), ClaimResponseDTO.class);
	}

	@CacheEvict(value = { "claims", "claim" }, allEntries = true)
	public void deleteClaim(Long id) {
		if (!repository.existsById(id)) {
			throw new ResourceNotFoundException(CLAIM_NOT_FOUND);
		}
		repository.deleteById(id);
	}
}
