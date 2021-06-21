# Netatmo Binding

The Netatmo binding integrates the following Netatmo products:

- *Personal Weather Station*. Reports temperature, humidity, air pressure, carbon dioxide concentration in the air, as well as the ambient noise level.
- *Thermostat*. Reports ambient temperature, allow to check target temperature, consult and change furnace heating status.
- *Indoor Camera / Welcome*. Reports last event and persons at home, consult picture and video from event/camera.
- *Outdoor Camera / Presence*. Reports last event, consult picture and video from event/camera.

See http://www.netatmo.com/ for details on their product.

## Binding Configuration

Before setting up your 'Things', you will have to grant openHAB to access Netatmo API.
Here is the procedure:

Create an application at https://dev.netatmo.com/dev/createapp

The variables you will need to get to setup the binding are:

* `<CLIENT_ID>` Your client ID taken from your App at https://dev.netatmo.com/dev/listapps
* `<CLIENT_SECRET>` A token provided along with the `<CLIENT_ID>`.
* `<USERNAME>` The username you use to connect to the Netatmo API (usually your mail address).
* `<PASSWORD>` The password attached to the above username.


The binding has the following configuration options:

-   **clientId:** Client ID provided for the application you created on http://dev.netatmo.com/createapp.
-   **clientSecret:**  Client Secret provided for the application you created.
-   **username:** Your Netatmo API username (email).
-   **password:** Your Netatmo API password.
-   **webHookUrl:** Protocol, public IP and port to access OH2 server from Internet.
-   **reconnectInterval:** The reconnection interval to Netatmo API (in s).
-   **backgroundDiscovery:** If set to true, the device and its associated modules are updated in the discovery inbox at each API call run to refresh device data. Default is false.

Create a `<openHAB-conf>/services/netatmo.cfg` file and use the above options like this:

```
binding.netatmo:clientId=ezezsdfdssfhdreytr
binding.netatmo:clientSecret=dshfdkfdfkshdkj
binding.netatmo:username=mail@mail.com
binding.netatmo:password=dssdsdsd
```


### 2. Things Configuration

### Webhook

Netatmo servers can send push notifications to the Netatmo Binding by using a callback URL.
The webhook URL is setup at binding level using "Webhook Address" parameter.
You will define here public way to access your openHAB server:

```
http(s)://xx.yy.zz.ww:443
```

Your Netatmo App will be configured automatically by the bridge to the endpoint : 

```
http(s)://xx.yy.zz.ww:443/netatmo
```

Please be aware of Netatmo own limits regarding webhook usage that lead to a 24h ban-time when webhook does not answer 5 times.

NB : Allowed ports for webhooks are 80, 88, 443 and 9443.


### Configure Things

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

append ":00:00:",

split the rest into three parts of two characters and append with a colon as delimiter.

For example your serial number "h00bcdc" should end up as "02:00:00:00:bc:dc".


## Discovery

If you did not manually create things in the *.things file, the Netatmo Binding is able to discover automatically all depending modules and devices from Netatmo website.


## Channels


### Weather Station Main Indoor Device

Weather station does not need any refreshInterval setting.
Based on a standard update period of 10mn by Netatmo systems - it will auto adapt to stick closest as possible to last data availability.

Example item for the **indoor module**:

```
Number Netatmo_Indoor_CO2 "CO2" <carbondioxide> { channel = "netatmo:NAMain:home:inside:Co2" }
```

**Supported channels for the main indoor module:**

