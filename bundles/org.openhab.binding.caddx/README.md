# Caddx Binding

The Caddx binding is used for communicating with the Caddx alarm panels

It provides connectivity to the alarm panel via a RS-232 serial connection to the Caddx interface or directly to the NX8E.


## Supported Things

This binding supports the following Thing types

| Thing      | Thing Type | Description                                                            |
|------------|------------|------------------------------------------------------------------------|
| bridge     | Bridge     | The  RS-232 interface.                                                 |
| panel      | Thing      | The basic representation of the alarm System.                          |
| partition  | Thing      | Represents a controllable area within the alarm system.                |
| zone       | Thing      | Represents a physical device such as a door, window, or motion sensor. |
| keypad     | Thing      | Represents a keypad.                                                   |

## Discovery

First the Caddx bridge must be manually defined. The serial port, baud rate and protocol have to be set correctly to match the respective configuration of the panel.
After the bridge is manually added and available to openHAB, the binding will automatically start to discover partitions and zones and add them to the discovery inbox.

Note:
There is currently no support to discover the available keypads.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The things can be configured either through the online configuration utility via discovery, or manually through the configuration file.
The following table shows the available configuration parameters for each thing.

<table>
	<tr><td><b>Thing</b></td><td><b>Configuration Parameters</b></td></tr>
	<tr><td>bridge</td><td><b>protocol</b> - Protocol used for the communication (Binary, Ascii) - Required.<br/><b>serialPort</b> - Serial port for the bridge - Required.<br/><b>baud</b> - Baud rate of the bridge - Not Required - default = 9600.</td></tr>
	<tr><td>partition</td><td><b>partitionNumber</b> - Partition number (1-8) - Required.</td></tr>
	<tr><td>zone</td><td><b>zoneNumber</b> - Zone number (1-192) - Required.</td></tr>
	<tr><td>keypad</td><td><b>keypadAddress</b> - Keypad address (192-255) - Required.</td></tr>
</table>

The binding can be configured manually if discovery is not utilized.
A full example is further below.

## Channels

Caddx Alarm things support a variety of channels as seen below in the following table:

