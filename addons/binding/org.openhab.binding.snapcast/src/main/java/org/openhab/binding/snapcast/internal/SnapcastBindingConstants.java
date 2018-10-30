/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SnapcastBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public class SnapcastBindingConstants {

    private static final String BINDING_ID = "snapcast";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final ThingTypeUID THING_TYPE_CLIENT = new ThingTypeUID(BINDING_ID, "client");

    // List of all Channel ids
    public static final String CHANNEL_SERVER_STREAMS = "streams";
    public static final String CHANNEL_SERVER_STREAMS_PLAYING = "streamsPlaying";
    public static final String CHANNEL_SERVER_STREAMS_IDLE = "streamsIdle";
    public static final String CHANNEL_CLIENT_NAME = "name";
    public static final String CHANNEL_CLIENT_VOLUME = "volume";
    public static final String CHANNEL_CLIENT_MUTE = "mute";
    public static final String CHANNEL_CLIENT_LATENCY = "latency";
    public static final String CHANNEL_STREAM_ID = "stream";
    public static final String CHANNEL_STREAM_STATUS = "streamStatus";

    // List of all Configuration ids
    public static final String CONFIG_SERVER_HOST = "host";
    public static final String CONFIG_SERVER_PORT = "port";
    public static final String CONFIG_CLIENT_ID = "id";

    // List of stream states
    public static final String STREAM_STATE_PLAYING = "playing";
    public static final String STREAM_STATE_IDLE = "idle";
    public static final String STREAM_STATE_DELIMITER = ",";

}
