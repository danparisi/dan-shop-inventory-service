package com.danshop.inventory.api.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.OptionalInt;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static lombok.Builder.Default;

@Data
@Builder
@JsonInclude(NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class CreateProductInventoryDTO {
    @NotEmpty
    private String code;

    @Default
    private OptionalInt innerQuantity;
    @Default
    private OptionalInt supplierQuantity;

}
