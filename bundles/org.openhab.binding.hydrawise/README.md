# Hydrawise Binding

![API Key](doc/logo.png)

The Hydrawise binding allows monitoring and control of [Hunter Industries's](https://www.hunterindustries.com/) line of [Hydrawise](https://www.hydrawise.com) internet connected irrigation systems.

## Supported Things

### Cloud Thing
    
The Cloud Thing type is the primary way most users will control and monitor their irrigation system.  
This allows full control over zones, sensors and weather forecasts.  
Changes made through this Thing type will be reflected in the Hydrawise mobile and web applications as well as in their reporting modules. 

#### Cloud Thing Supported Channel Groups

| channel group ID                      |
|---------------------------------------|
| [Zones](#Zone-Channel-Group)          |
| [All Zones](#All-Zones-Channel-Group) |
| [Sensor](#Sensor-Channel-Group)       |
| [Forecast](#Sensor-Channel-Group)     |
     
### Local Thing

The Local Thing type uses an undocumented API that allows direct HTTP access to a irrigation controller on the user's network.  
This provides a subset of features compared to the Cloud Thing type limited to basic zone control.  
Controlling zones through the local API will not be reported back to the cloud service or the Hydrawise mobile/web applications, and reporting functionality will not reflect the locally controlled state. 

Use Cases    

* The Local thing can be useful when testing zones, as there is no delay when starting/stopping zones as compared to the cloud API which can take anywhere between 5-15 seconds.  
* This is also useful if you wish to not use the cloud scheduling  at all and use openHAB as the irrigation scheduling system.

#### Local Thing Supported Channel Groups

| channel group ID                      |
|---------------------------------------|
| [Zones](#Zone-Channel-Group)          |
| [All Zones](#All-Zones-Channel-Group) |

## Thing Configuration

### Cloud Thing

| Configuration Name | type    | required | Comments                                                                           |
|--------------------|---------|----------|------------------------------------------------------------------------------------|
| apiKey             | String  | True     |                                                                                    |
| refresh            | Integer | True     | Defaults to a 30 seconds polling rate                                              |
| controllerId       | Integer | False    | Optional id of the controller if you have more then one registered to your account |

To obtain your API key, log into your [Hydrawsie Account](https://app.hydrawise.com/config/login) and click on your account icon, then account details:

![Account](doc/settings.png)

Then copy the API key shown here:

![API Key](doc/apikey.png)

### Local Thing

| Configuration Name | type    | required | Comments                                                                                                        |
|--------------------|---------|----------|-----------------------------------------------------------------------------------------------------------------|
| host               | String  | True     | IP or host name of the controller on your network                                                               |
| username           | String  | True     | User name (usually admin) set on the touch panel of the controller                                               |
| password           | String  | True     | Password set on the touch panel of the controller.  This can be found under the setting menu on the controller. |
| refresh            | Integer | True     | Defaults to a 30 seconds polling rate                                                                           |

## Channels

### Channel Groups

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

Up to 4 total weather forecasts are supported per Cloud Thing

| channel group ID | Description     |
|------------------|-----------------|
| forecast1        | Todays Forecast |
| forecast2        | Day 2 Forecast  |
| forecast3        | Day 3 Forecast  |
| forecast4        | Day 4 Forecast  |

#### All Zones Channel Group

A single all zone group are supported per Cloud or Local Thing

| channel group ID | Description            |
|------------------|------------------------|
| allzones         | commands for all zones |


### Channels

| channel ID      | type               | Groups         | description                                 | Read Write |
|-----------------|--------------------|----------------|---------------------------------------------|------------|
| name            | String             | zone, sensor   | Descriptive name                            | R          |
| icon            | String             | zone           | Icon URL                                    | R          |
| time            | Number             | zone           | Zone start time in seconds                  | R          |
| type            | Number             | zone           | Zone type                                   | R          |
| runcustom       | Number             | zone, allzones | Run zone for custom number of seconds       | W          |
| run             | Switch             | zone, allzones | Run/Start zone                              | RW         |
| nextrun         | DateTime           | zone           | Next date and time this zone will run       | R          |
| timeleft        | Number             | zone           | Amount of seconds left for the running zone | R          |
| input           | Number             | sensor         | Sensor input type                           | R          |
| mode            | Number             | sensor         | Sensor mode                                 | R          |
| timer           | Number             | sensor         | Sensor timer                                | R          |
| offtimer        | Number             | sensor         | Sensor off time                             | R          |
| offlevel        | Number             | sensor         | Sensor off level                            | R          |
| active          | Switch             | sensor         | Is sensor active / triggered                | R          |
| temperaturehigh | Number:Temperature | forecast       | Daily high temperature                      | R          |
| temperaturelow  | Number:Temperature | forecast       | Daily low temperature                       | R          |
| conditions      | String             | forecast       | Daily conditions description                | R          |
| day             | String             | forecast       | Day of week of forecast (Mon-Sun)           | R          |
| humidity        | Number             | forecast       | Daily humidity percentage                   | R          |
| wind            | Number:Speed       | forecast       | Daily wind speed                            | R          |

## Full Example

```
Group SprinklerZones
Group  SprinklerZone1 "1 Front Office Yard" (SprinklerZones)
String SprinklerZone1Name "1 Front Office Yard name" (SprinklerZone1) {channel="hydrawise:cloud:home:zone1#name"}
Switch SprinklerZone1Run "1 Front Office Yard Run" (SprinklerZone1) {channel="hydrawise:cloud:home:zone1#run"}
Switch SprinklerZone1RunLocal "1 Front Office Yard Run (local)" (SprinklerZone1) {channel="hydrawise:local:home:zone1#run"}
Number SprinklerZone1RunCustom "1 Front Office Yard Run Custom" (SprinklerZone1) {channel="hydrawise:cloud:home:zone1#runcustom"}
DateTime SprinklerZone1StartTime "1 Front Office Yard Start Time [%s]" (SprinklerZone1) {channel="hydrawise:cloud:home:zone1#nextruntime"}
Number SprinklerZone1TimeLeft "1 Front Office Yard Time Left" (SprinklerZone1) {channel="hydrawise:cloud:home:zone1#timeleft"}
String SprinklerZone1Icon "1 Front Office Yard Icon" (SprinklerZone1) {channel="hydrawise:cloud:home:zone1#icon"}

Group  SprinklerZone2 "2 Back Circle Lawn" (SprinklerZones)
String SprinklerZone2Name "2 Back Circle Lawn name" (SprinklerZone2) {channel="hydrawise:cloud:home:zone2#name"}
Switch SprinklerZone2Run "2 Back Circle Lawn Run" (SprinklerZone2) {channel="hydrawise:cloud:home:zone2#run"}
Switch SprinklerZone2RunLocal "2 Back Circle Lawn Run (local)" (SprinklerZone2) {channel="hydrawise:local:home:zone2#run"}
Number SprinklerZone2RunCustom "2 Back Circle Lawn Run Custom" (SprinklerZone2) {channel="hydrawise:cloud:home:zone2#runcustom"}
DateTime SprinklerZone2StartTime "2 Back Circle Lawn Start Time" (SprinklerZone2) {channel="hydrawise:cloud:home:zone2#nextruntime"}
Number SprinklerZone2TimeLeft "2 Back Circle Lawn Time Left" (SprinklerZone2) {channel="hydrawise:cloud:home:zone2#timeleft"}
String SprinklerZone2Icon "2 Back Circle Lawn Icon" (SprinklerZone2) {channel="hydrawise:cloud:home:zone2#icon"}

Group  SprinklerZone3 "3 Left of Drive Lawn" (SprinklerZones)
String SprinklerZone3Name "3 Left of Drive Lawn name" (SprinklerZone3) {channel="hydrawise:cloud:home:zone3#name"}
Switch SprinklerZone3Run "3 Left of Drive Lawn Run" (SprinklerZone3) {channel="hydrawise:cloud:home:zone3#run"}
Switch SprinklerZone3RunLocal "3 Left of Drive Lawn Run (local)" (SprinklerZone3) {channel="hydrawise:local:home:zone3#run"}
Number SprinklerZone3RunCustom "3 Left of Drive Lawn Run Custom" (SprinklerZone3) {channel="hydrawise:cloud:home:zone3#runcustom"}
DateTime SprinklerZone3StartTime "3 Left of Drive Lawn Start Time" (SprinklerZone3) {channel="hydrawise:cloud:home:zone3#nextruntime"}
Number SprinklerZone3TimeLeft "3 Left of Drive Lawn Time Left" (SprinklerZone3) {channel="hydrawise:cloud:home:zone3#timeleft"}
String SprinklerZone3Icon "3 Left of Drive Lawn Icon" (SprinklerZone3) {channel="hydrawise:cloud:home:zone3#icon"}
```

