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

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link IdData} implements IdData object.
 *
 * @author Roie Geron - Initial contribution
 */

public class IdData {

    @SerializedName("alarmTypes")
    @Expose
    private List<Integer> alarmTypes = null;
    @SerializedName("alarmDescs")
    @Expose
    private List<String> alarmDescs = null;
    @SerializedName("sensorTypes")
    @Expose
    private List<Integer> sensorTypes = null;
    @SerializedName("sensorDescs")
    @Expose
    private List<String> sensorDescs = null;
    // @SerializedName("alarmEvtTypes")
    // @Expose
    // private AlarmEvtTypes alarmEvtTypes;
    @SerializedName("alarmSensorTypes")
    @Expose
    private List<Integer> alarmSensorTypes = null;
    @SerializedName("alarmSensorDescs")
    @Expose
    private List<String> alarmSensorDescs = null;

    public List<Integer> getAlarmTypes() {
        return alarmTypes;
    }

    public void setAlarmTypes(List<Integer> alarmTypes) {
        this.alarmTypes = alarmTypes;
    }

    public List<String> getAlarmDescs() {
        return alarmDescs;
    }

    public void setAlarmDescs(List<String> alarmDescs) {
        this.alarmDescs = alarmDescs;
    }

    public List<Integer> getSensorTypes() {
        return sensorTypes;
    }

    public void setSensorTypes(List<Integer> sensorTypes) {
        this.sensorTypes = sensorTypes;
    }

    public List<String> getSensorDescs() {
        return sensorDescs;
    }

    public void setSensorDescs(List<String> sensorDescs) {
        this.sensorDescs = sensorDescs;
    }

    // public AlarmEvtTypes getAlarmEvtTypes() {
    // return alarmEvtTypes;
    // }
    //
    // public void setAlarmEvtTypes(AlarmEvtTypes alarmEvtTypes) {
    // this.alarmEvtTypes = alarmEvtTypes;
    // }

    public List<Integer> getAlarmSensorTypes() {
        return alarmSensorTypes;
    }

    public void setAlarmSensorTypes(List<Integer> alarmSensorTypes) {
        this.alarmSensorTypes = alarmSensorTypes;
    }

    public List<String> getAlarmSensorDescs() {
        return alarmSensorDescs;
    }

    public void setAlarmSensorDescs(List<String> alarmSensorDescs) {
        this.alarmSensorDescs = alarmSensorDescs;
    }

}