<table>
	<tr><td><b>Channel</b></td><td><b>Item Type</b></td><td><b>Type</b></td><td><b>Description</b></td></tr>
	<tr><td>panel_firmware_version</td><td>String</td><td>Configuration</td><td>Firmware version</td></tr>
	<tr><td>panel_log_message_n_0</td><td>String</td><td>Runtime</td><td>Log message 10</td></tr>
	<tr><td>panel_log_message_n_1</td><td>String</td><td>Runtime</td><td>Log message 9</td></tr>
	<tr><td>panel_log_message_n_2</td><td>String</td><td>Runtime</td><td>Log message 8</td></tr>
	<tr><td>panel_log_message_n_3</td><td>String</td><td>Runtime</td><td>Log message 7</td></tr>
	<tr><td>panel_log_message_n_4</td><td>String</td><td>Runtime</td><td>Log message 6</td></tr>
	<tr><td>panel_log_message_n_5</td><td>String</td><td>Runtime</td><td>Log message 5</td></tr>
	<tr><td>panel_log_message_n_6</td><td>String</td><td>Runtime</td><td>Log message 4</td></tr>
	<tr><td>panel_log_message_n_7</td><td>String</td><td>Runtime</td><td>Log message 3</td></tr>
	<tr><td>panel_log_message_n_8</td><td>String</td><td>Runtime</td><td>Log message 2</td></tr>
	<tr><td>panel_log_message_n_9</td><td>String</td><td>Runtime</td><td>Log message 1</td></tr>
	<tr><td>panel_interface_configuration_message</td><td>Switch</td><td>Configuration</td><td>Interface Configuration Message</td></tr>
	<tr><td>panel_zone_status_message</td><td>Switch</td><td>Configuration</td><td>Zone Status Message</td></tr>
	<tr><td>panel_zones_snapshot_message</td><td>Switch</td><td>Configuration</td><td>Zones Snapshot Message</td></tr>
	<tr><td>panel_partition_status_message</td><td>Switch</td><td>Configuration</td><td>Partition Status Message</td></tr>
	<tr><td>panel_partitions_snapshot_message</td><td>Switch</td><td>Configuration</td><td>Partitions Snapshot Message</td></tr>
	<tr><td>panel_system_status_message</td><td>Switch</td><td>Configuration</td><td>System Status Message</td></tr>
	<tr><td>panel_x10_message_received</td><td>Switch</td><td>Configuration</td><td>X-10 Message Received</td></tr>
	<tr><td>panel_log_event_message</td><td>Switch</td><td>Configuration</td><td>Log Event Message</td></tr>
	<tr><td>panel_keypad_message_received</td><td>Switch</td><td>Configuration</td><td>Keypad Message Received</td></tr>
	<tr><td>panel_interface_configuration_request</td><td>Switch</td><td>Configuration</td><td>Interface Configuration Request</td></tr>
	<tr><td>panel_zone_name_request</td><td>Switch</td><td>Configuration</td><td>Zone Name Request</td></tr>
	<tr><td>panel_zone_status_request</td><td>Switch</td><td>Configuration</td><td>Zone Status Request</td></tr>
	<tr><td>panel_zones_snapshot_request</td><td>Switch</td><td>Configuration</td><td>Zones Snapshot Request</td></tr>
	<tr><td>panel_partition_status_request</td><td>Switch</td><td>Configuration</td><td>Partition Status Request</td></tr>
	<tr><td>panel_partitions_snapshot_request</td><td>Switch</td><td>Configuration</td><td>Partitions Snapshot Request</td></tr>
	<tr><td>panel_system_status_request</td><td>Switch</td><td>Configuration</td><td>System Status Request</td></tr>
	<tr><td>panel_send_x10_message</td><td>Switch</td><td>Configuration</td><td>Send X-10 Message</td></tr>
	<tr><td>panel_log_event_request</td><td>Switch</td><td>Configuration</td><td>Log Event Request</td></tr>
	<tr><td>panel_send_keypad_text_message</td><td>Switch</td><td>Configuration</td><td>Send Keypad Text Message</td></tr>
	<tr><td>panel_keypad_terminal_mode_request</td><td>Switch</td><td>Configuration</td><td>Keypad Terminal Mode Request</td></tr>
	<tr><td>panel_program_data_request</td><td>Switch</td><td>Configuration</td><td>Program Data Request</td></tr>
	<tr><td>panel_program_data_command</td><td>Switch</td><td>Configuration</td><td>Program Data Command</td></tr>
	<tr><td>panel_user_information_request_with_pin</td><td>Switch</td><td>Configuration</td><td>User Information Request with PIN</td></tr>
	<tr><td>panel_user_information_request_without_pin</td><td>Switch</td><td>Configuration</td><td>User Information Request without PIN</td></tr>
	<tr><td>panel_set_user_code_command_with_pin</td><td>Switch</td><td>Configuration</td><td>Set User Code Command with PIN</td></tr>
	<tr><td>panel_set_user_code_command_without_pin</td><td>Switch</td><td>Configuration</td><td>Set User Code Command without PIN</td></tr>
	<tr><td>panel_set_user_authorization_command_with_pin</td><td>Switch</td><td>Configuration</td><td>Set User Authorization Command with PIN</td></tr>
	<tr><td>panel_set_user_authorization_command_without_pin</td><td>Switch</td><td>Configuration</td><td>Set User Authorization Command without PIN</td></tr>
	<tr><td>panel_store_communication_event_command</td><td>Switch</td><td>Configuration</td><td>Store Communication Event Command</td></tr>
	<tr><td>panel_set_clock_calendar_command</td><td>Switch</td><td>Configuration</td><td>Set Clock / Calendar Command</td></tr>
	<tr><td>panel_primary_keypad_function_with_pin</td><td>Switch</td><td>Configuration</td><td>Primary Keypad Function with PIN</td></tr>
	<tr><td>panel_primary_keypad_function_without_pin</td><td>Switch</td><td>Configuration</td><td>Primary Keypad Function without PIN</td></tr>
	<tr><td>panel_secondary_keypad_function</td><td>Switch</td><td>Configuration</td><td>Secondary Keypad Function</td></tr>
	<tr><td>panel_zone_bypass_toggle</td><td>Switch</td><td>Configuration</td><td>Zone Bypass Toggle</td></tr>
	<tr><td>partition_bypass_code_required</td><td>Switch</td><td>Partition Condition</td><td>Bypass code required</td></tr>
	<tr><td>partition_fire_trouble</td><td>Switch</td><td>Partition Condition</td><td>Fire trouble</td></tr>
	<tr><td>partition_fire</td><td>Switch</td><td>Partition Condition</td><td>Fire</td></tr>
	<tr><td>partition_pulsing_buzzer</td><td>Switch</td><td>Partition Condition</td><td>Pulsing Buzzer</td></tr>
	<tr><td>partition_tlm_fault_memory</td><td>Switch</td><td>Partition Condition</td><td>TLM fault memory</td></tr>
	<tr><td>partition_armed</td><td>Switch</td><td>Partition Condition</td><td>Armed</td></tr>
	<tr><td>partition_instant</td><td>Switch</td><td>Partition Condition</td><td>Instant</td></tr>
	<tr><td>partition_previous_alarm</td><td>Switch</td><td>Partition Condition</td><td>Previous Alarm</td></tr>
	<tr><td>partition_siren_on</td><td>Switch</td><td>Partition Condition</td><td>Siren on</td></tr>
	<tr><td>partition_steady_siren_on</td><td>Switch</td><td>Partition Condition</td><td>Steady siren on</td></tr>
	<tr><td>partition_alarm_memory</td><td>Switch</td><td>Partition Condition</td><td>Alarm memory</td></tr>
	<tr><td>partition_tamper</td><td>Switch</td><td>Partition Condition</td><td>Tamper</td></tr>
	<tr><td>partition_cancel_command_entered</td><td>Switch</td><td>Partition Condition</td><td>Cancel command entered</td></tr>
	<tr><td>partition_code_entered</td><td>Switch</td><td>Partition Condition</td><td>Code entered</td></tr>
	<tr><td>partition_cancel_pending</td><td>Switch</td><td>Partition Condition</td><td>Cancel pending</td></tr>
	<tr><td>partition_silent_exit_enabled</td><td>Switch</td><td>Partition Condition</td><td>Silent exit enabled</td></tr>
	<tr><td>partition_entryguard</td><td>Switch</td><td>Partition Condition</td><td>Entryguard (stay mode)</td></tr>
	<tr><td>partition_chime_mode_on</td><td>Switch</td><td>Partition Condition</td><td>Chime mode on</td></tr>
	<tr><td>partition_entry</td><td>Switch</td><td>Partition Condition</td><td>Entry</td></tr>
	<tr><td>partition_delay_expiration_warning</td><td>Switch</td><td>Partition Condition</td><td>Delay expiration warning</td></tr>
	<tr><td>partition_exit1</td><td>Switch</td><td>Partition Condition</td><td>Exit1</td></tr>
	<tr><td>partition_exit2</td><td>Switch</td><td>Partition Condition</td><td>Exit2</td></tr>
	<tr><td>partition_led_extinguish</td><td>Switch</td><td>Partition Condition</td><td>LED extinguish</td></tr>
	<tr><td>partition_cross_timing</td><td>Switch</td><td>Partition Condition</td><td>Cross timing</td></tr>
	<tr><td>partition_recent_closing_being_timed</td><td>Switch</td><td>Partition Condition</td><td>Recent closing being timed</td></tr>
	<tr><td>partition_exit_error_triggered</td><td>Switch</td><td>Partition Condition</td><td>Exit error triggered</td></tr>
	<tr><td>partition_auto_home_inhibited</td><td>Switch</td><td>Partition Condition</td><td>Auto home inhibited</td></tr>
	<tr><td>partition_sensor_low_battery</td><td>Switch</td><td>Partition Condition</td><td>Sensor low battery</td></tr>
	<tr><td>partition_sensor_lost_supervision</td><td>Switch</td><td>Partition Condition</td><td>Sensor lost supervision</td></tr>
	<tr><td>partition_zone_bypassed</td><td>Switch</td><td>Partition Condition</td><td>Zone bypassed</td></tr>
	<tr><td>partition_force_arm_triggered_by_auto_arm</td><td>Switch</td><td>Partition Condition</td><td>Force arm triggered by auto arm</td></tr>
	<tr><td>partition_ready_to_arm</td><td>Switch</td><td>Partition Condition</td><td>Ready to arm</td></tr>
	<tr><td>partition_ready_to_force_arm</td><td>Switch</td><td>Partition Condition</td><td>Ready to force arm</td></tr>
	<tr><td>partition_valid_pin_accepted</td><td>Switch</td><td>Partition Condition</td><td>Valid PIN accepted</td></tr>
	<tr><td>partition_chime_on</td><td>Switch</td><td>Partition Condition</td><td>Chime on (sounding)</td></tr>
	<tr><td>partition_error_beep</td><td>Switch</td><td>Partition Condition</td><td>Error beep (triple beep)</td></tr>
	<tr><td>partition_tone_on</td><td>Switch</td><td>Partition Condition</td><td>Tone on (activation tone)</td></tr>
	<tr><td>partition_entry1</td><td>Switch</td><td>Partition Condition</td><td>Entry 1</td></tr>
	<tr><td>partition_open_period</td><td>Switch</td><td>Partition Condition</td><td>Open period</td></tr>
	<tr><td>partition_alarm_sent_using_phone_number_1</td><td>Switch</td><td>Partition Condition</td><td>Alarm sent using phone number 1</td></tr>
	<tr><td>partition_alarm_sent_using_phone_number_2</td><td>Switch</td><td>Partition Condition</td><td>Alarm sent using phone number 2</td></tr>
	<tr><td>partition_alarm_sent_using_phone_number_3</td><td>Switch</td><td>Partition Condition</td><td>Alarm sent using phone number 3</td></tr>
	<tr><td>partition_cancel_report_is_in_the_stack</td><td>Switch</td><td>Partition Condition</td><td>Cancel report is in the stack</td></tr>
	<tr><td>partition_keyswitch_armed</td><td>Switch</td><td>Partition Condition</td><td>Keyswitch armed</td></tr>
	<tr><td>partition_delay_trip_in_progress</td><td>Switch</td><td>Partition Condition</td><td>Delay Trip in progress (common zone)</td></tr>
	<tr><td>partition_primary_command</td><td>Number</td><td>Command</td><td>Partition Primary Command</td></tr>
	<tr><td>partition_secondary_command</td><td>Number</td><td>Command</td><td>Partition Secondary Command</td></tr>
	<tr><td>zone_partition2</td><td>Switch</td><td>Configuration</td><td>Partition 2</td></tr>
	<tr><td>zone_partition3</td><td>Switch</td><td>Configuration</td><td>Partition 3</td></tr>
	<tr><td>zone_partition1</td><td>Switch</td><td>Configuration</td><td>Partition 1</td></tr>
	<tr><td>zone_partition4</td><td>Switch</td><td>Configuration</td><td>Partition 4</td></tr>
	<tr><td>zone_partition5</td><td>Switch</td><td>Configuration</td><td>Partition 5</td></tr>
	<tr><td>zone_partition6</td><td>Switch</td><td>Configuration</td><td>Partition 6</td></tr>
	<tr><td>zone_partition7</td><td>Switch</td><td>Configuration</td><td>Partition 7</td></tr>
	<tr><td>zone_partition8</td><td>Switch</td><td>Configuration</td><td>Partition 8</td></tr>
	<tr><td>zone_name</td><td>String</td><td>Configuration</td><td>Name</td></tr>
	<tr><td>zone_fire</td><td>Switch</td><td>Configuration</td><td>Fire</td></tr>
	<tr><td>zone_24hour</td><td>Switch</td><td>Configuration</td><td>24 Hour</td></tr>
	<tr><td>zone_key_switch</td><td>Switch</td><td>Configuration</td><td>Key-switch</td></tr>
	<tr><td>zone_follower</td><td>Switch</td><td>Configuration</td><td>Follower</td></tr>
	<tr><td>zone_entry_exit_delay_1</td><td>Switch</td><td>Configuration</td><td>Entry / exit delay 1</td></tr>
	<tr><td>zone_entry_exit_delay_2</td><td>Switch</td><td>Configuration</td><td>Entry / exit delay 2</td></tr>
	<tr><td>zone_interior</td><td>Switch</td><td>Configuration</td><td>Interior</td></tr>
	<tr><td>zone_local_only</td><td>Switch</td><td>Configuration</td><td>Local only</td></tr>
	<tr><td>zone_keypad_sounder</td><td>Switch</td><td>Configuration</td><td>Keypad Sounder</td></tr>
	<tr><td>zone_yelping_siren</td><td>Switch</td><td>Configuration</td><td>Yelping siren</td></tr>
	<tr><td>zone_steady_siren</td><td>Switch</td><td>Configuration</td><td>Steady siren</td></tr>
	<tr><td>zone_chime</td><td>Switch</td><td>Configuration</td><td>Chime</td></tr>
	<tr><td>zone_bypassable</td><td>Switch</td><td>Configuration</td><td>Bypassable</td></tr>
	<tr><td>zone_group_bypassable</td><td>Switch</td><td>Configuration</td><td>Group bypassable</td></tr>
	<tr><td>zone_force_armable</td><td>Switch</td><td>Configuration</td><td>Force armable</td></tr>
	<tr><td>zone_entry_guard</td><td>Switch</td><td>Configuration</td><td>Entry guard</td></tr>
	<tr><td>zone_fast_loop_response</td><td>Switch</td><td>Configuration</td><td>Fast loop response</td></tr>
	<tr><td>zone_double_eol_tamper</td><td>Switch</td><td>Configuration</td><td>Double EOL tamper</td></tr>
	<tr><td>zone_type_trouble</td><td>Switch</td><td>Configuration</td><td>Trouble</td></tr>
	<tr><td>zone_cross_zone</td><td>Switch</td><td>Configuration</td><td>Cross zone</td></tr>
	<tr><td>zone_dialer_delay</td><td>Switch</td><td>Configuration</td><td>Dialer delay</td></tr>
	<tr><td>zone_swinger_shutdown</td><td>Switch</td><td>Configuration</td><td>Swinger shutdown</td></tr>
	<tr><td>zone_restorable</td><td>Switch</td><td>Configuration</td><td>Restorable</td></tr>
	<tr><td>zone_listen_in</td><td>Switch</td><td>Configuration</td><td>Listen in</td></tr>
	<tr><td>zone_faulted</td><td>Switch</td><td>Zone Condition</td><td>Faulted (or delayed trip)</td></tr>
	<tr><td>zone_tampered</td><td>Switch</td><td>Zone Condition</td><td>Tampered</td></tr>
	<tr><td>zone_trouble</td><td>Switch</td><td>Zone Condition</td><td>Trouble</td></tr>
	<tr><td>zone_bypassed</td><td>Switch</td><td>Zone Condition</td><td>Bypassed</td></tr>
	<tr><td>zone_inhibited</td><td>Switch</td><td>Zone Condition</td><td>Inhibited (force armed)</td></tr>
	<tr><td>zone_low_battery</td><td>Switch</td><td>Zone Condition</td><td>Low battery</td></tr>
	<tr><td>zone_loss_of_supervision</td><td>Switch</td><td>Zone Condition</td><td>Loss of supervision</td></tr>
	<tr><td>zone_alarm_memory</td><td>Switch</td><td>Zone Condition</td><td>Alarm memory</td></tr>
	<tr><td>zone_bypass_memory</td><td>Switch</td><td>Zone Condition</td><td>Bypass memory</td></tr>
	<tr><td>zone_bypass_toggle</td><td>Switch</td><td>Command</td><td>Send Zone bypass</td></tr>
