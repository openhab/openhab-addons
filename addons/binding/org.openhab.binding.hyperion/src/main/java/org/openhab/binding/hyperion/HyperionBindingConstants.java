/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HyperionBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Walters - Initial contribution
 */
public class HyperionBindingConstants {

    public static final String BINDING_ID = "hyperion";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SERVER);
    }

    // List of all Channel ids
    public final static String CHANNEL_BRIGHTNESS = "brightness";
    public final static String CHANNEL_COLOR = "color";
    public final static String CHANNEL_CLEAR_ALL = "clear_all";
    public final static String CHANNEL_CLEAR = "clear";
    public final static String CHANNEL_EFFECT = "effect";

    // List of all properties
    public final static String PROP_HOST = "host";
    public final static String PROP_PORT = "port";
    public final static String PROP_PRIORITY = "priority";
    public final static String PROP_POLL_FREQUENCY = "poll_frequency";

    // config
    public static final String HOST = "host";
    public static final String PORT = "port";

}
