# SAICiSMART Binding

OpenHAB binding to the SAIC-API used by MG cars (MG4, MG5 EV, MG ZSV...)

It enables iSMART users to get battery status and other data from their cars.
They can also pre-heat their cars by turning ON the AC.

Based on the work done here: https://github.com/SAIC-iSmart-API

## Supported Things

European iSMART accounts and vehicles.

- `account`: Bridge representing an iSMART Account
- `vehicle`: Thing representing an iSMART MG Car

## Discovery

Vehicle discovery is implemented.
Once an account has been configured it can be scanned for vehicles.

## Thing Configuration

### `account` iSMART Account Configuration

| Name     | Type    | Description                 | Default | Required | Advanced |
|----------|---------|-----------------------------|---------|----------|----------|
| username | text    | iSMART username             | N/A     | yes      | no       |
| password | text    | iSMART password             | N/A     | yes      | no       |

### `vehicle` An iSMART MG Car

| Name          | Type | Description                          | Default | Required | Advanced |
|---------------|------|--------------------------------------|---------|----------|----------|
| vin           | text | Vehicle identification number (VIN)  | N/A     | yes      | no       |
| abrpUserToken | text | User token for A Better Routeplanner | N/A     | no       | no       |

## Channels

| Channel                    | Type                     | Read/Write | Description                                         | Advanced |
|----------------------------|--------------------------|------------|-----------------------------------------------------|----------|
| odometer                   | Number:Length            | R          | Total distance driven                               | no       |
| range-electric             | Number:Length            | R          | Electric range                                      | no       |
| soc                        | Number                   | R          | State of the battery in %                           | no       |
| power                      | Number:Power             | R          | Power usage                                         | no       |
| charging                   | Switch                   | R          | Charging                                            | no       |
| engine                     | Switch                   | R          | Engine state                                        | no       |
| speed                      | Number:Speed             | R          | Vehicle speed                                       | no       |
| location                   | Location                 | R          | The actual position of the vehicle                  | no       |
| heading                    | Number:Angle             | R          | The compass heading of the car, (0-360 degrees)     | no       |
| auxiliary-battery-voltage  | Number:ElectricPotential | R          | Auxiliary battery voltage                           | no       |
| tyre-pressure-front-left   | Number:Pressure          | R          | Pressure front left                                 | no       |
| tyre-pressure-front-right  | Number:Pressure          | R          | Pressure front right                                | no       |
| tyre-pressure-rear-left    | Number:Pressure          | R          | Pressure rear left                                  | no       |
| tyre-pressure-rear-right   | Number:Pressure          | R          | Pressure rear right                                 | no       |
| interior-temperature       | Number:Temperature       | R          | Interior temperature                                | no       |
| exterior-temperature       | Number:Temperature       | R          | Exterior temperature                                | no       |
| door-driver                | Contact                  | R          | Driver door open state                              | no       |
| door-passenger             | Contact                  | R          | Passenger door open state                           | no       |
| door-rear-left             | Contact                  | R          | Rear left door open state                           | no       |
| door-rear-right            | Contact                  | R          | Rear right door open state                          | no       |
| window-driver              | Contact                  | R          | Driver window open state                            | no       |
| window-passenger           | Contact                  | R          | Passenger window open state                         | no       |
| window-rear-left           | Contact                  | R          | Rear left window open state                         | no       |
| window-rear-right          | Contact                  | R          | Rear right window open state                        | no       |
| window-sun-roof            | Contact                  | R          | Sun roof open state                                 | no       |
| last-activity              | DateTime                 | R          | Last time the engine was on or the car was charging | no       |
| last-position-update       | DateTime                 | R          | Last time the Position data was updated             | no       |
| last-charge-state-update   | DateTime                 | R          | Last time the Charge State data was updated         | no       |
| remote-ac-status           | Number                   | R          | Status of the A/C                                   | no       |
| switch-ac                  | Switch                   | R/W        | Control the A/C remotely                            | no       |
| force-refresh              | Switch                   | R/W        | Force an immediate refresh of the car data          | yes      |
| last-alarm-message-date    | DateTime                 | R          | Last time an alarm message was sent                 | no       |
| last-alarm-message-content | String                   | R          | Vehicle message                                     | no       |

# Example

demo.things:

```java
Bridge saicismart:account:myaccount "My iSMART Account" [ username="MyEmail@domian.com", password="MyPassword" ] {
  Thing vehicle mymg5 "MG5" [ vin="XXXXXXXXXXXXXXXXX", abrpUserToken="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" ]
}
```

demo.items:

