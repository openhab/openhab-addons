# Haas+Sohn Pellet Stove Binding

This binding communicates with a Haas+Sohn pellet stove through the optional Wi‑Fi module.
More information about the Wi‑Fi module can be found here: <https://www.haassohn.com/de/ihr-plus/WLAN-Funktion>

## Supported Things

| Things               | Description                            | Thing Type |
|----------------------|----------------------------------------|------------|
| haassohnpelletstove  | Control of a Haas+Sohn pellet stove    | oven       |

## Thing Configuration

Two parameters are required: the IP address of the stove’s Wi‑Fi module on the local network and the stove’s access PIN.
The PIN can be found directly at the stove under Menu → Network → WLAN-PIN.

```java
Thing haassohnpelletstove:oven:myOven "Pelletstove"  [ hostIP="192.168.0.23", hostPIN="1234"]
```

## Channels

The following channels are supported:

| Channel | Type  | Access| Description|
|---------|-------|-------|------------|
| power                | Switch            | read/write | Turn the stove on/off                             |
| channelIsTemp        | Number:Temperature| read       | Current stove temperature                         |
| channelSpTemp        | Number:Temperature| read/write | Target stove temperature                          |
| channelMode          | String            | read       | Current stove mode (e.g., heating, error)         |
| channelEcoMode       | Switch            | read/write | Enable/disable Eco Mode                           |
| channelIgnitions     | Number            | read       | Total number of ignitions                         |
| channelMaintenanceIn | Number:Mass       | read       | Estimated pellets until next maintenance (kg)     |
| channelCleaningIn    | String            | read       | Estimated time until next cleaning (hh:mm)        |
| channelConsumption   | Number:Mass       | read       | Total pellet consumption                          |
| channelOnTime        | Number            | read       | Total operating hours                             |

## Full Example

demo.items:

```java
Number:Temperature isTemp { channel="oven:channelIsTemp" }
Number:Temperature spTemp { channel="oven:channelSpTemp" }
String mode { channel="oven:channelMode" }
Switch power { channel="oven:power" }
```

## Google Assistant configuration

See also: <https://www.openhab.org/docs/ecosystem/google-assistant/>

googleassistantdemo.items

```java
Group g_FeuerThermostat "FeuerThermostat" {ga="Thermostat" }
Number StatusFeuer "Status Feuer" (g_FeuerThermostat) { ga="thermostatMode" }
Number ZieltemperaturFeuer "ZieltemperaturFeuer" (g_FeuerThermostat) {ga="thermostatTemperatureSetpoint"}
Number TemperaturFeuer "TemperaturFeuer" (g_FeuerThermostat) {ga="thermostatTemperatureAmbient"}
```

## Tested Hardware

The binding has been successfully tested with the following ovens:

- HSP 7 DIANA
- HSP6 434.08
