# GME Binding

The GME binding integrates openHAB with the electricity market data services provided by the Italian Gestore dei Mercati Energetici (GME).

It provides access to Italian day-ahead electricity market prices, including national PUN prices and zonal prices.

The binding provides configurable 60, 30 or 15-minute price time series together with current, next, average, minimum and maximum price information.

It can be used for energy monitoring and automation scenarios such as load shifting, battery charging optimisation and photovoltaic self-consumption strategies.

## Supported Things

The binding supports the following Thing types:

| Thing Type UID | Type | Description |
|----------------|------|-------------|
| `gme:api` | Bridge | Connection to a GME API account and common API configuration |
| `gme:pun` | Thing | PUN and zonal electricity market prices |

The `gme:pun` Thing must be associated with a `gme:api` Bridge.

## Thing Configuration

### `gme:api` Bridge Configuration

The API Bridge manages authentication with GME and configuration shared by the price Things.

| Name | Type | Description | Default | Required | Advanced |
|------|------|-------------|---------|----------|----------|
| `username` | text | Username for the GME API | N/A | yes | no |
| `password` | text | Password for the GME API | N/A | yes | no |
| `initialPasswordChangedAt` | text | Optional initial password change date in `YYYY-MM-DD` format | N/A | no | yes |
| `marketZone` | text | Italian electricity market zone used for zonal prices | N/A | no | no |
| `granularity` | text | GME MGP market granularity: `PT60`, `PT30` or `PT15` | `PT60` | no | no |
| `refreshInterval` | integer | Interval in minutes for refreshing the current-day market dataset from GME | 60 | no | yes |

The following market zones are supported:

| Value | Market Zone |
|-------|-------------|
| `NORD` | Northern Italy |
| `CNOR` | Central-Northern Italy |
| `CSUD` | Central-Southern Italy |
| `SUD` | Southern Italy |
| `CALA` | Calabria |
| `SICI` | Sicily |
| `SARD` | Sardinia |

The configured market zone affects only zonal price channels.

National PUN price channels are independent of the selected market zone.

The `granularity` setting controls the market time unit requested from GME. `PT60` is the default and preserves hourly behaviour. `PT30` and `PT15` expose half-hourly and quarter-hourly MGP data respectively. GME supports these granularities for MGP data from 1 October 2025.

### Password Tracking

GME API passwords have a limited validity period.

The Bridge tracks the age of the configured password and exposes its estimated expiry through dedicated channels.

The password itself is never copied into the binding storage for this purpose.

Instead, the binding stores a SHA-256 fingerprint of the credentials and uses it to detect a credential change after a successful GME authentication.

When the binding is configured for the first time, `initialPasswordChangedAt` can optionally be used to provide the actual change date of an already existing GME password.

For example:

```text
2026-08-15
```

If this parameter is omitted during the first successful authentication, the current date and time are used as the initial password change time.

After tracking has been initialised, changing the GME password does not require updating `initialPasswordChangedAt`.

A successfully authenticated password change is detected automatically and starts a new password validity period.

An authentication failure does not change the stored password change date.

### `gme:pun` Thing Configuration

The PUN Thing does not require additional configuration.

It obtains authentication, refresh interval, market zone and market granularity information from its parent `gme:api` Bridge.

## Channels

### `gme:api` Bridge Channels

| Channel | Type | Read/Write | Description |
|---------|------|------------|-------------|
| `password-last-changed` | DateTime | R | Date and time when the current password was first observed or successfully changed |
| `password-expiry` | DateTime | R | Estimated expiry date and time of the current GME API password |
| `password-days-remaining` | Number | R | Estimated number of days remaining before password expiry |
| `password-status` | String | R | Password lifecycle status: `OK`, `CHANGE_SOON` or `EXPIRED` |

The password status becomes `CHANGE_SOON` after five months.

The estimated password expiry is six months after the tracked password change date.

### `gme:pun` Price Channels

| Channel | Type | Read/Write | Description |
|---------|------|------------|-------------|
| `current-price` | Number:EnergyPrice | R | Current PUN price for the active market interval |
| `next-price` | Number:EnergyPrice | R | Next PUN price for the following market interval |
| `today-prices` | Number:EnergyPrice | R | Today's PUN prices as a time series at the configured market granularity |
| `tomorrow-prices` | Number:EnergyPrice | R | Tomorrow's PUN prices as a time series at the configured market granularity |
| `today-zonal-prices` | Number:EnergyPrice | R | Today's prices for the configured market zone as a time series at the configured market granularity |
| `tomorrow-zonal-prices` | Number:EnergyPrice | R | Tomorrow's prices for the configured market zone as a time series at the configured market granularity |
| `today-average` | Number:EnergyPrice | R | Average PUN price for today |
| `today-min` | Number:EnergyPrice | R | Minimum PUN price for today |
| `today-max` | Number:EnergyPrice | R | Maximum PUN price for today |
| `today-min-time` | DateTime | R | Start time of today's minimum-price period |
| `today-max-time` | DateTime | R | Start time of today's maximum-price period |
| `tomorrow-average` | Number:EnergyPrice | R | Average PUN price for tomorrow |
| `tomorrow-min` | Number:EnergyPrice | R | Minimum PUN price for tomorrow |
| `tomorrow-max` | Number:EnergyPrice | R | Maximum PUN price for tomorrow |
| `tomorrow-min-time` | DateTime | R | Start time of tomorrow's minimum-price period |
| `tomorrow-max-time` | DateTime | R | Start time of tomorrow's maximum-price period |
| `tomorrow-available` | Switch | R | Indicates whether tomorrow's market data are available |
| `last-update` | DateTime | R | Time of the most recent successful market data update |

