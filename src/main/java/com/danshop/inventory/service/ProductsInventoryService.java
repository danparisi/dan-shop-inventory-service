package com.danshop.inventory.service;

import com.danshop.inventory.api.v1.ProductInventoryDTO;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class ProductsInventoryService {
    private final Map<String, Inventory> PRODUCTS_INVENTORY = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        PRODUCTS_INVENTORY.put("123", Inventory.builder().innerQuantity(1).supplierQuantity(5).build());
        PRODUCTS_INVENTORY.put("456", Inventory.builder().innerQuantity(3).supplierQuantity(10).build());
        PRODUCTS_INVENTORY.put("789", Inventory.builder().innerQuantity(0).supplierQuantity(50).build());
    }

    public Optional<ProductInventoryDTO> find(final String code) {
        return ofNullable(PRODUCTS_INVENTORY.get(code))
                .map(inventory -> toProductInventoryDTO(code, inventory));
    }

    public Collection<ProductInventoryDTO> findAll() {
        return PRODUCTS_INVENTORY
                .entrySet()
                .stream()
                .map(entry -> toProductInventoryDTO(entry.getKey(), entry.getValue()))
                .collect(toSet());
    }

    private static ProductInventoryDTO toProductInventoryDTO(String code, Inventory inventory) {
        return ProductInventoryDTO.builder()
                .code(code)
                .innerQuantity(inventory.getInnerQuantity())
                .supplierQuantity(inventory.getSupplierQuantity()).build();
    }

    @Data
    @Builder
    static class Inventory {
        private int innerQuantity = 0;
        private int supplierQuantity = 0;
    }
}
