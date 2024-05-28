# GridBox Binding

The [Viessmann GridBox](https://www.viessmann.de/de/produkte/energiemanagement/gridbox.html) is a energy management device which gathers information about produced and consumed electrical power from compatible energy meters, photovoltaic inverters, batteries, heat pumps, EV charging stations etc. and steers the connected components to increase the self consumption rate and efficiency of the system.

The Viessmann GridBox is a variety of the [gridX Gateway](https://de.gridx.ai/edge-services) and uses the gridX Xenon cloud service to upload the fetched data and deliver the data to the GridBox app and web service.
The measured data (energy production, consumptions, etc.) cannot be accessed locally. However, thanks to the pioneer work in the [unl0ck/viessmann-gridbox-connector](https://github.com/unl0ck/viessmann-gridbox-connector) repository, we can retrieve the data from the gridX cloud service using Rest-API calls.
The API is documented [here](https://developer.gridx.ai/reference/).

This binding polls the "live data" API endpoint to gather the available data from the GridBox. 
It creates a GridBox thing with the channels representing the data points of the live data API call.

For connection to the cloud service, account E-Mail and password used to connect to the [GridBox web service](https://mygridbox.viessmann.com/login) are required.
Authentication is handled by a OAuth call generating a ID Token which is required as a bearer token for subsequent calls to the gridX API.

At the moment, only one API-"system" per account is supported by this binding.
A "system" is the representation of a GridBox together with its connected appliances (PV inverter, heat pump etc.).
The binding will use the first system ID retrieved by a call to the https://api.gridx.de/systems API.

Also, only the live data API endpoint is supported by the binding as it is the most interesting for openHAB use cases. 
There is another API endpoint for fetching aggregated measurement data which could be added in the future.
Only the Viessmann GridBox variant is supported, other variants would need adaptions to the OAuth mechanism.

This binding is not endorsed or supported by Viessmann or gridX. 
Arbitrary breaking changes to the API can happen at any time, resulting in this binding failing to retrieve the data.

## Supported Things

The following thing can be created with the binding: 

- `gridbox`: A thing representing the GridBox, tied to an account of the Viessmann GridBox.

## Discovery

No support for auto discovery at the moment.

## Thing Configuration

The following configuration parameters are available on the GridBox thing:

### `gridbox` Thing Configuration

| Name            | Type    | Description                                       | Default | Required | Advanced |
|-----------------|---------|---------------------------------------------------|---------|----------|----------|
| email           | text    | E-Mail address used to log in to the GridBox API  | N/A     | yes      | no       |
| password        | text    | Password to access the GridBox API                | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec.             | 60      | no       | yes      |

## Channels

The following channels are supplied by the GridBox thing (descriptions taken from the API documentation):

| Channel                       | Type      | Read/Write  | Description                                                                                                                                                         |
|-------------------------------|-----------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| battery-capacity              | Number    | R           | Maximum energy the battery can provide in Wh.                                                                                                                       |
| battery-nominal-capacity      | Number    | R           |                                                                                                                                                                     |
| battery-power                 | Number    | R           | Power is the measured power used to charge/discharge the battery. Unit W, Meaning, Positive values indicate discharging. Negative values indicate charging.         |
| battery-remaining-charge      | Number    | R           | Remaining Charge is the amount of energy left.                                                                                                                      |
| battery-state-of-charge       | Number    | R           | State of Charge indicates how full a battery is. Unit Percentage points 0.0-1.0.                                                                                    |
| battery-level                 | Number    | R           | Battery level, ratio of remaining charge to capacity.                                                                                                               |
| consumption                   | Number    | R           | Adjusted power/energy of the system.                                                                                                                                |
| direct-consumption            | Number    | R           | Power/energy consumed through production directly.                                                                                                                  |
| direct-consumption-ev         | Number    | R           | Power/energy consumed by the EV through production directly.                                                                                                        |
| direct-consumption-heat-pump  | Number    | R           | Power/energy consumed by the heat pump through production directly.                                                                                                 |
| direct-consumption-heater     | Number    | R           | Power/energy consumed by the heater through production directly.                                                                                                    |
| direct-consumption-household  | Number    | R           | Power/energy consumed by the household through production directly.                                                                                                 |
| direct-consumption-rate       | Number    | R           | Ratio of direct consumption vs production (0.0-1.0).                                                                                                                |
| ev-charging-station-power     | Number    | R           | Measured power used to charge/discharge via EV station, positive values indicate charging, negatives discharging.                                                   |
| heat-pump-power               | Number    | R           | Aggregated measured power/energy for heat pumps.                                                                                                                    |
| photovoltaic-production       | Number    | R           | Photovoltaic is the measured power/energy in front of the photovoltaic systems.                                                                                     |
| production                    | Number    | R           | Sum of all energy producing appliances (e.g. PV).                                                                                                                   |
| self-consumption              | Number    | R           | Power/Energy consumed through production and charged into battery.                                                                                                  |
| self-consumption-rate         | Number    | R           | Ratio of self consumption vs production (0.0-1.0).                                                                                                                  |
| self-sufficiency-rate         | Number    | R           | Ratio of produced energy vs total consumed energy (0.0-1.0).                                                                                                        |
| self-supply                   | Number    | R           | Power/energy consumed through storage and production.                                                                                                               |
| total-consumption             | Number    | R           | Adjusted power/energy of the system including heatpumps and EV charging stations.                                                                                   |
                                                                                                                                                                                                                             
## Full Example                                                                                                                                                                                                              
                                                                                                                                                                                                                             
### Thing Configuration                                                                                                                                                                                                      
                                                                                                                                                                                                                             
```java                                                                                                                                                                                                                      
Thing gridbox:gridbox:901b4766e2 "GridBox" [email="abc@example.com",password="mypassword",refreshInterval=120]                                                                                                               
```                                                                                                                                                                                                                          
                                                                                                                                                                                                                             
### Item Configuration                                                                                                                                                                                                       

```java
Number GridBox_PhotovoltaicProduction "PV Production [%.0f W]" {channel="gridbox:gridbox:901b4766e2:photovoltaicProduction"}
```
