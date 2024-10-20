# Lambda Heat Pump

This extension adds support for the Lambda Heat Pump modbus protocol as provided by
https://lambda-wp.at/wp-content/uploads/2022/01/Modbus-Protokoll-und-Beschreibung.pdf 

A Lambda Heat Pump has to be reachable within your network. 
If you plan to use the E-Manager part to hand over your PV excess to the heat pump ask Lambda support to 
configure it to  
E-Meter Kommunikationsart:          ModBus Client
E-Meter Messpunkt:                  E-Eintrag

Other configurations of the E-Manager are not supported (yet).

Up to now only the following configuration is supported:
- only one heatpump(heatpump1)
- only one buffer for the heating(buffer1)
- only one buffer for the water heating boiler1)


## Supported Things

This bundle adds the following thing types to the Modbus binding.
Note, that the things will show up under the Modbus binding.

| Thing              | ThingTypeID | Description                                         |
| ------------------ | ----------- | --------------------------------------------------- |
| Lambda Heat Pump   | lambdahp    | A lambda heat pump visible in the local network     |

## Discovery

This extension does not support autodiscovery. The things need to be added manually.

A typical bridge configuration would look like this:

```java
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1 ]
```

## Thing Configuration

You first need to set up a TCP Modbus bridge according to the Modbus documentation.
You then add the lambda heat pump as part of the modbus binding.
Things in this extension will use the selected bridge to connect to the device.

The following parameters are valid for all thing types:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| refresh   | integer | no       | 30                 | Poll interval in seconds. Increase this if you encounter connection errors |
| maxTries  | integer | no       | 3                  | Number of retries when before giving up reading from this thing.           |

## Channels

Channels are grouped into channel groups.

### General Ambient Group

This group contains general operational information about the heat pump.


| Channel ID                       | Item Type          | Read only | Unit     | Description                                               				  |
| -------------------------------- | ------------------ | --------- | -------- | ------------------------------------------------------------------------ |
| ambient-error-number             | Number             | true      | [Nr]     | Ambient Error Number (0 = No error)                         		      |
| ambient-operating-state          | Number             | true      | [Nr]     | Ambient Operating State (0 = OFF, 1 = AUTOMATIC, 2 = MANUAL, 3 = ERROR)  |
| actual-ambient-temperature       | Number:Temperature | false     | [0.1 °C] | Actual Ambient Temperature (min = -50.0°C); max = 80.0°   				  |
| average-ambient-temperature      | Number:Temperature | true      | [0.1 °C] | Arithmetic average temperature of the last 60 minutes   				  |
| calculated-ambient-temperature   | Number:Temperature | true      | [0.1 °C] | Temperature for calculations in heat distribution modules 				  |

### General E-Manager Group

This group contains parameters signaling the PV excess to the heat pump.

