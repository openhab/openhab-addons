/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.common.collect.ImmutableSet;

/**
 * This class defines common constants, which are used across the whole binding.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Added channel and property keys
 */
@NonNullByDefault
public final class TPLinkSmartHomeBindingConstants {

    public static final String BINDING_ID = "tplinksmarthome";

    // List of all channel ids
    public static final String CHANNEL_SWITCH = "switch";

    // List of all plug channel ids
    public static final String CHANNEL_LED = "led";

    // List of all Light channel ids
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "colorTemperature";
    public static final int COLOR_TEMPERATURE_LB120_MIN = 2700;
    public static final int COLOR_TEMPERATURE_LB120_MAX = 6500;
    public static final int COLOR_TEMPERATURE_LB130_MIN = 2500;
    public static final int COLOR_TEMPERATURE_LB130_MAX = 9000;

    public static final Set<String> CHANNELS_BULB_SWITCH = ImmutableSet.of(CHANNEL_BRIGHTNESS, CHANNEL_COLOR,
            CHANNEL_COLOR_TEMPERATURE, CHANNEL_SWITCH);

    // List of all energy channel ids
    public static final String CHANNEL_ENERGY_POWER = "power";
    public static final String CHANNEL_ENERGY_TOTAL = "energyUsage";
    public static final String CHANNEL_ENERGY_VOLTAGE = "voltage";
    public static final String CHANNEL_ENERGY_CURRENT = "current";
    public static final Set<String> CHANNELS_ENERGY = ImmutableSet.of(CHANNEL_ENERGY_POWER, CHANNEL_ENERGY_TOTAL,
            CHANNEL_ENERGY_VOLTAGE, CHANNEL_ENERGY_CURRENT);

    // List of all misc channel ids
    public static final String CHANNEL_RSSI = "rssi";

    // List of configuration keys
    public static final String CONFIG_IP = "ipAddress";
    public static final String CONFIG_REFRESH = "refresh";
    // Only for bulbs
    public static final String CONFIG_TRANSITION_PERIOD = "transitionPeriod";

    // List of property keys
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_DEVICE_NAME = "device name";
    public static final String PROPERTY_MAC = "mac";
    public static final String PROPERTY_DEVICE_ID = "device id";
    public static final String PROPERTY_HARDWARE_VERSION = "hardware version";
    public static final String PROPERTY_SOFWARE_VERSION = "sofware version";
    public static final String PROPERTY_HARDWARE_ID = "hardware id";
    public static final String PROPERTY_FIRMWARE_ID = "firmware id";
    public static final String PROPERTY_OEM_ID = "oem id";
    public static final String PROPERTY_FEATURE = "feature";
    public static final String PROPERTY_PROTOCOL_NAME = "protocol name";
    public static final String PROPERTY_PROTOCOL_VERSION = "protocol version";

    private TPLinkSmartHomeBindingConstants() {
        // Constants class
    }
}
