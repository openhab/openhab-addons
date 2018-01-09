/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RoomStat {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("SetPoint")
    @Expose
    private Integer setPoint;
    @SerializedName("MeasuredTemperature")
    @Expose
    private Integer measuredTemperature;
    @SerializedName("MeasuredHumidity")
    @Expose
    private Integer measuredHumidity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSetPoint() {
        return setPoint < 0 ? 0 : setPoint;
    }

    public void setSetPoint(Integer setPoint) {
        this.setPoint = setPoint;
    }

    public Integer getMeasuredTemperature() {
        return measuredTemperature;
    }

    public void setMeasuredTemperature(Integer measuredTemperature) {
        this.measuredTemperature = measuredTemperature;
    }

    public Integer getMeasuredHumidity() {
        return measuredHumidity;
    }

    public void setMeasuredHumidity(Integer measuredHumidity) {
        this.measuredHumidity = measuredHumidity;
    }

}