package com.example.crud.event;

import com.example.crud.model.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Observer Pattern — Evento de domínio para operações em Produto.
 * Utiliza o sistema de eventos do Spring (ApplicationEvent).
 */
@Getter
public class ProductEvent extends ApplicationEvent {

    public enum EventType {
        CREATED, UPDATED, DELETED
    }

    private final Product product;
    private final EventType eventType;
    private final String performedBy;

    public ProductEvent(Object source, Product product, EventType eventType, String performedBy) {
        super(source);
        this.product = product;
        this.eventType = eventType;
        this.performedBy = performedBy;
    }
}
