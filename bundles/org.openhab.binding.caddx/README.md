# Caddx Binding

The Caddx binding is used for communicating with the Caddx alarm panels. Also known as Interlogix.

It provides connectivity to the NetworX alarm panels via a RS-232 serial connection to the NX-584E interface or directly to the NX-8E.

## Supported Things

This binding supports the following Thing types

| Thing      | Thing Type | Description                                                            |
|------------|------------|------------------------------------------------------------------------|
| bridge     | Bridge     | The  RS-232 interface.                                                 |
| panel      | Thing      | The basic representation of the alarm System.                          |
| partition  | Thing      | Represents a controllable area within the alarm system.                |
| zone       | Thing      | Represents a physical device such as a door, window, or motion sensor. |
| keypad     | Thing      | Represents a keypad. (Not yet functional)                              |

## Discovery

First the bridge must be **manually** defined. The serial port, baud rate and protocol have to be set correctly to match the respective configuration of the panel (see below Prerequisites section).
After the bridge is manually added and available to openHAB, the binding will automatically start to discover partitions and zones and add them to the discovery inbox.

Note:
There is currently no support to discover the available keypads.

## Prerequisites

For the binding to work the panel has also to be programmed appropriately. 

### Programming locations for the NX-8E control panel

| Location | Segment | Value       | Description                                                                                            |
|----------|---------|-------------|--------------------------------------------------------------------------------------------------------|
| 207      | 1       | 1           | Serial Port selector                                                                                   |
| 208      | 1       | 0 - 4       | Baud rate table<br>0 = 2400 Baud<br>1 = 4800 Baud<br>2 = 9600 Baud<br>3 = 19200 Baud<br>4 = 38400 Baud |
| 209      | 1       | Off/On      | Home automation protocol<br>1 = Off: Binary. On: ASCII.                                                |
| 210      | 1       | 2,5,6,7,8   | Enabling the Transitions                                                                               |
| 210      | 2       | 1,2,3,4     |                                                                                                        |
| 211      | 1       | 2,4,5,6,7,8 | Programming the Command/Request enables                                                                |
| 211      | 2       | 1,2,3,4,5   | (Flags 4 and 5 are not yet functional. Can be ignored.)                                                |
| 211      | 3       |             |                                                                                                        |
| 211      | 4       | 5,7,8       |                                                                                                        |
| 212      | 1       | 192         | Programming the LCD keypad address. (Not yet functional. Can be ignored.)                              |

### Programming locations for the NX-584E home automation module

| Location | Segment | Value       | Description                                                                                            |
|----------|---------|-------------|--------------------------------------------------------------------------------------------------------|
| 0        | 1       | Off/On      | Home automation protocol<br>1 = Off: Binary. On: ASCII.                                                |
| 1        | 1       | 0 - 4       | Baud rate table<br>0 = 600 Baud<br>1 = 1200 Baud<br>2 = 2400 Baud<br>3 = 4800 Baud<br>4 = 9600 Baud<br>5 = 19200 Baud<br>6 = 38400 Baud<br>7 = 76800 Baud |
| 2        | 1       | 2,5,6,7,8   | Enabling the Transitions                                                                               |
| 2        | 2       | 1,2,3,4     |                                                                                                        |
| 3        | 1       | 2,4,5,6,7,8 | Programming the Command/Request enables                                                                |
| 3        | 2       | 1,2,3,4,5   | (Flags 4 and 5 are not yet functional. Can be ignored.)                                                |
| 3        | 3       |             |                                                                                                        |
| 3        | 4       | 5,7,8       |                                                                                                        |
| 4        | 1       | 192         | Programming the LCD keypad address. (Not yet functional. Can be ignored.)                              |

## Thing Configuration

The things can be configured either through the online configuration utility via discovery, or manually through the configuration file.
The following table shows the available configuration parameters for each thing.

| Thing     | Configuration Parameters                                                                       |
|-----------|------------------------------------------------------------------------------------------------|
| bridge    | `serialPort` - Serial port for the bridge - Required                                           |
|           | `protocol` - Protocol used for the communication (Binary, Ascii) - Required - Default = Binary |
|           | `baud` - Baud rate of the bridge - Required - Default = 9600                                   |
| partition | `partitionNumber` - Partition number (1-8) - Required                                          |
| zone      | `zoneNumber` - Zone number (1-192) - Required                                                  |
| keypad    | `keypadAddress` - Keypad address (192-255) - Required                                          |

A full example is further below.

## Channels

Caddx Alarm things support a variety of channels as seen below in the following table:

