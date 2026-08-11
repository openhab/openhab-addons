# Ecovacs Binding

This binding provides integration for vacuum cleaning / mopping robots and robotic lawn mowers made by Ecovacs (<https://www.ecovacs.com/>).
It discovers devices and communicates to them by using Ecovacs' cloud services.

## Supported Things

- Ecovacs cloud API (`ecovacsapi`)
- Vacuum cleaner (`vacuum`)
- Robotic lawn mower (`mower`)

### Vacuum Cleaners

At this point, the following devices are fully supported and verified to be working:

- Deebot OZMO 900/905
- Deebot OZMO 920
- Deebot OZMO 930
- Deebot OZMO 950
- Deebot OZMO Slim 10/11
- Deebot N8 series
- Deebot T30 series

The following devices will likely work because they are using similar protocols as the above ones:

- Deebot 600/601/605
- Deebot 900/901
- Deebot OZMO 610
- Deebot 710/711/711s
- Deebot OZMO T5
- Deebot (OZMO) T8 series
- Deebot T9 series
- Deebot T10 series
- Deebot T20 series
- Deebot Slim 2
- Deebot N3 MAX
- Deebot N30 series
- Deebot N7
- Deebot U2 series
- Deebot X1 series
- Deebot X2 series

### Robotic Lawn Mowers (GOAT Series)

The following GOAT mowers are supported:

- GOAT O500 Panorama
- GOAT G1 / G1-800
- GOAT O600 RTK
- GOAT O800 RTK
- GOAT O1200 LiDAR Pro
- GOAT A1600 RTK
- GOAT A3000 LiDAR / LiDAR Pro

## Discovery

At first, you need to manually create the bridge thing for the cloud API.
Once that is done, the supported devices will be automatically discovered and added to the inbox.

## Thing Configuration

For the cloud API thing, the following parameters must be configured:

| Config    | Description                                                                                                                   |
|-----------|-------------------------------------------------------------------------------------------------------------------------------|
| email     | The email address you used when registering the Ecovacs cloud account                                                         |
| password  | The cloud account password                                                                                                    |
| continent | The continent you are residing on, or 'World' if none matches. This is used to select the correct cloud server to connect to. |

For the vacuum things, there is no required configuration (when using discovery). The following parameters exist:

| Config       | Description                                                                                                                   |
|--------------|-------------------------------------------------------------------------------------------------------------------------------|
| serialNumber | Required: The device's serial number as printed on the barcode below the dust bin. Filled automatically when using discovery. |
| refresh      | Refresh interval for polled data (see below), in minutes. By default set to 5 minutes.                                        |

For the mower things, the same configuration parameters apply:

| Config       | Description                                                                                                                   |
|--------------|-------------------------------------------------------------------------------------------------------------------------------|
| serialNumber | Required: The device's serial number. Filled automatically when using discovery.                                              |
| refresh      | Refresh interval for polled data (see below), in minutes. By default set to 5 minutes.                                        |

## Channels

The list below lists all channels supported by the binding.
In case a particular channel is not supported by a given device (see remarks), it is automatically removed from the given thing.

| Channel                                 | Type                 | Description                                               | Read Only | Updated By | Remarks  |
|-----------------------------------------|----------------------|-----------------------------------------------------------|-----------|------------|----------|
| actions#command                         | String               | Command to execute                                        | No        | Event      | [1]      |
| status#battery                          | Number               | Current battery level                                     | Yes       | Event      |          |
| status#state                            | String               | Current operational state                                 | Yes       | Event      | [2]      |
| status#current-cleaning-mode            | String               | Mode used in current cleaning run                         | Yes       | Event      | [3], [4] |
| status#current-cleaning-time            | Number:Time          | Time spent in current cleaning run                        | Yes       | Event      | [4]      |
| status#current-cleaned-area             | Number:Area          | Area cleaned in current cleaning run                      | Yes       | Event      | [4]      |
| status#current-cleaning-spot-definition | String               | The spot to clean in current cleaning run                 | Yes       | Event      | [4], [5] |
| status#water-system-present             | Switch               | Whether the device is currently ready for mopping         | Yes       | Event      | [6]      |
| status#wifi-rssi                        | Number:Power         | The current Wi-Fi signal strength of the device           | Yes       | Polling    | [7]      |
| consumables#main-brush-lifetime         | Number:Dimensionless | The remaining life time of the main brush in percent      | Yes       | Polling    | [8]      |
| consumables#side-brush-lifetime         | Number:Dimensionless | The remaining life time of the side brush in percent      | Yes       | Polling    |          |
| consumables#dust-filter-lifetime        | Number:Dimensionless | The remaining life time of the dust bin filter in percent | Yes       | Polling    |          |
| consumables#other-component-lifetime    | Number:Dimensionless | The remaining time until device maintenance in percent    | Yes       | Polling    | [9]      |
| consumables#round-mop-lifetime          | Number:Dimensionless | The remaining life time of the mops in percent            | Yes       | Polling    | [10]     |
| last-clean#last-clean-start             | DateTime             | The start time of the last completed cleaning run         | Yes       | Polling    |          |
| last-clean#last-clean-duration          | Number:Time          | The duration of the last completed cleaning run           | Yes       | Polling    |          |
| last-clean#last-clean-area              | Number:Area          | The area cleaned in the last completed cleaning run       | Yes       | Polling    |          |
| last-clean#last-clean-mode              | String               | The mode used for the last completed cleaning run         | Yes       | Polling    | [3]      |
| last-clean#last-clean-map               | Image                | The map image of the last completed cleaning run          | Yes       | Polling    |          |
| total-stats#total-cleaning-time         | Number:Time          | The total time spent cleaning during the device life time | Yes       | Polling    |          |
| total-stats#total-cleaned-area          | Number:Area          | The total area cleaned during the device life time        | Yes       | Polling    |          |
| total-stats#total-clean-runs            | Number               | The total number of clean runs in the device life time    | Yes       | Polling    |          |
| settings#auto-empty                     | Switch               | Whether dust bin auto empty to station is enabled         | No        | Polling    | [11]     |
| settings#cleaning-passes                | Number               | Number of cleaning passes to be used (1 or 2)             | No        | Polling    | [9]      |
| settings#continuous-cleaning            | Switch               | Whether unfinished cleaning resumes after charging        | No        | Polling    |          |
| settings#suction-power                  | String               | The power level used during cleaning                      | No        | Polling    | [12]     |
| settings#true-detect-3d                 | Switch               | Whether True Detect 3D is enabled                         | No        | Polling    | [13]     |
| settings#voice-volume                   | Dimmer               | The voice volume level in percent                         | No        | Polling    | [14]     |
| settings#water-amount                   | String               | The amount of water to be used when mopping               | No        | Polling    | [15]     |
| settings#water-amount-percent           | Dimmer               | The amount of water to be used when mopping (in percent)  | No        | Polling    | [16]     |

Remarks:

- [1] See [section below](#command-channel-actions)
- [2] Possible states: `cleaning`, `pause`, `stop`, `emptying`, `drying`, `washing`, `returning` and `charging` (where `emptying`, `drying` and `washing` are only available on newer models with auto empty station)
- [3] Possible states: `auto`, `edge`, `spot`, `spotArea`, `customArea`, `singleRoom`, and `sceneClean` (some of which depend on device capabilities)
- [4] Current cleaning status is only valid if the device is currently cleaning
- [5] Only valid for `spot`, `spotArea`, `customArea`, and `sceneClean` cleaning modes; value can be used for `spotArea`, `customArea`, or `sceneClean` commands (see below)
- [6] Only present if device has a mopping system
- [7] Only present on newer generation devices (Deebot OZMO 950 and newer)
- [8] Only present if device has a main brush
- [9] Only present on newer generation devices (Deebot N8/T8 or newer)
- [10] Only present if device has round mops (and not just a mopping plate)
- [11] Only present if device has a dustbin auto empty station; supports both on/off command (to turn on/off the setting) and the string `trigger` (to trigger immediate auto empty)
- [12] Only present if device can control power level. Possible values vary by device: `normal` and `high` are always supported, `silent` and `higher` are supported for some models
- [13] Only present if device supports True Detect 3D
- [14] Only present if device has voice reporting
- [15] Only present if device has a mopping system. Possible values include `low`, `medium`, `high` and `veryhigh`
- [16] Only present on Deebot X8 or newer.

## Mower Channels

The following channels are available for GOAT mower devices.
Channels not supported by a particular model are automatically removed.

| Channel                                 | Type                 | Description                                               | Read Only | Updated By | Remarks  |
|-----------------------------------------|----------------------|-----------------------------------------------------------|-----------|------------|----------|
| actions#command                         | String               | Command to execute                                        | No        | Event      | [M1]     |
| status#state                            | String               | Current operational state                                 | Yes       | Event      | [M2]     |
| status#battery                          | Number               | Battery level (percentage)                                | Yes       | Event      |          |
| status#current-mowing-time              | Number:Time          | Time spent in current mowing session                      | Yes       | Event      |          |
| status#current-mowed-area               | Number:Area          | Area mowed in current session                             | Yes       | Event      |          |
| status#wifi-rssi                        | Number:Power         | The current Wi-Fi signal strength of the device           | Yes       | Polling    |          |
| status#error-code                       | Number               | The numerical value of the last error (0 = none)          | Yes       | Event      |          |
| status#error-description                | String               | Text describing the last error                            | Yes       | Event      |          |
| consumables#blade-lifetime              | Number:Dimensionless | The remaining life time of the cutting blades in percent  | Yes       | Polling    |          |
| last-mow#last-mow-start                 | DateTime             | Start time of the last completed mowing run               | Yes       | Event      |          |
| last-mow#last-mow-duration              | Number:Time          | Duration of the last completed mowing run                 | Yes       | Event      |          |
| last-mow#last-mow-area                  | Number:Area          | Area mowed in the last completed mowing run               | Yes       | Event      |          |
| total-stats#total-mowing-time           | Number:Time          | Total mowing time in device life time                     | Yes       | Polling    |          |
| total-stats#total-mowed-area            | Number:Area          | Total area mowed in device life time                      | Yes       | Polling    |          |
| total-stats#total-mow-runs              | Number               | Total number of mowing runs in device life time           | Yes       | Polling    |          |
| settings#cutting-height                 | Number:Length        | Cutting height (30-75mm in 5mm steps)                     | [M3]      | Polling    | [M3]     |
| settings#voice-volume                   | Dimmer               | Voice volume level in percent                             | No        | Polling    |          |
| settings#true-detect-3d                 | Switch               | Whether TrueDetect 3D obstacle avoidance is enabled       | No        | Polling    |          |
| settings#safe-protect                   | Switch               | Stop mower on obstacle or unsafe condition                | No        | Polling    |          |
| settings#child-lock                     | Switch               | Lock mower to prevent unintended operation                | No        | Polling    |          |
| settings#rain-delay                     | Switch               | Rain detected, mowing delayed                             | Yes       | Polling    |          |
| settings#moveup-warning                 | Switch               | Alarm when mower is lifted off the ground                 | No        | Polling    |          |

Note: Some channels may be automatically removed if the device does not report support for the corresponding feature.

Remarks:

- [M1] See [Mower Command Channel Actions](#mower-command-channel-actions)
- [M2] Possible states: `mowing`, `paused`, `returning`, `charging`, `idle`, `docked`, `error`, `edgeCutting`
- [M3] Only present if device supports cutting height reporting. Currently read-only.

## Mower Command Channel Actions

The following actions are supported by the mower `command` channel:

| Name         | Action                                      | Remarks                                                                                       |
|--------------|---------------------------------------------|-----------------------------------------------------------------------------------------------|
| `mow`        | Start mowing in automatic mode.             | If currently paused, sends resume instead of start.                                           |
| `edgeCut`    | Start edge cutting.                         | Only if device supports edge mowing.                                                          |
| `zone:<ids>` | Start mowing specific zones.                | Only if device supports zone mowing. Format: `zone:<zone IDs>` (as shown in Ecovacs' app).    |
| `pause`      | Pause mowing if currently active.           | If the mower is idle, the command is ignored.                                                 |
| `resume`     | Resume mowing if currently paused.          | If the mower is not paused, the command is ignored.                                           |
| `stop`       | Stop mowing immediately.                    |                                                                                               |
| `dock`       | Send mower back to the charging station.    |                                                                                               |

## Command Channel Actions

The following actions are supported by the `command` channel:

| Name         | Action                                      | Remarks                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
|--------------|---------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `clean`      | Start cleaning in automatic mode.           |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `spotArea`   | Start cleaning specific rooms.              | <ul><li>Only if supported by device, which can be recognized by `spotArea` being present in the list of possible states of the `current-cleaning-mode` channel.</li><li>Format: `spotArea:<room IDs>`, where `room IDs` is a semicolon separated list of room letters as shown in Ecovacs' app, so a valid command could e.g. be `spotArea:A;D;E`.</li><li>If you want to run 2 clean passes, append `:x2` to the command, e.g. `spotArea:A;C;B:x2`.</li></ul>                                                                                            |
| `customArea` | Start cleaning specific areas.              | <ul><li>Only if supported by device, which can be recognized by `customArea` being present in the list of possible states of the `current-cleaning-mode` channel.</li><li>Format: `customArea:<x1>;<y1>;<x2>;<y2>`, where the parameters are coordinates (in mm) relative to the map.</li><li>The coordinates can be obtained from the `current-cleaning-spot-definition` channel when starting a custom area run from the app.</li><li>If you want to run 2 clean passes, append `:x2` to the command, e.g. `customArea:100;100;1000;1000:x2`.</li></ul> |
| `sceneClean` | Start cleaning using a predefined scenario. | <ul><li>Only if supported by device, which can be recognized by `sceneClean` being present in the list of possible states of the `current-cleaning-mode` channel.</li><li>Format: `sceneClean:<scenarioId>`. The `scenarioID` can be obtained from the `current-cleaning-spot-definition` channel when starting a scenario run from the app. Example: `sceneClean:5318`.</li></ul>                                                                                                                                                                        |
| `pause`      | Pause cleaning if it's currently active.    | If the device is idle, the command is ignored.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `resume`     | Resume cleaning if it's currently paused.   | If the device is not paused, the command is ignored.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `stop`       | Stop cleaning immediately.                  |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `charge`     | Send device to charging station.            |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |

## Rule actions

This binding includes a rule action, which allows playback of specific sounds on the device in case the device has a speaker.
There is a separate instance for each device, which can be retrieved like this:

```java
val vacuumActions = getActions("ecovacs","ecovacs:vacuum:1234567890")
```

where the first parameter always has to be `ecovacs` and the second is the full Thing UID of the device that should be used.
Once this action instance is retrieved, you can invoke the `playSound(String type)` method on it:

```java
vacuumActions.playSound("beep")
```

Supported sound types include:

- `beep`
- `iAmHere`
- `startup`
- `suspended`
- `batteryLow`

For special use cases, there is also a `playSoundWithId(int soundId)` method, where you can pass the numeric ID of the sound to play.
The exact meaning of the number depends on the specific device; you'll need to experiment with different numbers to see how the number-to-sound mapping looks like.
For reference, a list for the Deebot 900 can be found in the [Deebot 900 protocol documentation on GitHub](https://github.com/bmartin5692/sucks/blob/D901/protocol.md#user-content-sounds).

## File Based Configuration

If you want to create the API bridge in a .things file, the entry has to look as follows:

```java
Bridge ecovacs:ecovacsapi:ecovacsapi [ email="your.email@provider.com", password="yourpassword", continent="ww" ]
```

The possible values for `continent` include the following values:

- `ww` for World
- `eu` for Europe
- `na` for North America
- `as` for Asia

The devices are detected automatically.
If you also want to enter those manually, the syntax is as follows:

```java
Bridge ecovacs:ecovacsapi:ecovacsapi [ email="your.email@provider.com", password="yourpassword", continent="ww" ]
{
    Thing vacuum myDeebot "Deebot Vacuum" [ serialNumber="serial as printed on label below dust bin" ]
}
```

For mowers:

```java
Bridge ecovacs:ecovacsapi:ecovacsapi [ email="your.email@provider.com", password="yourpassword", continent="eu" ]
{
    Thing mower myGoat "GOAT O500 Panorama" [ serialNumber="your mower serial number" ]
}
```

### Mower Items Example

```java
String       GoatState              "Mower State [%s]"                    { channel="ecovacs:mower:myapi:myGoat:status#state" }
Number       GoatBattery            "Mower Battery [%d %%]"               { channel="ecovacs:mower:myapi:myGoat:status#battery" }
Number:Time  GoatMowingTime         "Current Mowing Time [%d %unit%]"     { channel="ecovacs:mower:myapi:myGoat:status#current-mowing-time" }
Number:Area  GoatMowedArea          "Current Mowed Area [%d %unit%]"      { channel="ecovacs:mower:myapi:myGoat:status#current-mowed-area" }
Number:Length GoatCuttingHeight     "Cutting Height [%d %unit%]"          { channel="ecovacs:mower:myapi:myGoat:settings#cutting-height" }
Dimmer       GoatVolume             "Voice Volume [%d %%]"                { channel="ecovacs:mower:myapi:myGoat:settings#voice-volume" }
Switch       GoatSafeProtect        "SafeProtect"                         { channel="ecovacs:mower:myapi:myGoat:settings#safe-protect" }
Switch       GoatChildLock          "Child Lock"                          { channel="ecovacs:mower:myapi:myGoat:settings#child-lock" }
Number:Dimensionless GoatBladeLife  "Blade Lifetime [%d %%]"              { channel="ecovacs:mower:myapi:myGoat:consumables#blade-lifetime" }
Number:Time  GoatTotalTime          "Total Mowing Time [%d %unit%]"       { channel="ecovacs:mower:myapi:myGoat:total-stats#total-mowing-time" }
Number:Area  GoatTotalArea          "Total Mowed Area [%d %unit%]"        { channel="ecovacs:mower:myapi:myGoat:total-stats#total-mowed-area" }
Number       GoatTotalRuns          "Total Mow Runs [%d]"                 { channel="ecovacs:mower:myapi:myGoat:total-stats#total-mow-runs" }
Number       GoatErrorCode          "Error Code [%d]"                     { channel="ecovacs:mower:myapi:myGoat:status#error-code" }
String       GoatErrorDesc          "Error [%s]"                          { channel="ecovacs:mower:myapi:myGoat:status#error-description" }
Number:Power GoatWifiRssi           "WiFi RSSI [%d %unit%]"               { channel="ecovacs:mower:myapi:myGoat:status#wifi-rssi" }
DateTime     GoatLastMowStart       "Last Mow Start [%1$tF %1$tR]"       { channel="ecovacs:mower:myapi:myGoat:last-mow#last-mow-start" }
Number:Time  GoatLastMowDuration    "Last Mow Duration [%d %unit%]"       { channel="ecovacs:mower:myapi:myGoat:last-mow#last-mow-duration" }
Number:Area  GoatLastMowArea        "Last Mowed Area [%d %unit%]"         { channel="ecovacs:mower:myapi:myGoat:last-mow#last-mow-area" }
Switch       GoatTrueDetect         "TrueDetect 3D"                       { channel="ecovacs:mower:myapi:myGoat:settings#true-detect-3d" }
Switch       GoatMoveUpWarning      "Move-Up Warning"                     { channel="ecovacs:mower:myapi:myGoat:settings#moveup-warning" }
String       GoatCommand            "Mower Command"                       { channel="ecovacs:mower:myapi:myGoat:actions#command" }
```

## Adding support for unsupported models

When encountering an unsupported model during discovery, the binding creates a log message like this one:

```text
2023-04-21 12:02:39.607 [INFO ] [acs.internal.api.impl.EcovacsApiImpl] - Found unsupported device DEEBOT N8 PRO CARE (class s1f8g7, company eco-ng), ignoring.
```

In such a case, please [create an issue in the openHAB Add-ons GitHub repository](https://github.com/openhab/openhab-addons/issues), listing the contents of the log line.
In addition to that, if the model is similar to an already supported one, you can try to add the support yourself (until getting an updated binding).
For doing so, you can follow the following steps:

- create the folder `<OPENHAB_USERDATA>/ecovacs` (if not done previously)
- create a file named `custom_device_descs.json`, whose format of that file is the same as [the built-in device list](https://raw.githubusercontent.com/openhab/openhab-addons/main/bundles/org.openhab.binding.ecovacs/src/main/resources/devices/supported_device_list.json)
- for a model that is very similar to an existing one, create an entry with `modelName`, `deviceClass` (from the log line) and `deviceClassLink` (`deviceClass` of the similar model)
- for other models, you can also try experimenting with creating a full entry, but it's likely that the binding code will need to be updated in that case
