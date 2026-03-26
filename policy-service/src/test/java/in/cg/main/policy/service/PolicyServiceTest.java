package in.cg.main.policy.service;

import in.cg.main.policy.dto.PurchaseRequest;
import in.cg.main.policy.entity.Policy;
import in.cg.main.policy.exception.ResourceNotFoundException;
import in.cg.main.policy.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock private PolicyRepository repository;
    @Mock private in.cg.main.policy.repository.PurchasedPolicyRepository purchasedPolicyRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private org.modelmapper.ModelMapper modelMapper;
    private PolicyService service;

    @BeforeEach
    void setUp() {
        service = new PolicyService(repository, purchasedPolicyRepository, modelMapper, rabbitTemplate, null);
        ReflectionTestUtils.setField(service, "self", service);
    }

    @Test
    void getPolicy_withInvalidId_shouldThrowException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getPolicy(99L));
    }

    @Test
    void getPolicy_shouldReturnPolicy() {
        Policy p = new Policy(); p.setId(1L); p.setName("Silver");
        in.cg.main.policy.dto.PolicyResponseDTO resp = new in.cg.main.policy.dto.PolicyResponseDTO();
        resp.setName("Silver");
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(modelMapper.map(any(), eq(in.cg.main.policy.dto.PolicyResponseDTO.class))).thenReturn(resp);
        
        assertEquals("Silver", service.getPolicy(1L).getName());
    }

    @Test
    void purchasePolicy_shouldSendMessageAndReturnSuccess() {
        Policy p = new Policy(); p.setId(1L); p.setName("Silver");
        p.setDeductibleAmount(500.0);
        
        PurchaseRequest req = new PurchaseRequest(); req.setPolicyId(1L); req.setCustomerUsername("alice");
        
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        
        String result = service.purchasePolicy(req);
        assertTrue(result.contains("CREATED"));
        verify(purchasedPolicyRepository, times(1)).save(any());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void deletePolicy_whenNotFound_shouldThrowException() {
        when(repository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.deletePolicy(1L));
    }

    @Test
    void createPolicy_shouldPersistAndReturnPolicy() {
        // WHY: Verification that admins can correctly add new insurance plans 
        // to the system, which is a core business operation.
        in.cg.main.policy.dto.PolicyDTO dto = new in.cg.main.policy.dto.PolicyDTO();
        dto.setName("Gold");
        Policy p = new Policy(); p.setName("Gold");
        
        when(modelMapper.map(any(), eq(Policy.class))).thenReturn(p);
        when(repository.save(any())).thenReturn(p);
        when(modelMapper.map(any(), eq(in.cg.main.policy.dto.PolicyResponseDTO.class))).thenReturn(new in.cg.main.policy.dto.PolicyResponseDTO());
        
        assertNotNull(service.createPolicy(dto));
    }

    @Test
    void deletePolicy_shouldCallRepositoryDelete() {
        // WHY: Verification that admins can retire outdated policies, 
        // ensuring data lifecycle management.
        when(repository.existsById(1L)).thenReturn(true);
        service.deletePolicy(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void getAllPolicies_shouldReturnList() {
        // WHY: Verification that the system can correctly retrieve the full 
        // catalog of available insurance products for prospective customers.
        when(repository.findAll()).thenReturn(java.util.Collections.singletonList(new Policy()));
        assertFalse(service.getAllPolicies().isEmpty());
    }

    @Test
    void purchasePolicy_whenPolicyNotFound_shouldThrowException() {
        // WHY: Verification that the system correctly handles purchase requests 
        // for non-existent policy IDs, preventing invalid transactions.
        PurchaseRequest req = new PurchaseRequest(); req.setPolicyId(99L);
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.purchasePolicy(req));
    }
}
