# OpenSprinkler Binding

This binding allows control of your OpenSprinkler devices.

## Supported Bridges

* `OpenSprinkler HTTP Bridge` is required to communicate with an OpenSprinkler device through the network and should be added first.

## Supported Things

* `OpenSprinkler Station` is for advanced control over a single station (zone) of a device, e.g. to turn it on or off.
* `OpenSprinkler Device` is for device-specific controls that usually apply to multiple stations or main unit sensors, e.g. if rain was detected.

## Discovery

OpenSprinkler devices can be discovered by the binding sending requests to every IP on your network.
Due to this method used, it is very slow at finding devices and can saturate network bandwidth.

## Bridge ('http') Configuration

-   hostname: Hostname or IP address of the OpenSprinkler HTTP API.
-   port: Port the OpenSprinkler device is listening on. Usually 80.
-   password: Admin password of the API. Factory default is: opendoor
-   refresh: Number of seconds in between refreshing the Thing state with the API.
-   basicUsername: (optional) Only needed when the OpenSprinkler device is behind a basic auth enforcing reverse proxy.
-   basicPassword: (optional) Only needed when the OpenSprinkler device is behind a basic auth enforcing reverse proxy.

### Station Thing Configuration

The `station` thing must be used with a `http` bridge and has the following configuration properties:

-   stationIndex: The index of the station to communicate with, starting with 0 for the first station

## Channels

The following channels are supported by the `station` thing.

| Channel Type ID    | Item Type   |    | Description                                              |
|--------------------|-------------|----|----------------------------------------------------------|
| stationState       | Switch      | RW | This channel indicates whether the station is on or off. |
| remainingWaterTime | Number:Time | R  | The time the station remains to be open.                 |
| nextDuration       | Number:Time | RW | The amount of time that will be used to keep the station |
|                    |             |    | open when next manually switched on. If not set, this    |
|                    |             |    | value will default to 18 hours which is the maximum time | 
|                    |             |    | supported.                                               |
| queued             | Switch      | RW | Indicates that the station is queued to be turned on.    |
|                    |             |    | The channel cannot be turned on, only turning it off is  |
|                    |             |    | supported (which removes the station from the queue).    |
| ignoreRain         | Switch      | RW | This channel makes the station ignore the rain delay.    |

When using the `nextDuration` channel, it is advised to setup persistence (e.g. MapDB) in order to persist the value through restarts.

The following channels are supported by the `device` thing.
NOTE: Some channels will only show up if the hardware has the required sensor and is setup correctly.

| Channel Type ID | Item Type              |    | Description                                                                        |
|-----------------|------------------------|----|------------------------------------------------------------------------------------|
| rainsensor      | Switch                 | RO | This channel indicates whether rain is detected by the device or not.              |
| sensor2         | Switch                 | RO | This channel is for the second sensor (if your hardware supports it).              |
| currentDraw     | Number:ElectricCurrent | RO | Shows the current draw of the device.                                              |
| waterlevel      | Number:Dimensionless   | RO | This channel shows the current water level in percent (0-250%). The water level is |
|                 |                        |    | calculated based on the weather and influences the duration of the water programs. |
| signalStrength  | Number:Power           | RO | Shows the reported RSSI value in dB to indicate how strong the WiFi Signal is.     |
| flowSensorCount | Number:Dimensionless   | RO | Shows the number of pulses the optional water flow sensor has reported.            |
| programs        | String                 | RW | Displays a list of the programs that are setup in your OpenSprinkler and when      |
|                 |                        |    | selected will start that program for you.                                          |
| stations        | String                 | RW | Display a list of stations that can be run when selected to the length of time set |
|                 |                        |    | in the `nextDuration` channel.                                                  |
| nextDuration    | Number:Time            | RW | The time the station will open for when any stations are selected from the         |
|                 |                        |    | `stations` channel.                                                              |
| resetStations   | Switch                 | RW | The ON command will stop all stations immediately, including those waiting to run. |
| enablePrograms  | Switch                 | RW | Allow programs to auto run. When OFF manually started stations will still work.    |

## Textual Example

demo.things:

```
Bridge opensprinkler:http:http [hostname="127.0.0.1", port=81, password="opendoor"] {
    Thing station 01 [stationIndex=0]
    Thing station 02 [stationIndex=1]
    Thing station 03 [stationIndex=2]
    Thing station 04 [stationIndex=3]
    Thing station 05 [stationIndex=4]
    Thing station 06 [stationIndex=5]
    Thing device device
}
```

demo.items:

```
Group stations
Switch Station01 (stations) { channel="opensprinkler:station:http:01:stationState" }
Number:Time Station01RaminingTime { channel="opensprinkler:station:http:01:remainingWaterTime" }
Switch Station02 (stations) { channel="opensprinkler:station:http:02:stationState" }
Switch Station03 (stations) { channel="opensprinkler:station:http:03:stationState" }
Number:Time Station03NextDuration { channel="opensprinkler:station:http:03:nextDuration" }
Switch Station04 (stations) { channel="opensprinkler:station:http:04:stationState" }
Switch Station05 (stations) { channel="opensprinkler:station:http:05:stationState" }
Switch Station06 (stations) { channel="opensprinkler:station:http:06:stationState" }

Switch RainSensor { channel="opensprinkler:device:http:device:rainsensor" }
Number:ElectricCurrent CurrentDraw {channel="opensprinkler:device:http:device:currentDraw"}
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame {
        Switch item=Station01
        Selection item=Station03NextDuration mappings=[300="5 min", 600="10 min"]
    }
}
```
