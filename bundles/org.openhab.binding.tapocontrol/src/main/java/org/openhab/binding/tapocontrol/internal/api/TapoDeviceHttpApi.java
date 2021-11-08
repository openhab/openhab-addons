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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tapocontrol.internal.device.TapoDevice;
import org.openhab.binding.tapocontrol.internal.helpers.PayloadBuilder;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCipher;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Smart Home device connections.
 * This class uses synchronous HttpClient-Requests for login to device
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDeviceHttpApi {
    private final Logger logger = LoggerFactory.getLogger(TapoDeviceHttpApi.class);
    private final TapoErrorHandler tapoError;
    private final String uid;
    private final HttpClient httpClient;
    private final TapoCipher tapoCipher;
    private Gson gson;
    private TapoCredentials credentials;
    private String token = "";
    private String cookie = "";
    protected String deviceURL = "";
    protected String ipAddress = "";

    /**
     * INIT CLASS
     *
     * @param config TapoControlConfiguration class
     */
    public TapoDeviceHttpApi(TapoDevice device, TapoCredentials credentials, HttpClient httpClient) {
        String ipAddress = device.getIpAddress();
        setDeviceURL(ipAddress);
        this.credentials = credentials;
        this.httpClient = httpClient;
        this.tapoCipher = new TapoCipher();
        this.gson = new Gson();
        this.tapoError = new TapoErrorHandler();
        this.uid = device.getThingUID().getAsString();
    }

    /***********************************
     *
     * LOGIN FUNCTIONS
     *
     ************************************/

    /**
     * Create Handshake and set cookie
     *
     * @return true if handshake (cookie) was created
     */
    protected String createHandshake() {
        String cookie = "";
        try {
            /* create payload for handshake */
            PayloadBuilder plBuilder = new PayloadBuilder();
            plBuilder.method = "handshake";
            plBuilder.addParameter("key", credentials.getPublicKey()); // ?.decode("UTF-8")
            String payload = plBuilder.getPayload();

            /* send request (create ) */
            logger.trace("({}) create handhsake with payload: {}", uid, payload.toString());
            ContentResponse response = sendRequest(this.deviceURL, payload);
            if (response != null && getErrorCode(response) == 0) {
                String encryptedKey = getKeyFromResponse(response);
                this.tapoCipher.setKey(encryptedKey, this.credentials);
                cookie = getCookieFromResponse(response);
            }
        } catch (Exception e) {
            logger.debug("({}) could not createHandshake: {}", uid, e.toString());
            tapoError.raiseError(ERR_HAND_SHAKE_FAILED, "could not createHandshake");
        }
        return cookie;
    }

    /**
     * return encrypted key from 'handshake' request
     * 
     * @param response ContentResponse from "handshake" method
     * @return
     */
    private String getKeyFromResponse(ContentResponse response) {
        String encryptedKey = "";
        String rBody = response.getContentAsString();
        try {
            JsonObject jsonObj = gson.fromJson(rBody, JsonObject.class);
            logger.trace("({}) received awnser: {}", uid, rBody);

            encryptedKey = jsonObj.getAsJsonObject("result").get("key").getAsString();
        } catch (Exception e) {
            logger.warn("({}) could not getKeyFromResponse '{}'", uid, rBody);
            tapoError.raiseError(ERR_HAND_SHAKE_FAILED, "could not getKeyFromResponse");
        }
        return encryptedKey;
    }

    /**
     * return cookie from 'handshake' request
     * 
     * @param response ContentResponse from "handshake" metho
     * @return
     */
    private String getCookieFromResponse(ContentResponse response) {
        String cookie = "";
        try {
            cookie = response.getHeaders().get("Set-Cookie").split(";")[0];
            logger.trace("({}) got cookie: '{}'", uid, cookie);
        } catch (Exception e) {
            logger.warn("({}) could not getCookieFromResponse", uid);
            tapoError.raiseError(ERR_HAND_SHAKE_FAILED, "could not getCookieFromResponse");
        }
        return cookie;
    }

    /**
     * Query Token from device
     * 
     * @return String with token returned from device
     */
    protected String queryToken() {
        String token = "";
        try {
            /* encrypt login credentials */
            PayloadBuilder plBuilder = new PayloadBuilder();
            plBuilder.method = "login_device";
            plBuilder.addParameter("username", this.credentials.getEncodedEmail());
            plBuilder.addParameter("password", this.credentials.getEncodedPassword());
            String payload = plBuilder.getPayload();
            String encryptedPayload = this.encryptPayload(payload);

            /* create secured login informations */
            plBuilder = new PayloadBuilder();
            plBuilder.method = "securePassthrough";
            plBuilder.addParameter("request", encryptedPayload);
            String securePassthroughPayload = plBuilder.getPayload();

            /* sendRequest and get Token */
            ContentResponse response = sendRequest(deviceURL, securePassthroughPayload);
            token = getTokenFromResponse(response);
        } catch (Exception e) {
            logger.debug("({}) error building login payload: {}", uid, e.toString());
            tapoError.raiseError(e, "error building login payload");
        }
        return token;
    }

    /**
     * get Token from "login"-request
     * 
     * @param response
     * @return
     */
    private String getTokenFromResponse(@Nullable ContentResponse response) {
        String result = "";
        if (response.getStatus() == 200) {
            String rBody = response.getContentAsString();
            String decryptedResponse = this.decryptResponse(rBody);
            logger.trace("({}) received result: {}", uid, decryptedResponse);

            /* get errocode (0=success) */
            try {
                JsonObject jsonObject = gson.fromJson(decryptedResponse, JsonObject.class);
                Integer errorCode = jsonObject.get("error_code").getAsInt();
                if (errorCode == 0) {
                    /* return result if set / else request was successfull */
                    result = jsonObject.getAsJsonObject("result").get("token").getAsString();
                } else {
                    /* return errorcode from device */
                    tapoError.raiseError(errorCode, "could not get token");
                    logger.debug("({}) login recieved errorCode {} - {}", uid, errorCode, tapoError.getMessage());
                }
            } catch (Exception e) {
                tapoError.raiseError(e, "could not get token");
                logger.debug("({}) unexpected json-response '{}'", uid, decryptedResponse);
            }
        } else {
            logger.debug("({}) invalid response while login", uid);
            tapoError.raiseError(ERR_HTTP_RESPONSE, "invalid response while login");
        }
        return result;
    }

    /***********************************
     *
     * HTTP-ACTIONS
     *
     ************************************/
    /**
     * SEND SYNCHRON HTTP-REQUEST
     * 
     * @param url url request is sent to
     * @param payload payload (String) to send
     * @return ContentResponse of request
     */
    @Nullable
    protected ContentResponse sendRequest(String url, String payload) {
        logger.trace("({}) sendRequest to '{}' with cookie '{}'", uid, url, this.cookie);
        tapoError.reset();

        Request httpRequest = httpClient.newRequest(url).method(HttpMethod.POST.toString());

        /* set header */
        httpRequest = setHeaders(httpRequest);
        httpRequest.timeout(TAPO_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        /* add request body */
        httpRequest.content(new StringContentProvider(payload, CONTENT_CHARSET), CONTENT_TYPE_JSON);

        try {
            ContentResponse httpResponse = httpRequest.send();
            return httpResponse;
        } catch (InterruptedException e) {
            logger.debug("({}) sending request interrupted: {}", uid, e.toString());
            tapoError.raiseError(e);
        } catch (TimeoutException e) {
            logger.debug("({}) sending request timeout: {}", uid, e.toString());
            tapoError.raiseError(ERR_CONNECT_TIMEOUT, e.toString());
        } catch (Exception e) {
            logger.debug("({}) sending request failed: {}", uid, e.toString());
            tapoError.raiseError(e);
        }
        return null;
    }

    /**
     * SEND ASYNCHRONOUS HTTP-REQUEST
     * (don't wait for awnser with programm code)
     * 
     * @param url string url request is sent to
     * @param payload data-payload
     * @param command command executed - this will handle RepsonseType
     */
    protected void sendAsyncRequest(String url, String payload, String command) {
        logger.trace("({}) sendAsncRequest to '{}' with cookie '{}'", uid, url, this.cookie);
        try {
            Request httpRequest = httpClient.newRequest(url).method(HttpMethod.POST.toString());

            /* set header */
            httpRequest = setHeaders(httpRequest);

            /* add request body */
            httpRequest.content(new StringContentProvider(payload, CONTENT_CHARSET), CONTENT_TYPE_JSON);

            httpRequest.timeout(TAPO_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS).send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    final HttpResponse response = (HttpResponse) result.getResponse();
                    if (result.getFailure() != null) {
                        /* handle result errors */
                        Throwable e = result.getFailure();
                        if (e instanceof TimeoutException) {
                            logger.debug("({}) sendAsyncRequest timeout'{}'", uid, e.getMessage().toString());
                            tapoError.raiseError(ERR_CONNECT_TIMEOUT, e.getMessage().toString());
                        } else {
                            logger.debug("({}) sendAsyncRequest failed'{}'", uid, e.getMessage().toString());
                            tapoError.raiseError(ERR_JSON_DECODE_FAIL, e.getMessage().toString());
                        }
                    } else if (response.getStatus() != 200) {
                        logger.debug("({}) sendAsyncRequest response error'{}'", uid, response.getStatus());
                        tapoError.raiseError(ERR_HTTP_RESPONSE, getContentAsString());
                    } else {
                        /* request succesfull */
                        String rBody = getContentAsString();
                        logger.trace("({}) requestCompleted '{}'", uid, rBody);
                        /* handle result */
                        switch (command) {
                            case DEVICE_CMD_SETINFO:
                                handleSuccessResponse(rBody);
                                break;
                            case DEVICE_CMD_GETINFO:
                                handleDeviceResult(rBody);
                                break;
                        }
                    }
                    /* request error */
                    if (tapoError.hasError()) {
                        handleErrorResult(tapoError);
                    }
                }
            });
        } catch (Exception e) {
            tapoError.raiseError(e);
        }
        if (tapoError.hasError()) {
            handleErrorResult(tapoError);
        }
    }

    protected void handleSuccessResponse(String responseBody) {
    }

    protected void handleDeviceResult(String responseBody) {
    }

    protected void handleErrorResult(TapoErrorHandler te) {
    }

    protected void handleErrorResult(Exception ex) {
    }

    /**
     * return error code from response
     * 
     * @param response
     * @return 0 if request was successfull
     */
    protected Integer getErrorCode(@Nullable ContentResponse response) {
        try {
            String responseBody = response.getContentAsString();
            return getErrorCode(responseBody);
        } catch (Exception e) {
            return ERR_HTTP_RESPONSE;
        }
    }

    /**
     * return error code from responseBody
     * 
     * @param responseBody
     * @return 0 if request was successfull
     */
    protected Integer getErrorCode(String responseBody) {
        try {
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            /* get errocode (0=success) */
            Integer errorCode = jsonObject.get("error_code").getAsInt();
            if (errorCode == 0) {
                return 0;
            } else {
                logger.debug("({}) device returns errorcode '{}'", uid, errorCode);
                tapoError.raiseError(errorCode);
                return errorCode;
            }
        } catch (Exception e) {
            return ERR_HTTP_RESPONSE;
        }
    }

    /**
     * SET HTTP-HEADERS
     */
    private Request setHeaders(Request httpRequest) {
        /* set header */
        httpRequest.header("content-type", CONTENT_TYPE_JSON);
        httpRequest.header("Accept", CONTENT_TYPE_JSON);
        if (!this.cookie.isEmpty()) {
            httpRequest.header(HTTP_AUTH_TYPE_COOKIE, this.cookie);
        }
        return httpRequest;
    }

    /***********************************
     *
     * ENCRYPTION / CODING
     *
     ************************************/

    /**
     * Decrypt Response
     * 
     * @param responseBody encrypted string from response-body
     * @return String decrypted responseBody
     */
    protected String decryptResponse(String responseBody) {
        try {
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            String encryptedResponse = jsonObject.getAsJsonObject("result").get("response").getAsString();
            String decryptedResponse = tapoCipher.decode(encryptedResponse);
            return decryptedResponse;
        } catch (Exception ex) {
            logger.debug("({}) exception decryptingResponse '{}'", uid, ex.toString());
            return "";
        }
    }

    /**
     * encrypt payload
     * 
     * @param payload
     * @return encrypted payload
     */
    protected String encryptPayload(String payload) {
        try {
            return tapoCipher.encode(payload);
        } catch (Exception ex) {
            logger.debug("({}) exception encoding Payload '{}'", uid, ex.toString());
            return "";
        }
    }

    /**
     * perform logout (dispose cookie)
     */
    public void logout() {
        logger.trace("DeviceHttpApi_logout");
        unsetToken();
        unsetCookie();
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
        return loggedIn(false);
    }

    /**
     * Logged In
     * 
     * @param raiseError if true
     * @return true if logged in
     */
    public Boolean loggedIn(Boolean raiseError) {
        if (!this.token.isBlank() && !this.cookie.isBlank()) {
            return true;
        } else {
            logger.trace("({}) not logged in", uid);
            if (raiseError) {
                tapoError.raiseError(ERR_LOGIN);
            }
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
    public void setDeviceURL(String ipAddress) {
        this.ipAddress = ipAddress;
        this.deviceURL = String.format(TAPO_DEVICE_URL, ipAddress);
    }

    /**
     * Set new ipAdresss with token
     * 
     * @param ipAddress ipAddres of device
     * @param token token from login-ressult
     */
    public void setDeviceURL(String ipAddress, String token) {
        this.ipAddress = ipAddress;
        this.deviceURL = String.format(TAPO_DEVICE_URL, ipAddress);
    }

    /**
     * Set new token
     * 
     * @param deviceURL
     * @param token
     */
    protected void setToken(String token) {
        if (!token.isBlank()) {
            String url = this.deviceURL.replaceAll("\\?token=\\w*", "");
            this.deviceURL = url + "?token=" + token;
        }
        this.token = token;
    }

    /**
     * Unset Token (device logout)
     */
    protected void unsetToken() {
        this.deviceURL = this.deviceURL.replaceAll("\\?token=\\w*", "");
        this.token = "";
    }

    /**
     * Set new cookie
     * 
     * @param cookie
     */
    protected void setCookie(String cookie) {
        this.cookie = cookie;
    }

    /**
     * Unset Cookie (device logout)
     */
    protected void unsetCookie() {
        this.httpClient.getCookieStore().removeAll();
        this.cookie = "";
    }
}
