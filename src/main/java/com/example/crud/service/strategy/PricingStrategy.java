package com.example.crud.service.strategy;

import java.math.BigDecimal;

/**
 * Strategy Pattern — Interface para diferentes estratégias de precificação.
 */
public interface PricingStrategy {

    BigDecimal calculatePrice(BigDecimal basePrice, int quantity);

    String getStrategyName();
}
