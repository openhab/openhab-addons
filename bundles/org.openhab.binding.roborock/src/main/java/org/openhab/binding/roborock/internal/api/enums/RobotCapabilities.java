/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * List of additional capabilities
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum RobotCapabilities {

    WATERBOX_STATUS("water_box_status", "status#water_box_status", "roborock:water_box_status", ""),
    LOCKSTATUS("lock_status", "status#lock_status", "roborock:lock_status", ""),
    WATERBOX_MODE("water_box_mode", "status#water_box_mode", "roborock:water_box_mode", ""),
    MOP_MODE("mop_mode", "status#mop_mode", "roborock:mop_mode", ""),
    WATERBOX_CARRIAGE("water_box_carriage_status", "status#water_box_carriage_status",
            "roborock:water_box_carriage_status", ""),
    MOP_FORBIDDEN("mop_forbidden_enable", "status#mop_forbidden_enable", "roborock:mop_forbidden_enable", ""),
    LOCATING("is_locating", "status#is_locating", "roborock:is_locating", ""),
    SEGMENT_STATUS("", "status#segment_status", "roborock:segment_status", "get_segment_status"),
    MAP_STATUS("", "status#map_status", "roborock:map_status", "get_map_status"),
    LED_STATUS("", "status#led_status", "roborock:led_status", "get_led_status"),
    CARPET_MODE("", "info#carpet_mode", "roborock:carpet_mode", "get_carpet_mode"),
    FW_FEATURES("", "info#fw_features", "roborock:fw_features", "get_fw_features"),
    ROOM_MAPPING("", "info#room_mapping", "roborock:room_mapping", "get_room_mapping"),
    MULTI_MAP_LIST("", "info#multi_maps_list", "roborock:multi_maps_list", "get_multi_maps_list"),
    CUSTOMIZE_CLEAN_MODE("", "info#customize_clean_mode", "roborock:customize_clean_mode", "get_customize_clean_mode"),
    SEGMENT_CLEAN("", "actions#segment", "roborock:segment", ""),
    COLLECT_DUST("auto_dust_collection", "actions#collect_dust", "roborock:collect_dust", ""),
    CLEAN_MOP_START("dry_status", "actions#clean_mop_start", "roborock:clean_mop_start", ""),
    CLEAN_MOP_STOP("dry_status", "actions#clean_mop_stop", "roborock:clean_mop_stop", ""),
    MOP_DRYING("dry_status", "status#is_mop_drying", "roborock:is_mop_drying", ""),
    MOP_DRYING_REMAINING_TIME("dry_status", "status#mop_drying_time", "roborock:mop_drying_time", ""),
    DOCK_STATE("dock_error_status", "status#dock_state", "roborock:dock_state", ""),
    DOCK_STATE_ID("dock_error_status", "status#dock_state_id", "roborock:dock_state_id", "");

    private final String statusFieldName;
    private final String channel;
    private final String channelType;
    private final String command;

    RobotCapabilities(String statusKey, String channel, String channelType, String command) {
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
