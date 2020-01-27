# Epson Projector Binding

This binding is compatible with Epson projectors which support the ESC/VP21 protocol over a serial port.
Alternatively you can connect to your projector via a TCP connection if you are using some kind of converter like `ser2net`.

## Supported Things

This binding supports only one thing: `projector`.

## Thing Configuration

The projector thing cannot be auto-discovered, it has to be configured by setting some of the following parameters:

- _serialPort_: Serial port device name that is connected to the Epson projector to control, e.g. COM1 on Windows, /dev/ttyS0 on Linux or /dev/tty.PL2303-0000103D on Mac
- _host_: IP address (for serial communication over TCP, leave the serial port parameter blank)
- _port_: Port (for serial communication over TCP, leave the serial port parameter blank)
- _pollingInterval_: Polling interval in milliseconds to update channel states

## Channels

| Channel       | Item Type           | Purpose  | Note  |
| ------------- | ------------------- | -------- | ----- |
| Power | Switch | Powers the device on or off. |  | 
| PowerState | String | Retrieves the actual power state of the device. |  | 
| LampTime | Number | Retrieves the lamp hours. |  | 
| KeyCode | Number |  |  | 
| VerticalKeystone | Number |  |  | 
| HorizontalKeystone | Number |  |  | 
| AutoKeystone | Number |  |  | 
| AspectRatio | String | Retrieves or set the aspect ratio. |  | 
| Luminance | String | Retrieves or set the ECO mode. |  | 
| Source | String | Retrieves or select the input source. |  | 
| DirectSource | Number |  |  | 
| Brightness | Number |  |  | 
| Contrast | Number |  |  | 
| Density | Number |  |  | 
| Tint | Number |  |  | 
| Sharpness | Number |  |  | 
| ColorTemperature | Number |  |  | 
| FleshTemperature | Number |  |  | 
| ColorMode | String | Retrieves or set the color mode. |  | 
| HorizontalPosition | Number |  |  | 
| VerticalPosition | Number |  |  | 
| Tracking | Number |  |  | 
| Sync | Number |  |  | 
| OffsetRed | Number |  |  | 
| OffsetGreen | Number |  |  | 
| OffsetBlue | Number |  |  | 
| GainRed | Number |  |  | 
| GainGreen | Number |  |  | 
| GainBlue | Number |  |  | 
| Gamma | String |  |  | 
| GammaStep | Number |  |  | 
| Color | String |  |  | 
| Mute | Switch |  |  | 
| HorizontalReverse | Switch |  |  | 
| VerticalReverse | Switch |  |  | 
| Background | String |  |  | 
| ErrCode | Number | Retrieves the last error code. |  | 
| ErrMessage | String | Retrieves the description of the last error. |  | 

## Full Example

items/epson.items

```
Switch epsonPower                          { channel="epsonprojector:hometheater:power" }
String epsonSource        "Source [%s]"    { channel="epsonprojector:hometheater:source" }
Number epsonDirectSource  "Direct Source"  { channel="epsonprojector:hometheater:directsource"}

Switch epsonMute               { channel="epsonprojector:hometheater:mute" }

Switch epsonHorizontalReverse  { channel="epsonprojector:hometheater:horizontalreverse" }
Switch epsonVerticalReverse    { channel="epsonprojector:hometheater:verticalreverse" }

String epsonAspectRatio       "AspectRatio [%s]"        { channel="epsonprojector:hometheater:aspectratio" }
String epsonColorMode         "ColorMode [%s]"          { channel="epsonprojector:hometheater:colormode" }
Number epsonColorTemperature  "Color Temperature [%d]"  <colorwheel>   { channel="epsonprojector:hometheater:colortemperature" }

Number epsonLampTime    "Lamp Time [%d h]"  <switch>       { channel="epsonprojector:hometheater:lamptime" }
Number epsonErrCode     "ErrCode [%d]"      <"siren-on">   { channel="epsonprojector:hometheater:errcode" }
String epsonErrMessage  "ErrMessage [%s]"   <"siren-off">  { channel="epsonprojector:hometheater:errmessage" }
```

sitemaps/epson.sitemap

```
sitemap epson label="Epson Projector Demo"
{
    Frame label="Controls" {
        Switch     item=epsonPower         label="Power"
        Text       item=epsonSource
        Selection  item=epsonDirectSource  label="DirectSource" mappings=[20="COMPONENT", 32="PC", 48=HDMI1, 160=HDMI2, 65=VIDEO, 66=SVIDEO]
        Switch     item=epsonMute label="Mute"
    }
    Frame label="Flip Projection" {
        Switch  item=epsonHorizontalReverse label="Horizontal Reverse"
        Switch  item=epsonVerticalReverse   label="Vertical Reverse"
    }
    Frame label="Info" {
        Text  item=epsonAspectRatio
        Text  item=epsonColorMode
        Text  item=epsonColorTemperature
        Text  item=epsonLampTime
        Text  item=epsonErrCode
        Text  item=epsonErrMessage
    }
}
```
