/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;

/**
 * The {@link NAHome} holds home information.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAHome extends NADevice implements NetatmoLocation {
    // Common part
    private double[] coordinates = {};
    private double altitude;
    private NAObjectMap<NARoom> rooms = new NAObjectMap<>();

    // Energy specific part
    // private SetpointMode thermMode = SetpointMode.UNKNOWN;
    private @Nullable ZonedDateTime thermModeEndtime;
    // private int thermSetpointDefaultDuration;

    // Security specific part
    public NAObjectMap<NAPerson> persons = new NAObjectMap<>();
    // public NAObjectMap<NAWelcome> cameras = new NAObjectMap<>();
    private List<NAHomeEvent> events = List.of();

    @Override
    public ModuleType getType() {
        return ModuleType.NAHome;
    }

    @Override
    public double getAltitude() {
        return altitude;
    }

    @Override
    public double[] getCoordinates() {
        return coordinates;
    }

    public NAObjectMap<NARoom> getRooms() {
        return rooms;
    }

    public @Nullable ZonedDateTime getThermModeEndTime() {
        return thermModeEndtime;
    }

    // public int getThermSetpointDefaultDuration() {
    // return thermSetpointDefaultDuration;
    // }

    // public SetpointMode getThermMode() {
    // return thermMode;
    // }

    public NAObjectMap<NAPerson> getPersons() {
        return persons;
    }

    public List<NAPerson> getKnownPersons() {
        return persons.values().stream().filter(person -> person.getName() != null).collect(Collectors.toList());
    }

    public List<NAHomeEvent> getEvents() {
        return events;
    }

    // public NAObjectMap<NAWelcome> getCameras() {
    // return cameras;
    // }

    public void setEvents(List<NAHomeEvent> events) {
        this.events = events;
    }
}
