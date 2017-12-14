# DSC Alarm Binding

The DSC PowerSeries Alarm System is a popular do-it-yourself home security system, which can be monitored and controlled remotely through a standard web-browser or mobile device.

This is the binding for the DSC PowerSeries Alarm System, utilizing either the EyezOn Envisalink 4/3/2DS interface or the DSC IT-100 RS-232 interface.
It provides connectivity to the DSC Alarm panel via a TCP socket connection to the EyesOn Envisalink 4/3/2DS interface or a RS-232 serial connection to the DSC IT-100 interface.  Additionally, their is provision to connect to the DSC IT-100 interface through a TCP serial server.

## Supported Things

This binding supports the following Thing types

<table>
<tr><td><b>Thing</b></td><td><b>Thing Type</b></td><td><b>Description</b></td></tr>
<tr><td>envisalink</td><td>Bridge</td><td>The EyezOn Envisalink 3/2DS interface.</td></tr>
<tr><td>it100</td><td>Bridge</td><td>The DSC IT-100 RS-232 interface.</td></tr>
<tr><td>tcpserver</td><td>Bridge</td><td>The DSC IT-100 TCP Server network interface.</td></tr>
<tr><td>panel</td><td>Thing</td><td>The basic representation of the DSC Alarm System.</td></tr>
<tr><td>partition</td><td>Thing</td><td>Represents a controllable area within a DSC Alarm system.</td></tr>
<tr><td>zone</td><td>Thing</td><td>Represents a physical device such as a door, window, or motion sensor.</td></tr>
<tr><td>keypad</td><td>Thing</td><td>Represents the central administrative unit.</td></tr>
</table>

## Binding Configuration

There are essentially no overall binding configuration settings that need to be set.
Most settings are through thing configuration parameters.

## Discovery

The DSC Alarm binding incorporates several discovery modes in order to find DSC Alarm systems.  First, there is the Envisalink bridge discovery mode which performs a network query for any Envisalink adapters and adds them to the discovery inbox.
Second, there is The IT-100 bridge discovery mode which will search serial ports for any IT-100 adapters and add them to the discovery inbox.
The bridge discovery modes are started manually through PaperUI.  Third, after a bridge is discovered and available to openHAB, the binding will attempt to discover DSC Alarm things and add them to the discovery inbox.  The TCP Server bridge does not implement bridge discovery but will utilize thing discovery once it is online.

Note: The Envisalink Bridge discovery does a TCP scan across your local network to find the interface.  This may create issues on the network so it is suggested that caution be used when trying this discovery.  The recommended method would be to manually add and configure the bridge through the 'dscalarm.thing' file or the PaperUI.  And then allow the binding to discover the DSC Alarm things.



## Thing Configuration

DSC Alarm things can be configured either through the online configuration utility via discovery, or manually through the 'dscalarm.things' configuration file.  The following table shows the available configuration parameters for each thing.

