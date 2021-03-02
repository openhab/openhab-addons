/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.flicbutton.internal;

import java.util.Collections;
import java.util.Set;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FlicButtonBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicButtonBindingConstants {

    public static final String BINDING_ID = "flicbutton";

    // List of all Thing Type UIDs
    public final static ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "flicd-bridge");
    public final static ThingTypeUID FLICBUTTON_THING_TYPE = new ThingTypeUID(BINDING_ID, "button");

    public final static Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(FLICBUTTON_THING_TYPE);

    // List of all configuration options
    public static final String CONFIG_HOST_NAME = "hostname";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_ADDRESS = "address";

    // List of all Channel ids
    public final static String CHANNEL_ID_RAWBUTTON_EVENTS = "rawbutton";
    public final static String CHANNEL_ID_BUTTON_EVENTS = "button";
    public final static String CHANNEL_ID_BATTERY_LEVEL = "battery-level";

    // Other stuff
    public final static int BUTTON_OFFLINE_GRACE_PERIOD_SECONDS = 60;
}
