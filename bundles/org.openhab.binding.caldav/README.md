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
Discovered Calendar Things store `path` relative to that server origin, for example `/calendars/user/family/`.
The Calendar Collection can be outside the account URL's path; absolute URLs remain valid for manually configured Calendar Things.

The server's display name is used as the Calendar label.
When several calendars in the account have the same name, the binding adds a short path suffix, such as `Kalender (123)` or `Kalender (a/family)`.
If the paths cannot distinguish the calendars, a short digest is used instead.
Calendars without a display name use a compact path or digest as their label.

Discovery also reads optional server metadata in the same request and stores available values as Thing properties:

| Property | Description |
| -------- | ----------- |
| `calendarDescription` | Server-provided Calendar Collection description |
| `calendarColor` | Server-provided color from the optional Apple calendar extension |
| `calendarPrivileges` | Reported DAV privileges, sorted and comma-separated, for example `read,write,write-content` |

These properties are only present when supplied by the server; `calendarPrivileges` requires at least one recognized privilege.
Privileges are informational and do not grant local permission to write.
`readOnly` remains the local safety boundary and must stay enabled; the binding reads calendars only.
Description and color values longer than 4096 characters are omitted.

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
  Limited responses are continued before the update is published; a synchronization attempt is limited to 100 pages and 5000 resource changes.
- `ETAG` compares server-provided resource versions and downloads only new or changed calendar data.
- `FULL` downloads all calendar data overlapping the synchronization horizon on every update.
- `AUTO` tries `SYNC_TOKEN`, then `ETAG`, then `FULL` when the server does not support a mode.
  Authentication and temporary server errors do not trigger this fallback.

A complete update publishes `sync#status=OK` and updates `sync#last`.
If some calendar data cannot be read, the status is `PARTIAL`; usable events remain available and `sync#last` retains the last complete success.
A failed update sets the Calendar Thing to `OFFLINE`, publishes `sync#status=ERROR` and preserves the last usable calendar data.
When the Account connection fails, Calendar Things without their own synchronization error are set to `BRIDGE_OFFLINE` and their sync state to `ERROR`.
A Calendar Thing with its own error retains that diagnosis until its next successful synchronization.

The Account Thing starts as `UNKNOWN` until a successful CalDAV request confirms the connection.
A scheduled check without a server request does not confirm connectivity or clear a previous connection error.

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

| Channel | Channel Type ID | Item Type | Description |
| --- | --- | --- | --- |
| `events#json` | `events-json` | String | All event instances within the configured time range as a JSON array. |
| `events#count` | `event-count` | Number | Number of event instances within the configured time range. |
| `events#range-start` | `range-start` | DateTime | Effective start of the current output range. |
| `events#range-end` | `range-end` | DateTime | Exclusive end of the current output range. |
| `events#truncated` | `events-truncated` | Switch | Indicates whether additional event instances were omitted because the configured maximum event count was reached. |

### `current`

| Channel | Channel Type ID | Item Type | Description |
| --- | --- | --- | --- |
| `current#active` | `current-active` | Switch | Indicates whether an event is currently active. |
| `current#uid` | `current-uid` | String | UID of the currently active event. |
| `current#title` | `current-title` | String | Title of the currently active event. |
| `current#description` | `current-description` | String | Description of the currently active event. |
| `current#location` | `current-location` | String | Location of the currently active event. |
| `current#start` | `current-start` | DateTime | Start time of the currently active event. |
| `current#end` | `current-end` | DateTime | End time of the currently active event. |
| `current#all-day` | `current-all-day` | Switch | Indicates whether the currently active event is an all-day event. |
| `current#organizer` | `current-organizer` | String | Organizer of the currently active event. |
| `current#categories` | `current-categories` | String | Categories of the currently active event. |

### `next`

| Channel | Channel Type ID | Item Type | Description |
| --- | --- | --- | --- |
| `next#uid` | `next-uid` | String | UID of the next event. |
| `next#title` | `next-title` | String | Title of the next event. |
| `next#description` | `next-description` | String | Description of the next event. |
| `next#location` | `next-location` | String | Location of the next event. |
| `next#start` | `next-start` | DateTime | Start time of the next event. |
| `next#end` | `next-end` | DateTime | End time of the next event. |
| `next#all-day` | `next-all-day` | Switch | Indicates whether the next event is an all-day event. |
| `next#organizer` | `next-organizer` | String | Organizer of the next event. |
| `next#categories` | `next-categories` | String | Categories of the next event. |

`current` shows an event that is in progress within the configured time range, including all-day events.
`next` shows the next upcoming event in that range; past and running events are excluded.

These channels update as events start or end and as the configured time range moves, without waiting for the next server synchronization.
When no event is selected, its event fields are `UNDEF`.
For a selected event, missing optional text fields are empty strings.
When there is no current event, `current#active` is `OFF`.

