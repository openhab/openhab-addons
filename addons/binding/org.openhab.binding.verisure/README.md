# Verisure Binding

This is an OpenHAB binding for Versiure Alarm system, by Securitas Direct.

This binding uses the rest API behind the myverisure pages https://mypages.verisure.com/login.html.

The binding supports several installation sites via the configuration parameter numberOfInstallations that defaults to 1.

Be aware that Verisure don't approve if you update to often, I have gotten no complaints running with a 10 minutes update interval, but officially you should use 30 minutes.

## Supported Things

This binding supports the following thing types:

- Bridge
- Alarm
- Smoke Detector (climate) 
- Water Detector (climate)
- Siren (climate)
- Night Control
- Yaleman SmartLock
- SmartPlug
- Door/Window Status
- User Presence Status
- Broadband Connection Status


## Binding Configuration

You will have to configure the bridge with username and password, these must be the same values as used when logging into https://mypages.verisure.com. 
You can also configure your pin-code to be able to lock/unlock the SmartLock and arm/unarm the alarm. 
It is also possible to configure the number of Verisure installations/sites you have, default is 1.

## Discovery

After the configuration of the Verisure Bridge all of the available Sensors, Alarms, SmartPlugs, SmartLocks and Climate devices will be discovered and placed as things in the inbox.

## Thing Configuration

Only the bridge require manual configuration. The devices and sensors should not be added by hand, let the discovery/inbox initially configure these.

## Supported Things and Channels 

### Verisure Bridge 

([bridge]) supports the following channel:

| Channel Type ID | Item Type | Description                                                                                     |
|-----------------|-----------|-------------------------------------------------------------------------------------------------|
| status          | String    | This channel can be used to trigger an instant refresh by sending a RefreshType.REFRESH command.|



### Verisure Alarm

([alarm]) supports the following channels:

| Channel Type ID | Item Type | Description                                                                                                                                                     
            |
|-----------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------|
| status          | String    | This channel reports the overall alarm status (armed/unarmed).                                                                  
            |
| numericStatus   | Number    | This channel reports the alarm status as a number.                                                                                
            |
| alarmStatus     | String    | This channel reports the specific alarm status ("DISARMED", "ARMED_HOME" or "ARMED AWAY").                                       
            |
| lastUpdate      | String    | This channel reports the last time the alarm status was changed.                                                          
            |
| changedByUser   | String    | This channel reports the user that last changed the state of the alarm.                                                       
            |
| siteName        | String    | This channel reports the name of the site.                                                                                     
            |
| siteId          | Number    | This channel reports the site ID of the site.                                                                                        
            |
| setAlarmStatus  | Number    | This channel is used to arm/disarm the alarm. Available alarm status are 0 for "DISARMED", 1 for "ARMED_HOME" and 2 for "ARMED AWAY"|                                             |

### Verisure Smoke Detector

([smokeDetector]) supports the following channels:
 
| Channel Type ID | Item Type            | Description                                                | 
|----------------|-----------------------|------------------------------------------------------------|                                                                                                                                          
| humidity       | Number                | This channel reports the current humidity in percentage.   |
| temperature    | Number:Temperature    | This channel reports the current humidity in percentage.   |
| lastUpdate     | String                | This channel reports the last time this sensor was updated.|
| location       | String                | This channel reports the location.                         |
| siteName       | String                | This channel reports the name of the site.                 |
| siteId         | Number                | This channel reports the site ID of the site.              |
 
### Verisure Water Detector

([waterDetector]) supports the following channels:

| Channel Type ID | Item Type            | Description                                                | 
|----------------|-----------------------|------------------------------------------------------------|                                                                                                                                          
| humidity       | Number                | This channel reports the current humidity in percentage.   |
| temperature    | Number:Temperature    | This channel reports the current humidity in percentage.   |
| lastUpdate     | String                | This channel reports the last time this sensor was updated.|
| location       | String                | This channel reports the location.                         |
| siteName       | String                | This channel reports the name of the site.                 |
| siteId         | Number                | This channel reports the site ID of the site.              |
 
### Verisure Siren

([siren]) supports the following channels:
 
| Channel Type ID | Item Type            | Description                                                | 
|----------------|-----------------------|------------------------------------------------------------|                                                                                                                                          
| humidity       | Number                | This channel reports the current humidity in percentage.   |
| temperature    | Number:Temperature    | This channel reports the current humidity in percentage.   |
| lastUpdate     | String                | This channel reports the last time this sensor was updated.|
| location       | String                | This channel reports the location.                         |
| siteName       | String                | This channel reports the name of the site.                 |
| siteId         | Number                | This channel reports the site ID of the site.              |

### Verisure Night Control

([nightControl]) supports the following channels:
 
| Channel Type ID | Item Type            | Description                                                | 
|----------------|-----------------------|------------------------------------------------------------|                                                                                                                                          
| humidity       | Number                | This channel reports the current humidity in percentage.   |
| temperature    | Number:Temperature    | This channel reports the current humidity in percentage.   |
| lastUpdate     | String                | This channel reports the last time this sensor was updated.|
| location       | String                | This channel reports the location.                         |
| siteName       | String                | This channel reports the name of the site.                 |
| siteId         | Number                | This channel reports the site ID of the site.              |

