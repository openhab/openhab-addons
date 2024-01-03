# Modbus Sungrow Binding

This binding integrates the sungrow inverters into openHAB.
It is based on the Sungrow specification "Communication Protocol of Residential Hybrid Inverter V1.0.23",
which can be found here: https://github.com/bohdan-s/SunGather/issues/36.

## Supported inverters

As defined within the spec mentioned above the following inverters are supported, but not all are tested yet:

- SH3K6
- SH4K6
- SH5K-20
- SH5K-V13
- SH3K6-30
- SH4K6-30
- SH5K-30
- SH3.0RS
- SH3.6RS
- SH4.0RS
- SH5.0RS
- SH6.0RS
- SH5.0RT
- SH6.0RT
- SH8.0RT
- SH10RT

## Supported Things

The binding supports only one thing:

- `sungrowInverter`: The sungrow inverter

## Preparation

The data from the inverter is read via Modbus. So you need to configure a Modbus Serial Slave `serial` or Modbus TCP Slave `tcp` as bridge first.
If you are using a Modbus TCP Slave and the WiNet-S Communication Module please ensure:

- that you have the correct IP-Address of your WiNet-S Device
- that Modbus is enabled within the Communication Module
- that you've the correct port number
- that the white list is disabled or your openHAB instance IP is listed

Enabling modbus and whitelist setting can be done in WiNet-S Web-UI as shown below:
<img src="./doc/WiNet-S_Modbus.PNG" alt="WiNet-S Modbus configuration"/>

## Thing Configuration

Once you've configured the Modbus TCP Slave or Modbus Serial Slave as Bridge you can configure the Sungrow inverter thing.
You just have to select the configured bridge and optional configure the polling interval. 

### `sungrowInverter` Thing Configuration

| Name         | Type    | Description                          | Default | Required | Advanced |
|--------------|---------|--------------------------------------|---------|----------|----------|
| pollInterval | integer | Interval the device is polled in ms. | 5000    | yes      | no       |

## Channels

The `sungrowInverter` thing has channels that serve the current state of the sungrow inverter,
as you are used to from the iSolareCloud Website and App.

## Configuration Example

This example shows how to configure a sungrow inverter connected via modbus and uses the most common channels.

sungrow.things

```java
Bridge modbus:tcp:sungrowBridge [ host="10.0.0.2", port=502, id=1, enableDiscovery=false ] {
    Thing sungrow-inverter sungrowInverter "Sungrow Inverter" [ pollInterval=5000 ]
}
```

sungrow.items

```java
// Groups
Group sungrowInverter "Sungrow Inverter" ["Inverter"]
Group overview "Overview" (sungrowInverter)
Group batteryInformation "Battery information" (sungrowInverter)
Group gridInformation "Grid information" (sungrowInverter)
Group loadInformation "Load information" (sungrowInverter)

// Overview
Number:Power total_active_power "Total Active Power" (overview) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-overview#sg-total-active-power"}
Number:Power total_dc_power "Total DC Power" (overview) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-overview#sg-total-dc-power"}
Number:Energy daily_pv_generation "Daily PV Generation" (overview) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-overview#sg-daily-pv-generation"}
Number:Energy total_pv_generation "Total PV Generation" (overview) ["Measurement", "Energy"]  {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-overview#sg-total-pv-generation"}

// Battery information
Number:Power battery_power "Battery Power" (batteryInformation) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-battery-information#sg-battery-power"}
Number:Dimensionless battery_level "Battery Level" (batteryInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-battery-information#sg-battery-power", stateDescription=""[pattern="%.1f W"]}
Number:Energy daily_charge_energy "Daily Battery Charge Energy" (batteryInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-battery-information#sg-daily-charge-energy"}
Number:Energy daily_discharge_energy "Daily Battery Discharge Energy" (batteryInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-battery-information#sg-daily-battery-discharge-energy"}

// Grid information
Number:Power export_power "Export Power" (gridInformation) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-grid-information#sg-export-power"}
Number:Energy daily_export_energy "Daily Export Energy" (gridInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-grid-information#sg-daily-export-energy"}

// Load information
Number:Power load_power "Load Power" (loadInformation) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-load-information#sg-load-power"}
Number:Energy daily_direct_energy_consumption "Daily Direct Energy Consumption" (loadInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-load-information#sg-daily-direct-energy-consumption"}```
