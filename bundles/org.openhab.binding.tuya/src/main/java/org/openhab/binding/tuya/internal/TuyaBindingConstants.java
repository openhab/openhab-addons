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
package org.openhab.binding.tuya.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link TuyaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TuyaBindingConstants {
    public static final String BINDING_ID = "tuya";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PROJECT = new ThingTypeUID(BINDING_ID, "project");
    public static final ThingTypeUID THING_TYPE_TUYA_DEVICE = new ThingTypeUID(BINDING_ID, "tuyaDevice");

    public static final String PROPERTY_CATEGORY = "category";

    public static final String CONFIG_LOCAL_KEY = "localKey";
    public static final String CONFIG_DEVICE_ID = "deviceId";
    public static final String CONFIG_DP = "dp";
    public static final String CONFIG_DP2 = "dp2";
    public static final String CONFIG_PRODUCT_ID = "productId";
    public static final String CONFIG_IP = "ip";
    public static final String CONFIG_MIN = "min";
    public static final String CONFIG_MAX = "max";
    public static final String CONFIG_PROTOCOL = "protocol";
    public static final String CONFIG_RANGE = "range";

    public static final ChannelTypeUID CHANNEL_TYPE_UID_NUMBER = new ChannelTypeUID(BINDING_ID, "number");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_IR_CODE = new ChannelTypeUID(BINDING_ID, "ir-code");

    public static final List<String> COLOUR_CHANNEL_CODES = List.of("colour_data");
    public static final List<String> DIMMER_CHANNEL_CODES = List.of("bright_value", "bright_value_1", "bright_value_2",
            "temp_value");

    // The heartbeat interval specifies the maximum amount of time that can pass without us
    // sending anything to a device. Once the heartbeat interval is reached we send a heartbeat
    // message to let the device know we are still present. Devices that do not receive traffic
    // on a connection for some interval (unspecified but typically ~30 seconds) close or discard
    // the connection.
    public static final int TCP_CONNECTION_HEARTBEAT_INTERVAL = 15; // Seconds

    // The amount of time a device has to respond to a message. If we don't see anything from
    // the device for this long after sending a message we consider the connection dead, close
    // it, and start trying to reconnect.
    public static final int TCP_CONNECTION_MESSAGE_RESPONSE = 1; // Seconds

    // How long to wait for a TCP session to connect before closing it and starting again. We do
    // not rely on TCP's own retry strategy because that varies between implementations so we
    // cannot know when the retry interval has become so great that it exceeds the amount of
    // time a battery device may be awake.
    public static final int TCP_CONNECT_TIMEOUT = 1000; // Milliseconds

    // How long to wait before attempting another connection after the previous closed or failed.
    // Note that if the previous attempt failed because the device was not reachable the interval
    // between attempts will be TCP_CONNECT_RETRY_INTERVAL however if the previous attempt failed
    // because the device was not responding the interval between attempts will be extended by
    // TCP_CONNECT_TIMEOUT. In order to catch battery powered devices while they are awake the sum
    // of TCP_CONNECT_TIMEOUT and TCP_CONNECT_RETRY_INTERVAL must therefore be small enough that at
    // least one, preferably two or three, connection attempts will be made during the time the
    // device is awake.
    public static final int TCP_CONNECT_RETRY_INTERVAL = 1000; // Milliseconds
}
