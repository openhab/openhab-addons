
# entsoe Binding

This binding fetches day-ahead energy spot prices from ENTSO-E, the European Network of Transmission System Operators for Electricity. Users can select a specific area to retrieve the relevant energy prices. This binding helps users monitor and manage their energy consumption based on real-time pricing data.

Prerequisites:

- openHAB version 4.2 since it makes use of persisted timeseries.
- Currency provider configured in unit settings of openHAB. We recommend using Freecurrency binding to fetch up to date exchange values.

## Supported Things

- `entsoe`: This is the main and single thing of the binding. 

## Thing Configuration

To access the ENTSO-E Transparency Platform API, users need a **security token** for authentication and authorization. This token ensure secure access to the platform's data and services. For detailed instructions on obtaining this token, you can refer to the [ENTSO-E API Guide 2. Authentication and Authorisation](https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_authentication_and_authorisation).

Mandatory parameters of the thing is security token and area. Optional parameters are VAT, additional cost and historic days.

Thing can be added in graphical user interface of openHAB or manually within a things file.

### `entsoe` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| securityToken                 | text              | Security token to fetch from ENTSO-E                                      | N/A       | yes      | no       |
| area                          | text              | Area                                                                      | N/A       | yes      | no       |
| vat                           | double            | Value added tax                                                           | 0         | no       | no       |
| additionalCost                | double            | Additional cost to add to price                                           | 0         | no       | no       |
| historicDays                  | integer           | Historic days to get prices from (will use exchange rate as of today)     | 0         | no       | no       |
| spotPricesAvailableUtcHour    | integer           | Which UTC hour binding assumes new spot prices for next day is available  | 12        | no       | yes      |

## Channels

Binding has two channels.

Prices which are the values fetched from ENTSOE and persisted in openHAB as timeseries. The price is per kWh at your selected base currency.
LastDayAheadReceived which is at which date and time the prices was received from ENTSO-E.

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| prices                | Number    | R         | Spot prices                               |
| lastDayAheadReceived  | DateTime  | R         | Date and time spot prices was received    |

### Thing Configuration

```java
Thing entsoe:dayAhead:eda "Entsoe Day Ahead" [ securityToken="your-security-token", area="10YNO-3--------J", vat=25.0, historicDays=14, additionalCost=0 ] 
```

### Item Configuration

```java
Number energySpotPrice "Current Spot Price" <price> { channel="entsoe:dayAhead:eda:prices" }
DateTime lastReceived "Last received spot prices" {channel="entsoe:dayAhead:eda:lastDayAheadReceived"}

rule "Spot prices received"
when
    Channel "entsoe:dayAhead:eda:pricesReceived" triggered
then
    // Do something within rule
    logInfo("entsoeRule", "entsoe channel triggered")
end
```
