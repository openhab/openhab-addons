# Hayward Omnilogic Binding

The Hayward Omnilogic binding integrates the Omnilogic pool controller using the Hayward API.

The Hayward Omnilogic API interacts with Hayward's cloud server requiring a connection with the Internet for sending and receiving information.

## Supported Things

The table below lists the Hayward OmniLogic binding thing types:

| Things                                  | Description                                                                                                                                    | Thing Type     |
|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|----------------|
| Hayward OmniLogix Connection            | Connection to Hayward's Server                                                                                                                 | bridge         |
| Backyard                                | Backyard                                                                                                                                       | backyard       |
| Body of Water                           | Body of Water                                                                                                                                  | bow            |
| Chlorinator                             | Chlorinator																																	   | chlorinator    |
| Colorlogic Light                        | Colorlogic Light                                                                                                                               | colorlogic     |
| Filter                                  | Filter/Pump control                                                                                                                            | filter         |
| Heater Equipment                        | Actual heater (i.e. gas)                                                                                                                       | heater         |
| Relay                                   | A Thermostat to control the various aspects of the house's HVAC system                                                                         | relay          |
| Virtaul Heater                          | A Virtual Heater that can controls the heater equipment based on priority                                                                      | virtualHeater  |
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
| backyardStatus       			| Number    | Backyard status															|      R     |
| backyardState 				| Number	| Backyard state								                            |      R     |
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
| chlorEnable       			| Number    | Chlorinator enable														|     R/W    |
| chlorOperatingMode    		| Number    | Chlorinator operating mode												|      R     |
| chlorTimedPercent 			| Number	| Chlorinator timed percent					                                |     R/W    |
| chlorOperatingState   		| Number    | Chlorinator operating state												|      R     |
| chlorScMode    				| Number    | Chlorinator mode															|      R     |
| chlorError     				| Number    | Chlorinator error															|      R     |
| chlorAlert        			| Number    | Chlorinator alert															|      R     |
| chlorAvgSaltLevel     		| Number    | Chlorinator average salt level											|      R     |
| chlorInstantSaltLevel 		| Number    | Chlorinator instant salt level											|      R     |
| chlorStatus     				| Number    | Chlorinator status													    |      R     |
		
### Colorlogic Light Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| colorLogicLightEnable       	| Number    | Colorlogic Light enable													|     R/W    |
| colorLogicLightState    		| Number    | Colorlogic Light state													|      R     |
| colorLogicLightCurrentShow 	| Number	| Colorlogic Light current show					                            |     R/W    |
| colorLogicLightSpeed   		| Number    | Colorlogic Light speed													|      R     |
| colorLogicLightBrightness		| Number    | Colorlogic Light brightness												|      R     |

### Filter Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| filterEnable	    			| Number    | Filter enable																|     R/W    |
| filterValvePosition    		| Number    | Filter valve position														|      R     |
| filterSpeed 					| Number	| Filter speed								                                |     R/W    |
| filterState   				| Number    | Filter state																|      R     |
| filterWhyFilterIsOn    		| Number    | Filter why is on															|      R     |
| filterFpOverride     			| Number    | Filter freeze protection override											|      R     |

### Heater Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| heaterState	    			| Number    | Heater state																|      R     |
| heaterTemp    				| Number    | Heater temperature														|      R     |
| heaterEnable 					| Number	| Heater enable								                                |      R     |
| heaterPriority    			| Number    | Heater priority															|      R     |
| heaterMaintainFor     		| Number    | Heater maintain for														|      R     |

### Relay Channels

| Channel Type ID       		| Item Type | Description                                                               | Read Write |
|-------------------------------|-----------|---------------------------------------------------------------------------|:----------:|
| relayState	    			| sys.power | Relay state																|     R/W    |

	
			
## Full Example

After installing the binding, you will need to manually add the Hayward Connection thing and enter your credentials.  All pool items will then be automatically discovered.
Goto the inbox and add the things.

### demo.items:

