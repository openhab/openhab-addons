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
    void handleGetMapResolvesCurrentRoomUnconditionallyOutsideTheDeduplicatorBranch() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String handleGetMapBody = extractMethodBody(source,
                "private void handleGetMap(int requestId, byte[] mapPayload)");

        int dedupIfIndex = handleGetMapBody.indexOf("if (mapUpdateDeduplicator.shouldPublish(pngBytes))");
        int updateCurrentRoomIndex = handleGetMapBody.indexOf("updateCurrentRoomState(mapData);");

        assertTrue(dedupIfIndex >= 0, "handleGetMap should still gate the PNG update on the deduplicator");
        assertTrue(updateCurrentRoomIndex >= 0, "handleGetMap should call updateCurrentRoomState after parsing");

        // The dedup if/else block ends at its closing brace; updateCurrentRoomState must be called
        // after that, not nested inside the "shouldPublish" branch.
        int dedupBlockEnd = findBlockEnd(handleGetMapBody, handleGetMapBody.indexOf('{', dedupIfIndex));
        assertTrue(updateCurrentRoomIndex > dedupBlockEnd,
                "updateCurrentRoomState must run unconditionally, after the PNG deduplicator branch, not inside it");
    }

    @Test
    void updateCurrentRoomStateUsesRoomAtRobotResolverAndSegmentRoomNames() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String updateCurrentRoomStateBody = extractMethodBody(source,
                "private void updateCurrentRoomState(RRMapData mapData)");

        assertTrue(updateCurrentRoomStateBody.contains("RoomAtRobotResolver.resolveSegmentId"),
                "updateCurrentRoomState should resolve the segment id via RoomAtRobotResolver");
        assertTrue(updateCurrentRoomStateBody.contains("segmentRoomNames"),
                "updateCurrentRoomState should resolve the room name via the segmentRoomNames table");
        assertTrue(updateCurrentRoomStateBody.contains("UnDefType.UNDEF"),
                "updateCurrentRoomState should fall back to UNDEF when no room name is resolved");
    }

    @Test
    void disableMapStateAlsoClearsCurrentRoomChannel() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String disableMapStateBody = extractMethodBody(source, "private void disableMapState(String reason)");

        assertTrue(
                disableMapStateBody.contains("RobotCapabilities.CURRENT_ROOM.getChannel()")
                        && disableMapStateBody.contains("UnDefType.UNDEF"),
                "disableMapState should clear status#current-room to UNDEF alongside the map image, "
                        + "since current-room is derived from the same map fetch");
    }

    @Test
    void handleGetRoomMappingPopulatesSegmentRoomNamesWithoutReparsingPublishedJson() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String handleGetRoomMappingBody = extractMethodBody(source,
                "private void handleGetRoomMapping(String response)");

        assertTrue(handleGetRoomMappingBody.contains("segmentRoomNames = "),
                "handleGetRoomMapping should populate segmentRoomNames while building the room-mapping JSON");
        assertFalse(handleGetRoomMappingBody.contains("JsonParser.parseString(mappedRoom"),
                "segmentRoomNames should be filled while building the JSON, not by re-parsing the published string");
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
