/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContent;
import org.openhab.binding.ojelectronics.internal.models.thermostat.Thermostat;
import org.openhab.binding.ojelectronics.internal.models.thermostat.ThermostatRealTimeValues;

import com.google.gson.annotations.SerializedName;

/**
 * Model for a SignalR query result
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class SignalRResultModel {
    @SerializedName("Groups")
    private @Nullable ArrayList<GroupContent> mGroups;
    @SerializedName("SequenceNr")
    private int mSequenceNr;

    @SerializedName("ThermostatRealTimes")
    private @Nullable ArrayList<ThermostatRealTimeValues> mThermostatRealTimes;

    @SerializedName("Thermostats")
    private @Nullable ArrayList<Thermostat> mThermostats;

    public @Nullable ArrayList<GroupContent> getGroups() {
        return this.mGroups;
    }

    public int getSequenceNr() {
        return this.mSequenceNr;
    }

    public @Nullable ArrayList<ThermostatRealTimeValues> getThermostatRealTimes() {
        return this.mThermostatRealTimes;
    }

    public @Nullable ArrayList<Thermostat> getThermostats() {
        return this.mThermostats;
    }

    public void setGroups(ArrayList<GroupContent> paramArrayList) {
        this.mGroups = paramArrayList;
    }

    public void setSequenceNr(int paramInt) {
        this.mSequenceNr = paramInt;
    }

    public void setThermostatRealTimes(ArrayList<ThermostatRealTimeValues> paramArrayList) {
        this.mThermostatRealTimes = paramArrayList;
    }

    public void setThermostats(ArrayList<Thermostat> paramArrayList) {
        this.mThermostats = paramArrayList;
    }
}
