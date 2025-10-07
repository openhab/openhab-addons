# Tibber Binding

The Tibber Binding retrieves `prices` from  [Tibber API](https://developer.tibber.com).
Users equipped with Tibber Pulse hardware can connect in addition to [live group](#live-group) and [statistics group](#statistics-group).

## Supported Things

| Type      | ID        | Description               |
|-----------|-----------|---------------------------|
| Thing     | tibberapi | Connection to Tibber API  |

## Thing Configuration

| Name          | Type      | Description                           | Default   | Required  |
|---------------|-----------|---------------------------------------|-----------|-----------|
| token         | text      | Tibber Personal Token                 | N/A       | yes       |
| homeid        | text      | Tibber Home ID                        | N/A       | yes       |
| updateHour    | integer   | Hour when spot prices are updated     | 13        | yes       |

Note: Tibber token is retrieved from your Tibber account:
[Tibber Account](https://developer.tibber.com/settings/accesstoken)

Note: Tibber HomeId is retrieved from [developer.tibber.com](https://developer.tibber.com/explorer):

- Sign in (Tibber user account) and "load" personal token.
- Copy query from below and paste into the Tibber API Explorer, and run query.
- If Tibber Pulse is connected, the Tibber API Explorer will report "true" for "realTimeConsumptionEnabled"
- Copy HomeId from Tibber API Explorer, without quotation marks, and use this in the bindings configuration.

```graphql
{
  viewer {
    homes {
      id
      features {
        realTimeConsumptionEnabled
      }
    }
  }
}
```

If user have multiple HomeIds / Pulse, separate Things have to be created for the different/desired HomeIds.

## Channels

### `price` group

Current and forecast Tibber price information.
All read-only.

| Channel ID        | Type                 | Description                                | Time Series  |
|-------------------|----------------------|--------------------------------------------|--------------|
| total             | Number:EnergyPrice   | Total price including energy and taxes     | yes          |
| spot              | Number:EnergyPrice   | Spot prices for energy today and tomorrow  | yes          |
| tax               | Number:EnergyPrice   | Taxes and additional expenses              | yes          |
| level             | Number               | Price levels for today and tomorrow        | yes          |
| average           | Number:EnergyPrice   | Average price from last 24 hours           | yes          |

Channel `spot-price` is _deprecated_ and will be removed in the next major update.
It's still available as advanced channel.
Naming was misleading as it reflected the total price and not the [Nord Pool spot price](https://developer.tibber.com/docs/reference#price) used by Tibber.

The `level` number is mapping the [Tibber Rating](https://developer.tibber.com/docs/reference#pricelevel) into numbers.
Zero reflects _normal_ price while values above 0 are _expensive_ and values below 0 are _cheap_.

Mapping:

- Very Cheap: -2
- Cheap: -1
- Normal: 0
- Expensive: 1
- Very Expensive: 2

The `average` values are not delivered by the Tibber API.
It's calculated by the binding to provide a trend line for the last 24 hours.
After initial setup the average values will stay NULL until the next day because the previous 24 h prices cannot be obtained by the Tibber API.

Please note time series are not supported by the default [rrd4j](https://www.openhab.org/addons/persistence/rrd4j/) persistence.
The items connected to the above channels needs to be stored in e.g. [InfluxDB](https://www.openhab.org/addons/persistence/influxdb/) or [InMemory](https://www.openhab.org/addons/persistence/inmemory/).

### `live` group

Live information from Tibber Pulse.
All values read-only.

| Channel ID                 | Type                     | Description                                                                      |
|----------------------------|--------------------------|----------------------------------------------------------------------------------|
| consumption                | Number:Power             | Consumption at the moment in watts                                               |
| minimum-consumption        | Number:Power             | Minimum power consumption since midnight in watts                                |
| peak-consumption           | Number:Power             | Peak power consumption since midnight in watts                                   |
| average-consumption        | Number:Power             | Average power consumption since midnight in watts                                |
| production                 | Number:Power             | Net power production at the moment in watts                                      |
| minimum-production         | Number:Power             | Minimum net power production since midnight in watts                             |
| peak-production            | Number:Power             | Maximum net power production since midnight in watts                             |
| power-balance              | Number:Power             | Current power consumption (as positive value) and production (as negative value) |
| voltage1                   | Number:ElectricPotential | Electric potential on phase 1                                                    |
| voltage2                   | Number:ElectricPotential | Electric potential on phase 2                                                    |
| voltage3                   | Number:ElectricPotential | Electric potential on phase 3                                                    |
| current1                   | Number:ElectricCurrent   | Electric current on phase 1                                                      |
| current2                   | Number:ElectricCurrent   | Electric current on phase 2                                                      |
| current3                   | Number:ElectricCurrent   | Electric current on phase 3                                                      |

### `statistics` group

Statistic information about total, daily and last hour energy consumption and production.
All values read-only.

| Channel ID            | Type                      | Description                                                   |
|-----------------------|---------------------------|---------------------------------------------------------------|
| total-consumption     | Number:Energy             | Total energy consumption measured by Tibber Pulse meter       |
| daily-consumption     | Number:Energy             | Energy consumed since midnight in kilowatt-hours              |
| daily-cost            | Number:Currency           | Accumulated cost since midnight                               |
| last-hour-consumption | Number:Energy             | Energy consumed since last hour shift in kilowatt-hours       |
| total-production      | Number:Energy             | Total energy production measured by Tibber Pulse meter        |
| daily-production      | Number:Energy             | Net energy produced since midnight in kilowatt-hours          |
| last-hour-production  | Number:Energy             | Net energy produced since last hour shift in kilowatt-hours   |

## Thing Actions

Thing actions can be used to perform calculations on the current available price information cached by the binding.
Cache contains energy prices from today and after reaching the `updateHour` also for tomorrow.
This is for planning when and for what cost a specific electric consumer can be started.

Performing a calcuation a `parameters` object is needed containing e.g. your boundaries for the calculation.
Parameter object allow 2 types: Java `Map` or JSON `String`.
The result is returned as JSON encoded `String`.
Refer below sections how the result looks like.
If the action cannot be performed, a warning will be logged and an empty `String` will be returned.
Some real life scenarios are schown in [Thing Actions](#thing-actions) section.

### `priceInfoStart`

Returns starting point as `Instant` of first available energy price.
It's not allowed to start calculations before this timestamp.

In case of error `Instant.MAX` is returned.

### `priceInfoEnd`

Returns end point as `Instant` of the last available energy price.
It's not allowed to exceed calculations after this timestamp.

In case of error `Instant.MIN` is returned.

### `listPrices`

List prices in ascending / decending _price_ order.
Use [persistence estensions](https://www.openhab.org/docs/configuration/persistence.html#persistence-extensions-in-scripts-and-rules) if you need _time_ ordering.

#### Parameters

| Name          | Type      | Description                           | Default           | Required  |
|---------------|-----------|---------------------------------------|-------------------|-----------|
| earliestStart | Instant   | Earliest start time                   | now               | no        |
| latestStop    | Instant   | Latest end time                       | `priceInfoEnd`    | no        |
| ascending     | boolean   | Price sorting order                   | true              | no        |

#### Example

```java
rule "Tibber Price List"
when
    System started // use your trigger
then
    var actions = getActions("tibber","tibber:tibberapi:xyz")
    // parameters empty => default parameters are used = starting from now till end of available price infos, ascending
    var parameters = "{}"
    var result = actions.listPrices(parameters)
    val numberOfPrices = transform("JSONPATH", "$.size", result)
    logInfo("TibberPriceList",result)
    for(var i=0; i<Integer.valueOf(numberOfPrices); i++) {
        // get values and convert them into correct format
        val priceString = transform("JSONPATH", "$.priceList["+i+"].price", result)
        val price = Double.valueOf(priceString)
        val startsAtString = transform("JSONPATH", "$.priceList["+i+"].startsAt", result)
        val startsAt = Instant.parse(startsAtString)
        logInfo("TibberPriceList","PriceInfo "+i+" : " + price + " Starts at : " + startsAt.atZone(ZoneId.systemDefault()))
    }
end
```

## Console output

```text
2025-05-29 15:52:31.345 [INFO ] [ab.core.model.script.TibberPriceList] - PriceInfo 0 : 0.1829 Starts at : 2025-05-30T13:00+02:00[Europe/Berlin]
2025-05-29 15:52:31.349 [INFO ] [ab.core.model.script.TibberPriceList] - PriceInfo 1 : 0.183 Starts at : 2025-05-30T14:00+02:00[Europe/Berlin]
2025-05-29 15:52:31.352 [INFO ] [ab.core.model.script.TibberPriceList] - PriceInfo 2 : 0.1842 Starts at : 2025-05-29T15:52:31.341193101+02:00[Europe/Berlin]
...
```

### Result

JSON encoded `String` result with keys

| Key           | Type      | Description                           |
|---------------|-----------|---------------------------------------|
| size          | int       | Size of price list                    |
| priceList     | JsonArray | Array of `priceInfo` entries          |

JSON Object `priceInfo`

| Key           | Type      | Description                           |
|---------------|-----------|---------------------------------------|
| startsAt      | String    | String encoded Instant                |
| duration      | int       | Price duration in seconds             |
| price         | double    | Price in your currency                |

#### Example

```json
{
    "size": 4,
    "priceList": [
        {
            "price": 0.1623,
            "duration": 3600,
            "level": -1,
            "startsAt": "2025-06-01T12:00:00Z"
        },
        {
            "price": 0.168,
            "duration": 3600,
            "level": -1,
            "startsAt": "2025-06-01T13:00:00Z"
        },
        {
            "price": 0.1712,
            "duration": 3600,
            "level": -1,
            "startsAt": "2025-06-01T11:00:00Z"
        },
        {
            "price": 0.1794,
            "duration": 3600,
            "level": -1,
            "startsAt": "2025-06-01T14:00:00Z"
        }
    ]
}
```

### `bestPricePeriod`

Calculates best cost for a consecutive period.
For use cases like dishwasher or laundry.

#### Parameters

| Name          | Type      | Description                                   | Default           | Required  |
|---------------|-----------|-----------------------------------------------|-------------------|-----------|
| earliestStart | Instant   | Earliest start time                           | now               | no        |
| latestStop    | Instant   | Latest end time                               | `priceInfoEnd`    | no        |
| power         | int       | Power in watts                                | N/A               | no        |
| duration      | String    | Duration as String with units `h`,`m` or `s`  | N/A               | true      |
| curve         | JsonArray | Array with `curveEntry` elements              | N/A               | no        |

Provide either

- `power` and `duration` for constant consumption _or_
- `curve` for sophisticated use cases like a recorded laundry power timeseries

JSON Object `curveEntry`

| Key           | Type      | Description                           |
|---------------|-----------|---------------------------------------|
| timestamp     | String    | String encoded Instant                |
| power         | int       | Power in watts                        |
| duration      | int       | Duration in seconds                   |

#### Example

```java
import java.util.Map;

var Timer bestPriceTimer = null

rule "Tibber Best Price"
when
    System started // use your trigger
then
    // get actions
    var actions = getActions("tibber","tibber:tibberapi:xyz")
    //create parameters for calculation
    var parameters = Map.of("duration", "1 h 34 m")
    // perform calculation
    var result = actions.bestPricePeriod(parameters)
    // log result, no prices given because no power value given
    logInfo("TibberBestPrice",result)
    
    // parameters with power value - as example use java Map instead of JSON  
    parameters = Map.of("duration", "1 h 34 m","power",423,"latestEnd",Instant.now().plusSeconds(7200))
    result = actions.bestPricePeriod(parameters)
    logInfo("TibberBestPrice",result)
    // calculate time between now and cheapest start and start timer to execute action
    val startsAt = transform("JSONPATH", "$.cheapestStart", result)
    var secondsTillStart = Duration.between(Instant.now(), Instant.parse(startsAt)).getSeconds()
    // is start shall happen immediately avoid negative values
    secondsTillStart = Math::max(0,secondsTillStart) 
    bestPriceTimer = createTimer(now.plusSeconds(secondsTillStart), [|           
        logInfo("TibberBestPrice","Start your device")
    ])
end
```

Console output:

```text
2025-05-29 16:07:40.858 [TRACE] [.internal.calculator.PriceCalculator] - Calculation time 2 ms for 1819 iterations
2025-05-29 16:07:40.860 [INFO ] [ab.core.model.script.TibberBestPrice] - {"cheapestStart":"2025-05-30T11:00:40.856950656Z","mostExpensiveStart":"2025-05-30T18:25:40.856950656Z"}
2025-05-29 16:07:40.861 [TRACE] [.internal.calculator.PriceCalculator] - Calculation time 0 ms for 26 iterations
2025-05-29 16:07:40.863 [INFO ] [ab.core.model.script.TibberBestPrice] - {"highestPrice":0.138712416,"lowestPrice":0.13126169399999998,"cheapestStart":"2025-05-29T14:07:40.861730141Z","averagePrice":0.134152053,"mostExpensiveStart":"2025-05-29T14:32:40.861730141Z"}
2025-05-29 16:07:40.967 [INFO ] [ab.core.model.script.TibberBestPrice] - Start your device
```

#### Result

JSON encoded `String` result with keys

| Key                   | Type      | Description                           |
|-----------------------|-----------|---------------------------------------|
| cheapestStart         | String    | Timestamp of cheapest start           |
| lowestPrice           | double    | Price of the cheapest period          |
| mostExpensiveStart    | String    | Timestamp of most expensive start     |
| highestPrice          | double    | Price of the most expensive period    |
| averagePrice          | double    | Average price within the period       |

#### Result Example

```json
{
    "highestPrice": 0.18921223574999999,
    "lowestPrice": 0.17497929625,
    "cheapestStart": "2025-05-31T15:12:58.135876781Z",
    "averagePrice": 0.1810258046730769,
    "mostExpensiveStart": "2025-05-31T15:37:58.135876781Z"
}
```

### `bestPriceSchedule`

Calculates best cost for a non-consecutive schedule.
For use cases like battery electric vehicle or heat-pump.

#### Parameters

| Name          | Type      | Description                               | Default          | Required  |
|---------------|-----------|-------------------------------------------|-------------------|-----------|
| earliestStart | Instant   | Earliest start time                       | now               | no        |
| latestStop    | Instant   | Latest end time                           | `priceInfoEnd`    | no        |
| power         | int       | Needed power                              | N/A               | no        |
| duration      | int       | Duration in seconds or String (8h 15m)    | N/A               | yes       |

#### Example

```java
rule "Tibber Schedule Calculation"
when
    System started // use your trigger
then
    var actions = getActions("tibber","tibber:tibberapi:xyz")
    // long period with constant power value
    var parameters = "{\"power\": 11000, \"duration\": \"8h 15m\"}"
    var result = actions.bestPriceSchedule(parameters)
    // get cost and convert it into double value
    val costString = transform("JSONPATH", "$.cost", result)
    val cost = Double.valueOf(costString)
    val scheduleSize = transform("JSONPATH", "$.size", result)
    logInfo("TibberSchedule",result)
    logInfo("TibberSchedule","Cost : " + cost+" Number of schedules : " + scheduleSize)
    for(var i=0; i<Integer.valueOf(scheduleSize); i++) {
        val schedule = transform("JSONPATH", "$.schedule["+i+"]", result)
        logInfo("TibberSchedule","Schedule "+i+": " + schedule)
        val scheduleStartString = transform("JSONPATH", "$.schedule["+i+"].start", result)
        val scheduleStart = Instant.parse(scheduleStartString)
        logInfo("TibberSchedule","Schedule "+i+" start: " + scheduleStart.atZone(ZoneId.systemDefault()).toString)
    }
end

```

Console output

```text
2025-05-29 19:42:38.223 [INFO ] [hab.core.model.script.TibberSchedule] - {"cost":17.004625,"size":2,"schedule":[{"start":"2025-05-30T08:00:00Z","stop":"2025-05-30T16:00:00Z","duration":28800,"cost":16.407600000000002},{"start":"2025-05-29T23:00:00Z","stop":"2025-05-29T23:15:00Z","duration":900,"cost":0.5970249999999999}]}
2025-05-29 19:42:38.225 [INFO ] [hab.core.model.script.TibberSchedule] - Cost : 17.004625 Number of schedules : 2
2025-05-29 19:42:38.227 [INFO ] [hab.core.model.script.TibberSchedule] - Schedule 0: {start=2025-05-30T08:00:00Z, stop=2025-05-30T16:00:00Z, duration=28800, cost=16.407600000000002}
2025-05-29 19:42:38.230 [INFO ] [hab.core.model.script.TibberSchedule] - Schedule 0 start: 2025-05-30T10:00+02:00[Europe/Berlin]
2025-05-29 19:42:38.232 [INFO ] [hab.core.model.script.TibberSchedule] - Schedule 1: {start=2025-05-29T23:00:00Z, stop=2025-05-29T23:15:00Z, duration=900, cost=0.5970249999999999}
2025-05-29 19:42:38.234 [INFO ] [hab.core.model.script.TibberSchedule] - Schedule 1 start: 2025-05-30T01:00+02:00[Europe/Berlin]
```

#### Result

JSON encoded `String` result with keys

| Key           | Type      | Description                           |
|---------------|-----------|---------------------------------------|
| size          | int       | Number of schedules                   |
| schedule      | JsonArray | Array of `scheduleEntry` elements     |

JSON Object `scheduleEntry`

| Key           | Type      | Description                           |
|---------------|-----------|---------------------------------------|
| timestamp     | String    | String encoded Instant                |
| duration      | int       | Price duration in seconds             |
| price         | double    | Price in your currency                |

Provide either

- `timestamp` - duration will be calculated automatically _or_
- `duration` if you already know it

#### Result Example

```json
{
    "cost": 16.092450000000003,
    "size": 2,
    "schedule": [
        {
            "start": "2025-06-01T08:00:00Z",
            "stop": "2025-06-01T16:00:00Z",
            "duration": 28800,
            "cost": 15.579300000000002
        },
        {
            "start": "2025-06-01T07:00:00Z",
            "stop": "2025-06-01T07:15:00Z",
            "duration": 900,
            "cost": 0.51315
        }
    ]
}
```

## Full Example

Full example with `demo.things` and `demo.items`

### `demo.things` Example

```java
Thing tibber:tibberapi:xyz [ homeid="xxx", token="xxxxxxx", updateHour=13 ]
```

### `demo.items` Example

```java
Number:EnergyPrice          Tibber_API_Spot_Prices              "Spot Prices"                {channel="tibber:tibberapi:xyz:price#spot-price"}
Number                      Tibber_API_Price_Level              "Price Level"                {channel="tibber:tibberapi:xyz:price#level"}
Number:EnergyPrice          Tibber_API_Average                  "Average Price"              {channel="tibber:tibberapi:xyz:price#average"}

Number:Power                Tibber_API_Live_Consumption         "Live Consumption"           {channel="tibber:tibberapi:xyz:live#consumption"}
Number:Power                Tibber_API_Minimum_Consumption      "Minimum Consumption"        {channel="tibber:tibberapi:xyz:live#minimum-consumption"}
Number:Power                Tibber_API_Peak_Consumption         "Peak Consumption"           {channel="tibber:tibberapi:xyz:live#peak-consumption"}
Number:Power                Tibber_API_Average_Consumption      "Average Consumption"        {channel="tibber:tibberapi:xyz:live#average-consumption"}
Number:Power                Tibber_API_Live_Production          "Live Production"            {channel="tibber:tibberapi:xyz:live#production"}
Number:Power                Tibber_API_Minimum_Production       "Minimum Production"         {channel="tibber:tibberapi:xyz:live#minimum-production"}
Number:Power                Tibber_API_Peak_Production          "Peak Production"            {channel="tibber:tibberapi:xyz:live#peak-production"}
Number:Power                Tibber_API_Power_Balance            "Power Balance"              {channel="tibber:tibberapi:xyz:live#consumption-and-production"}
Number:ElectricPotential    Tibber_API_Voltage_1                "Voltage 1"                  {channel="tibber:tibberapi:xyz:live#voltage1"}
Number:ElectricPotential    Tibber_API_Voltage_2                "Voltage 2"                  {channel="tibber:tibberapi:xyz:live#voltage2"}
Number:ElectricPotential    Tibber_API_Voltage_3                "Voltage 3"                  {channel="tibber:tibberapi:xyz:live#voltage3"}
Number:ElectricCurrent      Tibber_API_Current_1                "Current 1"                  {channel="tibber:tibberapi:xyz:live#current1"}
Number:ElectricCurrent      Tibber_API_Current_2                "Current 2"                  {channel="tibber:tibberapi:xyz:live#current2"}
Number:ElectricCurrent      Tibber_API_Current_3                "Current 3"                  {channel="tibber:tibberapi:xyz:live#current3"}

Number:Energy               Tibber_API_Total_Consumption        "Total Consumption"          {channel="tibber:tibberapi:xyz:statistics#total-consumption"}
Number:Energy               Tibber_API_Daily_Consumption        "Daily Consumption"          {channel="tibber:tibberapi:xyz:statistics#daily-consumption"}
Number:Currency             Tibber_API_Daily_Cost               "Daily Cost"                 {channel="tibber:tibberapi:xyz:statistics#daily-cost"}
Number:Energy               Tibber_API_Last_Hour_Consumption    "Last Hour Consumption"      {channel="tibber:tibberapi:xyz:statistics#last-hour-consumption"}
Number:Energy               Tibber_API_Total_Production         "Total Production"           {channel="tibber:tibberapi:xyz:statistics#total-production"}
Number:Energy               Tibber_API_Daily_Production         "Daily Production"           {channel="tibber:tibberapi:xyz:statistics#daily-production"}
Number:Energy               Tibber_API_Last_Hour_Production     "Last Hour Production"       {channel="tibber:tibberapi:xyz:statistics#last-hour-production"}
```
