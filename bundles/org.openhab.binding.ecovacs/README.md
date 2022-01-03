# Ecovacs Binding

This binding provides integration for vacuum cleaning / mopping robots made by Ecovacs (https://www.ecovacs.com/).
It discovers devices and communicates to them by using Ecovacs' cloud services.

## Supported Things

- Ecovacs cloud API (`ecovacsapi`)
- Vacuum cleaner (`vacuum`)

At this point, the following devices are fully supported and verified to be working:

- Deebot OZMO 920
- Deebot OZMO 930
- Deebot OZMO 950
- Deebot OZMO Slim 10/11
- Deebot N8 series

The following devices will likely work because they are using similar protocols as the above ones:

- Deebot 600/601/605
- Deebot OZMO 610
- Deebot 710/711/711s
- Deebot 900/901
- Deebot OZMO 900/905
- Deebot OZMO T5
- Deebot (OZMO) T8 series
- Deebot T9 series
- Deebot Slim 2
- Deebot N3 MAX
- Deebot N7
- Deebot U2 series
- Deebot X1 Omni

## Discovery

At first, you need to manually create the bridge thing for the cloud API. Once that is done, the supported devices will be automatically discovered and added to the inbox.

## Thing Configuration

For the cloud API thing, the following parameters must be configured:

| Config    | Description                                                                                                                   |
|-----------|-------------------------------------------------------------------------------------------------------------------------------|
| email     | The email address you used when registering the Ecovacs cloud account                                                         |
| password  | The cloud account password                                                                                                    |
| continent | The continent you are residing on, or 'World' if none matches. This is used to select the correct cloud server to connect to. |

For the vacuum things, there is no required configuration. Optionally, you can tweak the following parameters:

| Config  | Description                                                                            |
|---------|----------------------------------------------------------------------------------------|
| refresh | Refresh interval for polled data (see below), in minutes. By default set to 5 minutes. |

## Channels

The list below lists all channels supported by the binding. In case a particular channel is not supported by a given device (see remarks),
it is automatically removed from the given thing.

| Channel                          | Type                 | Description                                               | Read Only | Updated By | Remarks |
|----------------------------------|----------------------|-----------------------------------------------------------|-----------|------------|------   |
| actions#command                  | String               | Command to execute                                        | No        | Event      | [1]     |
| status#state                     | String               | Current operational state                                 | Yes       | Event      | [2]     |
| status#current-cleaning-time     | Number:Time          | Time spent in current cleaning run                        | Yes       | Event      | [3]     |
| status#current-cleaned-area      | Number:Area          | Area cleaned in current cleaning run                      | Yes       | Event      | [3]     |
| status#water-system-present      | Switch               | Whether the device is currently ready for mopping         | Yes       | Event      | [4]     |
| status#wifi-rssi                 | Number:Power         | The current Wi-Fi signal strength of the device           | Yes       | Polling    | [5]     |
| consumables#main-brush-lifetime  | Number:Dimensionless | The remaining life time of the main brush in percent      | Yes       | Polling    | [6]     |
| consumables#side-brush-lifetime  | Number:Dimensionless | The remaining life time of the side brush in percent      | Yes       | Polling    |         |
| consumables#dust-filter-lifetime | Number:Dimensionless | The remaining life time of the dust bin filter in percent | Yes       | Polling    |         |
| last-clean#last-clean-start      | DateTime             | The start time of the last completed cleaning run         | Yes       | Polling    |         |
| last-clean#last-clean-duration   | Number:Time          | The duration of the last completed cleaning run           | Yes       | Polling    |         |
| last-clean#last-clean-area       | Number:Area          | The area cleaned in the last completed cleaning run       | Yes       | Polling    |         |
| last-clean#last-clean-mode       | String               | The mode used for the last completed cleaning run         | Yes       | Polling    | [7]     |
| last-clean#last-clean-map        | Image                | The map image of the last completed cleaning run          | Yes       | Polling    |         |
| total-stats#total-cleaning-time  | Number:Time          | The total time spent cleaning during the device life time | Yes       | Polling    |         |
| total-stats#total-cleaned-area   | Number:Area          | The total area cleaned during the device life time        | Yes       | Polling    |         |
| total-stats#total-clean-runs     | Number               | The total number of clean runs in the device life time    | Yes       | Polling    |         |
| settings#suction-power           | String               | The power level used during cleaning                      | No        | Polling    | [8]     |
| settings#voice-volume            | Dimmer               | The voice volume level in percent                         | No        | Polling    | [9]     |
| settings#water-amount            | String               | The amount of water to be used when mopping               | No        | Polling    | [10]    |

Remarks:

- [1] See [section below](#command-channel-actions)
- [2] Possible states: 'auto', 'edge', 'spot', 'spotArea', 'customArea', 'singleRoom', 'pause', 'stop', 'returning' and 'charging'
- [3] Current cleaning status is only valid if the device is currently cleaning
- [4] Only present if device has a mopping system
- [5] Only present on newer generation devices (Deebot OZMO 950 and newer)
- [6] Only present if device has a main brush
- [7] For possible modes, see list under [2]
- [8] Only present if device can control power level. Possible values vary by device: 'normal' and 'high' are always supported, 'silent' and 'higher' are supported for some models
- [9] Only present if device has voice reporting
- [10] Only present if device has a mopping system. Possible values include 'low', 'medium', 'high' and 'veryhigh'

## Command Channel Actions

The following actions are supported by the `command` channel:

| Name       | Action                                    | Remarks                                              |
|------------|-------------------------------------------|------------------------------------------------------|
| `clean`    | Start cleaning in automatic mode.         |                                                      |
| `spotArea` | Start cleaning specific rooms.            | <ul><li>Only if supported by device, which can be recognized by `spotArea` being present in the list of possible states of the `state` channel.</li><li>Format: `spotArea:<room IDs>`, where `room IDs` is a comma separated list of room letters as shown in Ecovacs' app, so a valid command could e.g. be `spotArea:A,D,E`.</li><li>If you want to run 2 clean passes, amend `:x2` to the command, e.g. `spotArea:A,C,B:x2`.</li></ul> |
| `pause`    | Pause cleaning if it's currently active.  | If the device is idle, the command is ignored.       |
| `resume`   | Resume cleaning if it's currently paused. | If the device is not paused, the command is ignored. |
| `stop`     | Stop cleaning immediately.                |                                                      |
| `charge`   | Send device to charging station.          |                                                      |

## File Based Configuration

If you want to create the API bridge in a .things file, the entry has to look as follows:

```
Bridge ecovacs:ecovacsapi:ecovacsapi [ email="your.email@provider.com", password="yourpassword", continent="ww" ]
```

Then devices are detected automatically. If you also want to enter those manually, the syntax is as follows:

```
Bridge ecovacs:ecovacsapi:ecovacsapi [ email="your.email@provider.com", password="yourpassword", continent="ww" ]
{
    Thing vacuum myDeebot "Deebot Vacuum" [ serialNumber="serial as printed on label" ]
}
```

