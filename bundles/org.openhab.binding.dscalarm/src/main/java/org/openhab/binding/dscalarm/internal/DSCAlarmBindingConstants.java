/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dscalarm.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The DSCAlarmBinding class defines common constants, which are used across the whole binding.
 *
 * @author Russell Stephens - Initial contribution
 */
@NonNullByDefault
public class DSCAlarmBindingConstants {

    // Binding ID
    public static final String BINDING_ID = "dscalarm";

    // List of bridge device types
    public static final String ENVISALINK_BRIDGE = "envisalink";
    public static final String IT100_BRIDGE = "it100";
    public static final String TCPSERVER_BRIDGE = "tcpserver";

    // List of DSC Alarm device types
    public static final String PANEL = "panel";
    public static final String PARTITION = "partition";
    public static final String ZONE = "zone";
    public static final String KEYPAD = "keypad";

    // List of all Bridge Thing Type UIDs
    public static final ThingTypeUID ENVISALINKBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, ENVISALINK_BRIDGE);
    public static final ThingTypeUID IT100BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, IT100_BRIDGE);
    public static final ThingTypeUID TCPSERVERBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, TCPSERVER_BRIDGE);

    // List of all DSC Alarm Thing Type UIDs
    public static final ThingTypeUID PANEL_THING_TYPE = new ThingTypeUID(BINDING_ID, PANEL);
    public static final ThingTypeUID PARTITION_THING_TYPE = new ThingTypeUID(BINDING_ID, PARTITION);
    public static final ThingTypeUID ZONE_THING_TYPE = new ThingTypeUID(BINDING_ID, ZONE);
    public static final ThingTypeUID KEYPAD_THING_TYPE = new ThingTypeUID(BINDING_ID, KEYPAD);

    // List of all Channel IDs
    public static final String BRIDGE_RESET = "bridge_reset";
    public static final String SEND_COMMAND = "send_command";

    public static final String PANEL_MESSAGE = "panel_message";
    public static final String PANEL_COMMAND = "panel_command";
    public static final String PANEL_SYSTEM_ERROR = "panel_system_error";

    public static final String PANEL_TROUBLE_MESSAGE = "panel_trouble_message";
    public static final String PANEL_TROUBLE_LED = "panel_trouble_led";
    public static final String PANEL_SERVICE_REQUIRED = "panel_service_required";
    public static final String PANEL_AC_TROUBLE = "panel_ac_trouble";
    public static final String PANEL_TELEPHONE_TROUBLE = "panel_telephone_trouble";
    public static final String PANEL_FTC_TROUBLE = "panel_ftc_trouble";
    public static final String PANEL_ZONE_FAULT = "panel_zone_fault";
    public static final String PANEL_ZONE_TAMPER = "panel_zone_tamper";
    public static final String PANEL_ZONE_LOW_BATTERY = "panel_zone_low_battery";
    public static final String PANEL_TIME_LOSS = "panel_time_loss";

    public static final String PANEL_TIME = "panel_time";
    public static final String PANEL_TIME_STAMP = "panel_time_stamp";
    public static final String PANEL_TIME_BROADCAST = "panel_time_broadcast";
    public static final String PANEL_FIRE_KEY_ALARM = "panel_fire_key_alarm";
    public static final String PANEL_PANIC_KEY_ALARM = "panel_panic_key_alarm";
    public static final String PANEL_AUX_KEY_ALARM = "panel_aux_key_alarm";
    public static final String PANEL_AUX_INPUT_ALARM = "panel_aux_input_alarm";

    public static final String PARTITION_STATUS = "partition_status";
    public static final String PARTITION_ARM_MODE = "partition_arm_mode";
    public static final String PARTITION_ARMED = "partition_armed";
    public static final String PARTITION_ENTRY_DELAY = "partition_entry_delay";
    public static final String PARTITION_EXIT_DELAY = "partition_exit_delay";
    public static final String PARTITION_IN_ALARM = "partition_in_alarm";
    public static final String PARTITION_OPENING_CLOSING_MODE = "partition_opening_closing_mode";

    public static final String ZONE_STATUS = "zone_status";
    public static final String ZONE_MESSAGE = "zone_message";
    public static final String ZONE_BYPASS_MODE = "zone_bypass_mode";
    public static final String ZONE_IN_ALARM = "zone_in_alarm";
    public static final String ZONE_TAMPER = "zone_tamper";
    public static final String ZONE_FAULT = "zone_fault";
    public static final String ZONE_TRIPPED = "zone_tripped";

    public static final String KEYPAD_READY_LED = "keypad_ready_led";
    public static final String KEYPAD_ARMED_LED = "keypad_armed_led";
    public static final String KEYPAD_MEMORY_LED = "keypad_memory_led";
    public static final String KEYPAD_BYPASS_LED = "keypad_bypass_led";
    public static final String KEYPAD_TROUBLE_LED = "keypad_trouble_led";
    public static final String KEYPAD_PROGRAM_LED = "keypad_program_led";
    public static final String KEYPAD_FIRE_LED = "keypad_fire_led";
    public static final String KEYPAD_BACKLIGHT_LED = "keypad_backlight_led";
    public static final String KEYPAD_AC_LED = "keypad_ac_led";
    public static final String KEYPAD_LCD_UPDATE = "keypad_lcd_update";
    public static final String KEYPAD_LCD_CURSOR = "keypad_lcd_cursor";

    // Set of all supported Thing Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream
                    .of(ENVISALINKBRIDGE_THING_TYPE, IT100BRIDGE_THING_TYPE, TCPSERVERBRIDGE_THING_TYPE,
                            PANEL_THING_TYPE, PARTITION_THING_TYPE, ZONE_THING_TYPE, KEYPAD_THING_TYPE)
                    .collect(Collectors.toSet()));

    // Set of all supported Bridge Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(ENVISALINKBRIDGE_THING_TYPE, IT100BRIDGE_THING_TYPE, TCPSERVERBRIDGE_THING_TYPE)
                    .collect(Collectors.toSet()));
}
