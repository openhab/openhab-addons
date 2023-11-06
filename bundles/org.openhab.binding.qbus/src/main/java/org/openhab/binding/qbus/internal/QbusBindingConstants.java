/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.qbus.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link QbusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Koen Schockaert - Initial contribution
 */
@NonNullByDefault
public class QbusBindingConstants {

    private static final String BINDING_ID = "qbus";

    // bridge
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(BRIDGE_THING_TYPE);
    // Bridge config properties
    public static final String CONFIG_HOST_NAME = "addr";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_SN = "sn";
    public static final String CONFIG_SERVERCHECK = "serverCheck";

    // generic thing types
    public static final ThingTypeUID THING_TYPE_CO2 = new ThingTypeUID(BINDING_ID, "co2");
    public static final ThingTypeUID THING_TYPE_SCENE = new ThingTypeUID(BINDING_ID, "scene");
    public static final ThingTypeUID THING_TYPE_ON_OFF_LIGHT = new ThingTypeUID(BINDING_ID, "onOff");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER_SLATS = new ThingTypeUID(BINDING_ID,
            "rollershutter_slats");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Thing Type UIDs
    public static final Set<ThingTypeUID> SCENE_THING_TYPES_UIDS = Set.of(THING_TYPE_SCENE);
    public static final Set<ThingTypeUID> CO2_THING_TYPES_UIDS = Set.of(THING_TYPE_CO2);
    public static final Set<ThingTypeUID> ROLLERSHUTTER_THING_TYPES_UIDS = Set.of(THING_TYPE_ROLLERSHUTTER);
    public static final Set<ThingTypeUID> ROLLERSHUTTER_SLATS_THING_TYPES_UIDS = Set.of(THING_TYPE_ROLLERSHUTTER_SLATS);
    public static final Set<ThingTypeUID> BISTABIEL_THING_TYPES_UIDS = Set.of(THING_TYPE_ON_OFF_LIGHT);
    public static final Set<ThingTypeUID> THERMOSTAT_THING_TYPES_UIDS = Set.of(THING_TYPE_THERMOSTAT);
    public static final Set<ThingTypeUID> DIMMER_THING_TYPES_UIDS = Set.of(THING_TYPE_ON_OFF_LIGHT,
            THING_TYPE_DIMMABLE_LIGHT);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ON_OFF_LIGHT,
            THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_THERMOSTAT, THING_TYPE_SCENE, THING_TYPE_CO2,
            THING_TYPE_ROLLERSHUTTER, THING_TYPE_ROLLERSHUTTER_SLATS);

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_SCENE = "scene";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_MEASURED = "measured";
    public static final String CHANNEL_SETPOINT = "setpoint";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_CO2 = "co2";
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public static final String CHANNEL_SLATS = "slats";

    // Thing config properties
    public static final String CONFIG_BISTABIEL_ID = "bistabielId";
    public static final String CONFIG_DIMMER_ID = "dimmerId";
    public static final String CONFIG_THERMOSTAT_ID = "thermostatId";
    public static final String CONFIG_SCENE_ID = "sceneId";
    public static final String CONFIG_CO2_ID = "co2Id";
    public static final String CONFIG_ROLLERSHUTTER_ID = "rolId";
    public static final String CONFIG_STEP_VALUE = "step";
}
