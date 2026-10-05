# CalDAV Binding

The CalDAV binding connects openHAB directly to CalDAV servers.
An account is represented by a bridge and each CalDAV Calendar Collection by a Calendar Thing.

The binding works independently and does not require another calendar binding.

The binding reads calendars; calendar creation and modification are not supported.

## Supported Things

### CalDAV Account

The `account` bridge represents a CalDAV server account.
It manages authentication, Calendar Collection discovery, connection handling and synchronization scheduling.

### CalDAV Calendar

The `calendar` thing represents one CalDAV Calendar Collection below an `account` bridge.

## Discovery

By default, Calendar Collections are discovered once when the account bridge is initialized.
Further discovery scans can be started explicitly from the Inbox.

The `discoveryMode` parameter defines what the configured `url` represents and therefore which CalDAV discovery steps are performed.

| Mode     | `url` represents                                   | Discovery steps                                                                                                           |
|----------|----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `AUTO`   | CalDAV service endpoint                            | Resolve `DAV:current-user-principal`, read all `CALDAV:calendar-home-set` URLs, then enumerate their Calendar Collections |
| `DIRECT` | Collection container such as a `calendar-home-set` | Enumerate Calendar Collections directly with `PROPFIND` and `Depth: 1`                                                    |

In `AUTO` mode, all `calendar-home-set` URLs returned for the principal are processed and the discovered Calendar Collections are deduplicated by collection URI.

A principal URL is not a separate discovery mode because it would only skip the initial `current-user-principal` request.
If the URL of an individual calendar is already known, the Calendar Thing can be configured manually with that URL as its `path`.
This does not disable Calendar Collection discovery for the account; the account still performs its initial discovery scan and scans explicitly requested from the Inbox.

The binding does not perform RFC 6764 bootstrapping through DNS SRV/TXT records or `/.well-known/caldav`.
Redirects are not followed, including redirects from a well-known URL.
Configure a URL matching the selected `discoveryMode` instead.
All discovered URLs must have the same scheme, host and effective port as the account URL.

## Time Range

Each Calendar Thing exposes events from a freely configurable time range.

Example: today plus six days:

```text
rangeAnchor = TODAY
rangeStartOffset = 0
rangeEndOffset = 6
```

This exposes seven calendar days in total.

Example: yesterday through tomorrow:

```text
rangeAnchor = TODAY
rangeStartOffset = -1
rangeEndOffset = 1
```

Events that overlap the selected time range are included.

## Synchronization

The account bridge checks for updates at `refreshInterval` (default 300 seconds, minimum 30 seconds).
The synchronization horizon covers `maxPastDays` before today through `maxFutureDays` after today, inclusive.
A Calendar Thing's output range must fit completely within that horizon.

The `syncMode` Account parameter selects the synchronization strategy:

- `SYNC_TOKEN` uses the server's change tracking to download updates and detect deleted events.
- `ETAG` compares server-provided resource versions and downloads only new or changed calendar data.
- `FULL` downloads all calendar data overlapping the synchronization horizon on every update.
- `AUTO` tries `SYNC_TOKEN`, then `ETAG`, then `FULL` when the server does not support a mode.
  Authentication and temporary server errors do not trigger this fallback.

A complete update publishes `sync#status=OK` and updates `sync#last`.
If some calendar data cannot be read, the status is `PARTIAL`; usable events remain available and `sync#last` retains the last complete success.
A failed update sets the Calendar Thing to `OFFLINE`, publishes `sync#status=ERROR` and preserves the last usable calendar data.
Bridge connection failures also set Calendar Things to `BRIDGE_OFFLINE` and their sync state to `ERROR`.

Previously synchronized calendar data can remain available after an openHAB restart.
Before and during the initial calendar synchronization, the Calendar Thing is `UNKNOWN` and `sync#status` is `SYNCING`.
The `UNKNOWN` state indicates that live calendar data has not yet been confirmed.
After the first successful synchronization, it becomes `ONLINE`.
During later synchronization checks, an online Calendar Thing stays `ONLINE` while `sync#status` temporarily changes to `SYNCING`.
Connection failures are retried automatically; a failure affecting one calendar does not take other calendars offline.

`REFRESH` on calendar channels republishes local state without making a network request.
Before any usable data is available, event values are `UNDEF` and `current#active` is `OFF`.

## Recurring Events

The binding expands RRULE, RDATE and EXDATE within the synchronization horizon, including monthly/yearly rules, BYDAY and UNTIL.
RECURRENCE-ID exceptions replace their original occurrences, including moved and cancelled instances.
`includeCancelled=false` excludes cancelled instances.
All-day recurrence, UTC, TZID/VTIMEZONE and floating times are supported.

`RECURRENCE-ID;RANGE=THISANDFUTURE` and period-valued RDATE are currently unsupported.
Resources containing those forms are reported as `PARTIAL` instead of publishing an incorrect recurrence set.
Parser and resource limits cause an error rather than silent truncation.

