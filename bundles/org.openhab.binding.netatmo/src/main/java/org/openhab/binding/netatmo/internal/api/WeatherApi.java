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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureLimit;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureScale;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureType;
import org.openhab.binding.netatmo.internal.api.dto.NADeviceDataBody;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAMeasureBodyElem;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class WeatherApi extends RestManager {
    public class NAStationDataResponse extends ApiResponse<NADeviceDataBody<NAMain>> {
    }

    public class NAMeasuresResponse extends ApiResponse<List<NAMeasureBodyElem>> {
    }

    public WeatherApi(ApiBridge apiClient) {
        super(apiClient, NetatmoConstants.WEATHER_SCOPES);
    }

    /**
     *
     * The method getstationsdata Returns data from a user&#x27;s Weather Stations (measures and device specific
     * data).
     *
     * @param deviceId Id of the device you want to retrieve information of (optional)
     * @param getFavorites Whether to include the user&#x27;s favorite Weather Stations in addition to the user&#x27;s
     *            own Weather Stations (optional, default to false)
     * @return NAStationDataResponse
     * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    private NAStationDataResponse getStationsData(@Nullable String deviceId, boolean getFavorites)
            throws NetatmoException {
        String req = "getstationsdata";
        if (deviceId != null) {
            req += "?device_id=" + deviceId;
        }
        // TODO : getFavorites is not used
        NAStationDataResponse response = get(req, NAStationDataResponse.class);
        return response;
    }

    public NAMain getStationData(String deviceId) throws NetatmoException {
        NADeviceDataBody<NAMain> answer = getStationsData(deviceId, true).getBody();
        NAMain station = answer.getDevice(deviceId);
        if (station != null) {
            return station;
        }
        throw new NetatmoException(String.format("Unexpected answer cherching device '%s' : not found.", deviceId));
    }

    public NADeviceDataBody<NAMain> getStationsDataBody(@Nullable String deviceId) throws NetatmoException {
        return getStationsData(deviceId, true).getBody();
    }

    public double getMeasurements(String deviceId, @Nullable String moduleId, MeasureScale scale, MeasureType type,
            MeasureLimit limit) throws NetatmoException {
        List<NAMeasureBodyElem> result = getmeasure(deviceId, moduleId, scale,
                new String[] { (limit.toString() + "_" + type.toString()).toLowerCase() }, 0, 0, 0, false, false);
        return result.size() > 0 ? result.get(0).getSingleValue() : Double.NaN;
    }

    public double getMeasurements(String deviceId, @Nullable String moduleId, MeasureScale scale, MeasureType type)
            throws NetatmoException {
        List<NAMeasureBodyElem> result = getmeasure(deviceId, moduleId, scale,
                new String[] { type.toString().toLowerCase() }, 0, 0, 0, false, false);
        return result.size() > 0 ? result.get(0).getSingleValue() : Double.NaN;
    }

    public List<NAMeasureBodyElem> getmeasure(String deviceId, @Nullable String moduleId, MeasureScale scale,
            String[] type, long dateBegin, long dateEnd, int limit, boolean optimize, boolean realTime)
            throws NetatmoException {
        String req = "getmeasure?device_id=" + deviceId;
        if (moduleId != null) {
            req += "&module_id=" + moduleId;
        }
        req += "&scale=" + scale.getDescriptor();
        for (String measureType : type) {
            req += "&type=" + measureType;
        }
        if (dateBegin > 0) {
            req += "&date_begin=" + String.valueOf(dateBegin);
        }
        if (dateEnd > 0 && dateEnd > dateBegin) {
            req += "&date_end=" + String.valueOf(dateEnd);
        } else {
            req += "&date_end=last";
        }
        if (limit > 0 && limit <= 1024) {
            req += "&limit=" + String.valueOf(limit);
        }
        if (optimize) {
            req += "&optimize=true";
        }
        if (realTime) {
            req += "&real_time=true";
        }
        NAMeasuresResponse response = get(req, NAMeasuresResponse.class);
        return response.getBody();
    }
}
