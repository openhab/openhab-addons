/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.robot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * List of additional capabilities
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum RobotCababilities {

    WATERBOX_STATUS("water_box_status", "status#water_box_status", "miio:water_box_status"),
    LOCKSTATUS("lock_status", "status#lock_status", "miio:lock_status"),
    WATERBOX_MODE("water_box_mode", "status#water_box_mode", "miio:water_box_mode"),
    WATERBOX_CARRIAGE("water_box_carriage_status", "status#water_box_carriage_status",
            "miio:water_box_carriage_status"),
    MOP_FORBIDDEN("mop_forbidden_enable", "status#mop_forbidden_enable", "miio:mop_forbidden_enable"),
    LOCATING("is_locating", "status#is_locating", "miio:is_locating"),
    SEGMENT_CLEAN("", "actions#segment", "miio:segment");

    private final String statusFieldName;
    private final String channel;
    private final String channelType;

    RobotCababilities(String statusKey, String channel, String channelType) {
        this.statusFieldName = statusKey;
        this.channel = channel;
        this.channelType = channelType;
    }

    public String getStatusFieldName() {
        return statusFieldName;
    }

    public String getChannel() {
        return channel;
    }

    public ChannelTypeUID getChannelType() {
        return new ChannelTypeUID(channelType);
    }

    @Override
    public String toString() {
        return String.format("Capability %s: status field name: '%s', channel: '%s', channeltype: '%s'.", this.name(),
                statusFieldName, channel, channelType);
    }
}
