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
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEDOCKHANDLESTATUS;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEMESSAGETYPE;
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
    private YIOremoteDockHandler yioremotedockhandler = this;
    private WebSocketClient yioremoteDockHandlerwebSocketClient = new WebSocketClient();
    private YIOremoteDockWebsocket yioremoteDockwebSocketClient = new YIOremoteDockWebsocket();
    private ClientUpgradeRequest yioremoteDockwebSocketClientrequest = new ClientUpgradeRequest();
    private @Nullable URI uriyiodockwebsocketaddress;
    private YIOREMOTEDOCKHANDLESTATUS yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.UNINITIALIZED_STATE;
    private @Nullable Future<?> authenticationjob;
    private @Nullable Future<?> websocketpollingjob;
    public String stringreceivedmessage = "";
    private JsonObject jsonobjectrecievedJsonObject = new JsonObject();
    private boolean booleanheartbeat = false;
    private boolean booleanauthenticationok = false;
    private String stringreceivedstatus = "";
    private String stringlastsendircode = "";

    public YIOremoteDockHandler(Thing thing) {
        super(thing);
        yioremotedockhandler = this;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                uriyiodockwebsocketaddress = new URI("ws://" + localConfig.host + ":946");
                yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_PROCESS;
            } catch (URISyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Initialize web socket failed");
            }

            yioremoteDockwebSocketClient.addMessageHandler(new YIOremoteDockWebsocketInterface() {

                @Override
                public void onConnect(Boolean booleanconnectedflag) {
                    if (booleanconnectedflag) {
                        yioremotedockhandler.yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.CONNECTION_ESTABLISHED;
                        yioremotedockhandler.authenticate(booleanconnectedflag);
                    } else {
                        yioremotedockhandler.yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.CONNECTION_FAILED;
                    }
                }

                @Override
                public void onMessage(String message) {
                    yioremotedockhandler.stringreceivedmessage = message;
                    logger.debug("Message recieved {}", message);
                    jsonobjectrecievedJsonObject = convertStringtoJsonObject(
                            yioremotedockhandler.stringreceivedmessage);
                    if (jsonobjectrecievedJsonObject.size() > 0) {
                        if (yioremotedockhandler.decodereceivedMessage(jsonobjectrecievedJsonObject)) {
                            yioremotedockhandler.triggerChannel(getChannelUuid(GROUP_OUTPUT, YIODOCKSTATUS));
                            yioremotedockhandler.updateChannelString(GROUP_OUTPUT, YIODOCKSTATUS,
                                    yioremotedockhandler.stringreceivedstatus);
                            logger.debug("Message {} decoded", yioremotedockhandler.stringreceivedmessage);
                        } else {
                            logger.debug("Error during message {} decoding",
                                    yioremotedockhandler.stringreceivedmessage);
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
                    updateState(GROUP_OUTPUT, YIODOCKSTATUS, UnDefType.UNDEF);
                }
            });

            try {
                yioremoteDockHandlerwebSocketClient.start();

                yioremoteDockHandlerwebSocketClient.connect(yioremoteDockwebSocketClient, uriyiodockwebsocketaddress,
                        yioremoteDockwebSocketClientrequest);
            } catch (Exception e) {
                logger.debug("Connection error {}", e.getMessage());
            }

        });
    }

    private boolean decodereceivedMessage(JsonObject JsonObject_recievedJsonObject) {
        boolean booleanresult = false;

        if (JsonObject_recievedJsonObject.has("type")) {
            if (JsonObject_recievedJsonObject.get("type").toString().equalsIgnoreCase("\"auth_required\"")) {
                yioremotedockhandler.booleanheartbeat = true;
                booleanresult = true;
                yioremotedockhandler.stringreceivedstatus = "Authentication required";
            } else if (JsonObject_recievedJsonObject.get("type").toString().equalsIgnoreCase("\"auth_ok\"")) {
                yioremotedockhandler.booleanauthenticationok = true;
                yioremotedockhandler.booleanheartbeat = true;
                booleanresult = true;
                yioremotedockhandler.stringreceivedstatus = "Authentication ok";
            } else if (JsonObject_recievedJsonObject.get("type").toString().equalsIgnoreCase("\"dock\"")
                    && JsonObject_recievedJsonObject.has("message"))

            {
                if (JsonObject_recievedJsonObject.get("message").toString().equalsIgnoreCase("\"ir_send\"")) {
                    if (JsonObject_recievedJsonObject.get("success").toString().equalsIgnoreCase("true")) {
                        yioremotedockhandler.stringreceivedstatus = "Send IR Code successfully";
                        yioremotedockhandler.booleanheartbeat = true;
                        booleanresult = true;
                    } else {
                        if (stringlastsendircode.equalsIgnoreCase("\"0;0x0;0;0\"")) {
                            logger.debug("Send heartbeat Code success");
                        } else {
                            stringreceivedstatus = "Send IR Code failure";
                        }
                        yioremotedockhandler.booleanheartbeat = true;
                        booleanresult = true;
                    }
                } else {
                    logger.warn("No known message {}", stringreceivedmessage);
                    yioremotedockhandler.booleanheartbeat = false;
                    booleanresult = false;
                }
            } else if (JsonObject_recievedJsonObject.get("command").toString().equalsIgnoreCase("\"ir_receive\"")) {
                yioremotedockhandler.stringreceivedstatus = JsonObject_recievedJsonObject.get("code").toString()
                        .replace("\"", "");
                if (stringreceivedstatus.matches("[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                    yioremotedockhandler.stringreceivedstatus = JsonObject_recievedJsonObject.get("code").toString()
                            .replace("\"", "");
                } else {
                    yioremotedockhandler.stringreceivedstatus = "";
                }
                logger.debug("ir_receive message {}", stringreceivedstatus);
                yioremotedockhandler.booleanheartbeat = true;
                booleanresult = true;
            } else {
                logger.warn("No known message {}", stringreceivedmessage);
                yioremotedockhandler.booleanheartbeat = false;
                booleanresult = false;
            }
        } else

        {
            logger.warn("No known message {}", stringreceivedmessage);
            yioremotedockhandler.booleanheartbeat = false;
            booleanresult = false;
        }
        return booleanresult;
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

    protected void updateState(String group, String channelId, State value) {
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
        if (YIODOCKRECEIVERSWITCH.equals(channelUID.getIdWithoutGroup())) {
            if (yioremotedockactualstatus.equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_COMPLETE)) {
                if (command.toString().equals("ON")) {
                    logger.debug("YIODOCKRECEIVERSWITCH ON procedure: Switching IR Receiver on");
                    yioremotedockhandler.sendMessage(YIOREMOTEMESSAGETYPE.IRRECEIVERON, "");
                } else if (command.toString().equals("OFF")) {
                    logger.debug("YIODOCKRECEIVERSWITCH OFF procedure: Switching IR Receiver off");
                    yioremotedockhandler.sendMessage(YIOREMOTEMESSAGETYPE.IRRECEIVEROFF, "");
                } else {
                    logger.debug("YIODOCKRECEIVERSWITCH no procedure");
                }
            }
        }
    }

    public void sendircode(@Nullable String stringIRCode) {
        if (stringIRCode != null) {
            if (stringIRCode.matches("[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                yioremotedockhandler.sendMessage(YIOREMOTEMESSAGETYPE.IRSEND, stringIRCode);
            } else {
                logger.warn("Wrong ir code format {}", stringIRCode);
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

    private void authenticate(boolean booleanconnectedflag) {
        Runnable authenticationhread = new Runnable() {
            @Override
            public void run() {
                if (yioremotedockactualstatus.equals(YIOREMOTEDOCKHANDLESTATUS.CONNECTION_ESTABLISHED)) {
                    yioremotedockhandler.sendMessage(YIOREMOTEMESSAGETYPE.AUTHENTICATE,
                            yioremotedockhandler.localConfig.accesstoken);
                    yioremotedockhandler.yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_PROCESS;
                } else if (yioremotedockactualstatus.equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_PROCESS)) {
                    if (yioremotedockhandler.getbooleanauthenticationok()) {
                        yioremotedockhandler.yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_COMPLETE;
                        updateStatus(ThingStatus.ONLINE);
                        yioremotedockhandler.startwebsocketpollingthread();
                        if (authenticationjob != null) {
                            authenticationjob.cancel(true);
                        }
                    } else {
                        yioremotedockhandler.yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_FAILED;
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Connection lost no ping from YIO DOCK");
                    updateState(GROUP_OUTPUT, YIODOCKSTATUS, UnDefType.UNDEF);
                }
            }
        };
        authenticationjob = scheduler.scheduleWithFixedDelay(authenticationhread, 0, 5, TimeUnit.SECONDS);
    }

    private void startwebsocketpollingthread() {
        Runnable websocketpollingthread = new Runnable() {
            @Override
            public void run() {
                if (yioremotedockactualstatus.equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_COMPLETE)) {
                    if (yioremotedockhandler.getbooleanheartbeat()) {
                        logger.debug("heartbeat ok");
                        yioremotedockhandler.sendMessage(YIOREMOTEMESSAGETYPE.HEARTBEAT, "");
                    } else {
                        yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.CONNECTION_FAILED;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Connection lost no ping from YIO DOCK");
                        updateState(GROUP_OUTPUT, YIODOCKSTATUS, UnDefType.UNDEF);
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
                    updateState(GROUP_OUTPUT, YIODOCKSTATUS, UnDefType.UNDEF);
                }
            }
        };
        websocketpollingjob = scheduler.scheduleWithFixedDelay(websocketpollingthread, 0, 30, TimeUnit.SECONDS);
    }

    protected boolean getbooleanauthenticationok() {
        return yioremotedockhandler.booleanauthenticationok;
    }

    protected boolean getbooleanheartbeat() {
        boolean booleanresult = yioremotedockhandler.booleanheartbeat;
        yioremotedockhandler.booleanheartbeat = false;
        return booleanresult;
    }

    public YIOREMOTEDOCKHANDLESTATUS getyioremotedockactualstatus() {
        return yioremotedockhandler.yioremotedockactualstatus;
    }

    public void sendMessage(YIOREMOTEMESSAGETYPE messagetype, String messagepyload) {
        if (messagetype.equals(YIOREMOTEMESSAGETYPE.AUTHENTICATE)) {
            yioremoteDockwebSocketClient.sendMessage("{\"type\":\"auth\", \"token\":\"" + messagepyload + "\"}");
            logger.debug("sending authenticating message: \"{\"type\":\"auth\", \"token\":\"{}\"}\"", messagepyload);
        } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.HEARTBEAT)) {
            stringlastsendircode = "\"0;0x0;0;0\"";
            yioremoteDockwebSocketClient.sendMessage(
                    "{\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"0;0x0;0;0\", \"format\":\"hex\"}");
            logger.debug(
                    "sending heartbeat message: {\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"0;0x0;0;0\", \"format\":\"hex\"}");
        } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRRECEIVERON) && yioremotedockhandler
                .getyioremotedockactualstatus().equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_COMPLETE)) {
            yioremoteDockwebSocketClient.sendMessage("{\"type\":\"dock\", \"command\":\"ir_receive_on\"}");
            logger.debug("sending IR receiver on message: {\"type\":\"dock\", \"command\":\"ir_receive_on\"");
        } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRRECEIVEROFF) && yioremotedockhandler
                .getyioremotedockactualstatus().equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_COMPLETE)) {
            yioremoteDockwebSocketClient.sendMessage("{\"type\":\"dock\", \"command\":\"ir_receive_off\"}");
            logger.debug("sending IR receiver off message: {\"type\":\"dock\", \"command\":\"ir_receive_off\"");
        } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRSEND) && yioremotedockhandler
                .getyioremotedockactualstatus().equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_COMPLETE)) {
            yioremoteDockwebSocketClient.sendMessage("{\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\""
                    + messagepyload + "\", \"format\":\"hex\"}");
            stringlastsendircode = messagepyload;
            logger.debug(
                    "sending IR message: {\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"{}\", \"format\":\"hex\"}",
                    messagepyload);
        }
    }
}
