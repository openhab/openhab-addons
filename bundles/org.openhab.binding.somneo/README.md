# Philips Somneo Binding

This binding integrates Philips Somneo HF367X into openHAB.

## Supported Things

This binding does only support one Thing:

- `Philips Somneo HF367X`: A connected sleep and wake-Up light with the ThingTypeUID `hf367x`

## Thing Configuration

The Philips Somneo thing requires the `hostname` it can connect to.
Its API only allows HTTPS access, but unfortunately the SSL certificate is not trusted and must be ignored by the parameter.

| Parameter           | Values                                    | Default |
|---------------------|-------------------------------------------|---------|
| hostname            | Hostname or IP address of the device      | -       |
| port                | Port number                               | 443     |
| refreshInterval     | Interval the device is polled in sec      | 30      |
| ignoreSSLErrors     | Ignore SSL Errors                         | true    |

## Channels

| Channel               | Type                 | Read/Write | Description                                                 |
|-----------------------|----------------------|------------|-------------------------------------------------------------|
| _Sensor_              |                      |            |                                                             |
| sensor#illuminance    | Number:Illuminance   | R          | The current illuminance in lux                              |
| sensor#temperature    | Number:Temperature   | R          | The current temperature                                     |
| sensor#humidity       | Number:Dimensionless | R          | The current humidity in %                                   |
| sensor#noise          | Number:Dimensionless | R          | The current noise in dB                                     |
| _Light_               |                      |            |                                                             |
| light#main            | Switch               | RW         | Turn the light on, off and set the brightness               |
| light#night           | Switch               | RW         | Turn the night light on or off                              |
| _Sunset_              |                      |            |                                                             |
| sunset#switch         | Switch               | RW         | Turn the sunset program on or off                           |
| sunset#remainingTime  | Number:Time          | R          | Remaining time from an activated program                    |
| sunset#lightIntensity | Dimmer               | RW         | Set the brightness during the sunset programme              |
| sunset#duration       | Number:Time          | RW         | The duration of sunset program in minutes                   |
| sunset#colorSchema    | Number               | RW         | Choose a personal sunset                                    |
| sunset#ambientNoise   | String               | RW         | Ambient noise played during the sunset                      |
| sunset#volume         | Dimmer               | RW         | Set the volume during the sunset programme                  |
| _Relax_               |                      |            |                                                             |
| relax#switch          | Switch               | RW         | Turn the relax breathe program on or off                    |
| relax#remainingTime   | Number:Time          | R          | Remaining time from an activated program                    |
| relax#breathingRate   | Number               | RW         | Breathing rate per minute during the relax program          |
| relax#duration        | Number:Time          | RW         | The duration of breathe program in minutes                  |
| relax#guidanceType    | Number               | RW         | Select a breath guidance type during the relax program      |
| relax#lightIntensity  | Dimmer               | RW         | Set the brightness during the breathe programme             |
| relax#volume          | Dimmer               | RW         | Set the volume during the breathe programme                 |
| _Audio_               |                      |            |                                                             |
| audio#radio           | Player               | RW         | Controlling the radio and seeking for a frequency           |
| audio#aux             | Switch               | RW         | Turn the AUX input on or off                                |
| audio#volume          | Dimmer               | RW         | Change the sound volume of the device                       |
| audio#preset          | String               | RW         | The Device has 5 presets to store radio frequencies         |
| audio#frequency       | String               | R          | The currently selected radio frequency                      |

## Full Example

somneo.things:

```
Thing somneo:hf367x:1 "Philips Somneo" @ "Bedroom" [ hostname="192.168.0.110", ignoreSSLErrors=true ]
```

somneo.items:

