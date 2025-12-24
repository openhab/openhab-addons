# Lambda Heat Pump

This extension adds support for the Lambda Heat Pump modbus protocol as provided by
<https://lambda-wp.at/wp-content/uploads/2025/02/Modbus-Beschreibung-und-Protokoll.pdf>

A Lambda Heat Pump has to be reachable within your network.
If you plan to use the E-Manager part to hand over your PV excess to the heat pump ask Lambda support to configure it to  
E-Meter Kommunikationsart:          ModBus Client
E-Meter Messpunkt:                  E-Eintrag

Other configurations of the E-Manager are not supported (yet).

## Supported Things

This bundle adds the following Thing types to the Modbus binding.
Note, that the things will show up under the Modbus binding.

| Thing           | ThingTypeID         | Description                             |
| --------------- | --------------------| --------------------------------------- |
| General         | general             | General sections Ambient and E-Manager  |
| Heat Pump       | heat-pump           | Heat Pump section                       |
| Boiler          | boiler              | Boiler section                          |
| Buffer          | buffer              | Buffer section                          |
| Solar           | solar               | Solar section                           |
| Heating Circuit | heating-circuit     | Heating Circuit section                 |

A Modbus Bridge has to be installed before installing the above mentioned things.
The binding supports installations with more than one Heat Pump, Boiler, Buffer, Heating Circuit.
For each of these parts you have to provide the Subindex of your Thing in the configurations section, usually using the User Interface.
So if you have two Heating Circuits use 0 for the first and 1 for the second Heating Circuit.
Handling of General System Settings (Base Adress 200) is not supported (yet). Solar functions (Base Address 4000) are now supported.
Some of the registers noted RW in the manual are read only in the binding.

## Discovery

This extension does not support autodiscovery. The things need to be added manually.

## Thing Configuration

You first need to set up a TCP Modbus bridge according to the Modbus documentation.
A typical modbus bridge configuration would look like this:

```java
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1 ]
```

You then add the things of the Lambda Heat Pump system as part of the modbus binding.
Things in this extension will use the selected bridge to connect to the device.

The following parameters are valid for all things:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| refresh   | integer | no       | 30                 | Poll interval in seconds. Increase this if you encounter connection errors |
| maxTries  | integer | no       | 3                  | Number of retries when before giving up reading from this Thing.           |

Heat Pump, Boiler, Buffer and Heating Circuit things use another parameter Subindex to add one or more things of this type:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| Subindex  | integer | yes      | 0                  | Subindex for things of the same Thing type, starting with 0                |

| Thing Type       | Range   | 
| ---------------- | ------- | 
| Heatpump         | 0..2    | 
| Boiler           | 0..4    | 
| Buffer           | 0..4    | 
| Solar            | 0..2    | 
| Heating Circuit  | 0..11   | 

## Channels

Channels within the things are grouped into channel groups.

### General

### Ambient Group

| Channel ID                       | Item Type          | Read only | Description                                                              |
| -------------------------------- | ------------------ | --------- | ------------------------------------------------------------------------ |
| ambient-error-number             | Number             | true      | Ambient Error Number (0 = No error)                                      |
| ambient-operating-state          | Number             | true      | Ambient Operating State (0 = OFF, 1 = AUTOMATIC, 2 = MANUAL, 3 = ERROR)  |
| actual-ambient-temperature       | Number:Temperature | false     | Actual Ambient Temperature                                               |
| average-ambient-temperature      | Number:Temperature | true      | Arithmetic average temperature of the last 60 minutes                    |
| calculated-ambient-temperature   | Number:Temperature | true      | Temperature for calculations in heat distribution modules                |

### E-Manager Group

This group contains parameters signaling the PV excess to the heat pump.

