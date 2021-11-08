/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoErrorConstants.*;

import java.net.InetAddress;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.device.TapoDevice;
import org.openhab.binding.tapocontrol.internal.device.TapoDeviceInfo;
import org.openhab.binding.tapocontrol.internal.helpers.PayloadBuilder;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Smart Home device connections.
 * This class uses asynchronous HttpClient-Requests
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDeviceConnector extends TapoDeviceHttpApi {
    private final Logger logger = LoggerFactory.getLogger(TapoDeviceConnector.class);
    private final TapoErrorHandler tapoError;
    private final String uid;
    private final TapoDevice device;
    private TapoDeviceInfo deviceInfo;
    private Gson gson;
    private Long lastQuery = 0L;
    private Long lastSent = 0L;
    private Long lastLogin = 0L;

    /**
     * INIT CLASS
     *
     * @param config TapoControlConfiguration class
     */
    public TapoDeviceConnector(TapoDevice device, TapoCredentials credentials, HttpClient httpClient) {
        super(device, credentials, httpClient);
        this.device = device;
        this.gson = new Gson();
        this.tapoError = new TapoErrorHandler();
        this.deviceInfo = new TapoDeviceInfo();
        this.uid = device.getThingUID().getAsString();
    }

    /***********************************
     *
     * LOGIN FUNCTIONS
     *
     ************************************/
    /**
     * login
     *
     * @return true if success
     */
    public boolean login() {
        if (this.pingDevice()) {
            logger.debug("({}) sending login to url '{}'", uid, deviceURL);
            tapoError.reset(); // reset ErrorHandler

            Long now = System.currentTimeMillis();
            if (now > this.lastLogin + TAPO_LOGIN_MIN_GAP_MS) {
                this.lastLogin = now;
                unsetToken();
                unsetCookie();

                /* create ssl-handschake (cookie) */
                String cookie = createHandshake();
                if (!cookie.isBlank()) {
                    setCookie(cookie);
                    String token = queryToken();
                    setToken(token);
                }
            } else {
                logger.trace("({}) not done cause of min_gap '{}'", uid, TAPO_LOGIN_MIN_GAP_MS);
            }
            return this.loggedIn();
        } else {
            logger.debug("({}) no ping while login '{}'", uid, this.ipAddress);
            tapoError.raiseError(ERR_DEVICE_OFFLINE, "no ping while login");
            return false;
        }
    }

    /***********************************
     *
     * DEVICE ACTIONS
     *
     ************************************/

    /**
     * send command to device
     *
     * @param name Name of command to send
     * @param value Value to send to control
     */
    public void sendDeviceCommand(String name, Object value) {
        Long now = System.currentTimeMillis();
        if (now > this.lastSent + TAPO_SEND_MIN_GAP_MS) {
            /* encrypt command payload */
            PayloadBuilder plBuilder = new PayloadBuilder();
            plBuilder.method = DEVICE_CMD_SETINFO;
            plBuilder.addParameter(name, value);
            String payload = plBuilder.getPayload();
            String encryptedPayload = encryptPayload(payload);

            /* create secured payload */
            plBuilder = new PayloadBuilder();
            plBuilder.method = "securePassthrough";
            plBuilder.addParameter("request", encryptedPayload);
            String securePassthroughPayload = plBuilder.getPayload();

            this.lastSent = System.currentTimeMillis();
            sendAsyncRequest(deviceURL, securePassthroughPayload, DEVICE_CMD_SETINFO);
        } else {
            logger.debug("({}) command not sent becauso of min_gap: {}", uid, now + " <- " + lastSent);
        }
    }

    /**
     * send multiple commands to device
     *
     * @param map HashMap<String, Object> (name, value of parameter)
     */
    public void sendDeviceCommands(HashMap<String, Object> map) {
        Long now = System.currentTimeMillis();
        if (now > this.lastSent + TAPO_SEND_MIN_GAP_MS) {
            this.lastSent = now;

            /* encrypt command payload */
            PayloadBuilder plBuilder = new PayloadBuilder();
            plBuilder.method = DEVICE_CMD_SETINFO;
            for (HashMap.Entry<String, Object> entry : map.entrySet()) {
                plBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            String payload = plBuilder.getPayload();
            String encryptedPayload = encryptPayload(payload);

            /* create secured payload */
            plBuilder = new PayloadBuilder();
            plBuilder.method = "securePassthrough";
            plBuilder.addParameter("request", encryptedPayload);
            String securePassthroughPayload = plBuilder.getPayload();

            this.lastSent = System.currentTimeMillis();
            sendAsyncRequest(deviceURL, securePassthroughPayload, DEVICE_CMD_SETINFO);
        } else {
            logger.debug("({}) command not sent becauso of min_gap: {}", uid, now + " <- " + lastSent);
        }
    }

    /**
     * Query Info from Device
     *
     * @return tapo device info object
     */
    public void queryInfo() {
        logger.trace("({}) DeviceConnetor_queryInfo from '{}'", uid, deviceURL);
        Long now = System.currentTimeMillis();
        if (now > this.lastQuery + TAPO_SEND_MIN_GAP_MS) {
            this.lastQuery = now;

            /* encrypt command payload */
            PayloadBuilder plBuilder = new PayloadBuilder();
            plBuilder.method = DEVICE_CMD_GETINFO;
            String payload = plBuilder.getPayload();
            String encryptedPayload = encryptPayload(payload);

            /* create secured payload */
            plBuilder = new PayloadBuilder();
            plBuilder.method = "securePassthrough";
            plBuilder.addParameter("request", encryptedPayload);
            String securePassthroughPayload = plBuilder.getPayload();
            sendAsyncRequest(deviceURL, securePassthroughPayload, DEVICE_CMD_GETINFO);
        } else {
            logger.debug("({}) command not sent becauso of min_gap: {}", uid, now + " <- " + lastQuery);
        }
    }

    /***********************************
     *
     * HANDLE RESPONSES
     *
     ************************************/

    /**
     * Handle SuccessResponse (setDeviceInfo)
     */
    @Override
    protected void handleSuccessResponse(String responseBody) {
        try {
            JsonObject jsnResult = getJsonFromResponse(responseBody);
            Integer errorCode = jsnResult.get("error_code").getAsInt();
            if (errorCode != 0) {
                logger.debug("({}) set deviceInfo not succesfull: {}", uid, jsnResult.toString());
                this.device.handleConnectionState();
            }
        } catch (Exception e) {
            logger.debug("({}) handleSuccessResponse exception {}", uid, e.toString());
        }
    }

    /**
     * Handle JsonResponse (getDeviceInfo)
     */
    @Override
    protected void handleDeviceResult(String responseBody) {
        JsonObject jsnResult = getJsonFromResponse(responseBody);
        if (jsnResult.has("device_id")) {
            this.deviceInfo = new TapoDeviceInfo(jsnResult);
            this.device.setDeviceInfo(deviceInfo);
        } else {
            this.deviceInfo = new TapoDeviceInfo();
            this.device.handleConnectionState();
        }
    }

    /**
     * get Json from response
     * 
     * @param responseBody
     * @return JsonObject with result
     */
    private JsonObject getJsonFromResponse(String responseBody) {
        try {
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            /* get errocode (0=success) */
            Integer errorCode = jsonObject.get("error_code").getAsInt();
            if (errorCode == 0) {
                /* decrypt response */
                String decryptedResponse = decryptResponse(responseBody);
                jsonObject = gson.fromJson(decryptedResponse, JsonObject.class);
                logger.trace("({}) received result: {}", uid, decryptedResponse);

                /* return result if set / else request was successfull */
                if (jsonObject.has("result")) {
                    return jsonObject.getAsJsonObject("result");
                } else {
                    return jsonObject;
                }
            } else {
                /* return errorcode from device */
                tapoError.raiseError(errorCode, "device answers with errorcode");
                logger.debug("({}) device answers with errorcode {} - {}", uid, errorCode, tapoError.getMessage());
                return jsonObject;
            }
        } catch (Exception e) {
            logger.debug("({}) sendPayload exception {}", uid, e.toString());
            tapoError.raiseError(e);
            return new JsonObject();
        }
    }

    /**
     * HANDLE ERROR
     * 
     * @param te TapoErrorHandler
     */
    @Override
    protected void handleErrorResult(TapoErrorHandler te) {
        this.tapoError.set(te);
        this.device.handleConnectionState();
    }

    @Override
    protected void handleErrorResult(Exception ex) {
        this.tapoError.raiseError(ex);
        this.device.handleConnectionState();
    }

    /***********************************
     *
     * GET RESULTS
     *
     ************************************/

    /**
     * Check if device is online
     * 
     * @return true if device is online
     */
    public Boolean isOnline() {
        return isOnline(false);
    }

    /**
     * Check if device is online
     * 
     * @param raiseError if true
     * @return true if device is online
     */
    public Boolean isOnline(Boolean raiseError) {
        if (pingDevice()) {
            return true;
        } else {
            logger.trace("({})  device is offline (no ping)", uid);
            if (raiseError) {
                tapoError.raiseError(ERR_DEVICE_OFFLINE);
            }
            logout();
            return false;
        }
    }

    /**
     * ErrorCode
     * 
     * @return true if has error
     */
    public Boolean hasError() {
        return tapoError.getNumber() != 0;
    }

    /**
     * RETURN ERRORHANDLER
     * 
     * @return
     */
    public TapoErrorHandler getError() {
        return tapoError;
    }

    /**
     * SendSuccess
     * 
     * @return true if command was sent successfull
     */
    public Boolean commandSuccess() {
        return tapoError.getNumber() == 0;
    }

    /**
     * IP-Adress
     * 
     * @return String ipAdress
     */
    public String getIP() {
        return this.ipAddress;
    }

    /**
     * PING IP Adress
     * 
     * @return true if ping successfull
     */
    public Boolean pingDevice() {
        try {
            InetAddress address = InetAddress.getByName(this.ipAddress);
            return address.isReachable(TAPO_PING_TIMEOUT_MS);
        } catch (Exception e) {
            logger.debug("({}) InetAdress throws: {}", uid, e.getMessage());
            return false;
        }
    }
}
