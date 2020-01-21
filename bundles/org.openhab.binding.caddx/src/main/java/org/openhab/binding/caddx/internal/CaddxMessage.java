/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class that represents the Caddx Alarm Messages.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxMessage {

    private byte[] message;
    private boolean hasAcknowledgementFlag = false;
    private byte checksum1In;
    private byte checksum2In;
    private byte checksum1Calc;
    private byte checksum2Calc;
    private CaddxMessageType caddxMessageType;

    public byte getChecksum1In() {
        return checksum1In;
    }

    public byte getChecksum2In() {
        return checksum2In;
    }

    public byte getChecksum1Calc() {
        return checksum1Calc;
    }

    public byte getChecksum2Calc() {
        return checksum2Calc;
    }

    public enum Direction {
        In,
        Out
    };

    public enum Source {
        None,
        Panel,
        Keypad,
        Partition,
        Zone
    };

    @SuppressWarnings("null")
    @NonNullByDefault
    public static class Property {
        // private
        String name;
        String type; // 'Int', 'String', 'Bit'
        int byteFrom;
        int byteLength;
        int bitFrom;
        int bitLength;
        boolean external;
        String id;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        // Constructor
        public Property(String id, int byteFrom, int byteLength, int bitFrom, int bitLength, String type, String name,
                boolean external) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.byteFrom = byteFrom;
            this.byteLength = byteLength;
            this.bitFrom = bitFrom;
            this.bitLength = bitLength;
            this.external = external;
        }

        public String getValue(byte[] message) {
            int mask;
            int val;

            if (type == "Int") {
                if (bitFrom == 0 && bitLength == 0) {
                    mask = 255;
                    val = message[byteFrom - 1] & mask;
                } else {
                    mask = ((1 << ((bitLength - bitFrom))) - 1) << bitFrom;
                    val = (message[byteFrom - 1] & mask) >> bitFrom;
                }

                return Integer.toString(val);
            }

            if (type == "String") {
                byte[] str = Arrays.copyOfRange(message, byteFrom - 1, byteFrom + byteLength);
                return new String(str);
            }

            if (type == "Bit") {
                return (((message[byteFrom - 1] & (1 << bitFrom)) > 0) ? "true" : "false");
            }

            throw new IllegalArgumentException("type [" + type + "] is unknown.");
        }

        public String toString(byte[] message) {
            int mask;
            int val;

            if (type == "Int") {
                if (bitFrom == 0 && bitLength == 0) {
                    mask = 255;
                    val = message[byteFrom - 1];
                } else {
                    mask = ((1 << ((bitLength - bitFrom) + 1)) - 1) << bitFrom;
                    val = (message[byteFrom - 1] & mask) >> bitFrom;
                }

                return name + ": " + String.format("%2s", Integer.toHexString(val)) + " - " + Integer.toString(val)
                        + " - " + ((val >= 32 && val <= 'z') ? ((char) val) : "-");
            }

            if (type == "String") {
                StringBuilder sb = new StringBuilder();

                byte[] a = Arrays.copyOfRange(message, byteFrom - 1, byteFrom + byteLength);
                sb.append(name);
                sb.append(": ");
                sb.append(new String(a));
                sb.append("\r\n\r\n");
                for (int i = 0; i < byteLength; i++) {
                    sb.append(String.format("%2s", Integer.toHexString(message[byteFrom - 1 + i])));
                    sb.append(" - ");
                    sb.append((char) message[byteFrom - 1 + i]);
                    sb.append("\r\n");
                }

                return sb.toString();
            }

            if (type == "Bit") {
                return name + ": " + (((message[byteFrom - 1] & (1 << bitFrom)) > 0) ? "true" : "false");
            }

            return "Unknown type: " + type;
        }

    }

    public enum CaddxMessageType {

        Interface_Configuration_Message(0x01, null, 12, "Interface Configuration Message",
                "This message will contain the firmware version number and other information about features currently enabled. It will be sent each time the unit is reset or programmed.",
                Direction.In, Source.Panel,

                // Properties
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                new Property("panel_firmware_version", 2, 4, 0, 0, "String", "Firmware version", false),

                // Byte 6 Supported transition message flags (1)
                new Property("panel_interface_configuration_message", 6, 1, 1, 1, "Bit",
                        "Interface Configuration Message", false),
                new Property("panel_zone_status_message", 6, 1, 4, 1, "Bit", "Zone Status Message", false),
                new Property("panel_zones_snapshot_message", 6, 1, 5, 1, "Bit", "Zones Snapshot Message", false),
                new Property("panel_partition_status_message", 6, 1, 6, 1, "Bit", "Partition Status Message", false),
                new Property("panel_partitions_snapshot_message", 6, 1, 7, 1, "Bit", "Partitions Snapshot Message",
                        false),

                // Byte 7 Supported transition message flags (2)
                new Property("panel_system_status_message", 7, 1, 0, 1, "Bit", "System Status Message", false),
                new Property("panel_x10_message_received", 7, 1, 1, 1, "Bit", "X-10 Message Received", false),
                new Property("panel_log_event_message", 7, 1, 2, 1, "Bit", "Log Event Message", false),
                new Property("panel_keypad_message_received", 7, 1, 3, 1, "Bit", "Keypad Message Received", false),

                // Byte 8 Supported request / command flags (1)
                new Property("panel_interface_configuration_request", 8, 1, 1, 1, "Bit",
                        "Interface Configuration Request", false),
                new Property("panel_zone_name_request", 8, 1, 3, 1, "Bit", "Zone Name Request", false),
                new Property("panel_zone_status_request", 8, 1, 4, 1, "Bit", "Zone Status Request", false),
                new Property("panel_zones_snapshot_request", 8, 1, 5, 1, "Bit", "Zones Snapshot Request", false),
                new Property("panel_partition_status_request", 8, 1, 6, 1, "Bit", "Partition Status Request", false),
                new Property("panel_partitions_snapshot_request", 8, 1, 7, 1, "Bit", "Partitions Snapshot Request",
                        false),

                // Byte 9 Supported request / command flags (2)
                new Property("panel_system_status_request", 9, 1, 0, 1, "Bit", "System Status Request", false),
                new Property("panel_send_x10_message", 9, 1, 1, 1, "Bit", "Send X-10 Message", false),
                new Property("panel_log_event_request", 9, 1, 2, 1, "Bit", "Log Event Request", false),
                new Property("panel_send_keypad_text_message", 9, 1, 3, 1, "Bit", "Send Keypad Text Message", false),
                new Property("panel_keypad_terminal_mode_request", 9, 1, 4, 1, "Bit", "Keypad Terminal Mode Request",
                        false),

                // Byte 10 Supported request / command flags (3)
                new Property("panel_program_data_request", 10, 1, 0, 1, "Bit", "Program Data Request", false),
                new Property("panel_program_data_command", 10, 1, 1, 1, "Bit", "Program Data Command", false),
                new Property("panel_user_information_request_with_pin", 10, 1, 2, 1, "Bit",
                        "User Information Request with PIN", false),
                new Property("panel_user_information_request_without_pin", 10, 1, 3, 1, "Bit",
                        "User Information Request without PIN", false),
                new Property("panel_set_user_code_command_with_pin", 10, 1, 4, 1, "Bit",
                        "Set User Code Command with PIN", false),
                new Property("panel_set_user_code_command_without_pin", 10, 1, 5, 1, "Bit",
                        "Set User Code Command without PIN", false),
                new Property("panel_set_user_authorization_command_with_pin", 10, 1, 6, 1, "Bit",
                        "Set User Authorization Command with PIN", false),
                new Property("panel_set_user_authorization_command_without_pin", 10, 1, 7, 1, "Bit",
                        "Set User Authorization Command without PIN", false),

                // Byte 11 Supported request / command flags (4)
                new Property("panel_store_communication_event_command", 11, 1, 2, 1, "Bit",
                        "Store Communication Event Command", false),
                new Property("panel_set_clock_calendar_command", 11, 1, 3, 1, "Bit", "Set Clock / Calendar Command",
                        false),
                new Property("panel_primary_keypad_function_with_pin", 11, 1, 4, 1, "Bit",
                        "Primary Keypad Function with PIN", false),
                new Property("panel_primary_keypad_function_without_pin", 11, 1, 5, 1, "Bit",
                        "Primary Keypad Function without PIN", false),
                new Property("panel_secondary_keypad_function", 11, 1, 6, 1, "Bit", "Secondary Keypad Function", false),
                new Property("panel_zone_bypass_toggle", 11, 1, 7, 1, "Bit", "Zone Bypass Toggle", false)),

        Zone_Name_Message(0x03, null, 18, "Zone Name Message",
                "This message will contain the 16-character name for the zone number that was requested (via Zone Name Request (23h)).",
                Direction.In, Source.Zone,

                // Properties
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                new Property("zone_number", 2, 1, 0, 0, "Int", "Zone number", false),
                new Property("zone_name", 3, 16, 0, 0, "String", "Zone name", false)),

        Zone_Status_Message(0x04, null, 8, "Zone Status Message",
                "This message will contain all information relevant to a zone in the system.", Direction.In,
                Source.Zone,

                // Properties
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                new Property("zone_number", 2, 1, 0, 0, "Int", "Zone number", false),

                // Byte 3 Partition mask
                new Property("zone_partition1", 3, 1, 0, 1, "Bit", "Partition 1 enable", false),
                new Property("zone_partition2", 3, 1, 1, 1, "Bit", "Partition 2 enable", false),
                new Property("zone_partition3", 3, 1, 2, 1, "Bit", "Partition 3 enable", false),
                new Property("zone_partition4", 3, 1, 3, 1, "Bit", "Partition 4 enable", false),
                new Property("zone_partition5", 3, 1, 4, 1, "Bit", "Partition 5 enable", false),
                new Property("zone_partition6", 3, 1, 5, 1, "Bit", "Partition 6 enable", false),
                new Property("zone_partition7", 3, 1, 6, 1, "Bit", "Partition 7 enable", false),
                new Property("zone_partition8", 3, 1, 7, 1, "Bit", "Partition 8 enable", false),

                // Byte 4 Zone type flags (1)
                new Property("zone_fire", 4, 1, 0, 1, "Bit", "Fire", false),
                new Property("zone_24hour", 4, 1, 1, 1, "Bit", "24 Hour", false),
                new Property("zone_key_switch", 4, 1, 2, 1, "Bit", "Key-switch", false),
                new Property("zone_follower", 4, 1, 3, 1, "Bit", "Follower", false),
                new Property("zone_entry_exit_delay_1", 4, 1, 4, 1, "Bit", "Entry / exit delay 1", false),
                new Property("zone_entry_exit_delay_2", 4, 1, 5, 1, "Bit", "Entry / exit delay 2", false),
                new Property("zone_interior", 4, 1, 6, 1, "Bit", "Interior", false),
                new Property("zone_local_only", 4, 1, 7, 1, "Bit", "Local only", false),

                // Byte 5 Zone type flags (2)
                new Property("zone_keypad_sounder", 5, 1, 0, 1, "Bit", "Keypad sounder", false),
                new Property("zone_yelping_siren", 5, 1, 1, 1, "Bit", "Yelping siren", false),
                new Property("zone_steady_siren", 5, 1, 2, 1, "Bit", "Steady siren", false),
                new Property("zone_chime", 5, 1, 3, 1, "Bit", "Chime", false),
                new Property("zone_bypassable", 5, 1, 4, 1, "Bit", "Bypassable", false),
                new Property("zone_group_bypassable", 5, 1, 5, 1, "Bit", "Group bypassable", false),
                new Property("zone_force_armable", 5, 1, 6, 1, "Bit", "Force armable", false),
                new Property("zone_entry_guard", 5, 1, 7, 1, "Bit", "Entry guard", false),

                // Byte 6 Zone type flags (3)
                new Property("zone_fast_loop_response", 6, 1, 0, 1, "Bit", "Fast loop response", false),
                new Property("zone_double_eol_tamper", 6, 1, 1, 1, "Bit", "Double EOL tamper", false),
                new Property("zone_type_trouble", 6, 1, 2, 1, "Bit", "Trouble", false),
                new Property("zone_cross_zone", 6, 1, 3, 1, "Bit", "Cross zone", false),
                new Property("zone_dialer_delay", 6, 1, 4, 1, "Bit", "Dialer delay", false),
                new Property("zone_swinger_shutdown", 6, 1, 5, 1, "Bit", "Swinger shutdown", false),
                new Property("zone_restorable", 6, 1, 6, 1, "Bit", "Restorable", false),
                new Property("zone_listen_in", 6, 1, 7, 1, "Bit", "Listen in", false),

                // Byte 7 Zone condition flags (1)
                new Property("zone_faulted", 7, 1, 0, 1, "Bit", "Faulted (or delayed trip)", false),
                new Property("zone_tampered", 7, 1, 1, 1, "Bit", "Tampered", false),
                new Property("zone_trouble", 7, 1, 2, 1, "Bit", "Trouble", false),
                new Property("zone_bypassed", 7, 1, 3, 1, "Bit", "Bypassed", false),
                new Property("zone_inhibited", 7, 1, 4, 1, "Bit", "Inhibited (force armed)", false),
                new Property("zone_low_battery", 7, 1, 5, 1, "Bit", "Low battery", false),
                new Property("zone_loss_of_supervision", 7, 1, 6, 1, "Bit", "Loss of supervision", false),

                // Byte 8 Zone condition flags (2)
                new Property("zone_alarm_memory", 8, 1, 0, 1, "Bit", "Alarm memory", false),
                new Property("zone_bypass_memory", 8, 1, 1, 1, "Bit", "Bypass memory", false)),

        Zones_Snapshot_Message(0x05, null, 10, "Zones Snapshot Message",
                "This message will contain an abbreviated set of information for any group of 16 zones possible on the system. (A zone offset number will set the range of zones)",
                Direction.In, Source.Panel,

                // Properties
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                new Property("zone_offset", 2, 1, 0, 0, "Int", "Zone offset (0= start at zone 1)", false),

                // Byte 3 Zone 1 & 2 (+offset) status flags
                new Property("", 3, 1, 0, 1, "Bit", "Zone 1 faulted (or delayed trip)", false),
                new Property("", 3, 1, 1, 1, "Bit", "Zone 1 bypass (or inhibited)", false),
                new Property("zone_1_trouble", 3, 1, 2, 1, "Bit", "Zone 1 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 3, 1, 3, 1, "Bit", "Zone 1 alarm memory", false),
                new Property("", 3, 1, 4, 1, "Bit", "Zone 2 faulted (or delayed trip)", false),
                new Property("", 3, 1, 5, 1, "Bit", "Zone 2 bypass (or inhibited)", false),
                new Property("zone_2_trouble", 3, 1, 6, 1, "Bit", "Zone 2 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 3, 1, 7, 1, "Bit", "Zone 2 alarm memory", false),

                // Byte 4 Zone 3 & 4 status flags (see byte 3)
                new Property("", 4, 1, 0, 1, "Bit", "Zone 3 faulted (or delayed trip)", false),
                new Property("", 4, 1, 1, 1, "Bit", "Zone 3 bypass (or inhibited)", false),
                new Property("zone_3_trouble", 4, 1, 2, 1, "Bit", "Zone 3 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 4, 1, 3, 1, "Bit", "Zone 3 alarm memory", false),
                new Property("", 4, 1, 4, 1, "Bit", "Zone 4 faulted (or delayed trip)", false),
                new Property("", 4, 1, 5, 1, "Bit", "Zone 4 bypass (or inhibited)", false),
                new Property("zone_4_trouble", 4, 1, 6, 1, "Bit", "Zone 4 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 4, 1, 7, 1, "Bit", "Zone 4 alarm memory", false),

                // Byte 5 Zone 5 & 6 status flags (see byte 3)
                new Property("", 5, 1, 0, 1, "Bit", "Zone 5 faulted (or delayed trip)", false),
                new Property("", 5, 1, 1, 1, "Bit", "Zone 5 bypass (or inhibited)", false),
                new Property("zone_5_trouble", 5, 1, 2, 1, "Bit", "Zone 5 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 5, 1, 3, 1, "Bit", "Zone 5 alarm memory", false),
                new Property("", 5, 1, 4, 1, "Bit", "Zone 6 faulted (or delayed trip)", false),
                new Property("", 5, 1, 5, 1, "Bit", "Zone 6 bypass (or inhibited)", false),
                new Property("zone_6_trouble", 5, 1, 6, 1, "Bit", "Zone 6 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 5, 1, 7, 1, "Bit", "Zone 6 alarm memory", false),

                // Byte 6 Zone 7 & 8 status flags (see byte 3)
                new Property("", 6, 1, 0, 1, "Bit", "Zone 7 faulted (or delayed trip)", false),
                new Property("", 6, 1, 1, 1, "Bit", "Zone 7 bypass (or inhibited)", false),
                new Property("zone_7_trouble", 6, 1, 2, 1, "Bit", "Zone 7 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 6, 1, 3, 1, "Bit", "Zone 7 alarm memory", false),
                new Property("", 6, 1, 4, 1, "Bit", "Zone 8 faulted (or delayed trip)", false),
                new Property("", 6, 1, 5, 1, "Bit", "Zone 8 bypass (or inhibited)", false),
                new Property("zone_8_trouble", 6, 1, 6, 1, "Bit", "Zone 8 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 6, 1, 7, 1, "Bit", "Zone 8 alarm memory", false),

                // Byte 7 Zone 9 & 10 status flags (see byte 3)
                new Property("", 7, 1, 0, 1, "Bit", "Zone 9 faulted (or delayed trip)", false),
                new Property("", 7, 1, 1, 1, "Bit", "Zone 9 bypass (or inhibited)", false),
                new Property("zone_9_trouble", 7, 1, 2, 1, "Bit", "Zone 9 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 7, 1, 3, 1, "Bit", "Zone 9 alarm memory", false),
                new Property("", 7, 1, 4, 1, "Bit", "Zone 10 faulted (or delayed trip)", false),
                new Property("", 7, 1, 5, 1, "Bit", "Zone 10 bypass (or inhibited)", false),
                new Property("zone_10_trouble", 7, 1, 6, 1, "Bit", "Zone 10 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 7, 1, 7, 1, "Bit", "Zone 10 alarm memory", false),

                // Byte 8 Zone 11 & 12 status flags (see byte 3)
                new Property("", 8, 1, 0, 1, "Bit", "Zone 11 faulted (or delayed trip)", false),
                new Property("", 8, 1, 1, 1, "Bit", "Zone 11 bypass (or inhibited)", false),
                new Property("zone_11_trouble", 8, 1, 2, 1, "Bit", "Zone 11 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 8, 1, 3, 1, "Bit", "Zone 11 alarm memory", false),
                new Property("", 8, 1, 4, 1, "Bit", "Zone 12 faulted (or delayed trip)", false),
                new Property("", 8, 1, 5, 1, "Bit", "Zone 12 bypass (or inhibited)", false),
                new Property("zone_12_trouble", 8, 1, 6, 1, "Bit", "Zone 12 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 8, 1, 7, 1, "Bit", "Zone 12 alarm memory", false),

                // Byte 9 Zone 13 & 14 status flags (see byte 3)
                new Property("", 9, 1, 0, 1, "Bit", "Zone 13 faulted (or delayed trip)", false),
                new Property("", 9, 1, 1, 1, "Bit", "Zone 13 bypass (or inhibited)", false),
                new Property("zone_13_trouble", 9, 1, 2, 1, "Bit", "Zone 13 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 9, 1, 3, 1, "Bit", "Zone 13 alarm memory", false),
                new Property("", 9, 1, 4, 1, "Bit", "Zone 14 faulted (or delayed trip)", false),
                new Property("", 9, 1, 5, 1, "Bit", "Zone 14 bypass (or inhibited)", false),
                new Property("zone_14_trouble", 9, 1, 6, 1, "Bit", "Zone 14 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 9, 1, 7, 1, "Bit", "Zone 14 alarm memory", false),

                // Byte 10 Zone 15 & 16 status flags (see byte 3)
                new Property("", 10, 1, 0, 1, "Bit", "Zone 15 faulted (or delayed trip)", false),
                new Property("", 10, 1, 1, 1, "Bit", "Zone 15 bypass (or inhibited)", false),
                new Property("zone_15_trouble", 10, 1, 2, 1, "Bit", "Zone 15 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 10, 1, 3, 1, "Bit", "Zone 15 alarm memory", false),
                new Property("", 10, 1, 4, 1, "Bit", "Zone 16 faulted (or delayed trip)", false),
                new Property("", 10, 1, 5, 1, "Bit", "Zone 16 bypass (or inhibited)", false),
                new Property("zone_16_trouble", 10, 1, 6, 1, "Bit", "Zone 16 trouble (tamper, low battery, or lost)",
                        false),
                new Property("", 10, 1, 7, 1, "Bit", "Zone 16 alarm memory", false)),

        Partition_Status_Message(0x06, null, 9, "Partition Status Message",
                "This message will contain all information relevant to a single partition in the system.", Direction.In,
                Source.Partition,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                new Property("partition_number", 2, 1, 0, 0, "Int", "Partition number (0= partition 1)", false),

                // Byte 3 Partition condition flags (1)
                new Property("partition_bypass_code_required", 3, 1, 0, 1, "Bit", "Bypass code required", false),
                new Property("partition_fire_trouble", 3, 1, 1, 1, "Bit", "Fire trouble", false),
                new Property("partition_fire", 3, 1, 2, 1, "Bit", "Fire", false),
                new Property("partition_pulsing_buzzer", 3, 1, 3, 1, "Bit", "Pulsing Buzzer", false),
                new Property("partition_tlm_fault_memory", 3, 1, 4, 1, "Bit", "TLM fault memory", false),
                new Property("partition_armed", 3, 1, 6, 1, "Bit", "Armed", false),
                new Property("partition_instant", 3, 1, 7, 1, "Bit", "Instant", false),

                // Byte 4 Partition condition flags (2)
                new Property("partition_previous_alarm", 4, 1, 0, 1, "Bit", "Previous Alarm", false),
                new Property("partition_siren_on", 4, 1, 1, 1, "Bit", "Siren on", false),
                new Property("partition_steady_siren_on", 4, 1, 2, 1, "Bit", "Steady siren on", false),
                new Property("partition_alarm_memory", 4, 1, 3, 1, "Bit", "Alarm memory", false),
                new Property("partition_tamper", 4, 1, 4, 1, "Bit", "Tamper", false),
                new Property("partition_cancel_command_entered", 4, 1, 5, 1, "Bit", "Cancel command entered", false),
                new Property("partition_code_entered", 4, 1, 6, 1, "Bit", "Code entered", false),
                new Property("partition_cancel_pending", 4, 1, 7, 1, "Bit", "Cancel pending", false),

                // Byte 5 Partition condition flags (3)
                new Property("partition_silent_exit_enabled", 5, 1, 1, 1, "Bit", "Silent exit enabled", false),
                new Property("partition_entryguard", 5, 1, 2, 1, "Bit", "Entryguard (stay mode)", false),
                new Property("partition_chime_mode_on", 5, 1, 3, 1, "Bit", "Chime mode on", false),
                new Property("partition_entry", 5, 1, 4, 1, "Bit", "Entry", false),
                new Property("partition_delay_expiration_warning", 5, 1, 5, 1, "Bit", "Delay expiration warning",
                        false),
                new Property("partition_exit1", 5, 1, 6, 1, "Bit", "Exit1", false),
                new Property("partition_exit2", 5, 1, 7, 1, "Bit", "Exit2", false),

                // Byte 6 Partition condition flags (4)
                new Property("partition_led_extinguish", 6, 1, 0, 1, "Bit", "LED extinguish", false),
                new Property("partition_cross_timing", 6, 1, 1, 1, "Bit", "Cross timing", false),
                new Property("partition_recent_closing_being_timed", 6, 1, 2, 1, "Bit", "Recent closing being timed",
                        false),
                new Property("partition_exit_error_triggered", 6, 1, 4, 1, "Bit", "Exit error triggered", false),
                new Property("partition_auto_home_inhibited", 6, 1, 5, 1, "Bit", "Auto home inhibited", false),
                new Property("partition_sensor_low_battery", 6, 1, 6, 1, "Bit", "Sensor low battery", false),
                new Property("partition_sensor_lost_supervision", 6, 1, 7, 1, "Bit", "Sensor lost supervision", false),

                new Property("", 7, 1, 0, 0, "Int", "Last user number", false),

                // Byte 8 Partition condition flags (5)
                new Property("partition_zone_bypassed", 8, 1, 0, 1, "Bit", "Zone bypassed", false),
                new Property("partition_force_arm_triggered_by_auto_arm", 8, 1, 1, 1, "Bit",
                        "Force arm triggered by auto arm", false),
                new Property("partition_ready_to_arm", 8, 1, 2, 1, "Bit", "Ready to arm", false),
                new Property("partition_ready_to_force_arm", 8, 1, 3, 1, "Bit", "Ready to force arm", false),
                new Property("partition_valid_pin_accepted", 8, 1, 4, 1, "Bit", "Valid PIN accepted", false),
                new Property("partition_chime_on", 8, 1, 5, 1, "Bit", "Chime on (sounding)", false),
                new Property("partition_error_beep", 8, 1, 6, 1, "Bit", "Error beep (triple beep)", false),
                new Property("partition_tone_on", 8, 1, 7, 1, "Bit", "Tone on (activation tone)", false),

                // Byte 9 Partition condition flags (6)
                new Property("partition_entry1", 9, 1, 0, 1, "Bit", "Entry 1", false),
                new Property("partition_open_period", 9, 1, 1, 1, "Bit", "Open period", false),
                new Property("partition_alarm_sent_using_phone_number_1", 9, 1, 2, 1, "Bit",
                        "Alarm sent using phone number 1", false),
                new Property("partition_alarm_sent_using_phone_number_2", 9, 1, 3, 1, "Bit",
                        "Alarm sent using phone number 2", false),
                new Property("partition_alarm_sent_using_phone_number_3", 9, 1, 4, 1, "Bit",
                        "Alarm sent using phone number 3", false),
                new Property("partition_cancel_report_is_in_the_stack", 9, 1, 5, 1, "Bit",
                        "Cancel report is in the stack", false),
                new Property("partition_keyswitch_armed", 9, 1, 6, 1, "Bit", "Keyswitch armed", false),
                new Property("partition_delay_trip_in_progress", 9, 1, 7, 1, "Bit",
                        "Delay Trip in progress (common zone)", false)),

        Partitions_Snapshot_Message(0x07, null, 9, "Partitions Snapshot Message",
                "This message will contain an abbreviated set of information for all 8 partitions on the system.",
                Direction.In, Source.Panel,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2 Partition 1 condition flags
                new Property("partition_1_valid", 2, 1, 0, 1, "Bit", "Partition 1 valid partition", false),
                new Property("", 2, 1, 1, 1, "Bit", "Partition 1 ready", false),
                new Property("", 2, 1, 2, 1, "Bit", "Partition 1 armed", false),
                new Property("", 2, 1, 3, 1, "Bit", "Partition 1 stay mode", false),
                new Property("", 2, 1, 4, 1, "Bit", "Partition 1 chime mode", false),
                new Property("", 2, 1, 5, 1, "Bit", "Partition 1 any entry delay", false),
                new Property("", 2, 1, 6, 1, "Bit", "Partition 1 any exit delay", false),
                new Property("", 2, 1, 7, 1, "Bit", "Partition 1 previous alarm", false),

                // Byte 3 Partition 2 condition flags
                new Property("partition_2_valid", 3, 1, 0, 1, "Bit", "Partition 2 valid partition", false),
                new Property("", 3, 1, 1, 1, "Bit", "Partition 2 ready", false),
                new Property("", 3, 1, 2, 1, "Bit", "Partition 2 armed", false),
                new Property("", 3, 1, 3, 1, "Bit", "Partition 2 stay mode", false),
                new Property("", 3, 1, 4, 1, "Bit", "Partition 2 chime mode", false),
                new Property("", 3, 1, 5, 1, "Bit", "Partition 2 any entry delay", false),
                new Property("", 3, 1, 6, 1, "Bit", "Partition 2 any exit delay", false),
                new Property("", 3, 1, 7, 1, "Bit", "Partition 2 previous alarm", false),

                // Byte 4 Partition 3 condition flags
                new Property("partition_3_valid", 4, 1, 0, 1, "Bit", "Partition 3 valid partition", false),
                new Property("", 4, 1, 1, 1, "Bit", "Partition 3 ready", false),
                new Property("", 4, 1, 2, 1, "Bit", "Partition 3 armed", false),
                new Property("", 4, 1, 3, 1, "Bit", "Partition 3 stay mode", false),
                new Property("", 4, 1, 4, 1, "Bit", "Partition 3 chime mode", false),
                new Property("", 4, 1, 5, 1, "Bit", "Partition 3 any entry delay", false),
                new Property("", 4, 1, 6, 1, "Bit", "Partition 3 any exit delay", false),
                new Property("", 4, 1, 7, 1, "Bit", "Partition 3 previous alarm", false),

                // Byte 5 Partition 4 condition flags
                new Property("partition_4_valid", 5, 1, 0, 1, "Bit", "Partition 4 valid partition", false),
                new Property("", 5, 1, 1, 1, "Bit", "Partition 4 ready", false),
                new Property("", 5, 1, 2, 1, "Bit", "Partition 4 armed", false),
                new Property("", 5, 1, 3, 1, "Bit", "Partition 4 stay mode", false),
                new Property("", 5, 1, 4, 1, "Bit", "Partition 4 chime mode", false),
                new Property("", 5, 1, 5, 1, "Bit", "Partition 4 any entry delay", false),
                new Property("", 5, 1, 6, 1, "Bit", "Partition 4 any exit delay", false),
                new Property("", 5, 1, 7, 1, "Bit", "Partition 4 previous alarm", false),

                // Byte 6 Partition 5 condition flags
                new Property("partition_5_valid", 6, 1, 0, 1, "Bit", "Partition 5 valid partition", false),
                new Property("", 6, 1, 1, 1, "Bit", "Partition 5 ready", false),
                new Property("", 6, 1, 2, 1, "Bit", "Partition 5 armed", false),
                new Property("", 6, 1, 3, 1, "Bit", "Partition 5 stay mode", false),
                new Property("", 6, 1, 4, 1, "Bit", "Partition 5 chime mode", false),
                new Property("", 6, 1, 5, 1, "Bit", "Partition 5 any entry delay", false),
                new Property("", 6, 1, 6, 1, "Bit", "Partition 5 any exit delay", false),
                new Property("", 6, 1, 7, 1, "Bit", "Partition 5 previous alarm", false),

                // Byte 7 Partition 6 condition flags
                new Property("partition_6_valid", 7, 1, 0, 1, "Bit", "Partition 6 valid partition", false),
                new Property("", 7, 1, 1, 1, "Bit", "Partition 6 ready", false),
                new Property("", 7, 1, 2, 1, "Bit", "Partition 6 armed", false),
                new Property("", 7, 1, 3, 1, "Bit", "Partition 6 stay mode", false),
                new Property("", 7, 1, 4, 1, "Bit", "Partition 6 chime mode", false),
                new Property("", 7, 1, 5, 1, "Bit", "Partition 6 any entry delay", false),
                new Property("", 7, 1, 6, 1, "Bit", "Partition 6 any exit delay", false),
                new Property("", 7, 1, 7, 1, "Bit", "Partition 6 previous alarm", false),

                // Byte 8 Partition 7 condition flags
                new Property("partition_7_valid", 8, 1, 0, 1, "Bit", "Partition 7 valid partition", false),
                new Property("", 8, 1, 1, 1, "Bit", "Partition 7 ready", false),
                new Property("", 8, 1, 2, 1, "Bit", "Partition 7 armed", false),
                new Property("", 8, 1, 3, 1, "Bit", "Partition 7 stay mode", false),
                new Property("", 8, 1, 4, 1, "Bit", "Partition 7 chime mode", false),
                new Property("", 8, 1, 5, 1, "Bit", "Partition 7 any entry delay", false),
                new Property("", 8, 1, 6, 1, "Bit", "Partition 7 any exit delay", false),
                new Property("", 8, 1, 7, 1, "Bit", "Partition 7 previous alarm", false),

                // Byte 9 Partition 8 condition flags
                new Property("partition_8_valid", 9, 1, 0, 1, "Bit", "Partition 8 valid partition", false),
                new Property("", 9, 1, 1, 1, "Bit", "Partition 8 ready", false),
                new Property("", 9, 1, 2, 1, "Bit", "Partition 8 armed", false),
                new Property("", 9, 1, 3, 1, "Bit", "Partition 8 stay mode", false),
                new Property("", 9, 1, 4, 1, "Bit", "Partition 8 chime mode", false),
                new Property("", 9, 1, 5, 1, "Bit", "Partition 8 any entry delay", false),
                new Property("", 9, 1, 6, 1, "Bit", "Partition 8 any exit delay", false),
                new Property("", 9, 1, 8, 1, "Bit", "Partition 8 previous alarm", false)),

        System_Status_Message(0x08, null, 12, "System Status Message",
                "This message will contain all information relevant to the entire system.", Direction.In, Source.Panel,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("", 2, 1, 0, 0, "Int", "Panel ID number", false),

                // Byte 3
                new Property("", 3, 1, 0, 1, "Bit", "Line seizure", false),
                new Property("", 3, 1, 1, 1, "Bit", "Off hook", false),
                new Property("", 3, 1, 2, 1, "Bit", "Initial handshake received", false),
                new Property("", 3, 1, 3, 1, "Bit", "Download in progress", false),
                new Property("", 3, 1, 4, 1, "Bit", "Dialer delay in progress", false),
                new Property("", 3, 1, 5, 1, "Bit", "Using backup phone", false),
                new Property("", 3, 1, 6, 1, "Bit", "Listen in active", false),
                new Property("", 3, 1, 7, 1, "Bit", "Two way lockout", false),

                // Byte 4
                new Property("", 4, 1, 0, 1, "Bit", "Ground fault", false),
                new Property("", 4, 1, 1, 1, "Bit", "Phone fault", false),
                new Property("", 4, 1, 2, 1, "Bit", "Fail to communicate", false),
                new Property("", 4, 1, 3, 1, "Bit", "Fuse fault", false),
                new Property("", 4, 1, 4, 1, "Bit", "Box tamper", false),
                new Property("", 4, 1, 5, 1, "Bit", "Siren tamper / trouble", false),
                new Property("", 4, 1, 6, 1, "Bit", "Low Battery", false),
                new Property("", 4, 1, 7, 1, "Bit", "AC fail", false),

                // Byte 5
                new Property("", 5, 1, 0, 1, "Bit", "Expander box tamper", false),
                new Property("", 5, 1, 1, 1, "Bit", "Expander AC failure", false),
                new Property("", 5, 1, 2, 1, "Bit", "Expander low battery", false),
                new Property("", 5, 1, 3, 1, "Bit", "Expander loss of supervision", false),
                new Property("", 5, 1, 4, 1, "Bit", "Expander auxiliary output over current", false),
                new Property("", 5, 1, 5, 1, "Bit", "Auxiliary communication channel failure", false),
                new Property("", 5, 1, 6, 1, "Bit", "Expander bell fault", false),

                // Byte 6
                new Property("", 6, 1, 0, 1, "Bit", "6 digit PIN enabled", false),
                new Property("", 6, 1, 1, 1, "Bit", "Programming token in use", false),
                new Property("", 6, 1, 2, 1, "Bit", "PIN required for local download", false),
                new Property("", 6, 1, 3, 1, "Bit", "Global pulsing buzzer", false),
                new Property("", 6, 1, 4, 1, "Bit", "Global Siren on", false),
                new Property("", 6, 1, 5, 1, "Bit", "Global steady siren", false),
                new Property("", 6, 1, 6, 1, "Bit", "Bus device has line seized", false),
                new Property("", 6, 1, 7, 1, "Bit", "Bus device has requested sniff mode", false),

                // Byte 7
                new Property("", 7, 1, 0, 1, "Bit", "Dynamic battery test", false),
                new Property("", 7, 1, 1, 1, "Bit", "AC power on", false),
                new Property("", 7, 1, 2, 1, "Bit", "Low battery memory", false),
                new Property("", 7, 1, 3, 1, "Bit", "Ground fault memory", false),
                new Property("", 7, 1, 4, 1, "Bit", "Fire alarm verification being timed", false),
                new Property("", 7, 1, 5, 1, "Bit", "Smoke power reset", false),
                new Property("", 7, 1, 6, 1, "Bit", "50 Hz line power detected", false),
                new Property("", 7, 1, 7, 1, "Bit", "Timing a high voltage battery charge", false),

                // Byte 8
                new Property("", 8, 1, 0, 1, "Bit", "Communication since last autotest", false),
                new Property("", 8, 1, 1, 1, "Bit", "Power up delay in progress", false),
                new Property("", 8, 1, 2, 1, "Bit", "Walk test mode", false),
                new Property("", 8, 1, 3, 1, "Bit", "Loss of system time", false),
                new Property("", 8, 1, 4, 1, "Bit", "Enroll requested", false),
                new Property("", 8, 1, 5, 1, "Bit", "Test fixture mode", false),
                new Property("", 8, 1, 6, 1, "Bit", "Control shutdown mode", false),
                new Property("", 8, 1, 7, 1, "Bit", "Timing a cancel window", false),

                // Byte 9
                new Property("", 9, 1, 7, 1, "Bit", "Call back in progress", false),

                // Byte 10
                new Property("", 10, 1, 0, 1, "Bit", "Phone line faulted", false),
                new Property("", 10, 1, 1, 1, "Bit", "Voltage present interrupt active", false),
                new Property("", 10, 1, 2, 1, "Bit", "House phone off hook", false),
                new Property("", 10, 1, 3, 1, "Bit", "Phone line monitor enabled", false),
                new Property("", 10, 1, 4, 1, "Bit", "Sniffing", false),
                new Property("", 10, 1, 5, 1, "Bit", "Last read was off hook", false),
                new Property("", 10, 1, 6, 1, "Bit", "Listen in requested", false),
                new Property("", 10, 1, 7, 1, "Bit", "Listen in trigger", false),

                // Byte 11
                new Property("", 11, 1, 0, 1, "Bit", "Valid partition 1", false),
                new Property("", 11, 1, 1, 1, "Bit", "Valid partition 2", false),
                new Property("", 11, 1, 2, 1, "Bit", "Valid partition 3", false),
                new Property("", 11, 1, 3, 1, "Bit", "Valid partition 4", false),
                new Property("", 11, 1, 4, 1, "Bit", "Valid partition 5", false),
                new Property("", 11, 1, 5, 1, "Bit", "Valid partition 6", false),
                new Property("", 11, 1, 6, 1, "Bit", "Valid partition 7", false),
                new Property("", 11, 1, 7, 1, "Bit", "Valid partition 8", false),

                // Byte 12 Communicator stack pointer
                new Property("panel_communicator_stack_pointer", 12, 1, 0, 0, "Int", "Communicator stack pointer",
                        false)),

        X10_Message_Received(0x09, null, 4, "X-10 Message Received",
                "This message contains information about an X-10 command that was requested by any device on the system bus.",
                Direction.In, Source.Panel,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("", 2, 1, 0, 0, "Int", "House code (0=house A)", false),

                // Byte 3
                new Property("", 3, 1, 0, 0, "Int", "Unit code (0=unit 1)", false),

                // Byte 4
                new Property("", 4, 1, 0, 0, "Int", "X-10 function code", false)),

        Log_Event_Message(0x0a, null, 10, "Log Event Message",
                "This message will contain all information relating to an event in the log memory.", Direction.In,
                Source.Panel,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                // Byte 2
                new Property("panel_log_event_number", 2, 1, 0, 0, "Int", "Event number of this message", false),
                // Byte 3
                new Property("panel_log_event_size", 3, 1, 0, 0, "Int",
                        "Total log size (number of log entries allowed)", false),

                // Byte 4
                new Property("panel_log_event_type", 4, 1, 0, 7, "Int", "Event type", false),
                // Bits 0-6 See type definitions in table that follows
                // Bit 7 Non-reporting event if not set

                // Byte 5
                new Property("panel_log_event_zud", 5, 1, 0, 0, "Int", "Zone / User / Device number", false),
                // Byte 6
                new Property("panel_log_event_partition", 6, 1, 0, 0, "Int",
                        "Partition number (0=partition 1, if relevant)", false),
                // Byte 7
                new Property("panel_log_event_month", 7, 1, 0, 0, "Int", "Month (1-12)", false),
                // Byte 8
                new Property("panel_log_event_day", 8, 1, 0, 0, "Int", "Day (1-31)", false),
                // Byte 9
                new Property("panel_log_event_hour", 9, 1, 0, 0, "Int", "Hour (0-23)", false),
                // Byte 10
                new Property("panel_log_event_minute", 10, 1, 0, 0, "Int", "Minute (0-59)", false)),

        Keypad_Message_Received(0x0b, null, 3, "Keypad Message Received",
                "This message contains a keystroke from a keypad that is in a Terminal Mode.", Direction.In,
                Source.Keypad,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("keypad_address", 1, 2, 0, 0, "Int", "Keypad address", false),

                // Byte 3
                new Property("", 1, 1, 0, 0, "Int", "Key value", false)),

        Program_Data_Reply(0x10, null, 13, "Program Data Reply",
                "This message will contain a system device’s buss address, logical location, and program data that was previously requested (via Program Data Request (3Ch)).",
                Direction.In, Source.Panel,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("", 2, 1, 0, 0, "Int", "Device’s buss address", false),

                // Byte 3 Upper logical location / offset
                new Property("", 3, 1, 0, 3, "Int", "Bits 8-11 of logical location", false),
                new Property("", 3, 1, 4, 4, "Int", "Segment size (0=byte, 1=nibble)", false),
                new Property("", 3, 1, 5, 1, "Bit", "Must be 0", false),
                new Property("", 3, 1, 6, 6, "Int", "Segment offset (0-none, 1=8 bytes)", false),
                new Property("", 3, 1, 7, 1, "Bit", "Must be 0", false),

                // Byte 4 Bits 0-7 of logical location
                new Property("", 4, 1, 0, 0, "Int", "Bits 0-7 of logical location", false),

                // Byte 5 Location length / data type
                new Property("", 5, 1, 0, 4, "Int", "Number of segments in location (0=1 segment)", false),
                new Property("", 5, 1, 5, 7, "Int",
                        "Data type : 0=Binary 1=Decimal 2=Hexadecimal 3=ASCII 4=unused 5=unused 6=unused 7=unused",
                        false),

                // Byte 6 Data byte
                new Property("", 6, 1, 0, 0, "Int", "Data byte 0", false),
                // Byte 7 Data byte
                new Property("", 7, 1, 0, 0, "Int", "Data byte 1", false),
                // Byte 8 Data byte
                new Property("", 8, 1, 0, 0, "Int", "Data byte 2", false),
                // Byte 9 Data byte
                new Property("", 9, 1, 0, 0, "Int", "Data byte 3", false),
                // Byte 10 Data byte
                new Property("", 10, 1, 0, 0, "Int", "Data byte 4", false),
                // Byte 11 Data byte
                new Property("", 11, 1, 0, 0, "Int", "Data byte 5", false),
                // Byte 12 Data byte
                new Property("", 12, 1, 0, 0, "Int", "Data byte 6", false),
                // Byte 13 Data byte
                new Property("", 13, 1, 0, 0, "Int", "Data byte 7", false)),

        User_Information_Reply(0x12, null, 7, "User Information Reply",
                "This message will contain all digits, attributes and partitions for the requested user PIN number that was previously requested (via User Information Request with(out) PIN (32h,33h)).",
                Direction.In, Source.Panel,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("", 2, 1, 0, 0, "Int", "User Number (1=user 1)", false),

                // Byte 3 PIN digits 1 & 2
                new Property("", 3, 1, 0, 3, "Int", "PIN digit 1", false),
                new Property("", 3, 1, 4, 7, "Int", "PIN digit 2", false),

                // Byte 4 PIN digits 3 & 4
                new Property("", 4, 1, 0, 3, "Int", "PIN digit 3", false),
                new Property("", 4, 1, 4, 7, "Int", "PIN digit 4", false),

                // Byte 5 PIN digits 5 & 6
                new Property("", 5, 1, 0, 3, "Int", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
                new Property("", 5, 1, 4, 7, "Int", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),

                // Byte 6* Authority flags
                new Property("", 6, 1, 0, 1, "Bit", "Reserved (if bit 7 is clear) || Output 1 enable (if bit 7 is set)",
                        false),
                new Property("", 6, 1, 1, 1, "Bit", "Arm only (if bit 7 is clear) || Output 2 enable (if bit 7 is set)",
                        false),
                new Property("", 6, 1, 2, 1, "Bit",
                        "Arm only (during close window) (if bit 7 is clear) || Output 3 enable (if bit 7 is set)",
                        false),
                new Property("", 6, 1, 3, 1, "Bit",
                        "Master / program (if bit 7 is clear) || Output 4 enable (if bit 7 is set)", false),
                new Property("", 6, 1, 4, 1, "Bit",
                        "Arm / Disarm (if bit 7 is clear) || Arm / Disarm (if bit 7 is set)", false),
                new Property("", 6, 1, 5, 1, "Bit",
                        "Bypass enable (if bit 7 is clear) || Bypass enable (if bit 7 is set)", false),
                new Property("", 6, 1, 6, 1, "Bit",
                        "Open / close report enable (if bit 7 is clear) || Open / close report enable (if bit 7 is set)",
                        false),
                new Property("", 6, 1, 7, 1, "Bit", "Must be a 0 (if bit 7 is clear) || Must be a 1 (if bit 7 is set)",
                        false),

                // Byte 7 Authorized partition(s) mask
                new Property("", 7, 1, 0, 1, "Bit", "Authorized for partition 1", false),
                new Property("", 7, 1, 1, 1, "Bit", "Authorized for partition 2", false),
                new Property("", 7, 1, 2, 1, "Bit", "Authorized for partition 3", false),
                new Property("", 7, 1, 3, 1, "Bit", "Authorized for partition 4", false),
                new Property("", 7, 1, 4, 1, "Bit", "Authorized for partition 5", false),
                new Property("", 7, 1, 5, 1, "Bit", "Authorized for partition 6", false),
                new Property("", 7, 1, 6, 1, "Bit", "Authorized for partition 7", false),
                new Property("", 7, 1, 7, 1, "Bit", "Authorized for partition 8", false)),

        Request_Failed(0x1c, null, 1, "Command / Request Failed",
                "This message is sent in place of a ‘Positive Acknowledge’ message when a command or request was received properly, but the system was unable to carry out the task correctly. This would normally occur 2.5 seconds after receiving the initial command or request.",
                Direction.In, Source.Panel,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false)),

        Positive_Acknowledge(0x1d, null, 1, "Positive Acknowledge",
                "This message will acknowledge receipt of a message that had the ‘Acknowledge Required’ flag set in the command byte.",
                Direction.In, Source.Panel,
                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false)),

        Negative_Acknowledge(0x1e, null, 1, "Negative Acknowledge",
                "This message is sent in place of a ‘Positive Acknowledge’ message when the message received was not properly formatted. It will also be sent if an additional message is received before a reply has been returned during the 2.5 second allowable reply period of a previous message. An ‘Implied Negative Acknowledge’ is assumed when no acknowledge is returned with 3 seconds.",
                Direction.In, Source.Panel,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false)),

        Message_Rejected(0x1f, null, 1, "Message Rejected",
                "This message is sent in place of a ‘Positive Acknowledge’ message when the message was received properly formatted, but not supported or disabled.",
                Direction.In, Source.Panel,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false)),

        Interface_Configuration_Request(0x21, new int[] { 0x01, 0x1c, 0x1f }, 1, "Interface Configuration Request",
                "This request will cause the return of the Interface Configuration Message (01h) containing information about the options selected on the interface.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false)),

        Zone_Name_Request(0x23, new int[] { 0x03, 0x1c, 0x1f }, 2, "Zone Name Request",
                "This request will cause the return of the Zone Name Message (03h) for the zone number that was requested.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("", 2, 1, 0, 0, "Int", "Zone number (0= zone 1)", true)),

        Zone_Status_Request(0x24, new int[] { 0x04, 0x1c, 0x1f }, 2, "Zone Status Request",
                "This request will cause the return of the Zone Status Message (04h) for the zone number that was requested.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("zone_number", 2, 1, 0, 0, "Int", "Zone number (0= zone 1)", true)),

        Zones_Snapshot_Request(0x25, new int[] { 0x05, 0x1c, 0x1f }, 2, "Zones Snapshot Request",
                "This request will cause the return of the Zones Snapshot Message (05h) with the group of zones starting at the zone 1 plus the offset value.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("", 2, 1, 0, 0, "Int", "Zone number offset (0= start at zone 1)", true)),

        Partition_Status_Request(0x26, new int[] { 0x06, 0x1c, 0x1f }, 2, "Partition Status Request",
                "This request will cause the return of the Partition Status Message (06h) for the partition number that was requested.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("partition_number", 2, 1, 0, 0, "Int", "Partition number (0= partition 1)", true)),

        Partitions_Snapshot_Request(0x27, new int[] { 0x07, 0x1c, 0x1f }, 1, "Partitions Snapshot Request",
                "This request will cause the return of the Partitions Snapshot Message (07h) containing all partitions.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false)),

        System_Status_Request(0x28, new int[] { 0x08, 0x1c, 0x1f }, 1, "System Status Request",
                "This request will cause the return of the System Status Message (08h).", Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false)),

        Send_X_10_Message(0x29, new int[] { 0x1d, 0x1c, 0x1f }, 4, "Send X-10 Message",
                "This message will contain information about an X-10 command that should be resent on the system bus.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2
                new Property("", 2, 1, 0, 0, "Int", "House code (0=house A) ", true),

                // Byte 3
                new Property("", 3, 1, 0, 0, "Int", "Unit code (0=unit 1)", true),

                // Byte 4
                new Property("", 4, 1, 0, 0, "Int", "X-10 function code (see table at message # 0Ah)", true)),

        Log_Event_Request(0x2a, new int[] { 0x0a, 0x1c, 0x1f }, 2, "Log Event Request",
                "This request will cause the return of the Log Event Message (0Ah).", Direction.Out, Source.None,
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                new Property("panel_log_event_number", 2, 1, 0, 0, "Int", "Event number requested", true)),

        Send_Keypad_Text_Message(0x2b, new int[] { 0x1d, 0x1c, 0x1f }, 12, "Send Keypad Text Message",
                "This message will contain ASCII text for a specific keypad on the bus that will be displayed during Terminal Mode.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                // Byte 2 Keypad address
                new Property("", 2, 1, 0, 0, "Int", "Keypad address", false),
                // Byte 3 Keypad type (0=NX-148e)(all others not supported)
                new Property("", 3, 1, 0, 0, "Int", "Keypad type", false),
                // Byte 4 Display storage location (0=top left corner
                new Property("", 4, 1, 0, 0, "Int", "Display storage location", false),
                // Byte 5 ASCII character for location +0
                new Property("", 5, 1, 0, 0, "Int", "ASCII character for location +0", false),
                // Byte 6 ASCII character for location +1
                new Property("", 6, 1, 0, 0, "Int", "ASCII character for location +1", false),
                // Byte 7 ASCII character for location +2
                new Property("", 7, 1, 0, 0, "Int", "ASCII character for location +2", false),
                // Byte 8 ASCII character for location +3
                new Property("", 8, 1, 0, 0, "Int", "ASCII character for location +3", false),
                // Byte 9 ASCII character for location +4
                new Property("", 9, 1, 0, 0, "Int", "ASCII character for location +4", false),
                // Byte 10 ASCII character for location +5
                new Property("", 10, 1, 0, 0, "Int", "ASCII character for location +5", false),
                // Byte 11 ASCII character for location +6
                new Property("", 11, 1, 0, 0, "Int", "ASCII character for location +6", false),
                // Byte 12 ASCII character for location +7
                new Property("", 12, 1, 0, 0, "Int", "ASCII character for location +7", false)),

        Keypad_Terminal_Mode_Request(0x2c, new int[] { 0x1d, 0x1c, 0x1f }, 3, "Keypad Terminal Mode Request",
                "This message will contain the address of a keypad that should enter a Terminal Mode for the time contained. Only one keypad should be in the Terminal Mode at a time.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                // Byte 2 Keypad address
                new Property("", 2, 1, 0, 0, "Int", "Keypad address", false),
                // Byte 3
                new Property("", 3, 1, 0, 0, "Int", "Number of seconds for Terminal Mode", false)),

        Program_Data_Request(0x30, new int[] { 0x10, 0x1c, 0x1f }, 4, "Program Data Request",
                "This message will contain a system device’s buss address and the logical location of program data that will be returned in a Program Data Reply message (10h).",
                Direction.Out, Source.None,

                // Properties
                // Byte 1
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                // Byte 2 Device’s buss address
                new Property("", 2, 1, 0, 0, "Int", "Device’s buss address", false),
                // Byte 3 Upper logical location / offset
                // Bits 0-3 Bits 8-11 of logical location
                new Property("", 3, 1, 0, 4, "Int", "Bits 8-11 of logical location", false),
                // Bits 4,5 Must be 0
                new Property("", 3, 1, 4, 2, "Bit", "Must be 0", false),
                // Bit 6 Segment offset (0-none, 1=8 bytes)
                new Property("", 3, 1, 6, 1, "Bit", "Segment offset (0-none, 1=8 bytes)", false),
                // Bit 7
                new Property("", 3, 1, 7, 1, "Bit", "Must be 0", false),
                // Byte 4 Bits 0-7 of logical location
                new Property("", 4, 1, 0, 0, "Int", "Bits 0-7 of logical location", false)),

        Program_Data_Command(0x31, new int[] { 0x1d, 0x1c, 0x1f }, 13, "Program Data Command",
                "This message will contain a system device’s buss address and the logical location where the included data should be stored.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                // Byte 2 Device’s buss address
                new Property("", 2, 1, 0, 0, "Int", "Device’s buss address", false),
                // Byte 3 Upper logical location / offset
                // Bits 0-3 Bits 8-11 of logical location
                new Property("", 3, 1, 0, 4, "Bit", "Bits 8-11 of logical location", false),
                // Bit 4 Segment size (0=byte, 1=nibble)
                new Property("", 3, 1, 4, 1, "Bit", "Segment size (0=byte, 1=nibble)", false),
                // Bit 5 Must be 1
                new Property("", 3, 1, 5, 1, "Bit", "Must be 1", false),
                // Bit 6 Segment offset (0-none, 1=8 bytes)
                new Property("", 3, 1, 6, 1, "Bit", "Segment offset (0-none, 1=8 bytes)", false),
                // Bit 7 Must be 0
                new Property("", 3, 1, 7, 1, "Bit", "Must be 0", false),
                // Byte 4 Bits 0-7 of logical location
                new Property("", 4, 1, 0, 0, "Int", "Bits 0-7 of logical location", false),
                // Byte 5 Location length / data type
                // Bits 0-4 Number of segments in location (0=1 segment)
                new Property("", 5, 1, 0, 5, "Bit", "Number of segments in location (0=1 segment)", false),
                // Bits 5-7 Data type : 5=unused
                new Property("", 5, 1, 5, 3, "Bit",
                        "Data type: 0=Binary, 1=Decimal, 2=Hexadecimal, 3=ASCII, 4=unused, 5=unused, 6=unused, 7=unused",
                        false),
                // Byte 6 Data byte 1 to store
                new Property("", 6, 1, 0, 0, "Int", "Data byte 1 to store", false),
                // Byte 7 Data byte 2 to store
                new Property("", 7, 1, 0, 0, "Int", "Data byte 2 to store", false),
                // Byte 8 Data byte 3 to store
                new Property("", 8, 1, 0, 0, "Int", "Data byte 3 to store", false),
                // Byte 9 Data byte 4 to store
                new Property("", 9, 1, 0, 0, "Int", "Data byte 4 to store", false),
                // Byte 10 Data byte 5 to store
                new Property("", 10, 1, 0, 0, "Int", "Data byte 5 to store", false),
                // Byte 11 Data byte 6 to store
                new Property("", 11, 1, 0, 0, "Int", "Data byte 6 to store", false),
                // Byte 12 Data byte 7 to store
                new Property("", 12, 1, 0, 0, "Int", "Data byte 7 to store", false),
                // Byte 13 Data byte 8 to store
                new Property("", 13, 1, 0, 0, "Int", "Data byte 8 to store", false)),

        User_Information_Request_with_PIN(0x32, new int[] { 0x12, 0x1c, 0x1f }, 5, "User Information Request with PIN",
                "This message will contain a user number for which information is being requested and a PIN that will be checked for Master capability before proceeding. The information will be returned in a User Information Reply message (12h).",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                // Byte 2 (Master) PIN digits 1 & 2
                // Bits 0-3 PIN digit 1
                new Property("", 2, 1, 0, 4, "Bit", "PIN digit 1", false),
                // Bits 4-7 PIN digit 2
                new Property("", 2, 1, 4, 4, "Bit", "PIN digit 2", false),
                // Byte 3 (Master) PIN digits 3 & 4
                // Bits 0-3 PIN digit 3
                new Property("", 3, 1, 0, 4, "Bit", "PIN digit 3", false),
                // Bits 4-7 PIN digit 4
                new Property("", 3, 1, 4, 4, "Bit", "PIN digit 4", false),
                // Byte 4 (Master) PIN digits 5 & 6
                // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
                new Property("", 4, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
                // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
                new Property("", 4, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),
                // Byte 5 User number (1=user 1)
                new Property("", 5, 1, 0, 0, "Int", "User number (1=user 1)", false)),

        User_Information_Request_without_PIN(0x33, new int[] { 0x12, 0x1c, 0x1f }, 2,
                "User Information Request without PIN",
                "This message will contain a user number for which information is being requested, no authentication will be performed. The information will be returned in a User Information Reply message (12h).",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                // Byte 2 User number (1=user 1)
                new Property("", 2, 1, 0, 0, "Int", "User number (1=user 1)", false)),

        Set_User_Code_Command_with_PIN(0x34, new int[] { 0x12, 0x1c, 0x1f }, 8, "Set User Code Command with PIN",
                "This message will contain all digits that should be stored as the new code for the designated User number. A PIN will be checked for Master capability before proceeding. A successful programming of the user code will result in the User Information Reply (12h) returned in place of the acknowledge.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                // Byte 2 (Master) PIN digits 1 & 2
                // Bits 0-3 PIN digit 1
                new Property("", 2, 1, 0, 4, "Bit", "PIN digit 1", false),
                // Bits 4-7 PIN digit 2
                new Property("", 2, 1, 4, 4, "Bit", "PIN digit 2", false),
                // Byte 3 (Master) PIN digits 3 & 4
                // Bits 0-3 PIN digit 3
                new Property("", 3, 1, 0, 4, "Bit", "PIN digit 3", false),
                // Bits 4-7 PIN digit 4
                new Property("", 3, 1, 4, 4, "Bit", "PIN digit 4", false),
                // Byte 4 (Master) PIN digits 5 & 6
                // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
                new Property("", 4, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
                // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
                new Property("", 4, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),
                // Byte 5 User number (1=user 1)
                new Property("", 5, 1, 0, 0, "Int", "User number (1=user 1)", false),
                // Byte 6 PIN digits 1 & 2
                // Bits 0-3 PIN digit 1
                new Property("", 6, 1, 0, 4, "Bit", "PIN digit 1", false),
                // Bits 4-7 PIN digit 2
                new Property("", 6, 1, 4, 4, "Bit", "PIN digit 2", false),
                // Byte 7 PIN digits 3 & 4
                // Bits 0-3 PIN digit 3
                new Property("", 7, 1, 0, 4, "Bit", "PIN digit 3", false),
                // Bits 4-7 PIN digit 4
                new Property("", 7, 1, 4, 4, "Bit", "PIN digit 4", false),
                // Byte 8 PIN digits 5 & 6
                // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
                new Property("", 8, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
                // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
                new Property("", 8, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false)),

        Set_User_Code_Command_without_PIN(0x35, new int[] { 0x12, 0x1c, 0x1f }, 5, "Set User Code Command without PIN",
                "This message will contain all digits that should be stored as the new code for the designated User number. No authentication will be performed. A successful programming of the user code will result in the User Information Reply (12h) returned in place of the acknowledge.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),
                // Byte 2 User number (1=user 1)
                new Property("", 2, 1, 0, 0, "Int", "User number (1=user 1)", false),
                // Byte 3 PIN digits 1 & 2
                // Bits 0-3 PIN digit 1
                new Property("", 3, 1, 0, 4, "Bit", "PIN digit 1", false),
                // Bits 4-7 PIN digit 2
                new Property("", 3, 1, 4, 4, "Bit", "PIN digit 2", false),
                // Byte 4 PIN digits 3 & 4
                // Bits 0-3 PIN digit 3
                new Property("", 4, 1, 0, 4, "Bit", "PIN digit 3", false),
                // Bits 4-7 PIN digit 4
                new Property("", 4, 1, 4, 4, "Bit", "PIN digit 4", false),
                // Byte 5 PIN digits 5 & 6
                // Bits 0-3 PIN digit 5 (pad with 0 if 4 digit PIN)
                new Property("", 5, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
                // Bits 4-7 PIN digit 6 (pad with 0 if 4 digit PIN)
                new Property("", 5, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false)),

        Set_User_Authorization_Command_with_PIN(0x36, new int[] { 0x1d, 0x1c, 0x1f }, 7,
                "Set User Authorization Command with PIN",
                "This message will contain all attributes and partitions that should be stored as the new information for the designated User number. A PIN will be checked for Master capability before proceeding.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2 (Master) PIN digits 1 & 2
                new Property("", 2, 1, 0, 4, "Bit", "PIN digit 1", false),
                new Property("", 2, 1, 4, 4, "Bit", "PIN digit 2", false),

                // Byte 3 (Master) PIN digits 3 & 4
                new Property("", 3, 1, 0, 4, "Bit", "PIN digit 3", false),
                new Property("", 3, 1, 4, 4, "Bit", "PIN digit 4", false),

                // Byte 4 (Master) PIN digits 5 & 6
                new Property("", 4, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
                new Property("", 4, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),

                // Byte 5 User number (1=user 1)
                new Property("", 5, 1, 0, 0, "Int", "User number (1=user 1)", false),

                // Byte 6 Authority flags
                new Property("", 6, 1, 0, 1, "Bit", "Reserved (if bit 7 is clear) || Output 1 enable (if bit 7 is set)",
                        false),
                new Property("", 6, 1, 1, 1, "Bit", "Arm only (if bit 7 is clear) || Output 2 enable (if bit 7 is set)",
                        false),
                new Property("", 6, 1, 2, 1, "Bit",
                        "Arm only (during close window) (if bit 7 is clear) || Output 3 enable (if bit 7 is set)",
                        false),
                new Property("", 6, 1, 3, 1, "Bit",
                        "Master / program (if bit 7 is clear) || Output 4 enable (if bit 7 is set)", false),
                new Property("", 6, 1, 4, 1, "Bit",
                        "Arm / Disarm (if bit 7 is clear) || Arm / Disarm (if bit 7 is set)", false),
                new Property("", 6, 1, 5, 1, "Bit",
                        "Bypass enable (if bit 7 is clear) || Bypass enable (if bit 7 is set)", false),
                new Property("", 6, 1, 6, 1, "Bit",
                        "Open / close report enable (if bit 7 is clear) || Open / close report enable (if bit 7 is set)",
                        false),
                new Property("", 6, 1, 7, 1, "Bit", "Must be a 0 (if bit 7 is clear) || Must be a 1 (if bit 7 is set)",
                        false),

                // Byte 7 Authorized partition(s) mask
                new Property("", 7, 1, 0, 1, "Bit", "Authorized for partition 1", false),
                new Property("", 7, 1, 1, 1, "Bit", "Authorized for partition 2", false),
                new Property("", 7, 1, 2, 1, "Bit", "Authorized for partition 3", false),
                new Property("", 7, 1, 3, 1, "Bit", "Authorized for partition 4", false),
                new Property("", 7, 1, 4, 1, "Bit", "Authorized for partition 5", false),
                new Property("", 7, 1, 5, 1, "Bit", "Authorized for partition 6", false),
                new Property("", 7, 1, 6, 1, "Bit", "Authorized for partition 7", false),
                new Property("", 7, 1, 7, 1, "Bit", "Authorized for partition 8", false)),

        Set_User_Authorization_Command_without_PIN(0x37, new int[] { 0x1d, 0x1c, 0x1f }, 4,
                "Set User Authorization Command without PIN",
                "This message will contain all attributes and partitions that should be stored as the new information for the designated User number. No authentication will be performed.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2 User number (1=user 1)
                new Property("", 2, 1, 0, 0, "Int", "User number (1=user 1)", false),

                // Byte 3 Authority flags
                new Property("", 3, 1, 0, 1, "Bit", "Reserved (if bit 7 is clear) || Output 1 enable (if bit 7 is set)",
                        false),
                new Property("", 3, 1, 1, 1, "Bit", "Arm only (if bit 7 is clear) || Output 2 enable (if bit 7 is set)",
                        false),
                new Property("", 3, 1, 2, 1, "Bit",
                        "Arm only (during close window) (if bit 7 is clear) || Output 3 enable (if bit 7 is set)",
                        false),
                new Property("", 3, 1, 3, 1, "Bit",
                        "Master / program (if bit 7 is clear) || Output 4 enable (if bit 7 is set)", false),
                new Property("", 3, 1, 4, 1, "Bit",
                        "Arm / Disarm (if bit 7 is clear) || Arm / Disarm (if bit 7 is set)", false),
                new Property("", 3, 1, 5, 1, "Bit",
                        "Bypass enable (if bit 7 is clear) || Bypass enable (if bit 7 is set)", false),
                new Property("", 3, 1, 6, 1, "Bit",
                        "Open / close report enable (if bit 7 is clear) || Open / close report enable (if bit 7 is set)",
                        false),
                new Property("", 3, 1, 7, 1, "Bit", "Must be a 0 (if bit 7 is clear) || Must be a 1 (if bit 7 is set)",
                        false),

                // Byte 4 Authorized partition(s) mask
                new Property("", 4, 1, 0, 1, "Bit", "Authorized for partition 1", false),
                new Property("", 4, 1, 1, 1, "Bit", "Authorized for partition 2", false),
                new Property("", 4, 1, 2, 1, "Bit", "Authorized for partition 3", false),
                new Property("", 4, 1, 3, 1, "Bit", "Authorized for partition 4", false),
                new Property("", 4, 1, 4, 1, "Bit", "Authorized for partition 5", false),
                new Property("", 4, 1, 5, 1, "Bit", "Authorized for partition 6", false),
                new Property("", 4, 1, 6, 1, "Bit", "Authorized for partition 7", false),
                new Property("", 4, 1, 7, 1, "Bit", "Authorized for partition 8", false)),

        Store_Communication_Event_Command(0x3a, new int[] { 0x1d, 0x1c, 0x1f }, 6, "Store Communication Event Command",
                "This message will submit an event to the control’s communication stack for possible transmission over its telephone or alternate communications path.",
                Direction.Out, Source.None),

        Set_Clock_Calendar_Command(0x3b, new int[] { 0x1d, 0x1c, 0x1f }, 7, "Set Clock / Calendar Command",
                "This message will set the clock / calendar in the system.", Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2 Year (00-99)
                new Property("", 2, 1, 0, 0, "Int", "Year (00-99)", false),

                // Byte 3 Month (1-12)
                new Property("", 3, 1, 0, 0, "Int", "Month (1-12)", false),

                // Byte 4 Day (1-31)
                new Property("", 4, 1, 0, 0, "Int", "Day (1-31)", false),

                // Byte 5 Hour (0-23)
                new Property("", 5, 1, 0, 0, "Int", "Hour (0-23)", false),

                // Byte 6 Minute (0-59)
                new Property("", 6, 1, 0, 0, "Int", "Minute (0-59)", false),

                // Byte 7 Day
                new Property("", 7, 1, 0, 0, "Int", "Day", false)),

        Primary_Keypad_Function_with_PIN(0x3c, new int[] { 0x1d, 0x1c, 0x1f }, 6, "Primary Keypad Function with PIN",
                "This message will contain a value that defines with function to perform, the partitions to use and a PIN value for the validation.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2 PIN digits 1 & 2
                new Property("", 2, 1, 0, 4, "Bit", "PIN digit 1", false),
                new Property("", 2, 1, 4, 4, "Bit", "PIN digit 2", false),

                // Byte 3 PIN digits 3 & 4
                new Property("", 3, 1, 0, 4, "Bit", "PIN digit 3", false),
                new Property("", 3, 1, 4, 4, "Bit", "PIN digit 4", false),

                // Byte 4 PIN digits 5 & 6
                new Property("", 4, 1, 0, 4, "Bit", "PIN digit 5 (pad with 0 if 4 digit PIN)", false),
                new Property("", 4, 1, 4, 4, "Bit", "PIN digit 6 (pad with 0 if 4 digit PIN)", false),

                // Byte 5 Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm
                // in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode,
                // 08h-FFh Reserved]
                new Property("", 5, 1, 0, 0, "Int",
                        "Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode, 08h-FFh Reserved]",
                        false),

                // Byte 6 Partition mask
                new Property("", 6, 1, 0, 1, "Bit", "Perform on partition 1 (if PIN has access)", false),
                new Property("", 6, 1, 1, 1, "Bit", "Perform on partition 2 (if PIN has access)", false),
                new Property("", 6, 1, 2, 1, "Bit", "Perform on partition 3 (if PIN has access)", false),
                new Property("", 6, 1, 3, 1, "Bit", "Perform on partition 4 (if PIN has access)", false),
                new Property("", 6, 1, 4, 1, "Bit", "Perform on partition 5 (if PIN has access)", false),
                new Property("", 6, 1, 5, 1, "Bit", "Perform on partition 6 (if PIN has access)", false),
                new Property("", 6, 1, 6, 1, "Bit", "Perform on partition 7 (if PIN has access)", false),
                new Property("", 6, 1, 7, 1, "Bit", "Perform on partition 8 (if PIN has access)", false)),

        Primary_Keypad_Function_without_PIN(0x3d, new int[] { 0x1d, 0x1c, 0x1f }, 4,
                "Primary Keypad Function without PIN",
                "This message will contain a value that defines with function to perform, the partitions and user number to assign to the function.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2 "Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm
                // in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode,
                // 08h-FFh Reserved]",
                new Property("", 2, 1, 0, 0, "Int",
                        "Keypad function [00h Turn off any sounder or alarm, 01h Disarm, 02h Arm in away mode, 03h Arm in stay mode, 04h Cancel, 05h Initiate auto-arm, 06h Start walk-test mode, 07h Stop walk-test mode, 08h-FFh Reserved]",
                        false),

                // Byte 3 Partition mask
                new Property("", 3, 1, 0, 1, "Bit", "Perform on partition 1 (if PIN has access)", false),
                new Property("", 3, 1, 1, 1, "Bit", "Perform on partition 2 (if PIN has access)", false),
                new Property("", 3, 1, 2, 1, "Bit", "Perform on partition 3 (if PIN has access)", false),
                new Property("", 3, 1, 3, 1, "Bit", "Perform on partition 4 (if PIN has access)", false),
                new Property("", 3, 1, 4, 1, "Bit", "Perform on partition 5 (if PIN has access)", false),
                new Property("", 3, 1, 5, 1, "Bit", "Perform on partition 6 (if PIN has access)", false),
                new Property("", 3, 1, 6, 1, "Bit", "Perform on partition 7 (if PIN has access)", false),
                new Property("", 3, 1, 7, 1, "Bit", "Perform on partition 8 (if PIN has access)", false),

                // Byte 4 User number
                new Property("", 1, 1, 0, 0, "Int", "User number", false)),

        Secondary_Keypad_Function(0x3e, new int[] { 0x1d, 0x1c, 0x1f }, 4, "Secondary Keypad Function",
                "This message will contain a value that defines with function to perform, and the partitions to use.",
                Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2 "Keypad function [00h Stay (1 button arm / toggle interiors), 01h Chime (toggle chime mode),
                // 02h Exit (1 button arm / toggle instant), 03h Bypass interiors, 04h Fire panic, 05h Medical panic,
                // 06h Police panic, 07h Smoke detector reset, 08h Auto callback download, 09h Manual pickup download,
                // 0Ah Enable silent exit (for this arm cycle), 0Bh Perform test, 0Ch Group bypass, 0Dh Auxiliary
                // function 1, 0Eh Auxiliary function 2, 0Fh Start keypad sounder, 10h-FFh Reserved]",
                new Property("", 2, 1, 0, 0, "Int",
                        "Keypad function [00h Stay (1 button arm / toggle interiors), 01h Chime (toggle chime mode), 02h Exit (1 button arm / toggle instant), 03h Bypass interiors, 04h Fire panic, 05h Medical panic, 06h Police panic, 07h Smoke detector reset, 08h Auto callback download, 09h Manual pickup download, 0Ah Enable silent exit (for this arm cycle), 0Bh Perform test, 0Ch Group bypass, 0Dh Auxiliary function 1, 0Eh Auxiliary function 2, 0Fh Start keypad sounder, 10h-FFh Reserved]",
                        false),

                // Byte 3 Partition mask
                new Property("", 3, 1, 0, 1, "Bit", "Perform on partition 1", false),
                new Property("", 3, 1, 1, 1, "Bit", "Perform on partition 2", false),
                new Property("", 3, 1, 2, 1, "Bit", "Perform on partition 3", false),
                new Property("", 3, 1, 3, 1, "Bit", "Perform on partition 4", false),
                new Property("", 3, 1, 4, 1, "Bit", "Perform on partition 5", false),
                new Property("", 3, 1, 5, 1, "Bit", "Perform on partition 6", false),
                new Property("", 3, 1, 6, 1, "Bit", "Perform on partition 7", false),
                new Property("", 3, 1, 7, 1, "Bit", "Perform on partition 8", false)),

        Zone_Bypass_Toggle(0x3f, new int[] { 0x1d, 0x1c, 0x1f }, 2, "Zone Bypass Toggle",
                "This message will contain a number of a zone that should be (un)bypassed.", Direction.Out, Source.None,

                // Properties
                // Byte 1 Message number
                new Property("", 1, 1, 0, 0, "Int", "Message number", false),

                // Byte 2 Zone number (0= zone 1)
                new Property("", 2, 1, 0, 0, "Int", "Zone number (0= zone 1)", false));

        public final String name;
        public final String description;
        public final int number;
        public final int @Nullable [] replyMessageNumbers;
        public final int length;
        public final Direction direction;
        public final Source source;
        public final Property[] properties;

        CaddxMessageType(int number, int @Nullable [] replyMessageNumbers, int length, String name, String description,
                Direction direction, Source source, Property... properties) {
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
    };

    /**
     * Constructor.
     *
     * @param message
     *            - the message received
     */
    public CaddxMessage(byte[] message, boolean withChecksum) {
        if (withChecksum && message.length < 3) {
            throw new IllegalArgumentException("The message should be at least 3 bytes long");
        }
        if (!withChecksum && message.length < 1) {
            throw new IllegalArgumentException("The message should be at least 1 byte long");
        }

        // Received data
        byte[] msg = message;
        if (withChecksum) {
            checksum1In = message[message.length - 2];
            checksum2In = message[message.length - 1];
            msg = Arrays.copyOf(message, message.length - 2);
        }

        // Calculate the checksum
        byte[] fletcherSum = fletcher(msg);
        checksum1Calc = fletcherSum[0];
        checksum2Calc = fletcherSum[1];
        // Make the In checksum same as the Calculated in case it is not supplied
        if (!withChecksum) {
            checksum1In = checksum1Calc;
            checksum2In = checksum2Calc;
        }

        this.message = msg;

        // fill the message type
        caddxMessageType = CaddxMessageType.valueOfMessageType((message[0] & 0x7f));

        // Fill-in the properties
        processCaddxMessage();
    }

    /**
     * Builds a Caddx message for a zone bypass toggle command
     *
     * @param data The zone number
     * @return The Caddx message object
     */
    public static CaddxMessage buildZoneBypassToggle(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x3f;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a zone status request command
     *
     * @param data The zone number
     * @return The Caddx message object
     */
    public static CaddxMessage buildZoneStatusRequest(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x24;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a zone name request command
     *
     * @param data The zone number
     * @return The Caddx message object
     */
    public static CaddxMessage buildZoneNameRequest(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x23;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a partition status request command
     *
     * @param data The partition number
     * @return The Caddx message object
     */
    public static CaddxMessage buildPartitionStatusRequest(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x26;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a partition snapshot request command
     *
     * @param data The partition number
     * @return The Caddx message object
     */
    public static CaddxMessage buildPartitionSnapshotRequest(String data) {
        byte[] arr = new byte[1];
        arr[0] = 0x27;

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a partition primary command
     *
     * @param data Two values comma separated. The command, The partition number. e.g. "1,0"
     * @return The Caddx message object
     */
    public static CaddxMessage buildPartitionPrimaryCommand(String data) {
        String[] tokens = data.split(",");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("buildPartitionPrimaryCommand(): data has not the correct format.");
        }

        byte[] arr = new byte[3];
        arr[0] = 0x3e;
        arr[1] = (byte) Integer.parseInt(tokens[0]);
        arr[2] = (byte) (1 << Integer.parseInt(tokens[1]));

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a partition secondary command
     *
     * @param data Two values comma separated. The command, The partition number. e.g. "1,0"
     * @return The Caddx message object
     */
    public static CaddxMessage buildPartitionSecondaryCommand(String data) {
        String[] tokens = data.split(",");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("buildPartitionSecondaryCommand(): data has not the correct format.");
        }

        byte[] arr = new byte[3];
        arr[0] = 0x3e;
        arr[1] = (byte) Integer.parseInt(tokens[0]);
        arr[2] = (byte) (1 << Integer.parseInt(tokens[1]));

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a system status request command
     *
     * @param data Should be passed empty
     * @return The Caddx message object
     */
    public static CaddxMessage buildSystemStatusRequest(String data) {
        byte[] arr = new byte[1];
        arr[0] = 0x28;

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a interface configuration request command
     *
     * @param data Should be passed empty
     * @return The Caddx message object
     */
    public static CaddxMessage buildInterfaceConfigurationRequest(String data) {
        byte[] arr = new byte[1];
        arr[0] = 0x21;

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a log event request command
     *
     * @param data Should be the number of the event
     * @return The Caddx message object
     */
    public static CaddxMessage buildLogEventRequest(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x2a;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Returns the Caddx Message Type.
     *
     * @return messageType
     */
    public CaddxMessageType getCaddxMessageType() {
        return caddxMessageType;
    }

    public byte getMessageType() {
        return message[0];
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append(caddxMessageType.name);
        switch (caddxMessageType) {
            case Zone_Status_Request:
            case Zone_Status_Message:
                sb.append(" [Zone: ");
                sb.append(getPropertyById("zone_number"));
                sb.append("]");
                break;
            case Log_Event_Request:
            case Log_Event_Message:
                sb.append(" [Event: ");
                sb.append(getPropertyById("panel_log_event_number"));
                sb.append("]");
                break;
            case Partition_Status_Request:
            case Partition_Status_Message:
                sb.append(" [Partition: ");
                sb.append(getPropertyById("partition_number"));
                sb.append("]");
                break;
            default:
                break;
        }
        return sb.toString();
    }

    public String getPropertyValue(String property) {
        return propertyMap.get(property);
    }

    public String getPropertyById(String id) {
        return idMap.get(id);
    }

    public int @Nullable [] getReplyMessageNumbers() {
        return caddxMessageType.replyMessageNumbers;
    }

    public Source getSource() {
        return getCaddxMessageType().source;
    }

    public boolean isChecksumCorrect() {
        return checksum1In == checksum1Calc && checksum2In == checksum2Calc;
    }

    public boolean isLengthCorrect() {
        return message.length == caddxMessageType.length;
    }

    public boolean hasAcknowledgementFlag() {
        return hasAcknowledgementFlag;
    }

    public byte[] getMessageFrameBytes(CaddxProtocol protocol) {
        if (protocol == CaddxProtocol.Binary) {
            return getMessageFrameBytesInBinary();
        } else {
            return getMessageFrameBytesInAscii();
        }
    }

    private byte[] getMessageFrameBytesInBinary() {
        // Calculate bytes
        // 1 for the startbyte
        // 1 for the length
        // 2 for the checksum
        // n for the count of 0x7d and 0x7e occurrences in the message and checksum
        int additional = 4;
        for (int i = 0; i < message.length; i++) {
            if (message[i] == 0x7d || message[i] == 0x7e) {
                additional++;
            }
        }
        if (checksum1Calc == 0x7d || checksum1Calc == 0x7e) {
            additional++;
        }
        if (checksum2Calc == 0x7d || checksum2Calc == 0x7e) {
            additional++;
        }

        byte[] frame = new byte[message.length + additional];
        frame[0] = 0x7e;
        frame[1] = (byte) message.length;

        int fi = 2;
        for (int i = 0; i < message.length; i++) {
            byte b = message[i];
            if (b == 0x7e) {
                frame[fi++] = 0x7d;
                b = 0x5e;
            } else if (b == 0x7d) {
                frame[fi++] = 0x7d;
                b = 0x5d;
            }
            frame[fi++] = b;
        }

        // 1st checksum byte
        if (checksum1Calc == 0x7e) {
            frame[fi++] = 0x7d;
            frame[fi++] = 0x5e;
        } else if (checksum1Calc == 0x7d) {
            frame[fi++] = 0x7d;
            frame[fi++] = 0x5d;
        } else {
            frame[fi++] = checksum1Calc;
        }
        // 2nd checksum byte
        if (checksum2Calc == 0x7e) {
            frame[fi++] = 0x7d;
            frame[fi++] = 0x5e;
        } else if (checksum2Calc == 0x7d) {
            frame[fi++] = 0x7d;
            frame[fi++] = 0x5d;
        } else {
            frame[fi++] = checksum2Calc;
        }

        return frame;
    }

    private byte[] getMessageFrameBytesInAscii() {
        // Calculate additional bytes
        // 1 for the start byte
        // 2 for the length
        // 4 for the checksum
        // 1 for the stop byte
        int additional = 8;

        int fi = 0;
        byte[] frame = new byte[2 * message.length + additional];

        // start character
        frame[fi++] = 0x0a;

        // message length
        String tempString = Util.byteToHex((byte) message.length);
        frame[fi++] = (byte) tempString.charAt(0);
        frame[fi++] = (byte) tempString.charAt(1);

        // message
        for (int i = 0; i < message.length; i++) {
            byte b = message[i];
            tempString = Util.byteToHex(b);
            frame[fi++] = (byte) tempString.charAt(0);
            frame[fi++] = (byte) tempString.charAt(1);
        }

        // Checksum 1st byte
        tempString = Util.byteToHex(checksum1Calc);
        frame[fi++] = (byte) tempString.charAt(0);
        frame[fi++] = (byte) tempString.charAt(1);

        // Checksum 2nd byte
        tempString = Util.byteToHex(checksum2Calc);
        frame[fi++] = (byte) tempString.charAt(0);
        frame[fi++] = (byte) tempString.charAt(1);

        // Stop character
        frame[fi++] = (byte) 0x0d;

        return frame;
    }

    public byte[] getMessageBytes() {
        return message;
    }

    /**
     * Returns a string representation of a CaddxMessage.
     *
     * @return CaddxMessage string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        CaddxMessageType mt = CaddxMessageType.valueOfMessageType(message[0]);

        sb.append("Message: ");
        sb.append(String.format("%2s", Integer.toHexString(message[0])));
        sb.append(" ");
        sb.append(mt.name);
        sb.append("\r\n");

        for (CaddxMessage.Property p : mt.properties) {
            sb.append("\t" + p.toString(message));
            sb.append("\r\n");
        }

        return sb.toString();
    }

    private final Map<String, String> propertyMap = new HashMap<>();
    private final Map<String, String> idMap = new HashMap<>();

    /**
     * Processes the incoming Caddx message and extracts the information.
     */
    private void processCaddxMessage() {
        if ((message[0] & 0x80) != 0) {
            hasAcknowledgementFlag = true;
            message[0] = (byte) (message[0] & 0x7f);
        }

        // fill the property lookup hashmaps
        for (CaddxMessage.Property p : caddxMessageType.properties) {
            propertyMap.put(p.name, p.getValue(message));
        }
        for (CaddxMessage.Property p : caddxMessageType.properties) {
            if (!"".equals(p.id)) {
                idMap.put(p.id, p.getValue(message));
            }
        }
    }

    /**
     * Calculates the Fletcher checksum of the byte array.
     *
     * @param data The input byte array
     * @return Byte array with two elements. Checksum1 and Checksum2
     */
    private byte[] fletcher(byte data[]) {
        int len = data.length;
        int sum1 = len, sum2 = len;
        for (int i = 0; i < len; i++) {
            int d = data[i] & 0xff;
            if (0xff - sum1 < d) {
                sum1 = (sum1 + 1) & 0xff;
            }
            sum1 = (sum1 + d) & 0xff;
            if (sum1 == 0xff) {
                sum1 = 0;
            }
            if (0xff - sum2 < sum1) {
                sum2 = (sum2 + 1) & 0xff;
            }
            sum2 = (sum2 + sum1) & 0xff;
            if (sum2 == 0xff) {
                sum2 = 0;
            }
        }

        return new byte[] { (byte) sum1, (byte) sum2 };
    }
}
