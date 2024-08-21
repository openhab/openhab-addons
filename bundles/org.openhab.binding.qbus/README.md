# Qbus Binding

This binding for [Qbus](https://qbus.be) communicates with all controllers of the Qbus home automation system.

We also host a site which contains a [manual](https://iot.qbus.be/) where you can find lots of information to set up openHAB with Qbus client and server (for the moment only in Dutch).

The controllers can not communicate directly with openHAB, therefore we developed a client/server application which you must install prior to enable this binding.
More information can be found here:
[Qbus Client/Server](https://github.com/QbusKoen/QbusClientServer-Installer)

With this binding you can control and read almost every output from the Qbus system.

## Supported Things

The following things are supported by the Qbus binding:

- `dimmer`: Dimmer 1 button, 2 button and clc
- `onOff`: Bistabiel, Timer1-3, Interval
- `thermostats`: Thermostats - normal and PID
- `scene`: Scenes
- `co2`: CO2
- `rollershutter`: Rollershutter
- `rollershutter_slats`: Rollerhutter with slats

For now the following Qbus things are not yet supported but will come:

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

## Bridge configuration

```java
Bridge qbus:bridge:CTD001122 [ addr="localhost", sn="001122", port=8447, serverCheck=10 ] {
...
}
```

| Property      | Default   | Required | Description                                                                                                                          |
|---------------|-----------|----------|--------------------------------------------------------------------------------------------------------------------------------------|
| `addr`        | localhost | YES      | The ip address of the machine where the Qbus Server runs                                                                             |
| `sn`          |           | YES      | The serial number of your controller                                                                                                 |
| `port`        | 8447      | YES      | The communication port of the client/server                                                                                          |
| `serverCheck` | 10        | NO       | Refresh time - After x minutes there will be a check if server is still running and if client is still connected. If not - reconnect |

## Things configuration

| Thing Type ID         | Channel Name  | Read only | description                                            |
| --------------------- | ------------- | --------- | ------------------------------------------------------ |
| `onOff`               | switch        | No        | This is the channel for Bistable, Timers and Intervals |
| `dimmer`              | brightness    | No        | This is the channel for Dimmers 1&2 buttons and CLC    |
| `scene`               | Switch        | No        | This is the channel for scenes                         |
| `co2`                 | co2           | Yes       | This is the channel for CO2 sensors                    |
| `rollershutter`       | rollershutter | No        | This is the channel for rollershutters                 |
| `rollershutter_slats` | rollershutter | No        | This is the channel for rollershutters with slats      |
| `thermostat`          | setpoint      | No        | This is the channel for thermostats setpoint           |
| `thermostat`          | measured      | Yes       | This is the channel for thermostats currenttemp        |
| `thermostat`          | mode          | No        | This is the channel for thermostats mode               |

## Full Example

### Things

```java
Bridge qbus:bridge:CTD001122 [ addr="localhost", sn="001122", port=8447, serverCheck=10 ] {
    dimmer                   1      "ToonzaalLED"       [ dimmerId=100 ]
    onOff                    30     "Toonzaal230V"      [ bistabielId=76 ]
    thermostat               50     "Service"           [ thermostatId=99 ]
    scene                    70     "Disco"             [ sceneId=36 ]
    co2                      100    "Productie"         [ co2Id=26 ]
    rollershutter            120    "Roller1"           [ rolId=268 ]
    rollershutter_slats      121    "Roller2"           [ rolId=264 ]
}
```

### Items

```java
Dimmer              ToonzaalLED                 <light>                         ["Light"]           {channel="qbus:dimmer:CTD001122:1:brightness"}
Switch              Toonzaal230V                <light>                         ["Switch"]          {channel="qbus:onOff:CTD001122:30:switch"}
Group gThermostaat ["HVAC"]
Number:Temperature  ServiceSP"[%.1f %unit%]"    <temperature>   (gThermostaat)  ["Setpoint"]        {channel="qbus:thermostat:CTD001122:50:setpoint"}
Number:Temperature  ServiceCT"[%.1f %unit%]"    <temperature>   (gThermostaat)  ["Measurement"]     {channel="qbus:thermostat:CTD001122:50:measured"}
Number              ServiceMode                 <temperature>   (gThermostaat)  ["Control"]         {channel="qbus:thermostat:CTD001122:50:mode",ihc="0x33c311" , autoupdate="true"}
Switch              Disco                       <light>                         ["Switch"]          {channel="qbus:scene:CTD001122:36:scene"}
Number              ProductieCO2                <carbondioxide>                 ["CO2"]             {channel="qbus:co2:CTD001122:100:co2"}
Rollershutter       Roller1                     <rollershutter>                 ["Blinds"]          {channel="qbus:rollershutter:CTD001122:120:rollershutter"}
Rollershutter       Roller2                     <rollershutter>                 ["Blinds"]          {channel="qbus:rollershutter_slats:CTD001122:121:rollershutter"}
Dimmer              Roller2_slats               <rollershutter>                 ["Blinds"]          {channel="qbus:rollershutter_slats:CTD001122:121:slats"}
```

This is the link to the [Qbus forum](https://qbusforum.be). This forum is mainly in dutch and you can find a lot of information about the pre testings of this binding and offers a way to communicate with other users.
