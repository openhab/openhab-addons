# Jellyfin Binding for openHAB - AI Development Guide

## Project Overview
This is an **openHAB binding** for Jellyfin media servers, built as an OSGi bundle in Java 21 following openHAB's addon architecture patterns. The binding enables home automation control of Jellyfin servers and their connected clients through openHAB's Thing/Channel model.

## Mandatory Development Rules

### File Organization Requirement
**CRITICAL**: Every class, interface, and enum MUST be created in its own dedicated file. This is a non-negotiable requirement for:
- **Classes**: Each public class gets its own `.java` file matching the class name
- **Interfaces**: Each interface gets its own `.java` file matching the interface name  
- **Enums**: Each enum gets its own `.java` file matching the enum name
- **Exception Classes**: Each custom exception gets its own `.java` file
- **Abstract Classes**: Each abstract class gets its own `.java` file

This rule ensures:
- Better code organization and maintainability
- Easier navigation and IDE support
- Cleaner version control and merge conflicts
- Standard Java best practices compliance

### Class Diagram Maintenance Requirement
**CRITICAL**: After any structural changes to classes, interfaces, or architecture, the class diagram in `CONTRIBUTION.md` MUST be updated. This is a mandatory step for:
- **Adding new classes/interfaces**: Include them in the diagram with proper relationships
- **Removing classes/interfaces**: Remove them from the diagram and update relationships
- **Changing relationships**: Update dependency arrows, implementations, and interactions
- **Modifying method signatures**: Update key public methods shown in the diagram
- **Architectural changes**: Reflect pattern changes (static → instance, factory → DI, etc.)

This requirement ensures:
- Documentation stays synchronized with actual code structure
- Architecture decisions are visible and understandable
- Code reviews can validate both implementation and documentation consistency
- Future developers have accurate architectural guidance

**Verification Process**: 
1. Make code changes
2. Update class diagram in `CONTRIBUTION.md`
3. Verify diagram accuracy against actual code structure
4. Test build to ensure no compilation errors

### Git Operations and AI Agent Restrictions
**CRITICAL**: AI agents are **PROHIBITED** from performing commit operations. This is a strict security and code governance requirement:

#### Forbidden GitKraken MCP Operations
- **NEVER use `mcp_gitkraken_git_add_or_commit`** with action "commit" - AI agents must not commit code
- **Analysis and staging only**: AI agents may use git tools for analysis (status, diff, log) but NOT for committing changes
- **Human oversight required**: All commits must be performed by human developers with proper review

#### Allowed Git Operations for AI Agents
- **Status checking**: `mcp_gitkraken_git_status` to understand current state
- **Diff analysis**: `mcp_gitkraken_git_log_or_diff` to analyze changes
- **Branch operations**: `mcp_gitkraken_git_branch` for branch management (list only, not create)
- **File staging**: `mcp_gitkraken_git_add_or_commit` with action "add" for staging files (preparation only)

#### Commit Message Generation Only
- AI agents may **suggest commit messages** based on analyzed changes
- AI agents may **prepare staging** for human review
- **Human developers must execute** the actual commit operation
- This ensures proper code review, accountability, and governance

**Rationale**: Automated commits bypass essential human oversight, code review processes, and accountability measures required for production code changes.

### Java 21 Modern Development Standards
**MANDATORY**: All code MUST leverage Java 21 language features and best practices for maintainability, readability, and performance:

#### Stream API and Modern Collection Operations
- **Use `var` keyword** for local variable type inference where it improves readability
- **Prefer `List.of()`, `Set.of()`, `Map.of()`** over mutable collection constructors for immutable collections
- **Use `Collection.toList()`** instead of `collect(Collectors.toList())` for simple conversions
- **Use `Set.copyOf()`** instead of `new HashSet<>(collection)` for defensive copying
- **Leverage method references** (`::methodName`) over lambda expressions where applicable
- **Use `forEach()` with method references** instead of traditional for-loops where appropriate

#### Modern Control Flow and Pattern Matching
- **Extract complex filtering logic** into separate predicate methods with descriptive names
- **Use switch expressions** (not statements) for cleaner conditional logic where applicable
- **Prefer early returns** and guard clauses to reduce nesting levels
- **Use pattern matching** for instanceof checks where available

#### Modern null Safety and Optional Handling
- **Use `Optional.ofNullable()`** for null-safe operations
- **Chain Optional operations** (`map()`, `filter()`, `orElse()`) instead of explicit null checks
- **Use null-safe operators** and modern null-checking patterns
- **Avoid unnecessary null checks** when using modern collection factory methods

#### Code Organization and Readability
- **Keep methods focused** - extract complex operations into well-named helper methods
- **Use descriptive variable names** with `var` for improved readability
- **Prefer composition** over inheritance where applicable
- **Use record classes** for simple data transfer objects where beneficial
- **Minimize mutable state** and prefer immutable data structures

#### Performance and Memory Efficiency
- **Use parallel streams** only when beneficial (large datasets, CPU-intensive operations)
- **Prefer lazy evaluation** with streams over eager collection operations
- **Use `StringBuilder` or text blocks** for complex string construction
- **Minimize object allocations** in hot paths using modern collection operations

