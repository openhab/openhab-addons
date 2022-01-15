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

import com.google.gson.annotations.SerializedName;

/**
 * Senec Temperature information from "TEMPMEASURE" section.
 *
 * @author Erwin Guib - Initial Contribution
 */
public class SenecHomeTemperature implements Serializable {

    private static final long serialVersionUID = 5300207918289980752L;

    /**
     * Battery temperature (°C).
     */
    public @SerializedName("BATTERY_TEMP") String batteryTemperature;

    /**
     * Case temperature (°C).
     */
    public @SerializedName("CASE_TEMP") String caseTemperature;

    /**
     * MCU Temperature (°C).
     */
    public @SerializedName("MCU_TEMP") String mcuTemperature;

    @Override
    public String toString() {
        return "SenecHomeTemperature{" + "batteryTemperature='" + batteryTemperature + '\'' + ", caseTemperature='"
                + caseTemperature + '\'' + ", mcuTemperature='" + mcuTemperature + '\'' + '}';
    }
}
