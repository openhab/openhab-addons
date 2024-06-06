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
package org.openhab.binding.gridbox.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * {@link BatterySummary} is a gson-mapped class that will be used to contain summarized information of all batteries in
 * a GridBox API response
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class BatterySummary {

    @SerializedName("capacity")
    @Expose
    private long capacity;

    @SerializedName("nominalCapacity")
    @Expose
    private long nominalCapacity;

    @SerializedName("power")
    @Expose
    private long power;

    @SerializedName("remainingCharge")
    @Expose
    private long remainingCharge;

    @SerializedName("stateOfCharge")
    @Expose
    private long stateOfCharge;

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getNominalCapacity() {
        return nominalCapacity;
    }

    public void setNominalCapacity(long nominalCapacity) {
        this.nominalCapacity = nominalCapacity;
    }

    public long getPower() {
        return power;
    }

    public void setPower(long power) {
        this.power = power;
    }

    public long getRemainingCharge() {
        return remainingCharge;
    }

    public void setRemainingCharge(long remainingCharge) {
        this.remainingCharge = remainingCharge;
    }

    public long getStateOfCharge() {
        return stateOfCharge;
    }

    public void setStateOfCharge(long stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
    }

    @Override
    public String toString() {
        return "BatterySummary [capacity=" + capacity + ", nominalCapacity=" + nominalCapacity + ", power=" + power
                + ", remainingCharge=" + remainingCharge + ", stateOfCharge=" + stateOfCharge + "]";
    }
}
