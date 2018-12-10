/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.http.model.HttpHandlerConfig;

import java.time.Duration;

public class HttpBindingConstants {
    public static final String BINDING_ID = "http";

    public static final ThingTypeUID THING_TYPE_COLOR = new ThingTypeUID(BINDING_ID, "color");
    public static final ThingTypeUID THING_TYPE_CONTACT = new ThingTypeUID(BINDING_ID, "contact");
    public static final ThingTypeUID THING_TYPE_DATETIME = new ThingTypeUID(BINDING_ID, "datetime");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_IMAGE = new ThingTypeUID(BINDING_ID, "image");
    public static final ThingTypeUID THING_TYPE_LOCATION = new ThingTypeUID(BINDING_ID, "location");
    public static final ThingTypeUID THING_TYPE_NUMBER = new ThingTypeUID(BINDING_ID, "number");
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");
    public static final ThingTypeUID THING_TYPE_STRING = new ThingTypeUID(BINDING_ID, "string");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");

    public static final String CHANNEL_STATE = "state";

    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_STATE_REFRESH_INTERVAL = Duration.ofSeconds(60);
    public static final HttpHandlerConfig.Method DEFAULT_COMMAND_METHOD = HttpHandlerConfig.Method.POST;
    public static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=utf-8";

    public static final int MAX_RESPONSE_BODY_LEN = 1024;
    public static final int MAX_IMAGE_RESPONSE_BODY_LEN = 100 * 1024 * 1024;

    private HttpBindingConstants() {}
}
