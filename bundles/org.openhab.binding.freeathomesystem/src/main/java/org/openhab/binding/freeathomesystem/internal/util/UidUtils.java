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
package org.openhab.binding.freeathomesystem.internal.util;

import static org.openhab.binding.freeathomesystem.internal.FreeAtHomeSystemBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class UidUtils {

    public static ChannelGroupTypeUID generateChannelGroupTypeUID(String groupLabel) {
        String channelNameString;

        channelNameString = groupLabel + "_channelgroupstype";

        ChannelGroupTypeUID channelGroupTypeUID = new ChannelGroupTypeUID(BINDING_ID, channelNameString);

        return channelGroupTypeUID;
    }

    public static ChannelTypeUID generateChannelTypeUID(String valueType, boolean isReadOnly) {
        String channelNameString;

        if (isReadOnly) {
            channelNameString = valueType + "_channeltype" + "_ro";
        } else {
            channelNameString = valueType + "_channeltype";
        }

        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelNameString);

        return channelTypeUID;
    }

    public static ChannelUID generateChannelUID(ThingUID thingUID, String deviceId, String channelID, String label) {
        String localLabel = label.replaceAll("[^a-zA-Z\\d\\s:]", "_").replace(" ", "_").toLowerCase();

        ChannelUID channelUID = new ChannelUID(thingUID, String.format("%s_%s_%s", deviceId, channelID, localLabel));
        return channelUID;
    }

    public static ThingTypeUID generateThingUID() {
        return new ThingTypeUID(BINDING_ID, FREEATHOMEDEVICE_TYPE_ID);
    }
}
