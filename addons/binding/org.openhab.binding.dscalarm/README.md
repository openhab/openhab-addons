#DSC Alarm Binding

This is an OpenHAB binding for a DSC PowerSeries Alarm System utilizing the EyezOn Envisalink 3/2DS interface or the DSC IT-100 RS-232 interface.

The DSC PowerSeries Alarm System is a popular do-it-yourself home security system, which can be monitored and controlled remotely through a standard web-browser or mobile device.

The OpenHAB DSC Alarm binding provides connectivity to the DSC Alarm panel via a TCP socket connection to the EyesOn Envisalink 3/2DS interface or a RS-232 serial connection to the DSC IT-100 interface.

## Supported Things

This binding supports the following thing types

<table>
<tr><td><b>Thing</b></td><td><b>Thing Type</b></td><td><b>Description</b></td></tr>
<tr><td>EnvisalinkBridge</td><td>Bridge</td><td>The EyezOn Envisalink 3/2DS interface.</td></tr>
<tr><td>IT100Bridge</td><td>Bridge</td><td>The DSC IT-100 RS-232 interface.</td></tr>
<tr><td>Panel</td><td>Thing</td><td>The basic representation of the DSC Alarm System.</td></tr>
<tr><td>Partition</td><td>Thing</td><td>Represents a controllable area within a DSC Alarm system.</td></tr>
<tr><td>Zone</td><td>Thing</td><td>Represents a physical device such as a door, window, or motion sensor.</td></tr>
<tr><td>Keypad</td><td>Thing</td><td>Represents the central administrative unit.</td></tr>
</table>

## Binding Configuration

There are essentially no overall binding configuration settings that need to be set.  Most settings are through thing configuration parameters.

## Discovery

The DSC Alarm binding incorporates several discovery modes in order to find DSC Alarm devices.  First, there is the Envisalink bridge discovery mode which performs a network query for any Envisalink adapters and adds them to the discovery inbox.  Second, there is The IT-100 bridge discovery mode which will find the serial ports attached to the OpenHAB machine and add them to the discovery inbox, which allows the choosing of the port corresponding to the IT-100 bridge.  Third, after a bridge is discovered and available to OpenHAB, the binding will attempt to discover DSC Alarm devices and add them to the discovery inbox.

## Thing Configuration

DSC Alarm things can be configured either through the online configuration utility via discovery, or manually through the 'dscalarm.thing' configuration file.  The following table shows the available configuration parameters for each thing.

<table>
	<tr><td><b>Thing</b></td><td><b>Configuration Parameters</b></td></tr>	
	<tr><td>EnvisalinkBridge</td><td><table><tr><td><b>ipAddress</b> - IP address for the Envisalink adapter - Required.</td></tr><tr><td><b>port</b> - TCP port for the Envisalink adapter - Not Required - default = 4025.</td></tr><tr><td><b>password</b> - Password to login to the Envisalink bridge - Not Required.</td></tr><tr><td><b>connectionTimeout</b> - TCP socket connection timeout in milliseconds - Not Required - default=5000.<br/></td></tr><tr><td><b>pollPeriod</b> - Period of time in minutes between the poll command being sent to the Envisalink bridge - Not Required - default=1.</td></tr></table></td></tr>
	<tr><td>IT100Bridge</td><td><table><tr><td><b>serialPort</b> - Serial port for the IT-100s bridge - Required.</td></tr><tr><td><b>baud</b> - Baud rate of the IT-100 bridge - Not Required - default = 9600.</td></tr><tr><td><b>pollPeriod</b> - Period of time in minutes between the poll command being sent to the IT-100 bridge - Not Required - default=1.</td></tr></table></td></tr>
	<tr><td>Panel</td><td><table><tr><td><b>userCode</b> - User code for the DSC alarm panel - Not Required.</td></tr><tr><td><b>suppressAcknowledgementMsgs</b> - Suppress the display of acknowledgement messages when received - Not Required - default = false.</td></tr></table></td></tr>
	<tr><td>Partition</td><td><b>partitionNumber</b> - Partition number (1-8) - Required.</td></tr>
	<tr><td>Zone</td><td><table><tr><td><b>partitionNumber</b> - Partition number (1-8) - Not Required - default=1.</td></tr><tr><td><b>zoneNumber</b> - Zone number (1-64) - Required.</td></tr></table></td></tr>
	<tr><td>Keypad</td><td>No parameters</td></tr>
