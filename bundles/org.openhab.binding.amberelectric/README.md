# Amber Electric Binding

A binding that supports the Australian energy retailer Amber Electric's API (https://www.amber.com.au/) and provides data on the current pricing for buying and selling power, as well as the current level of renewables in the NEM.

## Supported Things

Amber Electric API 

## Discovery

Auto-discover is not currently supported.
You need to manually add a new thing using your API key

## Thing Configuration

As a minimum, the IP address is needed:

- `apikey` - The API key from the 'Developer' section of https://apps.amber.com.au
- 'nmi' optional -  the NMI for your property. Required if you have multiple properties with Amber
- 'refresh' the refresh rate for querying the API.

## Channels

| channel id           | type          | description                                                                           |
|----------------------|---------------|---------------------------------------------------------------------------------------|
| elecprice            | Number        | Current price to import power from the grid
| clprice              | Number        | Current price to import power for Controlled Load
| feedinprice          | Number        | Current price to export power to the grid
| elecstatus           | String        | Current price status of grid import 
| clstatus             | String        | Current price status of controlled load import
| feedinstatus         | String        | Current price status of Feed-In
| nemtime              | String        | NEM time of last pricing update
| renewables           | Number        | Current level of renewables in the grid
| spike                | Switch        | Report if the grid has a current price spike

## Full Example

amberelectric.things:

```java
amberelectric:amberelectric:AmberElectric [ apikey="psk_xxxxxxxxxxxxxxxxxxxx" ]
```

amberelectric.items:

```java
Number AmberElectric_ElecPrice { channel="amberelectric:amberelectric:AmberElectric:elecprice" }
Number AmberElectric_CLPrice { channel="amberelectric:amberelectric:AmberElectric:clprice" }
Number AmberElectric_FeedInPrice { channel="amberelectric:amberelectric:AmberElectric:feedinprice" }
String AmberElectric_ElecStatus { channel="amberelectric:amberelectric:AmberElectric:elecstatus" }
String AmberElectric_CLStatus { channel="amberelectric:amberelectric:AmberElectric:clstatus" }
String AmberElectric_FeedInStatus { channel="amberelectric:amberelectric:AmberElectric:feedinstatus" }
String AmberElectric_nemtime { channel="amberelectric:amberelectric:AmberElectric:nemtime" }
Number AmberElectric_Renewables { channel="amberelectric:amberelectric:AmberElectric:renewables" }
Switch AmberElectric_Spike { channel="amberelectric:amberelectric:AmberElectric:spike" }
```
    
amberelectric.sitemap:

```perl
Text item=AmberElectric_ElecPrice label="Electricity Price"
Text item=AmberElectric_CLPrice label="Controlled Load Price"
Text item=AmberElectric_FeedInPrice label="Feed-In Price"
Text item=AmberElectric_ElecStatus label="Electricity Price Status"
Text item=AmberElectric_CLStatus label="Controlled Load Price Status"
Text item=AmberElectric_FeedInStatus label="Feed-In Price Status"
Text item=AmberElectric_nemtime label="Current time of NEM pricing"
Text item=AmberElectric_Renewables label="Renewables Level"
Switch item=AmberElectric_Spike  label="Spike Status"
```
