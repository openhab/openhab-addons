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
package org.openhab.binding.fronius.internal.api.dto.ohmpilot;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OhmpilotRealtimeDetails} is responsible for storing
 * the "Details" node of the {@link OhmpilotRealtimeBodyData}.
 *
 * @author Hannes Spenger - Initial contribution
 */
public class OhmpilotRealtimeDetails {
    @SerializedName("Hardware")
    private String hardware;
    @SerializedName("Manufacturer")
    private String manufacturer;
    @SerializedName("Model")
    private String model;
    @SerializedName("Serial")
    private String serial;
    @SerializedName("Software")
    private String software;

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }
}
