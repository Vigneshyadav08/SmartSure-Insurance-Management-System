# SmartSure Insurance Management System

## Complete Viva Guide

This document explains the SmartSure Insurance Management System in the same order in which the application runs in real life:

1. Eureka Server
2. Config Server
3. Auth Service
4. Policy Service
5. Claim Service
6. Admin Service
7. API Gateway
8. Supporting infrastructure such as RabbitMQ, MySQL, Zipkin, Docker, Swagger, testing, and monitoring

The goal of this guide is not just to say what each service does, but also:

- why it exists
- which classes implement the logic
- how services interact
- what each important configuration means
- what happens step by step for a real request

---

## 1. Project Overview

SmartSure is a microservices-based insurance management backend built with Spring Boot and Spring Cloud. It separates the application into focused services instead of placing everything inside one monolithic application.

The system supports two main users:

- Customer
- Admin

The main business capabilities are:

- customer registration and login
- JWT-based authentication
- viewing and purchasing policies
- paying for a purchased policy
- submitting claims with optional file upload
- validating that claims are allowed only for active purchased policies
- admin review and approval or rejection of claims
- admin reporting and policy audit

The major technologies used are:

- Spring Boot
- Spring Cloud Eureka
- Spring Cloud Config
- Spring Cloud Gateway
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- OpenFeign
- RabbitMQ
- Resilience4j
- Springdoc OpenAPI / Swagger
- Docker / Docker Compose
- Zipkin
- JUnit and Mockito

---

## 2. Why This Project Uses Microservices

This project intentionally uses microservices because the business domains are naturally separate.

The responsibilities are divided like this:

- authentication belongs to Auth Service
- product and purchase lifecycle belongs to Policy Service
- claim workflow belongs to Claim Service
- administrative actions belong to Admin Service
- routing and cross-cutting security belong to API Gateway
- service discovery belongs to Eureka
- centralized configuration belongs to Config Server

### Why this is useful

If everything were in a single monolith:

- every module would be tightly coupled
- one failure could affect the whole system
- scaling one feature would require scaling everything
- service-to-service boundaries would be unclear

With this design:

- each service has one main business responsibility
- every service can be deployed independently
- every service keeps its own database tables and code
- service discovery and routing work dynamically through Eureka and Gateway

---

## 3. High-Level Architecture

The request flow is:

Client -> API Gateway -> Target Microservice

The service support flow is:

- every microservice gets config from Config Server
- every microservice registers itself with Eureka
- gateway routes requests using Eureka service names
- some services talk to each other using Feign clients
- some events are sent asynchronously through RabbitMQ

### Main runtime ports

- Eureka Server: 8761
- Config Server: 8888
- API Gateway: 8080
- Auth Service: 8081
- Policy Service: 8082
- Claim Service: 8083
- Admin Service: 8084
- Zipkin: 9411
- RabbitMQ management: 15672

---

## 4. Eureka Server

### What Eureka is

Eureka is the service registry of the project.

It keeps track of which service is running and where it is running. In a microservices system, hardcoding service IP addresses is not practical, especially in Docker, because container addresses can change.

### Why we use Eureka

Without Eureka:

- API Gateway would need fixed URLs for all services
- Feign clients would need hardcoded addresses
- Docker restarts and scale-out would break routing

With Eureka:

- services register themselves with names like `auth-service`, `policy-service`, `claim-service`, `admin-service`
- API Gateway routes to `lb://service-name`
- Feign clients call by logical service name instead of IP

### Main configuration

In `eureka-server/src/main/resources/application.yml`:

```yaml
server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
```

### Explanation

- `registerWithEureka: false`
  The Eureka server should not register itself like a normal client.

- `fetchRegistry: false`
  The registry server does not need to fetch registry data from another Eureka server in this project.

- `port: 8761`
  This is the dashboard and registry endpoint port.

### Main class

In `EurekaServerApplication.java`, the annotation `@EnableEurekaServer` turns the application into a registry server.

### Viva explanation

If asked "what does Eureka do?", you can say:

Eureka acts like the phonebook of the microservices system. Each service registers itself there, and the API Gateway and Feign clients ask Eureka where a service is currently running.

---

## 5. Config Server

### What Config Server is

Config Server is the centralized configuration provider for the microservices.

Instead of duplicating datasource URLs, RabbitMQ settings, Zipkin settings, resilience settings, and gateway routes inside each service, this project keeps shared service-specific configuration inside the `config-repo` folder.

### Why we use Config Server

Without Config Server:

- each service would have large duplicated `application.yml` files
- configuration changes would need to be updated in many places
- Docker and local environments would be harder to maintain

With Config Server:

- each service keeps only bootstrap-level values locally
- service-specific runtime configuration comes from `config-repo`
- local and Docker values can be controlled with environment variable placeholders

### Main configuration

In `config-server/src/main/resources/application.yml`:

```yaml
server:
  port: 8888

spring:
  profiles:
    active: native
  application:
    name: config-server
  cloud:
    config:
      server:
        native:
          search-locations: file:../config-repo
```

### Explanation

- `profiles.active: native`
  The config server reads configuration from local files instead of Git.

- `search-locations: file:../config-repo`
  The config files are stored in the `config-repo` directory beside the project modules.

### Main class

In `ConfigServerApplication.java`:

- `@EnableConfigServer` enables Spring Cloud Config Server
- `@EnableDiscoveryClient` allows Config Server itself to register with Eureka

### How client services use it

Each microservice has a small local `application.yml` like:

```yaml
spring:
  application:
    name: auth-service
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
```

This does two important things:

- identifies the service name for config lookup
- tells Spring Boot to fetch external configuration from Config Server during startup

### Viva explanation

If asked "why do we need Config Server if we already have application.yml?", you can say:

Local `application.yml` is only the minimal startup configuration. The full environment-specific business configuration is centralized in Config Server so all services can stay consistent and easier to maintain.

---

## 6. Auth Service

### Main purpose

Auth Service handles identity and authentication-related responsibilities.

It is responsible for:

- registration
- login
- password hashing
- JWT generation
- role lookup

### Why we separate Auth Service

Authentication is a cross-cutting concern. If login logic is mixed into every service:

- password logic gets duplicated
- security becomes inconsistent
- user management becomes harder to control

Keeping it in one service makes the design cleaner and easier to secure.

### Startup configuration

