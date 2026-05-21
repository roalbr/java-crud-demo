package com.example.crud.observer;

import com.example.crud.event.ProductEvent;
import com.example.crud.model.AuditLog;
import com.example.crud.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Observer Pattern — Escuta eventos de Produto e registra no MongoDB.
 * Usa @EventListener do Spring para desacoplamento total.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogObserver {

    private final AuditLogRepository auditLogRepository;

    @Async
    @EventListener
    public void onProductEvent(ProductEvent event) {
        log.info("Audit: {} - Product {} by {}",
                event.getEventType(), event.getProduct().getSku(), event.getPerformedBy());

        AuditLog auditLog = AuditLog.builder()
                .entityType("Product")
                .entityId(String.valueOf(event.getProduct().getId()))
                .action(event.getEventType().name())
                .performedBy(event.getPerformedBy())
                .details(buildDetails(event))
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }

    private String buildDetails(ProductEvent event) {
        return String.format("Product [%s] %s - SKU: %s, Name: %s",
                event.getProduct().getId(),
                event.getEventType().name().toLowerCase(),
                event.getProduct().getSku(),
                event.getProduct().getName());
    }
}
