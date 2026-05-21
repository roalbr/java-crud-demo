package com.example.crud.service.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy Pattern — Precificação regular (sem desconto).
 */
@Component("regular")
public class RegularPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity) {
        return basePrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String getStrategyName() {
        return "regular";
    }
}
