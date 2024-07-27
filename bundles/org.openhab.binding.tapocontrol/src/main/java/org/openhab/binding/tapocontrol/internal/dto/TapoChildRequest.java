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
package org.openhab.binding.tapocontrol.internal.dto;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.reactivex.annotations.Nullable;

/**
 * Holds child data sent to device
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Christian Wild - Code revision
 */
@NonNullByDefault
public record TapoChildRequest(@Expose String method, @Expose @Nullable Object params,
        @Expose long requestTimeMils) implements TapoBaseRequestInterface {

    private record ControlRequest(@Expose @SerializedName("device_id") String deviceId, @Expose Object requestData) {
    }

    /**
     * Create request to control child devices
     */
    public TapoChildRequest(TapoChildDeviceData deviceData) {
        this(DEVICE_CMD_CONTROL_CHILD,
                new ControlRequest(deviceData.getDeviceId(), new TapoRequest(DEVICE_CMD_SETINFO, deviceData)),
                System.currentTimeMillis());
    }

    /***********************************************
     * RETURN VALUES
     **********************************************/

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
