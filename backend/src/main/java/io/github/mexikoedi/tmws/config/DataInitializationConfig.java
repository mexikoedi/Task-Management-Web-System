package io.github.mexikoedi.tmws.config;

import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializationConfig {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializationConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public ApplicationRunner initializeAdminUser() {
        return args -> {
            // Erstelle Admin-Benutzer wenn nicht vorhanden
            if (!userRepository.existsByEmail("admin@tmws.local")) {
                User admin = new User();
                admin.setName("Administrator");
                admin.setEmail("admin@tmws.local");
                admin.setPassword(passwordEncoder.encode("Admin@123456789"));
                admin.setEnabled(true);
                admin.setEmailVerified(true);
                userRepository.save(admin);
                System.out.println("Admin-Benutzer erstellt: admin@tmws.local / Admin@123456789");
            }

            // Erstelle Demo-Benutzer wenn nicht vorhanden
            if (!userRepository.existsByEmail("demo@tmws.local")) {
                User demo = new User();
                demo.setName("Demo User");
                demo.setEmail("demo@tmws.local");
                demo.setPassword(passwordEncoder.encode("Demo@1234567"));
                demo.setEnabled(true);
                demo.setEmailVerified(true);
                userRepository.save(demo);
                System.out.println("Demo-Benutzer erstellt: demo@tmws.local / Demo@1234567");
            }
        };
    }
}


