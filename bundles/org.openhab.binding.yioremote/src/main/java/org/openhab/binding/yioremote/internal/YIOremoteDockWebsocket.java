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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEMESSAGETYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link YIOremoteDockWebsocket} is responsible for the Websocket Connection to the YIO Remote Dock
 *
 * @author Michael Loercher - Initial contribution
 */

@NonNullByDefault
@WebSocket
public class YIOremoteDockWebsocket {
    private @Nullable Session session;
    private String stringreceivedmessage = "";
    private final Logger logger = LoggerFactory.getLogger(YIOremoteDockWebsocket.class);
    private JsonObject jsonobjectrecievedJsonObject = new JsonObject();
    private boolean booleanauthenticationrequired = false;
    private boolean booleanheartbeat = false;
    private boolean booleanauthenticationok = false;
    private boolean booleansendirstatus = false;
    private String stringreceivedstatus = "";
    private String stringlastsendircode = "";
    private boolean booleannewmessagerecieved = false;

    CountDownLatch latch = new CountDownLatch(1);

    @OnWebSocketMessage
    public void onText(Session session, String message) throws IOException {
        stringreceivedmessage = message;
        jsonobjectrecievedJsonObject = convertStringtoJsonObject(stringreceivedmessage);
        if (decodereceivedMessage(jsonobjectrecievedJsonObject)) {
            logger.debug("Message {} decoded", stringreceivedmessage);
        } else {
            logger.debug("Error during message {} decoding", stringreceivedmessage);
        }
        booleannewmessagerecieved = true;
    }

    public String getstringreceivedmessage() {
        return this.stringreceivedmessage;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        latch.countDown();
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        try {
            booleanheartbeat = false;
        } catch (Exception ex) {
            logger.debug("WebSocketError");
        }
    }

    public void sendMessage(String str) {
        try {
            session.getRemote().sendString(str);
        } catch (IOException e) {
            logger.warn("Error during sendMessage function {}", e.getMessage());
        }
    }

