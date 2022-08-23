# Insteon Binding

Insteon is a home area networking technology developed primarily for connecting light switches and loads.
Insteon devices send messages either via the power line, or by means of radio frequency (RF) waves, or both (dual-band.
A considerable number of Insteon compatible devices such as switchable relays, thermostats, sensors etc are available.
More about Insteon can be found on [Wikipedia](https://en.wikipedia.org/wiki/Insteon).

This binding provides access to the Insteon network by means of either an Insteon PowerLinc Modem (PLM), a legacy Insteon Hub 2242-222 or the current 2245-222 Insteon Hub.
The modem can be connected to the openHAB server either via a serial port (Model 2413S) or a USB port (Model 2413U).
The Insteon PowerLinc Controller (Model 2414U) is not supported since it is a PLC not a PLM.
The modem can also be connected via TCP (such as ser2net).
The binding translates openHAB commands into Insteon messages and sends them on the Insteon network.
Relevant messages from the Insteon network (like notifications about switches being toggled) are picked up by the modem and converted to openHAB state updates by the binding.
The binding also supports sending and receiving of legacy X10 messages.

The openHAB binding supports monitoring and sending messages, configuring most of the device local settings and linking a device to the modem.
However, for more advanced configuration such as linking devices to each other, it can be done manually via the set button, or use the free [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) software.
The free HouseLinc software from Insteon can also be used for configuration, but it wipes the modem link database clean on its initial use, requiring to re-link the modem to all devices.

At startup, the binding will download the modem database along with each configured device all-link database if not previously downloaded and currently awake.
Therefore, the initialization on the first start may take some additional time to complete depending on the number of devices configured.
The device link databases are only downloaded once unless the binding receives an indication that a database was updated or marked to be refreshed via the openHAB console.

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

An Insteon bridge is not automatically discovered, you will have to manually add the it yourself.
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
| devicePollIntervalInSeconds | 300 | No | Device poll interval in seconds. Poll too often and you will overload the Insteon network, leading to sluggish or no response when trying to send messages to devices. The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers. |
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
| devicePollIntervalInSeconds | 300 | No | Device poll interval in seconds. Poll too often and you will overload the Insteon network, leading to sluggish or no response when trying to send messages to devices. The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers. |
| deviceDiscoveryEnabled | true | No | Discover Insteon devices found in the hub database but not configured. |
| sceneDiscoveryEnabled | false | No | Discover Insteon scenes found in the hub database but not configured. |
| deviceSyncEnabled | false | No | Synchronize related devices based on their all-link database. |

### Insteon PLM Configuration

The Insteon PLM is configured with the following parameters:

| Parameter | Default | Required | Description |
|-----------|:-------:|:--------:|-------------|
| serialPort | | Yes | Serial port connected to the modem. Example: `/dev/ttyS0` or `COM1` |
| baudRate | 19200 | No | Serial port baud rate connected to the modem. |
| devicePollIntervalInSeconds | 300 | No | Device poll interval in seconds. Poll too often and you will overload the Insteon network, leading to sluggish or no response when trying to send messages to devices. The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers. |
| deviceDiscoveryEnabled | true | No | Discover Insteon devices found in the hub database but not configured. |
| sceneDiscoveryEnabled | false | No | Discover Insteon scenes found in the hub database but not configured. |
| deviceSyncEnabled | false | No | Synchronize related devices based on their all-link database. |

### Insteon Device Configuration

The Insteon device is configured with the following parameter:

| Parameter | Required | Description |
|-----------|:--------:|-------------|
| address | Yes | Insteon address of the device. It can be found on the device. Example: `12.34.56`. |

The device type is automatically determine by the binding using the device product data. For a [battery powered device](#battery-powered-devices) that was never configured previously, it may take until the next time that device sends a broadcast message to be modeled properly. To speed up the process for this case, it is recommended to force the device to become awake after the associated bridge is online.

### Insteon Scene Configuration

The Insteon scene is configured with the following parameter:

| Parameter | Required | Description |
|-----------|:--------:|-------------|
| group | Yes | Insteon scene group number between 1 and 255. It can be found in the scene detailed information in the Insteon mobile app. |

### X10 Device Configuration

The X10 device is configured with the following parameters:

| Parameter | Required | Description |
|-----------|:--------:|-------------|
| houseCode | Yes | X10 house code of the device. Example: `A`|
| unitCode | Yes | X10 unit code of the device. Example: `1` |
| deviceType | Yes | X10 device type |

The following is a list of the supported X10 device types:

| Device Type | Description |
|-------------|-------------|
| X10_Switch | X10 Switch |
| X10_Dimmer | X10 Dimmer |
| X10_Sensor | X10 Sensor |

## Channels

Below is the list of possible channels for the Insteon devices.
In order to determine which channels a device supports, you can look at the device in the UI, or with the `listDevices` console command.

### State Channels

| Channel | Type | Access Mode | Description |
|---------|------|-------------|-------------|
| acDelay | Number:Time | R/W | AC Delay |
| alarmDelay | Switch | R/W | Alarm Delay |
| alarmDuration | Number:Time | R/W | Alarm Duration |
| alarmType | String | R/W | Alarm Type |
| armed | Switch | R | Armed State |
| backlightDuration | Number:Time | R/W | Back Light Duration |
| batteryLevel | Number:Dimensionless | R | Battery Level |
| batteryPowered | Switch | R | Battery Powered State |
| beep | Switch | W | Beep |
| broadcastOnOff | Switch | W | Broadcast On/Off |
| broadcastFastOnOff | Switch | W | Broadcast Fast On/Off |
| broadcastManualChange | Rollershutter | W | Broadcast Manual Change |
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
| fanMode | String | R/W | Fan Mode |
| fanSpeed | String | R/W | Fan Speed |
| fanState | Switch | R | Fan State |
| heatSetPoint | Number:Temperature | R/W | Heat Set Point |
| humidity | Number:Dimensionless | R | Current Humidity |
| humidityControl | String | R | Humidity Control State |
| humidityHigh | Number:Dimensionless | R/W | Humidity High |
| humidityLow | Number:Dimensionless | R/W | Humidity Low |
| kWh | Number:Energy | R | Energy Usage in Kilowatt Hour |
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
| programLock | Switch | R/W | Local Programming Lock |
| rampRate | Number:Time | R/W | Ramp Rate |
| relayMode | String | R/W | Output Relay Mode |
| relaySensorFollow | Switch | R/W | Output Relay Follows Input Sensor |
| reset | Switch | W | Reset |
| resumeDim | Switch | R/W | Resume Dim |
| rollershutter | Rollershutter | R/W | Rollershutter |
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
| toggleMode | Switch | R/W | 3-Way Toggle Mode |
| toggleModeButtonA | Switch | R/W | Toggle Mode Button A |
| toggleModeButtonB | Switch | R/W | Toggle Mode Button B |
| toggleModeButtonC | Switch | R/W | Toggle Mode Button C |
| toggleModeButtonD | Switch | R/W | Toggle Mode Button D |
| toggleModeButtonE | Switch | R/W | Toggle Mode Button E |
| toggleModeButtonF | Switch | R/W | Toggle Mode Button F |
| toggleModeButtonG | Switch | R/W | Toggle Mode Button G |
| toggleModeButtonH | Switch | R/W | Toggle Mode Button H |
| watts | Number:Power | R | Power Usage in Watts |

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

The following is a list of the supported triggered events for Insteon Device things:

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

```
Bridge insteon:plm:home [serialPort="/dev/ttyUSB0"] {
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

```
Switch switch1 { channel="insteon:device:home:243141:switch" }
Dimmer dimmer1 { channel="insteon:device:home:238F55:dimmer" }
Dimmer dimmer2 { channel="insteon:device:home:23B0D9:dimmer" }
Dimmer dimmer3 { channel="insteon:device:home:238FC9:dimmer" }
Dimmer keypad  { channel="insteon:device:home:22F8A8:dimmer" }
Switch keypadA { channel="insteon:device:home:22F8A8:buttonA" }
Switch keypadB { channel="insteon:device:home:22F8A8:buttonB" }
Switch keypadC { channel="insteon:device:home:22F8A8:buttonC" }
Switch keypadD { channel="insteon:device:home:22F8A8:buttonD" }
Switch scene42 { channel="insteon:scene:home:scene42:broadcastOnOff" }
Switch switch2 { channel="insteon:x10:home:A2:switch" }
```

## Console Commands

The binding provides commands you can use to help with troubleshooting.
Enter `openhab:insteon` or `insteon` in the console and you will get a list of available commands.

```
openhab> insteon
Usage: openhab:insteon listDevices - list configured Insteon/X10 devices with related channels and status
Usage: openhab:insteon listScenes - list configured Insteon scenes with related channels and status
Usage: openhab:insteon listChannels [<thingId>] - list available channel ids with configuration and link state, optionally limiting to a thing
Usage: openhab:insteon listModemDatabase - list Insteon PLM or hub database details
Usage: openhab:insteon listDeviceDatabase <address> - list a device all-link database records
Usage: openhab:insteon listDeviceProductData <address> - list a device product data
Usage: openhab:insteon listMonitored - list monitored device(s)
Usage: openhab:insteon linkDevice [<address>] - link a device to the modem, optionally providing its address
Usage: openhab:insteon unlinkDevice <address> - unlink a device from the modem
Usage: openhab:insteon refreshDevice <address> - refresh a device
Usage: openhab:insteon startMonitoring all|<address> - start logging message events for device(s) in separate file(s)
Usage: openhab:insteon stopMonitoring all|<address> - stop logging message events for device(s) in separate file(s)
Usage: openhab:insteon sendBroadcastMessage <group> <cmd1> <cmd2> - send a broadcast message to a group
Usage: openhab:insteon sendStandardMessage <address> <cmd1> <cmd2> - send a standard message to a device
Usage: openhab:insteon sendExtendedMessage <address> <cmd1> <cmd2> [<data1> ... <data13>] - send an extended message with standard crc to a device
Usage: openhab:insteon sendExtendedMessage2 <address> <cmd1> <cmd2> [<data1> ... <data12>] - send an extended message with a two-byte crc to a device
Usage: openhab:insteon sendX10Message <address> <cmd> - send an X10 message to a device
Usage: openhab:insteon sendIMMessage <name> [<data1> <data2> ...] - send an IM message to the modem
Usage: openhab:insteon switchModem <address> - switch modem bridge to use if more than one configured and enabled
```

## Insteon Groups and Scenes

How do Insteon devices tell other devices on the network that their state has changed? They send out a broadcast message, labeled with a specific *group* number.
All devices (called *responders*) that are configured to listen to this message will then go into a pre-defined state.
For instance when light switch A is switched to "ON", it will send out a message to group #1, and all responders will react to it, e.g they may go into the "ON" position as well.
Since more than one device can participate, the sending out of the broadcast message and the subsequent state change of the responders is referred to as "triggering a scene".

Many Insteon devices send out messages on different group numbers, depending on what happens to them.
A leak sensor may send out a message on group #1 when dry, and on group #2 when wet.
The default group used for e.g. linking two light switches is usually group #1.

The binding can now automatically determines the broadcast groups between the modem and linked devices, based on their all-link databases.

By default, the binding only sends single direct messages to the intended device to update its state, leaving the state of the related devices unchanged.
Whenever the bridge parameter `deviceSyncEnabled` is set to `true`, broadcast messages for supported Insteon commands (e.g. on/off, bright/dim, manual change) are sent to all responders of a given group, updating all related devices in one request.
If no broadcast group is determined or for Insteon commands that don't support broadcasting (e.g. percent), direct messages are sent to each related device instead, to adjust their level based on their all-link database.

## Insteon Binding Process

Before Insteon devices communicate with one another, they must be linked.
During the linking process, one of the devices will be the "Controller", the other the "Responder".

The responder listens to messages from the controller, and reacts to them.
Note that except for the case of a motion detector (which is just a controller to the modem), the modem controls the device (e.g. send on/off messages to it), and the device controls the modem (so the modem learns about the switch being toggled.
For this reason, most devices and in particular switches/dimmers should be linked twice, with one taking the role of controller during the first linking, and the other acting as controller during the second linking process.
To do so, first press and hold the "Set" button on the modem until the light starts blinking.
Then press and hold the "Set" button on the remote device,
e.g. the light switch, until it double beeps (the light on the modem should go off as well.
Now do exactly the reverse: press and hold the "Set" button on the remote device until its light starts blinking, then press and hold the "Set" button on the modem until it double beeps, and the light of the remote device (switch) goes off.

Alternatively, the binding can link a device to the modem programmatically using the `linkDevice` console command. Based on the initial set button pressed event received, the device will be linked one or both ways. This process only supports the linking of the primary function groups 0 and 1. Use [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) to link any additional groups for more complex devices.

## Insteon Devices

Since Insteon devices can have multiple features (for instance a switchable relay and a contact sensor) under a single Insteon device, an openHAB item is not bound to a device, but to a given feature of a device.
For example, the following lines would create two Number items referring to the same thermostat device, but to different features of it:

```
    Number:Temperature  thermostatCoolPoint "cool point [%.1f °F]" { channel="insteon:device:home:32F422:coolSetPoint" }
    Number:Temperature  thermostatHeatPoint "heat point [%.1f °F]" { channel="insteon:device:home:32F422:heatSetPoint" }
```

### Switches

The following example shows how to configure a simple light switch (2477S) in the .items file:

```
    Switch officeLight "office light" { channel="insteon:device:home:AABBCC:switch" }
```

### Dimmers

Here is how to configure a simple dimmer (2477D) in the .items file:

```
    Dimmer kitchenChandelier "kitchen chandelier" { channel="insteon:device:home:AABBCC:dimmer" }
```

For `ON` command requests, the binding uses the device on level and ramp rate local settings to set the dimmer level, the same way it would be set when physically pressing on the dimmer. These settings can be controlled using the `onLevel` and `rampRate` channels.

Alternatively, these settings can be overridden using the `onLevel` and `rampRate` channel parameters.
Doing so will result in different type of commands being triggered as opposed to having separate channels previously such as `fastOnOff`, `manualChange` and `rampDimmer` handling it.

When the `rampRate` parameter is configured, the binding will send a ramp rate command (previously generated by the `rampDimmer` channel) to the relevant device to set the level at the defined ramp rate.
When this parameter is set to instant (0.1 sec), on/off commands will trigger what used to be handled by the `fastOnOff` channel.
And percent commands will trigger what is defined in the Insteon protocol as instant change requests.

As far as the previously known `manualChange` channel, it has been rolled into the `rollershutter` channel for [window covering](#window-coverings) using `UP`, `DOWN` and `STOP` commands.
For the `dimmer` channel, the `INCREASE` and `DECREASE` commands can be used instead.

Ultimately, the `dimmer` channel parameters can be used to create custom channels via a thing file that can work as an alternative to having to configure an Insteon scene for a single device.

```
    Thing device 23B0D9 [address="23.B0.D9"] {
      Channels:
        Type dimmer : custom1 [onLevel=50, rampRate=150] // 50% on level at 2.5 minutes ramp rate
        Type dimmer : custom2 [onLevel=80]               // 80% on level at device configured ramp rate
        Type dimmer : custom3 [rampRate=480]             // device configured on level at 8 minutes ramp rate
    }
```

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
For instructions how to do this, check out the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) documentation.
You can even do that with the set buttons (see instructions that come with the keypad).

To accomplish this, you need to pick a set of unused groups that is globally unique (if you have multiple keypads, each one of them has to use different groups), one group for each button.
Then link the buttons such that they respond to those groups, and link the modem as a controller for them.

While previously, keypad buttons required a broadcast group to be configured, the binding now automatically determines that setting, based on the device link databases, deprecating the `group` channel parameter.
By default, the binding will only change the button led state when receiving on/off commands, depending on the keypad local radio group settings.
For button broadcast group support, set the bridge parameter `deviceSyncEnabled` to `true`.
Additionally, for button toggle mode set to always on, only `ON` commands will be processed, in line with the physical interaction.

#### Keypad Switches

**Items**

The following items will expose a keypad switch and its associated buttons:

```
    Switch keypadSwitch             "main switch"        { channel="insteon:device:home:AABBCC:switch" }
    Switch keypadSwitchA            "button A"           { channel="insteon:device:home:AABBCC:buttonA"}
    Switch keypadSwitchB            "button B"           { channel="insteon:device:home:AABBCC:buttonB"}
    Switch keypadSwitchC            "button C"           { channel="insteon:device:home:AABBCC:buttonC"}
    Switch keypadSwitchD            "button D"           { channel="insteon:device:home:AABBCC:buttonD"}
```


**Sitemap**

The following sitemap will bring the items to life in the GUI:

```
    Frame label="Keypad" {
          Switch item=keypadSwitch label="main"
          Switch item=keypadSwitchA label="button A"
          Switch item=keypadSwitchB label="button B"
          Switch item=keypadSwitchC label="button C"
          Switch item=keypadSwitchD label="button D"
    }
```

**Rules**

The following rules will monitor regular on/off, fast on/off and manual change button events:

```
    rule "Main Button Off Event"
    when
      Channel 'insteon:device:home:AABBCC:eventButtonMain' triggered PRESSED_OFF
    then
      // do something
    end

    rule "Main Button Fast On/Off Events"
    when
      Channel 'insteon:device:home:AABBCC:eventButtonMain' triggered DOUBLE_PRESSED_ON or
      Channel 'insteon:device:home:AABBCC:eventButtonMain' triggered DOUBLE_PRESSED_OFF
    then
      // do something
    end

    rule "Main Button Manual Change Stop Event"
    when
      Channel 'insteon:device:home:AABBCC:eventButtonMain' triggered RELEASED
    then
      // do something
    end

    rule "Keypad Button A On Event"
    when
      Channel 'insteon:device:home:AABBCC:eventButtonA' triggered PRESSED_ON
    then
      // do something
    end
```

#### Keypad Dimmers

The keypad dimmers are like keypad switches, except that the main load is dimmable.

**Items**

```
    Dimmer keypadDimmer           "main dimmer" { channel="insteon:device:home:AABBCC:dimmer" }
    Switch keypadDimmerButtonA    "button A"    { channel="insteon:device:home:AABBCC:buttonA" }
```

**Sitemap**

```
    Slider item=keypadDimmer label="main" switchSupport
    Switch item=keypadDimmerButtonA label="button A"
```

### Outlets

Here's how to configure the top and bottom outlet of the in-wall 2 outlet controller:

```
    Switch outletTop    "Outlet Top"    { channel="insteon:device:home:AABBCC:topOutlet" }
    Switch outletBottom "Outlet Bottom" { channel="insteon:device:home:AABBCC:bottomOutlet" }
```

This will give you individual control of each outlet.

### Mini Remotes

Link the mini remote to be a controller of the modem by using the set button.
Link all buttons, one after the other.
The 4-button mini remote sends out messages on groups 0x01 - 0x04, each corresponding to one button.
The modem's link database (see [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal)) should look like this:

```
    0000 xx.xx.xx                       xx.xx.xx  RESP  10100010 group: 01 data: 02 2c 41
    0000 xx.xx.xx                       xx.xx.xx  RESP  10100010 group: 02 data: 02 2c 41
    0000 xx.xx.xx                       xx.xx.xx  RESP  10100010 group: 03 data: 02 2c 41
    0000 xx.xx.xx                       xx.xx.xx  RESP  10100010 group: 04 data: 02 2c 41
```

The mini remote buttons cannot be modeled as items since they don't have a state or can receive commands. However, you can monitor button triggered events through rules that can set off subsequent actions:

**Rules**

```
    rule "Mini Remote Button A Pressed On"
    when
      Channel 'insteon:device:home:miniRemote:eventButtonA' triggered PRESSED_ON
    then
      // do something
    end
```

### Motion Sensors

Link such that the modem is a responder to the motion sensor.

**Items**

```
    Switch               motionSensor             "motion sensor [MAP(motion.map):%s]"  { channel="insteon:device:home:AABBCC:motion"}
    Number:Dimensionless motionSensorBatteryLevel "battery level [%.1f %%]"             { channel="insteon:device:home:AABBCC:batteryLevel" }
    Number:Dimensionless motionSensorLightLevel   "light level [%.1f %%]"               { channel="insteon:device:home:AABBCC:lightLevel" }
```

and create a file "motion.map" in the transforms directory with these entries:

```
    ON=detected
    OFF=cleared
    -=unknown
```

This will give you the motion state, battery level, and light level.
Note that battery and light level are only updated when either there is motion, light level above/below threshold, tamper switch activated, or the sensor battery runs low.

The motion sensor II includes additional channels:

**Items**

```
    Contact            motionSensorTamperSwitch "tamper switch [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:tamperSwitch" }
    Number:Temperature motionSensorTemperature  "temperature [%.1f °F]"               { channel="insteon:device:home:AABBCC:temperature" }
```

The temperature is automatically calculated in Fahrenheit based on the motion sensor II powered source.
Since that sensor might not be calibrated correctly, the output temperature may need to be offset on the openHAB side.

### Hidden Door Sensors

Similar in operation to the motion sensor above.
Link such that the modem is a responder to the motion sensor.

**Items**

Then create entries in the .items file like this:

```
    Contact              doorSensor             "door sensor [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:contact" }
    Number:Dimensionless doorSensorBatteryLevel "battery level [%.1f %%]"           { channel="insteon:device:home:AABBCC:batteryLevel" }
```

and create a file "contact.map" in the transforms directory with these entries:

```
    OPEN=open
    CLOSED=closed
    -=unknown
```

This will give you the contact state and battery level.
Note that battery level is only updated when the sensor is triggered or through its daily heartbeat.

### Locks

Read the instructions very carefully: sync with lock within 5 feet to avoid bad connection, link twice for both ON and OFF functionality.

**Items**

Put something like this into your .items file:

```
    Switch doorLock "Front Door [MAP(lock.map):%s]"  { channel="insteon:device:home:AABBCC:lock" }
```

and create a file "lock.map" in the transforms directory with these entries:

```
    ON=locked
    OFF=unlocked
    -=unknown
```

### I/O Linc (garage door openers)

The I/O Linc devices are really two devices in one: a relay and a contact.
Link the modem both ways, as responder and controller using the set buttons as described in the instructions.

**Items**

Along with this into your .items file:

```
    Switch  garageDoorOpener  "door opener"                        { channel="insteon:device:home:AABBCC:switch" }
    Contact garageDoorContact "door contact [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:contact" }
