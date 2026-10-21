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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MelCloudHomeUserContext}, in particular that units are found across both
 * owned and guest buildings.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeUserContextTest {

    private static MelCloudHomeAtaUnit ataUnit(String id) {
        MelCloudHomeAtaUnit unit = new MelCloudHomeAtaUnit();
        unit.id = id;
        return unit;
    }

    private static MelCloudHomeAtwUnit atwUnit(String id) {
        MelCloudHomeAtwUnit unit = new MelCloudHomeAtwUnit();
        unit.id = id;
        return unit;
    }

    @Test
    void whenAtaUnitIsInAnOwnedBuildingThenFindAtaUnitFindsIt() {
        // Arrange
        MelCloudHomeBuilding building = new MelCloudHomeBuilding();
        building.airToAirUnits = List.of(ataUnit("ata-1"));
        MelCloudHomeUserContext context = new MelCloudHomeUserContext();
        context.buildings = List.of(building);

        // Act
        Optional<MelCloudHomeAtaUnit> found = context.findAtaUnit("ata-1");

        // Assert
        assertEquals("ata-1", found.orElseThrow().id);
    }

    @Test
    void whenAtwUnitIsInAGuestBuildingThenFindAtwUnitFindsIt() {
        // Arrange
        MelCloudHomeBuilding guestBuilding = new MelCloudHomeBuilding();
        guestBuilding.airToWaterUnits = List.of(atwUnit("atw-1"));
        MelCloudHomeUserContext context = new MelCloudHomeUserContext();
        context.guestBuildings = List.of(guestBuilding);

        // Act
        Optional<MelCloudHomeAtwUnit> found = context.findAtwUnit("atw-1");

        // Assert
        assertEquals("atw-1", found.orElseThrow().id);
    }

    @Test
    void whenUnitIdIsUnknownThenFindAtaUnitReturnsEmpty() {
        // Arrange
        MelCloudHomeUserContext context = new MelCloudHomeUserContext();

        // Act
        Optional<MelCloudHomeAtaUnit> found = context.findAtaUnit("does-not-exist");

        // Assert
        assertEquals(Optional.empty(), found);
    }

    @Test
    void whenMultipleBuildingsHaveUnitsThenGetAllAtaUnitsReturnsThemAll() {
        // Arrange
        MelCloudHomeBuilding owned = new MelCloudHomeBuilding();
        owned.airToAirUnits = List.of(ataUnit("ata-1"));
        MelCloudHomeBuilding guest = new MelCloudHomeBuilding();
        guest.airToAirUnits = List.of(ataUnit("ata-2"));
        MelCloudHomeUserContext context = new MelCloudHomeUserContext();
        context.buildings = List.of(owned);
        context.guestBuildings = List.of(guest);

        // Act
        List<MelCloudHomeAtaUnit> allUnits = context.getAllAtaUnits();

        // Assert
        assertEquals(2, allUnits.size());
        assertTrue(allUnits.stream().anyMatch(unit -> "ata-1".equals(unit.id)));
        assertTrue(allUnits.stream().anyMatch(unit -> "ata-2".equals(unit.id)));
    }
}
