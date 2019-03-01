/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NanoleafBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class NanoleafBindingConstants {

    private static final String BINDING_ID = "nanoleaf";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_LIGHT_PANEL = new ThingTypeUID(BINDING_ID, "lightpanel");

    // Controller configuration settings
    public static final String CONFIG_ADDRESS = "address";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_AUTH_TOKEN = "authToken";

    // Panel configuration settings
    public static final String CONFIG_PANEL_ID = "id";

    // List of controller channels
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "colorTemperature";
    public static final String CHANNEL_COLOR_TEMPERATURE_ABS = "colorTemperatureAbs";
    public static final String CHANNEL_COLOR_MODE = "colorMode";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_RHYTHM_STATE = "rhythmState";
    public static final String CHANNEL_RHYTHM_ACTIVE = "rhythmActive";
    public static final String CHANNEL_RHYTHM_MODE = "rhythmMode";

    // List of light panel channels
    public static final String CHANNEL_PANEL_COLOR = "panelColor";

    // Nanoleaf OpenAPI URLs
    public static final String API_V1_BASE_URL = "/api/v1";
    public static final String API_GET_CONTROLLER_INFO = "/";
    public static final String API_ADD_USER = "/new";
    public static final String API_DELETE_USER = "";
    public static final String API_SET_VALUE = "/state";
    public static final String API_EFFECT = "/effects";
    public static final String API_RHYTHM_MODE = "/rhythm/rhythmMode";
    public static final String API_MIN_FW_VER = "1.5.0";

    // mDNS discovery service type
    // see http://forum.nanoleaf.me/docs/openapi#_gf9l5guxt8r0
    public static final String SERVICE_TYPE = "_nanoleafapi._tcp.local.";

    // Effect/scene name for static color
    public static final String EFFECT_NAME_STATIC_COLOR = "*Dynamic*";

    // Color channels increase/decrease brightness step size
    public final static int BRIGHTNESS_STEP_SIZE = 5;
}
