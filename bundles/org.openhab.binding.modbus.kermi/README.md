# Kermi Heat pump

Integrates the X-Center Device (X-Center Pro) of Kermi Heat pump.
Kermi X-Center & other attached devices (in progress) are integrated into the Modbus Binding.

This binding was tested and developed with Kermi x-change dynamic pro heat pump (build 2023).

Hint: This binding _may_ also work with devices from "Bösch" in Austria, which is a sub-brand of Kermi, they are nearly identically.

## Prerequisite

Requirement is contacting Kermi Support to activate Modbus-TCP which can be connected directly by network.
Older devices (non-Pro ?) were connected by Modbus-RCP - maybe you can try to connect them here using a Modbus-TCP modulator (e.x. from waveshare or similar).

## Supported Things

First you need a "Modbus TCP-Bridge" which establishes the basic connection towards your X-Center device.

| Name                     | Thing Type ID | Description                                                                                                                 |
|--------------------------|---------------|-----------------------------------------------------------------------------------------------------------------------------|
| Kermi Heat Pump X-Center | kermi-xcenter | Provides (electric) Power values, Flow-information, PV-States, Temperatures and general Information of your Kermi Heat Pump |

## Discovery

This binding does not support autodiscovery.

## Device IDs

| Device              | Device ID | Comment                                         |
|---------------------|-----------|-------------------------------------------------|
| X-Center            | 40        | on cascade-circuits: slave1: 41, slave2: 42, ...|

## Thing Configuration

The needed Bridge can be found in the **Modbus Binding** and have to be added manually without Discovery.

1. Create _Modbus TCP Slave (Bridge)_ with matching Settings of your Kermi Device:

