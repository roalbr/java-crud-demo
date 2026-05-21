package com.example.crud.factory;

import com.example.crud.service.strategy.PricingStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory Pattern — Fábrica para selecionar a estratégia de precificação.
 * Utiliza injeção de dependência do Spring para registrar todas as strategies.
 */
@Component
public class PricingStrategyFactory {

    private final Map<String, PricingStrategy> strategies;

    public PricingStrategyFactory(List<PricingStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PricingStrategy::getStrategyName, Function.identity()));
    }

    public PricingStrategy getStrategy(String strategyName) {
        PricingStrategy strategy = strategies.get(strategyName.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Estratégia de precificação não encontrada: " + strategyName +
                    ". Disponíveis: " + strategies.keySet());
        }
        return strategy;
    }
}
