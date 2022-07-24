/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.dto.Home;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent.NAEventsDataResponse;
import org.openhab.binding.netatmo.internal.api.dto.Ping;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;

/**
 * Base class for all Security related endpoints
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SecurityApi extends RestManager {
    public SecurityApi(ApiBridgeHandler apiClient) {
        super(apiClient, FeatureArea.SECURITY);
    }

    /**
     * Dissociates a webhook from a user.
     *
     * @throws NetatmoException If fail to call the API, e.g. server error or deserializing
     */
    public void dropWebhook() throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_DROP_WEBHOOK);
        post(uriBuilder, ApiResponse.Ok.class, null, null);
    }

    /**
     * Links a callback url to a user.
     *
     * @param uri Your webhook callback url (required)
     * @throws NetatmoException If fail to call the API, e.g. server error or deserializing
     */
    public boolean addwebhook(URI uri) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_ADD_WEBHOOK, PARAM_URL, uri.toString());
        post(uriBuilder, ApiResponse.Ok.class, null, null);
        return true;
    }

    public Collection<HomeEvent> getPersonEvents(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_GET_EVENTS, PARAM_HOME_ID, homeId, PARAM_PERSON_ID, personId,
                PARAM_OFFSET, 1);
        NAEventsDataResponse response = get(uriBuilder, NAEventsDataResponse.class);
        BodyResponse<Home> body = response.getBody();
        if (body != null) {
            Home home = body.getElement();
            if (home != null) {
                return home.getEvents().stream().filter(event -> personId.equals(event.getPersonId()))
                        .collect(Collectors.toList());
            }
        }
        throw new NetatmoException("home should not be null");
    }

    public Collection<HomeEvent> getDeviceEvents(String homeId, String deviceId, String deviceType)
            throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_GET_EVENTS, PARAM_HOME_ID, homeId, PARAM_DEVICE_ID, deviceId,
                PARAM_DEVICES_TYPE, deviceType);
        BodyResponse<Home> body = get(uriBuilder, NAEventsDataResponse.class).getBody();
        if (body != null) {
            Home home = body.getElement();
            if (home != null) {
                return home.getEvents();
            }
        }
        throw new NetatmoException("home should not be null");
    }

    public String ping(String vpnUrl) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(vpnUrl).path(PATH_COMMAND).path(SUB_PATH_PING);
        Ping response = get(uriBuilder, Ping.class);
        return response.getStatus();
    }

    public void changeStatus(String localCameraURL, boolean setOn) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(localCameraURL).path(PATH_COMMAND).path(SUB_PATH_CHANGESTATUS);
        uriBuilder.queryParam(PARAM_STATUS, setOn ? "on" : "off");
        post(uriBuilder, ApiResponse.Ok.class, null, null);
    }

    public void changeFloodLightMode(String homeId, String cameraId, FloodLightMode mode) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder(PATH_STATE);
        String payload = String.format(
                "{\"home\": {\"id\":\"%s\",\"modules\": [ {\"id\":\"%s\",\"floodlight\":\"%s\"} ]}}", homeId, cameraId,
                mode.name().toLowerCase());
        post(uriBuilder, ApiResponse.Ok.class, payload, "application/json;charset=utf-8");
    }

    public void setPersonAwayStatus(String homeId, String personId, boolean away) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder(away ? SUB_PATH_PERSON_AWAY : SUB_PATH_PERSON_HOME);
        String payload = String.format(
                away ? "{\"home_id\":\"%s\",\"person_id\":\"%s\"}" : "{\"home_id\":\"%s\",\"person_ids\":[\"%s\"]}",
                homeId, personId);
        post(uriBuilder, ApiResponse.Ok.class, payload, "application/json;charset=utf-8");
    }
}
