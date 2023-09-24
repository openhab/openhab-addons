# Easee Binding

The Easee binding can be used to retrieve data from the Easee Cloud API and also to control your wallbox via the Cloud API.
This allows you to dynamically adjust the charge current for your car depending on production of your solar plant.

## Supported Things

This binding provides three thing types:

| Thing/Bridge        | Thing Type          | Description                                                                                   |
|---------------------|---------------------|-----------------------------------------------------------------------------------------------|
| bridge              | site                | cloud connection to a site within an Easee account                                            |
| thing               | charger             | the physical charger which is connected to a circuit within the given site                    |
| thing               | mastercharger       | like the "normal" charger but with additional capability to control the circuit               |

Basically any Easee wallbox that supports the Cloud API should automatically be supported by this binding.

## Discovery

Auto-discovery is supported and will discover all circuits and chargers assigned to a given site.

## Bridge Configuration

The following configuration parameters are available for the binding/bridge:

| Configuration Parameter | Required | Description                                                                                                                                                                                                                                                                                                                                       |
|-------------------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| username                | yes      | The username to login at Easee Cloud service. This should be an e-mail address or phone number.                                                                                                                                                                                                                                                   |
| passord                 | yes      | Your password to login at Easee Cloud service.                                                                                                                                                                                                                                                                                                    |
| siteId                  | yes      | The ID of the site containing the wallbox(es) and circuit(s) that should be integrated into openHAB. The ID of your site can be found via the sites overview (<https://easee.cloud/sites>). You just need to click one of the sites listed there, the id will be part of the URL which is then opened. It will be a number with typically 6 digits. |
| dataPollingInterval     | no       | Interval (seconds) in which live data values are retrieved from the Easee Cloud API. (default = 120)                                                                                                                                                                                                                                              |

## Thing configuration

It is recommended to use auto discovery which does not require further configuration.
If manual configuration is preferred you need to specify configuration as below.

### Charger

| Configuration Parameter | Required | Description                                                                                                            |
|-------------------------|----------|------------------------------------------------------------------------------------------------------------------------|
| id                      | yes      | The id of the charger that will be represented by this thing.                                                          |

### Mastercharger

| Configuration Parameter | Required | Description                                                                                                            |
|-------------------------|----------|------------------------------------------------------------------------------------------------------------------------|
| id                      | yes      | The id of the charger that will be represented by this thing.                                                          |
| circuitId               | yes      | The id of the circuit that is controlled by this charger.                                                              |

## Channels

The binding only supports a subset of the available endpoints provided by the Easee Cloud API.
The tables below show all available channels and which of them are writable.
The settings that start with "dynamic" can be changed frequently, the others are written to flash and thus should not be changed too often as this could result in damage of your flash.

### Charger Channels

| Channel                                     | Item Type                | Writable | Description                                          | Allowed Values (write access)                                                                                                                                |
|---------------------------------------------|--------------------------|----------|------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| state#smartCharging                         | Switch                   | yes      |                                                      | ON/OFF                                                                                                                                                   |
| state#cableLocked                           | Switch                   | no       |                                                      |                                                                                                                                                              |
| state#chargerOpMode                         | Number                   | no       | 0=Offline, 1=Disconnected, 2=AwaitingStart, 3=Charging, 4=Completed, 5=Error, 6=ReadyToCharge, 7=AwaitingAuthentication, 8=Deauthenticating |                                                                       |
| state#totalPower                            | Number:Power             | no       | current session total power (all phases)             |                                                                                                                                                              |
| state#sessionEnergy                         | Number:Energy            | no       | current session                                      |                                                                                                                                                              |
| state#energyPerHour                         | Number:Energy            | no       | energy per hour                                      |                                                                                                                                                              |
| state#wiFiRSSI                              | Number:Power             | no       |                                                      |                                                                                                                                                              |
| state#cellRSSI                              | Number:Power             | no       |                                                      |                                                                                                                                                              |
| state#dynamicCircuitCurrentP1               | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| state#dynamicCircuitCurrentP2               | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| state#dynamicCircuitCurrentP3               | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| state#latestPulse                           | DateTime                 | no       |                                                      |                                                                                                                                                              |
| state#chargerFirmware                       | Number                   | no       |                                                      |                                                                                                                                                              |
| state#voltage                               | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inCurrentT2                           | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| state#inCurrentT3                           | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| state#inCurrentT4                           | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| state#inCurrentT5                           | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| state#outputCurrent                         | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT1T2                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT1T3                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT1T4                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT1T5                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT2T3                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT2T4                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT2T5                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT3T4                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT3T5                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#inVoltageT4T5                         | Number:ElectricPotential | no       |                                                      |                                                                                                                                                              |
| state#ledMode                               | Number                   | no       |                                                      |                                                                                                                                                              |
| state#cableRating                           | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| state#isOnline                              | Switch                   | no       |                                                      |                                                                                                                                                              |
| state#dynamicChargerCurrent                 | Number:ElectricCurrent   | yes      |                                                      | 0, 6-32                                                                                                                                                      |
| state#reasonForNoCurrent                    | Number                   | no       | 0=OK, 2=DynamicCircuitCurrentLimitTooLow, 27=DynamicCircuitCurrentCharging, 52=DynamicChargerCurrentLimitTooLow, 55=NotAuthorized, 79=CarLimit, 81=CarLimitedCharging |                                             |
| state#lifetimeEnergy                        | Number:Energy            | no       |                                                      |                                                                                                                                                              |
| state#errorCode                             | Number                   | no       |                                                      |                                                                                                                                                              |
| state#fatalErrorCode                        | Number                   | no       |                                                      |                                                                                                                                                              |
| state#connectedToCloud                      | Switch                   | no       |                                                      |                                                                                                                                                              |
| config#lockCablePermanently                 | Switch                   | yes      |                                                      | true/false                                                                                                                                                   |
| config#authorizationRequired                | Switch                   | yes      |                                                      | true/false                                                                                                                                                   |
| config#limitToSinglePhaseCharging           | Switch                   | yes      |                                                      | true/false                                                                                                                                                   |
| config#phaseMode                            | Number                   | yes      | 1=1phase, 2=auto, 3=3phase                           | 1-3                                                                                                                                                          |
| config#maxChargerCurrent                    | Number:ElectricCurrent   | no       | write access not yet implemented                     |                                                                                                                                                              |
| commands#genericCommand                     | String                   | yes      | Generic Endpoint to send commands                    | reboot, update_firmware, poll_all, smart_charging, start_charging, stop_charging, pause_charging, resume_charging, toggle_charging, override_schedule        |
| commands#startStop                          | Switch                   | yes      | Start/Stop Charing, only works with authorization    |                                                                                                                                                              |
| commands#pauseResume                        | Switch                   | yes      | Pause/Resume Charing                                 |                                                                                                                                                              |
| latestSession#sessionEnergy                 | Number:Energy            | no       | latest (already ended) session                       |                                                                                                                                                              |
| latestSession#sessionStart                  | DateTime                 | no       |                                                      |                                                                                                                                                              |
| latestSession#sessionEnd                    | DateTime                 | no       |                                                      |                                                                                                                                                              |

### Master Charger Channels

The Master Charger is like the "normal" charger but has some extra channels to control the circuit. These additional channels are listed in the table below.

| Channel                                     | Item Type                | Writable | Description                                          | Allowed Values (write access)                                                                                                                                |
|---------------------------------------------|--------------------------|----------|------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| dynamicCurrent#phase1                       | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| dynamicCurrent#phase2                       | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| dynamicCurrent#phase3                       | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| dynamicCurrent#dynamicCurrents              | String                   | yes      | read/write only for all phases.                      | &lt;value phase1&gt;;&lt;value phase2&gt;;&lt;value phase3&gt;  valid values for each phase are 0, 6-32. Example: 8;8;8                                      |
| settings#maxCircuitCurrentP1                | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| settings#maxCircuitCurrentP2                | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| settings#maxCircuitCurrentP3                | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| settings#maxCurrents                        | String                   | yes      | read/write only for all phases.                      | &lt;value phase1&gt;;&lt;value phase2&gt;;&lt;value phase3&gt;  valid values for each phase are 0, 6-32. Example: 8;8;8                                      |
| settings#offlineMaxCircuitCurrentP1         | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| settings#offlineMaxCircuitCurrentP2         | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| settings#offlineMaxCircuitCurrentP3         | Number:ElectricCurrent   | no       |                                                      |                                                                                                                                                              |
| settings#offlineMaxCurrents                 | String                   | yes      | read/write only for all phases.                      | &lt;value phase1&gt;;&lt;value phase2&gt;;&lt;value phase3&gt;  valid values for each phase are 0, 6-32. Example: 8;8;8                                      |
| settings#enableIdleCurrent                  | Switch                   | yes      |                                                      | ON/OFF                                                                                                                                                   |
| settings#allowOfflineMaxCircuitCurrent      | Switch                   | no       |                                                      |                                                                                                                                                              |

## Full Example

### Thing

#### Minimum configuration

```java
Bridge easee:site:mysite1 [ username="abc@def.net", password="secret", siteId="123456" ]
```

#### Manual configuration with two chargers, pollingInterval set to 60 seconds.

```java
Bridge easee:site:mysite1 [ username="abc@def.net", password="secret", siteId="471111", dataPollingInterval=60 ] {
        Thing mastercharger myCharger1 [ id="EHXXXXX1", circuitId="1234567" ]
        Thing charger myCharger2 [ id="EHXXXXX2" ]
}
```

### Items

```java
Number:ElectricCurrent  Easee_Circuit_Phase1                  "Phase 1"                                   { channel="easee:mastercharger:mysite1:myCharger1:dynamicCurrent#phase1" }
Number:ElectricCurrent  Easee_Circuit_Phase2                  "Phase 2"                                   { channel="easee:mastercharger:mysite1:myCharger1:dynamicCurrent#phase2" }
Number:ElectricCurrent  Easee_Circuit_Phase3                  "Phase 3"                                   { channel="easee:mastercharger:mysite1:myCharger1:dynamicCurrent#phase3" }
String                  Easee_Circuit_Dynamic_Phases          "Dynamic Power [MAP(easeePhases.map):%s]"   { channel="easee:mastercharger:mysite1:myCharger1:dynamicCurrent#setDynamicCurrents" }
Switch                  Easee_Charger_Start_Stop              "Start / Stop"                              { channel="easee:mastercharger:mysite1:myCharger1:commands#startStop" }
```

### Sitemap

```java
    Switch item=Easee_Circuit_Dynamic_Phases mappings=["0;0;0"="0.00 kW", "6;0;0"="1.44 kW", "7;0;0"="1.68 kW", "8;0;0"="1.92 kW", "9;0;0"="2.16 kW", "10;0;0"="2.40 kW", "16;0;0"="3.72 kW", "16;16;16"="11.1 kW"] icon="energy"
```

### Mapping

easeePhases.map will make the phase setting more readable.

```text
0;0;0=0.00 kW
6;0;0=1.44 kW
7;0;0=1.68 kW
8;0;0=1.92 kW
9;0;0=2.16 kW
10;0;0=2.40 kW
11;0;0=2.64 kW
12;0;0=2.88 kW
13;0;0=3.12 kW
14;0;0=3.36 kW
15;0;0=3.60 kW
16;0;0=3.72 kW
6;6;6=4.32 kW
7;7;7=5.04 kW
8;8;8=5.76 kW
9;9;9=6.48 kW
10;10;10=7.20 kW
11;11;11=7.92 kW
12;12;12=8.64 kW
13;13;13=9.36 kW
14;14;14=10.1 kW
15;15;15=10.8 kW
16;16;16=11.1 kW
```
