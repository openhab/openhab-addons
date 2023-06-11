/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.dto.thermostat.summary;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RunningDTO} contains information indicating what equipment
 * is running.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public class RunningDTO {

    /*
     * The thermostat identifier.
     */
    public String identifier;

    /*
     * If no equipment is currently running no data is returned. Possible values
     * are: heatPump, heatPump2, heatPump3, compCool1, compCool2, auxHeat1,
     * auxHeat2, auxHeat3, fan, humidifier, dehumidifier, ventilator, economizer,
     * compHotWater, auxHotWater.
     */
    public final Set<String> runningEquipment;

    public RunningDTO() {
        identifier = "";
        runningEquipment = new HashSet<>();
    }

    public String getId() {
        return identifier;
    }

    public RunningDTO getThis() {
        return this;
    }

    public boolean hasChanged(@Nullable RunningDTO previous) {
        if (previous == null) {
            return true;
        }
        return !runningEquipment.equals(previous.runningEquipment);
    }

    public boolean isIdle() {
        return runningEquipment.isEmpty();
    }

    public boolean isHeating() {
        return runningEquipment.contains("heatPump") || runningEquipment.contains("heatPump2")
                || runningEquipment.contains("heatPump3") || runningEquipment.contains("auxHeat1")
                || runningEquipment.contains("auxHeat2") || runningEquipment.contains("auxHeat3");
    }

    public boolean isCooling() {
        return runningEquipment.contains("compCool1") || runningEquipment.contains("compCool2");
    }

    public boolean isRunning(final String equipment) {
        return runningEquipment.contains(equipment);
    }
}
