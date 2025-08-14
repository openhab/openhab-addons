# PHC Binding

This binding allows you to integrate modules(at the Moment AM, EM, JRM and DIM) of PHC, without the PHC control (STM), in openHAB.

The serial protocol is mainly extracted, with thanks to the developers from the projects [PHCtoUDP](https://sourceforge.net/projects/phctoudp/) and [OpenHC](https://sourceforge.net/projects/openhc/?source=directory).

The basics of the module bus protocol can also be found in the [Wiki of the PHC-Forum (german)](https://wiki.phc-forum.de/index.php/PHC-Protokoll_des_internen_Bus).
While the Wiki is offline you can find a PDF version in this [PHC-Forum post](https://phc-forum.de/index.php/forum/phc-programmierung/129-phc-protokoll?start=15#1329).

## Serial Communication

The binding was tested with QinHeng Electronics HL-340 USB-Serial adapter (RS485) and the Digitus DA-70157 (FTDI/FT323RL) on Raspbian Ubilinux (Up Board) and Windows 10:

| Device/OS                | adaptor       | result       |
|--------------------------|---------------|--------------|
| Windows 10               | HL-340        | ok           |
|                          | FTDI          | good         |
| Raspberry Pi 3B/Jessie   | HL-340        | not reliable |
|                          | FTDI          | doesn´t work |
|                          | on board      | bad          |
| Up Board/ubilinux(Jessie)| HL-340        | not reliable |
|                          | FTDI          | good         |

If there are many modules on one bridge, the initialization can take a few minutes. If it does not work you can plug in the modules one after the other.
Sometimes after initialization, you might have to switch two times or the reaction could be a bit slow, but after you used a channel it should all work fine.

For all devices running with Linux that use the ch341 driver (HL-340), the new version (ch34x) is needed.
A guide for installing the CH340/341 UART driver for Raspberry Pi is available in the [raspberrypi-ch340-driver GitHub repository](https://github.com/aperepel/raspberrypi-ch340-driver).

If you don´t have the same kernel as used in the guide you have to compile the module yourself. In the guide is described a specific way for the Raspberry Pi. With another Linux version you can go the normal way with linux-headers.

According to the [Wiki of the PHC-Forum](https://wiki.phc-forum.de/index.php/PHC-Protokoll_des_internen_Bus#USB_RS-485_Adapter) the newer version of the FTDI adapter doesn't really work anymore either.

In Linux amongst others the user 'openhab' must be added to the group 'dialout': ```sudo usermod -a -G dialout openhab``` For more information read the [installation guide](https://www.openhab.org/docs/installation/linux.html#recommended-additional-setup-steps).

### Connection

There are two alternatives, the first of which is much simpler.

#### Connection via power supply (simpler, preferred)

The simplest way would be to connect the RS485 adaptor to the PHC power supply like in the table below and Out at the power supply to the first module like the STM before.

|  adaptor | PHC power supply |
|----------|------------------|
| 485+     | +A               |
| 485-     | -B               |

#### Make a direct RJ12 connection

Connect a RJ12 plug with the RS485 adaptor and the power supply as follows.

| RJ12 like in picture below | The cores on the other side |
|----------------------------|-----------------------------|
| 0V                         | 0V on power supply          |
| B-                         | 485- on adaptor             |
| A+                         | 485+ on adaptor             |
| 24+                        | +24V on power supply        |

![RJ12 Connector](doc/RJ12-Connector.png)

## Bridge

The Bridge manages the communication between the things and the modules via a serial port (RS485).
It represents the STM.
At the Moment you can only use one Bridge (like one STM).

### Configurations

**Serial Port:** Type the serial port of the RS485 adaptor, e.g. COM3 (Windows) or /dev/ttyUSB0 (Linux).

## Supported Things

- **AM module:** This represents the AM module with 8 outgoing channels (relays).

- **EM module:** This represents the EM module with 16 incoming (switches) and 8 outgoing (for a LED in the switch) channels.

- **JRM module:** This represents the JRM module with 4 channels for Shutters.

- **DIM:** This represents the DM module with 2 dimmer channels.

## Discovery

Not implemented yet.

## Thing Configuration

A thing accords with a module in the PHC software and the channels (with linked items) accord with the inputs and outputs.
Please note, if you define the things manually (not in the UI) that the ThingID always have to be the address (like the PID switches on the module).

### Parameters

- **address:** Type the address of the module like the DIP switches (you can also find in the PHC software) of the module, e.g. 10110. (mandatory)

- **upDownTime[1-4] (only JRM):** (advanced) The time in seconds that the shutter needs to move up or down, with a resolution of 1/10 seconds. The default, if no value is specified, is 30 seconds.

- **dimTime[1-2] (only DIM):** (advanced) The time in seconds in that the dimmer should move 100%. The default is 2 seconds, then for example dimming from 0 to 100% takes 2 second.

## Channels

| Thing Type             | Channel-Group Id | Channels | Item Type        |
|------------------------|------------------|----------|------------------|
| AM                     | am               | 00-07    | Switch           |
| EM                     | em               | 00-15    | Switch(read only)|
| EM                     | emLed            | 00-07    | Switch           |
| JRM                    | jrm              | 00-03    | Rollershutter    |
| JRM                    | jrmT             | 00-03    | Number           |
| DIM                    | dim              | 00-01    | Dimmer           |
| DIM                    | dimT             | 00-01    | Number           |

**Channel UID:** ```phc:<Thing Type>:<ThingID>:<Channel Group>#<Channel>``` e.g. ```phc:AM:01101:am#03```

- **am:** Outgoing switch channels (relay).
- **em:** Incoming channels.
- **emLed:** Outgoing switch channels e.g. for LEDs in light shutters.
- **jrm:** Outgoing shutter channels.
- **jrmT:** Time for shutter channels in seconds with an accuracy of 1/10 seconds.
These channels are used instead of the configuration parameters.
If you send the time via this channel, the Binding uses this time till you send another.
After reboot the config parameter is used by default.
- **dim:** Outgoing dimmer channels.

## Full Example

.things

```java
Bridge phc:bridge:demo [port="/dev/ttyUSB0"]{
    // The ThingID have to be the address.
    Thing AM 01101 [address="01101"]
    Thing EM 00110 [address="00110"]
    Thing JRM 10111 [address="10111", upDownTime3=60, upDownTime4=20]
    Thing DIM 00000 [address="00000"]
```

.items

```java
//AM Module
Switch Switch_1 {channel="phc:AM:01101:am#00"}
Switch Switch_2 {channel="phc:AM:01101:am#01"}
Switch Switch_3 {channel="phc:AM:01101:am#02"}
...
Switch Switch_8 {channel="phc:AM:01101:am#07"}

//JRM Module
Rollershutter Shutter_1 {channel="phc:JRM:10111:jrm#00"}
Rollershutter Shutter_2 {channel="phc:JRM:10111:jrm#01"}
Rollershutter Shutter_3 {channel="phc:JRM:10111:jrm#02"}
Rollershutter Shutter_4 {channel="phc:JRM:10111:jrm#03"}

Number ShutterTime_1 {channel="phc:JRM:10111:jrmT#00"}

//DIM Module
Dimmer Dimmer_1 {channel="phc:DIM:00000:dim#00}
Dimmer Dimmer_2 {channel="phc:DIM:00000:dim#01}

// EM Module
Switch InputLed_3 {channel="phc:EM:00110:emLed#03"}

Switch Input_1 {channel="phc:EM:00110:em#00"}
Switch Input_2 {channel="phc:EM:00110:em#01"}
Switch Input_3 {channel="phc:EM:00110:em#02"}
...
Switch Input_16 {channel="phc:EM:00110:em#15"}
```
