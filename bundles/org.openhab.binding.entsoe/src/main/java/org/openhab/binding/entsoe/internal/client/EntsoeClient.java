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
package org.openhab.binding.entsoe.internal.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.entsoe.internal.exception.EntsoeConfigurationException;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseException;
import org.openhab.core.OpenHAB;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miika Jukka - Initial contribution
 * @author Jørgen Melhus - Contribution
 * @author Bernd Weymann - Unit test preparation
 */
@NonNullByDefault
public class EntsoeClient {
    private final Logger logger = LoggerFactory.getLogger(EntsoeClient.class);
    private final HttpClient httpClient;
    private final String userAgent;

    public EntsoeClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        String bundleVersion = "unknown";
        if (FrameworkUtil.getBundle(this.getClass()) != null) {
            bundleVersion = OpenHAB.getVersion();
        }
        userAgent = "openHAB/" + bundleVersion;
    }

    public String doGetRequest(EntsoeRequest entsoeRequest, int timeoutSeconds)
            throws EntsoeResponseException, EntsoeConfigurationException {
        String url = entsoeRequest.toUrl();
        Request request = httpClient.newRequest(url) //
                .timeout(timeoutSeconds, TimeUnit.SECONDS) //
                .agent(userAgent) //
                .method(HttpMethod.GET);

        try {
            logger.debug("Sending GET request with parameters: {}", entsoeRequest);
            ContentResponse response = request.send();
            int status = response.getStatus();
            if (status == HttpStatus.UNAUTHORIZED_401) {
                // This will currently not happen because "WWW-Authenticate" header is missing; see below.
                throw new EntsoeConfigurationException("Authentication failed. Please check your security token");
            }
            if (!HttpStatus.isSuccess(status)) {
                throw new EntsoeResponseException("The request failed with HTTP error " + status);
            }

            String responseContent = response.getContentAsString();
            if (responseContent == null) {
                throw new EntsoeResponseException("Request failed");
            }
            return responseContent;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof HttpResponseException httpResponseException) {
                Response response = httpResponseException.getResponse();
                if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                    /*
                     * The service may respond with HTTP code 401 without any "WWW-Authenticate"
                     * header, violating RFC 7235. Jetty will then throw HttpResponseException.
                     * We need to handle this in order to attempt reauthentication.
                     */
                    throw new EntsoeConfigurationException("Authentication failed. Please check your security token");
                }
            }
            throw new EntsoeResponseException(e);
        } catch (TimeoutException e) {
            throw new EntsoeResponseException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }
}
