# ADR-0001: New Thing Type `combined-forecast`

**Status:** Accepted

**Date:** 2026-06-30

**Deciders:** Binding maintainers

## Context

The existing binding offers two separate Things for weather forecasts:

- `weather-and-forecast` — uses the free-tier Forecast5 API (`data/2.5/forecast`, 3-hour resolution,
  5 days) and the paid 16-day daily forecast API. Provides individual slot channel groups
  (`forecastHours03`, `forecastHours06`, …). No TimeSeries support.
- `onecall` — uses the paid One Call API 3.0. Provides both individual slot channel groups
  (`forecastHours01`–`forecastHours24`, `forecastMinutes01`–`forecastMinutes60`,
  `forecastToday`–`forecastDay7`) **and** TimeSeries channel groups. High granularity (minutely,
  hourly, daily), but capped at 48 hours for hourly data.

Neither Thing combines the strengths of both APIs into a single, unified TimeSeries.
The individual slot channel groups are a legacy pattern predating openHAB's TimeSeries support
and add 92+ channel groups of overhead with no benefit over Persistence queries.

A new user need has been identified: a single `forecast` TimeSeries that covers the full
5-day window at the finest available resolution, with configurable granularity to control
database growth.

## Decision

Introduce a new Thing type `combined-forecast` that:

1. Provides exactly **two channel groups**: `current` and `forecast`.
1. The `forecast` group delivers **one TimeSeries per channel**, merging data from two API sources.
1. Exposes a single `forecastResolution` configuration parameter to control which API tiers are
   fetched.
1. Makes **no changes to existing Thing types** — this is a purely additive new Thing.

## Data Sources and Merge Strategy

| Time window | Source | Resolution | API endpoint |
|---|---|---|---|
| 0–48 h | One Call API 3.0 (hourly) | 1 h | `data/3.0/onecall` |
| 48–120 h | Forecast5 | 3 h | `data/2.5/forecast` |

Both API calls are already implemented in `OpenWeatherMapConnection`:

- `getOneCallAPIData()` — returns current + hourly in a **single HTTP request**.
- `getHourlyForecastData()` — returns the 3-hour Forecast5 slots.

Merge rule for the `forecast` TimeSeries: append all Forecast5 slots whose `dt` is strictly
greater than the last One Call hourly slot's `dt`. No overlap, no gap.

## Options Considered

### Option A: New Thing `combined-forecast` (chosen)

| Dimension | Assessment |
|---|---|
| Breaking change | None — existing Things untouched |
| Complexity | Medium — new handler, new channel group type |
| Reuse | High — reuses existing Connection methods and DTOs |
| API cost | One Call 3.0 (paid) + Forecast5 (free tier) |
| DB growth (default) | ~8,760 entries/channel/year |

**Pros:**

- Zero risk to existing users.
- TimeSeries-only — no legacy slot overhead.
- Configurable granularity per user need.
- `3hourly` mode requires only the free-tier Forecast5 API (no One Call subscription needed).

**Cons:**

- One Call 3.0 subscription required for `hourly` mode.
- `dew_point` and `uvi` channels will have no entries beyond 48 h (Forecast5 does not provide them).

### Option B: Extend existing `onecall` Thing

**Pros:** No new Thing for users to configure.

**Cons:** Breaking change risk, mixes concerns, existing users affected by channel group changes.
Rejected.

## Configuration

```text
thing openweathermap:combined-forecast:myForecast "My Forecast" (openweathermap:weather-api:myBridge) {
    configuration:
        location    = "48.1374,11.5755"
        forecastResolution = "hourly"
}
```

| Parameter | Type | Default | Values |
|---|---|---|---|
| `location` | text | — | `lat,lon` |
| `forecastResolution` | text | `hourly` | `hourly`, `3hourly` |

### Resolution behaviour

| `forecastResolution` | TimeSeries content | API calls | Entries/year |
|---|---|---|---|
| `hourly` _(default)_ | 0–48 h (1 h) + 48–120 h (3 h) | 2 | ~8,760 |
| `3hourly` | 0–120 h (3 h) | 1 (Forecast5 only) | ~2,920 |

