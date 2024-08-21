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
package org.openhab.binding.solax.internal.connectivity.rawdata.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Result} is a sub-class used in the JSON response which provides the actual inverter data from the cloud.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class Result {

    private @Nullable String inverterSN;
    private @Nullable String sn;
    @SerializedName("acpower")
    private @Nullable Double acPower;
    @SerializedName("yieldtoday")
    private @Nullable Double yieldToday;
    @SerializedName("yieldtotal")
    private @Nullable Double yieldTotal;
    @SerializedName("feedinpower")
    private @Nullable Double feedInPower;
    @SerializedName("feedinenergy")
    private @Nullable Double feedInEnergy;
    @SerializedName("consumeenergy")
    private @Nullable Double consumeEnergy;
    @SerializedName("feedinpowerM2")
    private @Nullable Double feedInPowerM2;
    private @Nullable Double soc;
    private @Nullable Double peps1;
    private @Nullable Double peps2;
    private @Nullable Double peps3;
    private int inverterType;
    private int inverterStatus;
    private @Nullable String uploadTime;
    private @Nullable Double batPower;
    @SerializedName("powerdc1")
    private @Nullable Double powerDc1;
    @SerializedName("powerdc2")
    private @Nullable Double powerDc2;
    @SerializedName("powerdc3")
    private @Nullable Double powerDc3;
    @SerializedName("powerdc4")
    private @Nullable Double powerDc4;
    private int batStatus;

    public @Nullable String getInverterSN() {
        return inverterSN;
    }

    public void setInverterSN(String inverterSN) {
        this.inverterSN = inverterSN;
    }

    public @Nullable String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public @Nullable Double getAcPower() {
        return acPower;
    }

    public void setAcPower(Double acPower) {
        this.acPower = acPower;
    }

    public @Nullable Double getYieldToday() {
        return yieldToday;
    }

    public void setYieldToday(Double yieldToday) {
        this.yieldToday = yieldToday;
    }

    public @Nullable Double getYieldTotal() {
        return yieldTotal;
    }

    public void setYieldTotal(Double yieldTotal) {
        this.yieldTotal = yieldTotal;
    }

    public @Nullable Double getFeedInPower() {
        return feedInPower;
    }

    public void setFeedInPower(Double feedInPower) {
        this.feedInPower = feedInPower;
    }

    public @Nullable Double getFeedInEnergy() {
        return feedInEnergy;
    }

    public void setFeedInEnergy(Double feedInEnergy) {
        this.feedInEnergy = feedInEnergy;
    }

    public @Nullable Double getConsumeEnergy() {
        return consumeEnergy;
    }

    public void setConsumeEnergy(Double consumeEnergy) {
        this.consumeEnergy = consumeEnergy;
    }

    public @Nullable Double getFeedInPowerM2() {
        return feedInPowerM2;
    }

    public void setFeedInPowerM2(Double feedInPowerM2) {
        this.feedInPowerM2 = feedInPowerM2;
    }

    public @Nullable Double getSoc() {
        return soc;
    }

    public void setSoc(Double soc) {
        this.soc = soc;
    }

    public @Nullable Double getPeps1() {
        return peps1;
    }

    public void setPeps1(Double peps1) {
        this.peps1 = peps1;
    }

    public @Nullable Double getPeps2() {
        return peps2;
    }

    public void setPeps2(Double peps2) {
        this.peps2 = peps2;
    }

    public @Nullable Double getPeps3() {
        return peps3;
    }

    public void setPeps3(Double peps3) {
        this.peps3 = peps3;
    }

    public int getInverterType() {
        return inverterType;
    }

    public void setInverterType(int inverterType) {
        this.inverterType = inverterType;
    }

    public int getInverterStatus() {
        return inverterStatus;
    }

    public void setInverterStatus(int inverterStatus) {
        this.inverterStatus = inverterStatus;
    }

    public @Nullable String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public @Nullable Double getBatPower() {
        return batPower;
    }

    public void setBatPower(Double batPower) {
        this.batPower = batPower;
    }

    public @Nullable Double getPowerDc1() {
        return powerDc1;
    }

    public void setPowerDc1(Double powerDc1) {
        this.powerDc1 = powerDc1;
    }

    public @Nullable Double getPowerDc2() {
        return powerDc2;
    }

    public void setPowerDc2(Double powerDc2) {
        this.powerDc2 = powerDc2;
    }

    public @Nullable Double getPowerDc3() {
        return powerDc3;
    }

    public void setPowerDc3(Double powerDc3) {
        this.powerDc3 = powerDc3;
    }

    public @Nullable Double getPowerDc4() {
        return powerDc4;
    }

    public void setPowerDc4(Double powerDc4) {
        this.powerDc4 = powerDc4;
    }

    public int getBatStatus() {
        return batStatus;
    }

    public void setBatStatus(int batStatus) {
        this.batStatus = batStatus;
    }

    @Override
    public String toString() {
        return "Result [inverterSN=" + inverterSN + ", sn=" + sn + ", acPower=" + acPower + ", yieldToday=" + yieldToday
                + ", yieldTotal=" + yieldTotal + ", feedInPower=" + feedInPower + ", feedInEnergy=" + feedInEnergy
                + ", consumeEnergy=" + consumeEnergy + ", feedInPowerM2=" + feedInPowerM2 + ", soc=" + soc + ", peps1="
                + peps1 + ", peps2=" + peps2 + ", peps3=" + peps3 + ", inverterType=" + inverterType
                + ", inverterStatus=" + inverterStatus + ", uploadTime=" + uploadTime + ", batPower=" + batPower
                + ", powerDc1=" + powerDc1 + ", powerDc2=" + powerDc2 + ", powerDc3=" + powerDc3 + ", powerDc4="
                + powerDc4 + ", batStatus=" + batStatus + "]";
    }
}
