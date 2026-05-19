# Shelly Binding — Developer Guide

The openHAB Shelly binding integrates Shelly smart home devices across three generations
(Gen1 REST/CoAP, Gen2/3/4 WebSocket RPC, and BLU Bluetooth) behind a single handler
abstraction. All three generations share the same channel model; only the transport layer
differs. New device models typically require only a few lines of configuration — no new
handler code.

---

## Table of Contents

1. [Community](#1-community)
1. [Architecture Overview](#2-architecture-overview)
1. [Key Conventions](#3-key-conventions)
1. [Extension Guide](#4-extension-guide)
1. [Package Structure](#5-package-structure)
1. [Handler Hierarchy](#6-handler-hierarchy)
1. [API Layer](#7-api-layer)
1. [Device Profiling](#8-device-profiling)
1. [Channel System](#9-channel-system)
1. [Update Flow](#10-update-flow)
1. [Configuration Classes](#11-configuration-classes)
1. [Data Transfer Objects (DTOs)](#12-data-transfer-objects-dtos)
1. [Discovery](#13-discovery)
1. [Manager UI](#14-manager-ui)
1. [Resources](#15-resources)

---

## 1. Community

Questions, bug reports, and feature discussions for the binding:
<https://community.openhab.org/t/shelly-binding>

Pending PRs that affect the Shelly binding:
<https://github.com/openhab/openhab-addons/pulls?q=is%3Apr+is%3Aopen+%5Bshelly%5D>

File bugs and feature requests here (search before opening a new issue):
<https://github.com/openhab/openhab-addons/issues?q=is%3Aissue+is%3Aopen+%5Bshelly%5D>

---

## 2. Architecture Overview

The binding supports three fundamentally different device generations behind a single
handler abstraction:

| Generation | Protocol | Key Devices | Update Mechanism |
|---|---|---|---|
| **Gen1** | REST/HTTP + CoAP (CoIoT) | Shelly1, Shelly2, Plug, EM, Sensors | CoIoT push or HTTP polling |
| **Gen2/3/4** | JSON-RPC over WebSocket + HTTP | Shelly Plus, Shelly Pro | WebSocket push or HTTP polling |
| **BLU** | Bluetooth (via Gen2 gateway) | BLU Button, BLU H&T, BLU DW | BLE advertisement relayed via gateway |

All three generations share the same handler layer. `ShellyApiInterface` is the seam that
separates handler logic from transport:

```text
ShellyHandlerFactory
  └── ShellyBaseHandler (abstract)
        ├── ShellyRelayHandler
        ├── ShellyLightHandler
        ├── ShellyBluHandler
        └── ShellyProtectedHandler
              │
              │ ShellyApiInterface
              │
        ┌─────┴──────────────────────┐
        │                            │
   Shelly1HttpApi              Shelly2ApiRpc
   (Gen1 REST/CoIoT)           (Gen2 RPC/WS)
                                     │
                                ShellyBluApi
                                (BLU/BT relay)
```

Most channels are not statically defined in XML. They are created dynamically after the
first successful device status response, based on `ShellyDeviceProfile` populated from
`/settings` and `/status`. This allows a single thing type to handle device variants
without separate XML definitions.

---

## 3. Key Conventions

**Null safety:** `@NonNullByDefault` applies package-wide. Fields that may be `null`
must be annotated `@Nullable`. DTO fields are boxed types (`Boolean` / `Double` /
`Integer`) so that absent JSON fields deserialize to `null` rather than a default.

**Immutability:** Runtime configuration objects (`ShellyApiConfiguration`,
`ShellyBindingRuntimeConfig`) are immutable after construction. Mutation produces a new
instance via wither methods (`withHttpPort`). `ShellyBindingConfiguration` is a plain
mutable POJO populated by the OH framework — it is never shared across threads directly.
This design eliminates synchronization requirements where configs are shared.

**Exception handling:** All API errors surface as `ShellyApiException`. Handlers catch
this at the `refreshStatus()` and `handleCommand()` boundary and call
`handleApiException()`, which updates the thing status and schedules retries.

**Utility methods:** Use `ShellyUtils` for all type conversions. Never compare `null`
DTO fields directly — use `getBool(Boolean)`, `getDouble(Double)`, `getString(String)`.

**Channel updates:** Always go through `handler.updateChannel(group, channel, state)`,
not through `updateState()` directly. This routes through the deduplication cache.

**Logging:** Use `thingName` as the log prefix in all messages. `thingName` is set in
`ShellyBaseHandler` and forwarded to the API layer via `ShellyApiConfiguration.getRealm()`.

**Firmware version guards:** Use `ShellyVersionDTO.compare()` to gate features on
minimum firmware versions. The `extFeatures` flag on the profile signals `>= v1.10`
for Gen1 and guards channels / API calls that require newer firmware.

**Thing properties vs. channels:** Static device facts (MAC, hardware revision,
firmware version) belong in thing properties (`updateProperties()`). Values that change
at runtime belong in channels.

---

## 4. Extension Guide

### Quick Reference: Key Files by Task

| Task | Files to touch |
|---|---|
| New device model | `ShellyDevices.java`, `ShellyThingCreator.java`, `thing/shellyXxx.xml`, `shelly.properties` |
| New channel | `ShellyBindingConstants.java`, `ShellyChannelDefinitions.java`, `device.xml`, `shelly.properties`, `ShellyComponents.java` |
| New Gen1 API call | `ShellyApiInterface.java`, `Shelly1HttpApi.java`, `Shelly2ApiRpc.java` (no-op) |
| New Gen2 RPC call | `ShellyApiInterface.java`, `Shelly2ApiRpc.java`, `Shelly1HttpApi.java` (no-op) |
| New DTO field | `Shelly1ApiJsonDTO.java` or `Shelly2ApiJsonDTO.java` |
| New BLU device | All of the above plus `ShellyBluApi.java` and `shellyBlu.xml` |
| New thing config option | `ShellyThingConfiguration.java`, `ShellyApiConfiguration.java`, thing XML `<config-description>` |
| Binding-wide config option | `ShellyBindingConfiguration.java`, `ShellyBindingRuntimeConfig.java`, `binding.xml` |

---

### Adding a New Device / Thing

A "new device model" is one that fits an existing handler (e.g. a new Plus switch
variant). A completely new functional category may require additional steps.

**Step 1 — Add the hardware model constant** in `ShellyDevices.java`:

```java
public static final String SHELLYDT_PLUSMINI1PM = "SNSW-001P8EU";
```

**Step 2 — Add a `ThingTypeUID`** in `ShellyDevices.java`:

```java
public static final ThingTypeUID THING_TYPE_SHELLYPLUS1MINI_PM =
    new ThingTypeUID(BINDING_ID, "shellyplus1minipm");
```

**Step 3 — Add the UID to `SUPPORTED_THING_TYPES`** and the appropriate group set:

```java
SUPPORTED_THING_TYPES.add(THING_TYPE_SHELLYPLUS1MINI_PM);
GROUP_RELAY_THING_TYPES.add(THING_TYPE_SHELLYPLUS1MINI_PM);
```

**Step 4 — Add the discovery mapping** in `ShellyThingCreator.java`:

```java
THING_TYPE_BY_HW.put(SHELLYDT_PLUSMINI1PM, THING_TYPE_SHELLYPLUS1MINI_PM);
```

**Step 5 — Add a thing-type XML entry** in
`src/main/resources/OH-INF/thing/shellyGen2_relay.xml`:

```xml
<thing-type id="shellyplus1minipm">
    <label>Shelly Plus 1PM Mini</label>
    <description>Shelly Plus 1PM Mini relay with power metering</description>
    <config-description-ref uri="thing-type:shelly:shelly-gen2-config"/>
</thing-type>
```

**Step 6 — Add i18n labels** in `src/main/resources/OH-INF/i18n/shelly.properties`:

```properties
thing-type.shelly.shellyplus1minipm.label = Shelly Plus 1PM Mini
thing-type.shelly.shellyplus1minipm.description = Shelly Plus 1PM Mini relay with metering
```

> **Note:** Only edit `shelly.properties` (the English source). All other locale files
> (e.g. `shelly_de.properties`) are managed by the openHAB project via Crowdin and must
> **not** be edited by hand. Translators update them through the Crowdin web editor —
> see [Resources → Translations](#translations).

**Step 7 — Update `ShellyDeviceProfile.initFromThingType()`** if the new device
requires profile flags that cannot be inferred from `/settings` alone.

**Step 8 — Add JUnit test coverage** in
`ShellyThingCreatorTest.java`. Two parameterized test cases are required:

- Service-name lookup: add an `Arguments.of("shellyplugusg4-" + DEVICE_ID, "",
  THING_TYPE_SHELLYPLUGUSG4)` entry to
  `provideTestCasesForGetThingUIDReturnsThingUidAccordingToRuleset`.
- Model-ID lookup: add an `Arguments.of(SHELLYDT_PLUSPLUGUSG4, "",
  THING_TYPE_SHELLYPLUGUSG4)` entry to
  `provideTestCasesForGetThingUIDReturnsThingUidByDeviceType`.

Run `mvn test -pl :org.openhab.binding.shelly -Dtest=ShellyThingCreatorTest
-DskipChecks -Dmarkdownlint.skip=true` to verify.

**Step 9 — Add the device to `README.md`** in the supported-devices table and,
if its channel set differs from all existing types, add a new channel-reference
section.

---

### Adding a New Channel to an Existing Device

**Step 1 — Define a constant** in `ShellyBindingConstants.java`:

```java
public static final String CHANNEL_SENSOR_CO2 = "co2";
```

**Step 2 — Register the channel type** in `ShellyChannelDefinitions` constructor
(in the `@Activate` block):

```java
.add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_CO2, "sensorCO2", ITEMT_NUMBER))
```

The second-to-last argument is the `channelTypeId`, which must match an entry in
`device.xml` (or a system channel type like `system:indoor-temperature`).

**Step 3 — Add the channel type XML** in `src/main/resources/OH-INF/thing/device.xml`
if it is a new binding-specific type:

```xml
<channel-type id="sensorCO2">
    <item-type>Number:Dimensionless</item-type>
    <label>CO2 Level</label>
    <description>Carbon dioxide level in ppm</description>
    <state readOnly="true" pattern="%.0f ppm"/>
</channel-type>
```

**Step 4 — Add i18n strings** in `shelly.properties` (English only — see note in Step 6
above regarding locale files):

```properties
channel-type.shelly.sensorCO2.label = CO2 Level
channel-type.shelly.sensorCO2.description = Carbon dioxide level in ppm
```

**Step 5 — Conditionally add the channel** in the relevant `createXXXChannels()` method
in `ShellyChannelDefinitions.java`:

```java
// Only create the channel if the device actually reports CO2 data
addChannel(thing, newChannels, sdata.co2 != null,
    CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_CO2);
```

**Step 6 — Map the DTO field to the channel state** in `ShellyComponents.java`:

```java
// Inside updateSensors()
if (sdata.co2 != null) {
    handler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_CO2,
        toQuantityType(getDouble(sdata.co2), Units.PARTS_PER_MILLION));
}
```

**Step 7 — Add the DTO field** in `Shelly1ApiJsonDTO.ShellyStatusSensor` (Gen1) or
the appropriate Gen2 class:

```java
public static class ShellyStatusSensor {
    // ... existing fields ...
    @SerializedName("co2")
    public @Nullable Double co2;
}
```

---

### Adding a New BLU Device Type

BLU devices are read-only Bluetooth sensors. The binding receives their data as RPC
notifications from a Gen2 gateway.

**Step 1 — Follow steps 1–6** from "Adding a New Device Model", using
`src/main/resources/OH-INF/thing/shellyBlu.xml` as the XML target.

**Step 2 — Set profile flags** in `ShellyDeviceProfile.initFromThingType()`:

```java
} else if (THING_TYPE_BLUNEWSENSOR.equals(thingTypeUID)) {
    isBlu = true;
    isSensor = true;
    hasBattery = true;
    alwaysOn = false;
}
```

**Step 3 — Identify the BTHome object IDs** for the new device. Consult the device's
datasheet or the BTHome specification (<https://bthome.io/format/>) to find which
object IDs it broadcasts (e.g. 0x01=battery, 0x45=temperature, 0x3A=button event).
The gateway forwards these as named fields in the `BluScan.Data` RPC notification,
keyed by the decimal object ID. Then add a case in `ShellyBluApi.onMessage()` for
the new device's fields and call the appropriate `ShellyComponents.update*()` method
or update channels directly. See the BTHome object ID table in [§7 BLU](#blu--shellybluapi).

**Step 4 — Create channels** by adding or reusing entries in
`ShellyChannelDefinitions.createSensorChannels()`, gated on the new device's profile
flags.

---

### Adding Support for a New Sensor Field on an Existing Device

This is the most common case — just DTO + component mapping:

1. Add the field to the DTO inner class with `@Nullable`
1. Call `addChannel(...)` in `createSensorChannels()` conditional on the field
   being non-null
1. Map the value in `ShellyComponents.updateSensors()`
1. Add a channel constant in `ShellyBindingConstants`
1. Register the channel type in `CHANNEL_DEFINITIONS` and in `device.xml`
1. Add i18n strings

---

## 5. Package Structure

```text
internal/
├── ShellyBindingConstants.java  — All channel UIDs, property keys, timing constants
├── ShellyDevices.java           — ThingTypeUID definitions and device-type-to-UID mapping
├── ShellyHandlerFactory.java    — OSGi component; creates handler instances
│
├── api/                         — API abstraction layer
│   ├── ShellyApiInterface.java  — Contract for all API implementations
│   ├── ShellyDeviceProfile.java — Mutable device capability descriptor
│   └── ShellyApiException.java  — Unified exception for all API errors
│
├── api1/                        — Generation 1 implementation
│   ├── Shelly1HttpApi.java      — REST HTTP client (implements ShellyApiInterface)
│   ├── Shelly1CoapHandler.java  — CoIoT (CoAP) push listener per device
│   ├── Shelly1CoapServer.java   — Singleton CoAP server (port 5683)
│   └── Shelly1ApiJsonDTO.java   — All Gen1 JSON DTOs (static inner classes)
│
├── api2/                        — Generation 2 implementation
│   ├── Shelly2ApiRpc.java       — JSON-RPC client (implements ShellyApiInterface)
│   ├── Shelly2RpcSocket.java    — WebSocket lifecycle management
│   ├── ShellyBluApi.java        — BLU extension of Shelly2ApiRpc
│   └── Shelly2ApiJsonDTO.java   — All Gen2 JSON DTOs (static inner classes)
│
├── config/                      — Configuration POJOs
│   ├── ShellyThingConfiguration.java      — Thing-level config (IP, credentials, event flags)
│   ├── ShellyBindingConfiguration.java    — Binding-level POJO (raw OH/OSGi properties)
│   ├── ShellyBindingRuntimeConfig.java    — Runtime binding config (IP resolved, port set, immutable)
│   └── ShellyApiConfiguration.java        — API-layer config (immutable; derived from above)
│
├── discovery/                   — Device discovery
│   ├── ShellyMDNSDiscoveryParticipant.java — mDNS listener
│   ├── ShellyBasicDiscoveryService.java    — HTTP probe + result builder
│   └── ShellyThingCreator.java             — Maps device model strings to ThingTypeUIDs
│
├── handler/                     — Thing handlers and channel logic
│   ├── ShellyBaseHandler.java          — Core: polling, init, error state machine
│   ├── ShellyRelayHandler.java         — Relay/roller devices
│   ├── ShellyLightHandler.java         — Dimmers, RGBW2, bulbs
│   ├── ShellyBluHandler.java           — BLU sensor/button devices
│   ├── ShellyProtectedHandler.java     — Password-protected placeholder
│   ├── ShellyComponents.java           — DTO → channel state mapping (static helpers)
│   ├── ShellyChannelCache.java         — Deduplication cache for channel updates
│   ├── ShellyThingInterface.java       — Interface from handler to channel/profile layer
│   ├── ShellyManagerInterface.java     — Interface from manager UI to handler
│   └── ShellyThingTable.java           — OSGi service: registry of active handlers
│
├── manager/                     — Built-in web UI
│   ├── ShellyManagerServlet.java
│   ├── ShellyManager.java
│   ├── ShellyManagerPage.java
│   ├── ShellyManagerOverviewPage.java
│   ├── ShellyManagerActionPage.java
│   └── ShellyManagerOtaPage.java
│
├── provider/                    — Channel type and label provider
│   ├── ShellyChannelDefinitions.java   — Channel registry + dynamic channel builders
│   └── ShellyTranslationProvider.java  — i18n helper
│
└── util/                        — Utilities
    ├── ShellyUtils.java          — Type conversions, null-safe accessors
    ├── ShellyColorUtils.java     — HSB ↔ RGB conversions
    └── ShellyVersionDTO.java     — Firmware version comparison
```

---

## 6. Handler Hierarchy

### Class Hierarchy

```text
BaseThingHandler  (openHAB core)
  └── ShellyBaseHandler  (abstract — most logic lives here)
        ├── ShellyRelayHandler   — relay, roller, dimmer, meters
        ├── ShellyLightHandler   — color bulb, RGBW2, duo
        ├── ShellyBluHandler     — BLU battery sensors
        └── ShellyProtectedHandler — placeholder until credentials are entered
```

`ShellyHandlerFactory` selects the concrete handler based on the `ThingTypeUID`:

- Light thing types (`GROUP_LIGHT_THING_TYPES`) → `ShellyLightHandler`
- BLU thing types (`GROUP_BLU_THING_TYPES`) → `ShellyBluHandler`
- All others → `ShellyRelayHandler`
- `THING_TYPE_SHELLYPROTECTED` → `ShellyProtectedHandler`

### Initialization Sequence

```text
initialize()                      // called by openHAB on thing activation
  └── scheduleInitJob(2s delay)
        └── initializeThingConfig()  // load ShellyThingConfiguration; build apiConfig with DNS
        └── initializeThing()
              ├── create ShellyApiInterface (api1 or api2)
              ├── api.getDeviceInfo()          → ShellySettingsDevice
              ├── profile.initFromThingType()  → set flags from ThingTypeUID
              ├── api.getDeviceProfile()       → parse /settings JSON into profile
              ├── check credentials (401 → ShellyProtectedHandler)
              ├── setup CoAP handler (Gen1 only, if enableCoIOT)
              ├── api.setActionURLs()           Gen1: always called; CoIoT flag controls
              │                                 whether URLs are set or cleared on device
              ├── update thing properties (firmware, MAC, capabilities)
              └── start polling job
```

> **Note:** The `ShellyBaseHandler` constructor builds `ShellyApiConfiguration` with
> `resolveHostname=false` to avoid blocking the OSGi framework thread with a DNS lookup.
> The actual hostname resolution happens inside `initializeThingConfig()`, which always
> runs on the scheduler thread.

### Polling Loop

The polling job fires every `UPDATE_STATUS_INTERVAL_SECONDS` (3 s). Most cycles are
skipped using `skipCount` (default 20), so a full status update happens approximately
every 60 s:

```text
refreshStatus()
  ├── api.getStatus()              → ShellySettingsStatus
  ├── checkRestarted()             → detect device reboots
  ├── ShellyComponents.updateDeviceStatus()
  ├── ShellyComponents.updateRelays()     (if profile.hasRelays)
  ├── ShellyComponents.updateDimmers()    (if profile.isDimmer)
  ├── ShellyComponents.updateRollers()    (if profile.isRoller)
  ├── ShellyComponents.updateLight()      (if profile.isLight)
  ├── ShellyComponents.updateMeters()     (if profile.numMeters > 0)
  ├── ShellyComponents.updateSensors()    (if profile.isSensor)
  └── updateChannelDefinitions()          (first run only: creates channels)
```

### Command Handling

`handleCommand(ChannelUID, Command)` dispatches by channel group:

| Channel group | Handler action |
|---|---|
| `relay#output` | `api.setRelayTurn()` |
| `roller#control` | `api.setRollerTurn()` / `api.setRollerPos()` |
| `control#brightness` / `color#*` | `api.setBrightness()` / `api.setLightParm()` |
| `meter#resetTotals` | `api.resetMeterTotal()` |
| `control#mode` / `control#setTemp` | TRV: `api.setValveMode()` / `api.setValveTemperature()` |
| `RefreshType` | return cached state without API call |

### Error State Machine

`handleApiException()` classifies exceptions and transitions the thing status:

| Cause | Transition | Retry? |
|---|---|---|
| JSON parse error | `CONFIGURATION_ERROR` | No |
| HTTP 401 Unauthorized | `CONFIGURATION_ERROR` | No |
| HTTP 4xx / 5xx | `CONFIGURATION_ERROR` | No |
| Connection timeout / offline | `OFFLINE` | Yes, with exponential backoff |
| Watchdog expired | `OFFLINE` | Yes |
| Battery device sleep | `OFFLINE` (soft) | Yes (expected) |

---

## 7. API Layer

### `ShellyDiscoveryInterface` and `ShellyApiInterface`

`ShellyApiInterface extends ShellyDiscoveryInterface`. `ShellyDiscoveryInterface` carries
the four lifecycle methods used by both the discovery service and the full handler:

```java
void initialize();                                                    // no throws — impls handle internally
ShellySettingsDevice getDeviceInfo()               throws ShellyApiException;
ShellyDeviceProfile  getDeviceProfile(ThingTypeUID, ShellySettingsDevice) throws ShellyApiException;
void close();
```

`initialize()` is declared without `throws`. Implementations (`Shelly2ApiRpc`,
`ShellyBluApi`, `Shelly2ApiClient`) handle any setup exceptions internally. The discovery
service instantiates `Shelly2ApiClient` directly (not `Shelly2ApiRpc`) and calls
`api.initialize()` on a `ShellyDiscoveryInterface`-typed variable; the empty override in
`Shelly2ApiClient` satisfies the interface contract.

`ShellyApiInterface` adds all device-control methods. Key groups:

```java
// Lifecycle (from ShellyApiInterface, not ShellyDiscoveryInterface)
boolean isInitialized();

// Device info
ShellySettingsStatus getStatus() throws ShellyApiException;

// Relay
void setRelayTurn(int id, String turnMode) throws ShellyApiException; // "on"|"off"|"toggle"
void resetMeterTotal(int id) throws ShellyApiException;

// Roller
void setRollerTurn(int relayIndex, String turnMode) throws ShellyApiException;
void setRollerPos(int relayIndex, int position) throws ShellyApiException;

// Light
ShellyShortLightStatus setLightTurn(int id, String turnMode) throws ShellyApiException;
void setBrightness(int id, int brightness, boolean autoOn) throws ShellyApiException;
void setLightParm(int lightIndex, String parm, String value) throws ShellyApiException;
void setLightMode(String mode) throws ShellyApiException;  // "color"|"white"

// Thermostat (TRV)
void setValveMode(int id, boolean auto) throws ShellyApiException;
void setValveTemperature(int valveId, double value) throws ShellyApiException;

// Smoke
void muteSmokeAlarm(int smokeId) throws ShellyApiException;

// Firmware
ShellyOtaCheckResult checkForUpdate() throws ShellyApiException;
ShellySettingsUpdate firmwareUpdate(String uri) throws ShellyApiException;

// Network / cloud
boolean setWiFiRangeExtender(boolean enable) throws ShellyApiException;
boolean setBluetooth(boolean enable) throws ShellyApiException;

// Gen1-specific
String getCoIoTDescription() throws ShellyApiException;
void setActionURLs() throws ShellyApiException;
```

All methods throw `ShellyApiException`, which wraps HTTP errors, parse failures, and
connectivity problems. Callers handle the exception at the `refreshStatus()` /
`handleCommand()` boundary.

### Gen1 — `Shelly1HttpApi`

- Communicates via plain HTTP GET/POST to `http://<device-ip>/`
- Endpoints: `/settings`, `/status`, `/relay/0`, `/roller/0`, `/light/0`, `/emeter/0`
- Basic authentication: `Authorization: Basic base64(user:password)` when auth is enabled
- Workarounds for firmware bugs: Dimmer replaces `"lights"` with `"dimmers"` in JSON
  before parsing; Shelly UNI rewrites `ext_temperature` arrays to avoid Gson exceptions

**HTTP Action URL callbacks vs. CoIoT:**
Gen1 devices support two push mechanisms. They are mutually exclusive:

- **CoIoT / CoAP** (recommended): The device multicasts status updates to the network
  whenever its state changes. This requires no HTTP callback registration and works
  without knowing the openHAB server's IP address. CoIoT is enabled when
  `eventsCoIoT=true` in thing configuration and `autoCoIoT=true` in the binding
  configuration (the default).
- **HTTP Action URLs**: When CoIoT is _not_ active, `api.setActionURLs()` registers
  HTTP POST endpoints on the device (e.g. `btn_on_url`, `out_on_url`). The device then
  POSTs to openHAB when events occur. These URLs are cleared if CoIoT is enabled, as
  they would be redundant. The `Shelly1EventServlet` receives the POSTs.

`api.setActionURLs()` is always called from `initializeThing()`. Internally,
`setEventUrls()` and `setSensorEventUrls()` check `config.getEnableCoIOT()` and force
all event flags to `false` when CoIoT is active — this actively clears any previously
registered action URLs from the device rather than leaving stale callbacks in place:

```java
// setEventUrls() — simplified
boolean coiot = config.getEnableCoIOT();
setEventUrl(EVENT_TYPE_RELAY, 0, !coiot && config.getEventsSwitch(), ...);
setEventUrl(EVENT_TYPE_BUTTON, 0, !coiot && config.getEventsButton(), ...);
```

### Gen2 — `Shelly2ApiRpc`

Gen2 devices use JSON-RPC 2.0 as their protocol. All API calls, responses, and
push notifications share the same message format:

```json
{ "id": 1, "method": "Switch.Set", "params": { "id": 0, "on": true } }
```

Method names follow a `Component.Action` pattern: `Switch.Set`, `Cover.GoToPosition`,
`Light.Set`, `Sys.GetStatus`, `Shelly.GetConfig`.

#### WebSocket — Outbound Connection (always-on devices)

For always-on devices (Plus, Pro, Wall Display), the binding opens an **outbound**
WebSocket connection from openHAB to the device:

```text
openHAB  ──connect──►  ws://<device-ip>/rpc
         ◄──push────    NotifyStatus / NotifyEvent
         ──rpc──────►   Switch.Set, Cover.GoToPosition, …
```

Lifecycle (`Shelly2RpcSocket`):

- Connects immediately on handler initialization
- Sends a keepalive ping every 2 minutes; times out after 6 minutes idle
- Auto-reconnects with exponential backoff on disconnect
- Once connected, subscribes to `NotifyStatus` and `NotifyEvent` by calling
  `Shelly.GetStatus`. The device then pushes status changes proactively.

#### WebSocket — Inbound Connection (battery devices)

Battery-powered Gen2 devices (e.g. Shelly Plus H&T Gen3) wake up periodically and
_they_ connect **inbound** to openHAB's WebSocket servlet endpoint
`ws://<openHAB-ip>:<port>/shelly/wsevent`. This URL is passed to the device during
initialization so it knows where to connect when it wakes up:

```text
Device  ──connect──►  ws://<openHAB-ip>/shelly/wsevent
        ──push──────   NotifyStatus (with current readings)
        ◄──rpc──────   (optional response)
        ──disconnect
```

The binding does not initiate a connection to battery devices. Instead, it listens on
the servlet endpoint and routes incoming connections to the matching thing handler via
`ShellyThingTable`. The handler collects the data, then the device disconnects until
its next wake-up.

#### HTTP Fallback

If the WebSocket connection cannot be established, `Shelly2ApiRpc` falls back to plain
HTTP GET/POST to the device's `/rpc` endpoint with the same JSON-RPC payload.

### BLU — `ShellyBluApi`

- Extends `Shelly2ApiRpc`; overrides `initialize()`, `getDeviceInfo()`, and
  `getDeviceProfile()` to return synthetic data (no direct device connection)
- BLU devices are battery-powered Bluetooth sensors that cannot be reached from
  openHAB directly
- A Gen2 Plus/Pro gateway device runs a Lua script (installed by the binding when
  `enableBluGateway=true`) that receives BLE advertisements and forwards them as
  RPC `NotifyEvent` messages to openHAB
- The gateway's `Shelly2ApiRpc` instance relays these notifications to the matching
  `ShellyBluHandler` via `ShellyThingTable` using the BLU device's MAC address
  as lookup key

#### BTHome Payload Format

BLU devices broadcast sensor data using the **BTHome v2** open standard
(<https://bthome.io>). The BLE advertisement payload is a sequence of
`(object_id, value)` pairs encoded in little-endian. The gateway Lua script
decodes the raw BLE advertisement and forwards the values in the `BluScan.Data`
RPC notification as named fields keyed by their BTHome object ID.

Object IDs relevant to current Shelly BLU devices:

| Object ID | Hex | Data type | Factor | Meaning |
|---|---|---|---|---|
| 0 | 0x00 | uint8 | — | Packet ID (sequence counter) |
| 1 | 0x01 | uint8 | 1 | Battery (%) |
| 2 | 0x02 | sint16 | 0.01 | Temperature (°C, high precision) |
| 3 | 0x03 | uint16 | 0.01 | Humidity (%) |
| 5 | 0x05 | uint24 | 0.01 | Illuminance (lx) |
| 17 | 0x11 | uint8 | — | Opening: 0=closed, 1=open |
| 21 | 0x15 | uint8 | — | Battery low: 0=normal, 1=low |
| 33 | 0x21 | uint8 | — | Motion: 0=clear, 1=detected |
| 41 | 0x29 | uint8 | — | Smoke: 0=clear, 1=detected |
| 45 | 0x2D | uint8 | — | Window: 0=closed, 1=open |
| 58 | 0x3A | uint8 | — | Button event (see below) |
| 60 | 0x3C | uint8+uint8 | — | Dimmer event + steps |
| 63 | 0x3F | sint16 | 0.1 | Rotation (°) |
| 69 | 0x45 | sint16 | 0.1 | Temperature (°C, standard precision) |
| 240 | 0xF0 | uint16 | — | Device type ID |

Button event values (object ID 58 / 0x3A):

| Value | Event |
|---|---|
| 0 | None |
| 1 | Press |
| 2 | Double press |
| 3 | Triple press |
| 4 | Long press |
| 5 | Long double press |
| 128 | Hold press |

When adding a new BLU device, look up its object IDs in the BTHome specification
and map them to openHAB channel states in `ShellyBluApi`.

### Minimum Firmware Requirements

| Generation | Minimum Version | Notes |
|---|---|---|
| Gen1 | `v1.8.2` | Constant `SHELLY_API_MIN_FWVERSION` |
| Gen1 extended features | `v1.10` | Enables input events, auto-timer; `profile.extFeatures` |
| Gen2 / Gen3 / Gen4 | `v0.10.1` | Constant `SHELLY2_API_MIN_FWVERSION` |

Firmware version comparisons use `ShellyVersionDTO.compare(a, b)`. The `extFeatures`
flag on the profile is set to `true` when the Gen1 firmware is `>= v1.10`, and guards
channels and API calls that are not available on older firmware.

---

## 8. Device Profiling

`ShellyDeviceProfile` is the central descriptor of a device's capabilities. It drives
all channel creation and update logic. The profile is populated in two phases, then
consulted throughout the handler's lifetime.

### Key Fields — Detailed

#### `alwaysOn` vs. `hasBattery`

These two flags model the power supply and connectivity expectations:

- **`alwaysOn = true`**: The device is mains-powered and permanently reachable. The
  binding maintains a persistent connection (WebSocket for Gen2, CoIoT for Gen1) and
  expects responses within the normal timeout window. Failure to respond triggers an
  `OFFLINE` transition and retry logic.
- **`alwaysOn = false`** (battery devices): The device sleeps most of the time to
  preserve battery. Periods of silence are expected and do not cause an alarm. The
  binding transitions to `OFFLINE` softly during sleep, and back to `ONLINE` when the
  device wakes up and delivers a new reading.
- **`hasBattery = true`**: The device _has_ a battery, which may be its sole power
  source or a backup. When true, the binding creates battery-level and low-battery
  channels. A device can have `alwaysOn = true` and `hasBattery = true` (e.g. Shelly
  Motion 2, which has a battery but is always reachable over WiFi).

**`updatePeriod`** reflects these modes:

- For always-on devices: set to `2 * UPDATE_SETTINGS_INTERVAL_SECONDS + 10` seconds
  (a generous watchdog timeout — the device is expected to report frequently)
- For battery devices: set from the device's own `sleep_time` setting in `/settings`.
  This is used as the watchdog interval: if no update arrives within `updatePeriod`,
  the thing transitions to `OFFLINE` (expected during sleep)

#### `isBlu`

`true` for all BLU series Bluetooth devices. When set:

- `ShellyBluApi` is used instead of `Shelly2ApiRpc`
- No HTTP or WebSocket connection is made to the device itself
- `deviceIp` is empty; `deviceAddress` holds the normalized MAC address
- The device's data arrives exclusively via RPC notifications from a gateway
- `alwaysOn = false`, `hasBattery = true` are implied for all current BLU devices

#### `hasRelays` and `numRelays`

`hasRelays = true` indicates the device has at least one controllable output (relay,
MOSFET, or dimmer stage). It is derived as:

```java
hasRelays = (numRelays > 0) || isDimmer;
```

`numRelays` comes from `device.numOutputs` in the `/settings` response. It drives how
many indexed relay channel groups (`relay0`, `relay1`, …) are created. A dimmer counts
as a relay for channel group purposes even though it has a brightness channel instead
of a simple on/off.

#### `isSensor`

`true` for pure sensor devices (H&T, Door/Window, Button, Smoke, Motion, BLU sensors).
When set:

- No relay, roller, or light channels are created
- The `sensors` and `battery` channel groups are created
- The polling loop calls `ShellyComponents.updateSensors()` instead of relay/light
  update methods
- The binding tolerates gaps in updates (sleep periods) without going `OFFLINE`

#### `isLight` vs. `isBulb`

- **`isLight = true`**: The device has controllable light output. This includes bulbs,
  RGBW2 controllers, Duo (white), and Vintage. When true, the light channel groups
  (`control`, `color`, `white`) are created.
- **`isBulb = true`**: A subset of `isLight`. Set only for the Shelly Bulb RGBW and
  its variants. Bulbs support both full RGBW color mode and white mode with color
  temperature. The `inColor` flag then determines which channel group is active at
  runtime.
- **`isDuo = true`**: Another subset of `isLight`. Set for Shelly Duo (white bulb with
  tunable color temperature). No RGB channels — only brightness and color temperature.
- **`isRGBW2 = true`**: The RGBW2 controller. In color mode it has one RGBW output;
  in white mode it has up to four independent white output channels (`channel0`…`channel3`).

#### `inColor`

Only meaningful when `isLight = true`. Reflects the device's current operating mode
as reported by `/settings` (`mode == "color"`):

- `inColor = true`: the color channel group (`color`) with HSB/RGBW channels is active
- `inColor = false`: the white channel group (`white`) with brightness and color
  temperature is active

`inColor` can change at runtime when the user switches modes. The handler detects this
during a refresh and recreates the appropriate channels.

#### `isButton` vs. `isMultiButton`

- **`isButton = true`**: A single-button device (Shelly Button1, BLU Button1). Has
  one input, one `status` channel group, and one `buttonTrigger` channel. Cannot control
  an output directly — it only fires events.
- **`isMultiButton = true`**: A multi-button device (Shelly BLU Wall Switch 4, BLU RC
  Button 4). Has multiple independent inputs, each with its own indexed `status` channel
  group (`status1`, `status2`, …). `isMultiButton` implies `isButton = false`.

Both types use `isSensor = true` / `hasBattery = true` and deliver data via events
rather than polled status.

### Fields Reference

```java
// Identification
boolean initialized;   // false until fully loaded from API
String fwVersion;      // "v1.11.0" (Gen1) or "1.0.0" (Gen2)
String hwRev;          // hardware revision string
boolean isGen2;        // true for Gen2/3/4 devices (RPC API)
boolean isBlu;         // true for BLU Bluetooth series

// Power / connectivity
boolean alwaysOn;      // mains-powered, permanently reachable
boolean hasBattery;    // has battery (may still be alwaysOn)
int updatePeriod;      // watchdog / expected update interval in seconds

// Outputs
boolean hasRelays;     int numRelays;   // controllable relay outputs
boolean isRoller;      int numRollers;  // roller/shutter mode
boolean isDimmer;                       // Shelly Dimmer 1/2

// Lighting
boolean isLight;       // has any light control
boolean isBulb;        // RGBW bulb (color + white modes)
boolean isDuo;         // tunable white bulb
boolean isRGBW2;       // RGBW2 controller (4 white channels)
boolean inColor;       // currently in color mode (vs. white)
int minTemp;           int maxTemp;  // color temperature range (Kelvin)

// Sensors
boolean isSensor;      // pure sensor device (no relay/light)
boolean isHT;          // Shelly H&T (temperature + humidity)
boolean isDW;          // Shelly Door/Window
boolean isButton;      // single-button device
boolean isMultiButton; // multi-button (BLU Wall Switch 4)
boolean isMotion;      // Shelly Motion
boolean isSmoke;       // Shelly Smoke
boolean isTRV;         // Shelly TRV thermostat valve
boolean isSense;       // Shelly Sense (IR camera + sensor)
boolean isIX;          // Shelly i3 / ix3 input device
boolean isDistance;    // BLU Distance sensor
boolean isRemote;      // BLU Rotary Remote

// Metering
int numMeters;         // number of power meter channels
boolean isEMeter;      // energy meter (ShellyEM / 3EM)
boolean is3EM;         // 3-phase energy meter
boolean isEM50;        // Pro EM50 variant
boolean isCB;          // Pro Circuit Breaker

// Runtime data (updated per refresh cycle)
ShellySettingsGlobal settings;  // parsed /settings response
ShellySettingsStatus status;    // parsed /status response
ShellySettingsDevice device;    // device hardware info
String gateway;                 // gateway thing name (BLU devices)
```

### Initialization Phases

**Phase 1 — from ThingTypeUID** (`initFromThingType()`):
Sets the basic boolean flags purely from the static thing type, before any API call.
This allows the API layer to know what kind of device it is and what to expect.

**Phase 2 — from API** (`initialize(thingTypeUID, settingsJson, device)`):
Parses the full `/settings` JSON response via Gson. Derives:

- `numRelays`, `numRollers`, `numMeters` from `device.numOutputs` /
  `device.numRollers` / `device.numMeters`
- `isRoller` from `device.mode == "roller"`
- `inColor` from `mode == "color"` combined with `isLight`
- `extFeatures` from firmware version comparison (`>= v1.10`)
- `updatePeriod` from `settings.sleepTime` (battery) or the default polling interval

### Utility Methods

```java
// Channel group name for a given relay/light/roller index
// e.g. idx=0 → "relay0", idx=1 → "relay1"
String getControlGroup(int idx);

// Input channel group name (may differ from control group for rollers)
String getInputGroup(int idx);

// Channel suffix for multi-input devices: "" for single, "1"/"2"/... for multi
String getInputSuffix(int idx);

// True if input i is configured in button mode (vs. detached / follow mode)
boolean inButtonMode(int idx);

// True if the settings JSON has a registered event URL for the given type
boolean containsEventUrl(String eventType);

// True if the mDNS service name indicates a Gen2+ device
static boolean isGeneration2(String serviceName);
```

---

## 9. Channel System

### Design Principle

Channels are **not** declared statically in thing-type XML. They are added to the thing
programmatically after the first successful device status read. This allows:

- Conditional channels based on firmware/settings (e.g. `autoOn` timer only if the
  device actually reports `autoOn` in its settings)
- Indexed channels for multi-relay/multi-meter devices (`relay0`, `relay1`, `meter0`)
- Add-on sensor channels only when an add-on module is physically connected

### `ITEMT_*` Item Type Constants

Defined in `ShellyChannelDefinitions`, used when building `ChannelBuilder` instances:

| Constant | Item Type | Use Case |
|---|---|---|
| `ITEMT_SWITCH` | `Switch` | On/off state, binary flags |
| `ITEMT_CONTACT` | `Contact` | Door/window sensors |
| `ITEMT_ROLLER` | `Rollershutter` | Roller shutter control |
| `ITEMT_DIMMER` | `Dimmer` | Brightness (0–100%) |
| `ITEMT_STRING` | `String` | Text, event types, modes |
| `ITEMT_NUMBER` | `Number` | Plain dimensionless number |
| `ITEMT_TEMP` | `Number:Temperature` | Temperature with unit |
| `ITEMT_POWER` | `Number:Power` | Watts |
| `ITEMT_ENERGY` | `Number:Energy` | kWh |
| `ITEMT_VOLT` | `Number:ElectricPotential` | Volts |
| `ITEMT_AMP` | `Number:ElectricCurrent` | Amperes |
| `ITEMT_FREQ` | `Number:Frequency` | Hz |
| `ITEMT_LUX` | `Number:Illuminance` | Lux |
| `ITEMT_PERCENT` | `Number:Dimensionless` | 0–100% (battery, humidity) |
| `ITEMT_ANGLE` | `Number:Angle` | Degrees (tilt, rotation) |
| `ITEMT_DISTANCE` | `Number:Length` | Meters |
| `ITEMT_TIME` | `Number:Time` | Seconds |
| `ITEMT_DATETIME` | `DateTime` | Timestamps |

### Channel Registry

`CHANNEL_DEFINITIONS` is a static `ChannelMap` populated once at OSGi activation.
Each entry maps a `group#channel` key to a `ShellyChannel` descriptor:

```java
new ShellyChannel(translationProvider, groupId, channelId, channelTypeId, itemType)
```

The `channelTypeId` references either a system channel type (e.g. `system:power`) or
a binding-specific type defined in `device.xml` (e.g. `meterWatts`).

### Dynamic Channel Creation

All `createXXX()` methods follow the same pattern:

```java
public static Map<String, Channel> createRelayChannels(Thing thing,
        ShellyDeviceProfile profile, ShellySettingsRelay rstatus, int idx) {
    Map<String, Channel> add = new LinkedHashMap<>();
    String group = profile.getControlGroup(idx);  // "relay0", "relay1", ...

    // Only add a channel if the device actually reports the field
    addChannel(thing, add, rs.ison != null,    group, CHANNEL_OUTPUT);
    addChannel(thing, add, rs.autoOn != null,  group, CHANNEL_TIMER_AUTOON);
    addChannel(thing, add, rs.autoOff != null, group, CHANNEL_TIMER_AUTOOFF);
    ...
    return add;
}
```

`addChannel(thing, add, condition, group, channelId)` creates a `Channel` from the
type and item type registered in `CHANNEL_DEFINITIONS`, and inserts it into `add` only
when `condition` is `true` and the channel does not already exist on the thing.

The collected maps are merged and applied in `ShellyBaseHandler.updateChannelDefinitions()`:

```java
ThingBuilder tb = editThing();
for (Channel ch : dynChannels.values()) {
    tb.withChannel(ch);
}
updateThing(tb.build());
```

### Channel State Cache

`ShellyChannelCache` prevents redundant `updateState()` calls by storing the last
published state per channel:

```java
// Returns true only if the new value differs from the cached one
boolean updateChannel(String channelId, State newValue, Boolean forceUpdate);

// Retrieve last known state (returns UnDefType.NULL if not yet published)
State getValue(String group, String channel);
```

The cache starts **disabled** (all updates pass through). After `N` full update cycles
it is **enabled**, and subsequent calls are filtered. This ensures all channels are
published to openHAB at least once during initialization.

### Channel Groups Used at Runtime

| Group constant | XML group id | Used for |
|---|---|---|
| `CHANNEL_GROUP_DEV_STATUS` | `device` | Firmware, uptime, WiFi, heartbeat |
| `CHANNEL_GROUP_RELAY_CONTROL` | `relay0` / `relay1` | Relay output, input, timers |
| `CHANNEL_GROUP_ROL_CONTROL` | `roller` | Rollershutter, position, safety |
| `CHANNEL_GROUP_DIMMER_CONTROL` | `dimmer` | Brightness |
| `CHANNEL_GROUP_LIGHT_CONTROL` | `control` | Light power, color mode, timers |
| `CHANNEL_GROUP_COLOR_CONTROL` | `color` | HSB, RGBW values |
| `CHANNEL_GROUP_WHITE_CONTROL` | `white` | White brightness and temperature |
| `CHANNEL_GROUP_LIGHT_CHANNEL` | `channel0..3` | RGBW2 individual white channels |
| `CHANNEL_GROUP_METER` | `meter0` / `meter1` | Watts, kWh, voltage, current |
| `CHANNEL_GROUP_NMETER` | `nmeter` | 3EM neutral current |
| `CHANNEL_GROUP_SENSOR` | `sensors` | All environmental readings |
| `CHANNEL_GROUP_BATTERY` | `battery` | Level, low-battery flag |
| `CHANNEL_GROUP_CONTROL` | `control` | TRV mode, setpoint, boost |
| `CHANNEL_GROUP_STATUS` | `status1..4` | Button/input events |

---

## 10. Update Flow

### Push Path (Gen1 CoIoT / Gen2 WebSocket)

```text
Device → CoAP notification (Gen1)
       └── Shelly1CoapHandler.handleUpdate()
             └── ShellyBaseHandler.onEvent()
                   └── ShellyComponents.updateXXX()

Device → WebSocket message (Gen2)
       └── Shelly2RpcSocket.onWebSocketText()
             └── Shelly2ApiRpc.onMessage()
                   └── ShellyBaseHandler.onEvent()
                         └── ShellyComponents.updateXXX()
```

### Poll Path

```text
ScheduledExecutorService (every 3 s)
  └── ShellyBaseHandler.refreshStatus()
        ├── api.getStatus()         HTTP GET /status (or WebSocket RPC)
        └── ShellyComponents.updateXXX(handler, status)
              └── handler.updateChannel(group, channel, state)
                    └── ShellyChannelCache.updateChannel()  (suppresses duplicates)
                          └── updateState(channelUID, state)  (openHAB core)
```

### Event Callbacks (Gen1 HTTP Action URLs)

When CoIoT is **not** active, Gen1 devices POST to openHAB on button presses and relay
state changes. The binding registers these callback URLs during `initializeThing()` via
`api.setActionURLs()`. Incoming POSTs arrive at `Shelly1EventServlet` and are dispatched
to the matching handler via `ShellyThingTable`.

**Important:** When CoIoT is enabled, `setActionURLs()` still runs but all flags are
forced to `false`, which actively clears any previously registered callback URLs from
the device. This prevents stale action URLs from producing duplicate events when the
device switches from HTTP-callback mode to CoIoT mode.

### Battery Device Considerations

Battery-powered devices (H&T, DW, Button, BLU sensors) sleep between measurements.
The binding:

- Does not maintain a persistent connection; each wake-up is treated as a fresh contact
- Uses `hasBattery = true` in the profile to suppress connection-loss alarms during
  expected sleep periods
- Transitions to `OFFLINE` softly; the device wakes up and resumes normally
- Derives `updatePeriod` from the device's configured sleep interval to set an
  appropriate watchdog timeout

---

## 11. Configuration Classes

### Configuration Class Responsibilities

The binding uses four configuration classes. They fall into two groups:

**Raw/POJO layer** — populated directly by the OH framework, never shared across threads:

| Class | Scope | Populated by |
|---|---|---|
| `ShellyBindingConfiguration` | Binding-wide | `fromProperties()` from OSGi config PID |
| `ShellyThingConfiguration` | Per-thing | `getConfigAs()` from thing config |

**Runtime/derived layer** — immutable snapshots built from the POJO layer:

| Class | Scope | Built by |
|---|---|---|
| `ShellyBindingRuntimeConfig` | Binding-wide | `new ShellyBindingRuntimeConfig(raw, nas)` |
| `ShellyApiConfiguration` | Per-thing | `new ShellyApiConfiguration(thing, runtime, …)` |

The data flow at startup:

```text
OSGi properties
  │  fromProperties(Map)
  ↓
ShellyBindingConfiguration       ← POJO: raw fields only, no IP resolution
  │  new ShellyBindingRuntimeConfig(config, NetworkAddressService)
  ↓
ShellyBindingRuntimeConfig       ← immutable: local IP resolved, HTTP port set
  │  combined with
ShellyThingConfiguration         ← POJO: per-thing settings from OH UI
  │  new ShellyApiConfiguration(thing, runtime, realm, gen2)
  ↓
ShellyApiConfiguration           ← immutable per-thing snapshot passed to API layer
     provides: resolved device address, merged credentials, callback URLs,
               event flags, realm
```

`ShellyApiConfiguration` is the only config object the API layer (`Shelly1HttpApi`,
`Shelly2ApiRpc`) ever sees. It is created in `initializeThingConfig()` and passed to
`api.initialize()`. After initialization, the API layer holds an immutable copy and
does not need to know about the source objects.

Credentials are merged at `ShellyApiConfiguration` construction time:

- If `thingConfig.userId` / `thingConfig.password` are non-empty, they take precedence
- Otherwise the binding-wide `bindingConfig.defaultUserId` / `defaultPassword` are used

### Runtime Configuration Updates

Both `ShellyBindingConfiguration` and `ShellyThingConfiguration` can be changed at
runtime through the openHAB UI. The update paths are:

**Thing settings changed** → openHAB calls `handleConfigurationUpdate()` on the handler:

```text
handleConfigurationUpdate()
  ├── super.handleConfigurationUpdate()   persists new values
  ├── newConfig    = getConfigAs(ShellyThingConfiguration.class)
  ├── newApiConfig = buildApiConfig(newConfig, bindingConfig)   computed before writing
  ├── config    = newConfig        ← volatile write
  ├── apiConfig = newApiConfig     ← volatile write (consistent with config)
  ├── applyCoapConfig()            stops/reconfigures/recreates CoAP handler
  └── reinitializeThing()          triggers full re-initialization
```

**Binding settings changed** → OSGi DS calls `@Modified modified()` on
`ShellyHandlerFactory`, which propagates to all handlers:

```text
ShellyHandlerFactory.modified()
  ├── ShellyBindingConfiguration raw = fromProperties(newProperties)
  ├── ShellyBindingRuntimeConfig  rt = new ShellyBindingRuntimeConfig(raw, nas)
  ├── bindingConfig = rt            ← volatile write in factory
  └── for each handler: handler.updateBindingConfig(rt)
        ├── newApiConfig = buildApiConfig(config, rt)
        ├── bindingConfig = rt      ← volatile write in handler
        ├── apiConfig     = newApiConfig
        ├── applyCoapConfig()
        └── reinitializeThing()
```

Both paths compute the derived `apiConfig` before writing any fields, ensuring a
concurrent scheduler thread never observes a `config`/`bindingConfig` combination
that does not match the `apiConfig` it reads.

### `deviceIp` vs. `deviceAddress`

These two fields in `ShellyThingConfiguration` serve different purposes:

| Field | Used for | Value |
|---|---|---|
| `deviceIp` | Gen1 and Gen2 devices | IP address, hostname, or `host:port` |
| `deviceAddress` | BLU devices only | Bluetooth MAC address |

In `ShellyApiConfiguration` the two are resolved as follows:

```java
if (!thingConfig.getDeviceAddress().isEmpty()) {
    // BLU device: strip colons, lowercase → "aabbccddeeff"
    deviceAddress = thingConfig.getDeviceAddress()
                        .toLowerCase(Locale.ROOT).replace(":", "");
    deviceIp = "";                    // BLU has no reachable IP
} else {
    // Regular device: resolve hostname to IP if needed
    deviceIp = deviceAddress = resolveIp(thingConfig.getDeviceIp());
}
```

DNS resolution is controlled by the `resolveHostname` constructor parameter.
The handler constructor passes `false` (no DNS, safe on OSGi framework thread);
`initializeThingConfig()` rebuilds `apiConfig` with `resolveHostname=true` on the
scheduler thread where blocking is permitted.

For regular devices `deviceIp` and `deviceAddress` end up identical (the resolved IP).
For BLU devices `deviceIp` is empty and `deviceAddress` holds the normalized MAC.
`ShellyThingTable` uses `deviceAddress` as the lookup key when routing gateway RPC
notifications to the correct BLU handler.

### `ShellyThingConfiguration`

Per-thing configuration mapped from the `<config-description>` in `thing-types.xml`.

```java
String  deviceIp;            // Gen1/Gen2: IP address, hostname, or host:port
String  deviceAddress;       // BLU: Bluetooth MAC address (with or without colons)
String  userId;              // HTTP auth username (empty → use binding default)
String  password;            // HTTP auth password  (empty → use binding default)
int     updateInterval;      // Polling interval in seconds (default: 60)
int     favoriteUP;          // Roller position for ON command  (0 = disabled)
int     favoriteDOWN;        // Roller position for OFF command (0 = disabled)
int     lowBattery;          // Low-battery alarm threshold (%, default: 15)
boolean brightnessAutoOn;    // Turn light on when brightness > 0
boolean eventsButton;        // Register for button push events (Gen1)
boolean eventsSwitch;        // Register for switch state events (Gen1)
boolean eventsCoIoT;         // Use CoIoT push instead of HTTP polling (Gen1)
Boolean enableBluGateway;    // Install BLU gateway Lua script (Gen2)
Boolean enableRangeExtender; // Enable WiFi range extender mode (Gen2)
```

Getters are provided for all fields. Never access DTO fields directly from outside
the config layer.

### `ShellyBindingConfiguration`

A plain mutable POJO that carries the raw values from the OH binding configuration PID
`binding.shelly`. It is populated by the OH framework (or via `fromProperties`) and
**never shared across threads** — it is only used transiently to construct a
`ShellyBindingRuntimeConfig`.

```java
// Mutable fields (set by fromProperties; defaults shown)
String  defaultUserId   = "admin";  // Default HTTP username for all devices
String  defaultPassword = "admin";  // Default HTTP password
String  localIP         = "";       // Admin override; empty = auto-detect from NAS
boolean autoCoIoT       = true;     // Auto-enable CoIoT when possible

// Factory methods
static ShellyBindingConfiguration fromProperties(@Nullable Map<String, Object> p);
static ShellyBindingConfiguration fromProperties(@Nullable Dictionary<String, Object> p);

// Getters
String  getDefaultUserId();
String  getDefaultPassword();
String  getLocalIP();      // empty when no admin override is set
boolean isAutoCoIoT();
```

`localIP` here means "admin-configured override only". When it is empty, the
`ShellyBindingRuntimeConfig` constructor resolves the actual host IP via
`NetworkAddressService`.

### `ShellyBindingRuntimeConfig`

Immutable runtime snapshot derived from `ShellyBindingConfiguration` plus
`NetworkAddressService`. Held as `volatile` in `ShellyHandlerFactory` and
`ShellyBaseHandler` to guarantee safe publication after `@Modified` callbacks.

```java
// Immutable fields
private final String  localIP;         // Resolved: config override OR primary IPv4 from NAS
private final String  defaultUserId;
private final String  defaultPassword;
private final int     httpPort;        // -1 stored internally → DEFAULT_LOCAL_PORT via getter
private final boolean autoCoIoT;

// Constructor — resolves local IP; link-local addresses (169.254.x.x) become ""
ShellyBindingRuntimeConfig(ShellyBindingConfiguration config, NetworkAddressService nas);

// Wither — returns new instance with updated port (all other fields unchanged)
ShellyBindingRuntimeConfig withHttpPort(int httpPort);

// Getters
String  getLocalIP();
String  getDefaultUserId();
String  getDefaultPassword();
int     getHttpPort();     // returns DEFAULT_LOCAL_PORT when internal value is -1
boolean isAutoCoIoT();
```

**Why two classes?** `ShellyBindingConfiguration` mirrors the OH config model (mutable
POJO, populated by the framework, short-lived). `ShellyBindingRuntimeConfig` is the
long-lived, thread-safe snapshot that the rest of the binding works with — analogous to
the `ShellyThingConfiguration` / `ShellyApiConfiguration` split at the thing level.

### `ShellyApiConfiguration`

Derived configuration consumed exclusively by the API layer. Also immutable
(private final fields + getters). Created in `ShellyBaseHandler.initializeThingConfig()`:

```java
ShellyApiConfiguration apiConfig =
    new ShellyApiConfiguration(thingConfig, bindingConfig, realm, gen2);
```

Two fields are intentionally mutable after construction and use atomic types:

- **`realm`** (`AtomicReference<String>`): not known until the first `/shelly` response;
  set via `setRealm()` after device identification.
- **`enableCoIOT`** (`AtomicBoolean`): the autoCoIoT feature promotes this flag from
  `false → true` at runtime after `checkVersion()` confirms firmware support.

All other fields are `final`.

Key fields provided:

- `deviceIp` / `deviceAddress`: resolved addresses (see above)
- Merged credentials via `ShellyAuthCredentials` (inner class)
- `deviceApiUrl`, `eventCallbackUrl`, `webSocketCallback`: pre-built URL strings
- Event flags: `eventsButton`, `eventsSwitch`, `eventsPush`, `eventsRoller`,
  `eventsSensorReport`
- `enableCoIOT`: see above
- `realm`: see above

---

## 12. Data Transfer Objects (DTOs)

> **Tip:** Real-world JSON samples for every generation are collected in the DEVNOTES
> files — see [Resources → Sample API Responses](#sample-api-responses). They are
> invaluable when mapping a new field or device type to a DTO class.

All JSON API responses are deserialized by Gson into static inner classes in two files:

### `Shelly1ApiJsonDTO` (Gen1)

Key root classes and their corresponding API endpoints:

| DTO class | Source endpoint | Purpose |
|---|---|---|
| `ShellySettingsDevice` | `/settings` | Hardware type, MAC, firmware, mode |
| `ShellySettingsGlobal` | `/settings` | Full settings (relays, rollers, lights, …) |
| `ShellySettingsStatus` | `/status` | Full runtime status |
| `ShellyStatusRelay` | `/relay/<n>` | Single relay state |
| `ShellyRollerStatus` | `/roller/<n>` | Roller position and state |
| `ShellyStatusLight` | `/light/<n>` | Light brightness, color, mode |
| `ShellyStatusSensor` | `/status` (embedded) | Temperature, humidity, motion, … |
| `ShellySettingsEMeter` | `/emeter/<n>` | Voltage, current, power, energy |
| `ShellyOtaCheckResult` | `/ota/check` | Firmware update check result |

All fields use boxed types (`Boolean`, `Double`, `Integer`) so that absent JSON fields
deserialize to `null` rather than a default value. This null-presence is checked in
`ShellyChannelDefinitions.addChannel()` to decide whether to create a channel.

### `Shelly2ApiJsonDTO` (Gen2)

Gen2 uses JSON-RPC 2.0. Key structures:

```java
// Outbound RPC call
Shelly2RpcRequest { int id; String method; Object params; }

// Inbound response / push
Shelly2RpcBaseMessage {
    Integer id; String method; Object params; Object result; String error;
}

// Push notification
Shelly2RpcNotifyEvent { String dst; Shelly2NotifyEvent params; }
```

Config and status for each component type follow a naming convention:

- `Shelly2ConfigSwitch` / `Shelly2DeviceStatusSwitch` (switch/relay)
- `Shelly2ConfigLight` / `Shelly2DeviceStatusLight`
- `Shelly2ConfigCover` / `Shelly2DeviceStatusCover` (roller/shutter)

---

## 13. Discovery

### mDNS Discovery (`ShellyMDNSDiscoveryParticipant`)

The binding listens on service types `_http._tcp.local.` (for backwards compatibility
with older Gen1 firmware) and `_shelly._tcp.local.` (newer firmware and all Gen2+).
Service names are validated against the pattern:

```text
^([a-z0-9]*shelly[a-z0-9]*)-([a-z0-9]+)$
```

This matches names like `shelly1-abc123`, `shellyplus2pm-aabbcc`, `shellyblubutton1-112233`.

**On a new mDNS advertisement:**

1. Extract IPv4 address from `ServiceInfo.getInet4Addresses()`
1. Read the `gen` TXT record (`"2"`, `"3"`, `"4"` → Gen2) and fall back to
   `ShellyDeviceProfile.isGeneration2(serviceName)` for older firmware that does not
   publish the property
1. Capture a local snapshot of `this.bindingConfig` (volatile field) to avoid a
   TOCTOU race with concurrent `@Modified` callbacks
1. Probe the device via HTTP to read `/shelly` or `/settings` and confirm its type
1. Call `ShellyThingCreator.getThingUID()` to map the device type string to a
   `ThingTypeUID`
1. Build and return a `DiscoveryResult` with the device's IP as `deviceIp` property

### Standard Gen1 / Gen2 Discovery Flow

```text
mDNS advertisement arrives
  └── ShellyMDNSDiscoveryParticipant.createResult()
        └── ShellyBasicDiscoveryService.createResult()
              ├── HTTP GET http://<ip>/shelly    → device type, gen, MAC
              ├── ShellyThingCreator.getThingUID()
              └── DiscoveryResultBuilder
                    .withProperty("deviceIp", ip)
                    .withProperty("serviceName", name)
                    .withRepresentationProperty("deviceIp")
                    .build()
```

The resulting inbox entry is immediately usable — the user only needs to confirm it.

### BLU Device Discovery (via Gateway)

BLU devices do not advertise themselves on mDNS and have no IP address. They are
discovered indirectly through a Gen2 Plus/Pro gateway device:

1. The user configures a Gen2 gateway thing (Plus 1, Plus 2PM, Pro, etc.) and enables
   the `enableBluGateway` option.
1. On initialization, `Shelly2ApiRpc` installs a Lua script on the gateway device via
   the `Script.Create` / `Script.PutCode` RPC methods. The script scans for BLE
   advertisements and sends `BluScan.Data` notifications to openHAB.
1. When a BLU device comes within Bluetooth range, its BLE advertisement is forwarded
   as an RPC notification to openHAB.
1. `ShellyBluApi.onMessage()` receives the notification, identifies the BLU device
   type from the advertisement data (device MAC + model code), and calls
   `ShellyThingTable.discoveryResult()` to create an inbox entry.
1. The discovery result is built with the BLU device's MAC address as `deviceAddress`
   and the gateway's thing name as `gatewayDevice` property.
1. The created BLU thing has no `deviceIp` — it is linked to the gateway via
   `gatewayDevice`.

```text
BLU device broadcasts BLE advertisement
  └── Gateway Lua script receives it
        └── RPC NotifyEvent "BluScan.Data" → openHAB ws://<oh>/shelly/wsevent
              └── Shelly2ApiRpc.onMessage()
                    └── ShellyBluApi.processBleScanData()
                          └── ShellyThingTable.discoveryResult()
                                └── inbox entry: type=shellyblubuttonx,
                                                 deviceAddress=aabbccddeeff,
                                                 gatewayDevice=shellyplus2pm-abc123
```

The BLU thing handler (`ShellyBluHandler`) does not maintain a connection. It waits
for data-carrying RPC notifications forwarded by the gateway's `Shelly2ApiRpc` instance
via `ShellyThingTable` using the MAC address as a lookup key.

### Devices in Range Extender Mode

A Gen2 Plus/Pro device configured as a WiFi range extender (via `enableRangeExtender`
or through the Shelly app) bridges other devices onto the main WiFi network. From
openHAB's perspective, devices reachable through the extender are just regular WiFi
devices — they advertise themselves on mDNS and respond to HTTP just like any other
device. No special handling is needed in the binding.

The Gen2 gateway device that acts as a range extender is itself discovered via mDNS in
the normal way. When the `enableRangeExtender` thing option is enabled, the binding
calls `api.setWiFiRangeExtender(true)` to activate the mode on the device.

### `ShellyThingTable` — Shared Handler Registry

`ShellyThingTable` is an OSGi singleton service that keeps a map of all active thing
handlers, keyed by thing UID string. It is used by:

- **CoAP server**: routes incoming CoAP notifications to the handler whose device IP
  matches the source address
- **Event servlet**: routes incoming HTTP POSTs (Gen1 action callbacks) to the
  matching handler
- **WebSocket servlet**: routes inbound WebSocket connections from battery Gen2 devices
  to the matching handler
- **BLU gateway**: routes BLE scan notifications to the BLU handler by MAC address
- **Manager UI**: iterates all handlers for the device overview page
- **Discovery**: adds new inbox entries when BLU devices are detected

---

## 14. Manager UI

A built-in management interface is served at `/shelly/manager` via `ShellyManagerServlet`
(an OSGi `HttpServlet`). It provides:

- Device overview: all registered things, their status, firmware version, WiFi signal
- Per-device control panel: relay toggle, roller position, reboot, factory reset
- OTA firmware update trigger with version comparison

Pages extend `ShellyManagerPage` and return an `ShellyMgrResponse` containing MIME
type and HTML body. The servlet routes to the appropriate page class based on URL path.

Handlers expose themselves to the manager via `ShellyManagerInterface`, which
`ShellyBaseHandler` implements. `ShellyThingTable` holds the registry of all active
handlers, making them accessible from the servlet without direct coupling.

---

## 15. Resources

### Repository

The binding lives in the openHAB addons monorepo:
<https://github.com/openhab/openhab-addons/tree/main/bundles/org.openhab.binding.shelly>

### Official Shelly API Documentation

- **Gen1 REST API** — <https://shelly-api-docs.shelly.cloud/gen1/>
- **Gen2 / Gen3 / Gen4 RPC API** — <https://shelly-api-docs.shelly.cloud/gen2/>
- **BLU device data** — embedded in the Gen2 BLU gateway notification spec

### BTHome Standard

BLU device advertisements use the BTHome v2 open standard. The format specification
documents all object IDs, data types, encoding rules, and encryption:

- **Format spec** — <https://bthome.io/format/>
- **Object ID list** — full sensor, binary sensor, and event IDs with data types
  and scale factors; the decimal values of these IDs appear as field keys in the
  `BluScan.Data` RPC notification forwarded by the gateway

### Shelly Knowledge Base

Device specifications, parameters, wiring diagrams, and firmware changelogs for all
Shelly products:
<https://kb.shelly.cloud/knowledge-base/devices>

### Sample API Responses

Raw JSON payloads captured from real devices, organised by generation. Use these as a
reference when adding a new device model or mapping a new DTO field — they show exactly
what the device returns, including optional/firmware-version-dependent fields.

| File | Coverage |
|---|---|
| [DEVNOTES - API JSON Samples.md](https://github.com/markus7017/myfiles/blob/master/shelly/DEVNOTES%20-%20API%20JSON%20Samples.md) | Gen1 (`/settings`, `/status`, `/shelly`, …) |
| [DEVNOTES - Gen2 API JSON Samples.md](https://github.com/markus7017/myfiles/blob/master/shelly/DEVNOTES%20-%20Gen2%20API%20JSON%20Samples.md) | Gen2 RPC responses and push notifications |
| [DEVNOTES - Gen3 API JSON Samples.md](https://github.com/markus7017/myfiles/blob/master/shelly/DEVNOTES%20-%20Gen3%20API%20JSON%20Samples.md) | Gen3 RPC responses and push notifications |
| [DEVNOTES - Gen4 API JSON Samples.md](https://github.com/markus7017/myfiles/blob/master/shelly/DEVNOTES%20-%20Gen4%20API%20JSON%20Samples.md) | Gen4 RPC responses and push notifications |
| [DEVNOTES - BLU API JSON Samples.md](https://github.com/markus7017/myfiles/blob/master/shelly/DEVNOTES%20-%20BLU%20API%20JSON%20Samples.md) | BLU BLE advertisement and gateway RPC messages |

> **Note:** Samples reflect a specific firmware version at the time of capture. Fields
> may be added or renamed across firmware releases — always cross-check against the
> official API documentation when in doubt.

### Translations

openHAB uses Crowdin for all binding translations. The `shelly_de.properties` file (and
all other locale files) is generated from Crowdin — **do not edit it manually**. To
contribute or review translations, use the Crowdin web editor:
<https://crowdin.com/editor/openhab-addons/1200/en-de?view=comfortable&filter=basic&value=5>
