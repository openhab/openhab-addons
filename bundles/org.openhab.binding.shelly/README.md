# Shelly Binding

This Binding integrated Shelly devices.

## Supported Devices

|Thing               |Type                                                    |
|--------------------|--------------------------------------------------------|
| shelly1            | Shelly Single Relay Switch                             |
| shelly1pm          | Shelly Single Relay Switch with integrated Power Meter |
| shellyem           | Shelly EM with integrated Power Meter                  |
| shelly2-relay      | Shelly Double Relay Switch in relay mode               |
| shelly2-roller     | Shelly2 in Roller Mode                                 |
| shelly25-relay     | Shelly 2.5 in Relay Switch                             |
| shelly25-roller    | Shelly 2.5 in Roller Mode                              |
| shelly4pro         | Shelly 4x Relay Switch                                 |
| shellydimmer       | Shelly Dimmer                                          |
| shellyplugs        | Shelly Plug-S                                          |
| shellyplug         | Shelly Plug                                            |
| shellyrgbw2        | Shelly RGB Controller                                  |
| shellybulb         | Shelly Bulb in Color or White Mode                     |
| shellyht           | Shelly Sensor (temp+humidity)                          |
| shellyflood        | Shelly Flood Sensor                                    |
| shellysmoke        | Shelly Smoke Sensor                                    |
| shellysense        | Shelly Motion and IR Controller                        |
| shellydevice       | A password protected Shelly device or anunknown type   |

## Firmware

To utilize all features the binding requires firmware version 1.5.2 or newer.
This should be available for all devices.
Older versions work in general, but have impacts to functionality (e.g. no events for battery powered devices).

The binding displays a WARNING in the log if the firmware is older.
It also informs you when an update is available.
Use the device' web ui or the Shelly App to perform the update.

## Discovery

The binding uses mDNS to discover the Shelly devices.
They periodically announce their presence, which is used by the binding to find them on the local network.

Make sure to wake-up battery powered devices (press the button inside the device), so that they show up on the network.
Sometimes you need to run the manual discovery multiple times until you see all your devices.

Important: The IP address should not be changed after the device is added to openHAB.
This can be achieved by

- assigning a static IP address or
- using DHCP and setup the router to always assign the same IP address to the device

### Password Protected Devices

The Shelly devices can be configured to require authentication through a user id and password.
In this case you need to set these values in the Thing configuration after approving the Inbox entry.

## Binding Configuration

The binding has the following configuration options:

| Parameter      |Description                                                       |Mandatory|Default                                         |
|----------------|------------------------------------------------------------------|---------|------------------------------------------------|
| defaultUserId  |Default user id for HTTP authentication when not set in the Thing |    no   |admin                                           |
| defaultPassword|Default password for HTTP authentication when not set in the Thing|    no   |admin                                           |

## Thing Configuration

|Parameter         |Description                                                   |Mandatory|Default                                           |
|------------------|--------------------------------------------------------------|---------|--------------------------------------------------|
|deviceIp          |IP address of the Shelly device                               |    yes  |none                                              |
|userId            |The user id used for HTTP authentication                      |    no   |none                                              |
|password          |Password for HTTP authentication*                             |    no   |none                                              |
|lowBattery        |Threshold for battery level. Set alert when level is below.   |    no   |20 (=20%), only for battery powered devices       |
|updateInterval    |Interval for the background status check in seconds.          |    no   |1h for battery powered devices, 60s for all others|
|eventsButton      |true: register event "trigger when a button is pushed"        |    no   |false                                             |
|eventsPush        |true: register event "trigger on short and long push"         |    no   |false                                             |
|eventsSwitch      |true: register event "trigger of switching the relay output"  |    no   |true                                              |
|eventsSensorReport|true: register event "posted updated sensor data"             |    no   |true for sensor devices                           |
|eventsCoIoT       |true: Listen for CoIoT/COAP events                            |    no   |true for battery devices, false for others        |


## Channels

### General Notes

- The channels `input` and `input1`/`input2` are only updated with firmware 1.5.6+.
- Use the channel `rollerpos` only if you need the inverted value, otherwise use the control channel with item type `Number`, see example below.
- Short push and long push events require firmware version 1.5.6+.
- The different devices have different types of power meters, i.e. different sets of channels.

Every device has a channel group `device` with the following channels:

