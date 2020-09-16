/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YioRemoteDockHandleStatus;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YioRemoteMessages;
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
    private YIOremoteDockWebsocket yioremoteDockwebSocketClient = new YIOremoteDockWebsocket();
    private ClientUpgradeRequest yioremoteDockwebSocketClientrequest = new ClientUpgradeRequest();
    private @Nullable URI websocketAddress;
    private YioRemoteDockHandleStatus yioremotedockactualstatus = YioRemoteDockHandleStatus.UNINITIALIZED_STATE;
    private @Nullable Future<?> authenticationjob;
    private @Nullable Future<?> websocketpollingjob;
    public String receivedmessage = "";
    private JsonObject recievedjson = new JsonObject();
    private boolean heartbeat = false;
    private boolean authenticationok = false;
    private String receivedstatus = "";
    private String lastsendircode = "";

    public YIOremoteDockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                websocketAddress = new URI("ws://" + localConfig.host + ":946");
                yioremotedockactualstatus = YioRemoteDockHandleStatus.AUTHENTICATION_PROCESS;
            } catch (URISyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Initialize web socket failed " + e.getMessage());
            }

            yioremoteDockwebSocketClient.addMessageHandler(new YIOremoteDockWebsocketInterface() {

                @Override
                public void onConnect(boolean connectedflag) {
                    if (connectedflag) {
                        yioremotedockactualstatus = YioRemoteDockHandleStatus.CONNECTION_ESTABLISHED;
                        authenticate();
                    } else {
                        yioremotedockactualstatus = YioRemoteDockHandleStatus.CONNECTION_FAILED;
                    }
                }

                @Override
                public void onMessage(String message) {
                    receivedmessage = message;
                    logger.debug("Message recieved {}", message);
                    recievedjson = convertStringtoJsonObject(receivedmessage);
                    if (recievedjson.size() > 0) {
                        if (decodereceivedMessage(recievedjson)) {
                            triggerChannel(getChannelUuid(GROUP_OUTPUT, STATUS_STRING_CHANNEL));
                            updateChannelString(GROUP_OUTPUT, STATUS_STRING_CHANNEL, receivedstatus);
                            logger.debug("Message {} decoded", receivedmessage);
                        } else {
                            logger.debug("Error during message {} decoding", receivedmessage);
                        }
                    }
                }

                @Override
                public void onError() {
                    if (authenticationjob != null) {
                        authenticationjob.cancel(true);
                    }
                    if (websocketpollingjob != null) {
                        websocketpollingjob.cancel(true);
                    }
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Connection lost no ping from YIO DOCK");
                    updateState(GROUP_OUTPUT, STATUS_STRING_CHANNEL, UnDefType.UNDEF);
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

    private boolean decodereceivedMessage(JsonObject message) {
        boolean success = false;

        if (message.has("type")) {
            if (message.get("type").toString().equalsIgnoreCase("\"auth_required\"")) {
                heartbeat = true;
                success = true;
                receivedstatus = "Authentication required";
            } else if (message.get("type").toString().equalsIgnoreCase("\"auth_ok\"")) {
                authenticationok = true;
                heartbeat = true;
                success = true;
                receivedstatus = "Authentication ok";
            } else if (message.get("type").toString().equalsIgnoreCase("\"dock\"") && message.has("message"))

            {
                if (message.get("message").toString().equalsIgnoreCase("\"ir_send\"")) {
                    if (message.get("success").toString().equalsIgnoreCase("true")) {
                        receivedstatus = "Send IR Code successfully";
                        heartbeat = true;
                        success = true;
                    } else {
                        if (lastsendircode.equalsIgnoreCase("\"0;0x0;0;0\"")) {
                            logger.debug("Send heartbeat Code success");
                        } else {
                            receivedstatus = "Send IR Code failure";
                        }
                        heartbeat = true;
                        success = true;
                    }
                } else {
                    logger.warn("No known message {}", receivedmessage);
                    heartbeat = false;
                    success = false;
                }
            } else if (message.get("command").toString().equalsIgnoreCase("\"ir_receive\"")) {
                receivedstatus = message.get("code").toString().replace("\"", "");
                if (receivedstatus.matches("[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                    receivedstatus = message.get("code").toString().replace("\"", "");
                } else {
                    receivedstatus = "";
                }
                logger.debug("ir_receive message {}", receivedstatus);
                heartbeat = true;
                success = true;
            } else {
                logger.warn("No known message {}", receivedmessage);
                heartbeat = false;
                success = false;
            }
        } else {
            logger.warn("No known message {}", receivedmessage);
            heartbeat = false;
            success = false;
        }
        return success;
    }

    private JsonObject convertStringtoJsonObject(String jsonString) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(jsonString);
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
            JsonObject result = new JsonObject();
            return result;
        }
    }

    public void updateState(String group, String channelId, State value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        updateState(id, value);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(YIOremoteDockActions.class);
    }

    @Override
    public void dispose() {
        if (authenticationjob != null) {
            authenticationjob.cancel(true);
        }
        if (websocketpollingjob != null) {
            websocketpollingjob.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RECEIVER_SWITCH_CHANNEL.equals(channelUID.getIdWithoutGroup())) {
            if (yioremotedockactualstatus.equals(YioRemoteDockHandleStatus.AUTHENTICATION_COMPLETE)) {
                if (command.toString().equals("ON")) {
                    logger.debug("YIODOCKRECEIVERSWITCH ON procedure: Switching IR Receiver on");
                    sendMessage(YioRemoteMessages.IR_RECEIVER_ON, "");
                } else if (command.toString().equals("OFF")) {
                    logger.debug("YIODOCKRECEIVERSWITCH OFF procedure: Switching IR Receiver off");
                    sendMessage(YioRemoteMessages.IR_RECEIVER_OFF, "");
                } else {
                    logger.debug("YIODOCKRECEIVERSWITCH no procedure");
                }
            }
        }
    }

    public void sendircode(@Nullable String ircode) {
        if (ircode != null) {
            if (ircode.matches("[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                sendMessage(YioRemoteMessages.IR_SEND, ircode);
            } else {
                logger.warn("Wrong ir code format {}", ircode);
            }
        }
    }

    private ChannelUID getChannelUuid(String group, String typeId) {
        return new ChannelUID(getThing().getUID(), group, typeId);
    }

    private void updateChannelString(String group, String channelId, String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        updateState(id, new StringType(value));
    }

    private void authenticate() {
        Runnable authenticationhread = new Runnable() {
            @Override
            public void run() {
                if (yioremotedockactualstatus.equals(YioRemoteDockHandleStatus.CONNECTION_ESTABLISHED)) {
                    sendMessage(YioRemoteMessages.AUTHENTICATE_MESSAGE, localConfig.accessToken);
                    yioremotedockactualstatus = YioRemoteDockHandleStatus.AUTHENTICATION_PROCESS;
                } else if (yioremotedockactualstatus.equals(YioRemoteDockHandleStatus.AUTHENTICATION_PROCESS)) {
                    if (getbooleanauthenticationok()) {
                        yioremotedockactualstatus = YioRemoteDockHandleStatus.AUTHENTICATION_COMPLETE;
                        updateStatus(ThingStatus.ONLINE);
                        startwebsocketpollingthread();
                        if (authenticationjob != null) {
                            authenticationjob.cancel(true);
                        }
                    } else {
                        yioremotedockactualstatus = YioRemoteDockHandleStatus.AUTHENTICATION_FAILED;
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Connection lost no ping from YIO DOCK");
                    updateState(GROUP_OUTPUT, STATUS_STRING_CHANNEL, UnDefType.UNDEF);
                }
            }
        };
        authenticationjob = scheduler.scheduleWithFixedDelay(authenticationhread, 0, 5, TimeUnit.SECONDS);
    }

    private void startwebsocketpollingthread() {
        Runnable websocketpollingthread = new Runnable() {
            @Override
            public void run() {
                if (yioremotedockactualstatus.equals(YioRemoteDockHandleStatus.AUTHENTICATION_COMPLETE)) {
                    if (getbooleanheartbeat()) {
                        logger.debug("heartbeat ok");
                        sendMessage(YioRemoteMessages.HEARTBEAT_MESSAGE, "");
                    } else {
                        yioremotedockactualstatus = YioRemoteDockHandleStatus.CONNECTION_FAILED;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Connection lost no ping from YIO DOCK");
                        updateState(GROUP_OUTPUT, STATUS_STRING_CHANNEL, UnDefType.UNDEF);
                        if (websocketpollingjob != null) {
                            websocketpollingjob.cancel(true);
                        }
                    }
                } else {
                    if (websocketpollingjob != null) {
                        websocketpollingjob.cancel(true);
                    }
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Connection lost no ping from YIO DOCK");
                    updateState(GROUP_OUTPUT, STATUS_STRING_CHANNEL, UnDefType.UNDEF);
                }
            }
        };
        websocketpollingjob = scheduler.scheduleWithFixedDelay(websocketpollingthread, 0, 30, TimeUnit.SECONDS);
    }

    public boolean getbooleanauthenticationok() {
        return authenticationok;
    }

    public boolean getbooleanheartbeat() {
        boolean result = heartbeat;
        heartbeat = false;
        return result;
    }

    public YioRemoteDockHandleStatus getyioremotedockactualstatus() {
        return yioremotedockactualstatus;
    }

    public void sendMessage(YioRemoteMessages messagetype, String messagepyload) {
        if (messagetype.equals(YioRemoteMessages.AUTHENTICATE_MESSAGE)) {
            yioremoteDockwebSocketClient.sendMessage("{\"type\":\"auth\", \"token\":\"" + messagepyload + "\"}");
            logger.debug("sending authenticating message: \"{\"type\":\"auth\", \"token\":\"{}\"}\"", messagepyload);
        } else if (messagetype.equals(YioRemoteMessages.HEARTBEAT_MESSAGE)) {
            lastsendircode = "\"0;0x0;0;0\"";
            yioremoteDockwebSocketClient.sendMessage(
                    "{\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"0;0x0;0;0\", \"format\":\"hex\"}");
            logger.debug(
                    "sending heartbeat message: {\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"0;0x0;0;0\", \"format\":\"hex\"}");
        } else if (messagetype.equals(YioRemoteMessages.IR_RECEIVER_ON)
                && getyioremotedockactualstatus().equals(YioRemoteDockHandleStatus.AUTHENTICATION_COMPLETE)) {
            yioremoteDockwebSocketClient.sendMessage("{\"type\":\"dock\", \"command\":\"ir_receive_on\"}");
            logger.debug("sending IR receiver on message: {\"type\":\"dock\", \"command\":\"ir_receive_on\"");
        } else if (messagetype.equals(YioRemoteMessages.IR_RECEIVER_OFF)
                && getyioremotedockactualstatus().equals(YioRemoteDockHandleStatus.AUTHENTICATION_COMPLETE)) {
            yioremoteDockwebSocketClient.sendMessage("{\"type\":\"dock\", \"command\":\"ir_receive_off\"}");
            logger.debug("sending IR receiver off message: {\"type\":\"dock\", \"command\":\"ir_receive_off\"");
        } else if (messagetype.equals(YioRemoteMessages.IR_SEND)
                && getyioremotedockactualstatus().equals(YioRemoteDockHandleStatus.AUTHENTICATION_COMPLETE)) {
            yioremoteDockwebSocketClient.sendMessage("{\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\""
                    + messagepyload + "\", \"format\":\"hex\"}");
            lastsendircode = messagepyload;
            logger.debug(
                    "sending IR message: {\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"{}\", \"format\":\"hex\"}",
                    messagepyload);
        }
    }
}
