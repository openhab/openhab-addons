# BMW ConnectedDrive Binding

<img align="right" src="./doc/bmw-connected.png" width="150" height="150"/>

The Binding connects your BMW Vehicles which are registered in the _Garage_ of your BMW ConnectedDrive Portal.
Due to the high variety of Cars and ConnectedDrive Services you need to check which Channels are applicable to your Car. 
The Discovery Service recognizes your Car with the correct type 

* Conventional Fuel Vehicle
* Plugin-Hybrid Electrical Vehicle 
* Battery Electric Vehicle with Range Extender
* Battery Electric Vehicle 

In addition Properties are attached to the Vehicle Thing to observe the Supported & Activated Services.
Different Channel Groups are available so you are able to cross-check which group is supported by your Car.  

## Supported Things

### Bridge

The Bridge establishes the Connection between BMW ConnectedDrive Portal and openHAB.

| Name                       | Bridge Type ID | Description                                                |
|----------------------------|----------------|------------------------------------------------------------|
| BMW ConnectedDrive Account | account        | Access to BMW ConnectedDrive Portal for a specific user    |


### Things

Four different Vehicle Types are provided. They differ in the supported Channel Groups & Channels. 
While Conventional Fuel Vehicles have no "Charging Profile" Electric Vehicles don't provide a _fuel range_. 
For Hybrid Vehicles _fuel range_ and _electric range_ is provided and in addition a _combined range_
 
| Name                                | Thing Type ID | Supported Channel Groups                                   |
|-------------------------------------|---------------|------------------------------------------------------------|
| BMW Electric Vehicle                | BEV           | status, range, last-trip, all-trips, charge-profile, image, troubeshoot |
| BMW Electric Vehicle with REX       | BEV_REX       | status, range, last-trip, all-trips, charge-profile, image, troubeshoot |
| BMW Plug-In-Hybrid Electric Vehicle | PHEV          | status, range, last-trip, all-trips, image, troubeshoot |
| BMW Conventional Vehicle            | CONV          | status, range, last-trip, all-trips, image, troubeshoot |

#### Properties

<img align="right" src="./doc/properties.png" width="600" height="400"/>

For each Vehicle Properties are set with various informations. 
They are especially handy to figure out the provided Services of your Vehicle. 
In the right picture you can see in _Activated Services_ e.g. the _DoorLock_ and _DoorUnlock_ Services are mentioned. 
So you're sure that in Channel Group _Remote Services_ you are able to execute these commands.
Also _LastDestinations_ is mentioned in _Supported Services_ so it's valid to connect Channel Group _Last Destinations_ in order to display the last 3 Navigation Destinations.

Basically 3 Types of Information are registered as Properties

* Informations regarding your Dealer with Address and Phone Number
* Which services are available or not available
* Vehicle Properties like Color, Model Type, Drive Train and Construction Year

## Discovery

Auto Discovery is starting after you created the Bridge towards BMW ConnectedDrive. 
A list of your registered Vehicles is queried and all found Vehicles are added in Inbox.
As Unique Identifier the _Vehicle Identification Number_ (VIN) is used. 
If you already predefined a Thing in *.things configuration with the same VIN the Discovery won't discover it again. 
The Discovery still takes care updating the Properties of your Vehicle.

## Configuration

### Bridge

| Parameter       | Type    | Description                                                             |           
|-----------------|---------|-------------------------------------------------------------------------|
| userName        | text    | BMW Connected Drive Username                  |
| password        | text    | BMW Connected Drive Password                  |
| region          | text    | Select your Region in order to connect to the appropriate BMW Server.   |

The region Configuration has 3 different possibilities

* _NORTH_AMERICA_
* _CHINA_
* _ROW_ for Rest of World

### Things

All Things are needing the same Configuration Data

| Parameter       | Type    | Description                           |           
|-----------------|---------|---------------------------------------|
| vin             | text    | Vehicle Identification Number (VIN)   |
| refreshInterval | integer | Refresh Interval in Minutes           |
| units           | text    | Unit Selection                        |
| imageSize       | integer | Image Picture Size                    |
| imageViewport   | text    | Image Viewport                        |

The units can be configured in 3 ways

* _AUTODETECT_ selects Miles for US & UK, Kilometre otherwise
* _METRIC_ selects directly Kilometers
* _IMPERIAL_ selects directly Miles

The imageVieport allows you to show your Car from different angels.
Possible values are FRONT, REAR, SIDE, DASHBOARD or DRIVERDOOR

