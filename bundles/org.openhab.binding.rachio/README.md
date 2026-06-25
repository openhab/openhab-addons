# Rachio Binding

This binding integrates Rachio irrigation controllers and Smart Hose Timer resources through the Rachio Cloud APIs.
Use the Rachio app for account setup, device pairing, and detailed schedule or program editing.

## Supported Things

The `cloud` Thing is a bridge for one Rachio account.
After it is online, the binding discovers the Rachio resources that belong to that account.

| Thing Type      | Description                                                                                 |
|-----------------|---------------------------------------------------------------------------------------------|
| `cloud`         | Rachio Cloud Connector bridge.                                                              |
| `device`        | Rachio irrigation controller.                                                               |
| `zone`          | Irrigation zone for a controller.                                                           |
| `schedule`      | Fixed irrigation schedule rule.                                                             |
| `flex-schedule` | Flex irrigation schedule rule.                                                              |
| `base-station`  | Smart Hose Timer Wi-Fi hub.                                                                 |
| `valve`         | Smart Hose Timer valve.                                                                     |
| `valve-program` | Smart Hose Timer program with its own metadata and commands.                             |

Rachio exposes valve programs as separate resources, so the binding models each one as a `valve-program` Thing.

## Discovery

Discovery starts automatically when the `cloud` bridge is online.
You can also start a scan from the Inbox.
Discovery can create controller, zone, schedule, flex schedule, Smart Hose Timer base station, valve, and valve-program Things when those resources exist in the Rachio account.

Manual Things require the corresponding Rachio UUIDs, such as `deviceId`, `zoneId`, `scheduleRuleId`, `baseStationId`, `valveId`, and `programId`.
Discovery is recommended because it fills those IDs automatically.

## Configuration

### Cloud Connector

Create a Rachio API key in the Rachio Web App account settings, then add a Rachio Cloud Connector Thing.
Discovery starts automatically after the bridge connects.

| Parameter                   | Description                                                                                                    |
|-----------------------------|----------------------------------------------------------------------------------------------------------------|
| `apikey`                    | Rachio API key.                                                                                                |
| `pollingInterval`           | Poll interval in seconds. Use a moderate value such as 600 with irrigation controller webhooks.                |
| `defaultRuntime`            | Runtime in seconds used when a zone or valve command does not provide one.                                     |
| `eventHistoryLookbackHours` | Hours of recent irrigation controller event history to read. Set to `0` to disable event-history polling.      |
| `forecastUnits`             | Forecast units, `METRIC` or `US`.                                                                              |
| `hoseSummaryLookbackDays`   | Days of recent Smart Hose Timer summary data to read.                                                          |
| `hoseSummaryLookaheadDays`  | Days of upcoming Smart Hose Timer summary data to read.                                                        |
| `callbackUrl`               | Public HTTPS URL that forwards legacy Rachio NotificationService POST requests to `/rachio/webhook`.           |
| `callbackUsername`          | Optional legacy callback URL username. Leave empty unless the public endpoint supports URL userinfo callbacks. |
| `callbackPassword`          | Optional legacy callback URL password. Leave empty unless the public endpoint supports URL userinfo callbacks. |
| `clearAllCallbacks`         | Cleanup switch for stale Rachio callback registrations. Leave `false` for normal operation.                    |
| `autoConfigureWebhooks`     | Optional modern Rachio WebhookService registration for irrigation controllers. Defaults to `false`.            |
| `autoConfigureHoseTimerWebhooks` | Also register modern Smart Hose Timer valve and valve-program webhooks. Requires `autoConfigureWebhooks=true`. |
| `useCloudWebhook`           | Request the public webhook URL from openHAB Cloud Connector when modern registration is enabled.               |
| `publicWebhookUrl`          | Manually supplied public HTTPS URL for modern Rachio WebhookService callbacks.                                 |

Only Thing configuration is used.
There is no add-on-wide fallback configuration.

### File-Based Example

```text
Bridge rachio:cloud:home "Rachio Cloud" [
    apikey="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    pollingInterval=600,
    defaultRuntime=120,
    eventHistoryLookbackHours=24,
    forecastUnits="METRIC",
    hoseSummaryLookbackDays=2,
    hoseSummaryLookaheadDays=7,
    callbackUrl="https://example.org/rachio/webhook",
    clearAllCallbacks=false
] {
    Thing device controller "Rachio Controller" [
        deviceId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing zone front-lawn "Front Lawn" [
        zoneId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing schedule morning "Morning Schedule" [
        scheduleRuleId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing flex-schedule flex "Flex Schedule" [
        flexScheduleRuleId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing base-station hose-hub "Hose Timer Hub" [
        baseStationId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing valve garden-hose "Garden Hose Valve" [
        valveId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        baseStationId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]

    Thing valve-program morning-hose "Morning Hose Program" [
        programId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        valveId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        baseStationId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ]
}
```

