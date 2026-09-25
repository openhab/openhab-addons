---
children:
  - ["doc/filtering", "Device & Entity Filtering"]
  - ["doc/entities", "Supported Entity Mappings"]
---

# Home Assistant Link Binding

The Home Assistant Link binding (`hasslink`) connects to a Home Assistant instance via its native WebSocket API, exposing Home Assistant devices and entities as native openHAB Things, Channels, and Items with real-time, bi-directional synchronization.

While openHAB offers robust scripting, flexible rule engines, and tailored UIs, Home Assistant's massive contributor base rapidly adds support for new smart home hardware and niche integrations.
This binding bridges that gap—allowing you to use Home Assistant as an integration backend for hard-to-integrate devices while keeping openHAB as your primary home automation platform.

[[toc]]

## Features

- **Vast Ecosystem Expansion:** Instantly extends openHAB compatibility to thousands of Home Assistant-supported integrations and hardware.
- **Native WebSocket Communication:** Connects directly to Home Assistant's built-in `/api/websocket`—no extra integrations, add-ons, or Home Assistant-side modifications required.
- **Automatic Unit Conversion:** Maps Home Assistant units of measurement (`°C`, `°F`, `W`, `kWh`, `hPa`, etc.) directly to openHAB `QuantityType` items (`Number:Temperature`, `Number:Power`, etc.).
- **Dynamic Channel Generation:** Dynamically creates channels based on entity state attributes upon discovery or configuration.
- **Unified Channel Naming:** Predictable channel structure using `domain-entity_name` for primary channels and `domain-entity_name#attribute` for secondary attributes.
- **Bi-directional Control:** Full command mapping for switches, dimmers, colors, climate controls, covers, fans, media players, and more.
- **Rule Engine Actions:** Execute Home Assistant service calls, trigger buttons, run scripts, and activate scenes directly from openHAB automation rules.

## Prerequisites & Setup

### 1. Home Assistant Instance

You need a running Home Assistant instance on your local network. If you do not already have one, it can be run as a lightweight container alongside openHAB.

