# Dresden Elektronik deCONZ Binding

The Zigbee binding currently does not support the Dresden Elektronik Raspbee and Conbee Zigbee dongles.
The manufacturer provides a companion app called deCONZ together with the mentioned hardware.
deCONZ offers a documented real-time channel that this binding makes use of to bring support for all paired Zigbee devices.

## Supported Things

There is one bridge (`deconz`) that manages the connection to the deCONZ software instance.
These sensors are supported:

| Device type                       | Resource Type                     | Thing type           |
|-----------------------------------|-----------------------------------|----------------------|
| Presence Sensor                   | ZHAPresence, CLIPPresence         | `presencesensor`     |
| Power Sensor                      | ZHAPower, CLIPPower               | `powersensor`        |
| Consumption Sensor                | ZHAConsumption                    | `consumptionsensor`  |
| Switch                            | ZHASwitch                         | `switch`             |
| Light Sensor                      | ZHALightLevel                     | `lightsensor`        |
| Temperature Sensor                | ZHATemperature                    | `temperaturesensor`  |
| Humidity Sensor                   | ZHAHumidity                       | `humiditysensor`     |
| Pressure Sensor                   | ZHAPressure                       | `pressuresensor`     |
| Open/Close Sensor                 | ZHAOpenClose                      | `openclosesensor`    |
| Water Leakage Sensor              | ZHAWater                          | `waterleakagesensor` |
| Alarm Sensor                      | ZHAAlarm                          | `alarmsensor`        |
| Fire Sensor                       | ZHAFire                           | `firesensor`         |
| Vibration Sensor                  | ZHAVibration                      | `vibrationsensor`    |
| deCONZ Artificial Daylight Sensor | deCONZ specific: simulated sensor | `daylightsensor`     |
| Carbon-Monoxide Sensor            | ZHACarbonmonoxide                 | `carbonmonoxide`     |
| Air quality Sensor                | ZHAAirQuality                     | `airqualitysensor`   |
| Color Controller                  | ZBT-Remote-ALL-RGBW               | `colorcontrol`       |

Additionally lights, window coverings (blinds), door locks and thermostats are supported:

| Device type                          | Resource Type                                 | Thing type              |
|--------------------------------------|-----------------------------------------------|-------------------------|
| Dimmable Light                       | Dimmable light, Dimmable plug-in unit         | `dimmablelight`         |
| On/Off Light                         | On/Off light, On/Off plug-in unit, Smart plug | `onofflight`            |
| Color Temperature Light              | Color temperature light                       | `colortemperaturelight` |
| Color Light (w/o temperature)        | Color dimmable light                          | `colorlight`            |
| Extended Color Light (w/temperature) | Extended color light                          | `extendedcolorlight`    |
| Blind / Window Covering              | Window covering device                        | `windowcovering`        |
| Thermostat                           | ZHAThermostat                                 | `thermostat`            |
| Warning Device (Siren)               | Warning device                                | `warningdevice`         |
| Door Lock                            | A remotely operatable door lock               | `doorlock`              |

Currently only light-groups are supported via the thing-type `lightgroup`.

## Discovery

deCONZ software instances are discovered automatically in the same subnet.
Sensors, switches, groups, lights and blinds are discovered as soon as a `deconz` bridge thing comes online.
If your device is not discovered, please check the DEBUG log for unknown devices and report your findings.

## Thing Configuration

### Bridge

These configuration parameters are available:

| Parameter | Description                                                                     | Type    | Default |
|-----------|---------------------------------------------------------------------------------|---------|---------|
| host      | Host address (hostname / ip) of deCONZ interface                                | string  | n/a     |
| httpPort  | Port of deCONZ HTTP interface                                                   | string  | 80      |
| port      | Port of deCONZ Websocket (optional, can be filled automatically) **(Advanced)** | string  | n/a     |
| apikey    | Authorization API key (optional, can be filled automatically)                   | string  | n/a     |
| timeout   | Timeout for asynchronous HTTP requests (in milliseconds)                        | integer | 2000    |

The deCONZ bridge requires the IP address or hostname as a configuration value in order for the binding to know where to access it.
If needed you can specify an optional port for the HTTP interface or the Websocket.
The Websocket port can be filled automatically by requesting it via the HTTP interface - you only need to specify it if your deCONZ instance is running containerized.

