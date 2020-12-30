# Luxtronik/Novelan Heat Pump Binding

This binding was originally created for the Novelan heat pump. Since the Novelan control unit is based on the Luxtronik 2 contol unit of Alpha Innotec, this binding should work with all heat pumps that use this type of controller.

Note: The whole functionality was reverse engineered via tcpdump, so use it at your own risk.

These parameters can be changed:

* Heating operation mode
* Warm water operation mode
* Cooling operation mode
* Cooling release temperature
* Cooling inlet temperature
* Cooling start after hours
* Cooling stop after hours
* Offset of the heating curve
* Target temperature for warm water

## Prerequisites

The heat pump binding connects to your heat pump via network.
Make sure your heat pump is connected to your network and the network settings are configured.
To access the network settings go in the heat pump `Service menu` -> `system control` -> `IP address`.

## Supported Things

This binding only supports the `heatpump` thing type which represents one of your heat pumps.

## Discovery

This binding has no auto discovery. You have to create a thing for your heatpump manually.

## Thing Configuration

| Property | Default | Required | Description                               |
|----------|---------|----------|-------------------------------------------|
| host       |         |   Yes    | hostname or IP address of the heat pump to connect to |
| port     | 8888    |   No     | port number of the heat pump to connect to Please be aware that from firmware version 1.73, Alpha Innotec has changed the port to 8889. |
| connectionTimeout  | 5000      |   No     | connection timeout in microseconds               |
| pollingInterval  | 60      |   No     | polling interval in seconds               |

## Channels

