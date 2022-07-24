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
package org.openhab.binding.avmfritz.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This class defines common constants, which are used across the whole binding.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
 * @author Christoph Weitkamp - Added support for groups
 * @author Christoph Weitkamp - Added channels 'voltage' and 'battery_level'
 * @author Ulrich Mertin - Added support for HAN-FUN blinds
 */
@NonNullByDefault
public class AVMFritzBindingConstants {

    public static final String INVALID_PATTERN = "[^a-zA-Z0-9_]";

    public static final String BINDING_ID = "avmfritz";
    public static final String BRIDGE_FRITZBOX = "fritzbox";
    public static final String BOX_MODEL_NAME = "FRITZ!Box";
    public static final String POWERLINE546E_MODEL_NAME = "FRITZ!Powerline 546E";

    // List of main device types
    public static final String DEVICE_DECT500 = "FRITZ_DECT_500";
    public static final String DEVICE_DECT400 = "FRITZ_DECT_400";
    public static final String DEVICE_DECT440 = "FRITZ_DECT_440";
    public static final String DEVICE_DECT302 = "FRITZ_DECT_302";
    public static final String DEVICE_DECT301 = "FRITZ_DECT_301";
    public static final String DEVICE_DECT300 = "FRITZ_DECT_300";
    public static final String DEVICE_DECT210 = "FRITZ_DECT_210";
    public static final String DEVICE_DECT200 = "FRITZ_DECT_200";
    public static final String DEVICE_DECT100 = "FRITZ_DECT_Repeater_100";
    public static final String DEVICE_POWERLINE546E = "FRITZ_Powerline_546E";
    public static final String DEVICE_POWERLINE546E_STANDALONE = "FRITZ_Powerline_546E_Solo";
    public static final String DEVICE_COMETDECT = "Comet_DECT";
    public static final String DEVICE_HAN_FUN_CONTACT = "HAN_FUN_CONTACT";
    public static final String DEVICE_HAN_FUN_SWITCH = "HAN_FUN_SWITCH";
    public static final String DEVICE_HAN_FUN_ON_OFF = "HAN_FUN_ON_OFF";
    public static final String DEVICE_HAN_FUN_BLINDS = "HAN_FUN_BLINDS";
    public static final String DEVICE_HAN_FUN_COLOR_BULB = "HAN_FUN_COLOR_BULB";
    public static final String DEVICE_HAN_FUN_DIMMABLE_BULB = "HAN_FUN_DIMMABLE_BULB";

