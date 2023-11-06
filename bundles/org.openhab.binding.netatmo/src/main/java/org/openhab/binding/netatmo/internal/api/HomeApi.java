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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.NAHomeStatusResponse;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;

/**
 * The {@link HomeApi} handles general API endpoints not requiring specific scope area
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeApi extends RestManager {

    public HomeApi(ApiBridgeHandler apiClient) {
        super(apiClient, FeatureArea.NONE);
    }

    public Optional<NAHomeStatus> getHomeStatus(String homeId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_HOMESTATUS, PARAM_HOME_ID, homeId);

        return Optional.ofNullable(get(uriBuilder, NAHomeStatusResponse.class).getBody());
    }

    public @Nullable HomeData getHomeData(String homeId) throws NetatmoException {
        Collection<HomeData> result = getHomesData(homeId, null);
        return result.isEmpty() ? null : result.iterator().next();
    }

    public Collection<HomeData> getHomesData(@Nullable String homeId, @Nullable ModuleType type)
            throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_HOMES_DATA, PARAM_HOME_ID, homeId);

        if (type != null) {
            uriBuilder.queryParam(PARAM_GATEWAY_TYPE, type.name());
        }

        HomeData.HomesDataResponse response = get(uriBuilder, HomeData.HomesDataResponse.class);
        ListBodyResponse<HomeData> body = response.getBody();
        return body != null ? body.getElements() : Set.of();
    }
}
