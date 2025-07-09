    package com.zosh.modal;

    import jakarta.persistence.*;
    import lombok.*;
    import org.hibernate.annotations.CreationTimestamp;
    import org.hibernate.annotations.UpdateTimestamp;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;

    @Entity
    @Table(name = "service_offerings")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class ServiceOffering {

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_offerings_seq_gen")
        @SequenceGenerator(name = "service_offerings_seq_gen", sequenceName = "service_offerings_seq", allocationSize = 1)
        private Long id;

        @Column(name = "salon_id", nullable = false)
        private Long salonId;

        @Column(name = "category_id")
        private Long categoryId;

        @Column(nullable = false, length = 255)
        private String name;

        @Lob
        @Column(name = "description")
        private String description; // CLOB en Oracle

        @Column(name = "image", nullable = true, length = 500)
        private String image;

        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal price; // Para NUMBER(10,2)

        @Column(nullable = false)
        private Integer duration; // en minutos

        @Column(name = "available")
        private Boolean available = true;

        @Column(name = "created_at", nullable = false, updatable = false)
        @CreationTimestamp
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false)
        @UpdateTimestamp
        private LocalDateTime updatedAt;
    }
