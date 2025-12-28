# Sonnen Binding

The binding for sonnen communicates with a sonnen battery.
More information about the sonnen battery can be found on the [Sonnen official website](https://sonnen.de/).
The binding supports the old deprecated V1 from sonnen as well as V2 which requires an authentication token.
More information about the V2 API can be found at `http://LOCAL-SONNENBATTERY-SYSTEM-IP/api/doc.html`

## Supported Things

| Thing Type    | Description                    |
| ------------- | ------------------------------ |
| sonnenbattery | Monitoring of a sonnen battery |

## Thing Configuration

Only the parameter `hostIP` is required; this is the IP address of the sonnen battery in your local network.
If you want to use the V2 API, which supports more channels, you need to provide the `authToken`.

## Channels

The following channels are yet supported:

| Channel                        | Type          | Access     | Description                                                                                                                                                                                                                                |
|--------------------------------|---------------|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| batteryChargingState           | Switch        | read       | Indicates if the Battery is charging at that moment                                                                                                                                                                                        |
| batteryCharging                | Number:Power  | read       | Indicates the actual current charging the Battery. Otherwise 0.                                                                                                                                                                            |
| batteryDischargingState        | Switch        | read       | Indicates if the Battery is discharging at that moment                                                                                                                                                                                     |
| batteryDischarging             | Number:Power  | read       | Indicates the actual current discharging the Battery. Otherwise 0.                                                                                                                                                                         |
| consumption                    | Number:Power  | read       | Indicates the actual consumption of the consumer in watt                                                                                                                                                                                   |
| gridFeedIn                     | Number:Power  | read       | Indicates the actual current feeding to the Grid in watt. 0 if nothing is fed                                                                                                                                                              |
| gridConsumption                | Number:Power  | read       | Indicates the actual current consumption from the Grid in watt. 0 if nothing is received                                                                                                                                                   |
| solarProduction                | Number:Power  | read       | Indicates the actual production of the Solar system in watt                                                                                                                                                                                |
| batteryLevel                   | Number        | read       | Indicates the actual Battery Level in % from 0 - 100                                                                                                                                                                                       |
| flowConsumptionBatteryState    | Switch        | read       | Indicates if there is a current flow from Battery towards Consumption                                                                                                                                                                      |
| flowConsumptionGridState       | Switch        | read       | Indicates if there is a current flow from Grid towards Consumption                                                                                                                                                                         |
| flowConsumptionProductionState | Switch        | read       | Indicates if there is a current flow from Solar Production towards Consumption                                                                                                                                                             |
| flowGridBatteryState           | Switch        | read       | Indicates if there is a current flow from Grid towards Battery                                                                                                                                                                             |
| flowProductionBatteryState     | Switch        | read       | Indicates if there is a current flow from Production towards Battery                                                                                                                                                                       |
| energyImportedStateProduction  | Number:Energy | read       | Indicates the imported kWh Production                                                                                                                                                                                                      |
| energyExportedStateProduction  | Number:Energy | read       | Indicates the exported kWh Production                                                                                                                                                                                                      |
| energyImportedStateConsumption | Number:Energy | read       | Indicates the imported kWh Consumption                                                                                                                                                                                                     |
| energyExportedStateConsumption | Number:Energy | read       | Indicates the exported kWh Consumption                                                                                                                                                                                                     |
| batteryChargingFromGrid        | Switch        | read/write | Starts/stops active battery charging from Grid. Note: "Write-API" must be activated in the local web interface, and the token must be entered in the binding's authentication field.                                                       |
| batteryOperationMode           | String        | read       | Indicates if the Battery is in automatic or manual mode. Manual mode is required for active charging. Ensure the battery is in automatic mode if not actively charging. Change operation mode using the "batteryChargingFromGrid" channel. |

## Full Example

example.things:

```java
Thing sonnen:sonnenbattery:myBattery "Sonnen Battery"  [ hostIP="192.168.0.10"]
```

example.items:

```java
Number:Energy Consumption { channel="sonnen:sonnenbattery:myBattery:consumption" }
Number:Energy GridFeeding { channel="sonnen:sonnenbattery:myBattery:gridFeedIn" }
Number BatteryLevel { channel="sonnen:sonnenbattery:myBattery:batteryLevel" }
Switch FlowConsumptionBattery { channel="sonnen:sonnenbattery:myBattery:flowConsumptionBattery" }
```

## Tested Hardware

The binding was successfully tested with the following sonnen battery:

- sonnen eco 8.0 SW Version: 1.6.10.1221979
- sonnen eco 9.43 SW Version: 1.15.6.3277222
