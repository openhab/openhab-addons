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
package org.openhab.binding.mqtt.ruuvigateway.internal;

import static org.openhab.binding.mqtt.MqttBindingConstants.BINDING_ID;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RuuviGatewayBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class RuuviGatewayBindingConstants {
    public static final String BASE_TOPIC = "ruuvi/";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BEACON = new ThingTypeUID(BINDING_ID, "ruuvitag_beacon");

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

    public static final String CHANNEL_ID_RSSI = "rssi";
    public static final String CHANNEL_ID_TS = "ts";
    public static final String CHANNEL_ID_GWTS = "gwts";
    public static final String CHANNEL_ID_GWMAC = "gwmac";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BEACON);
    public static final int RUUVI_GATEWAY_SUBSCRIBE_TIMEOUT_MS = 30000;

    // Thing properties
    public static final String PROPERTY_TAG_ID = "tagID";
    public static final String CONFIGURATION_PROPERTY_TOPIC = "topic";

    public static final String CONFIGURATION_PROPERTY_TIMEOUT = "timeout"; // only for tests
}
