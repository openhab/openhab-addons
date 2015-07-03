/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.events;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.openhab.core.compat1x.internal.TypeMapper;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPublisherDelegate implements org.openhab.core.events.EventPublisher {

	private static final Logger logger = LoggerFactory.getLogger(EventPublisherDelegate.class);
	
	private EventPublisher eventPublisher;
	
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
	}

	@Override
	public void sendCommand(String itemName, Command command) {
	    // we do not offer synchronous sending of commands anymore
	    postCommand(itemName, command);
	}

	@Override
	public void postCommand(String itemName, Command command) {
		org.eclipse.smarthome.core.types.Command eshCommand = (org.eclipse.smarthome.core.types.Command) TypeMapper.mapToESHType(command);
        if(eshCommand!=null) {
            ItemCommandEvent event = ItemEventFactory.createCommandEvent(itemName, eshCommand);
            eventPublisher.post(event);
        } else {
            logger.warn("Compatibility layer could not convert {} of type {}.", command.toString(), command.getClass().getSimpleName() );
        }
	}

	@Override
	public void postUpdate(String itemName, State newState) {
		org.eclipse.smarthome.core.types.State eshState = (org.eclipse.smarthome.core.types.State) TypeMapper.mapToESHType(newState);
		if(eshState!=null) {
            ItemStateEvent event = ItemEventFactory.createStateEvent(itemName, eshState);
            eventPublisher.post(event);
		} else {
		    logger.warn("Compatibility layer could not convert {} of type {}.", newState.toString(), newState.getClass().getSimpleName() );
		}
	}
}
