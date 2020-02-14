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
package org.openhab.binding.webthings.internal.events;

import static org.openhab.binding.webthings.internal.WebThingsBindingConstants.*;
import static org.openhab.binding.webthings.internal.handler.WebThingsHandler.*;
import org.openhab.binding.webthings.internal.json.ItemStateEventPayload;

import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.smarthome.core.events.*;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.openhab.binding.webthings.internal.handler.WebThingsConnectorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;


/**
 * The {@link WebThingsItemEventSubscriber} is responsible to track item events and forward them to the WebThing
 *
 * @author schneider_sven - Initial contribution
 */
@Component
public class WebThingsItemEventSubscriber implements EventSubscriber {
    private final Logger logger = LoggerFactory.getLogger(WebThingsItemEventSubscriber.class);

    private final Set<String> subscribedEventTypes = new HashSet<>();
    {
        subscribedEventTypes.add(ItemStateChangedEvent.TYPE);
        //subscribedEventTypes.add(ItemCommandEvent.TYPE);
    }

    private final EventFilter eventFilter = new TopicEventFilter("smarthome/items/.*");

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public EventFilter getEventFilter() {
        return eventFilter;
    }

    @Override
    public void receive(Event event) {
        String topic = event.getTopic();
        String type = event.getType();
        String payload = event.getPayload();

        logger.debug("\n-------------------------------- Received event - Type: {} - Topic: {} --------------------------------", type, topic);

        if (event instanceof ItemStateChangedEvent) {
            //ItemStateChangedEvent itemStateEvent = (ItemStateChangedEvent) event;
            if(topic.indexOf("_") >= 0){
                // TODO: Handle channel names with multiple parts (e.g. color_temp)
                String thingID = getThingFromTopic(topic);
                String channel;
                if(topic.contains("color_temperature")){
                    thingID = thingID.substring(0, thingID.lastIndexOf(":"));
                    channel = "color_temperature";
                }else{
                    channel = getChannelFromTopic(topic);
                }

                // Get handler of relevant connector
                if(CONNECTOR_HANDLER_LIST.containsKey(thingID)){
                    WebThingsConnectorHandler handler = CONNECTOR_HANDLER_LIST.get(thingID);
                    
                    /* Only works for self hosted things
                    for(Thing thing: WEBTHINGS_HANDLER.getWebThingList()){
                        if(thing.getId().equals(thingID)){
                            thing.setProperty(channel, value);
                        }
                    }
                    */

                    // Get session of socket to WebThing
                    Session session = handler.getSocket().getSession();
                    if(session != null && session.isOpen()){
                        logger.debug("Session ready to send");

                        // Transform payload to command and get usable JSON from that command
                        Gson g = new Gson();
                        ItemStateEventPayload command = g.fromJson(payload, ItemStateEventPayload.class);
                        String jsonCommand = getPropertyFromCommand(handler.getHandlerConfig().id, channel, command);
                        if(jsonCommand != null){
                            if(!jsonCommand.replace("setProperty", "propertyStatus").equals(handler.getSocket().getLastMessage())){
                                try {
                                    // Send command to WebThing via socket
                                    session.getRemote().sendString(jsonCommand);
                                    logger.info("Send command to WebThing: {}", jsonCommand);
                                    handler.getSocket().setLastMessage(jsonCommand);  
                                } catch (IOException e) {
                                    logger.warn("Could not send command: {} to WebThing: {}", jsonCommand, thingID);
                                }
                            } else{
                                logger.debug("Not sending: Command equals last send message - Command: {}", jsonCommand);
                            }                      
                        }else{
                            logger.warn("Could not convert OH Command to Json String");
                        }
                    } else{
                        logger.warn("Could not process OH command: Session is empty or not open");
                    }
                }
            }
        }
    }

    /**
     * Extract thing ID from zopic
     * @param topic
     * @return Thing ID
     */
    private String getThingFromTopic(String topic){
        String thingID = topic.substring(0, topic.lastIndexOf("_", topic.length()));
        thingID = thingID.substring(thingID.lastIndexOf("/") +1, thingID.length());
        thingID = thingID.replace("_", ":");

        return thingID;
    }

    /**
     * Extract channel id from topic
     * @param topic
     * @return channel id
     */
    private String getChannelFromTopic(String topic){
        String channel = topic.substring(topic.lastIndexOf("_") +1, topic.length());
        channel = channel.substring(0, channel.lastIndexOf("/", channel.length()));

        return channel;
    }
}
