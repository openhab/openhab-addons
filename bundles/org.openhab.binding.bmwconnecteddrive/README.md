# BMWConnectedDrive Binding

<img align="right" src="./doc/bmw-connected.png" width="150" height="150"/>

The Binding connects your BMW Vehicales which are registered in your _Garage_ in your BMW ConnectedDrive Portal.

## Supported Things

### Bridge

The Bridge etsablishes the Connection between BMW ConnectedDrive Portal and opanHAB.

| Name                       | Bridge Type ID | Description                                                |
|----------------------------|----------------|------------------------------------------------------------|
| BMW ConnectedDrive Account | account        | Access to BMW ConnectedDrive Portal for a specific user    |


### Things

Four different Vehcile Types are provided. They differ in the supported Channel Groups & Channels. 
While Conventional Fuel Vehicles have no "Charging Profile" Electric Vehicles don't provide a _fuel range_. 
For Hybrid Vehicles _fuel range_ and _electric range_ is provided and in addition a _combined range_
 
| Name                                | Thing Type ID | Supported Channel Groups                                   |
|-------------------------------------|---------------|------------------------------------------------------------|
| BMW Electric Vehicle                | BEV           | status, range, last-trip, all-trips, charge-profile, image, troubeshoot |
| BMW Electric Vehicle with REX       | BEV_REX       | status, range, last-trip, all-trips, charge-profile, image, troubeshoot |
| BMW Plug-In-Hybrid Electric Vehicle | PHEV          | status, range, last-trip, all-trips, image, troubeshoot |
| BMW Conventional Vehicle            | CONV          | status, range, last-trip, all-trips, image, troubeshoot |

## Discovery

Auto Discovery is starting after you created the Bridge towards BMW ConnectedDrive Portal. 
A list of your registered Vehicles is queried and all found Vehicles are added in Inbox.
As Unique Identifier the _Vehicle Identification Number_ (VIN) is used. 
So if you already predefined a Thing in *.things configuration with the same VIN the Discovery won't highlight it again. 
Please note for Auto Detected Vehicles there are many Properties for the Vehicle added like Color, Model Type, responsible Dealer and more.

## Configuration

### Bridge

| Parameter       | Type    | Description                                                             |           
|-----------------|---------|-------------------------------------------------------------------------|
| userName        | text    | BMW Connected Drive Username                  |
| password        | text    | BMW Connected Drive Password                  |
| region          | text    | Select your Region in order to connect to the appropriate BMW Server. Select from Drop Down list _North America_, _China_ or _Rest of World_.  |

### Things

All Things are needing the same Configuration Data

| Parameter       | Type    | Description                                                             |           
|-----------------|---------|-------------------------------------------------------------------------|
| vin             | text    | Vehicle Identification Number (VIN)               |
| refreshInterval | integer | Refresh Interval in Minutes             |
| units           | text    | Unit Selection. Either AUTODETECT (UK & US) or directly set the desired Units Metric or Imperial  |
| imageSize       | integer | Image Picture Size<  |
| imageViewport   | text    | Image Viewport - FRONT, REAR, SIDE, DASHBOARD, DRIVERDOOR |

## Channels

There are many Channels available for each Vehilce. For better overview they are clustered in different Channel Groups.

### Channel Group _Status_

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                       |
|-----------------------|------------------|---------------------|--------|---------------------------------------------------|
| Modbus-ID             | status           | modbus-id           | String | Modbus ID / Magic Byte of E3DC                    |


### Channel Group _Range Data_

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                       |
|-----------------------|------------------|---------------------|--------|---------------------------------------------------|
| Modbus-ID             | range           | modbus-id           | String | Modbus ID / Magic Byte of E3DC                    |

### Channel Group _Last Trip_

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                       |
|-----------------------|------------------|---------------------|--------|---------------------------------------------------|
| Modbus-ID             | status           | modbus-id           | String | Modbus ID / Magic Byte of E3DC                    |


### Channel Group _Lifetime Statistics_

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                       |
|-----------------------|------------------|---------------------|--------|---------------------------------------------------|
| Modbus-ID             | range           | modbus-id           | String | Modbus ID / Magic Byte of E3DC                    |

### Channel Group _Vehicle Location_

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                       |
|-----------------------|------------------|---------------------|--------|---------------------------------------------------|
| Modbus-ID             | status           | modbus-id           | String | Modbus ID / Magic Byte of E3DC                    |


### Channel Group _Remote Services_

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                       |
|-----------------------|------------------|---------------------|--------|---------------------------------------------------|
| Modbus-ID             | range           | modbus-id           | String | Modbus ID / Magic Byte of E3DC                    |

### Channel Group _Image_

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                       |
|-----------------------|------------------|---------------------|--------|---------------------------------------------------|
| Modbus-ID             | status           | modbus-id           | String | Modbus ID / Magic Byte of E3DC                    |


### Channel Group _Troubleshooting_

| Channel Label         | Channel Group ID | Channel ID          | Type   | Description                                       |
|-----------------------|------------------|---------------------|--------|---------------------------------------------------|
| Modbus-ID             | range           | modbus-id           | String | Modbus ID / Magic Byte of E3DC                    |

## TroubleShooting

In order to to identify issues the TroubleShooting Channels are providing Analysis Informations

### Discovery

If your Vehicle isn't discovered correctly perform the following steps

* Check if your Vehicle is attached correctly in ConnectedDrive. Only if it's attached to your ConnectedDrive Account it's possible to detect it.
* In the below example sitemap the Switch _Log Discovery Fingerprint_ can be executed. 

### Car Data

### Update Timestamp

There's a timestamp showing the last update of your Vehicle. If this isn't shown correctly please check the date settings.
In case of Raspberry Pi execute _raspi-cinfig_, select _Localization Options_, the _Change Time Zone_
Select your _Geaographical Area_ and afterwards the correct City.
One restart of openHAB service with _systemctl restart openhab2_ is necessary in order to see the corrected Time Settings.

## Full Example

### Things

### Items

### Sitemap

## Going further

You're now able to receive your Vehicle Data in openHAB. Continue the work and combine this data with other Powerful openHAB Bindings and Widgets.

### OpenstreetMap Widget

### OpenWeatherMap Binding and Widget

Especially Electric Vehicles which maybe are charged with your Local Photovoltaic System the Weather forecast and corresponding Cloudiness is interesting.
Use the OpenWeatherMap Binding and existing [Widget Solutions[(https://community.openhab.org/t/openweathermap-widget-for-habpanel/65027) to check this data in addition to your Vehicles State of Charge.

## Credits

This work is based on the work of [Bimmer Connected](https://github.com/bimmerconnected/bimmer_connected). 
Also a [manual installation based on python](https://community.openhab.org/t/script-to-access-the-bmw-connecteddrive-portal-via-oh/37345) was already available for openHAB.
This Binding is basically a port to openHAB based on these concept works!  
