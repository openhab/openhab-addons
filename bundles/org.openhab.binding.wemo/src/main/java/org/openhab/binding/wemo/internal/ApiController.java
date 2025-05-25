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
package org.openhab.binding.wemo.internal;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.wemo.internal.exception.WemoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiController} is responsible for interacting with WeMo devices.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ApiController {

    private static final String HTTP_CALL_CONTENT_HEADER = "text/xml; charset=utf-8";
    private static final int PORT_RANGE_START = 49151;
    private static final int PORT_RANGE_END = 49157;
    private static final int HTTP_TIMEOUT_MS = 1000;

    private final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private final HttpClient httpClient;

    private @Nullable Integer lastPort;

    public ApiController(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String probeAndExecuteCall(String host, @Nullable Integer portFromService, String actionService,
            String soapHeader, String soapBody) throws InterruptedException, WemoException {
        // Build prioritized list of ports to try
        Set<Integer> portsToCheck = new LinkedHashSet<>();

        // Last known working port
        Integer lastPort = this.lastPort;
        if (lastPort != null) {
            portsToCheck.add(lastPort);
        }

        // Port announced via UPnP service
        if (portFromService != null) {
            portsToCheck.add(portFromService);
        }

        // Add remaining ports from the defined range
        for (int port = PORT_RANGE_START; port <= PORT_RANGE_END; port++) {
            portsToCheck.add(port);
        }

        for (Integer port : portsToCheck) {
            String wemoURL = "http://" + host + ":" + port + "/upnp/control/" + actionService + "1";
            try {
                String response = executeCall(wemoURL, soapHeader, soapBody);
                this.lastPort = port;
                return response;
            } catch (WemoException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    logger.debug("Request for {} failed: {}", wemoURL, e.getMessage());
                } else {
                    logger.debug("Request for {} failed: {} -> {}", wemoURL, e.getMessage(), cause.getMessage());
                }
            }
        }

        this.lastPort = null;

        String attemptedPorts = portsToCheck.stream().map(String::valueOf).collect(Collectors.joining(", "));

        throw new WemoException(
                "Failed to connect to device. All attempts for " + host + " on ports [" + attemptedPorts + "] failed.");
    }

    public String executeCall(String wemoURL, String soapHeader, String soapBody)
            throws InterruptedException, WemoException {
        Request request = httpClient.newRequest(wemoURL).timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .header(HttpHeader.CONTENT_TYPE, HTTP_CALL_CONTENT_HEADER).header("SOAPACTION", soapHeader)
                .method(HttpMethod.POST).content(new StringContentProvider(soapBody));

        logger.trace("POST request for URL: '{}', header: '{}', request body: '{}'", wemoURL, soapHeader, soapBody);

        try {
            ContentResponse response = request.send();

            String responseContent = response.getContentAsString();
            logger.trace("Response content: '{}'", responseContent);

            int status = response.getStatus();
            if (!HttpStatus.isSuccess(status)) {
                throw new WemoException("The request failed with HTTP error " + status, status);
            }

            return responseContent;
        } catch (TimeoutException e) {
            throw new WemoException("The HTTP request timed out", e);
        } catch (ExecutionException e) {
            throw new WemoException("The HTTP request failed", e);
        }
    }
}
