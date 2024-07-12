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
| today00, today01, today02 ... today23  | Hourly prices for today. `today00` provides the price from 0:00 to 1:00, `today01` from 1:00 to 02:00 and so on. As long as the API is working, this data should always be available |
| tomorrow00, tomorrow01, ... tomorrow23 | Hourly prices for the next day. They should be available starting at  14:00.                                                                                                         |

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
Bridge awattar:bridge:bridge1 "aWATTar Bridge" [ country="DE", vatPercent="19", basePrice="17.22"] {
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
Number:Dimensionless currentnet "Current price [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:current#market-net" }
Number:Dimensionless currentgross "Current price [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:current#market-gross" }
Number:Dimensionless totalnet "Current price [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:current#total-net" }
Number:Dimensionless totalgross "Current price [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:current#total-gross" }
Number:Dimensionless totalgross "Current price [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:current#total-gross" }

Number:Dimensionless today00 "Today 00-01 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today00#total-gross" }
Number:Dimensionless today01 "Today 01-02 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today01#total-gross" }
Number:Dimensionless today02 "Today 02-03 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today02#total-gross" }
Number:Dimensionless today03 "Today 03-04 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today03#total-gross" }
Number:Dimensionless today04 "Today 04-05 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today04#total-gross" }
Number:Dimensionless today05 "Today 05-06 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today05#total-gross" }
Number:Dimensionless today06 "Today 06-07 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today06#total-gross" }
Number:Dimensionless today07 "Today 07-08 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today07#total-gross" }
Number:Dimensionless today08 "Today 08-09 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today08#total-gross" }
Number:Dimensionless today09 "Today 09-10 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today09#total-gross" }
Number:Dimensionless today10 "Today 10-11 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today10#total-gross" }
Number:Dimensionless today11 "Today 11-12 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today11#total-gross" }
Number:Dimensionless today12 "Today 12-13 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today12#total-gross" }
Number:Dimensionless today13 "Today 13-14 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today13#total-gross" }
Number:Dimensionless today14 "Today 14-15 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today14#total-gross" }
Number:Dimensionless today15 "Today 15-16 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today15#total-gross" }
Number:Dimensionless today16 "Today 16-17 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today16#total-gross" }
Number:Dimensionless today17 "Today 17-18 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today17#total-gross" }
Number:Dimensionless today18 "Today 18-19 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today18#total-gross" }
Number:Dimensionless today19 "Today 19-20 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today19#total-gross" }
Number:Dimensionless today20 "Today 20-21 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today20#total-gross" }
Number:Dimensionless today21 "Today 21-22 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today21#total-gross" }
Number:Dimensionless today22 "Today 22-23 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today22#total-gross" }
Number:Dimensionless today23 "Today 23-00 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:today23#total-gross" }

Number:Dimensionless tomorrow00 "Tomorrow 00-01 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow00#total-gross" }
Number:Dimensionless tomorrow01 "Tomorrow 01-02 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow01#total-gross" }
Number:Dimensionless tomorrow02 "Tomorrow 02-03 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow02#total-gross" }
Number:Dimensionless tomorrow03 "Tomorrow 03-04 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow03#total-gross" }
Number:Dimensionless tomorrow04 "Tomorrow 04-05 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow04#total-gross" }
Number:Dimensionless tomorrow05 "Tomorrow 05-06 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow05#total-gross" }
Number:Dimensionless tomorrow06 "Tomorrow 06-07 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow06#total-gross" }
Number:Dimensionless tomorrow07 "Tomorrow 07-08 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow07#total-gross" }
Number:Dimensionless tomorrow08 "Tomorrow 08-09 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow08#total-gross" }
Number:Dimensionless tomorrow09 "Tomorrow 09-10 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow09#total-gross" }
Number:Dimensionless tomorrow10 "Tomorrow 10-11 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow10#total-gross" }
Number:Dimensionless tomorrow11 "Tomorrow 11-12 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow11#total-gross" }
Number:Dimensionless tomorrow12 "Tomorrow 12-13 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow12#total-gross" }
Number:Dimensionless tomorrow13 "Tomorrow 13-14 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow13#total-gross" }
Number:Dimensionless tomorrow14 "Tomorrow 14-15 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow14#total-gross" }
Number:Dimensionless tomorrow15 "Tomorrow 15-16 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow15#total-gross" }
Number:Dimensionless tomorrow16 "Tomorrow 16-17 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow16#total-gross" }
Number:Dimensionless tomorrow17 "Tomorrow 17-18 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow17#total-gross" }
Number:Dimensionless tomorrow18 "Tomorrow 18-19 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow18#total-gross" }
Number:Dimensionless tomorrow19 "Tomorrow 19-20 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow19#total-gross" }
Number:Dimensionless tomorrow20 "Tomorrow 20-21 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow20#total-gross" }
Number:Dimensionless tomorrow21 "Tomorrow 21-22 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow21#total-gross" }
Number:Dimensionless tomorrow22 "Tomorrow 22-23 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow22#total-gross" }
Number:Dimensionless tomorrow23 "Tomorrow 23-00 [%2.2f ct/kWh]"  { channel="awattar:prices:bridge1:price1:tomorrow23#total-gross" }

