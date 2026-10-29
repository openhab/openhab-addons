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

    private static MelCloudHomeTelemetryResponse responseOf(MelCloudHomeMeasureValue... values) {
        MelCloudHomeMeasureData measureData = new MelCloudHomeMeasureData();
        measureData.values = List.of(values);
        MelCloudHomeTelemetryResponse response = new MelCloudHomeTelemetryResponse();
        response.measureData = List.of(measureData);
        return response;
    }

    @Test
    void whenMeasureDataIsEmptyThenGetLatestSampleReturnsEmpty() {
        // Arrange
        MelCloudHomeTelemetryResponse response = new MelCloudHomeTelemetryResponse();

        // Act
        Optional<MelCloudHomeMeasureValue> latest = response.getLatestSample();

        // Assert
        assertEquals(Optional.empty(), latest);
    }

    @Test
    void whenValuesArePresentThenGetLatestSampleReturnsTheLastOne() {
        // Arrange
        MelCloudHomeTelemetryResponse response = responseOf(valueOf("2026-07-22 10:00", "100.0"),
                valueOf("2026-07-22 11:00", "150.5"));

        // Act
        Optional<MelCloudHomeMeasureValue> latest = response.getLatestSample();

        // Assert
        assertEquals(Optional.of("150.5"), latest.map(sample -> sample.value));
        assertEquals(Optional.of("2026-07-22 11:00"), latest.map(sample -> sample.time));
    }

    @Test
    void whenSeriesEndsWithAResetMarkerThenTheLastSampleKeepsTheMarker() {
        // Arrange: the ATA cumulative series delivers 100 Wh pulses followed by a 0 reset marker. The DTO stays
        // wire-verbatim here; deciding whether the bucket is current is the API client's job.
        MelCloudHomeTelemetryResponse response = responseOf(valueOf("2026-07-22 10:00", "100.0"),
                valueOf("2026-07-22 10:01", "0.0"));

        // Act
        Optional<MelCloudHomeMeasureValue> latest = response.getLatestSample();

        // Assert
        assertEquals(Optional.of("0.0"), latest.map(sample -> sample.value));
    }
}
