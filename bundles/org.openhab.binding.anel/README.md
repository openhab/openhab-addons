# Anel NET-PwrCtrl Binding

Monitor and control Anel NET-PwrCtrl devices.

NET-PwrCtrl devices are power sockets / relays that can be configured via browser but they can also be controlled over the network, e.g. with an Android or iPhone app - and also with openHAB via this binding.
Some NET-PwrCtrl devices also have 8 I/O ports which can either be used to directly switch the sockets / relays, or they can be used as general input / output switches in openHAB.


## Supported Things

There are three kinds of devices ([overview on manufacturer's homepage](https://en.anel.eu/?src=/produkte/produkte.htm)):

| [Anel NET-PwrCtrl HUT](https://en.anel.eu/?src=/produkte/hut_2/hut_2.htm) <br/> <sub>( _advanced-firmware_ )</sub>  | [Anel NET-PwrCtrl IO](https://en.anel.eu/?src=/produkte/io/io.htm) <br/> <sub>( _advanced-firmware_ )</sub> | [Anel NET-PwrCtrl HOME](https://de.anel.eu/?src=produkte/home/home.htm) <br/> <sub>( _home_ )</sub> <br/> (only German version) |
| --- | --- | --- |
| [![Anel NET-PwrCtrl HUT 2](https://de.anel.eu/image/leisten/HUT2LV-P_500.jpg)](https://de.anel.eu/?src=produkte/hut_2/hut_2.htm) | [![Anel NET-PwrCtrl IO](https://de.anel.eu/image/leisten/IO-Stecker.png)](https://de.anel.eu/?src=produkte/io/io.htm) | [![Anel NET-PwrCtrl HOME](https://de.anel.eu/image/leisten/HOME-DE-500.gif)](https://de.anel.eu/?src=produkte/home/home.htm) |

Thing type IDs:

* *home*: The smallest device, the _HOME_, is the only one with only three power sockets and only available in Germany.
* *simple-firmware*: The _PRO_ and _REDUNDANT_ have eight power sockets and a similar (simplified) firmware as the _HOME_.
* *advanced-firmware*: All others (_ADV_, _IO_, and the different _HUT_ variants) have eight power sockets / relays, eight IO ports, and an advanced firmware.

An [additional sensor](https://en.anel.eu/?src=/produkte/sensor_1/sensor_1.htm) may be used for monitoring temperature, humidity, and brightness.
The sensor can be attached to a _HUT_ device via an Ethernet cable (max length is 50m).


## Discovery

Devices can be discovered automatically if their UDP ports are configured as follows:

* 75 / 77 (default)
* 750 / 770
* 7500 / 7700
* 7750 / 7770

If a device is found for a specific port (excluding the default port), the subsequent port is also scanned, e.g. 7500/7700 &rarr; 7501/7701 &rarr; 7502/7702 &rarr; etc.

Depending on the network switch and router devices, discovery may or may not work on wireless networks.
It should work reliably though on local wired networks.


## Thing Configuration

Each Thing requires the following configuration parameters.

| Parameter             | Type    | Default     | Required | Description |
|-----------------------|---------|-------------|----------|-------------|
| Hostname / IP address | String  | net-control | yes      | Hostname or IP address of the device |
| Send Port             | Integer | 75          | yes      | UDP port to send data to the device (in the anel web UI, it's the receive port!) |
| Receive Port          | Integer | 77          | yes      | UDP port to receive data from the device (in the anel web UI, it's the send port!) |
| User                  | String  | user7       | yes      | User to access the device (make sure it has rights to change relay / IO states!) |
| Password              | String  | anel        | yes      | Password of the given user |

For multiple devices, please use exclusive UDP ports for each device.
Ports above 1024 are recommended because they are outside the range of system ports.

Possible entries in your thing file could be (thing types _home_, _simple-firmware_, and _advanced-firmware_ are explained above in _Supported Things_):

```
anel:home:mydevice1 [hostname="192.168.0.101", udpSendPort=7500, udpReceivePort=7700, user="user7", password="anel"]
anel:simple-firmware:mydevice2 [hostname="192.168.0.102", udpSendPort=7501, udpReceivePort=7701, user="user7", password="anel"]
anel:advanced-firmware:mydevice3 [hostname="192.168.0.103", udpSendPort=7502, udpReceivePort=7702, user="user7", password="anel"]
anel:advanced-firmware:mydevice4 [hostname="192.168.0.104", udpSendPort=7503, udpReceivePort=7703, user="user7", password="anel"]
```


## Channels

Depending on the thing type, the following channels are available.

| Channel ID         | Item Type          | Supported Things  | Read Only | Description |
|--------------------|--------------------|-------------------|-----------|-------------|
| prop#name          | String             | all               | yes       | Name of the device |
| prop#temperature   | Number:Temperature | simple / advanced | yes       | Temperature of the integrated sensor |
| sensor#temperature | Number:Temperature | advanced          | yes       | Temperature of the optional external sensor |
| sensor#humidity    | Number             | advanced          | yes       | Humidity of the optional external sensor |
| sensor#brightness  | Number             | advanced          | yes       | Brightness of the optional external sensor |
| r1#name            | String             | all               | yes       | Name of relay / socket 1 |
| r2#name            | String             | all               | yes       | Name of relay / socket 2 |
| r3#name            | String             | all               | yes       | Name of relay / socket 3 |
| r4#name            | String             | simple / advanced | yes       | Name of relay / socket 4 |
| r5#name            | String             | simple / advanced | yes       | Name of relay / socket 5 |
| r6#name            | String             | simple / advanced | yes       | Name of relay / socket 6 |
| r7#name            | String             | simple / advanced | yes       | Name of relay / socket 7 |
| r8#name            | String             | simple / advanced | yes       | Name of relay / socket 8 |
| r1#state           | Switch             | all               | no *      | State of relay / socket 1 |
| r2#state           | Switch             | all               | no *      | State of relay / socket 2 |
| r3#state           | Switch             | all               | no *      | State of relay / socket 3 |
| r4#state           | Switch             | simple / advanced | no *      | State of relay / socket 4 |
| r5#state           | Switch             | simple / advanced | no *      | State of relay / socket 5 |
| r6#state           | Switch             | simple / advanced | no *      | State of relay / socket 6 |
| r7#state           | Switch             | simple / advanced | no *      | State of relay / socket 7 |
| r8#state           | Switch             | simple / advanced | no *      | State of relay / socket 8 |
| r1#locked          | Switch             | all               | yes       | Whether or not relay / socket 1 is locked |
| r2#locked          | Switch             | all               | yes       | Whether or not relay / socket 2 is locked |
| r3#locked          | Switch             | all               | yes       | Whether or not relay / socket 3 is locked |
| r4#locked          | Switch             | simple / advanced | yes       | Whether or not relay / socket 4 is locked |
| r5#locked          | Switch             | simple / advanced | yes       | Whether or not relay / socket 5 is locked |
| r6#locked          | Switch             | simple / advanced | yes       | Whether or not relay / socket 6 is locked |
| r7#locked          | Switch             | simple / advanced | yes       | Whether or not relay / socket 7 is locked |
| r8#locked          | Switch             | simple / advanced | yes       | Whether or not relay / socket 8 is locked |
| io1#name           | String             | advanced          | yes       | Name of IO port 1 |
| io2#name           | String             | advanced          | yes       | Name of IO port 2 |
| io3#name           | String             | advanced          | yes       | Name of IO port 3 |
| io4#name           | String             | advanced          | yes       | Name of IO port 4 |
| io5#name           | String             | advanced          | yes       | Name of IO port 5 |
| io6#name           | String             | advanced          | yes       | Name of IO port 6 |
| io7#name           | String             | advanced          | yes       | Name of IO port 7 |
| io8#name           | String             | advanced          | yes       | Name of IO port 8 |
| io1#state          | Switch             | advanced          | no **     | State of IO port 1 |
| io2#state          | Switch             | advanced          | no **     | State of IO port 2 |
| io3#state          | Switch             | advanced          | no **     | State of IO port 3 |
| io4#state          | Switch             | advanced          | no **     | State of IO port 4 |
| io5#state          | Switch             | advanced          | no **     | State of IO port 5 |
| io6#state          | Switch             | advanced          | no **     | State of IO port 6 |
| io7#state          | Switch             | advanced          | no **     | State of IO port 7 |
| io8#state          | Switch             | advanced          | no **     | State of IO port 8 |
| io1#mode           | Switch             | advanced          | yes       | Mode of port 1: _ON_ = input, _OFF_ = output |
| io2#mode           | Switch             | advanced          | yes       | Mode of port 2: _ON_ = input, _OFF_ = output |
| io3#mode           | Switch             | advanced          | yes       | Mode of port 3: _ON_ = input, _OFF_ = output |
| io4#mode           | Switch             | advanced          | yes       | Mode of port 4: _ON_ = input, _OFF_ = output |
| io5#mode           | Switch             | advanced          | yes       | Mode of port 5: _ON_ = input, _OFF_ = output |
| io6#mode           | Switch             | advanced          | yes       | Mode of port 6: _ON_ = input, _OFF_ = output |
| io7#mode           | Switch             | advanced          | yes       | Mode of port 7: _ON_ = input, _OFF_ = output |
| io8#mode           | Switch             | advanced          | yes       | Mode of port 8: _ON_ = input, _OFF_ = output |

\* Relay / socket state is read-only if it is locked; otherwise it is changeable.<br/>
\** IO port state is read-only if its mode is _input_, it is changeable if its mode is _output_.


## Full Example

`.things` file:

```
Thing anel:advanced-firmware:anel1 "Anel1" [hostname="192.168.0.100", udpSendPort=7500, udpReceivePort=7700, user="user7", password="anel"]
```

`.items` file:

```
// device properties
String              anel1name               "Anel1 Name"                {channel="anel:advanced-firmware:anel1:prop#name"}
Number:Temperature  anel1temperature        "Anel1 Temperature"         {channel="anel:advanced-firmware:anel1:prop#temperature"}

// external sensor properties
Number:Temperature  anel1sensorTemperature  "Anel1 Sensor Temperature"  {channel="anel:advanced-firmware:anel1:sensor#temperature"}
Number              anel1sensorHumidity     "Anel1 Sensor Humidity"     {channel="anel:advanced-firmware:anel1:sensor#humidity"}
Number              anel1sensorBrightness   "Anel1 Sensor Brightness"   {channel="anel:advanced-firmware:anel1:sensor#brightness"}

// relay names and states
String  anel1relay1name    "Anel1 Relay1 name"    {channel="anel:advanced-firmware:anel1:r1#name"}
Switch  anel1relay1locked  "Anel1 Relay1 locked"  {channel="anel:advanced-firmware:anel1:r1#locked"}
Switch  anel1relay1state   "Anel1 Relay1"         {channel="anel:advanced-firmware:anel1:r1#state"}
Switch  anel1relay2state   "Anel1 Relay2"         {channel="anel:advanced-firmware:anel1:r2#state"}
Switch  anel1relay3state   "Anel1 Relay3"         {channel="anel:advanced-firmware:anel1:r3#state"}
Switch  anel1relay4state   "Anel1 Relay4"         {channel="anel:advanced-firmware:anel1:r4#state"}
Switch  anel1relay5state   "Light Bedroom"        {channel="anel:advanced-firmware:anel1:r5#state"}
Switch  anel1relay6state   "Doorbell"             {channel="anel:advanced-firmware:anel1:r6#state"}
Switch  anel1relay7state   "Socket TV"            {channel="anel:advanced-firmware:anel1:r7#state"}
Switch  anel1relay8state   "Socket Terrace"       {channel="anel:advanced-firmware:anel1:r8#state"}

// IO port names and states
String  anel1io1name   "Anel1 IO1 name"     {channel="anel:advanced-firmware:anel1:io1#name"}
Switch  anel1io1mode   "Anel1 IO1 mode"     {channel="anel:advanced-firmware:anel1:io1#mode"}
Switch  anel1io1state  "Anel1 IO1"          {channel="anel:advanced-firmware:anel1:io1#state"}
Switch  anel1io2state  "Anel1 IO2"          {channel="anel:advanced-firmware:anel1:io2#state"}
Switch  anel1io3state  "Anel1 IO3"          {channel="anel:advanced-firmware:anel1:io3#state"}
Switch  anel1io4state  "Anel1 IO4"          {channel="anel:advanced-firmware:anel1:io4#state"}
Switch  anel1io5state  "Switch Bedroom"     {channel="anel:advanced-firmware:anel1:io5#state"}
Switch  anel1io6state  "Doorbell"           {channel="anel:advanced-firmware:anel1:io6#state"}
Switch  anel1io7state  "Switch Office"      {channel="anel:advanced-firmware:anel1:io7#state"}
Switch  anel1io8state  "Reed Contact Door"  {channel="anel:advanced-firmware:anel1:io8#state"}
```

`.sitemap` file:

```
sitemap anel label="Anel NET-PwrCtrl" {
  Frame label="Device and Sensor" {
    Text   item=anel1name               label="Anel1 Name"
    Text   item=anel1temperature        label="Anel1 Temperature [%.1f °C]"
    Text   item=anel1sensorTemperature  label="Anel1 Sensor Temperature [%.1f °C]"
    Text   item=anel1sensorHumidity     label="Anel1 Sensor Humidity [%.1f]"
    Text   item=anel1sensorBrightness   label="Anel1 Sensor Brightness [%.1f]"
  }
  Frame label="Relays" {
    Text   item=anel1relay1name  label="Relay 1 name" labelcolor=[anel1relay1locked==ON="green",anel1relay1locked==OFF="maroon"]
    Switch item=anel1relay1state
    Switch item=anel1relay2state
    Switch item=anel1relay3state
    Switch item=anel1relay4state
    Switch item=anel1relay5state
    Switch item=anel1relay6state
    Switch item=anel1relay7state
    Switch item=anel1relay8state
  }
  Frame label="IO Ports" {
    Text   item=anel1io1name     label="IO 1 name"    labelcolor=[anel1io1mode==OFF="green",anel1io1mode==ON="maroon"]
    Switch item=anel1io1state
    Switch item=anel1io2state
    Switch item=anel1io3state
    Switch item=anel1io4state
    Switch item=anel1io5state
    Switch item=anel1io6state
    Switch item=anel1io7state
    Switch item=anel1io8state
  }
}
```

The relay / IO port names are rarely useful because you probably set similar (static) labels for the state items.<br/>
The locked state / IO mode is also rarely relevant in practice, because it typically doesn't change.

`.rules` file:

```
rule "doorbell only at daytime"
when Item anel1io6state changed then
  if (now.getHoursOfDay >= 6 && now.getHoursOfDay <= 22) {
    anel1relay6state.sendCommand(if (anel1io6state.state != ON) ON else OFF)
  }
  someNotificationItem.sendCommand("Someone just rang the doorbell")
end
```


## Reference Documentation

The UDP protocol of Anel devices is explained [here](https://forum.anel.eu/viewtopic.php?f=16&t=207).

