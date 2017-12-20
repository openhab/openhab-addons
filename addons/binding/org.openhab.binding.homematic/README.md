# Homematic Binding

This is the binding for the [eQ-3 Homematic Solution](http://www.eq-3.de/).
This binding allows you to integrate, view, control and configure all Homematic devices in the openHAB environment.

## Supported Bridges

All gateways which provides the Homematic BIN- or XML-RPC API:
*   CCU 1+2
*   [Homegear](https://www.homegear.eu)
*   [piVCCU](https://github.com/alexreinert/piVCCU)
*   [YAHM](https://github.com/leonsio/YAHM)
*   [Windows BidCos service](http://www.eq-3.de/downloads.html?kat=download&id=125)
*   [OCCU](https://github.com/eq-3/occu)

The Homematic IP Access Point does not support this API and can't be used with this binding.
But you can control Homematic IP devices with a CCU2 with at least firmware 2.17.15.

These ports are used by the binding by default to communicate **TO** the gateway:  
*   RF components: 2001
*   WIRED components: 2000
*   HMIP components: 2010
*   CUxD: 8701
*   TclRegaScript: 8181
*   Groups: 9292

And **FROM** the gateway to openHab:
*   XML-RPC: 9125
*   BIN-RPC: 9126

**Note:** The binding tries to identify the gateway with XML-RPC and uses henceforth:

*   **CCU**
    *   **RF**: BIN-RPC
    *   **WIRED**: BIN-RPC
    *   **HMIP**: XML-RPC
    *   **CUxD**: BIN-RPC (CUxD version >= 1.6 required)
    *   **Groups**: XML-RPC
*   **Homegear**
    *   BIN-RPC
*   **Other**
    *   XML-RPC

## Supported Things

All devices connected to a Homematic gateway.
All required openHAB metadata are generated during device discovery.
With Homegear or a CCU, variables and scripts are supported too.

## Discovery

Gateway discovery is only available for Homegear, you need at least 0.6.x for gateway discovery. For all other gateways you have to manually add a bridge in a things file.  
Device discovery is supported for all gateways.

The binding has a gateway type autodetection, but sometimes a gateway does not clearly notify the type.
If you are using a YAHM for example, you have to manually set the gateway type in the bride configuration to CCU.  

If autodetection can not identify the gateway, the binding uses the default gateway implementation.
The difference is, that variables, scripts and device names are not supported, everything else is the same.

## Bridge Configuration

There are several settings for a bridge:
-   **gatewayAddress** (required)  
Network address of the Homematic gateway

-   **gatewayType**  
Hint for the binding to identify the gateway type (auto|ccu|noccu) (default = auto).

-   **callbackHost**  
Callback network address of the openHAB server, default is auto-discovery

-   **callbackPort DEPRECATED, use binCallbackPort and xmlCallbackPort**  
Callback port of the openHAB server, default is 9125 and counts up for each additional bridge

-   **xmlCallbackPort**  
Callback port of the XML-RPC openHAB server, default is 9125 and counts up for each additional bridge

-   **binCallbackPort**  
Callback port of the BIN-RPC openHAB server, default is 9126 and counts up for each additional bridge

-   **aliveInterval DEPRECATED, not necessary anymore**  
The interval in seconds to check if the communication with the Homematic gateway is still alive. If no message receives from the Homematic gateway, the RPC server restarts (default = 300)

-   **reconnectInterval DEPRECATED, not necessary anymore**  
The interval in seconds to force a reconnect to the Homematic gateway, disables aliveInterval! (0 = disabled, default = disabled).  
If you have no sensors which sends messages in regular intervals and/or you have low communication, the aliveInterval may restart the connection to the Homematic gateway to often. The reconnectInterval disables the aliveInterval and reconnects after a fixed period of time.
Think in hours when configuring (one hour = 3600)

-   **timeout**  
The timeout in seconds for connections to a Homematic gateway (default = 15)

-   **socketMaxAlive**  
The maximum lifetime of a pooled socket connection to the Homematic gateway in seconds (default = 900)

-   **rfPort**  
The port number of the RF daemon (default = 2001)

-   **wiredPort**  
The port number of the HS485 daemon (default = 2000)

-   **hmIpPort**  
The port number of the HMIP server (default = 2010)

-   **cuxdPort**  
The port number of the CUxD daemon (default = 8701)

The syntax for a bridge is:

```
homematic:bridge:NAME
```

-   **homematic** the binding id, fixed
-   **bridge** the type, fixed
-   **name** the name of the bridge

#### Example

-   minimum configuration

```
Bridge homematic:bridge:ccu [ gatewayAddress="..."]
```

-   with callback settings

```
Bridge homematic:bridge:ccu [ gatewayAddress="...", callbackHost="...", callbackPort=... ]
```

-   multiple bridges

```
Bridge homematic:bridge:lxccu [ gatewayAddress="..."]
Bridge homematic:bridge:occu  [ gatewayAddress="..."]
```

## Thing Configuration

Things are all discovered automatically, you can handle them in PaperUI.  

If you really like to manually configure a thing:

```
Bridge homematic:bridge:ccu [ gatewayAddress="..." ]
{
  Thing HM-LC-Dim1T-Pl-2    JEQ0999999
}
```

The first parameter after Thing is the device type, the second the serial number.
If you are using Homegear, you have to add the prefix ```HG-``` for each type.
This is necessary, because the Homegear devices supports more datapoints than Homematic devices.

```
  Thing HG-HM-LC-Dim1T-Pl-2     JEQ0999999
```

As additional parameters you can define a name and a location for each thing.
The Name will be used to identify the Thing in the Paper UI lists, the Location will be used in the Control section of PaperUI to sort the things.

```
  Thing HG-HM-LC-Dim1T-Pl-2     JEQ0999999  "Name"  @  "Location"
```

All channels have two configs:
*   **delay**: delays transmission of a command **to** the Homematic gateway, duplicate commands are filtered out
*   **receiveDelay**: delays a received event **from** the Homematic gateway, duplicate events are filtered out (OH 2.2)

The receiveDelay is handy for dimmers and rollershutters for example.
If you have a slider in a UI and you move this slider to a new position, it jumps around because the gateway sends multiple events with different positions until the final has been reached.
If you set the ```receiveDelay``` to some seconds, these events are filtered out and only the last position is distributed to openHab.
The disadvantage is of course, that all events for this channel are delayed.

```
  Thing HM-LC-Dim1T-Pl-2    JEQ0999999 "Name"  @  "Location" {
      Channels:
          Type HM-LC-Dim1T-Pl-2_1_level : 1#LEVEL [
              delay = 0,
              receiveDelay = 4
          ]
  }
```

The Type is the device type, channel number and lowercase channel name separated with a underscore.

### Items

In the items file, you can map the datapoints, the syntax is:

```
homematic:TYPE:BRIDGE:SERIAL:CHANNELNUMBER#DATAPOINTNAME
```

*   **homematic:** the binding id, fixed  
*   **type:** the type of the Homematic device  
*   **bridge:** the name of the bridge  
*   **serial:** the serial number of the Homematic device  
*   **channelnumber:** the channel number of the Homematic datapoint
*   **datapointname:** the name of the Homematic datapoint

```
Switch  RC_1  "Remote Control Button 1" { channel="homematic:HM-RC-19-B:ccu:KEQ0099999:1#PRESS_SHORT" }
Dimmer  Light "Light [%d %%]"           { channel="homematic:HM-LC-Dim1T-Pl-2:ccu:JEQ0555555:1#LEVEL" }
```

**Note:** don't forget to add the ```HG-``` type prefix for Homegear devices

## Virtual device and datapoints

The binding supports one virtual device and some virtual datapoints.
Virtual datapoints are generated by the binding and provides special functionality.

#### GATEWAY-EXTRAS

The GATEWAY-EXTRAS is a virtual device which contains a switch to reload all values from all devices and also a switch to put the gateway in the install mode to add new devices.
If the gateway supports variables and scripts, you can handle them with this device too.
The type is generated: GATEWAY-EXTRAS-&lsqb;BRIDGE_ID&rsqb;.
Example: bridgeId=ccu, type=GATEWAY-EXTRAS-CCU  
Address: fixed GWE00000000

#### RELOAD_ALL_FROM_GATEWAY

A virtual datapoint (Switch) to reload all values for all devices, available in channel 0 in GATEWAY-EXTRAS

#### RELOAD_RSSI

A virtual datapoint (Switch) to reload all rssi values for all devices, available in channel 0 in GATEWAY-EXTRAS

#### RSSI

A virtual datapoint (Number) with the unified RSSI value from RSSI_DEVICE and RSSI_PEER, available in channel 0 for all wireless devices

#### INSTALL_MODE

A virtual datapoint (Switch) to start the install mode on the gateway, available in channel 0 in GATEWAY-EXTRAS

#### INSTALL_MODE_DURATION

A virtual datapoint (Integer) to hold the duration for the install mode, available in channel 0 in GATEWAY-EXTRAS (max 300 seconds, default = 60)

#### DELETE_MODE

A virtual datapoint (Switch) to remove the device from the gateway, available in channel 0 for each device. Deleting a device is only possible if DELETE_DEVICE_MODE is not LOCKED

#### DELETE_DEVICE_MODE

A virtual datapoint (Enum) to configure the device deletion with DELETE_MODE, available in channel 0 for each device
*   **LOCKED:** (default) device can not be deleted
*   **RESET:** device is reset to factory settings before deleting
*   **FORCE:** device is also deleted if it is not reachable
*   **DEFER:** if the device can not be reached, it is deleted at the next opportunity

**Note:** if you change the value and don't delete the device, the virtual datapoints resets to LOCKED after 30 seconds

#### ON_TIME_AUTOMATIC

A virtual datapoint (Number) to automatically set the ON_TIME datapoint before the STATE or LEVEL datapoint is sent to the gateway, available for all devices which supports the ON_TIME datapoint.  
This is usefull to automatically turn off the datapoint after the specified time.

#### DISPLAY_OPTIONS

A virtual datapoint (String) to control the display of a 19 button Homematic remote control (HM-RC-19), available on channel 18  

The remote control display is limited to five characters, a longer text is truncated.  

You have several additional options to control the display.
*   BEEP _(TONE1, TONE2, TONE3)_ - let the remote control beep
*   BACKLIGHT _(BACKLIGHT_ON, BLINK_SLOW, BLINK_FAST)_ - control the display backlight
*   UNIT _(PERCENT, WATT, CELSIUS, FAHRENHEIT)_ - display one of these units
*   SYMBOL _(BULB, SWITCH, WINDOW, DOOR, BLIND, SCENE, PHONE, BELL, CLOCK, ARROW_UP, ARROW_DOWN)_ - display symbols, multiple symbols possible

You can combine any option, they must be separated by a comma.
If you specify more than one option for BEEP, BACKLIGHT and UNIT, only the first one is taken into account and all others are ignored. For SYMBOL you can specify multiple options.

**Examples:**  

Assumed you mapped the virtual datapoint to a String item called Display_Options   

```
String Display_Options "Display_Options" { channel="homematic:HM-RC-19-B:ccu:KEQ0099999:18#DISPLAY_OPTIONS" }
```

show message TEST:

```
smarthome send Display_Options "TEST"
```

show message TEXT, beep once and turn backlight on:

```
smarthome send Display_Options "TEXT, TONE1, BACKLIGHT_ON"
```

show message 15, beep once, turn backlight on and shows the celsius unit:

```
smarthome send Display_Options "15, TONE1, BACKLIGHT_ON, CELSIUS"
```

show message ALARM, beep three times, let the backlight blink fast and shows a bell symbol:

```
smarthome send Display_Options "ALARM, TONE3, BLINK_FAST, BELL"
```

Duplicate options: TONE3 is ignored, because TONE1 is specified previously.

```
smarthome send Display_Options "TEXT, TONE1, BLINK_FAST, TONE3"
```

#### DISPLAY_SUBMIT

Adds multiple virtual datapoints to the HM-Dis-WM55 device to easily send colored text and icons to the display

Example: Display text at line 1,3 and 5 when the bottom button on the display is pressed

-   Items

```
String Display_line_1   "Line 1"    { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_LINE_1" }
String Display_line_3   "Line 3"    { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_LINE_3" }
String Display_line_5   "Line 5"    { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_LINE_5" }

String Display_color_1  "Color 1"   { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_COLOR_1" }
String Display_color_3  "Color 3"   { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_COLOR_3" }
String Display_color_5  "Color 5"   { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_COLOR_5" }

String Display_icon_1   "Icon 1"    { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_ICON_1" }
String Display_icon_3   "Icon 3"    { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_ICON_3" }
String Display_icon_5   "Icon 5"    { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_ICON_5" }

Switch Button_bottom    "Button"    { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#PRESS_SHORT" }
Switch Display_submit   "Submit"    { channel="homematic:HM-Dis-WM55:ccu:NEQ0123456:1#DISPLAY_SUBMIT" }
```

-   Rule

```
rule "Display Test"
when
    Item Button_bottom received update ON
then
    sendCommand(Display_line_1, "Line 1")
    sendCommand(Display_line_3, "Line 3")
    sendCommand(Display_line_5, "Line 5")

    sendCommand(Display_icon_1, "NONE")
    sendCommand(Display_icon_3, "OPEN")
    sendCommand(Display_icon_5, "INFO")

    sendCommand(Display_color_1, "NONE")
    sendCommand(Display_color_3, "RED")
    sendCommand(Display_color_5, "BLUE")

    sendCommand(Display_submit, ON)
end
```

#### PRESS

A virtual datapoint (String) to simulate a key press, available on all channels that contains PRESS_ datapoints.  
Available values: SHORT, LONG, LONG_RELEASE

Example: to capture a key press on the 19 button remote control in a rule

```
rule "example trigger rule"
when
    Channel 'homematic:HM-RC-19-B:ccu:KEQ0012345:1#PRESS' triggered SHORT
then
    ...
end
```

### Troubleshooting

**SHORT & LONG_PRESS events of push buttons do not occur on the event bus**  

It seems buttons like the HM-PB-2-WM55 do just send these kind of events to the CCU if they are mentioned in a CCU program.
A simple workaround to make them send these events is, to create a program (rule inside the CCU) that does just have a "When" part and no "Then" part, in this "When" part each channel needs to be mentioned at least once.
As the HM-PB-2-WM55 for instance has two channels, it is enough to mention the SHORT_PRESS event of channel 1 & 2.
The LONG_PRESS events will work automatically as they are part of the same channels.
After the creation of this program, the button device will receive configuration data from the CCU which have to be accepted by pressing the config-button at the back of the device.

**INSTALL_TEST**  

If a button is still not working and you do not see any PRESS_LONG / SHORT in your log file (loglevel DEBUG), it could be because of enabled security.
Try to disable security of your buttons in the HomeMatic Web GUI and try again.
If you can't disable security try to use key INSTALL_TEST which gets updated to ON for each key press

**-1 Failure**  

A device may return this failure while fetching the datapoint values.
I've tested pretty much but i did not found the reason. The HM-ES-TX-WM device for example always returns this failure, it's impossible with the current CCU2 firmware (2.17.15) to fetch the values.
I've implemented two workarounds, if a device returns the failure, workaround one is executed, if the device still returns the failure, workaround two is executed.
This always works in my tests, but you may see a OFFLINE, ONLINE cycle for the device.  
Fetching values is only done at startup or if you trigger a REFRESH. I hope this will be fixed in one of the next CCU firmwares.  
With [Homegear](https://www.homegear.eu) everything works as expected.

**No variables and scripts in GATEWAY-EXTRAS**  

The gateway autodetection of the binding can not clearly identify the gateway and falls back to the default implementation.
Use the ```gatewayType=ccu``` config to force the binding to use the CCU implementation.

**Variables out of sync**  

The CCU only sends a event if a datapoint of a device has changed.
There is (currently) no way to receive a event automatically when a variable has changed.
To reload all variable values, send a REFRESH command to any variable.  
e.g you have a item linked to a variable with the name Var_1  
In the console:

```
smarthome:send Var_1 REFRESH
```

In scripts:

```
import org.eclipse.smarthome.core.types.RefreshType
...
sendCommand(Var_1, RefreshType.REFRESH)
```

**Note:** adding new and removing deleted variables from the GATEWAY-EXTRAS Thing is currently not supported. You have to delete the Thing, start a scan and add it again.

### Debugging and Tracing

If you want to see what's going on in the binding, switch the loglevel to DEBUG in the Karaf console

```
log:set DEBUG org.openhab.binding.homematic
```

If you want to see even more, switch to TRACE to also see the gateway request/response data

```
log:set TRACE org.openhab.binding.homematic
```

Set the logging back to normal

```
log:set INFO org.openhab.binding.homematic
```

To identify problems, i need a full startup TRACE log

```
stop org.openhab.binding.homematic
log:set TRACE org.openhab.binding.homematic
start org.openhab.binding.homematic
```
