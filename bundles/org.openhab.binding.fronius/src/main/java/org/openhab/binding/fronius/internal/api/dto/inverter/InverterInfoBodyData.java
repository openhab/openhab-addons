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
package org.openhab.binding.fronius.internal.api.dto.inverter;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link InverterInfoBodyData} is responsible for storing the "Data" node of the {@link InverterInfoBody}.
 *
 * @author Florian Hotze - Initial contribution
 */
public class InverterInfoBodyData {
    @SerializedName("DT")
    private Integer deviceType;

    @SerializedName("PVPower")
    private Integer pvPower;

    @SerializedName("CustomName")
    private String customName;

    @SerializedName("Show")
    private Integer show;

    @SerializedName("UniqueID")
    private Integer uniqueID;

    @SerializedName("ErrorCode")
    private Integer errorCode;

    @SerializedName("StatusCode")
    private Integer statusCode;

    @SerializedName("InverterState")
    private String inverterState;

    public Integer getDeviceType() {
        return deviceType;
    }

    public Integer getPvPower() {
        return pvPower;
    }

    public String getCustomName() {
        return customName;
    }

    public Integer getShow() {
        return show;
    }

    public Integer getUniqueID() {
        return uniqueID;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getInverterState() {
        return inverterState;
    }
}
