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
package org.openhab.binding.ojelectronics.internal.models;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContentModel;
import org.openhab.binding.ojelectronics.internal.models.thermostat.ThermostatModel;
import org.openhab.binding.ojelectronics.internal.models.thermostat.ThermostatRealTimeValuesModel;

import com.google.gson.annotations.SerializedName;

/**
 * Model for a SignalR query result
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class SignalRResultModel {
    @SerializedName("Groups")
    private List<GroupContentModel> mGroups = List.of();
    @SerializedName("SequenceNr")
    private int mSequenceNr;

    @SerializedName("ThermostatRealTimes")
    private List<ThermostatRealTimeValuesModel> mThermostatRealTimes = List.of();

    @SerializedName("Thermostats")
    private List<ThermostatModel> mThermostats = List.of();

    public List<GroupContentModel> getGroups() {
        return this.mGroups;
    }

    public int getSequenceNr() {
        return this.mSequenceNr;
    }

    public List<ThermostatRealTimeValuesModel> getThermostatRealTimes() {
        return this.mThermostatRealTimes;
    }

    public List<ThermostatModel> getThermostats() {
        return this.mThermostats;
    }

    public void setGroups(List<GroupContentModel> paramArrayList) {
        this.mGroups = paramArrayList;
    }

    public void setSequenceNr(int paramInt) {
        this.mSequenceNr = paramInt;
    }

    public void setThermostatRealTimes(List<ThermostatRealTimeValuesModel> paramArrayList) {
        this.mThermostatRealTimes = paramArrayList;
    }

    public void setThermostats(List<ThermostatModel> paramArrayList) {
        this.mThermostats = paramArrayList;
    }
}
