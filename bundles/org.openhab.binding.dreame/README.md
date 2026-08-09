# Dreame Binding

This binding integrates Dreame robotic lawn mowers with openHAB through the Dreamehome cloud.
It uses the Dreamehome HTTPS API for authentication, discovery, commands and periodic status polling, and the Dreame MQTT service for live updates.

> **Development status:** The Dreame A1 Pro 2000 (`dreame.mower.g2540d`) has been tested with firmware `4.3.6_0623` in the European cloud region, including the `start`, `pause`, `stop` and `dock` commands.
> Other mower models use the same protocol family but still require testing.

## Supported Things

| Thing     | Type     | Description                                      |
|-----------|----------|--------------------------------------------------|
| `account` | Bridge   | One Dreamehome cloud account                     |
| `mower`   | Thing    | A mower associated with the Dreamehome account   |

The binding accepts devices whose model identifier starts with `dreame.mower.`.
Known mower identifiers include A1 (`dreame.mower.p2255`), A1 Pro (`dreame.mower.g2422` and `dreame.mower.g2540d`), A2 (`dreame.mower.g2408`), A2 1200 (`dreame.mower.g2568a`) and A3 (`dreame.mower.g3255`).
Only `dreame.mower.g2540d` is currently confirmed with a physical mower.

## Discovery

First add a Dreamehome account bridge with the same credentials and country used in the Dreamehome app.
After the bridge becomes online, mower Things are added to the inbox automatically.
A manual inbox scan can be used to repeat discovery.

## Thing Configuration

| Thing     | Parameter         | Required | Default | Description                                         |
|-----------|-------------------|----------|---------|-----------------------------------------------------|
| `account` | `username`        | yes      | N/A     | Dreamehome email address or phone number             |
| `account` | `password`        | yes      | N/A     | Dreamehome password                                  |
| `account` | `country`         | yes      | `de`    | Two-letter account country code                      |
| `mower`   | `deviceId`        | yes      | N/A     | Dreame cloud device identifier, normally discovered  |
| `mower`   | `refreshInterval` | no       | `30`    | HTTPS fallback polling interval in seconds            |

European country codes such as `de`, `fr` or `nl` are routed to the Dreame European cloud.
The minimum refresh interval is 15 seconds.

## Channels

Availability of properties marked as model-dependent is determined by the mower firmware.
An unsupported property is reported as `UNDEF`.

| Channel                 | Type                 | Access | Description                                      |
|-------------------------|----------------------|--------|--------------------------------------------------|
| `command`               | String               | W      | `start`, `pause`, `stop` or `dock`               |
| `state`                 | String               | R      | Current mower state                              |
| `battery-level`         | Number:Dimensionless | R      | Battery level in percent                         |
| `charging-status`       | String               | R      | Current charging state                           |
| `error-code`            | String               | R      | Proprietary device status or error code          |
| `firmware`              | String               | R      | Installed firmware version                       |
| `do-not-disturb`        | Switch               | RW     | Do not disturb setting; model-dependent          |
| `current-zone`          | String               | R      | Active mowing region identifier                  |
| `mowing-progress`       | Number:Dimensionless | R      | Progress of the current mowing task              |
| `planned-mowing-area`   | Number:Area          | R      | Planned area of the current mowing task          |
| `current-mowed-area`    | Number:Area          | R      | Area completed during the current mowing task    |
| `position-x`            | Number               | R      | X coordinate in the Dreame map coordinate system |
| `position-y`            | Number               | R      | Y coordinate in the Dreame map coordinate system |
| `heading`               | Number               | R      | Mower heading in degrees                         |
| `docking-state`         | String               | R      | Relationship to the charging station             |
| `location-state`        | Number               | R      | Proprietary Dreame location state                |
| `wifi-rssi`             | Number               | R      | Wi-Fi signal strength in dBm                     |
| `ble-rssi`              | Number               | R      | Bluetooth signal strength in dBm, if available   |
| `lte-rssi`              | Number               | R      | LTE signal strength in dBm, if available         |
| `task-executable`       | Switch               | R      | Proprietary task executable flag                 |
| `task-active`           | Switch               | R      | Active work sequence reported by the mower       |
| `task-operation`        | Number               | R      | Proprietary Dreame task operation code           |
| `task-state`            | String               | R      | State derived from observed task operation codes  |
| `task-time`             | Number               | R      | Raw undocumented task time field                  |
| `current-map-id`        | Number               | R      | Identifier of the active map                      |
| `maps`                  | String               | R      | Available map descriptors as JSON                 |
| `zones`                 | String               | R      | Mowing-zone descriptors as JSON                   |
| `map-svg`               | Image                | R      | Rendered SVG image of the active map              |
| `zone-mowing`           | String               | W      | Start selective mowing using zone IDs              |
| `cutting-height`        | Number (cm)          | R/W    | Map-wide cutting height in 0.5 cm steps            |
| `mowing-sessions`       | Number               | R      | Lifetime mowing sessions; model-dependent        |
| `total-mowing-time`     | Number:Time          | R      | Lifetime mowing time; model-dependent            |
| `total-mowed-area`      | Number:Area          | R      | Lifetime mowed area; model-dependent             |

