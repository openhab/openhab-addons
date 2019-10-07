/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/*
 * This file is based on:
 *
 * WebOSTVService
 * Connect SDK
 *
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 19 Jan 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.lgwebos.internal.handler;

import static org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVMouseSocket.WebOSTVMouseSocketListener;
import org.openhab.binding.lgwebos.internal.handler.command.ServiceCommand;
import org.openhab.binding.lgwebos.internal.handler.command.ServiceSubscription;
import org.openhab.binding.lgwebos.internal.handler.core.AppInfo;
import org.openhab.binding.lgwebos.internal.handler.core.ChannelInfo;
import org.openhab.binding.lgwebos.internal.handler.core.CommandConfirmation;
import org.openhab.binding.lgwebos.internal.handler.core.LaunchSession;
import org.openhab.binding.lgwebos.internal.handler.core.LaunchSession.LaunchSessionType;
import org.openhab.binding.lgwebos.internal.handler.core.Response;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;
import org.openhab.binding.lgwebos.internal.handler.core.TextInputStatusInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * WebSocket to handle the communication with WebOS device.
 *
 * @author Hyun Kook Khang - Initial contribution
 * @author Sebastian Prehn - Web Socket implementation and adoption for openHAB
 */
@WebSocket()
@NonNullByDefault
public class LGWebOSTVSocket {

    private static final Gson GSON = new GsonBuilder().create();

    public enum State {
        DISCONNECTED,
        CONNECTING,
        REGISTERING,
        REGISTERED,
        DISCONNECTING
    }

    private State state = State.DISCONNECTED;

    private final ConfigProvider config;
    private final WebSocketClient client;
    private @Nullable Session session;
    private final URI destUri;
    private @Nullable WebOSTVSocketListener listener;
    private final LGWebOSTVKeyboardInput keyboardInput;
    /**
     * Requests to which we are awaiting response.
     */
    private HashMap<Integer, ServiceCommand<?>> requests = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(LGWebOSTVSocket.class);
    private int nextRequestId = 0;

    private static final String FOREGROUND_APP = "ssap://com.webos.applicationManager/getForegroundAppInfo";
    // private static final String APP_STATUS = "ssap://com.webos.service.appstatus/getAppStatus";
    // private static final String APP_STATE = "ssap://system.launcher/getAppState";
    private static final String VOLUME = "ssap://audio/getVolume";
    private static final String MUTE = "ssap://audio/getMute";
    // private static final String VOLUME_STATUS = "ssap://audio/getStatus";
    private static final String CHANNEL_LIST = "ssap://tv/getChannelList";
    private static final String CHANNEL = "ssap://tv/getCurrentChannel";
    // private static final String PROGRAM = "ssap://tv/getChannelProgramInfo";
    // private static final String CURRENT_PROGRAM = "ssap://tv/getChannelCurrentProgramInfo";
    // private static final String THREED_STATUS = "ssap://com.webos.service.tv.display/get3DStatus";

    public LGWebOSTVSocket(WebSocketClient client, ConfigProvider config, String host, int port) {
        this.config = config;
        this.client = client;
        this.keyboardInput = new LGWebOSTVKeyboardInput(this);

        try {
            this.destUri = new URI("ws://" + host + ":" + port);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("IP address or hostname provided is invalid: " + host);
        }
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        State oldState = this.state;
        if (oldState != state) {
            this.state = state;
            Optional.ofNullable(this.listener).ifPresent(l -> l.onStateChanged(oldState, this.state));
        }
    }

    public void setListener(@Nullable WebOSTVSocketListener listener) {
        this.listener = listener;
    }

    public void clearRequests() {
        requests.clear();
    }

    public void connect() {
        synchronized (this) {
            if (state != State.DISCONNECTED) {
                logger.debug("Not trying to connect. Current state is: {}", state);
                return;
            }
            setState(State.CONNECTING);
        }

        try {
            this.client.connect(this, this.destUri);
            logger.debug("Connecting to: {}", this.destUri);
        } catch (IOException e) {
            logger.debug("Unable to connect.", e);
            setState(State.DISCONNECTED);
        }
    }

