# Sonnen Binding

The binding for sonnen communicates with a sonnen battery. More information about the sonnen battery can be found here: https://sonnen.de/

## Supported Things

| Things | Description  | Thing Type |
|--------|--------------|------------|
| sonnen | Control of a sonnen battery | battery|


## Thing Configuration

Only one parameter is required. The IP-Address of the sonnen battery in your local network.

```
Thing sonnen:battery:myBattery "sonnenBattery"  [ hostIP="192.168.0.10"]
```

## Channels

The following channels are yet supported:


| Channel | Type  | Access| Description|
|---------|-------|-------|------------|
| isBatteryCharging| Switch | read|Indicates if the Battery is charging at that moment|
|isBatteryDischarging|Switch|read|Indicates if the Battery is discharging at that moment|
|consumption|Number:Energy|read|Indicates the actual consumption of the consumer in watt|
|gridFeedIn|Number:Energy|read|Indicates the actual feeding to the Grid in watt|
|solarProduction|Number:Energy|read|Indicates the actual production of the Solar system in watt|
|batteryLevel|Number|read|Indicates the actual Battery Level in % from 0 - 100|
|isFlowConsumptionBattery|Switch|read|Indicates if there is a current flow from Battery towards Consumption|
|isFlowConsumptionGrid|Switch|read|Indicates if there is a current flow from Grid towards Consumption|
|isFlowConsumptionProduction|Switch|read|Indicates if there is a current flow from Solar Production towards Consumption|
|isFlowGridBattery|Switch|read|Indicates if there is a current flow from Grid towards Battery|
|isFlowProductionBattery|Switch|read|Indicates if there is a current flow from Production towards Battery|
|isFlowProductionGrid|Switch|read|Indicates if there is a current flow from Production towards Grid|

## Full Example

demo.items:

```
Number:Energy Consumption { channel="battery:consumption" }
Number:Energy GridFeeding { channel="battery:gridFeedIn" }
Number BatteryLevel { channel="battery:batteryLevel" }
Switch FlowConsumptionBattery { channel="oven:isFlowConsumptionBattery" }
```

## Tested Hardware

The binding was successfully tested with the following sonnen battery:

- sonnnen eco 8.0 SW Version: 1.6.10.1221979