</table>

Here is an example 'dscalarm.thing' file:

```
Bridge dscalarm:envisalinkbridge:tcpbridge [ ipAddress="192.168.0.100" ]
{
	Thing panel panel
	Thing partition partition1 [ partitionNumber=1 ]
	Thing zone zone1 [ partitionNumber=1, zoneNumber=1 ]
	Thing zone zone9 [ partitionNumber=1, zoneNumber=9 ]
	Thing zone zone10 [ partitionNumber=1, zoneNumber=10 ]
	Thing zone zone11 [ partitionNumber=1, zoneNumber=11 ]
	Thing zone zone12 [ partitionNumber=1, zoneNumber=12 ]
	Thing zone zone13 [ partitionNumber=1, zoneNumber=13 ]
	Thing zone zone14 [ partitionNumber=1, zoneNumber=14 ]
	Thing zone zone15 [ partitionNumber=1, zoneNumber=15 ]
	Thing zone zone21 [ partitionNumber=1, zoneNumber=21 ]
	Thing zone zone22 [ partitionNumber=1, zoneNumber=22 ]
	Thing zone zone23 [ partitionNumber=1, zoneNumber=23 ]
	Thing zone zone24 [ partitionNumber=1, zoneNumber=24 ]
	Thing zone zone25 [ partitionNumber=1, zoneNumber=25 ]
	Thing keypad keypad
}
```

## Channels
DSC Alarm things support a variety of channels as seen below in the following table:

<table>
    <tr><td><b>Channel</b></td><td><b>Item Type</b></td><td><b>Description</b></td></tr>
    <tr><td>bridge_connection</td><td>Switch</td><td>Bridge connection status.</td></tr>
    <tr><td>panel_message</td><td>String</td><td>Event messages received from the DSC Alarm system.</td></tr>
    <tr><td>panel_system_error</td><td>String</td><td>DSC Alarm system error.</td></tr>
    <tr><td>panel_trouble_message</td><td>String</td><td>Displays any trouble messages the panel might send.</td></tr>
    <tr><td>panel_trouble_led</td><td>Switch</td><td>The panel trouble LED is on.</td></tr>
    <tr><td>panel_service_required</td><td>Switch</td><td>Service is required on the panel.</td></tr>
    <tr><td>panel_ac_trouble</td><td>Switch</td><td>The panel has lost AC power.</td></tr>
    <tr><td>panel_telephone_trouble</td><td>Switch</td><td>Telephone line fault.</td></tr>
    <tr><td>panel_ftc_trouble</td><td>Switch</td><td>Failure to communicate with monitoring station.</td></tr>
    <tr><td>panel_zone_fault</td><td>Switch</td><td>There is a fault condition on a zone/sensor.</td></tr>
    <tr><td>panel_zone_tamper</td><td>Switch</td><td>There is a tamper condition on a zone/sensor.</td></tr>
    <tr><td>panel_time_low_battery</td><td>Switch</td><td>There is a low battery condition on a zone/sensor.</td></tr>
    <tr><td>panel_time_loss</td><td>Switch</td><td>Loss of time on the panel.</td></tr>
    <tr><td>panel_time</td><td>DateTime</td><td>DSC Alarm system time and date.</td></tr>
    <tr><td>panel_time_stamp</td><td>Switch</td><td>Turn DSC Alarm message time stamping ON/OFF.</td></tr>
    <tr><td>panel_time_broadcast</td><td>Switch</td><td>Turn DSC Alarm time broadcasting ON/OFF.</td></tr>
    <tr><td>panel_fire_key_alarm</td><td>Switch</td><td>A fire key alarm has happened.</td></tr>
    <tr><td>panel_panic_key_alarm</td><td>Switch</td><td>A panic key alarm has happened.</td></tr>
    <tr><td>panel_aux_key_alarm</td><td>Switch</td><td>An auxiliary key alarm has happened.</td></tr>
    <tr><td>panel_aux_input_alarm</td><td>Switch</td><td>An auxiliary input alarm has happened.</td></tr>
    <tr><td>partition_status</td><td>String</td><td>A partitions current status.</td></tr>
    <tr><td>partition_arm_mode</td><td>Number</td><td>A partitions current arm mode. The possible values are:
