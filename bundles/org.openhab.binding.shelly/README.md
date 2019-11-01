
# Shelly Binding (org.openhab.binding.shelly)

This openHAB 2 Binding implements control for the Shelly series of devices. This includes sending commands to the devices as well as reding the device status and sensor data.

Author: Markus Michels (markus7017)
Check  https://community.openhab.org/t/shelly-binding/56862/213 for more information, questions and contributing ideas. Any comment is welcome!

---

## Releases of this binding

Repo / Branches:
Latest **stable** release (master):     https://github.com/markus7017/org.openhab.binding.shelly/tree/master
Latest **snapshot** (work in progress): https://github.com/markus7017/org.openhab.binding.shelly/tree/snapshot

### 2.5-SNAPSHOT
* fix: Roller returns position -1
* Lots of re-factoring

### 2.4.2 release notes (stable)

+ Support for Shelly Flood
+ Support for Shelly Dimmer
+ Support for Shelly EM
+ Sense: read IR code list for Sense from device rather than hard coded list
+ CoIoT/COAP support added, enabled by default for battery devices
+ Create special device (shelly-protected) when device is password protected
+ new channel last_update for sensors = last time one of the state values have been updated
+ Add IP address to discovered Device Name

* Setting Event URLs reworked to support new Roller urls (roller_on/off/stop) and Dimmer URLs (btn1_on/off, btn2_on/off)
* channel name meter.totalWatts changed to meter.totalKWH (returns kw/h, not Watts)
* Roller: re-added OnOffType  (so you could send OPEN or ON / CLOSE or OFF / STOP)
* RGBW2: adjust numMeter (doesn't report this as part of the device property) -> work around for meter.Watts missing 
* Activation of Channel Cache is delayed for 60s to make sure that Persinstence restore is already done 
* dynamic thing updates removed (messes the log file), time and deviceUpTime removed from properties
* logging revised (include device name on most logs), more details about the bundle on startup
* various bug fixes and improvements
* refactoring started to get ready for 2.5 PR


Please delete and re-discover all things!

## Installation

### Before installing a new build

- delete all Shelly things in PaperUI:Configuration:Things and PaperUI:Inbox
- stop OH
- make sure the JSON DB files have no left-overs on Shelly definitions, delete the entries
- run "openhab-cli clean-cache"
- maybe empty your log

### General installation

- copy the jar into you OH's addons folder.
- start OH, wait until initialized
- run the thing discovery from the Inbox


---

## Hall of Frame - Contibutors

@Igi:     lot of testing around RGBW2 and in general!
@mherbst: supported Bulb and Sense testing
@hmerck:  concept, testing, supporting on coap implementation
@alexxio: testing, feedback

Thanks guys for supporting the community.

---

Please note:
This is a beta release, it has bugs, requires manual install etc. Questions, feedback and contributions are welcome (e.g. improving this documentation).


Looking for contribution: If you are familar with HTML and CSS you are welcome to contribute a nice HABpanel widget. ;-)

---

## Supported Devices

## Supported Things

|Thing             |Type                                                    | Status                                                   |
|------------------|--------------------------------------------------------|----------------------------------------------------------|
| shelly1          | Shelly Single Relay Switch                             | fully supported                                          |
| shelly1pm        | Shelly Single Relay Switch with integrated Power Meter | fully supported                                          |
| shelly1em        | Shelly EM  with integrated Power Meter                 | primarily support                                        |
| shelly2-relay    | Shelly Double Relay Switch (Shelly2 and Shelly2.5)     | fully supported                                          |
| shelly2-roller   | Shelly2 in Roller Mode (Shelly2 and Shelly2.5)         | fully supported                                          |
| shelly2dimmer-white   | Shelly Dimmer in White Mode                       | initial implementation, needs to be verified                                        |
| shellyht         | Shelly Sensor (temp+humidity)                          | needs to be verified                                     |
| shellyplug-s     | Shelly Plug                                            | fully supported                                          |
| shellyplug       | Shelly Plug                                            | fully supported                                          |
| shellyrgbw2      | Shelly RGB Controller                                  | fully supported                                          |
| shellybulb       | Shelly Bulb in Color or WHite Mode                     | fully supported                                          |
| shellysense      | Shelly Motion and IR Controller                        | fully supported                                          |
| shelly4pro       | Shelly 4x Relay Switch                                 | fully supported                                          |
| shellysmoke      | Shelly Sensor (temp+humidity)                          | should get discovered, but no special handling yet       |

