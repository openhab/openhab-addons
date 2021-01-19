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
package org.openhab.binding.netatmo.internal.api.aircare;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.RestManager;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants;
import org.openhab.binding.netatmo.internal.api.dto.NADeviceDataBody;
import org.openhab.binding.netatmo.internal.api.weather.NAMain;
import org.openhab.binding.netatmo.internal.api.weather.WeatherApi.NAStationDataResponse;

/**
 * Base class for all Air Care related rest manager
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AircareApi extends RestManager {

    public AircareApi(ApiBridge apiClient) {
        super(apiClient, NetatmoConstants.AIR_QUALITY_SCOPES);
    }

    /**
     *
     * The method gethomecoachsdata Returns data from a user Healthy Home Coach Station (measures and device specific
     * data).
     *
     * @param deviceId Id of the device you want to retrieve information of (optional)
     * @return NAStationDataResponse
     * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    private NAStationDataResponse getHomeCoachData(@Nullable String deviceId) throws NetatmoException {
        String req = "gethomecoachsdata";
        if (deviceId != null) {
            req += "?device_id=" + deviceId;
        }
        NAStationDataResponse response = get(req, NAStationDataResponse.class);
        return response;
    }

    public NAMain getHomeCoachDataBody(String deviceId) throws NetatmoException {
        NADeviceDataBody<NAMain> answer = getHomeCoachData(deviceId).getBody();
        NAMain result = answer.getDevice(deviceId);
        if (result != null) {
            return result;
        }
        throw new NetatmoException(String.format("Unexpected answer searching device '%s' : not found.", deviceId));
    }
}
