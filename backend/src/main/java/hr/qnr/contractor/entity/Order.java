package hr.qnr.contractor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    public enum Status { DRAFT, PENDING, IN_PROGRESS, RESOLVED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber; // NNN/YY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    private String location;
    private String contactPerson;
    private String phone;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_servicer_id")
    private User assignedServicer;

    // Estimated cost block
    private Integer estimatedKm;
    private Double estimatedWorkHours;
    private Integer estimatedNumberOfWorkers;
    private Double estimatedTotalHours;
    private BigDecimal estimatedMaterialCost;

    // Actual cost block
    private Integer actualKm;
    private Double actualWorkHours;
    private Integer actualNumberOfWorkers;
    private Double actualTotalHours;
    private BigDecimal actualMaterialCost;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderNote> notes = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderPhoto> photos = new ArrayList<>();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
