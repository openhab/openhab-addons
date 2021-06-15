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

import java.util.Collection;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.PresenceLightMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAPing;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeApi extends RestManager {

    public HomeApi(ApiBridge apiClient) {
        super(apiClient, FeatureArea.NONE);
    }

    public class NAHomesDataResponse extends ApiResponse<ListBodyResponse<NAHome>> {
    }

    public Collection<NAHome> getHomeData(String homeId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_GETHOME);
        uriBuilder.queryParam(PARM_MODULEID, homeId);
        return get(uriBuilder, NAHomesDataResponse.class).getBody().getElementsCollection();
    }

    public Collection<NAHome> getHomeList(@Nullable String homeId, @Nullable ModuleType type) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_HOMES);

        if (homeId != null) {
            uriBuilder.queryParam(PARM_HOMEID, homeId);
        }
        if (type != null) {
            uriBuilder.queryParam(PARM_GATEWAYTYPE, type.name());
        }

        NAHomesDataResponse response = get(uriBuilder, NAHomesDataResponse.class);
        return response.getBody().getElementsCollection();
    }

    public boolean setpersonsaway(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder().path(SPATH_PERSON_AWAY);
        String payload = String.format("{\"home_id\":\"%s\",\"person_id\":\"%s\"}", homeId, personId);
        post(uriBuilder, ApiResponse.Ok.class, payload);
        return true;
    }

    public boolean setpersonshome(String homeId, String personId) throws NetatmoException {
        UriBuilder uriBuilder = getAppUriBuilder().path(SPATH_PERSON_HOME);
        String payload = String.format("{\"home_id\":\"%s\",\"person_ids\":[\"%s\"]}", homeId, personId);
        post(uriBuilder, ApiResponse.Ok.class, payload);
        return true;
    }

    public String ping(String vpnUrl) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(vpnUrl).path(PATH_COMMAND).path("ping");
        NAPing response = get(uriBuilder, NAPing.class);
        return response.getStatus();
    }

    public boolean changeStatus(String localCameraURL, boolean setOn) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(localCameraURL).path(PATH_COMMAND).path(PARM_CHANGESTATUS);
        uriBuilder.queryParam("status", setOn ? "on" : "off");
        post(uriBuilder, ApiResponse.Ok.class, null);
        return true;
    }

    public boolean changeFloodLightMode(String localCameraURL, PresenceLightMode mode) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(localCameraURL).path(PATH_COMMAND).path(PARM_FLOODLIGHTSET);
        uriBuilder.queryParam("config", "%7B%22mode%22:%22" + mode.toString() + "%22%7D");
        get(uriBuilder, ApiResponse.Ok.class);
        return true;
    }
}
