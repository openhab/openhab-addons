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
package org.openhab.binding.homematic.internal.type;

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.BINDING_ID;

import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmGatewayInfo;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

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
        int value;
        try {
            String groupID = channelUID.getGroupId();
            value = groupID == null ? 0 : Integer.parseInt(groupID);
        } catch (NumberFormatException e) {
            value = 0;
        }
        return new HmDatapointInfo(channelUID.getThingUID().getId(), HmParamsetType.VALUES, value,
                channelUID.getIdWithoutGroup());
    }

    /**
     * Returns the address of the Homematic device from the given thing.
     */
    public static String getHomematicAddress(Thing thing) {
        return thing.getUID().getId();
    }
}
