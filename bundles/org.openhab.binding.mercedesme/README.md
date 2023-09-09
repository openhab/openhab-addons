# MercedesMe Binding

**
ALPHA VERSION!
Please connect the advanced channels form Vehicle to analyze problems!

- feature-capabilities
- command-capabilities
- proto-update

**

This binding provides similar access to your Mercedes Benz vehicle like the Smartphone App _Mercedes Me_.
For this you need a Mercedes developer account to get data from your vehicles.
Setup requires some, time so follow [the steps of bridge configuration](#bridge-configuration).

If you face some problems during setup or runtime please have a look into the [Troubleshooting section](#troubleshooting)

## Supported Things

| Type            | ID            | Description                                     |
|-----------------|---------------|-------------------------------------------------|
| Bridge          | `account`     | Connect your Mercedes Me account                |
| Thing           | `combustion`  | Conventional fuel vehicle                       |
| Thing           | `hybrid`      | Fuel vehicle with supporting electric engine    |
| Thing           | `bev`         | Battery electric vehicle                        |

## Bridge Configuration

Bridge needs configuration in order to connect properly to your Mercedes Me Account.


### Bridge Setup

Authorization is needed to activate the Bridge which is connected to your MercedesMe Account.
The Bridge will indicate in the Status headline if Authorization is needed including the URL which needs to be opened in your browser.
Three steps are needed

1. Open the mentioned URL like 192.168.x.x:8090/mb-auth 
Opening this URL will request a PIN to your configured EMail
Check your Mail Account if you received the PIN.
Click on Continue with Step 2 

2. Enter your PIN in the shown field
Leave GUID as identifier as it is
Click on Submit button

3. Consfirmation shall be shown that Authorization was successful.

In case of non successful Athorization check your log for errors. 

### MB Credentials

<img src="./doc/MBDeveloper-Credentials.png" width="500" height="280"/>

### MB Product Subscriptions

<img src="./doc/MBDeveloper-Subscriptions.png" width="500" height="300"/>

### openHAB Configuration

<img src="./doc/MercedesMeConfiguration.png" width="400" height="500"/>

### MB Access Request

<img src="./doc/MBAccessRequest.png" width="500" height="220"/>

### Callback page

<img src="./doc/CallbackUrl_Page.png" width="500" height="350"/>

### Bridge Configuration Parameters

| Name            | Type    | Description                           | Default     | Required | Advanced |
|-----------------|---------|---------------------------------------|-------------|----------|----------|
| email           | text    | Mercedes Benz Developer Client ID     | N/A         | yes      | no       |
| region          | text    | Mercedes Benz Developer Client Secret | EU          | yes      | no       |
| pin             | text    | Mercedes Benz Developer Image API Key | N/A         | no       | no       |

Set `region` to your location

- `EU` Europe and Rest of World
- `NA` North America
- `AP` Aisa Pacific
- `CN` China 

Set `pin` to your selected PIN of your Apple or Android installed MercedesMe App.
Parameter is *not required*.
Note `pin` is needed for some commands which are critical for Car and especially **personal saftey**.
E.g. closing windows needs to ensure no obstacles are in the way!
Commands protected by PIN

- Remote Starting Vehicle
- Unlock Doors
- Open / Ventilate Windows
- Open / Lift Sunroof


## Thing Configuration

For vehicle images Mercedes Benz Developer offers only a trial version with limited calls.
Check in **beforehand** if your vehicle has some restrictions or even if it's supported at all.
Visit [Vehicle Image Details](https://developer.mercedes-benz.com/products/vehicle_images/details) in order to check your vehicle capabilities.
Visit [Image Settings](https://developer.mercedes-benz.com/products/vehicle_images/docs#_default_image_settings) to get more information about
For example the EQA doesn't provide `night` images with `background`.
If your configuration is set this way the API calls are wasted!

<img src="./doc/ImageRestrictions.png" width="800" height="36"/>

See also [image channel section](#image) for further advise.

| Name            | Type    | Description                                         | Default | Required | Advanced |
|-----------------|---------|-----------------------------------------------------|---------|----------|----------|
| vin             | text    | Vehicle identification number                       | N/A     | yes      | no       |

For all vehicles you're free to give the tank / battery capacity.
Giving these values in configuration the open fuel / charge capacities are reported in the [range](#range) channels.

| Name            | Type    | Description                                         | Default | Required | Advanced | combustion | bev | hybrid |
|-----------------|---------|-----------------------------------------------------|---------|----------|----------|------------|-----|--------|
| batteryCapacity | decimal | Battery Capacity                                    | N/A     | no       | no       |            | X   | X      |
| fuelCapacity    | decimal | Fuel Capacity                                       | N/A     | no       | no       | X          |     | X      |

## Channels

Channels are separated in groups:

| Channel Group ID                 | Description                                       |
|----------------------------------|---------------------------------------------------|
| [vehicle](#vehicle)              | Vehicle Information                               |
| [doors](#doors)                  | Details of all doors                              |
| [lock](#lock)                    | Doors lock status                                 |
| [windows](#windows)              | Window Details                                    |
| [hvac](#hvac)                    | Climatization                                     |
| [service](#service)              | Service & Warnings                                |
| [range](#range)                  | Ranges and Odometer                               |
| [charge](#charge)                | Charging data and programs                        |
| [trip](#trip)                    | Trip data                                         |
| [position](#position)            | Positioning Data                                  |
| [tires](#tires)                  | Tire Informatios                                  |

### Vehicle

Group name: `vehicle`

| Channel               | Type                 |  Description                 | Read | Write | Advanced |
|-----------------------|----------------------|------------------------------|------|-------|----------|
| lock-status           | Number              |  Lock Status                  | X    |       |          |
| window-status         | Number              |  Window Status                | X    |       |          |
| door-status           | Number              |  Door Status                  | X    |       |          |
| ignition              | Number              |  Ignition                     | X    |       |          |
| feature-capabilities  | String              |  Feature Capabilities         | X    |       |    X     |
| command-capabilities  | String              |  Command Capabilities         | X    |       |    X     |
| proto-update          | String              |  Last Vehicle Data Update     | X    |       |    X     |

#### Lock Status Mapping

- 0 : Locked
- 1 : Unlocked

#### Window Status Mapping

- 0 : Intermediate
- 1 : Closed
- 2 : Open

#### Door Status Mapping

- 1 : Closed

#### Ignition Mapping

- 0 : Off
- 2 : Ready
- 4 : On

### Doors

Group name: `doors`

State representing if Door or other roofs, hoods or flaps are open.
States and Controls are depending on your vehicle capabilites.

| Channel             | Type                 |  Description                 | Read | Write |
|---------------------|----------------------|------------------------------|------|-------|
| front-left          | Contact              |  Front Left Door             | X    |       |
| front-right         | Contact              |  Fornt Right Door            | X    |       |
| rear-left           | Contact              |  Rear Left Door              | X    |       |
| rear-right          | Contact              |  Rear Right Door             | X    |       |
| deck-lid            | Contact              |  Deck lid                    | X    |       |
| engine-hood         | Contact              |  Engine Hood                 | X    |       |
| sunroof             | Number               |  Sun roof (only Cabriolet)   | X    |       |
| rooftop             | Number               |  Roof top                    | X    |       |
| sunroof-front-blind | Number               |  Sunroof Front Blind         | X    |       |
| sunroof-rear-blind  | Number               |  Sunroof Rear Blind          | X    |       |
| sunroof-control     | Number               |  Sunroof Control             |      | X     |

#### Sunroof Mapping

- 0 : Closed
- 1 : Open
- 2 : Open Lifting
- 3 : Running
- 4 : Closing
- 5 : Opening
- 6 : Closing

#### Rooftop Mapping
            
- 0 : Unlocked
- 1 : Open and locked
- 2 : Closed and locked

#### Sunroof Front Blind Mapping

- not available yet!

#### Sunroof Rear Blind Mapping

- not available yet!

#### Sunroof Control Mapping

- 0 : Close
- 1 : Open
- 2 : Lift

### Lock

Group name: `lock`
State representing if Door or other roofs, hoods or flaps are locked.
States and Controls are depending on your vehicle capabilites and Type.

| Channel             | Type                 |  Description                    | Read | Write |
|---------------------|----------------------|---------------------------------|------|-------|
| front-left          | Switch              |  Front Left Door Lock            | X    |       |
| front-right         | Switch              |  Front Right Door Lock           | X    |       |
| rear-left           | Switch              |  Rear Left Door Lock             | X    |       |
| rear-right          | Switch              |  Rear Right Door Lock            | X    |       |
| deck-lid            | Switch              |  Deck lid                        | X    |       |
| gas-flap            | Switch              |  Gas Flap (combustion & hybrid)  | X    |       |
| lock-control        | Switch              |  Lock / Unlock Verhicle          |      | X     |


### Windows

Group name: `windows`
State representing current Window position.
States and Controls are depending on your vehicle capabilites.

| Channel             | Type                 |  Description                 | Read | Write |
|---------------------|----------------------|------------------------------|------|-------|
| front-left          | Number               |  Front Left Window           | X    |       |
| front-right         | Number               |  Fornt Right Window          | X    |       |
| rear-left           | Number               |  Rear Left Window            | X    |       |
| rear-right          | Number               |  Rear Right Window           | X    |       |
| rear-right-blind    | Number               |  Rear Right Blind            | X    |       |
| rear-left-blind     | Number               |  Rear Left Blind             | X    |       |
| rear-blind          | Number               |  Rear  Blind                 | X    |       |
| window-control      | Number               |  Window Control              |      | X     |

#### Window Channel Mapping

- 0 : Intermediate
- 1 : Open
- 2 : Closed
- 3 : Airing
- 4 : Intermediate
- 5 : Running

#### Rear Right Blind Channel Mapping

- not available yet!
 
#### Rear Left Blind Channel Mapping

- not available yet!
 
#### Rear Blind Channel Mapping

- not available yet!

#### Flip Window Channel Mapping

- not available yet!

#### Window Control Channel Mapping
- 0 : Close
- 1 : Open
- 2 : Ventilate


### HVAC

Group name: `havc`
Configuration of vehicle climatization.
States and Controls are depending on your vehicle capabilites.

| Channel             | Type                 |  Description                    | Read | Write |
|---------------------|----------------------|---------------------------------|------|-------|
| front-left          | Switch              |  Front Left Seat Climatization   | X    | X     |
| front-right         | Switch              |  Front Left Seat Climatization   | X    | X     |
| rear-left           | Switch              |  Front Left Seat Climatization   | X    | X     |
| rear-right          | Switch              |  Front Left Seat Climatization   | X    | X     |
| zone                | Number              |  Selected Climatization Zone     | X    | X     |
| temperature         | Number:Temperature  |  Desired Temperature             | X    | X     |
| activate            | Switch              |  Gas Flap (combustion & hybrid)  | X    | X     |
| aux-heat            | Switch              |  Sunroof Control (Cabriolet)     | X    | X     |

#### Zone Mapping

Automatically calculated based on your vehicle capabilities

#### Temperautre Setting

Preconfigure selected zone with desired temperature

- Minimum : 16
- Maximum : 28

### Service

Group name: `service`
All channles read-only.
Service and Warning Information for vehicle
States and Controls are depending on your vehicle capabilites.

| Channel             | Type                 |  Description                    | bev | hybrid | combustion |
|---------------------|----------------------|---------------------------------|-----|--------|------------|
| starter-battery     | Number               |  Starter Battery Status         | X   | X      | X          |
| brake-fluid         | Switch               |  Brake Fluid Warning            | X   | X      | X          |
| brake-lining-wear   | Switch               |  Brake Lining Gear Warning      | X   | X      | X          |
| wash-water          | Switch               |  Wash Water Low Warning         | X   | X      | X          |
| coolant-fluid       | Switch               |  Coolant Fluid Low Warning      |     | X      | X          |
| engine              | Switch               |  Engine Warning                 |     | X      | X          |
| tires-rdk           | Number               |  Tire Pressure Warnings         | X   | X      | X          |
| service-days        | Number               |  Next Service in *x* days       | X   | X      | X          |


#### Starter Battery Mapping

-0 : Charged

### Range

Group name: `range`
All channles read-only.

| Channel          | Type                 |  Description                 | bev | hybrid | combustion |
|------------------|----------------------|------------------------------|-----|--------|------------|
| mileage          | Number:Length        |  Total mileage               | X   | X      | X          |
| soc              | Number:Dimensionless |  Battery state of charge     | X   | X      |            |
| charged          | Number:Energy        |  Charged Battery Energy      | X   | X      |            |
| uncharged        | Number:Energy        |  Uncharged Battery Energy    | X   | X      |            |
| soc              | Number:Dimensionless |  Battery state of charge     | X   | X      |            |
| range-electric   | Number:Length        |  Electric range              | X   | X      |            |
| radius-electric  | Number:Length        |  Electric radius for map     | X   | X      |            |
| fuel-level       | Number:Dimensionless |  Fuel level in percent       |     | X      | X          |
| fuel-remain      | Number:Volume        |  Reamaining Fuel             |     | X      | X          |
| fuel-open        | Number:Volume        |  Open Fuel Capacity          |     | X      | X          |
| range-fuel       | Number:Length        |  Fuel range                  |     | X      | X          |
| radius-fuel      | Number:Length        |  Fuel radius for map         |     | X      | X          |
| range-hybrid     | Number:Length        |  Hybrid range                |     | X      |            |
| radius-hybrid    | Number:Length        |  Hybrid radius for map       |     | X      |            |

Channels with `radius` are just giving a _guess_ which radius can be reached in a map display.


### Charge

Group name: `charge`
Only relevant for battery electric and hybrid vehicles.
Current charge values and charge program configuration.
States and Controls are depending on your vehicle capabilites.

| Channel             | Type                 |  Description                    | Read | Write |
|---------------------|----------------------|---------------------------------|------|-------|
| charge-flap         | Number               |  Charge Flap Stazus             | X    |       |
| oupler-ac-channel   | Number               |  Coupler AC Status              | X    |       |
| oupler-dc-channel   | Number               |  Coupler DC Status              | X    |       |
| coupler-lock        | Number               |  Coupler Lock Status            | X    |       |
| active              | Switch               |  Charging Active                | X    |       |
| power               | Number:Power         |  Current Charging Power         | X    |       |
| end-time            | DateTime             |  Estimated Charging End         | X    |       |
| program             | Number               |  Selected Charge Program        | X    | X     |
| max-soc             | Number:Dimensioless  |  Charge Target SoC              | X    | X     |
| auto-unlock         | Switch               |  Sunroof Control (Cabriolet)    | X    | X     |

#### Charge Flap Mapping

- 0 : Open
- 1 : Closed

#### Coupler AC Mapping

- not available yet!

#### Coupler DC Mapping

- not available yet!

#### Coupler Lock Mapping

- 0 : Locked
- 1 : Unlocked

#### Program Mapping

Calculated automatically based on your vehicle capabilities

#### Max SoC Setting

SoC target for selected rogram can be configured if your vehicle capabilities are supporting it.
Configuration Limit needs to respect 10% steps with a minimum of 50% and maximum of 100%.

#### Auto Unlock Setting

Charge Program can be configured to release Coupler Lock after target SoC is reached


### Trip

Group name: `trip`
All channels `read-only`

| Channel          | Type                 |  Description                                                         |
|------------------|----------------------|----------------------------------------------------------------------|
| distance         | Number Length        |  Last Trip Distance                                                  |
| time             | String               |  Last Trip Duration in days, hours and minutes                       |
| avg-speed        | Number               |  Last Trip Average Speed in km/h                                     |
| cons-ev          | Number:Energy        |  Last Trip Average Electric Energy Consumption per 100 Kilometre     |
| cons-conv        | Number:Volume        |  Last Trip Average Gas Consumption per 100 Kilometre                 |
| distance-reset   | Number Length        |  Since Reset Trip Distance                                           |
| time-reset       | String               |  Since Reset Duration in days, hours and minutes                     |
| avg-speed-reset  | Number               |  Since Reset Average Speed in km/h                                   |
| cons-ev-reset    | Number:Energy        |  Since Reset Average Electric Energy Consumption per 100 Kilometre   |
| cons-conv-reset  | Number:Volume        |  Since Reset Average Gas Consumption per 100 Kilometre               |

    <channel-type id="distance-reset-channel">
        <item-type>Number:Length</item-type>
        <label>Distance Reset</label>
        <state pattern="%.1f %unit%" readOnly="true"/>
    </channel-type>
    <channel-type id="driven-time-reset-channel">
        <item-type>String</item-type>
        <label>Driving Time Reset</label>
        <state readOnly="true"/>
    </channel-type>
    <channel-type id="avg-speed-reset-channel">
        <item-type>Number</item-type>
        <label>Average Speed Reset</label>
        <state pattern="%.1f km/h" readOnly="true"/>
    </channel-type>
    <channel-type id="consumption-ev-reset-channel">
        <item-type>Number:Energy</item-type>
        <label>Average Consumption Reset</label>
        <state pattern="%.1f %unit%" readOnly="true"/>
    </channel-type>
    <channel-type id="consumption-conv-reset-channel">
        <item-type>Number:Volume</item-type>
        <label>Average Consumption Reset</label>
        <state pattern="%.1f %unit%" readOnly="true"/>
    </channel-type>
### Position

Group name: `position`

| Channel          | Type                 |  Description                 |
|------------------|----------------------|------------------------------|
| heading          | Number:Angle         |  Vehicle heading             |
| last-update      | DateTime             |  Last location update        |

### Tires

Provides exterior and interior images for your specific vehicle.
Group name: `image`

| Channel          | Type                 |  Description                 | Write |
|------------------|----------------------|------------------------------|-------|
| image-data       | Raw                  |  Vehicle image               |       |
| image-view       | text                 |  Vehicle image viewpoint     |   X   |
| clear-cache      | Switch               |  Remove all stored images    |   X   |

**If** the `imageApiKey` in [Bridge Configuration Parameters](#bridge-configuration-parameters) is set the vehicle thing will try to get images.
Pay attention to the [Advanced Image Configuration Properties](#thing-configuration) before requesting new images.
Sending commands towards the `image-view` channel will change the image.
The `image-view` is providing options to select the available images for your specific vehicle.
Images are stored in `jsondb` so if you requested all images the Mercedes Benz Image API will not be called anymore which is good because you have a restricted amount of calls!
If you're not satisfied e.g. you want a background you need to

1. change the [Advanced Image Configuration Properties](#thing-configuration)
1. Switch `clear-cache` channel item to `ON` to clear all images
1. request them via `image-view`

## Troubleshooting

### Authorization fails

The configuration of openHAB account thing and the Mercedes Developer project need an extract match regarding

- MB project credentials vs. `clientId` `clientSecret` and `callbackUrl`
- MB project subscription of products vs. `scope`

If you follow the [bridge configuration steps](#bridge-configuration) both will match.
Otherwise you'll receive some error message when clicking the link after opening the `callbackUrl` in your browser

Most common errors:

- redirect URL doesn't match: Double check if `callbackUrl` is really saved correctly in your Mercedes Benz Developer project
- scope failure: the requested scope doesn't match with the subscribed products.
  - Check [openHab configuration switches](#openhab-configuration)
  - apply changes if necessary and don't forget to save
  - after these steps refresh the `callbackUrl` in [your browser](#callback-page) to apply these changes
  - try a new authorization clicking the link

### Receive no data

Especially after setting the frist Mercedes Benz Developer Project you'll receive no data.
It seems that the API isn't _filled_ yet.

#### Pre-Condition

- The Mercedes Me bridge is online = authorization is fine
- The Mercedes Me thing is online = API calls are fine

#### Solution

- Reduce `refreshInterval` to 1 minute
- Go to your vehicle, open doors and windows, turn on lights, drive a bit  ...
- wait until values are providing the right states

## Full example

The example is based on a battery electric vehicle.
Exchange configuration parameters in the Things section

Bridge

- 4711 - your desired bridge id
- YOUR_CLIENT_ID - Client ID of the Mercedes Developer project
- YOUR_CLIENT_SECRET - Client Secret of the Mercedes Developer project
- YOUR_API_KEY - Image API Key of the Mercedes Developer project
- YOUR_OPENHAB_SERVER_IP - IP address of your openHAB server
- 8090 - a **unique** port number - each bridge in your openHAB installation needs to have different port number!

Thing

- eqa - your desired vehicle thing id
- VEHICLE_VIN - your Vehicle Identification Number

### Things file

```java
Bridge mercedesme:account:4711   "MercedesMe John Doe" [ clientId="YOUR_CLIENT_ID", clientSecret="YOUR_CLIENT_SECRET", imageApiKey="YOUR_API_KEY", callbackIP="YOUR_OPENHAB_SERVER_IP", callbackPort=8092, odoScope=true, vehicleScope=true, lockScope=true, fuelScope=true, evScope=true] {
         Thing bev eqa           "Mercedes EQA"        [ vin="VEHICLE_VIN", refreshInterval=5, background=false, night=false, cropped=false, roofOpen=false, format="webp"]
}
```

### Items file

```java
Number:Length           EQA_Mileage                 "Odometer [%d %unit%]"                        {channel="mercedesme:bev:4711:eqa:range#mileage" }                                                                           
Number:Length           EQA_Range                   "Range [%d %unit%]"                           {channel="mercedesme:bev:4711:eqa:range#range-electric"}
Number:Length           EQA_RangeRadius             "Range Radius [%d %unit%]"                    {channel="mercedesme:bev:4711:eqa:range#radius-electric"}   
Number:Dimensionless    EQA_BatterySoc              "Battery Charge [%.1f %%]"                    {channel="mercedesme:bev:4711:eqa:range#soc"}

Contact                 EQA_DriverDoor              "Driver Door [%s]"                            {channel="mercedesme:bev:4711:eqa:doors#driver-front" }
Contact                 EQA_DriverDoorRear          "Driver Door Rear [%s]"                       {channel="mercedesme:bev:4711:eqa:doors#driver-rear" }
Contact                 EQA_PassengerDoor           "Passenger Door [%s]"                         {channel="mercedesme:bev:4711:eqa:doors#passenger-front" }
Contact                 EQA_PassengerDoorRear       "Passenger Door Rear [%s]"                    {channel="mercedesme:bev:4711:eqa:doors#passenger-rear" }
Number                  EQA_Trunk                   "Trunk [%s]"                                  {channel="mercedesme:bev:4711:eqa:doors#deck-lid" }
Number                  EQA_Rooftop                 "Rooftop [%s]"                                {channel="mercedesme:bev:4711:eqa:doors#rooftop" }
Number                  EQA_Sunroof                 "Sunroof [%s]"                                {channel="mercedesme:bev:4711:eqa:doors#sunroof" }

Number                  EQA_DoorLock                "Door Lock [%s]"                              {channel="mercedesme:bev:4711:eqa:lock#doors" }
Switch                  EQA_TrunkLock               "Trunk Lock [%s]"                             {channel="mercedesme:bev:4711:eqa:lock#deck-lid" }
Switch                  EQA_FlapLock                "Charge Flap Lock [%s]"                       {channel="mercedesme:bev:4711:eqa:lock#flap" }

Number                  EQA_DriverWindow            "Driver Window [%s]"                          {channel="mercedesme:bev:4711:eqa:windows#driver-front" }
Number                  EQA_DriverWindowRear        "Driver Window Rear [%s]"                     {channel="mercedesme:bev:4711:eqa:windows#driver-rear" }
Number                  EQA_PassengerWindow         "Passenger Window [%s]"                       {channel="mercedesme:bev:4711:eqa:windows#passenger-front" }
Number                  EQA_PassengerWindowRear     "Passenger Window Rear [%s]"                  {channel="mercedesme:bev:4711:eqa:windows#passenger-rear" }

Number:Angle            EQA_Heading                 "Heading [%.1f %unit%]"                       {channel="mercedesme:bev:4711:eqa:location#heading" }  

Image                   EQA_Image                   "Image"                                       {channel="mercedesme:bev:4711:eqa:image#image-data" }  
String                  EQA_ImageViewport           "Image Viewport [%s]"                         {channel="mercedesme:bev:4711:eqa:image#image-view" }  
Switch                  EQA_ClearCache              "Clear Cache [%s]"                            {channel="mercedesme:bev:4711:eqa:image#clear-cache" }  

Switch                  EQA_InteriorFront           "Interior Front Light [%s]"                   {channel="mercedesme:bev:4711:eqa:lights#interior-front" }  
Switch                  EQA_InteriorRear            "Interior Rear Light [%s]"                    {channel="mercedesme:bev:4711:eqa:lights#interior-rear" }  
Switch                  EQA_ReadingLeft             "Reading Light Left [%s]"                     {channel="mercedesme:bev:4711:eqa:lights#reading-left" }  
Switch                  EQA_ReadingRight            "Reading Light Right [%s]"                    {channel="mercedesme:bev:4711:eqa:lights#reading-right" }  
Number                  EQA_LightSwitch             "Main Light Switch [%s]"                      {channel="mercedesme:bev:4711:eqa:lights#light-switch" }  
```

### Sitemap

```perl
sitemap MB label="Mercedes Benz EQA" {
  Frame label="EQA Image" {
    Image  item=EQA_Image  
                       
  } 
  Frame label="Range" {
    Text    item=EQA_Mileage           
    Text    item=EQA_Range             
    Text    item=EQA_RangeRadius     
    Text    item=EQA_BatterySoc        
  }

  Frame label="Door Details" {
    Text      item=EQA_DriverDoor 
    Text      item=EQA_DriverDoorRear   
    Text      item=EQA_PassengerDoor 
    Text      item=EQA_PassengerDoorRear 
    Text      item=EQA_Trunk
    Text      item=EQA_Rooftop
    Text      item=EQA_Sunroof    
    Text      item=EQA_DoorLock
    Text      item=EQA_TrunkLock
    Text      item=EQA_FlapLock
  }

  Frame label="Windows" {
    Text     item=EQA_DriverWindow
    Text     item=EQA_DriverWindowRear 
    Text     item=EQA_PassengerWindow
    Text     item=EQA_PassengerWindowRear
  }
  
  Frame label="Location" {
    Text    item=EQA_Heading             
  }

  Frame label="Lights" {
    Text       item=EQA_InteriorFront
    Text       item=EQA_InteriorRear
    Text       item=EQA_ReadingLeft
    Text       item=EQA_ReadingRight
    Text       item=EQA_LightSwitch
  } 

  Frame label="Image Properties" {
    Selection    item=EQA_ImageViewport
    Switch       item=EQA_ClearCache
  } 
}
```

## Mercedes Benz Developer

Visit [Mercedes Benz Developer](https://developer.mercedes-benz.com/) to gain more deep information.
