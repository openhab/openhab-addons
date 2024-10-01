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

import static org.openhab.binding.linktap.protocol.frames.HandshakeResp.DATE_FORMATTER;
import static org.openhab.binding.linktap.protocol.frames.ValidationError.Cause.BUG;
import static org.openhab.binding.linktap.protocol.frames.ValidationError.Cause.USER;
import static org.openhab.binding.linktap.protocol.frames.WaterMeterStatus.OP_MODE_INSTANT;
import static org.openhab.binding.linktap.protocol.frames.WaterMeterStatus.OP_MODE_MONTH;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link SetupWaterPlan} defines the request to dismiss alerts from a given device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public abstract class SetupWaterPlan extends DeviceCmdReq {

    public SetupWaterPlan() {
    }

    /**
     * Defines the unique identifier for the instance of the watering plan
     * that we are sending.
     */
    @SerializedName("plan_sn")
    @Expose
    public int planSerialNo = DEFAULT_INT;

    /**
     * Defines the watering mode which can be:
     * watering mode (1 - Instant Mode, 2 - Calendar mode, 3 - 7 day mode, 4 - Odd-even mode, 5
     * - Interval mode, 6 - Month mode).
     * See OP_MODE_INSTANT....
     */
    @SerializedName("mode")
    @Expose
    public int mode = DEFAULT_INT;

    /**
     * Defines the eco mode options... only used by Instant mode but in all payloads
     * eco: the ECO mode works in a way that the valve opens for X seconds then closes
     * for Y seconds “X, Y, X, Y, …“. in [X, Y], X denotes the Valve ON duration,
     * Y denotes Valve OFF duration. The ECO mode will not be applied if either X or Y is zero.
     */
    protected int[] eco = new int[] { 0, 0 };

    /**
     * Defines the watering plan information for the mode specified
     */
    @SerializedName("sch")
    @Expose
    public WaterSchedule schedule = new WaterSchedule();

    protected class WaterSchedule implements IPayloadValidator {
        @Override
        public Collection<ValidationError> getValidationErrors() {
            return EMPTY_COLLECTION;
        }
    }

    public Collection<ValidationError> getValidationErrors() {
        final Collection<ValidationError> errors = super.getValidationErrors();

        if (planSerialNo == DEFAULT_INT) {
            errors.add(new ValidationError("plan_sn", "is invalid"));
        }
        if (mode < OP_MODE_INSTANT || mode > OP_MODE_MONTH) {
            errors.add(new ValidationError("mode", "mode not in range " + OP_MODE_INSTANT + " -> " + OP_MODE_MONTH));
        }
        if (eco.length != 2) {
            errors.add(new ValidationError("eco", "number of parameters is incorrect"));
        }
        return errors;
    }

    public class WaterPlanInstant extends WaterSchedule {

        /**
         * Defines the timestamp (YYYYMMDDHHMMSS) that the Instant Mode will take effect
         */
        @SerializedName("timestamp")
        @Expose
        public String timestamp = EMPTY_STRING;

        /**
         * Defines the target capacity for a watering session, measured in liters or gallons
         * (according to the Volume unit configuration in the gateway management page).
         * The minimum value is 1. When the water timer has a flow meter connected, if its
         * value is greater than 0, the watering process is controlled by both "volume" and "duration."
         * The watering stops when either of these conditions is met.
         * (Note: G1S does not support "watering by volume". D1, which includes two integrated flow meters,
         * supports "watering by volume")
         */
        @SerializedName("volume")
        @Expose
        public int volume = DEFAULT_INT;

        /**
         * This defines the watering duration in seconds. (Please note: for G1 & G2 models which support "watering
         * by minute" only, the duration value here needs to be an integral multiple of 60 seconds. For the G1S & G2S
         * and future models, the duration value can be any integer between 3 and 86399.)
         */
        @SerializedName("duration")
        @Expose
        public int duration = DEFAULT_INT;

        @Override
        public Collection<ValidationError> getValidationErrors() {
            final Collection<ValidationError> errors = new ArrayList<>(0);
            if (duration < 3 || duration > 86399) {
                errors.add(new ValidationError("duration", "not in range 3 -> 86340", USER));
            }
            if (volume < 1) {
                errors.add(new ValidationError("volume", "is less than 1", USER));
            }
            if (timestamp.length() != 14) {
                errors.add(new ValidationError("timestamp", "is not 14 characters long", BUG));
            }
            try {
                LocalDate.parse(timestamp.substring(0, 8), DATE_FORMATTER);
                LocalTime.parse(timestamp.substring(9), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add(new ValidationError("timestamp", "is invalid", BUG));
            }

            return errors;
        }
    }
}
