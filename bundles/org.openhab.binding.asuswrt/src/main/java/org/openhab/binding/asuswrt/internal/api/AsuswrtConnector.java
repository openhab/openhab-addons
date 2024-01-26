/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.asuswrt.internal.api;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtErrorConstants.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.getValueOrDefault;

import java.net.NoRouteToHostException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtConfiguration;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtCredentials;
import org.openhab.binding.asuswrt.internal.things.AsuswrtRouter;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtConnector} is a {@link AsuswrtHttpClient} that also keeps track of router configuration and
 * credentials.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtConnector extends AsuswrtHttpClient {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtConnector.class);
    private AsuswrtCredentials credentials;
    private AsuswrtConfiguration routerConfig;
    protected Long lastQuery = 0L;

    public AsuswrtConnector(AsuswrtRouter router) {
        super(router);
        routerConfig = router.getConfiguration();
        this.credentials = new AsuswrtCredentials(routerConfig);
    }

    /*
     * Connector commands
     */

    /**
     * Login to the router.
     */
    public Boolean login() {
        String url = getURL("login.cgi");
        String encodedCredentials = credentials.getEncodedCredentials();
        String payload = "";

        logout(); // logout (unset cookie) first
        router.errorHandler.reset();

        logger.trace("({}) perform login to '{}' with '{}'", uid, url, encodedCredentials);

        payload = "login_authorization=" + encodedCredentials + "}";
        ContentResponse response = getSyncRequest(url, payload);
        if (response != null) {
            setCookieFromResponse(response);
        }
        if (cookieStore.isValid()) {
            router.setState(ThingStatus.ONLINE);
            return true;
        }
        return false;
    }

    /**
     * Logout and unsets the cookie.
     */
    public void logout() {
        this.cookieStore.resetCookie();
    }

    /**
     * Gets system information from the device.
     */
    public void querySysInfo(boolean asyncRequest) {
        queryDeviceData(CMD_GET_SYSINFO, asyncRequest);
    }

    /**
     * Queries data from the device.
     *
     * @param command command constant to sent
     * @param asyncRequest <code>true</code> if request should be sent asynchronous, <code>false</code> if synchronous
     */
    public void queryDeviceData(String command, boolean asyncRequest) {
        logger.trace("({}) queryDeviceData", uid);
        Long now = System.currentTimeMillis();

        router.errorHandler.reset();
        if (cookieStore.cookieIsExpired()) {
            login();
        }

        if (now > this.lastQuery + HTTP_QUERY_MIN_GAP_MS) {
            String url = getURL("appGet.cgi");
            String payload = "hook=" + command;
            this.lastQuery = now;

            // Send payload as url parameter
            url = url + "?" + payload;
            url = url.replace(";", "%3B");

            // Send asynchronous or synchronous HTTP request
            if (asyncRequest) {
                sendAsyncRequest(url, payload, command);
            } else {
                sendSyncRequest(url, payload, command);
            }
        } else {
            logger.trace("({}) query skipped cause of min_gap: {} <- {}", uid, now, lastQuery);
        }
    }

    /*
     * Response handling
     */

    /**
     * Handle successful HTTP response by delegating to the connector class.
     *
     * @param responseBody response body as string
     * @param command command constant which was sent
     */
    @Override
    protected void handleHttpSuccessResponse(String responseBody, String command) {
        JsonObject jsonObject = getJsonFromString(responseBody);
        router.dataReceived(jsonObject, command);
    }

    /**
     * Handles HTTP result failures.
     *
     * @param e Throwable exception
     * @param payload full payload for debugging
     */
    @Override
    protected void handleHttpResultError(Throwable e, String payload) {
        super.handleHttpResultError(e, payload);
        String errorMessage = getValueOrDefault(e.getMessage(), "");

        if (e instanceof TimeoutException || e instanceof NoRouteToHostException) {
            router.errorHandler.raiseError(ERR_CONN_TIMEOUT, errorMessage);
            router.setState(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        } else if (e instanceof SSLException || e instanceof SSLKeyException || e instanceof SSLHandshakeException) {
            router.errorHandler.raiseError(ERR_SSL_EXCEPTION, payload);
            router.setState(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        } else if (e instanceof InterruptedException) {
            router.errorHandler.raiseError(new Exception(e), payload);
            router.setState(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        } else {
            router.errorHandler.raiseError(new Exception(e), errorMessage);
            router.setState(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
    }

    /*
     * Other
     */

    /**
     * Gets the target URL.
     */
    protected String getURL(String site) {
        String url = routerConfig.hostname;
        if (routerConfig.useSSL) {
            url = HTTPS_PROTOCOL + url;
            if (routerConfig.httpsPort != 443) {
                url = url + ":" + routerConfig.httpsPort;
            }
        } else {
            url = HTTP_PROTOCOL + url;
            if (routerConfig.httpPort != 80) {
                url = url + ":" + routerConfig.httpPort;
            }
        }
        return url + "/" + site;
    }
}
