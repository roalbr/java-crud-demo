package com.example.crud.controller;

import com.example.crud.model.AuditLog;
import com.example.crud.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para consulta de logs de auditoria (MongoDB).
 * Acesso restrito a ADMIN.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Auditoria", description = "Logs de auditoria (MongoDB)")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "Listar todos os logs de auditoria")
    public ResponseEntity<List<AuditLog>> getAll() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

    @GetMapping("/entity/{type}/{id}")
    @Operation(summary = "Buscar logs por entidade")
    public ResponseEntity<List<AuditLog>> getByEntity(
            @PathVariable String type,
            @PathVariable String id) {
        return ResponseEntity.ok(auditLogRepository.findByEntityTypeAndEntityId(type, id));
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Buscar logs por ação")
    public ResponseEntity<List<AuditLog>> getByAction(@PathVariable String action) {
        return ResponseEntity.ok(auditLogRepository.findByAction(action));
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Buscar logs por usuário")
    public ResponseEntity<List<AuditLog>> getByUser(@PathVariable String username) {
        return ResponseEntity.ok(auditLogRepository.findByPerformedBy(username));
    }
}
