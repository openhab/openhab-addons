/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The DSCAlarmBinding class defines common constants, which are used across the whole binding.
 *
 * @author Russell Stephens - Initial contribution
 */
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
    public final static ThingTypeUID ENVISALINKBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, ENVISALINK_BRIDGE);
    public final static ThingTypeUID IT100BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, IT100_BRIDGE);
    public final static ThingTypeUID TCPSERVERBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, TCPSERVER_BRIDGE);

    // List of all DSC Alarm Thing Type UIDs
    public final static ThingTypeUID PANEL_THING_TYPE = new ThingTypeUID(BINDING_ID, PANEL);
    public final static ThingTypeUID PARTITION_THING_TYPE = new ThingTypeUID(BINDING_ID, PARTITION);
    public final static ThingTypeUID ZONE_THING_TYPE = new ThingTypeUID(BINDING_ID, ZONE);
    public final static ThingTypeUID KEYPAD_THING_TYPE = new ThingTypeUID(BINDING_ID, KEYPAD);

    // List of all Channel IDs
    public final static String BRIDGE_RESET = "bridge_reset";

    public final static String PANEL_MESSAGE = "panel_message";
    public final static String PANEL_COMMAND = "panel_command";
    public final static String PANEL_SYSTEM_ERROR = "panel_system_error";

    public final static String PANEL_TROUBLE_MESSAGE = "panel_trouble_message";
    public final static String PANEL_TROUBLE_LED = "panel_trouble_led";
    public final static String PANEL_SERVICE_REQUIRED = "panel_service_required";
    public final static String PANEL_AC_TROUBLE = "panel_ac_trouble";
    public final static String PANEL_TELEPHONE_TROUBLE = "panel_telephone_trouble";
    public final static String PANEL_FTC_TROUBLE = "panel_ftc_trouble";
    public final static String PANEL_ZONE_FAULT = "panel_zone_fault";
    public final static String PANEL_ZONE_TAMPER = "panel_zone_tamper";
    public final static String PANEL_ZONE_LOW_BATTERY = "panel_zone_low_battery";
    public final static String PANEL_TIME_LOSS = "panel_time_loss";

    public final static String PANEL_TIME = "panel_time";
    public final static String PANEL_TIME_STAMP = "panel_time_stamp";
    public final static String PANEL_TIME_BROADCAST = "panel_time_broadcast";
    public final static String PANEL_FIRE_KEY_ALARM = "panel_fire_key_alarm";
    public final static String PANEL_PANIC_KEY_ALARM = "panel_panic_key_alarm";
    public final static String PANEL_AUX_KEY_ALARM = "panel_aux_key_alarm";
    public final static String PANEL_AUX_INPUT_ALARM = "panel_aux_input_alarm";

    public final static String PARTITION_STATUS = "partition_status";
    public final static String PARTITION_ARM_MODE = "partition_arm_mode";
    public final static String PARTITION_ARMED = "partition_armed";
    public final static String PARTITION_ENTRY_DELAY = "partition_entry_delay";
    public final static String PARTITION_EXIT_DELAY = "partition_exit_delay";
    public final static String PARTITION_IN_ALARM = "partition_in_alarm";
    public final static String PARTITION_OPENING_CLOSING_MODE = "partition_opening_closing_mode";

    public final static String ZONE_STATUS = "zone_status";
    public final static String ZONE_MESSAGE = "zone_message";
    public final static String ZONE_BYPASS_MODE = "zone_bypass_mode";
    public final static String ZONE_IN_ALARM = "zone_in_alarm";
    public final static String ZONE_TAMPER = "zone_tamper";
    public final static String ZONE_FAULT = "zone_fault";
    public final static String ZONE_TRIPPED = "zone_tripped";

    public final static String KEYPAD_READY_LED = "keypad_ready_led";
    public final static String KEYPAD_ARMED_LED = "keypad_armed_led";
    public final static String KEYPAD_MEMORY_LED = "keypad_memory_led";
    public final static String KEYPAD_BYPASS_LED = "keypad_bypass_led";
    public final static String KEYPAD_TROUBLE_LED = "keypad_trouble_led";
    public final static String KEYPAD_PROGRAM_LED = "keypad_program_led";
    public final static String KEYPAD_FIRE_LED = "keypad_fire_led";
    public final static String KEYPAD_BACKLIGHT_LED = "keypad_backlight_led";
    public final static String KEYPAD_AC_LED = "keypad_ac_led";
    public final static String KEYPAD_LCD_UPDATE = "keypad_lcd_update";
    public final static String KEYPAD_LCD_CURSOR = "keypad_lcd_cursor";

    // Set of all supported Thing Type UIDs
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(ENVISALINKBRIDGE_THING_TYPE,
            IT100BRIDGE_THING_TYPE, TCPSERVERBRIDGE_THING_TYPE, PANEL_THING_TYPE, PARTITION_THING_TYPE, ZONE_THING_TYPE,
            KEYPAD_THING_TYPE);

    // Set of all supported Bridge Type UIDs
    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet
            .of(ENVISALINKBRIDGE_THING_TYPE, IT100BRIDGE_THING_TYPE, TCPSERVERBRIDGE_THING_TYPE);

}
