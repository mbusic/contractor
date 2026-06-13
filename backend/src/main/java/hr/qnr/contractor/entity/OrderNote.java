package hr.qnr.contractor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "order_notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    private String authorName;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
