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
package org.openhab.binding.onecta.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OnectaGatewayConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaGatewayConstants {

    // List of all Channel ids
    public static final String CHANNEL_GW_DAYLIGHTSAVINGENABLED = "basic#daylightsavingtimeenabled";
    public static final String CHANNEL_GW_FIRMWAREVERSION = "basic#firmwareversion";
    public static final String CHANNEL_GW_IS_FIRMWAREUPDATE_SUPPORTED = "basic#isfirmwareupdatesupported";
    public static final String CHANNEL_GW_IS_IN_ERROR_STATE = "basic#isinerrorstate";
    public static final String CHANNEL_GW_REGION_CODE = "basic#regioncode";
    public static final String CHANNEL_GW_LED_ENABLED = "basic#ledenabled";
    public static final String CHANNEL_GW_SERIAL_NUMBER = "basic#serialnumber";
    public static final String CHANNEL_GW_SSID = "basic#ssid";
    public static final String CHANNEL_GW_TIME_ZONE = "basic#timezone";
    public static final String CHANNEL_GW_WIFICONNENTION_SSID = "basic#wificonnectionssid";
    public static final String CHANNEL_GW_WIFICONNENTION_STRENGTH = "basic#wificonnectionpower";
    public static final String PROPERTY_GW_NAME = "name";
    public static final String PROPERTY_GW_DISCOVERED = "Discovered";
}
