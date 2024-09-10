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
package org.openhab.binding.linktap.internal;

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linktap.protocol.frames.WaterMeterStatus;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link LinkTapBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LinkTapBindingConstants {

    private static final Type DEVICE_STATUS_CLASS_LIST_TYPE = new TypeToken<List<WaterMeterStatus.DeviceStatus>>() {
    }.getType();

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DEVICE_STATUS_CLASS_LIST_TYPE, new WaterMeterStatus.DeviceStatusClassTypeAdapter())
            .excludeFieldsWithoutExposeAnnotation().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .disableHtmlEscaping().create();

    private static final String BINDING_ID = "linktap";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    public static final String BRIDGE_PROP_GW_ID = "gatewayId";
    public static final String BRIDGE_PROP_HW_MODEL = "hardwareModel";
    public static final String BRIDGE_PROP_GW_VER = "version";
    public static final String BRIDGE_PROP_MAC_ADDR = "macAddress";
    public static final String BRIDGE_PROP_HTTP_API_ENABLED = "httpApiEnabled";
    public static final String BRIDGE_PROP_HTTP_API_EP = "httpApiCallback";
    public static final String BRIDGE_PROP_VOL_UNIT = "volumeUnit";
    public static final String BRIDGE_PROP_UTC_OFFSET = "utcOffset";
    public static final String BRIDGE_CONFIG_HOSTNAME = "host";
    public static final String BRIDGE_CONFIG_MDNS_ENABLE = "enableMDNS";
    public static final String BRIDGE_CONFIG_NON_HTML_COMM_ENABLE = "enableJSONComms";
    public static final String BRIDGE_CONFIG_ENFORCE_COMM_LIMITS = "enforceProtocolLimits";

    public static final String DEVICE_PROP_DEV_ID = "deviceId";
    public static final String DEVICE_PROP_DEV_NAME = "deviceName";
    public static final String DEVICE_CONFIG_DEV_ID = "id";
    public static final String DEVICE_CONFIG_DEV_NAME = "name";
    public static final String DEVICE_CONFIG_AUTO_ALERTS_ENABLE = "autoEnableAlerts";

    public static final String DEVICE_CHANNEL_WATERING_MODE = "mode";
    public static final String DEVICE_CHANNEL_IS_MANUAL_MODE = "manual-watering";
    public static final String DEVICE_CHANNEL_ACTIVE_WATERING = "watering";
    public static final String DEVICE_CHANNEL_RF_LINKED = "rf-linked";
    public static final String DEVICE_CHANNEL_FLM_LINKED = "flm-linked";
    public static final String DEVICE_CHANNEL_FALL_STATUS = "fall-status";
    public static final String DEVICE_CHANNEL_SHUTDOWN_FAILURE = "shutdown-failure";
    public static final String DEVICE_CHANNEL_HIGH_FLOW = "high-flow";
    public static final String DEVICE_CHANNEL_LOW_FLOW = "low-flow";
    public static final String DEVICE_CHANNEL_FINAL_SEGMENT = "eco-final";
    public static final String DEVICE_CHANNEL_SIGNAL = "signal";
    public static final String DEVICE_CHANNEL_BATTERY = "battery";
    public static final String DEVICE_CHANNEL_WATER_CUT = "water-cut";
    public static final String DEVICE_CHANNEL_CHILD_LOCK = "child-lock";
    public static final String DEVICE_CHANNEL_FLOW_RATE = "flow-rate";
    public static final String DEVICE_CHANNEL_CURRENT_VOLUME = "volume";
    public static final String DEVICE_CHANNEL_TOTAL_DURATION = "duration";
    public static final String DEVICE_CHANNEL_REMAIN_DURATION = "remaining";
    public static final String DEVICE_CHANNEL_FAILSAFE_DURATION = "dur-limit";
    public static final String DEVICE_CHANNEL_FAILSAFE_VOLUME = "vol-limit";
    public static final String DEVICE_CHANNEL_OH_VOLUME_LIMIT = "oh-vol-limit";
    public static final String DEVICE_CHANNEL_OH_DURATION_LIMIT = "oh-dur-limit";
    public static final String DEVICE_CHANNEL_PAUSE_PLAN_OVERRIDE = "plan-pause-enable";
    public static final String DEVICE_CHANNEL_PAUSE_PLAN_EXPIRES = "plan-resume-time";
    public static final String DEVICE_CHANNEL_WATER_PLAN_ID = "watering-plan-id";

    public enum WateringMode {

        /**
         * OFF (Ordinal 0).
         */
        OFF(0, "Off"),

        /**
         * INSTANT (Ordinal 1).
         */
        INSTANT(1, "Instant"),

        /**
         * CALENDAR (Ordinal 2).
         */
        CALENDAR(2, "Calendar"),

        /**
         * DAY (Ordinal 3).
         */
        DAY(3, "Day"),

        /**
         * ODD_EVEN (Ordinal 4).
         */
        ODD_EVEN(4, "Odd-even"),

        /**
         * INTERVAL (Ordinal 5).
         */
        INTERVAL(5, "Interval"),

        /**
         * MONTH (Ordinal 6).
         */
        MONTH(6, "Month");

        private final int value;
        private final String description;

        private WateringMode(final int value, final String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() {
            return value;
        }

        public String getDesc() {
            return description;
        }

        @Override
        public String toString() {
            return String.format("%d - %s", value, description);
        }
    }

    public enum ChildLockMode {

        /**
         * UNLOCKED (Ordinal 0).
         */
        UNLOCKED(0, "Unlocked"),

        /**
         * PART_LOCKED (Ordinal 1).
         */
        PART_LOCKED(1, "Partially locked"),

        /**
         * FULLY_LOCKED (Ordinal 2).
         */
        FULLY_LOCKED(2, "Completely locked");

        private final int value;
        private final String description;

        private ChildLockMode(final int value, final String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() {
            return value;
        }

        public String getDesc() {
            return description;
        }

        @Override
        public String toString() {
            return String.format("%d - %s", value, description);
        }
    }
}
