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
package org.openhab.binding.netatmo.internal.api.partner;

import java.util.List;
import java.util.Set;

import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ApiResponse;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.RestManager;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

public class PartnerApi extends RestManager {

    public PartnerApi(ApiBridge apiClient) {
        super(apiClient, Set.of());
    }

    public class NAPartnerDevicesResponse extends ApiResponse<List<String>> {
    }

    /**
     *
     * The method partnerdevices returns the list of device_id to which your partner application has access to.
     *
     * @return NAPartnerDevicesResponse
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public List<String> getPartnerDevices() throws NetatmoException {
        String req = "partnerdevices";
        NAPartnerDevicesResponse response = get(req, NAPartnerDevicesResponse.class);
        return response.getBody();
    }
}
