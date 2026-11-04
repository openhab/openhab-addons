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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response to a motor information request.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class MotorInfo extends RobonectAnswer {
    private @Nullable DriveMotors drive;
    private @Nullable BladeMotor blade;

    /**
     * Returns the drive motor measurements.
     *
     * @return the drive motor measurements, or {@code null} if absent
     */
    public @Nullable DriveMotors getDrive() {
        return drive;
    }

    /**
     * Returns the blade motor measurements.
     *
     * @return the blade motor measurements, or {@code null} if absent
     */
    public @Nullable BladeMotor getBlade() {
        return blade;
    }
}
