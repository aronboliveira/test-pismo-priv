# Pismo Challenge API

REST API for customer accounts and financial transactions.

<details open>
<summary><strong>🇺🇸 English (en-US)</strong></summary>

<details open>
<summary><strong>Build, Run and Deploy</strong></summary>

### Prerequisites

- Java 21+
- Docker and Docker Compose
- Maven 3.9+ (optional, wrapper `./mvnw` is included)

### 1) Build and test

```bash
./mvnw clean verify
```

Optional package without tests:

```bash
./mvnw clean package -DskipTests
```

### 2) Run locally (dev mode)

Start only PostgreSQL:

```bash
docker compose up -d db
```

Run the API:

```bash
./mvnw spring-boot:run
```

### 3) Run full stack with Docker (recommended)

This picks port `8080` if available, otherwise `8081`.

```bash
if ss -tln | grep -q ':8080 '; then API_PORT=8081; else API_PORT=8080; fi
echo "$API_PORT" > /tmp/pismo_api_port

docker compose down --remove-orphans
API_PORT="$API_PORT" docker compose up -d --build --wait
docker compose ps
```

### 4) Validate health and API docs

```bash
PORT=$(cat /tmp/pismo_api_port 2>/dev/null || echo 8080)
BASE="http://localhost:$PORT"

curl -i "$BASE/actuator/health"
curl -i "$BASE/swagger-ui.html"
curl -i "$BASE/v3/api-docs"
curl -i "$BASE/v3/api-docs.yaml"
```

Expected:

- `/actuator/health` -> `200`
- `/swagger-ui.html` -> `302` redirect (Swagger UI page renders)
- `/v3/api-docs` -> `200`
- `/v3/api-docs.yaml` -> `200`

### 5) Deploy

For a Docker-enabled host, deploy by cloning and running compose:

```bash
git clone <your-github-repo-url>
cd pismo-challenge-api
API_PORT=8080 docker compose up -d --build --wait
```

Update deployment:

```bash
git pull
API_PORT=8080 docker compose up -d --build
```

Stop/cleanup:

```bash
docker compose down
docker compose down -v --remove-orphans
```

### 6) Publish to GitHub

```bash
git add .
git commit -m "docs: improve bilingual README with run instructions"
git push origin main
```

### 7) Refresh handoff/documentation artifacts

Use this when preparing a final handoff package for reviewers:

```bash
date -Iseconds > .notes/tees/20260420/updated-at.txt
git --no-pager log --oneline -40 > .notes/tees/20260420/git-log.txt
git status --short --branch > .notes/tees/20260420/git-status.txt
find . -maxdepth 4 | sort > .notes/tees/20260420/fs-tree-v2.txt
{ ./mvnw -q -DskipTests validate && echo MAVEN_VALIDATE_OK; } > .notes/tees/20260420/maven-validate.txt 2>&1
```

Then update these docs together:

- `README.md`
- `.notes/THE_CLI.md`
- `.notes/.llms/ctx/challenge-context-handoffs/v9_20260420/session_handoff_v9.md`

</details>

<details>
<summary><strong>Project Overview</strong></summary>

### Goal

Implement the Phase 1 challenge requirements with clear run instructions, automated tests, Docker support, and OpenAPI documentation.

### Main Endpoints

- `POST /accounts` creates an account using a unique document number.
- `GET /accounts/{accountId}` retrieves account data.
- `POST /transactions` creates a transaction linked to an existing account.

### Transaction Rule

Operation type controls sign normalization:

| ID  | Description          | Stored Sign |
| --- | -------------------- | ----------- |
| 1   | PURCHASE             | Negative    |
| 2   | INSTALLMENT PURCHASE | Negative    |
| 3   | WITHDRAWAL           | Negative    |
| 4   | PAYMENT              | Positive    |

The client sends a positive amount, and the API applies the proper sign.

### Stack and Architecture

- Java 21 + Spring Boot 4.0.5 (Web MVC, Data JPA, Validation)
- PostgreSQL 17
- Layered design: controller -> service -> repository
- DTO boundary for requests/responses
- Strategy pattern (`AmountSignStrategy` + resolver) for operation-type sign rule
- Resilience4j: retry with exponential backoff, circuit breaker, rate limiter
- Global exception handling with standardized error payloads
- OpenAPI/Swagger via springdoc
- Dockerized API + DB using docker-compose
- GitHub Actions CI: build, tests, JaCoCo gate, Docker compose smoke test

### Quality Gates

- **JaCoCo coverage gate (enforced at `verify`)**: 100% on instructions, branches, lines, methods, classes.
- **Test count**: 69 tests across unit, slice (`@WebMvcTest`), integration (`@SpringBootTest`), resilience and concurrent stress suites.

### API Documentation URLs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

### Companion documentation

- Runtime CLI playbook: `.notes/THE_CLI.md`
- Session handoff (v9): `.notes/.llms/ctx/challenge-context-handoffs/v9_20260420/session_handoff_v9.md`
- Recorded command artifacts: `.notes/tees/20260420/`

