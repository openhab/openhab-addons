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
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ACCESSORY = new ThingTypeUID(BINDING_ID, "accessory");

    // specific Channel Type UIDs
    public static final String FAKE_PROPERTY_CHANNEL = "property-fake-channel";
    public static final ChannelTypeUID FAKE_PROPERTY_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID,
            FAKE_PROPERTY_CHANNEL);

    // prefixes for channel-group-type and channel-type UIDs
    public static final String CHANNEL_GROUP_TYPE_ID_FMT = "channel-group-type-%s";
    public static final String CHANNEL_TYPE_ID_FMT = "channel-type-%s-";

    // labels
    public static final String THING_LABEL_FMT = "%s on %s";

    // configuration parameters
    public static final String CONFIG_HOST_NAME = "hostName";
    public static final String CONFIG_IP_ADDRESS = "ipAddress";
    public static final String CONFIG_REFRESH_INTERVAL = "refreshInterval";
    public static final String CONFIG_ACCESSORY_ID = "accessoryID";

    // thing properties
    public static final String PROPERTY_PROTOCOL_VERSION = "protocolVersion";
    public static final String PROPERTY_ACCESSORY_CATEGORY = "accessoryCategory";

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
