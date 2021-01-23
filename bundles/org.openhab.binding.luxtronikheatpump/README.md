# LuxtronikHeatpump Binding

This binding gives the possibility to integrate any Heatpump that is based on the Luxtronik 2 contol unit of Alpha Innotec. This includes heatpumps of:

* Alpha InnoTec
* Buderus (Logamatic HMC20, HMC20 Z)
* CTA All-In-One (Aeroplus)
* Elco
* Nibe (AP-AW10)
* Roth (ThermoAura®, ThermoTerra)
* (Siemens) Novelan (WPR NET)
* Wolf Heiztechnik (BWL/BWS)

This binding was tested with:

* Siemens Novelan LD 7

_If you have another heatpump the binding works with, let us know, so we can extend the list_

Note: The whole functionality is based on data that was reverse engineered, so use it at your own risk. 

## Supported Things

This binding only supports one thing type "Luxtronik Heatpump" (heatpump).

## Thing Configuration

Each heatpump requires the following configuration parameters:

| parameter    | required | default | description |
|--------------|----------|---------|-------------|
| ipAddress    | yes      |         | IP address of the heatpump |
| port         | no       | 8889    | Port number to connect to. This should be `8889` for most heatpumps. For heatpumps using a firmware version before V1.73 port `8888` needs to be used. |
| refresh      | no       | 300     | Interval (in seconds) to refresh the channel values. |
| showAllChannels | no       | false    | Show all channels (even those determined as not supported) |

## Channels

As the Luxtronik 2 control is able to handle multiple heat pumps with different features (like heating, hot water, cooling, solar, photovoltaics, swimming pool,...), the binding has a lot channels. Depending on the heatpump it is used with, various channels might not hold any (useful) values. If `showAllChannels` is not activated for the thing, this binding will automatically try to hide channels that are not available for your heat pump. As this is done using reverse engineered parameters it might not be correct in all cases. If you miss a channel that should be available four your heat pump, you can enable `showAllChannels` for your thing, so all channels become available. Feel free to report such a case on the forum, so we can try to improve / fix that behavior.

The following channels are holding read only values:

