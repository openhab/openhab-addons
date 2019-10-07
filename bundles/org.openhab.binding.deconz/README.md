# Dresden Elektronik deCONZ Binding

The Zigbee binding currently does not support the Dresden Elektronik Raspbee and Conbee Zigbee dongles.
The manufacturer provides a companion app called deCONZ together with the mentioned hardware.
deCONZ offers a documented real-time channel that this binding makes use of to bring support for all paired Zigbee sensors and switches.

deCONZ also acts as a HUE bridge.
This binding is meant to be used together with the HUE binding which makes the lights and plugs available.

## Supported Things

There is one bridge (`deconz`) that manages the connection to the deCONZ software instance.
These things are supported:

| Device type                       | Resource Type                     | Thing type           |
|-----------------------------------|-----------------------------------|----------------------|
| Presence Sensor                   | ZHAPresence, CLIPPrensence        | `presencesensor`     |
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
| Vibration Sensor                  | ZHAVibration                      | `vibrationsensor`    |
| deCONZ Artificial Daylight Sensor | deCONZ specific: simulated sensor | `daylightsensor`     |

## Discovery

deCONZ software instances are discovered automatically in the same subnet.
Sensors, switches are discovered as soon as a `deconz` bridge Thing comes online.

## Thing Configuration

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
For this process the deCONZ bridge must be unlocked in the deCONZ software so that third party applications can register ([see deCONZ documentation](http://dresden-elektronik.github.io/deconz-rest-doc/getting_started/#unlock-the-gateway)).

### Textual Thing Configuration - Retrieving an API Key

If you use the textual configuration, the thing file without an API key will look like this, for example:

```
Bridge deconz:deconz:homeserver [ host="192.168.0.10" ]
```

In this case, the API key is generated automatically as described above (the deCONZ bridge has to be unlocked).
Please note that the generated key cannot be written automatically to the `.thing` file, and has to be set manually.
The generated key can be queried from the configuration using the openHAB console.
To do this log into the [console](https://www.openhab.org/docs/administration/console.html) and use the command `things show` to display the configuration parameters, e.g:

```
things show deconz:deconz:homeserver
```

Afterwards the API key has to be inserted in the `.thing` file as `apikey` configuration value, e.g.:

```
Bridge deconz:deconz:homeserver [ host="192.168.0.10", apikey="ABCDEFGHIJ" ]
```

## Channels

The devices support some of the following channels:

| Channel Type ID | Item Type                | Access Mode | Description                                                                               | Thing types                                  |
|-----------------|--------------------------|:-----------:|-------------------------------------------------------------------------------------------|----------------------------------------------|
| presence        | Switch                   |      R      | Status of presence: `ON` = presence; `OFF` = no-presence                                  | presencesensor                               |
| last_updated    | DateTime                 |      R      | Timestamp when the sensor was last updated                                                | all, except daylightsensor                   |
| power           | Number:Power             |      R      | Current power usage in Watts                                                              | powersensor, sometimes for consumptionsensor |
| consumption     | Number:Energy            |      R      | Current power usage in Watts/Hour                                                         | consumptionsensor                            |
| voltage         | Number:ElectricPotential |      R      | Current voltage in V                                                                      | some powersensors                            |
| current         | Number:ElectricCurrent   |      R      | Current current in mA                                                                     | some powersensors                            |
| button          | Number                   |      R      | Last pressed button id on a switch                                                        | switch                                       |
| lightlux        | Number:Illuminance       |      R      | Current light illuminance in Lux                                                          | lightsensor                                  |
| light_level     | Number                   |      R      | Current light level                                                                       | lightsensor                                  |
| dark            | Switch                   |      R      | Light level is below the darkness threshold.                                              | lightsensor, sometimes for presencesensor    |
| daylight        | Switch                   |      R      | Light level is above the daylight threshold.                                              | lightsensor                                  |
| temperature     | Number:Temperature       |      R      | Current temperature in ˚C                                                                 | temperaturesensor, some Xiaomi sensors       |
| humidity        | Number:Dimensionless     |      R      | Current humidity in %                                                                     | humiditysensor                               |
| pressure        | Number:Pressure          |      R      | Current pressure in hPa                                                                   | pressuresensor                               |
| open            | Contact                  |      R      | Status of contacts: `OPEN`; `CLOSED`                                                      | openclosesensor                              |
| waterleakage    | Switch                   |      R      | Status of water leakage: `ON` = water leakage detected; `OFF` = no water leakage detected | waterleakagesensor                           |
| alarm           | Switch                   |      R      | Status of an alarm: `ON` = alarm was triggered; `OFF` = no alarm                          | alarmsensor                                  |
| tampered        | Switch                   |      R      | Status of a zone: `ON` = zone is being tampered; `OFF` = zone is not tampered             | any IAS sensor                               |
| vibration       | Switch                   |      R      | Status of vibration: `ON` = vibration was detected; `OFF` = no vibration                  | alarmsensor                                  |
| light           | String                   |      R      | Light level: `Daylight`,`Sunset`,`Dark`                                                   | daylightsensor                               |
| value           | Number                   |      R      | Sun position: `130` = dawn; `140` = sunrise; `190` = sunset; `210` = dusk                 | daylightsensor                               |
| battery_level   | Number                   |      R      | Battery level (in %)                                                                      | any battery-powered sensor                   |
| battery_low     | Switch                   |      R      | Battery level low: `ON`; `OFF`                                                            | any battery-powered sensor                   |

**NOTE:** Beside other non mandatory channels, the `battery_level` and `battery_low` channels will be added to the Thing during runtime if the sensor is battery-powered.
The specification of your sensor depends on the deCONZ capabilities.
Have a detailed look for [supported devices](https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Supported-Devices).

### Trigger Channels

The dimmer switch additionally supports a trigger channel.

| Channel Type ID | Description               | Thing types |
|-----------------|---------------------------|-------------|
| buttonevent     | Event for switch pressed. | switch      |

## Full Example

### Things file ###

```
Bridge deconz:deconz:homeserver [ host="192.168.0.10", apikey="ABCDEFGHIJ" ] {
    presencesensor      livingroom-presence     "Livingroom Presence"       [ id="1" ]
    temperaturesensor   livingroom-temperature  "Livingroom Temperature"    [ id="2" ]
    humiditysensor      livingroom-humidity     "Livingroom Humidity"       [ id="3" ]
    pressuresensor      livingroom-pressure     "Livingroom Pressure"       [ id="4" ]
    openclosesensor     livingroom-window       "Livingroom Window"         [ id="5" ]
    switch              livingroom-hue-tap      "Livingroom Hue Tap"        [ id="6" ]
    waterleakagesensor  basement-water-leakage  "Basement Water Leakage"    [ id="7" ]
    alarmsensor         basement-alarm          "Basement Alarm Sensor"     [ id="8" ]
}
```

### Items file ###

```
Switch                  Livingroom_Presence     "Presence Livingroom [%s]"          <motion>        { channel="deconz:presencesensor:homeserver:livingroom-presence:presence" }
Number:Temperature      Livingroom_Temperature  "Temperature Livingroom [%.1f °C]"  <temperature>   { channel="deconz:temperaturesensor:homeserver:livingroom-temperature:temperature" }
Number:Dimensionless    Livingroom_Humidity     "Humidity Livingroom [%.1f %%]"     <humidity>      { channel="deconz:humiditysensor:homeserver:livingroom-humidity:humidity" }
Number:Pressure         Livingroom_Pressure     "Pressure Livingroom [%.1f hPa]"    <pressure>      { channel="deconz:pressuresensor:homeserver:livingroom-pressure:pressure" }
Contact                 Livingroom_Window       "Window Livingroom [%s]"            <door>          { channel="deconz:openclosesensor:homeserver:livingroom-window:open" }
Switch                  Basement_Water_Leakage  "Basement Water Leakage [%s]"                       { channel="deconz:waterleakagesensor:homeserver:basement-water-leakage:waterleakage" }
Switch                  Basement_Alarm          "Basement Alarm Triggered [%s]"                     { channel="deconz:alarmsensor:homeserver:basement-alarm:alarm" }
```

### Events

```php
rule "example trigger rule"
when
    Channel "deconz:switch:homeserver:livingroom-hue-tap:buttonevent" triggered 34   // Hue Tap Button 1 pressed
then
    ...
end
```