| Channel ID                       | Item Type          | Read only | Description                                                                            |
| -------------------------------- | ------------------ | --------- | -------------------------------------------------------------------------------------- |
| emanager-error-number            | Number             | true      | E-Manager Error Number (0 = No error)                                                  |
| emanager-operating-state         | Number             | true      | E-Manager Operating State (0 = OFF, 1 = AUTOMATIC, 2 = MANUAL, 3 = ERROR 4 = OFFLINE   |
| actual-power                     | Number:Power       | false     | Actual excess power 0 W .. 65535 W                                                     |
| actual-power-signed              | Number:Power       | false     | Actual excess power -32768 W .. 32767 W                                                |
| actual-power-consumption         | Number:Power       | true      | Power consumption of heat-pump (only valid when Betriebsart: Automatik, 0 W otherwise) |
| power-consumption-setpoint       | Number:Power       | false     | Power consumption setpoint for heat pump 1                                             |

### Heat Pump Group

This group contains general operational information about the heat pump itself.

| Channel ID                     | Item Type                 | Read only | Description                                                             |
| -------------------------------| --------------------------| --------- | ----------------------------------------------------------------------- |
| heat-pump-error-state          | Number                    | true      | Error state  (0 = NONE, 1 = MESSAGE, 2 = WARNING, 3 = ALARM, 4 = FAULT) |
| heat-pump-error-number         | Number                    | true      | Error number: scrolling through all active error numbers (1..99)        |
| heat-pump-state                | Number                    | true      | State: See Modbus description manual, link above                        |
| heat-pump-operating-state      | Number                    | true      | Operating State: See Modbus description manual, link above              |
| heat-pump-t-flow               | Number:Temperature        | true      | Flow line termperature                                                  |
| heat-pump-t-return             | Number:Temperature        | true      | Return line temperature                                                 |
| heat-pump-vol-sink             | Number:VolumetricFlowRate | true      | Volume flow heat sink                                                   |
| heat-pump-t-eqin               | Number:Temperature        | true      | Energy source inlet temperature                                         |
| heat-pump-t-eqout              | Number:Temperature        | true      | Energy source outlet temperature                                        |
| heat-pump-vol-source           | Number:VolumetricFlowRate | true      | Volume flow energy source                                               |
| heat-pump-compressor-rating    | Number                    | true      | Compressor unit rating                                                  |
| heat-pump-qp-heating           | Number:Power              | true      | Actual heating capacity                                                 |
| heat-pump-fi-power-consumption | Number:Power              | true      | Frequency inverter actual power consumption                             |
| heat-pump-cop                  | Number                    | true      | Coefficient of performance                                              |
| heat-pump-request-password     | Number                    | false     | Password register to release modbus request registers                   |
| heat-pump-request-type         | Number:                   | false     | Request Type                                                            |
| heat-pump-request-t-flow       | Number:Temperature        | false     | Requested flow line termperature                                        |
| heat-pump-request-t-return     | Number:Temperature        | false     | Requested return line temperature                                       |
| heat-pump-request-heat-sink    | Number:Temperature        | false     | Requested temperature difference between flow and return line           |
| heat-pump-relais-state         | Number:Temperature        | true      | Heatpump Relais State for 2nd heating stage                             |
| heat-pump-vdae                 | Number:Energy             | true      | Accumulated electrical energy consumption of compressor unit            |
| heat-pump-vdaq                 | Number:Energy             | true      | Accumulated thermal energy output of compressor unit                    |

### Boiler Group

This group contains information about the boiler for the water for domestic use / tap water / washwater.

| Channel ID                            | Item Type          | Read only | Description                                                         |
| ------------------------------------- | ------------------ | --------- | ------------------------------------------------------------------- |
| boiler-error-number                   | Number             | true      | Boiler Error Number (0 = No error)                                  |
| boiler-operating-state                | Number             | true      | Boiler Operating State: See Modbus description manual, link above   |
| boiler-actual-high-temperature        | Number:Temperature | true      | Actual temperature boiler high sensor                               |
| boiler-actual-low-temperature         | Number:Temperature | true      | Actual temperature boiler low sensor                                |
| maximum-boiler-temperature            | Number:Temperature | false     | Setting for maximum boiler temperature (min = 25.0°C; max = 65.0°C) |

### Buffer Group

This group contains information about the buffer for the heating circuit.

| Channel ID                                      | Item Type          | Read only | Description                                                          |
| ------------------------------------------------| ------------------ | --------- | ---------------------------------------------------------------------|
| buffer-error-number                             | Number             | true      | Buffer Error Number (0 = No error)                                   |
| buffer-operating-state                          | Number             | true      | Buffer Operating State: See Modbus description manual, link above    |
| buffer-actual-high-temperature                  | Number:Temperature | true      | Actual temperature buffer high sensor                                |
| buffer-actual-low-temperature                   | Number:Temperature | true      | Actual temperature buffer low sensor                                 |
| buffer-actual-modbus-temperature                | Number:Temperature | false     | Actual temperature set via modbus                                    |
| buffer-request-type                             | Number             | false     | Request Type: See Modbus description manual, link above              |
| buffer-request-flow-line-temperature            | Number:Temperature | false     | Requested flow line temperature                                      |
| buffer-request-return-line-temperature          | Number:Temperature | false     | Requested return line temperature                                    |
| buffer-request-heat-sink-temperature-difference | Number:Temperature | false     | Requested temperature difference between flow line and return line   |
| buffer-request-heating-capacity                 | Number:Power       | false     | Requested capacity                                                   |
| maximum-buffer-temperature                      | Number:Temperature | false     | Setting for maximum buffer temperature )                             |