| channel | type | description |
|---------------|-----------|---------|
| `temperature_outside` | Number | the measured temperature by the outside sensor |
| `temperature_outside_avg` | Number | the average measured temperature by the outside sensor |
| `temperature_return` | Number | the temperature returned by floor heating |
| `temperature_reference_return` | Number | the reference temperature of the heating water |
| `temperature_supply` | Number | the temperature sent to the floor heating |
| `temperature_servicewater_reference` | Number | the reference temperature of the servicewater |
| `temperature_servicewater` | Number | the temperature of the servicewater |
| `state` | Number | contains the state; Possible states are error, running, stopped, defrosting |
| `extended_state` | Number | contains the state; Possible states are error, heating, standby, switch-on delay, switching cycle | blocked, provider lock time, service water, screed heat up, defrosting, pump flow, desinfection, cooling, pool water, heating ext., service water ext., | flow monitoring, ZWE operation |
| `state_time` | Number | contains the time of the state in elapsed seconds |
| `switchoff_reason_0` | Number | contains the shutdown reason at slot 0 |
| `switchoff_reason_1` | Number | contains the shutdown reason at slot 1 |
| `switchoff_reason_2` | Number | contains the shutdown reason at slot 2 |
| `switchoff_reason_3` | Number | contains the shutdown reason at slot 3 |
| `switchoff_reason_4` | Number | contains the shutdown reason at slot 4 |
| `switchoff_reason_timestamp_0` | Number | contains the timestamp of shutdown slot 0 |
| `switchoff_reason_timestamp_1` | Number | contains the timestamp of shutdown slot 1 |
| `switchoff_reason_timestamp_2` | Number | contains the timestamp of shutdown slot 2 |
| `switchoff_reason_timestamp_3` | Number | contains the timestamp of shutdown slot 3 |
| `switchoff_reason_timestamp_4` | Number | contains the timestamp of shutdown slot 4 |
| `switchoff_error_0` | Number | contains the heatpump error code at slot 0 |
| `switchoff_error_1` | Number | contains the heatpump error code at slot 1 |
| `switchoff_error_2` | Number | contains the heatpump error code at slot 2 |
| `switchoff_error_3` | Number | contains the heatpump error code at slot 3 |
| `switchoff_error_4` | Number | contains the heatpump error code at slot 4 |
| `switchoff_error_timestamp_0` | Number | contains the timestamp of error slot 0 |
| `switchoff_error_timestamp_1` | Number | contains the timestamp of error slot 1 |
| `switchoff_error_timestamp_2` | Number | contains the timestamp of error slot 2 |
| `switchoff_error_timestamp_3` | Number | contains the timestamp of error slot 3 |
| `switchoff_error_timestamp_4` | Number | contains the timestamp of error slot 4 |
| `temperature_solar_collector` | Number | the temperature of the sensor in the solar collector |
| `temperature_hot_gas` | Number |
| `temperature_probe_in` | Number | temperature flowing to probe head |
| `temperature_probe_out` | Number | temperature coming  from probe head |
| `temperature_mk1` | Number | |
| `temperature_mk1_reference` | Number | |
| `temperature_mk2` | Number | |
| `temperature_mk2_reference` | Number | |
| `temperature_external_source` | Number |
| `time_compressor1` | String | operating time of compressor one |
| `starts_compressor1` | Number | total starts of compressor one |
| `time_compressor2` | String | operating time of compressor two |
| `starts_compressor2` | Number | total starts of compressor two |
| `temperature_out_external` | Number | |
| `time_zwe1` | String | |
| `time_zwe2` | String | |
| `time_zwe3` | String | |
| `time_heatpump` | String | operating time of heatpump |
| `time_heating` | String | operating time of heating |
| `time_warmwater` | String | operating time creating warm water |
| `time_cooling` | String | operating time of cooling |
| `thermalenergy_heating` | Number | total energy for heating in KWh |
| `thermalenergy_warmwater` | Number | total energy for creating warm water in KWh |
| `thermalenergy_pool` | Number | total energy for heating pool in KWh |
| `thermalenergy_total` | Number | sum of all total energy in KWh |
| `massflow` | Number | |
| `temperature_solar_storage` | Number | the temperature of the solar storage |
| `heating_operation_mode` | Number | operation mode (0="Auto", 1="Zuheizer", 2="Party", 3="Ferien", 4="Aus") |
| `heating_temperature` | Number | heating curve offset |
| `warmwater_operation_mode` | Number | (0="Auto", 1="Zuheizer", 2="Party", 3="Ferien", 4="Aus") |
| `warmwater_temperature` | Number | target temperature for warm water |
| `cooling_operation_mode` | Number | (1="Auto", 0="Off") |
| `cooling_release_temperature` | Number | cooling release temeprature |
| `cooling_inlet_temperature` | Number | cooling inlet temeprature |
| `cooling_start_hours` | Number | cooling start after hours |
| `cooling_stop_hours` | Number | cooling stop after hours |
| `output_av` | Switch | Output: defrosting (= Abtauventil) |
| `output_bup` | Switch | Output: water pump (= Brauchwasserpumpe) |
| `output_hup` | Switch | Output: heat pump (= Heizungsumwälzpumpe) |
| `output_mz1` | Switch | |
| `output_ven` | Switch | Output: ventilation |
| `output_vbo` | Switch | |
| `output_vd1` | Switch | Output: compressor 1 |
| `output_vd2` | Switch | Output: compressor 2 |
| `output_zip` | Switch | Output: water circulation pump |
| `output_zup` | Switch | Output: additional water pump |
| `output_zw1` | Switch | Output: additional heater 1 |
| `output_zw2sst` | Switch | Output: additional heater 2 |
| `output_zw3sst` | Switch | Output: additional heater 3 |
| `output_fp2` | Switch | |
| `output_slp` | Switch | |
| `output_sup` | Switch | |
| `output_ma2` | Switch | |
| `output_mz2` | Switch | |
| `output_ma3` | Switch | |
| `output_mz3` | Switch | |
| `output_fp3` | Switch | |
| `output_vsk` | Switch | |
| `output_frh` | Switch | |
| `output_vdh` | Switch | Output: compressor heating |
| `output_av2` | Switch | Output: defrosting 2 (= Abtauventil 2) |
| `output_vbo2` | Switch | |
| `output_vd12` | Switch | Output: compressor 1/2 |
| `output_vdh2` | Switch | Output: compressor heating 2 |


## Full Example

### Thing

```
Thing luxtronik:heatpump:myheatpump [ host="192.168.25.12", port="8888", connectionTimeout="5000", pollingInterval="60" ]
```

### Items

