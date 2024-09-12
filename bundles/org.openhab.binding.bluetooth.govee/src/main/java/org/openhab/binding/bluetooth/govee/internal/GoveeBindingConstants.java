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
package org.openhab.binding.bluetooth.govee.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GoveeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class GoveeBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HYGROMETER = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "goveeHygrometer");
    public static final ThingTypeUID THING_TYPE_HYGROMETER_MONITOR = new ThingTypeUID(
            BluetoothBindingConstants.BINDING_ID, "goveeHygrometerMonitor");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_HYGROMETER,
            THING_TYPE_HYGROMETER_MONITOR);

    // List of all Channel ids
    public static final String CHANNEL_ID_BATTERY = "battery";
    public static final String CHANNEL_ID_TEMPERATURE = "temperature";
    public static final String CHANNEL_ID_TEMPERATURE_ALARM = "temperatureAlarm";
    public static final String CHANNEL_ID_HUMIDITY = "humidity";
    public static final String CHANNEL_ID_HUMIDITY_ALARM = "humidityAlarm";
}
