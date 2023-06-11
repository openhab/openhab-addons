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
package org.openhab.binding.tplinksmarthome.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class defines common constants, which are used across the whole binding.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Added channel and property keys
 */
@NonNullByDefault
public final class TPLinkSmartHomeBindingConstants {

    public enum ColorScales {
        NOT_SUPPORTED(0, 0),
        K_2500_6500(2500, 6500),
        K_2700_6500(2700, 6500),
        K_2500_9000(2500, 9000);

        private final int warm;
        private final int cool;

        ColorScales(final int warm, final int cool) {
            this.warm = warm;
            this.cool = cool;
        }

        public int getWarm() {
            return warm;
        }

        public int getCool() {
            return cool;
        }
    }

    public static final String BINDING_ID = "tplinksmarthome";

    // List of all switch channel ids
    public static final String CHANNEL_SWITCH = "switch";

    // List of all plug channel ids
    public static final String CHANNEL_LED = "led";

    // List of all bulb channel ids
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "colorTemperature";
    public static final String CHANNEL_COLOR_TEMPERATURE_ABS = "colorTemperatureAbs";

    public static final Set<String> CHANNELS_BULB_SWITCH = Stream.of(CHANNEL_BRIGHTNESS, CHANNEL_COLOR,
            CHANNEL_COLOR_TEMPERATURE, CHANNEL_COLOR_TEMPERATURE_ABS, CHANNEL_SWITCH).collect(Collectors.toSet());

    // List of all energy channel ids
    public static final String CHANNEL_ENERGY_POWER = "power";
    public static final String CHANNEL_ENERGY_TOTAL = "energyUsage";
    public static final String CHANNEL_ENERGY_VOLTAGE = "voltage";
    public static final String CHANNEL_ENERGY_CURRENT = "current";
    public static final Set<String> CHANNELS_ENERGY = Stream
            .of(CHANNEL_ENERGY_POWER, CHANNEL_ENERGY_TOTAL, CHANNEL_ENERGY_VOLTAGE, CHANNEL_ENERGY_CURRENT)
            .collect(Collectors.toSet());

    // List of all misc channel ids
    public static final String CHANNEL_RSSI = "rssi";

    // List of all group channel ids
    public static final String CHANNEL_SWITCH_GROUP = "group";
    public static final String CHANNEL_OUTLET_GROUP_PREFIX = "outlet";

    // List of configuration keys
    public static final String CONFIG_IP = "ipAddress";
    public static final String CONFIG_DEVICE_ID = "deviceId";
    public static final String CONFIG_REFRESH = "refresh";
    // Only for bulbs
    public static final String CONFIG_TRANSITION_PERIOD = "transitionPeriod";

    // List of property keys
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_DEVICE_NAME = "device name";
    public static final String PROPERTY_MAC = "mac";
    public static final String PROPERTY_HARDWARE_VERSION = "hardware version";
    public static final String PROPERTY_SOFWARE_VERSION = "sofware version";
    public static final String PROPERTY_HARDWARE_ID = "hardware id";
    public static final String PROPERTY_FIRMWARE_ID = "firmware id";
    public static final String PROPERTY_OEM_ID = "oem id";
    public static final String PROPERTY_FEATURE = "feature";
    public static final String PROPERTY_PROTOCOL_NAME = "protocol name";
    public static final String PROPERTY_PROTOCOL_VERSION = "protocol version";

    public static final int FORCED_REFRESH_BOUNDERY_SECONDS = 60;
    public static final int FORCED_REFRESH_BOUNDERY_SWITCHED_SECONDS = 5;

    private TPLinkSmartHomeBindingConstants() {
        // Constants class
    }
}
