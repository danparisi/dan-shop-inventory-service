package com.danshop.inventory.api.v1;

import com.danshop.inventory.persistency.model.ProductInventoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductInventoryMapper {

    ProductInventoryDTO map(ProductInventoryEntity productInventoryEntity);

}
