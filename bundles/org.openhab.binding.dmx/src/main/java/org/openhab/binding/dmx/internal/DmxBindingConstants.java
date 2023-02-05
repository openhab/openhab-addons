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
package org.openhab.binding.dmx.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link DmxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DmxBindingConstants {
    public static final String BINDING_ID = "dmx";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CHASER = new ThingTypeUID(BINDING_ID, "chaser");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_COLOR = new ThingTypeUID(BINDING_ID, "color");
    public static final ThingTypeUID THING_TYPE_TUNABLEWHITE = new ThingTypeUID(BINDING_ID, "tunablewhite");
    public static final ThingTypeUID THING_TYPE_ARTNET_BRIDGE = new ThingTypeUID(BINDING_ID, "artnet-bridge");
    public static final ThingTypeUID THING_TYPE_LIB485_BRIDGE = new ThingTypeUID(BINDING_ID, "lib485-bridge");
    public static final ThingTypeUID THING_TYPE_SACN_BRIDGE = new ThingTypeUID(BINDING_ID, "sacn-bridge");

    // List of all config options
    public static final String CONFIG_UNIVERSE = "universe";
    public static final String CONFIG_DMX_ID = "dmxid";
    public static final String CONFIG_APPLY_CURVE = "applycurve";
    public static final String CONFIG_REFRESH_RATE = "refreshrate";

    public static final String CONFIG_SACN_MODE = "mode";
    public static final String CONFIG_ADDRESS = "address";
    public static final String CONFIG_LOCAL_ADDRESS = "localaddress";
    public static final String CONFIG_REFRESH_MODE = "refreshmode";

    public static final String CONFIG_DIMMER_TYPE = "dimmertype";
    public static final String CONFIG_DIMMER_FADE_TIME = "fadetime";
    public static final String CONFIG_DIMMER_DIM_TIME = "dimtime";
    public static final String CONFIG_DIMMER_TURNONVALUE = "turnonvalue";
    public static final String CONFIG_DIMMER_TURNOFFVALUE = "turnoffvalue";
    public static final String CONFIG_DIMMER_DYNAMICTURNONVALUE = "dynamicturnonvalue";
    public static final String CONFIG_CHASER_STEPS = "steps";
    public static final String CONFIG_CHASER_RESUME_AFTER = "resumeafter";

    // List of all channels
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_BRIGHTNESS_R = "brightness_r";
    public static final String CHANNEL_BRIGHTNESS_G = "brightness_g";
    public static final String CHANNEL_BRIGHTNESS_B = "brightness_b";
    public static final String CHANNEL_BRIGHTNESS_CW = "brightness_cw";
    public static final String CHANNEL_BRIGHTNESS_WW = "brightness_ww";

    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "color_temperature";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_MUTE = "mute";

    public static final ChannelTypeUID BRIGHTNESS_CHANNEL_TYPEUID = new ChannelTypeUID(BINDING_ID, CHANNEL_BRIGHTNESS);

    public static final ChannelTypeUID COLOR_CHANNEL_TYPEUID = new ChannelTypeUID(BINDING_ID, CHANNEL_COLOR);
    public static final ChannelTypeUID COLOR_TEMPERATURE_CHANNEL_TYPEUID = new ChannelTypeUID(BINDING_ID,
            CHANNEL_COLOR_TEMPERATURE);
    public static final ChannelTypeUID SWITCH_CHANNEL_TYPEUID = new ChannelTypeUID(BINDING_ID, CHANNEL_SWITCH);
    public static final ChannelTypeUID CONTROL_CHANNEL_TYPEUID = new ChannelTypeUID(BINDING_ID, CHANNEL_CONTROL);
    public static final ChannelTypeUID MUTE_CHANNEL_TYPEUID = new ChannelTypeUID(BINDING_ID, CHANNEL_MUTE);

    // Listener Type for channel updates
    public static enum ListenerType {
        VALUE,
        ACTION
    }
}
