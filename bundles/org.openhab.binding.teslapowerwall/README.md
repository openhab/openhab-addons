# TeslaPowerwall Binding

This binding enables the capture of key data from a Tesla Powerwall 2 into openHAB.

## Supported Things

- `powerwall` Tesla Powerwall 2

## Discovery

The binding does not support auto discovery.

## Thing Configuration

As a minimum, the hostname is needed:

* hostname - The hostname of the Tesla Powerwall 2. Defaults to powerwall to avoid SSL certificate issues
* email - the email of the local account on the Powerwall that the installer provided
* password - the password of the local account on the Powerwall that the installer provided
* refresh - The frequency with which to refresh information from the Tesla Powerwall2 specified in seconds. Defaults to 10 seconds.

## Channels

| channel id             | type           | description                                                                           |
|------------------------|----------------|---------------------------------------------------------------------------------------|
| gridstatus             | String         | Current status of the Power Grid
| batterysoe             | Number:Percent | Current battery state of charge
| mode                   | String         | Current operating mode
| reserve                | Number:Percent | Current battery reserve %
| grid-instpower         | Number:Power   | Instantaneous Grid Power Supply
| battery-instpower      | Number:Power   | Instantaneous Battery Power Supply
| home-instpower         | Number:Power   | Instantaneous Home Power Supply
| solar-instpower        | Number:Power   | Instantaneous Solar Power Supply
| grid-energyexported    | Number:Energy  | Total Grid Energy Exported
| battery-energyexported | Number:Energy  | Total Battery Energy Exported
| home-energyexported    | Number:Energy  | Total Home Energy Exported
| solar-energyexported   | Number:Energy  | Total Solar Energy Exported
| grid-energyimported    | Number:Energy  | Total Grid Energy Imported
| battery-energyimported | Number:Energy  | Total Battery Energy Imported
| home-energyimported    | Number:Energy  | Total Home Energy Imported
| solar-energyimported   | Number:Energy  | Total Solar Energy Imported
| degradation            | Number:Percent | Current battery degradation % (Based on single battery)
| full-pack-energy       | Number:Energy  | Reported battery capacity at full

## Full Example

### `teslapowerwall.things`:

```java
teslapowerwall:teslapowerwall:TeslaPowerwall [ hostname="192.168.0.5" ]
```

### `teslapowerwall.item`s:

```java
String TeslaPowerwall_gridstatus { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:gridstatus" }
Number:Percent TeslaPowerwall_batterysoe { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:batterysoe" }
String TeslaPowerwall_mode { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:mode" }
Number:Percent TeslaPowerwall_reserve { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:reserve" }
Number:Power TeslaPowerwall_grid_instpower { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:grid-instpower" }
Number:Power TeslaPowerwall_battery_instpower { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:battery-instpower" }
Number:Power TeslaPowerwall_home_instpower { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:home-instpower" }
Number:Power TeslaPowerwall_solar_instpower { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:solar-instpower" }
Number:Energy TeslaPowerwall_grid_energyexported { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:grid-energyexported" }
Number:Energy TeslaPowerwall_grid_energyimported { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:grid-energyimported" }
Number:Energy TeslaPowerwall_battery_energyexported { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:battery-energyexported" }
Number:Energy TeslaPowerwall_battery_energyimported { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:battery-energyimported" }
Number:Energy TeslaPowerwall_home_energyexported { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:home-energyexported" }
Number:Energy TeslaPowerwall_home_energyimported { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:home-energyimported" }
Number:Energy TeslaPowerwall_solar_energyexported { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:solar-energyexported" }
Number:Energy TeslaPowerwall_solar_energyimported { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:solar-energyimported" }
Number:Percent TeslaPowerwall_degradation { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:degradation" }
Number:Energy TeslaPowerwall_full_pack_energy { channel="teslapowerwall:teslapowerwall:TeslaPowerwall:full-pack-energy" }
```

### `teslapowerwall.sitemap`:

```perl
Text item=OpenGarage_StatusText label="Status"
Text item=TeslaPowerwall_gridstatus label="Grid Status [%s]"
Text item=TeslaPowerwall_batterysoe label="Battery Charge"
Text item=TeslaPowerwall_mode label="Battery Mode"
Text item=TeslaPowerwall_reserve label="Battery Reserve"
Text item=TeslaPowerwall_grid_instpower label="Grid Power [%.1f W]"
Text item=TeslaPowerwall_battery_instpower label="Battery Power [%.1f W]"
Text item=TeslaPowerwall_home_instpower label="Home Power [%.1f W]"
Text item=TeslaPowerwall_solar_instpower label="Solar Power [%.1f W]"
Text item=TeslaPowerwall_grid_energyexported label="Grid Energy Exported [%.1f kWh]"
Text item=TeslaPowerwall_grid_energyimported label="Grid Energy Imported [%.1f kWh]"
Text item=TeslaPowerwall_battery_energyexported label="Battery Energy Exported [%.1f kWh]"
Text item=TeslaPowerwall_battery_energyimported label="Battery Energy Imported [%.1f kWh]"
Text item=TeslaPowerwall_home_energyexported label="Home Energy Exported [%.1f kWh]"
Text item=TeslaPowerwall_home_energyimported label="Home Energy Imported [%.1f kWh]"
Text item=TeslaPowerwall_solar_energyexported label="Solar Energy Exported [%.1f kWh]"
Text item=TeslaPowerwall_solar_energyimported label="Solar Energy Imported [%.1f kWh]"
Text item=TeslaPowerwall_Battery_full_pack_energy label="Full Pack Energy"
Text item=TeslaPowerwall_Battery_degradation label="Degradation level"
```

