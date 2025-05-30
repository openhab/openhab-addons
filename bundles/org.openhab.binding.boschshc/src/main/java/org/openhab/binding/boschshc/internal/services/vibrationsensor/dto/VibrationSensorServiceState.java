/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.vibrationsensor.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * State for the vibration sensor service.
 * 
 * @author David Pace - Initial contribution
 *
 */
public class VibrationSensorServiceState extends BoschSHCServiceState {

    public VibrationSensorServiceState() {
        super("vibrationSensorState");
    }

    public boolean enabled;

    public VibrationSensorState value;

    public VibrationSensorSensitivity sensitivity;

    public VibrationSensorServiceState copy() {
        VibrationSensorServiceState copy = new VibrationSensorServiceState();
        copy.enabled = enabled;
        copy.value = value;
        copy.sensitivity = sensitivity;
        return copy;
    }
}
