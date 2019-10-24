/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.touchwand.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TouchWandBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandBindingConstants {

    private static final String BINDING_ID = "touchwand";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_SHUTTER = new ThingTypeUID(BINDING_ID, "shutter");
    public static final ThingTypeUID THING_TYPE_WALLCONTROLLER = new ThingTypeUID(BINDING_ID, "wallcontroller");
    public static final ThingTypeUID THING_TYPE_SCENARIO = new ThingTypeUID(BINDING_ID, "scenario");

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_SHUTTER = "shutter";
    public static final String CHANNEL_SWITCH_LONGSHORT = "switch_longshort";

    // List of configuration parameters

    public static final String HOST = "ipAddress";
    public static final String PORT = "port";
    public static final String USER = "username";
    public static final String PASS = "password";
    public static final String REFRESH = "refresh";

    // commands
    public static final String SWITCH_STATUS_ON = "255";
    public static final String SWITCH_STATUS_OFF = "0";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SWITCH);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SHUTTER);
        // SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_WALLCONTROLLER); // not implemented yet
        // SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SCENARIO); // not implemented yet
    }

}
