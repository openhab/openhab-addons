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

import static org.openhab.binding.webthings.internal.WebThingsBindingConstants.*;
import static org.openhab.binding.webthings.internal.WebThingsBindingGlobals.*;
import static org.openhab.binding.webthings.internal.utilities.WebThingsRestApiUtilities.getSecurityLink;

import org.openhab.binding.webthings.internal.config.WebThingsConnectorConfiguration;
import org.openhab.binding.webthings.internal.websockets.WebThingsConnectorSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebThingsConnectorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sven Schneider - Initial contribution
 */
@NonNullByDefault
public class WebThingsConnectorHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WebThingsConnectorHandler.class);

    private @Nullable WebThingsConnectorConfiguration config;

    private WebSocketClient client = new WebSocketClient();
    private WebThingsConnectorSocketHandler socket = new WebThingsConnectorSocketHandler(this);

    // private @Nullable WebThingsWsClient ws;

    public WebThingsConnectorHandler(Thing thing) {
        super(thing);
    }

    /**
     * @return the socket
     */
    public WebThingsConnectorSocketHandler getSocket() {
        return socket;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Re-create thing when update channel is used to restart websocket 
        if (CHANNEL_UPDATE.equals(channelUID.getId())) {
            Session session = socket.getSession();
            if(session != null){
                if(!session.isOpen()){
                    this.thingUpdated(this.thing);
                }
                if (command == OnOffType.ON) {
                    try {
                        this.thingUpdated(this.getThing());
                    } catch (Exception e) {
                        logger.error("Could not update thing: {}", e.getMessage());
                    }
                } else if (command == OnOffType.OFF) {
                    try {
                        this.thingUpdated(this.getThing());
                    } catch (Exception e) {
                        logger.error("Could not update thing: {}", e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(WebThingsConnectorConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            boolean thingReachable;

            try {
                // Start websocket client
                client.start();

                // Get relevant information for websocket
                URI echoUri = getUriFromConfig();
                ClientUpgradeRequest request = new ClientUpgradeRequest();

                // Remove idel timeout
                client.setMaxIdleTimeout(0);

                // Connect to websocket
                client.connect(socket, echoUri, request);
                logger.info("Connecting to : {}", echoUri);

                // Save handler to access later
                CONNECTOR_HANDLER_LIST.put(config.uid, this);

                // Setup successful 
                thingReachable = true;
            } catch (Exception e) {
                logger.error("Could not create connector: {}", e.getMessage());
                thingReachable = false;
            }

            // Set thing status
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not create websocket");
            }
        });
    }

    public void dispose() {
        // Close websocket before disposing of handler
        try {
            socket.awaitClose(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Could not close socket: {}", e.getMessage());
        } finally {
            try {
                client.stop();
            } catch (Exception e) {
                logger.error("Could not stop socket client: {}", e.getMessage());
            }
        }
        if(config != null && config.uid != null && CONNECTOR_HANDLER_LIST.containsKey(config.uid)){
            CONNECTOR_HANDLER_LIST.remove(config.uid);
        }
        super.dispose();
    }

    /**
     * Try to Reconnect to websocket
     * @param maxTries Maximum number of tries to reconnect
     * @throws IOException 
     * @return Reconnect successful or not
     */
    public boolean reconnect(int maxTries) {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, "Lost connection. Trying to reconnect websocket."); 
        int count = 0;
        while(true){
            logger.warn("Trying to reconnect to thing: {} - Try: {}/{}", config.id, count, maxTries); 
            try {
                client.start();
                client.connect(socket, getUriFromConfig(), new ClientUpgradeRequest());
                return true;
            } catch (IOException e) {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e1) {
                    logger.error("Could not stop sleep thread: {}", e1.getMessage());
                    break;
                }
                if(++count == maxTries){
                    logger.error("Could not reconnect to websocket - Error: {}", e.getMessage()); 
                    return false;
                };
            } catch(URISyntaxException e){
                logger.error("URISyntaxException in reconnect method: {}", e.getMessage());
                return false;
            } catch(Exception e){
                logger.error("Exception in reconnect method: {}", e.getMessage());
                return false;
            }
        }
        return false; 
    }

    /**
     * Get URI for websocket based on binding configuration
     * @return URI
     */
    private URI getUriFromConfig() throws URISyntaxException{
        String destUri;
        String websocketType;
        String host;

        // Do not use SSL when local address is used
        if(serverUrl.contains(".mozilla-iot.org")){
            websocketType = "wss://";
            host = serverUrl.replaceFirst("https://", "");
        }else{
            websocketType = "ws://";
            host = serverUrl.replaceFirst("http://", "");
            logger.warn("WARNING: WebSocket uses unsecured connection (no SSL)!");
        }
      
        // Clean up link
        String base = "";
        String security = "bearer";
        String securityToken = token;

        if(config.gateway){
            base = "things/";
        }else{
            security = config.security;
            securityToken = config.securityToken;
        }

        if(serverUrl.substring(serverUrl.length()-1).equals("/")){
            destUri = websocketType + host + base + config.id + getSecurityLink(security, securityToken);
        }else{
            destUri = websocketType + host + "/" + base + config.id + getSecurityLink(security, securityToken);
        }
        return new URI(destUri);
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    /**
     * Get Config of Thing
     * @return config
     */
    @Nullable
    public WebThingsConnectorConfiguration getHandlerConfig(){
        return config;
    }
}