## Webhooks

Webhook support is optional, and polling remains active for fallback and state reconciliation.
Modern openHAB WebhookService and legacy Rachio NotificationService processing are mutually exclusive for irrigation events.
**Note:** When changing between legacy and modern webhook modes, restart openHAB (or the binding) to ensure a clean webhook registration lifecycle and activate the new mode.

Modern WebhookService registration is off by default.
Set `autoConfigureWebhooks=true` and either:

- set `useCloudWebhook=true` to use a public URL from the openHAB Cloud Connector, or
- set `publicWebhookUrl` to a public HTTPS URL that forwards to `/rachio/webhook`.

The manual `publicWebhookUrl` must use HTTPS and must not contain URL userinfo credentials.
Modern events require a valid `x-signature` over the received request body.
Multiple Rachio bridges safely share the same local `/rachio/webhook` servlet path.

When modern mode is active, stale legacy callbacks may still arrive; they are acknowledged and ignored.
Otherwise, configuring `callbackUrl` keeps legacy NotificationService support available.
Legacy callbacks do not carry `x-signature` and are accepted only when their `externalId` and controller identity match a configured bridge and controller.

Processed irrigation events include schedule and zone-run status, rain sensor and delay changes, weather skips, and related controller status events.
Events without dedicated channel updates trigger a state refresh.

Smart Hose Timer webhooks are also off by default.
Set `autoConfigureHoseTimerWebhooks=true` in addition to `autoConfigureWebhooks=true` to register modern webhooks for valves and valve programs.
These webhooks cover valve run start/end and valve-program rain-skip creation/cancellation.

Smart Lighting webhooks are not supported.

## Channels

All public Thing type IDs, channel IDs, and channel type IDs use lower-case-hyphen naming.
Most duration channels use `Number:Time`; plain numeric commands are interpreted as seconds unless noted otherwise.

### Controller Channels

| Channel Group | Channels |
|---------------|----------|
| State         | `name`, `active`, `online`, `paused`, `pause-time`, `sleep-mode`, `rain-delay`, `rain-sensor-tripped` |
| Commands      | `run`, `run-zones`, `runtime`, `stop` |
| Active Run    | `active-zone-number`, `active-zone-name`, `active-zone-id`, `current-schedule-id`, `current-schedule-name`, `current-schedule-type`, `current-schedule-start-time`, `current-schedule-end-time`, `current-schedule-duration`, `current-schedule-running` |
| Events        | `last-api-event-type`, `last-api-event-time`, `last-api-event-summary`, `last-skip-type`, `last-skip-schedule-id`, `last-skip-start-time`, `last-skip-reason`, `last-event`, `last-event-time`, `last-update` |
| Forecast      | `forecast-summary`, `forecast-today-high`, `forecast-today-low`, `forecast-precipitation`, `forecast-precipitation-probability`, `forecast-wind`, `forecast-updated` |
| Schedule Info | `schedule-name`, `schedule-info`, `schedule-start`, `schedule-end` |

`run-zones` is a comma-separated list of zone numbers for the next controller-level `run` command.
An empty value means all zones.

### Zone Channels

| Channel Group | Channels |
|---------------|----------|
| State         | `name`, `number`, `enabled`, `last-watered-date`, `last-update`, `last-event`, `last-event-time` |
| Commands      | `run`, `runtime`, `moisture-level`, `moisture-percent` |
| Water Model   | `available-water`, `depth-of-water`, `saturated-depth-of-water`, `management-allowed-depletion`, `root-zone-depth`, `efficiency`, `yard-area-square-feet` |
| Runtime Data  | `run-total`, `fixed-runtime`, `max-runtime`, `runtime-no-multiplier`, `schedule-data-modified` |
| Image         | `image-url`, `image` |

#### Zone Channel Units

Zone water-depth and area channels retain the units returned by the Rachio API model:

| Channel | Unit |
|---------|------|
| `available-water` | `in` |
| `depth-of-water` | `in` |
| `saturated-depth-of-water` | `in` |
| `root-zone-depth` | `in` |
| `yard-area-square-feet` | `ft²` |

These are API-backed water-model values.
The channel metadata supplies default unit hints, but file-based Items retain their own unit metadata and label patterns.

`moisture-level` and `moisture-percent` are command inputs for Rachio's `setMoistureLevel` and `setMoisturePercent` operations, not continuously readable sensors.
They may remain `UNDEF` until commanded and are not derived from `available-water`, `depth-of-water`, or `saturated-depth-of-water`.

