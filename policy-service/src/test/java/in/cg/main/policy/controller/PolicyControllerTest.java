package in.cg.main.policy.controller;

import in.cg.main.policy.dto.PolicyDTO;
import in.cg.main.policy.dto.PolicyResponseDTO;
import in.cg.main.policy.dto.PurchaseRequest;
import in.cg.main.policy.service.PolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(service.getAllPolicies()).thenReturn(Collections.singletonList(new PolicyResponseDTO()));
        mockMvc.perform(get("/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        PolicyResponseDTO resp = new PolicyResponseDTO(); resp.setId(1L);
        PolicyDTO dto = new PolicyDTO();
        dto.setName("Term Life");
        dto.setCoverage(100000.0);
        dto.setPremium(500.0);
        dto.setDeductibleAmount(1000.0);
        dto.setMonths(12);

        when(service.createPolicy(any())).thenReturn(resp);

        mockMvc.perform(post("/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_shouldReturnPolicy() throws Exception {
        PolicyResponseDTO resp = new PolicyResponseDTO(); resp.setId(1L);
        when(service.getPolicy(1L)).thenReturn(resp);

        mockMvc.perform(get("/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/policies/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void purchase_shouldReturnSuccess() throws Exception {
        PurchaseRequest req = new PurchaseRequest();
        req.setPolicyId(1L);
        req.setCustomerUsername("alice");

        when(service.purchasePolicy(any())).thenReturn("Policy purchase requested. Initial Status: CREATED.");

        mockMvc.perform(post("/policies/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CREATED")));
    }
}
