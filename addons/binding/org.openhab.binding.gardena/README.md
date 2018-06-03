# Gardena Binding

This is the binding for [Gardena Smart Home](http://www.gardena.com/de/rasenpflege/smartsystem/).
This binding allows you to integrate, view and control Gardena Smart Home devices in the openHAB environment.

## Supported Things

Devices connected to Gardena Smart Home, currently:

| Thing type               | Name                  |
|--------------------------|-----------------------|
| bridge                   | smart Home Gateway    |
| mower                    | smart Sileno(+) Mower |
| watering_computer        | smart Water Control   |
| sensor                   | smart Sensor          |
| electronic_pressure_pump | smart Pressure Pump   |
| power                    | smart Power Plug      |

The schedules are not yet integrated!

## Discovery

An account must be specified, all things for an account are discovered automatically.

## Account Configuration

There are several settings for an account:

| Name                  | Required | Description                                                                            |
|-----------------------|----------|----------------------------------------------------------------------------------------|
| **email**             | yes      | The email address for logging into the Gardena Smart Home                              |
| **password**          | yes      | The password for logging into the Gardena Smart Home                                   |
| **sessionTimeout**    | no       | The timeout in minutes for a session to Gardena Smart Home (default = 30)              |
| **connectionTimeout** | no       | The timeout in seconds for connections to Gardena Smart Home (default = 10)            |
| **refresh**           | no       | The interval in seconds for refreshing the data from Gardena Smart Home (default = 60) |

**Example**

### Things

Minimal Thing configuration:

```java
Bridge gardena:account:home [ email="...", password="..." ]
```

Configuration with refresh:

```java
Bridge gardena:account:home [ email="...", password="...", refresh=30 ]
```

Configuration of multiple bridges:

```java
Bridge gardena:account:home1 [ email="...", password="..." ]
Bridge gardena:account:home2 [ email="...", password="..." ]
```

Once a connection to an account is established, connected Things are discovered automatically.

Alternatively, you can manually configure a Thing:

```perl
Bridge gardena:account:home [ email="...", password="..." ]
{
  Thing mower myMower [ deviceId="c81ad682-6e45-42ce-bed1-6b4eff5620c8" ]
}
```

## Items

In the items file, you can link items to channels of your Things:

```java
Number Battery_Level "Battery [%d %%]" {channel="gardena:mower:home:myMower:battery#level"}
```

## Sensor refresh

You can send a REFRESH command to items linked to these Sensor channels:

- ambient_temperature#temperature
- soil_temperature#temperature
- humidity#humidity
- light#light

In the console:

```shell
smarthome:send ITEM_NAME REFRESH
```

In scripts:

```shell
import org.eclipse.smarthome.core.types.RefreshType
...
sendCommand(ITEM_NAME, RefreshType.REFRESH)
```

## Examples

```shell
// smart Water Control
Switch  Watering_Valve      "Valve"             { channel="gardena:watering_computer:home:myvalve:outlet#valve_open"}
Number  Watering_Duration   "Duration [%d min]" { channel="gardena:watering_computer:home:wasserhahn:outlet#button_manual_override_time"}

// smart Power Plug
String Power_Timer          "Power Timer [%s]"  { channel="gardena:power:home:myPowerplug:power#power_timer"}
```

```shell
Watering_Valve.sendCommand(30) // 30 minutes
Watering_Duration.sendCommand(ON)

Power_Timer.sendCommand("on")
Power_Timer.sendCommand("off")
Power_Timer.sendCommand("180") // on for 180 seconds
```

### Debugging and Tracing

If you want to see what's going on in the binding, switch the loglevel to TRACE in the Karaf console

```shell
log:set TRACE org.openhab.binding.gardena
```

Set the logging back to normal

```shell
log:set INFO org.openhab.binding.gardena
```

**Note:** The Online/Offline status is not always valid. I'm using the ```connection_status``` property Gardena sends for each device, but it seems not to be very reliable.
My watering control for example shows offline, but it is still working.
I have to press the button on the device, then the status changed to online.
My mower always shows online, regardless of whether it is switched on or off.
This is not a binding issue, it must be fixed by Gardena.

When the binding sends a command to a device, it communicates only with the Gardena online service.
It has not control over, whether the command is sent from the online service via your gateway to the device.
It's the same as if you send the command in the Gardena App.
