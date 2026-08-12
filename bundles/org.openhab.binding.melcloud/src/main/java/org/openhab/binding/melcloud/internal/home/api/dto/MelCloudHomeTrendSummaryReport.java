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

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * One report in the {@code GET /report/v1/trendsummary} response. The mobile BFF wraps the response
 * in a top-level JSON array of these, so {@code MelCloudHomeApiClient} deserializes
 * {@code List<MelCloudHomeTrendSummaryReport>}. Used to extract the latest outdoor temperature datapoint for an ATA
 * unit; ATW units report outdoor temperature directly in their {@code settings} array instead
 * (see {@link MelCloudHomeAtwUnit#getOutdoorTemperature()}).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeTrendSummaryReport {

    private static final String OUTDOOR_TEMPERATURE_LABEL_MARKER = "OUTDOOR_TEMPERATURE";

    public List<MelCloudHomeTrendDataset> datasets = List.of();

    /**
     * Returns the latest genuine outdoor temperature reading of the dataset whose label contains
     * {@value #OUTDOOR_TEMPERATURE_LABEL_MARKER}, if present.
     *
     * <p>
     * With {@code period=Hourly}, the response mixes genuine unit readings with synthetic chart points the server
     * appends: bucket-aligned repeats of the last value, and a final point stamped with the query's own {@code to}
     * parameter echoed back verbatim. Both land exactly on a whole-minute ("HH:mm:00") boundary, whereas a genuine
     * reading carries the unit's actual upload time with arbitrary seconds. Points landing on ":00" seconds are
     * therefore treated as synthetic and skipped, walking backwards from the newest datapoint until a genuine one is
     * found. This mirrors the fix already applied to the reference Home Assistant MELCloud Home integration (issues
     * #152/#111 in {@code andrew-blake/melcloudhome}), which hit the same synthetic-point problem when it moved off
     * the less precise {@code Daily} period.
     *
     * @return the latest genuine reading's value, if any was found
     */
    public Optional<Double> getLatestOutdoorTemperature() {
        return datasets.stream().filter(dataset -> dataset.label.contains(OUTDOOR_TEMPERATURE_LABEL_MARKER)).findFirst()
                .flatMap(MelCloudHomeTrendSummaryReport::latestGenuineReading);
    }

    private static Optional<Double> latestGenuineReading(MelCloudHomeTrendDataset dataset) {
        List<MelCloudHomeTrendDataPoint> data = dataset.data;
        for (int i = data.size() - 1; i >= 0; i--) {
            MelCloudHomeTrendDataPoint point = data.get(i);
            if (!isSyntheticTimestamp(point.x)) {
                return Optional.of(point.y);
            }
        }
        return Optional.empty();
    }

    private static boolean isSyntheticTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp).getSecond() == 0;
        } catch (DateTimeParseException e) {
            // Unparseable timestamp; treat as unusable rather than risking a stale or bogus reading.
            return true;
        }
    }
}
