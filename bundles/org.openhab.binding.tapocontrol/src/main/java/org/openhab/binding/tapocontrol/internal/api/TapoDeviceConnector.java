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

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.api.protocol.TapoProtocolEnum;
import org.openhab.binding.tapocontrol.internal.api.protocol.TapoProtocolInterface;
import org.openhab.binding.tapocontrol.internal.api.protocol.aes.SecurePassthrough;
import org.openhab.binding.tapocontrol.internal.api.protocol.klap.KlapProtocol;
import org.openhab.binding.tapocontrol.internal.api.protocol.passthrough.PassthroughProtocol;
import org.openhab.binding.tapocontrol.internal.devices.bridge.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.wifi.TapoBaseDeviceHandler;
import org.openhab.binding.tapocontrol.internal.dto.TapoBaseRequestInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoChildRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoMultipleRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Connection handler class for TAPO wifi devices.
 * This class uses asynchronous HttpClient-Requests
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDeviceConnector implements TapoConnectorInterface {
    private final Logger logger = LoggerFactory.getLogger(TapoDeviceConnector.class);
    private final TapoBaseDeviceHandler device;
    private final TapoBridgeHandler bridge;
    private final String uid;
    private TapoProtocolInterface protocolHandler;
    private TapoResponse queryResponse = new TapoResponse();
    private long lastQuery = 0L;
    private long lastSent = 0L;
    private long lastLogin = 0L;
    private boolean queryAfterCommand = false;

    /***********************
     * Init Class
     **********************/

    public TapoDeviceConnector(TapoBaseDeviceHandler tapoDeviceHandler, TapoBridgeHandler bridgeThingHandler) {
        bridge = bridgeThingHandler;
        device = tapoDeviceHandler;
        uid = device.getThingUID().toString() + " / DeviceConnector";
        protocolHandler = setProtocol(device.getDeviceConfig().protocol);
    }

    /**
     * Set DeviceProtocol which is used for communication
     */
    protected TapoProtocolInterface setProtocol(String protocol) {
        switch (TapoProtocolEnum.valueOfString(protocol)) {
            case PASSTHROUGH:
                logger.trace("({}) selected passtrough-protocol '{}'", uid, protocol);
                return new PassthroughProtocol(this);
            case SECUREPASSTROUGH:
                logger.trace("({}) selected secure-passtrough-protocol '{}'", uid, protocol);
                return new SecurePassthrough(this);
            case KLAP:
                logger.trace("({}) selected klap-protocol '{}'", uid, protocol);
                return new KlapProtocol(this);
            default:
                logger.debug("({}) unknown protocol '{}'", uid, protocol);
                handleError(new TapoErrorHandler(NO_ERROR, protocol));
                return new PassthroughProtocol(this);
        }
    }

    /***********************
     * LOGIN FUNCTIONS
     **********************/

    public boolean login() {
        if (pingDevice()) {
            long now = System.currentTimeMillis();
            if (now > lastLogin + TAPO_LOGIN_MIN_GAP_MS) {
                lastLogin = now;
                try {
                    protocolHandler.login(bridge.getCredentials());
                } catch (TapoErrorHandler tapoError) {
                    logger.debug("({}) exception while login '{}'", uid, tapoError.toString());
                    handleError(tapoError);
                }
            }
        } else {
            logger.debug("({}) no ping while login '{}'", uid, device.getIpAddress());
            handleError(new TapoErrorHandler(ERR_BINDING_DEVICE_OFFLINE, "no ping while login"));
        }
        return protocolHandler.isLoggedIn();
    }

    public void logout() {
        protocolHandler.logout();
    }

    public boolean isLoggedIn() {
        return protocolHandler.isLoggedIn();
    }

    /***********************
     * DEVICE ACTIONS
     **********************/

    /**
     * Send raw (unsecured) request to device
     */
    public void sendRawCommand(TapoRequest request) {
        try {
            PassthroughProtocol passtrhough = new PassthroughProtocol(this);
            passtrhough.sendAsyncRequest(request);
        } catch (TapoErrorHandler tapoError) {
            logger.debug("({}) send raw command failed '{}'", uid, tapoError.toString());
            handleError(tapoError);
        }
    }

    /**
     * Send DeviceQueryCommand to Device
     *
     * @param queryCommand Command to be queried
     */
    public void sendQueryCommand(String queryCommand) {
        sendQueryCommand(queryCommand, false);
    }

    /**
     * Send Custom DeviceQuery
     *
     * @param queryCommand Command to be queried
     * @param ignoreGap ignore gap to last query. query anyway
     */
    public void sendQueryCommand(String queryCommand, boolean ignoreGap) {
        queryAfterCommand = false;
        long now = System.currentTimeMillis();
        if (ignoreGap || now > lastQuery + TAPO_QUERY_MIN_GAP_MS) {
            lastQuery = now;
            sendAsyncRequest(new TapoRequest(queryCommand));
        } else {
            logger.debug("({}) command not sent because of min_gap: {} <- {}", uid, now, lastQuery);
        }
    }

    /**
     * Send "set_device_info" command to device and query info immediately
     * 
     * @param deviceDataClass clazz contains devicedata which should be sent
     */
    public void sendDeviceCommand(Object deviceDataClass) {
        sendDeviceCommand(DEVICE_CMD_SETINFO, deviceDataClass);
    }

    /**
     * Send command to device with params and query info immediately
     * 
     * @param command command
     * @param deviceDataClass clazz contains devicedata which should be sent
     */
    public void sendDeviceCommand(String command, Object deviceDataClass) {
        sendDeviceCommand(command, deviceDataClass, false);
    }

    /**
     * Send command to device with params
     * 
     * @param command command
     * @param deviceDataClass clazz contains devicedata which should be sent
     * @param ignoreGap ignore gap to last query. query anyway
     */
    public void sendDeviceCommand(String command, Object deviceDataClass, boolean ignoreGap) {
        queryAfterCommand = false;
        long now = System.currentTimeMillis();
        if (ignoreGap || now > lastSent + TAPO_SEND_MIN_GAP_MS) {
            sendAsyncRequest(new TapoRequest(command, deviceDataClass));
        } else {
            logger.debug("({}) command not sent because of min_gap: {} <- {}", uid, now, lastSent);
        }
    }

    /**
     * Send command to device with params and query info immediately
     * 
     * @param deviceDataClass clazz contains devicedata which should be sent
     * @param multipleRequestSupported set to true if device supports multipleRequests
     */
    public void sendCommandAndQuery(Object deviceDataClass, boolean multipleRequestSupported) {
        sendCommandAndQuery(DEVICE_CMD_SETINFO, deviceDataClass, multipleRequestSupported);
    }

    /**
     * Send command to device with params and query info immediately
     * 
     * @param command command
     * @param deviceDataClass clazz contains devicedata which should be sent
     * @param multipleRequestSupported set to true if device supports multipleRequests
     */
    public void sendCommandAndQuery(String command, Object deviceDataClass, boolean multipleRequestSupported) {
        if (multipleRequestSupported) {
            List<TapoRequest> requests = new ArrayList<>();
            requests.add(new TapoRequest(command, deviceDataClass));
            requests.add(new TapoRequest(DEVICE_CMD_GETINFO));
            sendAsyncRequest(new TapoMultipleRequest(requests));
        } else {
            sendDeviceCommand(command, deviceDataClass, true);
            queryAfterCommand = true;
        }
    }

    /**
     * Send command to child device
     * 
     * @param childData ChildDeviceData-Class
     */
    public void sendChildCommand(TapoChildDeviceData childData) {
        sendChildCommand(childData, false);
    }

    /**
     * Send command to child device
     * 
     * @param childData ChildDeviceData-Class
     * @param ignoreGap ignore gap to last query. query anyway
     */
    public void sendChildCommand(TapoChildDeviceData childData, boolean ignoreGap) {
        long now = System.currentTimeMillis();
        if (ignoreGap || now > lastSent + TAPO_SEND_MIN_GAP_MS) {
            lastSent = now;
            sendAsyncRequest(new TapoChildRequest(childData));
        } else {
            logger.debug("({}) command not sent because of min_gap: {} <- {}", uid, now, lastSent);
        }
    }

    /**
     * send asynchronous multi-request
     *
     * @param requests list of TapoRequests should be sent to device
     */
    public void sendMultipleRequest(List<TapoRequest> requests) {
        sendMultipleRequest(requests, false);
    }

    /**
     * send asynchronous multi-request igrnoring min-gap
     * 
     * @param requests list of TapoRequests should be sent to device
     * @param ignoreGap ignoreGap ignore gap to last query. query anyway
     */
    public void sendMultipleRequest(List<TapoRequest> requests, boolean ignoreGap) {
        queryAfterCommand = false;
        long now = System.currentTimeMillis();
        if (ignoreGap || now > lastQuery + TAPO_QUERY_MIN_GAP_MS) {
            lastQuery = now;
            sendAsyncRequest(new TapoMultipleRequest(requests));
        } else {
            logger.debug("({}) command not sent because of min_gap: {} <- {}", uid, now, lastSent);
        }
    }

    /**
     * send asynchron multi-request
     * 
     * @param requests array of TapoRequest
     */
    public void sendMultipleRequest(TapoRequest... requests) {
        sendMultipleRequest(requests);
    }

    /**
     * send asynchron request to protocol handler
     * 
     * @param tapoRequest Request inherits TapoBaseRequestInterface
     */
    public void sendAsyncRequest(TapoBaseRequestInterface tapoRequest) {
        try {
            protocolHandler.sendAsyncRequest(tapoRequest);
        } catch (TapoErrorHandler tapoError) {
            handleError(tapoError);
        }
    }

    /***********************
     * Response-Handling
     **********************/

    /**
     * Return class object from json formated string
     * 
     * @param json json formatted string
     * @param clazz class string should parsed to
     */
    private <T> T getObjectFromJson(String json, Class<T> clazz) {
        try {
            @Nullable
            T result = GSON.fromJson(json, clazz);
            if (result == null) {
                throw new JsonParseException("result is null");
            }
            return result;
        } catch (Exception e) {
            logger.debug("({}) error parsing string {} to class: {}", uid, json, clazz.getName());
            device.setError(new TapoErrorHandler(ERR_API_JSON_DECODE_FAIL));
            return Objects.requireNonNull(GSON.fromJson(json, clazz));
        }
    }

    /**
     * Return class object from JsonObject
     * 
     * @param jso JsonOject
     * @param clazz class string should parsed to
     */
    private <T> T getObjectFromJson(JsonObject jso, Class<T> clazz) {
        return getObjectFromJson(jso.toString(), clazz);
    }

    /**
     * handle and decrypt response from device
     * 
     * @param response TapoResponse was received
     * @param command was sent to device belonging to response
     */
    @Override
    public void handleResponse(TapoResponse response, String command) {
        if (!response.hasError()) {
            if (DEVICE_CMD_MULTIPLE_REQ.equals(command)) {
                handleMultipleRespone(response);
            } else {
                handleSingleResponse(response, command);
            }
        } else {
            device.setError(new TapoErrorHandler(response.errorCode()));
        }
    }

    /**
     * Handle response got from single-request
     * 
     * @param response response from request
     * @param command command was sent
     */
    private void handleSingleResponse(TapoResponse response, String command) {
        logger.trace("({}) handle singleResponse from command '{}'", uid, command);
        if (DEVICE_CMDLIST_QUERY.contains(command)) {
            handleQueryResult(response, command);
        } else if (DEVICE_CMDLIST_SET.contains(command)) {
            handleSuccessResponse(response);
        } else {
            device.responsePasstrough(response);
        }
    }

    /**
     * Handle response got from multiple-request
     * 
     * @param response response from request
     */
    private void handleMultipleRespone(TapoResponse response) {
        logger.trace("({}) handle multipleResponse '{}'", uid, response);
        for (TapoResponse results : response.responses()) {
            handleSingleResponse(results, results.method());
        }
    }

    /**
     * Parse responsedata from result to object and inform device about new data
     * 
     * @param response response from request
     * @param command command was sent
     */
    private void handleQueryResult(TapoResponse response, String command) {
        if (!response.hasError()) {
            logger.trace("({}) queryResponse successfull '{}'", uid, response);
            queryResponse = response;
            device.newDataResult(command);
        } else {
            logger.debug("({}) query response returned error: {}", uid, response);
            device.setError(new TapoErrorHandler(queryResponse.errorCode()));
        }
    }

    /**
     * Handle SuccessResponse (setDeviceInfo)
     * 
     * @param response response from request
     */
    private void handleSuccessResponse(TapoResponse response) {
        if (response.hasError()) {
            logger.debug("({}) set deviceInfo not successful: {}", uid, response);
            device.setError(new TapoErrorHandler(response.errorCode()));
        } else {
            logger.trace("({}) setcommand successfull '{}'", uid, response);
            if (queryAfterCommand) {
                sendQueryCommand(DEVICE_CMD_GETINFO, true);
            }
        }
        queryAfterCommand = false;
        this.device.responsePasstrough(response);
    }

    /**
     * Handle custom response
     *
     * @param response response from request
     */
    protected void handleCustomResponse(TapoResponse response) {
        logger.trace("({}) handle custom response '{}'", uid, response);
        this.device.responsePasstrough(response);
    }

    /**
     * handle error
     */
    @Override
    public void handleError(TapoErrorHandler tapoError) {
        logger.debug("({}) handle error '{}'", uid, tapoError.getMessage());
        device.setError(tapoError);
    }

    /***********************
     * Get Values
     **********************/

    /**
     * Check if device is online
     *
     * @return true if device is online
     */
    public boolean isOnline() {
        return isOnline(false);
    }

    /**
     * Check if device is online
     *
     * @param raiseError if true - if false it will be only logout();
     */
    public boolean isOnline(boolean raiseError) {
        if (pingDevice()) {
            return true;
        } else {
            logger.trace("({})  device is offline (no ping)", uid);
            if (raiseError) {
                handleError(new TapoErrorHandler(ERR_BINDING_DEVICE_OFFLINE));
            }
            protocolHandler.logout();
            return false;
        }
    }

    /**
     * Get Dataobject from response
     * 
     * @param clazz object class response should be transformed
     */
    public <T> T getResponseData(Class<T> clazz) {
        return getObjectFromJson(queryResponse.result(), clazz);
    }

    /**
     * Ping to IP of device - return true if successfull
     */
    public boolean pingDevice() {
        try {
            InetAddress address = InetAddress.getByName(device.getIpAddress());
            return address.isReachable(TAPO_PING_TIMEOUT_MS);
        } catch (Exception e) {
            logger.debug("({}) InetAdress throws: {}", uid, e.getMessage());
            return false;
        }
    }

    @Override
    public HttpClient getHttpClient() {
        return bridge.getHttpClient();
    }

    @Override
    public void responsePasstrough(String response, String command) {
        throw new UnsupportedOperationException("Unimplemented method 'responsePasstrough'");
    }

    @Override
    public String getBaseUrl() {
        return device.getIpAddress() + ":" + device.getDeviceConfig().httpPort;
    }

    @Override
    public String getThingUID() {
        return device.getThingUID().toString();
    }
}
