package hr.qnr.contractor.controller;

import hr.qnr.contractor.dto.BranchDto;
import hr.qnr.contractor.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public List<BranchDto> list() {
        return branchService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchDto create(@RequestBody BranchDto req) {
        return branchService.create(req);
    }

    @PutMapping("/{id}")
    public BranchDto update(@PathVariable Long id, @RequestBody BranchDto req) {
        return branchService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        branchService.delete(id);
    }
}
