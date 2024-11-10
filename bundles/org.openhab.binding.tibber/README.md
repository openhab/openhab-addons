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

Tibber Default:

| Channel ID           | Description                                             | Read-only | Forecast |
|----------------------|---------------------------------------------------------|-----------|----------|
| current_total        | Current Total Price (energy + tax)                      | True      | yes      |
| current_startsAt     | Current Price Timestamp                                 | True      | no       |
| current_level        | Current Price Level                                     | True      | no       |
| daily_cost           | Daily Cost (last/previous day)                          | True      | no       |
| daily_consumption    | Daily Consumption (last/previous day)                   | True      | no       |
| daily_from           | Timestamp (daily from)                                  | True      | no       |
| daily_to             | Timestamp (daily to)                                    | True      | no       |
| hourly_cost          | Hourly Cost (last/previous hour)                        | True      | no       |
| hourly_consumption   | Hourly Consumption (last/previous hour)                 | True      | no       |
| hourly_from          | Timestamp (hourly from)                                 | True      | no       |
| hourly_to            | Timestamp (hourly to)                                   | True      | no       |
| tomorrow_prices      | JSON array of tomorrow's prices. See below for example. | True      | no       |
| today_prices         | JSON array of today's prices. See below for example.    | True      | no       |

Tibber Pulse (optional):

| Channel ID                          | Description                                   | Read-only |
|-------------------------------------|-----------------------------------------------|-----------|
| live_timestamp                      | Timestamp for live measurements               | True      |
| live_power                          | Live Power Consumption                        | True      |
| live_lastMeterConsumption           | Last Recorded Meter Consumption               | True      |
| live_accumulatedConsumption         | Accumulated Consumption since Midnight        | True      |
| live_accumulatedConsumptionThisHour | Accumulated Consumption since last hour shift | True      |
| live_accumulatedCost                | Accumulated Cost since Midnight               | True      |
| live_accumulatedReward              | Accumulated Reward since Midnight             | True      |
| live_currency                       | Currency of Cost                              | True      |
| live_minPower                       | Min Power Consumption since Midnight          | True      |
| live_averagePower                   | Average Power Consumption since Midnight      | True      |
| live_maxPower                       | Max Power Consumption since Midnight          | True      |
| live_voltage1                       | Voltage Phase 1                               | True      |
| live_voltage2                       | Voltage Phase  2                              | True      |
| live_voltage3                       | Voltage Phase 3                               | True      |
| live_current1                       | Current Phase 1                               | True      |
| live_current2                       | Current Phase 2                               | True      |
| live_current3                       | Current Phase 3                               | True      |
| live_powerProduction                | Live Power Production                         | True      |
| live_accumulatedProduction          | Accumulated Production since Midnight         | True      |
| live_accumulatedProductionThisHour  | Accumulated Production since last hour shift  | True      |
| live_lastMeterProduction            | Last Recorded Meter Production                | True      |
| live_minPowerproduction             | Min Power Production since Midnight           | True      |
| live_maxPowerproduction             | Max Power Production since Midnight           | True      |

## Binding Configuration

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

## Thing Configuration

When Tibber Binding is installed, Tibber API should be auto discovered.

Retrieve personal token and HomeId from description above, and initialize/start a scan with the binding.

Tibber API will be auto discovered if provided input is correct.

## Tomorrow and Today Prices

The today and tomorrow prices are served as forecast on the `current_total` channel and as JSON data on the channels `today_prices` and `tomorrow_prices`.
Example of tomorrow and today prices data structure - an array of tuples:

