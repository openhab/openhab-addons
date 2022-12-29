# LuxtronikHeatpump Binding

This binding gives the possibility to integrate any Heatpump that is based on the Luxtronik 2 contol unit of Alpha Innotec. This includes heatpumps of:

- Alpha InnoTec
- Buderus (Logamatic HMC20, HMC20 Z)
- CTA All-In-One (Aeroplus)
- Elco
- Nibe (AP-AW10)
- Roth (ThermoAura®, ThermoTerra)
- (Siemens) Novelan (WPR NET)
- Wolf Heiztechnik (BWL/BWS)

This binding was tested with:

- Siemens Novelan LD 7

:::tip Note
If you have another heatpump the binding works with, let us know, so we can extend the list
:::

Note: The whole functionality is based on data that was reverse engineered, so use it at your own risk.

## Supported Things

This binding only supports one thing type "Luxtronik Heatpump" (heatpump).

## Discovery

This binding will try to detect heat pumps that are reachable in the same IPv4 subnet.

## Thing Configuration

Each heatpump requires the following configuration parameters:

| parameter    | required | default | description |
|--------------|----------|---------|-------------|
| ipAddress    | yes      |         | IP address of the heatpump |
| port         | no       | 8889    | Port number to connect to. This should be `8889` for most heatpumps. For heatpumps using a firmware version before V1.73 port `8888` needs to be used. |
| refresh      | no       | 300     | Interval (in seconds) to refresh the channel values. |
| showAllChannels | no       | false    | Show all channels (even those determined as not supported) |

## Channels

As the Luxtronik 2 control is able to handle multiple heat pumps with different features (like heating, hot water, cooling, solar, photovoltaics, swimming pool,...), the binding has a lot channels.
Depending on the heatpump it is used with, various channels might not hold any (useful) values.
If `showAllChannels` is not activated for the thing, this binding will automatically try to hide channels that are not available for your heat pump. As this is done using reverse engineered parameters it might not be correct in all cases.
If you miss a channel that should be available for your heat pump, you can enable `showAllChannels` for your thing, so all channels become available. Feel free to report such a case on the forum, so we can try to improve / fix that behavior.

The following channels are holding read only values:

