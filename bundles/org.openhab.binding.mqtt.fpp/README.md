# FPP Binding


Note that the group 0 (or ALL group) is not auto discovered as a thing and thus has to be added manually if needed.

## Thing Configuration

| Parameter | Description | Required | Default |
|-|-|-|-|
| `whiteHue` | When both the `whiteHue` and `whiteSat` values are seen by the binding it will trigger the white LEDS. Set to -1 to disable, 0 for Alexa, or 35 for Google Home. | Y | 35 |
| `whiteSat` | When both the whiteHue and whiteSat values are seen by the binding it will trigger the white LEDS. Set to -1 to disable, 100 for Alexa or 32 for Google Home. | Y | 32 |
| `favouriteWhite` | When one of the shortcuts triggers white mode, use this for the colour white instead of the default colour. | Y |200 |
| `dimmedCT` | Traditional globes grow warmer the more they are dimmed. Set this to 370, or leave blank to disable. | N | blank |
| `oneTriggersNightMode` | Night mode is a much lower level of light and this feature allows it to be auto selected when your fader/slider moves to 1%. NOTE: Night mode by design locks out some controls of a physical remote, so this feature is disabled by default. | Y | false |
| `powerFailsToMinimum` | If lights loose power from the power switch OR a power outage, they will default to using the lowest brightness if the light was turned off before the power failure occurred. | Y | true |
| `whiteThreshold` | This feature allows you to use a color control to change to using the real white LEDs when the saturation is equal to, or below this threshold. -1 will disable this feature. | Y | 12 |
| `duvThreshold` | This feature allows you to use a color control to change to using the real warm/cool white LEDs to set a white color temperature if the color is within a certain threshold of the block body curve. 1 will effectively disable this feature. The default settings maps well to Apple's HomeKit that will allow you to choose a color temperature, but sends it as an HSB value. See <https://www.waveformlighting.com/tech/calculate-duv-from-cie-1931-xy-coordinates/> for more information. | Y | 0.003 |

## Channels

| Channel | Type | Description |
|-|-|-|
| `level` | Dimmer | Level changes the brightness of the globe. Not present if the bulb supports the `colour` channel. |
| `colourTemperature` | Dimmer | Change from cool to warm white with this control. |
| `colourTemperatureAbs` | Number:Temperature | Colour temperature in mireds. |
| `colour` | Color | Allows you to change the colour, brightness and saturation of the globe. Can also be linked directly with a Dimmer item if you happen to have a bulb that doesn't support colour, but is controlled by a remote that normally does support colour. |
| `discoMode` | String | Switch to a Disco mode directly from a drop down list. |
| `bulbMode` | String (read only) | Displays the mode the bulb is currently in so that rules can determine if the globe is white, a color, disco modes or night mode are selected. |
| `command` | String | Sends the raw commands that the buttons on a remote send. |


## Full Example

To use these examples for textual configuration, you must already have a configured MQTT `broker` thing, and know its unique ID.
This UID will be used in the things file and will replace the text `myBroker`.
The first line in the things file will create a `broker` thing and this can be removed if you have already setup a broker in another file or via the UI already.

*.things

```java
Bridge mqtt:broker:myBroker [ host="localhost", secure=false, password="*******", qos=1, username="user"]
Thing mqtt:rgb_cct:myBroker:0xE6C4 "Hallway" (mqtt:broker:myBroker) @ "MQTT"
```

*.items

```java
Dimmer Hallway_Level "Front Hall" {channel="mqtt:rgb_cct:myBroker:0xE6C4:colour"}
Dimmer Hallway_ColourTemperature "White Color Temp" {channel="mqtt:rgb_cct:myBroker:0xE6C4:colourTemperature"}
Number:Temperature Hallway_ColourTemperatureK "White Color Temp [%d %unit%]" {channel="mqtt:rgb_cct:myBroker:0xE6C4:colourTemperatureAbs", unit="K"}
Color  Hallway_Colour "Front Hall" ["Lighting"] {channel="mqtt:rgb_cct:myBroker:0xE6C4:colour"}
String Hallway_DiscoMode "Disco Mode" {channel="mqtt:rgb_cct:myBroker:0xE6C4:discoMode"}
String Hallway_BulbCommand "Send Command" {channel="mqtt:rgb_cct:myBroker:0xE6C4:command"}
String Hallway_BulbMode "Bulb Mode" {channel="mqtt:rgb_cct:myBroker:0xE6C4:bulbMode"}

```

*.sitemap

```perl
Text label="Hallway" icon="light"
{
    Switch      item=Hallway_Level
    Slider      item=Hallway_Level
    Slider      item=Hallway_ColourTemperature
    Colorpicker item=Hallway_Colour
    Selection   item=Hallway_DiscoMode
    Text        item=Hallway_BulbMode
    Switch item=Hallway_BulbCommand mappings=[next_mode='Mode +', previous_mode='Mode -', mode_speed_up='Speed +', mode_speed_down='Speed -', set_white='White', night_mode='Night' ]
}
```