| Channel ID          | Item Type            | Description                                              |
|---------------------|----------------------|----------------------------------------------------------|
| Co2                 | Number:Dimensionless | Air quality                                              |
| MinCo2              | Number:Dimensionless | Minimum CO2 on current day                               |
| MaxCo2              | Number:Dimensionless | Maximum CO2 on current day                               |
| DateMinCo2          | DateTime             | Date when minimum CO2 was reached on current day         |
| DateMaxCo2          | DateTime             | Date when maximum CO2 was reached on current day         |
| temperature         | Number:Temperature   | Current temperature                                      |
| temperature-trend           | String               | Temperature evolution trend (up, down, stable)           |
| Noise               | Number:Dimensionless | Current noise level                                      |
| MinNoise            | Number:Dimensionless | Minimum noise on current day                             |
| MaxNoise            | Number:Dimensionless | Maximum noise on current day                             |
| DateMinNoise        | DateTime             | Date when minimum noise was reached on current day       |
| DateMaxNoise        | DateTime             | Date when maximum noise was reached on current day       |
| Pressure            | Number:Pressure      | Current pressure                                         |
| MinPressure         | Number:Pressure      | Minimum pressure on current day                          |
| MaxPressure         | Number:Pressure      | Maximum pressure on current day                          |
| DateMinPressure     | DateTime             | Date when minimum pressure was reached on current day    |
| DateMaxPressure     | DateTime             | Date when maximum pressure was reached on current day    |
| pressure-trend      | String               | Pressure evolution trend for last 12h (up, down, stable) |
| absolute-pressure    | Number:Pressure      | Absolute pressure                                        |
| humidity            | Number:Dimensionless | Current humidity                                         |
| MinHumidity         | Number:Dimensionless | Minimum humidity on current day                          |
| MaxHumidity         | Number:Dimensionless | Maximum humidity on current day                          |
| DateMinHumidity     | DateTime             | Date when minimum humidity was reached on current day    |
| DateMaxHumidity     | DateTime             | Date when maximum humidity was reached on current day    |
| humidex             | Number               | Computed Humidex index                                   |
| heat-index           | Number:Temperature   | Computed Heat Index                                      |
| dewpoint            | Number:Temperature   | Computed dewpoint temperature                            |
| dew-point-depression  | Number:Temperature   | Computed dewpoint depression                             |
| today-min             | Number:Temperature   | Minimum temperature on current day                       |
| today-max             | Number:Temperature   | Maximum temperature on current day                       |
| ever-min         | DateTime             | Date when minimum temperature was reached on current day |
| ever-max         | DateTime             | Date when maximum temperature was reached on current day |
| timestamp           | DateTime             | Timestamp when data was measured                         |
| last-seen            | DateTime             | Last status store                                        |
| signal-strength     | Number               | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |
| location            | Location             | Location of the device                                   |

All these channels are read only.


### Weather Station Outdoor module

Example item for the **outdoor module** 

```
Number Netatmo_Outdoor_Temperature "Temperature" { channel = "netatmo:NAModule1:home:outside:Temperature" }
```

**Supported channels for the outdoor module:**

| Channel ID          | Item Type            | Description                                              |
|---------------------|----------------------|----------------------------------------------------------|
| temperature         | Number:Temperature   | Current temperature                                      |
| temperature-trend           | String               | Temperature evolution trend (up, down, stable)           |
| humidity            | Number:Dimensionless | Current humidity                                         |
| MinHumidity         | Number:Dimensionless | Minimum humidity on current day                          |
| MaxHumidity         | Number:Dimensionless | Maximum humidity on current day                          |
| DateMinHumidity     | DateTime             | Date when minimum humidity was reached on current day    |
| DateMaxHumidity     | DateTime             | Date when maximum humidity was reached on current day    |
| humidex             | Number               | Computed Humidex index                                   |
| heat-index           | Number:Temperature   | Computed Heat Index                                      |
| Dewpoint            | Number:Temperature   | Computed dewpoint temperature                            |
|dew-point-depression  | Number:Temperature   | Computed dewpoint depression                             |
| today-min             | Number:Temperature   | Minimum temperature on current day                       |
| today-max             | Number:Temperature   | Maximum temperature on current day                       |
| DateMinTemp         | DateTime             | Date when minimum temperature was reached on current day |
| DateMaxTemp         | DateTime             | Date when maximum temperature was reached on current day |
| timestamp           | DateTime             | Timestamp when data was measured                         |
| last-message         | DateTime             | Last message emitted by the module                       |
| LowBattery          | Switch               | Low battery                                              |
| battery-level           | Number               | Battery level                                            |
| signal-strength     | Number               | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |

All these channels are read only.


### Weather Station Additional Indoor module

Example item for the **indoor module** 

```
Number Netatmo_Indoor2_Temperature "Temperature" { channel = "netatmo:NAModule4:home:insidesupp:Temperature" }
```

**Supported channels for the additional indoor module:**

