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
package org.openhab.binding.nanoleaf.internal;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.API_ADD_USER;
import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.API_V1_BASE_URL;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
    private static final Pattern FIRMWARE_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final Pattern FIRMWARE_VERSION_PATTERN_BETA = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)-(\\d+)");
    private static final long CONNECT_TIMEOUT = 10L;

    public static Request requestBuilder(HttpClient httpClient, NanoleafControllerConfig controllerConfig,
            String apiOperation, HttpMethod method) throws NanoleafException {
        URI requestURI = getUri(controllerConfig, apiOperation, null);
        LOGGER.trace("RequestBuilder: Sending Request {}:{} {} \n op: {}  method: {}", new Object[] {
                requestURI.getHost(), requestURI.getPort(), requestURI.getPath(), apiOperation, method.toString() });
        return httpClient.newRequest(requestURI).method(method).timeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
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
            if (authToken == null) {
                throw new NanoleafUnauthorizedException("No authentication token found in configuration");
            }

            path = String.format("%s/%s%s", API_V1_BASE_URL, authToken, apiOperation);
        }

        try {
            return new URI(HttpScheme.HTTP.asString(), (String) null, address, port, path, query, (String) null);
        } catch (URISyntaxException var8) {
            LOGGER.warn("URI could not be parsed with path {}", path);
            throw new NanoleafException("Wrong URI format for API request");
        }
    }

    public static ContentResponse sendOpenAPIRequest(Request request) throws NanoleafException {
        try {
            traceSendRequest(request);
            ContentResponse openAPIResponse = request.send();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("API response from Nanoleaf controller: {}", openAPIResponse.getContentAsString());
            }
            LOGGER.debug("API response code: {}", openAPIResponse.getStatus());
            int responseStatus = openAPIResponse.getStatus();
            if (responseStatus != HttpStatus.OK_200 && responseStatus != HttpStatus.NO_CONTENT_204) {
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
            } else {
                return openAPIResponse;
            }
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof HttpResponseException httpResponseException
                    && httpResponseException.getResponse().getStatus() == HttpStatus.UNAUTHORIZED_401) {
                LOGGER.debug("OpenAPI request unauthorized. Invalid authorization token.");
                throw new NanoleafUnauthorizedException("Invalid authorization token");
            } else {
                throw new NanoleafException("Failed to send OpenAPI request (final)", ee);
            }
        } catch (TimeoutException te) {
            LOGGER.debug("OpenAPI request failed with timeout", te);
            throw new NanoleafException("Failed to send OpenAPI request: Timeout", te);
        } catch (InterruptedException ie) {
            throw new NanoleafInterruptedException("OpenAPI request has been interrupted", ie);
        }
    }

    private static void traceSendRequest(Request request) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Sending Request {} {}", request.getURI(),
                    request.getQuery() == null ? "no query parameters" : request.getQuery());
            LOGGER.trace("Request method:{} uri:{} params{}\n", request.getMethod(), request.getURI(),
                    request.getParams());
            if (request.getContent() != null) {
                Iterator<ByteBuffer> iter = request.getContent().iterator();
                while (iter.hasNext()) {
                    ByteBuffer buffer = iter.next();
                    LOGGER.trace("Content {}", StandardCharsets.UTF_8.decode(buffer).toString());
                }
            }

        }
    }

    public static boolean checkRequiredFirmware(@Nullable String modelId, @Nullable String currentFirmwareVersion) {
        if (modelId != null && currentFirmwareVersion != null) {
            int[] currentVer = getFirmwareVersionNumbers(currentFirmwareVersion);
            int[] requiredVer = getFirmwareVersionNumbers("NL22".equals(modelId) ? "1.5.0" : "1.1.0");

            for (int i = 0; i < currentVer.length; ++i) {
                if (currentVer[i] != requiredVer[i]) {
                    return (currentVer[i] > requiredVer[i]);
                }
            }

            return true;
        } else {
            return false;
        }
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
