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
A discovered controller still needs its Local Control Key before it can go online.

## Thing Configuration

| Parameter              | Type    | Required | Default | Description                                                                      |
|------------------------|---------|----------|---------|----------------------------------------------------------------------------------|
| `hostname`             | text    | yes      |         | IP address or host name of the controller, such as `tailwind-30aea4801880.local` |
| `token`                | text    | yes      |         | The 6-digit Local Control Key of the controller                                  |
| `door1PartialOpenTime` | decimal | no       | 2.5     | Seconds that door 1 runs when it is opened partially (0.5 to 15). Advanced.      |
| `door2PartialOpenTime` | decimal | no       | 2.5     | Seconds that door 2 runs when it is opened partially (0.5 to 15). Advanced.      |
| `door3PartialOpenTime` | decimal | no       | 2.5     | Seconds that door 3 runs when it is opened partially (0.5 to 15). Advanced.      |

The Local Control Key is shown on the Local Control Key page of the [TailWind web dashboard](https://web.gotailwind.com).
See the [token section](https://github.com/Scott--R/Tailwind_Local_Control_API#2-token) of the API document for details.

The host name of a controller is `tailwind-` followed by its MAC address and `.local`.
Use the IP address if the openHAB server cannot resolve `.local` names, and give the controller a fixed address in that case.

### Status Reports

The controller reports door changes to openHAB with UDP messages, so they show up immediately.
For this the binding tells the controller to send its reports to the primary IPv4 address of openHAB, which is set in the network settings, and to a free UDP port that the binding opens.
A firewall on the openHAB server must allow incoming UDP messages from the controller.

A controller sends its reports to one address only.
If another system, such as a second openHAB instance, also registers for them, only the one that registered last receives them.

The binding also asks the controller for its status every 5 minutes.
While the controller cannot be reached, the binding tries again after 30 seconds, and then at increasing intervals of up to 5 minutes.

## Channels

The Thing has a `controller` channel group, and a `door1`, `door2` and `door3` group for each door that is connected to the controller.

### Controller Channels

| Channel                     | Type         | Read/Write | Description                                                                                                   |
|-----------------------------|--------------|------------|---------------------------------------------------------------------------------------------------------------|
| `controller#night-mode`     | Switch       | R          | ON when night mode is enabled in the TailWind app                                                             |
| `controller#led-brightness` | Dimmer       | RW         | Brightness of the status LED                                                                                  |
| `controller#rssi`           | Number:Power | R          | Strength of the Wi-Fi signal the controller receives. Advanced.                                               |
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
| `door1#locked`   | Switch | R          | ON when the controller has locked the door. Advanced.                                    |
| `door1#disabled` | Switch | R          | ON when the door is disabled in the TailWind app. Advanced.                              |

The controller only moves a door that is not already in the requested state.
`partial` opens the door for the partial open time of the Thing configuration.

## Full Example

### Thing Configuration

```java
Thing tailwind:iq3:garage "Garage Controller" [ hostname="192.168.1.50", token="123456", door1PartialOpenTime=3 ]
```

### Item Configuration

```java
String Garage_Door_Status   "Garage Door [%s]"      { channel="tailwind:iq3:garage:door1#status" }
String Garage_Door_Control  "Garage Door"           { channel="tailwind:iq3:garage:door1#control" }
Dimmer Garage_Led           "Controller LED"        { channel="tailwind:iq3:garage:controller#led-brightness" }
Switch Garage_Night_Mode    "Night Mode"            { channel="tailwind:iq3:garage:controller#night-mode" }
```

### Sitemap Configuration

```perl
sitemap garage label="Garage" {
    Text item=Garage_Door_Status
    Switch item=Garage_Door_Control mappings=[open="Open", partial="Partial", close="Close"]
    Slider item=Garage_Led
}
```