| Channel                                          | Item Type | Type                | Description                                |
|--------------------------------------------------|-----------|---------------------|--------------------------------------------|
| send_Command                                     | String    | Command             | Send a command to the panel                |
| panel_firmware_version                           | String    | Configuration       | Firmware version                           |
| panel_log_message_n_0                            | String    | Runtime             | Log message 10                             |
| panel_log_message_n_1                            | String    | Runtime             | Log message 9                              |
| panel_log_message_n_2                            | String    | Runtime             | Log message 8                              |
| panel_log_message_n_3                            | String    | Runtime             | Log message 7                              |
| panel_log_message_n_4                            | String    | Runtime             | Log message 6                              |
| panel_log_message_n_5                            | String    | Runtime             | Log message 5                              |
| panel_log_message_n_6                            | String    | Runtime             | Log message 4                              |
| panel_log_message_n_7                            | String    | Runtime             | Log message 3                              |
| panel_log_message_n_8                            | String    | Runtime             | Log message 2                              |
| panel_log_message_n_9                            | String    | Runtime             | Log message 1                              |
| panel_interface_configuration_message            | Switch    | Configuration       | Interface Configuration Message            |
| panel_zone_status_message                        | Switch    | Configuration       | Zone Status Message                        |
| panel_zones_snapshot_message                     | Switch    | Configuration       | Zones Snapshot Message                     |
| panel_partition_status_message                   | Switch    | Configuration       | Partition Status Message                   |
| panel_partitions_snapshot_message                | Switch    | Configuration       | Partitions Snapshot Message                |
| panel_system_status_message                      | Switch    | Configuration       | System Status Message                      |
| panel_x10_message_received                       | Switch    | Configuration       | X-10 Message Received                      |
| panel_log_event_message                          | Switch    | Configuration       | Log Event Message                          |
| panel_keypad_message_received                    | Switch    | Configuration       | Keypad Message Received                    |
| panel_interface_configuration_request            | Switch    | Configuration       | Interface Configuration Request            |
| panel_zone_name_request                          | Switch    | Configuration       | Zone Name Request                          |
| panel_zone_status_request                        | Switch    | Configuration       | Zone Status Request                        |
| panel_zones_snapshot_request                     | Switch    | Configuration       | Zones Snapshot Request                     |
| panel_partition_status_request                   | Switch    | Configuration       | Partition Status Request                   |
| panel_partitions_snapshot_request                | Switch    | Configuration       | Partitions Snapshot Request                |
| panel_system_status_request                      | Switch    | Configuration       | System Status Request                      |
| panel_send_x10_message                           | Switch    | Configuration       | Send X-10 Message                          |
| panel_log_event_request                          | Switch    | Configuration       | Log Event Request                          |
| panel_send_keypad_text_message                   | Switch    | Configuration       | Send Keypad Text Message                   |
| panel_keypad_terminal_mode_request               | Switch    | Configuration       | Keypad Terminal Mode Request               |
| panel_program_data_request                       | Switch    | Configuration       | Program Data Request                       |
| panel_program_data_command                       | Switch    | Configuration       | Program Data Command                       |
| panel_user_information_request_with_pin          | Switch    | Configuration       | User Information Request with PIN          |
| panel_user_information_request_without_pin       | Switch    | Configuration       | User Information Request without PIN       |
| panel_set_user_code_command_with_pin             | Switch    | Configuration       | Set User Code Command with PIN             |
| panel_set_user_code_command_without_pin          | Switch    | Configuration       | Set User Code Command without PIN          |
| panel_set_user_authorization_command_with_pin    | Switch    | Configuration       | Set User Authorization Command with PIN    |
| panel_set_user_authorization_command_without_pin | Switch    | Configuration       | Set User Authorization Command without PIN |
| panel_store_communication_event_command          | Switch    | Configuration       | Store Communication Event Command          |
| panel_set_clock_calendar_command                 | Switch    | Configuration       | Set Clock / Calendar Command               |
| panel_primary_keypad_function_with_pin           | Switch    | Configuration       | Primary Keypad Function with PIN           |
| panel_primary_keypad_function_without_pin        | Switch    | Configuration       | Primary Keypad Function without PIN        |
| panel_secondary_keypad_function                  | Switch    | Configuration       | Secondary Keypad Function                  |
| panel_zone_bypass_toggle                         | Switch    | Configuration       | Zone Bypass Toggle                         |
| partition_bypass_code_required                   | Switch    | Partition Condition | Bypass code required                       |
| partition_fire_trouble                           | Switch    | Partition Condition | Fire trouble                               |
| partition_fire                                   | Switch    | Partition Condition | Fire                                       |
| partition_pulsing_buzzer                         | Switch    | Partition Condition | Pulsing Buzzer                             |
| partition_tlm_fault_memory                       | Switch    | Partition Condition | TLM fault memory                           |
| partition_armed                                  | Switch    | Partition Condition | Armed                                      |
| partition_instant                                | Switch    | Partition Condition | Instant                                    |
| partition_previous_alarm                         | Switch    | Partition Condition | Previous Alarm                             |
| partition_siren_on                               | Switch    | Partition Condition | Siren on                                   |
| partition_steady_siren_on                        | Switch    | Partition Condition | Steady siren on                            |
| partition_alarm_memory                           | Switch    | Partition Condition | Alarm memory                               |
| partition_tamper                                 | Switch    | Partition Condition | Tamper                                     |
| partition_cancel_command_entered                 | Switch    | Partition Condition | Cancel command entered                     |
| partition_code_entered                           | Switch    | Partition Condition | Code entered                               |
| partition_cancel_pending                         | Switch    | Partition Condition | Cancel pending                             |
| partition_silent_exit_enabled                    | Switch    | Partition Condition | Silent exit enabled                        |
| partition_entryguard                             | Switch    | Partition Condition | Entryguard (stay mode)                     |
| partition_chime_mode_on                          | Switch    | Partition Condition | Chime mode on                              |
| partition_entry                                  | Switch    | Partition Condition | Entry                                      |
| partition_delay_expiration_warning               | Switch    | Partition Condition | Delay expiration warning                   |
| partition_exit1                                  | Switch    | Partition Condition | Exit1                                      |
| partition_exit2                                  | Switch    | Partition Condition | Exit2                                      |
| partition_led_extinguish                         | Switch    | Partition Condition | LED extinguish                             |
| partition_cross_timing                           | Switch    | Partition Condition | Cross timing                               |
| partition_recent_closing_being_timed             | Switch    | Partition Condition | Recent closing being timed                 |
| partition_exit_error_triggered                   | Switch    | Partition Condition | Exit error triggered                       |
| partition_auto_home_inhibited                    | Switch    | Partition Condition | Auto home inhibited                        |
| partition_sensor_low_battery                     | Switch    | Partition Condition | Sensor low battery                         |
| partition_sensor_lost_supervision                | Switch    | Partition Condition | Sensor lost supervision                    |
| partition_zone_bypassed                          | Switch    | Partition Condition | Zone bypassed                              |
| partition_force_arm_triggered_by_auto_arm        | Switch    | Partition Condition | Force arm triggered by auto arm            |
| partition_ready_to_arm                           | Switch    | Partition Condition | Ready to arm                               |
| partition_ready_to_force_arm                     | Switch    | Partition Condition | Ready to force arm                         |
| partition_valid_pin_accepted                     | Switch    | Partition Condition | Valid PIN accepted                         |
| partition_chime_on                               | Switch    | Partition Condition | Chime on (sounding)                        |
| partition_error_beep                             | Switch    | Partition Condition | Error beep (triple beep)                   |
| partition_tone_on                                | Switch    | Partition Condition | Tone on (activation tone)                  |
| partition_entry1                                 | Switch    | Partition Condition | Entry 1                                    |
| partition_open_period                            | Switch    | Partition Condition | Open period                                |
| partition_alarm_sent_using_phone_number_1        | Switch    | Partition Condition | Alarm sent using phone number 1            |
| partition_alarm_sent_using_phone_number_2        | Switch    | Partition Condition | Alarm sent using phone number 2            |
| partition_alarm_sent_using_phone_number_3        | Switch    | Partition Condition | Alarm sent using phone number 3            |
| partition_cancel_report_is_in_the_stack          | Switch    | Partition Condition | Cancel report is in the stack              |
| partition_keyswitch_armed                        | Switch    | Partition Condition | Keyswitch armed                            |
| partition_delay_trip_in_progress                 | Switch    | Partition Condition | Delay Trip in progress (common zone)       |
| partition_secondary_command                      | Number    | Command             | Partition Secondary Command                |
| zone_partition2                                  | Switch    | Configuration       | Partition 2                                |
| zone_partition3                                  | Switch    | Configuration       | Partition 3                                |
| zone_partition1                                  | Switch    | Configuration       | Partition 1                                |
| zone_partition4                                  | Switch    | Configuration       | Partition 4                                |
| zone_partition5                                  | Switch    | Configuration       | Partition 5                                |
| zone_partition6                                  | Switch    | Configuration       | Partition 6                                |
| zone_partition7                                  | Switch    | Configuration       | Partition 7                                |
| zone_partition8                                  | Switch    | Configuration       | Partition 8                                |
| zone_name                                        | String    | Configuration       | Name                                       |
| zone_fire                                        | Switch    | Configuration       | Fire                                       |
| zone_24hour                                      | Switch    | Configuration       | 24 Hour                                    |
| zone_key_switch                                  | Switch    | Configuration       | Key-switch                                 |
| zone_follower                                    | Switch    | Configuration       | Follower                                   |
| zone_entry_exit_delay_1                          | Switch    | Configuration       | Entry / exit delay 1                       |
| zone_entry_exit_delay_2                          | Switch    | Configuration       | Entry / exit delay 2                       |
| zone_interior                                    | Switch    | Configuration       | Interior                                   |
| zone_local_only                                  | Switch    | Configuration       | Local only                                 |
| zone_keypad_sounder                              | Switch    | Configuration       | Keypad Sounder                             |
| zone_yelping_siren                               | Switch    | Configuration       | Yelping siren                              |
| zone_steady_siren                                | Switch    | Configuration       | Steady siren                               |
| zone_chime                                       | Switch    | Configuration       | Chime                                      |
| zone_bypassable                                  | Switch    | Configuration       | Bypassable                                 |
| zone_group_bypassable                            | Switch    | Configuration       | Group bypassable                           |
| zone_force_armable                               | Switch    | Configuration       | Force armable                              |
| zone_entry_guard                                 | Switch    | Configuration       | Entry guard                                |
| zone_fast_loop_response                          | Switch    | Configuration       | Fast loop response                         |
| zone_double_eol_tamper                           | Switch    | Configuration       | Double EOL tamper                          |
| zone_type_trouble                                | Switch    | Configuration       | Trouble                                    |
| zone_cross_zone                                  | Switch    | Configuration       | Cross zone                                 |
| zone_dialer_delay                                | Switch    | Configuration       | Dialer delay                               |
| zone_swinger_shutdown                            | Switch    | Configuration       | Swinger shutdown                           |
| zone_restorable                                  | Switch    | Configuration       | Restorable                                 |
| zone_listen_in                                   | Switch    | Configuration       | Listen in                                  |
| zone_faulted                                     | Contact   | Zone Condition      | Faulted (or delayed trip)                  |
| zone_tampered                                    | Switch    | Zone Condition      | Tampered                                   |
| zone_trouble                                     | Switch    | Zone Condition      | Trouble                                    |
| zone_bypassed                                    | Switch    | Zone Condition      | Bypassed                                   |
| zone_inhibited                                   | Switch    | Zone Condition      | Inhibited (force armed)                    |
| zone_low_battery                                 | Switch    | Zone Condition      | Low battery                                |
| zone_loss_of_supervision                         | Switch    | Zone Condition      | Loss of supervision                        |
| zone_alarm_memory                                | Switch    | Zone Condition      | Alarm memory                               |
| zone_bypass_memory                               | Switch    | Zone Condition      | Bypass memory                              |

