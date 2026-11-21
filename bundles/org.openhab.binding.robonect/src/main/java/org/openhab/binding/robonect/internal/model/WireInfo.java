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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response to a boundary-wire sensor request.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class WireInfo extends RobonectAnswer {
    private @Nullable List<Sensor> sensors;

    /**
     * Returns the boundary-wire sensor readings.
     *
     * @return the boundary-wire sensor readings, or {@code null} if not reported
     */
    public @Nullable List<Sensor> getSensors() {
        return sensors;
    }
}
