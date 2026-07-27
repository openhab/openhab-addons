# OCPP Binding

This binding lets openHAB act as an OCPP 1.6-J central system, so EV chargers (charge points) that speak OCPP connect directly to openHAB — no vendor cloud required.
It is built on the [ChargeTime OCA-OCPP](https://github.com/ChargeTimeEU/Java-OCA-OCPP) library.

Chargers open a WebSocket connection to openHAB and are modelled as a three-tier hierarchy that mirrors OCPP itself: one server endpoint, the charge points that dial in to it, and the connectors of each charge point.

It reports connection state, connector status and metering, and controls charging: current limit, pause, remote start/stop, availability, unlock and reset.

## Supported Things

- `server`: the OCPP JSON WebSocket endpoint chargers connect to. Acts as the bridge for all charge points.
- `chargepoint`: one physical charger, matched to a session by its OCPP charge point id (the last path segment of the URL it dials). Bridge for its connectors.
- `connector`: one connector (outlet) of a charger, carrying the live status and metering channels.

## Discovery

Discovery is passive — chargers announce themselves.
When a charger connects with a charge point id that has no `chargepoint` thing, it appears in the inbox under its `server` bridge.
When a known charge point reports a connector for the first time, that `connector` appears in the inbox under its `chargepoint` bridge.
There is no active scan; point your charger at `ws://<openhab-host>:<port>/<chargePointId>` and it will show up.

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

On each BootNotification the charger receives the configured settings above as a ChangeConfiguration burst, `configSettleSeconds` after boot; measurands a charger rejects are dropped one at a time until it accepts them (and the accepted set is remembered).
The binding also runs a heartbeat-derived liveness watchdog and self-heals when a charger reconnects under a new session.

### `chargepoint`

| Name          | Type | Description                                            | Default | Required | Advanced |
|---------------|------|-------------------------------------------------------|---------|----------|----------|
| chargePointId | text | The charger's OCPP identity (its WebSocket URL suffix) | N/A     | yes      | no       |

### `connector`

| Name        | Type    | Description                    | Default | Required | Advanced |
|-------------|---------|--------------------------------|---------|----------|----------|
| connectorId | integer | OCPP connector number (1..N)   | 1       | no       | no       |

## Channels

### `chargepoint`

| Channel   | Type     | Read/Write | Description                                |
|-----------|----------|------------|--------------------------------------------|
| connected | Switch   | R          | Whether the charger has an open session    |
| last-seen  | DateTime | R          | Timestamp of the last message received     |

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
| lock               | Switch                 | W          | Momentary — unlock the connector                       |
| reset              | Switch                 | W          | Momentary — soft reset the charger                     |
| hardware-max-current | Number:ElectricCurrent | RW         | Hardware current ceiling via a vendor config key       |

Beyond the channels above, the connector also exposes the full OCPP 1.6 SampledValue set — aggregate and per-phase current/voltage, active/reactive/apparent power, power factor, frequency, active/reactive energy (register and interval, import and export), plus vehicle telemetry (`soc`, `rpm`, `temperature`) — and per-transaction metadata (`id-tag`, `transaction-id`, `meter-start`, `meter-stop`, and the start/stop timestamps).

For chargers that reject a TxProfile outside a transaction (e.g. Phoenix CHARX), set `forceTxDefaultProfile` on the connector so the charge limit is sent as a TxDefaultProfile.

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
