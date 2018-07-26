/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.type;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.neeo.NeeoConstants;
import org.openhab.binding.neeo.NeeoUtil;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoDeviceDetails;
import org.openhab.binding.neeo.internal.models.NeeoMacro;
import org.openhab.binding.neeo.internal.models.NeeoRecipe;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.binding.neeo.internal.models.NeeoScenario;

/**
 * Utility class for generating some UIDs.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class UidUtils {

    /** The delimiter to separate 'parts' of an UID */
    private static final char DELIMITER = '-';

    /**
     * Determines if the specified device is an openhab thing or not. The determination is made if the adapter name for
     * the device is a valid {@link ThingUID}
     *
     * @param device a possibly null device
     * @return true if a thing, false otherwise
     */
    public static boolean isThing(NeeoDevice device) {
        final NeeoDeviceDetails details = device.getDetails();
        if (details == null) {
            return false;
        }

        try {
            new ThingUID(details.getAdapterName());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Generates the ThingTypeUID for the given {@link NeeoRoom}.
     *
     * @param room the non-null room
     * @return the thing type UID
     */
    public static ThingTypeUID generateThingTypeUID(NeeoRoom room) {
        Objects.requireNonNull(room, "room cannot be null");
        return new ThingTypeUID(NeeoConstants.BINDING_ID, "room" + DELIMITER + room.getKey());
    }

    /**
     * Generates the ThingTypeUID for the given {@link NeeoDevice}.
     *
     * @param device the non-null device
     * @return the thing type UID
     */
    public static ThingTypeUID generateThingTypeUID(NeeoDevice device) {
        Objects.requireNonNull(device, "device cannot be null");
        return new ThingTypeUID(NeeoConstants.BINDING_ID, "device" + DELIMITER + device.getKey());
    }

    /**
     * Generate group id for a {@link NeeoRecipe}
     *
     * @param recipe the non-null recipe
     * @return the recipe group id
     */
    static String generateGroupId(NeeoRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe cannot be null");
        return NeeoConstants.ROOM_CHANNEL_GROUP_RECIPEID + DELIMITER + recipe.getKey();
    }

    /**
     * Generate group id for a {@link NeeoScenario}
     *
     * @param scenario the non-null scenario
     * @return the scenario group id
     */
    static String generateGroupId(NeeoScenario scenario) {
        Objects.requireNonNull(scenario, "scenario cannot be null");

        return NeeoConstants.ROOM_CHANNEL_GROUP_SCENARIOID + DELIMITER + scenario.getKey();
    }

    /**
     * Parses the group id in the {@link ChannelUID}
     *
     * @param uid the non-null channel uid
     * @return the non-null, possibly empty group segments
     */
    public static String[] parseGroupId(ChannelUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final String group = uid.getGroupId();
        if (group == null) {
            return new String[0];
        }

        final int idx = group.indexOf(DELIMITER);
        if (idx < 0 || idx == group.length() - 1) {
            return new String[] { group };
        }
        return new String[] { group.substring(0, idx), group.substring(idx + 1) };
    }

    /**
     * Creates the channel id from the group/channel
     *
     * @param groupId the not empty group id
     * @param channelId the not empty channel id
     * @return the full channel id
     */
    public static String createChannelId(String groupId, String channelId) {
        NeeoUtil.requireNotEmpty(groupId, "groupId cannot be empty");
        NeeoUtil.requireNotEmpty(channelId, "channelId cannot be empty");

        return createChannelId(groupId, channelId, null);
    }

    /**
     * Creates the channel id from the group, channel id, and key
     *
     * @param groupId the possibly empty/null group id
     * @param channelId the not empty channel id
     * @param channelKey the possibly empty/null channel key
     * @return the full channel id
     */
    public static String createChannelId(String groupId, String channelId, @Nullable String channelKey) {
        NeeoUtil.requireNotEmpty(channelId, "channelId cannot be empty");

        return StringUtils.isEmpty(groupId) ? channelId
                : (groupId + (StringUtils.isEmpty(channelKey) ? "" : (DELIMITER + channelKey)) + "#" + channelId);
    }

    /**
     * Creates a {@link ChannelTypeUID} from a give {@link NeeoMacro}
     *
     * @param macro a non-null {@link NeeoMacro}
     * @return a non-null {@link ChannelTypeUID}
     */
    public static ChannelTypeUID createChannelTypeUID(NeeoMacro macro) {
        Objects.requireNonNull(macro, "macro cannot be null");
        return new ChannelTypeUID(NeeoConstants.BINDING_ID,
                NeeoConstants.DEVICE_CHANNEL_MACRO_STATUS + DELIMITER + macro.getKey());
    }

    /**
     * Creates a {@link ChannelGroupTypeUID}
     *
     * @param device a non-null {@link NeeoDevice}
     * @return a non-null {@link ChannelGroupTypeUID}
     */
    static ChannelGroupTypeUID createMacroChannelGroupTypeUID(NeeoDevice device) {
        Objects.requireNonNull(device, "device cannot be null");
        return new ChannelGroupTypeUID(NeeoConstants.BINDING_ID,
                NeeoConstants.DEVICE_GROUP_MACROS + DELIMITER + device.getKey());
    }
}