The `command` channel is stateless and therefore normally displays `NULL`.
The `zone-mowing` channel is also stateless. Send one or more positive zone IDs separated by commas, for example `1`
or `1,2`. Before sending the task to the mower, the binding removes duplicate IDs and verifies every ID against the
zones of the active map. Empty, non-numeric, non-positive and unknown zone IDs are rejected.
BLE and LTE values are `UNDEF` when the mower reports its protocol sentinel for an unavailable radio.
The `error-code`, `task-operation` and `task-time` values are intentionally exposed without an inferred meaning where
Dreame does not publish a protocol definition.
The `task-active` channel is `ON` while an observed work sequence is running, including return-to-dock, and `OFF` while
it is paused or after the mower reaches the dock. Until the first task activity message arrives, the binding initializes
this channel from known mower states and leaves it unchanged for unknown states.
The `map-svg` channel contains a rendered SVG of the active map, including mowing boundaries, exclusion areas, zones,
the charging station, the mower position and its mowing path when those elements are supplied by the mower.
The `cutting-height` channel controls the map-wide cutting height from 3.0 cm to 7.0 cm in 0.5 cm steps. The binding
preserves unknown fields in the mower's map preferences and confirms a change by reading the value back from the device.

## Full Example

### Thing Configuration

```java
Bridge dreame:account:home "Dreamehome" [ username="name@example.com", password="secret", country="de" ] {
    Thing mower a1pro "Dreame A1 Pro" [ deviceId="123456789", refreshInterval=30 ]
}
```

The device ID is sensitive account data.
Prefer inbox discovery instead of copying it into a text configuration.

### Item Configuration

```java
String Dreame_Command "Command" { channel="dreame:mower:home:a1pro:command" }
String Dreame_State "State [%s]" { channel="dreame:mower:home:a1pro:state" }
Number Dreame_Battery "Battery [%.0f %%]" { channel="dreame:mower:home:a1pro:battery-level" }
Number:Dimensionless Dreame_Progress "Progress [%.2f %%]" { channel="dreame:mower:home:a1pro:mowing-progress" }
Number:Area Dreame_Area "Mowed [%.2f %unit%]" { channel="dreame:mower:home:a1pro:current-mowed-area" }
String Dreame_Docking "Docking [%s]" { channel="dreame:mower:home:a1pro:docking-state" }
Number Dreame_WiFi "Wi-Fi [%.0f dBm]" { channel="dreame:mower:home:a1pro:wifi-rssi" }
Number Dreame_CuttingHeight "Cutting Height [%.1f cm]" { channel="dreame:mower:home:a1pro:cutting-height" }
String Dreame_ZoneMowing "Zone Mowing" { channel="dreame:mower:home:a1pro:zone-mowing" }
String Dreame_Zones "Zones [%s]" { channel="dreame:mower:home:a1pro:zones" }
Image Dreame_Map "Map" { channel="dreame:mower:home:a1pro:map-svg" }
```

Example commands:

```java
Dreame_Command.sendCommand("start")
Dreame_Command.sendCommand("pause")
Dreame_Command.sendCommand("dock")
Dreame_ZoneMowing.sendCommand("1")
Dreame_CuttingHeight.sendCommand(4.5)
```

## Communication

The binding refreshes the Dreamehome OAuth token before expiry.
HTTPS polling remains active as a fallback while MQTT supplies low-latency changes for state, battery, charging, position and task progress.
The MQTT connection uses TLS with hostname verification and the Dreame root certificate bundled with the binding.

No credentials, access tokens, refresh tokens or complete device identifiers are written to the log.

## Troubleshooting

For normal diagnostics, enable DEBUG logging:

```text
log:set DEBUG org.openhab.binding.dreame
```

TRACE includes sanitized cloud payloads and MQTT protocol frames and should only be enabled temporarily:

```text
log:set TRACE org.openhab.binding.dreame
```

Restore the default level afterward:

```text
log:set INFO org.openhab.binding.dreame
```

If login fails for a European account, configure the actual two-letter country code such as `de`, not the cloud region name.
If a newly added channel does not appear after updating a development JAR, disable and re-enable the Thing or restart the binding so openHAB reloads the Thing description.

## Known Limitations

- Commands are not yet verified on models other than `dreame.mower.g2540d`.
- Lifetime statistics and do not disturb are not exposed by all mower firmware versions.
- Map details depend on the geometry and metadata supplied by the mower firmware.
- Position values use the native Dreame map coordinate system and are not geographic coordinates.
- The integration depends on Dreamehome cloud services and may require updates if their private API changes.

## Protocol References

Protocol behavior was compared with the MIT-licensed [nicolasglg/dreame-mower-a1-pro](https://github.com/nicolasglg/dreame-mower-a1-pro) project and the mower protocol documentation in [TA2k/ioBroker.dreame](https://github.com/TA2k/ioBroker.dreame/blob/main/docs/mower-protocol.md).
No source code from these projects is copied into this binding.