Feedback is welcome any time. Leave some comments in the forum.

Please send a PM to markus7017 If you encounter errors and include a TRACE log.


## Binding installation

### Firmware

To utilize all features the binding requires firmware version 1.5.0 or newer. This should be available for all devices.
Older versions work in general, but have impacts to functionality (e.g. no events for battery powered devices).

The binding displays a WARNING if the firmware is older. It also informs you when an update is available. Use the device's web ui or the Shelly App to perform the update.

List of Firmware Versions for the different devices could be found here: https://api.shelly.cloud/files/firmware

###m Password protection

For now the binding doesn't support the http authentication. This is on the list. Please deactivate the authentication before discovering devices.


### Alpha/Beta versions

The binding is work in progress. You have to expect bugs etc. and each version might be incompatible to the existing thing defintion, which means no backware compatibility.

Channel definitions are subject to change with any alpha or beta release. Please make sure to **delete all Shelly things before updating*** the binding and clean out the JSON DB:

- delete all Shelly things from PaperUI's Inbox and Thing list
- stop OH
- run openhab-cli clean-cache
- check the JSON db files for shelly references, remove all entries
- copy the jar to the addons/ folder
- start OH, wait until everything is initialized
- run the device discovery

If you hit a problem make sure to post a TRACE log (or send PM) so I could look into the details.

### Instalation

As described above the binding will be installed by copying the jar into the addons folder of your OH installation. Once a stable state is reached the binding may become part of the openHAB 2.5 distribution, but this will take some time. The binding is developed an tested on OH version 2.4, but also runs on 2.5M1. Please post an info if you also verified compatibility to version 2.3. However, this release is not officially supported.


## Discovery

The binding uses mDNS to discovery the Shelly devices. They periodically announce their presence, which can be used by the binding to find them on the local network and fetch their IP address. The binding will then use the Shelly http api to discover device capabilities, read status and control the device. In addition event callbacks will be used to support battery powered devices.

If you are using password protected Shelly devices you need to configure userid and password. You could configure these settings in two ways

1. Global Default: Go to PaperUI:Configuration:Addons:Shelly Binding and edit the configuration. Those will be used when now settings are given on the thing level.
2. Edit the thing configuration. 

Important: The IP address shouldn't change after the device is added as a new thing in openHAB. This could be achieved by

- assigning a static IP address or
- use DHCP and setup the router to assign always the same ip address to the device

You need to re-discover the device if there is a reason why the ip address changed.

New devices could be discovered an added to the openHAB system by using Paper UI's Inbox. Running a manual discovery should show up all devices on your local network. 

There seems to be an issue between OH mDNS implementation and Shelly so that initially the binding is not able to catch the thing’s ip address (in this case the event reports 0.0.0.0 as ip address - this will be ignored) or devices don’t show up all the time. To fix this you need to run the manual discovery multiple times until you see all your devices. Make sure to wakeup battery powered devices (press the button inside the device) so they show up on the network.

## Binding Configuration

The binding has some global configuration options. Go to PaperUI:Configuration:Addons:Shelly Binding to edit those.

| Parameter      |Description                                                    |Mandantory|Default                                         |
|----------------|---------------------------------------------------------------|----------|------------------------------------------------|
| defaultUserId  |Default userid for http authentication when not set in thing   |    no    |admin                                           |
| defaultPassword|Default password for http authentication when not set in thing |    no    |adnub                                           |


### Thing Configuration

|Parameter         |Description                                                   |Mandantory|Default                                           |
|------------------|--------------------------------------------------------------|----------|--------------------------------------------------|
|deviceIp          |IP address of the Shelly device, usually auto-discovered      |    yes   |none                                              |
|userId            |The userid used for http authentication*                      |    no    |none                                              |
|password          |Password for http authentication*                             |    no    |none                                              |
|lowBattery        |Threshold for battery level. Set alert when level is below.   |    no    |20 (=20%), only for battery powered devices       |
|updateInterval    |Interval for the background status check in seconds.          |    no    |1h for battery powered devices, 60s for all others|
|eventsButton      |true: register event "trigger when a button is pushed"        |    no    |false                                             |
|eventsSwitch      |true: register event "trigger of switching the relay output"  |    no    |true                                              |
|eventsSensorReport|true: register event "posted updated sensor data"             |    no    |true for sensor devices                           |
|enableCoIoT       |true: Listen for CoIoT/COAP events, OFF: Don't use COAP       |    no    |true for battery devices, false for others        |

