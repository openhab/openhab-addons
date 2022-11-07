/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import java.net.NoRouteToHostException;
import java.util.concurrent.TimeoutException;

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
 * ASUSWRT HTTP CLIENT
 * 
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtConnector extends AsuswrtHttpClient {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtConnector.class);
    private AsuswrtCredentials credentials;
    private AsuswrtConfiguration routerConfig;
    protected Long lastQuery = 0L;

    /**
     * INIT CLASS
     * 
     * @param router router object
     */
    public AsuswrtConnector(AsuswrtRouter router) {
        super(router);
        routerConfig = router.getConfiguration();
        this.credentials = new AsuswrtCredentials(routerConfig);
    }

    /***********************************
     *
     * CONNECTOR COMMANDS
     *
     ************************************/

    /**
     * Login to router
     * 
     * @return
     */
    public Boolean login() {
        String url = routerConfig.url + "/login.cgi";
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
        if (isValidLogin()) {
            router.setState(ThingStatus.ONLINE);
            return true;
        }
        return false;
    }

    /**
     * Logout
     * unset cookie
     */
    public void logout() {
        this.cookie = "";
        this.token = "";
        this.cookieTimeStamp = 0L;
    }

    /**
     * Query SysInfo (synchronous)
     * get System Information from device
     */
    public void querySysInfo(Boolean asyncRequest) {
        queryDeviceData(CMD_GET_SYSINFO, asyncRequest);
    }

    /**
     * Query Data From Device asynchron
     * 
     * @param command command constant to sent
     * @param asyncRequest Boolean True if request should be sent asynchron, false if synchron
     */
    public void queryDeviceData(String command, Boolean asyncRequest) {
        logger.trace("({}) queryDeviceData", uid);
        Long now = System.currentTimeMillis();

        router.errorHandler.reset();
        checkAuth();

        if (now > this.lastQuery + HTTP_QUERY_MIN_GAP_MS) {
            String url = routerConfig.url + "/appGet.cgi";
            String payload = "hook=" + command;
            this.lastQuery = now;

            /* send payload as url parameter */
            url = url + "?" + payload;
            url = url.replace(";", "%3B");

            /* send asynchron or synchron http-request */
            if (asyncRequest.equals(true)) {
                sendAsyncRequest(url, payload, command);
            } else {
                sendSyncRequest(url, payload, command);
            }
        } else {
            logger.trace("({}) query skipped cause of min_gap: {} <- {}", uid, now, lastQuery);
        }
    }

    /***********************************
     *
     * HANDLE RESPONSES
     *
     ************************************/

    /**
     * Handle Sucessfull HTTP Response
     * delegated to connector-class
     * 
     * @param rBody response body as string
     * @param command command constant which was sent
     */
    @Override
    protected void handleHttpSuccessResponse(String responseBody, String command) {
        JsonObject jsonObject = getJsonFromString(responseBody);
        router.dataReceived(jsonObject, command);
    }

    /**
     * Handle HTTP-Result Failures
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
        } else if (e instanceof InterruptedException) {
            router.errorHandler.raiseError(new Exception(e), payload);
            router.setState(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        } else {
            router.errorHandler.raiseError(new Exception(e), errorMessage);
            router.setState(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
    }

    /***********************************
     *
     * PUBLIC STUFF
     *
     ************************************/

    /**
     * check if cookie is set and not expired
     * 
     * @return
     */
    public Boolean isValidLogin() {
        Boolean cookieExpired = System.currentTimeMillis() > this.cookieTimeStamp + (COOKIE_LIFETIME_S * 1000);
        if (cookieExpired.equals(true)) {
            logger.trace("({}) cookie is expired ", uid);
        }
        return cookieExpired.equals(false) && !this.cookie.isBlank();
    }

    /**
     * check authentication (login) and relogin if not
     * 
     * @return
     */
    public Boolean checkAuth() {
        if (isValidLogin().equals(false)) {
            return login();
        }
        return true;
    }
}
