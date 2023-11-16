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
package org.openhab.binding.fsinternetradio.internal.radio;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds the http-connection and session information for controlling the radio.
 *
 * @author Rainer Ostendorf
 * @author Patrick Koenemann
 * @author Svilen Valkanov - replaced Apache HttpClient with Jetty
 * @author Mihaela Memova - changed the calling of the stopHttpClient() method, fixed the hardcoded URL path, fixed the
 *         for loop condition part
 */
public class FrontierSiliconRadioConnection {

    private final Logger logger = LoggerFactory.getLogger(FrontierSiliconRadioConnection.class);

    /** Timeout for HTTP requests in ms */
    private static final int SOCKET_TIMEOUT = 5000;

    /** Hostname of the radio. */
    private final String hostname;

    /** Port number, usually 80. */
    private final int port;

    /** Access pin, passed upon login as GET parameter. */
    private final String pin;

    /** The session ID we get from the radio after logging in. */
    private String sessionId;

    /** http clients, store cookies, so it is kept in connection class. */
    private HttpClient httpClient = null;

    /** Flag indicating if we are successfully logged in. */
    private boolean isLoggedIn = false;

    public FrontierSiliconRadioConnection(String hostname, int port, String pin, HttpClient httpClient) {
        this.hostname = hostname;
        this.port = port;
        this.pin = pin;
        this.httpClient = httpClient;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    /**
     * Perform login/establish a new session. Uses the PIN number and when successful saves the assigned sessionID for
     * future requests.
     *
     * @return <code>true</code> if login was successful; <code>false</code> otherwise.
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public boolean doLogin() throws IOException {
        isLoggedIn = false; // reset login flag

        final String url = "http://" + hostname + ":" + port + FrontierSiliconRadioConstants.CONNECTION_PATH
                + "/CREATE_SESSION?pin=" + pin;

        logger.trace("opening URL: {}", url);

        Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(SOCKET_TIMEOUT,
                TimeUnit.MILLISECONDS);

        try {
            ContentResponse response = request.send();
            int statusCode = response.getStatus();
            if (statusCode != HttpStatus.OK_200) {
                String reason = response.getReason();
                logger.debug("Communication with radio failed: {} {}", statusCode, reason);
                if (statusCode == HttpStatus.FORBIDDEN_403) {
                    throw new IllegalStateException("Radio does not allow connection, maybe wrong pin?");
                }
                throw new IOException("Communication with radio failed, return code: " + statusCode);
            }

            final String responseBody = response.getContentAsString();
            if (!responseBody.isEmpty()) {
                logger.trace("login response: {}", responseBody);
            }

            final FrontierSiliconRadioApiResult result = new FrontierSiliconRadioApiResult(responseBody);
            if (result.isStatusOk()) {
                logger.trace("login successful");
                sessionId = result.getSessionId();
                isLoggedIn = true;
                return true; // login successful :-)
            }

        } catch (Exception e) {
            logger.debug("Fatal transport error: {}", e.toString());
            throw new IOException(e);
        }

        return false; // login not successful
    }

    /**
     * Performs a request to the radio with no further parameters.
     *
     * Typically used for polling state info.
     *
     * @param requestString REST API request, e.g. "GET/netRemote.sys.power"
     * @return request result
     * @throws IOException if the request failed.
     */
    public FrontierSiliconRadioApiResult doRequest(String requestString) throws IOException {
        return doRequest(requestString, null);
    }

    /**
     * Performs a request to the radio with addition parameters.
     *
     * Typically used for changing parameters.
     *
     * @param requestString REST API request, e.g. "SET/netRemote.sys.power"
     * @param params parameters, e.g. "value=1"
     * @return request result
     * @throws IOException if the request failed.
     */
    public FrontierSiliconRadioApiResult doRequest(String requestString, String params) throws IOException {
        // 3 retries upon failure
        for (int i = 0; i < 3; i++) {
            if (!isLoggedIn && !doLogin()) {
                continue; // not logged in and login was not successful - try again!
            }

            final String url = "http://" + hostname + ":" + port + FrontierSiliconRadioConstants.CONNECTION_PATH + "/"
                    + requestString + "?pin=" + pin + "&sid=" + sessionId
                    + (params == null || params.trim().length() == 0 ? "" : "&" + params);

            logger.trace("calling url: '{}'", url);

            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(SOCKET_TIMEOUT,
                    TimeUnit.MILLISECONDS);

            try {
                ContentResponse response = request.send();
                final int statusCode = response.getStatus();
                if (statusCode != HttpStatus.OK_200) {
                    /*-
                     * Issue: https://github.com/eclipse/smarthome/issues/2548
                     * If the session expired, we might get a 404 here. That's ok, remember that we are not logged-in
                     * and try again. Print warning only if this happens in the last iteration.
                     */
                    if (i >= 2) {
                        String reason = response.getReason();
                        logger.warn("Method failed: {}  {}", statusCode, reason);
                    }
                    isLoggedIn = false;
                    continue;
                }

                final String responseBody = response.getContentAsString();
                if (!responseBody.isEmpty()) {
                    logger.trace("got result: {}", responseBody);
                } else {
                    logger.debug("got empty result");
                    isLoggedIn = false;
                    continue;
                }

                final FrontierSiliconRadioApiResult result = new FrontierSiliconRadioApiResult(responseBody);
                if (result.isStatusOk()) {
                    return result;
                }

                isLoggedIn = false;
                continue; // try again
            } catch (Exception e) {
                logger.error("Fatal transport error: {}", e.toString());
                throw new IOException(e);
            }
        }
        isLoggedIn = false; // 3 tries failed. log in again next time, maybe our session went invalid (radio restarted?)
        return null;
    }
}
