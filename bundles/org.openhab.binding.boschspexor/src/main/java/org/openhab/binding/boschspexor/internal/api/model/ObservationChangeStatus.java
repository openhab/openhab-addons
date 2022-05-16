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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschspexor.internal.api.model.ObservationStatus.SensorMode;

/**
 * Representation of Observation Change Status
 *
 * @author Marc Fischer - Initial contribution *
 */
@NonNullByDefault
public class ObservationChangeStatus {
    /**
     * StatusCode
     *
     * @author Marc Fischer - Initial contribution
     *
     */
    public enum StatusCode {
        SUCCESS,
        FAILURE
    }

    private String observationType = ObservationStatus.TYPE_BURGLARY;
    private SensorMode sensorMode = SensorMode.Deactivated;
    private StatusCode statusCode = StatusCode.FAILURE;
    @Nullable
    private String message;

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

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public @Nullable String getMessage() {
        return message;
    }

    public void setMessage(@Nullable String message) {
        this.message = message;
    }
}
