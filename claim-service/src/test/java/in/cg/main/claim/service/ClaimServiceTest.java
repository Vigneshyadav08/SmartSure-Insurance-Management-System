package in.cg.main.claim.service;

import in.cg.main.claim.entity.Claim;
import in.cg.main.claim.repository.ClaimRepository;
import in.cg.main.claim.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import in.cg.main.claim.dto.ClaimRequestDTO;
import in.cg.main.claim.dto.ClaimResponseDTO;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock private ClaimRepository repository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ModelMapper modelMapper;
    @Mock private in.cg.main.claim.client.PolicyClient policyClient;
    private ClaimService service;

    @BeforeEach
    void setUp() {
        service = new ClaimService(repository, rabbitTemplate, modelMapper, policyClient);
    }

    @Test
    void submitClaim_shouldSetStatusToPendingAndSendRabbitMQMessage() throws IOException {
        Claim c = new Claim(); c.setId(1L); c.setCustomerUsername("alice");
        ClaimResponseDTO resp = new ClaimResponseDTO(); resp.setId(1L); resp.setCustomerUsername("alice"); 
        resp.setStatus("PENDING"); resp.setPayoutAmount(500.0);
        
        ClaimRequestDTO req = new ClaimRequestDTO(); req.setCustomerUsername("alice"); req.setPolicyId(1L); req.setClaimAmount(1000.0);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "text/plain", "content".getBytes());
        
        in.cg.main.claim.dto.PurchasedPolicyStatusResponseDTO policyStatus = new in.cg.main.claim.dto.PurchasedPolicyStatusResponseDTO();
        policyStatus.setStatus("ACTIVE");
        policyStatus.setDeductibleAmount(500.0);
        
        when(policyClient.getPurchasedPolicyStatus(anyString(), anyLong())).thenReturn(policyStatus);
        when(modelMapper.map(any(), eq(Claim.class))).thenReturn(c);
        when(repository.save(any())).thenReturn(c);
        when(modelMapper.map(any(), eq(ClaimResponseDTO.class))).thenReturn(resp);
        
        ClaimResponseDTO result = service.submitClaim(req, file);
        assertEquals("PENDING", result.getStatus());
        assertEquals(500.0, result.getPayoutAmount());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void submitClaim_whenPolicyNotActive_shouldThrowException() throws IOException {
        ClaimRequestDTO req = new ClaimRequestDTO(); req.setCustomerUsername("alice"); req.setPolicyId(1L);
        in.cg.main.claim.dto.PurchasedPolicyStatusResponseDTO policyStatus = new in.cg.main.claim.dto.PurchasedPolicyStatusResponseDTO();
        policyStatus.setStatus("CREATED");
        
        when(policyClient.getPurchasedPolicyStatus(anyString(), anyLong())).thenReturn(policyStatus);
        
        assertThrows(IllegalStateException.class, () -> service.submitClaim(req, null));
    }

    @Test
    void submitClaim_withEmptyFile_shouldStillSaveClaimInfo() throws IOException {
        Claim c = new Claim(); c.setId(2L);
        ClaimRequestDTO req = new ClaimRequestDTO(); req.setCustomerUsername("alice"); req.setPolicyId(1L); req.setClaimAmount(1000.0);
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        
        in.cg.main.claim.dto.PurchasedPolicyStatusResponseDTO policyStatus = new in.cg.main.claim.dto.PurchasedPolicyStatusResponseDTO();
        policyStatus.setStatus("ACTIVE");
        policyStatus.setDeductibleAmount(200.0);
        
        when(policyClient.getPurchasedPolicyStatus(anyString(), anyLong())).thenReturn(policyStatus);
        when(modelMapper.map(any(), eq(Claim.class))).thenReturn(c);
        when(repository.save(any())).thenReturn(c);
        when(modelMapper.map(any(), eq(ClaimResponseDTO.class))).thenReturn(new ClaimResponseDTO());
        
        service.submitClaim(req, file);
        verify(repository, atLeastOnce()).save(any());
    }

    @Test
    void downloadDocument_whenAdmin_shouldChangeStatusToUnderReview() throws IOException {
        Claim c = new Claim(); 
        c.setStatus("PENDING");
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, "content".getBytes());
        c.setDocumentPath(tempFile.toString());
        
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        when(repository.save(any())).thenReturn(c);
        
        service.downloadDocument(1L, true);
        assertEquals("UNDER_REVIEW", c.getStatus());
        Files.deleteIfExists(tempFile);
    }

    @Test
    void downloadDocument_whenNotAdmin_shouldNotChangeStatus() throws IOException {
        Claim c = new Claim(); 
        c.setStatus("PENDING");
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, "content".getBytes());
        c.setDocumentPath(tempFile.toString());
        
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        
        service.downloadDocument(1L, false);
        assertEquals("PENDING", c.getStatus());
        verify(repository, never()).save(any());
        Files.deleteIfExists(tempFile);
    }

    @Test
    void updateClaimStatus_shouldSaveNewStatus() {
        Claim c = new Claim(); c.setId(1L); c.setStatus("PENDING");
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        when(repository.save(any())).thenReturn(c);
        when(modelMapper.map(any(), eq(ClaimResponseDTO.class))).thenReturn(new ClaimResponseDTO());
        
        service.updateClaimStatus(1L, "APPROVED");
        assertEquals("APPROVED", c.getStatus());
    }

    @Test
    void deleteClaim_whenExists_shouldCallDelete() {
        when(repository.existsById(1L)).thenReturn(true);
        service.deleteClaim(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void getClaims_shouldReturnUserClaims() {
        when(repository.findByCustomerUsername("alice")).thenReturn(java.util.Collections.singletonList(new Claim()));
        assertFalse(service.getClaims("alice").isEmpty());
    }

    @Test
    void closeClaim_whenNotApproved_shouldThrowException() {
        Claim c = new Claim(); c.setStatus("PENDING");
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        assertThrows(IllegalStateException.class, () -> service.closeClaim(1L));
    }

    @Test
    void getClaimById_whenNotFound_shouldThrowException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getClaimById(99L));
    }

    @Test
    void downloadDocument_whenNoDocument_shouldThrowException() {
        Claim c = new Claim();
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        assertThrows(IllegalStateException.class, () -> service.downloadDocument(1L, false));
    }

    @Test
    void deleteClaim_whenNotFound_shouldThrowException() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.deleteClaim(99L));
    }
}