<table>
	<tr><td><b>Thing</b></td><td><b>Configuration Parameters</b></td></tr>
	<tr><td>envisalink</td><td><table><tr><td><b>ipAddress</b> - IP address for the Envisalink adapter - Required.</td></tr><tr><td><b>port</b> - TCP port for the Envisalink adapter - Not Required - default = 4025.</td></tr><tr><td><b>password</b> - Password to login to the Envisalink bridge - Not Required.</td></tr><tr><td><b>connectionTimeout</b> - TCP socket connection timeout in milliseconds - Not Required - default=5000.<br/></td></tr><tr><td><b>pollPeriod</b> - Period of time in minutes between the poll command being sent to the Envisalink bridge - Not Required - default=1.</td></tr></table></td></tr>
	<tr><td>it100</td><td><table><tr><td><b>serialPort</b> - Serial port for the IT-100s bridge - Required.</td></tr><tr><td><b>baud</b> - Baud rate of the IT-100 bridge - Not Required - default = 9600.</td></tr><tr><td><b>pollPeriod</b> - Period of time in minutes between the poll command being sent to the IT-100 bridge - Not Required - default=1.</td></tr></table></td></tr>
    <tr><td>tcpserver</td><td><table><tr><td><b>ipAddress</b> - IP address for the TCP Server - Required.</td></tr><tr><td><b>port</b> - TCP port for the TCP Server - Required.</td></tr><tr><td><b>connectionTimeout</b> - TCP socket connection timeout in milliseconds - Not Required - default=5000.<br/></td></tr><tr><td><b>pollPeriod</b> - Period of time in minutes between the poll command being sent to the TCP Server bridge - Not Required - default=1.</td></tr><tr><td><b>protocol</b> - The protocol used to interact with the DSC Alarm. Valid values are 1 for IT100 API or 2 for Envisalink TPI. The default is 1. - Not Required.</td></tr></table></td></tr>
    <tr><td>panel</td><td><table><tr><td><b>userCode</b> - User code for the DSC alarm panel - Not Required.</td></tr><tr><td><b>suppressAcknowledgementMsgs</b> - Suppress the display of acknowledgement messages when received - Not Required - default = false.</td></tr></table></td></tr>
	<tr><td>partition</td><td><b>partitionNumber</b> - Partition number (1-8) - Required.</td></tr>
	<tr><td>zone</td><td><table><tr><td><b>partitionNumber</b> - Partition number (1-8) - Not Required - default=1.</td></tr><tr><td><b>zoneNumber</b> - Zone number (1-64) - Required.</td></tr></table></td></tr>
	<tr><td>keypad</td><td>No parameters</td></tr>
</table>

The binding can be configured manually if discovery is not used.  A thing configuration file in the format 'bindingName.things' would need to be created, and placed in the 'conf/things' folder.  Here is an example of a thing configuration file called 'dscalarm.things':

```
Bridge dscalarm:envisalink:MyBridgeName [ ipAddress="192.168.0.100" ]
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
    <tr><td>bridge_reset</td><td>Switch</td><td>Reset the bridge connection.</td></tr>
    <tr><td>send_command</td><td>Switch</td><td>Send a DSC Alarm command.</td></tr>
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
    <tr><td>panel_zone_low_battery</td><td>Switch</td><td>There is a low battery condition on a zone/sensor.</td></tr>
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
    <tr><td>keypad_lcd_update</td><td>String</td><td>Text Changes of the IT-100 LCD Menu.</td></tr>
    <tr><td>keypad_lcd_cursor</td><td>String</td><td>LCD Cursor Position for The IT-100</td></tr>    
</table>

##Example

The following is an example of an item file (dscalarm.items):

```
Group DSCAlarm
Group DSCAlarmPanel (DSCAlarm)
Group DSCAlarmPartitions (DSCAlarm)
Group DSCAlarmZones (DSCAlarm)
Group DSCAlarmKeypads (DSCAlarm)

/* Groups By Device Type */
Group:Contact:OR(OPEN, CLOSED) DSCAlarmDoorWindow <door>
Group:Contact:OR(OPEN, CLOSED) DSCAlarmMotion <motionDetector>
Group:Contact:OR(OPEN, CLOSED) DSCAlarmSmoke <smokeDetector>

/* DSC Alarm Items */

Switch BRIDGE_CONNECTION {channel="dscalarm:envisalink:MyBridgeName:bridge_reset"}
String SEND_DSC_ALARM_COMMAND "Send a DSC Alarm Command" {channel="dscalarm:envisalink:MyBridgeName:send_command"}

