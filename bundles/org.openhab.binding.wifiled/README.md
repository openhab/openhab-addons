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

The LED WiFi Controllers can be auto discovered.
Your device needs to be connected to your local network (i.e. by using the WiFi PBC connection method or the native App shipped with the device).
Read the device manual for more information about how to connect your device to your network.

## Binding Configuration

No binding configuration required.

## Thing Configuration

Use the configuration if you have devices of type LD382 or LD686, want to enable color fading,
or if the device discovery does not find your LED controller automatically.

### Drivers

You can choose between two drivers with different functionality:

| Driver  | Supports Color Fading | Supports Programs | Polls LED State |
|---------|:---------------------:|:-----------------:|:---------------:|
| CLASSIC |                       |         ✓         |        ✓        |
| FADING  |            ✓          |                   |                 |

While the CLASSIC driver lets you choose and run device internal programs (e.g. alternating blue),
all normal operations (e.g. turn on or off, switch color, etc.) are performed immediately and
without any fading effect.

If you prefer to switch colors smoothly and to turn your light on and off by slightly increasing/decreasing the brightness, you should try the FADING driver.
If selected, you can also set the number of fading steps and the fading duration in the Thing configuration.
Each fading step will at least take 10 ms to be processed.
This limit comes from the speed of the LED controller and your network speed.
Thus a color fading with a configured fading duration of 0s might still take some time; count on more than 1 second for 100 steps.
If the FADING driver is chosen, the program and the programSpeed channels will not have any effect.

The polling period is a parameter only used by the CLASSIC driver and specifies the time in seconds after the LED state is refreshed in openHAB.

### Device Discovery

If the automatic discovery fails, you have to set the IP address and the port of your device manually.
Make sure that the device protocol matches your device type.

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
However, here is an example.

wifiled.things:

```java
Thing wifiled:wifiled:F0FE6B19CB2A [ ip="192.168.178.91", port=5577, pollingPeriod=3000, protocol="LD686", driver="CLASSIC", fadeDurationInMs=1000, fadeSteps=100 ]
```

wifiled.items:

```java
Switch MyWiFiLight_power "Power"     (Light) {channel="wifiled:wifiled:F0FE6B19CB2A:power"}
Dimmer MyWiFiLight_white "White"     (Light) {channel="wifiled:wifiled:F0FE6B19CB2A:white"}
Color  MyWiFiLight_color "Color"     (Light) {channel="wifiled:wifiled:F0FE6B19CB2A:color"}
Dimmer MyWiFiLight_speed "Speed_LED" (Light) {channel="wifiled:wifiled:F0FE6B19CB2A:programSpeed"}

String LED_1_program "LED 1 Preset [MAP(led_program.map):%s]" <menu>  {channel="wifiled:wifiled:F0FE6B19CB2A:program"}
Switch LED_1_preset_0     "Strobe"                 <smoke>        (LEDPresets1)     // Mode: 48
Switch LED_1_preset_1     "Strobe - Custom 1"      <smoke>        (LEDPresets1)     // Mode: 96
Switch LED_1_preset_2     "Strobe - Red"           <smoke>        (LEDPresets1)     // Mode: 49
Switch LED_1_preset_3     "Strobe - Green"         <smoke>        (LEDPresets1)     // Mode: 50
Switch LED_1_preset_4     "Strobe - Blue"          <smoke>        (LEDPresets1)     // Mode: 51
Switch LED_1_preset_5     "Strobe - Cyan"          <smoke>        (LEDPresets1)     // Mode: 53
Switch LED_1_preset_6     "Strobe - Purple"        <smoke>        (LEDPresets1)     // Mode: 54
Switch LED_1_preset_7     "Strobe - Yellow"        <smoke>        (LEDPresets1)     // Mode: 52
Switch LED_1_preset_8     "Strobe - White"         <smoke>        (LEDPresets1)     // Mode: 55
Switch LED_1_preset_9     "Strobe - R/G/B"         <smoke>        (LEDPresets1)     // Mode: 57
Switch LED_1_preset_10    "CrossFade - Red/Green"  <flow>         (LEDPresets1)     // Mode: 45
Switch LED_1_preset_11    "CrossFade - Red/Blue"   <flow>         (LEDPresets1)     // Mode: 46
Switch LED_1_preset_12    "CrossFade - Blue/Green" <flow>         (LEDPresets1)     // Mode: 47
Switch LED_1_preset_13    "Fade"                   <colorlight>   (LEDPresets1)     // Mode: 37
Switch LED_1_preset_14    "Fade - White"           <colorlight>   (LEDPresets1)     // Mode: 44
Switch LED_1_preset_15    "Fade - Green"           <colorlight>   (LEDPresets1)     // Mode: 39
Switch LED_1_preset_16    "Fade - DarkBlue"        <colorlight>   (LEDPresets1)     // Mode: 40
Switch LED_1_preset_17    "Fade - Giallo"          <colorlight>   (LEDPresets1)     // Mode: 41
Switch LED_1_preset_18    "Fade - Red"             <colorlight>   (LEDPresets1)     // Mode: 38
Switch LED_1_preset_19    "Fade - LightBlue"       <colorlight>   (LEDPresets1)     // Mode: 42
Switch LED_1_preset_20    "Fade - Purple"          <colorlight>   (LEDPresets1)     // Mode: 43
Switch LED_1_preset_21    "Fade - R/G/B"           <colorlight>   (LEDPresets1)     // Mode: 45
Switch LED_1_preset_22    "Jump"                   <chart>        (LEDPresets1)     // Mode: 56
Switch LED_1_preset_23    "Jump - R/G/B"           <chart>        (LEDPresets1)     // Mode: 99
```

