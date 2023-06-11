# Go-eCharger Binding

This Binding controls and reads data from the [Go-eCharger](https://go-e.co/).
It is a mobile wallbox for charging EVs and has an open REST API for reading data and configuration.
The API must be activated in the Go-eCharger app.

## Supported Things

This binding supports Go-eCharger HOME+ with 7.4kW, 11kW or 22kW.
The Go-eCharger HOMEfix with 11kW and 22kW is supported too.

## Thing Configuration

The thing has three configuration parameters:

| Parameter       | Description                                   | Required |
|-----------------|-----------------------------------------------|----------|
| ip              | The IP-address of your Go-eCharger            | yes      |
| apiVersion      | The API version to use (1=default or 2)       | no       |
| refreshInterval | Interval to read data, default 5 (in seconds) | no       |

The apiVersion 2 is only available for Go-eCharger with new hardware revision (CM-03).

## Channels

Currently available channels are
| Channel ID               | Item Type                | Description                                                   | API version       |
|--------------------------|--------------------------|---------------------------------------------------------------|-------------------|
| maxCurrent               | Number:ElectricCurrent   | Maximum current allowed to use for charging                   | 1 (r/w), 2 (r/w)  |
| maxCurrentTemp           | Number:ElectricCurrent   | Maximum current temporary (not written to EEPROM)             | 1 (r)             |
| pwmSignal                | String                   | Signal status for PWM signal                                  | 1 (r), 2 (r)      |
| error                    | String                   | Error code of charger                                         | 1 (r), 2 (r)      |
| voltageL1                | Number:ElectricPotential | Voltage on L1                                                 | 1 (r), 2 (r)      |
| voltageL2                | Number:ElectricPotential | Voltage on L2                                                 | 1 (r), 2 (r)      |
| voltageL3                | Number:ElectricPotential | Voltage on L3                                                 | 1 (r), 2 (r)      |
| currentL1                | Number:ElectricCurrent   | Current on L1                                                 | 1 (r), 2 (r)      |
| currentL2                | Number:ElectricCurrent   | Current on L2                                                 | 1 (r), 2 (r)      |
| currentL3                | Number:ElectricCurrent   | Current on L3                                                 | 1 (r), 2 (r)      |
| powerL1                  | Number:Power             | Power on L1                                                   | 1 (r), 2 (r)      |
| powerL2                  | Number:Power             | Power on L2                                                   | 1 (r), 2 (r)      |
| powerL3                  | Number:Power             | Power on L2                                                   | 1 (r), 2 (r)      |
| powerAll                 | Number:Power             | Power over all three phases                                   | 1 (r), 2 (r)      |
| phases                   | Number                   | Amount of phases currently used for charging                  | 1 (r), 2 (r/w)    |
| sessionChargeEnergyLimit | Number:Energy            | Wallbox stops charging after defined value, disable with 0    | 1 (r/w), 2 (r/w)  |
| sessionChargedEnergy     | Number:Energy            | Amount of energy that has been charged in this session        | 1 (r), 2 (r)      |
| totalChargedEnergy       | Number:Energy            | Amount of energy that has been charged since installation     | 1 (r), 2 (r)      |
| transaction              | Number                   | 0 if no card, otherwise card ID                               | 2 (r/w)           |
| allowCharging            | Switch                   | If `ON` charging is allowed                                   | 1 (r/w), 2 (r)    |
| cableCurrent             | Number:ElectricCurrent   | Specifies the max current that can be charged with that cable | 1 (r), 2 (r)      |
| temperature              | Number:Temperature       | Temperature of the curciuit board of the Go-eCharger          | 1 (r), 2 (r)      |
| temperatureType2Port     | Number:Temperature       | Temperature of the type 2 port of the Go-eCharger             | 2 (r)             |
| firmware                 | String                   | Firmware Version                                              | 1 (r), 2 (r)      |
| accessConfiguration      | String                   | Access configuration, for example OPEN, RFID ...              | 1 (r/w)           |
| forceState               | Number                   | Force state  (Neutral=0, Off=1, On=2)                         | 2 (r/w)           |

## Full Example

demo.things

```java
Thing goecharger:goe:garage [ip="192.168.1.36",refreshInterval=5]
```

demo.items

```java
Number:ElectricCurrent     GoEChargerMaxCurrent                 "Maximum current"                       {channel="goecharger:goe:garage:maxCurrent"}
Number:ElectricCurrent     GoEChargerMaxCurrentTemp             "Maximum current temporary"             {channel="goecharger:goe:garage:maxCurrentTemp"}
Number                     GoEChargerForceState                 "Force state"                           {channel="goecharger:goe:garage:forceState"}
Number                     GoEChargerPhases                     "Phases"                                {channel="goecharger:goe:garage:phases"}
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
Number:Power               GoEChargerPowerAll                   "Power over All"                        {channel="goecharger:goe:garage:powerAll"}
Number:Energy              GoEChargerSessionChargeEnergyLimit   "Current session charge energy limit"   {channel="goecharger:goe:garage:sessionChargeEnergyLimit"}
Number:Energy              GoEChargerSessionChargedEnergy       "Current session charged energy"        {channel="goecharger:goe:garage:sessionChargedEnergy"}
Number:Energy              GoEChargerTotalChargedEnergy         "Total charged energy"                  {channel="goecharger:goe:garage:totalChargedEnergy"}
Switch                     GoEChargerAllowCharging              "Allow charging"                        {channel="goecharger:goe:garage:allowCharging"}
Number:ElectricCurrent     GoEChargerCableCurrent               "Cable encoding"                        {channel="goecharger:goe:garage:cableCurrent"}
Number:Temperature         GoEChargerTemperatureType2Port       "Temperature type 2 port"               {channel="goecharger:goe:garage:temperatureType2Port"}
Number:Temperature         GoEChargerTemperatureCircuitBoard    "Temperature circuit board"             {channel="goecharger:goe:garage:temperature"}
String                     GoEChargerFirmware                   "Firmware"                              {channel="goecharger:goe:garage:firmware"}
String                     GoEChargerAccessConfiguration        "Access configuration"                  {channel="goecharger:goe:garage:accessConfiguration"}
```

## Setting charge current of Go-eCharger based on photovoltaik output

You can easily define rules to charge with PV power alone.
Here is a simple sample how such a rule could look like:

```java
rule "Set max amps for PV charging"
when
    Item availablePVCurrent received update
then
    logInfo("Amps available: ", receivedCommand.state)
    GoEChargerMaxCurrentTemp.sendCommand(receivedCommand.state)
end
```

Advanced example:

```java
rule "Set charging limit for go-eCharger"
when
    Time cron "*/10 * * ? * *" // Trigger every 10 seconds
then
    var actualMaxChargingCurrentInt = (GoEChargerMaxCurrent.state as Number).intValue

    if (GoEChargerExcessCharge.state == ON) {
        var currentChargingPower = GoEChargerPowerAll.state as Number
        var totalPowerOutputInWatt = (Total_power_fast.state as DecimalType) * 1000
        var availableChargingPowerInWatt = 0

        if (totalPowerOutputInWatt > 0 && currentChargingPower > 0) {
            // take care if already charging
            availableChargingPowerInWatt = currentChargingPower.intValue - totalPowerOutputInWatt.intValue
        } else {
            if (totalPowerOutputInWatt > 0) {
                totalPowerOutputInWatt = 0
            }
            availableChargingPowerInWatt = (totalPowerOutputInWatt.intValue * -1) + currentChargingPower.intValue
        }

        var maxAmp3Phases = (availableChargingPowerInWatt / 3) / 230
        if (maxAmp3Phases > 16.0) {
            maxAmp3Phases = 16.0
        }

        var maxAmp1Phase = availableChargingPowerInWatt / 230

        if (maxAmp3Phases >= 6) {
            // set force state to neutral (Neutral=0, Off=1, On=2)
            if (GoEChargerForceState.state != 0) {
                GoEChargerForceState.sendCommand(0)
            }

            // 3 phases
            if (GoEChargerPhases.state != 3) {
                GoEChargerPhases.sendCommand(3)
            }

            if (actualMaxChargingCurrentInt != maxAmp3Phases.intValue) {
                GoEChargerMaxCurrent.sendCommand(maxAmp3Phases.intValue)
                // logInfo("eCharger", "Set charging limit 3 Phases: " + maxAmp3Phases.intValue + " A")
            }
        } else {         
            if (maxAmp1Phase.intValue >= 6 ) {
                // set force state to neutral (Neutral=0, Off=1, On=2)
                if (GoEChargerForceState.state != 0) {
                    GoEChargerForceState.sendCommand(0)
                }

                // switch to 1 phase -> check if this is useful
                if (GoEChargerPhases.state != 1) {
                    GoEChargerPhases.sendCommand(1)
                }

                if (actualMaxChargingCurrentInt != maxAmp1Phase.intValue) {
                    GoEChargerMaxCurrent.sendCommand(maxAmp1Phase.intValue)
                    // logInfo("eCharger", "Set charging limit 1 Phase: " + maxAmp1Phase.intValue + " A")
                }
            } else {
                // switch off
                if (GoEChargerForceState.state != 1) {
                    GoEChargerMaxCurrent.sendCommand(6)
                    GoEChargerForceState.sendCommand(1)
                    // logInfo("eCharger", "Switch charging off")
                }
            }
        }
    } else {
        // set force state to neutral (Neutral=0, Off=1, On=2)
        if (GoEChargerForceState.state != 0) {
            GoEChargerForceState.sendCommand(0)
        }

        if (GoEChargerPhases.state != 3) {
            GoEChargerPhases.sendCommand(3)
        }

        if (actualMaxChargingCurrentInt != 16) {
            GoEChargerMaxCurrent.sendCommand(16)
        }
    }
end
```

You can also define more advanced rules if you have multiple cars that charge with a different amount of phases.
For example if your car charges on one phase only, you can set maxAmps to output of PV power, if your car charges on two phases you can set maxAmps to `pv output / 2`, and for 3 phases `pv output / 3`.
In general the calculation would be ´maxAmps = pvOutput / phases`.
