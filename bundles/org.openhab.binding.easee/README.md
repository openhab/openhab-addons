# Easee Binding

The Easee binding can be used to retrieve data from the Easee Cloud API and also to control your wallbox via the Cloud API.
This allows you to dynamically adjust the charge current for you car depending on production of your solar plant.

## Supported Things

This binding provides only one thing type: "wallbox" which is the wallbox itself.
Basically any Easee wallbox that supports the Cloud API should automatically be supported by this binding.
Create one wallbox thing per physical wallbox installation available in your home(s).

## Discovery

Auto-Discovery is currently not supported, as most homes should only have one wallbox this is not likely to be implemented as it will not have much benefit.

## Thing Configuration

The following configuration parameters are available for this thing:

- **username** (required)
The username to login at Easee Cloud service.
This should be an email adress or phone number.

- **passord** (required)  
Your password to login at Easee Cloud service.

- **wallboxId** (required)  
The ID (serial) of the wallbox used to identify the wall box at Easee cloud. This typically starts with "EH".

- **dataPollingInterval** (optional)  
interval (minutes) in which live data values are retrieved from the Easee Cloud API. (default = 5)

## Channels

The binding only supports a subset of the available endpoints that is provided by the Easee Cloud API.
The table below shows all channels that are available and which of them are writable.

| Channel Type ID                             | Item Type                | Writable | Description                                | Allowed Values (write access)                 |
|---------------------------------------------|--------------------------|----------|--------------------------------------------|-----------------------------------------------|
| charger_state#smartCharging                 | Switch                   | no       |                                            |                                               |
| charger_state#cableLocked                   | Switch                   | no       |                                            |                                               |
| charger_state#chargerOpMode                 | Number                   | no       |                                            |                                               |
| charger_state#totalPower                    | Number:Power             | no       |                                            |                                               |
| charger_state#sessionEnergy                 | Number:Energy            | no       |                                            |                                               |
| charger_state#latestPulse                   | DateTime                 | no       |                                            |                                               |
| charger_state#chargerFirmware               | Number                   | no       |                                            |                                               |
| charger_state#latestFirmware                | Number                   | no       |                                            |                                               |
| charger_state#voltage                       | Number:ElectricPotential | no       |                                            |                                               |
| charger_state#outputCurrent                 | Number:ElectricCurrent   | no       |                                            |                                               |
| charger_state#isOnline                      | Switch                   | no       |                                            |                                               |
| charger_state#dynamicChargerCurrent         | Number:ElectricCurrent   | no       |                                            |                                               |
| charger_state#reasonForNoCurrent            | Number                   | no       |                                            |                                               |
| charger_state#lifetimeEnergy                | Number:Energy            | no       |                                            |                                               |
| charger_state#errorCode                     | Number                   | no       |                                            |                                               |
| charger_state#fatalErrorCode                | Number                   | no       |                                            |                                               |
| charger_config#lockCablePermanently         | Switch                   | no*      |                                            |                                               |
| charger_config#lockCablePermanently         | Switch                   | no*      |                                            |                                               |
| charger_config#limitToSinglePhaseCharging   | Switch                   | no*      |                                            |                                               |
| charger_config#phaseMode                    | Number                   | no*      |                                            |                                               |

*write access not yet implemented


## Full Example

### Thing                                                                                                                                                                    

- minimum configuration

```
easee:wallbox:box1 [ username="abc@def.net", passord="secret", wallboxId="EH4711" ]
```

- two wallboxes, with pollingInterval

```
easee:wallbox:box1 [ username="abc@def.net", passord="secret", wallboxId="EH4711", dataPollingInterval=1 ]
easee:wallbox:box2 [ username="abc@def.net", passord="secret", wallboxId="EH4712", dataPollingInterval=1 ]
```
