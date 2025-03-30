# Stiebel Eltron ISG

This extension adds support for the Stiebel Eltron modbus protocol.

An Internet Service Gateway (ISG) with an installed modbus extension is required in order to run this binding.
In case the modbus extension is not yet installed on the ISG, the ISG Updater Tool for the update can be found here: <https://www.stiebel-eltron.de/de/home/produkte-loesungen/erneuerbare_energien/regelung_energiemanagement/internet_servicegateway/isg_web/downloads.html>

## Supported Things

This bundle adds the following thing types to the Modbus binding.
Note, that the things will show up under the Modbus binding.

| Thing              | ThingTypeID | Description                                         |
| ------------------ | ----------- | --------------------------------------------------- |
| Stiebel Eltron ISG | heatpump    | A stiebel eltron heat pump connected through an ISG |

## Discovery

This extension does not support autodiscovery. The things need to be added manually.

A typical bridge configuration would look like this:

```java
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1 ]
```

## Thing Configuration

You need first to set up a TCP Modbus bridge according to the Modbus documentation.
Things in this extension will use the selected bridge to connect to the device.

The following parameters are valid for all thing types:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| refresh   | integer | no       | 5                  | Poll interval in seconds. Increase this if you encounter connection errors |
| maxTries  | integer | no       | 3                  | Number of retries when before giving up reading from this thing.           |

## Channels

Channels are grouped into channel groups.

### System State Group

This group contains general operational information about the heat pump.

| Channel ID       | Item Type | Read only | Description                                                   |
| ---------------- | --------- | --------- | ------------------------------------------------------------- |
| is-heating       | Contact   | true      | OPEN in case the heat pump is currently in heating mode       |
| is-heating-water | Contact   | true      | OPEN in case the heat pump is currently in heating water mode |
| is-cooling       | Contact   | true      | OPEN in case the heat pump is currently in cooling mode       |
| is-pumping       | Contact   | true      | OPEN in case the heat pump is currently in pumping mode       |
| is-summer        | Contact   | true      | OPEN in case the heat pump is currently in summer mode        |

### System Parameters Group

This group contains system paramters of the heat pump.

| Channel ID                  | Item Type          | Read only | Description                                                                                                                                    |
| --------------------------- | ------------------ | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| operation-mode              | Number             | false     | The current operation mode of the heat pump (1=ready mode, 2=program mode, 3=comfort mode, 4=eco mode, 5=heating water mode, 0=emergency mode) |
| comfort-temperature-heating | Number:Temperature | false     | The current heating comfort temperature                                                                                                        |
| eco-temperature-heating     | Number:Temperature | false     | The current heating eco temperature                                                                                                            |
| comfort-temperature-water   | Number:Temperature | false     | The current water comfort temperature                                                                                                          |
| eco-temperature-water       | Number:Temperature | false     | The current water eco temperature                                                                                                              |

### System Information Group

This group contains general operational information about the device.

| Channel ID                 | Item Type            | Read only | Description                                           |
| -------------------------- | -------------------- | --------- | ----------------------------------------------------- |
| fek-temperature            | Number:Temperature   | true      | The current temperature measured by the FEK           |
| fek-temperature-setpoint   | Number:Temperature   | true      | The current set point of the FEK temperature          |
| fek-humidity               | Number:Dimensionless | true      | The current humidity measured by the FEK              |
| fek-dewpoint               | Number:Temperature   | true      | The current dew point temperature measured by the FEK |
| outdoor-temperature        | Number:Temperature   | true      | The current outdoor temperature                       |
| hk1-temperature            | Number:Temperature   | true      | The current temperature of the HK1                    |
| hk1-temperature-setpoint   | Number:Temperature   | true      | The current temperature set point of the HK1          |
| supply-temperature         | Number:Temperature   | true      | The current supply temperature                        |
| return-temperature         | Number:Temperature   | true      | The current return measured                           |
| source-temperature         | Number:Temperature   | true      | The current sourcetemperature                         |
| water-temperature          | Number:Temperature   | true      | The current water temperature                         |
| water-temperature-setpoint | Number:Temperature   | true      | The current water temperature set point               |

### Energy Information Group

This group contains about the energy consumption and delivery of the heat pump.

