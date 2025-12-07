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
package org.openhab.binding.homekit.internal;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Defines common constants which are used across the whole HomeKit binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBindingConstants {

    public static final String BINDING_ID = "homekit";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCESSORY = new ThingTypeUID(BINDING_ID, "accessory");
    public static final ThingTypeUID THING_TYPE_BRIDGED_ACCESSORY = new ThingTypeUID(BINDING_ID, "bridged-accessory");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    /**
     * Some Characteristics have variable values and others remain static over time. The latter are produced with
     * a ChannelDefinition with this channel-type uid. And when Things are created, rather than instantiating them
     * as (dynamic data) Channels of the Thing, instead they are added as (static data) Properties of the Thing.
     */
    public static final ChannelTypeUID CHANNEL_TYPE_STATIC = new ChannelTypeUID(BINDING_ID, "static-data");

    /**
     * format string for channel-group-type UIDs which represent services
     * format: 'channel-group-type'-[serviceIdentifier]-[serviceIid]-[thingId]-[accessoryId]
     * example: channel-group-type-accessory-information-1-1234567890abcdef-1
     */
    public static final String CHANNEL_GROUP_TYPE_ID_FMT = "channel-group-type-%s-%d-%s-%s";

    /**
     * format string for channel-type UIDs which represent characteristics
     * format: 'channel-type'-[characteristicIdentifier]-[characteristicIid]-[thingId]-[accessoryId]
     * example: channel-type-occupancy-detected-2694-1234567890abcdef-1
     */
    public static final String CHANNEL_TYPE_ID_FMT = "channel-type-%s-%d-%s-%s";

    /**
     * format string for channel-definition IDs like '[characteristicIdentifier]-[characteristicIid]'
     * used to instantiate channels and labels like '[thingName]-[accessoryAid]' used to discover
     * things; examples:
     * <ul>
     * <li>occupancy-detected-2694</li>
     * <li>11:22:33:44:55:66-1234</li>
     * </ul>
     */
    public static final String STRING_AID_FMT = "%s-%d";

    // labels for things e.g. 'Living Room Light (11:22:33:44:55:66-1234)'
    public static final String THING_LABEL_FMT = "%s (%s)";

    // configuration parameters
    public static final String CONFIG_HTTP_HOST_HEADER = "httpHostHeader";
    public static final String CONFIG_IP_ADDRESS = "ipAddress";
    public static final String CONFIG_REFRESH_INTERVAL = "refreshInterval";
    public static final String CONFIG_ACCESSORY_ID = "accessoryID";
    public static final String CONFIG_UNIQUE_ID = "uniqueId";

    // thing properties
    public static final String PROPERTY_PROTOCOL_VERSION = "protocolVersion";
    public static final String PROPERTY_ACCESSORY_CATEGORY = "accessoryCategory";
    public static final String PROPERTY_REPRESENTATION = "representationProperty";

    // channel properties
    public static final String PROPERTY_IID = "iid";
    public static final String PROPERTY_FORMAT = "format";
    public static final String PROPERTY_DATA_TYPE = "dataType";

    // HomeKit HTTP URI endpoints and content types
    public static final String ENDPOINT_ACCESSORIES = "/accessories";
    public static final String ENDPOINT_CHARACTERISTICS = "/characteristics";
    public static final String ENDPOINT_PAIR_SETUP = "/pair-setup";
    public static final String ENDPOINT_PAIR_VERIFY = "/pair-verify";

    public static final String CONTENT_TYPE_PAIRING = "application/pairing+tlv8";
    public static final String CONTENT_TYPE_HAP = "application/hap+json";

    // pattern matcher for pairing code XXX-XX-XXX or XXXX-XXXX or XXXXXXXX
    public static final Pattern PAIRING_CODE_PATTERN = Pattern.compile("\\d{3}-\\d{2}-\\d{3}|\\d{4}-\\d{4}|\\d{8}");

    // pattern matcher for host ipv4 address 123.123.123.123:12345
    public static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)):(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]?\\d{1,4})$");

    // pattern matcher for a fully qualified host name like foobar._hap._tcp.local. or foobar._hap._tcp.local.:12345
    // NOTE: this specially allows space characters in the host name -- even if normally not allowed by the RFC
    public static final Pattern HOST_PATTERN = Pattern.compile(
            "^([a-zA-Z0-9\\-\\x20]+)\\._hap\\._tcp\\.local\\.(?::([1-9]\\d{0,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5]))?$");

    // result messages for ThingActions; !! DO NOT LOCALIZE !!
    public static final String ACTION_RESULT_OK = "OK";
    public static final String ACTION_RESULT_OK_FORMAT = ACTION_RESULT_OK + " (%s)";
    public static final String ACTION_RESULT_ERROR = "ERROR";
    public static final String ACTION_RESULT_ERROR_FORMAT = ACTION_RESULT_ERROR + " (%s)";
}
