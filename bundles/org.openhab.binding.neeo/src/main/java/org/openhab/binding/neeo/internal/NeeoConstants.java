/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link NeeoConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoConstants {

    /** The main binding */
    public static final String BINDING_ID = "neeo";

    /** The various bridge/thing UIDs */
    public static final ThingTypeUID BRIDGE_TYPE_BRAIN = new ThingTypeUID(BINDING_ID, "brain");
    public static final ThingTypeUID BRIDGE_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    /** The MDNS type for the NEEO brain */
    public static final String NEEO_MDNS_TYPE = "_neeo._tcp.local.";

    /** Various config related */
    public static final String CONFIG_IPADDRESS = "ipAddress";
    public static final String CONFIG_ENABLEFORWARDACTIONS = "enableForwardActions";
    public static final String CONFIG_REFRESH_POLLING = "refreshPolling";
    public static final String CONFIG_DEVICEKEY = "deviceKey";
    public static final String CONFIG_ROOMKEY = "roomKey";
    public static final String CONFIG_EXCLUDE_THINGS = "excludeThings";

    /** Brain channels */
    public static final String CHANNEL_BRAIN_FOWARDACTIONS = "forwardActions";

    /** The various room channel constants */
    public static final String ROOM_CHANNEL_NAME = "name";
    public static final String ROOM_CHANNEL_TYPE = "type";
    public static final String ROOM_CHANNEL_ENABLED = "enabled";
    public static final String ROOM_CHANNEL_CONFIGURED = "configured";
    public static final String ROOM_CHANNEL_STATUS = "status";
    public static final String ROOM_CHANNEL_CURRENTSTEP = "currentStep";
    public static final String ROOM_GROUP_STATE_ID = "state";
    public static final String ROOM_GROUP_SCENARIO_ID = "scenario";
    public static final String ROOM_GROUP_RECIPE_ID = "recipe";
    public static final ChannelTypeUID ROOM_STATE_CURRENTSTEP_UID = new ChannelTypeUID(BINDING_ID,
            "room-state-currentstep");
    public static final ChannelTypeUID ROOM_SCENARIO_NAME_UID = new ChannelTypeUID(BINDING_ID, "room-scenario-name");
    public static final ChannelTypeUID ROOM_SCENARIO_CONFIGURED_UID = new ChannelTypeUID(BINDING_ID,
            "room-scenario-configured");
    public static final ChannelTypeUID ROOM_SCENARIO_STATUS_UID = new ChannelTypeUID(BINDING_ID,
            "room-scenario-status");
    public static final ChannelTypeUID ROOM_RECIPE_NAME_UID = new ChannelTypeUID(BINDING_ID, "room-recipe-name");
    public static final ChannelTypeUID ROOM_RECIPE_TYPE_UID = new ChannelTypeUID(BINDING_ID, "room-recipe-type");
    public static final ChannelTypeUID ROOM_RECIPE_ENABLED_UID = new ChannelTypeUID(BINDING_ID, "room-recipe-enabled");
    public static final ChannelTypeUID ROOM_RECIPE_STATUS_UID = new ChannelTypeUID(BINDING_ID, "room-recipe-status");

    /** The various device channel constants */
    public static final String DEVICE_CHANNEL_STATUS = "status";
    public static final String DEVICE_GROUP_MACROS_ID = "macros";
    public static final ChannelTypeUID DEVICE_MACRO_STATUS_UID = new ChannelTypeUID(BINDING_ID, "device-macros-status");

    public static final String WEBAPP_FORWARDACTIONS = "/neeo/binding/{brainid}/forwardactions";

    /** The default port the brain listens on. */
    public static final int DEFAULT_BRAIN_PORT = 3000;
    public static final int DEFAULT_BRAIN_HTTP_PORT = 8080;

    /** The default protocol for the brain. */
    public static final String PROTOCOL = "http://";

    /** The brain API constants */
    private static final String NEEO_VERSION = "/v1";
    public static final String FORWARD_ACTIONS = NEEO_VERSION + "/forwardactions";
    public static final String PROJECTS_HOME = NEEO_VERSION + "/projects/home";
    public static final String GET_ACTIVESCENARIOS = PROJECTS_HOME + "/activescenariokeys";
    public static final String GET_ROOM = PROJECTS_HOME + "/rooms/{roomkey}";
    public static final String EXECUTE_RECIPE = PROJECTS_HOME + "/rooms/{roomkey}/recipes/{recipekey}/execute";
    public static final String STOP_SCENARIO = PROJECTS_HOME + "/rooms/{roomkey}/scenarios/{scenariokey}/poweroff";
    public static final String TRIGGER_MACRO = PROJECTS_HOME
            + "/rooms/{roomkey}/devices/{devicekey}/macros/{macrokey}/trigger";
}
