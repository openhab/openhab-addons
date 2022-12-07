# Broadlink Binding

The binding integrates devices based on broadlink controllers.
As the binding uses the [broadlink-java-api](https://github.com/mob41/broadlink-java-api), theoretically all devices supported by the api can be integrated with this binding.

## Supported Things

_Note:_ So far only the Floureon Thermostat and Rm Mini 3 devices has been tested! The other things are "best guess" implementations.

| Things                  | Description                                                         | Thing Type           |
|-------------------------|---------------------------------------------------------------------|----------------------|
| Floureon Thermostat     | broadlink based Thermostat sold with the branding Floureon          | floureonthermostat   |
| Hysen Thermostat        | broadlink based Thermostat sold with the branding Hysen             | hysenthermostat      |
| Rm Mini                 | broadlink based Universal Controller sold with the branding Rm Mini | rmuniversalremote    |

## Discovery

Broadlink devices are discovered on the network by sending a specific broadcast message.
Authentication is automatically sent after creating the thing.

## Thing Configuration

Two parameter are required for creating things:

- `host`: The hostname or IP address of the device.
- `macAddress` : The network MAC of the device.

The autodiscovery process finds both parts automatically.

## Channels

### Floureon-/Hysenthermostat

| Channel Type ID               | Item Type          | Description                                                          |
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

### RM Mini Universal Controller

| Channel Type ID               | Item Type          | Description                                                          |
|-------------------------------|--------------------|----------------------------------------------------------------------|
| learningmode                 | Switch             | Put device in infrared learning mode when turned on                  |
| savelearned                  | String             | Saves the learned keys using the provided name                       |
| sendlearned                  | String             | Send previously learned keys by name                                 |

## Full Example

demo.things:

```java
Thing broadlinkthermostat:floureonthermostat:bathroomthermostat "Bathroom Thermostat" [ host="192.168.0.23", macAddress="00:10:FA:6E:38:4A"]
```

demo.items:

```java
Number:Temperature  Bathroom_Thermostat_Temperature      "Room temperature [%.1f %unit%]"        <temperature>  { channel="broadlinkthermostat:floureonthermostat:bathroomthermostat:roomtemperature"}
Number:Temperature  Bathroom_Thermostat_Temperature_Ext  "Room temperature (ext) [%.1f %unit%]"  <temperature>  { channel="broadlinkthermostat:floureonthermostat:bathroomthermostat:roomtemperature"}
Number:Temperature  Bathroom_Thermostat_Setpoint         "Setpoint [%.1f %unit%]"                <temperature>  { channel="broadlinkthermostat:floureonthermostat:bathroomthermostat:setpoint"}
Switch              Bathroom_Thermostat_Power            "Power"                                                { channel="broadlinkthermostat:floureonthermostat:bathroomthermostat:power"}
Switch              Bathroom_Thermostat_Active           "Active"                                               { channel="broadlinkthermostat:floureonthermostat:bathroomthermostat:active"}
String              Bathroom_Thermostat_Mode             "Mode"                                                 { channel="broadlinkthermostat:floureonthermostat:bathroomthermostat:mode"}
String              Bathroom_Thermostat_Sensor           "Sensor"                                               { channel="broadlinkthermostat:floureonthermostat:bathroomthermostat:sensor"}
Switch              Bathroom_Thermostat_Lock             "Lock"                                  <lock>         { channel="broadlinkthermostat:floureonthermostat:bathroomthermostat:remotelock"}
DateTime            Bathroom_Thermostat_Time             "Time [%1$tm/%1$td %1$tH:%1$tM]"        <time>         { channel="broadlinkthermostat:floureonthermostat:bathroomthermostat:time"}

```