## Full Example

The following is an example of a things file (caddx.things):

```
Bridge caddx:bridge:thebridge  "Bridge"                   [ protocol="Binary", serialPort="/dev/ttyUSB0", baudrate=38400 ] {
    Thing partition partition1 "Groundfloor alarm"        [ partitionNumber=1 ]
    Thing zone      zone1      "Livingroom motion sensor" [ zoneNumber=1 ]
    Thing zone      zone2      "Bedroom motion sensor"    [ zoneNumber=2 ]
    Thing zone      zone3      "Guestroom motion sensor"  [ zoneNumber=3 ]
    Thing zone      zone4      "Livingroom window"        [ zoneNumber=4 ]
    Thing zone      zone5      "Bedroom window"           [ zoneNumber=5 ]
    Thing zone      zone6      "Guestroom window"         [ zoneNumber=6 ]
}
```

The following is an example of an items file (caddx.items):

```
Group:Contact:OR(OPEN,CLOSED)  MotionSensors   "Motion Sensors [%s]"   <motion>
Group:Contact:OR(OPEN,CLOSED)  Windows         "Windows open [%s]"     <window>

Contact   Bedroom_Motion        "Bedroom [%s]"          <motion>       (MotionSensors)  { channel="caddx:zone:thebridge:zone1:zone_faulted" }
Contact   Livingroom_Motion     "Livingroom [%s]"       <motion>       (MotionSensors)  { channel="caddx:zone:thebridge:zone2:zone_faulted" }
Contact   Guestroom_Motion      "Guestroom [%s]"        <motion>       (MotionSensors)  { channel="caddx:zone:thebridge:zone3:zone_faulted" }
Contact   Bedroom_Window        "Bedroom Window [%s]"   <window>       (Windows)        { channel="caddx:zone:thebridge:zone4:zone_faulted" }
Contact   Livingroom_Window     "Livinroom Window [%s]" <window>       (Windows)        { channel="caddx:zone:thebridge:zone5:zone_faulted" }
Contact   Guestroom_Window      "Guestroom Window [%s]" <window>       (Windows)        { channel="caddx:zone:thebridge:zone6:zone_faulted" }

Switch    Partition1_Armed      "Armed [%s]"            <groundfloor>   { channel="caddx:partition:thebridge:partition1:partition_armed" }
Switch    Partition1_EntryGuard "Entry Guard [%s]"      <groundfloor>   { channel="caddx:partition:thebridge:partition1:partition_entryguard" }
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