## Channels

### Shelly 1 (thing-type: shelly1)

|Group     |Channel      |Type     |read-only|Desciption                                                                       |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |overpower    |Switch   |yes      |ON: The relay detected an overpower condition, output was turned OFF             |
|          |event        |Trigger  |yes      |Relay #1: Triggers an event when posted by the device                            |
|          |             |         |         |          the payload includes the event type and value as a J                   |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |

### Shelly 2 - relay mode thing-type: shelly2-relay)

|Group     |Channel      |Type     |read-only|Desciption                                                                       |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay1    |output       |Switch   |r/w      |Relay #1: Controls the relay's output channel (on/off)                           |
|          |overpower    |Switch   |yes      |Relay #1: ON: The relay detected an overpower condition, output was turned OFF   |
|          |autoOn       |Number   |r/w      |Relay #1: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #1: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #1: ON: An auto-on/off timer is active                                     |
|          |event        |Trigger  |yes      |Relay #1: Triggers an event when posted by the device                            |
|          |             |         |         |          the payload includes the event type and value as a J                   |
|relay2    |output       |Switch   |r/w      |Relay #2: Controls the relay's output channel (on/off)                           |
|          |overpower    |Switch   |yes      |Relay #2: ON: The relay detected an overpower condition, output was turned OFF   |
|          |trigger      |Trigger  |yes      |Relay #2: An OH trigger channel, see description below                           |
|          |autoOn       |Number   |r/w      |Relay #2: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #2: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #2: ON: An auto-on/off timer is active                                     |
|          |event        |Trigger  |yes      |Relay #2: Triggers an event when posted by the device                            |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |lastPower1   |Number   |yes      |Energy consumption in Watts for a round minute, 1 minute  ago                    |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 2 minutes ago                    |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 3 minutes ago                    |
|          |totalWatts   |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart) |
|          |timestamp    |String   |yes      |Timestamp of the last measurement                                                |

### Shelly 2 - roller mode thing-type: shelly2-roller)

|Group     |Channel      |Type     |read-only|Desciption                                                                          |
|----------|-------------|---------|---------|------------------------------------------------------------------------------------|
|roller    |control      |String   |r/w      |can be open (0%), stop, or close (100%); could also handle ON (open) and OFF (close)|
|          |rollerpos    |Percent  |r/w      |Roller position: 100%=open...0%=closed; gets updated when the roller stopped        |
|          |direction    |String   |yes      |Last direction: open or close                                                       |
|          |stopReason   |String   |yes      |Last stop reasons: normal, safety_switch or obstacle                                |
|          |calibrating  |Switch   |yes      |ON: Roller is in calibration mode, OFF: normal mode (no calibration)                |
|          |event        |Trigger  |yes      |Relay #1: Triggers an event when posted by the device, e,g, btn_up or btn_down      |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                                  |
|          |lastPower1   |Number   |yes      |Energy consumption in Watts for a round minute, 1 minute  ago                       |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 2 minutes ago                       |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 3 minutes ago                       |
|          |totalWatts   |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart)    |
|          |timestamp    |String   |yes      |Timestamp of the last measurement                                                   |

### Shelly 2.5 - relay mode (thing-type:shelly25-relay) 

The Shelly 2.5 includes 2 meters, one for each channel. Refer to Shelly 2 channel layout, the 2nd meter is represented by channel group "meter2" with the same channels like "meter1".

### Shelly 2.5 - roller mode (thing-type: shelly25-roller)

The Shelly 2.5 includes 2 meters, one for each channel. However, it doesn't make sense to differ power consumption for the roller moving up vs. moving down. For this the binding aggregates the power consumption of both relays and includes the values in "meter1". See channel description for Shelly 2 in roller mode.

### Shelly4 Pro

|Group     |Channel      |Type     |read-only|Desciption                                                                       |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay1    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay2    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay3    |             |         |         |See group relay1 for Shelly 2                                                    |
|relay4    |             |         |         |See group relay1 for Shelly 2                                                    |
|meter1    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter2    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter3    |             |         |         |See group meter1 for Shelly 2                                                    |
|meter4    |             |         |         |See group meter1 for Shelly 2                                                    |

### Shelly Dimmer (thing-type: shellydimmer)

