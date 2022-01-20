# MELCloud Binding

This is an openHAB binding for Mitsubishi MELCloud (https://www.melcloud.com/). 
Installing this binding you can control your Mitsubishi devices from openHAB without accessing the MELCloud App and benefiting from all openHAB automations.

## Supported Things

Supported thing types

* melcloudaccount (bridge)
* acdevice
* heatpumpdevice

A bridge is required to connect to your MELCloud account.


## Discovery

Discovery is used _after_ a bridge has been created and configured with your login information.

1. Add the binding
2. Add a new thing of type melcloudaccount and configure with username and password
3. Go to Inbox and start discovery devices using MELCloud Binding
4. Supported devices (A.C. Device, Heatpump Device) should appear in your inbox

Binding support also manual thing configuration by thing files.

## Thing Configuration

In order to manually create a thing file and not use the discovery routine you will need to know device MELCloud device ID.
This is a bit difficult to get. The easiest way of getting this is enable debug level logging of the binding or discovery devices by the binding (discovered device can be removed afterwards).

MELCloud account configuration:

| Config   | Mandatory | Description                             |
|----------|-----------|-----------------------------------------|
| username | x         | Email address tied to MELCloud account. |
| password | x         | Password to MELCloud account.           |
| language |           | Language ID, see table below.           |

| LanguageId  | Language          |
|-------------|-------------------|
| 0           | English (default) |
| 1           | Bulgarian         |
| 2           | Czech             |
| 3           | Danish            |
| 4           | German            |
| 5           | Estonian          |
| 6           | Spanish           |
| 7           | French            |
| 8           | Armenian          |
| 9           | Latvian           |
| 10          | Lithuanian        |
| 11          | Hungarian         |
| 12          | Dutch             |
| 13          | Norwegian         |
| 14          | Polish            |
| 15          | Portuguese        |
| 16          | Russian           |
| 17          | Finnish           |
| 18          | Swedish           |
| 19          | Italian           |
| 20          | Ukrainian         |
| 21          | Turkish           |
| 22          | Greek             |
| 23          | Croatian          |
| 24          | Romanian          |
| 25          | Slovenian         |


A.C. device and Heatpump device configuration:

| Config          | Mandatory | Description                                                                           |
|-----------------|-----------|---------------------------------------------------------------------------------------|
| deviceID        | x         | MELCloud device ID.                                                                   |
| buildingID      |           | MELCloud building ID. If not defined, binding tries to find matching id by device ID. |
| pollingInterval |           | Refresh time interval in seconds for updates from MELCloud.  Defaults to 60 seconds.  |



## Channels

A.C. device channels

| Channel             | Type               | Description                                                                              | Read Only |
|---------------------|--------------------|------------------------------------------------------------------------------------------|-----------|
| power               | Switch             | Power Status of Device.                                                                  | False     |
| operationMode       | String             | Operation mode: "1" = Heat, "2" = Dry, "3" = Cool, "7" = Fan, "8" = Auto.                | False     |
| setTemperature      | Number:Temperature | Set Temperature: Min = 10, Max = 40.                                                     | False     |
| fanSpeed            | String             | Fan speed: "0" = Auto, "1" = 1, "2" = 2, "3" = 3, "4" = 4, "5" = 5.                      | False     |
| vaneHorizontal      | String             | Vane Horizontal: "0" = Auto, "1" = 1, "2" = 2, "3" = 3, "4" = 4, "5" = 5, "12" = Swing.  | False     |
| vaneVertical        | String             | Vane Vertical: "0" = Auto, "1" = 1, "2" = 2, "3" = 3, "4" = 4, "5" = 5, "7" = Swing.     | False     |
| roomTemperature     | Number:Temperature | Room temperature.                                                                        | True      |
| lastCommunication   | DateTime           | Last Communication time when MELCloud communicated to the device.                        | True      |
| nextCommunication   | DateTime           | Next communication time when MELCloud will communicate to the device.                    | True      |
| offline             | Switch             | Is device in offline state.                                                              | True      |
| hasPendingCommand   | Switch             | Device has a pending command(s).                                                         | True      |

Heatpump device channels

| Channel             | Type               | Description                                                                | Read Only |
|---------------------|--------------------|----------------------------------------------------------------------------|-----------|
| power               | Switch             | Power Status of Device.                                                    | False     |
| forcedHotWaterMode  | Switch             | If water mode is Heat Now (true) or Auto (false)                           | False     |
| setTemperatureZone1 | Number:Temperature | Set Temperature Zone 1: Min = 10, Max = 30.                                | False     |
| roomTemperatureZone1| Number:Temperature | Room temperature Zone 1.                                                   | True      |
| tankWaterTemperature| Number:Temperature | Tank water temperature.                                                    | True      |
| lastCommunication   | DateTime           | Last Communication time when MELCloud communicated to the device.          | True      |
| nextCommunication   | DateTime           | Next communication time when MELCloud will communicate to the device.      | True      |
| offline             | Switch             | Is device in offline state.                                                | True      |
| hasPendingCommand   | Switch             | Device has a pending command(s).                                           | True      |

## Full Example for items configuration

**melcloud.things**

```
Bridge melcloud:melcloudaccount:myaccount "My MELCloud account" [ username="user.name@example.com", password="xxxxxx", language="0" ] {
	Thing acdevice livingroom "Livingroom A.C. device" [ deviceID=123456, pollingInterval=60 ]
	Thing heatpumpdevice attic "Attic Heatpump device" [ deviceID=789012, pollingInterval=60 ]
}
```

**melcloud.items**

```
Switch      power               { channel="melcloud:acdevice:myaccount:livingroom:power" }
String      operationMode       { channel="melcloud:acdevice:myaccount:livingroom:operationMode" }
Number      setTemperature      { channel="melcloud:acdevice:myaccount:livingroom:setTemperature" }
String      fanSpeed            { channel="melcloud:acdevice:myaccount:livingroom:fanSpeed" }
String      vaneHorizontal      { channel="melcloud:acdevice:myaccount:livingroom:vaneHorizontal" }
String      vaneVertical        { channel="melcloud:acdevice:myaccount:livingroom:vaneVertical" }
Number      roomTemperature     { channel="melcloud:acdevice:myaccount:livingroom:roomTemperature" }
DateTime    lastCommunication   { channel="melcloud:acdevice:myaccount:livingroom:lastCommunication" }
DateTime    nextCommunication   { channel="melcloud:acdevice:myaccount:livingroom:nextCommunication" }
Switch      offline             { channel="melcloud:acdevice:myaccount:livingroom:offline" }
Switch      hasPendingCommand   { channel="melcloud:acdevice:myaccount:livingroom:hasPendingCommand" }

Switch      heatpumpPower               { channel="melcloud:heatpumpdevice:myaccount:attic:power" }
Switch      heatpumpForcedHotWaterMode  { channel="melcloud:heatpumpdevice:myaccount:attic:forcedHotWaterMode" }
Number      heatpumpSetTemperatureZone1 { channel="melcloud:heatpumpdevice:myaccount:attic:setTemperatureZone1" }
Number      heatpumpRoomTemperatureZone1{ channel="melcloud:heatpumpdevice:myaccount:attic:roomTemperatureZone1" }
Number      heatpumpTankWaterTemperature{ channel="melcloud:heatpumpdevice:myaccount:attic:tankWaterTemperature" }
DateTime    heatpumpLastCommunication   { channel="melcloud:heatpumpdevice:myaccount:attic:lastCommunication" }
DateTime    heatpumpNextCommunication   { channel="melcloud:heatpumpdevice:myaccount:attic:nextCommunication" }
Switch      heatpumpOffline             { channel="melcloud:heatpumpdevice:myaccount:attic:offline" }
Switch      heatpumpHasPendingCommand   { channel="melcloud:heatpumpdevice:myaccount:attic:hasPendingCommand" }
```