| channel  | type   | advanced | description                  |
|----------|--------|----------|------------------------------|
| temperatureHeatingCircuitFlow | Number:Temperature |   | Flow temperature heating circuit |
| temperatureHeatingCircuitReturn | Number:Temperature |   | Return temperature heating circuit |
| temperatureHeatingCircuitReturnTarget | Number:Temperature |   | Return setpoint heating circuit |
| temperatureBufferTankReturn | Number:Temperature | x | Return temperature in buffer tank |
| temperatureHotGas | Number:Temperature | x | Hot gas temperature |
| temperatureOutside | Number:Temperature |   | Outside temperature |
| temperatureOutsideMean | Number:Temperature |   | Average temperature outside over 24 h (heating limit function) |
| temperatureHotWater | Number:Temperature |   | Hot water actual temperature |
| temperatureHeatSourceInlet | Number:Temperature | x | Heat source inlet temperature |
| temperatureHeatSourceOutlet | Number:Temperature | x | Heat source outlet temperature |
| temperatureMixingCircuit1Flow | Number:Temperature | x | Mixing circuit 1 Flow temperature |
| temperatureMixingCircuit1FlowTarget | Number:Temperature | x | Mixing circuit 1 Flow target temperature |
| temperatureRoomStation | Number:Temperature | x | Room temperature room station 1 |
| temperatureMixingCircuit2Flow | Number:Temperature | x | Mixing circuit 2 Flow temperature |
| temperatureMixingCircuit2FlowTarget | Number:Temperature | x | Mixing circuit 2 Flow target temperature |
| temperatureSolarCollector | Number:Temperature | x | Solar collector sensor |
| temperatureSolarTank | Number:Temperature | x | Solar tank sensor |
| temperatureExternalEnergySource | Number:Temperature | x | Sensor external energy source |
| inputASD | Switch | x | Input "Defrost end, brine pressure, flow rate" |
| inputHotWaterThermostat | Switch | x | Input "Domestic hot water thermostat" |
| inputUtilityLock | Switch | x | Input "EVU lock" |
| inputHighPressureCoolingCircuit | Switch | x | Input "High pressure cooling circuit |
| inputMotorProtectionOK | Switch | x | Input "Motor protection OK" |
| inputLowPressure | Switch | x | Input "Low pressure" |
| inputPEX | Switch | x | Input "Monitoring contact for potentiostat" |
| inputSwimmingPoolThermostat | Switch | x | Input "Swimming pool thermostat" |
| outputDefrostValve | Switch | x | Output "Defrost valve" |
| outputBUP | Switch | x | Output "Domestic hot water pump/changeover valve" |
| outputHeatingCirculationPump | Switch | x | Output "Heating circulation pump" |
| outputMixingCircuit1Open | Switch | x | Output "Mixing circuit 1 Open" |
| outputMixingCircuit1Closed | Switch | x | Output "Mixing circuit 1 Closed" |
| outputVentilation | Switch | x | Output "Ventilation" |
| outputVBO | Switch | x | Output "Brine pump/fan" |
| outputCompressor1 | Switch | x | Output "Compressor 1" |
| outputCompressor2 | Switch | x | Output "Compressor 2" |
| outputCirculationPump | Switch | x | Output "Circulation pump" |
| outputZUP | Switch | x | Output "Auxiliary circulation pump" |
| outputControlSignalAdditionalHeating | Switch | x | Output "Control signal additional heating" |
| outputFaultSignalAdditionalHeating | Switch | x | Output "Control signal additional heating/fault signal" |
| outputAuxiliaryHeater3 | Switch | x | Output "Auxiliary heater 3" |
| outputMixingCircuitPump2 | Switch | x | Output "Pump mixing circuit 2" |
| outputSolarChargePump | Switch | x | Output "Solar charge pump" |
| outputSwimmingPoolPump | Switch | x | Output "Swimming pool pump" |
| outputMixingCircuit2Closed | Switch | x | Output "Mixing circuit 2 Closed" |
| outputMixingCircuit2Open | Switch | x | Output "Mixing circuit 2 Open" |
| runtimeTotalCompressor1 | Number:Time |   | Operation hours compressor 1 |
| pulsesCompressor1 | Number:Dimensionless |   | Pulses compressor 1 |
| runtimeTotalCompressor2 | Number:Time | x | Operation hours compressor 2 |
| pulsesCompressor2 | Number:Dimensionless | x | Pulses compressor 2 |
| runtimeTotalSecondHeatGenerator1 | Number:Time | x | Operation hours Second heat generator 1 |
| runtimeTotalSecondHeatGenerator2 | Number:Time | x | Operation hours Second heat generator 2 |
| runtimeTotalSecondHeatGenerator3 | Number:Time | x | Operation hours Second heat generator 3 |
| runtimeTotalHeatPump | Number:Time |   | Operation hours heat pump |
| runtimeTotalHeating | Number:Time |   | Operation hours heating |
| runtimeTotalHotWater | Number:Time |   | Operation hours hot water |
| runtimeTotalCooling | Number:Time |   | Operation hours cooling |
| runtimeCurrentHeatPump | Number:Time | x | Heat pump running since |
| runtimeCurrentSecondHeatGenerator1 | Number:Time | x | Second heat generator 1 running since |
| runtimeCurrentSecondHeatGenerator2 | Number:Time | x | Second heat generator 2 running since |
| mainsOnDelay | Number:Time | x | Mains on delay |
| switchingCycleLockOff | Number:Time | x | Switching cycle lock off |
| switchingCycleLockOn | Number:Time | x | Switching cycle lock on |
| compressorIdleTime | Number:Time | x | Compressor Idle time |
| heatingControllerMoreTime | Number:Time | x | Heating controller More time |
| heatingControllerLessTime | Number:Time | x | Heating controller Less time |
| runtimeCurrentThermalDisinfection | Number:Time | x | Thermal disinfection running since |
| timeHotWaterLock | Number:Time | x | Hot water lock |
| bivalenceStage | Number | x | Bivalence stage |
| operatingStatus | Number |   | Operating status |
| errorTime0 | DateTime |   | Timestamp error 0 in memory |
| errorTime1 | DateTime | x | Timestamp error 1 in memory |
| errorTime2 | DateTime | x | Timestamp error 2 in memory |
| errorTime3 | DateTime | x | Timestamp error 3 in memory |
| errorTime4 | DateTime | x | Timestamp error 4 in memory |
| errorCode0 | Number |   | Error code Error 0 in memory |
| errorCode1 | Number | x | Error code Error 1 in memory |
| errorCode2 | Number | x | Error code Error 2 in memory |
| errorCode3 | Number | x | Error code Error 3 in memory |
| errorCode4 | Number | x | Error code Error 4 in memory |
| errorCountInMemory | Number | x | Number of errors in memory |
| shutdownReason0 | Number |   | Reason shutdown 0 in memory |
| shutdownReason1 | Number | x | Reason shutdown 1 in memory |
| shutdownReason2 | Number | x | Reason shutdown 2 in memory |
| shutdownReason3 | Number | x | Reason shutdown 3 in memory |
| shutdownReason4 | Number | x | Reason shutdown 4 in memory |
| shutdownTime0 | DateTime |   | Timestamp shutdown 0 in memory |
| shutdownTime1 | DateTime | x | Timestamp shutdown 1 in memory |
| shutdownTime2 | DateTime | x | Timestamp shutdown 2 in memory |
| shutdownTime3 | DateTime | x | Timestamp shutdown 3 in memory |
| shutdownTime4 | DateTime | x | Timestamp shutdown 4 in memory |
| comfortBoardInstalled | Switch | x | Comfort board installed |
| menuStateFull | String |   | Status (complete) |
| menuStateLine1 | Number |   | Status line 1 |
| menuStateLine2 | Number | x | Status line 2 |
| menuStateLine3 | Number | x | Status line 3 |
| menuStateTime | Number:Time | x | Status Time |
| bakeoutProgramStage | Number | x | Stage bakeout program |
| bakeoutProgramTemperature | Number:Temperature | x | Temperature bakeout program |
| bakeoutProgramTime | Number:Time | x | Runtime bakeout program |
| iconHotWater | Switch | x | DHW active/inactive icon |
| iconHeater | Number | x | Heater icon |
| iconMixingCircuit1 | Number | x | Mixing circuit 1 icon |
| iconMixingCircuit2 | Number | x | Mixing circuit 2 icon |
| shortProgramSetting | Number | x | Short program setting |
| statusSlave1 | Number | x | Status Slave 1 |
| statusSlave2 | Number | x | Status Slave 2 |
| statusSlave3 | Number | x | Status Slave 3 |
| statusSlave4 | Number | x | Status Slave 4 |
| statusSlave5 | Number | x | Status Slave 5 |
| currentTimestamp | DateTime | x | Current time of the heat pump |
| iconMixingCircuit3 | Number | x | Mixing circuit 3 icon |
| temperatureMixingCircuit3FlowTarget | Number:Temperature | x | Mixing circuit 3 Flow set temperature |
| temperatureMixingCircuit3Flow | Number:Temperature | x | Mixing circuit 3 Flow temperature |
| outputMixingCircuit3Close | Switch | x | Output "Mixing circuit 3 Closed" |
| outputMixingCircuit3Open | Switch | x | Output "Mixing circuit 3 Up" |
| outputMixingCircuitPump3 | Switch | x | Pump mixing circuit 3 |
| timeUntilDefrost | Number:Time | x | Time until defrost |
| temperatureRoomStation2 | Number:Temperature | x | Room temperature room station 2 |
| temperatureRoomStation3 | Number:Temperature | x | Room temperature room station 3 |
| iconTimeSwitchSwimmingPool | Number | x | Time switch swimming pool icon |
| runtimeTotalSwimmingPool | Number:Time | x | Operation hours swimming pool |
| coolingRelease | Switch | x | Release cooling |
| inputAnalog | Number:ElectricPotential | x | Analog input signal |
| iconCirculationPump | Number | x | Circulation pumps icon |
| heatMeterHeating | Number:Energy |   | Heat meter heating |
| heatMeterHotWater | Number:Energy |   | Heat meter domestic water |
| heatMeterSwimmingPool | Number:Energy |   | Heat meter swimming pool |
| heatMeterTotalSinceReset | Number:Energy |   | Total heat meter |
| heatMeterFlowRate | Number:VolumetricFlowRate |   | Heat meter flow rate |
| outputAnalog1 | Number:ElectricPotential | x | Analog output 1 |
| outputAnalog2 | Number:ElectricPotential | x | Analog output 2 |
| timeLockSecondHotGasCompressor | Number:Time | x | Lock second compressor hot gas |
| temperatureSupplyAir | Number:Temperature | x | Supply air temperature |
| temperatureExhaustAir | Number:Temperature | x | Exhaust air temperature |
| runtimeTotalSolar | Number:Time | x | Operating hours solar |
| outputAnalog3 | Number:ElectricPotential | x | Analog output 3 |
| outputAnalog4 | Number:ElectricPotential | x | Analog output 4 |
| outputSupplyAirFan | Number:ElectricPotential | x | Supply air fan (defrost function) |
| outputExhaustFan | Number:ElectricPotential | x | Exhaust fan |
| outputVSK | Switch | x | Output VSK |
| outputFRH | Switch | x | Output FRH |
| inputAnalog2 | Number:ElectricPotential | x | Analog input 2 |
| inputAnalog3 | Number:ElectricPotential | x | Analog input 3 |
| inputSAX | Switch | x | Input SAX |
| inputSPL | Switch | x | Input SPL |
| ventilationBoardInstalled | Switch | x | Ventilation board installed |
| flowRateHeatSource | Number:VolumetricFlowRate | x | Flow rate heat source |
| linBusInstalled | Switch | x | LIN BUS installed |
| temperatureSuctionEvaporator | Number:Temperature | x | Temperature suction evaporator |
| temperatureSuctionCompressor | Number:Temperature | x | Temperature suction compressor |
| temperatureCompressorHeating | Number:Temperature | x | Temperature compressor heating |
| temperatureOverheating | Number:Temperature | x | Overheating |
| temperatureOverheatingTarget | Number:Temperature | x | Overheating target |
| highPressure | Number:Pressure | x | High pressure |
| lowPressure | Number:Pressure | x | Low pressure |
| outputCompressorHeating | Switch | x | Output compressor heating |
| controlSignalCirculatingPump | Number:Energy | x | Control signal circulating pump |
| fanSpeed | Number | x | Fan speed |
| temperatureSafetyLimitFloorHeating | Switch | x | Safety temperature limiter floor heating |
| powerTargetValue | Number:Energy | x | Power target value |
| powerActualValue | Number:Energy | x | Power actual value |
| temperatureFlowTarget | Number:Temperature | x | Temperature flow set point |
| operatingStatusSECBoard | Number | x | SEC Board operating status |
| fourWayValve | Number | x | Four-way valve |
| compressorSpeed | Number | x | Compressor speed |
| temperatureCompressorEVI | Number:Temperature | x | Compressor temperature EVI (Enhanced Vapour Injection) |
| temperatureIntakeEVI | Number:Temperature | x | Intake temperature EVI |
| temperatureOverheatingEVI | Number:Temperature | x | Overheating EVI |
| temperatureOverheatingTargetEVI | Number:Temperature | x | Overheating EVI target |
| temperatureCondensation | Number:Temperature | x | Condensation temperature |
| temperatureLiquidEEV | Number:Temperature | x | Liquid temperature EEV (electronic expansion valve) |
| temperatureHypothermiaEEV | Number:Temperature | x | Hypothermia EEV |
| pressureEVI | Number:Pressure | x | Pressure EVI |
| voltageInverter | Number:ElectricPotential | x | Voltage inverter |
| temperatureHotGas2 | Number:Temperature | x | Hot gas temperature sensor 2 |
| temperatureHeatSourceInlet2 | Number:Temperature | x | Temperature sensor heat source inlet 2 |
| temperatureIntakeEvaporator2 | Number:Temperature | x | Intake temperature evaporator 2 |
| temperatureIntakeCompressor2 | Number:Temperature | x | Intake temperature compressor 2 |
| temperatureCompressor2Heating | Number:Temperature | x | Temperature compressor 2 heating |
| temperatureOverheating2 | Number:Temperature | x | Overheating 2 |
| temperatureOverheatingTarget2 | Number:Temperature | x | Overheating target 2 |
| highPressure2 | Number:Pressure | x | High pressure 2 |
| lowPressure2 | Number:Pressure | x | Low pressure 2 |
| inputSwitchHighPressure2 | Switch | x | Input pressure switch high pressure 2 |
| outputDefrostValve2 | Switch | x | Output defrost valve 2 |
| outputVBO2 | Switch | x | Output brine pump/fan 2 |
| outputCompressor1_2 | Switch | x | Compressor output 1 / 2 |
| outputCompressorHeating2 | Switch | x | Compressor output heating 2 |
| secondShutdownReason0 | Number | x | Reason shutdown 0 in memory |
| secondShutdownReason1 | Number | x | Reason shutdown 1 in memory |
| secondShutdownReason2 | Number | x | Reason shutdown 2 in memory |
| secondShutdownReason3 | Number | x | Reason shutdown 3 in memory |
| secondShutdownReason4 | Number | x | Reason shutdown 4 in memory |
| secondShutdownTime0 | DateTime | x | Timestamp shutdown 0 in memory |
| secondShutdownTime1 | DateTime | x | Timestamp shutdown 1 in memory |
| secondShutdownTime2 | DateTime | x | Timestamp shutdown 2 in memory |
| secondShutdownTime3 | DateTime | x | Timestamp shutdown 3 in memory |
| secondShutdownTime4 | DateTime | x | Timestamp shutdown 4 in memory |
| temperatureRoom | Number:Temperature | x | Room temperature actual value |
| temperatureRoomTarget | Number:Temperature | x | Room temperature set point |
| temperatureHotWaterTop | Number:Temperature | x | Temperature domestic water top |
| frequencyCompressor | Number:Frequency | x | Compressor frequency |
| channel232 | Number | x | Channel 232 |
| channel233 | Number | x | Channel 233 |
| channel234 | Number | x | Channel 234 |
| channel235 | Number | x | Channel 235 |
| frequencyCompressorTarget | Number:Frequency | x | Compressor Target Frequency |
| channel237 | Number | x | Channel 237 |
| channel238 | Number | x | Channel 238 |
| channel239 | Number | x | Channel 239 |
| channel240 | Number | x | Channel 240 |
| channel241 | Number | x | Channel 241 |
| channel242 | Number | x | Channel 242 |
| channel243 | Number | x | Channel 243 |
| channel244 | Number | x | Channel 244 |
| channel245 | Number | x | Channel 245 |
| channel246 | Number | x | Channel 246 |
| channel247 | Number | x | Channel 247 |
| channel248 | Number | x | Channel 248 |
| channel249 | Number | x | Channel 249 |
| channel250 | Number | x | Channel 250 |
| channel251 | Number | x | Channel 251 |
| channel252 | Number | x | Channel 252 |
| channel253 | Number | x | Channel 253 |
| flowRateHeatSource2 | Number:VolumetricFlowRate | x | Flow Rate Heat Source |
| channel255 | Number | x | Channel 255 |
| channel256 | Number | x | Channel 256 |
| heatingPowerActualValue | Number:Power | x | Heating Power Actual Value |
| channel258 | Number | x | Channel 258 |
| channel259 | Number | x | Channel 259 |
| channel260 | Number | x | Channel 260 |