### Solar Group

This group contains information about the solar thermic component.

| Channel ID                     | Item Type          | Read only | Description                                                         |
| ------------------------------ | ------------------ | --------- | ------------------------------------------------------------------- |
| solar-error-number            | Number             | true      | Solar Error Number (0 = No error)                                  |
| solar-operating-state         | Number             | true      | Solar Operating State: See Modbus description manual, link above   |
| solar-collector-temperature   | Number:Temperature | true      | Temperature of the solar collector                                |
| solar-storage-temperature     | Number:Temperature | true      | Temperature of the solar storage                                  |
| solar-pump-speed             | Number             | true      | Speed of the solar pump                                          |
| solar-heat-quantity          | Number:Energy      | true      | Heat quantity produced by solar                                  |
| solar-power-output           | Number:Power       | true      | Current power output of solar                                    |
| solar-operating-hours        | Number             | true      | Operating hours of solar component                               |

### Heating Circuit Group

This group contains general operational information about the heating circuit.

| Channel ID                                     | Item Type          | Read only | Description                                                 |
| ---------------------------------------------- | -------------------| --------- | ----------------------------------------------------------- |
| heating-circuit-error-number                   | Number             | true      | Error Number (0 = No error)                                 |
| heating-circuit-operating-state                | Number             | true      | Operating State: See Modbus description manual, link above  |
| heating-circuit-flow-line-temperature          | Number:Temperature | true      | Actual temperature flow line sensor                         |
| heating-circuit-return-line-temperature        | Number:Temperature | true      | Actual temperature return line sensor                       |
| heating-circuit-room-device-temperature        | Number:Temperature | false     | Actual temperature room device sensor                       |
| heating-circuit-setpoint-flow-line-temperature | Number:Temperature | false     | Setpoint temperature flow line                              | 
| heating-circuit-operating-mode                 | Number             | false     | Operating Mode: See Modbus description manual, link above   |
| heating-circuit-target-temperature-flow-line   | Number:Temperature | false     | Setpoin temperature flow line  (min = 15.0°C; max = 65.0°C) |
| heating-circuit-offset-flow-line-temperature   | Number:Temperature | false     | Setting for flow line temperature setpoint offset           |
| heating-circuit-room-heating-temperature       | Number:Temperature | false     | Setting for heating mode room setpoint temperature          |
| heating-circuit-room-cooling-temperature       | Number:Temperature | false     | Setting for cooling mode room setpoint temperature          |

## Full Example

### `demo.things` Example

