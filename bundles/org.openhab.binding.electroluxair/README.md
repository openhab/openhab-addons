# ElectroluxAir Binding

This is an openHAB binding for the Pure A9 Air Purifier, by Electrolux.

![Electrolux Pure A9](doc/electrolux_pure_a9.png)

## Supported Things

This binding supports the following thing types:

- api: Bridge - Implements the API that is used to communicate with the Air Purifier

- electroluxpurea9: The Pure A9 Air Purifier

## Discovery

After the configuration of the Bridge, your Electrolux Pure A9 device will be automatically discovered and placed as a thing in the inbox.

### Configuration Options

Only the bridge require manual configuration. The Electrolux Pure A9 thing can be added by hand, or you can let the discovery mechanism automatically find it.

#### Bridge

| Parameter | Description                                                  | Type   | Default  | Required |
|-----------|--------------------------------------------------------------|--------|----------|----------|
| username  | The username used to connect to the Electrolux app           | String | NA       | yes      |        
| password  | The password used to connect to the Electrolux app           | String | NA       | yes      |
| refresh   | Specifies the refresh interval in second                     | Number | 600      | yes      |

#### Electrolux Pure A9

| Parameter | Description                                                             | Type   | Default  | Required |
|-----------|-------------------------------------------------------------------------|--------|----------|----------|
| deviceId  | Product ID of your Electrolux Pure A9 found in Electrolux app           | Number | NA       | yes      |

## Channels

### Electrolux Pure A9

The following channels are supported:

| Channel Type ID             | Item Type             | Description                                                                    |
|-----------------------------|-----------------------|--------------------------------------------------------------------------------|
| temperature                 | Number:Temperature    | This channel reports the current temperature.                                  |
| humidity                    | Number:Dimensionless  | This channel reports the current humidity in percentage.                       |
| tvoc                        | Number:Density        | This channel reports the total Volatile Organic Compounds in microgram/m3.     |
| pm1                         | Number:Dimensionless  | This channel reports the Particulate Matter 1 in ppb.                          |
| pm2_5                       | Number:Dimensionless  | This channel reports the Particulate Matter 2.5 in ppb.                        |
| pm10                        | Number:Dimensionless  | This channel reports the Particulate Matter 10 in ppb.                         |
| co2                         | Number:Dimensionless  | This channel reports the CO2 level in ppm.                                     |
| fanSpeed                    | Number                | This channel sets and reports the current fan speed (1-9).                     |
| filterLife                  | Number:Dimensionless  | This channel reports the remaining filter life in %.                           |
| ionizer                     | Switch                | This channel sets and reports the status of the Ionizer function (On/Off).     |
| doorOpen                    | Contact               | This channel reports the status of door (Opened/Closed).                       |
| workMode                    | String                | This channel sets and reports the current work mode (Auto, Manual, PowerOff.)  |
| uiLIght                     | Switch                | This channel sets and reports the status of the UI Light function (On/Off).    |
| safetyLock                  | Switch                | This channel sets and reports the status of the Safety Lock  function (On/Off).|

## Full Example

### Things-file

```java
// Bridge configuration
Bridge electroluxair:api:myAPI "Electrolux Delta API" [username="user@password.com", password="12345", refresh="300"] {

     Thing electroluxpurea9 myElectroluxPureA9  "Electrolux Pure A9"    [ deviceId="123456789" ]
     
}
```

## Items-file

```java
// CO2
Number ElectroluxAirCO2 "Electrolux Air CO2 [%d ppm]" {channel="electroluxair:electroluxpurea9:myAPI:MyElectroluxPureA9:co2"}
// Temperature
Number:Temperature ElectroluxAirTemperature "Electrolux Air Temperature" {channel="electroluxair:electroluxpurea9:myAPI:myElectroluxPureA9:temperature"}
// Door status
Contact ElectroluxAirDoor "Electrolux Air Door Status" {channel="electroluxair:electroluxpurea9:myAPI:myElectroluxPureA9:doorOpen"}
// Work mode
String ElectroluxAirWorkModeSetting "ElectroluxAir Work Mode Setting" {channel="electroluxair:electroluxpurea9:myAPI:myElectroluxPureA9:workMode"}
// Fan speed
Number ElectroluxAirFanSpeed "Electrolux Air Fan Speed Setting" {channel="electroluxair:electroluxpurea9:myAPI:myElectroluxPureA9:fanSpeed"}
// UI Light
Switch ElectroluxAirUILight "Electrolux Air UI Light Setting" {channel="electroluxair:electroluxpurea9:myAPI:myElectroluxPureA9:uiLight"}
// Ionizer
Switch ElectroluxAirIonizer "Electrolux Air Ionizer Setting" {channel="electroluxair:electroluxpurea9:myAPI:myElectroluxPureA9:ionizer"}
// Safety Lock
Switch ElectroluxAirSafetyLock "Electrolux Air Safety Lock Setting" {channel="electroluxair:electroluxpurea9:myAPI:myElectroluxPureA9:safetyLock"}
```
