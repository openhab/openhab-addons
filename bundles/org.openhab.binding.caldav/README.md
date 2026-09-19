# CalDAV Binding

The CalDAV binding connects openHAB directly to CalDAV servers. An account is represented by a bridge and each CalDAV Calendar Collection by a Calendar Thing.

The binding works independently and does not require another calendar binding. Its calendar parser dependencies, biweekly and vinnie, are embedded privately. Jackson is not required for the supported iCalendar text format; biweekly's optional jCal functionality is not used.

The binding reads calendars; calendar creation and modification are not supported.

## Supported Things

### CalDAV Account

The `account` bridge represents a CalDAV server account. It owns authentication, server discovery, Calendar Home discovery, connection handling and synchronization scheduling.

### CalDAV Calendar

The `calendar` thing represents one CalDAV Calendar Collection below an `account` bridge.

## Discovery

After configuring the account bridge, Calendar Collections are discovered in the background every ten minutes. A CalDAV scan can also be started from the Inbox. Things can be configured manually.

With `discoveryMode=AUTO`, the scan resolves the current user principal and Calendar Home before listing collections. With `DIRECT`, it lists collections at `calendarHome`, or at `url` if `calendarHome` is empty. The discovered collection URI is stored as `calendarUid` and used to derive the Thing ID; the display name is not used as the identity.

Periodic account checks use the same discovery mode. `DIRECT` can therefore be used with a Calendar Home that does not provide principal discovery. All discovered URLs must have the same scheme, host and effective port as the account URL.

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

Internally the binding uses a half-open interval `[startInclusive, endExclusive)`.

An event is included if:

```text
event.start < range.end
AND
event.end > range.start
```

## Synchronization

The account bridge polls at `refreshInterval` (default 300 seconds, minimum 30 seconds) and serializes requests for its Calendar Things. The synchronization horizon covers `maxPastDays` before today through `maxFutureDays` after today, inclusive. A Calendar Thing's output range must fit completely within that horizon.

- `SYNC_TOKEN` uses WebDAV `sync-collection`, downloads changed resources and processes explicit deletions. An invalid sync token causes a new initial synchronization.
- `ETAG` queries resource ETags and downloads only new or changed resources. Resources are removed only after a complete successful listing.
- `FULL` requests all calendar data overlapping the horizon on every poll.
- `AUTO` tries sync-token synchronization, then ETags, then full queries when the server explicitly rejects a report as unsupported (HTTP 405/501 or the DAV `supported-report` precondition). Authentication and temporary server errors do not trigger this fallback.

A complete update publishes `sync#status=OK` and updates `sync#last`. Individual malformed or unsupported resources produce `PARTIAL`; other resources remain available and `sync#last` retains the last complete success. A failed request or incomplete listing publishes `ERROR`, preserves the previous data and does not advance the sync token. Bridge connection failures also set Calendar Things to `BRIDGE_OFFLINE` and their sync state to `ERROR`.

Bounded raw calendar data and the last complete success are persisted through openHAB Storage. Restored data remains marked `ERROR` and the Calendar Thing remains offline until a server synchronization succeeds. Removing the Thing removes its stored cache. Connection failures are retried with bounded backoff; an individual calendar failure does not take other calendars offline.

`REFRESH` on calendar channels republishes local state without making a network request. Before any usable data is available, event values are `UNDEF` and `current#active` is `OFF`.

## Recurring Events

The binding expands RRULE, RDATE and EXDATE within the synchronization horizon, including monthly/yearly rules, BYDAY and UNTIL. RECURRENCE-ID exceptions replace their original occurrences, including moved and cancelled instances. `includeCancelled=false` excludes cancelled instances. All-day recurrence, UTC, TZID/VTIMEZONE and floating times are supported.

`RECURRENCE-ID;RANGE=THISANDFUTURE` and period-valued RDATE are currently unsupported. Resources containing those forms are reported as `PARTIAL` instead of publishing an incorrect recurrence set. Parser and resource limits cause an error rather than silent truncation.

## Channels

All Calendar Thing channels are organized in channel groups.

### `events`