```java
Bridge modbus:tcp:Bridge "Lambda Modbus TCP Bridge" [ host="192.168.223.83", port=502, id=1, enableDiscovery=false ] {
    Thing general lambdageneral "Lambda General" (modbus:tcp:Bridge) [ refresh=60, subindex=0 ] 
    Thing heat-pump lambdaheat-pump "Lambda Heatpump" (modbus:tcp:Bridge) [ refresh=60, subindex=0 ]
    Thing boiler lambdaboiler "Lambda Boiler" (modbus:tcp:Bridge) [ refresh=60, subindex=0 ]
    Thing buffer lambdabuffer "Lambda Buffer" (modbus:tcp:Bridge) [ refresh=60, subindex=0 ]
    Thing solar lambdasolar "Lambda Solar" (modbus:tcp:Bridge) [ refresh=60, subindex=0 ]
}
```

### Items Lambda Boiler

```java
Number              lambdaboiler_errornumber               "Boiler Error Number"               (lambdaboiler)  { channel="modbus:boiler:Lambda_Bridge:lambdaboiler:boiler-group#boiler-error-number" }
Number              lambdaboiler_operatingstate            "Boiler Operating State"            (lambdaboiler)  { channel="modbus:boiler:Lambda_Bridge:lambdaboiler:boiler-group#boiler-operating-state" }
Number:Temperature  lambdaboiler_actualhightemperature     "Boiler Actual High Temperature"    (lambdaboiler)  { channel="modbus:boiler:Lambda_Bridge:lambdaboiler:boiler-group#boiler-actual-high-temperature" }
Number:Temperature  lambdaboiler_actuallowtemperature      "Boiler Actual Low Temperature"     (lambdaboiler)  { channel="modbus:boiler:Lambda_Bridge:lambdaboiler:boiler-group#boiler-actual-low-temperature" }
Number:Temperature  lambdaboiler_maximumboilertemperature  "Maximum Boiler Temperature"        (lambdaboiler)  { channel="modbus:boiler:Lambda_Bridge:lambdaboiler:boiler-group#maximum-boiler-temperature" }
```

### Items Lambda Buffer

```java
Number              lambdabuffer_errornumber                    "Buffer Error Number"                       (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-error-number" }
Number              lambdabuffer_operatingstate                 "Buffer Operating State"                    (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-operating-state" }
Number:Temperature  lambdabuffer_actualhightemperature          "Buffer Actual High Temperature"            (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-actual-high-temperature" }
Number:Temperature  lambdabuffer_actuallowtemperature           "Buffer Actual Low Temperature"             (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-actual-low-temperature" }
Number:Temperature  lambdabuffer_actualmodbustemperature       "Actual Modbus Temperature"                  (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-actual-modbus-temperature" }
Number              lambdabuffer_requesttype                   "Request Type"                               (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-request-type" }
Number:Temperature  lambdabuffer_requestflowlinetemperature    "Request Flow Line Temperature"              (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-request-flow-line-temperature" }
Number:Temperature  lambdabuffer_requestreturnlinetemperature  "Request Return Line Temperature"            (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-request-return-line-temperature" }
Number:Temperature  lambdabuffer_requestheatsinktemperature    "Requested Heat Sink Temperature Difference" (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-request-heat-sink-temperature" }
Number:Power        lambdabuffer_requestheatingcapacity              "Requested Heating Capacity"                 (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#buffer-request-heating-capacity" }
Number:Temperature  lambdabuffer_maximumbuffertemperature      "Maximum Buffer Temperature"                 (lambdabuffer)  { channel="modbus:buffer:Lambda_Bridge:lambdabuffer:buffer-group#maximum-buffer-temperature" }
```

### Items Lambda General

