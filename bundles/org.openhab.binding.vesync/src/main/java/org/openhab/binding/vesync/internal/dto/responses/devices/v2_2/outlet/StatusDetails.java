/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal.dto.responses.devices.v2_2.outlet;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link StatusDetails} class is used as a DTO to hold the Vesync's API's common response
 * data, with regard's to an outlet device's status.
 *
 * @author Marcel Goerentz - Initial contribution
 */
public class StatusDetails {

    @SerializedName("enabled")
    public boolean enabled = false;

    @SerializedName("voltage")
    public double voltage = 0.00;

    @SerializedName("energy")
    public double energy = 0.00;

    @SerializedName("power")
    public double power = 0.00;

    @SerializedName("current")
    public double current = 0.00;

    @SerializedName("highestVoltage")
    public int highestVoltage = 0;

    @SerializedName("voltagePTStatus")
    public boolean voltagePTStatus = false;

    public String getDeviceStatus() {
        return enabled ? "on" : "off";
    }
}
