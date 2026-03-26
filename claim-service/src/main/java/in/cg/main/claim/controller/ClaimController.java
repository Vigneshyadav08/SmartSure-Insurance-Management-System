package in.cg.main.claim.controller;

import in.cg.main.claim.dto.ClaimRequestDTO;
import in.cg.main.claim.dto.ClaimResponseDTO;
import in.cg.main.claim.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/claims")
@Tag(name = "Claim Service", description = "Customer creates and checks claims")
public class ClaimController {

	private final ClaimService service;

	public ClaimController(ClaimService service) {
		this.service = service;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Submit a new claim (Initial status: PENDING)")
	public ClaimResponseDTO submitClaim(@RequestPart("claimRequest") @Valid ClaimRequestDTO claimRequest,
			@RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
		return service.submitClaim(claimRequest, file);
	}

	@GetMapping("/user/{username}")
	@Operation(summary = "Get all claims submitted by a specific user")
	public List<ClaimResponseDTO> getMyClaims(@PathVariable String username) {
		return service.getClaims(username);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get details of a specific claim")
	public ClaimResponseDTO getClaim(@PathVariable Long id) {
		return service.getClaimById(id);
	}

	@GetMapping("/{id}/download")
	@Operation(summary = "Download supporting documents for a claim")
	public ResponseEntity<byte[]> download(@PathVariable Long id) throws IOException {
		byte[] doc = service.downloadDocument(id, false);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(doc);
	}
}
