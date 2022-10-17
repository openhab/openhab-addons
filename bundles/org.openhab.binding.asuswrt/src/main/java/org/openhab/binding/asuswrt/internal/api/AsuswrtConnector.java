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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.asuswrt.internal.AsuswrtRouter;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtConfiguration;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtCredentials;
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

        logger.trace("({}) perform login to '{}' with '{}'", uid, url, encodedCredentials);

        payload = "login_authorization=" + encodedCredentials + "}";
        ContentResponse response = sendRequest(url, payload);
        if (response != null) {
            setCookieFromResponse(response);
        }
        return isValidLogin();
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
    public void querySysInfo() {
        logger.trace("({}) query SysInfo", uid);
        String url = routerConfig.url + "/appGet.cgi";
        String payload = "hook=" + CMD_GET_SYSINFO;

        /* send payload as url parameter */
        url = url + "?" + payload;
        url = url.replace(";", "%3B");

        ContentResponse response = sendRequest(url, "");
        if (response != null) {
            handleHttpSuccessResponse(response.getContentAsString(), CMD_GET_SYSINFO);
        }
    }

    /**
     * Query Data From Device asynchron
     * 
     * @param command command constant to sent
     */
    public void queryDeviceData(String command) {
        logger.trace("({}) queryDeviceData", uid);
        String url = routerConfig.url + "/appGet.cgi";
        String payload = "hook=" + command;

        /* send payload as url parameter */
        url = url + "?" + payload;
        url = url.replace(";", "%3B");

        sendAsyncRequest(url, payload, command);
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
        switch (command) {
            case CMD_GET_SYSINFO:
                router.deviceInfo.setSysInfo(jsonObject);
                router.devicePropertiesChanged(router.deviceInfo);
                break;
            default:
                router.deviceInfo.setData(jsonObject);
                router.updateChannels(router.deviceInfo);
                break;
        }
    }

    /***********************************
     *
     * PUBLIC STUFF
     *
     ************************************/

    public Boolean isValidLogin() {
        Boolean cookieExpired = System.currentTimeMillis() > this.cookieTimeStamp + (COOKIE_LIFETIME_S * 1000);
        if (cookieExpired.equals(true)) {
            logger.trace("({}) cookie is expired ", uid);
        }
        return cookieExpired.equals(false) && !this.cookie.isBlank();
    }
}