| Channel ID          | Item Type            | Description                                              |
|---------------------|----------------------|----------------------------------------------------------|
| Co2                 | Number:Dimensionless | Air quality                                              |
| MinCo2              | Number:Dimensionless | Minimum CO2 on current day                               |
| MaxCo2              | Number:Dimensionless | Maximum CO2 on current day                               |
| DateMinCo2          | DateTime             | Date when minimum CO2 was reached on current day         |
| DateMaxCo2          | DateTime             | Date when maximum CO2 was reached on current day         |
| temperature         | Number:Temperature   | Current temperature                                      |
| temperature-trend           | String               | Temperature evolution trend (up, down, stable)           |
| humidity            | Number:Dimensionless | Current humidity                                         |
| MinHumidity         | Number:Dimensionless | Minimum humidity on current day                          |
| MaxHumidity         | Number:Dimensionless | Maximum humidity on current day                          |
| DateMinHumidity     | DateTime             | Date when minimum humidity was reached on current day    |
| DateMaxHumidity     | DateTime             | Date when maximum humidity was reached on current day    |
| humidex             | Number               | Computed Humidex index                                   |
| heat-index           | Number:Temperature   | Computed Heat Index                                      |
| Dewpoint            | Number:Temperature   | Computed dewpoint temperature                            |
| dew-point-depression  | Number:Temperature   | Computed dewpoint depression                             |
| today-min             | Number:Temperature   | Minimum temperature on current day                       |
| today-max             | Number:Temperature   | Maximum temperature on current day                       |
| ever-min         | DateTime             | Date when minimum temperature was reached on current day |
| ever-max         | DateTime             | Date when maximum temperature was reached on current day |
| timestamp           | DateTime             | Timestamp when data was measured                         |
| last-message         | DateTime             | Last message emitted by the module                       |
| LowBattery          | Switch               | Low battery                                              |
| battery-level           | Number               | Battery level                                            |
| signal-strength     | Number               | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |

All these channels are read only.


### Rain Gauge

Example item for the **rain gauge**

```
Number Netatmo_Rain_Current "Rain [%.1f mm]" { channel = "netatmo:NAModule3:home:rain:rain" }
```

**Supported channels for the rain guage:**

| Channel ID          | Item Type     | Description                                              |
|---------------------|---------------|----------------------------------------------------------|
| rain                | Number:Length | Quantity of water                                        |
| rain-sum-1            | Number:Length | Quantity of water on last hour                           |
| rain-sum-24           | Number:Length | Quantity of water on last day                            |
| timestamp           | DateTime      | Timestamp when data was measured                         |
| last-message         | DateTime      | Last message emitted by the module                       |
| LowBattery          | Switch        | Low battery                                              |
| battery-level           | Number        | Battery level                                            |
| signal-strength     | Number        | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |

All these channels are read only.


### Weather Station Wind module

Example item for the **wind module**:

```
Number Netatmo_Wind_Strength "Wind Strength [%.0f KPH]" { channel = "netatmo:NAModule2:home:wind:wind-strength" }
```

**Supported channels for the wind module:**

| Channel ID          | Item Type    | Description                                              |
|---------------------|--------------|----------------------------------------------------------|
| wind-angle           | Number:Angle | Current 5 minutes average wind direction                 |
| wind-strength        | Number:Speed | Current 5 minutes average wind speed                     |
| GustAngle           | Number:Angle | Direction of the last 5 minutes highest gust wind        |
| GustStrength        | Number:Speed | Speed of the last 5 minutes highest gust wind            |
| timestamp           | DateTime     | Timestamp when data was measured                         |
| last-message         | DateTime     | Last message emitted by the module                       |
| LowBattery          | Switch       | Low battery                                              |
| battery-level           | Number       | Battery level                                            |
| signal-strength     | Number       | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |
| max-strength     | Number:Speed | Maximum wind strength recorded                           |
| date-max-strength | DateTime     | Timestamp when MaxWindStrength was recorded              |

All these channels are read only.


### Healthy Home Coach Device

Example item for the **Healthy Home Coach**:

```
String Netatmo_LivingRoom_HomeCoach_HealthIndex "Climate" { channel = "netatmo:NHC:home:livingroom:health-index" }
```

**Supported channels for the healthy home coach device:**

| Channel ID          | Item Type            | Description                                              |
|---------------------|----------------------|-------------------------------------------------------------------------|
| health-index        | Number               | Health index (0 : healthy, 1 : fine, 2 : fair, 3 : poor, 4 : unhealthy) |
| Co2                 | Number:Dimensionless | Air quality                                                             |
| temperature         | Number:Temperature   | Current temperature                                                     |
| temperature-trend           | String               | Temperature evolution trend (up, down, stable)                          |
| Noise               | Number:Dimensionless | Current noise level                                                     |
| Pressure            | Number:Pressure      | Current pressure                                                        |
| pressure-trend      | String               | Pressure evolution trend for last 12h (up, down, stable)                |
| absolute-pressure   | Number:Pressure      | Absolute pressure                                                       |
| humidity            | Number:Dimensionless | Current humidity                                                        |
| today-min             | Number:Temperature   | Minimum temperature on current day                                      |
| today-max             | Number:Temperature   | Maximum temperature on current day                                      |
| ever-min         | DateTime             | Date when minimum temperature was reached on current day                |
| ever-max         | DateTime             | Date when maximum temperature was reached on current day                |
| timestamp           | DateTime             | Timestamp when data was measured                                        |
| last-seen            | DateTime             | Last status store                                                       |
| RadioStatus         | Number               | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |
| location            | Location             | Location of the device                                                  |

