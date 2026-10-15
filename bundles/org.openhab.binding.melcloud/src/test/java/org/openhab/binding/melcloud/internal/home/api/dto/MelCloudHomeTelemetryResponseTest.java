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
 * Unit tests for {@link MelCloudHomeTelemetryResponse}.
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
    void whenMeasureDataIsEmptyThenGetLatestValueReturnsEmpty() {
        // Arrange
        MelCloudHomeTelemetryResponse response = new MelCloudHomeTelemetryResponse();

        // Act
        Optional<Double> latest = response.getLatestValue();

        // Assert
        assertEquals(Optional.empty(), latest);
    }

    @Test
    void whenValuesArePresentThenGetLatestValueReturnsTheLastOne() {
        // Arrange
        MelCloudHomeMeasureData measureData = new MelCloudHomeMeasureData();
        measureData.values = List.of(valueOf("2026-07-22T00:00:00Z", "100.0"),
                valueOf("2026-07-23T00:00:00Z", "150.5"));
        MelCloudHomeTelemetryResponse response = new MelCloudHomeTelemetryResponse();
        response.measureData = List.of(measureData);

        // Act
        Optional<Double> latest = response.getLatestValue();

        // Assert
        assertEquals(Optional.of(150.5), latest);
    }

    @Test
    void whenSeriesEndsWithAResetMarkerThenTheLastValueIsReturned() {
        // Arrange: the ATA cumulative series delivers 100 Wh pulses followed by a 0 reset marker. The binding
        // publishes the latest bucket as an interval reading, so the marker is reported as the wire last sent it
        // rather than being summed into a cumulative value here.
        MelCloudHomeMeasureData measureData = new MelCloudHomeMeasureData();
        measureData.values = List.of(valueOf("2026-07-22T10:00:00Z", "100.0"), valueOf("2026-07-22T10:01:00Z", "0.0"));
        MelCloudHomeTelemetryResponse response = new MelCloudHomeTelemetryResponse();
        response.measureData = List.of(measureData);

        // Act
        Optional<Double> latest = response.getLatestValue();

        // Assert
        assertEquals(Optional.of(0.0), latest);
    }

    @Test
    void whenLatestValueIsNotNumericThenGetLatestValueReturnsEmpty() {
        // Arrange
        MelCloudHomeMeasureData measureData = new MelCloudHomeMeasureData();
        measureData.values = List.of(valueOf("2026-07-23T00:00:00Z", "not-a-number"));
        MelCloudHomeTelemetryResponse response = new MelCloudHomeTelemetryResponse();
        response.measureData = List.of(measureData);

        // Act
        Optional<Double> latest = response.getLatestValue();

        // Assert
        assertTrue(latest.isEmpty());
    }
}
