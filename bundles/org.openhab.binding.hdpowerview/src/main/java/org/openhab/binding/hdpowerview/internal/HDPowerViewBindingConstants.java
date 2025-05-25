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
package org.openhab.binding.hdpowerview.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HDPowerViewBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 * @author Jacob Laursen - Added support for scene groups, automations and repeaters
 */
@NonNullByDefault
public class HDPowerViewBindingConstants {

    public static final String BINDING_ID = "hdpowerview";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HUB = new ThingTypeUID(BINDING_ID, "hub");
    public static final ThingTypeUID THING_TYPE_SHADE = new ThingTypeUID(BINDING_ID, "shade");
    public static final ThingTypeUID THING_TYPE_REPEATER = new ThingTypeUID(BINDING_ID, "repeater");

    // List of all Channel ids
    public static final String CHANNEL_SHADE_POSITION = "position";
    public static final String CHANNEL_SHADE_SECONDARY_POSITION = "secondary";
    public static final String CHANNEL_SHADE_VANE = "vane";
    public static final String CHANNEL_SHADE_COMMAND = "command";
    public static final String CHANNEL_SHADE_LOW_BATTERY = "lowBattery";
    public static final String CHANNEL_SHADE_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_SHADE_BATTERY_VOLTAGE = "batteryVoltage";
    public static final String CHANNEL_SHADE_SIGNAL_STRENGTH = "signalStrength";
    public static final String CHANNEL_SHADE_HUB_RSSI = "hubRssi";
    public static final String CHANNEL_SHADE_REPEATER_RSSI = "repeaterRssi";

    public static final String CHANNEL_REPEATER_COLOR = "color";
    public static final String CHANNEL_REPEATER_IDENTIFY = "identify";
    public static final String CHANNEL_REPEATER_BLINKING_ENABLED = "blinkingEnabled";

    public static final String CHANNEL_GROUP_SCENES = "scenes";
    public static final String CHANNEL_GROUP_SCENE_GROUPS = "sceneGroups";
    public static final String CHANNEL_GROUP_AUTOMATIONS = "automations";

    public static final String CHANNELTYPE_SCENE_ACTIVATE = "scene-activate";
    public static final String CHANNELTYPE_SCENE_GROUP_ACTIVATE = "scene-group-activate";
    public static final String CHANNELTYPE_AUTOMATION_ENABLED = "automation-enabled";

    // Hub properties
    public static final String PROPERTY_FIRMWARE_NAME = "firmwareName";
    public static final String PROPERTY_RADIO_FIRMWARE_VERSION = "radioFirmwareVersion";
    public static final String PROPERTY_HUB_NAME = "hubName";

    // Shade properties
    public static final String PROPERTY_SHADE_TYPE = "type";
    public static final String PROPERTY_SHADE_CAPABILITIES = "capabilities";
    public static final String PROPERTY_MOTOR_FIRMWARE_VERSION = "motorFirmwareVersion";

    public static final List<String> NETBIOS_NAMES = Arrays.asList("PDBU-Hub3.0", "PowerView-Hub");

    public static final Pattern VALID_IP_V4_ADDRESS = Pattern
            .compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

    // generation 3
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_SHADE3 = new ThingTypeUID(BINDING_ID, "shade3");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_HUB, THING_TYPE_SHADE,
            THING_TYPE_REPEATER, THING_TYPE_GATEWAY, THING_TYPE_SHADE3);

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_POWER_TYPE = "powerType";
    public static final String PROPERTY_BLE_NAME = "bleName";

    // keys for hub/gateway label translation
    public static final String LABEL_KEY_HUB = "discovery.hub.label";
    public static final String LABEL_KEY_GATEWAY = "discovery.gateway.label";
}
