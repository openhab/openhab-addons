# API Architecture

This page documents the API client and communication structure in the Jellyfin binding.

```mermaid
classDiagram
    HandlerFactory --> ApiClientFactory : uses
    ApiClientFactory ..> ApiClient : creates
    
    class ApiClientFactory {
        +createApiClient() ApiClient
    }
    
    class ApiClient {
        <<interface>>
        +authenticateWithToken(String)
        +updateBaseUri(String)
    }
```

## Summary

API communication is handled by a factory and client abstraction, supporting
version-specific implementations.
See the [architecture overview](../architecture.md) for context.
