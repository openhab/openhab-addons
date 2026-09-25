# Home Assistant Entity Mapping Guide

This document details the domain-specific mapping rules, channel structures, and command handling for Home Assistant entities integrated into openHAB via the `hasslink` binding.

While simple entities (like basic switches, numeric sensors, and dropdown selects) map state values and commands 1:1, many Home Assistant domains feature domain-specific complexities—such as asymmetric state-to-command models (e.g., vacuum state `cleaning` vs. command `start`), inverted percentage scales, or inline parameters (e.g., alarm security PINs).
The entity handlers described below translate these domain behaviors into standard, native openHAB channels and item types.

[[toc]]

---

## Alarm Entity (`alarm_control_panel`)

_Home Assistant Documentation:_ [`alarm_control_panel`](https://www.home-assistant.io/integrations/alarm_control_panel/)

The primary channel exposes the alarm system status and accepts imperative action commands based on the entity's `supported_features` bitmask.

| Command             | Label / Description        |
|:--------------------|:---------------------------|
| `disarm`            | Disarm                     |
| `arm`               | Arm (Away Mode)            |
| `arm_home`          | Arm (Home Mode)            |
| `arm_night`         | Arm (Night Mode)           |
| `arm_vacation`      | Arm (Vacation Mode)        |
| `arm_custom_bypass` | Arm Custom Bypass          |
| `trigger`           | Manually trigger the alarm |

### Inline Security Code / PIN Commands

If your Home Assistant alarm panel requires a PIN code to arm or disarm, append the code to the command separated by a colon (`:`):

- `disarm:1234` — Disarms the panel using code `1234`
- `arm_home:5678` — Arms in Home mode using code `5678`

---

## Binary Sensor Entity (`binary_sensor`)

_Home Assistant Documentation:_ [`binary_sensor`](https://www.home-assistant.io/integrations/binary_sensor/)

Binary sensors represent two-state read-only telemetry inputs.

| Channel     | Item Type            | R/W | Description                                                                   |
|:------------|:---------------------|:----|:------------------------------------------------------------------------------|
| **Primary** | `Switch` / `Contact` | R   | Inferred dynamically as `Contact` or `Switch` based on the HA `device_class`. |

- **Read-Only Enforcement:** All binary sensor channels are marked `readOnly = true` in openHAB.
  Commands sent to this channel are ignored.

::: tip

To force `hasslink` to create a `Contact` channel (`OPEN`/`CLOSED`), set the entity's `device_class` to `door`, `garage_door`, `window`, or `opening`.

For ESPHome devices, set `device_class: garage_door` directly in your ESPHome YAML. If the HA UI does not allow changing the device class, override it via Home Assistant's `customize.yaml`.

:::

---

## Button Entity (`button`)

_Home Assistant Documentation:_ [`button`](https://www.home-assistant.io/integrations/button/)

Button entities represent stateless action triggers.

| Channel     | Item Type | R/W | Description                                                                      |
|:------------|:----------|:----|:---------------------------------------------------------------------------------|
| **Primary** | `String`  | W   | Accepts `PRESS` command to activate the button (`button.press` service call).    |

- **Stateless Behavior:** The `button` entity does not report or maintain a persistent state in openHAB.
  Sending a `PRESS` command triggers the associated action in Home Assistant.

See also: [pressButton action](../README.md#pressbutton)

---

## Calendar Entity (`calendar`)

_Home Assistant Documentation:_ [`calendar`](https://www.home-assistant.io/integrations/calendar/)

Calendar entities expose event state and current event information.

| Channel     | Item Type | R/W | Description                                                                   |
|:------------|:----------|:----|:------------------------------------------------------------------------------|
| **Primary** | `Switch`  | R   | Active event state (`ON` when an event is currently active, otherwise `OFF`). |
| `#message`  | `String`  | R   | Summary or title of the currently active calendar event.                      |

---

## Climate Entity (`climate`)

_Home Assistant Documentation:_ [`climate`](https://www.home-assistant.io/integrations/climate/)

The `climate` handler provides comprehensive control over HVAC systems, thermostats, and heat pumps.

| Channel                  | openHAB Item Type      | R/W | Description                                                           |
|:-------------------------|:-----------------------|:----|:----------------------------------------------------------------------|
| **Primary**              | `String`               | R/W | Primary HVAC mode (`off`, `heat`, `cool`, `auto`, `dry`, `fan_only`). |
| `#power`                 | `Switch`               | R/W | Convenience power toggle (`ON`/`OFF`).                                |
| `#current_temperature`   | `Number:Temperature`   | R   | Measured current ambient temperature.                                 |
| `#current_humidity`      | `Number:Dimensionless` | R   | Measured current relative humidity (%).                               |
| `#temperature`           | `Number:Temperature`   | R/W | Target temperature setpoint.                                          |
| `#target_temp_high`      | `Number:Temperature`   | R/W | High temperature setpoint (for dual-setpoint/range modes).            |
| `#target_temp_low`       | `Number:Temperature`   | R/W | Low temperature setpoint (for dual-setpoint/range modes).             |
| `#humidity`              | `Number:Dimensionless` | R/W | Target humidity setpoint (%).                                         |
| `#fan_mode`              | `String`               | R/W | Fan speed mode (e.g., `auto`, `low`, `high`).                         |
| `#preset_mode`           | `String`               | R/W | Preset profile (e.g., `eco`, `away`, `comfort`).                      |
| `#swing_mode`            | `String`               | R/W | Vertical louver swing setting.                                        |
| `#swing_horizontal_mode` | `String`               | R/W | Horizontal louver swing setting.                                      |

### Dynamic Options & Ranges

- **HVAC & Feature Modes:** Valid selection options for `Primary`, `#fan_mode`, `#preset_mode`, `#swing_mode`, and `#swing_horizontal_mode` are dynamically populated from the entity's attributes (`hvac_modes`, `fan_modes`, etc.).
- **Setpoint Bounds:** Min/max limits and step increments for `#temperature` and `#humidity` are automatically retrieved from the entity state (`min_temp`, `max_temp`, `target_temp_step`, `min_humidity`, `max_humidity`).

---

## Cover Entity (`cover`)

_Home Assistant Documentation:_ [`cover`](https://www.home-assistant.io/integrations/cover/)

Home Assistant and openHAB use opposite conventions for open/closed percentage values.
The `cover` handler automatically inverts position values to comply with native openHAB roller shutter and cover conventions:

| State      | Home Assistant Position | openHAB Position (`Rollershutter` / `Dimmer`) |
|:-----------|:------------------------|:----------------------------------------------|
| **Open**   | `100%`                  | `0%` (`UpDownType.UP`)                        |
| **Closed** | `0%`                    | `100%` (`UpDownType.DOWN`)                    |

### Position & Tilt Channels

- **`#current_position` (`Rollershutter`):** Controls cover opening/closing. Accepts `UP`, `DOWN`, `STOP`, and `PercentType` commands (inverted).
  The `MOVE` command is not supported.
- **`#current_tilt_position` (`Rollershutter`):** Controls louver/slat tilt angle (inverted percentage).

---

## Date & DateTime Entities (`date`, `datetime`)

_Home Assistant Documentation:_ [`date`](https://www.home-assistant.io/integrations/date/) | [`datetime`](https://www.home-assistant.io/integrations/datetime/)

Date and combined Date/Time picker entities map to openHAB `DateTime` channels.

| Domain     | Primary Channel Type | R/W | Service Call         | Format Example              |
|:-----------|:---------------------|:----|:---------------------|:----------------------------|
| `date`     | `DateTime`           | R/W | `date.set_value`     | `2026-09-23`                |
| `datetime` | `DateTime`           | R/W | `datetime.set_value` | `2026-09-23T17:52:00+00:00` |

- **Command Format:** When a `DateTimeType` command is sent to a `date` channel, only the date component (`YYYY-MM-DD`) is transmitted in the service payload.
  Commands sent to a `datetime` channel send the complete ISO-8601 timestamp string.

---

## Device Tracker Entity (`device_tracker`)

_Home Assistant Documentation:_ [`device_tracker`](https://www.home-assistant.io/integrations/device_tracker/)

Device trackers report presence detection, network connectivity, and geographic location telemetry.

| Channel          | Item Type              | R/W | Description                                                             |
|:-----------------|:-----------------------|:----|:------------------------------------------------------------------------|
| **Primary**      | `String`               | R   | Primary presence state (e.g., `home`, `not_home`, or zone name).        |
| `#location`      | `Location`             | R   | GPS coordinates (`latitude,longitude`), created when coordinates exist. |
| `#latitude`      | `Number`               | R   | Raw latitude coordinate.                                                |
| `#longitude`     | `Number`               | R   | Raw longitude coordinate.                                               |
| `#gps_accuracy`  | `Number:Length`        | R   | Location accuracy radius (metres).                                      |
| `#battery_level` | `Number:Dimensionless` | R   | Tracked device battery level (%).                                       |
| `#mac`           | `String`               | R   | Network MAC address.                                                    |
| `#ip`            | `String`               | R   | Network IP address.                                                     |
| `#host_name`     | `String`               | R   | Network hostname (checks `host_name` or `hostname` attribute).          |
| `#source_type`   | `String`               | R   | Tracking source mechanism (e.g., `gps`, `router`, `bluetooth`).         |

---

## Event Entity (`event`)

_Home Assistant Documentation:_ [`event`](https://www.home-assistant.io/integrations/event/)

The `event` handler maps stateless event generators (such as physical button presses, doorbell triggers, or remote control signals).

| Channel     | Item Type | R/W | Description                                                                |
|:------------|:----------|:----|:---------------------------------------------------------------------------|
| **Primary** | `String`  | R   | Primary event payload channel. Releases channel trigger events in openHAB. |

- **Trigger Behavior:** Primary event channels act as openHAB trigger channels.
  Incoming event notifications pass the reported `event_type` string (e.g., `press`, `double_press`, `long_press`) directly to linked rules.

---

## Fan Entity (`fan`)

_Home Assistant Documentation:_ [`fan`](https://www.home-assistant.io/integrations/fan/)

The `fan` handler maps power, speed percentages, oscillation, and operational modes.

| Channel        | Item Type | R/W | Description                                                      |
|:---------------|:----------|:----|:-----------------------------------------------------------------|
| **Primary**    | `Switch`  | R/W | Fan power state (`ON`/`OFF`).                                    |
| `#percentage`  | `Dimmer`  | R/W | Fan speed percentage (0–100%, feature-gated).                    |
| `#oscillating` | `Switch`  | R/W | Toggle fan oscillation (`ON`/`OFF`, feature-gated).              |
| `#direction`   | `String`  | R/W | Airflow direction (e.g., `forward`, `reverse`, feature-gated).   |
| `#preset_mode` | `String`  | R/W | Preset operational mode (options populated from `preset_modes`). |

---

## Geolocation Entity (`geo_location`)

_Home Assistant Documentation:_ [`geo_location`](https://www.home-assistant.io/integrations/geo_location/)

Geolocation entities represent external event tracking (such as earthquake alerts, wildfire tracking, or storm warning feeds).

| Channel      | Item Type       | R/W | Description                                                         |
|:-------------|:----------------|:----|:--------------------------------------------------------------------|
| **Primary**  | `Number:Length` | R   | Distance from the home location to the event origin.                |
| `#source`    | `String`        | R   | Data source or reporting agency name.                               |
| `#location`  | `Location`      | R   | Coordinates (`latitude,longitude`), created when coordinates exist. |
| `#latitude`  | `Number:Angle`  | R   | Event latitude (degrees).                                           |
| `#longitude` | `Number:Angle`  | R   | Event longitude (degrees).                                          |

---

## Humidifier Entity (`humidifier`)

_Home Assistant Documentation:_ [`humidifier`](https://www.home-assistant.io/integrations/humidifier/)

The `humidifier` handler manages humidifiers and dehumidifiers.

| Channel             | Item Type              | R/W | Description                                          |
|:--------------------|:-----------------------|:----|:-----------------------------------------------------|
| **Primary**         | `Switch`               | R/W | Device power state (`ON`/`OFF`).                     |
| `#target_humidity`  | `Number:Dimensionless` | R/W | Target humidity setpoint (%).                        |
| `#current_humidity` | `Number:Dimensionless` | R   | Current measured relative humidity (%).              |
| `#mode`             | `String`               | R/W | Operational mode (options populated when supported). |

---

## Image & Camera Entities (`image`, `camera`)

_Home Assistant Documentation:_ [`image`](https://www.home-assistant.io/integrations/image/) | [`camera`](https://www.home-assistant.io/integrations/camera/)

Image snapshots are fetched asynchronously over HTTP whenever Home Assistant broadcasts update events over the WebSocket connection.
To conserve network bandwidth and CPU cycles, Home Assistant limits automatic frame pushing.

To force an immediate frame update from openHAB rules or UI widgets, send a `REFRESH` command to the image channel.

### Image Entity (`image`)

| Channel     | Item Type | Description                       |
|:------------|:----------|:----------------------------------|
| **Primary** | `Image`   | Binary image payload as `RawType` |

### Camera Entity (`camera`)

| Channel       | Item Type | Description                                                   |
|:--------------|:----------|:--------------------------------------------------------------|
| **Primary**   | `String`  | Camera status string (e.g., `recording`, `streaming`, `idle`) |
| `#image`      | `Image`   | Snapshot image payload as `RawType`                           |
| `#power`      | `Switch`  | Toggles camera power state (if `ON_OFF` feature is supported) |
| `#stream_url` | `String`  | Stream source URL (if `STREAM` feature is supported)          |

---

## Lawn Mower Entity (`lawn_mower`)

_Home Assistant Documentation:_ [`lawn_mower`](https://www.home-assistant.io/integrations/lawn_mower/)

Robotic lawn mower entities decouple current operational status from activity commands.

| Channel     | Item Type | R/W | Description                                     |
|:------------|:----------|:----|:------------------------------------------------|
| **Primary** | `String`  | R/W | Primary operational status and command channel. |

### Supported Commands

Command options are populated dynamically based on the mower's `supported_features` bitmask:

| Command                  | Label / Description | Service Call              |
|:-------------------------|:--------------------|:--------------------------|
| `start`                  | Start Mowing        | `lawn_mower.start_mowing` |
| `pause`                  | Pause               | `lawn_mower.pause`        |
| `stop`                   | Stop                | `lawn_mower.stop`         |
| `dock`, `return_to_base` | Return to Base      | `lawn_mower.dock`         |

---

## Light Entity (`light`)

_Home Assistant Documentation:_ [`light`](https://www.home-assistant.io/integrations/light/)

The `light` domain dynamically inspects entity capabilities to adapt the primary channel type and expose additional attribute channels only when supported.

| Channel               | openHAB Item Type             | Notes                                                                                          |
|:----------------------|:------------------------------|:-----------------------------------------------------------------------------------------------|
| **Primary**           | `Color` / `Dimmer` / `Switch` | `Color` if color modes are supported; `Dimmer` if brightness is supported; otherwise `Switch`. |
| `#color_temp`         | `Number:Temperature`          | Created when color temperature is supported. Always expressed in **Kelvin** (K).               |
| `#color_temp_percent` | `Dimmer`                      | Normalized color temperature scale (0–100%), where **0% = Cool** and **100% = Warm**.          |
| `#effect`             | `String`                      | Created when effect selection is supported. Options are populated dynamically.                 |

### Color Temperature & Unit Conversion

- **Kelvin Representation:** Color temperature in openHAB is consistently represented as a `Number:Temperature` quantity in Kelvin (`K`).
- **Mired/Mirek Conversion:** If a command is sent in mireds or mireks (e.g. via scripts or legacy rules), the binding automatically converts the value to Kelvin before dispatching the payload to Home Assistant.

### Channel Interactions

- **Primary Channel Commands:** Sending a `PercentType` command adjusts brightness (scaled between HA's `0–255` and openHAB's `0–100%`).
  Sending `OnOffType` commands toggles power.
- **Attribute Channel Commands:** Updating an attribute channel (e.g., `#color_temp`, `#effect`) automatically turns the light ON, as attribute changes are dispatched as parameters of Home Assistant's `light.turn_on` service.

---

## Lock Entity (`lock`)

_Home Assistant Documentation:_ [`lock`](https://www.home-assistant.io/integrations/lock/)

Lock entities represent physical door locks, keypads, and access control hardware.

| Channel     | Item Type | R/W | Description                                                                        |
|:------------|:----------|:----|:-----------------------------------------------------------------------------------|
| **Primary** | `String`  | R/W | Primary lock status string and command entry point (`locked`, `unlocked`, `open`). |
| `#locked`   | `Switch`  | R/W | Convenience lock toggle (`ON` = locked, `OFF` = unlocked).                         |

### Commands & Security PIN Codes

- **Supported Commands:** Primary channel accepts `lock` (or `locked`), `unlock` (or `unlocked`), and `open`.
- **Inline Security Code / PIN:** If the lock requires a PIN or key code, append the code to the command separated by a colon (`:`), e.g., `unlock:1234` or `lock:5678`.

---

## Media Player Entity (`media_player`)

_Home Assistant Documentation:_ [`media_player`](https://www.home-assistant.io/integrations/media_player/)

The `media_player` entity maps playback controls, volume levels, and advanced media features to native openHAB channels:

- **Volume Scaling:** Home Assistant volume levels (`0.0` to `1.0`) are automatically converted to openHAB `PercentType` (`0%` to `100%`).
- **Playback Controls:** Primary state channel accepts standard `PlayerType` commands (`NEXT`, `PREVIOUS`, `PLAY`, `PAUSE`, `FASTFORWARD`, `REWIND`).
- **Sound Modes & Repeat:** Supports selectable sound modes via `sound_mode_list` exposed as string channels.
- **Shuffle & Mute:** Exposes toggleable control channels for shuffle randomization.

| Channel            | Item Type | R/W | Description                                                         |
|:-------------------|:----------|:----|:--------------------------------------------------------------------|
| **Primary**        | `String`  | R   | Current playback state of the media player (e.g., playing, paused). |
| `#player`          | `Player`  | R/W | Virtual attribute controlling playback actions and media transport. |
| `#stop`            | `Switch`  | W   | Momentary trigger switch to send `media_stop` service commands.     |
| `#power`           | `Switch`  | R/W | Turns the media player device on or off.                            |
| `#volume_level`    | `Dimmer`  | R/W | Volume level scaled between 0% and 100%.                            |
| `#is_volume_muted` | `Switch`  | R/W | Indicates whether the player volume is muted.                       |
| `#source`          | `String`  | R/W | Active input source, selectable from the device's source list.      |
| `#sound_mode`      | `String`  | R/W | Current audio sound mode (e.g., Music, Movie).                      |
| `#shuffle`         | `Switch`  | R/W | Enables or disables shuffle mode playback.                          |
| `#repeat`          | `String`  | R/W | Controls track or playlist repetition mode.                         |
| `#media_title`     | `String`  | R   | Title of the currently playing media item.                          |
| `#media_artist`    | `String`  | R   | Artist of the currently playing media item.                         |

---

## Number Entity (`number`)

_Home Assistant Documentation:_ [`number`](https://www.home-assistant.io/integrations/number/)

Number entities represent configurable numeric variables, setpoints, or sliders.

| Channel     | Item Type                    | R/W | Description                                                                                           |
|:------------|:-----------------------------|:----|:------------------------------------------------------------------------------------------------------|
| **Primary** | `Number` / `Number:Quantity` | R/W | Numeric value, inferred dynamically as standard numeric or QuantityType based on unit of measurement. |

- **Range Bounds & Step:** Minimum (`min`), maximum (`max`), and step (`step`) parameters defined in Home Assistant attributes are automatically extracted to openHAB state descriptions.
- **Service Call:** Commands dispatch `number.set_value` payloads to Home Assistant.

---

## Radio Frequency Entity (`radio_frequency`)

_Home Assistant Documentation:_ [`radio_frequency`](https://www.home-assistant.io/integrations/radio_frequency/)

Radio frequency entities represent RF transmitters, receivers, and signal gateways.

| Channel         | Item Type          | R/W | Description                                                          |
|:----------------|:-------------------|:----|:---------------------------------------------------------------------|
| **Primary**     | `DateTime`         | R   | Timestamp of the last received or transmitted RF signal.             |
| `#send_command` | `String`           | W   | Transmits raw RF payload strings via `radio_frequency.send_command`. |
| `#frequency`    | `Number:Frequency` | R   | Operating radio frequency (e.g., in MHz).                            |
| `#protocol`     | `String`           | R   | RF protocol metadata or identifier.                                  |

---

## Remote Entity (`remote`)

_Home Assistant Documentation:_ [`remote`](https://www.home-assistant.io/integrations/remote/)

Remote entities represent universal IR/RF remotes and media control hubs.

| Channel           | Item Type | R/W | Description                                                               |
|:------------------|:----------|:----|:--------------------------------------------------------------------------|
| **Primary**       | `Switch`  | R/W | Power state of the remote control hub (`ON`/`OFF`).                       |
| `#send_command`   | `String`  | W   | Transmits remote key commands via `remote.send_command`.                  |
| `#learn_command`  | `Switch`  | W   | Triggers command learning mode via `remote.learn_command` (if supported). |
| `#delete_command` | `Switch`  | W   | Deletes stored commands via `remote.delete_command` (if supported).       |

---

## Scene Entity (`scene`)

_Home Assistant Documentation:_ [`scene`](https://www.home-assistant.io/integrations/scene/)

Scene entities represent predefined home automation scenes.

| Channel     | Item Type | R/W | Description                                                                  |
|:------------|:----------|:----|:-----------------------------------------------------------------------------|
| **Primary** | `Switch`  | R/W | Triggers scene activation when commanded `ON`. Defaults to `OFF` in openHAB. |

- **Stateless Behavior:** Scenes do not maintain an active state.

See also: [activateScene action](../README.md#activatescene)

---

## Script Entity (`script`)

_Home Assistant Documentation:_ [`script`](https://www.home-assistant.io/integrations/script/)

Script entities represent executable sequence scripts defined in Home Assistant.

| Channel     | Item Type | R/W | Description                                         |
|:------------|:----------|:----|:----------------------------------------------------|
| **Primary** | `String`  | R/W | Primary script execution entry point.          |

- **Execution & Parameters:** Scripts are executed by calling the script's specific service name.

See also: [runScript action](../README.md#runscript)

---

## Select Entity (`select`)

_Home Assistant Documentation:_ [`select`](https://www.home-assistant.io/integrations/select/)

Select entities represent single-choice dropdown controls.

| Channel     | Item Type | R/W | Description            |
|:------------|:----------|:----|:-----------------------|
| **Primary** | `String`  | R/W | Selected option value. |

- **Dynamic Options:** Predefined option lists in Home Assistant (`options` attribute) are automatically populated in the openHAB state description.

---

## Sensor Entity (`sensor`)

_Home Assistant Documentation:_ [`sensor`](https://www.home-assistant.io/integrations/sensor/)

Sensors provide read-only numeric, string, or state telemetry.

| Channel     | Item Type | R/W | Description                                                                                                   |
|:------------|:----------|:----|:--------------------------------------------------------------------------------------------------------------|
| **Primary** | Inferred  | R   | Inferred dynamically as standard or `QuantityType` items based on `device_class` and unit.          |
| `#attribute`| Inferred  | R   | Generic secondary attributes discovered automatically. List and map attributes are serialized as JSON. |

---

## Siren Entity (`siren`)

_Home Assistant Documentation:_ [`siren`](https://www.home-assistant.io/integrations/siren/)

Siren entities represent audible alarms, strobes, and acoustic notification hardware.

| Channel     | Item Type | R/W | Description                                                                        |
|:------------|:----------|:----|:-----------------------------------------------------------------------------------|
| **Primary** | `Switch`  | R/W | Siren active state (`ON`/`OFF`), controlling `siren.turn_on` and `siren.turn_off`. |

---

## Switch Entity (`switch`)

_Home Assistant Documentation:_ [`switch`](https://www.home-assistant.io/integrations/switch/)

Switch entities represent basic binary power toggles, relays, and power outlets.

| Channel     | Item Type | R/W | Description                                                                        |
|:------------|:----------|:----|:-----------------------------------------------------------------------------------|
| **Primary** | `Switch`  | R/W | Primary power state (`ON`/`OFF`), invoking `switch.turn_on` and `switch.turn_off`. |

---

## Text Entity (`text`)

_Home Assistant Documentation:_ [`text`](https://www.home-assistant.io/integrations/text/)

Text entities represent configurable text input fields and string configuration options.

| Channel     | Item Type | R/W | Description                                                       |
|:------------|:----------|:----|:------------------------------------------------------------------|
| **Primary** | `String`  | R/W | Free-form text value, dispatching `text.set_value` service calls. |

---

## Time Entity (`time`)

_Home Assistant Documentation:_ [`time`](https://www.home-assistant.io/integrations/time/)

Time entities represent time selector controls.

| Channel     | Item Type  | R/W | Description                                                                              |
|:------------|:-----------|:----|:-----------------------------------------------------------------------------------------|
| **Primary** | `DateTime` | R/W | Selected time value, dispatching `time.set_value` using the time component (`HH:mm:ss`). |

---

## Update Entity (`update`)

_Home Assistant Documentation:_ [`update`](https://www.home-assistant.io/integrations/update/)

Update entities track software, integration, and firmware update availability.

| Channel              | Item Type | R/W | Description                                                               |
|:---------------------|:----------|:----|:--------------------------------------------------------------------------|
| **Primary**          | `Switch`  | R   | Update availability status (`ON` = update available, `OFF` = up to date). |
| `#installed_version` | `String`  | R   | Currently installed version string.                                       |
| `#latest_version`    | `String`  | R   | Latest available version string.                                          |

---

## Vacuum Entity (`vacuum`)

_Home Assistant Documentation:_ [`vacuum`](https://www.home-assistant.io/integrations/vacuum/)

The `vacuum` handler decouples passive device status from imperative cleaning commands.

| Channel      | Item Type | R/W | Description                                               |
|:-------------|:----------|:----|:----------------------------------------------------------|
| **Primary**  | `String`  | R/W | Primary vacuum status and command entry point.            |
| `#fan_speed` | `String`  | R/W | Fan speed mode selection (options dynamically populated). |

### Supported Commands

| Command                                    | Argument        | Description                                             |
|:-------------------------------------------|:----------------|:--------------------------------------------------------|
| `start`, `on`                              | —               | Start cleaning cycle                                   |
| `stop`, `off`                              | —               | Stop cleaning                                          |
| `pause`                                    | —               | Pause current cleaning task                            |
| `dock`, `home`, `return`, `return_to_base` | —               | Return to charging dock                                |
| `spot`, `spot_clean`, `clean_spot`         | —               | Perform spot cleaning                                  |
| `locate`                                   | —               | Trigger audible locate signal on vacuum                |
| `clean_segments`, `segment`                | `segment_id(s)` | Clean specific room/zone IDs (colon-separated argument) |

#### Segment Cleaning Example

To clean specific rooms or mapped segments, pass segment IDs separated by a colon:

- `clean_segments:2` — Cleans segment ID `2`
- `clean_segments:2,3,5` — Cleans segments `2`, `3`, and `5`

---

## Valve Entity (`valve`)

_Home Assistant Documentation:_ [`valve`](https://www.home-assistant.io/integrations/valve/)

Valve entities represent motorized fluid or gas valves, shutoff valves, and irrigation solenoids.

| Channel             | Item Type | R/W | Description                                                                     |
|:--------------------|:----------|:----|:--------------------------------------------------------------------------------|
| **Primary**         | `String`  | R/W | Primary valve state string (`open`, `opening`, `closed`, `closing`).            |
| `#current_position` | `Dimmer`  | R/W | Valve opening percentage (0–100%), dispatching `valve.set_valve_position`.      |
| `#open`             | `Switch`  | W   | Trigger command switch for `valve.open_valve` (resets to `OFF` automatically).  |
| `#close`            | `Switch`  | W   | Trigger command switch for `valve.close_valve` (resets to `OFF` automatically). |
| `#stop`             | `Switch`  | W   | Trigger command switch for `valve.stop_valve` (resets to `OFF` automatically).  |

---

## Water Heater Entity (`water_heater`)

_Home Assistant Documentation:_ [`water_heater`](https://www.home-assistant.io/integrations/water_heater/)

The `water_heater` handler manages water heaters, heat pump water tanks, and boilers.

| Channel                | openHAB Item Type    | R/W | Description                                                             |
|:-----------------------|:---------------------|:----|:------------------------------------------------------------------------|
| **Primary**            | `String`             | R/W | Operation mode selection (e.g., `eco`, `electric`, `heat_pump`, `off`). |
| `#power`               | `Switch`             | R/W | Convenience power toggle (`ON`/`OFF`).                                  |
| `#away_mode`           | `Switch`             | R/W | Toggle away/vacation mode (`turn_away_mode_on` / `turn_away_mode_off`). |
| `#current_temperature` | `Number:Temperature` | R   | Measured water temperature.                                             |
| `#temperature`         | `Number:Temperature` | R/W | Target water temperature setpoint.                                      |
| `#target_temp_high`    | `Number:Temperature` | R/W | Target high temperature setpoint (for dual-bound systems).              |
| `#target_temp_low`     | `Number:Temperature` | R/W | Target low temperature setpoint (for dual-bound systems).               |

---

## Weather Entity (`weather`)

_Home Assistant Documentation:_ [`weather`](https://www.home-assistant.io/integrations/weather/)

The `weather` handler maps current weather conditions and telemetry attributes as well as time-series forecast data.

| Channel                 | openHAB Item Type       | R/W | Description                                                          |
|:------------------------|:------------------------|:----|:---------------------------------------------------------------------|
| **Primary**             | `String`                | R   | Current weather condition string (e.g., `sunny`, `rainy`, `cloudy`). |
| `#temperature`          | `Number:Temperature`    | R   | Measured current ambient temperature.                                |
| `#apparent_temperature` | `Number:Temperature`    | R   | Feels-like apparent temperature.                                     |
| `#dew_point`            | `Number:Temperature`    | R   | Atmospheric dew point temperature.                                   |
| `#humidity`             | `Number:Dimensionless`  | R   | Relative humidity percentage (%).                                    |
| `#pressure`             | `Number:Pressure`       | R   | Atmospheric pressure.                                                |
| `#wind_speed`           | `Number:Speed`          | R   | Wind speed.                                                          |
| `#wind_gust_speed`      | `Number:Speed`          | R   | Wind gust speed.                                                     |
| `#wind_bearing`         | `Number:Angle`/`String` | R   | Wind direction angle (degrees) or cardinal direction string.         |
| `#cloud_coverage`       | `Number:Dimensionless`  | R   | Cloud cover percentage (%).                                          |
| `#precipitation`        | `Number:Length`         | R   | Precipitation amount.                                                |
| `#visibility`           | `Number:Length`         | R   | Visibility distance.                                                 |
| `#uv_index`             | `Number`                | R   | Ultraviolet index.                                                   |
| `#ozone`                | `Number`                | R   | Ozone concentration level.                                           |

### Weather Forecast Channels

When forecast feature flags (`FORECAST_DAILY`, `FORECAST_HOURLY`, or `FORECAST_TWICE_DAILY`) are enabled on the entity, the binding generates dedicated forecast channels updated using openHAB `TimeSeries` data:

- **Daily Forecast:** `forecast_*` (e.g., `forecast_temperature`, `forecast_condition`, `forecast_templow`, `forecast_precipitation_probability`)
- **Hourly Forecast:** `forecast_hourly_*` (e.g., `forecast_hourly_temperature`, `forecast_hourly_condition`)
- **Twice Daily Forecast:** `forecast_twice_daily_*`
