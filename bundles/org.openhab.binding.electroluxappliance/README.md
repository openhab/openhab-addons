# Electrolux Appliance Binding

This is a binding for Electrolux appliances.

## Supported Things

This binding supports the following thing types:

- api: Bridge - Implements the Electrolux Group API that is used to communicate with the different appliances
- air-purifier: The Electrolux Air Purifier
- washing-machine: The Electrolux Washing Machine

## Discovery

After the configuration of the `api` bridge, your Electrolux appliances will be automatically discovered and placed as a thing in the inbox.

### Configuration Options

Only the bridge requires manual configuration. 
The Electrolux appliance things can be added by hand, or you can let the discovery mechanism automatically find them.

#### `api` Bridge

| Parameter    | Description                                            | Type   | Default  | Required |
|--------------|--------------------------------------------------------|--------|----------|----------|
| apiKey       | Your created API key on developer.electrolux.one       | String | NA       | yes      |        
| refreshToken | Your created refresh token on developer.electrolux.one | String | NA       | yes      |
| refresh      | Specifies the refresh interval in second               | Number | 600      | yes      |

#### `air-purifier` Electrolux Air Purifier

| Parameter    | Description                                                              | Type   | Default  | Required |
|--------------|--------------------------------------------------------------------------|--------|----------|----------|
| serialNumber | Serial Number of your Electrolux appliance found in the Electrolux app   | Number | NA       | yes      |

#### `washing-machine` Electrolux Washing Machine

| Parameter    | Description                                                              | Type   | Default  | Required |
|--------------|--------------------------------------------------------------------------|--------|----------|----------|
| serialNumber | Serial Number of your Electrolux appliance found in the Electrolux app   | Number | NA       | yes      |

## Channels

### Electrolux Air Purifier

The following channels are supported:

| Channel Type ID             | Item Type             | Description                                                                    |
|-----------------------------|-----------------------|--------------------------------------------------------------------------------|
| temperature                 | Number:Temperature    | This channel reports the current temperature.                                  |
| humidity                    | Number:Dimensionless  | This channel reports the current humidity in percentage.                       |
| tvoc                        | Number:Dimensionless  | This channel reports the total Volatile Organic Compounds in ppb.              |
| pm1                         | Number:Density        | This channel reports the Particulate Matter 1 in microgram/m3.                 |
| pm2_5                       | Number:Density        | This channel reports the Particulate Matter 2.5 in microgram/m3.               |
| pm10                        | Number:Density        | This channel reports the Particulate Matter 10 in microgram/m3.                |
| co2                         | Number:Dimensionless  | This channel reports the CO2 level in ppm.                                     |
| fan-speed                   | Number                | This channel sets and reports the current fan speed (1-9).                     |
| filter-life                 | Number:Dimensionless  | This channel reports the remaining filter life in %.                           |
| ionizer                     | Switch                | This channel sets and reports the status of the Ionizer function (On/Off).     |
| door-state                  | Contact               | This channel reports the status of the door (Opened/Closed).                   |
| work-mode                   | String                | This channel sets and reports the current work mode (Auto, Manual, PowerOff.)  |
| ui-light                    | Switch                | This channel sets and reports the status of the UI Light function (On/Off).    |
| safety-lock                 | Switch                | This channel sets and reports the status of the Safety Lock function.          |
| status                      | String                | This channel is used to fetch latest status from the API.                      |

### Electrolux Washing Machine

The following channels are supported:

| Channel Type ID              | Item Type             | Description                                                                    |
|------------------------------|-----------------------|--------------------------------------------------------------------------------|
| door-state                   | Contact               | This channel reports the status of the door (Opened/Closed).                   |
| door-lock                    | Contact               | This channel reports the status of the door lock.                              |
| time-to-start                | Number:Time           | This channel reports the remaining time for a delayed start washing program.   |
| time-to-end                  | Number:Time           | This channel reports the remaining time to the end for a washing program.      |
| cycle-phase                  | String                | This channel reports the washing cycle phase.                                  |
| analog-temperature           | String                | This channel reports the washing temperature.                                  |
| steam-value                  | String                | This channel reports the washing steam value.                                  |
| programs-order               | String                | This channel reports the washing program.                                      |
| analog-spin-speed            | String                | This channel reports the washing spin speed.                                   |
| appliance-state              | String                | This channel reports the appliance state.                                      |
| appliance-mode               | String                | This channel reports the appliance mode.                                       |
| appliance-total-working-time | Number:Time           | This channel reports the total working time for the washing machine.           |
| appliance-ui-sw-version      | String                | This channel reports the appliance UI SW version.                              |
| optisense-result             | String                | This channel reports the optisense result.                                     |
| detergent-extradosage        | String                | This channel reports the detergent extra dosage.                               |
| softener-extradosage         | String                | This channel reports the softener extra dosage.                                |
| water-usage                  | Number:Volume         | This channel reports the water usage in litres.                                |
| total-wash-cycles-count      | Number                | This channel reports the total number of washing cycles.                       |
| status                       | String                | This channel is used to fetch latest status from the API.                      |

## Full Example

### `demo.things` Example

```java
// Bridge configuration
Bridge electroluxappliance:api:myAPI "Electrolux Group API" [apiKey="12345678", refreshToken="12345678", refresh="300"] {
     Thing air-purifier myair-purifier  "Electrolux Pure A9"    [ serialNummber="123456789" ]
}
```

##  `demo.items` Example

```java
// CO2
Number:Dimensionless electroluxapplianceCO2 "Electrolux Air CO2 [%d ppm]" {channel="electroluxappliance:air-purifier:myAPI:myair-purifier:co2"}
// Temperature
Number:Temperature electroluxapplianceTemperature "Electrolux Air Temperature" {channel="electroluxappliance:air-purifier:myAPI:myair-purifier:temperature"}
// Door status
Contact electroluxapplianceDoor "Electrolux Air Door Status" {channel="electroluxappliance:air-purifier:myAPI:myair-purifier:doorOpen"}
// Work mode
String electroluxapplianceWorkModeSetting "electroluxappliance Work Mode Setting" {channel="electroluxappliance:air-purifier:myAPI:myair-purifier:workMode"}
// Fan speed
Number electroluxapplianceFanSpeed "Electrolux Air Fan Speed Setting" {channel="electroluxappliance:air-purifier:myAPI:myair-purifier:fanSpeed"}
// UI Light
Switch electroluxapplianceUILight "Electrolux Air UI Light Setting" {channel="electroluxappliance:air-purifier:myAPI:myair-purifier:uiLight"}
// Ionizer
Switch electroluxapplianceIonizer "Electrolux Air Ionizer Setting" {channel="electroluxappliance:air-purifier:myAPI:myair-purifier:ionizer"}
// Safety Lock
Switch electroluxapplianceSafetyLock "Electrolux Air Safety Lock Setting" {channel="electroluxappliance:air-purifier:myAPI:myair-purifier:safetyLock"}
```
