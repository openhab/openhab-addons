# LG ThinQ Bridge & Things

This binding was developed to integrate the LG ThinQ API into openHAB. 
The ThinQ Bridge is necessary to work as a hub/bridge to discovery and first configure the LG ThinQ devices related with the LG's user account.
Then, the first thing is to create the LG ThinQ Bridge and then, it will discover all Things you have related in your LG Account.

## Supported Things
This binding support several devices from the LG ThinQ Devices V1 & V2 line. Se the table bellow:

| Device ID | Device Name     | Versions | Special Functions            | Commands                                        | Obs                                                                                                                                                              |
|-----------|-----------------|----------|------------------------------|-------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 401       | Air Conditioner | V1 & V2  | Filter and Energy Monitoring | All features in LG App, except Wind Direction   |                                                                                                                                                                  |
| 204       | Dish Washer     | V2       | None                         | None                                            | Provide only some channels to follow the cycle                                                                                                                   |
| 222       | Dryer Tower     | V1 & V2  | None                         | All features in LG App (including remote start) | LG has a WasherDryer Tower that is 2 in one device.<br/> When this device is discovered by this binding, it's recognized as 2 separated devices Washer and Dryer |
| 221       | Washer Tower    | V1 & V2  | None                         | All features in LG App (including remote start) | LG has a WasherDryer Tower that is 2 in one device.<br/> When this device is discovered by this binding, it's recognized as 2 separated devices Washer and Dryer |
| 201       | Washer Machine  | V1 & V2  | None                         | All features in LG App (including remote start) |                                                                                                                                                                  |
| 202       | Dryer Machine   | V1 & V2  | None                         | All features in LG App (including remote start) |                                                                                                                                                                  |
| 101       | Refrigerator    | V1 & V2  | None                         | All features in LG App                          |                                                                                                                                                                  |
| 401HP     | Heat Pump       | V1 & V2  | None                         | All features in LG App                          |                                                                                                                                                                  |

## Bridge Thing

This binding has a Bridge responsible for the discovery and registry of LG Things. The first step to create a thing, is to firstly add the LG Thinq Bridge, that will
connect the binding to your LG Account and after, the bridge can discovery all devices registered and you are able to add it to OpenHab Things.

## Discovery

This binding bas auto-discovering for the supported devices  

## Binding Configuration

The binding is configured through a bridge (LG GatewayBridge) and you must configure the following parameters:
    
