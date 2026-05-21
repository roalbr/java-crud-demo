package com.example.crud.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento MongoDB para log de auditoria.
 * Registra todas as operações CRUD realizadas no sistema.
 */
@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    private String entityType;

    private String entityId;

    private String action;

    private String performedBy;

    private String details;

    @CreatedDate
    private LocalDateTime timestamp;
}
