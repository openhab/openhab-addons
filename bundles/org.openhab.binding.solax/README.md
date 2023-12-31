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
| local-connect-inverter | Thing      | This is model representation of inverter with all the data available as a channels  |

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

| Channel                   | Type                       | Description                                                                                    |
|---------------------------|----------------------------|------------------------------------------------------------------------------------------------|
| battery-power             | Number:Power               | The power to / from battery (negative means power is pulled from battery and vice-versa) [W]   |
| battery-current           | Number:ElectricCurrent     | The current to / from battery (negative means power is pulled from battery and vice-versa) [A] |
| battery-voltage           | Number:ElectricPotential   | The voltage of the battery [V]                                                                 |
| battery-temperature       | Number:Temperature         | The temperature of the battery [C/F]                                                           |
| battery-state-of-charge   | Number                     | The state of charge of the battery [%]                                                         |

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

## Full Example

Here are some file based examples.

### Thing Configuration

```java
// The local connect inverter thing 
Thing solax:local-connect-inverter:localInverter  [ refreshInterval=10, password="<SERIAL NUMBER OF THE WIFI MODULE>", hostname="<local IP/hostname in the network>" ] 
```

### Item Configuration

```java
Group gSolaxInverter "Solax Inverter" <energy> (boilerRoom)
Group solarPanels "Solar panels" <energy> (gSolaxInverter)

Number solaxPowerWest "West [%.0f W]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:local-connect-inverter:localInverter:pv1-power" }
Number solaxPowerEast "East [%.0f W]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:local-connect-inverter:localInverter:pv2-power" }
Number solaxBatteryPower "Battery power [%.0f W]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:local-connect-inverter:localInverter:battery-power" }
Number solaxBatterySoc "Battery SoC [%.0f %%]" <batterylevel> (gsolax_inverter,EveryChangePersist) { channel="solax:local-connect-inverter:localInverter:battery-state-of-charge" }

Number solaxFeedInPower "Feed-in power (CEZ) [%.0f W]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:local-connect-inverter:localInverter:feed-in-power" }
Number solaxAcPower "Invertor output power [%.0f W]" <energy> (gsolax_inverter,EveryChangePersist){ channel="solax:local-connect-inverter:localInverter:inverter-output-power" }

String solaxInverterType "Inverter Type [%s]" <energy> (gsolax_inverter) { channel="solax:local-connect-inverter:localInverter:inverter-type"}
String solaxUploadTime "Last update time [%s]" <calendar> (gsolax_inverter) { channel="solax:local-connect-inverter:localInverter:last-update-time" }
String solaxRawData "Raw data [%s]" <data> (gsolax_inverter) { channel="solax:local-connect-inverter:localInverter:raw-data" }
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
    Text item=solaxBatterySoc valuecolor=[<=30="red", <50="orange", >=50="green"] {
        Switch item=Chart_Period label="Chart Period" mappings=[0="H", 1="D", 2="W", 3="M", 4="Y"]
        Text item=solaxBatterySoc valuecolor=[<=30="red", <50="orange", >=50="green"]
        Chart item=solaxBatterySoc period=h refresh=600 visibility=[Chart_Period==0]                    
        Chart item=solaxBatterySoc period=D refresh=3600 visibility=[Chart_Period==1]                   
        Chart item=solaxBatterySoc period=W refresh=3600 visibility=[Chart_Period==2, Chart_Period==Uninitialized]                  
        Chart item=solaxBatterySoc period=M refresh=3600 visibility=[Chart_Period==3]                   
        Chart item=solaxBatterySoc period=Y refresh=3600 visibility=[Chart_Period==4]       
    }
}
```


