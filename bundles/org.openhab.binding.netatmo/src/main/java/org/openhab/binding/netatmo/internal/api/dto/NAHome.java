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
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAHome extends NADevice {
    private @Nullable NAObjectMap<NAPerson> persons;
    private List<NAHomeEvent> events = List.of();
    private List<NAThermProgram> thermSchedules = List.of();
    private List<NAWelcome> cameras = List.of();
    private int thermSetpointDefaultDuration;
    @SerializedName("coordinates")
    private double[] location = {};
    private double altitude;

    public List<NAThermProgram> getThermSchedules() {
        return thermSchedules;
    }

    public int getThermSetpointDefaultDuration() {
        return thermSetpointDefaultDuration;
    }

    public @Nullable PointType getLocation() {
        if (location.length == 2) {
            return new PointType(new DecimalType(location[1]), new DecimalType(location[0]), new DecimalType(altitude));
        }
        return null;
    }

    public @Nullable NAObjectMap<NAPerson> getPersons() {
        return persons;
    }

    public @Nullable List<NAPerson> getKnownPersons() {
        NAObjectMap<NAPerson> personList = persons;
        if (personList != null) {
            return personList.values().stream().filter(person -> person.getName() != null).collect(Collectors.toList());
        }
        return null;
    }

    public List<NAHomeEvent> getEvents() {
        return events;
    }

    public List<NAWelcome> getCameras() {
        return cameras;
    }

    public Optional<NAPerson> getPerson(String id) {
        NAObjectMap<NAPerson> personList = persons;
        return personList == null ? Optional.empty() : Optional.ofNullable(personList.get(id));
    }

    public void setEvents(List<NAHomeEvent> events) {
        this.events = events;
    }
}
