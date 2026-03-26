package in.cg.main.auth.service;

import in.cg.main.auth.dto.AdminRequest;
import in.cg.main.auth.entity.UserApp;
import in.cg.main.auth.repository.UserRepository;
import in.cg.main.auth.util.JwtUtil;


import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import in.cg.main.auth.exception.ResourceNotFoundException;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, JwtUtil jwtUtil) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.jwtUtil = jwtUtil;
    }
    
    // Save a new user to the database (Public registration - always CUSTOMER)
    public String saveUser(in.cg.main.auth.dto.UserRegistrationRequest regRequest) {
        UserApp user = new UserApp();
        user.setUsername(regRequest.getUsername());
        user.setPassword(passwordEncoder.encode(regRequest.getPassword()));
        user.setEmail(regRequest.getEmail());
        user.setName(regRequest.getName());
        user.setPhone(regRequest.getPhone());
        user.setAddress(regRequest.getAddress());
        user.setRole("CUSTOMER"); // Enforce CUSTOMER role
        
        repository.save(user);
        return "User registered successfully as " + user.getRole();
    }

    // Save a new user by Admin (Admin can specify role)
    public String saveAdminUser(AdminRequest userRequest) {
        // Default to CUSTOMER if role is missing, but allow ADMIN
        if (userRequest.getRole() == null || userRequest.getRole().isBlank()) {
            userRequest.setRole("CUSTOMER");
        }
        UserApp user = modelMapper.map(userRequest, UserApp.class);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        
        repository.save(user);
        return "User created by admin with role: " + user.getRole();
    }

    // Create a JWT token for the user after successful login
    public String generateToken(String username) {
        UserApp user = repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        return jwtUtil.generateToken(username, user.getRole());
    }

    // Get the user's role by username
    public String getUserRole(String username) {
        UserApp user = repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        return user.getRole();
    }

    // This method is intentionally empty — Gateway handles all token validation
    public void validateToken(String token) {
        // Token validation is done at the API Gateway level
    }
}
