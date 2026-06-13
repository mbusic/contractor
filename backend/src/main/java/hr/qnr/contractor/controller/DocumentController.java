package hr.qnr.contractor.controller;

import hr.qnr.contractor.entity.Order;
import hr.qnr.contractor.repository.OrderRepository;
import hr.qnr.contractor.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders/{id}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final OrderRepository orderRepo;
    private final DocumentService documentService;

    @GetMapping(value = "/quote", produces = MediaType.TEXT_HTML_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<String> quote(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.generateQuote(findOrder(id)));
    }

    @GetMapping(value = "/workorder", produces = MediaType.TEXT_HTML_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<String> workOrder(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.generateWorkOrder(findOrder(id)));
    }

    @GetMapping(value = "/report", produces = MediaType.TEXT_HTML_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<String> report(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.generateReport(findOrder(id)));
    }

    @GetMapping(value = "/invoice", produces = MediaType.TEXT_HTML_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<String> invoice(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.generateInvoice(findOrder(id)));
    }

    private Order findOrder(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