    public void disconnect() {
        setState(State.DISCONNECTING);
        Optional.ofNullable(this.session).ifPresent(s -> s.close());
        setState(State.DISCONNECTED);
    }
    /*
     * WebSocket Callbacks
     */

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.debug("WebSocket Connected to: {}", session.getRemoteAddress().getAddress());
        this.session = session;
        sendHello();
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        Optional.ofNullable(this.listener).ifPresent(l -> l.onError(cause.getMessage()));
        logger.trace("Connection Error.", cause);
        if (State.CONNECTING == this.state) { // only a failed connection attempt.
            setState(State.DISCONNECTED);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.debug("WebSocket Closed - Code: {}, Reason: {}", statusCode, reason);
        this.requests.clear();
        this.session = null;
        setState(State.DISCONNECTED);
    }

    /*
     * WebOS WebSocket API specific Communication
     */
    void sendHello() {
        JsonObject packet = new JsonObject();
        packet.addProperty("id", nextRequestId());
        packet.addProperty("type", "hello");

        JsonObject payload = new JsonObject();
        payload.addProperty("appId", "org.openhab");
        payload.addProperty("appName", "openHAB");
        payload.addProperty("appRegion", Locale.getDefault().getDisplayCountry());
        packet.add("payload", payload);
        // the hello response will not contain id, therefore not registering in requests
        sendMessage(packet);
    }

    void sendRegister() {
        setState(State.REGISTERING);

        JsonObject packet = new JsonObject();
        int id = nextRequestId();
        packet.addProperty("id", id);
        packet.addProperty("type", "register");

        JsonObject manifest = new JsonObject();
        manifest.addProperty("manifestVersion", 1);

        String[] permissions = { "LAUNCH", "LAUNCH_WEBAPP", "APP_TO_APP", "CONTROL_AUDIO",
                "CONTROL_INPUT_MEDIA_PLAYBACK", "CONTROL_POWER", "READ_INSTALLED_APPS", "CONTROL_DISPLAY",
                "CONTROL_INPUT_JOYSTICK", "CONTROL_INPUT_MEDIA_RECORDING", "CONTROL_INPUT_TV", "READ_INPUT_DEVICE_LIST",
                "READ_NETWORK_STATE", "READ_TV_CHANNEL_LIST", "WRITE_NOTIFICATION_TOAST", "CONTROL_INPUT_TEXT",
                "CONTROL_MOUSE_AND_KEYBOARD", "READ_CURRENT_CHANNEL", "READ_RUNNING_APPS" };

        manifest.add("permissions", GSON.toJsonTree(permissions));

        JsonObject payload = new JsonObject();
        String key = config.getKey();
        if (key != null && !key.isEmpty()) {
            payload.addProperty("client-key", key);
        }
        payload.addProperty("pairingType", "PROMPT"); // PIN, COMBINED
        payload.add("manifest", manifest);
        packet.add("payload", payload);
        ResponseListener<JsonObject> dummyListener = new ResponseListener<JsonObject>() {

            @Override
            public void onSuccess(@Nullable JsonObject payload) {
                // Noting to do here. TV shows PROMPT dialog.
                // Waiting for message of type error or registered
            }

            @Override
            public void onError(String message) {
                logger.debug("Registration failed with message: {}", message);
                disconnect();
            }

        };

        this.requests.put(id, new ServiceSubscription<JsonObject>("dummy", payload, x -> x, dummyListener));
        sendMessage(packet);
    }

    private int nextRequestId() {
        int requestId;
        do {
            requestId = nextRequestId++;
        } while (requests.containsKey(requestId));
        return requestId;
    }

    public void sendCommand(ServiceCommand<?> command) {
        switch (state) {
            case REGISTERED:
                int requestId = nextRequestId();
                requests.put(requestId, command);
                JsonObject packet = new JsonObject();
                packet.addProperty("type", command.getType());
                packet.addProperty("id", requestId);
                packet.addProperty("uri", command.getTarget());
                JsonElement payload = command.getPayload();
                if (payload != null) {
                    packet.add("payload", payload);
                }
                this.sendMessage(packet);

                break;
            case CONNECTING:
            case REGISTERING:
            case DISCONNECTED:
            case DISCONNECTING:
                logger.warn("Skipping command {} for {} in state {}", command, command.getTarget(), state);
                break;
        }

    }

    public void unsubscribe(ServiceSubscription<?> subscription) {
        Optional<Entry<Integer, ServiceCommand<?>>> entry = this.requests.entrySet().stream()
                .filter(e -> e.getValue().equals(subscription)).findFirst();
        if (entry.isPresent()) {
            int requestId = entry.get().getKey();
            this.requests.remove(requestId);
            JsonObject packet = new JsonObject();
            packet.addProperty("type", "unsubscribe");
            packet.addProperty("id", requestId);
            sendMessage(packet);
        }
    }

