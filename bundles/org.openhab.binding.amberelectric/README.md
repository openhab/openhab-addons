# Amber Electric Binding

A binding that supports the Australian energy retailer Amber's API (<https://www.amber.com.au/>) and provides data on the current pricing for buying and selling power, as well as the current level of renewables in the NEM.

## Supported Things

- `service` Amber Electric API

## Discovery

The binding does not support auto discovery.

## Thing Configuration

As a minimum, the IP address is needed:

- `apiKey` - The API key from the 'Developer' section of <https://apps.amber.com.au>
- 'nmi' optional -  the NMI for your property. Required if you have multiple properties with Amber
- 'refresh' the refresh rate for querying the API.

## Channels

| channel id             | type                 | description                                       |
|------------------------|----------------------|---------------------------------------------------|
| electricity-price      | Number:EnergyPrice   | Current price to import power from the grid       |
| controlled-load-price  | Number:EnergyPrice   | Current price to import power for Controlled Load |
| feed-in-price          | Number:EnergyPrice   | Current price to export power to the grid         |
| electricity-status     | String               | Current price status of grid import               |
| controlled-load-status | String               | Current price status of controlled load import    |
| feed-in-status         | String               | Current price status of Feed-In                   |
| nem-time               | String               | NEM time of last pricing update                   |
| renewables             | Number:Dimensionless | Current level of renewables in the grid           |
| spike                  | Switch               | Report if the grid has a current price spike      |

## Full Example

### `amberelectric.things`

```java
amberelectric:service:AmberElectric [ apiKey="psk_xxxxxxxxxxxxxxxxxxxx" ]
```

### `amberelectric.items`

```java
Number:EnergyPrice AmberElectric_ElectricityPrice { channel="amberelectric:service:AmberElectric:electricity-price" }
Number:EnergyPrice AmberElectric_ControlledLoadPrice { channel="amberelectric:service:AmberElectric:controlled-load-price" }
Number:EnergyPrice AmberElectric_FeedInPrice { channel="amberelectric:service:AmberElectric:feed-in-price" }
String AmberElectric_ElectricityStatus { channel="amberelectric:service:AmberElectric:electricity-status" }
String AmberElectric_ControlledLoadStatus { channel="amberelectric:service:AmberElectric:controlled-load-status" }
String AmberElectric_FeedInStatus { channel="amberelectric:service:AmberElectric:feed-in-status" }
String AmberElectric_nemtime { channel="amberelectric:service:AmberElectric:nem-time" }
Number AmberElectric_Renewables { channel="amberelectric:service:AmberElectric:renewables" }
Switch AmberElectric_Spike { channel="amberelectric:service:AmberElectric:spike" }
```

### `amberelectric.sitemap`

```perl
Text item=AmberElectric_ElectricityPrice label="Electricity Price"
Text item=AmberElectric_ControlledLoadPrice label="Controlled Load Price"
Text item=AmberElectric_FeedInPrice label="Feed-In Price"
Text item=AmberElectric_ElectricityStatus label="Electricity Price Status"
Text item=AmberElectric_ControlledLoadStatus label="Controlled Load Price Status"
Text item=AmberElectric_FeedInStatus label="Feed-In Price Status"
Text item=AmberElectric_nemtime label="Current time of NEM pricing"
Text item=AmberElectric_Renewables label="Renewables Level"
Switch item=AmberElectric_Spike  label="Spike Status"
```
