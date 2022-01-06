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
package org.openhab.binding.bluetooth.ruuvitag.internal;

import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RuuviTagBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sami Salonen - Initial contribution
 */
public class RuuviTagBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BEACON = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "ruuvitag_beacon");

    // Channel IDs
    public static final String CHANNEL_ID_BATTERY = "batteryVoltage";
    public static final String CHANNEL_ID_DATA_FORMAT = "dataFormat";
    public static final String CHANNEL_ID_TEMPERATURE = "temperature";
    public static final String CHANNEL_ID_HUMIDITY = "humidity";
    public static final String CHANNEL_ID_PRESSURE = "pressure";
    public static final String CHANNEL_ID_TX_POWER = "txPower";

    public static final String CHANNEL_ID_ACCELERATIONX = "accelerationx";
    public static final String CHANNEL_ID_ACCELERATIONY = "accelerationy";
    public static final String CHANNEL_ID_ACCELERATIONZ = "accelerationz";
    public static final String CHANNEL_ID_MEASUREMENT_SEQUENCE_NUMBER = "measurementSequenceNumber";
    public static final String CHANNEL_ID_MOVEMENT_COUNTER = "movementCounter";
}
