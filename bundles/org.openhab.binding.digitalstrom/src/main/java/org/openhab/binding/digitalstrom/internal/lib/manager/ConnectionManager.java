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
package org.openhab.binding.digitalstrom.internal.lib.manager;

import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.listener.ConnectionListener;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.HttpTransport;

/**
 * The {@link ConnectionManager} manages the connection to a digitalSTROM-Server.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface ConnectionManager {

    public static final int GENERAL_EXCEPTION = -1;
    public static final int MALFORMED_URL_EXCEPTION = -2;
    public static final int CONNECTION_EXCEPTION = -3;
    public static final int SOCKET_TIMEOUT_EXCEPTION = -4;
    public static final int UNKNOWN_HOST_EXCEPTION = -5;
    public static final int AUTHENTIFICATION_PROBLEM = -6;
    public static final int SSL_HANDSHAKE_EXCEPTION = -7;

    /**
     * Returns the {@link HttpTransport} to execute queries or special commands on the digitalSTROM-Server.
     *
     * @return the HttpTransport
     */
    HttpTransport getHttpTransport();

    /**
     * Returns the {@link DsAPI} to execute commands on the digitalSTROM-Server.
     *
     * @return the DsAPI
     */
    DsAPI getDigitalSTROMAPI();

    /**
     * This method has to be called before each command to check the connection to the digitalSTROM-Server.
     * It examines the connection to the server, sets a new Session-Token, if it is expired and sets a new
     * Application-Token, if none it set at the digitalSTROM-Server. It also outputs the specific connection failure.
     *
     * @return true if the connection is established and false if not
     */
    boolean checkConnection();

    /**
     * Returns the current Session-Token.
     *
     * @return Session-Token
     */
    String getSessionToken();

    /**
     * Returns the auto-generated or user defined Application-Token.
     *
     * @return Application-Token
     */
    String getApplicationToken();

    /**
     * Registers a {@link ConnectionListener} to this {@link ConnectionManager}.
     *
     * @param connectionListener to register
     */
    void registerConnectionListener(ConnectionListener connectionListener);

    /**
     * Unregisters the {@link ConnectionListener} from this {@link ConnectionManager}.
     */
    void unregisterConnectionListener();

    /**
     * Revokes the saved Application-Token from the digitalSTROM-Server and returns true if the Application-Token was
     * revoke successful, otherwise false.
     *
     * @return successful = true, otherwise false
     */
    boolean removeApplicationToken();

    /**
     * Updates the login configuration.
     *
     * @param hostAddress of the digitalSTROM-Server
     * @param username to login
     * @param password to login
     * @param applicationToken to login
     */
    void updateConfig(String hostAddress, String username, String password, String applicationToken);

    /**
     * Updates the {@link Config} with the given config.
     *
     * @param config to update
     */
    void updateConfig(Config config);

    /**
     * Returns the {@link Config}.
     *
     * @return the config
     */
    Config getConfig();

    /**
     * Informs this {@link ConnectionManager} that the {@link Config} has been updated.
     */
    void configHasBeenUpdated();

    /**
     * Generates and returns a new session token.
     *
     * @return new session token
     */
    String getNewSessionToken();

    /**
     * Checks the connection through the given HTTP-Response-Code or exception code. If a {@link ConnectionListener} is
     * registered this method also informs the registered {@link ConnectionListener} if the connection state has
     * changed. <br>
     * <br>
     * <b>Exception-Codes:</b><br>
     * <b>{@link #GENERAL_EXCEPTION}</b> general exception<br>
     * <b>{@link #MALFORMED_URL_EXCEPTION}</b> MalformedURLException<br>
     * <b>{@link #CONNECTION_EXCEPTION}</b> java.net.ConnectException<br>
     * <b>{@link #SOCKET_TIMEOUT_EXCEPTION}</b> SocketTimeoutException<br>
     * <b>{@link #UNKNOWN_HOST_EXCEPTION}</b> java.net.UnknownHostException<br>
     * <br>
     * <b>Code for authentication problems:</b> {@link #AUTHENTIFICATION_PROBLEM}<br>
     *
     *
     * @param code exception or HTTP-Response-Code
     * @return true, if connection is valid
     */
    boolean checkConnection(int code);

    /**
     * Returns true, if connection is established, otherwise false.
     *
     * @return true, if connection is established, otherwise false
     */
    boolean connectionEstablished();
}
