# VW We Connect Binding

This is an openHAB binding for VW We Connect Portal.

This binding uses the rest API behind the VW We Connect Portal: 
https://www.portal.volkswagen-we.com


## Supported Things

This binding supports the following thing types:

- Bridge - VW We Connect Portal API 
- Vehicle - Any VW vehicle reachable via the VW We Connect Portal.


## Binding Configuration

You will have to configure the bridge with username and password, these must be the same credentials as used when logging into: 
https://www.portal.volkswagen-we.com. 

You must also configure your secure pin to be able to lock/unlock and start/stop the heater/ventilation. 

## Discovery

After the configuration of the VW Car Net Bridge all of the available vehicles will be discovered and placed as things in the inbox.

## Thing Configuration

Only the bridge require manual configuration. Vehicles can be added by hand, or you can let the discovery mechanism automatically find all of your vehicles.

## Enable Debugging

To enable DEBUG logging for the binding, login to Karaf console and enter:

`openhab> log:set DEBUG org.openhab.binding.vwweconnect`

## Supported Things and Channels 

### VW We Connect Bridge 

#### Configuration Options

*   `username` - The username used to connect to VW We Connect Portal.

*   `password` - The password used to connect to VW We Connect Portal.

*   `spin` - The user's secure PIN code to lock/unlock doors and start/stop the heater, same as configured on VW We Connect Portal.

*   `refresh` - Specifies the refresh interval in seconds (default 600).


#### Channels

vwweconnectapi supports the following channel:

| Channel Type ID | Item Type | Description                                                                                     |
|-----------------|-----------|-------------------------------------------------------------------------------------------------|
| status          | String    | This channel can be used to trigger an instant refresh by sending a RefreshType.REFRESH command.|


### VW Vehicle

#### Configuration Options

*   `VIN` - Vehicle Identification Number
    

#### Channels Groups and channels

vehicle supports the following channel groups and channels:

