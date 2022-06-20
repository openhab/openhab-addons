# MercedesMe Binding

Connects your Mercedes Me Account and attached vehicles to openHAB.

## Supported Things

- Bridge `account`: Data to connect your Mercedes Me account
- Thing `combustion`: Conventional fuel vehicle
- Thing `hybrid`: Fuel vehicle with supporting electric engine
- Thing `bev`: Battery electric vehicle


## Discovery

There's no automatic discovery. 


## Bridge Configuration

Bridge needs configuration in order to connect properly to your Mercedes Me Account. 
Perform the following steps to obtain the configuration data and perfrom the authorization flow.

1. Create Bridge account in openHAB
2. Go to [Mercedes Me Login](https://id.mercedes-benz.com/ciam/auth/login) page and login woth your credentials
3. Go to [Mercedes Developer Page](https://developer.mercedes-benz.com/) Login and you'll have access to the API.
4. Create a project in the [console tab](https://developer.mercedes-benz.com/console)
    - _Project Name:_  unique name e.g. **openHAB MercedesMe binding** plus **Your bridge ID**
    - _Purpose URL:_  use link towards this binding description 
    - _Business Purpose:_  e.g. **Private usage in openHAB Smarthome system**
5. After project is created subscribe to the API's with _Add Products_ button 
6. For all Products perform the same steps
    - Select product
    - Choose _Get For Free_
    - Choose _BYOCAR_ (Build Your Own Car)
    - Button _Confirm_
7. Select the following products
    - Vehicle Status
    - Vehicle Lock Status
    -  Pay as you drive insurance
    - Electric Vehcile Status
    - Fuel Status
8. Press _Subscribe_ button
9. Generate the project credentials - you need them to configuree your Bridge from 1.
10. Get the `Client ID` and `Client Secret` and put them into the Bridge configuration - save
11. The account bridge has one property `callbackUrl`. Copy it and paste it in a new browser tab
12. A simple HTML page is shown including a link towards the Authorization flow - don't click yet
13. The copied URL needs to be added in your Mercedes project credentials from 9.
14. Now click onto the link from 12. You'll be asked one time if you grant access towards the API. Click ok and authorization is done!

    - 

| Name            | Type    | Description                           | Default    | Required | Advanced |
|-----------------|---------|---------------------------------------|------------|----------|----------|
| clientId        | text    | Hostname or IP address of the device  | N/A        | yes      | no       |
| clientSecret    | text    | Password to access the device         | N/A        | yes      | no       |
| callbackIp      | text    | Password to access the device         | autodetect | no       | yes      |
| callbackPort    | integer | Interval the device is polled in sec. | autodetect | no       | yes      |
| scope           | text    | Password to access the device         | autodetect | no       | yes      |

### Thing Configuration

Configuration for vehicles ar the same.

| Name            | Type    | Description                           | Default | Required | 
|-----------------|---------|---------------------------------------|---------|----------|
| vin             | text    | Vehicle identification number         | N/A     | yes      |
| refreshInterval | integer | Refresh interval in minutes           | 5       | yes      |

## Channels

Channels are seperated in groups

### Range

Group name: `range`
All channels `readonly`

| Channel        | Type                 |  Description                 | bev | hybrid | combustion |
|----------------|----------------------|------------------------------| ----|--------|------------|
| mileage        | Number:Length        |  Total Mileage               | X   | X      | X          |
| soc            | Number:Dimensionless |  Battery state of charge     | X   | X      |            |
| range-electric | Number:Length        |  Electric range              | X   | X      |            |
| fuel-level     | Number:Dimensionless |  Fule level in percent       |     | X      | X          |
| range-fuel     | Number:Length        |  Fuel range                  |     | X      | X          |

### Doors

Group name: `doors`
All channels `readonly`

| Channel          | Type                 |  Description                 |
|------------------|----------------------|------------------------------|
| driver-front     | Contact              |  Driver door                 |
| driver-rear      | Contact              |  Driver door reat            |
| passenger-front  | Contact              |  Passenger door              |
| passenger-rear   | Contact              |  Passenger door rear         |
| deck-lid         | Contact              |  Deck lid                    |
| sunroof          | Number               |  Sun roof (Cabriolet         |
| rooftop          | Number               |  Roof top                    |

Mapping table `sunroof`

| Number          | Mapping             |
|-----------------|---------------------|
| 0               | Closed              |
| 1               | Open                |
| 2               | Open Lifting        |
| 3               | Running             |
| 4               | Closing             |
| 5               | Opening             |
| 6               | Closing             |

Mapping table `rootop`

| Number          | Mapping             |
|-----------------|---------------------|
| 0               | Unlocked            |
| 1               | Open and locked     |
| 2               | Closed and locked   |

### Windows

Group name: `windows`
All channels `readonly`

| Channel          | Type                 |  Description                 |
|------------------|----------------------|------------------------------|
| driver-front     | Number               |  Driver window               |
| driver-rear      | Number               |  Driver window reat          |
| passenger-front  | Number               |  Passenger window            |
| passenger-rear   | Number               |  Passenger window rear       |

Mapping table for all windows

| Number          | Mapping             |
|-----------------|---------------------|
| 0               | Intermediate        |
| 1               | Open                |
| 2               | Closed              |
| 3               | Airing              |
| 4               | Intermediate        |
| 5               | Running             |

### Lights

Group name: `lights`
All channels `readonly`

| Channel          | Type                 |  Description                 |
|------------------|----------------------|------------------------------|
| interior-front   | Switch               |  Driver door                 |
| interior-rear    | Switch               |  Driver door reat            |
| reading-left     | Switch               |  Passenger door              |
| reading-right    | Switch               |  Passenger door rear         |
| light-switch     | Number               |  Deck lid                    |

Mapping table `light-switch`

| Number          | Mapping             |
|-----------------|---------------------|
| 0               | Auto                |
| 1               | Headlight           |
| 2               | Sidelight Left      |
| 3               | Sidelight Right     |
| 4               | Parking Light       |

### Lock

Group name: `lock`
All channels `readonly`

| Channel          | Type                 |  Description                 |
|------------------|----------------------|------------------------------|
| doors            | Number               |  Lock status all doors       |
| deck-lid         | Switch               |  Deck lid lock               |
| flap             | Switch               |  Flap lock                   |

Mapping table `doors`

| Number          | Mapping             |
|-----------------|---------------------|
| 0               | Unlocked            |
| 1               | Locked Internal     |
| 2               | Locked External     |
| 3               | Unlocked Selective  |

### Location

Group name: `location`
All channels `readonly`

| Channel          | Type                 |  Description                 |
|------------------|----------------------|------------------------------|
| heading          | Number:Angle         |  Vehicle heading             |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
