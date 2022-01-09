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
package org.openhab.binding.lifx.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link LifxBinding} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Wouter Born - Added packet interval, power on brightness constants
 */
@NonNullByDefault
public class LifxBindingConstants {

    public static final String BINDING_ID = "lifx";

    // The LIFX LAN Protocol Specification states that lights can process up to 20 messages per second, not more.
    public static final long PACKET_INTERVAL = 50;

    // Port constants
    public static final int BROADCAST_PORT = 56700;
    public static final int UNICAST_PORT = 56700;

    // Minimum and maximum of MultiZone light indices
    public static final int MIN_ZONE_INDEX = 0;
    public static final int MAX_ZONE_INDEX = 255;

    // Fallback light state defaults
    public static final HSBK DEFAULT_COLOR = new HSBK(HSBType.WHITE, 3000);
    public static final PercentType DEFAULT_BRIGHTNESS = PercentType.HUNDRED;

    // List of all Channel IDs
    public static final String CHANNEL_ABS_TEMPERATURE = "abstemperature";
    public static final String CHANNEL_ABS_TEMPERATURE_ZONE = "abstemperaturezone";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_ZONE = "colorzone";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_HEV_CYCLE = "hevcycle";
    public static final String CHANNEL_INFRARED = "infrared";
    public static final String CHANNEL_SIGNAL_STRENGTH = "signalstrength";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_TEMPERATURE_ZONE = "temperaturezone";

    // List of all Channel Type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_BRIGHTNESS = new ChannelTypeUID(BINDING_ID, CHANNEL_BRIGHTNESS);
    public static final ChannelTypeUID CHANNEL_TYPE_COLOR = new ChannelTypeUID(BINDING_ID, CHANNEL_COLOR);
    public static final ChannelTypeUID CHANNEL_TYPE_EFFECT = new ChannelTypeUID(BINDING_ID, CHANNEL_EFFECT);
    public static final ChannelTypeUID CHANNEL_TYPE_HEV_CYCLE = new ChannelTypeUID(BINDING_ID, CHANNEL_HEV_CYCLE);
    public static final ChannelTypeUID CHANNEL_TYPE_INFRARED = new ChannelTypeUID(BINDING_ID, CHANNEL_INFRARED);
    public static final ChannelTypeUID CHANNEL_TYPE_TEMPERATURE = new ChannelTypeUID(BINDING_ID, CHANNEL_TEMPERATURE);

    // List of options for effect channel
    public static final String CHANNEL_TYPE_EFFECT_OPTION_OFF = "off";
    public static final String CHANNEL_TYPE_EFFECT_OPTION_MORPH = "morph";
    public static final String CHANNEL_TYPE_EFFECT_OPTION_FLAME = "flame";

    // Config property for the LIFX device id
    public static final String CONFIG_PROPERTY_DEVICE_ID = "deviceId";
    public static final String CONFIG_PROPERTY_FADETIME = "fadetime";

    // Config property for channel configuration
    public static final String CONFIG_PROPERTY_HEV_CYCLE_DURATION = "hevCycleDuration";
    public static final String CONFIG_PROPERTY_EFFECT_FLAME_SPEED = "effectFlameSpeed";
    public static final String CONFIG_PROPERTY_EFFECT_MORPH_SPEED = "effectMorphSpeed";
    public static final String CONFIG_PROPERTY_POWER_ON_BRIGHTNESS = "powerOnBrightness";
    public static final String CONFIG_PROPERTY_POWER_ON_COLOR = "powerOnColor";
    public static final String CONFIG_PROPERTY_POWER_ON_TEMPERATURE = "powerOnTemperature";

    // Property keys
    public static final String PROPERTY_HOST = "host";
    public static final String PROPERTY_HOST_VERSION = "hostVersion";
    public static final String PROPERTY_MAC_ADDRESS = "macAddress";
    public static final String PROPERTY_PRODUCT_ID = "productId";
    public static final String PROPERTY_PRODUCT_NAME = "productName";
    public static final String PROPERTY_PRODUCT_VERSION = "productVersion";
    public static final String PROPERTY_VENDOR_ID = "vendorId";
    public static final String PROPERTY_VENDOR_NAME = "vendorName";
    public static final String PROPERTY_WIFI_VERSION = "wifiVersion";
    public static final String PROPERTY_ZONES = "zones";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_COLORLIGHT = new ThingTypeUID(BINDING_ID, "colorlight");
    public static final ThingTypeUID THING_TYPE_COLORHEVLIGHT = new ThingTypeUID(BINDING_ID, "colorhevlight");
    public static final ThingTypeUID THING_TYPE_COLORIRLIGHT = new ThingTypeUID(BINDING_ID, "colorirlight");
    public static final ThingTypeUID THING_TYPE_COLORMZLIGHT = new ThingTypeUID(BINDING_ID, "colormzlight");
    public static final ThingTypeUID THING_TYPE_TILELIGHT = new ThingTypeUID(BINDING_ID, "tilelight");
    public static final ThingTypeUID THING_TYPE_WHITELIGHT = new ThingTypeUID(BINDING_ID, "whitelight");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_COLORLIGHT,
            THING_TYPE_COLORHEVLIGHT, THING_TYPE_COLORIRLIGHT, THING_TYPE_COLORMZLIGHT, THING_TYPE_TILELIGHT,
            THING_TYPE_WHITELIGHT);
}