/* DSC Alarm Panel Items */
String PANEL_MESSAGE "Panel Message: [%s]" (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_message"}
Number PANEL_COMMAND "Panel Commands" (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_command"}
String PANEL_SYSTEM_ERROR "Panel System Error: [%s]" (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_system_error"}

String PANEL_TROUBLE_MESSAGE "Panel Trouble Message: [%s]" <"shieldGreen"> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_trouble_message"}
Switch PANEL_TROUBLE_LED "Panel Trouble LED" <warning> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_trouble_led"}
Switch PANEL_SERVICE_REQUIRED <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_service_required"}
Switch PANEL_AC_TROUBLE <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_ac_trouble"}
Switch PANEL_TELEPHONE_TROUBLE <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_telephone_trouble"}
Switch PANEL_FTC_TROUBLE <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_ftc_trouble"}
Switch PANEL_ZONE_FAULT <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_zone_fault"}
Switch PANEL_ZONE_TAMPER <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_zone_tamper"}
Switch PANEL_ZONE_LOW_BATTERY <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_zone_low_battery"}
Switch PANEL_TIME_LOSS <yellowLED> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_time_loss"}

DateTime PANEL_TIME "Panel Time [%1$tA, %1$tm/%1$td/%1$tY %1tT]" <calendar> (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_time"}
Switch PANEL_TIME_STAMP (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_time_stamp"}
Switch PANEL_TIME_BROADCAST (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_time_broadcast"}

Switch PANEL_FIRE_KEY_ALARM (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_fire_key_alarm"}
Switch PANEL_PANIC_KEY_ALARM (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_panic_key_alarm"}
Switch PANEL_AUX_KEY_ALARM (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_aux_key_alarm"}
Switch PANEL_AUX_INPUT_ALARM (DSCAlarmPanel) {channel="dscalarm:panel:MyBridgeName:panel:panel_aux_input_alarm"}

/* DSC Alarm Partition Items */
String PARTITION1_STATUS "Partition 1 Status: [%s]" (DSCAlarmPartitions) {channel="dscalarm:partition:MyBridgeName:partition1:partition_status"}
Number PARTITION1_ARM_MODE "Partition 1 Arm Mode: [%d]" (DSCAlarmPartitions) {channel="dscalarm:partition:MyBridgeName:partition1:partition_arm_mode"}
Switch PARTITION1_ARMED (DSCAlarmPartitions) {channel="dscalarm:partition:MyBridgeName:partition1:partition_armed"}
Switch PARTITION1_ENTRY_DELAY (DSCAlarmPartitions) {channel="dscalarm:partition:MyBridgeName:partition1:partition_entry_delay"}
Switch PARTITION1_EXIT_DELAY (DSCAlarmPartitions) {channel="dscalarm:partition:MyBridgeName:partition1:partition_exit_delay"}
Switch PARTITION1_IN_ALARM (DSCAlarmPartitions) {channel="dscalarm:partition:MyBridgeName:partition1:partition_in_alarm"}
String PARTITION1_OPENING_CLOSING_MODE "Opening/Closing Mode: [%s]" (DSCAlarmPartitions) {channel="dscalarm:partition:MyBridgeName:partition1:partition_opening_closing_mode"}

/* DSC Alarm Zones Items */
Contact ZONE1_STATUS "Tamper Switch (Zone 1)" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone1:zone_status"}
String ZONE1_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone1:zone_message"}
Switch ZONE1_BYPASS_MODE "Tamper Switch Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone1:zone_bypass_mode"}
Switch ZONE1_IN_ALARM "Zone 1 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone1:zone_in_alarm"}
Switch ZONE1_TAMPER "Zone 1 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone1:zone_tamper"}
Switch ZONE1_FAULT "Zone 1 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone1:zone_fault"}
Switch ZONE1_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone1:zone_tripped"}

Contact ZONE9_STATUS "Front Door Sensor (Zone 9)" <door> (DSCAlarmZones, FrontFoyer, DSCAlarmDoorWindow) {channel="dscalarm:zone:MyBridgeName:zone9:zone_status"}
String ZONE9_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone9:zone_message"}
Switch ZONE9_BYPASS_MODE "Front Door Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone9:zone_bypass_mode"}
Switch ZONE9_IN_ALARM "Zone 9 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone9:zone_in_alarm"}
Switch ZONE9_TAMPER "Zone 9 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone9:zone_tamper"}
Switch ZONE9_FAULT "Zone 9 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone9:zone_fault"}
Switch ZONE9_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone9:zone_tripped"}

Contact ZONE10_STATUS "Deck Door Sensor (Zone 10)" <door> (DSCAlarmZones, FamilyRoom, DSCAlarmDoorWindow) {channel="dscalarm:zone:MyBridgeName:zone10:zone_status"}
String ZONE10_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone10:zone_message"}
Switch ZONE10_BYPASS_MODE "Deck Door Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone10:zone_bypass_mode"}
Switch ZONE10_IN_ALARM "Zone 10 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone10:zone_in_alarm"}
Switch ZONE10_TAMPER "Zone 10 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone10:zone_tamper"}
Switch ZONE10_FAULT "Zone 10 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone10:zone_fault"}
Switch ZONE10_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone10:zone_tripped"}

