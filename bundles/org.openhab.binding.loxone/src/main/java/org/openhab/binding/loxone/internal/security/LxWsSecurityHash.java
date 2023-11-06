/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.LxWebSocket;
import org.openhab.binding.loxone.internal.types.LxErrorCode;
import org.openhab.binding.loxone.internal.types.LxResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A hash-based authentication algorithm. No encryption and decryption supported.
 * The algorithm computes a HMAC-SHA1 hash from the user name and password, using a key received from the Miniserver.
 * This hash is sent to the Miniserver to authorize the user.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxWsSecurityHash extends LxWsSecurity {

    private static final String CMD_GET_KEY = "jdev/sys/getkey";
    private static final String CMD_AUTHENTICATE = "authenticate/";

    private final Logger logger = LoggerFactory.getLogger(LxWsSecurityHash.class);

    /**
     * Create a hash-based authentication instance.
     *
     * @param debugId instance of the client used for debugging purposes only
     * @param thingHandler API to the thing handler
     * @param socket websocket to perform communication with Miniserver
     * @param user user to authenticate
     * @param password password to authenticate
     */
    LxWsSecurityHash(int debugId, LxServerHandlerApi thingHandler, LxWebSocket socket, String user, String password) {
        super(debugId, thingHandler, socket, user, password);
    }

    @Override
    boolean execute() {
        logger.debug("[{}] Starting hash-based authentication.", debugId);
        if (password == null || password.isEmpty()) {
            return setError(LxErrorCode.USER_UNAUTHORIZED, "Enter password for hash-based authentication.");
        }
        LxResponse resp = socket.sendCmdWithResp(CMD_GET_KEY, true, false);
        if (!checkResponse(resp)) {
            return false;
        }
        String hash = hashString(user + ":" + password, resp.getValueAsString(), false);
        if (hash == null) {
            return setError(LxErrorCode.INTERNAL_ERROR, "Error hashing credentials.");
        }
        String cmd = CMD_AUTHENTICATE + hash;
        if (!checkResponse(socket.sendCmdWithResp(cmd, true, false))) {
            return false;
        }
        logger.debug("[{}] Authenticated - hash based authentication.", debugId);
        return true;
    }
}
