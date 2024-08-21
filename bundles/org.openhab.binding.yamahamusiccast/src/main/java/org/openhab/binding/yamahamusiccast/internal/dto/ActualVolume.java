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
package org.openhab.binding.yamahamusiccast.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the actual_volume object of the Status request requested from API or the UDP event from the
 * Yamaha model/device.
 *
 * @author Florian Hotze - Initial contribution
 */
public class ActualVolume {

    @SerializedName("mode")
    private String mode;

    @SerializedName("value")
    private float value;

    @SerializedName("unit")
    private String unit;

    public String getMode() {
        if (mode == null) {
            mode = "";
        }
        return mode;
    }

    /**
     * Volume in decibels (dB).
     *
     * @return volume in decibels (dB)
     */
    public float getValue() {
        return value;
    }

    public String getUnit() {
        if (unit == null) {
            unit = "";
        }
        return unit;
    }
}
