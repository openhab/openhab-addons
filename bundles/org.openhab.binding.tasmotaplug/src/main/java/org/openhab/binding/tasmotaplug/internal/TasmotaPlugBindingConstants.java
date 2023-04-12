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
package org.openhab.binding.tasmotaplug.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TasmotaPlugBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class TasmotaPlugBindingConstants {
    public static final String BINDING_ID = "tasmotaplug";

    public static final String GET_POWER = "/cm?cmnd=Power";
    public static final String SET_POWER = "/cm?cmnd=Power%20";

    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String BLANK = "";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLUG = new ThingTypeUID(BINDING_ID, "plug");

    // List of all Channel id's
    public static final String POWER = "power";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_PLUG);
    public static final Set<String> SUPPORTED_CHANNEL_IDS = Collections.singleton(POWER);
}
