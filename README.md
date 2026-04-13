# API de Veículos

API REST desenvolvida em Java com Spring Boot para gerenciamento de veículos, com autenticação baseada em JWT e controle de acesso por perfil de usuário.

## Tecnologias utilizadas
- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- JWT
- MapStruct
- JUnit 5
- Mockito
- Docker Compose
- Swagger / OpenAPI


## Funcionalidades implementadas
- Veículos
- Cadastro de veículos
- istagem paginada
- Busca por ID
- Atualização completa
- Atualização parcial
- Remoção lógica (soft delete)
- Filtros dinâmicos com Specification
- Ordenação customizada
- Relatório por marca (GROUP BY)
- Conversão de moeda BRL → USD
- Cache da cotação do dólar com Redis
- Usuários e autenticação
- Criação de usuário
- Login com email e senha
- Geração de token JWT
- Controle de acesso por perfil:
- USER: apenas leitura (GET)
- ADMIN: acesso total (GET, POST, PUT, PATCH, DELETE)


## Arquitetura

O projeto segue uma arquitetura em camadas:

- controller
- service
- repository
- dto
- mapper
- specification
- security
- exception

## Padrões adotados
- DTOs separados por responsabilidade
- Validação de entrada
- Tratamento global de exceções
- Paginação padronizada
- Soft delete para veículos
- Normalização de dados
- Senha com hash usando BCrypt

## Segurança
- Autenticação via JWT Bearer Token
- Senhas armazenadas com hash
- Autorização baseada em roles (USER, ADMIN)
- Header de autenticação
- Authorization: Bearer <token>

Diagrama ER
```mermaid
erDiagram
    VEHICLES {
        uuid id PK
        string license_plate
        string brand
        string model
        number vehicle_year
        string color
        number price
        boolean deleted
        date created_at
        date updated_at
    }

    USERS {
        uuid id PK
        string email
        string password_hash
        string role
    }
```

Diagrama de Sequencia

Criação de veículos

```mermaid
sequenceDiagram
    actor Client
    participant Controller
    participant Service
    participant Repository
    participant DB

    Client->>Controller: POST /vehicles (JWT)
    Controller->>Service: createVehicle(dto)

    Service->>Repository: findByLicensePlate()
    Repository->>DB: SELECT
    DB-->>Repository: result

    alt placa já existe
        Service-->>Controller: error 409
    else OK
        Service->>Repository: save(vehicle)
        Repository->>DB: INSERT
        DB-->>Repository: success
        Service-->>Controller: veículo criado
    end

    Controller-->>Client: response
```

Autenticação
```mermaid
sequenceDiagram
    actor Client
    participant AuthController
    participant AuthService
    participant UserRepository
    participant JWTService
    participant VehicleController

    Client->>AuthController: POST /auth/login (email, password)
    AuthController->>AuthService: authenticate(credentials)
    AuthService->>UserRepository: findByEmail(email)
    UserRepository-->>AuthService: user

    alt credenciais inválidas
        AuthService-->>AuthController: AuthenticationException
        AuthController-->>Client: 401 Unauthorized
    else credenciais válidas
        AuthService->>JWTService: generateToken(user, role)
        JWTService-->>AuthService: JWT
        AuthService-->>AuthController: token
        AuthController-->>Client: 200 OK + token
    end

    Client->>VehicleController: POST /vehicles (Bearer JWT)
    VehicleController->>JWTService: validateToken(token)
    JWTService-->>VehicleController: valid / invalid

    alt token inválido ou ausente
        VehicleController-->>Client: 401 Unauthorized
    else token válido
        VehicleController->>JWTService: extractRole(token)
        JWTService-->>VehicleController: USER / ADMIN

        alt role != ADMIN
            VehicleController-->>Client: 403 Forbidden
        else role = ADMIN
            VehicleController-->>Client: 201 Created
        end
    end
```

## Como executar

### Pré-requisitos
* Java 17+
* Maven
* Docker
* Docker Compose

### Subir PostgreSQL e Redis
`docker-compose up -d`

O PostgreSQL e o Redis estão configurados no docker-compose.
A aplicação Spring Boot não está dockerizada e deve ser executada localmente.

### Rodar a aplicação
`mvn spring-boot:run`

### Configuração da aplicação

Exemplo de application.yml:

```
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/apiveiculos
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: update

  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: SUA_CHAVE_BASE64
  expiration: 86400000
```

### Documentação

Swagger disponível em: [http://localhost:8080/docs](http://localhost:8080/docs)

### Para testar endpoints protegidos:

Crie um usuário
Faça login em /auth/login
Copie o token retornado
Clique em Authorize no Swagger
Informe o token Bearer

### Endpoints principais
*Auth*
`POST /auth/login`
`Users`
`POST /users`

*Vehicles*
`GET /vehicles`
`GET /vehicles/{id}`
`POST /vehicles`
`PUT /vehicles/{id}`
`PATCH /vehicles/{id}`
`DELETE /vehicles/{id}`