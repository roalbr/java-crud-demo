package com.example.crud.repository;

import com.example.crud.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de Repository usando @DataJpaTest com H2 embutido.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        productRepository.save(Product.builder()
                .name("Notebook Dell")
                .description("Notebook com i7")
                .price(new BigDecimal("4599.99"))
                .category("Eletrônicos")
                .sku("DELL-001")
                .stockQuantity(50)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .name("Mouse Logitech")
                .description("Mouse sem fio")
                .price(new BigDecimal("299.90"))
                .category("Periféricos")
                .sku("LOG-001")
                .stockQuantity(200)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .name("Teclado Inativo")
                .description("Produto desativado")
                .price(new BigDecimal("199.00"))
                .category("Periféricos")
                .sku("INATIVO-001")
                .stockQuantity(0)
                .active(false)
                .build());
    }

    @Test
    @DisplayName("Deve buscar produto por SKU")
    void shouldFindBySku() {
        Optional<Product> found = productRepository.findBySku("DELL-001");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Notebook Dell");
    }

    @Test
    @DisplayName("Deve retornar vazio para SKU inexistente")
    void shouldReturnEmptyForNonExistentSku() {
        Optional<Product> found = productRepository.findBySku("INEXISTENTE");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar por categoria")
    void shouldFindByCategory() {
        List<Product> products = productRepository.findByCategory("Periféricos");
        assertThat(products).hasSize(2);
    }

    @Test
    @DisplayName("Deve buscar apenas ativos")
    void shouldFindActiveOnly() {
        List<Product> active = productRepository.findByActiveTrue();
        assertThat(active).hasSize(2);
        assertThat(active).allMatch(Product::getActive);
    }

    @Test
    @DisplayName("Deve buscar por faixa de preço")
    void shouldFindByPriceRange() {
        List<Product> products = productRepository.findByPriceRange(
                new BigDecimal("200.00"), new BigDecimal("500.00"));
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Mouse Logitech");
    }

    @Test
    @DisplayName("Deve fazer busca textual paginada")
    void shouldSearchByText() {
        Page<Product> results = productRepository.search("Dell", PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getSku()).isEqualTo("DELL-001");
    }

    @Test
    @DisplayName("Deve verificar existência por SKU")
    void shouldCheckExistsBySku() {
        assertThat(productRepository.existsBySku("DELL-001")).isTrue();
        assertThat(productRepository.existsBySku("INEXISTENTE")).isFalse();
    }
}
