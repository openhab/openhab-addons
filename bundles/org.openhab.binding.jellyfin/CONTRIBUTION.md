# Jellyfin Binding Contribution Guide

This document provides information for developers who want to contribute to the Jellyfin binding for openHAB.

## Class Diagram

The following diagram shows the main classes and their relationships within the Jellyfin binding:

```mermaid
classDiagram
    HandlerFactory <|-- BaseThingHandlerFactory
    ServerHandler <|-- BaseBridgeHandler
    ServerDiscoveryService <|-- AbstractDiscoveryService
    ExceptionHandler ..|> ExceptionHandlerType
    
    HandlerFactory --> ApiClientFactory : uses
    ServerHandler --> ApiClient : uses
    ServerHandler --> ExceptionHandler : uses
    ServerHandler --> Configuration : uses
    ServerDiscoveryService --> ServerDiscovery : uses
    ServerDiscovery ..> ServerDiscoveryResult : creates
    ApiClientFactory ..> ApiClient : creates
    HandlerFactory ..> ServerHandler : creates
    
    class HandlerFactory {
        -ApiClientFactory apiClientFactory
        +supportsThingType(ThingTypeUID) boolean
        +createHandler(Thing) ThingHandler
    }
    
    class ServerHandler {
        -Logger logger
        -ExceptionHandler exceptionHandler
        -ApiClient apiClient
        -Configuration configuration
        +initialize()
        +handleCommand()
        +dispose()
    }
    
    class ApiClientFactory {
        +createApiClient() ApiClient
    }
    
    class ApiClient {
        +authenticateWithToken(String)
    }
    
    class ServerDiscoveryService {
        -Logger logger
        +startScan()
    }
    
    class ServerDiscovery {
        +discoverServers() List
    }
    
    class ExceptionHandlerType {
        <<interface>>
        +handle(Exception)
    }
    
    class ExceptionHandler {
        +handle(Exception)
    }
    
    class Configuration {
        +String hostname
        +int port
        +boolean ssl
    }
```

## Key Components

1. **HandlerFactory**: Creates thing handlers for the binding.
2. **ServerHandler**: Main bridge handler for Jellyfin servers.
3. **ApiClientFactory**: Creates API client instances.
4. **ApiClient**: Handles communication with the Jellyfin server.
5. **ServerDiscoveryService**: Discovers Jellyfin servers on the network.
6. **ExceptionHandler**: Handles exceptions that occur during binding operation.

## Development Workflow

When contributing to this binding, please follow these guidelines:

1. Make sure your code follows the openHAB code style and conventions.
2. Write unit tests for your changes.
3. Update documentation as needed.
4. Submit a pull request with a clear description of your changes.