Energy prices are exposed as `Number:EnergyPrice` values in `EUR/kWh`.

Price channels also provide openHAB time series data.

The binding handles Italian daylight-saving-time transitions for every supported market granularity, including 23/24/25-hour days and 92/96/100 quarter-hour periods.

## Authentication

The binding authenticates against the GME API using the username and password configured on the Bridge.

The authentication token is cached and reused for subsequent API requests.

If GME rejects a request because the token is no longer valid, the binding invalidates the cached token, authenticates again and retries the request once.

## Full Example

### Thing Configuration

```java
Bridge gme:api:account "GME API Account" [
    username="your_username",
    password="your_password",
    initialPasswordChangedAt="2026-08-15",
    marketZone="NORD",
    granularity="PT60",
    refreshInterval=60
] {
    Thing pun pun "GME Electricity Prices"
}
```

The `initialPasswordChangedAt` parameter can be omitted once password tracking has already been initialised.

### Item Configuration

```java
Number:EnergyPrice GME_CurrentPrice "Current PUN [%.6f %unit%]" {
    channel="gme:pun:account:pun:current-price"
}

Number:EnergyPrice GME_NextPrice "Next PUN [%.6f %unit%]" {
    channel="gme:pun:account:pun:next-price"
}

Number:EnergyPrice GME_TodayAverage "Today's Average [%.6f %unit%]" {
    channel="gme:pun:account:pun:today-average"
}

Number:EnergyPrice GME_TodayMinimum "Today's Minimum [%.6f %unit%]" {
    channel="gme:pun:account:pun:today-min"
}

Number:EnergyPrice GME_TodayMaximum "Today's Maximum [%.6f %unit%]" {
    channel="gme:pun:account:pun:today-max"
}

DateTime GME_TodayMinimumTime "Today's Minimum Time [%1$tH:%1$tM]" {
    channel="gme:pun:account:pun:today-min-time"
}

DateTime GME_TodayMaximumTime "Today's Maximum Time [%1$tH:%1$tM]" {
    channel="gme:pun:account:pun:today-max-time"
}

Switch GME_TomorrowAvailable "Tomorrow Available" {
    channel="gme:pun:account:pun:tomorrow-available"
}

DateTime GME_LastUpdate "Last Update [%1$td/%1$tm/%1$tY %1$tH:%1$tM]" {
    channel="gme:pun:account:pun:last-update"
}

DateTime GME_PasswordLastChanged "GME Password Last Changed [%1$td/%1$tm/%1$tY]" {
    channel="gme:api:account:password-last-changed"
}

DateTime GME_PasswordExpiry "GME Password Expiry [%1$td/%1$tm/%1$tY]" {
    channel="gme:api:account:password-expiry"
}

Number GME_PasswordDaysRemaining "GME Password Days Remaining [%d]" {
    channel="gme:api:account:password-days-remaining"
}

String GME_PasswordStatus "GME Password Status [%s]" {
    channel="gme:api:account:password-status"
}
```

## Automation Examples

The market price channels can be used in openHAB rules to implement energy-aware automations.

Typical use cases include:

- scheduling flexible electrical loads during lower-price periods
- displaying current and future electricity prices on dashboards
- combining market prices with photovoltaic production forecasts
- selecting favourable battery charging periods
- delaying discretionary loads when the current price is high
- notifying the user before the GME API password expires

## Technical Details

The binding uses a Bridge-based architecture.

```text
GME API Account Bridge
          |
          +--- PUN Price Thing
```

The Bridge is responsible for:

- API authentication
- authentication token lifecycle
- credential change tracking
- common configuration

The PUN Thing is responsible for:

- market data retrieval
- PUN price processing
- zonal price processing
- daily statistics
- market-granularity time series generation
- market data caching

Market data are interpreted using the `Europe/Rome` time zone.

## Testing

The binding includes automated tests for:

- API authentication
- token invalidation and authentication retry
- GME market data parsing
- price cache behaviour
- PUN time series generation
- 15, 30 and 60-minute market granularity
- daylight-saving-time transitions
- password age calculation
- credential fingerprint tracking
- credential change detection
- initial password date handling

The binding tests can be executed with:

```shell
./mvnw -pl :org.openhab.binding.gme -am test
```
