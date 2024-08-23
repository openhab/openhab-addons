# VolvoOnCall Binding

This binding integrates the [Volvo On Call](https://www.volvocars.com/intl/own/connectivity/volvo-on-call) compatible vehicles.
The integration happens through the WirelessCar Remote API.

## Supported Things

All cars compatible with Volvo On Call shall be supported by this binding.

## Discovery

Once a VocApi Bridge has been created with according credential, vehicles connected to this account will automatically be detected.

## Binding Configuration

The binding has no configuration options itself, all configuration is done at 'Things' level.

## Bridge Configuration

The 'VolvoOnCall API' bridge uses the owner's email address and password in order to access the VOC Remote API.
This is the same email address and password as used in the VolvoOnCall smartphone app, that allows to remotely control your car(s).

| Parameter       | Description                                          | Required |
|-----------------|------------------------------------------------------|--------- |
| username        | Username from the VolvoOnCall app (email address)    | yes      |
| password        | Password from the VolvoOnCall app                    | yes      |

Once the bridge created, you will be able to launch discovery of the vehicles attached to it.

## Thing Configuration

The 'VolvoOnCall API' bridge uses the owner's email address and password in order to access the VOC Remote API.

| Parameter       | Name             | Description                                             | Required |
|-----------------|------------------|---------------------------------------------------------|----------|
| vin             | Vin              | Vehicle Identification Number of the car                | yes      |
| refreshinterval | Refresj Interval | Interval in minutes to refresh the data (default=10)    | yes      |

## Channels

All numeric channels use the [UoM feature](https://openhab.org/blog/2018/02/22/units-of-measurement.html).
This means you can easily change the desired unit e.g. miles/h instead of km/h just in your item definition.

Some of the channels are only available for specific cars and models. These properties are added to the Thing
automatically by the binding when an API call is made.

Following channels are currently available:

| Channel Type ID                               | Item Type            | Description                                        | Remark                                         |
|-----------------------------------------------|----------------------|----------------------------------------------------|------------------------------------------------|
| doors#frontLeft                               | Contact              | Door front left                                    |                                                |
| doors#frontRight                              | Contact              | Door front right                                   |                                                |
| doors#rearLeft                                | Contact              | Door rear left                                     |                                                |
| doors#rearRight                               | Contact              | Door rear right                                    |                                                |
| doors#hood                                    | Contact              | Hood                                               |                                                |
| doors#tailgate                                | Contact              | Tailgate                                           |                                                |
| doors#carLocked                               | Switch               | Is the car locked                                  | Can also be used to lock / unlock the car. Only if property 'lock' is true. |
| windows#frontLeftWnd                          | Contact              | Window front left                                  |                                                |
| windows#frontRightWnd                         | Contact              | Window front right                                 |                                                |
| windows#rearLeftWnd                           | Contact              | Window rear left                                   |                                                |
| windows#rearRightWnd                          | Contact              | Window rear right                                  |                                                |
| odometer#odometer                             | Number:Length        | Odometer value                                     |                                                |
| odometer#tripmeter1                           | Number:Length        | Trip meter 1 value                                 |                                                |
| odometer#tripmeter2                           | Number:Length        | Trip meter 2 value                                 |                                                |
| tank#fuelAmount                               | Number:Volume        | Amount of fuel left in the tank                    |                                                |
| tank#fuelLevel                                | Number:Dimensionless | Percentage of fuel left in the tank                |                                                |
| tank#fuelAlert                                | Switch               | Alert if the amount of fuel is running low         | ON when distancy to empty < 100                |
| tank#distanceToEmpty                          | Number:Length        | Distance till tank is empty                        |                                                |
| position#location                             | Location             | Location of the car                                |                                                |
| position#locationTimestamp                    | DateTime             | Timestamp of the latest confirmed location         |                                                |
| tyrePressure#frontLeftTyre                    | Number               | Tyrepressure front left tyre                       | Normal / LowSoft                               |
| tyrePressure#frontRightTyre                   | Number               | Tyrepressure front right tyre                      | Normal / LowSoft                               |
| tyrePressure#rearLeftTyre                     | Number               | Tyrepressure rear left tyre                        | Normal / LowSoft                               |
| tyrePressure#rearRightTyre                    | Number               | Tyrepressure rear right tyre                       | Normal / LowSoft                               |
| other#averageSpeed                            | Number:Speed         | Average speed                                      |                                                |
| other#engineRunning                           | Switch               | Is the car engine running                          |                                                |
| other#remoteHeater                            | Switch               | Start the car remote heater                        | Only if property 'remoteHeater' is true        |
| other#preclimatization                        | Switch               | Start the car preclimatization                     | Only if property 'preclimatization' is true    |
| other#brakeFluidLevel                         | Number               | Brake fluid level                                  | Normal / Low / VeryLow                         |
| other#washerFluidLevel                        | Number               | Washer fluid level                                 | Normal / Low / VeryLow                         |
| other#serviceWarning                          | String               | Warning if service is needed                       |                                                |
| other#bulbFailure                             | Switch               | ON if at least one bulb is reported as failed      |                                                |
| battery#batteryLevel                          | Number:Dimensionless | Battery level                                      | Only for Plugin hybrid / Twin Engine models. The binding reports undefined in situations where it knows the API is misleading. |
| battery#batteryLevelRaw                       | Number:Dimensionless | Battery level                                      | Only for Plugin hybrid / Twin Engine models. Raw figure from the API, can be misleading. |
| battery#batteryDistanceToEmpty                | Number:Length        | Distance until battery is empty                    | Only for Plugin hybrid / Twin Engine models    |
| battery#chargeStatus                          | String               | Charging status                                    | Only for Plugin hybrid / Twin Engine models    |
| battery#chargeStatusCable                     | Switch               | Is the cable plugged in                            | Only for Plugin hybrid / Twin Engine models    |
| battery#chargeStatusCharging                  | Switch               | Is the car currently charging                      | Only for Plugin hybrid / Twin Engine models    |
| battery#chargeStatusFullyCharged              | Switch               | Is the car fully charged                           | Only for Plugin hybrid / Twin Engine models    |
| battery#timeToHVBatteryFullyCharged           | Number:Time          | Time in minutes until the battery is fully charged | Only for Plugin hybrid / Twin Engine models    |
| battery#chargingEnd                           | DateTime             | Calculated time when the battery is fully charged  | Only for Plugin hybrid / Twin Engine models    |
| lasttrip#tripConsumption                      | Number:Volume        | Last trip fuel consumption                         |                                                |
| lasttrip#tripDistance                         | Number:Length        | Last trip distance                                 |                                                |
| lasttrip#tripStartTime                        | DateTime             | Last trip start time                               |                                                |
| lasttrip#tripEndTime                          | DateTime             | Last trip end time                                 |                                                |
| lasttrip#tripDuration                         | Number:Time          | Last trip duration                                 |                                                |
| lasttrip#tripStartOdometer                    | Number:Length        | Last trip start odometer                           |                                                |
| lasttrip#tripStopOdometer                     | Number:Length        | Last trip stop odometer                            |                                                |
| lasttrip#startPosition                        | Location             | Last trip start location                           |                                                |
| lasttrip#endPosition                          | Location             | Last trip end location                             |                                                |

## Events

| Channel Type ID    | Options     | Description                                                    |
|--------------------|-------------|----------------------------------------------------------------|
| other#carEvent     |             |                                                                |
|                    | CAR_STOPPED | Triggered when the car has finished a trip                     |
|                    | CAR_MOVED   | Triggered if the car mileage has changed between two polls     |
|                    | CAR_STARTED | Triggered when the engine of the car went on between two polls |

## Full Example

demo.things:

```java
Bridge volvooncall:vocapi:glh "VoC Gaël" @ "System" [username="mail@address.org", password="mypassword"]
{
    Thing vehicle XC60 "XC60" @ "World" [vin="theCarVIN", refreshinterval=5]
}
```

demo.items:

```java
Group gVoc "Volvo On Call" 

Group:Contact:OR(OPEN,CLOSED) gDoorsOpening "Portes"     (gVoc)
Contact Voc_DoorsTailgate           "Tailgate"                              (gDoorsOpening)         {channel="volvooncall:vehicle:glh:XC60:doors#tailgate"}
Contact Voc_DoorsRearRight          "Rear right"                            (gDoorsOpening)         {channel="volvooncall:vehicle:glh:XC60:doors#rearRight"}
Contact Voc_DoorsRearLeft           "Rear Left"                             (gDoorsOpening)         {channel="volvooncall:vehicle:glh:XC60:doors#rearLeft"}
Contact Voc_DoorsFrontRight         "Passager"                              (gDoorsOpening)         {channel="volvooncall:vehicle:glh:XC60:doors#frontRight"}
Contact Voc_DoorsFrontLeft          "Conducteur"                            (gDoorsOpening)         {channel="volvooncall:vehicle:glh:XC60:doors#frontLeft"}
Contact Voc_DoorsHood               "Hood"                                  (gDoorsOpening)         {channel="volvooncall:vehicle:glh:XC60:doors#hood"}

Group:Contact:OR(OPEN,CLOSED) gWindowsOpening "Fenêtres"   (gVoc)
Contact Voc_WindowsRearRightWnd     "Rear right"                            (gWindowsOpening)       {channel="volvooncall:vehicle:glh:XC60:windows#rearRightWnd"}
Contact Voc_WindowsRearLeftWnd      "Rear Left"                             (gWindowsOpening)       {channel="volvooncall:vehicle:glh:XC60:windows#rearLeftWnd"}
Contact Voc_WindowsFrontRightWnd    "Passager"                              (gWindowsOpening)       {channel="volvooncall:vehicle:glh:XC60:windows#frontRightWnd"}
Contact Voc_WindowsFrontLeftWnd     "Conducteur"                            (gWindowsOpening)       {channel="volvooncall:vehicle:glh:XC60:windows#frontLeftWnd"}

Switch Voc_DoorsCarLocked           "Verouillée"                            (gVoc)                  {channel="volvooncall:vehicle:glh:XC60:doors#carLocked"}
Number:Length Voc_Odometer          "Kilométrage [%d %unit%]"               (gVoc)     {channel="volvooncall:vehicle:glh:XC60:odometer#odometer"}
Number:Dimensionless Voc_FuelLevel  "Fuel Level"                <sewerage>  (gVoc)     {channel="volvooncall:vehicle:glh:XC60:tank#fuelLevel"}
Switch Voc_Fuel_Alert               "Niveau Carburant"          <siren>     (gVoc)   {channel="volvooncall:vehicle:glh:XC60:tank#fuelAlert"}
Number Voc_Fluid_Message            "Lave Glace"                    (gVoc)                  {channel="volvooncall:vehicle:glh:XC60:other#washerFluidLevel"}
Location Voc_Location               "Location"                      (gVoc)                  {channel="volvooncall:vehicle:glh:XC60:position#location"}
DateTime Voc_Location_LUD           "Timestamp [%1$tH:%1$tM]"   <time>      (gVoc)                  {channel="volvooncall:vehicle:glh:XC60:position#locationTimestamp"}
Switch Voc_Fluid_Alert              "Alerte Lave Glace"         <siren>     (gVoc)

```

voc.sitemap:

```perl
sitemap voc label="Volvo On Call" {
    
    Frame label="Etat Véhicule" {
        Switch item=Voc_DoorsCarLocked
        Switch item=Voc_Location_LUD mappings=[REFRESH='MAJ !']
        Default item=Voc_Odometer
        Default item=Voc_FuelLevel
        Default item=Voc_Fuel_Alert
        Default item=Voc_Fluid_Message
        Default item=Voc_Fluid_Alert
    }

    Frame label="" {
        Mapview item=Voc_Location label="" height=10
    }
        
    Frame label="Opening Status" {
        Group item=gDoorsOpening
        Group item=gWindowsOpening
    }
}
```

## Rule Actions

Multiple actions are supported by this binding. In classic rules these are accessible as shown in the example below:

Example 1a: If Thing has been created using autodiscovery

```java
 val actions = getActions("volvooncall","volvooncall:vehicle:thingId")
 if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
 } else {
        actions.openCarCommand()
 }
```

Example 1b: If Thing has been created using script

```java
 val actions = getActions("volvooncall","volvooncall:vehicle:bridgeId:thingId")
 if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
 } else {
        actions.openCarCommand()
 }
```

### closeCarCommand()

Sends the command to close the car.

### openCarCommand()

Sends the command to open the car.

### engineStartCommand(runtime)

Sends the command to start the engine for a given runtime. Default 5 minutes.

 Parameters:

| Name    | Description                                   |
|---------|-----------------------------------------------|
| runtime | Integer - Time for the engine to stay on      |

### heaterStartCommand()

Sends the command to start the car heater (if remoteHeaterSupported).

### heaterStopCommand()

Sends the command to stop the car heater (if remoteHeaterSupported).

### preclimatizationStartCommand()

Sends the command to start the car heater (if preclimatizationSupported).

### preclimatizationStopCommand()

Sends the command to stop the car heater (if preclimatizationSupported).

### honkBlinkCommand(honk, blink)

Activates lights and/or the horn of the car

 Parameters:

| Name    | Description                               |
|---------|-------------------------------------------|
| honk    | Boolean - Activates the car horn          |
| blink   | Boolean - Activates the car lights        |
