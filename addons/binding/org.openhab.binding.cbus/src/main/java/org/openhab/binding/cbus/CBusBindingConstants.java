/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link CBusBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Scott Linton - Initial contribution
 */
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
    public final static ThingTypeUID BRIDGE_TYPE_CGATE = new ThingTypeUID(BINDING_ID, BRIDGE_CGATE);
    public final static ThingTypeUID BRIDGE_TYPE_NETWORK = new ThingTypeUID(BINDING_ID, BRIDGE_NETWORK);
    public final static ThingTypeUID THING_TYPE_GROUP = new ThingTypeUID(BINDING_ID, THING_GROUP);
    public final static ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, THING_LIGHT);
    public final static ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, THING_TEMPERATURE);
    public final static ThingTypeUID THING_TYPE_TRIGGER = new ThingTypeUID(BINDING_ID, THING_TRIGGER);
    public final static ThingTypeUID THING_TYPE_DALI = new ThingTypeUID(BINDING_ID, THING_DALI);

    // List of all Channel ids
    public final static String CHANNEL_LEVEL = "level";
    public final static String CHANNEL_STATE = "state";
    public final static String CHANNEL_TEMP = "temp";
    public final static String CHANNEL_VALUE = "value";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(BRIDGE_TYPE_CGATE,
            BRIDGE_TYPE_NETWORK, THING_TYPE_GROUP, THING_TYPE_LIGHT, THING_TYPE_TEMPERATURE, THING_TYPE_TRIGGER,
            THING_TYPE_DALI);
    public final static Set<ThingTypeUID> NETWORK_DISCOVERY_THING_TYPES_UIDS = ImmutableSet.of(BRIDGE_TYPE_NETWORK);

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PROJECT = "project";
    public static final String PROPERTY_NETWORK_SYNC = "syncInterval";

    public static final String PROPERTY_REFRESH_INTERVAL = "refresh";
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";

    public static final String CONFIG_GROUP_ID = "group";

    public static final String CBUS_APPLICATION_LIGHTING = "56";
    public static final String CBUS_APPLICATION_TEMPERATURE = "25";
    public static final String CBUS_APPLICATION_TRIGGER = "202";
    public static final String CBUS_APPLICATION_DALI = "95";
}
