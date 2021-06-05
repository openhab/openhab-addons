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

### CarNet Account (account)

The Account thing implements the online connect to the CarNet service.
An account is required to setup the connection (register on the manufacture's portal, e.g. myAudi.de).
Usually the manufacturer supports to register a single or multiple vehicles under this account (identified by their unique vehicle identification number - VIN).
Once the account gets online the binding retrieves the complete vehicle list and creates a thing per vehicle.

Thing Configuration

|Parameter         | Description                                                                               |Mandatory| Default   |
|------------------|-------------------------------------------------------------------------------------------|---------|-------------------|
| brand            | Car brand, currently only Audi is supported                                               | yes     | none              |
| country          | Market / country where the vehicle is registered. There are 3 platforms: DE=All Europe incl. Canada, US=USA and CN=China | yes     |DE        |
| user             | User ID for your CarNet account (same as login id for the manuafacturer's portal)         | yes     | none      |
| password         | Password for the CarNet account (same as portal)                                          | yes     | none      |
| pollingInterval  | Refresh interval in minutes for data refresh (CarNet is not event driven)                 | yes     | 15        |

The account thing has no channels.

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
| general      | kilometerStatus         | Number:Length        | yes     | The kilometers from the odometer when status was captured.                            |
|              | parkingLight            | Switch               | yes     | ON: Parking light is turned on                                                        |
|              | tempOutside             | Number:Temperature   | yes     | The outside temperature in °C.                                                        |
|              | parkingBrake            | Switch               | yes     | State of the parking brake                                                            |
|              | monthlyMilage           | Number:Length        | yes     | Average milage per month.                                                             |
|              | lastAction              | String               | yes     | Last action send to the vehicle, check last action status to see if the request has been processed |
|              | lastActionStatus        | String               | yes     | Result from last action sent to the vehicle                                           |
|              | lastActionPending       | Switch               | yes     | ON: An action was send to the vehicle and is in status progressing                    |
| control      | lock                    | Switch               | no      | Lock/Unlock doors                                                                     |
|              | climater                | Switch               | no      | Turn climater on/off                                                                  |
|              | windowHeat              | Switch               | no      | Turn window heating on/off                                                            |
|              | preHeater               | Switch               | no      | Turn pre-heating on/off                                                               |
|              | ventilation             | Switch               | no      | Turn ventilation on/off                                                                  |
| location     | position                | Location             | yes     | Last known Car location                                                               |
|              | positionLastUpdate      | DateTime             | yes     | Time of last update for the vehicle position.                                         |
|              | parkingPosition         | Location             | yes     | Last position where the vehicle was parked.                                           |
|              | parkingTime             | DateTime             | yes     | Time when the vehicle was parked                                                      |
| maintenance  | distanceOilChange       | Number:Length        | yes     | Distance until the next oil change is required.                                       |
|              | intervalOilChange       | Number:Time          | yes     | Milage until next oil change                                                          |
|              | distanceToInspection    | Number:Length        | yes     | Distance before the next inspection / service is required.                            |
|              | timeToInspection        | Number:Time          | yes     | Time until next inspection.                                                           |
|              | oilWarningChange        | Switch               | yes     | True when Oil is low                                                                  |
|              | alarmInspection         | Switch               | yes     | ON: Inspection alarm is on.                                                           |
|              | oilWarningLevel         | Switch               | yes     | Minimum oil warning level                                                             |
|              | oilPercentage           | Number:Dimensionless | yes     | Dipstick oil percentage                                                               |
|              | distanceAdBlue          | Number:Length        | yes     | Distance before the next Ad Blue fill-up is required.                                 |
| range        | totalRange              | Number:Length        | yes     | Total remaining range.                                                                |
|              | primaryRange            | Number:Length        | yes     | Range or the primary battery engine system.                                           |
|              | primaryFuelType         | Number               | yes     | Fuel type of the primary engine system.                                               |
|              | secondaryRange          | Number:Length        | yes     | ??? Range or the secondary battery?                                                   |
|              | secondaryFuelType       | Number               | yes     | ???  Drive                                                                            |
|              | fuelPercentage          | Number:Dimensionless | yes     | Percentage of fuel remaining.                                                         |
|              | fuelMethod              | String               | yes     | Method: 0=measured, 1=calculated                                                      |
|              | gasPercentage           | Number:Dimensionless | yes     | Percentage of natural gas remaining                                                   |
| status       | currentSpeed            | Number:Speed         | yes     | Current speed when data was last updated                                              |
|              | roofMotorCoverState     | Switch               | yes     | ON: Closed                                                                            |
|              | roofRearMotorCoverState | Switch               | yes     | State of the rear sun root motor cover                                                |
|              | serviceFlapState        | Switch               | yes     | ON: Service flap is closed and locked, OFF: flap is open or closed, but not locked    |
|              | spoilerState            | Switch               | yes     | State of the spoiler.                                                                 |
| charger      | chargingStatus          | String               | yes     | Charging status                                                                       |
|              | powerState              | String               | yes     | Indicates availability of charging power                                              |
|              | chargingState           | String               | yes     | Current status of the charging process                                                |
|              | energyFlow              | String               | yes     | Energy is flowing / charging                                                          |
|              | batteryState            | Number:Dimensionless | yes     | Battery level                                                                         |
|              | remainingTime           | Number:Time          | yes     | Estimated remaining time to fully charge the battery                                  |
|              | plugState               | String               | yes     | State of the charging plug, ON=connected                                              |
|              | lockState               | String               | yes     | ON=plug is locked                                                                     |
|              | errorCode               | Number               | yes     | Error code when charging failed                                                       |
| climater     | targetTemperature       | Number:Temperature   | no      | Target temperature for the A/C climator                                               |
|              | heaterSource            | String               | yes     | Indicates the source for heating                                                      |
|              | climatisationState      | String               | yes     | ON: Climatisation is active                                                           |
|              | frontLeft               | Switch               | yes     | ON: Climatisation for the front left zone is active                                   |
|              | frontRight              | Switch               | yes     | ON: Climatisation for the front left zone is active                                   |
|              | rearLeft                | Switch               | yes     | ON: Climatisation for the front left zone is active                                   |
|              | rearRight               | Switch               | yes     | ON: Climatisation for the front left zone is active                                   |
|              | mirrorHeat              | Switch               | yes     | ON: Mirror heating is active                                                          |
| rluHistory1  | rluOperation            | String               | yes     | Action type: lock/unlock                                                              |
|              | rluTimestamp            | DateTime             | yes     | Timestamp when the action was initiated.                                              |
|              | rluResult               | String               | yes     | Action result: 1=ok                                                                   |
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
| destination1 | destinationName         | String               | yes     | The textual description of this destination, might be empty/NaN.                      |
|              | destinationPoi          | String               | yes     | Name of the Point-of-Interested (is this destination has one) or NaN                  |
|              | destinationLocation     | Location             | yes     | Geo coordinates of this location (Location item type format)                          |
|              | destinatinStreet        | String               | yes     | Street address of the destination address, might be empty (NaN).                      |
|              | destinationCity         | String               | yes     | City of the destination address, might be empty (NaN).                                |
|              | destinationZip          | String               | yes     | The zip code of the destination address, might be empty (NaN).                        |
|              | destinationCountry      | String               | yes     | Country of the destination address, might be empty (NaN).                             |
|              | destinationSource       | String               | yes     | Source of the destination, e.g. could be the mobile App, might be empty (NaN).        |
| doors        | doorFrontLeftLocked     | Switch               | yes     | ON: The left front door is locked.                                                    |
|              | doorFrontLeftState      | Switch               | yes     | ON: The left front door is open.                                                      |
|              | doorFrontLeftSafety     | Switch               | yes     | ON: Safety lock for left front door is open.                                          |
|              | doorRearLeftLocked      | Switch               | yes     | ON: The left rear door is locked.                                                     |
|              | doorRearLeftState       | Switch               | yes     | ON: The left rear door is locked.                                                     |
|              | doorRearLeftSafety      | Switch               | yes     | ON: Safety lock for left rear door is open.                                           |
|              | doorFrontRightLocked    | Switch               | yes     | ON: The right front door locked.                                                      |
|              | doorFrontRightState     | Switch               | yes     | ON: The right front door is open.                                                     |
|              | doorFrontRightSafety    | Switch               | yes     | ON: Safety lock for right front door is open.                                         |
|              | doorRearRightLocked     | Switch               | yes     | ON: The rear right door is locked.                                                    |
|              | doorRearRightState      | Switch               | yes     | ON: The rear right door is open.                                                      |
|              | doorRearRightSafety     | Switch               | yes     | ON: Safety lock for right rear door is open.                                          |
|              | trunkLidLocked          | Switch               | yes     | ON: The trunk lid is locked.                                                          |
|              | trunkLidState           | Switch               | yes     | State of the trunk lid.                                                               |
|              | trunkLidSafety          | Switch               | yes     | ON: Trunk safety lock is open.                                                        |
|              | hoodLocked              | Switch               | yes     | ON: The hood is locked.                                                               |
|              | hoodState               | Switch               | yes     | ON: The hood is closed.                                                               |
|              | hoodSafety              | Switch               | yes     | ON: Hood safety is activated.                                                         |
|              | covertableTopState      | Number               | yes     | Status of the convertible top.                                                        |
| windows      | windowFrontLeftState    | Switch               | yes     | Status of the left front window, ON=closed                                            |
|              | windowFrontLeftPos      | Number:Dimensionless | yes     | Position of the left front window.                                                    |
|              | windowRearLeftState     | Switch               | yes     | The status of the left rear window, ON=closed                                         |
|              | windowRearLeftPos       | Number:Dimensionless | yes     | The position of the left rear window.                                                 |
|              | windowFrontRightState   | Switch               | yes     | Status of the left rear window, ON=closed                                             |
|              | windowFrontRightPos     | Number:Dimensionless | yes     | Position of the right front window.                                                   |
|              | windowRearRightState    | Switch               | yes     | Status of the right rear window, ON=closed                                            |
|              | windowRearRightPos      | Number:Dimensionless | yes     | Position of the right rear window.                                                    |
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
