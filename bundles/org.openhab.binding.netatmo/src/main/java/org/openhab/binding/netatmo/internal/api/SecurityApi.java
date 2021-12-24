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

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.PresenceLightMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent.NALastEventsDataResponse;
import org.openhab.binding.netatmo.internal.api.dto.NAPing;

/**
 * Base class for all Security related endpoints
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SecurityApi extends RestManager {
    public SecurityApi(ApiBridge apiClient) {
        super(apiClient, FeatureArea.SECURITY);
    }

    /**
     *
     * Dissociates a webhook from a user.
     *
     * @return boolean Success
     * @throws NetatmoException If fail to call the API, e.g. server error or deserializing
     */
    public boolean dropWebhook() throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_DROPWEBHOOK);
        post(uriBuilder, ApiResponse.Ok.class, null);
        return true;
    }

    /**
     *
     * Links a callback url to a user.
     *
     * @param uri Your webhook callback url (required)
     * @return boolean Success
     * @throws NetatmoException If fail to call the API, e.g. server error or deserializing
     */
    public boolean addwebhook(URI uri) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_ADDWEBHOOK);
        uriBuilder.queryParam(PARAM_URL, uri.toString());
        post(uriBuilder, ApiResponse.Ok.class, null);
        return true;
    }

    public Collection<NAHomeEvent> getLastEventsOf(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_GETLASTEVENT, PARAM_HOMEID, homeId, PARAM_PERSONID, personId);
        NALastEventsDataResponse response = get(uriBuilder, NALastEventsDataResponse.class);
        return response.getBody().getElements();
    }

    public String ping(String vpnUrl) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(vpnUrl).path(PATH_COMMAND).path(SUB_PATH_PING);
        NAPing response = get(uriBuilder, NAPing.class);
        return response.getStatus();
    }

    public boolean changeStatus(String localCameraURL, boolean setOn) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(localCameraURL).path(PATH_COMMAND).path(PARAM_CHANGESTATUS);
        uriBuilder.queryParam("status", setOn ? "on" : "off");
        post(uriBuilder, ApiResponse.Ok.class, null);
        return true;
    }

    public boolean changeFloodLightMode(String localCameraURL, PresenceLightMode mode) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(localCameraURL).path(PATH_COMMAND).path(PARAM_FLOODLIGHTSET);
        uriBuilder.queryParam("config", "%7B%22mode%22:%22" + mode.toString() + "%22%7D");
        get(uriBuilder, ApiResponse.Ok.class);
        return true;
    }

    public boolean setPersonAway(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder(SUB_PATH_PERSON_AWAY);
        String payload = String.format("{\"home_id\":\"%s\",\"person_id\":\"%s\"}", homeId, personId);
        post(uriBuilder, ApiResponse.Ok.class, payload);
        return true;
    }

    public boolean setPersonHome(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder(SUB_PATH_PERSON_HOME);
        String payload = String.format("{\"home_id\":\"%s\",\"person_ids\":[\"%s\"]}", homeId, personId);
        post(uriBuilder, ApiResponse.Ok.class, payload);
        return true;
    }
}
