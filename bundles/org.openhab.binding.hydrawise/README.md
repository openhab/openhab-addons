# Hydrawise Binding

![API Key](doc/logo.png)

The Hydrawise binding allows monitoring and control of [Hunter Industries's](https://www.hunterindustries.com/) line of [Hydrawise](https://www.hydrawise.com) internet connected irrigation systems.

## Supported Things

### Account Bridge Thing

The Account Bridge Thing type represents the user's account on the Hydrawise cloud service. The bridge can have one or more child [Controllers](#controller-thing) linked.

An account must be manually added and configured.

### Controller Thing

Controller Things are automatically discovered once an [Account Bridge](#account-bridge-thing) has be properly configured.

The Controller Thing type is the primary way most users will control and monitor their irrigation system.
This allows full control over zones, sensors and weather forecasts.  
Changes made through this Thing type will be reflected in the Hydrawise mobile and web applications as well as in their reporting modules.

Controller Things require a parent [Account Bridge](#account-bridge-thing)

#### Controller Thing Supported Channel Groups

| channel group ID                              |
|-----------------------------------------------|
| [Controller](#controller-thing-1) |
| [Zones](#zone-channel-group)                  |
| [All Zones](#all-zones-channel-group)         |
| [Sensor](#sensor-channel-group)               |
| [Forecast](#forecast-channel-group)             |

### Local Thing

The Local Thing type uses an undocumented API that allows direct HTTP access to an irrigation controller on the user's network.  
This provides a subset of features compared to the Cloud Thing type limited to basic zone control.  
Controlling zones through the local API will not be reported back to the cloud service or the Hydrawise mobile/web applications, and reporting functionality will not reflect the locally controlled state.

Local control may not be available on later Hydrawise controller firmware versions.

Use Cases

- The Local thing can be useful when testing zones, as there is no delay when starting/stopping zones as compared to the cloud API which can take anywhere between 5-15 seconds.  
- This is also useful if you wish to not use the cloud scheduling  at all and use openHAB as the irrigation scheduling system.

#### Local Thing Supported Channel Groups

| channel group ID                      |
|---------------------------------------|
| [Zones](#zone-channel-group)          |
| [All Zones](#all-zones-channel-group) |

## Thing Configuration

### Account Thing

| Configuration Name | type    | required | Comments                                                                                                                  |
|--------------------|---------|----------|---------------------------------------------------------------------------------------------------------------------------|
| userName           | String  | False    | The Hydrawise account user name                                                                                           |
| password           | String  | False    | The Hydrawise account password                                                                                            |
| savePassword       | Boolean | False    | By default the password will be not be persisted after the first login attempt unless this is true, defaults to false     |
| refresh            | Integer | False    | Defaults to a 60 second polling rate, more frequent polling may cause the service to deny requests                        |
| refreshToken       | Boolean | False    | An oAuth refresh token, this will be automatically configured after the first login and updated as the token is refreshed |

### Controller Thing

| Configuration Name | type    | required | Comments             |
|--------------------|---------|----------|----------------------|
| controllerId       | Integer | True     | ID of the controller |

### Local Thing

| Configuration Name | type    | required | Comments                                                                                                        |
|--------------------|---------|----------|-----------------------------------------------------------------------------------------------------------------|
| host               | String  | True     | IP or host name of the controller on your network                                                               |
| username           | String  | True     | User name (usually admin) set on the touch panel of the controller                                              |
| password           | String  | True     | Password set on the touch panel of the controller.  This can be found under the setting menu on the controller. |
| refresh            | Integer | True     | Defaults to a 30 seconds polling rate                                                                           |

## Channels

### Channel Groups

#### System Channel Group

| channel group ID | Description                     |
|------------------|---------------------------------|
| system           | System status of the controller |

#### Zone Channel Group

Up to 36 total zones are supported per Local or Cloud thing

| channel group ID | Description               |
|------------------|---------------------------|
| zone1            | Zone 1 channel group      |
| zone2            | Zone 1 channel group      |
| ...              | Zone 3 - 35 channel group |
| zone36           | Zone 36 channel group     |

#### Sensor Channel Group

Up to 4 total sensors are supported per Cloud Thing

| channel group ID | Description            |
|------------------|------------------------|
| sensor1          | Sensor 1 channel group |
| sensor2          | Sensor 2 channel group |
| sensor3          | Sensor 3 channel group |
| sensor4          | Sensor 4 channel group |

#### Forecast Channel Group

Up to 3 total weather forecasts are supported per Cloud Thing

| channel group ID | Description     |
|------------------|-----------------|
| forecast1        | Todays Forecast |
| forecast2        | Day 2 Forecast  |
| forecast3        | Day 3 Forecast  |

#### All Zones Channel Group

A single all zone group are supported per Cloud or Local Thing

| channel group ID | Description            |
|------------------|------------------------|
| allzones         | commands for all zones |

### Channels

Channels uses across zones, sensors and forecasts

| channel ID                 | type               | Groups         | description                                   | Read Write |
|----------------------------|--------------------|----------------|-----------------------------------------------|------------|
| name                       | String             | zone, sensor   | Descriptive name                              | R          |
| icon                       | String             | zone           | Icon URL                                      | R          |
| type                       | Number             | zone           | Zone type                                     | R          |
| run                        | Switch             | zone, allzones | Run/Start zone                                | RW         |
| runcustom                  | Number:Time        | zone, allzones | Run zone for custom length                    | W          |
| suspend                    | Switch             | zone, allzones | Suspend zone                                  | RW         |
| suspenduntil               | DateTime           | zone, allzones | Suspend zone unitl specified date             | RW         |
| nextrun                    | DateTime           | zone           | Next date and time this zone will run         | R          |
| timeleft                   | Number:Time        | zone           | Amount of time left for the running zone      | R          |
| input                      | Number             | sensor         | Sensor input type                             | R          |
| timer                      | Number             | sensor         | Sensor timer                                  | R          |
| offtimer                   | Number:Time        | sensor         | Sensor off timer                              | R          |
| offlevel                   | Number             | sensor         | Sensor off level                              | R          |
| active                     | Switch             | sensor         | Is sensor active / triggered                  | R          |
| temperaturehigh            | Number:Temperature | forecast       | Daily high temperature                        | R          |
| temperaturelow             | Number:Temperature | forecast       | Daily low temperature                         | R          |
| conditions                 | String             | forecast       | Daily conditions description                  | R          |
| day                        | DateTime           | forecast       | Day of week of forecast (Mon-Sun)             | R          |
| humidity                   | Number             | forecast       | Daily humidity percentage                     | R          |
| wind                       | Number:Speed       | forecast       | Daily wind speed                              | R          |
| evapotranspiration         | Number             | forecast       | Daily evapotranspiration amount               | R          |
| precipitation              | Number             | forecast       | Daily precipitation amount                    | R          |
| probabilityofprecipitation | Number             | forecast       | Daily probability of precipitation percentage | R          |

## Full Example

```java
Group Sprinkler             "Sprinkler"
Group SprinklerController   "Controller"    (Sprinkler)
Group SprinklerZones        "Zones"         (Sprinkler)
Group SprinklerSensors      "Sensors"       (Sprinkler)
Group SprinkerForecast      "Forecast"      (Sprinkler)

String SprinkerControllerStatus "Status [%s]" (SprinklerController) {channel="hydrawise:controller:myaccount:123456:controller#status"}
Number SprinkerControllerLastContact "Last Contact [%d]" (SprinklerController) {channel="hydrawise:controller:myaccount:123456:controller#lastcontact"}

Switch SprinklerSensor1 "Sprinler Sensor" (SprinklerSensors) {channel="hydrawise:controller:myaccount:123456:sensor1#active"}

Group SprinkerForecastDay1 "Todays Forecast" (SprinkerForecast)
Number:Temperature SprinkerForecastDay1HiTemp "High Temp [%d]" (SprinkerForecastDay1) {channel="hydrawise:controller:myaccount:123456:forecast1#temperaturehigh"}
Number:Temperature SprinkerForecastDay1LowTemp "Low Temp [%d]" (SprinkerForecastDay1) {channel="hydrawise:controller:myaccount:123456:forecast1#temperaturelow"}
String SprinkerForecastDay1Conditions "Conditions [%s]" (SprinkerForecastDay1) {channel="hydrawise:controller:myaccount:123456:forecast1#conditions"}
String SprinkerForecastDay1Day "Day [%s]" (SprinkerForecastDay1) {channel="hydrawise:controller:myaccount:123456:forecast1#day"}
Number SprinkerForecastDay1Humidity "Humidity [%d%%]" (SprinkerForecastDay1) {channel="hydrawise:controller:myaccount:123456:forecast1#humidity"}
Number:Speed SprinkerForecastDay1Wind "Wind [%s]" (SprinkerForecastDay1) {channel="hydrawise:controller:myaccount:123456:forecast1#wind"}

Group  SprinklerZone1 "1 Front Office Yard" (SprinklerZones)
String SprinklerZone1Name "1 Front Office Yard name" (SprinklerZone1) {channel="hydrawise:controller:myaccount:123456:zone1#name"}
Switch SprinklerZone1Run "1 Front Office Yard Run" (SprinklerZone1) {channel="hydrawise:controller:myaccount:123456:zone1#run"}
Switch SprinklerZone1RunLocal "1 Front Office Yard Run (local)" (SprinklerZone1) {channel="hydrawise:local:home:zone1#run"}
Number SprinklerZone1RunCustom "1 Front Office Yard Run Custom" (SprinklerZone1) {channel="hydrawise:controller:myaccount:123456:zone1#runcustom"}
DateTime SprinklerZone1StartTime "1 Front Office Yard Start Time [%s]" (SprinklerZone1) {channel="hydrawise:controller:myaccount:123456:zone1#nextruntime"}
Number SprinklerZone1TimeLeft "1 Front Office Yard Time Left" (SprinklerZone1) {channel="hydrawise:controller:myaccount:123456:zone1#timeleft"}
String SprinklerZone1Icon "1 Front Office Yard Icon" (SprinklerZone1) {channel="hydrawise:controller:myaccount:123456:zone1#icon"}

Group  SprinklerZone2 "2 Back Circle Lawn" (SprinklerZones)
String SprinklerZone2Name "2 Back Circle Lawn name" (SprinklerZone2) {channel="hydrawise:controller:myaccount:123456:zone2#name"}
Switch SprinklerZone2Run "2 Back Circle Lawn Run" (SprinklerZone2) {channel="hydrawise:controller:myaccount:123456:zone2#run"}
Switch SprinklerZone2RunLocal "2 Back Circle Lawn Run (local)" (SprinklerZone2) {channel="hydrawise:local:home:zone2#run"}
Number SprinklerZone2RunCustom "2 Back Circle Lawn Run Custom" (SprinklerZone2) {channel="hydrawise:controller:myaccount:123456:zone2#runcustom"}
DateTime SprinklerZone2StartTime "2 Back Circle Lawn Start Time" (SprinklerZone2) {channel="hydrawise:controller:myaccount:123456:zone2#nextruntime"}
Number SprinklerZone2TimeLeft "2 Back Circle Lawn Time Left" (SprinklerZone2) {channel="hydrawise:controller:myaccount:123456:zone2#timeleft"}
String SprinklerZone2Icon "2 Back Circle Lawn Icon" (SprinklerZone2) {channel="hydrawise:controller:myaccount:123456:zone2#icon"}

Group  SprinklerZone3 "3 Left of Drive Lawn" (SprinklerZones)
String SprinklerZone3Name "3 Left of Drive Lawn name" (SprinklerZone3) {channel="hydrawise:controller:myaccount:123456:zone3#name"}
Switch SprinklerZone3Run "3 Left of Drive Lawn Run" (SprinklerZone3) {channel="hydrawise:controller:myaccount:123456:zone3#run"}
Switch SprinklerZone3RunLocal "3 Left of Drive Lawn Run (local)" (SprinklerZone3) {channel="hydrawise:local:home:zone3#run"}
Number SprinklerZone3RunCustom "3 Left of Drive Lawn Run Custom" (SprinklerZone3) {channel="hydrawise:controller:myaccount:123456:zone3#runcustom"}
DateTime SprinklerZone3StartTime "3 Left of Drive Lawn Start Time" (SprinklerZone3) {channel="hydrawise:controller:myaccount:123456:zone3#nextruntime"}
Number SprinklerZone3TimeLeft "3 Left of Drive Lawn Time Left" (SprinklerZone3) {channel="hydrawise:controller:myaccount:123456:zone3#timeleft"}
String SprinklerZone3Icon "3 Left of Drive Lawn Icon" (SprinklerZone3) {channel="hydrawise:controller:myaccount:123456:zone3#icon"}
```
