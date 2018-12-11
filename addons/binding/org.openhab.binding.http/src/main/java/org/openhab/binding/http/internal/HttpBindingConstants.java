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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.time.Duration;

/**
 * Constants for the HTTP binding.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
public class HttpBindingConstants {
    public static final String BINDING_ID = "http";

    public static final ThingTypeUID THING_TYPE_HTTP = new ThingTypeUID(BINDING_ID, "http");

    public static final String CHANNEL_TYPE_ID_IMAGE = "state-image";

    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_STATE_REFRESH_INTERVAL = Duration.ofSeconds(60);
    public static final HttpMethod DEFAULT_COMMAND_METHOD = HttpMethod.POST;
    public static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=utf-8";

    public static final int MAX_RESPONSE_BODY_LEN = 1024;
    public static final int MAX_IMAGE_RESPONSE_BODY_LEN = 100 * 1024 * 1024;

    private HttpBindingConstants() {}
}
