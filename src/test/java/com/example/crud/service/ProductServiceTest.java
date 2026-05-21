package com.example.crud.service;

import com.example.crud.dto.ProductMapper;
import com.example.crud.dto.ProductRequest;
import com.example.crud.dto.ProductResponse;
import com.example.crud.event.ProductEvent;
import com.example.crud.exception.DuplicateResourceException;
import com.example.crud.exception.ResourceNotFoundException;
import com.example.crud.factory.PricingStrategyFactory;
import com.example.crud.model.Product;
import com.example.crud.repository.ProductRepository;
import com.example.crud.service.impl.ProductServiceImpl;
import com.example.crud.service.strategy.RegularPricingStrategy;
import com.example.crud.service.strategy.WholesalePricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProductService usando JUnit 5 e Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PricingStrategyFactory pricingStrategyFactory;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
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
    }

    @Nested
    @DisplayName("Criar Produto")
    class CreateProduct {

        @Test
        @DisplayName("Deve criar produto com sucesso")
        void shouldCreateProductSuccessfully() {
            when(productRepository.existsBySku("DELL-001")).thenReturn(false);
            when(productMapper.toEntity(productRequest)).thenReturn(product);
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(productMapper.toResponse(product)).thenReturn(productResponse);

            ProductResponse result = productService.create(productRequest);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Notebook Dell");
            assertThat(result.getSku()).isEqualTo("DELL-001");

            verify(productRepository).save(any(Product.class));
            verify(eventPublisher).publishEvent(any(ProductEvent.class));
        }

        @Test
        @DisplayName("Deve lançar exceção para SKU duplicado")
        void shouldThrowExceptionForDuplicateSku() {
            when(productRepository.existsBySku("DELL-001")).thenReturn(true);

            assertThatThrownBy(() -> productService.create(productRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("DELL-001");

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Buscar Produto")
    class GetProduct {

        @Test
        @DisplayName("Deve buscar produto por ID")
        void shouldGetProductById() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productMapper.toResponse(product)).thenReturn(productResponse);

            ProductResponse result = productService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção para ID inexistente")
        void shouldThrowExceptionForNonExistentId() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Deve buscar produto por SKU")
        void shouldGetProductBySku() {
            when(productRepository.findBySku("DELL-001")).thenReturn(Optional.of(product));
            when(productMapper.toResponse(product)).thenReturn(productResponse);

            ProductResponse result = productService.getBySku("DELL-001");

            assertThat(result.getSku()).isEqualTo("DELL-001");
        }

        @Test
        @DisplayName("Deve listar produtos paginados")
        void shouldGetAllProductsPaginated() {
            PageRequest pageable = PageRequest.of(0, 20);
            Page<Product> page = new PageImpl<>(List.of(product));
            when(productRepository.findByActiveTrue(pageable)).thenReturn(page);
            when(productMapper.toResponse(product)).thenReturn(productResponse);

            Page<ProductResponse> result = productService.getAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Notebook Dell");
        }
    }

    @Nested
    @DisplayName("Atualizar Produto")
    class UpdateProduct {

        @Test
        @DisplayName("Deve atualizar produto com sucesso")
        void shouldUpdateProductSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(productMapper.toResponse(product)).thenReturn(productResponse);

            ProductResponse result = productService.update(1L, productRequest);

            assertThat(result).isNotNull();
            verify(productMapper).updateEntity(eq(productRequest), any(Product.class));
            verify(eventPublisher).publishEvent(any(ProductEvent.class));
        }
    }

    @Nested
    @DisplayName("Deletar Produto")
    class DeleteProduct {

        @Test
        @DisplayName("Deve realizar soft delete do produto")
        void shouldSoftDeleteProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            productService.delete(1L);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());
            assertThat(captor.getValue().getActive()).isFalse();
            verify(eventPublisher).publishEvent(any(ProductEvent.class));
        }
    }

    @Nested
    @DisplayName("Calcular Preço")
    class CalculatePrice {

        @Test
        @DisplayName("Deve calcular preço com estratégia regular")
        void shouldCalculateRegularPrice() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(pricingStrategyFactory.getStrategy("regular")).thenReturn(new RegularPricingStrategy());

            BigDecimal price = productService.calculatePrice(1L, 5, "regular");

            assertThat(price).isEqualByComparingTo(new BigDecimal("22999.95"));
        }

        @Test
        @DisplayName("Deve calcular preço com estratégia wholesale")
        void shouldCalculateWholesalePrice() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(pricingStrategyFactory.getStrategy("wholesale")).thenReturn(new WholesalePricingStrategy());

            BigDecimal price = productService.calculatePrice(1L, 10, "wholesale");

            // 4599.99 * 10 * 0.90 = 41399.91
            assertThat(price).isEqualByComparingTo(new BigDecimal("41399.91"));
        }
    }
}