```java
Number 		MG5_Total_Distance_Driven		"MG5 Total Distance Driven"	["Length"]		{channel="saicismart:vehicle:myaccount:mymg5:odometer"}
Number 		MG5_Electric_Range			"MG5 Electric Range"		["Length"]		{channel="saicismart:vehicle:myaccount:mymg5:range-electric"}
Number		MG5_Battery_Level			"MG5 Battery Level"		["Energy"]		{channel="saicismart:vehicle:myaccount:mymg5:soc"}
Number 		MG5_Power_Usage 			"MG5 Power Usage"		["Power"]		{channel="saicismart:vehicle:myaccount:mymg5:power"}
Switch 		MG5_Charging 				"MG5 Charging"						{channel="saicismart:vehicle:myaccount:mymg5:charging"}
Switch 		MG5_Engine_State			"MG5 Engine State"					{channel="saicismart:vehicle:myaccount:mymg5:engine"}
Number 		MG5_Speed				"MG5 Speed"			["Speed"]		{channel="saicismart:vehicle:myaccount:mymg5:speed"}
Location	MG5_Location 				"MG5 Location"						{channel="saicismart:vehicle:myaccount:mymg5:location"}
Number		MG5_Heading				"MG5 Heading" 			["Angle"]		{channel="saicismart:vehicle:myaccount:mymg5:heading"}
Number 		MG5_Auxiliary_Battery_Voltage		"MG5 Auxiliary Battery Voltage"	["ElectricPotential"]	{channel="saicismart:vehicle:myaccount:mymg5:auxiliary-battery-voltage"}
Number 		MG5_Pressure_Front_Left 		"MG5 Pressure Front Left"	["Pressure"]		{channel="saicismart:vehicle:myaccount:mymg5:tyre-pressure-front-left"}
Number 		MG5_Pressure_Front_Right 		"MG5 Pressure Front Right	["Pressure"]		{channel="saicismart:vehicle:myaccount:mymg5:tyre-pressure-front-right"}
Number		MG5_Pressure_Rear_Left			"MG5 Pressure Rear Left"	["Pressure"]		{channel="saicismart:vehicle:myaccount:mymg5:tyre-pressure-rear-left"}
Number		MG5_Pressure_Rear_Right			"MG5 Pressure Rear Right"	["Pressure"]		{channel="saicismart:vehicle:myaccount:mymg5:tyre-pressure-rear-right"}
Number		MG5_Interior_Temperature		"MG5 Interior Temperature" 	["Temperature"]		{channel="saicismart:vehicle:myaccount:mymg5:interior-temperature"}
Number		MG5_Exterior_Temperature		"MG5 Exterior Temperature" 	["Temperature"]		{channel="saicismart:vehicle:myaccount:mymg5:exterior-temperature"}
Contact 	MG5_Driver_Door				"MG5 Driver Door"					{channel="saicismart:vehicle:myaccount:mymg5:door-driver"}
Contact		MG5_Passenger_Door			"MG5 Passenger Door" 					{channel="saicismart:vehicle:myaccount:mymg5:door-passenger"}
Contact 	MG5_Rear_Left_Door 			"MG5 Rear Left Door"					{channel="saicismart:vehicle:myaccount:mymg5:door-rear-left"}
Contact		MG5_Rear_Right_Door 			"MG5 Rear Right Door"					{channel="saicismart:vehicle:myaccount:mymg5:door-rear-right"}
Contact		MG5_Driver_Window			"MG5 Driver Window"					{channel="saicismart:vehicle:myaccount:mymg5:window-driver"}
Contact		MG5_Passenger_Window			"MG5 Passenger Window"					{channel="saicismart:vehicle:myaccount:mymg5:window-passenger"}
Contact		MG5_Rear_Left_Window			"MG5 Rear Left Window" 					{channel="saicismart:vehicle:myaccount:mymg5:window-rear-left"}
Contact		MG5_Rear_Right_Window 			"MG5 Rear Right Window"					{channel="saicismart:vehicle:myaccount:mymg5:window-rear-right"}
Contact		MG5_Sun_Roof				"MG5 Sun Roof"						{channel="saicismart:vehicle:myaccount:mymg5:window-sun-roof"}
DateTime 	MG5_Last_Car_Activity 			"MG5 Last Car Activity"					{channel="saicismart:vehicle:myaccount:mymg5:last-activity"}
DateTime 	MG5_Last_Position_Timestamp 		"MG5 Last Position Timestamp"				{channel="saicismart:vehicle:myaccount:mymg5:last-position-update"}
DateTime 	MG5_Last_Charge_State_Timestamp		"MG5 Last Charge State Timestamp"			{channel="saicismart:vehicle:myaccount:mymg5:last-charge-state-update"}
Number		MG5_Remote_AC 				"MG5 Remote A/C"					{channel="saicismart:vehicle:myaccount:mymg5:remote-ac-status"}
Switch		MG5_Switch_AC 				"MG5 Switch A/C"					{channel="saicismart:vehicle:myaccount:mymg5:switch-ac"}
Switch		MG5_Force_Refresh 			"MG5 Force Refresh"					{channel="saicismart:vehicle:myaccount:mymg5:force-refresh"}
DateTime 	MG5_Last_Alarm_Message_Timestamp	"MG5 Last Alarm Message Timestamp"			{channel="saicismart:vehicle:myaccount:mymg5:last-alarm-message-date"}
String 		MG5_Vehicle_Message			"MG5 Vehicle Message"					{channel="saicismart:vehicle:myaccount:mymg5:last-alarm-message-content"}
```

## Limitations

The advanced channel "force refresh" if used regularly will drain the 12v car battery and you will be unable to start it!

Only European iSMART accounts and vehicles are supported. API host configuration and testing for other markets is required.
