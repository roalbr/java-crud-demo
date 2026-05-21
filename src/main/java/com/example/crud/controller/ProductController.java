package com.example.crud.controller;

import com.example.crud.dto.ProductRequest;
import com.example.crud.dto.ProductResponse;
import com.example.crud.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller REST para CRUD de Produtos.
 *
 * Endpoints:
 * - GET    /api/products          - Listar (paginado)
 * - GET    /api/products/{id}     - Buscar por ID
 * - GET    /api/products/sku/{s}  - Buscar por SKU
 * - GET    /api/products/search   - Busca textual
 * - POST   /api/products          - Criar
 * - PUT    /api/products/{id}     - Atualizar
 * - DELETE /api/products/{id}     - Desativar (soft delete)
 * - GET    /api/products/{id}/price - Calcular preço com strategy
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "CRUD de Produtos")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Criar produto")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Buscar produto por SKU")
    public ResponseEntity<ProductResponse> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getBySku(sku));
    }

    @GetMapping
    @Operation(summary = "Listar produtos (paginado)")
    public ResponseEntity<Page<ProductResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getAll(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar produtos por texto")
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.search(query, pageable));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Buscar produtos por categoria")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getByCategory(category));
    }

    @GetMapping("/price-range")
    @Operation(summary = "Buscar produtos por faixa de preço")
    public ResponseEntity<List<ProductResponse>> getByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return ResponseEntity.ok(productService.getByPriceRange(min, max));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar produto (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/price")
    @Operation(summary = "Calcular preço com estratégia (regular, wholesale, promotional)")
    public ResponseEntity<BigDecimal> calculatePrice(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(defaultValue = "regular") String strategy) {
        return ResponseEntity.ok(productService.calculatePrice(id, quantity, strategy));
    }
}
