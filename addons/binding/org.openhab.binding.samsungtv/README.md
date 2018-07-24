# Samsung TV Binding

This binding integrates the [Samsung TV's](http://www.samsung.com).

## Supported Things

Samsung TV C (2010), D (2011), E (2012) and F (2013) models should be supported.
Because Samsung does not publish any documentation about the TV's UPnP interface, there could be differences between different TV models, which could lead to mismatch problems.

Tested TV models:

| Model     | State   | Notes                                                                                |
|-----------|---------|--------------------------------------------------------------------------------------|
| UE46E5505 | OK      | Initial contribution is done by this model                                           |
| UE46D5700 | PARTIAL | Supports at my home only commands via the fake remote, no discovery                  |
| UE40F6500 | OK      | All channels except `colorTemperature`, `programTitle` and `channelName` are working |


## Discovery

The TV's are discovered through UPnP protocol in the local network and all devices are put in the Inbox.

## Binding Configuration

The binding does not require any special configuration.

## Thing Configuration

The Samsung TV Thing requires the host name and port address as a configuration value in order for the binding to know how to access it. Samsung TV publish several UPnP devices and hostname is used to recognize those UPnP devices.
Port address is used for remote control emulation protocol.
Additionally, a refresh interval can be configured in milliseconds to specify how often TV resources are polled.

E.g.

```
Thing samsungtv:tv:livingroom [ hostName="192.168.1.10", port=55000, refreshInterval=1000 ]
```

## Channels

TVs support the following channels:

| Channel Type ID  | Item Type | Description                                                                                             |
|------------------|-----------|---------------------------------------------------------------------------------------------------------|
| volume           | Dimmer    | Volume level of the TV.                                                                                 |
| mute             | Switch    | Mute state of the TV.                                                                                   |
| brightness       | Dimmer    | Brightness of the TV picture.                                                                           |
| contrast         | Dimmer    | Contrast of the TV picture.                                                                             |
| sharpness        | Dimmer    | Sharpness of the TV picture.                                                                            |
| colorTemperature | Number    | Color temperature of the TV picture. Minimum value is 0 and maximum 4.                                  |
| sourceName       | String    | Name of the current source.                                                                             |
| sourceId         | Number    | Id of the current source.                                                                               |
| channel          | Number    | Selected TV channel number.                                                                             |
| programTitle     | String    | Program title of the current channel.                                                                   |
| channelName      | String    | Name of the current TV channel.                                                                         |
| url              | String    | Start TV web browser and go the given web page.                                                         |
| stopBrowser      | Switch    | Stop TV's web browser and go back to TV mode.                                                           |
| power            | Switch    | TV power. Some of the Samsung TV models doesn't allow to set Power ON remotely.                         |
| keyCode          | String    | The key code channel emulates the infrared remote controller and allows to send virtual button presses. |

E.g.

```
Dimmer  TV_Volume   { channel="samsungtv:tv:livingroom:volume" }
Switch  TV_Mute     { channel="samsungtv:tv:livingroom:mute" }
String  TV_KeyCode  { channel="samsungtv:tv:livingroom:keyCode" }
```
