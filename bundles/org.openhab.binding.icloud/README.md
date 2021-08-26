# iCloud Binding

The Apple [iCloud](https://www.apple.com/icloud/) is used to retrieve data such as the battery level or current location of one or multiple Apple devices connected to an iCloud account.
Updates are quick and accurate without significant battery time impact.
The Binding also offers access to the "Find My iPhone" function.

An Apple account is required.
Two factor authentication is supported.

Please note: Application specific passwords are not supported.
You will need to provide the account password to the Binding.

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
However this `REFRESH` command will not request data directly from the phone, but it will only make a query to the server.

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
| locationAccuracy   | Number   | Accuracy of the last position report (Advanced Option on UI)                                                                                                        |
| locationLastUpdate | DateTime | Timestamp of the last location update  (Advanced Option on UI)                                                                                                       |

## Full Example

### icloud.things

```php
Bridge icloud:account:myaccount [appleId="mail@example.com", password="secure", refreshTimeInMinutes=5]
{
    Thing device myiPhone8 "iPhone 8" @ "World" [deviceId="VIRG9FsrvXfE90ewVBA1H5swtwEQePdXVjHq3Si6pdJY2Cjro8QlreHYVGSUzuWV"]
}
```

The device ID can be found in the Inbox after it has been discovered.
The information *@ "World"* is optional.

### icloud.items

```php
Group    iCloud_Group "iPhone"

String   iPhone_BatteryStatus             "Battery Status [%s]" <battery> (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:batteryStatus"}
Number   iPhone_BatteryLevel              "Battery Level [%d %%]"   <battery> (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:batteryLevel"}
Switch   iPhone_FindMyPhone               "Trigger Find My iPhone"           (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:findMyPhone", autoupdate="false"}
Switch   iPhone_Refresh                   "Force iPhone Refresh"             (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:location", autoupdate="false"}
Location iPhone_Location                  "Coordinates"                      (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:location"}
Number   iPhone_LocationAccuracy          "Coordinates Accuracy [%.0f m]"    (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:locationAccuracy"}
DateTime iPhone_LocationLastUpdate        "Last Update [%1$td.%1$tm.%1$tY, %1$tH:%1$tM]" <time> (iCloud_Group) {channel="icloud:device:myaccount:myiPhone8:locationLastUpdate"}
Switch   iPhone_Home                      "Phone Home"            <presence> (iCloud_Group)
```

### icloud.sitemap

```php
sitemap icloud label="iCloud" {
    Frame item=iCloud_Group {
        Text item=iPhone_BatteryStatus
        Text item=iPhone_BatteryLevel
        Text item=iPhone_Home
        Text item=iPhone_LocationAccuracy
        Text item=iPhone_LocationLastUpdate
        Switch item=iPhone_FindMyPhone mappings=[ ON="Find!" ]
        Switch item=iPhone_Refresh mappings=[ REFRESH='Refresh now' ]
        // Mapview for Basic UI and Applications (Android/iOS)
        Mapview item=iPhone_Location height=10
    }
}
```

### icloud.rules

```php
rule "iPhone Home"
when
    Item iPhone_Location changed
then
    // specify your home location
    val PointType home_location  = new PointType(new DecimalType(51.0), new DecimalType(4.0))
    val PointType phone_location = iPhone_Location.state as PointType
    val int distance = phone_location.distanceFrom(home_location).intValue()
    // specify your preferred radius (in meters)
    if (distance < 200) {
        iPhone_Home.postUpdate(ON)
        logInfo("iPhone Home", "iPhone is at home.")
    } else {
        iPhone_Home.postUpdate(OFF)
        logInfo("iPhone Home", "iPhone is away.")
    }
end
```

Apple, iPhone, and iCloud are registered trademarks of Apple Inc., registered in the U.S. and other countries.
