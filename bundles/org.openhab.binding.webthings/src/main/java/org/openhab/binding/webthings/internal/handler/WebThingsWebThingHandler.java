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
import static org.openhab.binding.webthings.internal.WebThingsBindingGlobals.token;
import static org.openhab.binding.webthings.internal.WebThingsBindingGlobals.reconnectInterval;
import static org.openhab.binding.webthings.internal.utilities.WebThingsRestApiUtilities.*;
import static org.openhab.binding.webthings.internal.converters.WebThingToOpenhabConverter.*;

import org.openhab.binding.webthings.internal.config.WebThingsWebThingConfiguration;
import org.openhab.binding.webthings.internal.websockets.WebThingsWebThingSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebThingsConnectorHandler} is responsible for handling commands,
 * which are sent to one of the channels.
 *
 * @author Sven Schneider - Initial contribution
 */
@NonNullByDefault
public class WebThingsWebThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WebThingsWebThingHandler.class);

    private @Nullable WebThingsWebThingConfiguration config;
    private @Nullable ThingBuilder thingBuilder;

    private WebSocketClient client = new WebSocketClient();
    private WebThingsWebThingSocketHandler socket = new WebThingsWebThingSocketHandler(this);


    public WebThingsWebThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * @return the socket
     */
    public WebThingsWebThingSocketHandler getSocket() {
        return socket;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_UPDATE.equals(channelUID.getId())) {
            Session session = socket.getSession();
            if(session != null){
                if(!session.isOpen()){
                    this.thingUpdated(this.thing);
                }
                if (command == OnOffType.ON || command == OnOffType.OFF) {
                    try {
                        this.thingUpdated(this.getThing());
                    } catch (Exception e) {
                        logger.error("Could not update thing: {}", e.getMessage());
                    }
                }
            }
        } /*else if (command != RefreshType.REFRESH){
            // Get channel ID based on UID
            String channelUIDString = channelUID.toString();
            String channelId = channelUIDString.substring(channelUIDString.lastIndexOf(":")+1);

            // Convert relevant information from command
            Object commandClass = command.getClass();
            String type = commandClass.toString().substring(commandClass.toString().lastIndexOf(".")+1, commandClass.toString().lastIndexOf("Type"));
            String value = command.toString();

            // Convert command into JsonString
            String jsonCommand = getPropertyFromCommand("", channelId, new ItemStateEventPayload(type, value));

            // Send command to WebThing via socket
            socket.sendCommand(jsonCommand, this.getThing().getUID().toString());
        }*/
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public void initialize() {
        // Import config
        config = getConfigAs(WebThingsWebThingConfiguration.class);

        // If selected, import token from binding config and save into thing configuration
        if((config.securityToken == null || config.securityToken.isEmpty()) && config.importToken){
            Configuration newConfig = getConfig();
            newConfig.put("securityToken", token);
            updateConfiguration(newConfig);
            thingUpdated(this.getThing());
        }

        updateStatus(ThingStatus.UNKNOWN);

        // If a security scheme was selected but no respective information was provided --> Output error
        if(!config.security.equals("none") && (config.securityToken == null || config.security.isEmpty())){
            logger.error("Not enough security information provided to import from {}", config.link);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Not enough security information provided");
            return;
        }

        // Import WebThing as JsonObject
        JsonObject webThing;
        try {
            webThing = getWebThing(config.link, config.security, config.securityToken);
        } catch (IOException e) {
            logger.error("Could not import WebThing from {} - Error: {}", config.link, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not import WebThing");
            return;
        }

        // Get thingBuilder and build openHAB thing based on imported WebThing
        thingBuilder = editThing();
        try{
            createThingfromWebThing(webThing);
            updateThing(thingBuilder.build());
        } catch(IllegalArgumentException e){
            logger.warn("No new channels added - Error: {}", e.getMessage());
        }

        logger.debug("Created custom webthing");

        scheduler.execute(() -> {
            boolean thingReachable;

            try {
                // Start websocket client
                client.start();

                // Get relevant information for websocket
                URI echoUri = getUriFromConfig();
                ClientUpgradeRequest request = new ClientUpgradeRequest();

                // Remove idle timeout
                client.setMaxIdleTimeout(0);

                // Connect to websocket
                client.connect(socket, echoUri, request);
                logger.info("Connecting to : {}", echoUri);

                // Setup successful 
                thingReachable = true;
            } catch (Exception e) {
                logger.error("Could not create connector: {}", e.getMessage());
                thingReachable = false;
            }
            // Set thing status
            if (thingReachable) {
                WEBTHING_HANDLER_LIST.put(this.getThing().getUID().toString(), this);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not create websocket");
            }

            // Import current property statuses to sync after first creation
            JsonObject propertyStatuses;
            try {
                propertyStatuses = getWebThing(config.link, config.security, config.securityToken, "/properties");
            } catch (IOException e) {
                logger.warn("Could not import WebThing property statuses from {} - Error: {}", config.link, e.getMessage());
                return;
            }

            // Set channel for each property
            logger.info("Syncing item and property states for {}", this.getThing().getUID());
            syncThingStatuses(propertyStatuses);
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
        if(WEBTHING_HANDLER_LIST.containsKey(this.getThing().getUID().toString())){
            WEBTHING_HANDLER_LIST.remove(this.getThing().getUID().toString());
        }
        super.dispose();
    }

    /**
     * Interacts with the thingBuilder to create an openHAB thing based on a WebThing
     * @param webThing to be converted into openHAB thing
     * @throws IllegalArgumentException A channel exists already
     */
    private void createThingfromWebThing(JsonObject webThing) throws IllegalArgumentException {
        List<Channel> channels = new ArrayList<Channel>();
        Channel tmpChannel = this.getThing().getChannel(CHANNEL_UPDATE);
        channels.add(tmpChannel);

        JsonObject properties = webThing.getAsJsonObject("properties");
        for(String property: properties.keySet()){
            ChannelUID channelUID = new ChannelUID(this.thing.getUID().toString() + ":" + property);
            Map<String, String> channelMetaInfo = getChannelInfoFromProperty(property, properties);
            ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID, channelMetaInfo.get("itemType"));

            channelBuilder.withDescription(channelMetaInfo.get("description"));
            channelBuilder.withLabel(channelMetaInfo.get("label"));

            Set<String> defaultTags = new HashSet<String>();
            String tag = channelMetaInfo.get("defaultTag");
            if(!tag.isEmpty()){
                defaultTags.add(tag);
            }
            channelBuilder.withDefaultTags(defaultTags);

            channels.add(channelBuilder.build());
        }

        if(!channels.isEmpty()){
            try{
                thingBuilder.withChannels(channels);
            } catch(IllegalArgumentException e){
                throw new IllegalArgumentException();
            }
        }

        if(webThing.has("title") && !webThing.get("title").isJsonNull()){
            thingBuilder.withLabel("WebThing: " + webThing.get("title"));
        }
    }

    /**
     * Sync openHAB and WebThing channel/property statuses
     * @param propertyStatuses Imported property statuses
     */
    private void syncThingStatuses(JsonObject propertyStatuses){
        for(String property: propertyStatuses.keySet()){
            Channel channel = this.getThing().getChannel(property);
            String itemType = channel.getAcceptedItemType();
            String value = propertyStatuses.get(property).getAsString();

            Command command = getCommandFromProperty(itemType, value);
            this.postCommand(channel.getUID(), command);

            /*Object commandClass = command.getClass();
            String type = commandClass.toString().substring(commandClass.toString().lastIndexOf(".")+1, commandClass.toString().lastIndexOf("Type"));
            String jsonCommand = getPropertyFromCommand("", property, new ItemStateEventPayload(type, command.toString()));
            socket.setLastMessage(jsonCommand);*/
        }
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
            logger.warn("Trying to reconnect to thing: {} - Try: {}/{}", this.getThing().getUID(), count +1, maxTries); 
            try {
                client.start();
                client.connect(socket, getUriFromConfig(), new ClientUpgradeRequest());
                return true;
            } catch (IOException e) {
                try {
                    Thread.sleep(reconnectInterval);
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
        String websocketAddress;

        // Do not use SSL when local address is used
        if(config.link.contains("https://")){
            websocketAddress = config.link.replaceFirst("https", "wss");
        }else{
            websocketAddress = config.link.replaceFirst("http", "ws");
            logger.warn("WARNING: WebSocket uses unsecured connection (no SSL)!");
        }
        
        destUri = websocketAddress + getSecurityLink(config.security, config.securityToken);
        
        return new URI(destUri);
    }
    
    @Override
    public void postCommand(ChannelUID channelUID, Command command) {
        super.postCommand(channelUID, command);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Nullable
    public WebThingsWebThingConfiguration getHandlerConfig(){
        return config;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
    }
}
