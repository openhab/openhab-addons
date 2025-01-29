/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.discovery;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.hdpowerview.internal.dto.UserData;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Info;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This helper class fetches the hub serial number so it can be used as the representation property
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component
public class SerialNumberHelper {

    private static final String GEN12_URL_FORMAT = "http://%s/api/userdata";
    private static final String GEN3_URL_FORMAT = "http://%s/gateway/info";

    private final Gson gsonParser = new Gson();
    private final HttpClientFactory httpClientFactory;

    @Activate
    public SerialNumberHelper(@Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * Get the serial number of the hub / gateway on the given IPv4 address
     *
     * @param ipAddress a dotted ipv4 address
     * @param generation the hub / gateway generation
     * @return the serial number
     */
    public String getSerialNumber(String ipAddress, int generation) {
        try {
            HttpClient httpClient = httpClientFactory.getCommonHttpClient();
            String uri = String.format(generation < 3 ? GEN12_URL_FORMAT : GEN3_URL_FORMAT, ipAddress);
            ContentResponse content = httpClient.GET(uri);
            if (HttpStatus.OK_200 == content.getStatus()) {
                String json = content.getContentAsString();
                switch (generation) {
                    case 1, 2:
                        UserData userData = gsonParser.fromJson(json, UserData.class);
                        if (userData != null && userData.serialNumber instanceof String serialNumber) {
                            return serialNumber;
                        }
                        break;

                    case 3:
                        Info info = gsonParser.fromJson(json, Info.class);
                        if (info != null && info.getSerialNumber() instanceof String serialNumber) {
                            return serialNumber;
                        }
                        break;
                }
            }
        } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
        }
        // fall back to ip address
        return ipAddress;
    }
}
