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

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * One report in the {@code GET /report/v1/trendsummary} response (see ADR-003). The mobile BFF wraps the response
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
     * @return the latest (last) datapoint value of the dataset whose label contains
     *         {@value #OUTDOOR_TEMPERATURE_LABEL_MARKER}, if present
     */
    public Optional<Double> getLatestOutdoorTemperature() {
        return datasets.stream().filter(dataset -> dataset.label.contains(OUTDOOR_TEMPERATURE_LABEL_MARKER))
                .flatMap(dataset -> dataset.data.stream()).reduce((first, second) -> second)
                .map(dataPoint -> dataPoint.y);
    }
}
