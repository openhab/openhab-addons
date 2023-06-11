# SonyProjector Binding

This binding can be used to conrol Sony Projectors through:

- an Ethernet connection using PJ Talk
- or a (direct) serial connection
- or a serial over IP connection

For serial connection, you have to use a D-Sub 9 Pin cross (reverse) cable also called null modem.
For this cable, you will need a female connector on openHAB server side and a male connector on projector side (projector connector is female).
For users without serial connector on server side, of course you can add a serial to USB adapter to this serial cable.
Has been tested successfully using [this cable](https://www.amazon.fr/UGREEN-PL2303-Windows-Chrome-Connecteur/dp/B00QUZY4UG/ref=sr_1_3?ie=UTF8&qid=1548652565&sr=8-3&keywords=serial+usb) connected to a Windows 10 PC and Raspberry Pi 2.
In such cases, setup a serial connection thing type in openHAB.

You don't need to have your projector directly connected to your openHAB server.
You can connect it for example to a Raspberry Pi and use [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) to make the serial connection available on LAN (serial over IP).
In such a case, setup a serial over IP connection thing type in openHAB.

Here is the list of supported Sony projectors based on Sony protocol manuals:

- VPL-HW15
- VPL-HW20
- VPL-HW30ES
- VPL-HW35ES
- VPL-HW40ES
- VPL-HW50ES
- VPL-HW55ES
- VPL-HW58ES
- VPL-HW60
- VPL-HW65
- VPL-HW68
- VPL-VW40
- VPL-VW50
- VPL-VW60
- VPL-VW70
- VPL-VW85
- VPL-VW95ES
- VPL-VW100
- VPL-VW315
- VPL-VW320
- VPL-VW328
- VPL-VW365
- VPL-VW515
- VPL-VW520
- VPL-VW528
- VPL-VW665
- VPL-VW1000ES
- VPL-VW1100ES

Here is the list of supported Sony projectors but not sure due to assumptions done based on Sony user manuals and protocol manuals of other similar models (because Sony protocol manuals unfortunately not available for these models):

- VPL-HW10
- VPL-HW45ES
- VPL-VW80
- VPL-VW90ES
- VPL-VW200
- VPL-VW260ES
- VPL-VW270ES
- VPL-VW285ES
- VPL-VW295ES
- VPL-VW300ES
- VPL-VW350ES
- VPL-VW385ES
- VPL-VW500ES
- VPL-VW550ES (= VW675)
- VPL-VW570ES
- VPL-VW600ES
- VPL-VW675ES
- VPL-VW695ES
- VPL-VW760ES
- VPL-VW870ES
- VPL-VW885ES
- VPL-VW995ES

Control of other (HW or VW) models could work with the binding by selecting one of the supported models but without any guarantee.
Here is a list of potential candidates:

- VPL-VW10HT
- VPL-VW11HT
- VPL-VW12HT
- VPL-VW360ES
- VPL-VW685ES
- VPL-VW5000ES

## Supported Things

This binding supports the following thing types:

| Thing Type             | Description                                             |
| ---------------------- | ------------------------------------------------------- |
| ethernetconnection     | Ethernet connection to the Sony projector using PJ Talk |
| serialconnection       | Serial connection to the Sony projector                 |
| serialoveripconnection | Serial over IP connection to the Sony projector         |

## Discovery

Discovery is not supported at the moment.
You have to add all things manually.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

### Ethernet connection

The Ethernet connection thing requires the following configuration parameters:

| Parameter Label | Parameter ID | Description                                                              | Required | Default | Accepted values                                                                                                                                                                                                                                                                                                                                                                                           |
| --------------- | ------------ | ------------------------------------------------------------------------ | -------- | ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Address         | host         | Host name or IP address of the projector                                 | true     |         |                                                                                                                                                                                                                                                                                                                                                                                                           |
| Port            | port         | Communication port. Default is 53484                                     | false    | 53484   |                                                                                                                                                                                                                                                                                                                                                                                                           |
| Model           | model        | Projector model to be controlled. Default is AUTO                        | false    | AUTO    | AUTO, VPL-HW60, VPL-HW65, VPL-HW68, VPL-VW100, VPL-VW200, VPL-VW260ES, VPL-VW270ES, VPL-VW285ES, VPL-VW295ES, VPL-VW300ES, VPL-VW315, VPL-VW320, VPL-VW328, VPL-VW350ES, VPL-VW365, VPL-VW385ES, VPL-VW500ES, VPL-VW515, VPL-VW520, VPL-VW528, VPL-VW550ES, VPL-VW570ES, VPL-VW600ES, VPL-VW665, VPL-VW675ES, VPL-VW695ES, VPL-VW760ES, VPL-VW870ES, VPL-VW885ES, VPL-VW995ES, VPL-VW1000ES, VPL-VW1100ES |
| Community       | community    | Community of the projector. Length must be 4 characters. Default is SONY | false    | SONY    |                                                                                                                                                                                                                                                                                                                                                                                                           |

Some notes:

- Take care to enable PJ Talk on your projector.

### Serial connection

The serial connection thing requires the following configuration parameters:

| Parameter Label | Parameter ID | Description                                        | Required | Default   | Accepted values                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| --------------- | ------------ | -------------------------------------------------- | -------- | --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Serial Port     | port         | Serial port to use for connecting to the projector | true     |           |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| Model           | model        | Projector model to be controlled                   | true     | VPL-VW528 | VPL-HW10, VPL-HW15, VPL-HW20, VPL-HW30ES, VPL-HW35ES, VPL-HW40ES, VPL-HW45ES, VPL-HW50ES, VPL-HW55ES, VPL-HW58ES, VPL-HW60, VPL-HW65, VPL-HW68, VPL-VW40, VPL-VW50, VPL-VW60, VPL-VW70, VPL-VW80, VPL-VW85, VPL-VW90ES, VPL-VW95ES, VPL-VW100, VPL-VW200, VPL-VW260ES, VPL-VW270ES, VPL-VW285ES, VPL-VW295ES, VPL-VW300ES, VPL-VW315, VPL-VW320, VPL-VW328, VPL-VW350ES, VPL-VW365, VPL-VW385ES, VPL-VW500ES, VPL-VW515, VPL-VW520, VPL-VW528, VPL-VW550ES, VPL-VW570ES, VPL-VW600ES, VPL-VW665, VPL-VW675ES, VPL-VW695ES, VPL-VW760ES, VPL-VW870ES, VPL-VW885ES, VPL-VW995ES, VPL-VW1000ES, VPL-VW1100ES |

Some notes:

- On Linux, you may get an error stating the serial port cannot be opened when the SonyProjector binding tries to load.  You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
- Also on Linux you may have issues with the USB if using two serial USB devices e.g. SonyProjector and RFXcom. See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.

### Serial over IP connection

The serial over IP connection thing requires the following configuration parameters:

| Parameter Label | Parameter ID | Description                                                       | Required | Default   | Accepted values                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| --------------- | ------------ | ----------------------------------------------------------------- | -------- | --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Address         | host         | Host name or IP address of the machine connected to the projector | true     |           |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| Port            | port         | Communication port                                                | true     |           |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| Model           | model        | Projector model to be controlled                                  | true     | VPL-VW528 | VPL-HW10, VPL-HW15, VPL-HW20, VPL-HW30ES, VPL-HW35ES, VPL-HW40ES, VPL-HW45ES, VPL-HW50ES, VPL-HW55ES, VPL-HW58ES, VPL-HW60, VPL-HW65, VPL-HW68, VPL-VW40, VPL-VW50, VPL-VW60, VPL-VW70, VPL-VW80, VPL-VW85, VPL-VW90ES, VPL-VW95ES, VPL-VW100, VPL-VW200, VPL-VW260ES, VPL-VW270ES, VPL-VW285ES, VPL-VW295ES, VPL-VW300ES, VPL-VW315, VPL-VW320, VPL-VW328, VPL-VW350ES, VPL-VW365, VPL-VW385ES, VPL-VW500ES, VPL-VW515, VPL-VW520, VPL-VW528, VPL-VW550ES, VPL-VW570ES, VPL-VW600ES, VPL-VW665, VPL-VW675ES, VPL-VW695ES, VPL-VW760ES, VPL-VW870ES, VPL-VW885ES, VPL-VW995ES, VPL-VW1000ES, VPL-VW1100ES |

Some notes:

- Here is an example of ser2net.conf you can use to share your serial port /dev/ttyUSB0 on IP port 3333 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/):

