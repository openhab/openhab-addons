# Panasonic TV Binding

This binding integrates the [Panasonic TV's](http://www.panasonic.com).

## Supported Things

Panasonic Viera TV  (E6), models should be supported.
Panasonic TVs does not support full UPNP standard functionalities
Volume and Mute status are updated in real time
Most of the control is provided by sending Key Code to RemoteControl API

Source Name (read only) is updated when a Key Code to change the source is sent to tv, otherwise it is Undefined


Tested TV models:

| Model         | State   | Notes                                                                                |
|---------------|---------|--------------------------------------------------------------------------------------|
| Viera E6      | OK      | Initial contribution is done by this model                                           |
| TX-40CX680    | OK      | Initial contribution is done by this model                                           |



## Discovery

The TV's are discovered through UPnP protocol in the local network and all devices are put in the Inbox.
UPnP must be enabled in the TV settings via the "Network -> TV Remote App Settings -> TV Remote: ON". This may be different on other models.  
Optional: "Network Standby: ON" and "Powered On by Apps: ON" can be set to enable power on via the binding. 

## Binding Configuration

The binding does not require any special configuration.

## Thing Configuration

The Panasonic TV Thing requires the UPNP UDN of the RemoteController and MediaRenderer as a configuration value in order for the binding to know how to access it. Panasonic TV publish several UPnP devices and the manufacturer and service names are used to recognize those UPnP devices.
Additionally, a refresh interval can be configured in milliseconds to specify how often TV resources are polled.

E.g.

```
Thing panasonictv:tv:livingroom [ remoteControllerUdn="4D454930-0200-1000-8001-A813742A3472", mediaRendererUdn="4D454930-0100-1000-8001-A813742A3472", refreshInterval=1000 ]
```

## Channels

TVs support the following channels:

| Channel Type ID  | Item Type | Description                                                                                             |
|------------------|-----------|---------------------------------------------------------------------------------------------------------|
| volume           | Dimmer    | Volume level of the TV.                                                                                 |
| mute             | Switch    | Mute state of the TV.                                                                                                                  |
| sourceName       | String    | Name of the current source. Readonly, updated blindly when keyCode to change input is sent                                                                          |
| sourceId         | Number    | Id of the current source.                                                                               |
| keyCode          | String    | The key code channel emulates the infrared remote controller and allows to send virtual button presses. |

E.g.

```
Dimmer  TV_Volume   { channel="panasonictv:tv:livingroom:volume" }
Switch  TV_Mute     { channel="panasonictv:tv:livingroom:mute" }
String  TV_KeyCode  { channel="panasonictv:tv:livingroom:keyCode" }
```
