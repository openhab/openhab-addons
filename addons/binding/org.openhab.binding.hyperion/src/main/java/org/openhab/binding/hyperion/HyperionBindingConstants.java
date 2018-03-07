/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    // List of all Channel ids
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_CLEAR = "clear";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_BLACKBORDER = "blackborder";
    public static final String CHANNEL_SMOOTHING = "smoothing";
    public static final String CHANNEL_KODICHECKER = "kodichecker";
    public static final String CHANNEL_FORWARDER = "forwarder";
    public static final String CHANNEL_UDPLISTENER = "udplistener";
    public static final String CHANNEL_BOBLIGHTSERVER = "boblightserver";
    public static final String CHANNEL_GRABBER = "grabber";
    public static final String CHANNEL_V4L = "v4l";
    public static final String CHANNEL_LEDDEVICE = "leddevice";
    public static final String CHANNEL_HYPERION_ENABLED = "hyperionenabled";

    // Hyperion components
    public static final String COMPONENT_BLACKBORDER = "BLACKBORDER";
    public static final String COMPONENT_SMOOTHING = "SMOOTHING";
    public static final String COMPONENT_KODICHECKER = "KODICHECKER";
    public static final String COMPONENT_FORWARDER = "FORWARDER";
    public static final String COMPONENT_UDPLISTENER = "UDPLISTENER";
    public static final String COMPONENT_BOBLIGHTSERVER = "BOBLIGHTSERVER";
    public static final String COMPONENT_GRABBER = "GRABBER";
    public static final String COMPONENT_V4L = "V4L";
    public static final String COMPONENT_LEDDEVICE = "LEDDEVICE";

    // List of all properties
    public static final String PROP_HOST = "host";
    public static final String PROP_PORT = "port";
    public static final String PROP_PRIORITY = "priority";
    public static final String PROP_POLL_FREQUENCY = "poll_frequency";
    public static final String PROP_ORIGIN = "origin";

    // config
    public static final String HOST = "host";
    public static final String PORT = "port";

    // thing-types
    public static final String SERVER_V1 = "serverV1";
    public static final String SERVER_NG = "serverNG";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERVER_V1 = new ThingTypeUID(BINDING_ID, SERVER_V1);
    public static final ThingTypeUID THING_TYPE_SERVER_NG = new ThingTypeUID(BINDING_ID, SERVER_NG);

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SERVER_V1);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SERVER_NG);
    }

}
