# VolvoOnCall Binding

This binding integrates the [Volvo On Call](https://www.volvocars.com/intl/own/connectivity/volvo-on-call) compatible vehicles.
The integration happens through the WirelessCar Remote API.

## Supported Things

All cars compatible with Volvo On Call shall be supported by this binding.

## Discovery

Once a VocApi Bridge has been created with according credential, vehicles connected to this account will automatically be detected. 

## Binding Configuration

The binding has no configuration options itself, all configuration is done at 'Things' level.

## Thing Configuration

The 'VolvoOnCall API' bridge uses the owner's email address and password in order to access the VOC Remote API.
This is the same email address and password as used in the VolvoOnCall smartphone app, that allows to remotely control your car(s).

Once the bridge created, you'll be able to launch discovery of the vehicles attached to it.

## Channels


## Full Example

demo.Things:

```
Bridge volvooncall:vocapi:glh "VoC Gaël" @ "System" [username="mail@address.org", password="mypassword"]
{
    Thing vehicle XC60 "XC60" @ "World" [vin="theCarVIN", refreshinterval=5]
}
```

demo.items:

```
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
String Voc_Fluid_Message            "Lave Glace"                    (gVoc)                  {channel="volvooncall:vehicle:glh:XC60:other#washerFluidLevel"}
Location Voc_Location               "Location"                      (gVoc)                  {channel="volvooncall:vehicle:glh:XC60:position#location"}
DateTime Voc_Location_LUD           "Timestamp [%1$tH:%1$tM]"   <time>      (gVoc)                  {channel="volvooncall:vehicle:glh:XC60:position#locationTimestamp"}
Switch Voc_Fluid_Alert              "Alerte Lave Glace"         <siren>     (gVoc)

```

voc.sitemap:

```
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

Multiple actions are supported by this binding. In classic rules these are accessible as shown in this example (adjust getActions with your ThingId):

Example

```
 val actions = getActions("volvooncall","volvooncall:vehicle:myVinNumber")
 if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
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
 
Sends the command to start the car heater.

 ### heaterStopCommand()
 
Sends the command to stop the car heater.
 
 ### honkBlinkCommand(honk, blink)
 
Activates lights and/or the horn of the car

 Parameters:
| Name    | Description                               |
|---------|-------------------------------------------|
| honk    | Boolean - Activates the car horn          |
| blink   | Boolean - Activates the car lights        |