|Group     |Channel      |Type     |read-only|Desciption                                                                       |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|device    |uptime       |Number   |yes      |Number of seconds since the device was powered up                                |
|          |signal       |Number   |yes      |WiFi signal strength (RSSI)                                                      |
|          |alarm        |Trigger  |yes      |Most recent alarm for health check                                               |


### Alarm Events

The binding provides health monitoring functions for the device.
When an alarm condition is detected the channel alarm gets triggered and provides one of the following alarm types:

|Event Type|Description|
|------------|-----------------------------------------------------------------------------------------------------------------|
|WEAK_SIGNAL |An alarm is triggered when RSSI is < -80, which indicates an unstable connection.                                |
|RESTARTED   |The device has been restarted. This could be an indicator for a firmware problem.                                |
|OVER_TEMP   |The device is over heating, check installation and housing.                                                      |
|OVER_LOAD   |An over load condition has been detected, e.g. from the roller motor.                                            |
|OVER_POWER  |Maximum allowed power was exceeded. The relay was turned off.                                                    |
|LOAD_ERROR  |Device reported a load problem.                                                                                  |

A new alarm will be triggered on a new condition or every 5 minutes if the condition persists.

```
rule "Shelly Alarm"
when
    Channel "shelly:shelly1:XXXXXX:device#alarm" triggered
then
    logInfo("Shelly", "n alarm condition was detected:" + receivedEvent.toString())
end
```

### Shelly 1 (thing-type: shelly1)

|Group     |Channel      |Type     |read-only|Desciption                                                                       |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                       |

### Shelly 1PM (thing-type: shelly1pm)

|Group     |Channel      |Type     |read-only|Desciption                                                                       |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                       |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |lastPower1   |Number   |yes      |Energy consumption in Watts for a round minute, 1 minute  ago                    |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 2 minutes ago                    |
|          |lastPower3   |Number   |yes      |Energy consumption in Watts for a round minute, 3 minutes ago                    |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart) |
|          |timestamp    |String   |yes      |Timestamp of the last measurement                                                |

### Shelly EM (thing-type: shellyem)

|Group     |Channel      |Type     |read-only|Desciption                                                                       |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                       |
|          |event        |Trigger  |yes      |Triggers an event from the device, see general notes for details                 |
|meter1    |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart) |
|          |returnedKWH  |Number   |yes      |Total returned energy, kw/h                                                      |
|          |reactiveWatts|Number   |yes      |Instantaneous reactive power, Watts                                              |
|          |voltage      |Number   |yes      |RMS voltage, Volts                                                               |
|          |timestamp    |String   |yes      |Timestamp of the last measurement                                                |
|meter2    |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart) |
|          |returnedKWH  |Number   |yes      |Total returned energy, kw/h                                                      |
|          |reactiveWatts|Number   |yes      |Instantaneous reactive power, Watts                                              |
|          |voltage      |Number   |yes      |RMS voltage, Volts                                                               |
|          |timestamp    |String   |yes      |Timestamp of the last measurement                                                |

### Shelly 2 - relay mode thing-type: shelly2-relay)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay1    |output       |Switch   |r/w      |Relay #1: Controls the relay's output channel (on/off)                           |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                       |
|          |autoOn       |Number   |r/w      |Relay #1: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #1: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #1: ON: An auto-on/off timer is active                                     |
|          |event        |Trigger  |yes      |Relay #1: Triggers an event from the device, see general notes for details       |
|relay2    |output       |Switch   |r/w      |Relay #2: Controls the relay's output channel (on/off)                           |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                       |
|          |autoOn       |Number   |r/w      |Relay #2: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #2: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #2: ON: An auto-on/off timer is active                                     |
|          |event        |Trigger  |yes      |Relay #2: Triggers an event from the device, see general notes for details       |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |lastPower1   |Number   |yes      |Energy consumption in Watts for a round minute, 1 minute  ago                    |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 2 minutes ago                    |
|          |lastPower3   |Number   |yes      |Energy consumption in Watts for a round minute, 3 minutes ago                    |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart) |
|          |timestamp    |String   |yes      |Timestamp of the last measurement                                                |

### Shelly 2 - roller mode thing-type: shelly2-roller)

