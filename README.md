# Java CRUD Demo — Enterprise Spring Boot Application

Projeto enterprise Java demonstrando um CRUD completo com os principais **design patterns**, tecnologias e práticas do mercado.

## 🏗️ Tecnologias

| Tecnologia | Uso |
|---|---|
| **Java 17** | Linguagem principal |
| **Spring Boot 3.2** | Framework principal |
| **Spring Security + JWT** | Autenticação e autorização |
| **Spring Data JPA** | Persistência relacional |
| **Spring Data MongoDB** | Persistência NoSQL |
| **PostgreSQL** | Banco relacional |
| **MongoDB** | Banco de auditoria |
| **Docker & Docker Compose** | Containerização |
| **JUnit 5 + Mockito** | Testes unitários |
| **Testcontainers** | Testes de integração |
| **Prometheus + Grafana** | Monitoramento |
| **Swagger/OpenAPI** | Documentação da API |
| **MapStruct** | Mapeamento de DTOs |
| **Lombok** | Redução de boilerplate |
| **GitHub Actions** | CI/CD |

## 🎯 Design Patterns Implementados

### 1. Repository Pattern
- `ProductRepository` — Spring Data JPA (PostgreSQL)
- `AuditLogRepository` — Spring Data MongoDB
- Abstração completa do acesso a dados

### 2. Service Layer Pattern
- `ProductService` (interface) → `ProductServiceImpl` (implementação)
- Encapsula toda a lógica de negócio

### 3. DTO Pattern (Data Transfer Object)
- `ProductRequest` / `ProductResponse` — separação entre camada de transporte e entidade
- `ProductMapper` (MapStruct) — conversão automática entre DTOs e entidades

### 4. Builder Pattern
- Todas as entidades e DTOs usam `@Builder` do Lombok
- Criação fluente e legível de objetos

### 5. Strategy Pattern
- `PricingStrategy` (interface)
- `RegularPricingStrategy` — preço padrão
- `WholesalePricingStrategy` — desconto por volume (10% ≥ 10un, 20% ≥ 50un)
- `PromotionalPricingStrategy` — desconto fixo de 15%

### 6. Factory Pattern
- `PricingStrategyFactory` — seleciona a estratégia correta em runtime
- Usa injeção de dependência do Spring para registro automático

### 7. Observer Pattern
- `ProductEvent` — evento de domínio (ApplicationEvent do Spring)
- `AuditLogObserver` — escuta eventos e registra automaticamente no MongoDB
- Desacoplamento total entre operações CRUD e auditoria

### 8. Singleton Pattern
- Beans do Spring (Services, Repositories, etc.) são singletons por padrão

### 9. Template Method Pattern
- `OncePerRequestFilter` (JwtAuthenticationFilter) — define o esqueleto do filtro de autenticação

### 10. Dependency Injection / IoC
- Presente em todo o projeto via `@Autowired` / `@RequiredArgsConstructor`

## 📁 Estrutura do Projeto

```
src/main/java/com/example/crud/
├── CrudApplication.java           # Classe principal
├── config/                        # Configurações (Security, Async, OpenAPI, DataInitializer)
├── controller/                    # REST Controllers (ProductController, AuthController, AuditController)
├── dto/                           # DTOs e MapStruct Mapper
├── event/                         # Eventos de domínio (Observer Pattern)
├── exception/                     # Exceções customizadas + GlobalExceptionHandler
├── factory/                       # Factory Pattern (PricingStrategyFactory)
├── model/                         # Entidades JPA e documentos MongoDB
├── observer/                      # Event Listeners (AuditLogObserver)
├── repository/                    # Repositories (JPA + MongoDB)
├── security/                      # JWT + Spring Security
└── service/                       # Service Layer + Strategy Pattern
    ├── impl/                      # Implementações dos serviços
    └── strategy/                  # Estratégias de precificação
```

## 🚀 Como Executar

### Opção 1: Docker Compose (recomendado)

```bash
docker-compose up -d
```

Isso inicia: App (8080) + PostgreSQL (5432) + MongoDB (27017) + Prometheus (9090) + Grafana (3000)