All these channels are read only.


### Thermostat Relay Device

**Supported channels for the thermostat relay device:**

| Channel ID          | Item Type   | Description                                              |
|---------------------|-------------|----------------------------------------------------------|
| ConnectedBoiler     | Contact     | Plug connected boiler                                    |
| last-seen            | DateTime    | Last plug seen                                           |
| last-bilan           | DateTime    | Month of the last available thermostat bilan             |
| signal-strength     | Number      | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |
| location            | Location    | Location of the device                                   |
| setpoint-duration   | Number:Time | Default setpoint duration when manually set.             |
| planning            | String      | Id of the currently active planning when mode = program  |

All these channels are read only.


### Thermostat Module

**Supported channels for the thermostat module:**

| Channel ID          | Item Type          | Description                                                |
|---------------------|--------------------|------------------------------------------------------------|
| temperature         | Number:Temperature | Current temperature                                        |
| setpoint            | Number:Temperature | Thermostat temperature setpoint                            |
| setpoint-mode       | String             | Chosen setpoint_mode (program, away, hg, manual, off, max) |
| ThermRelayCmd       | Switch             | Indicates whether the furnace is heating or not            |
| anticipating   | Switch             | Indicates is anticipating the schedule                     |
| ThermOrientation    | Number:Angle       | Physical orientation of the thermostat module              |
| timestamp           | DateTime           | Timestamp when data was measured                           |
| setpoint-end     | DateTime           | Thermostat goes back to schedule after that timestamp      |
| last-message         | DateTime           | Last message emitted by the module                         |
| LowBattery          | Switch             | Low battery                                                |
| battery-level           | Number             | Battery level                                              |
| signal-strength     | Number             | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |

All these channels except setpoint, setpoint-mode and planning are read only.

### Valve Module

**Supported channels for the Valve module:**

| Channel ID          | Item Type          | Description                                                |
|---------------------|--------------------|------------------------------------------------------------|


### Welcome Home

All these channels are read only.

**Supported channels for the Home thing:**

| Channel ID               | Item Type | Description                                              |
|--------------------------|-----------|----------------------------------------------------------|
| welcomeHomeCity          | String    | City of the home                                         |
| welcomeHomeCountry       | String    | Country of the home                                      |
| welcomeHomeTimezone      | String    | Timezone of the home                                     |
| welcomeHomePersonCount   | Number    | Total number of Persons that are at home                 |
| welcomeHomeUnknownCount  | Number    | Count how many Unknown Persons are at home               |
| welcomeEventType         | String    | Type of event                                            |
| welcomeEventTime         | DateTime  | Time of occurrence of event                               |
| welcomeEventCameraId     | String    | Camera that detected the event                           |
| welcomeEventPersonId     | String    | Id of the person the event is about (if any)             |
| welcomeEventSnapshot     | Image     | picture of the last event, if it applies                 |
| welcomeEventSnapshotURL  | String    | if the last event (depending upon event type) in the home lead a snapshot picture, the picture URL will be available here |
| welcomeEventVideoURL     | String    | if the last event (depending upon event type) in the home lead a snapshot picture, the corresponding video URL will be available here |
| welcomeEventVideoStatus  | String    | Status of the video (recording, deleted or available)    |
| welcomeEventIsArrival    | Switch    | If person was considered "away" before being seen during this event |
| welcomeEventMessage      | String    | Message sent by Netatmo corresponding to given event     |
| welcomeEventSubType      | String    | Sub-type of SD and Alim events                           |

**Supported trigger channels for the Home thing:**

