# MercedesMe Binding

Connects your Mercedes Me Account and attached vehicles to openHAB.
Setup requires some time so follow [the steps of bridge[configuration}(#bridge-configuration)

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

1. Go to [Mercedes Developer Page](https://developer.mercedes-benz.com/). Login with your MercedesMe credentials.
2. Create a project in the [console tab](https://developer.mercedes-benz.com/console)
    - _Project Name:_  unique name e.g. **openHAB MercedesMe binding** plus **Your bridge ID**
    - _Purpose URL:_  use link towards this binding description 
    - _Business Purpose:_  e.g. **Private usage in openHAB Smarthome system**
3. After project is created subscribe [to these Mercedes Benz APIs](https://developer.mercedes-benz.com/products?vt=cars&vt=vans&vt=smart&p=BYOCAR) with _Add Products_ button 
4. For all Products perform the same steps
    - Select product
    - Choose _Get For Free_
    - Choose _BYOCAR_ (Build Your Own Car)
    - Button _Confirm_
5. Select the following products
    - Vehicle Status
    - Vehicle Lock Status
    - Pay as you drive insurance
    - Electric Vehcile Status
    - Fuel Status
6. Optional: Subscribe also to _Vehicle images_. Select the _Basic Trial_ version. The images will be stored so the API is used just a few times.
7. Press _Subscribe_ button. Your project should have [these product subscriptions](#mb-product-subscriptions)
8. Generate the project credentials
9. Open in new browser tab your openHAB page. Add a new Thing _MercedesMe Account_
10. Copy paste _Client ID_ , _Client Secret_ and _API Key_ from the Mercedes tab into the openHAB config
11. Check if the registered Mercedes products _excluding Vehicle Images_ are matching exactly with the openHab config switches
12. Create Thing!
13. The fresh created [account has one property](#openhab-configuration) `callbackUrl`. Copy it and paste it in a new browser tab
14. A simple HTML page is shown including a link towards the Authorization flow - **don't click yet**. If page isn't shown please adapt IP and port in openHAB configuration with Advanced Options activated
15. The copied URL needs to be added in your [Mercedes project credentials](#mb-credentials) from 8
16. Now click onto the link from 12. You'll be asked one time if you [grant access](mb-access-request) towards the API. Click ok and authorization is done!

Some supporting screenshots for the setup

### MB Credentials

<img src="./doc/MBDeveloper-Credentials.png" width="600" height="350"/>

### MB Product Subscriptions

<img src="./doc/MBDeveloper-Subscriptions.png" width="600" height="350"/>

### openHAB Configuration

<img src="./doc/MercedesMeConfiguration.png" width="400" height="500"/>

### MB Access Request

<img src="./doc/MBAccessRequest.png" width="500" height="350"/>


| Name            | Type    | Description                           | Default    | Required | Advanced |
|-----------------|---------|---------------------------------------|------------|----------|----------|
| clientId        | text    | Mercedes Benz Developer Client ID     | N/A        | yes      | no       |
| clientSecret    | text    | Mercedes Benz Developer Client Secret | N/A        | yes      | no       |
| imageApiKey     | text    | Mercedes Benz Developer Image API Key | N/A        | no       | no       |
| odoScope        | boolean | PayAsYourDrive Insurance              | true       | yes      | no       |
| vehicleScope    | boolean | Vehicle Status                        | true       | yes      | no       |
| lockScope       | boolean | Lock status of doors and trunk        | true       | yes      | no       |
| fuelScope       | boolean | Fuel Status                           | true       | yes      | no       |
| evScope         | boolean | Electric Vehicle Status               | true       | yes      | no       |
| callbackIp      | text    | IP address of your openHAB server     | autodetect | no       | yes      |
| callbackPort    | integer | **Unique** port number                | autodetect | no       | yes      |

The `callbackPort` needs to be unique for all created MercedesMe account things. Otherwise token exchange will be corrupted!


### Thing Configuration

Configuration for all vehicles are the same.

For vehicle images Mercedes Benz Developer offers only a trial version with limited calls.
Check in **beforehand** if your vehicle has some restrictions or even if it's suppoerted at all.
Visit [Vehicle Image Details](https://developer.mercedes-benz.com/products/vehicle_images/details) in order to check your vehcile capabilities.
For example the EQA doesn't provide `night` images with `background`.
If your configuration is set this way the API calls are wasted!

<img src="./doc/ImageRestrictions.png" width="800" height="30"/>

See also [image channel section](#image) for further advise!


| Name            | Type    | Description                                         | Default | Required | Advanced |
|-----------------|---------|-----------------------------------------------------|----- ---|----------|----------|
| vin             | text    | Vehicle identification number                       | N/A     | yes      | no       |
| refreshInterval | integer | Refresh interval in minutes                         | 5       | yes      | no       |
| background      | boolean | Vehicle images provided with or without background  | false   | no       | yes      |
| night           | boolean | Vehicle images in night conditions                  | false   | no       | yes      |
| cropped         | boolean | Vehicle images in 4:3 instead of 16:9               | false   | no       | yes      |
| roofOpen        | boolean | Vehicle images with open roof (only Cabriolet)      | false   | no       | yes      |
| format          | text    | Vehicle images format (webp or png)                 | webp    | no       | yes      |

## Channels

Channels are seperated in groups

### Range

Group name: `range`
All channels `readonly`

| Channel          | Type                 |  Description                 | bev | hybrid | combustion |
|------------------|----------------------|------------------------------| ----|--------|------------|
| mileage          | Number:Length        |  Total Mileage               | X   | X      | X          |
| soc              | Number:Dimensionless |  Battery state of charge     | X   | X      |            |
| range-electric   | Number:Length        |  Electric range              | X   | X      |            |
| radius-electric  | Number:Length        |  Electric radius for map     | X   | X      |            |
| fuel-level       | Number:Dimensionless |  Fuel level in percent       |     | X      | X          |
| range-fuel       | Number:Length        |  Fuel range                  |     | X      | X          |
| radius-fuel      | Number:Length        |  Fuel radius for map         |     | X      | X          |
| range-hybrid     | Number:Length        |  Hybrid range                |     | X      |            |
| radius-hybrid    | Number:Length        |  Hybrid radius for map       |     | X      |            |

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

### Image

Provides exterior and interior images for your specific vehicle.
Group name: `image`

| Channel          | Type                 |  Description                 | Write |
|------------------|----------------------|------------------------------|-------|
| image-data       | Raw                  |  Vehicle Image               |       |
| image-view       | text                 |  Vehicle Image Viewpoint     |   X   |
| clear-cache      | Switch               |  Remove All Stored Images    |   X   |

**If** the `imageApiKey` in [Bridge Configuration](#bridge-configuration) is set the vehicle thing will try to get images.
Pay attetion to the [Advanced Image Configuration Properties](#thing-configuration) before requesting new images.
Sending commands towards the `image-view` channel will change the image.
The `image-view` is providing options to select the available images for your specific vehicle.
Images are stored in `jsondb` so if you requested all images the Mercedes Benz Image API will not be called anymore which is good because you have arestricted amount of calls!
If you're not satisfied e.g. you want a background you need to

1. change the [Advanced Image Configuration Properties](#thing-configuration)
2. Switch `clear-cache` channel item to `ON` to clear all images
3. request them via `image-view` 

## Troubleshooting

### Receive no data

Especially after setting up a new Mercedes Benz Developer Project you'll receive no data.
It seems that the API isn't _filled_ yet. 

**Pre-Condition**
- The MercedesMe bridge is online = authorization is fine
- The MercedesMe thing is online = API calls are fine 

**Solution**
- Reduce `refreshInterval` to 1 minute
- Go to your vehcile, open doors and windows, turn on lights, drive a bit  ... 
- wait until values are providing the right states


## Mercedes Benz Developer

Visit [Mercedes Benz Developer](https://developer.mercedes-benz.com/) to gain more deep information.
