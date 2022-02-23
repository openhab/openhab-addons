# GoEChargerAPIv2 Binding

This Binding reads and writes data from the [Go-eCharger](https://go-e.co/) HOME+ and HOMEfix with API version 2 (Hardware version 3).
The HOME+ is a mobile wallbox for charging EVs, HOMEfix is a mounted wallbox and they have an open REST API for reading and writing data and configuration.

## Supported Things

This binding supports Go-eCharger HOME+ and HOMEfix with API version 2 (Hardware version 3) with 11kW or 22kW.

## Thing Configuration

The thing has two configuration parameters:

| Parameter       | Description                                   | Required |
|-----------------|-----------------------------------------------|----------|
| ip              | The IP-address of your Go-eCharger            | yes      |
| refreshInterval | Interval to read data, default 5 (in seconds) | no       |

### `demo` Thing Configuration

demo.things

```
Thing goecharger:goe:garage [ip="192.168.1.6",refreshInterval=5]
```

## Channels

Currently available channels are 
| Channel ID               | Item Type                | Description                                                   | Writable |
|--------------------------|--------------------------|---------------------------------------------------------------|          |
| maxCurrent               | Number:ElectricCurrent   | Maximum current allowed to use for charging                   | yes      |
| chargingPhases           | Number                   | Amount of phases currently used for charging                  | yes      |
| pwmSignal                | String                   | Signal status for PWM signal                                  | no       |
| error                    | String                   | Error code of charger                                         | no       |
| voltageL1                | Number:ElectricPotential | Voltage on L1                                                 | no       |
| voltageL2                | Number:ElectricPotential | Voltage on L2                                                 | no       |
| voltageL3                | Number:ElectricPotential | Voltage on L3                                                 | no       |
| currentL1                | Number:ElectricCurrent   | Current on L1                                                 | no       |
| currentL2                | Number:ElectricCurrent   | Current on L2                                                 | no       |
| currentL3                | Number:ElectricCurrent   | Current on L3                                                 | no       |
| powerL1                  | Number:Power             | Power on L1                                                   | no       |
| powerL2                  | Number:Power             | Power on L2                                                   | no       |
| powerL3                  | Number:Power             | Power on L2                                                   | no       |
| sessionChargeEnergyLimit | Number:Energy            | Wallbox stops charging after defined value, disable with 0    | yes      |
| sessionChargedEnergy     | Number:Energy            | Amount of energy that has been charged in this session        | no       |
| totalChargedEnergy       | Number:Energy            | Amount of energy that has been charged since installation     | no       |
| allowCharging            | Switch                   | If `ON` charging is allowed                                   | no       |
| cableCurrent             | Number:ElectricCurrent   | Specifies the max current that can be charged with that cable | no       |
| temperature1             | Number:Temperature       | Temperature 1 of the Go-eCharger                              | no       |
| temperature2             | Number:Temperature       | Temperature 2 of the Go-eCharger                              | no       |
| firmware                 | String                   | Firmware Version                                              | no       |

## Full Example

demo.items

```
Number:ElectricCurrent     GoEChargerMaxCurrent                 "Maximum current"                       {channel="goechargerapiv2:goeapiv2:garage:maxCurrent"}
Number                     GoEChargerChargingPhases             "Charging phases"                       {channel="goechargerapiv2:goeapiv2:garage:chargingPhases"}
String                     GoEChargerPwmSignal                  "Pwm signal status"                     {channel="goechargerapiv2:goeapiv2:garage:pwmSignal"}
String                     GoEChargerError                      "Error code"                            {channel="goechargerapiv2:goeapiv2:garage:error"}
Number:ElectricPotential   GoEChargerVoltageL1                  "Voltage l1"                            {channel="goechargerapiv2:goeapiv2:garage:voltageL1"}
Number:ElectricPotential   GoEChargerVoltageL2                  "Voltage l2"                            {channel="goechargerapiv2:goeapiv2:garage:voltageL2"}
Number:ElectricPotential   GoEChargerVoltageL3                  "Voltage l3"                            {channel="goechargerapiv2:goeapiv2:garage:voltageL3"}
Number:ElectricCurrent     GoEChargerCurrentL1                  "Current l1"                            {channel="goechargerapiv2:goeapiv2:garage:currentL1"}
Number:ElectricCurrent     GoEChargerCurrentL2                  "Current l2"                            {channel="goechargerapiv2:goeapiv2:garage:currentL2"}
Number:ElectricCurrent     GoEChargerCurrentL3                  "Current l3"                            {channel="goechargerapiv2:goeapiv2:garage:currentL3"}
Number:Power               GoEChargerPowerL1                    "Power l1"                              {channel="goechargerapiv2:goeapiv2:garage:powerL1"}
Number:Power               GoEChargerPowerL2                    "Power l2"                              {channel="goechargerapiv2:goeapiv2:garage:powerL2"}
Number:Power               GoEChargerPowerL3                    "Power l3"                              {channel="goechargerapiv2:goeapiv2:garage:powerL3"}
Number:Energy              GoEChargerSessionChargeEnergyLimit   "Current session charge energy limit"   {channel="goechargerapiv2:goeapiv2:garage:sessionChargeEnergyLimit"}
Number:Energy              GoEChargerSessionChargedEnergy       "Current session charged energy"        {channel="goechargerapiv2:goeapiv2:garage:sessionChargedEnergy"}
Number:Energy              GoEChargerTotalChargedEnergy         "Total charged energy"                  {channel="goechargerapiv2:goeapiv2:garage:totalChargedEnergy"}
Switch                     GoEChargerAllowCharging              "Allow charging"                        {channel="goechargerapiv2:goeapiv2:garage:allowCharging"}
Number:ElectricCurrent     GoEChargerCableCurrent               "Cable encoding"                        {channel="goechargerapiv2:goeapiv2:garage:cableCurrent"}
Number:Temperature         GoEChargerTemperature1               "Temperature 1"                         {channel="goechargerapiv2:goeapiv2:garage:temperature1"}
Number:Temperature         GoEChargerTemperature2               "Temperature 2"                         {channel="goechargerapiv2:goeapiv2:garage:temperature2"}
String                     GoEChargerFirmware                   "Firmware"                              {channel="goechargerapiv2:goeapiv2:garage:firmware"}
```

## Rule for setting maxCurrent of the charger

You can easily define rules to charge with photovoltaik power alone.
Here is a sample how such a rule could look like:

```
rule "Set charging limit for go-eCharger"
when
    Time cron "*/3 * * ? * *" // Trigger every 3 seconds, or alternative when Total_power_fast changed
then
    var totalPowerOutputInWatt = Total_power_fast.state as DecimalType * 1000
    if (totalPowerOutputInWatt > 0) {
        totalPowerOutputInWatt = 0
    }

    totalPowerOutputInWatt = totalPowerOutputInWatt * -1

    var maxAmp3Phases = (totalPowerOutputInWatt / 3) / 230
    if (maxAmp3Phases > 16.0) {
        maxAmp3Phases = 16.0
    }
    var maxAmp1Phase = totalPowerOutputInWatt / 230;

    if (maxAmp3Phases.intValue >= 6) {
        // 3 phases
        if ((GoEChargerChargingPhases.state as Number) != 3) {
            GoEChargerChargingPhases.sendCommand(3);
        }

        if ((GoEChargerMaxCurrent.state as Number).intValue != maxAmp3Phases.intValue) {
            GoEChargerMaxCurrent.sendCommand(maxAmp3Phases.intValue)
        }
    } else {
        if (maxAmp1Phase.intValue >= 6 ) {
            // switch to 1 phase -> check if this is useful
            if ((GoEChargerChargingPhases.state as Number) != 1) {
                GoEChargerChargingPhases.sendCommand(1)
            }

            if ((GoEChargerMaxCurrent.state as Number).intValue != maxAmp1Phase.intValue) {
                GoEChargerMaxCurrent.sendCommand(maxAmp1Phase.intValue)
            }
        } else {
            // switch off
        }
    }
end
```
