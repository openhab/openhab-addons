# Sungrow

This extension adds support for Sungrow hybrid inverters. These hybrid inverters allow direct connection of a storage battery.

## Supported Things

This extension has been tested with an SH10RT inverter but should at least other inverters of SH*RT series.

### Auto Discovery

This extension fully supports modbus auto discovery.

Auto discovery is turned off by default in the modbus binding so you have to enable it manually.

You can set the `enableDiscovery=true` parameter in your bridge.

A typical bridge configuration would look like this:

```
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1, enableDiscovery=true ]
```

## Thing Configuration

You need first to set up a TCP Modbus bridge according to the Modbus documentation.
Things in this extension will use the selected bridge to connect to the device.

The thing supports the following parameters:

| Parameter | Type    | Required | Default if omitted  | Description                                                                |
|-----------|---------|----------|---------------------|----------------------------------------------------------------------------|
| refresh   | integer | no       | 5                   | Poll interval in seconds. Increase this if you encounter connection errors |
| maxTries  | integer | no       | 3                   | Number of retries when before giving up reading from this thing. |

## Channels

Channels are grouped into channel groups.
Different things support a subset of the following groups.

### Device Information Group (deviceInformation)

This group contains general operational information about the device.

| Channel ID           | Item Type          | Description                                                     |
|----------------------|--------------------|-----------------------------------------------------------------|
| device-type          | String             | Sungrow inverter type name                                      |
| output-type          | String             | Output configuration of the inverter (single phase, 3P3L, 3P4L) |
| nominal-output-power | Number:Power       | Nominal output power of the inverter                            |
| inside-temperature   | Number:Temperature | Temperature inside the inverter in Celsius                      |
| system-state         | String             | Current system state of the device                              |

#### System States

Possible system states are Stop, Standby, Initial standby, Startup, Running, Fault, Running in maintain mode, Running in forced mode, Running in off-grid mode, Restarting, Running in External EMS mode.

### AC Summary Group (acGeneral)

This group contains summarized values for the AC side of the inverter.
Even if the inverter supports multiple phases this group will appear only once.

| Channel ID      | Item Type            | Description                              |
|-----------------|----------------------|------------------------------------------|
| ac-grid-state   | String               | Current grid state (On-Grid or Off-Grid) |
| ac-frequency    | Number:Frequency     | Actual grid frequency                    |
| ac-power-factor | Number:Dimensionless | Actual AC power factor (%)               |

### AC Phase Specific Group

This group describes values for a single phase of the inverter.
There can be a maximum of three of this group named:

- **acPhaseA**: available for all inverter types
- **acPhaseB**: available for slit-phase and three-phase inverters types
- **acPhaseC**: available for three-phase inverter types

| Channel ID           | Item Type                | Description                                      |
|----------------------|--------------------------|--------------------------------------------------|
| ac-phase-current     | Number:ElectricCurrent   | Actual current over this phase in Ampere         |
| ac-voltage-to-n      | Number:ElectricPotential | Voltage of this phase relative to the ground     |
| ac-voltage-to-next   | Number:ElectricPotential | Voltage of this phase relative to the next phase |

### Power Information Group

This group contains to power flow in supplied system.

| Channel ID     | Item Type    | Description                                  |
|----------------|--------------|----------------------------------------------|
| dc-total-power | Number:Power | Totel Power sourced by the PV moduls         |
| load-power     | Number:Power | Actual power conumption of the supplied load |
| grid-power     | Number:Power | Actual power imported or exported to the grid: <ul><li><b>positive value:</b> imported power</li><li><b>negative value:</b> exported power</li></ul> |

### MPPT Group

This group contains summarized data for the different strings of the inverter.
There are two instances of this group named:

- **mppt1**: first string
- **mppt2**: second string

| Channel ID | Item Type                | Description                                                                                                             |
|------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------|
| dc-current | Number:ElectricCurrent   | Actual DC current in Amperes                                                                                            |
| dc-voltage | Number:ElectricPotential | Actual DC voltage                                                                                                       |
| dc-power   | Number:Power             | Actual DC power produced (this value is not provided by the inverter; it is calculated from voltage and current values) |

### Battery Information Group

This group contains summerized data for the battery system.

| Channel ID          | Item Type                | Description                                      |
|---------------------|--------------------------|--------------------------------------------------|
| battery-current     | Number:ElectricCurrent   | Actual battery current in Amperes                |
| battery-voltage     | Number:ElectricPotential | Actual voltage of the battery                    |
| battery-power       | Number:Power             | Actual power charged or discharged to the battery: <ul><li><b>positive value:</b> discharge power</li><li><b>negative value:</b> charge power</li></ul> |
| battery-level       | Number:Dimensionless     | Actual chargelevel of the battery in percent     |
| battery-health      | Number:Dimensionless     | Actual state of health of the battery in percent |
| battery-temperature | Number:Temperature       | Actual temperature of the battery in Celsius     |
