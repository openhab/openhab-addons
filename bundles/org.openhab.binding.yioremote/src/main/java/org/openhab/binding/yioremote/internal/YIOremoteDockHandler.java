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
package org.openhab.binding.yioremote.internal;

import static org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YioRemoteDockHandleStatus;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YioRemoteMessages;
import org.openhab.binding.yioremote.internal.dto.AuthenticationMessage;
import org.openhab.binding.yioremote.internal.dto.IRCode;
import org.openhab.binding.yioremote.internal.dto.IRCodeSendMessage;
import org.openhab.binding.yioremote.internal.dto.IRReceiverMessage;
import org.openhab.binding.yioremote.internal.dto.PingMessage;
import org.openhab.binding.yioremote.internal.utils.Websocket;
import org.openhab.binding.yioremote.internal.utils.WebsocketInterface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link YIOremoteDockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class YIOremoteDockHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(YIOremoteDockHandler.class);

    YIOremoteConfiguration localConfig = getConfigAs(YIOremoteConfiguration.class);
    private WebSocketClient webSocketClient = new WebSocketClient();
    private Websocket yioremoteDockwebSocketClient = new Websocket();
    private ClientUpgradeRequest yioremoteDockwebSocketClientrequest = new ClientUpgradeRequest();
    private @Nullable URI websocketAddress;
    private YioRemoteDockHandleStatus yioRemoteDockActualStatus = YioRemoteDockHandleStatus.UNINITIALIZED_STATE;
    private @Nullable Future<?> initJob;
    private @Nullable Future<?> webSocketPollingJob;
    private @Nullable Future<?> webSocketReconnectionPollingJob;
    public String receivedMessage = "";
    private JsonObject recievedJson = new JsonObject();
    private boolean heartBeat = false;
    private boolean authenticationOk = false;
    private String receivedStatus = "";
    private IRCode irCodeReceivedHandler = new IRCode();
    private IRCode irCodeSendHandler = new IRCode();
    private IRCodeSendMessage irCodeSendMessageHandler = new IRCodeSendMessage(irCodeSendHandler);
    private AuthenticationMessage authenticationMessageHandler = new AuthenticationMessage();
    private IRReceiverMessage irReceiverMessageHandler = new IRReceiverMessage();
    private PingMessage pingMessageHandler = new PingMessage();
    private int reconnectionCounter = 0;

    public YIOremoteDockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        initJob = scheduler.submit(() -> {
            try {
                websocketAddress = new URI("ws://" + localConfig.host + ":946");
                yioRemoteDockActualStatus = YioRemoteDockHandleStatus.AUTHENTICATION_PROCESS;
            } catch (URISyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Initialize web socket failed: " + e.getMessage());
            }

            yioremoteDockwebSocketClient.addMessageHandler(new WebsocketInterface() {

                @Override
                public void onConnect(boolean connected) {
                    if (connected) {
                        yioRemoteDockActualStatus = YioRemoteDockHandleStatus.CONNECTION_ESTABLISHED;
                    } else {
                        yioRemoteDockActualStatus = YioRemoteDockHandleStatus.CONNECTION_FAILED;
                    }
                }

                @Override
                public void onMessage(String message) {
                    receivedMessage = message;
                    logger.debug("Message recieved {}", message);
                    recievedJson = convertStringToJsonObject(receivedMessage);
                    if (recievedJson.size() > 0) {
                        if (decodeReceivedMessage(recievedJson)) {
                            switch (yioRemoteDockActualStatus) {
                                case CONNECTION_ESTABLISHED:
                                case AUTHENTICATION_PROCESS:
                                    authenticateWebsocket();
                                    break;
                                case COMMUNICATION_ERROR:
                                    disposeWebsocketPollingJob();
                                    reconnectWebsocket();
                                    break;
                                case AUTHENTICATION_COMPLETE:
                                case CHECK_PONG:
                                case SEND_PING:
                                    updateChannelString(GROUP_OUTPUT, STATUS_STRING_CHANNEL, receivedStatus);
                                    triggerChannel(getChannelUuid(GROUP_OUTPUT, STATUS_STRING_CHANNEL));
                                    break;
                                default:
                                    break;
                            }
                            logger.debug("Message {} decoded", receivedMessage);
                        } else {
                            logger.debug("Error during message {} decoding", receivedMessage);
                        }
                    }
                }

                @Override
                public void onClose() {
                    logger.debug("onClose");
                    disposeWebsocketPollingJob();
                    reconnectWebsocket();
                }

                @Override
                public void onError(Throwable cause) {
                    logger.debug("onError");
                    disposeWebsocketPollingJob();
                    yioRemoteDockActualStatus = YioRemoteDockHandleStatus.COMMUNICATION_ERROR;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Communication lost no ping from YIO DOCK");
                    reconnectWebsocket();
                }
            });

            try {
                webSocketClient.start();

                webSocketClient.connect(yioremoteDockwebSocketClient, websocketAddress,
                        yioremoteDockwebSocketClientrequest);
            } catch (Exception e) {
                logger.debug("Connection error {}", e.getMessage());
            }

        });
    }

    private boolean decodeReceivedMessage(JsonObject message) {
        boolean success = false;

        if (message.has("type")) {
            if ("\"auth_required\"".equalsIgnoreCase(message.get("type").toString())) {
                success = true;
                receivedStatus = "Authentication required";
            } else if ("\"auth_ok\"".equalsIgnoreCase(message.get("type").toString())) {
                authenticationOk = true;
                success = true;
                receivedStatus = "Authentication ok";
            } else if ("\"dock\"".equalsIgnoreCase(message.get("type").toString()) && message.has("message")) {
                if ("\"pong\"".equalsIgnoreCase(message.get("message").toString())) {
                    heartBeat = true;
                    success = true;
                    receivedStatus = "Heart beat received";
                } else if ("\"ir_send\"".equalsIgnoreCase(message.get("message").toString())) {
                    if ("true".equalsIgnoreCase(message.get("success").toString())) {
                        receivedStatus = "Send IR Code successfully";
                        success = true;
                    } else {
                        receivedStatus = "Send IR Code failure";
                        heartBeat = true;
                        success = true;
                    }
                } else {
                    logger.warn("No known message {}", receivedMessage);
                    heartBeat = false;
                    success = false;
                }
            } else if ("\"ir_receive\"".equalsIgnoreCase(message.get("command").toString())) {
                receivedStatus = message.get("code").toString().replace("\"", "");
                if (receivedStatus.matches("[0-9]?[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                    irCodeReceivedHandler.setCode(message.get("code").toString().replace("\"", ""));
                } else {
                    irCodeReceivedHandler.setCode("");
                }

                logger.debug("ir_receive message {}", irCodeReceivedHandler.getCode());
                heartBeat = true;
                success = true;
            } else {
                logger.warn("No known message {}", irCodeReceivedHandler.getCode());
                heartBeat = false;
                success = false;
            }
        } else {
            logger.warn("No known message {}", irCodeReceivedHandler.getCode());
            heartBeat = false;
            success = false;
        }
        return success;
    }

    private JsonObject convertStringToJsonObject(String jsonString) {
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            JsonObject result;

            if (jsonElement instanceof JsonObject) {
                result = jsonElement.getAsJsonObject();
            } else {
                logger.debug("{} is not valid JSON stirng", jsonString);
                result = new JsonObject();
                throw new IllegalArgumentException(jsonString + "{} is not valid JSON stirng");
            }
            return result;
        } catch (IllegalArgumentException e) {
            return new JsonObject();
        }
    }

    public void updateState(String group, String channelId, State value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        updateState(id, value);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(YIOremoteDockActions.class);
    }

    @Override
    public void dispose() {
        Future<?> job = initJob;
        if (job != null) {
            job.cancel(true);
            initJob = null;
        }
        disposeWebsocketPollingJob();
        disposeWebSocketReconnectionPollingJob();
        try {
            webSocketClient.stop();
        } catch (Exception e) {
            logger.debug("Could not stop webSocketClient,  message {}", e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RECEIVER_SWITCH_CHANNEL.equals(channelUID.getIdWithoutGroup())) {
            switch (yioRemoteDockActualStatus) {
                case AUTHENTICATION_COMPLETE:
                case SEND_PING:
                case CHECK_PONG:
                    if (command == OnOffType.ON) {
                        logger.debug("YIODOCKRECEIVERSWITCH ON procedure: Switching IR Receiver on");
                        sendMessage(YioRemoteMessages.IR_RECEIVER_ON, "");
                    } else if (command == OnOffType.OFF) {
                        logger.debug("YIODOCKRECEIVERSWITCH OFF procedure: Switching IR Receiver off");
                        sendMessage(YioRemoteMessages.IR_RECEIVER_OFF, "");
                    } else {
                        logger.debug("YIODOCKRECEIVERSWITCH no procedure");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void sendIRCode(@Nullable String irCode) {
        if (irCode != null) {
            if (yioRemoteDockActualStatus.equals(YioRemoteDockHandleStatus.AUTHENTICATION_COMPLETE)
                    || yioRemoteDockActualStatus.equals(YioRemoteDockHandleStatus.SEND_PING)
                    || yioRemoteDockActualStatus.equals(YioRemoteDockHandleStatus.CHECK_PONG)) {
                if (irCode.matches("[0-9]?[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                    sendMessage(YioRemoteMessages.IR_SEND, irCode);
                } else {
                    logger.warn("Wrong ir code format {}", irCode);
                }
            } else {
                logger.debug("Wrong Dock Statusfor sending  {}", irCode);
            }
        } else {
            logger.warn("No ir code {}", irCode);
        }
    }

    private ChannelUID getChannelUuid(String group, String typeId) {
        return new ChannelUID(getThing().getUID(), group, typeId);
    }

    private void updateChannelString(String group, String channelId, String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        updateState(id, new StringType(value));
    }

    private void authenticateWebsocket() {
        switch (yioRemoteDockActualStatus) {
            case CONNECTION_ESTABLISHED:
                authenticationMessageHandler.setToken(localConfig.accessToken);
                sendMessage(YioRemoteMessages.AUTHENTICATE_MESSAGE, localConfig.accessToken);
                yioRemoteDockActualStatus = YioRemoteDockHandleStatus.AUTHENTICATION_PROCESS;
                break;
            case AUTHENTICATION_PROCESS:
                if (authenticationOk) {
                    yioRemoteDockActualStatus = YioRemoteDockHandleStatus.AUTHENTICATION_COMPLETE;
                    disposeWebSocketReconnectionPollingJob();
                    reconnectionCounter = 0;
                    updateStatus(ThingStatus.ONLINE);
                    updateState(STATUS_STRING_CHANNEL, StringType.EMPTY);
                    updateState(RECEIVER_SWITCH_CHANNEL, OnOffType.OFF);
                    webSocketPollingJob = scheduler.scheduleWithFixedDelay(this::pollingWebsocketJob, 0, 40,
                            TimeUnit.SECONDS);
                } else {
                    yioRemoteDockActualStatus = YioRemoteDockHandleStatus.AUTHENTICATION_FAILED;
                }
                break;
            default:
                disposeWebsocketPollingJob();
                yioRemoteDockActualStatus = YioRemoteDockHandleStatus.COMMUNICATION_ERROR;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Connection lost no ping from YIO DOCK");
                break;
        }
    }

    private void disposeWebsocketPollingJob() {
        Future<?> job = webSocketPollingJob;
        if (job != null) {
            job.cancel(true);
            webSocketPollingJob = null;
        }
    }

    private void disposeWebSocketReconnectionPollingJob() {
        Future<?> job = webSocketReconnectionPollingJob;
        if (job != null) {
            job.cancel(true);
            webSocketReconnectionPollingJob = null;
        }
        logger.debug("disposereconnection");
        reconnectionCounter = 0;
    }

    private void pollingWebsocketJob() {
        switch (yioRemoteDockActualStatus) {
            case AUTHENTICATION_COMPLETE:
                resetHeartbeat();
                sendMessage(YioRemoteMessages.HEARTBEAT_MESSAGE, "");
                yioRemoteDockActualStatus = YioRemoteDockHandleStatus.CHECK_PONG;
                break;
            case SEND_PING:
                resetHeartbeat();
                sendMessage(YioRemoteMessages.HEARTBEAT_MESSAGE, "");
                yioRemoteDockActualStatus = YioRemoteDockHandleStatus.CHECK_PONG;
                break;
            case CHECK_PONG:
                if (getHeartbeat()) {
                    updateChannelString(GROUP_OUTPUT, STATUS_STRING_CHANNEL, receivedStatus);
                    yioRemoteDockActualStatus = YioRemoteDockHandleStatus.SEND_PING;
                    logger.debug("heartBeat ok");
                } else {
                    yioRemoteDockActualStatus = YioRemoteDockHandleStatus.COMMUNICATION_ERROR;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Connection lost no ping from YIO DOCK");
                    disposeWebsocketPollingJob();
                    reconnectWebsocket();
                }
                break;
            default:
                disposeWebsocketPollingJob();
                yioRemoteDockActualStatus = YioRemoteDockHandleStatus.COMMUNICATION_ERROR;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Connection lost no ping from YIO DOCK");
                break;
        }
    }

    public boolean resetHeartbeat() {
        heartBeat = false;
        return true;
    }

    public boolean getHeartbeat() {
        return heartBeat;
    }

    public void reconnectWebsocket() {
        yioRemoteDockActualStatus = YioRemoteDockHandleStatus.COMMUNICATION_ERROR;
        if (webSocketReconnectionPollingJob == null) {
            webSocketReconnectionPollingJob = scheduler.scheduleWithFixedDelay(this::reconnectWebsocketJob, 0, 30,
                    TimeUnit.SECONDS);
        } else if (reconnectionCounter == 5) {
            reconnectionCounter = 0;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection lost no ping from YIO DOCK");
            if (webSocketReconnectionPollingJob == null) {
                webSocketReconnectionPollingJob = scheduler.scheduleWithFixedDelay(this::reconnectWebsocketJob, 0, 1,
                        TimeUnit.MINUTES);
            } else {
                disposeWebSocketReconnectionPollingJob();
                if (webSocketReconnectionPollingJob == null) {
                    webSocketReconnectionPollingJob = scheduler.scheduleWithFixedDelay(this::reconnectWebsocketJob, 0,
                            5, TimeUnit.MINUTES);
                }
            }
        } else {
        }
    }

    public void reconnectWebsocketJob() {
        reconnectionCounter++;
        switch (yioRemoteDockActualStatus) {
            case COMMUNICATION_ERROR:
                logger.debug("Reconnecting YIORemoteHandler");
                try {
                    disposeWebsocketPollingJob();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Connection lost no ping from YIO DOCK");
                    yioremoteDockwebSocketClient.closeWebsocketSession();
                    webSocketClient.stop();
                    yioRemoteDockActualStatus = YioRemoteDockHandleStatus.RECONNECTION_PROCESS;
                } catch (Exception e) {
                    logger.debug("Connection error {}", e.getMessage());
                }
                try {
                    websocketAddress = new URI("ws://" + localConfig.host + ":946");
                    yioRemoteDockActualStatus = YioRemoteDockHandleStatus.AUTHENTICATION_PROCESS;
                } catch (URISyntaxException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Initialize web socket failed: " + e.getMessage());
                }
                try {
                    webSocketClient.start();
                    webSocketClient.connect(yioremoteDockwebSocketClient, websocketAddress,
                            yioremoteDockwebSocketClientrequest);
                } catch (Exception e) {
                    logger.debug("Connection error {}", e.getMessage());
                }
                break;
            case AUTHENTICATION_COMPLETE:
                disposeWebSocketReconnectionPollingJob();
                reconnectionCounter = 0;
                break;
            default:
                break;
        }
    }

    public void sendMessage(YioRemoteMessages messageType, String messagePayload) {
        switch (messageType) {
            case AUTHENTICATE_MESSAGE:
                yioremoteDockwebSocketClient.sendMessage(authenticationMessageHandler.getAuthenticationMessageString());
                logger.debug("sending authenticating {}",
                        authenticationMessageHandler.getAuthenticationMessageString());
                break;
            case HEARTBEAT_MESSAGE:
                yioremoteDockwebSocketClient.sendMessage(pingMessageHandler.getPingMessageString());
                logger.debug("sending ping {}", pingMessageHandler.getPingMessageString());
                break;
            case IR_RECEIVER_ON:
                irReceiverMessageHandler.setOn();
                yioremoteDockwebSocketClient.sendMessage(irReceiverMessageHandler.getIRreceiverMessageString());
                logger.debug("sending IR receiver on message: {}",
                        irReceiverMessageHandler.getIRreceiverMessageString());
                break;
            case IR_RECEIVER_OFF:
                irReceiverMessageHandler.setOff();
                yioremoteDockwebSocketClient.sendMessage(irReceiverMessageHandler.getIRreceiverMessageString());
                logger.debug("sending IR receiver on message: {}",
                        irReceiverMessageHandler.getIRreceiverMessageString());
                break;
            case IR_SEND:
                irCodeSendHandler.setCode(messagePayload);
                yioremoteDockwebSocketClient.sendMessage(irCodeSendMessageHandler.getIRcodeSendMessageString());
                logger.debug("sending IR code: {}", irCodeSendMessageHandler.getIRcodeSendMessageString());
                break;
        }
    }
}
