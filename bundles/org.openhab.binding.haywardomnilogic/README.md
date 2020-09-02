# Hayward Omnilogic Binding

The Hayward Omnilogic binding integrates the Omnilogic pool controller using the Hayward API.

The Hayward Omnilogic API interacts with Hayward's cloud server requiring a connection with the Internet for sending and receiving information.

## Supported Things

The table below lists the Hayward OmniLogic binding thing types:

| Things                                  | Description                                                                                                  | Thing Type     |
|-----------------------------------------|--------------------------------------------------------------------------------------------------------------|----------------|
| Hayward OmniLogix Connection            | Connection to Hayward's Server                                                                               | bridge         |
| Backyard                                | Backyard                                                                                                     | backyard       |
| Body of Water                           | Body of Water                                                                                                | bow            |
| Chlorinator                             | Chlorinator																									 | chlorinator    |
| Colorlogic Light                        | Colorlogic Light                                                                                             | colorlogic     |
| Chlorine Sense and Dispense             | CSAD/ORP Control (under development)                                                                         | csad           |
| Filter                                  | Filter control                                                                                               | filter         |
| Heater Equipment                        | Actual heater (i.e. gas, solar, electric)                                                                    | heater         |
| Pump                                    | Auxillary pump control (i.e. spillover)                                                                      | pump           |
| Relay                                   | Accessory relay control (deck jet sprinklers, lights, etc.)                                                  | relay          |
| Virtaul Heater                          | A Virtual Heater that can control all of the heater equipment based on priority                              | virtualHeater  |

## Discovery

The binding will automatically discover the Omnilogic pool things from the cloud server using your Hayward Omnilogic credentials.

## Thing Configuration

Hayward OmniLogic Connection Parameters:

| Property			    | Default															| Required 	| Description 								|
|-----------------------|-------------------------------------------------------------------|-----------|:-----------------------------------------:|
| Host Name      		| https://app1.haywardomnilogic.com/HAAPI/HomeAutomation/API.ashx	| Yes		| Host name of the Hayward API server 		|
| User Name       		| None    															| Yes		| Your Hayward User Name (not email address)|
| Password 				| None																| Yes		| Your Hayward User Password				|
| Telemetry Poll Delay  | 12    															| Yes		| Telemetry Poll Delay (seconds)			|
| Alarm Poll Delay    	| 60    															| Yes		| Alarm Poll Delay (seconds)				|
| Command Poll Delay	| 12    															| Yes		| Command Poll Delay (seconds)				|

## Channels

### Backyard Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| backyardAirTemp       		| Number    | Backyard air temp sensor reading											|      R     |
| backyardStatus       			| String    | Backyard status															|      R     |
| backyardState 				| String	| Backyard state								                            |      R     |
| backyardAlarm1        		| String    | Backyard alarm #1															|      R     |
| backyardAlarm2    			| String    | Backyard alarm #2															|      R     |
| backyardAlarm3     			| String    | Backyard alarm #3															|      R     |
| backyardAlarm4        		| String    | Backyard alarm #4															|      R     |
| backyardAlarm5        		| String    | Backyard alarm #5															|      R     |


### Body of Water Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| bowFlow       				| Number    | Body of Water flow sensor feedback										|      R     |
| bowWaterTemp       			| Number    | Body of Water temperature  												|      R     |


### Chlorinator Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| chlorEnable       			| Switch    | Chlorinator enable														|     R/W    |
| chlorOperatingMode    		| String    | Chlorinator operating mode												|      R     |
| chlorTimedPercent 			| Number	| Chlorinator timed percent					                                |     R/W    |
| chlorOperatingState   		| Number    | Chlorinator operating state												|      R     |
| chlorScMode    				| String    | Chlorinator mode															|      R     |
| chlorError     				| Number    | Chlorinator error															|      R     |
| chlorAlert        			| String    | Chlorinator alert															|      R     |
| chlorAvgSaltLevel     		| Number    | Chlorinator average salt level in Part per Million (ppm)					|      R     |
| chlorInstantSaltLevel 		| Number    | Chlorinator instant salt level in Part per Million (ppm)    				|      R     |
| chlorStatus     				| Number    | Chlorinator K1/K2 relay status      									    |      R     |
		