```java
Number              lambdaambient_operatingstate               "Ambient Operating State"             (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:ambient-group#ambient-operating-state" }
Number              lambdaambient_errornumber                  "Ambient Error Number"                (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:ambient-group#ambient-error-number" }
Number:Temperature  lambdaambient_actualambienttemperature     "Ambient Actual Temperature"          (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:ambient-group#actual-ambient-temperature" }
Number:Temperature  lambdaambient_averageambienttemperature    "Ambient Average Temperature"         (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:ambient-group#average-ambient-temperature" }
Number:Temperature  lambdaambient_calculatedambienttemperature "Ambient Calculated Temperature"      (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:ambient-group#calculated-ambient-temperature" }
Number              lambdaemanager_operatingstate              "EManager Operating State"            (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:emanager-group#emanager-operating-state" }
Number              lambdaemanager_errornumber                 "EManager Error Number"               (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:emanager-group#emanager-error-number" }
Number:Power        lambdaemanager_actualpower                 "EManager Actual Power"               (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:emanager-group#actual-power" }
Number:Power        lambdaemanager_actualpowerconsumption      "EManager Actual Power Consumption"   (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:emanager-group#actual-power-consumption" }
Number:Power        lambdaemanager_powerconsumptionsetpoint    "EManager Power Consumption Setpoint" (lambdageneral)  { channel="modbus:general:Lambda_Bridge:lambdageneral:emanager-group#power-consumption-setpoint" }

```

### Items Heatingcircuit

```java
Number                     lambdaheatingcircuit_errornumber                  "Heating Circuit Error Number"                    (lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-error-number" }
Number                     lambdaheatingcircuit_operatingstate               "Heating Circuit Operating State"                 (lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-operating-state" }
Number:Temperature         lambdaheatingcircuit_flowlinetemperature          "Heating Circuit Flow Line Temperature"           (lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-flow-line-temperature" }
Number:Temperature         lambdaheatingcircuit_returnlinetemperature        "Heating Circuit Return Line Temperature"         (lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-return-line-temperature" }
Number:Temperature         lambdaheatingcircuit_roomdevicetemperature        "Heating Circuit Room Device Temperature"         (lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-room-device-temperature" } 
Number:Temperature         lambdaheatingcircuit_setpointflowlinetemperature  "Heating Circuit Setpoint Flow Line Temperature"  (lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-setpoint-flow-line-temperature" }
Number                     lambdaheatingcircuit_operatingmode                "Heating Circuit Operating Mode"                  (lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-operating-mode" }
Number:Temperature         lambdaheatingcircuit_offsetflowlinetemperature        "Heating Circuit Offset Flow Line Temperature"(lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-offset-flow-line-temperature" }
Number:Temperature         lambdaheatingcircuit_roomheatingtemperature        "Heating Circuit Room Heating Temperature"       (lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-room-heating-temperature" } 
Number:Temperature         lambdaheatingcircuit_roomcoolingtemperature        "Heating Circuit Room Cooling Temperature"       (lambdaheatingcircuit)       { channel="modbus:heating-circuit:Lambda_Bridge:lambdaheatingcircuit:heating-circuit-group#heating-circuit-room-cooling-temperature" }
```

### Items Lambda Solar

```java
Number              lambdasolar_errornumber               "Solar Error Number"               (lambdasolar)  { channel="modbus:solar:Lambda_Bridge:lambdasolar:solar-group#solar-error-number" }
Number              lambdasolar_operatingstate            "Solar Operating State"            (lambdasolar)  { channel="modbus:solar:Lambda_Bridge:lambdasolar:solar-group#solar-operating-state" }
Number:Temperature  lambdasolar_collectortemperature      "Solar Collector Temperature"      (lambdasolar)  { channel="modbus:solar:Lambda_Bridge:lambdasolar:solar-group#solar-collector-temperature" }
Number:Temperature  lambdasolar_storagetemperature        "Solar Storage Temperature"        (lambdasolar)  { channel="modbus:solar:Lambda_Bridge:lambdasolar:solar-group#solar-storage-temperature" }
Number              lambdasolar_pumpspeed                 "Solar Pump Speed"                 (lambdasolar)  { channel="modbus:solar:Lambda_Bridge:lambdasolar:solar-group#solar-pump-speed" }
Number:Energy       lambdasolar_heatquantity              "Solar Heat Quantity"              (lambdasolar)  { channel="modbus:solar:Lambda_Bridge:lambdasolar:solar-group#solar-heat-quantity" }
Number:Power        lambdasolar_poweroutput               "Solar Power Output"               (lambdasolar)  { channel="modbus:solar:Lambda_Bridge:lambdasolar:solar-group#solar-power-output" }
Number              lambdasolar_operatinghours            "Solar Operating Hours"            (lambdasolar)  { channel="modbus:solar:Lambda_Bridge:lambdasolar:solar-group#solar-operating-hours" }
```