```

and create a file "contact.map" in the transforms directory with these entries:

```
    OPEN=open
    CLOSED=closed
    -=unknown
```

For safety reasons, only close the garage door if you have visual contact to make sure there is no obstruction! The use of automated rules for closing garage doors is dangerous.

> NOTE: If the I/O Linc returns the wrong value when the device is polled (For example you open the garage door and the state correctly shows OPEN, but during polling it shows CLOSED), you probably linked the device with the PLM or hub when the door was in the wrong position.
You need unlink and then link again with the door in the opposite position.
Please see the Insteon I/O Linc documentation for further details.

### Fan Controllers

Here is an example configuration for a FanLinc module, which has a dimmable light and a variable speed fan:

**Items**

```
    Dimmer fanLincDimmer "dimmer [%d %%]" { channel="insteon:device:home:AABBCC:dimmer" }
    String fanLincFan    "fan speed"      { channel="insteon:device:home:AABBCC:fanSpeed" }
```

**Sitemap**

```
    Slider item=fanLincDimmer switchSupport
    Switch item=fanLincFan mappings=[ OFF="OFF", LOW="LOW", MEDIUM="MEDIUM", HIGH="HIGH" ]
```

### Power Meters

The iMeter Solo reports both wattage and kilowatt hours, and is updated during the normal polling process of the devices.
You can also force update the current values by sending a `REFRESH` command to the relevant item.
Additionally, the device can be reset.

See the example below:

**Items**

```
    Number:Power  iMeterWatts   "power [%d W]"       { channel="insteon:device:home:AABBCC:watts" }
    Number:Energy iMeterKwh     "energy [%.04f kWh]" { channel="insteon:device:home:AABBCC:kWh" }
    Switch        iMeterReset   "reset"              { channel="insteon:device:home:AABBCC:reset" }
