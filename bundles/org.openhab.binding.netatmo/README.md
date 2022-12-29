# Netatmo Binding

The Netatmo binding integrates the following Netatmo products:

- _Personal Weather Station_. Reports temperature, humidity, air pressure, carbon dioxide concentration in the air, as well as the ambient noise level.
- _Thermostat_. Reports ambient temperature, allow to check target temperature, consult and change furnace heating status.
- _Indoor Camera / Welcome_. Reports last event and persons at home, consult picture and video from event/camera.
- _Siren_
- _Outdoor Camera / Presence_. Reports last event, consult picture and video from event/camera.
- _Doorbell_
- _Smoke Detector_
- _Smart Door Sensor_

See <https://www.netatmo.com/> for details on their product.

## Binding Configuration

The binding requires you to register an Application with Netatmo Connect at [https://dev.netatmo.com/](https://dev.netatmo.com/) - this will get you a set of Client ID and Client Secret parameters to be used by your configuration.

### Create Netatmo Application

Follow instructions under:

 1. Setting Up Your Account
 1. Registering Your Application
 1. Setting Redirect URI and webhook URI can be skipped, these will be provided by the binding.

Variables needed for the setup of the binding are:

- `<CLIENT_ID>` Your client ID taken from your App at <https://dev.netatmo.com/apps>
- `<CLIENT_SECRET>` A token provided along with the `<CLIENT_ID>`.

The binding has the following configuration options:

| Parameter   | Type    | Description                                                  |
| ----------- | ------- | ------------------------------------------------------------ |
| readFriends | Boolean | Enables or disables the discovery of guest weather stations. |

## Netatmo Account (Bridge) Configuration

You will have to create at first a bridge to handle communication with your Netatmo Application.

The Account bridge has the following configuration elements:

| Parameter         | Type   | Required | Description                                                                                                            |
| ----------------- | ------ | -------- | ---------------------------------------------------------------------------------------------------------------------- |
| clientId          | String | Yes      | Client ID provided for the application you created on <http://dev.netatmo.com/createapp>                               |
| clientSecret      | String | Yes      | Client Secret provided for the application you created                                                                 |
| webHookUrl        | String | No       | Protocol, public IP and port to access openHAB server from Internet                                                    |
| reconnectInterval | Number | No       | The reconnection interval to Netatmo API (in s)                                                                        |
| refreshToken      | String | Yes*     | The refresh token provided by Netatmo API after the granting process. Can be saved in case of file based configuration |

(*) Strictly said this parameter is not mandatory at first run, until you grant your binding on Netatmo Connect. Once present, you'll not have to grant again.

**Supported channels for the Account bridge thing:**

| Channel Group | Channel Id    | Item Type | Description                                                        |
| ------------- | ------------- | --------- | ------------------------------------------------------------------ |
| monitoring    | request-count | Number    | Number of requests transmitted to Netatmo API during the last hour |

### Configure the Bridge

1. Complete the Netatmo Application Registration if you have not already done so, see above.
1. Make sure you have your _Client ID_ and _Client Secret_ identities available.
1. Add a new **"Netatmo Account"** thing. Choose new Id for the account, unless you like the generated one, put in the _Client ID_ and _Client Secret_ from the Netatmo Connect Application registration in their respective fields of the bridge configuration. Save the bridge.
1. The bridge thing will go _OFFLINE_ / _CONFIGURATION_ERROR_ - this is fine. You have to authorize this bridge with Netatmo Connect.
1. Go to the authorization page of your server. `http://<your openHAB address>:8080/netatmo/connect/<_CLIENT_ID_>`. Your newly added bridge should be listed there (no need for you to expose your openHAB server outside your local network for this).
1. Press the _"Authorize Thing"_ button. This will take you either to the login page of Netatmo Connect or directly to the authorization screen. Login and/or authorize the application. You will be returned and the entry should go green.
1. The bridge configuration will be updated with a refresh token and go _ONLINE_. The refresh token is used to re-authorize the bridge with Netatmo Connect Web API whenever required. So you can consult this token by opening the Thing page in MainUI, this is the value of the advanced parameter named “Refresh Token”.
1. If you're using file based .things config file, copy the provided refresh token in the **refreshToken** parameter of your thing definition (example below).

Now that you have got your bridge _ONLINE_ you can now start a scan with the binding to auto discover your things.

## List of supported things

| Thing Type      | Type   | Netatmo Object | Description                                                                                           | Thing Parameters                                                          |
| --------------- | ------ | -------------- | ----------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| account         | Bridge | N/A            | This bridge represents an account, gateway to Netatmo API.                                            | clientId, clientSecret, username, password, webHookUrl, reconnectInterval |
| home            | Bridge | NAHome         | A home hosting Security or Energy devices and modules.                                                | id, refreshInterval                                                       |
| person          | Thing  | NAPerson       | A person known by your Netatmo system.                                                                | id                                                                        |
| welcome         | Thing  | NACamera       | The Netatmo Smart Indoor Camera (Welcome).                                                            | id                                                                        |
| presence        | Thing  | NOC            | The Netatmo Smart Outdoor Camera (Presence) camera with or without siren.                             | id                                                                        |
| siren           | Thing  | NIS            | The Netatmo Smart Indoor Siren.                                                                       | id                                                                        |
| doorbell        | Thing  | NDB            | The Netatmo Smart Video Doorbell device.                                                              | id                                                                        |
| weather-station | Bridge | NAMain         | Main indoor module reporting temperature, humidity, pressure, air quality and sound level.            | id                                                                        |
| outdoor         | Thing  | NAModule1      | Outdoor module reporting temperature and humidity.                                                    | id                                                                        |
| wind            | Thing  | NAModule2      | Wind sensor reporting wind angle and strength.                                                        | id                                                                        |
| rain            | Thing  | NAModule3      | Rain Gauge measuring precipitation.                                                                   | id                                                                        |
| indoor          | Thing  | NAModule4      | Additional indoor module reporting temperature, humidity and CO2 level.                               | id                                                                        |
| home-coach      | Thing  | NHC            | Healthy home coach reporting health-index, temperature, humidity, pressure, air quality, sound level. | id                                                                        |
| plug            | Thing  | NAPlug         | The relay connected to the boiler controlling a Thermostat and zero or more valves.                   | id                                                                        |
| thermostat      | Thing  | NATherm1       | The Thermostat device placed in a given room.                                                         | id                                                                        |
| room            | Thing  | NARoom         | A room in your house.                                                                                 | id                                                                        |
| valve           | Thing  | NRV            | A valve controlling a radiator.                                                                       | id                                                                        |
| tag             | Thing  | NACamDoorTag   | A door / window sensor                                                                                | id                                                                        |

### Webhook

Netatmo servers can send push notifications to the Netatmo Binding by using a callback URL.
The webhook URL is setup at Netatmo Account level using "Webhook Address" parameter.
You will define here public way to access your openHAB server:

```text
http(s)://xx.yy.zz.ww:443
```

Your Netatmo App will be configured automatically by the bridge to the endpoint:

```text
http(s)://xx.yy.zz.ww:443/netatmo/webhook/<_CLIENT_ID_>
```

Please be aware of Netatmo own limits regarding webhook usage that lead to a 24h ban-time when webhook does not answer 5 times.

NB: Allowed ports for webhooks are 80, 88, 443 and 9443.

### Configure Things

The easiest way to retrieve the IDs for all the devices and modules is to use the console command `openhab:netatmo showIds`.
It shows the hierarchy of all the devices and modules including their IDs.
This can help to define all your things in a configuration file.

**Another way to get the IDs is to use the developer documentation on the netatmo site:**

The IDs for the modules can be extracted from the developer documentation on the netatmo site.
First login with your user.
Then some examples of the documentation contain the **real results** of your weather station.
In order to try the examples, you need the `device_id` of your Netatmo station.
You can find it in the configuration menu of the app (android or apple).
Get the IDs of your devices (indoor, outdoor, rain gauge)
[here](https://dev.netatmo.com/resources/technical/reference/weather/getstationsdata).

`main_device` is the ID of the "main device", the indoor sensor.
This is equal to the MAC address of the Netatmo.

The other modules you can recognize by "module_name" and then note the "\_id" which you need later.

**Another way to get the IDs is to calculate them:**

You have to calculate the ID for the outside module as follows: (it cannot be read from the app)

- if the first serial character is "h": start with "02"
- if the first serial character is "i": start with "03"
- if the first serial character is "j": start with "04"
- if the first serial character is "k": start with "05"

append ":00:00:",

split the rest into three parts of two characters and append with a colon as delimiter.

For example your serial number "h00bcdc" should end up as "02:00:00:00:bc:dc".

## Discovery

If you did not manually create things in the *.things file, the Netatmo Binding is able to discover automatically all depending modules and devices.

## Channels

### Weather Station Main Indoor Device

Weather station does not need any refreshInterval setting.
Based on a standard update period of 10mn by Netatmo systems - it will auto adapt to stick closest as possible to last data availability.

**Supported channels for the main indoor module:**

| Channel Group | Channel Id          | Item Type            | Description                                      |
| ------------- | ------------------- | -------------------- | ------------------------------------------------ |
| pressure      | value               | Number:Pressure      | Current pressure                                 |
| pressure      | absolute            | Number:Pressure      | Pressure at sea level                            |
| pressure      | trend               | String               | Pressure evolution trend over time               |
| noise         | value               | Number:Dimensionless | Current noise level                              |
| humidity      | value               | Number:Dimensionless | Current humidity                                 |
| humidity      | humidex             | Number               | Computed Humidex index                           |
| humidity      | humidex-scale       | Number               | Humidex index appreciation                       |
| temperature   | value               | Number:Temperature   | Current temperature                              |
| temperature   | min-today           | Number:Temperature   | Minimum temperature on current day               |
| temperature   | max-today           | Number:Temperature   | Maximum temperature on current day               |
| temperature   | min-time            | DateTime             | Moment of today's minimum temperature            |
| temperature   | max-time            | DateTime             | Moment of today's maximum temperature            |
| temperature   | trend               | String               | Temperature evolution trend over time            |
| temperature   | heat-index          | Number:Temperature   | Computed Heat Index                              |
| temperature   | dewpoint            | Number:Temperature   | Computed dewpoint temperature                    |
| temperature   | dewpoint-depression | Number:Temperature   | Computed dewpoint depression                     |
| airquality    | co2                 | Number:Dimensionless | CO2 level in ppm                                 |
| location      | value               | Location             | Location of the device                           |
| timestamp     | last-seen           | DateTime             | Last time the module reported its presence       |
| timestamp     | measures            | DateTime             | Moment of the last measures update               |
| signal        | strength            | Number               | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value               | Number:Power         | Signal strength in dBm                           |

All these channels are read only.

**Extensible channels for the main indoor module:**

| Channel Type         | Item Type            | Description                       | Channel parameters                                                   |
| -------------------- | -------------------- | --------------------------------- | -------------------------------------------------------------------- |
| co2-measurement      | Number:Dimensionless | CO2 measurement                   | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| co2-timestamp        | DateTime             | CO2 measurement timestamp         | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| hum-measurement      | Number:Dimensionless | Humidity measurement              | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| hum-timestamp        | DateTime             | Humidity measurement timestamp    | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| noise-measurement    | Number:Dimensionless | Noise measurement                 | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| noise-timestamp      | DateTime             | Noise measurement timestamp       | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| pressure-measurement | Number:Pressure      | Pressure measurement              | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| pressure-timestamp   | DateTime             | Pressure measurement timestamp    | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| temp-measurement     | Number:Temperature   | Temperature measurement           | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| temp-timestamp       | DateTime             | Temperature measurement timestamp | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |

### Weather Station Outdoor module

**Supported channels for the outdoor module:**

| Channel Group | Channel Id          | Item Type            | Description                                      |
| ------------- | ------------------- | -------------------- | ------------------------------------------------ |
| humidity      | value               | Number:Dimensionless | Current humidity                                 |
| humidity      | humidex             | Number               | Computed Humidex index                           |
| humidity      | humidex-scale       | Number               | Humidex index appreciation                       |
| temperature   | value               | Number:Temperature   | Current temperature                              |
| temperature   | min-today           | Number:Temperature   | Minimum temperature on current day               |
| temperature   | max-today           | Number:Temperature   | Maximum temperature on current day               |
| temperature   | min-time            | DateTime             | Moment of today's minimum temperature            |
| temperature   | max-time            | DateTime             | Moment of today's maximum temperature            |
| temperature   | trend               | String               | Temperature evolution trend over time            |
| temperature   | heat-index          | Number:Temperature   | Computed Heat Index                              |
| temperature   | dewpoint            | Number:Temperature   | Computed dewpoint temperature                    |
| temperature   | dewpoint-depression | Number:Temperature   | Computed dewpoint depression                     |
| timestamp     | last-seen           | DateTime             | Last time the module reported its presence       |
| timestamp     | measures            | DateTime             | Moment of the last measures update               |
| signal        | strength            | Number               | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value               | Number:Power         | Signal strength in dBm                           |
| battery       | value               | Number               | Battery level                                    |
| battery       | low-battery         | Switch               | Low battery                                      |

All these channels are read only.

**Extensible channels for the outdoor module:**

| Channel Type     | Item Type            | Description                       | Channel parameters                                                   |
| ---------------- | -------------------- | --------------------------------- | -------------------------------------------------------------------- |
| hum-measurement  | Number:Dimensionless | Humidity measurement              | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| hum-timestamp    | DateTime             | Humidity measurement timestamp    | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| temp-measurement | Number:Temperature   | Temperature measurement           | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| temp-timestamp   | DateTime             | Temperature measurement timestamp | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |

### Weather Station Additional Indoor module

**Supported channels for the additional indoor module:**

| Channel Group | Channel Id          | Item Type            | Description                                      |
| ------------- | ------------------- | -------------------- | ------------------------------------------------ |
| humidity      | value               | Number:Dimensionless | Current humidity                                 |
| humidity      | humidex             | Number               | Computed Humidex index                           |
| humidity      | humidex-scale       | Number               | Humidex index appreciation                       |
| temperature   | value               | Number:Temperature   | Current temperature                              |
| temperature   | min-today           | Number:Temperature   | Minimum temperature on current day               |
| temperature   | max-today           | Number:Temperature   | Maximum temperature on current day               |
| temperature   | min-time            | DateTime             | Moment of today's minimum temperature            |
| temperature   | max-time            | DateTime             | Moment of today's maximum temperature            |
| temperature   | trend               | String               | Temperature evolution trend over time            |
| temperature   | heat-index          | Number:Temperature   | Computed Heat Index                              |
| temperature   | dewpoint            | Number:Temperature   | Computed dewpoint temperature                    |
| temperature   | dewpoint-depression | Number:Temperature   | Computed dewpoint depression                     |
| airquality    | co2                 | Number:Dimensionless | Air quality                                      |
| timestamp     | last-seen           | DateTime             | Last time the module reported its presence       |
| timestamp     | measures            | DateTime             | Moment of the last measures update               |
| signal        | strength            | Number               | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value               | Number:Power         | Signal strength in dBm                           |
| battery       | value               | Number               | Battery level                                    |
| battery       | low-battery         | Switch               | Low battery                                      |

All these channels are read only.

**Extensible channels for the additional indoor module:**

| Channel Type     | Item Type            | Description                       | Channel parameters                                                   |
| ---------------- | -------------------- | --------------------------------- | -------------------------------------------------------------------- |
| co2-measurement  | Number:Dimensionless | CO2 measurement                   | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| co2-timestamp    | DateTime             | CO2 measurement timestamp         | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| hum-measurement  | Number:Dimensionless | Humidity measurement              | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| hum-timestamp    | DateTime             | Humidity measurement timestamp    | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| temp-measurement | Number:Temperature   | Temperature measurement           | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| temp-timestamp   | DateTime             | Temperature measurement timestamp | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |

### Rain Gauge

**Supported channels for the rain guage:**

| Channel Group | Channel Id  | Item Type     | Description                                      |
| ------------- | ----------- | ------------- | ------------------------------------------------ |
| rain          | value       | Number:Speed  | Current precipitation intensity                  |
| rain          | sum-1       | Number:Length | Quantity of water over last hour                 |
| rain          | sum-24      | Number:Length | Quantity of water during the current day         |
| timestamp     | last-seen   | DateTime      | Last time the module reported its presence       |
| timestamp     | measures    | DateTime      | Moment of the last measures update               |
| signal        | strength    | Number        | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value       | Number:Power  | Signal strength in dBm                           |
| battery       | value       | Number        | Battery level                                    |
| battery       | low-battery | Switch        | Low battery                                      |

All these channels are read only.

**Extensible channels for the rain guage:**

| Channel Type         | Item Type     | Description              | Channel parameters                                 |
| -------------------- | ------------- | ------------------------ | -------------------------------------------------- |
| sum_rain-measurement | Number:Length | Summing rain measurement | period (30min, 1hour, 3hours, 1day, 1week, 1month) |

### Weather Station Wind module

**Supported channels for the wind module:**

| Channel Group | Channel Id        | Item Type    | Description                                      |
| ------------- | ----------------- | ------------ | ------------------------------------------------ |
| wind          | angle             | Number:Angle | Current 5 minutes average wind direction         |
| wind          | strength          | Number:Speed | Current 5 minutes average wind speed             |
| wind          | max-strength      | Number:Speed | Maximum wind strength recorded                   |
| wind          | max-strength-date | DateTime     | Moment when MaxWindStrength was recorded         |
| wind          | gust-angle        | Number:Angle | Direction of the last 5 minutes highest gust     |
| wind          | gust-strength     | Number:Speed | Speed of the last 5 minutes highest gust wind    |
| timestamp     | last-seen         | DateTime     | Last time the module reported its presence       |
| timestamp     | measures          | DateTime     | Moment of the last measures update               |
| signal        | strength          | Number       | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value             | Number:Power | Signal strength in dBm                           |
| battery       | value             | Number       | Battery level                                    |
| battery       | low-battery       | Switch       | Low battery                                      |

All these channels are read only.

### Healthy Home Coach Device

**Supported channels for the healthy home coach device:**

| Channel Group | Channel Id          | Item Type            | Description                                      |
| ------------- | ------------------- | -------------------- | ------------------------------------------------ |
| noise         | value               | Number:Dimensionless | Current noise level                              |
| humidity      | value               | Number:Dimensionless | Current humidity                                 |
| humidity      | humidex             | Number               | Computed Humidex index                           |
| humidity      | humidex-scale       | Number               | Humidex index appreciation                       |
| pressure      | value               | Number:Pressure      | Current pressure                                 |
| pressure      | absolute            | Number:Pressure      | Pressure at sea level                            |
| temperature   | value               | Number:Temperature   | Current temperature                              |
| temperature   | min-today           | Number:Temperature   | Minimum temperature on current day               |
| temperature   | max-today           | Number:Temperature   | Maximum temperature on current day               |
| temperature   | min-time            | DateTime             | Moment of today's minimum temperature            |
| temperature   | max-time            | DateTime             | Moment of today's maximum temperature            |
| temperature   | heat-index          | Number:Temperature   | Computed Heat Index                              |
| temperature   | dewpoint            | Number:Temperature   | Computed dewpoint temperature                    |
| temperature   | dewpoint-depression | Number:Temperature   | Computed dewpoint depression                     |
| airquality    | health-index        | Number               | Health index (*)                                 |
| airquality    | co2                 | Number:Dimensionless | Air quality                                      |
| timestamp     | last-seen           | DateTime             | Last time the module reported its presence       |
| timestamp     | measures            | DateTime             | Moment of the last measures update               |
| signal        | strength            | Number               | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value               | Number:Power         | Signal strength in dBm                           |

(*) Health index values :

- 0 : healthy
- 1 : fine
- 2 : fair
- 3 : poor
- 4 : unhealthy

All these channels are read only.

**Extensible channels for the healthy home coach device:**

| Channel Type         | Item Type            | Description                       | Channel parameters                                                   |
| -------------------- | -------------------- | --------------------------------- | -------------------------------------------------------------------- |
| hum-measurement      | Number:Dimensionless | Humidity measurement              | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| hum-timestamp        | DateTime             | Humidity measurement timestamp    | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| noise-measurement    | Number:Dimensionless | Noise measurement                 | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| noise-timestamp      | DateTime             | Noise measurement timestamp       | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| pressure-measurement | Number:Pressure      | Pressure measurement              | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| pressure-timestamp   | DateTime             | Pressure measurement timestamp    | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |
| temp-measurement     | Number:Temperature   | Temperature measurement           | limit (MIN, MAX), period (30min, 1hour, 3hours, 1day, 1week, 1month) |
| temp-timestamp       | DateTime             | Temperature measurement timestamp | limit (DATE_MIN, DATE_MAX), period (1week, 1month)                   |

### Thermostat Relay Device

**Supported channels for the thermostat relay device:**

| Channel Group | Channel Id | Item Type    | Description                                      |
| ------------- | ---------- | ------------ | ------------------------------------------------ |
| signal        | strength   | Number       | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value      | Number:Power | Signal strength in dBm                           |

All these channels are read only.

### Thermostat Plug

**Supported channels for the thermostat plug device:**

| Channel Group | Channel Id | Item Type    | Description                                      |
| ------------- | ---------- | ------------ | ------------------------------------------------ |
| signal        | strength   | Number       | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value      | Number:Power | Signal strength in dBm                           |

All these channels are read only.

### Room

**Supported channels for the Room thing:**

| Channel Group | Channel Id            | Item Type            | Description                                             |
| ------------- | --------------------- | -------------------- | ------------------------------------------------------- |
| temperature   | value                 | Number:Temperature   | Current temperature in the room                         |
| properties    | window-open           | Contact              | Windows of the room are opened                          |
| properties    | anticipating          | Switch               | Anticipates next scheduled setpoint                     |
| properties    | heating-power-request | Number:Dimensionless | Percentage of heating power                             |
| setpoint      | value                 | Number:Temperature   | Thermostat temperature setpoint                         |
| setpoint      | mode                  | String               | Chosen thermostat mode (home, frost guard, manual, max) |
| setpoint      | start                 | DateTime             | Start time of the currently applied setpoint            |
| setpoint      | end                   | DateTime             | End time of the currently applied setpoint              |

All these channels except setpoint and setpoint-mode are read only.

### Thermostat Module

**Supported channels for the thermostat module:**

| Channel Group | Channel Id  | Item Type    | Description                                      |
| ------------- | ----------- | ------------ | ------------------------------------------------ |
| properties    | relay       | Contact      | Indicates if the boiler is currently heating     |
| signal        | strength    | Number       | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value       | Number:Power | Signal strength in dBm                           |
| battery       | value       | Number       | Battery level                                    |
| battery       | low-battery | Switch       | Low battery                                      |
| battery       | status      | String       | Description of the battery status (*)            |

(*) Can be UNDEF on some modules

### Valve Module

**Supported channels for the Valve module:**

| Channel Group | Channel Id  | Item Type    | Description                                      |
| ------------- | ----------- | ------------ | ------------------------------------------------ |
| signal        | strength    | Number       | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value       | Number:Power | Signal strength in dBm                           |
| battery       | value       | Number       | Battery level                                    |
| battery       | low-battery | Switch       | Low battery                                      |
| battery       | status      | String       | Description of the battery status (*)            |

### Welcome Home

All these channels are read only.

**Supported channels for the Home thing:**

| Channel Group | Channel Id             | Item Type | Description                                      |
| ------------- | ---------------------- | --------- | ------------------------------------------------ |
| security      | person-count           | Number    | Total number of persons that are at home         |
| security      | unknown-person-count   | Number    | Total number of unknown persons that are at home |
| security      | unknown-person-picture | Image     | Snapshot of unknown person that is at home       |

All these channels are read only.

**Supported trigger channels for the Home thing:**

| Channel Type ID  | Options            | Description                                                                                                                                                                      |
| ---------------- | ------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| cameraEvent      |                    | A camera event is triggered with a short delay but without requiring a webhook. The information of the event can get retrieved from the other "welcomeEvent" home thing channels |
|                  | HUMAN              | Triggered when a human (or person) was detected                                                                                                                                  |
|                  | ANIMAL             | Triggered when an animal was detected                                                                                                                                            |
|                  | MOVEMENT           | Triggered when an unspecified movement was detected                                                                                                                              |
|                  | VEHICLE            | Triggered when a vehicle was detected                                                                                                                                            |
| welcomeHomeEvent |                    | A welcome home event is triggered directly via a configured webhook                                                                                                              |
|                  | PERSON             | Triggered when a concrete person was detected                                                                                                                                    |
|                  | PERSON_AWAY        | Triggered when a concrete person leaves                                                                                                                                          |
|                  | MOVEMENT           | Triggered when a movement was detected                                                                                                                                           |
|                  | CONNECTION         | Triggered when a camera connection gets created                                                                                                                                  |
|                  | DISCONNECTION      | Triggered when a camera connection got lost                                                                                                                                      |
|                  | ON                 | Triggered when camera monitoring is switched on                                                                                                                                  |
|                  | OFF                | Triggered when camera monitoring is switched off                                                                                                                                 |
|                  | BOOT               | Triggered when a camera is booting                                                                                                                                               |
|                  | SD                 | Triggered when a camera SD card status was changed                                                                                                                               |
|                  | ALIM               | Triggered when a power supply status was changed                                                                                                                                 |
|                  | NEW_MODULE         | Triggered when a new module was discovered                                                                                                                                       |
|                  | MODULE_CONNECT     | Triggered when a module gets connected                                                                                                                                           |
|                  | MODULE_DISCONNECT  | Triggered when a module gets disconnected                                                                                                                                        |
|                  | MODULE_LOW_BATTERY | Triggered when the battery of a module gets low                                                                                                                                  |
|                  | MODULE_END_UPDATE  | Triggered when a firmware update of a module is done                                                                                                                             |
|                  | TAG_BIG_MOVE       | Triggered when a big movement of a tag was detected                                                                                                                              |
|                  | TAG_SMALL_MOVE     | Triggered when a small movement of a tag was detected                                                                                                                            |
|                  | TAG_UNINSTALLED    | Triggered when a tag gets uninstalled                                                                                                                                            |
|                  | TAG_OPEN           | Triggered when an open event of a tag was detected                                                                                                                               |

### Welcome, Presence and Doorbell Cameras

Warnings:

- The URL of the live snapshot is a fixed URL so the value of the channel cameraLivePictureUrl / welcomeCameraLivePictureUrl will never be updated once first set by the binding. So to get a refreshed picture, you need to use the refresh parameter in your sitemap image element.
- Some features like the video surveillance are accessed via the local network, so it may be helpful to set a static IP address for the camera within your local network.

**Supported channels for the Welcome Camera thing:**

| Channel Group | Channel ID           | Item Type    | Read/Write | Description                                                                                                                                 |
| ------------- | -------------------- | ------------ | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| status        | monitoring           | Switch       | Read-write | State of the camera (video surveillance on/off)                                                                                             |
| status        | sd-card              | String       | Read-only  | State of the SD card                                                                                                                        |
| status        | alim                 | String       | Read-only  | State of the power connector                                                                                                                |
| live          | picture              | Image        | Read-only  | Camera Live Snapshot                                                                                                                        |
| live          | local-picture-url    | String       | Read-only  | Local Url of the live snapshot for this camera                                                                                              |
| live          | vpn-picture-url      | String       | Read-only  | Url of the live snapshot for this camera through Netatmo VPN.                                                                               |
| live          | local-stream-url (*) | String       | Read-only  | Local Url of the live stream for this camera (accessible if openhab server and camera are located on the same lan.                          |
| live          | vpn-stream-url (*)   | String       | Read-only  | Url of the live stream for this camera through Netatmo VPN.                                                                                 |
| signal        | strength             | Number       | Read-only  | Signal strength (0 for no signal, 1 for weak...)                                                                                            |
| signal        | value                | Number:Power | Read-only  | Signal strength in dBm                                                                                                                      |
| last-event    | type                 | String       | Read-only  | Type of event                                                                                                                               |
| last-event    | subtype              | String       | Read-only  | Sub-type of event                                                                                                                           |
| last-event    | time                 | DateTime     | Read-only  | Time of occurrence of event                                                                                                                 |
| last-event    | message              | String       | Read-only  | Message sent by Netatmo corresponding to given event                                                                                        |
| last-event    | snapshot             | Image        | Read-only  | picture of the last event, if it applies                                                                                                    |
| last-event    | snapshot-url         | String       | Read-only  | If the last event (depending upon event type) in the home lead a snapshot picture, the picture URL will be available here                   |
| last-event    | local-video-url      | String       | Read-only  | If the last event (depending upon event type) in the home lead a snapshot picture, the corresponding local video URL will be available here |
| last-event    | vpn-video-url        | String       | Read-only  | If the last event (depending upon event type) in the home lead a snapshot picture, the corresponding VPN video URL will be available here   |
| last-event    | video-status         | String       | Read-only  | Status of the video (recording, deleted or available)                                                                                       |
| last-event    | person-id            | String       | Read-only  | Id of the person the event is about (if any)                                                                                                |

(*) This channel is configurable : low, poor, high.

**Supported channels for the Presence Camera thing:**

Warnings:

- The floodlight auto-mode (auto-mode) isn't updated it is changed by another application. Therefore the binding handles its own state of the auto-mode. This has the advantage that the user can define its own floodlight switch off behaviour.

| Channel Group | Channel ID           | Item Type    | Read/Write | Description                                                                                                                                 |
| ------------- | -------------------- | ------------ | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| status        | monitoring           | Switch       | Read-write | State of the camera (video surveillance on/off)                                                                                             |
| status        | sd-card              | String       | Read-only  | State of the SD card                                                                                                                        |
| status        | alim                 | String       | Read-only  | State of the power connector                                                                                                                |
| live          | picture              | Image        | Read-only  | Camera Live Snapshot                                                                                                                        |
| live          | picture-url          | String       | Read-only  | Url of the live snapshot for this camera                                                                                                    |
| live          | local-stream-url (*) | String       | Read-only  | Local Url of the live stream for this camera (accessible if openhab server and camera are located on the same lan.                          |
| live          | vpn-stream-url (*)   | String       | Read-only  | Url of the live stream for this camera through Netatmo VPN.                                                                                 |
| signal        | strength             | Number       | Read-only  | Signal strength (0 for no signal, 1 for weak...)                                                                                            |
| signal        | value                | Number:Power | Read-only  | Signal strength in dBm                                                                                                                      |
| presence      | floodlight           | Switch       | Read-write | Sets the floodlight to ON/OFF/AUTO                                                                                                          |
| last-event    | type                 | String       | Read-only  | Type of event                                                                                                                               |
| last-event    | subtype              | String       | Read-only  | Sub-type of event                                                                                                                           |
| last-event    | time                 | DateTime     | Read-only  | Time of occurrence of event                                                                                                                 |
| last-event    | message              | String       | Read-only  | Message sent by Netatmo corresponding to given event                                                                                        |
| last-event    | snapshot             | Image        | Read-only  | picture of the last event, if it applies                                                                                                    |
| last-event    | snapshot-url         | String       | Read-only  | if the last event (depending upon event type) in the home lead a snapshot picture, the picture URL will be available here                   |
| last-event    | local-video-url      | String       | Read-only  | If the last event (depending upon event type) in the home lead a snapshot picture, the corresponding local video URL will be available here |
| last-event    | vpn-video-url        | String       | Read-only  | If the last event (depending upon event type) in the home lead a snapshot picture, the corresponding VPN video URL will be available here   |
| last-event    | video-status         | String       | Read-only  | Status of the video (recording, deleted or available)                                                                                       |
| last-event    | person-id            | String       | Read-only  | Id of the person the event is about (if any)                                                                                                |

(*) This channel is configurable : low, poor, high.

**Supported channels for the Doorbell thing:**

| Channel Group | Channel ID        | Item Type    | Read/Write | Description                                                                                                                                 |
| ------------- | ----------------- | ------------ | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| status        | sd-card           | String       | Read-only  | State of the SD card                                                                                                                        |
| status        | alim              | String       | Read-only  | State of the power connector                                                                                                                |
| live          | picture           | Image        | Read-only  | Camera Live Snapshot                                                                                                                        |
| live          | local-picture-url | String       | Read-only  | Local Url of the live snapshot for this camera                                                                                              |
| live          | vpn-picture-url   | String       | Read-only  | Url of the live snapshot for this camera through Netatmo VPN.                                                                               |
| signal        | strength          | Number       | Read-only  | Signal strength (0 for no signal, 1 for weak...)                                                                                            |
| signal        | value             | Number:Power | Read-only  | Signal strength in dBm                                                                                                                      |
| last-event    | type              | String       | Read-only  | Type of event                                                                                                                               |
| last-event    | video-status      | String       | Read-only  | Status of the video (recording, deleted or available)                                                                                       |
| last-event    | time              | DateTime     | Read-only  | Time of occurrence of event                                                                                                                 |
| last-event    | local-video-url   | String       | Read-only  | If the last event (depending upon event type) in the home lead a snapshot picture, the corresponding local video URL will be available here |
| last-event    | vpn-video-url     | String       | Read-only  | If the last event (depending upon event type) in the home lead a snapshot picture, the corresponding VPN video URL will be available here   |
| sub-event     | type              | String       | Read-only  | Type of sub-event                                                                                                                           |
| sub-event     | time              | DateTime     | Read-only  | Time of occurrence of sub-event                                                                                                             |
| sub-event     | message           | String       | Read-only  | Message sent by Netatmo corresponding to given sub-event                                                                                    |
| sub-event     | snapshot-url      | String       | Read-only  | Depending upon event type in the home, a snapshot picture of the corresponding local video URL will be available here                       |
| sub-event     | vignette-url      | String       | Read-only  | A vignette representing the snapshot                                                                                                        |
| sub-event     | snapshot          | Image        | Read-only  | picture of the snapshot                                                                                                                     |
| sub-event     | vignet            | Image        | Read-only  | picture of the vignette                                                                                                                     |

Note: live feeds either locally or via VPN are not available in Netatmo API.

**Supported channels for the Siren thing:**

| Channel Group | Channel ID  | Item Type    | Read/Write | Description                                         |
| ------------- | ----------- | ------------ | ---------- | --------------------------------------------------- |
| siren         | status      | String       | Read-only  | Status of the siren, if silent or emitting an alarm |
| siren         | monitoring  | Switch       | Read-only  | State of the siren device                           |
| signal        | strength    | Number       | Read-only  | Signal strength (0 for no signal, 1 for weak...)    |
| signal        | value       | Number:Power | Read-only  | Signal strength in dBm                              |
| timestamp     | last-seen   | DateTime     | Read-only  | Last time the module reported its presence          |
| battery       | value       | Number       | Read-only  | Battery level                                       |
| battery       | low-battery | Switch       | Read-only  | Low battery                                         |

**Supported channels for the Door Tag thing:**

| Channel Group | Channel ID  | Item Type    | Read/Write | Description                                      |
| ------------- | ----------- | ------------ | ---------- | ------------------------------------------------ |
| tag           | status      | Contact      | Read-only  | Status of tag (OPEN,CLOSED)                      |
| signal        | strength    | Number       | Read-only  | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value       | Number:Power | Read-only  | Signal strength in dBm                           |
| timestamp     | last-seen   | DateTime     | Read-only  | Last time the module reported its presence       |
| battery       | value       | Number       | Read-only  | Battery level                                    |
| battery       | low-battery | Switch       | Read-only  | Low battery                                      |

### Welcome Person

Netatmo API distinguishes two kinds of persons:

- Known persons : have been identified by the camera and you have defined a name for those.
- Unknown persons : identified by the camera, but no name defined.

Person things are automatically created in discovery process for all known persons.

**Supported channels for the Person thing:**

| Channel Group | Channel ID   | Item Type | Description                                            |
| ------------- | ------------ | --------- | ------------------------------------------------------ |
| person        | avatar-url   | String    | URL for the avatar of this person                      |
| person        | avatar       | Image     | Avatar of this person                                  |
| person        | at-home      | Switch    | Indicates if this person is known to be at home or not |
| person        | last-seen    | DateTime  | Moment when this person was last seen                  |
| last-event    | subtype      | String    | Sub-type of event                                      |
| last-event    | message      | String    | Last event message from this person                    |
| last-event    | time         | DateTime  | Moment of the last event for this person               |
| last-event    | snapshot     | Image     | Picture of the last event for this person              |
| last-event    | snapshot-url | String    | URL for the picture of the last event for this person  |
| last-event    | camera-id    | String    | ID of the camera that triggered the event              |

All these channels except at-home are read only.

### Netatmo Smart Smoke Detector

All these channels are read only.

**Supported channels for the Smoke Detector thing:**

| Channel Group | Channel Id | Item Type    | Description                                      |
| ------------- | ---------- | ------------ | ------------------------------------------------ |
| signal        | strength   | Number       | Signal strength (0 for no signal, 1 for weak...) |
| signal        | value      | Number:Power | Signal strength in dBm                           |
| timestamp     | last-seen  | DateTime     | Last time the module reported its presence       |
| last-event    | type       | String       | Type of event                                    |
| last-event    | time       | DateTime     | Moment of the last event for this person         |
| last-event    | subtype    | String       | Sub-type of event                                |
| last-event    | message    | String       | Last event message from this person              |

## Configuration Examples

### things/netatmo.things

```java
Bridge netatmo:account:myaccount "Netatmo Account" [clientId="xxxxx", clientSecret="yyyy", refreshToken="zzzzz"] {
    Bridge weather-station inside "Inside Weather Station" [id="70:ee:aa:aa:aa:aa"] {
        outdoor outside   "Outside Module" [id="02:00:00:aa:aa:aa"] {
            Channels:
                Type hum-measurement : maxHumWeek [limit="MAX",period="1week"]
        }
        rain rainModule        "Rain Module"    [id="05:00:00:aa:aa:aa"] {
            Channels:
                Type sum_rain-measurement : rainThisWeek  "Rain This Week"     [period="1week"]
                Type sum_rain-measurement : rainThisMonth "Rain This Month"    [period="1month"]
        }
    }
    Bridge home myhome "My home" [ id="0123456789abcdef", refreshInterval=150 ] {
        Thing welcome mycam "My camera" [ id="70:aa:bb:cc:dd:ee" ] {
            Channels:
                Type live-stream-url : live#local-stream-url [ quality="high" ]
                Type live-stream-url : live#vpn-stream-url [ quality="low" ]
        }
    }
    Bridge home myhomeheating "Home heating" [ id="..." ] {
        Bridge plug relay "Boiler relay" [ id="..." ] {
            thermostat thermostat "Thermostat" [ id="..." ]
            valve valveoffice "Valve in office" [ id="..." ]
        }
        room office "Office" [ id="..." ]
    }
}
```

### items/netatmo.items

```java
// Indoor Module
Number:Temperature   Indoor_Temp                       "Temperature [%.1f %unit%]"                                  <temperature>      { channel = "netatmo:weather-station:myaccount:inside:temperature#value" }
Number:Temperature   Indoor_Min_Temp                   "Min Temperature Today [%.1f %unit%]"                        <temperature>      { channel = "netatmo:weather-station:myaccount:inside:temperature#min-today" }
Number:Temperature   Indoor_Max_Temp                   "Max Temperature Today [%.1f %unit%]"                        <temperature>      { channel = "netatmo:weather-station:myaccount:inside:temperature#max-today" }
Number:Dimensionless Indoor_Humidity                   "Humidity [%d %unit%]"                                       <humidity>         { channel = "netatmo:weather-station:myaccount:inside:humidity#value" }
Number               Indoor_Humidex                    "Humidex [%.0f]"                                             <temperature_hot>  { channel = "netatmo:weather-station:myaccount:inside:humidity#humidex" }
Number:Temperature   Indoor_HeatIndex                  "HeatIndex [%.1f %unit%]"                                    <temperature_hot>  { channel = "netatmo:weather-station:myaccount:inside:temperature#heat-index" }
Number:Temperature   Indoor_Dewpoint                   "Dewpoint [%.1f %unit%]"                                     <temperature_cold> { channel = "netatmo:weather-station:myaccount:inside:temperature#dewpoint" }
Number:Temperature   Indoor_DewpointDepression         "DewpointDepression [%.1f %unit%]"                           <temperature_cold> { channel = "netatmo:weather-station:myaccount:inside:temperature#dewpoint-depression" }
Number:Dimensionless Indoor_Co2                        "CO2 [%d %unit%]"                                            <carbondioxide>    { channel = "netatmo:weather-station:myaccount:inside:airquality#co2" }
Number:Pressure      Indoor_Pressure                   "Pressure [%.1f %unit%]"                                     <pressure>         { channel = "netatmo:weather-station:myaccount:inside:pressure#value" }
Number:Pressure      Indoor_AbsolutePressure           "AbsolutePressure [%.1f %unit%]"                             <pressure>         { channel = "netatmo:weather-station:myaccount:inside:pressure#absolute" }
Number:Dimensionless Indoor_Noise                      "Noise [%d %unit%]"                                          <soundvolume>      { channel = "netatmo:weather-station:myaccount:inside:noise#value" }
Number               Indoor_RadioStatus                 "RadioStatus [%s]"                                          <signal>           { channel = "netatmo:weather-station:myaccount:inside:signal#strength" }
DateTime             Indoor_TimeStamp                  "TimeStamp [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"                  <calendar>         { channel = "netatmo:weather-station:myaccount:inside:timestamp#measures" }
DateTime             Indoor_LastSeen            "LastSeen [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"                          <text>             { channel = "netatmo:weather-station:myaccount:inside:timestamp#last-seen" }

// Outdoor Module
Number:Temperature   Outdoor_Temperature               "Temperature [%.1f %unit%]"                                  <temperature>      { channel = "netatmo:outdoor:myaccount:inside:outside:temperature#value" }
String               Outdoor_TempTrend                 "TempTrend [%s]"                                             <line>             { channel = "netatmo:outdoor:myaccount:inside:outside:temperature#trend" }
Number:Dimensionless Outdoor_Humidity                  "Humidity [%d %unit%]"                                       <humidity>         { channel = "netatmo:outdoor:myaccount:inside:outside:humidity#value" }
Number               Outdoor_Humidex                   "Humidex [%.0f]"                                             <temperature_hot>  { channel = "netatmo:outdoor:myaccount:inside:outside:humidity#humidex" }
Number:Temperature   Outdoor_HeatIndex                 "heat-index [%.1f %unit%]"                                   <temperature_hot>  { channel = "netatmo:outdoor:myaccount:inside:outside:temperature#heat-index" }
Number:Temperature   Outdoor_Dewpoint                  "Dewpoint [%.1f %unit%]"                                     <temperature_cold> { channel = "netatmo:outdoor:myaccount:inside:outside:temperature#dewpoint" }
Number:Temperature   Outdoor_DewpointDepression        "DewpointDepression [%.1f %unit%]"                           <temperature_cold> { channel = "netatmo:outdoor:myaccount:inside:outside:temperature#dewpoint-depression" }
Number               Outdoor_RadioStatus               "RfStatus [%.0f / 5]"                                        <signal>           { channel = "netatmo:outdoor:myaccount:inside:outside:signal#strength" }
Switch               Outdoor_LowBattery                "LowBattery [%s]"                                            <siren>            { channel = "netatmo:outdoor:myaccount:inside:outside:battery#low-battery" }
DateTime             Outdoor_TimeStamp                 "Measures TimeStamp [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"         <calendar>         { channel = "netatmo:outdoor:myaccount:inside:outside:timestamp#measures" }
DateTime             Outdoor_LastMessage               "LastMessage [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"                <text>             { channel = "netatmo:outdoor:myaccount:inside:outside:timestamp#last-seen" }

// Rain Module
Number:Speed         Rain_Intensity                    "Rain Intensity [%.1f %unit%]"                               <rain>             { channel = "netatmo:rain:myaccount:inside:rainModule:rain#value"}
Number:Length        Rain_Hour                         "Rain Last Hour [%.1f %unit%]"                               <rain>             { channel = "netatmo:rain:myaccount:inside:rainModule:rain#sum-1"}
Number:Length        Rain_Today                        "Rain Today [%.1f %unit%]"                                   <rain>             { channel = "netatmo:rain:myaccount:inside:rainModule:rain#sum-24"}

// Camera
Switch               CameraMonitoring                  "Monitoring"                                                 <switch>           { channel = "netatmo:welcome:myaccount:myhome:mycam:status#monitoring", autoupdate="false" }
String               CameraAlimState                   "Alim State [%s]"                                            <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:status#alim" }
String               CameraSDCardState                 "SD Card State [%s]"                                         <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:status#sd-card" }
Image                CameraLiveSnapshot                "Live Snapshot"                                              <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:live#picture" }
String               CameraLiveStreamLocalUrl          "Live Video Stream [%s]"                                     <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:live#local-stream-url" }
String               CameraLiveStreamVpnUrl            "Live Video Stream [%s]"                                     <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:live#vpn-stream-url" }
Number               CameraWifiStrength                "Wi-Fi Strength [%s]"                                        <wifi>             { channel = "netatmo:welcome:myaccount:myhome:mycam:signal#strength" }
DateTime             CameraEventTime                   "Event Timestamp [%1$tb %1$td %1$tH:%1$tM]"                  <time>             { channel = "netatmo:welcome:myaccount:myhome:mycam:last-event#time" }
String               CameraEventType                   "Event Type [%s]"                                            <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:last-event#type" }
String               CameraEventMessage                "Event Message [%s]"                                         <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:last-event#message" }
Image                CameraEventSnapshot               "Event Snapshot"                                             <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:last-event#snapshot" }
String               CameraEventStreamLocalUrl         "Event Video Stream [%s]"                                    <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:last-event#local-video-url" }
String               CameraEventStreamVpnUrl           "Event Video Stream [%s]"                                    <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:last-event#vpn-video-url" }
String               CameraEventVideoStatus            "Video Status [%s]"                                          <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:last-event#video-status" }
String               CameraEventPersonId               "Person Id [%s]"                                             <none>             { channel = "netatmo:welcome:myaccount:myhome:mycam:last-event#person-id" }
```

### sitemaps/netatmo.sitemap

```perl
sitemap netatmo label="Netatmo" {
    Frame label="Indoor" {
        Text item=Indoor_Temp
        Text item=Indoor_Min_Temp
        Text item=Indoor_Max_Temp
        Text item=Indoor_Min_Temp_TS
        Text item=Indoor_Max_Temp_TS
        Text item=Indoor_Humidity
        Text item=Indoor_Humidex                     valuecolor=[<20.1="green",<29.1="blue",<28.1="yellow",<45.1="orange",<54.1="red",>54.1="maroon"]
        Text item=Indoor_HeatIndex
        Text item=Indoor_Dewpoint
        Text item=Indoor_DewpointDepression
        Text item=Indoor_Co2                        valuecolor=[<800="green",<1000="orange",<1400="red",>1399="maroon"]
        Text item=Indoor_Pressure
        Text item=Indoor_AbsolutePressure
        Text item=Indoor_Noise
        Text item=Indoor_WifiStatus
        Text item=Indoor_TimeStamp
        Text item=Indoor_LastSeen
    }
    Frame label="Outdoor" { 
        Text item=Outdoor_Temperature
        Text item=Outdoor_TempTrend
        Text item=Outdoor_Humidity
        Text item=Outdoor_Humidex                    valuecolor=[<20.1="green",<29.1="blue",<28.1="yellow",<45.1="orange",<54.1="red",>54.1="maroon"]
        Text item=Outdoor_HeatIndex
        Text item=Outdoor_Dewpoint
        Text item=Outdoor_DewpointDepression
        Text item=Outdoor_RadioStatus
        Text item=Outdoor_LowBattery
        Text item=Outdoor_BatteryVP
        Text item=Outdoor_TimeStamp
        Text item=Outdoor_LastMessage
    }
    Frame label="Rain" {
        Text item=Rain_Intensity
        Text item=Rain_Hour
        Text item=Rain_Today
        Text item=Rain_Week
        Text item=Rain_Month
        Text item=Rain_BatteryVP
    }
    Frame label="Camera" icon="camera" {
        Switch item=CameraMonitoring
        Text item=CameraAlimState
        Text item=CameraSDCardState
        Text item=CameraWifiStrength
        Image item=CameraLiveSnapshot
        Text label="Live (local)" icon="none" {
            Video url="xxxxxx" item=CameraLiveStreamLocalUrl encoding="HLS"
        }
        Text label="Live (VPN)" icon="none" {
            Video url="xxxxxx" item=CameraLiveStreamVpnUrl encoding="HLS"
        }
        Text label="Last event" icon="none" {
            Text item=CameraEventTime
            Text item=CameraEventType
            Text item=CameraEventMessage
            Text item=CameraEventPersonId
            Image item=CameraEventSnapshot
            Text item=CameraEventVideoStatus
            Text label="Video (local)" icon="none" {
                Video url="xxxxxx" item=CameraEventStreamLocalUrl encoding="HLS"
            }
            Text label="Video (VPN)" icon="none" {
                Video url="xxxxxx" item=CameraEventStreamVpnUrl encoding="HLS"
            }
        }
    }
}
```

## Rule Actions

Multiple actions are supported by this binding. In classic rules these are accessible as shown in this example (adjust getActions with your ThingId):

Example

```java
 val actions = getActions("netatmo","netatmo:room:myaccount:myhome:livingroom")
 if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
 }
```

### setThermRoomTempSetpoint(temp,endtime)

Sends a temperature setpoint (and switch to manual mode) to the thermostat for a room with an end time.

Parameters:

| Name    | Description                                                |
| ------- | ---------------------------------------------------------- |
| temp    | The temperature setpoint.                                  |
| endtime | Time the setpoint should end (Local Unix time in seconds). |

Example:

```java
actions.setThermRoomTempSetpoint(19.0, 1654387205)
```

### setThermRoomModeSetpoint(mode,endtime)

Sends a mode to the thermostat for a room with an optional end time.

Parameters:

| Name    | Description                                                |
| ------- | ---------------------------------------------------------- |
| mode    | The mode to set: MANUAL, MAX or HOME.                      |
| endtime | Time the setpoint should end (Local Unix time in seconds). |

Example:

```java
actions.setThermRoomModeSetpoint("MANUAL", 1654387205)
actions.setThermRoomModeSetpoint("HOME", null)
```

## Sample data

If you want to evaluate this binding but have not got a Netatmo station yourself
yet, you can search on the web for a publicly shared weather station.

## Icons

The following icons are used by original Netatmo web app:

### Modules

- `https://my.netatmo.com/images/my/app/module_int.png`
- `https://my.netatmo.com/images/my/app/module_ext.png`
- `https://my.netatmo.com/images/my/app/module_rain.png`

### Battery status

- `https://my.netatmo.com/images/my/app/battery_verylow.png`
- `https://my.netatmo.com/images/my/app/battery_low.png`
- `https://my.netatmo.com/images/my/app/battery_medium.png`
- `https://my.netatmo.com/images/my/app/battery_high.png`
- `https://my.netatmo.com/images/my/app/battery_full.png`

### Signal status

- `https://my.netatmo.com/images/my/app/signal_verylow.png`
- `https://my.netatmo.com/images/my/app/signal_low.png`
- `https://my.netatmo.com/images/my/app/signal_medium.png`
- `https://my.netatmo.com/images/my/app/signal_high.png`
- `https://my.netatmo.com/images/my/app/signal_full.png`

### Wifi status

- `https://my.netatmo.com/images/my/app/wifi_low.png`
- `https://my.netatmo.com/images/my/app/wifi_medium.png`
- `https://my.netatmo.com/images/my/app/wifi_high.png`
- `https://my.netatmo.com/images/my/app/wifi_full.png`
