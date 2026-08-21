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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Verifies the status#current-room wiring by inspecting handler source text, matching the style
 * used by {@link RoborockVacuumHandlerConsumableWriterTest} for logic that would otherwise require
 * heavy Thing/ChannelTypeRegistry mocking to exercise as a live handler instance.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault({})
class RoborockVacuumHandlerCurrentRoomWiringTest {

    private static final Path HANDLER_PATH = Path
            .of("src/main/java/org/openhab/binding/roborock/internal/RoborockVacuumHandler.java");

    @Test
    void handleGetMapResolvesCurrentRoomStraightAfterParsingAndBeforeRendering() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String handleGetMapBody = extractMethodBody(source, "void handleGetMap(int requestId, byte[] mapPayload)");

        int parseIndex = handleGetMapBody.indexOf("rrMapParser.parse(mapPayload);");
        int updateCurrentRoomIndex = handleGetMapBody.indexOf("updateCurrentRoomState(mapData);");
        int renderIndex = handleGetMapBody.indexOf("rrMapRenderer.renderAsPng(mapData);");
        int dedupIfIndex = handleGetMapBody.indexOf("if (mapUpdateDeduplicator.shouldPublish(pngBytes))");

        assertTrue(parseIndex >= 0, "handleGetMap should still parse the map payload");
        assertTrue(updateCurrentRoomIndex >= 0, "handleGetMap should call updateCurrentRoomState");
        assertTrue(dedupIfIndex >= 0, "handleGetMap should still gate the PNG update on the deduplicator");

