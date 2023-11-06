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
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;

/**
 * The {@link EnergyApi} handles API endpoints related to Energy feature area
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class EnergyApi extends RestManager {
    public EnergyApi(ApiBridgeHandler apiClient) {
        super(apiClient, FeatureArea.ENERGY);
    }

    /**
     *
     * The method switchSchedule switches the home's schedule to another existing schedule.
     *
     * @param homeId The id of home (required)
     * @param scheduleId The schedule id. It can be found in the getthermstate response, under the keys
     *            therm_program_backup and therm_program. (required)
     * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    public void switchSchedule(String homeId, String scheduleId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_SWITCH_SCHEDULE, PARAM_HOME_ID, homeId, PARAM_SCHEDULE_ID,
                scheduleId);
        post(uriBuilder, ApiResponse.Ok.class, null);
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
     * @throws NetatmoException when call failed, e.g. server error or cannot deserialize
     */
    public void setThermMode(String homeId, String mode) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_SET_THERM_MODE, PARAM_HOME_ID, homeId, PARAM_MODE, mode);
        post(uriBuilder, ApiResponse.Ok.class, null);
    }

    /**
     *
     * The method setThermpoint changes the Thermostat manual temperature setpoint.
     *
     * @param homeId The id of home (required)
     * @param roomId The id of the room (required)
     * @param mode The mode. (required)
     * @param endtime For manual or max setpoint_mode, defines when the setpoint expires.
     * @param temp For manual setpoint_mode, defines the temperature setpoint (in °C)
     * @throws NetatmoException when call failed, e.g. server error or cannot deserialize
     */
    public void setThermpoint(String homeId, String roomId, SetpointMode mode, long endtime, double temp)
            throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_SET_ROOM_THERMPOINT, PARAM_HOME_ID, homeId, PARAM_ROOM_ID,
                roomId, PARAM_MODE, mode.apiDescriptor);
        if (mode == SetpointMode.MANUAL || mode == SetpointMode.MAX) {
            uriBuilder.queryParam("endtime", endtime);
            if (mode == SetpointMode.MANUAL) {
                uriBuilder.queryParam("temp", temp > THERM_MAX_SETPOINT ? THERM_MAX_SETPOINT : temp);
            }
        }
        post(uriBuilder, ApiResponse.Ok.class, null);
    }
}
