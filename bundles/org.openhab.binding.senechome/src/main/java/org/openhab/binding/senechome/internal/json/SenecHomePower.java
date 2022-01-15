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
package org.openhab.binding.senechome.internal.json;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gson.annotations.SerializedName;

/**
 * Json model of senec home devices: This sub model provides the current power statistics by the inverter.
 *
 * Section "PV1" in Senec JSON.
 *
 * @author Steven Schwarznau - Initial Contribution
 */
public class SenecHomePower implements Serializable {

    private static final long serialVersionUID = -7092741166288342343L;

    /**
     * Power limitation (%).
     */
    public @SerializedName("POWER_RATIO") String powerLimitation;

    /**
     * Current DC current per MPP (A).
     */
    public @SerializedName("MPP_CUR") String[] currentPerMpp;

    /**
     * Current DC power per MPP (W)
     */
    public @SerializedName("MPP_POWER") String[] powerPerMpp;

    /**
     * Current DC tension per MPP (V).
     */
    public @SerializedName("MPP_VOL") String[] voltagePerMpp;

    @Override
    public String toString() {
        return "SenecHomePower [powerLimitation=" + powerLimitation + ", mppCur=" + Arrays.toString(currentPerMpp)
                + ", mppPower=" + Arrays.toString(powerPerMpp) + ", mppVol=" + Arrays.toString(voltagePerMpp) + "]";
    }
}
