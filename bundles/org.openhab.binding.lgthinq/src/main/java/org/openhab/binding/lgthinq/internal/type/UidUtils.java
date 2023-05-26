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
package org.openhab.binding.lgthinq.internal.type;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.BINDING_ID;

import org.openhab.binding.lgthinq.internal.model.ThinqChannel;
import org.openhab.binding.lgthinq.internal.model.ThinqChannelGroup;
import org.openhab.binding.lgthinq.internal.model.ThinqDevice;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Utility class for generating some UIDs.
 *
 * @author Nemer Daud - Initial contribution
 */
public class UidUtils {

    /**
     * Generates the ThingTypeUID for the given device.
     */
    public static ThingTypeUID generateThingTypeUID(ThinqDevice device) {
        return new ThingTypeUID(BINDING_ID, device.getType());
    }

    /**
     * Generates the ChannelTypeUID.
     */
    public static ChannelTypeUID generateChannelTypeUID(ThinqChannel channel) {
        return new ChannelTypeUID(BINDING_ID, String.format("%s_%s", channel.getDevice().getType(), channel.getName()));
    }

    /**
     * Generates the ChannelTypeUID for the given channel group.
     */
    public static ChannelGroupTypeUID generateChannelGroupTypeUID(ThinqChannelGroup grpChannel) {
        return new ChannelGroupTypeUID(BINDING_ID,
                String.format("%s_%s", grpChannel.getDevice().getType(), grpChannel.getName()));
    }

    /**
     * Generates the ChannelUID for the given datapoint with channelNumber and datapointName.
     */
    public static ChannelUID generateChannelUID(ThinqChannel dp, ThingUID thingUID) {
        return new ChannelUID(thingUID, String.valueOf(dp.getName()), dp.getName());
    }
}
