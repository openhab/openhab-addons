/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.iaqualink.internal.api.dto;

import java.util.Map;

/**
 * {@link Home} refers to the "Home" screen of a pool controller.
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
public class Home {

    private String status;

    private String response;

    private String systemType;

    private String tempScale;

    private String spaTemp;

    private String poolTemp;

    private String airTemp;

    private String spaSetPoint;

    private String poolSetPoint;

    private String coverPool;

    private String freezeProtection;

    private String spaPump;

    private String poolPump;

    private String spaHeater;

    private String poolHeater;

    private String solarHeater;

    private String spaSalinity;

    private String poolSalinity;

    private String orp;

    private String ph;

    private Map<String, String> serializedMap;

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

    public Map<String, String> getSerializedMap() {
        return serializedMap;
    }
}
