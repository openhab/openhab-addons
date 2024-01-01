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
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tapocontrol.internal.device.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.device.TapoDevice;
import org.openhab.binding.tapocontrol.internal.helpers.PayloadBuilder;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCipher;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Smart Home device connections.
 * This class uses synchronous HttpClient-Requests for login to device
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDeviceHttpApi {
    protected static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private final Logger logger = LoggerFactory.getLogger(TapoDeviceHttpApi.class);
    private final TapoCipher tapoCipher;
    private final TapoBridgeHandler bridge;
    protected final String uid;
    protected final TapoDevice device;

    private String token = "";
    private String cookie = "";
    protected String deviceURL = "";
    protected String ipAddress = "";

    /**
     * INIT CLASS
     *
     * @param device
     * @param bridgeThingHandler
     */
    public TapoDeviceHttpApi(TapoDevice device, TapoBridgeHandler bridgeThingHandler) {
        this.bridge = bridgeThingHandler;
        this.tapoCipher = new TapoCipher();
        this.device = device;
        this.uid = device.getThingUID().getAsString();
        setDeviceURL(device.getIpAddress());
    }

    /***********************************
     *
     * DELEGATING FUNCTIONS
     * will normaly be delegated to extension-classes(TapoDeviceConnector)
     *
     ************************************/
    /**
     * handle SuccessResponse (setDeviceInfo)
     *
     * @param responseBody String with responseBody from device
     */
    protected void handleSuccessResponse(String responseBody) {
    }

    /**
     * handle JsonResponse (getDeviceInfo)
     *
     * @param responseBody String with responseBody from device
     */
    protected void handleDeviceResult(String responseBody) {
    }

    /**
     * handle JsonResponse (getEnergyData)
     *
     * @param responseBody String with responseBody from device
     */
    protected void handleEnergyResult(String responseBody) {
    }

    /**
     * handle custom response
     *
     * @param responseBody String with responseBody from device
     */
    protected void handleCustomResponse(String responseBody) {
    }

    /**
     * handle JsonResponse (getChildDevices)
     *
     * @param responseBody String with responseBody from device
     */
    protected void handleChildDevices(String responseBody) {
    }

    /**
     * handle error
     *
     * @param tapoError TapoErrorHandler
     */
    protected void handleError(TapoErrorHandler tapoError) {
    }

    /**
     * refresh the list of child devices
     *
     */
    protected void queryChildDevices() {
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
            plBuilder.addParameter("key", bridge.getCredentials().getPublicKey()); // ?.decode("UTF-8")
            String payload = plBuilder.getPayload();

            /* send request (create ) */
            logger.trace("({}) create handhsake with payload: {}", uid, payload);
            ContentResponse response = sendRequest(this.deviceURL, payload);
            if (response != null && getErrorCode(response) == 0) {
                String encryptedKey = getKeyFromResponse(response);
                this.tapoCipher.setKey(encryptedKey, bridge.getCredentials());
                cookie = getCookieFromResponse(response);
            }
        } catch (Exception e) {
            logger.debug("({}) could not createHandshake: {}", uid, e.toString());
            handleError(new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED, "could not createHandshake"));
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
        String rBody = response.getContentAsString();
        JsonObject jsonObj = GSON.fromJson(rBody, JsonObject.class);
        if (jsonObj != null) {
            logger.trace("({}) received awnser: {}", uid, rBody);
            return jsonObjectToString(jsonObj.getAsJsonObject("result"), "key");
        } else {
            logger.warn("({}) could not getKeyFromResponse '{}'", uid, rBody);
            handleError(new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED, "could not getKeyFromResponse"));
        }
        return "";
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
            handleError(new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED, "could not getCookieFromResponse"));
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
            plBuilder.addParameter("username", bridge.getCredentials().getEncodedEmail());
            plBuilder.addParameter("password", bridge.getCredentials().getEncodedPassword());
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
            handleError(new TapoErrorHandler(e, "error building login payload"));
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
        TapoErrorHandler tapoError = new TapoErrorHandler();
        if (response != null && response.getStatus() == 200) {
            String rBody = response.getContentAsString();
            String decryptedResponse = this.decryptResponse(rBody);
            logger.trace("({}) received result: {}", uid, decryptedResponse);

            /* get errocode (0=success) */
            JsonObject jsonObject = GSON.fromJson(decryptedResponse, JsonObject.class);
            if (jsonObject != null) {
                Integer errorCode = jsonObjectToInt(jsonObject, "error_code", ERR_API_JSON_DECODE_FAIL.getCode());
                if (errorCode == 0) {
                    /* return result if set / else request was successful */
                    result = jsonObjectToString(jsonObject.getAsJsonObject("result"), "token");
                } else {
                    /* return errorcode from device */
                    tapoError.raiseError(errorCode, "could not get token");
                    logger.debug("({}) login recieved errorCode {} - {}", uid, errorCode, tapoError.getMessage());
                }
            } else {
                logger.debug("({}) unexpected json-response '{}'", uid, decryptedResponse);
                tapoError.raiseError(ERR_API_JSON_ENCODE_FAIL, "could not get token");
            }
        } else {
            logger.debug("({}) invalid response while login", uid);
            tapoError.raiseError(ERR_BINDING_HTTP_RESPONSE, "invalid response while login");
        }
        /* handle error */
        if (tapoError.hasError()) {
            handleError(tapoError);
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

        Request httpRequest = bridge.getHttpClient().newRequest(url).method(HttpMethod.POST.toString());

        /* set header */
        httpRequest = setHeaders(httpRequest);
        httpRequest.timeout(TAPO_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        /* add request body */
        httpRequest.content(new StringContentProvider(payload, CONTENT_CHARSET), CONTENT_TYPE_JSON);

        try {
            return httpRequest.send();
        } catch (InterruptedException e) {
            logger.debug("({}) sending request interrupted: {}", uid, e.toString());
            handleError(new TapoErrorHandler(e));
        } catch (TimeoutException e) {
            logger.debug("({}) sending request timeout: {}", uid, e.toString());
            handleError(new TapoErrorHandler(ERR_BINDING_CONNECT_TIMEOUT, e.toString()));
        } catch (Exception e) {
            logger.debug("({}) sending request failed: {}", uid, e.toString());
            handleError(new TapoErrorHandler(e));
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
        logger.trace("({}) command/payload: '{}''{}'", uid, command, payload);

        try {
            Request httpRequest = bridge.getHttpClient().newRequest(url).method(HttpMethod.POST.toString());

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
                        String errorMessage = getValueOrDefault(e.getMessage(), "");
                        if (e instanceof TimeoutException) {
                            logger.debug("({}) sendAsyncRequest timeout'{}'", uid, errorMessage);
                            handleError(new TapoErrorHandler(ERR_BINDING_CONNECT_TIMEOUT, errorMessage));
                        } else {
                            logger.debug("({}) sendAsyncRequest failed'{}'", uid, errorMessage);
                            handleError(new TapoErrorHandler(new Exception(e), errorMessage));
                        }
                    } else if (response.getStatus() != 200) {
                        logger.debug("({}) sendAsyncRequest response error'{}'", uid, response.getStatus());
                        handleError(new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, getContentAsString()));
                    } else {
                        /* request successful */
                        String rBody = getContentAsString();
                        logger.trace("({}) receivedRespose '{}'", uid, rBody);
                        if (!hasErrorCode(rBody)) {
                            rBody = decryptResponse(rBody);
                            logger.trace("({}) decryptedResponse '{}'", uid, rBody);
                            /* handle result */
                            switch (command) {
                                case DEVICE_CMD_SETINFO:
                                    handleSuccessResponse(rBody);
                                    break;
                                case DEVICE_CMD_GETINFO:
                                    handleDeviceResult(rBody);
                                    break;
                                case DEVICE_CMD_GETENERGY:
                                    handleEnergyResult(rBody);
                                    break;
                                case DEVICE_CMD_CUSTOM:
                                    handleCustomResponse(rBody);
                                    break;
                                case DEVICE_CMD_CHILD_DEVICE_LIST:
                                    handleChildDevices(rBody);
                                    break;
                            }
                        } else {
                            getErrorCode(rBody);
                        }
                    }
                }
            });
        } catch (Exception e) {
            handleError(new TapoErrorHandler(e));
        }
    }

    /**
     * return error code from response
     *
     * @param response
     * @return 0 if request was successfull
     */
    protected Integer getErrorCode(@Nullable ContentResponse response) {
        try {
            if (response != null) {
                String responseBody = response.getContentAsString();
                return getErrorCode(responseBody);
            } else {
                return ERR_BINDING_HTTP_RESPONSE.getCode();
            }
        } catch (Exception e) {
            return ERR_BINDING_HTTP_RESPONSE.getCode();
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
            JsonObject jsonObject = GSON.fromJson(responseBody, JsonObject.class);
            /* get errocode (0=success) */
            Integer errorCode = jsonObjectToInt(jsonObject, "error_code", ERR_API_JSON_DECODE_FAIL.getCode());
            if (errorCode == 0) {
                return 0;
            } else {
                logger.debug("({}) device returns errorcode '{}'", uid, errorCode);
                handleError(new TapoErrorHandler(errorCode));
                return errorCode;
            }
        } catch (Exception e) {
            return ERR_BINDING_HTTP_RESPONSE.getCode();
        }
    }

    /**
     * Check for JsonObject "errorcode" and if this is > 0 (no Error)
     *
     * @param responseBody
     * @return true if is js errorcode > 0; false if there is no "errorcode"
     */
    protected Boolean hasErrorCode(String responseBody) {
        if (isValidJson(responseBody)) {
            JsonObject jsonObject = GSON.fromJson(responseBody, JsonObject.class);
            /* get errocode (0=success) */
            Integer errorCode = jsonObjectToInt(jsonObject, "error_code", ERR_API_JSON_DECODE_FAIL.getCode());
            if (errorCode > 0) {
                return true;
            }
        }
        return false;
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
            JsonObject jsonObject = GSON.fromJson(responseBody, JsonObject.class);
            if (jsonObject != null) {
                String encryptedResponse = jsonObjectToString(jsonObject.getAsJsonObject("result"), "response");
                return tapoCipher.decode(encryptedResponse);
            } else {
                handleError(new TapoErrorHandler(ERR_API_JSON_DECODE_FAIL));
            }
        } catch (Exception ex) {
            logger.debug("({}) exception '{}' decryptingResponse: '{}'", uid, ex.toString(), responseBody);
        }
        return responseBody;
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
                handleError(new TapoErrorHandler(ERR_API_LOGIN));
            }
            return false;
        }
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    /**
     * Set new ipAddress
     *
     * @param ipAddress new ipAdress
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
        bridge.getHttpClient().getCookieStore().removeAll();
        this.cookie = "";
    }
}
