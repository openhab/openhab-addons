# Solax Binding

This is a binding for a Solax Wi-Fi module that supports connection directly via HTTP (Wi-Fi module firmware version 3.x+ is required).
Please note that earlier firmware releases do not support direct connection, therefore the binding will not work in it's current state.

Currently the Solax cloud services provide an update every 5 minutes (sometimes even more rarely) and they also happen to be down sometimes, which makes it hard to automate the decision making by openHAB if we have more complex rules.

The binding retrieves a structured data from the Wi-Fi module, parses it and pushes it into the inverter Thing where each channel represents a specific information (inverter output power, voltage, PV1 power, etc)

## Supported Things

| Thing             | Thing Type | Description                                                                                                                                 |
|-------------------|------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| localConnect      | Bridge     | The bridge is used to communicate directly with the Wi-Fi module of the inverter.                                                           |
| inverter          | Thing      | This is model representation of inverter with all the data available as a channels (Ex. inverter output power, voltage, PV1 power, etc)     |

## Discovery

Discovery is available. Once the localConnect bridge is configured with the necessary parameters, it can discover automatically the inverter with all it's channels as a separate thing.

## Thing Configuration

### Local Connect Bridge parameters

| Parameter         | Description                                                                                                                                        |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| refresh           | Value is in seconds. Defines the refresh interval when the binding polls from the inverter's Wi-Fi module. Optional parameter. Default 10 seconds. |
| password          | Password for accessing the Wi-Fi module (the serial number of the wifi). Mandatory parameter.                                                      |
| hostname          | IP address or hostname of your Wi-Fi module. If hostname is used must be resolvable by OpenHAB. Mandatory parameter.                               |

The bridge does not have any channels. It is used only to connect to the module and retrieve the data. The data is shown in the Inverter thing

### Inverter Thing Configuration

### Inverter Output Channels

| Channel                  | Type                       | Description                                      |
|--------------------------|----------------------------|--------------------------------------------------|
| inverterOutputPower      | system.electric-power      | The output power of the inverter [W]             |
| inverterCurrent          | system.electric-current    | The output current of the inverter [A]           |
| inverterVoltage          | system.electric-voltage    | The output voltage of the inverter [V]           |
| inverterFrequency        | Number:ElectricPotential   | The frequency of the output voltage [Hz]         |

### Photovoltaic Panels Production Channels

| Channel                  | Type                       | Description                            |
|--------------------------|----------------------------|----------------------------------------|
| pv1Voltage      | system.electric-voltage    | The voltage of PV1 string [V]                   |
| pv2Voltage      | system.electric-voltage    | The voltage of PV2 string [V]                   |
| pv1Current      | system.electric-current    | The current of PV1 string [A]                   |
| pv2Current      | system.electric-current    | The current of PV2 string [A]                   |
| pv1Power        | system.electric-power      | The output power PV1 string [W]                 |
| pv2Power        | system.electric-power      | The output power PV2 string [W]                 |
| pvTotalPower    | system.electric-power      | The total output power of both PV strings [W]   |
| pvTotalCurrent  | system.electric-current    | The total current of both PV strings [A]        |

### Battery channels

| Channel                  | Type                       | Description                                                                                    |
|--------------------------|----------------------------|------------------------------------------------------------------------------------------------|
| batteryPower             | system.electric-power      | The power to / from battery (negative means power is pulled from battery and vice-versa) [W]   |
| batteryCurrent           | system.electric-current    | The current to / from battery (negative means power is pulled from battery and vice-versa) [A] |
| batteryVoltage           | system.electric-voltage    | The voltage of the battery [V]                                                                 |
| batteryTemperature       | Number:Temperature         | The temperature of the battery [C/F]                                                           |
| batteryStateOfCharge     | system.battery-level       | The state of charge of the battery [%]                                                         |

### Grid related channels

