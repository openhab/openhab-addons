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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAThermostat extends NAModule {
    private @Nullable NAThermMeasure measured;
    private @Nullable NASetpoint setpoint;
    private int thermOrientation;
    private int thermRelayCmd;
    private boolean anticipating;

    private List<NAThermProgram> thermProgramList = List.of();

    public @Nullable NAThermMeasure getMeasured() {
        return measured;
    }

    public int getThermOrientation() {
        return thermOrientation;
    }

    public List<NAThermProgram> getThermProgramList() {
        return thermProgramList;
    }

    public @Nullable NAThermProgram getActiveProgram() {
        return thermProgramList.stream().filter(NAThermProgram::isSelected).findFirst().orElse(null);
    }

    public boolean getThermRelayCmd() {
        return thermRelayCmd != 0;
    }

    public double getSetpointTemp() {
        return setpoint != null ? setpoint.getSetpointTemp() : Double.NaN;
    }

    public long getSetpointEndtime() {
        return setpoint != null ? setpoint.getSetpointEndtime() : 0;
    }

    public SetpointMode getSetpointMode() {
        return setpoint != null ? setpoint.getMode() : SetpointMode.UNKNOWN;
    }

    public boolean isAnticipating() {
        return anticipating;
    }
}
