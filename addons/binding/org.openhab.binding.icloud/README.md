# iCloud Binding

The Apple [iCloud](https://www.apple.com/icloud/) is used to retrieve data such as the battery level or current location of one or multiple Apple devices connected to an iCloud account.
Updates are quick and accurate without significant battery time impact.
The Binding also offers access to the "Find My iPhone" function.

An Apple account is required.
Two factor authentication is supported.

Please note: Application specific passwords are not supported.
You'll need to provide the account password to the Binding.

## Discovery

An iCloud account can be added as a Thing and needs to be configured with your ID and password.
The devices registered to this account will then be automatically discovered.

## Binding Configuration

The Binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The Binding provides two Thing types.

### Account Thing

The account Thing, more precisely the account Bridge, represents one Apple iCloud account.
The account can be connected to multiple Apple devices which are represented as Things below the Bridge, see the example below.
You may create multiple account Things for multiple accounts.

### Device Thing

A device is identified by the device ID provided by Apple.
If a device is removed or disconnects from the account the respective openHAB device Thing status will change to `OFFLINE`.

All Things are updated according to the configured refresh time of their shared account Bridge.
You may force an update by sending the `REFRESH` command to any channel of the Bridge Things.

## Channels

### Account Thing

The account Thing does not provide any channels.

### Device Thing

The following channels are available (if supported by the device):

| Channel ID         | Type     | Description                                                                                                                                 |
|--------------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------|
| batteryStatus      | String   | Current battery status (Charging, NotCharging, Charged, Unknown)                                                                            |
| batteryLevel       | Number   | Battery charge in percent                                                                                                                         |
| findMyPhone        | Switch   | Triggers the ["Find My Phone"](https://support.apple.com/explore/find-my-iphone-ipad-mac-watch) functionality of the device (if available). |
| location           | Location | GPS coordinates of the devices current/last known location                                                                                  |
| locationAccuracy   | Number   | Accuracy of the last position report                                                                                                        |
| locationLastUpdate | DateTime | Timestamp of the last location update                                                                                                       |

## Full Example

### icloud.things

```php
Bridge icloud:account:myaccount [appleId="abc@xyz.tld", password="secure", refreshTimeInMinutes=5]
{
    Thing device myiPhone8 "iPhone 8" @ "World" [deviceId="VIRG9FsrvXfE90ewVBA1H5swtwEQePdXVjHq3Si6pdJY2Cjro8QlreHYVGSUzuWV"]
}
```

### icloud.items

```php
Group    iCloud_Group

String   iPhone_BatteryStatus             "Battery Status [%s %%]"             <battery> (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:batteryStatus"}
Number   iPhone_BatteryLevel              "Battery Level [%.0f]"               <battery> (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:batteryLevel"}
Switch   iPhone_FindMyPhone               "Trigger Find My iPhone"                       (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:findMyPhone"}
Location iPhone_Location                  "Coordinates"                                  (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:location"}
Number   iPhone_LocationAccuracy          "Coordinates Accuracy [%.0f m]"                (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:locationAccuracy"}
DateTime iPhone_LocationLastUpdate        "Last Update [%1$td.%1$tm.%1$tY, %1$tH:%1$tM]" (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:locationLastUpdate"}
```

Apple, iPhone, and iCloud are registered trademarks of Apple Inc., registered in the U.S. and other countries.
