/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constants for the HTTP binding.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
public class HttpBindingConstants {
    private static final String BINDING_ID = "http";

    static final ThingTypeUID THING_TYPE_HTTP = new ThingTypeUID(BINDING_ID, "http");

    static final String CHANNEL_TYPE_ID_IMAGE = "image";

    static final Map<String, List<Class<? extends State>>> CHANNEL_STATE_TYPES;
    static {
        final Map<String, List<Class<? extends State>>> channelStateTypes = new HashMap<>();
        channelStateTypes.put("color", Arrays.asList(HSBType.class, PercentType.class, OnOffType.class));
        channelStateTypes.put("contact", Collections.singletonList(OpenClosedType.class));
        channelStateTypes.put("datetime", Collections.singletonList(DateTimeType.class));
        channelStateTypes.put("percent", Arrays.asList(PercentType.class, OnOffType.class));
        channelStateTypes.put("location", Collections.singletonList(PointType.class));
        channelStateTypes.put("number", Arrays.asList(QuantityType.class, DecimalType.class));
        channelStateTypes.put("player", Arrays.asList(PlayPauseType.class, RewindFastforwardType.class));
        channelStateTypes.put("rollershutter", Arrays.asList(PercentType.class, UpDownType.class));
        channelStateTypes.put("string", Collections.singletonList(StringType.class));
        channelStateTypes.put("onoff", Collections.singletonList(OnOffType.class));
        CHANNEL_STATE_TYPES = Collections.unmodifiableMap(channelStateTypes);
    }

    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_STATE_REFRESH_INTERVAL = Duration.ofSeconds(60);
    public static final HttpMethod DEFAULT_COMMAND_METHOD = HttpMethod.POST;
    public static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=utf-8";

    static final int MAX_RESPONSE_BODY_LEN = 1024;
    static final int MAX_IMAGE_RESPONSE_BODY_LEN = 100 * 1024 * 1024;

    private HttpBindingConstants() {}
}
