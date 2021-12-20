# Epson Projector Binding

This binding is compatible with Epson projectors that support the ESC/VP21 protocol over the built-in network (ethernet or Wi-Fi) port, serial port or USB to serial adapter.
If your projector does not have built-in networking, you can connect to your projector's serial port via a TCP connection using a serial over IP device or by using`ser2net`.

## Supported Things

This binding supports two thing types based on the connection used: `projector-serial` and `projector-tcp`.

## Discovery

If the projector has a built-in ethernet port connected to the same network as the openHAB server and the 'AMX Device Discovery' option is present and enabled in the projector's network menu, the thing will be discovered automatically.
Serial port or serial over IP connections must be configured manually.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

The `projector-serial` thing has the following configuration parameters:

| Parameter       | Name             | Description                                                                                                                                                | Required |
|-----------------|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| serialPort      | Serial Port      | Serial port device name that is connected to the Epson projector to control, e.g. COM1 on Windows, /dev/ttyS0 on Linux or /dev/tty.PL2303-0000103D on Mac. | yes      |
| pollingInterval | Polling Interval | Polling interval in seconds to update channel states, range 5-60 seconds; default 10 seconds.                                                              | no       |
| maxVolume       | Max Volume Range | Set to the maximum volume level available in the projector's OSD to select the correct range for the volume control. e.g. 20 or 40; default 20             | no       |

The `projector-tcp` thing has the following configuration parameters:

| Parameter       | Name             | Description                                                                                                                                    | Required |
|-----------------|------------------|------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| host            | Host Name        | Host Name or IP address for the projector or serial over IP device.                                                                            | yes      |
| port            | Port             | Port for the projector or serial over IP device; default 3629 for projectors with built-in ethernet connector or Wi-Fi.                        | yes      |
| pollingInterval | Polling Interval | Polling interval in seconds to update channel states, range 5-60 seconds; default 10 seconds.                                                  | no       |
| maxVolume       | Max Volume Range | Set to the maximum volume level available in the projector's OSD to select the correct range for the volume control. e.g. 20 or 40; default 20 | no       |

Some notes:

* The binding should work on all Epson projectors that support the ESC/VP21 protocol, however not all binding channels will be useable on all projectors.
* The _source_ channel includes a dropdown with the most common source inputs.
* If your projector has a source input that is not in the dropdown, the two character hex code to access that input will be displayed by the _source_ channel when that input is selected by the remote control.
* By using the sitemap mapping or a rule to send the input's code back to the _source_ channel, any source on the projector can be accessed by the binding.
* The following channels _aspectratio_, _colormode_, _luminance_, _gamma_ and _background_ are pre-populated with a full set of options but not every option will be useable on all projectors.
* If your projector has an option in one of the above mentioned channels that is not recognized by the binding, the channel will display 'UNKNOWN' if that un-recognized option is selected by the remote control.
* The volume channel is a dimmer (0-100%) that is scaled to the range on the projector, either 0-20 or 0-40 per the maxVolume configuration setting. If your projector uses a different range, then the volume channel will not work.
* If the projector power is switched to off in the middle of a polling operation, some of the channel values may become undefined until the projector is switched on again.
* If the binding fails to connect to the projector using the direct IP connection, ensure that no password is configured on the projctor.

* On Linux, you may get an error stating the serial port cannot be opened when the epsonprojector binding tries to load.
* You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
* Also on Linux you may have issues with the USB if using two serial USB devices e.g. epsonprojector and RFXcom. See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.
* Here is an example of ser2net.conf you can use to share your serial port /dev/ttyUSB0 on IP port 4444 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) (take care, the baud rate is specific to the Epson projector):

```
4444:raw:0:/dev/ttyUSB0:9600 8DATABITS NONE 1STOPBIT LOCAL
```

## Channels

