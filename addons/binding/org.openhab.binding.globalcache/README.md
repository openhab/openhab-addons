# GlobalCache Binding

The [GlobalCache](http://www.globalcache.com) binding is used to enable communication between openHAB and GlobalCache [GC-100](http://www.globalcache.com/products/gc-100/) and [iTach](http://www.globalcache.com/products/itach/) family of devices.
Global Cache devices enable the control and automation of infrared, serial, and contact closure devices through an IP network (wired or wireless).

## Overview

The GlobalCache binding discovers GC-100 and iTach devices on the network, and creates an inbox entry for each discovered device.
Once added as a thing, the user can complete the configuration of the device, such as selecting a MAP file for IR and serial codes.

## Devices Supported

Devices are discovered dynamically.
There is a single thing created for each physical GC-100 or iTach device connected to the network.
Each thing has channels that correspond to the physical connectors on the device.

Currently supported devices include:

*   iTach WF2IR and IP2IR
*   iTach WF2CC and IP2CC
*   iTach WF2SL and IP2SL
*   iTach Flex Ethernet
*   iTach Flex Ethernet PoE
*   iTach Flex WiFi
*   GC-100-6
*   GC-100-12
*   ZMOTE Wi-Fi Universal Remote

## Device Discovery

GlobalCache GC-100, iTach, and Zmote devices emit an **announcement beacon** every 10-20 seconds on multicast address 239.255.250.250:9131.
The GlobalCache binding will automatically detect those devices, then add them to the inbox.

Background discovery is **enabled** by default.
To disable background discovery, add the following line to the *conf/services/runtime.cfg* file:

```
org.openhab.binding.globalcache.discovery.GlobalCacheDiscoveryService:backgroundDiscovery.enabled=false
```

Note that automatic device discovery **will not work** with GC-100's running firmware earlier than v3.0 as those versions do not emit announcement beacons on the multicast address.
GC-100's running firmware earlier than v3.0 must be configured manually, either through *Paper UI* or using a *.things* file.
See below.

## Thing Configuration

The iTach IR, iTach SL, GC-100, and Zmote devices require a MAP file in order to transform the openHAB command to an IR command or to a serial command.
In the thing configuration, enter the name of the MAP file containing the IR and/or serial codes ().
The MAP file should be placed in the *conf/transform* directory.
See example below.

For iTach SL and GC-100 devices that support serial connections, you must use the GlobalCache device web application to set the serial port parameters for **baud rate**, **flow control**, and **parity**.
These settings must match the serial port settings of the AV device being controlled.

For iTach Flex devices, you must set the Active Cable configuration parameter to match how the Flex is configured.
Available options are Infrared, Serial, and Relay/Sensor.
The default is Infrared.

The device's IP address is set at time of discovery.
However, in the event that the device's IP address is changed, the device IP address must be changed in the thing's configuration.

#### Manual Thing Creation

Devices can be manually created in the *PaperUI* or *HABmin*, or by placing a *.things* file in the *conf/things* directory.
See example below.

#### Binding Dependencies

The GlobalCache binding uses the **transform** binding to map commands to IR and serial codes.  See example below.

## Channels and Channel Types

There are four *channel types* used across the GC-100 and iTach family of devices.

-   Contact Closure (CC)
-   Infrared (IR)
-   Serial (SL)
-   Serial Direct (SL)

*Channels* follow a naming convention that relates to the physical configuration of the Global Cache device -- specifically the **module** and **connector** numbers.
For example, the channel name **m2c3** refers to connector 3 on module 2.

For iTach Flex devices, since they can be configured to support infrared, serial, or contact closure, channels are prefixed with ir-, sl-, and cc-, respectively.
For example, the IR channel on connector 3 on module 1 is named ir-m1c2.

## Infrared (IR) Channel

The *Infrared channel* sends IR codes out the IR connector on the device.
For example, the following item links to the module 1 / connector 2 channel on an iTach IR device.

```
String SamsungTV    "TV"     (gTheater)   { channel="globalcache:itachIR:000C1E0384A5:ir-m1#c2" }
```

The item definition for an iTach Flex Ethernet device would look like this.

```
String SamsungTV    "TV"     (gTheater)   { channel="globalcache:itachFlexEth:000C1E077BE1:ir-m1#c2" }
```

#### How to Specify IR Codes

IR codes are contained in a MAP file contained within the conf/transform directory.
See example below.

#### Supported IR Code Formats

Two different formatting methods are supported: Global Cache and hex code.
Here's an example of the Global Cache format:

```
38000,1,1,340,170,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,63,21,63,21,63,21,63,21,63,21,63,21,63,21,63,21,21,21,21,21,1691,340,85,21,3753
```

Here's an example of the hex code format:

```
0000 006E 0000 0024 0154 00AA 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 003F 0015 003F 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 0015 003F 0015 003F 0015 003F 0015 003F 0015 003F 0015 003F 0015 003F 0015 003F 0015 0015 0015 0015 0015 069B 0154 0055 0015 0EA9
```

The [Global Cache iConvert utility](http://www.globalcache.com/files/docs/gc_iconvert_relnotes.txt) can be used to convert between the two formats, if desired.
The iConvert utility is available on the Windows platform only.
Global Cache also maintains an online [IR Control Tower database](https://irdb.globalcache.com/) of IR codes.
There are numerous other sources of IR codes, such as iRule and RemoteCentral.

#### Unsupported Features

Currently, only the *IR Out* and *IR Blaster* connector configurations are supported.
Other settings, such as *Sensor In*, *Sensor Notify*, and *LED Lighting*, may be supported in the future.

## Contact Closure (CC) Channel

A *Contact Closure channel* activates the contact closure (relay) on the iTach or GC-100 device.  
For example, the following item links to the module 1, connector 1 channel on an iTach CC device.

```
Contact MyRelay    "My Relay [%s]"  (gRelays)   { channel="globalcache:itachCC:000C1E039BCF:cc-m1#c1" }
```

The item definition for an iTach Flex WiFi device would look like this.

```
String MyRelay     "My Relay [%s]"  (gRelays)   { channel="globalcache:itachFlex:000C01AF4990:cc-m1#c1" }
```

## Serial (SL) Channel

An *SL channel* sends serial command strings out the serial connector on the device.  
For example, the following item links to the module 1 connector 1 channel on a GC-100-6 device.

```
String RS232ME      "My RS232-controlled Device"   { channel="globalcache:gc100_6:000C459A120A:sl-m1#c1" }
```

Serial commands strings are contained in a MAP file contained within the conf/transform directory.
Serial command strings can contain URL-encoded characters in order to represent special characters such as spaces, tabs, carriage returns, line feeds, etc.
See example below.

## Serial (SL) Direct Channel

The Serial Direct channel type enables serial commands to be sent directly to the device without attempting to map the command using the transformation service.
This is useful in rules where the serial command might be constructed "on the fly" in the body of the rule.
For example, the following item links to the module 1 connector 1 channel on an iTach Flex device.

```
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

```
String RUSSCAA66_Receive    "Russound CAA66 Receive"   { channel="globalcache:gc100_06:000C1EFFF039:sl-m1#c1-receive" }
```
Here are some examples of common **End-ofMessage** delimiters.

```
%0D%0A      Carriage return / Line feed
%0D         Carriage return
%F7         Russound RNET message terminator
```

**Example**

### MAP File

```
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

### Items File

```
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

### Sitemap File

This is an example of how to use contact closure, infrared, and serial devices in a sitemap.

```
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

### Rule file

This is an example of how to use a Contact Closure channel within a rule to implement a momentary contact switch, which could be used to trigger a garage door opener.

```
var boolean isRunning = false

rule "Example Garage Door Opener"
when
    Item Garage_Door received command
then
    if (isRunning == false ) {
        isRunning = true
        sendCommand(ContactClosure1, ON)
        Thread.sleep(750)
        sendCommand(ContactClosure1, OFF)
        isRunning = false
    }      
end
```

This is an example of how to send IR and/or serial commands from within a rule.

```
rule "AV Power On/Off"
when
    Item AVPowerOn received command
then
    if(receivedCommand == ON) {
        sendCommand(SAMSUNGHLS, "SAMSUNGHLS_POWER_ON")
        sendCommand(HKAVR245, "HKAVR245_POWER_ON")
    }
    else {
        sendCommand(SAMSUNGHLS, "SAMSUNGHLS_POWER_OFF")
        sendCommand(HKAVR245, "HKAVR245_POWER_OFF")
    }
end
```

This is an example of how to send a serial command directly from within a rule.

```
rule "Russound Set Zone 1 Volume to 20"
when
    Item RussoundSetVolume received command
then
    sendCommand(RUSSCAA66, "%F0%00%00%7F%00%00%70%05%02%02%00%00%F1%21%00%14%00%00%00%01%23%F7")
end
```

### Manual Thing Creation

Place a file named *globalcache.things* in the *conf/things* directory.
The file should contain lines formatted like this.

```
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
