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
 * Unit tests for {@link MelCloudHomeTelemetryResponse} (see ADR-003).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeTelemetryResponseTest {

    private static MelCloudHomeMeasureValue valueOf(String time, String value) {
        MelCloudHomeMeasureValue measureValue = new MelCloudHomeMeasureValue();
        measureValue.time = time;
        measureValue.value = value;
        return measureValue;
    }

    @Test
    void whenMeasureDataIsEmpty_thenGetLatestValueWhReturnsEmpty() {
        // Arrange
        MelCloudHomeTelemetryResponse response = new MelCloudHomeTelemetryResponse();

        // Act
        Optional<Double> latest = response.getLatestValueWh();

        // Assert
        assertEquals(Optional.empty(), latest);
    }

    @Test
    void whenValuesArePresent_thenGetLatestValueWhReturnsTheLastOne() {
        // Arrange
        MelCloudHomeMeasureData measureData = new MelCloudHomeMeasureData();
        measureData.values = List.of(valueOf("2026-07-22T00:00:00Z", "100.0"),
                valueOf("2026-07-23T00:00:00Z", "150.5"));
        MelCloudHomeTelemetryResponse response = new MelCloudHomeTelemetryResponse();
        response.measureData = List.of(measureData);

        // Act
        Optional<Double> latest = response.getLatestValueWh();

        // Assert
        assertEquals(Optional.of(150.5), latest);
    }

    @Test
    void whenLatestValueIsNotNumeric_thenGetLatestValueWhReturnsEmpty() {
        // Arrange
        MelCloudHomeMeasureData measureData = new MelCloudHomeMeasureData();
        measureData.values = List.of(valueOf("2026-07-23T00:00:00Z", "not-a-number"));
        MelCloudHomeTelemetryResponse response = new MelCloudHomeTelemetryResponse();
        response.measureData = List.of(measureData);

        // Act
        Optional<Double> latest = response.getLatestValueWh();

        // Assert
        assertTrue(latest.isEmpty());
    }
}
