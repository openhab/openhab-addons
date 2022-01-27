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
package org.openhab.binding.millheat.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MillheatModel} represents the home structure as designed by the user in the Millheat app.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillheatModel {
    private final long lastUpdated;
    private final List<Home> homes = new ArrayList<>();

    public MillheatModel(final long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void addHome(final Home home) {
        homes.add(home);
    }

    public List<Home> getHomes() {
        return homes;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public Optional<Heater> findHeaterById(final Long id) {
        return findHeaters().filter(heater -> id.equals(heater.getId())).findFirst();
    }

    public Optional<Heater> findHeaterByMac(final String macAddress) {
        return findHeaters().filter(heater -> macAddress.equals(heater.getMacAddress())).findFirst();
    }

    public Optional<Heater> findHeaterByMacOrId(@Nullable final String macAddress, @Nullable final Long id) {
        Optional<Heater> heater = Optional.empty();

        if (macAddress != null) {
            heater = findHeaterByMac(macAddress);
        }
        if (!heater.isPresent() && id != null) {
            heater = findHeaterById(id);
        }
        return heater;
    }

    private Stream<Heater> findHeaters() {
        return Stream.concat(
                homes.stream().flatMap(home -> home.getRooms().stream()).flatMap(room -> room.getHeaters().stream()),
                homes.stream().flatMap(room -> room.getIndependentHeaters().stream()));
    }

    public Optional<Room> findRoomById(final Long id) {
        return homes.stream().flatMap(home -> home.getRooms().stream()).filter(room -> id.equals(room.getId()))
                .findFirst();
    }

    public Optional<Home> findHomeByRoomId(final Long id) {
        for (final Home home : homes) {
            for (final Room room : home.getRooms()) {
                if (id.equals(room.getId())) {
                    return Optional.of(home);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Home> findHomeById(Long homeId) {
        return homes.stream().filter(e -> e.getId().equals(homeId)).findFirst();
    }
}
