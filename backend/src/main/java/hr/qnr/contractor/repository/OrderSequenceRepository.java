package hr.qnr.contractor.repository;

import hr.qnr.contractor.entity.OrderSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSequenceRepository extends JpaRepository<OrderSequence, Integer> {
}
