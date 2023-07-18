# Bosch Indego Binding

This is the Binding for Bosch Indego Connect lawn mowers.
Thank´s to zazaz-de who found out how the API works.
His [Java Library](https://github.com/zazaz-de/iot-device-bosch-indego-controller) made this Binding possible.

## Discovery

When the bridge is authorized, the binding can automatically discover Indego mowers connected to the SingleKey ID account.

## Thing Configuration

### `account` Bridge Configuration

There are no parameters for the bridge.
However, the bridge is used for managing the [SingleKey ID](https://singlekey-id.com/) digital identity.

#### Authorization

To authorize, please follow these steps:

- In your browser, go to the [Bosch Indego login page](https://prodindego.b2clogin.com/prodindego.onmicrosoft.com/b2c_1a_signup_signin/oauth2/v2.0/authorize?redirect_uri=com.bosch.indegoconnect://login&client_id=65bb8c9d-1070-4fb4-aa95-853618acc876&response_type=code&scope=openid%20offline_access%20https://prodindego.onmicrosoft.com/indego-mobile-api/Indego.Mower.User).
- Select "Bosch ID", enter your e-mail address and password and click "Log-in".
- In your browser, open Developer Tools.
- With developer tools showing on the right, go to [Bosch Indego login page](https://prodindego.b2clogin.com/prodindego.onmicrosoft.com/b2c_1a_signup_signin/oauth2/v2.0/authorize?redirect_uri=com.bosch.indegoconnect://login&client_id=65bb8c9d-1070-4fb4-aa95-853618acc876&response_type=code&scope=openid%20offline_access%20https://prodindego.onmicrosoft.com/indego-mobile-api/Indego.Mower.User) again.
- "Please wait..." should now be displayed.
- Find the `authresp` and copy the code: `com.bosch.indegoconnect://login/?code=<copy this>`
- Use the openHAB console to authorize with this code: `openhab:boschindego authorize <paste code>`

### `indego` Thing Configuration

| Parameter          | Description                                                       | Default | Required |
|--------------------|-------------------------------------------------------------------|---------|----------|
| serialNumber       | The serial number of the connected Indego mower                   |         | yes      |
| refresh            | The number of seconds between refreshing device state when idle   | 180     | no       |
| stateActiveRefresh | The number of seconds between refreshing device state when active | 30      | no       |
| cuttingTimeRefresh | The number of minutes between refreshing last/next cutting time   | 60      | no       |

## Channels

| Channel            | Item Type                | Description                                                                                                                         | Writeable |
|--------------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------|-----------|
| state              | Number                   | You can send commands to this channel to control the mower and read the simplified state from it (1=mow, 2=return to dock, 3=pause) | Yes       |
| errorcode          | Number                   | Error code of the mower (0=no error)                                                                                                |           |
| statecode          | Number                   | Detailed state of the mower                                                                                                         |           |
| textualstate       | String                   | State as a text.                                                                                                                    |           |
| ready              | Number                   | Shows if the mower is ready to mow (1=ready, 0=not ready)                                                                           |           |
| mowed              | Dimmer                   | Cut grass in percent                                                                                                                |           |
| lastCutting        | DateTime                 | Last cutting time                                                                                                                   |           |
| nextCutting        | DateTime                 | Next scheduled cutting time                                                                                                         |           |
| batteryVoltage     | Number:ElectricPotential | Battery voltage reported by the device<sup>1</sup>                                                                                  |           |
| batteryLevel       | Number                   | Battery level as a percentage (0-100%)<sup>1</sup>                                                                                  |           |
| lowBattery         | Switch                   | Low battery warning with possible values on (low battery) and off (battery ok)<sup>1</sup>                                          |           |
| batteryTemperature | Number:Temperature       | Battery temperature reported by the device<sup>1</sup>                                                                              |           |
| gardenSize         | Number:Area              | Garden size mapped by the device                                                                                                    |           |
| gardenMap          | Image                    | Garden map created by the device<sup>2</sup>                                                                                        |           |

<sup>1)</sup> This will be updated every six hours when the device is idle. It will wake up the device, which can include turning on its display. When the device is active or charging, this will be updated every two minutes.

<sup>2)</sup> This will be updated as often as specified by the `stateActiveRefresh` thing parameter.

### State Codes

| Code  | Description                                 |
|-------|---------------------------------------------|
| 0     | Reading status                              |
| 257   | Charging                                    |
| 258   | Docked                                      |
| 259   | Docked - Software update                    |
| 260   | Docked                                      |
| 261   | Docked                                      |
| 262   | Docked - Loading map                        |
| 263   | Docked - Saving map                         |
| 266   | Leaving dock                                |
| 513   | Mowing                                      |
| 514   | Relocalising                                |
| 515   | Loading map                                 |
| 516   | Learning lawn                               |
| 517   | Paused                                      |
| 518   | Border cut                                  |
| 519   | Idle in lawn                                |
| 523   | SpotMow                                     |
| 524   | Mowing randomly                             |
| 768   | Returning to dock                           |
| 769   | Returning to dock                           |
| 770   | Returning to dock                           |
| 771   | Returning to dock - Battery low             |
| 772   | Returning to dock - Calendar timeslot ended |
| 773   | Returning to dock - Battery temp range      |
| 774   | Returning to dock                           |
| 775   | Returning to dock - Lawn complete           |
| 776   | Returning to dock - Relocalising            |
| 1025  | Diagnostic mode                             |
| 1026  | End of life                                 |
| 1281  | Software update                             |
| 1537  | Energy save mode                            |
| 64513 | Docked                                      |

## Full Example

### `indego.things` File

```java
Bridge boschindego:account:singlekey {
    Things:
        Thing indego lawnmower [serialNumber="1234567890", refresh=120]
}
```

### `indego.items` File

```java
Number                   Indego_State              { channel="boschindego:indego:singlekey:lawnmower:state" }
Number                   Indego_ErrorCode          { channel="boschindego:indego:singlekey:lawnmower:errorcode" }
Number                   Indego_StateCode          { channel="boschindego:indego:singlekey:lawnmower:statecode" }
String                   Indego_TextualState       { channel="boschindego:indego:singlekey:lawnmower:textualstate" }
Number                   Indego_Ready              { channel="boschindego:indego:singlekey:lawnmower:ready" }
Dimmer                   Indego_Mowed              { channel="boschindego:indego:singlekey:lawnmower:mowed" }
DateTime                 Indego_LastCutting        { channel="boschindego:indego:singlekey:lawnmower:lastCutting" }
DateTime                 Indego_NextCutting        { channel="boschindego:indego:singlekey:lawnmower:nextCutting" }
Number:ElectricPotential Indego_BatteryVoltage     { channel="boschindego:indego:singlekey:lawnmower:batteryVoltage" }
Number                   Indego_BatteryLevel       { channel="boschindego:indego:singlekey:lawnmower:batteryLevel" }
Switch                   Indego_LowBattery         { channel="boschindego:indego:singlekey:lawnmower:lowBattery" }
Number:Temperature       Indego_BatteryTemperature { channel="boschindego:indego:singlekey:lawnmower:batteryTemperature" }
Number:Area              Indego_GardenSize         { channel="boschindego:indego:singlekey:lawnmower:gardenSize" }
Image                    Indego_GardenMap          { channel="boschindego:indego:singlekey:lawnmower:gardenMap" }
```

### `indego.sitemap` File

```perl
Switch item=Indego_State mappings=[1="Mow", 2="Return",3="Pause"]
```
