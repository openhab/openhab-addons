# Error Handling Architecture

This page documents the event-driven error handling system in the Jellyfin binding.

```mermaid
classDiagram
    %% Error handling (Observer pattern)
    AbstractTask --> ContextualExceptionHandler : uses
    ContextualExceptionHandler --> ErrorEventBus : publishes to
    ErrorEventBus --> ErrorEventListener : notifies
    ErrorEventListener <|.. ServerHandler

    class ContextualExceptionHandler {
        +handle(Exception)
        -determineErrorType(Exception) ErrorType
        -determineErrorSeverity(Exception) ErrorSeverity
    }

    class ErrorEventBus {
        +addListener(ErrorEventListener)
        +removeListener(ErrorEventListener)
        +publishEvent(ErrorEvent)
    }

    class ErrorEventListener {
        <<interface>>
        +onErrorEvent(ErrorEvent)
    }

    class ErrorEvent {
        +getContext() String
        +getException() Exception
        +getType() ErrorType
        +getSeverity() ErrorSeverity
    }

    %% Color scheme for external libraries
    classDef jettyWebSocket fill:#99ccff,stroke:#6699cc,color:#000
    class AbstractTask jettyWebSocket
```

## Summary

The error handling architecture uses the Observer pattern to decouple error reporting
from error handling. Tasks report errors through the `ContextualExceptionHandler`,
which publishes events to the `ErrorEventBus`. The `ServerHandler` subscribes to
these events and handles them appropriately based on severity and type.

See the [architecture overview](../architecture.md) for context.
