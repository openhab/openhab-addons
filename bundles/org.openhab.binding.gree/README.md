# GREE Binding

This binding allows you too add GREE Air Conditioners as things. Once added as a thing, the user can control the Air Conditioner, similarly to how the Air Conditioner is controlled using the remote control or smartphone app.

Note : The GREE Air Conditioner must already be setup on the wifi network and must have a fixed IP Address.

## Supported Things

This binding supports one Thing type `airconditioner`.

## Discovery

Discovery is possible but as yet is not supported in this binding.

## Binding Configuration

No binding configuration required.

#### Manual Thing Creation

Fans can be manually created in the *PaperUI* or *HABmin*, or by placing a *.things* file in the *conf/things* directory.  See example below.


## Thing Configuration

The Air Conditioners IP address and refresh rate (in seconds) and network broadcast address are set at time of discovery.  However, in the event that any of this information changes, the Air Conditioners configuration must be updated.

## Channels

The following channels are supported for fans:

| Channel Name  | Item Type | Description                                                                                       |
|---------------|-----------|---------------------------------------------------------------------------------------------------|
| power         | Switch    | Power on/off the Air Conditioner                                                                  |
| mode          | Number    | Sets the operating mode of the Air Conditioner                                                    |
|               |           | Mode: Auto: 0, Cool: 1, Dry: 2, Fan: 3, Heat: 4                                                   |
|               |           | For more details see the Air Conditioner's operating manual.                                      |
| turbo         | Switch    | Set on/off the Air Conditioner's Turbo mode.                                                      |
|               |           | For more details see the Air Conditioner's operating manual.                                      |
| light         | Switch    | Enable/disable the front display on the Air Conditioner if applicable to the Air Conditioner model|
| temperature   | Number    | Sets the desired room temperature                                                                 |
| swingvertical | Number    | Sets the vertical swing action on the Air Conditioner                                             |
|               |           | Full Swing: 1, Up: 2, MidUp: 3, Mid: 4, Mid Down: 5, Down : 6                                     |
| windspeed     | Number    | Sets the fan speed on the Air conditioner Auto:0, Low:1, MidLow:2, Mid:3, MidHigh:4, High:5       |
|               |           | The number of speeds depends on the Air Conditioner model.                                        |
| air           | Switch    | Set on/off the Air Conditioner's Air function if applicable to the Air Conditioner model          |
| dry           | Switch    | Set on/off the Air Conditioner's Dry function if applicable to the Air Conditioner model          |
| health        | Switch    | Set on/off the Air Conditioner's Health function if applicable to the Air Conditioner model       |
| powersave     | Switch    | Set on/off the Air Conditioner's Power Saving function if applicable to the Air Conditioner model |


## Full Example

Things:

```
Thing gree:airconditioner:b2d08bb1 [ ipAddress="192.168.12.62", refresh=2 ]
```

Items:

```
Switch AirconPower                  { channel="gree:airconditioner:b2d08bb1:power" }
Number AirconMode                   { channel="gree:airconditioner:b2d08bb1:modec" }
Switch AirconTurbo                  { channel="gree:airconditioner:b2d08bb1:turbo" }
Switch AirconLight                  { channel="gree:airconditioner:b2d08bb1:light" }
Number AirconTemp "Temperature [%.1f °C]" {channel="gree:airconditioner:b2d08bb1:temperature" }
Number AirconTempSet                { channel="gree:airconditioner:b2d08bb1:temperature" }
Number AirconSwingVertical          { channel="gree:airconditioner:b2d08bb1:swingvertical" }
Number AirconFanSpeed               { channel="gree:airconditioner:b2d08bb1:windspeed" }
Switch AirconAir                    { channel="gree:airconditioner:b2d08bb1:air" }
Switch AirconDry                    { channel="gree:airconditioner:b2d08bb1:dry" }
Switch AirconHealth                 { channel="gree:airconditioner:b2d08bb1:health" }
Switch AirconPowerSaving            { channel="gree:airconditioner:b2d08bb1:powersave" }
```

Sitemap:

This is an example of how to set up your sitemap.

```
Frame label="Controls"
{
   Switch item=AirconPower label="Power" icon=switch
   Switch item=AirconMode label="Mode" mappings=[0="Auto", 1="Cool", 2="Dry", 3="Fan", 4="Heat"]
   Setpoint item=AirconTemp label="Set temperature" icon=temperature minValue=16 maxValue=30 step=1
}
Frame label="Fan Speed"
{
   Switch item=AirconFanSpeed label="Fan Speed" mappings=[0="Auto", 1="Low", 2="Medium Low", 3="Medium", 4="Medium High", 5="High"] icon=fan
}
Frame label="Fan-Swing Direction"
{
   Switch item=AirconSwingVertical label="Direction" mappings=[0="Off", 1="Full", 2="Up", 3="Mid-up", 4="Mid", 5="Mid-low", 6="Down"] icon=flow
}
Frame label="Options"
{
   Switch item=AirconTurbo label="Turbo" icon=fan
   Switch item=AirconLight label="Light" icon=light
   Switch item=AirconAir label="Air" icon=flow
   Switch item=AirconDry label="Dry" icon=rain
   Switch item=AirconHealth label="Health" icon=smiley
   Switch item=AirconPowerSaving label="Power Saving" icon=poweroutlet
}
```
