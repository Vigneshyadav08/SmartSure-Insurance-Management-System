package in.cg.main.policy.service;

import in.cg.main.policy.entity.Policy;
import in.cg.main.policy.entity.PurchasedPolicy;
import in.cg.main.policy.dto.PolicyDTO;
import in.cg.main.policy.dto.PolicyResponseDTO;
import in.cg.main.policy.dto.PurchaseRequest;
import in.cg.main.policy.dto.PurchasedPolicyResponseDTO;
import in.cg.main.policy.repository.PolicyRepository;
import in.cg.main.policy.repository.PurchasedPolicyRepository;
import in.cg.main.policy.exception.ResourceNotFoundException;
import in.cg.main.policy.config.RabbitMQConfig;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PolicyService {
    private final PolicyRepository repository;
    private final PurchasedPolicyRepository purchasedPolicyRepository;
    private final ModelMapper modelMapper;
    private final RabbitTemplate rabbitTemplate;
    private static final String POLICY_NOT_FOUND = "Policy not found";
    private static final String PURCHASE_NOT_FOUND = "Purchased policy record not found";

    private final PolicyService self;

    public PolicyService(PolicyRepository repository, PurchasedPolicyRepository purchasedPolicyRepository, 
                        ModelMapper modelMapper, RabbitTemplate rabbitTemplate, @Lazy PolicyService self) {
        this.repository = repository;
        this.purchasedPolicyRepository = purchasedPolicyRepository;
        this.modelMapper = modelMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.self = self;
    }

    @CacheEvict(value = "policies", allEntries = true)
    public PolicyResponseDTO createPolicy(PolicyDTO policy) {
        Policy saved = repository.save(modelMapper.map(policy,Policy.class));
        return modelMapper.map(saved, PolicyResponseDTO.class);
    }

    @Cacheable(value = "policies")
    public List<PolicyResponseDTO> getAllPolicies() {
        return repository.findAll().stream()
                .map(p -> modelMapper.map(p, PolicyResponseDTO.class))
                .toList();
    }

    @Cacheable(value = "policy", key = "#id")
    public PolicyResponseDTO getPolicy(Long id) {
        return modelMapper.map(getPolicyOrThrow(id), PolicyResponseDTO.class);
    }

    @CacheEvict(value = {"policies", "policy"}, allEntries = true)
    public void deletePolicy(Long id) {
        if(!repository.existsById(id)) {
            throw new ResourceNotFoundException(POLICY_NOT_FOUND);
        }
        repository.deleteById(id);
    }

    private Policy getPolicyOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(POLICY_NOT_FOUND));
    }

    // Step 1: Customer Purchases Policy (Status: CREATED)
    @CacheEvict(value = "purchased_policies", allEntries = true)
    public String purchasePolicy(PurchaseRequest request) {
        Policy policy = getPolicyOrThrow(request.getPolicyId());
        
        PurchasedPolicy purchase = new PurchasedPolicy();
        purchase.setPolicyId(policy.getId());
        purchase.setCustomerUsername(request.getCustomerUsername());
        purchase.setDeductibleAmount(policy.getDeductibleAmount());
        purchase.setStatus("CREATED");
        
        purchasedPolicyRepository.save(purchase);
        
        String message = "User " + request.getCustomerUsername() + " requested purchase of policy " + policy.getName();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.POLICY_ROUTING_KEY, message);
        return "Policy purchase requested. Initial Status: CREATED. Please proceed to payment.";
    }

    // Step 2: Customer Pays (Status: ACTIVE)
    @CacheEvict(value = "purchased_policies", allEntries = true)
    public String payPolicy(Long purchaseId) {
        PurchasedPolicy purchase = purchasedPolicyRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException(PURCHASE_NOT_FOUND));
        
        purchase.setStatus("ACTIVE");
        purchasedPolicyRepository.save(purchase);
        return "Payment received. Policy is now ACTIVE.";
    }

    // Step 3: Customer Cancels (Status: CANCELLED)
    @CacheEvict(value = "purchased_policies", allEntries = true)
    public String cancelPolicy(Long purchaseId) {
        PurchasedPolicy purchase = purchasedPolicyRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException(PURCHASE_NOT_FOUND));
        
        purchase.setStatus("CANCELLED");
        purchasedPolicyRepository.save(purchase);
        return "Policy has been CANCELLED.";
    }

    // Step 4: Admin Expires (Status: EXPIRY)
    @CacheEvict(value = "purchased_policies", allEntries = true)
    public String expirePolicy(Long purchaseId) {
        PurchasedPolicy purchase = purchasedPolicyRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException(PURCHASE_NOT_FOUND));
        
        purchase.setStatus("EXPIRY");
        purchasedPolicyRepository.save(purchase);
        return "Policy status updated to EXPIRY by Admin.";
    }

    // For Claim Service to verify status
    public PurchasedPolicyResponseDTO getPurchasedPolicyStatus(String username, Long policyId) {
        PurchasedPolicy purchase = purchasedPolicyRepository.findByCustomerUsernameAndPolicyId(username, policyId)
                .orElseThrow(() -> new ResourceNotFoundException("No purchase record found for this user and policy"));
        return modelMapper.map(purchase, PurchasedPolicyResponseDTO.class);
    }

    // For Admin to check all user policies
    public List<PurchasedPolicyResponseDTO> getPurchasedPoliciesByUser(String username) {
        return purchasedPolicyRepository.findByCustomerUsername(username).stream()
                .map(p -> modelMapper.map(p, PurchasedPolicyResponseDTO.class))
                .toList();
    }
}
