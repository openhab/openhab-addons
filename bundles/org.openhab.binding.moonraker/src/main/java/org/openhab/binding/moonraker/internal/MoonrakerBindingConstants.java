/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.moonraker.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;

/**
 * The {@link MoonrakerBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class MoonrakerBindingConstants {
    private static final String BINDING_ID = "moonraker";

    /** Delay between connection attempts */
    public static final int REINITIALIZE_DELAY_SECONDS = 2;
    /** Timeout for connection attempts */
    public static final int WEBSOCKET_TIMEOUT_SECONDS = 2;
    /** Delay between channel refresh */
    public static final int CHANNEL_REFRESH_INTERVAL_SECONDS = 5;

    /** Thing Type UID */
    public static final ThingTypeUID THING_TYPE_3DPRINTER = new ThingTypeUID(BINDING_ID, "printer_3d");

    /* Channel group UIDs */
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_EXTRUDER = new ChannelGroupTypeUID(BINDING_ID,
            "extruder");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_HEATER = new ChannelGroupTypeUID(BINDING_ID, "heater");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_TEMPERATURE_SENSOR = new ChannelGroupTypeUID(BINDING_ID,
            "temperature_sensor");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_FAN = new ChannelGroupTypeUID(BINDING_ID, "fan");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_TEMPERATURE_FAN = new ChannelGroupTypeUID(BINDING_ID,
            "temperature_fan");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_FILAMENT_SENSOR = new ChannelGroupTypeUID(BINDING_ID,
            "filament_sensor");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_BED_MESH = new ChannelGroupTypeUID(BINDING_ID,
            "bed_mesh");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_OUTPUT_PIN = new ChannelGroupTypeUID(BINDING_ID,
            "output_pin");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_GCODE_BUTTON = new ChannelGroupTypeUID(BINDING_ID,
            "gcode_button");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_POWER_DEVICE = new ChannelGroupTypeUID(BINDING_ID,
            "power_device");
}
