package in.cg.main.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import in.cg.main.auth.entity.UserApp;
import in.cg.main.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
public class AuthServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceApplication.class);

    private static final String ADMIN = "admin";

    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    AuthServiceApplication(PasswordEncoder passwordEncoder, @Value("${admin.password:SmartSureAdmin123}") String adminPassword) {
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
    
    @Bean
    CommandLineRunner init(UserRepository repository) {
        return args -> {
            // Check if admin already exists to avoid duplicate entry errors
            if (repository.findByUsername(ADMIN).isEmpty()) {
                UserApp admin = new UserApp();
                admin.setUsername(ADMIN);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole("ADMIN");
                repository.save(admin);
                log.info("Default admin user created: {}/********", ADMIN);
            }
        };
    }
}
