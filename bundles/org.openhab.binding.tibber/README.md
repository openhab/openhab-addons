# Tibber Binding

The Tibber Binding connects to the [Tibber API](https://developer.tibber.com), and enables users to retrieve electricity data:

* Default: Frequent polls are performed to retrieve electricity price and cost/consumption information
* Optional: For users having Tibber Pulse, a websocket connection is established to retrieve live measurements  

Refresh time (poll frequency) is set manually as part of setup, minimum 1 minute.

Tibber Pulse will automatically be detected by the Binding if present and associated with the token/HomeID used for setup.

## Supported Things

Provided one have a Tibber User Account, the Tibber API is recognized as a thing in openHAB using the Tibber Binding. 

Tibber Pulse is optional, but will enable live measurements.

The channels (i.e. measurements) associated with the Binding: 

Tibber Default:

| Channel ID         | Description                             | Read-only |
|--------------------|-----------------------------------------|-----------|
| Current Total      | Current Total Price (energy + tax)      | True      |
| Starts At          | Current Price Timestamp                 | True      |
| Current Level      | Current Price Level                     | True      |
| Daily Cost         | Daily Cost (last/previous day)          | True      |
| Daily Consumption  | Daily Consumption (last/previous day)   | True      |
| Daily From         | Timestamp (daily from)                  | True      |
| Daily To           | Timestamp (daily to)                    | True      |
| Hourly Cost        | Hourly Cost (last/previous hour)        | True      |
| Hourly Consumption | Hourly Consumption (last/previous hour) | True      |
| Hourly From        | Timestamp (hourly from)                 | True      |
| Hourly To          | Timestamp (hourly to)                   | True      |

Tibber Pulse (optional):

| Channel ID              | Description                              | Read-only |
|-------------------------|------------------------------------------|-----------|
| Timestamp               | Timestamp for live measurements          | True      |
| Power                   | Live Power Consumption                   | True      |
| Last Meter Consumption  | Last Recorded Meter Consumption          | True      |
| Accumulated Consumption | Accumulated Consumption since Midnight   | True      |
| Accumulated Cost        | Accumulated Cost since Midnight          | True      |
| Currency                | Currency of Cost                         | True      |
| Min Power               | Min Power Consumption since Midnight     | True      |
| Average Power           | Average Power Consumption since Midnight | True      |
| Max Power               | Max Power Consumption since Midnight     | True      |
| Voltage 1-3             | Voltage per Phase                        | True      |
| Current 1-3             | Current per Phase                        | True      |
| Power Production        | Live Power Production                    | True      |
| Accumulated Production  | Accumulated Production since Midnight    | True      |
| Min Power Production    | Min Power Production since Midnight      | True      |
| Max Power Production    | Max Power Production since Midnight      | True      |


## Binding Configuration

To access and initiate the Tibber Binding, a Tibber user account is required.

The following input is required for initialization:

* Tibber token
* Tibber HomeId
* Refresh Interval (min 1 minute)

Note: Tibber token is retrieved from your Tibber account:
[Tibber Account](https://developer.tibber.com/settings/accesstoken)

Note: Tibber HomeId is retrieved from [www.developer.com](https://developer.tibber.com/explorer): 

* Sign in (Tibber user account) and "load" personal token.
* Copy query from below and paste into the Tibber API Explorer, and run query. 
* If Tibber Pulse is connected, the Tibber API Explorer will report "true" for "realTimeConsumptionEnabled"
* Copy HomeId from Tibber API Explorer, without quotation marks, and use this in the bindings configuration.

```
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


## Full Example

### demo.things

```
Thing tibber:tibberapi:7cfae492 [ homeid="xxx", token="xxxxxxx" ]
```

### demo.items:
.
```
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
DateTime                   TibberAPILiveTimestamp                "Timestamp - Live Measurement"              {channel="tibber:tibberapi:7cfae492:live_timestamp"}
Number:Power               TibberAPILivePower                    "Live Power Consumption [%.0f W]"           {channel="tibber:tibberapi:7cfae492:live_power"}
Number:Energy              TibberAPILiveLastMeterConsumption     "Last Meter Consumption [%.2f kWh]"         {channel="tibber:tibberapi:7cfae492:live_lastMeterConsumption"}
Number:Energy              TibberAPILiveAccumulatedConsumption   "Accumulated Consumption [%.2f kWh]"        {channel="tibber:tibberapi:7cfae492:live_accumulatedConsumption"}
Number:Dimensionless       TibberAPILiveAccumulatedCost          "Accumulated Cost [%.2f NOK]"               {channel="tibber:tibberapi:7cfae492:live_accumulatedCost"}
String                     TibberAPILiveCurrency                 "Currency"                                  {channel="tibber:tibberapi:7cfae492:live_currency"}
Number:Power               TibberAPILiveMinPower                 "Min Power Consumption [%.0f W]"            {channel="tibber:tibberapi:7cfae492:live_minPower"}
Number:Power               TibberAPILiveAveragePower             "Average Power Consumption [%.0f W]"             {channel="tibber:tibberapi:7cfae492:live_averagePower"}
Number:Power               TibberAPILiveMaxPower                 "Max Power Consumption [%.0f W]"            {channel="tibber:tibberapi:7cfae492:live_maxPower"}
Number:ElectricPotential   TibberAPILiveVoltage1                 "Live Voltage Phase 1 [%.0 V]"              {channel="tibber:tibberapi:7cfae492:live_voltage1"}
Number:ElectricPotential   TibberAPILiveVoltage2                 "Live Voltage Phase 2 [%.0 V]"              {channel="tibber:tibberapi:7cfae492:live_voltage2"}
Number:ElectricPotential   TibberAPILiveVoltage3                 "Live Voltage Phase 3 [%.0 V]"              {channel="tibber:tibberapi:7cfae492:live_voltage3"}
Number:ElectricCurrent     TibberAPILiveCurrent1                 "Live Current Phase 1 [%.1 A]"              {channel="tibber:tibberapi:7cfae492:live_current1"}
Number:ElectricCurrent     TibberAPILiveCurrent2                 "Live Current Phase 2 [%.1 A]"              {channel="tibber:tibberapi:7cfae492:live_current2"}
Number:ElectricCurrent     TibberAPILiveCurrent3                 "Live Current Phase 3 [%.1 A]"              {channel="tibber:tibberapi:7cfae492:live_current3"}
Number:Power               TibberAPILivePowerProduction          "Live Power Production [%.0f W]"                 {channel="tibber:tibberapi:7cfae492:live_powerProduction"}
Number:Power               TibberAPILiveMinPowerproduction       "Min Power Production [%.0f W]"                  {channel="tibber:tibberapi:7cfae492:live_minPowerproduction"}
Number:Power               TibberAPILiveMaxPowerproduction       "Max Power Production [%.0f W]"                  {channel="tibber:tibberapi:7cfae492:live_maxPowerproduction"}
Number:Energy              TibberAPILiveAccumulatedProduction    "Accumulated Production [%.2f kWh]"         {channel="tibber:tibberapi:7cfae492:live_accumulatedProduction"}
```
