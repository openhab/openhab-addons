# Netatmo Binding

The Netatmo binding integrates the following Netatmo products:

- *Personal Weather Station*. Reports temperature, humidity, air pressure, carbon dioxide concentration in the air, as well as the ambient noise level.
- *Thermostat*. Reports ambient temperature, allow to check target temperature, consult and change furnace heating status.
- *Welcome Camera*. Reports last event and persons at home, consult picture and video from event/camera.

See http://www.netatmo.com/ for details on their product.


## Binding Configuration

The binding has no configuration options itself, all configuration is done at 'Things' level but before, you'll have to grant openHAB to access Netatmo API.
Here is the procedure:

### 1. Application Creation

Create an application at https://dev.netatmo.com/dev/createapp

The variables you'll need to get to setup the binding are:

* `<CLIENT_ID>` Your client ID taken from your App at https://dev.netatmo.com/dev/listapps
* `<CLIENT_SECRET>` A token provided along with the `<CLIENT_ID>`.
* `<USERNAME>` The username you use to connect to the Netatmo API (usually your mail address).
* `<PASSWORD>` The password attached to the above username.


### 2. Bridge and Things Configuration

Once you'll get needed informations from the Netatmo API, you'll be able to configure bridge and things.

E.g.

```
Bridge netatmo:netatmoapi:home [ clientId="<CLIENT_ID>", clientSecret="<CLIENT_SECRET>", username = "<USERNAME>", password = "<PASSWORD>", readStation=true|false, readHealthyHomeCoach=true|false, readThermostat=true|false, readWelcome=true|false] {
    Thing NAMain    inside  [ id="aa:aa:aa:aa:aa:aa" ]
    Thing NAModule1 outside  [ id="yy:yy:yy:yy:yy:yy", parentId="aa:aa:aa:aa:aa:aa" ]
    Thing NHC       homecoach  [ id="cc:cc:cc:cc:cc:cc", [refreshInterval=60000] ]
    Thing NAPlug    plugtherm  [ id="bb:bb:bb:bb:bb:bb", [refreshInterval=60000] ]
    Thing NATherm1  thermostat [ id="xx:xx:xx:xx:xx:xx", parentId="bb:bb:bb:bb:bb:bb" ]
    Thing NAWelcomeHome home   [ id="58yyacaaexxxebca99x999x", refreshInterval=600000 ]
    Thing NACamera camera [ id="cc:cc:cc:cc:cc:cc", parentId="58yyacaaexxxebca99x999x" ]
    Thing NAWelcomePerson sysadmin [ id="aaaaaaaa-bbbb-cccc-eeee-zzzzzzzzzzzz", parentId="58yyacaaexxxebca99x999x" ]
    ...
}
```


### Webhook

For Welcome or Presence Camera, Netatmo servers can send push notifications to the Netatmo Binding by using a callback URL.
The webhook URL is setup at bridge level using "Webhook Address" parameter.
You'll define here public way to access your OH2 server:

```
http(s)://xx.yy.zz.ww:8080
```

Your Netatmo App will be configured automatically by the bridge to the endpoint : 

```
http(s)://xx.yy.zz.ww:8080/netatmo/%id%/camera
```

where %id% is the id of your camera thing.

Please be aware of Netatmo own limits regarding webhook usage that lead to a 24h ban-time when webhook does not answer 5 times.


### Configure Things

