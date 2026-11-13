# aWATTar Binding

This binding provides access to the hourly prices for electricity for the German and Austrian provider aWATTar.

## Supported Things

There are three supported things.

### aWATTar Bridge

The `bridge` reads price data from the aWATTar API and stores the (optional) config values for VAT and energy base price.

### Prices Thing

The `prices` Thing provides todays and (after 14:00) tomorrows net and gross prices.

### Bestprice Thing

The `bestprice` Thing identifies the hours with the cheapest prices based on the given parameters.

**Deprecated:** the `bestprice` Thing will be removed in a future version.
Use the [Optimal Window automation add-on](https://www.openhab.org/addons/automation/optimalwindow/) together with the bridge time-series channels instead, see [Migrating from the bestprice Thing](#migrating-from-the-bestprice-thing).

Note: The Thing will schedule updates of the aWATTar API at 15:00, 18:00 and 21:00.
If late updates occur, e.g. after 21:00, there is a chance that consecutive best prices will be rescheduled.
As a consequence, a time schedule spanning over an update slot might be interrupted and rescheduled.

## Discovery

Auto discovery is not supported.

## Thing Configuration

### aWATTar Bridge

| Parameter  | Description                                                                                                                                                                                                                                                                                                     |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| vatPercent | Percentage of the value added tax to apply to net prices. Optional, defaults to 19.                                                                                                                                                                                                                             |
| basePrice  | The net(!) base price you have to pay for every kWh. Optional, but you most probably want to set it based on you delivery contract.                                                                                                                                                                             |
| timeZone   | The time zone the hour definitions of the things below refer to. Default is `CET`, as it corresponds to the aWATTar API. It is strongly recommended not to change this. However, if you do so, be aware that the prices delivered by the API will not cover a whole calendar day in this timezone. **Advanced** |
| country    | The country prices should be received for. Use `DE` for Germany or `AT` for Austria. `DE` is the default.                                                                                                                                                                                                       |
| serviceFee | The service fee in percent. Will be added to the total price. Will be calculated on top of the absolute price per hour. Default is `0`.                                                                                                                                                                         |

### Prices Thing

The prices thing does not need any configuration.

### Bestprice Thing

| Parameter     | Description                                                                                                                                                                                                  |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| rangeStart    | First hour of the time range the binding should search for the best prices. Default: `0`                                                                                                                     |
| rangeDuration | The duration of the time range the binding should search for best prices. Default: `24`                                                                                                                      |
| length        | number of best price hours to find within the range. This value has to be at least `1` and below `rangeDuration` Default: `1`                                                                                |
| consecutive   | if `true`, the thing identifies the cheapest consecutive range of `length` hours within the lookup range. Otherwise, the thing contains the cheapest `length` hours within the lookup range. Default: `true` |
| inverted      | if `true`, the worst prices will be searched instead of the best. Does currently not work in combination with 'consecutive'. Default: `false`                                                                |

#### Limitations

The channels of a bestprice thing are only defined when the binding has enough data to compute them.
The thing is recomputed after the end of the candidate time range for the next day, but only as soon as data for the next day is available from the aWATTar API, which is around 14:00.
So for a bestprice thing with `[ rangeStart=5, rangeDuration=5  ]` all channels will be undefined from 10:00 to 14:00.
Also, due to the time the aWATTar API delivers the data for the next day, it doesn't make sense to define a thing with `[ rangeStart=12, rangeDuration=20 ]` as the binding will be able to compute the channels only after 14:00.

## Channels

### Bridge

The bridge has two channels which support a time-series:

| channel      | type               | description                                                                                                                             |
| ------------ |--------------------| --------------------------------------------------------------------------------------------------------------------------------------- |
| market-net   | Number:EnergyPrice | This net market price per kWh. This is directly taken from the price the aWATTar API delivers.                                          |
| total-net    | Number:EnergyPrice | Sum of net market price and configured base price                                                                                       |

If you need gross prices, please use the [VAT profile](https://www.openhab.org/addons/transformations/vat/).
Use these channels to show or evaluate prices for today and tomorrow.

### Prices Thing

For every hour, the `prices` thing provides the following prices:

| channel      | type   | description                                                                                                                             |
| ------------ | ------ | --------------------------------------------------------------------------------------------------------------------------------------- |
| market-net   | Number | This net market price per kWh. This is directly taken from the price the aWATTar API delivers.                                          |
| market-gross | Number | The market price including VAT, using the defined VAT percentage.                                                                       |
| total-net    | Number | Sum of net market price and configured base price                                                                                       |
| total-gross  | Number | Sum of market and base price with VAT applied. Most probably this is the final price you will have to pay for one kWh in a certain hour |

All prices are available in each of the following channel groups:

| channel group                          | description                                                                                                                                                                          |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| current                                | The prices for the current hour                                                                                                                                                      |
| today00, today01, today02 ... today23  | **Deprecated**, use the bridge time-series channels instead. Hourly prices for today. `today00` provides the price from 0:00 to 1:00, `today01` from 1:00 to 02:00 and so on.     |
| tomorrow00, tomorrow01, ... tomorrow23 | **Deprecated**, use the bridge time-series channels instead. Hourly prices for the next day. They should be available starting at 14:00.                                             |

The `todayXX` and `tomorrowXX` channel groups are deprecated and will be removed in a future version.

### Bestprice Thing

| channel   | type        | description                                                                                                                                                                                               |
| --------- | ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| active    | Switch      | `ON` if the current time is within the bestprice period, `OFF` otherwise. If `consecutive` was set to `false`, this channel may change between `ON` and `OFF` multiple times within the bestprice period. |
| start     | DateTime    | The exact start time of the bestprice range. If `consecutive` was `false`, it is the start time of the first hour found.                                                                                  |
| end       | DateTime    | The exact end time of the bestprice range. If `consecutive` was `false`, it is the end time of the last hour found.                                                                                       |
| countdown | Number:Time | The time in minutes until start of the bestprice range. If start time passed. the channel will be set to `UNDEFINED` until the values for the next day are available.                                     |
| remaining | Number:Time | The time in minutes until end of the bestprice range. If start time passed. the channel will be set to `UNDEFINED` until the values for the next day are available.                                       |
| hours     | String      | A comma separated list of hours this bestprice period contains.                                                                                                                                           |

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

### Items

awattar.items:

```java
Number:EnergyPrice MarketNet "Market price (net) [%.3f %unit%]" { channel="awattar:bridge:bridge1:market-net" }
Number:EnergyPrice TotalNet  "Total price (net) [%.3f %unit%]"  { channel="awattar:bridge:bridge1:total-net" }

Number CurrentNet        "Current market price (net) [%.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:current#market-net" }
Number CurrentTotalGross "Current total price (gross) [%.2f ct/kWh]" { channel="awattar:prices:bridge1:price1:current#total-gross" }

DateTime    CarStart     "Start car loader [%1$tH:%1$tM]"  { channel="awattar:bestprice:bridge1:carloader:start" }
DateTime    CarEnd       "End car loader [%1$tH:%1$tM]"    { channel="awattar:bestprice:bridge1:carloader:end" }
Number:Time CarCountdown "Car loader starts in [%.0f min]" { channel="awattar:bestprice:bridge1:carloader:countdown" }
Number:Time CarRemaining "Car loader ends in [%.0f min]"   { channel="awattar:bestprice:bridge1:carloader:remaining" }
String      CarHours     "Car loader hours [%s]"           { channel="awattar:bestprice:bridge1:carloader:hours" }
Switch      CarActive    "Car loader active"               { channel="awattar:bestprice:bridge1:carloader:active" }

Switch WaterActive    "Water timer active" { channel="awattar:bestprice:bridge1:water:active" }
Switch HeatpumpActive "Heat pump active"   { channel="awattar:bestprice:bridge1:heatpump:active" }
```

### Persistence

The bridge channels provide future prices as a time-series.
To show them in a chart, persist them with the `forecast` strategy in a persistence service that can store future values, e.g. InfluxDB, JDBC or In-Memory (rrd4j cannot).

influxdb.persist:

```java
Items {
    MarketNet, TotalNet : strategy = forecast
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
  Chart item=TotalNet service="influxdb" period=2h-12h interpolation="step" legend=false
  Chart item=TotalNet service="influxdb" period=D-D interpolation="step" legend=false
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

### Usage hints

The idea of this binding is to support both automated and non automated components of your home.
For automated components, just decide when and how long you want to power them on and use the `active` switch of the bestprice thing to do so.
Many non automated components still allow some kind of locally programmed start and end times, e.g. washing machines or dishwashers.
So if you know your dishwasher needs less than 3 hour for one run and you want it to be done the next morning, use either the `countdown` or the `remaining` channel of a bestprice thing to determine the best start or end time to select.

## Migrating from the bestprice Thing

The best price logic moves to the [Optimal Window automation add-on](https://www.openhab.org/addons/automation/optimalwindow/).
It works on any Number Item that receives a price time-series, so link an Item to the bridge's `market-net` or `total-net` channel and persist it with the `forecast` strategy, see [Persistence](#persistence).

| bestprice Thing               | Optimal Window trigger                                 |
| ----------------------------- | ------------------------------------------------------ |
| bridge                        | `forecastItem`, the Item linked to the bridge channel  |
| `rangeStart`                  | `rangeStart`                                           |
| `rangeDuration`               | `rangeDuration`                                        |
| `length` (hours)              | `length` (duration, e.g. `3h` or `45m`)                |
| `consecutive`                 | `consecutive`                                          |
| `inverted`                    | `goal` = `maximum`                                     |
| channel `active`              | `activeItem`, or the "Within Optimal Window" condition |
| channels `start`, `end`       | `startItem`, `endItem`                                 |
| channel `remaining`           | `remainingItem`                                        |
| channel `hours`               | `windowTextItem`, e.g. `10:45–14:45`                   |
| channel `countdown`           | `countdownItem`                                        |
