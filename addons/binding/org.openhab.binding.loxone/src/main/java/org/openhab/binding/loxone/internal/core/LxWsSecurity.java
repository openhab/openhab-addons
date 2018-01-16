/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.openhab.binding.loxone.internal.core.LxJsonResponse.LxJsonSubResponse;
import org.openhab.binding.loxone.internal.core.LxServer.Configuration;
import org.openhab.binding.loxone.internal.core.LxWsClient.LxWebSocket;

/**
 * Security abstract class providing authentication and encryption services.
 * Used by the {@link LxWsClient} during connection establishment to authenticate user and during message exchange for
 * encryption and decryption or the messages.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
abstract class LxWsSecurity {
    final int debugId;
    final String user;
    final String password;
    final LxWebSocket socket;
    final Configuration configuration;

    LxOfflineReason reason;
    String details;
    boolean cancel = false;

    private final Lock authenticationLock = new ReentrantLock();

    /**
     * Create an authentication instance.
     *
     * @param debugId
     *            instance of the client used for debugging purposes only
     * @param configuration
     *            configuration object for getting and setting custom properties (e.g. token)
     * @param socket
     *            websocket to perform communication with Miniserver
     * @param user
     *            user to authenticate
     * @param password
     *            password to authenticate
     */
    LxWsSecurity(int debugId, Configuration configuration, LxWebSocket socket, String user, String password) {
        this.debugId = debugId;
        this.configuration = configuration;
        this.socket = socket;
        this.user = user;
        this.password = password;
    }

    /**
     * Initiate user authentication. This method will return immediately and authentication will be done in a separate
     * thread asynchronously. On successful or unsuccessful completion, a provided callback will be called with
     * information about failed reason and details of failure. In case of success, the reason value will be
     * {@link LxOfflineReason#NONE}
     * Only one authentication can run in parallel and must be performed sequentially (create no more threads).
     *
     * @param doneCallback
     *            callback to execute when authentication is finished or failed
     */
    void authenticate(BiConsumer<LxOfflineReason, String> doneCallback) {
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
     * @return
     *         true when authentication granted
     */
    abstract boolean execute();

    /**
     * Cancel authentication procedure and any pending activities.
     * It is supposed to be overridden by implementing classes.
     */
    void cancel() {
        cancel = true;
    }

    /**
     * Check a response received from the Miniserver for errors, interpret it and store the results in class fields.
     *
     * @param response
     *            response received from the Miniserver
     * @return
     *         {@link LxOfflineReason#NONE} when response is correct or a specific {@link LxOfflineReason}
     */
    boolean checkResponse(LxJsonSubResponse response) {
        if (response == null || cancel) {
            reason = LxOfflineReason.COMMUNICATION_ERROR;
            return false;
        }
        reason = LxOfflineReason.getReason(response.code);
        return (reason == LxOfflineReason.NONE);
    }

    /**
     * Hash string (e.g. containing user name and password or token) according to the algorithm required by the
     * Miniserver.
     *
     * @param string
     *            string to be hashed
     * @param hashKeyHex
     *            hash key received from the Miniserver in hex format
     * @return
     *         hashed string or null if failed
     */
    String hashString(String string, String hashKeyHex) {
        try {
            byte[] hashKeyBytes = Hex.decodeHex(hashKeyHex.toCharArray());
            SecretKeySpec signKey = new SecretKeySpec(hashKeyBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] rawData = mac.doFinal(string.getBytes());
            return Hex.encodeHexString(rawData);
        } catch (DecoderException | NoSuchAlgorithmException | InvalidKeyException e) {
            return null;
        }
    }

    /**
     * Encrypt string using current encryption algorithm.
     *
     * @param string
     *            input string to encrypt
     * @return
     *         encrypted string
     */
    String encrypt(String string) {
        // by default no encryption
        return string;
    }

    /**
     * Check if control is encrypted and decrypt it using current decryption algorithm.
     * If control is not encrypted or decryption is not available or not ready, the control should be returned in its
     * original form.
     *
     * @param control
     *            control to be decrypted
     * @return
     *         decrypted control or original control in case decryption is unavailable, control is not encrypted or
     *         other issue occurred
     */
    String decryptControl(String control) {
        // by default no decryption
        return control;
    }

    /**
     * Set error code and return false. It is used to report detailed error information from inside the algorithms.
     *
     * @param reason
     *            reason for failure
     * @param details
     *            details of the failure
     * @return
     *         always false
     */
    boolean setError(LxOfflineReason reason, String details) {
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
     * @param type
     *            type of security algorithm
     * @param swVersion
     *            Miniserver's software version or null if unknown
     * @param debugId
     *            instance of the client used for debugging purposes only
     * @param configuration
     *            configuration object for getting and setting custom properties (e.g. token)
     * @param socket
     *            websocket to perform communication with Miniserver
     * @param user
     *            user to authenticate
     * @param password
     *            password to authenticate
     * @return
     *         created security object
     */
    static LxWsSecurity create(LxWsSecurityType type, String swVersion, int debugId, Configuration configuration,
            LxWebSocket socket, String user, String password) {
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
            return new LxWsSecurityHash(debugId, configuration, socket, user, password);
        } else {
            return new LxWsSecurityToken(debugId, configuration, socket, user, password);
        }
    }

}