```json
[
  {
    "startsAt": "2022-09-27T00:00:00.000+02:00",
    "total": 3.8472
  },
  {
    "startsAt": "2022-09-27T01:00:00.000+02:00",
    "total": 3.0748
  },
  {
    "startsAt": "2022-09-27T02:00:00.000+02:00",
    "total": 2.2725
  },
  {
    "startsAt": "2022-09-27T03:00:00.000+02:00",
    "total": 2.026
  },
  {
    "startsAt": "2022-09-27T04:00:00.000+02:00",
    "total": 2.6891
  },
  {
    "startsAt": "2022-09-27T05:00:00.000+02:00",
    "total": 3.7821
  },
  {
    "startsAt": "2022-09-27T06:00:00.000+02:00",
    "total": 3.9424
  },
  {
    "startsAt": "2022-09-27T07:00:00.000+02:00",
    "total": 4.158
  },
  {
    "startsAt": "2022-09-27T08:00:00.000+02:00",
    "total": 4.2648
  },
  {
    "startsAt": "2022-09-27T09:00:00.000+02:00",
    "total": 4.2443
  },
  {
    "startsAt": "2022-09-27T10:00:00.000+02:00",
    "total": 4.2428
  },
  {
    "startsAt": "2022-09-27T11:00:00.000+02:00",
    "total": 4.2061
  },
  {
    "startsAt": "2022-09-27T12:00:00.000+02:00",
    "total": 4.1458
  },
  {
    "startsAt": "2022-09-27T13:00:00.000+02:00",
    "total": 3.9396
  },
  {
    "startsAt": "2022-09-27T14:00:00.000+02:00",
    "total": 3.8563
  },
  {
    "startsAt": "2022-09-27T15:00:00.000+02:00",
    "total": 4.0364
  },
  {
    "startsAt": "2022-09-27T16:00:00.000+02:00",
    "total": 4.093
  },
  {
    "startsAt": "2022-09-27T17:00:00.000+02:00",
    "total": 4.1823
  },
  {
    "startsAt": "2022-09-27T18:00:00.000+02:00",
    "total": 4.2779
  },
  {
    "startsAt": "2022-09-27T19:00:00.000+02:00",
    "total": 4.3154
  },
  {
    "startsAt": "2022-09-27T20:00:00.000+02:00",
    "total": 4.3469
  },
  {
    "startsAt": "2022-09-27T21:00:00.000+02:00",
    "total": 4.2329
  },
  {
    "startsAt": "2022-09-27T22:00:00.000+02:00",
    "total": 4.1014
  },
  {
    "startsAt": "2022-09-27T23:00:00.000+02:00",
    "total": 4.0265
  }
]
```

## Full Example

### `demo.things` Example

```java
Thing tibber:tibberapi:7cfae492 [ homeid="xxx", token="xxxxxxx" ]
```

### `demo.items` Example

