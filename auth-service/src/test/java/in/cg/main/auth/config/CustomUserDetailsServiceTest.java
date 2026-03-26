package in.cg.main.auth.config;

import in.cg.main.auth.entity.UserApp;
import in.cg.main.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository repository;
    @InjectMocks private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        UserApp user = new UserApp();
        user.setUsername("testuser");
        user.setPassword("pass");
        user.setRole("ADMIN");
        when(repository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("testuser");

        assertNotNull(details);
        assertEquals("testuser", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_shouldThrowException() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));
    }
}