| Channel Type ID  | Options                | Description                                           |
|------------------|------------------------|-------------------------------------------------------|
| cameraEvent      |                        | A camera event is triggered with a short delay but without requiring a webhook. The information of the event can get retrieved from the other "welcomeEvent" home thing channels |
|                  | HUMAN                  | Triggered when a human (or person) was detected       |
|                  | ANIMAL                 | Triggered when an animal was detected                 |
|                  | MOVEMENT               | Triggered when an unspecified movement was detected   |
|                  | VEHICLE                | Triggered when a vehicle was detected                 |
| welcomeHomeEvent |                        | A welcome home event is triggered directly via a configured webhook |
|                  | PERSON                 | Triggered when a concrete person was detected         |
|                  | PERSON_AWAY            | Triggered when a concrete person leaves               |
|                  | MOVEMENT               | Triggered when a movement was detected                |
|                  | CONNECTION             | Triggered when a camera connection gets created       |
|                  | DISCONNECTION          | Triggered when a camera connection got lost           |
|                  | ON                     | Triggered when camera monitoring is switched on       |
|                  | OFF                    | Triggered when camera monitoring is switched off      |
|                  | BOOT                   | Triggered when a camera is booting                    |
|                  | SD                     | Triggered when a camera SD card status was changed    |
|                  | ALIM                   | Triggered when a power supply status was changed      |
|                  | NEW_MODULE             | Triggered when a new module was discovered            |
|                  | MODULE_CONNECT         | Triggered when a module gets connected                |
|                  | MODULE_DISCONNECT      | Triggered when a module gets disconnected             |
|                  | MODULE_LOW_BATTERY     | Triggered when the battery of a module gets low       |
|                  | MODULE_END_UPDATE      | Triggered when a firmware update of a module is done  |
|                  | TAG_BIG_MOVE           | Triggered when a big movement of a tag was detected   |
|                  | TAG_SMALL_MOVE         | Triggered when a small movement of a tag was detected |
|                  | TAG_UNINSTALLED        | Triggered when a tag gets uninstalled                 |
|                  | TAG_OPEN               | Triggered when an open event of a tag was detected    |

### Welcome and Presence Camera

Warnings:

- The URL of the live snapshot is a fixed URL so the value of the channel cameraLivePictureUrl / welcomeCameraLivePictureUrl will never be updated once first set by the binding. So to get a refreshed picture, you need to use the refresh parameter in your sitemap image element.
- Some features like the video surveillance are accessed via the local network, so it may be helpful to set a static IP address for the camera within your local network.

**Supported channels for the Welcome Camera thing:**

| Channel ID                  | Item Type | Read/Write | Description                                                  |
|-----------------------------|-----------|------------|--------------------------------------------------------------|
| welcomeCameraStatus         | Switch    | Read-write | State of the camera (video surveillance on/off)              |
| welcomeCameraSdStatus       | Switch    | Read-only  | State of the SD card                                         |
| welcomeCameraAlimStatus     | Switch    | Read-only  | State of the power connector                                 |
| welcomeCameraIsLocal        | Switch    | Read-only  | indicates whether the camera is on the same network than the openHAB Netatmo Binding |
| welcomeCameraLivePicture    | Image     | Read-only  | Camera Live Snapshot                                         |
| welcomeCameraLivePictureUrl | String    | Read-only  | Url of the live snapshot for this camera                     |
| welcomeCameraLiveStreamUrl  | String    | Read-only  | Url of the live stream for this camera                       |

**Supported channels for the Presence Camera thing:**

Warnings:

- The floodlight auto-mode (auto-mode) isn't updated it is changed by another application. Therefore the binding handles its own state of the auto-mode. This has the advantage that the user can define its own floodlight switch off behaviour.

| Channel ID                  | Item Type | Read/Write | Description                                                  |
|-----------------------------|-----------|------------|--------------------------------------------------------------|
| cameraStatus                | Switch    | Read-write | State of the camera (video surveillance on/off)              |
| cameraSdStatus              | Switch    | Read-only  | State of the SD card                                         |
| cameraAlimStatus            | Switch    | Read-only  | State of the power connector                                 |
| cameraIsLocal               | Switch    | Read-only  | indicates whether the camera is on the same network than the openHAB Netatmo Binding |
| cameraLivePicture           | Image     | Read-only  | Camera Live Snapshot                                         |
| cameraLivePictureUrl        | String    | Read-only  | Url of the live snapshot for this camera                     |
| cameraLiveStreamUrl         | String    | Read-only  | Url of the live stream for this camera                       |
| auto-mode    | Switch    | Read-write | When set the floodlight gets switched to auto instead of off |
| floodlight            | Switch    | Read-write | Switch for the floodlight                                    |


### Welcome Person

Netatmo API distinguishes two kinds of persons:

* Known persons : have been identified by the camera and you have defined a name for those.
* Unknown persons : identified by the camera, but no name defined.

Person things are automatically created in discovery process for all known persons.

**Supported channels for the Person thing:**

| Channel ID                    | Item Type | Description                                            |
|-------------------------------|-----------|--------------------------------------------------------|
| last-seen                      | DateTime  | Time when this person was last seen                    |
| welcomePersonAtHome           | Switch    | Indicates if this person is known to be at home or not |
| welcomePersonAvatarUrl        | String    | URL for the avatar of this person                      |
| welcomePersonAvatar           | Image     | Avatar of this person                                  |
| welcomePersonLastEventMessage | String    | Last event message from this person                    |
| welcomePersonLastEventTime    | DateTime  | Last event message time for this person                |
| welcomePersonLastEventUrl     | String    | URL for the picture of the last event for this person  |
| welcomePersonLastEvent        | Image     | Picture of the last event for this person              |