### Opção 2: Desenvolvimento local (perfil dev com H2)

```bash
# Sem banco externo — usa H2 em memória
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

> **Nota:** No perfil `dev`, o MongoDB não é obrigatório. O H2 substitui o PostgreSQL.

### Opção 3: Com bancos externos

```bash
# Subir apenas os bancos
docker-compose up -d postgres mongodb

# Rodar a aplicação
mvn spring-boot:run
```

## 🔐 Autenticação (JWT)

### Registrar novo usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "novouser",
    "password": "senha123",
    "email": "novo@example.com",
    "fullName": "Novo Usuário"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

> **Usuários padrão:** `admin/admin123` (ADMIN+USER), `user/user123` (USER)

### Usar o token

```bash
TOKEN="<token_retornado>"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/products
```

## 📝 API Endpoints

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|-------------|
| POST | `/api/auth/register` | Registrar usuário | Público |
| POST | `/api/auth/login` | Login (retorna JWT) | Público |
| GET | `/api/products` | Listar produtos (paginado) | Público |
| GET | `/api/products/{id}` | Buscar por ID | Público |
| GET | `/api/products/sku/{sku}` | Buscar por SKU | Público |
| GET | `/api/products/search?query=` | Busca textual | Público |
| GET | `/api/products/category/{cat}` | Buscar por categoria | Público |
| GET | `/api/products/price-range?min=&max=` | Faixa de preço | Público |
| GET | `/api/products/{id}/price?quantity=&strategy=` | Calcular preço | Público |
| POST | `/api/products` | Criar produto | USER/ADMIN |
| PUT | `/api/products/{id}` | Atualizar produto | USER/ADMIN |
| DELETE | `/api/products/{id}` | Desativar (soft delete) | ADMIN |
| GET | `/api/audit` | Listar logs de auditoria | ADMIN |
| GET | `/api/audit/entity/{type}/{id}` | Logs por entidade | ADMIN |

### Swagger UI
Acesse: `http://localhost:8080/swagger-ui.html`

## 🧪 Testes

```bash
# Testes unitários
mvn test

# Testes de integração (requer Docker para Testcontainers)
mvn verify -P integration-test

# Todos os testes
mvn verify
```

### Estrutura de testes:
- **`ProductServiceTest`** — Testes unitários com Mockito (Service layer)
- **`ProductControllerTest`** — Testes de Controller com MockMvc
- **`PricingStrategyTest`** — Testes do Strategy Pattern
- **`ProductRepositoryTest`** — Testes de Repository com H2
- **`ProductIntegrationTest`** — Testes de integração com Testcontainers (PostgreSQL + MongoDB reais)

## 📊 Monitoramento

- **Prometheus:** `http://localhost:9090`
- **Grafana:** `http://localhost:3000` (admin/admin)
- **Actuator Health:** `http://localhost:8080/actuator/health`
- **Actuator Metrics:** `http://localhost:8080/actuator/metrics`
- **Actuator Prometheus:** `http://localhost:8080/actuator/prometheus`

## 🔄 CI/CD

O GitHub Actions pipeline (`.github/workflows/ci.yml`) executa:
1. Build do projeto
2. Testes unitários
3. Empacotamento (JAR)
4. Build da imagem Docker (apenas na branch main)

## 📚 Conceitos Demonstrados

Este projeto demonstra proficiência em:

- **Java 17** com features modernas (records, sealed classes, pattern matching)
- **Spring Boot** e todo o ecossistema Spring
- **REST APIs** com validação, paginação, tratamento de erros
- **Arquitetura de microsserviços** (separação de concerns, eventos, DTOs)
- **Spring Security** com JWT (OAuth2-like)
- **PostgreSQL** (JPA/Hibernate) e **MongoDB** (Spring Data MongoDB)
- **Design Patterns** aplicados na prática
- **Testes automatizados** em múltiplos níveis
- **Docker** e **Docker Compose** para infraestrutura
- **CI/CD** com GitHub Actions
- **Monitoramento** com Prometheus e Grafana
- **Documentação** com Swagger/OpenAPI
