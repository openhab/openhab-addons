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
package org.openhab.binding.draytonwiser.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class RoomStatDTO {

    @SerializedName("id")
    private Integer id;
    private int setPoint;
    private Integer measuredTemperature;
    private Integer measuredHumidity;

    public Integer getId() {
        return id;
    }

    public int getSetPoint() {
        return Math.max(0, setPoint);
    }

    public void setSetPoint(final Integer setPoint) {
        this.setPoint = setPoint;
    }

    public Integer getMeasuredTemperature() {
        return measuredTemperature;
    }

    public Integer getMeasuredHumidity() {
        return measuredHumidity;
    }
}
