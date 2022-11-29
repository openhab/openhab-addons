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

import java.util.Set;

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
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BOND_FAN, THING_TYPE_BOND_SHADES,
            THING_TYPE_BOND_FIREPLACE, THING_TYPE_BOND_GENERIC);

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES = Set.of(THING_TYPE_BOND_BRIDGE);

    /**
     * List of all Channel ids - these match the id fields in the OH-INF xml files
     */

    // Universal channels
    public static final String CHANNEL_GROUP_COMMON = "common";
    public static final String CHANNEL_POWER = CHANNEL_GROUP_COMMON + "#power";
    public static final String CHANNEL_COMMAND = CHANNEL_GROUP_COMMON + "command";

    // Ceiling fan channels
    public static final String CHANNEL_GROUP_FAN = "fan";
    public static final String CHANNEL_FAN_POWER = CHANNEL_GROUP_FAN + "#power";
    public static final String CHANNEL_FAN_SPEED = CHANNEL_GROUP_FAN + "#speed";
    public static final String CHANNEL_FAN_BREEZE_STATE = CHANNEL_GROUP_FAN + "#breezeState";
    public static final String CHANNEL_FAN_BREEZE_MEAN = CHANNEL_GROUP_FAN + "#breezeMean";
    public static final String CHANNEL_FAN_BREEZE_VAR = CHANNEL_GROUP_FAN + "#breezeVariability";
    public static final String CHANNEL_FAN_DIRECTION = CHANNEL_GROUP_FAN + "#direction";
    public static final String CHANNEL_FAN_TIMER = CHANNEL_GROUP_FAN + "#timer";

    // Fan light channels
    public static final String CHANNEL_GROUP_LIGHT = "light";
    public static final String CHANNEL_LIGHT_POWER = CHANNEL_GROUP_LIGHT + "#power";
    public static final String CHANNEL_LIGHT_BRIGHTNESS = CHANNEL_GROUP_LIGHT + "#brightness";

    public static final String CHANNEL_GROUP_UP_LIGHT = "upLight";
    public static final String CHANNEL_UP_LIGHT_POWER = CHANNEL_GROUP_UP_LIGHT + "#power";
    public static final String CHANNEL_UP_LIGHT_ENABLE = CHANNEL_GROUP_UP_LIGHT + "#enable";
    public static final String CHANNEL_UP_LIGHT_BRIGHTNESS = CHANNEL_GROUP_UP_LIGHT + "#brightness";

    public static final String CHANNEL_GROUP_DOWN_LIGHT = "downLight";
    public static final String CHANNEL_DOWN_LIGHT_POWER = CHANNEL_GROUP_DOWN_LIGHT + "#power";
    public static final String CHANNEL_DOWN_LIGHT_ENABLE = CHANNEL_GROUP_DOWN_LIGHT + "#enable";
    public static final String CHANNEL_DOWN_LIGHT_BRIGHTNESS = CHANNEL_GROUP_DOWN_LIGHT + "#brightness";

    // Fireplace channels
    public static final String CHANNEL_GROUP_FIREPLACE = "fireplace";
    public static final String CHANNEL_FLAME = CHANNEL_GROUP_FIREPLACE + "#flame";

    // Motorize shade channels
    public static final String CHANNEL_GROUP_SHADES = "shade";
    public static final String CHANNEL_ROLLERSHUTTER = CHANNEL_GROUP_SHADES + "#rollershutter";

    /**
     * Configuration arguments
     */
    public static final String CONFIG_SERIAL_NUMBER = "serialNumber";
    public static final String CONFIG_IP_ADDRESS = "ipAddress";
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

    /**
     * Constants
     */
    public static final int BOND_BPUP_PORT = 30007;
    public static final int BOND_API_TIMEOUT_MS = 3000;
}
