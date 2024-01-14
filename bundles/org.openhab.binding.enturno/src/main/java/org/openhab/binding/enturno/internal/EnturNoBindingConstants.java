/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.enturno.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EnturNoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michal Kloc - Initial contribution
 */
@NonNullByDefault
public class EnturNoBindingConstants {

    private static final String BINDING_ID = "enturno";

    public static final String TIME_ZONE = "Europe/Oslo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID LINESTOP = new ThingTypeUID(BINDING_ID, "linestop");

    // List of all channel groups
    public static final String CHANNEL_GROUP_STOP_PLACE = "stopPlace";
    public static final String CHANNEL_GROUP_DIRECTION_1 = "Direction01";
    public static final String CHANNEL_GROUP_DIRECTION_2 = "Direction02";

    // List of all channels
    public static final String CHANNEL_STOP_ID = "id";
    public static final String CHANNEL_STOP_NAME = "name";
    public static final String CHANNEL_STOP_TRANSPORT_MODE = "transportMode";
    public static final String CHANNEL_LINE_CODE = "lineCode";
    public static final String CHANNEL_DEPARTURE_01 = "departure01";
    public static final String CHANNEL_DEPARTURE_02 = "departure02";
    public static final String CHANNEL_DEPARTURE_03 = "departure03";
    public static final String CHANNEL_DEPARTURE_04 = "departure04";
    public static final String CHANNEL_DEPARTURE_05 = "departure05";
    public static final String ESTIMATED_FLAG_01 = "estimatedFlag01";
    public static final String ESTIMATED_FLAG_02 = "estimatedFlag02";
    public static final String ESTIMATED_FLAG_03 = "estimatedFlag03";
    public static final String ESTIMATED_FLAG_04 = "estimatedFlag04";
    public static final String ESTIMATED_FLAG_05 = "estimatedFlag05";
    public static final String CHANNEL_FRONT_DISPLAY = "frontDisplayText";
}
