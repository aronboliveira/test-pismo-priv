# Pismo Challenge API

API REST para gerenciamento de contas de clientes e transações financeiras.

## Tecnologias

- **Java 21** (LTS)
- **Spring Boot 4.0.5** (Web MVC, Data JPA, Validation)
- **PostgreSQL 17** (banco de dados relacional)
- **Lombok** (redução de boilerplate)
- **springdoc-openapi** (documentação Swagger/OpenAPI)
- **JUnit 5 + Mockito** (testes)
- **Docker + Docker Compose** (containerização)

## Pré-requisitos

- Java 21+
- Docker e Docker Compose
- Maven 3.9+ (ou use o wrapper `./mvnw` incluído)

## Como executar

### Via Docker Compose (recomendado)

Sobe o banco PostgreSQL e a aplicação em containers:

```bash
docker compose up --build
```

A API estará disponível em `http://localhost:8080`.

> Se a porta 8080 estiver em uso, defina a variável `API_PORT`:
>
> ```bash
> API_PORT=8081 docker compose up --build
> ```

Para parar:

```bash
docker compose down
```

### Execução local (desenvolvimento)

1. Suba apenas o banco de dados:

```bash
docker compose up db
```

2. Em outro terminal, execute a aplicação:

```bash
./mvnw spring-boot:run
```

### Build e testes

```bash
# Compilar e rodar todos os testes
./mvnw clean verify

# Apenas compilar (sem testes)
./mvnw clean package -DskipTests
```

## Endpoints da API

### POST /accounts

Cria uma nova conta.

**Request:**

```json
{
  "document_number": "12345678900"
}
```

**Response (201):**

```json
{
  "account_id": 1,
  "document_number": "12345678900"
}
```

### GET /accounts/{accountId}

Retorna os dados de uma conta existente.

**Response (200):**

```json
{
  "account_id": 1,
  "document_number": "12345678900"
}
```

### POST /transactions

Cria uma nova transação associada a uma conta.

**Request:**

```json
{
  "account_id": 1,
  "operation_type_id": 4,
  "amount": 123.45
}
```

**Response (201):**

```json
{
  "transaction_id": 1,
  "account_id": 1,
  "operation_type_id": 4,
  "amount": 123.45
}
```

### Tipos de operação

| ID  | Descrição            | Sinal do valor |
| --- | -------------------- | -------------- |
| 1   | PURCHASE             | Negativo       |
| 2   | INSTALLMENT PURCHASE | Negativo       |
| 3   | WITHDRAWAL           | Negativo       |
| 4   | PAYMENT              | Positivo       |

> O cliente sempre envia o valor positivo. A API aplica o sinal correto com base no tipo de operação.

## Documentação da API (Swagger)

Com a aplicação rodando, acesse:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **OpenAPI YAML:** http://localhost:8080/v3/api-docs.yaml

## Estrutura do projeto

```
src/main/java/com/pismochallenge/api/
├── config/             # Configurações (seed data, OpenAPI)
├── controller/         # REST controllers
├── dto/
│   ├── request/        # DTOs de entrada
│   └── response/       # DTOs de saída
├── entity/             # Entidades JPA
├── exception/          # Exceções e handler global
├── repository/         # Repositórios Spring Data
└── service/            # Camada de negócio
```

## Decisões de arquitetura

- **Arquitetura em camadas:** controller → service → repository. Sem lógica de negócio nos controllers.
- **DTOs na fronteira:** entidades JPA nunca são expostas diretamente na API.
- **Regra de sinal no service:** o serviço aplica negativo/positivo com base no tipo de operação.
- **Validação em DTOs:** anotações Bean Validation (`@NotNull`, `@Positive`, `@NotBlank`).
- **Exception handler global:** `@RestControllerAdvice` para respostas de erro consistentes.
- **`NUMERIC(19,4)` para valores monetários:** precisão decimal exata (nunca float/double para dinheiro).
- **Seed data via `ApplicationRunner`:** os 4 tipos de operação são inseridos automaticamente na inicialização.
