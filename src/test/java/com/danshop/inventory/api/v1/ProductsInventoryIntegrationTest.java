package com.danshop.inventory.api.v1;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.List;

import static com.danshop.inventory.api.v1.ProductInventoryController.BASE_ENDPOINT_PRODUCTS_INVENTORY;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@EmbeddedKafka
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductsInventoryIntegrationTest {
    private static final int AN_INNER_QUANTITY = 1;
    public static final int A_SUPPLIER_QUANTITY = 5;
    private static final String A_PRODUCT_CODE_123 = "123";
    private static final String A_MISSING_CODE = "missing-code";
    private static final String ENDPOINT_ALL_PRODUCTS_INVENTORY = BASE_ENDPOINT_PRODUCTS_INVENTORY;
    private static final String ENDPOINT_PRODUCT_INVENTORY = BASE_ENDPOINT_PRODUCTS_INVENTORY + "/{code}";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @SneakyThrows
    void shouldGetProductInventory() {
        ResponseEntity<ProductInventoryDTO> response = testRestTemplate
                .getForEntity(ENDPOINT_PRODUCT_INVENTORY, ProductInventoryDTO.class, A_PRODUCT_CODE_123);

        ProductInventoryDTO actual = response.getBody();
        assertNotNull(actual);
        assertEquals(OK, response.getStatusCode());
        assertEquals(A_PRODUCT_CODE_123, actual.getCode());
        assertEquals(AN_INNER_QUANTITY, actual.getInnerQuantity());
        assertEquals(A_SUPPLIER_QUANTITY, actual.getSupplierQuantity());
    }

    @Test
    @SneakyThrows
    void shouldGetAllProductsInventory() {
        ResponseEntity<ProductInventoryDTO[]> response = testRestTemplate
                .getForEntity(ENDPOINT_ALL_PRODUCTS_INVENTORY, ProductInventoryDTO[].class);

        List<ProductInventoryDTO> actual = asList(response.getBody());
        assertEquals(OK, response.getStatusCode());
        assertEquals(3, actual.size());
    }

    @Test
    @SneakyThrows
    void shouldReturnNoContentIfProductNotFound() {
        ResponseEntity<ProductInventoryDTO> response = testRestTemplate
                .getForEntity(ENDPOINT_PRODUCT_INVENTORY, ProductInventoryDTO.class, A_MISSING_CODE);

        assertEquals(NO_CONTENT, response.getStatusCode());
    }
}