|Group     |Channel      |Type     |read-only|Description                                                                           |
|----------|-------------|---------|---------|--------------------------------------------------------------------------------------|
|roller    |control      |Rollershutter|r/w  |can be open (0%), stop, or close (100%); could also handle ON (open) and OFF (close)  |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                            |
|          |rollerpos    |Number   |r/w      |Roller position: 100%=open...0%=closed; gets updated when the roller stops, see Notes |
|          |lastDirection|String   |yes      |Last direction: open or close                                                         |
|          |stopReason   |String   |yes      |Last stop reasons: normal, safety_switch or obstacle                                  |
|          |event        |Trigger  |yes      |Triggers an event from the device, see general notes for details                      |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                                    |
|          |lastPower1   |Number   |yes      |Energy consumption in Watts for a round minute, 1 minute  ago                         |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 2 minutes ago                         |
|          |lastPower3   |Number   |yes      |Energy consumption in Watts for a round minute, 3 minutes ago                         |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart)      |
|          |timestamp    |String   |yes      |Timestamp of the last measurement                                                     |

### Shelly 2.5 - relay mode (thing-type:shelly25-relay) 

The Shelly 2.5 includes 2 meters, one for each channel.

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay1    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay2    |             |         |         |See group relay1 for Shelly 2                                                    |
|meter1    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter2    |             |         |         |See group meter1 for Shelly 2                                                    |

### Shelly 2.5 - roller mode (thing-type: shelly25-roller)

The Shelly 2.5 includes 2 meters, one for each channel.
However, it doesn't make sense to differ power consumption for the roller moving up vs. moving down.
For this the binding aggregates the power consumption of both relays and includes the values in "meter1".

|Group     |Channel      |Type     |read-only|Description                                                                                |
|----------|-------------|---------|---------|-------------------------------------------------------------------------------------------|
|roller    |control      |Rollershutter   |r/w      |can be open (0%), stop, or close (100%); could also handle ON (open) and OFF (close)|
|          |rollerpos    |Dimmer  |r/w      |Roller position: 100%=open...0%=closed; gets updated when the roller stopped                |
|          |input        |Switch   |yes      |ON: Input/Button is powered, see General Notes on Channels                                 |
|          |lastDirection|String   |yes      |Last direction: open or close                                                              |
|          |stopReason   |String   |yes      |Last stop reasons: normal, safety_switch or obstacle                                       |
|          |calibrating  |Switch   |yes      |ON: Roller is in calibration mode, OFF: normal mode (no calibration)                       |
|          |positioning  |Switch   |yes      |ON: Roller is positioning/moving                                                           |
|          |event        |Trigger  |yes      |Triggers an event from the device, see general notes for details                           |
|meter     |             |         |         |See group meter1 for Shelly 2                                                              |

### Shelly4 Pro (thing-type: shelly4pro)

The Shelly 4Pro provides 4 relays and 4 power meters.
 
|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay1    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay2    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay3    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay4    |             |         |         |See group relay1 for Shelly 2                                                    |
|meter1    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter2    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter3    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter4    |             |         |         |See group meter1 for Shelly 2                                                    |

### Shelly Plug-S (thing-type: shellyplugs)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |             |         |         |See group relay1 for Shelly 2                                                    |
|meter     |             |         |         |See group meter1 for Shelly 2                                                    |
|led       |statusLed    |Switch   |r/w      |ON: Status LED is disabled, OFF: LED enabled                                     |
|          |powerLed     |Switch   |r/w      |ON: Power LED is disabled, OFF: LED enabled                                      |

### Shelly Dimmer (thing-type: shellydimmer)

|Group     |Channel      |Type     |read-only|Description                                                                      |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |brightness   |Dimmer   |r/w      |Currently selected brightness.                                                   |
|          |input1       |Switch   |yes      |State of Input 1 (S1)                                                            |
|          |input2       |Switch   |yes      |State of Input 2 (S2)                                                            |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds          |
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds          |
|          |event        |Trigger  |yes      |Triggers an event from the device, see general notes for details                 |
|status    |loaderror    |Switch   |yes      |Last error, "no" if none                                                         |
|          |overload     |Switch   |yes      |Overload condition detected, switch dimmer off or reduce load!                   |
|          |overtemperature |Switch|yes      |Internal device temperature over maximum. Switch off, check physical installation|
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |lastPower1   |Number   |yes      |Energy consumption in Watts for a round minute, 1 minute  ago                    |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 2 minutes ago                    |
|          |lastPower3   |Number   |yes      |Energy consumption in Watts for a round minute, 3 minutes ago                    |
|          |totalKWH     |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart) |
|          |timestamp    |String   |yes      |Timestamp of the last measurement                                                |

