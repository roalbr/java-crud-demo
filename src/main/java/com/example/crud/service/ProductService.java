package com.example.crud.service;

import com.example.crud.dto.ProductRequest;
import com.example.crud.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service Layer Pattern — interface de serviço para operações com Produto.
 */
public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse getById(Long id);

    ProductResponse getBySku(String sku);

    Page<ProductResponse> getAll(Pageable pageable);

    Page<ProductResponse> search(String query, Pageable pageable);

    List<ProductResponse> getByCategory(String category);

    List<ProductResponse> getByPriceRange(BigDecimal min, BigDecimal max);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    BigDecimal calculatePrice(Long productId, int quantity, String pricingStrategy);
}
