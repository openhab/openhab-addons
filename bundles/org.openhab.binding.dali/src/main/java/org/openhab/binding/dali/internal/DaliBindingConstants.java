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
package org.openhab.binding.dali.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DaliBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliBindingConstants {

    private static final String BINDING_ID = "dali";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "daliserver");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_GROUP = new ThingTypeUID(BINDING_ID, "group");
    public static final ThingTypeUID THING_TYPE_RGB = new ThingTypeUID(BINDING_ID, "rgb");
    public static final ThingTypeUID THING_TYPE_DEVICE_DT8 = new ThingTypeUID(BINDING_ID, "device-dt8");
    public static final ThingTypeUID THING_TYPE_GROUP_DT8 = new ThingTypeUID(BINDING_ID, "group-dt8");
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = new HashSet<>(Arrays
            .asList(THING_TYPE_DEVICE, THING_TYPE_GROUP, THING_TYPE_RGB, THING_TYPE_DEVICE_DT8, THING_TYPE_GROUP_DT8));

    public static final String CHANNEL_DIM_AT_FADE_RATE = "dimAtFadeRate";
    public static final String CHANNEL_DIM_IMMEDIATELY = "dimImmediately";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "color-temperature-abs";

    public static final String TARGET_ID = "targetId";
    public static final String READ_DEVICE_TARGET_ID = "readDeviceTargetId";
    public static final String TARGET_ID_R = "targetIdR";
    public static final String TARGET_ID_G = "targetIdG";
    public static final String TARGET_ID_B = "targetIdB";

    public static final int DALI_SWITCH_100_PERCENT = 254;
}
