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
import static org.openhab.binding.webthings.internal.converters.OpenhabToWebThingConverter.*;
import org.openhab.binding.webthings.internal.dto.ItemStateEventPayload;

import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.smarthome.core.events.*;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.openhab.binding.webthings.internal.handler.WebThingsWebThingHandler;
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
            if(topic.contains("webthings_webthing")){
                String thingUID = topic.substring(ordinalIndexOf(topic, "/", 1) +1, ordinalIndexOf(topic, "_", 2));
                thingUID = thingUID.replace("_", ":");
                if(WEBTHING_HANDLER_LIST.containsKey(thingUID)){
                    WebThingsWebThingHandler handler = WEBTHING_HANDLER_LIST.get(thingUID);
                    // Get channel ID based on UID
                    String channelId = topic.substring(ordinalIndexOf(topic, "_", 2) +1, topic.lastIndexOf("/"));

                    Session session = handler.getSocket().getSession();
                    if(session != null && session.isOpen()){
                        logger.debug("Session ready to send");

                        // Transform payload to command and get usable JSON from that command
                        Gson g = new Gson();
                        ItemStateEventPayload command = g.fromJson(payload, ItemStateEventPayload.class);
                        String jsonCommand = getPropertyFromCommand("", channelId, command);
                        if(jsonCommand != null){
                            String lastMessage = handler.getSocket().getLastMessage();
                            if(lastMessage == null || !jsonCommand.replace("setProperty", "propertyStatus").equals(lastMessage.replaceAll("\\s+",""))){
                                try {
                                    // Send command to WebThing via socket
                                    session.getRemote().sendString(jsonCommand);
                                    logger.info("Send command to WebThing: {}", jsonCommand);
                                    handler.getSocket().setLastMessage(jsonCommand);  
                                } catch (IOException e) {
                                    logger.warn("Could not send command: {} to WebThing: {}", jsonCommand, thingUID);
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

    private int ordinalIndexOf(String str, String substr, int n) {
        int pos = -1;
        do {
            pos = str.indexOf(substr, pos + 1);
        } while (n-- > 0 && pos != -1);
        return pos;
    }
}
