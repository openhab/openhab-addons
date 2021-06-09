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

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NALastEventsData;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class SecurityApi extends RestManager {
    public SecurityApi(ApiBridge apiClient) { // NO_UCD (unused code)
        super(apiClient, NetatmoConstants.SECURITY_SCOPES);
    }

    // /**
    // *
    // * Returns information about users homes and cameras.
    // *
    // * @param homeId Specify if you're looking for the events of a specific Home. (optional)
    // * @param size Number of events to retrieve. Default is 30. (optional)
    // * @return NAWelcomeHomeDataResponse
    // * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
    // * response body
    // */
    // private NAObjectMap<NAHome> getWelcomeData(@Nullable String homeId) throws NetatmoException {
    // // String req = "gethomedata";
    // // if (homeId != null) {
    // // req += "?home_id=" + homeId;
    // // }
    // // NAHomesDataResponse response = get(req, NAHomesDataResponse.class);
    // // return response;
    //
    // UriBuilder apiUriBuilder = getApiUriBuilder();
    // apiUriBuilder = apiUriBuilder.path(NA_GETHOMEDATA_SPATH);
    //
    // if (homeId != null) {
    // apiUriBuilder.queryParam(NA_HOMEID_PARAM, homeId);
    // }
    // NAHomesDataResponse response = get(apiUriBuilder.build(), NAHomesDataResponse.class);
    // return response.getBody();
    // }

    // public NAHome getWelcomeHomeData(String homeId) throws NetatmoException {
    // // NAHomesDataResponse response = getWelcomeData(homeId);
    // // return response.getBody().getHomes().get(0);
    //
    // NAObjectMap<NAHome> homeList = getHomeData(null);
    // NAHome home = homeList.get(homeId);
    // if (home != null) {
    // return home;
    // }
    // throw new NetatmoException(String.format("Home %s was not found", homeId));
    // }

    // public NAHomeData getWelcomeDataBody() throws NetatmoException {
    // return getWelcomeData(null).getBody();
    // }

    /**
     *
     * Dissociates a webhook from a user.
     *
     * @return boolean Success
     * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    public boolean dropWebhook() throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(NA_DROPWEBHOOK_SPATH);
        post(uriBuilder, ApiOkResponse.class, null);
        return true;
    }

    /**
     *
     * Links a callback url to a user.
     *
     * @param uri Your webhook callback url (required)
     * @return boolean Success
     * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    public boolean addwebhook(URI uri) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(NA_ADDWEBHOOK_SPATH);
        uriBuilder.queryParam("url", uri.toString());
        post(uriBuilder, ApiOkResponse.class, null);
        return true;
    }

    private class NALastEventsDataResponse extends ApiResponse<NALastEventsData> {
    }

    public List<NAHomeEvent> getLastEventOf(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(NA_GETLASTEVENT_SPATH);
        uriBuilder.queryParam(NA_HOMEID_PARAM, homeId);
        uriBuilder.queryParam("person_id", personId);
        NALastEventsDataResponse response = get(uriBuilder, NALastEventsDataResponse.class);
        return response.getBody().getEvents();
    }
}
