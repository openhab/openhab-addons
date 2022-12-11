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
package org.openhab.binding.nanoleaf.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NanoleafBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class NanoleafBindingConstants {

    public static final String BINDING_ID = "nanoleaf";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_LIGHT_PANEL = new ThingTypeUID(BINDING_ID, "lightpanel");

    // Controller configuration settings
    public static final String CONFIG_ADDRESS = "address";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_AUTH_TOKEN = "authToken";
    public static final String CONFIG_DEVICE_TYPE_CANVAS = "canvas";
    public static final String CONFIG_DEVICE_TYPE_LIGHTPANELS = "lightPanels";

    // Panel configuration settings
    public static final String CONFIG_PANEL_ID = "id";
    public static final String CONTROLLER_PANEL_ID = "-1";

    // List of controller channels
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "colorTemperature";
    public static final String CHANNEL_COLOR_TEMPERATURE_ABS = "colorTemperatureAbs";
    public static final String CHANNEL_COLOR_MODE = "colorMode";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_RHYTHM_STATE = "rhythmState";
    public static final String CHANNEL_RHYTHM_ACTIVE = "rhythmActive";
    public static final String CHANNEL_RHYTHM_MODE = "rhythmMode";
    public static final String CHANNEL_SWIPE = "swipe";
    public static final String CHANNEL_SWIPE_EVENT_UP = "UP";
    public static final String CHANNEL_SWIPE_EVENT_DOWN = "DOWN";
    public static final String CHANNEL_SWIPE_EVENT_LEFT = "LEFT";
    public static final String CHANNEL_SWIPE_EVENT_RIGHT = "RIGHT";
    public static final String CHANNEL_LAYOUT = "layout";
    public static final String CHANNEL_VISUAL_STATE = "visualState";

    // List of light panel channels
    public static final String CHANNEL_PANEL_COLOR = "color";
    public static final String CHANNEL_PANEL_TAP = "tap";

    // Nanoleaf OpenAPI URLs
    public static final String API_V1_BASE_URL = "/api/v1";
    public static final String API_GET_CONTROLLER_INFO = "/";
    public static final String API_ADD_USER = "/new";
    public static final String API_EVENTS = "/events";
    public static final String API_DELETE_USER = "";
    public static final String API_SET_VALUE = "/state";
    public static final String API_EFFECT = "/effects";
    public static final String API_RHYTHM_MODE = "/rhythm/rhythmMode";

    // Nanoleaf model IDs and minimum required firmware versions
    public static final String API_MIN_FW_VER_LIGHTPANELS = "1.5.0";
    public static final String API_MIN_FW_VER_CANVAS = "1.1.0";
    public static final String MODEL_ID_LIGHTPANELS = "NL22";

    public static final List<String> MODELS_WITH_TOUCHSUPPORT = Arrays.asList("NL29", "NL42", "NL47", "NL48", "NL52");
    public static final String DEVICE_TYPE_LIGHTPANELS = "lightPanels";
    public static final String DEVICE_TYPE_TOUCHSUPPORT = "canvas"; // we need to keep this enum for backward
                                                                    // compatibility even though not only canvas type
                                                                    // support touch

    // mDNS discovery service type
    // see http://forum.nanoleaf.me/docs/openapi#_gf9l5guxt8r0
    public static final String SERVICE_TYPE = "_nanoleafapi._tcp.local.";

    // Effect/scene name for static color
    public static final String EFFECT_NAME_STATIC_COLOR = "*Static*";
    public static final String EFFECT_NAME_SOLID_COLOR = "*Solid*";

    // Color channels increase/decrease brightness step size
    public static final int BRIGHTNESS_STEP_SIZE = 5;

    // Layout rendering
    public static final int LAYOUT_LIGHT_RADIUS = 8;
    public static final int LAYOUT_BORDER_WIDTH = 30;
}
