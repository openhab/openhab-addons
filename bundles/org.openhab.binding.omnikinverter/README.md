# Omnik Inverter Binding

This binding reads metrics from Omnik Solar Inverters.

## Supported Things

All Omniksols are expected to function, provided they have the Wifi module. At
moment of writing the _Omniksol-3.0k-TL2_ has been tested.

## Discovery

No autodiscovery available

## Thing Configuration

| Config   | Description                                                                                                                    | type    | Default   |
| :------- | :------------                                                                                                                  | :-----  | :-------- |
| hostname | The hostname or ip through which the inverter can be accessed                                                                  | string  | n/a       |
| port     | TCP port through which the inverter listens on for incoming connections                                                        | integer | 8899      |
| serial   | The serial of the wifi module. The Wifi module's SSID contains the number. This is the numerical part only, i.e. without _AP__ | integer | n/a       |

## Channels

| Channel Type Id | Item Type                | Description                                                                                                      |
|:----------------|:-------------------------|:-----------------------------------------------------------------------------------------------------------------|
| power           | Number:Power             | The instantaneous power generation for feed 1 to the grid, in Watt by default (**deprecated**; same as powerAC1) |
| powerAC1        | Number:Power             | The instantaneous power generation for feed 1 to the grid, in Watt by default                                    |
| powerAC2        | Number:Power             | The instantaneous power generation for feed 2 to the grid, in Watt by default                                    |
| powerAC3        | Number:Power             | The instantaneous power generation for feed 3 to the grid, in Watt by default                                    |
| voltageAC1      | Number:ElectricPotential | The voltage for feed 1 to the grid, in Volt by default                                                           |
| voltageAC2      | Number:ElectricPotential | The voltage for feed 2 to the grid, in Volt by default                                                           |
| voltageAC3      | Number:ElectricPotential | The voltage for feed 3 to the grid, in Volt by default                                                           |
| currentAC1      | Number:ElectricCurrent   | The current for feed 1 to the grid, in ampere by default                                                         |
| currentAC2      | Number:ElectricCurrent   | The current for feed 2 to the grid, in ampere by default                                                         |
| currentAC3      | Number:ElectricCurrent   | The current for feed 3 to the grid, in ampere by default                                                         |
| frequencyAC1    | Number:Frequency         | The frequrence of feed 1 of the grid, in hertz by default                                                        |
| frequencyAC2    | Number:Frequency         | The frequrence of feed 2 of the grid, in hertz by default                                                        |
| frequencyAC3    | Number:Frequency         | The frequrence of feed 3 of the grid, in hertz by default                                                        |
| currentPV1      | Number:ElectricCurrent   | The current generation for input string 1, in ampere by default                                                  |
| currentPV2      | Number:ElectricCurrent   | The current generation for input string 2, in ampere by default                                                  |
| currentPV3      | Number:ElectricCurrent   | The current generation for input string 3, in ampere by default                                                  |
| voltagePV1      | Number:ElectricPotential | The voltage on input string 1, in volt by default                                                                |
| voltagePV2      | Number:ElectricPotential | The voltage on input string 2, in volt by default                                                                |
| voltagePV3      | Number:ElectricPotential | The voltage on input string 3, in volt by default                                                                |
| energyToday     | Number:Energy            | The amount of energy generated today, in kWh by default                                                          |
| energyTotal     | Number:Energy            | The total amount of energy generated, in kWh by default                                                          |
| temperature     | Number:Temperature       | The temperature from the sensor                                                                                  |
| hoursTotal      | Number:Time              | The total amount of running hours                                                                                |

## Full Example

### demo.things

```java
Thing omnikinverter:omnik:70ecb4f0 "Solar Inverter" [ hostname="igen-wifi.lan",serial=604455290]
```

### demo.items

```java
Number:Power OmnikInverterBindingThing_InstantaneousPower "Solar Power" <sun> {channel="omnikinverter:omnik:70ecb4f0:power"}
Number:Power OmnikInverterBindingThing_InstantaneousPower1 "Solar Power 1" <sun> {channel="omnikinverter:omnik:70ecb4f0:powerAC1"}
Number:Power OmnikInverterBindingThing_InstantaneousPower2 "Solar Power 2" <sun> {channel="omnikinverter:omnik:70ecb4f0:powerAC2"}
Number:Power OmnikInverterBindingThing_InstantaneousPower3 "Solar Power 3" <sun> {channel="omnikinverter:omnik:70ecb4f0:powerAC3"}
Number:Voltage OmnikInverterBindingThing_VoltagePV1 "PV Voltage 1" {channel="omnikinverter:omnik:70ecb4f0:voltagePV1"}
Number:Voltage OmnikInverterBindingThing_VoltagePV2 "PV Voltage 2" {channel="omnikinverter:omnik:70ecb4f0:voltagePV2"}
Number:Voltage OmnikInverterBindingThing_VoltagePV3 "PV Voltage 3" {channel="omnikinverter:omnik:70ecb4f0:voltagePV3"}
Number:Current OmnikInverterBindingThing_CurrentPV1 "PV current 1" {channel="omnikinverter:omnik:70ecb4f0:currentPV1"}
Number:Current OmnikInverterBindingThing_CurrentPV2 "PV current 2" {channel="omnikinverter:omnik:70ecb4f0:currentPV2"}
Number:Current OmnikInverterBindingThing_CurrentPV3 "PV current 3" {channel="omnikinverter:omnik:70ecb4f0:currentPV3"}
Number:Energy OmnikInverterBindingThing_TotalGeneratedEnergyToday "Solar Energy Today" <sun> {channel="omnikinverter:omnik:70ecb4f0:energyToday"}
Number:Energy OmnikInverterBindingThing_TotalGeneratedEnergy "Solar Energy Total" {channel="omnikinverter:omnik:70ecb4f0:energyTotal"}
```

### Sitemap

```perl
Text item=OmnikInverterBindingThing_InstantaneousPower
Text item=OmnikInverterBindingThing_InstantaneousPower1
Text item=OmnikInverterBindingThing_InstantaneousPower2
Text item=OmnikInverterBindingThing_InstantaneousPower3
Text item=OmnikInverterBindingThing_VoltagePV1
Text item=OmnikInverterBindingThing_VoltagePV2
Text item=OmnikInverterBindingThing_VoltagePV3
Text item=OmnikInverterBindingThing_CurrentPV1
Text item=OmnikInverterBindingThing_CurrentPV2
Text item=OmnikInverterBindingThing_CurrentPV3
Text item=OmnikInverterBindingThing_TotalGeneratedEnergyToday
Text item=OmnikInverterBindingThing_TotalGeneratedEnergy
```

## References

Based on the work of <https://github.com/Woutrrr/Omnik-Data-Logger>
