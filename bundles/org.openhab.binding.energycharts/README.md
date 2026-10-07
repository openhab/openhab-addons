# Energy-Charts Binding

This binding provides day-ahead electricity prices from [Energy-Charts](https://www.energy-charts.info/), run by Fraunhofer ISE.
No account or API key is needed.

The prices are provided as time series with the resolution the market uses, e.g. 15 minutes.
The channel states always show the price of the current interval.

To run devices during the cheapest period, combine this binding with the [Optimal Window automation add-on](https://www.openhab.org/addons/automation/optimalwindow/).

## Supported Things

| Thing type | Description                                       |
| ---------- | ------------------------------------------------- |
| `prices`   | Day-ahead prices of one bidding zone              |

## Discovery

Auto discovery is not supported.

## Thing Configuration

| Parameter   | Description                                                                                                      | Default  | Required |
| ----------- | ---------------------------------------------------------------------------------------------------------------- | -------- | -------- |
| biddingZone | The bidding zone, see below                                                                                      |          | yes      |
| vatPercent  | Value added tax in percent, used for the gross prices                                                            | `0`      | no       |
| basePrice   | Net price in ct/kWh added to the market price, e.g. network charges and levies                                   | `0`      | no       |
| serviceFee  | Fee in percent of the absolute net total price (market price plus base price), added before VAT                  | `0`      | no       |

Supported bidding zones: `AT` (Austria), `BE` (Belgium), `CH` (Switzerland), `CZ` (Czech Republic), `DE-LU` (Germany, Luxembourg), `DK1` and `DK2` (Denmark), `FR` (France), `HU` (Hungary), `IT-North` (Italy North), `NL` (Netherlands), `NO2` (Norway 2), `PL` (Poland), `SE4` (Sweden 4) and `SI` (Slovenia).

Energy-Charts publishes these zones under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) with data from Bundesnetzagentur | SMARD.de.
Prices of other bidding zones are not offered by this binding, because their license only allows private use.

Prices for the next day are published around 13:00.
The binding requests prices when it starts and then after 13:00 at most once per hour, until the prices for the next day are available.

## Channels

| Channel      | Type               | Description                                                   |
| ------------ | ------------------ | ------------------------------------------------------------- |
| market-net   | Number:EnergyPrice | Day-ahead market price without VAT and base price             |
| market-gross | Number:EnergyPrice | Day-ahead market price with VAT but without base price        |
| total-net    | Number:EnergyPrice | Market price with base price and service fee but without VAT  |
| total-gross  | Number:EnergyPrice | Market price with base price, service fee and VAT             |

All channels support time series.
To keep future prices, e.g. for charts or the Optimal Window add-on, persist the Items with the `forecast` strategy in a persistence service that can store future values, e.g. InfluxDB, JDBC or In-Memory (rrd4j cannot).

## Full Example

### Thing Configuration

```java
Thing energycharts:prices:home "Electricity Prices" [ biddingZone="AT", vatPercent=20, basePrice=12.5 ]
```

### Item Configuration

```java
Number:EnergyPrice MarketNet  "Market price (net) [%.3f %unit%]"  { channel="energycharts:prices:home:market-net" }
Number:EnergyPrice TotalGross "Total price (gross) [%.3f %unit%]" { channel="energycharts:prices:home:total-gross" }
```

### Persistence Configuration

influxdb.persist:

```java
Items {
    MarketNet, TotalGross : strategy = forecast
}
```

### Sitemap Configuration

```perl
sitemap energycharts label="Electricity Prices"
{
    Frame label="Current Prices" {
        Text item=MarketNet
        Text item=TotalGross
    }
    Frame label="Price Forecast" {
        Chart item=TotalGross service="influxdb" period=D-D interpolation="step" legend=false
    }
}
```
