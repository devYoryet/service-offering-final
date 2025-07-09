package com.zosh.controller;

import com.zosh.modal.ServiceOffering;
import com.zosh.payload.dto.CategoryDTO;
import com.zosh.payload.dto.SalonDTO;
import com.zosh.payload.dto.ServiceDTO;
import com.zosh.service.ServiceOfferingService;
import com.zosh.service.clients.CategoryFeignClient;
import com.zosh.service.clients.SalonFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-offering/salon-owner")
public class SalonServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;
    private final SalonFeignClient salonService;
    private final CategoryFeignClient categoryService;

    // ========================
    // CREAR SERVICIO
    // ========================
    @PostMapping
    public ResponseEntity<?> createService(
            @RequestHeader("Authorization") String jwt,
            @RequestBody ServiceDTO service) {
        try {
            System.out.println("👉 category recibida: " + service.getCategory());

            if (service.getName() == null || service.getName().trim().isEmpty()) {
                return badRequest("El nombre del servicio es obligatorio");
            }

            if (service.getCategory() == null) {
                return badRequest("La categoría es obligatoria");
            }

            if (service.getPrice() == null || service.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return badRequest("El precio debe ser mayor a 0");
            }

            if (service.getDuration() == null || service.getDuration() <= 0) {
                return badRequest("La duración debe ser mayor a 0 minutos");
            }

            SalonDTO salon = salonService.getSalonByOwner(jwt).getBody();
            if (salon == null) {
                return badRequest("Usuario no tiene salón registrado. Debe crear un salón primero.");
            }

            CategoryDTO category = categoryService.getCategoryById(jwt, service.getCategory()).getBody();
            if (category == null) {
                return badRequest("La categoría seleccionada no existe. Debe crear categorías primero.");
            }

            ServiceOffering createdService = serviceOfferingService.createService(service, salon, category);
            if (createdService == null) {
                return serverError("Error creando el servicio");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(createdService);

        } catch (Exception e) {
            return serverError("Error interno del servidor: " + e.getMessage());
        }
    }

    // ========================
    // ACTUALIZAR SERVICIO
    // ========================
    @PatchMapping("/{serviceId}")
    public ResponseEntity<?> updateService(
            @PathVariable Long serviceId,
            @RequestHeader("Authorization") String jwt,
            @RequestBody ServiceOffering service) {

        try {
            SalonDTO salon = salonService.getSalonByOwner(jwt).getBody();
            if (salon == null) {
                return badRequest("Usuario no tiene salón registrado");
            }

            ServiceOffering updatedService = serviceOfferingService.updateService(serviceId, service);
            if (updatedService != null) {
                return ResponseEntity.ok(updatedService);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("Servicio no encontrado"));

        } catch (Exception e) {
            return serverError("Error actualizando servicio: " + e.getMessage());
        }
    }

    // ========================
    // NUEVO ENDPOINT - Obtener categorías del salón del dueño
    // ========================
    @GetMapping("/categories")
    public ResponseEntity<?> getCategoriesBySalonOwner(
            @RequestHeader("Authorization") String jwt,
            @RequestHeader(value = "X-Cognito-Sub", required = false) String cognitoSub,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Username", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Auth-Source", required = false) String authSource) {
        try {
            ResponseEntity<Set<CategoryDTO>> response = categoryService.getCategoriesBySalonOwner(
                    jwt, cognitoSub, userEmail, username, userRole, authSource);

            Set<CategoryDTO> categories = response.getBody();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return serverError("Error obteniendo categorías del salón: " + e.getMessage());
        }
    }

    // ========================
    // HEALTHCHECK
    // ========================
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "service-offering salon-owner controller");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // ========================
    // MÉTODOS AUXILIARES
    // ========================
    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(message));
    }

    private ResponseEntity<Map<String, Object>> serverError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(message));
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
        return errorResponse;
    }
}
