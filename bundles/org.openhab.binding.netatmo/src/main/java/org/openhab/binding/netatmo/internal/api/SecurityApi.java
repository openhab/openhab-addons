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

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent.NAEventsDataResponse;
import org.openhab.binding.netatmo.internal.api.dto.NAPing;
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
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_DROPWEBHOOK);
        post(uriBuilder, ApiResponse.Ok.class, null);
    }

    /**
     * Links a callback url to a user.
     *
     * @param uri Your webhook callback url (required)
     * @throws NetatmoException If fail to call the API, e.g. server error or deserializing
     */
    public void addwebhook(URI uri) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_ADDWEBHOOK, PARAM_URL, uri.toString());
        post(uriBuilder, ApiResponse.Ok.class, null);
    }

    public Collection<NAHomeEvent> getPersonEvents(String homeId, String personId) throws NetatmoException {
        // Note : the contract of the API is not respected. It only retrieves the last event and return empty if not the
        // same person. Adding offset parameter gives a chance to get it but how to guess how many must be retrieved ???
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_GETEVENTS, PARAM_HOMEID, homeId, PARAM_PERSONID, personId);
        NAEventsDataResponse response = get(uriBuilder, NAEventsDataResponse.class);
        NAHome home = response.getBody().getElement();
        if (home != null) {
            return home.getEvents();
        }
        throw new NetatmoException("home should not be null");
    }

    public Collection<NAHomeEvent> getCameraEvents(String homeId, String cameraId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_GETEVENTS, PARAM_HOMEID, homeId, PARAM_DEVICEID, cameraId);
        NAEventsDataResponse response = get(uriBuilder, NAEventsDataResponse.class);
        NAHome home = response.getBody().getElement();
        if (home != null) {
            return home.getEvents();
        }
        throw new NetatmoException("home should not be null");
    }

    public String ping(String vpnUrl) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(vpnUrl).path(PATH_COMMAND).path(SUB_PATH_PING);
        NAPing response = get(uriBuilder, NAPing.class);
        return response.getStatus();
    }

    public void changeStatus(String localCameraURL, boolean setOn) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(localCameraURL).path(PATH_COMMAND).path(SUB_PATH_CHANGESTATUS);
        uriBuilder.queryParam(PARAM_STATUS, setOn ? "on" : "off");
        post(uriBuilder, ApiResponse.Ok.class, null);
    }

    public void changeFloodLightMode(String localCameraURL, FloodLightMode mode) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(localCameraURL).path(PATH_COMMAND).path(SUB_PATH_FLOODLIGHTSET);
        uriBuilder.queryParam("config", "%7B%22mode%22:%22" + mode.toString() + "%22%7D");
        get(uriBuilder, ApiResponse.Ok.class);
    }

    public void setPersonAwayStatus(String homeId, String personId, boolean away) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder(away ? SUB_PATH_PERSON_AWAY : SUB_PATH_PERSON_HOME);
        String payload = String.format(
                away ? "{\"home_id\":\"%s\",\"person_id\":\"%s\"}" : "{\"home_id\":\"%s\",\"person_ids\":[\"%s\"]}",
                homeId, personId);
        post(uriBuilder, ApiResponse.Ok.class, payload);
    }
}
