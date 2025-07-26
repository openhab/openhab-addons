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
Other tools can be used to manage Insteon devices, such as the [Insteon Terminal](https://github.com/pfrommerd/insteon-terminal) open source project, or the [HouseLinc](https://www.insteon.com/houselinc) software from Insteon can also be used for configuration, but it wipes the modem link database clean on its initial use, requiring to re-link the modem to all devices.

At startup, the binding will download the modem database along with each configured device all-link database if not previously downloaded and currently awake.
Therefore, the initialization on the first start may take some additional time to complete depending on the number of devices configured.
The modem and device link databases information is then cached and updated accordingly based on relevant messages the binding receives.
To force a database redownload, use the [openHAB console](#console-commands).

## Important Note for openHAB 4.3.0

The binding has been rewritten to simplify the user experience by retrieving all the configuration directly from the device when possible, and improving the way the Insteon things are configured in MainUI.
If switching from a previous release, you will need to reconfigure your Insteon environment with the new bridges, things and channels to take advantage of these enhancements.
You can follow the [migration guide](#migration-guide).

However, the new version is fully backward compatible by supporting the legacy things.
On the first start, existing `device` things connected to a `network` bridge will be migrated to the `legacy-device` thing type while still keeping the same ids to prevent any breakage.
For textual configuration with defined thing channels, the channel types must be manually updated to the new ones by adding the `legacy` prefix and capitalizing the first letter, as shown in [these examples](#full-example).
It is important to note that once the migration has occurred, downgrading to an older version will not be possible.

## Supported Things

| Thing  | Type   | Description                                                      |
| ------ | ------ | ---------------------------------------------------------------- |
| hub1   | Bridge | An Insteon Hub Legacy that communicates with Insteon devices.    |
| hub2   | Bridge | An Insteon Hub 2 that communicates with Insteon devices.         |
| plm    | Bridge | An Insteon PLM that communicates with Insteon devices.           |
| device | Thing  | An Insteon device such as a switch, dimmer, keypad, sensor, etc. |
| scene  | Thing  | An Insteon scene that controls multiple devices simultaneously.  |
| x10    | Thing  | An X10 device such as a switch, dimmer or sensor.                |

### Legacy Things

| Thing         | Type   | Description                                                             |
| ------------- | ------ | ----------------------------------------------------------------------- |
| network       | Bridge | An Insteon PLM or Hub that communicates with Insteon devices.           |
| legacy-device | Thing  | An Insteon or X10 device such as a switch, dimmer, keypad, sensor, etc. |

## Discovery

An Insteon bridge is not automatically discovered and will have to be manually added.
Once configured, depending on the bridge discovery parameters, any Insteon devices or scenes that exists in the modem database and is not currently configured will be automatically be added to the inbox.
For the legacy bridge configuration, only missing device are discovered.
The naming convention for devices is **_Vendor_ _Model_ _Description_** if its product data is retrievable, otherwise **Insteon Device AA.BB.CC**, where `AA.BB.CC` is the Insteon device address.
For scenes, it is **Insteon Scene 42**, where `42` is the scene group number.
The device auto-discovery is enabled by default while disabled for scenes.
X10 devices are not auto-discovered.

## Thing Configuration

For bridge things, if the poll interval is too short, it will result in sluggish performance and no response when trying to send messages to devices.
The default poll interval of 300 seconds has been tested and found to be a good compromise in a configuration of about 110 switches/dimmers.

### `hub1`

| Parameter                      | Default | Required | Description                                                               |
| ------------------------------ | :-----: | :------: | ------------------------------------------------------------------------- |
| hostname                       |         |   Yes    | Network address of the hub.                                               |
| port                           |  9761   |    No    | Network port of the hub.                                                  |
| devicePollIntervalInSeconds    |   300   |    No    | Device poll interval in seconds.                                          |
| deviceResponseTimeoutInMinutes |   30    |    No    | Device response timeout in minutes before a device is considered offline. |
| deviceDiscoveryEnabled         |  true   |    No    | Discover Insteon devices found in the hub database but not configured.    |
| sceneDiscoveryEnabled          |  false  |    No    | Discover Insteon scenes found in the hub database but not configured.     |
| deviceSyncEnabled              |  false  |    No    | Synchronize related devices based on their all-link database.             |

>NOTE: Use this bridge to connect to a networked PLM via ser2net.

### `hub2`

| Parameter                      | Default | Required | Description                                                               |
| ------------------------------ | :-----: | :------: | ------------------------------------------------------------------------- |
| hostname                       |         |   Yes    | Network address of the hub.                                               |
| port                           |  25105  |    No    | Network port of the hub.                                                  |
| username                       |         |   Yes    | Username to access the hub.                                               |
| password                       |         |   Yes    | Password to access the hub.                                               |
| hubPollIntervalInMilliseconds  |  1000   |    No    | Hub poll interval in milliseconds.                                        |
| devicePollIntervalInSeconds    |   300   |    No    | Device poll interval in seconds.                                          |
| deviceResponseTimeoutInMinutes |   30    |    No    | Device response timeout in minutes before a device is considered offline. |
| deviceDiscoveryEnabled         |  true   |    No    | Discover Insteon devices found in the hub database but not configured.    |
| sceneDiscoveryEnabled          |  false  |    No    | Discover Insteon scenes found in the hub database but not configured.     |
| deviceSyncEnabled              |  false  |    No    | Synchronize related devices based on their all-link database.             |

### `plm`

| Parameter                      | Default | Required | Description                                                               |
| ------------------------------ | :-----: | :------: | ------------------------------------------------------------------------- |
| serialPort                     |         |   Yes    | Serial port connected to the modem. Example: `/dev/ttyS0` or `COM1`       |
| baudRate                       |  19200  |    No    | Serial port baud rate connected to the modem.                             |
| devicePollIntervalInSeconds    |   300   |    No    | Device poll interval in seconds.                                          |
| deviceResponseTimeoutInMinutes |   30    |    No    | Device response timeout in minutes before a device is considered offline. |
| deviceDiscoveryEnabled         |  true   |    No    | Discover Insteon devices found in the modem database but not configured.  |
| sceneDiscoveryEnabled          |  false  |    No    | Discover Insteon scenes found in the modem database but not configured.   |
| deviceSyncEnabled              |  false  |    No    | Synchronize related devices based on their all-link database.             |

### `device`

| Parameter | Required | Description                                                                        |
| --------- | :------: | ---------------------------------------------------------------------------------- |
| address   |   Yes    | Insteon address of the device. It can be found on the device. Example: `12.34.56`. |

The device type is automatically determined by the binding using the device product data.
For a [battery powered device](#battery-powered-devices) that was never configured previously, it may take until the next time that device sends a broadcast message to be modeled properly.
To speed up the process for this case, it is recommended to force the device to become awake after the associated bridge is online by pressing on its SET button.
Likewise, for a wired device that wasn't connected during the first binding initialization, press on its on/off button once powered on to notify the binding that it is available.

### `scene`

| Parameter | Required | Description                                                                                                                |
| --------- | :------: | -------------------------------------------------------------------------------------------------------------------------- |
| group     |   Yes    | Insteon scene group number between 0 and 255. It can be found in the scene detailed information in the Insteon mobile app. |

### `x10`

| Parameter  | Required | Description                                |
| ---------- | :------: | ------------------------------------------ |
| houseCode  |   Yes    | X10 house code of the device. Example: `A` |
| unitCode   |   Yes    | X10 unit code of the device. Example: `1`  |
| deviceType |   Yes    | X10 device type                            |

<details>
  <summary>Supported X10 device types</summary>

  | Device Type | Description |
  | ----------- | ----------- |
  | X10_Switch  | X10 Switch  |
  | X10_Dimmer  | X10 Dimmer  |
  | X10_Sensor  | X10 Sensor  |

</details>

### `network`

| Parameter                 | Default | Required | Description                             |
| ------------------------- | :-----: | :------: | --------------------------------------- |
| port                      |         |   Yes    | Port configuration.                     |
| devicePollIntervalSeconds |   300   |    No    | Poll interval of devices in seconds.    |
| additionalDevices         |         |    No    | File with additional device types.      |
| additionalFeatures        |         |    No    | File with additional feature templates. |

>NOTE: For users upgrading from InsteonPLM, The parameter port_1 is now port.

<details>
  <summary>Port configuration examples</summary>

  | Modem Type            | Port Configuration                                                                                              |
  | --------------------- | --------------------------------------------------------------------------------------------------------------- |
  | Hub (2245-222)        | `/hub2/my_user_name:my_password@192.168.1.100:25105,poll_time=1000`                                             |
  | Legacy Hub (2242-222) | `/hub/192.168.1.100:9761`                                                                                       |
  | PLM                   | `/dev/ttyS0` or `/dev/ttyUSB0` (Linux), `COM1` (Windows), `/tcp/192.168.1.100:9761` (Networked via ser2net) |
  | Smartenit ZBPLM       | `/dev/ttyUSB0,baudRate=115200` (Linux)                                                                          |

</details>

### `legacy-device`

| Parameter    | Required | Description                                                  |
| ------------ | :------: | ------------------------------------------------------------ |
| address      |   Yes    | Device address. Example: `12.34.56` (Insteon) or `A.1` (X10) |
| productKey   |   Yes    | Product key used to identify the model of the device.        |
| deviceConfig |    No    | Optional JSON object with device specific configuration.     |

<details>
  <summary>Supported product keys</summary>

  | Model      | Description                            | Product Key |
  | ---------- | -------------------------------------- | ----------- |
  | 2477D      | SwitchLinc Dimmer                      | F00.00.01   |
  | 2477S      | SwitchLinc Switch                      | F00.00.02   |
  | 2845-222   | Hidden Door Sensor                     | F00.00.03   |
  | 2876S      | ICON Switch                            | F00.00.04   |
  | 2456D3     | LampLinc V2                            | F00.00.05   |
  | 2442-222   | Micro Dimmer                           | F00.00.06   |
  | 2453-222   | DIN Rail On/Off                        | F00.00.07   |
  | 2452-222   | DIN Rail Dimmer                        | F00.00.08   |
  | 2458-A1    | MorningLinc RF Lock Controller         | F00.00.09   |
  | 2852-222   | Leak Sensor                            | F00.00.0A   |
  | 2672-422   | LED Dimmer                             | F00.00.0B   |
  | 2476D      | SwitchLinc Dimmer                      | F00.00.0C   |
  | 2634-222   | On/Off Dual-Band Outdoor Module        | F00.00.0D   |
  | 2342-2     | Mini Remote                            | F00.00.10   |
  | 2663-222   | On/Off Outlet                          | 0x000039    |
  | 2466D      | ToggleLinc Dimmer                      | F00.00.11   |
  | 2466S      | ToggleLinc Switch                      | F00.00.12   |
  | 2672-222   | LED Bulb                               | F00.00.13   |
  | 2487S      | KeypadLinc On/Off 6-Button             | F00.00.14   |
  | 2334-232   | KeypadLink Dimmer 6-Button             | F00.00.15   |
  | 2334-232   | KeypadLink Dimmer 8-Button             | F00.00.16   |
  | 2423A1     | iMeter Solo Power Meter                | F00.00.17   |
  | 2423A1     | Thermostat 2441TH                      | F00.00.18   |
  | 2457D2     | LampLinc Dimmer                        | F00.00.19   |
  | 2475SDB    | In-LineLinc Relay                      | F00.00.1A   |
  | 2635-222   | On/Off Module                          | F00.00.1B   |
  | 2475F      | FanLinc Module                         | F00.00.1C   |
  | 2456S3     | ApplianceLinc                          | F00.00.1D   |
  | 2674-222   | LED Bulb (recessed)                    | F00.00.1E   |
  | 2477SA1    | 220V 30-amp Load Controller N/O        | F00.00.1F   |
  | 2342-222   | Mini Remote (8 Button)                 | F00.00.20   |
  | 2441V      | Insteon Thermostat Adaptor for Venstar | F00.00.21   |
  | 2982-222   | Insteon Smoke Bridge                   | F00.00.22   |
  | 2487S      | KeypadLinc On/Off 8-Button             | F00.00.23   |
  | 2450       | IO Link                                | 0x00001A    |
  | 2486D      | KeypadLinc Dimmer                      | 0x000037    |
  | 2484DWH8   | KeypadLinc Countdown Timer             | 0x000041    |
  | Various    | PLM or Hub                             | 0x000045    |
  | 2843-222   | Wireless Open/Close Sensor             | 0x000049    |
  | 2842-222   | Motion Sensor                          | 0x00004A    |
  | 2844-222   | Motion Sensor II                       | F00.00.24   |
  | 2486DWH8   | KeypadLinc Dimmer                      | 0x000051    |
  | 2472D      | OutletLincDimmer                       | 0x000068    |
  | X10 switch | generic X10 switch                     | X00.00.01   |
  | X10 dimmer | generic X10 dimmer                     | X00.00.02   |
  | X10 motion | generic X10 motion sensor              | X00.00.03   |

</details>

## Channels

Below is the list of possible channels for the Insteon devices.
In order to determine which channels a device supports, check the device in the UI, or use the `insteon device listAll` console command.

### State Channels

| Channel               | Type                 | Access Mode | Description                  |
| --------------------- | -------------------- | :---------: | ---------------------------- |
| 3-way-mode            | Switch               |     R/W     | 3-Way Toggle Mode            |
| ac-delay              | Number:Time          |     R/W     | AC Delay                     |
| alert-delay           | Switch               |     R/W     | Alert Delay                  |
| alert-duration        | Number:Time          |     R/W     | Alert Duration               |
| alert-type            | String               |     R/W     | Alert Type                   |
| armed                 | Switch               |     R/W     | Armed                        |
| backlight-duration    | Number:Time          |     R/W     | Back Light Duration          |
| battery-level         | Number:Dimensionless |      R      | Battery Level                |
| battery-powered       | Switch               |      R      | Battery Powered              |
| beep                  | Switch               |      W      | Beep                         |
| button-a              | Switch               |     R/W     | Button A                     |
| button-b              | Switch               |     R/W     | Button B                     |
| button-c              | Switch               |     R/W     | Button C                     |
| button-d              | Switch               |     R/W     | Button D                     |
| button-e              | Switch               |     R/W     | Button E                     |
| button-f              | Switch               |     R/W     | Button F                     |
| button-g              | Switch               |     R/W     | Button G                     |
| button-h              | Switch               |     R/W     | Button H                     |
| button-beep           | Switch               |     R/W     | Beep on Button Press         |
| button-config         | String               |     R/W     | Button Config                |
| button-lock           | Switch               |     R/W     | Button Lock                  |
| carbon-monoxide-alarm | Switch               |      R      | Carbon Monoxide Alarm        |
| contact               | Contact              |      R      | Contact Sensor               |
| cool-setpoint         | Number:Temperature   |     R/W     | Cool Setpoint                |
| daytime               | Switch               |      R      | Daytime                      |
| dehumidify-setpoint   | Number:Dimensionless |     R/W     | Dehumidify Setpoint          |
| dimmer                | Dimmer               |     R/W     | Dimmer                       |
| energy-offset         | Number:Temperature   |     R/W     | Energy Temperature Offset    |
| energy-reset          | Switch               |      W      | Energy Usage Reset           |
| energy-saving         | Switch               |      R      | Energy Saving Mode           |
| energy-usage          | Number:Energy        |      R      | Energy Usage                 |
| fan-mode              | String               |     R/W     | Fan Mode                     |
| fan-speed             | String               |     R/W     | Fan Speed                    |
| fan-state             | Switch               |      R      | Fan State                    |
| fast-on-off           | Switch               |      W      | Fast On/Off                  |
| heartbeat-interval    | Number:Time          |     R/W     | Heartbeat Interval           |
| heartbeat-on-off      | Switch               |     R/W     | Heartbeat Enabled            |
| heat-setpoint         | Number:Temperature   |     R/W     | Heat Setpoint                |
| humidifier-state      | String               |      R      | Humidifier State             |
| humidify-setpoint     | Number:Dimensionless |     R/W     | Humidify Setpoint            |
| humidity              | Number:Dimensionless |      R      | Ambient Humidity             |
| last-heard-from       | DateTime             |      R      | Last Heard From              |
| leak                  | Switch               |      R      | Leak Sensor                  |
| led-brightness        | Dimmer               |     R/W     | LED Brightness Level         |
| led-on-off            | Switch               |     R/W     | LED Enabled                  |
| led-traffic           | Switch               |     R/W     | LED Traffic Blinking         |
| light-level           | Number:Dimensionless |      R      | Ambient Light Level          |
| load                  | Switch               |      R      | Load Sensor                  |
| load-sense            | Switch               |     R/W     | Load Sense                   |
| load-sense-bottom     | Switch               |     R/W     | Load Sense Bottom Outlet     |
| load-sense-top        | Switch               |     R/W     | Load Sense Top Outlet        |
| lock                  | Switch               |     R/W     | Lock                         |
| low-battery           | Switch               |      R      | Low Battery Alert            |
| malfunction           | Switch               |      R      | Malfunction Alert            |
| manual-change         | Rollershutter        |      W      | Manual Change                |
| momentary-duration    | Number:Time          |     R/W     | Momentary Duration           |
| monitor-mode          | Switch               |     R/W     | Monitor Mode                 |
| motion                | Switch               |      R      | Motion Sensor                |
| on-level              | Dimmer               |     R/W     | On Level                     |
| operation-mode        | String               |     R/W     | Switch Operation Mode        |
| outlet-bottom         | Switch               |     R/W     | Bottom Outlet                |
| outlet-top            | Switch               |     R/W     | Top Outlet                   |
| power-usage           | Number:Power         |      R      | Power Usage                  |
| program1              | Player               |     R/W     | Program 1                    |
| program2              | Player               |     R/W     | Program 2                    |
| program3              | Player               |     R/W     | Program 3                    |
| program4              | Player               |     R/W     | Program 4                    |
| program-lock          | Switch               |     R/W     | Local Programming Lock       |
| pump                  | Switch               |     R/W     | Pump Control                 |
| ramp-rate             | Number:Time          |     R/W     | Ramp Rate                    |
| relay-mode            | String               |     R/W     | Relay Mode                   |
| relay-sensor-follow   | Switch               |     R/W     | Relay Sensor Follow          |
| relay-sensor-inverted | Switch               |     R/W     | Relay Sensor Inverted        |
| resume-dim            | Switch               |     R/W     | Resume Dim Level             |
| reverse-direction     | Switch               |     R/W     | Reverse Motor Direction      |
| rollershutter         | Rollershutter        |     R/W     | Rollershutter                |
| scene                 | Switch               |     R/W     | Scene                        |
| siren                 | Switch               |     R/W     | Siren                        |
| smoke-alarm           | Switch               |      R      | Smoke Alarm                  |
| stage1-duration       | Number:Time          |     R/W     | Stage 1 Duration             |
| stay-awake            | Switch               |     R/W     | Stay Awake for Extended Time |
| switch                | Switch               |     R/W     | Switch                       |
| sync-time             | Switch               |      W      | Synchronize Time             |
| system-mode           | String               |     R/W     | System Mode                  |
| system-state          | String               |      R      | System State                 |
| tamper-switch         | Contact              |      R      | Tamper Switch                |
| temperature           | Number:Temperature   |      R      | Ambient Temperature          |
| temperature-scale     | String               |     R/W     | Temperature Scale            |
| test-alarm            | Switch               |      R      | Test Alarm                   |
| time-format           | String               |     R/W     | Time Format                  |
| toggle-mode-button-a  | String               |     R/W     | Toggle Mode Button A         |
| toggle-mode-button-b  | String               |     R/W     | Toggle Mode Button B         |
| toggle-mode-button-c  | String               |     R/W     | Toggle Mode Button C         |
| toggle-mode-button-d  | String               |     R/W     | Toggle Mode Button D         |
| toggle-mode-button-e  | String               |     R/W     | Toggle Mode Button E         |
| toggle-mode-button-f  | String               |     R/W     | Toggle Mode Button F         |
| toggle-mode-button-g  | String               |     R/W     | Toggle Mode Button G         |
| toggle-mode-button-h  | String               |     R/W     | Toggle Mode Button H         |
| valve1                | Switch               |     R/W     | Valve 1                      |
| valve2                | Switch               |     R/W     | Valve 2                      |
| valve3                | Switch               |     R/W     | Valve 3                      |
| valve4                | Switch               |     R/W     | Valve 4                      |
| valve5                | Switch               |     R/W     | Valve 5                      |
| valve6                | Switch               |     R/W     | Valve 6                      |
| valve7                | Switch               |     R/W     | Valve 7                      |
| valve8                | Switch               |     R/W     | Valve 8                      |

### Trigger Channels

| Channel             | Description         |
| ------------------- | ------------------- |
| event-button        | Event Button        |
| event-button-a      | Event Button A      |
| event-button-b      | Event Button B      |
| event-button-c      | Event Button C      |
| event-button-d      | Event Button D      |
| event-button-e      | Event Button E      |
| event-button-f      | Event Button F      |
| event-button-g      | Event Button G      |
| event-button-h      | Event Button H      |
| event-button-main   | Event Button Main   |
| event-button-bottom | Event Button Bottom |
| event-button-top    | Event Button Top    |
| im-event-button     | Event Button        |
| x10-event1          | X10 Event 1         |
| x10-event2          | X10 Event 2         |
| x10-event3          | X10 Event 3         |
| x10-event4          | X10 Event 4         |
| x10-event5          | X10 Event 5         |
| x10-event6          | X10 Event 6         |
| x10-event7          | X10 Event 7         |
| x10-event8          | X10 Event 8         |
| x10-event9          | X10 Event 9         |
| x10-event10         | X10 Event 10        |
| x10-event11         | X10 Event 11        |
| x10-event12         | X10 Event 12        |
| x10-event13         | X10 Event 13        |
| x10-event14         | X10 Event 14        |
| x10-event15         | X10 Event 15        |
| x10-event16         | X10 Event 16        |

The button events for supported Insteon devices:

| Event                | Description                           |
| -------------------- | ------------------------------------- |
| `PRESSED_ON`         | Button Pressed On (Regular On)        |
| `PRESSED_OFF`        | Button Pressed Off (Regular Off)      |
| `DOUBLE_PRESSED_ON`  | Button Double Pressed On (Fast On)    |
| `DOUBLE_PRESSED_OFF` | Button Double Pressed Off (Fast Off)  |
| `HELD_UP`            | Button Held Up (Manual Change Up)     |
| `HELD_DOWN`          | Button Held Down (Manual Change Down) |
| `RELEASED`           | Button Released (Manual Change Stop)  |

And for Insteon Hubs and PLMs:

| Event      | Description     |
| ---------- | --------------- |
| `PRESSED`  | Button Pressed  |
| `HELD`     | Button Held     |
| `RELEASED` | Button Released |

The events for the Insteon X10 RF Transceiver (EZX10RF):

| Event    | Description |
| -------- | ----------- |
| `ON`     | On          |
| `OFF`    | Off         |
| `BRIGHT` | Bright      |
| `DIM`    | Dim         |

### Legacy Channels

<details>

  | Channel                  | Type                 | Description                       |
  | ------------------------ | -------------------- | --------------------------------- |
  | acDelay                  | Number               | AC Delay                          |
  | backlightDuration        | Number               | Back Light Duration               |
  | batteryLevel             | Number               | Battery Level                     |
  | batteryPercent           | Number:Dimensionless | Battery Percent                   |
  | batteryWatermarkLevel    | Number               | Battery Watermark Level           |
  | beep                     | Switch               | Beep                              |
  | bottomOutlet             | Switch               | Bottom Outlet                     |
  | buttonA                  | Switch               | Button A                          |
  | buttonB                  | Switch               | Button B                          |
  | buttonC                  | Switch               | Button C                          |
  | buttonD                  | Switch               | Button D                          |
  | buttonE                  | Switch               | Button E                          |
  | buttonF                  | Switch               | Button F                          |
  | buttonG                  | Switch               | Button G                          |
  | buttonH                  | Switch               | Button H                          |
  | broadcastOnOff           | Switch               | Broadcast On/Off                  |
  | contact                  | Contact              | Contact                           |
  | coolSetPoint             | Number               | Cool Setpoint                     |
  | dimmer                   | Dimmer               | Dimmer                            |
  | fan                      | Number               | Fan                               |
  | fanMode                  | Number               | Fan Mode                          |
  | fastOnOff                | Switch               | Fast On/Off                       |
  | fastOnOffButtonA         | Switch               | Fast On/Off Button A              |
  | fastOnOffButtonB         | Switch               | Fast On/Off Button B              |
  | fastOnOffButtonC         | Switch               | Fast On/Off Button C              |
  | fastOnOffButtonD         | Switch               | Fast On/Off Button D              |
  | heatSetPoint             | Number               | Heat Setpoint                     |
  | humidity                 | Number               | Humidity                          |
  | humidityHigh             | Number               | Humidity High                     |
  | humidityLow              | Number               | Humidity Low                      |
  | isCooling                | Number               | Is Cooling                        |
  | isHeating                | Number               | Is Heating                        |
  | keypadButtonA            | Switch               | Keypad Button A                   |
  | keypadButtonB            | Switch               | Keypad Button B                   |
  | keypadButtonC            | Switch               | Keypad Button C                   |
  | keypadButtonD            | Switch               | Keypad Button D                   |
  | keypadButtonE            | Switch               | Keypad Button E                   |
  | keypadButtonF            | Switch               | Keypad Button F                   |
  | keypadButtonG            | Switch               | Keypad Button G                   |
  | keypadButtonH            | Switch               | Keypad Button H                   |
  | kWh                      | Number:Energy        | Kilowatt Hour                     |
  | lastHeardFrom            | DateTime             | Last Heard From                   |
  | ledBrightness            | Number               | LED brightness                    |
  | ledOnOff                 | Switch               | LED On/Off                        |
  | lightDimmer              | Dimmer               | light Dimmer                      |
  | lightLevel               | Number               | Light Level                       |
  | lightLevelAboveThreshold | Contact              | Light Level Above/Below Threshold |
  | loadDimmer               | Dimmer               | Load Dimmer                       |
  | loadSwitch               | Switch               | Load Switch                       |
  | loadSwitchFastOnOff      | Switch               | Load Switch Fast On/Off           |
  | loadSwitchManualChange   | Number               | Load Switch Manual Change         |
  | lowBattery               | Contact              | Low Battery                       |
  | manualChange             | Number               | Manual Change                     |
  | manualChangeButtonA      | Number               | Manual Change Button A            |
  | manualChangeButtonB      | Number               | Manual Change Button B            |
  | manualChangeButtonC      | Number               | Manual Change Button C            |
  | manualChangeButtonD      | Number               | Manual Change Button D            |
  | notification             | Number               | Notification                      |
  | onLevel                  | Number               | On Level                          |
  | rampDimmer               | Dimmer               | Ramp Dimmer                       |
  | rampRate                 | Number               | Ramp Rate                         |
  | reset                    | Switch               | Reset                             |
  | stage1Duration           | Number               | Stage 1 Duration                  |
  | switch                   | Switch               | Switch                            |
  | systemMode               | Number               | System Mode                       |
  | tamperSwitch             | Contact              | Tamper Switch                     |
  | temperature              | Number:Temperature   | Temperature                       |
  | temperatureLevel         | Number               | Temperature Level                 |
  | topOutlet                | Switch               | Top Outlet                        |
  | update                   | Switch               | Update                            |
  | watts                    | Number:Power         | Watts                             |

</details>

## Full Example

### Things

```java
Bridge insteon:plm:home [serialPort="/dev/ttyUSB0"] {
  Thing device 22f8a8 [address="22.F8.A8"]
  Thing device 238d93 [address="23.8D.93"]
  Thing device 238f55 [address="23.8F.55"]
  Thing device 238fc9 [address="23.8F.C9"]
  Thing device 23b0d9 [address="23.B0.D9"]
  Thing scene scene42 [group=42]
  Thing x10 a2 [houseCode="A", unitCode=2, deviceType="X10_Switch"]
}
```

<details>
  <summary>Legacy</summary>

  ```java
  Bridge insteon:network:home [port="/dev/ttyUSB0"] {
    Thing device 22F8A8 [address="22.F8.A8", productKey="F00.00.15"] {
      Channels:
        Type legacyKeypadButtonA : keypadButtonA [ group=3 ]
        Type legacyKeypadButtonB : keypadButtonB [ group=4 ]
        Type legacyKeypadButtonC : keypadButtonC [ group=5 ]
        Type legacyKeypadButtonD : keypadButtonD [ group=6 ]
    }
    Thing device 238D93 [address="23.8D.93", productKey="F00.00.12"]
    Thing device 238F55 [address="23.8F.55", productKey="F00.00.11"] {
      Channels:
        Type legacyDimmer        : dimmer [related="23.B0.D9+23.8F.C9"]
    }
    Thing device 238FC9 [address="23.8F.C9", productKey="F00.00.11"] {
      Channels:
        Type legacyDimmer        : dimmer [related="23.8F.55+23.B0.D9"]
    }
    Thing device 23B0D9 [address="23.B0.D9", productKey="F00.00.11"] {
      Channels:
        Type legacyDimmer        : dimmer [related="23.8F.55+23.8F.C9"]
    }
    Thing device 243141 [address="24.31.41", productKey="F00.00.11"]  {
      Channels:
        Type legacyDimmer        : dimmer [dimmermax=60]
    }
  }
  ```

</details>

### Items

```java
Switch switch1 { channel="insteon:device:home:243141:switch" }
Dimmer dimmer1 { channel="insteon:device:home:238f55:dimmer" }
Dimmer dimmer2 { channel="insteon:device:home:23b0d9:dimmer" }
Dimmer dimmer3 { channel="insteon:device:home:238fc9:dimmer" }
Dimmer keypad  { channel="insteon:device:home:22f8a8:dimmer" }
Switch keypadA { channel="insteon:device:home:22f8a8:button-a" }
Switch keypadB { channel="insteon:device:home:22f8a8:button-b" }
Switch keypadC { channel="insteon:device:home:22f8a8:button-c" }
Switch keypadD { channel="insteon:device:home:22f8a8:button-d" }
Switch scene42 { channel="insteon:scene:home:scene42:scene" }
Switch switch2 { channel="insteon:x10:home:a2:switch" }
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

### Modem Commands

```shell
openhab> insteon modem
Usage: openhab:insteon modem listAll - list configured Insteon modem bridges with related channels and status
Usage: openhab:insteon modem listDatabase [--records] - list all-link database summary or records and pending changes for the Insteon modem
Usage: openhab:insteon modem listFeatures - list features for the Insteon modem
Usage: openhab:insteon modem listProductData - list product data for the Insteon modem
Usage: openhab:insteon modem reloadDatabase - reload all-link database from the Insteon modem
Usage: openhab:insteon modem backupDatabase - backup all-link database from the Insteon modem to a file
Usage: openhab:insteon modem restoreDatabase <filename> --confirm - restore all-link database to the Insteon modem from a specific file
Usage: openhab:insteon modem addDatabaseController <address> <group> [<devCat> <subCat> <firmware>] - add a controller record to all-link database for the Insteon modem
Usage: openhab:insteon modem addDatabaseResponder <address> <group> - add a responder record to all-link database for the Insteon modem
Usage: openhab:insteon modem deleteDatabaseRecord <address> <group> - delete a controller/responder record from all-link database for the Insteon modem
Usage: openhab:insteon modem applyDatabaseChanges --confirm - apply all-link database pending changes for the Insteon modem
Usage: openhab:insteon modem clearDatabaseChanges - clear all-link database pending changes for the Insteon modem
Usage: openhab:insteon modem addDevice [<address>] - add an Insteon device to the modem, optionally providing its address
Usage: openhab:insteon modem removeDevice <address> [--force] - remove an Insteon device from the modem
Usage: openhab:insteon modem reset --confirm - reset the Insteon modem to factory defaults
Usage: openhab:insteon modem switch <thingId> - switch Insteon modem bridge to use if more than one configured and enabled
```

### Device Commands

```shell
openhab> insteon device
Usage: openhab:insteon device listAll - list configured Insteon/X10 devices with related channels and status
Usage: openhab:insteon device listDatabase <thingId> - list all-link database records and pending changes for a configured Insteon device
Usage: openhab:insteon device listFeatures <thingId> - list features for a configured Insteon/X10 device
Usage: openhab:insteon device listProductData <thingId> - list product data for a configured Insteon/X10 device
Usage: openhab:insteon device listMissingLinks --all|<thingId> - list missing links for a specific or all configured Insteon devices
Usage: openhab:insteon device addMissingLinks --all|<thingId> - add missing links for a specific or all configured Insteon devices
Usage: openhab:insteon device addDatabaseController <thingId> <address> <group> <data1> <data2> <data3> - add a controller record to all-link database for a configured Insteon device
Usage: openhab:insteon device addDatabaseResponder <thingId> <address> <group> <data1> <data2> <data3> - add a responder record to all-link database for a configured Insteon device
Usage: openhab:insteon device deleteDatabaseController <thingId> <address> <group> <data3> - delete a controller record from all-link database for a configured Insteon device
Usage: openhab:insteon device deleteDatabaseResponder <thingId> <address> <group> <data3> - delete a responder record from all-link database for a configured Insteon device
Usage: openhab:insteon device applyDatabaseChanges <thingId> --confirm - apply all-link database pending changes for a configured Insteon device
Usage: openhab:insteon device clearDatabaseChanges <thingId> - clear all-link database pending changes for a configured Insteon device
Usage: openhab:insteon device setButtonRadioGroup <thingId> <button1> <button2> [<button3> ... <button7>] - set a button radio group for a configured Insteon KeypadLinc device
Usage: openhab:insteon device clearButtonRadioGroup <thingId> <button1> <button2> [<button3> ... <button7>] - clear a button radio group for a configured Insteon KeypadLinc device
Usage: openhab:insteon device refresh --all|<thingId> - refresh data for a specific or all configured Insteon devices
```

### Scene Commands

```shell
openhab> insteon scene
Usage: openhab:insteon scene listAll - list configured Insteon scenes with related channels and status
Usage: openhab:insteon scene listDetails <thingId> - list details for a configured Insteon scene
Usage: openhab:insteon scene addDevice --new|<scene> <device> <feature> <onLevel> [<rampRate>] - add an Insteon device feature to a new or configured Insteon scene
Usage: openhab:insteon scene removeDevice <scene> <device> <feature> - remove an Insteon device feature from a configured Insteon scene
```

### Channel Commands

```shell
openhab> insteon channel
Usage: openhab:insteon channel listAll - list available channel ids with configuration and link state
```

### Debug Commands

```shell
openhab> insteon debug
Usage: openhab:insteon debug listMonitored - list monitored Insteon/X10 device(s)
Usage: openhab:insteon debug startMonitoring --all|<address> - start logging message events for Insteon/X10 device(s) in separate file(s)
Usage: openhab:insteon debug stopMonitoring --all|<address> - stop logging message events for Insteon/X10 device(s) in separate file(s)
Usage: openhab:insteon debug sendBroadcastMessage <group> <cmd1> <cmd2> - send an Insteon broadcast message to a group
Usage: openhab:insteon debug sendStandardMessage <address> <cmd1> <cmd2> - send an Insteon standard message to a device
Usage: openhab:insteon debug sendExtendedMessage <address> <cmd1> <cmd2> [<data1> ... <data13>] - send an Insteon extended message with standard crc to a device
Usage: openhab:insteon debug sendExtended2Message <address> <cmd1> <cmd2> [<data1> ... <data12>] - send an Insteon extended message with a two-byte crc to a device
Usage: openhab:insteon debug sendX10Message <address> <cmd> - send an X10 message to a device
Usage: openhab:insteon debug sendIMMessage <name> [<data1> <data2> ...] - send an IM message to the modem
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
Whenever the bridge related device synchronization parameter `deviceSyncEnabled` is set to `true`, broadcast messages for supported Insteon commands (e.g. on/off, bright/dim, manual change) are sent to all responders of a given group, updating all related devices in one request.
If no broadcast group is determined or for Insteon commands that don't support broadcasting (e.g. percent), direct messages are sent to each related device instead, to adjust their level based on their all-link database.

## Insteon Linking Process

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
Number:Temperature  thermostatCoolSetpoint "cool setpoint [%.1f °F]" { channel="insteon:device:home:32f422:cool-setpoint" }
Number:Temperature  thermostatHeatSetpoint "heat setpoint [%.1f °F]" { channel="insteon:device:home:32f422:heat-setpoint" }
```

### Switches

The following example shows how to configure a simple light switch (2477S) in the .items file:

```java
Switch officeLight "office light" { channel="insteon:device:home:aabbcc:switch" }
```

### Dimmers

Here is how to configure a simple dimmer (2477D) in the .items file:

```java
Dimmer kitchenChandelier "kitchen chandelier" { channel="insteon:device:home:aabbcc:dimmer" }
```

For `ON` command requests, the binding uses the device on level and ramp rate local settings to set the dimmer level, the same way it would be set when physically pressing on the dimmer.
These settings can be controlled using the `on-level` and `ramp-rate` channels.

Alternatively, these settings can be overridden using the `dimmer` channel parameters `onLevel` and `rampRate`.
Doing so will result in different type of commands being triggered as opposed to having separate channels previously such as `fastOnOff`, `manualChange` and `rampDimmer` handling it.

When the `rampRate` parameter is configured, the binding will send a ramp rate command (previously triggered by the `rampDimmer` channel) to the relevant device to set the level at the defined ramp rate.
When this parameter is set to instant (0.1 sec), on/off commands will trigger what used to be handled by the `fastOnOff` channel.
And percent commands will trigger what is defined in the Insteon protocol as instant change requests.

As far as the previously known `manualChange` channel, it has been rolled into the `rollershutter` channel for [window covering](#window-coverings) using `UP`, `DOWN` and `STOP` commands.
For the `dimmer` channel, the `INCREASE` and `DECREASE` commands can be used instead.

Ultimately, the `dimmer` channel parameters can be used to create custom channels via a thing file that can work as an alternative to having to configure an Insteon scene for a single device.

```java
Thing device 23b0d9 [address="23.B0.D9"] {
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
        Type legacyDimmer     : dimmer [dimmermax=70]
    }
    Thing device AABBCD [address="AA.BB.CD", productKey="F00.00.15"]  {
      Channels:
        Type legacyLoadDimmer : loadDimmer [dimmermax=60]
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
| :---: | :-----------: | :---------: |
| 0x01  |       1       |   (Load)    |
| 0x03  |       3       |      A      |
| 0x04  |       4       |      B      |
| 0x05  |       5       |      C      |
| 0x06  |       6       |      D      |

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
Switch keypadSwitch             "main switch"        { channel="insteon:device:home:aabbcc:switch" }
Switch keypadSwitchA            "button A"           { channel="insteon:device:home:aabbcc:button-a"}
Switch keypadSwitchB            "button B"           { channel="insteon:device:home:aabbcc:button-b"}
Switch keypadSwitchC            "button C"           { channel="insteon:device:home:aabbcc:button-c"}
Switch keypadSwitchD            "button D"           { channel="insteon:device:home:aabbcc:button-d"}
```

<details>
  <summary>Legacy</summary>

  ```java
  Switch keypadSwitch             "main switch"        { channel="insteon:device:home:AABBCC:switch" }
  Switch keypadSwitchA            "button A"           { channel="insteon:device:home:AABBCC:buttonA"}
  Switch keypadSwitchB            "button B"           { channel="insteon:device:home:AABBCC:buttonB"}
  Switch keypadSwitchC            "button C"           { channel="insteon:device:home:AABBCC:buttonC"}
  Switch keypadSwitchD            "button D"           { channel="insteon:device:home:AABBCC:buttonD"}
  ```

</details>

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

```java
rule "Main Button Off Event"
when
  Channel 'insteon:device:home:aabbcc:event-button-main' triggered PRESSED_OFF
then
  // do something
end

rule "Main Button Fast On/Off Events"
when
  Channel 'insteon:device:home:aabbcc:event-button-main' triggered DOUBLE_PRESSED_ON or
  Channel 'insteon:device:home:aabbcc:event-button-main' triggered DOUBLE_PRESSED_OFF
then
  // do something
end

rule "Main Button Manual Change Stop Event"
when
  Channel 'insteon:device:home:aabbcc:event-button-main' triggered RELEASED
then
  // do something
end

rule "Keypad Button A On Event"
when
  Channel 'insteon:device:home:aabbcc:event-button-a' triggered PRESSED_ON
then
  // do something
end
```

<details>
  <summary>Legacy</summary>

##### Legacy Items

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

##### Legacy Things

  The value after group must either be a number or string.
  The hexadecimal value 0xf3 can either converted to a numeric value 243 or the string value "0xf3".

  ```java
  Bridge insteon:network:home [port="/dev/ttyUSB0"] {
    Thing device AABBCC [address="AA.BB.CC", productKey="F00.00.15"] {
      Channels:
        Type legacyKeypadButtonA : keypadButtonA [ group="0xf3" ]
        Type legacyKeypadButtonB : keypadButtonB [ group="0xf4" ]
        Type legacyKeypadButtonC : keypadButtonC [ group="0xf5" ]
        Type legacyKeypadButtonD : keypadButtonD [ group="0xf6" ]
    }
  }
  ```

##### Legacy Sitemap

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
Dimmer keypadDimmer           "main dimmer" { channel="insteon:device:home:aabbcc:dimmer" }
Switch keypadDimmerButtonA    "button A"    { channel="insteon:device:home:aabbcc:button-a" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Dimmer keypadDimmer           "main dimmer" { channel="insteon:device:home:AABBCC:dimmer" }
  Switch keypadDimmerButtonA    "button A"    { channel="insteon:device:home:AABBCC:buttonA" }
  ```

</details>

##### Sitemap

```perl
Slider item=keypadDimmer label="main" switchSupport
Switch item=keypadDimmerButtonA label="button A"
```

### Outlets

Here's how to configure the top and bottom outlet of the in-wall 2 outlet controller:

```java
Switch outletTop    "Outlet Top"    { channel="insteon:device:home:aabbcc:outlet-top" }
Switch outletBottom "Outlet Bottom" { channel="insteon:device:home:aabbcc:outlet-bottom" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Switch outletTop    "Outlet Top"    { channel="insteon:device:home:AABBCC:topOutlet" }
  Switch outletBottom "Outlet Bottom" { channel="insteon:device:home:AABBCC:bottomOutlet" }
  ```

</details>

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

#### Rules

```java
rule "Mini Remote Button A Pressed On"
when
  Channel 'insteon:device:home:mini-remote:event-button-a' triggered PRESSED_ON
then
  // do something
end
```

### Motion Sensors

Link such that the modem is a responder to the motion sensor.

#### Items

```java
Switch               motionSensor             "motion sensor [MAP(motion.map):%s]" { channel="insteon:device:home:aabbcc:motion"}
Number:Dimensionless motionSensorBatteryLevel "battery level [%.1f %%]"            { channel="insteon:device:home:aabbcc:battery-level" }
Number:Dimensionless motionSensorLightLevel   "light level [%.1f %%]"              { channel="insteon:device:home:aabbcc:light-level" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Contact motionSensor             "motion sensor [MAP(motion.map):%s]" { channel="insteon:device:home:AABBCC:contact"}
  Number  motionSensorBatteryLevel "motion sensor battery level"        { channel="insteon:device:home:AABBCC:batteryLevel" }
  Number  motionSensorLightLevel   "motion sensor light level"          { channel="insteon:device:home:AABBCC:lightLevel" }
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
Contact            motionSensorTamperSwitch "tamper switch [MAP(contact.map):%s]" { channel="insteon:device:home:aabbcc:tamper-switch" }
Number:Temperature motionSensorTemperature  "temperature [%.1f °F]"               { channel="insteon:device:home:aabbcc:temperature" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Contact            motionSensorTamperSwitch "tamper switch [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:tamperSwitch" }
  Number:Temperature motionSensorTemperature  "temperature [%.1f °F]"               { channel="insteon:device:home:AABBCC:temperature" }
  ```

</details>

The temperature is automatically calculated in Fahrenheit based on the motion sensor II powered source.
Since that sensor might not be calibrated correctly, the output temperature may need to be offset on the openHAB side.

The battery and light level are only updated when either there is motion, light level above/below threshold, tamper switch activated, or the sensor battery runs low.

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

#### Items

```java
Contact              doorSensor             "door sensor [MAP(contact.map):%s]" { channel="insteon:device:home:aabbcc:contact" }
Number:Dimensionless doorSensorBatteryLevel "battery level [%.1f %%]"           { channel="insteon:device:home:aabbcc:battery-level" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Contact              doorSensor             "door sensor [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:contact" }
  Number:Dimensionless doorSensorBatteryLevel "battery level [%.1f %%]"           { channel="insteon:device:home:AABBCC:batteryLevel" }
  ```

</details>

and create a file "contact.map" in the transforms directory with these entries:

```text
OPEN=open
CLOSED=closed
-=unknown
```

Note that battery level is only updated when the sensor is triggered or through its daily heartbeat.

### Locks

It is important to sync with the lock controller within 5 feet to avoid poor connection and link twice for both ON and OFF functionality.

#### Items

```java
Switch doorLock "Front Door [MAP(lock.map):%s]"  { channel="insteon:device:home:aabbcc:lock" }
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

The I/O Linc devices are really two devices in one: an output relay and an input contact sensor.
To control the relay, link the modem as a controller using the set buttons as described in the instructions.
To get the state of the relay and sensor, the modem must also be linked as a responder to the I/O Linc.
The contact state is based on the sensor state at the time it is linked.
To invert the state, either relink the modem as a responder with the sensor state inverted, or toggle the channel `relay-sensor-inverted`.
By default, the device is inverted where an on command is sent when the sensor is closed, and off when open.
For a garage door opener, ensure the input sensor is closed (status LED off) during the linking process.

#### Items

```java
Switch  garageDoorOpener                 "door opener"                        { channel="insteon:device:home:aabbcc:switch" }
Contact garageDoorContact                "door contact [MAP(contact.map):%s]" { channel="insteon:device:home:aabbcc:contact" }
String  garageDoorRelayMode              "door relay mode"                    { channel="insteon:device:home:aabbcc:relay-mode" }
Switch  garageDoorRelaySensorInverted    "door relay sensor inverted"         { channel="insteon:device:home:aabbcc:relay-sensor-inverted" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Switch  garageDoorOpener  "door opener"                        { channel="insteon:device:home:AABBCC:switch" }
  Contact garageDoorContact "door contact [MAP(contact.map):%s]" { channel="insteon:device:home:AABBCC:contact" }
  ```

</details>

and create a file "contact.map" in the transforms directory with these entries:

```text
OPEN=open
CLOSED=closed
-=unknown
```

### Fan Controllers

Here is an example configuration for a FanLinc module, which has a dimmable light and a variable speed fan:

#### Items

```java
Dimmer fanLincDimmer "dimmer [%d %%]" { channel="insteon:device:home:aabbcc:dimmer" }
String fanLincFan    "fan speed"      { channel="insteon:device:home:aabbcc:fan-speed" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Dimmer fanLincDimmer "dimmer [%d %%]" { channel="insteon:device:home:AABBCC:lightDimmer" }
  Number fanLincFan    "fan"            { channel="insteon:device:home:AABBCC:fan"}
  ```

</details>

#### Sitemap

```perl
Slider item=fanLincDimmer switchSupport
Switch item=fanLincFan mappings=[ OFF="OFF", LOW="LOW", MEDIUM="MEDIUM", HIGH="HIGH" ]
```

### Power Meters

The iMeter Solo reports both energy and power usage, and is updated during the normal polling process of the devices.
Send a `REFRESH` command to force update the current values for the device.
Additionally, the device can be reset.

See the example below:

#### Items

```java
Number:Power  iMeterPower   "power [%d W]"       { channel="insteon:device:home:aabbcc:power-usage" }
Number:Energy iMeterEnergy  "energy [%.04f kWh]" { channel="insteon:device:home:aabbcc:energy-usage" }
Switch        iMeterReset   "reset"              { channel="insteon:device:home:aabbcc:reset" }
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
The channels to change the alert delay and duration are only used for the siren arming behavior.

Here is an example configuration for a siren module:

#### Items

```java
Switch siren                   "siren"                 { channel="insteon:device:home:aabbcc:siren" }
Switch sirenArmed              "armed"                 { channel="insteon:device:home:aabbcc:armed" }
Switch sirenAlertDelay         "alert delay"           { channel="insteon:device:home:aabbcc:alert-delay" }
Number:Time sirenAlertDuration "alert duration [%d s]" { channel="insteon:device:home:aabbcc:alert-duration" }
String sirenAlertType          "alert type [%s]"       { channel="insteon:device:home:aabbcc:alert-type" }
```

#### Sitemap

```perl
Switch   item=siren
Text     item=sirenArmed
Switch   item=sirenAlertDelay
Setpoint item=sirenAlertDuration minValue=0 maxValue=127 step=1
Switch   item=sirenAlertType mappings=[ CHIME="CHIME", LOUD_SIREN="LOUD SIREN" ]
```

### Smoke Detectors

The smoke bridge monitors First Alert ONELINK smoke and carbon monoxide detectors.

Here is an example configuration for a smoke bridge:

#### Items

```java
Switch smokeAlarm          "smoke alarm"           { channel="insteon:device:home:aabbcc:smoke-alarm" }
Switch carbonMonoxideAlarm "carbon monoxide alarm" { channel="insteon:device:home:aabbcc:carbon-monoxide-alarm" }
Switch lowBattery          "low battery"           { channel="insteon:device:home:aabbcc:low-battery" }
```

### Sprinklers

The EZRain device controls up to 8 sprinkler valves and 4 programs.
It can also enable pump control on the 8th valve.
Only one sprinkler valve can be on at the time.
When pump control is enabled, the 8th valve will remain on and cannot be controlled at the valve level.
Each sprinkler program can be turned on/off by using `PLAY` and `PAUSE` commands.
To skip forward or back to the next or previous valve in the program, use `NEXT` and `PREVIOUS` commands.

#### Items

```java
Switch valve1   "valve 1"   { channel="insteon:device:home:aabbcc:valve1" }
Switch valve2   "valve 2"   { channel="insteon:device:home:aabbcc:valve2" }
Switch valve3   "valve 3"   { channel="insteon:device:home:aabbcc:valve3" }
Switch valve4   "valve 4"   { channel="insteon:device:home:aabbcc:valve4" }
Switch valve5   "valve 5"   { channel="insteon:device:home:aabbcc:valve5" }
Switch valve6   "valve 6"   { channel="insteon:device:home:aabbcc:valve6" }
Switch valve7   "valve 7"   { channel="insteon:device:home:aabbcc:valve7" }
Switch valve8   "valve 8"   { channel="insteon:device:home:aabbcc:valve8" }
Switch pump     "pump"      { channel="insteon:device:home:aabbcc:pump" }
Player program1 "program 1" { channel="insteon:device:home:aabbcc:program1" }
Player program2 "program 2" { channel="insteon:device:home:aabbcc:program2" }
Player program3 "program 3" { channel="insteon:device:home:aabbcc:program3" }
Player program4 "program 4" { channel="insteon:device:home:aabbcc:program4" }
```

### Thermostats

The thermostat (2441TH) is one of the most complex Insteon devices available.
To ensure all links are configured between the modem and device, and the status reporting is enabled, use the `insteon device addMissingLinks` console command.

#### Items

```java
Number:Temperature   thermostatCoolSetpoint "cool setpoint [%.1f °F]" { channel="insteon:device:home:aabbcc:cool-setpoint" }
Number:Temperature   thermostatHeatSetpoint "heat setpoint [%.1f °F]" { channel="insteon:device:home:aabbcc:heat-setpoint" }
String               thermostatSystemMode   "system mode [%s]"        { channel="insteon:device:home:aabbcc:system-mode" }
String               thermostatSystemState  "system state [%s]"       { channel="insteon:device:home:aabbcc:system-state" }
String               thermostatFanMode      "fan mode [%s]"           { channel="insteon:device:home:aabbcc:fan-mode" }
Number:Temperature   thermostatTemperature  "temperature [%.1f °F]"   { channel="insteon:device:home:aabbcc:temperature" }
Number:Dimensionless thermostatHumidity     "humidity [%.0f %%]"      { channel="insteon:device:home:aabbcc:humidity" }
```

Add this as well for some more exotic features:

```java
Number:Time          thermostatACDelay            "A/C delay [%d min]"          { channel="insteon:device:home:aabbcc:ac-delay" }
Number:Time          thermostatBacklight          "backlight [%d sec]"          { channel="insteon:device:home:aabbcc:backlight-duration" }
Number:Time          thermostatStage1             "A/C stage 1 time [%d min]"   { channel="insteon:device:home:aabbcc:stage1-duration" }
Number:Dimensionless thermostatDehumidifySetpoint "dehumidify setpoint [%d %%]" { channel="insteon:device:home:aabbcc:dehumidify-setpoint" }
Number:Dimensionless thermostatHumidifySetpoint   "humidify setpoint [%d %%]"   { channel="insteon:device:home:aabbcc:humidify-setpoint" }
String               thermostatTemperatureScale   "temperature scale [%s]"      { channel="insteon:device:home:aabbcc:temperature-scale" }
String               thermostatTimeFormat         "time format [%s]"            { channel="insteon:device:home:aabbcc:time=format" }
```

<details>
  <summary>Legacy</summary>

  ```java
  Number              thermostatCoolPoint   "cool point [%.1f °F]"      { channel="insteon:device:home:AABBCC:coolSetPoint" }
  Number              thermostatHeatPoint   "heat point [%.1f °F]"      { channel="insteon:device:home:AABBCC:heatSetPoint" }
  Number              thermostatSystemMode  "system mode [%d]"          { channel="insteon:device:home:AABBCC:systemMode" }
  Number              thermostatFanMode     "fan mode [%d]"             { channel="insteon:device:home:AABBCC:fanMode" }
  Number              thermostatIsHeating   "is heating [%d]"           { channel="insteon:device:home:AABBCC:isHeating"}
  Number              thermostatIsCooling   "is cooling [%d]"           { channel="insteon:device:home:AABBCC:isCooling" }
  Number:Temperature  thermostatTemperature "temperature [%.1f %unit%]" { channel="insteon:device:home:AABBCC:temperature" }
  Number              thermostatHumidity    "humidity [%.0f %%]"        { channel="insteon:device:home:AABBCC:humidity" }
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

#### Sitemap

For the thermostat to display in the GUI, add this to the sitemap file:

```perl
Text     item=thermostatTemperature icon="temperature"
Text     item=thermostatHumidity
Setpoint item=thermostatCoolPoint icon="temperature" minValue=63 maxValue=90 step=1
Setpoint item=thermostatHeatPoint icon="temperature" minValue=50 maxValue=80 step=1
Switch   item=thermostatSystemMode mappings=[ OFF="OFF", HEAT="HEAT", COOL="COOL", AUTO="AUTO", PROGRAM="PROGRAM" ]
Text     item=thermostatSystemState
Switch   item=thermostatFanMode mappings=[ AUTO="AUTO", ALWAYS_ON="ALWAYS ON" ]
Setpoint item=thermostatACDelay minValue=2 maxValue=20 step=1
Setpoint item=thermostatBacklight minValue=0 maxValue=100 step=1
Setpoint item=thermostatDehumidifySetpoint minValue=20 maxValue=90 step=1
Setpoint item=thermostatHumidifySetpoint  minValue=0 maxValue=79 step=1
Setpoint item=thermostatStage1 minValue=1 maxValue=60 step=1
Switch   item=thermostatTemperatureScale mappings=[ CELSIUS="CELSIUS", FAHRENHEIT="FAHRENHEIT" ]
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
Rollershutter windowShade "window shade" { channel="insteon:device:home:aabbcc:rollershutter" }
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
Switch scene                    "scene"         { channel="insteon:scene:home:scene42:scene" }
Switch sceneFastOnOff           "fast on/off"   { channel="insteon:scene:home:scene42:fast-on-off" }
Rollershutter sceneManualChange "manual change" { channel="insteon:scene:home:scene42:manual-change" }
```

### Sitemap

```perl
Switch item=scene
Switch item=sceneFastOnOff mappings=[ ON="ON", OFF="OFF" ]
Switch item=sceneManualChange mappings=[ UP="UP", DOWN="DOWN", STOP="STOP" ]
```

Sending `ON` command to `scene` will cause the modem to send a broadcast message to group 42, and all devices that are configured to respond to it should react.
The current state of a scene is published on the `scene` channel.
An `ON` state indicates that all the device states associated to a scene are matching their configured link on level.

<details>
  <summary>Legacy</summary>

  The binding can command the modem to send broadcasts to a given Insteon group.
  Since it is a broadcast message, the corresponding item does _not_ take the address of any device, but of the modem itself.
  The format is `broadcastOnOff#X` where X is the group that you want to be able to broadcast messages to:

### Legacy Things

  ```java
  Bridge insteon:network:home [port="/dev/ttyUSB0"] {
    Thing device AABBCC             [address="AA.BB.CC", productKey="0x000045"] {
      Channels:
        Type legacyBroadcastOnOff : broadcastOnOff#2
    }
  }
  ```

  Or setting the device configuration parameter with a JSON object with `broadcastGroups` key and the broadcast group array value:

  ```java
  Bridge insteon:network:home [port="/dev/ttyUSB0"] {
    Thing device AABBCC             [address="AA.BB.CC", productKey="0x000045", deviceConfig="{'broadcastGroups': [2]}"]
  }
  ```

### Legacy Items

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
  Thing x10 a2 [houseCode="A", unitCode=2, deviceType="X10_Switch"]
  Thing x10 b4 [houseCode="B", unitCode=4, deviceType="X10_Dimmer"]
  Thing x10 c6 [houseCode="C", unitCode=6, deviceType="X10_Sensor"]
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
Switch  x10Switch "X10 switch" { channel="insteon:x10:home:a2:switch" }
Dimmer  x10Dimmer "X10 dimmer" { channel="insteon:x10:home:b4:dimmer" }
Contact x10Contact "X10 contact" { channel="insteon:x10:home:c6:contact" }
```

## Battery Powered Devices

Battery powered devices (mostly sensors) work differently than standard wired one.
To conserve battery, these devices are only pollable when there are awake.
Typically they send a heartbeat every 24 hours.
When the binding receives a message from one of these devices, it polls additional information needed during the awake period (about 4 seconds).
Some wireless devices have a `stay-awake` channel that can extend the period up to 4 minutes but at the cost of using more battery.
It shouldn't be used in most cases except during initial device configuration.
Same goes with commands, the binding will queue up commands requested on these devices and send them during the awake time window.
Only one command per channel is queued, this mean that subsequent requests will overwrite previous ones.

### Heartbeat Timeout

Sensor devices that support heartbeats have a timeout.
If a broadcast message is not received within a specific interval, the associated thing's status will change to offline.
This status persists until the binding receives a broadcast message from that device.
While most sensor devices have a hardcoded heartbeat interval of 24 hours, some allow modification via the `heartbeat-interval` channel.
This timeout feature is enabled by default on supporting devices and disabled on devices that can have their heartbeat turned off using the `heartbeat-on-off` channel.
Proper linking of the heartbeat group (typically group 4) to the modem is crucial; use the `insteon device addMissingLinks` console command to ensure this.
If the link is missing, the timeout feature will be disabled.
The heartbeat timeout can be manually reset, if necessary, by disabling and then re-enabling the associated device thing.

### Response Timeout

Non-battery powered devices have a response timeout.
If a successful response message is not received within a specific interval, the associated thing's status will change to offline.
While the device is offline, the binding will ignore commands sent to it.
This status persists until a valid response is received.
The response timeout can be increased from 30 minutes (default) up to 6 hours by updating the associated bridge parameter `deviceResponseTimeoutInMinutes`.

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
        Type legacyDimmer : dimmer [related="AA.BB.DD"]
    }
    Thing device AABBDD [address="AA.BB.DD", productKey="F00.00.11"] {
      Channels:
        Type legacyDimmer : dimmer [related="AA.BB.CC"]
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
        Type legacyBroadcastOnOff : broadcastOnOff#3 [related="AA.BB.DD+AA.BB.EE"]
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

```java
rule "Dimmer Paddle Events"
when
  Channel 'insteon:device:home:dimmer:event-button' triggered
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
  Channel 'insteon:device:home:keypad:event-button-a' triggered PRESSED_OFF
then
  // do something
end
```

## Migration Guide

Here are the recommended steps to follow when migrating from the legacy implementation:

- Create a new bridge matching your modem type.
This will automatically disable the legacy network bridge with the same configuration to prevent having two bridges connected to the same modem.

- Once your devices are discovered, they will show in your inbox.
  - Add the discovered things.
  - Connect the new things to your existing semantic models.
  - Link the new channels to your existing items.
  - Update your relevant rules.

- For battery powered devices, press on their SET button to speed up the discovery process.
Otherwise you may have to wait until the next time these devices send a heartbeat message which can take up to 24 hours.

- For wired devices that weren't available during the first binding initialization, once connected, press on their on/off button.
This will notify the binding to retrieve the product information from these devices.

- For scenes, you can either enable scene discovery and add the discovered things, or just manually add specific scene things based on your existing environment.
Enabling scene discovery might generate a considerable amount of things in your inbox depending on the number of scenes configured in your modem.

- If some unknown devices are showing in your inbox, it could be due corrupt messages the binding received during the modem database download phase.
Since the modem database is cached after the first download, it will need to be reloaded using the `insteon modem reloadDatabase` console command.
Otherwise, these devices will keep appearing in your inbox.

- If you have rules to send commands to synchronize the state between related devices, you can enable the device synchronization feature on the bridge instead.
This will synchronize related devices automatically based on their all-link database.

- If you need to re-enable the legacy bridge, simply disable the new bridge and enable the legacy one again.

- Once you finished updating your environment, you can remove the legacy bridge and things, which may need to be forced deleted since their bridge would be disabled.

## Troubleshooting

Turn on DEBUG or TRACE logging for `org.openhab.binding.insteon`.
See [logging in openHAB](https://www.openhab.org/docs/administration/logging.html) for more info.

### Debug Console Commands

To log message events between a device and the modem to a file:

```shell
# Single device monitor
openhab> insteon debug startMonitoring AA.BB.CC
# All devices monitor
openhab> insteon debug startMonitoring --all
```

To send a message to a device or broadcast group:

```shell
# Standard message to a device
openhab> insteon debug sendStandardMessage AA.BB.CC 11 FF
# Broadcast message to a group
openhab> insteon debug sendBroadcastMessage 42 13 00
```

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

  Device types are defined in the file `legacy-device-types.xml`, which is inside the Insteon bundle and thus not visible to the user.
  You can however load your own device_types.xml by referencing it in the network config parameters:

  ```ini
  additionalDevices="/usr/local/openhab/rt/my-own-devices.xml"
  ```

  Where the `my-own-devices.xml` file defines a new device like this:

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
  Avoid duplicate keys by finding the highest fake product key in the `legacy-device-types.xml` file, and incrementing by one.

### Adding New Legacy Device Features

  If you can't build a new device out of the existing device features (for a complete list see `legacy-device-features.xml`) you can add new features by specifying a file (let's call it `my-own-features.xml`) with the "additionalDevices" option in the network config parameters:

  ```ini
  additionalFeatures="/usr/local/openhab/rt/my-own-features.xml"
  ```

  In this file you can define your own features (or even overwrite an existing feature).
  In the example below a new feature "MyFeature" is defined, which can then be referenced from the `legacy-device-types.xml` file (or from `my-own-devices.xml`):

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
