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

    WATERBOX_STATUS("water_box_status", "status#water-box-status", "roborock:water-box-status", ""),
    LOCKSTATUS("lock_status", "status#lock-status", "roborock:lock-status", ""),
    WATERBOX_MODE("water_box_mode", "status#water-box-mode", "roborock:water-box-mode", ""),
    MOP_MODE("mop_mode", "status#mop-mode", "roborock:mop-mode", ""),
    WATERBOX_CARRIAGE("water_box_carriage_status", "status#water-box-carriage-status",
            "roborock:water-box-carriage-status", ""),
    MOP_FORBIDDEN("mop_forbidden_enable", "status#mop-forbidden-enable", "roborock:mop-forbidden-enable", ""),
    LOCATING("is_locating", "status#is-locating", "roborock:is-locating", ""),
    SEGMENT_STATUS("", "status#segment-status", "roborock:segment-status", "get-segment-status"),
    MAP_STATUS("", "status#map-status", "roborock:map-status", "get-map-status"),
    LED_STATUS("", "status#led-status", "roborock:led-status", "get-led-status"),
    CARPET_MODE("", "info#carpet-mode", "roborock:carpet-mode", "get-carpet-mode"),
    FW_FEATURES("", "info#fw-features", "roborock:fw-features", "get-fw-features"),
    ROOM_MAPPING("", "info#room-mapping", "roborock:room-mapping", "get-room-mapping"),
    MULTI_MAP_LIST("", "info#multi-maps-list", "roborock:multi-maps-list", "get-multi-maps-list"),
    CUSTOMIZE_CLEAN_MODE("", "info#customize-clean-mode", "roborock:customize-clean-mode", "get-customize-clean-mode"),
    SEGMENT_CLEAN("", "actions#segment", "roborock:segment", ""),
    COLLECT_DUST("auto_dust_collection", "actions#collect-dust", "roborock:collect-dust", ""),
    CLEAN_MOP_START("dry_status", "actions#clean-mop-start", "roborock:clean-mop-start", ""),
    CLEAN_MOP_STOP("dry_status", "actions#clean-mop-stop", "roborock:clean-mop-stop", ""),
    MOP_DRYING("dry_status", "status#is-mop-drying", "roborock:is-mop-drying", ""),
    MOP_DRYING_REMAINING_TIME("dry_status", "status#mop-drying-time", "roborock:mop-drying-time", ""),
    DOCK_STATE("dock_error_status", "status#dock-state", "roborock:dock-state", ""),
    DOCK_STATE_ID("dock_error_status", "status#dock-state-id", "roborock:dock-state-id", "");

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
