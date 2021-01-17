# aWATTar Binding

This binding provides access to the hourly prices for electricity for the German and Austrian provider aWATTar.


## Supported Things

There are three supported things.

### aWATTar Bridge

The `awattar-bridge` reads price data from the aWATTar API and stores the (optional) config values for VAT and energy base price.

### Prices Thing

The `prices` Thing provides todays and (after 14:00) tomorrows net and gross prices. 

### Bestprice Thing

The `bestprice` Thing identifies the hours with the cheapest prices based on the given parameters.

## Discovery

Auto discovery is not supported.

## Thing Configuration

### aWATTar Bridge


| Parameter      | Description                                                                                                                |
|-------------|-------------------------------------------------------------------------------------------------------------------------------|
| vatPercent  | Percentage of the value added tax to apply to net prices. Optional, defaults to 19.                                           |
| basePrice   | The net(!) base price you have to pay for every kWh. Optional, but you most probably want to set it based on you delivery contract.  |

### Prices Thing

The prices thing does not need any configuration.

### Bestprice Thing

| Parameter      | Description                                                                                                                |
|-------------|-------------------------------------------------------------------------------------------------------------------------------|
| rangeStart  | First hour of the time range the binding should search for the best prices. Default: `0`                                     |
| rangeDuration  | The duration of the time range the binding should search for best prices. Default: `24`   |
| length      | number of best price hours to find within the range. This value has to be at least `1` and below `rangeDuration` Default: `1` |
| consecutive | if `true`, the thing identifies the cheapest consecutive range of `length` hours within the lookup range. Otherwise, the thing contains the cheapest `length` hours within the lookup range. Default: `true` |

#### Limitations

The channels of a bestprice thing are only defined when the binding has enough data to compute them.
The thing is recomputed after the end of the candidate time range for the next day, but only as soon as data for the next day is available from the aWATTar API, which is around 14:00.
So for a bestprice thing with `[ rangeStart=5, rangeDuration=5  ]` all channels will be undefined from 10:00 to 14:00.
Also, due to the time the aWATTar API delivers the data for the next day, it doesn't make sense to define a thing with `[ rangeStart=12, rangeDuration=20 ]` as the binding will be available to compute the channels only after 14:00.

## Channels

### Prices Thing

For every hour, the `prices` thing provides the following prices:
 
| channel  | type   | description                  |
|----------|--------|------------------------------|
| market-net  | Number | This net market price per kWh. This is directly taken from the price the aWATTar API delivers.  |
| market-gross  | Number | The market price including VAT, using the defined VAT percentage.  |
| total-net | Number | Sum of net market price and configured base price |
| total-gross | Number | Sum of market and base price with VAT applied. Most probably this is the final price you will have to pay for one kWh in a certain hour |


All prices are available in each of the following channel groups:


| channel group | description                  |
|----------|--------------------------------|
| current | The prices for the current hour |
| today00, today01, today02 ... today23 |  Hourly prices for today. `today00` provides the price from 0:00 to 1:00, today01` from 1:00 to 02:00 and so on. As long as the API is working, this data should always be available |
| tomorrow00, tomorrow01, ... tomorrow23 | Hourly prices for the next day. They should be available starting at  14:00. |


### Bestprice Thing

| channel  | type   | description                  |
|----------|--------|------------------------------|
| active | Switch | `ON` if the current time is within the bestprice period, `OFF` otherwise. If `consecutive` was set to `false`, this channel may change between `ON` and `OFF` multiple times within the bestprice period. |
| start  | DateTime | The exact start time of the bestprice range. If `consecutive` was `false`, it is the start time of the first hour found.  |
| end  | DateTime | The exact end time of the bestprice range. If `consecutive` was `false`, it is the end time of the last hour found.  |
| countdown  | String | The time until start of the bestprice range in format `HH:MM`. If start time passed. the channel will be set to `UNDEFINED` until the values for the next day are available.   |
| remaining | String | The time until end of the bestprice range in format `HH:MM`. If start time passed. the channel will be set to `UNDEFINED` until the values for the next day are available. |
| hours | String | A comma separated list of hours this bestprice period contains. |




## Full Example

### Things

awattar.things:

```
Bridge awattar:awattar-bridge:bridge1 "aWATTar Bridge" [vatPercent="19", basePrice="17.22"] {
        Thing prices price1 "aWATTar Price" []
        Thing bestprice carloader "Car Loader" [ rangeStart="22", rangeDuration="8", length="4", consecutive="true" ]
        Thing bestprice water "Water timer" [ rangeStart="19", rangeDuration="12", length="1" ]
        Thing bestprice pool "Pool pump" [ length="16", consecutive="false" ]
        Thing bestprice aqualeds "Aquarium leds" [ rangeStart="8", rangeDuration="14", length="10", consecutive="true" ]
}
```

### Items

awattar.items:

```
Number:Dimensionless pricenet "Current net market price [%d %unit%]"  { channel="awattar:prices:bridge1:price1:current#market-net" }
Number:Dimensionless pricegross "Current gross market price [%d %unit%]"  { channel="awattar:prices:bridge1:price1:current#market-gross" }
Number:Dimensionless totalnet "Current net total price [%d %unit%]"  { channel="awattar:prices:bridge1:price1:current#total-net" }
Number:Dimensionless totalgross "Current gross total price [%d %unit%]"  { channel="awattar:prices:bridge1:price1:current#total-gross" }

DateTime Carstart "Start car loader [%1$tH:%1$tM]"  { channel="awattar:bestprice:bridge1:carloader:start" }
DateTime CarEnd "End car loader [%1$tH:%1$tM]"  { channel="awattar:bestprice:bridge1:carloader:end" }
String CarCountdown "Countdown for car loader [%s]"  { channel="awattar:bestprice:bridge1:carloader:countdown" }
String CarHours "Hours for car loader [%s]"  { channel="awattar:bestprice:bridge1:carloader:hours" }
Switch CarActive { channel="awattar:bestprice:bridge1:carloader:active" }

DateTime Aqualedsstart "Start aqualeds  [%1$tH:%1$tM]"  { channel="awattar:bestprice:bridge1:aqualeds:start" }
DateTime AqualedsEnd "End aqualeds  [%1$tH:%1$tM]"  { channel="awattar:bestprice:bridge1:aqualeds:end" }
String AqualedsCountdown "Countdown for aqualeds  [%s]"  { channel="awattar:bestprice:bridge1:aqualeds:countdown" }
String AqualedsRemaining "Remaining time for aqualeds  [%s]"  { channel="awattar:bestprice:bridge1:aqualeds:remaining" }
Switch AqualedsActive { channel="awattar:bestprice:bridge1:aqualeds:active" }
String AqualedsHours "Hours for aqualeds [%s]"  { channel="awattar:bestprice:bridge1:aqualeds:hours" }

Switch PoolActive { channel="awattar:bestprice:bridge1:pool:active" }
String PoolHours "Hours for pool [%s]"  { channel="awattar:bestprice:bridge1:pool:hours" }
```

### Sitemap

(TODO)

### Usage hints

The idea of this binding is to support both automated and non automated components of your home.
For automated components, just decide when and how long you want to power them on and use the `active` switch of the bestprice thing to do so.
Many non automated components still allow some kind of locally programmed start and end times, e.g. washing machines or dishwashers.
So if you know your dishwasher needs less than 3 hour for one run and you want it to be done the next morning, use either the `countdown` or the `remaining` channel of a bestprice thing to determine the best start or end time to select.
