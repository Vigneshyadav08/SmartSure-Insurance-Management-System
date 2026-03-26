package in.cg.main.auth.config;

import in.cg.main.auth.entity.UserApp;
import in.cg.main.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

// Spring Security needs this class to load user data during login
// When someone logs in, Spring calls loadUserByUsername() automatically
// It finds the user from our database and returns it in a format Spring understands
@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public CustomUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Look up the user in the database
        Optional<UserApp> userOptional = repository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        UserApp userApp = userOptional.get();

        // Convert our UserApp entity into Spring Security's UserDetails format
        // We also pass the role as a "GrantedAuthority" (ROLE_ADMIN or ROLE_CUSTOMER)
        return new User(
                userApp.getUsername(),
                userApp.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userApp.getRole()))
        );
    }
}
