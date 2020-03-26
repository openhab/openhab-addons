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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The {@link WebThingsWebThingSocketHandler} is responsible for handling the websocket with webthings
 *
 * @author schneider_sven - Initial contribution
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public abstract class WebThingsSocketHandler{
    protected final Logger logger = LoggerFactory.getLogger(WebThingsSocketHandler.class);
    protected final CountDownLatch closeLatch;
    protected final Object onMessageLock = new Object();
    
    @SuppressWarnings("unused")
    protected Session session;
    protected String lastMessage;
    //private WebThingsConnectorHandler thingsHandler;

    public WebThingsSocketHandler() {
        this.closeLatch = new CountDownLatch(1);
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
        this.session = null;
        this.closeLatch.countDown(); // trigger latch
        dispose();
    }

    public abstract void dispose();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("Got connect: {}", session);
        this.session = session;
    }

    public abstract void onMessage(Session session, String msg);

    public abstract void onError(Throwable cause);

    public String sendCommand(String jsonCommand, String uid){
        if(jsonCommand != null){
            if(!jsonCommand.replace("setProperty", "propertyStatus").equals(lastMessage)){
                try {
                    // Send command to WebThing via socket
                    session.getRemote().sendString(jsonCommand);
                    logger.info("Send command to WebThing: {}", jsonCommand);
                    lastMessage = jsonCommand;  
                } catch (IOException e) {
                    logger.warn("Could not send command: {} to WebThing: {}", jsonCommand, uid);
                }
            } else{
                logger.debug("Not sending: Command equals last send message - Command: {}", jsonCommand);
            }                      
        }else{
            logger.warn("Could not convert OH Command to Json String");
        }
        return null;
    }
}
