# Discovery and API Architecture

This page documents the discovery services and API communication in the Jellyfin binding.

```mermaid
classDiagram
    %% Discovery and API components
    HandlerFactory --> ApiClientFactory : uses
    ApiClientFactory ..> ApiClient : creates
    ServerDiscoveryService --> ServerDiscovery : uses
    ServerDiscovery ..> ServerDiscoveryResult : creates
    
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
        +discoverServers() List~ServerDiscoveryResult~
    }
    
    class ServerDiscoveryResult {
        +String id
        +String name
        +String uri
        +String version
    }
```

## Summary

Discovery and API communication are handled by dedicated services and factories.
See the [architecture overview](../architecture.md) for context.
