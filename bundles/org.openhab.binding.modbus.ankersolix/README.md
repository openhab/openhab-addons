# Modbus Anker Solix Binding

This binding adds Anker SOLIX device support as a Modbus sub-binding.
It currently supports Anker SOLIX devices with officially exposed Modbus profiles.

## Prerequisites

This add-on is a Modbus sub-binding and requires the base Modbus binding to be installed and active.
It is not a standalone binding.

When installed via the normal openHAB add-on workflow, dependencies are handled by openHAB.
When manually side-loading a jar from `addons/`, make sure a matching Modbus binding version is present in the runtime.

## Supported Things

- `ankersolix-solarbank4`: Anker SOLIX Solarbank 4 E5000 Pro.
- `ankersolix-solarbank-ac`: Anker SOLIX Solarbank Max AC / XE AC.
- `ankersolix-smartmeter-gen2`: Anker SOLIX Smart Meter Gen 2.
- `ankersolix-smartplug`: Anker SOLIX Smart Plug.
- `ankersolix-ev-charger`: Anker SOLIX V1 Smart EV Charger.

The thing is attached to an existing Modbus `tcp` or `serial` bridge.
Only devices with officially published Modbus profiles are supported; reserved and undocumented endpoints are intentionally excluded.

## Discovery

Discovery is available for all supported Anker SOLIX thing types.

Discovery runs on an existing Modbus `tcp` or `serial` bridge and probes the configured endpoint using read-only
register requests.

The bridge itself is still configured manually. Discovery then adds the matching child thing in the Inbox.

## Thing Configuration

| Name                           | Type    | Description                                  | Default | Required | Advanced |
|--------------------------------|---------|----------------------------------------------|---------|----------|----------|
| pollInterval                   | integer | Poll interval in milliseconds (ms)           | 5000    | yes      | no       |
| maxTries                       | integer | Maximum tries for read/write requests        | 3       | no       | yes      |
| writeProtectionDurationSeconds | integer | Shadow-state duration after successful write | 15      | no       | yes      |
| autoThirdPartyControl          | boolean | Switch Solarbank into third party control on connect | true | no   | yes      |

`pollInterval` is intentionally configured in milliseconds.
For example, `5000` means 5 seconds and the minimum value is `500` (0.5 seconds).

When `autoThirdPartyControl` is enabled (default), Solarbank things automatically switch into `third_party_control`
operating mode on connect. This is required for `battery-power-setpoint` and `battery-power-direction` writes to take
effect. Set it to `false` if you want to manage the operating mode yourself. The setting is ignored for Smart Meter and
Smart Plug and EV Charger things.

## Bridge Configuration

`host`, `port`, and Modbus transport settings are configured on the parent Modbus bridge (`tcp` or `serial`),
not on the child Anker SOLIX thing itself.

The Anker SOLIX thing inherits connection settings from that bridge and only provides device-specific parameters.

This still allows one endpoint per device: create one Modbus bridge per device (or gateway endpoint), each with its own
host/port settings.

### Example With Multiple Devices

```things
Bridge modbus:tcp:solixA [ host="192.168.1.41", port=502, id=1 ] {
    Thing ankersolix-solarbank4 deviceA [ pollInterval=5000 ]
}

Bridge modbus:tcp:solixB [ host="192.168.1.42", port=502, id=1 ] {
    Thing ankersolix-solarbank4 deviceB [ pollInterval=5000 ]
}
```

In other words, connection parameters are bridge-level and can still be unique per device.

### UI Setup (Short Version)

1. Add a Modbus `tcp` (or `serial`) bridge and set `host`/`port` for one device endpoint.
1. Run discovery from Inbox and add the detected Anker SOLIX child thing.
1. If discovery does not detect your device, add the Anker SOLIX thing (`ankersolix-solarbank4`, `ankersolix-solarbank-ac`, `ankersolix-smartmeter-gen2`, `ankersolix-smartplug`, or `ankersolix-ev-charger`) manually as child of that bridge.
1. Repeat with a second bridge if you want to connect a second device endpoint.

## Channels

The `ankersolix-solarbank4` and `ankersolix-solarbank-ac` things expose the following channels:

