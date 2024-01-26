# Kermi Heat pump

Integrates the X-Center Device (x-center pro) of Kermi Heat pump.
Kermi X-Center & other attached devices (in progress) are integrated into the Modbus Binding.
Requirement is contacting Kermi Support to activate Modbus-TCP which can be connected directly by network.
Older devices (non-Pro ?) were connected by Modbus-RCP - maybe you can try to connect them here using a Modbus-TCP modulator (e.x. from waveshare or similar).
This binding was tested and developed with Kermi x-change dynamic pro heat pump (build 2023).

See chapter [Thing Configuration](#thing-configuration) how to set them up.

## Supported Things

First you need a TCP-Bridge which establishes the basic connection towards your X-Center device

| Name                     | Thing Type ID | Description                                                                                                                 |
|--------------------------|---------------|-----------------------------------------------------------------------------------------------------------------------------|
| Kermi Heat Pump X-Center | kermi-xcenter | Provides (electric) Power values, Flow-information, PV-States, Temperatures and general Information of your Kermi Heat Pump |
| StorageSystemModule      | kermi-storage | Provides temperatures of your water-storage (drinking water), heating and cooling and details about your heating-circuit    |

## Discovery

There's no discovery.
Modbus registers are available for all devices.

## Device IDs

| Device              | device ID | Comment                                          |
|---------------------|-----------|--------------------------------------------------|
| X-Center            | 40        | on cascade-circuits: slave1: 41, slave2: 42, ... |
| StorageSystemModule | 50 or 51  |                                                  |

## Thing Configuration

The needed Things can be found in the **Modbus Binding** and have to be added manually without Discovery

1. Create _Modbus TCP Bridge_ with matching Settings of your Kermi Device

- IP Address
- Device ID
- Port ID

1. Create _Kermi Heat Pump X-Center_ and attach it to the previous installed _Modbus TCP Bridge_. Configuration requires an approriate Data Refresh Interval with more than 2000 Milliseconds, default is 5000. If it's too fast, you may experience errors in openhab or your x-center ! (Reboot if x-center stops responding on network access). You can enable "PV Modulation" if you want to read the values (default: disabled)

2. If you have a StorageSystemModule add _StorageSystemModule_ Thing, you have to create a separate _Modbus TCP Bridge_ because of the other device-id. (todo)


### Modbus TCP Slave

| Parameter | Type    | Description                                                   |
|-----------|---------|---------------------------------------------------------------|
| host      | text    | IP Address or reachable hostname of your device               |
| port      | integer | TCP Port of your Kermi device Modbus Settings. Default is 502 |
| deviceid  | integer | Modbus ID of your Kermi device Modbus Settings. Default is 40 |

### Kermi Heat Pump X-Center

Select as Bridge your previously created Modbus TCP Slave.

| Parameter       | Type    | Description                                                             |
|-----------------|---------|-------------------------------------------------------------------------|
| refresh         | integer | Refresh Rate of x-center values in Milliseconds                         |

### Kermi StorageSystemModule (todo)

Select as Bridge a seperate (second) Modbus TCP Slave.

| Parameter | Type    | Description                                                        |
|-----------|---------|--------------------------------------------------------------------|
| host      | text    | IP Address or reachable hostname of your device (same as x-center) |
| port      | integer | TCP Port of your Kermi device Modbus Settings. Default is 502      |
| deviceid  | integer | Modbus ID of your Kermi device Modbus Settings. Default is 50      |


### Things

```java
Bridge modbus:tcp:device "Kermi X-Center Modbus TCP" [ host="xcenter", port=502, id=40 ] {
 Bridge kermi-xcenter "Kermi X-Center Heat Pump" [ refresh=5000 ]
}
```

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

| Channel                | Type   | Description                                                                                  |
|------------------------|--------|----------------------------------------------------------------------------------------------|
| Global State           | String | State of heat pump as String taken from Kermi Documentation (german wording)                 |
| Global State as Number | Number | State of heat pump as Number, better for checking / integration as the State as String above |

Possible states:

- Standby 
- Alarm
- TWE
- Kuehlen
- Heizen
- Abtauung
- Vorbereitung
- Blockiert
- EVU Sperre
- Nicht verfuegbar  
  

- Status unknown (if no valid state is received)


#### Energy-Source

| Channel              | Type                | Description                                     |
|----------------------|---------------------|-------------------------------------------------|
| Exit temperature     | Number<Temperature> | in °C - Air temperature exiting heat pump       |
| Incoming temperature | Number<Temperature> | in °C - Air temperature incoming into heat pump |
| Temperature Outside  | Number<Temperature> | in °C - Outside Air Temperature (if connected)  |

#### Charging Circuit

| Channel            | Type                       | Description                                                                       |
|--------------------|----------------------------|-----------------------------------------------------------------------------------|
| Flow temperature   | Number<Temperature>        | in °C - Water temperature **from** heat pump to drinking water or heating circuit |
| Return temperature | Number<Temperature>        | in °C - Water temperature returning back **to** heat pump                         |
| Flow speed         | Number<VolumetricFlowRate> | in l/min - Flow speed of the water                                                |

#### Power and efficiency

| Channel                               | Type          | Description                                      |   
|---------------------------------------|---------------|--------------------------------------------------|   
| Current COP                           | Number        | current cop overall (Coefficient Of Performance) |  
| Current COP heating                   | Number        | cop for heating                                  |   
| Current COP drinking water            | Number        | cop for drinking water                           |   
| Current COP cooling                   | Number        | cop for cooling                                  |
|                                       |               |                                                  |
| Current power                         | Number<Power> | in W (Watt) - Power overall                      |
| Current power heating                 | Number<Power> | in W (Watt) - Power for heating                  |
| Current power drinking water          | Number<Power> | in W (Watt) - Power for drinking water           |
| Current power cooling                 | Number<Power> | in W (Watt) - Power for cooling                  |
|                                       |               |                                                  |
| Current electric power                | Number<Power> | in W (Watt) - electric Power overall             |
| Current electric power heating        | Number<Power> | in W (Watt) - electric Power for heating         |
| Current electric power drinking water | Number<Power> | in W (Watt) - electric Power for drinking water  |
| Current electric power cooling        | Number<Power> | in W (Watt) - electric Power for cooling         |

#### Workhours

| Channel                        | Type         | Description                                            |
|--------------------------------|--------------|--------------------------------------------------------|
| Fan workhours                  | Number<Time> | in h (hour) - worked hours of the fan                  |
| Storage Loading Pump workhours | Number<Time> | in h (hour) - worked hours of the storage loading pump |
| Compressor workhours           | Number<Time> | in h (hour) - worked hours of the compressor           |

#### Alarm

| Channel                        | Type         | Description                                            |
|--------------------------------|--------------|--------------------------------------------------------|
| Alarm state                    | Switch       | On / true if an alarm is raised                        |

#### PV Modulation

| Channel                           | Type                | Description                                                       |
|-----------------------------------|---------------------|-------------------------------------------------------------------|
| PV Modulation Active              | Switch              | On / true if PV Modulation is currently active                    |
| PV Power                          | Numbery<Power>      | in W (Watt) - Power of PV Modulation                              |
| Target temperature heating        | Number<Temperature> | in °C - target Temperature in PV Mode of heating (storage)        |
| Target temperature drinking water | Number<Temperature> | in °C - target Temperature in PV Mode of drinking water (storage) |


### Persistence

You can / should persist some items you want to track, maybe you track your power consumption with another device (PV-System or 'smart' electricity meter), so you can compare these values.
As these values are long-running ones, maybe you should invest a little amount of time using influx as persistance (additionally to your existing). Influx DB is the best storage for long terms and uses very small space for its data. Please read the documentation for better understanding how it works.

Simple: Install influxDB (>2.x) on a system within your network and enter your configuration settings in openhab using the influxdb-binding. As you do not track that mass of data you can use a raspberry pi with and SSD attaches to it (do NOT use an SD-card as database-storage). 

### Visualization

As many other users I like and use the Grafana approach (in combination with influxdb). See here for more information [InfluxDB & Grafana Tutorial](https://community.openhab.org/t/influxdb-grafana-persistence-and-graphing/13761)

### Credits

Credits goes to Bernd Weymann (Author of E3DC-Modbus-Binding). I used its basic structure / code and handling of Modbus Messages for this binding. Thanks.
