# Jellyfin Binding Contribution Guide

This document provides information for developers who want to contribute to the Jellyfin binding for openHAB.

## Class Diagram

The following diagram shows the main classes and their relationships within the Jellyfin binding:

```mermaid
classDiagram
    %% Class inheritance relationships
    AbstractTask <|-- ConnectionTask
    AbstractTask <|-- RegistrationTask
    AbstractTask <|-- UpdateTask
    AbstractTask <|-- UsersListTask
    AbstractTask <|-- ClientScanTask
    
    %% Class dependencies and usage relationships
    HandlerFactory --> ApiClientFactory : uses
    ServerHandler --> ApiClient : uses
    ServerHandler --> ExceptionHandler : uses
    ServerHandler --> Configuration : uses
    ServerHandler --> TaskFactory : uses
    ServerHandler --> TaskManager : uses
    ServerHandler --> ServerState : uses
    ServerDiscoveryService ..> ServerDiscovery : creates
    ServerDiscoveryService --> BindingConfiguration : uses
    TaskFactory ..> AbstractTask : creates
    TaskManager --> AbstractTask : manages
    TaskManager --> ServerState : evaluates
    ServerDiscovery ..> ServerDiscoveryResult : creates
    ApiClientFactory ..> ApiClient : creates
    HandlerFactory ..> ServerHandler : creates
    
    %% Class definitions with key attributes and methods
    class HandlerFactory {
        +createHandler(Thing) ThingHandler
    }
    
    class ServerHandler {
        +initialize()
        +handleCommand()
        +dispose()
        -transitionToState(ServerState)
    }
    
    class TaskManager {
        <<utility>>
        +static getTaskIdsForState(ServerState) Set~String~
        +static transitionTasksForState(Map, ServerState, ScheduledExecutorService)
        +static startTask(Map, String, ScheduledExecutorService)
        +static stopTask(Map, String)
        +static stopAllTasks(Map)
    }
    
    class ServerState {
        <<enumeration>>
        INITIALIZING
        ONLINE
        OFFLINE
        ERROR
        +getRequiredTasks() Set~String~
    }
    
    class ApiClientFactory {
        +createApiClient() ApiClient
    }
    
    class ApiClient {
        <<interface>>
        +authenticateWithToken(String)
        +updateBaseUri(String)
    }
    
    class ServerDiscoveryService {
        +startScan()
        -createServerDiscovery() ServerDiscovery
    }
    
    class ServerDiscovery {
        -int port
        -int timeout
        +ServerDiscovery(int port, int timeout)
        +discoverServers() List
    }
    
    class AbstractTask {
        <<abstract>>
        +getId() String
        +getStartupDelay() int
        +getInterval() int
        +run()
    }
    
    class TaskFactory {
        <<static>>
        +createConnectionTask(ApiClient, Consumer, ExceptionHandler) ConnectionTask
        +createRegistrationTask(ApiClient, ExceptionHandler) RegistrationTask
        +createUpdateTask(ApiClient, ExceptionHandler) UpdateTask
    }
    
    class ExceptionHandler {
        +handle(Exception)
    }
    
    class Configuration {
        +String hostname
        +int port
        +boolean ssl
        +String path
        +String token
        +getServerURI() URI
    }
    
    class BindingConfiguration {
        <<binding config>>
        +int discoveryPort
        +int discoveryTimeout
        +String discoveryMessage
        +static getConfiguration(ConfigurationAdmin) BindingConfiguration
    }
    
    class ServerDiscoveryResult {
        +String id
        +String name
        +String uri
        +String version
    }
    
    class ConnectionTask {
        %% Inherits run() from AbstractTask
    }
```

## Key Components

1. **HandlerFactory**: Creates thing handlers for the binding.
2. **ServerHandler**: Main bridge handler for Jellyfin servers that orchestrates server communication and state management.
3. **TaskManager**: Stateless utility class that manages task lifecycle operations based on server state transitions.
4. **ServerState**: Enumeration defining server states (INITIALIZING, ONLINE, OFFLINE, ERROR) and their required task sets.
5. **ApiClientFactory**: Creates API client instances for different API versions.
6. **ApiClient**: Handles communication with the Jellyfin server and manages authentication.
7. **ServerDiscoveryService**: Discovers Jellyfin servers on the network using UDP broadcasts.
8. **TaskFactory**: Creates various task instances used for server communication.
9. **AbstractTask**: Base class for all tasks that can be scheduled for execution.
10. **BindingConfiguration**: Contains configuration settings for the binding.
11. **ExceptionHandler**: Handles exceptions that occur during binding operation.

## Architecture Overview

The binding follows a state-driven architecture where:

- **ServerHandler** manages the overall server connection lifecycle and delegates task management to the **TaskManager** utility
- **TaskManager** provides stateless operations for starting, stopping, and transitioning tasks based on **ServerState**
- **ServerState** enum defines which tasks should be active for each server state
- Tasks are created by **TaskFactory** and extend **AbstractTask** for scheduled execution
- **ApiClient** provides the communication layer with version-specific implementations

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