```text
3333:raw:0:/dev/ttyUSB0:38400 8DATABITS EVEN 1STOPBIT
```

## Channels

The following channels are available:

| Channel ID        | Label                    | Item Type | Access Mode | Description                                                             | Possible values (depends on model)                                                                                                                 |
| ----------------- | ------------------------ | --------- | ----------- | ----------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| power             | Power                    | Switch    | RW          | Power ON/OFF the projector                                              | ON, OFF                                                                                                                                            |
| powerstate        | Power State              | String    | R           | Current detailed power state of the projector                           |                                                                                                                                                    |
| input             | Video Input              | String    | RW          | Select the equipment from which to display images                       | Video, SVideo, InputA, Component, HDMI, HDMI1, HDMI2, DVI                                                                                          |
| calibrationpreset | Calibrated Preset        | String    | RW          | Adjust the picture viewing mode by selecting one of the preset modes    | Film1, Film2, Digital, Reference, TV, Photo, Game, BRTCINE, BRTTV, Dynamic, Standard, Cinema, Cinema1, Cinema2, Cinema3, User, User1, User2, User3 |
| contrast          | Contrast                 | Dimmer    | RW          | Adjust the contrast of the picture                                      | Value between 0 and 100                                                                                                                            |
| brightness        | Brightness               | Dimmer    | RW          | Adjust the brightness of the picture                                    | Value between 0 and 100                                                                                                                            |
| color             | Color                    | Dimmer    | RW          | Adjust the color density                                                | Value between 0 and 100                                                                                                                            |
| hue               | Hue                      | Dimmer    | RW          | Adjust the color tone                                                   | Value between 0 and 100                                                                                                                            |
| sharpness         | Sharpness                | Dimmer    | RW          | Sharpen the outline of the picture or reduce the noise                  | Value between 0 and 100                                                                                                                            |
| colortemperature  | Color Temperature        | String    | RW          | Adjust the color temperature                                            | D55, D65, D75, D93, DCI, High, Middle, Low, Low1, Low2, Custom, Custom1, Custom2, Custom3, Custom4, Custom5                                        |
| irismode          | Iris Mode                | String    | RW          | Switch the iris aperture function                                       | AutoFull, Full, AutoLimited, Limited, On, Auto, Auto1, Auto2, Manual, Off                                                                          |
| irismanual        | Iris Manual              | Dimmer    | RW          | Force the iris aperture to a fixed value                                | Value between 0 and 100                                                                                                                            |
| irissensitivity   | Iris Sensitivity         | String    | RW          | Select the iris sensitivity                                             | Recommend, Fast, Slow                                                                                                                              |
| lampcontrol       | Lamp Control             | String    | RW          | Switch the lamp output                                                  | High, Low                                                                                                                                          |
| filmprojection    | Film Projection          | String    | RW          | Reproduce an image similar to that of projected film                    | On, Mode1, Mode2, Mode3, Off                                                                                                                       |
| motionenhancer    | Motion Enhancer          | String    | RW          | Reproduce fast-moving pictures smoothly without generating afterimages  | High, Low, SmoothHigh, SmoothLow, Impulse, Combination, TrueCinema, Off                                                                            |
| contrastenhancer  | Contrast Enhancer        | String    | RW          | Correct the level of bright and dark parts automatically                | High, Middle, Low, Off, -3, -2, -1, 0, 1, 2, 3                                                                                                     |
| filmmode          | Film Mode                | String    | RW          | Select the playback mode for film sources                               | Auto, Auto1, Auto2, Off                                                                                                                            |
| gammacorrection   | Gamma Correction         | String    | RW          | Adjust the response characteristics of the tone of the picture          | 1.8, 2.0, 2.1, 2.2, 2.4, 2.6, Gamma1, Gamma2, Gamma3, Gamma4, Gamma5, Gamma6, Gamma7, Gamma8, Gamma9, Gamma10, Off                                 |
| colorspace        | Color Space              | String    | RW          | Convert the color space                                                 | BT709, BT2020, DCI, AdobeRGB, ColorSpace1, ColorSpace2, ColorSpace3, Custom, Normal, Wide, Wide1, Wide2, Wide3                                     |
| nr                | Noise Reduction          | String    | RW          | Reduce the roughness or noise of the picture                            | Auto, High, Middle, Low, Off                                                                                                                       |
| blocknr           | Block Noise Reduction    | String    | RW          | Reduce digital noise that appears in mosaic-like pattern                | High, Middle, Low, Off                                                                                                                             |
| mosquitonr        | Mosquito Noise Reduction | String    | RW          | Reduce digital noise that appears near the outline of the picture       | High, Middle, Low, Off                                                                                                                             |
| mpegnr            | MPEG Noise Reduction     | String    | RW          | Reduce block noise and mosquito noise, in particular in digital signals | Auto, High, Middle, Low, Off                                                                                                                       |
| xvcolor           | x.v.Color                | Switch    | RW          | Set this when playing back an x.v.Color video signal                    | ON, OFF                                                                                                                                            |
| picturemuting     | Picture Muting           | Switch    | RW          | Mute or unmute the picture                                              | ON, OFF                                                                                                                                            |
| aspect            | Aspect Ratio             | String    | RW          | Set the aspect ratio of the picture to be displayed                     | Normal, 185, 235, VStretch, Stretch, Squeeze, Full, Full1, Full2, WideZoom, Zoom, Anamorphic, Subtitle                                             |
| overscan          | Overscan                 | Switch    | RW          | Hide or not the edges of the picture                                    | ON, OFF                                                                                                                                            |
| pictureposition   | Picture Position         | String    | RW          | Adjust the picture position                                             | 185, 235, Custom1, Custom2, Custom3, Position1, Position2, Position3, Position4, Position5                                                         |
| lampusetime       | Lamp Use Time            | Number    | R           | Indicate how long the lamp has been turned on                           |                                                                                                                                                    |

