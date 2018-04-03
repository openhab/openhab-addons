/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import org.openhab.binding.loxone.internal.core.LxJsonResponse.LxJsonSubResponse;
import org.openhab.binding.loxone.internal.core.LxServer.Configuration;
import org.openhab.binding.loxone.internal.core.LxWsClient.LxWebSocket;
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

    private Logger logger = LoggerFactory.getLogger(LxWsSecurityHash.class);

    /**
     * Create a hash-based authentication instance.
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
    LxWsSecurityHash(int debugId, Configuration configuration, LxWebSocket socket, String user, String password) {
        super(debugId, configuration, socket, user, password);
    }

    @Override
    boolean execute() {
        logger.debug("[{}] Starting hash-based authentication.", debugId);
        if (password == null || password.isEmpty()) {
            return setError(LxOfflineReason.UNAUTHORIZED, "Enter password for hash-based authentication.");
        }
        LxJsonSubResponse resp = socket.sendCmdWithResp(CMD_GET_KEY, true, false);
        if (!checkResponse(resp)) {
            return false;
        }
        String hash = hashString(user + ":" + password, resp.value.getAsString());
        if (hash == null) {
            return setError(LxOfflineReason.INTERNAL_ERROR, "Error hashing credentials.");
        }
        String cmd = CMD_AUTHENTICATE + hash;
        if (!checkResponse(socket.sendCmdWithResp(cmd, true, false))) {
            return false;
        }
        logger.debug("[{}] Authenticated - hash based authentication.", debugId);
        return true;
    }
}
