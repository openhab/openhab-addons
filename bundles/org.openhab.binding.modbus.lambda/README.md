# Lambda Heat Pump

This extension adds support for the Lambda Heat Pump modbus protocol as provided by
<https://lambda-wp.at/wp-content/uploads/2024/11/Modbus-Protokoll-und-Beschreibung.pdf >

A Lambda Heat Pump has to be reachable within your network.
If you plan to use the E-Manager part to hand over your PV excess to the heat pump ask Lambda support to 
configure it to  
E-Meter Kommunikationsart:          ModBus Client
E-Meter Messpunkt:                  E-Eintrag

Other configurations of the E-Manager are not supported (yet).

## Supported Things

This bundle adds the following thing types to the Modbus binding.
Note, that the things will show up under the Modbus binding.

| Thing                  | ThingTypeID         | Description                             |
| ---------------------- | --------------------| --------------------------------------- |
| Lambda General         | lambda-general      | General sections Ambient and E-Manager  |
| Lambda Heatpump        | lambda-heatpump     | Heatpump section                        |
| Lambda Boiler          | lambda-boiler       | Boiler section                          |
| Lambda Buffer          | lambda-buffer       | Buffer section                          |
| Lambda Heating Circuit | lambda-heating      | Heating circuit section                 |

A Modbus Bridge has to be installed before installing the above mentioned things.
The binding supports installations with more than one Heat Pump, Boiler, Buffer, Heating Circuit.
For each of these parts you have to provide the Subindex of your thing in the configurations section, 
usually using the User Interface. So if you have two Heating Circuits use 0 for the first and 1 for 
the second Heating Circuit.
Handling of General System Settings (Base Adress 200) and Solar (Base Adress 4000) are not supported (yet).
Some of the registers noted RW in the manual are read only in the binding.

## Discovery

This extension does not support autodiscovery. The things need to be added manually.

A typical modbus bridge configuration would look like this:

```java
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1 ]
```

## Thing Configuration

You first need to set up a TCP Modbus bridge according to the Modbus documentation.
You then add the things of the Lambda Heat Pump system as part of the modbus binding.
Things in this extension will use the selected bridge to connect to the device.

The following parameters are valid for all things:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| refresh   | integer | no       | 30                 | Poll interval in seconds. Increase this if you encounter connection errors |
| maxTries  | integer | no       | 3                  | Number of retries when before giving up reading from this thing.           |

The other things use another parameter Subindex to add one or more things of this type:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| Subindex  | integer | yes      | 0                  | Subindex for things of the same thing type, starting with 0                |

| Thing Type             | Range   | 
| ---------------------- | ------- | 
| Lambda Heatpump        | 0..2    | 
| Lambda Boiler          | 0..4    | 
| Lambda Buffer          | 0..4    | 
| Lambda Heating Circuit | 0..11   | 

## Channels

Channels within the things are grouped into channel groups.

### General Ambient Group

| Channel ID                       | Item Type          | Read only | Description                                                              |
| -------------------------------- | ------------------ | --------- | ------------------------------------------------------------------------ |
| ambient-error-number             | Number             | true      | Ambient Error Number (0 = No error)                                      |
| ambient-operating-state          | Number             | true      | Ambient Operating State (0 = OFF, 1 = AUTOMATIC, 2 = MANUAL, 3 = ERROR)  |
| actual-ambient-temperature       | Number:Temperature | false     | Actual Ambient Temperature (min = -50.0°C); max = 80.0°                  |
| average-ambient-temperature      | Number:Temperature | true      | Arithmetic average temperature of the last 60 minutes                    |
| calculated-ambient-temperature   | Number:Temperature | true      | Temperature for calculations in heat distribution modules                |

### Lambda General: General E-Manager Group

This group contains parameters signaling the PV excess to the heat pump.

