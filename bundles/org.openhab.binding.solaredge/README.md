# SolarEdge Binding

The SolarEdge binding retrieves live data from SolarEdge inverters via the central web API. This binding should in general be compatible with all inverter models that upload data to the SolarEdge portal.
Only read access is supported.

## Supported Things

This binding provides only one Thing type: "generic," which is the inverter itself.
As the name suggests, it is generic, which means it applies to all available inverters.
Create one inverter Thing per physical inverter installation available in your home(s).
Additional components such as batteries are automatically supported.
Inverters that have a meter attached allow more detailed measurements.
Either a SolarEdge Modbus meter or an S0 meter (export or consumption meter) can be used.
While the S0 meter is the cheaper solution, the SolarEdge meter can be used as a combined import and export meter and therefore allows even more detailed measurements.
For more details please see here:

- [SolarEdge meter](https://www.solaredge.com/products/pv-monitoring/accessories/css-wattnode-modbus-meter)
- [Avoiding Feed-In limitations with consumption meters](https://www.solaredge.com/solutions/feed-in-limitation-and-metering-solution#)
- [Detailed description of meter setup](https://solaredge.com/sites/default/files/feed-in_limitation_application_note.pdf)

## Discovery

Auto-Discovery is not supported, as access requires authentication.

## Thing Configuration

The following configuration parameters are available for this Thing:

- **tokenOrApiKey** (required)
Either the [official API key](https://www.youtube.com/watch?v=iR26nmL5bXg) for using the public API, or when using the unofficial private API: a token that can be retrieved from your browser cookie store when logged into the SolarEdge website.
It is called "SPRING_SECURITY_REMEMBER_ME_COOKIE".
When using this token, see also `usePrivateApi` and `meterInstalled`.
For Firefox, use the built-in [Storage Inspector](https://developer.mozilla.org/en-US/docs/Tools/Storage_Inspector) to retrieve the token.

- **solarId** (required)<br>
ID of your inverter at SolarEdge (can be found in the URL after successful login: <https://monitoring.solaredge.com/solaredge-web/p/site/> **&lt;solarId&gt;** /#/dashboard)

- **usePrivateApi** (optional)<br>
Can be set to true to use the private API.
The private API has no limit regarding query frequency but is less stable.
The private API will only gather live data if a meter is available.
The official public API has a limit of 300 queries per day but should be more reliable and stable.
Set this to true when using a token retrieved from the browser in `tokenOrApiKey`.
See also `meterInstalled`. (Default: false)

- **meterInstalled** (optional)<br>
Can be set to true for setups that contain a meter that is connected to the inverter.
A meter allows more detailed data retrieval.
This must be set to true when using a token retrieved from the browser in `tokenOrApiKey`.
This can be set either to true or false when using the API key. (Default: false)

- **liveDataPollingInterval** (optional)<br>
Interval (minutes) in which live data values are retrieved from SolarEdge.
Setting less than 10 minutes is only allowed when using the private API. (Default: 10)

- **aggregateDataPollingInterval** (optional)<br>
Interval (minutes) in which aggregate data values are retrieved from SolarEdge.
Setting less than 60 minutes is only allowed when using the private API. (Default: 60)

## Channels

Available channels depend on the specific setup, e.g., if a meter and/or a battery is present.
All numeric channels use the [UoM feature](https://openhab.org/blog/2018/02/22/units-of-measurement.html).
This means you can easily change the desired unit (e.g., MWh instead of kWh) just in your item definition.
The following channels are currently available:

| Channel Type ID                               | Item Type            | Description                                      | Remark                                           |
| --------------------------------------------- | -------------------- | ------------------------------------------------ | ------------------------------------------------ |
| live#production                               | Number:Power         | Current PV production                            | generally available                              |
| live#pv_status                                | String               | Current PV status                                | requires meter attached and 'meterInstalled' set |
| live#consumption                              | Number:Power         | Current power consumption                        | requires meter attached and 'meterInstalled' set |
| live#load_status                              | String               | Current load status                              | requires meter attached and 'meterInstalled' set |
| live#battery_charge                           | Number:Power         | Current charge flow                              | requires battery                                 |
| live#battery_discharge                        | Number:Power         | Current discharge flow                           | requires battery                                 |
| live#battery_charge_discharge                 | Number:Power         | Current charge/discharge flow (+/-)              | requires battery                                 |
| live#battery_level                            | Number:Dimensionless | Current charge level                             | requires battery                                 |
| live#battery_status                           | String               | Current battery status                           | requires battery                                 |
| live#battery_critical                         | String               | true or false                                    | requires battery                                 |
| live#import                                   | Number:Power         | Current import from grid                         | requires meter attached and 'meterInstalled' set |
| live#export                                   | Number:Power         | Current export to grid                           | requires meter attached and 'meterInstalled' set |
| live#grid_status                              | String               | Current grid status                              | requires meter attached and 'meterInstalled' set |
| aggregate_day#production                      | Number:Energy        | Day Aggregate PV production                      | general available                                |
| aggregate_day#consumption                     | Number:Energy        | Day Aggregate power consumption                  | requires meter attached and 'meterInstalled' set |
| aggregate_day#selfConsumptionForConsumption   | Number:Energy        | Day Aggregate self consumption (incl battery)    | requires meter attached and 'meterInstalled' set |
| aggregate_day#selfConsumptionCoverage         | Number:Dimensionless | Day coverage of consumption by self‑production   | requires meter attached and 'meterInstalled' set |
| aggregate_day#batterySelfConsumption          | Number:Energy        | Day Aggregate self consumption from battery      | requires battery and private API activated       |
| aggregate_day#import                          | Number:Energy        | Day Aggregate import from grid                   | requires meter attached and 'meterInstalled' set |
| aggregate_day#export                          | Number:Energy        | Day Aggregate export to grid                     | requires meter attached and 'meterInstalled' set |
| aggregate_week#production                     | Number:Energy        | Week Aggregate PV production                     | requires meter attached and 'meterInstalled' set |
| aggregate_week#consumption                    | Number:Energy        | Week Aggregate power consumption                 | requires meter attached and 'meterInstalled' set |
| aggregate_week#selfConsumptionForConsumption  | Number:Energy        | Week Aggregate self consumption (incl battery)   | requires meter attached and 'meterInstalled' set |
| aggregate_week#selfConsumptionCoverage        | Number:Dimensionless | Week coverage of consumption by self‑production  | requires meter attached and 'meterInstalled' set |
| aggregate_week#batterySelfConsumption         | Number:Energy        | Week Aggregate self consumption from battery     | requires battery and private API activated       |
| aggregate_week#import                         | Number:Energy        | Week Aggregate import from grid                  | requires meter attached and 'meterInstalled' set |
| aggregate_week#export                         | Number:Energy        | Week Aggregate export to grid                    | requires meter attached and 'meterInstalled' set |
| aggregate_month#production                    | Number:Energy        | Month Aggregate PV production                    | general available                                |
| aggregate_month#consumption                   | Number:Energy        | Month Aggregate power consumption                | requires meter attached and 'meterInstalled' set |
| aggregate_month#selfConsumptionForConsumption | Number:Energy        | Month Aggregate self consumption (incl battery)  | requires meter attached and 'meterInstalled' set |
| aggregate_month#selfConsumptionCoverage       | Number:Dimensionless | Month coverage of consumption by self‑production | requires meter attached and 'meterInstalled' set |
| aggregate_month#batterySelfConsumption        | Number:Energy        | Month Aggregate self consumption from battery    | requires battery and private API activated       |
| aggregate_month#import                        | Number:Energy        | Month Aggregate import from grid                 | requires meter attached and 'meterInstalled' set |
| aggregate_month#export                        | Number:Energy        | Month Aggregate export to grid                   | requires meter attached and 'meterInstalled' set |
| aggregate_year#production                     | Number:Energy        | Year Aggregate PV production                     | general available                                |
| aggregate_year#consumption                    | Number:Energy        | Year Aggregate power consumption                 | requires meter attached and 'meterInstalled' set |
| aggregate_year#selfConsumptionForConsumption  | Number:Energy        | Year Aggregate self consumption (incl battery)   | requires meter attached and 'meterInstalled' set |
| aggregate_year#selfConsumptionCoverage        | Number:Dimensionless | Year coverage of consumption by self‑production  | requires meter attached and 'meterInstalled' set |
| aggregate_year#batterySelfConsumption         | Number:Energy        | Year Aggregate self consumption from battery     | requires battery and private API activated       |
| aggregate_year#import                         | Number:Energy        | Year Aggregate import from grid                  | requires meter attached and 'meterInstalled' set |
| aggregate_year#export                         | Number:Energy        | Year Aggregate export to grid                    | requires meter attached and 'meterInstalled' set |

## Full Example

### Thing

- Minimum configuration

```java
solaredge:generic:se2200 [ tokenOrApiKey="...", solarId="..."]
```

- With polling intervals

```java
solaredge:generic:se2200[ tokenOrApiKey="...", solarId="...", liveDataPollingInterval=..., aggregateDataPollingInterval=... ]
```

- Full configuration

```java
solaredge:generic:se2200     [ tokenOrApiKey="secret", solarId="4711", meterInstalled=true, usePrivateApi=true, liveDataPollingInterval=15, aggregateDataPollingInterval=60 ]
```

- Multiple inverters

```java
solaredge:generic:home1 [ tokenOrApiKey="...", solarId="..."]
solaredge:generic:home2  [ tokenOrApiKey="...", solarId="..."]
```

### Items

```java
Number:Power            SE2200_Live_Production   "PV Produktion [%.2f %unit%]"    {channel="solaredge:generic:se2200:live#production"}
Number:Dimensionless    SE2200_Live_Level        "Batterieladung"                 {channel="solaredge:generic:se2200:live#battery_level"}
Number:Energy           SE2200_Day_Production    "PV Produktion [%.2f kWh]"       {channel="solaredge:generic:se2200:aggregate_day#production"}
```
