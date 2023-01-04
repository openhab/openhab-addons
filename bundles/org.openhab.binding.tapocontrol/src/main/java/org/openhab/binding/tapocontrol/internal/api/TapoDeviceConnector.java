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
package org.openhab.binding.tapocontrol.internal.api;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import java.net.InetAddress;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.device.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.device.TapoDevice;
import org.openhab.binding.tapocontrol.internal.helpers.PayloadBuilder;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tapocontrol.internal.structures.TapoDeviceInfo;
import org.openhab.binding.tapocontrol.internal.structures.TapoEnergyData;
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
    private final String uid;
    private final TapoDevice device;
    private TapoDeviceInfo deviceInfo;
    private TapoEnergyData energyData;
    private Gson gson;
    private long lastQuery = 0L;
    private long lastSent = 0L;
    private long lastLogin = 0L;

    /**
     * INIT CLASS
     *
     * @param config TapoControlConfiguration class
     */
    public TapoDeviceConnector(TapoDevice device, TapoBridgeHandler bridgeThingHandler) {
        super(device, bridgeThingHandler);
        this.device = device;
        this.gson = new Gson();
        this.deviceInfo = new TapoDeviceInfo();
        this.energyData = new TapoEnergyData();
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
            logger.trace("({}) sending login to url '{}'", uid, deviceURL);

            long now = System.currentTimeMillis();
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
            handleError(new TapoErrorHandler(ERR_DEVICE_OFFLINE, "no ping while login"));
            return false;
        }
    }

    /***********************************
     *
     * DEVICE ACTIONS
     *
     ************************************/

    /**
     * send custom command to device
     * 
     * @param plBuilder Payloadbuilder with unencrypted payload
     */
    public void sendCustomQuery(String queryMethod) {
        /* create payload */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = queryMethod;
        sendCustomPayload(plBuilder);
    }

    /**
     * send custom command to device
     * 
     * @param plBuilder Payloadbuilder with unencrypted payload
     */
    public void sendCustomPayload(PayloadBuilder plBuilder) {
        long now = System.currentTimeMillis();
        if (now > this.lastSent + TAPO_SEND_MIN_GAP_MS) {
            String payload = plBuilder.getPayload();
            sendSecurePasstrhroug(payload, DEVICE_CMD_CUSTOM);
        } else {
            logger.debug("({}) command not sent becauso of min_gap: {}", uid, now + " <- " + lastSent);
        }
    }

    /**
     * send "set_device_info" command to device
     *
     * @param name Name of command to send
     * @param value Value to send to control
     */
    public void sendDeviceCommand(String name, Object value) {
        long now = System.currentTimeMillis();
        if (now > this.lastSent + TAPO_SEND_MIN_GAP_MS) {
            this.lastSent = now;

            /* create payload */
            PayloadBuilder plBuilder = new PayloadBuilder();
            plBuilder.method = DEVICE_CMD_SETINFO;
            plBuilder.addParameter(name, value);
            String payload = plBuilder.getPayload();

            sendSecurePasstrhroug(payload, DEVICE_CMD_SETINFO);
        } else {
            logger.debug("({}) command not sent becauso of min_gap: {}", uid, now + " <- " + lastSent);
        }
    }

    /**
     * send multiple "set_device_info" commands to device
     *
     * @param map HashMap<String, Object> (name, value of parameter)
     */
    public void sendDeviceCommands(HashMap<String, Object> map) {
        long now = System.currentTimeMillis();
        if (now > this.lastSent + TAPO_SEND_MIN_GAP_MS) {
            this.lastSent = now;

            /* create payload */
            PayloadBuilder plBuilder = new PayloadBuilder();
            plBuilder.method = DEVICE_CMD_SETINFO;
            for (HashMap.Entry<String, Object> entry : map.entrySet()) {
                plBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            String payload = plBuilder.getPayload();

            sendSecurePasstrhroug(payload, DEVICE_CMD_SETINFO);
        } else {
            logger.debug("({}) command not sent becauso of min_gap: {}", uid, now + " <- " + lastSent);
        }
    }

    /**
     * Query Info from Device adn refresh deviceInfo
     */
    public void queryInfo() {
        queryInfo(false);
    }

    /**
     * Query Info from Device adn refresh deviceInfo
     * 
     * @param ignoreGap ignore gap to last query. query anyway
     */
    public void queryInfo(boolean ignoreGap) {
        logger.trace("({}) DeviceConnetor_queryInfo from '{}'", uid, deviceURL);
        long now = System.currentTimeMillis();
        if (ignoreGap || now > this.lastQuery + TAPO_SEND_MIN_GAP_MS) {
            this.lastQuery = now;

            /* create payload */
            PayloadBuilder plBuilder = new PayloadBuilder();
            plBuilder.method = DEVICE_CMD_GETINFO;
            String payload = plBuilder.getPayload();

            sendSecurePasstrhroug(payload, DEVICE_CMD_GETINFO);
        } else {
            logger.debug("({}) command not sent becauso of min_gap: {}", uid, now + " <- " + lastQuery);
        }
    }

    /**
     * Get energy usage from device
     */
    public void getEnergyUsage() {
        logger.trace("({}) DeviceConnetor_getEnergyUsage from '{}'", uid, deviceURL);

        /* create payload */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = DEVICE_CMD_GETENERGY;
        String payload = plBuilder.getPayload();

        sendSecurePasstrhroug(payload, DEVICE_CMD_GETENERGY);
    }

    /**
     * SEND SECUREPASSTHROUGH
     * encprypt payload and send to device
     * 
     * @param payload payload sent to device
     * @param command command executed - this will handle result
     */
    protected void sendSecurePasstrhroug(String payload, String command) {
        /* encrypt payload */
        logger.trace("({}) encrypting payload '{}'", uid, payload);
        String encryptedPayload = encryptPayload(payload);

        /* create secured payload */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = "securePassthrough";
        plBuilder.addParameter("request", encryptedPayload);
        String securePassthroughPayload = plBuilder.getPayload();

        sendAsyncRequest(deviceURL, securePassthroughPayload, command);
    }

    /***********************************
     *
     * HANDLE RESPONSES
     *
     ************************************/

    /**
     * Handle SuccessResponse (setDeviceInfo)
     * 
     * @param responseBody String with responseBody from device
     */
    @Override
    protected void handleSuccessResponse(String responseBody) {
        JsonObject jsnResult = getJsonFromResponse(responseBody);
        Integer errorCode = jsonObjectToInt(jsnResult, "error_code", ERR_JSON_DECODE_FAIL);
        if (errorCode != 0) {
            logger.debug("({}) set deviceInfo not successful: {}", uid, jsnResult);
            this.device.handleConnectionState();
        }
        this.device.responsePasstrough(responseBody);
    }

    /**
     * 
     * handle JsonResponse (getDeviceInfo)
     * 
     * @param responseBody String with responseBody from device
     */
    @Override
    protected void handleDeviceResult(String responseBody) {
        JsonObject jsnResult = getJsonFromResponse(responseBody);
        if (jsnResult.has(DEVICE_PROPERTY_ID)) {
            this.deviceInfo = new TapoDeviceInfo(jsnResult);
            this.device.setDeviceInfo(deviceInfo);
        } else {
            this.deviceInfo = new TapoDeviceInfo();
            this.device.handleConnectionState();
        }
        this.device.responsePasstrough(responseBody);
    }

    /**
     * handle JsonResponse (getEnergyData)
     * 
     * @param responseBody String with responseBody from device
     */
    @Override
    protected void handleEnergyResult(String responseBody) {
        JsonObject jsnResult = getJsonFromResponse(responseBody);
        if (jsnResult.has(ENERGY_PROPERTY_POWER)) {
            this.energyData = new TapoEnergyData(jsnResult);
            this.device.setEnergyData(energyData);
        } else {
            this.energyData = new TapoEnergyData();
        }
        this.device.responsePasstrough(responseBody);
    }

    /**
     * handle custom response
     * 
     * @param responseBody String with responseBody from device
     */
    @Override
    protected void handleCustomResponse(String responseBody) {
        this.device.responsePasstrough(responseBody);
    }

    /**
     * handle error
     * 
     * @param te TapoErrorHandler
     */
    @Override
    protected void handleError(TapoErrorHandler tapoError) {
        this.device.setError(tapoError);
    }

    /**
     * get Json from response
     * 
     * @param responseBody
     * @return JsonObject with result
     */
    private JsonObject getJsonFromResponse(String responseBody) {
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        /* get errocode (0=success) */
        if (jsonObject != null) {
            Integer errorCode = jsonObjectToInt(jsonObject, "error_code");
            if (errorCode == 0) {
                /* decrypt response */
                jsonObject = gson.fromJson(responseBody, JsonObject.class);
                logger.trace("({}) received result: {}", uid, responseBody);
                if (jsonObject != null) {
                    /* return result if set / else request was successful */
                    if (jsonObject.has("result")) {
                        return jsonObject.getAsJsonObject("result");
                    } else {
                        return jsonObject;
                    }
                }
            } else {
                /* return errorcode from device */
                TapoErrorHandler te = new TapoErrorHandler(errorCode, "device answers with errorcode");
                logger.debug("({}) device answers with errorcode {} - {}", uid, errorCode, te.getMessage());
                handleError(te);
                return jsonObject;
            }
        }
        logger.debug("({}) sendPayload exception {}", uid, responseBody);
        handleError(new TapoErrorHandler(ERR_HTTP_RESPONSE));
        return new JsonObject();
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
                handleError(new TapoErrorHandler(ERR_DEVICE_OFFLINE));
            }
            logout();
            return false;
        }
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
