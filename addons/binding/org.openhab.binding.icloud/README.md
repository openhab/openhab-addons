# iCloud Binding

This binding uses the Apple iCloud service for providing device based information such as battery details or location.
To use this binding, you must have an apple account.

The binding uses googles "[Geocode API](https://developers.google.com/maps/documentation/geocoding/)" to translate the coordinates into a address data.

<!-- TOC -->

- [iCloud Binding](#icloud-binding)
    - [Supported Things](#supported-things)
    - [Discovery](#discovery)
    - [Binding Configuration](#binding-configuration)
    - [Thing Configuration](#thing-configuration)
        - [iCloud Binding Thing](#icloud-binding-thing)
    - [Channels](#channels)
    - [Full Example](#full-example)
        - [Items](#items)
        - [Sitemap](#sitemap)
- [Configuration with Paper UI](#configuration-with-paper-ui)

<!-- /TOC -->

## Supported Things

The following devices are known to work with this binding:
* iPhone 6s
* iPhone 5c
* iPhone 7
* iWatch 2
* iPad Air 2/2017/Pro

Other devices should work as well. Please provide feedback if you have tested another device type. 

## Discovery

For each iCloud account to be included an iCloud binding thing needs to be configured with your id, password and refresh rate. The devices registered to this account will then be automatically discovered. Do not create devices manually.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

### iCloud Binding Thing
| Parameter	| Description |
|-----------|-------------|
|AppleId||
|Password||
|RefreshTimeInMinutes||

All parameters are mandatory.

## Channels

The following channels are available (if supported by the device):

| Channel ID    |Type           | Description |
| ------------- | ------------- |-------------|
| BatteryStatus | String        ||
| Battery Level | Number        ||
| FindMyPhone   | Switch        ||
| Location      | Location      ||
| Accuracy      | Number        ||
| Distance from Home | Number   ||
| Last Location Update | DateTime || 
| Street        | String        ||
| City          | String        ||
| Country       | String        ||
| Formatted Address | String    ||

## Full Example

### Items
icloud.items:

```php
String JohnIPhone6_BatteryStatus "Battery Status [%s]" <battery> (giPhone)  {channel="icloud:device:YourDeviceID:0:BatteryStatus"}
Number JohnIPhone6_BatteryLevel "Battery Level [%.0f]" <battery> (giPhone) {channel="icloud:device:YourDeviceID:0:BatteryLevel"}
Switch JohnIPhone6_FindMyPhone "Find iPhone [%s]" <suitcase> (giPhone) {channel="icloud:device:YourDeviceID:0:FindMyPhone"}
Location JohnIPhone6_Location "Coordinates" <suitcase> (giPhone)  {channel="icloud:device:YourDeviceID:0:Location"}
Number JohnIPhone6_LocationAccuracy "Coordinates Accuracy [%.0f]" <suitcase> (giPhone){channel="icloud:device:YourDeviceID:0:LocationAccuracy"}
Number JohnIPhone6_DistanceFromHome "Distance from home [%.0f]" <suitcase> (giPhone_A){channel="icloud:device:YourDeviceID:0:DistanceFromHome"}
DateTime JohnIPhone6_LastLocationUpdate    "Letztes Update [%1$td.%1$tm.%1$tY, %1$tH:%1$tM]"   <suitcase>      (giPhone)   {channel="icloud:device:YourDeviceID:0:LastUpdate"}
String JohnIPhone6_Street "Street [%s]" <suitcase> (giPhone) {channel="icloud:device:YourDeviceID e789ef3:0:AddressStreet"}
String JohnIPhone6_City "City [%s]" <suitcase> (giPhone) {channel="icloud:device:YourDeviceID:0:AddressCity"}
String JohnIPhone6_Country "Country [%s]" <suitcase> (giPhone)   {channel="icloud:device:YourDeviceID:0:AddressCountry"}
String JohnIPhone6_FormattedAddress "Address [%s]" <suitcase> (giPhone)   {channel="icloud:device:YourDeviceID:0:FormattedAddress"}
```
### Sitemap
```
sitemap icloud  label="iCloud Data" {
        Frame label="John Doe"  {
                Default item=JohnIPhone6_BatteryStatus icon="poweroutlet_uk"
                Default item=JohnIPhone6_BatteryLevel label="Battery Level [%.0f %%]" icon="battery"
                Default item=JohnIPhone6_Location label="Location [%s]"
                Default item=JohnIPhone6_LocationAccuracy icon="movecontrol"
                Default item=JohnIPhone6_DistanceFromHome label="Distance from Home [%.0f m]" icon="zoom"
                Default item=JohnIPhone6_Street icon="house"
                Default item=JohnIPhone6_City icon="house"
                Default item=JohnIPhone6_Country icon="house"
                Default item=JohnIPhone6_FormattedAddress
                Default item=JohnIPhone6_LastLocationUpdate label="Updated [%1$td/%1$tm/%1$ty %1$tH:%1$tM]" icon="clock"
                Switch  item=JohnIPhone6_FindMyPhone label="Find this device"
        }
}
```
# Configuration with Paper UI 

First select iCloud Binding in "Paper UI > Configuration > Things" and provide your id, password and refresh interval. Devices are automatically discovered once the thing is configured and online.


1.  Select the binding:  
![Select binding](./doc/Config_1.png "Step 1")
2.  Select the bridge:   
![Select bridge](./doc/Config_2.png "Step 2") 
3.  Configure your account and the desired refresh rate. 
![Configure](./doc/Config_3.png "Step 3") 