| Channel ID                      | Item Type            | Access      | Description |
|---------------------------------|----------------------|-------------|-------------|
| `device-model`                  | `String`             | read-only   | Device model string. |
| `device-serial-number`          | `String`             | read-only   | Device serial number. |
| `device-sw-version`             | `String`             | read-only   | Device firmware version. |
| `battery-soc`                   | `Number:Dimensionless` | read-only | Battery state of charge in percent. |
| `pv-power`                      | `Number:Power`       | read-only   | Total PV input power. |
| `battery-charging-power`        | `Number:Power`       | read-only   | Battery charging power (non-negative). |
| `battery-discharging-power`     | `Number:Power`       | read-only   | Battery discharging power (non-negative). |
| `load-power`                    | `Number:Power`       | read-only   | Current load power. |
| `grid-import-power`             | `Number:Power`       | read-only   | Grid import power (non-negative). |
| `grid-export-power`             | `Number:Power`       | read-only   | Grid export power (non-negative). |
| `ac-grid-output-power`          | `Number:Power`       | read-only   | AC grid output power. |
| `pv-total-generation`           | `Number:Energy`      | read-only   | Total PV generation (`kWh`). |
| `cumulative-charge-energy`      | `Number:Energy`      | read-only   | Cumulative charge energy (`kWh`). |
| `cumulative-discharge-energy`   | `Number:Energy`      | read-only   | Cumulative discharge energy (`kWh`). |
| `operating-mode`                | `String`             | read-write  | Operating mode. Supported command values: `self_consumption`, `tou_mode`, `third_party_control`, `custom_mode`, `socket_overlay_mode`, `smart_mode`, `dynamic_pricing`. |
| `battery-power-direction`       | `String`             | read-write  | Direction for setpoint control. Supported values: `charge`, `discharge`. |
| `battery-power-setpoint`        | `Number:Power`       | read-write  | Battery power setpoint in `W` (absolute value, `100`-`10000`). Sign sent to device is derived from `battery-power-direction`. |

### EV Charger Channels

| Channel ID | Item Type | Access | Description |
|------------|-----------|--------|-------------|
| `product-number` | `Number` | read-only | Product number register value. |
| `device-model` | `String` | read-only | Model name. |
| `device-serial-number` | `String` | read-only | Serial number. |
| `device-sw-version` | `String` | read-only | Software version. |
| `device-hw-version` | `String` | read-only | Hardware version. |
| `rated-power` | `Number:Power` | read-only | Rated power (`Pn`). |
| `minimum-output-current` | `Number:ElectricCurrent` | read-only | Minimum output current. |
| `maximum-output-current` | `Number:ElectricCurrent` | read-only | Maximum output current. |
| `alarm-information-1` ... `alarm-information-12` | `Number` | read-only | Raw alarm words (each bit represents an alarm). |
| `l1-n-voltage` | `Number:ElectricPotential` | read-only | L1-N voltage. |
| `l2-n-voltage` | `Number:ElectricPotential` | read-only | L2-N voltage. |
| `l3-n-voltage` | `Number:ElectricPotential` | read-only | L3-N voltage. |
| `l1-l2-voltage` | `Number:ElectricPotential` | read-only | L1-L2 voltage. |
| `l2-l3-voltage` | `Number:ElectricPotential` | read-only | L2-L3 voltage. |
| `l3-l1-voltage` | `Number:ElectricPotential` | read-only | L3-L1 voltage. |
| `l1-current` | `Number:ElectricCurrent` | read-only | L1 current. |
| `l2-current` | `Number:ElectricCurrent` | read-only | L2 current. |
| `l3-current` | `Number:ElectricCurrent` | read-only | L3 current. |
| `l1-active-power` | `Number:Power` | read-only | L1 active power. |
| `l2-active-power` | `Number:Power` | read-only | L2 active power. |
| `l3-active-power` | `Number:Power` | read-only | L3 active power. |
| `total-charging-active-power` | `Number:Power` | read-only | Total charging active power. |
| `l1-reactive-power` | `Number:Power` | read-only | L1 reactive power. |
| `l2-reactive-power` | `Number:Power` | read-only | L2 reactive power. |
| `l3-reactive-power` | `Number:Power` | read-only | L3 reactive power. |
| `l1-apparent-power` | `Number:Power` | read-only | L1 apparent power. |
| `l2-apparent-power` | `Number:Power` | read-only | L2 apparent power. |
| `l3-apparent-power` | `Number:Power` | read-only | L3 apparent power. |
| `current-charging-session-duration` | `Number:Time` | read-only | Current charging session duration (`s`). |
| `current-charging-capacity` | `Number:Energy` | read-only | Current charging capacity (`Wh`). |
| `pwm-enabled-status` | `String` | read-only | PWM enabled status (`disabled`, `enabled`). |
| `single-three-phase-operating-mode` | `String` | read-only | Single/three-phase operating mode (`single_phase`, `three_phase`). |
| `charging-mode` | `String` | read-only | Charging mode (`solar_plus_grid`, `only_solar`). |
| `load-balancing-enabled-status` | `String` | read-only | Load balancing enabled status (`disabled`, `enabled`). |
| `solar-power-balancing-enabled-status` | `String` | read-only | Solar power balancing enabled status (`disabled`, `enabled`). |
| `cp-acquisition-voltage` | `Number` | read-only | CP acquisition voltage raw value. |
| `cp-signal-status` | `String` | read-only | CP signal status enum value. |
| `relay-1-temperature` | `Number:Temperature` | read-only | Relay 1 temperature. |
| `relay-2-temperature` | `Number:Temperature` | read-only | Relay 2 temperature. |
| `boost-mode` | `String` | read-only | Boost mode status (`disabled`, `enabled`). |
| `led-light-brightness` | `Number:Dimensionless` | read-only | LED light brightness (`%`). |
| `charging-status` | `String` | read-only | Charging status enum value. |
| `ocpp-connection-status` | `String` | read-only | OCPP connection status (`not_connected`, `connecting`, `connected`). |
| `mqtt-connection-status` | `String` | read-only | MQTT connection status (`not_connected`, `connected`). |
| `charging-command` | `String` | read-write | Charging command values: `start_charging`, `stop_charging`. |
| `maximum-current-setting` | `Number:ElectricCurrent` | read-write | Maximum current setting (scaled from Modbus gain 10). |
| `boost-mode-command` | `Switch` | read-write | One-shot boost mode command. |
| `set-timeout` | `Number:Time` | read-write | Timeout setting, must be `> 5 s`. |
| `set-number-of-charging-phases` | `String` | read-write | Phase mode values: `default`, `fixed_single_phase`, `fixed_three_phase`. |

