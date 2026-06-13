package hr.qnr.contractor.repository;

import hr.qnr.contractor.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {
}
