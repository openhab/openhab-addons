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

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Websocket class to retrieve app status
 *
 * @author Arjan Mels - Initial contribution
 * @author Nick Waterton - Updated to handle >2020 TV's
 */
@NonNullByDefault
class WebSocketV2 extends WebSocketBase {
    private final Logger logger = LoggerFactory.getLogger(WebSocketV2.class);

    private String host = "";
    private String className = "";
    // temporary storage for source appId.
    String currentSourceApp = "";

    WebSocketV2(RemoteControllerWebSocket remoteControllerWebSocket) {
        super(remoteControllerWebSocket);
        this.host = remoteControllerWebSocket.host;
        this.className = this.getClass().getSimpleName();
    }

    @SuppressWarnings("unused")
    @NonNullByDefault({})
    private static class JSONAcq {
        String id;
        boolean result;

        static class Error {
            String code;
            String details;
            String message;
            String status;
        }

        Error error;

        public String getId() {
            return Optional.ofNullable(id).orElse("");
        }

        public boolean getResult() {
            return Optional.ofNullable(result).orElse(false);
        }

        public String getErrorCode() {
            return Optional.ofNullable(error).map(a -> a.code).orElse("");
        }
    }

    @SuppressWarnings("unused")
    @NonNullByDefault({})
    private static class JSONMessage {
        String event;
        String id;

        static class Result {
            String id;
            String name;
            String running;
            String visible;
        }

        static class Data {
            String id;
            String token;
        }

        static class Error {
            String code;
            String details;
            String message;
            String status;
        }

        Result result;
        Data data;
        Error error;

        public String getEvent() {
            return Optional.ofNullable(event).orElse("");
        }

        public String getName() {
            return Optional.ofNullable(result).map(a -> a.name).orElse("");
        }

        public String getId() {
            return Optional.ofNullable(result).map(a -> a.id).orElse("");
        }

        public String getVisible() {
            return Optional.ofNullable(result).map(a -> a.visible).orElse("");
        }
    }

    @Override
    public void onWebSocketText(@Nullable String msgarg) {
        if (msgarg == null) {
            return;
        }
        String msg = msgarg.replace('\n', ' ');
        super.onWebSocketText(msg);
        try {
            JSONAcq jsonAcq = this.remoteControllerWebSocket.gson.fromJson(msg, JSONAcq.class);
            if (jsonAcq != null && !jsonAcq.getId().isBlank()) {
                if (jsonAcq.getResult()) {
                    // 3 second delay as app does not report visible until then.
                    remoteControllerWebSocket.getAppStatus(jsonAcq.getId());
                }
                if (!jsonAcq.getErrorCode().isBlank()) {
                    if ("404".equals(jsonAcq.getErrorCode())) {
                        // remove app from manual list if it's not installed using message id.
                        removeApp(jsonAcq.getId());
                    }
                }
                return;
            }
        } catch (JsonSyntaxException ignore) {
            // ignore error
        }
        try {
            JSONMessage jsonMsg = this.remoteControllerWebSocket.gson.fromJson(msg, JSONMessage.class);
            if (jsonMsg == null) {
                return;
            }
            if (!jsonMsg.getId().isBlank()) {
                handleResult(jsonMsg);
                return;
            }
            if (jsonMsg.error != null) {
                logger.debug("{}: WebSocketV2 Error received: {}", host, msg);
                return;
            }

            switch (jsonMsg.getEvent()) {
                case "ms.channel.connect":
                    logger.debug("{}: V2 channel connected. Token = {}", host, jsonMsg.data.token);

                    // update is requested from ed.installedApp.get event: small risk that this websocket is not
                    // yet connected
                    // on >2020 TV's this doesn't work so samsungTvAppWatchService should kick in automatically
                    break;
                case "ms.channel.clientConnect":
                    logger.debug("{}: V2 client connected", host);
                    break;
                case "ms.channel.clientDisconnect":
                    logger.debug("{}: V2 client disconnected", host);
                    break;
                default:
                    logger.debug("{}: V2 Unknown event: {}", host, msg);
            }
        } catch (JsonSyntaxException e) {
            logger.warn("{}: {}: Error ({}) in message: {}", host, className, e.getMessage(), msg);
        }
    }

    /**
     * Handle results of getappstatus response, updates current running app channel
     */
    private synchronized void handleResult(JSONMessage jsonMsg) {
        if (remoteControllerWebSocket.noApps()) {
            updateApps(jsonMsg);
        }
        if (!jsonMsg.getName().isBlank() && "true".equals(jsonMsg.getVisible())) {
            logger.debug("{}: Running app: {} = {}", host, jsonMsg.getId(), jsonMsg.getName());
            currentSourceApp = jsonMsg.getId();
            remoteControllerWebSocket.callback.currentAppUpdated(jsonMsg.getName());
        }
        if (currentSourceApp.equals(jsonMsg.getId()) && "false".equals(jsonMsg.getVisible())) {
            currentSourceApp = "";
            remoteControllerWebSocket.callback.currentAppUpdated("");
        }
    }