The Modbus PDF references an external alarm list for bit-level alarm decoding, but this list is currently not published.
Therefore the binding exposes the 12 alarm registers as raw values for now.

Registers marked as `Reserved for Future Use` are intentionally not bound:

1. `20098` (RO)
1. `21004` (RW)

### Smart Meter Gen 2 Channels

| Channel ID | Item Type | Access | Description |
|------------|-----------|--------|-------------|
| `device-model` | `String` | read-only | Device model string. |
| `device-serial-number` | `String` | read-only | Device serial number. |
| `device-sw-version` | `String` | read-only | Device firmware version. |
| `meter-type` | `String` | read-only | Meter type (`single_phase` or `three_phase`). |
| `primary-total-active-power` | `Number:Power` | read-only | Primary CT total active power. |
| `primary-phase-1-active-power` | `Number:Power` | read-only | Primary CT phase 1 active power. |
| `primary-phase-1-current` | `Number:ElectricCurrent` | read-only | Primary CT phase 1 current. |
| `primary-phase-1-voltage` | `Number:ElectricPotential` | read-only | Primary CT phase 1 voltage. |
| `primary-phase-2-active-power` | `Number:Power` | read-only | Primary CT phase 2 active power. |
| `primary-phase-2-current` | `Number:ElectricCurrent` | read-only | Primary CT phase 2 current. |
| `primary-phase-2-voltage` | `Number:ElectricPotential` | read-only | Primary CT phase 2 voltage. |
| `primary-phase-3-active-power` | `Number:Power` | read-only | Primary CT phase 3 active power. |
| `primary-phase-3-current` | `Number:ElectricCurrent` | read-only | Primary CT phase 3 current. |
| `primary-phase-3-voltage` | `Number:ElectricPotential` | read-only | Primary CT phase 3 voltage. |
| `secondary-total-active-power` | `Number:Power` | read-only | Secondary CT total active power. |
| `secondary-phase-1-active-power` | `Number:Power` | read-only | Secondary CT phase 1 active power. |
| `secondary-phase-1-current` | `Number:ElectricCurrent` | read-only | Secondary CT phase 1 current. |
| `secondary-phase-1-voltage` | `Number:ElectricPotential` | read-only | Secondary CT phase 1 voltage. |
| `secondary-phase-2-active-power` | `Number:Power` | read-only | Secondary CT phase 2 active power. |
| `secondary-phase-2-current` | `Number:ElectricCurrent` | read-only | Secondary CT phase 2 current. |
| `secondary-phase-2-voltage` | `Number:ElectricPotential` | read-only | Secondary CT phase 2 voltage. |
| `secondary-phase-3-active-power` | `Number:Power` | read-only | Secondary CT phase 3 active power. |
| `secondary-phase-3-current` | `Number:ElectricCurrent` | read-only | Secondary CT phase 3 current. |
| `secondary-phase-3-voltage` | `Number:ElectricPotential` | read-only | Secondary CT phase 3 voltage. |

