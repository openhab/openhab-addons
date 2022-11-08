/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.openhab.binding.icloud.internal.json.request.ICloudFindMyDeviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * TODO
 *
 * @author Simon Spielmann
 */
public class FindMyIPhoneServiceManager {

    private ICloudSession session;

    private URI fmipRefreshUrl;

    private URI fmipSoundUrl;

    private final static String FMIP_ENDPOINT = "/fmipservice/client/web";

    private final Gson gson = new GsonBuilder().create();

    private final static Logger LOGGER = LoggerFactory.getLogger(FindMyIPhoneServiceManager.class);

    public FindMyIPhoneServiceManager(ICloudSession session, String serviceRoot, boolean withFamily)
            throws IOException, InterruptedException {

        this.session = session;
        this.fmipRefreshUrl = URI.create(serviceRoot + FMIP_ENDPOINT + "/refreshClient");
        this.fmipSoundUrl = URI.create(serviceRoot + FMIP_ENDPOINT + "/playSound");
    }

    /**
     * @throws InterruptedException
     * @throws IOException
     *
     */
    public String refreshClient() throws IOException, InterruptedException {

        Map localdata = Map.of("clientContext",
                Map.of("fmly", true, "shouldLocate", true, "selectedDevice", "All", "deviceListVersion", 1));

        return this.session.post(this.fmipRefreshUrl.toString(), this.gson.toJson(localdata), null);
    }

    public void playSound(String deviceId) throws IOException, InterruptedException {

        this.session.post(this.fmipSoundUrl.toString(), this.gson.toJson(new ICloudFindMyDeviceRequest(deviceId)),
                null);
    }
}