wifiled.sitemap

```perl
sitemap wifiled label="LED Sitemap"
{

    Frame {
        Switch item=MyWiFiLight_power
        Colorpicker item=MyWiFiLight_color visibility=[MyWiFiLight_power==ON]
        Slider item=MyWiFiLight_white      visibility=[MyWiFiLight_power==ON]
        Slider item=MyWiFiLight_speed      visibility=[MyWiFiLight_power==ON]

        Selection item=LED_1_program  visibility=[MyWiFiLight_power==ON]

        Switch item=LED_1_preset_0   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_1   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_2   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_3   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_4   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_5   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_6   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_7   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_8   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_9   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_10   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_11   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_12   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_13   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_14   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_15   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_16   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_17   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_18   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_19   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_20   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_21   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_22   visibility=[MyWiFiLight_power==ON]
        Switch item=LED_1_preset_23   visibility=[MyWiFiLight_power==ON]
    }
}
```

led_program.map

```text
-=Undefined
NULL=Undefined
48=Strobe
49=Strobe - Red
50=Strobe - Green
51=Strobe - Blue
52=Strobe - Yellow
53=Strobe - Cyan
54=Strobe - Purple
55=Strobe - White
57=Strobe - R/G/B
96=Strobe - Custom 1

45=CrossFade - Red/Green
46=CrossFade - Red/Blue
47=CrossFade - Blue/Green

37=Fade
38=Fade - Red
39=Fade - Green
40=Fade - DarkBlue
41=Fade - Yellow
42=Fade - LightBlue
43=Fade - Purple
44=Fade - White
45=Fade - R/G/B

56=Jump
99=Jump - R/G/B

97=Color (no preset selected)
```

wifiled.rules

