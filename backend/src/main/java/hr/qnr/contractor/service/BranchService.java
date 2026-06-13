package hr.qnr.contractor.service;

import hr.qnr.contractor.dto.BranchDto;
import hr.qnr.contractor.entity.Branch;
import hr.qnr.contractor.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepo;

    public List<BranchDto> getAll() {
        return branchRepo.findAll().stream().map(this::toDto).toList();
    }

    public BranchDto create(BranchDto req) {
        return toDto(branchRepo.save(Branch.builder().name(req.name()).city(req.city()).build()));
    }

    public BranchDto update(Long id, BranchDto req) {
        Branch b = branchRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        b.setName(req.name());
        b.setCity(req.city());
        return toDto(branchRepo.save(b));
    }

    public void delete(Long id) {
        branchRepo.deleteById(id);
    }

    private BranchDto toDto(Branch b) {
        return new BranchDto(b.getId(), b.getName(), b.getCity());
    }
}
