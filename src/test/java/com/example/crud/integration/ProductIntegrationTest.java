package com.example.crud.integration;

import com.example.crud.dto.AuthRequest;
import com.example.crud.dto.AuthResponse;
import com.example.crud.dto.ProductRequest;
import com.example.crud.dto.ProductResponse;
import com.example.crud.dto.RegisterRequest;
import com.example.crud.model.AppUser;
import com.example.crud.repository.AppUserRepository;
import com.example.crud.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração com Testcontainers (PostgreSQL + MongoDB reais).
 * Verifica o fluxo completo: autenticação → CRUD → auditoria.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
        registry.add("spring.autoconfigure.exclude", () -> "");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductRepository productRepository;

    private String baseUrl;
    private static String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    @Order(1)
    @DisplayName("Deve registrar novo usuário")
    void shouldRegisterUser() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("test123")
                .email("test@example.com")
                .fullName("Test User")
                .build();

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        authToken = response.getBody().getToken();
    }

    @Test
    @Order(2)
    @DisplayName("Deve fazer login")
    void shouldLogin() {
        if (!userRepository.existsByUsername("testlogin")) {
            AppUser user = AppUser.builder()
                    .username("testlogin")
                    .password(passwordEncoder.encode("login123"))
                    .email("testlogin@example.com")
                    .fullName("Test Login User")
                    .roles(Set.of("USER", "ADMIN"))
                    .active(true)
                    .build();
            userRepository.save(user);
        }

        AuthRequest request = new AuthRequest();
        request.setUsername("testlogin");
        request.setPassword("login123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        authToken = response.getBody().getToken();
    }

    @Test
    @Order(3)
    @DisplayName("Deve criar produto com autenticação")
    void shouldCreateProduct() {
        ProductRequest request = ProductRequest.builder()
                .name("Produto Teste")
                .description("Produto de teste de integração")
                .price(new BigDecimal("99.99"))
                .category("Testes")
                .sku("TEST-INT-001")
                .stockQuantity(10)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                baseUrl + "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Produto Teste");
        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("Deve listar produtos sem autenticação (GET público)")
    void shouldListProductsPublicly() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/products", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(5)
    @DisplayName("Deve rejeitar criação sem autenticação")
    void shouldRejectUnauthenticatedCreation() {
        ProductRequest request = ProductRequest.builder()
                .name("Produto Sem Auth")
                .description("Não deveria ser criado")
                .price(new BigDecimal("10.00"))
                .category("Testes")
                .sku("NOAUTH-001")
                .stockQuantity(1)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/products", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
