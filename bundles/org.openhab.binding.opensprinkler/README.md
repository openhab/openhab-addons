# OpenSprinkler Binding

This binding allows allows basic control of the OpenSprinkler and OpenSprinkler PI (Plus) devices.
Stations can be controlled to be turned on or off and rain sensor state can be read.

## Supported Bridges

*   HTTP (`http`) - The http bridge allows to communicate with an OpenSprinkler device through the network
*   Pi (`pi`) - With the pi bridge, openHAB can communicate with an OpenSprinkler device which is installed on the same Pi as the openHAB system.

## Supported Things

*   OpenSprinkler Station (`station`) - to control a single station of a device, e.g. to turn it on or off
*   OpenSprinkler Device (`device`) - for getting device-specific infos, e.g. if rain was detected

## Discovery

OpenSprinkler devices can be manually discovered by sending a request to every IP on the network.
Discovery needs to be run manually as this is a brute force method of finding devices that can saturate network or device available bandwidth.

## Thing Configuration

OpenSprinkler using the HTTP interface

```
Bridge opensprinkler:http:http [hostname="127.0.0.1", port=80, pasword="opendoor", refresh=60] {
    Thing station 01 [stationIndex=1]
}
```

-   hostname: Hostname or IP address of the OpenSprinkler HTTP API.
-   port: Port the OpenSprinkler device is listening on. Usually 80.
-   password: Admin password of the API. Factory default is: opendoor
-   refresh: Number of seconds in between refreshing the Thing state with the API.
-   basicUsername: (optional) Only needed when the OpenSprinkler device is behind a basic auth enforcing reverse proxy.
-   basicPassword: (optional) Only needed when the OpenSprinkler device is behind a basic auth enforcing reverse proxy.

OpenSprinkler using the Pi interface

```
Bridge opensprinkler:pi:pi [stations=8, refresh=60] {
    Thing station 01 [stationIndex=1]
}
```

-   stations: Number of stations to control.
-   refresh: Number of seconds in between refreshing the Thing state with the API.

### Station Thing Configuration

The `station` thing can be used with both bridge and has the following configuration properties:

-   stationIndex: The index of the station to communicate with, starting with 0 for the first station

Be aware, that not all features of a station may be supported by the Pi interface bridge.

## Channels

The following channel is supported by the `station` thing.

| Channel Type ID    | Item Type   |    | Description                                             |
|--------------------|-------------|----|---------------------------------------------------------|
| stationState       | Switch      | RW | This channel indicates whether station 01 is on or off. |
| remainingWaterTime | Number:Time | R  | The time the station remains to be open.                |
| nextDuration       | Number:Time | RW | A configuration item, which time, if linked, will be    |
|                    |             |    | used as the time the station will be kept open when     |
|                    |             |    | switched on. It's advised to add persistence for items  |
|                    |             |    | linked to this channel, the binding does not persist    |
|                    |             |    | values of it.

When using the `nextDuration` channel, it is advised to setup persistence (e.g. MapDB) in order to persist the value through restarts.

The following is supported by the `device` thing, but only when connected using the http interface.

| Channel Type ID | Item Type |    | Description                                                           |
|-----------------|-----------|----|-----------------------------------------------------------------------|
| rainsensor      | Switch    | RO | This channel indicates whether rain is detected by the device or not. |

## Example

demo.Things:

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

Switch RainSensor { channel="opensprinkler:station:http:device:rainsensor" }
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
