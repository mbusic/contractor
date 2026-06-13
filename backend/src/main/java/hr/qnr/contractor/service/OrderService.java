package hr.qnr.contractor.service;

import hr.qnr.contractor.dto.*;
import hr.qnr.contractor.entity.*;
import hr.qnr.contractor.entity.Order.Status;
import hr.qnr.contractor.repository.*;
import hr.qnr.contractor.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final BranchRepository branchRepo;
    private final ClientRepository clientRepo;
    private final UserRepository userRepo;
    private final OrderNumberGenerator orderNumberGenerator;
    private final FileStorageService fileStorage;

    @Transactional(readOnly = true)
    public List<OrderSummaryDto> getOrders(UserPrincipal principal) {
        User user = principal.getUser();
        List<Order> orders = switch (user.getRole()) {
            case CLIENT   -> orderRepo.findByClientId(user.getClient().getId());
            case SERVICER -> orderRepo.findByAssignedServicerId(user.getId());
            default       -> orderRepo.findAllByOrderByCreatedAtDesc();
        };
        return orders.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long id, UserPrincipal principal) {
        Order order = findOrder(id);
        checkAccess(order, principal.getUser());
        return toDto(order);
    }

    @Transactional
    public OrderDto createOrder(OrderCreateRequest req, UserPrincipal principal) {
        User user = principal.getUser();

        Order.OrderBuilder builder = Order.builder()
                .orderNumber(orderNumberGenerator.next())
                .location(req.location())
                .contactPerson(req.contactPerson())
                .phone(req.phone())
                .email(req.email())
                .description(req.description())
                .urgency(req.urgency())
                .status(Status.PENDING);

        if (req.branchId() != null) {
            builder.branch(branchRepo.findById(req.branchId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Branch not found")));
        }

        if (user.getRole() == User.Role.CLIENT) {
            builder.client(user.getClient());
        } else if (req.clientId() != null) {
            builder.client(clientRepo.findById(req.clientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client not found")));
        }

        return toDto(orderRepo.save(builder.build()));
    }

    @Transactional
    public OrderDto updateOrder(Long id, OrderUpdateRequest req, UserPrincipal principal) {
        Order order = findOrder(id);
        checkAccess(order, principal.getUser());

        if (req.branchId() != null) {
            order.setBranch(branchRepo.findById(req.branchId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Branch not found")));
        }
        if (req.clientId() != null) {
            order.setClient(clientRepo.findById(req.clientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client not found")));
        }
        if (req.assignedServicerId() != null) {
            order.setAssignedServicer(userRepo.findById(req.assignedServicerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servicer not found")));
        }

        if (req.location() != null)       order.setLocation(req.location());
        if (req.contactPerson() != null)  order.setContactPerson(req.contactPerson());
        if (req.phone() != null)          order.setPhone(req.phone());
        if (req.email() != null)          order.setEmail(req.email());
        if (req.description() != null)    order.setDescription(req.description());
        if (req.urgency() != null)        order.setUrgency(req.urgency());

        if (req.estimatedKm() != null)              order.setEstimatedKm(req.estimatedKm());
        if (req.estimatedWorkHours() != null)       order.setEstimatedWorkHours(req.estimatedWorkHours());
        if (req.estimatedNumberOfWorkers() != null) order.setEstimatedNumberOfWorkers(req.estimatedNumberOfWorkers());
        if (req.estimatedTotalHours() != null)      order.setEstimatedTotalHours(req.estimatedTotalHours());
        if (req.estimatedMaterialCost() != null)    order.setEstimatedMaterialCost(req.estimatedMaterialCost());

        if (req.actualKm() != null)              order.setActualKm(req.actualKm());
        if (req.actualWorkHours() != null)       order.setActualWorkHours(req.actualWorkHours());
        if (req.actualNumberOfWorkers() != null) order.setActualNumberOfWorkers(req.actualNumberOfWorkers());
        if (req.actualTotalHours() != null)      order.setActualTotalHours(req.actualTotalHours());
        if (req.actualMaterialCost() != null)    order.setActualMaterialCost(req.actualMaterialCost());

        return toDto(orderRepo.save(order));
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = findOrder(id);
        order.getPhotos().forEach(p -> {
            try { fileStorage.delete(p.getFilename()); } catch (IOException ignored) {}
        });
        orderRepo.delete(order);
    }

    @Transactional
    public OrderDto changeStatus(Long id, String statusStr, UserPrincipal principal) {
        Order order = findOrder(id);
        checkAccess(order, principal.getUser());
        order.setStatus(Status.valueOf(statusStr));
        return toDto(orderRepo.save(order));
    }

    @Transactional
    public OrderDto addNote(Long id, NoteRequest req, UserPrincipal principal) {
        Order order = findOrder(id);
        checkAccess(order, principal.getUser());
        OrderNote note = OrderNote.builder()
                .order(order)
                .text(req.text())
                .authorName(principal.getUser().getDisplayName())
                .build();
        order.getNotes().add(note);
        return toDto(orderRepo.save(order));
    }

    @Transactional
    public OrderDto addPhoto(Long id, MultipartFile file, UserPrincipal principal) throws IOException {
        Order order = findOrder(id);
        checkAccess(order, principal.getUser());
        if (order.getPhotos().size() >= 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 6 photos per order");
        }
        String filename = fileStorage.store(file);
        OrderPhoto photo = OrderPhoto.builder()
                .order(order)
                .filename(filename)
                .url("/api/files/" + filename)
                .build();
        order.getPhotos().add(photo);
        return toDto(orderRepo.save(order));
    }

    @Transactional
    public void deletePhoto(Long orderId, Long photoId) throws IOException {
        Order order = findOrder(orderId);
        OrderPhoto photo = order.getPhotos().stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));
        fileStorage.delete(photo.getFilename());
        order.getPhotos().remove(photo);
        orderRepo.save(order);
    }

    // --- mapping ---

    private OrderSummaryDto toSummary(Order o) {
        return new OrderSummaryDto(
                o.getId(),
                o.getOrderNumber(),
                o.getBranch() != null ? new BranchDto(o.getBranch().getId(), o.getBranch().getName(), o.getBranch().getCity()) : null,
                o.getUrgency(),
                o.getStatus().name(),
                o.getClient() != null ? o.getClient().getName() : null,
                o.getLocation()
        );
    }

    public OrderDto toDto(Order o) {
        UserDto servicerDto = null;
        if (o.getAssignedServicer() != null) {
            User s = o.getAssignedServicer();
            servicerDto = new UserDto(s.getId(), s.getUsername(), s.getRole().name(), s.getDisplayName(), null, null, null, null);
        }

        ClientDto clientDto = null;
        if (o.getClient() != null) {
            Client c = o.getClient();
            List<LocationDto> locs = c.getLocations().stream()
                    .map(l -> new LocationDto(l.getId(), l.getAddress(), l.getCity()))
                    .toList();
            clientDto = new ClientDto(c.getId(), c.getType().name(), c.getName(),
                    c.getContactPerson(), c.getPhone(), c.getEmail(), c.getAddress(), locs);
        }

        return new OrderDto(
                o.getId(), o.getOrderNumber(),
                o.getBranch() != null ? new BranchDto(o.getBranch().getId(), o.getBranch().getName(), o.getBranch().getCity()) : null,
                clientDto,
                o.getLocation(), o.getContactPerson(), o.getPhone(), o.getEmail(),
                o.getDescription(), o.getUrgency(), o.getStatus().name(),
                servicerDto,
                o.getEstimatedKm(), o.getEstimatedWorkHours(), o.getEstimatedNumberOfWorkers(),
                o.getEstimatedTotalHours(), o.getEstimatedMaterialCost(),
                o.getActualKm(), o.getActualWorkHours(), o.getActualNumberOfWorkers(),
                o.getActualTotalHours(), o.getActualMaterialCost(),
                o.getCreatedAt(), o.getUpdatedAt(),
                o.getNotes().stream().map(n -> new NoteDto(n.getId(), n.getText(), n.getAuthorName(), n.getCreatedAt())).toList(),
                o.getPhotos().stream().map(p -> new PhotoDto(p.getId(), p.getUrl())).toList()
        );
    }

    // --- helpers ---

    private Order findOrder(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private void checkAccess(Order order, User user) {
        if (user.getRole() == User.Role.CLIENT) {
            if (order.getClient() == null || !order.getClient().getId().equals(user.getClient().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } else if (user.getRole() == User.Role.SERVICER) {
            if (order.getAssignedServicer() == null || !order.getAssignedServicer().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
    }
}
