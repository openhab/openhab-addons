# LG ThinQ Bridge & Things

This binding was developed to integrate de OpenHab framework to LG ThinQ API. 
The ThinQ Bridge is necessary to work as a hub/bridge to discovery and first configure the LG ThinQ devices related with the LG's user account.
Then, the first thing is to create the LG ThinQ Bridge and then, it will discover all Things you have related in your LG Account.

## Supported Things
This binding support several devices from the LG ThinQ Devices V1 & V2 line. Se the table bellow:

| Device Name     | Versions | Special Functions            | Commands                                        | Obs                                                                                                                                                              |
|-----------------|----------|------------------------------|-------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Air Conditioner | V1 & V2  | Filter and Energy Monitoring | All features in LG App, except Wind Direction   |                                                                                                                                                                  |
| Dish Washer     | V2       | None                         | None                                            | Provide only some channels to follow the cycle                                                                                                                   |
| Dryer Tower     | V1 & V2  | None                         | All features in LG App (including remote start) | LG has a WasherDryer Tower that is 2 in one device.<br/> When this device is discovered by this binding, it's recognized as 2 separated devices Washer and Dryer |
| Washer Tower    | V1 & V2  | None                         | All features in LG App (including remote start) | LG has a WasherDryer Tower that is 2 in one device.<br/> When this device is discovered by this binding, it's recognized as 2 separated devices Washer and Dryer |
| Washer Machine  | V1 & V2  | None                         | All features in LG App (including remote start) |                                                                                                                                                                  |
| Dryer Machine   | V1 & V2  | None                         | All features in LG App (including remote start) |                                                                                                                                                                  |
| Refrigerator    | V1 & V2  | None                         | All features in LG App                          |                                                                                                                                                                  |
| Heat Pump       | V1 & V2  | None                         | All features in LG App                          |                                                                                                                                                                  |


## Discovery

This binding bas auto-discovering for the supported devices  

## Binding Configuration

![LG Bridge Configuration](doc/bridge-configuration.jpg)

The binding is represented by a bridge (LG GatewayBridge) and you must configure the following parameters:

| Bridge Parameter           | Description                                                                                                                                                                                        | Obs                                                                                                             |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| User Language              | More frequent languages used                                                                                                                                                                       | If you choose other, you can fill Manual user language (only if your language was not pre-defined in this combo |
| User Country               | More frequent countries used                                                                                                                                                                       | If you choose other, you can fill Manual user country (only if your country was not pre-defined in this combo   |
| Manual User Language       | The acronym for the language (PT, EN, IT, etc)                                                                                                                                                     |                                                                                                                 |
| Manual User Country        | The acronym for the country (UK, US, BR, etc)                                                                                                                                                      |                                                                                                                 |
| LG User name               | The LG user's account (normally an email)                                                                                                                                                          |                                                                                                                 |
| LG Password                | The LG user's password                                                                                                                                                                             |                                                                                                                 |
| LG Password                | The LG user's password                                                                                                                                                                             |                                                                                                                 |
| Pooling Discovery Interval | It the time (in seconds) that the bridge wait to try to fetch de devices registered to the user's account and, if find some new device, will show available to link. Please, choose some long time | greater than 300 seconds                                                                                        |


## Thing Configuration

All the configurations are pre-defined by the discovery process. But you can customize some parameters to fine-tune the device's state polling process:
Polling period in seconds when the device is off: is the period that the binding wait until hit the LG API to get the latest device's state when the device is actually turned off
Polling period in seconds when the device is oon: is the period that the binding wait until hit the LG API to get the latest device's state when the device is actually turned on

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

**Important:** this binding will always interact with the LG API server to get information about the device. This is the Smart ThinQ way to work, there is no other way (like direct access) to the devices. Hence, some side effects will happen in the following situations:
1. **Internet Link** - if you OpenHab server doesn't have a good internet connection this binding will not work properly! In the same way, if the internet link goes down, your Things and Bridge going to be Offline as well, and you won't be able to control the devices though OpenHab until the link comes back.
2. **LG ThinQ App** - if you've already used the LG ThinQ App to control your devices and hold it constantly activated in your mobile phone, you may experience some instability because the App (and Binding) will try to lock the device in LG ThinQ API Server to get it's current state. In the app, you may see some information in the device informing that "The device is being used by other" (something like this) and in the OpenHab, the thing can go Offline for a while.
3. **Pooling time** - both Bridge and Thing use pooling strategy to get the current state information about the registered devices. Note that the Thing pooling time is internal and can't be changed (please, don't change in the source code) and the Bridge can be changed for something greater than 300 seconds, and it's recommended long pooling periods for the Bridge because the discovery process fetch a lot of information from the LG API Server, depending on the number of devices you have registered in your account. 
About this last point, it's important to know that LG API is not Open & Public, i.e, only LG Official Partners with some agreement have access to their support and documentations. This binding was a hard (very hard actually) work to dig and reverse engineering in the LG's ThinQ API protocol. Because this, you must respect the hardcoded pool period to do not put your account in LG Blacklist.

## Be nice!
If you like the binding, why don't you support me by buying me a coffee?
It would certainly motivate me to further improve this work or to create new others cool bindings for OpenHab !

[![Buy me a coffee!](https://www.buymeacoffee.com/assets/img/custom_images/black_img.png)](https://www.buymeacoffee.com/nemerdaud)
