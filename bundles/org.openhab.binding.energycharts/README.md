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

| Parameter   | Description                 | Required |
| ----------- | --------------------------- | -------- |
| biddingZone | The bidding zone, see below | yes      |

Supported bidding zones: `AT` (Austria), `BE` (Belgium), `CH` (Switzerland), `CZ` (Czech Republic), `DE-LU` (Germany, Luxembourg), `DK1` and `DK2` (Denmark), `FR` (France), `HU` (Hungary), `IT-North` (Italy North), `NL` (Netherlands), `NO2` (Norway 2), `PL` (Poland), `SE4` (Sweden 4) and `SI` (Slovenia).

Energy-Charts publishes these zones under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) with data from Bundesnetzagentur | SMARD.de.
Prices of other bidding zones are not offered by this binding, because their license only allows private use.

Prices for the next day are published around 13:00.
The binding requests prices when it starts and then after 13:00 at most once per hour, until the prices for the next day are available.

## Channels

| Channel    | Type               | Description                        |
| ---------- | ------------------ | ---------------------------------- |
| spot-price | Number:EnergyPrice | Day-ahead market price without VAT |

The channel supports time series.
To keep future prices, e.g. for charts or the Optimal Window add-on, persist the Items with the `forecast` strategy in a persistence service that can store future values, e.g. InfluxDB, JDBC or In-Memory (rrd4j cannot).

### Value-Added Tax and Other Charges

The binding provides the plain market price.
For gross prices, link the Item with the [VAT profile](https://www.openhab.org/addons/transformations/vat/).
A fixed charge per kWh, e.g. the markup of your supplier, can be added with the `system:offset` profile.
Both profiles also apply to the time series.

## Full Example

### Thing Configuration

```java
Thing energycharts:prices:home "Electricity Prices" [ biddingZone="AT" ]
```

### Item Configuration

```java
Number:EnergyPrice SpotPrice      "Spot price [%.3f %unit%]"               { channel="energycharts:prices:home:spot-price" }
Number:EnergyPrice SpotPriceGross "Spot price (gross) [%.3f %unit%]"       { channel="energycharts:prices:home:spot-price" [ profile="transform:VAT" ] }
Number:EnergyPrice EnergyPrice    "Energy price with markup [%.3f %unit%]" { channel="energycharts:prices:home:spot-price" [ profile="system:offset", offset="0.015 EUR/kWh" ] }
```

### Persistence Configuration

influxdb.persist:

```java
Items {
    SpotPrice, SpotPriceGross, EnergyPrice : strategy = forecast
}
```

### Sitemap Configuration

```perl
sitemap energycharts label="Electricity Prices"
{
    Frame label="Current Prices" {
        Text item=SpotPrice
        Text item=SpotPriceGross
        Text item=EnergyPrice
    }
    Frame label="Price Forecast" {
        Chart item=SpotPriceGross service="influxdb" period=D-D interpolation="step" legend=false
    }
}
```
