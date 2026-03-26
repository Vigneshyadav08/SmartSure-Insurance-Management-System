package in.cg.main.auth.controller;

import in.cg.main.auth.dto.AdminRequest;
import in.cg.main.auth.dto.AuthRequest;
import in.cg.main.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import in.cg.main.auth.dto.AuthResponse;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Service",description = "user details")
public class AuthController {

    private final AuthService service;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService service, AuthenticationManager authenticationManager) {
        this.service = service;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    @Operation(summary = "user registration")
    public String addNewUser(@Valid @RequestBody in.cg.main.auth.dto.UserRegistrationRequest user) {
        return service.saveUser(user);
    }

    @PostMapping("/admin/create-user")
    @Operation(summary = "admin authorized registration")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String createByAdmin(@Valid @RequestBody AdminRequest user) {
        return service.saveAdminUser(user);
    }

    @PostMapping("/login")
    @Operation(summary = "logging in")
    public AuthResponse getToken(@Valid @RequestBody AuthRequest authRequest) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authenticate.isAuthenticated()) {
            String token = service.generateToken(authRequest.getUsername());
            String role = service.getUserRole(authRequest.getUsername());
            return new AuthResponse(token, role);
        } else {
            throw new BadCredentialsException("invalid access");
        }
    }
}