</details>

</details>

<details>
<summary><strong>🇧🇷 Português (pt-BR)</strong></summary>

<details open>
<summary><strong>Build, Run and Deploy</strong></summary>

### Pré-requisitos

- Java 21+
- Docker e Docker Compose
- Maven 3.9+ (opcional, o wrapper `./mvnw` já está incluído)

### 1) Build e testes

```bash
./mvnw clean verify
```

Empacotamento sem testes (opcional):

```bash
./mvnw clean package -DskipTests
```

### 2) Execução local (modo desenvolvimento)

Suba apenas o PostgreSQL:

```bash
docker compose up -d db
```

Execute a API:

```bash
./mvnw spring-boot:run
```

### 3) Execução completa com Docker (recomendado)

Esse fluxo usa a porta `8080` quando livre e `8081` quando `8080` já está ocupada.

```bash
if ss -tln | grep -q ':8080 '; then API_PORT=8081; else API_PORT=8080; fi
echo "$API_PORT" > /tmp/pismo_api_port

docker compose down --remove-orphans
API_PORT="$API_PORT" docker compose up -d --build --wait
docker compose ps
```

### 4) Validação de saúde e documentação

```bash
PORT=$(cat /tmp/pismo_api_port 2>/dev/null || echo 8080)
BASE="http://localhost:$PORT"

curl -i "$BASE/actuator/health"
curl -i "$BASE/swagger-ui.html"
curl -i "$BASE/v3/api-docs"
curl -i "$BASE/v3/api-docs.yaml"
```

Esperado:

- `/actuator/health` -> `200`
- `/swagger-ui.html` -> `302` (redireciona para a interface do Swagger)
- `/v3/api-docs` -> `200`
- `/v3/api-docs.yaml` -> `200`

### 5) Deploy

Em qualquer host com Docker, faça deploy clonando o repositório e subindo o compose:

```bash
git clone <url-do-seu-repo-no-github>
cd pismo-challenge-api
API_PORT=8080 docker compose up -d --build --wait
```

Atualização de deploy:

```bash
git pull
API_PORT=8080 docker compose up -d --build
```

Parar/limpar ambiente:

```bash
docker compose down
docker compose down -v --remove-orphans
```

### 6) Publicação no GitHub

```bash
git add .
git commit -m "docs: melhorar README bilíngue com instruções de execução"
git push origin main
```

### 7) Atualizar artefatos de handoff/documentação

Use este bloco ao preparar o pacote final para revisão:

```bash
date -Iseconds > .notes/tees/20260420/updated-at.txt
git --no-pager log --oneline -40 > .notes/tees/20260420/git-log.txt
git status --short --branch > .notes/tees/20260420/git-status.txt
find . -maxdepth 4 | sort > .notes/tees/20260420/fs-tree-v2.txt
{ ./mvnw -q -DskipTests validate && echo MAVEN_VALIDATE_OK; } > .notes/tees/20260420/maven-validate.txt 2>&1
```

Depois, mantenha estes documentos sincronizados:

- `README.md`
- `.notes/THE_CLI.md`
- `.notes/.llms/ctx/challenge-context-handoffs/v9_20260420/session_handoff_v9.md`

</details>

<details>
<summary><strong>Visão Geral do Projeto</strong></summary>

### Objetivo

Atender aos requisitos da Fase 1 do desafio: API REST funcional, instruções claras de execução, testes automatizados, suporte a Docker e documentação OpenAPI.

### Endpoints Principais

- `POST /accounts` cria uma conta com número de documento único.
- `GET /accounts/{accountId}` consulta uma conta existente.
- `POST /transactions` cria transações vinculadas a uma conta válida.

### Regra de Negócio de Sinal

O tipo da operação define o sinal armazenado:

| ID  | Descrição            | Sinal |
| --- | -------------------- | ----- |
| 1   | PURCHASE             | -     |
| 2   | INSTALLMENT PURCHASE | -     |
| 3   | WITHDRAWAL           | -     |
| 4   | PAYMENT              | +     |

O cliente envia valor positivo e a API normaliza para o sinal correto.

### Stack e Arquitetura

- Java 21 + Spring Boot 4.0.5 (Web MVC, Data JPA, Validation)
- PostgreSQL 17
- Arquitetura em camadas: controller -> service -> repository
- DTOs para entrada/saída da API
- Tratamento global de exceções com payload padronizado
- OpenAPI/Swagger com springdoc
- API e banco containerizados com docker-compose

### URLs da Documentação

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

### Documentos complementares

- Playbook de execução por CLI: `.notes/THE_CLI.md`
- Handoff da sessão (v9): `.notes/.llms/ctx/challenge-context-handoffs/v9_20260420/session_handoff_v9.md`
- Artefatos de comandos gravados: `.notes/tees/20260420/`

</details>

</details>