## Channels

All Calendar Thing channels are organized in channel groups.

### `events`

| Channel              | Item Type | Description                                                                            |
|----------------------|-----------|----------------------------------------------------------------------------------------|
| `events#json`        | String    | Parsed event instances overlapping the range, sorted and limited by `maxEvents`        |
| `events#count`       | Number    | Number of instances actually published in `events#json`                                |
| `events#range-start` | DateTime  | Effective inclusive range start                                                        |
| `events#range-end`   | DateTime  | Effective exclusive range end                                                          |
| `events#truncated`   | Switch    | Indicates that additional event instances were omitted because `maxEvents` was reached |

### `current`

| Channel               | Item Type |
|-----------------------|-----------|
| `current#active`      | Switch    |
| `current#uid`         | String    |
| `current#title`       | String    |
| `current#description` | String    |
| `current#location`    | String    |
| `current#start`       | DateTime  |
| `current#end`         | DateTime  |
| `current#all-day`     | Switch    |
| `current#organizer`   | String    |
| `current#categories`  | String    |

### `next`

The `next` group exposes the same event fields as `current`, except that it has no `active` channel.

`current` shows an event that is in progress within the configured time range, including all-day events.
`next` shows the next upcoming event in that range; past and running events are excluded.

These channels update as events start or end and as the configured time range moves, without waiting for the next server synchronization.
Unavailable event fields are `UNDEF`.
When there is no current event, `current#active` is `OFF`.

### `sync`

| Channel       | Item Type |
|---------------|-----------|
| `sync#last`   | DateTime  |
| `sync#status` | String    |
| `sync#error`  | String    |

## `events#json` Format

Example:

```json
[
  {
    "instanceId": "event-123|2026-09-18T06:00+02:00",
    "uid": "event-123",
    "recurrenceId": null,
    "title": "Waste collection",
    "description": "",
    "location": "",
    "start": "2026-09-18T06:00+02:00",
    "end": "2026-09-18T07:00+02:00",
    "allDay": false,
    "status": "CONFIRMED",
    "categories": ["Waste"],
    "organizer": ""
  }
]
```

For all-day events, `start` and `end` use `YYYY-MM-DD`; the `end` date is exclusive.
A successful update with no matching events publishes `[]`.

Treat `instanceId` as an opaque identifier.
`maxEvents` limits the published list; `events#truncated` indicates that additional parsed events were omitted.
It describes the output limit only; resource and parser failures are reported separately through the sync channels.
Timed values are ISO-8601 timestamps with an offset; seconds may be omitted when zero.

## MainUI Widget

The separate MainUI agenda widget displays events from a String Item linked to `events#json`.
Install the widget separately; it is not installed with the binding.
Widget installation and presentation are independent of the binding configuration.

## Configuration

### Account Parameters

| Parameter           | Default  | Current behavior                                                                                                                              |
|---------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `url`               | Required | Starting URL for CalDAV discovery; its meaning depends on `discoveryMode`                                                                     |
| `username`          | Optional | Username used for authentication. Configure both `username` and `password`, or leave both empty for anonymous access.                         |
| `password`          | Optional | Password or application password used for authentication. Configure both `username` and `password`, or leave both empty for anonymous access. |
| `requestTimeout`    | `30`     | Request timeout in seconds, 1–300                                                                                                             |
| `refreshInterval`   | `300`    | Polling delay in seconds; minimum 30                                                                                                          |
| `discoveryMode`     | `AUTO`   | Defines what `url` represents: `AUTO` starts with principal discovery; `DIRECT` enumerates Calendar Collections directly at `url`             |
| `authType`          | `AUTO`   | `BASIC`, `DIGEST`, or `AUTO` to accept either authentication challenge. Used when username and password are configured.                       |
| `verifyCertificate` | `true`   | Validates TLS certificates; `false` disables verification                                                                                     |
| `syncMode`          | `AUTO`   | `AUTO`, `SYNC_TOKEN`, `ETAG`, or `FULL`                                                                                                       |
| `maxPastDays`       | `30`     | Days before today in the sync horizon, 0–36500                                                                                                |
| `maxFutureDays`     | `365`    | Days after today in the sync horizon, 1–36500                                                                                                 |
| `readOnly`          | `true`   | Must be `true`; `false` is rejected                                                                                                           |

### Calendar Parameters

| Parameter          | Default  | Current behavior                                              |
|--------------------|----------|---------------------------------------------------------------|
| `path`             | Required | Calendar Collection URL or path relative to the account URL   |
| `rangeAnchor`      | `TODAY`  | `TODAY` (local midnight) or `NOW`                             |
| `rangeStartOffset` | `0`      | Inclusive start offset in days                                |
| `rangeEndOffset`   | `6`      | Inclusive final-day offset; exclusive end adds one day        |
| `maxEvents`        | `500`    | Maximum published instances, 1–50000                          |
| `includeCancelled` | `false`  | Includes cancelled instances when `true`                      |

