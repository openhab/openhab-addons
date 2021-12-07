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

import java.util.Collection;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHome.NAHomesDataResponse;

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

    public Collection<NAHome> getHomeData(String homeId) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder().path(SPATH_GETHOME);
        uriBuilder.queryParam(PARM_HOMEID, homeId);
        return get(uriBuilder, NAHomesDataResponse.class).getBody().getElements();
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
        return response.getBody().getElements();
    }

}
