# LG ThinQ Bridge & Things

This binding was developed to integrate the LG ThinQ API with openHAB. 
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

All the configurations are pre-defined by the discovery process. But you can customize to fine-tune the device's state polling process. See the table bellow:

| Parameter           | Description                                                                                                                                                                                                | Default Value | Supported Devices             |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|-------------------------------|
| Polling when off    | Seconds to wait to the next polling when device is off. Useful to save up i/o and cpu when your device is not working. If you use only this binding to control the device, you can put higher values here. | 10            | All                           |
| Polling when on     | Seconds to wait to the next polling for device state (dashboard channels)                                                                                                                                  | 10            | All                           |
| Polling Info Period | Seconds to wait to the next polling for Device's Extra Info (energy consumption, remaining filter, etc)                                                                                                    | 60            | Air Conditioner and Heat Pump |
| Extra Info          | If enables, extra info will be fetched in the polling process even when the device is powered off. It's not so common, since extra info are normally changed only when the device is running.              | Off           | Air Conditioner and Heat Pump |

## Channels

### Air Conditioner

LG ThinQ Air Conditioners supports the following channels (for some models, some channels couldn't be available):

#### Dashboard Channels

| channel #           | channel            | type               | description                                                                                                                                               |
|---------------------|--------------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| target-temperature  | Target Temperature | Number:Temperature | Defines the desired target temperature for the device                                                                                                     |
| current-temperature | Temperature        | Number:Temperature | Read-Only channel that indicates the current temperature informed by the device                                                                           |
| fan-speed           | Fan Speed          | Number             | This channel let you choose the current label value for the fan speed (Low, Medium, High, Auto, etc.). These values are pre-configured in discovery time. |
| op-mode             | Operation Mode     | Number (Labeled)   | Defines device's operation mode (Fan, Cool, Dry, etc). These values are pre-configured at discovery time.                                                 |
| power               | Power              | Switch             | Define the device's current power state.                                                                                                                  |
| cool-jet            | Cool Jet           | Switch             | Switch Cool Jet ON/OFF                                                                                                                                    |
| auto-dry            | Auto Dry           | Switch             | Switch Auto Dry ON/OFF                                                                                                                                    |
| energy-saving       | Energy Saving      | Switch             | Switch Energy Saving ON/OFF                                                                                                                               |
| fan-step-up-down    | Fan VDir           | Number             | Define the fan vertical direction's mode (Off, Upper, Circular, Up, Middle Up, etc)                                                                       |
| fan-step-left-right | Fan HDir           | Number             | Define the fan horizontal direction's mode (Off, Lefter, Left, Circular, Right, etc)                                                                      |

#### More Information Channel

| channel #            | channel                        | type                 | description                                                                  |
|----------------------|--------------------------------|----------------------|------------------------------------------------------------------------------|
| extra-info-collector | Enable Extended Info Collector | Switch               | Enable/Disable the extra information collector to update the bellow channels |
| current-power        | Current Power                  | Number:Energy        | The current power consumption in Kw/h                                        |
| remaining-filter     | Remaining Filter               | Number:Dimensionless | Percentage of the remaining filter                                           |

### Heat Pump

LG ThinQ Heat Pump supports the following channels

#### Dashboard Channels

| channel #           | channel             | type               | description                                                                                               |
|---------------------|---------------------|--------------------|-----------------------------------------------------------------------------------------------------------|
| target-temperature  | Target Temperature  | Number:Temperature | Defines the desired target temperature for the device                                                     |
| min-temperature     | Minimum Temperature | Number:Temperature | Minimum temperature for the current operation mode                                                        |
| max-temperature     | Maximum Temperature | Number:Temperature | Maximum temperature for the current operation mode                                                        |
| current-temperature | Temperature         | Number:Temperature | Read-Only channel that indicates the current temperature informed by the device                           |
| op-mode             | Operation Mode      | Number (Labeled)   | Defines device's operation mode (Fan, Cool, Dry, etc). These values are pre-configured at discovery time. |
| power               | Power               | Switch             | Define the device's current power state.                                                                  |
| air-water-switch    | Air/Water Switch    | Switch             | Switch the heat pump operation between Air or Water                                                       |

#### More Information Channel

| channel #            | channel                        | type                 | description                                                                  |
|----------------------|--------------------------------|----------------------|------------------------------------------------------------------------------|
| extra-info-collector | Enable Extended Info Collector | Switch               | Enable/Disable the extra information collector to update the bellow channels |
| current-power        | Current Power                  | Number:Energy        | The current power consumption in Kw/h                                        |

### Washer Machine

LG ThinQ Washer Machine supports the following channels

#### Dashboard Channels

| channel #         | channel           | type   | description                                                            |
|-------------------|-------------------|--------|------------------------------------------------------------------------|
| state             | Washer State      | String | General State of the Washer                                            |
| process-state     | Process State     | String | States of the running cycle                                            |
| course            | Course            | String | Course set up to work                                                  |
| temperature-level | Temperature Level | String | Temperature level supported by the Washer (Cold, 20, 30, 40, 50, etc.) |
| door-lock         | Door Lock         | Switch | Display if the Door is Locked.                                         | 
| rinse             | Rinse             | String | The Rinse set program                                                  |
| spin              | Spin              | String | The Spin set option                                                    |
| delay-time        | Delay Time        | String | Delay time programmed to start the cycle                               | 
| remain-time       | Remaining Time    | String | Remaining time to finish the course                                    | 
| stand-by          | Stand By Mode     | Switch | If the Washer is in stand-by-mode                                      |
| remote-start-flag | Remote Start      | Switch | If the Washer is in remote start mode waiting to be remotely started   |

#### Remote Start Option

This Channel Group is only available if the Washer is configured to Remote Start

| channel #            | channel           | type               | description                                                                                             |
|----------------------|-------------------|--------------------|---------------------------------------------------------------------------------------------------------|
| rs-start-stop        | Remote Start/Stop | Switch             | Switch to control if you want to start/stop the cycle remotely                                          |
| rs-course            | Course to Run     | String (Selection) | The pre-programmed course (or default) is shown. You can change-it if you want before remote start      |
| rs-temperature-level | Temperature Level | String (Selection) | The pre-programmed temperature (or default) is shown. You can change-it if you want before remote start |
| rs-spin              | Spin              | String             | The pre-programmed spin (or default) is shown. You can change-it if you want before remote start        |
| rs-rinse             | Rinse             | String             | The pre-programmed rinse (or default) is shown. You can change-it if you want before remote start       |

### Dryer Machine

LG ThinQ Dryer Machine supports the following channels

#### Dashboard Channels

| channel #         | channel           | type   | description                                                            |
|-------------------|-------------------|--------|------------------------------------------------------------------------|
| state             | Dryer State       | String | General State of the Washer                                            |
| process-state     | Process State     | String | States of the running cycle                                            |
| course            | Course            | String | Course set up to work                                                  |
| temperature-level | Temperature Level | String | Temperature level supported by the Washer (Cold, 20, 30, 40, 50, etc.) |
| child-lock        | Child Lock        | Switch | Display if the Door is Locked.                                         |
| dry-level         | Dry Level Course  | String | Dry level set to work in the course                                    |
| delay-time        | Delay Time        | String | Delay time programmed to start the cycle                               | 
| remain-time       | Remaining Time    | String | Remaining time to finish the course                                    | 
| stand-by          | Stand By Mode     | Switch | If the Washer is in stand-by-mode                                      |
| remote-start-flag | Remote Start      | Switch | If the Washer is in remote start mode waiting to be remotely started   |

#### Remote Start Option

This Channel Group is only available if the Dryer is configured to Remote Start

| channel #     | channel           | type               | description                                                                                             |
|---------------|-------------------|--------------------|---------------------------------------------------------------------------------------------------------|
| rs-start-stop | Remote Start/Stop | Switch             | Switch to control if you want to start/stop the cycle remotely                                          |
| rs-course     | Course to Run     | String (Selection) | The pre-programmed course (or default) is shown. You can change-it if you want before remote start      |

### Dryer/Washer Tower

LG ThinQ Dryer/Washer is recognized as 2 different things: Dryer & Washer machines. Thus, for this device, follow the sessions for Dryer Machine and Washer Machine

### Refrigerator

LG ThinQ Refrigerator supports the following channels

#### Dashboard Channels

| channel #            | channel                       | type               | description                                                                    |
|----------------------|-------------------------------|--------------------|--------------------------------------------------------------------------------|
| some-door-open       | Door Open                     | Contact            | Advice if the door is opened                                                   |
| freezer-temperature  | Freezer Set Point Temperature | Number:Temperature | Temperature level chosen. This channel supports commands to change temperature |
| fridge-temperature   | Fridge Set Point Temperature  | Number:Temperature | Temperature level chosen. This channel supports commands to change temperature |
| temp-unit            | Temp. Unit                    | String             | Temperature Unit (°C/F). Supports command to change the unit                   |
| fr-express-mode      | Express Freeze                | Switch             | Channel to change the express freeze function (ON/OFF/Rapid)                   |
| fr-express-cool-mode | Express Cool                  | Switch             | Channel to switch ON/OFF express cool function                                 |
| fr-eco-friendly-mode | Vacation                      | Switch             | Channel to switch ON/OFF Vacation function (unit will work in eco mode)        | 

#### More Information

This Channel Group is reports useful information data for the device:

| channel #           | channel          | type      | description                                                |
|---------------------|------------------|-----------|------------------------------------------------------------|
| fr-fresh-air-filter | Fresh Air Filter | String    | Shows the Fresh Air filter status (OFF/AUTO/POWER/REPLACE) |
| fr-water-filter     | Water Filter     | String    | Shows the filter's used months                             |

OBS: some versions of this device can not support all the channels, depending on the model's capabilities.

