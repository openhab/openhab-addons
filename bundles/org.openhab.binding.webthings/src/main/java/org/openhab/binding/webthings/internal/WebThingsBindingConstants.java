/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthings.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.webthings.internal.handler.WebThingsConnectorHandler;
import org.openhab.binding.webthings.internal.handler.WebThingsServerHandler;

/**
 * The {@link WebThingsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sven Schneider - Initial contribution
 */
@NonNullByDefault
public class WebThingsBindingConstants {

    private static final String BINDING_ID = "webthings";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONNECTOR = new ThingTypeUID(BINDING_ID, "connector");
    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");

    // List of all Channel ids
    public static final String CHANNEL_UPDATE = "updateChannel";
    public static final String CHANNEL_PORT = "portChannel";
    public static final String CHANNEL_THINGS = "things";

    // List of all other constants
    public static final Map<String, WebThingsConnectorHandler> CONNECTOR_HANDLER_LIST = new HashMap<String, WebThingsConnectorHandler>();
    public static final Map<Integer, WebThingsServerHandler> SERVER_HANDLER_LIST = new HashMap<Integer, WebThingsServerHandler>();
}
