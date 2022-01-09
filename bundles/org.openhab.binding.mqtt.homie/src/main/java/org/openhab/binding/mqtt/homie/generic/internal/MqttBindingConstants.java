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
package org.openhab.binding.mqtt.homie.generic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MqttBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttBindingConstants {

    public static final String BINDING_ID = "mqtt";

    // List of all Thing Type UIDs
    public static final ThingTypeUID HOMIE300_MQTT_THING = new ThingTypeUID(BINDING_ID, "homie300");

    public static final String CONFIG_HOMIE_CHANNEL = "channel-type:mqtt:homie-channel";

    public static final String HOMIE_PROPERTY_VERSION = "homieversion";
    public static final String HOMIE_PROPERTY_HEARTBEAT_INTERVAL = "heartbeat_interval";

    public static final int HOMIE_DEVICE_TIMEOUT_MS = 30000;
    public static final int HOMIE_SUBSCRIBE_TIMEOUT_MS = 500;
    public static final int HOMIE_ATTRIBUTE_TIMEOUT_MS = 200;
}
