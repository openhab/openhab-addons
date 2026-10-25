# aWATTar Binding

This binding provides electricity market prices for Germany and Austria through aWATTar or Energy-Charts.

## Supported Things

There are three supported things.

### aWATTar Bridge

The `bridge` reads price data from the selected API and stores the (optional) config values for VAT and energy base price. Select `Energy-Charts` to receive its timestamped intervals, including 15-minute prices where published.

### Prices Thing

The `prices` Thing provides current, today, and tomorrow net and gross prices in its fixed hourly channel groups.

### Bestprice Thing

The `bestprice` Thing identifies the cheapest API price intervals in a time range. With 15-minute data, a length of four intervals is one hour.

Note: The bridge schedules price refreshes at 15:00, 18:00 and 21:00.
If late updates occur, e.g. after 21:00, there is a chance that consecutive best prices will be rescheduled.
As a consequence, a time schedule spanning over an update slot might be interrupted and rescheduled.

## Discovery

Auto discovery is not supported.

## Thing Configuration

### aWATTar Bridge

| Parameter  | Description                                                                                                                                                                                                                                                                                                     |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| provider   | Price API: `awattar` (default) or `energy-charts`. Energy-Charts returns intervals based on its timestamp data.                                                                                                                                                                                                 |
| vatPercent | Percentage of the value added tax to apply to net prices. Optional, defaults to 19.                                                                                                                                                                                                                             |
| basePrice  | The net(!) base price you have to pay for every kWh. Optional, but you most probably want to set it based on you delivery contract.                                                                                                                                                                             |
| country    | The country prices should be received for. Use `DE` for Germany or `AT` for Austria. `DE` is the default.                                                                                                                                                                                                       |
| serviceFee | The service fee in percent. Will be added to the total price. Will be calculated on top of the absolute price per hour. Default is `0`.                                                                                                                                                                         |

Hour-based channel groups and Bestprice ranges use openHAB's configured time zone.

### Prices Thing

The prices thing does not need any configuration.

### Bestprice Thing

| Parameter     | Description                                                                                                                                                                               |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| rangeStart    | First hour of the time range the binding should search for the best prices. Default: `0`                                                                                                  |
| rangeDuration | The duration of the time range the binding should search for best prices. Default: `24`                                                                                                   |
| length        | Number of API price intervals to find within the range. Default: `1`. For quarter-hour prices, four intervals equal one hour.                                                             |
| consecutive   | if `true`, the thing identifies the cheapest consecutive range of `length` API intervals within the lookup range. Otherwise, it contains the cheapest `length` intervals. Default: `true` |
| inverted      | if `true`, the worst prices will be searched instead of the best. Does currently not work in combination with 'consecutive'. Default: `false`                                             |

#### Limitations

The channels of a bestprice thing are only defined when the binding has enough data to compute them.
The thing is recomputed after the end of the candidate time range for the next day, but only as soon as data for the next day is available from the aWATTar API, which is around 14:00.
So for a bestprice thing with `[ rangeStart=5, rangeDuration=5  ]` all channels will be undefined from 10:00 to 14:00.
Also, due to the time the aWATTar API delivers the data for the next day, it doesn't make sense to define a thing with `[ rangeStart=12, rangeDuration=20 ]` as the binding will be able to compute the channels only after 14:00.

## Channels

### Bridge

The bridge has four channels which support a time-series:

| channel      | type               | description                                                                  |
| ------------ | ------------------ | ---------------------------------------------------------------------------- |
| market-net   | Number:EnergyPrice | This net market price per kWh. This is directly taken from the selected API. |
| market-gross | Number:EnergyPrice | The market price including VAT, using the defined VAT percentage.            |
| total-net    | Number:EnergyPrice | Sum of net market price and configured base price                            |
| total-gross  | Number:EnergyPrice | Sum of market and base price with VAT applied                                |

The bridge time-series uses the selected API's returned timestamps, so aWATTar records are hourly points and Energy-Charts quarter-hour records remain quarter-hour points.
Use these channels to show or evaluate prices for today and tomorrow.
Energy-Charts market data is provided by Bundesnetzagentur / SMARD under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). The API response includes a `license_info` field with attribution details.

### Prices Thing

For each hourly channel group, the `prices` thing provides the following prices:

| channel      | type   | description                                                                                                                             |
| ------------ | ------ | --------------------------------------------------------------------------------------------------------------------------------------- |
| market-net   | Number | This net market price per kWh. This is directly taken from the selected API.                                                            |
| market-gross | Number | The market price including VAT, using the defined VAT percentage.                                                                       |
| total-net    | Number | Sum of net market price and configured base price                                                                                       |
| total-gross  | Number | Sum of market and base price with VAT applied. Most probably this is the final price you will have to pay for one kWh in a certain hour |

All prices are available in each of the following channel groups:

| channel group                          | description                                                                                                                                                                          |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| current                                | The price for the current API interval                                                                                                                                               |
| today00, today01, today02 ... today23  | **Deprecated**, use the bridge time-series channels instead. Prices of the API interval starting at the given hour today, e.g. `today01` provides the price starting at 01:00.       |
| tomorrow00, tomorrow01, ... tomorrow23 | **Deprecated**, use the bridge time-series channels instead. Prices of the API interval starting at the given hour tomorrow. They should be available starting at 14:00.             |

The `todayXX` and `tomorrowXX` channel groups are deprecated and will be removed in a future version.
With Energy-Charts quarter-hour prices they only contain the first quarter-hour of each hour, e.g. `today01` is the price from 01:00 to 01:15.

