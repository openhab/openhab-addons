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
import static org.openhab.binding.webthings.internal.utilities.WebThingsRestApiUtilities.*;

import javax.net.ssl.SSLHandshakeException;

import com.google.gson.Gson;

import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.websocket.api.CloseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.webthings.internal.dto.WebThingsPropertyCommand;
import org.openhab.binding.webthings.internal.handler.WebThingsConnectorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link WebThingsWebThingSocketHandler} is responsible for handling the websocket with webthings
 *
 * @author schneider_sven - Initial contribution
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebThingsConnectorSocketHandler extends WebThingsSocketHandler{
    private final Logger logger = LoggerFactory.getLogger(WebThingsConnectorSocketHandler.class);

    private WebThingsConnectorHandler connectorHandler;
    //private WebThingsConnectorHandler thingsHandler;

    public WebThingsConnectorSocketHandler(WebThingsConnectorHandler connectorHandler) {
        super();
        this.connectorHandler = connectorHandler;
    }

    public void dispose() {
        connectorHandler.updateStatus(ThingStatus.OFFLINE);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String msg) {
        // https://stackoverflow.com/questions/32979715/websockets-onmessage-lock
        logger.info("Got msg: {}", msg);
        synchronized(onMessageLock){
            // Only process message if not identical to last, avoids loops
            if(!msg.equals(getLastMessage())){           
                Gson g = new Gson();

                // Ignore successful connect messages (do not contain commands)
                if(msg != null && !msg.contains("messageType\":\"connected") && msg.contains("messageType\":\"propertyStatus")){
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
                        String thing = connectorHandler.getHandlerConfig().uid.replace(":", "_");
                        List<String> channels = new ArrayList<String>(command.getData().keySet());

                        for(String channel: channels){
                            String item = thing + "_" + channel;
                            try{
                                // API call to get itemType, possibly save itemType in property name for custom webthings
                                ItemDTO itemDTO = getOpenhabItem(item);
                                String itemType = itemDTO.type;

                                try{       
                                    // get string for API call based on command
                                    String value = command.getData().get(channel).toString();
                                    if(itemType != null && value != null){
                                        Command ohCommand = getCommandFromProperty(itemType, value);
                                        String  ohCommandAsString = ohCommand.toString();
                                        if(!ohCommandAsString.equals("EmptyCommand")){
                                            logger.info("Updating openHAB item: {} value: {}", item, ohCommandAsString);

                                            // Update openHAB item via API
                                            updateOpenhabItem(ohCommandAsString, item);
                                        }else{
                                            logger.warn("Could not find itemType: {} for thing: {}", channel, thing);
                                        }
                                    } else{
                                        logger.warn("ItemType: {} or value: {} is null", itemType, value);
                                    }
                                }catch(Exception e){
                                    logger.warn("Unhandled incoming command");
                                }
                            }catch(IOException e){
                                logger.error("Exception while updating OH item: {}", e.getMessage());
                            }
                        }
                    }

                // Set thing status to offline if websocket returns connected == false
                }else if(msg.contains("messageType\":\"connected")){
                    if(msg.contains("false")){
                        connectorHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "WebThing returned connected:false");
                    }else{
                        connectorHandler.updateStatus(ThingStatus.ONLINE);
                    }
                }
                setLastMessage(msg);
            }
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        boolean thingReachable = false;
        connectorHandler.updateStatus(ThingStatus.UNKNOWN);

        // Try to reconnect when error occuers
        if(cause instanceof CloseException || cause instanceof EofException || cause instanceof IOException){
            //https://stackoverflow.com/questions/44095346/reconnect-after-onwebsocketclose-jetty-9-4
            thingReachable = connectorHandler.reconnect(4);
        }else if(cause instanceof SSLHandshakeException){
            logger.error("SSL error: {}", cause.getMessage());
        }else if(cause instanceof SocketTimeoutException){
            logger.error("WebSocket Error: {}", cause.getMessage());
            connectorHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Websocket connection timed out");
        }else{
            logger.error("WebSocket Error: {}", cause.getMessage());
        }

        // Set thing to offline if reconnect not possible
        if(thingReachable){
            connectorHandler.updateStatus(ThingStatus.ONLINE);
        }else{
            connectorHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Lost websocket connection");
        }
    }
}
