# Broadlink Binding

The binding integrates devices based on Broadlink controllers.
As the binding uses the [broadlink-java-api](https://github.com/mob41/broadlink-java-api), theoretically all devices supported by the api can be integrated with this binding.

*Note:* So far only the Floureon Thermostat has been tested! 

## Supported Things

*Note:* So far only the Floureon Thermostat has been tested! The other things are "best guess" implementations.

| Things                  | Description                                                   | Thing Type           |
|-------------------------|---------------------------------------------------------------|----------------------|
| Floureon Thermostat     | Broadlink based Thermostat sold with the branding Floureon    | floureonthermostat   |
| Hysen Thermostat        | Broadlink based Thermostat sold with the branding Hysen       | hysenthermostat      |
| A1 Environmental Sensor | Broadlink based A1 Environmental Sensor                       | a1environmentalsensor|

## Discovery

Broadlink devices are discovered on the network by sending a specific broadcast message.
Authentication is automatically sent after creating the thing.

## Thing Configuration

Two parameter are required for creating things:
- `host`: The hostname or IP address of the device.
- `mac` : The network MAC of the device.
The autodiscovery process finds both parts automatically.

## Channels

### Floureon-/Hysenthermostat
| Channel Type ID               | Item Type          | Description                                                                                                                                                                           |
|-------------------------------|--------------------|----------------------------------------------------------------------|
| power                         | Switch             | Switch display on/off and enable/disables heating                    |
| mode                          | String             | Current mode of the thermostat (`auto` or `manual`)                  |
| sensor                        | String             | The sensor (`internal`/`external`) used for triggering the thermostat|
| roomtemperature               | Number:Temperature | Room temperature, measured directly at the device                    |
| roomtemperatureexternalsensor | Number:Temperature | Room temperature, measured by an external sensor                     |
| active                        | Switch             | Show if thermostat is currently actively heating                     |
| setpoint                      | Number:Temperature | Temperature setpoint that open/close valve                           |
| temperatureoffset             | Number:Temperature | Manual temperature adjustment                                        |
| remotelock                    | Switch             | Locks the device to only allow remote actions                        |
| time                          | DateTime           | The time and day of week of the device                               |

### A1 Environmental Sensor
| Channel Type ID  | Item Type          | Description                                                                                                                                                                           |
|------------------|--------------------|----------------------------------------------------|
| temperature      | Number:Temperature | Temperature                                        |
| airquality       | String             | Airquality                                         |
| noise            | String             | Noise                                              |
| light            | String             | Light                                              |
| humidity         | Number             | Humidity                                           |

## Full Example

demo.things:

```
Thing broadlink:floureonthermostat:bathroomthermostat "Bathroom Thermostat" [ host="192.168.0.23", mac="00:10:FA:6E:38:4A"]
```

demo.items:

```
Number:Temperature  Bathroom_Thermostat_Temperature      "Room temperature [%.1f %unit%]"        <temperature>  { channel="broadlink:floureonthermostat:bathroomthermostat:roomtemperature"}
Number:Temperature  Bathroom_Thermostat_Temperature_Ext  "Room temperature (ext) [%.1f %unit%]"  <temperature>  { channel="broadlink:floureonthermostat:bathroomthermostat:roomtemperature"}
Number:Temperature  Bathroom_Thermostat_Setpoint         "Setpoint [%.1f %unit%]"                <temperature>  { channel="broadlink:floureonthermostat:bathroomthermostat:setpoint"}
Switch              Bathroom_Thermostat_Power            "Power"                                                { channel="broadlink:floureonthermostat:bathroomthermostat:power"}
Switch              Bathroom_Thermostat_Active           "Active"                                               { channel="broadlink:floureonthermostat:bathroomthermostat:active"}
String              Bathroom_Thermostat_Mode             "Mode"                                                 { channel="broadlink:floureonthermostat:bathroomthermostat:mode"}
String              Bathroom_Thermostat_Sensor           "Sensor"                                               { channel="broadlink:floureonthermostat:bathroomthermostat:sensor"}
Switch              Bathroom_Thermostat_Lock             "Lock"                                  <lock>         { channel="broadlink:floureonthermostat:bathroomthermostat:remotelock"}
DateTime            Bathroom_Thermostat_Time             "Time [%1$tm/%1$td %1$tH:%1$tM]"        <time>         { channel="broadlink:floureonthermostat:bathroomthermostat:time"}

```

demo.rules:

```
TODO
```

demo.sitemap:

```
TODO
```