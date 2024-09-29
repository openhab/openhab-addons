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
import java.net.ConnectException;
import java.net.SocketTimeoutException;
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
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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

    private static final int DISCONNECTING_DELAY_SECONDS = 2;

    private static final Gson GSON = new GsonBuilder().create();

    private final Logger logger = LoggerFactory.getLogger(LGWebOSTVSocket.class);

    private final ConfigProvider config;
    private final WebSocketClient client;
    private final URI destUri;
    private final LGWebOSTVKeyboardInput keyboardInput;
    private final ScheduledExecutorService scheduler;
    private final Protocol protocol;

    public enum State {
        DISCONNECTING,
        DISCONNECTED,
        CONNECTING,
        REGISTERING,
        REGISTERED
    }

    private enum Protocol {
        WEB_SOCKET("ws", DEFAULT_WS_PORT),
        WEB_SOCKET_SECURE("wss", DEFAULT_WSS_PORT);

        private Protocol(String name, int port) {
            this.name = name;
            this.port = port;
        }

        public String name;
        public int port;
    }

    private State state = State.DISCONNECTED;

    private @Nullable Session session;
    private @Nullable Future<?> sessionFuture;
    private @Nullable WebOSTVSocketListener listener;

    /**
     * Requests to which we are awaiting response.
     */
    private HashMap<Integer, ServiceCommand<?>> requests = new HashMap<>();

    private int nextRequestId = 0;

    private @Nullable ScheduledFuture<?> disconnectingJob;

    public LGWebOSTVSocket(WebSocketClient client, ConfigProvider config, String host, boolean useTLS,
            ScheduledExecutorService scheduler) {
        this.config = config;
        this.client = client;
        this.keyboardInput = new LGWebOSTVKeyboardInput(this);
        this.protocol = useTLS ? Protocol.WEB_SOCKET_SECURE : Protocol.WEB_SOCKET;

        try {
            this.destUri = new URI(protocol.name + "://" + host + ":" + protocol.port);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("IP address or hostname provided is invalid: " + host);
        }

        this.scheduler = scheduler;
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        logger.debug("setState new {} - current {}", state, this.state);
        State oldState = this.state;
        if (oldState != state) {
            this.state = state;
            Optional.ofNullable(this.listener).ifPresent(l -> l.onStateChanged(this.state));
        }
    }

    public void setListener(@Nullable WebOSTVSocketListener listener) {
        this.listener = listener;
    }

    public void clearRequests() {
        requests.clear();
    }

    public void connect() {
        try {
            sessionFuture = this.client.connect(this, this.destUri);
            logger.debug("Connecting to: {}", this.destUri);
        } catch (IOException e) {
            logger.debug("Unable to connect.", e);
        }
    }

    public void disconnect() {
        Optional.ofNullable(this.session).ifPresent(s -> s.close());
        Future<?> future = sessionFuture;
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        stopDisconnectingJob();
        setState(State.DISCONNECTED);
    }

    private void disconnecting() {
        logger.debug("disconnecting");
        if (state == State.REGISTERED) {
            setState(State.DISCONNECTING);
        }
    }

    private void scheduleDisconectingJob() {
        ScheduledFuture<?> job = disconnectingJob;
        if (job == null || job.isCancelled()) {
            logger.debug("Schedule disconecting job");
            disconnectingJob = scheduler.schedule(this::disconnecting, DISCONNECTING_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void stopDisconnectingJob() {
        ScheduledFuture<?> job = disconnectingJob;
        if (job != null && !job.isCancelled()) {
            logger.debug("Stop disconnecting job");
            job.cancel(true);
        }
        disconnectingJob = null;
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
        logger.trace("Connection Error", cause);
        if (cause instanceof SocketTimeoutException && "Connect Timeout".equals(cause.getMessage())) {
            // this is expected during connection attempts while TV is off
            setState(State.DISCONNECTED);
            return;
        }
        if (cause instanceof ConnectException && "Connection refused".equals(cause.getMessage())) {
            // this is expected during TV startup or shutdown
            return;
        }

        String message = cause.getMessage();
        Optional.ofNullable(this.listener).ifPresent(l -> l.onError(message != null ? message : ""));
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
        setState(State.CONNECTING);

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
        if (!key.isEmpty()) {
            payload.addProperty("client-key", key);
        }
        payload.addProperty("pairingType", "PROMPT"); // PIN, COMBINED
        payload.add("manifest", manifest);
        packet.add("payload", payload);
        ResponseListener<JsonObject> dummyListener = new ResponseListener<>() {

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

        this.requests.put(id, new ServiceSubscription<>("dummy", payload, x -> x, dummyListener));
        sendMessage(packet, !key.isEmpty());
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
            case DISCONNECTING:
            case DISCONNECTED:
                logger.debug("Skipping {} command {} for {} in state {}", command.getType(), command,
                        command.getTarget(), state);
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
        sendMessage(json, false);
    }

    private void sendMessage(JsonObject json, boolean checkKey) {
        String msg = GSON.toJson(json);
        Session s = this.session;
        try {
            if (s != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Message [out]: {}", checkKey ? GSON.toJson(maskKeyInJson(json)) : msg);
                }
                s.getRemote().sendString(msg);
            } else {
                logger.warn("No Connection to TV, skipping [out]: {}",
                        checkKey ? GSON.toJson(maskKeyInJson(json)) : msg);
            }
        } catch (IOException e) {
            logger.warn("Unable to send message.", e);
        }
    }

    private JsonObject maskKeyInJson(JsonObject json) {
        if (json.has("payload") && json.getAsJsonObject("payload").has("client-key")) {
            JsonObject jsonCopy = json.deepCopy();
            JsonObject payload = jsonCopy.getAsJsonObject("payload");
            payload.remove("client-key");
            payload.addProperty("client-key", "***");
            return jsonCopy;
        }
        return json;
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        Response response = GSON.fromJson(message, Response.class);
        JsonElement payload = response.getPayload();
        JsonObject jsonPayload = payload == null ? null : payload.getAsJsonObject();
        String messageToLog = (jsonPayload != null && jsonPayload.has("client-key")) ? "***" : message;
        logger.trace("Message [in]: {}", messageToLog);
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
                    logger.debug("No matching request found for response message: {}", messageToLog);
                    break;
                }
                if (payload == null) {
                    logger.debug("No payload in response message: {}", messageToLog);
                    break;
                }
                try {
                    request.processResponse(jsonPayload);
                } catch (RuntimeException ex) {
                    // An uncaught runtime exception in @OnWebSocketMessage annotated method will cause the web socket
                    // implementation to call @OnWebSocketError callback in which we would reset the connection.
                    // Users have the ability to create miss-configurations in which IllegalArgumentException could be
                    // thrown
                    logger.warn("Error while processing message: {} - in response to request: {} - Error Message: {}",
                            messageToLog, request, ex.getMessage());
                }
                break;
            case "error":
                logger.debug("Error: {}", messageToLog);

                if (request == null) {
                    logger.warn("No matching request found for error message: {}", messageToLog);
                    break;
                }
                if (payload == null) {
                    logger.warn("No payload in error message: {}", messageToLog);
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
                            messageToLog, request, ex.getMessage());
                }
                break;
            case "hello":
                if (state != State.CONNECTING) {
                    logger.debug("Skipping response {}, not in CONNECTING state, state was {}", messageToLog, state);
                    break;
                }
                if (jsonPayload == null) {
                    logger.warn("No payload in error message: {}", messageToLog);
                    break;
                }
                Map<String, String> map = new HashMap<>();
                map.put(PROPERTY_DEVICE_OS, jsonPayload.get("deviceOS").getAsString());
                map.put(PROPERTY_DEVICE_OS_VERSION, jsonPayload.get("deviceOSVersion").getAsString());
                map.put(PROPERTY_DEVICE_OS_RELEASE_VERSION, jsonPayload.get("deviceOSReleaseVersion").getAsString());
                map.put(PROPERTY_LAST_CONNECTED, Instant.now().toString());
                config.storeProperties(map);
                sendRegister();
                break;
            case "registered":
                if (state != State.REGISTERING) {
                    logger.debug("Skipping response {}, not in REGISTERING state, state was {}", messageToLog, state);
                    break;
                }
                if (jsonPayload == null) {
                    logger.warn("No payload in registered message: {}", messageToLog);
                    break;
                }
                this.requests.remove(response.getId());
                config.storeKey(jsonPayload.get("client-key").getAsString());
                setState(State.REGISTERED);
                break;
        }
    }

    public interface WebOSTVSocketListener {

        public void onStateChanged(State state);

        public void onError(String errorMessage);
    }

    public ServiceSubscription<Boolean> subscribeMute(ResponseListener<Boolean> listener) {
        ServiceSubscription<Boolean> request = new ServiceSubscription<>(MUTE, null,
                (jsonObj) -> jsonObj.get("mute").getAsBoolean(), listener);
        sendCommand(request);
        return request;
    }

    public ServiceCommand<Boolean> getMute(ResponseListener<Boolean> listener) {
        ServiceCommand<Boolean> request = new ServiceCommand<>(MUTE, null,
                (jsonObj) -> jsonObj.get("mute").getAsBoolean(), listener);
        sendCommand(request);
        return request;
    }

    private Float volumeFromResponse(JsonObject jsonObj) {
        JsonObject parent = jsonObj.has("volumeStatus") ? jsonObj.getAsJsonObject("volumeStatus") : jsonObj;
        return parent.get("volume").getAsInt() >= 0 ? (float) (parent.get("volume").getAsInt() / 100.0) : Float.NaN;
    }

    public ServiceSubscription<Float> subscribeVolume(ResponseListener<Float> listener) {
        ServiceSubscription<Float> request = new ServiceSubscription<>(VOLUME, null, this::volumeFromResponse,
                listener);
        sendCommand(request);
        return request;
    }

    public ServiceCommand<Float> getVolume(ResponseListener<Float> listener) {
        ServiceCommand<Float> request = new ServiceCommand<>(VOLUME, null, this::volumeFromResponse, listener);
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

    public ServiceCommand<ChannelInfo> getCurrentChannel(ResponseListener<ChannelInfo> listener) {
        ServiceCommand<ChannelInfo> request = new ServiceCommand<>(CHANNEL, null,
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

        ResponseListener<CommandConfirmation> interceptor = new ResponseListener<>() {

            @Override
            public void onSuccess(CommandConfirmation confirmation) {
                if (confirmation.getReturnValue()) {
                    disconnecting();
                }
                listener.onSuccess(confirmation);
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
            }
        };
        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, null,
                x -> GSON.fromJson(x, CommandConfirmation.class), interceptor);
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
            if (obj.has("sessionId")) {
                launchSession.setSessionId(obj.get("sessionId").getAsString());
                launchSession.setSessionType(LaunchSessionType.App);
            } else {
                launchSession.setSessionType(LaunchSessionType.Unknown);
            }
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
            if (obj.has("sessionId")) {
                launchSession.setSessionId(obj.get("sessionId").getAsString());
                launchSession.setSessionType(LaunchSessionType.App);
            } else {
                launchSession.setSessionType(LaunchSessionType.Unknown);
            }
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

        JsonObject payload = new JsonObject();
        payload.addProperty("id", launchSession.getAppId());
        payload.addProperty("sessionId", launchSession.getSessionId());

        ServiceCommand<CommandConfirmation> request = new ServiceCommand<>(uri, payload,
                x -> GSON.fromJson(x, CommandConfirmation.class), listener);
        launchSession.getService().sendCommand(request);
    }

    public ServiceSubscription<AppInfo> subscribeRunningApp(ResponseListener<AppInfo> listener) {
        ResponseListener<AppInfo> interceptor = new ResponseListener<>() {

            @Override
            public void onSuccess(AppInfo appInfo) {
                if (appInfo.getId().isEmpty()) {
                    scheduleDisconectingJob();
                } else {
                    stopDisconnectingJob();
                    if (state == State.DISCONNECTING) {
                        setState(State.REGISTERED);
                    }
                }
                listener.onSuccess(appInfo);
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
            }
        };
        ServiceSubscription<AppInfo> request = new ServiceSubscription<>(FOREGROUND_APP, null,
                jsonObj -> GSON.fromJson(jsonObj, AppInfo.class), interceptor);
        sendCommand(request);
        return request;
    }

    public ServiceCommand<AppInfo> getRunningApp(ResponseListener<AppInfo> listener) {
        ServiceCommand<AppInfo> request = new ServiceCommand<>(FOREGROUND_APP, null,
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

        ResponseListener<JsonObject> listener = new ResponseListener<>() {

            @Override
            public void onSuccess(@Nullable JsonObject jsonObj) {
                if (jsonObj != null) {
                    String socketPath = jsonObj.get("socketPath").getAsString();
                    if (protocol == Protocol.WEB_SOCKET) {
                        socketPath = socketPath
                                .replace(Protocol.WEB_SOCKET_SECURE.name + ":", Protocol.WEB_SOCKET.name + ":")
                                .replace(":" + Protocol.WEB_SOCKET_SECURE.port + "/",
                                        ":" + Protocol.WEB_SOCKET.port + "/");
                    }
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

    // Simulate Remote Control Button press

    public void sendRCButton(String rcButton, ResponseListener<CommandConfirmation> listener) {
        executeMouse(s -> s.button(rcButton));
    }

    public interface ConfigProvider {
        void storeKey(String key);

        void storeProperties(Map<String, String> properties);

        String getKey();
    }
}