### Verisure Yaleman SmartLock

([smartLock]) supports the following channels:

| Channel Type ID        | Item Type | Description                                                                                              |
|------------------------|-----------|----------------------------------------------------------------------------------------------------------|
| status                 | String    | This channel reports the overall alarm status (armed/unarmed).                                           |
| numericStatus          | Number    | This channel reports the alarm status as a number.                                                       |
| smartLockStatus        | String    | This channel reports the lock status.                                                                    |
| lastUpdate             | String    | This channel reports the last time the alarm status was changed.                                         |
| changedByUser          | String    | This channel reports the user that last changed the state of the alarm.                                  |
| autoRelockEnabled      | String    | This channel reports the status of the Auto-lock function.                                               |
| smartLockVolume        | String    | This channel reports the status of the Auto-lock function.                                               |
| smartLockVoiceLevel    | String    | This channel reports the current voice level setting.                                                    | 
| location               | String    | This channel reports the location.                                                                       |
| siteName               | String    | This channel reports the name of the site.                                                               |
| siteId                 | Number    | This channel reports the site ID of the site.                                                            |                               
| siteName               | String    | This channel reports the name of the site.                                                               |
| siteId                 | Number    | This channel reports the site ID of the site.                                                            |
| setSmartLockStatus     | Switch    | This channel is used to lock/unlock.                                                                     |
| setAutoRelock          | Switch    | This channel is used to configure auto-lock functionality                                                |                
| setSmartLockVolume     | String    | This channel is used to set the volume level. Available volume settings are "SILENCE", "LOW" and "HIGH". |  
| setSmartLockVoiceLevel | String    | This channel is used to set the voice level. Available voice level settings are "ESSENTIAL" and "NORMAL".| 

### Verisure SmartPlug

([smartPlug]) supports the following channels:

| Channel Type ID    | Item Type | Description                                                       | 
|--------------------|-----------|-------------------------------------------------------------------|                                                                                                                                          
| status             | String    | This channel reports the lock status.                             |
| hazardous          | Number    | This channel reports if the smart plug is configured as hazardous.|
| smartPlugStatus    | String    | This channel reports the last time this sensor was updated.       |
| location           | String    | This channel reports the location.                                |
| siteName           | String    | This channel reports the name of the site.                        |
| siteId             | Number    | This channel reports the site ID of the site.                     |
| setSmartPlugStatus | Switch    | This channel is used to turn smart plug on/off.                   |

### Verisure DoorWindow Sensor

([doorWindowSensor]) supports the following channels:
 
| Channel Type ID | Item Type | Description                                                                 | 
|-----------------|-----------|-----------------------------------------------------------------------------|                                                                                                                                          
| state           | Contact   | This channel reports the if the door/window is open or closed (OPEN/CLOSED).|
| location        | String    | This channel reports the location.                                          |
| siteName        | String    | This channel reports the name of the site.                                  |
| siteId          | Number    | This channel reports the site ID of the site.                               |

### Verisure User Presence

([userPresence]) supports the following channels:
 
| Channel Type ID    | Item Type | Description                                                 | 
|--------------------|-----------|-------------------------------------------------------------|                                                                                                                                          
| webAccount         | String    | This channel reports the user's email.                      |
| userLocationStatus | String    | This channel reports the user presence (HOME/AWAY).         |
| userLocationName   | String    | This channel reports the name of the location (can be null).|
| siteName           | String    | This channel reports the name of the site.                  |
| siteId             | Number    | This channel reports the ID of the site.                    |

### Verisure Broadband Connection

([broadbandConnection]) supports the following channels:
 
| Channel Type ID | Item Type | Description                                                                    | 
|-----------------|-----------|--------------------------------------------------------------------------------|                                                                                                                                          
| timestamp       | String    | This channel reports the last time the Broadband connection status was checked.|
| hasWiFi         | String    | This channel reports if user has WiFi connection.                              |
| status          | String    | This channel reports the broadband connection status.                          |
| siteName        | String    | This channel reports the name of the site.                                     |
| siteId          | Number    | This channel reports the ID of the site.                                       |

## Example

### Things-file

````
// Bridge configuration
Bridge verisure:bridge:myverisure "Verisure Bridge" [username="x@y.com", password="1234", refresh="600", pin="111111", numberOfInstallations="1"]
````


### Items-file

