package hr.qnr.contractor.repository;

import hr.qnr.contractor.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