|Group     |Channel      |Type     |read-only|Desciption                                                                       |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Controls the relay's output channel (on/off)                                     |
|          |input1       |Switch   |yes      |State of Input 1 (S1)                                                            |
|          |input2       |Switch   |yes      |State of Input 2 (S2)                                                            |
|          |brightness   |Percent  |r/w      |Currently selected brightness.                                                   |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds          |
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds          |
|          |event        |Trigger  |yes      |Triggers an event when posted by the device                                      |
|          |             |         |         |the payload includes the event type and value as a J                             |
|status    |brightness   |Number   |r/w      |Light brightness                                                                 |
|          |error        |String   |yes      |Last error, "no" if none                                                         |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |


### Shelly Plug-S (thing-type: shellyplug-s)

|Group     |Channel      |Type     |read-only|Desciption                                                                       |
|----------|-------------|---------|---------|---------------------------------------------------------------------------------|
|relay     |output       |Switch   |r/w      |Relay #1: Controls the relay's output channel (on/off)                           |
|          |overpower    |Switch   |yes      |Relay #1: ON: The relay detected an overpower condition, output was turned OFF   |
|          |autoOn       |Number   |r/w      |Relay #1: Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Relay #1: Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |Relay #1: ON: An auto-on/off timer is active                                     |
|          |event        |Trigger  |yes      |Relay #1: Triggers an event when posted by the device                            |
|          |             |         |         |          the payload includes the event type and value as a J                   |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                               |
|          |lastPower1   |Number   |yes      |Energy consumption in Watts for a round minute, 1 minute  ago                    |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 2 minutes ago                    |
|          |lastPower2   |Number   |yes      |Energy consumption in Watts for a round minute, 3 minutes ago                    |
|          |totalWatts   |Number   |yes      |Total energy consumption in Watts since the device powered up (reset on restart) |
|          |timestamp    |String   |yes      |Timestamp of the last measurement                                                |
|led       |statusLed    |Switch   |r/w      |ON: Status LED is disabled, OFF: LED enabled                                     |
|          |powerLed     |Switch   |r/w      |ON: Power LED is disabled, OFF: LED enabled                                      |

### Shelly HT (thing-type: shellyht)

|Group     |Channel      |Type     |read-only|Desciption                                                             |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|sensors   |temperature  |Number   |yes      |Temperature, unit is reported by tempUnit                              |
|          |tempUnit     |Number   |yes      |Unit for temperature value: C for Celsius or F for Fahrenheit          |
|          |humidity     |Number   |yes      |Relative humidity in %                                                 |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |batteryAlert |Switch   |yes      |Low battery alert                                                      |
|          |batteryVoltage|Switch  |yes      |Voltage of the battery                                                 |


### Shelly Bulb (thing-type: shellybulb)

|Group     |Channel      |Type     |read-only|Desciption                                                             |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |power        |Switch   |r/w      |Switch light ON/OFF                                                    |
|          |mode         |Switch   |r/w      |Color mode: color or white                                             |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF; in sec            |
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON: in sec            |
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|color     |             |         |         |Color settings: only valid in COLOR mode                               |
|          |hsb          |HSB      |r/w      |Represents the color picker (HSBType), control r/g/b, bight not white  |
|          |fullColor    |String   |r/w      |Set Red / Green / Blue / Yellow / White mode and switch mode           |
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
|          |temperature  |Dimmer   |         |Color temperature: 0..100% for 3000..6500K                             |
|          |brightness   |Dimmer   |         |Brightness: 0..100% or 0..100                                          |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                     |
 


### Shelly RGBW2 in Color Mode

|Group     |Channel      |Type     |read-only|Desciption                                                             |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |power        |Switch   |r/w      |Switch light ON/OFF                                                    |
|          |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|light     |color        |Color    |r/w      |Color picker (HSBType)                                                 |
|          |fullColor    |String   |r/w      |Set Red / Green / Blue / Yellow / White mode and switch mode           | 
|          |             |         |r/w      |Valid settings: "red", "green", "blue", "yellow", "white" or "r,g,b,w" | 
|          |effect       |Number   |r/w      |Select a special effect                                                | 
|          |red          |Number   |r/w      |red brightness 0..255, use this only when not using the color picker   |
|          |green        |Number   |r/w      |green brightness 0..255, use this only when not using the color picker |
|          |blue         |Number   |r/w      |blue brightness 0..255, use this only when not using the color picker  |
|          |white        |Number   |r/w      |white brightness 0..255, use this only when not using the color picker |
|          |gain         |Number   |r/w      |gain 0..255, use this only when not using the color picker             |
|          |temperature  |Number   |r/w      |color temperature (K): 3000..6500                                      |
|meter     |currentWatts |Number   |yes      |Current power consumption in Watts                                     |

