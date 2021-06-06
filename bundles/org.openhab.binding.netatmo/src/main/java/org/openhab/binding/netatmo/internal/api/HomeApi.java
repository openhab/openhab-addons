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

import static org.openhab.binding.netatmo.internal.api.NetatmoConstants.*;

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.PresenceLightMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAPing;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeApi extends RestManager {

    public HomeApi(ApiBridge apiClient) {
        super(apiClient);
    }

    public class NAHomesDataResponse extends ApiResponse<NAHomeData> {
    }

    public class NAHomeDataResponse extends ApiResponse<NAHomeData> {
    }

    public NAHome getHomeData(String homeId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(NA_GETHOME_SPATH).queryParam("home_id", homeId);
        NAHomeDataResponse response = get(uriBuilder.build(), NAHomeDataResponse.class);

        List<NAHome> homes = response.getBody().getHomes();

        if (homes.size() != 1) {
            throw new NetatmoException(String.format("Home %s was not found", homeId));
        }
        return homes.get(0);
    }

    public List<NAHome> getHomeList(@Nullable ModuleType type) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(NA_HOMES_SPATH);

        if (type != null) {
            uriBuilder.queryParam("gateway_types", type.name());
        }

        NAHomesDataResponse response = get(uriBuilder.build(), NAHomesDataResponse.class);
        return response.getBody().getHomes();
    }

    public NAHome getHomesData(String homeId, @Nullable ModuleType type) throws NetatmoException {
        String req = "homesdata?home_id=" + homeId;
        if (type != null) {
            req += "&gateway_types=" + type.name();
        }
        NAHomesDataResponse response = get(req, NAHomesDataResponse.class);
        return response.getBody().getHomes().get(0);
    }

    public boolean setpersonsaway(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder().path(NA_PERSON_AWAY_SPATH);
        String payload = String.format("{\"home_id\":\"%s\",\"person_id\":\"%s\"}", homeId, personId);
        post(uriBuilder.build(), ApiOkResponse.class, payload);
        return true;
    }

    public boolean setpersonshome(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder().path(NA_PERSON_HOME_SPATH);
        String payload = String.format("{\"home_id\":\"%s\",\"person_ids\":[\"%s\"]}", homeId, personId);
        post(uriBuilder.build(), ApiOkResponse.class, payload);
        return true;
    }

    // public boolean switchSchedule(String homeId, String scheduleId) throws NetatmoException {
    // String req = "switchschedule";
    // String payload = String.format("{\"home_id\":\"%s\",\"schedule_id\":\"%s\"}", homeId, scheduleId);
    // ApiOkResponse response = post(req, payload, ApiOkResponse.class, false);
    // if (!response.isSuccess()) {
    // throw new NetatmoException(String.format("Unsuccessfull schedule change : %s", response.getStatus()));
    // }
    // return true;
    // }

    public String ping(String vpnUrl) throws NetatmoException {
        String url = vpnUrl + "/command/ping";
        NAPing response = get(url, NAPing.class);
        return response.getStatus();
    }

    public boolean changeStatus(String localCameraURL, boolean isOn) throws NetatmoException {
        String url = localCameraURL + "/command/changestatus?status=" + (isOn ? "on" : "off");
        ApiOkResponse response = post(url, null, ApiOkResponse.class, false);
        if (!response.isSuccess()) {
            throw new NetatmoException(String.format("Unsuccessfull camara status change : %s", response.getStatus()));
        }
        return true;
    }

    public boolean changeFloodLightMode(String localCameraURL, PresenceLightMode mode) throws NetatmoException {
        String url = localCameraURL + "/command/floodlight_set_config?config=%7B%22mode%22:%22" + mode.toString()
                + "%22%7D";
        ApiOkResponse response = get(url, ApiOkResponse.class);
        if (!response.isSuccess()) {
            throw new NetatmoException(String.format("Unsuccessfull camara status change : %s", response.getStatus()));
        }
        return true;
    }
}
