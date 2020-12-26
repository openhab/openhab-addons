# BMW ConnectedDrive Binding

<img align="right" src="./doc/bmw-connected.png" width="150" height="150"/>

The binding provides a connection between [BMW's ConnectedDrive Portal](https://www.bmw-connecteddrive.com/country-region-select/country-region-selection.html) and openHAB.
AllvVehicles connected to an account will be detected by the discovery with the correct type 

* Conventional Fuel Vehicle
* Plugin-Hybrid Electrical Vehicle 
* Battery Electric Vehicle with Range Extender
* Battery Electric Vehicle 

In addition properties are attached with information and services provided by this vehicle.
The provided data depends on 

1. the [Thing Type](#Things) and 
2. the services mentioned in [Properties](#properties)

Different channel groups are clustering all informations.
Check for each group if it's supported for this Vehicle.

Please note **this isn't a real-time binding**. 
If the aoor is opened the state isn't transmitted and changed immediately. 
This isn't a flaw in the binding itself because the state in BMW's own ConnectedDrive App is also updated with some delay. 

## Supported Things

### Bridge

The bridge establishes the connection between BMW ConnectedDrive Portal and openHAB.

| Name                       | Bridge Type ID | Description                                                |
|----------------------------|----------------|------------------------------------------------------------|
| BMW ConnectedDrive Account | account        | Access to BMW ConnectedDrive Portal for a specific user    |


### Things

Four different vehicle types are provided. 
They differ in the supported channel groups & channels. 
Conventional Fuel Vehicles have no _Charging Profile_, Electric Vehicles don't provide a _Fuel Range_. 
For hybrid vehicles in addition to _Fuel and Electric Range_ the _Hybrid Range_ is shown.
 
| Name                                | Thing Type ID | Supported Channel Groups                               |
|-------------------------------------|---------------|--------------------------------------------------------|
| BMW Electric Vehicle                | bev           | status, range, location, service, check, charge, image |
| BMW Electric Vehicle with REX       | bev_rex       | status, range, location, service, check, charge, image |
| BMW Plug-In-Hybrid Electric Vehicle | phev          | status, range, location, service, check, charge, image |
| BMW Conventional Vehicle            | conv          | status, range, location, service, check, image         |

 
#### Properties

<img align="right" src="./doc/properties.png" width="500" height="600"/>

For each vehicle properties are available. 
Basically 3 types of information are registered as properties

* Informations regarding your dealer with address and phone number
* Which services are available / not available
* Vehicle properties like color, model type, drive train and construction year

In the right picture can see in *Services Activated* e.g. the *DoorLock* and *DoorUnlock* services are mentioned. 
This ensures [Channel Group _Remote Services_](#remote-services) is supporting door lock and door unlock.

In  *Services Supported* the entry *LastDestination* is mentioned.
So it's valid to connect Channel Group [Last Destinations](#destinations) in order to display the last 3 Navigation Destinations.

| Property Key       | Property Value      |  Supported Channel Groups    |
|--------------------|---------------------|------------------------------|
| Services Supported | Statistics          | last-trip, lifetime          |
| Services Supported | LastDestinations    | destinations                 |
| Services Activated | list of services    | remote                       |


## Discovery

Auto discovery is starting after creation of the bridge towards BMW's ConnectedDrive. 
A list of your registered vehicles is queried and all found items are added in Inbox.
Unique identifier is the *Vehicle Identification Number* (VIN). 
If a thing is already declared in a  *.things configuration* discovery won't highlight it again.
Properties will be attached to predefined vehicles if the VIN is matching.

## Configuration

### Bridge

| Parameter       | Type    | Description                                                        |           
|-----------------|---------|--------------------------------------------------------------------|
| userName        | text    | BMW Connected Drive Username                                       |
| password        | text    | BMW Connected Drive Password                                       |
| region          | text    | Select Region in order to connect to the appropriate BMW Server.   |

The region Configuration has 3 different possibilities

* _NORTH_AMERICA_
* _CHINA_
* _ROW_ for Rest of World

### Thing

Same configuration is needed for all things

| Parameter       | Type    | Description                           |           
|-----------------|---------|---------------------------------------|
| vin             | text    | Vehicle Identification Number (VIN)   |
| refreshInterval | integer | Refresh Interval in Minutes           |
| units           | text    | Unit Selection. See below.            |
| imageSize       | integer | Image Size                            |
| imageViewport   | text    | Image Viewport                        |

The units can be configured in 3 ways

* _AUTODETECT_ selects miles for US & UK, kilometer otherwise
* _METRIC_ selects directly kilometers
* _IMPERIAL_ selects directly miles

The _imageVieport_ allows to show the vehicle from different angels.
Possible values are 

* FRONT
* REAR
* SIDE
* DASHBOARD
* DRIVERDOOR

## Channels

There are many channels available for each vehicle. 
For better overview they are clustered in different channel groups.
The channel groups are different for the vehicle types, on the build-in sensors of the vehicle and the activated services.


### Thing Channel Groups 

#### Vehicle Status

Reflects status of the vehicle.
Available for all vehicles, read-only.
Channel Group ID is **status**.

| Channel Label             | Channel ID          | Type          | Description                                                                       |
|---------------------------|---------------------|---------------|-----------------------------------------------------------------------------------|
| Overall Door Status       | doors               | String        | **Closed** if all closed otherwise **Open**. **Unknown** if no data is delivered  |
| Overall Window Status     | windows             | String        | **Closed** if all closed otherwise **Open** or **Intermediate**. **Unknown** if no data is delivered   |
| Doors Locked              | lock                | String        | Status if Doors are locked or unlocked                                            |
| Next Service Date         | service-date        | DateTime      | Date of Upcoming Service                                                          |
| Mileage till Next Service | service-mileage     | Number:Length | Mileage till Upcoming Service                                                     |
| Check Control             | check-control       | String        | Indicator if CheckControl is **Active** or **Not Active**. **Unknown** if no data is delivered         |
| Charging Status           | charge              | String        | Only available for PHEV, BEV_REX and BEV                                          |
| Last Status Timestamp     | last-update         | DateTime      | Date and Time of last status update.                                              |

See [further details for DateTime](#last-status-update-timestamp) in case of wrong timestamp values

#### Services

Group for all upcoming services with description, service date and / or service mileage
Channel Group ID is **service**.

| Channel Label                  | Channel ID          | Type           | 
|--------------------------------|---------------------|----------------|
| Service Name                   | name                | String         |
| Service Date                   | date                | Number         |
| Mileage till Service           | mileage             | Number:Length  |

If more than one service is scheduled in the future the String channel _name_ has all possible options attached.

#### Check Control

Group for all current active CheckControl Messages.
Channel Group ID is **check**.

| Channel Label                   | Channel ID          | Type           | 
|---------------------------------|---------------------|----------------|
| CheckControl Description        | name                | String         |
| CheckControl Mileage Occurrence | mileage             | Number:Length  |

If more than one check control message is active all possibilities are set as options to the String channel _name_. 

#### Doors Details

Detailed status of all doors and windows
Available for all vehicles, read-only. 
Channel Group ID is **doors**.

| Channel Label              | Channel ID              | Type          | 
|----------------------------|-------------------------|---------------|
| Driver Door                | driver-front            | String        |
| Driver Door Rear           | driver-rear             | String        |
| Passenger Door             | passenger-front         | String        |
| Passenger Door Rear        | passenger-rear          | String        |
| Trunk                      | trunk                   | String        |
| Hood                       | hood                    | String        |
| Driver Door Window         | window-driver-front     | String        |
| Driver Door Rear Window    | window-driver-rear      | String        |
| Passenger Door Window      | window-passenger-front  | String        |
| Passenger Door Rear Window | window-passenger-rear   | String        |
| Rear Window                | window-rear             | String        |
| Sunroof                    | sunroof                 | String        |

Following Strings will be delivered

* UNKNOWN - no status data available
* INVALID - this item isn't applicable for this vehicle
* CLOSED - the door / window is closed
* OPEN - the door / window is open
* INTERMEDIATE - window in intermediate position, not applicable for doors

#### Range Data

Based on vehicle type (Thing Type ID) some channels are presented or not. 
Conventional fuel vehicles don't provide *Electric Range* and Battery electric Vehicles don't show *Range Fuel*.
Hybrid vehicles have both and in addition *Hybrid Range*.
These are read-only values.
Channel Group ID is **range**.

| Channel Label         | Channel ID            | Type                 | CONV | PHEV | BEV_REX | BEV |
|-----------------------|-----------------------|----------------------|------|------|---------|-----|
| Mileage               | mileage               | Number:Length        |  X   |  X   |    X    |  X  |
| Fuel Range            | range-fuel            | Number:Length        |  X   |  X   |    X    |     |
| Battery Range         | range-electric        | Number:Length        |      |  X   |    X    |  X  | 
| Hybrid Range          | range-hybrid          | Number:Length        |      |  X   |    X    |     | 
| Battery Charge Level  | soc                   | Number:Dimensionless |      |  X   |    X    |  X  |
| Remaining Fuel        | remaining-fuel        | Number:Volume        |  X   |  X   |    X    |     | 
| Fuel Range Radius     | range-radius-fuel     | Number:Length        |  X   |  X   |    X    |     | 
| Electric Range Radius | range-radius-electric | Number:Length        |      |  X   |    X    |  X  | 
| Hybrid Range Radius   | range-radius-hybrid   | Number:Length        |      |  X   |    X    |     | 

See Description [Range vs Range Radius](#range-vs-range-radius) to get more information

#### Charge Profile

Valid for electric and hybrid vehicles
These are read-only values.
Channel Group ID is **charge**.

| Channel Label                      | Channel ID          | Type   | 
|------------------------------------|---------------------|--------|
| Air Conditioning at Departure Time | profile-climate     | Switch | 
| Charging Mode for Profile          | profile-mode        | String | 
| Charging Window Start Time         | window-start        | String | 
| Charging Window End Time           | window-end          | String | 
| Timer 1: Departure Time            | timer1-departure    | String | 
| Timer 1: Scheduled Days            | timer1-days         | String | 
| Timer 1: Enabled                   | timer1-enabled      | Switch | 
| Timer 2: Departure Time            | timer2-departure    | String | 
| Timer 2: Scheduled Days            | timer2-days         | String | 
| Timer 2: Enabled                   | timer2-enabled      | Switch | 
| Timer 3: Departure Time            | timer3-departure    | String | 
| Timer 3: Scheduled Days            | timer3-days         | String | 
| Timer 3: Enabled                   | timer3-enabled      | Switch | 


#### Location

Available for all vehicles.
These are read-only values.
Channel Group ID is **location**.

| Channel Label   | Channel ID          | Type         | 
|-----------------|---------------------|--------------|
| GPS Coordinates | gps                 | Location     | 
| Heading         | heading             | Number:Angle | 

#### Last Trip

Check [Vehicle Properties](#Properties) if *Statistics* is present in *Services Supported*
These are read-only values.
Channel Group ID is **last-trip**.

| Channel Label                           | Channel ID                   | Type          |
|-----------------------------------------|------------------------------|---------------|
| Last Trip Date                          | date                         | DateTime      |
| Last Trip Duration                      | duration                     | Number:Time   |
| Average Power Consumption per 100 km    | average-consumption          | Number:Power  |
| Average Combined Consumption per 100 km | average-combined-consumption | Number:Volume |
| Average Power Recuperation per 100 km   | average-recuperation         | Number:Power  |
| Last Trip Distance                      | distance                     | Number:Length |
| Distance since Last Charge              | distance-since-charging      | Number:Length |


#### Lifetime Statistics

Check [Vehicle Properties](#Properties) if *Statistics* is present in *Services Supported*
These are read-only values.
Channel Group ID is **lifetime**.

| Channel Label                           | Channel ID                   | Type          | 
|-----------------------------------------|------------------------------|---------------|
| Average Power Consumption per 100 km    | average-consumption          | Number:Power  |
| Average Power Recuperation per 100 km   | average-recuperation         | Number:Power  |
| Cumulated Electric Driven Distance      | cumulated-driven-distance    | Number:Length |
| Average Combined Consumption per 100 km | average-combined-consumption | Number:Volume |
| Longest Distance with one Charge        | single-longest-distance      | Number:Length |


#### Remote Services

Check [Vehicle Properties](#Properties) *Services Activated* which remote services are available
Only one remote service can be executed each Time.
Parallel execution isn't supported.
The *Service Execution State* channel is reporting the state.
State *Executed* is the final state when execution is finished.
Channel Group ID is **remote**.

| Channel Label           | Channel ID          | Type    | 
|-------------------------|---------------------|---------|
| Remote Service Command  | command             | String  |
| Service Execution State | state               | String  |

The channel _command_ has the following value options:

* _Flash Lights_ 
* _Vehicle Finder_
* _Door Lock_
* _Door Unlock_
* _Horn Blow_
* _Climate Control_

#### Destinations

Check [Vehicle Properties](#Properties) if *LastDestinations* is present in *Services Supported*
Channel Group ID is **destination**.

| Channel Label                    | Channel ID          | Type      | 
|----------------------------------|---------------------|-----------|
| Destination Name                 | name                | String    |
| Destination GPS Coordinates      | gps                 | Location  |

If more than one service is scheduled in the future the String channel _name_ has all possible options attached.


#### Image

Available for all Vehicles.
Picture can be modified regarding *Viewport* and *Size*.
See [Things Section](#thing) for Viewport possibilities and [Status Image](#status-image) for possible Use Cases.
Channel Group ID is **image**.

| Channel Label                 | Channel ID          | Type   | 
|-------------------------------|---------------------|--------|
| Rendered Image of the Vehicle | png                 | Image  |
| Image Viewport                | view                | String |
| Image Picture Size            | size                | Number |


## Further Descriptions

### List Interface

<img align="right" src="./doc/ServiceOptions.png" width="400" height="350"/>

Currently there are 3 occurrences of dynamic data delivered as Lists

* Upcoming Services delivered in group [Services](#services)
* Check Control Messages delivered in group [Check Control](#check_control)
* Last Destinations delivered in group [Destinations](#destinations)

The channel **name** shows the first element. 
All other elements are attached as options. 
The picture on the right shows the _Service Name_ item and all plus the 4 available options. 
With this you're able to select each service and the corresponding _Service Date & Milage_ will be shown.  

### TroubleShooting

BMW has a high range of Vehicles supported by BMWs ConnectedDrive.
In case of any issues with this Binding please help to resolve it! 
Please perform the following Steps:

* Can you login [into ConnectedDrive](https://www.bmw-connecteddrive.com/country-region-select/country-region-selection.html) with your Credentials? _Please note this isn't the BMW Customer Portal - it's the ConnectedDrive Portal_
* Is the Vehicle listed in your Account? _There's a one-to-one dependency from User to Vehicle_

If the access to the Portal and listing of the Vehicle is checked some debug data is needed in order to identify the issue. 

#### Generate Debug Fingerprint

If you checked the above pre-conditions you need to get the debug fingerprint from the debug logs.
First [enable debug logging](https://www.openhab.org/docs/administration/logging.html#defining-what-to-log) for the binding.
```
log:set DEBUG org.openhab.binding.bmwconnecteddrive
```
The debug fingerprint is generated when the Vehicle Thing is initialized the first time, e.g. after openHAB startup. 
To force a new fingerprint disable the thing shortly and enable it again. 
Personal Data is eliminated from the log entries so it should be possible to share them in public.
Data like

* Dealer Properties
* Vehicle Identification Number (VIN)
* Location Latitude / Longitude 

are anonymized.

After the corresponding Fingerprint is generated please [follow the instructions to raise an Issue](https://community.openhab.org/t/how-to-file-an-issue/68464) and attach the Fingerprint data!
Your feedback is highly appreciated!


### Range vs Range Radius

<img align="right" src="./doc/range-radius.png" width="400" height="350"/>

You will observe differences in the Vehicle Range and Range Radius values. 
While range is indicating the possible distance to be driven on roads the range radius indicates the reachable range on the Map.

The right picture shows the distance between Kassel and Frankfurt in Germany. 
While the Air-line Distance is ~145 Kilometer the Route Distance is ~192 Kilometer.
So Range value is the normal remaining range.
See the Section [OpenStreetMap](#openstreetMap-widget) how the Range Radius is used to indicate the reachable Range on Map.
Please note this is just an indicator and the effective range, especially for Electric Vehicles, 
depends on many factors like driving style and electric consumers. 
 
### Last Status Update Timestamp

A timestamp is showing the last Vehicle Status update. If this isn't shown correctly please check the date settings.
In case of Raspberry Pi execute *raspi-config*, select *Localization Options*, the *Change Time Zone*
Select your *Geographical Area* and afterwards the correct City.
One restart of openHAB service with *systemctl restart openhab2* is necessary in order to see the corrected Time Settings.
 
Correct TimeZone is crucial for handling all Time information in openHAB and it's discussed many times in the Forum.
See [similar discussion in the openHAB Forum](https://community.openhab.org/t/solved-wrong-local-time-how-to-change/90938) which deals with the same problem.


## Full Example

The example is based on a BMW i3 with Range Extender (REX). 
Exchange the 3 configuration parameters in the Things section

* YOUR_USERNAME - with your ConnectedDrive Login Username
* YOUR_PASSWORD - with your ConnectedDrive Password Credentials
* VEHICLE_VIN - the Vehicle Identification Number

In addition search for all occurrences of *i3* and replace it with your Vehicle Identification like *x3* or *535d* and 're ready to go!

### Things

```
Bridge bmwconnecteddrive:account:user   "BMW ConnectedDrive Account" [userName="YOUR_USERNAME",password="YOUR_PASSWORD",region="ROW"] {
         Thing BEV_REX i3       "BMW i3 94h REX"                [ vin="VEHICLE_VIN",units="AUTODETECT",imageSize=600,imageViewport="FRONT",refreshInterval=5]
}
```

### Items

```
Number:Length           i3Mileage                 "Odometer [%d %unit%]"                        <line>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:range#mileage" }                                                                           
Number:Length           i3Range                   "Range [%d %unit%]"                           <motion>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:range#hybrid"}
Number:Length           i3RangeElectric           "Electric Range [%d %unit%]"                  <motion>        (i3,long)   {channel="bmwconnecteddrive:BEV_REX:user:i3:range#electric"}   
Number:Length           i3RangeFuel               "Fuel Range [%d %unit%]"                      <motion>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:range#fuel"}
Number:Dimensionless    i3BatterySoc              "Battery Charge [%.1f %%]"                    <battery>       (i3,long)   {channel="bmwconnecteddrive:BEV_REX:user:i3:range#soc"}
Number:Volume           i3Fuel                    "Fuel [%.1f %unit%]"                          <oil>           (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:range#remaining-fuel"}
Number:Length           i3RadiusElectric          "Electric Radius [%d %unit%]"                 <zoom>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:range#radius-electric" }
Number:Length           i3RadiusHybrid            "Hybrid Radius [%d %unit%]"                   <zoom>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:range#radius-hybrid" }

String                  i3DoorStatus              "Door Status [%s]"                            <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:status#doors" }
String                  i3WindowStatus            "Window Status [%s]"                          <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:status#windows" }
String                  i3LockStatus              "Lock Status [%s]"                            <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:status#lock" }
DateTime                i3NextServiceDate         "Next Service Date [%1$tb %1$tY]"             <calendar>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:status#service-date" }
String                  i3NextServiceMileage      "Next Service Mileage [%d %unit%]"            <line>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:status#service-mileage" }
String                  i3CheckControl            "Check Control [%s]"                          <error>         (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:status#check-control" }
String                  i3ChargingStatus          "Charging [%s]"                               <energy>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:status#charge" } 
DateTime                i3LastUpdate              "Update [%1$tA, %1$td.%1$tm. %1$tH:%1$tM]"    <calendar>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:status#last-update"}

DateTime                i3TripDateTime            "Trip Date [%1$tA, %1$td.%1$tm. %1$tH:%1$tM]" <calendar>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#date"}
Number:Time             i3TripDuration            "Trip Duration [%d %unit%]"                   <time>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#duration"}
Number:Length           i3TripDistance            "Distance [%d %unit%]"                        <line>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#distance" }                                                                           
Number:Length           i3TripDistanceSinceCharge "Distance since last Charge [%d %unit%]"      <line>          (i3,long)   {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#distance-since-charging" }                                                                           
Number:Energy           i3AvgTripConsumption      "Average Consumption [%.1f %unit%]"           <energy>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#average-consumption" }                                                                           
Number:Volume           i3AvgTripCombined         "Average Combined Consumption [%.1f %unit%]"  <oil>           (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#average-combined-consumption" }                                                                           
Number:Energy           i3AvgTripRecuperation     "Average Recuperation [%.1f %unit%]"          <energy>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#average-recuperation" }                                                                           

Number:Length           i3CumulatedElectric       "Electric Distance Driven [%d %unit%]"        <line>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:lifetime#cumulated-driven-distance" }                                                                           
Number:Length           i3LongestEVTrip           "Longest Electric Trip [%d %unit%]"           <line>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:lifetime#single-longest-distance" }                                                                           
Number:Energy           i3AvgConsumption          "Average Consumption [%.1f %unit%]"           <energy>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:lifetime#average-consumption" }                                                                           
Number:Volume           i3AvgCombined             "Average Combined Consumption [%.1f %unit%]"  <oil>           (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:lifetime#average-combined-consumption" }                                                                           
Number:Energy           i3AvgRecuperation         "Average Recuperation [%.1f %unit%]"          <energy>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:lifetime#average-recuperation" }  

Number                  i3Latitude                "Latitude  [%.4f]"                            <zoom>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:location#latitude" }                                                                           
Number                  i3Longitude               "Longitude  [%.4f]"                           <zoom>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:location#longitude" }                                                                           
Number:Angle            i3Heading                 "Heading [%.1f %unit%]"                       <zoom>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:location#heading" }  

Switch                  i3RemoteFlash             "Flash"                                       <switch>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#light" } 
Switch                  i3RemoteFinder            "Finder"                                      <switch>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#finder" } 
Switch                  i3RemoteLock              "Lock"                                        <switch>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#lock" } 
Switch                  i3RemoteUnlock            "Unlock"                                      <switch>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#unlock" } 
Switch                  i3RemoteHorn              "Horn Blow"                                   <switch         (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#horn" } 
Switch                  i3RemoteClimate           "Climate"                                     <climate>       (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#climate" } 
String                  i3RemoteState             "Remote Execution State [%s]"                 <status>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#state" } 

String                  i3DriverDoor              "Driver Door [%s]"                            <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#driver-front" }
String                  i3DriverDoorRear          "Driver Door Rear [%s]"                       <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#driver-rear" }
String                  i3PassengerDoor           "Passenger Door [%s]"                         <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#passenger-front" }
String                  i3PassengerDoorRear       "Passenger Door Rear [%s]"                    <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#passenger-rear" }
String                  i3Hood                    "Hood [%s]"                                   <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#hood" }
String                  i3Trunk                   "Trunk [%s]"                                  <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#trunk" }
String                  i3DriverWindow            "Driver Window [%s]"                          <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#window-driver-front" }
String                  i3DriverWindowRear        "Driver Window Rear [%s]"                     <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#window-driver-rear" }
String                  i3PassengerWindow         "Passenger Window [%s]"                       <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#window-passenger-front" }
String                  i3PassengerWindowRear     "Passenger Window Rear [%s]"                  <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#window-passenger-rear" }
String                  i3RearWindow              "Rear Window [%s]"                            <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#window-rear" }
String                  i3Sunroof                 "Sunroof [%s]"                                <lock>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:doors#sunroof" }

String                  i3ServiceName              "Service Name [%s]"                          <text>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:service#name" }
Number:Length           i3ServiceMileage           "Service Mileage [%d %unit%]"                <line>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:service#mileage" }
DateTime                i3ServiceDate              "Service Date [%1$tb %1$tY]"                 <calendar>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:service#date" }
Number                  i3ServiceCount             "Service Count [%d]"                         <settings>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:service#size" }
Number                  i3ServiceIndex             "Service Index [%d]"                         <settings>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:service#index" }
Switch                  i3NextService              "Scroll"                                     <settings>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:service#next" }

String                  i3CCName                   "CheckControl Name [%s]"                     <text>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:check#name" }
Number:Length           i3CCMileage                "CheckControl Mileage [%d %unit%]"           <line>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:check#mileage" }
Number                  i3CCCount                  "CheckControl Count [%d]"                    <settings>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:check#size" }
Number                  i3CCIndex                  "CheckControl Index [%d]"                    <settings>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:check#index" }
Switch                  i3CCNext                   "Scroll"                                     <settings>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:check#next" }

String                  i3DestName                "Destination [%s]"                            <house>         (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:destination#name" } 
Number                  i3DestLat                 "Longitude [%.4f]"                            <zoom>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:destination#latitude" }                                                                           
Number                  i3DestLon                 "Latitude [%.4f]"                             <zoom>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:destination#longitude" }                                                                           
Number                  i3DestCount               "Destination Count [%d]"                      <settings>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:destination#size" } 
Number                  i3DestIndex               "Destination Index [%d]"                      <settings>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:destination#index" }                                                                           
Switch                  i3DestNext                "Next Destination"                            <settings>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:destination#next" }                                                                           
 
Switch                  i3ChargeProfileClimate    "Charge Profile Climatization"                <temperature>   (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#profile-climate" }  
String                  i3ChargeProfileMode       "Charge Profile Mode [%s]"                    <energy>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#profile-mode" } 
String                  i3ChargeWindowStart       "Charge Window Start [%s]"                    <time>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#window-start" } 
String                  i3ChargeWindowEnd         "Charge Window End [%s]"                      <time>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#window-end" } 
String                  i3Timer1Departure         "Timer 1 Departure [%s]"                      <time>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#timer1-departure" } 
String                  i3Timer1Days              "Timer 1 Days [%s]"                           <calendar>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#timer1-days" } 
Switch                  i3Timer1Enabled           "Timer 1 Enabled"                             <switch>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#timer1-enabled" }  
String                  i3Timer2Departure         "Timer 2 Departure [%s]"                      <time>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#timer2-departure" } 
String                  i3Timer2Days              "Timer 2 Days [%s]"                           <calendar>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#timer2-days" } 
Switch                  i3Timer2Enabled           "Timer 2 Enabled"                             <switch>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#timer2-enabled" }  
String                  i3Timer3Departure         "Timer 3 Departure [%s]"                      <time>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#timer3-departure" } 
String                  i3Timer3Days              "Timer 3 Days [%s]"                           <calendar>      (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#timer3-days" } 
Switch                  i3Timer3Enabled           "Timer 3 Enabled"                             <switch>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:charge#timer3-enabled" }  

Image                   i3Image                   "Image"                                                       (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:image#png" }  
String                  i3ImageViewport           "Image Viewport [%s]"                         <zoom>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:image#view" }  
Number                  i3ImageSize               "Image Size [%d]"                             <zoom>          (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:image#size" }  

Switch                  i3Troubleshoot            "Vehicle Fingerprint"                         <switch>        (i3)        {channel="bmwconnecteddrive:BEV_REX:user:i3:troubleshoot#vehicle-fingerprint" }  
Switch                  i3DiscoveryTroubleshoot   "Discovery Fingerprint"                       <switch>        (i3)        {channel="bmwconnecteddrive:account:user:discovery-fingerprint" }  
```

### Sitemap

```
sitemap BMW label="BMW" {
  Frame label="BMW i3" {
    Image  item=i3Image                     
  } 
  Frame label="Range" {
    Text    item=i3Mileage           
    Text    item=i3Range             
    Text    item=i3RangeElectric     
    Text    item=i3RangeFuel         
    Text    item=i3BatterySoc        
    Text    item=i3Fuel              
    Text    item=i3RadiusElectric       
    Text    item=i3RadiusHybrid         
  }
  Frame label="Status" {
    Text    item=i3DoorStatus           
    Text    item=i3WindowStatus         
    Text    item=i3LockStatus           
    Text    item=i3NextServiceDate              
    Text    item=i3NextServiceMileage       
    Text    item=i3CheckControl         
    Text    item=i3ChargingStatus           
    Text    item=i3LastUpdate               
  }
  Frame label="Remote Services" {
    Switch  item=i3RemoteFlash              
    Switch  item=i3RemoteFinder            
    Switch  item=i3RemoteLock               
    Switch  item=i3RemoteUnlock             
    Switch  item=i3RemoteHorn               
    Switch  item=i3RemoteClimate            
    Text    item=i3RemoteState              
  }
  Frame label="Last Trip" {
    Text    item=i3TripDateTime            
    Text    item=i3TripDuration            
    Text    item=i3TripDistance            
    Text    item=i3TripDistanceSinceCharge 
    Text    item=i3AvgTripConsumption      
    Text    item=i3AvgTripRecuperation     
    Text    item=i3AvgTripCombined     
  }
  Frame label="Lifetime" {
    Text    item=i3CumulatedElectric  
    Text    item=i3LongestEVTrip      
    Text    item=i3AvgConsumption     
    Text    item=i3AvgRecuperation          
    Text    item=i3AvgCombined          
  }
  Frame label="Services" {
    Text    item=i3ServiceName          
    Text    item=i3ServiceMileage          
    Text    item=i3ServiceDate          
    Text    item=i3ServiceCount          
    Text    item=i3ServiceIndex          
    Switch  item=i3NextService            
  }
  Frame label="CheckControl" {
    Text    item=i3CCName          
    Text    item=i3CCMileage          
    Text    item=i3CCCount          
    Text    item=i3CCIndex          
    Switch  item=i3CCNext            
  }
  Frame label="Door Details" {
    Text    item=i3DriverDoor
    Text    item=i3DriverDoorRear  
    Text    item=i3PassengerDoor 
    Text    item=i3PassengerDoorRear 
    Text    item=i3Hood 
    Text    item=i3Trunk
    Text    item=i3DriverWindow
    Text    item=i3DriverWindowRear 
    Text    item=i3PassengerWindow 
    Text    item=i3PassengerWindowRear 
    Text    item=i3RearWindow 
    Text    item=i3Sunroof 
  }

  Frame label="Location" {
    Text    item=i3Latitude           
    Text    item=i3Longitude          
    Text    item=i3Heading             
  }
  Frame label="Charge Profile" {    
    Switch  item=i3ChargeProfileClimate     
    Text    item=i3ChargeProfileMode        
    Text    item=i3ChargeWindowStart        
    Text    item=i3ChargeWindowEnd          
    Text    item=i3Timer1Departure          
    Text    item=i3Timer1Days               
    Switch  item=i3Timer1Enabled            
    Text    item=i3Timer2Departure          
    Text    item=i3Timer2Days               
    Switch  item=i3Timer2Enabled            
    Text    item=i3Timer3Departure          
    Text    item=i3Timer3Days               
    Switch  item=i3Timer3Enabled            
  } 
  Frame label="Last Destinations" {    
    Text  item=i3DestName                 
    Text  item=i3DestLat                                                                                   
    Text  item=i3DestLon                                                                                    
    Text  item=i3DestCount                 
    Text  item=i3DestIndex                                                                                        
    Switch  item=i3DestNext                                                                                        
  }  
  Frame label="Troubleshooting & Image Properties" {
    Text    item=i3ImageViewport
    Text    item=i3ImageSize 
    Switch  item=i3DiscoveryTroubleshoot    
    Switch  item=i3Troubleshoot             
  } 
}
```

## Going further

You're now able to receive the Vehicle Data in openHAB. Continue the work and combine this data with other Powerful openHAB Features, Bindings and Widgets.

### Notification

A quite handy rule if you aren't the *permanent Driver* of the Car but 're somehow responsible for it e.g for one of your family Members.
As soon as a check control message occurs a message notification is sent to the Android App.
Below the rule as an example. 
Just insert the Mail Address used for openHAB Cloud Connector.

```
// App Notification if Check Control Message is active
rule "CheckControl"
    when
        System started or
        Item i3CCCount changed 
    then
        val count = i3CCCount.state as Number
        if(count.intValue > 0) {
            sendNotification("bernd.w@ymann.de","i3 Check Control: "+i3CCName.state)
        }                                                    
end
```

Besides the CheckControl the next upcoming Service can be monitored.
The below rule checks every Monday at 9:00 o'clock if the next Service is required in the next 30 days.
Time enough to schedule a date at your favorite Garage. 

```
// Cron for Service observation
rule "Service"
    when
        System started or
        Time cron "0 0 9 ? * MON *"
    then
        val DateTime dt = new DateTime(i3ServiceDate.state.toString)
        val Number daysToService = (dt.getMillis - now.millis) / 1000 / 60 / 60 / 24
        if(daysToService < 30) {
            logInfo("Service Date","Time to schedule Service")
                sendNotification("YOUR_OPENHAB_EMAIL", "Service required in " + daysToService + " days")
        } else {
            logInfo("Service Date","{} days to {} Service left ",daysToService,i3ServiceName.state.toString)
        }
end
```

### OpenstreetMap Widget

<img align="right" src="./doc/panel.png" width="600" height="260"/>

The [OpenStreetMap Widget](https://community.openhab.org/t/custom-widget-map/39225) can be used to display a Map on the UI Panel.
It's configurable with *Markers* and *Accuracy Circles* which are quite handy to display several informations.
See the HABPanel example with the OpenStreetMap Widget on the right side with

* Marker of current Vehicle location as Center of the Map
* Green Circle showing Electric Range Radius
* Blue Circle showing Hybrid Range Radius
* One additional Markers for the current selected Destinations

Some small modifications are needed to get the Location values. 
The items file declares some String with Latitude, Longitude values as String used in the Markers of the Widget. 
The Range Radius is needed in Meters so a small conversion is needed.
The rules file is performing the calculations on startup and if the values are changing. 
If the location of the Vehicle changes it will be shown in the Map

#### Additional Items

```
// Range Radius Items
String          i3LatLongElectric      "Location String"
Number          i3RadiusElectricMeter  "Electric Range Radius"
String          i3LatLongHybrid        "Location String"
Number          i3RadiusHybridMeter    "Hybrid Range Radius"
//Destination Items
String          mapDestination         "Destination"
```

#### Rules

```
// Hybrid and Electric Range Radius plus Vehicle Coordinates
rule "LocationRangeChange"
    when
        System started or
        Item i3Latitude changed or
            Item i3Longitude changed or
            Item i3RangeRadiusElectric changed or
        Item i3RangeRadiusHybrid changed
    then
        // Electric Range Radius & Vehicle Coordinates
        val latitudeNumber = i3Latitude.state as Number
        val longitudeNumber = i3Longitude.state as Number
        i3LatLongElectric.sendCommand(latitudeNumber.floatValue+","+longitudeNumber.floatValue)
        val radiusElectricNumber = i3RadiusElectric.state as Number
        i3RadiusElectricMeter.sendCommand(radiusElectricNumber.intValue * 1000)
        
        // Hybrid Range Radius & Vehicle Coordinates
        i3LatLongHybrid.sendCommand(latitudeNumber.floatValue +","+longitudeNumber.floatValue)
        val radiusHybridNumber = i3RadiusHybrid.state as Number
        logInfo("Rage Map","Radius Hybrid"+radiusHybridNumber.intValue)
        i3RadiusHybridMeter.sendCommand(radiusHybridNumber.intValue * 1000)    
end

// Changes Coordinates to the current selected Destination List Item
rule "Destinations"
    when
        Item i3DestIndex changed   
    then   
        val lat = i3DestLat.state as Number
        val lon = i3DestLon.state as Number
        mapDestination.sendCommand(lat.floatValue +","+lon.floatValue) 
end
```

### Status Image

<img align="right"  src="./doc/CarStatusImages.png" width="400" height="300"/>

This Rule is aimed to improve the visibility of the Vehicle Status. 
Therefore the Image is used to reflect _an overall status_ which can be identified at the first glance.
As an example the Rule is reflecting the following status as Image

* Side - Vehicle is charging
* Driver Door - Doors are not locked => execute [Remote Service](#remote-services) Lock
* Dashboard - Check Control Message is available
* Front - Vehicle is at the Home Location
* Rear - Vehicle is away from Home Location

#### Status Image Rule

```
// Change Image according to Vehicle Status
rule "Image Status"
    when
        System started or
        Item i3ChargingStatus changed or
        Item i3Latitude changed or
        Item i3Longitude changed or
        Item i3CheckControl changed or
        Item i3LockStatus changed
    then
        if(i3ChargingStatus.state.toString == "Charging") {
            logInfo("Vehicle Image","Charging")
            i3ImageViewport.sendCommand("SIDE")
        } else if(i3LockStatus.state.toString != "Secured") {
            logInfo("Vehicle Image","Doors not locked")
            i3ImageViewport.sendCommand("DRIVERDOOR")
        } else if(i3CheckControl.state.toString != "Ok") {
            logInfo("Vehicle Image","Check Control Active")
            i3ImageViewport.sendCommand("DASHBOARD")
        } else {
            val latitudeNumber = i3Latitude.state as Number
            val longitudeNumber = i3Longitude.state as Number
            // Home Location Range
            if((1.23 < latitudeNumber.floatValue) && ( latitudeNumber.floatValue < 1.24) && (3.21 < longitudeNumber.floatValue) && (longitudeNumber.floatValue < 3.22) ) {
                logInfo("Vehicle Image","Home Location")
                i3ImageViewport.sendCommand("FRONT")
            } else {
                logInfo("Vehicle Image","Vehicle is away")
                i3ImageViewport.sendCommand("REAR")
            }    
        }
end
```

## Credits

This work is based on the project of [Bimmer Connected](https://github.com/bimmerconnected/bimmer_connected). 
Also a [manual installation based on python](https://community.openhab.org/t/script-to-access-the-bmw-connecteddrive-portal-via-oh/37345) was already available for openHAB.
This Binding is basically a port to openHAB based on these concept works!  
