package hr.qnr.contractor.repository;

import hr.qnr.contractor.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClientId(Long clientId);
    List<Order> findByAssignedServicerId(Long servicerId);
    List<Order> findAllByOrderByCreatedAtDesc();
}