Contact ZONE11_STATUS "Back Door Sensor (Zone 11)" <door> (DSCAlarmZones, UtilityRoom, DSCAlarmDoorWindow) {channel="dscalarm:zone:MyBridgeName:zone11:zone_status"}
String ZONE11_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone11:zone_message"}
Switch ZONE11_BYPASS_MODE "Back Door Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone11:zone_bypass_mode"}
Switch ZONE11_IN_ALARM "Zone 11 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone11:zone_in_alarm"}
Switch ZONE11_TAMPER "Zone 11 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone11:zone_tamper"}
Switch ZONE11_FAULT "Zone 11 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone11:zone_fault"}
Switch ZONE11_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone11:zone_tripped"}

Contact ZONE12_STATUS "Side Door Sensor (Zone 12)" <door> (DSCAlarmZones, SideFoyer, DSCAlarmDoorWindow) {channel="dscalarm:zone:MyBridgeName:zone12:zone_status"}
String ZONE12_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone12:zone_message"}
Switch ZONE12_BYPASS_MODE "Side Door Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone12:zone_bypass_mode"}
Switch ZONE12_IN_ALARM "Zone 12 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone12:zone_in_alarm"}
Switch ZONE12_TAMPER "Zone 12 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone12:zone_tamper"}
Switch ZONE12_FAULT "Zone 12 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone12:zone_fault"}
Switch ZONE12_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone12:zone_tripped"}

Contact ZONE13_STATUS "Garage Door 1 Sensor (Zone 13)" <door> (DSCAlarmZones, Garage, DSCAlarmDoorWindow) {channel="dscalarm:zone:MyBridgeName:zone13:zone_status"}
String ZONE13_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone13:zone_message"}
Switch ZONE13_BYPASS_MODE "Garage Door 1 Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone13:zone_bypass_mode"}
Switch ZONE13_IN_ALARM "Zone 13 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone13:zone_in_alarm"}
Switch ZONE13_TAMPER "Zone 13 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone13:zone_tamper"}
Switch ZONE13_FAULT "Zone 13 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone13:zone_fault"}
Switch ZONE13_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone13:zone_tripped"}

Contact ZONE14_STATUS "Garage Door 2 Sensor (Zone 14)" <garagedoor> (DSCAlarmZones, Garage, DSCAlarmDoorWindow) {channel="dscalarm:zone:MyBridgeName:zone14:zone_status"}
String ZONE14_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone14:zone_message"}
Switch ZONE14_BYPASS_MODE "Garage Door 2 Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone14:zone_bypass_mode"}
Switch ZONE14_IN_ALARM "Zone 14 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone14:zone_in_alarm"}
Switch ZONE14_TAMPER "Zone 14 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone14:zone_tamper"}
Switch ZONE14_FAULT "Zone 14 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone14:zone_fault"}
Switch ZONE14_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone14:zone_tripped"}

