# Belkin Wemo Binding

This binding integrates the [Belkin WeMo Family](https://www.belkin.com/us/Products/c/home-automation/).
The integration happens either through the WeMo-Link bridge, which acts as an IP gateway to the ZigBee devices or through WiFi connection to standalone devices.

## Supported Things

The WeMo Binding supports the Socket, Insight, Lightswitch, Motion, Dimmer, Coffemaker and Maker devices, as well as the WeMo-Link bridge with WeMo LED bulbs. 
The Binding also supports the Crock-Pot Smart Slow Cooker, Mr. Coffee Smart Coffemaker as well as the Holmes Smart Air Purifier, Holmes Smart Humidifier and Holmes Smart Heater.

## Discovery

The WeMo devices are discovered through UPnP discovery service in the network. Devices will show up in the inbox and can be easily added as Things.

## Binding Configuration

The binding does not need any configuration.

## Thing Configuration

For manual Thing configuration, one needs to know the UUID of a certain WeMo device.
In the thing file, this looks e.g. like

```
wemo:socket:Switch1 [udn="Socket-1_0-221242K11xxxxx"]
```

For a WeMo Link bridge and paired LED Lights, please use the following Thing definition

```
Bridge wemo:bridge:Bridge-1_0-231445B01006A0 [udn="Bridge-1_0-231445B010xxxx"] {
MZ100 94103EA2B278xxxx [ deviceID="94103EA2B278xxxx" ]
MZ100 94103EA2B278xxxx [ deviceID="94103EA2B278xxxx" ]
}
```



## Channels

Devices support some of the following channels:

| Channel Type        | Item Type     | Description                                                                                                                | Available on Thing                                   | 
|---------------------|---------------|----------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| motionDetection     | Switch        | On if motion is detected, off otherwise. (Motion Sensor only)                                                              | Motion                                               |
| lastMotionDetected  | DateTime      | Date and Time when the last motion was detected. (Motion Sensor only)                                                      | Motion                                               |
| state               | Switch        | This channel controls the actual binary State of a Device or represents Motion Detection.                                  | All but Dimmer, Crockpot, Airpurifier and Humidifier |
| lastChangedAt       | DateTime      | Date and Time the device was last turned on or of.                                                                         | Insight                                              |
| lastOnFor           | Number        | Time in seconds an Insight device was last turned on for.                                                                  | Insight                                              |
| onToday             | Number        | Time in seconds an Insight device has been switched on today.                                                              | Insight                                              |
| onTotal             | Number        | Time in seconds an Insight device has been switched on totally.                                                            | Insight                                              |
| timespan            | Number        | Time in seconds over which onTotal applies. Typically 2 weeks except first used.                                           | Insight                                              |
| averagePower        | Number:Power  | Average power consumption in Watts.                                                                                        | Insight                                              |
| currentPower        | Number:Power  | Current power consumption of an Insight device. 0 if switched off.                                                         | Insight                                              |
| energyToday         | Number:Energy | Energy in Wh used today.                                                                                                   | Insight                                              |
| energyTotal         | Number:Energy | Energy in Wh used in total.                                                                                                | Insight                                              |
| standbyLimit        | Number:Power  | Minimum energy draw in W to register device as switched on (default 8W, configurable via WeMo App).                        | Insight                                              |
| onStandBy           | Switch        | Read-only indication of whether or not the device plugged in to the insight switch is drawing more than the standby limit. | Insight                                              |
| relay               | Switch        | Switches the integrated relay contact close/open                                                                           | Maker                                                |
| sensor              | Switch        | Shows the state of the integrated sensor                                                                                   | Maker                                                |
| coffeeMode          | String        | Operation mode of a WeMo Coffee Maker                                                                                      | CoffeeMaker                                          |
| modeTime            | Number        | Current amount of time, in minutes, that the Coffee Maker has been in the current mode                                     | CoffeeMaker                                          |
| timeRemaining       | Number        | Remaining brewing time of a WeMo Coffee Maker                                                                              | CoffeeMaker                                          |
| waterLevelReached   | Switch        | Indicates if the WeMo Coffee Maker needs to be refilled                                                                    | CoffeeMaker                                          |
| cleanAdvise         | Switch        | Indicates if a WeMo Coffee Maker needs to be cleaned                                                                       | CoffeeMaker                                          |
| filterAdvise        | Switch        | Indicates if a WeMo Coffee Maker needs to have the filter changed                                                          | CoffeeMaker                                          |
| brewed              | DateTime      | Date/time the coffee maker last completed brewing coffee                                                                   | CoffeeMaker                                          |
| lastCleaned         | DateTime      | Date/time the coffee maker last completed cleaning                                                                         | CoffeeMaker                                          |
| brightness          | Number        | Brightness of a WeMo LED od Dimmwer.                                                                                       | LED, DimmerSwitch                                    |
| faderCountDownTime  | Number        | Dimmer fading duration time in minutes                                                                                     | DimmerSwitch                                         |
| faderEnabled        | Switch        | Switch the fader ON/OFF                                                                                                    | DimmerSwitch                                         |
| timerStart          | Switch        | Switch the fading timer ON/OFF                                                                                             | DimmerSwitch                                         |
| nightMode           | Switch        | Switch the nightMode ON/OFF                                                                                                | DimmerSwitch                                         |
| startTime           | DateTime      | Time when the nightMode starts                                                                                             | DimmerSwitch                                         |
| endTime             | DateTime      | Time when the nightMode ends                                                                                               | DimmerSwitch                                         |
| nightModeBrightness | Number        | Brightness used in nightMode                                                                                               | DimmerSwitch                                         |
| cookMode            | String        | Shows the operation mode of a WeMo Crockpot (OFF, WARM, LOW, HIGH                                                          | Crockpot                                             |
| warmCookTime        | Number        | Shows the timer settings for warm cooking mode                                                                             | Crockpot                                             |
| lowCookTime         | Number        | Shows the timer settings for low cooking mode                                                                              | Crockpot                                             |
| highCookTime        | Number        | Shows the timer settings for high cooking mode                                                                             | Crockpot                                             |
| cookedTime          | Number        | Shows the elapsed cooking time                                                                                             | Crockpot                                             |
| purifierMode        | String        | Runmode of Air Purifier (OFF, LOW, MED, HIGH, AUTO)                                                                        | Air Purifier                                         |
| airQuality          | String        | Air quality (POOR, MODERATE, GOOD)                                                                                         | Air Purifier                                         |
| ionizer             | Switch        | Indicates whether the ionizer is switched ON or OFF                                                                        | Air Purifier                                         |
| filterLife          | Number        | Indicates the remaining filter lifetime in Percent                                                                         | Air Purifier, Humidifier                             |
| expiredFilterTime   | Number        | Indicates whether the filter lifetime has expired or not                                                                   | Air Purifier, Humidifier                             |
| filterPresent       | Switch        | Indicates the presence of an air filter                                                                                    | Air Purifier                                         |
| humidifierMode      | String        | Runmode of Humidifier (OFF, MIN, LOW, MED, HIGH, MAX)                                                                      | Humidifier                                           |
| desiredHumidity     | Number        | Shows desired humidity in Percent                                                                                          | Humidifier                                           |
| currentHumidity     | Number        | Shows current humidity in Percent                                                                                          | Humidifier                                           |
| heaterMode          | String        | Runmode of Heater (OFF, FROSTPROTECT, HIGH, LOW, ECO)                                                                      | Heater                                               |
| currentTemp         | Number        | Shows current temperature                                                                                                  | Heater                                               |
| targetTemp          | Number        | Shows target temperature                                                                                                   | Heater                                               |
| autoOffTime         | DateTime      | Time when the heater switches off                                                                                          | Heater                                               |
| heatingRemaining    | Number        | Shows the remaining heating time                                                                                           | Heater                                               |


## Full Example

demo.things:

```
wemo:socket:Switch1     "DemoSwitch"   @ "Office"   [udn="Socket-1_0-221242K11xxxxx"]
wemo:motion:Sensor1     "MotionSensor" @ "Entrance" [udn="Sensor-1_0-221337L11xxxxx"]

Bridge wemo:bridge:Bridge-1_0-231445B010xxxx [udn="Bridge-1_0-231445B010xxxx"] {
MZ100 94103EA2B278xxxx  "DemoLight1"   @ "Living"   [ deviceID="94103EA2B278xxxx" ]
MZ100 94103EA2B278xxxx  "DemoLoght2"   @ "Living"   [ deviceID="94103EA2B278xxxx" ]
}
```

demo.items:

```
// Switch
Switch DemoSwitch            { channel="wemo:socket:Switch1:state" }

// Lightswitch
Switch LightSwitch           { channel="wemo:lightswitch:Lightswitch1:state" }

// Motion
Switch MotionSensor          { channel="wemo:Motion:Sensor1:motionDetection" }
DateTime MotionDetected      { channel="wemo:Motion:Sensor1:lastMotionDetected" }

// Insight
Switch InsightSwitch         { channel="wemo:insight:Insight-1_0-xxxxxxxxxxxxxx:state" }
Number:Power InsightPower    { channel="wemo:insight:Insight-1_0-xxxxxxxxxxxxxx:currentPower" }
Number InsightLastOn         { channel="wemo:insight:Insight-1_0-xxxxxxxxxxxxxx:lastOnFor" }
Number InsightToday          { channel="wemo:insight:Insight-1_0-xxxxxxxxxxxxxx:onToday" }
Number InsightTotal          { channel="wemo:insight:Insight-1_0-xxxxxxxxxxxxxx:onTotal" }

// LED Bulbs
Switch LED1                  { channel="wemo:MZ100:Bridge-1_0-231445B010xxxx:94103EA2B278xxxx:brightness" }
Dimmer dLED1                 { channel="wemo:MZ100:Bridge-1_0-231445B010xxxx:94103EA2B278xxxx:brightness" }
Switch LED2                  { channel="wemo:MZ100:Bridge-1_0-231445B010xxxx:94103EA2B278xxxx:brightness" }
Dimmer dLED2                 { channel="wemo:MZ100:Bridge-1_0-231445B010xxxx:94103EA2B278xxxx:brightness" }

// DimmerSwitch
Switch DimmerSwitch          { channel="wemo:dimmer:Dimmer-1_0-231445B010xxxx:brightness" }
Dimmer dDimmerSwitch         { channel="wemo:dimmer:Dimmer-1_0-231445B010xxxx:brightness" }
Number DimmerSwitchFaderTime { channel="wemo:dimmer:Dimmer-1_0-231445B010xxxx:faderCountDownTime" }
Switch DimmerSwitchFaderOn   { channel="wemo:dimmer:Dimmer-1_0-231445B010xxxx:faderEnabled" }
Switch DimmerSwitchTimer     { channel="wemo:dimmer:Dimmer-1_0-231445B010xxxx:timerStart" }
Switch DimmerNightMode       { channel="wemo:dimmer:Dimmer-1_0-231445B010xxxx:nightMode" }
Dimmer NightModeBrightness   { channel="wemo:dimmer:Dimmer-1_0-231445B010xxxx:nightModeBrightness" }
DateTime NightModeStart      { channel="wemo:dimmer:Dimmer-1_0-231445B010xxxx:startTime" }
DateTime NightModeEnd        { channel="wemo:dimmer:Dimmer-1_0-231445B010xxxx:endTime" }

// CoffeMaker
Switch CoffeSwitch          { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:state" }
String CoffeMode            { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:coffeeMode" }
Number CoffeModeTime        { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:modeTime" }
Number CoffeModeRemaining   { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:timeRemaining" }
Switch CoffeWater           { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:waterLevelReached" }
Switch CoffeCleanAdvicse    { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:cleanAdvise" }
Switch CoffeFilterAdvicse   { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:filterAdvise" }
DateTime CoffeLastCleaned   { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:lastCleaned" }

// Crockpot
String crockpotMode         { channel="wemo:crockpot:Crockpot-1_0-231445B010xxxx:cookMode" }
Number warmCookTime         { channel="wemo:crockpot:Crockpot-1_0-231445B010xxxx:warmCookTime" }
Number lowCookTime          { channel="wemo:crockpot:Crockpot-1_0-231445B010xxxx:lowCookTime" }
Number highCookTime         { channel="wemo:crockpot:Crockpot-1_0-231445B010xxxx:highCookTime" }
Number cookedTime           { channel="wemo:crockpot:Crockpot-1_0-231445B010xxxx:cookedTime" }

// Air Purifier
String airMode              { channel="wemo:purifier:AirPurifier-1_0-231445B010xxxx:airMode" }
String airQuality           { channel="wemo:purifier:AirPurifier-1_0-231445B010xxxx:airQuality" }
Switch ionizer              { channel="wemo:purifier:AirPurifier-1_0-231445B010xxxx:ionizer" }
Number filterLife           { channel="wemo:purifier:AirPurifier-1_0-231445B010xxxx:filterLife" }
Switch filterExpired        { channel="wemo:purifier:AirPurifier-1_0-231445B010xxxx:filterExpired" }
Switch filterPresent        { channel="wemo:purifier:AirPurifier-1_0-231445B010xxxx:filterPresent" }

// Humidifier
String humidifierMode       { channel="wemo:humidifier:Humidifier-1_0-231445B010xxxx:humidifierMode" }
Number desiredHumidity      { channel="wemo:humidifier:Humidifier-1_0-231445B010xxxx:desiredHumidity" }
Number currentHumidity      { channel="wemo:humidifier:Humidifier-1_0-231445B010xxxx:currentHumidity" }
Number filterLife           { channel="wemo:humidifier:Humidifier-1_0-231445B010xxxx:filterLife" }
Switch filterExpired        { channel="wemo:humidifier:Humidifier-1_0-231445B010xxxx:filterExpired" }
String waterLevel           { channel="wemo:humidifier:Humidifier-1_0-231445B010xxxx:waterLevel" }

// Heater
String heaterMode           { channel="wemo:heater:HeaterB-1_0-231445B010xxxx:heaterMode" }
Number currentTemp          { channel="wemo:heater:HeaterB-1_0-231445B010xxxx:currentTemp" }
Number targetTemp           { channel="wemo:heater:HeaterB-1_0-231445B010xxxx:targetTemp" }
DateTime autoOffTime        { channel="wemo:heater:HeaterB-1_0-231445B010xxxx:autoOffTime" }
String heaterRemaining      { channel="wemo:heater:HeaterB-1_0-231445B010xxxx:heaterRemaining" }

```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame {
       // Switch
       Switch item=DemoSwitch
       
       // LightSwitch
       Switch item=LightSwitch
       
       // Motion
       Switch item=MotionSensor
       Text item=MotionSensorLastChanged icon="clock"
       
       // Insight
       Switch item=InsightSwitch
       Number item=InsightPower
       Number item=InsightLastOn
       Number item=InsightToday
       Number item=InsightTotal
       
       // LED Bulb
       Switch item=LED1
       Slider item=dLED1
       Switch item=LED2
       Slider item=dLED2
       
       //DimmerSwitch
       Switch item=DimmerSwitch
       Slider item=dDimmerSwitch
       Number item=DimmerSwitchFaderTime
       Switch item=DimmerSwitchFaderOn
       Switch item=DimmerSwitchTimer
       Switch item=DimmerNightMode
       Slider item=NightModeBrightness
       Text item=NightModeStart
       Text item=NightModeEnd
       
       // CoffeMaker
       Switch item=CoffeSwitch
       Text item=CoffeMode
       Number item=CoffeModeTime
       Number item=CoffeModeRemaining
       Switch item=CoffeWater
       Switch item=CoffeCleanAdvicse
       Switch item=CoffeFilterAdvicse
       DateTime item=CoffeLastCleaned

       // CrockPot
       Switch item=crockpotMode label="Cooking Mode" mappings=[OFF="OFF", WARM="Warm", LOW="Low", HIGH="High"]
       Number item=warmCookTime
       Number item=lowCookTime
       Number item=highCookTime
       Number item=cookedTime

       // Air Purifier
       Switch item=airMode label="Cooking Mode" mappings=[OFF="OFF", LOW="Low", MED="Med", HIGH="High", AUTO="Auto"]
       Text item=airQuality
       Switch item=ionizer
       Number item=filterLive
       Switch item=filterExpired
       Switch item=filterPresent

       // Humidifier
       Switch item=humidifierMode label="Humidity Mode" mappings=[OFF="OFF", MIN="Min", LOW="Low", MED="Med", HIGH="High", MAX="Max"]
       Number item=desiredHumidity
       Number item=currentHumidity
       Number item=filterLive
       Switch item=filterExpired

       // Heater
       Switch item=heaterMode label="Heater Mode" mappings=[OFF="OFF", FROSTPROTECT="FrostProtect", HIGH="High", LOW="Low", ECO="Eco"]
       Number item=currentTemp
       Setpoint item=targetTemp
       Text item=autoOffTime
       Number item=heaterRemaining


    }
}
```
