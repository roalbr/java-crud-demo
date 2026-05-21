package com.example.crud;

import com.example.crud.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CrudApplicationTests {

    @MockBean
    private AuditLogRepository auditLogRepository;

    @Test
    void contextLoads() {
    }
}
