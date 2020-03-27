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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.webthings.internal.handler.WebThingsWebThingHandler;

/**
 * The {@link WebThingsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sven Schneider - Initial contribution
 */
@NonNullByDefault
public class WebThingsBindingConstants {

    public static final String BINDING_ID = "webthings";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WEBTHING = new ThingTypeUID(BINDING_ID, "webthing");

    // List of all supported Thing Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream.of(THING_TYPE_WEBTHING).collect(Collectors.toSet()));

    // List of all Channel IDs
    public static final String CHANNEL_UPDATE = "updateChannel";

    // List of all other constants
    public static final Map<String, WebThingsWebThingHandler> WEBTHING_HANDLER_LIST = new HashMap<String, WebThingsWebThingHandler>();
}
