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
package org.openhab.binding.webthings.internal.handler;

import static org.openhab.binding.webthings.internal.handler.WebThingsHandler.*;
import static org.openhab.binding.webthings.internal.utilities.WebThingsRestApiHandler.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import com.google.gson.Gson;

import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.websocket.api.CloseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.webthings.internal.json.WebThingsPropertyCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link WebThingsSocketHandler} is responsible for handling the websocket with webthings
 *
 * @author schneider_sven - Initial contribution
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebThingsSocketHandler{
    private final Logger logger = LoggerFactory.getLogger(WebThingsSocketHandler.class);
    private final CountDownLatch closeLatch;
    private final Object onMessageLock = new Object();
    
    @SuppressWarnings("unused")
    private Session session;
    private String lastMessage;
    private WebThingsConnectorHandler connectorHandler;
    //private WebThingsConnectorHandler thingsHandler;

    public WebThingsSocketHandler(WebThingsConnectorHandler connectorHandler) {
        this.closeLatch = new CountDownLatch(1);
        this.connectorHandler = connectorHandler;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return the lastMessage
     */
    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * @param lastMessage the lastMessage to set
     */
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.info("Connection closed - Code: {} Reason: {}", statusCode, reason);
        connectorHandler.updateStatus(ThingStatus.OFFLINE);
        this.session = null;
        this.closeLatch.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("Got connect: {}", session);
        this.session = session;
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

                    if(command.getMessageType().equals("propertyStatus") && !msg.equals(getLastMessage().replace("setProperty", "propertyStatus"))){
                        // Get thing UID
                        String thing = connectorHandler.getHandlerConfig().uid.replace(":", "_");
                        List<String> channels = new ArrayList<String>(command.getData().keySet());

                        // 
                        for(String channel: channels){
                            String item = thing + "_" + channel;
                            try{
                                // API call to get itemType, possibly save itemType in property name for custom webthings
                                ItemDTO itemDTO = getOpenhabItem(item);
                                String itemType = itemDTO.type;

                                try{       
                                    // get string for API call based on command
                                    String value = getCommandFromProperty(itemType, command.getData().get(channel).toString());
                                    if(value != null){
                                        logger.info("Updating openHAB item: {} value: {}", item, value);

                                        // Update openHAB item via API
                                        updateOpenhabItem(value, item);
                                    }else{
                                        logger.warn("Could not find itemType: {} for thing: {}", channel, thing);
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
                        connectorHandler.updateStatus(ThingStatus.OFFLINE);
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

        // Try to reconnect when error occuers
        if(cause instanceof CloseException || cause instanceof EofException){
            //https://stackoverflow.com/questions/44095346/reconnect-after-onwebsocketclose-jetty-9-4
            thingReachable = connectorHandler.reconnect(4);
        } else if(cause instanceof SSLHandshakeException){
            logger.error("SSL error: {}", cause.getMessage());
        }else{
            logger.error("WebSocket Error: {}", cause.getMessage());
        }

        // Set thing to offline if reconnect not possible
        if(thingReachable){
            connectorHandler.updateStatus(ThingStatus.ONLINE);
        }else{
            connectorHandler.updateStatus(ThingStatus.OFFLINE);
        }
    }
}
