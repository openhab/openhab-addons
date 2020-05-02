# Konnected Binding

This binding is for interacting with the [Konnected Alarm Panel](https://konnected.io/).
A module which interfaces with existing home security sensors.
Konnected is an open-source firmware and software that runs on a NodeMCU ESP8266 device.
The Konnected hardware is specifically designed for an alarm panel installation, but the general purpose firmware/software can be run on any ESP8266 device.

## Supported Things

This binding supports one type of thing module, which represents a Konnected Alarm Panel.

## Discovery

The binding will auto discover The Konnected Alarm Panels which are attached to the same network as the server running openHAB via UPnP.
The binding will then create things for each module discovered which can be added.

## Thing Configuration

The binding attempts to discover The Konnected Alarm Panels via the UPnP service.
The auto-discovery service of the binding will detect the ip address and port of the Konnected Alarm Panel.
The binding will attempt to obtain the ip address of your openHAB server as configured in the OSGi framework.
However, if it is unable to determine the ip address it will also attempt to use the network address service to obtain the ip address and port.
In addition you can also turn off discovery which when this setting is synced to the module will cause the device to no longer respond to UPnP requests as documented.
https://help.konnected.io/support/solutions/articles/32000023968-disabling-device-discovery
Please use this setting with caution and do not disable until a static ip address has been provided for your Konnected Alarm Panel via DHCP, router or otherwise.
The blink setting will disable the transmission LED on the Konnected Alarm Panel.


## Channels

The auto discovered thing adds two default channels.

| Channel | Channel Id | Channel Type | Description                                              |
|---------|------------|--------------|----------------------------------------------------------|
| 1       | Zone_6     | Switch       | A Switch channel for zone 6                              |
| 2       | Out        | Actuator     | The Channel for the Out Pin on the Konnected Alarm Panel |

One channel for Zone 6 which is a sensor type channel, and one channel for the out pin that is an actuator type channel.
These channels represent the two pins on the Konnected Alarm Panel whose type cannot be changed.
For zones 1-5, you will need to add channels for the remaining zones that you have connected and configure them with the appropriate configuration parameters for each channel.


| Channel Type | Item Type            | Config Parameters                                  | Description                                                                                                                                                                                                                                     |
|--------------|----------------------|----------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Switch       | Switch               | Zone Number                                        | This is the channel type for sensors or other read only devices                                                                                                                                                                                 |
| Actuator     | Switch               | Zone Number, Momentary, Pause, Times               | This is the channel type for devices whose state can be turned on an off by the Konnected Alarm Panel                                                                                                                                           |
| Temperature  | Number:Temperature   | Zone Number, DHT22, Poll Interval, DS18b20 Address | This is the channel for sensors which measure temperature (DHT22 and DS18B20). The DHT22 setting should be set to true when the channel is monitoring a zone connected to a DHT22 sensor and false if the zone is connected to a DS1820B sensor |
| Humidity     | Number:Dimensionless | Zone Number                                        | This is the channel type for the humidity sensor on a connected DHT22 sensor                                                                                                                                                                    |

You will need to configure each channel with the appropriate zone number corresponding to the zone on The Konnected Alarm Panel.
Then you need to link the corresponding item to the channel.

For the actuator type channels you can also add configuration parameters times, pause and momentary which will be added to the payload that is sent to the Konnected Alarm Panel.
These parameters will tell the module to pulse the actuator for certain time period.
A momentary switch actuates a switch for a specified time (in milliseconds) and then reverts it back to the off state.
This is commonly used with a relay module to actuate a garage door opener, or with a door bell to send a momentary trigger to sound the door bell.
A beep/blink switch is like a momentary switch that repeats either a specified number of times or indefinitely.
This is commonly used with a piezo buzzer to make a "beep beep" sound when a door is opened, or to make a repeating beep pattern for an alarm or audible warning.
It can also be used to blink lights.

DSB1820 temperature probes.
These are one wire devices which can all be Konnected to the same "Zone" on the Konnected Alarm Panel.
As part of its transmission  the module will include an unique "address" property of each sensor probe that will be logged to the debug log when received.
This needs to be added to the channel if there are multiple probes connected.
The default behavior in absence of this configuration will be to simply log the address of the received event.
A channel should be added for each probe, as indicated above and configured with the appropriate address.


## Full Example

*.items

```
Contact Front_Door_Sensor "Front Door" {channel="konnected:module:generic:switch"}
Switch Siren "Siren" {channel="konnected:module:generic:actuator"}
```

*.sitemap

```
Switch item=Front_Door_Sensor label="Front Door" icon="door" mappings=[OPEN="Open", CLOSED="Closed"]
Switch item=Siren label="Alarm Siren" icon="Siren" mappings=[ON="Open", OFF="Closed"]
```

*.things

```
Thing konnected:module:generic "Konnected Module" [ipAddress="http://192.168.30.153:9586", macAddress="1586517"]{
   Type switch      : switch      "Front Door"          [channel_zone=1]
   Type actuator    : actuator    "Siren"               [channel_zone=1, momentary = 50, times = 2, pause = 50]
   Type humidity    : humidity    "DHT - Humidity"      [channel_zone=1]
   Type temperature : temperature "DHT Temperature"     [channel_zone=1, tempsensorType = true, pollinterval = 1]
   Type temperature : temperature "DS18B20 Temperature" [channel_zone=1, tempsensorType = false, pollinterval = 1, ds18b20_address = "XX:XX:XX:XX:XX:XX:XX"]
}
```

