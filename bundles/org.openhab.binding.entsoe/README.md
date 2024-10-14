# ENTSO-E Binding

This binding fetches day-ahead energy spot prices from ENTSO-E, the European Network of Transmission System Operators for Electricity. 

Users can select a specific area to retrieve the relevant energy prices.
This binding helps users monitor and manage their energy consumption based on real-time pricing data.
It is recommended to use this binding together with a currency provider (e.g. [Freecurrency binding](https://www.openhab.org/addons/bindings/freecurrency/)) for exchanging euro spot prices to local currency.

## Supported Things

- `day-ahead`: This is the main and single Thing of the binding. 

## Thing Configuration

To access the ENTSO-E Transparency Platform API, users need a **security token** for authentication and authorization.
This token ensures secure access to the platform's data and services.
For detailed instructions on obtaining this token, you can refer to the [ENTSO-E API Guide 2. Authentication and Authorisation](https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_authentication_and_authorisation).

Mandatory parameters of the Thing are security token and area.
Optional parameters are historic days, resolution, availability hour for day ahead spot prices and request timeout.

### `entsoe` Thing Configuration

| Name                          | Type              | Description                                                               | Default   | Required | Advanced |
|-------------------------------|-------------------|---------------------------------------------------------------------------|-----------|----------|----------|
| securityToken                 | text              | Security token to fetch from ENTSO-E                                      | N/A       | yes      | no       |
| area                          | text              | Area                                                                      | N/A       | yes      | no       |
| historicDays                  | integer           | Historic days to get prices from (will use exchange rate as of today)     | 0         | no       | no       |
| resolution                    | text              | Data resolution                                                           | PT60M     | no       | no       |
| spotPricesAvailableCetHour    | integer           | Which CET hour binding assumes new spot prices for next day is available  | 13        | no       | yes      |
| requestTimeout                | integer           | Request timeout in seconds                                                | 30        | no       | yes      |

## Channels

Binding has one channel.

spot-price which are the values fetched from ENTSO-E and persisted in openHAB as time series.
The price is per kWh at your selected base currency.

| Channel                  | Type                  | Read/Write | Description                               |
|--------------------------|-----------------------|------------|-------------------------------------------|
| spot-price               | Number:EnergyPrice    | R          | Spot prices                               |

### Thing Configuration

```java
Thing entsoe:day-ahead:eda "Entsoe Day Ahead" [ securityToken="your-security-token", area="10YNO-3--------J", historicDays=14 ] 
```

### Item Configuration

```java
Number:EnergyPrice energySpotPrice "Current Spot Price" <price> { channel="entsoe:day-ahead:eda:spot-price" }
```

#### Value-Added Tax

VAT is not included in any of the prices.
To include VAT for items linked to the `Number:EnergyPrice` channel, the [VAT profile](https://www.openhab.org/addons/transformations/vat/) can be used.
This must be installed separately.
Once installed, simply select "Value-Added Tax" as Profile when linking an item.

#### Total Price

_Please note:_ There is no channel providing the total price.
Instead, create a group item with `SUM` as aggregate function and add the individual price items as children.
Read more about how to in this similar binding [Energi Data Service](https://www.openhab.org/addons/bindings/energidataservice/#total-price)

### Trigger Channels

Channel `prices-received` is triggered when new prices are available.

### Examples

examples.rules

```java
rule "Spot prices received"
when
    Channel "entsoe:day-ahead:eda:prices-received" triggered
then
    // Do something within rule
    logInfo("ENTSO-E Rule", "ENTSO-E channel triggered, new spot prices available")
end
```
