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
package org.openhab.binding.nikobus.internal.protocol;

import static org.openhab.binding.nikobus.internal.NikobusBindingConstants.CHANNEL_OUTPUT_PREFIX;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link SwitchModuleGroup} class defines Nikobus module group used for reading status or set its new value.
 * Nikobus module can always operate only in groups and not per-channel.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public enum SwitchModuleGroup {

    FIRST("12", "15", 1),
    SECOND("17", "16", 7);

    private final String statusRequest;
    private final String statusUpdate;
    private final int offset;

    private SwitchModuleGroup(String statusRequest, String statusUpdate, int offset) {
        this.statusRequest = statusRequest;
        this.statusUpdate = statusUpdate;
        this.offset = offset;
    }

    public String getStatusRequest() {
        return statusRequest;
    }

    public String getStatusUpdate() {
        return statusUpdate;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return 6;
    }

    public static SwitchModuleGroup mapFromChannel(ChannelUID channelUID) {
        if (!channelUID.getIdWithoutGroup().startsWith(CHANNEL_OUTPUT_PREFIX)) {
            throw new IllegalArgumentException("Unexpected channel " + channelUID.getId());
        }

        String channelNumber = channelUID.getIdWithoutGroup().substring(CHANNEL_OUTPUT_PREFIX.length());
        return mapFromChannel(Integer.parseInt(channelNumber));
    }

    public static SwitchModuleGroup mapFromChannel(int channelNumber) {
        int max = SECOND.getOffset() + SECOND.getCount();
        if (channelNumber < FIRST.getOffset() || channelNumber > max) {
            throw new IllegalArgumentException(String.format("Channel number should be between [%d, %d], but got %d",
                    FIRST.getOffset(), max, channelNumber));
        }
        return channelNumber >= SECOND.getOffset() ? SECOND : FIRST;
    }
}
