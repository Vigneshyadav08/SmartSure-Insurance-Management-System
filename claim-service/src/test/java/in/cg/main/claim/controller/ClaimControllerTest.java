package in.cg.main.claim.controller;

import in.cg.main.claim.dto.ClaimRequestDTO;
import in.cg.main.claim.dto.ClaimResponseDTO;
import in.cg.main.claim.service.ClaimService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClaimController.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimService service;

    @Autowired
    private ObjectMapper objectMapper;

    private ClaimRequestDTO createValidRequest() {
        ClaimRequestDTO req = new ClaimRequestDTO();
        req.setPolicyId(1L);
        req.setCustomerUsername("alice");
        req.setDescription("Car accident");
        req.setClaimAmount(5000.0);
        req.setIncidentDate(java.time.LocalDate.now());
        return req;
    }

    @Test
    void submitClaim_shouldReturnCreated() throws Exception {
        ClaimResponseDTO resp = new ClaimResponseDTO(); resp.setId(1L); resp.setStatus("PENDING");
        MockMultipartFile claimJson = new MockMultipartFile("claimRequest", "", "application/json", objectMapper.writeValueAsBytes(createValidRequest()));
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());

        when(service.submitClaim(any(), any())).thenReturn(resp);

        mockMvc.perform(multipart("/claims")
                .file(claimJson)
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getMyClaims_shouldReturnList() throws Exception {
        when(service.getClaims("user")).thenReturn(Collections.singletonList(new ClaimResponseDTO()));

        mockMvc.perform(get("/claims/user/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getClaim_shouldReturnClaim() throws Exception {
        ClaimResponseDTO resp = new ClaimResponseDTO(); resp.setId(1L);
        when(service.getClaimById(1L)).thenReturn(resp);

        mockMvc.perform(get("/claims/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void download_shouldReturnData() throws Exception {
        byte[] data = "test data".getBytes();
        when(service.downloadDocument(eq(1L), anyBoolean())).thenReturn(data);

        mockMvc.perform(get("/claims/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(data));
    }
}