## Channel Groups

### `current` — reuses existing `oneCallCurrent` channel group type

No changes to the existing channel group type definition.

### `forecast` — new `combinedForecastTimeSeries` channel group type

A new channel group type is required because it merges channels from both the One Call
hourly set and the Forecast5 3-hourly set. It is **not** a modification of
`oneCallHourlyTimeSeries` — it is a new addition to `channel-types.xml`.

Channels and their data availability per resolution:

| Channel | `hourly` | `3hourly` |
|---|---|---|
| `time-stamp` | ✅ 0–48 h | ✅ 0–120 h |
| `condition` / `condition-id` / icons | ✅ | ✅ |
| `temperature` | ✅ | ✅ |
| `apparent-temperature` | ✅ | ✅ |
| `pressure` | ✅ | ✅ |
| `humidity` | ✅ | ✅ |
| `wind-speed` / `wind-direction` / `gust-speed` | ✅ | ✅ |
| `cloudiness` | ✅ | ✅ |
| `visibility` | ✅ | ✅ (needs DTO fix¹) |
| `precip-probability` | ✅ | ✅ (needs DTO fix¹) |
| `rain` / `snow` | ✅ | ✅ |
| `dew-point` | ✅ 0–48 h only | — |
| `uvi` | ✅ 0–48 h only | — |

¹ See "Required Changes to Existing Code" below.

## New Files (purely additive)

- `handler/OpenWeatherMapCombinedForecastHandler.java`
- `config/OpenWeatherMapCombinedForecastConfiguration.java`

## Required Changes to Existing Code

> **Per project convention: each of these changes requires explicit approval before implementation.**

### Change 1 — `OpenWeatherMapBindingConstants.java`

Add constant:

```java
public static final ThingTypeUID THING_TYPE_COMBINED_FORECAST =
    new ThingTypeUID(BINDING_ID, "combined-forecast");
```

### Change 2 — `AbstractOpenWeatherMapHandler.java`

Add `THING_TYPE_COMBINED_FORECAST` to the `SUPPORTED_THING_TYPES` set.

### Change 3 — `OpenWeatherMapHandlerFactory.java`

Add `else if` branch for `THING_TYPE_COMBINED_FORECAST` returning a new
`OpenWeatherMapCombinedForecastHandler`.

### Change 4 — `dto/forecast/hourly/List.java`

Add two missing fields that the Forecast5 API already returns but the DTO currently ignores:

```java
private @Nullable Double pop;           // probability of precipitation
private @Nullable Integer visibility;   // average visibility in metres
```

### Additive XML changes (no behaviour change to existing Things)

- `thing-types.xml` — add `<thing-type id="combined-forecast">` entry.
- `channel-types.xml` — add `<channel-group-type id="combinedForecastTimeSeries">` entry.
- `config.xml` — add `<config-description uri="thing-type:openweathermap:combined-forecast">`.

## Consequences

- Users with a One Call 3.0 subscription get a single persistent TimeSeries covering 5 days.
- Users on the free tier can use `forecastResolution=3hourly` with Forecast5 only.
- `dew-point` and `uvi` channels will have `UNDEF` entries for timestamps beyond 48 h when using
  `hourly` resolution. This is expected and documented.
- The DTO fix for `pop` and `visibility` (Change 4) also benefits the existing
  `weather-and-forecast` Thing as a side effect.
- No existing Things, handlers, or channel types are removed or modified in behaviour.

## Action Items

1. [x] User approval for Change 1 — `OpenWeatherMapBindingConstants.java`
1. [x] User approval for Change 2 — `AbstractOpenWeatherMapHandler.java`
1. [x] User approval for Change 3 — `OpenWeatherMapHandlerFactory.java`
1. [x] User approval for Change 4 — `dto/forecast/hourly/List.java`
1. [x] Implement `OpenWeatherMapCombinedForecastConfiguration.java`
1. [x] Implement `OpenWeatherMapCombinedForecastHandler.java`
1. [x] Add XML definitions (thing-types, channel-types, config)
1. [x] Write tests for merge logic (One Call hourly + Forecast5 cutoff)
