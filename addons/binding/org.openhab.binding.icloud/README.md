# iCloud Binding

This binding uses the Apple iCloud service for providing device based information such as battery details or location.
An Apple account is required. Two factor authentication is supported.

Please note: Application specific passwords are not supported.

The binding uses Google's "[Geocode API](https://developers.google.com/maps/documentation/geocoding/)" to translate the coordinates into a address data.

<!-- TOC -->

- [iCloud Binding](#icloud-binding)
    - [Supported Things](#supported-things)
    - [Discovery](#discovery)
    - [Binding Configuration](#binding-configuration)
    - [Thing Configuration](#thing-configuration)
        - [iCloud Binding Thing](#icloud-binding-thing)
    - [Channels](#channels)
        - [Bridge](#bridge)
        - [Device](#device)
    - [Full Example](#full-example)
        - [iCloud.things:](#icloudthings)
        - [icloud.items:](#iclouditems)
- [Configuration with Paper UI](#configuration-with-paper-ui)

<!-- /TOC -->

## Supported Things

The following devices are known to work with this binding:

* iPhone 6s
* iPhone 5c
* iPhone 7
* iWatch 2
* iPad Air 2/2017/Pro
* MacBook Pro

Other devices should work as well. Please provide feedback if you have tested another device type. 

## Discovery

For each iCloud account to be included an iCloud binding thing needs to be configured with your id, password and refresh rate. The devices registered to this account will then be automatically discovered. Do not create devices manually.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

### iCloud Account Thing

| Parameter            | Description                                         |           |
|----------------------|-----------------------------------------------------|-----------|
| AppleId              |                                                     | mandatory |
| Password             |                                                     | mandatory |
| RefreshTimeInMinutes |                                                     | mandatory |
| GoogleAPIKey         | Google API key to be used for address lookup calls. | optional  |

Without a "GoogleAPIKey" the number of daily request are limited and the bindings address lookup functionality will stop working. It is recommended to provide this value.

## Channels

### Account

| Channel ID       | Type     | Description                                                              |
|------------------|----------|--------------------------------------------------------------------------|
| NumberOfDevices  | Number   | Number of registered devices with this iCloud account.                   |
| Owner            | String   | Registered owner of this iCloud account.                                 |
| Refresh          | Switch   | Update all devices registered with this account immediately.             |

### Device

The following channels are available (if supported by the device):

| Channel ID       | Type     | Description                                                              |
|------------------|----------|--------------------------------------------------------------------------|
| BatteryStatus    | String   | Current battery status (Charging, Charged, Unknown)                      |
| BatteryLevel     | Number   | Battery charge in %                                                      |
| FindMyPhone      | Switch   | Triggers the "Find my phone" functionality of the device (if available). |
| Location         | Location | Location of the device.                                                  |
| LocationAccuracy | Number   | Accuracy of the last position report.                                    |
| DistanceFromHome | Number   | Device distance from the location set in openHAB regional settings.                                     |
| LastUpdate       | DateTime | Timestamp of the last location update.                                   |
| AddressStreet    | String   | Street                                                                   |
| AddressCity      | String   | City                                                                     |
| AddressCountry   | String   | Country                                                                  |
| FormattedAddress | String   | Formatted address string                                                 |

## Full Example

### iCloud.things

```php
Bridge icloud:account:account1 [AppleId="abc@xyz.tld", Password="secure", RefreshTimeInMinutes=10]
{
    Thing device 0 "My iPhone 7" @ "World"
    Thing device 1 "My iWatch 2" @ "World"
}
```
The refresh time is optional and has a default of 5. The "label" @ "location" part is optional (as always).

### icloud.items

```php
Group iCloud_Group (Whg)
String iCloud_Account1_Owner "iCloud Account Owner [%s]" (iCloud_Group) {channel="icloud:bridge:account1:Owner"}
Number iCloud_Account1_NumberOfDevices "iCloud Account NumberOfDevices [%d]" (iCloud_Group) {channel="icloud:bridge:account1:NumberOfDevices"}
Switch iCloud_Account1_ForceRefresh "iCloud Account Force Refresh" (iCloud_Group) {channel="icloud:bridge:account1:ForcedRefresh"}

String iPhone_BatteryStatus "Battery Status [%s]" <battery> (iCloud_Group)  {channel="icloud:device:account1:0:BatteryStatus"}
Number iPhone_BatteryLevel "Battery Level [%.0f]" <battery> (iCloud_Group) {channel="icloud:device:account1:0:BatteryLevel"}
Switch iPhone_FindMyPhone "Find iPhone [%s]" <suitcase> (iCloud_Group) {channel="icloud:device:account1:0:FindMyPhone"}
Location iPhone_Location "Coordinates" <suitcase> (iCloud_Group)  {channel="icloud:device:account1:0:Location"}
Number iPhone_LocationAccuracy "Coordinates Accuracy [%.0f]" <suitcase> (iCloud_Group){channel="icloud:device:account1:0:LocationAccuracy"}
Number iPhone_DistanceFromHome "Distance from home [%.0f]" <suitcase> (iCloud_Group){channel="icloud:device:account1:0:DistanceFromHome"}
DateTime iPhone_LastLocationUpdate "Last Update [%1$td.%1$tm.%1$tY, %1$tH:%1$tM]" <suitcase> (iCloud_Group) {channel="icloud:device:account1:0:LastUpdate"}
String iPhone_Street "Street [%s]" <suitcase> (iCloud_Group) {channel="icloud:device:account1:0:AddressStreet"}
String iPhone_City "City [%s]" <suitcase> (iCloud_Group) {channel="icloud:device:account1:0:AddressCity"}
String iPhone_Country "Country [%s]" <suitcase> (iCloud_Group)   {channel="icloud:device:account1:0:AddressCountry"}
String iPhone_FormattedAddress "Address [%s]" <suitcase> (iCloud_Group) {channel="icloud:device:account1:0:FormattedAddress"}
```

# Configuration with Paper UI 

First select iCloud Binding in "Paper UI > Configuration > Things" and provide your id, password and refresh interval. Devices are automatically discovered once the thing is configured and online.

1.  Select the "iCloud Binding": ![Select binding](./doc/Config_1.png "Step 1")
2.  Select the "iCloud Account: ![Select bridge](./doc/Config_2.png "Step 2") 
3.  Configure your account and the desired refresh rate. ![Configure](./doc/Config_3.png "Step 3") 
