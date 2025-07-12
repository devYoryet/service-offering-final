package com.zosh.service.impl;

import com.zosh.modal.ServiceOffering;
import com.zosh.payload.dto.CategoryDTO;
import com.zosh.payload.dto.SalonDTO;
import com.zosh.payload.dto.ServiceDTO;
import com.zosh.repository.ServiceOfferingRepository;
import com.zosh.service.ServiceOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceOfferingImpl implements ServiceOfferingService {

    private final ServiceOfferingRepository serviceOfferingRepository;

    /*
     * ────────────────────────────────
     * Helpers
     * ────────────────────────────────
     */
    private BigDecimal toMoney(Number num) {
        if (num == null)
            return BigDecimal.ZERO.setScale(2);
        // Si ya viene BigDecimal, respétalo
        if (num instanceof BigDecimal bd)
            return bd.setScale(2);
        // Convierte int/Integer/long/etc a BigDecimal con 2 decimales
        return BigDecimal.valueOf(num.doubleValue()).setScale(2);
    }

    /*
     * ────────────────────────────────
     * CRUD
     * ────────────────────────────────
     */
    @Override
    public ServiceOffering createService(ServiceDTO serviceDTO,
            SalonDTO salonDTO,
            CategoryDTO categoryDTO) {

        ServiceOffering serviceOffering = new ServiceOffering();
        serviceOffering.setName(serviceDTO.getName());
        serviceOffering.setDescription(serviceDTO.getDescription());
        serviceOffering.setPrice(toMoney(serviceDTO.getPrice()));
        serviceOffering.setDuration(serviceDTO.getDuration());
        serviceOffering.setImage(serviceDTO.getImage());
        serviceOffering.setSalonId(salonDTO.getId());
        serviceOffering.setCategoryId(categoryDTO != null ? categoryDTO.getId() : null);

        return serviceOfferingRepository.save(serviceOffering);
    }

    @Override
    public ServiceOffering updateService(Long serviceId, ServiceOffering incoming) throws Exception {
        Optional<ServiceOffering> existingOpt = serviceOfferingRepository.findById(serviceId);

        if (existingOpt.isEmpty())
            throw new Exception("Service not found with id " + serviceId);

        ServiceOffering target = existingOpt.get();
        target.setName(incoming.getName());
        target.setDescription(incoming.getDescription());
        target.setPrice(toMoney(incoming.getPrice()));
        target.setDuration(incoming.getDuration());

        if (incoming.getImage() != null && !incoming.getImage().isBlank()) {
            target.setImage(incoming.getImage());
        }
        if (incoming.getCategoryId() != null) {
            target.setCategoryId(incoming.getCategoryId());
        }

        return serviceOfferingRepository.save(target);
    }

    @Override
    public Set<ServiceOffering> getAllServicesBySalonId(Long salonId, Long categoryId) {
        Set<ServiceOffering> services = serviceOfferingRepository.findBySalonId(salonId);
        if (categoryId != null) {
            services = services.stream()
                    .filter(s -> categoryId.equals(s.getCategoryId()))
                    .collect(Collectors.toSet());
        }
        return services;
    }

    @Override
    public ServiceOffering getServiceById(Long serviceId) {
        return serviceOfferingRepository.findById(serviceId).orElse(null);
    }

    @Override
    public Set<ServiceOffering> getServicesByIds(Set<Long> ids) {
        List<ServiceOffering> list = serviceOfferingRepository.findAllById(ids);
        return new HashSet<>(list);
    }

    @Override
    public void deleteService(Long serviceId) throws Exception {
        ServiceOffering service = serviceOfferingRepository.findById(serviceId)
                .orElseThrow(() -> new Exception("Servicio no encontrado con ID: " + serviceId));

        serviceOfferingRepository.delete(service);
    }
}
