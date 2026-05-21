package com.example.crud.controller;

import com.example.crud.dto.ProductRequest;
import com.example.crud.dto.ProductResponse;
import com.example.crud.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Controller usando MockMvc — verifica endpoints REST.
 * Exclui auto-configuração de segurança para isolar testes do controller.
 */
@WebMvcTest(
    controllers = ProductController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.example\\.crud\\.security\\..*|com\\.example\\.crud\\.config\\.Security.*")
)
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse productResponse;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .id(1L)
                .name("Notebook Dell")
                .description("Notebook com i7")
                .price(new BigDecimal("4599.99"))
                .category("Eletrônicos")
                .sku("DELL-001")
                .stockQuantity(50)
                .active(true)
                .build();

        productRequest = ProductRequest.builder()
                .name("Notebook Dell")
                .description("Notebook com i7")
                .price(new BigDecimal("4599.99"))
                .category("Eletrônicos")
                .sku("DELL-001")
                .stockQuantity(50)
                .build();
    }

    @Test
    @DisplayName("GET /api/products/{id} - Deve retornar produto")
    void shouldGetProductById() throws Exception {
        when(productService.getById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Dell"))
                .andExpect(jsonPath("$.sku").value("DELL-001"));
    }

    @Test
    @DisplayName("GET /api/products - Deve retornar lista paginada")
    void shouldGetAllProducts() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(productResponse));
        when(productService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Notebook Dell"));
    }

    @Test
    @DisplayName("POST /api/products - Deve criar produto")
    void shouldCreateProduct() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Notebook Dell"));
    }

    @Test
    @DisplayName("POST /api/products - Deve rejeitar request inválido")
    void shouldRejectInvalidRequest() throws Exception {
        ProductRequest invalid = new ProductRequest();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Deve atualizar produto")
    void shouldUpdateProduct() throws Exception {
        when(productService.update(eq(1L), any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Dell"));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Deve deletar produto")
    void shouldDeleteProduct() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/products/{id}/price - Deve calcular preço")
    void shouldCalculatePrice() throws Exception {
        when(productService.calculatePrice(1L, 5, "regular")).thenReturn(new BigDecimal("22999.95"));

        mockMvc.perform(get("/api/products/1/price")
                        .param("quantity", "5")
                        .param("strategy", "regular"))
                .andExpect(status().isOk())
                .andExpect(content().string("22999.95"));
    }
}
