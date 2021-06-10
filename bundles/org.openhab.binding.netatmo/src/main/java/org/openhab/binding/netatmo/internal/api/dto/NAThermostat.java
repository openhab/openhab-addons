/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAThermostat extends NAModule {
    private @Nullable NAThermMeasure measured;
    private boolean anticipating;
    private boolean boilerStatus;
    private boolean boilerValveComfortBoost;

    public @Nullable NAThermMeasure getMeasured() {
        return measured;
    }

    public boolean isAnticipating() {
        return anticipating;
    }

    public boolean getBoilerStatus() {
        return boilerStatus;
    }

    public boolean getBoilerValveComfortBoost() {
        return boilerValveComfortBoost;
    }
}