</table>

## Full Example

The following is an example of a things file (caddx.things):

```
Bridge caddx:bridge:thebridge  "Bridge"                   [ protocol="Binary", serialPort="/dev/ttyUSB0", baud=38400 ] {
    Thing partition partition1 "Groundfloor alarm"        [ partitionNumber=1 ]
    Thing zone      zone1      "Livingroom motion sensor" [ zoneNumber=1 ]
    Thing zone      zone2      "Bedroom motion sensor"    [ zoneNumber=2 ]
    Thing zone      zone3      "Guestroom motion sensor"  [ zoneNumber=3 ]
    Thing zone      zone4      "Livingroom window"        [ zoneNumber=4 ]
    Thing zone      zone5      "Bedroom window"           [ zoneNumber=5 ]
    Thing zone      zone6      "Guestroom window"         [ zoneNumber=6 ]
}
```

The following is an example of an item file (caddx.items):

```
Group:Switch:OR(ON, OFF)        MotionSensors   "Motion Sensors"   <motion>   ["MotionDetector"]
Group:Switch:OR(ON, OFF)        Windows         "Windows"          <window>   ["Window"]

Switch    Bedroom_Motion        "Motion Sensor [%s]"  <motion>       (MotionSensors)  { channel="caddx:zone:thebridge:zone1:zone_faulted" }
Switch    Livingroom_Motion     "Motion Sensor [%s]"  <motion>       (MotionSensors)  { channel="caddx:zone:thebridge:zone2:zone_faulted" }
Switch    Guestroom_Motion      "Motion Sensor [%s]"  <motion>       (MotionSensors)  { channel="caddx:zone:thebridge:zone3:zone_faulted" }
Switch    Bedroom_Window        "Window [%s]"         <window>       (Windows)        { channel="caddx:zone:thebridge:zone4:zone_faulted" }
Switch    Livingroom_Window     "Window [%s]"         <window>       (Windows)        { channel="caddx:zone:thebridge:zone5:zone_faulted" }
Switch    Guestroom_Window      "Window [%s]"         <window>       (Windows)        { channel="caddx:zone:thebridge:zone6:zone_faulted" }

Switch    Partition1_Armed      "Armed [%s]"          <groundfloor>   { channel="caddx:partition:thebridge:partition1:partition_armed" }
Switch    Partition1_EntryGuard "Entry Guard [%s]"    <groundfloor>   { channel="caddx:partition:thebridge:partition1:partition_entryguard" }
```

The following is an example of a sitemap file (home.sitemap):

```
sitemap home label="Home" {
    Frame label="Ground floor" {
    Text item=Partition1_Armed
    Text item=Partition1_EntryGuard

    Text item=MotionSensors
        Text label="Motion Sensors (detailed)" {
            Text item=Bedroom_Motion
            Text item=Livingroom_Motion
            Text item=Guestroom_Motion
        }

    Text item=Windows
        Text label="Windows (detailed)" {
            Text item=Bedroom_Window
            Text item=Livingroom_Window
            Text item=Guestroom_Window
        }
    }
}
```