The IDs for the modules can be extracted from the developer documentation on the netatmo site.
First login with your user.
Then some examples of the documentation contain the **real results** of your weather station.
Get the IDs of your devices (indoor, outdoor, rain gauge) [here](https://dev.netatmo.com/doc/methods/devicelist).

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

If you don't manually create things in the *.things file, the Netatmo Binding is able to discover automatically all depending modules and devices from Netatmo website.


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
| Temperature         | Number:Temperature   | Current temperature                                      |
| TempTrend           | String               | Temperature evolution trend (up, down, stable)           |
| Noise               | Number:Dimensionless | Current noise level                                      |
| Pressure            | Number:Pressure      | Current pressure                                         |
| PressTrend          | String               | Pressure evolution trend for last 12h (up, down, stable) |
| AbsolutePressure    | Number:Pressure      | Absolute pressure                                        |
| Humidity            | Number:Dimensionless | Current humidity                                         |
| Humidex             | Number               | Computed Humidex index                                   |
| HeatIndex           | Number:Temperature   | Computed Heat Index                                      |
| Dewpoint            | Number:Temperature   | Computed dewpoint temperature                            |
| DewpointDepression  | Number:Temperature   | Computed dewpoint depression                             |
| MinTemp             | Number:Temperature   | Minimum temperature on current day                       |
| MaxTemp             | Number:Temperature   | Maximum temperature on current day                       |
| DateMinTemp         | DateTime             | Date when minimum temperature was reached on current day |
| DateMaxTemp         | DateTime             | Date when maximum temperature was reached on current day |
| TimeStamp           | DateTime             | Timestamp when data was measured                         |
| LastStatusStore     | DateTime             | Last status store                                        |
| WifiStatus          | Number               | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |
| Location            | Location             | Location of the device                                   |

All these channels are read only.


### Weather Station Outdoor module

Example item for the **outdoor module** 

```
Number Netatmo_Outdoor_Temperature "Temperature" { channel = "netatmo:NAModule1:home:outside:Temperature" }
```

**Supported channels for the outdoor module:**

| Channel ID          | Item Type            | Description                                              |
|---------------------|----------------------|----------------------------------------------------------|
| Temperature         | Number:Temperature   | Current temperature                                      |
| TempTrend           | String               | Temperature evolution trend (up, down, stable)           |
| Humidity            | Number:Dimensionless | Current humidity                                         |
| Humidex             | Number               | Computed Humidex index                                   |
| HeatIndex           | Number:Temperature   | Computed Heat Index                                      |
| Dewpoint            | Number:Temperature   | Computed dewpoint temperature                            |
| DewpointDepression  | Number:Temperature   | Computed dewpoint depression                             |
| MinTemp             | Number:Temperature   | Minimum temperature on current day                       |
| MaxTemp             | Number:Temperature   | Maximum temperature on current day                       |
| DateMinTemp         | DateTime             | Date when minimum temperature was reached on current day |
| DateMaxTemp         | DateTime             | Date when maximum temperature was reached on current day |
| TimeStamp           | DateTime             | Timestamp when data was measured                         |
| LastMessage         | DateTime             | Last message emitted by the module                       |
| LowBattery          | Switch               | Low battery                                              |
| BatteryVP           | Number               | Battery level                                            |
| RfStatus            | Number               | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |

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
| Temperature         | Number:Temperature   | Current temperature                                      |
| TempTrend           | String               | Temperature evolution trend (up, down, stable)           |
| Humidity            | Number:Dimensionless | Current humidity                                         |
| Humidex             | Number               | Computed Humidex index                                   |
| HeatIndex           | Number:Temperature   | Computed Heat Index                                      |
| Dewpoint            | Number:Temperature   | Computed dewpoint temperature                            |
| DewpointDepression  | Number:Temperature   | Computed dewpoint depression                             |
| MinTemp             | Number:Temperature   | Minimum temperature on current day                       |
| MaxTemp             | Number:Temperature   | Maximum temperature on current day                       |
| DateMinTemp         | DateTime             | Date when minimum temperature was reached on current day |
| DateMaxTemp         | DateTime             | Date when maximum temperature was reached on current day |
| TimeStamp           | DateTime             | Timestamp when data was measured                         |
| LastMessage         | DateTime             | Last message emitted by the module                       |
| LowBattery          | Switch               | Low battery                                              |
| BatteryVP           | Number               | Battery level                                            |
| RfStatus            | Number               | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |

All these channels are read only.


### Rain Gauge

Example item for the **rain gauge**

```
Number Netatmo_Rain_Current "Rain [%.1f mm]" { channel = "netatmo:NAModule3:home:rain:Rain" }
```

**Supported channels for the rain guage:**

| Channel ID          | Item Type     | Description                                              |
|---------------------|---------------|----------------------------------------------------------|
| Rain                | Number:Length | Quantity of water                                        |
| SumRain1            | Number:Length | Quantity of water on last hour                           |
| SumRain24           | Number:Length | Quantity of water on last day                            |
| TimeStamp           | DateTime      | Timestamp when data was measured                         |
| LastMessage         | DateTime      | Last message emitted by the module                       |
| LowBattery          | Switch        | Low battery                                              |
| BatteryVP           | Number        | Battery level                                            |
| RfStatus            | Number        | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |

All these channels are read only.


### Weather Station Wind module

Example item for the **wind module**:

```
Number Netatmo_Wind_Strength "Wind Strength [%.0f KPH]" { channel = "netatmo:NAModule2:home:wind:WindStrength" }
```

**Supported channels for the wind module:**

| Channel ID          | Item Type    | Description                                              |
|---------------------|--------------|----------------------------------------------------------|
| WindAngle           | Number:Angle | Current 5 minutes average wind direction                 |
| WindStrength        | Number:Speed | Current 5 minutes average wind speed                     |
| GustAngle           | Number:Angle | Direction of the last 5 minutes highest gust wind        |
| GustStrength        | Number:Speed | Speed of the last 5 minutes highest gust wind            |
| TimeStamp           | DateTime     | Timestamp when data was measured                         |
| LastMessage         | DateTime     | Last message emitted by the module                       |
| LowBattery          | Switch       | Low battery                                              |
| BatteryVP           | Number       | Battery level                                            |
| RfStatus            | Number       | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |
| MaxWindStrength     | Number:Speed | Maximum wind strength recorded                           |
| DateMaxWindStrength | DateTime     | Timestamp when MaxWindStrength was recorded              |

All these channels are read only.


### Healthy Home Coach Device

Example item for the **Healthy Home Coach**:

```
String Netatmo_LivingRoom_HomeCoach_HealthIndex "Climate" { channel = "netatmo:NHC:home:livingroom:HealthIndex" }
```

**Supported channels for the healthy home coach device:**

| Channel ID          | Item Type            | Description                                              |
|---------------------|----------------------|----------------------------------------------------------|
| HealthIndex         | String               | Health index (healthy, fine, fair, poor, unhealthy)      |
| Co2                 | Number:Dimensionless | Air quality                                              |
| Temperature         | Number:Temperature   | Current temperature                                      |
| TempTrend           | String               | Temperature evolution trend (up, down, stable)           |
| Noise               | Number:Dimensionless | Current noise level                                      |
| Pressure            | Number:Pressure      | Current pressure                                         |
| PressTrend          | String               | Pressure evolution trend for last 12h (up, down, stable) |
| AbsolutePressure    | Number:Pressure      | Absolute pressure                                        |
| Humidity            | Number:Dimensionless | Current humidity                                         |
| MinTemp             | Number:Temperature   | Minimum temperature on current day                       |
| MaxTemp             | Number:Temperature   | Maximum temperature on current day                       |
| DateMinTemp         | DateTime             | Date when minimum temperature was reached on current day |
| DateMaxTemp         | DateTime             | Date when maximum temperature was reached on current day |
| TimeStamp           | DateTime             | Timestamp when data was measured                         |
| LastStatusStore     | DateTime             | Last status store                                        |
| WifiStatus          | Number               | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |
| Location            | Location             | Location of the device                                   |

All these channels are read only.


### Thermostat Relay Device

**Supported channels for the thermostat relay device:**

| Channel ID          | Item Type | Description                                              |
|---------------------|-----------|----------------------------------------------------------|
| ConnectedBoiler     | Switch    | Plug connected boiler                                    |
| LastPlugSeen        | DateTime  | Last plug seen                                           |
| LastBilan           | DateTime  | Month of the last available thermostat bilan             |
| LastStatusStore     | DateTime  | Last status store                                        |
| WifiStatus          | Number    | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |
| Location            | Location  | Location of the device                                   |

All these channels are read only.


### Thermostat Module

**Supported channels for the thermostat module:**

| Channel ID          | Item Type          | Description                                                |
|---------------------|--------------------|------------------------------------------------------------|
| Temperature         | Number:Temperature | Current temperature                                        |
| Sp_Temperature      | Number:Temperature | Thermostat temperature setpoint                            |
| SetpointMode        | String             | Chosen setpoint_mode (program, away, hg, manual, off, max) |
| Planning            | String             | Id of the currently active planning when mode = program    |
| ThermRelayCmd       | Switch             | Indicates whether the furnace is heating or not            |
| ThermOrientation    | Number             | Physical orientation of the thermostat module              |
| TimeStamp           | DateTime           | Timestamp when data was measured                           |
| SetpointEndTime     | DateTime           | Thermostat goes back to schedule after that timestamp      |
| LastMessage         | DateTime           | Last message emitted by the module                         |
| LowBattery          | Switch             | Low battery                                                |
| BatteryVP           | Number             | Battery level                                              |
| RfStatus            | Number             | Signal strength (0 for no signal, 1 for weak, 2 for average, 3 for good or 4 for excellent) |

All these channels except Sp_Temperature, SetpointMode and Planning are read only.


### Welcome Home

**Supported channels for the Home thing:**

| Channel ID              | Item Type | Description                                              |
|-------------------------|-----------|----------------------------------------------------------|
| welcomeHomeCity         | String    | City of the home                                         |
| welcomeHomeCountry      | String    | Country of the home                                      |
| welcomeHomeTimezone     | String    | Timezone of the home                                     |
| welcomeHomePersonCount  | Number    | Total number of Persons that are at home                 |
| welcomeHomeUnknownCount | Number    | Count how many Unknown Persons are at home               |
| welcomeEventType        | String    | Type of event                                            |
| welcomeEventTime        | DateTime  | Time of occurrence of event                               |
| welcomeEventCameraId    | String    | Camera that detected the event                           |
| welcomeEventPersonId    | String    | Id of the person the event is about (if any)             |
| welcomeEventSnapshot    | Image     | picture of the last event, if it applies                 |
| welcomeEventSnapshotURL | String    | if the last event (depending upon event type) in the home lead a snapshot picture, the picture URL will be available here |
| welcomeEventVideoURL    | String    | if the last event (depending upon event type) in the home lead a snapshot picture, the corresponding video URL will be available here |
| welcomeEventVideoStatus | String    | Status of the video (recording, deleted or available)    |
| welcomeEventIsArrival   | Switch    | If person was considered "away" before being seen during this event |
| welcomeEventMessage     | String    | Message sent by Netatmo corresponding to given event     |
| welcomeEventSubType     | String    | Sub-type of SD and Alim events                           |

All these channels are read only.


### Welcome Camera

**Supported channels for the Camera thing:**

| Channel ID                  | Item Type | Description                                              |
|-----------------------------|-----------|----------------------------------------------------------|
| welcomeCameraStatus         | Switch    | State of the camera                                      |
| welcomeCameraSdStatus       | Switch    | State of the SD card                                     |
| welcomeCameraAlimStatus     | Switch    | State of the power connector                             |
| welcomeCameraIsLocal        | Switch    | indicates whether the camera is on the same network than the openHAB Netatmo Binding |
| welcomeCameraLivePicture    | Image     | Camera Live Snapshot                                     |
| welcomeCameraLivePictureUrl | String    | Url of the live snapshot for this camera                 |
| welcomeCameraLiveStreamUrl  | String    | Url of the live stream for this camera                   |

All these channels are read only.

Warning : the URL of the live snapshot is a fixed URL so the value of the channel welcomeCameraLivePictureUrl will never be updated once first set by the binding.
So to get a refreshed picture, you need to use the refresh parameter in your sitemap image element.


### Welcome Person

Netatmo API distinguishes two kinds of persons:

* Known persons : have been identified by the camera and you have defined a name for those.
* Unknown persons : identified by the camera, but no name defined.

Person things are automatically created in discovery process for all known persons.

**Supported channels for the Person thing:**

| Channel ID                    | Item Type | Description                                            |
|-------------------------------|-----------|--------------------------------------------------------|
| welcomePersonLastSeen         | DateTime  | Time when this person was last seen                    |
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
    Thing NAMain inside "Netatmo Inside"  [ id="aa:aa:aa:aa:aa:aa" ]
    Thing NAModule1 outside "Netatmo Outside"  [ id="bb:bb:bb:bb:bb:bb", parentId="aa:aa:aa:aa:aa:aa" ]
}
```


## items/netatmo.items

```
# Indoor Module
Number:Temperature Netatmo_Indoor_Temperature         "Temperature [%.1f %unit%]"          <temperature>      { channel = "netatmo:NAMain:home:inside:Temperature" }
Number:Dimensionless Netatmo_Indoor_Humidity            "Humidity [%d %unit%]"               <humidity>         { channel = "netatmo:NAMain:home:inside:Humidity" }
Number Netatmo_Indoor_Humidex             "Humidex [%.0f]"              <temperature_hot>  { channel = "netatmo:NAMain:home:inside:Humidex" }
Number:Temperature Netatmo_Indoor_HeatIndex           "HeatIndex [%.1f %unit%]"            <temperature_hot>  { channel = "netatmo:NAMain:home:inside:HeatIndex" }
Number:Temperature Netatmo_Indoor_Dewpoint            "Dewpoint [%.1f %unit%]"             <temperature_cold> { channel = "netatmo:NAMain:home:inside:Dewpoint" }
Number:Temperature Netatmo_Indoor_DewpointDepression  "DewpointDepression [%.1f %unit%]"   <temperature_cold> { channel = "netatmo:NAMain:home:inside:DewpointDepression" }
Number:Dimensionless Netatmo_Indoor_Co2                 "Co2 [%d %unit%]"                 <carbondioxide>    { channel = "netatmo:NAMain:home:inside:Co2" }
Number:Pressure Netatmo_Indoor_Pressure            "Pressure [%.1f %unit%]"           <pressure>         { channel = "netatmo:NAMain:home:inside:Pressure" }
Number:Pressure Netatmo_Indoor_AbsolutePressure    "AbsolutePressure [%.1f %unit%]"   <pressure>         { channel = "netatmo:NAMain:home:inside:AbsolutePressure" }
Number:Dimensionless Netatmo_Indoor_Noise               "Noise [%d %unit%]"                <soundvolume>      { channel = "netatmo:NAMain:home:inside:Noise" }
Number Netatmo_Indoor_WifiStatus          "WifiStatus [%s]"                <signal>           { channel = "netatmo:NAMain:home:inside:WifiStatus" }
DateTime Netatmo_Indoor_TimeStamp         "TimeStamp [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"  <calendar>  { channel = "netatmo:NAMain:home:inside:TimeStamp" }
Location Netatmo_Indoor_Location          "Location"                       <movecontrol>      { channel = "netatmo:NAMain:home:inside:Location" }
DateTime Netatmo_Indoor_LastStatusStore   "LastStatusStore [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"  <text>  { channel = "netatmo:NAMain:home:inside:LastStatusStore" }

