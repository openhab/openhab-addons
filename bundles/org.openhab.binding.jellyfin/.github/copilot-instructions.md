# Jellyfin Binding for openHAB - AI Development Guide

## Project Overview
This is an **openHAB binding** for Jellyfin media servers, built as an OSGi bundle in Java 21 following openHAB's addon architecture patterns. The binding enables home automation control of Jellyfin servers and their connected clients through openHAB's Thing/Channel model.

## Architecture Essentials

### Core Components Structure
- **Bridge Pattern**: `JellyfinServerHandler` acts as a bridge for discovering and managing multiple `JellyfinClientHandler` instances
- **Handler Factory**: `JellyfinHandlerFactory` extends `BaseThingHandlerFactory` - the standard openHAB pattern for creating Thing handlers
- **Discovery Services**: Auto-discover Jellyfin servers via UDP broadcast (`JellyfinServerDiscoveryService`) and clients via server API (`JellyfinClientDiscoveryService`)
- **API Layer**: Generated Jellyfin API clients in `internal/api/generated/` with custom wrapper in `ApiClient.java` for UUID handling compatibility

### Thing Type Hierarchy
```
jellyfin:server (Bridge) -> jellyfin:client (Thing)
```
- Server bridges authenticate and manage API connections
- Client things represent controllable devices (Android TV, web clients, etc.)
- All Thing types defined in `Constants.java` as `ThingTypeUID` constants

### Configuration Files Locations
- Thing definitions: `src/main/resources/OH-INF/thing/thing-types.xml`
- Channel definitions: `src/main/resources/OH-INF/thing/thing-types.xml` 
- Binding config: `src/main/resources/OH-INF/addon/addon.xml`
- i18n properties: `src/main/resources/OH-INF/i18n/`

## Critical Development Patterns

### Handler Implementation
- All handlers extend `BaseBridgeHandler` (servers) or `BaseThingHandler` (clients)
- Use `@NonNullByDefault` annotation consistently across all classes
- Implement async task patterns via `TaskManager` and `AbstractTask` subclasses in `handler/tasks/`
- Bridge handlers must manage child discovery services registration/unregistration

### API Client Usage
- **UUID Compatibility**: Jellyfin servers return inconsistent UUID formats - custom `UuidDeserializer` handles this in `ApiClient.java`
- **Version Support**: Generated API supports both current (10.10.7+) and legacy versions - check `api/generated/current/` vs `api/generated/legacy/`
- **HTTP Client**: Use injected `HttpClientFactory` for HTTP connections, not direct `HttpClient` instantiation

### Discovery Service Pattern
```java
@Component(service = DiscoveryService.class, immediate = true)
public class JellyfinServerDiscoveryService extends AbstractDiscoveryService {
    // Manual trigger only - no background scanning
}
```

### Task Management
- Long-running operations use `TaskManager` with `AbstractTask` implementations
- Connection tasks, update polling, and API calls all follow this pattern
- Tasks handle their own error recovery and state management

## Build & Test Workflow

### Build System
- **Maven**: Standard openHAB addon build via `mvn clean install`
- **Quick Build**: Use provided VS Code task "Build" or `.vscode/scripts/build.sh`
- **Java Version**: Requires Java 21 (configured in build script)
- **Dependencies**: Jackson 2.19.0 for JSON processing, standard openHAB core dependencies

### Testing Patterns
- Unit tests in `src/test/java/` focus on UUID deserialization and API client functionality
- Test files: `ApiClientUuidTest`, `UuidDeserializerTest`, `UuidDeserializerIntegrationTest`
- Use JUnit 5 with openHAB test fixtures

### Code Generation
- Jellyfin API classes are **generated** from OpenAPI specs (see `logs/endpoints/` for discovered endpoints)
- **Never edit** files in `api/generated/` directly
- Custom logic goes in wrapper classes like `ApiClient.java` and `ApiClientFactory.java`

## Domain-Specific Conventions

### Channel Naming
- Use hyphenated lowercase: `playing-item-percentage`, `send-notification`
- Media control channels follow openHAB Player item conventions
- All channel IDs defined as constants in `Constants.java`

### Authentication Flow
- Server things require `userId` and `token` configuration
- Binding provides web UI at `/jellyfin/<server-thing-id>` for OAuth-like login assistance
- Authentication state managed in `ServerHandler` with connection tasks

### Error Handling
- Use `ExceptionHandler` and `ExceptionHandlerType` enum for consistent error categorization
- Bridge status drives child thing status - children go offline when bridge disconnects
- Failed API calls should not crash handlers - log and set appropriate Thing status

### Media Search Syntax
- Supports prefixed search: `<type:movie>`, `<season:1><episode:1>` 
- Default behavior: search by name starting with given text
- Implementation in server handler's channel command processing

## Key Files for Understanding
- `Constants.java` - All binding constants and channel definitions
- `JellyfinHandlerFactory.java` - Entry point and handler creation
- `ServerHandler.java` - Bridge logic and API management  
- `ApiClient.java` - Custom API wrapper with UUID fixes
- `TaskManager.java` - Async operation patterns
- `README.md` - User documentation and channel descriptions

## Integration Dependencies
- **openHAB Core**: Thing management, discovery services, channel types
- **Jellyfin API**: HTTP REST API with JSON responses
- **OSGi**: Bundle lifecycle and dependency injection via `@Component` annotations
- **Jackson**: JSON deserialization with custom UUID handling