        assertTrue(updateCurrentRoomIndex > parseIndex && updateCurrentRoomIndex < renderIndex,
                "updateCurrentRoomState must run after the parse and before renderAsPng, so that rendering "
                        + "outcomes cannot leave a stale room reported");
    }

    @Test
    void handleGetMapInvalidatesMapDerivedStateWhenParsingFails() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String handleGetMapBody = extractMethodBody(source, "void handleGetMap(int requestId, byte[] mapPayload)");

        int parseFailureLogIndex = handleGetMapBody.indexOf("Failed to parse map payload");
        int invalidateIndex = handleGetMapBody.indexOf("invalidateMapDerivedState();");

        assertTrue(parseFailureLogIndex >= 0, "handleGetMap should still log a parse failure");
        assertTrue(invalidateIndex > parseFailureLogIndex,
                "a failed map parse must invalidate the map-derived channels instead of leaving the previous "
                        + "map image and room in place");
    }

    @Test
    void invalidateMapDerivedStateClearsBothMapAndCurrentRoom() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String body = extractMethodBody(source, "private void invalidateMapDerivedState()");

        assertTrue(body.contains("CHANNEL_VACUUM_MAP") && body.contains("RobotCapabilities.CURRENT_ROOM.getChannel()"),
                "invalidateMapDerivedState should clear the map image and status#current-room together, "
                        + "since both are derived from the same map fetch");
        assertTrue(body.contains("mapUpdateDeduplicator.reset()"),
                "invalidateMapDerivedState should keep resetting the deduplicator");
    }

    @Test
    void roomMetadataLossClearsTheSegmentNameTable() throws IOException {
        String source = Files.readString(HANDLER_PATH);

        String clearBody = extractMethodBody(source, "private void clearSegmentRoomNames()");
        assertTrue(
                clearBody.contains("segmentRoomNames = Map.of()")
                        && clearBody.contains("RobotCapabilities.CURRENT_ROOM.getChannel()"),
                "clearSegmentRoomNames should drop the table and clear status#current-room");

        assertTrue(
                extractMethodBody(source, "private void disableRoomMappingState(String reason)")
                        .contains("clearSegmentRoomNames();"),
                "disabling room mapping should drop the segment-name table, so later map responses cannot "
                        + "republish names from a mapping that is no longer refreshed");
        assertTrue(
                extractMethodBody(source, "private void handleGetRoomMapping(String response)")
                        .contains("clearSegmentRoomNames();"),
                "an empty room-mapping response should drop the segment-name table as well");
    }

    @Test
    void handleGetRoomMappingKeepsTheNotFoundSentinelOutOfTheSegmentNameTable() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String body = extractMethodBody(source, "private void handleGetRoomMapping(String response)");

        int guardIndex = body.indexOf("if (!ROOM_NAME_NOT_FOUND.equals(name))");
        int putIndex = body.indexOf("putResolvedSegmentName(");

        assertTrue(guardIndex >= 0 && putIndex > guardIndex,
                "only matched room names may enter segmentRoomNames: status#current-room must report UNDEF "
                        + "rather than the literal \"Not found\" sentinel of the published room mapping");
    }

    @Test
    void updateCurrentRoomStateUsesRoomAtRobotResolverAndSegmentRoomNames() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String updateCurrentRoomStateBody = extractMethodBody(source,
                "private void updateCurrentRoomState(RRMapData mapData)");
        String roomStateAtBody = extractMethodBody(source,
                "private static @Nullable State roomStateAt(RRMapData mapData, MapPoint position,");

        assertTrue(updateCurrentRoomStateBody.contains("segmentRoomNames"),
                "updateCurrentRoomState should resolve the room name via the segmentRoomNames table");
        assertTrue(roomStateAtBody.contains("RoomAtRobotResolver.resolveSegmentId"),
                "the room lookup should resolve the segment id via RoomAtRobotResolver");
        assertTrue(roomStateAtBody.contains("segmentRoomNames"),
                "the room lookup should translate the segment id through the segmentRoomNames table");
        assertTrue(roomStateAtBody.contains("UnDefType.UNDEF"),
                "the room lookup should fall back to UNDEF when no room name is resolved");
    }

    @Test
    void disableMapStateAlsoClearsCurrentRoomChannel() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String disableMapStateBody = extractMethodBody(source, "private void disableMapState(String reason)");

        assertTrue(disableMapStateBody.contains("invalidateMapDerivedState();"),
                "disableMapState should go through the central map-derived invalidation, so status#current-room "
                        + "is cleared alongside the map image whenever map refresh is disabled");
    }

    @Test
    void handleGetRoomMappingPopulatesSegmentRoomNamesWithoutReparsingPublishedJson() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String handleGetRoomMappingBody = extractMethodBody(source,
                "private void handleGetRoomMapping(String response)");

        assertTrue(handleGetRoomMappingBody.contains("installSegmentRoomNames("),
                "handleGetRoomMapping should install the segment room names it builds");
        assertFalse(handleGetRoomMappingBody.contains("JsonParser.parseString(mappedRoom"),
                "segmentRoomNames should be filled while building the JSON, not by re-parsing the published string");
    }

    @Test
    void handleGetMapCachesTheParsedMapForTheDockingTransition() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String handleGetMapBody = extractMethodBody(source, "void handleGetMap(int requestId, byte[] mapPayload)");

        int parseIndex = handleGetMapBody.indexOf("rrMapParser.parse(mapPayload);");
        int cacheIndex = handleGetMapBody.indexOf("lastParsedMapData = mapData;");

        assertTrue(cacheIndex > parseIndex,
                "a successfully parsed map must be retained, so that the room can be re-resolved from the "
                        + "dock position when the robot docks - no map is polled while it sits there");
    }

    @Test
    void invalidateMapDerivedStateAlsoDropsTheCachedMap() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String body = extractMethodBody(source, "private void invalidateMapDerivedState()");

        assertTrue(body.contains("lastParsedMapData = null;"),
                "map data that was just declared unusable must not survive to place a docking robot: "
                        + "after an invalidation status#current-room stays UNDEF until the next map cycle");
    }

    @Test
    void aStateIdChangeIntoADockedStateReResolvesTheRoomFromTheDock() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String body = extractMethodBody(source,
                "private void updateStateIdAndRequestStatusIfChanged(int stateId, boolean triggerImmediateStatusQuery)");

        assertTrue(body.contains("hasJustDocked(previousStateId, stateId)"),
                "the docking transition should be detected where the state id change is already known");
        assertTrue(body.contains("updateCurrentRoomStateFromDock();"),
                "a robot arriving on its dock should re-resolve status#current-room from the dock position");
    }

    @Test
    void mapCycleResolutionKnowsWhetherTheRobotIsDocked() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String body = extractMethodBody(source, "private void updateCurrentRoomState(RRMapData mapData)");

        assertTrue(body.contains("resolveRoomStateFromMap(mapData, isAtDock(), segmentRoomNames)"),
                "a map arriving while the robot is docked must be resolved from the dock position, otherwise "
                        + "it pushes the room of the interrupted cleaning run back out");
    }

    @Test
    void theDockRoomIsResolvedWithoutFetchingAnotherMap() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String body = extractMethodBody(source, "private void updateCurrentRoomStateFromDock()");

        assertFalse(
                body.contains("sendRPCCommand") || body.contains("COMMAND_GET_MAP")
                        || body.contains("requestMapRefreshIfDue"),
                "docking must reuse the map already parsed, not trigger an extra map fetch");
        assertTrue(body.contains("resolveDockRoomState(lastParsedMapData, segmentRoomNames)"),
                "the dock room comes from the retained map plus the segment-name table");
    }

    @Test
    void theRoomDecisionAndItsPublicationAreAtomicAgainstTheOtherThread() throws IOException {
        String source = Files.readString(HANDLER_PATH);

        String mapGuarded = extractGuardedBlock(
                extractMethodBody(source, "void handleGetMap(int requestId, byte[] mapPayload)"));
        assertTrue(
                mapGuarded.contains("lastParsedMapData = mapData;") && mapGuarded.contains("updateCurrentRoomState("),
                "retaining the map and resolving the room from it must happen under the lock: the docked check "
                        + "it reads is written by the status thread, and a lost race would leave the robot "
                        + "position on top of the dock room until the next undock");

        String stateIdGuarded = extractGuardedBlock(extractMethodBody(source,
                "private void updateStateIdAndRequestStatusIfChanged(int stateId, boolean triggerImmediateStatusQuery)"));
        assertTrue(
                stateIdGuarded.contains("lastKnownStateId = stateId;")
                        && stateIdGuarded.contains("updateCurrentRoomStateFromDock();"),
                "publishing the state id change and the dock room must happen under the same lock, so the map "
                        + "thread cannot decide 'not docked' and publish afterwards");

        String invalidateGuarded = extractGuardedBlock(
                extractMethodBody(source, "private void invalidateMapDerivedState()"));
        assertTrue(invalidateGuarded.contains("lastParsedMapData = null;"),
                "dropping the retained map and clearing the channel belong in the same guarded section");

        String clearGuarded = extractGuardedBlock(extractMethodBody(source, "private void clearSegmentRoomNames()"));
        assertTrue(clearGuarded.contains("segmentRoomNames = Map.of()"),
                "losing the room metadata must not interleave with a room publication either");

        String installGuarded = extractGuardedBlock(
                extractMethodBody(source, "void installSegmentRoomNames(Map<Integer, String> resolvedSegmentNames)"));
        assertTrue(
                installGuarded.contains("segmentRoomNames = Map.copyOf(")
                        && installGuarded.contains("updateCurrentRoomState("),
                "installing the names and re-evaluating the cached map must happen under the same lock, "
                        + "so a concurrent map response cannot interleave with the re-publication");
    }

    private static String extractGuardedBlock(String methodBody) {
        int guardIndex = methodBody.indexOf("synchronized (currentRoomLock)");
        assertTrue(guardIndex >= 0, "no synchronized (currentRoomLock) section found");

        int bodyStart = methodBody.indexOf('{', guardIndex);
        assertTrue(bodyStart >= 0, "guarded section has no body");
        return methodBody.substring(bodyStart + 1, findBlockEnd(methodBody, bodyStart));
    }

    private static String extractMethodBody(String source, String methodSignature) {
        int signatureStart = source.indexOf(methodSignature);
        assertTrue(signatureStart >= 0, "Method signature not found: " + methodSignature);

        int bodyStart = source.indexOf('{', signatureStart);
        assertTrue(bodyStart >= 0, "Method body start not found: " + methodSignature);

        int bodyEnd = findBlockEnd(source, bodyStart);
        return source.substring(bodyStart + 1, bodyEnd);
    }

    private static int findBlockEnd(String source, int openBraceIndex) {
        int depth = 1;
        int index = openBraceIndex + 1;
        while (index < source.length() && depth > 0) {
            char current = source.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
            }
            index++;
        }
        assertTrue(depth == 0, "Block end not found starting at index " + openBraceIndex);
        return index - 1;
    }
}
