package com.example.crud.service;

import com.example.crud.factory.PricingStrategyFactory;
import com.example.crud.service.strategy.PricingStrategy;
import com.example.crud.service.strategy.PromotionalPricingStrategy;
import com.example.crud.service.strategy.RegularPricingStrategy;
import com.example.crud.service.strategy.WholesalePricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes do Strategy Pattern — verifica diferentes estratégias de precificação.
 */
class PricingStrategyTest {

    private PricingStrategyFactory factory;

    @BeforeEach
    void setUp() {
        List<PricingStrategy> strategies = List.of(
                new RegularPricingStrategy(),
                new WholesalePricingStrategy(),
                new PromotionalPricingStrategy()
        );
        factory = new PricingStrategyFactory(strategies);
    }

    @Test
    @DisplayName("Regular: preço = base * quantidade")
    void regularPricing() {
        PricingStrategy strategy = factory.getStrategy("regular");
        BigDecimal price = strategy.calculatePrice(new BigDecimal("100.00"), 5);
        assertThat(price).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Wholesale: sem desconto para quantidade < 10")
    void wholesaleNoDiscount() {
        PricingStrategy strategy = factory.getStrategy("wholesale");
        BigDecimal price = strategy.calculatePrice(new BigDecimal("100.00"), 5);
        assertThat(price).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Wholesale: 10% desconto para quantidade >= 10")
    void wholesaleSmallDiscount() {
        PricingStrategy strategy = factory.getStrategy("wholesale");
        BigDecimal price = strategy.calculatePrice(new BigDecimal("100.00"), 10);
        assertThat(price).isEqualByComparingTo(new BigDecimal("900.00"));
    }

    @Test
    @DisplayName("Wholesale: 20% desconto para quantidade >= 50")
    void wholesaleLargeDiscount() {
        PricingStrategy strategy = factory.getStrategy("wholesale");
        BigDecimal price = strategy.calculatePrice(new BigDecimal("100.00"), 50);
        assertThat(price).isEqualByComparingTo(new BigDecimal("4000.00"));
    }

    @Test
    @DisplayName("Promotional: 15% desconto fixo")
    void promotionalPricing() {
        PricingStrategy strategy = factory.getStrategy("promotional");
        BigDecimal price = strategy.calculatePrice(new BigDecimal("100.00"), 10);
        assertThat(price).isEqualByComparingTo(new BigDecimal("850.00"));
    }

    @Test
    @DisplayName("Factory: deve lançar exceção para estratégia inválida")
    void invalidStrategy() {
        assertThatThrownBy(() -> factory.getStrategy("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }
}