## Channels

There are many Channels available for each Vehicle. 
For better overview they are clustered in different Channel Groups.
The Channel Groups are different for the Car Types but also depends on the build in Sensors of your Car.
This means also the Construction Year is relevant if some Channels are supported or not.

### Bridge Channels

| Channel Label         | Channel ID            | Type   | Description                                       |
|-----------------------|-----------------------|--------|---------------------------------------------------|
| Door Status           | discovery-fingerprint | Switch | Forcing a log entry to analyze Discovery Problems |

### Thing Channel Groups 

#### Channel Group _Vehicle Status_

Available for all Vehicles.

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                                          |
|-----------------------|------------------|---------------------|--------|----------------------------------------------------------------------|
| Door Status           | status           | doors               | String | Closed if all closed, else reports the Door which is still open      |
| Window Status         | status           | windows             | String | Closed if all closed, else reports the Window which is still open    |
| Doors Locked          | status           | lock                | String | Status if Doors are locked or unlocked                               |
| Upcoming Service      | status           | service             | String | Upcoming Service either after certain Mileage, Date, or both         |
| Check Control         | status           | check-control       | String | Description of actual Check Control message, Ok if none is activated |


#### Channel Group _Range Data_

Based on Vehicle Type (Thing Type ID) some Channels are presented or not. 
For Conventional Fuel Cars the _Electric Range_ isn't presented while for Battery Electric Vehicles _Range Fuel_ isn't valid.
Hybrid Vehicles have both and in addition _Hybrid Range_

| Channel Label         | Channel Group ID | Channel ID          | Type                 | Valid for               | Description                                       |
|-----------------------|------------------|---------------------|----------------------|-------------------------|----------------------------------------|
| Mileage               | range            | mileage             | Number:Length        | All                     | Total Distance Driven                  |
| Hybrid Range          | range            | range-hybrid        | Number:Length        | PHEV BEV_REX            | Electric Range + Fuel Range            |
| Battery Range         | range            | range-electric      | Number:Length        | PHEV BEV_REX BEV        | Electric Range                         |
| Battery Charge Level  | range            | soc                 | Number:Dimensionless | PHEV BEV_REX BEV        | Battery State of Charge                |
| Fuel Range            | range            | range-fuel          | Number:Length        | CONV PHEV BEV_REX       | Fuel Range                             |
| Remaining Fuel        | range            | remaining-fuel      | Number:Volume        | CONV PHEV BEV_REX       | Remaining Fuel in Liters               |

#### Channel Group _Vehicle Location_

Available for all Vehicles.

| Channel Label  | Channel Group ID | Channel ID          | Type         | 
|----------------|------------------|---------------------|--------------|
| Latitude       | location         | latitude            | Number       | 
| Longitude      | location         | longitude           | Number       |
| Heading        | location         | heading             | Number:Angle | 

#### Channel Group _Last Trip_

Check in your Vehicle Thing Properties if _Statistics_ is present in _Services Supported_

| Channel Label               | Channel Group ID | Channel ID              | Type          | Description                                       |
|-----------------------------|------------------|-------------------------|---------------|---------------------------------------------------|
| Distance Driven             | last-trip        | distance                | Number:Length | Distance Driven on your Last Trip                 |
| Distance since Last Charge  | last-trip        | distance-since-charging | Number:Length | Distance Driven since Last Charge                 |
| Average Consumption         | last-trip        | average-consumption     | Number:Power  | Average Power Consumption on your Last Trip per 100 km |
| Average Recuperation        | last-trip        | average-recuperation    | Number:Power  | Average Power Recuperation on your Last Trip per 100 km |


#### Channel Group _Lifetime Statistics_

Check in your Vehicle Thing Properties if _Statistics_ is present in _Services Supported_

| Channel Label                      | Channel Group ID | Channel ID               | Type          | Description                                       |
|------------------------------------|------------------|--------------------------|---------------|---------------------------------------------------|
| Average Consumption                | lifetime        | average-consumption       | Number:Power  | Average Power Consumption on your Last Trip per 100 km |
| Distance since Last Charge         | lifetime        | average-recuperation      | Number:Power  | Average Power Recuperation on your Last Trip per 100 km |
| Cumulated Electric Driven Distance | lifetime        | cumulated-driven-distance | Number:Length | Total Distance Driven with Electric Power       |
| Longest Distance with one Charge   | lifetime        | single-longest-distance   | Number:Length | Longest Distance Driven with one single Charge      |


