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
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPublisherDelegate implements org.openhab.core.events.EventPublisher {

	private static final Logger logger = LoggerFactory.getLogger(EventPublisherDelegate.class);
	
	private ItemRegistry itemRegistry;
	private EventPublisher eventPublisher;
	
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
	}

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

	@Override
	public void sendCommand(String itemName, Command command) {
		try {
			Item item = itemRegistry.getItem(itemName);
			org.eclipse.smarthome.core.types.Command eshCommand = TypeParser.parseCommand(item.getAcceptedCommandTypes(), command.toString());
            if(eshCommand!=null) {
                eventPublisher.sendCommand(itemName, eshCommand);
            } else {
                logger.warn("Compatibility layer could not convert {} of type {}.", command.toString(), command.getClass().getSimpleName() );
            }
		} catch (ItemNotFoundException e) {
			logger.warn("Could not process command event '{}' as item '{}' is unknown", command.toString(), itemName);
		}
	}

	@Override
	public void postCommand(String itemName, Command command) {
		try {
			Item item = itemRegistry.getItem(itemName);
			org.eclipse.smarthome.core.types.Command eshCommand = TypeParser.parseCommand(item.getAcceptedCommandTypes(), command.toString());
            if(eshCommand!=null) {
                eventPublisher.postCommand(itemName, eshCommand);
            } else {
                logger.warn("Compatibility layer could not convert {} of type {}.", command.toString(), command.getClass().getSimpleName() );
            }
		} catch (ItemNotFoundException e) {
			logger.warn("Could not process command event '{}' as item '{}' is unknown", command.toString(), itemName);
		}
	}

	@Override
	public void postUpdate(String itemName, State newState) {
		try {
			Item item = itemRegistry.getItem(itemName);
			org.eclipse.smarthome.core.types.State eshState = mapState(item, newState);
			if(eshState!=null) {
			    eventPublisher.postUpdate(itemName, eshState);
			} else {
			    logger.warn("Compatibility layer could not convert {} of type {}.", newState.toString(), newState.getClass().getSimpleName() );
			}
		} catch (ItemNotFoundException e) {
			logger.warn("Could not process command event '{}' as item '{}' is unknown", newState.toString(), itemName);
		}
	}

	protected org.eclipse.smarthome.core.types.State mapState(Item item, State state) {
	    org.eclipse.smarthome.core.types.State eshState = null;
	    if(state == UnDefType.NULL) {
	        eshState = org.eclipse.smarthome.core.types.UnDefType.NULL;
	    } else if ( state == UnDefType.UNDEF) {
	        eshState = org.eclipse.smarthome.core.types.UnDefType.UNDEF;
	    } else {
	        eshState = TypeParser.parseState(item.getAcceptedDataTypes(), state.toString());
	    }
	    return eshState;
	}
}
