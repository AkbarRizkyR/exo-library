package id.shellrean.exo.service;

import id.shellrean.exo.dto.LoginRequest;
import id.shellrean.exo.dto.LoginResponse;
import id.shellrean.exo.dto.RegisterRequest;
import id.shellrean.exo.entity.User;
import id.shellrean.exo.repository.AuthRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.jwt.build.Jwt;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class AuthService {

    // 📝 Inisialisasi Logger dengan benar
    private static final Logger LOGGER = Logger.getLogger(AuthService.class);

    @Inject
    AuthRepository AuthRepository;

    public LoginResponse authenticate(LoginRequest loginRequest) {
        LOGGER.info("Login attempt with email: " + loginRequest.getEmail());

        // 1️⃣ Cek apakah user ditemukan di database
        User user = AuthRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            LOGGER.warn("User not found with email: " + loginRequest.getEmail());
            return null;
        }

        LOGGER.info("User found: " + user.getEmail());

        // 2️⃣ Cek apakah password sesuai
        boolean isPasswordValid = BCrypt.checkpw(loginRequest.getPassword(), user.getPassword());
        if (!isPasswordValid) {
            LOGGER.warn("Invalid password for email: " + loginRequest.getEmail());
            return null;
        }

        // 3️⃣ Jika valid, generate JWT token
        String token = Jwt.issuer("https://example.com")
                .upn(user.getEmail())
                .groups(new HashSet<>(List.of("User")))
                .expiresIn(Duration.ofHours(12))
                .sign();

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setName(user.getName());

        LOGGER.info("Authentication successful for: " + user.getEmail());

        return response;
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        // Cek apakah email sudah terdaftar
        if (AuthRepository.findByEmail(registerRequest.getEmail()) != null) {
            throw new RuntimeException("Email already registered.");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setName(registerRequest.getName());

        AuthRepository.persist(user);
    }
}
