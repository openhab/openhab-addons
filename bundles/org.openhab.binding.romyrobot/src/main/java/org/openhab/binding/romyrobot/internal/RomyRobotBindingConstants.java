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
package org.openhab.binding.romyrobot.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link romyRobotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernhard Kreuz - Initial contribution
 */
@NonNullByDefault
public class RomyRobotBindingConstants {

    private static final String BINDING_ID = "romyrobot";

    // List of all Thing Type UIDs
    public static final ThingTypeUID ROMYROBOT_DEVICE = new ThingTypeUID(BINDING_ID, "romyrobot");

    // List of all Channel ids

    public static final String CHANNEL_FW_VERSION = "fwversion";
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_ACTIVE_PUMP_VOLUME = "activepumpvolume";
    public static final String CHANNEL_STRATEGY = "strategy";
    public static final String CHANNEL_SUCTION_MODE = "suctionmode";
    public static final String CHANNEL_BATTERY = "battery";
    public static final String CHANNEL_CHARGING = "charging";
    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_POWER_STATUS = "powerstatus";
    public static final String CHANNEL_SELECTED_MAP = "selectedmap";
    public static final String CHANNEL_AVAILABLE_MAPS_JSON = "availablemapsjson";
}
