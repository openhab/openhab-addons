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
package org.openhab.binding.sonnen.internal.communication;

/**
 * The {@link SonnenJsonPowerMeterDataDTO} is the Java class used to map the JSON
 * response from the API to a PowerMeter Object.
 *
 * @author Christian Feininger - Initial contribution
 */
public class SonnenJsonPowerMeterDataDTO {

    // float a_l1;
    // float a_l2;
    // float a_l3;
    // String channel;
    // String deviceid;
    // String direction;
    // String error;
    float kwh_exported;
    float kwh_imported;
    // float v_l1_l2;
    // float v_l1_n;
    // float v_l2_l3;
    // float v_l2_n;
    // float v_l3_l1;
    // float v_l3_n;
    // float va_total;
    // float var_total;
    // float w_11;
    // float w_12;
    // float w_13;
    // float w_total;

    // /**
    // * @return the a_l2
    // */
    // public float getA_l2() {
    // return a_l2;
    // }
    //
    // /**
    // * @return the channel
    // */
    // public String getChannel() {
    // return channel;
    // }
    //
    // /**
    // * @return the deviceid
    // */
    // public String getDeviceid() {
    // return deviceid;
    // }
    //
    // /**
    // * @return the direction
    // */
    // public String getDirection() {
    // return direction;
    // }
    //
    // /**
    // * @return the error
    // */
    // public String getError() {
    // return error;
    // }

    /**
     * @return the kwh_exported
     */
    public float getKwh_exported() {
        return kwh_exported;
    }

    /**
     * @return the kwh_imported
     */
    public float getKwh_imported() {
        return kwh_imported;
    }

    // /**
    // * @return the v_l1_l2
    // */
    // public float getV_l1_l2() {
    // return v_l1_l2;
    // }
    //
    // /**
    // * @return the v_l1_n
    // */
    // public float getV_l1_n() {
    // return v_l1_n;
    // }
    //
    // /**
    // * @return the v_l2_l3
    // */
    // public float getV_l2_l3() {
    // return v_l2_l3;
    // }
    //
    // /**
    // * @return the v_l2_n
    // */
    // public float getV_l2_n() {
    // return v_l2_n;
    // }
    //
    // /**
    // * @return the v_l3_l1
    // */
    // public float getV_l3_l1() {
    // return v_l3_l1;
    // }
    //
    // /**
    // * @return the v_l3_n
    // */
    // public float getV_l3_n() {
    // return v_l3_n;
    // }
    //
    // /**
    // * @return the va_total
    // */
    // public float getVa_total() {
    // return va_total;
    // }
    //
    // /**
    // * @return the w_11
    // */
    // public float getW_11() {
    // return w_11;
    // }
    //
    // /**
    // * @return the w_12
    // */
    // public float getW_12() {
    // return w_12;
    // }
    //
    // /**
    // * @return the w_13
    // */
    // public float getW_13() {
    // return w_13;
    // }
    //
    // /**
    // * @return the w_total
    // */
    // public float getW_total() {
    // return w_total;
    // }
    //
    // /**
    // * @return the a_l3
    // */
    // public float getA_l3() {
    // return a_l3;
    // }
    //
    // /**
    // * @return the a_l1
    // */
    // public float getA_l1() {
    // return a_l1;
    // }
    //
    // /**
    // * @return the var_total
    // */
    // public float getVar_total() {
    // return var_total;
    // }
}
