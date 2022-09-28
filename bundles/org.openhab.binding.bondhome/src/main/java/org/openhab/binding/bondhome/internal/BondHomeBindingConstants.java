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
package org.openhab.binding.bondhome.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BondHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondHomeBindingConstants {

    public static final String BINDING_ID = "bondhome";
    public static final String CURRENT_BINDING_VERSION = "v0.2.4";

    /**
     * List of all Thing Type UIDs.
     */
    public static final ThingTypeUID THING_TYPE_BOND_BRIDGE = new ThingTypeUID(BINDING_ID, "bondBridge");
    public static final ThingTypeUID THING_TYPE_BOND_FAN = new ThingTypeUID(BINDING_ID, "bondFan");
    public static final ThingTypeUID THING_TYPE_BOND_SHADES = new ThingTypeUID(BINDING_ID, "bondShades");
    public static final ThingTypeUID THING_TYPE_BOND_FIREPLACE = new ThingTypeUID(BINDING_ID, "bondFireplace");
    public static final ThingTypeUID THING_TYPE_BOND_GENERIC = new ThingTypeUID(BINDING_ID, "bondGenericThing");

    /**
     * The supported thing types.
     */
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_TYPES = Stream
            .of(THING_TYPE_BOND_FAN, THING_TYPE_BOND_SHADES, THING_TYPE_BOND_FIREPLACE, THING_TYPE_BOND_GENERIC)
            .collect(Collectors.toSet());

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES = Collections.singleton(THING_TYPE_BOND_BRIDGE);

    /**
     * List of all Channel ids - these match the id fields in the OH-INF xml files
     */

    // Universal channels
    public static final String CHANNEL_GROUP_COMMON = "common";
    public static final String CHANNEL_POWER_STATE = "common#power";
    public static final String CHANNEL_LAST_UPDATE = "common#lastUpdate";
    public static final String CHANNEL_STOP = "common#stop";

    // Ceiling fan channels
    public static final String CHANNEL_GROUP_FAN = "fan";
    public static final String CHANNEL_FAN_SPEED = "fan#speed";
    public static final String CHANNEL_FAN_BREEZE_STATE = "fan#breezeState";
    public static final String CHANNEL_FAN_BREEZE_MEAN = "fan#breezeMean";
    public static final String CHANNEL_FAN_BREEZE_VAR = "fan#breezeVariability";
    public static final String CHANNEL_FAN_DIRECTION = "fan#direction";
    public static final String CHANNEL_TIMER = "fan#timer";

    // Fan light channels
    public static final String CHANNEL_GROUP_LIGHT = "light";
    public static final String CHANNEL_LIGHT_STATE = "light#power";
    public static final String CHANNEL_LIGHT_BRIGHTNESS = "light#brightness";
    public static final String CHANNEL_LIGHT_START_STOP = "light#dimmerStartStop";
    public static final String CHANNEL_LIGHT_DIRECTIONAL_INC = "light#dimmerIncr";
    public static final String CHANNEL_LIGHT_DIRECTIONAL_DECR = "light#dimmerDcr";

    public static final String CHANNEL_GROUP_UP_LIGHT = "upLight";
    public static final String CHANNEL_UP_LIGHT_STATE = "upLight#power";
    public static final String CHANNEL_UP_LIGHT_ENABLE = "upLight#enable";
    public static final String CHANNEL_UP_LIGHT_BRIGHTNESS = "upLight#brightness";
    public static final String CHANNEL_UP_LIGHT_START_STOP = "upLight#dimmerStartStop";
    public static final String CHANNEL_UP_LIGHT_DIRECTIONAL_INC = "upLight#dimmerIncr";
    public static final String CHANNEL_UP_LIGHT_DIRECTIONAL_DECR = "upLight#dimmerDcr";

    public static final String CHANNEL_GROUP_DOWN_LIGHT = "downLight";
    public static final String CHANNEL_DOWN_LIGHT_STATE = "downLight#power";
    public static final String CHANNEL_DOWN_LIGHT_ENABLE = "downLight#enable";
    public static final String CHANNEL_DOWN_LIGHT_BRIGHTNESS = "downLight#brightness";
    public static final String CHANNEL_DOWN_LIGHT_START_STOP = "downLight#dimmerStartStop";
    public static final String CHANNEL_DOWN_LIGHT_DIRECTIONAL_INC = "downLight#dimmerIncr";
    public static final String CHANNEL_DOWN_LIGHT_DIRECTIONAL_DECR = "downLight#dimmerDcr";

    // Fireplace channels
    public static final String CHANNEL_GROUP_FIREPLACE = "fireplace";
    public static final String CHANNEL_FLAME = "fireplace#flame";
    public static final String CHANNEL_FP_FAN_STATE = "fireplace#fanPower";
    public static final String CHANNEL_FP_FAN_SPEED = "fireplace#fanSpeed";

    // Motorize shade channels
    public static final String CHANNEL_GROUP_SHADES = "shade";
    public static final String CHANNEL_OPEN_CLOSE = "shade#openShade";
    public static final String CHANNEL_HOLD = "shade#hold";
    public static final String CHANNEL_PRESET = "shade#preset";

    /**
     * Configuration arguments
     */
    public static final String CONFIG_BOND_ID = "bondId";
    public static final String CONFIG_IP_ADDRESS = "bondIpAddress";
    public static final String CONFIG_LOCAL_TOKEN = "localToken";
    public static final String CONFIG_DEVICE_ID = "deviceId";
    public static final String CONFIG_LATEST_HASH = "lastDeviceConfigurationHash";

    /**
     * Device Properties
     */
    public static final String PROPERTIES_DEVICE_NAME = "deviceName";
    public static final String PROPERTIES_TEMPLATE_NAME = "template";
    public static final String PROPERTIES_MAX_SPEED = "maxSpeed";
    public static final String PROPERTIES_TRUST_STATE = "trustState";
    public static final String PROPERTIES_ADDRESS = "addr";
    public static final String PROPERTIES_RF_FREQUENCY = "freq";
    public static final String PROPERTIES_BINDING_VERSION = "bindingVersion";

    /**
     * Constants
     */
    public static final int BOND_BPUP_PORT = 30007;
    public static final int BOND_API_TIMEOUT_MS = 3000;
    public static final String API_ERR_HTTP_401_UNAUTHORIZED = "You need authentication credentials to continue";
    public static final String API_ERR_HTTP_404_NOTFOUND = "Resource not found";
    public static final String API_ERR_HTTP_500_SERVERERR = "Something unexpected happened";
    public static final String API_HASH = "hash";
    public static final String API_MISSING_DEVICE_NAME = "deviceName";
    public static final String API_MISSING_TEMPLATE = "template";
    public static final String API_MISSING_BOND_ID = "ZZBL12345";
    public static final String API_MISSING_LOCAL_TOKEN = "localToken";

    public static final String BOND_BRIDGE_TARGET = "zermatt";
    public static final String BOND_LAST_KNOWN_FIRMWARE = "v2.10.21";
    public static final String BOND_LAST_KNOWN_FIRMWARE_DATE = "Wed Feb 19 15:31:13 UTC 2020";
    public static final String BOND_BRIDGE_MAKE = "Olibra";
    public static final String BOND_BRIDGE_MODEL = "BD-1000";
    public static final String BOND_BRIDGE_BRANDING = "O_BD-1000";
}
