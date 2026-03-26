package in.cg.main.policy.controller;

import in.cg.main.policy.dto.PolicyDTO;
import in.cg.main.policy.dto.PurchaseRequest;
import in.cg.main.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import in.cg.main.policy.dto.PolicyResponseDTO;
import in.cg.main.policy.dto.PurchasedPolicyResponseDTO;

@RestController
@RequestMapping("/policies")
@Tag(name = "Policy Service", description = "create and purchase policies")
public class PolicyController {

    private final PolicyService service;

    public PolicyController(PolicyService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "only admin can creates policies")
    public PolicyResponseDTO create(@Valid @RequestBody PolicyDTO policy) {
        return service.createPolicy(policy);
    }

    @GetMapping
    @Operation(summary =  "returns all available policies")
    public List<PolicyResponseDTO> getAll() {
        return service.getAllPolicies();
    }

    @GetMapping("/{id}")
    @Operation(summary =  "retrieve policy details with id")
    public PolicyResponseDTO getById(@PathVariable Long id) {
        return service.getPolicy(id);
    }

    @PostMapping("/purchase")
    @Operation(summary = "Step 1: Request purchase (Initial Status: CREATED)")
    public String purchase(@Valid @RequestBody PurchaseRequest req) {
        return service.purchasePolicy(req);
    }

    @PostMapping("/purchase/{id}/pay")
    @Operation(summary = "Step 2: Submit payment (Status: ACTIVE)")
    public String pay(@PathVariable Long id) {
        return service.payPolicy(id);
    }

    @PostMapping("/purchase/{id}/cancel")
    @Operation(summary = "Step 3: Cancel policy (Status: CANCELLED)")
    public String cancel(@PathVariable Long id) {
        return service.cancelPolicy(id);
    }

    @PostMapping("/purchase/{id}/expire")
    @Operation(summary = "Step 4: Admin expires policy (Status: EXPIRY)")
    public String expire(@PathVariable Long id) {
        return service.expirePolicy(id);
    }

    @GetMapping("/purchase/status")
    @Operation(summary = "Retrieve purchased policy status for a user")
    public PurchasedPolicyResponseDTO getStatus(@RequestParam String username, @RequestParam Long policyId) {
        return service.getPurchasedPolicyStatus(username, policyId);
    }

    @GetMapping("/purchase/user/{username}")
    @Operation(summary = "Admin/User: Get all purchased policies for a user")
    public List<PurchasedPolicyResponseDTO> getByUser(@PathVariable String username) {
        return service.getPurchasedPoliciesByUser(username);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "only admin can delete any policy record")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deletePolicy(id);
    }
}
