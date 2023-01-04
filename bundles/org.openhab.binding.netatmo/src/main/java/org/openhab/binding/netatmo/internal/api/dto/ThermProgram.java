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
package org.openhab.binding.netatmo.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;

/**
 * The {@link ThermProgram} holds setpoint scheduling information.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class ThermProgram extends NAObject {
    private NAObjectMap<Zone> zones = new NAObjectMap<>();
    private List<TimeTableItem> timetable = List.of();
    private boolean selected;

    public List<TimeTableItem> getTimetable() {
        return timetable;
    }

    public boolean isSelected() {
        return selected;
    }

    public @Nullable Zone getZone(String id) {
        return zones.get(id);
    }
}
