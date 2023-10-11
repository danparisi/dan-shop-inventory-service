package com.danshop.inventory.service;

import com.danshop.inventory.api.v1.CreateProductInventoryDTO;
import com.danshop.inventory.api.v1.ProductInventoryDTO;
import com.danshop.inventory.api.v1.ProductInventoryMapper;
import com.danshop.inventory.api.v1.UpdateProductInventoryDTO;
import com.danshop.inventory.persistency.model.ProductInventoryEntity;
import com.danshop.inventory.persistency.repository.ProductInventoryRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class ProductsInventoryService {
    private final ProductInventoryMapper productInventoryMapper;
    private final ProductInventoryRepository productInventoryRepository;

    public Optional<ProductInventoryDTO> find(final String code) {
        return productInventoryRepository.findByCode(code)
                .map(productInventoryMapper::map);
    }

    public Collection<ProductInventoryDTO> findAll() {
        return productInventoryRepository
                .findAll()
                .stream()
                .map(productInventoryMapper::map)
                .collect(toSet());
    }

    public ProductInventoryDTO add(@NotNull CreateProductInventoryDTO createProductInventoryDTO) {
        ProductInventoryEntity newProductInventory = ProductInventoryEntity.builder().code(createProductInventoryDTO.getCode()).build();

        createProductInventoryDTO
                .getInnerQuantity()
                .ifPresent(newProductInventory::setInnerQuantity);
        createProductInventoryDTO
                .getSupplierQuantity()
                .ifPresent(newProductInventory::setSupplierQuantity);

        return productInventoryMapper.map(
                productInventoryRepository.save(newProductInventory));
    }

    public ProductInventoryDTO addOrUpdate(@NotNull String code, @NotNull UpdateProductInventoryDTO updateProductInventoryDTO) {
        final ProductInventoryEntity productInventory = retrieveByCode(code)
                .orElseGet(() -> ProductInventoryEntity.builder()
                        .code(code).build());

        updateProductInventoryDTO
                .getInnerQuantity()
                .ifPresent(productInventory::setInnerQuantity);
        updateProductInventoryDTO
                .getSupplierQuantity()
                .ifPresent(productInventory::setSupplierQuantity);

        return productInventoryMapper.map(
                productInventoryRepository.save(productInventory));
    }

    public void delete(String code) {
        ProductInventoryEntity productInventory = productInventoryRepository
                .findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(format("Expected Product Inventory with code [%s] not found", code)));

        productInventoryRepository.delete(productInventory);
    }

    private Optional<ProductInventoryEntity> retrieveByCode(String code) {
        return productInventoryRepository
                .findByCode(code);
    }
}