The `secondary-phase-*-voltage` channels intentionally report the same per-phase voltage as the `primary-phase-*-voltage` channels.
Both CT groups are connected to the same mains, so the line-to-neutral voltage per phase is physically identical, and the meter only exposes a single voltage measurement per phase.
This mirrors the upstream Home Assistant register map and is not a binding defect.

### Smart Plug Channels

| Channel ID | Item Type | Access | Description |
|------------|-----------|--------|-------------|
| `device-model` | `String` | read-only | Device model string. |
| `device-serial-number` | `String` | read-only | Device serial number. |
| `real-time-power` | `Number:Power` | read-only | Real-time power. |
| `voltage` | `Number:ElectricPotential` | read-only | Voltage. |
| `current` | `Number:ElectricCurrent` | read-only | Current. |
| `switch-status` | `String` | read-only | Switch status text (`connected` / `disconnected`). |
| `power-switch` | `Switch` | read-write | Switch command channel for socket power. |

### Channel Write Behavior

1. Solarbank write channels: `operating-mode`, `battery-power-direction`, and `battery-power-setpoint`.
1. Smart Plug write channel: `power-switch` (writes to control register, readback from status register).
1. EV Charger write channels: `charging-command`, `maximum-current-setting`, `boost-mode-command`, `set-timeout`, and `set-number-of-charging-phases`.
1. After successful writes, the binding keeps a temporary shadow state for `writeProtectionDurationSeconds` to avoid UI value flicker.
1. Sending `REFRESH` triggers an immediate one-time poll of all configured register ranges.

### Item Linking Examples

```items
Number:Dimensionless Solarbank_SoC "Solarbank SoC [%d %%]" { channel="modbus:ankersolix-solarbank4:solixA:deviceA:battery-soc" }
Number:Power Solarbank_PvPower "PV Power [%.0f %unit%]" { channel="modbus:ankersolix-solarbank4:solixA:deviceA:pv-power" }
String Solarbank_Mode "Operating Mode [%s]" { channel="modbus:ankersolix-solarbank4:solixA:deviceA:operating-mode" }
String Solarbank_Direction "Battery Direction [%s]" { channel="modbus:ankersolix-solarbank4:solixA:deviceA:battery-power-direction" }
Number:Power Solarbank_Setpoint "Battery Setpoint [%.0f %unit%]" { channel="modbus:ankersolix-solarbank4:solixA:deviceA:battery-power-setpoint" }
```

### Troubleshooting

1. Thing stays OFFLINE: Check that the parent Modbus bridge is ONLINE and that `host`, `port`, and slave `id` are correct.
1. No device appears in discovery inbox: Discovery requires an ONLINE parent Modbus bridge and valid endpoint/slave settings. Discovery only probes the currently configured endpoint and does not scan arbitrary hosts.
1. No values are updating: Verify network reachability to the bridge endpoint and increase `pollInterval` if the device cannot handle fast polling.
1. Commands appear to be ignored: Check that you are writing to supported write channels (`operating-mode`, `battery-power-direction`, `battery-power-setpoint`, `power-switch`, `charging-command`, `maximum-current-setting`, `boost-mode-command`, `set-timeout`, `set-number-of-charging-phases`).
1. Setpoint direction seems wrong: Set `battery-power-direction` first, then write `battery-power-setpoint`.
1. Setpoint or direction writes are ignored: Solarbank requires `third_party_control` operating mode. Keep `autoThirdPartyControl` enabled, or set `operating-mode` to `third_party_control` manually before writing.
1. Temporary UI value jumps: Tune `writeProtectionDurationSeconds` to keep write shadow values long enough until the next stable readback.
