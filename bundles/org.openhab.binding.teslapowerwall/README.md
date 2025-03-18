# TeslaPowerwall Binding

This binding enables the capture of key data from a Tesla Powerwall 2 into openHAB.

## Supported Things

- `powerwall` Tesla Powerwall 2

## Discovery

The binding does not support auto discovery.

## Thing Configuration

As a minimum, the hostname is needed:

- hostname - The hostname of the Tesla Powerwall 2. Defaults to powerwall to avoid SSL certificate issues
- email - the email of the local account on the Powerwall that the installer provided
- password - the password of the local account on the Powerwall that the installer provided
- refresh - The frequency with which to refresh information from the Tesla Powerwall2 specified in seconds. Defaults to 10 seconds.

## Channels

| channel id                | type                 | description                                                  |
|---------------------------|----------------------|--------------------------------------------------------------|
| grid-status               | String               | Current status of the Power Grid                             |
| battery-soe               | Number:Dimensionless | Current battery state of charge                              |
| mode                      | String               | Current operating mode                                       |
| reserve                   | Number:Dimensionless | Current battery reserve %                                    |
| grid-inst-power           | Number:Power         | Instantaneous Grid Power Supply                              |
| battery-inst-power        | Number:Power         | Instantaneous Battery Power Supply                           |
| home-inst-power           | Number:Power         | Instantaneous Home Power Supply                              |
| solar-inst-power          | Number:Power         | Instantaneous Solar Power Supply                             |
| grid-energy-exported      | Number:Energy        | Total Grid Energy Exported                                   |
| battery-energy-exported   | Number:Energy        | Total Battery Energy Exported                                |
| home-energy-exported      | Number:Energy        | Total Home Energy Exported                                   |
| solar-energy-exported     | Number:Energy        | Total Solar Energy Exported                                  |
| grid-energy-imported      | Number:Energy        | Total Grid Energy Imported                                   |
| battery-energy-imported   | Number:Energy        | Total Battery Energy Imported                                |
| home-energy-imported      | Number:Energy        | Total Home Energy Imported                                   |
| solar-energy-imported     | Number:Energy        | Total Solar Energy Imported                                  |
| degradation               | Number:Dimensionless | Current battery degradation % (Based on single battery)      |
| full-pack-energy          | Number:Energy        | Reported battery capacity at full                            |

## Full Example

### `teslapowerwall.things`

```java
teslapowerwall:tesla-powerwall:TeslaPowerwall [ hostname="192.168.0.5" ]
```

### `teslapowerwall.items`

```java
String TeslaPowerwall_grid-status { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:grid-status" }
Switch TeslaPowerwall_grid-services { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:grid-services" }
Number:Dimensionless TeslaPowerwall_battery-soe { channel="tesla-powerwall:teslapowerwall:TeslaPowerwall:battery-soe", unit="%" }
String TeslaPowerwall_mode { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:mode" }
Number:Dimensionless TeslaPowerwall_reserve { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:reserve", unit="%" }
Number:Power TeslaPowerwall_grid-inst-power { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:grid-inst-power" }
Number:Power TeslaPowerwall_battery-inst-power { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:battery-inst-power" }
Number:Power TeslaPowerwall_home-inst-power { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:home-inst-power" }
Number:Power TeslaPowerwall_solar-inst-power { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:solar-inst-power" }
Number:Energy TeslaPowerwall_grid-energy-exported { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:grid-energy-exported" }
Number:Energy TeslaPowerwall_grid-energy-imported { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:grid-energy-imported" }
Number:Energy TeslaPowerwall_battery-energy-exported { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:battery-energy-exported" }
Number:Energy TeslaPowerwall_battery-energy-imported { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:battery-energy-imported" }
Number:Energy TeslaPowerwall_home-energy-exported { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:home-energy-exported" }
Number:Energy TeslaPowerwall_home-energy-imported { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:home-energy-imported" }
Number:Energy TeslaPowerwall_solar-energy-exported { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:solar-energy-exported" }
Number:Energy TeslaPowerwall_solar-energy-imported { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:solar-energy-imported" }
Number:Dimensionless TeslaPowerwall_degradation { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:degradation", unit="%" }
Number:Energy TeslaPowerwall_full-pack-energy { channel="teslapowerwall:tesla-powerwall:TeslaPowerwall:full-pack-energy" }
```

### `teslapowerwall.sitemap`

```perl
Text item=TeslaPowerwall_grid-status label="Grid Status [%s]"
Text item=TeslaPowerwall_grid-services label="Grid Services Status [%s]"
Text item=TeslaPowerwall_battery-soe label="Battery Charge"
Text item=TeslaPowerwall_mode label="Battery Mode"
Text item=TeslaPowerwall_reserve label="Battery Reserve"
Text item=TeslaPowerwall_grid-inst-power label="Grid Power [%.1f W]"
Text item=TeslaPowerwall_battery-inst-power label="Battery Power [%.1f W]"
Text item=TeslaPowerwall_home-inst-power label="Home Power [%.1f W]"
Text item=TeslaPowerwall_solar-inst-power label="Solar Power [%.1f W]"
Text item=TeslaPowerwall_grid-energy-exported label="Grid Energy Exported [%.1f kWh]"
Text item=TeslaPowerwall_grid-energy-imported label="Grid Energy Imported [%.1f kWh]"
Text item=TeslaPowerwall_battery-energy-exported label="Battery Energy Exported [%.1f kWh]"
Text item=TeslaPowerwall_battery-energyi-mported label="Battery Energy Imported [%.1f kWh]"
Text item=TeslaPowerwall_home-energy-exported label="Home Energy Exported [%.1f kWh]"
Text item=TeslaPowerwall_home-energy-imported label="Home Energy Imported [%.1f kWh]"
Text item=TeslaPowerwall_solar-energy-exported label="Solar Energy Exported [%.1f kWh]"
Text item=TeslaPowerwall_solar-energy-imported label="Solar Energy Imported [%.1f kWh]"
Text item=TeslaPowerwall_full-pack-energy label="Full Pack Energy"
Text item=TeslaPowerwall_degradation label="Degradation level"
```
