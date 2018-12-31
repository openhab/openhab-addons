# Dresden Elektronik deCONZ Binding

The Zigbee binding currently does not support the Dresden Elektronik Raspbee and Conbee Zigbee dongles.
The manufacturer provides a companion app called deCONZ together with the mentioned hardware. deCONZ
offers a documented real-time channel that this binding makes use of to bring support for all
paired Zigbee sensors and switches.

deCONZ also acts as a HUE bridge. This binding is meant to be used together with the HUE binding
which makes the lights and plugs available.

## Supported Things

There is one bridge (`deconz`) that manages the connection to the deCONZ software instance.
These things are supported:

| Device type                       | Resource Type                     | Thing type            | Channels provided         |
| :-------------------------------- | :-------------------------------- | :-------------------- | :------------------------ |
| Presence sensor                   | ZHAPresence, CLIPPrensence        | `presencesensor`      | `presence`                |
| Power sensor                      | ZHAPower, CLIPPower               | `powersensor`         | `power`                   |
| Switch                            | ZHASwitch                         | `switch`              | `button`,`buttonevent`    |
| Light sensor                      | ZHALightLevel                     | `lightsensor`         | `lightlux`                |
| Temperature sensor                | ZHATemperature                    | `temperaturesensor`   | `temperature`             |
| Humidity sensor                   | ZHAHumidity                       | `humiditysensor`      | `humidity`                |
| Open/close sensor                 | ZHAOpenClose                      | `openclosesensor`     | `open`                    |
| deCONZ artificial daylight sensor | Deconz specific: simulated sensor | `daylightsensor`      | `value`,`light`           |

## Discovery

deCONZ software instances are discovered automatically in the same subnet.
Sensors, switches are discovered as soon as a `deconz` bridge Thing comes online.

## Thing Configuration

These configuration parameters are available:

| Parameter | Description                                                   | Type      | Default   |
| :-------- | :------------------------------------------------------------ | :-------: | :-------: |
| host      | Host address (hostname/ip:port) of deCONZ interface           | string    | n/a       |
| apikey    | Authorization API key (optional, can be filled automatically) | string    | n/a       |

The deCONZ bridge requires the IP address or hostname as a configuration value in order for the binding to know where to access it.

The API key is an optional value. If a deCONZ API key is available because it has already been created manually, it can also be entered as a configuration value. Otherwise the field can be left empty and the binding will generate the key automatically. For this process the deCONZ bridge must be unlocked in the deCONZ software so that third party applications can register. [See deCONZ documentation](http://dresden-elektronik.github.io/deconz-rest-doc/getting_started/#unlock-the-gateway)

### Textual Thing Configuration - Retrieving an API Key

If you use the textual configuration, the thing file without an API key will look like this, for example:

```
Bridge deconz:deconz:homeserver [ host="192.168.0.10" ]
```

In this case, the API key is generated automatically as described above (the deCONZ bridge has to be unlocked). Please note that the generated key cannot be written automatically to the `.thing` file, and has to be set manually.
The generated key can be queried from the configuration using the openHAB console. To do this log into the [console](https://www.openhab.org/docs/administration/console.html) and use the command `things show` to display the configuration parameters, e.g:
```
things show deconz:deconz:homeserver
```

Afterwards the displayed API key has to be inserted in the `.thing` file as `apikey` configuration value, e.g.:
```
Bridge deconz:deconz:homeserver [ host="192.168.0.10", apikey="ABCDEFGHIJ" ]
```

## Channels

The devices support some of the following channels:

| Channel Type ID   | Item Type             | Access Mode   | Description                                                               | Thing types       |
| :---------------- | :-------------------- | :-----------: | :------------------------------------------------------------------------ | :---------------- |
| presence          | Switch                | R             | Status of presence: `ON` = presence; `OFF` = no-presence                  | presencesensor    |
| power             | Number:Energy         | R             | Current power usage in Watts                                              | powersensor       |
| button            | Number                | R             | Last pressed button id on a switch                                        | switch            |
| lightlux          | Number:Illuminance    | R             | Current light illuminance in Lux                                          | lightsensor       |
| temperature       | Number:Temperature    | R             | Current temperature in ˚C                                                 | temperaturesensor |
| humidity          | Number:Dimensionless  | R             | Current humidity in %                                                     | humiditysensor    |
| open              | Contact               | R             | Status of contacts: `OPEN`; `CLOSED`                                      | openclosesensor   |
| light             | String                | R             | Light level: `Daylight`,`Sunset`,`Dark`                                   | daylightsensor    |
| value             | Number                | R             | Sun position: `130` = dawn; `140` = sunrise; `190` = sunset; `210` = dusk | daylightsensor    |

### Trigger Channels

The dimmer switch additionally supports a trigger channel.

| Channel Type ID   | Description                                                       | Thing types       |
| :---------------- | :---------------------------------------------------------------- | :---------------- |
| buttonevent       | Event for switch pressed.                                         | switch            |



## Full Example

### Things file ###

```
Bridge deconz:deconz:homeserver [ host="192.168.0.10", apikey="ABCDEFGHIJ" ] {
    presencesensor      livingroom-presence     "Livingroom Presence"       [ id="1" ]
    temperaturesensor   livingroom-temperature  "Livingroom Temperature"    [ id="2" ]
    humiditysensor      livingroom-humidity     "Livingroom Humidity"       [ id="3" ]
    openclosesensor     livingroom-window       "Livingroom Window"         [ id="4" ]
	switch              livingroom-hue-tap      "Livingroom Hue Tap"        [ id="5" ]
}
```

### Items file ###

```
Switch                  Livingroom_Presence     "Presence Livingroom [%s]"          <motion>        { channel="deconz:presencesensor:homeserver:livingroom-presence:presence" }
Number:Temperature      Livingroom_Temperature  "Temperature Livingroom [%.1f °C]"  <temperature>   { channel="deconz:temperaturesensor:homeserver:livingroom-temperature:temperature" }
Number:Dimensionless    Livingroom_Humidity     "Humidity Livingroom [%.1f %%]"     <humidity>      { channel="deconz:humiditysensor:homeserver:livingroom-humidity:humidity" }
Contact                 Livingroom_Window       "Window Livingroom [%s]"            <door>          { channel="deconz:openclosesensor:homeserver:livingroom-window:open" }
```

### Events

```php
rule "example trigger rule"
when
    Channel "deconz:switch:livingroom-hue-tap:buttonevent" triggered 34   // Hue Tap Button 1 pressed
then
    ...
end
```
