# Millheat Binding

This binding integrates the Mill Wi-Fi enabled panel heaters. See <https://www.millheat.com/mill-wifi/>

## Supported Things

This binding supports all Wi-Fi enabled heaters as well as the Wi-Fi socket.

- `account` = Mill Heating API - the account bridge
- `heater` = Panel/standalone heater
- `room` = A room defined in the mobile app
- `home` = A home defined in the mobile app

## Discovery

The binding will discover homes with rooms and heaters.

In order to do discovery, add a thing of type Mill Heating API and add username and password.

## Thing Configuration

See full example below for how to configure using thing files.

### Account

- `username` = email address used in app
- `password` = password used in app
- `refreshInterval` = number of seconds between refresh calls to the server

### Home

- `homeId` = id of home, type number (not string). Use auto discovery to find this value

### Room

- `roomId` = id of room, type number (not string). Use auto discovery to find this value

### Heater

- `macAddress` = network mac address of device in UPPERCASE.  
  Can be found in the app by viewing devices. Or you can find it during discovery. Used for heaters connected to a room.
- `heaterId` = id of device/heater, type number (not string)
  Use auto discovery to find this value. Used to identify independent heaters or heaters connected to a room.
- `power` = number of watts this heater is consuming when active.  
  Used to provide data for the currentPower channel.

Either `macAddres` or `heaterId` must be specified.

## Channels

### Home channels

| Channel                       | Read/write      | Item type           | Description |
| -------------------           | -------------   | ------------------- | ----------- |
| vacationMode                  | R/W             | Switch              | Vacation mode active. Note: In order to activate vacation mode, both vacationModeStart and vacationModeEnd must be set to valid values  |
| vacationModeAdvanced          | R/W             | Switch              | Vacation mode advanced active. Can only be activated after vacation mode is active  |
| vacationModeTargetTemperature | R/W             | Number:Temperature  | Temperature to use when activating vacation mode. Note: If advanced vacation mode is set, this temperature is ignored and the away temperature for each room is used instead  |
| vacationModeStart             | R/W             | DateTime            | Vacation mode start  |
| vacationModeEnd               | R/W             | DateTime            | Vacation mode end  |

### Room channels

| Channel             | Read/write    | Item type             | Description |
| ------------------- | ------------- | --------------------- | ----------- |
| currentTemperature  | R             | Number:Temperature    | Measured temperature in your room (if more than one heater then it is the average of all heaters) |
| currentMode         | R             | String                | Current mode (comfort, away, sleep etc) being active |
| targetTemperature   | R             | Number:Temperature    | Current target temperature for this room (managed by the room program and set by comfort- away- and sleepTemperature) |
| comfortTemperature  | R/W           | Number:Temperature    | Comfort mode temperature |
| awayTemperature     | R/W           | Number:Temperature    | Away mode temperature |
| sleepTemperature    | R/W           | Number:Temperature    | Sleep mode temperature |
| heatingActive       | R             | Switch                | Whether the heaters in this room are active |
| program             | R             | String                | Name of program used in this room |

### Heater channels

| Channel             | Read/write    | Item type          | Description |
| ------------------- | ------------- | ------------------ | ----------- |
| currentTemperature  | R             | Number:Temperature | Measured temperature by this heater |
| targetTemperature   | R/W           | Number:Temperature | Target temperature for this heater. Channel available only if heater is not connected to a room |
| currentPower        | R             | Number:Power       | Current power usage in watts. Note that the power attribute of the heater thing config must be set for this channel to be active  |
| heatingActive       | R             | Switch             | Whether the heater is active/heating  |
| fanActive           | R/W           | Switch             | Whether the fan (if available) is active (UNTESTED) |
| independent         | R             | Switch             | Whether this heater is controlled independently or part of a room setup |
| window              | R             | Contact            | Whether this heater has detected that a window nearby is open/detection of cold air (UNTESTED) |
| masterSwitch        | R/W           | Switch             | Turn heater ON/OFF. Channel available only if heater is not connected to a room |

## Full Example

millheat.things:

