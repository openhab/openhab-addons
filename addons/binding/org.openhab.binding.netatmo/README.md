---
layout: documentation
---

{% include base.html %}

# Netatmo Binding
 
The Netatmo binding integrates the following Netatmo products :
- *Personal Weather Station*. Reports temperature, humidity, air pressure, carbon dioxide concentration in the air, as well as the ambient noise level.
- *Thermostat*. Reports ambient temperature, allow to check target temperature, consult and change furnace heating status.

See http://www.netatmo.com/ for details on their product.
    
## Binding Configuration
 
The binding has no configuration options itself, all configuration is done at 'Things' level but before, you'll have to grant openHab to access Netatmo API. Here is the procedure :

### 1. Application creation
Create an application at https://dev.netatmo.com/dev/createapp

The variables you'll need to get to setup the binding are:
* `<CLIENT_ID>` Your client ID taken from your App at https://dev.netatmo.com/dev/listapps
* `<CLIENT_SECRET>` A token provided along with the `<CLIENT_ID>`.
* `<USERNAME>` The username you use to connect to the Netatmo API (usually your mail address).
* `<PASSWORD>` The password attached to the above username.
 
## 2.Bridge and Things Configuration
 
Once you'll get needed informations from the Netatmo API, you'll be able to configure bridge and things.

E.g.
```
Bridge netatmo:netatmoapi:home [ clientId="<CLIENT_ID>", clientSecret="<CLIENT_SECRET>", username = "<USERNAME>", password = "<PASSWORD>", readStation=true|false, readThermostat=true|false] {
    Thing NAMain    inside  [ equipmentId="aa:aa:aa:aa:aa:aa", [refreshInterval=60000] ]
    Thing NAModule1 outside  [ equipmentId="yy:yy:yy:yy:yy:yy", parentId="aa:aa:aa:aa:aa:aa" ]
    Thing NAPlug    plugtherm  [ equipmentId="bb:bb:bb:bb:bb:bb", [refreshInterval=60000] ]
    Thing NATherm1  thermostat [ equipmentId="xx:xx:xx:xx:xx:xx", parentId="bb:bb:bb:bb:bb:bb" ]
    ...
}  
```

### Configure Things

The IDs for the modules can be extracted from the developer documentation on the netatmo site.
First login with your user. Then some examples of the documentation contain the **real results** of your weather station. Get the IDs of your devices (indoor, outdoor, rain gauge) here:

```
https://dev.netatmo.com/doc/methods/devicelist
```

main_device is the ID of the "main device", the indoor sensor. This is equal to the MAC address of the Netatmo.

The other modules you can recognize by "module_name" and then note the "_id" which you need later.

**Another way to get the IDs is to calculate them:**

You have to calculate the ID for the outside module as follows: (it cannot be read from the app)
if the first serial character is "h":  start with "02",
if the first serial character is "i": start with "03",

append ":00:00:",

split the rest into three parts of two characters and append with a colon as delimeter.

For example your serial number "h00bcdc" should end up as "02:00:00:00:bc:dc".

## Discovery

If you don't manually create things in the *.things file, the Netatmo Binding is able to discover automatically all depending modules and devices from Netatmo website.
 
## Channels
 
### Weather Station Main Indoor Device
Example item for the **indoor module**:
```
Number Netatmo_Indoor_CO2 "CO2" <carbondioxide> { channel = "netatmo:NAMain:home:inside:Co2" }
```

**Supported types for the indoor module:**
* Temperature
* Humidity
* Co2
* Pressure
* AbsolutePressure
* Noise
* WifiStatus
* Location
* TimeStamp
* HeatIndex
* Humidex
* Dewpoint
* DewpointDepression
* WifiStatus
* LastStatusStore
 
### Weather Station Outdoor module
Example item for the **outdoor module** 
```
Number Netatmo_Outdoor_Temperature "Temperature" { channel = "netatmo:NAModule1:home:outside:Temperature" }
```