```java
rule "Program - Strobe"
when Item LED_1_preset_0 received command ON
then
    LED_1_program.sendCommand(48)
    LED_1_preset_0.sendCommand(OFF)
end

rule "Program - Strobe - Custom 1"
when Item LED_1_preset_1 received command ON
then
    LED_1_program.sendCommand(96)
    LED_1_preset_1.sendCommand(OFF)
end

rule "Program - Strobe - Red"
when Item LED_1_preset_2 received command ON
then
    LED_1_program.sendCommand(49)
    LED_1_preset_2.sendCommand(OFF)
end

rule "Program - Strobe - Green"
when Item LED_1_preset_3 received command ON
then
    LED_1_program.sendCommand(50)
    LED_1_preset_3.sendCommand(OFF)
end

rule "Program - Strobe - Blue"
when Item LED_1_preset_4 received command ON
then
    LED_1_program.sendCommand(51)
    LED_1_preset_4.sendCommand(OFF)
end

rule "Program - Strobe - Cyan"
when Item LED_1_preset_5 received command ON
then
    LED_1_program.sendCommand(53)
    LED_1_preset_5.sendCommand(OFF)
end

rule "Program - Strobe - Purple"
when Item LED_1_preset_6 received command ON
then
    LED_1_program.sendCommand(54)
    LED_1_preset_6.sendCommand(OFF)
end

rule "Program - Strobe - Yellow"
when Item LED_1_preset_7 received command ON
then
    LED_1_program.sendCommand(52)
    LED_1_preset_7.sendCommand(OFF)
end

rule "Program - Strobe - White"
when Item LED_1_preset_8 received command ON
then
    LED_1_program.sendCommand(55)
    LED_1_preset_8.sendCommand(OFF)
end

rule "Program - Strobe - R/G/B"
when Item LED_1_preset_9 received command ON
then
    LED_1_program.sendCommand(57)
    LED_1_preset_9.sendCommand(OFF)
end

rule "Program - CrossFade - Red/Green"
when Item LED_1_preset_10 received command ON
then
    LED_1_program.sendCommand(45)
    LED_1_preset_10.sendCommand(OFF)
end

rule "Program - CrossFade - Red/Blue"
when Item LED_1_preset_11 received command ON
then
    LED_1_program.sendCommand(46)
    LED_1_preset_11.sendCommand(OFF)
end

rule "Program - CrossFade - Blue/Green"
when Item LED_1_preset_12 received command ON
then
    LED_1_program.sendCommand(47)
    LED_1_preset_12.sendCommand(OFF)
end

rule "Program - Fade"
when Item LED_1_preset_13 received command ON
then
    LED_1_program.sendCommand(37)
    LED_1_preset_13.sendCommand(OFF)
end

rule "Program - Fade - White"
when Item LED_1_preset_14 received command ON
then
    LED_1_program.sendCommand(44)
    LED_1_preset_14.sendCommand(OFF)
end

rule "Program - Fade - Green"
when Item LED_1_preset_15 received command ON
then
    LED_1_program.sendCommand(39)
    LED_1_preset_15.sendCommand(OFF)
end

rule "Program - Fade - DarkBlue"
when Item LED_1_preset_16 received command ON
then
    LED_1_program.sendCommand(40)
    LED_1_preset_16.sendCommand(OFF)
end

rule "Program - Fade - Yellow"
when Item LED_1_preset_17 received command ON
then
    LED_1_program.sendCommand(41)
    LED_1_preset_17.sendCommand(OFF)
end

rule "Program - Fade - Red"
when Item LED_1_preset_18 received command ON
then
    LED_1_program.sendCommand(38)
    LED_1_preset_18.sendCommand(OFF)
end

rule "Program - Fade - LightBlue"
when Item LED_1_preset_19 received command ON
then
    LED_1_program.sendCommand(42)
    LED_1_preset_19.sendCommand(OFF)
end

rule "Program - Fade - Purple"
when Item LED_1_preset_20 received command ON
then
    LED_1_program.sendCommand(43)
    LED_1_preset_20.sendCommand(OFF)
end

rule "Program - Fade - R/G/B"
when Item LED_1_preset_21 received command ON
then
    LED_1_program.sendCommand(45)
    LED_1_preset_21.sendCommand(OFF)
end

rule "Program - Jump"
when Item LED_1_preset_22 received command ON
then
    LED_1_program.sendCommand(56)
    LED_1_preset_22.sendCommand(OFF)
end

rule "Program - Jump - R/G/B"
when Item LED_1_preset_23 received command ON
then
    LED_1_program.sendCommand(99)
    LED_1_preset_23.sendCommand(OFF)
end
```
