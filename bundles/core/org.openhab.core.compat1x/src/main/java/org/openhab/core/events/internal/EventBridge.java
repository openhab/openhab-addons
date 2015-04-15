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
import java.util.Map;

import org.eclipse.smarthome.core.events.EventConstants;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.core.compat1x.internal.ItemMapper;
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
public class EventBridge implements EventHandler {

    private static final String BRIDGEMARKER = "bridgemarker";
	private EventAdmin eventAdmin;
	private ItemRegistry itemRegistry;

    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    public void unsetEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

	@Override
	public void handleEvent(Event event) {
		
		if(!Boolean.TRUE.equals(event.getProperty(BRIDGEMARKER))) {
			
			// map event from ESH to openHAB
			if(event.getTopic().startsWith(EventConstants.TOPIC_PREFIX)) {
				String topic = org.openhab.core.events.EventConstants.TOPIC_PREFIX +
						event.getTopic().substring(EventConstants.TOPIC_PREFIX.length());
				Map<String, Object> properties = constructProperties(event);
				eventAdmin.postEvent(new Event(topic, properties));
			}
		
			// map event from openHAB to ESH
			if(event.getTopic().startsWith(org.openhab.core.events.EventConstants.TOPIC_PREFIX)) {
				String topic = EventConstants.TOPIC_PREFIX + 
						event.getTopic().substring(org.openhab.core.events.EventConstants.TOPIC_PREFIX.length());
				Map<String, Object> properties = constructProperties(event);
				eventAdmin.postEvent(new Event(topic, properties));
			}
		}
	}

	private Map<String, Object> constructProperties(Event event) {
		String[] propertyNames = event.getPropertyNames();
		Map<String, Object> properties = new HashMap<>();
		String itemName = (String) event.getProperty("item");
		if(itemName!=null) {
			for(String propertyName : propertyNames) {
				if(propertyName.equals("command")) {
					try {
						Item item = itemRegistry.getItem(itemName);
						org.openhab.core.items.Item ohItem = ItemMapper.mapToOpenHABItem(item);
						if(ohItem!=null) {
							org.openhab.core.types.Command command = 
									org.openhab.core.types.TypeParser.parseCommand(
											ohItem.getAcceptedCommandTypes(), event.getProperty(propertyName).toString());
							properties.put(propertyName, command);
						}
					} catch (ItemNotFoundException e) {}
				} else if(propertyName.equals("state")) {
					try {
						Item item = itemRegistry.getItem(itemName);
						org.openhab.core.items.Item ohItem = ItemMapper.mapToOpenHABItem(item);
						if(ohItem!=null) {
							org.openhab.core.types.State state = 
									org.openhab.core.types.TypeParser.parseState(
											ohItem.getAcceptedDataTypes(), event.getProperty(propertyName).toString());
							properties.put(propertyName, state);
						}
					} catch (ItemNotFoundException e) {}
				} else {
					properties.put(propertyName, event.getProperty(propertyName));
				}
			}
		}
		properties.put(BRIDGEMARKER, true);
		return properties;
	}

}
