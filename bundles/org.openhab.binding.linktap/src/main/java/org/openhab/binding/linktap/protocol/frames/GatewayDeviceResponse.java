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

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link GatewayDeviceResponse} defines the response from the Gateway when a status
 * is given about the state of the requested command.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class GatewayDeviceResponse extends TLGatewayFrame {

    public GatewayDeviceResponse() {
    }

    /**
     * Defines the processing result from the gateway
     */
    @SerializedName("ret")
    @Expose(serialize = false, deserialize = true)
    private @Nullable Integer returnValue = null;
    @Expose(serialize = false, deserialize = false)
    private ResultStatus cachedResEnum = ResultStatus.INVALID;

    public ResultStatus getRes() {
        if (cachedResEnum == ResultStatus.INVALID) {
            final Integer retValClone = returnValue;
            if (retValClone != null) {
                cachedResEnum = ResultStatus.values()[retValClone.intValue()];
            }
        }
        return cachedResEnum;
    }

    public boolean isSuccess() {
        return ResultStatus.RET_SUCCESS == getRes();
    }

    public boolean isRetryableError() {
        switch (getRes()) {
            case RET_CONFLICT_WATER_PLAN: // Conflict with watering plan
            case RET_GW_INTERNAL_ERR: // Gateway internal error
                return true;
            default:
                return false;
        }
    }

    public Collection<ValidationError> getValidationErrors() {
        final Collection<ValidationError> errors = super.getValidationErrors();

        if (ResultStatus.INVALID == getRes()) {
            errors.add(new ValidationError("res", "is invalid"));
        }

        return errors;
    }

    public enum ResultStatus {

        /**
         * RET_SUCCESS (Ordinal 0).
         */
        RET_SUCCESS(0, "Success", false),

        /**
         * RET_MESSAGE_FORMAT_ERR (Ordinal 1).
         */
        RET_MESSAGE_FORMAT_ERR(1, "Message format error", false),

        /**
         * RET_CMD_NOT_SUPPORTED (Ordinal 2).
         */
        RET_CMD_NOT_SUPPORTED(2, "CMD message not supported", false),

        /**
         * RET_GATEWAY_ID_NOT_MATCHED (Ordinal 3).
         */
        RET_GATEWAY_ID_NOT_MATCHED(3, "Gateway ID not matched", false),

        /**
         * RET_DEVICE_ID_ERROR (Ordinal 4).
         */
        RET_DEVICE_ID_ERROR(4, "End device ID error", false),

        /**
         * RET_DEVICE_NOT_FOUND (Ordinal 5).
         */
        RET_DEVICE_NOT_FOUND(5, "End device ID not found", false),

        /**
         * RET_GW_INTERNAL_ERR (Ordinal 6).
         */
        RET_GW_INTERNAL_ERR(6, "Gateway internal error", true),

        /**
         * RET_CONFLICT_WATER_PLAN (Ordinal 7).
         */
        RET_CONFLICT_WATER_PLAN(7, "Conflict with watering plan", false),

        /**
         * RET_GATEWAY_BUSY (Ordinal 8).
         */
        RET_GATEWAY_BUSY(8, "Gateway busy", true),

        /**
         * RET_BAD_PARAMETER (Ordinal 9).
         */
        RET_BAD_PARAMETER(9, "Bad parameter in message", false),

        /**
         * INVALID (Ordinal -1).
         */
        INVALID(-1, "Not Provided", false);

        private final int value;
        private final String description;
        private final boolean retry;

        private ResultStatus(final int value, final String description, final boolean retry) {
            this.value = value;
            this.description = description;
            this.retry = retry;
        }

        public int getValue() {
            return value;
        }

        public String getDesc() {
            return description;
        }

        public boolean getCanRetry() {
            return retry;
        }

        @Override
        public String toString() {
            return String.format("%d - %s", value, description);
        }
    }

    /*
     * public final static int RET_SUCCESS = 0;
     * public final static int RET_MESSAGE_FORMAT_ERR = 1;
     * public final static int RET_CMD_NOT_SUPPORTED = 2;
     * public final static int RET_GATEWAY_ID_NOT_MATCHED = 3;
     * public final static int RET_DEVICE_ID_ERROR = 4;
     * public final static int RET_DEVICE_NOT_FOUND = 5;
     * public final static int RET_GW_INTERNAL_ERR = 6;
     * public final static int RET_CONFLICT_WATER_PLAN = 7;
     * public final static int RET_GATEWAY_BUSY = 8;
     * public final static int RET_BAD_PARAMETER = 9;
     */
}
