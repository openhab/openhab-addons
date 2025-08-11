# Electrolux Appliance Binding

This is a binding for Electrolux appliances.

## Supported Things

This binding supports the following thing types:

- api: Bridge - Implements the Electrolux Group API that is used to communicate with the different appliances
- air-purifier: The Electrolux Air Purifier
- washing-machine: The Electrolux Washing Machine
- portable-air-conditioner: A Portable Air Conditioner

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

#### `portable-air-conditioner` Electrolux Portable Air Conditioner

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

### Electrolux Portable Air Conditioner

The following channels are supported:

| Channel Type ID           | Item Type          | Description                                                                                         | Writable                                 |
|---------------------------|--------------------|-----------------------------------------------------------------------------------------------------|------------------------------------------|
| appliance-running         | Switch             | The device's state running state.                                                                   | Yes - On / Off                           |
| ambient-temperature       | Number:Temperature | The measured ambient temperature.                                                                   | No                                       |
| target-temperature        | Number:Temperature | The target set-point temperature.                                                                   | Yes - 16 -> 32                           |
| sleep-mode                | Switch             | Whether sleep mode is active.                                                                       | Yes - On / Off                           |
| fan-swing                 | Switch             | Whether fan swing is active.                                                                        | Yes - On / Off                           |
| child-ui-lock             | Switch             | Whether child lock is active.                                                                       | Yes - On / Off                           |
| fan-mode                  | String             | The fan speed mode.                                                                                 | Yes - AUTO / HIGH / MIDDLE / LOW         |
| mode                      | String             | The operating mode.                                                                                 | Yes - AUTO / COOL / DRY / FANONLY        |
| network-quality-indicator | String             | Indicator for the network quality.                                                                  | No                                       |
| network-rssi              | Number:Power       | WiFi Received Signal Strength Indicator.                                                            | No                                       |
| compressor-state          | Switch             | Is the compressor running.                                                                          | No                                       |
| fourway-valve-state       | Switch             | The state of the four way valve.                                                                    | No                                       |
| evap-defrost-state        | Switch             | The state of the evap defrost.                                                                      | No                                       |
| off-timer-active          | Switch             | Whether a timer is active to turn off the appliance.                                                | Yes - When on applies off-timer-duration |
| off-timer-duration        | Number:Time        | Whether a timer is active to turn off the appliance. (Applied when off-timer-active is switched on) | Yes - to set time for off-timer-active   |
| off-timer-time            | DateTime           | The time when the auto off timer will be reached.                                                   | No                                       |
| on-timer-active           | Switch             | Whether a timer is active to turn on the appliance.                                                 | Yes - When on applies on-timer-duration  |
| on-timer-duration         | Number:Time        | Whether a timer is active to turn on the appliance. (Applied when on-timer-active is switched on)   | Yes - to set time for on-timer-active    |
| on-timer-time             | DateTime           | The time when the auto on timer will be reached.                                                    | No                                       |
| filter-state              | String             | The air filters state.                                                                              | No                                       |

## Full Example

### `demo.things` Example

```java
// Bridge configuration
Bridge electroluxappliance:api:myAPI "Electrolux Group API" [apiKey="12345678", refreshToken="12345678", refresh="300"] {
     Thing air-purifier             myair-purifier                "Electrolux Pure A9"    [ serialNumber="123456789" ]
     Thing portable-air-conditioner myportable-air-con            "AEG Comfort 6000"      [ serialNumber="234567891" ]   
}
```

## `demo.items` Example - Air Purifier

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

## `demo.items` Example - Portable Air Conditioner

```java
Group Electrolux_Air_Conditioner "Electrolux Air Conditioner" [AirConditioner]
Number:Temperature Electrolux_Air_Conditioner_Ambient_Temperature "Ambient Temperature [%.1f %unit%]" <temperature> (Electrolux_Air_Conditioner) [Measurement, Temperature] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:ambient-temperature", unit="°C" }
Switch Electrolux_Air_Conditioner_Child_Lock "Child Lock" <lock> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:child-ui-lock" }
Switch Electrolux_Air_Conditioner_Compressor_Running "Compressor Running" <switch> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:compressor-state" }
Switch Electrolux_Air_Conditioner_Evap_Defrost_State "Evap Defrost State" <switch> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:evap-defrost-state" }
String Electrolux_Air_Conditioner_Fan_Speed "Fan Speed" <flow> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:fan-mode" }
Switch Electrolux_Air_Conditioner_Fan_Swing "Fan Swing" <flow> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:fan-swing" }
Switch Electrolux_Air_Conditioner_Four_Way_Valve_State "Four Way Valve State" <switch> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:fourway-valve-state" }
String Electrolux_Air_Conditioner_Mode "Mode" <settings> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:mode" }
String Electrolux_Air_Conditioner_Network_Quality "Network Quality" <network> (Electrolux_Air_Conditioner) [SignalStrength, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:network-quality-indicator" }
Switch Electrolux_Air_Conditioner_Powered_On "Powered On" <switch> (Electrolux_Air_Conditioner) [Mode, Switch] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:appliance-running" }
Number:Power Electrolux_Air_Conditioner_RSSI "RSSI" <qualityOfService> (Electrolux_Air_Conditioner) [Point] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:network-rssi", unit="dBm" }
Switch Electrolux_Air_Conditioner_Sleep_Mode "Sleep Mode" <switch> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:sleep-mode" }
Number:Temperature Electrolux_Air_Conditioner_Target_Temperature "Target Temperature [%.1f %unit%]" <temperature> (Electrolux_Air_Conditioner) [Status, Temperature] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:target-temperature", unit="°C" }
Switch Electrolux_Air_Conditioner_Timer_Off_Activate "Timer Off Activate" <switch> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:off-timer-active" }
Number:Time Electrolux_Air_Conditioner_Timer_Off_Duration "Timer Off Duration [%.1f %unit%]" <settings> (Electrolux_Air_Conditioner) [Point] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:off-timer-duration", unit="s" }
DateTime Electrolux_Air_Conditioner_Offtimertime "Auto Off Expiry [%1$tF %1$tR]" <time> (Electrolux_Air_Conditioner) [Status, Timestamp] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:off-timer-time" }
Switch Electrolux_Air_Conditioner_Timer_On_Activate "Timer On Activate" <switch> (Electrolux_Air_Conditioner) [Mode, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:on-timer-active" }
Number:Time Electrolux_Air_Conditioner_Timer_On_Duration "Timer On Duration [%.1f %unit%]" <settings> (Electrolux_Air_Conditioner) [Point] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:on-timer-duration", unit="s" }
DateTime Electrolux_Air_Conditioner_Ontimertime "Auto On Expiry [%1$tF %1$tR]" <time> (Electrolux_Air_Conditioner) [Status, Timestamp] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:on-timer-time" }
String Electrolux_Air_Conditioner_Filter_State "Filter State" <text> (Electrolux_Air_Conditioner) [Info, Status] { channel="electroluxappliance:portable-air-conditioner:myAPI:myportable-air-con:filter-state" }
```
