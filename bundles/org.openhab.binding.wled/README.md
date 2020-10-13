# WLED Binding

This openHAB binding allows you to auto discover and use LED strings based on the WLED project:
<https://github.com/Aircoookie/WLED>

## Fault Finding

To watch what the binding does you can enter this in to the openHAB console, `log:set TRACE org.openhab.binding.wled` which will allow you to test the same commands in a web browser to determine if it is a bug in the binding, or in the firmware for WLED.
Firmware 0.10.2 is working very well with this binding after extensive testing, so if an issue is found please report what firmware version you are using.

## Supported Things

| Thing Type ID | Description |
|-|-|
| `wled` | Use this for RGB and RGBW strings. |

## Discovery

The auto discovery will work with this binding if your network supports mDNS.
If it fails to find your WLED, you can manually add a `wled` thing by using the UI or textual methods.
The full example section below gives everything needed to quickly setup using textual config.

## Thing Configuration

| Parameter | Description |
|-|-|
| `address`| The full URL to your WLED device. Example is `http://192.168.0.2:80` |
| `pollTime`| How often you want the states of the LED fetched in case you make changes with a non openHAB app, web browser, or the light is auto changing FX or presets. |
| `saturationThreshold` | Allows you to use a colorpicker control linked to the `masterControls` channel to trigger only using the pure white LEDs when your using RGBW strings instead of creating fake white light from the RGB channels. Try setting the value to 12 or for RGB strings, leave this on 0. |

## Channels

| Channel | Type | Description |
|-|-|-|
| `masterControls` | Color | Gives you control over the WLED like it is any normal light. Tag this control for Alexa or Google/Nest to change the lights instantly to any colour, brightness or on/off state that you ask for regardless of what mode the light is in. |
| `primaryColor` | Color | The primary colour used in FX. |
| `primaryWhite` | Dimmer | The amount of white light used in the primary colour if you have RGBW LEDs. |
| `secondaryColor` | Color | The secondary colour used in FX. |
| `secondaryWhite` | Dimmer | The amount of white light used in the secondary colour if you have RGBW LEDs. |
| `palettes` | String | A list of colour palettes you can select from that are used in the FX. |
| `fx` | String |  A list of Effects you can select from. |
| `speed` | Dimmer | Changes the speed of the loaded effect. |
| `intensity` | Dimmer | Changes the intensity of the loaded effect. |
| `presets` | String |  A list of presets that you can select from.  |
| `presetCycle` | Switch | Turns ON/OFF the automatic changing from one preset to the next. |
| `presetDuration` | Dimmer | How long it will display a preset for, before it begins to change from one preset to the next with `presetCycle` turned ON. |
| `transformTime` | Dimmer | How long it takes to transform/morph from one look to the next. |
| `sleep` | Switch | Turns on the sleep timer. |

## Full Example

*.things

```
Thing wled:wled:XmasTree "My Christmas Tree" @ "Lights" [address="http://192.168.0.4:80"]
```

*.items

```
Color XmasTree_MasterControls "Christmas Tree" ["Lighting"] {channel="wled:wled:ChristmasTree:masterControls"}
Color XmasTree_PrimaryColor "Primary Color"    {channel="wled:wled:ChristmasTree:primaryColor"}
Color XmasTree_SecondaryColor   "Secondary Color"  {channel="wled:wled:ChristmasTree:secondaryColor"}
String XmasTree_Effect      "FX"        <text>{channel="wled:wled:ChristmasTree:fx"}
String XmasTree_Palettes  "Palette"   <colorwheel>    {channel="wled:wled:ChristmasTree:palettes"}
String XmasTree_Presets  "Preset"    <text> {channel="wled:wled:ChristmasTree:presets"}
Dimmer XmasTree_FXSpeed    "FX Speed"  <time>  {channel="wled:wled:ChristmasTree:speed"}
Dimmer XmasTree_FXIntensity "FX Intensity" {channel="wled:wled:ChristmasTree:intensity"}
Switch XmasTree_PresetCycle "presetCycle" <time> {channel="wled:wled:ChristmasTree:presetCycle"}
Dimmer XmasTree_PresetDuration "presetDuration" <time> {channel="wled:wled:ChristmasTree:presetDuration"}
Dimmer XmasTree_TransformTime "presetTransformTime" <time> {channel="wled:wled:ChristmasTree:transformTime"}
Switch XmasTree_Sleep    "Sleep"     <moon> {channel="wled:wled:ChristmasTree:sleep"}

```

*.sitemap

```
Text label="XmasLights" icon="rgb"{
            Switch item=XmasTree_MasterControls
            Slider item=XmasTree_MasterControls
            Colorpicker item=XmasTree_MasterControls
            Switch item=XmasTree_Sleep
            Colorpicker item=XmasTree_PrimaryColor
            Colorpicker item=XmasTree_SecondaryColor
            Selection item=XmasTree_Effect
            Selection item=XmasTree_Palettes
            Selection item=XmasTree_Presets
            Default item=XmasTree_FXSpeed
            Default item=XmasTree_FXIntensity
            Default item=XmasTree_PresetCycle
            Default item=XmasTree_PresetDuration
            Default item=XmasTree_TransformTime
        }
        
```
