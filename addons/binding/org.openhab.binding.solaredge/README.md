# <bindingName> SolarEdge Binding

The SolarEdge binding is used to get live data from from SolarEdge inverters via the central web api. This binding should in general be compatible with all inverter models that upload data to the solaredge portal.
Only read access is supported.

## Supported Things

This binding provides only one thing type: The inverter itself. Create one inverter thing per physical inverter installation available in your home(s). Additional stuff like batteries is automatically supported.

## Discovery

Auto-Discovery is not supported, as access requires authentication.

## Thing Configuration

The syntax for a inverter thing is:

```
solaredge:web:<NAME>
```

- **solaredge** the binding id, fixed
- **web** fixed value
- **name** the name of the inverter (choose any name)

There are a few settings this thing:

- **username** (required)  
username used to authenticate on SolarEdge

- **password** (required)  
password used to authenticate on SolarEdge

- **solarId** (required)  
Id of your inverter at SolarEdge (can be found in the URL after successful login: https://monitoring.solaredge.com/solaredge-web/p/site/**<<solarId>>**/#/dashboard)

- **pollingInterval**  
interval (seconds) in which values are retrieved from NibeUplink. Setting less than 10 seconds is not recommended. (default = 60). 

### Examples

- minimum configuration

```
solaredge:web:se2200 [ username="...", password="...", solarId="..."]
```

- with pollingInterval

```
solaredge:web:se2200[ username="...", password="...", solarId="...", pollingInterval=... ]
```

- multiple inverters

```
solaredge:web:home1 [ username="...", password="...", solarId="..."]
solaredge:web:home2  [ username="...", password="...", solarId="..."]
```

## Channels

Available channels depend on the specific heatpump model. Following models/channels are currently available

| Channel Type ID                          | Item Type    | Description                                |
|------------------------------------------|--------------|--------------------------------------------|
| live#production                          | Number       | Current PV production                      |
| live#pv_status                           | String       | Current PV status                          |
| live#consumption                         | Number       | Current power consumption                  |
| live#load_status                         | String       | Current load status                        |
| live#battery_charge                      | Number       | Current charge flow                        |
| live#battery_level                       | Number       | Current charge level                       |
| live#battery_status                      | String       | Current battery status                     |
| live#battery_critical                    | String       | true or false                              |
| live#import                              | Number       | Current import from grid                   |
| live#export                              | Number       | Current export to grid                     |
| live#grid_status                         | String       | Current grid status                        |
| aggregate#production                     | Number       | Aggregate PV production                    |
| aggregate#consumption                    | Number       | Aggregate power consumption                |
| aggregate#selfConsumptionForConsumption  | Number       | Aggregate self consumption (incl battery)  |
| aggregate#batterySelfConsumption         | Number       | Aggregate self consumption from battery    |
| aggregate#import                         | Number       | Aggregate import from grid                 |
| aggregate#export                         | Number       | Aggregate export to grid                   |


## Full Example

### Thing

```
solaredge:web:se2200     [ username="solar@edge.de", password="secret", nibeId="4711", pollingInterval=30 ]
```

### Items

```
Number      SE2200_Live_Production   "PV Produktion [%.2f KW]"                {channel="solaredge:web:se2200:live#production"}
Number      SE2200_Live_Level        "Batterieladung [%d %%]"                 {channel="solaredge:web:se2200:live#battery_level"}
```
