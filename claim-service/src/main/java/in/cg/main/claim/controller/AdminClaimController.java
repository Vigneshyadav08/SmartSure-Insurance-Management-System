package in.cg.main.claim.controller;

import in.cg.main.claim.dto.ClaimResponseDTO;
import in.cg.main.claim.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/admin-internal/claims")
@Tag(name = "Admin Internal Claim API", description = "Endpoints used exclusively by Admin Service")
public class AdminClaimController {

    private final ClaimService service;

    public AdminClaimController(ClaimService service) {
        this.service = service;
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Internal: update claim status")
    public ClaimResponseDTO updateStatus(@PathVariable Long id, @RequestParam String status) {
        return service.updateClaimStatus(id, status);
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "Internal: close approved/rejected claim")
    public ClaimResponseDTO closeClaim(@PathVariable Long id) {
        return service.closeClaim(id);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Internal: download document and move status to UNDER_REVIEW")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws IOException {
        byte[] doc = service.downloadDocument(id, true);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(doc);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Internal: delete claim record")
    public String delete(@PathVariable Long id) {
        service.deleteClaim(id);
        return "deleted successfully";
    }
}
