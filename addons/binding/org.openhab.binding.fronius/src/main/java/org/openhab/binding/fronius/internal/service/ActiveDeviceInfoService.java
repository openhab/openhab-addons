/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal.service;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.fronius.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.configuration.ServiceConfiguration;
import org.openhab.binding.fronius.internal.model.ActiveDeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Service object for {@link ActiveDeviceInfoService}.
 *
 * @author Gerrit Beine
 */
public class ActiveDeviceInfoService {

    private final Logger logger = LoggerFactory.getLogger(ActiveDeviceInfoService.class);
    private final String API = FroniusBindingConstants.ACTIVE_DEVICE_INFO_URL;
    private final ServiceConfiguration configuration;
    private final JsonParser parser = new JsonParser();

    private String url;

    public ActiveDeviceInfoService(ServiceConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    public ActiveDeviceInfo getData() {
        final String url = getUrl();
        final HttpClient httpClient = new HttpClient();
        JsonObject json = new JsonObject();
        try {
            httpClient.start();
            ContentResponse response = httpClient.GET(url);
            logger.debug("Response data {}", response.toString());
            httpClient.stop();
            final String jsonString = response.getContentAsString();
            json = parser.parse(jsonString).getAsJsonObject();
        } catch (Exception e) {
            logger.warn("Error during HTTP request: {}", e);
        }
        return new ActiveDeviceInfo(json);
    }

    private String getUrl() {
        if (null == url) {
            StringBuilder sb = new StringBuilder();
            sb.append("http://");
            sb.append(configuration.getHostname());
            sb.append(API);
            sb.append("?DeviceClass=System");
            url = sb.toString();
        }
        logger.debug("ActiveDeviceInfo URL: {}", url);
        return url;
    }
}
