# Qbus Binding
![Qbus Logo](doc/Logo.JPG)

This binding for Qbus communicates for all controllers of the Qbus home automation system.

More information about Qbus can be found here:
[Qbus](https://qbus.be)

This is the link to the [Qbus forum](https://qbusforum.be). This forum is mainly in dutch and you can find a lot of information about the pre testings af this binding and offers a way to communicate with other users.

We also host a site which contains a [manual](https://manualoh.schockaert.tk/) where you can find lot's of information.

The controllers can not communicate directly with openHAB, therefore we developed a Client/Server application which you must install prior to enable this binding.
More information can be found here:
[Qbus Client/Server](https://github.com/QbusKoen/QbusClientServer)

With this binding you can control and read almost every output from the Qbus system.

## Supported Things

The following things are supported by the Qbus Binding:
- Dimmer 1 button, 2 button and clc as _dimmer_
- Bistabiel, Timer1-3, Interval as _onOff_
- Thermostats - normal and PID as _thermosats_
- Scenes as _scene_
- CO2 as _co2_
- Rollershutter and rollerhutter with slats as _rollershutter_

For now the folowing Qbus things are not yet supported but will come:
- DMX
- Timer 4 & 5
- HVAC
- Humidity
- Renson
- Duco
- Kinetura
- Energy monitor
- Weather station


## Discovery

The discovery service is not yet implemented but the System Manager III software of Qbus generates things and item files from the programming, which you can use directly in openHAB.

## Thing Configuration

This is an example of the things configuration file:

```
Bridge qbus:bridge:CTD001122 [ addr="localhost", sn="001122", port=8447, refresh=10 ] {
    dimmer                   1     "ToonzaalLED"      [ dimmerId=100 ]
    onOff                    30    "Toonzaal230V"     [ bistabielId=76 ]
    thermostat               50    "Service"          [ thermostatId=99 ]
    scene                    70    "Disco"            [ sceneId=36 ]
    co2                      100   "Productie"        [ co2Id=26 ]
    rollershutter            120   "Roller1"          [ rolId=268 ]
    rollershutter_slats      121   "Roller2"          [ rolId=264 ]
}
```
The Bridge connects to the QbusServer, so if the Client/Server application is installed on the same machine then localhost can be used as address. sn is the serial nr of your controller, port should allways be 8447, except in special installations as it is the communication port of the Server application. refresh is a time in minutes which will check the status of the server and reconnects if connection is broken after this time.

## Channels

| channel             | type          | description                                             |
|---------------------|---------------|---------------------------------------------------------|
| onOff               | switch        | This is the channel for Bistable, Timers and Intervals  |
| dimmer              | brightness    | This is the channel for Dimmers 1&2 buttons and CLC     |
| scene               | Switch        | This is the channel for scenes                          |
| co2                 | co2           | This is the channel for CO2 sensors                     |
| rollershutter       | rollershutter | This is the channel for rollershutters                  | 
| rollershutter_slats | rollershutter | This is the channel for rollershutters with slats       |
| thermostat          | setpoint      | This is the channel for thermostats setpoint            |
| thermostat          | measured      | This is the channel for thermostats currenttemp         |
| thermostat          | mode          | This is the channel for thermostats mode                |


## Full Example

### Things:
```
Bridge qbus:bridge:CTD001122 [ addr="localhost", sn="001122", port=8447, refresh=10 ] {
    dimmer                   1     "ToonzaalLED"      [ dimmerId=100 ]
    onOff                    30    "Toonzaal230V"     [ bistabielId=76 ]
    thermostat               50    "Service"          [ thermostatId=99 ]
    scene                    70    "Disco"            [ sceneId=36 ]
    co2                      100   "Productie"        [ co2Id=26 ]
    rollershutter            120    "Roller1"         [ rolId=268 ]
    rollershutter_slats      121    "Roller2"         [ rolId=264 ]
}
```
### Items:
```
Dimmer              ToonzaalLED          <light>    [ "Lighting" ]      {channel="qbus:dimmer:CTD007841:1:brightness"}
Switch              Toonzaal230V         <light>                        {channel="qbus:onOff:CTD007841:30:switch"}
Number:Temperature  ServiceSP"[%.1f °C]" (GroepThermostaten)            {channel="qbus:thermostat:CTD007841:50:setpoint"}
Number:Temperature  ServiceCT"[%.1f °C]" (GroepThermostaten)            {channel="qbus:thermostat:CTD007841:50:measured"}
Number              ServiceMode          (GroepThermostaten)            {channel="qbus:thermostat:CTD007841:50:mode",ihc="0x33c311" , autoupdate="true"}
Switch              Disco                <light>                        {channel="qbus:scene:CTD007841:36:scene"}
Number              ProductieCO2                                        {channel="qbus:co2:CTD007841:100:co2"}
Rollershutter       Roller1                                             {channel="qbus:rollershutter:CTD007841:120:rollershutter"}
Rollershutter       Roller2                                             {channel="qbus:rollershutter_slats:CTD007841:121:rollershutter"}
Dimmer              Roller2_slats                                       {channel="qbus:rollershutter_slats:CTD007841:121:slats"}
```