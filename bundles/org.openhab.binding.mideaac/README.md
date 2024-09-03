## Midea AC Binding

This binding integrates Air Conditioners that use the Midea protocol. Midea is an OEM for many brands.

An AC device is likely supported if it uses one of the following Android apps or it's iOS equivalent.
 
| Application						 		 |				Comment									 |
|:-------------------------------------------|:------------------------------------------------------|
|Midea Air (com.midea.aircondition.obm)		 | Full Support of key and token updates				 |
|NetHome Plus (com.midea.aircondition)		 | Full Support of key and token updates				 |
|SmartHome/MSmartHome (com.midea.ai.overseas)| Full Support of key and token updates				 |

Note: The Air Conditioner must already be set-up on the WiFi network and must have a fixed IP Address 
	with one of the three apps listed above for full discovery and key and token updates.

## Supported Things

This binding supports one Thing type `ac`.

## Discovery

Once the Air Conditioner is on the network (WiFi active) it could be discovered automatically.
An IP broadcast message is sent and every responding unit gets added to the Inbox.

## Binding Configuration

No binding configuration is required.

## Thing Configuration

| Parameter            		| Required ? 		| Comment
|:--------------------------|:------------------|:----------------------------------------------------------------------------------|
| IP Address 				| Required 			| IP Address of the device.          												|                                       
| IP Port         			| Required  		| IP port of the device (for V2: 6444).												|
| Device ID       			| Required  		| ID of the device. Leave 0 to do ID discovery (length 6 bytes). 					|                                   
| Cloud Provider 			| Required for V.3	| Cloud Provider name for email and password										|
| Cloud Provider Email  	| Optional 			| Email for cloud account chosen in Cloud Provider.									|
| Cloud Provider Password 	| Optional 			| Password for cloud account chosen in Cloud Provider.								|
| Token  					| Required for V.3 	| Secret Token (length 128 HEX)														|
| Key						| Required for V.3 	| Secret Key (length 64 HEX)       													|
| Polling time 				| Required  		| Polling time in seconds. Minimum time is 30 seconds, default is 60 seconds.		|
| Timeout Socket			| Required 			| Connecting timeout. Minimum time is 2 second, maximum 10 seconds (4 secs default).|
| Prompt tone 				| Optional 			| "Ding" tone when command is received and executed.								|

## Channels

Following channels are available:

| Channel               | Type                 | Description                                                                                                    | read only |
|:----------------------|:---------------------|:---------------------------------------------------------------------------------------------------------------|:----------|
| Power					| Switch               | Turn the AC on and off.                                                                                        |           |
| Target temperature    | Number:Temperature   | Target temperature.                                                                                            |           |
| Operational mode      | String               | Operational mode: OFF (turns off), AUTO, COOL, DRY, HEAT, FAN ONLY                                             |           |
| Fan speed             | String               | Fan speed: OFF (turns off), SILENT, LOW, MEDIUM, HIGH, AUTO. Not all modes supported by all units.             |           |
| Swing mode            | String               | Swing mode: OFF, VERTICAL, HORIZONTAL, BOTH. Not all modes supported by all units.                             |           |
| Eco mode              | Switch               | Eco mode - Cool only (Temperature is set to 24 C (75 F) and fan on AUTO)										|			|
| Turbo mode            | Switch               | Turbo mode, "Boost" in Midea Air app, long press "+" on IR Remote Controller. COOL and HEAT mode only.  		|           |
| Sleep function        | Switch               | Sleep function ("Moon with a star" icon on IR Remote Controller).                                              |           |
| ON Timer				| String               | Sets the future time to turn On the AC.																		|		    |
| OFF Timer				| String               | Sets the future time to turn off the AC.																		|			|
| Temperature Unit		| Switch               | Sets the display to Fahrenheit (true) or Celsius (false).														|           |
| Screen display		| Switch			   | If device supports across LAN, turns off the LED display.														|			|
| Indoor temperature    | Number:Temperature   | Indoor temperature measured in the room, where internal unit is installed.                                     | Yes       |
| Outdoor temperature   | Number:Temperature   | Outdoor temperature by external unit. Some units do not report reading when off.                               | Yes       |
| Humidity				| Number			   | If device supports, the indoor humidity.																		| Yes		|
| Dropped Commands		| Number			   | Quality of WiFi connections - For debugging only.																| Yes		|

Following items are in API but are not tested (marked as advanced and read only channels):

- Imode resume
- Timer mode
- Appliance error
- Cozy sleep
- Save
- Low frequency fan
- Super fan
- Feel own 
- Child sleep mode
- Exchange air
- Dry clean
- Auxiliary heat
- Clean up
- Catch cold
- Night light
- Peak electricity
- Natural fan
- Alternate Target Temperature

### Debugging and Tracing

Switch the log level to TRACE or DEBUG on the UI Settings Page (Add-on Settings)
