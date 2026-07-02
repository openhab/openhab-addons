# ADR-0001: New Thing Type `onecall-v3-timeseries`

**Status:** Accepted

**Date:** 2026-06-30

**Updated:** 2026-07-02 — renamed to `onecall-v3-timeseries`; `forecastResolution` parameter removed.

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
5-day window at the finest available resolution.

## Decision

Introduce a new Thing type `onecall-v3-timeseries` that:

1. Provides exactly **two channel groups**: `current` and `forecast`.
1. The `forecast` group delivers **one TimeSeries per channel**, merging data from two API sources.
1. Always fetches **both** API tiers — no configuration switch needed.
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

### Option A: New Thing `onecall-v3-timeseries` (chosen)

| Dimension | Assessment |
|---|---|
| Breaking change | None — existing Things untouched |
| Complexity | Medium — new handler, new channel group type |
| Reuse | High — reuses existing Connection methods and DTOs |
| API cost | One Call 3.0 (paid) + Forecast5 (free tier) |
| DB growth | ~8,760 entries/channel/year |

**Pros:**

- Zero risk to existing users.
- TimeSeries-only — no legacy slot overhead.
- Single fixed resolution: hourly (0–48 h) + 3-hourly (48–120 h).
- No configuration complexity — one clear mode.

**Cons:**

- One Call 3.0 subscription required.
- `dew_point` and `uvi` channels have no entries beyond 48 h (Forecast5 does not provide them).

### Option B: Extend existing `onecall` Thing

**Pros:** No new Thing for users to configure.

**Cons:** Breaking change risk, mixes concerns, existing users affected by channel group changes.
Rejected.

### Option C: configurable `forecastResolution`

**Pros:** Free-tier users could skip the One Call subscription.

**Cons:** Added complexity, extra code paths, tested failure modes.
Removed in revision 2026-07-02 — the Thing name `onecall-v3-timeseries` already signals that
One Call API 3.0 is required; a second resolution mode adds no value.

## Configuration

```text
thing openweathermap:onecall-v3-timeseries:myForecast "My Forecast" (openweathermap:weather-api:myBridge) {
    configuration:
        location = "48.1374,11.5755"
}
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `location` | text | yes | Geographic coordinates as `lat,lon` |

## Channel Groups

### `current` — reuses existing `oneCallCurrent` channel group type

No changes to the existing channel group type definition.

### `forecast` — new `oneCallV3TimeSeries` channel group type

A new channel group type is required because it merges channels from both the One Call
hourly set and the Forecast5 3-hourly set. It is **not** a modification of
`oneCallHourlyTimeSeries` — it is a new addition to `channel-types.xml`.

Channels and their data availability:

| Channel | 0–48 h (OneCall) | 48–120 h (Forecast5) |
|---|---|---|
| `condition` / `condition-id` / icons | ✅ | ✅ |
| `temperature` | ✅ | ✅ |
| `apparent-temperature` | ✅ | ✅ |
| `pressure` | ✅ | ✅ |
| `humidity` | ✅ | ✅ |
| `wind-speed` / `wind-direction` / `gust-speed` | ✅ | ✅ |
| `cloudiness` | ✅ | ✅ |
| `visibility` | ✅ | ✅ |
| `precip-probability` | ✅ | ✅ |
| `rain` / `snow` | ✅ | ✅ |
| `dew-point` | ✅ | — (UNDEF) |
| `uvindex` | ✅ | — (UNDEF) |

## New Files (purely additive)

- `handler/OpenWeatherMapOneCallV3TimeSeriesHandler.java`

## Required Changes to Existing Code

> **Per project convention: each of these changes requires explicit approval before implementation.**

### Change 1 — `OpenWeatherMapBindingConstants.java`

Add constant:

```java
public static final ThingTypeUID THING_TYPE_ONECALL_V3_TIMESERIES =
    new ThingTypeUID(BINDING_ID, "onecall-v3-timeseries");
```

### Change 2 — `AbstractOpenWeatherMapHandler.java`

Add `THING_TYPE_ONECALL_V3_TIMESERIES` to the `SUPPORTED_THING_TYPES` set.

### Change 3 — `OpenWeatherMapHandlerFactory.java`

Add `else if` branch for `THING_TYPE_ONECALL_V3_TIMESERIES` returning a new
`OpenWeatherMapOneCallV3TimeSeriesHandler`.

### Change 4 — `dto/forecast/hourly/List.java`

Add two missing fields that the Forecast5 API already returns but the DTO currently ignores:

```java
private @Nullable Double pop;           // probability of precipitation
private @Nullable Integer visibility;   // average visibility in metres
```

### Additive XML changes (no behaviour change to existing Things)

- `thing-types.xml` — add `<thing-type id="onecall-v3-timeseries">` entry.
- `channel-types.xml` — add `<channel-group-type id="oneCallV3TimeSeries">` entry.
- `config.xml` — add `<config-description uri="thing-type:openweathermap:onecall-v3-timeseries">`.

## Consequences

- Users with a One Call 3.0 subscription get a single persistent TimeSeries covering 5 days.
- `dew-point` and `uvindex` channels will have `UNDEF` entries for timestamps beyond 48 h.
  This is expected and documented.
- The DTO fix for `pop` and `visibility` (Change 4) also benefits the existing
  `weather-and-forecast` Thing as a side effect.
- No existing Things, handlers, or channel types are removed or modified in behaviour.

## Action Items

1. [x] User approval for Change 1 — `OpenWeatherMapBindingConstants.java`
1. [x] User approval for Change 2 — `AbstractOpenWeatherMapHandler.java`
1. [x] User approval for Change 3 — `OpenWeatherMapHandlerFactory.java`
1. [x] User approval for Change 4 — `dto/forecast/hourly/List.java`
1. [x] Implement `OpenWeatherMapOneCallV3TimeSeriesHandler.java`
1. [x] Add XML definitions (thing-types, channel-types, config)
1. [x] Write tests for merge logic (One Call hourly + Forecast5 cutoff)
1. [x] Rename Thing type to `onecall-v3-timeseries` (2026-07-02)
1. [x] Remove `forecastResolution` configuration parameter (2026-07-02)
1. [x] Rename handler to `OpenWeatherMapOneCallV3TimeSeriesHandler` (2026-07-02)
