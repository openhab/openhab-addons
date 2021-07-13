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
import java.util.Collection;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class SecurityApi extends RestManager {
    public SecurityApi(ApiBridge apiClient) { // NO_UCD (unused code)
        super(apiClient, FeatureArea.SECURITY);
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
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_DROPWEBHOOK);
        post(uriBuilder, ApiResponse.Ok.class, null);
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
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_ADDWEBHOOK);
        uriBuilder.queryParam(PARM_URL, uri.toString());
        post(uriBuilder, ApiResponse.Ok.class, null);
        return true;
    }

    private class NALastEventsDataResponse extends ApiResponse<ListBodyResponse<NAHomeEvent>> {
    }

    public Collection<NAHomeEvent> getLastEventsOf(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_GETLASTEVENT);
        uriBuilder.queryParam(PARM_HOMEID, homeId);
        uriBuilder.queryParam(PARM_PERSONID, personId);
        NALastEventsDataResponse response = get(uriBuilder, NALastEventsDataResponse.class);
        return response.getBody().getElementsCollection();
    }
}
