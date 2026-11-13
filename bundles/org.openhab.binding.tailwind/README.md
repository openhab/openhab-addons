# TailWind Binding

This binding controls garage doors through [TailWind](https://gotailwind.com/) smart garage controllers.
It uses the controller's local control API, so it works without the TailWind cloud service.

The API is described in the [Tailwind Local Control API](https://github.com/Scott--R/Tailwind_Local_Control_API) document.
The controller needs firmware 9.96 or later.

## Supported Things

| Thing Type | Description                                                  |
|------------|--------------------------------------------------------------|
| `iq3`      | TailWind iQ3 controller, which controls up to 3 garage doors |

## Discovery

Controllers are discovered automatically with mDNS and appear in the Inbox.
Add the controller from the Inbox and enter its Local Control Key, and it goes online.
Everything else is set up automatically, including the channels of the doors that are connected to the controller.

## Thing Configuration

| Parameter              | Type    | Required | Default | Description                                                                      |
|------------------------|---------|----------|---------|----------------------------------------------------------------------------------|
| `hostname`             | text    | yes      |         | Host name or IP address of the controller, such as `tailwind-30aea4801880.local` |
| `token`                | text    | yes      |         | The 6-digit Local Control Key of the controller                                  |
| `udpPort`              | integer | no       | 50904   | UDP port that receives the status reports of the controller. Advanced.           |
| `door1PartialOpenTime` | decimal | no       | 2.5     | Seconds that door 1 runs when it is opened partially (0.5 to 15). Advanced.      |
| `door2PartialOpenTime` | decimal | no       | 2.5     | Seconds that door 2 runs when it is opened partially (0.5 to 15). Advanced.      |
| `door3PartialOpenTime` | decimal | no       | 2.5     | Seconds that door 3 runs when it is opened partially (0.5 to 15). Advanced.      |

The Local Control Key is shown on the Local Control Key page of the [TailWind web dashboard](https://web.gotailwind.com).
See the [token section](https://github.com/Scott--R/Tailwind_Local_Control_API#2-token) of the API document for details.

The host name of a controller is `tailwind-` followed by its MAC address and `.local`.
The host name is recommended, because it stays the same when the router gives the controller another IP address.
If the openHAB server cannot resolve `.local` names, the binding uses the last known IP address of the controller.
For a controller that was not discovered, enter its IP address in that case, and give the controller a fixed address in the router.

### Status Reports

The controller reports door changes to openHAB with UDP messages, so they show up immediately.
The binding sets this up by itself: it tells the controller to send its reports to the openHAB server, to UDP port 50904.
Several controllers can use the same port.

Usually nothing needs to be configured. Only in these cases:

- If the openHAB server has a firewall, allow incoming UDP messages on port 50904.
- If openHAB runs in Docker, use host networking, or publish port 50904/udp.
- If another program uses port 50904, the binding uses a free port instead and logs a warning. Set `udpPort` to a free port to keep it fixed.
- The reports go to the primary address of openHAB in Settings, Network Settings. If none is set, the binding uses the address of the openHAB server that the controller can reach.

The binding only accepts reports sent from the address of the controller, so the controller must be reached directly, not through a proxy or address translation.

The binding also asks the controller for its status every 5 minutes.
While the controller cannot be reached, the binding tries again after 30 seconds, and then at increasing intervals of up to 5 minutes.
With each successful status request it registers for reports again, so the reports resume after the controller restarts.

A controller sends its reports to one address only.
If another system, such as a second openHAB instance, also registers for them, the two take the reports from each other at every status request, and each receives door changes only part of the time.

## Channels

The Thing has a `controller` channel group, and a `door1`, `door2` and `door3` group for each door that is connected to the controller.

### Controller Channels

| Channel                     | Type         | Read/Write | Description                                                                                                   |
|-----------------------------|--------------|------------|---------------------------------------------------------------------------------------------------------------|
| `controller#night-mode`     | Switch       | R          | ON when night mode is enabled in the TailWind app                                                             |
| `controller#led-brightness` | Dimmer       | RW         | Brightness of the status LED                                                                                  |
| `controller#rssi`           | Number:Power | R          | Strength of the Wi-Fi signal the controller receives                                                          |
| `controller#server-monitor` | Switch       | RW         | ON: the controller reboots itself when it loses the TailWind cloud server. Firmware 10.61 or later. Advanced. |
| `controller#command`        | String       | W          | `identify` blinks the LED, `reboot` restarts the controller. Firmware 10.10 or later. Advanced.               |

Turn `server-monitor` OFF to use the controller on a network without internet access.

### Door Channels

These are the channels of door 1.
The channels of the other doors start with `door2#` and `door3#`.

| Channel          | Type   | Read/Write | Description                                                                              |
|------------------|--------|------------|------------------------------------------------------------------------------------------|
| `door1#status`   | String | R          | `open` or `close`                                                                        |
| `door1#control`  | String | RW         | Send `open`, `partial` or `close` to move the door. The state is the status of the door. |
| `door1#locked`   | Switch | R          | ON when the controller has locked the door, so that it does not accept commands          |
| `door1#disabled` | Switch | R          | ON when the door is disabled in the TailWind app. Advanced.                              |

The controller only moves a door that is not already in the requested state.
`partial` opens the door for the partial open time of the Thing configuration.
A partially open door has the status `open`, so the controller ignores `open` and `partial` for it.
To open it fully, close it first.

### Door Names

To give a door a name, such as "Left Door", set it as the label of the items that are linked to the door's channels.
In the UI, enter the name as the label when you link a new item, or use "Add Equipment to Model" on the Thing and rename the door groups.
In an `.items` file, put the name in the label, as in the [Full Example](#full-example).
The Thing itself always shows Door 1, Door 2 and Door 3.

### Signal Strength Unit

The `rssi` channel is in dBm.
An item that is created in the UI gets this unit automatically.
An item in an `.items` file needs `unit="dBm"`, as in the [Full Example](#full-example), or it shows the value in watts.

## Troubleshooting

| Problem                                                       | Cause and solution                                                                                                                           |
|---------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| The Thing is offline with a configuration error about the key | The Local Control Key is wrong. Copy it again from the TailWind web dashboard.                                                               |
| The Thing is offline with a communication error               | The controller cannot be reached at `hostname`. Check the address, and use the IP address if `.local` names cannot be resolved.              |
| Door changes appear only after several minutes                | The status reports do not arrive. Check the firewall and Docker setup in Status Reports, and that no other system registers for the reports. |

For more detail in the log, run `log:set DEBUG org.openhab.binding.tailwind` in the openHAB console.

## Upgrading from the Marketplace Version

Earlier versions of this binding were installed from the openHAB Marketplace.
This version has a new Thing type, `iq3`, and new channel and parameter names, so existing Things must be created again:

1. Uninstall the TailWind binding from the Marketplace, or remove its jar from the `addons` folder.
1. Delete the old TailWind Thing (`tailwind:iQ3:...`).
1. Remove any `tailwind:iQ3` entry that is left in the Inbox. Do not use "Add All", because it would add the old entry too.
1. Install this binding, add the controller from the Inbox and enter its Local Control Key.
1. Link the items to the new channels. The names changed like this:

| Old                                        | New                                    |
|--------------------------------------------|----------------------------------------|
| `webServerAddress`                         | `hostname`                             |
| `authToken`                                | `token`                                |
| `doorOnePartialOpen` (and two, three)      | `door1PartialOpenTime` (and 2, 3)      |
| `doorCount`                                | not needed, doors are detected         |
| `doorOneName` (and two, three)             | the label of the items, see Door Names |
| `openHabHostAddress`                       | not needed, see Status Reports         |
| `controller#nightModeEnabled` (Number)     | `controller#night-mode` (Switch)       |
| `controller#ledBrightness`                 | `controller#led-brightness`            |
| `controller#routerRssi` (Number)           | `controller#rssi` (Number:Power, dBm)  |
| `controller#serverMonitor`                 | `controller#server-monitor`            |
| `controller#supportCommand`                | `controller#command`                   |
| `controller#productID`, `#firmwareVersion` | Thing properties                       |
| `doorOne#status`                           | `door1#status`                         |
| `doorOne#openClose`                        | `door1#control`                        |
| `doorOne#lockup` (Number)                  | `door1#locked` (Switch)                |
| `doorOne#disabled` (Number)                | `door1#disabled` (Switch)              |

## Full Example

### Thing Configuration

```java
Thing tailwind:iq3:garage "Garage Controller" [ hostname="tailwind-30aea4801880.local", token="123456", door1PartialOpenTime=3 ]
```

### Item Configuration

```java
String       Garage_Door_Status   "Left Door [%s]"            { channel="tailwind:iq3:garage:door1#status" }
String       Garage_Door_Control  "Left Door"                 { channel="tailwind:iq3:garage:door1#control" }
Dimmer       Garage_Led           "Controller LED"            { channel="tailwind:iq3:garage:controller#led-brightness" }
Switch       Garage_Night_Mode    "Night Mode"                { channel="tailwind:iq3:garage:controller#night-mode" }
Number:Power Garage_Rssi          "Wi-Fi Signal [%d dBm]"     { channel="tailwind:iq3:garage:controller#rssi", unit="dBm" }
```

### Sitemap Configuration

```perl
sitemap garage label="Garage" {
    Text item=Garage_Door_Status
    Switch item=Garage_Door_Control mappings=[open="Open", partial="Partial", close="Close"]
    Slider item=Garage_Led
    Text item=Garage_Rssi
}
```
