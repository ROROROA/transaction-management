# Transaction Management Service

A Spring Boot microservice for managing financial transactions, deployed on Kubernetes.

## Technology Stack

- **Backend**: Spring Boot 3 with Java 21
- **Build Tool**: Maven
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **Documentation**: Swagger/OpenAPI
- **Monitoring**: Spring Actuator

## Features

- RESTful API for transaction management
- Health monitoring via Spring Actuator
- API documentation with Swagger
- Containerized deployment
- Kubernetes configuration for scalability

## Development Setup

### Prerequisites

- JDK 21
- Maven
- Docker
- Kubernetes cluster or Minikube

### Building the Application

```bash
# Build with Maven
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