<br/>
0=disarmed<br/>
1=armed away<br/>
2=armed stay<br/>
3=away no delay<br/>
4=stay no delay<br/>
</td></tr>
    <tr><td>partition_armed</td><td>Switch</td><td>A partition has been armed.</td></tr>
    <tr><td>partition_entry_delay</td><td>Switch</td><td>A partition is in entry delay mode.</td></tr>
    <tr><td>partition_exit_delay</td><td>Switch</td><td>A partition is in exit delay mode.</td></tr>
    <tr><td>partition_in_alarm</td><td>Switch</td><td>A partition is in alarm.</td></tr>
    <tr><td>partition_opening_closing_mode</td><td>String</td><td>Displays the opening/closing mode of a partition.</td></tr>
    <tr><td>zone_status</td><td>Contact</td><td>A zones general (open/closed) status.</td></tr>
    <tr><td>zone_message</td><td>String</td><td>A zone status message.</td></tr>
    <tr><td>zone_bypass_mode</td><td>Switch</td><td>A zone bypass mode (OFF=Armed, ON=Bypassed).</td></tr>
    <tr><td>zone_in_alarm</td><td>Switch</td><td>A zone is in alarm.</td></tr>
    <tr><td>zone_tamper</td><td>Switch</td><td>A zone tamper condition has happened.</td></tr>
    <tr><td>zone_fault</td><td>Switch</td><td>A zone fault condition has happened.</td></tr>
    <tr><td>zone_tripped</td><td>Switch</td><td>A zone has tripped.</td></tr>
    <tr><td>keypad_ready_led</td><td>Number</td><td>Keypad Ready LED Status. The values are:
<br/>
0=OFF<br/>
1=ON<br/>
2=Flashing<br/>
</td></tr>
    <tr><td>keypad_armed_led</td><td>Number</td><td>Keypad Armed LED Status. The values are:
<br/>
0=OFF<br/>
1=ON<br/>
2=Flashing<br/></td></tr>
    <tr><td>keypad_memory_led</td><td>Number</td><td>Keypad Memory LED Status. The values are:
<br/>
0=OFF<br/>
1=ON<br/>
2=Flashing<br/></td></tr>
    <tr><td>keypad_bypass_led</td><td>Number</td><td>Keypad Bypass LED Status. The values are:
<br/>
0=OFF<br/>
1=ON<br/>
2=Flashing<br/></td></tr>
    <tr><td>keypad_trouble_led</td><td>Number</td><td>Keypad Trouble LED Status. The values are:
<br/>
0=OFF<br/>
1=ON<br/>
2=Flashing<br/></td></tr>
    <tr><td>keypad_program_led</td><td>Number</td><td>Keypad Program LED Status. The values are:
<br/>
0=OFF<br/>
1=ON<br/>
2=Flashing<br/></td></tr>
    <tr><td>keypad_fire_led</td><td>Number</td><td>Keypad Fire LED Status. The values are:
<br/>
0=OFF<br/>
1=ON<br/>
2=Flashing<br/></td></tr>
    <tr><td>keypad_backlight_led</td><td>Number</td><td>Keypad Backlight LED Status. The values are:
<br/>
0=OFF<br/>
1=ON<br/>
2=Flashing<br/></td></tr>
    <tr><td>keypad_ac_led</td><td>Number</td><td>Keypad AC LED Status. The values are:
<br/>
0=OFF<br/>
1=ON<br/>
2=Flashing<br/></td></tr>    
</table>

## Full Example

The following is an example of an item file (dscalarm.item):

