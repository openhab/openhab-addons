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

import java.time.ZonedDateTime;
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
public class NAHomeEnergy extends NAHome {
    private List<NAThermProgram> schedules = List.of();
    private SetpointMode thermMode = SetpointMode.UNKNOWN;
    private @Nullable ZonedDateTime thermModeEndtime;
    private int thermSetpointDefaultDuration;

    public List<NAThermProgram> getThermSchedules() {
        return schedules;
    }

    public @Nullable NAThermProgram getActiveProgram() {
        return schedules.stream().filter(NAThermProgram::isSelected).findFirst().orElse(null);
    }

    public @Nullable ZonedDateTime getThermModeEndTime() {
        return thermModeEndtime;
    }

    public int getThermSetpointDefaultDuration() {
        return thermSetpointDefaultDuration;
    }

    public SetpointMode getThermMode() {
        return thermMode;
    }
}