    @NonNullByDefault({})
    class JSONApp {
        public JSONApp(String id, String method) {
            this(id, method, null);
        }

        public JSONApp(String id, String method, @Nullable String metaTag) {
            // use message id to identify app to remove
            this.id = id;
            this.method = method;
            params.id = id;
            // not working
            params.metaTag = metaTag;
        }

        class Params {
            String id;
            String metaTag;
        }

        String method;
        String id;
        Params params = new Params();
    }

    /**
     * update manApp.name if it's incorrect
     */
    void updateApps(JSONMessage jsonMsg) {
        remoteControllerWebSocket.manApps.values().stream()
                .filter(a -> a.getAppId().equals(jsonMsg.getId()) && !a.getName().equals(jsonMsg.getName()))
                .peek(a -> logger.trace("{}: Updated app name {} to: {}", host, a.getName(), jsonMsg.getName()))
                .findFirst().ifPresent(a -> a.setName(jsonMsg.getName()));

        updateApp(jsonMsg);
    }

    /**
     * Fix app key, if it's the app id
     */
    @SuppressWarnings("null")
    void updateApp(JSONMessage jsonMsg) {
        if (remoteControllerWebSocket.manApps.containsKey(jsonMsg.getId())) {
            int type = remoteControllerWebSocket.manApps.get(jsonMsg.getId()).getType();
            remoteControllerWebSocket.manApps.put(jsonMsg.getName(),
                    remoteControllerWebSocket.new App(jsonMsg.getId(), jsonMsg.getName(), type));
            remoteControllerWebSocket.manApps.remove(jsonMsg.getId());
            logger.trace("{}: Updated app id {} name to: {}", host, jsonMsg.getId(), jsonMsg.getName());
            remoteControllerWebSocket.updateCount = 0;
        }
    }

    /**
     * Send get application status
     *
     * @param id appId of app to get status for
     */
    void getAppStatus(String id) {
        if (!id.isEmpty()) {
            boolean appType = getAppStream().filter(a -> a.getAppId().equals(id)).map(a -> a.getType() == 2).findFirst()
                    .orElse(true);
            // note apptype 4 always seems to return an error, so use default of 2 (true)
            String apptype = (appType) ? "ms.application.get" : "ms.webapplication.get";
            sendCommand(remoteControllerWebSocket.gson.toJson(new JSONApp(id, apptype)));
        }
    }

    /**
     * Closes current app if one is open
     *
     * @return false if no app was running, true if an app was closed
     */
    public boolean closeApp() {
        return getAppStream().filter(a -> a.appId.equals(currentSourceApp))
                .peek(a -> logger.debug("{}: closing app: {}", host, a.getName()))
                .map(a -> closeApp(a.getAppId(), a.getType() == 2)).findFirst().orElse(false);
    }

    public boolean closeApp(String appId, boolean appType) {
        String apptype = (appType) ? "ms.application.stop" : "ms.webapplication.stop";
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONApp(appId, apptype)));
        return true;
    }

    public void removeApp(String id) {
        remoteControllerWebSocket.manApps.values().removeIf(app -> app.getAppId().equals(id));
    }

    public Stream<RemoteControllerWebSocket.App> getAppStream() {
        return (remoteControllerWebSocket.noApps()) ? remoteControllerWebSocket.manApps.values().stream()
                : remoteControllerWebSocket.apps.values().stream();
    }

    /**
     * Launches app by appId, closes current app if sent ""
     * adds app if it's missing from manApps
     *
     * @param id AppId to launch
     * @param type (2 or 4)
     * @param metaTag optional url to launch (not working)
     */
    public void sendSourceApp(String id, boolean type, @Nullable String metaTag) {
        if (!id.isBlank()) {
            if (id.equals(currentSourceApp)) {
                logger.debug("{}: {} already running", host, id);
                return;
            }
            if ("org.tizen.browser".equals(id) && remoteControllerWebSocket.noApps()) {
                logger.warn("{}: using {} - you need a correct entry for \"Internet\" in the appslist file", host, id);
            }
            if (!getAppStream().anyMatch(a -> a.getAppId().equals(id))) {
                logger.debug("{}: Adding App : {}", host, id);
                remoteControllerWebSocket.manApps.put(id, remoteControllerWebSocket.new App(id, id, (type) ? 2 : 4));
            }
            String apptype = (type) ? "ms.application.start" : "ms.webapplication.start";
            sendCommand(remoteControllerWebSocket.gson.toJson(new JSONApp(id, apptype, metaTag)));
        } else {
            if (!closeApp()) {
                remoteControllerWebSocket.sendKeyPress(KeyCode.KEY_EXIT, 2000);
            }
        }
    }
}