| Channel Group ID#Channel Type ID     | Item Type            | Description                             | 
|--------------------------------------|------------------------|-----------------------------------------|
| details#name                         | String                 | Vehicle name                            | 
| details#model                        | String                 | Vehicle model                           |
| details#modelCode                    | String                 | Vehicle model code                      |
| details#modelYear                    | String                 | Vehicle model year                      |
| details#enrollmentDate               | DateTime               | Vehicle enrollment date                 |
| details#dashboardURL                 | String                 | User's home URL                         |
| details#imageURL                     | Image                  | Vehicle picture                         |
| details#engineTypeCombustian         | Switch                 | Is engine type combustian               |
| details#engineTypeElectric           | Switch                 | Is engine type electric                 |
| details#engineTypeHybridOCU1         | Switch                 | Is engine type hybrid OCU1              |
| details#engineTypeHybridOCU2         | Switch                 | Is engine type hybrid OCU2              |
| details#engineTypeCNG                | Switch                 | Is engine type compressed natural gas   |
| details#serviceInspectionStatus      | String                 | Service Inspection Status               |         
| details#oilInspectionStatus          | String                 | Oil Inspection Status                   | 
| odometer#totalDistance               | Number:Length          | Total distance                          | 
| odometer#totalAverageSpeed           | Number                 | Total average speed                     | 
| odometer#totalTripDistance           | Number:Length          | Total trip distance                     | 
| odometer#totalTripDuration           | Number:Time            | Total trip duration                     | 
| fuel#fuelLevel                       | Number:Dimensionless   | Fuel level                              |
| fuel#fuelConsumption                 | Number                 | Average fuel consumption                |
| fuel#fuelAlert                       | Switch                 | Fuel alert (< 10%)                      | 
| fuel#fuelRange                       | Number:Length          | Fuel range                              | 
| cng#cngLevel                         | Number:Dimensionless   | CNG level                               |
| cng#cngConsumption                   | Number                 | Average CNG consumption                 |
| cng#cngAlert                         | Switch                 | CNG alert (< 10%)                       | 
| cng#cngRange                         | Number:Length          | CNG range                               |
| electric#batteryLevel                | Number:Dimensionless   | Battery level                           |
| electric#electricConsumption         | Number                 | Electric consumption                    |
| electric#batteryAlert                | Switch                 | Battery alert (< 10%)                   | 
| electric#batteryRange                | Number:Length          | Battery range                           |  
| electric#chargingState               | Switch                 | Charging state                          |
| electric#chargingRemainingHour       | Number:Time            | Remaining charging time in hours        |
| electric#chargingRemainingMinute     | Number:Time            | Remaining charging time in minutes      |
| electric#chargingReason              | String                 | Charging reason                         |
| electric#pluginState                 | Switch                 | Plugin state                            |
| electric#lockState                   | Switch                 | Lock state                              |
| electric#extPowerSupplyState         | Switch                 | External power supply state             |
| electric#chargerMaxCurrent           | Number:ElectricCurrent | Max configured charger current in A     |
| electric#maxAmpere                   | Number:ElectricCurrent | Max configured charger current in A     |
| electric#maxCurrentReduced           | Switch                 | Is max current reduced                  |
| electric#climatisationState          | Switch                 | Climatisation state                     |
| electric#climatisationRemainingTime  | Number:Time            | Remaining climatisation time in minutes |
| electric#climatisationReason         | String                 | Climatisation reason                    |
| electric#windowHeatingStateFront     | Switch                 | Window heating state front              |
| electric#windowHeatingStateRear      | Switch                 | Window heating state rear               |
| doors#trunk                          | Contact                | Trunk status                            |
| doors#rightBack                      | Contact                | Right back door status                  |
| doors#leftBack                       | Contact                | Left back door status                   |
| doors#rightFront                     | Contact                | Right front door status                 |
| doors#leftFront                      | Contact                | Left front door status                  |
| doors#hood                           | Contact                | Hood status                             |
| doors#roof                           | Contact                | Roof status                             |
| doors#sunroof                        | Contact                | Sun Roof status                         |
| doors#doorslocked                    | Switch                 | Action lock/unlock door                 |
| doors#trunklocked                    | Switch                 | Not supported by API                    |
| windows#rightBackWnd                 | Contact                | Right back window status                |
| windows#leftBackWnd                  | Contact                | Left back window status                 |
| windows#rightFrontWnd                | Contact                | Right front window status               |
| windows#leftFrontWnd                 | Contact                | Left front window status                |
| position#location                    | Location               | Vehicle position                        |
| lasttrip#averageFuelConsumption      | Number                 | Last trip average fuel consumption      |
| lasttrip#averageCngConsumption       | Number                 | Last trip average CNG consumption       |
| lasttrip#averageElectricConsumption  | Number                 | Last trip average electric consumption  |
| lasttrip#averageAuxiliaryConsumption | Number                 | Last trip average auxiliary consumption |
| lasttrip#tripAverageSpeed            | Number                 | Last trip average speed                 |
| lasttrip#tripDistance                | Number:Length          | Last trip distance                      |
| lasttrip#tripStartTime               | DateTime               | Last trip start time                    |
| lasttrip#tripEndTime                 | DateTime               | Last trip end time                      |
| lasttrip#tripDuration                | Number:Time            | Last trip duration                      |
| action#remoteHeater                  | Switch                 | Action remote heater                    |
| action#remoteVentilation             | Switch                 | Action remote ventilation               |
| action#temperature                   | Number                 | Vehicle outdoor temperature             |
| action#remainingTime                 | Number:Time            | Remaining heater time                   |
| action#emanagerCharge                | Switch                 | Action emanager charge                  | 
| action#emanagerClimate               | Switch                 | Action emanager climatisation           |  
| action#emanagerWindowHeat            | Switch                 | Action emanager window heater           |              

## Rule Actions

Multiple actions are supported by this binding. In classic rules these are accessible as shown in the example below:

```
Example 1a: If Thing has been created using auto-discovery

 val actions = getActions("vwweconnect","vwweconnect:vehicle:thingId")
 if (null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
 } else {
        actions.unlockCommand()
 }
Example 1b: If Thing has been created using configuration things-file

 val actions = getActions("vwweconnect","vwweconnect:vehicle:bridgeId:thingId")
 if (null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
 } else {
        actions.unlockCommand()
 }
 ```
 
### Supported actions
 
#### unlockCommand()

Sends the command to unlock the vehicle.

#### lockCommand()

Sends the command to lock the vehicle.

#### heaterStartCommand()

Sends the command to start the vehicle heater.

#### heaterStopCommand()