### Colorlogic Light Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| colorLogicLightEnable       	| Switch    | Colorlogic Light enable													|     R/W    |
| colorLogicLightState    		| Number    | Colorlogic Light state													|      R     |
| colorLogicLightCurrentShow 	| Number	| Colorlogic Light current show					                            |     R/W    |
| colorLogicLightSpeed   		| Number    | Colorlogic Light speed													|      R     |
| colorLogicLightBrightness		| Number    | Colorlogic Light brightness												|      R     |

### Chlorine Sense and Dispense (CSAD/ORP) Channels

To be developed

### Filter Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| filterEnable	    			| Switch    | Filter enable																|     R/W    |
| filterValvePosition    		| Number    | Filter valve position														|      R     |
| filterSpeed 					| Number	| Filter speed								                                |     R/W    |
| filterState   				| Number    | Filter state																|      R     |
| filterLastSpeed       		| Number    | Filter last speed															|      R     |

### Heater Channels

| Channel Type ID               | Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| heaterState                   | Number    | Heater state                                                              |      R     |
| heaterTemp                    | Number    | Heater temperature                                                        |      R     |
| heaterEnable                  | Number    | Heater enable                                                             |      R     |
| heaterPriority                | Number    | Heater priority                                                           |      R     |
| heaterMaintainFor             | Number    | Heater maintain for                                                       |      R     |

### Pump Channels

| Channel Type ID               | Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| pumpEnable                    | Switch    | Pump enable                                                               |      R     |
| pumpSpeed                     | Number    | Pump speed                                                                |      R     |

### Relay Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| relayState	    			| Switch    | Relay state																|     R/W    |

### Virtual Heater Channels

| Channel Type ID               | Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| heaterState                   | Number    | Heater state                                                              |      R     |
| heaterEnable                  | Number    | Heater enable                                                             |      R     |


## Full Example

After installing the binding, you will need to manually add the Hayward Connection thing and enter your credentials.  All pool items will then be automatically discovered.
Goto the inbox and add the things.

### demo.items:

```
Number   PoolBackyardAirTemp "Air Temp [%1.0f °F]"                              { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAirTemp"}
String   PoolBackyardStatus "Status [%s]"                                       { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardStatus"}
String   PoolBackyardState "State [%s]"                                         { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardState"}
String   PoolBackyardAlarm1 "Alarm #1 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm1"}
String   PoolBackyardAlarm2 "Alarm #2 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm2"}
String   PoolBackyardAlarm3 "Alarm #3 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm3"}
String   PoolBackyardAlarm4 "Alarm #4 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm4"}
String   PoolBackyardAlarm5 "Alarm #5 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm5"}
  
Number   PoolBOWFlow "Flow Switch"                      						{ channel = "haywardomnilogic:bow:2ee76053:30:bowFlow"}
Number   PoolBOWWaterTemp "Water Temp [%1.0f °F]"                               { channel = "haywardomnilogic:bow:2ee76053:30:bowWaterTemp"}
 
Switch   PoolChlorEnable "Enable"                                               { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorEnable"}
String   PoolChlorOperatingMode "Operating Mode [%s]"                           { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorOperatingMode"}
Number   PoolChlorSaltOutput "Salt Output [%1.0f %%]"           				{ channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorTimedPercent"}
String   PoolChlorOperatingState "Operating State [%s]"                         { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorOperatingState"}
String   PoolChlorScMode "SC Mode [%s]"                                         { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorScMode"}
Number   PoolChlorError "Error [%s]"                                            { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorError"}
String   PoolChlorAlert "Alert"             									{ channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorAlert"}
Number   PoolChlorAverageSalt "Average Salt[%1.0f ppm]"                         { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorAvgSaltLevel"}
Number   PoolChlorInstantSalt "Instant Salt [%1.0f ppm]"                        { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorInstantSaltLevel"}
Number   PoolChlorStatus "K1/K2 Relay Status"                                   { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorStatus"}

Number   PoolHeaterState "State [%s]"                                           { channel = "haywardomnilogic:heater:2ee76053:33:heaterState"}
Number   PoolHeaterEnable "Power [%s]"                                          { channel = "haywardomnilogic:heater:2ee76053:33:heaterEnable"}

Switch   PoolLightEnable "Enable"                                               { channel = "haywardomnilogic:colorlogic:2ee76053:38:colorLogicLightEnable"}
String   PoolLightState "State"                   								{ channel = "haywardomnilogic:colorlogic:2ee76053:38:colorLogicLightState"}
String   PoolLightCurrentShow "Show"                                            { channel = "haywardomnilogic:colorlogic:2ee76053:38:colorLogicLightCurrentShow"}

Switch   PoolFilterEnable "Power"                                               { channel = "haywardomnilogic:filter:2ee76053:31:filterPowerSwitch"}
Number   PoolFilterValve "Valve Position [%s]"                                  { channel = "haywardomnilogic:filter:2ee76053:31:filterValvePosition"}
Number   PoolFilterSpeed "Speed[%1.0f %%]"                                      { channel = "haywardomnilogic:filter:2ee76053:31:filterSpeed"}
Number   PoolFilterState "State"                 								{ channel = "haywardomnilogic:filter:2ee76053:31:filterState"}
Number   PoolFilterLastSpeed "Speed[%1.0f %%]"                                  { channel = "haywardomnilogic:filter:2ee76053:31:filterLastSpeed"}

Switch   PoolVirtualHeaterEnable "Power"                                        { channel = "haywardomnilogic:virtualHeater:2ee76053:32:virtualHeaterEnable"}
Number   PoolVirtualHeaterTemp "Temp [%1.0f °F]"                                { channel = "haywardomnilogic:virtualHeater:2ee76053:32:virtualHeaterCurrentSetpoint"}
  
Switch   PoolRelay1 "Deck Jets"                                                 { channel = "haywardomnilogic:relay:2ee76053:37:relayState"}
Switch   PoolRelay2 "Vacuum"                                                    { channel = "haywardomnilogic:relay:2ee76053:36:relayState"}
```


