# SolarEdge Binding

The SolarEdge binding is used to get live data from from SolarEdge inverters via the central web api. This binding should in general be compatible with all inverter models that upload data to the solaredge portal.
Only read access is supported.

## Supported Things

This binding provides only one thing type: "generic" which is the inverter itself. As the name suggests it is generic which means it applies to all available inverters. Create one inverter thing per physical inverter installation available in your home(s). Additional stuff like batteries is automatically supported.

## Discovery

Auto-Discovery is not supported, as access requires authentication.

## Thing Configuration

The following configuration parameters are available for this thing:

- **tokenOrApiKey** (required)
Either the official API Key for using the public API or when using the inofficial private API: a token which can be retrieved from browser's cookie store when logged into the solaredge website. It is called 'SPRING_SECURITY_REMEMBER_ME_COOKIE'

- **solarId** (required)  
Id of your inverter at SolarEdge (can be found in the URL after successful login: https://monitoring.solaredge.com/solaredge-web/p/site/**<<solarId>>**/#/dashboard)

- **usePrivateApi** (optional)  
can be set to true to use the private API. Private API has no limit regarding query frequency but is less stable. Private API will only gather live data if a meter is available. The official public API ha a limit of 300 queries per day but should be much more reliable/stable. (default = false)

- **meterInstalled** (optional)  
can be set to true for setups that contain a SolarEdge modbus meter (see here: https://www.solaredge.com/products/pv-monitoring/accessories/css-wattnode-modbus-meter ). A meter allows more detailed live data retrieval. (default = false)

- **liveDataPollingInterval** (optional)  
interval (minutes) in which live data values are retrieved from Solaredge. Setting less than 10 minutes is only allowed when using private API. (default = 10). 

- **"aggregateDataPollingInterval"** (optional)  
interval (minutes) in which aggregate data values are retrieved from Solaredge. Setting less than 60 is only allowed when using private API. (default = 60). 

### Examples

- minimum configuration

```
solaredge:generic:se2200 [ tokenOrApiKey="...", solarId="..."]
```

- with pollingIntervals

```
solaredge:generic:se2200[ tokenOrApiKey="...", solarId="...", liveDataPollingInterval=..., aggregateDataPollingInterval=... ]
```

- multiple inverters

```
solaredge:generic:home1 [ tokenOrApiKey="...", solarId="..."]
solaredge:generic:home2  [ tokenOrApiKey="...", solarId="..."]
```

## Channels

Available channels depend on the specific setup e.g. if a meter and/or a battery is present. Following models/channels are currently available

| Channel Type ID                               | Item Type    | Description                                      | Remark                                          |
|-----------------------------------------------|--------------|--------------------------------------------------|-------------------------------------------------|
| live#production                               | Number       | Current PV production                            | general available                               |
| live#pv_status                                | String       | Current PV status                                | only available when 'meterInstalled' is set     |
| live#consumption                              | Number       | Current power consumption                        | only available when 'meterInstalled' is set     |
| live#load_status                              | String       | Current load status                              | only available when 'meterInstalled' is set     |
| live#battery_charge                           | Number       | Current charge flow                              | requires battery                                |
| live#battery_discharge                        | Number       | Current discharge flow                           | requires battery                                |
| live#battery_charge_discharge                 | Number       | Current charge/discharge flow (+/-)              | requires battery                                |
| live#battery_level                            | Number       | Current charge level                             | requires battery                                |
| live#battery_status                           | String       | Current battery status                           | requires battery                                |
| live#battery_critical                         | String       | true or false                                    | requires battery                                |
| live#import                                   | Number       | Current import from grid                         | only available when 'meterInstalled' is set     |
| live#export                                   | Number       | Current export to grid                           | only available when 'meterInstalled' is set     |
| live#grid_status                              | String       | Current grid status                              | only available when 'meterInstalled' is set     |
| aggregate_day#production                      | Number       | Day Aggregate PV production                      | general available                               |
| aggregate_day#consumption                     | Number       | Day Aggregate power consumption                  | requires solaredge modbus meter attached        |
| aggregate_day#selfConsumptionForConsumption   | Number       | Day Aggregate self consumption (incl battery)    | requires solaredge modbus meter attached        |
| aggregate_day#selfConsumptionCoverage         | Number       | Day Coverage of consumption by self production   | requires solaredge modbus meter attached        |
| aggregate_day#batterySelfConsumption          | Number       | Day Aggregate self consumption from battery      | requires battery and private API activated      |
| aggregate_day#import                          | Number       | Day Aggregate import from grid                   | requires solaredge modbus meter attached        |
| aggregate_day#export                          | Number       | Day Aggregate export to grid                     | requires solaredge modbus meter attached        |
| aggregate_week#production                     | Number       | Week Aggregate PV production                     | general available                               |
| aggregate_week#consumption                    | Number       | Week Aggregate power consumption                 | requires solaredge modbus meter attached        |
| aggregate_week#selfConsumptionForConsumption  | Number       | Week Aggregate self consumption (incl battery)   | requires solaredge modbus meter attached        |
| aggregate_week#selfConsumptionCoverage        | Number       | Week Coverage of consumption by self production  | requires solaredge modbus meter attached        |
| aggregate_week#batterySelfConsumption         | Number       | Week Aggregate self consumption from battery     | requires battery and private API activated      |
| aggregate_week#import                         | Number       | Week Aggregate import from grid                  | requires solaredge modbus meter attached        |
| aggregate_week#export                         | Number       | Week Aggregate export to grid                    | requires solaredge modbus meter attached        |
| aggregate_month#production                    | Number       | Month Aggregate PV production                    | general available                               |
| aggregate_month#consumption                   | Number       | Month Aggregate power consumption                | requires solaredge modbus meter attached        |
| aggregate_month#selfConsumptionForConsumption | Number       | Month Aggregate self consumption (incl battery)  | requires solaredge modbus meter attached        |
| aggregate_month#selfConsumptionCoverage       | Number       | Month Coverage of consumption by self production | requires solaredge modbus meter attached        |
| aggregate_month#batterySelfConsumption        | Number       | Month Aggregate self consumption from battery    | requires battery and private API activated      |
| aggregate_month#import                        | Number       | Month Aggregate import from grid                 | requires solaredge modbus meter attached        |
| aggregate_month#export                        | Number       | Month Aggregate export to grid                   | requires solaredge modbus meter attached        |
| aggregate_year#production                     | Number       | Year Aggregate PV production                     | general available                               |
| aggregate_year#consumption                    | Number       | Year Aggregate power consumption                 | requires solaredge modbus meter attached        |
| aggregate_year#selfConsumptionForConsumption  | Number       | Year Aggregate self consumption (incl battery)   | requires solaredge modbus meter attached        |
| aggregate_year#selfConsumptionCoverage        | Number       | Year Coverage of consumption by self production  | requires solaredge modbus meter attached        |
| aggregate_year#batterySelfConsumption         | Number       | Year Aggregate self consumption from battery     | requires battery and private API activated      |
| aggregate_year#import                         | Number       | Year Aggregate import from grid                  | requires solaredge modbus meter attached        |
| aggregate_year#export                         | Number       | Year Aggregate export to grid                    | requires solaredge modbus meter attached        |


## Full Example

### Thing

```
solaredge:generic:se2200     [ tokenOrApiKey="secret", solarId="4711", meterInstalled=true, liveDataPollingInterval=15 ]
```

### Items

```
Number      SE2200_Live_Production   "PV Produktion [%.2f KW]"                {channel="solaredge:generic:se2200:live#production"}
Number      SE2200_Live_Level        "Batterieladung [%d %%]"                 {channel="solaredge:generic:se2200:live#battery_level"}
```
