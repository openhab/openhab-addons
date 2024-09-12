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
package org.openhab.binding.pulseaudio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PulseaudioBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tobias Bräutigam - Initial contribution
 * @author Miguel Álvarez - Add new configuration options to sink and source
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

    public static final String DEVICE_PARAMETER_NAME_OR_DESCRIPTION = "name";
    public static final String DEVICE_PARAMETER_ADDITIONAL_FILTERS = "additionalFilters";
    public static final String DEVICE_PARAMETER_AUDIO_SINK_ACTIVATION = "activateSimpleProtocolSink";
    public static final String DEVICE_PARAMETER_AUDIO_SOURCE_ACTIVATION = "activateSimpleProtocolSource";
    public static final String DEVICE_PARAMETER_AUDIO_SOURCE_RATE = "simpleProtocolSourceRate";
    public static final String DEVICE_PARAMETER_AUDIO_SOURCE_FORMAT = "simpleProtocolSourceFormat";
    public static final String DEVICE_PARAMETER_AUDIO_SOURCE_CHANNELS = "simpleProtocolSourceChannels";
    public static final String DEVICE_PARAMETER_AUDIO_SOCKET_SO_TIMEOUT = "simpleProtocolSOTimeout";
    public static final String DEVICE_PARAMETER_IDLE_MODULES = "simpleProtocolIdleModules";
    public static final String DEVICE_PARAMETER_MIN_PORT = "simpleProtocolModuleMinPort";
    public static final String DEVICE_PARAMETER_MAX_PORT = "simpleProtocolModuleMaxPort";

    public static final String MODULE_SIMPLE_PROTOCOL_TCP_NAME = "module-simple-protocol-tcp";
}
