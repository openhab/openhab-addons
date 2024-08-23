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
package org.openhab.binding.samsungtv.internal.protocol;

import static org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.samsungtv.internal.SamsungTvAppWatchService;
import org.openhab.binding.samsungtv.internal.Utils;
import org.openhab.binding.samsungtv.internal.service.RemoteControllerService;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link RemoteControllerWebSocket} is responsible for sending key codes to the
 * Samsung TV via the websocket protocol (for newer TV's).
 *
 * @author Arjan Mels - Initial contribution
 * @author Arjan Mels - Moved websocket inner classes to standalone classes
 * @author Nick Waterton - added Action enum, manual app handling and some refactoring
 */
@NonNullByDefault
public class RemoteControllerWebSocket extends RemoteController implements Listener {

    private final Logger logger = LoggerFactory.getLogger(RemoteControllerWebSocket.class);

    private static final String WS_ENDPOINT_REMOTE_CONTROL = "/api/v2/channels/samsung.remote.control";
    private static final String WS_ENDPOINT_ART = "/api/v2/channels/com.samsung.art-app";
    private static final String WS_ENDPOINT_V2 = "/api/v2";

    // WebSocket helper classes
    private final WebSocketRemote webSocketRemote;
    private final WebSocketArt webSocketArt;
    private final WebSocketV2 webSocketV2;

    // refresh limit for current app update (in seconds)
    private static final long UPDATE_CURRENT_APP_REFRESH_SECONDS = 10;
    private Instant previousUpdateCurrentApp = Instant.MIN;

    // JSON parser class. Also used by WebSocket handlers.
    public final Gson gson = new Gson();

    // Callback class. Also used by WebSocket handlers.
    final RemoteControllerService callback;

    // Websocket client class shared by WebSocket handlers.
    final WebSocketClient client;

    // App File servicce
    private final SamsungTvAppWatchService samsungTvAppWatchService;

    // list instaled apps after 2 updates
    public int updateCount = 0;

    // UUID used for data exchange via websockets
    final UUID uuid = UUID.randomUUID();

    // Description of Apps
    public class App {
        public String appId;
        public String name;
        public int type;

        App(String appId, String name, int type) {
            this.appId = appId;
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public String getAppId() {
            return appId != null ? appId : "";
        }

        public String getName() {
            return name != null ? name : "";
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return Optional.ofNullable(type).orElse(2);
        }
    }

    // Map of all available apps
    public Map<String, App> apps = new ConcurrentHashMap<>();
    // manually added apps (from File)
    public Map<String, App> manApps = new ConcurrentHashMap<>();

    /**
     * The {@link Action} presents available actions for keys with Samsung TV.
     *
     */
    public static enum Action {

        CLICK("Click"),
        PRESS("Press"),
        RELEASE("Release"),
        MOVE("Move"),
        END("End"),
        TEXT("Text"),
        MOUSECLICK("MouseClick");

        private final String value;

        Action() {
            value = "Click";
        }

        Action(String newvalue) {
            this.value = newvalue;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Create and initialize remote controller instance.
     *
     * @param host Host name of the Samsung TV.
     * @param port TCP port of the remote controller protocol.
     * @param appName Application name used to send key codes.
     * @param uniqueId Unique Id used to send key codes.
     * @param callback RemoteControllerService callback
     * @throws RemoteControllerException
     */
    public RemoteControllerWebSocket(String host, int port, String appName, String uniqueId,
            RemoteControllerService callback) throws RemoteControllerException {
        super(host, port, appName, uniqueId);
        this.callback = callback;

        WebSocketFactory webSocketFactory = callback.getWebSocketFactory();
        if (webSocketFactory == null) {
            throw new RemoteControllerException("No WebSocketFactory available");
        }

        this.samsungTvAppWatchService = new SamsungTvAppWatchService(host, this);

        SslContextFactory sslContextFactory = new SslContextFactory.Client( /* trustall= */ true);
        /* remove extra filters added by jetty on cipher suites */
        sslContextFactory.setExcludeCipherSuites();
        client = webSocketFactory.createWebSocketClient("samsungtv", sslContextFactory);
        client.addLifeCycleListener(this);

        webSocketRemote = new WebSocketRemote(this);
        webSocketArt = new WebSocketArt(this);
        webSocketV2 = new WebSocketV2(this);
    }

    public boolean isConnected() {
        if (callback.getArtModeSupported()) {
            return webSocketRemote.isConnected() && webSocketArt.isConnected();
        }
        return webSocketRemote.isConnected();
    }

    public void openConnection() throws RemoteControllerException {
        logger.trace("{}: openConnection()", host);

        if (!(client.isStarted() || client.isStarting())) {
            logger.debug("{}: RemoteControllerWebSocket start Client", host);
            try {
                client.start();
                client.setMaxBinaryMessageBufferSize(1024 * 1024);
                // websocket connect will be done in lifetime handler
                return;
            } catch (Exception e) {
                logger.warn("{}: Cannot connect to websocket remote control interface: {}", host, e.getMessage());
                throw new RemoteControllerException(e);
            }
        }
        connectWebSockets();
    }

    private void logResult(String msg, Throwable cause) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}: {}: ", host, msg, cause);
        } else {
            logger.warn("{}: {}: {}", host, msg, cause.getMessage());
        }
    }

    private void connectWebSockets() {
        logger.trace("{}: connectWebSockets()", host);

        String encodedAppName = Utils.b64encode(appName);

        String protocol = PROTOCOL_SECUREWEBSOCKET.equals(callback.handler.configuration.getProtocol()) ? "wss" : "ws";
        try {
            String token = callback.handler.configuration.getWebsocketToken();
            if ("wss".equals(protocol) && token.isBlank()) {
                logger.warn(
                        "{}: WebSocketRemote connecting without Token, please accept the connection on the TV within 30 seconds",
                        host);
            }
            webSocketRemote.connect(new URI(protocol, null, host, port, WS_ENDPOINT_REMOTE_CONTROL,
                    "name=" + encodedAppName + (token.isBlank() ? "" : "&token=" + token), null));
        } catch (RemoteControllerException | URISyntaxException e) {
            logResult("Problem connecting to remote websocket", e);
        }

        try {
            webSocketArt.connect(new URI(protocol, null, host, port, WS_ENDPOINT_ART, "name=" + encodedAppName, null));
        } catch (RemoteControllerException | URISyntaxException e) {
            logResult("Problem connecting to artmode websocket", e);
        }

        try {
            webSocketV2.connect(new URI(protocol, null, host, port, WS_ENDPOINT_V2, "name=" + encodedAppName, null));
        } catch (RemoteControllerException | URISyntaxException e) {
            logResult("Problem connecting to V2 websocket", e);
        }
    }

    private void closeConnection() throws RemoteControllerException {
        logger.debug("{}: RemoteControllerWebSocket closeConnection", host);

        try {
            webSocketRemote.close();
            webSocketArt.close();
            webSocketV2.close();
            client.stop();
        } catch (Exception e) {
            throw new RemoteControllerException(e);
        }
    }

    @Override
    public void close() throws RemoteControllerException {
        logger.debug("{}: RemoteControllerWebSocket close", host);
        closeConnection();
    }

    public boolean noApps() {
        return apps.isEmpty();
    }

    public void listApps() {
        Stream<Map.Entry<String, App>> st = (noApps()) ? manApps.entrySet().stream() : apps.entrySet().stream();
        logger.debug("{}: Installed Apps: {}", host,
                st.map(entry -> entry.getValue().appId + " = " + entry.getKey()).collect(Collectors.joining(", ")));
    }

    /**
     * Retrieve app status for all apps. In the WebSocketv2 handler the currently running app will be determined
     */
    public synchronized void updateCurrentApp() {
        // limit noApp refresh rate
        if (noApps()
                && Instant.now().isBefore(previousUpdateCurrentApp.plusSeconds(UPDATE_CURRENT_APP_REFRESH_SECONDS))) {
            return;
        }
        previousUpdateCurrentApp = Instant.now();
        if (webSocketV2.isNotConnected()) {
            logger.warn("{}: Cannot retrieve current app webSocketV2 is not connected", host);
            return;
        }
        // if noapps by this point, start file app service
        if (updateCount >= 1 && noApps() && !samsungTvAppWatchService.getStarted()) {
            samsungTvAppWatchService.start();
        }
        // list apps
        if (updateCount++ == 2) {
            listApps();
        }
        for (App app : (noApps()) ? manApps.values() : apps.values()) {
            webSocketV2.getAppStatus(app.getAppId());
            // prevent being called again if this takes a while
            previousUpdateCurrentApp = Instant.now();
        }
    }

    /**
     * Update manual App list from file (called from SamsungTvAppWatchService)
     */
    public void updateAppList(List<String> fileApps) {
        previousUpdateCurrentApp = Instant.now();
        manApps.clear();
        fileApps.forEach(line -> {
            try {
                App app = gson.fromJson(line, App.class);
                if (app != null) {
                    manApps.put(app.getName(), new App(app.getAppId(), app.getName(), app.getType()));
                    logger.debug("{}: Added app: {}/{}", host, app.getName(), app.getAppId());
                }
            } catch (JsonSyntaxException e) {
                logger.warn("{}: cannot add app, wrong format {}: {}", host, line, e.getMessage());
            }
        });
        addKnownAppIds();
        updateCount = 0;
    }

    /**
     * Add all know app id's to manApps
     */
    public void addKnownAppIds() {
        KnownAppId.stream().filter(id -> !manApps.values().stream().anyMatch(a -> a.getAppId().equals(id)))
                .forEach(id -> {
                    previousUpdateCurrentApp = Instant.now();
                    manApps.put(id, new App(id, id, 2));
                    logger.debug("{}: Added Known appId: {}", host, id);
                });
    }

    /**
     * Send key code to Samsung TV.
     *
     * @param key Key code to send.
     */
    public void sendKey(Object key) {
        if (key instanceof KeyCode keyAsKeyCode) {
            sendKey(keyAsKeyCode, Action.CLICK);
        } else if (key instanceof String) {
            sendKey((String) key);
        }
    }

    public void sendKey(String value) {
        try {
            if (value.startsWith("{")) {
                sendKeyData(value, Action.MOVE);
            } else if ("LeftClick".equals(value) || "RightClick".equals(value)) {
                sendKeyData(value, Action.MOUSECLICK);
            } else if (value.isEmpty()) {
                sendKeyData("", Action.END);
            } else {
                sendKeyData(value, Action.TEXT);
            }
        } catch (RemoteControllerException e) {
            logger.debug("{}: Couldn't send Text/Mouse move {}", host, e.getMessage());
        }
    }

    public void sendKey(KeyCode key, Action action) {
        try {
            sendKeyData(key, action);
        } catch (RemoteControllerException e) {
            logger.debug("{}: Couldn't send command {}", host, e.getMessage());
        }
    }

    public void sendKeyPress(KeyCode key, int duration) {
        sendKey(key, Action.PRESS);
        // send key release in duration milliseconds
        @Nullable
        ScheduledExecutorService scheduler = callback.getScheduler();
        if (scheduler != null) {
            scheduler.schedule(() -> {
                if (isConnected()) {
                    sendKey(key, Action.RELEASE);
                }
            }, duration, TimeUnit.MILLISECONDS);
        }
    }

    private void sendKeyData(Object key, Action action) throws RemoteControllerException {
        logger.debug("{}: Try to send Key: {}, Action: {}", host, key, action);
        webSocketRemote.sendKeyData(action.toString(), key.toString());
    }

    public void sendSourceApp(String appName) {
        if (appName.toLowerCase().contains("slideshow")) {
            webSocketArt.setSlideshow(appName);
        } else {
            sendSourceApp(appName, null);
        }
    }

    public void sendSourceApp(final String appName, @Nullable String url) {
        Stream<Map.Entry<String, App>> st = (noApps()) ? manApps.entrySet().stream() : apps.entrySet().stream();
        boolean found = st.filter(a -> a.getKey().equals(appName) || a.getValue().name.equals(appName))
                .map(a -> sendSourceApp(a.getValue().appId, a.getValue().type == 2, url)).findFirst().orElse(false);
        if (!found) {
            // treat appName as appId with optional type number eg "3201907018807, 2"
            String[] appArray = (url == null) ? appName.trim().split(",") : "org.tizen.browser,4".split(",");
            sendSourceApp(appArray[0].trim(), (appArray.length > 1) ? "2".equals(appArray[1].trim()) : true, url);
        }
    }

    public boolean sendSourceApp(String appId, boolean type, @Nullable String url) {
        if (noApps()) {
            // 2020 TV's and later use webSocketV2 for app launch
            webSocketV2.sendSourceApp(appId, type, url);
        } else {
            if (webSocketV2.isConnected() && url == null) {
                // it seems all Tizen TV's can use webSocketV2 if it connects
                webSocketV2.sendSourceApp(appId, type, url);
            } else {
                webSocketRemote.sendSourceApp(appId, type, url);
            }
        }
        return true;
    }

    public void sendUrl(String url) {
        String processedUrl = url.replace("/", "\\/");
        sendSourceApp("Internet", processedUrl);
    }

    public boolean closeApp() {
        return webSocketV2.closeApp();
    }

    /**
     * Get app status after 3 second delay (apps take 3s to launch)
     */
    public void getAppStatus(String id) {
        @Nullable
        ScheduledExecutorService scheduler = callback.getScheduler();
        if (scheduler != null) {
            scheduler.schedule(() -> {
                if (webSocketV2.isConnected()) {
                    if (!id.isBlank()) {
                        webSocketV2.getAppStatus(id);
                    } else {
                        updateCurrentApp();
                    }
                }
            }, 3000, TimeUnit.MILLISECONDS);
        }
    }

    public void getArtmodeStatus(String... optionalRequests) {
        webSocketArt.getArtmodeStatus(optionalRequests);
    }

    @Override
    public void lifeCycleStarted(@Nullable LifeCycle arg0) {
        logger.trace("{}: WebSocketClient started", host);
        connectWebSockets();
    }

    @Override
    public void lifeCycleFailure(@Nullable LifeCycle arg0, @Nullable Throwable throwable) {
        logger.warn("{}: WebSocketClient failure: {}", host, throwable != null ? throwable.toString() : null);
    }

    @Override
    public void lifeCycleStarting(@Nullable LifeCycle arg0) {
        logger.trace("{}: WebSocketClient starting", host);
    }

    @Override
    public void lifeCycleStopped(@Nullable LifeCycle arg0) {
        logger.trace("{}: WebSocketClient stopped", host);
    }

    @Override
    public void lifeCycleStopping(@Nullable LifeCycle arg0) {
        logger.trace("{}: WebSocketClient stopping", host);
    }
}
