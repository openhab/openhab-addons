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
package org.openhab.binding.ecovacs.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.commands.PlaySoundCommand.SoundType;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;
import org.openhab.binding.ecovacs.internal.api.model.MoppingWaterAmount;
import org.openhab.binding.ecovacs.internal.api.model.SuctionPower;
import org.openhab.binding.ecovacs.internal.util.StateOptionEntry;
import org.openhab.binding.ecovacs.internal.util.StateOptionMapping;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EcovacsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcovacsBindingConstants {
    private static final String BINDING_ID = "ecovacs";

    // Client keys and secrets used for API authentication (extracted from Ecovacs app)
    public static final String CLIENT_KEY = "1520391301804";
    public static final String CLIENT_SECRET = "6c319b2a5cd3e66e39159c2e28f2fce9";
    public static final String AUTH_CLIENT_KEY = "1520391491841";
    public static final String AUTH_CLIENT_SECRET = "77ef58ce3afbe337da74aa8c5ab963a9";
    public static final String APP_KEY = "2ea31cf06e6711eaa0aff7b9558a534e";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_API = new ThingTypeUID(BINDING_ID, "ecovacsapi");
    public static final ThingTypeUID THING_TYPE_VACUUM = new ThingTypeUID(BINDING_ID, "vacuum");

    // List of all channel UIDs
    public static final String CHANNEL_ID_AUTO_EMPTY = "settings#auto-empty";
    public static final String CHANNEL_ID_BATTERY_LEVEL = "status#battery";
    public static final String CHANNEL_ID_CLEANING_MODE = "status#current-cleaning-mode";
    public static final String CHANNEL_ID_CLEANING_TIME = "status#current-cleaning-time";
    public static final String CHANNEL_ID_CLEANED_AREA = "status#current-cleaned-area";
    public static final String CHANNEL_ID_CLEANING_PASSES = "settings#cleaning-passes";
    public static final String CHANNEL_ID_CLEANING_SPOT_DEFINITION = "status#current-cleaning-spot-definition";
    public static final String CHANNEL_ID_CONTINUOUS_CLEANING = "settings#continuous-cleaning";
    public static final String CHANNEL_ID_COMMAND = "actions#command";
    public static final String CHANNEL_ID_DUST_FILTER_LIFETIME = "consumables#dust-filter-lifetime";
    public static final String CHANNEL_ID_ERROR_CODE = "status#error-code";
    public static final String CHANNEL_ID_ERROR_DESCRIPTION = "status#error-description";
    public static final String CHANNEL_ID_LAST_CLEAN_START = "last-clean#last-clean-start";
    public static final String CHANNEL_ID_LAST_CLEAN_DURATION = "last-clean#last-clean-duration";
    public static final String CHANNEL_ID_LAST_CLEAN_AREA = "last-clean#last-clean-area";
    public static final String CHANNEL_ID_LAST_CLEAN_MODE = "last-clean#last-clean-mode";
    public static final String CHANNEL_ID_LAST_CLEAN_MAP = "last-clean#last-clean-map";
    public static final String CHANNEL_ID_MAIN_BRUSH_LIFETIME = "consumables#main-brush-lifetime";
    public static final String CHANNEL_ID_OTHER_COMPONENT_LIFETIME = "consumables#other-component-lifetime";
    public static final String CHANNEL_ID_SIDE_BRUSH_LIFETIME = "consumables#side-brush-lifetime";
    public static final String CHANNEL_ID_STATE = "status#state";
    public static final String CHANNEL_ID_SUCTION_POWER = "settings#suction-power";
    public static final String CHANNEL_ID_TOTAL_CLEANING_TIME = "total-stats#total-cleaning-time";
    public static final String CHANNEL_ID_TOTAL_CLEANED_AREA = "total-stats#total-cleaned-area";
    public static final String CHANNEL_ID_TOTAL_CLEAN_RUNS = "total-stats#total-clean-runs";
    public static final String CHANNEL_ID_TRUE_DETECT_3D = "settings#true-detect-3d";
    public static final String CHANNEL_ID_VOICE_VOLUME = "settings#voice-volume";
    public static final String CHANNEL_ID_WATER_PLATE_PRESENT = "status#water-system-present";
    public static final String CHANNEL_ID_WATER_AMOUNT = "settings#water-amount";
    public static final String CHANNEL_ID_WIFI_RSSI = "status#wifi-rssi";

    public static final String CHANNEL_TYPE_ID_CLEAN_MODE = "current-cleaning-mode";
    public static final String CHANNEL_TYPE_ID_LAST_CLEAN_MODE = "last-clean-mode";

    public static final String CMD_AUTO_CLEAN = "clean";
    public static final String CMD_PAUSE = "pause";
    public static final String CMD_RESUME = "resume";
    public static final String CMD_CHARGE = "charge";
    public static final String CMD_STOP = "stop";
    public static final String CMD_SPOT_AREA = "spotArea";
    public static final String CMD_CUSTOM_AREA = "customArea";

    public static final StateOptionMapping<CleanMode> CLEAN_MODE_MAPPING = StateOptionMapping.of(
            new StateOptionEntry<>(CleanMode.AUTO, "auto"),
            new StateOptionEntry<>(CleanMode.EDGE, "edge", DeviceCapability.EDGE_CLEANING),
            new StateOptionEntry<>(CleanMode.SPOT, "spot", DeviceCapability.SPOT_CLEANING),
            new StateOptionEntry<>(CleanMode.SPOT_AREA, "spotArea", DeviceCapability.SPOT_AREA_CLEANING),
            new StateOptionEntry<>(CleanMode.CUSTOM_AREA, "customArea", DeviceCapability.CUSTOM_AREA_CLEANING),
            new StateOptionEntry<>(CleanMode.SINGLE_ROOM, "singleRoom", DeviceCapability.SINGLE_ROOM_CLEANING),
            new StateOptionEntry<>(CleanMode.PAUSE, "pause"), new StateOptionEntry<>(CleanMode.STOP, "stop"),
            new StateOptionEntry<>(CleanMode.WASHING, "washing"), new StateOptionEntry<>(CleanMode.DRYING, "drying"),
            new StateOptionEntry<>(CleanMode.RETURNING, "returning"));

    public static final StateOptionMapping<MoppingWaterAmount> WATER_AMOUNT_MAPPING = StateOptionMapping.of(
            new StateOptionEntry<>(MoppingWaterAmount.LOW, "low"),
            new StateOptionEntry<>(MoppingWaterAmount.MEDIUM, "medium"),
            new StateOptionEntry<>(MoppingWaterAmount.HIGH, "high"),
            new StateOptionEntry<>(MoppingWaterAmount.VERY_HIGH, "veryhigh"));

    public static final StateOptionMapping<SuctionPower> SUCTION_POWER_MAPPING = StateOptionMapping.of(
            new StateOptionEntry<>(SuctionPower.SILENT, "silent", DeviceCapability.EXTENDED_CLEAN_SPEED_CONTROL),
            new StateOptionEntry<>(SuctionPower.NORMAL, "normal"), new StateOptionEntry<>(SuctionPower.HIGH, "high"),
            new StateOptionEntry<>(SuctionPower.HIGHER, "higher", DeviceCapability.EXTENDED_CLEAN_SPEED_CONTROL));

    public static final StateOptionMapping<SoundType> SOUND_TYPE_MAPPING = StateOptionMapping.of(
            new StateOptionEntry<>(SoundType.BEEP, "beep"), new StateOptionEntry<>(SoundType.I_AM_HERE, "iAmHere"),
            new StateOptionEntry<>(SoundType.STARTUP, "startup"),
            new StateOptionEntry<>(SoundType.SUSPENDED, "suspended"),
            new StateOptionEntry<>(SoundType.BATTERY_LOW, "batteryLow"));
}
