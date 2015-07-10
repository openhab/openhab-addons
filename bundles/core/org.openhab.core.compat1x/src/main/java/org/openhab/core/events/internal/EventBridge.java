/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.events.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.openhab.core.compat1x.internal.TypeMapper;
import org.openhab.core.types.Command;
import org.openhab.core.types.EventType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

/**
 * This class acts as a bridge between events from openHAB 1.x (using "openhab" as a topic prefix) and
 * Eclipse SmartHome (using "smarthome" as a topic prefix).
 * It simply duplicates events with an updated topic prefix and works both ways.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class EventBridge implements EventHandler, EventSubscriber {

    private static final String BRIDGEMARKER = "bridgemarker";
    private EventAdmin eventAdmin;
    private EventPublisher eventPublisher;

    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    public void unsetEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Override
    public void handleEvent(Event event) {

        if (!Boolean.TRUE.equals(event.getProperty(BRIDGEMARKER))) {

            // map event from openHAB to ESH
            if (event.getTopic().startsWith(org.openhab.core.events.EventConstants.TOPIC_PREFIX)) {
                if (event.getTopic().endsWith(EventType.COMMAND.name())) {
                    String itemName = (String) event.getProperty("item");
                    Command ohCommand = (Command) event.getProperty("command");
                    ItemCommandEvent eshEvent = ItemEventFactory.createCommandEvent(itemName,
                            (org.eclipse.smarthome.core.types.Command) TypeMapper.mapToESHType(ohCommand));
                    eventPublisher.post(eshEvent);
                } else if (event.getTopic().endsWith(EventType.UPDATE.name())) {
                    String itemName = (String) event.getProperty("item");
                    State ohState = (State) event.getProperty("state");
                    ItemStateEvent eshEvent = ItemEventFactory.createStateEvent(itemName,
                            (org.eclipse.smarthome.core.types.State) TypeMapper.mapToESHType(ohState));
                    eventPublisher.post(eshEvent);
                }
            }
        }
    }

    private Map<String, Object> constructProperties(org.eclipse.smarthome.core.events.Event event) {
        Map<String, Object> properties = new HashMap<>();
        if (event instanceof ItemCommandEvent) {
            ItemCommandEvent icEvent = (ItemCommandEvent) event;
            String itemName = (String) icEvent.getItemName();
            properties.put("item", itemName);
            Type eshType = TypeMapper.mapToOpenHABType(icEvent.getItemCommand());
            if (eshType instanceof Command) {
                properties.put("command", (Command) eshType);
            } else {
                return null;
            }
        } else {
            ItemStateEvent isEvent = (ItemStateEvent) event;
            String itemName = (String) isEvent.getItemName();
            properties.put("item", itemName);
            Type eshType = TypeMapper.mapToOpenHABType(isEvent.getItemState());
            if (eshType instanceof State) {
                properties.put("state", (State) eshType);
            } else {
                return null;
            }
        }
        properties.put(BRIDGEMARKER, true);
        return properties;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        Set<String> types = new HashSet<>(2);
        types.add(ItemCommandEvent.TYPE);
        types.add(ItemStateEvent.TYPE);
        return types;
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(org.eclipse.smarthome.core.events.Event event) {
        if (event.getType().equals(ItemCommandEvent.TYPE)) {
            Map<String, Object> properties = constructProperties(event);
            if (properties != null) {
                String topic = org.openhab.core.events.EventConstants.TOPIC_PREFIX + "/" + EventType.COMMAND + "/"
                        + properties.get("item");
                eventAdmin.postEvent(new Event(topic, properties));
            }
        } else if (event.getType().equals(ItemStateEvent.TYPE)) {
            Map<String, Object> properties = constructProperties(event);
            if (properties != null) {
                String topic = org.openhab.core.events.EventConstants.TOPIC_PREFIX + "/" + EventType.UPDATE + "/"
                        + properties.get("item");
                ;
                eventAdmin.postEvent(new Event(topic, properties));
            }
        }
    }

}
