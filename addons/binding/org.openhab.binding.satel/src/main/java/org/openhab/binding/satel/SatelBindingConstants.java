/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SatelBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
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

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ETHM1, THING_TYPE_INTRS);

    // Physical devices
    public static final Set<ThingTypeUID> DEVICE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_OUTPUT,
            THING_TYPE_PARTITION, THING_TYPE_SHUTTER, THING_TYPE_ZONE);

    // Virtual devices
    public static final Set<ThingTypeUID> VIRTUAL_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_SYSTEM);

    // List of all Channel ids
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_DATE_TIME = "date_time";
    public static final String CHANNEL_SERVICE_MODE = "service_mode";
    public static final String CHANNEL_TROUBLES = "troubles";
    public static final String CHANNEL_TROUBLES_MEMORY = "troubles_memory";
    public static final String CHANNEL_ACU100_PRESENT = "acu100_present";
    public static final String CHANNEL_INTRX_PRESENT = "intrx_present";
    public static final String CHANNEL_GRADE23_SET = "grade23_set";
    public static final String CHANNEL_USER_CODE = "user_code";

    // Thing properties
    public static final String PROPERTY_REQUIRES_REFRESH = "requiresRefresh";

}
