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

@NonNullByDefault({})
class RoborockVacuumHandlerConsumableWriterTest {

    private static final Path HANDLER_PATH = Path
            .of("src/main/java/org/openhab/binding/roborock/internal/RoborockVacuumHandler.java");

    @Test
    void updateDeviceDoesNotWriteConsumablePercentChannelsFromRawWorkTime() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String updateDeviceBody = extractMethodBody(source, "private void updateDevice(Devices devices\\[])");

        assertFalse(updateDeviceBody.contains("CHANNEL_CONSUMABLE_MAIN_PERC"),
                "updateDevice must not write main consumable percent channel");
        assertFalse(updateDeviceBody.contains("CHANNEL_CONSUMABLE_SIDE_PERC"),
                "updateDevice must not write side consumable percent channel");
        assertFalse(updateDeviceBody.contains("CHANNEL_CONSUMABLE_FILTER_PERC"),
                "updateDevice must not write filter consumable percent channel");
    }

    @Test
    void updateDeviceDoesNotWriteBatteryChannelFromSnapshot() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String updateDeviceBody = extractMethodBody(source, "private void updateDevice(Devices devices\\[])");

        assertFalse(updateDeviceBody.contains("CHANNEL_BATTERY"),
                "updateDevice must not write battery channel; live status/DPS handlers are authoritative");
    }

    @Test
    void updateDeviceDoesNotWriteStateOrFanPowerFromSnapshot() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String updateDeviceBody = extractMethodBody(source, "private void updateDevice(Devices devices\\[])");

        assertFalse(updateDeviceBody.contains("CHANNEL_STATE_ID"),
                "updateDevice must not write state-id; live status/DPS handlers are authoritative");
        assertFalse(updateDeviceBody.contains("CHANNEL_FAN_POWER"),
                "updateDevice must not write fan-power; live status handlers are authoritative");
    }

    @Test
    void handleGetConsumablesRemainsAuthoritativeWriterForConsumablePercentChannels() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String consumablesBody = extractMethodBody(source, "private void handleGetConsumables\\(String response\\)");

        assertTrue(consumablesBody.contains("CHANNEL_CONSUMABLE_MAIN_PERC"),
                "handleGetConsumables should write main consumable percent channel");
        assertTrue(consumablesBody.contains("CHANNEL_CONSUMABLE_SIDE_PERC"),
                "handleGetConsumables should write side consumable percent channel");
        assertTrue(consumablesBody.contains("CHANNEL_CONSUMABLE_FILTER_PERC"),
                "handleGetConsumables should write filter consumable percent channel");
    }

    @Test
    void liveStatusAndDpsHandlersRemainAuthoritativeWritersForBatteryChannel() throws IOException {
        String source = Files.readString(HANDLER_PATH);
        String handleGetStatusBody = extractMethodBody(source, "private void handleGetStatus\\(String response\\)");

        assertTrue(handleGetStatusBody.contains("CHANNEL_BATTERY"),
                "handleGetStatus should continue writing battery channel from live status data");
        assertTrue(
                source.contains("dpsJsonObject.has(\"122\")")
                        && source.contains("updateState(CHANNEL_BATTERY, new DecimalType(battery));"),
                "handleMessage should continue writing battery channel from live DPS updates");
    }

    private static String extractMethodBody(String source, String methodSignatureRegex) {
        int signatureStart = source.indexOf(methodSignatureRegex.replace("\\", ""));
        assertTrue(signatureStart >= 0, "Method signature not found: " + methodSignatureRegex);

        int bodyStart = source.indexOf('{', signatureStart);
        assertTrue(bodyStart >= 0, "Method body start not found: " + methodSignatureRegex);

        int depth = 1;
        int index = bodyStart + 1;
        while (index < source.length() && depth > 0) {
            char current = source.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
            }
            index++;
        }

        assertTrue(depth == 0, "Method body end not found: " + methodSignatureRegex);
        return source.substring(bodyStart + 1, index - 1);
    }
}