### Shelly Bulb (thing-type: shellybulb)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |power        |Switch   |r/w      |Switch light ON/OFF                                                    |
|          |mode         |Switch   |r/w      |Color mode: color or white                                             |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF; in sec            |
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON: in sec            |
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|color     |             |         |         |Color settings: only valid in COLOR mode                               |
|          |hsb          |HSB      |r/w      |Represents the color picker (HSBType), control r/g/b, bight not white  |
|          |full         |String   |r/w      |Set Red / Green / Blue / Yellow / White mode and switch mode           |
|          |             |         |r/w      |Valid settings: "red", "green", "blue", "yellow", "white" or "r,g,b,w" | 
|          |red          |Dimmer   |r/w      |Red brightness: 0..100% or 0..255 (control only the red channel)       |
|          |green        |Dimmer   |r/w      |Green brightness: 0..100% or 0..255 (control only the red channel)     |
|          |blue         |Dimmer   |r/w      |Blue brightness: 0..100% or 0..255 (control only the red channel)      |
|          |white        |Dimmer   |r/w      |White brightness: 0..100% or 0..255 (control only the red channel)     |
|          |gain         |Dimmer   |r/w      |Gain setting: 0..100%     or 0..100                                    |
|          |effect       |Number   |r/w      |Puts the light into effect mode: 0..6)                                 |
|          |             |         |         |  0=No effect, 1=Meteor Shows, 2=Gradual Change, 3=Breath              |
|          |             |         |         |  4=Flash, 5=On/Off Gradual, 6=Red/Green Change                        |
|white     |             |         |         |Color settings: only valid in WHITE mode                               |
|          |temperature  |Number   |r/w      |color temperature (K): 0..100% or 3000..6500                           |
|          |brightness   |Dimmer   |         |Brightness: 0..100% or 0..100                                          |
 
### Shelly RGBW2 in Color Mode (thing-type: shellyrgbw2-color)

|Group     |Channel      |Type     |read-only|Desciption                                                             |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |power        |Switch   |r/w      |Switch light ON/OFF                                                    |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|light     |color        |Color    |r/w      |Color picker (HSBType)                                                 |
|          |fullColor    |String   |r/w      |Set Red / Green / Blue / Yellow / White mode and switch mode           | 
|          |             |         |r/w      |Valid settings: "red", "green", "blue", "yellow", "white" or "r,g,b,w" | 
|          |red          |Dimmer   |r/w      |Red brightness: 0..100% or 0..255 (control only the red channel)       |
|          |green        |Dimmer   |r/w      |Green brightness: 0..100% or 0..255 (control only the red channel)     |
|          |blue         |Dimmer   |r/w      |Blue brightness: 0..100% or 0..255 (control only the red channel)      |
|          |white        |Dimmer   |r/w      |White brightness: 0..100% or 0..255 (control only the red channel)     |
|          |gain         |Dimmer   |r/w      |Gain setting: 0..100%     or 0..100                                    |
|          |effect       |Number   |r/w      |Select a special effect                                                | 
|          |             |         |         |  0=No effect, 1=Meteor Shows, 2=Gradual Change, 3=Flash               |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                     |

### Shelly RGBW2 in White Mode (thing-type: shellyrgbw2-white)

|Group     |Channel      |Type     |read-only|Desciption                                                             |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|channel1  |power        |Switch   |r/w      |Channel 1: Turn channel on/off                                         |
|          |brightness   |Dimmer   |r/w      |Channel 1: Brightness: 0..100                                          |
|channel2  |power        |Switch   |r/w      |Channel 2: Turn channel on/off                                         |
|          |brightness   |Dimmer   |r/w      |Channel 2: Brightness: 0..100                                          |
|channel3  |power        |Switch   |r/w      |Channel 3: Turn channel on/off                                         |
|          |brightness   |Dimmer   |r/w      |Channel 3: Brightness: 0..100                                          |
|channel4  |power        |Switch   |r/w      |Channel 4: Turn channel on/off                                         |
|          |brightness   |Dimmer   |r/w      |Channel 4: Brightness: 0..100                                          |
|meter1    |currentWatts |Number   |yes      |Channel 1: Current power consumption in Watts                          |
|meter2    |currentWatts |Number   |yes      |Channel 2: Current power consumption in Watts                          |
|meter3    |currentWatts |Number   |yes      |Channel 3: Current power consumption in Watts                          |
|meter4    |currentWatts |Number   |yes      |Channel 4: Current power consumption in Watts                          |