```
Number   PoolBackyardAirTemp "Air Temp [%1.0f °F]"                              { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAirTemp"}
Number   PoolBackyardStatus "Status [%s]"                                       { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardStatus"}
Number   PoolBackyardState "State [%s]"                                         { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardState"}
String   PoolBackyardAlarm1 "Alarm #1 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm1"}
String   PoolBackyardAlarm2 "Alarm #2 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm2"}
String   PoolBackyardAlarm3 "Alarm #3 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm3"}
String   PoolBackyardAlarm4 "Alarm #4 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm4"}
String   PoolBackyardAlarm5 "Alarm #5 [%s]"                                     { channel = "haywardomnilogic:backyard:2ee76053:35940:backyardAlarm5"}
  
Number   PoolBOWFlow "Flow Switch [JS(haywardFlow.js):%s]"                      { channel = "haywardomnilogic:bow:2ee76053:30:bowFlow"}
Number   PoolBOWWaterTemp "Water Temp [%1.0f °F]"                               { channel = "haywardomnilogic:bow:2ee76053:30:bowWaterTemp"}
 
Switch   PoolChlorEnable "Enable"                                               { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorEnable"}
Number   PoolChlorOperatingMode "Operating Mode [%s]"                           { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorOperatingMode"}
Number   PoolChlorSaltOutput "Salt Output [%1.0f %%]" (PoolChlorChart)          { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorTimedPercent"}
Number   PoolChlorOperatingState "Operating State [%s]"                         { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorOperatingState"}
Number   PoolChlorScMode "SC Mode [%s]"                                         { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorScMode"}
Number   PoolChlorError "Error [%s]"                                            { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorError"}
Number   PoolChlorAlert "Alert [JS(haywardChlorinatorAlert.js):%s]"             { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorAlert"}
Number   PoolChlorAverageSalt "Average Salt[%1.0f ppm]"                         { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorAvgSaltLevel"}
Number   PoolChlorInstantSalt "Instant Salt [%1.0f ppm]"                        { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorInstantSaltLevel"}
Number   PoolChlorStatus "Status"                                               { channel = "haywardomnilogic:chlorinator:2ee76053:34:chlorStatus"}

Number   PoolHeaterState "State [%s]"                                           { channel = "haywardomnilogic:heater:2ee76053:33:heaterState"}
Number   PoolHeaterTemp "Temp [%1.0f °F]"                                       { channel = "haywardomnilogic:heater:2ee76053:33:heaterTemp"}
Number   PoolHeaterEnable "Power [%s]"                                          { channel = "haywardomnilogic:heater:2ee76053:33:heaterEnable"}
Number   PoolHeaterPriority "Priority [%s]"                                     { channel = "haywardomnilogic:heater:2ee76053:33:heaterPriority"}
Number   PoolHeaterMaintainFor "Maintain For [%s]"                              { channel = "haywardomnilogic:heater:2ee76053:33:heaterMaintainFor"}

Switch   PoolLightEnable "Enable"                                               { channel = "haywardomnilogic:colorlogic:2ee76053:38:colorLogicLightEnable"}
Number   PoolLightState "State [JS(haywardLightState.js):%s]"                   { channel = "haywardomnilogic:colorlogic:2ee76053:38:colorLogicLightState"}
Number   PoolLightCurrentShow "Show"                                            { channel = "haywardomnilogic:colorlogic:2ee76053:38:colorLogicLightCurrentShow"}
Number   PoolLightSpeed "Speed [%s]"                                            { channel = "haywardomnilogic:colorlogic:2ee76053:38:colorLogicLightSpeed"}
Number   PoolLightBrightness "Brightness [%s]"                                  { channel = "haywardomnilogic:colorlogic:2ee76053:38:colorLogicLightBrightness"}

Switch   PoolFilterEnable "Power"                                               { channel = "haywardomnilogic:filter:2ee76053:31:filterPowerSwitch"}
Number   PoolFilterValve "Valve Position [%s]"                                  { channel = "haywardomnilogic:filter:2ee76053:31:filterValvePosition"}
Number   PoolFilterSpeed "Speed[%1.0f %%]"                                      { channel = "haywardomnilogic:filter:2ee76053:31:filterSpeed"}
Number   PoolFilterState "State [JS(haywardFilterState.js):%s]"                 { channel = "haywardomnilogic:filter:2ee76053:31:filterState"}
Number   PoolFilterCall "Why Running [JS(haywardFilterWhyRunning.js):%s]"       { channel = "haywardomnilogic:filter:2ee76053:31:filterWhyFilterIsOn"}
Number   PoolFilterFP "Freeze Protection"                                       { channel = "haywardomnilogic:filter:2ee76053:31:filterFpOverride"}

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
                Text item=PoolFilterCall 
                Text item=PoolFilterFP 
                }
 
             Frame label="Heater"  
                {
                Text item=PoolHeaterState
                Text item=PoolHeaterTemp 
                Text item=PoolHeaterEnable 
                Text item=PoolHeaterPriority 
                Text item=PoolHeaterMaintainFor
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
                Selection item=PoolLightCurrentShow mappings=[0="Voodoo Lounge", 1="Deep Blue Sea", 2 = "Royal Blue", 3 = "Afternoon Sky", 4 = "Aqua Green", 5 = "Emerald", 6 = "Cloud White", 7 = "Warm Red", 8 = "Flamingo", 9 = "Vivid Violet", 10 = "Sangria", 11 = "Twilight", 12 = "Tranquility", 13 = "Gemstone", 14 = "USA", 15 = "Mardi Gras" ,16 = "Cool Cabaret"]
                Setpoint item=PoolLightSpeed
                Setpoint item=PoolLightBrightness
                Text item=PoolLightState
                }
  
                
            Frame label="Relays"    
                {
                Switch item=PoolRelay1
                Switch item=PoolRelay2                            
                }
  }
```

