# <bindingName> Binding

The Hayward Omnilogic binding integrates the Omnilogic pool controller using the Hayward API.

The Hayward Omnilogic API interacts with Hayward's cloud server requiring a connection with the Internet for sending and receiving information.

## Supported Things

The table below lists the Nest binding thing types:

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

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

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

After installing the binding, you will need to manually add the Hayward Connection thing and enter your credentials.  All pool items will then be automatically discovered
Goto the inbox and add them all.  Open any of the items and copy the bridgeUID (2ee76053) and replace all occurences in the items file. 

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

### transforms:

These transform will decode the integers provided from Hayward into text descriptions.  Please note this is a work in progress.
If you have any descriptions to add, please email to the code owner for updates to this page.

### transforms:haywardChlorinatorAlert.js

...
(function(i) {
    var state = ""
    var value = parseInt(i)
    
    if (value == 16){
        state = "Low T-Cell Temperature"; 
     
    } else {
        state = i + " (Unkown State)";
    }
    return state;
})(input)
...

### transforms:haywardFilterState.js

...
(function(i) {
    var state = ""
    var value = parseInt(i)
    
    if(value == 0) {
        state = "Off";
   
    } else if (value == 16){
        state = "Heater Cooldoown"; 
     
    } else {
        state = i + " (Unkown State)";
    }
    return state;
})(input)
...

### transforms:haywardFilterWhyRunning.js

...
(function(i) {
    var state = ""
    var value = parseInt(i)
    
    if(value == 0) {
        state = "Off";
        
    } else if (value == 1){
        state = "Off"; 
        
    } else if (value == 2){
        state = "Heater Cooldoown"; 
     
    } else if (value == 11){
        state = "On High (Priming)"; 

    } else if (value == 15){
        state = "Freeze Protection"; 
 
     } else {
        state = i + " (Unkown State)";
    }
    return state;
})(input)

...

### transforms:haywardFlow.js

...
(function(i) {
    var state = ""
    var value = parseInt(i)
    
    if(value == 0) {
        state = "Off";
        
    } else if (value == 1){
        state = "On"; 

    } else {
        state = i + " (Unkown State)";
    }
    return state;
})(input)
...

### transforms:haywardLightShow.map

...
NULL NULL
0 = Voodoo Lounge
1 = Deep Blue Sea
2 = Royal Blue
3 = Afternoon Sky
4 = Aqua Green
5 = Emerald
6 = Cloud White
7 = Warm Red
8 = Flamingo
9 = Vivid Violet
10 = Sangria
11 = Twilight
12 = Tranquility
13 = Gemstone
14 = USA
15 = Mardi Gras
16 = Cool Cabaret   
...

### transforms:haywardLightState.js

...
(function(i) {
    var state = ""
    var value = parseInt(i)
    
    if(value == 0) {
        state = "Off";
        
    }else if(value == 4) {
        state = "15 Sec White Light";
 
    }else if(value == 4) {
            state = "15 Sec White Light";
            
    }else if(value == 6) {
            state = "On";

    }else if(value == 7) {
        state = "Powering Off";
       
    } else {
        state = i;
    }
    return state;
})(input) 
...

### transforms:haywardLightState.map

...
NULL NULL
0 = Off
4 = Turning On
6 = On
7 = Turning Off
...


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

