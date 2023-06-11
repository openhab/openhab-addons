# Samsung TV Binding

This binding integrates the [Samsung TV's](https://www.samsung.com).

## Supported Things

Samsung TV C (2010), D (2011), E (2012) and F (2013) models should be supported.
Also support added for TVs using websocket remote interface (2016+ models)
Because Samsung does not publish any documentation about the TV's UPnP interface, there could be differences between different TV models, which could lead to mismatch problems.

Tested TV models:

| Model          | State   | Notes                                                                                                                                                  |
| -------------- | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| KU6519         | PARTIAL | Supported channels: `volume`, `mute`, `power`,  `keyCode` (at least)                                                                                   |
| LE40D579       | PARTIAL | Supported channels: `volume`, `mute`, `channel`, `keyCode`, `sourceName`,  `programTitle`, `channelName`,  `power`                                     |
| LE40C650       | PARTIAL | Supported channels: `volume`, `mute`, `channel`, `keyCode`, `brightness`, `contrast`, `colorTemperature`, `power` (only power off, unable to power on) |
| UE40F6500      | OK      | All channels except `colorTemperature`, `programTitle` and `channelName` are working                                                                   |
| UE40J6300AU    | PARTIAL | Supported channels: `volume`, `mute`, `sourceName`, `power`                                                                                            |
| UE43MU6199     | PARTIAL | Supported channels: `volume`, `mute`, `power` (at least)                                                                                               |
| UE46D5700      | PARTIAL | Supports at my home only commands via the fake remote, no discovery                                                                                    |
| UE46E5505      | OK      | Initial contribution is done by this model                                                                                                             |
| UE46F6510SS    | PARTIAL | Supported channels: `volume`, `mute`, `channel` (at least)                                                                                             |
| UE48J5670SU    | PARTIAL | Supported channels: `volume`, `sourceName`                                                                                                             |
| UE50MU6179     | PARTIAL | Supported channels: `volume`, `mute`, `power`, `keyCode`, `channel`, `sourceApp`, `url`                                                                |
| UE55LS003      | PARTIAL | Supported channels: `volume`, `mute`, `sourceApp`, `url`, `keyCode`, `power`, `artMode`                                                                |
| UE58RU7179UXZG | PARTIAL | Supported channels: `volume`, `mute`, `power`, `keyCode` (at least)                                                                                    |
| UN50J5200      | PARTIAL | Status is retrieved (confirmed `power`, `media title`). Operating device seems not working.                                                            |

## Discovery

The TV's are discovered through UPnP protocol in the local network and all devices are put in the Inbox.

## Binding Configuration

The binding does not require any special configuration.

## Thing Configuration

The Samsung TV Thing requires the host name and port address as a configuration value in order for the binding to know how to access it.
Samsung TV publish several UPnP devices and hostname is used to recognize those UPnP devices.
Port address is used for remote control emulation protocol.
Additionally, a refresh interval can be configured in milliseconds to specify how often TV resources are polled.

E.g.

```java
Thing samsungtv:tv:livingroom [ hostName="192.168.1.10", port=55000, macAddress="78:bd:bc:9f:12:34", refreshInterval=1000 ]
```

Different ports are used in different models. It may be 55000, 8001 or 8002.

## Channels

TVs support the following channels:

| Channel Type ID  | Item Type | Description                                                                                                                         |
| ---------------- | --------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| volume           | Dimmer    | Volume level of the TV.                                                                                                             |
| mute             | Switch    | Mute state of the TV.                                                                                                               |
| brightness       | Dimmer    | Brightness of the TV picture.                                                                                                       |
| contrast         | Dimmer    | Contrast of the TV picture.                                                                                                         |
| sharpness        | Dimmer    | Sharpness of the TV picture.                                                                                                        |
| colorTemperature | Number    | Color temperature of the TV picture. Minimum value is 0 and maximum 4.                                                              |
| sourceName       | String    | Name of the current source.                                                                                                         |
| sourceId         | Number    | Id of the current source.                                                                                                           |
| channel          | Number    | Selected TV channel number.                                                                                                         |
| programTitle     | String    | Program title of the current channel.                                                                                               |
| channelName      | String    | Name of the current TV channel.                                                                                                     |
| url              | String    | Start TV web browser and go the given web page.                                                                                     |
| stopBrowser      | Switch    | Stop TV's web browser and go back to TV mode.                                                                                       |
| power            | Switch    | TV power. Some of the Samsung TV models doesn't allow to set Power ON remotely.                                                     |
| artMode          | Switch    | TV art mode for e.g. Samsung The Frame TV's. Only relevant if power=off. If set to on when power=on, the power will be switched off |
| sourceApp        | String    | Currently active App.                                                                                                               |
| keyCode          | String    | The key code channel emulates the infrared remote controller and allows to send virtual button presses.                             |

E.g.

```java
Group   gLivingRoomTV    "Living room TV" <screen>
Dimmer  TV_Volume        "Volume"         <soundvolume>        (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:volume" }
Switch  TV_Mute          "Mute"           <soundvolume_mute>   (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:mute" }
String  TV_SourceName    "Source Name"                         (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:sourceName" }
String  TV_SourceApp     "Source App"                          (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:sourceApp" }
String  TV_ProgramTitle  "Program Title"                       (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:programTitle" }
String  TV_ChannelName   "Channel Name"                        (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:channelName" }
String  TV_KeyCode       "Key Code"                            (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:keyCode" }
Switch  TV_Power         "Power"                               (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:power" }
Switch  TV_ArtMode       "Art Mode"                            (gLivingRoomTV)   { channel="samsungtv:tv:livingroom:artMode" }
```

### Apps

List of known apps and the respective name that can be passed on to the `sourceApp` channel.
Values are confirmed to work on UE50MU6179.

| App           | Value in sourceApp | Description                       |
| ------------- | ------------------ | --------------------------------- |
| ARD Mediathek | `ARD Mediathek`    | German public TV broadcasting app |
| Browser       | `Internet`         | Built-in WWW browser              |
| Netflix       | `Netflix`          | Netflix App                       |
| Prime Video   | `Prime Video`      | Prime Video App                   |
| YouTube       | `YouTube`          | YouTube App                       |
| ZDF Mediathek | `ZDF mediathek`    | German public TV broadcasting app |

To discover all installed apps names, you can enable the DEBUG log output from the binding to see a list.