```
Group DSCAlarm
Group DSCAlarmPanel (DSCAlarm)
Group DSCAlarmPartitions (DSCAlarm)
Group DSCAlarmZones (DSCAlarm)
Group DSCAlarmKeypads (DSCAlarm)

/* DSC Alarm Items */

Switch BRIDGE_CONNECTION {channel="dscalarm:envisalinkbridge:tcpbridge:bridge_connection"}

/* DSC Alarm Panel Items */
Number PANEL_COMMAND "Panel Commands" (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_command"}
String PANEL_MESSAGE "Panel Message: [%s]" (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_message"}
String PANEL_SYSTEM_ERROR "Panel System Error: [%s]" <"shield-1"> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_system_error"}

String PANEL_TROUBLE_MESSAGE "Panel Trouble Message: [%s]" <"shield-1"> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_trouble_message"}
Switch PANEL_TROUBLE_LED "Panel Trouble LED" <warning> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_trouble_led"}
Switch PANEL_SERVICE_REQUIRED <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_service_required"}
Switch PANEL_AC_TROUBLE <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_ac_trouble"}
Switch PANEL_TELEPHONE_TROUBLE <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_telephone_trouble"}
Switch PANEL_FTC_TROUBLE <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_ftc_trouble"}
Switch PANEL_ZONE_FAULT <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_zone_fault"}
Switch PANEL_ZONE_TAMPER <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_zone_tamper"}
Switch PANEL_ZONE_LOW_BATTERY <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_zone_low_battery"}
Switch PANEL_TIME_LOSS <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_time_loss"}

DateTime PANEL_TIME "Panel Time [%1$tA, %1$tm/%1$td/%1$tY %1tT]" <calendar> (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_time"}
Switch PANEL_TIME_STAMP (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_time_stamp"}
Switch PANEL_TIME_BROADCAST (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_time_broadcast"}

Switch PANEL_FIRE_KEY_ALARM (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_fire_key_alarm"}
Switch PANEL_PANIC_KEY_ALARM (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_panic_key_alarm"}
Switch PANEL_AUX_KEY_ALARM (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_aux_key_alarm"}
Switch PANEL_AUX_INPUT_ALARM (DSCAlarmPanel) {channel="dscalarm:panel:tcpbridge:panel:panel_aux_input_alarm"}

/* DSC Alarm Partition Items */
String PARTITION1_STATUS "Partition 1 Status: [%s]" (DSCAlarmPartitions) {channel="dscalarm:partition:tcpbridge:partition1:partition_status"}
Number PARTITION1_ARM_MODE (DSCAlarmPartitions) {channel="dscalarm:partition:tcpbridge:partition1:partition_arm_mode"}
String PARTITION1_OPENING_CLOSING_MODE "Partition 1 Opening/Closing Mode: [%s]" (DSCAlarmPartitions) {channel="dscalarm:partition:tcpbridge:partition_opening_closing_mode"}

Switch PARTITION1_ARMED (DSCAlarmPartitions) {channel="dscalarm:partition:tcpbridge:partition1:partition_armed"}
Switch PARTITION1_ENTRY_DELAY (DSCAlarmPartitions) {channel="dscalarm:partition:tcpbridge:partition1:partition_entry_delay"}
Switch PARTITION1_EXIT_DELAY (DSCAlarmPartitions) {channel="dscalarm:partition:tcpbridge:partition1:partition_exit_delay"}
Switch PARTITION1_IN_ALARM (DSCAlarmPartitions) {channel="dscalarm:partition:tcpbridge:partition1:partition_in_alarm"}

/* DSC Alarm Zones Items */
Contact ZONE1_STATUS "Tamper Switch" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone1:zone_status"}
Switch ZONE1_BYPASS_MODE "Tamper Switch Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone1:zone_bypass_mode"}
String ZONE1_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone1:zone_message"}
Switch ZONE1_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone1:zone_in_alarm"}
Switch ZONE1_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone1:zone_tamper"}
Switch ZONE1_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone1:zone_fault"}
Switch ZONE1_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone1:zone_tripped"}

Contact ZONE9_STATUS "Front Door Sensor" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone9:zone_status"}
Switch ZONE9_BYPASS_MODE "Front Door Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone9:zone_bypass_mode"}
String ZONE9_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone9:zone_amessage"}
Switch ZONE9_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone9:zone_in_alarm"}
Switch ZONE9_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone9:zone_tamper"}
Switch ZONE9_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone9:zone_fault"}
Switch ZONE9_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone9:zone_tripped"}

Contact ZONE10_STATUS "Deck Door Sensor" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone10:zone_status"}
Switch ZONE10_BYPASS_MODE "Deck Door Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone10:zone_bypass_mode"}
String ZONE10_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone10:zone_message"}
Switch ZONE10_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone10:zone_in_alarm"}
Switch ZONE10_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone10:zone_tamper"}
Switch ZONE10_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone10:zone_fault"}
Switch ZONE10_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone10:zone_tripped"}

Contact ZONE11_STATUS "Back Door Sensor" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone11:zone_status"}
Switch ZONE11_BYPASS_MODE "Back Door Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone11:zone_bypass_mode"}
String ZONE11_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone11:zone_message"}
Switch ZONE11_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone11:zone_in_alarm"}
Switch ZONE11_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone11:zone_tamper"}
Switch ZONE11_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone11:zone_fault"}
Switch ZONE11_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone11:zone_tripped"}

Contact ZONE12_STATUS "Side Door Sensor" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone12:zone_status"}
Switch ZONE12_BYPASS_MODE "Side Door Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone12:zone_bypass_mode"}
String ZONE12_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone12:zone_message"}
Switch ZONE12_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone12:zone_in_alarm"}
Switch ZONE12_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone12:zone_tamper"}
Switch ZONE12_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone12:zone_fault"}
Switch ZONE12_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone12:zone_tripped"}

Contact ZONE13_STATUS "Garage Door 1 Sensor" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone13:zone_status"}
Switch ZONE13_BYPASS_MODE "Garage Door 1 Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone13:zone_bypass_mode"}
String ZONE13_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone13:zone_message"}
Switch ZONE13_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone13:zone_in_alarm"}
Switch ZONE13_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone13:zone_tamper"}
Switch ZONE13_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone13:zone_fault"}
Switch ZONE13_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone13:zone_tripped"}

Contact ZONE14_STATUS "Garage Door 2 Sensor" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone14:zone_status"}
Switch ZONE14_BYPASS_MODE "Garage Door 2 Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone14:zone_bypass_mode"}
String ZONE14_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone14:zone_message"}
Switch ZONE14_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone14:zone_in_alarm"}
Switch ZONE14_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone14:zone_tamper"}
Switch ZONE14_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone14:zone_fault"}
Switch ZONE14_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone14:zone_tripped"}

Contact ZONE15_STATUS "Garage Window Sensor" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone15:zone_status"}
Switch ZONE15_BYPASS_MODE "Garage Window Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone15:zone_bypass_mode"}
String ZONE15_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone15:zone_message"}
Switch ZONE15_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone15:zone_in_alarm"}
Switch ZONE15_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone15:zone_tamper"}
Switch ZONE15_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone15:zone_fault"}
Switch ZONE15_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone15:zone_tripped"}

Contact ZONE21_STATUS "Family Room Motion Sensor" (DSCAlarmZones,  FamilyRoom) {channel="dscalarm:zone:tcpbridge:zone21:zone_status"}
Switch ZONE21_BYPASS_MODE "Family Room Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone21:zone_bypass_mode"}
String ZONE21_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone21:zone_message"}
Switch ZONE21_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone21:zone_in_alarm"}
Switch ZONE21_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone21:zone_tamper"}
Switch ZONE21_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone21:zone_fault"}
Switch ZONE21_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone21:zone_tripped"}

Contact ZONE22_STATUS "Office Motion Sensor" (DSCAlarmZones,  Office) {channel="dscalarm:zone:tcpbridge:zone22:zone_status"}
Switch ZONE22_BYPASS_MODE "Office Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone22:zone_bypass_mode"}
String ZONE22_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone22:zone_message"}
Switch ZONE22_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone22:zone_in_alarm"}
Switch ZONE22_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone22:zone_tamper"}
Switch ZONE22_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone22:zone_fault"}
Switch ZONE22_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone22:zone_tripped"}

Contact ZONE23_STATUS "Dining Room Motion Sensor" (DSCAlarmZones,  DiningRoom) {channel="dscalarm:zone:tcpbridge:zone23:zone_status"}
Switch ZONE23_BYPASS_MODE "Dining Room Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone23:zone_bypass_mode"}
String ZONE23_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone23:zone_message"}
Switch ZONE23_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone23:zone_in_alarm"}
Switch ZONE23_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone23:zone_tamper"}
Switch ZONE23_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone23:zone_fault"}
Switch ZONE23_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone23:zone_tripped"}

Contact ZONE24_STATUS "Living Room Motion Sensor" (DSCAlarmZones,  LivingRoom) {channel="dscalarm:zone:tcpbridge:zone24:zone_status"}
Switch ZONE24_BYPASS_MODE "Living Room Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone24:zone_bypass_mode"}
String ZONE24_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone24:zone_message"}
Switch ZONE24_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone24:zone_in_alarm"}
Switch ZONE24_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone24:zone_tamper"}
Switch ZONE24_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone24:zone_fault"}
Switch ZONE24_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone24:zone_tripped"}

Contact ZONE25_STATUS "Utility Room Motion Sensor" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone25:zone_status"}
Switch ZONE25_BYPASS_MODE "Utility Room Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone25:zone_bypass_mode"}
String ZONE25_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone25:zone_message"}
Switch ZONE25_IN_ALARM (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone25:zone_in_alarm"}
Switch ZONE25_TAMPER (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone25:zone_tamper"}
Switch ZONE25_FAULT (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone25:zone_fault"}
Switch ZONE25_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:tcpbridge:zone25:zone_tripped"}

/* DSC Alarm Keypad Items */
Number KEYPAD_READY_LED "Ready LED Status: [%d]" <readyLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:tcpbridge:keypad:keypad_ready_led"}
Number KEYPAD_ARMED_LED "Armed LED Status: [%d]" <armedLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:tcpbridge:keypad:keypad_armed_led"}
Number KEYPAD_MEMORY_LED "Memory LED Status: [%d]" <memoryLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:tcpbridge:keypad:keypad_memory_led"}
Number KEYPAD_BYPASS_LED "Bypass LED Status: [%d]" <bypassLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:tcpbridge:keypad:keypad_bypass_led"}
Number KEYPAD_TROUBLE_LED "Trouble LED Status: [%d]" <trouble> (DSCAlarmKeypads) {channel="dscalarm:keypad:tcpbridge:keypad:keypad_trouble_led"}
Number KEYPAD_PROGRAM_LED "Program LED Status: [%d]" <programLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:tcpbridge:keypad:keypad_program_led"}
Number KEYPAD_FIRE_LED "Fire LED Status: [%d]" <fireLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:tcpbridge:keypad:keypad_fire_led"}
Number KEYPAD_BACKLIGHT_LED "Backlight LED Status: [%d]" <backlightLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:tcpbridge:keypad:keypad_backlight_led"}
Number KEYPAD_AC_LED "AC LED Status: [%d]" <acLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:tcpbridge:keypad:keypad_ac_led"}
```

