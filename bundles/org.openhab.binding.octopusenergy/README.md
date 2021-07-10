# Octopus Energy Binding

This binding allows openHAB to communicate with the public API from Octopus Energy (https://octopus.energy), an energy provider in the UK.
The following features are provided by the binding:

- Retrieval of latest meter readings (consumption)
- Retrieval of tariff schedule / unit rates (if on an agile tariff, half-hourly future rates are provided based on availability)
- Calculation of forward looking least cost window based on given energy consumption. I.e. when is the cheapest time to run my dishwasher? This is available as a single pair of channels (cheapestSlotDuration, cheapestSlotStart) and also as an action for the use in scripts/rules.

So far, this binding has only been tested with Octopus "Agile" electricity tariff. I would welcome users of "Go" or "Go Faster" for testing.

## Supported Things

This binding supports the following thing types

| Thing                 | Thing Type | Discovery | Description                                    |
|-----------------------|------------|-----------|------------------------------------------------|
| bridge                | Bridge     | Manual    |  A single connection to the Octopus Energy API |
| electricityMeterPoint | Thing      | Automatic |  An electricity meter point                    |
| gasMeterPoint         | Thing      | Automatic |  An gas meter point                            |


## Discovery

Once the bridge is configured with Octopus Energy account number and API key, the various meter points will be discovered automatically and added to the Inbox.
 
## Thing Configuration

#### Manual configuration

For the identifier of the meter points, the corresponding MPAN or MPRN is used.

```
Bridge octopusenergy:bridge:api "Demo Octopus Energy Bridge" [ accountNumber="<Account Number>", apiKey="<API Key>", refreshInterval=15 ]
{
  Thing electricityMeterPoint 1679122486235 "My Electricy Meter Point with MPAN 1679122486235"
  Thing gasMeterPoint            3046829404 "My Gas Meter Point with MPRN 3046829404"
}
```

## Channels

The following channels are defined. Except for the bridge refresh channel, all are read-only.

####  Bridge

| channel                        | type          | description                                                                                  |
|--------------------------------|---------------|----------------------------------------------------------------------------------------------|
| refresh                        | Switch        | Allows a manual refresh of the API data when sent an ON command                              |
| lastRefreshTime                | DateTime      | The time consumption and tariff information was last refreshed                               |

####  Electricity Meter Point

| channel                        | type          | description                                                                                  |
|--------------------------------|---------------|----------------------------------------------------------------------------------------------|
| mpan                           | String        | Meter Point Administration Number                                                            |
| currentTariff                  | String        | The currently applicable tariff                                                              |
| mostRecentConsumptionAmount    | Number:Energy | The amount of energy consumed during the given time window                                   |
| mostRecentConsumptionStartTime | DateTime      | The start time of the most recent consumption window                                         |
| mostRecentConsumptionEndTime   | DateTime      | The end time of the most recent consumption window                                           |
| unitPriceWindowStartTime       | Number        | The start time of the unit price window                                                      |
| unitPriceWindowEndTime         | DateTime      | The end time of the unit price window                                                        |
| unitPriceWindowMinAmount       | Number        | The minimum unit price of energy in GBp per Kilowatt Hour over the available forecast period |
| unitPriceWindowMaxAmount       | DateTime      | The maximum unit price of energy in GBp per Kilowatt Hour over the available forecast period |

####  Gas Meter Point

| channel                        | type          | description                                                                                  |
|--------------------------------|---------------|----------------------------------------------------------------------------------------------|
| mprn                           | String        | Meter Point Reference Number                                                                 |
| currentTariff                  | String        | The currently applicable tariff                                                              |

## Actions

The key feature of this binding is a number of actions which can be invoked against an electricity meter point to calculate the optimal time and cost for running a certain appliance (e.g. Dishwasher). This can be used in a variety of ways, such as:

- Display the recommended Dishwasher or Washing machine start time on a wall panel
- Schedule a nightly EV charging session if your EVSE or car allows automated scheduling
- Switching of storage heaters overnight
- Schedule wake up time for a server to run nightly backups

####  Electricity Meter Point

| action                         | parameters                                                        | description                                                                                                |
|--------------------------------|-------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| optimise                       | Duration duration                                                 | Calculates the optimal time and cost for an activity with a given duration.                                |
| optimiseWithRecurringEndTime   | Duration duration, int latestEndHour, int latestEndMinute         | Calculates the optimal time for an activity with a given duration and a latest end time (hour/minute)      |
| optimiseWithRecurringStartTime | Duration duration, int earliestStartHour, int earliestStartMinute | Calculates the optimal time for an activity with a given duration and an earliest start time (hour/minute) |
| optimiseWithAbsoluteStartTime  | Duration duration, ZonedDateTime earliestStartTime                | Calculates the optimal time for an activity with a given duration and an earliest absolute start time      |


## Examples

octopusenergy.things

```
Bridge octopusenergy:bridge:api "Demo Octopus Energy Bridge" [ accountNumber="A_123456", apiKey="sk_live_MGlrHJ67GKd93Sle9IO", refreshInterval=15 ]
{
  Thing electricityMeterPoint 1679122486235 "My Electricy Meter Point with MPAN 1679122486235"
  Thing gasMeterPoint            7048462901 "My Gas Meter Point with MPRN 7048462901"
}
```

octopusenergy.items

```
// Bridge Items
Switch   Refresh          "Refresh the data"                { channel="octopusenergy:bridge:api:refresh" }
DateTime LastRefreshTime  "Last Refresh Time [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]"      <time>  { channel="octopusenergy:bridge:api:lastRefreshTime" }

// Electricity Meter Point Items
String   MPAN             "MPAN [%s]"                                                            { channel="octopusenergy:electricityMeterPoint:api:1679122486235:mpan" }  
String   CurrentTariff    "Current Tariff [%s]"                                                  { channel="octopusenergy:electricityMeterPoint:api:1679122486235:currentTariff" }  
DateTime PriceWindowStart "Prices available from [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]"  <time>  { channel="octopusenergy:electricityMeterPoint:api:1679122486235:unitPriceWindowStartTime" }
DateTime PriceWindowEnd   "Prices available until [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" <time>  { channel="octopusenergy:electricityMeterPoint:api:1679122486235:unitPriceWindowEndTime" }
Number   PriceMin         "Lowest Unit Price [%.2f p/kWh]"                                       { channel="octopusenergy:electricityMeterPoint:api:1679122486235:unitPriceWindowMinAmount" }
Number   PriceMax         "Highest Unit Price [%.2f p/kWh]"                                      { channel="octopusenergy:electricityMeterPoint:api:1679122486235:unitPriceWindowMaxAmount" }

// Wall Panel Items (e.g. HABPanel)
// - connect DishwasherDuration to a slider (0-300, step 5)
// - connect DishwasherEndHour to a slider (0-23, step 1)
// - connect DishwasherEndMinute to a slider (0-55, step 5)
//
Number:Time DishwasherDuration         "Dishwasher Program Duration [%d mins]"
Number      DishwasherEndHour          "Dishwasher must finish before [%d hour]"
Number      DishwasherEndMinute        "Dishwasher must finish before [%d min]"
DateTime    DishwasherOptimalStartTime "Dishwasher optimal start time [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" <time> 
Number      DishwasherAvgUnitPrice     "Dishwasher average unit price [%.2f p/kWh]" <time> 

```

octopusenergy.rules

```
import java.math.BigDecimal
import java.time.Duration
import java.time.ZonedDateTime
import java.util.Map

rule "calculateDishwasherEndTime"
when
  Item LastRefreshTime changed
  or Item DishwasherDuration changed
  or Item DishwasherEndHour changed
  or Item DishwasherEndMinute changed
then
  val octopusAction = getActions("octopusenergy","octopusenergy:electricityMeterPoint:api:1679122486235")

  var Duration duration = Duration.ofMinutes((DishwasherDuration.state as Number).longValue())
  var int endHour = (DishwasherEndHour.state as Number).intValue()
  var int endMinute = (DishwasherEndMinute.state as Number).intValue()

  var Map<String,Object> result = octopusAction.optimiseWithRecurringEndTime(duration,endHour,endMinute);
  
  var ZonedDateTime optimisedStartTime = (result.get("optimisedStartTime") as ZonedDateTime)
  var BigDecimal optimisedAverageUnitCost = (result.get("optimisedAverageUnitCost") as BigDecimal)

  postUpdate(DishwasherOptimalStartTime, new DateTimeType(optimisedStartTime))
  postUpdate(DishwasherAvgUnitPrice, new DecimalType(optimisedAverageUnitCost))
end
```




