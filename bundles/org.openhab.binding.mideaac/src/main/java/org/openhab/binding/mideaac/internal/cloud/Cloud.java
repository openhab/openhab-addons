/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.cloud;

import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.security.Security;
import org.openhab.binding.mideaac.internal.security.TokenKey;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link Cloud} class connects to the Cloud Provider
 * with user supplied information (or defaults) to retrieve the Security
 * Token and Key.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - JavaDoc and changed getQueryString for special
 *         characters to allow for default email with a "+" and
 *         to update the SmartHome cloud requirements.
 */
@NonNullByDefault
public class Cloud {
    private final Logger logger = LoggerFactory.getLogger(Cloud.class);

    private static final int CLIENT_TYPE = 1; // Android
    private static final int FORMAT = 2; // JSON
    private static final String LANGUAGE = "en_US";
    private static final String DEVICE_ID = StringUtils.getRandomString(16, "0123456789abcdef");

    private HttpClient httpClient;

    private String errMsg = "";

    private @Nullable String accessToken = "";

    private String loginAccount;
    private String password;
    private CloudProvider cloudProvider;
    private Security security;

    private @Nullable String loginId;
    private String sessionId = "";

    /**
     * Parameters for Cloud Provider
     * 
     * @param email email
     * @param password password
     * @param cloudProvider Cloud Provider
     * @param httpClient Used to send posts to the cloud
     */
    public Cloud(String email, String password, CloudProvider cloudProvider, HttpClient httpClient) {
        this.loginAccount = email;
        this.password = password;
        this.cloudProvider = cloudProvider;
        this.security = new Security(cloudProvider);
        this.httpClient = httpClient;
        logger.debug("Cloud provider: {}", cloudProvider.name());
    }

    /**
     * This is called during the loginId(), login() and getToken() methods to send a
     * HTTP Post to the cloud provider. There are two different messages
     * and formats separated by the type of Cloud Provider (proxied or Not).
     * The return is msg "ok", "errorCode":"0" (ideally).
     * There is also information for the next message or the key and token.
     */
    private @Nullable JsonObject apiRequest(String endpoint, @Nullable JsonObject args, @Nullable JsonObject data) {
        if (data == null) {
            data = new JsonObject();
            data.addProperty("appId", cloudProvider.appid());
            data.addProperty("format", FORMAT);
            data.addProperty("clientType", CLIENT_TYPE);
            data.addProperty("language", LANGUAGE);
            data.addProperty("deviceId", DEVICE_ID);
            data.addProperty("src", cloudProvider.appid());
            DateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ROOT);
            data.addProperty("stamp", fmt.format(new Date()));
        }

        // Add the proxy request ID before endpoint-specific arguments so the signed
        // request follows the same field order as the SmartHome client.
        if (!data.has("reqId") && !cloudProvider.proxied().isBlank()) {
            data.addProperty("reqId", StringUtils.getRandomString(32, "0123456789abcdef"));
        }

        // For the getLoginId() this adds the email account
        // For the login() this adds the email account and the encrypted password
        // For the getToken() this adds the udpid and deviceID
        if (args != null) {
            for (Map.Entry<String, JsonElement> entry : args.entrySet()) {
                data.add(entry.getKey(), entry.getValue());
            }
        }

        String url = cloudProvider.apiurl() + endpoint;
        logger.debug("Url for request {}", url);

        String json = data.toString();
        logger.debug("Request json: {}", json);

        int time = (int) (new Date().getTime() / 1000);
        String random = String.valueOf(time);

        Request request = httpClient.newRequest(url).method(HttpMethod.POST).timeout(15, TimeUnit.SECONDS);

        // .version(HttpVersion.HTTP_1_1)
        request.agent("Dalvik/2.1.0 (Linux; U; Android 7.0; SM-G935F Build/NRD90M)");

        if (!cloudProvider.proxied().isBlank()) {
            request.header("Content-Type", "application/json");
        } else {
            request.header("Content-Type", "application/x-www-form-urlencoded");
        }

        request.header("secretVersion", "1");

        request.header("random", random);

        // Add the sign to the header, different for proxied
        if (!cloudProvider.proxied().isBlank()) {
            String sign = security.newSign(json, random);
            request.header("sign", sign);
        } else {
            if (!Objects.isNull(sessionId) && !sessionId.isBlank()) {
                data.addProperty("sessionId", sessionId);
            }
            String sign = security.sign(url, data);
            data.addProperty("sign", sign);
            request.header("sign", sign);
        }

        // If available, blank if not
        request.header("accessToken", accessToken);

        logger.debug("Request headers: {}", request.getHeaders().toString());

        // Different formats for proxied or not
        if (!cloudProvider.proxied().isBlank()) {
            request.content(new StringContentProvider(json));
        } else {
            String body = Utils.getQueryString(data, false);
            logger.debug("Request body: {}", body);
            request.content(new StringContentProvider(body));
        }

        // POST the payload
        @Nullable
        ContentResponse cr = null;
        try {
            cr = request.send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt flag
            logger.warn("Request interrupted: {}", e.getMessage());
            return null; // Return quickly
        } catch (TimeoutException e) {
            logger.warn("Request timed out: {}", e.getMessage());
        } catch (ExecutionException e) {
            logger.warn("Request execution failed: {}", e.getMessage());
        }