Sends the command to stop the vehicle heater.

#### ventilationStartCommand() NOTE: Does not seem to work

Sends the command to start the vehicle ventilation.

#### ventilationStopCommand() NOTE: Does not seem to work

Sends the command to stop the vehicle ventilation.

### Electrical/hybrid cars might support the following actions NOTE: Not tested at all!!!

#### chargerStartCommand()

Sends the command to start the vehicle battery charging.

#### chargerStopCommand()

Sends the command to stop the vehicle battery charging.

#### climateStartCommand()

Sends the command to start the vehicle climatisation.

#### climateStopCommand()

Sends the command to stop the vehicle climatisation.

#### windowHeatStartCommand()

Sends the command to start the vehicle window heating.

#### windowHeatStopCommand()

Sends the command to stop the vehicle window heating.
 

## Example

### Things-file

````
// Bridge configuration
Bridge vwweconnect:vwweconnectapi:myvwweconnect "VW We Connect" [username="x@y.com", password="1234", refresh="600", spin="1111"] {
     Thing vehicle         JannesFolka         "VW Vehicle"                  [ vin="WVGZZZ5XAPQ834262" ]
}
````

### Items-file

````
Group gTotalTripData
Group gLastTripData
Group gFuelInfo
Group gDoorStatus
Group gWindowStatus

// My bridge
String   VWWeConnectRefreshStatus  "VW We Connect refresh status" {channel="vwweconnect:vwweconnectapi:myvwweconnect:status"}
Switch   RefreshVehicleStatus      "Vehicle Refresh Status"

