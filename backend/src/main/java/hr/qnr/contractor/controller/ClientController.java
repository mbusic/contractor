package hr.qnr.contractor.controller;

import hr.qnr.contractor.dto.ClientCreateRequest;
import hr.qnr.contractor.dto.ClientDto;
import hr.qnr.contractor.dto.LocationCreateRequest;
import hr.qnr.contractor.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<ClientDto> list() {
        return clientService.getAll();
    }

    @GetMapping("/{id}")
    public ClientDto get(@PathVariable Long id) {
        return clientService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDto create(@RequestBody ClientCreateRequest req) {
        return clientService.create(req);
    }

    @PutMapping("/{id}")
    public ClientDto update(@PathVariable Long id, @RequestBody ClientCreateRequest req) {
        return clientService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }

    @PostMapping("/{id}/locations")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDto addLocation(@PathVariable Long id, @RequestBody LocationCreateRequest req) {
        return clientService.addLocation(id, req);
    }

    @DeleteMapping("/{id}/locations/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Long id, @PathVariable Long locationId) {
        clientService.deleteLocation(id, locationId);
    }
}