    private void sendMessage(JsonObject json) {
        String msg = GSON.toJson(json);
        Session s = this.session;
        try {
            if (s != null) {
                logger.trace("Message [out]: {}", msg);
                s.getRemote().sendString(msg);
            } else {
                logger.warn("No Connection to TV, skipping [out]: {}", msg);
            }
        } catch (IOException e) {
            logger.warn("Unable to send message.", e);
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.trace("Message [in]: {}", message);
        Response response = GSON.fromJson(message, Response.class);
        ServiceCommand<?> request = null;

        if (response.getId() != null) {
            request = requests.get(response.getId());
            if (request == null) {
                logger.warn("Received a response with id {}, for which no request was found. This should not happen.",
                        response.getId());
            } else {
                // for subscriptions we want to keep the original
                // message, so that we have a reference to the response listener
                if (!(request instanceof ServiceSubscription<?>)) {
                    requests.remove(response.getId());
                }
            }
        }

        switch (response.getType()) {
            case "response":
                if (request == null) {
                    logger.debug("No matching request found for response message: {}", message);
                    break;
                }
                if (response.getPayload() == null) {
                    logger.debug("No payload in response message: {}", message);
                    break;
                }
                try {
                    request.processResponse(response.getPayload().getAsJsonObject());
                } catch (RuntimeException ex) {
                    // An uncaught runtime exception in @OnWebSocketMessage annotated method will cause the web socket
                    // implementation to call @OnWebSocketError callback in which we would reset the connection.
                    // Users have the ability to create miss-configurations in which IllegalArgumentException could be
                    // thrown
                    logger.warn("Error while processing message: {} - in response to request: {} - Error Message: {}",
                            message, request, ex.getMessage());
                }
                break;
            case "error":
                logger.debug("Error: {}", message);

                if (request == null) {
                    logger.warn("No matching request found for error message: {}", message);
                    break;
                }
                if (response.getPayload() == null) {
                    logger.warn("No payload in error message: {}", message);
                    break;
                }
                try {
                    request.processError(response.getError());
                } catch (RuntimeException ex) {
                    // An uncaught runtime exception in @OnWebSocketMessage annotated method will cause the web socket
                    // implementation to call @OnWebSocketError callback in which we would reset the connection.
                    // Users have the ability to create miss-configurations in which IllegalArgumentException could be
                    // thrown
                    logger.warn("Error while processing error: {} - in response to request: {} - Error Message: {}",
                            message, request, ex.getMessage());
                }
                break;
            case "hello":
                if (response.getPayload() == null) {
                    logger.warn("No payload in error message: {}", message);
                    break;
                }
                JsonObject deviceDescription = response.getPayload().getAsJsonObject();
                Map<String, String> map = new HashMap<>();
                map.put(PROPERTY_DEVICE_OS, deviceDescription.get("deviceOS").getAsString());
                map.put(PROPERTY_DEVICE_OS_VERSION, deviceDescription.get("deviceOSVersion").getAsString());
                map.put(PROPERTY_DEVICE_OS_RELEASE_VERSION,
                        deviceDescription.get("deviceOSReleaseVersion").getAsString());
                map.put(PROPERTY_LAST_CONNECTED, Instant.now().toString());
                config.storeProperties(map);
                sendRegister();
                break;
            case "registered":
                if (response.getPayload() == null) {
                    logger.warn("No payload in registered message: {}", message);
                    break;
                }
                this.requests.remove(response.getId());
                config.storeKey(response.getPayload().getAsJsonObject().get("client-key").getAsString());
                setState(State.REGISTERED);
                break;
        }

    }

    public boolean isConnected() {
        return state == State.REGISTERED;
    }

    public interface WebOSTVSocketListener {

        public void onStateChanged(State oldState, State newState);

        public void onError(String errorMessage);

    }

    public ServiceSubscription<Boolean> subscribeMute(ResponseListener<Boolean> listener) {
        ServiceSubscription<Boolean> request = new ServiceSubscription<>(MUTE, null,
                (jsonObj) -> jsonObj.get("mute").getAsBoolean(), listener);
        sendCommand(request);
        return request;
    }

    public ServiceSubscription<Float> subscribeVolume(ResponseListener<Float> listener) {
        ServiceSubscription<Float> request = new ServiceSubscription<>(VOLUME, null,
                jsonObj -> (float) (jsonObj.get("volume").getAsInt() / 100.0), listener);
        sendCommand(request);
        return request;
    }

    public void setMute(boolean isMute, ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://audio/setMute";
        JsonObject payload = new JsonObject();
        payload.addProperty("mute", isMute);

        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, payload,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void setVolume(float volume, ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://audio/setVolume";
        JsonObject payload = new JsonObject();
        int intVolume = Math.round(volume * 100.0f);
        payload.addProperty("volume", intVolume);
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, payload,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void volumeUp(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://audio/volumeUp";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void volumeDown(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://audio/volumeDown";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public ServiceSubscription<ChannelInfo> subscribeCurrentChannel(ResponseListener<ChannelInfo> listener) {
        ServiceSubscription<ChannelInfo> request = new ServiceSubscription<>(CHANNEL, null,
                jsonObj -> GSON.fromJson(jsonObj, ChannelInfo.class), listener);
        sendCommand(request);

        return request;
    }

    public void setChannel(ChannelInfo channelInfo, ResponseListener<CommandConfirmation> listener) {
        JsonObject payload = new JsonObject();
        if (channelInfo.getId() != null) {
            payload.addProperty("channelId", channelInfo.getId());
        }
        if (channelInfo.getChannelNumber() != null) {
            payload.addProperty("channelNumber", channelInfo.getChannelNumber());
        }
        setChannel(payload, listener);
    }

    private void setChannel(JsonObject payload, ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://tv/openChannel";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, payload,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void channelUp(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://tv/channelUp";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void channelDown(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://tv/channelDown";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void getChannelList(ResponseListener<List<ChannelInfo>> listener) {
        ServiceCommand<List<ChannelInfo>> request = new ServiceCommand<>(CHANNEL_LIST, null,
                jsonObj -> GSON.fromJson(jsonObj.get("channelList"), new TypeToken<ArrayList<ChannelInfo>>() {
                }.getType()), listener);
        sendCommand(request);
    }

    // TOAST

    public void showToast(String message, ResponseListener<CommandConfirmation> listener) {
        showToast(message, null, null, listener);
    }

    public void showToast(String message, @Nullable String iconData, @Nullable String iconExtension,
            ResponseListener<CommandConfirmation> listener) {
        JsonObject payload = new JsonObject();
        payload.addProperty("message", message);

        if (iconData != null && iconExtension != null) {
            payload.addProperty("iconData", iconData);
            payload.addProperty("iconExtension", iconExtension);
        }

        sendToast(payload, listener);
    }

    private void sendToast(JsonObject payload, ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://system.notifications/createToast";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, payload,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    // POWER
    public void powerOff(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://system/turnOff";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    // MEDIA CONTROL
    public void play(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://media.controls/play";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void pause(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://media.controls/pause";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void stop(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://media.controls/stop";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void rewind(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://media.controls/rewind";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    public void fastForward(ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://media.controls/fastForward";
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        sendCommand(request);
    }

    // APPS

    public void getAppList(final ResponseListener<List<AppInfo>> listener) {
        String uri = "ssap://com.webos.applicationManager/listApps";

        ServiceCommand<List<AppInfo>> request = new ServiceCommand<>(uri, null,
                jsonObj -> GSON.fromJson(jsonObj.get("apps"), new TypeToken<ArrayList<AppInfo>>() {
                }.getType()), listener);

        sendCommand(request);
    }

    public void launchAppWithInfo(AppInfo appInfo, ResponseListener<LaunchSession> listener) {
        launchAppWithInfo(appInfo, null, listener);
    }

    public void launchAppWithInfo(final AppInfo appInfo, @Nullable JsonObject params,
            final ResponseListener<LaunchSession> listener) {
        String uri = "ssap://system.launcher/launch";
        JsonObject payload = new JsonObject();

        final String appId = appInfo.getId();

        String contentId = null;

        if (params != null) {
            contentId = params.get("contentId").getAsString();
        }

        payload.addProperty("id", appId);

        if (contentId != null) {
            payload.addProperty("contentId", contentId);
        }

        if (params != null) {
            payload.add("params", params);
        }

        ServiceCommand<LaunchSession> request = new ServiceCommand<>(uri, payload, obj -> {
            LaunchSession launchSession = new LaunchSession();
            launchSession.setService(this);
            launchSession.setAppId(appId); // note that response uses id to mean appId
            launchSession.setSessionId(obj.get("sessionId").getAsString());
            launchSession.setSessionType(LaunchSessionType.App);
            return launchSession;
        }, listener);
        sendCommand(request);
    }

    public void launchBrowser(String url, final ResponseListener<LaunchSession> listener) {
        String uri = "ssap://system.launcher/open";
        JsonObject payload = new JsonObject();
        payload.addProperty("target", url);

        ServiceCommand<LaunchSession> request = new ServiceCommand<>(uri, payload, obj -> {
            LaunchSession launchSession = new LaunchSession();
            launchSession.setService(this);
            launchSession.setAppId(obj.get("id").getAsString()); // note that response uses id to mean appId
            launchSession.setSessionId(obj.get("sessionId").getAsString());
            launchSession.setSessionType(LaunchSessionType.App);
            return launchSession;
        }, listener);
        sendCommand(request);
    }

    public void closeLaunchSession(LaunchSession launchSession, ResponseListener<CommandConfirmation> listener) {
        LGWebOSTVSocket service = launchSession.getService();

        switch (launchSession.getSessionType()) {
            case App:
            case ExternalInputPicker:
                service.closeApp(launchSession, listener);
                break;

            /*
             * If we want to extend support for MediaPlayer or WebAppLauncher at some point, this is how it was handeled
             * in connectsdk:
             *
             * case Media:
             * if (service instanceof MediaPlayer) {
             * ((MediaPlayer) service).closeMedia(launchSession, listener);
             * }
             * break;
             *
             *
             * case WebApp:
             * if (service instanceof WebAppLauncher) {
             * ((WebAppLauncher) service).closeWebApp(launchSession, listener);
             * }
             * break;
             * case Unknown:
             */
            default:
                listener.onError("This DeviceService does not know ho to close this LaunchSession");
                break;
        }
    }

    public void closeApp(LaunchSession launchSession, ResponseListener<CommandConfirmation> listener) {
        String uri = "ssap://system.launcher/close";
        String appId = launchSession.getAppId();
        String sessionId = launchSession.getSessionId();

        JsonObject payload = new JsonObject();
        payload.addProperty("id", appId);
        payload.addProperty("sessionId", sessionId);

        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, payload,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        launchSession.getService().sendCommand(request);
    }

    public ServiceSubscription<AppInfo> subscribeRunningApp(ResponseListener<AppInfo> listener) {
        ServiceSubscription<AppInfo> request = new ServiceSubscription<>(FOREGROUND_APP, null,
                jsonObj -> GSON.fromJson(jsonObj, AppInfo.class), listener);
        sendCommand(request);
        return request;

    }

    // KEYBOARD

    public ServiceSubscription<TextInputStatusInfo> subscribeTextInputStatus(
            ResponseListener<TextInputStatusInfo> listener) {
        return keyboardInput.connect(listener);
    }

    public void sendText(String input) {
        keyboardInput.sendText(input);
    }

    public void sendEnter() {
        keyboardInput.sendEnter();
    }

    public void sendDelete() {
        keyboardInput.sendDel();
    }

    // MOUSE

    public void executeMouse(Consumer<LGWebOSTVMouseSocket> onConnected) {
        LGWebOSTVMouseSocket mouseSocket = new LGWebOSTVMouseSocket(this.client);
        mouseSocket.setListener(new WebOSTVMouseSocketListener() {

            @Override
            public void onStateChanged(LGWebOSTVMouseSocket.State oldState, LGWebOSTVMouseSocket.State newState) {
                switch (newState) {
                    case CONNECTED:
                        onConnected.accept(mouseSocket);
                        mouseSocket.disconnect();
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onError(String errorMessage) {
                logger.debug("Error in communication with Mouse Socket: {}", errorMessage);
            }
        });

        String uri = "ssap://com.webos.service.networkinput/getPointerInputSocket";

        ResponseListener<JsonObject> listener = new ResponseListener<JsonObject>() {

            @Override
            public void onSuccess(@Nullable JsonObject jsonObj) {
                if (jsonObj != null) {
                    String socketPath = jsonObj.get("socketPath").getAsString().replace("wss:", "ws:").replace(":3001/",
                            ":3000/");
                    try {
                        mouseSocket.connect(new URI(socketPath));
                    } catch (URISyntaxException e) {
                        logger.warn("Connect mouse error: {}", e.getMessage());
                    }
                }
            }

            @Override
            public void onError(String error) {
                logger.warn("Connect mouse error: {}", error);
            }
        };

        ServiceCommand<JsonObject> request = new ServiceCommand<>(uri, null, x -> x, listener);
        sendCommand(request);

    }

    public interface ConfigProvider {
        void storeKey(@Nullable String key);

        void storeProperties(Map<String, String> properties);

        @Nullable
        String getKey();
    }

}
