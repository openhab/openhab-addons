/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MillheatModel} represents the home structure as designed by the user in the Mill app.
 * It is an immutable snapshot rebuilt on every poll; thing handlers read from it rather than
 * calling the API themselves.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Identifiers are cloud API UUIDs rather than numbers
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

    public Optional<Heater> findHeaterById(final String id) {
        return findHeaters().filter(heater -> id.equals(heater.getId())).findFirst();
    }

    public Optional<Heater> findHeaterByMac(final String macAddress) {
        final String wanted = normalizeMac(macAddress);
        return findHeaters().filter(heater -> wanted.equals(normalizeMac(heater.getMacAddress()))).findFirst();
    }

    /**
     * The API reports MAC addresses colon separated and users have configurations in both shapes,
     * so comparison is on the hex digits alone.
     */
    private static String normalizeMac(final @Nullable String macAddress) {
        return macAddress == null ? "" : macAddress.replaceAll("[^0-9A-Fa-f]", "").toUpperCase(Locale.ROOT);
    }

    public Optional<Heater> findHeaterByMacOrId(final @Nullable String macAddress, final @Nullable String id) {
        Optional<Heater> heater = Optional.empty();
        if (macAddress != null && !macAddress.isBlank()) {
            heater = findHeaterByMac(macAddress);
        }
        if (heater.isEmpty() && id != null && !id.isBlank()) {
            heater = findHeaterById(id);
        }
        return heater;
    }

    private Stream<Heater> findHeaters() {
        return Stream.concat(
                homes.stream().flatMap(home -> home.getRooms().stream()).flatMap(room -> room.getHeaters().stream()),
                homes.stream().flatMap(home -> home.getIndependentHeaters().stream()));
    }

    public Optional<Room> findRoomById(final String id) {
        return homes.stream().flatMap(home -> home.getRooms().stream()).filter(room -> id.equals(room.getId()))
                .findFirst();
    }

    public Optional<Home> findHomeByRoomId(final String id) {
        return homes.stream().filter(home -> home.getRooms().stream().anyMatch(room -> id.equals(room.getId())))
                .findFirst();
    }

    public Optional<Home> findHomeById(final String homeId) {
        return homes.stream().filter(home -> homeId.equals(home.getId())).findFirst();
    }
}
