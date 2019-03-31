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
package org.openhab.binding.net.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link NetBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class NetBindingConstants {

    private static final String BINDING_ID = "net";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_UDP_SERVER = new ThingTypeUID(BINDING_ID, "udp-server");
    public static final ThingTypeUID BRIDGE_TCP_SERVER = new ThingTypeUID(BINDING_ID, "tcp-server");
    public static final ThingTypeUID BRIDGE_HTTP_SERVER = new ThingTypeUID(BINDING_ID, "http-server");

    public static final ThingTypeUID THING_DATA_HANDLER = new ThingTypeUID(BINDING_ID, "data-handler");

    /**
     * Presents all supported Bridge types by net binding.
     */
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES = ImmutableSet.of(BRIDGE_UDP_SERVER, BRIDGE_TCP_SERVER,
            BRIDGE_HTTP_SERVER);

    /**
     * Presents all supported Thing types by net binding.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.of(THING_DATA_HANDLER);

    // List of all Channel ids
    public static final String CHANNEL_DATA_RECEIVED = "dataReceived";

    // Supported charsets
    public static final String CHARSET_ASCII = "ASCII";
    public static final String CHARSET_BINARY = "BINARY";
    public static final String CHARSET_HEXASTRING = "HEXASTRING";
    public static final String CHARSET_UTF8 = "UTF8";

    public static final String CHANNEL_PARAM_TRANSFORM = "transform";

    public static final Map<String, List<Class<? extends State>>> CHANNEL_STATE_TYPES;
    static {
        final Map<String, List<Class<? extends State>>> channelStateTypes = new HashMap<>();
        channelStateTypes.put("color", Arrays.asList(HSBType.class, PercentType.class, OnOffType.class));
        channelStateTypes.put("contact", Collections.singletonList(OpenClosedType.class));
        channelStateTypes.put("datetime", Collections.singletonList(DateTimeType.class));
        channelStateTypes.put("dimmer", Arrays.asList(PercentType.class, OnOffType.class));
        channelStateTypes.put("location", Collections.singletonList(PointType.class));
        // channelStateTypes.put("number", Arrays.asList(QuantityType.class, DecimalType.class));
        channelStateTypes.put("number", Arrays.asList(DecimalType.class));
        channelStateTypes.put("player", Arrays.asList(PlayPauseType.class, RewindFastforwardType.class));
        channelStateTypes.put("rollershutter", Arrays.asList(PercentType.class, UpDownType.class));
        channelStateTypes.put("string", Collections.singletonList(StringType.class));
        channelStateTypes.put("switch", Collections.singletonList(OnOffType.class));
        CHANNEL_STATE_TYPES = Collections.unmodifiableMap(channelStateTypes);
    }
}
