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
package org.openhab.binding.digiplex.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DigiplexBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Robert Michalak - Initial contribution
 */
@NonNullByDefault
public class DigiplexBindingConstants {

    private static final String BINDING_ID = "digiplex";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_AREA = new ThingTypeUID(BINDING_ID, "area");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(DigiplexBindingConstants.THING_TYPE_BRIDGE, DigiplexBindingConstants.THING_TYPE_ZONE,
                    DigiplexBindingConstants.THING_TYPE_AREA)
            .collect(Collectors.toSet());

    public static final String PROPERTY_ZONE_NO = "ZONE_ID";
    public static final String PROPERTY_AREA_NO = "AREA_ID";

    // List of all Channel ids
    // Bridge
    public static final String BRIDGE_MESSAGES_SENT = "statistics#messages_sent";
    public static final String BRIDGE_RESPONSES_RECEIVED = "statistics#responses_received";
    public static final String BRIDGE_EVENTS_RECEIVED = "statistics#events_received";

    public static final String BRIDGE_TLM_TROUBLE = "troubles#tlm_trouble";
    public static final String BRIDGE_AC_FAILURE = "troubles#ac_failure";
    public static final String BRIDGE_BATTERY_FAILURE = "troubles#battery_failure";
    public static final String BRIDGE_AUX_CURRENT_LIMIT = "troubles#aux_current_limit";
    public static final String BRIDGE_BELL_CURRENT_LIMIT = "troubles#bell_current_limit";
    public static final String BRIDGE_BELL_ABSENT = "troubles#bell_absent";
    public static final String BRIDGE_CLOCK_TROUBLE = "troubles#clock_trouble";
    public static final String BRIDGE_GLOBAL_FIRE_LOOP = "troubles#global_fire_loop";
    // Zone
    public static final String ZONE_STATUS = "status";
    public static final String ZONE_EXTENDED_STATUS = "extended_status";
    public static final String ZONE_ALARM = "alarm";
    public static final String ZONE_FIRE_ALARM = "fire_alarm";
    public static final String ZONE_SUPERVISION_LOST = "supervision_lost";
    public static final String ZONE_LOW_BATTERY = "low_battery";
    public static final String ZONE_LAST_TRIGGERED = "last_triggered";
    // Area
    public static final String AREA_STATUS = "status";
    public static final String AREA_ARMED = "armed";
    public static final String AREA_ZONE_IN_MEMORY = "zone_in_memory";
    public static final String AREA_TROUBLE = "trouble";
    public static final String AREA_READY = "ready";
    public static final String AREA_IN_PROGRAMMING = "in_programming";
    public static final String AREA_ALARM = "alarm";
    public static final String AREA_STROBE = "strobe";
    public static final String AREA_CONTROL = "control";

    public static final List<String> ZONE_DEFAULT_NAMES = Arrays.asList("Zone %03d", "Zone %d");
    public static final String AREA_DEFAULT_NAME = "Area %d";

    public static final StringType COMMAND_OK = new StringType("Ok");
    public static final StringType COMMAND_FAIL = new StringType("Fail");

    public static final int GLOBAL_AREA_NO = 0;
}
