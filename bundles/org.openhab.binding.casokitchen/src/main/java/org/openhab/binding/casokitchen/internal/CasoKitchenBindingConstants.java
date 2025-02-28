/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.casokitchen.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;

/**
 * The {@link CasoKitchenBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CasoKitchenBindingConstants {
    private static final String BINDING_ID = "casokitchen";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WINECOOLER = new ThingTypeUID(BINDING_ID, "winecooler-2z");

    // List of all Channel Group ids
    public static final String TOP = "top";
    public static final String BOTTOM = "bottom";
    public static final String GENERIC = "generic";

    // List of all Channel ids
    public static final String TEMPERATURE = "temperature";
    public static final String TARGET_TEMPERATURE = "set-temperature";
    public static final String POWER = "power";
    public static final String LIGHT = "light-switch";
    public static final String HINT = "hint";
    public static final String LAST_UPDATE = "last-update";

    public static final int MINIMUM_REFRESH_INTERVAL_MIN = 5;
    public static final String EMPTY = "";

    public static final String BASE_URL = "https://publickitchenapi.casoapp.com";
    public static final String LIGHT_URL = BASE_URL + "/api/v1.1/Winecooler/SetLight";
    public static final String STATUS_URL = BASE_URL + "/api/v1.1/Winecooler/Status";
    public static final String HTTP_HEADER_API_KEY = "x-api-key";

    public static final Gson GSON = new Gson();
}