| Channel | Item Type | Description |
|---|---|---|
| `events#json` | String | Parsed event instances overlapping the range, sorted and limited by `maxEvents` |
| `events#count` | Number | Number of instances actually published in `events#json` |
| `events#range-start` | DateTime | Effective inclusive range start |
| `events#range-end` | DateTime | Effective exclusive range end |
| `events#truncated` | Switch | Indicates that additional event instances were omitted because `maxEvents` was reached |

### `current`

| Channel | Item Type |
|---|---|
| `current#active` | Switch |
| `current#uid` | String |
| `current#title` | String |
| `current#description` | String |
| `current#location` | String |
| `current#start` | DateTime |
| `current#end` | DateTime |
| `current#all-day` | Switch |
| `current#organizer` | String |
| `current#categories` | String |

### `next`

The `next` group exposes the same event fields as `current`, except that it has no `active` channel.

`current` selects the first sorted instance satisfying `start <= now < end`, including all-day events in the openHAB time zone. `next` selects the first future instance; past and running instances are excluded. A zero-duration event at the current instant can be next but is never current. Both selections use the complete window before the JSON `maxEvents` limit.

Selection is recalculated locally at event boundaries, midnight and at least every minute for moving `NOW` windows. No network request is needed for these transitions. Missing event fields become `UNDEF` and `current#active` becomes `OFF`.

### `sync`

| Channel | Item Type |
|---|---|
| `sync#last` | DateTime |
| `sync#status` | String |
| `sync#error` | String |

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

For all-day events, `start` and `end` use `YYYY-MM-DD`; the `end` date is exclusive. A successful update with no matching events publishes `[]`.

Treat `instanceId` as an opaque identifier. `maxEvents` limits the published list after sorting; `events#truncated` indicates that additional parsed events were omitted. It describes the output limit only; resource and parser failures are reported separately through the sync channels. Event text is escaped by a JSON serializer, including control characters. Timed values are ISO-8601 timestamps with an offset; seconds may be omitted when zero.

## Configuration

### Account Parameters

| Parameter | Default | Current behavior |
|---|---|---|
| `url` | Required | Account endpoint used for principal and Calendar Home discovery |
| `username` | Required | Authentication username |
| `password` | Required | Password or application password |
| `requestTimeout` | `30` | Request timeout in seconds, 1–300 |
| `refreshInterval` | `300` | Polling delay in seconds; minimum 30 |
| `discoveryMode` | `AUTO` | Discovery and account-check mode: `AUTO` or `DIRECT` |
| `calendarHome` | Empty | Optional Calendar Home URL for `DIRECT` |
| `authType` | `AUTO` | `BASIC`, `DIGEST`, or `AUTO` to accept either authentication challenge |
| `verifyCertificate` | `true` | Validates TLS certificates; `false` disables verification for this account only |
| `syncMode` | `AUTO` | `AUTO`, `SYNC_TOKEN`, `ETAG`, or `FULL` |
| `maxPastDays` | `30` | Days before today in the sync horizon, 0–36500 |
| `maxFutureDays` | `365` | Days after today in the sync horizon, 1–36500 |
| `readOnly` | `true` | Must be `true`; `false` is rejected |

### Calendar Parameters

| Parameter | Default | Current behavior |
|---|---|---|
| `path` | Required | Calendar Collection URL or path relative to the account URL |
| `calendarId` | Required | Collection identifier in metadata; not used to build requests |
| `enabled` | `true` | Enables synchronization |
| `rangeAnchor` | `TODAY` | `TODAY` (local midnight) or `NOW` |
| `rangeStartOffset` | `0` | Inclusive start offset in days |
| `rangeEndOffset` | `6` | Inclusive final-day offset; exclusive end adds one day |
| `maxEvents` | `500` | Maximum published instances, 1–50000 |
| `includeCancelled` | `false` | Includes cancelled instances when `true` |

The following examples configure an account bridge and one calendar. Replace the
server URL and credentials with values for the CalDAV service.

### YAML