```java
Number:Dimensionless       TibberAPICurrentTotal                 "Current Total Price [%.2f NOK]"            {channel="tibber:tibberapi:7cfae492:current_total"}
DateTime                   TibberAPICurrentStartsAt              "Timestamp - Current Price"                 {channel="tibber:tibberapi:7cfae492:current_startsAt"}
String                     TibberAPICurrentLevel                 "Price Level"                               {channel="tibber:tibberapi:7cfae492:current_level"}
DateTime                   TibberAPIDailyFrom                    "Timestamp - Daily From"                    {channel="tibber:tibberapi:7cfae492:daily_from"}
DateTime                   TibberAPIDailyTo                      "Timestamp - Daily To"                      {channel="tibber:tibberapi:7cfae492:daily_to"}
Number:Dimensionless       TibberAPIDailyCost                    "Total Daily Cost [%.2f NOK]"               {channel="tibber:tibberapi:7cfae492:daily_cost"}
Number:Energy              TibberAPIDailyConsumption             "Total Daily Consumption [%.2f kWh]"        {channel="tibber:tibberapi:7cfae492:daily_consumption"}
DateTime                   TibberAPIHourlyFrom                   "Timestamp - Hourly From"                   {channel="tibber:tibberapi:7cfae492:hourly_from"}
DateTime                   TibberAPIHourlyTo                     "Timestamp - Hourly To"                     {channel="tibber:tibberapi:7cfae492:hourly_to"}
Number:Dimensionless       TibberAPIHourlyCost                   "Total Hourly Cost [%.2f NOK]"              {channel="tibber:tibberapi:7cfae492:hourly_cost"}
Number:Energy              TibberAPIHourlyConsumption            "Total Hourly Consumption [%.2f kWh]"       {channel="tibber:tibberapi:7cfae492:hourly_consumption"}
String                     TibberAPITomorrowPrices               "Price per hour tomorrow JSON array"        {channel="tibber:tibberapi:7cfae492:tomorrow_prices"}
String                     TibberAPITodayPrices                  "Price per hour today JSON array"           {channel="tibber:tibberapi:7cfae492:today_prices"}
DateTime                   TibberAPILiveTimestamp                "Timestamp - Live Measurement"              {channel="tibber:tibberapi:7cfae492:live_timestamp"}
Number:Power               TibberAPILivePower                    "Live Power Consumption [%.0f W]"           {channel="tibber:tibberapi:7cfae492:live_power"}
Number:Energy              TibberAPILiveLastMeterConsumption     "Last Meter Consumption [%.2f kWh]"         {channel="tibber:tibberapi:7cfae492:live_lastMeterConsumption"}
Number:Energy              TibberAPILiveAccumulatedConsumption   "Accumulated Consumption [%.2f kWh]"        {channel="tibber:tibberapi:7cfae492:live_accumulatedConsumption"}
Number:Energy              TibberAPILiveAccumulatedConsumptionThisHour   "kWh consumed since since last hour shift [%.2f kWh]"        {channel="tibber:tibberapi:7cfae492:live_accumulatedConsumptionLastHour"}
Number:Dimensionless       TibberAPILiveAccumulatedCost          "Accumulated Cost [%.2f NOK]"               {channel="tibber:tibberapi:7cfae492:live_accumulatedCost"}
Number:Dimensionless       TibberAPILiveAccumulatedReward        "Accumulated Reward [%.2f NOK]"             {channel="tibber:tibberapi:7cfae492:live_accumulatedReward"}
String                     TibberAPILiveCurrency                 "Currency"                                  {channel="tibber:tibberapi:7cfae492:live_currency"}
Number:Power               TibberAPILiveMinPower                 "Min Power Consumption [%.0f W]"            {channel="tibber:tibberapi:7cfae492:live_minPower"}
Number:Power               TibberAPILiveAveragePower             "Average Power Consumption [%.0f W]"        {channel="tibber:tibberapi:7cfae492:live_averagePower"}
Number:Power               TibberAPILiveMaxPower                 "Max Power Consumption [%.0f W]"            {channel="tibber:tibberapi:7cfae492:live_maxPower"}
Number:ElectricPotential   TibberAPILiveVoltage1                 "Live Voltage Phase 1 [%.0 V]"              {channel="tibber:tibberapi:7cfae492:live_voltage1"}
Number:ElectricPotential   TibberAPILiveVoltage2                 "Live Voltage Phase 2 [%.0 V]"              {channel="tibber:tibberapi:7cfae492:live_voltage2"}
Number:ElectricPotential   TibberAPILiveVoltage3                 "Live Voltage Phase 3 [%.0 V]"              {channel="tibber:tibberapi:7cfae492:live_voltage3"}
Number:ElectricCurrent     TibberAPILiveCurrent1                 "Live Current Phase 1 [%.1 A]"              {channel="tibber:tibberapi:7cfae492:live_current1"}
Number:ElectricCurrent     TibberAPILiveCurrent2                 "Live Current Phase 2 [%.1 A]"              {channel="tibber:tibberapi:7cfae492:live_current2"}
Number:ElectricCurrent     TibberAPILiveCurrent3                 "Live Current Phase 3 [%.1 A]"              {channel="tibber:tibberapi:7cfae492:live_current3"}
Number:Power               TibberAPILivePowerProduction          "Live Power Production [%.0f W]"            {channel="tibber:tibberapi:7cfae492:live_powerProduction"}
Number:Energy              TibberAPILiveAccumulatedProduction    "Accumulated Production [%.2f kWh]"         {channel="tibber:tibberapi:7cfae492:live_accumulatedProduction"}
Number:Energy              TibberAPILiveAccumulatedProductionThisHour   "Net kWh produced since last hour shift [%.2f kWh]"        {channel="tibber:tibberapi:7cfae492:live_accumulatedProductionThisHour"}
Number:Energy              TibberAPILiveLastMeterProduction      "Min Power Production [%.0f W]"             {channel="tibber:tibberapi:7cfae492:live_lastMeterProduction"}
Number:Power               TibberAPILiveMinPowerproduction       "Min Power Production [%.0f W]"             {channel="tibber:tibberapi:7cfae492:live_minPowerproduction"}
Number:Power               TibberAPILiveMaxPowerproduction       "Max Power Production [%.0f W]"             {channel="tibber:tibberapi:7cfae492:live_maxPowerproduction"}
```
