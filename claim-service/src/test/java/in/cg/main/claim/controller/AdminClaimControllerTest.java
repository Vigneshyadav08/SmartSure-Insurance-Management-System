package in.cg.main.claim.controller;

import in.cg.main.claim.dto.ClaimResponseDTO;
import in.cg.main.claim.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminClaimController.class)
class AdminClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimService service;

    @Test
    void updateStatus_shouldReturnUpdated() throws Exception {
        ClaimResponseDTO resp = new ClaimResponseDTO(); resp.setStatus("APPROVED");
        when(service.updateClaimStatus(1L, "APPROVED")).thenReturn(resp);

        mockMvc.perform(put("/admin-internal/claims/1/status").param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void closeClaim_shouldReturnClosed() throws Exception {
        ClaimResponseDTO resp = new ClaimResponseDTO(); resp.setStatus("CLOSED");
        when(service.closeClaim(1L)).thenReturn(resp);

        mockMvc.perform(put("/admin-internal/claims/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void download_shouldReturnDataAndTriggerReview() throws Exception {
        byte[] data = "test data".getBytes();
        when(service.downloadDocument(eq(1L), eq(true))).thenReturn(data);

        mockMvc.perform(get("/admin-internal/claims/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(data));
    }

    @Test
    void delete_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/admin-internal/claims/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("deleted successfully"));
    }
}
