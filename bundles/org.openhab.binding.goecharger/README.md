# Go-eCharger Binding

This Binding controls and reads data from the [Go-eCharger](https://go-e.co/).
It is a mobile wallbox for charging EVs and has an open REST API for reading data and configuration.

## Supported Things

This binding supports go-eCharger HOME+ with 7.4kW or 22kW.

## Discovery

There is no auto discovery.
You need to get the IP from the Go-eCharger and put it into the configuration.

## Thing Configuration

The thing has two configuration parameters:

| Parameter | Description                                                              | Required |
|-----------|------------------------------------------------------------------------- |----------|
| ip        | the ip-address of your go-eCharger | yes |
| refreshInterval  | Interval to read data, default 5 (in seconds) | no |

## Channels

Currently available channels are 
| Channel ID | Item Type    | Description              |
|------------|--------------|------------------------- |
| maxAmpere | Number:ElectricCurrent | Maximum current allowed to use for charging |
| pwmSignal | Number | Signal status for PWM signal |
| error | String | Error code of charger |
| voltageL1 | Number:ElectricPotential | Voltage on L1 |
| voltageL2 | Number:ElectricPotential | Voltage on L2 |
| voltageL3 | Number:ElectricPotential | Voltage on L3 |
| currentL1 | Number:ElectricCurrent | Current on L1 |
| currentL2 | Number:ElectricCurrent | Current on L2 |
| currentL3 | Number:ElectricCurrent | Current on L3 |
| powerL1 | Number:Power | Power on L1 |
| powerL2 | Number:Power | Power on L2 |
| powerL3 | Number:Power | Power on L2 |
| phases | Number | Amount of phases currently used for charging |
| sessionChargeConsumptionLimit | Number:Energy | Wallbox stops charging after defined value, disable with 0 |
| sessionChargeConsumption | Number:Energy | Amount of kWh that have been charged in this session |
| totalConsumption | Number:Energy | Amount of kWh that have been charged since installation |
| allowCharging | Switch | If `ON` charging is allowed |
| cableEncoding | Number:ElectricCurrent | Specifies the max amps that can be charged with that cable |
| temperature | Number:Temperature | Temperature of the Go-eCharger |
| firmware | String | Firmware Version |
| accessState | String | Access state, for example OPEN, RFID ... |

## Full Example

demo.things

```
Thing goecharger:goe:garage [ip="192.168.1.36",refreshInterval=5]
```

demo.items

```
Number:ElectricCurrent     GoEChargerBindingThingMaxAmpere                       "Maximum current"                 {channel="goecharger:goe:garage:maxAmpere"}
Number                     GoEChargerBindingThingPwmSignal                       "Pwm signal status"               {channel="goecharger:goe:garage:pwmSignal"}
String                     GoEChargerBindingThingError                           "Error code"                      {channel="goecharger:goe:garage:error"}
Number:ElectricPotential   GoEChargerBindingThingVoltageL1                       "Voltage l1"                      {channel="goecharger:goe:garage:voltageL1"}
Number:ElectricPotential   GoEChargerBindingThingVoltageL2                       "Voltage l2"                      {channel="goecharger:goe:garage:voltageL2"}
Number:ElectricPotential   GoEChargerBindingThingVoltageL3                       "Voltage l3"                      {channel="goecharger:goe:garage:voltageL3"}
Number:ElectricCurrent     GoEChargerBindingThingCurrentL1                       "Current l1"                      {channel="goecharger:goe:garage:currentL1"}
Number:ElectricCurrent     GoEChargerBindingThingCurrentL2                       "Current l2"                      {channel="goecharger:goe:garage:currentL2"}
Number:ElectricCurrent     GoEChargerBindingThingCurrentL3                       "Current l3"                      {channel="goecharger:goe:garage:currentL3"}
Number:Power               GoEChargerBindingThingPowerL1                         "Power l1"                        {channel="goecharger:goe:garage:powerL1"}
Number:Power               GoEChargerBindingThingPowerL2                         "Power l2"                        {channel="goecharger:goe:garage:powerL2"}
Number:Power               GoEChargerBindingThingPowerL3                         "Power l3"                        {channel="goecharger:goe:garage:powerL3"}
Number                     GoEChargerBindingThingPhases                          "Phases"                          {channel="goecharger:goe:garage:phases"}
Number:Energy              GoEChargerBindingThingSessionChargeConsumptionLimit   "Current session charge limit"    {channel="goecharger:goe:garage:sessionChargeConsumptionLimit"}
Number:Energy              GoEChargerBindingThingSessionChargeConsumption        "Current session charge amount"   {channel="goecharger:goe:garage:sessionChargeConsumption"}
Number:Energy              GoEChargerBindingThingTotalConsumption                "Total charge amount"             {channel="goecharger:goe:garage:totalConsumption"}
Switch                     GoEChargerBindingThingAllowCharging                   "Allow charging"                  {channel="goecharger:goe:garage:allowCharging"}
Number:ElectricCurrent     GoEChargerBindingThingCableEncoding                   "Cable encoding"                  {channel="goecharger:goe:garage:cableEncoding"}
Number:Temperature         GoEChargerBindingThingTemperature                     "Temperature"                     {channel="goecharger:goe:garage:temperature"}
String                     GoEChargerBindingThingFirmware                        "Firmware"                        {channel="goecharger:goe:garage:firmware"}
String                     GoEChargerBindingThingAccessState                     "Access state"                    {channel="goecharger:goe:garage:accessState"}
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
The used phases can be read from Voltage L1-L3 or Power or Current.
For example if your car charges with one phase only L1 will be most likely around 220V, L2 and L3 will be 0.
With that you can get the amount of used phases and calculate the current you need to set.
