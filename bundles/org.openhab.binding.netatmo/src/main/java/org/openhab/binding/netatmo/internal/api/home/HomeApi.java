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
package org.openhab.binding.netatmo.internal.api.home;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ApiOkResponse;
import org.openhab.binding.netatmo.internal.api.ApiResponse;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.RestManager;
import org.openhab.binding.netatmo.internal.api.doc.ModuleType;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.PresenceLightMode;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeApi extends RestManager {

    public HomeApi(ApiBridge apiClient) {
        super(apiClient, Set.of());
    }

    public class NAHomesDataResponse extends ApiResponse<NAHomeData> {
    }

    public NAHomeData getHomesData(ModuleType type) throws NetatmoException {
        String req = "homesdata?gateway_types=" + type.name();
        NAHomesDataResponse response = get(req, NAHomesDataResponse.class);
        return response.getBody();
    }

    public NAHome getHomeData(String homeId) throws NetatmoException {
        String req = "homesdata?home_id=" + homeId;
        NAHomesDataResponse response = get(req, NAHomesDataResponse.class);
        return response.getBody().getHomes().get(0);
    }

    public boolean setpersonsaway(String homeId, String personId) throws NetatmoException {
        String req = "setpersonsaway";
        String payload = String.format("{\"home_id\":\"%s\",\"person_id\":\"%s\"}", homeId, personId);
        ApiOkResponse response = post(req, payload, ApiOkResponse.class, false);
        if (!response.isSuccess()) {
            throw new NetatmoException(String.format("Unsuccessfull person away command : %s", response.getStatus()));
        }
        return true;
    }

    public boolean setpersonshome(String homeId, String personId) throws NetatmoException {
        String req = "setpersonshome";
        String payload = String.format("{\"home_id\":\"%s\",\"person_ids\":[\"%s\"]}", homeId, personId);
        ApiOkResponse response = post(req, payload, ApiOkResponse.class, false);
        if (!response.isSuccess()) {
            throw new NetatmoException(String.format("Unsuccessfull person away command : %s", response.getStatus()));
        }
        return true;
    }

    public boolean switchSchedule(String homeId, String scheduleId) throws NetatmoException {
        String req = "switchschedule";
        String payload = String.format("{\"home_id\":\"%s\",\"schedule_id\":\"%s\"}", homeId, scheduleId);
        ApiOkResponse response = post(req, payload, ApiOkResponse.class, false);
        if (!response.isSuccess()) {
            throw new NetatmoException(String.format("Unsuccessfull schedule change : %s", response.getStatus()));
        }
        return true;
    }

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

    // TODO : did not find a way to have this work
    // public boolean changeSetpointDefaultDuration(String homeId, int intValue) throws NetatmoException {
    // String req = "api/sethomedata";
    // String payload = String.format("{\"home\":{\"id\":\"%s\",\"therm_setpoint_default_duration\":%d}}", homeId,
    // intValue);
    //
    // NAOkResponse response = apiHandler.post(req, payload, NAOkResponse.class, false);
    // if (!response.isSuccess()) {
    // throw new NetatmoException(
    // String.format("Unsuccessfull setpoint duration change : %s", response.getStatus()));
    // }
    // return true;
    // }
}