```

### Sirens

When turning on the siren directly, the binding will trigger the siren with no delay and up to the maximum duration (~2 minutes).
The channels to change the alarm delay and duration are only for the siren arming behavior.

Here is an example configuration for a siren module:

**Items**

```
    Switch siren                   "siren"                 { channel="insteon:device:home:AABBCC:siren" }
    Switch sirenArmed              "armed"                 { channel="insteon:device:home:AABBCC:armed" }
    Switch sirenAlarmDelay         "alarm delay"           { channel="insteon:device:home:AABBCC:alarmDelay" }
    Number:Time sirenAlarmDuration "alarm duration [%d s]" { channel="insteon:device:home:AABBCC:alarmDuration" }
    String sirenAlarmType          "alarm type [%s]"       { channel="insteon:device:home:AABBCC:alarmType" }
```

**Sitemap**

```
    Switch   item=siren
    Text     item=sirenArmed
    Switch   item=sirenAlarmDelay
    Setpoint item=sirenAlarmDuration minValue=0 maxValue=127 step=1
    Switch   item=sirenAlarmType mappings=[ CHIME="CHIME", LOUD_SIREN="LOUD SIREN" ]
```

### Thermostats

The thermostat (2441TH) is one of the most complex Insteon devices available.
It must first be properly linked to the modem using configuration software like [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal.
The Insteon Terminal wiki describes in detail how to link the thermostat, and how to make it publish status update reports.

When all is set and done the modem must be configured as a controller to group 0 (not sure why), and a responder to groups 1-5 such that it picks up when the thermostat switches on/off heating and cooling etc, and it must be a responder to special group 0xEF to get status update reports when measured values (temperature) change.
Symmetrically, the thermostat must be a responder to group 0, and a controller for groups 1-5 and 0xEF.
The linking process is not difficult but needs some persistence.
Again, refer to the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) documentation.

**Items**

This is an example of what to put into your .items file:

```
    Number:Temperature   thermostatCoolPoint   "cool point [%.1f °F]"  { channel="insteon:device:home:AABBCC:coolSetPoint" }
    Number:Temperature   thermostatHeatPoint   "heat point [%.1f °F]"  { channel="insteon:device:home:AABBCC:heatSetPoint" }
    String               thermostatSystemMode  "system mode [%s]"      { channel="insteon:device:home:AABBCC:systemMode" }
    String               thermostatSystemState "system state [%s]"     { channel="insteon:device:home:AABBCC:systemState" }
    String               thermostatFanMode     "fan mode [%s]"         { channel="insteon:device:home:AABBCC:fanMode" }
    Number:Temperature   thermostatTemperature "temperature [%.1f °F]" { channel="insteon:device:home:AABBCC:temperature" }
    Number:Dimensionless thermostatHumidity    "humidity [%.0f %%]"    { channel="insteon:device:home:AABBCC:humidity" }
