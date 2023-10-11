package com.danshop.inventory.api.v1;

import com.danshop.inventory.service.ProductsInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static com.danshop.inventory.api.v1.ProductInventoryController.BASE_ENDPOINT_PRODUCTS_INVENTORY;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(BASE_ENDPOINT_PRODUCTS_INVENTORY)
public class ProductInventoryController {
    static final String BASE_ENDPOINT_PRODUCTS_INVENTORY = "/v1/products";

    private final ProductsInventoryService productsInventoryService;

    @GetMapping("/{code}")
    public ResponseEntity<ProductInventoryDTO> get(@PathVariable String code) {
        log.info("Returning product [{}] inventory", code);

        return productsInventoryService
                .find(code)
                .map(ResponseEntity::ok)
                .orElse(noContent().build());
    }

    @GetMapping
    public ResponseEntity<Collection<ProductInventoryDTO>> getAll() {
        log.info("Returning all products inventory");

        return ok(productsInventoryService.findAll());
    }

    @PostMapping
    public ResponseEntity<ProductInventoryDTO> add(@RequestBody @Valid CreateProductInventoryDTO createProductInventoryDTO) {
        String productInventoryCode = createProductInventoryDTO.getCode();
        log.info("Adding new product [{}] inventory", productInventoryCode);

        return ok(productsInventoryService
                .add(createProductInventoryDTO));
    }

    @PutMapping("/{code}")
    public ResponseEntity<ProductInventoryDTO> update(@PathVariable String code,
                                                      @RequestBody @Valid UpdateProductInventoryDTO updateProductInventoryDTO) {
        log.info("Updating product [{}] inventory", code);

        return ok(productsInventoryService
                .addOrUpdate(code, updateProductInventoryDTO));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        log.info("Deleting product [{}] inventory", code);

        productsInventoryService.delete(code);
        return ok().build();
    }

}
