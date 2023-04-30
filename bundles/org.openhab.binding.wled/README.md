# WLED Binding

This binding allows you to auto discover and use LED strings based on the WLED project:
<https://github.com/Aircoookie/WLED>

## Supported Things

| Thing Type ID | Description |
|-|-|
| `json` | A bridge to a WLED device using the JSON API. Add this thing first. |
| `segment` | A segment is used to turn a LED strip or string, into 1 or more lights. Each segment is like a separate light globe that can have its own color or effect. |

## Discovery

The auto discovery will find your WLED if your network supports mDNS and the UDP port 5353 is not blocked by a fire wall.
Before discovering any WLED devices, you may wish to name them by providing a 'Server description' in the WLED web page, CONFIG>User Interface> setup page.
openHAB will then discover and auto name your WLED bridge thing to the name provided as the 'Server description'.
Segments will be discovered with an Inbox scan after the bridge thing is first showing up as ONLINE.
Any segments that have been given a name in the WLED firmware, will be given the same name when discovery adds them to the Inbox.

## Bridge Thing Configuration

| Parameter | Description | Required | Default |
|-|-|-|-|
| `address`| The full URL to your WLED device. Example is `http://192.168.0.2:80` | Y | |
| `pollTime`| How often in seconds you want the states of the LED fetched in case you make changes with a non openHAB app, web browser, or the light is auto changing FX or presets. | Y | 10 |
| `saturationThreshold` | Allows you to use a colorpicker control linked to the `masterControls` channel to trigger only using the pure white LEDs instead of creating fake white light from the RGB channels. Try setting the value to 12 or leave this on 0 for RGB strings. | Y | 0 |

## Bridge Thing Channels

| Channel | Type | Description |
|-|-|-|
| `globalBrightness` | Dimmer | Changes the brightness of all segments at the same time. |
| `presets` | String |  A list of presets that you can select from and will display -1 when no presets are running.  |
| `playlists` | String | A list of playlists that you can select from and will display -1 when none are running. |
| `presetCycle` | Switch | Turns ON/OFF the automatic changing from one preset to the next. Only in V0.12.0 and older firmwares. |
| `presetDuration` | Number:Time | How long in seconds it will display a preset for, before it begins to change from one preset to the next with `presetCycle` turned ON. Only in V0.12.0 and older firmwares. |
| `transformTime` | Number:Time | How long in seconds it takes to transform/morph from one look to the next. |
| `sleep` | Switch | Turns on the sleep or 'night light' timer which can be configured to work in many different ways. Refer to WLED documentation for how this can be setup. The default action is the light will fade to OFF over the next 60 minutes. |
| `sleepMode` | String | Timed Light Mode selects how the light will fade or increase when the sleep timer is turned ON. |
| `sleepDuration` | Number:Time | Time it takes to change/fade to the target brightness. |
| `sleepTargetBrightness` | Dimmer | Sets how bright the light will be after the sleep duration time has expired. |
| `syncSend` | Switch | Sends UDP packets that tell other WLED lights to follow this one. |
| `syncReceive` | Switch | Allows UDP packets from other WLED lights to control this one. |
| `liveOverride` | String | A value of "0" turns off, "1" will override live data to display what you want, and "2" overrides until you reboot the ESP device. |

## Thing Configuration

| Parameter | Description | Required | Default |
|-|-|-|-|
| `segmentIndex` | The index number to the LED segment you wish these channels to control. Leave on 0 if you do not know what a segment is. | Y | 0 |

## Thing Channels

| Channel | Type | Description |
|-|-|-|
| `masterControls` | Color | Gives you control over the WLED segment like it is any normal light. Tag this control for Alexa or Google/Nest to change the lights instantly to any colour, brightness or on/off state that you ask for regardless of what mode the light is in. |
| `segmentBrightness` | Dimmer | Allows you to Dim and turn the entire segment ON and OFF. |
| `primaryColor` | Color | The primary colour used in FX. |
| `primaryWhite` | Dimmer | The amount of white light used in the primary colour. Only available if you have RGBW LEDs. |
| `secondaryColor` | Color | The secondary colour used in FX. |
| `secondaryWhite` | Dimmer | The amount of white light used in the second colour. Only available if you have RGBW LEDs. |
| `tertiaryColor` | Color | The third colour used in FX. |
| `tertiaryWhite` | Dimmer | The amount of white light used in the third colour. Only available if you have RGBW LEDs. |
| `palettes` | String | A list of colour palettes you can select from that are used in the FX. |
| `fx` | String |  A list of Effects you can select from. |
| `speed` | Dimmer | Changes the speed of the loaded effect. |
| `intensity` | Dimmer | Changes the intensity of the loaded effect. |
| `mirror` | Switch | Mirror the effect for this segment. |
| `reverse` | Switch | Reverse the effect for this segment. |
| `grouping` | Number | The number of LEDs that are grouped together to display as one pixel in FX. Use metadata to display a list widget slider. |
| `spacing` | Number | The number of LEDs that will not light up in between FX pixels. Use metadata to display a list widget slider. |

## Rule Actions

This binding has two rule Actions `savePreset(int presetNumber)` and `savePreset(int presetNumber, String presetName)` which can save the current state of the WLED string into a preset slot that you can specify.

In Xtend rules, you can use the Actions like this.

```java
getActions("wled", "wled:json:XmasTree").savePreset(5,"Flashy Preset")
```

## Sitemap Example

If you use the ADMIN>MODEL>`Create equipment from thing` feature you can use the below and just change the name before the underscore to match what you named the `segment` thing when it was added via the Inbox.

*.sitemap

```perl
        Text label="XmasLights" icon="rgb"{
            Switch item=XmasTree_MasterControls
            Slider item=XmasTree_SegmentBrightness
            Colorpicker item=XmasTree_MasterControls
            Switch item=XmasTree_SleepTimer
            Colorpicker item=XmasTree_PrimaryColor
            Colorpicker item=XmasTree_SecondaryColor
            Selection item=XmasTree_Effect
            Selection item=XmasTree_Palettes
            Selection item=XmasTree_Presets
            Default item=XmasTree_FXSpeed
            Default item=XmasTree_FXIntensity            
            Selection item=XmasTree_TransformTime mappings=[0='0 seconds', 2 ='2 seconds', 10='10 seconds', 30='30 seconds', 60='60 seconds']
        }
        
```
