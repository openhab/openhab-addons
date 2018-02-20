# Netatmo Binding

The Netatmo binding integrates the following Netatmo products:

- *Personal Weather Station*. Reports temperature, humidity, air pressure, carbon dioxide concentration in the air, as well as the ambient noise level.
- *Thermostat*. Reports ambient temperature, allow to check target temperature, consult and change furnace heating status.

See http://www.netatmo.com/ for details on their product.


## Binding Configuration

The binding has no configuration options itself, all configuration is done at 'Things' level but before, you'll have to grant openHab to access Netatmo API. Here is the procedure:

### 1. Application Creation

Create an application at https://dev.netatmo.com/dev/createapp

The variables you'll need to get to setup the binding are:

* `<CLIENT_ID>` Your client ID taken from your App at https://dev.netatmo.com/dev/listapps
* `<CLIENT_SECRET>` A token provided along with the `<CLIENT_ID>`.
* `<USERNAME>` The username you use to connect to the Netatmo API (usually your mail address).
* `<PASSWORD>` The password attached to the above username.


## 2. Bridge and Things Configuration

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
The webhook URL is setup at bridge level using "Webhook Address" parameter. You'll define here public way to access your OH2 server:

```
http(s)://xx.yy.zz.ww:8080
```

Your Netatmo App will be configured automatically by the bridge to the endpoint : 

```
http(s)://xx.yy.zz.ww:8080/netatmo/camera
```

Please be aware of Netatmo own limits regarding webhook usage that lead to a 24h ban-time when webhook does not answer 5 times.


### Configure Things

