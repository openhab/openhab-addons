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
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.ThermostatZoneType;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAThermProgram extends NAObject {
    private List<NAZone> zones = List.of();
    private List<NATimeTableItem> timetable = List.of();
    private boolean selected;

    public List<NATimeTableItem> getTimetable() {
        return timetable;
    }

    public boolean isSelected() {
        return selected;
    }

    // TODO Remove unused code found by UCDetector
    // public double getZoneTemperature(SetpointMode currentMode) {
    // try {
    // ThermostatZoneType equivalentZone = ThermostatZoneType.valueOf(currentMode.toString());
    // return getZoneTemperature(equivalentZone);
    // } catch (IllegalArgumentException ignore) { // not all thermostat modes have an equivalent zone
    // return Double.NaN;
    // }
    // }

    public double getZoneTemperature(ThermostatZoneType zone) {
        return zones.stream().filter(z -> z.getType() == zone).findFirst().map(NAZone::getTemp).orElse(Double.NaN);
    }

    public @Nullable NAZone getZone(String id) {
        return zones.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }
}
