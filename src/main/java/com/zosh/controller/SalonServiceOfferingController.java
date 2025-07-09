// =============================================================================
// SERVICE-OFFERING - SalonServiceOfferingController FIX BigDecimal
// backend/microservices/service-offering/src/main/java/com/zosh/controller/SalonServiceOfferingController.java
// =============================================================================
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-offering/salon-owner")
public class SalonServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;
    private final SalonFeignClient salonService;
    private final CategoryFeignClient categoryService;

    @PostMapping
    public ResponseEntity<?> createService(
            @RequestHeader("Authorization") String jwt,
            @RequestBody ServiceDTO service) {

        System.out.println("🛠️ SERVICE-OFFERING - createService");
        System.out.println("📋 Datos recibidos:");
        System.out.println("   Nombre: " + service.getName());
        System.out.println("   Categoría ID: " + service.getCategory());
        System.out.println("   Precio: " + service.getPrice());
        System.out.println("   Duración: " + service.getDuration());

        try {
            // 🚀 PASO 1: VALIDAR DATOS DE ENTRADA CON BIGDECIMAL
            if (service.getName() == null || service.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("El nombre del servicio es obligatorio"));
            }

            if (service.getCategory() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("La categoría es obligatoria"));
            }

            // 🚀 FIX BIGDECIMAL: Usar método de comparación correcto
            if (service.getPrice() == null || service.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("El precio debe ser mayor a 0"));
            }

            // 🚀 VALIDAR DURACIÓN
            if (service.getDuration() == null || service.getDuration() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("La duración debe ser mayor a 0 minutos"));
            }

            // 🚀 PASO 2: OBTENER SALÓN DEL USUARIO
            System.out.println("🔍 PASO 2: Obteniendo salón del usuario...");
            SalonDTO salon = null;

            try {
                ResponseEntity<SalonDTO> salonResponse = salonService.getSalonByOwner(jwt);
                salon = salonResponse.getBody();

                if (salon == null) {
                    System.out.println("❌ SALÓN ES NULL");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createErrorResponse(
                                    "Usuario no tiene salón registrado. Debe crear un salón primero."));
                }

                System.out.println("✅ Salón encontrado: " + salon.getName() + " (ID: " + salon.getId() + ")");

            } catch (Exception e) {
                System.err.println("❌ Error obteniendo salón: " + e.getMessage());

                if (e.getMessage() != null
                        && (e.getMessage().contains("404") || e.getMessage().contains("not found"))) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createErrorResponse(
                                    "Usuario no tiene salón registrado. Debe crear un salón primero antes de crear servicios."));
                }

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Error interno obteniendo información del salón: " + e.getMessage()));
            }

            // 🚀 PASO 3: VERIFICAR QUE LA CATEGORÍA EXISTE
            System.out.println("🔍 PASO 3: Verificando categoría ID: " + service.getCategory());
            CategoryDTO category = null;

            try {
                ResponseEntity<CategoryDTO> categoryResponse = categoryService.getCategoryById(service.getCategory());
                category = categoryResponse.getBody();

                if (category == null) {
                    System.out.println("❌ CATEGORÍA NO ENCONTRADA");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createErrorResponse(
                                    "La categoría seleccionada no existe. ID: " + service.getCategory()));
                }

                System.out.println("✅ Categoría encontrada: " + category.getName() + " (ID: " + category.getId() + ")");

            } catch (Exception e) {
                System.err.println("❌ Error obteniendo categoría: " + e.getMessage());

                if (e.getMessage() != null
                        && (e.getMessage().contains("404") || e.getMessage().contains("not found"))) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createErrorResponse(
                                    "La categoría seleccionada no existe. Debe crear categorías primero."));
                }

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Error interno verificando categoría: " + e.getMessage()));
            }

            // 🚀 PASO 4: CREAR EL SERVICIO
            System.out.println("🔍 PASO 4: Creando servicio...");
            ServiceOffering createdService = serviceOfferingService.createService(service, salon, category);

            if (createdService == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Error creando el servicio"));
            }

            System.out.println("✅ Servicio creado exitosamente:");
            System.out.println("   ID: " + createdService.getId());
            System.out.println("   Nombre: " + createdService.getName());
            System.out.println("   Salón: " + createdService.getSalonId());
            System.out.println("   Categoría: " + createdService.getCategoryId());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdService);

        } catch (Exception e) {
            System.err.println("❌ Error general creando servicio: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno del servidor: " + e.getMessage()));
        }
    }

    @PatchMapping("/{serviceId}")
    public ResponseEntity<?> updateService(
            @PathVariable Long serviceId,
            @RequestHeader("Authorization") String jwt,
            @RequestBody ServiceOffering service) {

        System.out.println("🛠️ SERVICE-OFFERING - updateService ID: " + serviceId);

        try {
            // Verificar que el usuario tenga un salón
            try {
                ResponseEntity<SalonDTO> salonResponse = salonService.getSalonByOwner(jwt);
                SalonDTO salon = salonResponse.getBody();

                if (salon == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createErrorResponse("Usuario no tiene salón registrado"));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Error verificando salón del usuario"));
            }

            ServiceOffering updatedService = serviceOfferingService.updateService(serviceId, service);

            if (updatedService != null) {
                System.out.println("✅ Servicio actualizado exitosamente: " + updatedService.getName());
                return ResponseEntity.ok(updatedService);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Servicio no encontrado"));

        } catch (Exception e) {
            System.err.println("❌ Error actualizando servicio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno actualizando servicio: " + e.getMessage()));
        }
    }

    // =========================================================================
    // MÉTODO AUXILIAR PARA CREAR RESPUESTAS DE ERROR CONSISTENTES
    // =========================================================================
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
        return errorResponse;
    }

    // =========================================================================
    // ENDPOINT DE SALUD PARA VERIFICAR QUE EL CONTROLADOR FUNCIONA
    // =========================================================================
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "service-offering salon-owner controller");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}