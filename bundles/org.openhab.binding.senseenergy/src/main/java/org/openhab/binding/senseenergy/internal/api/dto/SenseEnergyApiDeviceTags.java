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
package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyApiDeviceTags } is the dto for tag info inside the SenseEnergyApiDevice dto class
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiDeviceTags {
    public enum Stage {
        Tracking,
        Inventory
    }

    @SerializedName("Stage")
    public Stage stage;
    @SerializedName("UserDeleted")
    public boolean userDeleted;
    @SerializedName("AlwaysOn")
    public boolean alwaysOn;
    @SerializedName("DUID")
    public String deviceID;
    @SerializedName("SSIEnabled")
    public boolean ssiEnabled;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SenseEnergyApiDeviceTags that = (SenseEnergyApiDeviceTags) o;
        if (userDeleted != that.userDeleted)
            return false;
        if (alwaysOn != that.alwaysOn)
            return false;
        if (ssiEnabled != that.ssiEnabled)
            return false;
        if (stage != that.stage)
            return false;
        if (deviceID != null ? !deviceID.equals(that.deviceID) : that.deviceID != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = stage != null ? stage.hashCode() : 0;
        result = 31 * result + (userDeleted ? 1 : 0);
        result = 31 * result + (alwaysOn ? 1 : 0);
        result = 31 * result + (deviceID != null ? deviceID.hashCode() : 0);
        result = 31 * result + (ssiEnabled ? 1 : 0);
        return result;
    }
}
