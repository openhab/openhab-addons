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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.roborock.internal.api.enums.StatusType;
import org.openhab.binding.roborock.internal.map.RRMapData;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests the dock-position fallback of {@code status#current-room}: while the robot sits on its
 * charging dock, the room is resolved from the dock's position in the map rather than from the
 * robot position the map carries, which can still be the one it had while cleaning.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault({})
class RoborockVacuumHandlerDockRoomTest {

    /** Coordinate-to-pixel divisor of the RR map format, mirroring {@code RoomAtRobotResolver}. */
    private static final int MM = 50;

    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;

    private static final int HALLWAY_SEGMENT = 4;
    private static final int DOCK_ROOM_SEGMENT = 7;

    private static final Map<Integer, String> ROOM_NAMES = Map.of(HALLWAY_SEGMENT, "Flur", DOCK_ROOM_SEGMENT,
            "Abstellraum");

    private static final int ROBOT_PIXEL_X = 3;
    private static final int ROBOT_PIXEL_Y = 3;
    private static final int DOCK_PIXEL_X = 15;
    private static final int DOCK_PIXEL_Y = 15;

    @Test
    void docksResolveToTheDockRoomEvenWhenTheCachedMapStillPlacesTheRobotElsewhere() {
        RRMapData mapData = twoRoomMap(true, true);

        State dockRoomState = RoborockVacuumHandler.resolveDockRoomState(mapData, ROOM_NAMES);

        assertEquals(new StringType("Abstellraum"), dockRoomState);
    }

    @Test
    void mapCycleWhileDockedResolvesFromTheDockPosition() {
        RRMapData mapData = twoRoomMap(true, true);

        State roomState = RoborockVacuumHandler.resolveRoomStateFromMap(mapData, true, ROOM_NAMES);

        assertEquals(new StringType("Abstellraum"), roomState);
    }

    @Test
    void mapCycleWhileNotDockedResolvesFromTheRobotPosition() {
        RRMapData mapData = twoRoomMap(true, true);

        State roomState = RoborockVacuumHandler.resolveRoomStateFromMap(mapData, false, ROOM_NAMES);

        assertEquals(new StringType("Flur"), roomState);
    }

    @Test
    void dockingWithoutACachedMapReportsUndef() {
        assertEquals(UnDefType.UNDEF, RoborockVacuumHandler.resolveDockRoomState(null, ROOM_NAMES));
    }

    @Test
    void dockingWithoutAChargerPositionInTheCachedMapReportsUndef() {
        RRMapData mapData = twoRoomMap(true, false);

        assertEquals(UnDefType.UNDEF, RoborockVacuumHandler.resolveDockRoomState(mapData, ROOM_NAMES));
    }

    @Test
    void mapCycleWhileDockedWithoutAChargerPositionReportsUndefInsteadOfTheRobotPosition() {
        RRMapData mapData = twoRoomMap(true, false);

        State roomState = RoborockVacuumHandler.resolveRoomStateFromMap(mapData, true, ROOM_NAMES);

        assertEquals(UnDefType.UNDEF, roomState);
    }

    @Test
    void anUnresolvablePositionWhileCleaningLeavesTheChannelUntouched() {
        RRMapData mapData = unsegmentedFloorMap(true, false);

        assertNull(RoborockVacuumHandler.resolveRoomStateFromMap(mapData, false, ROOM_NAMES),
                "the next map cycle corrects the room on its own, so a transient resolver miss must not "
                        + "drag the channel to UNDEF");
    }

    @Test
    void anUnresolvableDockPositionReportsUndefBecauseNothingSelfCorrectsOnTheDock() {
        RRMapData mapData = unsegmentedFloorMap(false, true);

        assertEquals(UnDefType.UNDEF, RoborockVacuumHandler.resolveRoomStateFromMap(mapData, true, ROOM_NAMES));
        assertEquals(UnDefType.UNDEF, RoborockVacuumHandler.resolveDockRoomState(mapData, ROOM_NAMES));
    }

    @Test
    void mapCycleWithoutAnyUsablePositionReportsUndef() {
        RRMapData mapData = twoRoomMap(false, false);

        assertEquals(UnDefType.UNDEF, RoborockVacuumHandler.resolveRoomStateFromMap(mapData, false, ROOM_NAMES));
        assertEquals(UnDefType.UNDEF, RoborockVacuumHandler.resolveRoomStateFromMap(mapData, true, ROOM_NAMES));
    }

    @Test
    void dockingWithoutRoomMetadataReportsUndefInsteadOfTheStaleCleaningRoom() {
        RRMapData mapData = twoRoomMap(true, true);

        State dockRoomState = RoborockVacuumHandler.resolveDockRoomState(mapData, Map.of());

        assertEquals(UnDefType.UNDEF, dockRoomState);
    }

    @Test
    void mapCycleWhileDockedReportsUndefForAnUnnamedDockSegment() {
        RRMapData mapData = twoRoomMap(true, true);

        State roomState = RoborockVacuumHandler.resolveRoomStateFromMap(mapData, true, Map.of(HALLWAY_SEGMENT, "Flur"));

        assertEquals(UnDefType.UNDEF, roomState);
    }

    @Test
    void hasJustDockedFiresOnlyWhenAnUndockedStateTurnsIntoADockedOne() {
        assertTrue(RoborockVacuumHandler.hasJustDocked(StatusType.ROOM.getId(), StatusType.CHARGING.getId()),
                "the segment clean of the capture ended on the dock and must re-resolve the room");
        assertTrue(RoborockVacuumHandler.hasJustDocked(StatusType.DOCKING.getId(), StatusType.CHARGING.getId()),
                "arriving from the drive back to the dock is a docking transition too");
        assertTrue(RoborockVacuumHandler.hasJustDocked(null, StatusType.CHARGING.getId()),
                "the first state ever seen being a docked one counts as arriving on the dock");
    }

    @Test
    void hasJustDockedIgnoresChangesThatDoNotEndOnTheDock() {
        assertFalse(RoborockVacuumHandler.hasJustDocked(StatusType.CHARGING.getId(), StatusType.CHARGING.getId()),
                "staying docked must not republish the same room on every status poll");
        assertFalse(RoborockVacuumHandler.hasJustDocked(StatusType.CHARGING.getId(), StatusType.EMPTYING_BIN.getId()),
                "moving between two docked states is not an arrival");
        assertFalse(RoborockVacuumHandler.hasJustDocked(StatusType.CHARGING.getId(), StatusType.ROOM.getId()),
                "leaving the dock must not touch the room, the next map cycle owns it again");
        assertFalse(RoborockVacuumHandler.hasJustDocked(StatusType.IDLE.getId(), StatusType.DOCKING.getId()),
                "driving towards the dock is not being on it");
        assertFalse(RoborockVacuumHandler.hasJustDocked(StatusType.CLEANING.getId(), StatusType.PAUSED.getId()),
                "a pause in the middle of a room is not a docking transition");
    }

    @Test
    void statusTypeKnowsWhichStatesPutTheRobotOnItsDock() {
        assertTrue(StatusType.CHARGING.isAtDock());
        assertTrue(StatusType.CHARGING_ERROR.isAtDock());
        assertTrue(StatusType.FULL.isAtDock());
        assertTrue(StatusType.EMPTYING_BIN.isAtDock(), "the dock empties the bin, the robot has to be on it");
        assertTrue(StatusType.WASHING_MOP.isAtDock(), "the mop is washed in the dock");
        assertTrue(StatusType.WASHING_MOP2.isAtDock());

        assertFalse(StatusType.DOCKING.isAtDock(), "still driving towards the dock");
        assertFalse(StatusType.RETURNING.isAtDock(), "still driving towards the dock");
        assertFalse(StatusType.GOING_WASH_MOP.isAtDock(), "still driving towards the dock");
        assertFalse(StatusType.BACK_TO_DOCK_WASHING_DUSTER.isAtDock(), "still driving towards the dock");
        assertFalse(StatusType.CLEANING.isAtDock());
        assertFalse(StatusType.ROOM.isAtDock());
        assertFalse(StatusType.IDLE.isAtDock());
        assertFalse(StatusType.UNKNOWN.isAtDock(), "an unmapped state id must not be taken for a docked robot");
    }

    /** A map with two segmented rooms: the robot in the hallway segment, the charging dock in the other. */
    private static RRMapData twoRoomMap(boolean withRobotPosition, boolean withChargerPosition) {
        byte[] imageData = new byte[WIDTH * HEIGHT];
        fillSegment(imageData, 0, 0, 9, HEIGHT - 1, HALLWAY_SEGMENT);
        fillSegment(imageData, 10, 0, WIDTH - 1, HEIGHT - 1, DOCK_ROOM_SEGMENT);

        Integer robotX = withRobotPosition ? Integer.valueOf(toMapCoordinate(ROBOT_PIXEL_X)) : null;
        Integer robotY = withRobotPosition ? Integer.valueOf(toMapCoordinate(ROBOT_PIXEL_Y)) : null;
        Integer chargerX = withChargerPosition ? Integer.valueOf(toMapCoordinate(DOCK_PIXEL_X)) : null;
        Integer chargerY = withChargerPosition ? Integer.valueOf(toMapCoordinate(DOCK_PIXEL_Y)) : null;

        return new RRMapData(WIDTH, HEIGHT, 0, 0, imageData, robotX, robotY, null, chargerX, chargerY, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), new byte[WIDTH * HEIGHT]);
    }

    /** A map without any segmented-floor pixels, so no position on it resolves to a room. */
    private static RRMapData unsegmentedFloorMap(boolean withRobotPosition, boolean withChargerPosition) {
        byte[] imageData = new byte[WIDTH * HEIGHT];

        Integer robotX = withRobotPosition ? Integer.valueOf(toMapCoordinate(ROBOT_PIXEL_X)) : null;
        Integer robotY = withRobotPosition ? Integer.valueOf(toMapCoordinate(ROBOT_PIXEL_Y)) : null;
        Integer chargerX = withChargerPosition ? Integer.valueOf(toMapCoordinate(DOCK_PIXEL_X)) : null;
        Integer chargerY = withChargerPosition ? Integer.valueOf(toMapCoordinate(DOCK_PIXEL_Y)) : null;

        return new RRMapData(WIDTH, HEIGHT, 0, 0, imageData, robotX, robotY, null, chargerX, chargerY, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), new byte[WIDTH * HEIGHT]);
    }

    private static void fillSegment(byte[] imageData, int fromX, int fromY, int toX, int toY, int segmentId) {
        // Classifier 7 in the low 3 bits, segment id in the high 5 bits.
        byte pixel = (byte) ((segmentId << 3) | 0x07);
        for (int y = fromY; y <= toY; y++) {
            for (int x = fromX; x <= toX; x++) {
                imageData[y * WIDTH + x] = pixel;
            }
        }
    }

    /** Inverse of the resolver's pixel transform for a map with {@code top} and {@code left} 0. */
    private static int toMapCoordinate(int pixel) {
        return (pixel + 1) * MM;
    }
}