### Items Lambda Heatpump

```java
Number                     lambdaheatpump_errorstate          "Heatpump Error State"                                   (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-error-state" }
Number                     lambdaheatpump_errornumber         "Heatpump Error Number"                                  (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-error-number" }
Number                     lambdaheatpump_state               "Heatpump State"                                         (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-state" }
Number                     lambdaheatpump_operatingstate      "Heatpump Operating State"                               (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-operating-state" }
Number:Temperature         lambdaheatpump_tflow               "Heatpump Flow Line Temperature"                         (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-t-flow" }
Number:Temperature         lambdaheatpump_treturn             "Heatpump Return Line Temperature"                       (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-t-return" }
Number:VolumetricFlowRate  lambdaheatpump_volsink             "Heatpump Volume Flow Heat Sink"                         (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-vol-sink" }
Number:Temperature         lambdaheatpump_teqin               "Heatpump Energy Source Inlet Temperature"               (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-t-eqin" }
Number:Temperature         lambdaheatpump_teqout              "Heatpump Energy Source Outlet Temperature"              (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-t-eqout" }
Number:VolumetricFlowRate  lambdaheatpump_volsource           "Heatpump Volume Flow Energy Source"                     (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-vol-source" }
Number                     lambdaheatpump_compressorrating    "Heatpump Compressor Rating"                             (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-compressor-rating" }
Number:Power               lambdaheatpump_qpheating           "Heatpump Actual Heating Capacity"                       (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-qp-heating" }
Number:Power               lambdaheatpump_fipowerconsumption  "Heatpump Frequency inverter Actual Power Consumption"   (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-fi-power-consumption" }
Number                     lambdaheatpump_cop                 "Heatpump COP"                                           (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-cop" }
Number                     lambdaheatpump_requestpassword     "Heatpump Request Password"                              (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-request-password" }
Number                     lambdaheatpump_requesttype         "Heatpump Request Type"                                  (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-request-type" }
Number                     lambdaheatpump_requesttflow        "Heatpump Requested Flow Line Temperature"               (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-request-t-flow" }
Number                     lambdaheatpump_requesttreturn      "Heatpump Requested Return Line Temperature"             (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-request-t-return" }
Number                     lambdaheatpump_requestheatsink     "Heatpump Requested Heat Sink Temperature"               (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-request-heat-sink" }
Number                     lambdaheatpump_relaisstate         "Heatpump Relais State"                                  (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-relais-state" }
Number:Energy              lambdaheatpump_vdae                "Heatpump Accumulated Electrical Energy consumption"     (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-vdae" }
Number:Energy              lambdaheatpump_vdaq                "Heatpump Accumulated Thermical Energy consumption"      (lambdaheatpump)       { channel="modbus:heat-pump:Lambda_Bridge:lambdaheatpumpheat-pump-group#heat-pump-vdaq" }
```

### Example: (DSL) Send Power value the E-Manager of the Lambda Heat Pump

'''
// Sending Value to Heatpump
// Script has to send a value about every 30 seconds, for example with cron settings.
// Calculate power_to_heat-pump using your data provided by the PV system.
// var int power_to_heat-pump =  ((lambdaemanager_actualpowerconsumption.state as Number) - (PW_Battery.state as Number) - (PW_Grid.state as Number)).intValue

var int power_to_heat-pump = 1000  

  lambdaemanager_actualpower.sendCommand(power_to_heat-pump)
'''

