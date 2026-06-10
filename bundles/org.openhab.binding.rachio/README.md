# Rachio Sprinkler Binding

This binding integrates Rachio sprinkler controllers and Smart Hose Timer resources through the Rachio Cloud APIs.
It requires a Rachio account and API key.

The binding supports monitoring and day-to-day control, including zone runs, rain delay, schedule commands, Smart Hose Timer controls, and webhook updates.
Use the Rachio app for device setup, account administration, and detailed schedule or program editing.

## Supported Things

The Rachio Cloud Connector is a Bridge Thing.

Controller, zone, schedule, flex schedule, Smart Hose Timer base station, valve, and valve program Things are discovered under that bridge when the corresponding Rachio resources exist.

| Thing           | Description                                                                                                                   |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------|
| `cloud`         | Rachio Cloud Connector for one Rachio account. Multiple accounts can be configured by adding multiple Cloud Connector Things. |
| `device`        | Rachio irrigation controller.                                                                                                 |
| `zone`          | Irrigation zone for a controller.                                                                                             |
| `schedule`      | Fixed schedule rule.                                                                                                          |
| `flex-schedule` | Flex schedule rule with status and ScheduleRuleService command channels.                                                      |
| `base-station`  | Smart Hose Timer Wi-Fi hub.                                                                                                   |
| `valve`         | Smart Hose Timer valve with manual start/stop and default runtime control.                                                    |
| `valve-program` | Separately discoverable Smart Hose Timer schedule/program resource associated with a valve.                                   |

## Migration from older Rachio binding versions

### What changed

- Account-level configuration now belongs on the Rachio Cloud Connector Thing (`rachio:cloud`).
- Configure API, polling, runtime, webhook, and Smart Hose Timer options on the Cloud Connector Thing; no add-on-wide fallback configuration is used.
- Thing type, public channel, and channel-type IDs use the openHAB lower-case-hyphen naming convention.
- Several physical numeric channels now use typed Quantity Item types such as `Number:Time`, `Number:Length`, `Number:Area`, `Number:Temperature`, `Number:Speed`, and `Number:Dimensionless`.
- Controller and zone Things should use real Rachio API UUIDs (`deviceId`, `zoneId`), not the controller MAC address.
- Webhook callback credentials should preferably be configured through `callbackUsername` and `callbackPassword`, not embedded in `callbackUrl`.

### Before upgrading

- Back up existing `.things`, `.items`, and relevant UI-managed Things and Items if possible.
- Note the current Cloud Connector configuration.
- If using file-based Things, check whether controller and zone Things use explicit `deviceId` and `zoneId`.
- If using webhooks, note the existing callback URL and credentials.

### Thing migration

This initial add-on contribution does not ship thing-type update aliases.
Managed Things created with earlier test builds may need to be recreated through discovery or updated to use current Thing type IDs such as `flex-schedule`, `base-station`, and `valve-program`.

An Item link depends on the full `ChannelUID`, which includes both the Thing UID and channel ID.
Changing either the Thing UID or channel ID breaks the existing Item link and requires relinking it.
Channel IDs were aligned with the openHAB naming convention before this initial contribution was merged.
Items linked to channels from earlier development builds therefore need to be relinked.

Flex Schedule uses the Thing type `rachio:flex-schedule`.
Delete and rediscover any `rachio:flexschedule` Things created by previous development builds.
Discovery is recommended because it fills the Rachio API identifiers automatically.

### Item migration for Quantity channels

Existing Items are not automatically converted from plain `Number` to `Number:*`; update manually created or managed Items where unit-aware behavior is required.
Plain runtime and delay commands remain seconds, and plain `moisture-level` commands remain millimeters.
`moisture-percent` and `forecast-precipitation-probability` use Rachio's 0..1 fraction semantics.

