package hr.qnr.contractor.controller;

import hr.qnr.contractor.dto.*;
import hr.qnr.contractor.security.UserPrincipal;
import hr.qnr.contractor.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderSummaryDto> list(@AuthenticationPrincipal UserPrincipal principal) {
        return orderService.getOrders(principal);
    }

    @GetMapping("/{id}")
    public OrderDto get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return orderService.getOrder(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@RequestBody OrderCreateRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        return orderService.createOrder(req, principal);
    }

    @PutMapping("/{id}")
    public OrderDto update(@PathVariable Long id, @RequestBody OrderUpdateRequest req,
                           @AuthenticationPrincipal UserPrincipal principal) {
        return orderService.updateOrder(id, req, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

    @PatchMapping("/{id}/status")
    public OrderDto changeStatus(@PathVariable Long id, @RequestBody StatusChangeRequest req,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        return orderService.changeStatus(id, req.status(), principal);
    }

    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto addNote(@PathVariable Long id, @RequestBody NoteRequest req,
                            @AuthenticationPrincipal UserPrincipal principal) {
        return orderService.addNote(id, req, principal);
    }

    @PostMapping("/{id}/photos")
    public OrderDto addPhoto(@PathVariable Long id,
                             @RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        return orderService.addPhoto(id, file, principal);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto(@PathVariable Long id, @PathVariable Long photoId) throws IOException {
        orderService.deletePhoto(id, photoId);
    }
}
