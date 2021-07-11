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
package org.openhab.binding.sensorpush.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SensorPushBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class SensorPushBindingConstants {

    private static final String BINDING_ID = "sensorpush";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "cloudbridge");
    public static final ThingTypeUID THING_TYPE_SENSOR = new ThingTypeUID(BINDING_ID, "sensor");

    // Set of discoverable Thing Type UIDs
    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPE_UIDS = Collections.singleton(THING_TYPE_SENSOR);

    // Properties
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_ADDRESS = "bluetoothAddress";

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_TIME = "time";
    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_DEWPOINT = "dewpoint";
    public static final String CHANNEL_VPD = "vpd";
}
