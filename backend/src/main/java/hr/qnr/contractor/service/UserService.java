package hr.qnr.contractor.service;

import hr.qnr.contractor.dto.UserCreateRequest;
import hr.qnr.contractor.dto.UserDto;
import hr.qnr.contractor.entity.User;
import hr.qnr.contractor.repository.BranchRepository;
import hr.qnr.contractor.repository.ClientRepository;
import hr.qnr.contractor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final BranchRepository branchRepo;
    private final ClientRepository clientRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UserDto> getByRole(String role) {
        User.Role r = User.Role.valueOf(role.toUpperCase());
        return userRepo.findAll().stream()
                .filter(u -> u.getRole() == r)
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserDto create(UserCreateRequest req) {
        User u = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .role(User.Role.valueOf(req.role().toUpperCase()))
                .displayName(req.displayName())
                .build();
        if (req.branchId() != null) {
            u.setBranch(branchRepo.findById(req.branchId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Branch not found")));
        }
        if (req.clientId() != null) {
            u.setClient(clientRepo.findById(req.clientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client not found")));
        }
        return toDto(userRepo.save(u));
    }

    @Transactional
    public UserDto update(Long id, UserCreateRequest req) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (req.displayName() != null) u.setDisplayName(req.displayName());
        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.password()));
        }
        if (req.role() != null) u.setRole(User.Role.valueOf(req.role().toUpperCase()));
        if (req.branchId() != null) {
            u.setBranch(branchRepo.findById(req.branchId()).orElseThrow());
        }
        if (req.clientId() != null) {
            u.setClient(clientRepo.findById(req.clientId()).orElseThrow());
        }
        return toDto(userRepo.save(u));
    }

    public void delete(Long id) {
        userRepo.deleteById(id);
    }

    UserDto toDto(User u) {
        return new UserDto(
                u.getId(), u.getUsername(), u.getRole().name(), u.getDisplayName(),
                u.getClient() != null ? u.getClient().getId() : null,
                u.getClient() != null ? u.getClient().getName() : null,
                u.getBranch() != null ? u.getBranch().getId() : null,
                u.getBranch() != null ? u.getBranch().getName() : null
        );
    }
}