    public void sendMessage(YIOREMOTEMESSAGETYPE messagetype, String messagepyload) {
        try {
            if (messagetype.equals(YIOREMOTEMESSAGETYPE.AUTHENTICATE)) {
                session.getRemote().sendString("{\"type\":\"auth\", \"token\":\"" + messagepyload + "\"}");
                logger.debug("sending authenticating message: \"{\"type\":\"auth\", \"token\":\"{}\"}\"",
                        messagepyload);
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.HEARTBEAT)) {
                stringlastsendircode = "\"0;0x0;0;0\"";
                session.getRemote().sendString(
                        "{\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"0;0x0;0;0\", \"format\":\"hex\"}");
                logger.debug(
                        "sending heartbeat message: {\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"0;0x0;0;0\", \"format\":\"hex\"}");
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRRECEIVERON)) {
                session.getRemote().sendString("{\"type\":\"dock\", \"command\":\"ir_receive_on\"}");
                logger.debug("sending heartbeat message: {\"type\":\"dock\", \"command\":\"ir_receive_on\"");
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRRECEIVEROFF)) {
                session.getRemote().sendString("{\"type\":\"dock\", \"command\":\"ir_receive_off\"}");
                logger.debug("sending heartbeat message: {\"type\":\"dock\", \"command\":\"ir_receive_off\"");
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRSEND)) {
                session.getRemote().sendString("{\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"" + messagepyload
                        + "\", \"format\":\"hex\"}");
                stringlastsendircode = messagepyload;
                logger.debug(
                        "sending heartbeat message: {\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"{}\", \"format\":\"hex\"}",
                        messagepyload);
            }
        } catch (IOException e) {
            logger.warn("Error during sendMessage function {}", e.getMessage());
        }
    }

    private boolean decodereceivedMessage(JsonObject JsonObject_recievedJsonObject) {
        boolean booleanresult = false;

        if (JsonObject_recievedJsonObject.has("type")) {
            if (JsonObject_recievedJsonObject.get("type").toString().equalsIgnoreCase("\"auth_required\"")) {
                booleanauthenticationrequired = true;
                booleanheartbeat = true;
                booleanresult = true;
            } else if (JsonObject_recievedJsonObject.get("type").toString().equalsIgnoreCase("\"auth_ok\"")) {
                booleanauthenticationrequired = false;
                booleanauthenticationok = true;
                booleanheartbeat = true;
                booleanresult = true;
            } else if (JsonObject_recievedJsonObject.get("type").toString().equalsIgnoreCase("\"dock\"")
                    && JsonObject_recievedJsonObject.has("message")) {
                if (JsonObject_recievedJsonObject.get("message").toString().equalsIgnoreCase("\"ir_send\"")) {
                    if (JsonObject_recievedJsonObject.get("success").toString().equalsIgnoreCase("true")) {
                        stringreceivedstatus = "Send IR Code successfully";
                        booleansendirstatus = true;
                        booleanheartbeat = true;
                        booleanresult = true;
                    } else {
                        if (stringlastsendircode.equalsIgnoreCase("\"0;0x0;0;0\"")) {
                            logger.debug("Send heartbeat Code success");
                        } else {
                            stringreceivedstatus = "Send IR Code failure";
                        }
                        booleansendirstatus = true;
                        booleanheartbeat = true;
                        booleanresult = true;
                    }
                } else {
                    logger.warn("No known message {}", stringreceivedmessage);
                    booleanheartbeat = false;
                    booleanresult = false;
                }
            } else if (JsonObject_recievedJsonObject.get("command").toString().equalsIgnoreCase("\"ir_receive\"")) {
                stringreceivedstatus = JsonObject_recievedJsonObject.get("code").toString().replace("\"", "");
                if (stringreceivedstatus.matches("[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                    stringreceivedstatus = JsonObject_recievedJsonObject.get("code").toString().replace("\"", "");
                } else {
                    stringreceivedstatus = "";
                }
                logger.debug("ir_receive message {}", stringreceivedstatus);
                booleanheartbeat = true;
                booleanresult = true;
            } else {
                logger.warn("No known message {}", stringreceivedmessage);
                booleanheartbeat = false;
                booleanresult = false;
            }
        } else {
            logger.warn("No known message {}", stringreceivedmessage);
            booleanheartbeat = false;
            booleanresult = false;
        }
        return booleanresult;
    }

    private JsonObject convertStringtoJsonObject(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(jsonString);

        JsonObject result = null;
        if (jsonElement instanceof JsonObject) {
            result = jsonElement.getAsJsonObject();
        } else {
            logger.debug("{} is not valid JSON stirng", jsonString);
            throw new IllegalArgumentException(jsonString + "{} is not valid JSON stirng");
        }
        return result;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public boolean getbooleanheartbeat() {
        boolean booleanresult = false;
        booleanresult = booleanheartbeat;
        booleanheartbeat = false;
        return booleanresult;
    }

    public boolean getbooleanauthenticationrequired() {
        boolean booleanresult = false;
        booleanresult = booleanauthenticationrequired;
        booleanauthenticationrequired = false;
        return booleanresult;
    }

    public String getstringreceivedstatus() {
        String stringresult = "";
        stringresult = stringreceivedstatus;
        stringreceivedstatus = "";
        return stringresult;
    }

    public boolean getbooleanauthenticationok() {
        boolean booleanresult = false;
        booleanresult = booleanauthenticationok;
        booleanauthenticationok = false;
        return booleanresult;
    }

    public boolean getbooleansendirstatus() {
        boolean booleanresult = false;
        booleanresult = booleansendirstatus;
        booleansendirstatus = false;
        return booleanresult;
    }

    public boolean getbooleannewmessagerecieved() {
        boolean booleanresult = false;
        booleanresult = booleannewmessagerecieved;
        booleannewmessagerecieved = false;
        return booleanresult;
    }
}
