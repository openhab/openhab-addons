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
The setting that start with "dynamic" can be changed frequently, the other are written to flash and thus should not be changed too often as this could result in damage of your flash.

| Channel Type ID                             | Item Type                | Writable | Description                                | Allowed Values (write access)                                                                                                                                |
|---------------------------------------------|--------------------------|----------|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| charger_state#smartCharging                 | Switch                   | no       |                                            |                                                                                                                                                              |
| charger_state#cableLocked                   | Switch                   | no       |                                            |                                                                                                                                                              |
| charger_state#chargerOpMode                 | Number                   | no       |                                            |                                                                                                                                                              |
| charger_state#totalPower                    | Number:Power             | no       |                                            |                                                                                                                                                              |
| charger_state#sessionEnergy                 | Number:Energy            | no       |                                            |                                                                                                                                                              |
| charger_state#dynamicCircuitCurrentP1       | Number:ElectricCurrent   | no       |                                            |                                                                                                                                                              |
| charger_state#dynamicCircuitCurrentP2       | Number:ElectricCurrent   | no       |                                            |                                                                                                                                                              |
| charger_state#dynamicCircuitCurrentP3       | Number:ElectricCurrent   | no       |                                            |                                                                                                                                                              |
| charger_state#latestPulse                   | DateTime                 | no       |                                            |                                                                                                                                                              |
| charger_state#chargerFirmware               | Number                   | no       |                                            |                                                                                                                                                              |
| charger_state#latestFirmware                | Number                   | no       |                                            |                                                                                                                                                              |
| charger_state#voltage                       | Number:ElectricPotential | no       |                                            |                                                                                                                                                              |
| charger_state#outputCurrent                 | Number:ElectricCurrent   | no       |                                            |                                                                                                                                                              |
| charger_state#isOnline                      | Switch                   | no       |                                            |                                                                                                                                                              |
| charger_state#dynamicChargerCurrent         | Number:ElectricCurrent   | yes      |                                            | 6-32                                                                                                                                                         |
| charger_state#reasonForNoCurrent            | Number                   | no       |                                            |                                                                                                                                                              |
| charger_state#lifetimeEnergy                | Number:Energy            | no       |                                            |                                                                                                                                                              |
| charger_state#errorCode                     | Number                   | no       |                                            |                                                                                                                                                              |
| charger_state#fatalErrorCode                | Number                   | no       |                                            |                                                                                                                                                              |
| charger_config#lockCablePermanently         | Switch                   | yes      |                                            | true/false                                                                                                                                                   |
| charger_config#authorizationRequired        | Switch                   | yes      |                                            | true/false                                                                                                                                                   |
| charger_config#limitToSinglePhaseCharging   | Switch                   | yes      |                                            | true/false                                                                                                                                                   |
| charger_config#phaseMode                    | Number                   | yes      | 1=1phase, 2=auto, 3=3phase                 | 1-3                                                                                                                                                          |
| charger_config#maxChargerCurrent            | Number:ElectricCurrent   | no       | write access not yet implemented           |                                                                                                                                                              |
| charger_commands#genericCommand             | String                   | yes      | Generic Endpoint to send commands          | reboot, update_firmware, poll_all, smart_charging, start_charging, stop_charging, pause_charging, resume_charging, toggle_charging, override_schedule        |




## Full Example

### Thing                                                                                                                                                                    

- minimum configuration

```
easee:wallbox:box1 [ username="abc@def.net", password="secret", wallboxId="EH4711" ]
```

- two wallboxes, with pollingInterval

```
easee:wallbox:box1 [ username="abc@def.net", password="secret", wallboxId="EH4711", dataPollingInterval=1 ]
easee:wallbox:box2 [ username="abc@def.net", password="secret", wallboxId="EH4712", dataPollingInterval=1 ]
```
