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
package org.openhab.binding.tapocontrol.internal.api.protocol.aes;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.JsonUtils.*;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoEncoder;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tapocontrol.internal.helpers.TapoKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handler class for TAPO-SECUREPASSTHROUGH-SESSION
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class SecurePassthroughSession {
    private static final int RSA_KEYSIZE = 1024;

    private final Logger logger = LoggerFactory.getLogger(SecurePassthroughSession.class);

    private final SecurePasstroughCipher cipher = new SecurePasstroughCipher();
    private final TapoKeyPair tapoKeyPair;
    private final SecurePassthrough spth;
    private String cookie = "";
    private String token = "";
    private String uid;

    // List of class-specific commands
    public static final String DEVICE_CMD_GET_TOKEN = "login_device";
    public static final String DEVICE_CMD_GET_COOKIE = "handshake";
    public static final String DEVICE_CMD_SECURE_METHOD = "securePassthrough";

    public SecurePassthroughSession(SecurePassthrough sptHandler) {
        tapoKeyPair = new TapoKeyPair(RSA_KEYSIZE);
        spth = sptHandler;
        uid = spth.httpDelegator.getThingUID() + " / SecurePassthrough-Session";
    }

    public void reset() {
        unsetCookie();
        unsetToken();
    }

    /***********************
     * Request Sender
     **********************/

    private ContentResponse sendHandshakeRequest(TapoRequest tapoRequest, boolean encrypt)
            throws TimeoutException, InterruptedException, ExecutionException, TapoErrorHandler {
        String url = spth.getUrl();
        logger.trace("({}) sending Handshake request: '{}' to '{}' ", uid, tapoRequest, url);
        Request httpRequest = spth.httpDelegator.getHttpClient().newRequest(url).method(HttpMethod.POST.toString());

        if (encrypt) {
            tapoRequest = encryptRequest(tapoRequest);
            logger.trace("({}) encrypted request: '{}' with cookie '{}'", uid, tapoRequest, cookie);
        }

        /* set header */
        httpRequest = spth.setHeaders(httpRequest);
        httpRequest.timeout(TAPO_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        /* add request body */
        httpRequest.content(new StringContentProvider(tapoRequest.toString(), CONTENT_CHARSET), CONTENT_TYPE_JSON);

        return httpRequest.send();
    }

    /************************
     * HANDSHAKE & COOKIE
     ************************/
    /**
     * Create Handshake
     */
    public boolean login(TapoCredentials credentials) throws TapoErrorHandler {
        reset();
        try {
            TapoCredentials encodedCredentials = encodeCredentials(credentials);
            createHandshake();
            createToken(encodedCredentials);
        } catch (TapoErrorHandler e) {
            throw e;
        } catch (Exception e) {
            throw new TapoErrorHandler(ERR_API_LOGIN_FAILED);
        }
        return isHandshakeComplete();
    }

    /************************
     * HANDSHAKE & COOKIE
     ************************/

    /**
     * Create Handshake and get cookie
     */
    private void createHandshake() throws TimeoutException, InterruptedException, ExecutionException, TapoErrorHandler {
        /* create handshake request */
        JsonObject json = new JsonObject();
        json.addProperty("key", tapoKeyPair.getPublicKey());
        TapoRequest handshakeRequest = new TapoRequest(DEVICE_CMD_GET_COOKIE, json);
        /* send request */
        logger.trace("({}) create handhsake with payload: {}", uid, handshakeRequest);
        ContentResponse response = sendHandshakeRequest(handshakeRequest, false);
        handleHandshakeResponse(response);
    }

    /**
     * work with response from handshake request
     * get cookie from request and set cipher
     */
    private void handleHandshakeResponse(ContentResponse response) throws TapoErrorHandler {
        /* setCookie */
        String result = response.getHeaders().get("Set-Cookie").split(";")[0];
        setCookie(result);

        TapoResponse tapoResponse = spth.getTapoResponse(response);
        if (!tapoResponse.hasError()) {
            String encryptedKey = tapoResponse.result().get("key").getAsString();
            cipher.setKey(encryptedKey, tapoKeyPair);
        } else {
            logger.debug("({}) could not createHandshake: {} ({})", uid, tapoResponse.message(),
                    tapoResponse.errorCode());
            throw new TapoErrorHandler(tapoResponse.errorCode(), tapoResponse.message());
        }
    }

    /**
     * query Token from device with encoded credentials
     */
    private void createToken(TapoCredentials encodedCredentials)
            throws TimeoutException, InterruptedException, ExecutionException, TapoErrorHandler {
        /* create handshake request */
        JsonObject json = new JsonObject();
        json.addProperty("username", encodedCredentials.username());
        json.addProperty("password", encodedCredentials.password());
        TapoRequest loginRequest = new TapoRequest(DEVICE_CMD_GET_TOKEN, json);

        /* send request */
        ContentResponse response = sendHandshakeRequest(loginRequest, true);
        handleTokenResponse(response);
    }

    /**
     * get token from "login"-request
     */
    private void handleTokenResponse(ContentResponse response) throws TapoErrorHandler {
        TapoResponse tapoResponse = spth.getTapoResponse(response);
        if (!tapoResponse.hasError()) {
            setToken(jsonObjectToString(tapoResponse.result(), "token"));
        } else {
            logger.debug("({}) invalid response while login: {} ({})", uid, tapoResponse.message(),
                    tapoResponse.errorCode());
            throw new TapoErrorHandler(tapoResponse.errorCode(), tapoResponse.message());
        }
    }

    private void setToken(String token) {
        this.token = token;
    }

    private void unsetToken() {
        token = "";
    }

    /**
     * Cookie Handling
     */
    private void setCookie(String cookie) {
        this.cookie = cookie;
    }

    private void unsetCookie() {
        spth.httpDelegator.getHttpClient().getCookieStore().removeAll();
        this.cookie = "";
    }

    /************************
     * GET VALUES
     ************************/

    public String getCookie() {
        return cookie;
    }

    public String getToken() {
        return token;
    }

    public SecurePasstroughCipher getCipher() {
        return cipher;
    }

    public boolean isHandshakeComplete() {
        return !cookie.isBlank() && !token.isBlank();
    }

    /***********************************
     * ENCRYPTION / CODING
     ************************************/

    private TapoCredentials encodeCredentials(TapoCredentials credentials) throws NoSuchAlgorithmException {
        String username = TapoEncoder.b64Encode(TapoEncoder.sha1Encode(credentials.username()));
        String password = TapoEncoder.b64Encode(credentials.password());
        return new TapoCredentials(username, password);
    }

    /**
     * Decrypt encrypted TapoResponse
     */
    public TapoResponse decryptResponse(TapoResponse response) throws TapoErrorHandler {
        if (response.result().has("response")) {
            try {
                String encryptedResponse = response.result().get("response").getAsString();
                String decryptedResponse = cipher.decode(encryptedResponse);
                logger.trace("({}) decrypted response '{}'", uid, decryptedResponse);
                return Objects.requireNonNull(GSON.fromJson(decryptedResponse, TapoResponse.class));
            } catch (Exception e) {
                logger.debug("({}) exception '{}' decryptingResponse: '{}'", uid, e, response);
                throw new TapoErrorHandler(ERR_DATA_DECRYPTING);
            }
        }
        throw new TapoErrorHandler(ERR_DATA_FORMAT);
    }

    /**
     * Encrypt Request
     */
    public TapoRequest encryptRequest(Object request) throws TapoErrorHandler {
        try {
            JsonObject jso = new JsonObject();
            jso.addProperty("request", cipher.encode(GSON.toJson(request)));
            return new TapoRequest(DEVICE_CMD_SECURE_METHOD, jso);
        } catch (Exception e) {
            logger.debug("({}) exception encoding Payload '{}'", uid, e.toString());
            throw new TapoErrorHandler(ERR_DATA_ENCRYPTING);
        }
    }
}
