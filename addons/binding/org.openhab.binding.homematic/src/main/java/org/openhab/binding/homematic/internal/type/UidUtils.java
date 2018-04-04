/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.type;

import static org.openhab.binding.homematic.HomematicBindingConstants.BINDING_ID;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmGatewayInfo;
import org.openhab.binding.homematic.internal.model.HmParamsetType;

/**
 * Utility class for generating some UIDs.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class UidUtils {

    /**
     * Generates the ThingTypeUID for the given device. If it's a Homegear device, add a prefix because a Homegear
     * device has more datapoints.
     */
    public static ThingTypeUID generateThingTypeUID(HmDevice device) {
        if (!device.isGatewayExtras() && device.getGatewayId().equals(HmGatewayInfo.ID_HOMEGEAR)) {
            return new ThingTypeUID(BINDING_ID, String.format("HG-%s", device.getType()));
        } else {
            return new ThingTypeUID(BINDING_ID, device.getType());
        }
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType, channelNumber and datapointName.
     */
    public static ChannelTypeUID generateChannelTypeUID(HmDatapoint dp) {
        return new ChannelTypeUID(BINDING_ID, String.format("%s_%s_%s", dp.getChannel().getDevice().getType(),
                dp.getChannel().getNumber(), dp.getName()));
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType and channelNumber.
     */
    public static ChannelGroupTypeUID generateChannelGroupTypeUID(HmChannel channel) {
        return new ChannelGroupTypeUID(BINDING_ID,
                String.format("%s_%s", channel.getDevice().getType(), channel.getNumber()));
    }

    /**
     * Generates the ThingUID for the given device in the given bridge.
     */
    public static ThingUID generateThingUID(HmDevice device, Bridge bridge) {
        ThingTypeUID thingTypeUID = generateThingTypeUID(device);
        return new ThingUID(thingTypeUID, bridge.getUID(), device.getAddress());
    }

    /**
     * Generates the ChannelUID for the given datapoint with channelNumber and datapointName.
     */
    public static ChannelUID generateChannelUID(HmDatapoint dp, ThingUID thingUID) {
        return new ChannelUID(thingUID, String.valueOf(dp.getChannel().getNumber()), dp.getName());
    }

    /**
     * Generates the HmDatapointInfo for the given thing and channelUID.
     */
    public static HmDatapointInfo createHmDatapointInfo(ChannelUID channelUID) {
        return new HmDatapointInfo(channelUID.getThingUID().getId(), HmParamsetType.VALUES,
                NumberUtils.toInt(channelUID.getGroupId()), channelUID.getIdWithoutGroup());
    }

    /**
     * Returns the address of the Homematic device from the given thing.
     */
    public static String getHomematicAddress(Thing thing) {
        return thing.getUID().getId();
    }

}