````
// SmartLock and Alarm
Switch   SmartLock                     "Verisure SmartLock"  <lock>   [ "Switchable" ]  {channel="verisure:smartLock:myverisure:B2XX_YYST:setSmartLockStatus"}
Number   AlarmHome                     "Alarm Home"          <alarm>                    {channel="verisure:alarm:myverisure:alarm_2:setAlarmStatus"}
Switch   AlarmHomeVirtual              "Verisure Alarm"      <alarm>  [ "Switchable" ] 
String   AlarmStatus                   "Verisure Alarm Status"                          {channel="verisure:alarm:myverisure:alarm_2:status"}
Number   AlarmNumericStatus            "Verisure Alarm Numeric Status"                  {channel="verisure:alarm:myverisure:alarm_2:numericStatus"}
String   AlarmAlarmStatus              "Verisure Alarm Status"                          {channel="verisure:alarm:myverisure:alarm_2:alarmStatus"}
String   AlarmTimeStamp                "Verisure Alarm Time Stamp"                      {channel="verisure:alarm:myverisure:alarm_2:timestamp"}
String   AlarmChangedByUser            "Verisure Alarm Changed By User"                 {channel="verisure:alarm:myverisure:alarm_2:changedByUser"}
Switch   AutoLock                      "AutoLock"            <lock>   [ "Switchable" ]  {channel="verisure:smartLock:myverisure:B2XX_YYST:setAutoRelock"}
String   SmartLockStatus               "SmartLock Status"                               {channel="verisure:smartLock:myverisure:B2XX_YYST:smartLockStatus"}
String   SmartLockCurrentStatus        "SmartLock Current Status"                       {channel="verisure:smartLock:myverisure:B2XX_YYST:status"}
Number   SmartLockNumericStatus        "SmartLock Numeric Status"                       {channel="verisure:smartLock:myverisure:B2XX_YYST:numericStatus"}
String   SmartLockVolume               "SmartLock Volym"     <lock>                     {channel="verisure:smartLock:myverisure:B2XX_YYST:setSmartLockVolume"}
String   SmartLockVolumes              "SmartLock Volumes"                              {channel="verisure:smartLock:myverisure:B2XX_YYST:smartLockVolume"}
String   AlarmHomeInstallationName     "Alarm Home Installation Name"                   {channel="verisure:alarm:myverisure:alarm_2:siteName"}    

// SmartPlugs         
Switch   SmartPlugLamp                 "SmartPlug"               <lock>   [ "Switchable" ]  {channel="verisure:smartPlug:myverisure:A2XY_FGXY:setSmartPlugStatus"}
Switch   SmartPlugGlavaRouter          "SmartPlug Glava Router"  <lock>   [ "Switchable" ]  {channel="verisure:smartPlug:myverisure:XYZX_ABCD:setSmartPlugStatus"}

// DoorWindow
String DoorWindowLocation              "Door Window Location"    {channel="verisure:doorWindowSensor:myverisure:1SG5_GHGT:location"}
String DoorWindowStatus                "Door Window Status"      {channel="verisure:doorWindowSensor:myverisure:1SG5_GHGT:state"}

// UserLocation
String UserLocationEmail               "User Location Email"     {channel="verisure:userPresence:myverisure:userpresence_2:webAccount"}
String UserLocationStatus              "User Location Status"    {channel="verisure:userPresence:myverisure:userpresence_2:userLocationStatus"}
String UserLocationName                "User Location Name"      {channel="verisure:userPresence:myverisure:userpresence_2:userLocationName"}
String UserLocationEmailGlava          "User Location Email Glava"     {channel="verisure:userPresence:myverisure:userpresence_1:webAccount"}
String UserLocationStatusGlava         "User Location Status Glava"    {channel="verisure:userPresence:myverisure:userpresence_1:userLocationStatus"}
String UserLocationNameGlava           "User Location Name Glava"      {channel="verisure:userPresence:myverisure:userpresence_1:userLocationName"}
````

### Sitemap

````
    Frame label="SmartLock and Alarm" {
        Text label="SmartLock and Alarm" icon="groundfloor" {
            Frame label="Yale Doorman SmartLock" {
                Switch item=SmartLock label="Yale Doorman SmartLock" icon="lock.png"
            }
            Frame label="Verisure Alarm" {
                Switch  item=AlarmHome  icon="alarm" label="Verisure Alarm"  mappings=[0="Disarm", 1="Arm Home", 2="Arm Away"]
            }
            Frame label="Yale Doorman SmartLock AutoLock" {
                Switch item=AutoLock label="Yale Doorman SmartLock AutoLock" icon="lock.png"
            }
            Frame label="Yale Doorman SmartLock Volume"  {
                Switch  item=SmartLockVolume  icon="lock" label="Yale Doorman SmartLock Volume"  mappings=["SILENCE"="Silence", "LOW"="Low", "HIGH"="High"]
            }
            Text item=AlarmStatus
            Text item=AlarmNumericStatus
            Text item=AlarmAlarmStatus
            Text item=AlarmHomeInstallationName
            Text item=AlarmChangedByUser
            Text item=AlarmTimeStamp
            Text item=SmartLockStatus
            Text item=SmartLockCurrentStatus
            Text item=SmartLockNumericStatus
        }
    }

    Frame label="SmartPlugs" {
        Text label="SmartPlugs" icon="attic" {
            Frame label="SmartPlug Lamp" {
                Switch item=SmartPlugLamp label="Verisure SmartPlug Lamp" icon="smartheater.png"
            }
        }
    }
````
