/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dreame.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Constants shared by the Dreame binding.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class DreameBindingConstants {

    public static final String BINDING_ID = "dreame";

    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_MOWER = new ThingTypeUID(BINDING_ID, "mower");

    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_CHARGING_STATUS = "charging-status";
    public static final String CHANNEL_ERROR_CODE = "error-code";
    public static final String CHANNEL_FIRMWARE = "firmware";
    public static final String CHANNEL_DND = "do-not-disturb";
    public static final String CHANNEL_CURRENT_ZONE = "current-zone";
    public static final String CHANNEL_MOWING_SESSIONS = "mowing-sessions";
    public static final String CHANNEL_TOTAL_MOWING_TIME = "total-mowing-time";
    public static final String CHANNEL_TOTAL_MOWED_AREA = "total-mowed-area";
    public static final String CHANNEL_POSITION_X = "position-x";
    public static final String CHANNEL_POSITION_Y = "position-y";
    public static final String CHANNEL_HEADING = "heading";
    public static final String CHANNEL_MOWING_PROGRESS = "mowing-progress";
    public static final String CHANNEL_PLANNED_MOWING_AREA = "planned-mowing-area";
    public static final String CHANNEL_CURRENT_MOWED_AREA = "current-mowed-area";
    public static final String CHANNEL_DOCKING_STATE = "docking-state";
    public static final String CHANNEL_LOCATION_STATE = "location-state";
    public static final String CHANNEL_WIFI_RSSI = "wifi-rssi";
    public static final String CHANNEL_BLE_RSSI = "ble-rssi";
    public static final String CHANNEL_LTE_RSSI = "lte-rssi";
    public static final String CHANNEL_TASK_EXECUTABLE = "task-executable";
    public static final String CHANNEL_TASK_ACTIVE = "task-active";
    public static final String CHANNEL_TASK_OPERATION = "task-operation";
    public static final String CHANNEL_TASK_STATE = "task-state";
    public static final String CHANNEL_TASK_TIME = "task-time";
    public static final String CHANNEL_CURRENT_MAP_ID = "current-map-id";
    public static final String CHANNEL_MAPS = "maps";
    public static final String CHANNEL_ZONES = "zones";
    public static final String CHANNEL_MAP_SVG = "map-svg";
    public static final String CHANNEL_MAP_PNG = "map-png";
    public static final String CHANNEL_ZONE_MOWING = "zone-mowing";
    public static final String CHANNEL_CUTTING_HEIGHT = "cutting-height";

    private DreameBindingConstants() {
    }
}
