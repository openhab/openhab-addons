/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.plex.internal.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.plex.internal.config.PlexServerConfiguration;
import org.openhab.binding.plex.internal.dto.MediaContainer;
import org.openhab.binding.plex.internal.dto.MediaContainer.Device;
import org.openhab.binding.plex.internal.dto.MediaContainer.Device.Connection;
import org.openhab.binding.plex.internal.dto.NotificationContainer;
import org.openhab.binding.plex.internal.dto.User;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * The {@link PlexApiConnector} is responsible for communications with the PLEX server
 *
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
@NonNullByDefault
public class PlexApiConnector {
    private static final int REQUEST_TIMEOUT_MS = 2000;
    private static final String TOKEN_HEADER = "X-Plex-Token";
    private static final String SIGNIN_URL = "https://plex.tv/users/sign_in.xml";
    private static final String CLIENT_ID = "928dcjhd-91ka-la91-md7a-0msnan214563";
    private static final String API_URL = "https://plex.tv/api/resources?includeHttps=1";
    private final HttpClient httpClient;
    private WebSocketClient wsClient = new WebSocketClient();
    private PlexSocket plexSocket = new PlexSocket();

    private final Logger logger = LoggerFactory.getLogger(PlexApiConnector.class);
    private @Nullable PlexUpdateListener listener;

    private final XStream xStream = new XStream(new StaxDriver());
    private Gson gson = new Gson();
    private boolean isShutDown = false;

    private @Nullable ScheduledFuture<?> socketReconnect;
    private ScheduledExecutorService scheduler;
    private @Nullable URI uri;

    private String username = "";
    private String password = "";
    private String token = "";
    private String host = "";
    private int port = 0;
    private static String scheme = "";

    public PlexApiConnector(ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        setupXstream();
    }

    public void setParameters(PlexServerConfiguration connProps) {
        username = connProps.username;
        password = connProps.password;
        token = connProps.token;
        host = connProps.host;
        port = connProps.portNumber;
        wsClient = new WebSocketClient();
        plexSocket = new PlexSocket();
    }

    private String getSchemeWS() {
        return "http".equals(scheme) ? "ws" : "wss";
    }

    public boolean hasToken() {
        return !token.isBlank();
    }

    /**
     * Base configuration for XStream
     */
    private void setupXstream() {
        xStream.allowTypesByWildcard(
                new String[] { User.class.getPackageName() + ".**", MediaContainer.class.getPackageName() + ".**" });
        xStream.setClassLoader(PlexApiConnector.class.getClassLoader());
        xStream.ignoreUnknownElements();
        xStream.processAnnotations(User.class);
        xStream.processAnnotations(MediaContainer.class);
    }

    /**
     * Fetch the XML data and parse it through xStream to get a MediaContainer object
     *
     * @return
     */
    public @Nullable MediaContainer getSessionData() {
        try {
            String url = "https://" + host + ":" + port + "/status/sessions" + "?X-Plex-Token=" + token;
            logger.debug("Polling session data '{}'", url);
            return getFromXml(doHttpRequest("GET", url, getClientHeaders(), false), MediaContainer.class);
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("An exception occurred while polling the PLEX Server: '{}'", e.getMessage());
            return null;
        }
    }

    /**
     * Assemble the URL to include the Token
     *
     * @param url The url portion that is returned from the sessions call
     * @return the completed url that will be usable
     */
    public String getURL(String url) {
        return scheme + "://" + host + ":" + port + url + "?X-Plex-Token=" + token;
    }

    /**
     * This method will get an X-Token from the PLEX.tv server if one is not provided in the bridge config
     * and use this in the communication with the plex server
     */
    public void getToken() {
        User user;
        String authString = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        Properties headers = getClientHeaders();
        headers.put("Authorization", "Basic " + authString);

        try {
            user = getFromXml(doHttpRequest("POST", SIGNIN_URL, headers, true), User.class);
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("An exception occurred while fetching PLEX user token :'{}'", e.getMessage(), e);
            throw new ConfigurationException("Error occurred while fetching PLEX user token, please check config");
        }

        if (user.getAuthenticationToken() != null) {
            token = user.getAuthenticationToken();
            logger.debug("PLEX login successful using username/password");
        } else {
            throw new ConfigurationException("Invalid credentials for PLEX account, please check config");
        }
    }