#### Channel Group _Remote Services_

Check in your Vehicle Thing Properties _Services Activated_ which Remote Services are available
Only one Remote Service can be executed each Time.
Parallel execution isn't supported.
The _Service Execution State_ Channel is reporting the state.
State _Executed_ is the final State when Execution is finished.

| Channel Label           | Channel Group ID | Channel ID          | Type    | 
|-------------------------|------------------|---------------------|---------|
| Activate Flash Lights   | remote           | light               | Switch  |
| Find Vehicle            | remote           | finder              | Switch  |
| Lock Doors              | remote           | lock                | Switch  |
| Unlock Doors            | remote           | unlock              | Switch  |
| Horn Blow               | remote           | horn                | Switch  |
| Active Air Conditioning | remote           | climate             | Switch  |
| Service Execution State | remote           | state               | STring  |

#### Channel Group _Destinations_

Check in your Vehicle Thing Properties if _LastDestinations_ is present in _Services Supported_

| Channel Label                        | Channel Group ID | Channel ID          | Type    | 
|--------------------------------------|------------------|---------------------|---------|
| Last Destination Name                | destination      | name-1              | String  |
| Last Destination Latitude            | destination      | lat-1               | Number  |
| Last Destination Longitude           | destination      | lon-1               | Number  |
| Second Last Destination Name         | destination      | name-2              | String  |
| Second Last Destination Latitude     | destination      | lat-2               | Number  |
| Second Last Destination Longitude    | destination      | lon-2               | Number  |
| Third Last Destination Name          | destination      | name-3              | String  |
| Third Last Destination Latitude      | destination      | lat-3               | Number  |
| Third Last Destination Longitude     | destination      | lon-3               | Number  |


#### Channel Group _Image_

Available for all Vehicles.

| Channel Label                 | Channel Group ID | Channel ID          | Type   | 
|-------------------------------|------------------|---------------------|--------|
| Rendered Image of your Vehicle| image            | png                 | Image  |

#### Channel Group _Troubleshooting_