Please note that the settings of channel group color are only valid in color mode and vice versa for white mode.
The current firmware doesn't support the timestamp report for the meters.
In this case "n/a" is returned.
Maybe an upcoming firmware release adds this attribute, then the correct value is returned;

### Shelly HT (thing-type: shellyht)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|sensors   |temperature  |Number   |yes      |Temperature, unit is reported by tempUnit                              |
|          |humidity     |Number   |yes      |Relative humidity in %                                                 |
|          |last_update  |String   |yes      |Timestamp of the last update (values read by the binding)              |
|          |event        |Trigger  |yes      |Triggers an event from the device, see general notes for details       |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |voltage      |Number   |yes      |Voltage of the battery                                                 |
|          |lowBattery   |Switch   |yes      |Low battery alert (< 20%)                                              |

### Shelly Flood (thing type: shellyflood)

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|sensors   |temperature  |Number   |yes      |Temperature, unit is reported by tempUnit                              |
|          |flood        |Switch   |yes      |ON: Flooding condition detected, OFF: no flooding                      |
|          |last_update  |String   |yes      |Timestamp of the last update (values read by the binding)              |
|          |event        |Trigger  |yes      |Triggers an event from the device, see general notes for details       |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |voltage      |Number   |yes      |Voltage of the battery                                                 |
|          |lowBattery   |Switch   |yes      |Low battery alert (< 20%)                                              |

### Shelly Sense (thing-type: shellysense) 

|Group     |Channel      |Type     |read-only|Description                                                            |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |key          |String   |r/w      |Send a IR key to the sense. There a 3 different types supported        |
|          |             |         |         |Stored key: send the key code defined by the App , e.g. 123_1_up       |
|          |             |         |         |Pronto hex: send a Pronto Code in ex format, e.g. 0000 006C 0022 ...   |
|          |             |         |         |Pronto base64: in base64 format, will be send 1:1 to the Sense         |
|          |motionTime   |Number   |r/w      |Define the number of seconds when the Sense should report motion       |
|          |motionLED    |Switch   |r/w      |Control the motion LED: ON when motion is detected or OFF              |
|          |charger      |Switch   |yes      |ON: charger connected, OFF: charger not connected.                     |
|sensors   |temperature  |Number   |yes      |Temperature in °C                                                      |
|          |humidity     |Number   |yes      |Relative humidity in %                                                 |
|          |lux          |Number   |yes      |Brightness in Lux                                                      |
|          |motion       |Switch   |yes      |ON: Motion detected, OFF: No motion (check also motionTimer)           |
|          |last_update  |String   |yes      |Timestamp of the last update (values read by the binding)              |
|          |event        |Trigger  |yes      |Triggers an event from the device, see general notes for details       |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |batteryAlert |Switch   |yes      |Low battery alert                                                      |

## Full Example

### shelly.things

```
/* Shelly 2.5 Roller */
Thing shelly:shelly25-roller:XXXXX1 "Shelly 25 Roller XXXXX1" @ "Home Theater" [deviceIp="x.x.x.x", userId="", password=""]
Thing shelly:shelly25-roller:XXXXX2 "Shelly 25 Roller XXXXX2" @ "Living Room"  [deviceIp="x.x.x.x", userId="admin", password="secret"]


/* Shelly 2.5 Relays */
Thing shelly:shelly25-relay:XXXXX3 "Shelly 25 Relay XXXXX3" @ "Hall Way" [deviceIp="x.x.x.x", userId="", password=""]
Thing shelly:shelly25-relay:XXXXX4 "Shelly 25 Relay XXXXX4" @ "Dining Room" [deviceIp="x.x.x.x", userId="", password=""]
Thing shelly:shelly25-relay:XXXXX5 "Shelly 25 Relay XXXXX5" @ "Bed Room" [deviceIp="x.x.x.x", userId="", password=""]

/* Other *
Thing shelly:shellyht:e01691 "ShellyChimenea" @ "lowerground" [ deviceIp="10.0.55.101", userId="", password="", lowBattery=15 , eventsCoIoT=true ]
Thing shelly:shellyht:e01681 "ShellyDormitorio" @ "upperground" [ deviceIp="10.0.55.102", userId="", password="", lowBattery=15 , eventsCoIoT=true ]
Thing shelly:shellyflood:XXXXXX "ShellyFlood" @ "cellar" [ deviceIp="10.0.0.103", userId="", password="", lowBattery=15, eventsSwitch=true, eventsButton=true, eventsCoIoT=true ]

```

