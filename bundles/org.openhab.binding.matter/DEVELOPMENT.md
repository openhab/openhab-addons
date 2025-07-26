# Development Guide

This document describes how to set up your development environment and contribute to the project. The project consists of three main components:

1. Code Generation Tool
1. Matter.js WebSocket Service
1. openHAB Java Add-on

## General Build Requirements

- Java 17 or higher
- Node.js 18 or higher
- npm 9 or higher

## Building the Project

The project uses Maven as the primary build tool. To build all components:

```bash
mvn clean install
```

### Maven Build Process

The `mvn clean install` command executes several steps to build the WebSocket server and package everything together.
By default, this will not regenerate the matter cluster classes. To regenerate the cluster classes, use the `code-gen` profile:

```bash
mvn clean install -P code-gen
```

The following maven steps are executed:

1. **Clean Phase**
   - Without `-P code-gen`: Cleans only standard build output directories
   - With `-P code-gen`: Additionally cleans:
     - The `code-gen/out` directory
     - Generated Java classes in `src/main/java/org/openhab/binding/matter/internal/client/dto/cluster/gen`

1. **Generate Resources Phase**
   - Sets up Node.js and npm environment
   - Installs Matter server dependencies
   - Builds Matter server using webpack
   - Copies built `matter.js` to the appropriate resource directory for inclusion in the final jar

1. **Generate Sources Phase** (only with `-P code-gen`)
   - Runs code generation tool:
     1. Installs code-gen npm dependencies
     1. Runs the main 'app.ts' which uses custom handlebars template for code generation from Matter.js SDK definitions
     1. Moves generated Java classes to `src/main/java/.../internal/client/dto/cluster/gen`
     1. Cleans up temporary output directories

1. **Process Sources Phase** (only with `-P code-gen`)
   - Formats generated code using spotless

1. **Compile and Package**
   - Compiles Java sources

## Project Components

### 1. Code Generation Tool (`code-gen/`)

#### Purpose

The code generation tool is responsible for creating Java classes from the Matter.js SDK definitions. It processes the Matter protocol specifications and generates type-safe Java code that represents Matter clusters, attributes, and commands.

#### Architecture

- Located in the `code-gen/` directory
- Uses TypeScript for code generation logic (see `code-gen/app.ts`)
- Utilizes Handlebars templates for Java code generation (see `code-gen/templates`)
- Processes Matter.js SDK definitions directly from the matter.js SDK ( `Matter.children....`)

#### Building and Running

```bash
cd code-gen
npm install
npm run build
```

The generated Java classes will be placed in the openHAB addon's source directory.

### 2. Matter.js WebSocket Service (`matter-server/`)

#### Purpose

The Matter.js WebSocket service acts as a bridge between the openHAB binding and the Matter.js SDK. It provides a WebSocket interface that allows the Java binding to communicate with Matter devices through the Matter.js protocol implementation.

#### Architecture

- WebSocket server implementation in TypeScript
- Two main operation modes:
  - Client Controller: Acts as a Matter controller allowing communication with Matter devices
  - Bridge Controller: Acts as a Matter bridge node, exposing non matter devices (openHAB items) as endpoints for 3rd party clients to control.  This will bind on the default matter port by default.
  - Modes are independent of each other and create their own matter instances
- Real-time event system for device state updates

#### WebSocket Protocol

##### Connection Establishment

1. Client connects to WebSocket server with query parameters:
   - `service`: Either 'bridge' or 'client'
   - For client mode: `controllerName` parameter required
   - For bridge mode: `uniqueId` parameter required
1. Server validates connection parameters and initializes appropriate controller
1. Server sends 'ready' event when controller is initialized

##### Message Types

###### Requests

```typescript
{
    id: string;          // Unique request identifier which will be used in the response to track messages
    namespace: string;   // Command RPC namespace
    function: string;    // Function to execute in the namespace
    args?: any[];       // Optional function arguments
}
```

###### Responses

```typescript
{
    type: string;    // "response"
    message: {
        type: string;    // "result", "resultError", "resultSuccess"
        id: string;      // Matching ID from the original request
        result?: any;    // Operation result (if successful)
        error?: string;  // Error message (if failed)
    }
}
```

###### Events

```typescript
{
    type: string;    // "event"
    message: {
        type: string;    // Event type (see below)
        data?: any;      // Event data (string, number, boolean, map, etc....)
    }
}
```

##### Event Types

- `attributeChanged`: Device attribute value updates
- `eventTriggered`: Device-triggered events
- `nodeStateInformation`: Device connection state changes
- `nodeData`: Device data updates (cluster and attributes)
- `bridgeEvent`: Bridge-related events

##### Node States

- `Connected`: Node is connected and ready for querying of data
- `Disconnected`: Node is disconnected
- `Reconnecting`: Node is attempting to reconnect (but still offline)
- `WaitingForDeviceDiscovery`: Waiting for MDNS announcement (so still offline)
- `StructureChanged`: Node structure has been modified
- `Decommissioned`: Node has been decommissioned

#### Components

- `app.ts`: Main server implementation and WebSocket handling
  - Manages WebSocket connections
  - Handles message routing
  - Implements connection lifecycle
- `Controller.ts`: Base abstract controller functionality (implemented by client and bridge)
  - Common controller operations
  - Message handling framework
  - Handles looking up namespaces and functions for remote commands
- `client/`: Matter controller functionality
- `bridge/`: Matter bridge functionality
- `util/`: Shared utilities and helper functions

#### Building and Running

```bash
cd matter-server
npm install
npm run webpack
```

Server configuration options:

- `--port`: WebSocket server port (default: 8888)
- `--host`: Server host address

#### Error Handling

- Connection errors trigger immediate WebSocket closure
- Operation errors are returned in response messages
- Node state changes are communicated via events
- Automatic reconnection for temporary disconnections
- Parent process monitoring for clean shutdown

### 3. openHAB Matter Binding (`src/`)

#### Purpose

The openHAB Matter binding provides integration between openHAB and Matter devices. It implements the openHAB binding framework and communicates with Matter devices through the Matter.js WebSocket service.

#### Architecture

##### Shared Client Code

- Location: `src/main/java/.../internal/client/`
- Handles WebSocket communication with Matter server
- Implements message serialization/deserialization
- Manages connection lifecycle

##### Controller Code

- Location: `src/main/java/.../internal/controller/`
- Implements Matter device control logic
- Manages device state and commands through "converter" classes

##### Bridge Code

- Location: `src/main/java/.../internal/bridge/`
- Implements openHAB Matter bridge functionality
- Uses Item metadata tags to identity Items to expose (similar to homekit, alexa, ga, etc....)
- Handles device pairing and commissioning of 3rd party controllers (Amazon, Apple, Google, etc.... )

#### Building

```bash
mvn clean install
```
