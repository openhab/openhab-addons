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
package org.openhab.binding.tapocontrol.internal.api.protocol.klap;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoEncoder.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.ByteUtils.*;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for KLAP Session
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class KlapSession {
    private final Logger logger = LoggerFactory.getLogger(KlapSession.class);

    private final KlapProtocol klap;
    private @NonNullByDefault({}) KlapCipher cipher;
    private byte[] localSeed = new byte[16];
    private byte[] remoteSeed = new byte[16];
    private byte[] serverHash = new byte[0];
    private byte[] authHash = new byte[0];
    private byte[] localSeedAuthHash = new byte[0];
    private String sessionId = "";
    private long expireAt = 0L;
    private String uid;

    // List of class-specific commands
    public static final String DEVICE_CMD_HANDSHAKE1 = "handshake1";
    public static final String DEVICE_CMD_HANDSHAKE2 = "handshake2";
    public static final String DEVICE_CMD_SECURE_METHOD = "securePassthrough";
    public static final String TP_SESSION_COOKIE_HEADER = "Set-Cookie";
    public static final String TP_SESSION_COOKIE_NAME = "TP_SESSIONID";
    public static final String TP_SESSION_COOKIE_TIMEOUT = "TIMEOUT";

    /************************
     * INIT CLASS
     ************************/

    public KlapSession(KlapProtocol klapHandler) {
        klap = klapHandler;
        localSeed = newLocalSeed();
        uid = klap.httpDelegator.getThingUID() + "KLAP-Session";
    }

    /* create new random local seed */
    private byte[] newLocalSeed() {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[16];
        random.nextBytes(randomBytes);
        return randomBytes;
    }

    /************************
     * SET VALUES
     ************************/

    /* reset data (logout) */
    public void reset() {
        logger.trace("reset KlapSession");
        localSeed = newLocalSeed();
        remoteSeed = new byte[0];
        localSeedAuthHash = new byte[0];
        serverHash = new byte[0];
        sessionId = "";
        expireAt = 0L;
        cipher = null;
    }

    /* set sessionId (cookie) */
    public boolean setSession(ContentResponse response) throws TapoErrorHandler {
        try {
            /* get cookie */
            String header = response.getHeaders().get(TP_SESSION_COOKIE_HEADER);
            String cookie = header.split(";")[0].replace(TP_SESSION_COOKIE_NAME + "=", "");
            int timeout = Integer.parseInt(header.split(";")[1].replace(TP_SESSION_COOKIE_TIMEOUT + "=", ""));
            sessionId = cookie;
            expireAt = System.currentTimeMillis() * timeout;
            /* get seeds */
            byte[] responseContent = response.getContent();
            byte[] bRemoteSeed = truncateByteArray(responseContent, 0, 16); // get first 16 bytes
            byte[] bServerHash = truncateByteArray(responseContent, 16, responseContent.length - 16); // get
                                                                                                      // rest
            setSeed(bRemoteSeed, bServerHash);
        } catch (Exception e) {
            throw new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED, e.getMessage());
        }

        return true;
    }

    /**
     * set Seed and compare handshake1 hashes
     */
    public boolean setSeed(byte[] remoteSeed, byte[] serverHash) throws TapoErrorHandler {
        logger.trace("({}) Init Session", uid);
        try {
            logger.trace("remoteseed is '{}' / serverhash is '{}' authhash is '{}'", byteArrayToHex(localSeed),
                    byteArrayToHex(remoteSeed), byteArrayToHex(authHash));

            byte[] concatByteArray = concatBytes(localSeed, remoteSeed, authHash);
            byte[] bLocalSeedAuthHash = sha256Encode(concatByteArray);

            if (Arrays.equals(bLocalSeedAuthHash, serverHash)) {
                logger.trace("handshake1 sucessfull");
                this.remoteSeed = remoteSeed;
                this.serverHash = serverHash;
                this.localSeedAuthHash = bLocalSeedAuthHash;
                return true;
            } else {
                logger.trace("handshake1 does not match {} / {}", byteArrayToHex(bLocalSeedAuthHash),
                        byteArrayToHex(serverHash));
                reset();
                throw new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED);
            }
        } catch (Exception e) {
            throw new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED);
        }
    }

    /***********************
     * Request Sender
     **********************/

    /*
     * Sender for handling Handshake
     */
    private ContentResponse sendHandshake(String customUrl, byte[] payloadBytes, String command)
            throws TimeoutException, InterruptedException, ExecutionException, TapoErrorHandler {
        logger.trace("({}) sending bytes'{} to '{}' ", uid, byteArrayToHex(payloadBytes), customUrl);

        Request httpRequest = klap.httpDelegator.getHttpClient().newRequest(customUrl)
                .method(HttpMethod.POST.toString());

        httpRequest = klap.setHeaders(httpRequest);
        httpRequest.timeout(TAPO_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        /* add request body */
        httpRequest.content(new BytesContentProvider(payloadBytes));

        return httpRequest.send();
    }

    /* complete Handshake (init Cipher) */
    private void completeHandshake() throws Exception {
        cipher = new KlapCipher(localSeed, remoteSeed, authHash);
    }

    /************************
     * HANDSHAKE & COOKIE
     ************************/

    /**
     * Create Handshake
     */
    public boolean login(TapoCredentials credentials) throws TapoErrorHandler {
        try {
            authHash = generateAuthHash(credentials);
            if (createHandshake1()) {
                logger.trace("({}) handshake1 successfull", uid);
                if (createHandshake2()) {
                    logger.trace("({}) handshake2 successfull", uid);
                    completeHandshake();
                    return isHandshakeComplete();
                }
            } else {
                throw new TapoErrorHandler(NO_ERROR, BINDING_ID);
            }
        } catch (TapoErrorHandler e) {
            throw e;
        } catch (Exception e) {
            throw new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED);
        }
        return false;
    }

    /**
     * Handle Handshake (set session)
     */
    private boolean createHandshake1()
            throws TimeoutException, InterruptedException, ExecutionException, TapoErrorHandler {
        String handshakeUrl = klap.getUrl() + "/" + DEVICE_CMD_HANDSHAKE1;
        byte[] bytes = getLocalSeed();
        ContentResponse response = sendHandshake(handshakeUrl, bytes, DEVICE_CMD_HANDSHAKE1);
        if (response.getStatus() == 200) {
            logger.trace("({}) got handshake response", uid);
            setSession(response);
            return seedIsOkay();
        } else {
            logger.debug("({}) invalid handshake1 response {}", uid, response.getStatus());
            throw new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, "invalid response receicved");
        }
    }

    /**
     * Complete Handshake2 (set cipher)
     */
    private boolean createHandshake2() throws TimeoutException, InterruptedException, ExecutionException,
            NoSuchAlgorithmException, TapoErrorHandler {
        String handshakeUrl = klap.getUrl() + "/" + DEVICE_CMD_HANDSHAKE2;
        byte[] byteArr = concatBytes(remoteSeed, localSeed, authHash);
        byte[] payloadBytes = sha256Encode(byteArr);
        ContentResponse response = sendHandshake(handshakeUrl, payloadBytes, DEVICE_CMD_HANDSHAKE2);

        if (response.getStatus() == 200) {
            return true;
        } else {
            logger.debug("({}) invalid handshake1 response {}", uid, response.getStatus());
            throw new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, response.getReason());
        }
    }

    /************************
     * ENCODING / DECODING
     ************************/

    /**
     * generate auth-hash from credentials
     */
    private byte[] generateAuthHash(TapoCredentials credentials) throws NoSuchAlgorithmException, TapoErrorHandler {
        byte[] bUsername = sha1Encode(credentials.username().getBytes(StandardCharsets.UTF_8));
        byte[] bPassword = sha1Encode(credentials.password().getBytes(StandardCharsets.UTF_8));
        return sha256Encode(concatBytes(bUsername, bPassword));
    }

    /**
     * Decrypt encrypted TapoResponse
     */
    public String decryptResponse(byte[] byteResponse, Integer ivSeq) throws TapoErrorHandler {
        try {
            return cipher.decrypt(byteResponse, ivSeq);
        } catch (Exception e) {
            logger.debug("({}) exception decrypting Payload '{}'", uid, e.toString());
            throw new TapoErrorHandler(ERR_DATA_DECRYPTING);
        }
    }

    /**
     * Encrypt Request
     */
    public byte[] encryptRequest(Object request) throws TapoErrorHandler {
        try {
            return cipher.encrypt(request.toString());
        } catch (Exception e) {
            logger.debug("({}) exception encrypting Payload '{}'", uid, e.toString());
            throw new TapoErrorHandler(ERR_DATA_ENCRYPTING);
        }
    }

    /************************
     * GET VALUES
     ************************/

    public long expireAt() {
        return expireAt;
    }

    public String getCookie() {
        if (!sessionId.isBlank()) {
            return TP_SESSION_COOKIE_NAME + "=" + sessionId;
        }
        return "";
    }

    public KlapCipher getCipher() {
        return cipher;
    }

    public byte[] getLocalSeed() {
        return localSeed;
    }

    public Integer getIvSequence() {
        return cipher.getIvSeq();
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isHandshakeComplete() {
        return !sessionId.isBlank() && cipher != null && cipher.isInitialized();
    }

    public boolean isExpired() {
        return (expireAt - (System.currentTimeMillis())) <= 40 * 1000;
    }

    /* return true if seeds are set */
    public boolean seedIsOkay() {
        return serverHash.length > 0 && Arrays.equals(localSeedAuthHash, serverHash);
    }
}