| Channel ID              | Item Type     | Read only | Description                                      |
| ----------------------- | ------------- | --------- | ------------------------------------------------ |
| production-heat-today   | Number:Energy | true      | The heat quantity delivered today                |
| production-heat-total   | Number:Energy | true      | The heat quantity delivered in total             |
| production-water-today  | Number:Energy | true      | The water heat quantity delivered today          |
| production-water-total  | Number:Energy | true      | The water heat quantity delivered in total       |
| consumption-heat-today  | Number:Energy | true      | The power consumption for heating today          |
| consumption-heat-total  | Number:Energy | true      | The power consumption for heating in total       |
| consumption-water-today | Number:Energy | true      | The power consumption for water heating today    |
| consumption-water-total | Number:Energy | true      | The power consumption for water heating in total |

## Full Example

### Thing Configuration

```java
Bridge modbus:tcp:bridge "Stiebel Modbus TCP"[ host="hostname|ip", port=502, id=1 ] {
 Thing heatpump stiebelEltron "StiebelEltron" (modbus:tcp:modbusbridge) @"Room"  [ ]
}
```

### Item Configuration

```java
Number:Temperature stiebel_eltron_temperature_fek            "Temperature FEK [%.1f °C]" <temperature>    { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-temperature" }
Number:Temperature stiebel_eltron_setpoint_fek            "Set point FEK [%.1f °C]" <temperature>    { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-temperature-setpoint" }
Number:Dimensionless stiebel_eltron_humidity_fek            "Humidity FEK [%.1f %%]" <humidity>   { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-humidity" }
Number:Temperature stiebel_eltron_dewpoint_fek            "Dew point FEK [%.1f °C]" <temperature>    { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-dewpoint" }

Number:Temperature stiebel_eltron_outdoor_temp            "Outdoor temperature [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemInformation#outdoor-temperature" }
Number:Temperature stiebel_eltron_temp_hk1                "Temperature HK1 [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemInformation#hk1-temperature" }
Number:Temperature stiebel_eltron_setpoint_hk1            "Set point HK1 [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemInformation#hk1-temperature-setpoint" }
Number:Temperature stiebel_eltron_temp_water                "Water temperature  [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemInformation#water-temperature" }
Number:Temperature stiebel_eltron_setpoint_water            "Water setpoint [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemInformation#water-temperature-setpoint" }
Number:Temperature stiebel_eltron_source_temp            "Source temperature [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemInformation#source-temperature" }
Number:Temperature stiebel_eltron_vorlauf_temp            "Supply tempertature [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemInformation#supply-temperature" }
Number:Temperature stiebel_eltron_ruecklauf_temp            "Return temperature  [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemInformation#return-temperature" }

Number stiebel_eltron_heating_comfort_temp              "Heating Comfort Temperature [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemParameter#comfort-temperature-heating" }
Number stiebel_eltron_heating_eco_temp              "Heating Eco Temperature [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemParameter#eco-temperature-heating" }
Number stiebel_eltron_water_comfort_temp              "Water Comfort Temperature [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemParameter#comfort-temperature-water" }
Number stiebel_eltron_water_eco_temp              "Water Eco Temperature [%.1f °C]"    { channel="modbus:heatpump:stiebelEltron:systemParameter#eco-temperature-water" }
Number stiebel_eltron_operation_mode           "Operation Mode"   { channel="modbus:heatpump:stiebelEltron:systemParameter#operation-mode" }

Contact stiebel_eltron_mode_pump               "Pump [%d]"   { channel="modbus:heatpump:stiebelEltron:systemState#is-pumping" }
Contact stiebel_eltron_mode_heating             "Heating [%d]"   { channel="modbus:heatpump:stiebelEltron:systemState#is-heating" }
Contact stiebel_eltron_mode_water              "Heating Water [%d]"   { channel="modbus:heatpump:stiebelEltron:systemState#is-heating-water" }
Contact stiebel_eltron_mode_cooling             "Cooling [%d]"   { channel="modbus:heatpump:stiebelEltron:systemState#is-cooling" }
Contact stiebel_eltron_mode_summer             "Summer Mode [%d]"   { channel="modbus:heatpump:stiebelEltron:systemState#is-summer" }

Number:Energy stiebel_eltron_production_heat_today            "Heat quantity today [%.0f kWh]"    { channel="modbus:heatpump:stiebelEltron:energyInformation#production_heat_today" }
Number:Energy stiebel_eltron_production_heat_total            "Heat quantity total  [%.3f MWh]"   {channel="modbus:heatpump:stiebelEltron:energyInformation#production_heat_total"}
Number:Energy stiebel_eltron_production_water_today            "Water heat quantity today  [%.0f kWh]"    { channel="modbus:heatpump:stiebelEltron:energyInformation#production_water_today" }
Number:Energy stiebel_eltron_production_water_total            "Water heat quantity total  [%.3f MWh]"   {channel="modbus:heatpump:stiebelEltron:energyInformation#production_water_total"}
Number:Energy stiebel_eltron_consumption_heat_total             "Heating power consumption total [%.3f MWh]"  {channel="modbus:heatpump:stiebelEltron:energyInformation#consumption_heat_total"}
Number:Energy stiebel_eltron_consumption_heat_today            "Heating power consumption today [%.0f kWh]"    { channel="modbus:heatpump:stiebelEltron:energyInformation#consumption_heat_today" }
Number:Energy stiebel_eltron_consumption_water_today            "Water heating power consumption today  [%.0f kWh]"    { channel="modbus:heatpump:stiebelEltron:energyInformation#consumption_water_today" }
Number:Energy stiebel_eltron_consumption_water_total            "Water heating power consumption total [%.3f MWh]"   {channel="modbus:heatpump:stiebelEltron:energyInformation#consumption_water_total"}

```