    // List of main group types
    public static final String GROUP_HEATING = "FRITZ_GROUP_HEATING";
    public static final String GROUP_SWITCH = "FRITZ_GROUP_SWITCH";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_FRITZBOX);
    public static final ThingTypeUID DECT500_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT500);
    public static final ThingTypeUID DECT400_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT400);
    public static final ThingTypeUID DECT440_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT440);
    public static final ThingTypeUID DECT302_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT302);
    public static final ThingTypeUID DECT301_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT301);
    public static final ThingTypeUID DECT300_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT300);
    public static final ThingTypeUID DECT210_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT210);
    public static final ThingTypeUID DECT200_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT200);
    public static final ThingTypeUID DECT100_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT100);
    public static final ThingTypeUID POWERLINE546E_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_POWERLINE546E);
    public static final ThingTypeUID POWERLINE546E_STANDALONE_THING_TYPE = new ThingTypeUID(BINDING_ID,
            DEVICE_POWERLINE546E_STANDALONE);
    public static final ThingTypeUID COMETDECT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_COMETDECT);
    public static final ThingTypeUID HAN_FUN_CONTACT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_HAN_FUN_CONTACT);
    public static final ThingTypeUID HAN_FUN_SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_HAN_FUN_SWITCH);
    public static final ThingTypeUID HAN_FUN_ON_OFF_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_HAN_FUN_ON_OFF);
    public static final ThingTypeUID HAN_FUN_BLINDS_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_HAN_FUN_BLINDS);
    public static final ThingTypeUID HAN_FUN_COLOR_BULB_THING_TYPE = new ThingTypeUID(BINDING_ID,
            DEVICE_HAN_FUN_COLOR_BULB);
    public static final ThingTypeUID HAN_FUN_DIMMABLE_BULB_THING_TYPE = new ThingTypeUID(BINDING_ID,
            DEVICE_HAN_FUN_DIMMABLE_BULB);
    public static final ThingTypeUID GROUP_HEATING_THING_TYPE = new ThingTypeUID(BINDING_ID, GROUP_HEATING);
    public static final ThingTypeUID GROUP_SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, GROUP_SWITCH);

    // List of all Thing config ids
    public static final String CONFIG_IP_ADDRESS = "ipAddress";
    public static final String CONFIG_PROTOCOL = "protocol";
    public static final String CONFIG_USER = "user";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_POLLING_INTERVAL = "pollingInterval";
    public static final String CONFIG_SYNC_TIMEOUT = "syncTimeout";
    public static final String CONFIG_AIN = "ain";

    // List of all Properties
    public static final String PROPERTY_MASTER = "master";
    public static final String PROPERTY_MEMBERS = "members";
    public static final String PRODUCT_NAME = "productName";

    // List of all channel groups
    public static final String CHANNEL_GROUP_DEVICE = "device";
    public static final String CHANNEL_GROUP_SENSORS = "sensors";
    public static final String CHANNEL_GROUP_TOP_LEFT = "top-left";
    public static final String CHANNEL_GROUP_BOTTOM_LEFT = "bottom-left";
    public static final String CHANNEL_GROUP_TOP_RIGHT = "top-right";
    public static final String CHANNEL_GROUP_BOTTOM_RIGHT = "bottom-right";

    // List of all Channel ids
    public static final String CHANNEL_CALL_INCOMING = "incoming_call";
    public static final String CHANNEL_CALL_OUTGOING = "outgoing_call";
    public static final String CHANNEL_CALL_ACTIVE = "active_call";
    public static final String CHANNEL_CALL_STATE = "call_state";

    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_LOCKED = "locked";
    public static final String CHANNEL_DEVICE_LOCKED = "device_locked";
    public static final String CHANNEL_APPLY_TEMPLATE = "apply_template";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_ENERGY = "energy";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_OUTLET = "outlet";
    public static final String CHANNEL_ACTUALTEMP = "actual_temp";
    public static final String CHANNEL_SETTEMP = "set_temp";
    public static final String CHANNEL_ECOTEMP = "eco_temp";
    public static final String CHANNEL_COMFORTTEMP = "comfort_temp";
    public static final String CHANNEL_RADIATOR_MODE = "radiator_mode";
    public static final String CHANNEL_NEXT_CHANGE = "next_change";
    public static final String CHANNEL_NEXTTEMP = "next_temp";
    public static final String CHANNEL_BATTERY_LOW = "battery_low";
    public static final String CHANNEL_BATTERY = "battery_level";
    public static final String CHANNEL_CONTACT_STATE = "contact_state";
    public static final String CHANNEL_PRESS = "press";
    public static final String CHANNEL_LAST_CHANGE = "last_change";
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ON_OFF = "on_off";

    // List of all Channel config ids
    public static final String CONFIG_CHANNEL_TEMP_OFFSET = "offset";

    // List of all Input tags
    public static final String INPUT_PRESENT = "present";
    public static final String INPUT_ACTUALTEMP = "tist";
    public static final String INPUT_SETTEMP = "tsoll";
    public static final String INPUT_ECOTEMP = "absenk";
    public static final String INPUT_COMFORTTEMP = "komfort";
    public static final String INPUT_NEXTCHANGE = "endperiod";
    public static final String INPUT_NEXTTEMP = "tchange";
    public static final String INPUT_BATTERY = "batterylow";

    // List of all call states
    public static final StringType CALL_STATE_IDLE = new StringType("IDLE");
    public static final StringType CALL_STATE_RINGING = new StringType("RINGING");
    public static final StringType CALL_STATE_DIALING = new StringType("DIALING");
    public static final StringType CALL_STATE_ACTIVE = new StringType("ACTIVE");

    // List of all Mode types
    public static final String MODE_AUTO = "AUTOMATIC";
    public static final String MODE_MANUAL = "MANUAL";
    public static final String MODE_VACATION = "VACATION";
    public static final String MODE_ON = "ON";
    public static final String MODE_OFF = "OFF";
    public static final String MODE_COMFORT = "COMFORT";
    public static final String MODE_ECO = "ECO";
    public static final String MODE_BOOST = "BOOST";
    public static final String MODE_WINDOW_OPEN = "WINDOW_OPEN";
    public static final String MODE_UNKNOWN = "UNKNOWN";

    public static final Set<ThingTypeUID> SUPPORTED_LIGHTING_THING_TYPES = Set.of(DECT500_THING_TYPE,
            HAN_FUN_COLOR_BULB_THING_TYPE, HAN_FUN_DIMMABLE_BULB_THING_TYPE);

    public static final Set<ThingTypeUID> SUPPORTED_BUTTON_THING_TYPES_UIDS = Set.of(DECT400_THING_TYPE,
            DECT440_THING_TYPE, HAN_FUN_SWITCH_THING_TYPE);

    public static final Set<ThingTypeUID> SUPPORTED_HEATING_THING_TYPES = Set.of(DECT300_THING_TYPE, DECT302_THING_TYPE,
            DECT301_THING_TYPE, COMETDECT_THING_TYPE);

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(DECT100_THING_TYPE,
            DECT200_THING_TYPE, DECT210_THING_TYPE, POWERLINE546E_THING_TYPE, HAN_FUN_CONTACT_THING_TYPE,
            HAN_FUN_ON_OFF_THING_TYPE, HAN_FUN_BLINDS_THING_TYPE);

    public static final Set<ThingTypeUID> SUPPORTED_GROUP_THING_TYPES_UIDS = Set.of(GROUP_HEATING_THING_TYPE,
            GROUP_SWITCH_THING_TYPE);

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Set.of(BRIDGE_THING_TYPE,
            POWERLINE546E_STANDALONE_THING_TYPE);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(SUPPORTED_LIGHTING_THING_TYPES,
            SUPPORTED_BUTTON_THING_TYPES_UIDS, SUPPORTED_HEATING_THING_TYPES, SUPPORTED_DEVICE_THING_TYPES_UIDS,
            SUPPORTED_GROUP_THING_TYPES_UIDS, SUPPORTED_BRIDGE_THING_TYPES_UIDS).flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
}