        if (cr != null) {
            logger.debug("Response json: {}", cr.getContentAsString());
            JsonObject result = Objects.requireNonNull(new Gson().fromJson(cr.getContentAsString(), JsonObject.class));

            int code = -1;

            if (result.get("errorCode") != null) {
                code = result.get("errorCode").getAsInt();
            } else if (result.get("code") != null) {
                code = result.get("code").getAsInt();
            } else {
                errMsg = "No code in cloud response";
                logger.warn("Error logging to Cloud: {}", errMsg);
                return null;
            }

            String msg = result.get("msg").getAsString();
            if (code != 0) {
                errMsg = msg;
                logger.debug("Error {} logging to Cloud: {}", code, msg);
                throw new LoginFailedException("Login failed with error code " + code + ": " + msg);
            } else {
                logger.debug("Api response ok: {} ({})", code, msg);
                if (!cloudProvider.proxied().isBlank()) {
                    return result.get("data").getAsJsonObject();
                } else {
                    return result.get("result").getAsJsonObject();
                }
            }
        } else {
            logger.warn("No response from cloud!");
        }

        return null;
    }

    /**
     * First gets the loginId from the Cloud using the email, then gets the session
     * Id with the email and encypted password (using the LoginId). Then
     * gets the token and key. If loginId and sessionId exist from an earlier
     * attempt, it goes directly to getting the token and key.
     * 
     * @return true or false
     */
    public boolean login() {
        // First get the loginId using the your email
        if (loginId == null) {
            if (!getLoginId()) {
                return false;
            }
        }
        // No need to login again, skip to getToken() with device Id
        if (!Objects.isNull(sessionId) && !sessionId.isBlank()) {
            return true;
        }

        logger.trace("Using loginId: {}", loginId);

        if (!cloudProvider.proxied().isBlank()) {
            // This is for the (proxied) cloud
            JsonObject newData = new JsonObject();
            JsonObject data = new JsonObject();
            data.addProperty("platform", FORMAT);
            data.addProperty("deviceId", DEVICE_ID);
            newData.add("data", data);

            JsonObject iotData = new JsonObject();
            iotData.addProperty("appId", cloudProvider.appid());
            iotData.addProperty("clientType", CLIENT_TYPE);
            iotData.addProperty("iampwd", security.encryptIamPassword(loginId, password));
            iotData.addProperty("loginAccount", loginAccount);
            iotData.addProperty("password", security.encryptPassword(loginId, password));
            iotData.addProperty("pushToken", Utils.tokenUrlsafe(120));
            iotData.addProperty("reqId", StringUtils.getRandomString(32, "0123456789abcdef"));
            iotData.addProperty("src", cloudProvider.appid());
            DateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ROOT);
            iotData.addProperty("stamp", fmt.format(new Date()));
            newData.add("iotData", iotData);

            @Nullable
            JsonObject response = apiRequest("/mj/user/login", null, newData);
            if (response == null) {
                return false;
            }

            accessToken = response.getAsJsonObject("mdata").get("accessToken").getAsString();
        } else {
            // This for the non-proxied cloud providers
            String passwordEncrypted = security.encryptPassword(loginId, password);

            JsonObject data = new JsonObject();
            data.addProperty("loginAccount", loginAccount);
            data.addProperty("password", passwordEncrypted);

            JsonObject response = apiRequest("/v1/user/login", data, null);

            if (response == null) {
                return false;
            }

            accessToken = response.get("accessToken").getAsString();
            sessionId = response.get("sessionId").getAsString();
        }

        return true;
    }

    /**
     * Gets token and key with the device Id modified to udpid
     * after SessionId (non-proxied) accessToken is established
     * 
     * @param deviceId The AC Device ID to be modified
     * @return token and key
     */
    public TokenKey getToken(String deviceId, String tokenKeyMethod) {
        long i = Long.valueOf(deviceId);

        JsonObject args = new JsonObject();
        if ("LittleEndian".equals(tokenKeyMethod)) {
            args.addProperty("udpid", security.getUdpId(Utils.toIntTo6ByteArray(i, ByteOrder.LITTLE_ENDIAN)));
        } else {
            args.addProperty("udpid", security.getUdpId(Utils.toIntTo6ByteArray(i, ByteOrder.BIG_ENDIAN)));
        }

        args.addProperty("applianceCodes", deviceId);
        JsonObject response = apiRequest("/v1/iot/secure/getToken", args, null);

        if (response == null) {
            return new TokenKey("", "");
        }

        JsonArray tokenlist = response.getAsJsonArray("tokenlist");
        JsonObject el = tokenlist.get(0).getAsJsonObject();
        String token = el.getAsJsonPrimitive("token").getAsString();
        String key = el.getAsJsonPrimitive("key").getAsString();

        return new TokenKey(token, key);
    }

    /**
     * Gets the login ID from your email address
     * 
     * @return true or false
     */
    public boolean getLoginId() {
        JsonObject args = new JsonObject();
        args.addProperty("loginAccount", loginAccount);

        JsonObject response = apiRequest("/v1/user/login/id/get", args, null);
        if (response == null) {
            return false;
        }

        loginId = response.get("loginId").getAsString();
        return true;
    }
}
