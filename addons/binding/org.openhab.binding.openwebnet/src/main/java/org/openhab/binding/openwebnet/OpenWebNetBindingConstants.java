/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link OpenWebNetBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Antoine Laydier - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetBindingConstants {

    private static final String BINDING_ID = "openwebnet";

    // bridge thing types
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "dongle");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_LIGHTING = new ThingTypeUID(BINDING_ID, "lighting");
    public static final ThingTypeUID THING_TYPE_DUAL_LIGHTING = new ThingTypeUID(BINDING_ID, "duallighting");
    public static final ThingTypeUID THING_TYPE_AUTOMATION = new ThingTypeUID(BINDING_ID, "automation");

    // Id of the channels
    public static final String CHANNEL_SWITCH01 = "Switch01";
    public static final String CHANNEL_SWITCH02 = "Switch02";
    public static final String CHANNEL_DIMMER01 = "Dimmer01";
    public static final String CHANNEL_DIMMER02 = "Dimmer02";
    public static final String CHANNEL_SHUTTER = "Shutter";

    // List all light channels
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_SWITCH = "switch";

    // Bridge config properties
    public static final String SERIAL_PORT = "serialPort";
    public static final String NAME = "name";

    // Thing config properties
    public static final String CHANNEL1 = "channel1";
    public static final String CHANNEL2 = "channel2";

    public static final ChannelTypeUID BRIGHTNESS_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, CHANNEL_BRIGHTNESS);
    public static final ChannelTypeUID SWITCH_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, CHANNEL_SWITCH);

    // Module Properties
    public static final String PROPERTY_VERSION = "version";

}
