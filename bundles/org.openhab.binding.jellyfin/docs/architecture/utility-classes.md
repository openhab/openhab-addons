# Utility Classes Architecture

This page documents the extracted utility classes that handle specific
responsibilities in the Jellyfin binding.

```mermaid
%%{init: { 'themeVariables': { 'fontSize': '14px' }, 'class': { 'useMaxWidth': false } } }%%
classDiagram
    %% Utility classes for separation of concerns
    ServerHandler --> UserManager : uses
    ServerHandler --> ConfigurationManager : uses
    ServerHandler --> ServerStateManager : uses
    ClientHandler --> ClientStateUpdater : uses

    class UserManager {
        +processUsersList(List, List) UserChangeResult
    }

    class ConfigurationManager {
        +analyze~T~(ConfigurationExtractor~T~, T, Configuration) ConfigurationUpdate
    }

    class ConfigurationExtractor~T~ {
        <<interface>>
    }

    ConfigurationManager --> ConfigurationExtractor : uses

    class ServerStateManager {
        +analyzeServerState(ServerState, Configuration, Thing) StateAnalysis
        +isValidStateTransition(ServerState, ServerState) boolean
        +getStateDescription(ServerState) String
    }

    class ClientStateUpdater {
        +calculateChannelStates(SessionInfoDto) Map
    }

    %% Records for immutable data transfer (stubs; see Record Details below)
    class UserChangeResult {
        <<record>>
    }
    class ConfigurationUpdate {
        <<record>>
    }
    class StateAnalysis {
        <<record>>
    }
```

## Summary

Utility classes provide focused, testable logic for distinct responsibilities:

- **UserManager**: User list processing and change tracking
- **ConfigurationManager**: Configuration extraction and update analysis (Strategy pattern)
- **ServerStateManager**: Server state transitions and validation
- **ClientStateUpdater**: Jellyfin session state calculation into openHAB channel states

Each utility is designed for independent testing and reusability across handlers
and services.

### Detailed Documentation

- **Configuration Strategy Pattern**: See [Configuration Management Architecture](configuration-management.md)
- **State Calculation Flow**: See [State Calculation Architecture](state-calculation.md)

See the [architecture overview](../architecture.md) for context.

## Record Details

The following records are used for immutable data transfer and are referenced by utility classes and handlers. The class diagram below shows fields for each record.

```mermaid
%%{init: { 'themeVariables': { 'fontSize': '14px' }, 'class': { 'useMaxWidth': false } } }%%
classDiagram
    class UserChangeResult {
        <<record>>
        +List~String~ currentUserIds
        +List~String~ addedUserIds
        +List~String~ removedUserIds
        +List~UserDto~ enabledVisibleUsers
    }
    class ConfigurationUpdate {
        <<record>>
        +String hostname
        +int port
        +boolean ssl
        +String path
        +boolean hasChanges
        +applyTo(Configuration) void
    }
    class StateAnalysis {
        <<record>>
        +ServerState recommendedState
        +String reason
        +URI serverUri
    }

    classDef openhab fill:#ff8c1a,stroke:#333,stroke-width:1px,color:#fff;
    classDef apiGen fill:#2ca02c,stroke:#333,stroke-width:1px,color:#fff;
    classDef internal fill:#ffffff,stroke:#333,stroke-width:1px,color:#000;

    class UserChangeResult internal
    class ConfigurationUpdate internal
    class StateAnalysis internal
```

**Descriptions:**

- **UserChangeResult**: Returned by user synchronization; contains lists of current, added, and removed user IDs and a list of enabled visible user DTOs.
- **ConfigurationUpdate**: Produced by configuration analysis; contains fields describing changes and an `applyTo(Configuration)` helper.
- **StateAnalysis**: Used by `ServerStateManager` to represent a recommended `ServerState`, the reason for the recommendation, and the server URI used.
