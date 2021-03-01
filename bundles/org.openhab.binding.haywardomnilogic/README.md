# Hayward Omnilogic Binding

The Hayward Omnilogic binding integrates the Omnilogic pool controller using the Hayward API.

The Hayward Omnilogic API interacts with Hayward's cloud server requiring a connection with the Internet for sending and receiving information.

## Supported Things

The table below lists the Hayward OmniLogic binding thing types:

| Things                       | Description                                                                     | Thing Type    |
|------------------------------|---------------------------------------------------------------------------------|---------------|
| Hayward OmniLogix Connection | Connection to Hayward's Server                                                  | bridge        |
| Backyard                     | Backyard                                                                        | backyard      |
| Body of Water                | Body of Water                                                                   | bow           |
| Chlorinator                  | Chlorinator                                                                     | chlorinator   |
| Colorlogic Light             | Colorlogic Light                                                                | colorlogic    |
| Filter                       | Filter control                                                                  | filter        |
| Heater Equipment             | Actual heater (i.e. gas, solar, electric)                                       | heater        |
| Pump                         | Auxillary pump control (i.e. spillover)                                         | pump          |
| Relay                        | Accessory relay control (deck jet sprinklers, lights, etc.)                     | relay         |
| Virtaul Heater               | A Virtual Heater that can control all of the heater equipment based on priority | virtualHeater |

## Discovery

The binding will automatically discover the Omnilogic pool things from the cloud server using your Hayward Omnilogic credentials.

## Thing Configuration

Hayward OmniLogic Connection Parameters:

| Property             | Default                                                        | Required | Description                                  |
|----------------------|----------------------------------------------------------------|----------|----------------------------------------------|
| Host Name            | https://app1.haywardomnilogic.com/HAAPI/HomeAutomation/API.ash | Yes      | Host name of the Hayward API server          |
| User Name            | None                                                           | Yes      | Your Hayward User Name (not email address)   |
| Password             | None                                                           | Yes      | Your Hayward User Password                   |
| Telemetry Poll Delay | 12                                                             | Yes      | Telemetry Poll Delay (10-60 seconds)         |
| Alarm Poll Delay     | 60                                                             | Yes      | Alarm Poll Delay (0-120 seconds, 0 disabled) |

## Channels

### Backyard Channels

| backyardAirTemp | Number:Temperature | Backyard air temp sensor reading | R |
|-----------------|--------------------|----------------------------------|:-:|
| backyardStatus  | String             | Backyard status                  | R |
| backyardState   | String             | Backyard state                   | R |
| backyardAlarm1  | String             | Backyard alarm #1                | R |
| backyardAlarm2  | String             | Backyard alarm #2                | R |
| backyardAlarm3  | String             | Backyard alarm #3                | R |
| backyardAlarm4  | String             | Backyard alarm #4                | R |
| backyardAlarm5  | String             | Backyard alarm #5                | R |

### Body of Water Channels

| Channel Type ID | Item Type          | Description                        | Read Write |
|-----------------|--------------------|------------------------------------|:----------:|
| bowFlow         | Switch             | Body of Water flow sensor feedback |      R     |
| bowWaterTemp    | Number:Temperature | Body of Water temperature          |      R     |

### Chlorinator Channels

| Channel Type ID       | Item Type            | Description                                              | Read Write |
|-----------------------|----------------------|----------------------------------------------------------|:----------:|
| chlorEnable           | Switch               | Chlorinator enable                                       |     R/W    |
| chlorOperatingMode    | String               | Chlorinator operating mode                               |      R     |
| chlorTimedPercent     | Number:Dimensionless | Chlorinator timed percent                                |     R/W    |
| chlorOperatingState   | Number               | Chlorinator operating state                              |      R     |
| chlorScMode           | String               | Chlorinator super chlorinate mode                        |      R     |
| chlorError            | Number               | Chlorinator error                                        |      R     |
| chlorAlert            | String               | Chlorinator alert                                        |      R     |
| chlorAvgSaltLevel     | Number:Dimensionless | Chlorinator average salt level in Part per Million (ppm) |      R     |
| chlorInstantSaltLevel | Number:Dimensionless | Chlorinator instant salt level in Part per Million (ppm) |      R     |
| chlorStatus           | Number               | Chlorinator K1/K2 relay status                           |      R     |

