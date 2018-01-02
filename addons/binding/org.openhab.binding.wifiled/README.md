# WiFi LED Binding

This binding is used to control LED strips connected by WiFi.
These devices are sold with different names, i.e. Magic Home LED, UFO LED, LED NET controller, etc.  

## Supported Things

The following table shows a list of RGBW(W) LED devices supported by this binding.

Device table with supported channels:

| Device Type | power | color | white | white2 | program | programSpeed |
|-------------|:-----:|:-----:|:-----:|:------:|:-------:|:------------:|
| LD382       |   ✓   |   ✓   |   ✓   |        |    ✓    |      ✓       |
| LD382A      |   ✓   |   ✓   |   ✓   |        |    ✓    |      ✓       |
| LD686       |   ✓   |   ✓   |   ✓   |   ✓    |    ✓    |      ✓       |

Other LD*** devices might work but probably need some small adaptations.

## Discovery

The LED WiFi Controllers can be discovered by triggering a search in openHAB's inbox.
Your device needs to be connected to your local network (i.e. by using the WiFi PBC connection method or the native App shipped with the device).
Read the device manual for more information about how to connect your device to your network.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The thing can be configured through the Paper UI.
Use the configuration if you have devices of type LD382 or LD686, want to enable color fading,
or if the device discovery does not find your LED controller automatically.

### Drivers

You can choose between two drivers with different functionality:

| Driver  | Supports Color Fading | Supports Programs | Polls LED State |
|---------|:---------------------:|:-----------------:|:---------------:|
| CLASSIC |                       |         ✓         |        ✓        |
| FADING  |            ✓          |                   |                 |

While the CLASSIC driver let you choose and run device internal programs (e.g. alternating blue),
all normal operations (turn on or off, switch color, set brightness, ...) are performed immediately
and without any fading effect.

If you prefer to switch colors smoothly and to turn your light on and off by slightly rising/decreasing the brightness
you should try the FADING driver.
If selected you can also set the number of fading steps and the fading duration in the thing configuration.
Note that each fading step will at least take 10 ms for being processed.
This natural limit is given by the speed of the LED controller and your network speed.
Thus, a color fading with a configured fading duration of 0s might still take some time (count with more than 1 second for 100 steps).
IF the "FADING" driver is chosen the program channel and the programSpeed channel will not have any effect.

The polling period is a parameter only used by the CLASSIC driver and specifies a the time in seconds after the LED state is refreshed in openHAB.

### Device Discovery

If the automatic discovery fails you have to set the IP address and the port of your device manually.
Moreover, make sure that the device protocol matches you device type.

## Channels

| Channel Type ID | Item Type | Description                                                            | Access |
|-----------------|-----------|------------------------------------------------------------------------|--------|
| power           | Switch    | Power state of the LEDs (ON/OFF)                                       | R/W    |
| color           | Color     | Color of the RGB LEDs                                                  | R/W    |
| white           | Dimmer    | Brightness of the first (warm) white LEDs (min=0, max=100)             | R/W    |
| white2          | Dimmer    | Brightness of the second (warm) white LEDs (min=0, max=100)            | R/W    |
| program         | String    | Program to run by the controller (i.e. color cross fade, strobe, etc.) | R/W    |
| programSpeed    | Dimmer    | Speed of the program                                                   | R/W    |

## Example

Usually, there is no need to define your WiFi LED controllers via configuration files.
However, if you like to do it, here is an example.

wifiled.things:

    Thing wifiled:wifiled:F0FE6B19CB2A [ ip="192.168.178.91", port=5577, pollingPeriod=3000, protocol="LD686", driver="CLASSIC", fadeDurationInMs=1000, fadeSteps=100 ]

wifiled.items:

    Switch MyWiFiLight_power "Power" (Light) {channel="wifiled:wifiled:F0FE6B19CB2A:power"}
    Dimmer MyWiFiLight_white "White" (Light) {channel="wifiled:wifiled:F0FE6B19CB2A:white"}
    Color  MyWiFiLight_color "Color" (Light) {channel="wifiled:wifiled:F0FE6B19CB2A:color"}
