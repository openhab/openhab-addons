/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;

/**
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public final class ChannelUIDConstants {

    public static final ChannelUID CHANNEL_UID_BRIGHTNESS = createChannel(LB130, CHANNEL_BRIGHTNESS);
    public static final ChannelUID CHANNEL_UID_COLOR = createChannel(LB130, CHANNEL_COLOR);
    public static final ChannelUID CHANNEL_UID_COLOR_TEMPERATURE = createChannel(LB130, CHANNEL_COLOR_TEMPERATURE);
    public static final ChannelUID CHANNEL_UID_COLOR_TEMPERATURE_ABS = createChannel(LB130,
            CHANNEL_COLOR_TEMPERATURE_ABS);
    public static final ChannelUID CHANNEL_UID_ENERGY_CURRENT = createChannel(HS110, CHANNEL_ENERGY_CURRENT);
    public static final ChannelUID CHANNEL_UID_ENERGY_POWER = createChannel(HS110, CHANNEL_ENERGY_POWER);
    public static final ChannelUID CHANNEL_UID_ENERGY_TOTAL = createChannel(HS110, CHANNEL_ENERGY_TOTAL);
    public static final ChannelUID CHANNEL_UID_ENERGY_VOLTAGE = createChannel(HS110, CHANNEL_ENERGY_VOLTAGE);
    public static final ChannelUID CHANNEL_UID_LED = createChannel(HS100, CHANNEL_LED);
    public static final ChannelUID CHANNEL_UID_OTHER = createChannel(HS100, "OTHER");
    public static final ChannelUID CHANNEL_UID_RSSI = createChannel(HS100, CHANNEL_RSSI);
    public static final ChannelUID CHANNEL_UID_SWITCH = createChannel(HS100, CHANNEL_SWITCH);

    private static final String ID = "1234";

    private ChannelUIDConstants() {
        // Util class
    }

    private static ChannelUID createChannel(TPLinkSmartHomeThingType thingType, String channelId) {
        return new ChannelUID(new ThingUID(thingType.thingTypeUID(), ID), channelId);
    }

    public static ChannelUID createChannel(TPLinkSmartHomeThingType thingType, String groupId, String channelId) {
        return new ChannelUID(new ThingUID(thingType.thingTypeUID(), ID), groupId, channelId);
    }
}
