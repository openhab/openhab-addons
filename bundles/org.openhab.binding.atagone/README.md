# ATAG ONE Binding

This binding integrates the [ATAG ONE](https://www.atag.nl/producten/thermostaten/atag-one) smart thermostat with openHAB via its local HTTP API, without requiring any cloud connection or MQTT broker.

## Supported Things

| Thing ID | Description |
|----------|-------------|
| `thermostat` | ATAG ONE thermostat (local LAN API) |

## Discovery

The thermostat broadcasts a UDP datagram on port 11000 approximately every 10 seconds.
The binding listens passively and creates an Inbox entry when it detects a device.
Discovery is optional — the Thing can also be created manually (see below).

## Pairing

Pairing is normally automatic — no action is needed on the thermostat itself. The binding generates
a stable client identifier on first contact and the Thing goes `ONLINE` directly.

If the thermostat instead requires manual confirmation, the Thing will go
`OFFLINE / CONFIGURATION_PENDING`. In that case:

1. Open the thermostat display.
1. Navigate to **Settings → Connected apps** and press **Accept**.

The Thing transitions to `ONLINE` within a few seconds. On subsequent openHAB restarts the saved
client ID is reused, so this step is not repeated.

## Thing Configuration

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `hostname` | text | yes | — | IP address or hostname of the thermostat |
| `port` | integer | no | `10000` | HTTP port of the local API |
| `refreshInterval` | integer | no | `30` | Poll interval in seconds |
| `clientId` | text | no | auto | Stable client identifier used for pairing (advanced) |

### Textual configuration example

```java
Thing atagone:thermostat:boiler "ATAG ONE" [
    hostname        = "192.168.1.42",
    refreshInterval = 30
]
```

`clientId` is omitted — the binding generates one on first pairing and persists it automatically.

## Channels

### Standard channels

| Channel ID | Type | RW | Description |
|------------|------|----|-------------|
| `room-temperature` | `Number:Temperature` | R | Room temperature (built-in sensor) |
| `target-temperature` | `Number:Temperature` | RW | Target (setpoint) temperature |
| `hvac-mode` | `String` | RW | `auto` or `heat` |
| `preset-mode` | `String` | RW | Active preset: `manual`, `auto`, `holiday`, `extend`, `fireplace` |
| `preset-mode-duration` | `Number:Time` | R | Remaining duration of current timed preset |
| `ch-water-temperature` | `Number:Temperature` | R | Heating Circuit Temperature |
| `ch-return-temperature` | `Number:Temperature` | R | Heating Circuit Return Temperature (advanced) |
| `ch-water-pressure` | `Number:Pressure` | R | CH circuit water pressure |
| `ch-setpoint` | `Number:Temperature` | R | Boiler Target Water Temperature (advanced) |
| `dhw-temperature` | `Number:Temperature` | R | Hot Water Temperature |
| `dhw-target-temperature` | `Number:Temperature` | RW | Hot Water Target Temperature |
| `dhw-mode` | `Number` | R | DHW operating mode (raw device value) |
| `outside-temperature` | `Number:Temperature` | R | Outside Temperature (boiler estimate) |
| `flame` | `Switch` | R | Burner flame active |
| `modulation-level` | `Number:Dimensionless` | R | Burner modulation level (%) |
| `burning-hours` | `Number:Time` | R | Total burner hours |
| `burner-target` | `String` | R | `none`, `ch`, or `dhw` |
| `vacation-start` | `DateTime` | R | Vacation period start (advanced) |
| `vacation-end` | `DateTime` | R | Vacation period end (advanced) |
| `vacation-temperature` | `Number:Temperature` | RW | Setpoint during vacation |
| `vacation-duration` | `Number:Time` | RW | Vacation duration in days — writing activates holiday mode immediately |
| `fireplace-duration` | `Number:Time` | RW | Fireplace mode duration in hours — reading shows the stored default; writing activates fireplace mode for exactly the written duration |
| `weather-status` | `String` | R | Weather compensation status |
| `device-errors` | `String` | R | Active device error codes |
| `boiler-errors` | `String` | R | Active boiler error codes |
| `time-to-target` | `Number:Time` | R | Estimated time to reach target temperature |

Advanced diagnostic channels are also available (visible when **Show advanced** is enabled in the UI).

## Preset modes

`preset-mode` accepts the following write values:

| Value | Description |
|-------|-------------|
| `auto` | Follow the programmed schedule |
| `holiday` | Hold a fixed low temperature for the vacation period |
| `fireplace` | Temporarily reduce setpoint (fireplace warmth compensation) — reuses the last known `fireplace-duration`, or 1 hour if none is known yet |

`manual` and `extend` cannot be set directly via the API.
Writing an unknown value is rejected with a warning and the item reverts to its last known state.

`extend` is activated automatically by the device when you write to `target-temperature` while the preset is `auto`.
The `extend-duration` channel (advanced) shows the stored default extend duration and is read-only.

## Holiday (vacation) mode

Holiday mode lets you set a fixed low temperature for a defined period.
There is no separate "activate" step — writing a duration starts the period immediately, starting now.

```text
Number:Time  atagone_vacation_duration  "Vacation duration"  { channel="atagone:thermostat:boiler:vacation-duration" }
String       atagone_preset             "Preset mode"        { channel="atagone:thermostat:boiler:preset-mode" }
```

Two equivalent ways to start it:

- Write a duration to `vacation-duration` (in days, e.g. `7 d`) — holiday mode starts immediately for that many days.
- Write `preset-mode = holiday` — reuses the currently-active `vacation-duration` if a holiday is already running, otherwise starts one using the device's own stored default duration (typically 7 days, but reflects whatever was last configured on the thermostat or in its app).

`vacation-start` and `vacation-end` are read-only status channels that report the currently running period; they cannot be written to schedule a future period.

Cancel by writing `preset-mode = auto`.

## Fireplace mode

Writing a duration to `fireplace-duration` (in hours, e.g. `2 h`) activates fireplace mode immediately for exactly that duration.
There is no separate "activate" step.

```text
Number:Time  atagone_fireplace  "Fireplace duration"  { channel="atagone:thermostat:boiler:fireplace-duration" }
```

Send `2 h` to activate for 2 hours (any time unit is accepted, e.g. `7200 s` works too).
Reading `fireplace-duration` returns the stored default duration from the device (the value used when fireplace mode is activated from the physical thermostat).

## Full example

### `atagone.items`

```text
Number:Temperature  CH_Room_Temp        "Room [%.1f °C]"      { channel="atagone:thermostat:boiler:room-temperature" }
Number:Temperature  CH_Target_Temp      "Target [%.1f °C]"    { channel="atagone:thermostat:boiler:target-temperature" }
String              CH_Preset           "Preset [%s]"         { channel="atagone:thermostat:boiler:preset-mode" }
Number:Time         CH_Vacation_Duration "Vacation duration [%.0f %unit%]" { channel="atagone:thermostat:boiler:vacation-duration" }
Number:Temperature  CH_Vacation_Temp    "Vacation temp [%.1f °C]" { channel="atagone:thermostat:boiler:vacation-temperature" }
Number:Time         CH_Fireplace        "Fireplace [%.1f %unit%]" { channel="atagone:thermostat:boiler:fireplace-duration" }
Switch              CH_Flame            "Flame"               { channel="atagone:thermostat:boiler:flame" }
Number:Temperature  DHW_Temp            "DHW [%.1f °C]"       { channel="atagone:thermostat:boiler:dhw-temperature" }
Number:Pressure     CH_Water_Pressure   "Pressure [%.2f bar]" { channel="atagone:thermostat:boiler:ch-water-pressure" }
```
