/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.listener.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ListenerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author VitaTucek - Initial contribution
 */
@NonNullByDefault
public class ListenerBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LISTENER = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "listener");

    // List of Channel ids
    public static final String CHANNEL_TYPE_INTERVAL = "interval";
    public static final String CHANNEL_TYPE_MANUFACTURER_NUMBER = "manufacturer-number";
    public static final String CHANNEL_TYPE_MANUFACTURER_RAW = "manufacturer-raw";
    public static final String CHANNEL_TYPE_SERVICE_NUMBER = "service-number";
    public static final String CHANNEL_TYPE_SERVICE_RAW = "service-raw";

    // List of Parameters
    public static final String PARAMETER_DATA_UUID = "uuid";
    public static final String PARAMETER_DATA_BEGIN = "dataBegin";
    public static final String PARAMETER_DATA_LENGTH = "dataLength";
    public static final String PARAMETER_MULTIPLYER = "multiplyer";
    public static final String PARAMETER_PAYLOAD_LENGTH = "payloadLength";

    // List of Property IDs
    public static final String PROPERTY_ADVERTISING_INTERVAL = "advertisingInterval";
}
