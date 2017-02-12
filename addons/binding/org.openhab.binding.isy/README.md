# ISY Binding

This binding integrates with [Universal Device's ISY994](https://www.universal-devices.com/residential/isy994i-series/) control system.


## Supported Things

This binding currently supports the following thing types:


* dimmer
* switch
* keypad
* motion sensor
* appliance linc
* keypadlinc5
* keypadlinc6
* remotelinc8
* leak detector
* inlinelinc switch
* garage door kit
* program
* scene
* variable

## Discovery

Discovery is supported for the above insteon devices.  Discovery is also supported for Scenes, Programs and Variables.


**Note:** Discovery of the actual Isy has not been implemented.  You will need to add that manually.  Once the Isy has been added, scanning again for the Isy binding will find the insteon devices, programs, etc.

## Binding Configuration

This binding does not require any special configuration.

## Thing Configuration

The bridge requires the IP address of the bridge as well as the username and password to log in to the bridge.


## Channels

The following channels are supported:

| Thing Type      | Channel Type ID   | Item Type    | Description                                  |
|-----------------|-------------------|--------------|--------------------------------------------- |
| dimmer          | lightlevel        | Dimmer       | Increase/decrease the light level            |
| switch          | switchstatus      | Switch       | On/off status of the switch                  |
| motion          | motion_sensor     | Switch       | Motion Detected                              |
| motion          | dusk_sensor       | Switch       | Dusk/Dawn Sensor                             |
| motion          | low_battery_sensor| Switch       | Low Battery Sensor                           |
| garage          | relay             | Switch       | Dusk/Dawn Sensor                             |
| garage          | contactSensor     | Switch       | Low Battery Sensor                           |
| keypadlinc6     | lightlevel        | Switch       | Button to trigger a scene or rule            |
| keypadlinc6     | button_a          | Switch       | Button a                                     |
| keypadlinc6     | button_b          | Switch       | Button b                                     |
| keypadlinc6     | button_c          | Switch       | Button c                                     |
| keypadlinc6     | button_d          | Switch       | Button d                                     |
| remotelinc8     | button_a          | Switch       | Button e                                     |
| remotelinc8     | button_a          | Switch       | Button a                                     |
| remotelinc8     | button_b          | Switch       | Button b                                     |
| remotelinc8     | button_c          | Switch       | Button c                                     |
| remotelinc8     | button_d          | Switch       | Button d                                     |
| remotelinc8     | button_e          | Switch       | Button e                                     |
| remotelinc8     | button_f          | Switch       | Button f                                     |
| remotelinc8     | button_g          | Switch       | Button g                                     |
| remotelinc8     | button_h          | Switch       | Button h                                     |
| leakdetector    | dry               | Switch       | Dry contact sensor                           |
| leakdetector    | wet               | Switch       | Wet contact sensor                           |
| leakdetector    | heartbeat         | Switch       | Heartbeat sensor                             |


## Sitemap sample

```
sitemap testing label="Main Menu"
{
    Frame {
        Switch item=isy_program_isybridge_shower_preheat_run mappings=["ON"="On"] label="Preheat Shower - Run"
        Switch item=isy_program_isybridge_shower_preheat_runThen mappings=["ON"="On"] label="Preheat Shower - Run Then"
        Switch item=isy_program_isybridge_shower_preheat_stop  mappings=["ON"="On"] label="Preheat Shower - Stop"
    }
}
```

## Rule sample

```
var Timer timer

rule "Turn on music in bathroom with motion"
when
        Item isy_motion_isybridge_masterbathmotion2_Sensor_motion received update ON or
    Item isy_motion_isybridge_masterbathshwrmot_Sen_motion received update ON
then
        var theTemp = isy_variable_isybridge_2_2_value.state
    logDebug("Master bath", "house_keeping active: " + theTemp)
    if (theTemp == 0) {
    if(now.getHourOfDay > 9  && now.getHourOfDay < 22) {
        logDebug("Master bath","Motion detected in master bath, let's start the sonos.")
        if (timer !=null)
                timer.cancel
        timer = createTimer(now.plusMinutes(3)) [|
                logDebug("Master bath","Turning off bathroom music")
        sendCommand("sonos_PLAY1_RINCON_B8E93759C82601400_control", "PAUSE")
        timer = null
        ]
    sendCommand("sonos_PLAY1_RINCON_B8E93759C82601400_control", "PLAY")
        logDebug("Master bath","Turn on bathroom music")
        }
    } else {
        logDebug("Master bath", "Sonos not turned on because housekeeping is active")
    }
end

rule "Turn of music if lights turned off in bathroom"
when
    Item isy_dimmer_isybridge_masterbathlightsrecessed_lightlevel changed to 0 or
    Item isy_dimmer_isybridge_masterbathlightsrecessed_paddleaction received update DOF 
then
    logDebug("Master bath", "Pausing music because lights turned off")
    sendCommand("sonos_PLAY1_RINCON_B8E93759C82601400_control", "PAUSE")
    if (timer !=null)
                timer.cancel
    timer = null
```

## Items example to expose isy things to alexa - see Hue Emulation
```
Switch  isy_dimmer_isybridge_kitchenlightsfan_lightlevel  "Kitchen Fan" [ "Switchable" ]
Switch  isy_scene_isybridge_kitchenlightscooking_onoff  "Cooking Lights" [ "Switchable" ]
Switch  isy_program_isybridge_garage_s_garbage_start_run "Take Out Garbage" [ "Switchable" ]
```

"Alexa turn on cooking lights." - sends on to the isy scene for kitchen lights cooking
Alexa turn on take out garbage" - runs the take out garbage program on the isy
