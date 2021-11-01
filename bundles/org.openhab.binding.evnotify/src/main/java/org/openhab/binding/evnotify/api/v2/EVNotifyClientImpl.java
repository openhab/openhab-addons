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
package org.openhab.binding.evnotify.api.v2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.openhab.binding.evnotify.api.ChargingData;
import org.openhab.binding.evnotify.api.EVNotifyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link EVNotifyClient} is responsible for retrieving
 * data via EVNotify V2 API
 *
 * @author Michael Schmidt - Initial contribution
 */
public class EVNotifyClientImpl implements EVNotifyClient {

    public static String BASIC_API_URL_PATTERN = "https://app.evnotify.de/soc?akey=%s&token=%s";
    public static String EXTENDED_API_URL_PATTERN = "https://app.evnotify.de/extended?akey=%s&token=%s";

    private final Logger logger = LoggerFactory.getLogger(EVNotifyClientImpl.class);
    private final HttpClient client;

    private final String aKey;

    private final String token;

    public EVNotifyClientImpl(String aKey, String token, HttpClient client) {
        this.aKey = aKey;
        this.token = token;
        this.client = client;
    }

    @Override
    public ChargingData getCarChargingData() throws IOException, InterruptedException {

        // create the requests
        var basicRequest = HttpRequest.newBuilder(URI.create(String.format(BASIC_API_URL_PATTERN, aKey, token)))
                .header("accept", "application/json").build();

        var extendedRequest = HttpRequest.newBuilder(URI.create(String.format(EXTENDED_API_URL_PATTERN, aKey, token)))
                .header("accept", "application/json").build();

        try {
            // use the client to send the requests
            var basicResponse = client.send(basicRequest, HttpResponse.BodyHandlers.ofString());
            BasicChargingDataDTO basicChargingDataDTO = new Gson().fromJson(basicResponse.body(),
                    BasicChargingDataDTO.class);

            var extendedResponse = client.send(extendedRequest, HttpResponse.BodyHandlers.ofString());
            ExtendedChargingDataDTO extendedChargingData = new Gson().fromJson(extendedResponse.body(),
                    ExtendedChargingDataDTO.class);

            return new ChargingDataDTO(basicChargingDataDTO, extendedChargingData);
        } catch (Exception e) {
            logger.error("Could not retrieve state of car", e);
            throw e;
        }
    }
}