### Shelly RGBW2 in White Mode

|control   |autoOn       |Number   |r/w      |Sets a  timer to turn the device ON after every OFF command; in seconds|
|          |autoOff      |Number   |r/w      |Sets a  timer to turn the device OFF after every ON command; in seconds|
|          |timerActive  |Switch   |yes      |ON: An auto-on/off timer is active                                     |
|channel1  |power        |Switch   |r/w      |Channel 1: Turn channel on/off                                         |
|          |brightness   |Number   |r/w      |Channel 1: Brightness: 0..100                                          |
|channel2  |power        |Switch   |r/w      |Channel 2: Turn channel on/off                                         |
|          |brightness   |Number   |r/w      |Channel 2: Brightness: 0..100                                          |
|channel3  |power        |Switch   |r/w      |Channel 3: Turn channel on/off                                         |
|          |brightness   |Number   |r/w      |Channel 3: Brightness: 0..100                                          |
|channel4  |power        |Switch   |r/w      |Channel 4: Turn channel on/off                                         |
|          |brightness   |Number   |r/w      |Channel 4: Brightness: 0..100                                          |
|meter1    |currentWatts |Number   |yes      |Channel 1: Current power consumption in Watts                          |
|meter2    |currentWatts |Number   |yes      |Channel 2: Current power consumption in Watts                          |
|meter3    |currentWatts |Number   |yes      |Channel 3: Current power consumption in Watts                          |
|meter4    |currentWatts |Number   |yes      |Channel 4: Current power consumption in Watts                          |

Please note that the settings of channel group color are only valid in color mode and vice versa for white mode.
The current firmware doesn't support the timestamp report for the meters. In thise case "n/a" is returned. Maybe an upcoming firmware release adds this attribute, then the correct value is returned;


### Shelly Sense

|Group     |Channel      |Type     |read-only|Desciption                                                             |
|----------|-------------|---------|---------|-----------------------------------------------------------------------|
|control   |key          |String   |r/w      |Send a IR key to the sense. There a 3 different types supported        |
|          |             |         |         |Stored key: send the key code defined by the App , e.g. 123_1_up       |
|          |             |         |         |Pronto hex: send a Pronto Code in ex format, e.g. 0000 006C 0022 ...   |
|          |             |         |         |Pronto base64: in base64 format, will be send 1:1 to the Sense         |
|          |motionTime   |Number   |r/w      |Define the number of seconds when the Sense should report motion       |
|          |motionLED    |Switch   |r/w      |Control the motion LED: ON when motion is detected or OFF              |
|          |charger      |Switch   |yes      |ON: charger connected, OFF: charger not connected.                     |
|sensors   |temperature  |Number   |yes      |Temperature, unit is reported by tempUnit                              |
|          |tempUnit     |Number   |yes      |Unit for temperature value: C for Celsius or F for Fahrenheit          |
|          |humidity     |Number   |yes      |Relative humidity in %                                                 |
|          |lux          |Number   |yes      |Brightness in Lux                                                      |
|          |motion       |Switch   |yes      |ON: Motion detected, OFF: No motion (check also motionTimer)           |
|battery   |batteryLevel |Number   |yes      |Battery Level in %                                                     |
|          |batteryAlert |Switch   |yes      |Low battery alert                                                      |



### Other devices

The thing definiton fo the following devices is primarily. If you have one of those devices send a PM to marks7017 and we could work on the implementation/testing.

- thing-type: shellysmoke
- thing-type: shellysense
- thing-type: shellyplug


## Full Example

Note: PaperUI is recommended, if you want to use text files make sure to replace the thing id from you channel definition 

* .things

* .items


* .sitemap

* .rules

reading colors from color picker:
import org.openhab.core.library.types.*
 
var HSBType hsbValue
var int redValue
var int greenValue
var int blueValue
var String RGBvalues

rule "color" // The unique name for the rule
when
    Item ShellyColor changed
then
    var HSBType hsbValue = ShellyColor.state as HSBType
    var int redValue = hsbValue.red.intValue
    var int greenValue = hsbValue.green.intValue
    var int blueValue = hsbValue.blue.intValue
end