The following examples configure an account bridge and one calendar.
Replace the server URL and credentials with values for the CalDAV service.

### Thing Configuration

:::: tabs

::: tab DSL

```java
Bridge caldav:account:ionos "CalDAV Account" [
    url="https://caldav.example.net/caldav/",
    username="user@example.net",
    password="SECRET"
] {
    Thing calendar family "Family Calendar" [
        path="https://caldav.example.net/caldav/family/"
    ]
}
```

:::

::: tab YAML

Save this YAML configuration under `$OPENHAB_CONF/yaml/`, for example as `caldav.yaml`.
See the [openHAB YAML configuration documentation](https://www.openhab.org/docs/configuration/yaml/).

```yaml
version: 1
things:
  caldav:account:ionos:
    isBridge: true
    label: CalDAV Account
    config:
      url: "https://caldav.example.net/caldav/"
      username: "user@example.net"
      password: "SECRET"

  caldav:calendar:ionos:family:
    bridge: caldav:account:ionos
    label: Family Calendar
    config:
      path: "https://caldav.example.net/caldav/family/"
```

:::

::::

### Item Configuration

```java
String   Family_Cal_Events      "Calendar events" { channel="caldav:calendar:ionos:family:events#json" }
Number   Family_Cal_Count       "Event count"      { channel="caldav:calendar:ionos:family:events#count" }
Switch   Family_Cal_Truncated   "Event list truncated" { channel="caldav:calendar:ionos:family:events#truncated" }
DateTime Family_Cal_LastSync    "Last sync" { channel="caldav:calendar:ionos:family:sync#last" }
String   Family_Cal_SyncStatus  "Sync"             { channel="caldav:calendar:ionos:family:sync#status" }
```

### Example: IONOS CalDAV

IONOS Mail Business was used as a real-world interoperability test environment during development of this binding.
The tested service is based on an Open-Xchange (OX) system.
The binding itself uses standard CalDAV and WebDAV mechanisms and is not specific to IONOS or Open-Xchange.

For the tested IONOS Mail Business setup, the CalDAV account endpoint was:

```text
https://dav.mailbusiness.ionos.de/caldav/
```

This endpoint can be configured as the Account Thing `url`.

With automatic discovery enabled, the server returns the available Calendar Collections.
Their URLs follow this form:

```text
https://dav.mailbusiness.ionos.de/caldav/<calendar-id>/
```

For example:

```text
Account URL:
https://dav.mailbusiness.ionos.de/caldav/

Discovered Calendar Collection:
https://dav.mailbusiness.ionos.de/caldav/<calendar-id>/
```

For the tested IONOS setup, the complete Calendar Collection URL could also be obtained from the calendar properties in Webmail and configured directly as the Calendar Thing `path`.

The exact endpoints and discovery behavior exposed by a CalDAV provider can depend on the server software and its configuration.
Other Open-Xchange installations, other providers, or differently configured systems may therefore use different URL structures or discovery settings.

Automatic discovery should be preferred when the configured Account URL exposes the required CalDAV discovery properties.
If principal discovery is not available, `DIRECT` mode can be used with the corresponding collection URL as the Account URL.

## Security

- HTTPS is required, except for HTTP loopback addresses used by local servers.
  URLs with embedded credentials or fragments are rejected.
- Basic and Digest authentication are supported.
  Credentials are not forwarded to other servers; redirects are not followed.
- Prefer installing a valid certificate chain over disabling verification.
- Calendar data that exceeds size or complexity limits causes an error; the last usable data is retained.
- Calendar data is stored locally.
  Protect openHAB's storage like other personal calendar data.
- The binding performs no calendar writes.

## Time Zones and All-Day Events

The time range and floating timestamps use the time zone configured in openHAB.
Known `TZID` values such as `Europe/Bratislava` are resolved using the Java runtime time-zone database and do not require a matching `VTIMEZONE` component.
Embedded `VTIMEZONE` definitions are respected for calendar-defined time zones.
An unknown `TZID` without a usable definition causes synchronization to report `PARTIAL` for the affected calendar.
The per-collection CalDAV `calendar-timezone` property is currently not used.
`TODAY` uses local midnight; `NOW` uses the current time.
Both apply offsets in calendar days.
The end offset must be at least the start offset.

Timed recurrences preserve local wall-clock time across daylight-saving changes.
All-day dates remain date-only values in JSON, with exclusive end dates.

After changing the openHAB time zone, wait for the next synchronization to update calendar data.
If the output range moves beyond the synchronized horizon, the calendar reports an error until synchronization provides the required data.

## Troubleshooting

### Authentication failed

Check the server URL, username and password/application password.

### Calendar not found

Verify the Calendar Collection path or run discovery again.

### TLS certificate error

Fix the certificate trust chain rather than disabling certificate validation whenever possible.
