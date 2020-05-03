# GREE Binding

This binding integrates GREE Air Conditioners.

Note : The GREE Air Conditioner must already be setup on the WiFi network and must have a fixed IP Address.

## Supported Things

This binding supports one Thing type `airconditioner`.

## Discovery

Once the GREE is on the network (WiFi active) it could be discovery automatically.
An IP broadcast message is sent and every responding units gets added to the Inbox. 

## Binding Configuration

No binding configuration is required.

#### Manual Thing Creation

Fans can be manually created in the *PaperUI* or *HABmin*, or by placing a *.things* file in the *conf/things* directory.  See example below.


## Thing Configuration

The Air Conditioner's IP address is mandantory, all other parameters are optional. 
If the broadcast is not set (default) it will be derived from openHAB's network setting (PaperUI:Configuration:System:Network Settings). 
Change this only for good reasons.

## Channels

The following channels are supported for fans:

| Channel Name  | Item Type | Description                                                                                       |
|---------------|-----------|---------------------------------------------------------------------------------------------------|
| power         | Switch    | Power on/off the Air Conditioner                                                                  |
| mode          | String    | Sets the operating mode of the Air Conditioner                                                    |
|               |           | Mode can be one of auto/cool/eco/dry/fan/turboheat or on/off                                      |||               |           | You could also send "0".."4", which will be send transparent to the device:                       |
|               |           | those map to: "0"=Auto, "1"=Cool, "2"=Dry, "3"=Fan only, "4"=heat                                 | 
|               |           | Check the Air Conditioner's operating manual for supported modes.                                 |
| temperature   | Number    | Sets the desired room temperature                                                                 |
| swingvertical | Number    | Sets the vertical swing action on the Air Conditioner                                             |
| air           | Switch    | Set on/off the Air Conditioner's Air function if applicable to the Air Conditioner model          |
| dry           | Switch    | Set on/off the Air Conditioner's Dry function if applicable to the Air Conditioner model          |
| health        | Switch    | Set on/off the Air Conditioner's Health function if applicable to the Air Conditioner model       |
| powersave     | Switch    | Set on/off the Air Conditioner's Power Saving function if applicable to the Air Conditioner model |
| turbo         | Switch    | Set on/off the Air Conditioner's Turbo mode.                                                      |
| light         | Switch    | Enable/disable the front display on the Air Conditioner if applicable to the Air Conditioner model|
|               |           | Full Swing: 1, Up: 2, MidUp: 3, Mid: 4, Mid Down: 5, Down : 6                                     |
| windspeed     | Number    | Sets the fan speed on the Air conditioner Auto:0, Low:1, MidLow:2, Mid:3, MidHigh:4, High:5       |
|               |           | The number of speeds depends on the Air Conditioner model.                                        |


When changing the mode the air conditioner will be turned on (unless is off is selected).

## Full Example

### Generic

Things:

```
Thing gree:airconditioner:a1234561 [ ipAddress="192.168.1.111", refresh=2 ]
```

Items:

```
Switch AirconPower                  { channel="gree:airconditioner:a1234561:power" }
Number AirconMode                   { channel="gree:airconditioner:a1234561:mode" }
Switch AirconTurbo                  { channel="gree:airconditioner:a1234561:turbo" }
Switch AirconLight                  { channel="gree:airconditioner:a1234561:light" }
Number AirconTemp "Temperature [%.1f °C]" {channel="gree:airconditioner:a1234561:temperature" }
Number AirconTempSet                { channel="gree:airconditioner:a1234561:temperature" }
Number AirconSwingVertical          { channel="gree:airconditioner:a1234561:swingvertical" }
Number AirconFanSpeed               { channel="gree:airconditioner:a1234561:windspeed" }
Switch AirconAir                    { channel="gree:airconditioner:a1234561:air" }
Switch AirconDry                    { channel="gree:airconditioner:a1234561:dry" }
Switch AirconHealth                 { channel="gree:airconditioner:a1234561:health" }
Switch AirconPowerSaving            { channel="gree:airconditioner:a1234561:powersave" }
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

### Google Home Assistant

This example shows who to make the GREE Air Conditioner controllable by Google HA (A/C mode + temperature)

Items:

```
Group Gree_Modechannel                 "Gree"        { ga="Thermostat" } // új Gree bindinggal
// Gree Klíma
    Switch   GreeAirConditioner_Powerchannel           "Aircon"                                        {channel="greeair:greeairthing:a1234561:powerchannel", ga="Switch"}
    Number   GreeAirConditioner_Modechannel            "Aircon Mode"                                   {channel="greeair:greeairthing:a1234561:modechannel"}
    String   GreeAirConditioner_Modechannel_GA         "Aircon Mode" (gGreeAirConditioner_Modechannel) {ga="thermostatMode" }
    Switch   GreeAirConditioner_Turbochannel           "Turbo"                                         {channel="greeair:greeairthing:a1234561:turbochannel"}
    Switch   GreeAirConditioner_Lightchannel           "Light"                                         {channel="greeair:greeairthing:a1234561:lightchannel"}
    Number   GreeAirConditioner_Tempchannel            "Aircon Temperature"  (gGre
```

Rule:
```
rule "Translate Mode from GA 2"
when
        Item GreeMode_GA changed
then        
        if(GreeMode_GA.state == "off" ) {
            sendCommand(GreePower,OFF)
         }  else {
            if(GreePower.state == OFF) {
                sendCommand(GreePower,ON)
            }
            if(GreeMode_GA.state == "cool" ) {
                sendCommand(GreeMode,1)
             }
            if(GreeMode_GA.state == "dry" ) {
                sendCommand(GreeMode,2)
             }
            if(GreeMode_GA.state == "fan-only" ) {
                sendCommand(GreeMode,3) 
             }
            if(GreeMode_GA.state == "heat" ) {
                sendCommand(GreeMode,4)
             }
        }
end
```
