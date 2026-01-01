# Utility Classes Architecture

This page documents the extracted utility classes that handle specific
responsibilities in the Jellyfin binding.

```mermaid
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
        +calculateChannelStates(SessionInfoDto$) Map~String, State~
    }

    %% Records for immutable data transfer (details omitted, see Record Details section)
    class UserChangeResult {
        <<record>>
    }
    class ConfigurationUpdate {
        <<record>>
    }
    class StateAnalysis {
        <<record>>
    }

    classStyle ServerStateManager fill:#ffb366,stroke:#ff8800
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
