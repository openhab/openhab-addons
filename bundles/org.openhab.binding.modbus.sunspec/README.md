# Modbus: SunSpec Bundle

This bundle is an extension for the Modbus binding to support the SunSpec protocol.

SunSpec is a format for inverters and smart meters to communicate over the Modbus protocol.
It defines how common parameters like AC/DC voltage and current, lifetime produced energy, device temperature etc can be read from the device.

SunSpec is supported by several manufacturers like ABB, Fronius, LG, SMA, SolarEdge, Schneider Electric.
For a list of certified products see this page: https://sunspec.org/sunspec-certified-products/

# IMPORTANT: under merge

** IMPORTANT: this version of this bundle is being merged into openHAB. This will be done in small steps - this means that not everything in this readme is supported at the moment **

Currently supported features are:

* basic values of single phase inverters without auto-discovery

 For the complete version of this bundle please contact me!


## Supported Things

This bundle adds the following thing types to the Modbus binding.
Note, that the things will show up under the Modbus binding.

| Thing                 | Description                                                           |
|-----------------------|-----------------------------------------------------------------------|
| inverter-single-phase | For simple, single phase inverters                                    |
| inverter-split-phase  | Split phase inverters (Japanese grid and 240V grid in North America)  |
| inverter-three-phase  | Three phase inverters                                                 |
| meter-single-phase    | Single phase meters (AN or AB)                                        |
| meter-split-phase     | Split single phase meters (ABN)                                       |
| meter-wye-phase       | Wye connected three phase meters (ABCN)                               |
| meter-delta-phase     | Delta connected three phase meters (ABC)                              |



## Binding Configuration

This bundle requires the openHAB 2 compatible Modbus binding to be installed.
Please refer to the Modbus binding configuration.
This addon does not require any additional configuration.

## Thing Configuration

You need first to set up either a TCP or a Serial Modbus bridge according to the Modbus documentation.
Things in this bundle will use the selected bridge to connect to the device.

The preferred way to add new things is by using the discovery feature.
This way the bundle will automatically detect if the Modbus bridge supports the SunSpec protocol and if so what type of models are available.
It will automatically detect the register addresses for each model.

### Auto discovering things

This bingind fully supports modbus auto discovery, that means all supported profiles should appear in the inbox once you connect your device.

Auto discovery is turned off by default in the modbus binding so you have to enable it manually.

You can add `enableDiscovery=true` attribute to your bridge config, or you can enable it in the paper ui under the modbus tcp|serial slave thing.

A typical bridge configuration would looke like this:

```
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1, enableDiscovery=true ]
```

### Adding things manually

If you decide to add a thing manually then first you have to find out the start address of the model block and the length of it.
While the length is usually fixed the address isn't.
Please refer to your device's vendor documentation how model blocks are laid for your equipment.

The following parameters are valid for all thing types:

| Parameter | Type    | Required | Default if ommitted | Description                             |
|-----------|---------|----------|---------------------|-----------------------------------------|
| address   | integer | yes      | N/A                 | Start address of the model block.       |
| length    | integer | yes      | N/A                 | Length of the model block. Setting this too short could cause problems during parsing |
| refresh   | integer | no       | 5                   | Poll inteval in seconds. Increase this if you encounter connection errors |
| maxTries  | integer | no       | 3                   | Number of retries when before giving up reading from this thing. |


## Channels

Channels are grouped into channel groups.
Different things support a subset of the following groups.

### Device information group (deviceInformation)

This group contains general operational information about the device.

| Channel ID              | Item Type             | Description                                                                        |
|-------------------------|-----------------------|------------------------------------------------------------------------------------|
| cabinet-temperature     | Number:Temperature    | Temperature of the cabinet if supported in Celsius                                 |
| heatsink-temperature    | Number:Temperature    | Device heat sink temperature in Celsius                                            |
| transformer-temperature | Number:Temperature    | Temperature of the transformer in Celsius                                          |
| other-temperature       | Number:Temperature    | Any other temperature reading not covered by the above items if available. Celsius |
| status                  | String                | Device status: OFF=Off, SLEEP=Sleeping/night mode, ON=On - producing power         |

Supported by: all inverter things

### AC summary group (acGeneral)

#### inverters

This group contains summarized values for the AC side of the inverter.
Even if the inverter supports multiple phases this group will appear only once.

| Channel ID           | Item Type              | Description                                                         |
|----------------------|------------------------|---------------------------------------------------------------------|
| ac-total-current     | Number:ElectricCurrent | Total AC current over all phases in Amperes                         |
| ac-power             | Number:Power           | Actual AC power over all phases in Watts                            |
| ac-frequency         | Number:Frequency       | Actual grid frequency                                               |
| ac-apparent-power    | Number:Power           | Actual AC apparent power                                            |
| ac-reactive-power    | Number:Power           | Actual AC reactive power                                            |
| ac-power-factor      | Number:Dimensionless   | Actual AC power factor (%)                                          |
| ac-lifetime-energy   | Number:Energy          | AC lifetime energy production for this device in WattHours          |

