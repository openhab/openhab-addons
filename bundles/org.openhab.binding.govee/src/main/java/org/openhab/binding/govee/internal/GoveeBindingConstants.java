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
package org.openhab.binding.govee.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GoveeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GoveeBindingConstants {

    // Keep the legacy configuration name for compatibility with existing Things and inbox entries.
    public static final String CONFIG_DEVICE_ID = "macAddress";

    // Thing properties
    public static final String PROPERTY_DEVICE_ID = "deviceId";
    public static final String PROPERTY_NETWORK_MAC_ADDRESS = "mac";
    public static final String IP_ADDRESS = "hostname";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String PRODUCT_NAME = "productName";
    public static final String HW_VERSION = "wifiHardwareVersion";
    public static final String SW_VERSION = "wifiSoftwareVersion";
    public static final String BINDING_ID = "govee";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "govee-light");

    // List of all Channel ids
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "color-temperature";
    public static final String CHANNEL_COLOR_TEMPERATURE_ABS = "color-temperature-abs";

    // Limit values of channels
    public static final Double COLOR_TEMPERATURE_MIN_VALUE = 2000.0;
    public static final Double COLOR_TEMPERATURE_MAX_VALUE = 9000.0;

    public static final String PROPERTY_COLOR_TEMPERATURE_MIN = "minKelvin";
    public static final String PROPERTY_COLOR_TEMPERATURE_MAX = "maxKelvin";
}
