# MCD Binding

This binding allows you to send sensor events from your openHAB environment to the cloud application Managing Care Digital (MCD) by [C&S Computer und Software GmbH](https://www.managingcare.de/). 

MCD is the platform for inpatient and outpatient nursing services. 
Our REST API allows you to send a variety of sensor events to the system and thus being able to connect your Ambient Assisted Living (AAL) or smart home environment to the documentation software of your nursing service. 

Please note that a valid account is needed to access MCD and the Sensor API.


## Supported Things

There are two supported things: **MCD Bridge** and **MCD Sensor Thing**. 


## Discovery

Discovery is not supported. 


## Thing Configuration

This section shows the configuration parameters of both supported things.

### MCD Bridge

The MCD Bridge (`mcdBridge`) needs to be configured with your valid C&S MCD / sync API credentials. 

| parameter | description                        |
|-----------|------------------------------------|
| userEmail     | Email of account                   |
| userPassword  | valid password for the given email |

### MCD Sensor Thing

Each sensor thing (`mcdSensor`) needs to be configured with the identical serial number, that is assigned to this sensor in MCD. 

| parameter      | description                        |
|----------------|------------------------------------|
| serialNumber  | serial number of the sensor in MCD |

## Channels

The `mcdSensor` thing supports the following channels.  To see the sensors' events, please visit [Managing Care Digital](https://cundsdokumentation.de/) and navigate to the dashboard. 

| channel     | type   | description                                   |
|-------------|--------|-----------------------------------------------|
| lastEvent | String | shows the last event that was sent with date and time |
| sendEvent | String | stateless channel for sending events to the API, see list below for valid commands |

The channel `sendEvent` accepts valid Sensor Event Definitions as well as the corresponding ID. 
The following table contains all currently accepted Sensor Event Definitions that can be passed as String type commands. 
As soon as new events are added to the API, you can use their ID, even if the Definition is not yet added to this list. 
For more information about the API, you can have a look at the [C&S Sync API](https://cunds-syncapi.azurewebsites.net/ApiDocumentation).

| Valid String Type Commands |
|------------|
| BEDEXIT |
| BEDENTRY |
| FALL |
| CHANGEPOSITION |
| BATTERYSTATE |
| INACTIVITY |
| ALARM |
| OPEN |
| CLOSE |
| ON |
| OFF |
| ACTIVITY |
| CAPACITY |
| GAS |
| VITALVALUE |
| ROOMEXIT |
| ROOMENTRY |
| REMOVESENSOR |
| SITDOWN |
| STANDUP |
| INACTIVITYROOM |
| SMOKEALARM |
| HEAT |
| COLD |
| QUALITYAIR |
| ALARMAIR |
| ROOMTEMPERATURE |
| HUMIDITY |
| AIRPRESSURE |
| CO2 |
| INDEXUV |
| WEARTIME |
| FIRSTURINE |
| NEWDIAPER |
| DIAPERREMOVED |
| NOCONNECTION |
| LOWBATTERY |
| CONTROLLSENSOR |
| LYING |
| SPILLED |
| DAMAGED |
| GEOEXIT |
| GEOENTRY |
| WALKING |
| RESTING |
| TURNAROUND |
| HOMEEMERGENCY |
| TOILETFLUSH |
| DORSALPOSITION |
| ABDOMINALPOSITION |
| LYINGLEFT |
| LYINGRIGHT |
| LYINGHALFLEFT |
| LYINGHALFRIGHT |
| MOVEMENT |
| PRESENCE |
| NUMBERPERSONS |
| BRIGHTNESSZONE |


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
String sendEvent "Send Event" {channel="mcd:mcdSensor:examlpeSensor:sendEvent"}
```

demo.sitemap:

```
Text item=sendEvent
Text item=lastValue
```