## Full Example

example.things:

```java
Thing sonyprojector:ethernetconnection:proj "Projector" [ host="192.168.0.200" ]
Thing sonyprojector:ethernetconnection:proj2 "Projector" [ host="192.168.0.205", port=53484, model="VPL-VW365", community="SONY" ]
Thing sonyprojector:serialconnection:proj3 "Projector" [ port="/dev/ttyUSB0", model="VPL-HW55ES" ]
Thing sonyprojector:serialoveripconnection:proj4 "Projector" [ host="192.168.0.210", port=3000, model="VPL-VW1000ES" ]
```

example.items:

```java
Switch proj_power "Power" { channel = "sonyprojector:ethernetconnection:proj:power" }
String proj_powerstate "Power State [%s]" { channel = "sonyprojector:ethernetconnection:proj:powerstate" }
String proj_input "Video Input [%s]" { channel = "sonyprojector:ethernetconnection:proj:input" }
String proj_calibrationpreset "Calibrated Preset [%s]" { channel = "sonyprojector:ethernetconnection:proj:calibrationpreset" }
Dimmer proj_contrast "Contrast [%d]" { channel = "sonyprojector:ethernetconnection:proj:contrast" }
Dimmer proj_brightness "Brightness [%d]" { channel = "sonyprojector:ethernetconnection:proj:brightness" }
Dimmer proj_color "Color [%d]" { channel = "sonyprojector:ethernetconnection:proj:color" }
Dimmer proj_hue "Hue [%d]" { channel = "sonyprojector:ethernetconnection:proj:hue" }
Dimmer proj_sharpness "Sharpness [%d]" { channel = "sonyprojector:ethernetconnection:proj:sharpness" }
String proj_colortemperature "Color Temperature [%s]" { channel = "sonyprojector:ethernetconnection:proj:colortemperature" }
String proj_irismode "Iris Mode [%s]" { channel = "sonyprojector:ethernetconnection:proj:irismode" }
Dimmer proj_irismanual "Iris Manual [%d]" { channel = "sonyprojector:ethernetconnection:proj:irismanual" }
String proj_irissensitivity "Iris Sensitivity [%s]" { channel = "sonyprojector:ethernetconnection:proj:irissensitivity" }
String proj_lampcontrol "Lamp Control [%s]" { channel = "sonyprojector:ethernetconnection:proj:lampcontrol" }
String proj_filmprojection "Film Projection [%s]" { channel = "sonyprojector:ethernetconnection:proj:filmprojection" }
String proj_motionenhancer "Motion Enhancer [%s]" { channel = "sonyprojector:ethernetconnection:proj:motionenhancer" }
String proj_contrastenhancer "Contrast Enhancer [%s]" { channel = "sonyprojector:ethernetconnection:proj:contrastenhancer" }
String proj_filmmode "Film Mode [%s]" { channel = "sonyprojector:ethernetconnection:proj:filmmode" }
String proj_gammacorrection "Gamma Correction [%s]" { channel = "sonyprojector:ethernetconnection:proj:gammacorrection" }
String proj_colorspace "Color Space [%s]" { channel = "sonyprojector:ethernetconnection:proj:colorspace" }
String proj_nr "Noise Reduction [%s]" { channel = "sonyprojector:ethernetconnection:proj:nr" }
String proj_blocknr "Block Noise Reduction [%s]" { channel = "sonyprojector:ethernetconnection:proj:blocknr" }
String proj_mosquitonr "Mosquito Noise Reduction [%s]" { channel = "sonyprojector:ethernetconnection:proj:mosquitonr" }
String proj_mpegnr "MPEG Noise Reduction [%s]" { channel = "sonyprojector:ethernetconnection:proj:mpegnr" }
Switch proj_xvcolor "x.v.Color" { channel = "sonyprojector:ethernetconnection:proj:xvcolor" }
Switch proj_picturemuting "Picture Muting" { channel = "sonyprojector:ethernetconnection:proj:picturemuting" }
String proj_aspect "Aspect Ratio [%s]" { channel = "sonyprojector:ethernetconnection:proj:aspect" }
Switch proj_overscan "Overscan" { channel = "sonyprojector:ethernetconnection:proj:overscan" }
String proj_pictureposition "Picture Position [%s]" { channel = "sonyprojector:ethernetconnection:proj:pictureposition" }
Number proj_lampusetime "Lamp Use Time [%d]" { channel = "sonyprojector:ethernetconnection:proj:lampusetime" }

Switch proj3_power "Power" { channel = "sonyprojector:serialconnection:proj3:power" }
String proj3_powerstate "Power State [%s]" { channel = "sonyprojector:serialconnection:proj3:powerstate" }
String proj3_input "Video Input [%s]" { channel = "sonyprojector:serialconnection:proj3:input" }
String proj3_calibrationpreset "Calibrated Preset [%s]" { channel = "sonyprojector:serialconnection:proj3:calibrationpreset" }
Dimmer proj3_contrast "Contrast [%d]" { channel = "sonyprojector:serialconnection:proj3:contrast" }
Dimmer proj3_brightness "Brightness [%d]" { channel = "sonyprojector:serialconnection:proj3:brightness" }
Dimmer proj3_color "Color [%d]" { channel = "sonyprojector:serialconnection:proj3:color" }
Dimmer proj3_hue "Hue [%d]" { channel = "sonyprojector:serialconnection:proj3:hue" }
Dimmer proj3_sharpness "Sharpness [%d]" { channel = "sonyprojector:serialconnection:proj3:sharpness" }
String proj3_colortemperature "Color Temperature [%s]" { channel = "sonyprojector:serialconnection:proj3:colortemperature" }
String proj3_irismode "Iris Mode [%s]" { channel = "sonyprojector:serialconnection:proj3:irismode" }
Dimmer proj3_irismanual "Iris Manual [%d]" { channel = "sonyprojector:serialconnection:proj3:irismanual" }
String proj3_irissensitivity "Iris Sensitivity [%s]" { channel = "sonyprojector:serialconnection:proj3:irissensitivity" }
String proj3_lampcontrol "Lamp Control [%s]" { channel = "sonyprojector:serialconnection:proj3:lampcontrol" }
String proj3_filmprojection "Film Projection [%s]" { channel = "sonyprojector:serialconnection:proj3:filmprojection" }
String proj3_motionenhancer "Motion Enhancer [%s]" { channel = "sonyprojector:serialconnection:proj3:motionenhancer" }
String proj3_contrastenhancer "Contrast Enhancer [%s]" { channel = "sonyprojector:serialconnection:proj3:contrastenhancer" }
String proj3_filmmode "Film Mode [%s]" { channel = "sonyprojector:serialconnection:proj3:filmmode" }
String proj3_gammacorrection "Gamma Correction [%s]" { channel = "sonyprojector:serialconnection:proj3:gammacorrection" }
String proj3_colorspace "Color Space [%s]" { channel = "sonyprojector:serialconnection:proj3:colorspace" }
String proj3_nr "Noise Reduction [%s]" { channel = "sonyprojector:serialconnection:proj3:nr" }
String proj3_blocknr "Block Noise Reduction [%s]" { channel = "sonyprojector:serialconnection:proj3:blocknr" }
String proj3_mosquitonr "Mosquito Noise Reduction [%s]" { channel = "sonyprojector:serialconnection:proj3:mosquitonr" }
String proj3_mpegnr "MPEG Noise Reduction [%s]" { channel = "sonyprojector:serialconnection:proj3:mpegnr" }
Switch proj3_xvcolor "x.v.Color" { channel = "sonyprojector:serialconnection:proj3:xvcolor" }
Switch proj3_picturemuting "Picture Muting" { channel = "sonyprojector:serialconnection:proj3:picturemuting" }
String proj3_aspect "Aspect Ratio [%s]" { channel = "sonyprojector:serialconnection:proj3:aspect" }
Switch proj3_overscan "Overscan" { channel = "sonyprojector:serialconnection:proj3:overscan" }
String proj3_pictureposition "Picture Position [%s]" { channel = "sonyprojector:serialconnection:proj3:pictureposition" }
Number proj3_lampusetime "Lamp Use Time [%d]" { channel = "sonyprojector:serialconnection:proj3:lampusetime" }

Switch proj4_power "Power" { channel = "sonyprojector:serialoveripconnection:proj4:power" }
String proj4_powerstate "Power State [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:powerstate" }
String proj4_input "Video Input [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:input" }
String proj4_calibrationpreset "Calibrated Preset [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:calibrationpreset" }
Dimmer proj4_contrast "Contrast [%d]" { channel = "sonyprojector:serialoveripconnection:proj4:contrast" }
Dimmer proj4_brightness "Brightness [%d]" { channel = "sonyprojector:serialoveripconnection:proj4:brightness" }
Dimmer proj4_color "Color [%d]" { channel = "sonyprojector:serialoveripconnection:proj4:color" }
Dimmer proj4_hue "Hue [%d]" { channel = "sonyprojector:serialoveripconnection:proj4:hue" }
Dimmer proj4_sharpness "Sharpness [%d]" { channel = "sonyprojector:serialoveripconnection:proj4:sharpness" }
String proj4_colortemperature "Color Temperature [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:colortemperature" }
String proj4_irismode "Iris Mode [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:irismode" }
Dimmer proj4_irismanual "Iris Manual [%d]" { channel = "sonyprojector:serialoveripconnection:proj4:irismanual" }
String proj4_irissensitivity "Iris Sensitivity [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:irissensitivity" }
String proj4_lampcontrol "Lamp Control [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:lampcontrol" }
String proj4_filmprojection "Film Projection [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:filmprojection" }
String proj4_motionenhancer "Motion Enhancer [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:motionenhancer" }
String proj4_contrastenhancer "Contrast Enhancer [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:contrastenhancer" }
String proj4_filmmode "Film Mode [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:filmmode" }
String proj4_gammacorrection "Gamma Correction [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:gammacorrection" }
String proj4_colorspace "Color Space [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:colorspace" }
String proj4_nr "Noise Reduction [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:nr" }
String proj4_blocknr "Block Noise Reduction [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:blocknr" }
String proj4_mosquitonr "Mosquito Noise Reduction [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:mosquitonr" }
String proj4_mpegnr "MPEG Noise Reduction [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:mpegnr" }
Switch proj4_xvcolor "x.v.Color" { channel = "sonyprojector:serialoveripconnection:proj4:xvcolor" }
Switch proj4_picturemuting "Picture Muting" { channel = "sonyprojector:serialoveripconnection:proj4:picturemuting" }
String proj4_aspect "Aspect Ratio [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:aspect" }
Switch proj4_overscan "Overscan" { channel = "sonyprojector:serialoveripconnection:proj4:overscan" }
String proj4_pictureposition "Picture Position [%s]" { channel = "sonyprojector:serialoveripconnection:proj4:pictureposition" }
Number proj4_lampusetime "Lamp Use Time [%d]" { channel = "sonyprojector:serialoveripconnection:proj4:lampusetime" }
```

