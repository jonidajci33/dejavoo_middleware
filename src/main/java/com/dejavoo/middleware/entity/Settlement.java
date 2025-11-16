package com.dejavoo.middleware.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement", indexes = {
    @Index(name = "idx_settlement_id", columnList = "settlement_id", unique = true),
    @Index(name = "idx_merchant_id", columnList = "merchant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_id", nullable = false, unique = true, length = 100)
    private String settlementId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "merchant_id", referencedColumnName = "id")
    private Merchant merchant;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "settlement_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal settlementAmount;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "settlement_count")
    private Integer settlementCount;

    @Column(name = "tpn", length = 50)
    private String tpn;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "sub_event_type", length = 50)
    private String subEventType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
