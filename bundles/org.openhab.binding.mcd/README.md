# MCD Binding

This binding allows you to send sensor events from your openHAB environment to the cloud application Managing Care Digital (MCD) by [C&S Computer und Software GmbH](https://www.managingcare.de/). 

MCD is the platform for inpatient and outpatient nursing services. Our REST API allows you to send a variety of sensor events to the system and thus being able to connect your Ambient Assisted Living (AAL) or smart home environment to the documentation software of your nursing service. 

Please note that a valid account is needed to access MCD and the Sensor API.


## Supported Things

There are two supported things: **MCD Bridge** and **MCD Sensor Thing**. 


## Discovery

Discovery is not supported.


## Binding Configuration

No binding configuration required. 


## Thing Configuration

This section shows the configuration parameters of both supported things.

### MCD Bridge

The MCD Bridge (`mcdBridge`) needs to be configured with your valid C&S MCD / sync API credentials. 

| parameter | description                        |
|-----------|------------------------------------|
| email     | Email of account                   |
| password  | valid password for the given email |

### MCD Sensor Thing

Each sensor thing (`mcdSensor`) needs to be configured with the identical serial number, that is assigned to this sensor in MCD. 

| parameter      | description                        |
|----------------|------------------------------------|
| parent bridge  | parent MCD bridge is required      |
| serial number  | serial number of the sensor in MCD |

## Channels

The `mcdSensor` thing supports the following channels.  To see the sensors' events, please visit [Managing Care Digital](https://cundsdokumentation.de/) and navigate to the dashboard. 

| channel     | type   | description                                   |
|-------------|--------|-----------------------------------------------|
| lastEvent | String | shows the last event that was sent with date and time |
| sendEvent | String | stateless channel for sending events to the API |

The channel `sendEvents` accepts valid Sensor Event Definitions as well as the corresponding ID. The following table contains allcurrently accepted Sensor Event Definitions and their respective ID. As soon as new events are added to the API, you can use their ID, even if the Definition is not yet added to this list. 

| ID | Definition |
|----|------------|
| 2 | BEDEXIT |
| 3 | BEDENTRY |
| 4 | FALL |
| 5 | CHANGEPOSITION |
| 6 | BATTERYSTATE |
| 7 | INACTIVITY |
| 8 | ALARM |
| 9 | OPEN |
| 10 | CLOSE |
| 11 | ON |
| 12 | OFF |
| 13 | ACTIVITY |
| 14 | CAPACITY |
| 15 | GAS |
| 16 | VITALVALUE |
| 17 | ROOMEXIT |
| 18 | ROOMENTRY |
| 19 | REMOVESENSOR |
| 20 | SITDOWN |
| 21 | STANDUP |
| 22 | INACTIVITYROOM |
| 23 | SMOKEALARM |
| 24 | HEAT |
| 25 | COLD |
| 26 | QUALITYAIR |
| 27 | ALARMAIR |
| 28 | ROOMTEMPERATURE |
| 29 | HUMIDITY |
| 30 | AIRPRESSURE |
| 31 | CO2 |
| 32 | INDEXUV |
| 33 | WEARTIME |
| 34 | FIRSTURINE |
| 35 | NEWDIAPER |
| 36 | DIAPERREMOVED |
| 37 | NOCONNECTION |
| 38 | LOWBATTERY |
| 39 | CONTROLLSENSOR |
| 40 | LYING |
| 41 | SPILLED |
| 42 | DAMAGED |
| 43 | GEOEXIT |
| 44 | GEOENTRY |
| 45 | WALKING |
| 46 | RESTING |
| 47 | TURNAROUND |
| 48 | HOMEEMERGENCY |
| 49 | TOILETFLUSH |
| 50 | DORSALPOSITION |
| 51 | ABDOMINALPOSITION |
| 52 | LYINGLEFT |
| 53 | LYINGRIGHT |
| 54 | LYINGHALFLEFT |
| 55 | LYINGHALFRIGHT |
| 56 | MOVEMENT |
| 57 | PRESENCE |
| 58 | NUMBERPERSONS |
| 59 | BRIGHTNESSZONE |


## Full Example

Here is an example for the textual configuration. You can of course use the Administration section of the GUI as well.

demo.things:

```
Bridge mcd:mcdBridge:exampleBridge [userEmail="your.email@examle.com", userPassword="your.password"]{
    Thing mcd:mcdSensor:examlpeSensor [serialNumber="123"]
    Thing mcd:mcdSensor:secondExamlpeSensor [serialNumber="456"]
}
```

demo.items:

```
String lastValue "Last Value" {channel="mcd:mcdSensor:examlpeSensor:lastValue"}
String sendEvent "Sit Status" {channel="mcd:mcdSensor:examlpeSensor:sendEvent"}
```

demo.sitemap:

```
Text item=sendEvent
Text item=lastValue
```
