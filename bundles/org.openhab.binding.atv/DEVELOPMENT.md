# Development

Notes for building and working on the Apple TV binding.

## Overview

The binding has two parts:

- **Client** (`src/main/java/org/openhab/binding/atv/internal/client/`) — a self-contained
  Java library that talks to Apple TV and AirPlay devices over AirPlay, Companion, MRP, and RAOP.
  The initial version was ported from [pyatv](https://github.com/postlund/pyatv) 0.18.0; the
  MRP protobuf definitions are copied from it. pyatv is MIT licensed, and that license and
  attribution are included in the binding's `NOTICE` file. The client now lives and evolves
  here in the binding.
- **Binding** (`src/main/java/org/openhab/binding/atv/internal/`)
  - `discovery/` finds devices on the network using mDNS.
  - `handler/` connects to a device, handles pairing, and maps its features to channels.
  - `config/` and `AtvBindingConstants` hold configuration and shared constants.

## Build

From the `openhab-addons` root:

```bash
mvn clean install -pl :org.openhab.binding.atv
```

Add `-DskipChecks` to skip static analysis while iterating.

## Device identity

A device is identified by its MAC (`macAddress`), which stays the same when the IP changes.
Discovery keeps the `host` up to date automatically; set it by hand only for a static IP or a
device on a different subnet.

## MRP protobuf

The MRP protocol uses protobuf messages defined in `src/main/proto/`. The generated Java is
committed under `src/gen/java/`, which is added as a separate source root. That directory is
not scanned by the static-analysis tools (Checkstyle, PMD, SpotBugs), so the generated code
needs no null annotations, Javadoc, or author tags — it only carries the EPL license header.
A normal build needs no code generation.

Regenerate it only after changing a `.proto` file:

```bash
mvn -Pcode-gen process-sources -pl :org.openhab.binding.atv
```

The `code-gen` profile runs `protoc` (downloading it online the first time), writes the sources
into `src/gen/java`, and then runs the license plugin's `format` goal to stamp the EPL header
that `protoc` does not emit.
