# Client Discovery Implementation Plan

## 1. Overview

Background client discovery enables the binding to automatically detect Jellyfin clients at regular intervals, ensuring new clients are added to the openHAB inbox without manual scans.
Background client discovery enables the binding to automatically detect Jellyfin clients at
regular intervals, ensuring new clients are added to the openHAB inbox without manual scans

---

## 2. Objectives

- Periodically scan for Jellyfin clients (default: every 60 seconds)
- Add new clients to the inbox via `thingDiscovered()`
- Avoid duplicate discoveries
- Cleanly start/stop background discovery with handler lifecycle
- Ensure thread safety and resource management

---

## 3. Implementation Steps

### 3.1. Scheduling

- Use a scheduler (e.g., `ScheduledExecutorService`) to run discovery every 60 seconds.
- Start scheduling in `startBackgroundDiscovery()`.
- Cancel scheduling in `stopBackgroundDiscovery()`.

### 3.2. Discovery Logic

- On each interval, call the client discovery method.
- For each discovered client:
  - Generate a unique `ThingUID`
  - Create a `DiscoveryResult`
  - Call `thingDiscovered(result)`

### 3.3. Deduplication

- Track discovered clients (e.g., by ThingUID or deviceId).
- Only add new clients not already present in the inbox.

### 3.4. Error Handling

- Catch and log exceptions during discovery.
- Ensure scheduler continues after recoverable errors.

### 3.5. Lifecycle Integration

- Start background discovery when handler is activated.
- Stop background discovery when handler is disposed.

---

## 4. Architecture Diagram

```mermaid
classDiagram
    class ServerHandler {
        -Map~String, SessionInfoDto~ clients
        -ClientDiscoveryService discoveryService
        +updateClientList()
        +getClients() Map
        +getServices() Collection
    }
    
    class ClientDiscoveryService {
        -ServerHandler bridgeHandler
        +startScan()
        +discoverClients()
        -discoverClient(SessionInfoDto)
        -generateThingUID(String) ThingUID
    }
    
    class ClientListUpdater {
        +updateClients(ApiClient, Set, Map)~static~
    }
    
    class SessionInfoDto {
        +String id
        +String deviceId
        +UUID userId
        +String deviceName
        +String client
    }
    
    class DiscoveryService {
        <<interface>>
        +startScan()
    }
    
    class ThingHandlerService {
        <<interface>>
        +setThingHandler(ThingHandler)
    }
    
    ServerHandler --> ClientDiscoveryService : registers
    ServerHandler --> ClientListUpdater : uses
    ClientListUpdater --> SessionInfoDto : queries
    ClientDiscoveryService --|> DiscoveryService
    ClientDiscoveryService --|> ThingHandlerService
    ClientDiscoveryService --> ServerHandler : accesses
    ClientDiscoveryService --> SessionInfoDto : processes
    
    note for ClientDiscoveryService "Bridge-bound discovery\nextends AbstractThingHandlerDiscoveryService"
    note for SessionInfoDto "deviceId is stable identifier\nused for ThingUID generation"
```

## 5. Sequence Diagram

```mermaid
flowchart TD
    ServerHandler[ServerHandler Activated] --> StartDiscovery[Start Background Discovery]
    StartDiscovery --> Scheduler[Scheduler: every 60s]
    Scheduler --> DiscoverClients[Discover Clients]
    DiscoverClients --> ForEachClient[For each client]
    ForEachClient --> GenerateThingUID[Generate ThingUID]
    GenerateThingUID --> CheckDuplicates[Check for duplicates]
    CheckDuplicates -- New --> ThingDiscovered["thingDiscovered(result)"]
    CheckDuplicates -- Duplicate --> Skip[Skip]
    ThingDiscovered --> Inbox[Client appears in Inbox]
    StartDiscovery -.-> StopDiscovery[Stop Background Discovery]
    StopDiscovery -.-> Scheduler
```

---

**Version:** 1.1
**Last Updated:** 2025-11-14
**Last update:** GitHub Copilot
**Agent:** GitHub Copilot (GPT-4.1, User: pgfeller)
