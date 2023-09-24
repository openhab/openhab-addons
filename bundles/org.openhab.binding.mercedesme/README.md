# MercedesMe Binding

This binding provides similar access to your Mercedes Benz vehicle like the Smartphone App _Mercedes Me_.


## ''Alpha Version''

Current Development Status is Alpha.
Data Mappings are missing and testing of features and regions are necessary.

In order to analyze problems check the binding logs.
**In addition please connect the advanced channels from vehicle [vehicle](#vehicle) to analyze problems!**

- feature-capabilities
- command-capabilities
- proto-update


## Supported Things

| Type            | ID            | Description                                     |
|-----------------|---------------|-------------------------------------------------|
| Bridge          | `account`     | Connect your Mercedes Me account                |
| Thing           | `combustion`  | Conventional fuel vehicle                       |
| Thing           | `hybrid`      | Fuel vehicle with supporting electric engine    |
| Thing           | `bev`         | Battery electric vehicle                        |

## Discovery

The MercedesMe binding is based on the API of the Smartphone App.
You have an account which connects to one or more vehicles.
Setup the MercedesMe Account Bridge with your EMail address used in Smartphone App.
After successful authorization your attached vehicles are found automatically.
Manual Discovery not necessary!

## Bridge Configuration

Bridge needs configuration in order to connect properly to your Mercedes Me Account.
You need to have access to your Mercedes Benz via Smartphone App.
Otherwise this binding will not work!


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

3. Confirmation shall be shown that Authorization was successful.

In case of non successful Authorization check your log for errors. 

Some Screenshots to follow Authorization:

### After Bridge Setup

<img src="./doc/OH-Step0.png" width="500" height="240"/>

### Authorization Step 1

<img src="./doc/OH-Step1.png" width="500" height="200"/>

### Authorization Step 2

<img src="./doc/OH-Step2.png" width="500" height="200"/>

### Authorization Step 3

<img src="./doc/OH-Step3.png" width="400" height="130"/>

### Vehicle Capabilities

<img src="./doc/OH-capabilities.png" width="500" height="280"/>


### Bridge Configuration Parameters

| Name            | Type    | Description                             | Default     | Required | Advanced |
|-----------------|---------|-----------------------------------------|-------------|----------|----------|
| email           | text    | Mercedes Benz registered EMail Address  | N/A         | yes      | no       |
| pin             | text    | Mercedes Benz Smartphone App PIN        | N/A         | no       | no       |
| region          | text    | Your region                             | EU          | yes      | no       |
| refreshInterval | integer | API Polling Interval                    | 15          | yes      | no       |

Set `region` to your location

- `EU` Europe and Rest of World
- `NA` North America
- `AP` Aisa Pacific
- `CN` China 

Set `pin` to your selected PIN of your Apple or Android installed MercedesMe App.
Parameter is *not required*.
Note `pin` is needed for some commands which are critical for Car and especially **personal safety**.
E.g. closing windows needs to ensure no obstacles are in the way!
Commands protected by PIN

- Remote Starting Vehicle
- Unlock Doors
- Open / Ventilate Windows
- Open / Lift Sunroof


## Thing Configuration

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
| [tires](#tires)                  | Tire Informations                                 |

## Actions

See [Vehicle Actions](#vehicle-actions) which can be used in rules.


### Vehicle

Group name: `vehicle`

| Channel               | Type                |  Description                  | Read | Write | Advanced |
|-----------------------|---------------------|-------------------------------|------|-------|----------|
| lock                  | Number              |  Lock Status and Control      | X    | X     |          |
| windows               | Number              |  Window Status and Control    | X    | X     |          |
| door-status           | Number              |  Door Status                  | X    |       |          |
| ignition              | Number              |  Ignition                     | X    | X     |          |
| feature-capabilities  | String              |  Feature Capabilities         | X    |       |    X     |
| command-capabilities  | String              |  Command Capabilities         | X    |       |    X     |
| proto-update          | String              |  Last Vehicle Data Update     | X    |       |    X     |

Advanced channels are only for debugging. 
If you encounter problems with this binding follow the instructions from [Troubleshooting](#troubleshooting) section.

#### Lock Status Mapping

State 

- 0 : Locked
- 1 : Unlocked

Command 

- 0 : Lock
- 1 : Unlock

#### Window Mappings

State

- 0 : Intermediate
- 1 : Closed
- 2 : Open

Command

- 0 : Ventilate 
- 1 : Close
- 2 : Open

Note: State mapping doesn't correspond to command mapping!
That's the mapping of Mercedes SDK.
So if your windows are in state `0 : Intermediate` you need to send command `0 : Close` to close them!

#### Door Status Mapping

- 0 : Open
- 1 : Closed

#### Ignition Mapping

State 

- 0 : Off
- 2 : Ready
- 4 : On

Command

- 0 : Off
- 4 : On

### Doors

Group name: `doors`

State representing if Door or other roofs, hoods or flaps are open.
States and Controls are depending on your vehicle capabilities.

| Channel             | Type                 |  Description                 | Read | Write |
|---------------------|----------------------|------------------------------|------|-------|
| front-left          | Contact              |  Front Left Door             | X    |       |
| front-right         | Contact              |  Fornt Right Door            | X    |       |
| rear-left           | Contact              |  Rear Left Door              | X    |       |
| rear-right          | Contact              |  Rear Right Door             | X    |       |
| deck-lid            | Contact              |  Deck lid                    | X    |       |
| engine-hood         | Contact              |  Engine Hood                 | X    |       |
| rooftop             | Number               |  Roof top                    | X    |       |
| sunroof-front-blind | Number               |  Sunroof Front Blind         | X    |       |
| sunroof-rear-blind  | Number               |  Sunroof Rear Blind          | X    |       |
| sunroof             | Number               |  Sun roof                    | X    | X     |


#### Rooftop Mapping
            
- 0 : Unlocked
- 1 : Open and locked
- 2 : Closed and locked

#### Sunroof Front Blind Mapping

- not available yet!

#### Sunroof Rear Blind Mapping

- not available yet!

#### Sunroof Mapping

State

- 0 : Closed
- 1 : Open
- 2 : Lifted
- 3 : Running
- 4 : Closing
- 5 : Opening
- 6 : Closing

Command

- 0 : Close
- 1 : Open
- 2 : Lift

### Lock

Group name: `lock`
State representing if doors, hoods or flaps are locked.
States and Controls are depending on your vehicle capabilities and Type.

| Channel             | Type                 |  Description                    | Read | Write |
|---------------------|----------------------|---------------------------------|------|-------|
| front-left          | Switch              |  Front Left Door Lock            | X    |       |
| front-right         | Switch              |  Front Right Door Lock           | X    |       |
| rear-left           | Switch              |  Rear Left Door Lock             | X    |       |
| rear-right          | Switch              |  Rear Right Door Lock            | X    |       |
| deck-lid            | Switch              |  Deck lid                        | X    |       |
| gas-flap            | Switch              |  Gas Flap (combustion & hybrid)  | X    |       |


### Windows

Group name: `windows`
State representing current Window position.

| Channel             | Type                 |  Description                 | Read | Write |
|---------------------|----------------------|------------------------------|------|-------|
| front-left          | Number               |  Front Left Window           | X    |       |
| front-right         | Number               |  Fornt Right Window          | X    |       |
| rear-left           | Number               |  Rear Left Window            | X    |       |
| rear-right          | Number               |  Rear Right Window           | X    |       |
| rear-right-blind    | Number               |  Rear Right Blind            | X    |       |
| rear-left-blind     | Number               |  Rear Left Blind             | X    |       |
| rear-blind          | Number               |  Rear  Blind                 | X    |       |

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


### HVAC

Group name: `havc`
Configuration of vehicle climatization.
States and Controls are depending on your vehicle capabilities.

| Channel             | Type                 |  Description                    | Read | Write |
|---------------------|----------------------|---------------------------------|------|-------|
| front-left          | Switch              |  Front Left Seat Climatization   | X    | X     |
| front-right         | Switch              |  Front Left Seat Climatization   | X    | X     |
| rear-left           | Switch              |  Front Left Seat Climatization   | X    | X     |
| rear-right          | Switch              |  Front Left Seat Climatization   | X    | X     |
| zone                | Number              |  Selected Climatization Zone     | X    | X     |
| temperature         | Number:Temperature  |  Desired Temperature             | X    | X     |
| activate            | Switch              |  Gas Flap (combustion & hybrid)  | X    | X     |
| aux-heat            | Switch              |  Auxiliary Heating               | X    | X     |

#### Zone Mapping

Automatically calculated based on your vehicle capabilities

#### Temperature Setting

Preconfigure selected zone with desired temperature
Minimum and Maximum Temperature depends on your local settings either Degrre Clesius or Fahrenheit.

Celsius 

- Minimum : 16
- Maximum : 28

Fahrenheit

- Minimum : 60
- Maximum : 84

If you need details regarding your specific vehicle connect advanced channel `command-capabilities`.
It delivers a JSON String with your vehcile command capabilities.

````
"commandName": "TEMPERATURE_CONFIGURE",
"isAvailable": true,
"parameters": [
    {
        "allowedEnums": [
            "FRONT_CENTER"
        ],
        "parameterName": "TEMPERATURE_POINTS_ZONE"
    },
    {
        "allowedEnums": null,
        "maxValue": 28,
        "minValue": 16,
        "parameterName": "TEMPERATURE_POINTS_TEMPERATURE",
        "steps": 0.5
    }
]
````
### Service

Group name: `service`
All channels read-only.
Service and Warning Information for vehicle
States and Controls are depending on your vehicle capabilities.

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
All channels read-only.

| Channel          | Type                 |  Description                 | bev | hybrid | combustion |
|------------------|----------------------|------------------------------|-----|--------|------------|
| mileage          | Number:Length        |  Total mileage               | X   | X      | X          |
| soc              | Number:Dimensionless |  Battery state of charge     | X   | X      |            |
| charged          | Number:Energy        |  Charged Battery Energy      | X   | X      |            |
| uncharged        | Number:Energy        |  Uncharged Battery Energy    | X   | X      |            |
| range-electric   | Number:Length        |  Electric range              | X   | X      |            |
| radius-electric  | Number:Length        |  Electric radius for map     | X   | X      |            |
| fuel-level       | Number:Dimensionless |  Fuel level in percent       |     | X      | X          |
| fuel-remain      | Number:Volume        |  Remaining Fuel              |     | X      | X          |
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
States and Controls are depending on your vehicle capabilities.

| Channel             | Type                 |  Description                           | Read | Write |
|---------------------|----------------------|----------------------------------------|------|-------|
| charge-flap         | Number               |  Charge Flap Status                    | X    |       |
| coupler-ac          | Number               |  Coupler AC Status                     | X    |       |
| coupler-dc          | Number               |  Coupler DC Status                     | X    |       |
| coupler-lock        | Number               |  Coupler Lock Status                   | X    |       |
| active              | Switch               |  Charging Active                       | X    |       |
| power               | Number:Power         |  Current Charging Power                | X    |       |
| end-time            | DateTime             |  Estimated Charging End                | X    |       |
| program             | Number               |  Selected Charge Program               | X    | X     |
| max-soc             | Number:Dimensionless |  Charge Target SoC                     | X    | X     |
| auto-unlock         | Switch               |  Auto Unlock Coupler after charging    | X    | X     |

#### Charge Flap Mapping

- 0 : Open
- 1 : Closed

#### Coupler AC Mapping

- 0 : Plugged
- 2 : Unplugged

#### Coupler DC Mapping

- 0 : Plugged
- 2 : Unplugged

#### Coupler Lock Mapping

- 0 : Locked
- 1 : Unlocked


#### Program Mapping

Calculated automatically based on your vehicle capabilities

#### Max SoC Setting

SoC target for selected program can be configured if your vehicle capabilities are supporting it.
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
| avg-speed        | Number:Speed         |  Last Trip Average Speed in km/h                                     |
| cons-ev          | Number               |  Last Trip Average Electric Energy Consumption                       |
| cons-conv        | Number               |  Last Trip Average Fuel Consumption                                  |
| distance-reset   | Number Length        |  Since Reset Trip Distance                                           |
| time-reset       | String               |  Since Reset Duration in days, hours and minutes                     |
| avg-speed-reset  | Number:Speed         |  Since Reset Average Speed in km/h                                   |
| cons-ev-reset    | Number               |  Since Reset Average Electric Energy Consumption                     |
| cons-conv-reset  | Number:Volume        |  Since Reset Average Fuel Consumption                                |
| cons-ev-unit     | String               |  Unit of Average Electric Consumption                                |
| cons-conv-unit   | String               |  Unit of Average Fuel Consumption                                    |

In your MercedesMe App Front Page 

- Burger Menu top left 
- last Entry `Settings`
- First Entry `Units`

you can configure different average consumption units like kWh per 100 kilometer or km per kWh.

<img src="./doc/ElectricConsumptionUints.png" width="300" height="300"/>

### Position

Group name: `position`

| Channel             | Type                 |  Description                                    | Read | Write |
|---------------------|----------------------|-------------------------------------------------|------|-------|
| heading             | Number:Angle         |  Heading of Vehicle                             | X    |       |
| gps                 | Point                |  GPS Location Point of Vehicle                  | X    |       |
| signal              | Number               |  Request Light or Horn Signal to find Vehicle   |      |  X    |

#### Signal Settings

Depends on the capabilities of your vehicle

### Tires

Group name: `tires`
All channels `read-only`

| Channel                  | Type                 |  Description                    |
|--------------------------|----------------------|---------------------------------|
| pressure-front-left      | Number:Pressure      |  Tire Pressure Front Left       |
| pressure-front-right     | Number:Pressure      |  Tire Pressure Front Right      |
| pressure-rear-left       | Number:Pressure      |  Tire Pressure Rear Left        |
| pressure-rear-right      | Number:Pressure      |  Tire Pressure Rear Right       |
| sensor-available         | Number               |  Tire Sensor Available          | 
| marker-front-left        | Number               |  Tire Marker Front Left         |
| marker-front-right       | Number               |  Tire Marker Front Right        | 
| marker-rear-left         | Number               |  Tire Marker Rear Left          | 
| marker-rear-right        | Number               |  Tire Marker Rear Right         |
| last-update              | DateTime             |  Timestamp of last Measurement  |

#### Sensor Available Mapping

- Not available yet!

#### Tire Marker Mapping

- Not available yet!

### Commands

Group name: `command`
All channels `read-only`

| Channel              | Type        |  Description                       |
|----------------------|-------------|------------------------------------|
| cmd-name             | String      |  Command Name which is handled     |
| cmd-state            | String      |  Current Command State             |
| cmd-last-update      | String      |  Timestamp of last update          |

Show state of the send command sent by above channels which are able to write values.
**Don't flood the API with commands**.
The Mercedes API cannot withstand _Monkey Testing_.
Send lock/unlock or temperatures in a short period of time will result in failures.


Command Names:

-  [ignition | vehicle](#vehicle) : ENGINESTART, ENGINESTOP
-  [lock | vehicle](#vehicle) : DOORSLOCK, DOORSUNLOCK
-  [windows | vehicle](#vehicle) : WINDOWOPEN, WINDOWVENTILATE, WINDOWCLOSE
-  [sunroof | doors](#doors) : SUNROOFOPEN, SUNROOFLIFT, SUNROOFCLOSE  
-  [activate | hvac](#hvac) : PRECONDSTART, PRECONDSTOP
-  [seats,zone,temperature | hvac](#hvac) : TEMPERATURECONFIGURE
-  [program, mox-soc, auto-unlock | charge](#charge) : CHARGEPROGRAMCONFIGURE
-  [signal | position](#position) : SIGPOSSTART


Command State:

- INITIATION
- ENQUEUED
- PROCESSING
- FINISHED
- FAILED

## Vehicle Actions

You've the possibility to perform the below action in your rules. 

### Send POI

````java
    /**
     * Send Point of Interest (POI) to your vehicle.
     * This POI is shown in your vehicle messages and can be instantly used to start a navigation route to this point.
     * A "catchy" title plus latitude / longitude are mandatory.
     * Parameters args is optional. If you use it respect the following order
     * 1) City
     * 2) Street
     * 3) Postal Code
     * If you miss any of them provide an empty String
     *
     * @param title - the title will be shown in your vehicle message inbox
     * @param latitude - latitude of POI location
     * @param longitude - longitude of POI location
     * @param args - optional but respect order city, street, postal code
     */
    public void sendPoi(String title, double latitude, double longitude, String... args) 
````

### Example

If you have 2 items `Poi_Location` (PointType) and `Poi_Location_Name` (StringType).
Set first the name and then change the location and the rule will trigger.

````
rule "Send POI"
    when
        Item Poi_Location changed
    then
        val mercedesmeActions = getActions("mercedesme","mercedesme:bev:abc:xyz")
        val double lat = Poi_Location.state.getLatitude
        val double lon = Poi_Location.state.getLongitude
        mercedesmeActions.sendPoi(Poi_Location_Name.state.toString,lat,lon)
end
````

## Troubleshooting

There's a big variety of vehicles with different features and different command capabilities.
In order to be able to analyze problems 3 advanced channels are placed in the vehicle group.

* feature-capabilities - showing which feature your vehicle is eqipped with
* command-capabilities - showing which commands can be sent to your vehicle
* proto-update - latest update of your vehicle data

In case you find problems regarding this binding add items to these 3 channels.
The items are reporting Strings in json format.
Please check yourself no critical data is inside.
Vehicle Identification Number (VIN) isn't part of data.
GPS data which is showing your location is anonymized.
The content of these items shall be used to create a problem report.

Keep these 3 channels disconnected during normal operation.