### Sitemap Configuration

```perl
Text label="Heat pumpt" icon="temperature" {
 Frame label="Optation Mode" {
  Default item=stiebel_eltron_mode_pump
  Default item=stiebel_eltron_mode_heating
  Default item=stiebel_eltron_mode_water
  Default item=stiebel_eltron_mode_cooling
  Default item=stiebel_eltron_mode_summer
 }
 Frame label= "State" {
  Default item=stiebel_eltron_operation_mode icon="settings"
  Default item=stiebel_eltron_outdoor_temp  icon="temperature"
  Default item=stiebel_eltron_temp_hk1  icon="temperature"
  Default item=stiebel_eltron_setpoint_hk1  icon="temperature"
  Default item=stiebel_eltron_vorlauf_temp  icon="temperature"
  Default item=stiebel_eltron_ruecklauf_temp  icon="temperature"
  Default item=stiebel_eltron_temp_water  icon="temperature"
  Default item=stiebel_eltron_setpoint_water icon="temperature"
  Default item=stiebel_eltron_temperature_fek  icon="temperature"
  Default item=stiebel_eltron_setpoint_fek icon="temperature"
  Default item=stiebel_eltron_humidity_fek icon="humidity"
  Default item=stiebel_eltron_dewpoint_fek icon="temperature"
  Default item=stiebel_eltron_source_temp icon="temperature"
 }
 Frame label="Paramters" {
  Setpoint item=stiebel_eltron_heating_comfort_temp icon="temperature" step=1 minValue=5 maxValue=30
  Setpoint item=stiebel_eltron_heating_eco_temp icon="temperature" step=1 minValue=5 maxValue=30
  Setpoint item=stiebel_eltron_water_comfort_temp icon="temperature" step=1 minValue=10 maxValue=60
  Setpoint item=stiebel_eltron_water_eco_temp icon="temperature" step=1 minValue=10 maxValue=60
 }
 Frame label="Energy consumption" {
  Default item=stiebel_eltron_consumption_heat_today icon="energy"
  Default item=stiebel_eltron_consumption_heat_total icon="energy"
  Default item=stiebel_eltron_consumption_water_today icon="energy"
  Default item=stiebel_eltron_consumption_water_total icon="energy"
 }
 Frame label="Heat quantity" {
  Default item=stiebel_eltron_production_heat_today icon="radiator"
  Default item=stiebel_eltron_production_heat_total icon="radiator"
  Default item=stiebel_eltron_production_water_today icon="water"
  Default item=stiebel_eltron_production_water_total icon="water"
 }

}

```
