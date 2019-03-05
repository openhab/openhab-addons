/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.nanoleaf.internal.config.NanoleafControllerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenAPIUtils} offers helper methods to support API communication with the controller
 *
 * @author Martin Raepple - Initial contribution
 */
public class OpenAPIUtils {

    private final static Logger logger = LoggerFactory.getLogger(OpenAPIUtils.class);

    // Regular expression for firmware version
    private static final Pattern FIRMWARE_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

    private static final int[] requiredVer = getFirmwareVersionNumbers(NanoleafBindingConstants.API_MIN_FW_VER);

    public static Request requestBuilder(HttpClient httpClient, NanoleafControllerConfig controllerConfig,
            String apiOperation, HttpMethod method) throws NanoleafException, NanoleafUnauthorizedException {
        String path;

        // get network settings from configuration
        String address = controllerConfig.address;
        int port = controllerConfig.port;

        if (apiOperation.equals(API_ADD_USER)) {
            path = String.format("%s%s", API_V1_BASE_URL, apiOperation);
        } else {
            String authToken = controllerConfig.authToken;
            if (authToken != null) {
                path = String.format("%s/%s%s", API_V1_BASE_URL, authToken, apiOperation);
            } else {
                throw new NanoleafUnauthorizedException("No authentication token found in configuration");
            }
        }
        URI requestURI;
        try {
            requestURI = new URI(HttpScheme.HTTP.asString(), null, address, port, path, null, null);
        } catch (URISyntaxException use) {
            logger.warn(String.format("URI could not be parsed with path %s", path));
            throw new NanoleafException("Wrong URI format for API request");
        }
        return httpClient.newRequest(requestURI).method(method);
    }

    public static ContentResponse sendOpenAPIRequest(Request request)
            throws NanoleafException, NanoleafUnauthorizedException {
        try {
            ContentResponse openAPIResponse = request.send();
            if (logger.isTraceEnabled()) {
                logger.trace("API response from Nanoleaf controller: {}", openAPIResponse.getContentAsString());
            }
            logger.debug("API response code: {}", openAPIResponse.getStatus());
            int responseStatus = openAPIResponse.getStatus();
            if (responseStatus == HttpStatus.OK_200 || responseStatus == HttpStatus.NO_CONTENT_204) {
                return openAPIResponse;
            } else {
                if (openAPIResponse.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                    throw new NanoleafUnauthorizedException("OpenAPI request unauthorized");
                } else {
                    throw new NanoleafException(String.format("OpenAPI request failed. HTTP response code %s",
                            openAPIResponse.getStatus()));
                }
            }
        } catch (ExecutionException | TimeoutException | InterruptedException clientException) {
            if (clientException.getCause() instanceof HttpResponseException) {
                if (((HttpResponseException) clientException.getCause()).getResponse()
                        .getStatus() == HttpStatus.UNAUTHORIZED_401) {
                    logger.warn("OpenAPI request unauthorized. Invalid authorization token.");
                    throw new NanoleafUnauthorizedException("Invalid authorization token");
                }
            }
            throw new NanoleafException(
                    String.format("Failed to send OpenAPI request: %s", clientException.getMessage()));
        }
    }

    public static boolean checkRequiredFirmware(String currentFirmwareVersion) {
        int[] currentVer = getFirmwareVersionNumbers(currentFirmwareVersion);

        for (int i = 0; i < currentVer.length; i++) {
            if (currentVer[i] != requiredVer[i]) {
                return currentVer[i] > requiredVer[i];
            }
        }
        return true;
    }

    private static int[] getFirmwareVersionNumbers(String firmwareVersion) throws IllegalArgumentException {
        Matcher m = FIRMWARE_VERSION_PATTERN.matcher(firmwareVersion);
        if (!m.matches()) {
            throw new IllegalArgumentException("Malformed controller firmware version");
        }
        return new int[] { Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)) };
    }
}