| Channel ID                       | Item Type          | Read only | Unit     | Description                                               							     |
| -------------------------------- | ------------------ | --------- | -------- | --------------------------------------------------------------------------------------- |
| emanager-error-number            | Number             | true      | [Nr]     | E-Manager Error Number (0 = No error)                                                   |
| emanager-operating-state         | Number             | true      | [Nr]     | E-Manager Operating State (0 = OFF, 1 = AUTOMATIC, 2 = MANUAL, 3 = ERROR 4 = OFFLINE    |
| actual-power   				   | Number:Power       | false     | [W]      | Actual excess power -32768	W .. 32767 W                   							     |
| actual-power-consumption         | Number:Power       | true      | [W]      | Power consumption of heatpump 1 (only valid when Betriebsart: Automatik, 0 W otherwise) |
| power-consumption-setpoint       | Number:Power       | false     | [W]      | Power consumption setpoint for heat pump 1									             |


### Heat Pump 1 Group

This group contains general operational information about the heat pump itself.

| Channel ID                     | Item Type                 | Read only | Unit         | Description                                               			  |
| -------------------------------| ------------------------- | --------- | ------------ | ----------------------------------------------------------------------- |
| heatpump1-error-state      	 | Number          			 | true      | [Nr]         | Error state  (0 = NONE, 1 = MESSAGE, 2 = WARNING, 3 = ALARM, 4 = FAULT )|
| heatpump1-error-number         | Number           		 | true      | [Nr]         | Error number: scrolling through all active error number (1..99)         |
| heatpump1-state      	    	 | Number	       			 | true      | [Nr]         | State: See Modbus description manual of the manufacterer         		  |
| heatpump1-operating-state      | Number	        		 | true      | [Nr]         | Operating State: See Modbus description manual, link above              |
| heatpump1-t-flow           	 | Number:Temperature 		 | true      | [0.01°C]     | Flow line termperature 	 				   			   				  |
| heatpump1-t-return			 | Number:Temperature 		 | true      | [0.01°C]     | Return line temperature	   						       				  |
| heatpump1-vol-sink  		   	 | Number:VolumetricFlowRate | true      | [0.01 l/min] | Volume flow heat sink                                     			  |
| heatpump1-t-eqin  			 | Number:Temperature        | true      | [0.01°C]     | Energy source inlet temperature						   			      |
| heatpump1-t-eqout 			 | Number:Temperature  	     | true      | [0.01°C]     | Energy source outlet temperature		                   				  |
| heatpump1-vol-source        	 | Number:VolumetricFlowRate | true      | [0.01 l/min] | Volume flow energy source	 	 						   				  |
| heatpump1-compressor-rating    | Number             		 | true      | [0.01%]      | Compressor unit rating                                   			  	  |
| heatpump1-qp-heating           | Number:Power              | true      | [0.1kW]      | Actual heating capacity								   				  |
| heatpump1-fi-power-consumption | Number:Power      		 | true      | [W]          | Frequency inverter actual power consumption               			  |
| heatpump1-cop		 			 | Number					 | true      | [0.01%]	    | Coefficient of performance								   			  | 
| heatpump1-set-error-quit   	 | Number           		 | false     | [Nr]         | Set Error Quit (1 = Quit all active heat pump errors                    |


### Boiler 1 Group

This group contains information about the boiler for the water for domestic use / tap water / washwater.

| Channel ID                         | Item Type          | Read only | Unit     | Description                                                         |
| ---------------------------------- | ------------------ | --------- | -------- | --------------------------------------------------------------------|
| boiler1-error-number               | Number             | true      | [Nr]     | Boiler 1 Error Number(0 = No error)                                 |
| boiler1-operating-state            | Number             | true      | [Nr]     | Boiler 1 Operating State: See Modbus description manual, link above |
| boiler1-actual-high-temperature    | Number:Temperature | true      | [0.1 °C] | Actual temperature boiler high sensor   			                   |
| boiler1-actual-low-temperature     | Number:Temperature | true      | [0.1 °C] | Actual temperature boiler low sensor        						   |
| boiler1-maximum-boiler-temperature | Number:Temperature | false     | [0.1 °C] | Setting for maximum boiler temperature (min = 25.0°C; max = 65.0°C) |

### Buffer 1 Group

This group contains information about the buffer for the heating circuit.

| Channel ID                      | Item Type          | Read only | Unit     | Description                                                            |
| ------------------------------- | ------------------ | --------- | -------- | -----------------------------------------------------------------------|
| buffer1-error-number            | Number             | true      | [Nr]     | Buffer 1 Error Number (0 = No error)                                   |
| buffer1-operating-state         | Number             | true      | [Nr]     | Buffer 1 Operating State: See Modbus description manual, link above    |
| buffer1-actual-high-temperature | Number:Temperature | true      | [0.1 °C] | Actual temperature buffer high sensor				                   |
| buffer1-actual-low-temperature  | Number:Temperature | true      | [0.1 °C] | Actual temperature buffer low sensor        						   |
| Buffer 1 Maximum Temperature	  | Number:Temperature | false	   | [0.1 °C] | Setting for maximum buffer temperature (min = 25.0°C; max = 65.0°C)    |


### Heating Circuit 1 Group

This group contains general operational information about the heating circuit 1.

| Channel ID                       				 | Item Type          | Read only | Unit         | Description                                               					   |
| ---------------------------------------------- | -------------------| --------- | ------------ | --------------------------------------------------------------------------------|
| heatingcircuit1-error-number      			 | Number             | true      | [Nr]         | Error Number (0 = No error)													   |
| heatingcircuit1-operating-state            	 | Number             | true      | [Nr]         | Operating State: See Modbus description manual, link above|					   |
| heatingcircuit1-flow-line-temperature   		 | Number:Temperature | true      | [Nr]         | Actual temperature flow line sensor         									   |
| heatingcircuit1-return-line-temperature      	 | Number:Temperature | true      | [Nr]         | Actual temperature return line sensor         								   |
| heatingcircuit1-room-device-temperature        | Number:Temperature | true      | [0.01°C]     | Actual temperature room device sensor (min = -29.9°C; max = 99.9°C) 	 		   |
| heatingcircuit1-setpoint-flow-line-temperature | Number:Temperature | true      | [0.01°C]     | Setpoint temperature flow line (min = 15.0°C; max = 65.0°C)	   				   | 
| heatingcircuit1-operating-mode  		   		 | Number 			  | true      | [Nr] 		 | Operating Mode: See Modbus description manual, link above    				   |
| heatingcircuit1-offset-flow-line-temperature   | Number:Temperature | true      | [0.01°C]     | Setting for flow line temperature setpoint offset(min = -10.0K; max = 10.0K)	   |
| heatingcircuit1-room-heating-temperature 		 | Number:Temperature | rue       | [0.01°C]     | Setting for heating mode room setpoint temperature(min = 15.0°C; max = 40.0 °C) |
| eatingcircuit1-room-cooling-temperature        | Number:Temperature | true      | [0.01 l/min] | Setting for cooling mode room setpoint temperature(min = 15.0°C; max = 40.0 °C) |



## Full Example

### Thing Configuration
UID: modbus:tcp:Lambda_Bridge
label: Lambda Modbus Bridge
thingTypeUID: modbus:tcp
configuration:
  rtuEncoded: false
  connectMaxTries: 1
  reconnectAfterMillis: 0
  timeBetweenTransactionsMillis: 100
  port: 502
  timeBetweenReconnectMillis: 0
  connectTimeoutMillis: 10000
  host: 192.168.223.83
  afterConnectionDelayMillis: 0
  id: 1
  enableDiscovery: false

### Example to write PV excess to the Lambda Heat Pump
// PV_Battery.state and PV_Grid have to be provided by your PV inverter
// Mode of E-Manager has to be switched to AUTOMATIK in the Lambda Heat Pump App
var int P_Available =  ((Lambda_EMgr_Power_Consumption_Value_as_Number.state as Number) - (PW_Battery.state as Number) - (PW_Grid.state as Number)).intValue 
    lambdahp_actual_power.sendCommand(P_Available)


