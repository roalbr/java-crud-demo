package com.example.crud.service.impl;

import com.example.crud.dto.ProductMapper;
import com.example.crud.dto.ProductRequest;
import com.example.crud.dto.ProductResponse;
import com.example.crud.event.ProductEvent;
import com.example.crud.exception.DuplicateResourceException;
import com.example.crud.exception.ResourceNotFoundException;
import com.example.crud.factory.PricingStrategyFactory;
import com.example.crud.model.Product;
import com.example.crud.repository.ProductRepository;
import com.example.crud.service.ProductService;
import com.example.crud.service.strategy.PricingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementação do ProductService.
 *
 * Design Patterns utilizados:
 * - Service Layer: encapsula lógica de negócio
 * - Repository: delega acesso a dados
 * - Observer: publica eventos via ApplicationEventPublisher
 * - Strategy: usa PricingStrategyFactory para cálculo de preço
 * - DTO: converte entre entidades e objetos de transporte
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PricingStrategyFactory pricingStrategyFactory;

    @Override
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Produto com SKU '" + request.getSku() + "' já existe");
        }

        Product product = productMapper.toEntity(request);
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        product.setActive(true);

        Product saved = productRepository.save(product);
        log.info("Produto criado: id={}, sku={}", saved.getId(), saved.getSku());

        eventPublisher.publishEvent(
                new ProductEvent(this, saved, ProductEvent.EventType.CREATED, getCurrentUser()));

        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = findProductOrThrow(id);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "SKU", sku));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String query, Pageable pageable) {
        return productRepository.search(query, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(String category) {
        return productMapper.toResponseList(productRepository.findByCategory(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getByPriceRange(BigDecimal min, BigDecimal max) {
        return productMapper.toResponseList(productRepository.findByPriceRange(min, max));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);

        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU '" + request.getSku() + "' já está em uso");
        }

        productMapper.updateEntity(request, product);
        Product updated = productRepository.save(product);
        log.info("Produto atualizado: id={}", updated.getId());

        eventPublisher.publishEvent(
                new ProductEvent(this, updated, ProductEvent.EventType.UPDATED, getCurrentUser()));

        return productMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Product product = findProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Produto desativado (soft delete): id={}", id);

        eventPublisher.publishEvent(
                new ProductEvent(this, product, ProductEvent.EventType.DELETED, getCurrentUser()));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculatePrice(Long productId, int quantity, String strategyName) {
        Product product = findProductOrThrow(productId);
        PricingStrategy strategy = pricingStrategyFactory.getStrategy(strategyName);
        return strategy.calculatePrice(product.getPrice(), quantity);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
