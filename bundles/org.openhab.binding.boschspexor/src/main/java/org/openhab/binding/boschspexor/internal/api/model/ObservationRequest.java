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
import org.openhab.binding.boschspexor.internal.api.model.ObservationStatus.SensorMode;

/**
 * Request representation to change an observation
 *
 * @author Marc Fischer - Initial contribution
 */
@NonNullByDefault
public class ObservationRequest {

    private final String observationType;

    private final SensorMode sensorMode;

    public ObservationRequest(String observationType, SensorMode sensorMode) {
        this.observationType = observationType;
        this.sensorMode = sensorMode;
    }

    public String getObservationType() {
        return observationType;
    }

    public SensorMode getSensorMode() {
        return sensorMode;
    }
}
