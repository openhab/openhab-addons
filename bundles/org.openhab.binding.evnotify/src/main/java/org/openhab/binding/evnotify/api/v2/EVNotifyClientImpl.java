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

import org.openhab.binding.evnotify.api.ApiException;
import org.openhab.binding.evnotify.api.ChargingData;
import org.openhab.binding.evnotify.api.EVNotifyClient;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EVNotifyClient} is responsible for retrieving
 * data via EVNotify V2 API
 *
 * @author Michael Schmidt - Initial contribution
 */
public class EVNotifyClientImpl implements EVNotifyClient {

    public static String BASIC_API_URL_PATTERN = "https://app.evnotify.de/soc?akey=%s&token=%s";
    public static String EXTENDED_API_URL_PATTERN = "https://app.evnotify.de/extended?akey=%s&token=%s";

    private final HttpClient client;

    private final String akey;

    private final String token;

    public EVNotifyClientImpl(String akey, String token, HttpClient client) {
        this.akey = akey;
        this.token = token;
        this.client = client;
    }

    @Override
    public ChargingData getCarChargingData() throws IOException, InterruptedException, ApiException {

        // create the requests
        var basicRequest = HttpRequest.newBuilder(URI.create(String.format(BASIC_API_URL_PATTERN, akey, token)))
                .header("accept", "application/json").build();

        var extendedRequest = HttpRequest.newBuilder(URI.create(String.format(EXTENDED_API_URL_PATTERN, akey, token)))
                .header("accept", "application/json").build();

        try {
            // use the client to send the requests
            var basicResponse = client.send(basicRequest, HttpResponse.BodyHandlers.ofString());
            validateResponse(basicResponse);
            BasicChargingDataDTO basicChargingDataDTO = new Gson().fromJson(basicResponse.body(),
                    BasicChargingDataDTO.class);

            var extendedResponse = client.send(extendedRequest, HttpResponse.BodyHandlers.ofString());
            validateResponse(extendedResponse);
            ExtendedChargingDataDTO extendedChargingData = new Gson().fromJson(extendedResponse.body(),
                    ExtendedChargingDataDTO.class);

            return new ChargingDataDTO(basicChargingDataDTO, extendedChargingData);
        } catch (JsonSyntaxException e) {
            throw new ApiException("Request failed with invalid response body", e);
        }
    }

    private void validateResponse(HttpResponse<String> httpResponse) throws ApiException {
        if (httpResponse != null) {

            if (httpResponse.statusCode() >= 400) {
                throw new ApiException(getErrorMessage(httpResponse));
            }

            if (httpResponse.body() == null) {
                throw new ApiException("Request failed with null response body");
            }

        } else {
            throw new ApiException("Response was null");
        }
    }

    private String getErrorMessage(HttpResponse<String> httpResponse) {
        String errorMessage = String.format("Request failed with status %d", httpResponse.statusCode());

        if (httpResponse.body() != null) {
            try {
                ErrorDTO errorDTO = new Gson().fromJson(httpResponse.body(), ErrorDTO.class);

                errorMessage += String.format(" with error code %d and message '%s'", errorDTO.getCode(),
                        errorDTO.getMessage());

            } catch (Exception ignored) {
            }
        }
        return errorMessage;
    }
}