| Channel            | Item Type | Purpose                                                                                    | Values    | 
| ------------------ | --------- | ------------------------------------------------------------------------------------------ | --------- | 
| power              | Switch    | Powers the projector on or off.                                                            |           | 
| powerstate         | String    | Retrieves the textual power state of the projector.                                        | Read only | 
| source             | String    | Retrieve or set the input source.                                                          | See above | 
| aspectratio        | String    | Retrieve or set the aspect ratio.                                                          | See above | 
| colormode          | String    | Retrieve or set the color mode.                                                            | See above | 
| freeze             | Switch    | Turn the freeze screen mode on or off.                                                     |           | 
| mute               | Switch    | Turn the AV mute on or off.                                                                |           | 
| volume             | Dimmer    | Retrieve or set the volume. Scaled to 0-20 or 0-40 on the projector per maxVolume setting. | 0% - 100% | 
| luminance          | String    | Retrieve or set the lamp mode.                                                             | See above | 
| brightness         | Number    | Retrieve or set the brightness.                                                            | -24 - +24 | 
| contrast           | Number    | Retrieve or set the contrast.                                                              | -24 - +24 | 
| density            | Number    | Retrieve or set the density (color saturation).                                            | -32 - +32 | 
| tint               | Number    | Retrieve or set the tint.                                                                  | -32 - +32 | 
| colortemperature   | Number    | Retrieve or set the color temperature.                                                     | 0   - +9  | 
| fleshtemperature   | Number    | Retrieve or set the flesh temperature.                                                     | 0   - +6  | 
| gamma              | String    | Retrieve or set the gamma setting.                                                         | See above | 
| autokeystone       | Switch    | Turn the auto keystone mode on or off.                                                     |           | 
| verticalkeystone   | Number    | Retrieve or set the vertical keystone.                                                     | -30 - +30 | 
| horizontalkeystone | Number    | Retrieve or set the horizontal keystone.                                                   | -30 - +30 | 
| verticalposition   | Number    | Retrieve or set the vertical position.                                                     | -8  - +10 | 
| horizontalposition | Number    | Retrieve or set the horizontal position.                                                   | -23 - +26 | 
| verticalreverse    | Switch    | Turn the vertical reverse mode on or off.                                                  |           | 
| horizontalreverse  | Switch    | Turn the horizontal reverse mode on or off.                                                |           | 
| background         | String    | Retrieve or set the background color/logo.                                                 | See above | 
| keycode            | String    | Send a key operation command to the projector. (2 character code)                          | Send only | 
| lamptime           | Number    | Retrieves the lamp hours.                                                                  | Read only | 
| errcode            | Number    | Retrieves the last error code.                                                             | Read only | 
| errmessage         | String    | Retrieves the description of the last error.                                               | Read only | 

## Full Example

things/epson.things:

```
// serial port connection
epsonprojector:projector-serial:hometheater "Projector" [ serialPort="COM5", pollingInterval=10, maxVolume=20 ]

// direct IP or serial over IP connection
epsonprojector:projector-tcp:hometheater "Projector" [ host="192.168.0.10", port=3629, pollingInterval=10, maxVolume=20 ]

```

items/epson.items

