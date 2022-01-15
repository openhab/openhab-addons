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
package org.openhab.binding.cbus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link CBusBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Scott Linton - Initial contribution
 */

@NonNullByDefault
public class CBusBindingConstants {

    public static final String BINDING_ID = "cbus";

    // List of main things
    public static final String BRIDGE_CGATE = "cgate";
    public static final String BRIDGE_NETWORK = "network";
    public static final String THING_GROUP = "group";
    public static final String THING_LIGHT = "light";
    public static final String THING_TEMPERATURE = "temperature";
    public static final String THING_TRIGGER = "trigger";
    public static final String THING_DALI = "dali";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_CGATE = new ThingTypeUID(BINDING_ID, BRIDGE_CGATE);
    public static final ThingTypeUID BRIDGE_TYPE_NETWORK = new ThingTypeUID(BINDING_ID, BRIDGE_NETWORK);
    public static final ThingTypeUID THING_TYPE_GROUP = new ThingTypeUID(BINDING_ID, THING_GROUP);
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, THING_LIGHT);
    public static final ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, THING_TEMPERATURE);
    public static final ThingTypeUID THING_TYPE_TRIGGER = new ThingTypeUID(BINDING_ID, THING_TRIGGER);
    public static final ThingTypeUID THING_TYPE_DALI = new ThingTypeUID(BINDING_ID, THING_DALI);

    // List of all Channel ids
    public static final String CHANNEL_LEVEL = "level";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_TEMP = "temp";
    public static final String CHANNEL_VALUE = "value";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(BRIDGE_TYPE_CGATE, BRIDGE_TYPE_NETWORK, THING_TYPE_GROUP, THING_TYPE_LIGHT,
                    THING_TYPE_TEMPERATURE, THING_TYPE_TRIGGER, THING_TYPE_DALI));
    public static final Set<ThingTypeUID> NETWORK_DISCOVERY_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(BRIDGE_TYPE_NETWORK));

    public static final String CONFIG_NETWORK_ID = "id";
    public static final String CONFIG_NETWORK_PROJECT = "project";
    public static final String CONFIG_NETWORK_SYNC = "syncInterval";
    public static final String PROPERTY_NETWORK_NAME = "name";

    public static final String CONFIG_CGATE_IP_ADDRESS = "ipAddress";

    public static final String CONFIG_GROUP_ID = "group";

    public static final String PROPERTY_NETWORK_ID = "CBUS Network Id";
    public static final String PROPERTY_APPLICATION_ID = "CBUS Application Id";
    public static final String PROPERTY_GROUP_NAME = "CBUS Group Name";

    public static final int CBUS_APPLICATION_LIGHTING = 56;
    public static final int CBUS_APPLICATION_TEMPERATURE = 25;
    public static final int CBUS_APPLICATION_TRIGGER = 202;
    public static final int CBUS_APPLICATION_DALI = 95;
}
