# OCPP Binding

This binding lets openHAB act as an OCPP 1.6-J central system, so EV chargers (charge points) that speak OCPP connect directly to openHAB — no vendor cloud required.
It is built on the [ChargeTime OCA-OCPP](https://github.com/ChargeTimeEU/Java-OCA-OCPP) library.

Chargers open a WebSocket connection to openHAB and are modelled as a three-tier hierarchy that mirrors OCPP itself: one server endpoint, the charge points that dial in to it, and the connectors of each charge point.

It reports connection state, connector status and metering, and controls charging: current limit, pause, remote start/stop, availability, unlock and reset.

## Supported Things

- `server`: the OCPP JSON WebSocket endpoint chargers connect to, and the bridge for all charge points.
- `chargepoint`: one physical charger matched to a session by its OCPP charge point id (the URL path it dials, without the leading slash), and the bridge for its connectors.
- `connector`: one connector (outlet) of a charger, carrying the live status and metering channels.

## Discovery

Discovery is passive — chargers announce themselves.
When a charger connects with a charge point id that has no `chargepoint` thing, it appears in the inbox under its `server` bridge.
When a known charge point reports a connector for the first time, that `connector` appears in the inbox under its `chargepoint` bridge.
There is no active scan; point your charger at `ws://<openhab-host>:<port>/<chargePointId>` and it will show up.

If you are unsure what id your charger uses, you do not have to guess it.
Connect the charger and it appears in the inbox under its real id, ready to accept.
Or enable `log:set DEBUG org.openhab.binding.ocpp` and look for the `Charger connected: id=...` line, which prints the exact id the charger dialed.
The id is whatever path the charger appends to its backend URL — often its serial number — so it is easiest to read it back here rather than hunt for it in the charger's own settings.

## Thing Configuration

### `server`

| Name                        | Type    | Description                                                                 | Default | Required | Advanced |
|-----------------------------|---------|-----------------------------------------------------------------------------|---------|----------|----------|
| port                        | integer | TCP port the OCPP server listens on                                         | 8887    | no       | no       |
| host                        | text    | Local bind address                                                          | 0.0.0.0 | no       | yes      |
| heartbeatInterval           | integer | Heartbeat interval (s) returned to chargers on boot                         | 300     | no       | yes      |
| meterValuesData             | text    | Measurands to configure on chargers (empty = leave unchanged)               | (empty) | no       | yes      |
| meterValueSampleInterval    | integer | MeterValueSampleInterval to configure (-1 = leave unchanged)                | -1      | no       | yes      |
| clockAlignedDataInterval    | integer | ClockAlignedDataInterval to configure (-1 = leave unchanged)                | -1      | no       | yes      |
| disableRemoteTxAuthorization| boolean | Configure AuthorizeRemoteTxRequests=false                                    | false   | no       | yes      |
| vendorConfig                | text[]  | Extra ChangeConfiguration entries as key=value, applied on boot             | (empty) | no       | yes      |
| pingInterval                | integer | WebSocket ping interval (s). A charger that does not answer a ping is disconnected, and many never do — leave at 0 unless yours is known to reply | 0 | no | yes |
| requestTimeoutSeconds       | integer | Seconds before an unanswered request to a charger fails                     | 30      | no       | yes      |
| authPassword                | text    | HTTP Basic password chargers must present (username = charge point id), 16–20 visible ASCII characters. Empty disables authentication | (empty) | no | yes |
| tags                        | text[]  | idTag whitelist. Empty accepts every tag; otherwise unknown tags are rejected | (empty) | no     | yes      |
| chargers                    | text[]  | Charge point id allow-list. Empty accepts any charger; otherwise unlisted ones are rejected | (empty) | no | yes |

These settings are pushed to a charger as ChangeConfiguration requests after it boots, one at a time, and only until the charger has accepted them once for the configured values — a changed configuration is sent again on the charger's next boot, an unchanged one is not repeated on every reconnect.
A request a charger leaves unanswered fails after `requestTimeoutSeconds`; the OCPP library itself would wait on it forever.
Measurands a charger rejects are dropped one at a time until it accepts them, and the accepted set is remembered per configuration key.
The binding also runs a heartbeat-derived liveness watchdog and self-heals when a charger reconnects under a new session.

### `chargepoint`

| Name          | Type | Description                                            | Default | Required | Advanced |
|---------------|------|-------------------------------------------------------|---------|----------|----------|
| chargePointId | text | The charger's OCPP identity (its WebSocket URL suffix) | N/A     | yes      | no       |
| configSettleSeconds | integer | Delay after BootNotification before the configuration above is sent. Some chargers are not ready to answer immediately | 0 | no | yes |
| meterless     | boolean | The charger has no internal meter: skip measurand configuration and disable clock-aligned sampling | false | no | yes |
| heartbeat     | integer | Per-charger heartbeat interval (s), overriding the server default. Also sizes this charger's liveness window. 0 uses the server default | 0 | no | yes |

### `connector`

| Name        | Type    | Description                    | Default | Required | Advanced |
|-------------|---------|--------------------------------|---------|----------|----------|
| connectorId | integer | OCPP connector number (1..N)   | 1       | no       | no       |
| forceTxDefaultProfile | boolean | Always send the charge limit as a TxDefaultProfile, even during a transaction. Needed for chargers that reject a TxProfile outside one | false | no | yes |
| profileMinIntervalMs | integer | Minimum spacing (ms) between SetChargingProfile sends; rapid changes are coalesced. 0 disables | 0 | no | yes |
| hardwareMaxCurrentKey | text | Vendor ChangeConfiguration key backing the `hardware-max-current` channel. Empty disables that channel | (empty) | no | yes |
| remoteStartTag | text | idTag used when starting a transaction via the `charging` channel | openhab | no | yes |
| meterValuesPollSeconds | integer | Poll this connector for MeterValues every N seconds via TriggerMessage. 0 disables polling | 0 | no | yes |
| stuckStateRecovery | boolean | Send an UnlockConnector if the connector stays in a transient state (Preparing/Finishing) too long. Off by default; enable only for a charger known to wedge there | false | no | yes |

Most connectors need no configuration beyond `connectorId`.
The rest cover specific charger behaviors.
`forceTxDefaultProfile` is for chargers that reject a `TxProfile` when no transaction is active — a Phoenix CHARX does: the charge limit is then sent as a `TxDefaultProfile`, which such chargers accept and apply through their own load management.
`profileMinIntervalMs` coalesces rapid limit changes into at most one `SetChargingProfile` per interval, which keeps a solar-tracking rule that adjusts the limit every few seconds from flooding the charger.
`meterValuesPollSeconds` actively polls a connector for `MeterValues` for chargers that do not push them on their own; a poll is skipped while the previous one is still outstanding, so a charger that stops answering cannot build a backlog.
`hardwareMaxCurrentKey` binds the `hardware-max-current` channel to a vendor `ChangeConfiguration` key, since the hardware ceiling is not a standard OCPP setting.
`stuckStateRecovery` is left off because auto-unlocking a connector is a physical side effect, and `Preparing` and `Finishing` are normal states a charger can dwell in.

## Channels

### `chargepoint`

| Channel   | Type     | Read/Write | Description                                |
|-----------|----------|------------|--------------------------------------------|
| connected | Switch   | R          | Whether the charger has an open session    |
| last-seen  | DateTime | R          | Timestamp of the last contact from the charger |
| reset      | Switch   | W          | Momentary — soft reset the charge point    |

Vendor, model, firmware version and serial number are published as thing properties from the charger's BootNotification.

### `connector`

| Channel            | Type                   | Read/Write | Description                                             |
|--------------------|------------------------|------------|--------------------------------------------------------|
| charge-point-status  | String                 | R          | OCPP status (Available, Preparing, Charging, ...)      |
| cable-connected     | Switch                 | R          | Whether a vehicle cable is plugged in (derived)        |
| current-import-l1/l2/l3 | Number:ElectricCurrent | R         | Imported current per phase (MeterValues)               |
| voltage-l1/l2/l3      | Number:ElectricPotential | R        | Voltage per phase (MeterValues)                        |
| current-offered     | Number:ElectricCurrent | R          | Current offered to the vehicle                         |
| power-active-import  | Number:Power           | R          | Active power imported                                  |
| power-offered       | Number:Power           | R          | Power offered to the vehicle                           |
| energy-active-import | Number:Energy          | R          | Energy register (Energy.Active.Import.Register)        |
| charging           | Switch                 | RW         | ON while a transaction runs; command to remote start/stop |
| charge-limit        | Number:ElectricCurrent | RW         | Charge current cap via SetChargingProfile              |
| pause              | Switch                 | RW         | Pause charging (profile limit 0) without ending the transaction |
| availability       | Switch                 | RW         | OCPP availability (Operative/Inoperative)              |
| unlock             | Switch                 | W          | Momentary — unlock the connector                       |
| hardware-max-current | Number:ElectricCurrent | RW         | Hardware current ceiling via a vendor config key       |

Beyond the channels above, the connector also exposes the full OCPP 1.6 SampledValue set — aggregate and per-phase current/voltage, active and reactive power, power factor, frequency, active/reactive energy (register and interval, import and export), plus vehicle telemetry (`soc`, `rpm`, `temperature`) — and per-transaction metadata (`id-tag`, `transaction-id`, `meter-start`, `meter-stop`) and the metering timestamps (`timestamp`, `timestamp-start`, `timestamp-stop`).

For chargers that reject a TxProfile outside a transaction (e.g. Phoenix CHARX), set `forceTxDefaultProfile` on the connector so the charge limit is sent as a TxDefaultProfile.

## Controlling a charge

The connector's writable channels map to OCPP commands, and each updates only once the charger confirms the command — a rejected request leaves the channel showing the real state rather than the requested one.

`charging` starts and stops a transaction: sending it `ON` issues a `RemoteStartTransaction`, `OFF` a `RemoteStopTransaction`.
The transaction is started with the idTag from the connector's `remoteStartTag` (default `openhab`), which has to be authorized: by this binding through the `server` thing's `tags` list (empty accepts any tag), and by the charger itself if it enforces its own whitelist.
So if `ON` does nothing, set `remoteStartTag` to a tag your charger accepts, or allow that tag on the charger.
Most chargers also only start once a vehicle is plugged in, so a `RemoteStart` on an idle connector is often ignored.
Because `charging` follows the charger's reported status, it also reads `ON` on its own whenever a transaction is running, however it was started.

`charge-limit` caps the charging current: the value is sent as a `SetChargingProfile` and the channel reflects the applied limit once accepted.
`pause` suspends charging with a 0 A profile without ending the transaction; switching it off resumes — at your `charge-limit` if one is set, otherwise by removing the cap so the charger returns to its own maximum — distinct from `charging`, which ends the session.
A pause is a 0 A limit, so a resume must lift the cap rather than send another 0 A, which a charger reads as "stay suspended".
`availability` takes the connector Operative or Inoperative, `unlock` releases the cable lock, and the `chargepoint`-level `reset` performs a soft reset of the whole charger.

## Full Example

### `demo.things`

```java
Bridge ocpp:server:main [ port=8887 ] {
    Bridge chargepoint wallbox "Wallbox" [ chargePointId="wallbox" ] {
        Thing connector c1 "Connector 1" [ connectorId=1 ]
    }
}
```

### `demo.items`

```java
String  Wallbox_Status  "Status [%s]"             { channel="ocpp:connector:main:wallbox:c1:charge-point-status" }
Switch  Wallbox_Cable   "Cable connected"         { channel="ocpp:connector:main:wallbox:c1:cable-connected" }
Number:Power  Wallbox_Power "Power [%.0f W]"      { channel="ocpp:connector:main:wallbox:c1:power-active-import" }
Number:Energy Wallbox_Energy "Energy [%.2f kWh]"  { channel="ocpp:connector:main:wallbox:c1:energy-active-import" }
```

## Troubleshooting

### A charge point stays UNKNOWN, or nothing appears in the inbox

The charge point id is the path of the WebSocket URL the charger dials, so the charger must connect to `ws://<host>:<port>/<chargePointId>`.
A charger pointed at the bare root (`ws://<host>:<port>/`, nothing after the slash) sends no id and is ignored, logging `connected without a charge point id in its URL path`.
Put the id in the charger's backend URL — many chargers keep the URL and the id in separate fields, but it still has to end up as the URL path after the leading slash — and make the `chargepoint` thing's `chargePointId` match it exactly.
If you are unsure what the charger actually sends, enable `log:set DEBUG org.openhab.binding.ocpp` and read it off the `Charger connected: id=...` line.

### A connector sits at SuspendedEVSE and will not charge

`SuspendedEVSE` means the charge point itself is withholding energy — a charging-profile limit or an authorization result — unlike `SuspendedEV`, which is the vehicle not drawing (battery full, or charging scheduled in the car).
Check the connector is not left paused and that `charge-limit` is not 0: sending `pause` OFF resumes charging — at your `charge-limit` if one is set, otherwise by clearing the cap so the charger returns to its own maximum.

## Security

Without `authPassword` the endpoint runs OCPP security profile 0: a plain-text WebSocket that accepts every connection, appropriate only on a trusted LAN.
Anyone who can reach the port can connect under any charge point id, so restrict exposure by binding a specific interface (`host`) or with firewall rules.
Setting `authPassword` enables HTTP Basic authentication (security profile 1): a charger must present the password with its charge point id as the username, and other connections are rejected before a session opens.
The password must be 16–20 visible ASCII characters — the OCPP library rejects other lengths during the handshake, before authentication even runs.