```
Switch epsonPower                                      { channel="epsonprojector:projector-serial:hometheater:power" }
String epsonSource       "Source [%s]"                 { channel="epsonprojector:projector-serial:hometheater:source" }
String epsonAspectRatio  "Aspect Ratio [%s]"           { channel="epsonprojector:projector-serial:hometheater:aspectratio" }
String epsonColorMode    "Color Mode [%s]"             { channel="epsonprojector:projector-serial:hometheater:colormode" }
Switch epsonFreeze                                     { channel="epsonprojector:projector-serial:hometheater:freeze" }
Switch epsonMute                                       { channel="epsonprojector:projector-serial:hometheater:mute" }
Dimmer epsonVolume                                     { channel="epsonprojector:projector-serial:hometheater:volume" }
String epsonLuminance    "Lamp Mode [%s]"              { channel="epsonprojector:projector-serial:hometheater:luminance" }

Number epsonBrightness                                 { channel="epsonprojector:projector-serial:hometheater:brightness" }
Number epsonContrast                                   { channel="epsonprojector:projector-serial:hometheater:contrast" }
Number epsonDensity                                    { channel="epsonprojector:projector-serial:hometheater:density" }
Number epsonTint                                       { channel="epsonprojector:projector-serial:hometheater:tint" }
Number epsonColorTemperature                           { channel="epsonprojector:projector-serial:hometheater:colortemperature" }
Number epsonFleshTemperature                           { channel="epsonprojector:projector-serial:hometheater:fleshtemperature" }
String epsonGamma        "Gamma [%s]"                  { channel="epsonprojector:projector-serial:hometheater:gamma" }

Switch epsonAutokeystone                               { channel="epsonprojector:projector-serial:hometheater:autokeystone" }
Number epsonVerticalKeystone                           { channel="epsonprojector:projector-serial:hometheater:verticalkeystone" }
Number epsonHorizontalKeystone                         { channel="epsonprojector:projector-serial:hometheater:horizontalkeystone" }
Number epsonVerticalPosition                           { channel="epsonprojector:projector-serial:hometheater:verticalposition" }
Number epsonHorizontalPosition                         { channel="epsonprojector:projector-serial:hometheater:horizontalposition" }
Switch epsonVerticalReverse                            { channel="epsonprojector:projector-serial:hometheater:verticalreverse" }
Switch epsonHorizontalReverse                          { channel="epsonprojector:projector-serial:hometheater:horizontalreverse" }

String epsonBackground  "Background [%s]"              { channel="epsonprojector:projector-serial:hometheater:background" }
String epsonKeyCode     "Key Code [%s]"                { channel="epsonprojector:projector-serial:hometheater:keycode", autoupdate="false" }
String epsonPowerState  "Power State [%s]"   <switch>  { channel="epsonprojector:projector-serial:hometheater:powerstate" }
Number epsonLampTime    "Lamp Time [%d h]"   <switch>       { channel="epsonprojector:projector-serial:hometheater:lamptime" }
Number epsonErrCode     "Error Code [%d]"    <"siren-on">   { channel="epsonprojector:projector-serial:hometheater:errcode" }
String epsonErrMessage  "Error Message [%s]" <"siren-off">  { channel="epsonprojector:projector-serial:hometheater:errmessage" }
```

sitemaps/epson.sitemap

```
sitemap epson label="Epson Projector"
{
    Frame label="Controls" {
        Switch     item=epsonPower   label="Power"
        Selection  item=epsonSource  label="Source" mappings=["30"="HDMI1", "A0"="HDMI2", "14"="Component", "20"="PC DSUB", "41"="Video", "42"="S-Video"]
        Switch     item=epsonFreeze  label="Freeze"
        Switch     item=epsonMute    label="AV Mute"
        // Volume can be a Setpoint also
        Slider     item=epsonVolume  label="Volume" minValue=0 maxValue=100 step=1 icon="soundvolume"
        Switch     item=epsonKeyCode label="Navigation" icon="screen" mappings=["03"="Menu", "35"="˄", "36"="˅", "37"="<", "38"=">", "16"="Enter"]
    }
    Frame label="Adjust Image" {
        Setpoint   item=epsonBrightness         label="Brightness"
        Setpoint   item=epsonContrast           label="Contrast"
        Setpoint   item=epsonDensity            label="Color Saturation"
        Setpoint   item=epsonTint               label="Tint"
        Switch     item=epsonAutokeystone       label="Auto Keystone"
        Setpoint   item=epsonVerticalKeystone   label="Vertical Keystone"
        Setpoint   item=epsonHorizontalKeystone label="Horizontal Keystone"
        Setpoint   item=epsonVerticalPosition   label="Vertical Position"
        Setpoint   item=epsonHorizontalPosition label="Horizontal Position"
        Selection  item=epsonBackground         label="Background"
    }
    Frame label="Flip Projection" {
        Switch  item=epsonVerticalReverse   label="Vertical Reverse"
        Switch  item=epsonHorizontalReverse label="Horizontal Reverse"
    }
    Frame label="Info" {
        Text  item=epsonAspectRatio
        Text  item=epsonColorMode
        Text  item=epsonColorTemperature    label="Color Temperature"
        Text  item=epsonFleshTemperature    label="Flesh Temperature"
        Text  item=epsonGamma
        Text  item=epsonLuminance
        Text  item=epsonPowerState
        Text  item=epsonLampTime
        Text  item=epsonErrCode
        Text  item=epsonErrMessage
    }
}
```
