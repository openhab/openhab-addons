# Solax Binding

This is a binding for Solax solar power inverters.

Solax Wi-Fi module with direct connection via HTTP is supported.
Wi-Fi module firmware version 3.x+ is required.
Please note that earlier firmware releases do not support direct connection, therefore the binding will not work in its current state.

The binding retrieves a structured data from the inverter's Wi-Fi module, parses it and pushes it into the inverter Thing where each channel represents a specific information (inverter output power, voltage, PV1 power, etc.)

In case the parsed information that comes with the binding out of the box differs, the raw data channel can be used with a combination of JSON Path transformation to map the proper values to the necessary items.

## Supported Things

| Thing                  | Thing Type | Description                                                                         |
|------------------------|------------|-------------------------------------------------------------------------------------|
| local-connect-inverter | Thing      | An inverter representation with all the data available as a channels (directly retrieved from the wi-fi module  |
| cloud-connect-inverter | Thing      | An inverter representation with all the data available as a channels (retrieved from the Solax cloud API)  |

Note: Channels may vary depending on the inverter type and the availability of information for parsing the raw data. 
If you're missing a channel this means that it's not supported for your inverter type.

## Thing Configuration

### Local Connect Inverter Configuration

| Parameter         | Description                                                                                                                                        |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| refreshInterval   | Defines the refresh interval when the binding polls from the inverter's Wi-Fi module (in seconds). Optional parameter. Default 10 seconds.         |
| password          | Password for accessing the Wi-Fi module (the serial number of the wifi). Mandatory parameter.                                                      |
| hostname          | IP address or hostname of your Wi-Fi module. If hostname is used must be resolvable by OpenHAB. Mandatory parameter.                               |

### Inverter Output Channels

| Channel                         | Type                       | Description                                                    |
|---------------------------------|----------------------------|----------------------------------------------------------------|
| inverter-output-power           | Number:Power               | The output power of the inverter [W]                           |
| inverter-current                | Number:ElectricCurrent     | The output current of the inverter [A]                         |
| inverter-voltage                | Number:ElectricPotential   | The output voltage of the inverter [V]                         |
| inverter-frequency              | Number:Frequency           | The frequency of the electricity of the inverter [Hz]          |
| inverter-output-power-phase1    | Number:Power               | The output power of phase 1 of the inverter [W]                |
| inverter-output-power-phase2    | Number:Power               | The output power of phase 2 of the inverter [W]                |
| inverter-output-power-phase3    | Number:Power               | The output power of phase 3 of the inverter [W]                |
| inverter-total-output-power     | Number:Power               | The total output power of all phases of the inverter [W]       |
| inverter-current-phase1         | Number:ElectricCurrent     | The output current of phase 1 of the inverter [A]              |
| inverter-current-phase2         | Number:ElectricCurrent     | The output current of phase 2 of the inverter [A]              |
| inverter-current-phase3         | Number:ElectricCurrent     | The output current of phase 3 of the inverter [A]              |
| inverter-voltage-phase1         | Number:ElectricPotential   | The output voltage of phase 1 of the inverter [V]              |
| inverter-voltage-phase2         | Number:ElectricPotential   | The output voltage of phase 2 of the inverter [V]              |
| inverter-voltage-phase3         | Number:ElectricPotential   | The output voltage of phase 3 of the inverter [V]              |
| inverter-frequency-phase1       | Number:Frequency           | The frequency of phase 1 of the inverter [Hz]                  |
| inverter-frequency-phase2       | Number:Frequency           | The frequency of phase 2 of the inverter [Hz]                  |
| inverter-frequency-phase3       | Number:Frequency           | The frequency of phase 3 of the inverter [Hz]                  |

### Photovoltaic Panels Production Channels

| Channel                  | Type                       | Description                                     |
|--------------------------|----------------------------|-------------------------------------------------|
| pv1-voltage              | Number:ElectricPotential   | The voltage of PV1 string [V]                   |
| pv2-voltage              | Number:ElectricPotential   | The voltage of PV2 string [V]                   |
| pv1-current              | Number:ElectricCurrent     | The current of PV1 string [A]                   |
| pv2-current              | Number:ElectricCurrent     | The current of PV2 string [A]                   |
| pv1-power                | Number:Power               | The output power PV1 string [W]                 |
| pv2-power                | Number:Power               | The output power PV2 string [W]                 |
| pv-total-power           | Number:Power               | The total output power of both PV strings [W]   |
| pv-total-current         | Number:ElectricCurrent     | The total current of both PV strings [A]        |

### Battery channels

| Channel                   | Type                       | Description                                                                                        |
|---------------------------|----------------------------|----------------------------------------------------------------------------------------------------|
| battery-power             | Number:Power               | The power to / from battery (negative means power is pulled from the battery and vice-versa) [W]   |
| battery-current           | Number:ElectricCurrent     | The current to / from battery (negative means power is pulled from the battery and vice-versa) [A] |
| battery-voltage           | Number:ElectricPotential   | The voltage of the battery [V]                                                                     |
| battery-temperature       | Number:Temperature         | The temperature of the battery [C/F]                                                               |
| battery-level             | Number                     | The state of charge of the battery [%]                                                             |

### Grid related channels

| Channel                  | Type                       | Description                                                                                    |
|--------------------------|----------------------------|------------------------------------------------------------------------------------------------|
| feed-in-power            | Number:Power               | The power to / from grid (negative means power is pulled from the grid and vice-versa) [W]     |

### General channels

| Channel                  | Type                       | Description                                                                                                                                 |
|--------------------------|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| last-update-time         | DateTime                   | Last time when a call has been made to the inverter                                                                                         |
| raw-data                 | String                     | The raw data retrieved from inverter in JSON format. (Usable for channels not implemented. Can be consumed with the JSONpath transformation |

### Statistics / Usage related Channels

| Channel                          | Type                       | Description                                               |
|----------------------------------|----------------------------|-----------------------------------------------------------|
| power-usage                      | Number:Power               | Current power usage / consumption of the building [W]     |
| total-energy                     | Number:Energy              | Total energy output from the inverter [kWh]               |
| total-battery-discharge-energy   | Number:Energy              | Total energy from the battery [kWh]                       |
| total-battery-charge-energy      | Number:Energy              | Total energy to the battery [kWh]                         |
| total-pv-energy                  | Number:Energy              | Total energy from the PV [kWh]                            |
| total-consumption                | Number:Energy              | Total energy consumed for the building [kWh]              |
| total-feed-in-energy             | Number:Energy              | Total energy consumed from the electricity provider [kWh] |
| today-energy                     | Number:Energy              | Energy output from the inverter for the day [kWh]         |
| today-battery-discharge-energy   | Number:Energy              | Total energy from the battery output for the day [kWh]    |
| today-battery-charge-energy      | Number:Energy              | Total energy charged to the battery for the day [kWh]     |
| today-feed-in-energy             | Number:Energy              | Total energy charged to the battery for the day [kWh]     |
| today-consumption                | Number:Energy              | Total energy consumed for the day [kWh]                   |

### Properties

| Property          | Description                               |
|-------------------|-------------------------------------------|
| serialNumber      | The serial number of the Wi-Fi module     |
| inverterType      | Inverter Type (for example X1_HYBRID_G4)  |


### Cloud Connect Inverter Configuration

| Parameter         | Description                                                                                                                                        |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| refreshInterval   | Defines the refresh interval when the binding polls from from the Solax cloud (in seconds). Optional parameter(min=15, max=600). Default is 30 seconds. Be advised that the cloud API is limited to max 10 calls per minute and 10000 calls per day.                                                                                                              |
| password          | The registration number, shown in the Solax Cloud web portal. Mandatory parameter.                                                                 |
| token             | Token for accessing the Solax Cloud API. Can be obtained via Service -> API on the Solax cloud web portal. Mandatory parameter.                    |

### Channels

| Channel                         | Type                       | Description                                                                                                                                 |
|---------------------------------|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| inverter-output-power           | Number:Power               | The output power of the inverter [W]                                                                                                        |
| pv1-power                       | Number:Power               | The output power PV1 string [W]                                                                                                             |
| pv2-power                       | Number:Power               | The output power PV2 string [W]                                                                                                             |
| pv3-power                       | Number:Power               | The output power PV3 string [W]                                                                                                             |
| pv4-power                       | Number:Power               | The output power PV4 string [W]                                                                                                             |
| pv-total-power                  | Number:Power               | The output power of all the photovoltaic strings [W]                                                                                        |
| battery-power                   | Number:Power               | The power to / from battery (negative means power is pulled from the battery and vice-versa) [W]                                            |
| battery-level                   | Number                     | The state of charge of the battery [%]                                                                                                      |
| feed-in-power                   | Number:Power               | The power to / from grid (negative means power is pulled from the grid and vice-versa) [W]                                                  |
| total-feed-in-energy            | Number:Energy              | Total energy consumed from the electricity provider [kWh]                                                                                   |
| total-consumption               | Number:Energy              | Total energy consumed for the building [kWh]                                                                                                |
| today-energy                    | Number:Energy              | Energy output from the inverter for the day [kWh]                                                                                           |
| total-energy                    | Number:Energy              | Total energy output from the inverter [kWh]                                                                                                 |
| raw-data                        | String                     | The raw data retrieved from inverter in JSON format. (Usable for channels not implemented. Can be consumed with the JSONpath transformation |
| inverter-status                 | String                     | The status of the inverter. (For the various status types, refer to the API documentation)                                                  |
| last-update-time                | DateTime                   | Last time when a call has been made to the inverter                                                                                         |
| inverter-meter2-power           | Number:Power               | Inverter power on meter2 [W]                                                                                                                |
| inverter-eps-power-r            | Number:Power               | Inverter AC EPS power R [W]                                                                                                                 |
| inverter-eps-power-s            | Number:Power               | Inverter AC EPS power S [W]                                                                                                                 |
| inverter-eps-power-t            | Number:Power               | Inverter AC EPS power T [W]                                                                                                                 |

## Full Example

Here are some file based examples.

### Thing Configuration

```java
// The local connect inverter thing 
Thing solax:local-connect-inverter:localInverter  [ refreshInterval=10, password="<SERIAL NUMBER OF THE WIFI MODULE>", hostname="<local IP/hostname in the network>" ] 
Thing solax:cloud-connect-inverter:cloudInverter  [ refresh=30, password="<REG_NUMBER>", token="<TOKEN>" ] 
```

### Item Configuration

```java
Group gSolaxInverter "Solax Inverter" <energy> (boilerRoom)
Group solarPanels "Solar panels" <energy> (gSolaxInverter)

// Direct connect
Number solaxPowerWest "West Power [%d W]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:local-connect-inverter:localInverter:pv1-power" }
Number solaxPowerEast "East Power [%d W]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:local-connect-inverter:localInverter:pv2-power" }
Number solaxGenerationTotal "Total generаtion now [%.0f W]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels) { channel="solax:local-connect-inverter:localInverter:pv-total-power" }
Number solaxVoltageWest "West Voltage [%.1f V]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:local-connect-inverter:localInverter:pv1-voltage" }
Number solaxVoltageEast "East Voltage [%.1f V]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:local-connect-inverter:localInverter:pv2-voltage" }
Number solaxCurrentWest "West Current [%.1f A]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:local-connect-inverter:localInverter:pv1-current" }
Number solaxCurrentEast "East Current [%.1f A]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:local-connect-inverter:localInverter:pv2-current" }
Number solaxBatteryPower "Battery power [%.0f W]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:local-connect-inverter:localInverter:battery-power" }
Number solaxBatterySoc "Battery SoC [%.0f %%]" <batterylevel> (gsolax_inverter,EveryChangePersist) { channel="solax:local-connect-inverter:localInverter:battery-level" }
Number solaxBatteryTemperature "Battery temperature [%d °C]" <temperature> (gsolax_inverter,EveryChangePersist) { channel="solax:local-connect-inverter:localInverter:battery-temperature" }
Number solaxBatteryCurrent "Battery current [%.1f A]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:local-connect-inverter:localInverter:battery-current" }
Number solaxBatteryVoltage "Battery voltage [%.1f V]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:local-connect-inverter:localInverter:battery-voltage" }

Number solaxFeedInPower "Feed-in power (CEZ) [%.0f W]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:local-connect-inverter:localInverter:feed-in-power" }
Number solaxCalculatedTotalFeedInPower "Calculated feed-in total power (CEZ) [%.0f KWh]" <energy> (gsolax_inverter,EveryChangePersist)
Number solaxCalculatedTotalFeedInPowerThisMonth "Calculated feed-in total power this month (CEZ) [%.0f KWh]" <energy> (gsolax_inverter,EveryChangePersist) 
Number solaxAcPower "Invertor output power [%.0f W]" <energy> (gsolax_inverter,EveryChangePersist){ channel="solax:local-connect-inverter:localInverter:inverter-output-power" }
Number solaxFrequency "Invertor frequency [%.2f Hz]" <energy> (gsolax_inverter,EveryChangePersist){ channel="solax:local-connect-inverter:localInverter:inverter-frequency" }
Number solaxVoltage "Invertor voltage [%.1f V]" <energy> (gsolax_inverter,EveryChangePersist){ channel="solax:local-connect-inverter:localInverter:inverter-voltage" }

String solaxLocalUploadTime "Local update time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1tS]" <calendar> (gsolax_inverter) { channel="solax:local-connect-inverter:localInverter:last-update-time" }
String solaxCloudUploadTime "Cloud update time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1tS]" <calendar> (gsolax_inverter) { channel="solax:cloud-connect-inverter:cloudInverter:last-update-time" }

String solaxLocalRawData "Local raw data [%s]" <data> (gsolax_inverter) { channel="solax:local-connect-inverter:localInverter:raw-data" }
String solaxCloudRawData "Cloud raw data [%s]" <data> (gsolax_inverter) { channel="solax:cloud-connect-inverter:cloudInverter:raw-data" }

// Cloud
Number solaxYieldToday "Yield today [%.0f kWh]" <energy> (gsolax_inverter){ channel="solax:cloud-connect-inverter:cloudInverter:today-energy" } 
Number solaxYieldTotal "Yield total [%.0f kWh]" <energy> (gsolax_inverter) { channel="solax:cloud-connect-inverter:cloudInverter:total-energy" }
Number solaxFeedInEnergy "Total Feed-in (CEZ) Power [%.0f kWh]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:cloud-connect-inverter:cloudInverter:total-feed-in-energy" }
String solaxInverterStatus "Inverter Status [%s]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:cloud-connect-inverter:cloudInverter:inverter-status" }
```

### Sitemap Configuration

```perl
Frame label="Solar power strings" {
    Text item=solaxPowerEast valuecolor=[<=30="gray",<=300="red", <1500="orange", >=1500="green"] {
        Text item=solaxPowerEast icon="energy" valuecolor=[<=30="gray",<=300="red", <1500="orange", >=1500="green"]
        Text item=solaxPowerWest icon="energy" valuecolor=[<=30="gray",<=300="red", <1500="orange", >=1500="green"]
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Chart item=solarPanels period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solarPanels period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solarPanels period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solarPanels period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solarPanels period=Y refresh=3600 visibility=[Chart_Period==4]
    }
    Text item=solaxPowerWest valuecolor=[<=30="gray",<=300="red", <1500="orange", >=1500="green"] {
        Text item=solaxPowerEast icon="energy" valuecolor=[<=30="gray",<=300="red", <1500="orange", >=1500="green"]
        Text item=solaxPowerWest icon="energy" valuecolor=[<=30="gray",<=300="red", <1500="orange", >=1500="green"]
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Chart item=solarPanels period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solarPanels period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solarPanels period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solarPanels period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solarPanels period=Y refresh=3600 visibility=[Chart_Period==4]
    }
    Text item=solaxVoltageEast valuecolor=[==0="gray",>0="green", >480="orange", >=500="red"]
    Text item=solaxVoltageWest valuecolor=[==0="gray",>0="green", >480="orange", >=500="red"]
    Text item=solaxCurrentEast valuecolor=[==0="gray",>0="green", >5="orange", >=10="red"]
    Text item=solaxCurrentWest valuecolor=[==0="gray",>0="green", >5="orange", >=10="red"]
    Text item=solaxGenerationTotal valuecolor=[<=100="gray",<=500="red", <2000="orange", >=2000="green"] {
        Text item=solaxPowerEast icon="energy" valuecolor=[<=30="gray",<=300="red", <1500="orange", >=1500="green"]
        Text item=solaxPowerWest icon="energy" valuecolor=[<=30="gray",<=300="red", <1500="orange", >=1500="green"]
        Text item=solaxGenerationTotal icon="energy" valuecolor=[<=30="gray",<=300="red", <1500="orange", >=1500="green"]
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Chart item=solarPanels period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solarPanels period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solarPanels period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solarPanels period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solarPanels period=Y refresh=3600 visibility=[Chart_Period==4]
    }
}
Frame label="Consumption" {
    Text item=solaxAcPower valuecolor=[<=30="gray", <800="green", <1500="orange", >=1500="red"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxAcPower icon="energy" valuecolor=[<=30="gray", <800="green", <1500="orange", >=1500="red"]
        Chart item=solaxAcPower period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxAcPower period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solaxAcPower period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxAcPower period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxAcPower period=Y refresh=3600 visibility=[Chart_Period==4]
    }
    Text item=solaxFeedInPower valuecolor=[<=30="gray", <800="green", <1500="orange", >=1500="red"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxFeedInPower icon="energy" valuecolor=[<=30="gray", <800="green", <1500="orange", >=1500="red"]
        Chart item=solaxFeedInPower period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxFeedInPower period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solaxFeedInPower period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxFeedInPower period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxFeedInPower period=Y refresh=3600 visibility=[Chart_Period==4]
    }
    Text item=solaxFrequency valuecolor=[<=30="gray", <800="green", <1500="orange", >=1500="red"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxFrequency icon="energy" valuecolor=[<=30="gray", <800="green", <1500="orange", >=1500="red"]
        Chart item=solaxFrequency period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxFrequency period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solaxFrequency period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxFrequency period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxFrequency period=Y refresh=3600 visibility=[Chart_Period==4]
    }
    Text item=solaxVoltage valuecolor=[<=30="gray", <800="green", <1500="orange", >=1500="red"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxVoltage icon="energy" valuecolor=[<=30="gray", <800="green", <1500="orange", >=1500="red"]
        Chart item=solaxVoltage period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxVoltage period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solaxVoltage period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxVoltage period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxVoltage period=Y refresh=3600 visibility=[Chart_Period==4]
    }
}
Frame label="Battery" {
    Text item=solaxBatteryPower valuecolor=[<=-500="red", <0="orange", ==0="gray", >0="green"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxBatteryPower icon="energy" valuecolor=[<-800="red", <0="orange", ==0="gray", >=0="green"]
        Chart item=solaxBatteryPower period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxBatteryPower period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solaxBatteryPower period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxBatteryPower period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxBatteryPower period=Y refresh=3600 visibility=[Chart_Period==4]
    }
    Text item=solaxBatteryCurrent valuecolor=[<=-5="red", <0="orange", ==0="gray", >0="green"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxBatteryCurrent icon="energy" valuecolor=[<-800="red", <0="orange", ==0="gray", >=0="green"]
        Chart item=solaxBatteryCurrent period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxBatteryCurrent period=D refresh=3600 visibility=[Chart_Period==1]			
        Chart item=solaxBatteryCurrent period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxBatteryCurrent period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxBatteryCurrent period=Y refresh=3600 visibility=[Chart_Period==4]
    }
    Text item=solaxBatteryVoltage valuecolor=[<=-500="red", <0="orange", ==0="gray", >0="green"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxBatteryVoltage icon="energy" valuecolor=[<-800="red", <0="orange", ==0="gray", >=0="green"]
        Chart item=solaxBatteryVoltage period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxBatteryVoltage period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solaxBatteryVoltage period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxBatteryVoltage period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxBatteryVoltage period=Y refresh=3600 visibility=[Chart_Period==4]
    }
    Text item=solaxBatterySoc valuecolor=[<=30="red", <50="orange", >=50="green"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxBatterySoc valuecolor=[<=30="red", <50="orange", >=50="green"]
        Chart item=solaxBatterySoc period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxBatterySoc period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solaxBatterySoc period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxBatterySoc period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxBatterySoc period=Y refresh=3600 visibility=[Chart_Period==4]
    }
    Text item=solaxBatteryTemperature valuecolor=[<=35="green", <45="orange", >=45="red"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxBatteryTemperature valuecolor=[<=25="green", <40="orange", >=40="red"]
        Chart item=solaxBatteryTemperature period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxBatteryTemperature period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solaxBatteryTemperature period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxBatteryTemperature period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxBatteryTemperature period=Y refresh=3600 visibility=[Chart_Period==4]
    }
}
Frame label="Statistics" {
    Text item=solaxYieldToday
    Text item=solaxYieldTotal
    Text item=solaxConsumeEnergy
    Text item=solaxFeedInEnergy
    Text item=solaxCalculatedTotalFeedInPower
    Text item=solaxCalculatedTotalFeedInPowerThisMonth valuecolor=[<200="green", >=200="orange", >=300="red"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxCalculatedTotalFeedInPowerThisMonth valuecolor=[<=30="red", <50="orange", >=50="green"]
        Chart item=solaxCalculatedTotalFeedInPowerThisMonth period=h refresh=600 visibility=[Chart_Period==0]
        Chart item=solaxCalculatedTotalFeedInPowerThisMonth period=D refresh=3600 visibility=[Chart_Period==1]
        Chart item=solaxCalculatedTotalFeedInPowerThisMonth period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]
        Chart item=solaxCalculatedTotalFeedInPowerThisMonth period=M refresh=3600 visibility=[Chart_Period==3]
        Chart item=solaxCalculatedTotalFeedInPowerThisMonth period=Y refresh=3600 visibility=[Chart_Period==4]
    }
}
Frame label="General" {
    Text item=solaxInverterStatus
    Text item=solaxLocalUploadTime
    Text item=solaxCloudUploadTime
}
Frame label="Raw data" {
    Text item=solaxLocalRawData
    Text item=solaxCloudRawData
}
```
