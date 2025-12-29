# pilight Binding

The pilight binding allows openHAB to communicate with a [pilight](https://www.pilight.org/) instance running pilight version 6.0 or greater.

> pilight is a free, open-source, full-fledged domotica solution that runs on a Raspberry Pi, HummingBoard, BananaPi,
> Radxa, but also on *BSD and various Linux distributions (tested on Arch, Ubuntu, and Debian).
> It's open source and freely available for anyone.
> pilight works with a great deal of devices and is frequency independent.
> Therefore, it can control devices working at 315MHz, 433MHz, 868MHz, etc.
> Support for these devices is dependent on the community, because we as developers don't own them all.

pilight is a cheap way to control 'Click On Click Off' devices.
It started as an application for the Raspberry Pi (using the GPIO interface), but it's also possible now to connect it to any other PC using an Arduino Nano.
You will need a cheap 433MHz transceiver in both cases.
See the [Pilight manual](https://manual.pilight.org/electronics/wiring.html) for more information.

## Supported Things

| Thing     | Type   | Description                                                                |
|-----------|--------|----------------------------------------------------------------------------|
| `bridge`  | Bridge | Pilight bridge required for the communication with the pilight daemon.     |
| `contact` | Thing  | Pilight contact (read-only).                                               |
| `dimmer`  | Thing  | Pilight dimmer.                                                            |
| `switch`  | Thing  | Pilight switch.                                                            |
| `generic` | Thing  | Pilight generic device for which you have to add the channels dynamically. |

## Binding Configuration

### `bridge` Thing

A `bridge` is required for the communication with a pilight daemon. Multiple pilight instances are supported by creating
different pilight `bridge` things.

The `bridge` requires the following configuration parameters:

| Parameter Label      | Parameter ID        | Description                                                                                                                                                                             | Required |
|----------------------|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| IP Address           | ipAddress           | Host name or IP address of the pilight daemon                                                                                                                                           | yes      |
| Port                 | port                | Port number on which the pilight daemon is listening. Default: 5000                                                                                                                     | yes      |
| Delay                | delay               | Delay (in milliseconds) between consecutive commands. Recommended value without band-pass filter: 1000. Recommended value with band-pass filter: somewhere between 200-500. Default: 500 | no       |
| Background Discovery | backgroundDiscovery | Whether pilight devices for this Bridge should automatically be discovered. Default: true                                                                                               | no       |

Important: You must explicitly configure the port in the pilight daemon config; otherwise, a random port will be used and the binding will not be able to connect.

### `contact`, `dimmer`, `switch`, `generic` Things

These things have all one required parameter:

| Parameter Label | Parameter ID | Description            | Required |
|-----------------|--------------|------------------------|----------|
| Name            | name         | Name of pilight device | yes      |

## Channels

The `bridge` Thing has no channels.

The `contact`, `dimmer` and `switch` things all have one channel:

| Thing     | Channel  | Type    | Description             |
|-----------|----------|---------|-------------------------|
| `contact` | state    | Contact | State of the contact    |
| `dimmer`  | dimlevel | Dimmer  | Dim level of the dimmer |
| `switch`  | state    | Switch  | State of the switch     |

The `generic` Thing has no fixed channels, so you have to add them manually.
Currently, only String and Number channels are supported.

## Auto Discovery

### Bridge Auto Discovery

The pilight daemon implements an SSDP interface, which can be used to search for running pilight daemon instances by sending an SSDP request via multicast UDP (this mechanism may only work if the [standalone mode](https://manual.pilight.org/configuration/settings.html#standalone) in the pilight daemon is disabled).
After loading the binding, this bridge discovery is automatically run and scheduled to scan for bridges every 10 minutes.

### Device Auto Discovery

After a `bridge` Thing has been configured in openHAB, it automatically establishes a connection between the pilight daemon and openHAB.
As soon as the bridge is connected, the devices configured in the pilight daemon are automatically found via autodiscovery in the background (or via a manually triggered discovery) and are displayed in the inbox to easily create things from them.

## Full Example

### pilight.things

```java
Bridge pilight:bridge:raspi "Pilight Daemon raspi" [ ipAddress="192.168.1.1", port=5000, backgroundDiscovery=false ] {
        Thing switch office "Office" [ name="office" ]
        Thing dimmer piano "Piano"  [ name="piano" ]
        Thing generic weather "Weather"  [ name="weather" ] {
            Channels:
              Type number : temperature "Temperature" [ property="temperature"]
              Type number : humidity "Humidity" [ property="humidity"]
        }
}
```

### pilight.items

```java
Switch office_switch "Büro" { channel="pilight:switch:raspi:office:state" }
Dimmer piano_light "Klavier [%.0f %%]" { channel="pilight:dimmer:raspi:piano:dimlevel" }
Number weather_temperature  "Aussentemperatur [%.1f °C]" <temperature>  { channel="pilight:generic:raspi:weather:temperature" }
Number weather_humidity "Feuchtigkeit [%.0f %%]" <humidity> { channel="pilight:generic:raspi:weather:humidity" }

```

### fragment.sitemap

```perl
Switch item=office_switch
Slider item=piano_light
Text item=weather_temperature
Text item=weather_humidity
```
