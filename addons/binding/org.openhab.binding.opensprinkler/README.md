# OpenSprinkler Binding

This binding allows allows basic control of the OpenSprinkler and OpenSprinkler PI (Plus) devices.
Stations can be controlled to be turned on or off and rain sensor state can be read.

## Supported Things

*   OpenSprinkler using the HTTP API access on the network.
*   OpenSprinkler PI (Plus) through the GPIO when openHAB is installed on the same Raspberry Pi used in the OpenSprinkler PI.

## Discovery

OpenSprinkler devices can be manually discovered by sending a request to every IP on the network.
Discovery needs to be run manually as this is a brute force method of finding devices that can saturate network or device available bandwidth.

## Thing Configuration

OpenSprinkler

```
opensprinkler:http:1 [ hostname="127.0.0.1", port=80, password="opendoor", refresh=60 ]
```

-   hostname: Hostname or IP address of the OpenSprinkler HTTP API.
-   port: Port the OpenSprinkler device is listening on. Usually 80.
-   password: Admin password of the API. Factory default is: opendoor
-   refresh: Number of seconds in between refreshing the Thing state with the API.

OpenSprinkler PI

```
opensprinkler:pi:1 [ stations=8, refresh=60 ]
```

-   stations: Number of stations to control.
-   refresh: Number of seconds in between refreshing the Thing state with the API.

## Channels

The following channels are supported by all devices.

| Channel Type ID | Item Type |    | Description                                             |
|-----------------|-----------|----|---------------------------------------------------------|
| station01       | Switch    | RW | This channel indicates whether station 01 is on or off. |
| station02       | Switch    | RW | This channel indicates whether station 02 is on or off. |
| station03       | Switch    | RW | This channel indicates whether station 03 is on or off. |
| station04       | Switch    | RW | This channel indicates whether station 04 is on or off. |
| station05       | Switch    | RW | This channel indicates whether station 05 is on or off. |
| station06       | Switch    | RW | This channel indicates whether station 06 is on or off. |
| station07       | Switch    | RW | This channel indicates whether station 07 is on or off. |
| station08       | Switch    | RW | This channel indicates whether station 08 is on or off. |
| station09       | Switch    | RW | This channel indicates whether station 09 is on or off. |
| station10       | Switch    | RW | This channel indicates whether station 10 is on or off. |
| station11       | Switch    | RW | This channel indicates whether station 11 is on or off. |
| station12       | Switch    | RW | This channel indicates whether station 12 is on or off. |
| station13       | Switch    | RW | This channel indicates whether station 13 is on or off. |
| station14       | Switch    | RW | This channel indicates whether station 14 is on or off. |
| station15       | Switch    | RW | This channel indicates whether station 15 is on or off. |
| station16       | Switch    | RW | This channel indicates whether station 16 is on or off. |
| station17       | Switch    | RW | This channel indicates whether station 17 is on or off. |
| station18       | Switch    | RW | This channel indicates whether station 18 is on or off. |
| station19       | Switch    | RW | This channel indicates whether station 19 is on or off. |
| station20       | Switch    | RW | This channel indicates whether station 20 is on or off. |
| station21       | Switch    | RW | This channel indicates whether station 21 is on or off. |
| station22       | Switch    | RW | This channel indicates whether station 22 is on or off. |
| station23       | Switch    | RW | This channel indicates whether station 23 is on or off. |
| station24       | Switch    | RW | This channel indicates whether station 24 is on or off. |
| station25       | Switch    | RW | This channel indicates whether station 25 is on or off. |
| station26       | Switch    | RW | This channel indicates whether station 26 is on or off. |
| station27       | Switch    | RW | This channel indicates whether station 27 is on or off. |
| station28       | Switch    | RW | This channel indicates whether station 28 is on or off. |
| station29       | Switch    | RW | This channel indicates whether station 29 is on or off. |
| station30       | Switch    | RW | This channel indicates whether station 30 is on or off. |
| station31       | Switch    | RW | This channel indicates whether station 31 is on or off. |
| station32       | Switch    | RW | This channel indicates whether station 32 is on or off. |
| station33       | Switch    | RW | This channel indicates whether station 33 is on or off. |
| station34       | Switch    | RW | This channel indicates whether station 34 is on or off. |
| station35       | Switch    | RW | This channel indicates whether station 35 is on or off. |
| station36       | Switch    | RW | This channel indicates whether station 36 is on or off. |
| station37       | Switch    | RW | This channel indicates whether station 37 is on or off. |
| station38       | Switch    | RW | This channel indicates whether station 38 is on or off. |
| station39       | Switch    | RW | This channel indicates whether station 39 is on or off. |
| station40       | Switch    | RW | This channel indicates whether station 40 is on or off. |
| station41       | Switch    | RW | This channel indicates whether station 41 is on or off. |
| station42       | Switch    | RW | This channel indicates whether station 42 is on or off. |
| station43       | Switch    | RW | This channel indicates whether station 43 is on or off. |
| station44       | Switch    | RW | This channel indicates whether station 44 is on or off. |
| station45       | Switch    | RW | This channel indicates whether station 45 is on or off. |
| station46       | Switch    | RW | This channel indicates whether station 46 is on or off. |
| station47       | Switch    | RW | This channel indicates whether station 47 is on or off. |
| station48       | Switch    | RW | This channel indicates whether station 48 is on or off. |


The following are only support by the OpenSprinkler using the HTTP API interface.

| Channel Type ID | Item Type |    | Description                                                           |
|-----------------|-----------|----|-----------------------------------------------------------------------|
| rainsensor      | Switch    | RO | This channel indicates whether rain is detected by the device or not. |

## Example

demo.Things:

```
opensprinkler:http:1 [ hostname="192.168.1.23", port=80, password="opendoor", refresh=60 ]
```

demo.items:

```
Group stations
Switch Station01 (stations) { channel="opensprinkler:http:1:station01" }
Switch Station02 (stations) { channel="opensprinkler:http:1:station02" }
Switch Station03 (stations) { channel="opensprinkler:http:1:station03" }
Switch Station04 (stations) { channel="opensprinkler:http:1:station04" }
Switch Station05 (stations) { channel="opensprinkler:http:1:station05" }
Switch Station06 (stations) { channel="opensprinkler:http:1:station06" }
Switch Station07 (stations) { channel="opensprinkler:http:1:station07" }
Switch Station08 (stations) { channel="opensprinkler:http:1:station08" }

Switch RainSensor { channel="opensprinkler:http:1:rainsensor" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame {
        Text item=RainSensor label="Rain [%s]"
        Group item=stations label="Water Stations"
    }
}
```