#### File Size and Code Organization Requirements
- **Maximum file size**: Individual Java files MUST NOT exceed **450 lines** including comments and whitespace
- **Utility class extraction**: When a class approaches the 450-line limit, extract cohesive utility classes following Single Responsibility Principle
- **Recommended extraction targets**:
  - Complex business logic into dedicated service/utility classes
  - Configuration management into `ConfigurationManager` utility
  - User/data processing into specialized manager classes
  - State management logic into `StateManager` utilities
- **Extraction guidelines**:
  - Each extracted class should have a single, clear responsibility
  - Use dependency injection to provide utility classes to main handlers
  - Preserve all existing functionality during extraction
  - Maintain comprehensive error handling and logging
  - Follow existing package structure: `internal.handler.util.*` for utility classes

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

## Documentation Consistency

### Contribution Guide Maintenance
- **Always verify** that class diagrams in `CONTRIBUTION.md` reflect actual code structure after making changes
- **Update architecture diagrams** when adding, removing, or renaming classes/interfaces
- **Focus on big picture**: Diagrams should show architectural concepts, not implementation details
- **Avoid clutter**: Only include classes that represent core patterns or main architectural components
- **Class naming consistency**: Ensure all references use current class names, not old/deprecated ones

## Critical Development Patterns

### Handler Implementation
- All handlers extend `BaseBridgeHandler` (servers) or `BaseThingHandler` (clients)
- Use `@NonNullByDefault` annotation consistently across all classes
- Implement async task patterns via `TaskManager` and `AbstractTask` subclasses in `handler/tasks/`
- Bridge handlers must manage child discovery services registration/unregistration

### Event-Driven Error Handling
- **Observer Pattern**: Use `ErrorEventBus` with `ErrorEventListener` for loose coupling between error producers and consumers
- **Strategy Pattern**: `ContextualExceptionHandler` intelligently categorizes exceptions by type and severity
- **Error Events**: All errors are represented as `ErrorEvent` objects with context, type, and severity information
- **No Circular Dependencies**: Tasks → ContextualExceptionHandler → ErrorEventBus → ServerHandler (one-way flow)
- **Thread Safety**: `ErrorEventBus` uses `CopyOnWriteArrayList` for concurrent access

### Error Classification
- **Error Types**: CONNECTION_ERROR, AUTHENTICATION_ERROR, API_ERROR, CONFIGURATION_ERROR, UNKNOWN_ERROR
- **Error Severities**: WARNING (log only), RECOVERABLE (error state, allow recovery), FATAL (error state, restart required)
- **Context-Aware**: Each task gets its own exception handler with specific context for better debugging

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
- Tasks handle their own error recovery via event-driven exception handlers
- Each task receives a context-specific `ContextualExceptionHandler`

## Build & Test Workflow

### Build System
- **Maven**: Standard openHAB addon build via `mvn clean install`
- **Build Verification**: Always use Maven commands for checking modifications and compilation
- **Java Version**: Requires Java 21 (configured in build script)
- **Dependencies**: Jackson 2.19.0 for JSON processing, standard openHAB core dependencies
- **Note**: Do not use VS Code tasks for build verification - use Maven directly

### Testing Patterns
- Unit tests in `src/test/java/` focus on UUID deserialization and API client functionality
- Test files: `ApiClientUuidTest`, `UuidDeserializerTest`, `UuidDeserializerIntegrationTest`
- Use JUnit 5 with openHAB test fixtures

### Code Generation
- Jellyfin API classes are **generated** from OpenAPI specs (see `logs/endpoints/` for discovered endpoints)
- **Never edit** files in `api/generated/` directly
- **Ignore files with `._ suffix** - these are deprecated/backup files and should not be modified
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
- **Event-Driven Architecture**: Use `ErrorEventBus` and `ErrorEventListener` pattern instead of direct coupling
- **Exception Classification**: `ContextualExceptionHandler` automatically categorizes exceptions by type and severity
- **Context Preservation**: Each exception handler includes context information for better debugging
- **Strategy Pattern**: Different error types and severities trigger different response strategies in listeners

### Media Search Syntax
- Supports prefixed search: `<type:movie>`, `<season:1><episode:1>` 
- Default behavior: search by name starting with given text
- Implementation in server handler's channel command processing

## Architecture Diagram Guidelines

### When Making Code Changes
- **Always check** `CONTRIBUTION.md` class diagram consistency after structural changes
- **Update diagrams** immediately when adding, removing, or renaming classes/interfaces
- **Verify relationships** match actual code dependencies and patterns
- **Remove outdated references** to deleted or renamed classes

### Diagram Design Principles
- **Big Picture Focus**: Show architectural concepts and patterns, not implementation details
- **Core Components Only**: Include classes that represent main architectural components
- **Pattern Visualization**: Emphasize design patterns (Observer, Strategy, Factory, etc.)
- **Avoid Implementation Details**: No private methods, constants, or internal data structures
- **Relationship Clarity**: Use clear, meaningful relationship labels (owns, creates, uses, etc.)

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