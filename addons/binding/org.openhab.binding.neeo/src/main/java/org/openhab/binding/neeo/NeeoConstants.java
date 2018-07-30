/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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

    /** The bridge thing uid */
    public static final ThingTypeUID BRIDGE_TYPE_BRAIN = new ThingTypeUID(BINDING_ID, "brain");

    /** The MDNS type for the NEEO brain */
    public static final String NEEO_MDNS_TYPE = "_neeo._tcp.local.";

    /** Various config related */
    public static final String CONFIG_IPADDRESS = "ipAddress";
    public static final String CONFIG_ENABLEFORWARDACTIONS = "enableForwardActions";
    public static final String CONFIG_DESCRIPTION_URI_ROOM = "thing-type:neeo:room:config";
    public static final String CONFIG_DESCRIPTION_URI_DEVICE = "thing-type:neeo:device:config";
    public static final String CONFIG_REFRESH_POLLING = "refreshPolling";
    public static final String CONFIG_DEVICEKEY = "deviceKey";
    public static final String CONFIG_ROOMKEY = "roomKey";
    public static final String CONFIG_EXCLUDE_THINGS = "excludeThings";

    /** Brain channels */
    public static final String CHANNEL_BRAIN_FOWARDACTIONS = "forwardActions";

    /** Various room channel grouping constants */
    public static final String ROOM_CHANNEL_GROUP_STATE = "room-state";
    public static final String ROOM_CHANNEL_GROUP_STATEID = "state";
    public static final String ROOM_CHANNEL_GROUP_RECIPE = "room-recipe";
    public static final String ROOM_CHANNEL_GROUP_RECIPEID = "recipe";
    public static final String ROOM_CHANNEL_GROUP_SCENARIO = "room-scenario";
    public static final String ROOM_CHANNEL_GROUP_SCENARIOID = "scenario";

    /** The various room channel constants */
    public static final String ROOM_CHANNEL_NAME = "name";
    public static final String ROOM_CHANNEL_TYPE = "type";
    public static final String ROOM_CHANNEL_ENABLED = "enabled";
    public static final String ROOM_CHANNEL_CONFIGURED = "configured";
    public static final String ROOM_CHANNEL_STATUS = "status";
    public static final String ROOM_CHANNEL_CURRENTSTEP = "currentStep";

    public static final String DEVICE_GROUP_MACROS = "device-macros";
    public static final String DEVICE_GROUP_MACROSID = "macros";
    public static final String DEVICE_CHANNEL_MACRO_STATUS = "device-macros-status";

    /** Discovery timeouts (in seconds) */
    public static final int BRAIN_DISCOVERY_TIMEOUT = 10;
    public static final int ROOM_DISCOVERY_TIMEOUT = 5;
    public static final int DEVICE_DISCOVERY_TIMEOUT = 5;

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

    /** Constants for scans in seconds */
    public static final int SCAN_ROOMS = 2000;
    public static final int SCAN_DEVICES = 1000;

    /** Constants used to store various types */
    public static final String FILENAME_THINGTYPES = ConfigConstants.getUserDataFolder() + File.separator + "neeo"
            + File.separator + "neeothingtypes.json";
}