    /**
     * This method will get the Api information from the PLEX.tv servers.
     */
    public boolean getApi() {
        try {
            MediaContainer api = getFromXml(doHttpRequest("GET", API_URL, getClientHeaders(), true),
                    MediaContainer.class);
            logger.trace("MediaContainer {}", api.getSize());
            if (api.getDevice() != null) {
                InetAddress hostAddress = InetAddress.getByName(host);
                for (Device tmpDevice : api.getDevice()) {
                    logger.trace("== Device {}", tmpDevice.getName());
                    if (tmpDevice.getConnection() != null) {
                        for (Connection tmpConn : tmpDevice.getConnection()) {
                            InetAddress tmpAddress = InetAddress.getByName(tmpConn.getAddress());
                            logger.trace("== Connection {}", tmpConn.getAddress());
                            if (hostAddress.equals(tmpAddress)) {
                                scheme = tmpConn.getProtocol();
                                logger.debug(
                                        "PLEX Api fetched. Found configured PLEX server in Api request, applied. Protocol used : {}",
                                        scheme);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("An exception occurred while fetching API :'{}'", e.getMessage(), e);
        }
        return false;
    }

    /**
     * Make an HTTP request and return the response as a string.
     *
     * @param method GET/POST
     * @param url What URL to call
     * @param headers Additional headers that will be used
     * @param verify Flag to indicate if ssl certificate checking should be done
     * @return Returns a string of the http response
     * @throws IOException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    private String doHttpRequest(String method, String url, Properties headers, boolean verify)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        final String response;
        if (verify) {
            // Requests sent to the PLEX.tv servers should use certificate checking
            response = HttpUtil.executeUrl(method, url, headers, null, null, REQUEST_TIMEOUT_MS);
        } else {
            // Requests sent to the local server need to bypass certificate checking via the custom httpClient
            final Request request = httpClient.newRequest(url).method(HttpUtil.createHttpMethod(method))
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            for (String httpHeaderKey : headers.stringPropertyNames()) {
                if (httpHeaderKey.equalsIgnoreCase(HttpHeader.USER_AGENT.toString())) {
                    request.agent(headers.getProperty(httpHeaderKey));
                } else {
                    request.header(httpHeaderKey, headers.getProperty(httpHeaderKey));
                }
            }
            final ContentResponse res = request.send();
            response = res.getContentAsString();
        }
        logger.trace("HTTP response: {}", response);
        return response;
    }

    /**
     * Return the class object that was represented by the xml input string.
     *
     * @param response The xml response to parse
     * @param type Class type for the XML parsing
     * @return Returns a class object from the input sring
     */
    private <T> T getFromXml(String response, Class<T> type) {
        @SuppressWarnings("unchecked")
        T obj = (T) xStream.fromXML(response);
        return obj;
    }

    /**
     * Fills in the header information for any calls to PLEX services
     *
     * @return Property headers
     */
    private Properties getClientHeaders() {
        Properties headers = new Properties();
        headers.put(HttpHeader.USER_AGENT, "openHAB/" + org.openhab.core.OpenHAB.getVersion() + " PLEX binding");
        headers.put("X-Plex-Client-Identifier", CLIENT_ID);
        headers.put("X-Plex-Product", "openHAB");
        headers.put("X-Plex-Version", "");
        headers.put("X-Plex-Device", "JRE11");
        headers.put("X-Plex-Device-Name", "openHAB");
        headers.put("X-Plex-Provides", "controller");
        headers.put("X-Plex-Platform", "Java");
        headers.put("X-Plex-Platform-Version", "JRE11");
        if (hasToken()) {
            headers.put(TOKEN_HEADER, token);
        }
        return headers;
    }

    /**
     * Register callback to PlexServerHandler
     *
     * @param listener function to call
     */
    public void registerListener(PlexUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * Dispose method, cleans up the websocket starts the reconnect logic
     */
    public void dispose() {
        isShutDown = true;
        try {
            wsClient.stop();
            ScheduledFuture<?> socketReconnect = this.socketReconnect;
            if (socketReconnect != null) {
                socketReconnect.cancel(true);
                this.socketReconnect = null;
            }
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Could not stop webSocketClient,  message {}", e.getMessage());
        }
    }

    /**
     * Connect to the websocket
     */
    public void connect() {
        logger.debug("Connecting to WebSocket");
        try {
            wsClient = new WebSocketClient(httpClient);
            uri = new URI(getSchemeWS() + "://" + host + ":32400/:/websockets/notifications?X-Plex-Token=" + token); // WS_ENDPOINT_TOUCHWAND);
        } catch (URISyntaxException e) {
            logger.debug("URI not valid {} message {}", uri, e.getMessage());
            return;
        }
        wsClient.setConnectTimeout(2000);
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        try {
            isShutDown = false;
            wsClient.start();
            wsClient.connect(plexSocket, uri, request);
            logger.debug("Connected to webSocket URI {}", uri);
        } catch (Exception e) {
            logger.debug("Could not connect webSocket URI {} message {}", uri, e.getMessage(), e);
        }
    }

    /**
     * PlexSocket class to handle the websocket connection to the PLEX server
     */
    @WebSocket(maxIdleTime = 360000) // WEBSOCKET_IDLE_TIMEOUT_MS)
    public class PlexSocket {
        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.debug("Connection closed: {} - {}", statusCode, reason);
            if (!isShutDown) {
                logger.debug("PLEX websocket closed - reconnecting");
                asyncWeb();
            }
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            logger.debug("PLEX Socket connected to {}", session.getRemoteAddress().getAddress());
        }

        @OnWebSocketMessage
        public void onMessage(String msg) {
            NotificationContainer notification = gson.fromJson(msg, NotificationContainer.class);
            if (notification != null) {
                PlexUpdateListener listenerLocal = listener;
                if (listenerLocal != null && "playing".equals(notification.getNotificationContainer().getType())) {
                    listenerLocal.onItemStatusUpdate(
                            notification.getNotificationContainer().getPlaySessionStateNotification().get(0)
                                    .getSessionKey(),
                            notification.getNotificationContainer().getPlaySessionStateNotification().get(0)
                                    .getState());
                }
            }
        }

        @OnWebSocketError
        public void onError(Throwable cause) {
            if (!isShutDown) {
                logger.debug("WebSocket onError - reconnecting");
                asyncWeb();
            }
        }

        private void asyncWeb() {
            ScheduledFuture<?> mySocketReconnect = socketReconnect;
            if (mySocketReconnect == null || mySocketReconnect.isDone()) {
                socketReconnect = scheduler.schedule(PlexApiConnector.this::connect, 5, TimeUnit.SECONDS); // WEBSOCKET_RECONNECT_INTERVAL_SEC,
            }
        }
    }

    /**
     * Handles control commands to the plex player.
     *
     * Supports:
     * - Play / Pause
     * - Previous / Next
     *
     * @param command The control command
     * @param playerID The ID of the PLEX player
     */
    public void controlPlayer(Command command, String playerID) {
        logger.debug("Controlling player '{}' with command '{}'", playerID, command);

        String commandPath = null;
        if (command instanceof PlayPauseType) {
            if (command.equals(PlayPauseType.PLAY)) {
                commandPath = "/player/playback/play";
            }
            if (command.equals(PlayPauseType.PAUSE)) {
                commandPath = "/player/playback/pause";
            }
        }

        if (command instanceof NextPreviousType) {
            if (command.equals(NextPreviousType.PREVIOUS)) {
                commandPath = "/player/playback/skipPrevious";
            }
            if (command.equals(NextPreviousType.NEXT)) {
                commandPath = "/player/playback/skipNext";
            }
        }

        if (commandPath != null) {
            try {
                String url = "https://" + host + ":" + port + commandPath;
                Properties headers = getClientHeaders();
                headers.put("X-Plex-Target-Client-Identifier", playerID);
                doHttpRequest("GET", url, headers, false);
                logger.debug("Command '{}' sent to player '{}' with url {}", commandPath, playerID, url);
            } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
                logger.debug("An exception occurred trying to send command '{}' to the player: {}", commandPath,
                        e.getMessage());
            }
        } else {
            logger.warn("Could not match command '{}' to an action", command);
        }
    }
}
