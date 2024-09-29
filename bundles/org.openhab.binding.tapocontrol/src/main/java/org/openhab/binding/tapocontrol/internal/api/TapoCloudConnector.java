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
package org.openhab.binding.tapocontrol.internal.api;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.JsonUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.api.protocol.passthrough.PassthroughProtocol;
import org.openhab.binding.tapocontrol.internal.devices.bridge.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.devices.bridge.dto.TapoCloudLoginData;
import org.openhab.binding.tapocontrol.internal.devices.bridge.dto.TapoCloudLoginResult;
import org.openhab.binding.tapocontrol.internal.discovery.dto.TapoDiscoveryResultList;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TAPO-Cloud connections.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoCloudConnector implements TapoConnectorInterface {
    private final Logger logger = LoggerFactory.getLogger(TapoCloudConnector.class);
    private final TapoBridgeHandler bridge;

    private @NonNullByDefault({}) TapoCloudLoginResult loginResult;
    private String token = "";
    private String uid;
    private PassthroughProtocol passthrough;

    /***********************
     * Init Class
     **********************/

    public TapoCloudConnector(TapoBridgeHandler bridge) {
        this.bridge = bridge;
        this.uid = bridge.getUID().getAsString();
        passthrough = new PassthroughProtocol(this);
    }

    /***********************
     * Response-Handling
     **********************/

    /**
     * handle received reponse-string
     */
    @Override
    public void responsePasstrough(String response, String command) {
    }

    /**
     * handle received response
     */
    @Override
    public void handleResponse(TapoResponse tapoResponse, String command) throws TapoErrorHandler {
        switch (command) {
            case CLOUD_CMD_LOGIN:
                handleLoginResult(tapoResponse);
                break;
            case CLOUD_CMD_GETDEVICES:
                bridge.getDiscoveryService().addScanResults(getDeviceListFromResponse(tapoResponse));
                break;
            default:
                logger.debug("({}) handleResponse - unknown command: {}", uid, command);
                throw new TapoErrorHandler(ERR_BINDING_NOT_IMPLEMENTED);
        }
    }

    /**
     * set bridge error
     */
    @Override
    public void handleError(TapoErrorHandler tapoError) {
        bridge.setError(tapoError);
    }

    /***********************
     * Login Handling
     **********************/
    /**
     * login to cloud
     */
    public boolean login(TapoCredentials credentials) throws TapoErrorHandler {
        logout();

        TapoCloudLoginData loginData = new TapoCloudLoginData(credentials.username(), credentials.password());
        TapoRequest request = new TapoRequest(CLOUD_CMD_LOGIN, loginData);

        passthrough.sendRequest(request);
        return isLoggedIn();
    }

    /*
     * get response from login and set token
     */
    private void handleLoginResult(TapoResponse tapoResponse) throws TapoErrorHandler {
        logger.trace("({}) received login result: {}", uid, tapoResponse);
        loginResult = getObjectFromJson(tapoResponse.result(), TapoCloudLoginResult.class);
        token = loginResult.token();
        if (!isLoggedIn()) {
            throw new TapoErrorHandler(ERR_API_LOGIN);
        }
    }

    public void logout() {
        loginResult = new TapoCloudLoginResult();
        token = "";
    }

    public boolean isLoggedIn() {
        return !token.isBlank();
    }

    /***********************
     * Cloud Action Handlers
     **********************/
    /**
     * Query DeviceList from cloud
     */
    public void getDeviceList() {
        TapoRequest request = new TapoRequest(CLOUD_CMD_GETDEVICES);
        logger.trace("({}) sending cloud command: {}", uid, request);
        try {
            passthrough.sendRequest(request);
        } catch (TapoErrorHandler tapoError) {
            logger.debug("({}) get devicelist failed: {}", uid, tapoError.getCode());
            handleError(tapoError);
        }
    }

    /**
     * get DeviceList from response
     */
    private TapoDiscoveryResultList getDeviceListFromResponse(TapoResponse tapoResponse) throws TapoErrorHandler {
        logger.trace("({}) received devicelist: {}", uid, tapoResponse);
        return getObjectFromJson(tapoResponse.result(), TapoDiscoveryResultList.class);
    }

    /***********************
     * Get Values
     **********************/

    @Override
    public HttpClient getHttpClient() {
        return bridge.getHttpClient();
    }

    @Override
    public String getThingUID() {
        return bridge.getUID().toString();
    }

    /************************
     * Private Helpers
     ************************/

    /**
     * Get Cloud-URL
     */
    @Override
    public String getBaseUrl() {
        String url = TAPO_CLOUD_URL;
        if (!token.isBlank()) {
            url = url + "?token=" + token;
        }
        return url;
    }
}