| Thing type      | Channels                                                                                              | New Item type          |
|-----------------|-------------------------------------------------------------------------------------------------------|------------------------|
| `device`        | `pause-time`, `runtime`, `rain-delay`, `current-schedule-duration`                                    | `Number:Time`          |
| `device`        | `forecast-today-high`, `forecast-today-low`                                                           | `Number:Temperature`   |
| `device`        | `forecast-precipitation`                                                                              | `Number:Length`        |
| `device`        | `forecast-precipitation-probability`                                                                  | `Number:Dimensionless` |
| `device`        | `forecast-wind`                                                                                       | `Number:Speed`         |
| `zone`          | `runtime`, `run-total`, `fixed-runtime`, `max-runtime`, `runtime-no-multiplier`                        | `Number:Time`          |
| `zone`          | `available-water`, `depth-of-water`, `saturated-depth-of-water`, `root-zone-depth`, `moisture-level`  | `Number:Length`        |
| `zone`          | `yard-area-square-feet`                                                                               | `Number:Area`          |
| `zone`          | `management-allowed-depletion`, `efficiency`, `moisture-percent`                                      | `Number:Dimensionless` |
| `schedule`      | `seasonal-adjustment`                                                                                 | `Number:Dimensionless` |
| `flex-schedule` | `seasonal-adjustment`                                                                                 | `Number:Dimensionless` |
| `valve`         | `runtime`, `default-runtime`, `next-planned-run-duration`, `last-completed-run-duration`              | `Number:Time`          |
| `valve`         | `battery-level`                                                                                       | `Number:Dimensionless` |
| `valve-program` | `duration`, `interval-days`                                                                           | `Number:Time`          |
| `valve-program` | `seasonal-adjustment`                                                                                 | `Number:Dimensionless` |

Valve Program `interval-days` is represented as `Number:Time` with day semantics.

```text
Number:Time Rachio_Zone1_Runtime "Zone 1 runtime [%d s]" { channel="rachio:zone:cloud:zone1:runtime" }
Number:Length Rachio_Zone1_MoistureLevel "Zone 1 moisture [%.1f mm]" { channel="rachio:zone:cloud:zone1:moisture-level" }
Number:Dimensionless Rachio_ForecastPrecipProbability "Rain probability [%.0f %%]" { channel="rachio:device:cloud:controller1:forecast-precipitation-probability" }
```

### Controller and zone identity migration

`deviceId` and `zoneId` must be the corresponding Rachio API UUIDs; `deviceId` is not the controller MAC address.
Discovered Things may retain MAC-derived openHAB Thing UIDs for compatibility, but API calls use the configured Rachio UUIDs.
Inbox discovery is the simplest way to obtain the correct identifiers.

### Webhook and callback migration

The recommended callback URL for myopenHAB.org is:

```text
https://home.myopenhab.org/rachio/webhook
```

```text
callbackUrl="https://home.myopenhab.org/rachio/webhook"
callbackUsername="user@example.com"
callbackPassword="raw-password-with-special-characters"
```

Enter raw credentials; the binding percent-encodes them before webhook registration.
Legacy callback URLs containing encoded credentials remain supported, but separate `callbackUsername` and `callbackPassword` values take precedence.
Keep `clearAllCallbacks=false` during normal operation.
Set it to `true` only to remove stale callbacks, then return it to `false` after registration succeeds.

### Post-upgrade validation checklist

- Cloud Connector Thing is ONLINE.
- Controller Thing is ONLINE.
- Existing zone Things are ONLINE.
- Quantity channels publish expected values.
- Sending a plain numeric runtime command still starts watering for the expected number of seconds.
- `run-zones` plus controller `run` works for selected zones.
- `stop` stops watering.
- Webhook registration succeeds in logs, or is briefly deferred and retried automatically if the local API budget guard is active.
- Recent event or webhook channels update after a watering event.
- No duplicate webhooks are created after restart.

## Configuration

