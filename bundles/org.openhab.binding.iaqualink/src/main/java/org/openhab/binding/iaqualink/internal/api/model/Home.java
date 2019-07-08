/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.iaqualink.internal.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * {@link Home} refers to the "Home" screen of a pool controller.
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
public class Home {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("response")
    @Expose
    private String response;
    @SerializedName("system_type")
    @Expose
    private String systemType;
    @SerializedName("temp_scale")
    @Expose
    private String tempScale;
    @SerializedName("spa_temp")
    @Expose
    private String spaTemp;
    @SerializedName("pool_temp")
    @Expose
    private String poolTemp;
    @SerializedName("air_temp")
    @Expose
    private String airTemp;
    @SerializedName("spa_set_point")
    @Expose
    private String spaSetPoint;
    @SerializedName("pool_set_point")
    @Expose
    private String poolSetPoint;
    @SerializedName("cover_pool")
    @Expose
    private String coverPool;
    @SerializedName("freeze_protection")
    @Expose
    private String freezeProtection;
    @SerializedName("spa_pump")
    @Expose
    private String spaPump;
    @SerializedName("pool_pump")
    @Expose
    private String poolPump;
    @SerializedName("spa_heater")
    @Expose
    private String spaHeater;
    @SerializedName("pool_heater")
    @Expose
    private String poolHeater;
    @SerializedName("solar_heater")
    @Expose
    private String solarHeater;
    @SerializedName("spa_salinity")
    @Expose
    private String spaSalinity;
    @SerializedName("pool_salinity")
    @Expose
    private String poolSalinity;
    @SerializedName("orp")
    @Expose
    private String orp;
    @SerializedName("ph")
    @Expose
    private String ph;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public String getTempScale() {
        return tempScale;
    }

    public void setTempScale(String tempScale) {
        this.tempScale = tempScale;
    }

    public String getSpaTemp() {
        return spaTemp;
    }

    public void setSpaTemp(String spaTemp) {
        this.spaTemp = spaTemp;
    }

    public String getPoolTemp() {
        return poolTemp;
    }

    public void setPoolTemp(String poolTemp) {
        this.poolTemp = poolTemp;
    }

    public String getAirTemp() {
        return airTemp;
    }

    public void setAirTemp(String airTemp) {
        this.airTemp = airTemp;
    }

    public String getSpaSetPoint() {
        return spaSetPoint;
    }

    public void setSpaSetPoint(String spaSetPoint) {
        this.spaSetPoint = spaSetPoint;
    }

    public String getPoolSetPoint() {
        return poolSetPoint;
    }

    public void setPoolSetPoint(String poolSetPoint) {
        this.poolSetPoint = poolSetPoint;
    }

    public String getCoverPool() {
        return coverPool;
    }

    public void setCoverPool(String coverPool) {
        this.coverPool = coverPool;
    }

    public String getFreezeProtection() {
        return freezeProtection;
    }

    public void setFreezeProtection(String freezeProtection) {
        this.freezeProtection = freezeProtection;
    }

    public String getSpaPump() {
        return spaPump;
    }

    public void setSpaPump(String spaPump) {
        this.spaPump = spaPump;
    }

    public String getPoolPump() {
        return poolPump;
    }

    public void setPoolPump(String poolPump) {
        this.poolPump = poolPump;
    }

    public String getSpaHeater() {
        return spaHeater;
    }

    public void setSpaHeater(String spaHeater) {
        this.spaHeater = spaHeater;
    }

    public String getPoolHeater() {
        return poolHeater;
    }

    public void setPoolHeater(String poolHeater) {
        this.poolHeater = poolHeater;
    }

    public String getSolarHeater() {
        return solarHeater;
    }

    public void setSolarHeater(String solarHeater) {
        this.solarHeater = solarHeater;
    }

    public String getSpaSalinity() {
        return spaSalinity;
    }

    public void setSpaSalinity(String spaSalinity) {
        this.spaSalinity = spaSalinity;
    }

    public String getPoolSalinity() {
        return poolSalinity;
    }

    public void setPoolSalinity(String poolSalinity) {
        this.poolSalinity = poolSalinity;
    }

    public String getOrp() {
        return orp;
    }

    public void setOrp(String orp) {
        this.orp = orp;
    }

    public String getPh() {
        return ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }

}