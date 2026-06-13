package hr.qnr.contractor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_sequences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderSequence {

    @Id
    @Column(name = "seq_year")
    private Integer year;

    @Column(nullable = false)
    @Builder.Default
    private int lastSequence = 0;
}
