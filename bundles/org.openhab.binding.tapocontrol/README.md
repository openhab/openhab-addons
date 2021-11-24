# TapoControl Binding

This binding adds support to control Tapo (Copyright © TP-Link Corporation Limited) Smart Home Devices from your local openHAB system.

## Supported Things

The following Tapo-Devices are supported

### P100/P105 SmartPlug (WiFi)

* Power On/Off
* Wi-Fi signal (SignalStrength)
* On-Time (Time in seconds device is switched on)

### L510_Series dimmable SmartBulb (WiFi)

* Light On/Off
* Brightnes (Dimmer)  0-100 %
* ColorTemperature (Number) 2500-6500 K
* Wi-Fi signal (SignalStrength)
* On-Time (Time in seconds device is switched on)

### L530_Series MultiColor SmartBulb (WiFi)

* Light On/Off
* Brightnes (Dimmer)  0-100 %
* ColorTemperature (Number) 2500-6500 K
* Color (Color)
* Wi-Fi signal (SignalStrength)
* On-Time (Time in seconds device is switched on)

### L900 MultiColor LightStrip (WiFi)

* Light On/Off
* Brightnes (Dimmer)  0-100 %
* ColorTemperature (Number) 2500-6500 K
* Color (Color)
* Wi-Fi signal (SignalStrength)
* On-Time (Time in seconds device is switched on)


## Prerequisites

Before using Smart Plugs with openHAB the devices must be connected to the Wi-Fi network.
This can be done using the Tapo provided mobile app.
You need to setup a bridge (Cloud-Login) to commiunicate with your devices.

## Discovery

Discovery is done by connecting to the Tapo-Cloud Service. 
All devices stored in your cloud account will be detected even if they are not in your network.
You need to know the IP-Adress of your device. This must be set manually in the thing configuration

## Bridge Configuration

The bridge needs to be configured with by `username` and `password` (Tapo-Cloud login) .
This is used for device discovery and to create a handshake (cookie) to act with your devices over the local network.

The thing has the following configuration parameters:

| Parameter          | Description                                                          |
|--------------------|----------------------------------------------------------------------|
| username           | Username (eMail) of your Tapo-Cloud                                  |
| password           | Password of your Tapo-Cloud                                          |

## Thing Configuration

The thing needs to be configured with `ipAddress`.

The thing has the following configuration parameters:

| Parameter          | Description                                                          |
|--------------------|----------------------------------------------------------------------|
| ipAddress          | IP Address of the device.                                            |
| pollingInterval    | Refresh interval in seconds. Optional. The default is 30 seconds     |


## Channels

All devices support some of the following channels:

| group     | channel          |type                    | description                  | things supporting this channel  |
|-----------|----------------- |------------------------|------------------------------|---------------------------------|
| actuator  | output           | Switch                 | Power device on or off       | P100, P105,L510, L530, L900     |
|           | brightness       | Dimmer                 | Brightness 0-100%            | L510, L530, L900                |
|           | colorTemperature | Number                 | White-Color-Temp 2500-6500K  | L510, L530, L900                |
|           | color            | Color                  | Color                        | L530, L900                      |
| device    | wifiSignal       | system.signal-strength | WiFi-quality-level           | P100, P105, L510, L530, L900    |
|           | onTime           | Number:Time            | seconds output is on         | P100, P105, L510, L530, L900    |


## Channel Refresh

When the thing receives a `RefreshType` command the thing will send a new refreshRequest over http.
To minimize network traffic the default refresh-rate is set to 30 seconds. This can be reduced down to 10 seconds in advanced settings of the device. If any command was sent to a channel, it will do an immediately refresh of the whole device.


## Full Example

### tapocontrol.things:

```
tapocontrol:bridge:myTapoBridge                     "Cloud-Login"               [ username="you@yourpovider.com", password="verysecret" ]
tapocontrol:P100:myTapoBridge:mySocket              "My-Socket"                 [ ipAddress="192.168.178.150", pollingInterval=30 ]
tapocontrol:L510_Series:myTapoBridge:whiteBulb      "white-light"               [ ipAddress="192.168.178.151", pollingInterval=30 ]
tapocontrol:L530_Series:myTapoBridge:colorBulb      "color-light"               [ ipAddress="192.168.178.152", pollingInterval=30 ]
tapocontrol:L900:myTapoBridge:myLightStrip          "light-strip"               [ ipAddress="192.168.178.153", pollingInterval=30 ]
``` 

### tapocontrol.items:

```
Switch       TAPO_SOCKET      "socket"                { channel="tapocontrol:P100:myTapoBridge:mySocket:actuator#output" }
``` 
