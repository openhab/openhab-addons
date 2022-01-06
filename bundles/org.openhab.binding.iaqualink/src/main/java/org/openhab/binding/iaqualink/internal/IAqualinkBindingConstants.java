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
package org.openhab.binding.iaqualink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link IAqualinkBindingConstants} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class IAqualinkBindingConstants {

    public static final String BINDING_ID = "iaqualink";
    public static final String CHANNEL_TYPE_AUX_SWITCH = "aux-switch";
    public static final String CHANNEL_TYPE_AUX_DIMMER = "aux-dimmer";
    public static final String CHANNEL_TYPE_AUX_JANDYCOLOR = "aux-jandycolor";
    public static final String CHANNEL_TYPE_AUX_PENTAIRSAM = "aux-pentairsam";
    public static final String CHANNEL_TYPE_AUX_JANDYLED = "aux-jandyled";
    public static final String CHANNEL_TYPE_AUX_PENTAIRIB = "aux-pentairib";
    public static final String CHANNEL_TYPE_AUX_HAYWARD = "aux-hayward";

    public static final String CHANNEL_TYPE_ONETOUCH = "onetouch";

    public static final ThingTypeUID IAQUALINK_DEVICE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "controller");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_ONETOUCH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_ONETOUCH);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_AUX_SWITCH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_AUX_SWITCH);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_AUX_DIMMER = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_AUX_DIMMER);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_AUX_JANDYCOLOR = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_AUX_JANDYCOLOR);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_AUX_PENTAIRSAM = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_AUX_PENTAIRSAM);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_AUX_JANDYLED = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_AUX_JANDYLED);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_AUX_PENTAIRIB = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_AUX_PENTAIRIB);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_AUX_HAYWARD = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_AUX_HAYWARD);
}
