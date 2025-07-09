package com.zosh.controller;

import com.zosh.modal.ServiceOffering;
import com.zosh.payload.dto.CategoryDTO;
import com.zosh.payload.dto.SalonDTO;
import com.zosh.payload.dto.ServiceDTO;
import com.zosh.payload.dto.UserDTO;
import com.zosh.service.ServiceOfferingService;
import com.zosh.service.clients.CategoryFeignClient;
import com.zosh.service.clients.SalonFeignClient;
import com.zosh.service.clients.UserFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-offering/salon-owner")
public class SalonServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;
    private final SalonFeignClient salonService;
    private final CategoryFeignClient categoryService;

    @PostMapping
    public ResponseEntity<ServiceOffering> createService(
            @RequestHeader("Authorization") String jwt,
            @RequestBody ServiceDTO service) {

        System.out.println("🛠️ SERVICE-OFFERING - createService");

        try {
            // 🚀 OBTENER SALÓN CON MANEJO DE ERRORES
            SalonDTO salon = null;
            try {
                salon = salonService.getSalonByOwner(jwt).getBody();
            } catch (Exception e) {
                System.err.println("❌ Error obteniendo salón: " + e.getMessage());

                // 🚨 MANEJO ESPECÍFICO DE ERRORES
                if (e.getMessage().contains("404") || e.getMessage().contains("not found")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(null); // O crear una respuesta de error personalizada
                }
                throw e; // Re-lanzar otros errores
            }

            if (salon == null) {
                System.out.println("❌ Usuario no tiene salón registrado");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            System.out.println("✅ Salón encontrado: " + salon.getName());

            // 🚀 OBTENER CATEGORÍA CON MANEJO DE ERRORES
            CategoryDTO category = null;
            try {
                category = categoryService.getCategoryById(service.getCategory()).getBody();
            } catch (Exception e) {
                System.err.println("❌ Error obteniendo categoría: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            if (category == null) {
                System.out.println("❌ Categoría no encontrada");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // 🚀 CREAR SERVICIO
            ServiceOffering createdService = serviceOfferingService.createService(service, salon, category);

            return new ResponseEntity<>(createdService, HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("❌ Error general creando servicio: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{serviceId}")
    public ResponseEntity<ServiceOffering> updateService(
            @PathVariable Long serviceId,
            @RequestBody ServiceOffering service) {

        System.out.println("🛠️ SERVICE-OFFERING - updateService");

        try {
            ServiceOffering updatedService = serviceOfferingService.updateService(serviceId, service);

            if (updatedService != null) {
                return new ResponseEntity<>(updatedService, HttpStatus.OK);
            }

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            System.err.println("❌ Error actualizando servicio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