**Supported types for the outdoor module:**
* Temperature
* Humidity
* RfStatus
* BatteryVP
* TimeStamp
* Humidex
* HeatIndex
* Dewpoint
* DewpointDepression
* LastMessage
* LowBattery

### Weather Station Additional Indoor module
Example item for the **indoor module** 
```
Number Netatmo_Indoor2_Temperature "Temperature" { channel = "netatmo:NAModule4:home:insidesupp:Temperature" }
```

**Supported types for the additional indoor module:**
* Co2
* Temperature
* Humidity
* RfStatus
* BatteryVP
* TimeStamp
* Humidex
* HeatIndex
* Dewpoint
* DewpointDepression
* LastMessage
* LowBattery

### Rain
Example item for the **rain gauge** 
```
Number Netatmo_Rain_Current "Rain [%.1f mm]" { channel = "netatmo:NAModule3:home:rain:Rain" }
```

**Supported types for the rain guage:**
* Rain
* RfStatus
* BatteryVP
* LastMessage
* LowBattery

### Weather Station Wind module
Example item for the **wind module** :
```
Number Netatmo_Wind_Strength "Wind Strength [%.0f KPH]" { channel = "netatmo:NAModule2:home:wind:WindStrength" }
```

**Supported types for the wind module:**
* WindStrength
* WindAngle
* GustStrength
* GustAngle
* LastMessage
* LowBattery
* RfStatus
* BatteryVP

### Thermostat Relay Device


**Supported types for the thermostat relay device:**
* LastStatusStore
* WifiStatus
* Location

### Thermostat Module

**Supported types for the thermostat module:**
* Temperature
* SetpointTemperature
* SetpointMode
* BoilerOn
* BoilerOff
* TimeStamp

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
If $JAVA_HOME is not set then run the command:
update-alternatives --list java

This should output something similar to:
/usr/lib/jvm/java-8-oracle/jre/bin/java

Use everything before /jre/... to set the JAVA_HOME environment variable:
export JAVA_HOME=/usr/lib/jvm/java-8-oracle

After you set the environment variable, try:

ls -l $JAVA_HOME/jre/lib/security/cacerts

If it's set correctly then you should see something similar to:
-rw-r--r-- 1 root root 101992 Nov 4 10:54 /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts

Now try and rerun the keytool command. If you didn't get errors, you should be good to go.

source: http://jinahya.wordpress.com/2013/04/28/installing-the-startcom-ca-certifcate-into-the-local-jdk/  

alternative approach if above solution does not work: 
 
```
sudo keytool -delete -alias StartCom-Root-CA -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit  
```  
    
download the certificate from https://api.netatmo.net to $JAVA_HOME/jre/lib/security/ and save it as api.netatmo.net.crt (X.509 / PEM)


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
http://my.netatmo.com/img/my/app/module_int.png
http://my.netatmo.com/img/my/app/module_ext.png
http://my.netatmo.com/img/my/app/module_rain.png

## Battery status
http://my.netatmo.com/img/my/app/battery_verylow.png
http://my.netatmo.com/img/my/app/battery_low.png
http://my.netatmo.com/img/my/app/battery_medium.png
http://my.netatmo.com/img/my/app/battery_high.png
http://my.netatmo.com/img/my/app/battery_full.png

## Signal status
http://my.netatmo.com/img/my/app/signal_verylow.png
http://my.netatmo.com/img/my/app/signal_low.png
http://my.netatmo.com/img/my/app/signal_medium.png
http://my.netatmo.com/img/my/app/signal_high.png
http://my.netatmo.com/img/my/app/signal_full.png

## Wifi status
http://my.netatmo.com/img/my/app/wifi_low.png
http://my.netatmo.com/img/my/app/wifi_medium.png
http://my.netatmo.com/img/my/app/wifi_high.png
http://my.netatmo.com/img/my/app/wifi_full.png
 