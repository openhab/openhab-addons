/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.listener;

/**
 * The {@link ConnectionListener} is notified if the connection state of digitalSTROM-Server has changed.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface ConnectionListener {

    /* Connection-States */
    /**
     * State, if you're not authenticated on the digitalSTROM-Server.
     */
    final String NOT_AUTHENTICATED = "notAuth";
    /**
     * State, if the connection to the digitalSTROM-Server is lost.
     */
    final String CONNECTION_LOST = "connLost";
    /**
     * State, if a ssl handshake problem occured while communicating with the digitalSTROM-Server.
     */
    final String SSL_HANDSHAKE_ERROR = "sslHandshakeError";
    /**
     * State, if the connection to the digitalSTROM-Server is resumed.
     */
    final String CONNECTION_RESUMED = "connResumed";
    /**
     * State, if the Application-Token is generated.
     */
    final String APPLICATION_TOKEN_GENERATED = "appGen";

    /* Not authentication reasons */
    /**
     * State, if the given Application-Token cannot be used.
     */
    final String WRONG_APP_TOKEN = "wrongAppT";
    /**
     * State, if the given username or password cannot be used.
     */
    final String WRONG_USER_OR_PASSWORD = "wrongUserOrPasswd";
    /**
     * State, if no username or password is set and the given application-token cannot be used or is null.
     */
    final String NO_USER_PASSWORD = "noUserPasswd";

    /**
     * State, if the connection timed out.
     */
    final String CONNECTON_TIMEOUT = "connTimeout";

    /**
     * State, if the host address cannot be found.
     */
    final String HOST_NOT_FOUND = "hostNotFound";

    /**
     * State, if the host address is unknown.
     */
    final String UNKNOWN_HOST = "unknownHost";

    /**
     * State, if the the URL is invalid.
     */
    final String INVALID_URL = "invalideURL";

    /**
     * This method is called whenever the connection state has changed from {@link #CONNECTION_LOST}
     * to {@link #CONNECTION_RESUMED} and vice versa. It also will be called if the application-token is generated over
     * {@link #APPLICATION_TOKEN_GENERATED}.
     *
     * @param newConnectionState of the connection
     */
    void onConnectionStateChange(String newConnectionState);

    /**
     * This method is called whenever the connection state has changed to {@link #NOT_AUTHENTICATED} or
     * {@link #CONNECTION_LOST}
     * and also passes the reason why. Reason can be:
     * <ul>
     * <li>{@link #WRONG_APP_TOKEN} if the given application-token can't be used.</li>
     * <li>{@link #WRONG_USER_OR_PASSWORD} if the given user name or password can't be used.</li>
     * <li>{@link #NO_USER_PASSWORD} if no user name or password is set and the given application-token can't be
     * used.</li>
     * <li>{@link #HOST_NOT_FOUND} if the host can't be found.</li>
     * <li>{@link #INVALID_URL} if the the URL is invalid.</li>
     * </ul>
     *
     * @param newConnectionState of the connection
     * @param reason why the connection is failed
     */
    void onConnectionStateChange(String newConnectionState, String reason);
}
