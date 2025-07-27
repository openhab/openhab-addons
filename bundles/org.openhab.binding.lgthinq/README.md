# LG ThinQ Binding

This binding was developed to integrate the LG ThinQ API with openHAB.

## Supported Things

This binding support several devices from the LG ThinQ Devices V1 & V2 line.
All devices require a configured Bridge.
See the table bellow:

| Thing ID            | Device Name     | Versions | Special Functions            | Commands                                        | Obs                                                                                                                                                              |
|---------------------|-----------------|----------|------------------------------|-------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| air-conditioner-401 | Air Conditioner | V1 & V2  | Filter and Energy Monitoring | All features in LG App, except Wind Direction   |                                                                                                                                                                  |
| dishwasher-204      | Dish Washer     | V2       | None                         | None                                            | Provide only some channels to follow the cycle                                                                                                                   |
| dryer-tower-222     | Dryer Tower     | V1 & V2  | None                         | All features in LG App (including remote start) | LG has a WasherDryer Tower that is 2 in one device.<br/> When this device is discovered by this binding, it's recognized as 2 separated devices Washer and Dryer |
| washer-tower-221    | Washer Tower    | V1 & V2  | None                         | All features in LG App (including remote start) | LG has a WasherDryer Tower that is 2 in one device.<br/> When this device is discovered by this binding, it's recognized as 2 separated devices Washer and Dryer |
| washer-201          | Washer Machine  | V1 & V2  | None                         | All features in LG App (including remote start) |                                                                                                                                                                  |
| dryer-202           | Dryer Machine   | V1 & V2  | None                         | All features in LG App (including remote start) |                                                                                                                                                                  |
| fridge-101          | Refrigerator    | V1 & V2  | None                         | All features in LG App                          |                                                                                                                                                                  |
| heatpump-401HP      | Heat Pump       | V1 & V2  | None                         | All features in LG App                          |                                                                                                                                                                  |

## `bridge` Thing

This binding has a Bridge responsible for discovering and registering LG Things.
Thus, adding the Bridge (LGThinq GW Bridge) is the first step in configuring this Binding.
The following parameters are available to configure the Bridge and to link to your LG Account as well:

| Bridge Parameter   | Label                      | Description                                                                                                                                                                                                                 | Obs                                                                                                             |
|--------------------|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| language           | User Language              | More frequent languages used                                                                                                                                                                                                | If you choose other, you can fill Manual user language (only if your language was not pre-defined in this combo |
| country            | User Country               | More frequent countries used                                                                                                                                                                                                | If you choose other, you can fill Manual user country (only if your country was not pre-defined in this combo   |
| manualLanguage     | Manual User Language       | The acronym for the language (PT, EN, IT, etc)                                                                                                                                                                              |                                                                                                                 |
| manualCountry      | Manual User Country        | The acronym for the country (UK, US, BR, etc)                                                                                                                                                                               |                                                                                                                 |
| username           | LG User name               | The LG user's account (normally an email)                                                                                                                                                                                   |                                                                                                                 |
| password           | LG Password                | The LG user's password                                                                                                                                                                                                      |                                                                                                                 |
| pollingIntervalSec | Polling Discovery Interval | It is the time (in seconds) that the bridge waits to try to fetch the devices registered to the user's account and, if it finds some new device, will show it as available to link. Please, choose a long time greater than 300 seconds. |                                                                                                                 |
| alternativeServer  | Alt Gateway Server         | Only used if you have some proxy to the LG API Server or for Mock Tests                                                                                                                                                     |                                                                                                                 |

## Discovery

This Binding has auto-discovery for the supported LG Thinq devices.
Once LG Thinq Bridge has been added, LG Thinq devices linked to your account will be automatically discovered and displayed in the openHAB Inbox.

## Thing Configuration

All the configurations are pre-defined by the discovery process.
But you can customize to fine-tune the device's state polling process.
See the table below:

| Parameter                     | Description                                                                                                                                                                                                | Default Value | Supported Devices             |
|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|-------------------------------|
| pollingPeriodPowerOffSeconds  | Seconds to wait to the next polling when device is off. Useful to save up i/o and cpu when your device is not working. If you use only this binding to control the device, you can put higher values here. | 10            | All                           |
| pollingPeriodPowerOnSeconds   | Seconds to wait to the next polling for device state (dashboard channels)                                                                                                                                  | 10            | All                           |
| pollingExtraInfoPeriodSeconds | Seconds to wait to the next polling for Device's Extra Info (energy consumption, remaining filter, etc)                                                                                                    | 60            | Air Conditioner and Heat Pump |
| pollExtraInfoOnPowerOff       | If enables, extra info will be fetched in the polling process even when the device is powered off. It's not so common, since extra info are normally changed only when the device is running.              | Off           | Air Conditioner and Heat Pump |

## Channels

### Air Conditioner

Most, but not all, LG ThinQ Air Conditioners support the following channels:

#### Dashboard Channels

| channel #           | channel            | type               | description                                                                                                                                               |
|---------------------|--------------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| target-temperature  | Target Temperature | Number:Temperature | Defines the desired target temperature for the device                                                                                                     |
| current-temperature | Temperature        | Number:Temperature | Read-Only channel that indicates the current temperature informed by the device                                                                           |
| fan-speed           | Fan Speed          | Number             | This channel let you choose the current label value for the fan speed (Low, Medium, High, Auto, etc.). These values are pre-configured in discovery time. |
| op-mode             | Operation Mode     | Number (Labeled)   | Defines device's operation mode (Fan, Cool, Dry, etc). These values are pre-configured at discovery time.                                                 |
| power               | Power              | Switch             | Define the device's Power state.                                                                                                                          |
| cool-jet            | Cool Jet           | Switch             | Switch Cool Jet ON/OFF                                                                                                                                    |
| auto-dry            | Auto Dry           | Switch             | Switch Auto Dry ON/OFF                                                                                                                                    |
| energy-saving       | Energy Saving      | Switch             | Switch Energy Saving ON/OFF                                                                                                                               |
| fan-step-up-down    | Fan VDir           | Number             | Define the fan vertical direction's mode (Off, Upper, Circular, Up, Middle Up, etc)                                                                       |
| fan-step-left-right | Fan HDir           | Number             | Define the fan horizontal direction's mode (Off, Lefter, Left, Circular, Right, etc)                                                                      |

#### More Information Channel

| channel #            | channel                        | type                 | description                                                                  |
|----------------------|--------------------------------|----------------------|------------------------------------------------------------------------------|
| extra-info-collector | Enable Extended Info Collector | Switch               | Enable/Disable the extra information collector to update the bellow channels |
| current-energy       | Current Energy                 | Number:Energy        | The Current Energy consumption in Kwh                                        |
| remaining-filter     | Remaining Filter               | Number:Dimensionless | Percentage of the remaining filter                                           |

### Heat Pump

LG ThinQ Heat Pump supports the following channels

#### Heat Pump Channels

| channel #           | channel             | type               | description                                                                                               |
|---------------------|---------------------|--------------------|-----------------------------------------------------------------------------------------------------------|
| target-temperature  | Target Temperature  | Number:Temperature | Defines the desired target temperature for the device                                                     |
| min-temperature     | Minimum Temperature | Number:Temperature | Minimum temperature for the current operation mode                                                        |
| max-temperature     | Maximum Temperature | Number:Temperature | Maximum temperature for the current operation mode                                                        |
| current-temperature | Temperature         | Number:Temperature | Read-Only channel that indicates the current temperature informed by the device                           |
| op-mode             | Operation Mode      | Number (Labeled)   | Defines device's operation mode (Fan, Cool, Dry, etc). These values are pre-configured at discovery time. |
| power               | Power               | Switch             | Define the device's Current Power state.                                                                  |
| air-water-switch    | Air/Water Switch    | Switch             | Switch the heat pump operation between Air or Water                                                       |

#### Heat Pump More Information Channel

| channel #            | channel                        | type                 | description                                                                  |
|----------------------|--------------------------------|----------------------|------------------------------------------------------------------------------|
| extra-info-collector | Enable Extended Info Collector | Switch               | Enable/Disable the extra information collector to update the bellow channels |
| current-energy       | Current Energy                 | Number:Energy        | The Current Energy consumption in Kwh                                        |

### Washer Machine

LG ThinQ Washer Machine supports the following channels

#### Washer Machine Dashboard Channels

| channel #          | channel           | type       | description                                                                                                |
|--------------------|-------------------|------------|------------------------------------------------------------------------------------------------------------|
| state              | Washer State      | String     | General State of the Washer                                                                                |
| power              | Power             | Switch     | Define the device's Current Power state.                                                                   |
| process-state      | Process State     | String     | States of the running cycle                                                                                |
| course             | Course            | String     | Course set up to work                                                                                      |
| temperature-level  | Temperature Level | String     | Temperature level supported by the Washer (Cold, 20, 30, 40, 50, etc.)                                     |
| door-lock          | Door Lock         | Switch     | Display if the Door is Locked.                                                                             |
| rinse              | Rinse             | String     | The Rinse set program                                                                                      |
| spin               | Spin              | String     | The Spin set option                                                                                        |
| delay-time         | Delay Time        | String     | Delay time programmed to start the cycle                                                                   |
| remain-time        | Remaining Time    | String     | Remaining time to finish the course                                                                        |
| stand-by           | Stand By Mode     | Switch     | If the Washer is in stand-by-mode                                                                          |
| rs-flag            | Remote Start      | Switch     | If the Washer is in remote start mode waiting to be remotely started                                       |

#### Washer Machine Remote Start Option

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

#### Dryer Machine Dashboard Channels

| channel #          | channel           | type    | description                                                            |
|--------------------|-------------------|---------|------------------------------------------------------------------------|
| power              | Power             | Switch  | Define the device's Current Power state.                               |
| state              | Dryer State       | String  | General State of the Washer                                            |
| process-state      | Process State     | String  | States of the running cycle                                            |
| course             | Course            | String  | Course set up to work                                                  |
| temperature-level  | Temperature Level | String  | Temperature level supported by the Washer (Cold, 20, 30, 40, 50, etc.) |
| child-lock         | Child Lock        | Switch  | Display if the Door is Locked.                                         |
| dry-level          | Dry Level Course  | String  | Dry level set to work in the course                                    |
| delay-time         | Delay Time        | String  | Delay time programmed to start the cycle                               |
| remain-time        | Remaining Time    | String  | Remaining time to finish the course                                    |
| stand-by           | Stand By Mode     | Switch  | If the Washer is in stand-by-mode                                      |
| rs-flag            | Remote Start      | Switch  | If the Washer is in remote start mode waiting to be remotely started   |

#### Dryer Machine Remote Start Option

This Channel Group is only available if the Dryer is configured to Remote Start

| channel #     | channel           | type               | description                                                                                             |
|---------------|-------------------|--------------------|---------------------------------------------------------------------------------------------------------|
| rs-start-stop | Remote Start/Stop | Switch             | Switch to control if you want to start/stop the cycle remotely                                          |
| rs-course     | Course to Run     | String (Selection) | The pre-programmed course (or default) is shown. You can change-it if you want before remote start      |

### Dryer/Washer Tower

LG ThinQ Dryer/Washer is recognized as 2 different things: Dryer & Washer machines.
Thus, for this device, follow the paragraph's for Dryer Machine and Washer Machine

### Refrigerator

LG ThinQ Refrigerator supports the following channels

#### Refrigerator Dashboard Channels

| channel #            | channel                       | type               | description                                                                    |
|----------------------|-------------------------------|--------------------|--------------------------------------------------------------------------------|
| door-open            | Door Open                     | Contact            | Advice if the door is opened                                                   |
| freezer-temperature  | Freezer Set Point Temperature | Number:Temperature | Temperature level chosen. This channel supports commands to change temperature |
| fridge-temperature   | Fridge Set Point Temperature  | Number:Temperature | Temperature level chosen. This channel supports commands to change temperature |
| temp-unit            | Temp. Unit                    | String             | Temperature Unit (°C/F). Supports command to change the unit                   |
| express-mode      | Express Freeze                | Switch             | Channel to change the express freeze function (ON/OFF/Rapid)                   |
| express-cool-mode | Express Cool                  | Switch             | Channel to switch ON/OFF express cool function                                 |
| eco-friendly-mode | Vacation                      | Switch             | Channel to switch ON/OFF Vacation function (unit will work in eco mode)        |

#### Refrigerator More Information

This Channel Group is reports useful information data for the device:

| channel #           | channel          | type      | description                                                |
|---------------------|------------------|-----------|------------------------------------------------------------|
| fresh-air-filter | Fresh Air Filter | String    | Shows the Fresh Air filter status (OFF/AUTO/POWER/REPLACE) |
| water-filter     | Water Filter     | String    | Shows the filter's used months                             |

OBS: some versions of this device can not support all the channels, depending on the model's capabilities.

## Full Example

Example of how to configure a thing.

### Example `demo.things`

```java
Bridge lgthinq:bridge:MyLGThinqBridge [ username="user@registered.com", password="cleartext-password", language="en", country="US", poolingIntervalSec=600] {
   Thing air-conditioner-401 myAC [ modelUrlInfo="<ac-model-url>", deviceId="<device-id>", platformType="<platform-type>", modelId="<model-id>", deviceAlias="<MyAC>" ]
}
```

Until now, there is no way to easily obtain the values of ac-model-url, device-id, platform-type and model-id. So, if you really need
to configure the LGThinq thing textually, I suggest you to first add it with the UI discovery process through the LG Thinq Bridge, then after, copy
these properties from the thing created and complete the textual configuration.

Here are some examples on how to map the channels to items.

### Example `demo.items`

```java
Switch               ACPower        "Power"                   <switch>  { channel="lgthinq:air-conditioner-401:myAC:dashboard#power" }
Number               ACOpMode       "Operation Mode"          <text>    { channel="lgthinq:air-conditioner-401:myAC:dashboard#op-mode" }
Number:Temperature   ACTargetTemp   "Target Temperature"      <text>    { channel="lgthinq:air-conditioner-401:myAC:dashboard#target-temperature" }
Number:Temperature   ACCurrTemp     "Temperature"             <text>    { channel="lgthinq:air-conditioner-401:myAC:dashboard#current-temperature" }
Number               ACFanSpeed     "Fan Speed"               <text>    { channel="lgthinq:air-conditioner-401:myAC:dashboard#fan-speed" }
Switch               ACCoolJet      "CoolJet"                 <switch>  { channel="lgthinq:air-conditioner-401:myAC:dashboard#cool-jet" }
Switch               ACAutoDry      "Auto Dry"                <switch>  { channel="lgthinq:air-conditioner-401:myAC:dashboard#auto-dry" }
Switch               ACEnSaving     "Energy Saving"           <switch>  { channel="lgthinq:air-conditioner-401:myAC:dashboard#emergy-saving" }
Number               ACFanVDir      "Vertical Direction"      <text>    { channel="lgthinq:air-conditioner-401:myAC:dashboard#fan-step-up-down" }
```

### Example `demo.sitemap`

All the channels already have StateDescription for the selection Channels. So, unless you want to rename theirs into demo.items,
you can simply define as Selection that the default description of the values will be displayed.

```perl

sitemap demo label="Air Conditioner"
{
    Frame label="Dashboard" {
        Switch    item=ACPower
        Selection item=ACOpMode
        Selection item=ACTargetTemp
        Text      item=ACCurrTemp
        Selection item=ACFanSpeed
        Switch    item=ACCoolJet
        Switch    item=ACAutoDry
        Switch    item=ACEnSaving
        Selection item=ACFanSpeed
    }
}
```