### Colorlogic Light Channels

| Channel Type ID            | Item Type | Description                   | Read Write |
|----------------------------|-----------|-------------------------------|:----------:|
| colorLogicLightEnable      | Switch    | Colorlogic Light enable       |     R/W    |
| colorLogicLightState       | String    | Colorlogic Light state        |      R     |
| colorLogicLightCurrentShow | String    | Colorlogic Light current show |     R/W    |

### Filter Channels

| Channel Type ID     | Item Type            | Description            | Read Write |
|---------------------|----------------------|------------------------|:----------:|
| filterEnable        | Switch               | Filter enable          |     R/W    |
| filterValvePosition | String               | Filter valve position  |      R     |
| filterSpeed         | Number:Dimensionless | Filter speed in %      |     R/W    |
| filterState         | String               | Filter state           |      R     |
| filterLastSpeed     | Number:Dimensionless | Filter last speed in % |      R     |

### Heater Channels

| Channel Type ID | Item Type | Description   | Read Write |
|-----------------|-----------|---------------|:----------:|
| heaterState     | Number    | Heater state  |      R     |
| heaterEnable    | Switch    | Heater enable |      R     |

### Pump Channels

| Channel Type ID | Item Type            | Description     | Read Write |
|-----------------|----------------------|-----------------|:----------:|
| pumpEnable      | Switch               | Pump enable     |      R     |
| pumpSpeed       | Number:Dimensionless | Pump speed in % |      R     |

### Relay Channels

| Channel Type ID | Item Type | Description | Read Write |
|-----------------|-----------|-------------|:----------:|
| relayState      | Switch    | Relay state |     R/W    |

### Virtual Heater Channels

| Channel Type ID       | Item Type          | Description             | Read Write |
|-----------------------|--------------------|-------------------------|:----------:|
| heaterEnable          | Number             | Heater enable           |      R     |
| heaterCurrentSetpoint | Number:Temperature | Heater Current Setpoint |     R/W    |

## Full Example

After installing the binding, you will need to manually add the Hayward Connection thing and enter your credentials.
All pool items can be autmatically discovered by scanning the bridge
Goto the inbox and add the things.

### demo.items:

