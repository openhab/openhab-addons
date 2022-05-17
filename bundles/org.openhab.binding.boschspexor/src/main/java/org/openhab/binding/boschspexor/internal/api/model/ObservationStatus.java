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
package org.openhab.binding.boschspexor.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of Observation Status
 *
 * @author Marc Fischer - Initial contribution
 */
@NonNullByDefault
public class ObservationStatus {

    public static final String TYPE_BURGLARY = "Burglary";

    /**
     * Sensor modes
     *
     */
    public enum SensorMode {
        @JsonProperty("Deactivated")
        DEACTIVATED,
        @JsonProperty("InActivation")
        IN_ACTIVATION,
        @JsonProperty("InCalibration")
        IN_CALIBRATION,
        @JsonProperty("Activated")
        ACTIVATED,
        @JsonProperty("Triggered")
        TRIGGERED,
        @JsonProperty("InDeactivation")
        IN_DEACTIVATION
    }

    private String observationType = TYPE_BURGLARY;
    private SensorMode sensorMode = SensorMode.DEACTIVATED;

    public String getObservationType() {
        return observationType;
    }

    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }

    public SensorMode getSensorMode() {
        return sensorMode;
    }

    public void setSensorMode(SensorMode sensorMode) {
        this.sensorMode = sensorMode;
    }
}
