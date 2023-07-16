# ConnectedCar Binding

This binding integrates the various connected car service provided into openHAB.
It supports various APIs and brands including Audi, Volkswagen and Skoda.
The binding retrieves  status information and implements control functions for compatible vehicles, which have activated the service.
Depending on brand/model and API a lot of diagnostic data and  access to remote control functions like lock/unlock doors, start/stop climater and other are integrated.

**Important: The availability of this data and remote services depends on brand, car model and user consent**

The binding has automated discovery of available data points and services as good as possible.
Some functions are disabled by factory default, some of them need an additional license.
Contact your dealer for details and ask for activation of all services included in the subscribed plan.

Also make sure that privacy settings are adjusted, you need to consent to make them available to the manufacturer's mobile App and therefore also for the binding.
Some services require the SPIN (security PIN), check Vehicle Thing configuration.

An Internet connection is required to access the services.

## Binding Configuration

The binding itself has no configuration options

## Discovery

An account is required to setup the connection (register on the manufacture's portal, e.g. myAudi.de).

The  Account Thing has to be added manually giving the necessary credentials.
Once the account is online the binding can query all registered vehicles and creates a vehicle thing for each of them.

Make sure to select the correct type of Account Think (e.g. select Sokda Electrical instead of Skoda, which is for non-electrical models).

## CarNet: Audi, VW, Skoda, SEAT

VW has a special API for the ID. models - WeConnect ID., check below for more information.
Skoda has a special API for the Enyak, see below.

Some verified vehicles:

|Brand     |Model       |Year|Type      |Market|Doorlock|Clima|Preheat|Ventilation|WinH|HF |Trips|GeoA|SpeedA|Notes                                                  |
|----------|------------|----|----------|------|--------|-----|-------|-----------|----|---|-----|----|------|-------------------------------------------------------|
|Audi      |A6          |2020|Diesel    |DE/DE |yes     |n/a  |no     |no         |no  |no |no   |no  |no    |Various services not available even with active license|
|Audi      |Q3 Sportback|2020|Gas       |DE/DE |        |     |       |           |    |   |     |    |      |                                                       |
|Audi      |e-tron  300 |2021|Electrical|DE/DE |        |     |       |           |    |   |     |    |      |                                                       |
|Skoda     |Superb      |2020|Hybrid    |DE/CH |        |     |       |           |    |   |     |    |      |                                                       |
|Volkswagen|Arteon      |2021|          |DE/   |        |     |       |           |    |   |     |    |      |                                                       |
|Volkswagen|eGolf       |2020|Electrical|DE/DE |no      |yes  |n/a    |           |    |   |     |    |      |                                                       |
|Volkswagen|eGolf       |2020|Electrical|DE/DE |yes     |yes  |n/a    |n/a        |    |yes*|    |    |      |*Flash only, no honk                                   |
|Volkswagen|eUp         |    |Electrical|DE/DE |        |     |       |           |    |   |     |    |      |                                                       |
|Volkswagen|Passat GTE  |2016|Hybrid    |DE/NL |        |     |n/a    |n/a        |    |n/a|     |    |      |                                                       |
|Volkswagen|Passat Va   |2016|Diesel    |DE/NL |yes     |n/a  |n/a    |           |    |   |     |    |      |                                                       |
|Volkswagen|Tiguan      |2021|Diesel    |DE/DE |yes     |n/a  |yes    |           |    |   |     |    |      |                                                       |
|Volkswagen|Tiguan      |2020|Gas       |DE/SE |yes     |n/a  |yes    |yes        |n/a |yes|yes  |    |      |                                                       |
|Volkswagen|T-Roc Cabrio|2020|          |DE/   |        |     |       |           |    |   |     |    |      |                                                       |

`WinH = Window Heat, HF=Honk and Flash; GeoA = Geo Alerts|SpedA = Speed Alerts`
`blank=unknown/untested, yes=feature available and verified, no=feature available, but not supported/doesn't work; n/a=feature not available`

### API Throtteling

Usually the vehicle sends frequent status updates to the backend.
You have the option to request a forced update from the vehicle using the `control#update` channel.

Various APIs (specifically CarNet) have an integrated throttling, which protects the 12V battery to get drained from frequent status refresh requests. 
Once the limit is reached further API calls are rejected until the engine is started (which usually recharges the battery). 
Therefore be careful by using the "Update Vehicle Status" channel, each time a request is send. 
The channel general#rateLimit allows you to keep an eye on the remaining number of refreshes. 
**This is not a daily limit!** 

For example if this limit is 50 and your vehicle sits for a week you have apx. 7 requests per day (50 requests/7 days).

# Supported Things

## Account, Vehicle and Unit things

There are 2 types of things

- Account Thing implements the online access to the service backend
- Vehicle Thing provides status data and remote control functions

| Thing      | Description                        | Portal                            | API                   |
|------------|------------------------------------|-----------------------------------|-----------------------|
| myaudi     | myAudi Account thing (bridge)      | https://myaudi.de                 | CarNet                |
| volkswagen | myVolkswagen Account  (bridge)     | https://myvolkswagen.de           | CarNet                |
| skoda      | Skoda Connect Account (bridge)     | https://www.skoda-connect.com     | CarNet                |
| seat       | myVolkswagen Account (bridge)      | https://www.googleadservices.com  | CarNet                | 
| vwid       | VW ID. Account (bridge)            | https://myvolkswagen.de           | WeConnect.ID          |
| skoda-e    | Skoda Enyaq Account (bridge)       | https://www.skoda-connect.com     | Skoda Native + CarNet |
| ford       | FordPass Account (bridge)          | https://fordpass.com              | FordPass              |
| wecharge   | VW ID.Charger Account (bridge)     | https://web-home-mobile.apps.emea.vwapps.io/ | WeCharge   |

Note that the VW ID. uses a different account thing than other Volkswagen and same for the Skoda Enyaq compared to other Skoda models.        

| Thing      | Description                         |
|------------|-------------------------------------|
| cnvehicle  | CarNet Thing (Audi, VW, Skoda, SEAT |
| idvehicle  | VW ID. vehicle, e.g. ID.3/ID.4      |
| sevehicle  | Skoda Enyaq vehicle                 |
| fordvehicle| Ford vehicle                        |
| wcbox      | VW Wallbox with WeCharge service    | 

Things have specific configuration options, e.g. number of history entries.
Depending on brand, car model, activated license and privacy settings a different set of channels and actions are supported.

## CarNet Account (myaudi, volkswagen, skoda, seat)

Thing Configuration

|Parameter            | Description                                                                               |Mandatory| Default   |
|---------------------|-------------------------------------------------------------------------------------------|---------|-----------|
|user                 | User ID for your CarNet account (same as login id for the manuafacturer's portal)         | yes     | none      |
|password             | Password for the CarNet account (same as portal)                                          | yes     | none      |
|apiLevelVentilation  | Format for the Preheat/Ventilation API call, see below                                    | no      | 2         |
|apiLevelClimatisation| Format for the Climatisation API call, see below                                          | no      | 2         |

Some functions require different payload formats for the API call.
If you see the control channels (e.g. clima, preheat, ventilation) and the function doesn't work you could change the API level and see if this helps.
Change thos parameters only when needed.

The Account Thing has no channels.

## CarNet Vehicle (cnvehicle)

The thing type `cnvehicle` (CarNet Vehicle) represents one vehicle found under the Account.
If the are multple vehicles registered with this account a dedicated `cnvehicle` is created for each of them.
The vehicle is identified by the unique Vehicle Identification Number (VIN).

The API provides a large number of vehicle data items, the binding clusters the channels into different groups.

**Important: You have to enable the Connected Car services and also give your consent in the MMI system or the online portal.
Use the code on your key holder to setup Connect Car Services and check the manual for additional information.

Please note:
Available channels depend on the specific vehicle type and equipment.
Most of them will be created dynamically depending on availability of the CarNet services.
On startup the binding detects the available API services and deactivates them if the API responds with a 403 status (don't worry if you see those messages in the log).

**Thing initialization might take up to 2 minutes** depending on the amount of available data and performance of the CarNet backend services.
Sometimes the API has high response times (or even times out).
The binding does a frequent recovery check (on each poll cycle) and re-connects to the API service if nessesary.


### CarNet Thing Configuration (cnvehicle)

| Parameter          | Description                                                                 | Mandatory |Default |
|--------------------|-----------------------------------------------------------------------------|-----------|--------|
| pin                | Security PIN (SPIN) required for priveledged functions like lock/unlock     | no        | 1      |
| enableAddressLookup| Enables a reverse lookup into the street address using OpenStreetMap        | no        | enabled|
| numShortTrip       | Number of entries in the Short Trip History (one channel group each)        | no        | 1      |
| numLongTrip        | Number of entries in the Long Trip History (one channel group each)         | no        | 1      |
| numRluHistory      | Number of entries in the RLU History  (one channel group each)              | no        | 1      |
| numDestinations    | Number of entries in the Destination History (one channel group each)       | no        | 1      |
| numSpeedAlerts     | Number of entries in the Speed Alert History (one channel group each)       | no        | 1      |
| numGeoFenceAlerts  | Number of entries in the Geofence Alert History (one channel group each)    | no        | 1      |
| pollingInterval    | Refresh interval in minutes for data refresh (CarNet is not event driven)   | yes       | 15     |

The binding integrates a feature to reverse lookup of the geo coordinates from the vehicle position and provides the street address as channel location#location.
This feature is based on OpenStreetMap, you may disable the lookup in the thing configuration.

For all num-options:
Values can be 1..5 or 0 to disable this history.
This creates a channel group per entry, e.g. destination1, destination2, destination3, rluHistory1, rluHistory2 etc.
The newest entry will be in xxx1 channel group.

### Remote Actions

The binding supports remote actions depending on available services.
Make sure you issues only once action at a time, you could use the `lastActionPending` (ON=action pending) and `lastActionResult`  channels to synchronize.

The result codes are not consistent across the different services:

|Code                                                 | Description                                                                                   |
|-----------------------------------------------------|-----------------------------------------------------------------------------------------------|
|rejected                                             |Action was rejected, because another one was pending or API returned a general error, check log|
|request_fail, failed, api_error                      |Action failed, check log                                                                       | 
|request_in_progress, queued, fetched, request_started|Temporarity status, action is processing, but not yet performed                                |
|timeout                                              |Action timed out and was most likely not performed                                             |
|request_successful, succeeded                        |Request wascompleted successful, action performed                                              |
|request_in_progress                                  |Request id was not found in the backend, maybe a bug                                           |

Enable openHAB's DEBUG or TRACE log for details analysis.

### CarNet Thing Channels

The following channels are available depending on the vehicle type:

|Group            |Channel                  |Type                  |read-only| Description                                                                           |
|-----------------|-------------------------|----------------------|---------|---------------------------------------------------------------------------------------|
| general      | lastUpdate                  | DateTime             | yes     | Last time data has been updated                                                                 |
|              | lastAction                  | String               | yes     | Last action sent to the vehicle (check lastActionStatus for status/result)                      |
|              | lastActionStatus            | String               | yes     | Result from last action sent to the vehicle                                                     |
|              | lastActionPending           | Switch               | yes     | ON: An action was sent and is in status progressing                                             |
|              | rateLimit                   | Number               | yes     | Number of remaining requests before the API gets throttled                                      |
| control      | lock                        | Switch               | no      | Lock/Unlock doors                                                                               |
|              | charge                      | Switch               | no      | Turn charger on/off                                                                             |
|              | climater                    | Switch               | no      | Turn climatisation on/off                                                                       |
|              | targetTemperature           | Number:Temperature   | yes     | Target temperature for the A/C climater                                                         |
|              | targetChgLvl                | Number:Dimensionless | no      | CHarging stops automatically when the given level is reached                                    |
|              | preHeater                   | Switch               | no      | Turn pre-heating on/off                                                                         |
|              | duration                    | Number               | no      | Duration to run the ventilation/pre-heater in minutes                                           |
|              | windowHeat                  | Switch               | no      | Turn window heating on/off                                                                      |
|              | ventilation                 | Switch               | no      | Turn ventilation on/off                                                                         |
|              | flash                       | Switch               | no      | ON: Triggers lights flashing                                                                    |
|              | honkFlash                   | Switch               | no      | ON: Triggers Honk and Flash                                                                     |
|              | hfDuration                  | Number               | no      | Duration in seconds for Flash/Honk &amp; Flash                                                  |
|              | update                      | Switch               | no      | Force status update of vehicle status                                                           |
| status       | odometer                    | Number:Length        | yes     | The  overall distance of the odometer when status was captured                                  |
|              | carMoving                   | Switch               | yes     | ON: Car is moving                                                                               |
|              | currentSpeed                | Number:Speed         | yes     | Current speed when data was last updated                                                        |
|              | vehicleLocked               | Switch               | yes     | ON: Vehicle is completely locked. This includes doors, windows, but also hood and trunk         |
|              | tempOutside                 | Number:Temperature   | yes     | The outside temperature in °C.                                                                  |
|              | maintenanceRequired         | Switch               | yes     | ON: Some type of maintenance is required, check status group for details                        |
|              | parkingBrake                | Switch               | yes     | State of the parking brake                                                                      |
|              | doorsClosed                 | Switch               | yes     | ON: All Doors are closed                                                                        |
|              | windowsClosed               | Switch               | yes     | ON: All Windows are closed                                                                      |
|              | tiresOk                     | Switch               | yes     | ON: Pressure for all tires is ok, otherwise check single tires                                  |
|              | vehicleLights               | Switch               | yes     | Light status                                                                                    |
|              | parkingLight                | Switch               | yes     | ON: Parking light is turned on                                                                  |
|              | monthlyMilage               | Number:Length        | yes     | Average milage per month.                                                                       |
| location     | locationLastUpdate          | DateTime             | yes     | Time of last update for the vehicle position                                                    |
|              | locationPosition            | Location             | yes     | Last known vehicle location                                                                     |
|              | locationAddress             | String               | yes     | Address for the last known vehicle location                                                     |
|              | parkingPosition             | Location             | yes     | Last position where the vehicle was parked                                                      |
|              | parkingAddress              | String               | yes     | Address for the last position where the vehicle was parked                                      |
|              | parkingTime                 | DateTime             | yes     | Time when the vehicle was parked                                                                |
| maintenance  | alarmInspection             | Switch               | yes     | ON: Inspection alarm is on.                                                                     |
|              | distanceToInspection        | Number:Length        | yes     | Distance before the next inspection / service is required.                                      |
|              | timeToInspection            | Number:Time          | yes     | Time until next inspection.                                                                     |
|              | distanceOilChange           | Number:Length        | yes     | Distance until the next oil change is required.                                                 |
|              | distanceAdBlue              | Number:Length        | yes     | Distance before the next Ad Blue fill-up is required.                                           |
|              | oilPercentage               | Number:Dimensionless | yes     | Remaining oil percentage (dip stick)                                                            |
|              | oilWarningChange            | Switch               | yes     | True when Oil is low                                                                            |
|              | oilWarningLevel             | Switch               | yes     | Minimum oil warning level                                                                       |
|              | oilPercentage               | Number:Dimensionless | yes     | Remaining oil percentage (dip stick)                                                            |
|              | intervalOilChange           | Number:Time          | yes     | Distance until next oil change                                                                  |
| range        | totalRange                  | Number:Length        | yes     | Total remaining range.                                                                          |
|              | primaryRange                | Number:Length        | yes     | Range or the primary engine                                                                     |
|              | secondaryRange              | Number:Length        | yes     | Range or the secondary engine                                                                   |
|              | fuelPercentage              | Number:Dimensionless | yes     | Percentage of fuel remaining.                                                                   |
|              | fuelMethod                  | String               | yes     | Method: 0=measured, 1=calculated                                                                |
|              | gasPercentage               | Number:Dimensionless | yes     | Percentage of natural gas remaining                                                             |
| charger      | chargingState               | String               | yes     | Current charging status                                                                         |
|              | chargingStatus              | String               | yes     | Charging status                                                                                 |
|              | chargingMode                | String               | yes     | Indicates the selected charging mode                                                            |
|              | chargingLevel               | Number:Dimensionless | yes     | Current charging level in percent for an electrical car                                         |
|              | batteryState                | Number               | yes     | Battery level                                                                                   |
|              | remainingChargingTime       | Number:Time          | yes     | Time to reach a fully charged battery                                                           |
|              | plugState                   | Switch               | yes     | State of the charging plug, ON=connected                                                        |
|              | lockState                   | Switch               | yes     | ON: Plug is locked, OFF: Plug is unlocked and can be removed                                    |
|              | powerState                  | String               | yes     | Indicates availability of charging power                                                        |
|              | chargingPower               | Number:ElectricPoten | yes     | Current charging power                                                                          |
|              | energyFlow                  | String               | yes     | Energy is flowing / charging                                                                    |
|              | chargingRate                | Number               | yes     | Charging rate in km per hour                                                                    |
|              | maxCurrent                  | Number:ElectricCurre | no      | Maximum current for the charging process                                                        |
|              | chargerName                 | String               | yes     |                                                                                                 |
|              | chargerAddress              | String               | yes     | Location/address of the charging station                                                        |
|              | chargerLastConnect          | DateTime             | yes     | Date/Time of the last charger connection                                                        |
|              | errorCode                   | Number               | yes     | Error code when charging failed                                                                 |
| climater     | climatisationState          | String               | yes     | ON: Climatisation is active                                                                     |
|              | remainingClimatisation      | Number:Time          | yes     | Remaining time for climatisation                                                                |
|              | mirrorHeat                  | Switch               | yes     | Remaining climatisation time                                                                    |
|              | heaterSource                | String               | no      | Indicates the source for heating                                                                |
| tripShort1   | timestamp                   | DateTime             | yes     | Trip time                                                                                       |
|              | mileage                     | Number:Length        | yes     | Distance for this trip.                                                                         |
|              | startMileage                | Number:Length        | yes     | Start Milage for the trip                                                                       |
|              | overallMileage              | Number:Length        | yes     | Overall milage after this trip                                                                  |
|              | avgSpeed                    | Number:Speed         | yes     | Average Speed for this trip                                                                     |
|              | avgElectricConsumption      | Number:Energy        | yes     | Electrical consumption during the trip                                                          |
|              | avgFuelConsumption          | Number:Volume        | yes     | Average fuel consumption for this trip                                                          |
| tripLong1    | timestamp                   | DateTime             | yes     | Trip time                                                                                       |
|              | mileage                     | Number:Length        | yes     | Distance for this trip.                                                                         |
|              | startMileage                | Number:Length        | yes     | Start Milage for the trip                                                                       |
|              | overallMileage              | Number:Length        | yes     | Overall milage after this trip                                                                  |
|              | avgSpeed                    | Number:Speed         | yes     | Average Speed for this trip                                                                     |
|              | avgElectricConsumption      | Number:Energy        | yes     | Electrical consumption during the trip                                                          |
|              | avgFuelConsumption          | Number:Volume        | yes     | Average fuel consumption for this trip                                                          |
| rluHistory1  | rluOperation                | String               | yes     | Action type: lock/unlock                                                                        |
|              | rluTimestamp                | DateTime             | yes     | Timestamp when the Lock/Unlock action was initiated                                             |
|              | rluResult                   | String               | yes     | Action result: 1=ok                                                                             |
| speedAlerts1 | speedAlertType              | String               | yes     | Type of Speed Alert (START_EXCEEDING)                                                           |
|              | geoFenceAlertType           | String               | yes     | Type of Speed Alert (ENTER_REDZONE, EXIT_GREENZONE)                                             |
|              | geoFenceAlertTime           | DateTime             | yes     | When did the alert occurred                                                                     |
|              | speedAlertTime              | DateTime             | yes     | When did the alert occurred                                                                     |
|              | speedAlertLimit             | Number:Speed         | yes     | Exceeded Speed Limit                                                                            |
|              | speedAlertDescr             | String               | yes     | Name of Alert Definition                                                                        |
|              | geoFenceAlertDescr          | String               | yes     | Name of Alert Definition                                                                        |
| destination  | destinationPoi              | String               | yes     | Name of the Point-of-Interest (is this destination has one)                                     |
|              | destinationZip              | String               | yes     | The zip code of the destination address, might be empty                                         |
|              | destinationCity             | String               | yes     | City of the destination address, might be empty                                                 |
|              | destinationName             | String               | yes     | The textual description of this destination, might be empty                                     |
|              | destinationCountry          | String               | yes     | Country of the destination address, might be empty                                              |
|              | destinationLocation         | Location             | yes     | Geo coordinates of this location (Location item type format)                                    |
|              | destinatinStreet            | String               | yes     | Street address of the destination address, might be empty                                       |
|              | destinationSource           | String               | yes     | Source of the destination, e.g. could be the mobile App, might be empty (NaN).                  |
| doors        | doorFrontLeftState          | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | doorFrontLeftLocked         | Switch               | yes     | ON: The left front door is locked                                                               |
|              | doorFrontRightState         | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | doorFrontRightLocked        | Switch               | yes     | ON: The right front door locked                                                                 |
|              | doorRearLeftState           | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | doorRearLeftLocked          | Switch               | yes     | ON: The left rear door is locked                                                                |
|              | doorRearRightState          | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | doorRearRightLocked         | Switch               | yes     | ON: The rear right door is locked.                                                              |
|              | doorRearLeftState           | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | hoodState                   | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | hoodLocked                  | Switch               | yes     | ON: The hood is locked                                                                          |
|              | trunkLidState               | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | trunkLidLocked              | Switch               | yes     | ON: The trunk lid is locked.                                                                    |
|              | covertibleTopState          | Contact              | yes     | Status of the convertible top (OPEN/CLOSED)                                                     |
|              | covertibleTopPos            | Number:Dimensionless | yes     | The position of the convertible top (if any)                                                    |
| windows      | windowFrontLeftState        | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | windowFrontLeftPos          | Number:Dimensionless | yes     | Position of the left front window                                                               |
|              | windowFrontRightState       | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | windowFrontRightPos         | Number:Dimensionless | yes     | Position of the right front window                                                              |
|              | windowRearLeftState         | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | windowRearLeftPos           | Number:Dimensionless | yes     | The position of the left rear window                                                            |
|              | windowRearRightState        | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | windowRearRightPos          | Number:Dimensionless | yes     | Position of the right rear window                                                               |
|              | roofFrontCoverState         | Contact              | yes     | Front roof cover state: OPEN or CLOSED                                                          |
|              | roofFrontCoverPos           | Number:Dimensionless | yes     | Position of the front roof cover                                                                |
|              | roofRearCoverState          | Contact              | yes     | Rear roof cover state: OPEN or CLOSED                                                           |
|              | roofRearCoverPos            | Number:Dimensionless | yes     | Position of the rear roof cover                                                                 |
| tires        | tirePresFrontLeft           | Switch               | yes     | Pressure of the left front tire, ON=OK                                                          |
|              | tirePresFrontRight          | Switch               | yes     | Pressure of the right front tire, ON=OK                                                         |
|              | tirePresRearLeft            | Switch               | yes     | Pressure of the left rear tire, ON=OK                                                           |
|              | tirePresRearRight           | Switch               | yes     | Pressure of the right rear tire, ON=OK                                                          |
|              | tirePresSpare               | Switch               | yes     | Pressure of the spare tire, ON=OK                                                               |
| pictures     | imageUrl1..n                | String               | no      | URL to vehicle picture(s)                                                                                              |


### CarNet Status Codes / Errors

Some common error codes:

|Code         |Message                                                                          |Description                                       |
|-------------|---------------------------------------------------------------------------------|--------------------------------------------------|
|VSR.9007     |Service disabled, legitimation is pending. Check data privacy settings in the MMI|Check Data Privacy settings in MMI, give consent  |
|VSR.9025     |TSS responded: 429                                                               |AOI has been throttled, increase polling interval |
|VSR.9026     |Technical validation error                                                       |Some technical problem, check log, enable DEBUG   |
|business.1003]Request is already pending                                                       |There is already a request peneindg, await result |

### Full Example (CarNet)

.things

```
Bridge connectedcar:volkswagen:vw   "VW" [user="<username>", password="<password>" ] {
    Thing cnvehicle   WAUZZZXXXXXXXXXXX   "My Car"    [ vin="WAUZZZXXXXXXXXXXX", pin="<s-pin>", pollingInterval=15, enableAddressLookup=true ]
}
```

.items

```
Switch                      Locked               "Vehicle Locked"                      { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:status#vehicleLocked" }
Switch                      AllWindowsClosed     "All Windows Closed"                  { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:status#windowsClosed" }
Switch                      TirePressureOk       "Tire Pressure OK"                    { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:status#tiresOk" }
Switch                      ParkingBrake         "Parking Brake"                       { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:status#parkingBrake" }
Number:Length               Reichweite1          "Reichweite [%.1f %unit%]"            { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:range#totalRange" }
Number:Length               MonthlyMilage        "Monthly Milage [%.1f %unit%]"        { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:status#monthlyMilage" }
Number:Dimensionless        LadestandPer1        "Ladestand [%.1f %unit%]"             { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:range#chargingLevel" }
Number:Length               Km1                  "Kilometerstand [%.1f %unit%]"        { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:status#odometer" }
Location                    Position1            "Position"                            { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:location#position" }
Switch                      Update1              "Update"                              { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:control#update" }
Number:Temperature          OutsideTemp1         "Außentemperatur [%.1f %unit%]"       { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:status#tempOutside" }
DateTime                    Timestamp_S          "Timestamp"                           { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripShort1#timestamp" }
Number:Energy               AvgConsumption_S     "Avg Electrical Cons [%.1f %unit%]"   { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripShort1#avgElectricConsumption" }
Number:Speed                AvgSpeed_S           "Avg Speed [%.1f %unit%]"             { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripShort1#avgSpeed" }
Number:Length               TripMilage_S         "Trip Milage [%.1f %unit%]"           { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripShort1#mileage" }
DateTime                    Timestamp_L          "Timestamp"                           { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#timestamp" }
Number:Energy               AvgConsumption_L     "Avg Electrical Cons [%.1f %unit%]"   { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#avgElectricConsumption" }
Number:Speed                AvgSpeed_L           "Avg Speed [%.1f %unit%]"             { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#avgSpeed" }
Number:Length               TripMilage_L         "Trip Milage [%.1f %unit%]"           { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#mileage" }
Number:Length               StartMilage_L        "Start Milage [%.1f %unit%]"          { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#startMileage" }
Number:Length               OverallMilage_L      "Overall Milage [%.1f %unit%]"        { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#overallMileage" }
String                      ChargingStatus       "Charging Status"                     { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:charger#chargingStatus" }
Number                      ChargingError        "Charging Error [%.1f %unit%]"        { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:charger#errorCode" }
String                      PowerState           "Power State"                         { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:charger#powerState" }
String                      ChargingState        "Charging State"                      { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:charger#chargingState" }
String                      EnergyFlow           "Energy Flow"                         { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:charger#energyFlow" }
Number:Dimensionless        BatteryState         "Battery State [%.1f %unit%]"         { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:charger#batteryState" }
Number                      RemainingTime        "Remaining Time [%.1f %unit%]"        { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:charger#remainingTime" }
String                      PlugState            "Plug State"                          { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:charger#plugState" }
String                      PlugLockState        "Plug Lock State"                     { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:charger#lockState" }
Number:Temperature          TargetTemp           "Target Temperature [%.1f %unit%]"    { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:climater#targetTemperature" }
String                      HeaterSource         "Heater Source"                       { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:climater#heaterSource" }
String                      ClimatisationState   "Climatisation State"                 { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:climater#climatisationState" }
Switch                      ZoneFrontLeft        "Zone Front Left"                     { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:climater#frontLeft" }
Switch                      ZoneFrontRight       "Zone Front Right"                    { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:climater#frontRight" }
Switch                      ZoneRearLeft         "Zone Rear Left"                      { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:climater#rearLeft" }
Switch                      ZoneRearRight        "Zone Rear Right"                     { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:climater#rearRight" }
Switch                      MirrorHeating        "Mirror Heating"                      { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:climater#mirrorHeat" }
Switch                      LockVehicle          "Lock Vehicle"                        { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:control#lock" }
Switch                      ClimateControl       "Climate ON/OFF"                      { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:control#climater" }
Switch                      WindowHeater         "Window Heater ON/OFF"                { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:control#windowHeat" }
Switch                      ChargerSwitch        "Charging ON/OFF"                     { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:control#charger" }
Switch                      PreHeater            "Pre-Heater ON/OFF"                   { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:control#preHeater" }
Number                      Current_Speed        "Current Speed [%.1f %unit%]"         { channel="connectedcar:cnvehicle:vw:WAUZZZXXXXXXXXXXX:status#currentSpeed" }
```

## WeConnect ID.: VW ID.3/ID.4

Select this account type only if you have an ID. electrical car.
For other models select the Volkswagen (CarNet) account thing type (see above for further information).

### VW ID. Account Thing (vwid)

You need the credentials used for the myVolkswagen portal (the WeConnect. portal has been shut down).
If you don't already have one you need to create a Volkswagen ID and add the vehicle there.

|Parameter            | Description                                                                               |Mandatory| Default   |
|---------------------|-------------------------------------------------------------------------------------------|---------|-----------|
|user                 | User ID for your myVolkswagen account (same as login id for the myVolkswagen portal)      | yes     | none      |
|password             | Password for the myVolkswagen account (same as portal)                                    | yes     | none      |

### VW ID. Vehicle thing (idvehicle)

### Channels for the VW ID. Vehicles

| Group        | Channel                     | Item Type            |Read only| Description                                                   |
|--------------|-----------------------------|----------------------|---------|---------------------------------------------------------------|
| general      | lastUpdate                  | DateTime             | yes     | Last time data has been updated                               |
| status       | timeInCar                   | DateTime             | yes     | Time in car                                                   |
|              | error                       | String               | yes     | Error status reported by vehicle                              |
| range        | totalRange                  | Number:Length        | yes     | Total remaining range.                                        |
| control      | charge                      | Switch               | yes     | Turn charger on/off                                           |
|              | maxCurrent                  | String               | yes     | Maximum current for the charging process                      |
|              | climater                    | Switch               | no      | Turn climatisation on/off                                     |
|              | windowHeat                  | Switch               | no      | Turn window heating on/off                                    |
|              | update                      | Switch               | no      | Force status update of vehicle status                         |
| charger      | chargingMode                | String               | yes     | Indicates the selected charging mode                          |
|              | chargingState               | String               | yes     | Current charging status                                       |
|              | chargingLevel               | Number:Dimensionless | yes     | Current charging level in percent for an electrical car       |
|              | chargingPower               | Number:ElectricPoten | yes     | Current charging power                                        |
|              | chargingRate                | Number               | yes     | Charging rate in km per hour                                  |
|              | plugState                   | String               | yes     | State of the charging plug, ON=connected                      |
|              | lockState                   | Switch               | yes     | ON: Plug is locked, OFF: Plug is unlocked and can be removed  |
|              | targetChgLvl                | Number:Dimensionless | yes     | Charging stops automatically when the given level is reached  |
|              | remainingChargingTime       | Number:Time          | yes     | Time to reach a fully charged battery                         |
| climater     | climatisationState          | Switch               | yes     | ON: Climatisation is active                                   |
|              | remainingClimatisation      | Number:Time          | yes     | Remaining time for climatisation                              |
|              | targetTemperature           | Number:Temperature   | yes     | Target temperature for the A/C climater                       |
| pictures     | imageUrl1..n                | String               | no      | URL to vehicle picture(s)                                                                                              |

## Skoda Electrical Vehicles

Select this account type only if you have a electrical Skoda  vehicle (e.g. Enyaq).
For other models select the Skoda (CarNet) account thing type (see above for further information).

### Skoda Enyaq Account Thing (skoda-e)

You need the credentials used for the Skoda Connect portal.

|Parameter            | Description                                                                               |Mandatory| Default   |
|---------------------|-------------------------------------------------------------------------------------------|---------|-----------|
|user                 | User ID for your CarNet account (same as login id for the manuafacturer's portal)         | yes     | none      |
|password             | Password for the CarNet account (same as portal)                                          | yes     | none      |

### Skoda Enyaq Vehicle thing (sevehicle)

| Group        | Channel                     | Item Type            |Read only| Description                                                 |
|--------------|-----------------------------|----------------------|---------|-------------------------------------------------------------|
| general      | lastUpdate                  | DateTime             | yes     | Last time data has been updated                             |                                                       |
| range        | totalRange                  | Number:Length        | yes     | Total remaining range.                                                                            |
| control      | charge                      | Switch               | yes     | Turn charger on/off                                                                             |
|              | maxCurrent                  | String               | yes     | Maximum current for the charging process                                                        |
|              | climater                    | Switch               | no      | Turn climatisation on/off                                                                       |
|              | windowHeat                  | Switch               | no      | Turn window heating on/off                                                                      |
|              | update                      | Switch               | no      | Force status update of vehicle status                                                           |
| charger      | chargingMode                | String               | yes     | Indicates the selected charging mode                                                            |
|              | chargingState               | String               | yes     | Current charging status                                                                         |
|              | chargingLevel               | Number:Dimensionless | yes     | Current charging level in percent for an electrical car                                         |
|              | chargingPower               | Number:ElectricPoten | yes     | Current charging power                                                                          |
|              | chargingRate                | Number               | yes     | Charging rate in km per hour                                                                    |
|              | plugState                   | String               | yes     | State of the charging plug, ON=connected                                                        |
|              | lockState                   | Switch               | yes     | ON: Plug is locked, OFF: Plug is unlocked and can be removed                                    |
|              | targetChgLvl                | Number:Dimensionless | yes     | Charging stops automatically when the given level is reached                                    |
|              | remainingChargingTime       | Number:Time          | yes     | Time to reach a fully charged battery                                                           |
| climater     | climatisationState          | Switch               | yes     | ON: Climatisation is active                                                                     |
|              | remainingClimatisation      | Number:Time          | yes     | Remaining time for climatisation                                                                |
|              | targetTemperature           | Number:Temperature   | yes     | Target temperature for the A/C climater                                                         |


## Ford

### FordPass Account Thing (ford)

You need the credentials used for the Skoda Connect portal.

|Parameter            | Description                                                                               |Mandatory| Default   |
|---------------------|-------------------------------------------------------------------------------------------|---------|-----------|
|user                 | User ID for your CarNet account (same as login id for the manuafacturer's portal)         | yes     | none      |
|password             | Password for the CarNet account (same as portal)                                          | yes     | none      |


### FordPass Vehicle Thing (fordvehicle)

| Group        | Channel                     | Item Type            |Read only| Description                                                     |
|--------------|-----------------------------|----------------------|---------|-----------------------------------------------------------------|
| general      | lastUpdate                  | DateTime             | yes     | Last time data has been updated                                 |
| control      | lock                        | Switch               | no      | Lock/Unlock doors                                               |
|              | engine                      | Switch               | no      | Start/stop engine                                               |
|              | update                      | Switch               | no      | Force status update of vehicle status                           |
| status       | odometer                    | Number:Length        | yes     | The  overall distance of the odometer when status was captured                                  |
|              | vehicleLocked               | Switch               | yes     | ON: Vehicle is completely locked. This includes doors, windows, but also hood and trunk         |
|              | doorsClosed                 | Switch               | yes     | ON: All Doors are closed                                                                        |
|              | windowsClosed               | Switch               | yes     | ON: All Windows are closed                                                                      |
|              | tiresOk                     | Switch               | yes     | ON: Pressure for all tires is ok, otherwise check single tires                                  |
|              | vehicleLights               | Switch               | yes     | Light status                                                                                    |
|              | deepSleep                   | Switch               | yes     | ON: On Board Unit/Modem is in deep sleep                                                        |
|              | softwareUpgrade             | Switch               | yes     | ON: A software upgrade is in progress                                                           |
| location     | locationLastUpdate          | DateTime             | yes     | Time of last update for the vehicle position                                                    |
|              | locationPosition            | Location             | yes     | Last known vehicle location                                                                     |
|              | locationAddress             | String               | yes     | Address for the last known vehicle location                                                     |
| range        | totalRange                  | Number:Length        | yes     | Total remaining range.                                          |
|              | primaryRange                | Number:Length        | yes     | Range or the primary engine                                                                     |
|              | secondaryRange              | Number:Length        | yes     | Range or the secondary engine                                                                   |
|              | fuelPercentage              | Number:Dimensionless | yes     | Percentage of fuel remaining.                                                                   |
| maintenance  | oilPercentage               | Number:Dimensionless | yes     | Remaining oil percentage (dip stick)                                                            |
|              | oilWarningLevel             | Switch               | yes     | Minimum oil warning level                                                                       |
| charger      | chargingState               | String               | yes     | Current charging status                                         |
|              | chargingLevel               | Number:Dimensionless | yes     | Current charging level in percent for an electrical car         |
|              | plugState                   | String               | yes     | State of the charging plug, ON=connected                        |
| doors        | doorFrontLeftState          | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | doorFrontRightState         | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | doorRearLeftState           | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | doorRearRightState          | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | doorRearLeftState           | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | innerTailgateState          | Contact              | yes     | Inner Tail Gate state                                                                           |
|              | trunkLidState               | Contact              | yes     | State: OPEN or CLOSED                                                                           |
| windows      | windowFrontLeftState        | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | windowFrontRightState       | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | windowRearLeftState         | Contact              | yes     | State: OPEN or CLOSED                                                                           |
|              | windowRearRightState        | Contact              | yes     | State: OPEN or CLOSED                                                                           |
| tires        | tirePresFrontLeft           | Switch               | yes     | Pressure of the left front tire, ON=OK                                                          |
|              | tirePresFrontRight          | Switch               | yes     | Pressure of the right front tire, ON=OK                                                         |
|              | tirePresRearLeft            | Switch               | yes     | Pressure of the left rear tire, ON=OK                                                           |
|              | tirePresRearRight           | Switch               | yes     | Pressure of the right rear tire, ON=OK                                                          |
|              | tirePresInnerRearLeft       | Switch               | yes     | Pressure of the inner left rear tire, ON=OK                                                     |
|              | tirePresInnerRearRight      | Switch               | yes     | Pressure of the inner right rear tire, ON=OK                                                    |


## WeCharge Wallbox

### WeCharge Account Thing (wecharge) - Configuration

You need the credentials used for the Skoda Connect portal.

|Parameter            | Description                                                                               |Mandatory| Default   |
|---------------------|-------------------------------------------------------------------------------------------|---------|-----------|
|user                 | User ID for your CarNet account (same as login id for the manuafacturer's portal)         | yes     | none      |
|password             | Password for the CarNet account (same as portal)                                          | yes     | none      |

### WeCharge Wallbox Thing (wcbox) - Configuration

| Parameter          | Description                                                                 | Mandatory |Default |
|--------------------|-----------------------------------------------------------------------------|-----------|--------|
| numChangingRecords | Number of charging records to read                                          | yes       | 3      |
| pollingInterval    | Refresh interval in minutes for data refresh (CarNet is not event driven)   | yes       | 15     |

### WeCharge Wallbox Thing (wcbox) - Channels

| Group        | Channel                     | Item Type            |Read only| Description                                                                                     |
|--------------|-----------------------------|----------------------|---------|-------------------------------------------------------------------------------------------------|
| general      | lastUpdate                  | DateTime             | yes     | Last time data has been updated                                                                 |
| charger      | chargerName                 | String               | yes     |                                                                                                 |
|              | chargerAddress              | String               | yes     | Location/address of the charging station                                                        |
|              | chargerLastConnect          | DateTime             | yes     | Date/Time of the last charger connection                                                        |
| subscription | subTariff                   | String               | yes     | Tariff book for the subscription                                                                |
|              | subStatus                   | String               | yes     | Status of the subscription (usually actice)                                                     |
|              | subEndDate                  | DateTime             | yes     | Provides the expiration date of the subscription                                                |
|              | subMonthlyFee               | Number               | yes     | Monthly base fee in local currency                                                              |
| transaction1 | transId                     | String               | yes     | A unique technical transaction id                                                               |
|              | transStart                  | String               | yes     | Date/Time when the charging process was started                                                 |
|              | transEnd                    | String               | yes     | Date/Time when charging was completed                                                           |
|              | transDuration               | Number               | yes     | Duration of the charging process                                                                |
|              | transLocation               | Location             | yes     | Location (Station) where the transaction was performed                                          |
|              | transAddress                | String               | yes     | Location (Station) where the transaction was performed                                          |
|              | transPowerType              | String               | yes     | Type of energy: DC or AC                                                                        |
|              | transEvseId                 | String               | yes     | E Mobility of the station                                                                       |
|              | transPrice                  | Number               | yes     | Price of the charging cycle in local currency                                                   |
|              | transEnergy                 | Number:Energy        | yes     | Consumed energy for the charging process                                                        |
|              | transSubscription           | String               | yes     | Subscription for the transaction                                                                |
| rfid         | rfidId                      | String               | yes     | Label of the RFID Card                                                                          |
|              | rfidStatus                  | String               | yes     |                                                                                                 |
|              | rfidPublicCharging          | Switch               | yes     | ON: Card can be used for charging at public stations                                            |
|              | rfidLastUpdated             | DateTime             | yes     | Date/time of the last update                                                                    |
