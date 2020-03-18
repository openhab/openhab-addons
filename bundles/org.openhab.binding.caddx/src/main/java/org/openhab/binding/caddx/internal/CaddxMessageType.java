package org.openhab.binding.caddx.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public enum CaddxMessageType {

    Interface_Configuration_Message(0x01, null, 12, "Interface Configuration Message",
            "This message will contain the firmware version number and other information about features currently enabled. It will be sent each time the unit is reset or programmed.",
            CaddxDirection.In, CaddxSource.Panel,

            // Properties
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            new CaddxProperty("panel_firmware_version", 2, 4, 0, 0, "String", "Firmware version", false),

            // Byte 6 Supported transition message flags (1)
            new CaddxProperty("panel_interface_configuration_message", 6, 1, 1, 1, "Bit",
                    "Interface Configuration Message", false),
            new CaddxProperty("panel_zone_status_message", 6, 1, 4, 1, "Bit", "Zone Status Message", false),
            new CaddxProperty("panel_zones_snapshot_message", 6, 1, 5, 1, "Bit", "Zones Snapshot Message", false),
            new CaddxProperty("panel_partition_status_message", 6, 1, 6, 1, "Bit", "Partition Status Message", false),
            new CaddxProperty("panel_partitions_snapshot_message", 6, 1, 7, 1, "Bit", "Partitions Snapshot Message",
                    false),

            // Byte 7 Supported transition message flags (2)
            new CaddxProperty("panel_system_status_message", 7, 1, 0, 1, "Bit", "System Status Message", false),
            new CaddxProperty("panel_x10_message_received", 7, 1, 1, 1, "Bit", "X-10 Message Received", false),
            new CaddxProperty("panel_log_event_message", 7, 1, 2, 1, "Bit", "Log Event Message", false),
            new CaddxProperty("panel_keypad_message_received", 7, 1, 3, 1, "Bit", "Keypad Message Received", false),

            // Byte 8 Supported request / command flags (1)
            new CaddxProperty("panel_interface_configuration_request", 8, 1, 1, 1, "Bit",
                    "Interface Configuration Request", false),
            new CaddxProperty("panel_zone_name_request", 8, 1, 3, 1, "Bit", "Zone Name Request", false),
            new CaddxProperty("panel_zone_status_request", 8, 1, 4, 1, "Bit", "Zone Status Request", false),
            new CaddxProperty("panel_zones_snapshot_request", 8, 1, 5, 1, "Bit", "Zones Snapshot Request", false),
            new CaddxProperty("panel_partition_status_request", 8, 1, 6, 1, "Bit", "Partition Status Request", false),
            new CaddxProperty("panel_partitions_snapshot_request", 8, 1, 7, 1, "Bit", "Partitions Snapshot Request",
                    false),

            // Byte 9 Supported request / command flags (2)
            new CaddxProperty("panel_system_status_request", 9, 1, 0, 1, "Bit", "System Status Request", false),
            new CaddxProperty("panel_send_x10_message", 9, 1, 1, 1, "Bit", "Send X-10 Message", false),
            new CaddxProperty("panel_log_event_request", 9, 1, 2, 1, "Bit", "Log Event Request", false),
            new CaddxProperty("panel_send_keypad_text_message", 9, 1, 3, 1, "Bit", "Send Keypad Text Message", false),
            new CaddxProperty("panel_keypad_terminal_mode_request", 9, 1, 4, 1, "Bit", "Keypad Terminal Mode Request",
                    false),

            // Byte 10 Supported request / command flags (3)
            new CaddxProperty("panel_program_data_request", 10, 1, 0, 1, "Bit", "Program Data Request", false),
            new CaddxProperty("panel_program_data_command", 10, 1, 1, 1, "Bit", "Program Data Command", false),
            new CaddxProperty("panel_user_information_request_with_pin", 10, 1, 2, 1, "Bit",
                    "User Information Request with PIN", false),
            new CaddxProperty("panel_user_information_request_without_pin", 10, 1, 3, 1, "Bit",
                    "User Information Request without PIN", false),
            new CaddxProperty("panel_set_user_code_command_with_pin", 10, 1, 4, 1, "Bit",
                    "Set User Code Command with PIN", false),
            new CaddxProperty("panel_set_user_code_command_without_pin", 10, 1, 5, 1, "Bit",
                    "Set User Code Command without PIN", false),
            new CaddxProperty("panel_set_user_authorization_command_with_pin", 10, 1, 6, 1, "Bit",
                    "Set User Authorization Command with PIN", false),
            new CaddxProperty("panel_set_user_authorization_command_without_pin", 10, 1, 7, 1, "Bit",
                    "Set User Authorization Command without PIN", false),

            // Byte 11 Supported request / command flags (4)
            new CaddxProperty("panel_store_communication_event_command", 11, 1, 2, 1, "Bit",
                    "Store Communication Event Command", false),
            new CaddxProperty("panel_set_clock_calendar_command", 11, 1, 3, 1, "Bit", "Set Clock / Calendar Command",
                    false),
            new CaddxProperty("panel_primary_keypad_function_with_pin", 11, 1, 4, 1, "Bit",
                    "Primary Keypad Function with PIN", false),
            new CaddxProperty("panel_primary_keypad_function_without_pin", 11, 1, 5, 1, "Bit",
                    "Primary Keypad Function without PIN", false),
            new CaddxProperty("panel_secondary_keypad_function", 11, 1, 6, 1, "Bit", "Secondary Keypad Function",
                    false),
            new CaddxProperty("panel_zone_bypass_toggle", 11, 1, 7, 1, "Bit", "Zone Bypass Toggle", false)),

    Zone_Name_Message(0x03, null, 18, "Zone Name Message",
            "This message will contain the 16-character name for the zone number that was requested (via Zone Name Request (23h)).",
            CaddxDirection.In, CaddxSource.Zone,

            // Properties
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            new CaddxProperty("zone_number", 2, 1, 0, 0, "Int", "Zone number", false),
            new CaddxProperty("zone_name", 3, 16, 0, 0, "String", "Zone name", false)),

    Zone_Status_Message(0x04, null, 8, "Zone Status Message",
            "This message will contain all information relevant to a zone in the system.", CaddxDirection.In, CaddxSource.Zone,

            // Properties
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            new CaddxProperty("zone_number", 2, 1, 0, 0, "Int", "Zone number", false),

            // Byte 3 Partition mask
            new CaddxProperty("zone_partition1", 3, 1, 0, 1, "Bit", "Partition 1 enable", false),
            new CaddxProperty("zone_partition2", 3, 1, 1, 1, "Bit", "Partition 2 enable", false),
            new CaddxProperty("zone_partition3", 3, 1, 2, 1, "Bit", "Partition 3 enable", false),
            new CaddxProperty("zone_partition4", 3, 1, 3, 1, "Bit", "Partition 4 enable", false),
            new CaddxProperty("zone_partition5", 3, 1, 4, 1, "Bit", "Partition 5 enable", false),
            new CaddxProperty("zone_partition6", 3, 1, 5, 1, "Bit", "Partition 6 enable", false),
            new CaddxProperty("zone_partition7", 3, 1, 6, 1, "Bit", "Partition 7 enable", false),
            new CaddxProperty("zone_partition8", 3, 1, 7, 1, "Bit", "Partition 8 enable", false),

            // Byte 4 Zone type flags (1)
            new CaddxProperty("zone_fire", 4, 1, 0, 1, "Bit", "Fire", false),
            new CaddxProperty("zone_24hour", 4, 1, 1, 1, "Bit", "24 Hour", false),
            new CaddxProperty("zone_key_switch", 4, 1, 2, 1, "Bit", "Key-switch", false),
            new CaddxProperty("zone_follower", 4, 1, 3, 1, "Bit", "Follower", false),
            new CaddxProperty("zone_entry_exit_delay_1", 4, 1, 4, 1, "Bit", "Entry / exit delay 1", false),
            new CaddxProperty("zone_entry_exit_delay_2", 4, 1, 5, 1, "Bit", "Entry / exit delay 2", false),
            new CaddxProperty("zone_interior", 4, 1, 6, 1, "Bit", "Interior", false),
            new CaddxProperty("zone_local_only", 4, 1, 7, 1, "Bit", "Local only", false),

            // Byte 5 Zone type flags (2)
            new CaddxProperty("zone_keypad_sounder", 5, 1, 0, 1, "Bit", "Keypad sounder", false),
            new CaddxProperty("zone_yelping_siren", 5, 1, 1, 1, "Bit", "Yelping siren", false),
            new CaddxProperty("zone_steady_siren", 5, 1, 2, 1, "Bit", "Steady siren", false),
            new CaddxProperty("zone_chime", 5, 1, 3, 1, "Bit", "Chime", false),
            new CaddxProperty("zone_bypassable", 5, 1, 4, 1, "Bit", "Bypassable", false),
            new CaddxProperty("zone_group_bypassable", 5, 1, 5, 1, "Bit", "Group bypassable", false),
            new CaddxProperty("zone_force_armable", 5, 1, 6, 1, "Bit", "Force armable", false),
            new CaddxProperty("zone_entry_guard", 5, 1, 7, 1, "Bit", "Entry guard", false),

            // Byte 6 Zone type flags (3)
            new CaddxProperty("zone_fast_loop_response", 6, 1, 0, 1, "Bit", "Fast loop response", false),
            new CaddxProperty("zone_double_eol_tamper", 6, 1, 1, 1, "Bit", "Double EOL tamper", false),
            new CaddxProperty("zone_type_trouble", 6, 1, 2, 1, "Bit", "Trouble", false),
            new CaddxProperty("zone_cross_zone", 6, 1, 3, 1, "Bit", "Cross zone", false),
            new CaddxProperty("zone_dialer_delay", 6, 1, 4, 1, "Bit", "Dialer delay", false),
            new CaddxProperty("zone_swinger_shutdown", 6, 1, 5, 1, "Bit", "Swinger shutdown", false),
            new CaddxProperty("zone_restorable", 6, 1, 6, 1, "Bit", "Restorable", false),
            new CaddxProperty("zone_listen_in", 6, 1, 7, 1, "Bit", "Listen in", false),

            // Byte 7 Zone condition flags (1)
            new CaddxProperty("zone_faulted", 7, 1, 0, 1, "Bit", "Faulted (or delayed trip)", false),
            new CaddxProperty("zone_tampered", 7, 1, 1, 1, "Bit", "Tampered", false),
            new CaddxProperty("zone_trouble", 7, 1, 2, 1, "Bit", "Trouble", false),
            new CaddxProperty("zone_bypassed", 7, 1, 3, 1, "Bit", "Bypassed", false),
            new CaddxProperty("zone_inhibited", 7, 1, 4, 1, "Bit", "Inhibited (force armed)", false),
            new CaddxProperty("zone_low_battery", 7, 1, 5, 1, "Bit", "Low battery", false),
            new CaddxProperty("zone_loss_of_supervision", 7, 1, 6, 1, "Bit", "Loss of supervision", false),

            // Byte 8 Zone condition flags (2)
            new CaddxProperty("zone_alarm_memory", 8, 1, 0, 1, "Bit", "Alarm memory", false),
            new CaddxProperty("zone_bypass_memory", 8, 1, 1, 1, "Bit", "Bypass memory", false)),

    Zones_Snapshot_Message(0x05, null, 10, "Zones Snapshot Message",
            "This message will contain an abbreviated set of information for any group of 16 zones possible on the system. (A zone offset number will set the range of zones)",
            CaddxDirection.In, CaddxSource.Panel,

            // Properties
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            new CaddxProperty("zone_offset", 2, 1, 0, 0, "Int", "Zone offset (0= start at zone 1)", false),

            // Byte 3 Zone 1 & 2 (+offset) status flags
            new CaddxProperty("", 3, 1, 0, 1, "Bit", "Zone 1 faulted (or delayed trip)", false),
            new CaddxProperty("", 3, 1, 1, 1, "Bit", "Zone 1 bypass (or inhibited)", false),
            new CaddxProperty("zone_1_trouble", 3, 1, 2, 1, "Bit", "Zone 1 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 3, 1, 3, 1, "Bit", "Zone 1 alarm memory", false),
            new CaddxProperty("", 3, 1, 4, 1, "Bit", "Zone 2 faulted (or delayed trip)", false),
            new CaddxProperty("", 3, 1, 5, 1, "Bit", "Zone 2 bypass (or inhibited)", false),
            new CaddxProperty("zone_2_trouble", 3, 1, 6, 1, "Bit", "Zone 2 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 3, 1, 7, 1, "Bit", "Zone 2 alarm memory", false),

            // Byte 4 Zone 3 & 4 status flags (see byte 3)
            new CaddxProperty("", 4, 1, 0, 1, "Bit", "Zone 3 faulted (or delayed trip)", false),
            new CaddxProperty("", 4, 1, 1, 1, "Bit", "Zone 3 bypass (or inhibited)", false),
            new CaddxProperty("zone_3_trouble", 4, 1, 2, 1, "Bit", "Zone 3 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 4, 1, 3, 1, "Bit", "Zone 3 alarm memory", false),
            new CaddxProperty("", 4, 1, 4, 1, "Bit", "Zone 4 faulted (or delayed trip)", false),
            new CaddxProperty("", 4, 1, 5, 1, "Bit", "Zone 4 bypass (or inhibited)", false),
            new CaddxProperty("zone_4_trouble", 4, 1, 6, 1, "Bit", "Zone 4 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 4, 1, 7, 1, "Bit", "Zone 4 alarm memory", false),

            // Byte 5 Zone 5 & 6 status flags (see byte 3)
            new CaddxProperty("", 5, 1, 0, 1, "Bit", "Zone 5 faulted (or delayed trip)", false),
            new CaddxProperty("", 5, 1, 1, 1, "Bit", "Zone 5 bypass (or inhibited)", false),
            new CaddxProperty("zone_5_trouble", 5, 1, 2, 1, "Bit", "Zone 5 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 5, 1, 3, 1, "Bit", "Zone 5 alarm memory", false),
            new CaddxProperty("", 5, 1, 4, 1, "Bit", "Zone 6 faulted (or delayed trip)", false),
            new CaddxProperty("", 5, 1, 5, 1, "Bit", "Zone 6 bypass (or inhibited)", false),
            new CaddxProperty("zone_6_trouble", 5, 1, 6, 1, "Bit", "Zone 6 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 5, 1, 7, 1, "Bit", "Zone 6 alarm memory", false),

            // Byte 6 Zone 7 & 8 status flags (see byte 3)
            new CaddxProperty("", 6, 1, 0, 1, "Bit", "Zone 7 faulted (or delayed trip)", false),
            new CaddxProperty("", 6, 1, 1, 1, "Bit", "Zone 7 bypass (or inhibited)", false),
            new CaddxProperty("zone_7_trouble", 6, 1, 2, 1, "Bit", "Zone 7 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 6, 1, 3, 1, "Bit", "Zone 7 alarm memory", false),
            new CaddxProperty("", 6, 1, 4, 1, "Bit", "Zone 8 faulted (or delayed trip)", false),
            new CaddxProperty("", 6, 1, 5, 1, "Bit", "Zone 8 bypass (or inhibited)", false),
            new CaddxProperty("zone_8_trouble", 6, 1, 6, 1, "Bit", "Zone 8 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 6, 1, 7, 1, "Bit", "Zone 8 alarm memory", false),

            // Byte 7 Zone 9 & 10 status flags (see byte 3)
            new CaddxProperty("", 7, 1, 0, 1, "Bit", "Zone 9 faulted (or delayed trip)", false),
            new CaddxProperty("", 7, 1, 1, 1, "Bit", "Zone 9 bypass (or inhibited)", false),
            new CaddxProperty("zone_9_trouble", 7, 1, 2, 1, "Bit", "Zone 9 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 7, 1, 3, 1, "Bit", "Zone 9 alarm memory", false),
            new CaddxProperty("", 7, 1, 4, 1, "Bit", "Zone 10 faulted (or delayed trip)", false),
            new CaddxProperty("", 7, 1, 5, 1, "Bit", "Zone 10 bypass (or inhibited)", false),
            new CaddxProperty("zone_10_trouble", 7, 1, 6, 1, "Bit", "Zone 10 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 7, 1, 7, 1, "Bit", "Zone 10 alarm memory", false),

            // Byte 8 Zone 11 & 12 status flags (see byte 3)
            new CaddxProperty("", 8, 1, 0, 1, "Bit", "Zone 11 faulted (or delayed trip)", false),
            new CaddxProperty("", 8, 1, 1, 1, "Bit", "Zone 11 bypass (or inhibited)", false),
            new CaddxProperty("zone_11_trouble", 8, 1, 2, 1, "Bit", "Zone 11 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 8, 1, 3, 1, "Bit", "Zone 11 alarm memory", false),
            new CaddxProperty("", 8, 1, 4, 1, "Bit", "Zone 12 faulted (or delayed trip)", false),
            new CaddxProperty("", 8, 1, 5, 1, "Bit", "Zone 12 bypass (or inhibited)", false),
            new CaddxProperty("zone_12_trouble", 8, 1, 6, 1, "Bit", "Zone 12 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 8, 1, 7, 1, "Bit", "Zone 12 alarm memory", false),

            // Byte 9 Zone 13 & 14 status flags (see byte 3)
            new CaddxProperty("", 9, 1, 0, 1, "Bit", "Zone 13 faulted (or delayed trip)", false),
            new CaddxProperty("", 9, 1, 1, 1, "Bit", "Zone 13 bypass (or inhibited)", false),
            new CaddxProperty("zone_13_trouble", 9, 1, 2, 1, "Bit", "Zone 13 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 9, 1, 3, 1, "Bit", "Zone 13 alarm memory", false),
            new CaddxProperty("", 9, 1, 4, 1, "Bit", "Zone 14 faulted (or delayed trip)", false),
            new CaddxProperty("", 9, 1, 5, 1, "Bit", "Zone 14 bypass (or inhibited)", false),
            new CaddxProperty("zone_14_trouble", 9, 1, 6, 1, "Bit", "Zone 14 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 9, 1, 7, 1, "Bit", "Zone 14 alarm memory", false),

            // Byte 10 Zone 15 & 16 status flags (see byte 3)
            new CaddxProperty("", 10, 1, 0, 1, "Bit", "Zone 15 faulted (or delayed trip)", false),
            new CaddxProperty("", 10, 1, 1, 1, "Bit", "Zone 15 bypass (or inhibited)", false),
            new CaddxProperty("zone_15_trouble", 10, 1, 2, 1, "Bit", "Zone 15 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 10, 1, 3, 1, "Bit", "Zone 15 alarm memory", false),
            new CaddxProperty("", 10, 1, 4, 1, "Bit", "Zone 16 faulted (or delayed trip)", false),
            new CaddxProperty("", 10, 1, 5, 1, "Bit", "Zone 16 bypass (or inhibited)", false),
            new CaddxProperty("zone_16_trouble", 10, 1, 6, 1, "Bit", "Zone 16 trouble (tamper, low battery, or lost)",
                    false),
            new CaddxProperty("", 10, 1, 7, 1, "Bit", "Zone 16 alarm memory", false)),

    Partition_Status_Message(0x06, null, 9, "Partition Status Message",
            "This message will contain all information relevant to a single partition in the system.", CaddxDirection.In,
            CaddxSource.Partition,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            new CaddxProperty("partition_number", 2, 1, 0, 0, "Int", "Partition number (0= partition 1)", false),

            // Byte 3 Partition condition flags (1)
            new CaddxProperty("partition_bypass_code_required", 3, 1, 0, 1, "Bit", "Bypass code required", false),
            new CaddxProperty("partition_fire_trouble", 3, 1, 1, 1, "Bit", "Fire trouble", false),
            new CaddxProperty("partition_fire", 3, 1, 2, 1, "Bit", "Fire", false),
            new CaddxProperty("partition_pulsing_buzzer", 3, 1, 3, 1, "Bit", "Pulsing Buzzer", false),
            new CaddxProperty("partition_tlm_fault_memory", 3, 1, 4, 1, "Bit", "TLM fault memory", false),
            new CaddxProperty("partition_armed", 3, 1, 6, 1, "Bit", "Armed", false),
            new CaddxProperty("partition_instant", 3, 1, 7, 1, "Bit", "Instant", false),

            // Byte 4 Partition condition flags (2)
            new CaddxProperty("partition_previous_alarm", 4, 1, 0, 1, "Bit", "Previous Alarm", false),
            new CaddxProperty("partition_siren_on", 4, 1, 1, 1, "Bit", "Siren on", false),
            new CaddxProperty("partition_steady_siren_on", 4, 1, 2, 1, "Bit", "Steady siren on", false),
            new CaddxProperty("partition_alarm_memory", 4, 1, 3, 1, "Bit", "Alarm memory", false),
            new CaddxProperty("partition_tamper", 4, 1, 4, 1, "Bit", "Tamper", false),
            new CaddxProperty("partition_cancel_command_entered", 4, 1, 5, 1, "Bit", "Cancel command entered", false),
            new CaddxProperty("partition_code_entered", 4, 1, 6, 1, "Bit", "Code entered", false),
            new CaddxProperty("partition_cancel_pending", 4, 1, 7, 1, "Bit", "Cancel pending", false),

            // Byte 5 Partition condition flags (3)
            new CaddxProperty("partition_silent_exit_enabled", 5, 1, 1, 1, "Bit", "Silent exit enabled", false),
            new CaddxProperty("partition_entryguard", 5, 1, 2, 1, "Bit", "Entryguard (stay mode)", false),
            new CaddxProperty("partition_chime_mode_on", 5, 1, 3, 1, "Bit", "Chime mode on", false),
            new CaddxProperty("partition_entry", 5, 1, 4, 1, "Bit", "Entry", false),
            new CaddxProperty("partition_delay_expiration_warning", 5, 1, 5, 1, "Bit", "Delay expiration warning",
                    false),
            new CaddxProperty("partition_exit1", 5, 1, 6, 1, "Bit", "Exit1", false),
            new CaddxProperty("partition_exit2", 5, 1, 7, 1, "Bit", "Exit2", false),

            // Byte 6 Partition condition flags (4)
            new CaddxProperty("partition_led_extinguish", 6, 1, 0, 1, "Bit", "LED extinguish", false),
            new CaddxProperty("partition_cross_timing", 6, 1, 1, 1, "Bit", "Cross timing", false),
            new CaddxProperty("partition_recent_closing_being_timed", 6, 1, 2, 1, "Bit", "Recent closing being timed",
                    false),
            new CaddxProperty("partition_exit_error_triggered", 6, 1, 4, 1, "Bit", "Exit error triggered", false),
            new CaddxProperty("partition_auto_home_inhibited", 6, 1, 5, 1, "Bit", "Auto home inhibited", false),
            new CaddxProperty("partition_sensor_low_battery", 6, 1, 6, 1, "Bit", "Sensor low battery", false),
            new CaddxProperty("partition_sensor_lost_supervision", 6, 1, 7, 1, "Bit", "Sensor lost supervision", false),

            new CaddxProperty("", 7, 1, 0, 0, "Int", "Last user number", false),

            // Byte 8 Partition condition flags (5)
            new CaddxProperty("partition_zone_bypassed", 8, 1, 0, 1, "Bit", "Zone bypassed", false),
            new CaddxProperty("partition_force_arm_triggered_by_auto_arm", 8, 1, 1, 1, "Bit",
                    "Force arm triggered by auto arm", false),
            new CaddxProperty("partition_ready_to_arm", 8, 1, 2, 1, "Bit", "Ready to arm", false),
            new CaddxProperty("partition_ready_to_force_arm", 8, 1, 3, 1, "Bit", "Ready to force arm", false),
            new CaddxProperty("partition_valid_pin_accepted", 8, 1, 4, 1, "Bit", "Valid PIN accepted", false),
            new CaddxProperty("partition_chime_on", 8, 1, 5, 1, "Bit", "Chime on (sounding)", false),
            new CaddxProperty("partition_error_beep", 8, 1, 6, 1, "Bit", "Error beep (triple beep)", false),
            new CaddxProperty("partition_tone_on", 8, 1, 7, 1, "Bit", "Tone on (activation tone)", false),

            // Byte 9 Partition condition flags (6)
            new CaddxProperty("partition_entry1", 9, 1, 0, 1, "Bit", "Entry 1", false),
            new CaddxProperty("partition_open_period", 9, 1, 1, 1, "Bit", "Open period", false),
            new CaddxProperty("partition_alarm_sent_using_phone_number_1", 9, 1, 2, 1, "Bit",
                    "Alarm sent using phone number 1", false),
            new CaddxProperty("partition_alarm_sent_using_phone_number_2", 9, 1, 3, 1, "Bit",
                    "Alarm sent using phone number 2", false),
            new CaddxProperty("partition_alarm_sent_using_phone_number_3", 9, 1, 4, 1, "Bit",
                    "Alarm sent using phone number 3", false),
            new CaddxProperty("partition_cancel_report_is_in_the_stack", 9, 1, 5, 1, "Bit",
                    "Cancel report is in the stack", false),
            new CaddxProperty("partition_keyswitch_armed", 9, 1, 6, 1, "Bit", "Keyswitch armed", false),
            new CaddxProperty("partition_delay_trip_in_progress", 9, 1, 7, 1, "Bit",
                    "Delay Trip in progress (common zone)", false)),

    Partitions_Snapshot_Message(0x07, null, 9, "Partitions Snapshot Message",
            "This message will contain an abbreviated set of information for all 8 partitions on the system.",
            CaddxDirection.In, CaddxSource.Panel,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2 Partition 1 condition flags
            new CaddxProperty("partition_1_valid", 2, 1, 0, 1, "Bit", "Partition 1 valid partition", false),
            new CaddxProperty("", 2, 1, 1, 1, "Bit", "Partition 1 ready", false),
            new CaddxProperty("", 2, 1, 2, 1, "Bit", "Partition 1 armed", false),
            new CaddxProperty("", 2, 1, 3, 1, "Bit", "Partition 1 stay mode", false),
            new CaddxProperty("", 2, 1, 4, 1, "Bit", "Partition 1 chime mode", false),
            new CaddxProperty("", 2, 1, 5, 1, "Bit", "Partition 1 any entry delay", false),
            new CaddxProperty("", 2, 1, 6, 1, "Bit", "Partition 1 any exit delay", false),
            new CaddxProperty("", 2, 1, 7, 1, "Bit", "Partition 1 previous alarm", false),

            // Byte 3 Partition 2 condition flags
            new CaddxProperty("partition_2_valid", 3, 1, 0, 1, "Bit", "Partition 2 valid partition", false),
            new CaddxProperty("", 3, 1, 1, 1, "Bit", "Partition 2 ready", false),
            new CaddxProperty("", 3, 1, 2, 1, "Bit", "Partition 2 armed", false),
            new CaddxProperty("", 3, 1, 3, 1, "Bit", "Partition 2 stay mode", false),
            new CaddxProperty("", 3, 1, 4, 1, "Bit", "Partition 2 chime mode", false),
            new CaddxProperty("", 3, 1, 5, 1, "Bit", "Partition 2 any entry delay", false),
            new CaddxProperty("", 3, 1, 6, 1, "Bit", "Partition 2 any exit delay", false),
            new CaddxProperty("", 3, 1, 7, 1, "Bit", "Partition 2 previous alarm", false),

            // Byte 4 Partition 3 condition flags
            new CaddxProperty("partition_3_valid", 4, 1, 0, 1, "Bit", "Partition 3 valid partition", false),
            new CaddxProperty("", 4, 1, 1, 1, "Bit", "Partition 3 ready", false),
            new CaddxProperty("", 4, 1, 2, 1, "Bit", "Partition 3 armed", false),
            new CaddxProperty("", 4, 1, 3, 1, "Bit", "Partition 3 stay mode", false),
            new CaddxProperty("", 4, 1, 4, 1, "Bit", "Partition 3 chime mode", false),
            new CaddxProperty("", 4, 1, 5, 1, "Bit", "Partition 3 any entry delay", false),
            new CaddxProperty("", 4, 1, 6, 1, "Bit", "Partition 3 any exit delay", false),
            new CaddxProperty("", 4, 1, 7, 1, "Bit", "Partition 3 previous alarm", false),

            // Byte 5 Partition 4 condition flags
            new CaddxProperty("partition_4_valid", 5, 1, 0, 1, "Bit", "Partition 4 valid partition", false),
            new CaddxProperty("", 5, 1, 1, 1, "Bit", "Partition 4 ready", false),
            new CaddxProperty("", 5, 1, 2, 1, "Bit", "Partition 4 armed", false),
            new CaddxProperty("", 5, 1, 3, 1, "Bit", "Partition 4 stay mode", false),
            new CaddxProperty("", 5, 1, 4, 1, "Bit", "Partition 4 chime mode", false),
            new CaddxProperty("", 5, 1, 5, 1, "Bit", "Partition 4 any entry delay", false),
            new CaddxProperty("", 5, 1, 6, 1, "Bit", "Partition 4 any exit delay", false),
            new CaddxProperty("", 5, 1, 7, 1, "Bit", "Partition 4 previous alarm", false),

            // Byte 6 Partition 5 condition flags
            new CaddxProperty("partition_5_valid", 6, 1, 0, 1, "Bit", "Partition 5 valid partition", false),
            new CaddxProperty("", 6, 1, 1, 1, "Bit", "Partition 5 ready", false),
            new CaddxProperty("", 6, 1, 2, 1, "Bit", "Partition 5 armed", false),
            new CaddxProperty("", 6, 1, 3, 1, "Bit", "Partition 5 stay mode", false),
            new CaddxProperty("", 6, 1, 4, 1, "Bit", "Partition 5 chime mode", false),
            new CaddxProperty("", 6, 1, 5, 1, "Bit", "Partition 5 any entry delay", false),
            new CaddxProperty("", 6, 1, 6, 1, "Bit", "Partition 5 any exit delay", false),
            new CaddxProperty("", 6, 1, 7, 1, "Bit", "Partition 5 previous alarm", false),

            // Byte 7 Partition 6 condition flags
            new CaddxProperty("partition_6_valid", 7, 1, 0, 1, "Bit", "Partition 6 valid partition", false),
            new CaddxProperty("", 7, 1, 1, 1, "Bit", "Partition 6 ready", false),
            new CaddxProperty("", 7, 1, 2, 1, "Bit", "Partition 6 armed", false),
            new CaddxProperty("", 7, 1, 3, 1, "Bit", "Partition 6 stay mode", false),
            new CaddxProperty("", 7, 1, 4, 1, "Bit", "Partition 6 chime mode", false),
            new CaddxProperty("", 7, 1, 5, 1, "Bit", "Partition 6 any entry delay", false),
            new CaddxProperty("", 7, 1, 6, 1, "Bit", "Partition 6 any exit delay", false),
            new CaddxProperty("", 7, 1, 7, 1, "Bit", "Partition 6 previous alarm", false),

            // Byte 8 Partition 7 condition flags
            new CaddxProperty("partition_7_valid", 8, 1, 0, 1, "Bit", "Partition 7 valid partition", false),
            new CaddxProperty("", 8, 1, 1, 1, "Bit", "Partition 7 ready", false),
            new CaddxProperty("", 8, 1, 2, 1, "Bit", "Partition 7 armed", false),
            new CaddxProperty("", 8, 1, 3, 1, "Bit", "Partition 7 stay mode", false),
            new CaddxProperty("", 8, 1, 4, 1, "Bit", "Partition 7 chime mode", false),
            new CaddxProperty("", 8, 1, 5, 1, "Bit", "Partition 7 any entry delay", false),
            new CaddxProperty("", 8, 1, 6, 1, "Bit", "Partition 7 any exit delay", false),
            new CaddxProperty("", 8, 1, 7, 1, "Bit", "Partition 7 previous alarm", false),

            // Byte 9 Partition 8 condition flags
            new CaddxProperty("partition_8_valid", 9, 1, 0, 1, "Bit", "Partition 8 valid partition", false),
            new CaddxProperty("", 9, 1, 1, 1, "Bit", "Partition 8 ready", false),
            new CaddxProperty("", 9, 1, 2, 1, "Bit", "Partition 8 armed", false),
            new CaddxProperty("", 9, 1, 3, 1, "Bit", "Partition 8 stay mode", false),
            new CaddxProperty("", 9, 1, 4, 1, "Bit", "Partition 8 chime mode", false),
            new CaddxProperty("", 9, 1, 5, 1, "Bit", "Partition 8 any entry delay", false),
            new CaddxProperty("", 9, 1, 6, 1, "Bit", "Partition 8 any exit delay", false),
            new CaddxProperty("", 9, 1, 8, 1, "Bit", "Partition 8 previous alarm", false)),

    System_Status_Message(0x08, null, 12, "System Status Message",
            "This message will contain all information relevant to the entire system.", CaddxDirection.In, CaddxSource.Panel,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Panel ID number", false),

            // Byte 3
            new CaddxProperty("", 3, 1, 0, 1, "Bit", "Line seizure", false),
            new CaddxProperty("", 3, 1, 1, 1, "Bit", "Off hook", false),
            new CaddxProperty("", 3, 1, 2, 1, "Bit", "Initial handshake received", false),
            new CaddxProperty("", 3, 1, 3, 1, "Bit", "Download in progress", false),
            new CaddxProperty("", 3, 1, 4, 1, "Bit", "Dialer delay in progress", false),
            new CaddxProperty("", 3, 1, 5, 1, "Bit", "Using backup phone", false),
            new CaddxProperty("", 3, 1, 6, 1, "Bit", "Listen in active", false),
            new CaddxProperty("", 3, 1, 7, 1, "Bit", "Two way lockout", false),

            // Byte 4
            new CaddxProperty("", 4, 1, 0, 1, "Bit", "Ground fault", false),
            new CaddxProperty("", 4, 1, 1, 1, "Bit", "Phone fault", false),
            new CaddxProperty("", 4, 1, 2, 1, "Bit", "Fail to communicate", false),
            new CaddxProperty("", 4, 1, 3, 1, "Bit", "Fuse fault", false),
            new CaddxProperty("", 4, 1, 4, 1, "Bit", "Box tamper", false),
            new CaddxProperty("", 4, 1, 5, 1, "Bit", "Siren tamper / trouble", false),
            new CaddxProperty("", 4, 1, 6, 1, "Bit", "Low Battery", false),
            new CaddxProperty("", 4, 1, 7, 1, "Bit", "AC fail", false),

            // Byte 5
            new CaddxProperty("", 5, 1, 0, 1, "Bit", "Expander box tamper", false),
            new CaddxProperty("", 5, 1, 1, 1, "Bit", "Expander AC failure", false),
            new CaddxProperty("", 5, 1, 2, 1, "Bit", "Expander low battery", false),
            new CaddxProperty("", 5, 1, 3, 1, "Bit", "Expander loss of supervision", false),
            new CaddxProperty("", 5, 1, 4, 1, "Bit", "Expander auxiliary output over current", false),
            new CaddxProperty("", 5, 1, 5, 1, "Bit", "Auxiliary communication channel failure", false),
            new CaddxProperty("", 5, 1, 6, 1, "Bit", "Expander bell fault", false),

            // Byte 6
            new CaddxProperty("", 6, 1, 0, 1, "Bit", "6 digit PIN enabled", false),
            new CaddxProperty("", 6, 1, 1, 1, "Bit", "Programming token in use", false),
            new CaddxProperty("", 6, 1, 2, 1, "Bit", "PIN required for local download", false),
            new CaddxProperty("", 6, 1, 3, 1, "Bit", "Global pulsing buzzer", false),
            new CaddxProperty("", 6, 1, 4, 1, "Bit", "Global Siren on", false),
            new CaddxProperty("", 6, 1, 5, 1, "Bit", "Global steady siren", false),
            new CaddxProperty("", 6, 1, 6, 1, "Bit", "Bus device has line seized", false),
            new CaddxProperty("", 6, 1, 7, 1, "Bit", "Bus device has requested sniff mode", false),

            // Byte 7
            new CaddxProperty("", 7, 1, 0, 1, "Bit", "Dynamic battery test", false),
            new CaddxProperty("", 7, 1, 1, 1, "Bit", "AC power on", false),
            new CaddxProperty("", 7, 1, 2, 1, "Bit", "Low battery memory", false),
            new CaddxProperty("", 7, 1, 3, 1, "Bit", "Ground fault memory", false),
            new CaddxProperty("", 7, 1, 4, 1, "Bit", "Fire alarm verification being timed", false),
            new CaddxProperty("", 7, 1, 5, 1, "Bit", "Smoke power reset", false),
            new CaddxProperty("", 7, 1, 6, 1, "Bit", "50 Hz line power detected", false),
            new CaddxProperty("", 7, 1, 7, 1, "Bit", "Timing a high voltage battery charge", false),

            // Byte 8
            new CaddxProperty("", 8, 1, 0, 1, "Bit", "Communication since last autotest", false),
            new CaddxProperty("", 8, 1, 1, 1, "Bit", "Power up delay in progress", false),
            new CaddxProperty("", 8, 1, 2, 1, "Bit", "Walk test mode", false),
            new CaddxProperty("", 8, 1, 3, 1, "Bit", "Loss of system time", false),
            new CaddxProperty("", 8, 1, 4, 1, "Bit", "Enroll requested", false),
            new CaddxProperty("", 8, 1, 5, 1, "Bit", "Test fixture mode", false),
            new CaddxProperty("", 8, 1, 6, 1, "Bit", "Control shutdown mode", false),
            new CaddxProperty("", 8, 1, 7, 1, "Bit", "Timing a cancel window", false),

            // Byte 9
            new CaddxProperty("", 9, 1, 7, 1, "Bit", "Call back in progress", false),

            // Byte 10
            new CaddxProperty("", 10, 1, 0, 1, "Bit", "Phone line faulted", false),
            new CaddxProperty("", 10, 1, 1, 1, "Bit", "Voltage present interrupt active", false),
            new CaddxProperty("", 10, 1, 2, 1, "Bit", "House phone off hook", false),
            new CaddxProperty("", 10, 1, 3, 1, "Bit", "Phone line monitor enabled", false),
            new CaddxProperty("", 10, 1, 4, 1, "Bit", "Sniffing", false),
            new CaddxProperty("", 10, 1, 5, 1, "Bit", "Last read was off hook", false),
            new CaddxProperty("", 10, 1, 6, 1, "Bit", "Listen in requested", false),
            new CaddxProperty("", 10, 1, 7, 1, "Bit", "Listen in trigger", false),

            // Byte 11
            new CaddxProperty("", 11, 1, 0, 1, "Bit", "Valid partition 1", false),
            new CaddxProperty("", 11, 1, 1, 1, "Bit", "Valid partition 2", false),
            new CaddxProperty("", 11, 1, 2, 1, "Bit", "Valid partition 3", false),
            new CaddxProperty("", 11, 1, 3, 1, "Bit", "Valid partition 4", false),
            new CaddxProperty("", 11, 1, 4, 1, "Bit", "Valid partition 5", false),
            new CaddxProperty("", 11, 1, 5, 1, "Bit", "Valid partition 6", false),
            new CaddxProperty("", 11, 1, 6, 1, "Bit", "Valid partition 7", false),
            new CaddxProperty("", 11, 1, 7, 1, "Bit", "Valid partition 8", false),

            // Byte 12 Communicator stack pointer
            new CaddxProperty("panel_communicator_stack_pointer", 12, 1, 0, 0, "Int", "Communicator stack pointer",
                    false)),

    X10_Message_Received(0x09, null, 4, "X-10 Message Received",
            "This message contains information about an X-10 command that was requested by any device on the system bus.",
            CaddxDirection.In, CaddxSource.Panel,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, "Int", "House code (0=house A)", false),

            // Byte 3
            new CaddxProperty("", 3, 1, 0, 0, "Int", "Unit code (0=unit 1)", false),

            // Byte 4
            new CaddxProperty("", 4, 1, 0, 0, "Int", "X-10 function code", false)),

    Log_Event_Message(0x0a, null, 10, "Log Event Message",
            "This message will contain all information relating to an event in the log memory.", CaddxDirection.In,
            CaddxSource.Panel,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            // Byte 2
            new CaddxProperty("panel_log_event_number", 2, 1, 0, 0, "Int", "Event number of this message", false),
            // Byte 3
            new CaddxProperty("panel_log_event_size", 3, 1, 0, 0, "Int",
                    "Total log size (number of log entries allowed)", false),

            // Byte 4
            new CaddxProperty("panel_log_event_type", 4, 1, 0, 7, "Int", "Event type", false),
            // Bits 0-6 See type definitions in table that follows
            // Bit 7 Non-reporting event if not set

            // Byte 5
            new CaddxProperty("panel_log_event_zud", 5, 1, 0, 0, "Int", "Zone / User / Device number", false),
            // Byte 6
            new CaddxProperty("panel_log_event_partition", 6, 1, 0, 0, "Int",
                    "Partition number (0=partition 1, if relevant)", false),
            // Byte 7
            new CaddxProperty("panel_log_event_month", 7, 1, 0, 0, "Int", "Month (1-12)", false),
            // Byte 8
            new CaddxProperty("panel_log_event_day", 8, 1, 0, 0, "Int", "Day (1-31)", false),
            // Byte 9
            new CaddxProperty("panel_log_event_hour", 9, 1, 0, 0, "Int", "Hour (0-23)", false),
            // Byte 10
            new CaddxProperty("panel_log_event_minute", 10, 1, 0, 0, "Int", "Minute (0-59)", false)),

    Keypad_Message_Received(0x0b, null, 3, "Keypad Message Received",
            "This message contains a keystroke from a keypad that is in a Terminal Mode.", CaddxDirection.In, CaddxSource.Keypad,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("keypad_address", 1, 2, 0, 0, "Int", "Keypad address", false),

            // Byte 3
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Key value", false)),

    Program_Data_Reply(0x10, null, 13, "Program Data Reply",
            "This message will contain a system device’s buss address, logical location, and program data that was previously requested (via Program Data Request (3Ch)).",
            CaddxDirection.In, CaddxSource.Panel,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Device’s buss address", false),

            // Byte 3 Upper logical location / offset
            new CaddxProperty("", 3, 1, 0, 3, "Int", "Bits 8-11 of logical location", false),
            new CaddxProperty("", 3, 1, 4, 4, "Int", "Segment size (0=byte, 1=nibble)", false),
            new CaddxProperty("", 3, 1, 5, 1, "Bit", "Must be 0", false),
            new CaddxProperty("", 3, 1, 6, 6, "Int", "Segment offset (0-none, 1=8 bytes)", false),
            new CaddxProperty("", 3, 1, 7, 1, "Bit", "Must be 0", false),

            // Byte 4 Bits 0-7 of logical location
            new CaddxProperty("", 4, 1, 0, 0, "Int", "Bits 0-7 of logical location", false),

            // Byte 5 Location length / data type
            new CaddxProperty("", 5, 1, 0, 4, "Int", "Number of segments in location (0=1 segment)", false),
            new CaddxProperty("", 5, 1, 5, 7, "Int",
                    "Data type : 0=Binary 1=Decimal 2=Hexadecimal 3=ASCII 4=unused 5=unused 6=unused 7=unused", false),

            // Byte 6 Data byte
            new CaddxProperty("", 6, 1, 0, 0, "Int", "Data byte 0", false),
            // Byte 7 Data byte
            new CaddxProperty("", 7, 1, 0, 0, "Int", "Data byte 1", false),
            // Byte 8 Data byte
            new CaddxProperty("", 8, 1, 0, 0, "Int", "Data byte 2", false),
            // Byte 9 Data byte
            new CaddxProperty("", 9, 1, 0, 0, "Int", "Data byte 3", false),
            // Byte 10 Data byte
            new CaddxProperty("", 10, 1, 0, 0, "Int", "Data byte 4", false),
            // Byte 11 Data byte
            new CaddxProperty("", 11, 1, 0, 0, "Int", "Data byte 5", false),
            // Byte 12 Data byte
            new CaddxProperty("", 12, 1, 0, 0, "Int", "Data byte 6", false),
            // Byte 13 Data byte
            new CaddxProperty("", 13, 1, 0, 0, "Int", "Data byte 7", false)),

    User_Information_Reply(0x12, null, 7, "User Information Reply",
            "This message will contain all digits, attributes and partitions for the requested user PIN number that was previously requested (via User Information Request with(out) PIN (32h,33h)).",
            CaddxDirection.In, CaddxSource.Panel,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, "Int", "User Number (1=user 1)", false),

            // Byte 3 PIN digits 1 & 2
            new CaddxProperty("", 3, 1, 0, 3, "Int", "PIN digit 1", false),
            new CaddxProperty("", 3, 1, 4, 7, "Int", "PIN digit 2", false),

            // Byte 4 PIN digits 3 & 4
            new CaddxProperty("", 4, 1, 0, 3, "Int", "PIN digit 3", false),
            new CaddxProperty("", 4, 1, 4, 7, "Int", "PIN digit 4", false),

            // Byte 5 PIN digits 5 & 6
            new CaddxProperty("", 5, 1, 0, 3, "Int", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            new CaddxProperty("", 5, 1, 4, 7, "Int", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),

            // Byte 6* Authority flags
            new CaddxProperty("", 6, 1, 0, 1, "Bit",
                    "Reserved (if bit 7 is clear) || Output 1 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 1, 1, "Bit",
                    "Arm only (if bit 7 is clear) || Output 2 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 2, 1, "Bit",
                    "Arm only (during close window) (if bit 7 is clear) || Output 3 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 3, 1, "Bit",
                    "Master / program (if bit 7 is clear) || Output 4 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 4, 1, "Bit",
                    "Arm / Disarm (if bit 7 is clear) || Arm / Disarm (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 5, 1, "Bit",
                    "Bypass enable (if bit 7 is clear) || Bypass enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 6, 1, "Bit",
                    "Open / close report enable (if bit 7 is clear) || Open / close report enable (if bit 7 is set)",
                    false),
            new CaddxProperty("", 6, 1, 7, 1, "Bit", "Must be a 0 (if bit 7 is clear) || Must be a 1 (if bit 7 is set)",
                    false),

            // Byte 7 Authorized partition(s) mask
            new CaddxProperty("", 7, 1, 0, 1, "Bit", "Authorized for partition 1", false),
            new CaddxProperty("", 7, 1, 1, 1, "Bit", "Authorized for partition 2", false),
            new CaddxProperty("", 7, 1, 2, 1, "Bit", "Authorized for partition 3", false),
            new CaddxProperty("", 7, 1, 3, 1, "Bit", "Authorized for partition 4", false),
            new CaddxProperty("", 7, 1, 4, 1, "Bit", "Authorized for partition 5", false),
            new CaddxProperty("", 7, 1, 5, 1, "Bit", "Authorized for partition 6", false),
            new CaddxProperty("", 7, 1, 6, 1, "Bit", "Authorized for partition 7", false),
            new CaddxProperty("", 7, 1, 7, 1, "Bit", "Authorized for partition 8", false)),

    Request_Failed(0x1c, null, 1, "Command / Request Failed",
            "This message is sent in place of a ‘Positive Acknowledge’ message when a command or request was received properly, but the system was unable to carry out the task correctly. This would normally occur 2.5 seconds after receiving the initial command or request.",
            CaddxDirection.In, CaddxSource.Panel,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false)),

    Positive_Acknowledge(0x1d, null, 1, "Positive Acknowledge",
            "This message will acknowledge receipt of a message that had the ‘Acknowledge Required’ flag set in the command byte.",
            CaddxDirection.In, CaddxSource.Panel,
            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false)),

    Negative_Acknowledge(0x1e, null, 1, "Negative Acknowledge",
            "This message is sent in place of a ‘Positive Acknowledge’ message when the message received was not properly formatted. It will also be sent if an additional message is received before a reply has been returned during the 2.5 second allowable reply period of a previous message. An ‘Implied Negative Acknowledge’ is assumed when no acknowledge is returned with 3 seconds.",
            CaddxDirection.In, CaddxSource.Panel,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false)),

    Message_Rejected(0x1f, null, 1, "Message Rejected",
            "This message is sent in place of a ‘Positive Acknowledge’ message when the message was received properly formatted, but not supported or disabled.",
            CaddxDirection.In, CaddxSource.Panel,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false)),

    Interface_Configuration_Request(0x21, new int[] { 0x01, 0x1c, 0x1f }, 1, "Interface Configuration Request",
            "This request will cause the return of the Interface Configuration Message (01h) containing information about the options selected on the interface.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false)),

    Zone_Name_Request(0x23, new int[] { 0x03, 0x1c, 0x1f }, 2, "Zone Name Request",
            "This request will cause the return of the Zone Name Message (03h) for the zone number that was requested.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Zone number (0= zone 1)", true)),

    Zone_Status_Request(0x24, new int[] { 0x04, 0x1c, 0x1f }, 2, "Zone Status Request",
            "This request will cause the return of the Zone Status Message (04h) for the zone number that was requested.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("zone_number", 2, 1, 0, 0, "Int", "Zone number (0= zone 1)", true)),

    Zones_Snapshot_Request(0x25, new int[] { 0x05, 0x1c, 0x1f }, 2, "Zones Snapshot Request",
            "This request will cause the return of the Zones Snapshot Message (05h) with the group of zones starting at the zone 1 plus the offset value.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Zone number offset (0= start at zone 1)", true)),

    Partition_Status_Request(0x26, new int[] { 0x06, 0x1c, 0x1f }, 2, "Partition Status Request",
            "This request will cause the return of the Partition Status Message (06h) for the partition number that was requested.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("partition_number", 2, 1, 0, 0, "Int", "Partition number (0= partition 1)", true)),

    Partitions_Snapshot_Request(0x27, new int[] { 0x07, 0x1c, 0x1f }, 1, "Partitions Snapshot Request",
            "This request will cause the return of the Partitions Snapshot Message (07h) containing all partitions.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false)),

    System_Status_Request(0x28, new int[] { 0x08, 0x1c, 0x1f }, 1, "System Status Request",
            "This request will cause the return of the System Status Message (08h).", CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false)),

    Send_X_10_Message(0x29, new int[] { 0x1d, 0x1c, 0x1f }, 4, "Send X-10 Message",
            "This message will contain information about an X-10 command that should be resent on the system bus.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, "Int", "House code (0=house A) ", true),

            // Byte 3
            new CaddxProperty("", 3, 1, 0, 0, "Int", "Unit code (0=unit 1)", true),

            // Byte 4
            new CaddxProperty("", 4, 1, 0, 0, "Int", "X-10 function code (see table at message # 0Ah)", true)),

    Log_Event_Request(0x2a, new int[] { 0x0a, 0x1c, 0x1f }, 2, "Log Event Request",
            "This request will cause the return of the Log Event Message (0Ah).", CaddxDirection.Out, CaddxSource.None,
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            new CaddxProperty("panel_log_event_number", 2, 1, 0, 0, "Int", "Event number requested", true)),

    Send_Keypad_Text_Message(0x2b, new int[] { 0x1d, 0x1c, 0x1f }, 12, "Send Keypad Text Message",
            "This message will contain ASCII text for a specific keypad on the bus that will be displayed during Terminal Mode.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            // Byte 2 Keypad address
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Keypad address", false),
            // Byte 3 Keypad type (0=NX-148e)(all others not supported)
            new CaddxProperty("", 3, 1, 0, 0, "Int", "Keypad type", false),
            // Byte 4 Display storage location (0=top left corner
            new CaddxProperty("", 4, 1, 0, 0, "Int", "Display storage location", false),
            // Byte 5 ASCII character for location +0
            new CaddxProperty("", 5, 1, 0, 0, "Int", "ASCII character for location +0", false),
            // Byte 6 ASCII character for location +1
            new CaddxProperty("", 6, 1, 0, 0, "Int", "ASCII character for location +1", false),
            // Byte 7 ASCII character for location +2
            new CaddxProperty("", 7, 1, 0, 0, "Int", "ASCII character for location +2", false),
            // Byte 8 ASCII character for location +3
            new CaddxProperty("", 8, 1, 0, 0, "Int", "ASCII character for location +3", false),
            // Byte 9 ASCII character for location +4
            new CaddxProperty("", 9, 1, 0, 0, "Int", "ASCII character for location +4", false),
            // Byte 10 ASCII character for location +5
            new CaddxProperty("", 10, 1, 0, 0, "Int", "ASCII character for location +5", false),
            // Byte 11 ASCII character for location +6
            new CaddxProperty("", 11, 1, 0, 0, "Int", "ASCII character for location +6", false),
            // Byte 12 ASCII character for location +7
            new CaddxProperty("", 12, 1, 0, 0, "Int", "ASCII character for location +7", false)),

    Keypad_Terminal_Mode_Request(0x2c, new int[] { 0x1d, 0x1c, 0x1f }, 3, "Keypad Terminal Mode Request",
            "This message will contain the address of a keypad that should enter a Terminal Mode for the time contained. Only one keypad should be in the Terminal Mode at a time.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            // Byte 2 Keypad address
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Keypad address", false),
            // Byte 3
            new CaddxProperty("", 3, 1, 0, 0, "Int", "Number of seconds for Terminal Mode", false)),

    Program_Data_Request(0x30, new int[] { 0x10, 0x1c, 0x1f }, 4, "Program Data Request",
            "This message will contain a system device’s buss address and the logical location of program data that will be returned in a Program Data Reply message (10h).",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            // Byte 2 Device’s buss address
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Device’s buss address", false),
            // Byte 3 Upper logical location / offset
            // Bits 0-3 Bits 8-11 of logical location
            new CaddxProperty("", 3, 1, 0, 4, "Int", "Bits 8-11 of logical location", false),
            // Bits 4,5 Must be 0
            new CaddxProperty("", 3, 1, 4, 2, "Bit", "Must be 0", false),
            // Bit 6 Segment offset (0-none, 1=8 bytes)
            new CaddxProperty("", 3, 1, 6, 1, "Bit", "Segment offset (0-none, 1=8 bytes)", false),
            // Bit 7
            new CaddxProperty("", 3, 1, 7, 1, "Bit", "Must be 0", false),
            // Byte 4 Bits 0-7 of logical location
            new CaddxProperty("", 4, 1, 0, 0, "Int", "Bits 0-7 of logical location", false)),

    Program_Data_Command(0x31, new int[] { 0x1d, 0x1c, 0x1f }, 13, "Program Data Command",
            "This message will contain a system device’s buss address and the logical location where the included data should be stored.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            // Byte 2 Device’s buss address
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Device’s buss address", false),
            // Byte 3 Upper logical location / offset
            // Bits 0-3 Bits 8-11 of logical location
            new CaddxProperty("", 3, 1, 0, 4, "Bit", "Bits 8-11 of logical location", false),
            // Bit 4 Segment size (0=byte, 1=nibble)
            new CaddxProperty("", 3, 1, 4, 1, "Bit", "Segment size (0=byte, 1=nibble)", false),
            // Bit 5 Must be 1
            new CaddxProperty("", 3, 1, 5, 1, "Bit", "Must be 1", false),
            // Bit 6 Segment offset (0-none, 1=8 bytes)
            new CaddxProperty("", 3, 1, 6, 1, "Bit", "Segment offset (0-none, 1=8 bytes)", false),
            // Bit 7 Must be 0
            new CaddxProperty("", 3, 1, 7, 1, "Bit", "Must be 0", false),
            // Byte 4 Bits 0-7 of logical location
            new CaddxProperty("", 4, 1, 0, 0, "Int", "Bits 0-7 of logical location", false),
            // Byte 5 Location length / data type
            // Bits 0-4 Number of segments in location (0=1 segment)
            new CaddxProperty("", 5, 1, 0, 5, "Bit", "Number of segments in location (0=1 segment)", false),
            // Bits 5-7 Data type : 5=unused
            new CaddxProperty("", 5, 1, 5, 3, "Bit",
                    "Data type: 0=Binary, 1=Decimal, 2=Hexadecimal, 3=ASCII, 4=unused, 5=unused, 6=unused, 7=unused",
                    false),
            // Byte 6 Data byte 1 to store
            new CaddxProperty("", 6, 1, 0, 0, "Int", "Data byte 1 to store", false),
            // Byte 7 Data byte 2 to store
            new CaddxProperty("", 7, 1, 0, 0, "Int", "Data byte 2 to store", false),
            // Byte 8 Data byte 3 to store
            new CaddxProperty("", 8, 1, 0, 0, "Int", "Data byte 3 to store", false),
            // Byte 9 Data byte 4 to store
            new CaddxProperty("", 9, 1, 0, 0, "Int", "Data byte 4 to store", false),
            // Byte 10 Data byte 5 to store
            new CaddxProperty("", 10, 1, 0, 0, "Int", "Data byte 5 to store", false),
            // Byte 11 Data byte 6 to store
            new CaddxProperty("", 11, 1, 0, 0, "Int", "Data byte 6 to store", false),
            // Byte 12 Data byte 7 to store
            new CaddxProperty("", 12, 1, 0, 0, "Int", "Data byte 7 to store", false),
            // Byte 13 Data byte 8 to store
            new CaddxProperty("", 13, 1, 0, 0, "Int", "Data byte 8 to store", false)),

    User_Information_Request_with_PIN(0x32, new int[] { 0x12, 0x1c, 0x1f }, 5, "User Information Request with PIN",
            "This message will contain a user number for which information is being requested and a PIN that will be checked for Master capability before proceeding. The information will be returned in a User Information Reply message (12h).",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            // Byte 2 (Master) PIN digits 1 & 2
            // Bits 0-3 PIN digit 1
            new CaddxProperty("", 2, 1, 0, 4, "Bit", "PIN digit 1", false),
            // Bits 4-7 PIN digit 2
            new CaddxProperty("", 2, 1, 4, 4, "Bit", "PIN digit 2", false),
            // Byte 3 (Master) PIN digits 3 & 4
            // Bits 0-3 PIN digit 3
            new CaddxProperty("", 3, 1, 0, 4, "Bit", "PIN digit 3", false),
            // Bits 4-7 PIN digit 4
            new CaddxProperty("", 3, 1, 4, 4, "Bit", "PIN digit 4", false),
            // Byte 4 (Master) PIN digits 5 & 6
            // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 4, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 4, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),
            // Byte 5 User number (1=user 1)
            new CaddxProperty("", 5, 1, 0, 0, "Int", "User number (1=user 1)", false)),

    User_Information_Request_without_PIN(0x33, new int[] { 0x12, 0x1c, 0x1f }, 2,
            "User Information Request without PIN",
            "This message will contain a user number for which information is being requested, no authentication will be performed. The information will be returned in a User Information Reply message (12h).",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            // Byte 2 User number (1=user 1)
            new CaddxProperty("", 2, 1, 0, 0, "Int", "User number (1=user 1)", false)),

    Set_User_Code_Command_with_PIN(0x34, new int[] { 0x12, 0x1c, 0x1f }, 8, "Set User Code Command with PIN",
            "This message will contain all digits that should be stored as the new code for the designated User number. A PIN will be checked for Master capability before proceeding. A successful programming of the user code will result in the User Information Reply (12h) returned in place of the acknowledge.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            // Byte 2 (Master) PIN digits 1 & 2
            // Bits 0-3 PIN digit 1
            new CaddxProperty("", 2, 1, 0, 4, "Bit", "PIN digit 1", false),
            // Bits 4-7 PIN digit 2
            new CaddxProperty("", 2, 1, 4, 4, "Bit", "PIN digit 2", false),
            // Byte 3 (Master) PIN digits 3 & 4
            // Bits 0-3 PIN digit 3
            new CaddxProperty("", 3, 1, 0, 4, "Bit", "PIN digit 3", false),
            // Bits 4-7 PIN digit 4
            new CaddxProperty("", 3, 1, 4, 4, "Bit", "PIN digit 4", false),
            // Byte 4 (Master) PIN digits 5 & 6
            // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 4, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 4, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),
            // Byte 5 User number (1=user 1)
            new CaddxProperty("", 5, 1, 0, 0, "Int", "User number (1=user 1)", false),
            // Byte 6 PIN digits 1 & 2
            // Bits 0-3 PIN digit 1
            new CaddxProperty("", 6, 1, 0, 4, "Bit", "PIN digit 1", false),
            // Bits 4-7 PIN digit 2
            new CaddxProperty("", 6, 1, 4, 4, "Bit", "PIN digit 2", false),
            // Byte 7 PIN digits 3 & 4
            // Bits 0-3 PIN digit 3
            new CaddxProperty("", 7, 1, 0, 4, "Bit", "PIN digit 3", false),
            // Bits 4-7 PIN digit 4
            new CaddxProperty("", 7, 1, 4, 4, "Bit", "PIN digit 4", false),
            // Byte 8 PIN digits 5 & 6
            // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 8, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 8, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false)),

    Set_User_Code_Command_without_PIN(0x35, new int[] { 0x12, 0x1c, 0x1f }, 5, "Set User Code Command without PIN",
            "This message will contain all digits that should be stored as the new code for the designated User number. No authentication will be performed. A successful programming of the user code will result in the User Information Reply (12h) returned in place of the acknowledge.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),
            // Byte 2 User number (1=user 1)
            new CaddxProperty("", 2, 1, 0, 0, "Int", "User number (1=user 1)", false),
            // Byte 3 PIN digits 1 & 2
            // Bits 0-3 PIN digit 1
            new CaddxProperty("", 3, 1, 0, 4, "Bit", "PIN digit 1", false),
            // Bits 4-7 PIN digit 2
            new CaddxProperty("", 3, 1, 4, 4, "Bit", "PIN digit 2", false),
            // Byte 4 PIN digits 3 & 4
            // Bits 0-3 PIN digit 3
            new CaddxProperty("", 4, 1, 0, 4, "Bit", "PIN digit 3", false),
            // Bits 4-7 PIN digit 4
            new CaddxProperty("", 4, 1, 4, 4, "Bit", "PIN digit 4", false),
            // Byte 5 PIN digits 5 & 6
            // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 5, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 5, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false)),

    Set_User_Authorization_Command_with_PIN(0x36, new int[] { 0x1d, 0x1c, 0x1f }, 7,
            "Set User Authorization Command with PIN",
            "This message will contain all attributes and partitions that should be stored as the new information for the designated User number. A PIN will be checked for Master capability before proceeding.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2 (Master) PIN digits 1 & 2
            new CaddxProperty("", 2, 1, 0, 4, "Bit", "PIN digit 1", false),
            new CaddxProperty("", 2, 1, 4, 4, "Bit", "PIN digit 2", false),

            // Byte 3 (Master) PIN digits 3 & 4
            new CaddxProperty("", 3, 1, 0, 4, "Bit", "PIN digit 3", false),
            new CaddxProperty("", 3, 1, 4, 4, "Bit", "PIN digit 4", false),

            // Byte 4 (Master) PIN digits 5 & 6
            new CaddxProperty("", 4, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            new CaddxProperty("", 4, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),

            // Byte 5 User number (1=user 1)
            new CaddxProperty("", 5, 1, 0, 0, "Int", "User number (1=user 1)", false),

            // Byte 6 Authority flags
            new CaddxProperty("", 6, 1, 0, 1, "Bit",
                    "Reserved (if bit 7 is clear) || Output 1 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 1, 1, "Bit",
                    "Arm only (if bit 7 is clear) || Output 2 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 2, 1, "Bit",
                    "Arm only (during close window) (if bit 7 is clear) || Output 3 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 3, 1, "Bit",
                    "Master / program (if bit 7 is clear) || Output 4 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 4, 1, "Bit",
                    "Arm / Disarm (if bit 7 is clear) || Arm / Disarm (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 5, 1, "Bit",
                    "Bypass enable (if bit 7 is clear) || Bypass enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 6, 1, "Bit",
                    "Open / close report enable (if bit 7 is clear) || Open / close report enable (if bit 7 is set)",
                    false),
            new CaddxProperty("", 6, 1, 7, 1, "Bit", "Must be a 0 (if bit 7 is clear) || Must be a 1 (if bit 7 is set)",
                    false),

            // Byte 7 Authorized partition(s) mask
            new CaddxProperty("", 7, 1, 0, 1, "Bit", "Authorized for partition 1", false),
            new CaddxProperty("", 7, 1, 1, 1, "Bit", "Authorized for partition 2", false),
            new CaddxProperty("", 7, 1, 2, 1, "Bit", "Authorized for partition 3", false),
            new CaddxProperty("", 7, 1, 3, 1, "Bit", "Authorized for partition 4", false),
            new CaddxProperty("", 7, 1, 4, 1, "Bit", "Authorized for partition 5", false),
            new CaddxProperty("", 7, 1, 5, 1, "Bit", "Authorized for partition 6", false),
            new CaddxProperty("", 7, 1, 6, 1, "Bit", "Authorized for partition 7", false),
            new CaddxProperty("", 7, 1, 7, 1, "Bit", "Authorized for partition 8", false)),

    Set_User_Authorization_Command_without_PIN(0x37, new int[] { 0x1d, 0x1c, 0x1f }, 4,
            "Set User Authorization Command without PIN",
            "This message will contain all attributes and partitions that should be stored as the new information for the designated User number. No authentication will be performed.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2 User number (1=user 1)
            new CaddxProperty("", 2, 1, 0, 0, "Int", "User number (1=user 1)", false),

            // Byte 3 Authority flags
            new CaddxProperty("", 3, 1, 0, 1, "Bit",
                    "Reserved (if bit 7 is clear) || Output 1 enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 1, 1, "Bit",
                    "Arm only (if bit 7 is clear) || Output 2 enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 2, 1, "Bit",
                    "Arm only (during close window) (if bit 7 is clear) || Output 3 enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 3, 1, "Bit",
                    "Master / program (if bit 7 is clear) || Output 4 enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 4, 1, "Bit",
                    "Arm / Disarm (if bit 7 is clear) || Arm / Disarm (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 5, 1, "Bit",
                    "Bypass enable (if bit 7 is clear) || Bypass enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 6, 1, "Bit",
                    "Open / close report enable (if bit 7 is clear) || Open / close report enable (if bit 7 is set)",
                    false),
            new CaddxProperty("", 3, 1, 7, 1, "Bit", "Must be a 0 (if bit 7 is clear) || Must be a 1 (if bit 7 is set)",
                    false),

            // Byte 4 Authorized partition(s) mask
            new CaddxProperty("", 4, 1, 0, 1, "Bit", "Authorized for partition 1", false),
            new CaddxProperty("", 4, 1, 1, 1, "Bit", "Authorized for partition 2", false),
            new CaddxProperty("", 4, 1, 2, 1, "Bit", "Authorized for partition 3", false),
            new CaddxProperty("", 4, 1, 3, 1, "Bit", "Authorized for partition 4", false),
            new CaddxProperty("", 4, 1, 4, 1, "Bit", "Authorized for partition 5", false),
            new CaddxProperty("", 4, 1, 5, 1, "Bit", "Authorized for partition 6", false),
            new CaddxProperty("", 4, 1, 6, 1, "Bit", "Authorized for partition 7", false),
            new CaddxProperty("", 4, 1, 7, 1, "Bit", "Authorized for partition 8", false)),

    Store_Communication_Event_Command(0x3a, new int[] { 0x1d, 0x1c, 0x1f }, 6, "Store Communication Event Command",
            "This message will submit an event to the control’s communication stack for possible transmission over its telephone or alternate communications path.",
            CaddxDirection.Out, CaddxSource.None),

    Set_Clock_Calendar_Command(0x3b, new int[] { 0x1d, 0x1c, 0x1f }, 7, "Set Clock / Calendar Command",
            "This message will set the clock / calendar in the system.", CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2 Year (00-99)
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Year (00-99)", false),

            // Byte 3 Month (1-12)
            new CaddxProperty("", 3, 1, 0, 0, "Int", "Month (1-12)", false),

            // Byte 4 Day (1-31)
            new CaddxProperty("", 4, 1, 0, 0, "Int", "Day (1-31)", false),

            // Byte 5 Hour (0-23)
            new CaddxProperty("", 5, 1, 0, 0, "Int", "Hour (0-23)", false),

            // Byte 6 Minute (0-59)
            new CaddxProperty("", 6, 1, 0, 0, "Int", "Minute (0-59)", false),

            // Byte 7 Day
            new CaddxProperty("", 7, 1, 0, 0, "Int", "Day", false)),

    Primary_Keypad_Function_with_PIN(0x3c, new int[] { 0x1d, 0x1c, 0x1f }, 6, "Primary Keypad Function with PIN",
            "This message will contain a value that defines with function to perform, the partitions to use and a PIN value for the validation.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2 PIN digits 1 & 2
            new CaddxProperty("", 2, 1, 0, 4, "Bit", "PIN digit 1", false),
            new CaddxProperty("", 2, 1, 4, 4, "Bit", "PIN digit 2", false),

            // Byte 3 PIN digits 3 & 4
            new CaddxProperty("", 3, 1, 0, 4, "Bit", "PIN digit 3", false),
            new CaddxProperty("", 3, 1, 4, 4, "Bit", "PIN digit 4", false),

            // Byte 4 PIN digits 5 & 6
            new CaddxProperty("", 4, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            new CaddxProperty("", 4, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),

            // Byte 5 Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm
            // in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode,
            // 08h-FFh Reserved]
            new CaddxProperty("", 5, 1, 0, 0, "Int",
                    "Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode, 08h-FFh Reserved]",
                    false),

            // Byte 6 Partition mask
            new CaddxProperty("", 6, 1, 0, 1, "Bit", "Perform on partition 1 (if PIN has access)", false),
            new CaddxProperty("", 6, 1, 1, 1, "Bit", "Perform on partition 2 (if PIN has access)", false),
            new CaddxProperty("", 6, 1, 2, 1, "Bit", "Perform on partition 3 (if PIN has access)", false),
            new CaddxProperty("", 6, 1, 3, 1, "Bit", "Perform on partition 4 (if PIN has access)", false),
            new CaddxProperty("", 6, 1, 4, 1, "Bit", "Perform on partition 5 (if PIN has access)", false),
            new CaddxProperty("", 6, 1, 5, 1, "Bit", "Perform on partition 6 (if PIN has access)", false),
            new CaddxProperty("", 6, 1, 6, 1, "Bit", "Perform on partition 7 (if PIN has access)", false),
            new CaddxProperty("", 6, 1, 7, 1, "Bit", "Perform on partition 8 (if PIN has access)", false)),

    Primary_Keypad_Function_without_PIN(0x3d, new int[] { 0x1d, 0x1c, 0x1f }, 4, "Primary Keypad Function without PIN",
            "This message will contain a value that defines with function to perform, the partitions and user number to assign to the function.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2 "Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm
            // in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode,
            // 08h-FFh Reserved]",
            new CaddxProperty("", 2, 1, 0, 0, "Int",
                    "Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode, 08h-FFh Reserved]",
                    false),

            // Byte 3 Partition mask
            new CaddxProperty("", 3, 1, 0, 1, "Bit", "Perform on partition 1 (if PIN has access)", false),
            new CaddxProperty("", 3, 1, 1, 1, "Bit", "Perform on partition 2 (if PIN has access)", false),
            new CaddxProperty("", 3, 1, 2, 1, "Bit", "Perform on partition 3 (if PIN has access)", false),
            new CaddxProperty("", 3, 1, 3, 1, "Bit", "Perform on partition 4 (if PIN has access)", false),
            new CaddxProperty("", 3, 1, 4, 1, "Bit", "Perform on partition 5 (if PIN has access)", false),
            new CaddxProperty("", 3, 1, 5, 1, "Bit", "Perform on partition 6 (if PIN has access)", false),
            new CaddxProperty("", 3, 1, 6, 1, "Bit", "Perform on partition 7 (if PIN has access)", false),
            new CaddxProperty("", 3, 1, 7, 1, "Bit", "Perform on partition 8 (if PIN has access)", false),

            // Byte 4 User number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "User number", false)),

    Secondary_Keypad_Function(0x3e, new int[] { 0x1d, 0x1c, 0x1f }, 4, "Secondary Keypad Function",
            "This message will contain a value that defines with function to perform, and the partitions to use.",
            CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2 "Keypad function [00h Stay (1 button arm / toggle interiors), 01h Chime (toggle chime mode),
            // 02h Exit (1 button arm / toggle instant), 03h Bypass interiors, 04h Fire panic, 05h Medical panic,
            // 06h Police panic, 07h Smoke detector reset, 08h Auto callback download, 09h Manual pickup download,
            // 0Ah Enable silent exit (for this arm cycle), 0Bh Perform test, 0Ch Group bypass, 0Dh Auxiliary
            // function 1, 0Eh Auxiliary function 2, 0Fh Start keypad sounder, 10h-FFh Reserved]",
            new CaddxProperty("", 2, 1, 0, 0, "Int",
                    "Keypad function [00h Stay (1 button arm / toggle interiors), 01h Chime (toggle chime mode), 02h Exit (1 button arm / toggle instant), 03h Bypass interiors, 04h Fire panic, 05h Medical panic, 06h Police panic, 07h Smoke detector reset, 08h Auto callback download, 09h Manual pickup download, 0Ah Enable silent exit (for this arm cycle), 0Bh Perform test, 0Ch Group bypass, 0Dh Auxiliary function 1, 0Eh Auxiliary function 2, 0Fh Start keypad sounder, 10h-FFh Reserved]",
                    false),

            // Byte 3 Partition mask
            new CaddxProperty("", 3, 1, 0, 1, "Bit", "Perform on partition 1", false),
            new CaddxProperty("", 3, 1, 1, 1, "Bit", "Perform on partition 2", false),
            new CaddxProperty("", 3, 1, 2, 1, "Bit", "Perform on partition 3", false),
            new CaddxProperty("", 3, 1, 3, 1, "Bit", "Perform on partition 4", false),
            new CaddxProperty("", 3, 1, 4, 1, "Bit", "Perform on partition 5", false),
            new CaddxProperty("", 3, 1, 5, 1, "Bit", "Perform on partition 6", false),
            new CaddxProperty("", 3, 1, 6, 1, "Bit", "Perform on partition 7", false),
            new CaddxProperty("", 3, 1, 7, 1, "Bit", "Perform on partition 8", false)),

    Zone_Bypass_Toggle(0x3f, new int[] { 0x1d, 0x1c, 0x1f }, 2, "Zone Bypass Toggle",
            "This message will contain a number of a zone that should be (un)bypassed.", CaddxDirection.Out, CaddxSource.None,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, "Int", "Message number", false),

            // Byte 2 Zone number (0= zone 1)
            new CaddxProperty("", 2, 1, 0, 0, "Int", "Zone number (0= zone 1)", false));

    public final String name;
    public final String description;
    public final int number;
    public final int @Nullable [] replyMessageNumbers;
    public final int length;
    public final CaddxDirection direction;
    public final CaddxSource source;
    public final CaddxProperty[] properties;

    CaddxMessageType(int number, int @Nullable [] replyMessageNumbers, int length, String name, String description,
            CaddxDirection direction, CaddxSource source, CaddxProperty... properties) {
        this.name = name;
        this.description = description;
        this.direction = direction;
        this.source = source;
        this.number = number;
        this.replyMessageNumbers = replyMessageNumbers;
        this.length = length;
        this.properties = properties;
    }

    private static final Map<Integer, CaddxMessageType> BY_MESSAGE_TYPE = new HashMap<>();

    static {
        for (CaddxMessageType mt : values()) {
            BY_MESSAGE_TYPE.put(mt.number, mt);
        }
    }

    public static CaddxMessageType valueOfMessageType(int number) {
        return BY_MESSAGE_TYPE.get(number);
    }
}
