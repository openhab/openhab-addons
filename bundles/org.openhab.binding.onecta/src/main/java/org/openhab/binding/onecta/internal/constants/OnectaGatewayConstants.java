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
package org.openhab.binding.onecta.internal.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OnectaGatewayConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaGatewayConstants {
    public static final String PROPERTY_GW_NAME = "name";
    // List of all Channel ids
    public static final String CHANNEL_GW_DAYLIGHTSAVINGENABLED = "basic#daylight-savingtime-enabled";
    public static final String CHANNEL_GW_FIRMWAREVERSION = "basic#firmware-version";
    public static final String CHANNEL_GW_IS_FIRMWAREUPDATE_SUPPORTED = "basic#is-firmware-update-supported";
    public static final String CHANNEL_GW_IS_IN_ERROR_STATE = "basic#is-in-error-state";
    public static final String CHANNEL_GW_REGION_CODE = "basic#region-code";
    public static final String CHANNEL_GW_LED_ENABLED = "basic#led-enabled";
    public static final String CHANNEL_GW_SERIAL_NUMBER = "basic#serial-number";
    public static final String CHANNEL_GW_SSID = "basic#ssid";
    public static final String CHANNEL_GW_TIME_ZONE = "basic#timezone";
    public static final String CHANNEL_GW_WIFICONNENTION_SSID = "basic#wifi-connection-ssid";
    public static final String CHANNEL_GW_WIFICONNENTION_STRENGTH = "basic#wifi-connection-strength";
    public static final String CHANNEL_GW_MODEL_INFO = "basic#model-info";
    public static final String CHANNEL_GW_IP_ADDRESS = "basic#ip-address";
    public static final String CHANNEL_GW_MAC_ADDRESS = "basic#mac-address";
}
