package hr.qnr.contractor.service;

import hr.qnr.contractor.dto.ClientCreateRequest;
import hr.qnr.contractor.dto.ClientDto;
import hr.qnr.contractor.dto.LocationDto;
import hr.qnr.contractor.entity.Client;
import hr.qnr.contractor.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepo;

    @Transactional(readOnly = true)
    public List<ClientDto> getAll() {
        return clientRepo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ClientDto getById(Long id) {
        return toDto(clientRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @Transactional
    public ClientDto create(ClientCreateRequest req) {
        Client c = Client.builder()
                .type(Client.ClientType.valueOf(req.type()))
                .name(req.name())
                .contactPerson(req.contactPerson())
                .phone(req.phone())
                .email(req.email())
                .address(req.address())
                .build();
        return toDto(clientRepo.save(c));
    }

    @Transactional
    public ClientDto update(Long id, ClientCreateRequest req) {
        Client c = clientRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (req.name() != null)          c.setName(req.name());
        if (req.contactPerson() != null) c.setContactPerson(req.contactPerson());
        if (req.phone() != null)         c.setPhone(req.phone());
        if (req.email() != null)         c.setEmail(req.email());
        if (req.address() != null)       c.setAddress(req.address());
        return toDto(clientRepo.save(c));
    }

    public void delete(Long id) {
        clientRepo.deleteById(id);
    }

    ClientDto toDto(Client c) {
        List<LocationDto> locs = c.getLocations().stream()
                .map(l -> new LocationDto(l.getId(), l.getAddress(), l.getCity()))
                .toList();
        return new ClientDto(c.getId(), c.getType().name(), c.getName(),
                c.getContactPerson(), c.getPhone(), c.getEmail(), c.getAddress(), locs);
    }
}