The usage of the numbered channels above is currently unknown. If you are able to directly match one of the values to any value reported by your heat pump, feel free to report back on the forum, so we are able to give the channel a proper name instead.

The following channels are also writable:
| channel  | type   | advanced | description                  |
|----------|--------|----------|------------------------------|
| temperatureHeatingParallelShift | Number:Temperature |   | Heating temperature (parallel shift) |
| temperatureHotWaterTarget | Number:Temperature |   | Hot water target temperature |
| heatingMode | Number |   | Heating mode |
| hotWaterMode | Number |   | Hot water operating mode |
| thermalDisinfectionMonday | Switch |  x  | Thermal disinfection (Monday) |
| thermalDisinfectionTuesday | Switch |  x  | Thermal disinfection (Tuesday) |
| thermalDisinfectionWednesday | Switch |  x  | Thermal disinfection (Wednesday) |
| thermalDisinfectionThursday | Switch |  x  | Thermal disinfection (Thursday) |
| thermalDisinfectionFriday | Switch |  x  | Thermal disinfection (Friday) |
| thermalDisinfectionSaturday | Switch |  x  | Thermal disinfection (Saturday) |
| thermalDisinfectionSunday | Switch |  x  | Thermal disinfection (Sunday) |
| thermalDisinfectionPermanent | Switch |  x  | Thermal disinfection (Permanent) |
| comfortCoolingMode | Number |   | Comfort cooling mode |
| temperatureComfortCoolingATRelease | Number:Temperature |   | Comfort cooling AT release |
| temperatureComfortCoolingATReleaseTarget | Number:Temperature |   | Comfort cooling AT release target |
| temperatureHeatingLimit | Number:Temperature |   | Temperature Heating Limit |
| comfortCoolingATExcess | Number:Time |   | AT Excess |
| comfortCoolingATUndercut | Number:Time |   | AT undercut |