```
Number HeatPump_Temperature_1   "Wärmepumpe Außentemperatur [%.1f °C]"   <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_outside" }
Number HeatPump_Temperature_2   "Rücklauf [%.1f °C]"  <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_return" }
Number HeatPump_Temperature_3   "Rücklauf Soll [%.1f °C]" <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_reference_return" }
Number HeatPump_Temperature_4   "Vorlauf [%.1f °C]"    <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_supply" }
Number HeatPump_Temperature_5   "Brauchwasser Soll [%.1f °C]"  <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_servicewater_reference" }
Number HeatPump_Temperature_6   "Brauchwasser Ist [%.1f °C]"   <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_servicewater" }
Number HeatPump_Temperature_7   "Solarkollektor [%.1f °C]" <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_solar_collector" }
Number HeatPump_Temperature_8   "Solarspeicher [%.1f °C]"  <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_solar_storage" }
Number HeatPump_State   "Status [%s]"   (gHeatpump) { channel="luxtronik:heatpump:myheatpump:state" }
Number HeatPump_State_Time   "Status seit [%s s]"  (gHeatpump) { channel="luxtronik:heatpump:myheatpump:state_time" }
Number HeatPump_Retrun_External     "Rücklauf Extern [%.1f °C]"   <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_out_external" } // return external
Number HeatPump_Hot_Gas     "Temperatur Heissgas [%.1f °C]"    <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_hot_gas" } // return hot gas
Number HeatPump_Outside_Avg     "mittlere Aussentemperatur [%.1f °C]"  <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_outside_avg" }
Number HeatPump_Probe_in    "Sondentemperatur Eingang [%.1f °C]"   <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_probe_in" }
Number HeatPump_Probe_out   "Sondentemperatur Ausgang [%.1f °C]"   <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_probe_out" }
Number HeatPump_Mk1     "Vorlauftemperatur MK1 IST [%.1f °C]"  <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_mk1" }
Number HeatPump_Mk1_Reference   "Vorlauftemperatur MK1 SOLL [%.1f °C]" <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_mk1_reference" }
Number HeatPump_Mk2     "Vorlauftemperatur MK2 IST [%.1f °C]"  <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_mk2" }
Number HeatPump_Mk2_Reference   "Vorlauftemperatur MK2 SOLL [%.1f °C]" <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_mk2_reference" }
Number HeatPump_External_Source     "Temperatur externe Energiequelle [%.1f °C]"   <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:temperature_external_source" }
Number HeatPump_Time_Compressor1   "Bertriebszeit Verdichter1 [%d s]"  <time> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:time_compressor1" }
Number HeatPump_Starts_Compressor1  "Verdichter 1 [%d]"  (gHeatpump) { channel="luxtronik:heatpump:myheatpump:starts_compressor1" }
Number HeatPump_Time_Compressor2   "Bertriebszeit Verdichter2 [%d s]"  <time> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:time_compressor2" }
Number HeatPump_Starts_Compressor2  "Verdichter 2 [%d]"  (gHeatpump) { channel="luxtronik:heatpump:myheatpump:starts_compressor2" }
Number HeatPump_Time_Zwe1  "Bertriebszeit ZWE1 [%d s]" <time> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:time_zwe1" }
Number HeatPump_Time_Zwe2  "Bertriebszeit ZWE2 [%d s]" <time> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:time_zwe2" }
Number HeatPump_Time_Zwe3  "Bertriebszeit ZWE3 [%d s]" <time> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:time_zwe3" }
Number HeatPump_Time_Heatpump  "Bertriebszeit [%d s]"  <time> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:time_heatpump" }
Number HeatPump_Time_Heating   "Bertriebszeit Heizung [%d s]"  <time> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:time_heating" }
Number HeatPump_Time_Warmwater "Bertriebszeit Brauchwasser [%d s]" <time> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:time_warmwater" }
Number HeatPump_Time_Cooling   "Bertriebszeit Kuehlung [%d s]" <time> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:time_cooling" }
Number HeatPump_Thermalenergy_Heating   "Waermemenge Heizung [%.1f KWh]"    <energy> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:thermalenergy_heating" }
Number HeatPump_Thermalenergy_Warmwater     "Waermemenge Brauchwasser [%.1f KWh]"   <energy> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:thermalenergy_warmwater" }
Number HeatPump_Thermalenergy_Pool  "Waermemenge Schwimmbad [%.1f KWh]" <energy> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:thermalenergy_pool" }
Number HeatPump_Thermalenergy_Total     "Waermemenge gesamt seit Reset [%.1f KWh]"  <energy> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:thermalenergy_total" }
Number HeatPump_Massflow    "Massentrom [%d L/h]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:massflow" }
Number HeatPump_State_Ext   "Status [%s]"   <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:extended_state" }
String HeatPump_heating_operation_mode   "Heizung Betriebsart [%s]"  (gHeatpump) { channel="luxtronik:heatpump:myheatpump:heating_operation_mode" }
Number HeatPump_heating_temperature   "Heizung Temperatur [%.1f °C]" <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:heating_temperature" }
String HeatPump_warmwater_operation_mode   "Warmwasser Betriebsart [%s]"  (gHeatpump) { channel="luxtronik:heatpump:myheatpump:warmwater_operation_mode" }
Number HeatPump_warmwater_temperature   "Warmwasser Temperatur [%.1f °C]" <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:warmwater_temperature" }
String HeatPump_Cool_BA "Betriebsart Kühlung[%s]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:cooling_operation_mode" }
Number HeatPump_Cooling_Release "Freigabe [%.1f °C]" <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:cooling_release_temperature" }
Number HeatPump_Cooling_Inlet "Vorlauf Soll [%.1f °C]" <temperature> (gHeatpump) { channel="luxtronik:heatpump:myheatpump:cooling_inlet_temperature" }
Number HeatPump_Cooling_Start "AT Überschreitung[%.1f hrs]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:cooling_start_hours" }
Number HeatPump_Cooling_Stop "AT Unterschreitung[%.1f hrs]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:cooling_stop_hours" }
Switch HeatPump_HUP  "Heizungsumwälzpumpe [%s]"   <switch>   (gHeatpump)   { channel="luxtronik:heatpump:myheatpump:output_hup" }
Number HeatPump_Reason_Code_0 "Abschaltgrund 0 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_0" }
DateTime HeatPump_Reason_Timestamp_0 "Abschaltgrund Zeitstempel 0 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_timestamp_0" }
Number HeatPump_Reason_Code_1 "Abschaltgrund 1 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_1" }
DateTime HeatPump_Reason_Timestamp_1 "Abschaltgrund Zeitstempel 1 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_timestamp_1" }
Number HeatPump_Reason_Code_2 "Abschaltgrund 2 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_2" }
DateTime HeatPump_Reason_Timestamp_2 "Abschaltgrund Zeitstempel 2 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_timestamp_2" }
Number HeatPump_Reason_Code_3 "Abschaltgrund 3 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_3" }
DateTime HeatPump_Reason_Timestamp_3 "Abschaltgrund Zeitstempel 3 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_timestamp_3" }
Number HeatPump_Reason_Code_4 "Abschaltgrund 4 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_4" }
DateTime HeatPump_Reason_Timestamp_4 "Abschaltgrund Zeitstempel 4 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_reason_timestamp_4" }
Number HeatPump_Error_Count "Fehlerzhahl [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_count" }
Number HeatPump_Error_Code_0 "Fehlergrund 0 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_0" }
DateTime HeatPump_Error_Timestamp_0 "Fehler Zeitstempel 0 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_timestamp_0" }
Number HeatPump_Error_Code_1 "Fehlergrund 1 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_1" }
DateTime HeatPump_Error_Timestamp_1 "Fehler Zeitstempel 1 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_timestamp_1" }
Number HeatPump_Error_Code_2 "Fehlergrund 2 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_2" }
DateTime HeatPump_Error_Timestamp_2 "Fehler Zeitstempel 2 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_timestamp_2" }
Number HeatPump_Error_Code_3 "Fehlergrund 3 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_3" }
DateTime HeatPump_Error_Timestamp_3 "Fehler Zeitstempel 3 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_timestamp_3" }
Number HeatPump_Error_Code_4 "Fehlergrund 4 [%d]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_4" }
DateTime HeatPump_Error_Timestamp_4 "Fehler Zeitstempel 4 [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]" (gHeatpump) { channel="luxtronik:heatpump:myheatpump:switchoff_error_timestamp_4" }
```

### Sitemap

```
sitemap default label="Main Screen" {
    Text item=HeatPump_Temperature_1 label="Außentemperatur"
    Text item=HeatPump_Temperature_4 label="Temperatur Vorlauf"
    Text item=HeatPump_State label="Status"
    Text item=HeatPump_State_Ext label="Status erweitert"
    Text item=HeatPump_State_Time label="Status seit"
    Switch item=HeatPump_heating_operation_mode  mappings=[0="Auto", 1="Zuheizer", 2="Party", 3="Ferien", 4="Aus"]
    Setpoint item=HeatPump_heating_temperature minValue=-10 maxValue=10 step=0.5
    Switch item=HeatPump_warmwater_operation_mode  mappings=[0="Auto", 1="Zuheizer", 2="Party", 3="Ferien", 4="Aus"]
    Setpoint item=HeatPump_warmwater_temperature minValue=10 maxValue=65 step=1
}
```
