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
package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.icloud.internal.utilities.JsonUtils;

/**
 * This class gives access to the find my iPhone (FMIP) service.
 *
 * @author Simon Spielmann - Initial Contribution.
 */
@NonNullByDefault
public class FindMyIPhoneServiceManager {

    private ICloudSession session;

    private URI fmipRefreshUrl;

    private URI fmipSoundUrl;

    private static final String FMIP_ENDPOINT = "/fmipservice/client/web";

    /**
     * The constructor.
     *
     * @param session {@link ICloudSession} to use for API calls.
     * @param serviceRoot Root URL for FMIP service.
     */
    public FindMyIPhoneServiceManager(ICloudSession session, String serviceRoot) {
        this.session = session;
        this.fmipRefreshUrl = URI.create(serviceRoot + FMIP_ENDPOINT + "/refreshClient");
        this.fmipSoundUrl = URI.create(serviceRoot + FMIP_ENDPOINT + "/playSound");
    }

    /**
     * Receive client information as JSON.
     *
     * @return Information about all clients as JSON
     *         {@link org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudDeviceInformation}.
     *
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if this blocking request was interrupted
     * @throws ICloudApiResponseException if the request failed (e.g. not OK HTTP return code)
     *
     */
    public String refreshClient() throws IOException, InterruptedException, ICloudApiResponseException {
        Map<String, Object> request = Map.of("clientContext",
                Map.of("fmly", true, "shouldLocate", true, "selectedDevice", "All", "deviceListVersion", 1));
        return session.post(this.fmipRefreshUrl.toString(), JsonUtils.toJson(request), null);
    }

    /**
     * Play sound (find my iPhone) on given device.
     *
     * @param deviceId ID of the device to play sound on
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if this blocking request was interrupted
     * @throws ICloudApiResponseException if the request failed (e.g. not OK HTTP return code)
     */
    public void playSound(String deviceId) throws IOException, InterruptedException, ICloudApiResponseException {
        Map<String, Object> request = Map.of("device", deviceId, "fmyl", true, "subject", "Message from openHAB.");
        session.post(this.fmipSoundUrl.toString(), JsonUtils.toJson(request), null);
    }
}