Available for all Vehicles - really!
Please check [TroubleShooting Section](#TroubleShooting) for further advice

| Channel Label                       | Channel Group ID | Channel ID          | Type   | Description                                       |
|-------------------------------------|------------------|---------------------|--------|---------------------------------------------------|
| Log Vehicle Fingerprint             | troubleshoot     | vehicle-fingerprint | Switch | Forces log entries in openHAB logger in order to raise issues |

## TroubleShooting

As stated at the beginning: BMW has a high bandwidth of Vehicles supported by BMWs ConnectedDrive.
In case of any issues you face with this Binding please help to resolve it! 
Before raising an issue _Fingerprint_ data is needed in order to provide a proper analysis.
Two Fingerprints are available 

* Discovery - Your Vehicle isn't found automatically
* Vehicle - You have issue with the delivered Vehicle values

I've done my best to eliminate any private data from these Fingerprints.
Following data is replaced with ANONYMOUS data

* Dealer Properties
* Vehcile Identification Number (VIN)
* Location Latitude / Longitude is set to 0

After you've generated the corresponding Fingerprint please [follow the instructions to raise an Issue](https://community.openhab.org/t/how-to-file-an-issue/68464) and attach the Fingerprint data!
Your feedback is highly appreciated!

### Discovery

If your Vehicle isn't discovered correctly or you cannot find the correct Properties perform the following steps

* Check if your Vehicle is attached correctly in ConnectedDrive. Only if it's attached to your ConnectedDrive Account it's possible to detect it.
* In the below example Sitemap the Switch _Log Discovery Fingerprint_ can be executed. 

Copy the the log which can be found in general at [http://openhab:9001/](http://openhab:9001/) and raise an issue as described before.

### Vehicle Data

Maybe your Vehicle is discovered correctly and the Properties are correct but you've issue with the delivered Vehicle Values
### Update Timestamp

There's a timestamp showing the last update of your Vehicle. If this isn't shown correctly please check the date settings.
In case of Raspberry Pi execute _raspi-cinfig_, select _Localization Options_, the _Change Time Zone_
Select your _Geaographical Area_ and afterwards the correct City.
One restart of openHAB service with _systemctl restart openhab2_ is necessary in order to see the corrected Time Settings.

## Full Example

The example is based on a BMW i3 with Range Extender (REX). 
Exchange the 3 configuration parameters in the Things section

* YOUR_USERNAME - with your ConnectedDrive Login Username
* YOUR_PASSWORD - with your ConnectedDrive Password Credentials
* VEHICLE_VIN - the Vehicle Identification Number

and you're ready to go!

### Things

```
Bridge bmwconnecteddrive:account:user   "BMW ConnectedDrive Account" [userName="YOUR_USERNAME",password="YOUR_PASSWORD",region="ROW"] {
         Thing BEV_REX i3       "BMW i3 94h REX"                [ vin="VEHICLE_VIN",units="AUTODETECT",imageSize=2048,imageViewport="FRONT",refreshInterval=15]
}
```

### Items

```
Number:Length           i3Mileage                 "Odometer"                 <chart>    (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:range#mileage" }                                                                           
Number:Length           i3Range                   "Hybrid Range"             <motion>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:range#remaining-range-hybrid"}
Number:Length           i3RangeElectric           "Electric Range"           <motion>   (i3,long)   {channel="bmwconnecteddrive:BEV_REX:user:i3:range#remaining-range-electric"}   
Number:Length           i3RangeFuel               "Fuel Range"               <motion>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:range#remaining-range-fuel"}
Number:Dimensionless    i3BatterySoc              "Battery Charged"          <battery>  (i3,long)   {channel="bmwconnecteddrive:BEV_REX:user:i3:range#remaining-soc"}
Number:Dimensionless    i3Fuel                    "Fuel Charged"             <oil>      (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:range#remaining-fuel"}
String                  i3LastUpdate              "Updated"                  <calendar> (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:range#last-update"}

String                  i3DoorStatus              "Door Status"       <lock>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:status#doors" }
String                  i3WindowStatus            "Window Status"       <lock>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:status#windows" }
String                  i3LockStatus              "Locked Status"       <lock>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:status#lock" }
String                  i3ServiceStatus           "Service"       <calendar>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:status#service" }
String                  i3CheckControl            "Check Control"       <error>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:status#check-control" }
String                  i3ChargingStatus          "Charging"       <batterylevel>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:status#charging-status" }

Number:Length           i3TripDistance            "Distance"                 <chart>    (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#distance" }                                                                           
Number:Length           i3TripDistanceSinceCharge "Driven Since last Charge" <chart>    (i3,long)    {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#distance-since-charging" }                                                                           
Number:Energy           i3AvgTripConsumption      "Average Consumption"      <energy>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#average-consumption" }                                                                           
Number:Energy           i3AvgTripRecuperation     "Average Recuperation"     <energy>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:last-trip#average-recuperation" }                                                                           

Number:Length           i3CumulatedElectric       "Eletric Driven Distance"  <chart>    (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:lifetime#cumulated-driven-distance" }                                                                           
Number:Length           i3LongestEVTrip           "Longest Eletric Trip"     <chart>    (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:lifetime#single-longest-distance" }                                                                           
Number:Energy           i3AvgConsumption          "Average Consumption"      <energy>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:lifetime#average-consumption" }                                                                           
Number:Energy           i3AvgRecuperation         "Average Recuperation"     <energy>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:lifetime#average-recuperation" }  

Number                  i3Longitude               "Location Longitude"       <zoom>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:location#longitude" }                                                                           
Number                  i3Latitude                "Location Latitude"        <zoom>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:location#latitude" }                                                                           
String                  i3LatLong                 "Location as String"       <zoom>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:location#latlong" }
Number:Angle            i3Heading                 "Vehicle Heading"          <zoom>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:location#heading" }  
Number                  i3RangeRadius             "Range Radius"             <zoom>     (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:location#range-radius" }

Switch                  i3RemoteFlash             "Remote Flashlight"        <switch>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#light" } 
Switch                  i3RemoteFinder            "Remote Flashlight"        <switch>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#finder" } 
Switch                  i3RemoteLock              "Remote Flashlight"        <switch>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#lock" } 
Switch                  i3RemoteUnlock            "Remote Flashlight"        <switch>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#unlock" } 
Switch                  i3RemoteHorn              "Remote Flashlight"        <switch>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#horn" } 
Switch                  i3RemoteClimate           "Remote Flashlight"        <switch>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#climate" } 
String                  i3RemoteState             "Remote Flashlight"        <status>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:remote#state" } 

Image                   i3Image                   "Car Image"                <switch> (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:image#rendered" }  

Switch                  i3Troubleshoot            "Force Fingerprint Log"    <switch>   (i3)   {channel="bmwconnecteddrive:BEV_REX:user:i3:troubleshoot#cardata-fingerprint" }  
Switch                  i3DiscoveryFinger         "Discovery Fingerprint"    <switch>   (i3)   {channel="bmwconnecteddrive:account:user:discovery-fingerprint"}
 ```

### Sitemap

```
sitemap BMW label="BMW" {
  Frame label="BMW i3" {
    Image  item=i3Image                     label="Car Image"
  } 
  Frame label="Range" {
    Text    item=i3Mileage                  label="Odometer [%d %unit%]"
    Text    item=i3Range                    label="Range [%d %unit%]"
    Text    item=i3RangeElectric            label="Eletric Range [%d %unit%]"
    Text    item=i3RangeFuel                label="Fuel Range [%d %unit%]"
    Text    item=i3BatterySoc               label="Battery Charge [%.1f %%]"
    Text    item=i3Fuel                     label="Fuel Charge [%.1f %%]"
    Text    item=i3LastUpdate               label="Last Update [%s]"
  }
  Frame label="Status" {
    Text    item=i3DoorStatus               label="Door Status [%s]"
    Text    item=i3WindowStatus             label="Window Status [%s]"
    Text    item=i3LockStatus               label="Lock Status [%s]"
    Text    item=i3ServiceStatus            label="Service [%s]"
    Text    item=i3CheckControl             label="Check Control [%s]"
    Text    item=i3ChargingStatus           label="Charging [%s]"
  }
  Frame label="Remote Services" {
    Switch  item=i3RemoteFlash              label="Flash"
    Switch  item=i3RemoteFinder             label="Finder"
    Switch  item=i3RemoteLock               label="Lock"
    Switch  item=i3RemoteUnlock             label="Unlock"
    Switch  item=i3RemoteHorn               label="Horn"
    Switch  item=i3RemoteClimate            label="Air Conditioning"
    Text    item=i3RemoteState              label="Execution Status"
}
  Frame label="Last Trip" {
    Text    item=i3TripDistance             label="Distance [%d %unit%]"
    Text    item=i3TripDistanceSinceCharge  label="Distance since last Charge [%d %unit%]"
    Text    item=i3AvgTripConsumption       label="Average Consumption [%.1f %unit%]"
    Text    item=i3AvgTripRecuperation      label="Average Recuperation [%.1f %unit%]"
  }
  Frame label="Lifetime" {
    Text    item=i3CumulatedElectric        label="Eletric Distance Driven [%d %unit%]"
    Text    item=i3LongestEVTrip            label="Longest Trip [%d %unit%]"
    Text    item=i3AvgConsumption           label="Average Consumption [%.1f %unit%]"
    Text    item=i3AvgRecuperation          label="Average Recuperation [%.1f %unit%]"      
  }
  Frame label="Location" {
    Text    item=i3Latitude                 label="Latitude  [%.4f]"
    Text    item=i3Longitude                label="Longitude [%.4f]"
    Text    item=i3LatLong                  label="Location  [%s]"
    Text    item=i3Heading                  label="Heading [%.1f %unit%]" 
    Text    item=i3RangeRadius              label="Radius [%d]"   
  }
  Frame label="Troubleshooting" {    
    Switch  item=i3DiscoveryFinger          label="Log Discovery Fingerprint"
    Switch  item=i3Troubleshoot             label="Log CarData Fingerprint"
    
  } 
}
```

## Going further

You're now able to receive your Vehicle Data in openHAB. Continue the work and combine this data with other Powerful openHAB Bindings and Widgets.

### OpenstreetMap Widget

### OpenWeatherMap Binding and Widget

Especially Electric Vehicles which maybe are charged with your Local Photovoltaic System the Weather forecast and corresponding Cloudiness is interesting.
Use the OpenWeatherMap Binding and existing [Widget Solutions](https://community.openhab.org/t/openweathermap-widget-for-habpanel/65027) to check this data in addition to your Vehicles State of Charge.

## Credits

This work is based on the work of [Bimmer Connected](https://github.com/bimmerconnected/bimmer_connected). 
Also a [manual installation based on python](https://community.openhab.org/t/script-to-access-the-bmw-connecteddrive-portal-via-oh/37345) was already available for openHAB.
This Binding is basically a port to openHAB based on these concept works!  