| Bridge Parameter   | Label                      | Description                                                                                                                                                                                                                 | Obs                                                                                                             |
|--------------------|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| language           | User Language              | More frequent languages used                                                                                                                                                                                                | If you choose other, you can fill Manual user language (only if your language was not pre-defined in this combo |
| country            | User Country               | More frequent countries used                                                                                                                                                                                                | If you choose other, you can fill Manual user country (only if your country was not pre-defined in this combo   |
| manualLanguage     | Manual User Language       | The acronym for the language (PT, EN, IT, etc)                                                                                                                                                                              |                                                                                                                 |
| manualCountry      | Manual User Country        | The acronym for the country (UK, US, BR, etc)                                                                                                                                                                               |                                                                                                                 |
| username           | LG User name               | The LG user's account (normally an email)                                                                                                                                                                                   |                                                                                                                 |
| password           | LG Password                | The LG user's password                                                                                                                                                                                                      |                                                                                                                 |
| poolingIntervalSec | Polling Discovery Interval | It the time (in seconds) that the bridge wait to try to fetch de devices registered to the user's account and, if find some new device, will show available to link. Please, choose some long time greater than 300 seconds |
| alternativeServer  | Alt Gateway Server         | Only used if you have some proxy to the LG API Server or for Mock Tests                                                                                                                                                     |                                                                                                                 |



## Thing Configuration

All the configurations are pre-defined by the discovery process. But you can customize some parameters to fine-tune the device's state polling process:

Polling period in seconds when the device is off: is the period that the binding wait until hit the LG API to get the latest device's state when the device is actually turned off
Polling period in seconds when the device is on: is the period that the binding wait until hit the LG API to get the latest device's state when the device is actually turned on

## Channels

### Air Conditioner
LG ThinQ Air Conditioners supports the following channels

#### Dashboard Channels

| channel            | type             | description                                                                                                                                               |
|--------------------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Target Temperature | Temperature      | Defines the desired target temperature for the device                                                                                                     |
| Temperature        | Temperature      | Read-Only channel that indicates the current temperature informed by the device                                                                           |
| Fan Speed          | Number (Labeled) | This channel let you choose the current label value for the fan speed (Low, Medium, High, Auto, etc.). These values are pre-configured in discovery time. |
| Operation Mode     | Number (Labeled) | Defines device's operation mode (Fan, Cool, Dry, etc). These values are pre-configured at discovery time.                                                 |
| Power              | Switch           | Define the device's current power state.                                                                                                                  |
| Cool Jet           | Switch           | Switch Cool Jet ON/OFF                                                                                                                                    |
| Auto Dry           | Switch           | Switch Auto Dry ON/OFF                                                                                                                                    |
| Energy Saving      | Switch           | Switch Energy Saving ON/OFF                                                                                                                               |

#### More Information Channel

| channel                        | type                 | description                                                                                               |
|--------------------------------|----------------------|-----------------------------------------------------------------------------------------------------------|
| Enable Extended Info Collector | Switch               | Enable/Disable the extra information collector to update the bellow channels                              |
| Current Power                  | Number:Energy        | The current power consumption                                                                             |
| Remaining Filter               | Number:Dimensionless | Per percentage of the filter remaining                                                                    |

### Heat Pump
LG ThinQ Heat Pump supports the following channels

#### Dashboard Channels

| channel             | type             | description                                                                                               |
|---------------------|------------------|-----------------------------------------------------------------------------------------------------------|
| Target Temperature  | Temperature      | Defines the desired target temperature for the device                                                     |
| Minimum Temperature | Temperature      | Minimum temperature for the current operation mode                                                        |
| Maximum Temperature | Temperature      | Maximum temperature for the current operation mode                                                        |
| Temperature         | Temperature      | Read-Only channel that indicates the current temperature informed by the device                           |
| Operation Mode      | Number (Labeled) | Defines device's operation mode (Fan, Cool, Dry, etc). These values are pre-configured at discovery time. |
| Power               | Switch           | Define the device's current power state.                                                                  |
| Air/Water Switch    | Switch           | Switch the heat pump operation between Air or Water                                                       |

#### More Information Channel

| channel                        | type                 | description                                                                                               |
|--------------------------------|----------------------|-----------------------------------------------------------------------------------------------------------|
| Enable Extended Info Collector | Switch               | Enable/Disable the extra information collector to update the bellow channels                              |
| Current Power                  | Number:Energy        | The current power consumption                                                                             |

### Washer Machine
LG ThinQ Washer Machine supports the following channels

#### Dashboard Channels

| channel           | type   | description                                                            |
|-------------------|--------|------------------------------------------------------------------------|
| Washer State      | String | General State of the Washer                                            |
| Process State     | String | States of the running cycle                                            |
| Course            | String | Course set up to work                                                  |
| Temperature Level | String | Temperature level supported by the Washer (Cold, 20, 30, 40, 50, etc.) |
| Door Lock         | Switch | Display if the Door is Locked.                                         | 
| Rinse             | String | The Rinse set program                                                  |
| Spin              | String | The Spin set option                                                    |
| Delay Time        | String | Delay time programmed to start the cycle                               | 
| Remaining Time    | String | Remaining time to finish the course                                    | 
| Stand By Mode     | Switch | If the Washer is in stand-by-mode                                      |
| Remote Start      | Switch | If the Washer is in remote start mode waiting to be remotely started   |

#### Remote Start Option

This Channel Group is only available if the Washer is configured to Remote Start

| channel           | type               | description                                                                                             |
|-------------------|--------------------|---------------------------------------------------------------------------------------------------------|
| Remote Start/Stop | Switch             | Switch to control if you want to start/stop the cycle remotely                                          |
| Course to Run     | String (Selection) | The pre-programmed course (or default) is shown. You can change-it if you want before remote start      |
| Temperature Level | String (Selection) | The pre-programmed temperature (or default) is shown. You can change-it if you want before remote start |
| Spin              | String             | The pre-programmed spin (or default) is shown. You can change-it if you want before remote start        |
| Rinse             | String             | The pre-programmed rinse (or default) is shown. You can change-it if you want before remote start       |

### Dryer Machine
LG ThinQ Dryer Machine supports the following channels

#### Dashboard Channels

| channel           | type   | description                                                            |
|-------------------|--------|------------------------------------------------------------------------|
| Dryer State       | String | General State of the Washer                                            |
| Process State     | String | States of the running cycle                                            |
| Course            | String | Course set up to work                                                  |
| Temperature Level | String | Temperature level supported by the Washer (Cold, 20, 30, 40, 50, etc.) |
| Chiel Lock        | Switch | Display if the Door is Locked.                                         |
| Dry Level Course  | String | Dry level set to work in the course                                    |
| Delay Time        | String | Delay time programmed to start the cycle                               | 
| Remaining Time    | String | Remaining time to finish the course                                    | 
| Stand By Mode     | Switch | If the Washer is in stand-by-mode                                      |
| Remote Start      | Switch | If the Washer is in remote start mode waiting to be remotely started   |

#### Remote Start Option

This Channel Group is only available if the Dryer is configured to Remote Start

| channel           | type               | description                                                                                             |
|-------------------|--------------------|---------------------------------------------------------------------------------------------------------|
| Remote Start/Stop | Switch             | Switch to control if you want to start/stop the cycle remotely                                          |
| Course to Run     | String (Selection) | The pre-programmed course (or default) is shown. You can change-it if you want before remote start      |

### Dryer/Washer Tower
LG ThinQ Dryer/Washer is recognized as 2 different things: Dryer & Washer machines. Thus, for this device, follow the sessions for Dryer Machine and Washer Machine

### Refrigerator
LG ThinQ Refrigerator supports the following channels

#### Dashboard Channels

| channel                       | type        | description                                                                    |
|-------------------------------|-------------|--------------------------------------------------------------------------------|
| Door Open                     | Contact     | Advice if the door is opened                                                   |
| Freezer Set Point Temperature | Temperature | Temperature level chosen. This channel supports commands to change temperature |
| Fridge Set Point Temperature  | Temperature | Temperature level chosen. This channel supports commands to change temperature |
| Temp. Unit                    | String      | Temperature Unit (°C/F). Supports command to change the unit                   |
| Express Freeze                | Switch      | Channel to change the express freeze function (ON/OFF/Rapid)                   |
| Express Cool                  | Switch      | Channel to switch ON/OFF express cool function                                 |
| Vacation                      | Switch      | Channel to switch ON/OFF Vacation function (unit will work in eco mode)        | 

#### More Information

This Channel Group is reports useful information data for the device:

| channel          | type      | description                                                |
|------------------|-----------|------------------------------------------------------------|
| Fresh Air Filter | String    | Shows the Fresh Air filter status (OFF/AUTO/POWER/REPLACE) |
| Water Filter     | String    | Shows the filter's used months                             |

OBS: some versions of this device can not support all the channels, depending on the model's capabilities.

