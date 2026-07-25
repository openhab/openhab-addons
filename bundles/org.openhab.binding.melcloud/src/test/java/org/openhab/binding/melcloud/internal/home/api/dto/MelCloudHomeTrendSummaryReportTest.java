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
 * Unit tests for {@link MelCloudHomeTrendSummaryReport}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeTrendSummaryReportTest {

    private static MelCloudHomeTrendDataPoint pointOf(String x, double y) {
        MelCloudHomeTrendDataPoint point = new MelCloudHomeTrendDataPoint();
        point.x = x;
        point.y = y;
        return point;
    }

    @Test
    void whenOutdoorTemperatureDatasetIsPresentThenGetLatestOutdoorTemperatureReturnsItsLastPoint() {
        // Arrange
        MelCloudHomeTrendDataset dataset = new MelCloudHomeTrendDataset();
        dataset.label = "OUTDOOR_TEMPERATURE";
        dataset.data = List.of(pointOf("2026-07-22T00:00:00", 4.0), pointOf("2026-07-23T00:00:00", 6.5));
        MelCloudHomeTrendSummaryReport report = new MelCloudHomeTrendSummaryReport();
        report.datasets = List.of(dataset);

        // Act
        Optional<Double> latest = report.getLatestOutdoorTemperature();

        // Assert
        assertEquals(Optional.of(6.5), latest);
    }

    @Test
    void whenNoDatasetMatchesOutdoorTemperatureThenGetLatestOutdoorTemperatureReturnsEmpty() {
        // Arrange
        MelCloudHomeTrendDataset dataset = new MelCloudHomeTrendDataset();
        dataset.label = "ROOM_TEMPERATURE";
        dataset.data = List.of(pointOf("2026-07-23T00:00:00", 21.0));
        MelCloudHomeTrendSummaryReport report = new MelCloudHomeTrendSummaryReport();
        report.datasets = List.of(dataset);

        // Act
        Optional<Double> latest = report.getLatestOutdoorTemperature();

        // Assert
        assertTrue(latest.isEmpty());
    }
}
