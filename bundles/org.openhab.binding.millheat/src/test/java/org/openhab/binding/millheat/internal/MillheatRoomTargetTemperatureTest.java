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
package org.openhab.binding.millheat.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.binding.millheat.internal.dto.HouseDTO;
import org.openhab.binding.millheat.internal.dto.RoomInfoDTO;
import org.openhab.binding.millheat.internal.dto.VacationModeRequest;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.Room;

/**
 * Covers which setpoint a room reports for the mode it is in.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public class MillheatRoomTargetTemperatureTest {

    private static Room roomInVacationHouse(final String vacationModeType) {
        final HouseDTO house = new HouseDTO("house-1", "Huset", "Norway", "1234", "Europe/Oslo", "owner-1", "vacation",
                Boolean.TRUE, 1L, 2L, 10.0, vacationModeType, null, 0L);
        final RoomInfoDTO room = new RoomInfoDTO("room-1", "Living room", "house-1", 22.0, 18.0, 16.0, "vacation", null,
                null, 0L, null, 20.0, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, 0.0);
        return new Room(room, new Home(house));
    }

    @Test
    public void vacationUsesTheHouseTemperatureByDefault() {
        final Room room = roomInVacationHouse(VacationModeRequest.TYPE_VACATION_TEMPERATURE);
        assertEquals(10.0, room.getTargetTemperature());
    }

    @Test
    public void advancedVacationUsesTheRoomAwayTemperature() {
        // Advanced vacation mode tells the service to fall back to each room's away temperature,
        // so the house-wide vacation temperature is not what the devices follow.
        final Room room = roomInVacationHouse(VacationModeRequest.TYPE_AWAY_TEMPERATURE);
        assertEquals(16.0, room.getTargetTemperature());
    }
}