### `sync`

| Channel | Channel Type ID | Item Type | Description |
| --- | --- | --- | --- |
| `sync#last` | `last-sync` | DateTime | Time of the last complete successful synchronization. |
| `sync#status` | `sync-status` | String | Current synchronization status of the calendar. |
| `sync#error` | `connection-error` | String | Last communication or synchronization error reported for this calendar. |

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

| Name | Type | Description | Default | Required | Advanced |
| --- | --- | --- | --- | --- | --- |
| `url` | text | Starting URL for CalDAV discovery; its meaning depends on `discoveryMode` | N/A | yes | no |
| `username` | text | Username used for authentication. Configure both `username` and `password`, or leave both empty for anonymous access. | N/A | no | no |
| `password` | text | Password or application password used for authentication. Configure both `username` and `password`, or leave both empty for anonymous access. | N/A | no | no |
| `authType` | text | `BASIC`, `DIGEST`, or `AUTO` to accept either authentication challenge. Used when username and password are configured. | `AUTO` | yes | no |
| `requestTimeout` | integer | Request timeout in seconds, 1–300 | `30` | yes | no |
| `verifyCertificate` | boolean | Validates TLS certificates; `false` disables verification | `true` | yes | no |
| `discoveryMode` | text | Defines what `url` represents: `AUTO` starts with principal discovery; `DIRECT` enumerates Calendar Collections directly at `url` | `AUTO` | yes | no |
| `refreshInterval` | integer | Polling delay in seconds; 30–2147483647 | `300` | yes | no |
| `syncMode` | text | `AUTO`, `SYNC_TOKEN`, `ETAG`, or `FULL` | `AUTO` | yes | no |
| `maxPastDays` | integer | Days before today in the sync horizon, 0–36500 | `30` | yes | no |
| `maxFutureDays` | integer | Days after today in the sync horizon, 1–36500 | `365` | yes | no |
| `readOnly` | boolean | Must be `true`; `false` is rejected | `true` | yes | yes |

### Calendar Parameters

| Name | Type | Description | Default | Required | Advanced |
| --- | --- | --- | --- | --- | --- |
| `path` | text | Absolute same-origin Calendar Collection URL or URI reference resolved against the account URL; the collection need not be below the account path | N/A | yes | no |
| `rangeAnchor` | text | `TODAY` (local midnight) or `NOW` | `TODAY` | yes | no |
| `rangeStartOffset` | integer | Inclusive start offset in days; -2147483648–2147483647 | `0` | yes | no |
| `rangeEndOffset` | integer | Inclusive final-day offset; exclusive end adds one day; -2147483648–2147483647 | `6` | yes | no |
| `maxEvents` | integer | Maximum published instances, 1–50000 | `500` | yes | no |
| `includeCancelled` | boolean | Includes cancelled instances when `true` | `false` | yes | yes |

The Calendar Collection must have the same scheme, host and effective port as the account URL.
The account path is not a required prefix of the collection path.
For example, account URL `https://example.org/caldav/` with `path="/calendars/users/9/family/"` resolves to `https://example.org/calendars/users/9/family/`.
Automatically discovered calendars use such origin-relative references; absolute same-origin URLs also remain valid for manual configuration.

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
Calendar queries send the openHAB time zone in `CALDAV:timezone` so the server applies the same time zone to date-only and floating values.
`TODAY` uses local midnight; `NOW` uses the current time.
Both apply offsets in calendar days.
The end offset must be at least the start offset.

Timed recurrences preserve local wall-clock time across daylight-saving changes.
All-day dates remain date-only values in JSON, with exclusive end dates.

After changing the openHAB time zone, wait for the next synchronization to update calendar data.
If the output range moves beyond the synchronized horizon, the calendar reports an error until synchronization provides the required data.

## Troubleshooting

### Authentication failed

HTTP 401 is reported as an authentication failure.
Check the server URL, username and password/application password.
The Account Thing remains `OFFLINE` while requests are rejected and returns to `ONLINE` after successful server communication.

### Access forbidden

HTTP 403 means that the account may be authenticated but does not have permission to access the endpoint or calendar.
Check the account permissions and the Calendar Collection path.

### Connection errors

The status description distinguishes an unresolved server name, a connection failure, a timeout and a TLS connection failure.
Check DNS resolution, server availability, network connectivity and the configured request timeout.
Other HTTP errors include the status code; for example, HTTP 500 indicates a server error.
The binding retries failed communication automatically and preserves the last usable calendar data.

### Calendar not found

HTTP 404 at the discovery endpoint indicates that the configured server URL could not be found.
For a calendar, verify the Calendar Collection path or run discovery again.
A calendar that disappears after a successful synchronization is reported as `GONE`.

### TLS certificate error

Check the certificate trust chain, hostname and certificate validity.
Correct certificate problems and keep certificate verification enabled whenever possible.
