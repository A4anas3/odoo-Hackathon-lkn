package com.brightloop.product.service.impl;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.common.exception.BadRequestException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.product.dto.ProductRequest;
import com.brightloop.product.dto.ProductResponse;
import com.brightloop.product.dto.ProductVariantRequest;
import com.brightloop.product.dto.ProductVariantResponse;
import com.brightloop.product.entity.Product;
import com.brightloop.product.entity.ProductVariant;
import com.brightloop.product.repo.ProductRepository;
import com.brightloop.product.repo.ProductVariantRepository;
import com.brightloop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listProducts(String search, String productType, Boolean isActive, Pageable pageable) {
        Page<Product> page = productRepository.searchProducts(search, productType, isActive, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String code = request.getProductCode();
        if (code == null || code.isBlank()) {
            code = "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        if (productRepository.existsByProductCode(code)) {
            throw new BadRequestException("Product with code " + code + " already exists");
        }

        Product product = Product.builder()
                .productCode(code)
                .name(request.getName())
                .description(request.getDescription())
                .productType(request.getProductType() != null ? request.getProductType() : "SUBSCRIPTION")
                .salePrice(request.getSalePrice())
                .costPrice(request.getCostPrice() != null ? request.getCostPrice() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .notes(request.getNotes())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .variants(new ArrayList<>())
                .build();

        if (request.getVariants() != null) {
            for (ProductVariantRequest vr : request.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .attributeName(vr.getAttributeName())
                        .attributeValue(vr.getAttributeValue())
                        .additionalPrice(vr.getAdditionalPrice())
                        .sku(vr.getSku())
                        .isActive(vr.getIsActive() != null ? vr.getIsActive() : true)
                        .build();
                product.getVariants().add(variant);
            }
        }

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        if (request.getProductType() != null) {
            product.setProductType(request.getProductType());
        }
        product.setSalePrice(request.getSalePrice());
        if (request.getCostPrice() != null) {
            product.setCostPrice(request.getCostPrice());
        }
        if (request.getCurrency() != null) {
            product.setCurrency(request.getCurrency());
        }
        product.setNotes(request.getNotes());
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setDeletedAt(Instant.now());
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public ProductVariantResponse addVariant(UUID productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .attributeName(request.getAttributeName())
                .attributeValue(request.getAttributeValue())
                .additionalPrice(request.getAdditionalPrice())
                .sku(request.getSku())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        ProductVariant saved = productVariantRepository.save(variant);
        return mapVariantToResponse(saved);
    }

    private ProductResponse mapToResponse(Product product) {
        List<ProductVariantResponse> variantResponses = new ArrayList<>();
        if (product.getVariants() != null) {
            for (ProductVariant v : product.getVariants()) {
                variantResponses.add(mapVariantToResponse(v));
            }
        }

        return ProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .name(product.getName())
                .description(product.getDescription())
                .productType(product.getProductType())
                .salePrice(product.getSalePrice())
                .costPrice(product.getCostPrice())
                .currency(product.getCurrency())
                .notes(product.getNotes())
                .isActive(product.getIsActive())
                .variants(variantResponses)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductVariantResponse mapVariantToResponse(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .attributeName(variant.getAttributeName())
                .attributeValue(variant.getAttributeValue())
                .additionalPrice(variant.getAdditionalPrice())
                .sku(variant.getSku())
                .isActive(variant.getIsActive())
                .build();
    }
}
