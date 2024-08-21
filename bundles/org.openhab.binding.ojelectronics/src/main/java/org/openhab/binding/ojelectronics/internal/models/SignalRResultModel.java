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
    private List<GroupContentModel> groups = List.of();
    @SerializedName("SequenceNr")
    private int sequenceNr;

    @SerializedName("ThermostatRealTimes")
    private List<ThermostatRealTimeValuesModel> thermostatRealTimes = List.of();

    @SerializedName("Thermostats")
    private List<ThermostatModel> thermostats = List.of();

    public List<GroupContentModel> getGroups() {
        return this.groups;
    }

    public int getSequenceNr() {
        return this.sequenceNr;
    }

    public List<ThermostatRealTimeValuesModel> getThermostatRealTimes() {
        return this.thermostatRealTimes;
    }

    public List<ThermostatModel> getThermostats() {
        return this.thermostats;
    }

    public void setGroups(List<GroupContentModel> paramArrayList) {
        this.groups = paramArrayList;
    }

    public void setSequenceNr(int paramInt) {
        this.sequenceNr = paramInt;
    }

    public void setThermostatRealTimes(List<ThermostatRealTimeValuesModel> paramArrayList) {
        this.thermostatRealTimes = paramArrayList;
    }

    public void setThermostats(List<ThermostatModel> paramArrayList) {
        this.thermostats = paramArrayList;
    }
}
