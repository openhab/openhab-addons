# Tibber Binding

The Tibber Binding supports two independent ways to get data from Tibber.

The **cloud** Thing `tibberapi` retrieves `prices` from the [Tibber API](https://developer.tibber.com).
If you have Tibber Pulse hardware, you can also use the [live group](#live-group) and [statistics group](#statistics-group).
Historical energy data (consumption, cost, and production) is available in the [history group](#history-group) for all users.
It needs a Tibber account.

The **local** Things `pulse-bridge` and `pulse-meter` read the live data of a Tibber Pulse directly from the Tibber bridge on your local network.
They need no Tibber account and no Internet access, see [Local Pulse Bridge](#local-pulse-bridge).

The sections up to the [Full Example](#full-example) describe the cloud part.
The section [Local Pulse Bridge](#local-pulse-bridge) describes the local part and can be read on its own.

## Supported Things

| Type   | ID             | Thing Type UID        | Description                                          |
|--------|----------------|-----------------------|------------------------------------------------------|
| Thing  | `tibberapi`    | `tibber:tibberapi`    | Cloud: connection to the Tibber API                  |
| Bridge | `pulse-bridge` | `tibber:pulse-bridge` | Local: connection to one Tibber bridge               |
| Thing  | `pulse-meter`  | `tibber:pulse-meter`  | Local: one electricity meter read through the bridge |

## Thing Configuration

| Name       | Type    | Description                             | Default | Required |
|------------|---------|-----------------------------------------|---------|----------|
| token      | text    | Tibber Personal Token                   | N/A     | yes      |
| homeid     | text    | Tibber Home ID                          | N/A     | yes      |
| updateHour | integer | Local hour when spot prices are updated | 13      | yes      |

Note: The Tibber token is retrieved from your Tibber account:
[Tibber Account](https://developer.tibber.com/settings/accesstoken)

Note: The Tibber Home ID is retrieved from [developer.tibber.com](https://developer.tibber.com/explorer):

- Sign in (Tibber user account) and "load" personal token.
- Copy query from below and paste into the Tibber API Explorer, and run query.
- If Tibber Pulse is connected, the Tibber API Explorer will report "true" for "realTimeConsumptionEnabled"
- Copy the Home ID from the Tibber API Explorer, without quotation marks, and use it in the binding configuration.

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

If you have multiple Home IDs/Pulse devices, create separate Things for each Home ID.

## Channels

### `price` group

Current and forecast Tibber price information.
All values are read-only.

| Channel ID | Type               | Description                               | Time Series |
|------------|--------------------|-------------------------------------------|-------------|
| total      | Number:EnergyPrice | Total price including energy and taxes    | yes         |
| spot       | Number:EnergyPrice | Spot prices for energy today and tomorrow | yes         |
| tax        | Number:EnergyPrice | Taxes and additional expenses             | yes         |
| level      | Number             | Price levels for today and tomorrow       | yes         |
| average    | Number:EnergyPrice | Average price from last 24 hours          | yes         |

Channel `spot-price` is _deprecated_ and will be removed in the next major update.
It's still available as an advanced channel.
The naming was misleading as it reflected the total price and not the [Nord Pool spot price](https://developer.tibber.com/docs/reference#price) used by Tibber.

The `level` number maps the [Tibber Rating](https://developer.tibber.com/docs/reference#pricelevel) into numbers.
Zero reflects _normal_ price, while values above 0 are _expensive_ and values below 0 are _cheap_.

Mapping:

- Very Cheap: -2
- Cheap: -1
- Normal: 0
- Expensive: 1
- Very Expensive: 2

The `average` values are not delivered by the Tibber API.
They are calculated by the binding to provide a trend line for the last 24 hours.
After the initial setup, the average values will remain NULL until the next day because the previous 24 h prices cannot be obtained by the Tibber API.

Please note that time series are not supported by the default [rrd4j](https://www.openhab.org/addons/persistence/rrd4j/) persistence.
The items connected to the above channels need to be stored in, e.g., [InfluxDB](https://www.openhab.org/addons/persistence/influxdb/) or [InMemory](https://www.openhab.org/addons/persistence/inmemory/).

#### Trigger Channels

Channel `event` can trigger the following events:

| Event               | Description                    |
|---------------------|--------------------------------|
| DAY_AHEAD_AVAILABLE | Day-ahead prices are available |

### `live` group

Live information from Tibber Pulse.
All values are read-only.

| Channel ID          | Type                     | Description                                                                      |
|---------------------|--------------------------|----------------------------------------------------------------------------------|
| consumption         | Number:Power             | Consumption at the moment in watts                                               |
| minimum-consumption | Number:Power             | Minimum power consumption since midnight in watts                                |
| peak-consumption    | Number:Power             | Peak power consumption since midnight in watts                                   |
| average-consumption | Number:Power             | Average power consumption since midnight in watts                                |
| production          | Number:Power             | Net power production at the moment in watts                                      |
| minimum-production  | Number:Power             | Minimum net power production since midnight in watts                             |
| peak-production     | Number:Power             | Maximum net power production since midnight in watts                             |
| power-balance       | Number:Power             | Current power consumption (as positive value) and production (as negative value) |
| voltage1            | Number:ElectricPotential | Electric potential on phase 1                                                    |
| voltage2            | Number:ElectricPotential | Electric potential on phase 2                                                    |
| voltage3            | Number:ElectricPotential | Electric potential on phase 3                                                    |
| current1            | Number:ElectricCurrent   | Electric current on phase 1                                                      |
| current2            | Number:ElectricCurrent   | Electric current on phase 2                                                      |
| current3            | Number:ElectricCurrent   | Electric current on phase 3                                                      |

### `statistics` group

Statistical information about total, daily, and last-hour energy consumption and production.
All values are read-only.

| Channel ID            | Type            | Description                                                 |
|-----------------------|-----------------|-------------------------------------------------------------|
| total-consumption     | Number:Energy   | Total energy consumption measured by Tibber Pulse meter     |
| daily-consumption     | Number:Energy   | Energy consumed since midnight in kilowatt-hours            |
| daily-cost            | Number:Currency | Accumulated cost since midnight                             |
| last-hour-consumption | Number:Energy   | Energy consumed since last hour shift in kilowatt-hours     |
| total-production      | Number:Energy   | Total energy production measured by Tibber Pulse meter      |
| daily-production      | Number:Energy   | Net energy produced since midnight in kilowatt-hours        |
| last-hour-production  | Number:Energy   | Net energy produced since last hour shift in kilowatt-hours |

### `history` group

Historical energy consumption, cost, and production delivered as time series.
All values are read-only.

The Tibber API only returns **completed** periods — for example, an annual query run in May 2026 returns data up to end of 2025.
The binding automatically fills the gap for the current incomplete period by aggregating data bottom-up:
daily entries are summed into the current week, weekly entries into the current month, and monthly entries into the current year.
These synthetic entries are transient and will be replaced by real API data once the period closes.

| Channel ID          | Type            | Description                          | Time Series |
|---------------------|-----------------|--------------------------------------|-------------|
| yearly-consumption  | Number:Energy   | Yearly energy consumption            | yes         |
| yearly-cost         | Number:Currency | Yearly energy costs                  | yes         |
| yearly-production   | Number:Energy   | Yearly energy production             | yes         |
| monthly-consumption | Number:Energy   | Monthly energy consumption           | yes         |
| monthly-cost        | Number:Currency | Monthly energy costs                 | yes         |
| monthly-production  | Number:Energy   | Monthly energy production            | yes         |
| weekly-consumption  | Number:Energy   | Weekly energy consumption            | yes         |
| weekly-cost         | Number:Currency | Weekly energy costs                  | yes         |
| weekly-production   | Number:Energy   | Weekly energy production             | yes         |
| daily-consumption   | Number:Energy   | Daily energy consumption             | yes         |
| daily-cost          | Number:Currency | Daily energy costs                   | yes         |
| daily-production    | Number:Energy   | Daily energy production              | yes         |

#### Persistence Requirement

History channels deliver data as time series, which is not supported by the default [rrd4j](https://www.openhab.org/addons/persistence/rrd4j/) persistence.
Items linked to history channels **must** be configured with a persistence service that supports time series, such as [InfluxDB](https://www.openhab.org/addons/persistence/influxdb/) or [InMemory](https://www.openhab.org/addons/persistence/inmemory/).
Without a compatible persistence service, history data will not be stored correctly.

#### Initial Fetch on Item Link

When a new item is linked to a history channel, the binding performs an **initial fetch for one time window only** — for example, the last year for yearly channels or the last month for monthly channels.
This keeps the startup load low and avoids requesting the entire available history from the Tibber API at once.

To populate the full available history (all years, months, weeks, or days), trigger the [`fetchHistory`](#fetchhistory) Thing Action manually after linking the item.

#### Automatic Refresh

Once an item is linked, history data is refreshed automatically once per day at 01:00.
A full re-fetch for any time window can also be triggered at any time via the [`fetchHistory`](#fetchhistory) Thing Action.

## Thing Actions

Thing actions can be used to perform calculations on the currently available price information cached by the binding.
The cache contains energy prices for today and, after reaching the `updateHour`, also for tomorrow.
This helps plan when to run a device and at what cost.

Performing a calculation requires a `parameters` object containing, e.g., your boundaries for the calculation.
The parameter object allows two types: a Java `Map` or a JSON `String`.
The result is returned as a JSON-encoded `String`.
See the sections below for the result format.
If the action cannot be performed, a warning will be logged and an empty `String` will be returned.
Some real-life scenarios are shown in the [Thing Actions](#thing-actions) section.

### `priceInfoStart`

Returns the starting point as an `Instant` of the first available energy price.
Calculations must not start before this timestamp.

In case of error `Instant.MAX` is returned.

### `priceInfoEnd`

Returns the end point as an `Instant` of the last available energy price.
Calculations must not extend beyond this timestamp.

In case of error `Instant.MIN` is returned.

### `listPrices`

List prices in ascending or descending price order.
Use [persistence extensions](https://www.openhab.org/docs/configuration/persistence.html#persistence-extensions-in-scripts-and-rules) if you need time ordering.

#### Parameters

| Name          | Type    | Description         | Default        | Required |
|---------------|---------|---------------------|----------------|----------|
| earliestStart | Instant | Earliest start time | now            | no       |
| latestEnd     | Instant | Latest end time     | `priceInfoEnd` | no       |
| ascending     | boolean | Price sorting order | true           | no       |

#### Example

```java
rule "Tibber Price List"
when
    System started // use your trigger
then
    var actions = getActions("tibber","tibber:tibberapi:xyz")
    // parameters empty => default parameters are used = starting from now till end of available price information, ascending
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

JSON-encoded `String` result with keys

| Key       | Type       | Description                  |
|-----------|------------|------------------------------|
| size      | int        | Size of price list           |
| priceList | JSON array | Array of `priceInfo` entries |

JSON Object `priceInfo`

| Key      | Type   | Description               |
|----------|--------|---------------------------|
| startsAt | String | String encoded Instant    |
| duration | int    | Price duration in seconds |
| price    | double | Price in your currency    |

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

Calculates the lowest cost for a consecutive period.
For use cases like a dishwasher or laundry.

#### Parameters

| Name          | Type      | Description                                  | Default        | Required |
|---------------|-----------|----------------------------------------------|----------------|----------|
| earliestStart | Instant   | Earliest start time                          | now            | no       |
| latestStop    | Instant   | Latest end time                              | `priceInfoEnd` | no       |
| power         | int       | Power in watts                               | N/A            | no       |
| duration      | String    | Duration as String with units `h`,`m` or `s` | N/A            | true     |
| curve         | JsonArray | Array with `curveEntry` elements             | N/A            | no       |

Provide either

- `power` and `duration` for constant consumption _or_
- `curve` for sophisticated use cases like a recorded laundry power time series

JSON Object `curveEntry`

| Key       | Type   | Description            |
|-----------|--------|------------------------|
| timestamp | String | String encoded Instant |
| power     | int    | Power in watts         |
| duration  | int    | Duration in seconds    |

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
    // if the start should happen immediately, avoid negative values
    secondsTillStart = Math::max(0,secondsTillStart) 
    bestPriceTimer = createTimer(now.plusSeconds(secondsTillStart), [
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

JSON-encoded `String` result with keys

| Key                | Type   | Description                        |
|--------------------|--------|------------------------------------|
| cheapestStart      | String | Timestamp of cheapest start        |
| lowestPrice        | double | Price of the cheapest period       |
| mostExpensiveStart | String | Timestamp of most expensive start  |
| highestPrice       | double | Price of the most expensive period |
| averagePrice       | double | Average price within the period    |

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

Calculates the lowest cost for a non-consecutive schedule.
For use cases like a battery electric vehicle or a heat pump.

#### Parameters

| Name          | Type    | Description                            | Default        | Required |
|---------------|---------|----------------------------------------|----------------|----------|
| earliestStart | Instant | Earliest start time                    | now            | no       |
| latestStop    | Instant | Latest end time                        | `priceInfoEnd` | no       |
| power         | int     | Needed power                           | N/A            | no       |
| duration      | int     | Duration in seconds or String (8h 15m) | N/A            | yes      |

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

| Key      | Type       | Description                       |
|----------|------------|-----------------------------------|
| size     | int        | Number of schedules               |
| schedule | JSON array | Array of `scheduleEntry` elements |

JSON Object `scheduleEntry`

| Key      | Type   | Description                    |
|----------|--------|--------------------------------|
| start    | String | Start time (Instant as string) |
| stop     | String | Stop time (Instant as string)  |
| duration | int    | Duration in seconds            |
| cost     | double | Cost in your currency          |

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

### `fetchHistory`

Triggers a full history fetch for a specific time window and stores the result persistently.
Use this action to manually refresh history data, for example after the binding has been offline for an extended period.

#### Parameters

| Name   | Type   | Description                                         | Required |
|--------|--------|-----------------------------------------------------|----------|
| window | String | Time window to fetch: `ANNUAL`, `MONTHLY`, `WEEKLY`, `DAILY` | yes |

#### Example

```java
rule "Tibber Fetch Full History"
when
    System started // use your trigger
then
    var actions = getActions("tibber","tibber:tibberapi:xyz")
    actions.fetchHistory("ANNUAL")
    actions.fetchHistory("MONTHLY")
    actions.fetchHistory("WEEKLY")
    actions.fetchHistory("DAILY")
end
```

## Full Example

Full example with `demo.things` and `demo.items`

### `demo.things` Example

```java
Thing tibber:tibberapi:xyz [ homeid="xxx", token="xxxxxxx", updateHour=13 ]
```

### `demo.items` Example

```java
Number:EnergyPrice          Tibber_API_Spot_Prices              "Spot Prices"                {channel="tibber:tibberapi:xyz:price#spot"}
Number                      Tibber_API_Price_Level              "Price Level"                {channel="tibber:tibberapi:xyz:price#level"}
Number:EnergyPrice          Tibber_API_Average                  "Average Price"              {channel="tibber:tibberapi:xyz:price#average"}

Number:Power                Tibber_API_Live_Consumption         "Live Consumption"           {channel="tibber:tibberapi:xyz:live#consumption"}
Number:Power                Tibber_API_Minimum_Consumption      "Minimum Consumption"        {channel="tibber:tibberapi:xyz:live#minimum-consumption"}
Number:Power                Tibber_API_Peak_Consumption         "Peak Consumption"           {channel="tibber:tibberapi:xyz:live#peak-consumption"}
Number:Power                Tibber_API_Average_Consumption      "Average Consumption"        {channel="tibber:tibberapi:xyz:live#average-consumption"}
Number:Power                Tibber_API_Live_Production          "Live Production"            {channel="tibber:tibberapi:xyz:live#production"}
Number:Power                Tibber_API_Minimum_Production       "Minimum Production"         {channel="tibber:tibberapi:xyz:live#minimum-production"}
Number:Power                Tibber_API_Peak_Production          "Peak Production"            {channel="tibber:tibberapi:xyz:live#peak-production"}
Number:Power                Tibber_API_Power_Balance            "Power Balance"              {channel="tibber:tibberapi:xyz:live#power-balance"}
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

Number:Energy               Tibber_API_Yearly_Consumption       "Yearly Consumption"         {channel="tibber:tibberapi:xyz:history#yearly-consumption"}
Number:Currency             Tibber_API_Yearly_Cost              "Yearly Cost"                {channel="tibber:tibberapi:xyz:history#yearly-cost"}
Number:Energy               Tibber_API_Yearly_Production        "Yearly Production"          {channel="tibber:tibberapi:xyz:history#yearly-production"}
Number:Energy               Tibber_API_Monthly_Consumption      "Monthly Consumption"        {channel="tibber:tibberapi:xyz:history#monthly-consumption"}
Number:Currency             Tibber_API_Monthly_Cost             "Monthly Cost"               {channel="tibber:tibberapi:xyz:history#monthly-cost"}
Number:Energy               Tibber_API_Monthly_Production       "Monthly Production"         {channel="tibber:tibberapi:xyz:history#monthly-production"}
Number:Energy               Tibber_API_Weekly_Consumption       "Weekly Consumption"         {channel="tibber:tibberapi:xyz:history#weekly-consumption"}
Number:Currency             Tibber_API_Weekly_Cost              "Weekly Cost"                {channel="tibber:tibberapi:xyz:history#weekly-cost"}
Number:Energy               Tibber_API_Weekly_Production        "Weekly Production"          {channel="tibber:tibberapi:xyz:history#weekly-production"}
Number:Energy               Tibber_API_History_Daily_Consumption "History Daily Consumption" {channel="tibber:tibberapi:xyz:history#daily-consumption"}
Number:Currency             Tibber_API_History_Daily_Cost       "History Daily Cost"         {channel="tibber:tibberapi:xyz:history#daily-cost"}
Number:Energy               Tibber_API_History_Daily_Production "History Daily Production"   {channel="tibber:tibberapi:xyz:history#daily-production"}
```

### Rule listen to day-ahead price update

```java
rule "Tibber day-ahead prices available"
when
    Channel 'tibber:tibberapi:xyz:price#event' triggered
then
    logInfo("Tibber Update","Price event {}", receivedEvent)
end
```

## Local Pulse Bridge

The Things `pulse-bridge` and `pulse-meter` read the live data of a Tibber Pulse directly from the Tibber bridge on your local network.
They need no Tibber account and no Internet access.
The bridge sends a new reading about every 3 seconds, and the Binding publishes it as net power, import and export energy and, if your meter reports them, voltage, current, power per phase and grid frequency.

### Related Bindings

- Use the cloud Thing `tibberapi` of this Binding, see [Thing Configuration](#thing-configuration), if you need prices, consumption history, or the cloud-based Pulse live data.
- Use the [`smartmeter`](https://www.openhab.org/addons/bindings/smartmeter/) Binding if you read your meter directly through a serial or USB optical head, without the bridge.

### Pulse Things

| Thing Type ID  | Type   | Label               | Description                                                      |
|----------------|--------|---------------------|------------------------------------------------------------------|
| `pulse-bridge` | Bridge | Tibber Pulse Bridge | Connection to one Tibber bridge.                                 |
| `pulse-meter`  | Thing  | Tibber Pulse Meter  | One electricity meter, identified by the device id of its Pulse. |

Add the `pulse-bridge` Thing first, then the `pulse-meter` Thing below it.
Both are offered in the Inbox, see [Discovery](#discovery).

### Prerequisites

- The bridge must be reachable from openHAB in your local network.
  Give it a fixed IP address, for example with a DHCP reservation in your router.
- The password is the code on the QR label of the bridge.
- The web interface of the bridge must stay enabled permanently, see the next section.
- Only Pulse models that read the meter in the SML format are supported, which depends on the model you own.
  Pulse models for other meter types, such as impulse counters, are not supported.

### Keeping the Bridge's Web Interface Enabled

The bridge delivers its readings through its web server, so a bridge whose web interface is switched off cannot be used.
The steps below follow the guide of the Home Assistant project `marq24/ha-tibber-pulse-local`, and they can differ by hardware revision and firmware.

1. Put the bridge into access point mode by unplugging it and plugging it in again, as described in the guide.
   The LED turns green.
1. Connect a phone or laptop to the Wi-Fi network `Tibber Bridge`.
   The Wi-Fi password is the code on the label of the bridge.
1. Open the web interface of the bridge in a browser and log in with the same code.
1. Set the parameter `webserver_force_enable` to `true`, using the console or the parameters page depending on your firmware, and store the parameters.
1. Restart the bridge so that it returns to your home network.

See the [preparation guide](https://github.com/marq24/ha-tibber-pulse-local/blob/main/preparation.md) for details, screenshots and troubleshooting.

### Discovery

#### Bridge Discovery

The bridge appears in the Inbox by itself while its web interface is enabled.
The entry is named "Tibber Pulse Bridge" and contains the IP address of the bridge.
Its Thing UID contains the host label of the bridge, for example `tibber:pulse-bridge:bridge01234567`.

#### Meter Discovery

When the `pulse-bridge` Thing is online, the meters that it receives appear in the Inbox as "Tibber Pulse Meter".
The device id of the Pulse is part of the Thing UID of the entry.
Approve the entry to create the `pulse-meter` Thing.
A manual scan offers the meters again, for example after you removed a `pulse-meter` Thing.

### Pulse Thing Configuration

#### `pulse-bridge` Thing Configuration

| Name       | Type | Description                                                                  | Default | Required | Advanced |
|------------|------|------------------------------------------------------------------------------|---------|----------|----------|
| `hostname` | text | Host name or IP address of the bridge, without scheme, port, or path.        | N/A     | yes      | no       |
| `password` | text | Password of the bridge web interface, printed as a code on the bridge label. | N/A     | yes      | no       |

Do not configure two `pulse-bridge` Things for the same bridge.

#### `pulse-meter` Thing Configuration

| Name           | Type    | Description                                                                                              | Default | Required | Advanced |
|----------------|---------|----------------------------------------------------------------------------------------------------------|---------|----------|----------|
| `deviceId`     | text    | Device id of the Pulse, 16 hexadecimal digits. The Inbox entry of the meter contains it.                 | N/A     | yes      | no       |
| `staleTimeout` | integer | Seconds without a valid reading after which the meter goes offline and its live values become undefined. | 60      | no       | yes      |

The `staleTimeout` must be at least 10.

### Pulse Channels

All Channels of the `pulse-meter` Thing are read-only.
The per-phase Channels and `frequency` are advanced Channels.
A quantity that your meter does not report stays `UNDEF`.

| Channel ID      | Type                     | Unit | Description                                                                        | Advanced |
|-----------------|--------------------------|------|------------------------------------------------------------------------------------|----------|
| `power`         | Number:Power             | W    | Net active power, positive when drawn from the grid and negative when fed into it. | no       |
| `energy-import` | Number:Energy            | kWh  | Total energy drawn from the grid.                                                  | no       |
| `energy-export` | Number:Energy            | kWh  | Total energy fed into the grid.                                                    | no       |
| `power-l1`      | Number:Power             | W    | Active power on phase L1.                                                          | yes      |
| `power-l2`      | Number:Power             | W    | Active power on phase L2.                                                          | yes      |
| `power-l3`      | Number:Power             | W    | Active power on phase L3.                                                          | yes      |
| `voltage-l1`    | Number:ElectricPotential | V    | Voltage on phase L1.                                                               | yes      |
| `voltage-l2`    | Number:ElectricPotential | V    | Voltage on phase L2.                                                               | yes      |
| `voltage-l3`    | Number:ElectricPotential | V    | Voltage on phase L3.                                                               | yes      |
| `current-l1`    | Number:ElectricCurrent   | A    | Current on phase L1.                                                               | yes      |
| `current-l2`    | Number:ElectricCurrent   | A    | Current on phase L2.                                                               | yes      |
| `current-l3`    | Number:ElectricCurrent   | A    | Current on phase L3.                                                               | yes      |
| `frequency`     | Number:Frequency         | Hz   | Grid frequency.                                                                    | yes      |

The energy counters keep their last value when the connection is lost, all other Channels become `UNDEF`.
To record when the meter was last updated, link a DateTime Item to the `power` Channel with the `timestamp-update` profile, as in the example below.

### Pulse Thing Statuses

#### `pulse-bridge` Thing Statuses

| Status  | Detail              | Cause                                                                                |
|---------|---------------------|--------------------------------------------------------------------------------------|
| UNKNOWN | NONE                | Waiting for the first data from the bridge                                           |
| ONLINE  | NONE                | The bridge delivers data                                                             |
| OFFLINE | CONFIGURATION_ERROR | The host name is invalid or the password is empty or was rejected by the bridge      |
| OFFLINE | COMMUNICATION_ERROR | The bridge cannot be reached, does not deliver data, or another client took the data |

The Binding reconnects by itself after a communication error.
After a rejected password it waits until you change the configuration of the `pulse-bridge` Thing.
A `pulse-bridge` Thing that was approved without a password stays UNINITIALIZED until you enter the password.

#### `pulse-meter` Thing Statuses

| Status  | Detail              | Cause                                  |
|---------|---------------------|----------------------------------------|
| UNKNOWN | NONE                | No reading has arrived yet             |
| ONLINE  | NONE                | The meter delivers readings            |
| OFFLINE | COMMUNICATION_ERROR | No reading for `staleTimeout` seconds  |
| OFFLINE | BRIDGE_OFFLINE      | The `pulse-bridge` Thing is not online |
| OFFLINE | CONFIGURATION_ERROR | Invalid `deviceId` or `staleTimeout`   |

### Limits and Notes

- Only Pulse models that read the meter in the SML format are supported, which depends on the model you own.
  Models for other meter types, such as impulse counters, are not supported.
- The bridge serves only one client at a time.
  If you open the live page of the bridge, a browser tab or a Home Assistant integration while the Binding is connected, one of them loses the data.
- The communication with the bridge is not encrypted, so the password can be read by anyone who can listen on your local network.
- The Binding was tested with a Tibber Pulse IR on a bridge with firmware ESP 1811-97099eed / EFR 835-942ac28e.
  Other firmware versions, other meters and several Pulses on one bridge may behave differently.
- Meters that report no power leave the power Channels `UNDEF`.
- Bridges that announce themselves differently from the tested firmware are not discovered, the API token of newer firmware is not supported, and neither are readings that newer firmware decodes itself.

### Pulse Full Example

#### `pulse.things` Example

```java
Bridge tibber:pulse-bridge:home "Tibber Pulse Bridge" [ hostname="192.0.2.20", password="<QR code>" ] {
    Thing pulse-meter main "Electricity Meter" [ deviceId="0123456789abcdef", staleTimeout=60 ]
}
```

Replace the host name and the password with the values of your bridge.
The password is the code on its label.

#### `pulse.items` Example

```java
Number:Power             Grid_Power         "Grid power [%.0f %unit%]"      {channel="tibber:pulse-meter:home:main:power"}
Number:Energy            Grid_Energy_Import "Energy imported [%.3f %unit%]" {channel="tibber:pulse-meter:home:main:energy-import"}
Number:Energy            Grid_Energy_Export "Energy exported [%.3f %unit%]" {channel="tibber:pulse-meter:home:main:energy-export"}
Number:Power             Grid_Power_L1      "Power L1 [%.0f %unit%]"        {channel="tibber:pulse-meter:home:main:power-l1"}
Number:ElectricPotential Grid_Voltage_L1    "Voltage L1 [%.1f %unit%]"      {channel="tibber:pulse-meter:home:main:voltage-l1"}
Number:ElectricCurrent   Grid_Current_L1    "Current L1 [%.2f %unit%]"      {channel="tibber:pulse-meter:home:main:current-l1"}
Number:Frequency         Grid_Frequency     "Frequency [%.1f %unit%]"       {channel="tibber:pulse-meter:home:main:frequency"}
DateTime                 Grid_Last_Update   "Last update [%1$tF %1$tT]"     {channel="tibber:pulse-meter:home:main:power" [profile="system:timestamp-update"]}
```

### Acknowledgements

The Pulse part of this Binding is an independent implementation.
It is not affiliated with or supported by Tibber, and it contains no code from the projects below or from the SmlLib library that they use.
It builds on the experience that these projects documented about the Tibber bridge:

- [`marq24/ha-tibber-pulse-local`](https://github.com/marq24/ha-tibber-pulse-local) (Apache-2.0): how to read the data stream of the bridge and how to interpret its readings.
- [`jimmyw/ha-tibber-pulse-local`](https://github.com/jimmyw/ha-tibber-pulse-local) (MIT): how to reconnect after a lost connection, and how the bridge announces itself on the network.
- [`ProfDrYoMan/tibber_pulse_local`](https://github.com/ProfDrYoMan/tibber_pulse_local) (MIT): the collection of knowledge about the bridge.
