// =============================================================================
// SERVICE DTO - Fix BigDecimal comparisons
// backend/microservices/service-offering/src/main/java/com/zosh/payload/dto/ServiceDTO.java
// =============================================================================
package com.zosh.payload.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDTO {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration; // en minutos
    private String image;
    private Long category; // ID de la categoría
    private Long salonId; // ID del salón (opcional)

    // Métodos de validación para evitar comparaciones problemáticas
    public boolean isValidPrice() {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isValidDuration() {
        return duration != null && duration > 0;
    }

    public boolean hasValidCategory() {
        return category != null && category > 0;
    }

    // Getters y setters adicionales para compatibilidad
    public Double getPriceAsDouble() {
        return price != null ? price.doubleValue() : null;
    }

    public void setPriceFromDouble(Double priceValue) {
        this.price = priceValue != null ? BigDecimal.valueOf(priceValue) : null;
    }
}