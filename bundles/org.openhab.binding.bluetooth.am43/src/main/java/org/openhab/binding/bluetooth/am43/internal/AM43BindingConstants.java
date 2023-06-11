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
package org.openhab.binding.bluetooth.am43.internal;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AM43BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class AM43BindingConstants {

    private static final String BINDING_ID = "am43";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AM43 = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            BINDING_ID);

    // List of all Channel ids
    // public static final String CHANNEL_ID_NAME = "name";
    public static final String CHANNEL_ID_DIRECTION = "direction";
    public static final String CHANNEL_ID_TOP_LIMIT_SET = "topLimitSet";
    public static final String CHANNEL_ID_BOTTOM_LIMIT_SET = "bottomLimitSet";
    public static final String CHANNEL_ID_HAS_LIGHT_SENSOR = "hasLightSensor";
    public static final String CHANNEL_ID_OPERATION_MODE = "operationMode";
    public static final String CHANNEL_ID_SPEED = "speed";
    public static final String CHANNEL_ID_ELECTRIC = "electric";
    public static final String CHANNEL_ID_POSITION = "position";
    public static final String CHANNEL_ID_LENGTH = "length";
    public static final String CHANNEL_ID_DIAMETER = "diameter";
    public static final String CHANNEL_ID_TYPE = "type";
    public static final String CHANNEL_ID_LIGHT_LEVEL = "lightLevel";

    public static final UUID SERVICE_UUID = UUID.fromString("0000fe50-0000-1000-8000-00805f9b34fb");

    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000fe51-0000-1000-8000-00805f9b34fb");

    public static List<String> getAllChannels() {
        return Arrays.asList(CHANNEL_ID_DIRECTION, CHANNEL_ID_TOP_LIMIT_SET, CHANNEL_ID_BOTTOM_LIMIT_SET,
                CHANNEL_ID_HAS_LIGHT_SENSOR, CHANNEL_ID_OPERATION_MODE, CHANNEL_ID_SPEED, CHANNEL_ID_ELECTRIC,
                CHANNEL_ID_POSITION, CHANNEL_ID_LENGTH, CHANNEL_ID_DIAMETER, CHANNEL_ID_TYPE, CHANNEL_ID_LIGHT_LEVEL);
    }
}
