# Hager Flow Modbus Binding

Integrates the Hager Flow energy management system into openHAB over Modbus.
The Hager Flow XEM470 exposes several logical devices on one connection, each of them addressed by its own
Modbus unit id: the energy manager itself, up to eight charging stations (EVSE), a battery storage, up to
eight power meters and up to ten SG-Ready devices.
Every one of them is available as a Thing of this binding.

Communication is handled by the [Modbus binding](https://www.openhab.org/addons/bindings/modbus/), so a
Modbus TCP or Modbus serial bridge is required as the root of the Thing hierarchy.

## Supported Things

| Thing Type ID | Label | Description |
|---------------|-------|-------------|
| hager-flow-ems | Hager Flow EMS | Energy manager of a Hager Flow system, providing general settings and the summed measurements of all connected devices. |
| hager-flow-evse | Hager Flow EVSE | Charging station of a Hager Flow system with status, session and instantaneous charging values. |
| hager-flow-storage | Hager Flow Storage | Battery storage of a Hager Flow system. |
| hager-flow-powermeter | Hager Flow PowerMeter | Power meter of a Hager Flow system with per phase power, voltage and energy values. |
| hager-flow-sgready | Hager Flow SG-Ready Device | SG-Ready device of a Hager Flow system. |

Thing hierarchy: the energy manager is a Bridge which is attached to the Modbus bridge.
All other roles are Things which are attached to the energy manager.

## Discovery

The binding does not provide a discovery service, all Things have to be added manually.

## Thing Configuration

### Modbus Bridge

Create a Modbus TCP or Modbus serial bridge which connects to the device.
The unit id of the Modbus bridge itself is not used by this binding, because every Hager Flow Thing
carries its own `slaveId`.

### Hager Flow Thing Configuration

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| slaveId | integer | yes | see below | Modbus unit id of the device |
| refresh | integer | no | 5000 | Interval the registers are polled in milliseconds, at least 1000 |

The valid range and the default of `slaveId` depend on the role:

| Thing Type ID | Role | slaveId range | slaveId default |
|---------------|------|---------------|-----------------|
| hager-flow-ems | Hager Flow EMS | 0 - 247 | 0 |
| hager-flow-evse | Hager Flow EVSE | 1 - 8 | 1 |
| hager-flow-storage | Hager Flow Storage | 0 - 247 | 20 |
| hager-flow-powermeter | Hager Flow PowerMeter | 30 - 37 | 30 |
| hager-flow-sgready | Hager Flow SG-Ready Device | 50 - 59 | 50 |

## Channels

All register values are read only except the boost mode of a charging station.
Values which are declared as an enumeration in the Modbus table are provided as `String` channels,
the possible values are listed in the channel description.
Registers with independent bits are provided as one `Switch` channel per bit.

### Hager Flow EMS (`hager-flow-ems`)

#### Device Identification (`device-identification`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| vendor-name | String | RO | Vendor Name |  |
| product-code | String | RO | Product Code |  |
| vendor-url | String | RO | Vendor URL |  |
| product-serial-number | String | RO | Product Code (Serial number) |  |
| sw-version | String | RO | SW version |  |

#### General Information (`general-information`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| max-current-phase | Number:ElectricCurrent | RO | Max Current Phase | Main Fuse. |
| max-current-phase-derated | Number:ElectricCurrent | RO | Max Current Phase Derated (80%) | Main Fuse derated. |
| max-power-phase | Number:Power | RO | Max Power Phase | Main Fuse. |
| max-power-phase-derated | Number:Power | RO | Max Power Phase Derated (80%) | Main Fuse derated. |
| max-power | Number:Power | RO | Max Power | Max Installation power from grid. |
| max-power-derated | Number:Power | RO | Max Power Derated (80%) | Max Installation power from grid derated. |
| evcs-count | Number | RO | EVCS Number | Number of Connected EVSE. |
| evcs-list-evcs-1 | Switch | RO | EVCS 1 | Charging station 1 is connected to the energy manager. |
| evcs-list-evcs-2 | Switch | RO | EVCS 2 | Charging station 2 is connected to the energy manager. |
| evcs-list-evcs-3 | Switch | RO | EVCS 3 | Charging station 3 is connected to the energy manager. |
| evcs-list-evcs-4 | Switch | RO | EVCS 4 | Charging station 4 is connected to the energy manager. |
| evcs-list-evcs-5 | Switch | RO | EVCS 5 | Charging station 5 is connected to the energy manager. |
| evcs-list-evcs-6 | Switch | RO | EVCS 6 | Charging station 6 is connected to the energy manager. |
| evcs-list-evcs-7 | Switch | RO | EVCS 7 | Charging station 7 is connected to the energy manager. |
| evcs-list-evcs-8 | Switch | RO | EVCS 8 | Charging station 8 is connected to the energy manager. |
| phase-number | Number | RO | Phase Number | One phase or Three phases. |
| sun-power-priority-battery-first | Switch | RO | Battery First | Battery is charged before the car. |
| sun-power-priority-allow-battery-discharge-into-car | Switch | RO | Battery to Car | Battery discharge into the car is allowed. |
| blackout-active | String | RO | Blackout Active | Possible values: NOT_POSSIBLE, ACTIVE, NOT_ACTIVE, NOT_AVAILABLE, SWITCH_IN_ISLAND_STATE. |
| sgready-brain-disable | String | RO | SGReady Brain disable | Possible values: SGReady Brain enabled, SGReady Brain disabled. |
| sgready-power | Number:Power | RO | SGReady Power | Total nominal power controlled by the SG-Ready interface. |

#### Slave ID identity (`slave-id-identity`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| slaveid-type | String | RO | SlaveID type | Possible values: Main system (EMS), EVSE, Storage, Power Meter, SG-Ready. |
| slave-id-index-by-type | Number | RO | Slave ID index by type |  |

#### Slave ID 0 - Root/Main Measurements (`root-main-measurements`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| root-main-current-sum | Number:ElectricCurrent | RO | Root/Main Current (ΣL) |  |
| root-main-current-l1 | Number:ElectricCurrent | RO | Root/Main Current (L1) |  |
| root-main-current-l2 | Number:ElectricCurrent | RO | Root/Main Current (L2) |  |
| root-main-current-l3 | Number:ElectricCurrent | RO | Root/Main Current (L3) |  |
| root-main-power-sum | Number:Power | RO | Root/Main Power (ΣL) |  |
| root-main-power-l1 | Number:Power | RO | Root/Main Power (L1) |  |
| root-main-power-l2 | Number:Power | RO | Root/Main Power (L2) |  |
| root-main-power-l3 | Number:Power | RO | Root/Main Power (L3) |  |
| all-evcs-current-sum | Number:ElectricCurrent | RO | All EVCS Current (ΣL) |  |
| all-evcs-power-sum | Number:Power | RO | All EVCS Power (ΣL) |  |
| all-pv-current-sum | Number:ElectricCurrent | RO | All PV Current (ΣL) |  |
| all-pv-power-sum | Number:Power | RO | All PV Power (ΣL) |  |
| all-battery-current-sum | Number:ElectricCurrent | RO | All Battery Current (ΣL) |  |
| all-battery-power-sum | Number:Power | RO | All Battery Power (ΣL) |  |
| battery-state-of-charge | Number:Dimensionless | RO | Battery State of Charge |  |
| all-house-consumption-current-sum | Number:ElectricCurrent | RO | All HouseConsumption Current (ΣL) |  |
| all-house-consumption-power-sum | Number:Power | RO | All HouseConsumption Power (ΣL) |  |
| autarky-last-hour | Number:Dimensionless | RO | Autarky (last hour) |  |
| self-consumption-last-hour | Number:Dimensionless | RO | Self Consumption (last hour) |  |
| margin-to-max-power-total | Number:Power | RO | MarginToMaxPowerTotal |  |
| margin-to-max-power-l1 | Number:Power | RO | MarginToMaxPowerL1 |  |
| margin-to-max-power-l2 | Number:Power | RO | MarginToMaxPowerL2 |  |
| margin-to-max-power-l3 | Number:Power | RO | MarginToMaxPowerL3 |  |

### Hager Flow EVSE (`hager-flow-evse`)

#### Identity information (`identity`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| evcs-number | Number | RO | EVCS Number |  |
| evcs-type | String | RO | EVCS Type | Possible values: virtual, E3DC wallbox, Easy Connect wallbox, wallbox RSCP group (farming), Edison Connect wallbox, Multi Connect wallbox, Eebus Wallbox. |
| evcs-name | String | RO | EVCS Name | Friendly name set by User. |
| evcs-serial | String | RO | EVCS Serial |  |
| evcs-firmware-version | String | RO | EVCS Firmware version |  |

#### General Status and configuration (`general-status-and-configuration`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| evcs-state-normal-mode | Switch | RO | Normal Mode | Charging station operates in normal mode. |
| evcs-state-default-mode | Switch | RO | Default Mode | Charging station fell back to default mode because of a parameter error. |
| evcs-state-sun-mode-active | Switch | RO | Sun Mode | Charging from surplus photovoltaic power is active. |
| evcs-state-temperature-derating-active | Switch | RO | Temperature Derating | Charging power is reduced because of high temperature. |
| evcs-state-schuko-active | Switch | RO | Schuko Socket | The additional Schuko socket is active. |
| evcs-state-crc-wrong-meter-value-range | Switch | RO | Meter Value Error | The energy meter reports a value outside the expected range. |
| evcs-state-limited-operation | Switch | RO | Limited Operation | Not all operating modes are available. |
| evcs-energy-all | Number:Energy | RO | EVCS Energy All | EVCS Energy index. |
| evcs-solar-energy | Number:Energy | RO | EVCS Solar Energy | EVCS Solar Energy index. |
| evcs-connection-state | String | RO | EVCS Connection State | Possible values: EVCS disconnected, EVCS connected. |
| evcs-working-state | String | RO | EVCS Working State | Possible values: EVCS not working properly, EVCS working normally. |
| evcs-service-state | String | RO | EVCS Service State | Possible values: no upgrade ongoing, firmware upgrade ongoing. |
| evcs-power-meter-connection-state | String | RO | EVCS Power Meter Connection State | Possible values: power meter disconnected, power meter connected. |
| evcs-power-meter-working-state | String | RO | EVCS Power Meter Working State | Possible values: power meter not working properly, power meter working normally. |
| evcs-power-meter-service-state | String | RO | EVCS Power Meter Service State | Possible values: no upgrade ongoing, firmware upgrade ongoing. |
| evcs-availability | String | RO | EVCS Availability | Possible values: Unavailable (charge is forbidden), Available (charge might be authorized). |
| evcs-station-enabled | String | RO | EVCS Station Enabled | Possible values: Disabled, Enabled. |
| evcs-max-charging-power | Number:Power | RO | EVCS Max Charging Power | In total for all phases. This value might be changed when charging session is started. |
| evcs-max-charging-power-per-phase | Number:Power | RO | EVCS Max Charging Power per Phase |  |
| evcs-max-charging-current | Number:ElectricCurrent | RO | EVCS Max Charging Current | EVCS max allowed charging current. |
| evcs-min-charging-power | Number:Power | RO | EVCS Min Charging Power |  |
| evcs-min-charging-power-per-phase | Number:Power | RO | EVCS Min Charging Power per Phase |  |
| evcs-min-charging-current | Number:ElectricCurrent | RO | EVCS Min Charging Current | EVCS min allowed charging current. |
| evcs-boost-mode | Switch | RW | EVCS Boost Mode | Boost mode override for the next charging session. |
| evcs-phase-management | String | RO | EVCS Phase Management | Possible values: mono, tri, auto. |
| evcs-charge-strategy | String | RO | EVCS Charge Strategy | Possible values: Boost (Immediate), Delayed, disabled. |

#### Charging Session Information (`charging-session-information`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| evcs-state-session-sun-mode-active | Switch | RO | Sun Mode | Charging from surplus photovoltaic power was active during the session. |
| evcs-state-session-temperature-derating-active | Switch | RO | Temperature Derating | Charging power was reduced during the session because of high temperature. |
| charging-session-id | String | RO | Charging Session ID |  |
| charging-session-badge-id | String | RO | Charging Session Badge ID |  |
| active-phase | Number | RO | Active Phase |  |
| energy-session | Number:Energy | RO | Energy Session | Energy meter since start of current charging session, restarts at 0 Wh at each charging session. |
| grid-energy-session | Number:Energy | RO | Grid Energy Session | Energy meter since start of current charging session, restarts at 0 Wh at each charging session. |
| solar-energy-session | Number:Energy | RO | Solar Energy Session | Energy meter since start of current charging session, restarts at 0 Wh at each charging session. |

#### Instantaneous Measures (`instantaneous-measures`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| total-charging-current | Number:ElectricCurrent | RO | Total Charging Current |  |
| charging-current-l1 | Number:ElectricCurrent | RO | Charging Current (L1) |  |
| charging-current-l2 | Number:ElectricCurrent | RO | Charging Current (L2) |  |
| charging-current-l3 | Number:ElectricCurrent | RO | Charging Current (L3) |  |
| charging-power-total | Number:Power | RO | Charging Power Total |  |
| charging-power-l1 | Number:Power | RO | Charging Power (L1) |  |
| charging-power-l2 | Number:Power | RO | Charging Power (L2) |  |
| charging-power-l3 | Number:Power | RO | Charging Power (L3) |  |
| assign-power-total | Number:Power | RO | Assign Power Total |  |
| assigned-power-l1 | Number:Power | RO | Assigned Power (L1) |  |
| assigned-power-l2 | Number:Power | RO | Assigned Power (L2) |  |
| assigned-power-l3 | Number:Power | RO | Assigned Power (L3) |  |

### Hager Flow Storage (`hager-flow-storage`)

#### Storage Status (`status`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| storage-index | Number | RO | Storage Index |  |
| storage-dcdc-status | Switch | RO | Storage DCDC status |  |
| storage-bat-status | Switch | RO | Storage Bat Status |  |
| battery-state-of-charge | Number:Dimensionless | RO | Battery State of Charge |  |

### Hager Flow PowerMeter (`hager-flow-powermeter`)

#### PowerMeter Identity (`identity`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| powermeter-id | Number | RO | PowerMeter Id |  |
| powermeter-name | String | RO | PowerMeter Name |  |
| powermeter-type | String | RO | PowerMeter Type | Possible values: UNDEFINED, ROOT, ADDITIONAL, ADDITIONAL PRODUCTION, ADDITIONAL CONSUMPTION, FARM, UNUSED, WALLBOX, FARM ADDITIONAL. |

#### PowerMeter Status (`status`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| powermeter-connection-status | Switch | RO | PowerMeter Connection status |  |
| powermeter-working-status | Switch | RO | PowerMeter Working status |  |
| powermeter-in-service-status | Switch | RO | PowerMeter In service status |  |
| powermeter-active-phase-l1-active | Switch | RO | Phase L1 | Phase L1 carries current. |
| powermeter-active-phase-l2-active | Switch | RO | Phase L2 | Phase L2 carries current. |
| powermeter-active-phase-l3-active | Switch | RO | Phase L3 | Phase L3 carries current. |

#### PowerMeter Measurements (`measurements`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| powermeter-power-sum | Number:Power | RO | PowerMeter Power (ΣL) |  |
| powermeter-power-l1 | Number:Power | RO | PowerMeter Power (L1) |  |
| powermeter-power-l2 | Number:Power | RO | PowerMeter Power (L2) |  |
| powermeter-power-l3 | Number:Power | RO | PowerMeter Power (L3) |  |
| powermeter-voltage-l1 | Number:ElectricPotential | RO | PowerMeter Voltage (L1) |  |
| powermeter-voltage-l2 | Number:ElectricPotential | RO | PowerMeter Voltage (L2) |  |
| powermeter-voltage-l3 | Number:ElectricPotential | RO | PowerMeter Voltage (L3) |  |
| powermeter-energy-sum | Number:Energy | RO | PowerMeter Energy (ΣL) |  |
| powermeter-energy-l1 | Number:Energy | RO | PowerMeter Energy (L1) |  |
| powermeter-energy-l2 | Number:Energy | RO | PowerMeter Energy (L2) |  |
| powermeter-energy-l3 | Number:Energy | RO | PowerMeter Energy (L3) |  |

### Hager Flow SG-Ready Device (`hager-flow-sgready`)

#### SG-Ready Identity (`identity`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| sgready-index | Number | RO | SGReady index |  |
| sgready-name | String | RO | SGReady Name |  |

#### SG-Ready Status (`status`)

| Channel | Type | Access | Label | Description |
|---------|------|--------|-------|-------------|
| sgready-active | String | RO | SGReady active | Possible values: Inactive, active. |
| sgready-working | Switch | RO | SGReady working |  |
| sgready-status | String | RO | SGReady status | Possible values: STATE_1_BLOCK, STATE_2_NORMAL, STATE_3_GO, STATE_4_FORCE_GO. |

## Full Example

### Things

```java
Bridge modbus:tcp:flow "Hager Flow Modbus TCP" [ host="192.168.178.60", port=502, id=0 ] {
    Bridge modbus:hager-flow-ems:ems "Hager Flow EMS" [ slaveId=0, refresh=5000 ] {
        Thing modbus:hager-flow-evse:evse1 "Hager Flow EVSE 1" [ slaveId=1 ]
        Thing modbus:hager-flow-storage:storage "Hager Flow Storage" [ slaveId=20 ]
        Thing modbus:hager-flow-powermeter:meter1 "Hager Flow PowerMeter 1" [ slaveId=30 ]
        Thing modbus:hager-flow-sgready:sgready1 "Hager Flow SG-Ready 1" [ slaveId=50 ]
    }
}
```

### Items

```java
Number:Power           HagerFlow_GridPower            "Grid Power" { channel="modbus:hager-flow-ems:flow:ems:root-main-measurements#root-main-power-sum" }
Number:Power           HagerFlow_PvPower              "PV Power" { channel="modbus:hager-flow-ems:flow:ems:root-main-measurements#all-pv-power-sum" }
Number:Dimensionless   HagerFlow_BatterySoc           "Battery Charge" { channel="modbus:hager-flow-ems:flow:ems:root-main-measurements#battery-state-of-charge" }
Number                 HagerFlow_EvcsCount            "Connected Charging Stations" { channel="modbus:hager-flow-ems:flow:ems:general-information#evcs-count" }
Switch                 HagerFlow_Evcs1Connected       "EVCS 1 Connected" { channel="modbus:hager-flow-ems:flow:ems:general-information#evcs-list-evcs-1" }
Switch                 HagerFlow_BatteryFirst         "Battery First" { channel="modbus:hager-flow-ems:flow:ems:general-information#sun-power-priority-battery-first" }
String                 HagerFlow_BlackoutState        "Blackout State" { channel="modbus:hager-flow-ems:flow:ems:general-information#blackout-active" }
String                 HagerFlow_EvseName             "Charging Station Name" { channel="modbus:hager-flow-evse:flow:ems:evse1:identity#evcs-name" }
Switch                 HagerFlow_EvseConnected        "Charging Station Connected" { channel="modbus:hager-flow-evse:flow:ems:evse1:general-status-and-configuration#evcs-connection-state" }
Switch                 HagerFlow_EvseBoostMode        "Charging Station Boost Mode" { channel="modbus:hager-flow-evse:flow:ems:evse1:general-status-and-configuration#evcs-boost-mode" }
Number:Power           HagerFlow_EvseChargingPower    "Charging Power" { channel="modbus:hager-flow-evse:flow:ems:evse1:instantaneous-measures#charging-power-total" }
Number:Energy          HagerFlow_EvseSessionEnergy    "Energy Of The Session" { channel="modbus:hager-flow-evse:flow:ems:evse1:charging-session-information#energy-session" }
Number:Dimensionless   HagerFlow_StorageSoc           "Storage Charge" { channel="modbus:hager-flow-storage:flow:ems:storage:status#battery-state-of-charge" }
Number:Power           HagerFlow_MeterPower           "Meter Power" { channel="modbus:hager-flow-powermeter:flow:ems:meter1:measurements#powermeter-power-sum" }
Number:Energy          HagerFlow_MeterEnergy          "Meter Energy" { channel="modbus:hager-flow-powermeter:flow:ems:meter1:measurements#powermeter-energy-sum" }
String                 HagerFlow_SgReadyState         "SG-Ready State" { channel="modbus:hager-flow-sgready:flow:ems:sgready1:status#sgready-status" }
```

The channel UID is built from the binding, the Thing type, the bridge chain and the channel group:
`modbus:<thingTypeId>:<modbusBridgeId>:<emsThingId>:<thingId>:<groupId>#<channelId>`
for the subordinate roles and `modbus:hager-flow-ems:<modbusBridgeId>:<emsThingId>:<groupId>#<channelId>`
for the energy manager itself.

## Known Limitations

The following parts of the Hager Flow Modbus table are not provided as channels:

- `status` (register `4945`, charging session information) packs the `ALG Data` byte structure of Annex 1 of
  the Modbus table.
  Its sub fields are not documented in the table itself, so the register is not decoded.
- `EVCS Charge Strategy daytime` and `EVCS Charge Strategy Minimum Energy Value` (registers `4634` and `4698`).
  The Modbus table declares a length of 64 words for seven weekday values, which does not divide evenly.
  A correct transcription needs clarification by the vendor.
- The `Modbus Wallbox specific` registers (MAC address, IP address, gateway, subnet, DHCP, RFID, register
  `5376` onwards) hold network and setup data and are not part of this binding.
- The `Slave ID identity` registers (type and index of the Modbus unit) are only provided by the energy
  manager Thing.
  The same registers exist for every other unit id and can be read there with the Modbus binding directly.
- `SGReady working` (register `4151`) is declared as `bool` in the Modbus table but documents the four
  states `0`, `-1`, `-2` and `-3`.
  The register is provided as a `Switch`, so `0` is `OFF` and every other value is `ON`.
  The detailed state is not part of the channel.

The Root/Main power values (registers `4102`, `4104`, `4106` and `4108`) are declared as `U32` in the Modbus
table, but they are read as `I32` because they can become negative.
The vendor was notified about the incorrect documentation, see
[openhab-addons issue 18711](https://github.com/openhab/openhab-addons/issues/18711).

The register addresses of the `Sun Power priority` register (`513`) were inferred from the address range
around it, because the address column of that row is not contained in the text layer of the source document.

## Source

The register addresses, data types and value ranges are transcribed from
`doc/01.Modbus.table.Flow.pdf`, Hager Flow XEM470 Modbus table, document version `FR2_2024_03`.