Supported by: all inverter things


#### meters

This group contains summarized values for the power meter over all phases.

| Channel ID                           | Item Type                | Description                                                         |
|--------------------------------------|--------------------------|---------------------------------------------------------------------|
| ac-total-current                     | Number:ElectricCurrent   | Total AC current over all phases in Amperes                         |
| ac-average-voltage-to-n              | Number:ElectricPotential | Average Line to Neutral AC Voltage over all phases                  |
| ac-average-voltage-to-next           | Number:ElectricPotential | Average Line to Line AC Voltage  over all phases                    |
| ac-frequency                         | Number:Frequency         | Actual grid frequency                                               |
| ac-total-real-power                  | Number:Power             | Total Real Power over all phases(W)                                 |
| ac-total-apparent-power              | Number:Power             | Total Apparent Power over all phases (W)                            |
| ac-total-reactive-power              | Number:Power             | Total Reactive Power over all phases (W)                            |
| ac-average-power-factor              | Number:Dimensionless     | Average AC Power Factor over all phases (%)                         |
| ac-total-exported-real-energy        | Number:Energy            | Total Real Energy Exported over all phases (Wh)                     |
| ac-total-imported-real-energy        | Number:Energy            | Total Real Energy Imported  over all phases (Wh)                    |
| ac-total-exported-apparent-energy    | Number:Energy            | Total Apparent Energy Exported over all phases (VAh)                |
| ac-total-imported-apparent-energy    | Number:Energy            | Total Apparent Energy Imported over all phases (VAh)                |
| ac-total-imported-reactive-energy-q1 | Number:Energy            | Total Reactive Energy Imported Quadrant 1 over all phases (VARh)    |
| ac-total-imported-reactive-energy-q2 | Number:Energy            | Total Reactive Energy Imported Quadrant 2 over all phases (VARh)    |
| ac-total-exported-reactive-energy-q3 | Number:Energy            | Total Reactive Energy Exported Quadrant 3 over all phases (VARh)    |
| ac-total-exported-reactive-energy-q4 | Number:Energy            | Total Reactive Energy Exported Quadrant 4 over all phases (VARh)    |

Supported by: all meter things


### AC phase specific group

#### inverters

This group describes values for a single phase of the inverter.
There can be a maximum of three of this group named:

acPhaseA: available for all inverter types

acPhaseB: available for inverter-slit-phase and inverter-three-phase type inverters

acPhaseC: available only for inverter-three-phase type inverters.

| Channel ID           | Item Type                | Description                                                         |
|----------------------|--------------------------|---------------------------------------------------------------------|
| ac-phase-current     | Number:ElectricCurrent   | Actual current over this phase in Watts                             |
| ac-voltage-to-next   | Number:ElectricPotential | Voltage of this phase relative to the next phase, or to the ground in case of single phase inverter. Note: some single phase SolarEdge inverters incorrectly use this value to report the voltage to neutral value|
| ac-voltage-to-n      | Number:ElectricPotential | Voltage of this phase relative to the ground                        |

Supported by: all inverter things

#### meters

This group holds values for a given line of the meter.
There can be a maximum of three of this group named:

acPhaseA: available for all meter types

acPhaseB: available for meter-split-phase, meter-wye-phase and meter-delta-phase meters

acPhaseC: available only for meter-wye-phase and meter-delta-phase meters type inverters.


