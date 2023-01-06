# Orbit B-hyve Binding

This is the binding for the [Orbit B-hyve](https://bhyve.orbitonline.com/) wi-fi sprinklers.

## Supported Things

This binding should support all the sprinklers which can be controlled by the Orbit B-hyve mobile application.
So far only the [Orbit B-hyve 8-zone Indoor Timer](https://bhyve.orbitonline.com/indoor-timer/) has been confirmed working. (Hardware version WT24-0001)

## Discovery

This binding supports the auto discovery of the sprinklers bound to your Orbit B-hyve account.  
To start the discovery you need to create a bridge thing and enter valid credentials to your Orbit B-hyve cloud account.

## Thing Configuration

The bridge thing requires a manual configuration. You have to enter valid credentials to your Orbit B-hyve account, and you can also set the refresh time in seconds for polling data from the Orbit cloud.  
There is no user configuration related to sprinkler things. Sprinklers do need a configuration property _id_ identifying the device, but the only way how to retrieve it is to let the bridge to auto discover sprinklers.

## Channels

This binding automatically detects all zones and programs for each sprinkler and creates these dynamic channels:

| channel          | type   | description                                                      |
|------------------|--------|------------------------------------------------------------------|
| zone_%           | Switch | This channel controls the manual zone watering (ON/OFF)          |
| program_%        | Switch | This channel controls the manual program watering (ON/OFF)       |
| enable_program_% | Switch | This channel controls the automatic program scheduling (ON/OFF)  |

Beside the dynamic channels each sprinkler thing provides these standard channels:

| channel        | type        | description                                                        |
|----------------|-------------|--------------------------------------------------------------------|
| mode           | String      | This channel represents the mode of sprinkler device (auto/manual) |
| next_start     | DateTime    | This channel represents the start time of the next watering        |
| rain_delay     | Number:Time | This channel manages the current rain delay in hours               |
| watering_time  | Number:Time | This channel manages the manual zone watering time in minutes      |
| control        | Switch      | This channel controls the sprinkler (ON/OFF)                       |
| smart_watering | Switch      | This channel controls the smart watering (ON/OFF)                  |

## Full Example

### Things Example

```java
Bridge orbitbhyve:bridge:mybridge "Orbit Bridge" [ email="your@ema.il", password="yourPass", refresh=30 ] {  
  Thing sprinkler indoor_timer "Sprinkler" [ id="4cab55704e0d7ddf98c1cc37" ]  
}
```

### Items Example

```java
Switch IrrigationControl "Irrigation active" <bhyve>  (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:control" }  
Switch IrrigationSmartWatering "Smart watering" <bhyve> (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:smart_watering" }  
Switch Irrigation1 "Zone 1" <water> (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:zone_1" }  
Switch Irrigation2 "Zone 2" <water> (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:zone_2" }  
Switch Irrigation3 "Zone 3" <water> (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:zone_3" }  
Switch Irrigation4 "Zone 4" <water> (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:zone_4" }  
Switch IrrigationP1 "Run program A" <program> (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:program_a" }  
Switch IrrigationP1Enable "Schedule program A" <program>  (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:enable_program_a" }  
String IrrigationMode "Irrigation mode [%s]" <water>  (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:mode" }  
Number IrrigationTime "Irrigation time [%d min]" <clock>  (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:watering_time" }  
Number IrrigationRainDelay "Rain delay [%d h]" <hourglass>  (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:rain_delay" }  
DateTime IrrigationNextStart "Next start A [%1$td.%1$tm.%1$tY %1$tR]" <clock>  (Out_Irrigation) { channel="orbitbhyve:sprinkler:mybridge:indoor_timer:next_start" }  
```

### Sitemap Example

```perl
Switch item=IrrigationControl  
Switch item=IrrigationSmartWatering  
Switch item=Irrigation1  
Switch item=Irrigation2  
Switch item=Irrigation3  
Switch item=Irrigation4  
Setpoint item=IrrigationTime minValue=1 maxValue=240 step=1  
Switch item=IrrigationP1  
Switch item=IrrigationP1Enable  
Text item=IrrigationMode  
Text item=IrrigationRainDelay  
Switch item=IrrigationRainDelay mappings=[0="OFF", 24="24", 48="48", 72="72"]  
Text item=IrrigationNextStart visibility=[IrrigationP1Enable==ON]  
```
