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

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

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
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SenseEnergyApiDeviceTags that = (SenseEnergyApiDeviceTags) o;
        return userDeleted == that.userDeleted && alwaysOn == that.alwaysOn && ssiEnabled == that.ssiEnabled
                && Objects.equals(stage, that.stage) && Objects.equals(deviceID, that.deviceID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stage, userDeleted, alwaysOn, deviceID, ssiEnabled);
    }
}
