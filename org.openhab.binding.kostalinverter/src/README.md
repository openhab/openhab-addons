
# Kostal Inverter Binding

Scrapes the web interface of the inverter for the metrics of the supported channels below.

![Kostal Inverter Proven Generation](doc/kostalpico.jpg)
![Kostal Inverter New Generation](doc/kostalinverter8_5.jpg)

## Supported Things

Tested with Kostal Inverter Proven Generation and Kostal Inverter New Generation but might work with other inverters from Kostal too.


## Discovery

None

## Channels

```
Kostal Proven Generation:
acPower,
totalEnergy,
dayEnergy,
status

Kostal New Generation:
Channel_0 ++ Channel_22
```

## Thing Configuration

demo.things

```
This applies to Kostal Proven Generation
Thing kostalinverter:kostalinverter:inverter [ url="http://192.168.0.128",,,type="proven_generation"]

This applies to Kostal New Generation
kostalinverter:kostalinverter:inverter [ url="http://192.168.1.101", type="new_generation", dxsEntriesCfgFile="/home/'user'/Kostal_DxsEntries.cfg"]
```

If the thing goes online then the connection to the web interface is successful. In case
it is offline you should see an error message.

## Items

demo.items:

```
GrGroup gGF                       "gGF"                                       <energy>                    
Number SolarPower               "AC Power [%.2f W]"                         <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_0" }
Number SolarPower_Max           "Todays Maximum [%.2f W]"                   <energy> (gGF) 
Number SolarPower_Min           "Todays Mimimum [%.2f W]"                   <energy> (gGF) 
Number SolarPowerChart          "Chart Period Solar Power"
DateTime SolarPowerTimestamp    "Last Update AC Power [%1$ta %1$tR]"        <clock>

Number SolarEnergyDay           "Day Energy [%.2f Wh]"                     <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_1" }
Number SolarEnergyDay_Max       "Todays Maximum [%.2f Wh]"                 <energy> (gGF) 
Number SolarEnergyDay_Min       "Todays Mimimum [%.2f Wh]"                 <energy> (gGF) 
Number SolarEnergyDayChart      "Chart Period SolarEnergyDay "
DateTime SolarEnergyDayTimestamp    "Last Update Day Energy  [%1$ta %1$tR]"     <clock>

Number SolarTotalEnergy         "Total Energy [%.2f kWh]"                   <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_2" }
Number SolarTotalEnergy_Max     "Todays Maximum [%.2f kWh]"                 <energy> (gGF) 
Number SolarTotalEnergy_Min     "Todays Mimimum [%.2f kWh]"                 <energy> (gGF) 
Number SolarTotalEnergyChart    "Chart Period SolarTotalEnergy "
DateTime SolarTotalEnergyTimestamp    "Last Update Total Energy  [%1$ta %1$tR]" <clock>

Number SolarStatus              "Status[%s]"                                <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_3" }
Number SolarStatus_Max      "Todays Maximum [%s]"                       <energy> (gGF) 
Number SolarStatus_Min      "Todays Mimimum [%s]"                       <energy> (gGF) 
Number SolarStatusChart     "Chart Period SolarStatus "
DateTime SolarStatusTimestamp   "Last Update Solar Status  [%1$ta %1$tR]"       <clock>

Number Dc1Voltage               "DC1 Voltage[%.2f V]"                       <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_4" }
Number Dc1Current               "DC1 Current[%.2f A]"                       <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_5" }
Number Dc1Power                 "DC1 Power[%.2f W]"                         <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_6" }
Number Dc2Voltage               "DC2 Voltage[%.2f V]"                       <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_7" }
Number Dc2Current               "DC2 Current[%.2f A]"                       <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_8" }
Number Dc2Power                 "DC2 Power[%.2f W]"                         <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_9" }
Number Dc3Voltage               "DC3 Voltage[%.2f V]"                       <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_10" }
Number Dc3Current               "DC3 Current[%.2f A]"                       <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_11" }
Number Dc3Power                 "DC3 Power[%.2f W]"                         <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_12" }
Number GridVoltageL1            "L1 Voltage[%.2f V]"                        <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_13" }
Number GridCurrentL1            "L1 Current[%.2f A]"                        <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_14" }
Number GridPowerL1              "L1 Power[%.2f W]"                          <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_15" }
Number GridVoltageL2            "L2 Voltage[%.2f V]"                        <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_16" }
Number GridCurrentL2            "L2 Current[%.2f A]"                        <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_17" }
Number GridPowerL2              "L2 Power[%.2f W]"                          <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_18" }
Number GridVoltageL3            "L3 Voltage[%.2f V]"                        <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_19" }
Number GridCurrentL3            "L3 Current[%.2f A]"                        <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_20" }
Number GridPowerL3              "L3 Power[%.2f W]"                          <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_21" }
Number TotalOperatingTime       "TotalOperatingTime[%.2f W]"                <energy> (gGF) { channel="kostalinverter:kostalinverter:inverter:dxsEntries_22" }
```

```
##Configuration

Examples of configuration-file. This file (Kostal_DxsEntries.cfg) must be located regarded to demo.things

dxsEntries_0=67109120
# Channel 0=acPower

dxsEntries_1=251658753
# Channel 1=dayEnergy

dxsEntries_2=251658754
# Channel 2=totalEnergy

dxsEntries_3=16780032
# Channel 3=status

dxsEntries_4=33555202
# Channel 4=dc1Voltage

dxsEntries_5=33555201
# Channel 5=dc1Current

dxsEntries_6=33555203
# Channel 6=dc1Power

dxsEntries_7=33555458
# Channel 7=dc2Voltage

dxsEntries_8=33555457
# Channel 8=dc2Current

dxsEntries_9=33555459
# Channel 9=dc2Power

dxsEntries_10=33555714
# Channel 10=dc3Voltage

dxsEntries_11=33555713
# Channel 11=dc3Current

dxsEntries_12=33555715
# Channel 12=dc3Power

dxsEntries_13=67109378
# Channel 13=gridVoltageL1

dxsEntries_14=67109377
# Channel 14=gridCurrentL1

dxsEntries_15=67109379
# Channel 15=gridPowerL1

dxsEntries_16=67109634
# Channel 16=gridVoltageL2

dxsEntries_17=67109633
# Channel 17=gridCurrentL2

dxsEntries_18=67109635
# Channel 18=gridPowerL2

dxsEntries_19=67109890
# Channel 19=gridVoltageL3

dxsEntries_20=67109889
# Channel 20=gridCurrentL3

dxsEntries_21=67109891
# Channel 21=gridPowerL3

dxsEntries_22=251658496
# Channel 22=totalOperatingTime

```

