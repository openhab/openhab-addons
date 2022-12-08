# Insteon Binding

Insteon is a home area networking technology developed primarily for connecting light switches and loads.
Insteon devices send messages either via the power line, or by means of radio frequency (RF) waves, or both (dual-band.
A considerable number of Insteon compatible devices such as switchable relays, thermostats, sensors etc are available.
More about Insteon can be found on [Wikipedia](https://en.wikipedia.org/wiki/Insteon).

This binding provides access to the Insteon network by means of either an Insteon PowerLinc Modem (PLM), a legacy Insteon Hub 2242-222 or the current 2245-222 Insteon Hub.
The modem can be connected to the openHAB server either via a serial port (Model 2413S) or a USB port (Model 2413U.
The Insteon PowerLinc Controller (Model 2414U) is not supported since it is a PLC not a PLM.
The modem can also be connected via TCP (such as ser2net.
The binding translates openHAB commands into Insteon messages and sends them on the Insteon network.
Relevant messages from the Insteon network (like notifications about switches being toggled) are picked up by the modem and converted to openHAB status updates by the binding.
The binding also supports sending and receiving of legacy X10 messages.

The binding does not support linking new devices on the fly, i.e. all devices must be linked with the modem _before_ starting the Insteon binding.

The openHAB binding supports minimal configuration of devices, currently only monitoring and sending messages.
For all other configuration and set up of devices, link the devices manually via the set buttons, or use the free [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) software.
The free HouseLinc software from Insteon can also be used for configuration, but it wipes the modem link database clean on its initial use, requiring to re-link the modem to all devices.

## Supported Things

| Thing  | Type   | Description                  |
|----------|--------|------------------------------|
| network  | Bridge | An insteon PLM or hub that is used to communicate with the Insteon devices |
|device| Thing | Insteon devices such as dimmers, keypads, sensors, etc. |

## Discovery

The network bridge is not automatically discovered, you will have to manually add the it yourself.
Upon proper configuration of the network bridge, the network device database will be downloaded.
Any Insteon device that exists in the database and is not currently configured is added to the inbox.
The naming convention is **Insteon Device AABBCC**, where AA, BB and CC are from the Insteon device address.
X10 devices are not auto discovered.

## Thing Configuration

### Network Configuration

The Insteon PLM or hub is configured with the following parameters:

| Parameter | Default | Required | Description |
|----------|---------:|--------:|-------------|
| port   |         |   Yes    | **Examples:**<br>- PLM on  Linux: `/dev/ttyS0` or `/dev/ttyUSB0`<br>- Smartenit ZBPLM on Linux: `/dev/ttyUSB0,baudRate=115200`<br>- PLM on Windows: `COM1`<br>- Current  hub (2245-222) at 192.168.1.100 on port 25105, with a poll interval of 1000 ms (1 second): `/hub2/my_user_name:my_password@192.168.1.100:25105,poll_time=1000`<br>- Legacy hub (2242-222) at 192.168.1.100 on port 9761:`/hub/192.168.1.100:9761`<br>- Networked PLM using ser2net at 192.168.1.100 on port 9761:`/tcp/192.168.1.100:9761` |
| devicePollIntervalSeconds | 300 |  No  | Poll interval of devices in seconds. Poll too often and you will overload the insteon network, leading to sluggish or no response when trying to send messages to devices. The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers. |
| additionalDevices | |       No     | File with additional device types. The syntax of the file is identical to the `device_types.xml` file in the source tree. Please remember to post successfully added device types to the openhab group so the developers can include them into the `device_types.xml` file! |
| additionalFeatures | |      No     | File with additional feature templates, like in the `device_features.xml` file in the source tree. |

>NOTE: For users upgrading from InsteonPLM, The parameter port_1 is now port.

### Device Configuration

The Insteon device is configured with the following required parameters:

| Parameter | Description |
|----------|-------------|
|address|Insteon or X10 address of the device. Insteon device addresses are in the format 'xx.xx.xx', and can be found on the device. X10 device address are in the format 'x.y' and are typically configured on the device.|
|productKey|Insteon binding product key that is used to identy the device. Every Insteon device type is uniquely identified by its Insteon product key, typically a six digit hex number. For some of the older device types (in particular the SwitchLinc switches and dimmers), Insteon does not give a product key, so an arbitrary fake one of the format Fxx.xx.xx (or Xxx.xx.xx for X10 devices) is assigned by the binding.|
|deviceConfig|Optional JSON object with device specific configuration. The JSON object will contain one or more key/value pairs. The key is a parameter for the device and the type of the value will vary.|

The following is a list of the product keys and associated devices.
These have been tested and should work out of the box:

| Model | Description | Product Key | tested by |
|-------|-------------|-------------|-----------|
| 2477D | SwitchLinc Dimmer | F00.00.01 | Bernd Pfrommer |
| 2477S | SwitchLinc Switch | F00.00.02 | Bernd Pfrommer |
| 2845-222 | Hidden Door Sensor | F00.00.03 | Josenivaldo Benito |
| 2876S | ICON Switch | F00.00.04 | Patrick Giasson |
| 2456D3 | LampLinc V2 | F00.00.05 | Patrick Giasson |
| 2442-222 | Micro Dimmer | F00.00.06 | Josenivaldo Benito |
| 2453-222 | DIN Rail On/Off | F00.00.07 | Josenivaldo Benito |
| 2452-222 | DIN Rail Dimmer | F00.00.08 | Josenivaldo Benito |
| 2458-A1 | MorningLinc RF Lock Controller | F00.00.09 | cdeadlock |
| 2852-222 | Leak Sensor | F00.00.0A | Kirk McCann |
| 2672-422 | LED Dimmer | F00.00.0B | ??? |
| 2476D | SwitchLinc Dimmer | F00.00.0C | LiberatorUSA |
| 2634-222 | On/Off Dual-Band Outdoor Module | F00.00.0D | LiberatorUSA |
| 2342-2 | Mini Remote | F00.00.10 | Bernd Pfrommer |
| 2663-222 | On/Off Outlet | 0x000039 | SwissKid |
| 2466D | ToggleLinc Dimmer | F00.00.11 | Rob Nielsen |
| 2466S | ToggleLinc Switch | F00.00.12 | Rob Nielsen |
| 2672-222 | LED Bulb | F00.00.13 | Rob Nielsen |
| 2487S | KeypadLinc On/Off 6-Button | F00.00.14 | Bernd Pfrommer |
| 2334-232 | KeypadLink Dimmer 6-Button | F00.00.15 | Rob Nielsen |
| 2334-232 | KeypadLink Dimmer 8-Button | F00.00.16 | Rob Nielsen |
| 2423A1 | iMeter Solo Power Meter | F00.00.17 | Rob Nielsen |
| 2423A1 | Thermostat 2441TH | F00.00.18 | Daniel Campbell, Bernd Pfrommer |
| 2457D2 | LampLinc Dimmer | F00.00.19 | Jonathan Huizingh |
| 2475SDB | In-LineLinc Relay | F00.00.1A | Jim Howard |
| 2635-222 | On/Off Module | F00.00.1B | Jonathan Huizingh |
| 2475F | FanLinc Module | F00.00.1C | Brian Tillman |
| 2456S3 | ApplianceLinc | F00.00.1D | ??? |
| 2674-222 | LED Bulb (recessed) | F00.00.1E | Steve Bate |
| 2477SA1 | 220V 30-amp Load Controller N/O | F00.00.1F | Shawn R. |
| 2342-222 | Mini Remote (8 Button) | F00.00.20 | Bernd Pfrommer |
| 2441V | Insteon Thermostat Adaptor for Venstar | F00.00.21 | Bernd Pfrommer |
| 2982-222 | Insteon Smoke Bridge | F00.00.22 | Bernd Pfrommer |
| 2487S | KeypadLinc On/Off 8-Button | F00.00.23 | Tom Weichmann |
| 2450 | IO Link | 0x00001A | Bernd Pfrommer |
| 2486D | KeypadLinc Dimmer | 0x000037 | Patrick Giasson, Joe Barnum |
| 2484DWH8 | KeypadLinc Countdown Timer | 0x000041 | Rob Nielsen |
| Various | PLM or hub | 0x000045 | Bernd Pfrommer |
| 2843-222 | Wireless Open/Close Sensor | 0x000049 | Josenivaldo Benito |
| 2842-222 | Motion Sensor | 0x00004A | Bernd Pfrommer |
| 2844-222 | Motion Sensor II | F00.00.24 | Rob Nielsen |
| 2486DWH8 | KeypadLinc Dimmer | 0x000051 | Chris Graham |
| 2472D | OutletLincDimmer | 0x000068 | Chris Graham |
| X10 switch | generic X10 switch | X00.00.01 | Bernd Pfrommer |
| X10 dimmer | generic X10 dimmer | X00.00.02 | Bernd Pfrommer |
| X10 motion | generic X10 motion sensor | X00.00.03 | Bernd Pfrommer |

## Channels

Below is the list of possible channels for the Insteon devices.
In order to determine which channels a device supports, you can look at the device in the UI, or with the command `display_devices` in the console.

| channel  | type   | description                  |
|----------|--------|------------------------------|
| acDelay | Number | AC Delay |
| backlightDuration | Number | Back Light Duration |
| batteryLevel | Number | Battery Level |
| batteryPercent | Number:Dimensionless | Battery Percent |
| batteryWatermarkLevel | Number | Battery Watermark Level |
| beep | Switch | Beep |
| bottomOutlet | Switch | Bottom Outlet |
| buttonA | Switch | Button A |
| buttonB | Switch | Button B |
| buttonC | Switch | Button C |
| buttonD | Switch | Button D |
| buttonE | Switch | Button E |
| buttonF | Switch | Button F |
| buttonG | Switch | Button G |
| buttonH | Switch | Button H |
| broadcastOnOff | Switch | Broadcast On/Off |
| contact | Contact | Contact |
| coolSetPoint | Number | Cool Set Point |
| dimmer | Dimmer | Dimmer |
| fan | Number | Fan |
| fanMode | Number | Fan Mode |
| fastOnOff | Switch | Fast On/Off |
| fastOnOffButtonA | Switch | Fast On/Off Button A |
| fastOnOffButtonB | Switch | Fast On/Off Button B |
| fastOnOffButtonC | Switch | Fast On/Off Button C |
| fastOnOffButtonD | Switch | Fast On/Off Button D |
| heatSetPoint | Number | Heat Set Point |
| humidity | Number | Humidity |
| humidityHigh | Number | Humidity High |
| humidityLow | Number | Humidity Low |
| isCooling | Number | Is Cooling |
| isHeating | Number | Is Heating |
| keypadButtonA | Switch | Keypad Button A |
| keypadButtonB | Switch | Keypad Button B |
| keypadButtonC | Switch | Keypad Button C |
| keypadButtonD | Switch | Keypad Button D |
| keypadButtonE | Switch | Keypad Button E |
| keypadButtonF | Switch | Keypad Button F |
| keypadButtonG | Switch | Keypad Button G |
| keypadButtonH | Switch | Keypad Button H |
| kWh | Number:Energy | Kilowatt Hour |
| lastHeardFrom | DateTime | Last Heard From |
| ledBrightness | Number | LED brightness |
| ledOnOff | Switch | LED On/Off |
| lightDimmer | Dimmer | light Dimmer |
| lightLevel | Number | Light Level |
| lightLevelAboveThreshold | Contact | Light Level Above/Below Threshold |
| loadDimmer | Dimmer | Load Dimmer |
| loadSwitch | Switch | Load Switch |
| loadSwitchFastOnOff | Switch | Load Switch Fast On/Off |
| loadSwitchManualChange | Number | Load Switch Manual Change |
| lowBattery | Contact | Low Battery |
| manualChange | Number | Manual Change |
| manualChangeButtonA | Number | Manual Change Button A |
| manualChangeButtonB | Number | Manual Change Button B |
| manualChangeButtonC | Number | Manual Change Button C |
| manualChangeButtonD | Number | Manual Change Button D |
| notification | Number | Notification |
| onLevel | Number | On Level |
| rampDimmer | Dimmer | Ramp Dimmer |
| rampRate | Number | Ramp Rate |
| reset | Switch | Reset |
| stage1Duration | Number | Stage 1 Duration |
| switch | Switch | Switch |
| systemMode | Number | System Mode |
| tamperSwitch | Contact | Tamper Switch |
| temperature | Number:Temperature | Temperature |
| temperatureLevel | Number | Temperature Level |
| topOutlet | Switch | Top Outlet |
| update | Switch | Update |
| watts | Number:Power | Watts |

## Full Example

Sample things file:

```java
Bridge insteon:network:home [port="/dev/ttyUSB0"] {
  Thing device 22F8A8 [address="22.F8.A8", productKey="F00.00.15"] {
    Channels:
      Type keypadButtonA : keypadButtonA [ group=3 ]
      Type keypadButtonB : keypadButtonB [ group=4 ]
      Type keypadButtonC : keypadButtonC [ group=5 ]
      Type keypadButtonD : keypadButtonD [ group=6 ]
  }
  Thing device 238D93 [address="23.8D.93", productKey="F00.00.12"]
  Thing device 238F55 [address="23.8F.55", productKey="F00.00.11"] {
    Channels:
      Type dimmer        : dimmer [related="23.B0.D9+23.8F.C9"]
  }
  Thing device 238FC9 [address="23.8F.C9", productKey="F00.00.11"] {
    Channels:
      Type dimmer        : dimmer [related="23.8F.55+23.B0.D9"]
  }
  Thing device 23B0D9 [address="23.B0.D9", productKey="F00.00.11"] {
    Channels:
      Type dimmer        : dimmer [related="23.8F.55+23.8F.C9"]
  }
  Thing device 243141 [address="24.31.41", productKey="F00.00.11"]  {
    Channels:
      Type dimmer        : dimmer [dimmermax=60]
  }
}
```

Sample items file:

```java
Switch switch1 { channel="insteon:device:home:243141:switch" }
Dimmer dimmer1 { channel="insteon:device:home:238F55:dimmer" }
Dimmer dimmer2 { channel="insteon:device:home:23B0D9:dimmer" }
Dimmer dimmer3 { channel="insteon:device:home:238FC9:dimmer" }
Dimmer keypad  { channel="insteon:device:home:22F8A8:loadDimmer" }
Switch keypadA { channel="insteon:device:home:22F8A8:keypadButtonA" }
Switch keypadB { channel="insteon:device:home:22F8A8:keypadButtonB" }
Switch keypadC { channel="insteon:device:home:22F8A8:keypadButtonC" }
Switch keypadD { channel="insteon:device:home:22F8A8:keypadButtonD" }
Dimmer dimmer  { channel="insteon:device:home:238D93:dimmer" }
```

## Console Commands

The binding provides commands you can use to help with troubleshooting.
Enter `openhab:insteon` or `insteon` in the console and you will get a list of available commands.
The `openhab:` prefix is optional:

```shell
openhab> openhab:insteon
Usage: openhab:insteon display_devices - display devices that are online, along with available channels
Usage: openhab:insteon display_channels - display channels that are linked, along with configuration information
Usage: openhab:insteon display_local_database - display Insteon PLM or hub database details
Usage: openhab:insteon display_monitored - display monitored device(s)
Usage: openhab:insteon start_monitoring all|address - start displaying messages received from device(s)
Usage: openhab:insteon stop_monitoring all|address - stop displaying messages received from device(s)
Usage: openhab:insteon send_standard_message address flags cmd1 cmd2 - send standard message to a device
Usage: openhab:insteon send_extended_message address flags cmd1 cmd2 [up to 13 bytes] - send extended message to a device
Usage: openhab:insteon send_extended_message_2 address flags cmd1 cmd2 [up to 12 bytes] - send extended message with a two byte crc to a device
```

Here is an example of command: `insteon display_local_database`.

The send message commands do not display any results.
If you want to see the response from the device, you will need to monitor the device.

## Insteon Groups and Scenes

How do Insteon devices tell other devices on the network that their state has changed? They send out a broadcast message, labeled with a specific _group_ number.
All devices (called _responders_) that are configured to listen to this message will then go into a pre-defined state.
For instance when light switch A is switched to "ON", it will send out a message to group #1, and all responders will react to it, e.g they may go into the "ON" position as well.
Since more than one device can participate, the sending out of the broadcast message and the subsequent state change of the responders is referred to as "triggering a scene".
At the device and PLM level, the concept of a "scene" does not exist, so you will find it notably absent in the binding code and this document.
A scene is strictly a higher level concept, introduced to shield the user from the details of how the communication is implemented.

Many Insteon devices send out messages on different group numbers, depending on what happens to them.
A leak sensor may send out a message on group #1 when dry, and on group #2 when wet.
The default group used for e.g. linking two light switches is usually group #1.

## Insteon Binding Process

Before Insteon devices communicate with one another, they must be linked.
During the linking process, one of the devices will be the "Controller", the other the "Responder" (see e.g. the [SwitchLinc Instructions](https://www.insteon.com/pdf/2477S.pdf)).

The responder listens to messages from the controller, and reacts to them.
Note that except for the case of a motion detector (which is just a controller to the modem), the modem controls the device (e.g. send on/off messages to it), and the device controls the modem (so the modem learns about the switch being toggled.
For this reason, most devices and in particular switches/dimmers should be linked twice, with one taking the role of controller during the first linking, and the other acting as controller during the second linking process.
To do so, first press and hold the "Set" button on the modem until the light starts blinking.
Then press and hold the "Set" button on the remote device,
e.g. the light switch, until it double beeps (the light on the modem should go off as well.
Now do exactly the reverse: press and hold the "Set" button on the remote device until its light starts blinking, then press and hold the "Set" button on the modem until it double beeps, and the light of the remote device (switch) goes off.

For some of the more sophisticated devices the complete linking process can no longer be done with the set buttons, but requires software like [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal).

## Insteon Features

Since Insteon devices can have multiple features (for instance a switchable relay and a contact sensor) under a single Insteon address, an openHAB item is not bound to a device, but to a given feature of a device.
For example, the following lines would create two Number items referring to the same thermostat device, but to different features of it:

```java
Number  thermostatCoolPoint "cool point [%.1f °F]" { channel="insteon:device:home:32F422:coolSetPoint" }
Number  thermostatHeatPoint "heat point [%.1f °F]" { channel="insteon:device:home:32F422:heatSetPoint" }
```

### Simple Light Switches

The following example shows how to configure a simple light switch (2477S) in the .items file:

```java
Switch officeLight "office light"  { channel="insteon:device:home:AABBCC:switch" }
```

### Simple Dimmers

Here is how to configure a simple dimmer (2477D) in the .items file:

```java
Dimmer kitchenChandelier "kitchen chandelier" { channel="insteon:device:home:AABBCC:dimmer" }
```

Dimmers can be configured with a maximum level when turning a device on or setting a percentage level.
If a maximum level is configured, openHAB will never set the level of the dimmer above the level specified.
The parameter dimmermax must be defined for the channel.
The below example sets a maximum level of 70% for dim 1 and 60% for dim 2:

#### Things

```java
Bridge insteon:network:home [port="/dev/ttyUSB0"] {
  Thing device AABBCC [address="AA.BB.CC", productKey="F00.00.11"]  {
    Channels:
      Type dimmer     : dimmer [dimmermax=70]
  }
  Thing device AABBCD [address="AA.BB.CD", productKey="F00.00.15"]  {
    Channels:
      Type loadDimmer : loadDimmer [dimmermax=60]
  }
}
```

#### Items

```java
Dimmer d1 "dimmer 1" { channel="insteon:device:home:AABBCC:dimmer"}
Dimmer d2 "dimmer 2" { channel="insteon:device:home:AABBCD:loadDimmer"}
```

Setting a maximum level does not affect manual turning on or dimming a switch.

### On/Off Outlets

Here's how to configure the top and bottom outlet of the in-wall 2 outlet controller:

```java
Switch fOutTop "Front Outlet Top"    <socket> { channel="insteon:device:home:AABBCC:topOutlet" }
Switch fOutBot "Front Outlet Bottom" <socket> { channel="insteon:device:home:AABBCC:bottomOutlet" }
```

This will give you individual control of each outlet.

### Mini Remotes

Link the mini remote to be a controller of the modem by using the set button.
Link all buttons, one after the other.
The 4-button mini remote sends out messages on groups 0x01 - 0x04, each corresponding to one button.
The modem's link database (see [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal)) should look like this:

```text
0000 xx.xx.xx                       xx.xx.xx  RESP  10100010 group: 01 data: 02 2c 41
0000 xx.xx.xx                       xx.xx.xx  RESP  10100010 group: 02 data: 02 2c 41
0000 xx.xx.xx                       xx.xx.xx  RESP  10100010 group: 03 data: 02 2c 41
0000 xx.xx.xx                       xx.xx.xx  RESP  10100010 group: 04 data: 02 2c 41
```

**Items**
This goes into the items file:

```java
Switch miniRemoteButtonA "mini remote button a" { channel="insteon:device:home:AABBCC:buttonA", autoupdate="false" }
Switch miniRemoteButtonB "mini remote button b" { channel="insteon:device:home:AABBCC:buttonB", autoupdate="false" }
Switch miniRemoteButtonC "mini remote button c" { channel="insteon:device:home:AABBCC:buttonC", autoupdate="false" }
Switch miniRemoteButtonD "mini remote button d" { channel="insteon:device:home:AABBCC:buttonD", autoupdate="false" }
```

**Sitemap**
This goes into the sitemap file:

```perl
Switch item=miniRemoteButtonA label="mini remote button a" mappings=[ OFF="Off", ON="On"]
Switch item=miniRemoteButtonB label="mini remote button b" mappings=[ OFF="Off", ON="On"]
Switch item=miniRemoteButtonC label="mini remote button c" mappings=[ OFF="Off", ON="On"]
Switch item=miniRemoteButtonD label="mini remote button d" mappings=[ OFF="Off", ON="On"]
```

The switches in the GUI just display the mini remote's most recent button presses.
They are not operable because the PLM cannot trigger the mini remotes scenes.

### Motion Sensors

Link such that the modem is a responder to the motion sensor.
Create a contact.map file in the transforms directory as described elsewhere in this document.
Then create entries in the .items file like this:

#### Items

```java
Contact motionSensor             "motion sensor [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:contact"}
Number  motionSensorBatteryLevel "motion sensor battery level"         { channel="insteon:device:home:AABBCC:batteryLevel" }
Number  motionSensorLightLevel   "motion sensor light level"           { channel="insteon:device:home:AABBCC:lightLevel" }
```

This will give you a contact, the battery level, and the light level.
The motion sensor II includes three additional channels:

```java
Number  motionSensorBatteryPercent     "motion sensor battery percent"                     { channel="insteon:device:home:AABBCC:batteryPercent" }
Contact motionSensorTamperSwitch       "motion sensor tamper switch [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:tamperSwitch"}
Number  motionSensorTemperatureLevel   "motion sensor temperature level"                   { channel="insteon:device:home:AABBCC:temperatureLevel" }
```

The battery, light level and temperature level are updated when either there is motion, light level above/below threshold, tamper switch activated, or the sensor battery runs low.
This is accomplished by querying the device for the data.
The motion sensor II will also periodically send data if the alternate heartbeat is enabled on the device.

If the alternate heartbeat is enabled, the device can be configured to not query the device and rely on the data from the alternate heartbeat.
Disabling the querying of the device should provide more accurate battery data since it appears to fluctuate with queries of the device.
This can be configured with the device configuration parameter of the device.
The key in the JSON object is `heartbeatOnly` and the value is a boolean:

#### Things

```java
Bridge insteon:network:home [port="/dev/ttyUSB0"] {
  Thing device AABBCC [address="AA.BB.CC", productKey="F00.00.24", deviceConfig="{'heartbeatOnly': true}"]
}

```

The temperature can be calculated in Fahrenheit using the following formulas:

- If the device is battery powered: `temperature = 0.73 * motionSensorTemperatureLevel - 20.53`
- If the device is USB powered: `temperature = 0.72 * motionSensorTemperatureLevel - 24.61`

Since the motion sensor II might not be calibrated correctly, the values `20.53` and `24.61` can be adjusted as necessary to produce the correct temperature.

### Hidden Door Sensors

Similar in operation to the motion sensor above.
Link such that the modem is a responder to the motion sensor.
Create a contact.map file in the transforms directory like the following:

```text
OPEN=open
CLOSED=closed
-=unknown
```

**Items**
Then create entries in the .items file like this:

```java
Contact doorSensor             "Door sensor [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:contact" }
Number  doorSensorBatteryLevel "Door sensor battery level [%.1f]"  { channel="insteon:device:home:AABBCC:batteryLevel" }
```

This will give you a contact and the battery level.
Note that battery level is only updated when either there is motion, or the sensor battery runs low.

### Locks

Read the instructions very carefully: sync with lock within 5 feet to avoid bad connection, link twice for both ON and OFF functionality.

**Items**
Put something like this into your .items file:

```java
Switch doorLock "Front Door [MAP(lock.map):%s]"  { channel="insteon:device:home:AABBCC:switch" }
```

and create a file "lock.map" in the transforms directory with these entries:

```text
ON=Lock
OFF=Unlock
-=unknown
```

### I/O Linc (garage door openers)

The I/O Linc devices are really two devices in one: a relay and a contact.
To control the relay, link the modem as a controller using the set buttons as described in the instructions.
To get the status of the contact, the modem must also be linked as a responder to the I/O Linc.
The I/O Linc has a feature to invert the contact or match the contact when it sends commands to any linked responders.
This is based on the status of the contact when it is linked, and was intended for controlling other devices with the contact.
The binding expects the contact to be inverted to work properly.
Ensure the contact is OFF (status LED is dark/garage door open) when linking the modem as a responder to the I/O Linc in order for it to function properly.

Add this map into your transforms directory as "contact.map":

```text
OPEN=open
CLOSED=closed
-=unknown
```

**Items**
Along with this into your .items file:

```java
Switch  garageDoorOpener  "garage door opener"                        <garagedoor>  { channel="insteon:device:home:AABBCC:switch", autoupdate="false" }
Contact garageDoorContact "garage door contact [MAP(contact.map):%s]"               { channel="insteon:device:home:AABBCC:contact" }
```

**Sitemap**
To make it visible in the GUI, put this into your sitemap file:

```perl
Switch item=garageDoorOpener label="garage door opener" mappings=[ ON="OPEN/CLOSE"]
Text item=garageDoorContact
```

For safety reasons, only close the garage door if you have visual contact to make sure there is no obstruction! The use of automated rules for closing garage doors is dangerous.

> NOTE: If the I/O Linc contact status appears delayed, or returns the wrong value when the sensor changes states, the contact was likely ON (status LED lit) when the modem was linked as a responder.
Examples of this behavior would include: The status remaining CLOSED for up to 3 minutes after the door is opened, or the status remains OPEN for up to three minutes after the garage is opened and immediately closed again.
To resolve this behavior the I/O Linc will need to be unlinked and then re-linked to the modem with the contact OFF (stats LED off). 
That would be with the door open when using the Insteon garage kit.

### Keypads

Before you attempt to configure the keypads, please familiarize yourself with the concept of an Insteon group.

The Insteon keypad devices typically control one main load and have a number of buttons that will send out group broadcast messages to trigger a scene.
If you just want to use the main load switch within openHAB just link modem and device with the set buttons as usual, no complicated linking is necessary.
But if you want to get the buttons to work, read on.

Each button will send out a message for a different, predefined group.
Complicating matters further, the button numbering used internally by the device must be mapped to whatever labels are printed on the physical buttons of the device.
Here is an example correspondence table:

| Group | Button Number | 2487S Label |
|-------|---------------|-------------|
|  0x01 |        1      |   (Load)    |
|  0x03 |        3      |     A       |
|  0x04 |        4      |     B       |
|  0x05 |        5      |     C       |
|  0x06 |        6      |     D       |

When e.g. the "A" button is pressed (that's button #3 internally) a broadcast message will be sent out to all responders configured to listen to Insteon group #3.
This means you must configure the modem as a responder to group #3 (and #4, #5, #6) messages coming from your keypad.
For instructions how to do this, check out the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal).
You can even do that with the set buttons (see instructions that come with the keypad).

While capturing the messages that the buttons emit is pretty straight forward, controlling the buttons is  another matter.
They cannot be simply toggled with a direct command to the device, but instead a broadcast message must be sent on a group number that the button has been programmed to listen to.
This means you need to pick a set of unused groups that is globally unique (if you have multiple keypads, each one of them has to use different groups), one group for each button.
The example configuration below uses groups 0xf3, 0xf4, 0xf5, and 0xf6.
Then link the buttons such that they respond to those groups, and link the modem as a controller for them (see [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) documentation.
In your items file you specify these groups with the "group=" parameters such that the binding knows what group number to put on the outgoing message.

#### Keypad Switches

##### Items

Here is a simple example, just using the load (main) switch:

```java
Switch keypadSwitch             "main load"          { channel="insteon:device:home:AABBCC:loadSwitch" }
Number keypadSwitchManualChange "main manual change" { channel="insteon:device:home:AABBCC:loadSwitchManualChange" }
Switch keypadSwitchFastOnOff    "main fast on/off"   { channel="insteon:device:home:AABBCC:loadSwitchFastOnOff" }
```

Most people will not use the fast on/off features or the manual change feature, so you really only need the first line.
To make the buttons available, add the following:

###### Things

```java
Bridge insteon:network:home [port="/dev/ttyUSB0"] {
  Thing device AABBCC [address="AA.BB.CC", productKey="F00.00.15"] {
    Channels:
      Type keypadButtonA : keypadButtonA [ group="0xf3" ]
      Type keypadButtonB : keypadButtonB [ group="0xf4" ]
      Type keypadButtonC : keypadButtonC [ group="0xf5" ]
      Type keypadButtonD : keypadButtonD [ group="0xf6" ]
  }
}
```

The value after group must either be a number or string.
The hexadecimal value 0xf3 can either converted to a numeric value 243 or the string value "0xf3".

###### Items

```java
Switch keypadSwitchA "keypad button A" { channel="insteon:device:home:AABBCC:keypadButtonA"}
Switch keypadSwitchB "keypad button B" { channel="insteon:device:home:AABBCC:keypadButtonB"}
Switch keypadSwitchC "keypad button C" { channel="insteon:device:home:AABBCC:keypadButtonC"}
Switch keypadSwitchD "keypad button D" { channel="insteon:device:home:AABBCC:keypadButtonD"}
```

##### Sitemap

The following sitemap will bring the items to life in the GUI:

```perl
Frame label="Keypad" {
      Switch item=keypadSwitch label="main"
      Switch item=keypadSwitchFastOnOff label="fast on/off"
      Switch item=keypadSwitchManualChange label="manual change" mappings=[ 0="DOWN", 1="STOP",  2="UP"]
      Switch item=keypadSwitchA label="button A"
      Switch item=keypadSwitchB label="button B"
      Switch item=keypadSwitchC label="button C"
      Switch item=keypadSwitchD label="button D"
}
```

#### Keypad Dimmers

The keypad dimmers are like keypad switches, except that the main load is dimmable.

##### Items

```java
Dimmer keypadDimmer           "dimmer"                          { channel="insteon:device:home:AABBCC:loadDimmer" }
Switch keypadDimmerButtonA    "keypad dimmer button A [%d %%]"  { channel="insteon:device:home:AABBCC:keypadButtonA" }
```

##### Sitemap

```perl
Slider item=keypadDimmer switchSupport
Switch item=keypadDimmerButtonA label="buttonA"
```

### Thermostats

The thermostat (2441TH) is one of the most complex Insteon devices available.
It must first be properly linked to the modem using configuration software like [Insteon Terminal](<https://github.com/pfrommerd/insteon-terminal>.
The Insteon Terminal wiki describes in detail how to link the thermostat, and how to make it publish status update reports.

When all is set and done the modem must be configured as a controller to group 0 (not sure why), and a responder to groups 1-5 such that it picks up when the thermostat switches on/off heating and cooling etc, and it must be a responder to special group 0xEF to get status update reports when measured values (temperature) change.
Symmetrically, the thermostat must be a responder to group 0, and a controller for groups 1-5 and 0xEF.
The linking process is not difficult but needs some persistence.
Again, refer to the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) documentation.

#### Items

This is an example of what to put into your .items file:

```java
Number              thermostatCoolPoint   "cool point [%.1f °F]"       { channel="insteon:device:home:AABBCC:coolSetPoint" }
Number              thermostatHeatPoint   "heat point [%.1f °F]"       { channel="insteon:device:home:AABBCC:heatSetPoint" }
Number              thermostatSystemMode  "system mode [%d]"           { channel="insteon:device:home:AABBCC:systemMode" }
Number              thermostatFanMode     "fan mode [%d]"              { channel="insteon:device:home:AABBCC:fanMode" }
Number              thermostatIsHeating   "is heating [%d]"            { channel="insteon:device:home:AABBCC:isHeating"}
Number              thermostatIsCooling   "is cooling [%d]"            { channel="insteon:device:home:AABBCC:isCooling" }
Number:Temperature  thermostatTemperature  "temperature [%.1f %unit%]" { channel="insteon:device:home:AABBCC:temperature" }
Number              thermostatHumidity    "humidity [%.0f %%]"         { channel="insteon:device:home:AABBCC:humidity" }
```

Add this as well for some more exotic features:

```java
Number              thermostatACDelay      "A/C delay [%d min]"        { channel="insteon:device:home:AABBCC:acDelay" }
Number              thermostatBacklight    "backlight [%d sec]"        { channel="insteon:device:home:AABBCC:backlightDuration" }
Number              thermostatStage1       "A/C stage 1 time [%d min]" { channel="insteon:device:home:AABBCC:stage1Duration" }
Number              thermostatHumidityHigh "humidity high [%d %%]"     { channel="insteon:device:home:AABBCC:humidityHigh" }
Number              thermostatHumidityLow  "humidity low [%d %%]"      { channel="insteon:device:home:AABBCC:humidityLow" }
```

#### Sitemap

For the thermostat to display in the GUI, add this to the sitemap file:

```perl
Text   item=thermostatTemperature icon="temperature"
Text   item=thermostatHumidity
Setpoint item=thermostatCoolPoint icon="temperature" minValue=63 maxValue=90 step=1
Setpoint item=thermostatHeatPoint icon="temperature" minValue=50 maxValue=80 step=1
Switch item=thermostatSystemMode  label="system mode" mappings=[ 0="OFF",  1="HEAT", 2="COOL", 3="AUTO", 4="PROGRAM"]
Switch item=thermostatFanMode  label="fan mode" mappings=[ 0="AUTO",  1="ALWAYS ON"]
Switch item=thermostatIsHeating  label="is heating" mappings=[ 0="OFF",  1="HEATING"]
Switch item=thermostatIsCooling  label="is cooling" mappings=[ 0="OFF",  1="COOLING"]
Setpoint item=thermostatACDelay  minValue=2 maxValue=20 step=1
Setpoint item=thermostatBacklight  minValue=0 maxValue=100 step=1
Setpoint item=thermostatHumidityHigh  minValue=0 maxValue=100 step=1
Setpoint item=thermostatHumidityLow   minValue=0 maxValue=100 step=1
Setpoint item=thermostatStage1  minValue=1 maxValue=60 step=1
```

### Power Meters

The iMeter Solo reports both wattage and kilowatt hours, and is updated during the normal polling process of the devices.
You can also manually update the current values from the device and reset the device.
See the example below:

#### Items

```java
Number:Power  iMeterWatts   "iMeter [%d watts]"   { channel="insteon:device:home:AABBCC:watts" }
Number:Energy iMeterKwh     "iMeter [%.04f kWh]"  { channel="insteon:device:home:AABBCC:kWh" }
Switch        iMeterUpdate  "iMeter Update"       { channel="insteon:device:home:AABBCC:update" }
Switch        iMeterReset   "iMeter Reset"        { channel="insteon:device:home:AABBCC:reset" }
```

### Fan Controllers

Here is an example configuration for a FanLinc module, which has a dimmable light and a variable speed fan:

#### Items

```java
Dimmer fanLincDimmer "fanlinc dimmer [%d %%]" { channel="insteon:device:home:AABBCC:lightDimmer" }
Number fanLincFan    "fanlinc fan"            { channel="insteon:device:home:AABBCC:fan"}
```

#### Sitemap

```perl
Slider item=fanLincDimmer switchSupport
Switch item=fanLincFan label="fan speed" mappings=[ 0="OFF",  1="LOW", 2="MEDIUM", 3="HIGH"]
```

### X10 Devices

It is worth noting that both the Inseon PLM and the 2014 Hub can both command X10 devices over the powerline, and also set switch stats based on X10 signals received over the powerline.
This allows openHAB not only control X10 devices without the need for other hardwaare, but it can also have rules that react to incoming X10 powerline commands.
While you cannot bind the the X10 devices to the Insteon PLM/HUB, here are some examples for configuring X10 devices.
Be aware that most X10 switches/dimmers send no status updates, i.e. openHAB will not learn about switches that are toggled manually.
Further note that X10 devices are addressed with `houseCode.unitCode`, e.g. `A.2`.

#### Items

```java
Switch x10Switch  "X10 switch" { channel="insteon:device:home:AABB:switch" }
Dimmer x10Dimmer  "X10 dimmer" { channel="insteon:device:home:AABB:dimmer" }
Contact x10Motion "X10 motion" { channel="insteon:device:home:AABB:contact" }
```

## Direct Sending of Group Broadcasts (Triggering Scenes)

The binding can command the modem to send broadcasts to a given Insteon group.
Since it is a broadcast message, the corresponding item does _not_ take the address of any device, but of the modem itself.
The format is `broadcastOnOff#X` where X is the group that you want to be able to broadcast messages to:

### Things

```java
Bridge insteon:network:home [port="/dev/ttyUSB0"] {
  Thing device AABBCC             [address="AA.BB.CC", productKey="0x000045"] {
    Channels:
      Type broadcastOnOff : broadcastOnOff#2
  }
}

```

### Items

```java
Switch  broadcastOnOff "group on/off"  { channel="insteon:device:home:AABBCC:broadcastOnOff#2" }
```

Flipping this switch to "ON" will cause the modem to send a broadcast message with group=2, and all devices that are configured to respond to it should react.

Channels can also be configured using the device configuration parameter of the device.
The key in the JSON object is `broadcastGroups` and the value is an array of integers:

### Things (device Config)

```java
Bridge insteon:network:home [port="/dev/ttyUSB0"] {
  Thing device AABBCC             [address="AA.BB.CC", productKey="0x000045", deviceConfig="{'broadcastGroups': [2]}"]
}

```

## Channel "related" Property

When an Insteon device changes its state because it is directly operated (for example by flipping a switch manually), it sends out a broadcast message to announce the state change, and the binding (if the PLM modem is properly linked as a responder) should update the corresponding openHAB items.
Other linked devices however may also change their state in response, but those devices will _not_ send out a broadcast message, and so openHAB will not learn about their state change until the next poll.
One common scenario is e.g. a switch in a 3-way configuration, with one switch controlling the load, and the other switch being linked as a controller.
In this scenario, the "related" keyword can be used to have the binding poll a related device whenever a state change occurs for another device.
A typical example would be two dimmers (A and B) in a 3-way configuration:

```java
Bridge insteon:network:home [port="/dev/ttyUSB0"] {
  Thing device AABBCC [address="AA.BB.CC", productKey="F00.00.11"] {
    Channels:
      Type dimmer : dimmer [related="AA.BB.DD"]
  }
  Thing device AABBDD [address="AA.BB.DD", productKey="F00.00.11"] {
    Channels:
      Type dimmer : dimmer [related="AA.BB.CC"]
  }
}
```

Another scenario is a group broadcast message, the binding doesn't know which devices have responded to the message since its a broadcast message.
In this scenario, the "related" keyword can be used to have the binding poll one or more related device when group message are sent.
A typical example would be a switch configured to broadcast to a group, and one or more devices configured to respond to the message:

```java
Bridge insteon:network:home [port="/dev/ttyUSB0"] {
  Thing device AABBCC [address="AA.BB.CC", productKey="0x000045"] {
    Channels:
      Type broadcastOnOff : broadcastOnOff#3 [related="AA.BB.DD"]
  }
  Thing device AABBDD [address="AA.BB.DD", productKey="F00.00.11"]
}
```

More than one device can be polled by separating them with "+" sign, e.g. "related=aa.bb.cc+xx.yy.zz" would poll both of these devices.
The implemenation of the _related_ keyword is simple: if you add it to a channel, and that channel changes its state, then the _related_ device will be polled to see if its state has updated.

## Troubleshooting

Turn on DEBUG or TRACE logging for `org.openhab.binding.insteon.
See [logging in openHAB](https://www.openhab.org/docs/administration/logging.html) for more info.

### Device Permissions / Linux Device Locks

When openHAB is running as a non-root user (Linux/OSX) it is important to ensure it has write access not just to the PLM device, but to the os lock directory.
Under openSUSE this is `/run/lock` and is managed by the **lock** group.

Example commands to grant openHAB access (adjust for your distribution):

```shell
usermod -a -G dialout openhab
usermod -a -G lock openhab
```

Insufficient access to the lock directory will result in openHAB failing to access the device, even if the device itself is writable.

### Adding New Device Types (Using Existing Device Features)

Device types are defined in the file `device_types.xml`, which is inside the Insteon bundle and thus not visible to the user.
You can however load your own device_types.xml by referencing it in the network config parameters:

```text
additionalDevices="/usr/local/openhab/rt/my_own_devices.xml"
```

Where the `my_own_devices.xml` file defines a new device like this:

```xml
    <xml>
     <device productKey="F00.00.XX">
      <model>2456-D3</model>
      <description>LampLinc V2</description>
      <feature name="dimmer">GenericDimmer</feature>
      <feature name="lastheardfrom">GenericLastTime</feature>
     </device>
    </xml>
```

Finding the Insteon product key can be tricky since Insteon has not updated the product key table (<https://www.insteon.com/pdf/insteon_devcats_and_product_keys_20081008.pdf>) since 2008.
If a web search does not turn up the product key, make one up, starting with "F", like: F00.00.99.
Avoid duplicate keys by finding the highest fake product key in the `device_types.xml` file, and incrementing by one.

### Adding New Device Features

If you can't build a new device out of the existing device features (for a complete list see `device_features.xml`) you can add new features by specifying a file (let's call it `my_own_features.xml`) with the "additionalDevices" option in the network config parameters:

```text
additionalFeatures="/usr/local/openhab/rt/my_own_features.xml"
```

In this file you can define your own features (or even overwrite an existing feature.
In the example below a new feature "MyFeature" is defined, which can then be referenced from the `device_types.xml` file (or from `my_own_devices.xml`):

```xml
    <xml>
     <feature name="MyFeature">
     <message-dispatcher>DefaultDispatcher</message-dispatcher>
     <message-handler cmd="0x03">NoOpMsgHandler</message-handler>
     <message-handler cmd="0x06">NoOpMsgHandler</message-handler>
     <message-handler cmd="0x11">NoOpMsgHandler</message-handler>
     <message-handler cmd="0x13">NoOpMsgHandler</message-handler>
     <message-handler cmd="0x19">LightStateSwitchHandler</message-handler>
     <command-handler command="OnOffType">IOLincOnOffCommandHandler</command-handler>
     <poll-handler>DefaultPollHandler</poll-handler>
     </feature>
    </xml>
```

## Known Limitations and Issues

- Devices cannot be linked to the modem while the binding is running.
If new devices are linked, the binding must be restarted.
- Setting up Insteon groups and linking devices cannot be done from within openHAB.
Use the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) for that.
If using Insteon Terminal (especially as root), ensure any stale lock files (For example, /var/lock/LCK..ttyUSB0) are removed before starting openHAB runtime.
Failure to do so may result in "found no ports".
- The Insteon PLM or hub is know to break in about 2-3 years due to poorly sized capacitors.
You can repair it yourself using basic soldering skills, search for "Insteon PLM repair" or "Insteon hub repair".
- Using the Insteon Hub 2014 in conjunction with other applications (such as the InsteonApp) is not supported. Concretely, openHAB will not learn when a switch is flipped via the Insteon App until the next poll, which could take minutes.
