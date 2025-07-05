/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal;

import static org.openhab.binding.zwavejs.internal.CommandClassConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class BindingConstants {

    public static final String BINDING_ID = "zwavejs";

    public static final String DISCOVERY_NODE_LABEL_PATTERN = "%s %s (node %s)";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_NODE = new ThingTypeUID(BINDING_ID, "node");

    // List of all Thing Configuration Parameters
    public static final String CONFIG_HOSTNAME = "hostname";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_NODE_ID = "id";
    public static final String CONFIG_CONFIG_AS_CHANNEL = "configurationChannels";

    // List of all Channel Configuration Parameters
    public static final String CONFIG_CHANNEL_INCOMING_UNIT = "incomingUnit";
    public static final String CONFIG_CHANNEL_COMMANDCLASS_NAME = "commandClassName";
    public static final String CONFIG_CHANNEL_COMMANDCLASS_ID = "commandClassId";
    public static final String CONFIG_CHANNEL_ENDPOINT = "endpoint";
    public static final String CONFIG_CHANNEL_PROPERTY_KEY_STR = "propertyKeyStr";
    public static final String CONFIG_CHANNEL_PROPERTY_KEY_INT = "propertyKeyInt";
    public static final String CONFIG_CHANNEL_READ_PROPERTY = "readProperty";
    public static final String CONFIG_CHANNEL_WRITE_PROPERTY_STR = "writePropertyStr";
    public static final String CONFIG_CHANNEL_WRITE_PROPERTY_INT = "writePropertyInt";
    public static final String CONFIG_CHANNEL_INVERTED = "inverted";
    public static final String CONFIG_CHANNEL_FACTOR = "factor";

    // List of all Thing Properties
    public static final String PROPERTY_HOME_ID = "homeId";
    public static final String PROPERTY_DRIVER_VERSION = "driverVersion";
    public static final String PROPERTY_SERVER_VERSION = "serverVersion";
    public static final String PROPERTY_SCHEMA_MIN = "minSchemaVersion";
    public static final String PROPERTY_SCHEMA_MAX = "maxSchemaVersion";
    public static final String PROPERTY_NODE_IS_SECURE = "isSecure";
    public static final String PROPERTY_NODE_IS_LISTENING = "isListening";
    public static final String PROPERTY_NODE_IS_ROUTING = "isRouting";
    public static final String PROPERTY_NODE_LASTSEEN = "lastSeen";
    public static final String PROPERTY_NODE_FREQ_LISTENING = "isFrequentListening";

    public static final List<Integer> CONFIGURATION_COMMAND_CLASSES = List.of(COMMAND_CLASS_CONFIGURATION,
            COMMAND_CLASS_WAKE_UP);

    // color related property keys
    public static final int WARM_PROPERTY_KEY = 0;
    public static final int COLD_PROPERTY_KEY = 1;
    public static final int RED_PROPERTY_KEY = 2;
    public static final int GREEN_PROPERTY_KEY = 3;
    public static final int BLUE_PROPERTY_KEY = 4;

    // color related strings
    public static final String RED = "red";
    public static final String GREEN = "green";
    public static final String BLUE = "blue";
    public static final String WARM_WHITE = "warmWhite";
    public static final String COLD_WHITE = "coldWhite";
    public static final String HEX = "hex";
    public static final String COLOR_TEMP_CHANNEL_COMMAND_CLASS_NAME = "Color Switch";
    public static final String COLOR_TEMP_CHANNEL_PROPERTY_NAME = "colorTemperature";
}