| Channel ID                       | Item Type          | Read only | Description                                                                              |
| -------------------------------- | ------------------ | --------- | --------------------------------------------------------------------------------------- |
| emanager-error-number            | Number             | true      | E-Manager Error Number (0 = No error)                                                   |
| emanager-operating-state         | Number             | true      | E-Manager Operating State (0 = OFF, 1 = AUTOMATIC, 2 = MANUAL, 3 = ERROR 4 = OFFLINE    |
| actual-power                     | Number:Power       | false     | Actual excess power -32768 W .. 32767 W                                                 |
| actual-power-consumption         | Number:Power       | true      | Power consumption of heatpump (only valid when Betriebsart: Automatik, 0 W otherwise)   |
| power-consumption-setpoint       | Number:Power       | false     | Power consumption setpoint for heat pump 1                                              |

### Heat Pump 1 Group

This group contains general operational information about the heat pump itself.

| Channel ID                     | Item Type                | Read only | Description                                                             |
| -------------------------------| -------------------------| --------- | ----------------------------------------------------------------------- |
| heatpump-error-state          | Number                    | true      | Error state  (0 = NONE, 1 = MESSAGE, 2 = WARNING, 3 = ALARM, 4 = FAULT) |
| heatpump-error-number         | Number                    | true      | Error number: scrolling through all active error numbers (1..99)        |
| heatpump-state                | Number                    | true      | State: See Modbus description manual, link above                        |
| heatpump-operating-state      | Number                    | true      | Operating State: See Modbus description manual, link above              |
| heatpump-t-flow               | Number:Temperature        | true      | Flow line termperature                                                  |
| heatpump-t-return             | Number:Temperature        | true      | Return line temperature                                                 |
| heatpump-vol-sink             | Number:VolumetricFlowRate | true      | Volume flow heat sink                                                   |
| heatpump-t-eqin               | Number:Temperature        | true      | Energy source inlet temperature                                         |
| heatpump-t-eqout              | Number:Temperature        | true      | Energy source outlet temperature                                        |
| heatpump-vol-source           | Number:VolumetricFlowRate | true      | Volume flow energy source                                               |
| heatpump-compressor-rating    | Number                    | true      | Compressor unit rating                                                  |
| heatpump-qp-heating           | Number:Power              | true      | Actual heating capacity                                                 |
| heatpump-fi-power-consumption | Number:Power              | true      | Frequency inverter actual power consumption                             |
| heatpump-cop                  | Number                    | true      | Coefficient of performance                                              | 
| heatpump-vdae                 | Number:Energy             | true      | Accumulated electrical energy consumption of compressor unit            |
| heatpump-vdaq                 | Number:Energy             | true      | Accumulated thermal energy output of compressor unit                    |
| heatpump-set-error-quit       | Number                    | false     | Set Error Quit (1 = Quit all active heat pump errors                    |

### Lambda Boiler: Boiler Group

This group contains information about the boiler for the water for domestic use / tap water / washwater.

| Channel ID                        | Item Type          | Read only | Description                                                        |
| --------------------------------- | ------------------ | --------- | ------------------------------------------------------------------ |
| boiler-error-number               | Number             | true      | Boiler Error Number(0 = No error)                                  |
| boiler-operating-state            | Number             | true      | Boiler Operating State: See Modbus description manual, link above  |
| boiler-actual-high-temperature    | Number:Temperature | true      | Actual temperature boiler high sensor                              |
| boiler-actual-low-temperature     | Number:Temperature | true      | Actual temperature boiler low sensor                               |
| boiler-maximum-boiler-temperature | Number:Temperature | false     | Setting for maximum boiler temperature (min = 25.0°C; max = 65.0°C)|

### Lambda Buffer: Buffer Group

This group contains information about the buffer for the heating circuit.

| Channel ID                             | Item Type          | Read only | Description                                                            |
| -------------------------------------- | ------------------ | --------- | ---------------------------------------------------------------------- |
| buffer-error-number                    | Number             | true      | Buffer Error Number (0 = No error)                                     |
| buffer-operating-state                 | Number             | true      | Buffer Operating State: See Modbus description manual, link above      |
| buffer-actual-high-temperature         | Number:Temperature | true      | Actual temperature buffer high sensor                                  |
| buffer-actual-low-temperature          | Number:Temperature | true      | Actual temperature buffer low sensor                                   |
| buffer-maximum-buffer-temperature      | Number:Temperature | false     | Setting for maximum buffer temperature (min = 25.0°C; max = 65.0°C)    |


### Lambda Heating: Heating Circuit Group

This group contains general operational information about the heating circuit.

| Channel ID                                    | Item Type          | Read only | Description                                                                     |
| --------------------------------------------- | -------------------| --------- | ------------------------------------------------------------------------------- |
| heatingcircuit-error-number                   | Number             | true      | Error Number (0 = No error)                                                     |
| heatingcircuit-operating-state                | Number             | true      | Operating State: See Modbus description manual, link above|                     |
| heatingcircuit-flow-line-temperature          | Number:Temperature | true      | Actual temperature flow line sensor                                             |
| heatingcircuit-return-line-temperature        | Number:Temperature | true      | Actual temperature return line sensor                                           |
| heatingcircuit-room-device-temperature        | Number:Temperature | false     | Actual temperature room device sensor (min = -29.9°C; max = 99.9°C)             |
| heatingcircuit-setpoint-flow-line-temperature | Number:Temperature | false     | Setpoint temperature flow line (min = 15.0°C; max = 65.0°C)                     | 
| heatingcircuit-operating-mode                 | Number             | false     | Operating Mode: See Modbus description manual, link above                       |
| heatingcircuit-offset-flow-line-temperature   | Number:Temperature | false     | Setting for flow line temperature setpoint offset(min = -10.0K; max = 10.0K)    |
| heatingcircuit-room-heating-temperature       | Number:Temperature | false     | Setting for heating mode room setpoint temperature(min = 15.0°C; max = 40.0 °C) |
| heatingcircuit-room-cooling-temperature       | Number:Temperature | false     | Setting for cooling mode room setpoint temperature(min = 15.0°C; max = 40.0 °C) |

## Full Example

### Things
```java
Bridge modbus:tcp:Lambda_Bridge "Lambda Modbus TCP Bridge" [ host="192.168.223.83", port=502, id=1, enableDiscovery=false ] {
    Thing lambda-general lambdageneral "Lambda General" (modbus:tcp:Lambda_Bridge) [ refresh=60, subindex=0 ] 
    Thing lambda-heatpump lambdaheatpump "Lambda Heatpump" (modbus:tcp:Lambda_Bridge) [ refresh=60, subindex=0 ]
    Thing lambda-boiler lambdaboiler "Lambda Boiler" (modbus:tcp:Lambda_Bridge) [ refresh=60, subindex=0 ]
    Thing lambda-buffer lambdabuffer "Lambda Buffer" (modbus:tcp:Lambda_Bridge) [ refresh=60, subindex=0 ]
    Thing lambda-heatingcircuit lambdaheatingcircuit "Lambda Heating Circuit" (modbus:tcp:Lambda_Bridge) [ refresh=60, subindex=0 ]
}
```

### Items Lambda General
```java
Number              lambdaambient_operatingstate               "Ambient Operating State"             (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:ambient-group#ambient-operating-state" }
Number              lambdaambient_errornumber                  "Ambient Error Number"                (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:ambient-group#ambient-error-number" }
Number:Temperature  lambdaambient_actualambienttemperature     "Ambient Actual Temperature"          (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:ambient-group#actual-ambient-temperature" }
Number:Temperature  lambdaambient_averageambienttemperature    "Ambient Average Temperature"         (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:ambient-group#average-ambient-temperature" }
Number:Temperature  lambdaambient_calculatedambienttemperature "Ambient Calculated Temperature"      (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:ambient-group#calculated-ambient-temperature" }
Number              lambdaemanager_operatingstate              "EManager Operating State"            (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:emanager-group#emanager-operating-state" }
Number              lambdaemanager_errornumber                 "EManager Error Number"               (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:emanager-group#emanager-error-number" }
Number:Power        lambdaemanager_actualpower                 "EManager Actual Power"               (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:emanager-group#actual-power" }
Number:Power        lambdaemanager_actualpowerconsumption      "EManager Actual Power Consumption"   (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:emanager-group#actual-power-consumption" }
Number:Power        lambdaemanager_powerconsumptionsetpoint    "EManager Power Consumption Setpoint" (lambdageneral)  { channel="modbus:lambda-general:Lambda_Bridge:lambdageneral:emanager-group#power-consumption-setpoint" }
```

### Items Lambda Heatpump
```java
Number                     lambdaheatpump_errorstate          "Heatpump Error State"                                 (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-error-state" }
Number                     lambdaheatpump_errornumber         "Heatpump Error Number"                                (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-error-number" }
Number                     lambdaheatpump_state               "Heatpump State"                                       (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-state" }
Number                     lambdaheatpump_operatingstate      "Heatpump Operating State"                             (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-operating-state" }
Number:Temperature         lambdaheatpump_tflow               "Heatpump Flow Line Temperature"                       (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-t-flow" }
Number:Temperature         lambdaheatpump_treturn             "Heatpump Return Line Temperature"                     (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-t-return" }
Number:VolumetricFlowRate  lambdaheatpump_volsink             "Heatpump Volume Flow Heat Sink"                       (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-vol-sink" }
Number:Temperature         lambdaheatpump_teqin               "Heatpump Energy Source Inlet Temperature"             (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-t-eqin" }
Number:Temperature         lambdaheatpump_teqout              "Heatpump Energy Source Outlet Temperature"            (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-t-eqout" }
Number:VolumetricFlowRate  lambdaheatpump_volsource           "Heatpump Volume Flow Energy Source"                   (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-vol-source" }
Number                     lambdaheatpump_compressorrating    "Heatpump Compressor Rating"                           (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-compressor-rating" }
Number:Power               lambdaheatpump_qpheating           "Heatpump Actual Heating Capacity"                     (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-qp-heating" }
Number:Power               lambdaheatpump_fipowerconsumption  "Heatpump Frequency inverter Actual Power Consumption" (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-fi-power-consumption" }
Number                     lambdaheatpump_cop                 "Heatpump COP"                                         (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-cop" }
Number:Energy              lambdaheatpump_vdae                "Heatpump Accumulated Electrical Energy consumption"   (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-vdae" }
Number:Energy              lambdaheatpump_vdaq                "Heatpump Accumulated Thermical Energy consumption"    (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-vdaq" }
Number                     lambdaheatpump_seterrorquit        "Heatpump Set Error Quit"                              (lambdaheatpump)       { channel="modbus:lambda-heatpump:Lambda_Bridge:lambdaheatpump:heatpump-group#heatpump-set-error-quit" }
```

### Items Lambda Boiler
```java
Number              lambdaboiler_operatingstate            "Boiler Operating State"            (lambdaboiler)  { channel="modbus:lambda-boiler:Lambda_Bridge:lambdaboiler:boiler-group#boiler-operating-state" }
Number              lambdaboiler_errornumber               "Boiler Error Number"               (lambdaboiler)  { channel="modbus:lambda-boiler:Lambda_Bridge:lambdaboiler:boiler-group#boiler-error-number" }
Number:Temperature  lambdaboiler_actualhightemperature     "Boiler Actual High Temperature"    (lambdaboiler)  { channel="modbus:lambda-boiler:Lambda_Bridge:lambdaboiler:boiler-group#boiler-actual-high-temperature" }
Number:Temperature  lambdaboiler_actuallowtemperature      "Boiler Actual Low Temperature"     (lambdaboiler)  { channel="modbus:lambda-boiler:Lambda_Bridge:lambdaboiler:boiler-group#boiler-actual-low-temperature" }
Number:Temperature  lambdaboiler_maximumboilertemperature  "Maximum Boiler Temperature"        (lambdaboiler)  { channel="modbus:lambda-boiler:Lambda_Bridge:lambdaboiler:boiler-group#boiler-maximum-boiler-temperature" }
```

### Items Lambda Buffer
```java
Number              lambdabuffer_operatingstate            "Buffer Operating State"            (lambdabuffer)  { channel="modbus:lambda-buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-operating-state" }
Number              lambdabuffer_errornumber               "Buffer Error Number"               (lambdabuffer)  { channel="modbus:lambda-buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-error-number" }
Number:Temperature  lambdabuffer_actualhightemperature     "Buffer Actual High Temperature"    (lambdabuffer)  { channel="modbus:lambda-buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-actual-high-temperature" }
Number:Temperature  lambdabuffer_actuallowtemperature      "Buffer Actual Low Temperature"     (lambdabuffer)  { channel="modbus:lambda-buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-actual-low-temperature" }
Number:Temperature  lambdabuffer_maximumbuffertemperature  "Maximum Buffer Temperature"        (lambdabuffer)  { channel="modbus:lambda-buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-maximum-buffer-temperature" }
```


### Items Heatingcircuit
```java
Number                     lambdaheatingcircuit_errornumber                  "Heatingcircuit Error Number"                    (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-error-number" }
Number                     lambdaheatingcircuit_operatingstate               "Heatingcircuit Operating State"                 (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-operating-state" }
Number:Temperature         lambdaheatingcircuit_flowlinetemperature          "Heatingcircuit Flow Line Temperature"           (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-flow-line-temperature" }
Number:Temperature         lambdaheatingcircuit_returnlinetemperature        "Heatingcircuit Return Line Temperature"         (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-return-line-temperature" }
Number:Temperature         lambdaheatingcircuit_roomdevicetemperature        "Heatingcircuit Room Device Temperature"         (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-room-device-temperature" }
Number:Temperature         lambdaheatingcircuit_setpointflowlinetemperature  "Heatingcircuit Setpoint Flow Line Temperature"  (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-setpoint-flow-line-temperature" }
Number                     lambdaheatingcircuit_operatingmode                "Heatingcircuit Operating Mode"                  (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-operating-mode" }
Number:Temperature         lambdaheatingcircuit_offsetflowlinetemperature    "Heatingcircuit Offset Flow Line Temperature"    (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-offset-flow-line-temperature" }
Number:Temperature         lambdaheatingcircuit_roomheatingtemperature       "Heatingcircuit Room Heating Temperature"        (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-room-heating-temperature" }
Number:Temperature         lambdaheatingcircuit_roomcoolingtemperature       "Heatingcircuit Room Cooling Temperature"        (lambdaheatingcircuit)       { channel="modbus:lambda-heatingcircuit:Lambda_Bridge:lambdaheating:heatingcircuit-group#heatingcircuit-room-cooling-temperature" }
```

### Example: (DSL) Send Power value the E-Manager of the Lambda Heat Pump
// Sending Value to Heatpump
// Script has to send a value about every 30 seconds, for example with cron settings
// Calculate power_to_heatpump using your data provided by the PV system
// var int power_to_heatpump =  ((lambdaemanager_actualpowerconsumption.state as Number) - (PV_Battery.state as Number) - (PV_Grid.state as Number)).intValue

var int power_to_heatpump = 1000  

  lambdahp_actual_power.sendCommand(power_to_heatpump)


