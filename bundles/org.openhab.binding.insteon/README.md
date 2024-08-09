# Insteon Binding

Insteon is a proprietary home automation system that enables light switches, lights, thermostats, leak sensors, remote controls, motion sensors, and other electrically powered devices to interoperate through power lines, radio frequency (RF) communications, or both (dual-band)
More about Insteon can be found on [Wikipedia](https://en.wikipedia.org/wiki/Insteon).

It provides access to the Insteon network by means of either an Insteon PowerLinc Modem (PLM), the legacy 2242-222 Insteon Hub or the current 2245-222 Insteon Hub 2.
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

The binding has been rewritten in openHAB 4.3 to simplify the user experience by retrieving all the configuration directly from the device when possible, and improving the way the Insteon things are configured in MainUI.
If switching from a previous release, you will need to reconfigure your Insteon environment with the new bridges, things and channels to take advantage of these enhancements.
However, the new version is fully backward compatible by supporting the legacy things.
On the first start, existing `device` things connected to a `network` bridge will be migrated to the `legacy-device` thing type while still keeping the same ids to prevent any breakage.
It is important to note that once the migration has occurred, downgrading to an older version will not be possible.

## Supported Things

| Thing | Type | Description |
|-------|------|-------------|
| hub1  | Bridge | An Insteon Hub Legacy that communicates with Insteon devices. |
| hub2  | Bridge | An Insteon Hub 2 that communicates with Insteon devices. |
| plm  | Bridge | An Insteon PLM that communicates with Insteon devices. |
| device | Thing | An Insteon device such as a switch, dimmer, keypad, sensor, etc. |
| scene | Thing | An Insteon scene that controls multiple devices simultaneously. |
| x10 | Thing | An X10 device such as a switch, dimmer or sensor. |

### Legacy Things

| Thing | Type | Description |
|-------|------|-------------|
| network | Bridge | An Insteon PLM or Hub that communicates with Insteon devices. |
| legacy_device | Thing | An Insteon or X10 device such as a switch, dimmer, keypad, sensor, etc. |

## Discovery

An Insteon bridge is not automatically discovered and will have to be manually added.
Once configured, depending on the bridge discovery parameters, any Insteon devices or scenes that exists in the modem database and is not currently configured will be automatically be added to the inbox.
For the legacy bridge configuration, only missing device are discovered.
The naming convention for devices is **_Vendor_ _Model_ _Description_** if its product data is retrievable, otherwise **Insteon Device AA.BB.CC**, where `AA.BB.CC` is the Insteon device address.
For scenes, it is **Insteon Scene 42**, where `42` is the scene group number.
The device auto-discovery is enabled by default while disabled for scenes.
X10 devices are not auto discovered.

## Thing Configuration

### Insteon Hub Configuration

The Insteon Hub Legacy is configured with the following parameters:

| Parameter | Default | Required | Description |
|-----------|:-------:|:--------:|-------------|
| hostname | | Yes | Network address of the hub. |
| port | 9761 | No | Network port of the hub. |
| devicePollIntervalInSeconds | 300 | No | Device poll interval in seconds. The hub will be overloaded if interval is too short, leading to sluggish or no response when trying to send messages to devices. The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers. |
| deviceDiscoveryEnabled | true | No | Discover Insteon devices found in the hub database but not configured. |
| sceneDiscoveryEnabled | false | No | Discover Insteon scenes found in the hub database but not configured. |
| deviceSyncEnabled | false | No | Synchronize related devices based on their all-link database. |

### Insteon Hub 2 Configuration

The Insteon Hub 2 is configured with the following parameters:

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


<details>
  <summary>Supported X10 device types</summary>

  | Device Type | Description |
  |-------------|-------------|
  | X10_Switch | X10 Switch |
  | X10_Dimmer | X10 Dimmer |
  | X10_Sensor | X10 Sensor |
</details>

### Legacy Network Configuration

The Insteon PLM or hub is configured with the following parameters:

| Parameter | Default | Required | Description |
|-----------|:-------:|:--------:|-------------|
| port | | Yes | **Examples:**<br>- PLM on Linux: `/dev/ttyS0` or `/dev/ttyUSB0`<br>- Smartenit ZBPLM on Linux: `/dev/ttyUSB0,baudRate=115200`<br>- PLM on Windows: `COM1`<br>- Current hub (2245-222) at 192.168.1.100 on port 25105, with a poll interval of 1000 ms (1 second): `/hub2/my_user_name:my_password@192.168.1.100:25105,poll_time=1000`<br>- Legacy hub (2242-222) at 192.168.1.100 on port 9761:`/hub/192.168.1.100:9761`<br>- Networked PLM using ser2net at 192.168.1.100 on port 9761:`/tcp/192.168.1.100:9761` |
| devicePollIntervalSeconds | 300 | No | Poll interval of devices in seconds. Poll too often and you will overload the insteon network, leading to sluggish or no response when trying to send messages to devices. The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers. |
| additionalDevices | | No | File with additional device types. The syntax of the file is identical to the `device_types.xml` file in the source tree. Please remember to post successfully added device types to the openhab group so the developers can include them into the `device_types.xml` file! |
| additionalFeatures | | No | File with additional feature templates, like in the `device_features.xml` file in the source tree. |

>NOTE: For users upgrading from InsteonPLM, The parameter port_1 is now port.

### Legacy Device Configuration

| Parameter | Required | Description |
|-----------|:--------:|-------------|
| address | Yes | Insteon or X10 address of the device. Insteon device addresses are in the format 'xx.xx.xx', and can be found on the device. X10 device address are in the format 'x.y' and are typically configured on the device. |
| productKey | Yes | Insteon binding product key that is used to identy the device. Every Insteon device type is uniquely identified by its Insteon product key, typically a six digit hex number. For some of the older device types (in particular the SwitchLinc switches and dimmers), Insteon does not give a product key, so an arbitrary fake one of the format Fxx.xx.xx (or Xxx.xx.xx for X10 devices) is assigned by the binding. |
| deviceConfig | No | Optional JSON object with device specific configuration. The JSON object will contain one or more key/value pairs. The key is a parameter for the device and the type of the value will vary. |

<details>
  <summary>Supported product keys</summary>

  | Model | Description | Product Key |
  |-------|-------------|-------------|
  | 2477D | SwitchLinc Dimmer | F00.00.01 |
  | 2477S | SwitchLinc Switch | F00.00.02 |
  | 2845-222 | Hidden Door Sensor | F00.00.03 |
  | 2876S | ICON Switch | F00.00.04 |
  | 2456D3 | LampLinc V2 | F00.00.05 |
  | 2442-222 | Micro Dimmer | F00.00.06 |
  | 2453-222 | DIN Rail On/Off | F00.00.07 |
  | 2452-222 | DIN Rail Dimmer | F00.00.08 |
  | 2458-A1 | MorningLinc RF Lock Controller | F00.00.09 |
  | 2852-222 | Leak Sensor | F00.00.0A |
  | 2672-422 | LED Dimmer | F00.00.0B |
  | 2476D | SwitchLinc Dimmer | F00.00.0C |
  | 2634-222 | On/Off Dual-Band Outdoor Module | F00.00.0D |
  | 2342-2 | Mini Remote | F00.00.10 |
  | 2663-222 | On/Off Outlet | 0x000039 |
  | 2466D | ToggleLinc Dimmer | F00.00.11 |
  | 2466S | ToggleLinc Switch | F00.00.12 |
  | 2672-222 | LED Bulb | F00.00.13 |
  | 2487S | KeypadLinc On/Off 6-Button | F00.00.14 |
  | 2334-232 | KeypadLink Dimmer 6-Button | F00.00.15 |
  | 2334-232 | KeypadLink Dimmer 8-Button | F00.00.16 |
  | 2423A1 | iMeter Solo Power Meter | F00.00.17 |
  | 2423A1 | Thermostat 2441TH | F00.00.18 |
  | 2457D2 | LampLinc Dimmer | F00.00.19 |
  | 2475SDB | In-LineLinc Relay | F00.00.1A |
  | 2635-222 | On/Off Module | F00.00.1B |
  | 2475F | FanLinc Module | F00.00.1C |
  | 2456S3 | ApplianceLinc | F00.00.1D |
  | 2674-222 | LED Bulb (recessed) | F00.00.1E |
  | 2477SA1 | 220V 30-amp Load Controller N/O | F00.00.1F |
  | 2342-222 | Mini Remote (8 Button) | F00.00.20 |
  | 2441V | Insteon Thermostat Adaptor for Venstar | F00.00.21 |
  | 2982-222 | Insteon Smoke Bridge | F00.00.22 |
  | 2487S | KeypadLinc On/Off 8-Button | F00.00.23 |
  | 2450 | IO Link | 0x00001A |
  | 2486D | KeypadLinc Dimmer | 0x000037 |
  | 2484DWH8 | KeypadLinc Countdown Timer | 0x000041 |
  | Various | PLM or Hub | 0x000045 |
  | 2843-222 | Wireless Open/Close Sensor | 0x000049 |
  | 2842-222 | Motion Sensor | 0x00004A |
  | 2844-222 | Motion Sensor II | F00.00.24 |
  | 2486DWH8 | KeypadLinc Dimmer | 0x000051 |
  | 2472D | OutletLincDimmer | 0x000068 |
  | X10 switch | generic X10 switch | X00.00.01 |
  | X10 dimmer | generic X10 dimmer | X00.00.02 |
  | X10 motion | generic X10 motion sensor | X00.00.03 |
</details>

## Channels

Below is the list of possible channels for the Insteon devices.
In order to determine which channels a device supports, check the device in the UI, or use the `insteon device listAll` console command.

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
| imEventButton | Event Button |

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


### Legacy Channels

<details>

  | channel | type | description |
  |---------|------|-------------|
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

</details>

## Full Example

### Things

```java
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

<details>
  <summary>Legacy</summary>

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

</details>

### Items

```java
Switch switch1 { channel="insteon:device:home:243141:switch" }
Dimmer dimmer1 { channel="insteon:device:home:238F55:dimmer" }
Dimmer dimmer2 { channel="insteon:device:home:23B0D9:dimmer" }
Dimmer dimmer3 { channel="insteon:device:home:238FC9:dimmer" }
Dimmer keypad  { channel="insteon:device:home:22F8A8:dimmer" }
Switch keypadA { channel="insteon:device:home:22F8A8:buttonA" }
Switch keypadB { channel="insteon:device:home:22F8A8:buttonB" }
Switch keypadC { channel="insteon:device:home:22F8A8:buttonC" }
Switch keypadD { channel="insteon:device:home:22F8A8:buttonD" }
Switch scene42 { channel="insteon:scene:home:scene42:sceneOnOff" }
Switch switch2 { channel="insteon:x10:home:A2:switch" }
```

## Console Commands

The binding provides commands to help with configuring and troubleshooting.
Most commands support auto-completion during input based on the existing configuration.
If a legacy network bridge is active, the console will revert to legacy commands.
Enter `openhab:insteon` or `insteon` in the console to get a list of available commands.

```shell
openhab> insteon
Usage: openhab:insteon modem - Insteon modem commands
Usage: openhab:insteon device - Insteon/X10 device commands
Usage: openhab:insteon scene - Insteon scene commands
Usage: openhab:insteon channel - Insteon channel commands
Usage: openhab:insteon debug - Insteon debug commands
```

<details>
  <summary>Legacy</summary>

  ```shell
  openhab> insteon
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

</details>

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

Alternatively, the binding can link a device to the modem programmatically using the `insteon modem addDevice` console command.
Based on the initial set button pressed event received, the device will be linked one or both ways.
Once the newly linked device is added as a thing, additional links for more complex devices can be added using the `insteon device addMissingLinks` console command.

## Insteon Devices

Since Insteon devices can have multiple features (for instance a switchable relay and a contact sensor) under a single Insteon device, an openHAB item is not bound to a device, but to a given feature of a device.
For example, the following lines would create two Number items referring to the same thermostat device, but to different features of it:

```java
Number:Temperature  thermostatCoolPoint "cool point [%.1f °F]" { channel="insteon:device:home:32F422:coolSetPoint" }
Number:Temperature  thermostatHeatPoint "heat point [%.1f °F]" { channel="insteon:device:home:32F422:heatSetPoint" }
```

### Switches

The following example shows how to configure a simple light switch (2477S) in the .items file:

```java
Switch officeLight "office light" { channel="insteon:device:home:AABBCC:switch" }
```

### Dimmers

Here is how to configure a simple dimmer (2477D) in the .items file:

```java
Dimmer kitchenChandelier "kitchen chandelier" { channel="insteon:device:home:AABBCC:dimmer" }
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
    // 50% on level at 2.5 minutes ramp rate
    Type dimmer : custom1 [onLevel=50, rampRate=150]
    // 80% on level at device configured ramp rate
    Type dimmer : custom2 [onLevel=80]
    // device configured on level at 8 minutes ramp rate
    Type dimmer : custom3 [rampRate=480]
}
```

<details>
  <summary>Legacy</summary>

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

</details>

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
These groups can be linked programmatically using the `insteon device addMissingLinks` console command, or via the device set buttons (see the keypad instructions).

While previously, keypad buttons required a broadcast group to be configured, the binding now automatically determines that setting, based on the device link databases, deprecating the `group` channel parameter.
By default, the binding will only change the button led state when receiving on/off commands, depending on the keypad local radio group settings.
For button broadcast group support, set the bridge parameter `deviceSyncEnabled` to `true`.
Additionally, for button toggle mode set to always on or off, only `ON` or `OFF` commands will be processed, in line with the physical interaction.

#### Keypad Switches

##### Items

The following items will expose a keypad switch and its associated buttons:

```java
Switch keypadSwitch             "main switch"        { channel="insteon:device:home:AABBCC:switch" }
Switch keypadSwitchA            "button A"           { channel="insteon:device:home:AABBCC:buttonA"}
Switch keypadSwitchB            "button B"           { channel="insteon:device:home:AABBCC:buttonB"}
Switch keypadSwitchC            "button C"           { channel="insteon:device:home:AABBCC:buttonC"}
Switch keypadSwitchD            "button D"           { channel="insteon:device:home:AABBCC:buttonD"}
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

<details>
  <summary>Legacy</summary>

  ##### Items

  Here is a simple example, just using the load (main) switch:

  ```java
  Switch keypadSwitch             "main load"          { channel="insteon:device:home:AABBCC:loadSwitch" }
  Number keypadSwitchManualChange "main manual change" { channel="insteon:device:home:AABBCC:loadSwitchManualChange" }
  Switch keypadSwitchFastOnOff    "main fast on/off"   { channel="insteon:device:home:AABBCC:loadSwitchFastOnOff" }
  Switch keypadSwitchA            "keypad button A"    { channel="insteon:device:home:AABBCC:keypadButtonA"}
  Switch keypadSwitchB            "keypad button B"    { channel="insteon:device:home:AABBCC:keypadButtonB"}
  Switch keypadSwitchC            "keypad button C"    { channel="insteon:device:home:AABBCC:keypadButtonC"}
  Switch keypadSwitchD            "keypad button D"    { channel="insteon:device:home:AABBCC:keypadButtonD"}
  ```

  ##### Things

  The value after group must either be a number or string.
  The hexadecimal value 0xf3 can either converted to a numeric value 243 or the string value "0xf3".

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

</details>

#### Keypad Dimmers

The keypad dimmers are like keypad switches, except that the main load is dimmable.

##### Items

```java
Dimmer keypadDimmer           "main dimmer" { channel="insteon:device:home:AABBCC:dimmer" }
Switch keypadDimmerButtonA    "button A"    { channel="insteon:device:home:AABBCC:buttonA" }
```

##### Sitemap

```perl
Slider item=keypadDimmer label="main" switchSupport
Switch item=keypadDimmerButtonA label="button A"
```

### Outlets

Here's how to configure the top and bottom outlet of the in-wall 2 outlet controller:

```java
Switch outletTop    "Outlet Top"    { channel="insteon:device:home:AABBCC:topOutlet" }
Switch outletBottom "Outlet Bottom" { channel="insteon:device:home:AABBCC:bottomOutlet" }
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
  Channel 'insteon:device:home:miniRemote:eventButtonA' triggered PRESSED_ON
then
  // do something
end
```

### Motion Sensors

Link such that the modem is a responder to the motion sensor.

##### Items

```java
Switch               motionSensor             "motion sensor [MAP(motion.map):%s]"  { channel="insteon:device:home:AABBCC:motion"}
Number:Dimensionless motionSensorBatteryLevel "battery level [%.1f %%]"             { channel="insteon:device:home:AABBCC:batteryLevel" }
Number:Dimensionless motionSensorLightLevel   "light level [%.1f %%]"               { channel="insteon:device:home:AABBCC:lightLevel" }
```

  ```java
  Contact motionSensor             "motion sensor [MAP(motion.map):%s]" { channel="insteon:device:home:AABBCC:contact"}
  Number  motionSensorBatteryLevel "motion sensor battery level"         { channel="insteon:device:home:AABBCC:batteryLevel" }
  Number  motionSensorLightLevel   "motion sensor light level"           { channel="insteon:device:home:AABBCC:lightLevel" }
  ```

</details>

and create a file "motion.map" in the transforms directory with these entries:

```text
ON=detected
OFF=cleared
-=unknown
```

The motion sensor II includes additional channels:

```java
Contact            motionSensorTamperSwitch "tamper switch [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:tamperSwitch" }
Number:Temperature motionSensorTemperature  "temperature [%.1f °F]"               { channel="insteon:device:home:AABBCC:temperature" }
```

The temperature is automatically calculated in Fahrenheit based on the motion sensor II powered source.
Since that sensor might not be calibrated correctly, the output temperature may need to be offset on the openHAB side.

Note that battery and light level are only updated when either there is motion, light level above/below threshold, tamper switch activated, or the sensor battery runs low.

<details>
  <summary>Legacy</summary>

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

</details>

### Hidden Door Sensors

Similar in operation to the motion sensor above.
Link such that the modem is a responder to the motion sensor.

##### Items

```java
Contact              doorSensor             "door sensor [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:contact" }
Number:Dimensionless doorSensorBatteryLevel "battery level [%.1f %%]"           { channel="insteon:device:home:AABBCC:batteryLevel" }
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
Switch doorLock "Front Door [MAP(lock.map):%s]"  { channel="insteon:device:home:AABBCC:lock" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Switch doorLock "Front Door [MAP(lock.map):%s]"  { channel="insteon:device:home:AABBCC:switch" }
  ```

</details>

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
Switch  garageDoorOpener  "door opener"                        { channel="insteon:device:home:AABBCC:switch" }
Contact garageDoorContact "door contact [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:contact" }
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
Dimmer fanLincDimmer "dimmer [%d %%]" { channel="insteon:device:home:AABBCC:dimmer" }
String fanLincFan    "fan speed"      { channel="insteon:device:home:AABBCC:fanSpeed" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Dimmer fanLincDimmer "dimmer [%d %%]" { channel="insteon:device:home:AABBCC:lightDimmer" }
  Number fanLincFan    "fan"            { channel="insteon:device:home:AABBCC:fan"}
  ```

</details>

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
Number:Power  iMeterPower   "power [%d W]"       { channel="insteon:device:home:AABBCC:powerUsage" }
Number:Energy iMeterEnergy  "energy [%.04f kWh]" { channel="insteon:device:home:AABBCC:energyUsage" }
Switch        iMeterReset   "reset"              { channel="insteon:device:home:AABBCC:reset" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Number:Power  iMeterWatts   "iMeter [%d watts]"   { channel="insteon:device:home:AABBCC:watts" }
  Number:Energy iMeterKwh     "iMeter [%.04f kWh]"  { channel="insteon:device:home:AABBCC:kWh" }
  Switch        iMeterUpdate  "iMeter Update"       { channel="insteon:device:home:AABBCC:update" }
  Switch        iMeterReset   "iMeter Reset"        { channel="insteon:device:home:AABBCC:reset" }
  ```

</details>

### Sirens

When turning on the siren directly, the binding will trigger the siren with no delay and up to the maximum duration (~2 minutes).
The channels to change the alarm delay and duration are only for the siren arming behavior.

Here is an example configuration for a siren module:

##### Items

```java
Switch siren                   "siren"                 { channel="insteon:device:home:AABBCC:siren" }
Switch sirenArmed              "armed"                 { channel="insteon:device:home:AABBCC:armed" }
Switch sirenAlarmDelay         "alarm delay"           { channel="insteon:device:home:AABBCC:alarmDelay" }
Number:Time sirenAlarmDuration "alarm duration [%d s]" { channel="insteon:device:home:AABBCC:alarmDuration" }
String sirenAlarmType          "alarm type [%s]"       { channel="insteon:device:home:AABBCC:alarmType" }
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
Switch valve1   "valve 1"   { channel="insteon:device:home:AABBCC:valve1" }
Switch valve2   "valve 2"   { channel="insteon:device:home:AABBCC:valve2" }
Switch valve3   "valve 3"   { channel="insteon:device:home:AABBCC:valve3" }
Switch valve4   "valve 4"   { channel="insteon:device:home:AABBCC:valve4" }
Switch valve5   "valve 5"   { channel="insteon:device:home:AABBCC:valve5" }
Switch valve6   "valve 6"   { channel="insteon:device:home:AABBCC:valve6" }
Switch valve7   "valve 7"   { channel="insteon:device:home:AABBCC:valve7" }
Switch valve8   "valve 8"   { channel="insteon:device:home:AABBCC:valve8" }
Switch pump     "pump"      { channel="insteon:device:home:AABBCC:pump" }
Player program1 "program 1" { channel="insteon:device:home:AABBCC:program1" }
Player program2 "program 2" { channel="insteon:device:home:AABBCC:program2" }
Player program3 "program 3" { channel="insteon:device:home:AABBCC:program3" }
Player program4 "program 4" { channel="insteon:device:home:AABBCC:program4" }
```

### Thermostats

The thermostat (2441TH) is one of the most complex Insteon devices available.
To ensure all links are configured between the modem and device, and the status reporting is enabled, use the `insteon device addMissingLinks` console command.

##### Items

```java
Number:Temperature   thermostatCoolPoint   "cool point [%.1f °F]"  { channel="insteon:device:home:AABBCC:coolSetPoint" }
Number:Temperature   thermostatHeatPoint   "heat point [%.1f °F]"  { channel="insteon:device:home:AABBCC:heatSetPoint" }
String               thermostatSystemMode  "system mode [%s]"      { channel="insteon:device:home:AABBCC:systemMode" }
String               thermostatSystemState "system state [%s]"     { channel="insteon:device:home:AABBCC:systemState" }
String               thermostatFanMode     "fan mode [%s]"         { channel="insteon:device:home:AABBCC:fanMode" }
Number:Temperature   thermostatTemperature "temperature [%.1f °F]" { channel="insteon:device:home:AABBCC:temperature" }
Number:Dimensionless thermostatHumidity    "humidity [%.0f %%]"    { channel="insteon:device:home:AABBCC:humidity" }
```

Add this as well for some more exotic features:

```java
Number:Time          thermostatACDelay      "A/C delay [%d min]"        { channel="insteon:device:home:AABBCC:acDelay" }
Number:Time          thermostatBacklight    "backlight [%d sec]"        { channel="insteon:device:home:AABBCC:backlightDuration" }
Number:Time          thermostatStage1       "A/C stage 1 time [%d min]" { channel="insteon:device:home:AABBCC:stage1Duration" }
Number:Dimensionless thermostatHumidityHigh "humidity high [%d %%]"     { channel="insteon:device:home:AABBCC:humidityHigh" }
Number:Dimensionless thermostatHumidityLow  "humidity low [%d %%]"      { channel="insteon:device:home:AABBCC:humidityLow" }
String               thermostatTempFormat   "temperature format [%s]"   { channel="insteon:device:home:AABBCC:temperatureFormat" }
String               thermostatTimeFormat   "time format [%s]"          { channel="insteon:device:home:AABBCC:timeFormat" }
```

<details>
  <summary>Legacy</summary>

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

</details>

##### Sitemap

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

<details>
  <summary>Legacy</summary>

  ```perl
  Text     item=thermostatTemperature icon="temperature"
  Text     item=thermostatHumidity
  Setpoint item=thermostatCoolPoint icon="temperature" minValue=63 maxValue=90 step=1
  Setpoint item=thermostatHeatPoint icon="temperature" minValue=50 maxValue=80 step=1
  Switch   item=thermostatSystemMode  label="system mode" mappings=[ 0="OFF",  1="HEAT", 2="COOL", 3="AUTO", 4="PROGRAM"]
  Switch   item=thermostatFanMode  label="fan mode" mappings=[ 0="AUTO",  1="ALWAYS ON"]
  Switch   item=thermostatIsHeating  label="is heating" mappings=[ 0="OFF",  1="HEATING"]
  Switch   item=thermostatIsCooling  label="is cooling" mappings=[ 0="OFF",  1="COOLING"]
  Setpoint item=thermostatACDelay  minValue=2 maxValue=20 step=1
  Setpoint item=thermostatBacklight  minValue=0 maxValue=100 step=1
  Setpoint item=thermostatHumidityHigh  minValue=0 maxValue=100 step=1
  Setpoint item=thermostatHumidityLow   minValue=0 maxValue=100 step=1
  Setpoint item=thermostatStage1  minValue=1 maxValue=60 step=1
  ```

</details>

### Window Coverings

Here is an example configuration for a micro open/close module (2444-222) in the .items file:

```java
Rollershutter windowShade "window shade" { channel="insteon:device:home:AABBCC:rollershutter" }
```

Similar to [dimmers](#dimmers), the binding uses the device on level and ramp rate local settings to set the rollershutter level, the same way it would be set when physically interacting with the controller, and can be overridden using the `onLevel` and `rampRate`channel parameters.

## Insteon Scenes

The binding can trigger scenes by commanding the modem to send broadcasts to a given Insteon group.

### Things

```java
Bridge insteon:plm:home [serialPort="/dev/ttyUSB0"] {
  Thing scene scene42 [group=42]
}
```

### Items

```java
Switch sceneOnOff               "scene on/off"        { channel="insteon:scene:home:scene42:sceneOnOff" }
Switch sceneFastOnOff           "scene fast on/off"   { channel="insteon:scene:home:scene42:sceneFastOnOff" }
Rollershutter sceneManualChange "scene manual change" { channel="insteon:scene:home:scene42:sceneManualChange" }
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

<details>
  <summary>Legacy</summary>

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

  Or setting the device configuration parameter with a JSON object with `broadcastGroups` key and the broadcast group array value:

  ```java
  Bridge insteon:network:home [port="/dev/ttyUSB0"] {
    Thing device AABBCC             [address="AA.BB.CC", productKey="0x000045", deviceConfig="{'broadcastGroups': [2]}"]
  }
  ```

  ### Items

  ```java
  Switch  broadcastOnOff "group on/off"  { channel="insteon:device:home:AABBCC:broadcastOnOff#2" }
  ```

  Flipping this switch to "ON" will cause the modem to send a broadcast message with group=2, and all devices that are configured to respond to it should react.

</details>

## X10 Devices

It is worth noting that both the Insteon PLM and the 2014 Hub can both command X10 devices over the powerline, and also set switch stats based on X10 signals received over the powerline.
This allows openHAB not only control X10 devices without the need for other hardware, but it can also have rules that react to incoming X10 powerline commands.

Note that X10 switches/dimmers send no status updates when toggled manually.

### Things

```java
Bridge insteon:plm:home [serialPort="/dev/ttyUSB0"] {
  Thing x10 A2 [houseCode="A", unitCode=2, deviceType="X10_Switch"]
  Thing x10 B4 [houseCode="B", unitCode=4, deviceType="X10_Dimmer"]
  Thing x10 C6 [houseCode="C", unitCode=6, deviceType="X10_Sensor"]
}
```

<details>
  <summary>Legacy</summary>

  ```java
  Bridge insteon:network:home [port="/dev/ttyUSB0"] {
    Thing device A2 [address="A.2", productKey="X00.00.01"]
    Thing device B4 [address="B.4", productKey="X00.00.02"]
    Thing device C6 [address="C.6", productKey="X00.00.03"]
  }
  ```

</details>

### Items

```java
Switch  x10Switch "X10 switch" { channel="insteon:x10:home:A2:switch" }
Dimmer  x10Dimmer "X10 dimmer" { channel="insteon:x10:home:B4:dimmer" }
Contact x10Contact "X10 contact" { channel="insteon:x10:home:C6:contact" }
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
It is important that the heartbeat group (typically 4) is linked properly to the modem by using the `insteon device addMissingLinks` console command.
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

<details>
  <summary>Legacy</summary>

  The `related` channel parameter can be used to have the binding poll a related device whenever a state change occurs for another device.
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

  The binding doesn't know which devices have responded to the message since its a broadcast message.
  The `related` channel parameter can be used to have the binding poll one or more related device when group message are sent.
  More than one device can be polled by separating them with `+` sign.
  A typical example would be a switch configured to broadcast to a group, and one or more devices configured to respond to the message:

  ```java
  Bridge insteon:network:home [port="/dev/ttyUSB0"] {
    Thing device AABBCC [address="AA.BB.CC", productKey="0x000045"] {
      Channels:
        Type broadcastOnOff : broadcastOnOff#3 [related="AA.BB.DD+AA.BB.EE"]
    }
    Thing device AABBDD [address="AA.BB.DD", productKey="F00.00.11"]
    Thing device AABBEE [address="AA.BB.EE", productKey="F00.00.11"]
  }
  ```

</details>

## Triggered Events

In order to monitor if an Insteon device button was directly operated and the type of interaction, triggered event channels can be used.
These channels have the sole purpose to be used in rules in order to set off subsequent actions based on these events.
Below are examples, including all available events, of a dimmer button and a keypad button:

```php
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

## Troubleshooting

Turn on DEBUG or TRACE logging for `org.openhab.binding.insteon`.
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

## Legacy Device Customization

<details>

  ### Adding New Legacy Device Types (Using Existing Device Features)

  Device types are defined in the file `legacy_device_types.xml`, which is inside the Insteon bundle and thus not visible to the user.
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
  Avoid duplicate keys by finding the highest fake product key in the `legacy_device_types.xml` file, and incrementing by one.

  ### Adding New Legacy Device Features

  If you can't build a new device out of the existing device features (for a complete list see `legacy_device_features.xml`) you can add new features by specifying a file (let's call it `my_own_features.xml`) with the "additionalDevices" option in the network config parameters:

  ```text
  additionalFeatures="/usr/local/openhab/rt/my_own_features.xml"
  ```

  In this file you can define your own features (or even overwrite an existing feature.
  In the example below a new feature "MyFeature" is defined, which can then be referenced from the `legacy_device_types.xml` file (or from `my_own_devices.xml`):

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

</details>

## Known Limitations and Issues

- Using the Insteon binding in conjunction with other applications (such as the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) or the Insteon App) can result in some unexpected behavior.
