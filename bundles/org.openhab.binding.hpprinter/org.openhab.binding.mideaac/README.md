## Midea AC Binding

This binding integrates Air Conditioners that use the Midea protocol. 
Reviewing the Home Assistant equivalent integration, Midea is an OEM for many brands including: AirCon, Alpine Home Air, Artel, Beko, Canair, Carrier, Century, Comfee, Cooper&Hunter, Electrolux, Friedrich, Galactic, Goodman, Hualing, Idea, Inventor, Kaisai, Kenmore, Klimaire, Lennox, LG, Mitsui, Mr. Cool, Neoclima, Olimpia Splendid, Pioneer, Pridiom, QLIMA, Qzen, Rotenso, Royal Clima, Samsung, Senville, Thermocore, Toshiba, Trane and more. Only a few of the above brands have been tested with OpenHAB.  Please report any misinformation.

A device is likely supported if it uses one of the following Android apps or it's iOS equivalent.
 
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
| IP Address 				| Required 			| IP Address of the device.           												|                                                            
| IP Port         			| Required  		| IP port of the device (for V2: 6444).												|
| Device ID       			| Required  		| ID of the device. Leave 0 to do ID discovery (length 6 bytes - signed). 			|                                   
| Cloud Provider 			| Optional  		| Cloud Provider name for email and password										|
| Cloud Provider Email  	| Optional 			| Email for cloud account chosen in Cloud Provider.									|
| Cloud Provider Password 	| Optional 			| Password for cloud account chosen in Cloud Provider.								|
| Re-authorization Interval | Optional 			| Request new token after X hours (never - set 0).									|
| Token  					| Required for V.3 	| Secret Token (length 128 HEX)														|
| Key						| Required for V.3 	| Secret Key (length 64 HEX)       													|
| Polling time 				| Required  		| Polling time in seconds to update status. Minimum time is 30 seconds.				|
| Timeout 					| Required 			| Connecting timeout. Minimum time is 2 second, maximum 10 seconds (4 secs default).|
| Prompt tone 				| Optional 			| "Ding" tone when command is received and executed.								|

## Channels

Following items are available:

| Channel               | Type                 | Description                                                                                                    | read only |
|:----------------------|:---------------------|:---------------------------------------------------------------------------------------------------------------|:----------|
| Power					| Switch               | Turn the AC on and off.                                                                                        |           |
| Target temperature    | Number:Temperature   | Target temperature.                                                                                            |           |
| Operational mode      | String               | Operational mode: OFF (turns off), AUTO, COOL, DRY, HEAT.                                                      |           |
| Fan speed             | String               | Fan speed: OFF (turns off), SILENT, LOW, MEDIUM, HIGH, AUTO.                                                   |           |
| Swing mode            | String               | Swing mode: OFF, VERTICAL, HORIZONTAL, BOTH.                                                                   |           |
| Eco mode              | Switch               | Eco mode - Cool only (Temperature is set to 24 C (75 F) and fan on AUTO) (1) Switch may not be set in all ACs	| Note (1)  |
| Turbo mode            | Switch               | Turbo mode, "Boost" in Midea Air app, long press "+" on IR Remote Controller. COOL and HEAT mode only.  		|           |
| Indoor temperature    | Number:Temperature   | Indoor temperature measured in the room, where internal unit is installed.                                     | Yes       |
| Outdoor temperature   | Number:Temperature   | Outdoor temperature measured outside, where external unit is installed.                                        | Yes       |
| Sleep function        | Switch               | Sleep function ("Moon with a star" icon on IR Remote Controller).                                              |           |
| ON Timer				| String               |																												| Yes       |
| OFF Timer				| String               |																												| Yes       |
| Temperature Unit		| Switch               | Sets the display to Fahrenheit (true) or Celsius (false).														|           |

Following items are in API but are not tested (marked as advanced and read only channels):

- Screen display
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
- Aux heat
- Clean up
- Temperature unit
- Catch cold
- Night light
- Peak elec
- Natural fan
- Humidity
- Dropped Commands - To monitor polling without raising the Log level.

### Debugging and Tracing

Switch the log level to TRACE or DEBUG on the UI Settings Page (Add-on Settings)
