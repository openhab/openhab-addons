/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.sonyps4.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SonyPS4BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class SonyPS4BindingConstants {

    private static final String BINDING_ID = "sonyps4";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SONYPS4 = new ThingTypeUID(BINDING_ID, "SonyPS4");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SONYPS4);

    // List of all Channel ids
    static final String CHANNEL_POWER = "power";
    static final String CHANNEL_APPLICATION_NAME = "applicationName";
    static final String CHANNEL_APPLICATION_TITLEID = "applicationTitleid";
    static final String CHANNEL_APPLICATION_IMAGE = "applicationImage";
    static final String CHANNEL_KEY_UP = "keyUp";
    static final String CHANNEL_KEY_DOWN = "keyDown";
    static final String CHANNEL_KEY_RIGHT = "keyRight";
    static final String CHANNEL_KEY_LEFT = "keyLeft";
    static final String CHANNEL_KEY_ENTER = "keyEnter";
    static final String CHANNEL_KEY_BACK = "keyBack";
    static final String CHANNEL_KEY_OPTION = "keyOption";
    static final String CHANNEL_KEY_PS = "keyPS";

    static final String CHANNEL_HOST_NAME = "hostName";

    // List of all properties in the response from the PS4
    public static final String RESPONSE_HOST_ID = "host-id";
    public static final String RESPONSE_HOST_TYPE = "host-type";
    public static final String RESPONSE_HOST_NAME = "host-name";
    public static final String RESPONSE_HOST_REQUEST_PORT = "host-request-port";
    public static final String RESPONSE_DEVICE_DISCOVERY_PROTOCOL_VERSION = "device_discovery_protocol-version";
    public static final String RESPONSE_SYSTEM_VERSION = "system-version";
    public static final String RESPONSE_RUNNING_APP_NAME = "running-app-name";
    public static final String RESPONSE_RUNNING_APP_TITLEID = "running-app-titleid";

    public static final String PS4HW_CUHXXXX = "CUH-XXXX";
    public static final String PS4HW_CUH1000 = "CUH-1000";
    public static final String PS4HW_CUH1100 = "CUH-1100";
    public static final String PS4HW_CUH1200 = "CUH-1200";
    public static final String PS4HW_CUH2000 = "CUH-2000";
    public static final String PS4HW_CUH2100 = "CUH-2100";
    public static final String PS4HW_CUH7000 = "CUH-7000";
    public static final String PS4HW_CUH7100 = "CUH-7100";

    public static final int PS4_KEY_UP = 1 << 0;
    public static final int PS4_KEY_DOWN = 1 << 1;
    public static final int PS4_KEY_RIGHT = 1 << 2;
    public static final int PS4_KEY_LEFT = 1 << 3;
    public static final int PS4_KEY_ENTER = 1 << 4;
    public static final int PS4_KEY_BACK = 1 << 5;
    public static final int PS4_KEY_OPTION = 1 << 6;
    public static final int PS4_KEY_PS = 1 << 7;
    public static final int PS4_KEY_OFF = 1 << 8;
    public static final int PS4_KEY_CANCEL = 1 << 9;
    public static final int PS4_KEY_OPEN_RC = 1 << 10;
    public static final int PS4_KEY_CLOSE_RC = 1 << 11;

    public static final String PS4COMS_SYNC = "Ps4Sync";
}
