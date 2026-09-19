# WS90 Weather Station: Setup and Rain Automation with openHAB

The Ecowitt WS90 is a solar-powered, all-in-one weather sensor (temperature, humidity, wind, rain, UV, pressure) that connects to openHAB as a Shelly BLU device (thing-type `shellybluws90`).
It does not talk to openHAB directly — a Shelly Plus/Pro device acts as a Bluetooth gateway and forwards the sensor's BTHome advertisements to the binding.

This tutorial assumes you are already familiar with adding devices to openHAB in general.
It focuses on the WS90-specific setup steps, a firmware quirk you should know about before building automations, and four example rules that react to sensor changes.

## Prerequisites

- A Shelly Plus/Pro device nearby with Bluetooth enabled, used as the BLU gateway.
- Familiarity with the general Shelly BLU pairing procedure — see [Discovery of BLU Devices](../README.md#discovery-of-blu-devices) in the README; it is not repeated here.

## 1. Adding the WS90

The WS90 setup follows the standard BLU pairing flow from the README, with one difference: it is solar-powered and broadcasts continuously, so **no button press is needed** to make it appear in the Inbox — it shows up automatically once it is within range of a configured BLU gateway.

Because the WS90 sends its readings split across two BTHome packet types (environmental and atmospheric), it can take a couple of advertisement cycles after adding the Thing until every channel is populated for the first time.

## 2. Channels

All available channels (temperature, humidity, wind, gust, pressure, sea-level pressure, dew point, apparent temperature, rain status, precipitation, UV index, battery, etc.) are documented in the README's channel table — see [Ecowitt WS90 Weather Station](../README.md#ecowitt-ws90-weather-station-shelly-blu-thing-type-shellybluws90). Link the channels you need to items as usual; this tutorial only covers the two channels used in the automation examples below: `rainStatus` (Switch) and `precipitation` (Number:Length).

## 3. Firmware Quirk: Rain Indicator Latching

The `rainStatus` channel mirrors the WS90's raw BTHome Moisture reading (`0x20`) as reported by the device firmware.
The sensor's piezo element stays wet for a while after it has actually stopped raining, so **`rainStatus` keeps reporting ON well after the rain has ended** — this is a characteristic of the WS90 firmware/hardware, not something the binding can filter out, since the binding only forwards the value the device itself reports.

Practical implications for automation:

- Don't treat `rainStatus` transitions as precise start/stop timestamps for the rain event itself — in particular, expect the `ON → OFF` transition to lag behind reality by anywhere from several minutes to longer, depending on drying conditions.
- **Don't rely on `rainStatus` alone to decide "has it stopped raining"** — this is exactly the failure mode above. Example 2 below builds a more reliable rain indicator on top of the `precipitation` counter instead, using `rainStatus` only as a fast trigger to start watching, not as the source of truth for when it ends.
- If you only need "is it currently, or was it very recently, wet outside" (e.g. a simple sensor-hold-off indicator, not an automation decision), the README's own debounce rule under the WS90 channel table is enough — see the note there.

## 4. Example Rules

All four examples use the Rules DSL for consistency with the rest of the README; the same logic works in any other openHAB rule language.

### Example 1: React to a Sensor Value Change

The base pattern used throughout this tutorial: trigger a rule when a channel's value changes, and act on the new value. Here, a storm warning notification when gust speed crosses a threshold:

```java
rule "WS90 Gust Warning"
when
    Item WS90_GustSpeed changed
then
    if ((WS90_GustSpeed.state as QuantityType<?>).doubleValue > 15.0) {
        logInfo("WS90", "Gust speed high: " + WS90_GustSpeed.state)
        sendBroadcastNotification("Storm warning: gusts over 15 m/s")
    }
end
```

This "changed" trigger is the right tool for most channels. Rain detection (Example 2) is the exception — see the firmware quirk in Section 3 — and needs a different approach.

### Example 2: Reliable Rain Detection

Because `rainStatus` can stay latched ON long after the rain has actually stopped, treat it as a **trigger to start watching**, not as the final answer. The actual "is it still raining" question is answered by checking whether `precipitation` — the monotonic rain counter — is still increasing:

- If `precipitation` has increased in the last check window, it's still raining, regardless of what `rainStatus` says.
- If `precipitation` has been flat for a full check window, rain has stopped, even if `rainStatus` is still stuck ON.
- If `rainStatus` itself reports OFF, that also ends it immediately — whichever condition is met first wins.

This needs a persisted `WS90_IsRaining` Switch item (`restoreOnStartup`) to hold the computed result, and a periodic check via a cron trigger rather than a rule-local `createTimer` — a cron trigger reschedules itself automatically after an openHAB restart, while an in-memory timer does not, and `WS90_IsRaining` recovers its last value from persistence either way:

```java
Switch WS90_IsRaining "It's raining [%s]" { restoreOnStartup }
```

```java
rule "WS90 Rain Started"
when
    Item WS90_RainStatus changed to ON
then
    WS90_IsRaining.postUpdate(ON)
    logInfo("WS90", "Rain indicator ON — watching precipitation trend")
end

rule "WS90 Rain Stopped (device report)"
when
    Item WS90_RainStatus changed to OFF
then
    WS90_IsRaining.postUpdate(OFF)
    logInfo("WS90", "Rain indicator cleared by device")
end

rule "WS90 Rain Trend Check"
when
    Time cron "0 */10 * * * ?"
then
    if (WS90_IsRaining.state == ON) {
        val delta = WS90_Precipitation.deltaSince(now.minusMinutes(10))
        if (delta === null || delta <= 0) {
            WS90_IsRaining.postUpdate(OFF)
            logInfo("WS90", "No precipitation increase in the last 10 min — rain stopped (device still reports wet sensor)")
        }
    }
end
```

`WS90_IsRaining` is what the rest of your rules (e.g. pausing irrigation) should link to, not `rainStatus` directly. This also survives an openHAB restart mid-rain: `restoreOnStartup` puts `WS90_IsRaining` back to `ON`, and the next cron cycle re-evaluates the precipitation trend to confirm or clear it — no in-memory state needs to survive the restart.

### Example 3: 1h / 24h Rainfall via openHAB Persistence

`precipitation` is a **monotonic counter** — the total accumulated rainfall in mm since the sensor was last reset — not a per-interval amount. The WS90 firmware itself tracks hourly/24h totals internally, but does not expose them over BTHome, so the binding has no value to surface as a channel.

The README already suggests deriving these with openHAB's own persistence instead of waiting for a binding feature, since it's just the counter's _delta over a time window_ — exactly what the persistence extension's `deltaSince()` provides, the same method used to build the trend check in Example 2.

**Requirements:**

- A persistence service (e.g. `rrd4j` or `influxdb`) must be configured to store `WS90_Precipitation`, with a strategy that captures every update (`everyUpdate`/`everyChange`) — an averaging/decimating strategy will skew the delta. This is the same requirement Example 2 relies on; one persistence configuration covers both.
- Two extra items to hold the computed rolling totals:

```java
Number:Length WS90_RainLast1h  "Rain last hour [%.1f mm]"
Number:Length WS90_RainLast24h "Rain last 24h [%.1f mm]"
```

**Rule**, recomputed periodically (every 5 minutes here):

```java
rule "WS90 Rolling Rain Totals"
when
    Time cron "0 */5 * * * ?"
then
    val rain1h  = WS90_Precipitation.deltaSince(now.minusHours(1))
    val rain24h = WS90_Precipitation.deltaSince(now.minusHours(24))

    // deltaSince returns null if there's no persisted data far enough back yet
    // (e.g. right after openHAB start), and a negative value if the sensor's
    // own counter was reset in between (e.g. after a battery change) — clamp both cases to 0.
    WS90_RainLast1h.postUpdate(if (rain1h  === null || rain1h  < 0) 0 else rain1h)
    WS90_RainLast24h.postUpdate(if (rain24h === null || rain24h < 0) 0 else rain24h)
end
```

`deltaSince(timestamp)` uses the default persistence service; pass a service ID as a second argument (e.g. `.deltaSince(now.minusHours(1), "influxdb")`) if `precipitation` isn't stored in your default one.

### Example 4: Daily Minimum / Maximum Temperature Since Midnight

The WS90 reports the current temperature only, so the daily low and high are derived from persistence as well, using the `minimumSince()` and `maximumSince()` extension methods.
The rule recalculates both values each time a new temperature arrives, starting from midnight of the current day.

**Requirements:**

- A persistence service (e.g. `rrd4j` or `influxdb`) must be configured to store `WS90_Temperature`, with a strategy that captures every update (`everyUpdate`/`everyChange`).
  A coarser strategy (e.g. `everyMinute` or `everyHour`) works too, but the min/max then only reflect the persisted samples.
- Two extra items to hold the daily extremes:

```java
Number:Temperature WS90_TemperatureMin "Temperature min today [%.1f °C]"
Number:Temperature WS90_TemperatureMax "Temperature max today [%.1f °C]"
```

**Rule**, triggered on every temperature update:

```java
rule "WS90 Daily Temperature Min/Max"
when
    Item WS90_Temperature received update
then
    val dayStart = now.toLocalDate().atStartOfDay(now.getZone())

    val minimum = WS90_Temperature.minimumSince(dayStart)
    val maximum = WS90_Temperature.maximumSince(dayStart)

    // minimumSince/maximumSince return null if nothing has been persisted since midnight yet
    if (minimum !== null) {
        WS90_TemperatureMin.postUpdate(minimum.state)
    }

    if (maximum !== null) {
        WS90_TemperatureMax.postUpdate(maximum.state)
    }
end
```

`now.toLocalDate().atStartOfDay(now.getZone())` yields midnight in your local time zone, so the values reset automatically with the first temperature update after midnight.
As in Example 3, pass a service ID as a second argument (e.g. `.minimumSince(dayStart, "influxdb")`) if `temperature` isn't stored in your default persistence service.

## 5. How Apparent Temperature Is Calculated

The `apparentTemp` channel is a "feels like" temperature computed by the binding itself from three WS90 readings — temperature, humidity, and wind speed — no rule needed; it's published automatically once all three inputs are available.

It matches everyday experience with weather:

- **Humidity makes it feel hotter.** On a humid day, sweat evaporates more slowly, so the air feels warmer than the thermometer says. Example: at 25 °C, apparent temperature stays close to 25 °C when the air is dry, but climbs to around 29 °C at 80% humidity with no wind — a muggy day feels several degrees hotter than the actual reading.
- **Wind makes it feel colder.** Moving air carries heat away from skin faster, the same effect as wind chill. Example: at 25 °C with moderate humidity, a light breeze of 8 m/s (~30 km/h) can bring the apparent temperature down to around 20 °C.

If both effects apply at once, they partly cancel out — a warm, humid, and windy day can feel close to the actual temperature, even though a calm day at the same reading would feel noticeably hotter.

If both humidity and wind readings aren't available yet (e.g. right after adding the Thing), `apparentTemp` isn't published until they are.

<details>
<summary>Exact formula (Steadman, 1979), for reference</summary>

1. Water vapor pressure `e` (hPa), from temperature `T` (°C) and relative humidity `RH` (%):

   `e = (RH / 100) × 6.105 × exp((17.27 × T) / (237.7 + T))`

1. Apparent temperature `AT` (°C), from `T`, `e`, and wind speed `v` (m/s):

   `AT = T + 0.33 × e − 0.70 × v − 4.0`

</details>

## Related

- [Discovery of BLU Devices](../README.md#discovery-of-blu-devices) — general BLU gateway pairing.
- [Ecowitt WS90 Weather Station](../README.md#ecowitt-ws90-weather-station-shelly-blu-thing-type-shellybluws90) — full channel table and the `rainStatus` debounce-switch note.
- [openHAB Persistence documentation](https://www.openhab.org/docs/configuration/persistence.html) — configuring services, strategies, and the full set of extension methods (`deltaSince`, `sumSince`, `averageSince`, ...).
