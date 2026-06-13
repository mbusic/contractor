package hr.qnr.contractor.controller;

import hr.qnr.contractor.dto.LoginRequest;
import hr.qnr.contractor.dto.LoginResponse;
import hr.qnr.contractor.entity.User;
import hr.qnr.contractor.repository.UserRepository;
import hr.qnr.contractor.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        User user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generate(user.getUsername(), user.getRole().name());

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                user.getDisplayName(),
                user.getId(),
                user.getClient() != null ? user.getClient().getId() : null,
                user.getBranch() != null ? user.getBranch().getId() : null
        );
    }
}
