# Verisure Binding

This is an openHAB binding for Verisure Smart Alarms by Verisure Securitas.

This binding uses a rest API used by the [Verisure My Pages webpage](https://mypages.verisure.com/login.html)



## Supported Things

This binding supports the following thing types:

- Bridge
- Alarm
- Smoke Detector (climate)
- Water Detector (climate)
- Siren (climate)
- Night Control
- Yaleman Doorman SmartLock
- SmartPlug
- Door/Window Status
- User Presence Status
- Broadband Connection Status
- Mice Detection Status (incl. climate)
- Event Log
- Gateway


## Binding Configuration

You will have to configure the bridge with username and password of a pre-defined user on [Verisure page](https://mypages.verisure.com) that has not activated Multi Factor Authentication (MFA/2FA). 

Verisure allows you to have more than one user so the suggestion is to use a specific user for automation that has MFA/2FA deactivated.
**NOTE:** To be able to have full control over all SmartLock/alarm functionality, the user also needs to have Administrator rights.

You must also configure pin-code(s) to be able to lock/unlock the SmartLock(s) and arm/unarm the Alarm(s).



## Discovery

After the configuration of the Verisure Bridge all of the available Sensors, Alarms, SmartPlugs, SmartLocks, Climate and Mice Detection devices will be discovered and placed as things in the inbox.

## Thing Configuration

Only the bridge require manual configuration. The devices and sensors can be added by hand, or you can let the discovery mechanism automatically find all of your Verisure things.

## Enable Debugging

To enable DEBUG logging for the binding, login to Karaf console and enter:

`openhab> log:set DEBUG org.openhab.binding.verisure`

## Supported Things and Channels

### Verisure Bridge

#### Configuration Options

*   `username` - The username used to connect to https://mypages.verisure.com
    * The user has to have Administrator rights to have full SmartLock functionality

*   `password` - The password used to connect to https://mypages.verisure.com

*   `refresh` - Specifies the refresh interval in seconds

*   `pin` - The username's pin code to arm/disarm alarm and lock/unlock door. In the case of more than one installation and different pin-codes, use a comma separated string where pin-code matches order of installations. The installation order can be found using DEBUG log settings.
    * Two installations where the first listed installation uses a 6 digit pin-code and second listed installation uses a 4 digit pin-code: 123456,1234

If you define the bridge in a things-file the bridge type id is defined as `bridge`, e.g.:

`Bridge verisure:bridge:myverisureBridge verisure:bridge:myverisure`

#### Channels

The following channels are supported:

| Channel Type ID | Item Type | Description                                                                                     |
|-----------------|-----------|-------------------------------------------------------------------------------------------------|
| status          | String    | This channel can be used to trigger an instant refresh by sending a RefreshType.REFRESH command.|


### Verisure Alarm

#### Configuration Options

*   `deviceId` - Device Id
    *   Since Alarm lacks a Verisure ID, the following naming convention is used for alarm on installation ID 123456789: 'alarm123456789'. Installation ID can be found using DEBUG log settings

#### Channels

The following channels are supported:

| Channel Type ID     | Item Type | Description                                                                               |
|---------------------|-----------|-------------------------------------------------------------------------------------------|
| changedByUser       | String    | This channel reports the user that last changed the state of the alarm.                   |
| changedVia          | String    | This channel reports the method used to change the status.                                |
| timestamp           | DateTime  | This channel reports the last time the alarm status was changed.                          |
| installationName    | String    | This channel reports the installation name.                                               |
| installationId      | Number    | This channel reports the installation ID.                                                 |
| alarmStatus         | String    | This channel is used to arm/disarm the alarm. Available alarm status are "DISARMED", "ARMED_HOME" and "ARMED_AWAY".|
| alarmTriggerChannel | trigger   | This is a trigger channel that receives events.                                           |

### Verisure Yaleman SmartLock

#### Configuration Options

*   `deviceId` - Device Id
    *   Sensor Id. Example 5A4C35FT (Note: Verisure ID, found in the Verisure App or My Pages)

#### Channels

The following channels are supported:

| Channel Type ID         | Item Type | Description                                                                                              |
|-------------------------|-----------|----------------------------------------------------------------------------------------------------------|
| changedByUser           | String    | This channel reports the user that last changed the state of the alarm.                                  |
| timestamp               | DateTime  | This channel reports the last time the alarm status was changed.                                         |
| changedVia              | String    | This channel reports the method used to change the status.                                               |
| motorJam                | Switch    | This channel reports if the SmartLock motor has jammed.                                                  |
| location                | String    | This channel reports the location of the device.                                                         |
| installationName        | String    | This channel reports the installation name.                                                              |
| installationId          | Number    | This channel reports the installation ID.                                                                |
| smartLockStatus         | Switch    | This channel is used to lock/unlock.                                                                     |
| autoRelock              | Switch    | This channel is used to configure auto-lock functionality. Only supported for users with Administrator rights.                                                |
| smartLockVolume         | String    | This channel is used to set the volume level. Available volume settings are "SILENCE", "LOW" and "HIGH". Only supported for users with Administrator rights.|
| smartLockVoiceLevel     | String    | This channel is used to set the voice level. Available voice level settings are "ESSENTIAL" and "NORMAL". Only supported for users with Administrator rights.|
| smartLockTriggerChannel | trigger    | This is a trigger channel that receives events. |

### Verisure SmartPlug

#### Configuration Options

*   `deviceId` - Device Id
     *   Sensor Id. Example 5A4C35FT (Note: Verisure ID, found in the Verisure App or My Pages or on the sensor itself)

#### Channels

The following channels are supported:

| Channel Type ID         | Item Type | Description                                                       |
|-------------------------|-----------|-------------------------------------------------------------------|
| hazardous               | Switch    | This channel reports if the smart plug is configured as hazardous.|
| location                | String    | This channel reports the location of the device.                  |
| installationName        | String    | This channel reports the installation name.                       |
| installationId          | Number    | This channel reports the installation ID.                         |
| smartPlugStatus         | Switch    | This channel is used to turn smart plug on/off.                   |
| smartPlugTriggerChannel | trigger   | This is a trigger channel that receives events.                   |

### Verisure Smoke Detector

#### Configuration Options

*   `deviceId` - Device Id
     *   Sensor Id. Example 5A4C35FT (Note: Verisure ID, found in the Verisure App or on the sensor itself)

#### Channels

The following channels are supported:

| Channel Type ID             | Item Type             | Description                                                                 |
|-----------------------------|-----------------------|-----------------------------------------------------------------------------|
| temperature                 | Number:Temperature    | This channel reports the current temperature.                               |
| humidity                    | Number                | This channel reports the current humidity in percentage.                    |
| humidityEnabled             | Switch                | This channel reports if the Climate is device capable of reporting humidity.|
| timestamp                   | DateTime              | This channel reports the last time this sensor was updated.                 |
| location                    | String                | This channel reports the location of the device.                            |
| installationName            | String                | This channel reports the installation name.                                 |
| installationId              | Number                | This channel reports the installation ID.                                   |
| lowBattery                  | Switch                | This channel reports if the battery level is low.                           | 
| smokeDetectorTriggerChannel | trigger               | This is a trigger channel that receives events.                             |

### Verisure Water Detector

#### Configuration Options

*   `deviceId` - Device Id
     *   Sensor Id. Example 5A4C35FT (Note: Verisure ID, found in the Verisure App or My Pages or on the sensor itself)

#### Channels

The following channels are supported:



| Channel Type ID             | Item Type             | Description                                                  | 
|-----------------------------|-----------------------|--------------------------------------------------------------|                                                                                                                                          
| temperature                 | Number:Temperature    | This channel reports the current temperature.                |
| timestamp                   | DateTime              | This channel reports the last time this sensor was updated.  |
| location                    | String                | This channel reports the location of the device.             |
| installationName            | String                | This channel reports the installation name.                  |
| installationId              | Number                | This channel reports the installation ID.                    |
| waterDetectorTriggerChannel | trigger               | This is a trigger channel that receives events.              |


### Verisure Siren

#### Configuration Options

*   `deviceId` - Device Id
     *   Sensor Id. Example 5A4C35FT (Note: Verisure ID, found in the Verisure App or My Pages or on the sensor itself)

#### Channels

The following channels are supported:
 
| Channel Type ID     | Item Type             | Description                                                | 
|---------------------|-----------------------|------------------------------------------------------------|                                                                                                                                          
| temperature         | Number:Temperature    | This channel reports the current temperature.              |
| timestamp           | DateTime              | This channel reports the last time this sensor was updated.|
| location            | String                | This channel reports the location.                         |
| installationName    | String                | This channel reports the installation name.                |
| installationId      | Number                | This channel reports the installation ID.                  |
| lowBattery          | Switch                | This channel reports if the battery level is low.          | 
| sirenTriggerChannel | trigger               | This is a trigger channel that receives events.            |

### Verisure Night Control

#### Configuration Options

*   `deviceId` - Device Id
     *   Sensor Id. Example 5A4C35FT (Note: Verisure ID, found in the Verisure App or My Pages or on the sensor itself)

#### Channels

The following channels are supported: 
| Channel Type ID            | Item Type             | Description                                                | 
|----------------------------|-----------------------|------------------------------------------------------------|                                                                                                                                          
| temperature                | Number:Temperature    | This channel reports the current temperature.              |
| timestamp                  | DateTime              | This channel reports the last time this sensor was updated.|
| location                   | String                | This channel reports the location.                         |
| installationName           | String                | This channel reports the installation name.                |
| installationId             | Number                | This channel reports the installation ID.                  |
| lowBattery                 | Switch                | This channel reports if the battery level is low.          | 
| nightControlTriggerChannel | trigger               | This is a trigger channel that receives events.            |

### Verisure DoorWindow Sensor

#### Configuration Options

*   `deviceId` - Device Id
     *   Sensor Id. Example 5A4C35FT (Note: Verisure ID, found in the Verisure App or My Pages or on the sensor itself)

#### Channels

The following channels are supported:

| Channel Type ID          | Item Type | Description                                                                 |
|--------------------------|-----------|-----------------------------------------------------------------------------|
| state                    | Contact   | This channel reports the if the door/window is open or closed (OPEN/CLOSED).|
| timestamp                | DateTime  | This channel reports the last time this sensor was updated.                 |
| location                 | String    | This channel reports the location of the device.                            |
| installationName         | String    | This channel reports the installation name.                                 |
| installationId           | Number    | This channel reports the installation ID.                                   |
| lowBattery               | Switch    | This channel reports if the battery level is low.                           | 
| doorWindowTriggerChannel | trigger   | This is a trigger channel that receives events.                             |


### Verisure User Presence

#### Configuration Options

*   `deviceId` - Device Id
     *  Since User presence lacks a Verisure ID, it is constructed from the user's email address, where the '@' sign is removed, and the site id. The following naming convention is used for User presence on site id 123456789 for a user with email address test@gmail.com: 'uptestgmailcom123456789'. Installation ID can be found using DEBUG log settings.

#### Channels

The following channels are supported:

| Channel Type ID    | Item Type | Description                                                             |
|--------------------|-----------|-------------------------------------------------------------------------|
| userLocationStatus | String    | This channel reports the user presence status (HOME/AWAY).              |
| timestamp          | DateTime  | This channel reports the last time the User Presence status was changed.|
| userName           | String    | This channel reports the user's name.                                   |
| webAccount         | String    | This channel reports the user's email address.                          |
| userDeviceName     | String    | This channel reports the name of the user device.                       |
| installationName   | String    | This channel reports the installation name.                             |
| installationId     | Number    | This channel reports the installation ID.                               |

### Verisure Broadband Connection

#### Configuration Options

*   `deviceId` - Device Id
     *  Since Broadband connection lacks a Verisure ID, the following naming convention is used for Broadband connection on site id 123456789: 'bc123456789'. Installation ID can be found using DEBUG log settings.

#### Channels

The following channels are supported:

| Channel Type ID | Item Type | Description                                                                    |
|-----------------|-----------|--------------------------------------------------------------------------------|
| connected       | String    | This channel reports the broadband connection status (true means connected).   |
| timestamp       | DateTime  | This channel reports the last time the Broadband connection status was checked.|
| installationName| String    | This channel reports the installation name.                                    |
| installationId  | Number    | This channel reports the installation ID.                                      |

### Verisure Mice Detection

#### Configuration Options

*   `deviceId` - Device Id
     *  Sensor Id. Example 5A4C35FT (Note: Verisure ID, found in the Verisure App or My Pages or on the sensor itself)

#### Channels

The following channels are supported:
 
| Channel Type ID             | Item Type          | Description                                                                         | 
|-----------------------------|--------------------|-------------------------------------------------------------------------------------|                                                                                                                                          
| countLatestDetection        | Number             | This channel reports the number of mice counts the latest detection during last 24. | 
| countLast24Hours            | Number             | This channel reports the total number of mice counts the last 24h.                  |
| durationLatestDetection     | Number:Time        | This channel reports the detection duration in min of latest detection.             |
| durationLast24Hours         | Number:Time        | This channel reports the total detection duration in min for the last 24 hours.     |
| timestamp                   | DateTime           | This channel reports time for the last mouse detection.                             |
| temperature                 | Number:Temperature | This channel reports the current  temperature.                                      |
| temperatureTimestamp        | DateTime           | This channel reports the time for the last temperature reading.                     |
| location                    | String             | This channel reports the location of the device.                                    |
| installationName            | String             | This channel reports the installation name.                                         |
| installationId              | Number             | This channel reports the installation ID.                                           |
| miceDetectionTriggerChannel | trigger            | This is a trigger channel that receives events.                                     |

### Verisure Event Log

#### Configuration Options

*   `deviceId` - Device Id
     *  Since Event Log lacks a Verisure ID, the following naming convention is used for Event Log on site id 123456789: 'el123456789'. Installation ID can be found using DEBUG log settings.
             

#### Channels

The following channels are supported:

| Channel Type ID     | Item Type | Description                                                             |
|---------------------|-----------|-------------------------------------------------------------------------|
| lastEventLocation   | String    | This channel reports location for last event in event log.              |
| lastEventDeviceId   | String    | This channel reports device ID for last event in event log.             |
| lastEventDeviceType | String    | This channel reports device type for last event in event log.           |
| lastEventType       | String    | This channel reports type for last event in event log.                  |
| lastEventCategory   | String    | This channel reports category for last event in event log.              |
| lastEventTime       | DateTime  | This channel reports time for last event in event log.                  |
| lastEventUserName   | String    | This channel reports user name for last event in event log.             |
| eventLog            | String    | This channel reports the last 15 events from event log in a JSON array. |

### Verisure Gateway

#### Configuration Options

*    `deviceId` - Device Id
     *  Sensor Id. Example 3B4C35FT (Note: Verisure ID, found in the Verisure App or My Pages or on the Gateway itself)

#### Channels

The following channels are supported:

| Channel Type ID     | Item Type | Description                                                          |
|---------------------|-----------|----------------------------------------------------------------------|
| model               | String    | This channel reports gateway model.                                  |
| location            | String    | This channel reports gateway location.                               |
| statusGSMOverUDP    | String    | This channel reports communication status for GSM over UDP.          |
| testTimeGSMOverUDP  | DateTime  | This channel reports last communication test time for GSM over UDP.  |
| statusGSMOverSMS    | String    | This channel reports communication status for GSM over SMS.          |
| testTimeGSMOverSMS  | DateTime  | This channel reports last communication test time for GSM over SMS.  |
| statusGPRSOverUDP   | String    | This channel reports communication status for GPRS over UDP.         |
| testTimeGPRSOverUDP | DateTime  | This channel reports last communication test time for GPRS over UDP. |
| statusETHOverUDP    | String    | This channel reports communication status for ETH over UDP.          |
| testTimeETHOverUDP  | DateTime  | This channel reports last communication test time for ETH over UDP.  |

## Trigger Events

To be able to get trigger events you need an active Event Log thing, you can either get it via auto-detection or create your own in a things-file.
The following trigger events are defined per thing type:

| Event Type        | Thing Type    | Description                                                |
|-------------------|---------------|------------------------------------------------------------|
| LOCK              | SmartLock     | SmartLock has been locked.                                 |
| UNLOCK            | SmartLock     | SmartLock has been locked.                                 |
| LOCK_FAILURE      | SmartLock     | SmartLock has failed to lock/unlock.                       |
| ARM               | Alarm         | Alarm has been armed.                                      |
| DISARM            | Alarm         | Alarm has been disarmed.                                   |
| DOORWINDOW_OPENED | DoorWindow    | DoorWindow has detected a door/window that opened.         |
| DOORWINDOW_CLOSED | DoorWindow    | DoorWindow has detected a door/window that closed.         |
| INTRUSION         | DoorWindow    | DoorWindow has detected an intrusion.                      |
| FIRE              | SmokeDetector | SmokeDetector has detected fire/smoke.                     |
| WATER             | WaterDetector | WaterDetector has detected a water leak.                   |
| MICE              | MiceDetector  | WaterMiceDetector has detected a mouse.                    |
| COM_FAILURE       | All           | Communication failure detected.                            |
| COM_RESTORED      | All           | Communication restored.                                    |
| COM_TEST          | All           | Communication test.                                        |
| BATTERY_LOW       | All           | Battery low level detected.                                |
| BATTERY_RESTORED  | All           | Battery level restored.                                    |
| SABOTAGE_ALARM    | All           | Sabotage alarm detected.                                   |
| SABOTAGE_RESTORED | All           | Sabotage alarm restored.                                   |

## Example

### Things-file

````
// Bridge configuration
Bridge verisure:bridge:myverisure "Verisure Bridge" [username="x@y.com", password="1234", refresh="600", pin="111111"] {

     Thing alarm         JannesAlarm         "Verisure Alarm"                  [ deviceId="alarm123456789" ]
     Thing smartLock     JannesSmartLock     "Verisure Entrance Yale Doorman"  [ deviceId="3C446NPO" ]
     Thing smartPlug     JannesSmartPlug     "Verisure SmartPlug"              [ deviceId="3D7GMANV" ]
     Thing waterDetector JannesWaterDetector "Verisure Water Detector"         [ deviceId="3WETQRH5" ]
     Thing userPresence  JannesUserPresence  "Verisure User Presence"          [ deviceId="uptestgmailcom123456789" ]
     Thing eventLog      JannesEventLog      "Verisure Event Log"              [ deviceId="el123456789" ]
     Thing gateway       JannesGateway       "Verisure Gateway"                [ deviceId="3AFG5673" ]
}
````

### Items-file

````
Group gVerisureMiceDetection
Group gVerisureEventLog
Group gVerisureGateway

// SmartLock and Alarm
Switch   SmartLock                     "Verisure SmartLock"  <lock>   [ "Switchable" ]  {channel="verisure:smartLock:myverisure:JannesSmartLock:smartLockStatus"}
Switch   AutoLock                      "AutoLock"            <lock>   [ "Switchable" ]  {channel="verisure:smartLock:myverisure:JannesSmartLock:autoRelock"}
String   SmartLockVolume               "SmartLock Volume"     <lock>                    {channel="verisure:smartLock:myverisure:JannesSmartLock:smartLockVolume"}
DateTime SmartLockLastUpdated          "SmartLock Last Updated [%1$tY-%1$tm-%1$td %1$tR]" {channel="verisure:smartLock:myverisure:JannesSmartLock:timestamp"}
String   AlarmHome                     "Alarm Home"          <alarm>                    {channel="verisure:alarm:myverisure:JannesAlarm:alarmStatus"}
DateTime  AlarmLastUpdated             "Verisure Alarm Last Updated [%1$tY-%1$tm.%1$td %1$tR]"               {channel="verisure:alarm:myverisure:JannesAlarm:timestamp"}
String   AlarmChangedByUser            "Verisure Alarm Changed By User"                 {channel="verisure:alarm:myverisure:JannesAlarm:changedByUser"}


// SmartPlugs
Switch   SmartPlugLamp                 "SmartPlug"               <lock>   [ "Switchable" ]  {channel="verisure:smartPlug:myverisure:4ED5ZXYC:smartPlugStatus"}
Switch   SmartPlugGlavaRouter          "SmartPlug Glava Router"  <lock>   [ "Switchable" ]  {channel="verisure:smartPlug:myverisure:JannesSmartPlug:smartPlugStatus"}

// DoorWindow
String DoorWindowLocation              "Door Window Location"    {channel="verisure:doorWindowSensor:myverisure:1SG5GHGT:location"}
String DoorWindowStatus                "Door Window Status"      {channel="verisure:doorWindowSensor:myverisure:1SG5GHGT:state"}

// UserPresence
String UserName                        "User Name"               {channel="verisure:userPresence:myverisure:JannesUserPresence:userName"}
String UserLocationEmail               "User Location Email"     {channel="verisure:userPresence:myverisure:JannesUserPresence:webAccount"}
String UserLocationName                "User Location Name"      {channel="verisure:userPresence:myverisure:JannesUserPresence:userLocationStatus"}
String UserNameGlava                   "User Name Glava"               {channel="verisure:userPresence:myverisure:userpresencetestgmailcom123456789:userName"}
String UserLocationEmailGlava          "User Location Email Glava"     {channel="verisure:userPresence:myverisure:userpresencetestgmailcom123456789:webAccount"}
String UserLocationNameGlava           "User Location Name Glava"      {channel="verisure:userPresence:myverisure:userpresencetestgmailcom1123456789:userLocationStatus"}

// EventLog
String LastEventLocation                "Last Event Location"     (gVerisureEventLog) {channel="verisure:eventLog:myverisure:JannesEventLog:lastEventLocation"}
String LastEventDeviceId                "Last Event Device ID"    (gVerisureEventLog) {channel="verisure:eventLog:myverisure:JannesEventLog:lastEventDeviceId"}
String LastEventDeviceType              "Last Event Device Type"  (gVerisureEventLog) {channel="verisure:eventLog:myverisure:JannesEventLog:lastEventDeviceType"}
String LastEventType                    "Last Event Type"         (gVerisureEventLog) {channel="verisure:eventLog:myverisure:JannesEventLog:lastEventType"}
String LastEventCategory                "Last Event Category"     (gVerisureEventLog) {channel="verisure:eventLog:myverisure:JannesEventLog:lastEventCategory"}
DateTime LastEventTime                  "Last Event Time [%1$tY-%1$tm-%1$td %1$tR]"    (gVerisureEventLog) {channel="verisure:eventLog:myverisure:JannesEventLog:lastEventTime"}
String LastEventUserName                "Last Event User Name"    (gVerisureEventLog) {channel="verisure:eventLog:myverisure:JannesEventLog:lastEventUserName"}
String EventLog                         "Event Log"               {channel="verisure:eventLog:myverisure:JannesEventLog:eventLog"}

// Gateway
String VerisureGatewayModel              "Gateway Model"                   (gVerisureGateway) {channel="verisure:gateway:myverisure:JannesGateway:model"}
String VerisureGatewayLocation           "Gateway Location"                (gVerisureGateway) {channel="verisure:gateway:myverisure:JannesGateway:location"}
String VerisureGWStatusGSMOverUDP        "Gateway Status GSMOverUDP"       (gVerisureGateway) {channel="verisure:gateway:myverisure:JannesGateway:statusGSMOverUDP"}
DateTime VerisureGWTestTimeGSMOverUDP    "Gateway Test Time GSMOverUDP"    (gVerisureGateway) {channel="verisure:gateway:myverisure:JannesGateway:testTimeGSMOverUDP"}
String VerisureGWStatusGSMOverSMS        "Gateway Status GSMOverSMS"       (gVerisureGateway)  {channel="verisure:gateway:myverisure:JannesGateway:statusGSMOverSMS"}
DateTime VerisureGWTestTimeGSMOverSMS    "Gateway Test Time GSMOverSMS"    (gVerisureGateway) {channel="verisure:gateway:myverisure:JannesGateway:testTimeGSMOverSMS"}
String VerisureGWStatusGPRSOverUDP       "Gateway Status GPRSOverUDP"      (gVerisureGateway) {channel="verisure:gateway:myverisure:JannesGateway:statusGPRSOverUDP"}
DateTime VerisureGWTestTimeGPRSOverUDP   "Gateway Test Time GPRSOverUDP"   (gVerisureGateway) {channel="verisure:gateway:myverisure:JannesGateway:testTimeGPRSOverUDP"}
String VerisureGWStatusETHOverUDP        "Gateway Status ETHOverUDP"       (gVerisureGateway) {channel="verisure:gateway:myverisure:JannesGateway:statusETHOverUDP"}
DateTime VerisureGWTestTimeETHOverUDP    "Gateway Test Time ETHOverUDP"    (gVerisureGateway) {channel="verisure:gateway:myverisure:JannesGateway:testTimeETHOverUDP"}

// Broadband Connection
String CurrentBBStatus                 "Broadband Connection Status"       {channel="verisure:broadbandConnection:myverisure:bc123456789:connected"}

// Verisure Mice Detection
Number MouseCountLastDetection          "Mouse Count Last Detection"  (gVerisureMiceDetection)     {channel="verisure:miceDetection:myverisure:2CFZH80U:countLatestDetection"}
Number MouseCountLast24Hours            "Mouse Count Last 24 Hours"    (gVerisureMiceDetection)   {channel="verisure:miceDetection:myverisure:2CFZH80U:countLast24Hours"}
DateTime MouseLastDetectionTime         "Mouse Last Detection Time [%1$tY-%1$tm-%1$td %1$tR]" (gVerisureMiceDetection) {channel="verisure:miceDetection:myverisure:2CFZH80U:timestamp"}
Number MouseDurationLastDetection       "Mouse Duration Last Detection"    (gVerisureMiceDetection)   {channel="verisure:miceDetection:myverisure:2CFZH80U:durationLatestDetection"}
Number MouseDurationLast24Hours         "Mouse Duration Last 24 Hours"    (gVerisureMiceDetection)   {channel="verisure:miceDetection:myverisure:2CFZH80U:durationLast24Hours"}
Number MouseDetectionTemperature        "Mouse Detection Temperature [%.1f C]"  <temperature> (gTemperaturesVerisure, gVerisureMiceDetection) ["CurrentTemperature"] {channel="verisure:miceDetection:myverisure:2CFZH80U:temperature"}
DateTime MouseDetectionTemperatureTime  "Mouse Detection Temperature Time [%1$tY-%1$tm-%1$td %1$tR]" (gVerisureMiceDetection) {channel="verisure:miceDetection:myverisure:2CFZH80U:temperatureTimestamp"}
String MouseDetectionLocation           "Mouse Detection Location"      (gVerisureMiceDetection)   {channel="verisure:miceDetection:myverisure:2CFZH80U:location"}

````

### Sitemap

````
    Frame label="SmartLock and Alarm" {
        Text label="SmartLock and Alarm" icon="groundfloor" {
            Frame label="Yale Doorman SmartLock" {
                Switch item=SmartLock label="Yale Doorman SmartLock" icon="lock.png"
            }
            Frame label="Verisure Alarm" {
                Switch  item=AlarmHome  icon="alarm" label="Verisure Alarm"  mappings=["DISARMED"="Disarm", "ARMED_HOME"="Arm Home", "ARMED_AWAY"="Arm Away"]
            }
            Frame label="Yale Doorman SmartLock AutoLock" {
                Switch item=AutoLock label="Yale Doorman SmartLock AutoLock" icon="lock.png"
            }
            Frame label="Yale Doorman SmartLock Volume"  {
                Switch  item=SmartLockVolume  icon="lock" label="Yale Doorman SmartLock Volume"  mappings=["SILENCE"="Silence", "LOW"="Low", "HIGH"="High"]
            }
            Text item=AlarmHomeInstallationName label="Alarm Installation [%s]"
            Text item=AlarmChangedByUser label="Changed by user [%s]"
            Text item=AlarmLastUpdated
            Text item=SmartLockStatus label="SmartLock status [%s]"
            Text item=SmartLockLastUpdated
            Text item=SmartLockOperatedBy label="Changed by user [%s]"
            Text item=DoorWindowStatus label="Door State"
            Text item=DoorWindowLocation
        }
    }

    Frame label="SmartPlugs" {
        Text label="SmartPlugs" icon="attic" {
            Frame label="SmartPlug Lamp" {
                Switch item=SmartPlugLamp label="Verisure SmartPlug Lamp" icon="smartheater.png"
            }
        }
    }

    Frame label="User Presence" {
		Text label="User Presence" icon="attic" {
			Frame label="User Presence Champinjonvägen" {
				Text item=UserName label="User Name [%s]"
				Text item=UserLocationEmail label="User Email [%s]"
                     Text item=UserLocationStatus label="User Location Status [%s]"
			}
		}
	}

     Frame label="Broadband Connection" {
		Text label="Broadband Connection" icon="attic" {
			Frame label="Broadband Connection Champinjonvägen" {
				Text item=CurrentBBStatus label="Broadband Connection Status [%s]"
			}
		}
	}

    Frame label="Mice Detection" {
            Group item=gVerisureMiceDetection label="Verisure Mice Detection"
    }

    Frame label="Event Log" {
            Group item=gVerisureEventLog label="Verisure Event Log"
    }

    Frame label="Gateway" {
            Group item=gVerisureGateway label="Verisure Gateway"
    }

````

### Rules

````
import org.openhab.core.types.RefreshType

rule "Handle Refesh of Verisure"
when
    Item RefreshVerisure received command
then
    var String command = RefreshVerisure.state.toString.toLowerCase
    logDebug("RULES","RefreshVerisure Rule command: " + command)
    sendCommand(VerisureBridgeStatus, RefreshType.REFRESH)
end

rule "Verisure SmartLock Event Triggers"
when
    Channel "verisure:smartLock:myverisure:JannesSmartLock:smartLockTriggerChannel" triggered
then
    logInfo("RULES", "A SmartLock trigger event was detected:" + receivedEvent.toString())
end

rule "Verisure Gateway Event Triggers"
when
    Channel "verisure:gateway:myverisure:JannesGateway:gatewayTriggerChannel" triggered
then
    logInfo("RULES", "A Gateway trigger event was detected:" + receivedEvent.toString())
end

rule "Verisure DoorWindow Event Triggers"
when
    Channel "verisure:doorWindowSensor:myverisure:1SG5GHGT:doorWindowTriggerChannel" triggered
then
    logInfo("RULES", "A DoorWindow trigger event was detected:" + receivedEvent.toString())
end


````