For migration details, see [Migration from older Rachio binding versions](#migration-from-older-rachio-binding-versions).

### UI setup

1. Go to Inbox and press the + button.
1. Click Add Manually at the end of the list.
1. Select the Rachio Binding.
1. Select the Rachio Cloud Connector Thing.
1. Enter at least the API key.
1. Save the Thing configuration.

After the bridge connects successfully, supported Things are discovered automatically and appear in the Inbox.

Use Scan later if you want to refresh discovery results manually.

### `.things` setup

Create `conf/things/rachio.things` and configure the Cloud Connector:

```text
Bridge rachio:cloud:1 [
    apikey="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx",
    pollingInterval=600,
    defaultRuntime=120,
    eventHistoryLookbackHours=24,
    forecastUnits="METRIC",
    hoseSummaryLookbackDays=2,
    hoseSummaryLookaheadDays=7,
    callbackUrl="https://home.myopenhab.org/rachio/webhook",
    callbackUsername="user@example.com",
    callbackPassword="raw-password-with-special-characters",
    clearAllCallbacks=false
]
```

The bridge Thing does not have channels.

### Cloud Connector parameters

| Parameter                   | Description                                                                                                                                                                              |
|-----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `apikey`                    | API token required to access the Rachio Cloud account. Create it in the Rachio Web App account settings.                                                                                 |
| `pollingInterval`           | Delay between status polls, in seconds. A value around 10 minutes is usually enough when webhooks are configured; use an interval greater than 90 seconds to avoid unnecessary API load. |
| `defaultRuntime`            | Runtime in seconds used when a zone or valve run command has no explicit runtime.                                                                                                        |
| `eventHistoryLookbackHours` | Hours of recent controller event history to retrieve. Set to 0 to disable event history polling.                                                                                         |
| `forecastUnits`             | Units for the Rachio forecast endpoint: `METRIC` or `US`.                                                                                                                                |
| `hoseSummaryLookbackDays`   | Days of recent Smart Hose Timer Summary day-view data to retrieve for valve and program run state. Default is 2; set to 0 to skip historical runs.                                       |
| `hoseSummaryLookaheadDays`  | Days of upcoming Smart Hose Timer Summary day-view data to retrieve for planned runs and skip controls. Default is 7.                                                                    |
| `callbackUrl`               | Public HTTPS URL that forwards to `/rachio/webhook`. In the recommended Basic Auth setup, do not include credentials in this URL.                                                        |
| `callbackUsername`          | Optional HTTP Basic Auth username for the webhook endpoint. Enter the raw value; the binding percent-encodes it before registering the webhook with Rachio.                              |
| `callbackPassword`          | Optional HTTP Basic Auth password for the webhook endpoint. Enter the raw value; the binding percent-encodes it before registering the webhook with Rachio.                              |
| `clearAllCallbacks`         | Cleanup switch for stale Rachio callback registrations. Leave `false` for normal operation.                                                                                              |

Cloud Connector Thing configuration is the only user configuration source.
Built-in defaults are used for unset optional values.
Configuration precedence is: `Cloud Connector Thing configuration > built-in defaults`.
No add-on-wide fallback configuration is read.

### openHAB Cloud / myopenHAB.org setup

For users of [openHAB Cloud](https://www.openhab.org/docs/configuration/openhab-cloud.html) or [myopenHAB.org](https://www.myopenhab.org/), configure the public callback URL and Basic Auth credentials separately:

```text
callbackUrl="https://home.myopenhab.org/rachio/webhook"
callbackUsername="user@example.com"
callbackPassword="raw-password-with-special-characters"
```

Webhook forwarding through openHAB Cloud / myopenHAB.org does not require exposing any Items in the Cloud Connector configuration.

Check openHAB logs for successful webhook registration after saving the bridge.
If the local Rachio API budget guard is active, webhook registration may be deferred briefly; the binding retries automatically.

## Adding Controllers and Zones

Recommended: use Inbox discovery.

1. Add and configure the Rachio Cloud Connector.
1. Wait for the bridge to initialize successfully.
1. Accept the discovered controller, zone, schedule, flex schedule, base station, valve, and valve program Things you want to use.

Manual creation is also supported with real Rachio UUIDs.

Use `deviceId` for the controller API UUID and `zoneId` for the zone API UUID.

For older binding upgrade details, see [Controller and zone identity migration](#controller-and-zone-identity-migration).

## Rachio API Coverage

This binding maps supported Rachio API resources to openHAB Things, channels, and commands.
Use the Rachio app for full device, account, and schedule editing.

### Supported

- Rachio account / Cloud Connector
- Irrigation controller discovery
- Controller status and basic control
- Zone discovery and zone start/stop
- Multiple-zone start
- Fixed schedule discovery and basic control
- Flex schedule discovery, status, and ScheduleRuleService commands
- Current schedule
- Forecast
- Recent controller events
- Rain delay
- Webhook registration and routing
- Webhook signature validation with the `x-signature` header
- Webhook duplicate event protection using Rachio `eventId`
- Smart Hose Timer base stations
- Smart Hose Timer valves
- Smart Hose Timer valve programs
- Smart Hose Timer planned run and program skip controls where represented by current channels
- QuantityType channel support for physical values

Schedule-rule, Smart Hose Timer Program, and webhook support is limited to the Things and channels below.
The binding queries Rachio's `listWebhookEventTypes` catalog and subscribes to supported events for implemented Thing types.

## Device Thing

| Channel                            | Description                                                                                                                |
|------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `name`                             | Device name.                                                                                                               |
| `active`                           | ON when the controller is active.                                                                                          |
| `online`                           | ON when the controller is connected to the Rachio cloud.                                                                   |
| `paused`                           | ON pauses the currently active zone run for `pause-time` seconds; OFF resumes the active zone run.                         |
| `pause-time`                       | `Number:Time` duration to pause the active zone run. Plain numeric commands are seconds. Valid range is 0 to 3600 seconds. |
| `sleep-mode`                       | ON when Rachio device sleep mode is active.                                                                                |
| `stop`                             | ON stops watering for all zones.                                                                                           |
| `run`                              | ON starts watering selected zones from `run-zones`.                                                                        |
| `run-zones`                        | Comma-separated zone numbers to run, for example `1,3`; an empty value means all zones.                                    |
| `runtime`                          | Controller-level `Number:Time` run duration for the multi-zone `run` command. Plain numeric commands are seconds.          |
| `rain-delay`                       | `Number:Time` rain delay duration. Plain numeric commands are seconds; 0 means not in rain delay mode.                     |
| `rain-sensor-tripped`              | ON when the rain sensor has tripped.                                                                                       |
| `active-zone-number`               | Zone number currently watering, populated from zone run webhook events.                                                    |
| `active-zone-name`                 | Zone name currently watering, populated from zone run webhook events.                                                      |
| `active-zone-id`                   | Rachio zone UUID currently watering, populated from zone run webhook events.                                               |
| `last-update`                      | Timestamp of last status update.                                                                                           |
| `last-event`                       | Last event received from the cloud.                                                                                        |
| `last-event-time`                  | Timestamp of the last received event.                                                                                      |
| `schedule-name`                    | Current or last executed schedule name.                                                                                    |
| `schedule-info`                    | Description of the current or last executed schedule.                                                                      |
| `schedule-start`                   | Schedule start time.                                                                                                       |
| `schedule-end`                     | Schedule end time.                                                                                                         |
| `current-schedule-running`         | ON when Rachio reports a currently running schedule from `/device/{id}/current_schedule`.                                  |
| `current-schedule-id`              | Rachio schedule ID for the currently running schedule.                                                                     |
| `current-schedule-name`            | Name of the currently running schedule.                                                                                    |
| `current-schedule-type`            | Type of the currently running schedule.                                                                                    |
| `current-schedule-start-time`      | Start time of the currently running schedule.                                                                              |
| `current-schedule-end-time`        | End time of the currently running schedule.                                                                                |
| `current-schedule-duration`        | `Number:Time` duration of the currently running schedule, published in seconds.                                            |
| `last-api-event-type`              | Type of the latest event retrieved from recent device event history.                                                       |
| `last-api-event-time`              | Time of the latest event retrieved from recent device event history.                                                       |
| `last-api-event-summary`           | Summary of the latest event retrieved from recent device event history.                                                    |
| `forecast-summary`                 | Forecast summary from Rachio.                                                                                              |
| `forecast-today-high`              | Today's `Number:Temperature` high temperature in the configured forecast units.                                            |
| `forecast-today-low`               | Today's `Number:Temperature` low temperature in the configured forecast units.                                             |
| `forecast-precipitation`           | Today's `Number:Length` forecast precipitation amount in the configured forecast units.                                    |
| `forecast-precipitation-probability` | Today's `Number:Dimensionless` precipitation probability as a 0..1 fraction.                                             |
| `forecast-wind`                    | Today's `Number:Speed` wind speed in the configured forecast units.                                                        |
| `forecast-updated`                 | Timestamp of the forecast data when provided by Rachio.                                                                    |
| `last-skip-type`                   | Most recent weather intelligence skip event type.                                                                          |
| `last-skip-schedule-id`            | Schedule ID associated with the most recent weather intelligence skip event.                                               |
| `last-skip-start-time`             | Start time associated with the most recent weather intelligence skip event.                                                |
| `last-skip-reason`                 | Summary or reason from the most recent weather intelligence skip event.                                                    |

When starting zones from the controller Thing with `run-zones` and `run`, controller `runtime` controls the duration for every selected zone.

If controller `runtime` is greater than 0, that value is used.

If controller `runtime` is 0, the bridge `defaultRuntime` is used.

Zone-specific `runtime` values only apply when starting an individual zone from that zone Thing.

Forecast channels follow the Cloud Connector `forecastUnits` setting.

With `METRIC`, forecast temperatures, precipitation, and wind are published as Celsius, millimeters, and meters per second.

With `US`, they are published as Fahrenheit, inches, and miles per hour.

## Zone Thing

| Channel                      | Description                                                                                                                                                                                               |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `number`                     | Zone number as assigned by the controller.                                                                                                                                                                |
| `name`                       | Zone name.                                                                                                                                                                                                |
| `enabled`                    | ON when the zone is enabled. Sending ON/OFF enables or disables the zone.                                                                                                                                 |
| `run`                        | ON starts watering. If `runtime` is 0, `defaultRuntime` is used. OFF stops watering.                                                                                                                      |
| `runtime`                    | `Number:Time` duration to run the zone when `run` receives ON. Plain numeric commands are seconds.                                                                                                        |
| `run-total`                  | Total `Number:Time` duration the zone was watering, as returned by the cloud service.                                                                                                                     |
| `available-water`            | `Number:Length` available water value returned by Rachio for the zone, published in inches.                                                                                                               |
| `image-url`                  | URL to the zone picture.                                                                                                                                                                                  |
| `image`                      | Native openHAB Image channel for the zone picture.                                                                                                                                                        |
| `depth-of-water`             | `Number:Length` depth of water value returned by Rachio, published in inches.                                                                                                                             |
| `saturated-depth-of-water`   | `Number:Length` saturated depth of water value returned by Rachio, published in inches.                                                                                                                   |
| `management-allowed-depletion` | `Number:Dimensionless` depletion fraction returned by Rachio.                                                                                                                                           |
| `root-zone-depth`            | `Number:Length` root zone depth value returned by Rachio, published in inches.                                                                                                                            |
| `efficiency`                 | `Number:Dimensionless` efficiency fraction returned by Rachio.                                                                                                                                            |
| `yard-area-square-feet`      | `Number:Area` yard area in square feet as returned by Rachio.                                                                                                                                             |
| `last-watered-date`          | Timestamp when Rachio reports the zone was last watered.                                                                                                                                                  |
| `fixed-runtime`              | `Number:Time` fixed runtime value returned by Rachio, published in seconds.                                                                                                                               |
| `max-runtime`                | `Number:Time` maximum runtime value returned by Rachio, published in seconds.                                                                                                                             |
| `runtime-no-multiplier`      | `Number:Time` runtime without multiplier value returned by Rachio, published in seconds.                                                                                                                  |
| `schedule-data-modified`     | ON when Rachio reports modified schedule data for the zone.                                                                                                                                               |
| `moisture-level`             | `Number:Length` command-only soil moisture adjustment input for `zone/setMoistureLevel`. Plain numeric commands are millimeters.                                                                          |
| `moisture-percent`           | `Number:Dimensionless` command-only soil moisture adjustment input for `zone/setMoisturePercent`. Plain numeric commands are a 0..1 fraction; quantity percentages such as `50 %` are converted to `0.5`. |
| `last-update`                | Timestamp of last status update.                                                                                                                                                                          |
| `last-event`                 | Last event received from the cloud.                                                                                                                                                                       |
| `last-event-time`            | Timestamp of the last received event.                                                                                                                                                                     |

The `image-url` channel remains available for URL-based integrations.

The `image` channel downloads the same zone picture as native openHAB image data and can be linked to an `Image` Item.

If an image cannot be downloaded, the zone remains online and the URL channel is still updated.

The Rachio public Zone object does not return readable `moistureLevel` or `moisturePercent` fields.
Those channels are retained as command-only soil moisture adjustment inputs for external measurements or manual correction.
After restart or before the first successful command, their Item state may be `UNDEF`.
Readable water-model values are available through `available-water`, `depth-of-water`, and `saturated-depth-of-water`.

## Smart Hose Timer Things

Smart Hose Timer support covers base stations, valves, and valve programs.

Discovery is recommended after the Cloud Connector is online.

Manual creation is supported when the real Rachio IDs are configured:

```text
Thing base-station hosehub "Hose Timer Hub" [
    baseStationId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
]

Thing valve garden "Garden Hose Valve" [
    valveId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    baseStationId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
]

Thing valve-program morninghose "Morning Hose Program" [
    programId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    valveId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    baseStationId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
]
```

### Base Station channels

| Channel      | Description                                                     |
|--------------|-----------------------------------------------------------------|
| `name`       | Base station name when reported by Rachio.                      |
| `online`     | ON when Rachio reports the base station is online or connected. |
| `last-update` | Timestamp of last base station state update.                   |

### Valve channels

| Channel                    | Description                                                                                                                                                                    |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `name`                     | Valve name when reported by Rachio.                                                                                                                                            |
| `online`                   | ON when Rachio reports the valve is online or connected.                                                                                                                       |
| `run`                      | Send ON to start watering, OFF to stop watering.                                                                                                                               |
| `runtime`                  | `Number:Time` runtime for the next manual valve start. Plain numeric commands are seconds. If 0, the valve default runtime is used, then the bridge `defaultRuntime` fallback. |
| `default-runtime`          | `Number:Time` valve default manual runtime. Plain numeric commands are seconds. Sending a value updates Rachio using `setDefaultRuntime`.                                      |
| `state-matches`            | ON when `ValveState.matches` indicates the physical valve has synchronized with the desired cloud-side state.                                                                  |
| `flow-detected`            | ON when a valve webhook event or valve state reports flow.                                                                                                                     |
| `battery-level`            | `Number:Dimensionless` battery level when reported by Rachio, published as percent.                                                                                            |
| `serial-number`            | Valve serial number when reported by Rachio.                                                                                                                                   |
| `last-run-type`            | Run type from the most recent valve webhook event.                                                                                                                             |
| `last-end-reason`          | End reason from the most recent valve stop webhook event.                                                                                                                      |
| `next-planned-runtime`     | Start time of the next planned valve run from Summary day views.                                                                                                               |
| `next-planned-run-duration` | `Number:Time` duration of the next planned valve run, published in seconds.                                                                                                   |
| `next-planned-run-program-id` | Program ID associated with the next planned valve run.                                                                                                                       |
| `next-planned-run-skipped` | ON when the next planned valve run is currently skipped.                                                                                                                       |
| `last-completed-runtime`   | Start time of the most recent completed valve run from Summary day views.                                                                                                      |
| `last-completed-run-duration` | `Number:Time` duration of the most recent completed valve run, published in seconds.                                                                                         |
| `last-run-status`          | Status of the most recent completed valve run from Summary day views.                                                                                                          |
| `skip-next-planned-run`    | Send ON to skip the next upcoming valve planned run when Summary identifiers are available.                                                                                    |
| `cancel-next-planned-run-skip` | Send ON to cancel the next skipped upcoming valve run when Summary identifiers are available.                                                                              |
| `last-update`              | Timestamp of last valve state update.                                                                                                                                          |
| `last-event`               | Most recent valve webhook event.                                                                                                                                               |
| `last-event-time`          | Timestamp of the most recent valve webhook event.                                                                                                                              |

The Smart Hose Timer API is asynchronous, so `state-matches` may remain OFF after changing `default-runtime` until the valve applies the update.
Summary day-view polling uses the bridge `hoseSummaryLookbackDays` and `hoseSummaryLookaheadDays` settings.

### Valve Program channels

Rachio exposes valve programs as separately discoverable schedule/program resources with their own metadata and commands.
The binding therefore models each program as a related `valve-program` Thing, linked by `valveId`, rather than as an embedded channel group on the `valve` Thing.

| Channel                                   | Description                                                                                              |
|-------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `name`                                    | Program name.                                                                                            |
| `enabled`                                 | ON when Rachio reports that the program is enabled.                                                      |
| `program-type`                            | Program type returned by Rachio.                                                                         |
| `valve-id`                                | Associated Smart Hose Timer Valve UUID.                                                                  |
| `start-time`                              | Program start time value returned by Rachio.                                                             |
| `next-runtime`                            | Next planned runtime when available from Rachio or Summary day views.                                    |
| `last-runtime`                            | Last runtime when available from Rachio or Summary day views.                                            |
| `duration`                                | `Number:Time` Program duration, published in seconds.                                                    |
| `days-of-week`                            | Days-of-week structure returned by Rachio.                                                               |
| `interval-days`                           | `Number:Time` Program interval, published in days when provided by Rachio.                               |
| `seasonal-adjustment`                     | `Number:Dimensionless` seasonal adjustment fraction when provided by Rachio.                             |
| `updated-at`                              | Last update time when provided by Rachio.                                                                |
| `next-program-run-skipped`                | ON when the next upcoming run for this program is currently skipped.                                     |
| `skip-next-planned-run`                   | Send ON to skip the next upcoming planned run for this program when Summary identifiers are available.   |
| `cancel-next-planned-run-skip`            | Send ON to cancel the next skipped upcoming run for this program when Summary identifiers are available. |
| `last-rain-skip-planned-run-start-time`   | Planned run start time from the most recent Program rain-skip-created webhook event.                     |
| `last-rain-skip-canceled-planned-run-start-time` | Planned run start time from the most recent Program rain-skip-canceled webhook event.            |
| `last-update`                             | Timestamp of last Program state update.                                                                  |
| `last-event`                              | Most recent Program webhook event.                                                                       |
| `last-event-time`                         | Timestamp of the most recent Program webhook event.                                                      |

Skip commands use Summary day-view identifiers.
The binding uses planned-run skip endpoints when a run ID and date are available, and Program skip endpoints when only a Program ID and timestamp are available.

## Schedule Things

Fixed schedule rules are represented by `schedule` Things.

Flex schedules use the same lower-case-hyphen channel IDs and expose the same command operations as fixed schedules.

| Fixed schedule channel | Flex schedule channel  | Description                                                                                              |
|------------------------|------------------------|----------------------------------------------------------------------------------------------------------|
| `name`                 | `name`                 | Schedule rule name.                                                                                      |
| `enabled`              | `enabled`              | ON if the schedule rule is enabled.                                                                      |
| `type`                 | `type`                 | Schedule rule type.                                                                                      |
| `start-time`           | `start-time`           | Schedule start time from Rachio `startDate`, with `startTime` used as a fallback when present.           |
| `last-run`             | `last-run`             | Last run time when Rachio provides a usable best-effort field.                                           |
| `next-run`             | `next-run`             | Next run time when Rachio provides a usable best-effort field.                                           |
| `zones`                | `zones`                | Comma-separated Rachio zone IDs associated with the schedule.                                            |
| `seasonal-adjustment`  | `seasonal-adjustment`  | `Number:Dimensionless` seasonal adjustment value. Sending a plain number updates the corresponding rule. |
| `start`                | `start`                | Send ON to start the schedule rule.                                                                      |
| `skip`                 | `skip`                 | Send ON to skip the schedule rule.                                                                       |
| `skip-forward-zone-run` | `skip-forward-zone-run` | Send ON to skip the currently running zone in this schedule context.                                   |
| `last-update`          | `last-update`          | Timestamp of last schedule state update.                                                                 |

Fixed schedule metadata is loaded through ScheduleRuleService with `GET /public/schedulerule/:id`.
Flex schedule metadata is loaded through FlexScheduleRuleService with `GET /public/flexschedulerule/:id`.
Commands for both fixed and flex schedules use the ScheduleRuleService `start`, `skip`, `seasonal_adjustment`, and `skip_forward_zone_run` endpoints with the corresponding rule ID.

Manual schedule creation requires `scheduleRuleId`; manual flex schedule creation requires `flexScheduleRuleId`.
Discovery creates both Thing types when the controller payload contains the corresponding rule IDs.

Schedule and flex schedule DateTime channels support epoch milliseconds, epoch seconds, numeric strings, and ISO-8601 strings when those fields are present.
`last-run` and `next-run` remain `UNDEF` when the Rachio API response does not include a usable value; the binding does not fabricate dates.

## Webhook Events

The binding registers for supported schedule, zone-run, weather-skip, valve-run, and valve-program rain-skip events.
Registration is retried automatically when the local API budget guard defers it.

Inbound events are verified with the `x-signature` header, and duplicate `eventId` deliveries are acknowledged without being routed twice.
Events update matching controller, schedule, zone, valve, or valve-program Things when sufficient identity information is available.

## Full Example

### Thing Definition

```text
Bridge rachio:cloud:1 @ "Sprinkler" [
    apikey="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    pollingInterval=600,
    defaultRuntime=120,
    callbackUrl="https://home.myopenhab.org/rachio/webhook",
    callbackUsername="user@example.com",
    callbackPassword="raw-password-with-special-characters",
    clearAllCallbacks=false
] {
    Thing device controller "Rachio Controller" @ "Sprinkler" [
        deviceId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing zone controller-zone1 "Front Lawn" @ "Sprinkler" [
        zoneId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing schedule morning "Morning Schedule" @ "Sprinkler" [
        scheduleRuleId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing flex-schedule flex "Flex Schedule" @ "Sprinkler" [
        flexScheduleRuleId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing base-station hosehub "Hose Timer Hub" @ "Garden" [
        baseStationId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing valve gardenhose "Garden Hose Valve" @ "Garden" [
        valveId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        baseStationId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing valve-program morninghose "Morning Hose Program" @ "Garden" [
        programId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        valveId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        baseStationId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]
}
```

### Items Definition

```text
String Rachio_Controller_Name "Controller Name" { channel="rachio:device:1:controller:name" }
Switch Rachio_Controller_Online "Controller Online" { channel="rachio:device:1:controller:online" }
Switch Rachio_Controller_Run "Run Selected Zones" { channel="rachio:device:1:controller:run" }
String Rachio_Controller_RunZones "Run Zone List" { channel="rachio:device:1:controller:run-zones" }
Number:Time Rachio_Controller_Runtime "Controller Runtime [%d s]" { channel="rachio:device:1:controller:runtime" }
Number:Time Rachio_Controller_RainDelay "Rain Delay [%d s]" { channel="rachio:device:1:controller:rain-delay" }

Number:Temperature Rachio_Forecast_High "Forecast High" { channel="rachio:device:1:controller:forecast-today-high" }
Number:Length Rachio_Forecast_Precipitation "Forecast Precipitation" { channel="rachio:device:1:controller:forecast-precipitation" }
Number:Dimensionless Rachio_Forecast_PrecipitationProbability "Rain Probability [%.0f %%]" { channel="rachio:device:1:controller:forecast-precipitation-probability" }

String Rachio_Zone1_Name "Zone Name" { channel="rachio:zone:1:controller-zone1:name" }
Switch Rachio_Zone1_Run "Run Zone" { channel="rachio:zone:1:controller-zone1:run" }
Number:Time Rachio_Zone1_Runtime "Zone Runtime [%d s]" { channel="rachio:zone:1:controller-zone1:runtime" }
Number:Length Rachio_Zone1_MoistureLevel "Moisture Level [%.1f mm]" { channel="rachio:zone:1:controller-zone1:moisture-level" }
Number:Dimensionless Rachio_Zone1_MoisturePercent "Moisture [%.0f %%]" { channel="rachio:zone:1:controller-zone1:moisture-percent" }
Image Rachio_Zone1_Image "Zone Image" { channel="rachio:zone:1:controller-zone1:image" }

Switch HoseValve_Run "Run Hose Valve" { channel="rachio:valve:1:gardenhose:run" }
Number:Time HoseValve_Runtime "Hose Runtime [%d s]" { channel="rachio:valve:1:gardenhose:runtime" }
Number:Time HoseValve_DefaultRuntime "Default Hose Runtime [%d s]" { channel="rachio:valve:1:gardenhose:default-runtime" }
Number:Dimensionless HoseValve_BatteryLevel "Valve Battery [%.0f %%]" { channel="rachio:valve:1:gardenhose:battery-level" }
Number:Time HoseValve_NextRunDuration "Next Hose Run Duration [%d s]" { channel="rachio:valve:1:gardenhose:next-planned-run-duration" }
Switch HoseValve_SkipNext "Skip Next Hose Run" { channel="rachio:valve:1:gardenhose:skip-next-planned-run" }

String HoseProgram_Name "Program Name" { channel="rachio:valve-program:1:morninghose:name" }
Number:Time HoseProgram_Duration "Program Duration [%d s]" { channel="rachio:valve-program:1:morninghose:duration" }
Number:Time HoseProgram_Interval "Program Interval [%.0f d]" { channel="rachio:valve-program:1:morninghose:interval-days" }
Number:Dimensionless HoseProgram_SeasonalAdjustment "Seasonal Adjustment [%.0f %%]" { channel="rachio:valve-program:1:morninghose:seasonal-adjustment" }
```

### Rule Example

```text
rule "Start Rachio zone for 30 minutes"
when
    Item Some_Button changed
then
    Rachio_Zone1_RunTime.sendCommand(1800)
    Rachio_Zone1_Run.sendCommand(ON)
end
