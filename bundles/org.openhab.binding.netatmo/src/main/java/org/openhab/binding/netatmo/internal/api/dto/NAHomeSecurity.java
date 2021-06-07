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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAHomeSecurity extends NAHome {
    private NAObjectMap<NAPerson> persons = new NAObjectMap<NAPerson>();
    private NAObjectMap<NAWelcome> cameras = new NAObjectMap<NAWelcome>();
    private List<NAHomeEvent> events = List.of();

    public NAObjectMap<NAPerson> getPersons() {
        return persons;
    }

    public List<NAPerson> getKnownPersons() {
        return persons.values().stream().filter(person -> person.getName() != null).collect(Collectors.toList());
    }

    public List<NAHomeEvent> getEvents() {
        return events;
    }

    public NAObjectMap<NAWelcome> getCameras() {
        return cameras;
    }

    // TODO Remove unused code found by UCDetector
    // public Optional<NAPerson> getPerson(String id) {
    // return Optional.ofNullable(persons.get(id));
    // }

    public void setEvents(List<NAHomeEvent> events) {
        this.events = events;
    }
}
