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

Make sure to select the correct type of Account Think (e.g. select Sokda Enyaq instead of Skoda, which is for non-electrical models).

## CarNet: Audi, VW, Skoda

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

There are 2 types of things

- Account Thingimplements the online access to the service backend
- Vehicle Thing provides status data and remote control functions

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


### Thing Configuration

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

### Thing Channels

The following channels are available depending on the vehicle type:

|Group            |Channel                  |Type                  |read-only| Description                                                                           |
|-----------------|-------------------------|----------------------|---------|---------------------------------------------------------------------------------------|
| status          | kilometerStatus         | Number:Length        | yes     | The kilometers from the odometer when status was captured.                                 |
|                 | tempOutside             | Number:Temperature   | yes     | The outside temperature in °C.                                                             |
|                 | parkingLight            | Switch               | yes     | ON: Parking light is turned on                                                             |
|                 | parkingBrake            | Switch               | yes     | State of the parking brake                                                                 |
|                 | currentSpeed            | Number:Speed         | yes     | Current speed when data was last updated                                                   |
|                 | vehicleLocked           | Switch               | yes     | ON: Vehicle is completely locked. This includes doors, windows, but also hood and trunk    |
|                 | maintenanceRequired     | Switch               | yes     | ON: Some type of maintenance is required, check status group for details                   |
|                 | windowsClosed           | Switch               | yes     | ON: All Windows are closed.                                                                |
|                 | tiresOk                 | Switch               | yes     | ON: Pressure for all tires is ok, otherwhise check single tires.                           |
|                 | monthlyMilage           | Number:Length        | yes     | Average milage per month.                                                                  |
| control         | update                  | Switch               | no      | Force status update of vehicle status                                                      |
|                 | lock                    | Switch               | no      | Lock/Unlock doors                                                                          |
|                 | climater                | Switch               | no      | Turn climater on/off                                                                       |
|                 | targetTemperature       | Number:Temperature   | no      | Target temperature for the A/C climator                                                    |
|                 | heaterSource            | String               | no      | Indicates the source for heating                                                           |
|                 | windowHeat              | Switch               | no      | Turn window heating on/off                                                                 |
|                 | preHeater               | Switch               | no      | Turn pre-heating on/off                                                                    |
|                 | ventilation             | Switch               | no      | Turn ventilation on/off                                                                    |
|                 | duration                | Number               | no      |                                                                                            |
|                 | charge                  | Switch               | no      | Turn charger on/off                                                                        |
|                 | maxCurrent              | Number               | no      | Set the maximum current for the charging process                                           |
|                 | flash                   | Switch               | no      | ON: Triggers lights flashing                                                               |
|                 | honkFlash               | Switch               | no      | ON: Treiggers Honk and Flash                                                               |
|                 | hfDuration              | Number               | no      | Duration in seconds for Flash/Honk &amp; Flash                                             |
| general         | lastUpdate              | DateTime             | yes     | Last time data has been updated.                                                           |
|                 | lastAction              | String               | yes     | Last action sent to the vehicle (check lastActionStatus for status/result)                 |
|                 | lastActionStatus        | String               | yes     | Result from last action sent to the vehicle                                                |
|                 | lastActionPending       | Switch               | yes     | ON: An action was send to the vehicle and is in status progressing                         |
|                 | pictureUrl&lt;n&gt;     | String               | yes     | URL to picture(s) provided by manufacturer |
| location        | locationPosition        | Location             | yes     | Last known vehicle location                                                                |
|                 | locationLastUpdate      | DateTime             | yes     | Time of last update for the vehicle position.                                              |
|                 | locationAddress         | String               | yes     | Address for the last known vehicle location                                                |
|                 | parkingPosition         | Location             | yes     | Last position where the vehicle was parked.                                                |
|                 | parkingAddress          | String               | yes     | Address for the last position where the vehicle was parked.                                |
|                 | parkingTime             | DateTime             | yes     | Time when the vehicle was parked                                                           |
| climater        | climatisationState      | String               | yes     | ON: Climatisation is active                                                                |
|                 | frontLeft               | Switch               | yes     | ON: Climatisation for the front left zone is active                                        |
|                 | frontRight              | Switch               | yes     | ON: Climatisation for the front left zone is active                                        |
|                 | rearLeft                | Switch               | yes     | ON: Climatisation for the front left zone is active                                        |
|                 | rearRight               | Switch               | yes     | ON: Climatisation for the front left zone is active                                        |
|                 | mirrorHeat              | Switch               | yes     | ON: Mirror heating is active                                                               |
| charger         | chargingStatus          | String               | yes     | Charging status                                                                            |
|                 | powerState              | String               | yes     | Indicates availability of charging power                                                   |
|                 | chargingState           | String               | yes     | Current status of the charging process                                                     |
|                 | energyFlow              | String               | yes     | Energy is flowing / charging                                                               |
|                 | batteryState            | Number:Dimensionless | yes     | Battery level                                                                              |
|                 | remainingTime           | Number:Time          | yes     | Estimated remaining time to fully charge the battery                                       |
|                 | plugState               | String               | yes     | State of the charging plug, ON=connected                                                   |
|                 | lockState               | String               | yes     | ON:Plug is locked, OFF: Plug is unlocked and can be removed                                |
|                 | errorCode               | Number               | yes     | Error code when charging failed                                                            |
| range           | fuelPercentage          | Number:Dimensionless | yes     | Percentage of fuel remaining.                                                              |
|                 | fuelMethod              | String               | yes     | Method: 0=measured, 1=calculated                                                           |
|                 | totalRange              | Number:Length        | yes     | Total remaining range.                                                                     |
|                 | primaryRange            | Number:Length        | yes     | Range or the primary engine                                                                |
|                 | primaryFuelType         | Number               | yes     | Fuel type of the primary engine (3=Electrical, 5=Diesel, 6=Gas)                            |
|                 | secondaryRange          | Number:Length        | yes     | Range or the secondary engine                                                              |
|                 | secondaryFuelType       | Number               | yes     | Fuel type of the secondary engine (3=Electrical, 5=Diesel, 6=Gas)                          |
|                 | chargingLevel           | Number:Dimensionless | yes     | Current charging level in percent for an electrical car                                    |
|                 | gasPercentage           | Number:Dimensionless | yes     | Percentage of natural gas remaining                                                        |
| maintenance     | alarmInspection         | Switch               | yes     | ON: Inspection alarm is on.                                                                |
|                 | distanceToInspection    | Number:Length        | yes     | Distance before the next inspection / service is required.                                 |
|                 | timeToInspection        | Number:Time          | yes     | Time until next inspection.                                                                |
|                 | distanceAdBlue          | Number:Length        | yes     | Distance before the next Ad Blue fill-up is required.                                      |
|                 | oilWarningChange        | Switch               | yes     | True when Oil is low                                                                       |
|                 | oilWarningLevel         | Switch               | yes     | Minimum oil warning level                                                                  |
|                 | oilPercentage           | Number:Dimensionless | yes     | Remaining oil percentage (dip stick)                                                       |
|                 | distanceOilChange       | Number:Length        | yes     | Distance until the next oil change is required.                                            |
|                 | intervalOilChange       | Number:Time          | yes     | Distance until next oil change                                                             |
| doors           | doorFrontLeftState      | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | doorFrontLeftLocked     | Switch               | yes     | ON: The left front door is locked                                                          |
|                 | doorFrontLeftSafety     | Switch               | yes     | ON: Safety lock for left front door is open                                                |
|                 | doorFrontRightState     | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | doorFrontRightLocked    | Switch               | yes     | ON: The right front door locked                                                            |
|                 | doorFrontRightSafety    | Switch               | yes     | ON: Safety lock for right front door is open                                               |
|                 | doorRearLeftState       | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | doorRearLeftLocked      | Switch               | yes     | ON: The left rear door is locked                                                           |
|                 | doorRearLeftSafety      | Switch               | yes     | ON: Safety lock for left rear door is open                                                 |
|                 | doorRearRightState      | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | doorRearRightLocked     | Switch               | yes     | ON: The rear right door is locked.                                                         |
|                 | doorRearRightSafety     | Switch               | yes     | ON: Safety lock for right rear door is open                                                |
|                 | covertibleTopState      | Contact              | yes     | Status of the convertible top (OPEN/CLOSED)                                                |
|                 | covertibleTopPos        | Number:Dimensionless | yes     | The position of the convertible top (if any)                                               |
|                 | trunkLidState           | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | trunkLidLocked          | Switch               | yes     | ON: The trunk lid is locked.                                                               |
|                 | trunkLidSafety          | Switch               | yes     | ON: Trunk safety lock is open.                                                             |
|                 | hoodState               | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | hoodLocked              | Switch               | yes     | ON: The hood is locked                                                                     |
|                 | hoodSafety                Switch               | yes     | ON: Hood safety is activated                                                               |
| windows         | windowFrontLeftState    | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | windowFrontLeftPos      | Number:Dimensionless | yes     | Position of the left front window                                                          |
|                 | windowRearLeftState     | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | windowRearLeftPos       | Number:Dimensionless | yes     | The position of the left rear window                                                       |
|                 | windowFrontRightState   | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | windowFrontRightPos     | Number:Dimensionless | yes     | Position of the right front window                                                         |
|                 | windowRearRightState    | Contact              | yes     | State: OPEN or CLOSED                                                                      |
|                 | windowRearRightPos      | Number:Dimensionless | yes     | Position of the right rear window                                                          |
|                 | roofFrontCoverState     | Contact              | yes     | Front sun roof state: OPEN or CLOSED                                                       |
|                 | roofFrontCoverPos       | Number:Dimensionless | yes     | Position of the front sun roof cover                                                       |
|                 | roofRearCoverState      | Contact              | yes     | Rear sun roof state: OPEN or CLOSED                                                        |
|                 | roofRearCoverPos        | Number:Dimensionless | yes     | Position of the rear sun roof cover                                                        |
| tires           | tirePresFrontLeft       | Switch               | yes     | Pressure of the left front tire, ON=OK                                                     |
|                 | tirePresRearLeft        | Switch               | yes     | Pressure of the left rear tire, ON=OK                                                      |
|                 | tirePresFrontRight      | Switch               | yes     | Pressure of the right front tire, ON=OK                                                    |
|                 | tirePresRearRight       | Switch               | yes     | Pressure of the right rear tire, ON=OK                                                     |
| rluHistory1     | rluOperation            | String               | yes     | Action type: lock/unlock                                                                   |
|                 | rluTimestamp            | DateTime             | yes     | Timestamp when the action was initiated.                                                   |
|                 | rluResult               | String               | yes     | Action result: 1=ok                                                                        |
| tripShort1      | timestamp               | DateTime             | yes     | Trip time                                                                                  |
|                 | avgElectricConsumption  | Number:Energy        | yes     | Electrical Consumptio during the trip                                                      |
|                 | avgFuelConsumption      | Number:Volume        | yes     | Average fuel consumption for this trip                                                     |
|                 | avgSpeed                | Number:Speed         | yes     | Average Speed for this trip                                                                |
|                 | startMileage            | Number:Length        | yes     | Start Milage for the trip                                                                  |
|                 | mileage                 | Number:Length        | yes     | Distance for this trip.                                                                    |
|                 | overallMileage          | Number:Length        | yes     | Overall milage after this trip                                                             |
| tripLong1       | timestamp               | DateTime             | yes     | Trip time                                                                                  |
|                 | avgElectricConsumption  | Number:Energy        | yes     | Electrical Consumptio during the trip                                                      |
|                 | avgFuelConsumption      | Number:Volume        | yes     | Average fuel consumption for this trip                                                     |
|                 | avgSpeed                | Number:Speed         | yes     | Average Speed for this trip                                                                |
|                 | startMileage            | Number:Length        | yes     | Start Milage for the trip                                                                  |
|                 | mileage                 | Number:Length        | yes     | Distance for this trip.                                                                    |
|                 | overallMileage          |Number:Length         | yes     | Overall milage after this trip                                                             |
| geoFenceAlerts1 | geoFenceAlertType       | String               | yes     | Type of Speed Alert (ENTER_REDZONE, EXIT_GREENZONE)                                        |
|                 | geoFenceAlertTime       | DateTime             | yes     | When did the alert occured                                                                 |
|                 | geoFenceAlertDescr      | String               | yes     | Name of Alert Definition                                                                   |
| speedAlerts1    | speedAlertType          | String               | yes     | Type of Speed Alert (START_EXCEEDING)                                                      |
|                 | speedAlertTime          | DateTime             | yes     | When did the alert occured                                                                 |
|                 | speedAlertDescr         | String               | yes     | Name of Alert Definition                                                                   |
|                 | speedAlertLimit         | Number:Speed         | yes     | Exceeded Speed Limit                                                                       |


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
Bridge carnet:volkswagen:vw   "VW" [user="<username>", password="<password>" ] {
    Thing vehicle   WAUZZZXXXXXXXXXXX   "My Car"    [ vin="WAUZZZXXXXXXXXXXX", pin="<s-pin>", pollingInterval=15, enableAddressLookup=true ]
}
```

.items

```
Switch                      Locked               "Vehicle Locked"                      { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:general#vehicleLocked" }
Switch                      AllWindowsClosed     "All Windows Closed"                  { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:general#windowsClosed" }
Switch                      TirePressureOk       "Tire Pressure OK"                    { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:general#tiresOk" }
Switch                      ParkingBrake         "Parking Brake"                       { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:general#parkingBrake" }
Number:Length               Reichweite1          "Reichweite [%.1f %unit%]"            { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:range#totalRange" }
Number:Length               MonthlyMilage        "Monthly Milage [%.1f %unit%]"        { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:general#monthlyMilage" }
Number:Dimensionless        LadestandPer1        "Ladestand [%.1f %unit%]"             { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:range#chargingLevel" }
Number:Length               Km1                  "Kilometerstand [%.1f %unit%]"        { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:general#kilometerStatus" }
Location                    Position1            "Position"                            { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:location#position" }
Switch                      Update1              "Update"                              { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:control#update" }
Number:Temperature          OutsideTemp1         "Außentemperatur [%.1f %unit%]"       { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:general#tempOutside" }
DateTime                    Timestamp_S          "Timestamp"                           { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripShort1#timestamp" }
Number:Energy               AvgConsumption_S     "Avg Electrical Cons [%.1f %unit%]"   { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripShort1#avgElectricConsumption" }
Number:Speed                AvgSpeed_S           "Avg Speed [%.1f %unit%]"             { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripShort1#avgSpeed" }
Number:Length               TripMilage_S         "Trip Milage [%.1f %unit%]"           { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripShort1#mileage" }
DateTime                    Timestamp_L          "Timestamp"                           { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#timestamp" }
Number:Energy               AvgConsumption_L     "Avg Electrical Cons [%.1f %unit%]"   { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#avgElectricConsumption" }
Number:Speed                AvgSpeed_L           "Avg Speed [%.1f %unit%]"             { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#avgSpeed" }
Number:Length               TripMilage_L         "Trip Milage [%.1f %unit%]"           { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#mileage" }
Number:Length               StartMilage_L        "Start Milage [%.1f %unit%]"          { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#startMileage" }
Number:Length               OverallMilage_L      "Overall Milage [%.1f %unit%]"        { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:tripLong1#overallMileage" }
String                      ChargingStatus       "Charging Status"                     { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:charger#chargingStatus" }
Number                      ChargingError        "Charging Error [%.1f %unit%]"        { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:charger#errorCode" }
String                      PowerState           "Power State"                         { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:charger#powerState" }
String                      ChargingState        "Charging State"                      { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:charger#chargingState" }
String                      EnergyFlow           "Energy Flow"                         { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:charger#energyFlow" }
Number:Dimensionless        BatteryState         "Battery State [%.1f %unit%]"         { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:charger#batteryState" }
Number                      RemainingTime        "Remaining Time [%.1f %unit%]"        { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:charger#remainingTime" }
String                      PlugState            "Plug State"                          { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:charger#plugState" }
String                      PlugLockState        "Plug Lock State"                     { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:charger#lockState" }
Number:Temperature          TargetTemp           "Target Temperature [%.1f %unit%]"    { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:climater#targetTemperature" }
String                      HeaterSource         "Heater Source"                       { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:climater#heaterSource" }
String                      ClimatisationState   "Climatisation State"                 { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:climater#climatisationState" }
Switch                      ZoneFrontLeft        "Zone Front Left"                     { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:climater#frontLeft" }
Switch                      ZoneFrontRight       "Zone Front Right"                    { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:climater#frontRight" }
Switch                      ZoneRearLeft         "Zone Rear Left"                      { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:climater#rearLeft" }
Switch                      ZoneRearRight        "Zone Rear Right"                     { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:climater#rearRight" }
Switch                      MirrorHeating        "Mirror Heating"                      { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:climater#mirrorHeat" }
Switch                      LockVehicle          "Lock Vehicle"                        { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:control#lock" }
Switch                      ClimateControl       "Climate ON/OFF"                      { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:control#climater" }
Switch                      WindowHeater         "Window Heater ON/OFF"                { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:control#windowHeat" }
Switch                      ChargerSwitch        "Charging ON/OFF"                     { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:control#charger" }
Switch                      PreHeater            "Pre-Heater ON/OFF"                   { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:control#preHeater" }
Number                      Current_Speed        "Current Speed [%.1f %unit%]"         { channel="carnet:vehicle:vw:WAUZZZXXXXXXXXXXX:status#currentSpeed" }
```

## WeConnect ID.: VW ID.3/ID.4

Select this account type only if you have an ID. electrical car.
For other models select the Volkswagen (CarNet) account thing type (see above for further information).

### VW ID. Account Thing (vwid)

You need the credentials used for the myVolkswagen portal (the WeConnect. portal has been shut down).
If you don't already have one you need to create a Volkswagen ID and add the vehicle there.

|Parameter            | Description                                                                               |Mandatory| Default   |
|---------------------|-------------------------------------------------------------------------------------------|---------|-----------|
|user                 | User ID for your myVolkswagen account (same as login id for the myVolkswagen portal)         | yes     | none      |
|password             | Password for the myVolkswagen account (same as portal)                                          | yes     | none      |

### VW ID. Vehicle thing (idvehicle)

### Channels for the VW ID. Vehicles

| Group        | Channel                     | Item Type            |Read only| Description                    |
|--------------|-----------------------------|----------------------|---------|--------------------------------|
| general      | lastUpdate                  | DateTime             | yes     | Last time data has been updated                                                                 |
|              | timeInCar                   | DateTime             | yes     | Time in car                    |
|              | pictureUrl&lt;n&gt;         | String               | yes     | URL to picture(s) provided by manufacturer |
| range        | totalRange                  | Number:Length        | yes     | Total remaining range.                                                                          |
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

## Skoda Enyaq

Select this account type only if you have a Skoda Enyaq electrical car.
For other models select the Skoda (CarNet) account thing type (see above for further information).

### Enyaq Account Thing (enyak)

You need the credentials used for the Skoda Connect portal.

|Parameter            | Description                                                                               |Mandatory| Default   |
|---------------------|-------------------------------------------------------------------------------------------|---------|-----------|
|user                 | User ID for your CarNet account (same as login id for the manuafacturer's portal)         | yes     | none      |
|password             | Password for the CarNet account (same as portal)                                          | yes     | none      |

### Enyaq Vehicle thing (enyakvehicle)

### Channels for the Enyaq Vehicles

| Group        | Channel                     | Item Type            |Read only| Description                    |
|--------------|-----------------------------|----------------------|---------|--------------------------------|
| general      | lastUpdate                  | DateTime             | yes     | Last time data has been updated                                                                 |
| range        | totalRange                  | Number:Length        | yes     | Total remaining range.                                                                          |
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