### Bestprice Thing

| channel   | type        | description                                                                                                                                                                                               |
| --------- | ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| active    | Switch      | `ON` if the current time is within the bestprice period, `OFF` otherwise. If `consecutive` was set to `false`, this channel may change between `ON` and `OFF` multiple times within the bestprice period. |
| start     | DateTime    | The exact start time of the bestprice range. If `consecutive` was `false`, it is the start time of the first interval found.                                                                              |
| end       | DateTime    | The exact end time of the bestprice range. If `consecutive` was `false`, it is the end time of the last interval found.                                                                                   |
| hours     | String      | A comma-separated list of API price intervals in the bestprice period. Quarter-hour intervals include their minute in the label.                                                                          |
| countdown | Number:Time | The time in minutes until start of the bestprice range. If start time passed. the channel will be set to `UNDEFINED` until the values for the next day are available.                                     |
| remaining | Number:Time | The time in minutes until end of the bestprice range. If start time passed. the channel will be set to `UNDEFINED` until the values for the next day are available.                                       |

## Full Example

### Things

awattar.things:

```java
Bridge awattar:bridge:bridge1 "aWATTar Bridge" [ country="DE", vatPercent="19", basePrice="17.22", serviceFee="3" ] {
 Thing prices price1 "aWATTar Price" []
// The car should be loaded for 4 hours during the night
 Thing bestprice carloader "Car Loader" [ rangeStart="22", rangeDuration="8", length="4", consecutive="true" ]
// In the cheapest hour of the night the garden should be watered
 Thing bestprice water "Water timer" [ rangeStart="19", rangeDuration="12", length="1" ]
// The heatpump should run the 12 cheapest hours per day
 Thing bestprice heatpump "Heat pump" [ length="12", consecutive="false" ]
}
```

`length` counts API price intervals.
The example above uses hourly aWATTar prices.
With `provider="energy-charts"` and quarter-hour prices, multiply it by four for the same durations: `length="16"` for the car loader, `length="4"` for the water timer and `length="48"` for the heat pump.

### Items

awattar.items:

```java
Number:EnergyPrice MarketNet  "Market price (net) [%.3f %unit%]"  { channel="awattar:bridge:bridge1:market-net" }
Number:EnergyPrice TotalGross "Total price (gross) [%.3f %unit%]" { channel="awattar:bridge:bridge1:total-gross" }

Number CurrentNet        "Current market price (net) [%.2f ct/kWh]" { channel="awattar:prices:bridge1:price1:current#market-net" }
Number CurrentTotalGross "Current total price (gross) [%.2f ct/kWh]" { channel="awattar:prices:bridge1:price1:current#total-gross" }

DateTime    CarStart     "Start car loader [%1$tH:%1$tM]"    { channel="awattar:bestprice:bridge1:carloader:start" }
DateTime    CarEnd       "End car loader [%1$tH:%1$tM]"      { channel="awattar:bestprice:bridge1:carloader:end" }
Number:Time CarCountdown "Car loader starts in [%.0f min]"   { channel="awattar:bestprice:bridge1:carloader:countdown" }
Number:Time CarRemaining "Car loader ends in [%.0f min]"     { channel="awattar:bestprice:bridge1:carloader:remaining" }
String      CarHours     "Car loader price intervals [%s]"   { channel="awattar:bestprice:bridge1:carloader:hours" }
Switch      CarActive    "Car loader active"                 { channel="awattar:bestprice:bridge1:carloader:active" }

Switch WaterActive    "Water timer active" { channel="awattar:bestprice:bridge1:water:active" }
Switch HeatpumpActive "Heat pump active"   { channel="awattar:bestprice:bridge1:heatpump:active" }
```

### Persistence

The bridge channels provide future prices as a time-series.
To show them in a chart, persist them with the `forecast` strategy in a persistence service that can store future values, e.g. InfluxDB, JDBC or In-Memory (rrd4j cannot).

influxdb.persist:

```java
Items {
    MarketNet, TotalGross : strategy = forecast
}
```

### Sitemap

```perl
sitemap awattar label="aWATTar"
{
 Frame label="Current Prices" {
  Text item=CurrentNet
  Text item=CurrentTotalGross
 }
 Frame label="Price Forecast" {
  Chart item=TotalGross service="influxdb" period=2h-12h interpolation="step" legend=false
  Chart item=TotalGross service="influxdb" period=D-D interpolation="step" legend=false
 }
 Frame label="Car Loader" {
  Switch item=CarActive
  Text item=CarStart
  Text item=CarEnd
  Text item=CarCountdown
  Text item=CarRemaining
  Text item=CarHours
 }
}
```

`period=2h-12h` shows the last 2 and the next 12 hours, `period=D-D` the last and the next 24 hours.
`interpolation="step"` draws each price as a flat step for its whole interval, so hourly aWATTar prices and quarter-hour Energy-Charts prices are both shown as they are billed.

### Usage hints

The idea of this binding is to support both automated and non automated components of your home.
For automated components, just decide when and how long you want to power them on and use the `active` switch of the bestprice thing to do so.
Many non automated components still allow some kind of locally programmed start and end times, e.g. washing machines or dishwashers.
So if you know your dishwasher needs less than 3 hour for one run and you want it to be done the next morning, use either the `countdown` or the `remaining` channel of a bestprice thing to determine the best start or end time to select.