// My vehicles
String   VehicleName                "Vehicle name [%s]"  <car>   {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:details#name"}
String   VehicleModel               "Vehicle model [%s]"  <car>  {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:details#model"}
String   VehicleCode                "Vehicle model code [%s]"  <car>  {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:details#modelCode"}
String   VehicleYear                "Vehicle model year [%s]" <car> {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:details#modelYear"}
DateTime VehicleRolloutDate         "Vehicle roll-out date [%1$tY-%1$tm-%1$td]" <car> {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:details#enrollmentDate"}
Image    VehicleImage               "Vehicle image" <car> {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:details#imageURL"} 
String   VehicleServiceInspection   "Vehicle service inspection [%s]" <car> {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:details#serviceInspectionStatus"}
String   VehicleOilInspection       "Vehicle oil inspection [%s]" <car> {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:details#oilInspectionStatus"}

// Total trip data
Number   VehicleTotalAvgSpeed       "Total average speed" <odometer> (gTotalTripData) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:odometer#totalAverageSpeed"}
Number   VehicleTotalDistance       "Total distance" <odometer> (gTotalTripData) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:odometer#totalTripDistance"}
Number   VehicleTotalDuration       "Total duration" <odometer> (gTotalTripData) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:odometer#totalTripDuration"}

// Fuel Info
Number   VehicleFuelLevel           "Fuel level (%)" <sewerage> (gFuelInfo) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:fuel#fuelLevel"}
Number   VehicleAvgFuelConsumption  "Average fuel consumption (%)" <sewerage> (gFuelInfo) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:fuel#fuelConsumption"}
Switch   VehicleFuelAlert           "Fuel alert" <sewerage>  {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:fuel#fuelAlert"}
Number   VehicleFuelRange           "Fuel Range" <motion> (gFuelInfo) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:fuel#fuelRange"}

// Door status
Contact  VehicleTrunk               "Trunk [%s]" <door> (gDoorStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:doors#trunk"}
Contact  VehicleDoorRightBack       "Door right back [%s]" <door> (gDoorStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:doors#rightBack"}
Contact  VehicleDoorLeftBack        "Door left back [%s]" <door> (gDoorStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:doors#leftBack"}
Contact  VehicleDoorRightFont       "Door right front [%s]" <door> (gDoorStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:doors#rightFront"}
Contact  VehicleDoorLeftFront       "Door left front [%s]" <door> (gDoorStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:doors#leftFront"}
Contact  VehicleHood                "Hood [%s]" <door> (gDoorStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:doors#hood"}

// Window status
Contact  VehicleWindowRightBack      "Window right back [%s]" <window> (gWindowStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:windows#rightBackWnd"}
Contact  VehicleWindowLeftBack       "Window left back [%s]" <window> (gWindowStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:windows#leftBackWnd"}
Contact  VehicleWindowRightFont      "Window right front [%s]" <window> (gWindowStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:windows#rightFrontWnd"}
Contact  VehicleWindowLeftFront      "Window left front [%s]" <window> (gWindowStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:windows#leftFrontWnd"}
Contact  VehicleWindowRoof           "Window roof [%s]" <window> (gWindowStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:windows#roof"}
Contact  VehicleWindowSunRoof        "Window sun roof [%s]" <window> (gWindowStatus) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:windows#sunroof"}

// Location
Location VehicleLocation             "Location lon/lat" <map> {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:position#location"}

// Last trip
Number   VehicleLastTripDistance     "Last trip distance" <odometer> (gLastTripData) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:lasttrip#tripDistance"}
Number   VehicleLastTripAvgFuelCons  "Last trip average fuel consumption" <sewerage> (gLastTripData) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:lasttrip#averageFuelConsumption"}
Number   VehicleLastTripAvgSpeed     "Last trip average speed" <speed> (gLastTripData) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:lasttrip#tripAverageSpeed"}
DateTime VehicleLastTripStartTime    "Last trip start time [%1$tY-%1$tm-%1$td %1$tR]" <time> (gLastTripData) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:lasttrip#tripStartTime"}
DateTime VehicleLastTripEndTime      "Last trip end time [%1$tY-%1$tm-%1$td %1$tR]" <time> (gLastTripData) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:lasttrip#tripEndTime"}
Number   VehicleLastTripDuration     "Last trip duration" <time> (gLastTripData) {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:lasttrip#tripDuration"}

// Actions
Switch   VehicleDoorLock            "Vehicle DoorLock"  <lock>   [ "Switchable" ]  {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:doors#doorsLocked"}
Switch   VehicleRemoteHeater        "Vehicle Remote Heater"  <temperature>   [ "Switchable" ]  {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:action#remoteHeater"}
Switch   VehicleRemoteVentilation   "Vehicle Remote Ventilation"  <temperature>   [ "Switchable" ]  {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:action#remoteVentilation"}
Number   VehicleTemperature         "Vehicle Temperature"  <temperature>   {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:action#temperature"}
Number   VehicleRemaingHeaterTime   "Vehicle remaining heater time" <clock> {channel="vwweconnect:vehicle:WVGZZZ5XAPQ834262:action#remainingTime"}
````

### Sitemap

````
 sitemap vwweconnect label="Wolkswagen We Connect"
{
    Frame label="Vehicle Details" {
        Image item=VehicleImage label="Vehicle Details"   {
            Frame label="Vehicle Details" {
                Text item=VehicleName icon="car"  
                Text item=VehicleModel icon="car"
                Text item=VehicleCode icon="car"
                Text item=VehicleYear icon="car"
                Text item=VehicleRolloutDate icon="car"
                Text item=VehicleServiceInspection icon="car"
                Text item=VehicleOilInspection icon="car"
            }
        }
    }
    
    Group item=gTotalTripData label="Total Trip Data"

    Group item=gLastTripData label="Last Trip Data"
    
    Group item=gFuelInfo label="Fuel Data"

    Frame label="Vehicle Location" {
        Mapview item=VehicleLocation label="Vehicle Location" height=5
    }

    Group item=gDoorStatus label="Door Status"

    Group item=gWindowStatus label="Window Status"

    Frame label="Vehicle Outdoor Temperature" {
        Text item=VehicleTemperature
    }

    Frame label="Vehicle Actions" {
        Text label="Lock/Unlock, Remote Heater/Ventilation" icon="car" {
            Frame label="Vehicle Doorlock" {
                Switch item=VehicleDoorLock label="Vehicle Doorlock" icon="lock.png"
            }
            Frame label="Vehicle Heater" {
                Switch item=VehicleRemoteHeater label="Vehicle Remote Heater" icon="temperature_hot"
            }
            Frame label="Vehicle Ventilation" {
                Switch item=VehicleRemoteVentilation label="Vehicle Remote Ventilation" icon="temperature_cold"
            }
            Text item=VehicleRemaingHeaterTime
            Switch item=RefreshVehicleStatus label="Manual refresh status from VW We Connect" icon="zoom"
        }
    }
}   
````