- **IP Address** - IP Address or hostname of your heatpump
- **Port** - Port of modbus on your heatpump (normally 502) 
- **Device ID** - ID on modbus, 40 is default for Kermi (see [Device IDs](#device-ids))

1. Create _Kermi Heat Pump X-Center_ and attach it to the previous installed _Modbus TCP Slave (Bridge)_.
Configuration requires an appropriate Data Refresh Interval with more than 2000 Milliseconds, default is 5000.
If it's too fast, you may experience errors in openHAB or your X-Center!
Reboot if X-Center stops responding on network access.
You can enable "PV Modulation" if you want to read the values (default: disabled)

Details on Configurations explained below.

### Modbus TCP Slave

| Parameter | Type    | Description                                                   |
|-----------|---------|---------------------------------------------------------------|
| host      | text    | IP Address or reachable hostname of your device               |
| port      | integer | TCP Port of your Kermi device Modbus Settings. Default is 502 |
| deviceId  | integer | Modbus ID of your Kermi device Modbus Settings. Default is 40 |

### Kermi Heat Pump X-Center

Select as Bridge your previously created Modbus TCP Slave.

| Parameter | Type    | Description                                                    |
|-----------|---------|----------------------------------------------------------------|
| refresh   | integer | Refresh Rate of X-Center values in Milliseconds (default:5000) |
| pvEnabled | boolean | Read PV-Modulation (default:false)                             |


### Kermi StorageSystemModule (support planned in future releases)

Select as Bridge a separate (second) Modbus TCP Slave.

| Parameter | Type    | Description                                                        |
|-----------|---------|--------------------------------------------------------------------|
| host      | text    | IP Address or reachable hostname of your device (same as X-Center) |
| port      | integer | TCP Port of your Kermi device Modbus Settings. Default is 502      |
| deviceId  | integer | Modbus ID of your Kermi device Modbus Settings. Default is 50      |

### Channel-Groups

The X-Center / StorageSystemModule device offers quite an amount of channels.
They are grouped into 7 channel-groups:

- State
- EnergySource
- Charging Circuit
- Power & Efficiency
- Workhours
- Alarm
- PV Modulation

### Channels

#### Status

| Channel Id      | Channel         | Type   | Description                                                          |
|-----------------|-----------------|--------|----------------------------------------------------------------------|
| global-state-id | Global State ID | Number | State of heat pump as Number, displayed as readable State-Text in UI |

Possible states:

- Standby
- Alarm
- DrinkingWater
- Heating
- Defrost
- Preparing
- Blocked
- EVU Blocktime
- Unavailable

#### Energy-Source

| Channel Id                 | Channel Label        | Channnel Id | Type                | Description                                     |
|----------------------------|----------------------|-------------|---------------------|-------------------------------------------------|
| exit-temperature           | Exit temperature     |             | Number<Temperature> | in °C - Air temperature exiting heat pump       |
| incoming-temperature       | Incoming temperature |             | Number<Temperature> | in °C - Air temperature incoming into heat pump |
| temperature-sensor-outside | Temperature Outside  |             | Number<Temperature> | in °C - Outside Air Temperature (if connected)  |

#### Charging Circuit

| Channel Id         | Channel Label      | Type                       | Description                                                                       |
|--------------------|--------------------|----------------------------|-----------------------------------------------------------------------------------|
| flow-temperature   | Flow temperature   | Number<Temperature>        | in °C - Water temperature **from** heat pump to drinking water or heating circuit |
| return-temperature | Return temperature | Number<Temperature>        | in °C - Water temperature returning back **to** heat pump                         |
| flow-speed         | Flow speed         | Number<VolumetricFlowRate> | in l/min - Flow speed of the water                                                |

#### Power and efficiency

| Channel Id                     | Channel Label                         | Type          | Description                                      |   
|--------------------------------|---------------------------------------|---------------|--------------------------------------------------|
| cop                            | Current COP                           | Number        | current cop overall (Coefficient Of Performance) |  
| cop-heating                    | Current COP heating                   | Number        | cop for heating                                  |
| cop-drinkingwater              | Current COP drinking water            | Number        | cop for drinking water                           |
| cop-cooling                    | Current COP cooling                   | Number        | cop for cooling                                  |
|                                |                                       |               |                                                  |
| power                          | Current power                         | Number<Power> | in W (Watt) - Power overall                      |
| power-heating                  | Current power heating                 | Number<Power> | in W (Watt) - Power for heating                  |
| power-drinkingwater            | Current power drinking water          | Number<Power> | in W (Watt) - Power for drinking water           |
| power-cooling                  | Current power cooling                 | Number<Power> | in W (Watt) - Power for cooling                  |
|                                |                                       |               |                                                  |
| electrical-power               | Current electric power                | Number<Power> | in W (Watt) - electric Power overall             |
| electrical-power-heating       | Current electric power heating        | Number<Power> | in W (Watt) - electric Power for heating         |
| electrical-power-drinkingwater | Current electric power drinking water | Number<Power> | in W (Watt) - electric Power for drinking water  |
| electrical-power-cooling       | Current electric power cooling        | Number<Power> | in W (Watt) - electric Power for cooling         |

#### Workhours

| Channel Id                     | Channel Label                  | Type         | Description                                            |
|--------------------------------|--------------------------------|--------------|--------------------------------------------------------|
| workhours-fan                  | Fan workhours                  | Number<Time> | in h (hour) - worked hours of the fan                  |
| workhours-storage-loading-pump | Storage Loading Pump workhours | Number<Time> | in h (hour) - worked hours of the storage loading pump |
| workhours-compressor           | Compressor workhours           | Number<Time> | in h (hour) - worked hours of the compressor           |

#### Alarm

| Channel Id  | Channel Label | Type   | Description                     |
|-------------|---------------|--------|---------------------------------|
| alarm-state | Alarm state   | Switch | On / true if an alarm is raised |

#### PV Modulation

| Channel Id                          | Channel Label                     | Type                | Description                                                       |
|-------------------------------------|-----------------------------------|---------------------|-------------------------------------------------------------------|
| pv-state                            | PV Modulation Active              | Switch              | On / true if PV Modulation is currently active                    |
| pv-power                            | PV Power                          | Numbery<Power>      | in W (Watt) - Power of PV Modulation                              |
| pv-target-temperature-heating       | Target temperature heating        | Number<Temperature> | in °C - target Temperature in PV Mode of heating (storage)        |
| pv-target-temperature-drinkingwater | Target temperature drinking water | Number<Temperature> | in °C - target Temperature in PV Mode of drinking water (storage) |

## Full Example

Attention: Configuration by file is not recommended. You can configure everything in the main UI.

### `kermi.things` Example

```java
Bridge modbus:tcp:device "Kermi X-Center Modbus TCP" [ host="xcenter", port=502, id=40 ] {
  Bridge kermi-xcenter heatpump "Kermi X-Center Heat Pump" [ refresh=5000, pvEnabled=false ]
}
```

### Items

```java
Number    XCenter_Global_State_Id                 "X-Center Global State ID"            (kermi)      { channel="modbus:tcp:device:heatpump:state#global-state-id" }

Number:Temperature        Heatpump_FlowTemperature       "Flow Temperature" (kermi,persist) { channel="modbus:tcp:device:heatpump:charging-circuit#flow-temperature" }
Number:Temperature        Heatpump_ReturnTemperature     "Return Temperature" (kermi,persist) { channel="modbus:tcp:device:heatpump:charging-circuit#return-temperature" }
Number:VolumetricFlowRate Heatpump_FlowSpeed             "Flow Speed" (kermi,persist) { channel="modbus:tcp:device:heatpump:charging-circuit#flow-speed" }

Number:Temperature        Heatpump_ExitTemperature       "Exit Temperature" (kermi,persist) { channel="modbus:tcp:device:heatpump:energy-source#exit-temperature" }
Number:Temperature        Heatpump_Incomingtemperature   "Incoming temperature" (kermi,persist) { channel="modbus:tcp:device:heatpump:energy-source#incoming-temperature" }
Number:Temperature        Heatpump_TemperatureOutside    "Temperature Outside" (kermi,persist) { channel="modbus:tcp:device:heatpump:energy-source#temperature-sensor-outside" }

Number                    Heatpump_CurrentCOP                                "Current COP"                             (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#cop" }
Number                    Heatpump_CurrentCOPHeating                         "Current COP Heating"                     (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#cop-heating" }
Number                    Heatpump_CurrentCOPdrinkingwater                   "Current COP drinking water"              (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#cop-drinkingwater" }
Number                    Heatpump_CurrentCOPCooling                         "Current COP Cooling"                     (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#cop-cooling" }
Number:Power              Heatpump_CurrentPower                              "Current Power"                           (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#power" }
Number:Power              Heatpump_Currentpowerheating                       "Current power heating"                   (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#power-heating" }
Number:Power              Heatpump_CurrentPowerDrinkingWater                 "Current Power Drinking Water"            (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#power-drinkingwater" }
Number:Power              Heatpump_Currentpowercooling                       "Current power cooling"                   (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#power-cooling" }
Number:Power              Heatpump_CurrentElectricPower                      "Current Electric Power"                  (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#electric-power" }
Number:Power              Heatpump_CurrentElectricPowerHeating               "Current Electric Power Heating"          (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#electric-power-heating" }
Number:Power              Heatpump_Currentelectricpowerdrinkingwater         "Current electric power drinking water"   (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#electric-power-drinkingwater" }
Number:Power              Heatpump_CurrentElectricPowerCooling               "Current Electric Power Cooling"          (kermi)         { channel="modbus:tcp:device:heatpump:power-channel#electric-power-cooling" }

Switch                    Heatpump_PVModulationActive                        "PV Modulation Active"                    (kermi)         { channel="modbus:tcp:device:heatpump:pv-modulation#pv-state" }
Number:Power              Heatpump_PVPower                                   "PV Power"                                (kermi)         { channel="modbus:tcp:device:heatpump:pv-modulation#pv-power" }
Number:Temperature        Heatpump_PVTempHeating                             "PV Temp Heating"                         (kermi)         { channel="modbus:tcp:device:heatpump:pv-modulation#pv-target-temperature-heating" }
Number:Temperature        Heatpump_PVTempDrinkingwater                       "PV Temp Drinkingwater"                   (kermi)         { channel="modbus:tcp:device:heatpump:pv-modulation#pv-target-temperature-drinkingwater" }

Number:Time               Heatpump_FanWorkhours                              "Fan Workhours"                           (kermi)         { channel="modbus:tcp:device:heatpump:workhours#workhours-fan" }
Number:Time               Heatpump_StorageLoadingPumpWorkhours               "StorageLoadingPump Workhours"            (kermi)         { channel="modbus:tcp:device:heatpump:workhours#workhours-storage-loading-pump" }
Number:Time               Heatpump_CompressorWorkhours                       "Compressor Workhours"                    (kermi)         { channel="modbus:tcp:device:heatpump:workhours#workhours-compressor" }
```

## Persistence

You can / should persist some items you want to track, maybe you track your power consumption with another device (PV-System or 'smart' electricity meter), so you can compare these values.

Suggestion / Optional:  
As these (power & temperature) values are long-running ones, maybe you should invest a little amount of time using influxDB as persistence (additionally to your existing default persistence in openHAB).
InfluxDB is a good storage-solution for long terms and uses very small space for its data. Please read the documentation for better understanding how it works.


### Visualization

As many other users I like and use the Grafana approach (in combination with influxdb).
See here for more information [InfluxDB & Grafana Tutorial](https://community.openhab.org/t/influxdb-grafana-persistence-and-graphing/13761)

### Credits

Credits goes to Bernd Weymann (Author of E3DC-Modbus-Binding).
I used its basic structure / code and handling of Modbus Messages for this binding. Thanks.
