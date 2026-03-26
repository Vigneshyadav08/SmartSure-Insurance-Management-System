package in.cg.main.auth.service;

import in.cg.main.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import in.cg.main.auth.entity.UserApp;
import in.cg.main.auth.exception.ResourceNotFoundException;
import in.cg.main.auth.dto.AdminRequest;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private org.modelmapper.ModelMapper modelMapper;
    @Mock private in.cg.main.auth.util.JwtUtil jwtUtil;
    @InjectMocks private AuthService service;

    @Test
    void saveUser_shouldEncodePasswordAndSaveUser() {
        in.cg.main.auth.dto.UserRegistrationRequest reg = new in.cg.main.auth.dto.UserRegistrationRequest();
        reg.setUsername("bob"); reg.setPassword("secret"); reg.setEmail("bob@test.com");
        reg.setName("Bob Smith"); reg.setPhone("1234567890"); reg.setAddress("123 Street");
        
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        String result = service.saveUser(reg);
        
        assertEquals("User registered successfully as CUSTOMER", result);
        verify(repository).save(any(UserApp.class));
    }

    @Test
    void saveAdminUser_shouldUseModelMapper() {
        AdminRequest adminReq = new AdminRequest();
        adminReq.setUsername("admin2"); adminReq.setPassword("p"); adminReq.setRole("ADMIN");
        
        UserApp mappedUser = new UserApp();
        when(modelMapper.map(any(), eq(UserApp.class))).thenReturn(mappedUser);
        
        service.saveAdminUser(adminReq);
        verify(repository).save(mappedUser);
    }

    @Test
    void generateToken_whenUserExists_shouldInvokeJwtUtil() {
        UserApp user = new UserApp();
        user.setUsername("charlie"); user.setRole("CUSTOMER");
        when(repository.findByUsername("charlie")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("charlie", "CUSTOMER")).thenReturn("mock-token");
        
        String token = service.generateToken("charlie");
        assertEquals("mock-token", token);
    }

    @Test
    void validateToken_shouldExecuteWithoutError() {
        // Covers the intentionally empty method for coverage purposes
        service.validateToken("some-token");
    }

    @Test
    void generateToken_whenUserNotFound_shouldThrowException() {
        when(repository.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.generateToken("nobody"));
    }

    @Test
    void saveAdminUser_withNoRole_shouldDefaultToCustomer() {
        AdminRequest adminReq = new AdminRequest();
        adminReq.setRole(""); // Empty role
        
        UserApp user = new UserApp();
        when(modelMapper.map(any(), eq(UserApp.class))).thenReturn(user);
        
        service.saveAdminUser(adminReq);
        assertEquals("CUSTOMER", adminReq.getRole());
    }
}