| channel  | type   | advanced | description                  |
|----------|--------|----------|------------------------------|
| Temperatur_TVL | Number:Temperature |   | Flow temperature heating circuit | 
| Temperatur_TRL | Number:Temperature |   | Return temperature heating circuit | 
| Sollwert_TRL_HZ | Number:Temperature |   | Return setpoint heating circuit | 
| Temperatur_TRL_ext | Number:Temperature | x | Return temperature in buffer tank | 
| Temperatur_THG | Number:Temperature | x | Hot gas temperature | 
| Temperatur_TA | Number:Temperature |   | Outside temperature | 
| Mitteltemperatur | Number:Temperature |   | Average temperature outside over 24 h (heating limit function) | 
| Temperatur_TBW | Number:Temperature |   | Hot water actual temperature | 
| Einst_BWS_akt | Number:Temperature |   | Hot water target temperature | 
| Temperatur_TWE | Number:Temperature | x | Heat source inlet temperature | 
| Temperatur_TWA | Number:Temperature | x | Heat source outlet temperature | 
| Temperatur_TFB1 | Number:Temperature | x | Mixing circuit 1 Flow temperature | 
| Sollwert_TVL_MK1 | Number:Temperature | x | Mixing circuit 1 Flow target temperature | 
| Temperatur_RFV | Number:Temperature | x | Room temperature room station 1 | 
| Temperatur_TFB2 | Number:Temperature | x | Mixing circuit 2 Flow temperature | 
| Sollwert_TVL_MK2 | Number:Temperature | x | Mixing circuit 2 Flow target temperature | 
| Temperatur_TSK | Number:Temperature | x | Solar collector sensor | 
| Temperatur_TSS | Number:Temperature | x | Solar tank sensor | 
| Temperatur_TEE | Number:Temperature | x | Sensor external energy source | 
| ASDin | Switch | x | Input "Defrost end, brine pressure, flow rate" | 
| BWTin | Switch | x | Input "Domestic hot water thermostat" | 
| EVUin | Switch | x | Input "EVU lock" | 
| HDin | Switch | x | Input "High pressure refrigerant circuit | 
| MOTin | Switch | x | Input "Motor protection OK" | 
| NDin | Switch | x | Input "Low pressure" | 
| PEXin | Switch | x | Input "Monitoring contact for potentiostat" | 
| SWTin | Switch | x | Input "Swimming pool thermostat" | 
| AVout | Switch | x | Output "Defrost valve" | 
| BUPout | Switch | x | Output "Domestic hot water pump/changeover valve" | 
| HUPout | Switch | x | Output "Heating circulation pump" | 
| MA1out | Switch | x | Output "Mixing circuit 1 Up" | 
| MZ1out | Switch | x | Output "Mixing circuit 1 Closed" | 
| VENout | Switch | x | Output "Ventilation" | 
| VBOout | Switch | x | Output "Brine pump/fan" | 
| VD1out | Switch | x | Output "Compressor 1" | 
| VD2out | Switch | x | Output "Compressor 2" | 
| ZIPout | Switch | x | Output "Circulation pump" | 
| ZUPout | Switch | x | Output "Auxiliary circulation pump" | 
| ZW1out | Switch | x | Output "Control signal additional heating" | 
| ZW2SSTout | Switch | x | Output "Control signal additional heating/fault signal" | 
| ZW3SSTout | Switch | x | Output "Auxiliary heater 3" | 
| FP2out | Switch | x | Output "Pump mixing circuit 2" | 
| SLPout | Switch | x | Output "Solar charge pump" | 
| output_sup | Switch | x | Output "Swimming pool pump" | 
| MZ2out | Switch | x | Output "Mixing circuit 2 Closed" | 
| MA2out | Switch | x | Output "Mixing circuit 2 Up" | 
| Zaehler_BetrZeitVD1 | Number:Time |   | Operating hours compressor 1 | 
| Zaehler_BetrZeitImpVD1 | Number:Dimensionless |   | Pulses compressor 1 | 
| Zaehler_BetrZeitVD2 | Number:Time | x | Operating hours compressor 2 | 
| Zaehler_BetrZeitImpVD2 | Number:Dimensionless | x | Pulses compressor 2 | 
| Zaehler_BetrZeitZWE1 | Number:Time | x | Pulses Compressor operating hours Second heat generator 1 | 
| Zaehler_BetrZeitZWE2 | Number:Time | x | Pulses Compressor operating hours Second heat generator 2 | 
| Zaehler_BetrZeitZWE3 | Number:Time | x | Pulses Compressor operating hours Second heat generator 3 | 
| Zaehler_BetrZeitWP | Number:Time |   | Operating hours heat pump | 
| Zaehler_BetrZeitHz | Number:Time |   | Operating hours heating | 
| Zaehler_BetrZeitBW | Number:Time |   | Operating hours hot water | 
| Zaehler_BetrZeitKue | Number:Time |   | Operating hours cooling | 
| Time_WPein_akt | Number:Time | x | Heat pump running since | 
| Time_ZWE1_akt | Number:Time | x | Second heat generator 1 running since | 
| Time_ZWE2_akt | Number:Time | x | Second heat generator 2 running since | 
| Timer_EinschVerz | Number:Time | x | Mains on delay | 
| Time_SSPAUS_akt | Number:Time | x | Switching cycle lock off | 
| Time_SSPEIN_akt | Number:Time | x | Switching cycle lock on | 
| Time_VDStd_akt | Number:Time | x | Compressor Idle time | 
| Time_HRM_akt | Number:Time | x | Heating controller More time | 
| Time_HRW_akt | Number:Time | x | Heating controller Less time | 
| Time_LGS_akt | Number:Time | x | Thermal disinfection running since | 
| Time_SBW_akt | Number:Time | x | Hot water lock | 
| BIV_Stufe_akt | Number | x | Bivalence stage | 
| WP_BZ_akt | Number |   | Operating status | 
| ERROR_Time0 | DateTime |   | Timestamp error 0 in memory | 
| ERROR_Time1 | DateTime | x | Timestamp error 1 in memory | 
| ERROR_Time2 | DateTime | x | Timestamp error 2 in memory | 
| ERROR_Time3 | DateTime | x | Timestamp error 3 in memory | 
| ERROR_Time4 | DateTime | x | Timestamp error 4 in memory | 
| ERROR_Nr0 | Number |   | Error code Error 0 in memory | 
| ERROR_Nr1 | Number | x | Error code Error 1 in memory | 
| ERROR_Nr2 | Number | x | Error code Error 2 in memory | 
| ERROR_Nr3 | Number | x | Error code Error 3 in memory | 
| ERROR_Nr4 | Number | x | Error code Error 4 in memory | 
| AnzahlFehlerInSpeicher | Number | x | Number of errors in memory | 
| Switchoff_file_Nr0 | Number |   | Reason shutdown 0 in memory | 
| Switchoff_file_Nr1 | Number | x | Reason shutdown 1 in memory | 
| Switchoff_file_Nr2 | Number | x | Reason shutdown 2 in memory | 
| Switchoff_file_Nr3 | Number | x | Reason shutdown 3 in memory | 
| Switchoff_file_Nr4 | Number | x | Reason shutdown 4 in memory | 
| Switchoff_file_Time0 | DateTime |   | Timestamp shutdown 0 in memory | 
| Switchoff_file_Time1 | DateTime | x | Timestamp shutdown 1 in memory | 
| Switchoff_file_Time2 | DateTime | x | Timestamp shutdown 2 in memory | 
| Switchoff_file_Time3 | DateTime | x | Timestamp shutdown 3 in memory | 
| Switchoff_file_Time4 | DateTime | x | Timestamp shutdown 4 in memory | 
| Comfort_exists | Switch | x | Comfort board installed | 
| HauptMenuStatus | String |   | Status (complete) | 
| HauptMenuStatus_Zeile1 | Number |   | Status line 1 | 
| HauptMenuStatus_Zeile2 | Number | x | Status line 2 | 
| HauptMenuStatus_Zeile3 | Number | x | Status Zeile 3 | 
| HauptMenuStatus_Zeit | Number:Time | x | Status Time Line 2 | 
| HauptMenuAHP_Stufe | Number | x | Stage bakeout program | 
| HauptMenuAHP_Temp | Number:Temperature | x | Temperature bakeout program | 
| HauptMenuAHP_Zeit | Number:Time | x | Runtime bakeout program | 
| SH_BWW | Switch | x | DHW active/inactive icon | 
| SH_HZ | Number | x | Heater icon | 
| SH_MK1 | Number | x | Mixing circuit 1 icon | 
| SH_MK2 | Number | x | Mixing circuit 2 icon | 
| Einst_Kurzrpgramm | Number | x | Short program setting | 
| StatusSlave1 | Number | x | Status Slave 1 | 
| StatusSlave2 | Number | x | Status Slave 2 | 
| StatusSlave3 | Number | x | Status Slave 3 | 
| StatusSlave4 | Number | x | Status Slave 4 | 
| StatusSlave5 | Number | x | Status Slave 5 | 
| AktuelleTimeStamp | DateTime | x | Current time of the heat pump | 
| SH_MK3 | Number | x | Mixing circuit 3 icon | 
| Sollwert_TVL_MK3 | Number:Temperature | x | Mixing circuit 3 Flow set temperature | 
| Temperatur_TFB3 | Number:Temperature | x | Mixing circuit 3 Flow temperature | 
| MZ3out | Switch | x | Output "Mixing circuit 3 Closed" | 
| MA3out | Switch | x | Output "Mixing circuit 3 Up" | 
| FP3out | Switch | x | Pump mixing circuit 3 | 
| Time_AbtIn | Number:Time | x | Time until defrost | 
| Temperatur_RFV2 | Number:Temperature | x | Room temperature room station 2 | 
| Temperatur_RFV3 | Number:Temperature | x | Room temperature room station 3 | 
| SH_SW | Number | x | Time switch swimming pool icon | 
| Zaehler_BetrZeitSW | Number:Time | x | Swimming pool operating hours | 
| FreigabKuehl | Switch | x | Release cooling | 
| AnalogIn | Number:ElectricPotential | x | Analog input signal | 
| SH_ZIP | Number | x | Circulation pumps icon | 
| WMZ_Heizung | Number:Energy |   | Heat meter heating | 
| WMZ_Brauchwasser | Number:Energy |   | Heat meter domestic water | 
| WMZ_Schwimmbad | Number:Energy |   | Heat meter swimming pool | 
| WMZ_Seit | Number:Energy |   | Total heat meter | 
| WMZ_Durchfluss | Number:VolumetricFlowRate |   | Heat meter flow rate | 
| AnalogOut1 | Number:ElectricPotential | x | Analog output 1 | 
| AnalogOut2 | Number:ElectricPotential | x | Analog output 2 | 
| Time_Heissgas | Number:Time | x | Lock second compressor hot gas | 
| Temp_Lueftung_Zuluft | Number:Temperature | x | Supply air temperature | 
| Temp_Lueftung_Abluft | Number:Temperature | x | Exhaust air temperature | 
| Zaehler_BetrZeitSolar | Number:Time | x | Operating hours solar | 
| AnalogOut3 | Number:ElectricPotential | x | Analog output 3 | 
| AnalogOut4 | Number:ElectricPotential | x | Analog output 4 | 
| Out_VZU | Number:ElectricPotential | x | Supply air fan (defrost function) | 
| Out_VAB | Number:ElectricPotential | x | Exhaust fan | 
| Out_VSK | Switch | x | Output VSK | 
| OUT_FRH | Switch | x | Output FRH | 
| AnalogIn2 | Number:ElectricPotential | x | Analog input 2 | 
| AnalogIn3 | Number:ElectricPotential | x | Analog input 3 | 
| SAXin | Switch | x | Input SAX | 
| SPLin | Switch | x | Input SPL | 
| Compact_exists | Switch | x | Ventilation board installed | 
| Durchfluss_WQ | Number:VolumetricFlowRate | x | Flow rate heat source | 
| LIN_exists | Switch | x | LIN BUS installed | 
| LIN_ANSAUG_VERDAMPFER | Number:Temperature | x | Temperature suction evaporator | 
| LIN_ANSAUG_VERDICHTER | Number:Temperature | x | Temperature suction compressor | 
| LIN_VDH | Number:Temperature | x | Temperature compressor heating | 
| LIN_UH | Number:Temperature | x | Overheating | 
| LIN_UH_Soll | Number:Temperature | x | Overheating target | 
| LIN_HD | Number:Pressure | x | High pressure | 
| LIN_ND | Number:Pressure | x | Low pressure | 
| LIN_VDH_out | Switch | x | Output compressor heating | 
| HZIO_PWM | Number:Energy | x | Control signal circulating pump | 
| HZIO_VEN | Number | x | Fan speed | 
| HZIO_STB | Switch | x | Safety tempearture limiter floor heating | 
| SEC_Qh_Soll | Number:Energy | x | Power target value | 
| SEC_Qh_Ist | Number:Energy | x | Power actual value | 
| SEC_TVL_Soll | Number:Temperature | x | Temperature flow set point | 
| SEC_BZ | Number | x | SEC Board operating status | 
| SEC_VWV | Number | x | Four-way valve | 
| SEC_VD | Number | x | Compressor speed | 
| SEC_VerdEVI | Number:Temperature | x | Compressor temperature EVI (Enhanced Vapour Injection) | 
| SEC_AnsEVI | Number:Temperature | x | Intake temperature EVI | 
| SEC_UEH_EVI | Number:Temperature | x | Overheating EVI | 
| SEC_UEH_EVI_S | Number:Temperature | x | Overheating EVI target | 
| SEC_KondTemp | Number:Temperature | x | Condensation temperature | 
| SEC_FlussigEx | Number:Temperature | x | Liquid temperature EEV (electronic expansion valve) | 
| SEC_UK_EEV | Number:Temperature | x | Hypothermia EEV | 
| SEC_EVI_Druck | Number:Pressure | x | Pressure EVI | 
| SEC_U_Inv | Number:ElectricPotential | x | Voltage inverter | 
| Temperatur_THG_2 | Number:Temperature | x | Hot gas temperature sensor 2 | 
| Temperatur_TWE_2 | Number:Temperature | x | Temperature sensor heat source inlet 2 | 
| LIN_ANSAUG_VERDAMPFER_2 | Number:Temperature | x | Intake temperature evaporator 2 | 
| LIN_ANSAUG_VERDICHTER_2 | Number:Temperature | x | Intake temperature compressor 2 | 
| LIN_VDH_2 | Number:Temperature | x | Temperature compressor 2 heating | 
| LIN_UH_2 | Number:Temperature | x | Overheating 2 | 
| LIN_UH_Soll_2 | Number:Temperature | x | Overheating target 2 | 
| LIN_HD_2 | Number:Pressure | x | High pressure 2 | 
| LIN_ND_2 | Number:Pressure | x | Low pressure 2 | 
| HDin_2 | Switch | x | Input pressure switch high pressure 2 | 
| AVout_2 | Switch | x | Output defrost valve 2 | 
| VBOout_2 | Switch | x | Output brine pump/fan 2 | 
| VD1out_2 | Switch | x | Compressor output 1 / 2 | 
| LIN_VDH_out_2 | Switch | x | Compressor output heating 2 | 
| Switchoff2_file_Nr0 | Number | x | Reason shutdown 0 in memory | 
| Switchoff2_file_Nr1 | Number | x | Reason shutdown 1 in memory | 
| Switchoff2_file_Nr2 | Number | x | Reason shutdown 2 in memory | 
| Switchoff2_file_Nr3 | Number | x | Reason shutdown 3 in memory | 
| Switchoff2_file_Nr4 | Number | x | Reason shutdown 4 in memory | 
| Switchoff2_file_Time0 | DateTime | x | Timestamp shutdown 0 in memory | 
| Switchoff2_file_Time1 | DateTime | x | Timestamp shutdown 1 in memory | 
| Switchoff2_file_Time2 | DateTime | x | Timestamp shutdown 2 in memory | 
| Switchoff2_file_Time3 | DateTime | x | Timestamp shutdown 3 in memory | 
| Switchoff2_file_Time4 | DateTime | x | Timestamp shutdown 4 in memory | 
| RBE_RT_Ist | Number:Temperature | x | Room temperature actual value | 
| RBE_RT_Soll | Number:Temperature | x | Room temperature set point | 
| Temperatur_BW_oben | Number:Temperature | x | Temperature domestic water top | 
| Freq_VD | Number:Frequency | x | Compressor frequency | 

