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
package org.openhab.binding.nanoleaf.internal;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
@NonNullByDefault
public class OpenAPIUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIUtils.class);

    // Regular expression for firmware version
    private static final Pattern FIRMWARE_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final Pattern FIRMWARE_VERSION_PATTERN_BETA = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)-(\\d+)");

    public static Request requestBuilder(HttpClient httpClient, NanoleafControllerConfig controllerConfig,
            String apiOperation, HttpMethod method) throws NanoleafException {
        URI requestURI = getUri(controllerConfig, apiOperation, null);
        LOGGER.trace("RequestBuilder: Sending Request {}:{} {} ", requestURI.getHost(), requestURI.getPort(),
                requestURI.getPath());

        return httpClient.newRequest(requestURI).method(method);
    }

    public static URI getUri(NanoleafControllerConfig controllerConfig, String apiOperation, @Nullable String query)
            throws NanoleafException {
        String path;

        // get network settings from configuration
        String address = controllerConfig.address;
        int port = controllerConfig.port;

        if (API_ADD_USER.equals(apiOperation)) {
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
            requestURI = new URI(HttpScheme.HTTP.asString(), null, address, port, path, query, null);
        } catch (URISyntaxException use) {
            LOGGER.warn("URI could not be parsed with path {}", path);
            throw new NanoleafException("Wrong URI format for API request");
        }
        return requestURI;
    }

    public static ContentResponse sendOpenAPIRequest(Request request) throws NanoleafException {
        try {
            traceSendRequest(request);
            ContentResponse openAPIResponse;
            openAPIResponse = request.send();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("API response from Nanoleaf controller: {}", openAPIResponse.getContentAsString());
            }
            LOGGER.debug("API response code: {}", openAPIResponse.getStatus());
            int responseStatus = openAPIResponse.getStatus();
            if (responseStatus == HttpStatus.OK_200 || responseStatus == HttpStatus.NO_CONTENT_204) {
                return openAPIResponse;
            } else {
                if (openAPIResponse.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                    throw new NanoleafUnauthorizedException("OpenAPI request unauthorized");
                } else if (openAPIResponse.getStatus() == HttpStatus.NOT_FOUND_404) {
                    throw new NanoleafNotFoundException("OpenAPI request did not get any result back");
                } else if (openAPIResponse.getStatus() == HttpStatus.BAD_REQUEST_400) {
                    throw new NanoleafBadRequestException(
                            String.format("Nanoleaf did not expect this request. HTTP response code %s",
                                    openAPIResponse.getStatus()));
                } else {
                    throw new NanoleafException(String.format("OpenAPI request failed. HTTP response code %s",
                            openAPIResponse.getStatus()));
                }
            }
        } catch (ExecutionException | TimeoutException clientException) {
            if (clientException.getCause() instanceof HttpResponseException
                    && ((HttpResponseException) clientException.getCause()).getResponse()
                            .getStatus() == HttpStatus.UNAUTHORIZED_401) {
                LOGGER.warn("OpenAPI request unauthorized. Invalid authorization token.");
                throw new NanoleafUnauthorizedException("Invalid authorization token");
            }
            throw new NanoleafException("Failed to send OpenAPI request", clientException);
        } catch (InterruptedException interruptedException) {
            throw new NanoleafInterruptedException("OpenAPI request has been interrupted", interruptedException);
        }
    }

    private static void traceSendRequest(Request request) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        LOGGER.trace("Sending Request {} {}", request.getURI(),
                request.getQuery() == null ? "no query parameters" : request.getQuery());
        LOGGER.trace("Request method:{} uri:{} params{}\n", request.getMethod(), request.getURI(), request.getParams());
        if (request.getContent() != null) {
            Iterator<ByteBuffer> iter = request.getContent().iterator();
            if (iter != null) {
                while (iter.hasNext()) {
                    @Nullable
                    ByteBuffer buffer = iter.next();
                    LOGGER.trace("Content {}", StandardCharsets.UTF_8.decode(buffer).toString());
                }
            }
        }
    }

    public static boolean checkRequiredFirmware(@Nullable String modelId, @Nullable String currentFirmwareVersion) {
        if (modelId == null || currentFirmwareVersion == null) {
            return false;
        }
        int[] currentVer = getFirmwareVersionNumbers(currentFirmwareVersion);

        int[] requiredVer = getFirmwareVersionNumbers(
                MODEL_ID_LIGHTPANELS.equals(modelId) ? API_MIN_FW_VER_LIGHTPANELS : API_MIN_FW_VER_CANVAS);

        for (int i = 0; i < currentVer.length; i++) {
            if (currentVer[i] != requiredVer[i]) {
                return currentVer[i] > requiredVer[i];
            }
        }
        return true;
    }

    public static int[] getFirmwareVersionNumbers(String firmwareVersion) throws IllegalArgumentException {
        LOGGER.debug("firmwareVersion: {}", firmwareVersion);
        Matcher m = FIRMWARE_VERSION_PATTERN.matcher(firmwareVersion);

        if (m.matches()) {
            return new int[] { Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)) };
        } else {
            m = FIRMWARE_VERSION_PATTERN_BETA.matcher(firmwareVersion);
            if (m.matches()) {
                return new int[] { Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)) };
            } else {
                throw new IllegalArgumentException("Malformed controller firmware version " + firmwareVersion);
            }
        }
    }
}
