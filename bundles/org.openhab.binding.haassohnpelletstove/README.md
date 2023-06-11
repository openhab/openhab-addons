# Haas Sohn Pellet Stove Binding

The binding for Haassohnpelletstove communicates with a Haas and Sohn Pelletstove through the optional
WIFI module. More information about the WIFI module can be found here: <https://www.haassohn.com/de/ihr-plus/WLAN-Funktion>

## Supported Things

| Things | Description  | Thing Type |
|--------|--------------|------------|
| haassohnpelletstove | Control of a Haas & Sohn Pellet Stove| oven|

## Thing Configuration

In general two parameters are required. The IP-Address of the WIFI-Modul of the Stove in the local Network and the Access PIN of the Stove.
The PIN can be found directly at the stove under the Menue/Network/WLAN-PIN

```java
Thing haassohnpelletstove:oven:myOven "Pelletstove"  [ hostIP="192.168.0.23", hostPIN="1234"]
```

## Channels

The following channels are yet supported:

| Channel | Type  | Access| Description|
|---------|-------|-------|------------|
| power| Switch | read/write|Turn the stove on/off|
|channelIsTemp|Number:Temperature|read|Receives the actual temperature of the stove|
|channelSpTemp|Number:Temperature|read/write|Receives and sets the target temperature of the stove|
|channelMode|String|read|Receives the actual mode the stove is in like heating, cooling, error, ....|
|channelEcoMode|Switch|read/write|Turn the eco mode of the stove on/off|
|channelIngitions|Number|read|Amount of ignitions of the stove|
|channelMaintenanceIn|Number:Mass|read|States the next maintenance in kg|
|channelCleaningIn|String|read|States the next cleaning window in hours:minutes as string|
|channelConsumption|Number:Mass|read|Total consumption of the stove|
|channelOnTime|Number|read|Operation hours of the stove|

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

The binding was successfully tested with the following ovens:

- HSP 7 DIANA
- HSP6 434.08
