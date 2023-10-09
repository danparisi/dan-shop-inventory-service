package com.danshop.inventory.persistency.repository;

import com.danshop.inventory.persistency.model.ProductInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductInventoryRepository extends JpaRepository<ProductInventoryEntity, Integer> {

    Optional<ProductInventoryEntity> findByCode(String code);
}