Save this example under `$OPENHAB_CONF/yaml/`, for example as `caldav.yaml`. See the [openHAB YAML configuration documentation](https://www.openhab.org/docs/configuration/yaml/).

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
      discoveryMode: AUTO
      refreshInterval: 300
      requestTimeout: 30

  caldav:calendar:ionos:family:
    bridge: caldav:account:ionos
    label: Family Calendar
    config:
      path: "https://caldav.example.net/caldav/family/"
      calendarId: "family"
      enabled: true
      rangeAnchor: TODAY
      rangeStartOffset: 0
      rangeEndOffset: 6
      maxEvents: 500
```

### Classic `.things` and `.items`

```text
Bridge caldav:account:ionos "CalDAV Account" [
    url="https://caldav.example.net/caldav/",
    username="user@example.net",
    password="SECRET",
    discoveryMode="AUTO",
    refreshInterval=300,
    requestTimeout=30
] {
    Thing calendar family "Family Calendar" [
        path="https://caldav.example.net/caldav/family/",
        calendarId="family",
        enabled=true,
        rangeAnchor="TODAY",
        rangeStartOffset=0,
        rangeEndOffset=6,
        maxEvents=500
    ]
}
```

```text
String   Family_Cal_Events     "Calendar events [%s]" { channel="caldav:calendar:ionos:family:events#json" }
Number   Family_Cal_Count       "Event count [%d]"      { channel="caldav:calendar:ionos:family:events#count" }
Switch   Family_Cal_Truncated   "Event list truncated [%s]" { channel="caldav:calendar:ionos:family:events#truncated" }
DateTime Family_Cal_LastSync    "Last sync [%1$tF %1$tR]" { channel="caldav:calendar:ionos:family:sync#last" }
String   Family_Cal_SyncStatus  "Sync [%s]"             { channel="caldav:calendar:ionos:family:sync#status" }
```

### IONOS CalDAV

IONOS Mail Business provides the individual calendar URL in Webmail under the calendar's properties. Copy that complete URL into the Calendar Thing's `path`, and use your full email address as the username. See the [IONOS CalDAV instructions](https://www.ionos.com/help/email/managing-mail-business/syncing-mail-business-calendar-with-mac-os-x/).

Configure the account `url` with an endpoint supporting principal and Calendar Home discovery, or use `DIRECT` with the actual Calendar Home. Do not construct a collection URL from its display name or `calendarId`. The `calendarId` parameter is required by the metadata but does not replace `path` in requests.

## Security

- HTTPS is required, except for HTTP loopback addresses used by local servers. URLs with embedded credentials or fragments are rejected.
- Basic and Digest authentication use an isolated account client. Credentials are not forwarded to foreign origins; redirects are not followed.
- TLS certificate and hostname verification are enabled by default. Prefer installing a valid certificate chain over disabling verification.
- XML external entities, DOCTYPE, external DTDs and XInclude are disabled. XML depth is limited to 64.
- Limits are 8 MiB per HTTP/XML response and per resource cache, 1 MiB per calendar resource, 5000 resources and 50000 expanded instances per calendar/horizon. A limit failure retains the previous state.
- Stored caches contain calendar data and sync tokens, but no passwords or authentication headers. Protect openHAB's storage like other personal calendar data.
- The binding performs no calendar writes.

## Time Zones and All-Day Events

The time range and floating timestamps use the time zone configured in openHAB. `TODAY` uses local midnight; `NOW` uses the current time. Both apply offsets in calendar days, and the exclusive end is the anchor plus `rangeEndOffset + 1` days. The end offset must be at least the start offset.

Timed recurrences preserve local wall-clock time across daylight-saving changes. All-day dates remain date-only values in JSON, with exclusive end dates. Without DTEND or DURATION, an all-day event lasts one day and a timed event has duration zero. Zero-duration events belong to the output range when their start is within `[startInclusive, endExclusive)`.

A time-zone change invalidates derived event data until synchronization recalculates it. Data extending beyond a cached horizon is marked stale while awaiting synchronization.

## Troubleshooting

### Authentication failed

Check the server URL, username and password/application password.

### Calendar not found

Verify the Calendar Collection path or run discovery again.

### TLS certificate error

Fix the certificate trust chain rather than disabling certificate validation whenever possible.

## Development Notes

Core metadata is located under:

```text
src/main/resources/OH-INF/
```

The separate MainUI agenda widget consumes `events#json` through a linked String Item. It is not installed with the binding. Widget installation and presentation are independent of the binding configuration.