example.sitemap:

```perl
    Frame label="Projector" {
        Switch item=proj_power
        Text item=proj_powerstate
        Switch item=proj_input
        Selection item=proj_calibrationpreset
        Slider item=proj_contrast
        Slider item=proj_brightness
        Slider item=proj_color
        Slider item=proj_hue
        Slider item=proj_sharpness
        Selection item=proj_colortemperature
        Selection item=proj_irismode
        Slider item=proj_irismanual
        Switch item=proj_irissensitivity
        Switch item=proj_lampcontrol
        Switch item=proj_filmprojection
        Switch item=proj_motionenhancer
        Switch item=proj_contrastenhancer
        Switch item=proj_filmmode
        Selection item=proj_gammacorrection
        Selection item=proj_colorspace
        Switch item=proj_nr
        Switch item=proj_blocknr
        Switch item=proj_mosquitonr
        Switch item=proj_mpegnr
        Switch item=proj_xvcolor
        Switch item=proj_picturemuting
        Selection item=proj_aspect
        Switch item=proj_overscan
        Selection item=proj_pictureposition
        Text item=proj_lampusetime
    }
    Frame label="Projector" {
        Switch item=proj3_power
        Text item=proj3_powerstate
        Switch item=proj3_input
        Selection item=proj3_calibrationpreset
        Slider item=proj3_contrast
        Slider item=proj3_brightness
        Slider item=proj3_color
        Slider item=proj3_hue
        Slider item=proj3_sharpness
        Selection item=proj3_colortemperature
        Selection item=proj3_irismode
        Slider item=proj3_irismanual
        Switch item=proj3_irissensitivity
        Switch item=proj3_lampcontrol
        Switch item=proj3_filmprojection
        Switch item=proj3_motionenhancer
        Switch item=proj3_contrastenhancer
        Switch item=proj3_filmmode
        Selection item=proj3_gammacorrection
        Selection item=proj3_colorspace
        Switch item=proj3_nr
        Switch item=proj3_blocknr
        Switch item=proj3_mosquitonr
        Switch item=proj3_mpegnr
        Switch item=proj3_xvcolor
        Switch item=proj3_picturemuting
        Selection item=proj3_aspect
        Switch item=proj3_overscan
        Selection item=proj3_pictureposition
        Text item=proj3_lampusetime
    }
    Frame label="Projector" {
        Switch item=proj4_power
        Text item=proj4_powerstate
        Switch item=proj4_input
        Selection item=proj4_calibrationpreset
        Slider item=proj4_contrast
        Slider item=proj4_brightness
        Slider item=proj4_color
        Slider item=proj4_hue
        Slider item=proj4_sharpness
        Selection item=proj4_colortemperature
        Selection item=proj4_irismode
        Slider item=proj4_irismanual
        Switch item=proj4_irissensitivity
        Switch item=proj4_lampcontrol
        Switch item=proj4_filmprojection
        Switch item=proj4_motionenhancer
        Switch item=proj4_contrastenhancer
        Switch item=proj4_filmmode
        Selection item=proj4_gammacorrection
        Selection item=proj4_colorspace
        Switch item=proj4_nr
        Switch item=proj4_blocknr
        Switch item=proj4_mosquitonr
        Switch item=proj4_mpegnr
        Switch item=proj4_xvcolor
        Switch item=proj4_picturemuting
        Selection item=proj4_aspect
        Switch item=proj4_overscan
        Selection item=proj4_pictureposition
        Text item=proj4_lampusetime
    }
```
