# Dreame Binding

This binding integrates supported Dreame robotic vacuums and Dreame or MOVA robotic lawn mowers with openHAB through
the Dreamehome or MOVAhome cloud.
It uses the selected cloud service for authentication, discovery, commands, periodic status polling and MQTT live updates.

> **Development status:** The Dreame A1 Pro 2000 (`dreame.mower.g2540d`) has been tested with firmware `4.3.6_0623` in the European cloud region, including the `start`, `pause`, `stop` and `dock` commands.
> The MOVA 1000 (`mova.mower.g2405c`) has been community-tested with MOVAhome, including discovery, MQTT updates,
> commands, zone mowing and map rendering.
> The MOVA LiDAX 1200 (`mova.mower.g2529d`) has been community-tested with MOVAhome, including commands, MQTT
> updates, task state, docking state and the statistics refresh after returning to the dock.
> The MOVA LiDAX Ultra 1600 AWD (`mova.mower.g2584d`) has also been community-tested with MOVAhome.
> Other mower models use the same protocol family but still require testing.

## Vacuum Support

The binding recognizes model identifiers in the `dreame.vacuum.*` family as vacuum candidates.
Discovery of the L50 Ultra Pro (`dreame.vacuum.r9445d`) has been confirmed by a community tester.
MQTT subscription and reception during cleaning, pause and return-to-dock have also been confirmed.
Cleaning, pause, return-to-dock and charging codes have been correlated with a community test on this model.
Discovery offers a separate `vacuum` Thing in the inbox. Models with a protocol mapping in the
[Home Assistant Dreame integration](https://github.com/foXaCe/dreame-vacuum/blob/main/docs/supported_devices.md)
use the common Dreame property and action mappings and open an MQTT subscription with the existing account credentials
and TLS settings. Other vacuum candidates remain visible in discovery, but their Thing reports that no verified protocol mapping exists.
A supported Thing starts as `UNKNOWN` until valid device status arrives.
Mower Things continue to use their existing discovery, commands and MQTT processing.

For discovery diagnostics, enable TRACE for `org.openhab.binding.dreame.internal.handler.DreameVacuumHandler`,
then initialize the vacuum Thing (or reconnect its account bridge).
The diagnostic line contains the validated cloud model identifier and flags indicating whether firmware,
owner and broker metadata are present. It omits their values, device IDs, names, tokens, maps and room data.
The shared HTTP transport also omits device-list response bodies, including unknown fields and embedded JSON.
The same handler logger emits `Vacuum MQTT diagnostics` when the subscription succeeds, fails, or receives a message.
Message summaries include known method names, numeric service/property addresses and value types.
For mapped vacuum models, `properties_changed` also includes bounded integer values for these candidate fields:

| Address (siid/piid) | Candidate meaning | Diagnostic value range |
|--------------------|-------------------|------------------------|
| `2/1` | Device state | 0–255 |
| `2/2` | Error code | 0–65535 |
| `3/1` | Battery level | 0–100 |
| `3/2` | Charging status | 0–255 |
| `4/1` | Operating status | 0–255 |
| `4/2` | Cleaning time | 0–65535 |
| `4/3` | Cleaned area | 0–65535 |
| `4/4` | Suction level | 0–255 |
| `4/5` | Water volume | 0–255 |
| `4/7` | Task status | 0–255 |
| `4/23` | Cleaning mode | 0–255 |
| `4/25` | Base status | 0–255 |
| `4/40` | Drying time | 0–255 |
| `9/1`, `9/2` | Main brush service time and percentage | 0–65535 / 0–100 |
| `10/1`, `10/2` | Side brush service time and percentage | 0–65535 / 0–100 |
| `11/1`, `11/2` | Filter percentage and service time | 0–100 / 0–65535 |
| `12/2`–`12/4` | Lifetime cleaning statistics | 0–65535 |
| `15/3` | Auto-empty availability | 0–255 |
| `16/1`, `16/2` | Sensor service percentage and time | 0–100 / 0–65535 |
| `18/1`, `18/2` | Mop-pad service percentage and time | 0–100 / 0–65535 |
| `20/1`, `20/2` | Detergent percentage and time | 0–100 / 0–65535 |

Addresses were cross-checked against the [Tasshack/dreame-vacuum property mapping](https://github.com/Tasshack/dreame-vacuum/blob/dev/custom_components/dreame_vacuum/dreame/types.py).
The ranges are diagnostic limits, not declarations of supported enum values.
Diagnostics retain raw numeric codes. Channels assign names to observed states using the reference mapping.
String values (including numeric strings), arrays, objects, out-of-range numbers and failed property results are omitted,
except for the bounded `4/50` structured setting value used by CleanGenius and Cleaning Route.
All other property values, unknown field names, raw payloads and MQTT topics remain excluded. Payloads over 64 KiB or 32 nesting levels are omitted;
property summaries are limited to 32 entries per message.
Subscription success confirms broker access only. A valid status push or property response sets the Thing to `ONLINE`.
Every 60 seconds the binding checks the subscription and refreshes account credentials when necessary.
Failed connections are replaced, and changes to credentials cause the subscription to be recreated.
Disabling the Thing or taking its bridge offline stops the subscription and retry task.

The binding queries the supported status and setting properties on initialization and at the configured
`refreshInterval` through the cloud HTTP API (60 seconds by default).
A failed query sets the Thing to `OFFLINE` unless a newer valid push arrived during that query; the next valid update restores `ONLINE`.
Community testing confirmed ONLINE status, periodic property queries, MQTT updates and continued operation of the
supported commands and settings on the L50 Ultra Pro.
Explicit control commands and setting changes also use the cloud HTTP API; MQTT remains subscription-only.

### Vacuum Channels

| Channel | Item type | Access | Meaning |
|---------|-----------|--------|---------|
| `command` | String | RW | Send a cleaning, dock or station command; retains the last acknowledged command |
| `battery-level` | Number:Dimensionless | R | Last reported battery percentage (0–100) |
| `state` | String | R | Device state, including cleaning, mopping, docking, washing, drying and maintenance transitions |
| `operating-status` | String | R | Operating status, including cleaning, mapping, remote-control and monitoring activities |
| `charging-status` | String | R | `NOT_CHARGING`, `RETURNING`, `CHARGING`, `CHARGING_COMPLETED` |
| `error-code` | Number | R | Raw numeric error code; 0 was observed during normal cleaning |
| `cleaning-time` | Number:Time | R | Elapsed cleaning time in minutes |
| `cleaned-area` | Number:Area | R | Area cleaned during the current task in square metres |
| `suction-level` | String | RW | Suction level: `QUIET`, `STANDARD`, `STRONG`, `TURBO` |
| `water-volume` | String | RW | Water volume: `LOW`, `MEDIUM`, `HIGH` |
| `task-status` | String | R | Current task, including automatic, room and zone cleaning and their paused variants |
| `cleaning-mode` | String | RW | Cleaning mode: `SWEEPING`, `MOPPING`, `SWEEPING_AND_MOPPING` |
| `clean-genius` | String | RW | CleanGenius mode: `OFF`, `ROUTINE`, `DEEP` |
| `cleaning-route` | String | RW | Cleaning route: `STANDARD`, `QUICK` |
| `drying-time` | String | RW | Mop drying time: `2H`, `3H`, `4H` |
| `main-brush-left` | Number:Dimensionless | R | Main brush service life remaining in percent |
| `main-brush-time-left` | Number:Time | R | Main brush service time remaining in hours |
| `side-brush-left` | Number:Dimensionless | R | Side brush service life remaining in percent |
| `side-brush-time-left` | Number:Time | R | Side brush service time remaining in hours |
| `filter-left` | Number:Dimensionless | R | Filter service life remaining in percent |
| `filter-time-left` | Number:Time | R | Filter service time remaining in hours |
| `sensor-dirty-left` | Number:Dimensionless | R | Remaining sensor-cleaning interval in percent |
| `sensor-dirty-time-left` | Number:Time | R | Remaining sensor-cleaning interval in hours |
| `mop-pad-left` | Number:Dimensionless | R | Mop pad service life remaining in percent |
| `mop-pad-time-left` | Number:Time | R | Mop pad service time remaining in hours |
| `detergent-left` | Number:Dimensionless | R | Detergent supply remaining in percent |
| `detergent-time-left` | Number:Time | R | Estimated detergent supply remaining in days |
| `total-cleaning-time` | Number:Time | R | Total cleaning time in minutes |
| `cleaning-count` | Number | R | Total number of cleaning jobs |
| `total-cleaned-area` | Number:Area | R | Total cleaned area in square metres |
| `base-status` | String | R | Mop washing and drying station state |
| `rooms` | String | R | Room segment IDs and names from the current map, for example `3=Kitchen, 7=Living Room` |
| `room-cleaning` | String | RW | Comma-separated room IDs, for example `3,7`; retains the last acknowledged selection |
| `map-png` | Image | R | Rendered PNG map for Basic UI and clients without reliable SVG support |
| `map-svg` | Image | R | Rendered vector SVG map |

Unknown state codes are preserved as `UNKNOWN_<code>`.
Device-state codes 19 and above differ between older and newer Dreame protocol variants. The binding selects the state
schema from the model and, for transitional models, the firmware build using the `NEW_STATE` capability in the
[reference device database](https://github.com/Tasshack/dreame-vacuum/blob/8556ef85132c3d73a5288dd00738636392dac633/custom_components/dreame_vacuum/dreame/const.py).
The corresponding old and new state tables follow the
[reference state mapping](https://github.com/Tasshack/dreame-vacuum/blob/8556ef85132c3d73a5288dd00738636392dac633/custom_components/dreame_vacuum/dreame/types.py).
Unknown models use the new schema, while unmapped values remain visible as `UNKNOWN_<code>`.
Values are updated independently as MQTT properties or query results arrive; missing or failed properties do not overwrite other channels.
A query does not overwrite a newer push received for the same channel while the query was running.
Channels remain `UNDEF` until their first valid update and are cleared when the subscription is replaced or the Thing/bridge is stopped.
Channels show the last received values and retain them if a property query fails.
`REFRESH` republishes cached values only and sends no device request.
Properties unavailable on a particular model remain `UNDEF`; a failed optional property does not prevent other status updates.
Maintenance, consumable and lifetime-statistics channels are marked as advanced in the Thing description.

### Vacuum Commands

The `command` channel accepts `START`, `PAUSE`, `DOCK`, `STOP`, `LOCATE`, `AUTO_EMPTY`, `WASH_MOPS`,
`PAUSE_WASHING`, `START_DRYING` and `STOP_DRYING` for mapped vacuum models.
The action and station-input mappings follow the
[Dreame vacuum reference implementation](https://github.com/foXaCe/dreame-vacuum/blob/main/custom_components/dreame_vacuum/dreame/types_properties.py).
The commands and writable setting channels have been exercised in community testing on the L50 Ultra Pro.
Room cleaning uses the most recently reported suction and water settings and falls back to `STANDARD` and `MEDIUM`
until those properties have been received. Room IDs are the numeric segment identifiers from the current map.
For example, send `START` to a String Item linked to `dreame:vacuum:<bridge>:<device>:command`.

Commands execute in order with up to eight waiting commands. Disabling the Thing or reconnecting its bridge discards waiting commands.
A request already dispatched cannot be recalled. Failed or timed-out commands are not automatically retried, because the robot may already have acted.
The device result must contain numeric code 0 for acknowledgement; status channel values continue to come from device property reports and queries.
The command channel retains the last command acknowledged by the vacuum; a failed command does not replace it.
Enable DEBUG for `org.openhab.binding.dreame.internal.handler.DreameVacuumHandler` to see command acknowledgements and failures.
`REFRESH` on the command channel sends nothing.

## Supported Things

| Thing     | Type     | Description                                      |
|-----------|----------|--------------------------------------------------|
| `account` | Bridge   | One Dreamehome or MOVAhome cloud account         |
| `mower`   | Thing    | A mower associated with the cloud account        |
| `vacuum`  | Thing    | Vacuum discovery, status, map and controls |

Mower discovery accepts devices whose model identifier starts with `dreame.mower.` or `mova.mower.`.
Known mower identifiers include A1 (`dreame.mower.p2255`), A1 Pro (`dreame.mower.g2422` and `dreame.mower.g2540d`), A2 (`dreame.mower.g2408`), A2 1200 (`dreame.mower.g2568a`) and A3 (`dreame.mower.g3255`).
Known MOVA identifiers include MOVA 600 (`mova.mower.g2405a`), MOVA 600 Kit (`mova.mower.g2405b`), MOVA 1000
(`mova.mower.g2405c`), MOVA LiDAX 1200 (`mova.mower.g2529d`) and MOVA LiDAX Ultra 1600 AWD
(`mova.mower.g2584d`).
The `dreame.mower.g2540d`, `mova.mower.g2405c`, `mova.mower.g2529d` and `mova.mower.g2584d` models are confirmed
with physical mowers.

## Discovery

First add an account bridge with the same credentials, country and cloud service used in the Dreamehome or MOVAhome app.
After the bridge becomes online, supported mower and vacuum Things are added to the inbox automatically.
A manual inbox scan can be used to repeat discovery.

## Thing Configuration

| Thing     | Parameter         | Required | Default | Description                                         |
|-----------|-------------------|----------|---------|-----------------------------------------------------|
| `account` | `cloudService`    | yes      | `dreamehome` | `dreamehome` or `movahome`                    |
| `account` | `username`        | yes      | N/A     | Email address or phone number used by the app        |
| `account` | `password`        | yes      | N/A     | Password used by the app                             |
| `account` | `country`         | yes      | `de`    | Two-letter account country code                      |
| `mower`   | `deviceId`        | yes      | N/A     | Cloud device identifier, normally discovered         |
| `mower`   | `refreshInterval` | no       | `30`    | HTTPS fallback polling interval in seconds            |
| `vacuum`  | `deviceId`        | yes      | N/A     | Cloud device identifier, normally discovered         |
| `vacuum`  | `refreshInterval` | no       | `60`    | HTTPS fallback polling interval in seconds            |

European country codes such as `de`, `fr` or `nl` are routed to the selected service's European cloud.
The minimum refresh interval is 15 seconds. MQTT continues to deliver immediate updates independently of this interval.

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
| `do-not-disturb`        | Switch               | RW²    | Do not disturb setting; model-dependent          |
| `do-not-disturb-active` | Switch               | R      | Whether the configured DND window is active      |
| `current-zone`          | String               | R      | Active mowing zone identifier                    |
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
| `current-map-id`        | Number               | R/W¹   | Identifier of the active map                      |
| `maps`                  | String               | R      | Available map descriptors as JSON                 |
| `zones`                 | String               | R      | Active-map mowing-zone descriptors as JSON        |
| `map-svg`               | Image                | R      | Rendered SVG image of the active map              |
| `map-png`               | Image                | R      | PNG map for mobile and Basic UI clients            |
| `zone-mowing`           | String               | W      | Start selective mowing using zone IDs              |
| `cutting-height`        | Number (cm)          | R/W    | Electronic map-wide cutting height; model-dependent |
| `mowing-sessions`       | Number               | R      | Lifetime mowing sessions; model-dependent        |
| `total-mowing-time`     | Number:Time          | R      | Lifetime mowing time; model-dependent            |
| `total-mowed-area`      | Number:Area          | R      | Lifetime mowed area; model-dependent             |

The `command` channel is stateless and therefore normally displays `NULL`.
Map IDs exposed by openHAB start at one. When the cloud reports a map change through MQTT, the binding reloads the
active map, mowing zones, rendered map and map-specific settings immediately.
The binding exposes the same rendered map as SVG and PNG. Use `map-svg` in clients with reliable SVG support and
`map-png` in the openHAB mobile apps or Basic UI if SVG rendering is unavailable or unstable.
The `zone-mowing` channel is also stateless. Send one or more positive zone IDs separated by commas, for example `1`
or `1,2`. Before sending the task to the mower, the binding removes duplicate IDs and verifies every ID against the
zones of the active map. Empty, non-numeric, non-positive and unknown zone IDs are rejected.
BLE and LTE values are `UNDEF` when the mower reports its protocol sentinel for an unavailable radio.
The `error-code`, `task-operation` and `task-time` values are intentionally exposed without an inferred meaning where
Dreame does not publish a protocol definition.
Mower state code `75` is shown as `paused_at_maintenance_point` and sets `task-active` to `OFF`.
The `task-active` channel is `ON` while an observed work sequence is running, including return-to-dock, and `OFF` while
it is paused or after the mower reaches the dock. Until the first task activity message arrives, the binding initializes
this channel from known mower states and leaves it unchanged for unknown states.
The `map-svg` channel contains a rendered SVG of the active map, including mowing boundaries, exclusion areas, zones,
the charging station, the mower position and its mowing path when those elements are supplied by the mower.
The `cutting-height` channel controls the map-wide cutting height from 3.0 cm to 7.0 cm in 0.5 cm steps. The binding
preserves unknown fields in the mower's map preferences and confirms a change by reading the value back from the device.
It is `UNDEF` for the MOVA 1000 (`mova.mower.g2405c`), which uses a mechanical cutting-height control.

## Full Example

### Thing Configuration

```java
Bridge dreame:account:home "Dreamehome" [ cloudService="dreamehome", username="name@example.com", password="secret", country="de" ] {
    Thing mower a1pro "Dreame A1 Pro" [ deviceId="123456789", refreshInterval=30 ]
    Thing vacuum l50 "Dreame L50 Ultra Pro" [ deviceId="987654321", refreshInterval=60 ]
}
```

The device ID is sensitive account data.
Prefer inbox discovery instead of copying it into a text configuration.

### Mower Item Configuration

```java
String Dreame_Command "Command" { channel="dreame:mower:home:a1pro:command" }
String Dreame_State "State [%s]" { channel="dreame:mower:home:a1pro:state" }
Number:Dimensionless Dreame_Battery "Battery [%.0f %%]" { channel="dreame:mower:home:a1pro:battery-level" }
Number:Dimensionless Dreame_Progress "Progress [%.2f %%]" { channel="dreame:mower:home:a1pro:mowing-progress" }
Number:Area Dreame_Area "Mowed [%.2f %unit%]" { channel="dreame:mower:home:a1pro:current-mowed-area" }
String Dreame_Docking "Docking [%s]" { channel="dreame:mower:home:a1pro:docking-state" }
Number Dreame_WiFi "Wi-Fi [%.0f dBm]" { channel="dreame:mower:home:a1pro:wifi-rssi" }
Number Dreame_CuttingHeight "Cutting Height [%.1f cm]" { channel="dreame:mower:home:a1pro:cutting-height" }
String Dreame_ZoneMowing "Zone Mowing" { channel="dreame:mower:home:a1pro:zone-mowing" }
String Dreame_Zones "Zones [%s]" { channel="dreame:mower:home:a1pro:zones" }
Image Dreame_Map "Map" { channel="dreame:mower:home:a1pro:map-svg" }
Image Dreame_MapPng "Map (PNG)" { channel="dreame:mower:home:a1pro:map-png" }
```

Example commands:

```java
Dreame_Command.sendCommand("start")
Dreame_Command.sendCommand("pause")
Dreame_Command.sendCommand("dock")
Dreame_ZoneMowing.sendCommand("1")
Dreame_CuttingHeight.sendCommand(4.5)
```

### Vacuum Item Configuration

```java
String Vacuum_Command "Vacuum Command [%s]" { channel="dreame:vacuum:home:l50:command" }
Number:Dimensionless Vacuum_Battery "Vacuum Battery [%.0f %%]" { channel="dreame:vacuum:home:l50:battery-level" }
String Vacuum_State "Vacuum State [%s]" { channel="dreame:vacuum:home:l50:state" }
String Vacuum_OperatingStatus "Operating Status [%s]" { channel="dreame:vacuum:home:l50:operating-status" }
String Vacuum_ChargingStatus "Charging Status [%s]" { channel="dreame:vacuum:home:l50:charging-status" }
Number Vacuum_ErrorCode "Vacuum Error [%d]" { channel="dreame:vacuum:home:l50:error-code" }
Number:Time Vacuum_CleaningTime "Cleaning Time [%.0f %unit%]" { channel="dreame:vacuum:home:l50:cleaning-time" }
Number:Area Vacuum_CleanedArea "Cleaned Area [%.0f %unit%]" { channel="dreame:vacuum:home:l50:cleaned-area" }
String Vacuum_TaskStatus "Task Status [%s]" { channel="dreame:vacuum:home:l50:task-status" }
String Vacuum_BaseStatus "Base Status [%s]" { channel="dreame:vacuum:home:l50:base-status" }

String Vacuum_SuctionLevel "Suction Level [%s]" { channel="dreame:vacuum:home:l50:suction-level" }
String Vacuum_WaterVolume "Water Volume [%s]" { channel="dreame:vacuum:home:l50:water-volume" }
String Vacuum_CleaningMode "Cleaning Mode [%s]" { channel="dreame:vacuum:home:l50:cleaning-mode" }
String Vacuum_CleanGenius "CleanGenius [%s]" { channel="dreame:vacuum:home:l50:clean-genius" }
String Vacuum_CleaningRoute "Cleaning Route [%s]" { channel="dreame:vacuum:home:l50:cleaning-route" }
String Vacuum_DryingTime "Drying Time [%s]" { channel="dreame:vacuum:home:l50:drying-time" }

String Vacuum_Rooms "Rooms [%s]" { channel="dreame:vacuum:home:l50:rooms" }
String Vacuum_RoomCleaning "Room Cleaning [%s]" { channel="dreame:vacuum:home:l50:room-cleaning" }
Image Vacuum_MapPng "Vacuum Map (PNG)" { channel="dreame:vacuum:home:l50:map-png" }
Image Vacuum_MapSvg "Vacuum Map (SVG)" { channel="dreame:vacuum:home:l50:map-svg" }

Number:Dimensionless Vacuum_MainBrush "Main Brush [%.0f %%]" { channel="dreame:vacuum:home:l50:main-brush-left" }
Number:Dimensionless Vacuum_SideBrush "Side Brush [%.0f %%]" { channel="dreame:vacuum:home:l50:side-brush-left" }
Number:Dimensionless Vacuum_Filter "Filter [%.0f %%]" { channel="dreame:vacuum:home:l50:filter-left" }
Number:Dimensionless Vacuum_MopPads "Mop Pads [%.0f %%]" { channel="dreame:vacuum:home:l50:mop-pad-left" }
Number:Time Vacuum_TotalCleaningTime "Total Cleaning Time [%.0f %unit%]" { channel="dreame:vacuum:home:l50:total-cleaning-time" }
Number Vacuum_CleaningCount "Cleaning Count [%.0f]" { channel="dreame:vacuum:home:l50:cleaning-count" }
Number:Area Vacuum_TotalCleanedArea "Total Cleaned Area [%.0f %unit%]" { channel="dreame:vacuum:home:l50:total-cleaned-area" }
```

Use the numeric segment IDs shown by `Vacuum_Rooms` for room cleaning. The PNG map is suitable for Basic UI;
clients with reliable SVG support can use the SVG channel.

Example vacuum commands and settings:

```java
Vacuum_Command.sendCommand("START")
Vacuum_Command.sendCommand("PAUSE")
Vacuum_Command.sendCommand("DOCK")
Vacuum_Command.sendCommand("AUTO_EMPTY")
Vacuum_Command.sendCommand("WASH_MOPS")
Vacuum_Command.sendCommand("START_DRYING")

Vacuum_SuctionLevel.sendCommand("TURBO")
Vacuum_WaterVolume.sendCommand("MEDIUM")
Vacuum_CleaningMode.sendCommand("SWEEPING_AND_MOPPING")
Vacuum_CleanGenius.sendCommand("ROUTINE")
Vacuum_CleaningRoute.sendCommand("QUICK")
Vacuum_DryingTime.sendCommand("3H")

Vacuum_RoomCleaning.sendCommand("3,7")
```

## Communication

The binding refreshes the selected cloud service's OAuth token before expiry.
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

- Mower models other than `dreame.mower.g2540d`, `mova.mower.g2405c`, `mova.mower.g2529d` and
  `mova.mower.g2584d` still require physical-device testing.
- Vacuum discovery, status, commands, settings and map rendering have been physically tested with the L50 Ultra Pro;
  other mapped vacuum models remain untested with this binding.
- ¹ The active map can be selected on MOVA mowers. This selects the map used by the openHAB cloud session; other MOVAhome app sessions may continue to display another map.
- Lifetime statistics and do not disturb are not exposed by all mower firmware versions.
- ² On the MOVA LiDAX Ultra 1600 AWD (`mova.mower.g2584d`), do not disturb is currently read-only: MQTT reports its
  state and schedule at property `2/51`, but the command format has not been verified. The binding therefore does not
  send do not disturb commands for this model. To prevent a linked Item from showing an optimistic command value,
  disable autoupdate for that Item until writing has been validated on a physical mower.
- `do-not-disturb-active` is calculated from the enabled flag and time window reported by the mower, using the local
  time zone of the openHAB server. Time windows crossing midnight are supported.
- Map details depend on the geometry and metadata supplied by the mower firmware.
- Vacuum room names depend on saved-map metadata supplied by the vacuum cloud service. The map and numeric room IDs
  remain available when that metadata is absent.
- Position values use the native Dreame map coordinate system and are not geographic coordinates.
- The integration depends on private Dreamehome and MOVAhome cloud APIs and may require updates if they change.

## Protocol References

Protocol behavior was compared with the MIT-licensed [nicolasglg/dreame-mower-a1-pro](https://github.com/nicolasglg/dreame-mower-a1-pro) project and the mower protocol documentation in [TA2k/ioBroker.dreame](https://github.com/TA2k/ioBroker.dreame/blob/main/docs/mower-protocol.md).
No source code from these projects is copied into this binding.
The vacuum model identifiers are derived from the MIT-licensed
[foXaCe/dreame-vacuum device database](https://github.com/foXaCe/dreame-vacuum/tree/d32732d1175d9b3e90e5f8f38651370effea8fe0),
as documented in the binding's `NOTICE` file.

The latest L50 test confirms `DRYING` and `SLEEPING` and observes state codes 9, 12 and 20 during a mop session.
These map to `WASHING`, `SWEEPING_AND_MOPPING` and `CLEAN_ADD_WATER`, following the reference state enumeration.
The tester directly identified code 9 as mop washing; code 20 still needs confirmation against the physical operation.

### Vacuum map

Link `map-png` or `map-svg` to an Image Item and display that Item in an image card in Main UI.
Rooms use distinct colors, while walls, the cleaning path, room names with their numeric segment IDs, the robot and the charging station are rendered separately.
The preview uses the map's native orientation rather than the app's rotation setting.
The binding requests a complete I frame through the device action and then applies sequential MQTT P frames.
If a partial frame is missing, belongs to another map or uses an unsupported V3 cover/diff update, the binding discards it and requests a new complete frame during the next refresh.
Until a supported complete frame arrives, the channels remain undefined; reconnecting clears the previous image.
Encrypted map values are not supported.
Cloud map objects are downloaded only from validated Dreame or Alibaba Cloud HTTPS hosts.
Malformed maps and maps exceeding the 1 MiB decompressed data or 2048-pixel per-axis limits are ignored.

The `map-png` and `map-svg` Image channels provide the same map as PNG and vector SVG respectively.
Use `map-png` for Basic UI and clients that do not display SVG reliably.
Both channels support cached REFRESH requests and clear on reconnection.
The read-only `rooms` channel exposes the room mapping from the current map as a compact list.
Use one or more of its numeric IDs for the `room-cleaning` command.