The following channels are also writable:
| channel  | type   | advanced | description                  |
|----------|--------|----------|------------------------------|
| Einst_WK_akt | Number:Temperature |   | Heating temperature (parallel shift) | 
| Einst_BWS_akt_2 | Number:Temperature |   | Hot water temperature | 
| Ba_Hz_akt | Number |   | Heating mode | 
| Ba_Bw_akt | Number |   | Hot water operating mode | 
| Einst_BwTDI_akt_MO | Switch |  x  | Thermal disinfection (Monday) |
| Einst_BwTDI_akt_DI | Switch |  x  | Thermal disinfection (Tuesday) |
| Einst_BwTDI_akt_MI | Switch |  x  | Thermal disinfection (Wednesday) |
| Einst_BwTDI_akt_DO | Switch |  x  | Thermal disinfection (Thursday) |
| Einst_BwTDI_akt_FR | Switch |  x  | Thermal disinfection (Friday) |
| Einst_BwTDI_akt_SA | Switch |  x  | Thermal disinfection (Saturday) |
| Einst_BwTDI_akt_SO | Switch |  x  | Thermal disinfection (Sunday) |
| Einst_BwTDI_akt_AL | Switch |  x  | Thermal disinfection (Permanent) |
| Einst_BWStyp_akt | Number |   | Comfort cooling mode | 
| Einst_KuCft1_akt | Number:Temperature |   | Comfort cooling AT release | 
| Sollwert_KuCft1_akt | Number:Temperature |   | Comfort cooling AT release target | 
| Einst_Kuhl_Zeit_Ein_akt | Number |   | AT Excess | 
| Einst_Kuhl_Zeit_Aus_akt | Number |   | AT undercut | 