## Example

Below you can find some example textual configuration for a heatpump with some basic functionallity. This can be extended/adjusted according to your needs and depending on the availability of channels (see list above).

_heatpump.things:_

```java
Thing luxtronikheatpump:heatpump:heatpump "Heatpump" [
    ipAddress="192.168.178.12",
    port="8889",
    refresh="300"
]
```

_heatpump.items:_

```java
Group    gHeatpump   "Heatpump"   <temperature>

Number:Temperature HeatPump_Temp_Outside   "Temperature outside [%.1f °C]"   <temperature> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:temperatureOutside" }
Number:Temperature HeatPump_Temp_Outside_Avg     "Avg. temperature outside [%.1f °C]"  <temperature> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:temperatureOutsideMean" }

Number:Time HeatPump_Hours_Heatpump  "Operating hours [%d h]"  <clock> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:runtimeTotalHeatPump" }
Number:Time HeatPump_Hours_Heating   "Operating hours heating [%d h]"  <clock> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:runtimeTotalHeating" }
Number:Time HeatPump_Hours_Warmwater "Operating hours hot water [%d h]" <clock> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:runtimeTotalHotWater" }

String HeatPump_State_Ext   "State [%s]"   (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:menuStateFull" }

Number HeatPump_heating_operation_mode   "Heating operation mode [%s]"  (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:heatingMode" }
Number HeatPump_heating_temperature   "Heating temperature [%.1f]"  (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:temperatureHeatingParallelShift" }
Number HeatPump_warmwater_operation_mode   "Hot water operation mode [%s]"  (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:hotWaterMode" }
Number HeatPump_warmwater_temperature   "Hot water temperature [%.1f]"  (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:temperatureHotWaterTarget" }
```

