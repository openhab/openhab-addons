/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.caddx.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * All the panel message types
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public enum CaddxMessageType {

    INTERFACE_CONFIGURATION_MESSAGE(0x01, null, 12, "Interface Configuration Message",
            "This message will contain the firmware version number and other information about features currently enabled. It will be sent each time the unit is reset or programmed.",
            CaddxDirection.IN, CaddxSource.PANEL,

            // Properties
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            new CaddxProperty("panel_firmware_version", 2, 4, 0, 0, CaddxPropertyType.STRING, "Firmware version",
                    false),

            // Byte 6 Supported transition message flags (1)
            new CaddxProperty("panel_interface_configuration_message", 6, 1, 1, 1, CaddxPropertyType.BIT,
                    "Interface Configuration Message", false),
            new CaddxProperty("panel_zone_status_message", 6, 1, 4, 1, CaddxPropertyType.BIT, "Zone Status Message",
                    false),
            new CaddxProperty("panel_zones_snapshot_message", 6, 1, 5, 1, CaddxPropertyType.BIT,
                    "Zones Snapshot Message", false),
            new CaddxProperty("panel_partition_status_message", 6, 1, 6, 1, CaddxPropertyType.BIT,
                    "Partition Status Message", false),
            new CaddxProperty("panel_partitions_snapshot_message", 6, 1, 7, 1, CaddxPropertyType.BIT,
                    "Partitions Snapshot Message", false),

            // Byte 7 Supported transition message flags (2)
            new CaddxProperty("panel_system_status_message", 7, 1, 0, 1, CaddxPropertyType.BIT, "System Status Message",
                    false),
            new CaddxProperty("panel_x10_message_received", 7, 1, 1, 1, CaddxPropertyType.BIT, "X-10 Message Received",
                    false),
            new CaddxProperty("panel_log_event_message", 7, 1, 2, 1, CaddxPropertyType.BIT, "Log Event Message", false),
            new CaddxProperty("panel_keypad_message_received", 7, 1, 3, 1, CaddxPropertyType.BIT,
                    "Keypad Message Received", false),

            // Byte 8 Supported request / command flags (1)
            new CaddxProperty("panel_interface_configuration_request", 8, 1, 1, 1, CaddxPropertyType.BIT,
                    "Interface Configuration Request", false),
            new CaddxProperty("panel_zone_name_request", 8, 1, 3, 1, CaddxPropertyType.BIT, "Zone Name Request", false),
            new CaddxProperty("panel_zone_status_request", 8, 1, 4, 1, CaddxPropertyType.BIT, "Zone Status Request",
                    false),
            new CaddxProperty("panel_zones_snapshot_request", 8, 1, 5, 1, CaddxPropertyType.BIT,
                    "Zones Snapshot Request", false),
            new CaddxProperty("panel_partition_status_request", 8, 1, 6, 1, CaddxPropertyType.BIT,
                    "Partition Status Request", false),
            new CaddxProperty("panel_partitions_snapshot_request", 8, 1, 7, 1, CaddxPropertyType.BIT,
                    "Partitions Snapshot Request", false),

            // Byte 9 Supported request / command flags (2)
            new CaddxProperty("panel_system_status_request", 9, 1, 0, 1, CaddxPropertyType.BIT, "System Status Request",
                    false),
            new CaddxProperty("panel_send_x10_message", 9, 1, 1, 1, CaddxPropertyType.BIT, "Send X-10 Message", false),
            new CaddxProperty("panel_log_event_request", 9, 1, 2, 1, CaddxPropertyType.BIT, "Log Event Request", false),
            new CaddxProperty("panel_send_keypad_text_message", 9, 1, 3, 1, CaddxPropertyType.BIT,
                    "Send Keypad Text Message", false),
            new CaddxProperty("panel_keypad_terminal_mode_request", 9, 1, 4, 1, CaddxPropertyType.BIT,
                    "Keypad Terminal Mode Request", false),

            // Byte 10 Supported request / command flags (3)
            new CaddxProperty("panel_program_data_request", 10, 1, 0, 1, CaddxPropertyType.BIT, "Program Data Request",
                    false),
            new CaddxProperty("panel_program_data_command", 10, 1, 1, 1, CaddxPropertyType.BIT, "Program Data Command",
                    false),
            new CaddxProperty("panel_user_information_request_with_pin", 10, 1, 2, 1, CaddxPropertyType.BIT,
                    "User Information Request with PIN", false),
            new CaddxProperty("panel_user_information_request_without_pin", 10, 1, 3, 1, CaddxPropertyType.BIT,
                    "User Information Request without PIN", false),
            new CaddxProperty("panel_set_user_code_command_with_pin", 10, 1, 4, 1, CaddxPropertyType.BIT,
                    "Set User Code Command with PIN", false),
            new CaddxProperty("panel_set_user_code_command_without_pin", 10, 1, 5, 1, CaddxPropertyType.BIT,
                    "Set User Code Command without PIN", false),
            new CaddxProperty("panel_set_user_authorization_command_with_pin", 10, 1, 6, 1, CaddxPropertyType.BIT,
                    "Set User Authorization Command with PIN", false),
            new CaddxProperty("panel_set_user_authorization_command_without_pin", 10, 1, 7, 1, CaddxPropertyType.BIT,
                    "Set User Authorization Command without PIN", false),

            // Byte 11 Supported request / command flags (4)
            new CaddxProperty("panel_store_communication_event_command", 11, 1, 2, 1, CaddxPropertyType.BIT,
                    "Store Communication Event Command", false),
            new CaddxProperty("panel_set_clock_calendar_command", 11, 1, 3, 1, CaddxPropertyType.BIT,
                    "Set Clock / Calendar Command", false),
            new CaddxProperty("panel_primary_keypad_function_with_pin", 11, 1, 4, 1, CaddxPropertyType.BIT,
                    "Primary Keypad Function with PIN", false),
            new CaddxProperty("panel_primary_keypad_function_without_pin", 11, 1, 5, 1, CaddxPropertyType.BIT,
                    "Primary Keypad Function without PIN", false),
            new CaddxProperty("panel_secondary_keypad_function", 11, 1, 6, 1, CaddxPropertyType.BIT,
                    "Secondary Keypad Function", false),
            new CaddxProperty("panel_zone_bypass_toggle", 11, 1, 7, 1, CaddxPropertyType.BIT, "Zone Bypass Toggle",
                    false)),

    ZONE_NAME_MESSAGE(0x03, null, 18, "Zone Name Message",
            "This message will contain the 16-character name for the zone number that was requested (via Zone Name Request (23h)).",
            CaddxDirection.IN, CaddxSource.ZONE,

            // Properties
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            new CaddxProperty("zone_number", 2, 1, 0, 0, CaddxPropertyType.INT, "Zone number", false),
            new CaddxProperty("zone_name", 3, 16, 0, 0, CaddxPropertyType.STRING, "Zone name", false)),

    ZONE_STATUS_MESSAGE(0x04, null, 8, "Zone Status Message",
            "This message will contain all information relevant to a zone in the system.", CaddxDirection.IN,
            CaddxSource.ZONE,

            // Properties
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            new CaddxProperty("zone_number", 2, 1, 0, 0, CaddxPropertyType.INT, "Zone number", false),

            // Byte 3 Partition mask
            new CaddxProperty("zone_partition1", 3, 1, 0, 1, CaddxPropertyType.BIT, "Partition 1 enable", false),
            new CaddxProperty("zone_partition2", 3, 1, 1, 1, CaddxPropertyType.BIT, "Partition 2 enable", false),
            new CaddxProperty("zone_partition3", 3, 1, 2, 1, CaddxPropertyType.BIT, "Partition 3 enable", false),
            new CaddxProperty("zone_partition4", 3, 1, 3, 1, CaddxPropertyType.BIT, "Partition 4 enable", false),
            new CaddxProperty("zone_partition5", 3, 1, 4, 1, CaddxPropertyType.BIT, "Partition 5 enable", false),
            new CaddxProperty("zone_partition6", 3, 1, 5, 1, CaddxPropertyType.BIT, "Partition 6 enable", false),
            new CaddxProperty("zone_partition7", 3, 1, 6, 1, CaddxPropertyType.BIT, "Partition 7 enable", false),
            new CaddxProperty("zone_partition8", 3, 1, 7, 1, CaddxPropertyType.BIT, "Partition 8 enable", false),

            // Byte 4 Zone type flags (1)
            new CaddxProperty("zone_fire", 4, 1, 0, 1, CaddxPropertyType.BIT, "Fire", false),
            new CaddxProperty("zone_24hour", 4, 1, 1, 1, CaddxPropertyType.BIT, "24 Hour", false),
            new CaddxProperty("zone_key_switch", 4, 1, 2, 1, CaddxPropertyType.BIT, "Key-switch", false),
            new CaddxProperty("zone_follower", 4, 1, 3, 1, CaddxPropertyType.BIT, "Follower", false),
            new CaddxProperty("zone_entry_exit_delay_1", 4, 1, 4, 1, CaddxPropertyType.BIT, "Entry / exit delay 1",
                    false),
            new CaddxProperty("zone_entry_exit_delay_2", 4, 1, 5, 1, CaddxPropertyType.BIT, "Entry / exit delay 2",
                    false),
            new CaddxProperty("zone_interior", 4, 1, 6, 1, CaddxPropertyType.BIT, "Interior", false),
            new CaddxProperty("zone_local_only", 4, 1, 7, 1, CaddxPropertyType.BIT, "Local only", false),

            // Byte 5 Zone type flags (2)
            new CaddxProperty("zone_keypad_sounder", 5, 1, 0, 1, CaddxPropertyType.BIT, "Keypad sounder", false),
            new CaddxProperty("zone_yelping_siren", 5, 1, 1, 1, CaddxPropertyType.BIT, "Yelping siren", false),
            new CaddxProperty("zone_steady_siren", 5, 1, 2, 1, CaddxPropertyType.BIT, "Steady siren", false),
            new CaddxProperty("zone_chime", 5, 1, 3, 1, CaddxPropertyType.BIT, "Chime", false),
            new CaddxProperty("zone_bypassable", 5, 1, 4, 1, CaddxPropertyType.BIT, "Bypassable", false),
            new CaddxProperty("zone_group_bypassable", 5, 1, 5, 1, CaddxPropertyType.BIT, "Group bypassable", false),
            new CaddxProperty("zone_force_armable", 5, 1, 6, 1, CaddxPropertyType.BIT, "Force armable", false),
            new CaddxProperty("zone_entry_guard", 5, 1, 7, 1, CaddxPropertyType.BIT, "Entry guard", false),

            // Byte 6 Zone type flags (3)
            new CaddxProperty("zone_fast_loop_response", 6, 1, 0, 1, CaddxPropertyType.BIT, "Fast loop response",
                    false),
            new CaddxProperty("zone_double_eol_tamper", 6, 1, 1, 1, CaddxPropertyType.BIT, "Double EOL tamper", false),
            new CaddxProperty("zone_type_trouble", 6, 1, 2, 1, CaddxPropertyType.BIT, "Trouble", false),
            new CaddxProperty("zone_cross_zone", 6, 1, 3, 1, CaddxPropertyType.BIT, "Cross zone", false),
            new CaddxProperty("zone_dialer_delay", 6, 1, 4, 1, CaddxPropertyType.BIT, "Dialer delay", false),
            new CaddxProperty("zone_swinger_shutdown", 6, 1, 5, 1, CaddxPropertyType.BIT, "Swinger shutdown", false),
            new CaddxProperty("zone_restorable", 6, 1, 6, 1, CaddxPropertyType.BIT, "Restorable", false),
            new CaddxProperty("zone_listen_in", 6, 1, 7, 1, CaddxPropertyType.BIT, "Listen in", false),

            // Byte 7 Zone condition flags (1)
            new CaddxProperty("zone_faulted", 7, 1, 0, 1, CaddxPropertyType.BIT, "Faulted (or delayed trip)", false),
            new CaddxProperty("zone_tampered", 7, 1, 1, 1, CaddxPropertyType.BIT, "Tampered", false),
            new CaddxProperty("zone_trouble", 7, 1, 2, 1, CaddxPropertyType.BIT, "Trouble", false),
            new CaddxProperty("zone_bypassed", 7, 1, 3, 1, CaddxPropertyType.BIT, "Bypassed", false),
            new CaddxProperty("zone_inhibited", 7, 1, 4, 1, CaddxPropertyType.BIT, "Inhibited (force armed)", false),
            new CaddxProperty("zone_low_battery", 7, 1, 5, 1, CaddxPropertyType.BIT, "Low battery", false),
            new CaddxProperty("zone_loss_of_supervision", 7, 1, 6, 1, CaddxPropertyType.BIT, "Loss of supervision",
                    false),

            // Byte 8 Zone condition flags (2)
            new CaddxProperty("zone_alarm_memory", 8, 1, 0, 1, CaddxPropertyType.BIT, "Alarm memory", false),
            new CaddxProperty("zone_bypass_memory", 8, 1, 1, 1, CaddxPropertyType.BIT, "Bypass memory", false)),

    ZONES_SNAPSHOT_MESSAGE(0x05, null, 10, "Zones Snapshot Message",
            "This message will contain an abbreviated set of information for any group of 16 zones possible on the system. (A zone offset number will set the range of zones)",
            CaddxDirection.IN, CaddxSource.PANEL,

            // Properties
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            new CaddxProperty("zone_offset", 2, 1, 0, 0, CaddxPropertyType.INT, "Zone offset (0= start at zone 1)",
                    false),

            // Byte 3 Zone 1 & 2 (+offset) status flags
            new CaddxProperty("zone_1_faulted", 3, 1, 0, 1, CaddxPropertyType.BIT, "Zone 1 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_1_bypassed", 3, 1, 1, 1, CaddxPropertyType.BIT, "Zone 1 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_1_trouble", 3, 1, 2, 1, CaddxPropertyType.BIT,
                    "Zone 1 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_1_alarm_memory", 3, 1, 3, 1, CaddxPropertyType.BIT, "Zone 1 alarm memory", false),
            new CaddxProperty("zone_2_faulted", 3, 1, 4, 1, CaddxPropertyType.BIT, "Zone 2 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_2_bypassed", 3, 1, 5, 1, CaddxPropertyType.BIT, "Zone 2 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_2_trouble", 3, 1, 6, 1, CaddxPropertyType.BIT,
                    "Zone 2 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_2_alarm_memory", 3, 1, 7, 1, CaddxPropertyType.BIT, "Zone 2 alarm memory", false),

            // Byte 4 Zone 3 & 4 status flags (see byte 3)
            new CaddxProperty("zone_3_faulted", 4, 1, 0, 1, CaddxPropertyType.BIT, "Zone 3 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_3_bypassed", 4, 1, 1, 1, CaddxPropertyType.BIT, "Zone 3 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_3_trouble", 4, 1, 2, 1, CaddxPropertyType.BIT,
                    "Zone 3 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_3_alarm_memory", 4, 1, 3, 1, CaddxPropertyType.BIT, "Zone 3 alarm memory", false),
            new CaddxProperty("zone_4_faulted", 4, 1, 4, 1, CaddxPropertyType.BIT, "Zone 4 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_4_bypassed", 4, 1, 5, 1, CaddxPropertyType.BIT, "Zone 4 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_4_trouble", 4, 1, 6, 1, CaddxPropertyType.BIT,
                    "Zone 4 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_4_alarm_memory", 4, 1, 7, 1, CaddxPropertyType.BIT, "Zone 4 alarm memory", false),

            // Byte 5 Zone 5 & 6 status flags (see byte 3)
            new CaddxProperty("zone_5_faulted", 5, 1, 0, 1, CaddxPropertyType.BIT, "Zone 5 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_5_bypassed", 5, 1, 1, 1, CaddxPropertyType.BIT, "Zone 5 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_5_trouble", 5, 1, 2, 1, CaddxPropertyType.BIT,
                    "Zone 5 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_5_alarm_memory", 5, 1, 3, 1, CaddxPropertyType.BIT, "Zone 5 alarm memory", false),
            new CaddxProperty("zone_6_faulted", 5, 1, 4, 1, CaddxPropertyType.BIT, "Zone 6 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_6_bypassed", 5, 1, 5, 1, CaddxPropertyType.BIT, "Zone 6 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_6_trouble", 5, 1, 6, 1, CaddxPropertyType.BIT,
                    "Zone 6 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_6_alarm_memory", 5, 1, 7, 1, CaddxPropertyType.BIT, "Zone 6 alarm memory", false),

            // Byte 6 Zone 7 & 8 status flags (see byte 3)
            new CaddxProperty("zone_7_faulted", 6, 1, 0, 1, CaddxPropertyType.BIT, "Zone 7 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_7_bypassed", 6, 1, 1, 1, CaddxPropertyType.BIT, "Zone 7 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_7_trouble", 6, 1, 2, 1, CaddxPropertyType.BIT,
                    "Zone 7 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_7_alarm_memory", 6, 1, 3, 1, CaddxPropertyType.BIT, "Zone 7 alarm memory", false),
            new CaddxProperty("zone_8_faulted", 6, 1, 4, 1, CaddxPropertyType.BIT, "Zone 8 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_8_bypassed", 6, 1, 5, 1, CaddxPropertyType.BIT, "Zone 8 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_8_trouble", 6, 1, 6, 1, CaddxPropertyType.BIT,
                    "Zone 8 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_8_alarm_memory", 6, 1, 7, 1, CaddxPropertyType.BIT, "Zone 8 alarm memory", false),

            // Byte 7 Zone 9 & 10 status flags (see byte 3)
            new CaddxProperty("zone_9_faulted", 7, 1, 0, 1, CaddxPropertyType.BIT, "Zone 9 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_9_bypassed", 7, 1, 1, 1, CaddxPropertyType.BIT, "Zone 9 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_9_trouble", 7, 1, 2, 1, CaddxPropertyType.BIT,
                    "Zone 9 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_9_alarm_memory", 7, 1, 3, 1, CaddxPropertyType.BIT, "Zone 9 alarm memory", false),
            new CaddxProperty("zone_10_faulted", 7, 1, 4, 1, CaddxPropertyType.BIT, "Zone 10 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_10_bypassed", 7, 1, 5, 1, CaddxPropertyType.BIT, "Zone 10 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_10_trouble", 7, 1, 6, 1, CaddxPropertyType.BIT,
                    "Zone 10 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_10_alarm_memory", 7, 1, 7, 1, CaddxPropertyType.BIT, "Zone 10 alarm memory", false),

            // Byte 8 Zone 11 & 12 status flags (see byte 3)
            new CaddxProperty("zone_11_faulted", 8, 1, 0, 1, CaddxPropertyType.BIT, "Zone 11 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_11_bypassed", 8, 1, 1, 1, CaddxPropertyType.BIT, "Zone 11 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_11_trouble", 8, 1, 2, 1, CaddxPropertyType.BIT,
                    "Zone 11 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_11_alarm_memory", 8, 1, 3, 1, CaddxPropertyType.BIT, "Zone 11 alarm memory", false),
            new CaddxProperty("zone_12_faulted", 8, 1, 4, 1, CaddxPropertyType.BIT, "Zone 12 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_12_bypassed", 8, 1, 5, 1, CaddxPropertyType.BIT, "Zone 12 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_12_trouble", 8, 1, 6, 1, CaddxPropertyType.BIT,
                    "Zone 12 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_12_alarm_memory", 8, 1, 7, 1, CaddxPropertyType.BIT, "Zone 12 alarm memory", false),

            // Byte 9 Zone 13 & 14 status flags (see byte 3)
            new CaddxProperty("zone_13_faulted", 9, 1, 0, 1, CaddxPropertyType.BIT, "Zone 13 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_13_bypassed", 9, 1, 1, 1, CaddxPropertyType.BIT, "Zone 13 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_13_trouble", 9, 1, 2, 1, CaddxPropertyType.BIT,
                    "Zone 13 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_13_alarm_memory", 9, 1, 3, 1, CaddxPropertyType.BIT, "Zone 13 alarm memory", false),
            new CaddxProperty("zone_14_faulted", 9, 1, 4, 1, CaddxPropertyType.BIT, "Zone 14 faulted (or delayed trip)",
                    false),
            new CaddxProperty("zone_14_bypassed", 9, 1, 5, 1, CaddxPropertyType.BIT, "Zone 14 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_14_trouble", 9, 1, 6, 1, CaddxPropertyType.BIT,
                    "Zone 14 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_14_alarm_memory", 9, 1, 7, 1, CaddxPropertyType.BIT, "Zone 14 alarm memory", false),

            // Byte 10 Zone 15 & 16 status flags (see byte 3)
            new CaddxProperty("zone_15_faulted", 10, 1, 0, 1, CaddxPropertyType.BIT,
                    "Zone 15 faulted (or delayed trip)", false),
            new CaddxProperty("zone_15_bypassed", 10, 1, 1, 1, CaddxPropertyType.BIT, "Zone 15 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_15_trouble", 10, 1, 2, 1, CaddxPropertyType.BIT,
                    "Zone 15 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_15_alarm_memory", 10, 1, 3, 1, CaddxPropertyType.BIT, "Zone 15 alarm memory",
                    false),
            new CaddxProperty("zone_16_faulted", 10, 1, 4, 1, CaddxPropertyType.BIT,
                    "Zone 16 faulted (or delayed trip)", false),
            new CaddxProperty("zone_16_bypassed", 10, 1, 5, 1, CaddxPropertyType.BIT, "Zone 16 bypass (or inhibited)",
                    false),
            new CaddxProperty("zone_16_trouble", 10, 1, 6, 1, CaddxPropertyType.BIT,
                    "Zone 16 trouble (tamper, low battery, or lost)", false),
            new CaddxProperty("zone_16_alarm_memory", 10, 1, 7, 1, CaddxPropertyType.BIT, "Zone 16 alarm memory",
                    false)),

    PARTITION_STATUS_MESSAGE(0x06, null, 9, "Partition Status Message",
            "This message will contain all information relevant to a single partition in the system.",
            CaddxDirection.IN, CaddxSource.PARTITION,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            new CaddxProperty("partition_number", 2, 1, 0, 0, CaddxPropertyType.INT,
                    "Partition number (0= partition 1)", false),

            // Byte 3 Partition condition flags (1)
            new CaddxProperty("partition_bypass_code_required", 3, 1, 0, 1, CaddxPropertyType.BIT,
                    "Bypass code required", false),
            new CaddxProperty("partition_fire_trouble", 3, 1, 1, 1, CaddxPropertyType.BIT, "Fire trouble", false),
            new CaddxProperty("partition_fire", 3, 1, 2, 1, CaddxPropertyType.BIT, "Fire", false),
            new CaddxProperty("partition_pulsing_buzzer", 3, 1, 3, 1, CaddxPropertyType.BIT, "Pulsing Buzzer", false),
            new CaddxProperty("partition_tlm_fault_memory", 3, 1, 4, 1, CaddxPropertyType.BIT, "TLM fault memory",
                    false),
            new CaddxProperty("partition_armed", 3, 1, 6, 1, CaddxPropertyType.BIT, "Armed", false),
            new CaddxProperty("partition_instant", 3, 1, 7, 1, CaddxPropertyType.BIT, "Instant", false),

            // Byte 4 Partition condition flags (2)
            new CaddxProperty("partition_previous_alarm", 4, 1, 0, 1, CaddxPropertyType.BIT, "Previous Alarm", false),
            new CaddxProperty("partition_siren_on", 4, 1, 1, 1, CaddxPropertyType.BIT, "Siren on", false),
            new CaddxProperty("partition_steady_siren_on", 4, 1, 2, 1, CaddxPropertyType.BIT, "Steady siren on", false),
            new CaddxProperty("partition_alarm_memory", 4, 1, 3, 1, CaddxPropertyType.BIT, "Alarm memory", false),
            new CaddxProperty("partition_tamper", 4, 1, 4, 1, CaddxPropertyType.BIT, "Tamper", false),
            new CaddxProperty("partition_cancel_command_entered", 4, 1, 5, 1, CaddxPropertyType.BIT,
                    "Cancel command entered", false),
            new CaddxProperty("partition_code_entered", 4, 1, 6, 1, CaddxPropertyType.BIT, "Code entered", false),
            new CaddxProperty("partition_cancel_pending", 4, 1, 7, 1, CaddxPropertyType.BIT, "Cancel pending", false),

            // Byte 5 Partition condition flags (3)
            new CaddxProperty("partition_silent_exit_enabled", 5, 1, 1, 1, CaddxPropertyType.BIT, "Silent exit enabled",
                    false),
            new CaddxProperty("partition_entryguard", 5, 1, 2, 1, CaddxPropertyType.BIT, "Entryguard (stay mode)",
                    false),
            new CaddxProperty("partition_chime_mode_on", 5, 1, 3, 1, CaddxPropertyType.BIT, "Chime mode on", false),
            new CaddxProperty("partition_entry", 5, 1, 4, 1, CaddxPropertyType.BIT, "Entry", false),
            new CaddxProperty("partition_delay_expiration_warning", 5, 1, 5, 1, CaddxPropertyType.BIT,
                    "Delay expiration warning", false),
            new CaddxProperty("partition_exit1", 5, 1, 6, 1, CaddxPropertyType.BIT, "Exit1", false),
            new CaddxProperty("partition_exit2", 5, 1, 7, 1, CaddxPropertyType.BIT, "Exit2", false),

            // Byte 6 Partition condition flags (4)
            new CaddxProperty("partition_led_extinguish", 6, 1, 0, 1, CaddxPropertyType.BIT, "LED extinguish", false),
            new CaddxProperty("partition_cross_timing", 6, 1, 1, 1, CaddxPropertyType.BIT, "Cross timing", false),
            new CaddxProperty("partition_recent_closing_being_timed", 6, 1, 2, 1, CaddxPropertyType.BIT,
                    "Recent closing being timed", false),
            new CaddxProperty("partition_exit_error_triggered", 6, 1, 4, 1, CaddxPropertyType.BIT,
                    "Exit error triggered", false),
            new CaddxProperty("partition_auto_home_inhibited", 6, 1, 5, 1, CaddxPropertyType.BIT, "Auto home inhibited",
                    false),
            new CaddxProperty("partition_sensor_low_battery", 6, 1, 6, 1, CaddxPropertyType.BIT, "Sensor low battery",
                    false),
            new CaddxProperty("partition_sensor_lost_supervision", 6, 1, 7, 1, CaddxPropertyType.BIT,
                    "Sensor lost supervision", false),

            new CaddxProperty("", 7, 1, 0, 0, CaddxPropertyType.INT, "Last user number", false),

            // Byte 8 Partition condition flags (5)
            new CaddxProperty("partition_zone_bypassed", 8, 1, 0, 1, CaddxPropertyType.BIT, "Zone bypassed", false),
            new CaddxProperty("partition_force_arm_triggered_by_auto_arm", 8, 1, 1, 1, CaddxPropertyType.BIT,
                    "Force arm triggered by auto arm", false),
            new CaddxProperty("partition_ready_to_arm", 8, 1, 2, 1, CaddxPropertyType.BIT, "Ready to arm", false),
            new CaddxProperty("partition_ready_to_force_arm", 8, 1, 3, 1, CaddxPropertyType.BIT, "Ready to force arm",
                    false),
            new CaddxProperty("partition_valid_pin_accepted", 8, 1, 4, 1, CaddxPropertyType.BIT, "Valid PIN accepted",
                    false),
            new CaddxProperty("partition_chime_on", 8, 1, 5, 1, CaddxPropertyType.BIT, "Chime on (sounding)", false),
            new CaddxProperty("partition_error_beep", 8, 1, 6, 1, CaddxPropertyType.BIT, "Error beep (triple beep)",
                    false),
            new CaddxProperty("partition_tone_on", 8, 1, 7, 1, CaddxPropertyType.BIT, "Tone on (activation tone)",
                    false),

            // Byte 9 Partition condition flags (6)
            new CaddxProperty("partition_entry1", 9, 1, 0, 1, CaddxPropertyType.BIT, "Entry 1", false),
            new CaddxProperty("partition_open_period", 9, 1, 1, 1, CaddxPropertyType.BIT, "Open period", false),
            new CaddxProperty("partition_alarm_sent_using_phone_number_1", 9, 1, 2, 1, CaddxPropertyType.BIT,
                    "Alarm sent using phone number 1", false),
            new CaddxProperty("partition_alarm_sent_using_phone_number_2", 9, 1, 3, 1, CaddxPropertyType.BIT,
                    "Alarm sent using phone number 2", false),
            new CaddxProperty("partition_alarm_sent_using_phone_number_3", 9, 1, 4, 1, CaddxPropertyType.BIT,
                    "Alarm sent using phone number 3", false),
            new CaddxProperty("partition_cancel_report_is_in_the_stack", 9, 1, 5, 1, CaddxPropertyType.BIT,
                    "Cancel report is in the stack", false),
            new CaddxProperty("partition_keyswitch_armed", 9, 1, 6, 1, CaddxPropertyType.BIT, "Keyswitch armed", false),
            new CaddxProperty("partition_delay_trip_in_progress", 9, 1, 7, 1, CaddxPropertyType.BIT,
                    "Delay Trip in progress (common zone)", false)),

    PARTITIONS_SNAPSHOT_MESSAGE(0x07, null, 9, "Partitions Snapshot Message",
            "This message will contain an abbreviated set of information for all 8 partitions on the system.",
            CaddxDirection.IN, CaddxSource.PANEL,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2 Partition 1 condition flags
            new CaddxProperty("partition_1_valid", 2, 1, 0, 1, CaddxPropertyType.BIT, "Partition 1 valid partition",
                    false),
            new CaddxProperty("", 2, 1, 1, 1, CaddxPropertyType.BIT, "Partition 1 ready", false),
            new CaddxProperty("", 2, 1, 2, 1, CaddxPropertyType.BIT, "Partition 1 armed", false),
            new CaddxProperty("", 2, 1, 3, 1, CaddxPropertyType.BIT, "Partition 1 stay mode", false),
            new CaddxProperty("", 2, 1, 4, 1, CaddxPropertyType.BIT, "Partition 1 chime mode", false),
            new CaddxProperty("", 2, 1, 5, 1, CaddxPropertyType.BIT, "Partition 1 any entry delay", false),
            new CaddxProperty("", 2, 1, 6, 1, CaddxPropertyType.BIT, "Partition 1 any exit delay", false),
            new CaddxProperty("", 2, 1, 7, 1, CaddxPropertyType.BIT, "Partition 1 previous alarm", false),

            // Byte 3 Partition 2 condition flags
            new CaddxProperty("partition_2_valid", 3, 1, 0, 1, CaddxPropertyType.BIT, "Partition 2 valid partition",
                    false),
            new CaddxProperty("", 3, 1, 1, 1, CaddxPropertyType.BIT, "Partition 2 ready", false),
            new CaddxProperty("", 3, 1, 2, 1, CaddxPropertyType.BIT, "Partition 2 armed", false),
            new CaddxProperty("", 3, 1, 3, 1, CaddxPropertyType.BIT, "Partition 2 stay mode", false),
            new CaddxProperty("", 3, 1, 4, 1, CaddxPropertyType.BIT, "Partition 2 chime mode", false),
            new CaddxProperty("", 3, 1, 5, 1, CaddxPropertyType.BIT, "Partition 2 any entry delay", false),
            new CaddxProperty("", 3, 1, 6, 1, CaddxPropertyType.BIT, "Partition 2 any exit delay", false),
            new CaddxProperty("", 3, 1, 7, 1, CaddxPropertyType.BIT, "Partition 2 previous alarm", false),

            // Byte 4 Partition 3 condition flags
            new CaddxProperty("partition_3_valid", 4, 1, 0, 1, CaddxPropertyType.BIT, "Partition 3 valid partition",
                    false),
            new CaddxProperty("", 4, 1, 1, 1, CaddxPropertyType.BIT, "Partition 3 ready", false),
            new CaddxProperty("", 4, 1, 2, 1, CaddxPropertyType.BIT, "Partition 3 armed", false),
            new CaddxProperty("", 4, 1, 3, 1, CaddxPropertyType.BIT, "Partition 3 stay mode", false),
            new CaddxProperty("", 4, 1, 4, 1, CaddxPropertyType.BIT, "Partition 3 chime mode", false),
            new CaddxProperty("", 4, 1, 5, 1, CaddxPropertyType.BIT, "Partition 3 any entry delay", false),
            new CaddxProperty("", 4, 1, 6, 1, CaddxPropertyType.BIT, "Partition 3 any exit delay", false),
            new CaddxProperty("", 4, 1, 7, 1, CaddxPropertyType.BIT, "Partition 3 previous alarm", false),

            // Byte 5 Partition 4 condition flags
            new CaddxProperty("partition_4_valid", 5, 1, 0, 1, CaddxPropertyType.BIT, "Partition 4 valid partition",
                    false),
            new CaddxProperty("", 5, 1, 1, 1, CaddxPropertyType.BIT, "Partition 4 ready", false),
            new CaddxProperty("", 5, 1, 2, 1, CaddxPropertyType.BIT, "Partition 4 armed", false),
            new CaddxProperty("", 5, 1, 3, 1, CaddxPropertyType.BIT, "Partition 4 stay mode", false),
            new CaddxProperty("", 5, 1, 4, 1, CaddxPropertyType.BIT, "Partition 4 chime mode", false),
            new CaddxProperty("", 5, 1, 5, 1, CaddxPropertyType.BIT, "Partition 4 any entry delay", false),
            new CaddxProperty("", 5, 1, 6, 1, CaddxPropertyType.BIT, "Partition 4 any exit delay", false),
            new CaddxProperty("", 5, 1, 7, 1, CaddxPropertyType.BIT, "Partition 4 previous alarm", false),

            // Byte 6 Partition 5 condition flags
            new CaddxProperty("partition_5_valid", 6, 1, 0, 1, CaddxPropertyType.BIT, "Partition 5 valid partition",
                    false),
            new CaddxProperty("", 6, 1, 1, 1, CaddxPropertyType.BIT, "Partition 5 ready", false),
            new CaddxProperty("", 6, 1, 2, 1, CaddxPropertyType.BIT, "Partition 5 armed", false),
            new CaddxProperty("", 6, 1, 3, 1, CaddxPropertyType.BIT, "Partition 5 stay mode", false),
            new CaddxProperty("", 6, 1, 4, 1, CaddxPropertyType.BIT, "Partition 5 chime mode", false),
            new CaddxProperty("", 6, 1, 5, 1, CaddxPropertyType.BIT, "Partition 5 any entry delay", false),
            new CaddxProperty("", 6, 1, 6, 1, CaddxPropertyType.BIT, "Partition 5 any exit delay", false),
            new CaddxProperty("", 6, 1, 7, 1, CaddxPropertyType.BIT, "Partition 5 previous alarm", false),

            // Byte 7 Partition 6 condition flags
            new CaddxProperty("partition_6_valid", 7, 1, 0, 1, CaddxPropertyType.BIT, "Partition 6 valid partition",
                    false),
            new CaddxProperty("", 7, 1, 1, 1, CaddxPropertyType.BIT, "Partition 6 ready", false),
            new CaddxProperty("", 7, 1, 2, 1, CaddxPropertyType.BIT, "Partition 6 armed", false),
            new CaddxProperty("", 7, 1, 3, 1, CaddxPropertyType.BIT, "Partition 6 stay mode", false),
            new CaddxProperty("", 7, 1, 4, 1, CaddxPropertyType.BIT, "Partition 6 chime mode", false),
            new CaddxProperty("", 7, 1, 5, 1, CaddxPropertyType.BIT, "Partition 6 any entry delay", false),
            new CaddxProperty("", 7, 1, 6, 1, CaddxPropertyType.BIT, "Partition 6 any exit delay", false),
            new CaddxProperty("", 7, 1, 7, 1, CaddxPropertyType.BIT, "Partition 6 previous alarm", false),

            // Byte 8 Partition 7 condition flags
            new CaddxProperty("partition_7_valid", 8, 1, 0, 1, CaddxPropertyType.BIT, "Partition 7 valid partition",
                    false),
            new CaddxProperty("", 8, 1, 1, 1, CaddxPropertyType.BIT, "Partition 7 ready", false),
            new CaddxProperty("", 8, 1, 2, 1, CaddxPropertyType.BIT, "Partition 7 armed", false),
            new CaddxProperty("", 8, 1, 3, 1, CaddxPropertyType.BIT, "Partition 7 stay mode", false),
            new CaddxProperty("", 8, 1, 4, 1, CaddxPropertyType.BIT, "Partition 7 chime mode", false),
            new CaddxProperty("", 8, 1, 5, 1, CaddxPropertyType.BIT, "Partition 7 any entry delay", false),
            new CaddxProperty("", 8, 1, 6, 1, CaddxPropertyType.BIT, "Partition 7 any exit delay", false),
            new CaddxProperty("", 8, 1, 7, 1, CaddxPropertyType.BIT, "Partition 7 previous alarm", false),

            // Byte 9 Partition 8 condition flags
            new CaddxProperty("partition_8_valid", 9, 1, 0, 1, CaddxPropertyType.BIT, "Partition 8 valid partition",
                    false),
            new CaddxProperty("", 9, 1, 1, 1, CaddxPropertyType.BIT, "Partition 8 ready", false),
            new CaddxProperty("", 9, 1, 2, 1, CaddxPropertyType.BIT, "Partition 8 armed", false),
            new CaddxProperty("", 9, 1, 3, 1, CaddxPropertyType.BIT, "Partition 8 stay mode", false),
            new CaddxProperty("", 9, 1, 4, 1, CaddxPropertyType.BIT, "Partition 8 chime mode", false),
            new CaddxProperty("", 9, 1, 5, 1, CaddxPropertyType.BIT, "Partition 8 any entry delay", false),
            new CaddxProperty("", 9, 1, 6, 1, CaddxPropertyType.BIT, "Partition 8 any exit delay", false),
            new CaddxProperty("", 9, 1, 8, 1, CaddxPropertyType.BIT, "Partition 8 previous alarm", false)),

    SYSTEM_STATUS_MESSAGE(0x08, null, 12, "System Status Message",
            "This message will contain all information relevant to the entire system.", CaddxDirection.IN,
            CaddxSource.PANEL,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Panel ID number", false),

            // Byte 3
            new CaddxProperty("", 3, 1, 0, 1, CaddxPropertyType.BIT, "Line seizure", false),
            new CaddxProperty("", 3, 1, 1, 1, CaddxPropertyType.BIT, "Off hook", false),
            new CaddxProperty("", 3, 1, 2, 1, CaddxPropertyType.BIT, "Initial handshake received", false),
            new CaddxProperty("", 3, 1, 3, 1, CaddxPropertyType.BIT, "Download in progress", false),
            new CaddxProperty("", 3, 1, 4, 1, CaddxPropertyType.BIT, "Dialer delay in progress", false),
            new CaddxProperty("", 3, 1, 5, 1, CaddxPropertyType.BIT, "Using backup phone", false),
            new CaddxProperty("", 3, 1, 6, 1, CaddxPropertyType.BIT, "Listen in active", false),
            new CaddxProperty("", 3, 1, 7, 1, CaddxPropertyType.BIT, "Two way lockout", false),

            // Byte 4
            new CaddxProperty("", 4, 1, 0, 1, CaddxPropertyType.BIT, "Ground fault", false),
            new CaddxProperty("", 4, 1, 1, 1, CaddxPropertyType.BIT, "Phone fault", false),
            new CaddxProperty("", 4, 1, 2, 1, CaddxPropertyType.BIT, "Fail to communicate", false),
            new CaddxProperty("", 4, 1, 3, 1, CaddxPropertyType.BIT, "Fuse fault", false),
            new CaddxProperty("", 4, 1, 4, 1, CaddxPropertyType.BIT, "Box tamper", false),
            new CaddxProperty("", 4, 1, 5, 1, CaddxPropertyType.BIT, "Siren tamper / trouble", false),
            new CaddxProperty("", 4, 1, 6, 1, CaddxPropertyType.BIT, "Low Battery", false),
            new CaddxProperty("panel_ac_fail", 4, 1, 7, 1, CaddxPropertyType.BIT, "AC fail", false),

            // Byte 5
            new CaddxProperty("", 5, 1, 0, 1, CaddxPropertyType.BIT, "Expander box tamper", false),
            new CaddxProperty("", 5, 1, 1, 1, CaddxPropertyType.BIT, "Expander AC failure", false),
            new CaddxProperty("", 5, 1, 2, 1, CaddxPropertyType.BIT, "Expander low battery", false),
            new CaddxProperty("", 5, 1, 3, 1, CaddxPropertyType.BIT, "Expander loss of supervision", false),
            new CaddxProperty("", 5, 1, 4, 1, CaddxPropertyType.BIT, "Expander auxiliary output over current", false),
            new CaddxProperty("", 5, 1, 5, 1, CaddxPropertyType.BIT, "Auxiliary communication channel failure", false),
            new CaddxProperty("", 5, 1, 6, 1, CaddxPropertyType.BIT, "Expander bell fault", false),

            // Byte 6
            new CaddxProperty("", 6, 1, 0, 1, CaddxPropertyType.BIT, "6 digit PIN enabled", false),
            new CaddxProperty("", 6, 1, 1, 1, CaddxPropertyType.BIT, "Programming token in use", false),
            new CaddxProperty("", 6, 1, 2, 1, CaddxPropertyType.BIT, "PIN required for local download", false),
            new CaddxProperty("", 6, 1, 3, 1, CaddxPropertyType.BIT, "Global pulsing buzzer", false),
            new CaddxProperty("", 6, 1, 4, 1, CaddxPropertyType.BIT, "Global Siren on", false),
            new CaddxProperty("", 6, 1, 5, 1, CaddxPropertyType.BIT, "Global steady siren", false),
            new CaddxProperty("", 6, 1, 6, 1, CaddxPropertyType.BIT, "Bus device has line seized", false),
            new CaddxProperty("", 6, 1, 7, 1, CaddxPropertyType.BIT, "Bus device has requested sniff mode", false),

            // Byte 7
            new CaddxProperty("", 7, 1, 0, 1, CaddxPropertyType.BIT, "Dynamic battery test", false),
            new CaddxProperty("panel_ac_power_on", 7, 1, 1, 1, CaddxPropertyType.BIT, "AC power on", false),
            new CaddxProperty("panel_low_battery_memory", 7, 1, 2, 1, CaddxPropertyType.BIT, "Low battery memory",
                    false),
            new CaddxProperty("", 7, 1, 3, 1, CaddxPropertyType.BIT, "Ground fault memory", false),
            new CaddxProperty("", 7, 1, 4, 1, CaddxPropertyType.BIT, "Fire alarm verification being timed", false),
            new CaddxProperty("", 7, 1, 5, 1, CaddxPropertyType.BIT, "Smoke power reset", false),
            new CaddxProperty("", 7, 1, 6, 1, CaddxPropertyType.BIT, "50 Hz line power detected", false),
            new CaddxProperty("", 7, 1, 7, 1, CaddxPropertyType.BIT, "Timing a high voltage battery charge", false),

            // Byte 8
            new CaddxProperty("", 8, 1, 0, 1, CaddxPropertyType.BIT, "Communication since last autotest", false),
            new CaddxProperty("", 8, 1, 1, 1, CaddxPropertyType.BIT, "Power up delay in progress", false),
            new CaddxProperty("", 8, 1, 2, 1, CaddxPropertyType.BIT, "Walk test mode", false),
            new CaddxProperty("", 8, 1, 3, 1, CaddxPropertyType.BIT, "Loss of system time", false),
            new CaddxProperty("", 8, 1, 4, 1, CaddxPropertyType.BIT, "Enroll requested", false),
            new CaddxProperty("", 8, 1, 5, 1, CaddxPropertyType.BIT, "Test fixture mode", false),
            new CaddxProperty("", 8, 1, 6, 1, CaddxPropertyType.BIT, "Control shutdown mode", false),
            new CaddxProperty("", 8, 1, 7, 1, CaddxPropertyType.BIT, "Timing a cancel window", false),

            // Byte 9
            new CaddxProperty("", 9, 1, 7, 1, CaddxPropertyType.BIT, "Call back in progress", false),

            // Byte 10
            new CaddxProperty("", 10, 1, 0, 1, CaddxPropertyType.BIT, "Phone line faulted", false),
            new CaddxProperty("", 10, 1, 1, 1, CaddxPropertyType.BIT, "Voltage present interrupt active", false),
            new CaddxProperty("", 10, 1, 2, 1, CaddxPropertyType.BIT, "House phone off hook", false),
            new CaddxProperty("", 10, 1, 3, 1, CaddxPropertyType.BIT, "Phone line monitor enabled", false),
            new CaddxProperty("", 10, 1, 4, 1, CaddxPropertyType.BIT, "Sniffing", false),
            new CaddxProperty("", 10, 1, 5, 1, CaddxPropertyType.BIT, "Last read was off hook", false),
            new CaddxProperty("", 10, 1, 6, 1, CaddxPropertyType.BIT, "Listen in requested", false),
            new CaddxProperty("", 10, 1, 7, 1, CaddxPropertyType.BIT, "Listen in trigger", false),

            // Byte 11
            new CaddxProperty("", 11, 1, 0, 1, CaddxPropertyType.BIT, "Valid partition 1", false),
            new CaddxProperty("", 11, 1, 1, 1, CaddxPropertyType.BIT, "Valid partition 2", false),
            new CaddxProperty("", 11, 1, 2, 1, CaddxPropertyType.BIT, "Valid partition 3", false),
            new CaddxProperty("", 11, 1, 3, 1, CaddxPropertyType.BIT, "Valid partition 4", false),
            new CaddxProperty("", 11, 1, 4, 1, CaddxPropertyType.BIT, "Valid partition 5", false),
            new CaddxProperty("", 11, 1, 5, 1, CaddxPropertyType.BIT, "Valid partition 6", false),
            new CaddxProperty("", 11, 1, 6, 1, CaddxPropertyType.BIT, "Valid partition 7", false),
            new CaddxProperty("", 11, 1, 7, 1, CaddxPropertyType.BIT, "Valid partition 8", false),

            // Byte 12 Communicator stack pointer
            new CaddxProperty("panel_communicator_stack_pointer", 12, 1, 0, 0, CaddxPropertyType.INT,
                    "Communicator stack pointer", false)),

    X10_MESSAGE_RECEIVED(0x09, null, 4, "X-10 Message Received",
            "This message contains information about an X-10 command that was requested by any device on the system bus.",
            CaddxDirection.IN, CaddxSource.PANEL,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "House code (0=house A)", false),

            // Byte 3
            new CaddxProperty("", 3, 1, 0, 0, CaddxPropertyType.INT, "Unit code (0=unit 1)", false),

            // Byte 4
            new CaddxProperty("", 4, 1, 0, 0, CaddxPropertyType.INT, "X-10 function code", false)),

    LOG_EVENT_MESSAGE(0x0a, null, 10, "Log Event Message",
            "This message will contain all information relating to an event in the log memory.", CaddxDirection.IN,
            CaddxSource.PANEL,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            // Byte 2
            new CaddxProperty("panel_log_event_number", 2, 1, 0, 0, CaddxPropertyType.INT,
                    "Event number of this message", false),
            // Byte 3
            new CaddxProperty("panel_log_event_size", 3, 1, 0, 0, CaddxPropertyType.INT,
                    "Total log size (number of log entries allowed)", false),

            // Byte 4
            new CaddxProperty("panel_log_event_type", 4, 1, 0, 7, CaddxPropertyType.INT, "Event type", false),
            // Bits 0-6 See type definitions in table that follows
            // Bit 7 Non-reporting event if not set

            // Byte 5
            new CaddxProperty("panel_log_event_zud", 5, 1, 0, 0, CaddxPropertyType.INT, "Zone / User / Device number",
                    false),
            // Byte 6
            new CaddxProperty("panel_log_event_partition", 6, 1, 0, 0, CaddxPropertyType.INT,
                    "Partition number (0=partition 1, if relevant)", false),
            // Byte 7
            new CaddxProperty("panel_log_event_month", 7, 1, 0, 0, CaddxPropertyType.INT, "Month (1-12)", false),
            // Byte 8
            new CaddxProperty("panel_log_event_day", 8, 1, 0, 0, CaddxPropertyType.INT, "Day (1-31)", false),
            // Byte 9
            new CaddxProperty("panel_log_event_hour", 9, 1, 0, 0, CaddxPropertyType.INT, "Hour (0-23)", false),
            // Byte 10
            new CaddxProperty("panel_log_event_minute", 10, 1, 0, 0, CaddxPropertyType.INT, "Minute (0-59)", false)),

    KEYPAD_MESSAGE_RECEIVED(0x0b, null, 3, "Keypad Message Received",
            "This message contains a keystroke from a keypad that is in a Terminal Mode.", CaddxDirection.IN,
            CaddxSource.KEYPAD,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("", 1, 2, 0, 0, CaddxPropertyType.INT, "Keypad address", false),

            // Byte 3
            new CaddxProperty("keypad_key_pressed", 1, 1, 0, 0, CaddxPropertyType.INT, "Key value", false)),

    PROGRAM_DATA_REPLY(0x10, null, 13, "Program Data Reply",
            "This message will contain a system device’s buss address, logical location, and program data that was previously requested (via Program Data Request (3Ch)).",
            CaddxDirection.IN, CaddxSource.PANEL,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Device’s buss address", false),

            // Byte 3 Upper logical location / offset
            new CaddxProperty("", 3, 1, 0, 3, CaddxPropertyType.INT, "Bits 8-11 of logical location", false),
            new CaddxProperty("", 3, 1, 4, 4, CaddxPropertyType.INT, "Segment size (0=byte, 1=nibble)", false),
            new CaddxProperty("", 3, 1, 5, 1, CaddxPropertyType.BIT, "Must be 0", false),
            new CaddxProperty("", 3, 1, 6, 6, CaddxPropertyType.INT, "Segment offset (0-none, 1=8 bytes)", false),
            new CaddxProperty("", 3, 1, 7, 1, CaddxPropertyType.BIT, "Must be 0", false),

            // Byte 4 Bits 0-7 of logical location
            new CaddxProperty("", 4, 1, 0, 0, CaddxPropertyType.INT, "Bits 0-7 of logical location", false),

            // Byte 5 Location length / data type
            new CaddxProperty("", 5, 1, 0, 4, CaddxPropertyType.INT, "Number of segments in location (0=1 segment)",
                    false),
            new CaddxProperty("", 5, 1, 5, 7, CaddxPropertyType.INT,
                    "Data type : 0=Binary 1=Decimal 2=Hexadecimal 3=ASCII 4=unused 5=unused 6=unused 7=unused", false),

            // Byte 6 Data byte
            new CaddxProperty("", 6, 1, 0, 0, CaddxPropertyType.INT, "Data byte 0", false),
            // Byte 7 Data byte
            new CaddxProperty("", 7, 1, 0, 0, CaddxPropertyType.INT, "Data byte 1", false),
            // Byte 8 Data byte
            new CaddxProperty("", 8, 1, 0, 0, CaddxPropertyType.INT, "Data byte 2", false),
            // Byte 9 Data byte
            new CaddxProperty("", 9, 1, 0, 0, CaddxPropertyType.INT, "Data byte 3", false),
            // Byte 10 Data byte
            new CaddxProperty("", 10, 1, 0, 0, CaddxPropertyType.INT, "Data byte 4", false),
            // Byte 11 Data byte
            new CaddxProperty("", 11, 1, 0, 0, CaddxPropertyType.INT, "Data byte 5", false),
            // Byte 12 Data byte
            new CaddxProperty("", 12, 1, 0, 0, CaddxPropertyType.INT, "Data byte 6", false),
            // Byte 13 Data byte
            new CaddxProperty("", 13, 1, 0, 0, CaddxPropertyType.INT, "Data byte 7", false)),

    USER_INFORMATION_REPLY(0x12, null, 7, "User Information Reply",
            "This message will contain all digits, attributes and partitions for the requested user PIN number that was previously requested (via User Information Request with(out) PIN (32h,33h)).",
            CaddxDirection.IN, CaddxSource.PANEL,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "User Number (1=user 1)", false),

            // Byte 3 PIN digits 1 & 2
            new CaddxProperty("", 3, 1, 0, 3, CaddxPropertyType.INT, "PIN digit 1", false),
            new CaddxProperty("", 3, 1, 4, 7, CaddxPropertyType.INT, "PIN digit 2", false),

            // Byte 4 PIN digits 3 & 4
            new CaddxProperty("", 4, 1, 0, 3, CaddxPropertyType.INT, "PIN digit 3", false),
            new CaddxProperty("", 4, 1, 4, 7, CaddxPropertyType.INT, "PIN digit 4", false),

            // Byte 5 PIN digits 5 & 6
            new CaddxProperty("", 5, 1, 0, 3, CaddxPropertyType.INT, "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            new CaddxProperty("", 5, 1, 4, 7, CaddxPropertyType.INT, "PIN digit 6 (pad with 0 if 4 digit PIN)", false),

            // Byte 6* Authority flags
            new CaddxProperty("", 6, 1, 0, 1, CaddxPropertyType.BIT,
                    "Reserved (if bit 7 is clear) || Output 1 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 1, 1, CaddxPropertyType.BIT,
                    "Arm only (if bit 7 is clear) || Output 2 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 2, 1, CaddxPropertyType.BIT,
                    "Arm only (during close window) (if bit 7 is clear) || Output 3 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 3, 1, CaddxPropertyType.BIT,
                    "Master / program (if bit 7 is clear) || Output 4 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 4, 1, CaddxPropertyType.BIT,
                    "Arm / Disarm (if bit 7 is clear) || Arm / Disarm (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 5, 1, CaddxPropertyType.BIT,
                    "Bypass enable (if bit 7 is clear) || Bypass enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 6, 1, CaddxPropertyType.BIT,
                    "Open / close report enable (if bit 7 is clear) || Open / close report enable (if bit 7 is set)",
                    false),
            new CaddxProperty("", 6, 1, 7, 1, CaddxPropertyType.BIT,
                    "Must be a 0 (if bit 7 is clear) || Must be a 1 (if bit 7 is set)", false),

            // Byte 7 Authorized partition(s) mask
            new CaddxProperty("", 7, 1, 0, 1, CaddxPropertyType.BIT, "Authorized for partition 1", false),
            new CaddxProperty("", 7, 1, 1, 1, CaddxPropertyType.BIT, "Authorized for partition 2", false),
            new CaddxProperty("", 7, 1, 2, 1, CaddxPropertyType.BIT, "Authorized for partition 3", false),
            new CaddxProperty("", 7, 1, 3, 1, CaddxPropertyType.BIT, "Authorized for partition 4", false),
            new CaddxProperty("", 7, 1, 4, 1, CaddxPropertyType.BIT, "Authorized for partition 5", false),
            new CaddxProperty("", 7, 1, 5, 1, CaddxPropertyType.BIT, "Authorized for partition 6", false),
            new CaddxProperty("", 7, 1, 6, 1, CaddxPropertyType.BIT, "Authorized for partition 7", false),
            new CaddxProperty("", 7, 1, 7, 1, CaddxPropertyType.BIT, "Authorized for partition 8", false)),

    REQUEST_FAILED(0x1c, null, 1, "Command / Request Failed",
            "This message is sent in place of a ‘Positive Acknowledge’ message when a command or request was received properly, but the system was unable to carry out the task correctly. This would normally occur 2.5 seconds after receiving the initial command or request.",
            CaddxDirection.IN, CaddxSource.PANEL,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false)),

    POSITIVE_ACKNOWLEDGE(0x1d, null, 1, "Positive Acknowledge",
            "This message will acknowledge receipt of a message that had the ‘Acknowledge Required’ flag set in the command byte.",
            CaddxDirection.IN, CaddxSource.PANEL,
            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false)),

    NEGATIVE_ACKNOWLEDGE(0x1e, null, 1, "Negative Acknowledge",
            "This message is sent in place of a ‘Positive Acknowledge’ message when the message received was not properly formatted. It will also be sent if an additional message is received before a reply has been returned during the 2.5 second allowable reply period of a previous message. An ‘Implied Negative Acknowledge’ is assumed when no acknowledge is returned with 3 seconds.",
            CaddxDirection.IN, CaddxSource.PANEL,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false)),

    MESSAGE_REJECTED(0x1f, null, 1, "Message Rejected",
            "This message is sent in place of a ‘Positive Acknowledge’ message when the message was received properly formatted, but not supported or disabled.",
            CaddxDirection.IN, CaddxSource.PANEL,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false)),

    INTERFACE_CONFIGURATION_REQUEST(0x21, new int[] { 0x01, 0x1c, 0x1f }, 1, "Interface Configuration Request",
            "This request will cause the return of the Interface Configuration Message (01h) containing information about the options selected on the interface.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false)),

    ZONE_NAME_REQUEST(0x23, new int[] { 0x03, 0x1c, 0x1f }, 2, "Zone Name Request",
            "This request will cause the return of the Zone Name Message (03h) for the zone number that was requested.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Zone number (0= zone 1)", true)),

    ZONE_STATUS_REQUEST(0x24, new int[] { 0x04, 0x1c, 0x1f }, 2, "Zone Status Request",
            "This request will cause the return of the Zone Status Message (04h) for the zone number that was requested.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("zone_number", 2, 1, 0, 0, CaddxPropertyType.INT, "Zone number (0= zone 1)", true)),

    ZONES_SNAPSHOT_REQUEST(0x25, new int[] { 0x05, 0x1c, 0x1f }, 2, "Zones Snapshot Request",
            "This request will cause the return of the Zones Snapshot Message (05h) with the group of zones starting at the zone 1 plus the offset value.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Zone number offset (0= start at zone 1)", true)),

    PARTITION_STATUS_REQUEST(0x26, new int[] { 0x06, 0x1c, 0x1f }, 2, "Partition Status Request",
            "This request will cause the return of the Partition Status Message (06h) for the partition number that was requested.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("partition_number", 2, 1, 0, 0, CaddxPropertyType.INT,
                    "Partition number (0= partition 1)", true)),

    PARTITIONS_SNAPSHOT_REQUEST(0x27, new int[] { 0x07, 0x1c, 0x1f }, 1, "Partitions Snapshot Request",
            "This request will cause the return of the Partitions Snapshot Message (07h) containing all partitions.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false)),

    SYSTEM_STATUS_REQUEST(0x28, new int[] { 0x08, 0x1c, 0x1f }, 1, "System Status Request",
            "This request will cause the return of the System Status Message (08h).", CaddxDirection.OUT,
            CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false)),

    SEND_X_10_MESSAGE(0x29, new int[] { 0x1d, 0x1c, 0x1f }, 4, "Send X-10 Message",
            "This message will contain information about an X-10 command that should be resent on the system bus.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "House code (0=house A) ", true),

            // Byte 3
            new CaddxProperty("", 3, 1, 0, 0, CaddxPropertyType.INT, "Unit code (0=unit 1)", true),

            // Byte 4
            new CaddxProperty("", 4, 1, 0, 0, CaddxPropertyType.INT, "X-10 function code (see table at message # 0Ah)",
                    true)),

    LOG_EVENT_REQUEST(0x2a, new int[] { 0x0a, 0x1c, 0x1f }, 2, "Log Event Request",
            "This request will cause the return of the Log Event Message (0Ah).", CaddxDirection.OUT, CaddxSource.NONE,
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            new CaddxProperty("panel_log_event_number", 2, 1, 0, 0, CaddxPropertyType.INT, "Event number requested",
                    true)),

    SEND_KEYPAD_TEXT_MESSAGE(0x2b, new int[] { 0x1d, 0x1c, 0x1f }, 12, "Send Keypad Text Message",
            "This message will contain ASCII text for a specific keypad on the bus that will be displayed during Terminal Mode.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            // Byte 2 Keypad address
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Keypad address", false),
            // Byte 3 Keypad type (0=NX-148e)(all others not supported)
            new CaddxProperty("", 3, 1, 0, 0, CaddxPropertyType.INT, "Keypad type", false),
            // Byte 4 Display storage location (0=top left corner
            new CaddxProperty("", 4, 1, 0, 0, CaddxPropertyType.INT, "Display storage location", false),
            // Byte 5 ASCII character for location +0
            new CaddxProperty("", 5, 1, 0, 0, CaddxPropertyType.INT, "ASCII character for location +0", false),
            // Byte 6 ASCII character for location +1
            new CaddxProperty("", 6, 1, 0, 0, CaddxPropertyType.INT, "ASCII character for location +1", false),
            // Byte 7 ASCII character for location +2
            new CaddxProperty("", 7, 1, 0, 0, CaddxPropertyType.INT, "ASCII character for location +2", false),
            // Byte 8 ASCII character for location +3
            new CaddxProperty("", 8, 1, 0, 0, CaddxPropertyType.INT, "ASCII character for location +3", false),
            // Byte 9 ASCII character for location +4
            new CaddxProperty("", 9, 1, 0, 0, CaddxPropertyType.INT, "ASCII character for location +4", false),
            // Byte 10 ASCII character for location +5
            new CaddxProperty("", 10, 1, 0, 0, CaddxPropertyType.INT, "ASCII character for location +5", false),
            // Byte 11 ASCII character for location +6
            new CaddxProperty("", 11, 1, 0, 0, CaddxPropertyType.INT, "ASCII character for location +6", false),
            // Byte 12 ASCII character for location +7
            new CaddxProperty("", 12, 1, 0, 0, CaddxPropertyType.INT, "ASCII character for location +7", false)),

    KEYPAD_TERMINAL_MODE_REQUEST(0x2c, new int[] { 0x1d, 0x1c, 0x1f }, 3, "Keypad Terminal Mode Request",
            "This message will contain the address of a keypad that should enter a Terminal Mode for the time contained. Only one keypad should be in the Terminal Mode at a time.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            // Byte 2 Keypad address
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Keypad address", false),
            // Byte 3
            new CaddxProperty("", 3, 1, 0, 0, CaddxPropertyType.INT, "Number of seconds for Terminal Mode", false)),

    PROGRAM_DATA_REQUEST(0x30, new int[] { 0x10, 0x1c, 0x1f }, 4, "Program Data Request",
            "This message will contain a system device’s buss address and the logical location of program data that will be returned in a Program Data Reply message (10h).",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            // Byte 2 Device’s buss address
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Device’s buss address", false),
            // Byte 3 Upper logical location / offset
            // Bits 0-3 Bits 8-11 of logical location
            new CaddxProperty("", 3, 1, 0, 4, CaddxPropertyType.INT, "Bits 8-11 of logical location", false),
            // Bits 4,5 Must be 0
            new CaddxProperty("", 3, 1, 4, 2, CaddxPropertyType.BIT, "Must be 0", false),
            // Bit 6 Segment offset (0-none, 1=8 bytes)
            new CaddxProperty("", 3, 1, 6, 1, CaddxPropertyType.BIT, "Segment offset (0-none, 1=8 bytes)", false),
            // Bit 7
            new CaddxProperty("", 3, 1, 7, 1, CaddxPropertyType.BIT, "Must be 0", false),
            // Byte 4 Bits 0-7 of logical location
            new CaddxProperty("", 4, 1, 0, 0, CaddxPropertyType.INT, "Bits 0-7 of logical location", false)),

    PROGRAM_DATA_COMMAND(0x31, new int[] { 0x1d, 0x1c, 0x1f }, 13, "Program Data Command",
            "This message will contain a system device’s buss address and the logical location where the included data should be stored.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            // Byte 2 Device’s buss address
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Device’s buss address", false),
            // Byte 3 Upper logical location / offset
            // Bits 0-3 Bits 8-11 of logical location
            new CaddxProperty("", 3, 1, 0, 4, CaddxPropertyType.BIT, "Bits 8-11 of logical location", false),
            // Bit 4 Segment size (0=byte, 1=nibble)
            new CaddxProperty("", 3, 1, 4, 1, CaddxPropertyType.BIT, "Segment size (0=byte, 1=nibble)", false),
            // Bit 5 Must be 1
            new CaddxProperty("", 3, 1, 5, 1, CaddxPropertyType.BIT, "Must be 1", false),
            // Bit 6 Segment offset (0-none, 1=8 bytes)
            new CaddxProperty("", 3, 1, 6, 1, CaddxPropertyType.BIT, "Segment offset (0-none, 1=8 bytes)", false),
            // Bit 7 Must be 0
            new CaddxProperty("", 3, 1, 7, 1, CaddxPropertyType.BIT, "Must be 0", false),
            // Byte 4 Bits 0-7 of logical location
            new CaddxProperty("", 4, 1, 0, 0, CaddxPropertyType.INT, "Bits 0-7 of logical location", false),
            // Byte 5 Location length / data type
            // Bits 0-4 Number of segments in location (0=1 segment)
            new CaddxProperty("", 5, 1, 0, 5, CaddxPropertyType.BIT, "Number of segments in location (0=1 segment)",
                    false),
            // Bits 5-7 Data type : 5=unused
            new CaddxProperty("", 5, 1, 5, 3, CaddxPropertyType.BIT,
                    "Data type: 0=Binary, 1=Decimal, 2=Hexadecimal, 3=ASCII, 4=unused, 5=unused, 6=unused, 7=unused",
                    false),
            // Byte 6 Data byte 1 to store
            new CaddxProperty("", 6, 1, 0, 0, CaddxPropertyType.INT, "Data byte 1 to store", false),
            // Byte 7 Data byte 2 to store
            new CaddxProperty("", 7, 1, 0, 0, CaddxPropertyType.INT, "Data byte 2 to store", false),
            // Byte 8 Data byte 3 to store
            new CaddxProperty("", 8, 1, 0, 0, CaddxPropertyType.INT, "Data byte 3 to store", false),
            // Byte 9 Data byte 4 to store
            new CaddxProperty("", 9, 1, 0, 0, CaddxPropertyType.INT, "Data byte 4 to store", false),
            // Byte 10 Data byte 5 to store
            new CaddxProperty("", 10, 1, 0, 0, CaddxPropertyType.INT, "Data byte 5 to store", false),
            // Byte 11 Data byte 6 to store
            new CaddxProperty("", 11, 1, 0, 0, CaddxPropertyType.INT, "Data byte 6 to store", false),
            // Byte 12 Data byte 7 to store
            new CaddxProperty("", 12, 1, 0, 0, CaddxPropertyType.INT, "Data byte 7 to store", false),
            // Byte 13 Data byte 8 to store
            new CaddxProperty("", 13, 1, 0, 0, CaddxPropertyType.INT, "Data byte 8 to store", false)),

    USER_INFORMATION_REQUEST_WITH_PIN(0x32, new int[] { 0x12, 0x1c, 0x1f }, 5, "User Information Request with PIN",
            "This message will contain a user number for which information is being requested and a PIN that will be checked for Master capability before proceeding. The information will be returned in a User Information Reply message (12h).",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            // Byte 2 (Master) PIN digits 1 & 2
            // Bits 0-3 PIN digit 1
            new CaddxProperty("", 2, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 1", false),
            // Bits 4-7 PIN digit 2
            new CaddxProperty("", 2, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 2", false),
            // Byte 3 (Master) PIN digits 3 & 4
            // Bits 0-3 PIN digit 3
            new CaddxProperty("", 3, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 3", false),
            // Bits 4-7 PIN digit 4
            new CaddxProperty("", 3, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 4", false),
            // Byte 4 (Master) PIN digits 5 & 6
            // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 4, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 4, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 6 (pad with 0 if 4 digit PIN)", false),
            // Byte 5 User number (1=user 1)
            new CaddxProperty("", 5, 1, 0, 0, CaddxPropertyType.INT, "User number (1=user 1)", false)),

    USER_INFORMATION_REQUEST_WITHOUT_PIN(0x33, new int[] { 0x12, 0x1c, 0x1f }, 2,
            "User Information Request without PIN",
            "This message will contain a user number for which information is being requested, no authentication will be performed. The information will be returned in a User Information Reply message (12h).",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            // Byte 2 User number (1=user 1)
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "User number (1=user 1)", false)),

    SET_USER_CODE_COMMAND_WITH_PIN(0x34, new int[] { 0x12, 0x1c, 0x1f }, 8, "Set User Code Command with PIN",
            "This message will contain all digits that should be stored as the new code for the designated User number. A PIN will be checked for Master capability before proceeding. A successful programming of the user code will result in the User Information Reply (12h) returned in place of the acknowledge.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            // Byte 2 (Master) PIN digits 1 & 2
            // Bits 0-3 PIN digit 1
            new CaddxProperty("", 2, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 1", false),
            // Bits 4-7 PIN digit 2
            new CaddxProperty("", 2, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 2", false),
            // Byte 3 (Master) PIN digits 3 & 4
            // Bits 0-3 PIN digit 3
            new CaddxProperty("", 3, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 3", false),
            // Bits 4-7 PIN digit 4
            new CaddxProperty("", 3, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 4", false),
            // Byte 4 (Master) PIN digits 5 & 6
            // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 4, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 4, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 6 (pad with 0 if 4 digit PIN)", false),
            // Byte 5 User number (1=user 1)
            new CaddxProperty("", 5, 1, 0, 0, CaddxPropertyType.INT, "User number (1=user 1)", false),
            // Byte 6 PIN digits 1 & 2
            // Bits 0-3 PIN digit 1
            new CaddxProperty("", 6, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 1", false),
            // Bits 4-7 PIN digit 2
            new CaddxProperty("", 6, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 2", false),
            // Byte 7 PIN digits 3 & 4
            // Bits 0-3 PIN digit 3
            new CaddxProperty("", 7, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 3", false),
            // Bits 4-7 PIN digit 4
            new CaddxProperty("", 7, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 4", false),
            // Byte 8 PIN digits 5 & 6
            // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 8, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 8, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 6 (pad with 0 if 4 digit PIN)", false)),

    SET_USER_CODE_COMMAND_WITHOUT_PIN(0x35, new int[] { 0x12, 0x1c, 0x1f }, 5, "Set User Code Command without PIN",
            "This message will contain all digits that should be stored as the new code for the designated User number. No authentication will be performed. A successful programming of the user code will result in the User Information Reply (12h) returned in place of the acknowledge.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),
            // Byte 2 User number (1=user 1)
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "User number (1=user 1)", false),
            // Byte 3 PIN digits 1 & 2
            // Bits 0-3 PIN digit 1
            new CaddxProperty("", 3, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 1", false),
            // Bits 4-7 PIN digit 2
            new CaddxProperty("", 3, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 2", false),
            // Byte 4 PIN digits 3 & 4
            // Bits 0-3 PIN digit 3
            new CaddxProperty("", 4, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 3", false),
            // Bits 4-7 PIN digit 4
            new CaddxProperty("", 4, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 4", false),
            // Byte 5 PIN digits 5 & 6
            // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 5, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
            new CaddxProperty("", 5, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 6 (pad with 0 if 4 digit PIN)", false)),

    SET_USER_AUTHORIZATION_COMMAND_WITH_PIN(0x36, new int[] { 0x1d, 0x1c, 0x1f }, 7,
            "Set User Authorization Command with PIN",
            "This message will contain all attributes and partitions that should be stored as the new information for the designated User number. A PIN will be checked for Master capability before proceeding.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2 (Master) PIN digits 1 & 2
            new CaddxProperty("", 2, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 1", false),
            new CaddxProperty("", 2, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 2", false),

            // Byte 3 (Master) PIN digits 3 & 4
            new CaddxProperty("", 3, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 3", false),
            new CaddxProperty("", 3, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 4", false),

            // Byte 4 (Master) PIN digits 5 & 6
            new CaddxProperty("", 4, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            new CaddxProperty("", 4, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 6 (pad with 0 if 4 digit PIN)", false),

            // Byte 5 User number (1=user 1)
            new CaddxProperty("", 5, 1, 0, 0, CaddxPropertyType.INT, "User number (1=user 1)", false),

            // Byte 6 Authority flags
            new CaddxProperty("", 6, 1, 0, 1, CaddxPropertyType.BIT,
                    "Reserved (if bit 7 is clear) || Output 1 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 1, 1, CaddxPropertyType.BIT,
                    "Arm only (if bit 7 is clear) || Output 2 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 2, 1, CaddxPropertyType.BIT,
                    "Arm only (during close window) (if bit 7 is clear) || Output 3 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 3, 1, CaddxPropertyType.BIT,
                    "Master / program (if bit 7 is clear) || Output 4 enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 4, 1, CaddxPropertyType.BIT,
                    "Arm / Disarm (if bit 7 is clear) || Arm / Disarm (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 5, 1, CaddxPropertyType.BIT,
                    "Bypass enable (if bit 7 is clear) || Bypass enable (if bit 7 is set)", false),
            new CaddxProperty("", 6, 1, 6, 1, CaddxPropertyType.BIT,
                    "Open / close report enable (if bit 7 is clear) || Open / close report enable (if bit 7 is set)",
                    false),
            new CaddxProperty("", 6, 1, 7, 1, CaddxPropertyType.BIT,
                    "Must be a 0 (if bit 7 is clear) || Must be a 1 (if bit 7 is set)", false),

            // Byte 7 Authorized partition(s) mask
            new CaddxProperty("", 7, 1, 0, 1, CaddxPropertyType.BIT, "Authorized for partition 1", false),
            new CaddxProperty("", 7, 1, 1, 1, CaddxPropertyType.BIT, "Authorized for partition 2", false),
            new CaddxProperty("", 7, 1, 2, 1, CaddxPropertyType.BIT, "Authorized for partition 3", false),
            new CaddxProperty("", 7, 1, 3, 1, CaddxPropertyType.BIT, "Authorized for partition 4", false),
            new CaddxProperty("", 7, 1, 4, 1, CaddxPropertyType.BIT, "Authorized for partition 5", false),
            new CaddxProperty("", 7, 1, 5, 1, CaddxPropertyType.BIT, "Authorized for partition 6", false),
            new CaddxProperty("", 7, 1, 6, 1, CaddxPropertyType.BIT, "Authorized for partition 7", false),
            new CaddxProperty("", 7, 1, 7, 1, CaddxPropertyType.BIT, "Authorized for partition 8", false)),

    SET_USER_AUTHORIZATION_COMMAND_WITHOUT_PIN(0x37, new int[] { 0x1d, 0x1c, 0x1f }, 4,
            "Set User Authorization Command without PIN",
            "This message will contain all attributes and partitions that should be stored as the new information for the designated User number. No authentication will be performed.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2 User number (1=user 1)
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "User number (1=user 1)", false),

            // Byte 3 Authority flags
            new CaddxProperty("", 3, 1, 0, 1, CaddxPropertyType.BIT,
                    "Reserved (if bit 7 is clear) || Output 1 enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 1, 1, CaddxPropertyType.BIT,
                    "Arm only (if bit 7 is clear) || Output 2 enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 2, 1, CaddxPropertyType.BIT,
                    "Arm only (during close window) (if bit 7 is clear) || Output 3 enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 3, 1, CaddxPropertyType.BIT,
                    "Master / program (if bit 7 is clear) || Output 4 enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 4, 1, CaddxPropertyType.BIT,
                    "Arm / Disarm (if bit 7 is clear) || Arm / Disarm (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 5, 1, CaddxPropertyType.BIT,
                    "Bypass enable (if bit 7 is clear) || Bypass enable (if bit 7 is set)", false),
            new CaddxProperty("", 3, 1, 6, 1, CaddxPropertyType.BIT,
                    "Open / close report enable (if bit 7 is clear) || Open / close report enable (if bit 7 is set)",
                    false),
            new CaddxProperty("", 3, 1, 7, 1, CaddxPropertyType.BIT,
                    "Must be a 0 (if bit 7 is clear) || Must be a 1 (if bit 7 is set)", false),

            // Byte 4 Authorized partition(s) mask
            new CaddxProperty("", 4, 1, 0, 1, CaddxPropertyType.BIT, "Authorized for partition 1", false),
            new CaddxProperty("", 4, 1, 1, 1, CaddxPropertyType.BIT, "Authorized for partition 2", false),
            new CaddxProperty("", 4, 1, 2, 1, CaddxPropertyType.BIT, "Authorized for partition 3", false),
            new CaddxProperty("", 4, 1, 3, 1, CaddxPropertyType.BIT, "Authorized for partition 4", false),
            new CaddxProperty("", 4, 1, 4, 1, CaddxPropertyType.BIT, "Authorized for partition 5", false),
            new CaddxProperty("", 4, 1, 5, 1, CaddxPropertyType.BIT, "Authorized for partition 6", false),
            new CaddxProperty("", 4, 1, 6, 1, CaddxPropertyType.BIT, "Authorized for partition 7", false),
            new CaddxProperty("", 4, 1, 7, 1, CaddxPropertyType.BIT, "Authorized for partition 8", false)),

    STORE_COMMUNICATION_EVENT_COMMAND(0x3a, new int[] { 0x1d, 0x1c, 0x1f }, 6, "Store Communication Event Command",
            "This message will submit an event to the control’s communication stack for possible transmission over its telephone or alternate communications path.",
            CaddxDirection.OUT, CaddxSource.NONE),

    SET_CLOCK_CALENDAR_COMMAND(0x3b, new int[] { 0x1d, 0x1c, 0x1f }, 7, "Set Clock / Calendar Command",
            "This message will set the clock / calendar in the system.", CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2 Year (00-99)
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Year (00-99)", false),

            // Byte 3 Month (1-12)
            new CaddxProperty("", 3, 1, 0, 0, CaddxPropertyType.INT, "Month (1-12)", false),

            // Byte 4 Day (1-31)
            new CaddxProperty("", 4, 1, 0, 0, CaddxPropertyType.INT, "Day (1-31)", false),

            // Byte 5 Hour (0-23)
            new CaddxProperty("", 5, 1, 0, 0, CaddxPropertyType.INT, "Hour (0-23)", false),

            // Byte 6 Minute (0-59)
            new CaddxProperty("", 6, 1, 0, 0, CaddxPropertyType.INT, "Minute (0-59)", false),

            // Byte 7 Day
            new CaddxProperty("", 7, 1, 0, 0, CaddxPropertyType.INT, "Day", false)),

    PRIMARY_KEYPAD_FUNCTION_WITH_PIN(0x3c, new int[] { 0x1d, 0x1c, 0x1f }, 6, "Primary Keypad Function with PIN",
            "This message will contain a value that defines with function to perform, the partitions to use and a PIN value for the validation.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2 PIN digits 1 & 2
            new CaddxProperty("", 2, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 1", false),
            new CaddxProperty("", 2, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 2", false),

            // Byte 3 PIN digits 3 & 4
            new CaddxProperty("", 3, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 3", false),
            new CaddxProperty("", 3, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 4", false),

            // Byte 4 PIN digits 5 & 6
            new CaddxProperty("", 4, 1, 0, 4, CaddxPropertyType.BIT, "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
            new CaddxProperty("", 4, 1, 4, 4, CaddxPropertyType.BIT, "PIN digit 6 (pad with 0 if 4 digit PIN)", false),

            // Byte 5 Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm
            // in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode,
            // 08h-FFh Reserved]
            new CaddxProperty("", 5, 1, 0, 0, CaddxPropertyType.INT,
                    "Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode, 08h-FFh Reserved]",
                    false),

            // Byte 6 Partition mask
            new CaddxProperty("", 6, 1, 0, 1, CaddxPropertyType.BIT, "Perform on partition 1 (if PIN has access)",
                    false),
            new CaddxProperty("", 6, 1, 1, 1, CaddxPropertyType.BIT, "Perform on partition 2 (if PIN has access)",
                    false),
            new CaddxProperty("", 6, 1, 2, 1, CaddxPropertyType.BIT, "Perform on partition 3 (if PIN has access)",
                    false),
            new CaddxProperty("", 6, 1, 3, 1, CaddxPropertyType.BIT, "Perform on partition 4 (if PIN has access)",
                    false),
            new CaddxProperty("", 6, 1, 4, 1, CaddxPropertyType.BIT, "Perform on partition 5 (if PIN has access)",
                    false),
            new CaddxProperty("", 6, 1, 5, 1, CaddxPropertyType.BIT, "Perform on partition 6 (if PIN has access)",
                    false),
            new CaddxProperty("", 6, 1, 6, 1, CaddxPropertyType.BIT, "Perform on partition 7 (if PIN has access)",
                    false),
            new CaddxProperty("", 6, 1, 7, 1, CaddxPropertyType.BIT, "Perform on partition 8 (if PIN has access)",
                    false)),

    PRIMARY_KEYPAD_FUNCTION_WITHOUT_PIN(0x3d, new int[] { 0x1d, 0x1c, 0x1f }, 4, "Primary Keypad Function without PIN",
            "This message will contain a value that defines with function to perform, the partitions and user number to assign to the function.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2 "Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm
            // in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode,
            // 08h-FFh Reserved]",
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT,
                    "Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode, 08h-FFh Reserved]",
                    false),

            // Byte 3 Partition mask
            new CaddxProperty("", 3, 1, 0, 1, CaddxPropertyType.BIT, "Perform on partition 1 (if PIN has access)",
                    false),
            new CaddxProperty("", 3, 1, 1, 1, CaddxPropertyType.BIT, "Perform on partition 2 (if PIN has access)",
                    false),
            new CaddxProperty("", 3, 1, 2, 1, CaddxPropertyType.BIT, "Perform on partition 3 (if PIN has access)",
                    false),
            new CaddxProperty("", 3, 1, 3, 1, CaddxPropertyType.BIT, "Perform on partition 4 (if PIN has access)",
                    false),
            new CaddxProperty("", 3, 1, 4, 1, CaddxPropertyType.BIT, "Perform on partition 5 (if PIN has access)",
                    false),
            new CaddxProperty("", 3, 1, 5, 1, CaddxPropertyType.BIT, "Perform on partition 6 (if PIN has access)",
                    false),
            new CaddxProperty("", 3, 1, 6, 1, CaddxPropertyType.BIT, "Perform on partition 7 (if PIN has access)",
                    false),
            new CaddxProperty("", 3, 1, 7, 1, CaddxPropertyType.BIT, "Perform on partition 8 (if PIN has access)",
                    false),

            // Byte 4 User number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "User number", false)),

    SECONDARY_KEYPAD_FUNCTION(0x3e, new int[] { 0x1d, 0x1c, 0x1f }, 3, "Secondary Keypad Function",
            "This message will contain a value that defines with function to perform, and the partitions to use.",
            CaddxDirection.OUT, CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2 "Keypad function [00h Stay (1 button arm / toggle interiors), 01h Chime (toggle chime mode),
            // 02h Exit (1 button arm / toggle instant), 03h Bypass interiors, 04h Fire panic, 05h Medical panic,
            // 06h Police panic, 07h Smoke detector reset, 08h Auto callback download, 09h Manual pickup download,
            // 0Ah Enable silent exit (for this arm cycle), 0Bh Perform test, 0Ch Group bypass, 0Dh Auxiliary
            // function 1, 0Eh Auxiliary function 2, 0Fh Start keypad sounder, 10h-FFh Reserved]",
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT,
                    "Keypad function [00h Stay (1 button arm / toggle interiors), 01h Chime (toggle chime mode), 02h Exit (1 button arm / toggle instant), 03h Bypass interiors, 04h Fire panic, 05h Medical panic, 06h Police panic, 07h Smoke detector reset, 08h Auto callback download, 09h Manual pickup download, 0Ah Enable silent exit (for this arm cycle), 0Bh Perform test, 0Ch Group bypass, 0Dh Auxiliary function 1, 0Eh Auxiliary function 2, 0Fh Start keypad sounder, 10h-FFh Reserved]",
                    false),

            // Byte 3 Partition mask
            new CaddxProperty("", 3, 1, 0, 1, CaddxPropertyType.BIT, "Perform on partition 1", false),
            new CaddxProperty("", 3, 1, 1, 1, CaddxPropertyType.BIT, "Perform on partition 2", false),
            new CaddxProperty("", 3, 1, 2, 1, CaddxPropertyType.BIT, "Perform on partition 3", false),
            new CaddxProperty("", 3, 1, 3, 1, CaddxPropertyType.BIT, "Perform on partition 4", false),
            new CaddxProperty("", 3, 1, 4, 1, CaddxPropertyType.BIT, "Perform on partition 5", false),
            new CaddxProperty("", 3, 1, 5, 1, CaddxPropertyType.BIT, "Perform on partition 6", false),
            new CaddxProperty("", 3, 1, 6, 1, CaddxPropertyType.BIT, "Perform on partition 7", false),
            new CaddxProperty("", 3, 1, 7, 1, CaddxPropertyType.BIT, "Perform on partition 8", false)),

    ZONE_BYPASS_TOGGLE(0x3f, new int[] { 0x1d, 0x1c, 0x1f }, 2, "Zone Bypass Toggle",
            "This message will contain a number of a zone that should be (un)bypassed.", CaddxDirection.OUT,
            CaddxSource.NONE,

            // Properties
            // Byte 1 Message number
            new CaddxProperty("", 1, 1, 0, 0, CaddxPropertyType.INT, "Message number", false),

            // Byte 2 Zone number (0= zone 1)
            new CaddxProperty("", 2, 1, 0, 0, CaddxPropertyType.INT, "Zone number (0= zone 1)", false));

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

    public static @Nullable CaddxMessageType valueOfMessageType(int number) {
        return BY_MESSAGE_TYPE.get(number);
    }
}
