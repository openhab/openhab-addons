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

    // labels
    public static final String THING_LABEL_FMT = "%s on %s";
    public static final String GROUP_TYPE_LABEL_FMT = "Channel group type: %s";
    public static final String CHANNEL_TYPE_LABEL_FMT = "Channel type: %s";

    // configuration parameters
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_PAIRING_CODE = "pairingCode";
    public static final String CONFIG_REFRESH_INTERVAL = "refreshInterval";

    // properties
    public static final String PROPERTY_ACCESSORY_UID = "accessoryUID";
    public static final String PROPERTY_PROTOCOL_VERSION = "protocolVersion";
    public static final String PROPERTY_ACCESSORY_CATEGORY = "accessoryCategory";
    public static final String PROPERTY_CONTROLLER_PRIVATE_KEY = "controllerPrivateKey";
    public static final String PROPERTY_ACCESSORY_PUBLIC_KEY = "accessoryPublicKey";

    // HomeKit HTTP URI endpoints and content types
    public static final String ENDPOINT_PAIRING = "pair-setup";
    public static final String ENDPOINT_ACCESSORIES = "accessories";
    public static final String ENDPOINT_CHARACTERISTICS = "characteristics";
    public static final String CONTENT_TYPE_PAIRING = "application/pairing+tlv8";
    public static final String CONTENT_TYPE_HAP = "application/hap+json";
}
