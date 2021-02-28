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
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.device.TapoDeviceInfo;
import org.openhab.binding.tapocontrol.internal.helpers.MimeEncode;
import org.openhab.binding.tapocontrol.internal.helpers.PayloadBuilder;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCipher;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tapocontrol.internal.helpers.TapoHttp;
import org.openhab.binding.tapocontrol.internal.helpers.TapoHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Connection
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoConnector {

    private final Logger logger = LoggerFactory.getLogger(TapoConnector.class);
    private final TapoErrorHandler tapoError = new TapoErrorHandler();
    private TapoHttp tapoHttp;
    private Gson gson;
    private TapoCredentials credentials;
    private TapoCipher tapoCipher;
    private String ipAddress = "";
    private String token = "";
    private String cookie = "";
    private String deviceURL = "";
    private Integer errorCode = 0;

    /**
     * INIT CLASS
     *
     * @param config TapoControlConfiguration class
     */
    public TapoConnector(String ipAddress, TapoCredentials credentials) {
        this.credentials = credentials;
        this.tapoCipher = new TapoCipher();
        this.gson = new Gson();
        this.tapoHttp = new TapoHttp();
        setIpAddress(ipAddress);
    }

    /***********************************
     *
     * ENCRYPTION / CODING
     *
     ************************************/

    /**
     * Create Handshake and set cookie
     *
     * @return true if handshake (cookie) was created
     */
    private Boolean createHandshake() {
        this.tapoHttp = new TapoHttp();
        String encryptedKey = "";

        /* create payload for handshake */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = "handshake";
        plBuilder.addParameter("key", credentials.getPublicKey()); // ?.decode("UTF-8")
        String payload = plBuilder.getPayload();

        /* send request (perform login) */
        logger.trace("create handhsake with payload: " + payload.toString());
        tapoHttp.url = deviceURL;
        tapoHttp.setRequest(payload);

        try {
            TapoHttpResponse response = tapoHttp.send();

            String rBody = response.getResponseBody();
            JsonObject jsonObj = gson.fromJson(rBody, JsonObject.class);
            try {
                encryptedKey = jsonObj.getAsJsonObject("result").get("key").getAsString();
            } catch (Exception e) {
                // TODO: handle exception
            }

            setCipher(encryptedKey);
            this.cookie = response.getResponseHeader("Set-Cookie").split(";")[0];
            return true;
        } catch (Exception ex) {
            logger.debug("Something went wrong: " + ex.getMessage());
            tapoError.raiseError(ex, "could not create handshake");
            this.cookie = "";
            return false;
        }
    }

    /**
     * Create Cipher ( Decode Handshake )
     *
     * @param key encrypted key
     * @return true if success
     */
    private Boolean setCipher(String key) {
        logger.trace("Will try to decode the following key: " + key);

        MimeEncode mimeEncode = new MimeEncode();

        try {
            byte[] decode = mimeEncode.decode(key.getBytes("UTF-8"));
            byte[] decode2 = mimeEncode.decode(credentials.getPrivateKeyBytes());
            Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey p = kf.generatePrivate(new PKCS8EncodedKeySpec(decode2));
            instance.init(Cipher.DECRYPT_MODE, p);
            byte[] doFinal = instance.doFinal(decode);
            byte[] bArr = new byte[16];
            byte[] bArr2 = new byte[16];
            System.arraycopy(doFinal, 0, bArr, 0, 16);
            System.arraycopy(doFinal, 16, bArr2, 0, 16);
            this.tapoCipher = new TapoCipher(bArr, bArr2);
            return true;
        } catch (Exception ex) {
            logger.warn("Something went wrong: " + ex.getMessage());
            tapoError.raiseError(ex);
            return false;
        }
    }

    /**
     * Decrypt Response
     * 
     * @param responseBody encrypted string from response-body
     * @return String decrypted responseBody
     */
    private String decryptResponse(String responseBody) {
        try {
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            String encryptedResponse = jsonObject.getAsJsonObject("result").get("response").getAsString();
            String decryptedResponse = tapoCipher.decode(encryptedResponse);
            return decryptedResponse;
        } catch (Exception ex) {
            return "";
        }
    }

    /***********************************
     *
     * HTTP ACTIONS
     *
     ************************************/

    /**
     * login
     *
     * @param name Name of command to send
     * @param value Value to send to control
     * @return true if success
     */
    public boolean login() {
        logger.trace("sending login");

        String securePassthroughPayload = "";
        tapoError.reset(); // reset ErrorHandler

        /* ping device */
        if (!pingDevice()) {
            tapoError.raiseError(ERROR_DEVICE_OFFLINE, "device offline while login");
            logout();
            return false;
        }

        /* create handschake (cookie) */
        this.createHandshake();

        if (cookie != "") {
            try {
                /* encrypt login credentials */
                PayloadBuilder plBuilder = new PayloadBuilder();
                plBuilder.method = "login_device";
                plBuilder.addParameter("username", this.credentials.getEncodedEmail());
                plBuilder.addParameter("password", this.credentials.getEncodedPassword());
                String payload = plBuilder.getPayload();
                String encryptedPayload = tapoCipher.encode(payload);

                /* create secured login informations */
                plBuilder = new PayloadBuilder();
                plBuilder.method = "securePassthrough";
                plBuilder.addParameter("request", encryptedPayload);
                securePassthroughPayload = plBuilder.getPayload();
            } catch (Exception ex) {
                logger.trace("error building payload '{}'", ex.toString());
                tapoError.raiseError(ex, "error building payload for login request");
                return false;
            }

            /* send request (perform login) */
            tapoHttp.url = deviceURL;
            tapoHttp.cookie = this.cookie;
            tapoHttp.request = securePassthroughPayload;
            TapoHttpResponse response = tapoHttp.send();

            /* work with response */
            if (response.responseIsOK()) {
                String rBody = response.getResponseBody();
                String decryptedResponse = this.decryptResponse(rBody);
                JsonObject jsonObject = gson.fromJson(decryptedResponse, JsonObject.class);
                try {
                    this.token = jsonObject.getAsJsonObject("result").get("token").getAsString();
                } catch (Exception e) {
                    this.errorCode = Integer
                            .parseInt(jsonObject.getAsJsonObject("result").get("error_code").getAsString());
                    logger.trace("enexpected json-response '{}'", decryptedResponse);
                    tapoError.raiseError(errorCode, "could not get token");
                }
            } else {
                logger.trace("login failed");
                tapoError.raiseError(ERROR_RESPONSE, "invalid response while login");
                this.token = "";
            }
        } else {
            logger.trace("cookie error");
            tapoError.raiseError(ERROR_COOKIE, "cookie bot set while login");
            this.token = "";
        }
        return this.loggedIn();
    }

    /**
     * sendPayload
     *
     * @param PayloadBuilder payload
     * @return JsonObject with response
     */
    @Nullable
    protected JsonObject sendPayload(PayloadBuilder plBuilder) {
        String payload = plBuilder.getPayload();
        logger.trace("sending payload '{}'", payload);
        tapoError.reset(); // reset ErrorHandler

        String securePassthroughPayload = "";
        String url = deviceURL + "?token=" + this.token;

        if (checkConnection(true)) {
            try {
                /* encrypt command payload */
                String encryptedPayload = this.tapoCipher.encode(payload);

                /* create secured payload */
                plBuilder = new PayloadBuilder();
                plBuilder.method = "securePassthrough";
                plBuilder.addParameter("request", encryptedPayload);
                securePassthroughPayload = plBuilder.getPayload();
            } catch (Exception ex) {
                logger.trace("error building payload '{}'", ex.toString());
                tapoError.raiseError(ex, "error building payload for send command");
                return tapoError.getJson();
            }

            /* send request */
            tapoHttp.url = url;
            tapoHttp.cookie = this.cookie;
            tapoHttp.request = securePassthroughPayload;
            TapoHttpResponse response = tapoHttp.send();

            /* work with response */
            if (response.responseIsOK()) {
                String rBody = response.getResponseBody();
                try {
                    JsonObject jsonObject = gson.fromJson(rBody, JsonObject.class);
                    /* decrypt response */
                    String decryptedResponse = decryptResponse(rBody);
                    jsonObject = gson.fromJson(decryptedResponse, JsonObject.class);
                    /* get errocode (0=success) */
                    Integer errorCode = jsonObject.get("error_code").getAsInt();
                    if (errorCode == 0) {
                        /* return result if set / else request was successfull */
                        if (jsonObject.has("result")) {
                            return jsonObject.getAsJsonObject("result");
                        } else {
                            return gson.fromJson("{ 'success': true }", JsonObject.class);
                        }
                    } else {
                        /* return errorcode from device */
                        tapoError.raiseError(errorCode, "device answers with errorcode");
                        return jsonObject;
                    }
                } catch (Exception e) {
                    tapoError.raiseError(e);
                }
            } else {
                tapoError.raiseError(ERROR_RESPONSE);
            }
        }
        return tapoError.getJson();
    }

    /**
     * set Device Info
     *
     * @param name Name of command to send
     * @param value Value to send to control
     * @return true if sent successfull ( no error returned )
     */
    public Boolean setDeviceInfo(String name, String value) {
        logger.trace("building command '{}' '{}'", name, value);

        /* encrypt command payload */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = "set_device_info";
        plBuilder.addParameter(name, value);
        return sendPayload(plBuilder).has("success");
    }

    /**
     * set Device Info
     *
     * @param name Name of command to send
     * @param value Value to send to control
     * @return true if sent successfull ( no error returned )
     */
    public Boolean setDeviceInfo(String name, Boolean value) {
        logger.trace("building command '{}' '{}'", name, value);

        /* encrypt command payload */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = "set_device_info";
        plBuilder.addParameter(name, value);
        return sendPayload(plBuilder).has("success");
    }

    /**
     * set Device Info
     *
     * @param name Name of command to send
     * @param value Value to send to control
     * @return true if sent successfull ( no error returned )
     */
    public Boolean setDeviceInfo(String name, Integer value) {
        logger.trace("building command '{}' '{}'", name, value);

        /* encrypt command payload */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = "set_device_info";
        plBuilder.addParameter(name, value);
        return sendPayload(plBuilder).has("success");
    }

    /**
     * Query Info from Device
     *
     * @return tapo device info object
     */
    public TapoDeviceInfo queryInfo() {
        /* encrypt command payload */
        PayloadBuilder plBuilder = new PayloadBuilder();
        plBuilder.method = "get_device_info";
        JsonObject result = sendPayload(plBuilder);

        if (result.has("device_id")) {
            return new TapoDeviceInfo(result);
        } else {
            return new TapoDeviceInfo();
        }
    }

    /**
     * CHECK CONNECTION AND LOGIN
     * 
     * @param raiseError raises tapoError if true
     * @return true if okay
     */
    protected Boolean checkConnection(Boolean raiseError) {
        if (!pingDevice()) {
            logger.trace("device is offline");
            if (raiseError) {
                tapoError.raiseError(ERROR_DEVICE_OFFLINE);
            }
            return false;
        }
        if (!loggedIn()) {
            logger.trace("not logged-in");
            if (raiseError) {
                tapoError.raiseError(ERROR_LOGIN);
            }
            return false;
        }
        return true;
    }

    /**
     * perform logout (dispose cookie)
     */
    protected void logout() {
        this.token = "";
        this.cookie = "";
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    /**
     * Set new ipAddress
     * 
     * @param new ipAdress
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        this.deviceURL = String.format(TAPO_DEVICE_URL, ipAddress);
    }

    /***********************************
     *
     * GET RESULTS
     *
     ************************************/
    /**
     * Logged In
     * 
     * @return true if logged in
     */
    public Boolean loggedIn() {
        if (isOnline()) {
            return this.token != "";
        } else {
            logout();
            return false;
        }
    }

    /**
     * Logged In
     * 
     * @param raiseError if true
     * @return true if logged in
     */
    public Boolean loggedIn(Boolean raiseError) {
        if (loggedIn()) {
            return true;
        } else {
            if (raiseError) {
                tapoError.raiseError(ERROR_LOGIN);
            }
            logout();
            return false;
        }
    }

    /**
     * Check if device is online
     * 
     * @return true if device is online
     */
    public Boolean isOnline() {
        return pingDevice();
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
            if (raiseError) {
                tapoError.raiseError(ERROR_DEVICE_OFFLINE);
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
     * ErrorCode
     * 
     * @return ErrorCode returned by device
     */
    public Integer errorCode() {
        return tapoError.getNumber();
    }

    /**
     * ErrorMessage
     * 
     * @return String Error text
     */
    public String errorMessage() {
        return tapoError.getMessage();
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
            return false;
        }
    }
}