### Schedule Channels

Fixed `schedule` and `flex-schedule` Things expose the same channel IDs:

| Channel | Description |
|---------|-------------|
| `name` | Schedule rule name. |
| `enabled` | Whether the rule is enabled. |
| `type` | Schedule rule type. |
| `start-time` | Schedule start time when provided by Rachio. |
| `last-run` | Last run time when provided by Rachio. |
| `next-run` | Next run time when provided by Rachio. |
| `zones` | Comma-separated Rachio zone IDs associated with the rule. |
| `seasonal-adjustment` | Seasonal adjustment value; sending a number updates it. |
| `start` | Send `ON` to start the rule. |
| `skip` | Send `ON` to skip the rule. |
| `skip-forward-zone-run` | Send `ON` to skip the currently running zone for the rule. |
| `last-update` | Timestamp of the last schedule state update. |

### Smart Hose Timer Channels

| Thing Type      | Channels |
|-----------------|----------|
| `base-station`  | `name`, `online`, `last-update` |
| `valve`         | `name`, `online`, `run`, `runtime`, `default-runtime`, `state-matches`, `flow-detected`, `battery-level`, `serial-number`, `last-run-type`, `last-end-reason`, `next-planned-runtime`, `next-planned-run-duration`, `next-planned-run-program-id`, `next-planned-run-skipped`, `last-completed-runtime`, `last-completed-run-duration`, `last-run-status`, `skip-next-planned-run`, `cancel-next-planned-run-skip`, `last-update`, `last-event`, `last-event-time` |
| `valve-program` | `name`, `enabled`, `program-type`, `valve-id`, `start-time`, `next-runtime`, `last-runtime`, `duration`, `days-of-week`, `interval-days`, `seasonal-adjustment`, `updated-at`, `next-program-run-skipped`, `skip-next-planned-run`, `cancel-next-planned-run-skip`, `last-rain-skip-planned-run-start-time`, `last-rain-skip-canceled-planned-run-start-time`, `last-update`, `last-event`, `last-event-time` |

Smart Hose Timer skip commands need run or program identifiers from Rachio summary data.
When Rachio has not supplied those identifiers, the command is ignored and logged at debug level.

## Item Examples

```java
Switch Rachio_Controller_Stop "Stop Watering" {
    channel="rachio:device:home:controller:stop"
}

String Rachio_Controller_RunZones "Run Zones" {
    channel="rachio:device:home:controller:run-zones"
}

Number:Time Rachio_Controller_Runtime "Runtime [%d s]" {
    channel="rachio:device:home:controller:runtime"
}

Switch Rachio_Zone_Run "Front Lawn" {
    channel="rachio:zone:home:front-lawn:run"
}

Number:Time Rachio_Zone_Runtime "Front Lawn Runtime [%d s]" {
    channel="rachio:zone:home:front-lawn:runtime"
}

Number:Length Rachio_Zone1_AvailableWater "Available water [%.2f in]" {
    unit="in",
    channel="rachio:zone:<bridgeId>:<controllerId>-1:available-water"
}

Number:Length Rachio_Zone1_DepthOfWater "Depth of water [%.2f in]" {
    unit="in",
    channel="rachio:zone:<bridgeId>:<controllerId>-1:depth-of-water"
}

Number:Area Rachio_Zone1_YardArea "Yard area [%.1f ft²]" {
    unit="ft²",
    channel="rachio:zone:<bridgeId>:<controllerId>-1:yard-area-square-feet"
}

Number:Length Rachio_Controller_ForecastPrecipitation "Forecast precipitation [%.1f mm]" {
    unit="mm",
    channel="rachio:device:<bridgeId>:<controllerId>:forecast-precipitation"
}

Switch Rachio_Schedule_Start "Start Morning Schedule" {
    channel="rachio:schedule:home:morning:start"
}

Switch Rachio_Valve_Run "Garden Hose" {
    channel="rachio:valve:home:garden-hose:run"
}
```

The forecast precipitation example is separate from zone water-depth telemetry and is intended for `METRIC` forecast mode.
Existing file-based Items that show SI base units may need explicit `unit` metadata or an updated label pattern as shown above.

## Notes and Limitations

- The binding depends on Rachio Cloud availability and API rate limits.
- Use the Rachio app for full schedule, program, account, and device management.
- Smart Hose Timer state can be asynchronous; `state-matches` may remain `OFF` until the valve applies a change.
- Zone images are proxied through openHAB because Rachio can return image URLs without the expected media type.
