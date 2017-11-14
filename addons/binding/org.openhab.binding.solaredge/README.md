# SolarEdge Binding

The SolarEdge binding is used to get live data from from SolarEdge inverters via the central web api. This binding should in general be compatible with all inverter models that upload data to the solaredge portal.
Only read access is supported.

## Supported Things

This binding provides only one thing type: The inverter itself. Create one inverter thing per physical inverter installation available in your home(s). Additional stuff like batteries is automatically supported.

## Discovery

Auto-Discovery is not supported, as access requires authentication.

## Thing Configuration

The syntax for a inverter thing is:

```
solaredge:generic:<NAME>
```

- **solaredge** the binding id, fixed
- **generic** fixed value
- **name** the name of the inverter (choose any name)

There are a few settings this thing:

- **username** (required)  
username used to authenticate on SolarEdge

- **password** (required)  
password used to authenticate on SolarEdge

- **solarId** (required)  
Id of your inverter at SolarEdge (can be found in the URL after successful login: https://monitoring.solaredge.com/solaredge-web/p/site/**<<solarId>>**/#/dashboard)

- **legacyMode** (optional)  
can be set to true for old setups that do not contain a smart meter. In that case live data cannot be retrieved. With legacy mode at least the current production can be retrieved for those setups. (default = false)

- **liveDataPollingInterval** (optional)  
interval (seconds) in which live data values are retrieved from Solaredge. Setting less than 10 seconds is not recommended. (default = 30). 

- **"aggregateDataPollingInterval"** (optional)  
interval (seconds) in which aggregate data values are retrieved from Solaredge. Setting less than 60 seconds is not recommended. (default = 300). 

### Examples

- minimum configuration

```
solaredge:generic:se2200 [ username="...", password="...", solarId="..."]
```

- with pollingInterval

```
solaredge:generic:se2200[ username="...", password="...", solarId="...", liveDataPollingInterval=..., aggregateDataPollingInterval=... ]
```

- multiple inverters

```
solaredge:generic:home1 [ username="...", password="...", solarId="..."]
solaredge:generic:home2  [ username="...", password="...", solarId="..."]
```

## Channels

Available channels depend on the specific heatpump model. Following models/channels are currently available

| Channel Type ID                               | Item Type    | Description                                      | Remark                                 |
|-----------------------------------------------|--------------|--------------------------------------------------|----------------------------------------|
| live#production                               | Number       | Current PV production                            | general available                      |
| live#pv_status                                | String       | Current PV status                                | not available in legacy mode           |
| live#consumption                              | Number       | Current power consumption                        | not available in legacy mode           |
| live#load_status                              | String       | Current load status                              | not available in legacy mode           |
| live#battery_charge                           | Number       | Current charge flow                              | requires battery                       |
| live#battery_level                            | Number       | Current charge level                             | requires battery                       |
| live#battery_status                           | String       | Current battery status                           | requires battery                       |
| live#battery_critical                         | String       | true or false                                    | requires battery                       |
| live#import                                   | Number       | Current import from grid                         | not available in legacy mode           |
| live#export                                   | Number       | Current export to grid                           | not available in legacy mode           |
| live#grid_status                              | String       | Current grid status                              | not available in legacy mode           |
| aggregate_day#production                      | Number       | Day Aggregate PV production                      | general available                      |
| aggregate_day#consumption                     | Number       | Day Aggregate power consumption                  | requires a smart meter attached        |
| aggregate_day#selfConsumptionForConsumption   | Number       | Day Aggregate self consumption (incl battery)    | requires a smart meter attached        |
| aggregate_day#selfConsumptionCoverage         | Number       | Day Coverage of consumption by self production   | requires a smart meter attached        |
| aggregate_day#batterySelfConsumption          | Number       | Day Aggregate self consumption from battery      | requires battery                       |
| aggregate_day#import                          | Number       | Day Aggregate import from grid                   | requires a smart meter attached        |
| aggregate_day#export                          | Number       | Day Aggregate export to grid                     | requires a smart meter attached        |
| aggregate_week#production                     | Number       | Week Aggregate PV production                     | general available                      |
| aggregate_week#consumption                    | Number       | Week Aggregate power consumption                 | requires a smart meter attached        |
| aggregate_week#selfConsumptionForConsumption  | Number       | Week Aggregate self consumption (incl battery)   | requires a smart meter attached        |
| aggregate_week#selfConsumptionCoverage        | Number       | Week Coverage of consumption by self production  | requires a smart meter attached        |
| aggregate_week#batterySelfConsumption         | Number       | Week Aggregate self consumption from battery     | requires battery                       |
| aggregate_week#import                         | Number       | Week Aggregate import from grid                  | requires a smart meter attached        |
| aggregate_week#export                         | Number       | Week Aggregate export to grid                    | requires a smart meter attached        |
| aggregate_month#production                    | Number       | Month Aggregate PV production                    | general available                      |
| aggregate_month#consumption                   | Number       | Month Aggregate power consumption                | requires a smart meter attached        |
| aggregate_month#selfConsumptionForConsumption | Number       | Month Aggregate self consumption (incl battery)  | requires a smart meter attached        |
| aggregate_month#selfConsumptionCoverage       | Number       | Month Coverage of consumption by self production | requires a smart meter attached        |
| aggregate_month#batterySelfConsumption        | Number       | Month Aggregate self consumption from battery    | requires battery                       |
| aggregate_month#import                        | Number       | Month Aggregate import from grid                 | requires a smart meter attached        |
| aggregate_month#export                        | Number       | Month Aggregate export to grid                   | requires a smart meter attached        |
| aggregate_year#production                     | Number       | Year Aggregate PV production                     | general available                      |
| aggregate_year#consumption                    | Number       | Year Aggregate power consumption                 | requires a smart meter attached        |
| aggregate_year#selfConsumptionForConsumption  | Number       | Year Aggregate self consumption (incl battery)   | requires a smart meter attached        |
| aggregate_year#selfConsumptionCoverage        | Number       | Year Coverage of consumption by self production  | requires a smart meter attached        |
| aggregate_year#batterySelfConsumption         | Number       | Year Aggregate self consumption from battery     | requires battery                       |
| aggregate_year#import                         | Number       | Year Aggregate import from grid                  | requires a smart meter attached        |
| aggregate_year#export                         | Number       | Year Aggregate export to grid                    | requires a smart meter attached        |


## Full Example

### Thing

```
solaredge:generic:se2200     [ username="solar@edge.de", password="secret", solarId="4711", legacyMode=true, liveDataPollingInterval=15 ]
```

### Items

```
Number      SE2200_Live_Production   "PV Produktion [%.2f KW]"                {channel="solaredge:generic:se2200:live#production"}
Number      SE2200_Live_Level        "Batterieladung [%d %%]"                 {channel="solaredge:generic:se2200:live#battery_level"}
```
