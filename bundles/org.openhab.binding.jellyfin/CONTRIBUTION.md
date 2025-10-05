# Jellyfin Binding Contribution Guide

This document provides information for developers who want to contribute to the Jellyfin binding for openHAB.

## Class Diagram

The following diagram shows the main classes and their relationships within the Jellyfin binding:

```mermaid
classDiagram
    %% Interface implementations
    ErrorEventListener <|.. ServerHandler
    
    %% Class dependencies and usage relationships
    HandlerFactory --> ApiClientFactory : uses
    ServerHandler --> ApiClient : uses
    ServerHandler --> TaskFactory : uses
    ServerHandler --> TaskManager : uses
    ServerHandler --> ErrorEventBus : owns
    ServerDiscoveryService ..> ServerDiscovery : creates
    ServerDiscoveryService --> BindingConfiguration : uses
    TaskFactory ..> AbstractTask : creates
    TaskManager --> AbstractTask : manages
    ServerDiscovery ..> ServerDiscoveryResult : creates
    ApiClientFactory ..> ApiClient : creates
    HandlerFactory --> ServerHandler : creates
    ContextualExceptionHandler --> ErrorEventBus : publishes to
    ErrorEventBus --> ErrorEventListener : notifies
    TaskFactory --> ContextualExceptionHandler : creates
    HandlerFactory ..> ServerHandler : creates
    EventDrivenExceptionHandler --> ErrorEventBus : publishes to
    ErrorEventBus --> ErrorEventListener : notifies
    TaskFactory --> EventDrivenExceptionHandler : creates
    
    %% Class definitions with key attributes and methods
    class HandlerFactory {
        +createHandler(Thing) ThingHandler
    }
    
    class ServerHandler {
        +initialize()
        +handleCommand()
        +dispose()
        +onErrorEvent(ErrorEvent)
    }
    
    class TaskManager {
        <<utility>>
        +static getTaskIdsForState(ServerState) List~String~
        +static transitionTasksForState(Map, ServerState, ScheduledExecutorService)
        +static startTask(Map, String, ScheduledExecutorService)
        +static stopTask(Map, String)
        +static stopAllTasks(Map)
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
    }
    
    class ServerDiscovery {
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
        +createTask(String taskType, ApiClient, ...) AbstractTask
        +createConnectionTask(...) AbstractTask
        +createUpdateTask(...) AbstractTask
    }
    
    class BindingConfiguration {
        <<binding config>>
        +static getConfiguration(ConfigurationAdmin) BindingConfiguration
    }
    
    class ServerDiscoveryResult {
        +String id
        +String name
        +String uri
        +String version
    }
    
    class ErrorEventListener {
        <<interface>>
        +onErrorEvent(ErrorEvent)
    }
    
    class ErrorEventBus {
        +addListener(ErrorEventListener)
        +removeListener(ErrorEventListener)
        +publishEvent(ErrorEvent)
    }
    
    class ContextualExceptionHandler {
        +handle(Exception)
    }
```

## Key Components

1. **HandlerFactory**: Creates thing handlers for the binding.
2. **ServerHandler**: Main bridge handler for Jellyfin servers that orchestrates server communication and state management.
   Implements `ErrorEventListener` for event-driven error handling.
3. **TaskManager**: Stateless utility class that manages task lifecycle operations based on server state transitions.
4. **ApiClientFactory**: Creates API client instances for different API versions.
5. **ApiClient**: Handles communication with the Jellyfin server and manages authentication.
6. **ServerDiscoveryService**: Discovers Jellyfin servers on the network using UDP broadcasts.
7. **TaskFactory**: Creates various task instances used for server communication.
8. **AbstractTask**: Base class for all tasks that can be scheduled for execution.
9. **BindingConfiguration**: Contains configuration settings for the binding.
10. **ErrorEventBus**: Central event bus for error events using the Observer pattern, providing loose coupling between error producers and consumers.
11. **ContextualExceptionHandler**: Intelligent exception handler that categorizes exceptions by type and severity, then publishes events to the error event bus.
12. **ErrorEvent**: Event object that encapsulates exception information with context, type, and severity for better error handling.

## Architecture Overview

The binding follows a state-driven architecture with event-driven error handling where:

- **ServerHandler** manages the overall server connection lifecycle and delegates task management to the **TaskManager** utility
- **TaskManager** provides stateless operations for starting, stopping, and transitioning tasks based on **ServerState**
- **ServerState** enum defines which tasks should be active for each server state
- Tasks are created by **TaskFactory** and extend **AbstractTask** for scheduled execution
- **ApiClient** provides the communication layer with version-specific implementations
- **Error Handling** uses the Observer pattern:
  - **ContextualExceptionHandler** categorizes exceptions and publishes **ErrorEvent** objects
  - **ErrorEventBus** manages event distribution using thread-safe operations
  - **ServerHandler** implements **ErrorEventListener** to react to error events with appropriate state changes
  - No circular dependencies: Tasks → ContextualExceptionHandler → ErrorEventBus → ServerHandler

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
