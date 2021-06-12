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
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.PresenceLightMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeSecurity;
import org.openhab.binding.netatmo.internal.api.dto.NAPing;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeApi extends RestManager {

    public HomeApi(ApiBridge apiClient) {
        super(apiClient);
    }

    public class NAHomesDataResponse extends ApiResponse<ListBodyResponse<NAHome>> {
    }

    // public Collection<NAHome> getHomeData(String homeId) throws NetatmoException {
    // UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_GETHOME);
    // uriBuilder.queryParam(PARM_MODULEID, homeId);
    // return get(uriBuilder, NAHomesDataResponse.class).getBody().getElementsCollection();
    // }

    // Not sure if I will keep this...
    public Collection<NAHome> getHomes(@Nullable String homeId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_GETHOME);
        ListBodyResponse<NAHome> response = get(uriBuilder, NAHomesDataResponse.class).getBody();
        Collection<NAHome> homeListResponse = getHomeList(null, null);
        Collection<NAHome> homeDataResponse = response.getElementsCollection();
        homeListResponse.forEach(h1 -> {
            homeDataResponse.stream().filter(h2 -> h2.getId().equals(h1.getId())).findFirst().ifPresent(h2 -> {
                h1.setPlace(h2.getPlace());
                h1.getModules().putAll(h2.getModules());
                if (h1 instanceof NAHomeSecurity && h2 instanceof NAHomeSecurity) {
                    ((NAHomeSecurity) h1).getCameras().putAll(((NAHomeSecurity) h2).getCameras());
                    ((NAHomeSecurity) h1).getPersons().putAll(((NAHomeSecurity) h2).getPersons());
                }
            });
        });
        // Complete gethomedata with informations provided by homesdata

        if (homeId != null) {
            homeListResponse.removeIf(home -> !home.getId().equals(homeId));
        }

        return homeListResponse;
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
