# CarNet Binding

This binding integrates the CarNet service provided by Audi, Volkswagen and other brands.
It provides access to status information and control functions for compatible vehicles, which have activated the service.
The API provides a lot of diagnostic data and provide access to remote control functions like lock/unlock door.

## Discovery

The binding supports automated discovery of vehicles.
For now only Audi is supported, other brand might follow in the future, because the API has a big overlap between various brands.
The CarNet Account Thing has to be added manually giving the necessary credentials.
Once the account is online the binding can query all registered vehicles and creates a vehicle thing for each of them.

## Binding Configuration

The binding itself has no configuration options

## Supported Things

### CarNet Account (myaudi, volkswagen, skoda)

The Account thing implements the online connect to the CarNet service.
The binding supports myAudi (myaudi) and Skoda Connect (skoda).
Depending on brand, car model and activated license a different set of channels and actions are supported,

Some verified vehicles:

|Brand     |Model       |Year|Type      |Market|Notes                                                        |Doorlock|Clima|Preheat|
|----------|------------|----|----------|------|-------------------------------------------------------------|--------|-----|-------|
|Audi      |A6          |2020|Diesel    |DE/DE |TripData service doesn't work even with valid license        |yes     |no   |n/a    |
|Audi      |Q3 Sportback|2020|Gas       |DE/DE |                                                             |        |     |       |
|Skoda     |Superb      |2020|Hybrid    |DE/CH |                                                             |        |     |       |
|Volkswagen|Arteon      |2021|          |DE/   |                                                             |        |     |       |
|Volkswagen|eGolf       |2020|Electrical|DE/DE |                                                             |no      |yes  |n/a    |
|Volkswagen|eUp         |    |Electrical|DE/DE |                                                             |        |     |       |
|Volkswagen|Passat GTE  |2016|Hybrid    |DE/NL |                                                             |n/a     |n/a  |n/a    |
|Volkswagen|Tiguan      |2021|Diesel    |DE/DE |                                                             |n/a     |n/a  |n/a    |
|Volkswagen|T-Roc Cabrio|2020|          |DE/   |                                                             |        |     |       |


`yes=feature available and verified, no=feature available, but not supported/doesn't work; n/a=feature not available; blank=unknown/untested`


