# ADR-010: Log Redaction of Personal Data and Identifiers

## Status

Accepted

## Context

Both the legacy MELCloud API (`MelCloudConnection`) and the MELCloud Home API
(`MelCloudHomeConnection`, `MelCloudHomeApiClient`) log raw HTTP request/response
bodies at `debug`/`trace` level for troubleshooting (e.g. `"Login response: {}"`,
`"Device list response: {}"`, `"MELCloud Home monitor/user response: {}"`,
`"MELCloud Home BFF GET {} -> {}"`). These bodies routinely contain:

- Personal data: the account holder's first/last name, e-mail address, and
  user-chosen labels for buildings/rooms/devices (`OwnerName`, `OwnerEmail`,
  `Name`, `givenDisplayName`, ...).
- Hardware/account identifiers: legacy `DeviceID`/`BuildingID`/`FloorID`/`AreaID`,
  MELCloud Home `id`/`systemId`/`unitId`, `MacAddress`/`connectedInterfaceIdentifier`,
  `SerialNumber`.
- A session credential: the legacy API's `ContextKey` and the MELCloud Home
  WebSocket `hash`, both bearer-style tokens — already covered by the
  project-wide "never log secrets/tokens" rule (`rules/java-coding-rules.md`),
  but not actually honored by these call sites before this change.

Two developer-configured `*Config#toString()` implementations
(`AcDeviceConfig`, `HeatpumpDeviceConfig`) are logged directly on handler
`initialize()` and printed the raw legacy `deviceID`/`buildingID`.
`MelCloudHomeUnitConfig#toString()` printed the raw MELCloud Home `unitId`.
Several handler/discovery call sites also interpolate a raw `unitId` or
`deviceID` directly into a log message outside of any JSON body (e.g.
`"Telemetry poll failed for ATA unit {}"`, `"Found device: {} : {}"` with a
raw properties map).

Real captured payloads confirmed this in practice: `src/test/ata.json` and
`src/test/atw-ftc7.json` (added as MELCloud Home Thing-handler test fixtures)
contain a real account holder's name, e-mail, building ID, unit ID, and
`connectedInterfaceIdentifier` (MAC address) — exactly the kind of data that
was ending up unredacted in `debug`/`trace` logs whenever these responses
were fetched.

## Decision

Introduce `org.openhab.binding.melcloud.internal.logging.SensitiveDataMasker`,
a stateless utility used at every log call site that previously exposed raw
personal data or identifiers:

- **`maskId(String)`** — partial masking for identifiers (device ID, unit ID,
  MAC address, serial number, ...): keeps the last 4 characters visible
  (`"...ac62"`), full redaction (`"***"`) if the value is 4 characters or
  shorter. Chosen over full removal so that multiple log lines referencing
  the same device can still be correlated during troubleshooting, without
  exposing the full identifier.
- **`maskJson(String)`** — regex-based redaction of a fixed set of known
  sensitive JSON keys (case-insensitive) in a raw JSON string: personal-data
  keys (`email`, `firstname`, `lastname`, `ownerName`, `deviceName`,
  `buildingName`, `floorName`, `areaName`, `givenDisplayName`) and the
  session-credential keys (`contextKey`, `hash`) are fully redacted;
  identifier keys (`id`, `deviceId`, `buildingId`, `floorId`, `areaId`,
  `imageId`, `ownerId`, `unitId`, `systemId`, `macAddress`, `serialNumber`,
  `connectedInterfaceIdentifier`) go through `maskId`. Only scalar
  (string/number/`null`) values are redacted; a sensitive key holding a
  nested object/array is left untouched — none of the DTOs in this binding
  currently do that, and a full JSON-tree-aware redactor was judged
  disproportionate to the risk.
  - The generic `name` key is deliberately **not** in the redaction list.
    Testing against the real `src/test/ata.json` capture caught this: the
    MELCloud Home API reuses `name` as a technical field-name inside its
    `settings` arrays (`{"name": "OperationMode", "value": "Cool"}`), so
    redacting it wholesale would also destroy unrelated, non-sensitive
    telemetry that troubleshooting a channel-mapping bug actually needs. The
    one place a generic `Name` key _is_ personal data — the legacy login
    response's account holder name — is redacted at that one call site via
    the new `maskAdditionalField(String, String)`, whose response shape is
    known not to collide with the settings-array pattern.
- **`maskGuidsInUrl(String)`** — the MELCloud Home BFF encodes `unitId` as a
  path/query segment (`/monitor/ataunit/{unitId}`,
  `?unitId={unitId}&...`), not just in the body; this masks any
  GUID-formatted substring the same way as `maskId`.

Applied at every identified call site: `MelCloudConnection` (login, device
list, device/heatpump status GET and SetAta/SetAtw POST bodies),
`MelCloudHomeConnection#fetchUserMonitor`, `MelCloudHomeApiClient` (BFF GET/PUT
url+body), `MelCloudDiscoveryService`/`MelCloudHomeUnitDiscoveryService`
(log a masked id instead of the raw properties map), `AcDeviceConfig`/
`HeatpumpDeviceConfig`/`MelCloudHomeUnitConfig#toString()`, and the direct
`unitId` interpolations in `MelCloudHomeAccountHandler`,
`MelCloudHomeAtaUnitHandler`, `MelCloudHomeAtwUnitHandler`.

`MelCloudHomeConnection#fetchDiscoveryDocument` (the OIDC
`.well-known/openid-configuration` document) is intentionally left unmasked:
it is static server metadata with no personal data or per-account
identifiers.

Not changed: the `deviceProperties`/`properties` maps passed to
`DiscoveryResultBuilder#withProperties(...)` still carry the raw MAC
address/device ID/unit ID. That is a legitimate, necessary use — openHAB
stores these as Thing properties for the user to see/use in their own
inventory — distinct from writing them to the application log file.

## Consequences

### Positive

- `debug`/`trace` logs (including logs a user might paste into a GitHub issue
  or forum post) no longer expose the account holder's name, e-mail, or the
  MAC address/serial number/systemId of their physical devices.
- The legacy session `ContextKey` and MELCloud Home WebSocket `hash` are now
  actually honored by the project's existing "never log secrets/tokens" rule,
  closing a pre-existing gap.
- Log lines referencing the same unit/device across multiple polls remain
  correlatable via the masked suffix, preserving most of the practical
  troubleshooting value.

### Negative

- `maskJson` is a fixed key allowlist, not a schema-driven redactor: a future
  DTO field that is personal data or an identifier but isn't named like the
  existing ones (or is nested in an object/array) will not be redacted
  automatically and needs to be added to `SensitiveDataMasker`'s key lists.
- Regex-based JSON handling is a deliberate, documented trade-off (see
  `SensitiveDataMasker`'s class Javadoc) rather than a full JSON-tree parse;
  correct for the flat scalar fields this binding's APIs currently use, but
  would need revisiting if a sensitive field ever becomes a nested
  object/array.
- Building/room/area display names (the MELCloud Home API's building-level
  `name` field specifically, as opposed to `givenDisplayName` for
  units/devices, which _is_ redacted) are not covered by the generic
  redaction, per the `name`-key collision above. This is a known, accepted
  gap, not an oversight.