Here is an example sitemap:

```
Frame label="Alarm System" {
	Text label="DSC Alarm System" icon="MyImages/DSC" {
		Frame label="Panel" {
			Switch item=PANEL_CONNECTION label="Panel Connection" mappings=[1="Connected", 0="Disconnected"]
			Text item=PANEL_MESSAGE icon="MyImages/arrow_down"
			Selection item=PANEL_COMMAND icon="MyImages/arrow_up" mappings=[0="Poll", 1="Status Report", 2="Labels Request (Serial Only)", 8="Dump Zone Timers (TCP Only)", 10="Set Time/Date", 200="Send User Code"]
			Text item=PANEL_TIME {
				Switch item=PANEL_TIME_STAMP label="Panel Time Stamp"
				Switch item=PANEL_TIME_BROADCAST label="Panel Time Broadcast"
			}
			
			Text item=PANEL_SYSTEM_ERROR icon="MyImages/system-error"
							
			Text item=PANEL_TROUBLE_LED label="Panel Trouble Condition" {
				Text item=PANEL_TROUBLE_MESSAGE icon="shield-0"
				Text item=PANEL_SERVICE_REQUIRED label="Service Required"
				Text item=PANEL_AC_TROUBLE label="AC Trouble"
				Text item=PANEL_TELEPHONE_TROUBLE label="Telephone Line Trouble"
				Text item=PANEL_FTC_TROUBLE label="Failed to Communicate Trouble"
				Text item=PANEL_ZONE_FAULT label="Zone Fault"
				Text item=PANEL_ZONE_TAMPER label="Zone Tamper"
				Text item=PANEL_ZONE_LOW_BATTERY label="Zone Low Battery"
				Text item=PANEL_TIME_LOSS label="Panel Time Loss"					
			}
		}

		Frame label="Partitions" {
				Text item=PARTITION1_STATUS icon="shield-1" {
					Switch item=PARTITION1_ARM_MODE label="Partition 1 Arm Options" icon="shield-1" mappings=[0="Disarm", 1="Away", 2="Stay", 3="Zero"]
					Text item=PARTITION1_OPENING_CLOSING_MODE icon="shield-1"
				}
		}

		Frame label="Keypad" {
			Text label="Keypad LED Status" icon="MyImages/DSCKeypad" {
				Text item=KEYPAD_READY_LED label="Ready LED Status"
				Text item=KEYPAD_ARMED_LED label="Armed LED Status"
				Text item=KEYPAD_MEMORY_LED label="Memory LED Status"
				Text item=KEYPAD_BYPASS_LED label="Bypass LED Status"
				Text item=KEYPAD_TROUBLE_LED label="Trouble LED Status"
				Text item=KEYPAD_PROGRAM_LED label="Program LED Status"
				Text item=KEYPAD_FIRE_LED label="Fire LED Status"
				Text item=KEYPAD_BACKLIGHT_LED label="Backlight LED Status"
				Text item=KEYPAD_AC_LED label="AC LED Status"
			}
		}

		Frame label="Zones" {
			Text item=ZONE1_GENERAL_STATUS {
				Switch item=ZONE1_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE1_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE1_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE1_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}

			Text item=ZONE9_GENERAL_STATUS {
				Switch item=ZONE9_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE9_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE9_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE9_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE10_GENERAL_STATUS {
				Switch item=ZONE10_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE10_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE10_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE10_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE11_GENERAL_STATUS {
				Switch item=ZONE11_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE11_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE11_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE11_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE12_GENERAL_STATUS {
				Switch item=ZONE12_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE12_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE12_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE12_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE13_GENERAL_STATUS {
				Switch item=ZONE13_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE13_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE13_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE13_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE14_GENERAL_STATUS {
				Switch item=ZONE14_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE14_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE14_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE14_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE15_GENERAL_STATUS {
				Switch item=ZONE15_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE15_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE15_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE15_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE21_GENERAL_STATUS {
				Switch item=ZONE21_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE21_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE21_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE21_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE22_GENERAL_STATUS {
				Switch item=ZONE22_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE22_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE22_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE22_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE23_GENERAL_STATUS {
				Switch item=ZONE23_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE23_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE23_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE23_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE24_GENERAL_STATUS {
				Switch item=ZONE24_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE24_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE24_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE24_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
			Text item=ZONE25_GENERAL_STATUS {
				Switch item=ZONE25_BYPASS_MODE icon="MyImages/Zone-Alarm" mappings=[0="Armed", 1="Bypassed"]
				Frame label="Other Status:" {
					Text item=ZONE25_ALARM_STATUS icon="MyImages/Status-warning"
					Text item=ZONE25_FAULT_STATUS icon="MyImages/Status-warning"
					Text item=ZONE25_TAMPER_STATUS icon="MyImages/Status-warning"
				}
			}
		}
	}
}
```
