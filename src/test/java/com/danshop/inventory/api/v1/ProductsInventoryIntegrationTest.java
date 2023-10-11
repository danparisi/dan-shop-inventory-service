package com.danshop.inventory.api.v1;

import com.danshop.inventory.persistency.model.ProductInventoryEntity;
import com.danshop.inventory.persistency.repository.ProductInventoryRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;

import static com.danshop.inventory.api.v1.ProductInventoryController.BASE_ENDPOINT_PRODUCTS_INVENTORY;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@EmbeddedKafka
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductsInventoryIntegrationTest {
    private static final Random RANDOM = new Random();
    private static final String A_MISSING_CODE = "missing-code";
    private static final String ENDPOINT_PRODUCT_INVENTORY = BASE_ENDPOINT_PRODUCTS_INVENTORY + "/{code}";

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @BeforeEach
    void beforeEach() {
        productInventoryRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldGetProductInventory() {
        ProductInventoryEntity productInventory = storeNewProductInventory();

        ResponseEntity<ProductInventoryDTO> response = testRestTemplate
                .getForEntity(ENDPOINT_PRODUCT_INVENTORY, ProductInventoryDTO.class, productInventory.getCode());

        ProductInventoryDTO actual = response.getBody();
        assertNotNull(actual);
        assertEquals(OK, response.getStatusCode());
        assertEquals(productInventory.getCode(), actual.getCode());
        assertEquals(productInventory.getInnerQuantity(), actual.getInnerQuantity());
        assertEquals(productInventory.getSupplierQuantity(), actual.getSupplierQuantity());
    }

    @Test
    @SneakyThrows
    void shouldNotGetProductInventory() {
        String aNotExistingProductCode = randomAlphabetic(10);

        ResponseEntity<ProductInventoryDTO> response = testRestTemplate
                .getForEntity(ENDPOINT_PRODUCT_INVENTORY, ProductInventoryDTO.class, aNotExistingProductCode);

        assertEquals(NO_CONTENT, response.getStatusCode());
    }

    @Test
    @SneakyThrows
    void shouldGetAllProductsInventory() {
        ProductInventoryEntity productInventory1 = storeNewProductInventory();
        ProductInventoryEntity productInventory2 = storeNewProductInventory();
        ProductInventoryEntity productInventory3 = storeNewProductInventory();

        ResponseEntity<ProductInventoryDTO[]> response = testRestTemplate
                .getForEntity(BASE_ENDPOINT_PRODUCTS_INVENTORY, ProductInventoryDTO[].class);

        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, ProductInventoryDTO> products = stream(response.getBody()).collect(toMap(ProductInventoryDTO::getCode, identity()));
        assertEquals(3, products.size());
        verifyExpectedProductInventoryInCollection(products, productInventory1);
        verifyExpectedProductInventoryInCollection(products, productInventory2);
        verifyExpectedProductInventoryInCollection(products, productInventory3);
    }

    @Test
    @SneakyThrows
    void shouldCreateProductInventory() {
        CreateProductInventoryDTO createProductInventoryDTO = CreateProductInventoryDTO.builder()
                .code(randomAlphabetic(10))
                .innerQuantity(OptionalInt.of(RANDOM.nextInt(11, 20)))
                .supplierQuantity(OptionalInt.of(RANDOM.nextInt(101, 200))).build();

        ResponseEntity<ProductInventoryDTO> result = testRestTemplate
                .postForEntity(BASE_ENDPOINT_PRODUCTS_INVENTORY, createProductInventoryDTO, ProductInventoryDTO.class);

        assertEquals(OK, result.getStatusCode());
        ProductInventoryDTO actual = result.getBody();
        assertNotNull(actual);
        assertEquals(createProductInventoryDTO.getCode(), actual.getCode());
        assertEquals(createProductInventoryDTO.getInnerQuantity().orElse(0), actual.getInnerQuantity());
        assertEquals(createProductInventoryDTO.getSupplierQuantity().orElse(0), actual.getSupplierQuantity());
        verifyDatabaseProduct(createProductInventoryDTO);
    }

    @Test
    @SneakyThrows
    void shouldUpdateProductInventory() {
        ProductInventoryEntity productInventory = storeNewProductInventory();
        String existingProductInventoryCode = productInventory.getCode();
        UpdateProductInventoryDTO updateProductInventoryDTO = UpdateProductInventoryDTO.builder()
                .innerQuantity(OptionalInt.of(RANDOM.nextInt(11, 20)))
                .supplierQuantity(OptionalInt.of(RANDOM.nextInt(101, 200))).build();

        ResponseEntity<ProductInventoryDTO> result = testRestTemplate
                .exchange(ENDPOINT_PRODUCT_INVENTORY, PUT, new HttpEntity<>(updateProductInventoryDTO), ProductInventoryDTO.class, existingProductInventoryCode);

        assertEquals(OK, result.getStatusCode());
        ProductInventoryDTO actual = result.getBody();
        assertNotNull(actual);
        assertEquals(existingProductInventoryCode, actual.getCode());
        assertEquals(updateProductInventoryDTO.getInnerQuantity().orElse(0), actual.getInnerQuantity());
        assertEquals(updateProductInventoryDTO.getSupplierQuantity().orElse(0), actual.getSupplierQuantity());
        verifyDatabaseProduct(updateProductInventoryDTO, existingProductInventoryCode);
    }

    @Test
    @SneakyThrows
    void shouldUpdateProductInventoryIfNotFound() {
        String aNotExistingProductCode = randomAlphabetic(10);
        UpdateProductInventoryDTO anUpdateProductInventoryDTO = UpdateProductInventoryDTO.builder().build();

        ResponseEntity<ProductInventoryDTO> result = testRestTemplate
                .exchange(ENDPOINT_PRODUCT_INVENTORY, PUT, new HttpEntity<>(anUpdateProductInventoryDTO), ProductInventoryDTO.class, aNotExistingProductCode);

        assertEquals(OK, result.getStatusCode());
        ProductInventoryDTO actual = result.getBody();
        assertNotNull(actual);
        assertEquals(aNotExistingProductCode, actual.getCode());
        verifyDatabaseProduct(anUpdateProductInventoryDTO, aNotExistingProductCode);
    }

    @Test
    @SneakyThrows
    void shouldDeleteProductInventory() {
        ProductInventoryEntity productInventory = storeNewProductInventory();
        String existingProductInventoryCode = productInventory.getCode();

        testRestTemplate.delete(ENDPOINT_PRODUCT_INVENTORY, existingProductInventoryCode);

        assertEquals(empty(), productInventoryRepository.findByCode(existingProductInventoryCode));
    }

    private void verifyDatabaseProduct(CreateProductInventoryDTO createProductInventoryDTO) {
        String productInventoryCode = createProductInventoryDTO.getCode();
        ProductInventoryEntity productInventoryEntity = retrieveMandatoryProductInventoryByCode(productInventoryCode);

        assertEquals(productInventoryCode, productInventoryEntity.getCode());
        assertEquals(createProductInventoryDTO.getInnerQuantity().orElse(0), productInventoryEntity.getInnerQuantity());
        assertEquals(createProductInventoryDTO.getSupplierQuantity().orElse(0), productInventoryEntity.getSupplierQuantity());
    }

    private void verifyDatabaseProduct(UpdateProductInventoryDTO updateProductInventoryDTO, String code) {
        ProductInventoryEntity productInventoryEntity = retrieveMandatoryProductInventoryByCode(code);

        assertEquals(code, productInventoryEntity.getCode());
        assertEquals(updateProductInventoryDTO.getInnerQuantity().orElse(0), productInventoryEntity.getInnerQuantity());
        assertEquals(updateProductInventoryDTO.getSupplierQuantity().orElse(0), productInventoryEntity.getSupplierQuantity());
    }

    private static void verifyExpectedProductInventoryInCollection(Map<String, ProductInventoryDTO> products, ProductInventoryEntity expected) {
        ProductInventoryDTO productInventoryDTO = products.get(expected.getCode());
        assertNotNull(productInventoryDTO);

        assertEquals(expected.getInnerQuantity(), productInventoryDTO.getInnerQuantity());
        assertEquals(expected.getSupplierQuantity(), productInventoryDTO.getSupplierQuantity());
    }

    @Test
    @SneakyThrows
    void shouldReturnNoContentIfProductNotFound() {
        ResponseEntity<ProductInventoryDTO> response = testRestTemplate
                .getForEntity(ENDPOINT_PRODUCT_INVENTORY, ProductInventoryDTO.class, A_MISSING_CODE);

        assertEquals(NO_CONTENT, response.getStatusCode());
    }

    private ProductInventoryEntity storeNewProductInventory() {
        return productInventoryRepository.save(
                ProductInventoryEntity.builder()
                        .code(randomAlphabetic(5))
                        .innerQuantity(RANDOM.nextInt(0, 10))
                        .supplierQuantity(RANDOM.nextInt(0, 100)).build());
    }

    private ProductInventoryEntity retrieveMandatoryProductInventoryByCode(String code) {
        return productInventoryRepository
                .findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(format("Expected Product Inventory with code [%s] not found", code)));
    }
}
