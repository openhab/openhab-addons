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
package org.openhab.binding.linktap.protocol.frames;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceCmdReq} is a payload representing the current status of the
 * water timer.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class WaterMeterStatus extends GatewayDeviceResponse {

    public WaterMeterStatus() {
    }

    @Override
    public ResultStatus getRes() {
        if (super.getRes() == ResultStatus.INVALID) {
            return ResultStatus.RET_SUCCESS;
        }
        return super.getRes();
    }

    public static class DeviceStatusClassTypeAdapter implements JsonDeserializer<List<DeviceStatus>> {
        public @Nullable List<DeviceStatus> deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext ctx) {
            List<DeviceStatus> vals = new ArrayList<>();
            if (json.isJsonArray()) {
                for (JsonElement e : json.getAsJsonArray()) {
                    vals.add(ctx.deserialize(e, DeviceStatus.class));
                }
            } else if (json.isJsonObject()) {
                vals.add(ctx.deserialize(json, DeviceStatus.class));
            }
            return vals;
        }
    }

    /**
     * Defines the device stat's for each device
     */
    @SerializedName("dev_stat")
    @Expose
    public List<DeviceStatus> deviceStatuses = new ArrayList<DeviceStatus>();

    public static class DeviceStatus implements IPayloadValidator {
        /**
         * Defines the targetted device ID
         */
        @SerializedName("dev_id")
        @Expose
        public String deviceId = EMPTY_STRING;

        /**
         * Defines the currently active plan (Operating Mode)
         */
        @SerializedName("plan_mode")
        @Expose
        public @Nullable Integer planMode;

        /**
         * Defines the serial number of the currently active plan
         */
        @SerializedName("plan_sn")
        @Expose
        public int planSerialNo = DEFAULT_INT;

        /**
         * Defines if the water timer is connected to the Gateway
         */
        @SerializedName("is_rf_linked")
        @Expose
        public @Nullable Boolean isRfLinked;

        /**
         * Defines whether the flow meter is plugin
         */
        @SerializedName("is_flm_plugin")
        @Expose
        public @Nullable Boolean isFlmPlugin;

        /**
         * Water timer fall alert status
         */
        @SerializedName("is_fall")
        @Expose
        public @Nullable Boolean isFall;

        /**
         * Valve shut-down failure alert status
         */
        @SerializedName("is_broken")
        @Expose
        public @Nullable Boolean isBroken;

        /**
         * Water cut-off alert status
         */
        @SerializedName("is_cutoff")
        @Expose
        public @Nullable Boolean isCutoff;

        /**
         * Unusually high flow alert status
         */
        @SerializedName("is_leak")
        @Expose
        public @Nullable Boolean isLeak;

        /**
         * Unusually low flow alert status
         */
        @SerializedName("is_clog")
        @Expose
        public @Nullable Boolean isClog;

        /**
         * Water timer signal reception level
         */
        @SerializedName("signal")
        @Expose
        public @Nullable Integer signal;

        /**
         * Water timer battery level
         */
        @SerializedName("battery")
        @Expose
        public @Nullable Integer battery;

        /**
         * Defines the lock in operation
         */
        @SerializedName("child_lock")
        @Expose
        public @Nullable Integer childLock;

        /**
         * Is manual watering currently on
         */
        @SerializedName("is_manual_mode")
        @Expose
        public @Nullable Boolean isManualMode;

        /**
         * Is watering currently on
         */
        @SerializedName("is_watering")
        @Expose
        public @Nullable Boolean isWatering = false;

        /**
         * When the ECO mode is enabled, the watering duration is divided into multiple "on-off-on-off" segments.
         * If is_final is true,it means current watering belongs to the last segment. If both is_watering and is_final
         * are false,it means that the watering is currently suspended (i.e. in midst of the segments), and there are
         * subsequent watering seqments to be executed.
         */
        @SerializedName("is_final")
        @Expose
        public @Nullable Boolean isFinal;

        /**
         * The duration of the current watering cycle in seconds
         */
        @SerializedName("total_duration")
        @Expose
        public @Nullable Integer totalDuration;

        /**
         * The remaining duration of the current watering cycle in seconds
         */
        @SerializedName("remain_duration")
        @Expose
        public @Nullable Integer remainDuration;

        /**
         * The failsafe duration of the current watering cycle in seconds
         */
        @SerializedName("failsafe_duration")
        @Expose
        public @Nullable Integer failsafeDuration;

        /**
         * The current water flow rate (LPN or GPM)
         */
        @SerializedName("speed")
        @Expose
        public @Nullable Double speed = 0.0d;

        /**
         * The accumulated volume of the current watering cycle (Litre or Gallon)
         */
        @SerializedName("volume")
        @Expose
        public @Nullable Double volume = 0.0d;

        /**
         * The volume limit of the current watering cycle (Litre or Gallon)
         */
        @SerializedName("volume_limit")
        @Expose
        public @Nullable Double volumeLimit = 0.0d;

        public Collection<ValidationError> getValidationErrors() {
            final Collection<ValidationError> errors = new ArrayList<>(0);

            final Integer planModeRaw = planMode;
            if (planModeRaw == null || planModeRaw < 1 || planModeRaw > OP_MODE_DESC.length) {
                errors.add(new ValidationError("planMode", "is not in range 1 -> " + OP_MODE_DESC.length));
            }

            final Integer signalRaw = signal;
            if (signalRaw == null || signalRaw < 0 || signalRaw > 100) {
                errors.add(new ValidationError("signal", "is not in range 0 -> 100"));
            }
            final Integer batteryRaw = battery;
            if (batteryRaw == null || batteryRaw < 0 || batteryRaw > 100) {
                errors.add(new ValidationError("battery", "is not in range 0 -> 100"));
            }
            if (planSerialNo == DEFAULT_INT) {
                errors.add(new ValidationError("plan_sn", "is invalid"));
            }
            final Integer childLockRaw = childLock;
            if (childLockRaw == null || childLockRaw < LockReq.LOCK_UNLOCKED || childLockRaw > LockReq.LOCK_FULL) {
                errors.add(new ValidationError("child_lock",
                        "is not in range " + LockReq.LOCK_UNLOCKED + " -> " + LockReq.LOCK_FULL));
            }
            if (!DEVICE_ID_PATTERN.matcher(deviceId).matches()) {
                errors.add(new ValidationError("dev_id", "is not in the expected format"));
            }

            return errors;
        }
    }

    public Collection<ValidationError> getValidationErrors() {
        return EMPTY_COLLECTION;
    }

    public static final int OP_MODE_INSTANT = 1;

    public static final int OP_MODE_CALENDAR = 2;

    public static final int OP_MODE_WEEK_TIMER = 3;

    public static final int OP_MODE_ODD_EVEN = 4;

    public static final int OP_MODE_INTERVAL = 5;

    public static final int OP_MODE_MONTH = 6;

    public static final String[] OP_MODE_DESC = new String[] { "Instant Mode", "Calendar Mode", "7 Day Mode",
            "Odd-Even Mode", "Interval Mode", "Month Mode" };
}
