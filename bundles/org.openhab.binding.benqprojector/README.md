# BenQ Projector Binding

This binding is compatible with BenQ projectors that support the control protocol via the built-in Ethernet port, serial port or USB to serial adapter.
If your projector does not have built-in networking, you can connect to your projector's serial port via a TCP connection using a serial over IP device or by using`ser2net`.

The manufacturer's guide for connecting to the projector and the control protocol can be found in this document: [LX9215_RS232 Control Guide_0_Windows7_Windows8_WinXP.pdf](https://esupportdownload.benq.com/esupport/Projector/Control%20Protocols/LX9215/LX9215_RS232%20Control%20Guide_0_Windows7_Windows8_WinXP.pdf)

## Supported Things

This binding supports two thing types based on the connection used: `projector-serial` and `projector-tcp`.

## Discovery

If the projector has a built-in Ethernet port connected to the same network as the openHAB server and either the 'AMX Device Discovery' or 'Control4' options are present and enabled in the projector's network menu, the thing will be discovered automatically.
Serial port or serial over IP connections must be configured manually.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

The `projector-serial` thing has the following configuration parameters:

| Parameter       | Name             | Description                                                                                                                                               | Required |
|-----------------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| serialPort      | Serial Port      | Serial port device name that is connected to the BenQ projector to control, e.g. COM1 on Windows, /dev/ttyS0 on Linux or /dev/tty.PL2303-0000103D on Mac. | yes      |
| pollingInterval | Polling Interval | Polling interval in seconds to update channel states, range 5-60 seconds; default 10 seconds.                                                             | no       |

The `projector-tcp` thing has the following configuration parameters:

| Parameter       | Name             | Description                                                                                                 | Required |
|-----------------|------------------|-------------------------------------------------------------------------------------------------------------|----------|
| host            | Host Name        | Host Name or IP address for the projector or serial over IP device.                                         | yes      |
| port            | Port             | Port for the projector or serial over IP device, Default 8000 for BenQ projectors with built in networking. | yes      |
| pollingInterval | Polling Interval | Polling interval in seconds to update channel states, range 5-60 seconds; default 10 seconds.               | no       |

Some notes:

- If using a serial port connection, the baud rate in the projector OSD menu must be set to 9600 bps.
- The _source_, _picturemode_ and _aspectratio_ channels include a dropdown with the most commonly used settings.
- Not all pre-defined dropdown options will be usable if your particular projector does support a given option.
- If your projector has an option that is not in the dropdown, the string code to access that option will be displayed by the channel when that option is selected by the remote control.
- By using the sitemap mapping or a rule to send that code back to the channel, any options that are missing in the binding can be accessed.

- On Linux, you may get an error stating the serial port cannot be opened when the benqprojector binding tries to load.
- You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
- Also on Linux you may have issues with the USB if using two serial USB devices e.g. benqprojector and RFXcom. See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.
- Here is an example of ser2net.conf you can use to share your serial port /dev/ttyUSB0 on IP port 4444 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) (take care, the baud rate is specific to the BenQ projector):

```text
4444:raw:0:/dev/ttyUSB0:9600 8DATABITS NONE 1STOPBIT LOCAL
```

## Channels

| Channel            | Item Type | Purpose                                               | Values     |
| ------------------ | --------- | ----------------------------------------------------- | ---------- |
| power              | Switch    | Powers the projector on or off.                       |            |
| source             | String    | Retrieve or set the input source.                     | See above  |
| picturemode        | String    | Retrieve or set the picture mode.                     | See above  |
| aspectratio        | String    | Retrieve or set the aspect ratio.                     | See above  |
| freeze             | Switch    | Turn the freeze image mode on or off.                 |            |
| blank              | Switch    | Turn the screen blank mode on or off.                 |            |
| directcmd          | String    | Send a command directly to the projector.             | Write only |
| lamptime           | Number    | Retrieves the number of hours the lamp has been used. | Read only  |

## Full Example

things/benq.things:

```java
// serial port connection
benqprojector:projector-serial:hometheater "Projector" [ serialPort="COM5", pollingInterval=10 ]

// direct IP or serial over IP connection
benqprojector:projector-tcp:hometheater "Projector" [ host="192.168.0.10", port=8000, pollingInterval=10 ]

```

items/benq.items

```java
Switch benqPower                                      { channel="benqprojector:projector-serial:hometheater:power" }
String benqSource       "Source [%s]"                 { channel="benqprojector:projector-serial:hometheater:source" }
String benqPictureMode  "Picture Mode [%s]"           { channel="benqprojector:projector-serial:hometheater:picturemode" }
String benqAspectRatio  "Aspect Ratio [%s]"           { channel="benqprojector:projector-serial:hometheater:aspectratio" }
Switch benqFreeze                                     { channel="benqprojector:projector-serial:hometheater:freeze" }
Switch benqBlank                                      { channel="benqprojector:projector-serial:hometheater:blank" }
String benqDirect                                     { channel="benqprojector:projector-serial:hometheater:directcmd" }
Number benqLampTime     "Lamp Time [%d h]"   <switch> { channel="benqprojector:projector-serial:hometheater:lamptime" }
```

sitemaps/benq.sitemap

```perl
sitemap benq label="BenQ Projector" {
    Frame label="Controls" {
        Switch     item=benqPower  label="Power"
        Selection  item=benqSource label="Source" mappings=["hdmi"="HDMI", "hdmi2"="HDMI2", "ypbr"="Component", "RGB"="Computer", "vid"="Video", "svid"="S-Video"]
        Selection  item=benqPictureMode label="Picture Mode"
        Selection  item=benqAspectRatio label="Aspect Ratio"
        Switch     item=benqFreeze label="Freeze"
        Switch     item=benqBlank  label="Blank Screen"
        // This Selection is deprecated in favor of the Buttongrid element below
        Selection  item=benqDirect label="Direct Command"
        Text       item=benqLampTime
        Buttongrid item=benqDirect label="Remote Control" staticIcon=material:tv_remote buttons=[1:2:up="Up"=f7:arrowtriangle_up, 3:2:down="Down"=f7:arrowtriangle_down, 2:1:left="Left"=f7:arrowtriangle_left, 2:3:right="Right"=f7:arrowtriangle_right, 2:2:enter="Enter", 4:1:"menu=on"="Menu On", 4:2:"menu=off"="Menu Off", 4:3:"vol=+"="Volume +", 5:1:"mute=on"="Mute On", 5:2:"mute=off"="Mute Off", 5:3:"vol=-"="Volume -", 6:1:zoomO="Zoom Out", 6:2:zoomI="Zoom In", 6:3:auto="Zoom Auto"]
    }
    Frame label="Advanced Controls" {
        Switch     item=benqDirect label="Image Flip"       mappings=["pp=FT"="Front","pp=RE"="Rear","pp=FC"="Front Ceiling","pp=RC"="Rear Ceiling"]
        Switch     item=benqDirect label="Load Lens Memory" mappings=["lensload=m1"="1","lensload=m2"="2","lensload=m3"="3","lensload=m4"="4"]
        Switch     item=benqDirect label="Lamp Mode"        mappings=["lampm=lnor"="Normal","lampm=eco"="Eco","lampm=seco"="SmartEco"]
        Switch     item=benqDirect label="Lamp Mode"        mappings=["lampm=seco2"="SmartEco2","lampm=seco3"="SmartEco3","lampm=dimming"="Dimming","lampm=custom"="Custom"]
    }
}
```
