# GREE Binding

This binding integrates Air Conditioners that use the GREE protocol (GREE, Sinclair and others).

Note: The Air Conditioner must already be set-up on the WiFi network and must have a fixed IP Address.     

## Supported Things

This binding supports one Thing type `airconditioner`.

## Discovery

Once the Air Conditioner is on the network (WiFi active) it could be discovered automatically.
An IP broadcast message is sent and every responding unit gets added to the Inbox. 

## Binding Configuration

No binding configuration is required.

## Thing Configuration

| Channel Name             | Type       | Description                                                                                   |
|--------------------------|------------|-----------------------------------------------------------------------------------------------|
| ipAddress                | IP Address | IP address of the unit.                                                                       |
| broadcastAddress         | IP Address | Broadcast address being used for discovery, usually derived from the IP interface address.    |
| refresh                  | Integer    | Refresh interval in seconds for polling the device status.                                    |
| currentTemperatureOffset | Decimal    | Offset in Celsius for the current temperature value received from the device.                 |

The Air Conditioner's IP address is mandatory, all other parameters are optional. 
If the broadcast is not set (default) it will be derived from openHAB's network setting (Check Network Settings in the openHAB UI).
Only change this if you have a good reason to.

## Channels

The following channels are supported for fans:

| Channel Name       | Item Type | Description                                                                                       |
|--------------------|-----------|---------------------------------------------------------------------------------------------------|
| power              | Switch    | Power on/off the Air Conditioner                                                                  |
| mode               | String    | Sets the operating mode of the Air Conditioner                                                    |
|                    |           | Mode can be one of auto/cool/eco/dry/fan/heat or on/off                                           |
|                    |           | Check the Air Conditioner's operating manual for supported modes.                                 |
| temperature        | Number:Temperature | Sets the desired room temperature.                                                       |
| currentTemperature | Number:Temperature | Displays the current room temperature (if supported by the unit, otherwise NaN).         |
| air                | Switch    | Set on/off the Air Conditioner's Air function if applicable to the Air Conditioner model          |
| dry                | Switch    | Set on/off the Air Conditioner's Dry function if applicable to the Air Conditioner model          |
| health             | Switch    | Set on/off the Air Conditioner's Health function if applicable to the Air Conditioner model       |
| turbo              | Switch    | Set on/off the Air Conditioner's Turbo Mode.                                                      |
| quiet              | String    | Set Quiet Mode: off/auto/quiet                                                                    |
| swingUpDown        | Number    | Sets the vertical (up..down) swing action on the Air Conditioner,                                 |
|                    |           | OFF: 0, Full Swing: 1, Up: 2, MidUp: 3, Mid: 4, Mid Down: 5, Down : 6                             |
| swingLeftRight     | Number    | Sets the horizontal (left..right) swing action on the Air Conditioner                             |
|                    |           | OFF: 0, Full Swing: 1, Left: 2, Mid Left: 3, Mid: 4, Mid Right: 5, Right : 6                      |
| windspeed          | Number    | Sets the fan speed on the Air conditioner Auto:0, Low:1, MidLow:2, Mid:3, MidHigh:4, High:5       |
|                    |           | The number of speeds depends on the Air Conditioner model.                                        |
| powersave          | Switch    | Set on/off the Air Conditioner's Power Saving function if applicable to the Air Conditioner model |
| light              | Switch    | Enable/disable the front display on the Air Conditioner if applicable to the Air Conditioner model|
|                    |           | Full Swing: 1, Up: 2, MidUp: 3, Mid: 4, Mid Down: 5, Down : 6                                     |


When changing mode, the air conditioner will be turned on unless "off" is selected.

## Full Example

**Things**

```
Thing gree:airconditioner:a1234561 [ ipAddress="192.168.1.111", refresh=2 ]
```

**Items**

```
Switch AirconPower                  { channel="gree:airconditioner:a1234561:power" }
String AirconMode                   { channel="gree:airconditioner:a1234561:mode" }
Switch AirconTurbo                  { channel="gree:airconditioner:a1234561:turbo" }
Switch AirconLight                  { channel="gree:airconditioner:a1234561:light" }
Number AirconTargetTemp "Target Temperature [%.1f °C]" {channel="gree:airconditioner:a1234561:temperature" }
Number AirconCurrentTemp "Current Temperature [%.1f °C]" {channel="gree:airconditioner:a1234561:currentTemperature" }
Number AirconSwingVertical          { channel="gree:airconditioner:a1234561:swingUpDown" }
Number AirconSwingHorizontal        { channel="gree:airconditioner:a1234561:swingLeftRight" }
Number AirconFanSpeed               { channel="gree:airconditioner:a1234561:windspeed" }
Switch AirconAir                    { channel="gree:airconditioner:a1234561:air" }
Switch AirconDry                    { channel="gree:airconditioner:a1234561:dry" }
Switch AirconHealth                 { channel="gree:airconditioner:a1234561:health" }
Switch AirconPowerSaving            { channel="gree:airconditioner:a1234561:powersave" }
```

**Sitemap**

This is an example of how to set up your sitemap.

```
Frame label="Controls"
{
   Switch item=AirconMode label="Mode" mappings=["auto"="Auto", "cool"="Cool", "eco"="Eco", "dry"="Dry", "fan"="Fan", "turbo"="Turbo", "heat"="Heat", "on"="ON", "off"="OFF"]
   Setpoint item=AirconTargetTemp label="Set target temperature" icon=temperature minValue=16 maxValue=30 step=1
}
Frame label="Current Temperature"
{
   Text item=AirconCurrentTemp label="Current temperature [%.1f °C]" icon="temperature"
}
Frame label="Fan Speed"
{
   Switch item=AirconFanSpeed label="Fan Speed" mappings=[0="Auto", 1="Low", 2="Medium Low", 3="Medium", 4="Medium High", 5="High"] icon=fan
}
Frame label="Fan-Swing Direction"
{
   Switch item=AirconSwingVertical label="Direction V" mappings=[0="Off", 1="Full", 2="Up", 3="Mid-up", 4="Mid", 5="Mid-low", 6="Down"] icon=flow
   Switch item=AirconSwingHorizontal label="Direction H" mappings=[0="Off", 1="Full", 2="Left", 3="Mid-left", 4="Mid", 5="Mid-right", 6="Right"] icon=flow
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

**Example**

This example shows how to make a GREE Air Conditioner controllable by Google HA (A/C mode + temperature)

**Items**

```
Group Gree_Modechannel              "Gree"                { ga="Thermostat" } // allows mapping for Google Home Assistent
Switch   GreeAirConditioner_Power   "Aircon"              {channel="gree:airconditioner:a1234561:power", ga="Switch"}
String   GreeAirConditioner_Mode    "Aircon Mode"         {channel="gree:airconditioner:a1234561:mode", ga="thermostatMode"}
Number   GreeAirConditioner_Temp    "Aircon Temperature"  {channel="gree:airconditioner:a1234561:temperature}
Switch   GreeAirConditioner_Light   "Light"               {channel="gree:airconditioner:a1234561:light"}
```

**Rules**

```
rule "Mode changed"
when
        Item GreeAirConditioner_Mode changed
then        
        if(GreeAirConditioner_Mode.state == "cool" ) {
            logInfo("A/C", "Cooling has be turned on")
        } 
end
```
