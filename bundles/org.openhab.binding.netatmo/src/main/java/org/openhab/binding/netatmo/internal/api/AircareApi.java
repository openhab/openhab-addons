/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAMain.StationDataResponse;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;

/**
 * Base class for all Air Care related endpoints
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AircareApi extends RestManager {

    public AircareApi(ApiBridgeHandler apiClient) {
        super(apiClient, FeatureArea.AIR_CARE);
    }

    /**
     * Returns data from Healthy Home Coach Station (measures and device specific data).
     *
     * @param deviceId Id of the device you want to retrieve information of (optional)
     * @return StationDataResponse
     * @throws NetatmoException If fail to call the API, e.g. server error or deserializing
     */
    public StationDataResponse getHomeCoachData(@Nullable String deviceId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_HOMECOACH, PARAM_DEVICE_ID, deviceId);
        return get(uriBuilder, StationDataResponse.class);
    }

    public NAMain getHomeCoach(String deviceId) throws NetatmoException {
        ListBodyResponse<NAMain> answer = getHomeCoachData(deviceId).getBody();
        if (answer != null) {
            NAMain station = answer.getElement(deviceId);
            if (station != null) {
                return station;
            }
        }
        throw new NetatmoException("Unexpected answer querying device '%s' : not found.", deviceId);
    }
}