### demo.sitemap:

```
sitemap demo label="Demo Sitemap" {

            Frame label="Backyard" 
                {
                Text item=PoolBackyardAirTemp icon="temperature"     
                Text item=PoolBackyardStatus                               
                Text item=PoolBackyardState 
                Text item=PoolBackyardAlarm1 label="Alarm 1" visibility=[PoolBackyardAlarm1!=""]  icon="alarm"                        
                Text item=PoolBackyardAlarm2 label="Alarm 2" visibility=[PoolBackyardAlarm2!=""]  icon="alarm"    
                Text item=PoolBackyardAlarm3 label="Alarm 3" visibility=[PoolBackyardAlarm3!=""]  icon="alarm"     
                Text item=PoolBackyardAlarm4 label="Alarm 4" visibility=[PoolBackyardAlarm4!=""]  icon="alarm"      
                Text item=PoolBackyardAlarm5 label="Alarm 5" visibility=[PoolBackyardAlarm5!=""]  icon="alarm"   
                }
                
            Frame label="Pool"     
                {
                Text item=PoolBOWFlow     
                Text item=PoolBOWWaterTemp icon="temperature" 
                }
                                                                               
            Frame label="Pump"  
                {     
                Switch item=PoolFilterEnable 
                Text item=PoolFilterValve 
                Setpoint item=PoolFilterSpeed minValue=0 maxValue=100 step=5
                Text item=PoolFilterState 
				Text item=PoolFilterLastSpeed
                }
 
             Frame label="Heater"  
                {
                Text item=PoolHeaterState
                Text item=PoolHeaterTemp 
                }

            Frame label="Virtual Heater"    
                {
                Switch item=PoolVirtualHeaterEnable
                Setpoint item=PoolVirtualHeaterTemp                           
                }
                                
            Frame label="Chlorinator"   
                {
                Switch item=PoolChlorEnable
                Text item=PoolChlorOperatingMode
                Setpoint item=PoolChlorSaltOutput
                Text item=PoolChlorOperatingState
                Text item=PoolChlorScMode
                Text item=PoolChlorError
                Text item=PoolChlorAlert
                Text item=PoolChlorAverageSalt
                Text item=PoolChlorInstantSalt
                Text item=PoolChlorStatus                             
                }
                
            Frame label="ColorLogic Light"  
                {
                Switch item=PoolLightEnable
                Selection item=PoolLightCurrentShow
                Text item=PoolLightState
                }
  
            Frame label="Relays"    
                {
                Switch item=PoolRelay1
                Switch item=PoolRelay2                            
                }
  }
```

