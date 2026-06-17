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
| `valve-program` | Smart Hose Timer schedule-like program resource with its own metadata and command channels. |

Rachio exposes Smart Hose Timer valve programs as separate cloud resources linked to a valve and base station.
The binding therefore models each one as a `valve-program` Thing instead of folding program data into the `valve` Thing.

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

When `callbackUrl` is configured, the binding registers legacy Rachio NotificationService callbacks for irrigation controller resources.
Registration is retried automatically when local API-rate protection delays it.

Legacy callbacks do not carry `x-signature`.
They are accepted only when their `externalId` and controller identity match a registered bridge and controller.
After direct event handling, the binding still performs a reconciliation refresh as a safety check.

Handled legacy irrigation events include schedule status, zone run start/stop/completed/cycling, rain sensor, rain delay, weather intelligence or skip, and related controller status events.
Legacy delta, water budget, and brownout valve events are treated as refresh/reconciliation triggers when no dedicated channel exists.

Smart Hose Timer base stations, valves, and valve programs are maintained by polling.
The binding does not currently register valve-run, valve-program, Smart Lighting, or newer Rachio WebhookService callbacks.

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

`moisture-level` and `moisture-percent` are command-only soil moisture adjustment inputs.
Rachio does not return them as readable zone state, so they may be `UNDEF` until a command is sent.
Readable water-model values are available through the water model channels.

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

Switch Rachio_Schedule_Start "Start Morning Schedule" {
    channel="rachio:schedule:home:morning:start"
}

Switch Rachio_Valve_Run "Garden Hose" {
    channel="rachio:valve:home:garden-hose:run"
}
```

## Notes and Limitations

- The binding depends on Rachio Cloud availability and API rate limits.
- Use the Rachio app for full schedule, program, account, and device management.
- Smart Hose Timer state can be asynchronous; `state-matches` may remain `OFF` until the valve applies a change.
- Zone images are proxied through openHAB because Rachio can return image URLs without the expected media type.
- `moisture-level` and `moisture-percent` are command-only adjustment channels.