_heatpump.sitemap:_

```perl
sitemap heatpump label="Heatpump" {
    Frame label="Heatpump" {
        Text item=HeatPump_State_Ext
        Text item=HeatPump_Temperature_1
        Text item=HeatPump_Outside_Avg
        Text item=HeatPump_Hours_Heatpump
        Text item=HeatPump_Hours_Heating
        Text item=HeatPump_Hours_Warmwater
        Switch item=HeatPump_heating_operation_mode  mappings=[0="Auto", 1="Auxiliary heater", 2="Party", 3="Holiday", 4="Off"]
        Setpoint item=HeatPump_heating_temperature minValue=-10 maxValue=10 step=0.5
        Switch item=HeatPump_warmwater_operation_mode  mappings=[0="Auto", 1="Auxiliary heater", 2="Party", 3="Holiday", 4="Off"]
        Setpoint item=HeatPump_warmwater_temperature minValue=10 maxValue=65 step=1
    }
}
```

## Development Notes

This binding was initially based on the [Novelan/Luxtronik Heat Pump Binding](https://v2.openhab.org/addons/bindings/novelanheatpump1/) for openHAB 1.

Luxtronik control units have an internal webserver which serves a Java applet. This applet can be used to configure some parts of the heat pump. The applet itselves uses a socket connection to fetch and send data to the heatpump.
This socket is also used by this binding. To get some more information on how this socket works you can check out other Luxtronik tools like [Luxtronik2 for NodeJS](https://github.com/coolchip/luxtronik2).

A detailed parameter descriptions for the Java Webinterface can be found in the [Loxwiki](https://www.loxwiki.eu/display/LOX/Java+Webinterface)
