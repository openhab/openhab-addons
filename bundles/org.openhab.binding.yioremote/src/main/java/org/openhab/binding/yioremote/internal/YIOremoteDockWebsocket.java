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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEDOCKHANDLESTATUS;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEMESSAGETYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private boolean booleannewmessagerecieved = false;
    private String stringlastsendircode = "";
    private @Nullable YIOremoteDockHandler yioremotedockhandler;
    private @Nullable YIOremoteDockWebsocketInterface yioremotedockwebsocketinterfacehandler;

    public YIOremoteDockWebsocket(YIOremoteDockHandler thing) {
        yioremotedockhandler = thing;
    }

    public void addMessageHandler(YIOremoteDockWebsocketInterface yioremotedockwebsocketinterfacehandler) {
        this.yioremotedockwebsocketinterfacehandler = yioremotedockwebsocketinterfacehandler;
    }

    @OnWebSocketMessage
    public void onText(Session session, String stringreceivedmessage) {
        yioremotedockwebsocketinterfacehandler.onMessage(stringreceivedmessage);
    }

    public String getstringreceivedmessage() {
        return this.stringreceivedmessage;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        yioremotedockwebsocketinterfacehandler.onConnect(true);
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        try {
            logger.debug("WebSocketError");
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
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRRECEIVERON) && yioremotedockhandler
                    .getyioremotedockactualstatus().equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_COMPLETE)) {
                session.getRemote().sendString("{\"type\":\"dock\", \"command\":\"ir_receive_on\"}");
                logger.debug("sending IR receiver on message: {\"type\":\"dock\", \"command\":\"ir_receive_on\"");
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRRECEIVEROFF) && yioremotedockhandler
                    .getyioremotedockactualstatus().equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_COMPLETE)) {
                session.getRemote().sendString("{\"type\":\"dock\", \"command\":\"ir_receive_off\"}");
                logger.debug("sending IR receiver off message: {\"type\":\"dock\", \"command\":\"ir_receive_off\"");
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRSEND) && yioremotedockhandler
                    .getyioremotedockactualstatus().equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATION_COMPLETE)) {
                session.getRemote().sendString("{\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"" + messagepyload
                        + "\", \"format\":\"hex\"}");
                stringlastsendircode = messagepyload;
                logger.debug(
                        "sending IR message: {\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"{}\", \"format\":\"hex\"}",
                        messagepyload);
            }
        } catch (IOException e) {
            logger.warn("Error during sendMessage function {}", e.getMessage());
        }
    }
}
