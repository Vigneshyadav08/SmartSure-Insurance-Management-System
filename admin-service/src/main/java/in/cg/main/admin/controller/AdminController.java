package in.cg.main.admin.controller;

import in.cg.main.admin.entity.Report;
import in.cg.main.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Service", description = "admin authorizes claimed policies")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @PutMapping("/claims/{id}/approve")
    @Operation(summary = "approve claimed policy")
    public Object approveClaim(@PathVariable Long id) {
        return service.approveClaim(id);
    }

    @PutMapping("/claims/{id}/reject")
    @Operation(summary = "reject claimed policy")
    public Object rejectClaim(@PathVariable Long id) {
        return service.rejectClaim(id);
    }

    @PostMapping("/reports/generate")
    @Operation(summary = "generate report of admin")
    public Report generateReport() {
        return service.generateReport();
    }

    @GetMapping("/claims/{id}/download")
    @Operation(summary = "admin can download and check documents submitted by customer")
    public byte[] downloadClaimDocument(@PathVariable Long id) {
        return service.downloadClaimDocument(id);
    }

    @PutMapping("/claims/{id}/close")
    @Operation(summary = "close an approved or rejected claim")
    public Object closeClaim(@PathVariable Long id) {
        return service.closeClaim(id);
    }

    @GetMapping("/users/{username}/policies")
    @Operation(summary = "admin can check policy details of a specific user")
    public Object getPurchasedPolicies(@PathVariable String username) {
        return service.getPurchasedPolicies(username);
    }

    @PutMapping("/policies/{id}/expire")
    @Operation(summary = "admin can manually trigger policy expiry")
    public String expirePolicy(@PathVariable Long id) {
        return service.expirePolicy(id);
    }
}