```
// Sensors
Number:Illuminance   PhilipsSomneo_Illuminance "Illuminance" <Sun>         ["Measurement", "Light"]       { channel="somneo:hf367x:1:sensor#illuminance" }
Number:Temperature   PhilipsSomneo_Temperature "Temperature" <Temperature> ["Measurement", "Temperature"] { channel="somneo:hf367x:1:sensor#temperature" }
Number:Dimensionless PhilipsSomneo_Humidity    "Humidity"    <Humidity>    ["Measurement", "Humidity"]    { channel="somneo:hf367x:1:sensor#humidity" }
Number:Dimensionless PhilipsSomneo_Noise       "Noise"       <Noise>       ["Measurement", "Noise"]       { channel="somneo:hf367x:1:sensor#noise" }
// Light
Dimmer PhilipsSomneo_MainLight       "Light"        <Light> ["Control", "Light"] { channel="somneo:hf367x:1:light#main" }
Switch PhilipsSomneo_NightLite       "Night Light"  <Light> ["Control", "Light"] { channel="somneo:hf367x:1:light#night" }
// Sunset
Switch      PhilipsSomneo_SunsetSwitch    "Sunset Program"  <Light>       ["Switch", "Power"]             { channel="somneo:hf367x:1:sunset#switch" }
Number:Time PhilipsSomneo_SunsetRemaining "Remaining Time"  <Time>        ["Status", "Duration"]          { channel="somneo:hf367x:1:sunset#remainingTime" }
Dimmer      PhilipsSomneo_SunsetIntensity "Light Intensity" <Light>       ["Control", "Light"]            { channel="somneo:hf367x:1:sunset#lightIntensity" }
Number:Time PhilipsSomneo_SunsetDuration  "Duration"        <Time>        ["Control", "Duration"]         { channel="somneo:hf367x:1:sunset#duration" }
Number      PhilipsSomneo_SunsetColor     "Sunset Color"    <Sunset>      ["Control", "ColorTemperature"] { channel="somneo:hf367x:1:sunset#colorSchema" }
String      PhilipsSomneo_SunsetNoise     "Ambient Noise"   <Noise>       ["Control", "Noise"]            { channel="somneo:hf367x:1:sunset#ambientNoise" }
Dimmer      PhilipsSomneo_SunsetVolume    "Volume"          <SoundVolume> ["Control", "SoundVolume"]      { channel="somneo:hf367x:1:sunset#volume" }
// Relax
Switch      PhilipsSomneo_RelaxSwitch        "Relax Program"   <Light>       ["Switch", "Power"]        { channel="somneo:hf367x:1:relax#switch" }
Number:Time PhilipsSomneo_RelaxRemaining     "Remaining Time"  <Time>        ["Status", "Duration"]     { channel="somneo:hf367x:1:relax#remainingTime" }
Number      PhilipsSomneo_RelaxBreathingRate "Breathing Rate"                ["Control"]                { channel="somneo:hf367x:1:relax#breathingRate" }
Number:Time PhilipsSomneo_RelaxDuration      "Duration"        <Time>        ["Control", "Duration"]    { channel="somneo:hf367x:1:relax#duration" }
Number      PhilipsSomneo_RelaxGuidanceType  "Guidance Type"                 ["Control"]                { channel="somneo:hf367x:1:relax#guidanceType" }
Dimmer      PhilipsSomneo_RelaxIntensity     "Light Intensity" <Light>       ["Control", "Light"]       { channel="somneo:hf367x:1:relax#lightIntensity" }
Dimmer      PhilipsSomneo_RelaxVolume        "Volume"          <SoundVolume> ["Control", "SoundVolume"] { channel="somneo:hf367x:1:relax#volume" }
// Audio
Player PhilipsSomneo_AudioRadio     "Radio Control" <MediaControl> ["Control"]                { channel="somneo:hf367x:1:audio#radio" }
Switch PhilipsSomneo_AudioAux       "AUX-Input"                    ["Switch", "Power"]        { channel="somneo:hf367x:1:audio#aux" }
Dimmer PhilipsSomneo_AudioVolume    "Volume"        <SoundVolume>  ["Control", "SoundVolume"] { channel="somneo:hf367x:1:audio#volume" }
String PhilipsSomneo_AudioPreset    "FM Preset"                    ["Control"]                { channel="somneo:hf367x:1:audio#preset" }
String PhilipsSomneo_AudioFrequency "FM Frequency"                 ["Status"]                 { channel="somneo:hf367x:1:audio#frequency" }
```

somneo.sitemap:

```
sitemap somneo label="Philips Somneo" {
    Frame label="Sensors" {
        Default item=PhilipsSomneo_Illuminance
        Default item=PhilipsSomneo_Temperature
        Default item=PhilipsSomneo_Humidity
        Default item=PhilipsSomneo_Noise
    }
    Frame label="Lights" {
        Default item=PhilipsSomneo_MainLight
        Default item=PhilipsSomneo_MainLight visibility=[PhilipsSomneo_MainLight>0]
        Default item=PhilipsSomneo_NightLite
    }
    Frame label="Programs" {
        Default item=PhilipsSomneo_SunsetSwitch
        Default item=PhilipsSomneo_SunsetRemaining visibility=[PhilipsSomneo_SunsetSwitch==ON]
        Text label="Sunset Settings" icon="settings" {
            Default item=PhilipsSomneo_SunsetIntensity
            Default item=PhilipsSomneo_SunsetDuration
            Selection item=PhilipsSomneo_SunsetColor
            Default item=PhilipsSomneo_SunsetNoise
            Default item=PhilipsSomneo_SunsetVolume
        }
        Default item=PhilipsSomneo_RelaxSwitch
        Default item=PhilipsSomneo_RelaxRemaining visibility=[PhilipsSomneo_RelaxSwitch==ON]
        Text label="Relax Settings" icon="settings" {
            Default item=PhilipsSomneo_RelaxBreathingRate
            Selection item=PhilipsSomneo_RelaxDuration
            Switch item=PhilipsSomneo_RelaxGuidanceType mappings=[0="Light", 1="Sound"]
            Default item=PhilipsSomneo_RelaxIntensity
            Default item=PhilipsSomneo_RelaxVolume
        }
    }
    Frame label="Audio" {
        Default item=PhilipsSomneo_AudioRadio
        Default item=PhilipsSomneo_AudioAux
        Default item=PhilipsSomneo_AudioVolume visibility=[PhilipsSomneo_AudioRadio==PLAY, PhilipsSomneo_AudioAux==ON]
        Default item=PhilipsSomneo_AudioPreset visibility=[PhilipsSomneo_AudioRadio==PLAY]
        Default item=PhilipsSomneo_AudioFrequency visibility=[PhilipsSomneo_AudioRadio==PLAY]
    }
}
```

## Acknowledgements

Thanks to:

* [homebridge-somneo](https://github.com/zackwag/homebridge-somneo) - For creating a similar plugin in another platform and exposing endpoints for control.
* [somneo-client](https://github.com/DonkerNet/somneo-client) - For creating a similar plugin in another platform and exposing endpoints for control.
* HTTP Binding and other OpenHAB addons - Which was used as examples.
