# Go-eCharger Binding

This Binding controls and reads data from the [Go-eCharger](https://go-e.co/).
It is a mobile wallbox for charging EVs and has an open REST API for reading data and configuration.

## Supported Things

This binding supports Go-eCharger HOME+ with 7.4kW or 22kW.

## Thing Configuration

The thing has two configuration parameters:

| Parameter       | Description                                   | Required |
|-----------------|-----------------------------------------------|----------|
| ip              | The IP-address of your Go-eCharger            | yes      |
| refreshInterval | Interval to read data, default 5 (in seconds) | no       |

## Channels

Currently available channels are 
| Channel ID               | Item Type                | Description                                                   |
|--------------------------|--------------------------|---------------------------------------------------------------|
| maxCurrent               | Number:ElectricCurrent   | Maximum current allowed to use for charging                   |
| pwmSignal                | String                   | Signal status for PWM signal                                  |
| error                    | String                   | Error code of charger                                         |
| voltageL1                | Number:ElectricPotential | Voltage on L1                                                 |
| voltageL2                | Number:ElectricPotential | Voltage on L2                                                 |
| voltageL3                | Number:ElectricPotential | Voltage on L3                                                 |
| currentL1                | Number:ElectricCurrent   | Current on L1                                                 |
| currentL2                | Number:ElectricCurrent   | Current on L2                                                 |
| currentL3                | Number:ElectricCurrent   | Current on L3                                                 |
| powerL1                  | Number:Power             | Power on L1                                                   |
| powerL2                  | Number:Power             | Power on L2                                                   |
| powerL3                  | Number:Power             | Power on L2                                                   |
| phases                   | Number                   | Amount of phases currently used for charging                  |
| sessionChargeEnergyLimit | Number:Energy            | Wallbox stops charging after defined value, disable with 0    |
| sessionChargedEnergy     | Number:Energy            | Amount of energy that has been charged in this session        |
| totalChargedEnergy       | Number:Energy            | Amount of energy that has been charged since installation     |
| allowCharging            | Switch                   | If `ON` charging is allowed                                   |
| cableCurrent             | Number:ElectricCurrent   | Specifies the max current that can be charged with that cable |
| temperature              | Number:Temperature       | Temperature of the Go-eCharger                                |
| firmware                 | String                   | Firmware Version                                              |
| accessConfiguration      | String                   | Access configuration, for example OPEN, RFID ...              |

## Full Example

demo.things

```
Thing goecharger:goe:garage [ip="192.168.1.36",refreshInterval=5]
```

demo.items

```
Number:ElectricCurrent     GoEChargerMaxCurrent                 "Maximum current"                       {channel="goecharger:goe:garage:maxCurrent"}
String                     GoEChargerPwmSignal                  "Pwm signal status"                     {channel="goecharger:goe:garage:pwmSignal"}
String                     GoEChargerError                      "Error code"                            {channel="goecharger:goe:garage:error"}
Number:ElectricPotential   GoEChargerVoltageL1                  "Voltage l1"                            {channel="goecharger:goe:garage:voltageL1"}
Number:ElectricPotential   GoEChargerVoltageL2                  "Voltage l2"                            {channel="goecharger:goe:garage:voltageL2"}
Number:ElectricPotential   GoEChargerVoltageL3                  "Voltage l3"                            {channel="goecharger:goe:garage:voltageL3"}
Number:ElectricCurrent     GoEChargerCurrentL1                  "Current l1"                            {channel="goecharger:goe:garage:currentL1"}
Number:ElectricCurrent     GoEChargerCurrentL2                  "Current l2"                            {channel="goecharger:goe:garage:currentL2"}
Number:ElectricCurrent     GoEChargerCurrentL3                  "Current l3"                            {channel="goecharger:goe:garage:currentL3"}
Number:Power               GoEChargerPowerL1                    "Power l1"                              {channel="goecharger:goe:garage:powerL1"}
Number:Power               GoEChargerPowerL2                    "Power l2"                              {channel="goecharger:goe:garage:powerL2"}
Number:Power               GoEChargerPowerL3                    "Power l3"                              {channel="goecharger:goe:garage:powerL3"}
Number                     GoEChargerPhases                     "Phases"                                {channel="goecharger:goe:garage:phases"}
Number:Energy              GoEChargerSessionChargeEnergyLimit   "Current session charge energy limit"   {channel="goecharger:goe:garage:sessionChargeEnergyLimit"}
Number:Energy              GoEChargerSessionChargedEnergy       "Current session charged energy"        {channel="goecharger:goe:garage:sessionChargedEnergy"}
Number:Energy              GoEChargerTotalChargedEnergy         "Total charged energy"                  {channel="goecharger:goe:garage:totalChargedEnergy"}
Switch                     GoEChargerAllowCharging              "Allow charging"                        {channel="goecharger:goe:garage:allowCharging"}
Number:ElectricCurrent     GoEChargerCableCurrent               "Cable encoding"                        {channel="goecharger:goe:garage:cableCurrent"}
Number:Temperature         GoEChargerTemperature                "Temperature"                           {channel="goecharger:goe:garage:temperature"}
String                     GoEChargerFirmware                   "Firmware"                              {channel="goecharger:goe:garage:firmware"}
String                     GoEChargerAccessConfiguration        "Access configuration"                  {channel="goecharger:goe:garage:accessConfiguration"}
```

## Setting charge current of Go-eCharger based on photovoltaik output

You can easily define rules to charge with PV power alone.
Here is a simple sample how such a rule could look like:

```
rule "Set max amps for PV charging"
when
    Item availablePVCurrent received update
then
    logInfo("Amps available: ", receivedCommand.state)
    MaxAmpere.sendCommand(receivedCommand.state)
end
```
You can also define more advanced rules if you have multiple cars that charge with a different amount of phases.
For example if your car charges on one phase only, you can set maxAmps to output of PV power, if your car charges on two phases you can set maxAmps to `pv output / 2`, and for 3 phases `pv output / 3`.
In general the calculation would be ´maxAmps = pvOutput / phases`.
