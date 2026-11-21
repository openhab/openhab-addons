/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.robonect.internal.model.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared HTTP transport and parser for Robonect endpoint APIs.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
final class RobonectTransport {
    private final Logger logger = LoggerFactory.getLogger(RobonectTransport.class);
    final String baseUrl;
    final String webBaseUrl;
    final String ajaxUrl;
    private final HttpClient httpClient;

    private static class BasicResult implements Authentication.Result {

        private final HttpHeader header;
        private final URI uri;
        private final String value;

        /**
         * Creates an authentication result.
         *
         * @param header the HTTP header to set
         * @param uri the URI for the authentication result
         * @param value the authentication header value
         */
        public BasicResult(HttpHeader header, URI uri, String value) {
            this.header = header;
            this.uri = uri;
            this.value = value;
        }

        /**
         * Returns the URI associated with this authentication result.
         *
         * @return the authentication URI
         */
        @Override
        public URI getURI() {
            return this.uri;
        }

        /**
         * Applies the authentication header to a request.
         *
         * @param request the request to authenticate
         */
        @Override
        @NonNullByDefault({}) // Jetty's interface leaves the request's nullness unspecified.
        public void apply(Request request) {
            request.header(this.header, this.value);
        }

        /**
         * Describes the authentication result without including the header value.
         *
         * @return a description containing the URI
         */
        @Override
        public String toString() {
            return String.format("Basic authentication result for %s", this.uri);
        }
    }

    /**
     * Creates a transport for the configured endpoint.
     *
     * @param httpClient the shared HTTP client
     * @param endpoint the connection and authentication settings
     */
    RobonectTransport(HttpClient httpClient, RobonectEndpoint endpoint) {
        this.httpClient = httpClient;
        this.baseUrl = "http://" + endpoint.getIpAddress() + "/json";
        this.webBaseUrl = "http://" + endpoint.getIpAddress();
        this.ajaxUrl = "http://" + endpoint.getIpAddress() + "/ajax";
        if (endpoint.isUseAuthentication()) {
            addPreemptiveAuthentication(httpClient, endpoint);
        }
    }

    /**
     * Registers Basic authentication for requests to the endpoint.
     *
     * @param httpClient the HTTP client whose authentication store is updated
     * @param endpoint the endpoint providing the credentials
     */
    private void addPreemptiveAuthentication(HttpClient httpClient, RobonectEndpoint endpoint) {
        AuthenticationStore auth = httpClient.getAuthenticationStore();
        URI uri = URI.create(baseUrl).resolve("/");
        auth.addAuthenticationResult(
                new BasicResult(HttpHeader.AUTHORIZATION, uri, "Basic " + Base64.getEncoder().encodeToString(
                        (endpoint.getUser() + ":" + endpoint.getPassword()).getBytes(StandardCharsets.ISO_8859_1))));
    }

    /**
     * Sends a command to the JSON endpoint.
     *
     * @param command the command to send
     * @return the response body
     */
    String sendCommand(Command command) {
        return sendCommand(command, baseUrl);
    }

    /**
     * Sends a command using a supplied base URL.
     *
     * @param command the command to send
     * @param commandBaseUrl the base URL used by the command
     * @return the response body
     */
    String sendCommand(Command command, String commandBaseUrl) {
        return sendCommand(command, commandBaseUrl, true);
    }

    /**
     * Sends a command with control over response-body logging.
     *
     * @param command the command to send
     * @param commandBaseUrl the base URL used by the command
     * @param logResponseBody whether the response body may be logged
     * @return the response body
     */
    String sendCommand(Command command, String commandBaseUrl, boolean logResponseBody) {
        return sendCommand(command, commandBaseUrl, logResponseBody, false);
    }

    /**
     * Sends a command, suppressing sensitive request details when requested.
     *
     * @param command the command to send
     * @param commandBaseUrl the base URL used by the command
     * @param logResponseBody whether the response body may be logged
     * @param sensitive whether sensitive request details must be suppressed
     * @return the response body
     */
    String sendCommand(Command command, String commandBaseUrl, boolean logResponseBody, boolean sensitive) {
        try {
            if (!sensitive && logger.isDebugEnabled()) {
                logger.debug("send HTTP GET to: {} ", command.toCommandURL(commandBaseUrl));
            }
            ContentResponse response = httpClient.newRequest(command.toCommandURL(commandBaseUrl))
                    .method(HttpMethod.GET).timeout(30000, TimeUnit.MILLISECONDS).send();
            String responseString = null;

            // jetty uses UTF-8 as default encoding. However, HTTP 1.1 specifies ISO_8859_1
            if (response.getEncoding() == null || response.getEncoding().isBlank()) {
                responseString = new String(response.getContent(), StandardCharsets.ISO_8859_1);
            } else {
                // currently v0.9e Robonect does not specifiy the encoding. But if later versions will
                // add, it should work with the default method to get the content as string.
                responseString = response.getContentAsString();
            }

            if (logResponseBody && logger.isDebugEnabled()) {
                logger.debug("Response body was: {} ", responseString);
            }
            return responseString;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (sensitive) {
                throw new RobonectCommunicationException("Could not send command");
            }
            throw new RobonectCommunicationException("Could not send command " + command.toCommandURL(commandBaseUrl),
                    e);
        } catch (ExecutionException | TimeoutException e) {
            if (sensitive) {
                // Transport exceptions can contain the request URL, so do not retain their causes here.
                throw new RobonectCommunicationException("Could not send command");
            }
            throw new RobonectCommunicationException("Could not send command " + command.toCommandURL(commandBaseUrl),
                    e);
        } catch (RuntimeException e) {
            if (sensitive) {
                throw new RobonectCommunicationException("Could not send command");
            }
            throw e;
        }
    }
}
