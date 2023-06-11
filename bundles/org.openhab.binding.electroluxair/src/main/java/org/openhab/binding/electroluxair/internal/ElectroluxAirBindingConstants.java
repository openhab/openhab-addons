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
package org.openhab.binding.electroluxair.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ElectroluxAirBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxAirBindingConstants {

    public static final String BINDING_ID = "electroluxair";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ELECTROLUX_PURE_A9 = new ThingTypeUID(BINDING_ID, "electroluxpurea9");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "api");

    // List of all Channel ids
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_TVOC = "tvoc";
    public static final String CHANNEL_PM1 = "pm1";
    public static final String CHANNEL_PM25 = "pm2_5";
    public static final String CHANNEL_PM10 = "pm10";
    public static final String CHANNEL_CO2 = "co2";
    public static final String CHANNEL_FILTER_LIFE = "filterLife";
    public static final String CHANNEL_DOOR_OPEN = "doorOpen";
    public static final String CHANNEL_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_WORK_MODE = "workMode";
    public static final String CHANNEL_IONIZER = "ionizer";

    // List of all Properties ids
    public static final String PROPERTY_BRAND = "brand";
    public static final String PROPERTY_COLOUR = "colour";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_DEVICE = "device";
    public static final String PROPERTY_FW_VERSION = "fwVersion";
    public static final String PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String PROPERTY_WORKMODE = "workmode";

    // List of all Commands
    public static final String COMMAND_WORKMODE_POWEROFF = "PowerOff";
    public static final String COMMAND_WORKMODE_AUTO = "Auto";
    public static final String COMMAND_WORKMODE_MANUAL = "Manual";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE,
            THING_TYPE_ELECTROLUX_PURE_A9);
}
