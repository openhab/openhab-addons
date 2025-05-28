# Tibber Binding

The Tibber Binding connects to the [Tibber API](https://developer.tibber.com), and enables users to retrieve electricity data:

- Default: Frequent polls are performed to retrieve electricity price and cost/consumption information
- Optional: For users having Tibber Pulse, a websocket connection is established to retrieve live measurements

Refresh time (poll frequency) is set manually as part of setup, minimum 1 minute.

Tibber Pulse will automatically be detected by the Binding if present and associated with the token/HomeID used for setup.

## Supported Things

Provided one have a Tibber User Account, the Tibber API is recognized as a thing in openHAB using the Tibber Binding.

Tibber Pulse is optional, but will enable live measurements.

The channels (i.e. measurements) associated with the Binding:

### price group

Forecast values og Tibber pricing.
All read-only.

| Channel ID        | Type                 | Description         | Forecast |
|-------------------|----------------------|---------------------|----------|
| spot-prices       | Number:EnergyPrice   | Spot Prices         | yes      |
| level             | Number               | Price Level         | yes      |
| average           | Number:EnergyPrice   | Average 24h         | yes      |

The `level` number is mapping the [Tibber Rating](https://developer.tibber.com/docs/reference#pricelevel) into numbers.
Zero reflects _normal_ price while values above 0 are _expensive_ and values below 0 are _cheap_.

Mapping:

- Very Cheap : -2
- Cheap : -1
- Normal : 0
- Expensive : 1
- Very Expensive : 2


The `average` values are not delivered by Tibber API.
It's calculated by the binding to provide a trend line for the last 24 hours.
After initial setup the average values will stay NULL until the next day because the previous 24 h prices cannot be obtained by the Tibber API.

### live group

Live information from Tibber Pulse.
All values read-only.

| Channel ID            | Type                      | Description                                           |
|-----------------------|---------------------------|-------------------------------------------------------|
| consumption           | Number:Power              | Consumption at the moment in watts                    |
| minimum-consumption   | Number:Power              | Minimum power consumption since midnight in watts     |
| peak-consumtion       | Number:Power              | Peak power consumption since midnight in watts        |
| production            | Number:Power              | Net power production at the moment in watts           |
| minimum-production    | Number:Power              | Minimum net power production since midnight in watts  |
| peak-production       | Number:Power              | Maximum net power production since midnight in watts  |
| voltage1              | Number:ElectricPotential  | Electric potential on phase 1                         |
| voltage2              | Number:ElectricPotential  | Electric potential on phase 2                         |
| voltage3              | Number:ElectricPotential  | Electric potential on phase 3                         |
| current1              | Number:ElectricCurrent    | Electric current on phase 1                           |
| current2              | Number:ElectricCurrent    | Electric current on phase 2                           |
| current3              | Number:ElectricCurrent    | Electric current on phase 3                           |


### statistics group

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

## Thing Configuration

| Name          | Type      | Description                           | Default   | Required  |
|---------------|-----------|---------------------------------------|-----------|-----------|
| token         | text      | Tibber Personal Token                 | N/A       | yes       |
| homeid        | text      | Tibber Home ID                        | N/A       | yes       |
| updateHour    | integer   | Hour when spot prices are updated     | 13        | yes       |

To access and initiate the Tibber Binding, a Tibber user account is required.

The following input is required for initialization:

- Tibber token
- Tibber HomeId
- Refresh Interval (min 1 minute)

Note: Tibber token is retrieved from your Tibber account:
[Tibber Account](https://developer.tibber.com/settings/accesstoken)

Note: Tibber HomeId is retrieved from [developer.tibber.com](https://developer.tibber.com/explorer):

- Sign in (Tibber user account) and "load" personal token.
- Copy query from below and paste into the Tibber API Explorer, and run query.
- If Tibber Pulse is connected, the Tibber API Explorer will report "true" for "realTimeConsumptionEnabled"
- Copy HomeId from Tibber API Explorer, without quotation marks, and use this in the bindings configuration.

```json
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

## Thing Actions

Thing actions can be used to perform calculations on the current available price information cached by the binding. 
Cache contains energy prices from today and after reaching the `updateHour` also for tomorrow.
This is for planning when and for what cost a specific electric consumer can be started.  

Performing a calcuation a `paramters` object is needed containing e.g. your boundaries for the calculation.
Parameter object allow 2 types: Java `Map` or JSON `String`.
Depending on which parameter object is given the returned result object is eiter a Java `Map` or JSON `String`.


### `priceInfoStart`

Returns starting point as `Instant` of first available energy price. 
It's not allowed to start calculations before this timestamp.

In case of error `Instant.MAX` is returned.

### `priceInfoEnd`

Returns end point as `Instant` of the last available energy price. 
It's not allowed to exceed calculations after this timestamp.

In case of error `Instant.MIN` is returned.

### listPrices

List prices in ascending / decending order.

Parameters:

| Name          | Type      | Description                           | Default           | Required  |
|---------------|-----------|---------------------------------------|-------------------|-----------|
| earliestStart | Instant   | Earliest start time                   | now               | no        |
| latestStop    | Instant   | Latest end time                       | `priceInfoEnd`    | no        |
| ascending     | boolean   | Hour when spot prices are updated     | true              | no        |

Result:

Java `Map` or JSON `String` with following keys
 
| Key           | Type      | Description                           | 
|---------------|-----------|---------------------------------------|
| earliestStart | Instant   | Earliest start time                   |

### bestPricePeriod

Calculates best cost for a consecutive period.
For use cases like dishwasher or laundry.

Parameters:

| Name          | Type      | Description                           | Default           | Required  |
|---------------|-----------|---------------------------------------|-------------------|-----------|
| earliestStart | Instant   | Earliest start time                   | now               | no        |
| latestStop    | Instant   | Latest end time                       | `priceInfoEnd`    | no        |
| curve         | List<Map> | List of curve etnries                 | N/A               | true      |

### bestPriceSchedule

Calculates best cost for a non-consecutive schedule.
For use cases like battery electric vehicle or heat-pump.

Parameters:

| Name          | Type      | Description                           | Default           | Required  |
|---------------|-----------|---------------------------------------|-------------------|-----------|
| earliestStart | Instant   | Earliest start time                   | now               | no        |
| latestStop    | Instant   | Latest end time                       | `priceInfoEnd`    | no        |
| power         | int       | Needed power                          | N/A               | no        |
| duration      | int       | Hour when spot prices are updated     | N/A               | yes        |

## Full Example

### `demo.things` Example

```java
Thing tibber:tibberapi:7cfae492 [ homeid="xxx", token="xxxxxxx", updateHour=13 ]
```

### `demo.items` Example

```java
Number:EnergyPrice       TibberAPICurrentTotal                 "Current Total Price [%.2f NOK]"            {channel="tibber:tibberapi:7cfae492:current_total"}
Number       TibberAPIDailyCost                    "Total Daily Cost [%.2f NOK]"               {channel="tibber:tibberapi:7cfae492:daily_cost"}
Number:EnergyPrice              TibberAPIDailyConsumption             "Total Daily Consumption [%.2f kWh]"        {channel="tibber:tibberapi:7cfae492:daily_consumption"}
Number:Power              TibberAPIHourlyConsumption            "Total Hourly Consumption [%.2f kWh]"       {channel="tibber:tibberapi:7cfae492:hourly_consumption"}
Number:Power              TibberAPIHourlyConsumption            "Total Hourly Consumption [%.2f kWh]"       {channel="tibber:tibberapi:7cfae492:hourly_consumption"}
Number:Power               TibberAPILivePower                    "Live Power Consumption [%.0f W]"           {channel="tibber:tibberapi:7cfae492:live_power"}
Number:Power               TibberAPILiveMinPower                 "Min Power Consumption [%.0f W]"            {channel="tibber:tibberapi:7cfae492:live_minPower"}
Number:Power               TibberAPILiveAveragePower             "Average Power Consumption [%.0f W]"        {channel="tibber:tibberapi:7cfae492:live_averagePower"}
Number:Power               TibberAPILiveMaxPower                 "Max Power Consumption [%.0f W]"            {channel="tibber:tibberapi:7cfae492:live_maxPower"}
Number:ElectricPotential   TibberAPILiveVoltage1                 "Live Voltage Phase 1 [%.0 V]"              {channel="tibber:tibberapi:7cfae492:live_voltage1"}
Number:ElectricPotential   TibberAPILiveVoltage2                 "Live Voltage Phase 2 [%.0 V]"              {channel="tibber:tibberapi:7cfae492:live_voltage2"}
Number:ElectricPotential   TibberAPILiveVoltage3                 "Live Voltage Phase 3 [%.0 V]"              {channel="tibber:tibberapi:7cfae492:live_voltage3"}
Number:ElectricCurrent     TibberAPILiveCurrent1                 "Live Current Phase 1 [%.1 A]"              {channel="tibber:tibberapi:7cfae492:live_current1"}
Number:ElectricCurrent     TibberAPILiveCurrent2                 "Live Current Phase 2 [%.1 A]"              {channel="tibber:tibberapi:7cfae492:live_current2"}
Number:ElectricCurrent     TibberAPILiveCurrent3                 "Live Current Phase 3 [%.1 A]"              {channel="tibber:tibberapi:7cfae492:live_current3"}
Number:Energy              TibberAPILiveAccumulatedProduction    "Accumulated Production [%.2f kWh]"         {channel="tibber:tibberapi:7cfae492:live_accumulatedProduction"}
Number:Energy              TibberAPILiveAccumulatedProductionThisHour   "Net kWh produced since last hour shift [%.2f kWh]"        {channel="tibber:tibberapi:7cfae492:live_accumulatedProductionThisHour"}
Number:Currency              TibberAPILiveLastMeterProduction      "Min Power Production [%.0f W]"             {channel="tibber:tibberapi:7cfae492:live_lastMeterProduction"}
Number:Energy              TibberAPILiveLastMeterProduction      "Min Power Production [%.0f W]"             {channel="tibber:tibberapi:7cfae492:live_lastMeterProduction"}
Number:Energy              TibberAPILiveLastMeterConsumption     "Last Meter Consumption [%.2f kWh]"         {channel="tibber:tibberapi:7cfae492:live_lastMeterConsumption"}
Number:Energy              TibberAPILiveAccumulatedConsumption   "Accumulated Consumption [%.2f kWh]"        {channel="tibber:tibberapi:7cfae492:live_accumulatedConsumption"}
Number:Energy              TibberAPILiveAccumulatedConsumptionThisHour   "kWh consumed since since last hour shift [%.2f kWh]"        {channel="tibber:tibberapi:7cfae492:live_accumulatedConsumptionLastHour"}
```