```text
Group gPool "Pool" ["Location"]

Group gHaywardChlorinator "Hayward Chlorinator" (gPool) ["Equipment"] 
Switch               HaywardChlorinator_Power            "Power"                (gHaywardChlorinator) ["Point"]  { channel="haywardomnilogic:chlorinator:3766402f00:34:chlorEnable" }           
String               HaywardChlorinator_OperatingMode    "Operating Mode"       (gHaywardChlorinator) ["Point"]  { channel="haywardomnilogic:chlorinator:3766402f00:34:chlorOperatingMode" }    
Number:Dimensionless HaywardChlorinator_SaltOutput       "Salt Output (%)"      (gHaywardChlorinator) ["Point"]  { channel="haywardomnilogic:chlorinator:3766402f00:34:chlorTimedPercent" }     
String               HaywardChlorinator_scMode           "scMode"               (gHaywardChlorinator) ["Point"]  { channel="haywardomnilogic:chlorinator:3766402f00:34:chlorScMode" }           
Number               HaywardChlorinator_ChlorinatorError "Chlorinator Error"    (gHaywardChlorinator) ["Point"]  { channel="haywardomnilogic:chlorinator:3766402f00:34:chlorError" }            
String               HaywardChlorinator_ChlorinatorAlert "Chlorinator Alert"    (gHaywardChlorinator) ["Point"]  { channel="haywardomnilogic:chlorinator:3766402f00:34:chlorAlert" }            
Number:Dimensionless HaywardChlorinator_AverageSaltLevel "Average Salt Level"   (gHaywardChlorinator) ["Point"]  { channel="haywardomnilogic:chlorinator:3766402f00:34:chlorAvgSaltLevel" }     
Number:Dimensionless HaywardChlorinator_InstantSaltLevel "Instant Salt Level"   (gHaywardChlorinator) ["Point"]  { channel="haywardomnilogic:chlorinator:3766402f00:34:chlorInstantSaltLevel" } 
Number               HaywardChlorinator_Status           "Status"               (gHaywardChlorinator) ["Point"]  { channel="haywardomnilogic:chlorinator:3766402f00:34:chlorStatus" }           


Group gHaywardBackyard "Hayward Backyard" (gPool) ["Equipment"]
Number:Temperature  HaywardBackyard_AirTemp        "Air Temp"                   (gHaywardBackyard) ["Point"]  { channel="haywardomnilogic:backyard:3766402f00:35940:backyardAirTemp" } 
String              HaywardBackyard_Status         "Status"                     (gHaywardBackyard) ["Point"]  { channel="haywardomnilogic:backyard:3766402f00:35940:backyardStatus" }  
String              HaywardBackyard_State          "State"                      (gHaywardBackyard) ["Point"]  { channel="haywardomnilogic:backyard:3766402f00:35940:backyardState" }   
String              HaywardBackyard_BackyardAlarm1 "Alarm"                      (gHaywardBackyard) ["Point"]  { channel="haywardomnilogic:backyard:3766402f00:35940:backyardAlarm1" }  
String              HaywardBackyard_BackyardAlarm2 "Alarm"                      (gHaywardBackyard) ["Point"]  { channel="haywardomnilogic:backyard:3766402f00:35940:backyardAlarm2" }  
String              HaywardBackyard_BackyardAlarm3 "Alarm"                      (gHaywardBackyard) ["Point"]  { channel="haywardomnilogic:backyard:3766402f00:35940:backyardAlarm3" }  
String              HaywardBackyard_BackyardAlarm4 "Alarm"                      (gHaywardBackyard) ["Point"]  { channel="haywardomnilogic:backyard:3766402f00:35940:backyardAlarm4" }  
String              HaywardBackyard_BackyardAlarm5 "Alarm"                      (gHaywardBackyard) ["Point"]  { channel="haywardomnilogic:backyard:3766402f00:35940:backyardAlarm5" }  

Group gHaywardGas "Hayward Gas" (gPool) ["Equipment"]
Number              HaywardGas_HeaterState  "Heater State"                      (gHaywardGas) ["Point"]  { channel="haywardomnilogic:heater:3766402f00:33:heaterState" }  
Switch              HaywardGas_HeaterEnable "Heater Enable"                     (gHaywardGas) ["Point"]  { channel="haywardomnilogic:heater:3766402f00:33:heaterEnable" } 

Group gHaywardJets "Hayward Jets" (gPool) ["Equipment"]
Switch              HaywardJets_Power "Power"                                   (gHaywardJets) ["Point"]  { channel="haywardomnilogic:relay:3766402f00:37:relayState" } 

Group gHaywardPool "Hayward Pool" (gPool) ["Equipment"]
Switch              HaywardPool_FlowSensor "Flow Sensor"                        (gHaywardPool) ["Point"]  { channel="haywardomnilogic:bow:3766402f00:30:bowFlow" }      
Number:Temperature  HaywardPool_WaterTemp  "Water Temp"                         (gHaywardPool) ["Point"]  { channel="haywardomnilogic:bow:3766402f00:30:bowWaterTemp" } 

Group gHaywardPoolLight "Hayward Pool Light" (gPool) ["Equipment"]
Switch              HaywardPoolLight_Power       "Power"                        (gHaywardPoolLight) ["Point"]  { channel="haywardomnilogic:colorlogic:3766402f00:38:colorLogicLightEnable" }      
String              HaywardPoolLight_LightState  "Light State"                  (gHaywardPoolLight) ["Point"]  { channel="haywardomnilogic:colorlogic:3766402f00:38:colorLogicLightState" }       
String              HaywardPoolLight_CurrentShow "Current Show"                 (gHaywardPoolLight) ["Point"]  { channel="haywardomnilogic:colorlogic:3766402f00:38:colorLogicLightCurrentShow" } 

```

