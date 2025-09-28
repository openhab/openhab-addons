# Jellyfin Binding Contribution Guide

This document provides information for developers who want to contribute to the Jellyfin binding for openHAB.

## Class Diagram

The following diagram shows the main classes and their relationships within the Jellyfin binding:

```mermaid
classDiagram
    %% Class inheritance relationships
    HandlerFactory <|-- BaseThingHandlerFactory
    ServerHandler <|-- BaseBridgeHandler
    ServerDiscoveryService <|-- AbstractDiscoveryService
    ExceptionHandler ..|> ExceptionHandlerType
    AbstractTask <|-- ConnectionTask
    AbstractTask <|-- RegistrationTask
    AbstractTask <|-- UpdateTask
    AbstractTask <|-- UsersListTask
    AbstractTask <|-- ClientScanTask
    ApiClient <|-- org.openhab.binding.jellyfin.internal.api.generated.ApiClient
    
    %% Class dependencies and usage relationships
    HandlerFactory --> ApiClientFactory : uses
    ServerHandler --> ApiClient : uses
    ServerHandler --> ExceptionHandler : uses
    ServerHandler --> Configuration : uses
    ServerHandler --> TaskFactory : uses
    TaskFactory ..> AbstractTask : creates
    ServerDiscoveryService --> ServerDiscovery : uses
    ServerDiscovery ..> ServerDiscoveryResult : creates
    ApiClientFactory ..> ApiClient : creates
    HandlerFactory ..> ServerHandler : creates
    
    %% Class definitions with key attributes and methods
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
        -Map~String,ScheduledFuture~ scheduledTasks
        +initialize()
        +handleCommand()
        +dispose()
    }
    
    class ApiClientFactory {
        +createApiClient() ApiClient
    }
    
    class ApiClient {
        +authenticateWithToken(String)
        +updateBaseUri(String)
    }
    
    class ServerDiscoveryService {
        -Logger logger
        -ServerDiscovery serverDiscovery
        +startScan()
    }
    
    class ServerDiscovery {
        -int port
        -int timeout
        +discoverServers() List
    }
    
    class AbstractTask {
        <<abstract>>
        -String id
        -int startupDelay
        -int interval
        +getId() String
        +getStartupDelay() int
        +getInterval() int
        +run()
    }
    
    class TaskFactory {
        <<static>>
        +createConnectionTask(ApiClient, Consumer, ExceptionHandlerType) ConnectionTask
        +createRegistrationTask(ApiClient, ExceptionHandlerType) RegistrationTask
        +createUpdateTask(ApiClient, ExceptionHandlerType) UpdateTask
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
        +String path
    }
    
    class BindingConfiguration {
        +int discoveryPort
        +int discoveryTimeout
        +String discoveryMessage
        +static getConfiguration(ConfigurationAdmin) BindingConfiguration
    }
```

## Key Components

1. **HandlerFactory**: Creates thing handlers for the binding.
2. **ServerHandler**: Main bridge handler for Jellyfin servers.
3. **ApiClientFactory**: Creates API client instances for different API versions.
4. **ApiClient**: Handles communication with the Jellyfin server and manages authentication.
5. **ServerDiscoveryService**: Discovers Jellyfin servers on the network using UDP broadcasts.
6. **TaskFactory**: Creates various task instances used for server communication.
7. **AbstractTask**: Base class for all tasks that can be scheduled for execution.
8. **BindingConfiguration**: Contains configuration settings for the binding.
9. **ExceptionHandler**: Handles exceptions that occur during binding operation.

## API Version Support

The Jellyfin binding is designed to work with multiple server API versions.
The current implementation supports:

1. **Current API**: For Jellyfin server versions 10.9.0 and newer (including 10.10.x)

The API client code is automatically generated from the OpenAPI specifications using the OpenAPI Generator.
This approach allows for easier adaptation to API changes and better maintainability compared to using external SDKs.

## Development Workflow

When contributing to this binding, please follow these guidelines:

1. Make sure your code follows the openHAB code style and conventions.
2. Write unit tests for your changes.
3. Update documentation as needed.
4. Submit a pull request with a clear description of your changes.