For full installation options, see the official [Home Assistant Installation Guide](https://www.home-assistant.io/installation/).

#### Minimal Docker Compose Example

If running in Docker, a basic setup requires `network_mode: host` to allow mDNS discovery and local API connectivity:

```yaml
version: '3.8'

services:
  homeassistant:
    container_name: homeassistant
    image: ghcr.io/home-assistant/home-assistant:stable
    volumes:
      - /opt/homeassistant/config:/config
      - /etc/localtime:/etc/localtime:ro
    restart: unless-stopped
    network_mode: host
```

### 2. Generate a Long-Lived Access Token

openHAB authenticates with Home Assistant using a token:

1. Open your Home Assistant UI in a web browser.
1. Click your user profile icon (bottom-left corner).
1. Select the **Security** tab at the top of the profile page.
1. Scroll down to the **Long-Lived Access Tokens** section.
1. Select **Create Token**, enter a name (e.g., `openHAB Link`), and copy the generated token string.
1. Store this token securely. You will provide it when configuring the `hasslink:server` Thing in openHAB.

## Discovery

The binding supports automatic discovery for both the Home Assistant server itself and its connected entities.

### Server Bridge Discovery (mDNS)

If Home Assistant is running on your local network with mDNS enabled, openHAB will automatically discover the Home Assistant server instance:

1. Go to **Settings → Things → Inbox** in openHAB.
1. Choose the discovered Home Assistant server from the Inbox.
1. Enter your **Long-Lived Access Token** in the Bridge configuration and save.

### Entity & Device Discovery

Once the `server` Bridge is online, device discovery runs automatically:

1. The binding queries the Home Assistant WebSocket API and device/entity registries.
1. Discovered physical and logical hardware devices appear in your openHAB **Inbox** as Things (e.g., `hasslink:device:XXXXX:XXXXX`).
1. Adding a Thing from the Inbox automatically generates its primary channel and any mapped attribute channels.
1. **Real-Time Additions:** Devices added to Home Assistant while openHAB is running are automatically detected and added to the Inbox without requiring a restart.

> **Filtered Discovery & Manual Scans:**
>
> To prevent cluttering your openHAB Inbox with hundreds of virtual helpers, automations, scripts, persons, and system domains that are typically of lower direct interest, auto-discovery focuses exclusively on physical and logical devices by default.
> Skipped entities and independent items can still be added manually or configured via discovery filter options on the Bridge.
>
> If you add or rename entities in Home Assistant and want to refresh the Inbox immediately, go to **Settings → Things → + (Add)**, select **Home Assistant Link Binding**, and trigger a manual scan.

## Thing Configuration

### Bridge (`server`)

The `server` Bridge manages the persistent WebSocket connection to Home Assistant Core and controls global Inbox discovery filters. Standard wildcard glob patterns (`*`, `?`) are supported across area, domain, and label filters.

| Parameter                   | Type            | Default   | Required | Description                                                                                           |
|:----------------------------|:----------------|:----------|:---------|:------------------------------------------------------------------------------------------------------|
| `host`                      | Text            | N/A       | Yes      | Hostname or IP address of Home Assistant                                                              |
| `port`                      | Integer         | `8123`    | No       | WebSocket/HTTP port                                                                                   |
| `token`                     | Text            | N/A       | Yes      | Home Assistant Long-Lived Access Token                                                                |
| `secure`                    | Boolean         | `false`   | No       | Use TLS/SSL (`wss://`)                                                                                |
| `ignoreIndependentEntities` | Boolean         | `true`    | No       | Exclude entities without a parent device (helpers, automations, person tracking) from Inbox discovery |
| `includedAreas`             | Text (Multiple) | `[]`      | No       | Whitelist of Home Assistant Area IDs to discover (supports glob patterns, e.g., `*bedroom*`)          |
| `excludedAreas`             | Text (Multiple) | `[]`      | No       | Blacklist of Home Assistant Area IDs to exclude from discovery (supports glob patterns)               |
| `includedDomains`           | Text (Multiple) | `[]`      | No       | Whitelist of Home Assistant domains to discover (supports glob patterns, e.g., `light*`)              |
| `excludedDomains`           | Text (Multiple) | See below | No       | Blacklist of Home Assistant domains to exclude from discovery (supports glob patterns)                |
| `includedLabels`            | Text (Multiple) | `[]`      | No       | Whitelist of Home Assistant Labels/Tags to discover (supports glob patterns, e.g., `openhab*`)        |
| `excludedLabels`            | Text (Multiple) | `[]`      | No       | Blacklist of Home Assistant Labels/Tags to ignore during discovery (supports glob patterns)           |

> **Default Excluded Domains:** `automation`,`conversation`,`counter`,`media_source`,`persistent_notification`,`stt`,`sun`,`timer`,`tts`,`zone`

### Device / Entity Container (`device`)

The `device` Thing maps one or more Home Assistant entities to openHAB channels and provides per-Thing channel creation filters. Standard wildcard glob patterns (`*`, `?`) are supported across all filter fields.

| Parameter          | Type            | Default | Required | Description                                                                                                     |
|:-------------------|:----------------|:--------|:---------|:----------------------------------------------------------------------------------------------------------------|
| `deviceId`         | Text            | N/A     | No       | Home Assistant device registry identifier                                                                       |
| `entityIds`        | Text (Multiple) | `[]`    | No       | Explicitly defined entity IDs or glob patterns; bypasses all device exclusions and filter rules                 |
| `includedDomains`  | Text (Multiple) | `[]`    | No       | Whitelist of Home Assistant domains to restrict channels to (supports glob patterns, e.g., `sensor*`)           |
| `excludedDomains`  | Text (Multiple) | `[]`    | No       | Blacklist of Home Assistant domains to exclude from creating channels (supports glob patterns, e.g., `update*`) |
| `includedLabels`   | Text (Multiple) | `[]`    | No       | Whitelist of Home Assistant Labels/Tags for child entities (supports glob patterns)                             |
| `excludedLabels`   | Text (Multiple) | `[]`    | No       | Blacklist of Home Assistant Labels/Tags for child entities (supports glob patterns)                             |
| `includedEntities` | Text (Multiple) | `[]`    | No       | Specific Entity IDs or glob patterns (e.g., `sensor.*bed*`, `*_temperature`) to explicitly allow as channels    |
| `excludedEntities` | Text (Multiple) | `[]`    | No       | Specific Entity IDs or glob patterns (e.g., `*battery*`, `*_rssi`) to exclude from channel creation             |

> **Note:** At least one of `deviceId` or `entityIds` must be defined.

For full evaluation semantics and details on composite filtering, see [filtering](doc/filtering.md).

::: tip
The device ID can be obtained from **Settings → Devices & Services → Devices**.
Click on the device you want to use.

The device ID is the final segment of the URL.
For example:

`https://homeassistant.local/config/devices/device/5a471c1a7211f02d76869838fe451d68`

The device ID is:

`5a471c1a7211f02d76869838fe451d68`
:::

## Supported Entity Domains

The binding includes dedicated mapping logic for over 35 Home Assistant entity domains:

- **Lighting & Power:** `light`, `switch`, `button`, `input_button`, `scene`, `script`
- **Sensors:** `sensor`, `binary_sensor`, `device_tracker`, `geolocation`, `event`
- **Climate & Environment:** `climate`, `humidifier`, `water_heater`, `weather`, `fan`
- **Covers & Locks:** `cover`, `lock`, `valve`
- **Media & Remotes:** `media_player`, `remote`, `camera`, `image`
- **Controls & Inputs:** `number`, `input_number`, `select`, `input_select`, `text`, `input_text`, `date`, `time`, `datetime`
- **Robotics & Vacuum:** `vacuum`, `lawn_mower`, `siren`
- **System & Infrastructure:** `update`, `alarm_control_panel`, `infrared`, `radio_frequency`

## Channel Types & Event Handling

The binding utilizes standard openHAB channel types mapped dynamically across entities:

- **Standard State Channels:** `color`, `contact`, `datetime`, `dimmer`, `image`, `location`, `number`, `player`, `rollershutter`, `string`, `switch`
- **Event Channel (`event`):** Used exclusively for the Home Assistant `event` entity domain.

### Event Channel Behavior (`event` Domain)

For Home Assistant `event` entities, `event` acts purely as an openHAB **Trigger channel**:

- **Rule Triggers:** Incoming Home Assistant events fire openHAB channel trigger events directly (e.g., `channel "hasslink:..." triggered`).
- **Item Linking:** If you wish to post commands or update Item states from incoming events, link the channel to an Item using standard openHAB Profiles or rules.

## Channel Naming Conventions

Channels are generated dynamically using the following structure:

- **Primary Channel:** `domain-entity_name` (e.g., `light-living_room_light`, `sensor-living_room_temperature`)
  Exposes the primary state of the entity (e.g., `ON`/`OFF`, numeric values, or string states).

- **Domain Attribute Channel:** `domain-entity_name#attribute_name` (e.g., `light-living_room_light#color_temp`, `climate-hallway#target_temperature`)
  Exposes first-class, strongly typed mapped attributes explicitly handled by specific entity implementations.

- **Generic Attribute Channel (Sensor Entities):** `sensor-entity_name#attribute_name` (e.g., `sensor-weather_station#pressure_trend`)
  Auto-discovered fallback attributes generated exclusively for sensor entities from the Home Assistant state payload when not mapped as primary channels.
  List and map attributes are provided as JSON strings.

- **Raw JSON Attributes Channel:** `domain-entity_name#json_attributes`
  Exposes the entire Home Assistant entity attributes payload as a serialized JSON string.

  > **Note:** This channel is **not created automatically**.
  To opt in, you must manually create a custom channel with the following configuration parameters:

  - `entityId: <entity_id>`
  - `attribute: json_attributes`

  > Once defined, payload serialization is evaluated lazily and only executes when an Item is actively linked to the channel.

## Supported Entity Domains & Mapping Logic

The binding provides out-of-the-box support for over 35 Home Assistant entity domains.
While standard scalar entities (such as simple sensors, switches, or selects) map symmetrically between Home Assistant states and openHAB items, more complex domains require specialized handling for state inversions, dynamic command generation, or parameterized action service calls.

For full details on domain-specific channel structures, command syntax (e.g., alarm PIN codes, vacuum segment cleaning), percentage inversions (e.g., covers), and attribute channel generation, see the **[Home Assistant Entity Mapping Guide](doc/entities.md)**.

## Rule Actions

The binding exposes actions scoped to the `server` Bridge (`hasslink:server`).
Actions can be retrieved in openHAB automation rules using the action identifier `"hasslink"` and the bridge UID.

### Action Summary

| Method                                                                                                               | Description                                                                 |
|:---------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------|
| [callService](#callservice)(`String` domain, `String` service, `String` entityId, `Map`<`String`, `Object`> payload) | Invokes any native Home Assistant service via the WebSocket connection.     |
| [pressButton](#pressbutton)(`String` entityId)                                                                       | Triggers a Home Assistant button entity (`button` or `input_button`).       |
| [runScript](#runscript)(`String` scriptId, `Map`<`String`, `Object`> variables)                                      | Executes a Home Assistant script directly by service name.                  |
| [activateScene](#activatescene)(`String` sceneId)                                                                    | Activates a Home Assistant scene using the generic `scene.turn_on` service. |

### Action Details

---

#### callService

`callService(String domain, String service, String entityId, Map<String, Object> payload)`

Invokes any native Home Assistant service via the WebSocket connection.

- `domain` _(String, required)_: The target Home Assistant service domain (e.g. `"light"`, `"notify"`, `"climate"`).
- `service` _(String, required)_: The target service action (e.g. `"turn_on"`, `"persistent_notification"`, `"set_temperature"`).
- `entityId` _(String, optional)_: Fully qualified entity ID (`"domain.object_id"`) or `null` if the service does not target a specific entity.
- `payload` _(Map<String, Object>, optional)_: Key-value parameters passed to the service payload or `null`.

---

#### pressButton

**Syntax:** `pressButton(String entityId)`

Triggers a Home Assistant button entity (`button` or `input_button`).

- `entityId` _(String, required)_: The target entity ID. Accepts a fully qualified entity ID (e.g., `"button.reboot"`, `"input_button.reset"`) or a shorthand object ID (e.g., `"reboot"`), which automatically defaults to the `button` domain (`"button.reboot"`).

---

#### runScript

**Syntax:** `runScript(String scriptId, Map<String, Object> variables)`

Executes a Home Assistant script directly by service name.

- `scriptId` _(String, required)_: Fully qualified script ID (e.g., `"script.welcome_home"`) or simple script name (e.g., `"welcome_home"`).
- `variables` _(Map<String, Object>, optional)_: Execution variables passed to the script payload or `null`.

---

#### activateScene

**Syntax:** `activateScene(String sceneId)`

Activates a Home Assistant scene using the generic `scene.turn_on` service.

- `sceneId` _(String, required)_: Fully qualified scene ID (e.g., `"scene.movie_night"`) or simple scene name (e.g., `"movie_night"`).

---

### Rule Examples

:::: tabs

::: tab DSL

```java
rule "Trigger Home Assistant Script & Actions"
when
    Item LivingRoom_SceneSwitch received command ON
then
    val actions = getActions("hasslink", "hasslink:server:homeassistant")
    if (actions === null) {
        logWarn("rules", "HassLink actions not available")
        return;
    }

    // Activate a scene (shorthand or full entity ID)
    actions.activateScene("movie_night")

    // Press a button
    actions.pressButton("button.reboot_router")

    // Run a script with variables
    actions.runScript("welcome_home", Map.of("brightness", 80))

    // Call arbitrary HA service
    actions.callService("notify", "persistent_notification", null, Map.of(
        "title", "openHAB Alert",
        "message", "Motion detected in hallway"
    ))
end
```

:::

::: tab JS

```javascript
const actions = actions.get("hasslink", "hasslink:server:homeassistant");

if (actions) {
  // Activate a scene
  actions.activateScene("movie_night");

  // Trigger a button entity
  actions.pressButton("reboot_router");

  // Run script with parameters
  actions.runScript("script.welcome_home", { brightness: 100, color: "red" });

  // Custom service call
  actions.callService("light", "turn_on", "light.living_room", { brightness_pct: 75 });
}
```

:::

::: tab JRuby

```ruby
thing = things["hasslink:server:homeassistant"]

# Activate scene
thing.activate_scene("movie_night")

# Press button
thing.press_button("button.reboot_router")

# Run script with parameters
thing.run_script("welcome_home", { "brightness" => 80 })

# Generic service call
thing.call_service("climate", "set_temperature", "climate.hallway", { "temperature" => 21.5 })
```

:::

:::

## Textual Configuration Examples

### `things/homeassistant.things`

```java
Bridge hasslink:server:homeassistant "Home Assistant Core" [
    host="192.168.1.100",
    port=8123,
    token="eyJhbGciOiJIUzI1NiIsInR...",
    secure=false,
    ignoreIndependentEntities=true,
    includedLabels="openhab*"
] {
    // Single entity mapping
    Thing device desk_light "Desk Lamp" [
        entityIds="light.desk_lamp"
    ]

    // Physical device mapping via Home Assistant Device ID with wildcard exclusions for static noise
    Thing device lawn_mower "Lawn Mower" [
        deviceId="3a4b5c6d7e8f90123456789abcdef012",
        excludedEntities="*_version", "*_mac", "*_fw_*"
    ]

    // Grouping multiple standalone entities into one Thing
    Thing device office_environment "Office Environment" [
        entityIds=
            "sensor.office_temperature",
            "sensor.office_humidity",
            "switch.office_fan"
    ]
}
```

### `items/homeassistant.items`

```java
// Light Controls
Switch DeskLamp_Power "Desk Lamp Power" <light> { channel="hasslink:device:homeassistant:desk_light:light-desk_lamp" }
Dimmer DeskLamp_Brightness "Desk Lamp Brightness" <light> { channel="hasslink:device:homeassistant:desk_light:light-desk_lamp#brightness" }

// Environment Sensors (Automated QuantityType)
Number:Temperature Office_Temp "Office Temperature [%.1f %unit%]" <temperature> { channel="hasslink:device:homeassistant:office_environment:sensor-office_temperature" }
Number:Dimensionless Office_Humidity "Office Humidity [%.0f %%]" <humidity> { channel="hasslink:device:homeassistant:office_environment:sensor-office_humidity" }

// Climate Controls
String AC_Mode "AC Mode" <climate> { channel="hasslink:device:homeassistant:living_room_ac:climate-living_room_ac" }
Number:Temperature AC_TargetTemp "Target Temperature [%.1f %unit%]" <temperature> { channel="hasslink:device:homeassistant:living_room_ac:climate-living_room_ac#target_temperature" }
```
