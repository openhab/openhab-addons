# Insteon Binding

Insteon is a proprietary home automation system that enables light switches, lights, thermostats, leak sensors, remote controls, motion sensors, and other electrically powered devices to interoperate through power lines, radio frequency (RF) communications, or both (dual-band)
More about Insteon can be found on [Wikipedia](https://en.wikipedia.org/wiki/Insteon).

This binding is mostly a rewrite of the [legacy binding](https://www.openhab.org/addons/bindings/insteon/) adding some much needed improvements such as the automated device configuration layer.
It provides access to the Insteon network by means of either an Insteon PowerLinc Modem (PLM), a legacy Insteon Hub 2242-222 or the current 2245-222 Insteon Hub.
The modem can be connected to the openHAB server either via a serial port (Model 2413S) or a USB port (Model 2413U).
The Insteon PowerLinc Controller (Model 2414U) is not supported since it is a PLC not a PLM.
The modem can also be connected via TCP (such as ser2net).
The binding translates openHAB commands into Insteon messages and sends them on the Insteon network.
Relevant messages from the Insteon network (like notifications about switches being toggled) are picked up by the modem and converted to openHAB state updates by the binding.
The binding also supports sending and receiving of legacy X10 messages.

The openHAB binding supports configuring most of the device local settings, linking a device to the modem, managing link database records and scenes along with monitoring inbound/outbound messages.
Other tools can be used to managed Insteon devices, such as the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) open source project, or the [HouseLinc](https://www.insteon.com/houselinc) software from Insteon can also be used for configuration, but it wipes the modem link database clean on its initial use, requiring to re-link the modem to all devices.

At startup, the binding will download the modem database along with each configured device all-link database if not previously downloaded and currently awake.
Therefore, the initialization on the first start may take some additional time to complete depending on the number of devices configured.
The modem and device link databases are only downloaded once unless the binding receives an indication that a database was updated or marked to be refreshed via the [openHAB console](#console-commands).

If switching from the legacy binding, there is unfortunately no direct path to convert existing configuration due to the new way bridges, devices and scenes are configured and discovered.
It is recommended to disable the legacy network bridge to prevent both bindings from connecting to the same modem at the same time while keeping the ability to port some of the configuration over.
Once migrated, the legacy binding should be removed along with relevant bridge and things.

## Supported Things

| Thing | Type | Description |
|-------|------|-------------|
| hub1  | Bridge | An Insteon Hub Legacy that communicates with Insteon devices. |
| hub2  | Bridge | An Insteon Hub that communicates with Insteon devices. |
| plm  | Bridge | An Insteon PLM that communicates with Insteon devices. |
| device | Thing | An Insteon device such as a switch, dimmer, keypad, sensor, etc. |
| scene | Thing | An Insteon scene that controls multiple devices simultaneously. |
| x10 | Thing | An X10 device such as a switch, dimmer or sensor. |

## Discovery

An Insteon bridge is not automatically discovered and will have to be manually added.
Once configured, depending on the bridge discovery parameters, any Insteon devices or scenes that exists in the modem database and is not currently configured will be automatically be added to the inbox.
The naming convention for devices is **_Vendor_ _Model_ _Description_** if its product data is retrievable, otherwise **Insteon Device AABBCC**, where `AA`, `BB` and `CC` are from the Insteon device address.
For scenes, it is **Insteon Scene 42**, where `42` is the scene group number.
The device auto-discovery is enabled by default while disabled for scenes.
X10 devices are not auto discovered.

## Thing Configuration

### Insteon Hub Legacy Configuration

The Insteon Hub Legacy is configured with the following parameters:

| Parameter | Default | Required | Description |
|-----------|:-------:|:--------:|-------------|
| hostname | | Yes | Network address of the hub. |
| port | 9761 | No | Network port of the hub. |
| devicePollIntervalInSeconds | 300 | No | Device poll interval in seconds. The hub will be overloaded if interval is too short, leading to sluggish or no response when trying to send messages to devices. The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers. |
| deviceDiscoveryEnabled | true | No | Discover Insteon devices found in the hub database but not configured. |
| sceneDiscoveryEnabled | false | No | Discover Insteon scenes found in the hub database but not configured. |
| deviceSyncEnabled | false | No | Synchronize related devices based on their all-link database. |

### Insteon Hub Configuration

The Insteon Hub is configured with the following parameters:

| Parameter | Default | Required | Description |
|-----------|:-------:|:--------:|-------------|
| hostname | | Yes | Network address of the hub. |
| port | 25105 | No | Network port of the hub. |
| username | | Yes | Username to access the hub. |
| password | | Yes | Password to access the hub. |
| hubPollIntervalInMilliseconds | 1000 | No | Hub poll interval in milliseconds. |
| devicePollIntervalInSeconds | 300 | No | Device poll interval in seconds. The hub will be overloaded if interval is too short, leading to sluggish or no response when trying to send messages to devices. The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers. |
| deviceDiscoveryEnabled | true | No | Discover Insteon devices found in the hub database but not configured. |
| sceneDiscoveryEnabled | false | No | Discover Insteon scenes found in the hub database but not configured. |
| deviceSyncEnabled | false | No | Synchronize related devices based on their all-link database. |

### Insteon PLM Configuration

The Insteon PLM is configured with the following parameters:

| Parameter | Default | Required | Description |
|-----------|:-------:|:--------:|-------------|
| serialPort | | Yes | Serial port connected to the modem. Example: `/dev/ttyS0` or `COM1` |
| baudRate | 19200 | No | Serial port baud rate connected to the modem. |
| devicePollIntervalInSeconds | 300 | No | Device poll interval in seconds. The modem will be overloaded if interval is too short, leading to sluggish or no response when trying to send messages to devices. The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers. |
| deviceDiscoveryEnabled | true | No | Discover Insteon devices found in the modem database but not configured. |
| sceneDiscoveryEnabled | false | No | Discover Insteon scenes found in the modem database but not configured. |
| deviceSyncEnabled | false | No | Synchronize related devices based on their all-link database. |

### Insteon Device Configuration

The Insteon device is configured with the following parameter:

| Parameter | Required | Description |
|-----------|:--------:|-------------|
| address | Yes | Insteon address of the device. It can be found on the device. Example: `12.34.56`. |

The device type is automatically determined by the binding using the device product data.
For a [battery powered device](#battery-powered-devices) that was never configured previously, it may take until the next time that device sends a broadcast message to be modeled properly.
To speed up the process for this case, it is recommended to force the device to become awake after the associated bridge is online.
Likewise, for a device that wasn't accessible during the binding initialization phase, press on its SET button once powered on to notify the binding that it is available.

### Insteon Scene Configuration

The Insteon scene is configured with the following parameter:

| Parameter | Required | Description |
|-----------|:--------:|-------------|
| group | Yes | Insteon scene group number between 2 and 254. It can be found in the scene detailed information in the Insteon mobile app. |

### X10 Device Configuration

The X10 device is configured with the following parameters:

| Parameter | Required | Description |
|-----------|:--------:|-------------|
| houseCode | Yes | X10 house code of the device. Example: `A`|
| unitCode | Yes | X10 unit code of the device. Example: `1` |
| deviceType | Yes | X10 device type |

The supported X10 device types:

| Device Type | Description |
|-------------|-------------|
| X10_Switch | X10 Switch |
| X10_Dimmer | X10 Dimmer |
| X10_Sensor | X10 Sensor |

## Channels

Below is the list of possible channels for the Insteon devices.
In order to determine which channels a device supports, check the device in the UI, or use the `insteon2 device listAll` console command.

### State Channels

| Channel | Type | Access Mode | Description |
|---------|------|-------------|-------------|
| 3WayMode | Switch | R/W | 3-Way Toggle Mode |
| acDelay | Number:Time | R/W | AC Delay |
| alarmDelay | Switch | R/W | Alarm Delay |
| alarmDuration | Number:Time | R/W | Alarm Duration |
| alarmType | String | R/W | Alarm Type |
| armed | Switch | R/W | Armed State |
| backlightDuration | Number:Time | R/W | Back Light Duration |
| batteryLevel | Number:Dimensionless | R | Battery Level |
| batteryPowered | Switch | R | Battery Powered State |
| beep | Switch | W | Beep |
| buttonA | Switch | R/W | Button A |
| buttonB | Switch | R/W | Button B |
| buttonC | Switch | R/W | Button C |
| buttonD | Switch | R/W | Button D |
| buttonE | Switch | R/W | Button E |
| buttonF | Switch | R/W | Button F |
| buttonG | Switch | R/W | Button G |
| buttonH | Switch | R/W | Button H |
| buttonBeep | Switch | R/W | Beep on Button Press |
| buttonConfig | String | R/W | Button Config |
| buttonLock | Switch | R/W | Button Lock |
| carbonMonoxideAlarm | Switch | R | Carbon Monoxide Alarm |
| contact | Contact | R | Contact State |
| coolSetPoint | Number:Temperature | R/W | Cool Set Point |
| daylight | Contact | R | Daylight State |
| dimmer | Dimmer | R/W | Dimmer |
| energyOffset | Number:Temperature | R/W | Energy Set Point Offset |
| energySaving | Switch | R | Energy Saving |
| energyUsage | Number:Energy | R | Energy Usage in Kilowatt Hour |
| error | Switch | R | Error |
| fanMode | String | R/W | Fan Mode |
| fanSpeed | String | R/W | Fan Speed |
| fanState | Switch | R | Fan State |
| heartbeatInterval | Number:Time | R/W | Heartbeat Interval |
| heartbeatOnOff | Switch | R/W | Heartbeat On/Off |
| heatSetPoint | Number:Temperature | R/W | Heat Set Point |
| humidity | Number:Dimensionless | R | Current Humidity |
| humidityControl | String | R | Humidity Control State |
| humidityHigh | Number:Dimensionless | R/W | Humidity High |
| humidityLow | Number:Dimensionless | R/W | Humidity Low |
| lastHeardFrom | DateTime | R | Last Heard From |
| leak | Switch | R | Leak Detected |
| ledBrightness | Dimmer | R/W | LED Brightness |
| ledOnOff | Switch | R/W | LED On/Off |
| ledTraffic | Switch | R/W | LED Blink on Traffic |
| lightLevel | Number:Dimensionless | R | Light Level |
| load | Switch | R | Load State |
| loadSense | Switch | R/W | Load Sense |
| loadSenseBottom | Switch | R/W | Load Sense Bottom |
| loadSenseTop | Switch | R/W | Load Sense Top |
| lock | Switch | R/W | Lock |
| lowBattery | Switch | R | Low Battery Alert |
| momentaryDuration | Number:Time | R/W | Momentary Duration |
| monitorMode | Switch | R/W | Monitor Mode |
| motion | Switch | R | Motion Detected |
| onLevel | Dimmer | R/W | On Level |
| operationMode | String | R/W | Switch Operation Mode |
| outletBottom | Switch | R/W | Outlet Bottom |
| outletTop | Switch | R/W | Outlet Top |
| powerUsage | Number:Power | R | Power Usage in Watts |
| program1 | Player | R/W | Program 1 |
| program2 | Player | R/W | Program 2 |
| program3 | Player | R/W | Program 3 |
| program4 | Player | R/W | Program 4 |
| programLock | Switch | R/W | Local Programming Lock |
| pump | Switch | R/W | Pump |
| rampRate | Number:Time | R/W | Ramp Rate |
| relayMode | String | R/W | Output Relay Mode |
| relaySensorFollow | Switch | R/W | Output Relay Follows Input Sensor |
| reset | Switch | W | Reset |
| resumeDim | Switch | R/W | Resume Dim |
| reverseDirection | Switch | R/W | Reverse Direction |
| rollershutter | Rollershutter | R/W | Rollershutter |
| sceneOnOff | Switch | R/W | Scene On/Off |
| sceneFastOnOff | Switch | W | Scene Fast On/Off |
| sceneManualChange | Rollershutter | W | Scene Manual Change |
| siren | Switch | R/W | Siren |
| smokeAlarm | Switch | R | Smoke Alarm |
| stage1Duration | Number:Time | R/W | Stage 1 Duration |
| stayAwake | Switch | R/W | Stay Awake for Extended Time |
| switch | Switch | R/W | Switch |
| syncTime | Switch | W | Sync Time |
| systemMode | String | R/W | System Mode |
| systemState | String | R | System State |
| tamperSwitch | Contact | R | Tamper Switch |
| temperature | Number:Temperature | R | Current Temperature |
| temperatureFormat | String | R/W | Temperature Format |
| testAlarm | Switch | R | Test Alarm |
| timeFormat | String | R/W | Time Format |
| toggleModeButtonA | String | R/W | Toggle Mode Button A |
| toggleModeButtonB | String | R/W | Toggle Mode Button B |
| toggleModeButtonC | String | R/W | Toggle Mode Button C |
| toggleModeButtonD | String | R/W | Toggle Mode Button D |
| toggleModeButtonE | String | R/W | Toggle Mode Button E |
| toggleModeButtonF | String | R/W | Toggle Mode Button F |
| toggleModeButtonG | String | R/W | Toggle Mode Button G |
| toggleModeButtonH | String | R/W | Toggle Mode Button H |
| valve1 | Switch | R/W | Valve 1 |
| valve2 | Switch | R/W | Valve 2 |
| valve3 | Switch | R/W | Valve 3 |
| valve4 | Switch | R/W | Valve 4 |
| valve5 | Switch | R/W | Valve 5 |
| valve6 | Switch | R/W | Valve 6 |
| valve7 | Switch | R/W | Valve 7 |
| valve8 | Switch | R/W | Valve 8 |

### Trigger Channels

| Channel | Description |
|---------|-------------|
| eventButton | Event Button |
| eventButtonA | Event Button A |
| eventButtonB | Event Button B |
| eventButtonC | Event Button C |
| eventButtonD | Event Button D |
| eventButtonE | Event Button E |
| eventButtonF | Event Button F |
| eventButtonG | Event Button G |
| eventButtonH | Event Button H |
| eventButtonMain | Event Button Main |
| eventButtonBottom | Event Button Bottom |
| eventButtonTop | Event Button Top |

The supported triggered events for Insteon Device things:

| Event | Description |
|-------|-------------|
| `PRESSED_ON` | Button Pressed On (Regular On) |
| `PRESSED_OFF` | Button Pressed Off (Regular Off) |
| `DOUBLE_PRESSED_ON` | Button Double Pressed On (Fast On) |
| `DOUBLE_PRESSED_OFF` | Button Double Pressed Off (Fast Off) |
| `HELD_UP` | Button Held Up (Manual Change Up) |
| `HELD_DOWN` | Button Held Down (Manual Change Down) |
| `RELEASED` | Button Released (Manual Change Stop) |

And for Insteon Hub and PLM things:

| Event | Description |
|-------|-------------|
| `PRESSED` | Button Pressed |
| `HELD` | Button Held |
| `RELEASED` | Button Released |

## Full Example

Sample things file:

```java
Bridge insteon2:plm:home [serialPort="/dev/ttyUSB0"] {
  Thing device 22F8A8 [address="22.F8.A8"]
  Thing device 238D93 [address="23.8D.93"]
  Thing device 238F55 [address="23.8F.55"]
  Thing device 238FC9 [address="23.8F.C9"]
  Thing device 23B0D9 [address="23.B0.D9"]
  Thing scene scene42 [group=42]
  Thing x10 A2 [houseCode="A", unitCode=2, deviceType="X10_Switch"]
}
```

Sample items file:

```java
Switch switch1 { channel="insteon2:device:home:243141:switch" }
Dimmer dimmer1 { channel="insteon2:device:home:238F55:dimmer" }
Dimmer dimmer2 { channel="insteon2:device:home:23B0D9:dimmer" }
Dimmer dimmer3 { channel="insteon2:device:home:238FC9:dimmer" }
Dimmer keypad  { channel="insteon2:device:home:22F8A8:dimmer" }
Switch keypadA { channel="insteon2:device:home:22F8A8:buttonA" }
Switch keypadB { channel="insteon2:device:home:22F8A8:buttonB" }
Switch keypadC { channel="insteon2:device:home:22F8A8:buttonC" }
Switch keypadD { channel="insteon2:device:home:22F8A8:buttonD" }
Switch scene42 { channel="insteon2:scene:home:scene42:sceneOnOff" }
Switch switch2 { channel="insteon2:x10:home:A2:switch" }
```

## Console Commands

The binding provides commands to help with configuring and troubleshooting.
Most commands support auto-completion during input based on the existing configuration.
Enter `openhab:insteon2` or `insteon2` in the console to get a list of available commands.

```shell
openhab> insteon2
Usage: openhab:insteon2 modem - Insteon modem commands
Usage: openhab:insteon2 device - Insteon/X10 device commands
Usage: openhab:insteon2 scene - Insteon scene commands
Usage: openhab:insteon2 channel - Insteon channel commands
Usage: openhab:insteon2 debug - Insteon debug commands
```

## Insteon Groups and Scenes

How do Insteon devices tell other devices on the network that their state has changed? They send out a broadcast message, labeled with a specific _group_ number.
All devices (called _responders_) that are configured to listen to this message will then go into a pre-defined state.
For instance when light switch A is switched to "ON", it will send out a message to group #1, and all responders will react to it, e.g they may go into the "ON" position as well.
Since more than one device can participate, the sending out of the broadcast message and the subsequent state change of the responders is referred to as "triggering a scene".

Many Insteon devices send out messages on different group numbers, depending on what happens to them.
A leak sensor may send out a message on group #1 when dry, and on group #2 when wet.
The default group used for e.g. linking two light switches is usually group #1.

The binding can now automatically determines the broadcast groups between the modem and linked devices, based on their all-link databases.

By default, the binding only sends direct messages to the intended device to update its state, leaving the state of the related devices unchanged.
Whenever the bridge parameter `deviceSyncEnabled` is set to `true`, broadcast messages for supported Insteon commands (e.g. on/off, bright/dim, manual change) are sent to all responders of a given group, updating all related devices in one request.
If no broadcast group is determined or for Insteon commands that don't support broadcasting (e.g. percent), direct messages are sent to each related device instead, to adjust their level based on their all-link database.

## Insteon Binding Process

Before Insteon devices communicate with one another, they must be linked.
During the linking process, one of the devices will be the "Controller", the other the "Responder".

The responder listens to messages from the controller, and reacts to them.
Note that except for the case of a motion detector (which is just a controller to the modem), the modem controls the device (e.g. send on/off messages to it), and the device controls the modem, so it learns about the switch being toggled.
For this reason, most devices and in particular switches/dimmers should be linked twice, with one taking the role of controller during the first linking, and the other acting as controller during the second linking process.
To do so, first press and hold the "Set" button on the modem until the light starts blinking.
Then press and hold the "Set" button on the remote device, e.g. the light switch, until it double beeps (the light on the modem should go off as well).
Now do exactly the reverse: press and hold the "Set" button on the remote device until its light starts blinking, then press and hold the "Set" button on the modem until it double beeps, and the light of the remote device (switch) goes off.

Alternatively, the binding can link a device to the modem programmatically using the `insteon2 modem addDevice` console command.
Based on the initial set button pressed event received, the device will be linked one or both ways.
Once the newly linked device is added as a thing, additional links for more complex devices can be added using the `insteon2 device addMissingLinks` console command.

## Insteon Devices

Since Insteon devices can have multiple features (for instance a switchable relay and a contact sensor) under a single Insteon device, an openHAB item is not bound to a device, but to a given feature of a device.
For example, the following lines would create two Number items referring to the same thermostat device, but to different features of it:

```java
Number:Temperature  thermostatCoolPoint "cool point [%.1f °F]" { channel="insteon2:device:home:32F422:coolSetPoint" }
Number:Temperature  thermostatHeatPoint "heat point [%.1f °F]" { channel="insteon2:device:home:32F422:heatSetPoint" }
```

### Switches

The following example shows how to configure a simple light switch (2477S) in the .items file:

```java
Switch officeLight "office light" { channel="insteon2:device:home:AABBCC:switch" }
```

### Dimmers

Here is how to configure a simple dimmer (2477D) in the .items file:

```java
Dimmer kitchenChandelier "kitchen chandelier" { channel="insteon2:device:home:AABBCC:dimmer" }
```

For `ON` command requests, the binding uses the device on level and ramp rate local settings to set the dimmer level, the same way it would be set when physically pressing on the dimmer.
These settings can be controlled using the `onLevel` and `rampRate` channels.

Alternatively, these settings can be overridden using the `onLevel` and `rampRate` channel parameters.
Doing so will result in different type of commands being triggered as opposed to having separate channels previously such as `fastOnOff`, `manualChange` and `rampDimmer` handling it.

When the `rampRate` parameter is configured, the binding will send a ramp rate command (previously generated by the `rampDimmer` channel) to the relevant device to set the level at the defined ramp rate.
When this parameter is set to instant (0.1 sec), on/off commands will trigger what used to be handled by the `fastOnOff` channel.
And percent commands will trigger what is defined in the Insteon protocol as instant change requests.

As far as the previously known `manualChange` channel, it has been rolled into the `rollershutter` channel for [window covering](#window-coverings) using `UP`, `DOWN` and `STOP` commands.
For the `dimmer` channel, the `INCREASE` and `DECREASE` commands can be used instead.

Ultimately, the `dimmer` channel parameters can be used to create custom channels via a thing file that can work as an alternative to having to configure an Insteon scene for a single device.

```java
Thing device 23B0D9 [address="23.B0.D9"] {
  Channels:
    Type dimmer : custom1 [onLevel=50, rampRate=150] // 50% on level at 2.5 minutes ramp rate
    Type dimmer : custom2 [onLevel=80]               // 80% on level at device configured ramp rate
    Type dimmer : custom3 [rampRate=480]             // device configured on level at 8 minutes ramp rate
}
```

### Keypads

The Insteon keypad devices typically control one main load and have a number of buttons that will send out group broadcast messages to trigger a scene.
To use the main load switch within openHAB, link the modem and device with the set buttons as usual.
For the scene buttons, each one will send out a message for a different, predefined group.
The button numbering used internally by the device must be mapped to whatever labels are printed on the physical buttons of the device.
Here is an example correspondence table:

| Group | Button Number | 2487S Label |
|-------|---------------|-------------|
|  0x01 |        1      |   (Load)    |
|  0x03 |        3      |     A       |
|  0x04 |        4      |     B       |
|  0x05 |        5      |     C       |
|  0x06 |        6      |     D       |

When e.g. the "A" button is pressed (that's button #3 internally) a broadcast message will be sent out to all responders configured to listen to Insteon group #3.
In this case, the modem must be configured as a responder to group #3 (and #4, #5, #6) messages coming from the keypad.
These groups can be linked programmatically using the `insteon2 device addMissingLinks` console command, or via the device set buttons (see the keypad instructions).

While previously, keypad buttons required a broadcast group to be configured, the binding now automatically determines that setting, based on the device link databases, deprecating the `group` channel parameter.
By default, the binding will only change the button led state when receiving on/off commands, depending on the keypad local radio group settings.
For button broadcast group support, set the bridge parameter `deviceSyncEnabled` to `true`.
Additionally, for button toggle mode set to always on or off, only `ON` or `OFF` commands will be processed, in line with the physical interaction.

#### Keypad Switches

##### Items

The following items will expose a keypad switch and its associated buttons:

```java
Switch keypadSwitch             "main switch"        { channel="insteon2:device:home:AABBCC:switch" }
Switch keypadSwitchA            "button A"           { channel="insteon2:device:home:AABBCC:buttonA"}
Switch keypadSwitchB            "button B"           { channel="insteon2:device:home:AABBCC:buttonB"}
Switch keypadSwitchC            "button C"           { channel="insteon2:device:home:AABBCC:buttonC"}
Switch keypadSwitchD            "button D"           { channel="insteon2:device:home:AABBCC:buttonD"}
```

##### Sitemap

The following sitemap will bring the items to life in the GUI:

```perl
Frame label="Keypad" {
      Switch item=keypadSwitch label="main"
      Switch item=keypadSwitchA label="button A"
      Switch item=keypadSwitchB label="button B"
      Switch item=keypadSwitchC label="button C"
      Switch item=keypadSwitchD label="button D"
}
```

##### Rules

The following rules will monitor regular on/off, fast on/off and manual change button events:

```php
rule "Main Button Off Event"
when
  Channel 'insteon2:device:home:AABBCC:eventButtonMain' triggered PRESSED_OFF
then
  // do something
end

rule "Main Button Fast On/Off Events"
when
  Channel 'insteon2:device:home:AABBCC:eventButtonMain' triggered DOUBLE_PRESSED_ON or
  Channel 'insteon2:device:home:AABBCC:eventButtonMain' triggered DOUBLE_PRESSED_OFF
then
  // do something
end

rule "Main Button Manual Change Stop Event"
when
  Channel 'insteon2:device:home:AABBCC:eventButtonMain' triggered RELEASED
then
  // do something
end

rule "Keypad Button A On Event"
when
  Channel 'insteon2:device:home:AABBCC:eventButtonA' triggered PRESSED_ON
then
  // do something
end
```

#### Keypad Dimmers

The keypad dimmers are like keypad switches, except that the main load is dimmable.

##### Items

```java
Dimmer keypadDimmer           "main dimmer" { channel="insteon2:device:home:AABBCC:dimmer" }
Switch keypadDimmerButtonA    "button A"    { channel="insteon2:device:home:AABBCC:buttonA" }
```

##### Sitemap

```perl
Slider item=keypadDimmer label="main" switchSupport
Switch item=keypadDimmerButtonA label="button A"
```

### Outlets

Here's how to configure the top and bottom outlet of the in-wall 2 outlet controller:

```java
Switch outletTop    "Outlet Top"    { channel="insteon2:device:home:AABBCC:topOutlet" }
Switch outletBottom "Outlet Bottom" { channel="insteon2:device:home:AABBCC:bottomOutlet" }
```

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

The mini remote buttons cannot be modeled as items since they don't have a state or can receive commands. However, button triggered events can be monitored through rules that can set off subsequent actions:

##### Rules

```php
rule "Mini Remote Button A Pressed On"
when
  Channel 'insteon2:device:home:miniRemote:eventButtonA' triggered PRESSED_ON
then
  // do something
end
```

### Motion Sensors

Link such that the modem is a responder to the motion sensor.

##### Items

```java
Switch               motionSensor             "motion sensor [MAP(motion.map):%s]"  { channel="insteon2:device:home:AABBCC:motion"}
Number:Dimensionless motionSensorBatteryLevel "battery level [%.1f %%]"             { channel="insteon2:device:home:AABBCC:batteryLevel" }
Number:Dimensionless motionSensorLightLevel   "light level [%.1f %%]"               { channel="insteon2:device:home:AABBCC:lightLevel" }
```

and create a file "motion.map" in the transforms directory with these entries:

```text
ON=detected
OFF=cleared
-=unknown
```

The motion sensor II includes additional channels:

```java
Contact            motionSensorTamperSwitch "tamper switch [MAP(contact.map):%s]" { channel="insteon2:device:home:AABBCC:tamperSwitch" }
Number:Temperature motionSensorTemperature  "temperature [%.1f °F]"               { channel="insteon2:device:home:AABBCC:temperature" }
```

The temperature is automatically calculated in Fahrenheit based on the motion sensor II powered source.
Since that sensor might not be calibrated correctly, the output temperature may need to be offset on the openHAB side.

Note that battery and light level are only updated when either there is motion, light level above/below threshold, tamper switch activated, or the sensor battery runs low.

### Hidden Door Sensors

Similar in operation to the motion sensor above.
Link such that the modem is a responder to the motion sensor.

##### Items

```java
Contact              doorSensor             "door sensor [MAP(contact.map):%s]" { channel="insteon2:device:home:AABBCC:contact" }
Number:Dimensionless doorSensorBatteryLevel "battery level [%.1f %%]"           { channel="insteon2:device:home:AABBCC:batteryLevel" }
```

and create a file "contact.map" in the transforms directory with these entries:

```text
OPEN=open
CLOSED=closed
-=unknown
```

Note that battery level is only updated when the sensor is triggered or through its daily heartbeat.

### Locks

It is important to sync with the lock contorller within 5 feet to avoid bad connection and link twice for both ON and OFF functionality.

##### Items

```java
Switch doorLock "Front Door [MAP(lock.map):%s]"  { channel="insteon2:device:home:AABBCC:lock" }
```

and create a file "lock.map" in the transforms directory with these entries:

```text
ON=locked
OFF=unlocked
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

##### Items

```java
Switch  garageDoorOpener  "door opener"                        { channel="insteon2:device:home:AABBCC:switch" }
Contact garageDoorContact "door contact [MAP(contact.map):%s]" { channel="insteon2:device:home:AABBCC:contact" }
```

and create a file "contact.map" in the transforms directory with these entries:

```text
OPEN=open
CLOSED=closed
-=unknown
```

> NOTE: If the I/O Linc contact status appears delayed, or returns the wrong value when the sensor changes states, the contact was likely ON (status LED lit) when the modem was linked as a responder.
Examples of this behavior would include: The status remaining CLOSED for up to 3 minutes after the door is opened, or the status remains OPEN for up to three minutes after the garage is opened and immediately closed again.
To resolve this behavior the I/O Linc will need to be unlinked and then re-linked to the modem with the contact OFF (stats LED off).
That would be with the door open when using the Insteon garage kit.

### Fan Controllers

Here is an example configuration for a FanLinc module, which has a dimmable light and a variable speed fan:

##### Items

```java
Dimmer fanLincDimmer "dimmer [%d %%]" { channel="insteon2:device:home:AABBCC:dimmer" }
String fanLincFan    "fan speed"      { channel="insteon2:device:home:AABBCC:fanSpeed" }
```

##### Sitemap

```perl
Slider item=fanLincDimmer switchSupport
Switch item=fanLincFan mappings=[ OFF="OFF", LOW="LOW", MEDIUM="MEDIUM", HIGH="HIGH" ]
```

### Power Meters

The iMeter Solo reports both wattage and kilowatt hours, and is updated during the normal polling process of the devices.
Send a `REFRESH` command to force update the current values for the device.
Additionally, the device can be reset.

See the example below:

##### Items

```java
Number:Power  iMeterPower   "power [%d W]"       { channel="insteon2:device:home:AABBCC:powerUsage" }
Number:Energy iMeterEnergy  "energy [%.04f kWh]" { channel="insteon2:device:home:AABBCC:energyUsage" }
Switch        iMeterReset   "reset"              { channel="insteon2:device:home:AABBCC:reset" }
```

### Sirens

When turning on the siren directly, the binding will trigger the siren with no delay and up to the maximum duration (~2 minutes).
The channels to change the alarm delay and duration are only for the siren arming behavior.

Here is an example configuration for a siren module:

##### Items

```java
Switch siren                   "siren"                 { channel="insteon2:device:home:AABBCC:siren" }
Switch sirenArmed              "armed"                 { channel="insteon2:device:home:AABBCC:armed" }
Switch sirenAlarmDelay         "alarm delay"           { channel="insteon2:device:home:AABBCC:alarmDelay" }
Number:Time sirenAlarmDuration "alarm duration [%d s]" { channel="insteon2:device:home:AABBCC:alarmDuration" }
String sirenAlarmType          "alarm type [%s]"       { channel="insteon2:device:home:AABBCC:alarmType" }
```

##### Sitemap

```perl
Switch   item=siren
Text     item=sirenArmed
Switch   item=sirenAlarmDelay
Setpoint item=sirenAlarmDuration minValue=0 maxValue=127 step=1
Switch   item=sirenAlarmType mappings=[ CHIME="CHIME", LOUD_SIREN="LOUD SIREN" ]
```

### Sprinklers

The EZRain device controls up to 8 sprinkler valves and 4 programs.
It can also enable pump control on the 8th valve.
Only one sprinkler valve can be on at the time.
When pump control is enabled, the 8th valve will remain on and cannot be controlled at the valve level.
Each sprinkler program can be turned on/off by using `PLAY` and `PAUSE` commands.
To skip forward or back to the next or previous valve in the program, use `NEXT` and `PREVIOUS` commands.

##### Items

```java
Switch valve1   "valve 1"   { channel="insteon2:device:home:AABBCC:valve1" }
Switch valve2   "valve 2"   { channel="insteon2:device:home:AABBCC:valve2" }
Switch valve3   "valve 3"   { channel="insteon2:device:home:AABBCC:valve3" }
Switch valve4   "valve 4"   { channel="insteon2:device:home:AABBCC:valve4" }
Switch valve5   "valve 5"   { channel="insteon2:device:home:AABBCC:valve5" }
Switch valve6   "valve 6"   { channel="insteon2:device:home:AABBCC:valve6" }
Switch valve7   "valve 7"   { channel="insteon2:device:home:AABBCC:valve7" }
Switch valve8   "valve 8"   { channel="insteon2:device:home:AABBCC:valve8" }
Switch pump     "pump"      { channel="insteon2:device:home:AABBCC:pump" }
Player program1 "program 1" { channel="insteon2:device:home:AABBCC:program1" }
Player program2 "program 2" { channel="insteon2:device:home:AABBCC:program2" }
Player program3 "program 3" { channel="insteon2:device:home:AABBCC:program3" }
Player program4 "program 4" { channel="insteon2:device:home:AABBCC:program4" }
```

### Thermostats

The thermostat (2441TH) is one of the most complex Insteon devices available.
To ensure all links are configured between the modem and device, and the status reporting is enabled, use the `insteon2 device addMissingLinks` console command.

#### Items

```java
Number:Temperature   thermostatCoolPoint   "cool point [%.1f °F]"  { channel="insteon2:device:home:AABBCC:coolSetPoint" }
Number:Temperature   thermostatHeatPoint   "heat point [%.1f °F]"  { channel="insteon2:device:home:AABBCC:heatSetPoint" }
String               thermostatSystemMode  "system mode [%s]"      { channel="insteon2:device:home:AABBCC:systemMode" }
String               thermostatSystemState "system state [%s]"     { channel="insteon2:device:home:AABBCC:systemState" }
String               thermostatFanMode     "fan mode [%s]"         { channel="insteon2:device:home:AABBCC:fanMode" }
Number:Temperature   thermostatTemperature "temperature [%.1f °F]" { channel="insteon2:device:home:AABBCC:temperature" }
Number:Dimensionless thermostatHumidity    "humidity [%.0f %%]"    { channel="insteon2:device:home:AABBCC:humidity" }
```

Add this as well for some more exotic features:

```java
Number:Time          thermostatACDelay      "A/C delay [%d min]"        { channel="insteon2:device:home:AABBCC:acDelay" }
Number:Time          thermostatBacklight    "backlight [%d sec]"        { channel="insteon2:device:home:AABBCC:backlightDuration" }
Number:Time          thermostatStage1       "A/C stage 1 time [%d min]" { channel="insteon2:device:home:AABBCC:stage1Duration" }
Number:Dimensionless thermostatHumidityHigh "humidity high [%d %%]"     { channel="insteon2:device:home:AABBCC:humidityHigh" }
Number:Dimensionless thermostatHumidityLow  "humidity low [%d %%]"      { channel="insteon2:device:home:AABBCC:humidityLow" }
String               thermostatTempFormat   "temperature format [%s]"   { channel="insteon2:device:home:AABBCC:temperatureFormat" }
String               thermostatTimeFormat   "time format [%s]"          { channel="insteon2:device:home:AABBCC:timeFormat" }
```

#### Sitemap

For the thermostat to display in the GUI, add this to the sitemap file:

```perl
Text     item=thermostatTemperature icon="temperature"
Text     item=thermostatHumidity
Setpoint item=thermostatCoolPoint icon="temperature" minValue=63 maxValue=90 step=1
Setpoint item=thermostatHeatPoint icon="temperature" minValue=50 maxValue=80 step=1
Switch   item=thermostatSystemMode mappings=[ OFF="OFF", HEAT="HEAT", COOL="COOL", AUTO="AUTO", PROGRAM="PROGRAM" ]
Text     item=thermostatSystemState
Switch   item=thermostatFanMode mappings=[ AUTO="AUTO", ON="ALWAYS ON" ]
Setpoint item=thermostatACDelay minValue=2 maxValue=20 step=1
Setpoint item=thermostatBacklight minValue=0 maxValue=100 step=1
Setpoint item=thermostatHumidityHigh minValue=0 maxValue=100 step=1
Setpoint item=thermostatHumidityLow  minValue=0 maxValue=100 step=1
Setpoint item=thermostatStage1 minValue=1 maxValue=60 step=1
Switch   item=thermostatTempFormat mappings=[ CELSIUS="CELSIUS", FAHRENHEIT="FAHRENHEIT" ]
```

### Window Coverings

Here is an example configuration for a micro open/close module (2444-222) in the .items file:

```java
Rollershutter windowShade "window shade" { channel="insteon2:device:home:AABBCC:rollershutter" }
```

Similar to [dimmers](#dimmers), the binding uses the device on level and ramp rate local settings to set the rollershutter level, the same way it would be set when physically interacting with the controller, and can be overridden using the `onLevel` and `rampRate`channel parameters.

## Insteon Scenes

The binding can trigger scenes by commanding the modem to send broadcasts to a given Insteon group.

### Things

```java
Bridge insteon2:plm:home [serialPort="/dev/ttyUSB0"] {
  Thing scene scene42 [group=42]
}
```

### Items

```java
Switch sceneOnOff               "scene on/off"        { channel="insteon2:scene:home:scene42:sceneOnOff" }
Switch sceneFastOnOff           "scene fast on/off"   { channel="insteon2:scene:home:scene42:sceneFastOnOff" }
Rollershutter sceneManualChange "scene manual change" { channel="insteon2:scene:home:scene42:sceneManualChange" }
```

### Sitemap

```perl
Switch item=sceneOnOff
Switch item=sceneFastOnOff mappings=[ ON="ON", OFF="OFF" ]
Switch item=sceneManualChange mappings=[ UP="UP", DOWN="DOWN", STOP="STOP" ]
```

Sending `ON` command to `sceneOnOff` will cause the modem to send a broadcast message with group=42, and all devices that are configured to respond to it should react.
The current state of a scene is published on the `sceneOnOff` channel.
An `ON` state indicates that all the device states associated to a scene are matching their configured link on level.

## X10 Devices

It is worth noting that both the Insteon PLM and the 2014 Hub can both command X10 devices over the powerline, and also set switch stats based on X10 signals received over the powerline.
This allows openHAB not only control X10 devices without the need for other hardware, but it can also have rules that react to incoming X10 powerline commands.

Note that X10 switches/dimmers send no status updates when toggled manually.

### Things

```java
Bridge insteon2:plm:home [serialPort="/dev/ttyUSB0"] {
  Thing x10 A2 [houseCode="A", unitCode=2, deviceType="X10_Switch"]
  Thing x10 B4 [houseCode="B", unitCode=4, deviceType="X10_Dimmer"]
  Thing x10 C6 [houseCode="C", unitCode=6, deviceType="X10_Sensor"]
}
```

### Items

```java
Switch  x10Switch "X10 switch" { channel="insteon2:x10:home:A2:switch" }
Dimmer  x10Dimmer "X10 dimmer" { channel="insteon2:x10:home:B4:dimmer" }
Contact x10Contact "X10 contact" { channel="insteon2:x10:home:C6:contact" }
```

## Battery Powered Devices

Battery powered devices (mostly sensors) work differently than standard wired one.
To conserve battery, these devices are only pollable when there are awake.
Typically they send a heartbeat every 24 hours.
When the binding receives a message from one of these devices, it polls additional information needed during the awake period (about 4 seconds).
Some wireless devices have a `stayAwake` channel that can extend the period up to 4 minutes but at the cost of using more battery.
It shouldn't be used in most cases except during initial device configuration.
Same goes with commands, the binding will queue up commands requested on these devices and send them during the awake time window.
Only one command per channel is queued, this mean that subsequent requests will overwrite previous ones.

### Heartbeat Timeout Monitor

Sensor devices that supports heartbeat have a timeout monitor.
If no broadcast message is received within a specific interval, the associated thing status will go offline until the binding receives a broadcast message from that device.
The heartbeat interval on most sensor devices is hard coded as 24 hours but some have the ability to change that interval through the `heartbeatInterval` channel.
It is enabled by default on devices that supports that feature and will be disabled on devices that have the ability to turn off their heartbeat through the `heartbeatOnOff` channel.
It is important that the heartbeat group (typically 4) is linked properly to the modem by using the `insteon2 device addMissingLinks` console command.
Otherwise, if the link is missing, the timeout monitor will be disabled.
If necessary, the heartbeat timeout monitor can be manually reset by disabling and re-enabling the associated device thing.

## Related Devices

When an Insteon device changes its state because it is directly operated (for example by flipping a switch manually), it sends out a broadcast message to announce the state change, and the binding (if the PLM modem is properly linked as a responder) should update the corresponding openHAB items.
Other linked devices however may also change their state in response, but those devices will _not_ send out a broadcast message, and so openHAB will not learn about their state change until the next poll.
One common scenario is e.g. a switch in a 3-way configuration, with one switch controlling the load, and the other switch being linked as a controller.
In this scenario, when the binding receives a broadcast message from one of these devices indicating a state change, it will poll the other related devices shortly after, instead of waiting until the next scheduled device poll which can take minutes.
It is important to note, that the binding will now automatically determine related devices, based on device link databases, deprecating the `related` channel parameter.
Likewise, the related devices from triggered button events will be polled as well.
For scenes, these will be polled based on the modem database, after sending a group broadcast message.

## Triggered Events

In order to monitor if an Insteon device button was directly operated and the type of interaction, triggered event channels can be used.
These channels have the sole purpose to be used in rules in order to set off subsequent actions based on these events.
Below are examples, including all available events, of a dimmer button and a keypad button:

```php
rule "Dimmer Paddle Events"
when
  Channel 'insteon2:device:home:dimmer:eventButton' triggered
then
  switch receivedEvent {
    case PRESSED_ON:         // do something (regular on)
    case PRESSED_OFF:        // do something (regular off)
    case DOUBLE_PRESSED_ON:  // do something (fast on)
    case DOUBLE_PRESSED_OFF: // do something (fast off)
    case HELD_UP:            // do something (manual change up)
    case HELD_DOWN:          // do something (manual change down)
    case RELEASED:           // do something (manual change stop)
  }
end

rule "Keypad Button A Pressed Off"
when
  Channel 'insteon2:device:home:keypad:eventButtonA' triggered PRESSED_OFF
then
  // do something
end
```

## Troubleshooting

Turn on DEBUG or TRACE logging for `org.openhab.binding.insteon2`.
See [logging in openHAB](https://www.openhab.org/docs/administration/logging.html) for more info.

### Device Permissions / Linux Device Locks

When openHAB is running as a non-root user (Linux/OSX) it is important to ensure it has write access not just to the PLM device, but to the os lock directory.
Under openSUSE this is `/run/lock` and is managed by the **lock** group.

Example commands to grant openHAB access, depending on Linux distribution:

```shell
usermod -a -G dialout openhab
usermod -a -G lock openhab
```

Insufficient access to the lock directory will result in openHAB failing to access the device, even if the device itself is writable.

## Known Limitations and Issues

- Using the Insteon binding in conjunction with other applications (such as the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) or the Insteon App) can result in some unexpected behavior.
