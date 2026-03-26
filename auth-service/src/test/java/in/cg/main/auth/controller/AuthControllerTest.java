package in.cg.main.auth.controller;

import in.cg.main.auth.dto.AuthRequest;
import in.cg.main.auth.dto.AuthResponse;
import in.cg.main.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.BadCredentialsException;
import in.cg.main.auth.dto.AdminRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService service;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthController controller;

    @Test
    void register_shouldReturnSuccessMessage() {
        when(service.saveUser(any())).thenReturn("User registered successfully as CUSTOMER");
        assertEquals("User registered successfully as CUSTOMER", controller.addNewUser(new in.cg.main.auth.dto.UserRegistrationRequest()));
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(service.generateToken("john")).thenReturn("jwt-token");
        when(service.getUserRole("john")).thenReturn("CUSTOMER");
        AuthRequest req = new AuthRequest();
        req.setUsername("john"); req.setPassword("pass");
        AuthResponse response = controller.getToken(req);
        assertEquals("jwt-token", response.getToken());
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    void createByAdmin_shouldInvokeService() {
        AdminRequest req = new AdminRequest();
        when(service.saveAdminUser(any())).thenReturn("Admin success");
        assertEquals("Admin success", controller.createByAdmin(req));
    }

    @Test
    void getToken_withInvalidAuth_shouldThrowException() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("failed"));
        AuthRequest req = new AuthRequest();
        req.setUsername("bad"); req.setPassword("wrong");
        assertThrows(BadCredentialsException.class, () -> controller.getToken(req));
    }

    @Test
    void getToken_whenNotAuthenticated_shouldThrowBadCredentialsException() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        
        AuthRequest req = new AuthRequest();
        req.setUsername("user"); req.setPassword("pass");
        
        assertThrows(BadCredentialsException.class, () -> controller.getToken(req));
    }
}