# Outdoor Module
Number:Temperature Netatmo_Outdoor_Temperature        "Temperature [%.1f %unit%]"          <temperature>      { channel = "netatmo:NAModule1:home:outside:Temperature" }
String Netatmo_Outdoor_TempTrend          "TempTrend [%s]"                 <line>             { channel = "netatmo:NAModule1:home:outside:TempTrend" }
Number:Dimensionless Netatmo_Outdoor_Humidity           "Humidity [%d %unit%]"               <humidity>         { channel = "netatmo:NAModule1:home:outside:Humidity" }
Number Netatmo_Outdoor_Humidex            "Humidex [%.0f]"              <temperature_hot>  { channel = "netatmo:NAModule1:home:outside:Humidex" }
Number:Temperature Netatmo_Outdoor_HeatIndex          "HeatIndex [%.1f %unit%]"            <temperature_hot>  { channel = "netatmo:NAModule1:home:outside:HeatIndex" }
Number:Temperature Netatmo_Outdoor_Dewpoint           "Dewpoint [%.1f %unit%]"             <temperature_cold> { channel = "netatmo:NAModule1:home:outside:Dewpoint" }
Number:Temperature Netatmo_Outdoor_DewpointDepression "DewpointDepression [%.1f %unit%]"   <temperature_cold> { channel = "netatmo:NAModule1:home:outside:DewpointDepression" }
Number Netatmo_Outdoor_RfStatus           "RfStatus [%.0f / 5]"            <signal>           { channel = "netatmo:NAModule1:home:outside:RfStatus" }
Switch Netatmo_Outdoor_LowBattery         "LowBattery [%s]"                <siren>            { channel = "netatmo:NAModule1:home:outside:LowBattery" }
Number Netatmo_Outdoor_BatteryVP          "BatteryVP [%.0f %%]"            <battery>          { channel = "netatmo:NAModule1:home:outside:BatteryVP" }
DateTime Netatmo_Outdoor_TimeStamp        "TimeStamp [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"  <calendar>  { channel = "netatmo:NAModule1:home:outside:TimeStamp" }
DateTime Netatmo_Outdoor_LastMessage      "LastMessage [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"  <text>  { channel = "netatmo:NAModule1:home:outside:LastMessage" }
```


## sitemaps/netatmo.sitemap

```
sitemap netatmo label="Netatmo"
{
    Frame label="Indoor" {
        Text    item=Netatmo_Indoor_Temperature
        Text    item=Netatmo_Indoor_Humidity
        Text    item=Netatmo_Indoor_Humidex  valuecolor=[<20.1="green",<29.1="blue",<28.1="yellow",<45.1="orange",<54.1="red",>54.1="maroon"]
        Text    item=Netatmo_Indoor_HeatIndex
        Text    item=Netatmo_Indoor_Dewpoint
        Text    item=Netatmo_Indoor_DewpointDepression
        Text    item=Netatmo_Indoor_Co2  valuecolor=[<800="green",<1000="orange",<1400="red",>1399="maroon"]
        Text    item=Netatmo_Indoor_Pressure
        Text    item=Netatmo_Indoor_AbsolutePressure
        Text    item=Netatmo_Indoor_Noise
        Text    item=Netatmo_Indoor_WifiStatus
        Text    item=Netatmo_Indoor_TimeStamp
        Text    item=Netatmo_Indoor_Location
        Text    item=Netatmo_Indoor_LastStatusStore
    }
    Frame label="Outdoor" {
        Text    item=Netatmo_Outdoor_Temperature
        Text    item=Netatmo_Outdoor_TempTrend
        Text    item=Netatmo_Outdoor_Humidity
        Text    item=Netatmo_Outdoor_Humidex
        Text    item=Netatmo_Outdoor_HeatIndex
        Text    item=Netatmo_Outdoor_Dewpoint
        Text    item=Netatmo_Outdoor_DewpointDepression
        Text    item=Netatmo_Outdoor_RfStatus
        Text    item=Netatmo_Outdoor_LowBattery
        Text    item=Netatmo_Outdoor_BatteryVP  valuecolor=[>60="green",>45="orange",>36="red",>0="maroon"]
        Text    item=Netatmo_Outdoor_TimeStamp
        Text    item=Netatmo_Outdoor_LastMessage
    }
}
```


# Common problems


## Missing Certificate Authority

This version of the binding has been modified to avoid the need to import StartCom certificate in the local JDK certificate store.

```
javax.net.ssl.SSLHandshakeException:
sun.security.validator.ValidatorException:
PKIX path building failed:
sun.security.provider.certpath.SunCertPathBuilderException:
unable to find valid certification path to requested target
```

can be solved by installing the StartCom CA Certificate into the local JDK like this:

* Download the certificate from https://www.startssl.com/certs/ca.pem or use wget https://www.startssl.com/certs/ca.pem
* Then import it into the keystore (the password is "changeit")

```
$JAVA_HOME/bin/keytool -import -keystore $JAVA_HOME/jre/lib/security/cacerts -alias StartCom-Root-CA -file ca.pem
```

If `$JAVA_HOME` is not set then run the command:

```
update-alternatives --list java
```

This should output something similar to:

```
/usr/lib/jvm/java-8-oracle/jre/bin/java
```

Use everything before /jre/... to set the JAVA_HOME environment variable:

```
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
```

After you set the environment variable, try:

```
ls -l $JAVA_HOME/jre/lib/security/cacerts
```

If it's set correctly then you should see something similar to:

```
-rw-r--r-- 1 root root 101992 Nov 4 10:54 /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts
```

Now try and rerun the keytool command.
If you didn't get errors, you should be good to go [source](http://jinahya.wordpress.com/2013/04/28/installing-the-startcom-ca-certifcate-into-the-local-jdk/).

Alternative approach if above solution does not work: 

```
sudo keytool -delete -alias StartCom-Root-CA -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit  
```  

Download the certificate from https://api.netatmo.net to `$JAVA_HOME/jre/lib/security/` and save it as api.netatmo.net.crt (X.509 / PEM).

```
sudo $JAVA_HOME/bin/keytool -import -keystore $JAVA_HOME/jre/lib/security/cacerts -alias StartCom-Root-CA -file api.netatmo.net.crt
```

The password is "changeit".


# Sample data

If you want to evaluate this binding but have not got a Netatmo station yourself
yet, you can add the Netatmo office in Paris to your account:

http://www.netatmo.com/en-US/addguest/index/TIQ3797dtfOmgpqUcct3/70:ee:50:00:02:20


# Icons

The following icons are used by original Netatmo web app:


## Modules

- http://my.netatmo.com/img/my/app/module_int.png
- http://my.netatmo.com/img/my/app/module_ext.png
- http://my.netatmo.com/img/my/app/module_rain.png


## Battery status

- http://my.netatmo.com/img/my/app/battery_verylow.png
- http://my.netatmo.com/img/my/app/battery_low.png
- http://my.netatmo.com/img/my/app/battery_medium.png
- http://my.netatmo.com/img/my/app/battery_high.png
- http://my.netatmo.com/img/my/app/battery_full.png


## Signal status

- http://my.netatmo.com/img/my/app/signal_verylow.png
- http://my.netatmo.com/img/my/app/signal_low.png
- http://my.netatmo.com/img/my/app/signal_medium.png
- http://my.netatmo.com/img/my/app/signal_high.png
- http://my.netatmo.com/img/my/app/signal_full.png


## Wifi status

- http://my.netatmo.com/img/my/app/wifi_low.png
- http://my.netatmo.com/img/my/app/wifi_medium.png
- http://my.netatmo.com/img/my/app/wifi_high.png
- http://my.netatmo.com/img/my/app/wifi_full.png
