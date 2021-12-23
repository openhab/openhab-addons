/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api;

import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.*;

import java.time.ZonedDateTime;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAMain.NAStationDataResponse;
import org.openhab.binding.netatmo.internal.api.dto.NAMeasureBodyElem;

/**
 * Base class for all Weather related endpoints
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WeatherApi extends RestManager {
    public WeatherApi(ApiBridge apiClient) {
        super(apiClient, FeatureArea.WEATHER);
    }

    /**
     *
     * Returns data from a user's Weather Stations (measures and device specific data);
     *
     * @param deviceId Id of the device you want to retrieve information of (optional)
     * @param getFavorites Whether to include the user's favorite Weather Stations in addition to the user's
     *            own Weather Stations (optional, default to false)
     * @return NAStationDataResponse
     * @throws NetatmoException If fail to call the API, e.g. server error or deserializing
     */
    public NAStationDataResponse getStationsData(@Nullable String deviceId, boolean getFavorites)
            throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SPATH_GETSTATION, PARAM_DEVICEID, deviceId, //
                PARAM_FAVORITES, getFavorites);
        NAStationDataResponse response = get(uriBuilder, NAStationDataResponse.class);
        return response;
    }

    public NAMain getStationData(String deviceId) throws NetatmoException {
        ListBodyResponse<NAMain> answer = getStationsData(deviceId, true).getBody();
        NAMain station = answer.getElement(deviceId);
        if (station != null) {
            return station;
        }
        throw new NetatmoException(String.format("Unexpected answer searching device '%s' : not found.", deviceId));
    }

    public @Nullable Object getMeasurements(String deviceId, @Nullable String moduleId, @Nullable String scale,
            MeasureClass measureClass) throws NetatmoException {
        NAMeasureBodyElem<?> result = getMeasure(deviceId, moduleId, scale, measureClass.apiDescriptor);
        return result.getSingleValue();
    }

    public @Nullable Object getMeasurements(String deviceId, @Nullable String moduleId, @Nullable String scale,
            MeasureClass measureClass, String limit) throws NetatmoException {
        String queryLimit = limit;
        if (!measureClass.apiDescriptor.contains("_")) {
            queryLimit += "_" + measureClass.apiDescriptor;
        }

        NAMeasureBodyElem<?> result = getMeasure(deviceId, moduleId, scale, queryLimit.toLowerCase());
        return result.getSingleValue();
    }

    private NAMeasureBodyElem<?> getMeasure(String deviceId, @Nullable String moduleId, @Nullable String scale,
            String measureType) throws NetatmoException {
        // NAMeasuresResponse is not designed for optimize=false
        UriBuilder uriBuilder = getApiUriBuilder(SPATH_GETMEASURE, PARAM_DEVICEID, deviceId, "real_time", true,
                "date_end", "last", "optimize", true, "type", measureType.toLowerCase(), PARAM_MODULEID, moduleId);

        if (scale != null) {
            uriBuilder.queryParam("scale", scale.toLowerCase());
        }
        if (measureType.startsWith("date")) {
            NADateMeasuresResponse response = get(uriBuilder, NADateMeasuresResponse.class);
            if (!response.getBody().isEmpty()) {
                return response.getBody().get(0);
            }
        } else {
            NAMeasuresResponse response = get(uriBuilder, NAMeasuresResponse.class);
            if (!response.getBody().isEmpty()) {
                return response.getBody().get(0);
            }
        }
        throw new NetatmoException("Empty response while getting measurements");
    }

    public class NAMeasuresResponse extends ApiResponse<List<NAMeasureBodyElem<Double>>> {
    }

    public class NADateMeasuresResponse extends ApiResponse<List<NAMeasureBodyElem<ZonedDateTime>>> {
    }
}