All these channels except welcomePersonAtHome are read only.

# Configuration Examples


## things/netatmo.things

```
// Bridge configuration:
Bridge netatmo:netatmoapi:home "Netatmo API" [ clientId="*********", clientSecret="**********", username = "mail@example.com", password = "******", readStation=true, readThermostat=false] {
    // Thing configuration:
    Thing NAMain inside "Netatmo Inside"       [ id="aa:aa:aa:aa:aa:aa" ]
    Thing NAModule1 outside "Netatmo Outside"  [ id="bb:bb:bb:bb:bb:bb", parentId="aa:aa:aa:aa:aa:aa" ]
    Thing NAModule3 rain "Netatmo Rain"        [ id="cc:cc:cc:cc:cc:cc", parentId="aa:aa:aa:aa:aa:aa" ]
}
```

## items/netatmo.items

```
# Indoor Module
Number:Temperature   Indoor_Temp                       "Temperature [%.1f %unit%]"                                  <temperature>      { channel = "netatmo:NAMain:home:inside:temperature" }
Number:Temperature   Indoor_Min_Temp                   "Min Temperature Today [%.1f %unit%]"                        <temperature>      { channel = "netatmo:NAMain:home:inside:today-min" }
Number:Temperature   Indoor_Max_Temp                   "Max Temperature Today [%.1f %unit%]"                        <temperature>      { channel = "netatmo:NAMain:home:inside:today-max" }
DateTime             Indoor_Min_Temp_TS                "Min Temperature Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"      <calendar>         { channel = "netatmo:NAMain:home:inside:ever-min" }
DateTime             Indoor_Max_Temp_TS                "Max Temperature Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"      <calendar>         { channel = "netatmo:NAMain:home:inside:ever-max" }
Number:Dimensionless Indoor_Humidity                   "Humidity [%d %unit%]"                                       <humidity>         { channel = "netatmo:NAMain:home:inside:humidity" }
Number:Dimensionless Indoor_Min_Humidity               "Min Humidity Today [%d %unit%]"                             <humidity>         { channel = "netatmo:NAMain:home:inside:MinHumidity" }
Number:Dimensionless Indoor_Max_Humidity               "Max Humidity Today [%d %unit%]"                             <humidity>         { channel = "netatmo:NAMain:home:inside:MaxHumidity" }
DateTime             Indoor_Min_Humidity_TS            "Min Humidity Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"         <calendar>         { channel = "netatmo:NAMain:home:inside:DateMinHumidity" }
DateTime             Indoor_Max_Humidity_TS            "Max Humidity Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"         <calendar>         { channel = "netatmo:NAMain:home:inside:DateMaxHumidity" }
Number               Indoor_Humidex                    "Humidex [%.0f]"                                             <temperature_hot>  { channel = "netatmo:NAMain:home:inside:humidex" }
Number:Temperature   Indoor_HeatIndex                  "HeatIndex [%.1f %unit%]"                                    <temperature_hot>  { channel = "netatmo:NAMain:home:inside:heat-index" }
Number:Temperature   Indoor_Dewpoint                   "Dewpoint [%.1f %unit%]"                                     <temperature_cold> { channel = "netatmo:NAMain:home:inside:Dewpoint" }
Number:Temperature   Indoor_DewpointDepression         "DewpointDepression [%.1f %unit%]"                           <temperature_cold> { channel = "netatmo:NAMain:home:inside:dew-point-depression" }
Number:Dimensionless Indoor_Co2                        "CO2 [%d %unit%]"                                            <carbondioxide>    { channel = "netatmo:NAMain:home:inside:Co2" }
Number:Dimensionless Indoor_Min_Co2                    "Min CO2 Today [%.1f %unit%]"                                <carbondioxide>    { channel = "netatmo:NAMain:home:inside:MinCo2" }
Number:Dimensionless Indoor_Max_Co2                    "Max CO2 Today [%.1f %unit%]"                                <carbondioxide>    { channel = "netatmo:NAMain:home:inside:MaxCo2" }
DateTime             Indoor_Min_Co2_TS                 "Min CO2 Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"              <calendar>         { channel = "netatmo:NAMain:home:inside:DateMinCo2" }
DateTime             Indoor_Max_Co2_TS                 "Max CO2 Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"              <calendar>         { channel = "netatmo:NAMain:home:inside:DateMaxCo2" }
Number:Pressure      Indoor_Pressure                   "Pressure [%.1f %unit%]"                                     <pressure>         { channel = "netatmo:NAMain:home:inside:Pressure" }
Number:Pressure      Indoor_Min_Pressure               "Min Pressure Today [%d %unit%]"                             <pressure>         { channel = "netatmo:NAMain:home:inside:MinPressure" }
Number:Pressure      Indoor_Max_Pressure               "Max Pressure Today [%d %unit%]"                             <pressure>         { channel = "netatmo:NAMain:home:inside:MaxPressure" }
DateTime             Indoor_Min_Pressure_TS            "Min Pressure Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"         <calendar>         { channel = "netatmo:NAMain:home:inside:DateMinPressure" }
DateTime             Indoor_Max_Pressure_TS            "Max Pressure Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"         <calendar>         { channel = "netatmo:NAMain:home:inside:DateMaxPressure" }
Number:Pressure      Indoor_AbsolutePressure           "AbsolutePressure [%.1f %unit%]"                             <pressure>         { channel = "netatmo:NAMain:home:inside:absolute-pressure" }
Number:Dimensionless Indoor_Noise                      "Noise [%d %unit%]"                                          <soundvolume>      { channel = "netatmo:NAMain:home:inside:Noise" }
Number:Dimensionless Indoor_Min_Noise                  "Min Noise Today [%.1f %unit%]"                              <soundvolume>      { channel = "netatmo:NAMain:home:inside:MinNoise" }
Number:Dimensionless Indoor_Max_Noise                  "Max Noise Today [%.1f %unit%]"                              <soundvolume>      { channel = "netatmo:NAMain:home:inside:MaxNoise" }
DateTime             Indoor_Min_Noise_TS               "Min Noise Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"            <calendar>         { channel = "netatmo:NAMain:home:inside:DateMinNoise" }
DateTime             Indoor_Max_Noise_TS               "Max Noise Today [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"            <calendar>         { channel = "netatmo:NAMain:home:inside:DateMaxNoise" }
Number               Indoor_RadioStatus                 "RadioStatus [%s]"                                            <signal>           { channel = "netatmo:NAMain:home:inside:RadioStatus" }
DateTime             Indoor_TimeStamp                  "TimeStamp [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"                  <calendar>         { channel = "netatmo:NAMain:home:inside:timestamp" }
Location             Indoor_Location                   "Location"                                                   <movecontrol>      { channel = "netatmo:NAMain:home:inside:location" }
DateTime             Indoor_LastSeen            "LastSeen [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"            <text>             { channel = "netatmo:NAMain:home:inside:last-seen" }

# Outdoor Module
Number:Temperature   Outdoor_Temperature               "Temperature [%.1f %unit%]"                                  <temperature>      { channel = "netatmo:NAModule1:home:outside:temperature" }
String               Outdoor_TempTrend                 "TempTrend [%s]"                                             <line>             { channel = "netatmo:NAModule1:home:outside:temperature-trend" }
Number:Dimensionless Outdoor_Humidity                  "Humidity [%d %unit%]"                                       <humidity>         { channel = "netatmo:NAModule1:home:outside:humidity" }
Number               Outdoor_Humidex                   "Humidex [%.0f]"                                             <temperature_hot>  { channel = "netatmo:NAModule1:home:outside:humidex" }
Number:Temperature   Outdoor_HeatIndex                 "heat-index [%.1f %unit%]"                                    <temperature_hot>  { channel = "netatmo:NAModule1:home:outside:HeatIndex" }
Number:Temperature   Outdoor_Dewpoint                  "Dewpoint [%.1f %unit%]"                                     <temperature_cold> { channel = "netatmo:NAModule1:home:outside:Dewpoint" }
Number:Temperature   Outdoor_DewpointDepression        "DewpointDepression [%.1f %unit%]"                           <temperature_cold> { channel = "netatmo:NAModule1:home:outside:dew-point-depression" }
Number               Outdoor_RadioStatus               "RfStatus [%.0f / 5]"                                        <signal>           { channel = "netatmo:NAModule1:home:outside:signal-strength" }
Switch               Outdoor_LowBattery                "LowBattery [%s]"                                            <siren>            { channel = "netatmo:NAModule1:home:outside:LowBattery" }
Number               Outdoor_BatteryVP                 "BatteryVP [%.0f %%]"                                        <battery>          { channel = "netatmo:NAModule1:home:outside:battery-level" }
DateTime             Outdoor_TimeStamp                 "TimeStamp [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"                  <calendar>         { channel = "netatmo:NAModule1:home:outside:timestamp" }
DateTime             Outdoor_LastMessage               "LastMessage [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"                <text>             { channel = "netatmo:NAModule1:home:outside:last-message" }

# Rain Module
Number:Length        Rain_Hour                         "Rain Last Hour [%.02f %unit%]"                              <rain>             {channel="netatmo:NAModule3:home:rain:rain-sum-1"}
Number:Length        Rain_Today                        "Rain Today [%.02f %unit%]"                                  <rain>             {channel="netatmo:NAModule3:home:rain:rain-sum-24"}
Number               Rain_BatteryVP                    "Rain battery status [%d%%]"                                 <battery>          {channel="netatmo:NAModule3:home:rain:battery-level"}
```