| Channel                  | Type                       | Description                                                                                    |
|--------------------------|----------------------------|------------------------------------------------------------------------------------------------|
| feedInPower              | system.electric-power      | The power to / from grid (negative means power is pulled from the grid and vice-versa) [W]     |
| onGridTotalYield*        | system.electric-power      | Total Yield from PV strings [W]                                                                |
| onGridDailyYield*         | system.electric-power      | The power to / from grid (negative means power is pulled from the grid and vice-versa) [W]    |
| onTotalFeedinEnergy*      | system.electric-power      | The power to / from grid (negative means power is pulled from the grid and vice-versa) [W]    |
| onTotalFeedinEnergy*      | system.electric-power      | The power to / from grid (negative means power is pulled from the grid and vice-versa) [W]    |

_* experimental channels. So far the data does not seem reliable. Either it's taken from wrong part of the JSON or it's calculated wrong in the inverter itself. If someone finds better way to parse it or the meaning of the various int values, please open a new issue for solax binding and share the information._

### General channels

| Channel                  | Type                       | Description                                                                                                                                 |
|--------------------------|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| lastUpdateTime           | DateTime                   | Last time when a call has been made to the inverter                                                                                         |
| rawData                  | String                     | The raw data retrieved from inverter in JSON format. (Usable for channels not implemented. Can be consumed with the JSONpath transformation |

### Properties

| Property          | Description                               |
|-------------------|-------------------------------------------|
| serialWifi        | The Wi-Fi module's serial number          |
| inverterType      | Inverter Type (for example X1_HYBRID_G4)  |


## Full Example

Here are some file based examples.

### Thing Configuration

```java
// The local connect bridge
Bridge solax:localConnect:localBridge "Solax Inverter LocalBridge" [refresh=10, password="<SERIAL NUMBER OF THE WIFI>", hostname="<local IP/hostname in the network>"] {
    Thing inverter MyInverter "MyInverter via LocalBridge" [serialWifi="<SERIAL NUMBER OF THE WIFI>", inverterType="X1_HYBRID_G4"]
}
```

### Item Configuration

```java
Group gSolaxInverter "Solax Inverter" <energy> (boilerRoom)
Group solarPanels "Solar panels" <energy> (gSolaxInverter)
String solaxSerial "Serial Number [%s]" <energy> (gsolax_inverter) { channel="solax:inverter:localBridge:MyInverter:serialWifi" }

Number solaxPowerWest "West [%.0f W]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:inverter:localBridge:MyInverter:pv1Power" }
Number solaxPowerEast "East [%.0f W]" <solarplant> (gsolax_inverter,EveryChangePersist,solarPanels){ channel="solax:inverter:localBridge:MyInverter:pv2Power" }
Number solaxBatteryPower "Battery power [%.0f W]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:inverter:localBridge:MyInverter:batteryPower" }
Number solaxBatterySoc "Battery SoC [%.0f %%]" <batterylevel> (gsolax_inverter,EveryChangePersist) { channel="solax:inverter:localBridge:MyInverter:batteryStateOfCharge" }

Number solaxFeedInPower "Feed-in power (CEZ) [%.0f W]" <energy> (gsolax_inverter,EveryChangePersist) { channel="solax:inverter:localBridge:MyInverter:feedInPower" }
Number solaxAcPower "Invertor output power [%.0f W]" <energy> (gsolax_inverter,EveryChangePersist){ channel="solax:inverter:localBridge:MyInverter:inverterOutputPower" }

String solaxInverterType "Inverter Type [%s]" <energy> (gsolax_inverter) { channel="solax:inverter:localBridge:MyInverter:inverterType"}
String solaxUploadTime "Last update time [%s]" <calendar> (gsolax_inverter) { channel="solax:inverter:localBridge:MyInverter:lastUpdateTime" }
String solaxRawData "Raw data [%s]" <data> (gsolax_inverter) { channel="solax:inverter:localBridge:MyInverter:rawData" }
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


