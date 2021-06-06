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
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NASetpoint {
    private double thermSetpointTemperature;
    private long setpointEndtime;
    private @Nullable SetpointMode setpointMode;

    public double getSetpointTemperature() {
        return thermSetpointTemperature;
    }

    // TODO : dégager les setters ?
    public void setSetpointTemperature(double thermSetpointTemperature) {
        this.thermSetpointTemperature = thermSetpointTemperature;
    }

    public long getSetpointEndtime() {
        return setpointEndtime;
    }

    public void setSetpointEndtime(long setpointEndtime) {
        this.setpointEndtime = setpointEndtime;
    }

    public SetpointMode getMode() {
        SetpointMode mode = setpointMode;
        return mode != null ? mode : SetpointMode.UNKNOWN;
    }

    public void setSetpointMode(SetpointMode mode) {
        this.setpointMode = mode;
    }
}
