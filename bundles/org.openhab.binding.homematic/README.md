# Homematic Binding

This is the binding for the [eQ-3 Homematic Solution](https://eq-3.de/).
This binding allows you to integrate, view, control and configure all Homematic devices in openHAB.

## Configuration of the CCU

Under `Home page > Settings > Control panel` with the menu `Configure Firewall` the Firewall configurations have to be adjusted.
The CCU has to be configured to have "XML-RPC" set to "Full Access" or "Restricted access".
Also the "Remote Homematic-Script API" has to be set to "Full Access" or "Restricted access".
When the option "Restricted access" is used, some ports have to be added to the "Port opening" list.

```text
2000;
2001;
2010;
8181;
8701;
9292;
```

Also the IP of the device running openHAB has to be set to the list of "IP addresses for restricted access".

Also under `Home page > Settings > Control panel` with the menu `Security` the option `Authentication` has to be disabled if the option 'useAuthentication' is not set.
This option may be enabled if the option 'useAuthentication' is set and BIN-RPC is not used.
In this case, a user and password must be created.
This can be done under `Home page > Settings > Control panel` with the menu `User management`.
This can be done under `Home page > Settings > Control Panel` in the `User Management` menu.
The new user should have the following configuration:

- User name - button for login: No
- Permission level: User
- Expert mode not visible: Yes
- Automatically confirm the device message: Yes

The user and password must then be entered in the 'Username' and 'Password' settings.

If this is not done the binding will not be able to connect to the CCU and the CCU Thing will stay uninitialized and sets a timeout exception or a authentication error

```text
xxx-xx-xx xx:xx:xx.xxx [hingStatusInfoChangedEvent] - - 'homematic:bridge:xxx' changed from INITIALIZING to OFFLINE (COMMUNICATION_ERROR): java.net.SocketTimeoutException: Connect Timeout
```

## Supported Bridges

All gateways which provides the Homematic BIN- or XML-RPC API:

- CCU 1, 2 and 3
- [RaspberryMatic](https://github.com/jens-maus/RaspberryMatic)
- [Homegear](https://homegear.eu) (>= 0.8.0-1988)
- [piVCCU](https://github.com/alexreinert/piVCCU)
- [YAHM](https://github.com/leonsio/YAHM)
- [Windows BidCos service](https://eq-3.de/service/downloads.html?kat=download&id=125) (included in "LAN Usersoftware" download)
- [OCCU](https://github.com/eq-3/occu)

The Homematic IP Access Point **does not support** this API and and can't be used with this binding.

Homematic IP support:

- CCU2 with at least firmware 2.17.15
- [RaspberryMatic](https://github.com/jens-maus/RaspberryMatic) with the [HM-MOD-RPI-PCB](https://www.elv.de/homematic-funkmodul-fuer-raspberry-pi-bausatz.html) or [RPI-RF-MOD](https://www.elv.de/homematic-funk-modulplatine-fuer-raspberry-pi-3-rpi-rf-mod-komplettbausatz.html) RF module
- [piVCCU](https://github.com/alexreinert/piVCCU)
- [YAHM](https://github.com/leonsio/YAHM)

These ports are used by the binding by default to communicate **TO** the gateway:

- RF components: 2001
- WIRED components: 2000
- HMIP components: 2010
- CUxD: 8701
- TclRegaScript: 8181
- Groups: 9292

And **FROM** the gateway to the binding:

- XML-RPC: 9125
- BIN-RPC: 9126

CCU Autodiscovery:

- UDP 43439

**Note:** The binding tries to identify the gateway with XML-RPC and uses henceforth:

- **CCU**
  - **RF**: XML-RPC
  - **WIRED**: XML-RPC
  - **HMIP**: XML-RPC
  - **CUxD**: BIN-RPC (CUxD version >= 1.6 required)
  - **Groups**: XML-RPC

- **Homegear**
  - BIN-RPC

- **Other**
  - XML-RPC

## Supported Things

All devices connected to a Homematic gateway.
All required metadata are generated during device discovery.
With Homegear or a CCU, variables and scripts are supported too.

## Discovery

Gateway discovery is available:

- CCU
- RaspberryMatic >= 2.29.23.20171022
- Homegear >= 0.6.x
- piVCCU

For all other gateways you have to manually add a bridge in a things file. Device discovery is supported for all gateways.

The binding has a gateway type autodetection, but sometimes a gateway does not clearly notify the type.
If you are using a YAHM for example, you have to manually set the gateway type in the bride configuration to CCU.

If autodetection can not identify the gateway, the binding uses the default gateway implementation.
The difference is, that variables, scripts and device names are not supported, everything else is the same.

### Automatic install mode during discovery

Besides discovering devices that are already known by the gateway, it may be desired to connect new devices to your system - which requires your gateway to be in install mode.
Starting the binding's DiscoveryService will automatically put your gateway(s) in install mode for a specified period of time (see installModeDuration).

**Note:** Enabling / disabling of install mode is also available via GATEWAY-EXTRAS.
You may use this if you prefer.

**Exception:** If a gateway is not ONLINE, the install mode will not be set automatically.
For instance during initialization of the binding its DiscoveryService is started and will discover devices that are already connected.
However, the install mode is not automatically enabled in this situation because the gateway is in the status INITIALIZING.

## Bridge Configuration

There are several settings for a bridge:

- **gatewayAddress** (required)
Network address of the Homematic gateway

- **gatewayType**
Hint for the binding to identify the gateway type (auto|ccu|noccu) (default = "auto").

- **callbackHost**
Callback network address of the system runtime, default is auto-discovery

- **xmlCallbackPort**
Callback port of the binding's XML-RPC server, default is 9125 and counts up for each additional bridge

- **binCallbackPort**
Callback port of the binding's BIN-RPC server, default is 9126 and counts up for each additional bridge

- **timeout**
The timeout in seconds for connections to a Homematic gateway (default = 15)

- **discoveryTimeToLive**
The time to live in seconds for discovery results of a Homematic gateway (default = -1, which means infinite)

- **socketMaxAlive**
The maximum lifetime of a socket connection to and from a Homematic gateway in seconds (default = 900)

- **rfPort**
The port number of the RF daemon (default = 2001)

- **wiredPort**
The port number of the HS485 daemon (default = 2000)

- **hmIpPort**
The port number of the HMIP server (default = 2010)

- **cuxdPort**
The port number of the CUxD daemon (default = 8701)

- **groupPort**
The port number of the Group daemon (default = 9292)

- **callbackRegTimeout**
Maximum time in seconds for callback registration in the Homematic gateway (default = 120s).
For a CCU2, the value may need to be increased to 180s.

- **installModeDuration**
Time in seconds that the controller will be in install mode when a device discovery is initiated (default = 60)

- **unpairOnDeletion**
If set to true, devices are automatically unpaired from the gateway when their corresponding things are deleted.  
**Warning:** The option "factoryResetOnDeletion" also unpairs a device, so in order to avoid unpairing on deletion completely, both options need to be set to false! (default = false)

- **factoryResetOnDeletion**
If set to true, devices are automatically factory reset when their corresponding things are removed.
Due to the factory reset, the device will also be unpaired from the gateway, even if "unpairOnDeletion" is set to false! (default = false)

- **bufferSize**
  If a large number of devices are connected to the gateway, the default buffersize of 2048 kB may be too small for communication with the gateway.
  In this case, e.g. the discovery fails.
  With this setting the buffer size can be adjusted. The value is specified in kB.
  
- **useAuthentication**
Username and password are send to the gateway to authenticate the access to the gateway.

- **userName**
Username for Authentication to the gateway.

- **password**
Password for Authentication to the gateway.

The syntax for a bridge is:

```java
homematic:bridge:NAME
```

- **homematic** the binding id, fixed
- **bridge** the type, fixed
- **name** the name of the bridge

### Example

#### Minimum configuration

```java
Bridge homematic:bridge:ccu [ gatewayAddress="..."]
```

#### With callback settings

```java
Bridge homematic:bridge:ccu [ gatewayAddress="...", callbackHost="...", callbackPort=... ]
```

#### Multiple bridges

```java
Bridge homematic:bridge:lxccu [ gatewayAddress="..."]
Bridge homematic:bridge:occu  [ gatewayAddress="..."]
```

## Thing Configuration

Things are all discovered automatically.

If you really like to manually configure a thing:

```java
Bridge homematic:bridge:ccu [ gatewayAddress="..." ]
{
  Thing HM-LC-Dim1T-Pl-2    JEQ0999999
}
```

The first parameter after Thing is the device type, the second the serial number.
If you are using Homegear, you have to add the prefix `HG-` for each type.
The `HG-` prefix is only needed for Things, not for Items or channel configs.
This is necessary, because the Homegear devices supports more datapoints than Homematic devices.

```java
  Thing HG-HM-LC-Dim1T-Pl-2     JEQ0999999  "Name"  @  "Location"
```

All channels have two configs:

- **delay**: delays transmission of a command **to** the Homematic gateway, duplicate commands are filtered out
- **receiveDelay**: delays a received event **from** the Homematic gateway, duplicate events are filtered out (OH 2.2)

The `receiveDelay` is handy for dimmers and roller shutters for example.
If you have a slider in a UI and you move this slider to a new position, it jumps around because the gateway sends multiple events with different positions until the final has been reached.
If you set the `receiveDelay` to some seconds, these events are filtered out and only the last position is distributed to the binding.
The disadvantage is of course, that all events for this channel are delayed.

```java
  Thing HM-LC-Dim1T-Pl-2    JEQ0999999 "Name"  @  "Location" {
      Channels:
          Type HM-LC-Dim1T-Pl-2_1_LEVEL : 1#LEVEL [
              delay = 0,
              receiveDelay = 4
          ]
  }
```

The `Type` is the device type, channel number and UPPERCASE channel name separated with an underscore.
Note that, for Homegear devices, in contrast to the specification of the Thing above no `HG-` prefix is needed for the specification of the Type of the Channel.

The channel configs are optional.

Example without channel configs

```java
  Thing HM-LC-Dim1T-Pl-2    JEQ0999999 "Name"  @  "Location" {
      Channels:
          Type HM-LC-Dim1T-Pl-2_1_LEVEL : 1#LEVEL
  }
```

### Items

In the items file, you can map the datapoints. The syntax is:

```java
homematic:TYPE:BRIDGE:SERIAL:CHANNELNUMBER#DATAPOINTNAME
```

- **homematic:** the binding id, fixed
- **type:** the type of the Homematic device
- **bridge:** the name of the bridge
- **serial:** the serial number of the Homematic device
- **channelnumber:** the channel number of the Homematic datapoint
- **datapointname:** the name of the Homematic datapoint

```java
Switch  RC_1  "Remote Control Button 1" { channel="homematic:HM-RC-19-B:ccu:KEQ0099999:1#PRESS_SHORT" }
Dimmer  Light "Light [%d %%]"           { channel="homematic:HM-LC-Dim1T-Pl-2:ccu:JEQ0555555:1#LEVEL" }
```

**Note:** don't forget to add the `HG-` type prefix for Homegear devices

## Virtual device GATEWAY-EXTRAS

The GATEWAY-EXTRAS is a virtual device which contains a switch to reload all values from all devices and also a switch to put the gateway in the install mode to add new devices.
If the gateway supports variables and scripts, you can handle them with this device too.

The type is generated: `GATEWAY-EXTRAS-[BRIDGE_ID]`.
Example: bridgeId=**ccu** -> type=GATEWAY-EXTRAS-**CCU**

The address of the virtual device must be the default value `GWE00000000`.
Usage of a custom ID is not supported.

### RELOAD_ALL_FROM_GATEWAY

A virtual datapoint (Switch) to reload all values for all devices, available in channel 0 in GATEWAY-EXTRAS

### RELOAD_RSSI

A virtual datapoint (Switch) to reload all RSSI values for all devices, available in channel 0 in GATEWAY-EXTRAS

### INSTALL_MODE

A virtual datapoint (Switch) to start the install mode on the gateway, available in channel 0 in GATEWAY-EXTRAS

### INSTALL_MODE_DURATION

A virtual datapoint (Integer) to hold the duration for the install mode, available in channel 0 in GATEWAY-EXTRAS (max 300 seconds, default = 60)

## Virtual datapoints

Virtual datapoints are generated by the binding and provide special functionality for several device types.

### RSSI

A virtual datapoint (Number) with the unified RSSI value from RSSI_DEVICE and RSSI_PEER, available in channel 0 for all wireless devices

### DELETE_MODE

A virtual datapoint (Switch) to remove the device from the gateway, available in channel 0 for each device. Deleting a device is only possible if DELETE_DEVICE_MODE is not LOCKED

### DELETE_DEVICE_MODE

A virtual datapoint (Enum) to configure the device deletion with DELETE_MODE, available in channel 0 for each device

- **LOCKED:** (default) device can not be deleted
- **RESET:** device is reset to factory settings before deleting
- **FORCE:** device is also deleted if it is not reachable
- **DEFER:** if the device can not be reached, it is deleted at the next opportunity

**Note:** if you change the value and don't delete the device, the virtual datapoints resets to LOCKED after 30 seconds

### ON_TIME_AUTOMATIC

A virtual datapoint (Number) to automatically set the ON_TIME datapoint before the STATE or LEVEL datapoint is sent to the gateway, available for all devices which supports the ON_TIME datapoint.
This is useful to automatically turn off the datapoint after the specified time.

### BUTTON

A virtual datapoint (String) to simulate a key press, available on all channels that contains PRESS_ datapoints.

Available values:

- `SHORT_PRESSED`: triggered on a short key press
- `LONG_PRESSED`: triggered on a key press longer than `LONG_PRESS_TIME` (variable configuration per key, default is 0.4 s)
- `LONG_REPEATED`: triggered on long key press repetition, that is, in `LONG_PRESS_TIME` intervals as long as key is held
- `LONG_RELEASED`: triggered when a key is released after being long pressed

**Example:** to capture a short key press on the 19 button remote control in a rule

```javascript
rule "example trigger rule"
when
    Channel 'homematic:HM-RC-19-B:ccu:KEQ0012345:1#BUTTON' triggered SHORT_PRESSED
then
    ...
end
```

### DISPLAY_OPTIONS (only HM-RC-19)

A virtual datapoint (String) to control the display of a 19 button Homematic remote control (HM-RC-19), available on channel 18

The remote control display is limited to five characters, a longer text is truncated.

You have several additional options to control the display.

- BEEP _(TONE1, TONE2, TONE3)_ - let the remote control beep
- BACKLIGHT _(BACKLIGHT_ON, BLINK_SLOW, BLINK_FAST)_ - control the display backlight
- UNIT _(PERCENT, WATT, CELSIUS, FAHRENHEIT)_ - display one of these units
- SYMBOL _(BULB, SWITCH, WINDOW, DOOR, BLIND, SCENE, PHONE, BELL, CLOCK, ARROW_UP, ARROW_DOWN)_ - display symbols, multiple symbols possible

You can combine any option, they must be separated by a comma.
If you specify more than one option for BEEP, BACKLIGHT and UNIT, only the first one is taken into account and all others are ignored. For SYMBOL you can specify multiple options.

**Examples:**

Assumed you mapped the virtual datapoint to a String item called `Display_Options`.

```java
String Display_Options "Display_Options" { channel="homematic:HM-RC-19-B:ccu:KEQ0099999:18#DISPLAY_OPTIONS" }
```

show message "TEST":

```shell
smarthome send Display_Options "TEST"
```

show message "TEXT", beep once and turn backlight on:

```shell
smarthome send Display_Options "TEXT, TONE1, BACKLIGHT_ON"
```

show message "15", beep once, turn backlight on and shows the celsius unit:

```shell
smarthome send Display_Options "15, TONE1, BACKLIGHT_ON, CELSIUS"
```

show message "ALARM", beep three times, let the backlight blink fast and shows a bell symbol:

```shell
smarthome send Display_Options "ALARM, TONE3, BLINK_FAST, BELL"
```

Duplicate options: TONE3 is ignored, because TONE1 is specified previously.

```shell
smarthome send Display_Options "TEXT, TONE1, BLINK_FAST, TONE3"
```

### DISPLAY_SUBMIT (only HM-Dis-WM55 and HM-Dis-EP-WM55)

Adds multiple virtual datapoints to the HM-Dis-WM55 and HM-Dis-EP-WM55 devices to easily send (colored) text and icons to the display.

**Note:** The HM-Dis-EP-WM55 has only a black and white display and therefore does not support datapoints for colored lines. In addition, only lines 1-3 can be set.

#### Example

Display text at line 1,3 and 5 when the bottom button on the display is pressed

##### Items

```java
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

##### Rule

```javascript
rule "Display Test"
when
    Item Button_bottom received update ON
then
    Display_line_1.sendCommand("Line 1")
    Display_line_3.sendCommand("Line 3")
    Display_line_5.sendCommand("Line 5")

    Display_icon_1.sendCommand("NONE")
    Display_icon_3.sendCommand("OPEN")
    Display_icon_5.sendCommand("INFO")

    Display_color_1.sendCommand("NONE")
    Display_color_3.sendCommand("RED")
    Display_color_5.sendCommand("BLUE")

    Display_submit.sendCommand(ON)
end
```

**Available icons:**

- NONE
- OFF
- ON
- OPEN
- CLOSED
- ERROR
- OK
- INFO
- NEW_MESSAGE
- SERVICE
- SIGNAL_GREEN
- SIGNAL_YELLOW
- SIGNAL_RED

**Available colors (only HM-Dis-WM55):**

- NONE(=WHITE)
- WHITE
- RED
- ORANGE
- YELLOW
- GREEN
- BLUE

### HmIP-WRCD

The HmIP-WRCD display lines can be set via a combined parameter:

```java
String Display_CombinedParam "Combined Parameter" {channel="homematic:HmIP-WRCD:ccu:123456:3#COMBINED_PARAMETER"}
```

#### Set Display Lines

The combined parameter can be used in a rule file like this:

```java
Display_CombinedParam.sendCommand("{DDBC=WHITE,DDTC=BLACK,DDI=0,DDA=CENTER,DDS=Just a test,DDID=3,DDC=true}")
```

If you want to use the combined parameter in the console, you have to use ' instead of ", to prevent evaluation of curly braces:

```shell
openhab:send Display_CombinedParam '{DDBC=WHITE,DDTC=BLACK,DDI=0,DDA=CENTER,DDS=Just a test,DDID=3,DDC=true}'
```

**Key translation:**

- DDBC: Background color of this line. (_WHITE_, _BLACK_)
- DDTC: Text color of this line. (_WHITE_, _BLACK_)
- DDI: Icon to be shown after text. (see icon listing below)
- DDA: Alignment of this line. (_LEFT_, _CENTER_, _RIGHT_)
- DDS: Text of this line. (String, but see special character listing below)
- DDID: Line number. (_1-5_)
- DDC: Commit, should be set in the last line, otherwise leave unset. (_true_)

Each line can be updated separately without changing the other lines.

Multiple lines can be updated within one command, use comma to separate each line.
Here an example for a rule file:

```java
Display_CombinedParam.sendCommand("{DDBC=WHITE,DDTC=BLACK,DDI=24,DDA=LEFT,DDS=Window open,DDID=4},{DDBC=WHITE,DDTC=BLACK,DDI=0,DDA=LEFT,DDS=Temp.: %sC,DDID=2,DDC=true}")
```

**Special Characters:**

- [ -> Ä
- \# -> Ö
- $ -> Ü
- { -> ä
- | -> ö
- } -> ü
- _ -> ß
- ] -> &
- ' -> =
- ; -> Sand Glass
- < -> Arrow Down
- = -> Arrow Up
- \> -> Arrow Up Right
- @ -> Arrow Down Right

**Icons:**

- 0 - No Icon
- 1 - Light off
- 2 - Light on
- 3 - Locked
- 4 - Unlocked
- 5 - X
- 6 - Check
- 7 - Information
- 8 - Envelope
- 9 - Spanner
- 10 - Sun
- 11 - Moon
- 12 - Wind
- 13 - Cloud
- 14 - Cloud/Lightning
- 15 - Cloud/Light Rain
- 16 - Cloud/Moon
- 17 - Cloud/Rain
- 18 - Cloud/Snow
- 19 - Cloud/Sun
- 20 - Cloud/Sun/Rain
- 21 - Cloud/Snowflake
- 22 - Cloud/Raindrop
- 23 - Flame
- 24 - Window Open
- 25 - Roller Shutter
- 26 - Eco
- 27 - ? (Rectangle in circle)
- 28 - House with person
- 29 - House empty
- 30 - Bell
- 31 - Clock

#### Alarm Beep

The display can also make short beep alarms:

```java
Display_CombinedParam.sendCommand("{R=0,IN=10,ANS=0}")
```

Note, that a commit (`DDC`) is not necessary for sounds.

As with line configuration, this can be combined with other line updates, separated with a comma.

##### Key translations

- R: Repetitions (_0 to 15_, 15=infinite)
- IN: Interval (_5 to 80_ in steps of five)
- ANS: Beep sound (_-1 to 7_, see beep table)

##### Beep Sounds

This is the official mapping for the beep sounds

- -1 - No Sound
- 0 - Empty Battery
- 1 - Alarm Off
- 2 - External Alarm activated
- 3 - Internal Alarm activated
- 4 - External Alarm delayed activated
- 5 - Internal Alarm delayed activated
- 6 - Event
- 7 - Error

## Troubleshooting

### SHORT & LONG_PRESS events of push buttons do not occur on the event bus

It seems buttons like the HM-PB-2-WM55 do just send these kind of events to the CCU if they are mentioned in a CCU program.
A simple workaround to make them send these events is, to create a program (rule inside the CCU) that does just have a "When" part and no "Then" part, in this "When" part each channel needs to be mentioned at least once.
As the HM-PB-2-WM55 for instance has two channels, it is enough to mention the SHORT_PRESS event of channel 1 & 2.
The LONG_PRESS events will work automatically as they are part of the same channels.
After the creation of this program, the button device will receive configuration data from the CCU which have to be accepted by pressing the config-button at the back of the device.

### INSTALL_TEST

If a button is still not working and you do not see any PRESS_LONG / SHORT in your log file (log level DEBUG), it could be because of enabled security.
Try to disable security of your buttons in the HomeMatic Web GUI and try again.
If you can't disable security try to use key INSTALL_TEST which gets updated to ON for each key press

### -1 Failure

A device may return this failure while fetching the datapoint values.
I have tested pretty much but I did not find the reason.
The HM-ES-TX-WM device for example always returns this failure, it is impossible with the current CCU2 firmware (2.17.15) to fetch the values.
I have implemented two workarounds, if a device returns the failure, workaround one is executed, if the device still returns the failure, workaround two is executed.
This always works in my tests, but you may see an OFFLINE, ONLINE cycle for the device.
Fetching values is only done at startup or if you trigger a REFRESH.
I hope this will be fixed in one of the next CCU firmwares.
With [Homegear](https://www.homegear.eu) everything works as expected.

### No variables and scripts in GATEWAY-EXTRAS

The gateway autodetection of the binding can not clearly identify the gateway and falls back to the default implementation.
Use the ```gatewayType=ccu``` config to force the binding to use the CCU implementation.

### Variables out of sync

The CCU only sends an event if a datapoint of a device has changed.
There is (currently) no way to receive an event automatically when a variable has changed.
To reload all variable values, send a REFRESH command to any variable.
E.g you have an item linked to a variable with the name `Var_1`.

In the console:

```shell
openhab:send Var_1 REFRESH
```

In scripts:

:::: tabs

::: tab DSL

```java
Var_1.sendCommand(REFRESH)
```

:::

::: tab JavaScript

```javascript
import org.openhab.core.types.RefreshType
...
Var_1.sendCommand(RefreshType.REFRESH)
```

:::

::: tab JRuby

```ruby
Var_1.refresh
```

:::

::::

**Note:** adding new and removing deleted variables from the GATEWAY-EXTRAS thing is currently not supported.
You have to delete the thing, start a scan and add it again.

**`openhab.log` contains an exception with message: `Buffering capacity 2097152 exceeded` resp. discovery detects no devices**

In case of problems in the discovery or if above mentioned error message appears in `openhab.log`, the size for the transmission buffer for the communication with the gateway is too small.
The problem can be solved by increasing the `bufferSize` value in the bridge configuration.

### Rollershutters are inverted

openHAB and the CCU are using different values for the same state of a rollershutter.
Examples: HmIP-BROLL, HmIP-FROLL, HmIP-BBL, HmIP-FBL and HmIP-DRBLI4
|         | Open | Closed |
| ------- | ---- | ------ |
| openHAB | 0%   | 100%   |
| CCU     | 100% | 0%     |

### The binding does not receive any status changes from the Homematic gateway

First of all, make sure that none of the ports needed to receive status changes from the gateway are blocked by firewall settings.

If the computer running openHAB has more than one IP address, a wrong one may have been set as receiver for status changes.
In this case change the setting for `callbackHost` to the correct address.

### Debugging and Tracing

If you want to see what's going on in the binding, switch the log level to DEBUG in the Karaf console

```shell
log:set DEBUG org.openhab.binding.homematic
```

If you want to see even more, switch to TRACE to also see the gateway request/response data

```shell
log:set TRACE org.openhab.binding.homematic
```

Set the logging back to normal

```shell
log:set INFO org.openhab.binding.homematic
```

To identify problems, a full startup TRACE log will be needed:

```shell
stop org.openhab.binding.homematic
log:set TRACE org.openhab.binding.homematic
start org.openhab.binding.homematic
```

### Running in Docker

First of all you need to map the `XML-RPC` and `BIN-RPC` Callback Ports to the outside world.
They must not be mapped to a different port number.
Next make sure that you set the `Callback Network Address` to the IP where the homematic can reach the exposed ports.
