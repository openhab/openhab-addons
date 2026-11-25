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
package org.openhab.binding.roborock.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.roborock.internal.api.HomeData.Rooms;
import org.openhab.core.types.StateOption;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 * Tests the room selection options built for {@code actions#segment} from a {@code get_room_mapping} result.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
class RoborockVacuumHandlerSegmentOptionsTest {

    private static final Rooms[] HOME_ROOMS = { room(1001, "Kitchen"), room(1002, "Hallway") };

    @Test
    void knownRoomsAreOfferedByNameAndResolvedForCurrentRoom() {
        Map<Integer, String> resolved = new HashMap<>();

        List<StateOption> options = RoborockVacuumHandler
                .buildSegmentOptions(rooms("[[16,\"1001\",14],[17,\"1002\",9]]"), HOME_ROOMS, resolved);

        assertEquals(List.of(new StateOption("16", "Kitchen"), new StateOption("17", "Hallway")), options);
        assertEquals(Map.of(16, "Kitchen", 17, "Hallway"), resolved);
    }

    @Test
    void unknownHomeRoomStaysSelectableButIsNotResolvedForCurrentRoom() {
        Map<Integer, String> resolved = new HashMap<>();

        List<StateOption> options = RoborockVacuumHandler.buildSegmentOptions(rooms("[[18,\"9999\"]]"), HOME_ROOMS,
                resolved);

        assertEquals(List.of(new StateOption("18", "Room 18")), options);
        assertTrue(resolved.isEmpty(), "status#current-room must report UNDEF for a room without a known name");
    }

    @Test
    void malformedEntriesAreSkipped() {
        Map<Integer, String> resolved = new HashMap<>();

        List<StateOption> options = RoborockVacuumHandler
                .buildSegmentOptions(rooms("[[\"abc\",\"1001\"],[19],{\"a\":1},[20,\"1002\"]]"), HOME_ROOMS, resolved);

        assertEquals(List.of(new StateOption("20", "Hallway")), options);
        assertEquals(Map.of(20, "Hallway"), resolved);
    }

    @Test
    void emptyResultYieldsNoOptions() {
        assertTrue(RoborockVacuumHandler.buildSegmentOptions(rooms("[]"), HOME_ROOMS, new HashMap<>()).isEmpty());
    }

    private static JsonArray rooms(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }

    private static Rooms room(int id, String name) {
        Rooms room = new Rooms();
        room.id = id;
        room.name = name;
        return room;
    }
}
