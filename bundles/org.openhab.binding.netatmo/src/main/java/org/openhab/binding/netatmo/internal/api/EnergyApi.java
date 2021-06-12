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

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class EnergyApi extends RestManager {

    public class NAHomeStatusResponse extends ApiResponse<NAHomeStatus> {
    }

    public EnergyApi(ApiBridge apiClient) {
        super(apiClient, NetatmoConstants.ENERGY_SCOPES);
    }

    public NAHome getHomeStatus(String homeId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_HOMESTATUS).queryParam(PARM_HOMEID, homeId).queryParam(
                PARM_DEVICETYPE, ModuleType.NAPlug.name(), ModuleType.NRV.name(), ModuleType.NATherm1.name());

        NAHomeStatusResponse response = get(uriBuilder, NAHomeStatusResponse.class);
        return response.getBody().getHome();
    }

    /**
     *
     * The method switchschedule switches the home&#x27;s schedule to another existing schedule.
     *
     * @param homeId The id of home (required)
     * @param scheduleId The schedule id. It can be found in the getthermstate response, under the keys
     *            therm_program_backup and therm_program. (required)
     * @return boolean success
     * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    public boolean switchSchedule(String homeId, String scheduleId) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder().path(SPATH_SWITCHSCHEDULE);
        uriBuilder.queryParam(PARM_HOMEID, homeId).queryParam(PARM_SCHEDULEID, scheduleId);
        post(uriBuilder, ApiResponse.Ok.class, null);
        return true;
    }

    /**
     *
     * This endpoint permits to control the heating of a specific home. A home can be set in 3 differents modes:
     * "schedule" mode in which the home will follow the user schedule
     * "away" mode which will put the whole house to away (default is 12° but can be changed by the user in its
     * settings)
     * "hg" corresponds to frostguard mode (7° by default)
     *
     * @param homeId The id of home (required)
     * @param mode The mode. (required)
     * @return boolean success
     * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    public boolean setThermMode(String homeId, String mode) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_SETTHERMMODE);
        uriBuilder.queryParam(PARM_HOMEID, homeId).queryParam(PARM_MODE, mode);
        post(uriBuilder, ApiResponse.Ok.class, null);
        return true;
    }

    /**
     *
     * The method setroomthermpoint changes the Thermostat manual temperature setpoint.
     *
     * @param homeId The id of home (required)
     * @param roomId The id of the room (required)
     * @param mode The mode. (required)
     * @param endtime When using the manual or max setpoint_mode, this parameter defines when the setpoint
     *            expires. (optional)
     * @param temp When using the manual setpoint_mode, this parameter defines the temperature setpoint (in
     *            Celcius) to use. (optional)
     * @return ApiResponse.Ok
     * @throws NetatmoCommunicationException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    public boolean setRoomThermpoint(String homeId, String roomId, SetpointMode mode, long endtime, double temp)
            throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_SETROOMTHERMPOINT).queryParam(PARM_HOMEID, homeId)
                .queryParam(PARM_ROOMID, roomId).queryParam(PARM_MODE, mode.getDescriptor());
        if (mode == SetpointMode.MANUAL || mode == SetpointMode.MAX) {
            uriBuilder.queryParam("endtime", endtime);
            if (mode == SetpointMode.MANUAL) {
                uriBuilder.queryParam("temp", temp);
            }
        }
        post(uriBuilder, ApiResponse.Ok.class, null);
        return true;
    }
}
