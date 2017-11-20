# iCloud Binding

This binding uses the Apple [iCloud](https://www.apple.com/icloud/) service for providing device based information such as battery details or location.
An Apple account is required. Two factor authentication is supported.

Please note: Application specific passwords are not supported.

The binding uses Google's [Geocode API](https://developers.google.com/maps/documentation/geocoding/) to derive a postal address from geographic coordinates.

## Supported Things

The following devices are known to work with this binding:

* iPhone 6s
* iPhone 5c
* iPhone 7
* iWatch 2
* iPad Air 2/2017/Pro
* MacBook Pro

Other devices connected to your iCloud account should work as well.
Please provide feedback if you have tested another device type.

## Discovery

For each iCloud account to be included an iCloud binding thing needs to be configured with your ID and password.
The devices registered to this account will then be automatically discovered.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The binding provides two thing types.

### iCloud Account (Bridge) Thing

The following table lists the configuration parameters:

| Parameter            | Description                                         |                             |
|----------------------|-----------------------------------------------------|-----------------------------|
| appleId              |                                                     | mandatory                   |
| password             |                                                     | mandatory                   |
| refreshTimeInMinutes |                                                     | optional, 5 minutes default |
| googleAPIKey         | Google API key to be used for address lookup calls. | optional                    |

Without a "GoogleAPIKey" the number of daily request are limited (see also [API useage limits](https://developers.google.com/maps/documentation/geocoding/usage-limitsv)) and the bindings address lookup functionality will stop working.
It is recommended to provide this value.

A device is identified by a hash value calculated from the device id provided by apple.
If a device is removed from the account the respective openHAB thing will go OFFLINE.

## Channels

### Account

| Channel ID          | Type   | Description                                                                                               |
|---------------------|--------|-----------------------------------------------------------------------------------------------------------|
| numberOfDevices     | Number | Number of registered devices with this iCloud account.                                                    |
| owner               | String | Registered owner of this iCloud account.                                                                  |
| refresh             | Switch | Update all devices registered with this account immediately.                                              |

### Device

The following channels are available (if supported by the device):

| Channel ID                   | Type     | Description                                                                                                                                 |
|------------------------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------|
| batteryStatus                | String   | Current battery status (Charging, Charged, Unknown)                                                                                         |
| batteryLevel                 | Number   | Battery charge in %                                                                                                                         |
| findMyPhone                  | Switch   | Triggers the ["Find my phone"](https://support.apple.com/explore/find-my-iphone-ipad-mac-watch) functionality of the device (if available). |
| location                     | Location | Location of the device.                                                                                                                     |
| locationAccuracy             | Number   | Accuracy of the last position report.                                                                                                       |
| locationLastUpdate           | DateTime | Timestamp of the last location update.                                                                                                      |
| distanceFromHome             | Number   | Device distance from the location set in openHAB regional settings.                                                                         |
| addressStreet<sup>i</sup>    | String   | Street component of the address.                                                                                                            |
| addressCity<sup>i</sup>      | String   | City component of the address.                                                                                                              |
| addressCountry<sup>i</sup>   | String   | Country component of the addess.                                                                                                            |
| formattedAddress<sup>i</sup> | String   | Formatted human readable address string.                                                                                                    |
| deviceName                   | String   | The device name assigned to the device through device settings.                                                                             |
<sup>i</sup> A google API key needs to be configured for this channels to be populated.

## Full Example

### icloud.things

```php
Bridge icloud:account:myaccount [appleId="abc@xyz.tld", password="secure", refreshTimeInMinutes=5, googleAPIKey="abc123"]
{
    Thing device 4b86035f "iPhone 8" @ "World"
    Thing device 4a8fd953 "iWatch 2" @ "World"
}
```

The "label" @ "location" part is optional (as always).

### icloud.items

```php
Group    iCloud_Group

String   iCloud_Account_Owner             "iCloud Account Owner [%s]"                    (iCloud_Group) {channel="icloud:account:myaccount:owner"}
Number   iCloud_Account_NumberOfDevices   "iCloud Account NumberOfDevices [%d]"          (iCloud_Group) {channel="icloud:account:myaccount:numberOfDevices"}
Switch   iCloud_Account_ForceRefresh      "iCloud Account Force Refresh"                 (iCloud_Group) {channel="icloud:account:myaccount:refresh"}

String   iPhone_BatteryStatus             "Battery Status [%s %%]"             <battery> (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:batteryStatus"}
Number   iPhone_BatteryLevel              "Battery Level [%.0f]"               <battery> (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:batteryLevel"}
Switch   iPhone_FindMyPhone               "Trigger Find My iPhone"                       (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:findMyPhone"}
Location iPhone_Location                  "Coordinates"                                  (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:location"}
Number   iPhone_LocationAccuracy          "Coordinates Accuracy [%.0f m]"                (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:locationAccuracy"}
DateTime iPhone_LocationLastUpdate        "Last Update [%1$td.%1$tm.%1$tY, %1$tH:%1$tM]" (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:locationLastUpdate"}
Number   iPhone_DistanceFromHome          "Distance from home [%.0f m]"                  (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:distanceFromHome"}
String   iPhone_Street                    "Street [%s]"                                  (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:addressStreet"}
String   iPhone_City                      "City [%s]"                                    (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:addressCity"}
String   iPhone_Country                   "Country [%s]"                                 (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:addressCountry"}
String   iPhone_FormattedAddress          "Address [%s]"                                 (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:formattedAddress"}
String   iPhone_DeviceName                "Device Name [%s]"                             (iCloud_Group) {channel="icloud:device:myaccount:4b86035f:deviceName"}
```

# Configuration with Paper UI

First select iCloud Binding in "Paper UI > Configuration > Things" and provide your id, password and refresh interval. Devices are automatically discovered once the thing is configured and online.

1.  Select the "iCloud Binding": ![Select binding](./doc/Config_1.png "Step 1")
2.  Select the "iCloud Account: ![Select bridge](./doc/Config_2.png "Step 2") 
3.  Configure your account and the desired refresh rate. ![Configure](./doc/Config_3.png "Step 3") 

# Logging

Use the following command in a console session to change the loglevel of the binding to DEBUG:

```
 log:set DEBUG org.openhab.binding.icloud
```

See also [OpenHAB documentation](http://docs.openhab.org/administration/logging.html) .