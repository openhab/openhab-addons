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
package org.openhab.binding.neeo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoDeviceDetails;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;

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
        final String adapterName = details.getAdapterName();
        if (adapterName == null) {
            return false;
        }
        try {
            new ThingUID(adapterName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses the channel id/key in the {@link ChannelUID} and will return a non-null, non-empty string array
     * representing the parts. The first element will always be the channelID itself. If there is a second element, the
     * second element will the the channel key
     *
     * @param uid the non-null channel uid
     * @return the non-null, non empty (only 1 or 2 element) list of parts
     */
    public static String[] parseChannelId(ChannelUID uid) {
        final String channelId = uid.getIdWithoutGroup();
        final int idx = channelId.indexOf(DELIMITER);
        if (idx < 0 || idx == channelId.length() - 1) {
            return new String[] { channelId };
        }
        return new String[] { channelId.substring(0, idx), channelId.substring(idx + 1) };
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
    public static String createChannelId(@Nullable String groupId, String channelId, @Nullable String channelKey) {
        NeeoUtil.requireNotEmpty(channelId, "channelId cannot be empty");

        return ((groupId == null || groupId.isEmpty()) ? "" : (groupId + "#"))
                + ((channelKey == null || channelKey.isEmpty()) ? channelId : (channelId + DELIMITER + channelKey));
    }

    /**
     * Creates the {@link ChannelUID} for a give thingUID, group ID and channel ID
     *
     * @param thingUid a non-null thing UID
     * @param groupId a non-null, non-empty group ID
     * @param channelId a non-null, non-empty channel ID
     * @return a non-null {@link ChannelUID}
     */
    public static ChannelUID createChannelUID(ThingUID thingUid, String groupId, String channelId) {
        return createChannelUID(thingUid, groupId, channelId, null);
    }

    /**
     * Creates the {@link ChannelUID} for a give thingUID, group ID, channel ID and channel key
     *
     * @param thingUid a non-null thing UID
     * @param groupId a non-null, non-empty group ID
     * @param channelId a non-null, non-empty channel ID
     * @param channelKey a potentially null, potentially empty channel KEY
     * @return a non-null {@link ChannelUID}
     */
    public static ChannelUID createChannelUID(ThingUID thingUid, String groupId, String channelId,
            @Nullable String channelKey) {
        return new ChannelUID(thingUid, groupId,
                channelId + ((channelKey == null || channelKey.isEmpty()) ? "" : (DELIMITER + channelKey)));
    }
}
