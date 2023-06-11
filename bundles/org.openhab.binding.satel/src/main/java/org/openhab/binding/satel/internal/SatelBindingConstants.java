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
package org.openhab.binding.satel.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link SatelBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SatelBindingConstants {

    public static final String BINDING_ID = "satel";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ETHM1 = new ThingTypeUID(BINDING_ID, "ethm-1");
    public static final ThingTypeUID THING_TYPE_INTRS = new ThingTypeUID(BINDING_ID, "int-rs");
    public static final ThingTypeUID THING_TYPE_OUTPUT = new ThingTypeUID(BINDING_ID, "output");
    public static final ThingTypeUID THING_TYPE_PARTITION = new ThingTypeUID(BINDING_ID, "partition");
    public static final ThingTypeUID THING_TYPE_SHUTTER = new ThingTypeUID(BINDING_ID, "shutter");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_SYSTEM = new ThingTypeUID(BINDING_ID, "system");
    public static final ThingTypeUID THING_TYPE_EVENTLOG = new ThingTypeUID(BINDING_ID, "event-log");
    public static final ThingTypeUID THING_TYPE_ATD100 = new ThingTypeUID(BINDING_ID, "atd-100");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Stream.of(THING_TYPE_ETHM1, THING_TYPE_INTRS)
            .collect(Collectors.toSet());

    // Physical devices
    public static final Set<ThingTypeUID> DEVICE_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_OUTPUT, THING_TYPE_PARTITION, THING_TYPE_SHUTTER, THING_TYPE_ZONE, THING_TYPE_ATD100)
            .collect(Collectors.toSet());

    // Virtual devices
    public static final Set<ThingTypeUID> VIRTUAL_THING_TYPES_UIDS = Stream.of(THING_TYPE_SYSTEM, THING_TYPE_EVENTLOG)
            .collect(Collectors.toSet());

    // Channel types
    public static final ChannelTypeUID CHANNEL_TYPE_LOBATT = new ChannelTypeUID("system", "low-battery");
    public static final ChannelTypeUID CHANNEL_TYPE_NOCOMM = new ChannelTypeUID(BINDING_ID, "device_nocomm");

    // List of all Channel ids except those covered by state enums
    public static final String CHANNEL_SHUTTER_STATE = "shutter_state";
    public static final String CHANNEL_DATE_TIME = "date_time";
    public static final String CHANNEL_SERVICE_MODE = "service_mode";
    public static final String CHANNEL_TROUBLES = "troubles";
    public static final String CHANNEL_TROUBLES_MEMORY = "troubles_memory";
    public static final String CHANNEL_ACU100_PRESENT = "acu100_present";
    public static final String CHANNEL_INTRX_PRESENT = "intrx_present";
    public static final String CHANNEL_GRADE23_SET = "grade23_set";
    public static final String CHANNEL_USER_CODE = "user_code";
    public static final String CHANNEL_INDEX = "index";
    public static final String CHANNEL_PREV_INDEX = "prev_index";
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_DESCRIPTION = "description";
    public static final String CHANNEL_DETAILS = "details";
    public static final String CHANNEL_TEMPERATURE = "temperature";
}