DateTime CarStart "Start car loader [%1$tH:%1$tM]"  { channel="awattar:bestprice:bridge1:carloader:start" }
DateTime CarEnd "End car loader [%1$tH:%1$tM]"  { channel="awattar:bestprice:bridge1:carloader:end" }
String CarCountdown "Countdown for car loader [%s]"  { channel="awattar:bestprice:bridge1:carloader:countdown" }
String CarHours "Hours for car loader [%s]"  { channel="awattar:bestprice:bridge1:carloader:hours" }
Switch CarActive { channel="awattar:bestprice:bridge1:carloader:active" }

Switch WaterActive { channel="awattar:bestprice:bridge1:water:active" }
Switch HeatpumpActive { channel="awattar:bestprice:bridge1:heatpump:active" }
```

### Sitemap

```perl
sitemap default label="aWATTar Sitemap"
{
 Frame label="Car Loader" {
  Switch item=CarActive
  Text item=CarCountdown
  Text item=CarStart
  Text item=CarEnd
  Text item=CarHours
 }
 Frame label="Current Prices" {
  Text label="Current Net" item=currentnet
  Text label="Current Gross" item=currentgross
  Text label="Total Net" item=totalnet
  Text label="Total Gross" item=totalgross
 }
 Frame label="Todays Prices (total gross)" {
  Text item=today00
  Text item=today01
  Text item=today02
  Text item=today03
  Text item=today04
  Text item=today05
  Text item=today06
  Text item=today07
  Text item=today08
  Text item=today09
  Text item=today10
  Text item=today11
  Text item=today12
  Text item=today13
  Text item=today14
  Text item=today15
  Text item=today16
  Text item=today17
  Text item=today18
  Text item=today19
  Text item=today20
  Text item=today21
  Text item=today22
  Text item=today23
 }
 Frame label="Tomorrows Prices (total gross)" {
  Text item=tomorrow00
  Text item=tomorrow01
  Text item=tomorrow02
  Text item=tomorrow03
  Text item=tomorrow04
  Text item=tomorrow05
  Text item=tomorrow06
  Text item=tomorrow07
  Text item=tomorrow08
  Text item=tomorrow09
  Text item=tomorrow10
  Text item=tomorrow11
  Text item=tomorrow12
  Text item=tomorrow13
  Text item=tomorrow14
  Text item=tomorrow15
  Text item=tomorrow16
  Text item=tomorrow17
  Text item=tomorrow18
  Text item=tomorrow19
  Text item=tomorrow20
  Text item=tomorrow21
  Text item=tomorrow22
  Text item=tomorrow23
 }
}
```

### Usage hints

The idea of this binding is to support both automated and non automated components of your home.
For automated components, just decide when and how long you want to power them on and use the `active` switch of the bestprice thing to do so.
Many non automated components still allow some kind of locally programmed start and end times, e.g. washing machines or dishwashers.
So if you know your dishwasher needs less than 3 hour for one run and you want it to be done the next morning, use either the `countdown` or the `remaining` channel of a bestprice thing to determine the best start or end time to select.
