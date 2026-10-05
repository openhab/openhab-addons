/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.motionblinds.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MotionBlindsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class MotionBlindsBindingConstants {

    public static final String BINDING_ID = "motionblinds";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WIFI_MOTOR = new ThingTypeUID(BINDING_ID, "wifi-motor");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_WIFI_MOTOR);

    // List of all Channel ids
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_TILT = "tilt";
    public static final String CHANNEL_ANGLE = "angle";
    public static final String CHANNEL_RSSI = "rssi";

    // Configuration and property names
    public static final String CONFIG_MAC_ADDRESS = "macAddress";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_PROTOCOL_VERSION = "protocolVersion";

    // Device types of motors that are directly connected to Wi-Fi (no hub)
    public static final String DEVICE_TYPE_WIFI_CURTAIN = "22000000";
    public static final String DEVICE_TYPE_WIFI_BLIND = "22000002";
    public static final String DEVICE_TYPE_WIFI_GATE = "22000005";
    public static final Set<String> DEVICE_TYPES_WIFI = Set.of(DEVICE_TYPE_WIFI_CURTAIN, DEVICE_TYPE_WIFI_BLIND,
            DEVICE_TYPE_WIFI_GATE);

    // Protocol
    public static final String MULTICAST_ADDRESS = "238.0.0.18";
    public static final int UDP_PORT_SEND = 32100;
    public static final int UDP_PORT_RECEIVE = 32101;

    public static final String MSG_GET_DEVICE_LIST = "GetDeviceList";
    public static final String MSG_GET_DEVICE_LIST_ACK = "GetDeviceListAck";
    public static final String MSG_READ_DEVICE = "ReadDevice";
    public static final String MSG_READ_DEVICE_ACK = "ReadDeviceAck";
    public static final String MSG_WRITE_DEVICE = "WriteDevice";
    public static final String MSG_WRITE_DEVICE_ACK = "WriteDeviceAck";
    public static final String MSG_REPORT = "Report";
    public static final String MSG_HEARTBEAT = "Heartbeat";

    public static final int OPERATION_CLOSE = 0;
    public static final int OPERATION_OPEN = 1;
    public static final int OPERATION_STOP = 2;
}
