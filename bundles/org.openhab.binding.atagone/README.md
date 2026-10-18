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

## Thing Properties

Populated from the device once it's paired, matching the portal's Account → Devices screen:

| Property | Description |
|----------|-------------|
| `deviceId` | The ONE controller's own identifier (also the representation property) |
| `serialNumber` | Boiler serial number (P-number) |
| `vendor` | Always `ATAG` |
| `firmwareVersion` | Firmware version, parsed from the device's update-check URL |
| `installerId` | Installer identifier, if the installer has registered one on the device |

## Channels

Channels are organized into five groups, by subsystem: **Operating Mode** (active preset and timed
modes — the one cross-cutting exception, since a mode isn't specific to heating or hot water),
**Central Heating** and **Hot Water** (setpoints, status, and settings per subsystem), **Device**
(hardware diagnostics), and **Alerts**.

### Operating Mode (`control#`)

| Channel ID | Type | RW | Description |
|------------|------|----|-------------|
| `control#preset-mode` | `String` | RW | Active preset: `manual`, `auto`, `holiday`, `extend`, `fireplace`. The **only** channel that can activate or cancel a mode — see [Preset modes](#preset-modes) |
| `control#vacation-duration` | `Number:Time` | RW | Vacation duration in days — **value-setter only**, writing it does not activate holiday mode. Resets to 0 on cancel |
| `control#vacation-temperature` | `Number:Temperature` | RW | Setpoint during vacation |
| `control#vacation-start` | `DateTime` | R | Vacation period start (advanced) |
| `control#vacation-end` | `DateTime` | R | Vacation period end (advanced) |
| `control#vacation-remaining` | `Number:Time` | R | Time remaining in the current holiday period (advanced) |
| `control#extend-duration` | `Number:Time` | RW | **Value-setter only** — writing it does not activate extend mode. In 15-minute increments, 15 min – 6 h. This is **additional** time on top of whatever's left until the device's next programmed schedule change, not an absolute session length. Persists across cancel (unlike the other two duration channels). See `extend-remaining` for the actual remaining-time countdown |
| `control#extend-remaining` | `Number:Time` | R | Time remaining in the current extend session (advanced) |
| `control#fireplace-duration` | `Number:Time` | RW | Fireplace mode duration in hours, 1–24 — **value-setter only**, writing it does not activate fireplace mode. Reverts to the factory default (1 h) on cancel |
| `control#fireplace-remaining` | `Number:Time` | R | Time remaining in the current fireplace session (advanced) |
| `control#next-schedule-time` | `DateTime` | R | When the central heating schedule's next entry starts (advanced) |
| `control#next-schedule-temperature` | `Number:Temperature` | R | Setpoint the central heating schedule's next entry sets (advanced) |

### Central Heating (`heating#`)

| Channel ID | Type | RW | Description |
|------------|------|----|-------------|
| `heating#target-temperature` | `Number:Temperature` | RW | Target (setpoint) room temperature |
| `heating#room-temperature` | `Number:Temperature` | R | Room temperature (built-in sensor) |
| `heating#outside-temperature` | `Number:Temperature` | R | Outside Temperature (boiler estimate) |
| `heating#weather-status` | `String` | R | Weather compensation status |
| `heating#water-temperature` | `Number:Temperature` | R | Heating Circuit Temperature |
| `heating#return-temperature` | `Number:Temperature` | R | Heating Circuit Return Temperature (advanced) |
| `heating#water-pressure` | `Number:Pressure` | R | CH circuit water pressure |
| `heating#delta-temperature` | `Number:Temperature` | R | Difference between flow and return temperature (advanced) |
| `heating#water-setpoint` | `Number:Temperature` | R | Boiler Target Water Temperature (advanced) |
| `heating#control-mode` | `String` | RW | `thermostat` (room-sensor setpoint control) or `weather-dependent` (weather-compensated heating curve) — independent of `preset-mode` (advanced) |
| `heating#flame` | `Switch` | R | Burner flame active |
| `heating#central-heating-active` | `Switch` | R | ON when the boiler is actively serving central heating demand |
| `heating#weather-temperature` | `Number:Temperature` | R | Outside temperature from the local weather service (advanced) |
| `heating#modulation-level` | `Number:Dimensionless` | R | Burner modulation level (%) |
| `heating#burning-hours` | `Number:Time` | R | Total burner hours |
| `heating#time-to-target` | `Number:Time` | R | Estimated time to reach target temperature |
| `heating#schedule-base-temperature` | `Number:Temperature` | RW | Central heating schedule's fallback temperature (advanced) |
| `heating#schedule` | `String` | R | Full central heating week schedule as JSON (advanced) |
| `heating#frost-protection` | `String` | RW | Which sensor(s) frost protection uses: `off`, `outside`, `inside`, `both` (advanced) |
| `heating#frost-protection-temperature-room` | `Number:Temperature` | RW | Indoor threshold below which frost protection activates, 4–10 °C (advanced) |
| `heating#frost-protection-temperature-outside` | `Number:Temperature` | RW | Outdoor threshold below which frost protection activates, -10–5 °C (advanced) |
| `heating#summer-eco-mode` | `Switch` | RW | Reduces heating activity once the outside temperature is warm enough (advanced) |
| `heating#summer-eco-temperature` | `Number:Temperature` | RW | Outside temperature above which summer eco mode activates (advanced) |
| `heating#heating-type` | `String` | RW | Installed heating system type, used by the weather-compensation algorithm (advanced) |
| `heating#insulation` | `String` | RW | Building insulation quality, used by the weather-compensation algorithm (advanced) |
| `heating#building-size` | `String` | RW | Building size, used by the weather-compensation algorithm (advanced) |
| `heating#wdr-temperature-influence` | `String` | RW | How strongly room temperature influences the weather-compensated heating curve (advanced) |
| `heating#climate-zone` | `Number:Temperature` | RW | Reference outdoor design temperature for the local climate (advanced) |
| `heating#max-preheat` | `String` | RW | Maximum pre-heat time before a scheduled temperature change: `off`, `1h`, `2h`, `3h`, or `automatic` (advanced) |

### Hot Water (`hotwater#`)

| Channel ID | Type | RW | Description |
|------------|------|----|-------------|
| `hotwater#target-temperature` | `Number:Temperature` | R | Hot Water Target Temperature — reflects the active schedule period |
| `hotwater#temperature` | `Number:Temperature` | R | Hot Water Temperature |
| `hotwater#hot-water-active` | `Switch` | R | ON when the boiler is actively serving hot water demand |
| `hotwater#schedule-base-temperature` | `Number:Temperature` | RW | Hot water schedule's fallback temperature — its bounds come from the device (10–65 °C on a combi boiler, wider on a system boiler with a 3-port valve kit) (advanced) |
| `hotwater#schedule` | `String` | R | Full hot water week schedule as JSON (advanced) |
| `hotwater#legionella-protection` | `Switch` | RW | Periodically heats the tank above a threshold to kill legionella bacteria (advanced) |
| `hotwater#legionella-protection-day` | `String` | RW | Weekday legionella protection runs on (advanced) |
| `hotwater#legionella-protection-time` | `String` | RW | Time of day legionella protection starts at, as `HH:mm` (advanced) |

### Device (`device#`)

| Channel ID | Type | RW | Description |
|------------|------|----|-------------|
| `device#display-brightness` | `Number:Dimensionless` | RW | Thermostat display brightness, 10–100% (advanced) |
| `device#time-zone` | `String` | RW | Configured time zone. Only `berlin` is device-confirmed; the other 9 cities are unverified — write at your own risk (advanced) |
| `device#language` | `String` | RW | Display language: `english`, `dutch`, `french`, `italian`, or `german` — verified against the app (advanced) |
| `device#wifi-signal` | `Number:Dimensionless` | R | WiFi signal quality, `0` (no signal) to `4` (excellent) — bucketed rather than raw dBm, since openHAB has no display unit to pin dBm to and would otherwise render it as watts (advanced) |

Further advanced diagnostic channels in the Device group (power supply, controller health) are also
available (visible when **Show advanced** is enabled in the UI).

### Alerts (`alerts#`)

| Channel ID | Type | RW | Description |
|------------|------|----|-------------|
| `alerts#device-errors` | `String` | R | Active device error codes |
| `alerts#boiler-errors` | `String` | R | Active boiler error codes |

## Preset modes

`preset-mode` is the **only** channel that can ever activate or cancel a mode. The duration channels
(`vacation-duration`, `extend-duration`, `fireplace-duration`) are pure value-setters — writing one only
updates the stored default for that mode, it never triggers activation on its own, matching how the
device itself treats a duration field written alone.

`preset-mode` accepts the following write values:

| Value | Description |
|-------|-------------|
| `auto` | Follow the programmed schedule. Also cancels whichever timed preset is currently active |
| `manual` | Hold the current target temperature indefinitely, ignoring the schedule. Reuses whichever temperature `target-temperature` last reported |
| `holiday` | Hold a fixed low temperature for the vacation period, using the currently stored `vacation-duration` (or the device's own configured default if none has been set) |
| `fireplace` | Temporarily reduce setpoint (fireplace warmth compensation), using the currently stored `fireplace-duration` (or 1 hour if none has been set) |
| `extend` | Temporarily extend the current schedule block, using the currently stored `extend-duration` as **additional** time on top of whatever's left until the device's next programmed schedule change — not an absolute session length (or the device's own configured default if none has been set) |

Writing an unknown value is rejected with a warning and the item reverts to its last known state.

The "device's own configured default" for holiday and extend isn't itself a channel — it's a fixed
value the device stores and this binding reads once per poll, purely as the fallback used when
activating that mode with no duration set. There's nothing to configure from openHAB's side.

To activate a mode with a **custom** duration in a single write, instead of first writing the duration
channel and then `preset-mode`, use the [Actions](#actions) below.

Use `vacation-remaining`, `extend-remaining`, or `fireplace-remaining` to see the actual countdown in
an active timed preset; the duration channels themselves only show the stored request value.

## Holiday (vacation) mode

Holiday mode holds a fixed low temperature for a defined period.

```text
Number:Time  atagone_vacation_duration  "Vacation duration"  { channel="atagone:thermostat:boiler:control#vacation-duration" }
String       atagone_preset             "Preset mode"        { channel="atagone:thermostat:boiler:control#preset-mode" }
```

To start it with a specific duration: write it to `vacation-duration` first, then write
`preset-mode = holiday`. Writing `preset-mode = holiday` alone reuses the currently-active
`vacation-duration` if a holiday is already running, otherwise starts one using the device's own stored
default duration (typically 7 days, but reflects whatever was last configured on the thermostat or in
its app). For a one-write custom-duration activation, use the `activateVacation` action instead.

`vacation-duration` resets to 0 whenever a holiday period is cancelled — it does not persist across
cancel the way `extend-duration` does.

`vacation-start` and `vacation-end` are read-only status channels that report the currently running
period; they cannot be written directly, but a future start can be scheduled via the `activateVacation`
action's underlying mechanism. Rewriting a pending/future-scheduled start before it has begun is
unsupported and can reset the device — treat a scheduled vacation as write-once until it either starts
or is cancelled.

Cancel by writing `preset-mode = auto`.

## Fireplace mode

Fireplace mode temporarily reduces the setpoint for warmth compensation while a fireplace is in use.

```text
Number:Time  atagone_fireplace  "Fireplace duration"  { channel="atagone:thermostat:boiler:control#fireplace-duration" }
String       atagone_preset     "Preset mode"          { channel="atagone:thermostat:boiler:control#preset-mode" }
```

To start it with a specific duration: write it to `fireplace-duration` first (any time unit is accepted,
e.g. `2 h` or `7200 s`), then write `preset-mode = fireplace`. Writing `preset-mode = fireplace` alone
reuses the currently stored duration. For a one-write custom-duration activation, use the
`activateFireplace` action instead.

Reading `fireplace-duration` returns the stored default duration from the device (the value used when
fireplace mode is activated from the physical thermostat). Unlike `extend-duration`, this value does
**not** persist across cancel — it always reverts to the factory default (1 h).

**Cancelling fireplace mode via the API does not take effect on its own.** Writing `preset-mode = auto`
is accepted by the device but requires confirming on the thermostat's physical display before it actually
takes effect — this is confirmed device behavior, not a binding limitation, and no payload avoids it. The
binding logs a warning when this happens. The `cancelMode` action reports this explicitly via its
`requiresPhysicalConfirmation` output.

## Actions

The binding registers ten [Thing Actions](https://www.openhab.org/docs/configuration/rules-dsl.html#thing-actions)
under the `atagone` scope: four for activating or cancelling a mode with a custom duration in a single
call, instead of the two-write channel pattern described above (set the duration channel, then
`preset-mode`); four for editing weekly schedules period-by-period, which no channel exposes at all;
and two for replacing a whole week's schedule in one device write.

| Action | Description |
|--------|-------------|
| `activateVacation(long durationSeconds)` | Activates holiday mode immediately for the given duration |
| `activateExtend(long durationSeconds)` | Activates extend mode immediately, additive to the time remaining until the next schedule boundary |
| `activateFireplace(long durationSeconds)` | Activates fireplace mode immediately for the given duration |
| `cancelMode()` | Cancels whichever timed preset is currently active or pending and returns to auto. Returns `true` if the mode being left is fireplace, meaning the write is accepted but requires confirming on the thermostat's physical display to actually take effect |
| `setChSchedulePeriod(String weekday, int periodIndex, int startMinutes, int endMinutes, double temperatureCelsius)` | Sets or replaces one time period in a weekday's central heating schedule. `periodIndex` is 0-based within that day's existing periods; pass the day's current period count to append a new one |
| `clearChSchedulePeriod(String weekday, int periodIndex)` | Removes one time period from a weekday's central heating schedule, shifting later periods down |
| `setDhwSchedulePeriod(String weekday, int periodIndex, int startMinutes, int endMinutes, double temperatureCelsius)` | Same as `setChSchedulePeriod`, for the hot water schedule |
| `clearDhwSchedulePeriod(String weekday, int periodIndex)` | Same as `clearChSchedulePeriod`, for the hot water schedule |
| `setChSchedule(String json)` | Replaces the central heating schedule in one device write, same JSON shape as the `heating#schedule` channel |
| `setDhwSchedule(String json)` | Same as `setChSchedule`, for the hot water schedule |

Each activation/cancel action composes the same multi-field write the corresponding `preset-mode`
channel value uses internally (e.g. `activateVacation` sets both `ch_mode` and the device's
`start_vacation` field in one request) — vacation in particular cannot be activated with `ch_mode`
alone, and `cancelMode` handles the active-vs-pending distinction for cancelling a vacation
automatically, so a script author never needs to know these details.

The schedule actions take a weekday name (`monday`..`sunday`), not a raw day number — the device uses
two different, unrelated weekday numbering schemes internally, and a name sidesteps that ambiguity.
`weekday` is case-insensitive. Every call resends the entire week's schedule with only the targeted
period changed; **writing a schedule has been observed to make the thermostat briefly unresponsive
(around 100 seconds)**, so avoid calling these from a tight loop or in response to frequent events.

**All four schedule-write actions reject a period that overlaps another period already on the same
weekday** — two periods `[aStart, aEnd)` and `[bStart, bEnd)` overlap if `aStart < bEnd && aEnd >
bStart` (half-open intervals, so one period ending exactly when the next starts is not an overlap).
`setChSchedulePeriod`/`setDhwSchedulePeriod` exclude the period being replaced from that comparison.
Rejection returns `false` (or, for `setChSchedule`/`setDhwSchedule`, `null` from the underlying
compose step) with no write sent — the same generic failure signal every other invalid input on
these actions already uses (unknown weekday, out-of-range index, malformed JSON, invalid bounds).
There's no separate exception type or error code to catch a schedule conflict specifically; a caller
that needs to tell the two apart has to check its own input against `heating#schedule`/
`hotwater#schedule` before calling, the same way it must already avoid the other rejection cases.

`setChSchedule`/`setDhwSchedule` take the same JSON shape `heating#schedule`/`hotwater#schedule`
publish:

```json
{"baseTemp":22.5,"days":{"monday":[{"start":360,"end":1260,"temp":20.5}],"tuesday":[],"wednesday":[],"thursday":[],"friday":[],"saturday":[],"sunday":[]}}
```

`start`/`end` are minutes since midnight, matching the per-period actions' own units. A weekday absent
from `days` is resent unchanged from the last poll, so a caller only needs to name the day(s) it
actually edited — the device still requires the whole schedule object on every write, the binding
composes that from the JSON given plus what it last polled. `baseTemp` is optional and defaults to the
current value. One call replaces up to a full week in a single device write, rather than one write per
period at the firmware's 2-second minimum interval between requests. Both actions return `true` once
the write is parsed, validated and queued — not once the device has confirmed it, since confirmation
can take up to the ~100 s mentioned above; watch the corresponding `schedule` channel, which republishes
as soon as the device acknowledges the write, to see the confirmed result.

Example from a rule:

```javascript
actions.thingActions("atagone", "atagone:thermostat:boiler").activateFireplace(7200);
actions.thingActions("atagone", "atagone:thermostat:boiler").setChSchedulePeriod("monday", 0, 360, 1320, 20.0);
actions.thingActions("atagone", "atagone:thermostat:boiler").setChSchedule('{"days":{"monday":[{"start":360,"end":1320,"temp":20.0}]}}');
```

## Full example

### `atagone.items`

```text
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
