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

    public static final String DISCOVERY_GATEWAY_LABEL_PATTERN = "Z-Wave JS Gateway (%s)";
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
    public static final String CONFIG_CHANNEL_ITEM_TYPE = "itemType";
    public static final String CONFIG_CHANNEL_COMMANDCLASS_NAME = "commandClassName";
    public static final String CONFIG_CHANNEL_COMMANDCLASS_ID = "commandClassId";
    public static final String CONFIG_CHANNEL_ENDPOINT = "endpoint";
    public static final String CONFIG_CHANNEL_WRITE_PROPERTY = "writeProperty";
    public static final String CONFIG_CHANNEL_INVERTED = "inverted";
    public static final String CONFIG_CHANNEL_FACTOR = "factor";

    // List of all Thing Properties
    public static final String PROPERTY_HOME_ID = "homeId";
    public static final String PROPERTY_DRIVER_VERSION = "driverVersion";
    public static final String PROPERTY_SERVER_VERSION = "serverVersion";
    public static final String PROPERTY_SCHEMA_MIN = "minSchemaVersion";
    public static final String PROPERTY_SCHEMA_MAX = "maxSchemaVersion";

    public static final String PROPERTY_NODE_MANUFACTURER = "manufacturer";
    public static final String PROPERTY_NODE_PRODUCT = "product";
    public static final String PROPERTY_NODE_IS_SECURE = "isSecure";
    public static final String PROPERTY_NODE_IS_LISTENING = "isListening";
    public static final String PROPERTY_NODE_IS_ROUTING = "isRouting";
    public static final String PROPERTY_NODE_LASTSEEN = "lastSeen";
    public static final String PROPERTY_NODE_FREQ_LISTENING = "isFrequentListening";

    public static final String CC_CONFIGURATION = "Configuration";
    public static final String CC_WAKE_UP = "Wake Up";
    public static final String CC_NOTIFICATION = "Notification";

    public static final List<String> CONFIGURATION_COMMAND_CLASSES = List.of(CC_CONFIGURATION, CC_WAKE_UP);
}
