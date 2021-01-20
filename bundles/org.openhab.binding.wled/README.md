# WLED Binding

This binding allows you to auto discover and use LED strings based on the WLED project:
<https://github.com/Aircoookie/WLED>

## Supported Things

| Thing Type ID | Description |
|-|-|
| `wled` | Use this for RGB and RGBW strings. |

## Discovery

The auto discovery will find your WLED if your network supports mDNS and the UDP port 5353 is not blocked by a fire wall.
Before discovering any WLED devices, you may wish to name them by providing a 'Server description' in the WLED web page, CONFIG>User Interface> setup page.
openHAB will then discover and auto name your WLED to the name provided as the 'Server description'.
If it fails to find your WLED, you can still manually add a `wled` thing by using the UI or textual methods.
For multiple segments, the binding will only auto find the first segment.
For additional segments, you can add them manually and set the `segmentIndex` config to the correct number shown in the WLED control web page.

## Thing Configuration

| Parameter | Description | Required | Default |
|-|-|-|-|
| `address`| The full URL to your WLED device. Example is `http://192.168.0.2:80` | Y | |
| `pollTime`| How often in seconds you want the states of the LED fetched in case you make changes with a non openHAB app, web browser, or the light is auto changing FX or presets. | Y | 10 |
| `segmentIndex` | The index number to the LED segment you wish these channels to control. Leave on -1 if you do not know what a segment is. | Y | -1 |
| `saturationThreshold` | Allows you to use a colorpicker control linked to the `masterControls` channel to trigger only using the pure white LEDs instead of creating fake white light from the RGB channels. Try setting the value to 12 or leave this on 0 for RGB strings. | Y | 0 |

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
| `presetDuration` | Number:Time | How long in seconds it will display a preset for, before it begins to change from one preset to the next with `presetCycle` turned ON. |
| `transformTime` | Number:Time | How long in seconds it takes to transform/morph from one look to the next. |
| `sleep` | Switch | Turns on the sleep or 'night light' timer which can be configured to work in many different ways. Refer to WLED documentation for how this can be setup. The default action is the light will fade to OFF over the next 60 minutes. |
| `syncSend` | Switch | Sends UDP packets that tell other WLED lights to follow this one. |
| `syncReceive` | Switch | Allows UDP packets from other WLED lights to control this one. |

## Rule Actions

This binding has a rule Action `savePreset(int presetNumber)` which can save the current state of the WLED string into a preset slot that you can specify.
Currently 1 to 16 are valid preset slots.

In Xtend rules, you can use the Actions like this.

```
getActions("wled", "wled:wled:XmasTree").savePreset(5)
```

## Sitemap Example

If you use the ADMIN>MODEL>`Create equipment from thing` feature you can use the below and just change the name before the underscore to match what you named the `wled` thing when it was added via the Inbox.

*.sitemap

```
        Text label="XmasLights" icon="rgb"{
            Switch item=XmasTree_MasterControls
            Slider item=XmasTree_MasterControls
            Colorpicker item=XmasTree_MasterControls
            Switch item=XmasTree_SleepTimer
            Colorpicker item=XmasTree_PrimaryColor
            Colorpicker item=XmasTree_SecondaryColor
            Selection item=XmasTree_Effect
            Selection item=XmasTree_Palettes
            Selection item=XmasTree_Presets
            Default item=XmasTree_FXSpeed
            Default item=XmasTree_FXIntensity
            Default item=XmasTree_PresetCycle
            Selection item=XmasTree_PresetDuration mappings=[2 ='2 seconds', 10='10 seconds', 30='30 seconds', 60='60 seconds']
            Selection item=XmasTree_TransformTime mappings=[0='0 seconds', 2 ='2 seconds', 10='10 seconds', 30='30 seconds', 60='60 seconds']
        }
        
```
