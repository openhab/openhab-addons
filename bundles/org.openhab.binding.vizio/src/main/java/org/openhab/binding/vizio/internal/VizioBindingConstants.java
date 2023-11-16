/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.vizio.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VizioBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class VizioBindingConstants {
    public static final String BINDING_ID = "vizio";
    public static final String PROPERTY_UUID = "uuid";
    public static final String PROPERTY_HOST_NAME = "hostName";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_AUTH_TOKEN = "authToken";
    public static final String PROPERTY_APP_LIST_JSON = "appListJson";
    public static final String EMPTY = "";
    public static final String MODIFY_STRING_SETTING_JSON = "{\"REQUEST\": \"MODIFY\",\"VALUE\": \"%s\",\"HASHVAL\": %d}";
    public static final String MODIFY_INT_SETTING_JSON = "{\"REQUEST\": \"MODIFY\",\"VALUE\": %d,\"HASHVAL\": %d}";
    public static final String UNKNOWN_APP_STR = "Unknown app_id: %d, name_space: %d";
    public static final String ON = "ON";
    public static final String OFF = "OFF";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VIZIO_TV = new ThingTypeUID(BINDING_ID, "vizio_tv");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_VIZIO_TV);

    // List of all Channel id's
    public static final String POWER = "power";
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";
    public static final String SOURCE = "source";
    public static final String ACTIVE_APP = "activeApp";
    public static final String CONTROL = "control";
    public static final String BUTTON = "button";
}
