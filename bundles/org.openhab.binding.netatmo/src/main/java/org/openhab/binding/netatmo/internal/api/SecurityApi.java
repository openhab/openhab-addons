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

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.HomeApi.NAHomesDataResponse;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class SecurityApi extends RestManager {
    public SecurityApi(ApiBridge apiClient) {
        super(apiClient, NetatmoConstants.SECURITY_SCOPES);
    }

    /**
     *
     * Returns information about users homes and cameras.
     *
     * @param homeId Specify if you're looking for the events of a specific Home. (optional)
     * @param size Number of events to retrieve. Default is 30. (optional)
     * @return NAWelcomeHomeDataResponse
     * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    private NAHomesDataResponse getWelcomeData(@Nullable String homeId) throws NetatmoException {
        String req = "gethomedata";
        if (homeId != null) {
            req += "?home_id=" + homeId;
        }
        NAHomesDataResponse response = get(req, NAHomesDataResponse.class);
        return response;
    }

    public NAHome getWelcomeHomeData(String homeId) throws NetatmoException {
        NAHomesDataResponse response = getWelcomeData(homeId);
        return response.getBody().getHomes().get(0);
    }

    public NAHomeData getWelcomeDataBody() throws NetatmoException {
        return getWelcomeData(null).getBody();
    }

    /**
     *
     * Dissociates a webhook from a user.
     *
     * @return boolean Success
     * @throws NetatmoException If fail to call the API, e.g. server error or cannot deserialize the
     *             response body
     */
    public boolean dropWebhook() throws NetatmoException {
        String req = "dropwebhook";
        ApiOkResponse response = post(req, null, ApiOkResponse.class, true);
        if (!response.isSuccess()) {
            throw new NetatmoException(String.format("Unsuccessfull schedule change : %s", response.getStatus()));
        }
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
        String req = "addwebhook?url=" + uri.toString();
        ApiOkResponse response = post(req, null, ApiOkResponse.class, true);
        if (!response.isSuccess()) {
            throw new NetatmoException(String.format("Unsuccessfull schedule change : %s", response.getStatus()));
        }
        return true;
    }
}