The API key is an optional value.
If a deCONZ API key is available because it has already been created manually, it can also be entered as a configuration value.
Otherwise the field can be left empty and the binding will generate the key automatically.
For this process the deCONZ bridge must be unlocked in the deCONZ software so that third party applications can register ([see deCONZ documentation](https://dresden-elektronik.github.io/deconz-rest-doc/getting_started/#unlock-the-gateway)).

### Things

All non-bridge things share the mandatory `id` parameter, an integer assigned to the device while pairing to deconz.
Auto-discovered things do not need to be configured.

All sensor-things have an additional `lastSeenPolling` parameter.
Due to limitations in the API of deCONZ, the `lastSeen` channel (available some sensors) is only available when using polling.
Allowed values are all positive integers, the unit is minutes.
The default-value is `1440`, which means "once a day".

`dimmablelight`, `extendedcolorlight`, `colorlight` and `colortemperaturelight` have an additional optional parameter `transitiontime`.
The transition time is the time to move between two states and is configured in seconds.
The resolution provided is 1/10s.
If no value is provided, the default value of the device is used.

`extendedcolorlight`, `colorlight` and `lightgroup` have different modes for setting the color.
Some devices accept only XY, others HSB, others both modes and the binding tries to autodetect the correct mode.
If this fails, the advanced `colormode` parameter can be set to `xy` or `hs`.

### Textual Thing Configuration - Retrieving an API Key

If you use the textual configuration, the thing file without an API key will look like this, for example:

```java
Bridge deconz:deconz:homeserver [ host="192.168.0.10" ]
```

In this case, the API key is generated automatically as described above (the deCONZ bridge has to be unlocked).
Please note that the generated key cannot be written automatically to the `.thing` file, and has to be set manually.
The generated key can be queried from the configuration using the openHAB console.
To do this log into the [console](https://www.openhab.org/docs/administration/console.html) and use the command `things show` to display the configuration parameters, e.g:

```shell
things show deconz:deconz:homeserver
```

Afterwards the API key has to be inserted in the `.thing` file as `apikey` configuration value, e.g.:

```java
Bridge deconz:deconz:homeserver [ host="192.168.0.10", apikey="ABCDEFGHIJ" ]
```

## Channels

The sensor devices support some of the following channels:

| Channel Type ID | Item Type                | Access Mode | Description                                                                               | Thing types                                       |
|-----------------|--------------------------|-------------|-------------------------------------------------------------------------------------------|---------------------------------------------------|
| presence        | Switch                   | R           | Status of presence: `ON` = presence; `OFF` = no-presence                                  | presencesensor                                    |
| enabled         | Switch                   | R/W         | This channel activates or deactivates the sensor                                          | presencesensor                                    |
| last_updated    | DateTime                 | R           | Timestamp when the sensor was last updated                                                | all, except daylightsensor                        |
| last_seen       | DateTime                 | R           | Timestamp when the sensor was last seen                                                   | all, except daylightsensor                        |
| power           | Number:Power             | R           | Current power usage in Watts                                                              | powersensor, sometimes for consumptionsensor      |
| consumption     | Number:Energy            | R           | Current power usage in Watts/Hour                                                         | consumptionsensor                                 |
| voltage         | Number:ElectricPotential | R           | Current voltage in V                                                                      | some powersensors                                 |
| current         | Number:ElectricCurrent   | R           | Current current in mA                                                                     | some powersensors                                 |
| button          | Number                   | R           | Last pressed button id on a switch                                                        | switch, colorcontrol                              |
| gesture         | Number                   | R           | A gesture that was performed with the switch                                              | switch                                            |
| lightlux        | Number:Illuminance       | R           | Current light illuminance in Lux                                                          | lightsensor                                       |
| light_level     | Number                   | R           | Current light level                                                                       | lightsensor                                       |
| dark            | Switch                   | R           | Light level is below the darkness threshold                                               | lightsensor, sometimes for presencesensor         |
| daylight        | Switch                   | R           | Light level is above the daylight threshold                                               | lightsensor                                       |
| temperature     | Number:Temperature       | R           | Current temperature in ˚C                                                                 | temperaturesensor, some Xiaomi sensors,thermostat |
| humidity        | Number:Dimensionless     | R           | Current humidity in %                                                                     | humiditysensor                                    |
| pressure        | Number:Pressure          | R           | Current pressure in hPa                                                                   | pressuresensor                                    |
| open            | Contact                  | R           | Status of contacts: `OPEN`; `CLOSED`                                                      | openclosesensor                                   |
| waterleakage    | Switch                   | R           | Status of water leakage: `ON` = water leakage detected; `OFF` = no water leakage detected | waterleakagesensor                                |
| fire            | Switch                   | R           | Status of a fire: `ON` = fire was detected; `OFF` = no fire detected                      | firesensor                                        |
| alarm           | Switch                   | R           | Status of an alarm: `ON` = alarm was triggered; `OFF` = no alarm                          | alarmsensor                                       |
| tampered        | Switch                   | R           | Status of a zone: `ON` = zone is being tampered; `OFF` = zone is not tampered             | any IAS sensor                                    |
| vibration       | Switch                   | R           | Status of vibration: `ON` = vibration was detected; `OFF` = no vibration                  | alarmsensor                                       |
| light           | String                   | R           | Light level: `Daylight`; `Sunset`; `Dark`                                                 | daylightsensor                                    |
| value           | Number                   | R           | Sun position: `130` = dawn; `140` = sunrise; `190` = sunset; `210` = dusk                 | daylightsensor                                    |
| battery_level   | Number                   | R           | Battery level (in %)                                                                      | any battery-powered sensor                        |
| battery_low     | Switch                   | R           | Battery level low: `ON`; `OFF`                                                            | any battery-powered sensor                        |
| carbonmonoxide  | Switch                   | R           | `ON` = carbon monoxide detected                                                           | carbonmonoxide                                    |
| airquality      | String                   | R           | Current air quality level                                                                 | airqualitysensor                                  |
| airqualityppb   | Number:Dimensionless     | R           | Current air quality ppb (parts per billion)                                               | airqualitysensor                                  |
| color           | Color                    | R           | Color set by remote                                                                       | colorcontrol                                      |
| windowopen      | Contact                  | R           | `windowopen` status is reported by some thermostats                                       | thermostat                                        |

**NOTE:** Beside other non mandatory channels, the `battery_level` and `battery_low` channels will be added to the Thing during runtime if the sensor is battery-powered.
The specification of your sensor depends on the deCONZ capabilities.
Have a detailed look for [supported devices](https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Supported-Devices).

The `last_seen` channel is added when it is available AND the `lastSeenPolling` parameter of this sensor is used to enable polling.

Other devices support

| Channel Type ID   | Item Type                | Access Mode | Description                           | Thing types                                     |
|-------------------|--------------------------|:-----------:|---------------------------------------|-------------------------------------------------|
| brightness        | Dimmer                   |     R/W     | Brightness of the light               | `dimmablelight`, `colortemperaturelight`        |
| switch            | Switch                   |     R/W     | State of an ON/OFF device             | `onofflight`                                    |
| color             | Color                    |     R/W     | Color of a multi-color light          | `colorlight`, `extendedcolorlight`, `lightgroup`|
| color_temperature | Number                   |     R/W     | Color temperature in Kelvin. The value range is determined by each individual light     | `colortemperaturelight`, `extendedcolorlight`, `lightgroup` |
| effect            | String                   |     R/W     | Effect selection. Allowed commands are set dynamically                                  | `colorlight`                                    |
| effectSpeed       | Number                   |     W       | Effect Speed                          | `colorlight`                                    |
| lock              | Switch                   |     R/W     | Lock (ON) or unlock (OFF) the doorlock| `doorlock`                                      |
| ontime            | Number:Time              |     W       | Timespan for which the light is turned on | all lights |
| position          | Rollershutter            |     R/W     | Position of the blind                 | `windowcovering`                                |
| heatsetpoint      | Number:Temperature       |     R/W     | Target Temperature in °C              | `thermostat`                                    |
| valve             | Number:Dimensionless     |     R       | Valve position in %                   | `thermostat`                                    |
| mode              | String                   |     R/W     | Mode: "auto", "heat" and "off"        | `thermostat`                                    |
| offset            | Number                   |     R       | Temperature offset for sensor         | `thermostat`                                    |
| alert             | String                   |     W       | Turn alerts on. Allowed commands are `none`, `select` (short blinking), `lselect` (long blinking) | `warningdevice`, `lightgroup`, `dimmablelight`, `colorlight`, `extendedcolorlight`, `colortemperaturelight` |
| all_on            | Switch                   |     R       | All lights in group are on            | `lightgroup`                                    |
| any_on            | Switch                   |     R       | Any light in group is on              | `lightgroup`                                    |
| scene             | String                   |     W       | Recall a scene. Allowed commands are set dynamically                                    | `lightgroup`                                    |

**NOTE:** For groups `color` and `color_temperature`  are used for sending commands to the group.
Their state represents the last command send to the group, not necessarily the actual state of the group.

### Trigger Channels

The dimmer switch additionally supports trigger channels.

| Channel Type ID | Description              | Thing types          |
|-----------------|--------------------------|----------------------|
| buttonevent     | Event for switch pressed | switch, colorcontrol |
| gestureevent    | Event for gestures       | switch               |

**NOTE:** The `gestureevent` trigger channel is only available if the optional channel `gesture` is present.
Both will be added during runtime if supported by the switch.
`gestureevent` can trigger one of the following events:

| Gesture                          | Event |
|----------------------------------|-------|
| GESTURE_NONE                     | 0     |
| GESTURE_SHAKE                    | 1     |
| GESTURE_DROP                     | 2     |
| GESTURE_FLIP_90                  | 3     |
| GESTURE_FLIP_180                 | 4     |
| GESTURE_PUSH                     | 5     |
| GESTURE_DOUBLE_TAP               | 6     |
| GESTURE_ROTATE_CLOCKWISE         | 7     |
| GESTURE_ROTATE_COUNTER_CLOCKWISE | 8     |

## Full Example

### Things file

```java
Bridge deconz:deconz:homeserver [ host="192.168.0.10", apikey="ABCDEFGHIJ" ] {
    presencesensor      livingroom-presence     "Livingroom Presence"       [ id="1" ]
    temperaturesensor   livingroom-temperature  "Livingroom Temperature"    [ id="2" ]
    humiditysensor      livingroom-humidity     "Livingroom Humidity"       [ id="3" ]
    airqualitysensor    livingroom-voc          "Livingroom Voc"            [ id="9" ]
    pressuresensor      livingroom-pressure     "Livingroom Pressure"       [ id="4" ]
    openclosesensor     livingroom-window       "Livingroom Window"         [ id="5" ]
    switch              livingroom-hue-tap      "Livingroom Hue Tap"        [ id="6" ]
    waterleakagesensor  basement-water-leakage  "Basement Water Leakage"    [ id="7" ]
    alarmsensor         basement-alarm          "Basement Alarm Sensor"     [ id="8", lastSeenPolling=5 ]
    dimmablelight       livingroom-ceiling      "Livingroom Ceiling"        [ id="1" ]
    lightgroup          livingroom              "Livingroom"                [ id="1" ]
    doorlock            entrance-door           "Door Lock"                 [ id="20" ]
}
```

### Items file

```java
Switch                  Livingroom_Presence     "Presence Livingroom [%s]"          <motion>        { channel="deconz:presencesensor:homeserver:livingroom-presence:presence" }
Number:Temperature      Livingroom_Temperature  "Temperature Livingroom [%.1f °C]"  <temperature>   { channel="deconz:temperaturesensor:homeserver:livingroom-temperature:temperature" }
Number:Dimensionless    Livingroom_Humidity     "Humidity Livingroom [%.1f %%]"     <humidity>      { channel="deconz:humiditysensor:homeserver:livingroom-humidity:humidity" }
String                  Livingroom_voc_label    "Air quality Livingroom [%s]"                       { channel="deconz:airqualitysensor:homeserver:livingroom-voc:airquality" }
Number:Dimensionless    Livingroom_voc          "Air quality [%d ppb]"                              { channel="deconz:airqualitysensor:homeserver:livingroom-voc:airqualityppb" }
Number:Pressure         Livingroom_Pressure     "Pressure Livingroom [%.1f hPa]"    <pressure>      { channel="deconz:pressuresensor:homeserver:livingroom-pressure:pressure" }
Contact                 Livingroom_Window       "Window Livingroom [%s]"            <door>          { channel="deconz:openclosesensor:homeserver:livingroom-window:open" }
Switch                  Basement_Water_Leakage  "Basement Water Leakage [%s]"                       { channel="deconz:waterleakagesensor:homeserver:basement-water-leakage:waterleakage" }
Switch                  Basement_Alarm          "Basement Alarm Triggered [%s]"                     { channel="deconz:alarmsensor:homeserver:basement-alarm:alarm" }
Dimmer                  Livingroom_Ceiling      "Livingroom Ceiling [%d]"           <light>         { channel="deconz:dimmablelight:homeserver:livingroom-ceiling:brightness" }                 
Color                   Livingroom              "Livingroom Light Control"                          { channel="deconz:lightgroup:homeserver:livingroom:color" }
Switch                  Entrance_Door           "Doorlock"                                          { channel="deconz:doorlock:homeserver:entrance-door:lock" }
```

### Events

```java
rule "example trigger rule"
when
    Channel "deconz:switch:homeserver:livingroom-hue-tap:buttonevent" triggered 34   // Hue Tap Button 1 pressed
then
    ...
end
```

### Troubleshooting

By default state updates are ignored for 250ms after a command.
If your light takes more than that to change from one state to another, you might experience a problem with jumping sliders/color pickers.
In that case the `transitiontime` parameter should be changed to the desired time.
