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
package org.openhab.binding.melcloud.internal.home.api.dto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Root response of {@code GET /context}: every building the user owns, plus every building shared
 * with them as a guest.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeUserContext {

    public List<MelCloudHomeBuilding> buildings = List.of();
    public List<MelCloudHomeBuilding> guestBuildings = List.of();

    private Stream<MelCloudHomeBuilding> allBuildings() {
        return Stream.concat(buildings.stream(), guestBuildings.stream());
    }

    public Optional<MelCloudHomeAtaUnit> findAtaUnit(String unitId) {
        return allBuildings().flatMap(building -> building.airToAirUnits.stream())
                .filter(unit -> unitId.equals(unit.id)).findFirst();
    }

    public Optional<MelCloudHomeAtwUnit> findAtwUnit(String unitId) {
        return allBuildings().flatMap(building -> building.airToWaterUnits.stream())
                .filter(unit -> unitId.equals(unit.id)).findFirst();
    }

    public List<MelCloudHomeAtaUnit> getAllAtaUnits() {
        return allBuildings().flatMap(building -> building.airToAirUnits.stream()).toList();
    }

    public List<MelCloudHomeAtwUnit> getAllAtwUnits() {
        return allBuildings().flatMap(building -> building.airToWaterUnits.stream()).toList();
    }
}
