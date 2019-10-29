# Belkin Wemo Binding

This binding integrates the [Belkin WeMo Family](https://www.belkin.com/us/Products/c/home-automation/).
The integration happens either through the WeMo-Link bridge, which acts as an IP gateway to the ZigBee devices or through WiFi connection to standalone devices.

## Supported Things

The WeMo Binding supports the Socket, Insight, Lightswitch, Motion, Dimmer, Coffemaker and Maker devices, as well as the WeMo-Link bridge with WeMo LED bulbs.

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

| Channel Type ID     | Item Type | Description                                                                                                                 | Available on thing
|---------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------|
| motionDetection     | Switch    | On if motion is detected, off otherwise. (Motion Sensor only)                                                               | Motion
| lastMotionDetected  | DateTime  | Date and Time when the last motion was detected. (Motion Sensor only)                                                       | Motion
| state               | Switch    | This channel controls the actual binary State of a Device or represents Motion Detection.                                   | All
| lastChangedAt       | DateTime  | Date and Time the device was last turned on or of.                                                                          | Insight
| lastOnFor           | Number    | Time in seconds an Insight device was last turned on for.                                                                   | Insight
| onToday             | Number    | Time in seconds an Insight device has been switched on today.                                                               | Insight
| onTotal             | Number    | Time in seconds an Insight device has been switched on totally.                                                             | Insight
| timespan            | Number    | Time in seconds over which onTotal applies. Typically 2 weeks except first used.                                            | Insight
| averagePower        | Number    | Average power consumption in Watts.                                                                                         | Insight
| currentPower        | Number    | Current power consumption of an Insight device. 0 if switched off.                                                          | Insight
| energyToday         | Number    | Energy in Wh used today.                                                                                                    | Insight
| energyTotal         | Number    | Energy in Wh used in total.                                                                                                 | Insight
| standbyLimit        | Number    | Minimum energy draw in W to register device as switched on (default 8W, configurable via WeMo App).                         | Insight
| onStandBy           | Switch    | Read-only indication of whether  or not the device plugged in to the insight switch is drawing more than the standby limit. | Insight
| relay               | Switch    | Switches the integrated relay contact close/open                                                                            | Maker
| sensor              | Switch    | Shows the state of the integrated sensor                                                                                    | Maker
| coffeeMode          | String    | Operation mode of a WeMo Coffee Maker                                                                                       | CoffeeMaker
| modeTime            | Number    | Current amount of time, in minutes, that the Coffee Maker has been in the current mode                                      | CoffeeMaker
| timeRemaining       | Number    | Remaining brewing time of a WeMo Coffee Maker                                                                               | CoffeeMaker
| waterLevelReached   | Switch    | Indicates if the WeMo Coffee Maker needs to be refilled                                                                     | CoffeeMaker
| cleanAdvise         | Switch    | Indicates if a WeMo Coffee Maker needs to be cleaned                                                                        | CoffeeMaker
| filterAdvise        | Switch    | Indicates if a WeMo Coffee Maker needs to have the filter changed                                                           | CoffeeMaker
| brewed              | DateTime  | Date/time the coffee maker last completed brewing coffee                                                                    | CoffeeMaker
| lastCleaned         | DateTime  | Date/time the coffee maker last completed cleaning                                                                          | CoffeeMaker
| brightness          | Number    | Brightness of a WeMo LED.                                                                                                   | LED
| faderCountDownTime  | Number    | Dimmer fading duration time in minutes                                                                                      | Dimmer
| faderEnabled        | Switch    | Switch the fader ON/OFF                                                                                                     | Dimmer
| timerStart          | Switch    | Switch the fading timer ON/OFF                                                                                              | Dimmer
| nightMode           | Switch    | Switch the nightMode ON/OFF                                                                                                 | Dimmer
| startTime           | DateTime  | Time when the nightMode starts                                                                                              | Dimmer
| endTime             | DateTime  | Time when the nightMode ends                                                                                                | Dimmer
| nightModeBrightness | Number    | Brightness used in nightMode                                                                                                | Dimmer


## Full Example

demo.things:

```
wemo:socket:Switch1 [udn="Socket-1_0-221242K11xxxxx"]
wemo:motion:Sensor1 [udn="Sensor-1_0-221337L11xxxxx"]
Bridge wemo:bridge:Bridge-1_0-231445B010xxxx [udn="Bridge-1_0-231445B010xxxx"] {
MZ100 94103EA2B278xxxx [ deviceID="94103EA2B278xxxx" ]
MZ100 94103EA2B278xxxx [ deviceID="94103EA2B278xxxx" ]
}
```

demo.items:

```
Switch DemoSwitch    { channel="wemo:socket:Switch1:state" }
Switch LightSwitch   { channel="wemo:lightswitch:Lightswitch1:state" }
Switch MotionSensor  { channel="wemo:Motion:Sensor1:motionDetection" }
Switch MotionDetected  { channel="wemo:Motion:Sensor1:lastMotionDetected" }
Number InsightPower  { channel="wemo:insight:Insight1:currentPower" }
Number InsightLastOn { channel="wemo:insight:Insight1:lastOnFor" }
Number InsightToday  { channel="wemo:insight:Insight1:onToday" }
Number InsightTotal  { channel="wemo:insight:Insight1:onTotal" }
Switch LED1 { channel="wemo:MZ100:Bridge-1_0-231445B010xxxx:94103EA2B278xxxx:state" }
Dimmer dLED1 { channel="wemo:MZ100:Bridge-1_0-231445B010xxxx:94103EA2B278xxxx:brightness" }
Switch LED2 { channel="wemo:MZ100:Bridge-1_0-231445B010xxxx:94103EA2B278xxxx:state" }
Dimmer dLED2 { channel="wemo:MZ100:Bridge-1_0-231445B010xxxx:94103EA2B278xxxx:brightness" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
		Frame label="WeMo" {
			Switch item=DemoSwitch
			Switch item=LightSwitch
			Switch item=MotionSensor
			Number item=InsightPower
			Number item=InsightLastOn
			Number item=InsightToday
			Number item=InsightTotal
			Switch item=LED1
			Slider item=dLED1
			Switch item=LED2
			Slider item=dLED2
		}
}
```
