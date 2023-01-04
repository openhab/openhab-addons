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
package org.openhab.binding.digitalstrom.internal.lib.manager.impl;

import java.net.HttpURLConnection;

import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.listener.ConnectionListener;
import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.HttpTransport;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.impl.DsAPIImpl;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.impl.HttpTransportImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link ConnectionManagerImpl} is the implementation of the {@link ConnectionManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class ConnectionManagerImpl implements ConnectionManager {

    /**
     * Query to get all enabled application tokens. Can be executed with {@link DsAPI#query(String, String)} or
     * {@link DsAPI#query2(String, String)}.
     */
    public final String QUERY_GET_ENABLED_APPLICATION_TOKENS = "/system/security/applicationTokens/enabled/*(*)";
    private final Logger logger = LoggerFactory.getLogger(ConnectionManagerImpl.class);

    private Config config;
    private ConnectionListener connListener;
    private HttpTransport transport;
    private String sessionToken;
    private Boolean connectionEstablished = false;
    private boolean genAppToken;
    private DsAPI digitalSTROMClient;

    /**
     * The same constructor like {@link #ConnectionManagerImpl(String, String, String, String)}, but the connection
     * timeout and read timeout can be set, too.
     *
     * @param hostArddress (must not be null)
     * @param connectTimeout (if connectTimeout is lower than 0 the {@link Config#DEFAULT_CONNECTION_TIMEOUT} will be
     *            set)
     * @param readTimeout (if readTimeout is lower than 0 the {@link Config#DEFAULT_CONNECTION_TIMEOUT} will be set)
     * @param username (can be null, if application token is set)
     * @param password (can be null, if application token is set
     * @param applicationToken (can be null, if username and password is set)
     * @see #ConnectionManagerImpl(String, String, String, String)
     */
    public ConnectionManagerImpl(String hostArddress, int connectTimeout, int readTimeout, String username,
            String password, String applicationToken) {
        init(hostArddress, connectTimeout, readTimeout, username, password, applicationToken, false);
    }

    /**
     * Creates a new {@link ConnectionManagerImpl} through a {@link Config} object, which has all configurations set.
     *
     * @param config (must not be null)
     */
    public ConnectionManagerImpl(Config config) {
        init(config, false);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(Config)}, but a {@link ConnectionListener} can be
     * registered, too.
     *
     * @param config (must not be null)
     * @param connectionListener (can be null)
     * @see #ConnectionManagerImpl(Config)
     */
    public ConnectionManagerImpl(Config config, ConnectionListener connectionListener) {
        this.connListener = connectionListener;
        init(config, false);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(Config, ConnectionListener)}, but through genApToken it
     * can be set, if an application token will be automatically generated.
     *
     * @param config (must not be null)
     * @param connectionListener (can be null)
     * @param genAppToken (true = application token will be generated, otherwise false)
     * @see #ConnectionManagerImpl(Config, ConnectionListener)
     */
    public ConnectionManagerImpl(Config config, ConnectionListener connectionListener, boolean genAppToken) {
        this.connListener = connectionListener;
        this.genAppToken = genAppToken;
        init(config, false);
    }

    /**
     * Creates a new {@link ConnectionManagerImpl} with the given parameters, which are needed to create the
     * {@link HttpTransport} and to login into the digitalSTROM server. If the application token is null and the
     * username and password are valid, an application token will be automatically generated or an existing application
     * token for the at {@link Config#getApplicationName()} set application name will be set.
     *
     * @param hostAddress (must not be null)
     * @param username (can be null, if application token is set)
     * @param password (can be null, if application token is set
     * @param applicationToken (can be null, if username and password is set)
     */
    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken) {
        init(hostAddress, -1, -1, username, password, applicationToken, false);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(String, String, String, String)}, but without username
     * and password.
     *
     * @param hostAddress (must not be null)
     * @param applicationToken (must not be null)
     */
    public ConnectionManagerImpl(String hostAddress, String applicationToken) {
        init(hostAddress, -1, -1, null, null, applicationToken, false);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(String, String, String, String)}, but without application
     * token.
     *
     * @param hostAddress (must not be null)
     * @param username (must not be null)
     * @param password (must not be null)
     * @see #ConnectionManagerImpl(String, String, String, String)
     */
    public ConnectionManagerImpl(String hostAddress, String username, String password) {
        init(hostAddress, -1, -1, username, password, null, false);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(String, String, String)}, but a
     * {@link ConnectionListener} can be set, too.
     *
     * @param hostAddress (must not be null)
     * @param username (must not be null)
     * @param password (must not be null)
     * @param connectionListener (can be null)
     * @see #ConnectionManagerImpl(String, String, String)
     */
    public ConnectionManagerImpl(String hostAddress, String username, String password,
            ConnectionListener connectionListener) {
        this.connListener = connectionListener;
        init(hostAddress, -1, -1, username, password, null, false);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(String, String, String, String)}, but a
     * {@link ConnectionListener} can be set, too.
     *
     * @param hostAddress (must not be null)
     * @param username (can be null, if application token is set)
     * @param password (can be null, if application token is set)
     * @param applicationToken (can be null, if username and password is set)
     * @param connectionListener (can be null)
     * @see #ConnectionManagerImpl(String, String, String, String)
     */
    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken,
            ConnectionListener connectionListener) {
        this.connListener = connectionListener;
        init(hostAddress, -1, -1, username, password, null, false);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(String, String, String)}, but through genApToken it
     * can be set, if an application token will be automatically generated.
     *
     * @param hostAddress (must not be null)
     * @param username (must not be null)
     * @param password (must not be null)
     * @param genAppToken (true = application token will be generated, otherwise false)
     * @see #ConnectionManagerImpl(String, String, String, String)
     */
    public ConnectionManagerImpl(String hostAddress, String username, String password, boolean genAppToken) {
        this.genAppToken = genAppToken;
        init(hostAddress, -1, -1, username, password, null, false);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(String, String, String, String)}, but through genApToken
     * it can be set, if an application token will be automatically generated.
     *
     * @param hostAddress (must not be null)
     * @param username (can be null, if application token is set)
     * @param password (can be null, if application token is set)
     * @param applicationToken (can be null, if username and password is set)
     * @param genAppToken (true = application token will be generated, otherwise false)
     * @see #ConnectionManagerImpl(String, String, String, String)
     */
    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken,
            boolean genAppToken) {
        this.genAppToken = genAppToken;
        init(hostAddress, -1, -1, username, password, applicationToken, false);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(String, String, String, String, boolean)}, but through
     * acceptAllCerts it can be set, if all SSL-Certificates will be accept.
     *
     * @param hostAddress (must not be null)
     * @param username (can be null, if application token is set)
     * @param password (can be null, if application token is set)
     * @param applicationToken (can be null, if username and password is set)
     * @param genAppToken (true = application token will be generated, otherwise false)
     * @param acceptAllCerts (true = all SSL-Certificates will be accept, otherwise false)
     * @see #ConnectionManagerImpl(String, String, String, String, boolean)
     */
    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken,
            boolean genAppToken, boolean acceptAllCerts) {
        this.genAppToken = genAppToken;
        init(hostAddress, -1, -1, username, password, applicationToken, acceptAllCerts);
    }

    /**
     * The same constructor like {@link #ConnectionManagerImpl(String, String, String, String, boolean)}, but a
     * {@link ConnectionListener} can be set, too.
     *
     * @param hostAddress (must not be null)
     * @param username (can be null, if application token is set)
     * @param password (can be null, if application token is set)
     * @param applicationToken (can be null, if username and password is set)
     * @param genAppToken (true = application token will be generated, otherwise false)
     * @param connectionListener (can be null)
     * @see #ConnectionManagerImpl(String, String, String, String, boolean)
     */
    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken,
            boolean genAppToken, ConnectionListener connectionListener) {
        this.connListener = connectionListener;
        this.genAppToken = genAppToken;
        init(hostAddress, -1, -1, username, password, applicationToken, false);
    }

    private void init(String hostAddress, int connectionTimeout, int readTimeout, String username, String password,
            String applicationToken, boolean acceptAllCerts) {
        config = new Config(hostAddress, username, password, applicationToken);
        if (connectionTimeout >= 0) {
            config.setConnectionTimeout(connectionTimeout);
        }
        if (readTimeout >= 0) {
            config.setReadTimeout(readTimeout);
        }
        init(config, acceptAllCerts);
    }

    private void init(Config config, boolean acceptAllCerts) {
        this.config = config;
        this.transport = new HttpTransportImpl(this, acceptAllCerts);
        this.digitalSTROMClient = new DsAPIImpl(transport);
        if (this.genAppToken) {
            this.onNotAuthenticated();
        }
    }

    @Override
    public HttpTransport getHttpTransport() {
        return transport;
    }

    @Override
    public DsAPI getDigitalSTROMAPI() {
        return this.digitalSTROMClient;
    }

    @Override
    public String getSessionToken() {
        return this.sessionToken;
    }

    @Override
    public String getNewSessionToken() {
        if (this.genAppToken) {
            String token = config.getAppToken();
            if (token != null && !token.isBlank()) {
                sessionToken = this.digitalSTROMClient.loginApplication(token);
            } else if (codeIsAuthentificationFaild()) {
                onNotAuthenticated();
            }
        } else {
            sessionToken = this.digitalSTROMClient.login(this.config.getUserName(), this.config.getPassword());
        }
        return sessionToken;
    }

    @Override
    public synchronized boolean checkConnection() {
        return checkConnection(this.digitalSTROMClient.checkConnection(null));
    }

    private final short code = HttpURLConnection.HTTP_OK;

    private boolean codeIsAuthentificationFaild() {
        return this.code == HttpURLConnection.HTTP_FORBIDDEN;
    }

    @Override
    public boolean checkConnection(int code) {
        switch (code) {
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
            case HttpURLConnection.HTTP_OK:
                if (!connectionEstablished) {
                    onConnectionResumed();
                }
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                connectionEstablished = false;
                break;
            case HttpURLConnection.HTTP_FORBIDDEN:
                getNewSessionToken();
                if (sessionToken != null) {
                    if (!connectionEstablished) {
                        onConnectionResumed();
                    }
                } else {
                    if (this.genAppToken) {
                        onNotAuthenticated();
                    }
                    connectionEstablished = false;
                }
                break;
            case ConnectionManager.MALFORMED_URL_EXCEPTION:
                onConnectionLost(ConnectionListener.INVALID_URL);
                break;
            case ConnectionManager.CONNECTION_EXCEPTION:
            case ConnectionManager.SOCKET_TIMEOUT_EXCEPTION:
                onConnectionLost(ConnectionListener.CONNECTON_TIMEOUT);
                break;
            case ConnectionManager.SSL_HANDSHAKE_EXCEPTION:
                onConnectionLost(ConnectionListener.SSL_HANDSHAKE_ERROR);
                break;
            case ConnectionManager.GENERAL_EXCEPTION:
                onConnectionLost(ConnectionListener.CONNECTION_LOST);
                break;
            case ConnectionManager.UNKNOWN_HOST_EXCEPTION:
                onConnectionLost(ConnectionListener.UNKNOWN_HOST);
                break;
            case ConnectionManager.AUTHENTIFICATION_PROBLEM:
                if (connListener != null) {
                    if (config.getAppToken() != null) {
                        connListener.onConnectionStateChange(ConnectionListener.NOT_AUTHENTICATED,
                                ConnectionListener.WRONG_APP_TOKEN);
                    } else {
                        connListener.onConnectionStateChange(ConnectionListener.NOT_AUTHENTICATED,
                                ConnectionListener.WRONG_USER_OR_PASSWORD);
                    }
                }
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
                onConnectionLost(ConnectionListener.HOST_NOT_FOUND);
                break;
        }
        return connectionEstablished;
    }

    @Override
    public boolean connectionEstablished() {
        return connectionEstablished;
    }

    /**
     * This method is called whenever the connection to the digitalSTROM-Server is available,
     * but requests are not allowed due to a missing or invalid authentication.
     */
    private void onNotAuthenticated() {
        String applicationToken = null;
        boolean isAuthenticated = false;
        String token = config.getAppToken();
        if (token != null && !token.isBlank()) {
            sessionToken = digitalSTROMClient.loginApplication(token);
            if (sessionToken != null) {
                isAuthenticated = true;
            } else {
                if (connListener != null) {
                    connListener.onConnectionStateChange(ConnectionListener.NOT_AUTHENTICATED,
                            ConnectionListener.WRONG_APP_TOKEN);
                    if (!checkUserPassword()) {
                        return;
                    }
                }
            }
        }
        if (checkUserPassword()) {
            if (!isAuthenticated) {
                // if an application-token for the application exists, use this application-token and test host is
                // reachable
                logger.debug("check existing application-tokens");
                sessionToken = digitalSTROMClient.login(config.getUserName(), config.getPassword());
                if (sessionToken != null) {
                    JsonObject jObj = digitalSTROMClient.query(sessionToken, QUERY_GET_ENABLED_APPLICATION_TOKENS);

                    if (jObj != null) {
                        if (jObj.get("enabled") != null && jObj.get("enabled").isJsonArray()) {
                            JsonArray jArray = jObj.get("enabled").getAsJsonArray();
                            // application-token check
                            for (int i = 0; i < jArray.size(); i++) {
                                JsonObject appToken = jArray.get(i).getAsJsonObject();
                                if (appToken.get("applicationName") != null && appToken.get("applicationName")
                                        .getAsString().equals(config.getApplicationName())) {
                                    // found application-token, set as application-token
                                    applicationToken = appToken.get("token").getAsString();
                                    logger.debug("found application-token {} for application {}", applicationToken,
                                            config.getApplicationName());
                                    break;
                                }
                            }
                        }
                        if (applicationToken == null) {
                            // no token found, generate applicationToken
                            applicationToken = this.digitalSTROMClient
                                    .requestAppplicationToken(config.getApplicationName());
                            logger.debug(
                                    "no application-token for application {} found, generate an application-token {}",
                                    config.getApplicationName(), applicationToken);
                            if (applicationToken != null && !applicationToken.isBlank()) {
                                // enable applicationToken
                                if (!digitalSTROMClient.enableApplicationToken(applicationToken,
                                        digitalSTROMClient.login(config.getUserName(), config.getPassword()))) {
                                    // if enable failed set application-token = null so thats not will be set
                                    applicationToken = null;
                                }
                            }
                        }
                        if (applicationToken != null) {
                            logger.debug("application-token can be used");
                            config.setAppToken(applicationToken);
                            isAuthenticated = true;
                        }
                    }
                } else {
                    if (connListener != null) {
                        connListener.onConnectionStateChange(ConnectionListener.NOT_AUTHENTICATED,
                                ConnectionListener.WRONG_USER_OR_PASSWORD);
                        return;
                    }
                }
            }
            // remove password and username, to don't store them persistently
            if (isAuthenticated) {
                config.removeUsernameAndPassword();
                if (connListener != null) {
                    connListener.onConnectionStateChange(ConnectionListener.APPLICATION_TOKEN_GENERATED);
                }
            }
        } else if (!isAuthenticated) {
            if (connListener != null) {
                connListener.onConnectionStateChange(ConnectionListener.NOT_AUTHENTICATED,
                        ConnectionListener.NO_USER_PASSWORD);
            }
        }
    }

    private boolean checkUserPassword() {
        String userName = config.getUserName();
        String password = config.getPassword();
        return userName != null && !userName.isBlank() && password != null && !password.isBlank();
    }

    /**
     * This method is called whenever the connection to the digitalSTROM-Server is lost.
     *
     * @param reason
     */
    private void onConnectionLost(String reason) {
        if (connListener != null) {
            connListener.onConnectionStateChange(ConnectionListener.CONNECTION_LOST, reason);
        }
        connectionEstablished = false;
    }

    /**
     * This method is called whenever the connection to the digitalSTROM-Server is resumed.
     */
    private void onConnectionResumed() {
        if (connListener != null) {
            connListener.onConnectionStateChange(ConnectionListener.CONNECTION_RESUMED);
        }
        connectionEstablished = true;
    }

    @Override
    public void registerConnectionListener(ConnectionListener listener) {
        this.connListener = listener;
    }

    @Override
    public void unregisterConnectionListener() {
        this.connListener = null;
    }

    @Override
    public String getApplicationToken() {
        return config.getAppToken();
    }

    @Override
    public boolean removeApplicationToken() {
        String token = config.getAppToken();
        if (token != null && !token.isBlank()) {
            return digitalSTROMClient.revokeToken(token, null);
        }
        return true;
    }

    @Override
    public void updateConfig(String host, String username, String password, String applicationToken) {
        init(host, -1, -1, username, password, applicationToken, false);
    }

    @Override
    public void updateConfig(Config config) {
        if (this.config != null) {
            this.config.updateConfig(config);
        } else {
            this.config = config;
        }
        init(this.config, false);
    }

    @Override
    public void configHasBeenUpdated() {
        init(this.config, false);
    }

    @Override
    public Config getConfig() {
        return this.config;
    }
}
