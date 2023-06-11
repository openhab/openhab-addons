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

    WATERBOX_STATUS("water_box_status", "status#water_box_status", "miio:water_box_status", ""),
    LOCKSTATUS("lock_status", "status#lock_status", "miio:lock_status", ""),
    WATERBOX_MODE("water_box_mode", "status#water_box_mode", "miio:water_box_mode", ""),
    WATERBOX_CARRIAGE("water_box_carriage_status", "status#water_box_carriage_status", "miio:water_box_carriage_status",
            ""),
    MOP_FORBIDDEN("mop_forbidden_enable", "status#mop_forbidden_enable", "miio:mop_forbidden_enable", ""),
    LOCATING("is_locating", "status#is_locating", "miio:is_locating", ""),
    SEGMENT_STATUS("", "status#segment_status", "miio:segment_status", "get_segment_status"),
    MAP_STATUS("", "status#map_status", "miio:map_status", "get_map_status"),
    LED_STATUS("", "status#led_status", "miio:led_status", "get_led_status"),
    CARPET_MODE("", "info#carpet_mode", "miio:carpet_mode", "get_carpet_mode"),
    FW_FEATURES("", "info#fw_features", "miio:fw_features", "get_fw_features"),
    ROOM_MAPPING("", "info#room_mapping", "miio:room_mapping", "get_room_mapping"),
    MULTI_MAP_LIST("", "info#multi_maps_list", "miio:multi_maps_list", "get_multi_maps_list"),
    CUSTOMIZE_CLEAN_MODE("", "info#customize_clean_mode", "miio:customize_clean_mode", "get_customize_clean_mode"),
    SEGMENT_CLEAN("", "actions#segment", "miio:segment", "");

    private final String statusFieldName;
    private final String channel;
    private final String channelType;
    private final String command;

    RobotCababilities(String statusKey, String channel, String channelType, String command) {
        this.statusFieldName = statusKey;
        this.channel = channel;
        this.channelType = channelType;
        this.command = command;
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

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return String.format("Capability %s: status field name: '%s', channel: '%s', channeltype: '%s'%s%s.",
                this.name(), statusFieldName, channel, channelType, command.isBlank() ? "" : ", custom command: ",
                command);
    }
}