## Example

Below you can find some example textual configuration for a heatpump with some basic functionallity. This can be extended/adjusted according to your needs and depending on the availability of channels (see list above).

_heatpump.things:_
```
Thing luxtronikheatpump:heatpump:heatpump "Heatpump" [
    ipAddress="192.168.178.12",
    port="8889",
    refresh="300"
]
```

_heatpump.items:_
```
Group    gHeatpump   "Heatpump"   <temperature>

Number:Temperature HeatPump_Temp_Outside   "Temperature outside [%.1f °C]"   <temperature> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:Temperatur_TA" }
Number:Temperature HeatPump_Temp_Outside_Avg     "Avg. temperature outside [%.1f °C]"  <temperature> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:Mitteltemperatur" }

Number:Time HeatPump_Hours_Heatpump  "Operating hours [%d h]"  <clock> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:Zaehler_BetrZeitWP" }
Number:Time HeatPump_Hours_Heating   "Operating hours heating [%d h]"  <clock> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:Zaehler_BetrZeitHz" }
Number:Time HeatPump_Hours_Warmwater "Operating hours hot water [%d h]" <clock> (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:Zaehler_BetrZeitBW" }

String HeatPump_State_Ext   "State [%s]"   (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:HauptMenuStatus" }

Number HeatPump_heating_operation_mode   "Heating operation mode [%s]"  (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:Ba_Hz_akt" }
Number HeatPump_heating_temperature   "Heating temperature [%.1f]"  (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:Einst_WK_akt" }
Number HeatPump_warmwater_operation_mode   "Hot water operation mode [%s]"  (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:Ba_Bw_akt" }
Number HeatPump_warmwater_temperature   "Hot water temperature [%.1f]"  (gHeatpump) { channel="luxtronikheatpump:heatpump:heatpump:Einst_BWS_akt" }
```

