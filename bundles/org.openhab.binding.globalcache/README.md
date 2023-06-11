# GlobalCache Binding

The [GlobalCache](https://www.globalcache.com) binding enables communication
between openHAB and GlobalCache [GC-100](https://www.globalcache.com/products/gc-100/) and [iTach](https://www.globalcache.com/products/itach/) family of devices.
GlobalCache devices enable the control and automation of infrared, serial, and contact closure devices over a wired or wireless IP network.

## Overview

The GlobalCache binding discovers GC-100 and iTach devices on the network, and creates an inbox entry for each discovered device.
Once added as a thing, the user can complete the configuration of the device, such as adding the name of the MAP file that contains IR and/or serial codes.

## Devices Supported

Devices are discovered dynamically.
There is a single thing created for each physical GC-100 or iTach device discovered on the network.
Each thing has channels that correspond to the physical connectors on the device.

Currently supported devices include:

| Device                            | Thing ID  |
|-----------------------------------|-----------|
| iTach WF2IR and IP2IR             | itachIR   |
| iTach WF2SL and IP2SL             | itachSL   |
| iTach WF2CC and IP2CC             | itachCC   |
| iTach Flex Ethernet and Flex WiFi | itachFlex |
| GC-100-6                          | gc100_06  |
| GC-100-12                         | gc100_12  |
| ZMOTE WiFi Universal Remote       | zmote     |

## Thing Configuration

The binding uses the MAP transformation service to convert commands into IR and/or serial codes.
If not already installed, the MAP transformation service is installed automatically when the binding is installed.

In the event that the device's IP address is changed, the thing configuration must be updated manually, as the device cannot auto-discover the change.

### iTach IR and ZMOTE

The iTach IR (Infrared) and ZMOTE devices have the following configuration parameters.

| Parameter    | Parameter ID | Required/Optional | Description |
|--------------|--------------|-------------------|-------------|
| IP Address   | ipAddress    | Required          | The device's IP address. |
| MAP Filename | mapFilename  | Required          | The MAP file that contains mappings of commands to the IR codes. |

### iTach SL

The iTach SL (Serial) device has the following configuration parameters.
Note that you must use the iTach SL's web application to set the serial port
parameters for **baud rate**, **flow control**, and **parity** to match the configuration of the end device to which the iTach is connected.

| Parameter      | Parameter ID  | Required/Optional | Description |
|----------------|---------------|-------------------|-------------|
| IP Address     | ipAddress     | Required          | The device's IP address. |
| MAP Filename   | mapFilename   | Required          | The MAP file that contains mappings of commands to the serial codes. |
| Enable Two Way | enableTwoWay1 | Optional          | Enable two-way communication with the device. By default, the binding only sends commands to the device. |
| EOM Delimiter  | eomDelimiter1 | Optional          | The End-of-Message delimiter used to identify the end of a message that the binding received from the device. |

### iTach CC

The iTach CC (Contact Closure) device has the following configuration parameters.

| Parameter    | Parameter ID | Required/Optional | Description |
|--------------|--------------|-------------------|-------------|
| IP Address   | ipAddress    | Required          | The device's IP address. |

### iTach Flex

The iTach Flex device has the following configuration parameters.
Note that you must set the Active Cable configuration parameter to match how the Flex is configured.
Available options are Infrared, Serial, and Relay/Sensor.
The default is Infrared.

When the iTach Fles is configured for serial operation, you must use the iTach Flex's web application to set the serial port
parameters for **baud rate**, **flow control**, and **parity** to match the configuration of the end device to which the iTach Flex is connected.

| Parameter      | Parameter ID  | Required/Optional | Description |
|----------------|---------------|-------------------|-------------|
| IP Address     | ipAddress     | Required          | The device's IP address. |
| MAP Filename   | mapFilename   | Required          | The MAP file that contains mappings of commands to the IR and/or serial codes. |
| Active Cable   | activeCable   | Required          | Available options are Infrared (FLEX_INFRARED), Serial (FLEX_SERIAL) or Relay/Sensor (FLEX_RELAY). |
| Enable Two Way | enableTwoWay1 | Optional          | Enable two-way communication with the device. By default, the binding only sends commands to the device. |
| EOM Delimiter  | eomDelimiter1 | Optional          | The End-of-Message delimiter used to identify the end of a message that the binding received from the device. |

### GC-100-6

The GC-100-6 device has the following configuration parameters.
Note that you must use the GC-100-6's web application to set the serial port
parameters for **baud rate**, **flow control**, and **parity** to match the configuration of the end device to which the GC-100-6 is connected.

| Parameter      | Parameter ID  | Required/Optional | Description |
|----------------|---------------|-------------------|-------------|
| IP Address     | ipAddress     | Required          | The device's IP address. |
| MAP Filename   | mapFilename   | Required          | The MAP file that contains mappings of commands to the IR and serial codes. |
| Enable Two Way | enableTwoWay1 | Optional          | Enable two-way communication with the device. By default, the binding only sends commands to the device. |
| EOM Delimiter  | eomDelimiter1 | Optional          | The End-of-Message delimiter used to identify the end of a message that the binding received from the device. |

### GC-100-12

The GC-100-12 device has the following configuration parameters.
Note that you must use the GC-100-12's web application to set the serial port
parameters for **baud rate**, **flow control**, and **parity** to match the configuration of the end device to which the GC-100-12 is connected.

| Parameter        | Parameter ID  | Required/Optional | Description |
|------------------|---------------|-------------------|-------------|
| IP Address       | ipAddress     | Required          | The device's IP address. |
| MAP Filename     | mapFilename   | Required          | The MAP file that contains mappings of commands to the IR and/or serial codes. |
| Enable Two Way 1 | enableTwoWay1 | Optional          | Enable two-way communication between the binding and the device on serial port #1. By default, the binding only sends commands to the device. |
| EOM Delimiter 1  | eomDelimiter1 | Optional          | The End-of-Message delimiter used to identify the end of a message that the binding received from the device on serial port #1. |
| Enable Two Way 2 | enableTwoWay2 | Optional          | Enable two-way communication between the binding and the device on serial port #2. By default, the binding only sends commands to the device. |
| EOM Delimiter 2  | eomDelimiter2 | Optional          | The End-of-Message delimiter used to identify the end of a message that the binding received from the device on serial port #2. |

### Manual Thing Creation

Devices can be created in the _UI_, or by placing a _.things_ file in the _conf/things_ directory.
See example below.

### Binding Dependencies

The GlobalCache binding uses the **transform** binding to map commands to IR and serial codes.  See example below.

## Device Discovery

GlobalCache GC-100, iTach, and Zmote devices emit an **announcement beacon** every 10-20 seconds on multicast address 239.255.250.250:9131.
The GlobalCache binding will automatically detect those devices, then add them to the inbox.

Background discovery is **enabled** by default.
To disable background discovery, add the following line to the _conf/services/runtime.cfg_ file:

```text
discovery.globalcache:background=false
```

Note that automatic device discovery **will not work** with GC-100's running firmware earlier than v3.0 as those versions do not emit announcement beacons on the multicast address.
GC-100's running firmware earlier than v3.0 must be configured manually, either through the _UI_ or using a _.things_ file.
See below.

## Channels and Channel Types

There are four _channel types_ used across the GC-100 and iTach family of devices.

- Contact Closure (CC)
- Infrared (IR)
- Serial (SL)
- Serial Direct (SL)

_Channels_ follow a naming convention that relates to the physical configuration of the Global Cache device -- specifically the **module** and **connector** numbers.
For example, the channel name **m2c3** refers to connector 3 on module 2.

For iTach Flex devices, since they can be configured to support infrared, serial, or contact closure, channels are prefixed with ir-, sl-, and cc-, respectively.
For example, the IR channel on connector 3 on module 1 is named ir-m1c2.

## Infrared (IR) Channel

The _Infrared channel_ sends IR codes out the IR connector on the device.
For example, the following item links to the module 1 / connector 2 channel on an iTach IR device.

```java
String SamsungTV    "TV"     (gTheater)   { channel="globalcache:itachIR:000C1E0384A5:ir-m1#c2" }
```

The item definition for an iTach Flex Ethernet device would look like this.

```java
String SamsungTV    "TV"     (gTheater)   { channel="globalcache:itachFlexEth:000C1E077BE1:ir-m1#c2" }
```

### How to Specify IR Codes

IR codes are contained in a MAP file contained within the conf/transform directory.
See example below.

### Supported IR Code Formats

Two different formatting methods are supported: Global Cache and hex code.
Here's an example of the Global Cache format:

```text
38000,1,1,340,170,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,63,21,63,21,63,21,63,21,63,21,21,21,21,21,1691,340,85,21,3753
```

Here's an example of the hex code format:

```text
0000 006E 0000 0024 0154 00AA 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 003F 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 003F 0015 003F 0015 003F 0015 003F 0015 003F 0015 003F 0015 003F 0015 0015 0015 0015 0015 069B 0154 0055 0015 0EA9
```

The [Global Cache iConvert utility](https://www.globalcache.com/files/docs/gc_iconvert_relnotes.txt) can be used to convert between the two formats, if desired.
The Global Cache iConvert utility is available on the Windows platform only.
A third party macOS port, [iConvert GC](https://www.rmartijnr.eu/iconvert.html), is available.

Global Cache also maintains an online [IR Control Tower database](https://irdb.globalcache.com/) of IR codes.
There are numerous other sources of IR codes, such as iRule and RemoteCentral.

#### Unsupported Features

Currently, only the _IR Out_ and _IR Blaster_ connector configurations are supported.
Other settings, such as _Sensor In_, _Sensor Notify_, and _LED Lighting_, may be supported in the future.

## Contact Closure (CC) Channel

A _Contact Closure channel_ activates the contact closure (relay) on the iTach or GC-100 device.
For example, the following item links to the module 1, connector 1 channel on an iTach CC device.

```java
Switch MyRelay    "My Relay [%s]"  (gRelays)   { channel="globalcache:itachCC:000C1E039BCF:cc-m1#c1" }
```

The item definition for an iTach Flex WiFi device would look like this.

```java
String MyRelay     "My Relay [%s]"  (gRelays)   { channel="globalcache:itachFlex:000C01AF4990:cc-m1#c1" }
```

## Serial (SL) Channel

An _SL channel_ sends serial command strings out the serial connector on the device.
For example, the following item links to the module 1 connector 1 channel on a GC-100-6 device.

```java
String RS232ME      "My RS232-controlled Device"   { channel="globalcache:gc100_6:000C459A120A:sl-m1#c1" }
```

Serial commands strings are contained in a MAP file contained within the conf/transform directory.
Serial command strings can contain URL-encoded characters in order to represent special characters such as spaces, tabs, carriage returns, line feeds, etc.
See example below.

## Serial (SL) Direct Channel

The Serial Direct channel type enables serial commands to be sent directly to the device without attempting to map the command using the transformation service.
This is useful in rules where the serial command might be constructed "on the fly" in the body of the rule.
For example, the following item links to the module 1 connector 1 channel on an iTach Flex device.

```java
String RUSSCAA66    "Russound CAA66"   { channel="globalcache:itachFlex:000C45D530B9:sl-m1#c1-direct" }
```

## Serial (SL) Receive Channel

The Serial Receive channel receives feedback from the device connected to the GlobalCache's serial port.
You enable this functionality by setting the **Enable Two Way** switch to ON in the thing configuration.
In addition, you must set the **End-of-Message Delimiter** in the thing configuration.
The End-of-Message can be a single character, or a sequence of characters.
Use URL encoding for non-printable characters.

For example, the following item links to the receive channel on module 1 connector 1 on a GC-100.
A rule that looks for updates on this item will be able to process messages sent from the device connected to the GlobalCache's serial port.

```java
String RUSSCAA66_Receive    "Russound CAA66 Receive"   { channel="globalcache:gc100_06:000C1EFFF039:sl-m1#c1-receive" }
```

Here are some examples of common **End-ofMessage** delimiters.

```text
%0D%0A      Carriage return / Line feed
%0D         Carriage return
%F7         Russound RNET message terminator
```

### Example

#### MAP File

```text
# Harmon Kardon AVR-245 Home Theater Receiver
HKAVR245_POWER_ON   = 38000,1,1,340,170,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,63,21,63,21,63,21,63,21,63,21,21,21,21,21,1691,340,85,21,3753
HKAVR245_POWER_OFF  = 38000,1,1,340,170,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,21,21,63,21,63,21,63,21,63,21,63,21,21,21,21,21,63,21,21,21,21,21,21,21,21,21,21,21,63,21,63,21,21,21,1691,340,85,21,3753
HKAVR245_VOLUME_UP  = 38000,1,1,340,170,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,21,21,63,21,63,21,63,21,21,21,21,21,21,21,63,21,63,21,21,21,21,21,21,21,63,21,63,21,63,21,21,21,21,21,1691,340,85,21,3753
HKAVR245_VOLUME_DOWN    = 38000,1,1,340,170,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,21,21,21,21,21,21,21,21,63,21,21,21,21,21,63,21,63,21,63,21,63,21,63,21,21,21,63,21,63,21,21,21,21,21,1691,340,85,21,3753
HKAVR245_MUTE       = 38000,1,1,340,170,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,21,21,63,21,21,21,21,21,21,21,21,21,21,21,63,21,63,21,21,21,63,21,63,21,63,21,63,21,63,21,21,21,21,21,1691,340,85,21,3753
HKAVR245_HDMI1      = 38000,1,69,345,174,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,64,21,21,21,21,21,21,21,21,21,64,21,64,21,64,21,21,21,21,21,64,21,21,21,21,21,64,21,64,21,64,21,64,21,64,21,21,21,64,21,64,21,21,21,21,21,21,21,21,21,1725,345,86,21,3708
HKAVR245_HDMI2      = 38000,1,69,345,174,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,64,21,21,21,21,21,21,21,21,21,64,21,64,21,64,21,21,21,64,21,64,21,21,21,21,21,64,21,64,21,64,21,64,21,21,21,21,21,64,21,64,21,21,21,21,21,21,21,21,21,1725,345,86,21,3708

# Samsung HL-S Series DLP TV
SAMSUNGHLS_POWER_ON = 38000,1,1,170,169,21,64,21,64,21,64,21,21,21,21,21,21,21,21,21,21,21,64,21,64,21,64,21,21,21,21,21,21,21,21,21,21,21,64,21,21,21,21,21,64,21,64,21,21,21,21,21,64,21,21,21,64,21,64,21,21,21,21,21,64,21,64,21,21,21,1673
SAMSUNGHLS_POWER_OFF    = 38000,1,1,170,169,21,64,21,64,21,64,21,21,21,21,21,21,21,21,21,21,21,64,21,64,21,64,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,64,21,64,21,21,21,21,21,64,21,64,21,64,21,64,21,21,21,21,21,64,21,64,21,21,21,1673
SAMSUNGHLS_HDMI1    = 38000,1,1,170,169,21,64,21,64,21,64,21,21,21,21,21,21,21,21,21,21,21,64,21,64,21,64,21,21,21,21,21,21,21,21,21,21,21,64,21,64,21,21,21,64,21,21,21,21,21,21,21,64,21,21,21,21,21,64,21,21,21,64,21,64,21,64,21,21,21,1673
SAMSUNGHLS_HDMI2    = 38000,1,1,170,169,21,64,21,64,21,64,21,21,21,21,21,21,21,21,21,21,21,64,21,64,21,64,21,21,21,21,21,21,21,21,21,21,21,64,21,21,21,21,21,64,21,21,21,64,21,64,21,64,21,21,21,64,21,64,21,21,21,64,21,21,21,21,21,21,21,1673


# Xfinity Pace XG2V2-P Cable Box
XFINITYXG2_GUIDE    = 38000,1,37,8,34,8,65,8,29,8,106,8,50,8,50,8,44,8,101,8,525,8,34,8,60,8,29,8,29,8,39,8,65,8,29,8,29,8,3058,8,34,8,65,8,29,8,106,8,50,8,50,8,44,8,101,8,525,8,34,8,101,8,70,8,29,8,39,8,65,8,29,8,29,8,3058
XFINITYXG2_EXIT     = 38000,1,37,8,34,8,65,8,29,8,106,8,50,8,50,8,44,8,101,8,525,8,34,8,44,8,29,8,29,8,39,8,81,8,29,8,29,8,3058,8,34,8,65,8,29,8,106,8,50,8,50,8,44,8,101,8,525,8,34,8,86,8,70,8,29,8,39,8,81,8,29,8,29,8,3058

# Serial commands
# Represents the command string "POWER ON" followed by carriage return line feed
RS232ME_POWER_ON = POWER%20ON%0D%0A
# Represents the command string "POWER OFF" followed by carriage return line feed
RS232ME_POWER_OFF = POWER%20OFF%0D%0A
# Represents the command string "VOLUME UP" followed by carriage return line feed
RS232ME_VOLUME_UP   = VOLUME%20UP%0D%0A
# Represents the command string "VOLUME DOWN" followed by carriage return line feed
RS232ME_VOLUME_DOWN = VOLUME%20DOWN%0D%0A
```

#### Items File

```java
Switch ContactClosure1  "Relay on Connector 1"              { channel="globalcache:itachCC:000C1E017BCF:cc-m1#c1" }
Switch ContactClosure2  "Relay on Connector 2"              { channel="globalcache:itachCC:000C1E017BCF:cc-m1#c2" }
Switch ContactClosure3  "Relay on Connector 3"              { channel="globalcache:itachCC:000C1E017BCF:cc-m1#c3" }

Switch GarageDoor       "Open/Close Garage Door"
Switch GarageLight      "Garage Door Light"

String XFINITYFR        "Xfinity Box in Family Room"        { channel="globalcache:itachIR:000C1E038B75:ir-m1#c1" }
String HKAVR245         "Harmon Kardon AVR-245 Receiver"    { channel="globalcache:itachIR:000C1E038B75:ir-m1#c2" }
String SAMSUNGHLS       "Samsung HL-S DLP TV"               { channel="globalcache:itachIR:000C1E038B75:ir-m1#c3" }

String RS232ME          "Preamp"                            { channel="globalcache:itachIR:000C7720B39F:sl-m1#c1" }

String RUSSCAA66        "Russound CAA66"                    { channel="globalcache:itachFlex:000C45D530B9:sl-m1#c1-direct" }

String ZSAMSUNGHLS      "Samsung HL-S DLP TV"               { channel="globalcache:zmote:CI00073306:ir-m1-c1#c1" }
```

#### Sitemap File

This is an example of how to use contact closure, infrared, and serial devices in a sitemap.

```perl
Frame label="Contact Closure" {
    Switch item=ContactClosure1 label="Open/Close Garage Door"
    Switch item=ContactClosure2 label="Light on Garage Door Opener"
    Switch item=ContactClosure3 label="Unused"
}

Frame label="Harmon Kardon Receiver" {
    Switch item=HKAVR245 label="Power" mappings=[HKAVR245_POWER_ON="On", HKAVR245_POWER_OFF="Off"]
    Switch item=HKAVR245 label="Volume" mappings=[HKAVR245_MUTE="Mute", HKAVR245_VOLUME_UP="Up", HKAVR245_VOLUME_DOWN="Down"]
    Switch item=HKAVR245 label="Select Input" mappings=[HKAVR245_HDMI1="HDMI 1", HKAVR245_HDMI2="HDMI 2"]
}

Frame label="Samsung TV" {
    Switch item=SAMSUNGHLS label="Power" mappings=[SAMSUNGHLS_POWER_ON="On",SAMSUNGHLS_POWER_OFF="Off"]
}

Frame label="Xfinity One" {
    Switch item=XFINITYFR label="Guide" mappings=[XFINITYXG2_GUIDE="Guide",XFINITYXG2_EXIT="Exit"]
}

Frame label="Preamp" {
    Switch item=RS232ME label="Power" mappings=[RS232ME_POWER_ON="On",RS232ME_POWER_OFF="Off"]
}

Frame label="Garage Door" {
    Switch item=Garage_Door label="Open/Close" mappings=[ON="Do It"]
}
```

#### Rule file

This is an example of how to use a Contact Closure channel within a rule to implement a momentary contact switch, which could be used to trigger a garage door opener.

```java
var boolean isRunning = false

rule "Example Garage Door Opener"
when
    Item Garage_Door received command
then
    if (isRunning == false ) {
        isRunning = true
        ContactClosure1.sendCommand(ON)
        Thread.sleep(750)
        ContactClosure1.sendCommand(OFF)
        isRunning = false
    }      
end
```

This is an example of how to send IR and/or serial commands from within a rule.

```java
rule "AV Power On/Off"
when
    Item AVPowerOn received command
then
    if(receivedCommand == ON) {
        SAMSUNGHLS.sendCommand("SAMSUNGHLS_POWER_ON")
        HKAVR245.sendCommand("HKAVR245_POWER_ON")
    }
    else {
        SAMSUNGHLS.sendCommand("SAMSUNGHLS_POWER_OFF")
        HKAVR245.sendCommand("HKAVR245_POWER_OFF")
    }
end
```

This is an example of how to send a serial command directly from within a rule.

```java
rule "Russound Set Zone 1 Volume to 20"
when
    Item RussoundSetVolume received command
then
    RUSSCAA66.sendCommand("%F0%00%00%7F%00%00%70%05%02%02%00%00%F1%21%00%14%00%00%00%01%23%F7")
end
```

### Manual Thing Creation

Place a file named _globalcache.things_ in the _conf/things_ directory.
The file should contain lines formatted like this.

```java
globalcache:itachCC:000CFF17B106 [ ipAddress="192.168.12.62" ]
globalcache:itachIR:000C0B1E54A0 [ ipAddress="192.168.12.63", mapFilename="ir-codes.map" ]
globalcache:itachSL:000CF886B107 [ ipAddress="192.168.12.64", mapFilename="serial-codes.map"  ]
globalcache:itachFlex:000C8A76E610 [ ipAddress="192.168.12.65", mapFilename="ir-codes.map", activeCable="FLEX_INFRARED"]
globalcache:itachFlex:000C07BA7E11 [ ipAddress="192.168.12.66", mapFilename="serial-codes.map", activeCable="FLEX_SERIAL"]
globalcache:itachFlex:000CED0B3402 [ ipAddress="192.168.12.67", activeCable="FLEX_RELAY"]
globalcache:gc100_06:000C1065AE17 [ ipAddress="192.168.12.68", mapFilename="ir-serial-codes.map" ]
globalcache:gc100_12:000C162D7902 [ ipAddress="192.168.12.69", mapFilename="ir-serial-codes.map" ]
globalcache:zmote:CI00073306 [ ipAddress="192.168.12.142", mapFilename="ir-serial-codes.map" ]
```
