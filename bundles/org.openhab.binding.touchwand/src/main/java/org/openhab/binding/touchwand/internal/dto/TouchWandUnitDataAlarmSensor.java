/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.touchwand.internal.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link TouchWandUnitDataAlarmSensor} implements WallController unit
 * property.
 *
 * @author Roie Geron - Initial contribution
 */
public class TouchWandUnitDataAlarmSensor extends TouchWandUnitData {

    @SerializedName("idData")
    @Expose
    private IdData idData;

    public IdData getIdData() {
        return idData;
    }

    public void setIdData(IdData idData) {
        this.idData = idData;
    }

    @SerializedName("currStatus")
    @Expose
    private AlarmSensorCurrStatus currStatus;

    @Override
    public AlarmSensorCurrStatus getCurrStatus() {
        return this.currStatus;
    }

    public void setCurrStatus(AlarmSensorCurrStatus currStatus) {
        this.currStatus = currStatus;
    }

}