### shelly.items

```
/* Relays */
Switch Shelly_XXXXX3_Relay        "Garage Light"                  {channel="shelly:shelly1:XXXXX3:relay#output"}
Switch Shelly_XXXXX3_OverPower    "Garage Light Over Power"       {channel="shelly:shelly1:XXXXX3:relay#overpower"}
Switch Shelly_XXXXX3_OverTemp     "Garage Light Over Temperature" {channel="shelly:shelly1:XXXXX3:relay#overtemperature"}
Number Shelly_XXXXX3_AutoOnTimer  "Garage Light Auto On Timer"    {channel="shelly:shelly1:XXXXX3:relay#autoOn"}
Number Shelly_XXXXX3_AutoOffTimer "Garage Light Auto Off Timer"   {channel="shelly:shelly1:BA2F18:relay#autoOff"}
Switch Shelly__TimerActive        "Garage Light Timer Active"     {channel="shelly:shelly1:BA2F18:relay#timerActive"}

/* Sensors */
Number ShellyHT_Dormitorio_Temp  "Dormitorio Temperature" <temperature> {channel="shelly:shellyht:e01681:sensors#temperature"}
Number ShellyHT_Dormitorio_Humid "Dormitorio Humidity"    <humidity>    {channel="shelly:shellyht:e01681:sensors#humidity"}
Number ShellyHT_Dormitorio_Batt  "Dormitorio Battery"     <battery>     {channel="shelly:shellyht:e01681:battery#batteryLevel"}
Number ShellyHT_Chimenea_Temp    "Chimenea Temperature"   <temperature> {channel="shelly:shellyht:e01691:sensors#temperature"}
Number ShellyHT_Chimenea_Humid   "Chimenea Humidity"      <humidity>    {channel="shelly:shellyht:e01691:sensors#humidity"}
Number ShellyHT_Chimenea_Batt    "Chimenea Battery"       <battery>     {channel="shelly:shellyht:e01691:battery#batteryLevel"}
Number ShellyF_Sotano_Temp       "Sotano Temperature"     <temperature> {channel="shelly:shellyflood:764fe0:sensors#temperature"}
Number ShellyF_Sotano_Batt       "Sotano Battery"         <battery>     {channel="shelly:shellyflood:764fe0:battery#batteryLevel"}
Switch ShellyF_Sotano_Flood      "Sotano Flood Alarm"     <alarm>       {channel="shelly:shellyflood:764fe0:sensors#flood"}

/* Dimmer */
Switch DimmerSwitch     "Light on/off"                       {channel="shelly:shellydimmer:XXX:relay#brightness"}
Dimmer DimmerBrightness "Garage Light Brightness"            {channel="shelly:shellydimmer:XXX:relay#brightness"}
Dimmer DimmerIncDec     "Garage Light +/-"                   {channel="shelly:shellydimmer:XXX:relay#brightness"}

Number Shelly_Power     "Bath Room Light Power"                {channel="shelly:shelly1:XXXXXX:meter#currentWatts"} /* Power Meter */

```

### shelly.rules

reading colors from color picker:

```
import org.openhab.core.library.types.*

rule "color" 
when
    Item ShellyColor changed
then
    var HSBType hsbValue = ShellyColor.state as HSBType
    var int redValue = hsbValue.red.intValue
    var int greenValue = hsbValue.green.intValue
    var int blueValue = hsbValue.blue.intValue
end

```

### shelly.sitemap

```
sitemap demo label="Home"
{
        Frame label="Dimmer" {
            Switch   item=DimmerSwitch
            Slider   item=DimmerBrightness
            SetPoint item=DimmerIncDec

            Number   item=ShellyHT_Dormitorio_Temp
            Number   item=ShellyHT_Chimenea_Humid
            Number   item=ShellyF_Sotano_Batt
            Number   item=Shelly_Power
        }
}

```
