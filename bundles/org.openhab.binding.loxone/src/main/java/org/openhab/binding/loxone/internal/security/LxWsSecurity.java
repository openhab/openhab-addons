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
package org.openhab.binding.loxone.internal.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.openhab.binding.loxone.internal.LxServerHandler;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.LxWebSocket;
import org.openhab.binding.loxone.internal.types.LxErrorCode;
import org.openhab.binding.loxone.internal.types.LxResponse;
import org.openhab.binding.loxone.internal.types.LxWsSecurityType;
import org.openhab.core.util.HexUtils;

/**
 * Security abstract class providing authentication and encryption services.
 * Used by the {@link LxServerHandler} during connection establishment to authenticate user and during message exchange
 * for encryption and decryption or the messages.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public abstract class LxWsSecurity {
    final int debugId;
    final String user;
    final String password;
    final LxWebSocket socket;
    final LxServerHandlerApi thingHandler;

    LxErrorCode reason;

    private String details;
    private boolean cancel = false;
    private final Lock authenticationLock = new ReentrantLock();

    /**
     * Create an authentication instance.
     *
     * @param debugId instance of the client used for debugging purposes only
     * @param thingHandler API to the thing handler
     * @param socket websocket to perform communication with Miniserver
     * @param user user to authenticate
     * @param password password to authenticate
     */
    LxWsSecurity(int debugId, LxServerHandlerApi thingHandler, LxWebSocket socket, String user, String password) {
        this.debugId = debugId;
        this.thingHandler = thingHandler;
        this.socket = socket;
        this.user = user;
        this.password = password;
    }

    /**
     * Initiate user authentication. This method will return immediately and authentication will be done in a separate
     * thread asynchronously. On successful or unsuccessful completion, a provided callback will be called with
     * information about failed reason and details of failure. In case of success, the reason value will be
     * {@link LxErrorCode#OK}
     * Only one authentication can run in parallel and must be performed sequentially (create no more threads).
     *
     * @param doneCallback callback to execute when authentication is finished or failed
     */
    public void authenticate(BiConsumer<LxErrorCode, String> doneCallback) {
        Runnable init = () -> {
            authenticationLock.lock();
            try {
                execute();
                doneCallback.accept(reason, details);
            } finally {
                authenticationLock.unlock();
            }
        };
        new Thread(init).start();
    }

    /**
     * Perform user authentication using a specific authentication algorithm.
     * This method will be executed in a dedicated thread to allow sending synchronous messages to the Miniserver.
     *
     * @return true when authentication granted
     */
    abstract boolean execute();

    /**
     * Cancel authentication procedure and any pending activities.
     * It is supposed to be overridden by implementing classes.
     */
    public void cancel() {
        cancel = true;
    }

    /**
     * Check a response received from the Miniserver for errors, interpret it and store the results in class fields.
     *
     * @param response response received from the Miniserver
     * @return {@link LxErrorCode#OK} when response is correct or a specific {@link LxErrorCode}
     */
    boolean checkResponse(LxResponse response) {
        if (response == null || response.subResponse == null || cancel) {
            reason = LxErrorCode.COMMUNICATION_ERROR;
            return false;
        }
        reason = response.getResponseCode();
        return (reason == LxErrorCode.OK);
    }

    /**
     * Hash string (e.g. containing user name and password or token) according to the algorithm required by the
     * Miniserver.
     *
     * @param string string to be hashed
     * @param hashKeyHex hash key received from the Miniserver in hex format
     * @param sha256 if SHA-256 algorithm should be used (SHA-1 otherwise)
     * @return hashed string or null if failed
     */
    String hashString(String string, String hashKeyHex, boolean sha256) {
        if (string == null || hashKeyHex == null) {
            return null;
        }
        try {
            String alg = sha256 ? "HmacSHA256" : "HmacSHA1";
            byte[] hashKeyBytes = HexUtils.hexToBytes(hashKeyHex);
            SecretKeySpec signKey = new SecretKeySpec(hashKeyBytes, alg);
            Mac mac = Mac.getInstance(alg);
            mac.init(signKey);
            byte[] rawData = mac.doFinal(string.getBytes());
            return HexUtils.bytesToHex(rawData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return null;
        }
    }

    /**
     * Encrypt string using current encryption algorithm.
     *
     * @param string input string to encrypt
     * @return encrypted string
     */
    public String encrypt(String string) {
        // by default no encryption
        return string;
    }

    /**
     * Check if control is encrypted and decrypt it using current decryption algorithm.
     * If control is not encrypted or decryption is not available or not ready, the control should be returned in its
     * original form.
     *
     * @param control control to be decrypted
     * @return decrypted control or original control in case decryption is unavailable, control is not encrypted or
     *         other issue occurred
     */
    public String decryptControl(String control) {
        // by default no decryption
        return control;
    }

    /**
     * Set error code and return false. It is used to report detailed error information from inside the algorithms.
     *
     * @param reason reason for failure
     * @param details details of the failure
     * @return always false
     */
    boolean setError(LxErrorCode reason, String details) {
        if (reason != null) {
            this.reason = reason;
        }
        if (details != null) {
            this.details = details;
        }
        return false;
    }

    /**
     * Create an authentication instance.
     *
     * @param type type of security algorithm
     * @param swVersion Miniserver's software version or null if unknown
     * @param debugId instance of the client used for debugging purposes only
     * @param thingHandler API to the thing handler
     * @param socket websocket to perform communication with Miniserver
     * @param user user to authenticate
     * @param password password to authenticate
     * @return created security object
     */
    public static LxWsSecurity create(LxWsSecurityType type, String swVersion, int debugId,
            LxServerHandlerApi thingHandler, LxWebSocket socket, String user, String password) {
        LxWsSecurityType securityType = type;
        if (securityType == LxWsSecurityType.AUTO && swVersion != null) {
            String[] versions = swVersion.split("[.]");
            if (versions != null && versions.length > 0 && Integer.parseInt(versions[0]) <= 8) {
                securityType = LxWsSecurityType.HASH;
            } else {
                securityType = LxWsSecurityType.TOKEN;
            }
        }
        if (securityType == LxWsSecurityType.HASH) {
            return new LxWsSecurityHash(debugId, thingHandler, socket, user, password);
        } else {
            return new LxWsSecurityToken(debugId, thingHandler, socket, user, password);
        }
    }
}
