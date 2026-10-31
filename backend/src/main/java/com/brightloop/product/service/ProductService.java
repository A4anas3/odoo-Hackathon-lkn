package com.brightloop.product.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.product.dto.ProductRequest;
import com.brightloop.product.dto.ProductResponse;
import com.brightloop.product.dto.ProductVariantRequest;
import com.brightloop.product.dto.ProductVariantResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    PageResponse<ProductResponse> listProducts(String search, String productType, Boolean isActive, Pageable pageable);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(UUID id);

    ProductResponse updateProduct(UUID id, ProductRequest request);

    void deleteProduct(UUID id);

    ProductVariantResponse addVariant(UUID productId, ProductVariantRequest request);
}
