package hr.qnr.contractor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_photos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private String filename;   // stored filename on disk
    private String url;        // served URL path
}
