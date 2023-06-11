# EcoTouch Binding

The openHAB EcoTouch binding allows interaction with a [Waterkotte](https://www.waterkotte.de/) heat pump.

## Supported Things

| Thing type      | Description                                      |
|-----------------|--------------------------------------------------|
| geo             | Waterkotte EcoTouch Geo + EcoVent                |
| air             | Waterkotte EcoTouch Air + EcoVent                |

This binding was tested with a Waterkotte DS 5027 Ai DS 5010.5Ai geothermal heat pump.

## Discovery

Discovery is not supported. You need to provide the IP address of the display unit of the heatpump.

## Thing Configuration

Things can be fully configured via the UI. The following information is useful when configuring things via thing configuration files.

| Property                        | Type    | Default | Required | Description |
|---------------------------------|---------|---------|----------|-------------|
| ip                              | String  |         | yes      | IP address or hostname of the display unit of the heat pump |
| username                        | String  |         | yes      | since software version 1.6.xx of the display unit: "waterkotte"; previously "admin". |
| password                        | String  |         | yes      | since software version 1.6.xx of the display unit: "waterkotte"; previously "wtkadmin". |
| refresh                         | Integer |      60 | no       | time in s after which all channels will be requested again from the heat pump |

## Channels

Standard channels:

| Channel ID                 | Type                 | Read-Only | Description                                                     |
|----------------------------|----------------------|-----------|-----------------------------------------------------------------|
| adapt_heating              | Number:Temperature   | no        | Allows to adjust the heating temperature by an offset           |
| alarm                      | Number:Dimensionless | yes       | Alarm                                                           |
| cop_cooling                | Number:Dimensionless | yes       | COP Cooling                                                     |
| cop_heating                | Number:Dimensionless | yes       | COP Heating                                                     |
| enable_cooling             | Switch               | no        | Enable Cooling                                                  |
| enable_heating             | Switch               | no        | Enable Heating                                                  |
| enable_warmwater           | Switch               | no        | Enable Warm Water                                               |
| power_compressor           | Number:Power         | yes       | Power Compressor                                                |
| power_cooling              | Number:Power         | yes       | Power Cooling                                                   |
| power_heating              | Number:Power         | yes       | Power Heating                                                   |
| state_alarm                | Switch               | yes       | State Alarm                                                     |
| temperature_cooling_return | Number:Temperature   | yes       | Temperature Cooling Return                                      |
| temperature_cooling_set2   | Number:Temperature   | no        | Temperature Cooling Setpoint BMS                                |
| temperature_flow           | Number:Temperature   | yes       | Temperature Flow                                                |
| temperature_heating_return | Number:Temperature   | yes       | Temperature Heating Return                                      |
| temperature_heating_set    | Number:Temperature   | yes       | Temperature Heating Setpoint                                    |
| temperature_outside        | Number:Temperature   | yes       | The current outside temperature                                 |
| temperature_outside_24h    | Number:Temperature   | yes       | The outside temperature averaged over one day                   |
| temperature_return         | Number:Temperature   | yes       | Temperature Return                                              |
| temperature_return_set     | Number:Temperature   | yes       | Temperature Return Setpoint                                     |
| temperature_room           | Number:Temperature   | yes       | Temperature Room                                                |
| temperature_room_1h        | Number:Temperature   | yes       | Temperature Room 1h Average                                     |
| temperature_source_in      | Number:Temperature   | yes       | Temperature Source Input                                        |
| temperature_source_out     | Number:Temperature   | yes       | Temperature Source Output                                       |
| temperature_water          | Number:Temperature   | yes       | Temperature Water                                               |
| temperature_water_set2     | Number:Temperature   | no        | Temperature Water Setpoint BMS                                  |

Advanced channels:

| Channel ID                       | Type                 | Read-Only | Description                                                             |
|----------------------------------|----------------------|-----------|-------------------------------------------------------------------------|
| compressor_power                 | Number:Dimensionless | yes       | Percent Power Compressor                                                |
| coolEnableTemp                   | Number:Temperature   | no        | Temperature Cooling Enable                                              |
| date_day                         | Number:Dimensionless | yes       | Day                                                                     |
| date_month                       | Number:Dimensionless | yes       | Month                                                                   |
| date_year                        | Number:Dimensionless | yes       | Year                                                                    |
| enable_pool                      | Switch               | no        | Enable Pool                                                             |
| enable_pv                        | Switch               | no        | Enable PV                                                               |
| hysteresis_heating               | Number:Temperature   | no        | Hysteresis Heating                                                      |
| interruptions                    | Number:Dimensionless | yes       | Interruptions                                                           |
| manual_4wayvalve                 | Number:Dimensionless | no        | Operating Mode 4Way Valve                                               |
| manual_coolvalve                 | Number:Dimensionless | no        | Operating Mode Cooling Valve                                            |
| manual_heatingpump               | Number:Dimensionless | no        | Operating Mode Heating Pump                                             |
| manual_multiext                  | Number:Dimensionless | no        | Operating Mode Multi Ouput Ext                                          |
| manual_poolvalve                 | Number:Dimensionless | no        | Operating Mode Pool Valve                                               |
| manual_solarpump1                | Number:Dimensionless | no        | Operating Mode Solar Pump                                               |
| manual_solarpump2                | Number:Dimensionless | no        | Operating Mode Solar Pump 2                                             |
| manual_sourcepump                | Number:Dimensionless | no        | Operating Mode Source Pump                                              |
| manual_tankpump                  | Number:Dimensionless | no        | Operating Mode Tank Pump                                                |
| manual_valve                     | Number:Dimensionless | no        | Operating Mode Valve                                                    |
| maxVLTemp                        | Number:Temperature   | yes       | maxVLTemp                                                               |
| nviHeizkreisNorm                 | Number:Temperature   | no        | nviHeizkreisNorm                                                        |
| nviNormAussen                    | Number:Temperature   | no        | nviNormAussen                                                           |
| nviSollKuehlen                   | Number:Temperature   | no        | nviSollKuehlen                                                          |
| nviTHeizgrenze                   | Number:Temperature   | no        | nviTHeizgrenze                                                          |
| nviTHeizgrenzeSoll               | Number:Temperature   | no        | nviTHeizgrenze Setpoint                                                 |
| operating_hours_circulation_pump | Number:Time          | yes       | Operating Hours Circulation Pump                                        |
| operating_hours_compressor1      | Number:Time          | yes       | Operating Hours Compressor 1                                            |
| operating_hours_compressor2      | Number:Time          | yes       | Operating Hours Compressor 2                                            |
| operating_hours_solar            | Number:Time          | yes       | Operating Hours Solar                                                   |
| operating_hours_source_pump      | Number:Time          | yes       | Operating Hours Source Pump                                             |
| percent_compressor               | Number:Dimensionless | yes       | Percent Compressor                                                      |
| percent_heat_circ_pump           | Number:Dimensionless | yes       | Percent Heating Circulation Pump                                        |
| percent_source_pump              | Number:Dimensionless | yes       | Percent Source Pump                                                     |
| position_expansion_valve         | Number:Dimensionless | yes       | Position Expansion Valve                                                |
| pressure_condensation            | Number:Pressure      | yes       | Pressure Condensation                                                   |
| pressure_evaporation             | Number:Pressure      | yes       | Pressure Evaporation                                                    |
| state                            | Number:Dimensionless | yes       | A Bitfield which encodes the state of all valves, pumps and compressors |
| state_compressor1                | Switch               | yes       | State Compressor 1                                                      |
| state_compressor2                | Switch               | yes       | State Compressor 2                                                      |
| state_cooling                    | Switch               | yes       | State Cooling                                                           |
| state_cooling4way                | Switch               | yes       | State Cooling4Way                                                       |
| state_evd                        | Switch               | yes       | State EVD                                                               |
| state_extheater                  | Switch               | yes       | State External Heater                                                   |
| state_heatingpump                | Switch               | yes       | State Heating Pump                                                      |
| state_pool                       | Switch               | yes       | State Pool                                                              |
| state_service                    | Switch               | yes       | State Service Mode                                                      |
| state_solar                      | Switch               | yes       | State Solar                                                             |
| state_sourcepump                 | Switch               | yes       | State Source Pump                                                       |
| state_water                      | Switch               | yes       | State Water                                                             |
| tempSet0Deg                      | Number:Temperature   | yes       | Heating Setpoint at 0°C Outside                                         |
| tempchange_cooling_pv            | Number:Temperature   | no        | Temperature Change Cooling if PV                                        |
| tempchange_heating_pv            | Number:Temperature   | no        | Temperature Change Heating if PV                                        |
| tempchange_pool_pv               | Number:Temperature   | no        | Temperature Change Pool if PV                                           |
| tempchange_warmwater_pv          | Number:Temperature   | no        | Temperature Change Water if PV                                          |
| temperature2_outside_1h          | Number:Temperature   | yes       | Temperature Outside 1h Average                                          |
| temperature_condensation         | Number:Temperature   | yes       | Temperature Condensation                                                |
| temperature_cooling_set          | Number:Temperature   | yes       | Temperature Cooling Setpoint                                            |
| temperature_evaporation          | Number:Temperature   | yes       | Temperature Evaporation                                                 |
| temperature_heating_set2         | Number:Temperature   | no        | Temperature Heating Setpoint BMS                                        |
| temperature_outside_1h           | Number:Temperature   | yes       | The outside temperature averaged over one hour                          |
| temperature_pool                 | Number:Temperature   | yes       | Temperature Pool                                                        |
| temperature_pool_set             | Number:Temperature   | yes       | Temperature Pool Setpoint                                               |
| temperature_pool_set2            | Number:Temperature   | no        | Temperature Pool Setpoint BMS                                           |
| temperature_solar                | Number:Temperature   | yes       | Temperature Solar                                                       |
| temperature_solar_flow           | Number:Temperature   | yes       | Temperature Solar Flow                                                  |
| temperature_storage              | Number:Temperature   | yes       | Temperature Storage                                                     |
| temperature_suction              | Number:Temperature   | yes       | Temperature Suction                                                     |
| temperature_water_set            | Number:Temperature   | yes       | Temperature Water Setpoint                                              |
| time_hour                        | Number:Dimensionless | yes       | Hour                                                                    |
| time_minute                      | Number:Dimensionless | yes       | Minute                                                                  |
| version_bios                     | Number:Dimensionless | yes       | Version BIOS                                                            |
| version_controller               | Number:Dimensionless | yes       | Version Display Controller                                              |
| version_controller_build         | Number:Dimensionless | yes       | Build Number Display Controller                                         |

If the Ecovent Unit is attached to the heat pump, the following additional channels are available:

| Channel ID                 | Type                 | Read-Only | Description |
|----------------------------|----------------------|-----------|-------------|
| ecovent_CO2_value          | Number:Dimensionless | yes       | EcoVent CO2                                                     |
| ecovent_mode               | Number:Dimensionless | no        | EcoVent Mode (0..5: Day, Night, Timer, Party, Vacation, Bypass) |
| ecovent_moisture_value     | Number:Dimensionless | yes       | EcoVent Air Moisture                                            |
| ecovent_output_y1          | Number:Dimensionless | yes       | EcoVent Fan                                                     |
| ecovent_temp_exhaust_air   | Number:Temperature   | yes       | EcoVent Temperature Exhaust Air                                 |
| ecovent_temp_exit_air      | Number:Temperature   | yes       | EcoVent Temperature Exit Air                                    |
| ecovent_temp_outdoor_air   | Number:Temperature   | yes       | EcoVent Temperature Outdoor Air                                 |
| ecovent_temp_supply_air    | Number:Temperature   | yes       | EcoVent Temperature Supply Air                                  |

The air heatpump has the following additional channels:

| Channel ID              | Type               | Read-Only | Description             |
|-------------------------|--------------------|-----------|-------------------------|
| temperature_suction_air | Number:Temperature | yes       | Temperature Suction Air |
| temperature_sump        | Number:Temperature | yes       | Temperature Sump        |
| temperature_surrounding | Number:Temperature | yes       | Temperature Surrounding |

## Example

### ecotouch.things

```java
Thing ecotouch:geo:heatpump "Waterkotte Heatpump" @ "basement" [ ip="192.168.1.100", username="admin", password="wtkadmin", refresh=120 ]
```

### ecotouch.items

```java
Number:Temperature HeatPump_Temp_Aussen     { channel="ecotouch:geo:heatpump:temperature_outside" }
Number:Temperature HeatPump_Temp_Aussen_1d  { channel="ecotouch:geo:heatpump:temperature_outside_24h" }
Number:Temperature HeatPump_Temp_Quelle_in  { channel="ecotouch:geo:heatpump:temperature_source_in" }
Number:Temperature HeatPump_Temp_Quelle_out { channel="ecotouch:geo:heatpump:temperature_source_out" }
Number:Temperature HeatPump_Temp_Wasser     { channel="ecotouch:geo:heatpump:temperature_water" }
Number:Temperature HeatPump_Temp_Heizen     { channel="ecotouch:geo:heatpump:temperature_heating_return" }
Number:Power HeatPump_power_el              { channel="ecotouch:geo:heatpump:power_compressor" }
Number:Power HeatPump_power_th              { channel="ecotouch:geo:heatpump:power_heating" }
Number HeatPump_COP_heating                 { channel="ecotouch:geo:heatpump:cop_heating" }
Number:Temperature HeatPump_adaptHeating    { channel="ecotouch:geo:heatpump:adapt_heating" }
Switch HeatPump_state_sourcepump            { channel="ecotouch:geo:heatpump:state_sourcepump" }
```

### ecotouch.sitemap

```perl
sitemap ecotouch label="Waterkotte EcoTouch"
{
    Text item=HeatPump_Temp_Aussen
    Text item=HeatPump_Temp_Aussen_1d
    Text item=HeatPump_Temp_Quelle_in
    Text item=HeatPump_Temp_Quelle_out
    Text item=HeatPump_Temp_Wasser
    Text item=HeatPump_Temp_Heizen
    Text item=HeatPump_Temp_Heizen
    Text item=HeatPump_power_th
    Text item=HeatPump_COP_heating
    Setpoint item=HeatPump_adaptHeating minValue=-2.0 maxValue=2.0 step=0.5
}
```

A snippet to show the current state of the heatpump (you need to have the corresponding items in your .items-file):

```java
    Text label="State" icon="settings" {
        Text item=HeatPump_state_sourcepump   label="State Source Pump [%s]"      valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_heatingpump  label="State Heating Pump [%s]"     valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_evd          label="State EVD [%s]"              valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_compressor1  label="State Compressor 1 [%s]"     valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_extheater    label="State External Heater [%s]"  valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_alarm        label="State Alarm [%s]"            valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_cooling      label="State Cooling [%s]"          valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_water        label="State Water [%s]"            valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_pool         label="State Pool [%s]"             valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_solar        label="State Solar [%s]"            valuecolor=[==ON="green", ==OFF="red"]
        Text item=HeatPump_state_cooling4way  label="State Cooling4Way [%s]"      valuecolor=[==ON="green", ==OFF="red"]
    }
```
