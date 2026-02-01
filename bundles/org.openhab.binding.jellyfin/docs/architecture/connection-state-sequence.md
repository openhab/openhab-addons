# Connection → CONNECTED → ServerSync / Discovery Sequence 🔧

This diagram shows the interaction flow from initial connection (SystemInfo retrieval) to the handler transitioning to the CONNECTED state and the resulting background tasks (WebSocket vs ServerSync polling and Discovery). It also includes the WebSocket fallback path where ServerSync (polling) is started if the WebSocket fails.

> Diagram source: `connection-state-sequence.mmd` (Mermaid sequence diagram)

```mermaid
%%{init: { 'themeVariables': { 'fontSize': '14px' }, 'sequence': { 'actorMargin': 20 } } }%%
sequenceDiagram
autonumber
participant Conn as ConnectionTask
participant Handler as ServerHandler
participant TM as TaskManager
participant WS as WebSocketTask
participant Sync as ServerSyncTask
participant Disc as DiscoveryTask
participant HTTP as Jellyfin /Users
participant UM as UserManager
participant SM as SessionManager
participant CDS as ClientDiscoveryService

%% Connection and state transition
Conn->>+Handler: retrieve SystemInfo
Handler->>Handler: setState(CONNECTED)
Handler->>Handler: set Thing ONLINE
Handler->>TM: processStateChange(CONNECTED)

alt WebSocket available
    TM->>+WS: start WebSocketTask (realtime)
    TM->>+Disc: start DiscoveryTask
else No WebSocket
    TM->>+Sync: start ServerSyncTask (polling)
    TM->>+Disc: start DiscoveryTask
end

loop ServerSync (every 60s)
    Sync->>+HTTP: GET /Users
    HTTP-->>-Sync: List<UserDto>
    Sync->>Handler: usersHandler(users)
    Handler->>UM: processUsersList(users)
    UM-->>Handler: userChangeResult
    Handler->>SM: updateSessions(newSessions)
    Handler->>CDS: discoverClients()
    CDS->>CDS: find and publish client discovery results
end

%% WebSocket fallback path
WS-->>-Handler: connectionFailure
Handler->>Handler: handleWebSocketFallback()
Handler->>TM: processStateChange(CONNECTED)  %% ensures ServerSync started
Handler->>+Sync: start ServerSyncTask (fallback)
```

---

If you'd like, I can also add a short reference link to `docs/architecture/task-management.md` or `core-handler.md` to make the diagram easier to find. Would you like that? (Yes/No)
