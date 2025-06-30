# Transaction Management Service

A Spring Boot microservice for managing financial transactions, deployed on Kubernetes.

## Business Overview

The Transaction Management Service is designed to handle financial transactions between accounts. It provides a robust platform for:

- Creating and managing transaction records
- Supporting various transaction statuses (PENDING, COMPLETED, FAILED, CANCELLED)
- Enforcing business rules for transactions (e.g., sender and receiver cannot be the same)
- Implementing proper status transition rules
- Providing transaction history and detailed lookups
- Supporting pagination for large transaction volumes

## Architecture

This service draws inspiration from Domain-Driven Design (DDD) principles to better align the codebase with business requirements:

- **Layered Architecture**:
  - **Presentation Layer**: REST controllers handling HTTP requests and responses
  - **Domain Layer**: Transaction Service layer implementing core business logic
  - **Infrastructure Layer**: Repositories and external integrations

- **Domain Concepts**:
  - **Rich Domain Models**: Business logic encapsulated within Transaction entity
  - **Aggregates**: Transaction as the main aggregate root with defined boundaries
  - **Repository Pattern**: Clean separation between domain and persistence layers
  - **Value Objects**: Immutable objects like TransactionCreateRequest and TransactionResponse
  - **Domain Services**: Transaction Service implementing domain operations

The Transaction Service layer acts as the primary domain layer, containing all business rules, validations, and transaction processing logic. This approach ensures that business rules are centralized and not scattered throughout the application.

## Libraries and Dependencies

- **Spring Boot Starter Web**: Core framework for building web applications
- **Spring Boot Starter Validation**: Bean validation with Hibernate validator
- **Spring Boot Starter Cache**: Caching abstraction for Spring applications
- **Spring Boot Starter Actuator**: Production-ready features for monitoring and management
- **Spring Data Commons**: Core Spring Data functionality without specific database implementation
- **Caffeine**: High-performance, near-optimal caching library
- **Lombok**: Reduces boilerplate code for model/data objects
- **Springdoc OpenAPI**: API documentation with Swagger UI
- **Spring Boot DevTools**: Development-time tools for faster restarts and live reload
- **Spring Boot Starter Test**: Testing framework including JUnit, Mockito, and AssertJ

## Technology Stack

### Backend
- **Framework**: Spring Boot 3 with Java 21
- **Build Tool**: Maven
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **Documentation**: Swagger/OpenAPI
- **Monitoring**: Spring Actuator
- **Caching**: Caffeine

### Frontend
- **Framework**: Vue 3 with Composition API
- **State Management**: Pinia
- **Routing**: Vue Router
- **Build Tool**: Vite

## Features

- RESTful API for transaction management
- Health monitoring via Spring Actuator
- API documentation with Swagger
- Containerized deployment
- Kubernetes configuration for scalability
- Responsive web interface for transaction management
- Comprehensive test suite including unit, integration, and stress tests

## Testing

The application includes a comprehensive testing strategy:

- **Unit Tests**: Testing individual components in isolation
- **Integration Tests**: Testing interactions between components
- **Stress Tests**: Simulating high concurrency to ensure system stability under load
- **Component Tests**: Testing frontend components with Vue Test Utils

All tests can be run with:

```bash
./mvnw test
```

## Development Setup

### Prerequisites

- JDK 21
- Maven
- Docker
- Kubernetes cluster or Minikube
- Node.js and npm (for frontend development)

### Building the Application

```bash
# Build backend with Maven
./mvnw clean package


# Build Docker image
docker build -t transaction-management:latest .
```

### Deployment

```bash
# Deploy to Kubernetes
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

## API Documentation

Access the Swagger UI at `/swagger-ui.html` when the application is running.

## Monitoring

Health and info endpoints are available at:
- `/actuator/health`
- `/actuator/info`

## Configuration

The application can be configured through `application.properties` file or environment variables.

## License

This project is licensed under the MIT License - see the LICENSE file for details.