```

Add this as well for some more exotic features:

```
    Number:Time          thermostatACDelay      "A/C delay [%d min]"        { channel="insteon:device:home:AABBCC:acDelay" }
    Number:Time          thermostatBacklight    "backlight [%d sec]"        { channel="insteon:device:home:AABBCC:backlightDuration" }
    Number:Time          thermostatStage1       "A/C stage 1 time [%d min]" { channel="insteon:device:home:AABBCC:stage1Duration" }
    Number:Dimensionless thermostatHumidityHigh "humidity high [%d %%]"     { channel="insteon:device:home:AABBCC:humidityHigh" }
    Number:Dimensionless thermostatHumidityLow  "humidity low [%d %%]"      { channel="insteon:device:home:AABBCC:humidityLow" }
    String               thermostatTempFormat   "temperature format [%s]"   { channel="insteon:device:home:AABBCC:temperatureFormat" }
    String               thermostatTimeFormat   "time format [%s]"          { channel="insteon:device:home:AABBCC:timeFormat" }
```

**Sitemap**

For the thermostat to display in the GUI, add this to the sitemap file:

```
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

```
    Rollershutter windowShade "window shade" { channel="insteon:device:home:AABBCC:rollershutter" }
```

Similar to [dimmers](#dimmers), the binding uses the device on level and ramp rate local settings to set the rollershutter level, the same way it would be set when physically interacting with the controller, and can be overridden using the `onLevel` and `rampRate`channel parameters.

## Insteon Scenes

The binding can trigger scenes by commanding the modem to send broadcasts to a given Insteon group.

**Things**

```
    Bridge insteon:plm:home [serialPort="/dev/ttyUSB0"] {
      Thing scene scene42 [group=42]
    }
```

**Items**

```
    Switch sceneOnOff               "scene on/off"        { channel="insteon:scene:home:scene42:broadcastOnOff" }
    Switch sceneFastOnOff           "scene fast on/off"   { channel="insteon:scene:home:scene42:broadcastFastOnOff" }
    Rollershutter sceneManualChange "scene manual change" { channel="insteon:scene:home:scene42:broadcastManualChange" }
```

**Sitemap**

```
    Switch item=sceneOnOff mappings=[ ON="ON", OFF="OFF" ]
    Switch item=sceneFastOnOff mappings=[ ON="ON", OFF="OFF" ]
    Switch item=sceneManualChange mappings=[ UP="UP", DOWN="DOWN", STOP="STOP" ]
```

Sending `ON` command to `sceneOnOff` will cause the modem to send a broadcast message with group=42, and all devices that are configured to respond to it should react.
Because scenes are stateless, the scene channels will not receive any state updates and should be used as command only.

## X10 Devices

It is worth noting that both the Insteon PLM and the 2014 Hub can both command X10 devices over the powerline, and also set switch stats based on X10 signals received over the powerline.
This allows openHAB not only control X10 devices without the need for other hardware, but it can also have rules that react to incoming X10 powerline commands.
While you cannot bind the the X10 devices to the Insteon PLM/HUB, here are some examples for configuring X10 devices.
Be aware that most X10 switches/dimmers send no status updates, i.e. openHAB will not learn about switches that are toggled manually.
Further note that X10 devices are addressed with `houseCode.unitCode`, e.g. `A.2`.

**Things**

```
    Bridge insteon:plm:home [serialPort="/dev/ttyUSB0"] {
      Thing x10 A2 [houseCode="A", unitCode=2, deviceType="X10_Switch"]
      Thing x10 B4 [houseCode="B", unitCode=4, deviceType="X10_Dimmer"]
      Thing x10 C6 [houseCode="C", unitCode=6, deviceType="X10_Sensor"]
    }
```

**Items**

```
    Switch  x10Switch "X10 switch" { channel="insteon:x10:home:A2:switch" }
    Dimmer  x10Dimmer "X10 dimmer" { channel="insteon:x10:home:B4:dimmer" }
    Contact x10Contact "X10 contact" { channel="insteon:x10:home:C6:contact" }
```

## Battery Powered Devices

Battery powered devices (mostly sensors) work differently than standard wired one.
To conserve battery, these devices are only pollable when there are awake.
Typically they send a heartbeat every 24 hours. When the binding receives a message from one of these devices, it polls additional information needed during the awake period (about 4 seconds).
Some wireless devices have a `stayAwake` channel that can extend the period to 4 minutes but at the cost of using more battery. It shouldn't be used in most cases except during initial device configuration.
Same goes with commands, the binding will queue up commands requested on these devices and send them during the awake time window.
Only one command per channel is queued, this mean that the subsequent requests will overwrite the previous ones.

### Heartbeat Timeout Monitor

Sensor devices that supports heartbeat have a timeout monitor.
If no broadcast message is received within a 24 hours interval, the associated thing status will go offline until the binding receives a broadcast message from that device.

## Related Devices

When an Insteon device changes its state because it is directly operated (for example by flipping a switch manually), it sends out a broadcast message to announce the state change, and the binding (if the PLM modem is properly linked as a responder) should update the corresponding openHAB items.
Other linked devices however may also change their state in response, but those devices will **not** send out a broadcast message, and so openHAB will not learn about their state change until the next poll.
One common scenario is e.g. a switch in a 3-way configuration, with one switch controlling the load, and the other switch being linked as a controller.
In this scenario, when the binding receives a broadcast message from one of these devices indicating a state change, it will poll the other related devices shortly after, instead of waiting until the next scheduled device poll which can take minutes.
It is important to note, that the binding will now automatically determine related devices, based on device link databases, deprecating the `related` channel parameter.
Likewise, the related devices from triggered button events will be polled as well.
For scenes, these will be polled based on the modem database, after sending a group broadcast message.

## Triggered Events

In order to monitor if an Insteon device button was directly operated and the type of interaction, triggered event channels can be used.
These channels have the sole purpose to be used in rules in order to set off subsequent actions based on these events.
Below are examples, including all available events, of a dimmer button and a keypad button:

```
    rule "Dimmer Paddle Events"
    when
      Channel 'insteon:device:home:dimmer:eventButton' triggered
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
      Channel 'insteon:device:home:keypad:eventButtonA' triggered PRESSED_OFF
    then
      // do something
    end
```

If you previously used `fastOnOff` and `manualChange` channels to monitor these events, make sure to update your rules to use the event channels instead.

## Troubleshooting

Turn on DEBUG or TRACE logging for `org.openhab.binding.insteon`.
See [logging in openHAB](https://www.openhab.org/docs/administration/logging.html) for more info.

### Device Permissions / Linux Device Locks

When openHAB is running as a non-root user (Linux/OSX) it is important to ensure it has write access not just to the PLM device, but to the os lock directory.
Under openSUSE this is `/run/lock` and is managed by the **lock** group.

Example commands to grant openHAB access (adjust for your distribution):

````
usermod -a -G dialout openhab
usermod -a -G lock openhab
````

Insufficient access to the lock directory will result in openHAB failing to access the device, even if the device itself is writable.

## Known Limitations and Issues

* Setting up Insteon groups and linking devices cannot be done from within openHAB.
Use the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) for that.
If using Insteon Terminal (especially as root), ensure any stale lock files (For example, /var/lock/LCK..ttyUSB0) are removed before starting openHAB runtime.
* Using the Insteon Hub 2014 in conjunction with other applications (such as the InsteonApp) is not supported. Concretely, openHAB will not learn when a switch is flipped via the Insteon App until the next poll, which could take minutes.
