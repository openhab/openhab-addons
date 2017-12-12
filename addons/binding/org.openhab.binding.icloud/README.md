# iCloud Binding

The Apple [iCloud](https://www.apple.com/icloud/) is used to retrieve data such as the battery level or current location of one or multiple Apple devices connected to an iCloud account.
Updates are quick and accurate without significant battery time impact.
The Binding also offers access to the "Find My iPhone" function.

An Apple account is required. Two factor authentication is supported.

Please note: Application specific passwords are not supported.
You´ll need to provide the account password to the binding.

## Supported Things

The following devices are known to work with this Binding:

* iPhone 6s
* iPhone 5c
* iPhone 7
* Apple Watch Series 2
* iPad Air 2/2017/Pro
* MacBook Pro

Other devices connected to your iCloud account should work as well.
Please provide feedback if you have tested another device type.

## Discovery

For each iCloud account to be included an iCloud Binding Thing needs to be configured with your ID and password.
The devices registered to this account will then be automatically discovered.

## Binding Configuration

The Binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The Binding provides two Thing types.

### iCloud Account (Bridge) Thing

The following table lists the configuration parameters:

| Parameter            | Description                                         |                             |
|----------------------|-----------------------------------------------------|-----------------------------|
| appleId              |                                                     | mandatory                   |
| password             |                                                     | mandatory                   |
| refreshTimeInMinutes |                                                     | optional, 5 minutes default |

A device is identified by a hash value calculated from the device id provided by apple.
If a device is removed from the account the respective openHAB Thing will go OFFLINE.

## Channels

### Account

| Channel ID      | Type   | Description                                                  |
|-----------------|--------|--------------------------------------------------------------|
| owner           | String | Registered owner of this iCloud account.                     |
| refresh         | Switch | Update all devices registered with this account immediately. |

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
| deviceName                   | String   | The device name assigned to the device through device settings.                                                                             |

## Full Example

### icloud.things

```php
Bridge icloud:account:myaccount [appleId="abc@xyz.tld", password="secure", refreshTimeInMinutes=5, googleAPIKey="abc123"]
{
    Thing device myiPhone8 "iPhone 8" @ "World" [deviceId="VIRG9FsrvXfE90ewVBA1H5swtwEQePdXVjHq3Si6pdJY2Cjro8QlreHYVGSUzuWV"]
}
```

The "label" @ "location" part is optional (as always).

### icloud.items

```php
Group    iCloud_Group

String   iCloud_Account_Owner             "iCloud Account Owner [%s]"                    (iCloud_Group) {channel="icloud:account:myaccount:owner"}
Number   iCloud_Account_NumberOfDevices   "iCloud Account NumberOfDevices [%d]"          (iCloud_Group) {channel="icloud:account:myaccount:numberOfDevices"}
Switch   iCloud_Account_ForceRefresh      "iCloud Account Force Refresh"                 (iCloud_Group) {channel="icloud:account:myaccount:refresh"}

String   iPhone_BatteryStatus             "Battery Status [%s %%]"             <battery> (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:batteryStatus"}
Number   iPhone_BatteryLevel              "Battery Level [%.0f]"               <battery> (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:batteryLevel"}
Switch   iPhone_FindMyPhone               "Trigger Find My iPhone"                       (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:findMyPhone"}
Location iPhone_Location                  "Coordinates"                                  (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:location"}
Number   iPhone_LocationAccuracy          "Coordinates Accuracy [%.0f m]"                (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:locationAccuracy"}
DateTime iPhone_LocationLastUpdate        "Last Update [%1$td.%1$tm.%1$tY, %1$tH:%1$tM]" (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:locationLastUpdate"}
String   iPhone_DeviceName                "Device Name [%s]"                             (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:deviceName"}
```