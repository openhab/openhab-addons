# iCloud Binding

This binding uses the Apple iCloud service for providing device based information such as battery details or location.
To use this binding, you must have an apple account.

## Supported Things

The following devices are known to work with this binding:
* iPhone 6s
* iPhone 5c
* iPhone 7
* iWatch 2
* iPad Air 2/2017/Pro

Other devices should work as well. Please provide feedback if you have tested another device type. 

## Discovery

For each iCloud account to be included in openHAB an iCloud binding (bridge) needs to be configured with your apple id, password & refresh rate.
The devices registered to this account will then be automatically discovered, no need to create devices manually.

## Binding Configuration

Not necessary

## Thing Configuration

First select iCloud Binding in Paper UI - configuration - things and provide your id, password and refresh interval. Devices are automatically discovered once the thing is configured and online.

You can either use the Paper UI or textual configuration in order to link channels to items.
Paper UI configuration:
Each device channel must be linked to items. Select “Create new item” to create items.

### Example: 
Device: iPhone 6s 
Channel: BatteryStatus
Possible Name: iPhone6s_BatteryStatus



Textual configuration: 
Items can be prepared to be linked in Paper UI later. Following example shows the first device (device “0”). Exchange “YourDeviceID” with the real device ID as shown in things.

Example:
icloud:device:be123ef2:0:BatteryStatus
be123ef2 = device ID
0 = first device in account (second device = 1, third device = 2)
BatteryStatus = channel

## Channels

The following channels are available (if supported by the device):

| Channel ID    |Type           |
| ------------- | ------------- |
| BatteryStatus | String        |
| Battery Level | Number        |
| FindMyPhone   | Switch        |
| Location      | Location      |
| Accuracy      | Number        |
| Distance from Home | Number   |
| Last Location Update | DateTime | 
| Street        | String        |
| City          | String        |
| Country       | String        |
| Formatted Address | String    |

## Full Example

1.  Select the binding:  
![Select binding](./doc/Config_1.png "Step 1")
2.  Select the bridge:   
![Select bridge](./doc/Config_2.png "Step 2") 
3.  Configure your account and the desired refresh rate. 
![Configure](./doc/Config_3.png "Step 3") 

icloud.items:

```
String iPhone_Battery_Status "Battery Status [%s]" <battery> (giPhone)  {channel="icloud:device:YourDeviceID:0:BatteryStatus"}
Number iPhone_Battery_Level "Battery Level [%.0f]" <battery> (giPhone) {channel="icloud:device:YourDeviceID:0:BatteryLevel"}
Switch Find_my_iPhone_A "Find iPhone [%s]" <suitcase> (giPhone) {channel="icloud:device:YourDeviceID:0:FindMyPhone"}
Location iPhone_Coordinates "Coordinates" <suitcase> (giPhone)  {channel="icloud:device:YourDeviceID:0:Location"}
Number iPhone_Coordinates_Accuracy "Coordinates Accuracy [%.0f]" <suitcase> (giPhone){channel="icloud:device:YourDeviceID:0:LocationAccuracy"}
Number iPhone_Dist_from_Home "Distance from home [%.0f]" <suitcase> (giPhone_A){channel="icloud:device:YourDeviceID:0:DistanceFromHome"}
DateTime iPhone_Location_Timestamp_A    "Letztes Update [%1$td.%1$tm.%1$tY, %1$tH:%1$tM]"   <suitcase>      (giPhone)   {channel="icloud:device:YourDeviceID:0:LastUpdate"}
String iPhone_Street "Street [%s]" <suitcase> (giPhone) {channel="icloud:device:YourDeviceID e789ef3:0:AddressStreet"}
String iPhone_City "City [%s]" <suitcase> (giPhone) {channel="icloud:device:YourDeviceID:0:AddressCity"}
String iPhone_Country "Country [%s]" <suitcase> (giPhone)   {channel="icloud:device:YourDeviceID:0:AddressCountry"}
String iPhone_Address "Address [%s]" <suitcase> (giPhone)   {channel="icloud:device:YourDeviceID:0:FormattedAddress"}
```
