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

package org.openhab.binding.touchwand.internal.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link AlarmSensorCurrStatus} implements CurrStatus data class.
 *
 * @author Roie Geron - Initial contribution
 */

public class AlarmSensorCurrStatus {

    @SerializedName("batt")
    @Expose
    private Integer batt;
    // @SerializedName("alarm_7")
    // @Expose
    // private Alarm7 alarm7;
    // @SerializedName("sensor_1")
    // @Expose
    // private Double sensor1;
    // @SerializedName("alrm_snsr")
    // @Expose
    // private AlrmSnsr alrmSnsr;

    public Integer getBatt() {
        return batt;
    }

    public void setBatt(Integer batt) {
        this.batt = batt;
    }

    // public Alarm7 getAlarm7() {
    // return alarm7;
    // }
    //
    // public void setAlarm7(Alarm7 alarm7) {
    // this.alarm7 = alarm7;
    // }
    //
    // public Double getSensor1() {
    // return sensor1;
    // }
    //
    // public void setSensor1(Double sensor1) {
    // this.sensor1 = sensor1;
    // }

    // public AlrmSnsr getAlrmSnsr() {
    // return alrmSnsr;
    // }
    //
    // public void setAlrmSnsr(AlrmSnsr alrmSnsr) {
    // this.alrmSnsr = alrmSnsr;
    // }

    // public class AlrmSnsr {
    //
    // @SerializedName("0")
    // @Expose
    // private org.openhab.binding.touchwand.internal.data._0 _0;
    // @SerializedName("5")
    // @Expose
    // private _5_ _5;
    //
    // public org.openhab.binding.touchwand.internal.data._0 get0() {
    // return _0;
    // }
    //
    // public void set0(org.openhab.binding.touchwand.internal.data._0 _0) {
    // this._0 = _0;
    // }
    //
    // public _5_ get5() {
    // return _5;
    // }
    //
    // public void set5(_5_ _5) {
    // this._5 = _5;
    // }
    //
    // }

}
