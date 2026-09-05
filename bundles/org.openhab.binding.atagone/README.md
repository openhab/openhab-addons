# ATAG ONE Binding

This binding integrates the [ATAG ONE](https://www.atag.nl/producten/thermostaten/atag-one) smart thermostat with openHAB via its local HTTP API.
It requires no cloud connection or MQTT broker.

## Supported Things

| Thing ID     | Description                         |
| ------------ | ----------------------------------- |
| `thermostat` | ATAG ONE thermostat (local LAN API) |

## Discovery

The thermostat broadcasts a UDP datagram on port 11000 approximately every 10 seconds.
The binding listens passively and creates an Inbox entry when it detects a device.
Discovery is optional — the Thing can also be created manually (see below).

## Pairing

Pairing is normally automatic — no action is needed on the thermostat itself.
The binding generates a stable client identifier on first contact and the Thing goes `ONLINE` directly.

If the thermostat instead requires manual confirmation, the Thing will go `OFFLINE / CONFIGURATION_PENDING`.
In that case:

1. Open the thermostat display.
1. Navigate to **Settings → Connected apps** and press **Accept**.

The Thing transitions to `ONLINE` within a few seconds.
On subsequent openHAB restarts the saved client ID is reused, so this step is not repeated.

## Thing Configuration

| Parameter         | Type    | Required | Default | Description                                          |
| ----------------- | ------- | -------- | ------- | ---------------------------------------------------- |
| `hostname`        | text    | yes      | —       | IP address or hostname of the thermostat             |
| `port`            | integer | no       | `10000` | HTTP port of the local API                           |
| `refreshInterval` | integer | no       | `30`    | Poll interval in seconds                             |
| `clientId`        | text    | no       | auto    | Stable client identifier used for pairing (advanced) |

### Textual configuration example

```java
Thing atagone:thermostat:boiler "ATAG ONE" [
    hostname        = "192.168.1.42",
    refreshInterval = 30
]
```

`clientId` is omitted — the binding generates one on first pairing and persists it automatically.

## Channels

Channels are organized into five groups, by subsystem.
**Operating Mode** holds the active preset and timed modes — the one cross-cutting exception, since a mode isn't specific to heating or hot water.
**Central Heating** and **Hot Water** hold setpoints, status, and settings per subsystem.
**Device** holds hardware diagnostics.
**Alerts** holds active error codes.

### Operating Mode (`control#`)

| Channel ID                     | Type                 | RW  | Description                                                                                                                                                                                                         |
| ------------------------------ | -------------------- | --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `control#preset-mode`          | `String`             | RW  | Active preset: `manual`, `auto`, `holiday`, `extend`, `fireplace`.                                                                                                                                                  |
| `control#preset-mode-duration` | `Number:Time`        | R   | Remaining duration of current timed preset.                                                                                                                                                                         |
| `control#vacation-duration`    | `Number:Time`        | RW  | Vacation duration in days — value-setter only, writing it does not activate holiday mode. Resets to 0 on cancel.                                                                                                    |
| `control#vacation-temperature` | `Number:Temperature` | RW  | Setpoint during vacation.                                                                                                                                                                                           |
| `control#vacation-start`       | `DateTime`           | R   | Vacation period start (advanced).                                                                                                                                                                                   |
| `control#vacation-end`         | `DateTime`           | R   | Vacation period end (advanced).                                                                                                                                                                                     |
| `control#extend-duration`      | `Number:Time`        | RW  | Value-setter only — writing it does not activate extend mode. Additional time on top of whatever's left until the device's next programmed schedule change, not an absolute session length. Persists across cancel. |
| `control#fireplace-duration`   | `Number:Time`        | RW  | Fireplace mode duration in hours — value-setter only, writing it does not activate fireplace mode. Reverts to the factory default (1 h) on cancel.                                                                  |

`preset-mode` is the only channel that can ever activate or cancel a mode — see [Preset modes](#preset-modes).

### Central Heating (`heating#`)

| Channel ID                    | Type                   | RW  | Description                                                                                                                      |
| ----------------------------- | ---------------------- | --- | -------------------------------------------------------------------------------------------------------------------------------- |
| `heating#target-temperature`  | `Number:Temperature`   | RW  | Target (setpoint) room temperature.                                                                                              |
| `heating#room-temperature`    | `Number:Temperature`   | R   | Room temperature (built-in sensor).                                                                                              |
| `heating#outside-temperature` | `Number:Temperature`   | R   | Outside temperature, as estimated by the boiler.                                                                                 |
| `heating#weather-status`      | `String`               | R   | Weather compensation status.                                                                                                     |
| `heating#water-temperature`   | `Number:Temperature`   | R   | Central heating flow temperature.                                                                                                |
| `heating#return-temperature`  | `Number:Temperature`   | R   | Central heating return temperature (advanced).                                                                                   |
| `heating#water-pressure`      | `Number:Pressure`      | R   | Central heating circuit water pressure.                                                                                          |
| `heating#water-setpoint`      | `Number:Temperature`   | R   | Central heating flow setpoint targeted by the boiler (advanced).                                                                 |
| `heating#control-mode`        | `String`               | R   | `room` (room-sensor setpoint control) or `weather` (weather-compensated heating curve), independent of `preset-mode` (advanced). |
| `heating#flame`               | `Switch`               | R   | Burner flame active.                                                                                                             |
| `heating#burner-target`       | `String`               | R   | `none`, `ch`, or `dhw`.                                                                                                          |
| `heating#modulation-level`    | `Number:Dimensionless` | R   | Burner modulation level (%).                                                                                                     |
| `heating#burning-hours`       | `Number:Time`          | R   | Total burner running time.                                                                                                       |
| `heating#time-to-target`      | `Number:Time`          | R   | Estimated time to reach target temperature.                                                                                      |

### Hot Water (`hotwater#`)

| Channel ID                    | Type                 | RW  | Description                                                                                                                        |
| ----------------------------- | -------------------- | --- | ---------------------------------------------------------------------------------------------------------------------------------- |
| `hotwater#target-temperature` | `Number:Temperature` | R   | Hot water target temperature, reflecting the active schedule period. Not writable — the device has no direct control field for it. |
| `hotwater#temperature`        | `Number:Temperature` | R   | Hot water temperature.                                                                                                             |

### Alerts (`alerts#`)

| Channel ID             | Type     | RW  | Description                |
| ---------------------- | -------- | --- | -------------------------- |
| `alerts#device-errors` | `String` | R   | Active device error codes. |
| `alerts#boiler-errors` | `String` | R   | Active boiler error codes. |

Device (`device#`) and further advanced diagnostic channels in the Operating Mode/Central Heating/Hot Water groups are also available.
Enable **Show advanced** in the UI to see them.

## Preset modes

`preset-mode` is the only channel that can ever activate or cancel a mode.
The duration channels (`vacation-duration`, `extend-duration`, `fireplace-duration`) are pure value-setters — writing one only updates the stored default for that mode.
It never triggers activation on its own, matching how the device itself treats a duration field written alone.

`preset-mode` accepts the following write values:

| Value       | Description                                                                                                                                                                                  |
| ----------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `auto`      | Follow the programmed schedule. Also cancels whichever timed preset is currently active.                                                                                                     |
| `holiday`   | Hold a fixed low temperature for the vacation period, using the currently stored `vacation-duration` (or the device's own configured default if none has been set).                          |
| `fireplace` | Temporarily reduce setpoint (fireplace warmth compensation), using the currently stored `fireplace-duration` (or 1 hour if none has been set).                                               |
| `extend`    | Temporarily extend the current schedule block, using the currently stored `extend-duration` as additional time on top of whatever's left until the device's next programmed schedule change. |

`manual` cannot be set directly via the API — it is set by the device when you adjust the temperature on the display.
Writing an unknown value is rejected with a warning and the item reverts to its last known state.

To activate a mode with a custom duration in a single write, instead of first writing the duration channel and then `preset-mode`, use the [Actions](#actions) below.

Use `preset-mode-duration` to see the actual remaining time in an active timed preset.
The duration channels themselves only show the stored request value, not a countdown.

## Holiday (vacation) mode

Holiday mode holds a fixed low temperature for a defined period.

```java
Number:Time  atagone_vacation_duration  "Vacation duration"  { channel="atagone:thermostat:boiler:control#vacation-duration" }
String       atagone_preset             "Preset mode"        { channel="atagone:thermostat:boiler:control#preset-mode" }
```

To start it with a specific duration: write it to `vacation-duration` first, then write `preset-mode = holiday`.
Writing `preset-mode = holiday` alone reuses the currently-active `vacation-duration` if a holiday is already running, otherwise starts one using the device's own stored default duration (typically 7 days, but reflects whatever was last configured on the thermostat or in its app).
For a one-write custom-duration activation, use the `activateVacation` action instead.

`vacation-duration` resets to 0 whenever a holiday period is cancelled — it does not persist across cancel the way `extend-duration` does.

`vacation-start` and `vacation-end` are read-only status channels that report the currently running period; they cannot be written directly, but a future start can be scheduled via the `activateVacation` action's underlying mechanism.
Rewriting a pending/future-scheduled start before it has begun is unsupported and can reset the device — treat a scheduled vacation as write-once until it either starts or is cancelled.

Cancel by writing `preset-mode = auto`.

## Fireplace mode

Fireplace mode temporarily reduces the setpoint for warmth compensation while a fireplace is in use.

```java
Number:Time  atagone_fireplace  "Fireplace duration"  { channel="atagone:thermostat:boiler:control#fireplace-duration" }
String       atagone_preset     "Preset mode"          { channel="atagone:thermostat:boiler:control#preset-mode" }
```

To start it with a specific duration: write it to `fireplace-duration` first (any time unit is accepted, e.g. `2 h` or `7200 s`), then write `preset-mode = fireplace`.
Writing `preset-mode = fireplace` alone reuses the currently stored duration.
For a one-write custom-duration activation, use the `activateFireplace` action instead.

Reading `fireplace-duration` returns the stored default duration from the device (the value used when fireplace mode is activated from the physical thermostat).
Unlike `extend-duration`, this value does not persist across cancel — it always reverts to the factory default (1 h).

Cancelling fireplace mode via the API does not take effect on its own.
Writing `preset-mode = auto` is accepted by the device but requires confirming on the thermostat's physical display before it actually takes effect — this is confirmed device behavior, not a binding limitation, and no payload avoids it.
The binding logs a warning when this happens.
The `cancelMode` action reports this explicitly via its `requiresPhysicalConfirmation` output.

## Actions

The binding registers four [Thing Actions](https://www.openhab.org/docs/configuration/rules-dsl.html#thing-actions) under the `atagone` scope, for rule authors who want to activate a mode with a custom duration — or cancel one — in a single call, instead of the two-write channel pattern described above (set the duration channel, then `preset-mode`):

| Action                                    | Description                                                                                                                                                                                                                                                |
| ----------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `activateVacation(long durationSeconds)`  | Activates holiday mode immediately for the given duration.                                                                                                                                                                                                 |
| `activateExtend(long durationSeconds)`    | Activates extend mode immediately, additive to the time remaining until the next schedule boundary.                                                                                                                                                        |
| `activateFireplace(long durationSeconds)` | Activates fireplace mode immediately for the given duration.                                                                                                                                                                                               |
| `cancelMode()`                            | Cancels whichever timed preset is currently active or pending and returns to auto. Returns `true` if the mode being left is fireplace, meaning the write is accepted but requires confirming on the thermostat's physical display to actually take effect. |

Each action composes the same multi-field write the corresponding `preset-mode` channel value uses internally (e.g. `activateVacation` sets both `ch_mode` and the device's `start_vacation` field in one request).
Vacation in particular cannot be activated with `ch_mode` alone, and `cancelMode` handles the active-vs-pending distinction for cancelling a vacation automatically, so a script author never needs to know these details.

Example from a rule:

```javascript
actions
  .thingActions("atagone", "atagone:thermostat:boiler")
  .activateFireplace(7200);
```

## Full example

### `atagone.items`

```java
Number:Temperature  CH_Room_Temp        "Room [%.1f °C]"      { channel="atagone:thermostat:boiler:heating#room-temperature" }
Number:Temperature  CH_Target_Temp      "Target [%.1f °C]"    { channel="atagone:thermostat:boiler:heating#target-temperature" }
String              CH_Preset           "Preset [%s]"         { channel="atagone:thermostat:boiler:control#preset-mode" }
Number:Time         CH_Vacation_Duration "Vacation duration [%.0f %unit%]" { channel="atagone:thermostat:boiler:control#vacation-duration" }
Number:Temperature  CH_Vacation_Temp    "Vacation temp [%.1f °C]" { channel="atagone:thermostat:boiler:control#vacation-temperature" }
Number:Time         CH_Fireplace        "Fireplace [%.1f %unit%]" { channel="atagone:thermostat:boiler:control#fireplace-duration" }
Switch              CH_Flame            "Flame"               { channel="atagone:thermostat:boiler:heating#flame" }
Number:Temperature  DHW_Temp            "DHW [%.1f °C]"       { channel="atagone:thermostat:boiler:hotwater#temperature" }
Number:Pressure     CH_Water_Pressure   "Pressure [%.2f bar]" { channel="atagone:thermostat:boiler:heating#water-pressure" }
```
