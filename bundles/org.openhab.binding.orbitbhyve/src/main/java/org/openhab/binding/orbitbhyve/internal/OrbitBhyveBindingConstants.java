/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.orbitbhyve.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OrbitBhyveBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class OrbitBhyveBindingConstants {

    public static final String BINDING_ID = "orbitbhyve";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_SPRINKLER = new ThingTypeUID(BINDING_ID, "sprinkler");

    // List of all Channel ids
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_SMART_WATERING = "smart_watering";
    public static final String CHANNEL_NEXT_START = "next_start";
    public static final String CHANNEL_RAIN_DELAY = "rain_delay";
    public static final String CHANNEL_WATERING_TIME = "watering_time";

    // Constants
    public static final String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36";
    public static final String BHYVE_API = "https://api.orbitbhyve.com/v1/";
    public static final String BHYVE_SESSION = BHYVE_API + "session";
    public static final String BHYVE_DEVICES = BHYVE_API + "devices";
    public static final String BHYVE_PROGRAMS = BHYVE_API + "sprinkler_timer_programs";
    public static final String BHYVE_WS_URL = "wss://api.orbitbhyve.com/v1/events";
    public static final int BHYVE_TIMEOUT = 5;
}