## sitemaps/netatmo.sitemap

```
sitemap netatmo label="Netatmo" {
    Frame label="Indoor" {
        Text item=Indoor_Temp
        Text item=Indoor_Min_Temp
        Text item=Indoor_Max_Temp
        Text item=Indoor_Min_Temp_TS
        Text item=Indoor_Max_Temp_TS
        Text item=Indoor_Humidity
        Text item=Indoor_Min_Humidity
        Text item=Indoor_Max_Humidity
        Text item=Indoor_Min_Humidity_TS
        Text item=Indoor_Max_Humidity_TS
        Text item=Indoor_Humidex                     valuecolor=[<20.1="green",<29.1="blue",<28.1="yellow",<45.1="orange",<54.1="red",>54.1="maroon"]
        Text item=Indoor_HeatIndex
        Text item=Indoor_Dewpoint
        Text item=Indoor_DewpointDepression
        Text item=Indoor_Co2                        valuecolor=[<800="green",<1000="orange",<1400="red",>1399="maroon"]
        Text item=Indoor_Min_Co2                    valuecolor=[<800="green",<1000="orange",<1400="red",>1399="maroon"]
        Text item=Indoor_Min_Co2_This_Week          valuecolor=[<800="green",<1000="orange",<1400="red",>1399="maroon"]
        Text item=Indoor_Min_Co2_This_Month         valuecolor=[<800="green",<1000="orange",<1400="red",>1399="maroon"]
        Text item=Indoor_Max_Co2                    valuecolor=[<800="green",<1000="orange",<1400="red",>1399="maroon"]
        Text item=Indoor_Max_Co2_This_Week          valuecolor=[<800="green",<1000="orange",<1400="red",>1399="maroon"]
        Text item=Indoor_Max_Co2_This_Month         valuecolor=[<800="green",<1000="orange",<1400="red",>1399="maroon"]
        Text item=Indoor_Min_Co2_TS
        Text item=Indoor_Max_Co2_TS
        Text item=Indoor_Pressure
        Text item=Indoor_Min_Pressure
        Text item=Indoor_Max_Pressure
        Text item=Indoor_Min_Pressure_TS
        Text item=Indoor_Max_Pressure_TS
        Text item=Indoor_AbsolutePressure
        Text item=Indoor_Noise
        Text item=Indoor_Min_Noise
        Text item=Indoor_Max_Noise
        Text item=Indoor_Min_Noise_TS
        Text item=Indoor_Max_Noise_TS
        Text item=Indoor_WifiStatus
        Text item=Indoor_TimeStamp
        Text item=Indoor_Location
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
        Text item=Rain_Hour
        Text item=Rain_Today
        Text item=Rain_Week
        Text item=Rain_Month
        Text item=Rain_BatteryVP
    }
}
```


