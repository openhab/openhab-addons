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
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * {@link EvChargingStation} is a gson-mapped class that will be used to contain information of one EV charging
 * station/wallbox in a GridBox API response
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class EvChargingStation {

    @SerializedName("applianceID")
    @Expose
    @Nullable
    private String applianceID;

    @SerializedName("currentL1")
    @Expose
    private long currentL1;

    @SerializedName("currentL2")
    @Expose
    private long currentL2;

    @SerializedName("currentL3")
    @Expose
    private long currentL3;

    @SerializedName("plugState")
    @Expose
    @Nullable
    private String plugState;

    @SerializedName("power")
    @Expose
    private long power;

    @SerializedName("readingTotal")
    @Expose
    private long readingTotal;

    @Nullable
    public String getApplianceID() {
        return applianceID;
    }

    public void setApplianceID(String applianceID) {
        this.applianceID = applianceID;
    }

    public long getCurrentL1() {
        return currentL1;
    }

    public void setCurrentL1(long currentL1) {
        this.currentL1 = currentL1;
    }

    public long getCurrentL2() {
        return currentL2;
    }

    public void setCurrentL2(long currentL2) {
        this.currentL2 = currentL2;
    }

    public long getCurrentL3() {
        return currentL3;
    }

    public void setCurrentL3(long currentL3) {
        this.currentL3 = currentL3;
    }

    @Nullable
    public String getPlugState() {
        return plugState;
    }

    public void setPlugState(String plugState) {
        this.plugState = plugState;
    }

    public long getPower() {
        return power;
    }

    public void setPower(long power) {
        this.power = power;
    }

    public long getReadingTotal() {
        return readingTotal;
    }

    public void setReadingTotal(long readingTotal) {
        this.readingTotal = readingTotal;
    }

    @Override
    public String toString() {
        return "EvChargingStation [applianceID=" + applianceID + ", currentL1=" + currentL1 + ", currentL2=" + currentL2
                + ", currentL3=" + currentL3 + ", plugState=" + plugState + ", power=" + power + ", readingTotal="
                + readingTotal + "]";
    }
}