```java
Bridge millheat:account:home "Millheat account" [username="email@address.com",password="topsecret"] {
    Thing home monaco "Penthouse Monaco" [ homeId=100000000000000 ] // Note: numeric value
    Thing room office "Office room" [ roomId=200000000000000 ] Note: numeric value
    Thing heater office "Office panel heater" [ macAddress="F0XXXXXXXXX", power=900, heaterId=12345 ] Note: heaterId is a numeric value, macAddress in UPPERCASE
} 
```

millheat.items:

```java
// Items connected to HOME channels
Number:Temperature Vacation_Target_Temperature "Vacation target temp [%d %unit%]" <temperature>  {channel="millheat:home:home:monaco:vacationModeTargetTemperature"}
Switch Vacation_Mode "Vacation mode" <vacation>  {channel="millheat:home:home:monaco:vacationMode"}
Switch Vacation_Mode_Advanced "Use room away temperatures" <vacation>  {channel="millheat:home:home:monaco:vacationModeAdvanced"}
DateTime Vacation_Mode_Start "Vacation mode start [%1$td.%1$tm.%1$ty %1$tH:%1$tM]" <vacation>  {channel="millheat:home:home:monaco:vacationModeStart"}
DateTime Vacation_Mode_End "Vacation mode end [%1$td.%1$tm.%1$ty %1$tH:%1$tM]" <vacation>  {channel="millheat:home:home:monaco:vacationModeStart"}

// Items connected to ROOM channels
Number:Temperature Heating_Office_Room_Current_Temperature "Office current [%.1f %unit%]" <temperature>  {channel="millheat:room:home:office:currentTemperature"}
Number:Temperature Heating_Office_Room_Target_Temperature "Office target [%.1f %unit%]" <temperature>  {channel="millheat:room:home:office:targetTemperature"}
Number:Temperature Heating_Office_Room_Sleep_Temperature "Office sleep [%.1f %unit%]" <temperature>  {channel="millheat:room:home:office:sleepTemperature"}
Number:Temperature Heating_Office_Room_Away_Temperature "Office away [%.1f %unit%]" <temperature>  {channel="millheat:room:home:office:awayTemperature"}
Number:Temperature Heating_Office_Room_Comfort_Temperature "Office comfort [%.1f %unit%]" <temperature>  {channel="millheat:room:home:office:comfortTemperature"}
Switch Heating_Office_Room_Heater_Active "Office active [%s]" <fire>  {channel="millheat:room:home:office:heatingActive"}
String Heating_Office_Room_Mode "Office current mode [%s]" {channel="millheat:room:home:office:currentMode"}
String Heating_Office_Room_Program "Office program [%s]" {channel="millheat:room:home:office:program"}

// Items connected to HEATER channels
Number:Power Heating_Office_Heater_Current_Energy "Energy usage [%d W]" <energy>  {channel="millheat:heater:home:office:currentEnergy"}
Number:Temperature Heating_Office_Heater_Current_Temperature "Heater current [%.1f %unit%]" <temperature>  {channel="millheat:heater:home:office:currentTemperature"}
Number:Temperature Heating_Office_Heater_Target_Temperature "Heater target [%.1f %unit%]" <temperature>  {channel="millheat:heater:home:office:targetTemperature"}
Switch Heating_Office_Heater_Heater_Active "Heater active [%s]" <fire>  {channel="millheat:heater:home:office:heatingActive"}
Switch Heating_Office_Heater_Fan_Active "Fan active [%s]" <fan>  {channel="millheat:heater:home:office:fanActive"}
Contact Heating_Office_Heater_Window "Window status [%s]" <window>  {channel="millheat:heater:home:office:window"}
Switch Heating_Office_Heater_Independent "Heater independent [%s]" <switch>  {channel="millheat:heater:home:office:independent"}
Switch Heating_Office_Heater_MasterSwitch "Heater masterswitch [%s]" <switch>  {channel="millheat:heater:home:office:masterSwitch"}
```

## Setting up vacation mode

In order to activate vacation mode, follow these steps in a rule:

- Set start time (DateTime) on `DateTime` item linked to channel type `vacationModeStart`
- Set end time (DateTime) on `DateTime` item linked to channel type `vacationModeEnd`
- Activate vacation mode on `Switch` item linked to channel type `vacationMode`
- Optional - set advanced vacation mode on `Switch` item linked to channel type `vacationModeAdvanced`