An account is required to setup the connection (register on the manufacture's portal, e.g. myAudi.de).
Usually the manufacturer supports to register a single or multiple vehicles under this account (identified by their unique vehicle identification number - VIN).
Once the account gets online the binding retrieves the complete vehicle list and creates a thing per vehicle.

Thing Configuration

|Parameter         | Description                                                                               |Mandatory| Default   |
|------------------|-------------------------------------------------------------------------------------------|---------|-----------|
| user             | User ID for your CarNet account (same as login id for the manuafacturer's portal)         | yes     | none      |
| password         | Password for the CarNet account (same as portal)                                          | yes     | none      |
| pollingInterval  | Refresh interval in minutes for data refresh (CarNet is not event driven)                 | yes     | 30        |

The account thing has no channels.

### Status Codes / Errors

|Code         |Message                                                                          |Description                                       |
|-------------|---------------------------------------------------------------------------------|--------------------------------------------------|
|VSR.9007     |Service disabled, legitimation is pending. Check data privacy settings in the MMI|Check Data Privacy settings in MMI, give consent  |
|VSR.9025     |TSS responded: 429                                                               |AOI has been throttled, increase polling interval |
|VSR.9026     |Technical validation error                                                       |Some technical problem, check log, enable DEBUG   |
|business.1003]Request is already pending                                                       |There is already a request peneindg, await result |

### Vehicle (vehicle)

The thing type vehicle represents a registered vehicle.
The vehicle is associated with an account and an online connection is required to access the service.
The vehicle is identified by the unique Vehicle Identification Number (VIN).

The API provides a large number of vehicle data items.
The binding wraps those to channels in different groups.

Important: You have to enabled the Car Connect services and also give your consent in the MMI system. Use the code on your key holder to setup Car Connect services.

Please note:
Available channels depend on the specific vehicle type and equipment.
Most of them will be created dynamically depending on availability of the CarNet services.
On startup the binding detects the available API services and deactives them if the API responds with a 403 status (don't worry if you see those messages in the log).

Even if the car has a climater it might be that the service is not accessible and the channels will not be created if the car doesn't have a pre-heating system. This also applies to the trip data and destination history. The dependency on a pre-heating system is not obvious, but it has been verified by several users.

Thing initialization might take up to 1 minute depending on the amount of available data and API performance.
Sometimes the API has higher response times (or even times out).
The binding does a frequent recovery check (on each poll cycle) and re-connects to the API service if neccesary.


Thing Configuration

| Parameter        | Description                                                                 | Mandatory |Default      |
|------------------|-----------------------------------------------------------------------------|-----------|-------------|
| PIN              | Security PIN (SPIN) required for priveledged functions like lock/unlock     | no        | 1           |
| numShortTrip     | Number of entries in the Short Trip History (one channel group each)        | no        | 1           |
| numLongTrip      | Number of entries in the Long Trip History (one channel group each)         | no        | 1           |
| numActionHistory | Number of entries in the Action History (one channel group each)            | no        | 1           |
| numShortTrip     | Number of entries in the Short Trip History (one channel group each)        | no        | 1           |
| numDestinations  | Number of entries in the Destination History (one channel group each)       | no        | 1           |

For all num-options:
Values can be 1..5 or 0 to disable this history.
This creates a channel group per entry, e.g. destination1, destination2, destination3, rluHistory1, rluHistory2 etc.
The newest entry will be in xxx1 channel group.

The following channels are available depending on the vehicle type:

|Group         |Channel                  |Type                  |read-only| Description                                                                           |
|--------------|-------------------------|----------------------|---------|---------------------------------------------------------------------------------------|
| general      | lastUpdate              | DateTime             | yes     | Last time data has been updated.                                                      |
|              | lastAction              | String               | yes     | Last action sent to the vehicle (check lastActionStatus for status/result)            |
|              | lastActionStatus        | String               | yes     | Result from last action sent to the vehicle                                           |
|              | lastActionPending       | Switch               | yes     | ON: An action was send to the vehicle and is in status progressing                    |
| control      | update                  | Switch               | no      | Force status update of vehicle status                                                 |
|              | lock                    | Switch               | no      | Lock/Unlock doors                                                                     |
|              | charge                  | Switch               | no      | Turn charger on/off                                                                   |
|              | climater                | Switch               | no      | Turn climater on/off                                                                  |
|              | windowHeat              | Switch               | no      | Turn window heating on/off                                                            |
|              | preHeater               | Switch               | no      | Turn pre-heating on/off                                                               |
|              | ventilation             | Switch               | no      | Turn ventilation on/off                                                               |
| status       | kilometerStatus         | Number:Length        | yes     | The kilometers from the odometer when status was captured.                            |
|              | tempOutside             | Number:Temperature   | yes     | The outside temperature in °C.                                                        |
|              | vehicleLocked           | Switch               | yes     | ON: Vehicle is completely locked. This includes doors, windows, but also hood etc.    |
|              | windowsClosed           | Switch               | yes     | ON: All Windows are closed.                                                           |
|              | tiresOk                 | Switch               | yes     | ON: Pressure for all tires is ok, otherwhise check single tires.                      |
|              | currentSpeed            | Number:Speed         | yes     | Current speed when data was last updated                                              |
|              | monthlyMilage           | Number:Length        | yes     | Average milage per month.                                                             |
|              | parkingBrake            | Switch               | yes     | State of the parking brake                                                            |
|              | parkingLight            | Switch               | yes     | ON: Parking light is turned on                                                        |
|              | maintenanceRequired     | Switch               | yes     | ON: Some type of maintenance is requied, check status group for details               |
|              | spoilerState            | Contact              | yes     | State of the spoiler.                                                                 |
|              | serviceFlapState        | Contact              | yes     | ON: Service flap is closed and locked, OFF: flap is open or closed, but not locked    |
| location     | position                | Location             | yes     | Last known vehicle location                                                           |
|              | positionLastUpdate      | DateTime             | yes     | Time of last update for the vehicle position.                                         |
|              | parkingPosition         | Location             | yes     | Last position where the vehicle was parked.                                           |
|              | parkingTime             | DateTime             | yes     | Time when the vehicle was parked                                                      |
| range        | fuelPercentage          | Number:Dimensionless | yes     | Percentage of fuel remaining.                                                         |
|              | fuelMethod              | String               | yes     | Method: 0=measured, 1=calculated                                                      |
|              | totalRange              | Number:Length        | yes     | Total remaining range.                                                                |
|              | primaryRange            | Number:Length        | yes     | Range or the primary engine                                                           |
|              | primaryFuelType         | Number               | yes     | Fuel type of the primary engine (3=Electrical, 5=Gas, 6=Diesel)                       |
|              | secondaryRange          | Number:Length        | yes     | Range or the secondary engine                                                         |
|              | secondaryFuelType       | Number               | yes     | Fuel type of the secondary engine (3=Electrical, 5=Gas, 6=Diesel)                     |
|              | chargingLevel           | Number:Dimensionless | yes     | Current charging level in percent for an electrical car                               |
|              | gasPercentage           | Number:Dimensionless | yes     | Percentage of natural gas remaining                                                   |
| charge       | chargingStatus          | String               | yes     | Charging status                                                                       |
|              | powerState              | String               | yes     | Indicates availability of charging power                                              |
|              | chargingState           | String               | yes     | Current status of the charging process                                                |
|              | energyFlow              | String               | yes     | Energy is flowing / charging                                                          |
|              | batteryState            | Number:Dimensionless | yes     | Battery level                                                                         |
|              | remainingTime           | Number:Time          | yes     | Estimated remaining time to fully charge the battery                                  |
|              | plugState               | String               | yes     | State of the charging plug, ON=connected                                              |
|              | lockState               | String               | yes     | ON:Plug is locked, OFF: Plug is unlocked and can be removed                           |
|              | errorCode               | Number               | yes     | Error code when charging failed                                                       |
| climater     | targetTemperature       | Number:Temperature   | no      | Target temperature for the A/C climator                                               |
|              | heaterSource            | String               | yes     | Indicates the source for heating                                                      |
|              | climatisationState      | String               | yes     | ON: Climatisation is active                                                           |
|              | frontLeft               | Switch               | yes     | ON: Climatisation for the front left zone is active                                   |
|              | frontRight              | Switch               | yes     | ON: Climatisation for the front left zone is active                                   |
|              | rearLeft                | Switch               | yes     | ON: Climatisation for the front left zone is active                                   |
|              | rearRight               | Switch               | yes     | ON: Climatisation for the front left zone is active                                   |
|              | mirrorHeat              | Switch               | yes     | ON: Mirror heating is active                                                          |
| maintenance  | alarmInspection         | Switch               | yes     | ON: Inspection alarm is on.                                                           |
|              | distanceToInspection    | Number:Length        | yes     | Distance before the next inspection / service is required.                            |
|              | timeToInspection        | Number:Time          | yes     | Time until next inspection.                                                           |
|              | distanceAdBlue          | Number:Length        | yes     | Distance before the next Ad Blue fill-up is required.                                 |
|              | oilWarningChange        | Switch               | yes     | True when Oil is low                                                                  |
|              | oilWarningLevel         | Switch               | yes     | Minimum oil warning level                                                             |
|              | oilPercentage           | Number:Dimensionless | yes     | Remaining oil percentage (dip stick)                                                  |
|              | distanceOilChange       | Number:Length        | yes     | Distance until the next oil change is required.                                       |
|              | intervalOilChange       | Number:Time          | yes     | Distance until next oil change                                                        |
| tripShort1   | timestamp               | DateTime             | yes     | Trip time                                                                             |
|              | avgElectricConsumption  | Number:Energy        | yes     | Electrical Consumptio during the trip                                                 |
|              | avgFuelConsumption      | Number:Volume        | yes     | Average fuel consumption for this trip                                                |
|              | avgSpeed                | Number:Speed         | yes     | Average Speed for this trip                                                           |
|              | startMileage            | Number:Length        | yes     | Start Milage for the trip                                                             |
|              | mileage                 | Number:Length        | yes     | Distance for this trip.                                                               |
|              | overallMileage          | Number:Length        | yes     | Overall milage after this trip                                                        |
| tripLong1    | timestamp               | DateTime             | yes     | Trip time                                                                             |
|              | avgElectricConsumption  | Number:Energy        | yes     | Electrical Consumptio during the trip                                                 |
|              | avgFuelConsumption      | Number:Volume        | yes     | Average fuel consumption for this trip                                                |
|              | avgSpeed                | Number:Speed         | yes     | Average Speed for this trip                                                           |
|              | startMileage            | Number:Length        | yes     | Start Milage for the trip                                                             |
|              | mileage                 | Number:Length        | yes     | Distance for this trip.                                                               |
|              | overallMileage          | Number:Length        | yes     | Overall milage after this trip                                                        |
| rluHistory1  | rluOperation            | String               | yes     | Action type: lock/unlock                                                              |
|              | rluTimestamp            | DateTime             | yes     | Timestamp when the action was initiated.                                              |
|              | rluResult               | String               | yes     | Action result: 1=ok                                                                   |
| doors        | doorFrontLeftState      | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | doorFrontLeftLocked     | Switch               | yes     | ON: The left front door is locked.                                                    |
|              | doorFrontLeftSafety     | Switch               | yes     | ON: Safety lock for left front door is open.                                          |
|              | doorFrontRightState     | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | doorFrontRightLocked    | Switch               | yes     | ON: The right front door locked.                                                      |
|              | doorFrontRightSafety    | Switch               | yes     | ON: Safety lock for right front door is open.                                         |
|              | doorRearLeftState       | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | doorRearLeftLocked      | Switch               | yes     | ON: The left rear door is locked.                                                     |
|              | doorRearLeftSafety      | Switch               | yes     | ON: Safety lock for left rear door is open.                                           |
|              | doorRearRightState      | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | doorRearRightLocked     | Switch               | yes     | ON: The rear right door is locked.                                                    |
|              | doorRearRightSafety     | Switch               | yes     | ON: Safety lock for right rear door is open.                                          |
| doors        | trunkLidState           | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | trunkLidLocked          | Switch               | yes     | ON: The trunk lid is locked.                                                          |
|              | trunkLidSafety          | Switch               | yes     | ON: Trunk safety lock is open.                                                        |
|              | hoodState               | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | hoodLocked              | Switch               | yes     | ON: The hood is locked.                                                               |
|              | hoodSafety              | Switch               | yes     | ON: Hood safety is activated.                                                         |
|              | convertibleTopState     | Contact              | yes     | Status of the convertible top (OPEN/CLOSED)                                           |
|              | covertableTopPos        | Number:Dimensionless | yes     | The position of the convertible top (if any)                                          |
| windows      | windowFrontLeftState    | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | windowFrontLeftPos      | Number:Dimensionless | yes     | Position of the left front window.                                                    |
|              | windowRearLeftState     | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | windowRearLeftPos       | Number:Dimensionless | yes     | The position of the left rear window.                                                 |
|              | windowFrontRightState   | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | windowFrontRightPos     | Number:Dimensionless | yes     | Position of the right rear window.                                                    |
|              | windowRearRightState    | Contact              | yes     | State: OPEN or CLOSED                                                                 |
|              | windowRearRightPos      | Number:Dimensionless | yes     | Position of the right rear window.                                                    |
|              | roofFrontCoverState     | Contact              | yes     | Front sun roof state: OPEN or CLOSED                                                  |
|              | roofFrontCoverPos       | Number               | yes     | Position of the front sun roof cover                                                  |
|              | roofRearCoverState      | Contact              | yes     | Rear sun roof state: OPEN or CLOSED                                                   |
|              | roofRearCoverPos        | Number               | yes     | Position of the rear sun roof cover                                                   |
| tires        | tirePresFrontLeft       | Switch               | yes     | Pressure of the left front tire, ON=OK                                                |
|              | tirePresRearLeft        | Switch               | yes     | Pressure of the left rear tire, ON=OK                                                 |
|              | tirePresFrontRight      | Switch               | yes     | Pressure of the right front tire, ON=OK                                               |
|              | tirePresRearRight       | Switch               | yes     | Pressure of the right rear tire, ON=OK                                                |


## Full Example

.things

.items

```
// Audi e-tron
Switch                      Locked               "Vehicle Locked"                      { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:general#vehicleLocked" }
Switch                      AllWindowsClosed     "All Windows Closed"                  { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:general#windowsClosed" }
Switch                      TirePressureOk       "Tire Pressure OK"                    { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:general#tiresOk" }
Switch                      ParkingBrake         "Parking Brake"                       { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:general#parkingBrake" }
Number:Length               Reichweite1          "Reichweite [%.1f %unit%]"            { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:range#totalRange" }
Number:Length               MonthlyMilage        "Monthly Milage [%.1f %unit%]"        { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:general#monthlyMilage" }
Number:Dimensionless        LadestandPer1        "Ladestand [%.1f %unit%]"             { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:range#chargingLevel" }
Number:Length               Km1                  "Kilometerstand [%.1f %unit%]"        { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:general#kilometerStatus" }
Location                    Position1            "Position"                            { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:location#position" }
Switch                      Update1              "Update"                              { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:control#update" }
Number:Temperature          OutsideTemp1         "Außentemperatur [%.1f %unit%]"       { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:general#tempOutside" }
DateTime                    Timestamp_S          "Timestamp"                           { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripShort1#timestamp" }
Number:Energy               AvgConsumption_S     "Avg Electrical Cons [%.1f %unit%]"   { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripShort1#avgElectricConsumption" }
Number:Speed                AvgSpeed_S           "Avg Speed [%.1f %unit%]"             { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripShort1#avgSpeed" }
Number:Length               TripMilage_S         "Trip Milage [%.1f %unit%]"           { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripShort1#mileage" }
DateTime                    Timestamp_L          "Timestamp"                           { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripLong1#timestamp" }
Number:Energy               AvgConsumption_L     "Avg Electrical Cons [%.1f %unit%]"   { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripLong1#avgElectricConsumption" }
Number:Speed                AvgSpeed_L           "Avg Speed [%.1f %unit%]"             { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripLong1#avgSpeed" }
Number:Length               TripMilage_L         "Trip Milage [%.1f %unit%]"           { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripLong1#mileage" }
Number:Length               StartMilage_L        "Start Milage [%.1f %unit%]"          { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripLong1#startMileage" }
Number:Length               OverallMilage_L      "Overall Milage [%.1f %unit%]"        { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:tripLong1#overallMileage" }
String                      ChargingStatus       "Charging Status"                     { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:charger#chargingStatus" }
Number                      ChargingError        "Charging Error [%.1f %unit%]"        { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:charger#errorCode" }
String                      PowerState           "Power State"                         { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:charger#powerState" }
String                      ChargingState        "Charging State"                      { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:charger#chargingState" }
String                      EnergyFlow           "Energy Flow"                         { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:charger#energyFlow" }
Number:Dimensionless        BatteryState         "Battery State [%.1f %unit%]"         { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:charger#batteryState" }
Number                      RemainingTime        "Remaining Time [%.1f %unit%]"        { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:charger#remainingTime" }
String                      PlugState            "Plug State"                          { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:charger#plugState" }
String                      PlugLockState        "Plug Lock State"                     { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:charger#lockState" }
Number:Temperature          TargetTemp           "Target Temperature [%.1f %unit%]"    { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:climater#targetTemperature" }
String                      HeaterSource         "Heater Source"                       { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:climater#heaterSource" }
String                      ClimatisationState   "Climatisation State"                 { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:climater#climatisationState" }
Switch                      ZoneFrontLeft        "Zone Front Left"                     { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:climater#frontLeft" }
Switch                      ZoneFrontRight       "Zone Front Right"                    { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:climater#frontRight" }
Switch                      ZoneRearLeft         "Zone Rear Left"                      { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:climater#rearLeft" }
Switch                      ZoneRearRight        "Zone Rear Right"                     { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:climater#rearRight" }
Switch                      MirrorHeating        "Mirror Heating"                      { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:climater#mirrorHeat" }
Switch                      LockVehicle          "Lock Vehicle"                        { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:control#lock" }
Switch                      ClimateControl       "Climate ON/OFF"                      { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:control#climater" }
Switch                      WindowHeater         "Window Heater ON/OFF"                { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:control#windowHeat" }
Switch                      ChargerSwitch        "Charging ON/OFF"                     { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:control#charger" }
Switch                      PreHeater            "Pre-Heater ON/OFF"                   { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:control#preHeater" }
Number                      Current_Speed        "Current Speed [%.1f %unit%]"         { channel="carnet:vehicle:f1dadf50:WAUZZZXXXXXXXXXXX:status#currentSpeed" }
```

.sitemap

.rule
