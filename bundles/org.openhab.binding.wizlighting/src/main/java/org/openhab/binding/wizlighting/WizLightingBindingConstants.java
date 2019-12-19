/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WizLightingBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
public class WizLightingBindingConstants {

    /**
     * The binding id.
     */
    public static final String BINDING_ID = "wizlighting";

    /**
     * List of all Thing Type UIDs.
     */
    public static final ThingTypeUID THING_TYPE_WIZ_BULB = new ThingTypeUID(BINDING_ID, "bulb");

    /**
     * List of all Channel ids
     */
    public static final String BULB_COLOR_CHANNEL_ID = "color";
    public static final String BULB_SCENE_CHANNEL_ID = "scene";
    public static final String BULB_SPEED_CHANNEL_ID = "speed";

    /**
     * The supported thing types.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(THING_TYPE_WIZ_BULB)));

    // -------------- Configuration arguments ----------------
    /**
     * Mac address configuration argument key.
     */
    public static final String BULB_MAC_ADDRESS_ARG = "bulbMacAddress";
    /**
     * Wifi socket update interval configuration argument key.
     */
    public static final String UPDATE_INTERVAL_ARG = "updateInterval";
    /**
     * Host address configuration argument key.
     */
    public static final String BULB_IP_ADDRESS_ARG = "bulbIpAddress";

    public static final String HOME_ID_ARG = "homeId";

    // -------------- Default values ----------------
    /**
     * Default Wifi socket refresh interval.
     */
    public static final long DEFAULT_REFRESH_INTERVAL = 60;

    /**
     * Default Wifi socket default UDP port.
     */
    public static final int BULB_DEFAULT_UDP_PORT = 38899;

    /**
     * Default listener socket default UDP port.
     */
    public static final int LISTENER_DEFAULT_UDP_PORT = 38900;

}
