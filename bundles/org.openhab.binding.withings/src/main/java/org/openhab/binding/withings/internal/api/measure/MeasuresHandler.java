/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.api.measure;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.withings.internal.api.AbstractAPIHandler;
import org.openhab.binding.withings.internal.service.AccessTokenService;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class MeasuresHandler extends AbstractAPIHandler {

    private static final String MEASURE_API_URL = "https://wbsapi.withings.net/measure";
    private static final int WEIGHT_TYPE = 1;
    private static final int HEIGHT_TYPE = 4;
    private static final int FAT_MASS_TYPE = 8;

    public MeasuresHandler(AccessTokenService accessTokenService, HttpClient httpClient) {
        super(accessTokenService, httpClient);
    }

    public Optional<LatestMeasureData> loadLatestMeasureData() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("meastypes", "1,4,8"); // 1 = weight in kg, 4 = height in meters, 8 = fat mass in kg

        Optional<MeasuresResponseDTO> measuresResponse = executePOSTRequest(MEASURE_API_URL, "getmeas", parameters,
                MeasuresResponseDTO.class);
        if (measuresResponse.isPresent()) {
            MeasuresResponseDTO.MeasuresBody body = measuresResponse.get().getBody();
            if (body != null) {
                List<MeasuresResponseDTO.MeasureGroup> measureGroups = body.getMeasureGroups();
                if (measureGroups != null && !measureGroups.isEmpty()) {
                    measureGroups.sort(Comparator.reverseOrder());

                    return Optional.of(new LatestMeasureData(findFirstWeightMeasure(measureGroups),
                            findFirstHeightMeasure(measureGroups), findFirstFatMassMeasure(measureGroups)));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<MeasuresResponseDTO.Measure> findFirstWeightMeasure(
            List<MeasuresResponseDTO.MeasureGroup> measureGroups) {
        return findMeasure(measureGroups, WEIGHT_TYPE);
    }

    private Optional<MeasuresResponseDTO.Measure> findFirstHeightMeasure(
            List<MeasuresResponseDTO.MeasureGroup> measureGroups) {
        return findMeasure(measureGroups, HEIGHT_TYPE);
    }

    private Optional<MeasuresResponseDTO.Measure> findFirstFatMassMeasure(
            List<MeasuresResponseDTO.MeasureGroup> measureGroups) {
        return findMeasure(measureGroups, FAT_MASS_TYPE);
    }

    private Optional<MeasuresResponseDTO.Measure> findMeasure(List<MeasuresResponseDTO.MeasureGroup> measureGroups,
            int searchedType) {
        for (MeasuresResponseDTO.MeasureGroup measureGroup : measureGroups) {
            List<MeasuresResponseDTO.Measure> measures = measureGroup.getMeasures();
            if (measures != null) {
                for (MeasuresResponseDTO.Measure measure : measures) {
                    if (searchedType == measure.getType()) {
                        return Optional.of(measure);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
