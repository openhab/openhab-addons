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
package org.openhab.binding.webthings.internal.websockets;

import static org.openhab.binding.webthings.internal.converters.WebThingToOpenhabConverter.*;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLHandshakeException;

import com.google.gson.Gson;

import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.websocket.api.CloseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.webthings.internal.dto.WebThingsPropertyCommand;
import org.openhab.binding.webthings.internal.handler.WebThingsWebThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebThingsConnectorSocketHandler} is responsible for handling the websocket with webthings
 *
 * @author schneider_sven - Initial contribution
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebThingsWebThingSocketHandler extends WebThingsSocketHandler{
    private final Logger logger = LoggerFactory.getLogger(WebThingsWebThingSocketHandler.class);

    private WebThingsWebThingHandler webThingHandler;

    public WebThingsWebThingSocketHandler(WebThingsWebThingHandler webThingHandler) {
        super();
        this.webThingHandler = webThingHandler;
    }

    @Override
    public void dispose() {
        webThingHandler.updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    @OnWebSocketMessage
    public void onMessage(Session session, String msg) {
        // https://stackoverflow.com/questions/32979715/websockets-onmessage-lock
        logger.info("Got msg: {}", msg);
        synchronized(onMessageLock){
            // Only process message if not identical to last, avoids loops
            if(!msg.equals(getLastMessage())){           
                Gson g = new Gson();

                // Ignore successful connect messages (do not contain commands)
                if(msg != null && !msg.contains("messageType\":\"connected")){
                    // Create object from msg
                    WebThingsPropertyCommand command = g.fromJson(msg, WebThingsPropertyCommand.class);

                    String lastMessageFormatted = "";
                    if(lastMessage != null){
                        lastMessageFormatted = lastMessage.substring(1, lastMessage.length());
                        if(lastMessage.contains("setProperty")){
                            lastMessageFormatted = lastMessageFormatted.replace("setProperty", "propertyStatus");
                        }
                    }
                     
                    if(command.getMessageType().equals("propertyStatus") && (lastMessage == null || !msg.replaceAll("\\s+","").contains(lastMessageFormatted))){
                        // Get thing UID
                        
                        for(String property: command.getData().keySet()){
                            ChannelUID channelUID = new ChannelUID(webThingHandler.getThing().getUID(), property);
                            Channel channel = webThingHandler.getThing().getChannel(channelUID);
                            
                            String itemType = channel.getAcceptedItemType();
                            String value = command.getData().get(property).toString();
                            if(itemType != null && value != null){
                                Command ohCommand = getCommandFromProperty(itemType, value);
                                if(!ohCommand.toString().equals("EmptyCommand")){
                                    logger.info("Updating openHAB item: {} value: {}", channelUID, ohCommand.toString());
                                    webThingHandler.postCommand(channelUID, ohCommand);
                                } else {
                                    logger.warn("Could not convert property {} to itemType: {} ", property, channel.getAcceptedItemType());
                                }
                            } else {
                                logger.warn("ItemType: {} or value: {} is null", itemType, value);
                            }
                        }
                    }

                // Set thing status to offline if websocket returns connected == false
                }else if(msg.contains("messageType\":\"connected")){
                    if(msg.contains("false")){
                        webThingHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "WebThing returned connected:false");
                    }else{
                        webThingHandler.updateStatus(ThingStatus.ONLINE);
                    }
                }
                setLastMessage(msg);
            }
        }
    }

    @Override
    @OnWebSocketError
    public void onError(Throwable cause) {
        boolean thingReachable = false;

        // Try to reconnect when error occuers
        if(cause instanceof CloseException || cause instanceof EofException || cause instanceof IOException){
            //https://stackoverflow.com/questions/44095346/reconnect-after-onwebsocketclose-jetty-9-4
            thingReachable = webThingHandler.reconnect(4);
        } else if(cause instanceof SSLHandshakeException){
            logger.error("SSL error: {}", cause.getMessage());
        }else if(cause instanceof SocketTimeoutException){
            logger.error("WebSocket Error: {}", cause.getMessage());
            webThingHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Websocket connection timed out");
        }else{
            logger.error("WebSocket Error: {}", cause.getMessage());
        }

        // Set thing to offline if reconnect not possible
        if(thingReachable){
            webThingHandler.updateStatus(ThingStatus.ONLINE);
        }else{
            webThingHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Lost websocket connection");
        }
    }
}