The IDs for the modules can be extracted from the developer documentation on the netatmo site.
First login with your user. Then some examples of the documentation contain the **real results** of your weather station. Get the IDs of your devices (indoor, outdoor, rain gauge) [here](https://dev.netatmo.com/doc/methods/devicelist).

`main_device` is the ID of the "main device", the indoor sensor. This is equal to the MAC address of the Netatmo.

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

#### Configuration

Weather station does not need any refreshInterval setting. Based on a standard update period of 10mn by Netatmo systems - it will auto adapt to stick closest as possible to last data availability.

Example item for the **indoor module**:

```
Number Netatmo_Indoor_CO2 "CO2" <carbondioxide> { channel = "netatmo:NAMain:home:inside:Co2" }
```

**Supported channels for the indoor module:**

* Temperature
* TemperatureTrend
* Humidity
* Co2
* Pressure
* PressureTrend
* AbsolutePressure
* Noise
* HeatIndex
* Humidex
* Dewpoint
* DewpointDepression
* WifiStatus
* Location
* TimeStamp
* LastStatusStore
* MinTemp
* MaxTemp
* DateMinTemp
* DateMaxTemp


### Weather Station Outdoor module

Example item for the **outdoor module** 

```
Number Netatmo_Outdoor_Temperature "Temperature" { channel = "netatmo:NAModule1:home:outside:Temperature" }
```

**Supported channels for the outdoor module:**

* Temperature
* TemperatureTrend
* Humidity
* RfStatus
* BatteryVP
* Humidex
* HeatIndex
* Dewpoint
* DewpointDepression
* TimeStamp
* LastMessage
* LowBattery
* MinTemp
* MaxTemp
* DateMinTemp
* DateMaxTemp


### Weather Station Additional Indoor module

Example item for the **indoor module** 

```
Number Netatmo_Indoor2_Temperature "Temperature" { channel = "netatmo:NAModule4:home:insidesupp:Temperature" }
```

**Supported channels for the additional indoor module:**

* Temperature
* TemperatureTrend
* Humidity
* Co2
* RfStatus
* BatteryVP
* Humidex
* HeatIndex
* Dewpoint
* DewpointDepression
* TimeStamp
* LastMessage
* LowBattery
* MinTemp
* MaxTemp
* DateMinTemp
* DateMaxTemp


### Rain Gauge

Example item for the **rain gauge**

```
Number Netatmo_Rain_Current "Rain [%.1f mm]" { channel = "netatmo:NAModule3:home:rain:Rain" }
```

**Supported channels for the rain guage:**

* Rain
* Rain1
* Rain24
* TimeStamp
* RfStatus
* BatteryVP
* LastMessage
* LowBattery

### Weather Station Wind module

Example item for the **wind module**:

```
Number Netatmo_Wind_Strength "Wind Strength [%.0f KPH]" { channel = "netatmo:NAModule2:home:wind:WindStrength" }
```

**Supported channels for the wind module:**

* WindStrength
* WindAngle
* GustStrength
* GustAngle
* LastMessage
* LowBattery
* TimeStamp
* RfStatus
* BatteryVP

### Healthy Home Coach Device

Example item for the **Healthy Home Coach**:

```
String Netatmo_LivingRoom_HomeCoach_HealthIndex "Climate" { channel = "netatmo:NHC:home:livingroom:HealthIndex" }
```

**Supported channels for the healthy home coach device:**

* HealthIndex
* Temperature
* TemperatureTrend
* Humidity
* Co2
* Pressure
* PressureTrend
* AbsolutePressure
* Noise
* WifiStatus
* Location
* TimeStamp
* LastStatusStore
* MinTemp
* MaxTemp
* DateMinTemp
* DateMaxTemp


### Thermostat Relay Device

**Supported channels for the thermostat relay device:**

* LastStatusStore
* WifiStatus
* Location
* ConnectedBoiler
* LastPlugSeen
* LastBilan


### Thermostat Module

**Supported channels for the thermostat module:**

* Temperature
* SetpointTemperature
* SetpointMode
* BoilerOn
* BoilerOff
* TimeStamp


### Welcome Home

This part of the binding will require basic read_camera and access_camera scopes. write_camera will only be needed to changed some channels from within OH2 (detailed below).

**Supported channels for the Home thing:**

* welcomeHomeCity
* welcomeHomeCountry
* welcomeHomeTimezone
* welcomeHomePersonCount
* welcomeHomeUnknownCount
* welcomeEventType
* welcomeEventTime
* welcomeEventCameraId
* welcomeEventPersonId
* welcomeEventVideoStatus
* welcomeEventIsArrival
* welcomeEventMessage
* welcomeEventSubType
* welcomeEventSnapshot : picture of the last event, if it applies.
* welcomeEventSnapshotURL : if the last event (depending upon event type) in the home lead a a snapshot picture, it will be available here.
* welcomeEventVideoURL :  the last event (depending upon event type) in the home lead a a snapshot picture, the corresponding videoo will be available here.


### Welcome Camera

**Supported channels for the Camera thing:**

* welcomeCameraStatus
* welcomeCameraSdStatus
* welcomeCameraAlimStatus
* welcomeCameraIsLocal : indicates wether the camera is on the same network than the openHab Netatmo Binding
* welcomeCameraLivePicture : current image snapshot
* welcomeCameraLivePictureUrl : url of the current image
* welcomeCameraLiveStreamUrl : url of the feed for live video


### Welcome Person

Netatmo API distinguishes two kinds of persons:

* Known persons : have been identified by the camera and you have defined a name for those.
* Unknown persons : identified by the camera, but no name defined.

Person things are automatically created in discovery process for all known persons.

**Supported channels for the Person thing:**

* welcomePersonLastSeen
* welcomePersonAtHome. Indicates if this person is known to be at home or not. Modifying this value from OH2 requires the "write_camera" in the Netatmo App scope. Warning : while setting person away is fine, the contrary does not seem supported officialy by Netatmo API. 
* welcomePersonAvatarUrl
* welcomePersonAvatar
* welcomePersonLastEventMessage
* welcomePersonLastEventTime
* welcomePersonLastEvent
* welcomePersonLastEventUrl


# Configuration Examples


## transform/netatmo_unit_en.map

```
0=Metric
1=Imperial
```


## transform/netatmo_pressureunit.map

```
0=mbar
1=inHg
2=mmHg
```


## transform/netatmo_windunit.map

```
0=Km/h
1=Miles/H
2=m/s
3=Beaufort
4=Knot
```


## things/netatmo.things

```
// Bridge configuration:
Bridge netatmo:netatmoapi:home "Netatmo API" [ clientId="*********", clientSecret="**********", username = "me@example.com", password = "******", readStation=true, readThermostat=false] {
    // Thing configuration:
    Thing netatmo:NAMain:home:inside "Netatmo Inside"  [ id="aa:aa:aa:aa:aa:aa" ]
    Thing netatmo:NAModule1:home:outside "Netatmo Outside"  [ id="bb:bb:bb:bb:bb:bb", parentId="aa:aa:aa:aa:aa:aa" ]
}
```


## items/netatmo.items

```
# Indoor Module
Number Netatmo_Indoor_Temperature         "Temperature [%.2f °C]"          <temperature>      { channel = "netatmo:NAMain:home:inside:Temperature" }
Number Netatmo_Indoor_Humidity            "Humidity [%d %%]"               <humidity>         { channel = "netatmo:NAMain:home:inside:Humidity" }
Number Netatmo_Indoor_Humidex             "Humidex [%.1f °C]"              <temperature_hot>  { channel = "netatmo:NAMain:home:inside:Humidex" }
Number Netatmo_Indoor_HeatIndex           "HeatIndex [%.1f °C]"            <temperature_hot>  { channel = "netatmo:NAMain:home:inside:HeatIndex" }
Number Netatmo_Indoor_Dewpoint            "Dewpoint [%.1f °C]"             <temperature_cold> { channel = "netatmo:NAMain:home:inside:Dewpoint" }
Number Netatmo_Indoor_DewpointDepression  "DewpointDepression [%.1f °C]"   <temperature_cold> { channel = "netatmo:NAMain:home:inside:DewpointDepression" }
Number Netatmo_Indoor_Co2                 "Co2 [%.0f ppm]"                 <carbondioxide>    { channel = "netatmo:NAMain:home:inside:Co2" }
Number Netatmo_Indoor_Pressure            "Pressure [%.1f mbar]"           <pressure>         { channel = "netatmo:NAMain:home:inside:Pressure" }
Number Netatmo_Indoor_AbsolutePressure    "AbsolutePressure [%.1f mbar]"   <pressure>         { channel = "netatmo:NAMain:home:inside:AbsolutePressure" }
Number Netatmo_Indoor_Noise               "Noise [%.0f db]"                <soundvolume>      { channel = "netatmo:NAMain:home:inside:Noise" }
Number Netatmo_Indoor_WifiStatus          "WifiStatus [%s]"                <signal>           { channel = "netatmo:NAMain:home:inside:WifiStatus" }
DateTime Netatmo_Indoor_TimeStamp         "TimeStamp [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"  <calendar>  { channel = "netatmo:NAMain:home:inside:TimeStamp" }
Location Netatmo_Indoor_Location          "Location"                       <movecontrol>      { channel = "netatmo:NAMain:home:inside:Location" }
DateTime Netatmo_Indoor_LastStatusStore   "LastStatusStore [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"  <text>  { channel = "netatmo:NAMain:home:inside:LastStatusStore" }
Number Netatmo_Indoor_Unit                "Unit [MAP(netatmo_unit_en.map):%s]"  <text>        { channel = "netatmo:NAMain:home:inside:Unit" }
Number Netatmo_Indoor_WindUnit            "WindUnit [MAP(netatmo_windunit.map):%s]"  <text>   { channel = "netatmo:NAMain:home:inside:WindUnit" }
Number Netatmo_Indoor_PressureUnit        "PressureUnit [MAP(netatmo_pressureunit.map):%s]"  <pressure>  { channel = "netatmo:NAMain:home:inside:PressureUnit" }

# Outdoor Module
Number Netatmo_Outdoor_Temperature        "Temperature [%.2f °C]"          <temperature>      { channel = "netatmo:NAModule1:home:outside:Temperature" }
String Netatmo_Outdoor_TempTrend          "TempTrend [%s]"                 <line>             { channel = "netatmo:NAModule1:home:outside:TempTrend" }
Number Netatmo_Outdoor_Humidity           "Humidity [%d %%]"               <humidity>         { channel = "netatmo:NAModule1:home:outside:Humidity" }
Number Netatmo_Outdoor_Humidex            "Humidex [%.1f °C]"              <temperature_hot>  { channel = "netatmo:NAModule1:home:outside:Humidex" }
Number Netatmo_Outdoor_HeatIndex          "HeatIndex [%.1f °C]"            <temperature_hot>  { channel = "netatmo:NAModule1:home:outside:HeatIndex" }
Number Netatmo_Outdoor_Dewpoint           "Dewpoint [%.1f °C]"             <temperature_cold> { channel = "netatmo:NAModule1:home:outside:Dewpoint" }
Number Netatmo_Outdoor_DewpointDepression "DewpointDepression [%.1f °C]"   <temperature_cold> { channel = "netatmo:NAModule1:home:outside:DewpointDepression" }
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
        Text    item=Netatmo_Indoor_Unit
        Text    item=Netatmo_Indoor_WindUnit
        Text    item=Netatmo_Indoor_PressureUnit
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

This version of the binding has been modified to avoid the need to impoort StartCom certificate in the local JDK certificate store.

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

Now try and rerun the keytool command. If you didn't get errors, you should be good to go [source](http://jinahya.wordpress.com/2013/04/28/installing-the-startcom-ca-certifcate-into-the-local-jdk/).  

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
