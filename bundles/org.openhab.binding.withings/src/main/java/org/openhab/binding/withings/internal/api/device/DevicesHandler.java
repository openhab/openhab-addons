/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.api.device;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.withings.internal.api.AbstractAPIHandler;
import org.openhab.binding.withings.internal.service.AccessTokenService;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class DevicesHandler extends AbstractAPIHandler {

    private static final String USER_API_URL = "https://wbsapi.withings.net/v2/user";

    public DevicesHandler(AccessTokenService accessTokenService, HttpClient httpClient) {
        super(accessTokenService, httpClient);
    }

    public List<DevicesResponseDTO.Device> loadDevices() {
        Optional<DevicesResponseDTO> devicesResponse = executePOSTRequest(USER_API_URL, "getdevice", new HashMap<>(),
                DevicesResponseDTO.class);
        if (devicesResponse.isPresent()) {
            DevicesResponseDTO.DevicesBody body = devicesResponse.get().getBody();
            if (body != null) {
                List<DevicesResponseDTO.Device> devices = body.getDevices();
                if (devices != null) {
                    return devices;
                }
            }
        }
        return Collections.emptyList();
    }
}
