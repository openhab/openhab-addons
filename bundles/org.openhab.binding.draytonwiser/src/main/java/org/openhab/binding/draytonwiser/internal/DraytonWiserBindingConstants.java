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
package org.openhab.binding.draytonwiser.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link DraytonWiserBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class DraytonWiserBindingConstants {

    public static final String BINDING_ID = "draytonwiser";

    public static final String REFRESH_INTERVAL = "refresh";
    public static final int DEFAULT_REFRESH_SECONDS = 60;

    public static final int OFFLINE_TEMPERATURE = -32768;

    // Web Service Endpoints
    public static final String DEVICE_ENDPOINT = "data/domain/Device/";
    public static final String ROOMSTATS_ENDPOINT = "data/domain/RoomStat/";
    public static final String TRVS_ENDPOINT = "data/domain/SmartValve/";
    public static final String ROOMS_ENDPOINT = "data/domain/Room/";
    public static final String HEATCHANNELS_ENDPOINT = "data/domain/HeatingChannel/";
    public static final String SYSTEM_ENDPOINT = "data/domain/System/";
    public static final String STATION_ENDPOINT = "data/network/Station/";
    public static final String DOMAIN_ENDPOINT = "data/domain/";
    public static final String HOTWATER_ENDPOINT = "data/domain/HotWater/";
    public static final String SMARTPLUG_ENDPOINT = "data/domain/SmartPlug/";

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "heathub");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "boiler-controller");
    public static final ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");
    public static final ThingTypeUID THING_TYPE_ROOMSTAT = new ThingTypeUID(BINDING_ID, "roomstat");
    public static final ThingTypeUID THING_TYPE_ITRV = new ThingTypeUID(BINDING_ID, "itrv");
    public static final ThingTypeUID THING_TYPE_HOTWATER = new ThingTypeUID(BINDING_ID, "hotwater");
    public static final ThingTypeUID THING_TYPE_SMARTPLUG = new ThingTypeUID(BINDING_ID, "smart-plug");

    // properties
    public static final String PROP_ADDRESS = "networkAddress";
    public static final String PROP_SERIAL_NUMBER = "serialNumber";
    public static final String PROP_NAME = "name";
    public static final String PROP_ID = "id";

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_TEMPERATURE = "currentTemperature";
    public static final String CHANNEL_CURRENT_HUMIDITY = "currentHumidity";
    public static final String CHANNEL_CURRENT_SETPOINT = "currentSetPoint";
    public static final String CHANNEL_CURRENT_BATTERY_VOLTAGE = "currentBatteryVoltage";
    public static final String CHANNEL_CURRENT_BATTERY_LEVEL = "currentBatteryLevel";
    public static final String CHANNEL_CURRENT_WISER_BATTERY_LEVEL = "currentWiserBatteryLevel";
    public static final String CHANNEL_CURRENT_DEMAND = "currentDemand";
    public static final String CHANNEL_HEAT_REQUEST = "heatRequest";
    public static final String CHANNEL_CURRENT_SIGNAL_RSSI = "currentSignalRSSI";
    public static final String CHANNEL_CURRENT_SIGNAL_LQI = "currentSignalLQI";
    public static final String CHANNEL_CURRENT_SIGNAL_STRENGTH = "currentSignalStrength";
    public static final String CHANNEL_CURRENT_WISER_SIGNAL_STRENGTH = "currentWiserSignalStrength";
    public static final String CHANNEL_HEATING_OVERRIDE = "heatingOverride";
    public static final String CHANNEL_HOT_WATER_OVERRIDE = "hotWaterOverride";
    public static final String CHANNEL_HEATCHANNEL_1_DEMAND = "heatChannel1Demand";
    public static final String CHANNEL_HEATCHANNEL_2_DEMAND = "heatChannel2Demand";
    public static final String CHANNEL_HEATCHANNEL_1_DEMAND_STATE = "heatChannel1DemandState";
    public static final String CHANNEL_HEATCHANNEL_2_DEMAND_STATE = "heatChannel2DemandState";
    public static final String CHANNEL_HOTWATER_DEMAND_STATE = "hotWaterDemandState";
    public static final String CHANNEL_AWAY_MODE_STATE = "awayModeState";
    public static final String CHANNEL_ECO_MODE_STATE = "ecoModeState";
    public static final String CHANNEL_MANUAL_MODE_STATE = "manualModeState";
    public static final String CHANNEL_ZIGBEE_CONNECTED = "zigbeeConnected";
    public static final String CHANNEL_HOT_WATER_SETPOINT = "hotWaterSetPoint";
    public static final String CHANNEL_HOT_WATER_BOOST_DURATION = "hotWaterBoostDuration";
    public static final String CHANNEL_HOT_WATER_BOOSTED = "hotWaterBoosted";
    public static final String CHANNEL_HOT_WATER_BOOST_REMAINING = "hotWaterBoostRemaining";
    public static final String CHANNEL_ROOM_BOOST_DURATION = "roomBoostDuration";
    public static final String CHANNEL_ROOM_BOOSTED = "roomBoosted";
    public static final String CHANNEL_ROOM_BOOST_REMAINING = "roomBoostRemaining";
    public static final String CHANNEL_ROOM_WINDOW_STATE_DETECTION = "windowStateDetection";
    public static final String CHANNEL_ROOM_WINDOW_STATE = "windowState";
    public static final String CHANNEL_DEVICE_LOCKED = "deviceLocked";
    public static final String CHANNEL_SMARTPLUG_OUTPUT_STATE = "plugOutputState";
    public static final String CHANNEL_SMARTPLUG_AWAY_ACTION = "plugAwayAction";
    public static final String CHANNEL_COMFORT_MODE_STATE = "comfortModeState";
    public static final String CHANNEL_SMARTPLUG_INSTANTANEOUS_POWER = "plugInstantaneousPower";
    public static final String CHANNEL_SMARTPLUG_ENERGY_DELIVERED = "plugEnergyDelivered";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_CONTROLLER, THING_TYPE_ROOM,
            THING_TYPE_ROOMSTAT, THING_TYPE_BRIDGE, THING_TYPE_ITRV, THING_TYPE_HOTWATER, THING_TYPE_SMARTPLUG);

    // Lookups from text representations to useful values

    public enum SignalStrength {
        VERYGOOD(4),
        GOOD(3),
        MEDIUM(2),
        POOR(1),
        NOSIGNAL(0);

        private final int signalStrength;

        SignalStrength(final int signalStrength) {
            this.signalStrength = signalStrength;
        }

        public static State toSignalStrength(final String strength) {
            try {
                return new DecimalType(SignalStrength.valueOf(strength.toUpperCase()).signalStrength);
            } catch (final IllegalArgumentException e) {
                // Catch unrecognized values.
                return UnDefType.UNDEF;
            }
        }
    }

    public enum BatteryLevel {
        FULL(100),
        NORMAL(80),
        TWOTHIRDS(60),
        ONETHIRD(40),
        LOW(20),
        CRITICAL(0);

        private final int batteryLevel;

        private BatteryLevel(final int batteryLevel) {
            this.batteryLevel = batteryLevel;
        }

        public static State toBatteryLevel(final @Nullable String level) {
            if (level != null) {
                try {
                    return new DecimalType(BatteryLevel.valueOf(level.toUpperCase()).batteryLevel);
                } catch (final IllegalArgumentException e) {
                    // Catch unrecognized values.
                    return UnDefType.UNDEF;
                }
            } else {
                return UnDefType.UNDEF;
            }
        }
    }
}