In `auth-service/src/main/resources/application.yml`:

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
```

This means:

- Auth Service runs on port 8081
- it identifies itself as `auth-service`
- it loads additional configuration from Config Server
- it registers with Eureka using the configured Eureka client settings

### Main class

In `AuthServiceApplication.java`:

- `@SpringBootApplication` starts the Spring Boot app
- a `CommandLineRunner` creates a default admin user if it does not exist

That startup runner is useful because:

- the system always has at least one admin account
- manual first-time admin insertion is avoided

### Security configuration

In `SecurityConfig.java`:

Important pieces:

- `@EnableWebSecurity`
- `@EnableMethodSecurity`
- `PasswordEncoder` bean using `BCryptPasswordEncoder`
- `AuthenticationManager` bean
- `SecurityFilterChain`

#### Why BCrypt is used

Passwords must never be stored in plain text.

`BCryptPasswordEncoder`:

- hashes passwords before storing them
- protects against plain-text leakage
- is a standard secure password hashing approach in Spring Security

#### Why AuthenticationManager is used

When a user logs in, Spring Security must verify:

- username exists
- password matches hashed password

`AuthenticationManager` performs that verification.

### JWT Utility

In `JwtUtil.java`:

Important methods:

- `generateToken(String username, String role)`
- `validateToken(String token)`
- `extractUsername(String token)`
- `extractRole(String token)`

#### What JWT contains here

The token stores:

- subject = username
- claim `role` = user role
- issued time
- expiration time

#### Why JWT is used

JWT gives stateless authentication.

That means:

- the server does not need to store login session state in memory
- Gateway can validate the token on every request
- downstream routing can use the token role for authorization checks

### Service layer

In `AuthService.java`:

#### Important business methods

- `saveUser(...)`
- `saveAdminUser(...)`
- `generateToken(...)`
- `getUserRole(...)`

#### Important design point

In `saveUser(...)`, every public registration is forced to role `CUSTOMER`.

Why that is important:

- users should not self-register as admin
- role escalation must be controlled

### Controller

In `AuthController.java`:

Endpoints:

- `POST /auth/register`
- `POST /auth/admin/create-user`
- `POST /auth/login`

### Request flow for login

1. User sends username and password to `/auth/login`
2. `AuthenticationManager` verifies credentials
3. If valid, `AuthService.generateToken()` creates a JWT
4. Role is fetched and returned along with token
5. Client uses this token in later requests through Gateway

### Viva explanation

If asked "why do we still have Spring Security inside auth-service if Gateway also checks JWT?", say:

Auth Service needs Spring Security for credential authentication and protected admin-only endpoints inside the auth module. Gateway handles token validation for cross-service access, but login itself must still verify username and password in Auth Service.

---

## 7. Policy Service

### Main purpose

Policy Service manages:

- policy catalog
- purchased policy records
- purchase lifecycle transitions
- purchased policy status lookup for claims and admin
- policy purchase event publishing through RabbitMQ

### Why we separate Policy Service

Insurance product management is different from claim handling.

Policy Service focuses on:

- what policies exist
- who purchased which policy
- whether a purchased policy is CREATED, ACTIVE, CANCELLED, or EXPIRY

### Main controller

In `PolicyController.java`

Important endpoints:

- `POST /policies`
- `GET /policies`
- `GET /policies/{id}`
- `POST /policies/purchase`
- `POST /policies/purchase/{id}/pay`
- `POST /policies/purchase/{id}/cancel`
- `POST /policies/purchase/{id}/expire`
- `GET /policies/purchase/status`
- `GET /policies/purchase/user/{username}`
- `DELETE /policies/{id}`

### Actual access rule for policy APIs

The final gateway behavior is:

- `GET /policies` is public so anyone can browse available plans
- `GET /policies/{id}` requires login
- `GET /policies/{id}` is accessible to both `CUSTOMER` and `ADMIN`
- policy creation, deletion, and purchase-related write operations remain protected

Why this rule makes sense:

- plan listing is part of discovery, so public access is reasonable
- detailed policy operations and customer-specific actions still require authentication

### Business service

In `PolicyService.java`

Important methods:

- `createPolicy`
- `getAllPolicies`
- `getPolicy`
- `deletePolicy`
- `purchasePolicy`
- `payPolicy`
- `cancelPolicy`
- `expirePolicy`
- `getPurchasedPolicyStatus`
- `getPurchasedPoliciesByUser`

### Core business logic

#### Policy catalog vs purchased policy

This service manages two different concepts:

- `Policy`
  A product definition such as plan name, premium, deductible, etc.

- `PurchasedPolicy`
  A customer-specific purchase record

This is important in viva because many people confuse "policy product" and "policy purchase instance".

### Purchased policy lifecycle

Implemented through string statuses in `PurchasedPolicy.java`:

- CREATED
- ACTIVE
- CANCELLED
- EXPIRY

#### Flow

1. Customer requests purchase
2. `purchasePolicy(...)` creates a purchased policy with status `CREATED`
3. Payment endpoint changes status to `ACTIVE`
4. Cancel endpoint changes status to `CANCELLED`
5. Expire endpoint changes status to `EXPIRY`

### Why deductible is stored in purchased policy

In `purchasePolicy(...)`, deductible amount is copied from the policy product into the purchased record.

Why that is useful:

- claim processing can use the actual deductible associated with the purchase
- claim logic does not need to depend on current product edits later

### Caching

This service uses Spring Cache annotations such as:

- `@Cacheable`
- `@CacheEvict`

Why caching is used:

- reading policy lists and policy details is common
- repeated DB reads can be reduced
- writes evict cache so stale data is not served

### RabbitMQ publishing

In `purchasePolicy(...)`:

```java
rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.POLICY_ROUTING_KEY, message);
```

Why this is used:

- purchase confirmation can trigger side actions asynchronously
- admin-side listeners can log or observe business events
- the purchase API stays responsive without waiting for those side actions

### RabbitMQ configuration

In `RabbitMQConfig.java`:

- exchange: `smartsure.exchange`
- queue: `policy.purchase.queue`
- routing key: `policy.purchased`

This creates a standard producer-side messaging setup.

### Viva explanation

If asked "what is the difference between buying a policy and activating a policy?", say:

Buying creates a purchase record with status `CREATED`. Payment changes that purchased record to `ACTIVE`. Claim submission is allowed only when the purchased policy status is `ACTIVE`.

---

## 8. Claim Service

### Main purpose

Claim Service handles:

- claim submission
- file upload
- payout calculation
- claim status management
- internal admin claim actions
- claim event publishing through RabbitMQ

### Why Claim Service is separate

Claim processing is the most workflow-heavy part of the system.

It includes:

- validating another service's data
- storing customer documents
- moving through multiple claim statuses
- payout calculation
- admin review transitions

That makes it a strong independent domain.

### Feign integration with Policy Service

In `PolicyClient.java`:

```java
@FeignClient(name = "policy-service")
```

This tells Spring Cloud OpenFeign to call the service registered in Eureka as `policy-service`.

Method used:

- `getPurchasedPolicyStatus(username, policyId)`

### Why Feign is used here

Claim submission depends on policy state.

The claim service must ask:

"Is this user's purchased policy ACTIVE?"

That is policy domain data, so Claim Service should not query Policy Service's database directly. It should call Policy Service through an API. Feign gives a clean declarative client for that.

### Main service class

In `ClaimService.java`

Important methods:

- `submitClaim`
- `getClaims`
- `getClaimById`
- `updateClaimStatus`
- `downloadDocument`
- `closeClaim`
- `deleteClaim`

### Core claim submission logic

In `submitClaim(...)`, the steps are:

1. Call Policy Service through Feign
2. Read purchased policy status
3. Reject claim if status is not `ACTIVE`
4. Map request DTO into entity
5. copy deductible amount from purchased policy status
6. calculate payout
7. set claim status to `PENDING`
8. save claim
9. optionally store uploaded file
10. publish RabbitMQ event

### Payout calculation

In code:

```java
double payout = Math.max(0, req.getClaimAmount() - policyStatus.getDeductibleAmount());
```

Why this is done:

- deductible reduces the amount paid by the insurer
- payout should not become negative

### File handling

Uploaded documents are saved in the `uploads/` directory.

Why this matters in viva:

- claim metadata is stored in the database
- actual file bytes are stored on disk
- the path is saved in the entity

### Claim statuses

As implemented in service logic, the claim status can move through:

- PENDING
- UNDER_REVIEW
- APPROVED
- REJECTED
- CLOSED

#### Important behavior

When admin downloads a document through the admin-internal endpoint, the service moves the claim to `UNDER_REVIEW` unless it is already approved or rejected.

This is a nice viva point because it shows business-state transition driven by an action, not just by direct status update.

### Controllers

#### Customer-facing controller

In `ClaimController.java`

Main endpoints:

- `POST /claims`
- `GET /claims/user/{username}`
- `GET /claims/{id}`
- `GET /claims/{id}/download`

#### Admin-internal controller

In `AdminClaimController.java`

Main endpoints:

- `PUT /admin-internal/claims/{id}/status`
- `PUT /admin-internal/claims/{id}/close`
- `GET /admin-internal/claims/{id}/download`
- `DELETE /admin-internal/claims/{id}`

Why internal endpoints exist:

- admin actions should call a dedicated internal API
- this keeps customer claim APIs separate from admin workflow APIs

### RabbitMQ config

In `RabbitMQConfig.java`:

- exchange: `smartsure.exchange`
- queue: `claim.submitted.queue`
- routing key: `claim.submitted`

### Viva explanation

If asked "why does Claim Service call Policy Service first?", say:

Because claim eligibility depends on purchased policy status. Only active purchased policies can create valid claims, so Claim Service validates the policy through Policy Service before saving the claim.

---

## 9. Admin Service

### Main purpose

Admin Service handles administrative operations that span multiple domains.

It is responsible for:

- approving claims
- rejecting claims
- downloading customer claim documents for review
- closing reviewed claims
- viewing purchased policies for a user
- expiring policies
- generating reports
- listening to RabbitMQ business events

### Why Admin Service is separate

Admin actions are fundamentally orchestration-oriented.

This service does not own all the business data itself. Instead, it coordinates actions across other services:

- Claim Service for claim status updates
- Policy Service for user purchase history and expiry actions

### Feign clients

#### Policy client

In `PolicyFeignClient.java`

This client calls `policy-service`.

#### Claim client

In `ClaimFeignClient.java`

This client calls `claim-service`.

### Why Feign is valuable here

Admin Service is mostly an orchestrator.

Instead of manual `RestTemplate` code, Feign allows the service to call downstream APIs like normal Java methods. That makes the orchestration layer cleaner and easier to maintain.

### Main service class

In `AdminService.java`

Important methods:

- `getPurchasedPolicies`
- `expirePolicy`
- `approveClaim`
- `rejectClaim`
- `downloadClaimDocument`
- `closeClaim`
- `generateReport`

### Resilience4j in Admin Service

This class uses:

- `@CircuitBreaker`
- `@Retry`
- `@Bulkhead`

Why this is important:

- admin operations depend on remote services
- if a downstream service is failing, admin-service should not hang or collapse
- fallback methods return graceful messages instead of raw system failure

#### Explanation of each annotation

- `@CircuitBreaker`
  Stops repeated calls to a failing service after failure threshold is reached.

- `@Retry`
  Retries temporary failures automatically before giving up.

- `@Bulkhead`
  Limits concurrent calls so one failing dependency does not consume all resources.

### Report generation

`generateReport()` creates and stores a `Report` entity with generation timestamp.

Why it matters:

- shows admin-side persistence separate from orchestration
- demonstrates admin database ownership

### Messaging consumer

In `NotificationListener.java`

This class uses `@RabbitListener` on:

- `policy.purchase.queue`
- `claim.submitted.queue`

Why this is useful:

- admin service can observe important business events asynchronously
- producer services do not wait for admin-side processing

### Controller

In `AdminController.java`

Important endpoints:

- `PUT /admin/claims/{id}/approve`
- `PUT /admin/claims/{id}/reject`
- `POST /admin/reports/generate`
- `GET /admin/claims/{id}/download`
- `PUT /admin/claims/{id}/close`
- `GET /admin/users/{username}/policies`
- `PUT /admin/policies/{id}/expire`

### Viva explanation

If asked "why not let admin directly update claim or policy tables?", say:

In microservices architecture, each service owns its own data. Admin Service should not directly access another service's database. It must call the owning service through API contracts.

---

## 10. API Gateway

### Main purpose

API Gateway is the single public entry point to the system.

It is responsible for:

- receiving client requests
- validating JWT for protected routes
- enforcing role-based access logic
- rate limiting
- routing requests to services through Eureka
- applying resilience fallback at the gateway layer
- exposing aggregated Swagger documentation

### Why we need API Gateway

Without Gateway:

- the client would need to know all service URLs
- JWT validation logic would be duplicated everywhere
- CORS, rate limiting, and cross-cutting request checks would be inconsistent

With Gateway:

- client only talks to one entry point
- downstream services stay focused on business logic

### Local startup configuration

In `api-gateway/src/main/resources/application.yml`:

- application name is `api-gateway`
- config is loaded from Config Server
- Eureka is used for service discovery

### Full route configuration

The main gateway configuration is in `config-repo/api-gateway.yml`.

Important route examples:

```yaml
- id: auth-service
  uri: lb://auth-service
  predicates:
    - Path=/auth,/auth/**
```

`lb://` means load-balanced routing using service instances from Eureka.

### Gateway routes

Main functional routes:

- `/auth` -> auth-service
- `/policies` -> policy-service
- `/claims` -> claim-service
- `/admin` -> admin-service

Swagger routes:

- `/auth-service/v3/api-docs`
- `/policy-service/v3/api-docs`
- `/claim-service/v3/api-docs`
- `/admin-service/v3/api-docs`

### Important gateway behavior for policies

The gateway now treats policy read APIs differently from policy write APIs:

- `GET /policies` is treated as a public endpoint
- other policy endpoints are still evaluated by the authorization rules
- authenticated `CUSTOMER` and `ADMIN` users can access policy read endpoints like `GET /policies/{id}`
- write endpoints such as purchase and admin actions remain protected

### Authentication filter

In `AuthenticationFilter.java`

This is one of the most important classes for viva.

#### What it does

1. Reads request path and HTTP method
2. Allows public endpoints like login, registration, Swagger, and docs
3. Reads `Authorization` header
4. Validates token using gateway-side `JwtUtil`
5. Extracts role from token
6. Checks role-based access rules
7. Either forwards the request or rejects it

#### Why this design is good

- authentication and authorization checks happen before request reaches the business service
- protected services do not need to duplicate the same gateway-level check
- invalid requests are rejected early

### Access control rules

As implemented:

- `GET /policies` is public
- `ADMIN` can access all routes
- `CUSTOMER` cannot access `/admin/**`
- authenticated `CUSTOMER` users can read policy and claim data allowed by the gateway rule set
- customer `POST` access is limited to purchase or claim submission paths

### Why `GET /policies` is public

This is a business-driven rule.

Users need to browse available insurance plans before deciding whether to register or log in. So the gateway allows the collection endpoint `/policies` without authentication, but keeps the rest of the protected workflow behind JWT validation.

### Gateway-side JWT utility

In `api-gateway JwtUtil.java`

The gateway uses the same secret to:

- validate token signature
- extract username
- extract role

Why we do this in gateway:

- gateway must trust but verify client tokens
- services should receive already screened traffic whenever possible

### Rate limiting

In `RateLimitFilter.java`

The filter gets the default Resilience4j rate limiter from the registry and rejects requests with `429 TOO_MANY_REQUESTS` when the limit is exceeded.

Why it is useful:

- protects the system from rapid request bursts
- prevents a client from overwhelming backend services

### Fallback controller

In `FallbackController.java`

Fallback endpoints are:

- `/fallback/auth`
- `/fallback/policy`
- `/fallback/claim`
- `/fallback/admin`

Why this exists:

- when a circuit breaker opens or downstream service is unavailable, gateway can return a friendly response
- users see controlled failure instead of low-level error noise

### Swagger aggregation

Gateway exposes one Swagger UI and loads docs from all services through relative doc URLs.

Why this is useful:

- one interface for testing all services
- easier demonstration during viva
- easier manual API testing

### Viva explanation

If asked "what is the difference between Eureka and Gateway?", say:

Eureka is the directory of available services. Gateway is the entry point that receives client traffic, validates security, and uses Eureka to decide where to route each request.

---

## 11. MySQL and Persistence Design

### Why database per service pattern matters

Each microservice owns its own persistent data.

This project uses separate schemas such as:

- `smartsure_auth`
- `smartsure_policy`
- `smartsure_claim`
- `smartsure_admin`

### Why this is important

- loose coupling between services
- one service cannot silently break another through schema changes
- business boundaries stay clear

### Standard layered pattern used in services

The project follows this common structure:

- Controller
- Service
- Repository
- Entity
- DTO

#### Meaning

- Controller handles HTTP requests
- Service contains business logic
- Repository handles database access using Spring Data JPA
- Entity maps Java classes to DB tables
- DTO controls request and response shapes

### Why JPA is used

Spring Data JPA reduces boilerplate:

- standard CRUD methods come from repository interfaces
- developers focus more on business logic than manual SQL for simple operations

---

## 12. RabbitMQ

### What RabbitMQ is doing in this project

RabbitMQ handles asynchronous events.

Events currently include:

- policy purchase notifications
- claim submitted notifications

### Why asynchronous messaging is useful

Not every action must be synchronous.

For example:

- customer should not wait for admin-side observation logic
- services can publish events and continue
- listeners can process side-effects independently

### Producer services

- Policy Service publishes purchase events
- Claim Service publishes claim submission events

### Consumer service

- Admin Service listens to both queues

### Viva explanation

If asked "why not call Admin Service directly after purchase?", say:

Because admin-side observation is not part of the critical response path. RabbitMQ decouples the producer and consumer and keeps the customer API faster and more reliable.

---

## 13. OpenFeign and Inter-Service Communication

### Where Feign is used

- Claim Service -> Policy Service
- Admin Service -> Claim Service
- Admin Service -> Policy Service

### Why OpenFeign is used instead of hardcoded HTTP client code

Feign allows us to declare service contracts as interfaces.

Benefits:

- less boilerplate
- cleaner code
- service name discovery through Eureka
- easier maintenance and readability

### How it works internally

When Spring sees:

```java
@FeignClient(name = "policy-service")
```

it creates an HTTP client behind the scenes that:

- asks Eureka for `policy-service`
- resolves the instance
- makes the HTTP call

---

## 14. Resilience4j

### Where resilience appears

- API Gateway route filters use circuit breaker
- Admin Service uses circuit breaker, retry, and bulkhead

### Why resilience is needed

In distributed systems, failures are normal:

- service may be temporarily down
- network may fail
- service may respond too slowly

Resilience4j allows the system to handle that gracefully.

### Patterns used

#### Circuit Breaker

Stops repeated calls to failing service and triggers fallback.

#### Retry

Retries temporary failures before final failure.

#### Bulkhead

Limits concurrent calls and prevents resource exhaustion.

#### Rate Limiter

Restricts request rate at gateway level.

### Viva explanation

If asked "why use resilience in both gateway and admin-service?", say:

Gateway protects external request routing, while Admin Service protects internal orchestration calls. They solve similar failure problems at different layers.

---

## 15. Swagger / OpenAPI

### Why Swagger is important here

This project has multiple services and many endpoints. Swagger helps:

- document endpoints clearly
- test APIs without Postman
- show the full system during viva

### How it is organized

- each service exposes OpenAPI docs
- gateway aggregates them into one Swagger UI

Access:

- `http://localhost:8080/swagger-ui.html`

### Good viva point

Swagger is not just for documentation. In a microservices system, it also reduces testing friction and helps verify route integration from the gateway.

---

## 16. Docker and Docker Compose

### Why Docker is used

Docker gives reproducible runtime environments for:

- MySQL
- RabbitMQ
- Zipkin
- Eureka
- Config Server
- API Gateway
- Auth Service
- Policy Service
- Claim Service
- Admin Service

### Why Compose is useful

Docker Compose starts the entire ecosystem together and injects environment variables such as:

- datasource URLs
- config-server URL
- eureka URL
- rabbitmq host
- zipkin endpoint

### Important startup order

For stable startup:

1. MySQL
2. RabbitMQ
3. Eureka
4. Config Server
5. Business services
6. API Gateway

Why order matters:

- services need dependencies alive before they can start correctly
- gateway fallback can appear if backend services are not yet registered in Eureka

### Docker readiness point

In Docker, the main gateway issue is usually not route logic. It is startup timing and service registration timing. If Eureka does not yet show backend services as UP, gateway will log "No servers available for service".

---

## 17. Zipkin and Monitoring

### Zipkin

Zipkin is used for distributed tracing.

Why that matters:

- a request can pass through gateway and multiple services
- tracing lets us see the path and timing of each hop

If Zipkin is unavailable, the business system usually still works, but tracing warnings appear.

### Eureka dashboard

The Eureka dashboard is the simplest operational check.

If routing fails, first verify:

- is the service UP in Eureka
- did it register with the correct service name

### SonarQube and JaCoCo

The project also includes code quality and coverage tooling.

Why useful in viva:

- shows engineering discipline
- demonstrates testing and quality practices beyond only feature code

---

## 18. Testing Strategy

### Types of tests present

The project contains unit and controller-oriented tests across services.

The test setup uses:

- JUnit 5
- Mockito
- Spring Boot test support
- H2 for test scope in some modules

### Why testing matters here

Microservices have more integration boundaries, so regressions are easier to introduce.

Tests help verify:

- authentication logic
- claim status rules
- payout calculation
- controller behavior
- exception handling
- Feign-dependent service logic using mocks

### Viva explanation

If asked "why mock repositories and clients?", say:

Because unit tests should isolate business logic. Mocking avoids dependency on real databases and remote services, making tests faster and more deterministic.

---

## 19. End-to-End Request Flow

This is the best section to explain the system as a story.

### Scenario A: User login

1. Client sends `POST /auth/login` to Gateway
2. Gateway sees login is public and forwards request
3. Auth Service authenticates username and password
4. Auth Service generates JWT containing username and role
5. Client receives token

### Scenario B: Purchase a policy

Before purchase, a user can browse available plans publicly:

1. Client calls `GET /policies`
2. Gateway allows this request without authentication
3. Gateway routes to Policy Service
4. Policy Service returns available insurance plans

Then the purchase flow begins:

1. Client sends token and calls `POST /policies/purchase`
2. Gateway validates token and role
3. Gateway routes to Policy Service using Eureka
4. Policy Service creates a `PurchasedPolicy` record with status `CREATED`
5. Policy Service publishes RabbitMQ purchase event
6. Admin listener can observe the event asynchronously
7. Response returns to client

### Scenario C: Pay for purchased policy

1. Client calls `POST /policies/purchase/{id}/pay`
2. Gateway validates token
3. Policy Service changes status from `CREATED` to `ACTIVE`

### Scenario D: Submit a claim

1. Client calls `POST /claims` with multipart data
2. Gateway validates token
3. Claim Service receives claim request and optional file
4. Claim Service calls Policy Service through Feign
5. Policy Service returns purchased policy status and deductible
6. Claim Service checks status
7. If not `ACTIVE`, claim is rejected
8. If active, payout is calculated
9. Claim is stored with `PENDING`
10. file is saved if present
11. claim event is published to RabbitMQ
12. response is returned

### Scenario E: Admin reviews claim

1. Admin calls `GET /admin/claims/{id}/download`
2. Gateway validates admin role
3. Admin Service calls Claim Service internal endpoint via Feign
4. Claim Service returns document bytes
5. Claim Service can move status to `UNDER_REVIEW`

### Scenario F: Admin approves or rejects claim

1. Admin calls `/admin/claims/{id}/approve` or `/reject`
2. Gateway validates admin access
3. Admin Service uses Feign to call Claim Service
4. Claim Service updates status to `APPROVED` or `REJECTED`
5. Resilience4j protects this call path

### Scenario G: Admin closes claim

1. Admin calls `/admin/claims/{id}/close`
2. Admin Service calls Claim Service
3. Claim Service allows close only if status is already `APPROVED` or `REJECTED`
4. Final status becomes `CLOSED`

---

## 20. Key Viva Questions and Strong Answers

### Q1. Why use microservices for this project?

Because authentication, policy management, claim processing, and admin orchestration are different business domains. Microservices give separation of concerns, independent deployment, and better fault isolation.

### Q2. Why use Eureka?

Eureka provides service discovery. Gateway and Feign clients do not need fixed IP addresses. They use service names and Eureka resolves them.

### Q3. Why use Config Server?

Config Server centralizes environment-specific configuration so services stay lightweight and configuration changes are easier to manage.

### Q4. Why use JWT?

JWT provides stateless authentication. The gateway can validate user identity and role without server-side session storage.

### Q5. Why use API Gateway?

Gateway is the single entry point. It centralizes authentication, authorization, rate limiting, route forwarding, fallback handling, and Swagger aggregation.

### Q6. Why use OpenFeign?

OpenFeign makes inter-service REST calls cleaner and integrates naturally with Eureka service discovery.

### Q7. Why use RabbitMQ?

RabbitMQ supports asynchronous event communication so non-critical side operations do not slow down user-facing APIs.

### Q8. Why use Resilience4j?

Resilience4j improves fault tolerance with circuit breaker, retry, bulkhead, and rate limiting patterns.

### Q9. Why can Claim Service not directly read Policy Service database?

Because in microservices architecture each service owns its own database. Cross-service data must be accessed through APIs, not by direct table access.

### Q10. What is the most important business rule here?

A claim can only be submitted if the purchased policy is `ACTIVE`. Claim Service enforces this by calling Policy Service first.

---

## 21. Final Summary

SmartSure is a layered Spring microservices application where:

- Eureka handles discovery
- Config Server handles centralized configuration
- Auth Service handles login and token generation
- Policy Service handles policy catalog and purchased policy lifecycle
- Claim Service handles claim workflow and payout calculation
- Admin Service orchestrates administrative operations across services
- API Gateway secures and routes all external requests
- RabbitMQ supports asynchronous event flow
- Feign supports synchronous service-to-service communication
- Resilience4j improves stability
- Docker makes the whole ecosystem portable

If you explain the project in this order during viva, it becomes easy to show that you understand both architecture and code:

infrastructure first, then business services, then gateway, then interactions.

---

## 22. Practical Viva Closing Line

You can close your explanation like this:

SmartSure is a microservices-based insurance platform where each service owns a clear business responsibility, infrastructure services support discovery and configuration, the gateway centralizes security and routing, and both synchronous Feign calls and asynchronous RabbitMQ events are used to coordinate the full insurance lifecycle from login to claim closure.

---

## 23. Backend Code Walkthrough

This section explains the backend code structure itself, because in viva you may be asked not only "what does the service do?" but also:

- why did you create this class
- why is this object needed
- why are DTOs used
- why do we need repository and service separately
- how does data move through the code

### 23.1 Standard Layered Backend Pattern

Most modules follow this pattern:

Request -> Controller -> Service -> Repository -> Database

and back:

Database -> Repository -> Service -> DTO -> Controller -> Response

### Why this pattern is used

#### Controller layer

Controller classes define REST endpoints.

Examples:

- `AuthController`
- `PolicyController`
- `ClaimController`
- `AdminController`

What controllers do:

- receive HTTP requests
- read path variables, query params, request body, multipart file
- call the service layer
- return response DTOs or simple messages

Why we keep controllers thin:

- HTTP handling stays separate from business rules
- code is easier to test
- logic is reusable from service methods

#### Service layer

Service classes contain business logic.

Examples:

- `AuthService`
- `PolicyService`
- `ClaimService`
- `AdminService`

What services do:

- validate business rules
- coordinate repositories
- coordinate Feign clients
- publish RabbitMQ events
- calculate payout, status changes, and orchestration logic

Why service layer is needed:

- controllers should not contain business logic
- repositories should not contain orchestration logic
- code becomes cleaner and easier to maintain

#### Repository layer

Repository interfaces are Spring Data JPA abstractions for database operations.

Examples:

- `UserRepository`
- `PolicyRepository`
- `PurchasedPolicyRepository`
- `ClaimRepository`
- `ReportRepository`

Why repositories are used:

- avoids boilerplate SQL for standard CRUD
- Spring auto-implements many operations
- service layer can call readable methods like `findByUsername(...)`

#### Entity layer

Entity classes map Java objects to database tables using JPA.

Examples:

- `UserApp`
- `Policy`
- `PurchasedPolicy`
- `Claim`
- `Report`

Why entities are used:

- each row in a table becomes a Java object
- JPA / Hibernate can persist and load these objects automatically

#### DTO layer

DTO means Data Transfer Object.

Examples:

- `AuthRequest`
- `AuthResponse`
- `UserRegistrationRequest`
- `PolicyDTO`
- `PolicyResponseDTO`
- `PurchaseRequest`
- `ClaimRequestDTO`
- `ClaimResponseDTO`
- `PurchasedPolicyStatusResponseDTO`

Why DTOs are used:

- request/response shape is controlled
- entities are not exposed directly everywhere
- API contracts stay cleaner
- validation becomes easier

### 23.2 Why Entities and DTOs are both needed

This is one of the most common viva questions.

#### Entity

An entity is for database persistence.

Example:

- `Claim` entity contains DB fields such as id, customerUsername, status, payoutAmount, documentPath

#### DTO

A DTO is for request/response exchange.

Example:

- `ClaimRequestDTO` carries the data required to submit a claim
- `ClaimResponseDTO` carries the cleaned response returned to client

#### Why not return entity directly?

Because:

- entities may contain internal fields we do not want to expose
- request fields and DB fields are often not identical
- API design becomes tightly coupled to DB design

### 23.3 Why ModelMapper is used

Several services define a `ModelMapper` bean.

Why:

- it converts DTO objects to entity objects and vice versa
- avoids repetitive manual field copying

Example:

```java
Claim claim = modelMapper.map(req, Claim.class);
```

What this means:

- `ClaimRequestDTO` object is converted into a `Claim` entity object
- matching field names are copied automatically

Why this helps:

- less boilerplate code
- cleaner service layer

### 23.4 Important Annotations Used in Backend Code

#### `@SpringBootApplication`

Marks the main startup class of a module.

What it does:

- enables component scanning
- enables auto-configuration
- marks the application entry point

#### `@RestController`

Used on controller classes.

What it means:

- the class handles REST endpoints
- return values are written directly as HTTP response body

#### `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`

Used to map Java methods to HTTP routes.

Why important:

- defines the public API of each service

#### `@Service`

Marks business service classes.

Why:

- Spring creates them as beans
- makes them injectable into controllers and other beans

#### `@Repository`

Used for repository classes or interfaces.

Why:

- tells Spring this bean handles persistence

#### `@Entity`

Marks a class as a JPA entity.

Why:

- maps class to a database table

#### `@Id`, `@GeneratedValue`

Used in entity primary key definitions.

Why:

- identifies the database primary key
- allows automatic ID generation

#### `@Bean`

Used in config classes to create Spring-managed objects like:

- `PasswordEncoder`
- `AuthenticationManager`
- `ModelMapper`
- RabbitMQ queue, exchange, binding

#### `@FeignClient`

Used to create service-to-service HTTP client interfaces.

Why:

- avoids manual client boilerplate
- integrates with Eureka

#### `@Cacheable` and `@CacheEvict`

Used in services like Policy Service and Claim Service.

Why:

- read-heavy methods can be cached
- write methods clear stale cache entries

#### `@RabbitListener`

Used in Admin Service.

Why:

- consumes asynchronous messages from RabbitMQ queues

#### `@CircuitBreaker`, `@Retry`, `@Bulkhead`

Used in Admin Service and gateway configuration.

Why:

- adds resilience when remote services fail or respond slowly

### 23.5 How Objects Move in Auth Flow

#### Login request objects

Client sends `AuthRequest`.

`AuthController` receives it:

```java
public AuthResponse getToken(@Valid @RequestBody AuthRequest authRequest)
```

Then:

1. controller passes username/password to `AuthenticationManager`
2. manager verifies credentials
3. controller asks `AuthService` for token
4. `AuthService` loads `UserApp` from `UserRepository`
5. `JwtUtil` generates token
6. controller returns `AuthResponse`

Objects involved:

- `AuthRequest`
- `UsernamePasswordAuthenticationToken`
- `UserApp`
- `AuthResponse`

#### Why each object exists

- `AuthRequest`: captures login input
- `UsernamePasswordAuthenticationToken`: Spring Security authentication wrapper
- `UserApp`: persistent database user entity
- `AuthResponse`: sends token and role back to client

### 23.6 How Objects Move in Policy Purchase Flow

Client sends `PurchaseRequest` to `PolicyController`.

Then:

1. `PolicyController.purchase(...)` receives `PurchaseRequest`
2. controller calls `PolicyService.purchasePolicy(request)`
3. service loads `Policy` by ID from `PolicyRepository`
4. service creates a new `PurchasedPolicy`
5. service copies fields such as `policyId`, `customerUsername`, and `deductibleAmount`
6. service sets status `CREATED`
7. repository saves `PurchasedPolicy`
8. RabbitMQ event message is published
9. string response is returned

Objects involved:

- `PurchaseRequest`
- `Policy`
- `PurchasedPolicy`
- message string for RabbitMQ

#### Why separate `Policy` and `PurchasedPolicy`

Because:

- `Policy` is the product master data
- `PurchasedPolicy` is the specific customer purchase record

This is a very strong point for viva.

### 23.7 How Objects Move in Claim Submission Flow

This is the most important code walkthrough in the system.

Client sends multipart request to `ClaimController`:

- one part is `ClaimRequestDTO`
- another optional part is `MultipartFile`

Flow:

1. `ClaimController.submitClaim(...)` receives request DTO and file
2. controller calls `ClaimService.submitClaim(req, file)`
3. service calls `PolicyClient.getPurchasedPolicyStatus(...)`
4. Policy Service returns `PurchasedPolicyStatusResponseDTO`
5. claim service checks `status`
6. if status is not `ACTIVE`, throw exception
7. `ClaimRequestDTO` is mapped into `Claim` entity using `ModelMapper`
8. deductible is copied into entity
9. payout is calculated and stored
10. status is set to `PENDING`
11. repository saves claim
12. if file exists, file is written to disk and path is stored
13. entity is mapped to `ClaimResponseDTO`
14. RabbitMQ message is published
15. response DTO is returned

Objects involved:

- `ClaimRequestDTO`
- `MultipartFile`
- `PurchasedPolicyStatusResponseDTO`
- `Claim`
- `ClaimResponseDTO`

#### Why this design is good

- claim rules stay inside service layer
- claim-service does not illegally touch policy DB
- payout logic is centralized
- file handling is attached to claim lifecycle cleanly

### 23.8 How Objects Move in Admin Approval Flow

Admin calls `PUT /admin/claims/{id}/approve`.

Flow:

1. `AdminController.approveClaim(id)` receives request
2. controller calls `AdminService.approveClaim(id)`
3. `AdminService` method is wrapped with:
   - circuit breaker
   - retry
   - bulkhead
4. service calls `ClaimFeignClient.updateClaimStatus(id, "APPROVED")`
5. Feign sends HTTP request to Claim Service internal endpoint
6. Claim Service updates `Claim` entity status
7. updated object is returned
8. if downstream service fails, fallback method may return graceful message

Objects involved:

- path variable `id`
- Feign client proxy object
- `ClaimResponseDTO` or generic response object

### 23.9 Gateway Code Flow

When a protected request enters the system:

1. Gateway route matches path from `config-repo/api-gateway.yml`
2. `AuthenticationFilter` runs
3. it extracts `Authorization` header
4. `JwtUtil` validates token and extracts role
5. `hasAccess(...)` decides whether method/path is allowed
6. `RateLimitFilter` checks request rate
7. request is routed to downstream service via `lb://service-name`
8. if service is unavailable, fallback can trigger

Objects involved:

- `ServerWebExchange`
- `HttpHeaders.AUTHORIZATION`
- token string
- role string
- gateway filters

#### Why `ServerWebExchange` is used

Gateway is built on Spring WebFlux, which is reactive. `ServerWebExchange` is the reactive request-response container used by filters.

### 23.10 Why Config Classes Exist

A config class exists when object creation should be centralized and explicit.

Examples:

- `SecurityConfig`
- `RabbitMQConfig`
- OpenAPI config classes

Why not create these objects manually everywhere:

- one centralized source of truth is better
- Spring can inject the same bean where needed
- configuration becomes reusable and maintainable

### 23.11 Why `CommandLineRunner` is used in Auth Service

The auth service creates default admin user on startup if missing.

Why this is useful:

- simplifies first-time setup
- avoids manual admin insertion
- guarantees admin login exists for testing and demonstration

### 23.12 Why custom exceptions are used

The project contains `ResourceNotFoundException` and global exception handler classes.

Why this is good design:

- business failures are represented clearly
- controllers do not need repetitive try-catch blocks
- API returns cleaner error responses

### 23.13 Why file upload logic is inside Claim Service

Claim documents belong to claims, so file handling is part of claim domain logic.

Why not keep it in gateway or admin-service:

- gateway should only route and validate
- admin-service only reviews claims, it does not own claim creation
- claim-service is the true owner of claim document lifecycle

### 23.14 Why Gateway has both filters and fallback controller

Filters and fallback do different jobs.

- filter job:
  validate request before routing

- fallback job:
  handle downstream failure after routing attempt

So both are needed.

---

## 24. Code-Level Explanation by Service Objects

This section gives a quick object dictionary you can use in viva.

### Auth Service Objects

- `UserApp`
  User entity stored in auth database.

- `UserRegistrationRequest`
  DTO for public registration input.

- `AdminRequest`
  DTO for admin-created user input.

- `AuthRequest`
  DTO containing login credentials.

- `AuthResponse`
  DTO returned after successful login.

- `UserRepository`
  Loads and saves users.

- `AuthService`
  Handles registration, role control, and token generation.

- `JwtUtil`
  Creates and validates JWT.

### Policy Service Objects

- `Policy`
  Insurance product definition.

- `PurchasedPolicy`
  Customer’s purchased policy record.

- `PolicyDTO`
  DTO used to create policy.

- `PolicyResponseDTO`
  DTO returned for policy details.

- `PurchaseRequest`
  DTO used when customer buys a policy.

- `PurchasedPolicyResponseDTO`
  DTO returned for purchased policy information.

- `PolicyRepository`
  CRUD repository for policy catalog.

- `PurchasedPolicyRepository`
  CRUD repository for purchase records.

- `PolicyService`
  Handles product and purchase lifecycle logic.

### Claim Service Objects

- `Claim`
  Claim entity stored in claim database.

- `ClaimRequestDTO`
  Incoming claim submission data.

- `ClaimResponseDTO`
  Outgoing claim response data.

- `PurchasedPolicyStatusResponseDTO`
  Feign response object from Policy Service.

- `ClaimRepository`
  Database access for claims.

- `PolicyClient`
  Feign client to policy-service.

- `ClaimService`
  Handles claim validation, payout, file handling, and status transitions.

### Admin Service Objects

- `Report`
  Admin reporting entity.

- `ReportRepository`
  Repository for saved reports.

- `PolicyFeignClient`
  Admin-to-policy service client.

- `ClaimFeignClient`
  Admin-to-claim service client.

- `AdminService`
  Orchestrates admin workflows.

- `NotificationListener`
  RabbitMQ consumer for policy and claim events.

### Gateway Objects

- `AuthenticationFilter`
  JWT and role validation filter.

- `RateLimitFilter`
  Request throttling filter.

- `JwtUtil`
  Token parser and validator at gateway layer.

- `FallbackController`
  Controlled fallback responses for service failures.

---

## 25. How To Explain Code in Viva in 2 Minutes

If examiner asks, "Explain your backend code structure," you can say:

I followed a layered Spring Boot design in each microservice. Controller classes expose REST APIs, service classes hold business logic, repository interfaces handle JPA persistence, entities represent database tables, and DTOs define request and response contracts. For inter-service calls I used OpenFeign so services communicate by Eureka service name instead of fixed URLs. For asynchronous events I used RabbitMQ. Gateway contains filters for JWT validation and rate limiting, while resilience is handled using Resilience4j. This keeps each layer focused and makes the project easier to test, maintain, and scale.

---

## 26. Detailed Security Explanation: JWT, Authentication, Authorization

This section is for viva questions specifically on security.

The main security concepts in this project are:

- authentication
- authorization
- JWT token generation and validation
- password hashing
- Spring Security integration
- Gateway-level request protection

### 26.1 Authentication vs Authorization

This is the first thing to explain clearly.

#### Authentication

Authentication means:

"Who are you?"

In this project, authentication happens during login when the system checks:

- does the username exist
- does the password match the stored hashed password

If both are correct, the user is authenticated.

#### Authorization

Authorization means:

"What are you allowed to do?"

In this project, authorization happens after authentication, when the system checks the user's role:

- `ADMIN`
- `CUSTOMER`

Examples:

- admin can access `/admin/**`
- customer cannot access `/admin/**`
- customer can log in, purchase policy, and submit claim

### 26.2 Where Security Is Implemented

Security is split across two places:

#### In `auth-service`

Used for:

- login credential verification
- password hashing
- loading user details from database
- generating JWT token
- securing auth-service internal admin-only endpoint

#### In `api-gateway`

Used for:

- validating JWT on incoming requests
- extracting role from token
- deciding access based on role and path
- blocking invalid or unauthorized requests before forwarding

This split is important.

Why?

- auth-service knows how to verify username/password and issue token
- gateway knows how to protect the rest of the system using that token

### 26.3 User Object Used in Security

The central database object is `UserApp` from `auth-service`.

Fields:

- `id`
- `username`
- `password`
- `email`
- `name`
- `phone`
- `address`
- `role`

Why it is important:

- `username` is used during login
- `password` stores BCrypt-hashed password
- `role` is the basis of authorization

The repository is `UserRepository`, which provides:

```java
Optional<UserApp> findByUsername(String username);
```

Why that repository method is important:

- login needs to load user by username
- token generation needs to read user role
- Spring Security user details loading also needs it

### 26.4 Password Hashing

Password hashing is configured in `SecurityConfig.java` using:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

#### Why BCrypt is used

Passwords must not be stored as plain text.

If password is stored as plain text:

- database leak reveals all user passwords
- same password reuse becomes dangerous

BCrypt solves this by:

- hashing password before storing
- making hash one-way
- making brute force more difficult

#### Where password hashing happens

In `AuthService.saveUser(...)`:

```java
user.setPassword(passwordEncoder.encode(regRequest.getPassword()));
```

And in admin-created user flow:

```java
user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
```

So passwords are encoded before saving to DB.

### 26.5 Spring Security Login Flow in Auth Service

This is the most important authentication flow.

#### Step 1: Client sends login request

Endpoint:

```java
POST /auth/login
```

Handled in `AuthController`:

```java
public AuthResponse getToken(@Valid @RequestBody AuthRequest authRequest)
```

The input DTO `AuthRequest` contains:

- username
- password

#### Step 2: AuthenticationManager verifies credentials

Inside controller:

```java
Authentication authenticate = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
```

What this does:

- wraps raw username/password into `UsernamePasswordAuthenticationToken`
- passes them to Spring Security
- Spring Security tries to authenticate the user

Why `UsernamePasswordAuthenticationToken` is used:

- it is the standard Spring Security authentication request object
- it carries principal and credentials

#### Step 3: CustomUserDetailsService loads user from DB

Spring Security uses `CustomUserDetailsService`.

In `CustomUserDetailsService.loadUserByUsername(...)`:

1. `UserRepository.findByUsername(username)` loads `UserApp`
2. if not found, throws `UsernameNotFoundException`
3. converts `UserApp` into Spring Security `UserDetails`

Code concept:

```java
return new User(
    userApp.getUsername(),
    userApp.getPassword(),
    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userApp.getRole()))
);
```

#### Why convert to `UserDetails`

Spring Security does not work directly with our custom entity automatically.

It understands `UserDetails`, so this conversion is needed.

Why authority is `"ROLE_" + role`:

- Spring Security convention expects role authorities like `ROLE_ADMIN`
- this is needed for annotations such as `@PreAuthorize`

#### Step 4: Password comparison happens

Spring Security compares:

- raw password from login request
- hashed password from `UserDetails`

using the configured `PasswordEncoder`.

If match succeeds, authentication is successful.

### 26.6 JWT Token Generation

After successful authentication, `AuthController` calls:

```java
String token = service.generateToken(authRequest.getUsername());
```

This reaches `AuthService.generateToken(...)`.

That method:

1. loads the user from database
2. gets the role
3. calls `jwtUtil.generateToken(username, role)`

#### JWT generation code

In `auth-service JwtUtil.java`:

```java
public String generateToken(String username, String role) {
    return Jwts.builder()
            .setClaims(Map.of("role", role))
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1800000))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
}
```

### 26.7 Detailed Meaning of JWT Fields

#### `setClaims(Map.of("role", role))`

Adds custom claim data inside the token.

Here the custom claim is:

- `role`

Why we store role:

- gateway can authorize request without calling database for every request
- role travels inside the signed token

#### `setSubject(username)`

Stores the username as the token subject.

Why:

- identifies which user the token belongs to

#### `setIssuedAt(...)`

Stores token creation time.

Why:

- useful for audit and token lifecycle

#### `setExpiration(...)`

Sets token expiry to 30 minutes.

Why:

- limits misuse if token is stolen
- forces time-bounded access

#### `signWith(key(), SignatureAlgorithm.HS256)`

Signs the token cryptographically.

Why:

- prevents user from editing the token manually
- if token payload changes, signature validation fails

#### `compact()`

Converts the built token into final JWT string.

### 26.8 Secret Key and Signing

In both auth-service and gateway `JwtUtil`, the secret is read from:

```java
@Value("${jwt.secret:...}")
private String secret;
```

Then:

```java
private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
}
```

#### Why secret key is important

The secret is used to:

- sign token in auth-service
- verify token in gateway

If gateway and auth-service do not use the same secret:

- gateway cannot validate tokens issued by auth-service

### 26.9 JWT Validation

In both `JwtUtil` classes:

```java
public void validateToken(String token) {
    Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
}
```

What this checks:

- token signature is correct
- token is not malformed
- token is not expired

If invalid, an exception is thrown.

### 26.10 What Auth Service JwtFilter Does

The `JwtFilter` in auth-service is a `OncePerRequestFilter`.

Why `OncePerRequestFilter` is used:

- ensures filter runs once per request
- standard Spring Security way to process JWT on servlet side

#### Flow inside `doFilterInternal(...)`

1. read `Authorization` header
2. check if it starts with `Bearer `
3. extract token
4. validate token using `jwtUtil.validateToken(token)`
5. extract username and role
6. if security context is empty, create authentication object
7. set it in `SecurityContextHolder`
8. continue filter chain

Important code meaning:

```java
new UsernamePasswordAuthenticationToken(
    user,
    null,
    List.of(new SimpleGrantedAuthority("ROLE_" + role))
)
```

What this means:

- principal = username
- credentials = not needed now, so null
- authorities = role converted to Spring Security authority

#### Why auth-service still has a JWT filter

Even though gateway handles most JWT validation for external access, auth-service can still protect its own internal secured endpoints, such as admin-only user creation.

### 26.11 SecurityFilterChain in Auth Service

In `SecurityConfig.securityFilterChain(...)`:

Public paths are:

- `/auth/register`
- `/auth/login`
- `/v3/api-docs/**`
- `/swagger-ui/**`

Everything else is authenticated.

Why:

- registration and login must be public
- Swagger docs should be accessible for testing
- protected endpoints require authentication

### 26.12 Method-Level Authorization in Auth Service

In `AuthController`:

```java
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
```

This is used on:

- `/auth/admin/create-user`

What it means:

- only admin-authenticated users can create users through that endpoint

Why this is authorization and not authentication:

- user is already authenticated
- now we are deciding whether the user has permission

### 26.13 Gateway AuthenticationFilter Detailed Explanation

This is the central protection for the whole system.

In `api-gateway AuthenticationFilter`:

#### Step 1: read request details

```java
String path = exchange.getRequest().getURI().getPath();
String method = exchange.getRequest().getMethod().name();
```

Why:

- authorization depends on URL and HTTP method

#### Step 2: allow public endpoints

```java
if (isPublic(path, method)) return chain.filter(exchange);
```

Public endpoints are:

- login
- register
- `GET /policies`
- swagger
- api docs

Why:

- users must be able to log in first
- users should be able to browse available policies before authentication
- docs should be reachable for testing
- other protected business endpoints still require authentication

#### Step 3: extract bearer token

```java
String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
```

If header missing or invalid:

- gateway returns `401 UNAUTHORIZED`

Why:

- protected routes require valid bearer token

#### Step 4: validate token and extract role

```java
jwtUtil.validateToken(token);
String role = jwtUtil.extractRole(token);
```

Why:

- gateway must ensure token is genuine
- role is needed for authorization decision

#### Step 5: authorization check using `hasAccess(...)`

Rules in current code:

- `GET /policies` is allowed publicly before role validation
- if role is `ADMIN`, allow everything
- if path starts with `/admin/` and user is not admin, deny
- if role is `CUSTOMER`, allow the permitted policy and claim operations defined by method/path checks

If access fails:

- gateway returns `403 FORBIDDEN`

### 26.14 Difference Between 401 and 403

This is another very common viva question.

#### 401 Unauthorized

Means:

- you are not authenticated
- token missing, invalid, or expired

In this project, gateway returns 401 when:

- no bearer token
- invalid token

#### 403 Forbidden

Means:

- you are authenticated
- but you are not allowed to perform this action

In this project, gateway returns 403 when:

- customer tries to access admin path
- role-based rule fails

### 26.15 Why Gateway Authorization Is Useful

Why not do all role checks in every service?

Because:

- role logic would be duplicated
- every service would need identical token parsing
- request rejection would happen too late

Gateway-level authorization gives:

- centralized security
- reduced duplication
- faster rejection of bad requests

### 26.16 Complete Security Flow from Login to Protected API

This is the full story you can say in viva.

#### Login phase

1. user sends username/password to auth-service through gateway
2. auth-service uses Spring Security `AuthenticationManager`
3. `CustomUserDetailsService` loads user from DB
4. password is verified using BCrypt
5. `JwtUtil` generates token with username and role
6. token is returned to user

#### Protected request phase

1. user sends bearer token in `Authorization` header
2. request reaches gateway
3. gateway `AuthenticationFilter` validates token
4. gateway extracts role
5. gateway checks path and method permissions
6. if allowed, gateway forwards request to target service
7. if not allowed, gateway returns 401 or 403

### 26.17 Why JWT is Better Than Session for This Project

JWT suits this project because it is a distributed microservices architecture.

If traditional session were used:

- server would need shared session storage
- scaling services becomes harder
- gateway and services would need session coordination

With JWT:

- token is self-contained
- gateway can verify it independently
- no shared session store is required

### 26.18 Security Objects Dictionary

Use this as quick viva revision.

- `UserApp`
  Database user entity.

- `UserRepository`
  Loads user from DB.

- `AuthRequest`
  Login input DTO.

- `AuthResponse`
  Login output DTO containing token and role.

- `PasswordEncoder`
  Hashes passwords using BCrypt.

- `AuthenticationManager`
  Verifies username/password during login.

- `CustomUserDetailsService`
  Converts `UserApp` into Spring Security `UserDetails`.

- `JwtUtil`
  Generates, validates, and parses JWT.

- `JwtFilter`
  Reads bearer token and sets security context inside auth-service.

- `SecurityFilterChain`
  Defines which endpoints are public and which require auth.

- `AuthenticationFilter` in gateway
  Validates JWT and enforces route authorization.

- `UsernamePasswordAuthenticationToken`
  Spring Security authentication object used during login and context setup.

- `SimpleGrantedAuthority`
  Spring Security representation of role/authority.

- `SecurityContextHolder`
  Stores current authenticated user context during request processing.

### 26.19 Best Viva Answer for Security

If examiner says, "Explain JWT, authentication, and authorization in your project," you can say:

Authentication in my project happens in auth-service during login. Spring Security uses AuthenticationManager and CustomUserDetailsService to load the user from database and verify the BCrypt-hashed password. After successful authentication, auth-service generates a JWT containing username and role, signed with a secret key using HS256. Authorization is then handled mainly in the API Gateway. For each protected request, the gateway extracts the bearer token, validates the signature and expiry, extracts the role, and checks whether the role is allowed to access that path and HTTP method. If the token is missing or invalid, it returns 401. If the token is valid but the role is not allowed, it returns 403.
