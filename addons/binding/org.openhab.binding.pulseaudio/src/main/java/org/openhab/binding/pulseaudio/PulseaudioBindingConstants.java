/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pulseaudio;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PulseaudioBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@NonNullByDefault
public class PulseaudioBindingConstants {

    public static final String BINDING_ID = "pulseaudio";

    // List of all Thing Type UIDs
    public static final ThingTypeUID COMBINED_SINK_THING_TYPE = new ThingTypeUID(BINDING_ID, "combinedSink");
    public static final ThingTypeUID SINK_THING_TYPE = new ThingTypeUID(BINDING_ID, "sink");
    public static final ThingTypeUID SOURCE_THING_TYPE = new ThingTypeUID(BINDING_ID, "source");
    public static final ThingTypeUID SINK_INPUT_THING_TYPE = new ThingTypeUID(BINDING_ID, "sinkInput");
    public static final ThingTypeUID SOURCE_OUTPUT_THING_TYPE = new ThingTypeUID(BINDING_ID, "sourceOutput");

    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Channel ids
    public static final String VOLUME_CHANNEL = "volume";
    public static final String MUTE_CHANNEL = "mute";
    public static final String STATE_CHANNEL = "state";
    public static final String SLAVES_CHANNEL = "slaves";
    public static final String ROUTE_TO_SINK_CHANNEL = "routeToSink";

    // List of all Parameters
    public static final String BRIDGE_PARAMETER_HOST = "host";
    public static final String BRIDGE_PARAMETER_PORT = "port";
    public static final String BRIDGE_PARAMETER_REFRESH_INTERVAL = "refresh";

    public static final String DEVICE_PARAMETER_NAME = "name";

    public static Map<String, Boolean> TYPE_FILTERS = new HashMap<String, Boolean>();

    static {
        TYPE_FILTERS.put(SINK_THING_TYPE.getId(), true);
        TYPE_FILTERS.put(SINK_INPUT_THING_TYPE.getId(), false);
        TYPE_FILTERS.put(SOURCE_THING_TYPE.getId(), false);
        TYPE_FILTERS.put(SOURCE_OUTPUT_THING_TYPE.getId(), false);
    }
}
