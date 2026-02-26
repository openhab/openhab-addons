# Belkin WeMo Binding

This binding integrates the [Belkin WeMo Family](https://www.belkin.com/us/Products/c/home-automation/).
Integration happens either through the WeMo Link bridge, which acts as an IP gateway to Zigbee devices, or through Wi-Fi connection to standalone devices.

## Supported Things

The WeMo binding supports the Socket, Outdoor Plug, Insight, Light Switch, Motion, Dimmer, Coffee Maker and Maker devices, as well as the WeMo Link bridge with WeMo LED bulbs.
The binding also supports the Crock-Pot Smart Slow Cooker, Mr. Coffee Smart Coffee Maker, Holmes Smart Air Purifier, Holmes Smart Humidifier, and Holmes Smart Heater.

## Discovery

WeMo devices are discovered through UPnP discovery service in the network.
Devices will appear in the Inbox and can be easily added as Things.

## Binding Configuration

The binding does not need any configuration.

## Thing Configuration

For manual Thing configuration, you need to know the UDN of a WeMo device.
It can be obtained most easily by performing auto-discovery before configuring the Thing manually.

Most devices share the `udn` configuration parameter:

| Configuration Parameter | Description                        |
|-------------------------|------------------------------------|
| udn                     | The UDN identifies the WeMo device |

### WeMo LED Light

For LED lights paired to a WeMo Link bridge, use the following configuration parameter:

| Configuration Parameter | Description                                     |
|-------------------------|-------------------------------------------------|
| deviceID                | The device ID identifies one certain WeMo light |

### WeMo Insight Switch

The WeMo Insight Switch has some additional parameters for controlling the behavior of the `currentPower` channel.
This channel reports the current power consumption in watts.
The internal theoretical accuracy is 5 mW (three decimals).
These raw values are reported with high frequency; often multiple updates occur within a single second.
For example, the sequence 40.440 W, 40.500 W and 40.485 W would result in the channel being updated with values rounded to the nearest integer: 40 W, 41 W and 40 W, respectively.

When persisting Items linked to this channel, this can result in a significant amount of data being stored.
To mitigate this issue, a sliding window with a moving average calculation has been introduced.
This window uses a one-minute default period.
This is combined with a delta trigger value, which defaults to 1 W.
This means the channel is updated only when one of the following conditions is met:

1. The rounded value received is equal to the rounded average for the past minute (i.e., this value has stabilized). This introduces a delay for very small changes in consumption, but it prevents excessive logging and persistence caused by temporary small changes and rounding.
1. The rounded value received is more than 1 W different from the previous value. When changes occur quickly, the channel is updated immediately.

| Configuration Parameter    | Description                                                                           |
|----------------------------|---------------------------------------------------------------------------------------|
| udn                        | The UDN identifies the WeMo Insight Switch                                            |
| currentPowerSlidingSeconds | Sliding window in seconds for which moving average power is calculated (0 = disabled) |
| currentPowerDeltaTrigger   | Delta triggering immediate channel update (in watts)                                  |

The moving average calculation can be disabled by setting either `currentPowerSlidingSeconds` or `currentPowerDeltaTrigger` to 0.
This causes the channel to be updated the same way as in openHAB versions prior to 3.3.

## Channels

Devices support some of the following channels:

| Channel Type        | Item Type     | Description                                                                                                                | Available on Thing                                    |
|---------------------|---------------|----------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| motionDetection     | Switch        | On if motion is detected, off otherwise (Motion Sensor only).                                                              | Motion                                                |
| lastMotionDetected  | DateTime      | Date and time when the last motion was detected (Motion Sensor only).                                                      | Motion                                                |
| state               | Switch        | This channel controls the actual binary state of a device or represents motion detection.                                  | All but Dimmer, Crockpot, Air Purifier and Humidifier |
| lastChangedAt       | DateTime      | Date and time the device was last turned on or off.                                                                        | Insight                                               |
| lastOnFor           | Number        | Time in seconds an Insight device was last turned on for.                                                                  | Insight                                               |
| onToday             | Number        | Time in seconds an Insight device has been switched on today.                                                              | Insight                                               |
| onTotal             | Number        | Time in seconds an Insight device has been switched on in total.                                                           | Insight                                               |
| timespan            | Number        | Time in seconds over which onTotal applies. Typically 2 weeks, except when first used.                                     | Insight                                               |
| averagePower        | Number:Power  | Average power consumption in watts.                                                                                        | Insight                                               |
| currentPower        | Number:Power  | Current power consumption of an Insight device (0 if switched off).                                                        | Insight                                               |
| currentPowerRaw     | Number:Power  | Current power consumption of an Insight device with full precision (5 mW accuracy, three decimals; 0 if switched off).     | Insight                                               |
| energyToday         | Number:Energy | Energy in watt-hours used today.                                                                                           | Insight                                               |
| energyTotal         | Number:Energy | Energy in watt-hours used in total.                                                                                        | Insight                                               |
| standbyLimit        | Number:Power  | Minimum energy draw in watts to register device as switched on (default 8 W, configurable via WeMo app).                   | Insight                                               |
| onStandBy           | Switch        | Read-only indication of whether or not the device plugged in to the insight switch is drawing more than the standby limit. | Insight                                               |
| relay               | Switch        | Switches the integrated relay contact close/open.                                                                          | Maker                                                 |
| sensor              | Switch        | Shows the state of the integrated sensor.                                                                                  | Maker                                                 |
| coffeeMode          | String        | Operation mode of a WeMo Coffee Maker.                                                                                     | Coffee Maker                                          |
| modeTime            | Number        | Current amount of time, in minutes, that the Coffee Maker has been in the current mode.                                    | Coffee Maker                                          |
| timeRemaining       | Number        | Remaining brewing time of a WeMo Coffee Maker.                                                                             | Coffee Maker                                          |
| waterLevelReached   | Switch        | Indicates if the WeMo Coffee Maker needs to be refilled.                                                                   | Coffee Maker                                          |
| cleanAdvise         | Switch        | Indicates if a WeMo Coffee Maker needs to be cleaned.                                                                      | Coffee Maker                                          |
| filterAdvise        | Switch        | Indicates if a WeMo Coffee Maker needs the filter changed.                                                                 | Coffee Maker                                          |
| brewed              | DateTime      | Date/time the Coffee Maker last completed brewing coffee.                                                                  | Coffee Maker                                          |
| lastCleaned         | DateTime      | Date/time the Coffee Maker last completed cleaning.                                                                        | Coffee Maker                                          |
| brightness          | Number        | Brightness of a WeMo LED or Dimmer Switch.                                                                                 | LED, Dimmer Switch                                    |
| faderCountDownTime  | Number        | Dimmer fading duration time in minutes.                                                                                    | Dimmer Switch                                         |
| faderEnabled        | Switch        | Switch the fader ON/OFF.                                                                                                   | Dimmer Switch                                         |
| timerStart          | Switch        | Switch the fading timer ON/OFF.                                                                                            | Dimmer Switch                                         |
| nightMode           | Switch        | Switch the night mode ON/OFF.                                                                                              | Dimmer Switch                                         |
| startTime           | DateTime      | Time when the night mode starts.                                                                                           | Dimmer Switch                                         |
| endTime             | DateTime      | Time when the night mode ends.                                                                                             | Dimmer Switch                                         |
| nightModeBrightness | Number        | Brightness used in night mode.                                                                                             | Dimmer Switch                                         |
| cookMode            | String        | Shows the operation mode of a WeMo Crock-Pot (OFF, WARM, LOW, HIGH).                                                       | Crock-Pot                                             |
| warmCookTime        | Number        | Shows the timer settings for warm cooking mode.                                                                            | Crock-Pot                                             |
| lowCookTime         | Number        | Shows the timer settings for low cooking mode.                                                                             | Crock-Pot                                             |
| highCookTime        | Number        | Shows the timer settings for high cooking mode.                                                                            | Crock-Pot                                             |
| cookedTime          | Number        | Shows the elapsed cooking time.                                                                                            | Crock-Pot                                             |
| purifierMode        | String        | Run mode of air purifier (OFF, LOW, MED, HIGH, AUTO).                                                                      | Air Purifier                                          |
| airQuality          | String        | Air quality (POOR, MODERATE, GOOD).                                                                                        | Air Purifier                                          |
| ionizer             | Switch        | Indicates whether the ionizer is switched ON or OFF.                                                                       | Air Purifier                                          |
| filterLife          | Number        | Indicates the remaining filter lifetime in percent.                                                                        | Air Purifier, Humidifier                              |
| expiredFilterTime   | Number        | Indicates whether the filter lifetime has expired.                                                                         | Air Purifier, Humidifier                              |
| filterPresent       | Switch        | Indicates the presence of an air filter.                                                                                   | Air Purifier                                          |
| humidifierMode      | String        | Run mode of humidifier (OFF, MIN, LOW, MED, HIGH, MAX).                                                                    | Humidifier                                            |
| desiredHumidity     | Number        | Shows desired humidity in percent.                                                                                         | Humidifier                                            |
| currentHumidity     | Number        | Shows current humidity in percent.                                                                                         | Humidifier                                            |
| heaterMode          | String        | Run mode of heater (OFF, FROSTPROTECT, HIGH, LOW, ECO).                                                                    | Heater                                                |
| currentTemp         | Number        | Shows current temperature.                                                                                                 | Heater                                                |
| targetTemp          | Number        | Shows target temperature.                                                                                                  | Heater                                                |
| autoOffTime         | DateTime      | Time when the heater switches off.                                                                                         | Heater                                                |
| heatingRemaining    | Number        | Shows the remaining heating time.                                                                                          | Heater                                                |

## Full Example

demo.things:

```java
wemo:socket:Switch1     "DemoSwitch"   @ "Office"   [udn="Socket-1_0-221242K11xxxxx"]
wemo:motion:Sensor1     "MotionSensor" @ "Entrance" [udn="Sensor-1_0-221337L11xxxxx"]
wemo:insight:Insight1   "Insight"      @ "Attic"    [udn="Insight-1_0-xxxxxxxxxxxxxx", currentPowerSlidingSeconds=120, currentPowerDeltaTrigger=2]

Bridge wemo:bridge:Bridge-1_0-231445B010xxxx [udn="Bridge-1_0-231445B010xxxx"] {
MZ100 94103EA2B278xxxx  "DemoLight1"   @ "Living"   [ deviceID="94103EA2B278xxxx" ]
MZ100 94103EA2B278xxxx  "DemoLight2"   @ "Living"   [ deviceID="94103EA2B278xxxx" ]
}
```

demo.items:

```java
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

// Coffee Maker
Switch CoffeeSwitch          { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:state" }
String CoffeeMode            { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:coffeeMode" }
Number CoffeeModeTime        { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:modeTime" }
Number CoffeeModeRemaining   { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:timeRemaining" }
Switch CoffeeWater           { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:waterLevelReached" }
Switch CoffeeCleanAdvise     { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:cleanAdvise" }
Switch CoffeeFilterAdvise    { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:filterAdvise" }
DateTime CoffeeLastCleaned   { channel="wemo:coffee:Coffee-1_0-231445B010xxxx:lastCleaned" }

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

```perl
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

       // Coffee Maker
       Switch item=CoffeeSwitch
       Text item=CoffeeMode
       Number item=CoffeeModeTime
       Number item=CoffeeModeRemaining
       Switch item=CoffeeWater
       Switch item=CoffeeCleanAdvise
       Switch item=CoffeeFilterAdvise
       DateTime item=CoffeeLastCleaned

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