Contact ZONE15_STATUS "Garage Window Sensor (Zone 15)" (DSCAlarmZones, Garage, DSCAlarmDoorWindow) {channel="dscalarm:zone:MyBridgeName:zone15:zone_status"}
String ZONE15_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone15:zone_message"}
Switch ZONE15_BYPASS_MODE "Garage Window Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone15:zone_bypass_mode"}
Switch ZONE15_IN_ALARM "Zone 15 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone15:zone_in_alarm"}
Switch ZONE15_TAMPER "Zone 15 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone15:zone_tamper"}
Switch ZONE15_FAULT "Zone 15 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone15:zone_fault"}
Switch ZONE15_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone15:zone_tripped"}

Contact ZONE21_STATUS "Family Room Motion Sensor (Zone 21)" <motionDetector> (DSCAlarmZones,  FamilyRoom, DSCAlarmMotion) {channel="dscalarm:zone:MyBridgeName:zone21:zone_status"}
String ZONE21_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone21:zone_message"}
Switch ZONE21_BYPASS_MODE "Family Room Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone21:zone_bypass_mode"}
Switch ZONE21_IN_ALARM "Zone 21 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone21:zone_in_alarm"}
Switch ZONE21_TAMPER "Zone 21 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone21:zone_tamper"}
Switch ZONE21_FAULT "Zone 21 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone21:zone_fault"}
Switch ZONE21_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone21:zone_tripped"}

Contact ZONE22_STATUS "Office Motion Sensor (Zone 22)" <motionDetector> (DSCAlarmZones,  Office, DSCAlarmMotion) {channel="dscalarm:zone:MyBridgeName:zone22:zone_status"}
String ZONE22_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone22:zone_message"}
Switch ZONE22_BYPASS_MODE "Office Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone22:zone_bypass_mode"}
Switch ZONE22_IN_ALARM "Zone 22 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone22:zone_in_alarm"}
Switch ZONE22_TAMPER "Zone 22 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone22:zone_tamper"}
Switch ZONE22_FAULT "Zone 22 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone22:zone_fault"}
Switch ZONE22_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone22:zone_tripped"}

Contact ZONE23_STATUS "Dining Room Motion Sensor (Zone 23)" <motionDetector> (DSCAlarmZones, DiningRoom, DSCAlarmMotion) {channel="dscalarm:zone:MyBridgeName:zone23:zone_status"}
String ZONE23_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone23:zone_message"}
Switch ZONE23_BYPASS_MODE "Dining Room Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone23:zone_bypass_mode"}
Switch ZONE23_IN_ALARM "Zone 23 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone23:zone_in_alarm"}
Switch ZONE23_TAMPER "Zone 23 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone23:zone_tamper"}
Switch ZONE23_FAULT "Zone 23 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone23:zone_fault"}
Switch ZONE23_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone23:zone_tripped"}

Contact ZONE24_STATUS "Living Room Motion Sensor (Zone 24)" <motionDetector> (DSCAlarmZones,  LivingRoom, DSCAlarmMotion) {channel="dscalarm:zone:MyBridgeName:zone24:zone_status"}
String ZONE24_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone24:zone_message"}
Switch ZONE24_BYPASS_MODE "Living Room Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone24:zone_bypass_mode"}
Switch ZONE24_IN_ALARM "Zone 24 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone24:zone_in_alarm"}
Switch ZONE24_TAMPER "Zone 24 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone24:zone_tamper"}
Switch ZONE24_FAULT "Zone 24 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone24:zone_fault"}
Switch ZONE24_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone24:zone_tripped"}

Contact ZONE25_STATUS "Utility Room Motion Sensor (Zone 25)" <motionDetector> (DSCAlarmZones,  UtilityRoom, DSCAlarmMotion) {channel="dscalarm:zone:MyBridgeName:zone25:zone_status"}
String ZONE25_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone25:zone_message"}
Switch ZONE25_BYPASS_MODE "Utility Room Motion Sensor Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone25:zone_bypass_mode"}
Switch ZONE25_IN_ALARM "Zone 25 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone25:zone_in_alarm"}
Switch ZONE25_TAMPER "Zone 25 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone25:zone_tamper"}
Switch ZONE25_FAULT "Zone 25 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone25:zone_fault"}
Switch ZONE25_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone25:zone_tripped"}

