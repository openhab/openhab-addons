# Go-eCharger Binding

This Binding controls and reads data from the [Go-eCharger](https://go-e.co/). It is a mobile wallbox for charging EVs and has an open REST API for reading data and configuration.

## Supported Things

This binding supports go-eCharger HOME+ with 7.4kW or 22kW.

## Discovery

There is no auto discovery. You need to get the IP from the Go-eCharger and put it into the configuration.

## Thing Configuration

The thing has two configuration parameters:

| Parameter | Description                                                              |
|-----------|------------------------------------------------------------------------- |
| ip        | the ip-address of your go-eCharger |
| refreshInterval  | Interval to read data (in seconds) |

## Channels

Currently available channels are 
| Channel ID | Item Type    | Description              |
|------------|--------------|------------------------- |
| maxAmpere | Number | Max ampere allowed to use for charging |
| pwmSignal | Number | Signal status for PWM signal |
| error | String | Error code of charger |
| voltageL1 | Number | Voltage on L1 |
| voltageL2 | Number | Voltage on L2 |
| voltageL3 | Number | Voltage on L3 |
| currentL1 | Number | Current on L1 |
| currentL2 | Number | Current on L2 |
| currentL3 | Number | Current on L3 |
| powerL1 | Number | Power on L1 |
| powerL2 | Number | Power on L2 |
| powerL3 | Number | Power on L2 |
| phases | Number | Connected lines to charger |
| sessionChargeConsumptionLimit | Number | Wallbox stops charging after defined value |
| sessionChargeConsumption | Number | Amount of kWh that have been charged in this session |
| totalConsumption | Number | Amount of kWh that have been charged since installation |
| allowCharging | Switch | If `ON` charging is allowed |
| stopState | Switch | If `ON` charger will stop after reaching current session charge limit |
| cableEncoding | Number | Specifies the max amps that can be charged with that cable |
| temperature | Number | Temperature of the Go-eCharger |
| firmware | String | Firmware Version |

## Full Example

demo.things
```
Thing goecharger:goe:garage [ip="192.168.1.36",refreshInterval=5]
```

demo.items
```
Number   MaxAmpere                       "Max ampere"                      {channel="goecharger:goe:garage:maxAmpere"}
Number   PwmSignal                       "Pwm signal status"               {channel="goecharger:goe:garage:pwmSignal"}
String   Error                           "Error code"                      {channel="goecharger:goe:garage:error"}
Number   VoltageL1                       "Voltage L1"                      {channel="goecharger:goe:garage:voltageL1"}
Number   VoltageL2                       "Voltage L2"                      {channel="goecharger:goe:garage:voltageL2"}
Number   VoltageL3                       "Voltage L3"                      {channel="goecharger:goe:garage:voltageL3"}
Number   CurrentL1                       "Current L1"                      {channel="goecharger:goe:garage:currentL1"}
Number   CurrentL2                       "Current L2"                      {channel="goecharger:goe:garage:currentL2"}
Number   CurrentL3                       "Current L3"                      {channel="goecharger:goe:garage:currentL3"}
Number   PowerL1                         "Power L1"                        {channel="goecharger:goe:garage:powerL1"}
Number   PowerL2                         "Power L2"                        {channel="goecharger:goe:garage:powerL2"}
Number   PowerL3                         "Power L3"                        {channel="goecharger:goe:garage:powerL3"}
Number   Phases                          "Phases"                          {channel="goecharger:goe:garage:phases"}
Number   SessionChargeConsumptionLimit   "Current session charge limit"    {channel="goecharger:goe:garage:sessionChargeConsumptionLimit"}
Number   SessionChargeConsumption        "Current session charge amount"   {channel="goecharger:goe:garage:sessionChargeConsumption"}
Switch   AllowCharging                   "Allow charging"                  {channel="goecharger:goe:garage:allowCharging"}
Switch   StopState                       "Stop state"                      {channel="goecharger:goe:garage:stopState"}
Number   CableEncoding                   "Cable encoding"                  {channel="goecharger:goe:garage:cableEncoding"}
Number   Temperature                     "Temperature"                     {channel="goecharger:goe:garage:temperature"}
String   Firmware                        "Firmware"                        {channel="goecharger:goe:garage:firmware"}
Number   TotalConsumption                "Total charge amount"             {channel="goecharger:goe:garage:totalConsumption"}
```

## Settings charge current of Go-eCharger based on PV

You can easily define rules to charge with PV power alone. Here is a simple sample how such a rule could look like:
```
rule "Set max amps for PV charging"
when
    Item availablePVCurrent received update
then
    logInfo("Amps available: ", receivedCommand.state)
    MaxAmpere.sendCommand(receivedCommand.state)
end
```
You can also define more advanced rules if you have multiple cars that charge with a different amount of phases. The used phases can be read from Voltage L1-L3 or Power or Current. For example if your car charges with one phase only L1 will be most likely around 220V, L2 and L3 will be 0. With that you can get the amount of used phases and calculate the current you need to set.