# Sample data

If you want to evaluate this binding but have not got a Netatmo station yourself
yet, you can add the Netatmo office in Paris to your account:

http://www.netatmo.com/en-US/addguest/index/TIQ3797dtfOmgpqUcct3/70:ee:50:00:02:20


# Icons

The following icons are used by original Netatmo web app:


## Modules

- https://my.netatmo.com/images/my/app/module_int.png
- https://my.netatmo.com/images/my/app/module_ext.png
- https://my.netatmo.com/images/my/app/module_rain.png


## Battery status

- https://my.netatmo.com/images/my/app/battery_verylow.png
- https://my.netatmo.com/images/my/app/battery_low.png
- https://my.netatmo.com/images/my/app/battery_medium.png
- https://my.netatmo.com/images/my/app/battery_high.png
- https://my.netatmo.com/images/my/app/battery_full.png


## Signal status

- https://my.netatmo.com/images/my/app/signal_verylow.png
- https://my.netatmo.com/images/my/app/signal_low.png
- https://my.netatmo.com/images/my/app/signal_medium.png
- https://my.netatmo.com/images/my/app/signal_high.png
- https://my.netatmo.com/images/my/app/signal_full.png


## Wifi status

- https://my.netatmo.com/images/my/app/wifi_low.png
- https://my.netatmo.com/images/my/app/wifi_medium.png
- https://my.netatmo.com/images/my/app/wifi_high.png
- https://my.netatmo.com/images/my/app/wifi_full.png