Contact ZONE51_STATUS "Utility Room Smoke Detector (Zone 51)" <smokeDetector> (DSCAlarmZones,  UtilityRoom, DSCAlarmSmoke) {channel="dscalarm:zone:MyBridgeName:zone51:zone_status"}
String ZONE51_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone51:zone_message"}
Switch ZONE51_BYPASS_MODE "Utility Room Smoke Detector Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone51:zone_bypass_mode"}
Switch ZONE51_IN_ALARM "Zone 51 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone51:zone_in_alarm"}
Switch ZONE51_TAMPER "Zone 51 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone51:zone_tamper"}
Switch ZONE51_FAULT "Zone 51 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone51:zone_fault"}
Switch ZONE51_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone51:zone_tripped"}

Contact ZONE52_STATUS "Dining Room Smoke Detector (Zone 52)" <smokeDetector> (DSCAlarmZones, DiningRoom, DSCAlarmSmoke) {channel="dscalarm:zone:MyBridgeName:zone52:zone_status"}
String ZONE52_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone52:zone_message"}
Switch ZONE52_BYPASS_MODE "Dining Room Smoke Detector Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone52:zone_bypass_mode"}
Switch ZONE52_IN_ALARM "Zone 52 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone52:zone_in_alarm"}
Switch ZONE52_TAMPER "Zone 52 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone52:zone_tamper"}
Switch ZONE52_FAULT "Zone 52 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone52:zone_fault"}
Switch ZONE52_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone52:zone_tripped"}

Contact ZONE53_STATUS "Front Foyer Smoke Detector (Zone 53)" <smokeDetector> (DSCAlarmZones, FrontFoyer, DSCAlarmSmoke) {channel="dscalarm:zone:MyBridgeName:zone53:zone_status"}
String ZONE53_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone53:zone_message"}
Switch ZONE53_BYPASS_MODE "Front Foyer Smoke Detector Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone53:zone_bypass_mode"}
Switch ZONE53_IN_ALARM "Zone 53 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone53:zone_in_alarm"}
Switch ZONE53_TAMPER "Zone 53 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone53:zone_tamper"}
Switch ZONE53_FAULT "Zone 53 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone53:zone_fault"}
Switch ZONE53_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone53:zone_tripped"}

Contact ZONE54_STATUS "Upstairs Hall Smoke Detector (Zone 54)" <smokeDetector> (DSCAlarmZones, UpstairsHall, DSCAlarmSmoke) {channel="dscalarm:zone:MyBridgeName:zone54:zone_status"}
String ZONE54_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone54:zone_message"}
Switch ZONE54_BYPASS_MODE "Upstairs Hall Smoke Detector Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone54:zone_bypass_mode"}
Switch ZONE54_IN_ALARM "Zone 54 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone54:zone_in_alarm"}
Switch ZONE54_TAMPER "Zone 54 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone54:zone_tamper"}
Switch ZONE54_FAULT "Zone 54 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone54:zone_fault"}
Switch ZONE54_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone54:zone_tripped"}

