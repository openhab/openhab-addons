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
package org.openhab.binding.melcloud.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MelCloudBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Luca Calcaterra - Initial contribution
 * @author Wietse van Buitenen - Added heatpump device
 * @author Alessio Galliazzo - Added heatpump functionalities for flow temperature and temperature control
 * @author Bernd Weymann - Added MELCloud Home bridge skeleton
 * @author Bernd Weymann - Added MELCloud Home ATA/ATW unit Things
 */
@NonNullByDefault
public class MelCloudBindingConstants {

    private static final String BINDING_ID = "melcloud";

    // List of Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_MELCLOUD_ACCOUNT = new ThingTypeUID(BINDING_ID, "melcloudaccount");
    public static final ThingTypeUID THING_TYPE_HEATPUMPDEVICE = new ThingTypeUID(BINDING_ID, "heatpumpdevice");
    // Skeleton bridge for the newer MELCloud Home platform (auth.melcloudhome.com).
    public static final ThingTypeUID THING_TYPE_MELCLOUD_HOME_ACCOUNT = new ThingTypeUID(BINDING_ID, "home-account");
    // MELCloud Home unit Things.
    public static final ThingTypeUID THING_TYPE_MELCLOUD_HOME_ATA_UNIT = new ThingTypeUID(BINDING_ID, "ata-unit");
    public static final ThingTypeUID THING_TYPE_MELCLOUD_HOME_ATW_UNIT = new ThingTypeUID(BINDING_ID, "atw-unit");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACDEVICE = new ThingTypeUID(BINDING_ID, "acdevice");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_OPERATION_MODE = "operationMode";
    public static final String CHANNEL_SET_TEMPERATURE = "setTemperature";
    public static final String CHANNEL_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_VANE_HORIZONTAL = "vaneHorizontal";
    public static final String CHANNEL_VANE_VERTICAL = "vaneVertical";
    public static final String CHANNEL_SET_TEMPERATURE_ZONE1 = "setTemperatureZone1";
    public static final String CHANNEL_ROOM_TEMPERATURE_ZONE1 = "roomTemperatureZone1";
    public static final String CHANNEL_SET_TEMPERATURE_ZONE2 = "setTemperatureZone2";
    public static final String CHANNEL_ROOM_TEMPERATURE_ZONE2 = "roomTemperatureZone2";
    public static final String CHANNEL_FORCED_HOTWATERMODE = "forcedHotWaterMode";
    public static final String CHANNEL_TANKWATERTEMPERATURE = "tankWaterTemperature";
    public static final String CHANNEL_TANK_TARGET_WATER_TEMPERATURE = "tankTargetWaterTemperature";

    public static final String CHANNEL_HEAT_TEMPERATURE_MODE_ZONE1 = "heatTemperatureModeZone1";
    public static final String CHANNEL_HEAT_FLOW_TEMPERATURE_ZONE1 = "heatFlowTemperatureZone1";
    public static final String CHANNEL_HEAT_TEMPERATURE_MODE_ZONE2 = "heatTemperatureModeZone2";
    public static final String CHANNEL_HEAT_FLOW_TEMPERATURE_ZONE2 = "heatFlowTemperatureZone2";

    // Read Only Channels
    public static final String CHANNEL_ROOM_TEMPERATURE = "roomTemperature";
    public static final String CHANNEL_LAST_COMMUNICATION = "lastCommunication";
    public static final String CHANNEL_NEXT_COMMUNICATION = "nextCommunication";
    public static final String CHANNEL_HAS_PENDING_COMMAND = "hasPendingCommand";
    public static final String CHANNEL_OFFLINE = "offline";

    // MELCloud Home ATA/ATW unit channels; several channels above (power, setTemperature, roomTemperature,
    // setTemperatureZone1/2, roomTemperatureZone1/2, forcedHotWaterMode, tankWaterTemperature,
    // tankTargetWaterTemperature) are reused as-is. operationMode/fanSpeed/vaneHorizontal/vaneVertical reuse the same
    // channel ids, but the ATA unit binds them to dedicated Number channel-types (ataOperationMode-channel/
    // ataFanSpeed-channel/ataVaneHorizontal-channel/ataVaneVertical-channel) instead of the legacy A.C. Device's
    // String-based ones.
    public static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoorTemperature";
    public static final String CHANNEL_ENERGY_CONSUMED = "energyConsumed";
    public static final String CHANNEL_ENERGY_PRODUCED = "energyProduced";
    public static final String CHANNEL_COP = "cop";
    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_IS_IN_ERROR = "isInError";
    public static final String CHANNEL_ERROR_CODE = "errorCode";
    public static final String CHANNEL_IN_STANDBY_MODE = "inStandbyMode";
    public static final String CHANNEL_OPERATION_STATUS = "operationStatus";
    public static final String CHANNEL_ZONE1_OPERATION_MODE = "zone1OperationMode";
    public static final String CHANNEL_ZONE2_OPERATION_MODE = "zone2OperationMode";
    public static final String CHANNEL_HOLIDAY_MODE = "holidayMode";
    public static final String CHANNEL_FROST_PROTECTION = "frostProtection";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_MELCLOUD_ACCOUNT, THING_TYPE_ACDEVICE, THING_TYPE_HEATPUMPDEVICE,
                    THING_TYPE_MELCLOUD_HOME_ACCOUNT, THING_TYPE_MELCLOUD_HOME_ATA_UNIT,
                    THING_TYPE_MELCLOUD_HOME_ATW_UNIT).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_ACDEVICE, THING_TYPE_HEATPUMPDEVICE).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> HOME_DISCOVERABLE_THING_TYPE_UIDS = Collections.unmodifiableSet(Stream
            .of(THING_TYPE_MELCLOUD_HOME_ATA_UNIT, THING_TYPE_MELCLOUD_HOME_ATW_UNIT).collect(Collectors.toSet()));
}
