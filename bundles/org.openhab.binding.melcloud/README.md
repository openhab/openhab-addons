
# MELCloud Binding

This is an openHAB binding for Mitsubishi MELCloud (https://www.melcloud.com/). 
Installing this binding you can control your Mitsubishi devices from openHAB without accessing the MELCloud App and benefiting from all openHAB automations.

## Supported Things

Supported thing types

* melcloudaccount (bridge)
* acdevice

A bridge is required to connect to your MELCloud account.


## Discovery

Discovery is used _after_ a bridge has been created and configured with your login information.

1. Add the binding
2. Add a new thing of type melcloudaccount and configure with username and password
3. Go to Inbox and start discovery of A.C. devices using MELCloud Binding
4. A.C. devices should appear in your inbox

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


A.C. device configuration:

| Config          | Mandatory | Description                                                                           |
|-----------------|-----------|---------------------------------------------------------------------------------------|
| deviceID        | x         | MELCloud device ID.                                                                   |
| buildingID      |           | MELCloud building ID. If not defined, binding tries to find matching id by device ID. |
| pollingInterval |           | Refresh time interval in seconds for updates from MELCloud.  Defaults to 60 seconds.  |



## Channels

A.C. device channels

| Channel             | Type     | Description                                                                | Read Only |
|---------------------|----------|----------------------------------------------------------------------------|-----------|
| power               | Switch   | Power Status of Device.                                                    | False     |
| operationMode       | Number   | Operation mode: 1 = Heat, 2 = Dry, 3 = Cool, 7 = Fan, 8 = Auto.            | False     |
| setTemperature      | Number   | Set Temperature: Min = 10, Max = 40.                                       | False     |
| fanSpeed            | Number   | Fan speed: 0 = Auto, 1 = 1, 2 = 2, 3 = 3, 4 = 4, 5 = 5.                    | False     |
| vaneHorizontal      | Number   | Vane Horizontal: 0 = Auto, 1 = 1, 2 = 2, 3 = 3, 4 = 4, 5 = 5, 12 = Swing.  | False     |
| vaneVertical        | Number   | Vane Vertical: 0 = Auto, 1 = 1, 2 = 2, 3 = 3, 4 = 4, 5 = 5, 7 = Swing.     | False     |
| roomTemperature     | Number   | Room temperature.                                                          | True      |
| lastCommunication   | DateTime | Last Communication time when MELCloud communicated to the device.          | True      |
| nextCommunication   | DateTime | Next communication time when MELCloud will communicate to the device.      | True      |
| offline             | Switch   | Is device in offline state.                                                | True      |
| hasPendingCommand   | Switch   | Device has a pending command(s).                                           | True      |


## Full Example for items configuration

**melcloud.things**

```
Bridge melcloud:melcloudaccount:myaccount "My MELCloud account" [ username="user.name@email.com", password="xxxxxx", language="0" ] {
    Thing acdevice livingroom "Livingroom A.C. device" [ deviceID=123456, pollingInterval=60 ]
}
```

**melcloud.items**

```
Switch      power               { channel="melcloud:acdevice:myaccount:livingroom:power" }
Number      operationMode       { channel="melcloud:acdevice:myaccount:livingroom:operationMode" }
Number      setTemperature      { channel="melcloud:acdevice:myaccount:livingroom:setTemperature" }
Number      fanSpeed            { channel="melcloud:acdevice:myaccount:livingroom:fanSpeed" }
Number      vaneHorizontal      { channel="melcloud:acdevice:myaccount:livingroom:vaneHorizontal" }
Number      vaneVertical        { channel="melcloud:acdevice:myaccount:livingroom:vaneVertical" }
Number      roomTemperature     { channel="melcloud:acdevice:myaccount:livingroom:roomTemperature" }
DateTime    lastCommunication   { channel="melcloud:acdevice:myaccount:livingroom:lastCommunication" }
DateTime    nextCommunication   { channel="melcloud:acdevice:myaccount:livingroom:nextCommunication" }
Switch      offline             { channel="melcloud:acdevice:myaccount:livingroom:offline" }
Switch      hasPendingCommand   { channel="melcloud:acdevice:myaccount:livingroom:hasPendingCommand" }
```