Contact ZONE55_STATUS "Master Bedroom Smoke Detector (Zone 55)" <smokeDetector> (DSCAlarmZones, Bedroom, DSCAlarmSmoke) {channel="dscalarm:zone:MyBridgeName:zone55:zone_status"}
String ZONE55_MESSAGE "Zone Message: [%s]" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone55:zone_message"}
Switch ZONE55_BYPASS_MODE "Master Bedroom Smoke Detector Bypass Mode" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone55:zone_bypass_mode"}
Switch ZONE55_IN_ALARM "Zone 55 Alarm Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone55:zone_in_alarm"}
Switch ZONE55_TAMPER "Zone 55 Tamper Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone55:zone_tamper"}
Switch ZONE55_FAULT "Zone 55 Fault Condition" (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone55:zone_fault"}
Switch ZONE55_TRIPPED (DSCAlarmZones) {channel="dscalarm:zone:MyBridgeName:zone55:zone_tripped"}

/* DSC Alarm Keypad Items */
Number KEYPAD_READY_LED "Ready LED Status" <readyLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:MyBridgeName:keypad:keypad_ready_led"}
Number KEYPAD_ARMED_LED "Armed LED Status" <armedLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:MyBridgeName:keypad:keypad_armed_led"}
Number KEYPAD_MEMORY_LED "Memory LED Status" <memoryLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:MyBridgeName:keypad:keypad_memory_led"}
Number KEYPAD_BYPASS_LED "Bypass LED Status" <bypassLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:MyBridgeName:keypad:keypad_bypass_led"}
Number KEYPAD_TROUBLE_LED "Trouble LED Status" <troubleLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:MyBridgeName:keypad:keypad_trouble_led"}
Number KEYPAD_PROGRAM_LED "Program LED Status" <programLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:MyBridgeName:keypad:keypad_program_led"}
Number KEYPAD_FIRE_LED "Fire LED Status" <fireLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:MyBridgeName:keypad:keypad_fire_led"}
Number KEYPAD_BACKLIGHT_LED "Backlight LED Status" <backlightLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:MyBridgeName:keypad:keypad_backlight_led"}
Number KEYPAD_AC_LED "AC LED Status" <acLED> (DSCAlarmKeypads) {channel="dscalarm:keypad:MyBridgeName:keypad:keypad_ac_led"}
```

Here is an example sitemap:

```
    Frame label="Alarm System" {
        Text label="DSC Alarm System" {
            Frame label="Panel" {
                Switch item=BRIDGE_CONNECTION label="Panel Connection" mappings=[ON="Connected", OFF="Disconnected"]
                Text item=PANEL_MESSAGE
                Selection item=PANEL_COMMAND mappings=[0="Poll", 1="Status Report", 2="Labels Request (Serial Only)", 8="Dump Zone Timers (TCP Only)", 10="Set Time/Date", 200="Send User Code"]
                Text item=PANEL_TIME {
                    Switch item=PANEL_TIME_STAMP label="Panel Time Stamp"
                    Switch item=PANEL_TIME_BROADCAST label="Panel Time Broadcast"
                }

                Text item=PANEL_SYSTEM_ERROR

                Text item=PANEL_TROUBLE_LED label="Panel Trouble Condition" {
                    Text item=PANEL_TROUBLE_MESSAGE
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
                Text item=PARTITION1_STATUS {
                    Switch item=PARTITION1_ARM_MODE label="Partition 1 Arm Options" mappings=[0="Disarm", 1="Away", 2="Stay", 3="No Entry Delay", 4="With User Code"]
                    Text item=PARTITION1_OPENING_CLOSING_MODE
                }
            }

            Frame label="Keypad" {
                Text label="Keypad LED Status" {
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
                Text label="All Zones" {
                    Text item=ZONE1_STATUS {
                        Switch item=ZONE1_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE1_IN_ALARM
                            Switch item=ZONE1_TAMPER
                            Switch item=ZONE1_FAULT
                        }
                    }

                    Text item=ZONE9_STATUS {
                        Switch item=ZONE9_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE9_IN_ALARM
                            Switch item=ZONE9_TAMPER
                            Switch item=ZONE9_FAULT
                        }
                    }
                    Text item=ZONE10_STATUS {
                        Switch item=ZONE10_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE10_IN_ALARM
                            Switch item=ZONE10_TAMPER
                            Switch item=ZONE10_FAULT
                        }
                    }
                    Text item=ZONE11_STATUS {
                        Switch item=ZONE11_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE11_IN_ALARM
                            Switch item=ZONE11_TAMPER
                            Switch item=ZONE11_FAULT
                        }
                    }
                    Text item=ZONE12_STATUS {
                        Switch item=ZONE12_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE12_IN_ALARM
                            Switch item=ZONE12_TAMPER
                            Switch item=ZONE12_FAULT
                        }
                    }
                    Text item=ZONE13_STATUS {
                        Switch item=ZONE13_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE13_IN_ALARM
                            Switch item=ZONE13_TAMPER
                            Switch item=ZONE13_FAULT
                        }
                    }
                    Text item=ZONE14_STATUS {
                        Switch item=ZONE14_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE14_IN_ALARM
                            Switch item=ZONE14_TAMPER
                            Switch item=ZONE14_FAULT
                        }
                    }
                    Text item=ZONE15_STATUS {
                        Switch item=ZONE15_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE15_IN_ALARM
                            Switch item=ZONE15_TAMPER
                            Switch item=ZONE15_FAULT
                        }
                    }
                    Text item=ZONE21_STATUS {
                        Switch item=ZONE21_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE21_IN_ALARM
                            Switch item=ZONE21_TAMPER
                            Switch item=ZONE21_FAULT
                        }
                    }
                    Text item=ZONE22_STATUS {
                        Switch item=ZONE22_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE22_IN_ALARM
                            Switch item=ZONE22_TAMPER
                            Switch item=ZONE22_FAULT
                        }
                    }
                    Text item=ZONE23_STATUS {
                        Switch item=ZONE23_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE23_IN_ALARM
                            Switch item=ZONE23_TAMPER
                            Switch item=ZONE23_FAULT
                        }
                    }
                    Text item=ZONE24_STATUS {
                        Switch item=ZONE24_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE24_IN_ALARM
                            Switch item=ZONE24_TAMPER
                            Switch item=ZONE24_FAULT
                        }
                    }
                    Text item=ZONE25_STATUS {
                        Switch item=ZONE25_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE25_IN_ALARM
                            Switch item=ZONE25_TAMPER
                            Switch item=ZONE25_FAULT
                        }
                    }
                    Text item=ZONE51_STATUS {
                        Switch item=ZONE51_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE51_IN_ALARM
                            Switch item=ZONE51_TAMPER
                            Switch item=ZONE51_FAULT
                        }
                    }
                    Text item=ZONE52_STATUS {
                        Switch item=ZONE52_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE52_IN_ALARM
                            Switch item=ZONE52_TAMPER
                            Switch item=ZONE52_FAULT
                        }
                    }
                    Text item=ZONE53_STATUS {
                        Switch item=ZONE53_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE53_IN_ALARM
                            Switch item=ZONE53_TAMPER
                            Switch item=ZONE53_FAULT
                        }
                    }
                    Text item=ZONE54_STATUS {
                        Switch item=ZONE54_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE54_IN_ALARM
                            Switch item=ZONE54_TAMPER
                            Switch item=ZONE54_FAULT
                        }
                    }
                    Text item=ZONE55_STATUS {
                        Switch item=ZONE55_BYPASS_MODE mappings=[OFF="Armed", ON="Bypassed"]
                        Frame label="Other Status:" {
                            Switch item=ZONE55_IN_ALARM
                            Switch item=ZONE55_TAMPER
                            Switch item=ZONE55_FAULT
                        }
                    }
                }

                Group item=DSCAlarmDoorWindow label="Door/Window Sensors"
                Group item=DSCAlarmMotion label="Motion Sensors"
                Group item=DSCAlarmSmoke label="Smoke Detectors"

            }
        }
    }
```

Sample Rules for Sending a DSC Alarm Command

```
rule "SendKeystrokeStringCommand"
when   
    Item SwitchItemName received command ON
then   
    sendCommand(SEND_DSC_ALARM_COMMAND, "071,1*101#")
end

rule "SendPollingCommand"

when   
    Item SwitchItemName received command ON
then   
    sendCommand(SEND_DSC_ALARM_COMMAND, "000")
end
```

Notice the command variations in the examples.
If a command has data, there needs to be a comma between the command and the data as seen above in the first example.
If there is no data then it would only require the command itself as in the second example.
