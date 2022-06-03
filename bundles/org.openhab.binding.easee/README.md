# Easee Binding

The Easee binding can be used to retrieve data from the Easee Cloud API and also to control your wallbox via the Cloud API.
This allows you to dynamically adjust the charge current for your car depending on production of your solar plant.

## Supported Things

This binding provides three thing types: 

| Thing/Bridge        | Thing Type          | Description                                                                                   |
|---------------------|---------------------|-----------------------------------------------------------------------------------------------|
| bridge              | site                | cloud connection to a site within an Easee account                                            |
| thing               | circuit             | a circuit which may contain one or more chargers                                              |
| thing               | charger             | the physical charger which is connected to a circuit within the given site                    |


Basically any Easee wallbox that supports the Cloud API should automatically be supported by this binding.

## Discovery

Discovery is implemented and will discover all circuits and chargers assigned to a given site.

## Bridge Configuration

The following configuration parameters are available for the binding/bridge:

- **username** (required)
The username to login at Easee Cloud service.
This should be an email adress or phone number.

- **passord** (required)  
Your password to login at Easee Cloud service.

- **siteId** (required)  
The ID of the site containing the wallbox(es) and circuit(s) that should be integrated into Openhab.
The ID of your site can be found out via the sites overview (https://easee.cloud/sites).
You just need to click on one of the sites listed there, the id will be part of the URL which is then opened.
It will be a number with typically 6 digits.

- **dataPollingInterval** (optional)  
interval (minutes) in which live data values are retrieved from the Easee Cloud API. (default = 5)

## Thing configuration

It is recommended to use auto discovery which does not require further configuration.
If manual configuration is preferred you need to specify configuration as below.

- **id** (required)
The id of the circuit or charger that will be represented by this thing.

## Channels

The binding only supports a subset of the available endpoints that is provided by the Easee Cloud API.
The tables below shows all channels that are available and which of them are writable.
The setting that start with "dynamic" can be changed frequently, the other are written to flash and thus should not be changed too often as this could result in damage of your flash.

### Charger Channels

| Channel Type ID                             | Item Type                | Writable | Description                                | Allowed Values (write access)                                                                                                                                |
|---------------------------------------------|--------------------------|----------|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| state#smartCharging                 | Switch                   | no       |                                            |                                                                                                                                                              |
| state#cableLocked                   | Switch                   | no       |                                            |                                                                                                                                                              |
| state#chargerOpMode                 | Number                   | no       |                                            |                                                                                                                                                              |
| state#totalPower                    | Number:Power             | no       |                                            |                                                                                                                                                              |
| state#sessionEnergy                 | Number:Energy            | no       |                                            |                                                                                                                                                              |
| state#dynamicCircuitCurrentP1       | Number:ElectricCurrent   | no       |                                            |                                                                                                                                                              |
| state#dynamicCircuitCurrentP2       | Number:ElectricCurrent   | no       |                                            |                                                                                                                                                              |
| state#dynamicCircuitCurrentP3       | Number:ElectricCurrent   | no       |                                            |                                                                                                                                                              |
| state#latestPulse                   | DateTime                 | no       |                                            |                                                                                                                                                              |
| state#chargerFirmware               | Number                   | no       |                                            |                                                                                                                                                              |
| state#latestFirmware                | Number                   | no       |                                            |                                                                                                                                                              |
| state#voltage                       | Number:ElectricPotential | no       |                                            |                                                                                                                                                              |
| state#outputCurrent                 | Number:ElectricCurrent   | no       |                                            |                                                                                                                                                              |
| state#isOnline                      | Switch                   | no       |                                            |                                                                                                                                                              |
| state#dynamicChargerCurrent         | Number:ElectricCurrent   | yes      |                                            | 6-32                                                                                                                                                         |
| state#reasonForNoCurrent            | Number                   | no       |                                            |                                                                                                                                                              |
| state#lifetimeEnergy                | Number:Energy            | no       |                                            |                                                                                                                                                              |
| state#errorCode                     | Number                   | no       |                                            |                                                                                                                                                              |
| state#fatalErrorCode                | Number                   | no       |                                            |                                                                                                                                                              |
| config#lockCablePermanently         | Switch                   | yes      |                                            | true/false                                                                                                                                                   |
| config#authorizationRequired        | Switch                   | yes      |                                            | true/false                                                                                                                                                   |
| config#limitToSinglePhaseCharging   | Switch                   | yes      |                                            | true/false                                                                                                                                                   |
| config#phaseMode                    | Number                   | yes      | 1=1phase, 2=auto, 3=3phase                 | 1-3                                                                                                                                                          |
| config#maxChargerCurrent            | Number:ElectricCurrent   | no       | write access not yet implemented           |                                                                                                                                                              |
| commands#genericCommand             | String                   | yes      | Generic Endpoint to send commands          | reboot, update_firmware, poll_all, smart_charging, start_charging, stop_charging, pause_charging, resume_charging, toggle_charging, override_schedule        |

### Circuit Channels Channels

TODO



## Full Example

### Thing                                                                                                                                                                    

- minimum configuration

```
easee:site:mysite1 [ username="abc@def.net", password="secret", siteId="123456" ]
```

- manual configuration with two wallboxes, pollingInterval set to 1 minute.

```
easee:site:mysite1 [ username="abc@def.net", password="secret", siteId="471111", dataPollingInterval=1 ] {
        Thing charger myCharger1 [ id="EHXXXXX1" ]
        Thing charger myCharger2 [ id="EHXXXXX2" ]
}
```