_heatpump.sitemap:_
```
sitemap heatpump label="Heatpump" {
    Frame label="Heatpump" {
        Text item=HeatPump_State_Ext
        Text item=HeatPump_Temperature_1
        Text item=HeatPump_Outside_Avg
        Text item=HeatPump_Hours_Heatpump
        Text item=HeatPump_Hours_Heating
        Text item=HeatPump_Hours_Warmwater
        Switch item=HeatPump_heating_operation_mode  mappings=[0="Auto", 1="Auxiliary heater", 2="Party", 3="Holiday", 4="Off"]
        Setpoint item=HeatPump_heating_temperature minValue=-10 maxValue=10 step=0.5
        Switch item=HeatPump_warmwater_operation_mode  mappings=[0="Auto", 1="Auxiliary heater", 2="Party", 3="Holiday", 4="Off"]
        Setpoint item=HeatPump_warmwater_temperature minValue=10 maxValue=65 step=1
    }
}
```

## Development Notes

This binding was initially based on the [Novelan/Luxtronik Heat Pump Binding](https://v2.openhab.org/addons/bindings/novelanheatpump1/) for OpenHAB 1.

Luxtronik control units have an internal webserver which serves an Java applet. This applet can be used to configure some parts of the heat pump. The applet itselves uses a socket connection to fetch and send data to the heatpump.
This socket is also used by this binding. To get some more information on how this socket works you can check out other Luxtronik tools like [Luxtronik2 for NodeJS](https://github.com/coolchip/luxtronik2).

A detailed parameter descriptions for the Java Webinterface can be found in the [Loxwiki](https://www.loxwiki.eu/display/LOX/Java+Webinterface)