| Channel ID                     | Item Type                | Description                                                         |
|--------------------------------|--------------------------|---------------------------------------------------------------------|
| ac-phase-current               | Number:ElectricCurrent   | Actual current over this line in Watts                              |
| ac-voltage-to-n                | Number:ElectricPotential | Voltage of this line relative to the neutral line                   |
| ac-voltage-to-next             | Number:ElectricPotential | Voltage of this line relative to the next line                      |
| ac-real-power                  | Number:Power             | AC Real Power value (W)                                             |
| ac-apparent-power              | Number:Power             | AC Apparent Power value                                             |
| ac-reactive-power              | Number:Power             | AC Reactive Power value                                             |
| ac-power-factor                | Number:Dimensionless     | AC Power Factor (%)                                                 |
| ac-exported-real-energy        | Number:Energy            | Real Energy Exported (Wh                                            |
| ac-imported-real-energy        | Number:Energy            | Real Energy Imported (Wh)                                           |
| ac-exported-apparent-energy    | Number:Energy            | Apparent Energy Exported (VAh)                                      |
| ac-imported-apparent-energy    | Number:Energy            | Apparent Energy Imported (VAh)                                      |
| ac-imported-reactive-energy-q1 | Number:Energy            | Reactive Energy Imported Quadrant 1 (VARh)                          |
| ac-imported-reactive-energy-q2 | Number:Energy            | Reactive Energy Imported Quadrant 2 (VARh)                          |
| ac-exported-reactive-energy-q3 | Number:Energy            | Reactive Energy Exported Quadrant 3 (VARh)                          |
| ac-exported-reactive-energy-q4 | Number:Energy            | Reactive Energy Exported Quadrant 4 (VARh)                          |


Supported by: all meter things

### DC general group

This group contains summarized data for the DC side of the inverter.
DC information is summarized even if the inverter has multiple strings.

| Channel ID           | Item Type                | Description                                                         |
|----------------------|--------------------------|---------------------------------------------------------------------|
| dc-current           | Number:ElectricCurrent   | Actual DC current in Amperes                                        |
| dc-voltage           | Number:ElectricPotential | Actual DC voltage                                                   |
| dc-power             | Number:Power             | Actual DC power produced                                            |

Supported by: all inverter things


## Full Example

To configure a SunSpec inverter you have to set up a Modbus bridge with the connection parameters.
The Modbus binding supports both TCP and Serial connections please choose the one that's appropriate for you.
Please enable discovery on the bridge.

Textual configuration is optional, you can set up everything using PaperUI.
After adding the Modbus bridge and enabling discovery a scan will be initiated and if the device supports SunSpec then the known models will be added to the inbox with correct address configuration.

### Thing Configuration

The preferred way to add a SunSpec compatible Thing is through auto-discovery.
Whoever if the auto-discovery would not work, advanced users could set up the thing through the config file.

Please note that the nested bridge configuration does not work at the moment.
Use the following flat format to set up the bridge and the inverter thing:

```
Bridge modbus:tcp:bridge [ host="hostname|ip", port=502, id=1, enableDiscovery=true ]
Thing modbus:inverter-single-phase:bridge:se4000h "SE4000h" (modbus:tcp:modbusbridge) [ address=40069, length=52, refresh=15 ]
```

Note: make sure that refresh, port and id values are numerical, without quotes.

### Item Configuration

```
Number Inverter_Temperature "Temperature [%.1f C]"  {channel="modbus:inverter-single-phase:bridge:se4000h:deviceInformation#heatsink-temperature"}

Number Inverter_AC_Power "AC Power [%d W]" {channel="modbus:inverter-single-phase:bridge:se4000h:acGeneral#ac-power"}

Number Inverter_AC1_A "AC Current Phase 1 [%0.2f A]" {channel="modbus:inverter-single-phase:bridge:se4000h:acPhaseA#ac-phase-current"}

```

### Sitemap Configuration

```
                        Text item=Inverter_Temperature
                        Text item=Inverter_AC_Current
                        Text item=Inverter_AC_Power
                        Chart item=Inverter_Temperature period=D refresh=600000
                        Chart item=Inverter_AC_Power period=D refresh=30000

```

## Vendor specific information

### SolarEdge

Newer models of SolarEdge inverters can be monitored over TCP, but you need to enable support in the inverter first.
Refer to the "Modbus over TCP Configuration" chapter in this documentation: https://www.solaredge.com/sites/default/files/sunspec-implementation-technical-note.pdf

Modbus connection is limited to a single client at a time, so make sure no other clients are using the port.

## For Developers

SunSpec is a big specification with many different type of devices.
If you own or have access to an appliance that is not supported at the moment then your help is welcome.

If you want to extend the bundle yourself, you have to do the followings:

 - Define your thing type, channel types and channel groups according to openHAB development practices.
 You can look at the meter and inverter types to get ideas how you can avoid repeating the same configuration over and over.
 
 - Extend the `AbstractSunSpecHandler` and implement the handlePolledData method.
 This method will be regularly called with the register data read from the appliance.
 The method should parse the data and update the channels with them.

 - The preferred way to parse the raw data is to write a parser for you model block type.
 Your class should implement the `SunspecParser` class and it is preferred to extend the `AbstractBaseParser` class.
 This base class has methods to accurately extract fields from the register array.

 - The parser should only retrieve the data from the register array and return them in a block descriptor class.
 Scaling and other higher level transformation should be done by the handler itself.
 
 - To include your block type in auto discovery you have to add its id to the `SUPPORTED_THING_TYPES_UIDS` map in `SunSpecConstants`. This is enough for our discovery process to include your thing type in the results.


If you have questions or need help don't hesitate to contact us over the OpenHAB community forums and github pages.
 
