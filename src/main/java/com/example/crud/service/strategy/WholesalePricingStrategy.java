package com.example.crud.service.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy Pattern — Precificação atacado (desconto por volume).
 * Aplica 10% de desconto para quantidades >= 10, 20% para >= 50.
 */
@Component("wholesale")
public class WholesalePricingStrategy implements PricingStrategy {

    private static final BigDecimal SMALL_DISCOUNT = new BigDecimal("0.90");
    private static final BigDecimal LARGE_DISCOUNT = new BigDecimal("0.80");

    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity) {
        BigDecimal total = basePrice.multiply(BigDecimal.valueOf(quantity));

        if (quantity >= 50) {
            return total.multiply(LARGE_DISCOUNT).setScale(2, RoundingMode.HALF_UP);
        } else if (quantity >= 10) {
            return total.multiply(SMALL_DISCOUNT).setScale(2, RoundingMode.HALF_UP);
        }

        return total;
    }

    @Override
    public String getStrategyName() {
        return "wholesale";
    }
}
