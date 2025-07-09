package com.zosh.service.clients;

import com.zosh.payload.dto.CategoryDTO;

import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("CATEGORY")
public interface CategoryFeignClient {

    @GetMapping("/api/categories/{id}")
    ResponseEntity<CategoryDTO> getCategoryById(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long id);

    @GetMapping("/api/categories/salon-owner")
    ResponseEntity<Set<CategoryDTO>> getCategoriesBySalonOwner(
            @RequestHeader("Authorization") String jwt,
            @RequestHeader(value = "X-Cognito-Sub", required = false) String cognitoSub,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Username", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Auth-Source", required = false) String authSource);

}
