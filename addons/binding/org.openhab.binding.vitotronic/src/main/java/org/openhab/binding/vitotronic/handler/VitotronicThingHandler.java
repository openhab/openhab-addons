/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vitotronic.handler;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VitotronicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Stefan Andres - Initial contribution
 */

public class VitotronicThingHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(VitotronicThingHandler.class);
    
    VitotronicBridgeHandler bridgeHandler;


	public VitotronicThingHandler(Thing thing) {
		super(thing);
	}
	
	@Override
	public void initialize() {
		bridgeHandler = getBridgeHandler();
		logger.debug("Thing Handler for {} started", getThing().getUID().getId() );
		registerVitotronicThingListener();
	}
	
	@Override
	public void dispose() {
		logger.debug("Thing Handler for {} stop", getThing().getUID().getId() );
		unregisterVitotronicThingListener();
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.trace("Handle command for channel '{}' command '{}'", channelUID.getId(), command.toString());
		bridgeHandler.updateChannel(getThing().getUID().getId(), channelUID.getId(), command.toString());
     
	}
	
	public void updateStatus(ThingStatus status ) {
		super.updateStatus(status);
	}
	
	

	private synchronized  VitotronicBridgeHandler getBridgeHandler() {
		
		VitotronicBridgeHandler bridgeHandler = null;
		
		Bridge bridge = getBridge();
		  if (bridge == null) {
		     logger.debug("Required bridge not defined for device {}.");
		     return null;
		  }
		  ThingHandler handler = bridge.getHandler();
		  if (handler instanceof VitotronicBridgeHandler) {
		      bridgeHandler = (VitotronicBridgeHandler) handler;
		  } else {
			  logger.debug("No available bridge handler found for bridge: {} .", bridge.getUID());
			  bridgeHandler = null;
		  }	
		return bridgeHandler;
	}
	
	
	
	private void registerVitotronicThingListener(){
		bridgeHandler.registerVitotronicThingListener(this);
	}
	
	private void unregisterVitotronicThingListener(){
		bridgeHandler.unregisterThingListener(this);
		
	}
	
	
	public void setChannelValue(String channelId, String value) {
		Channel channel = getThing().getChannel(channelId);
		if (channel == null) {
			logger.trace("Cannel '{}:{}' not implemented", getThing().getUID().getId(), channelId);
			return;
		} 		
			
	    logger.trace("Set {}:{}:{} = {}", getThing().getUID().getId(), channelId, channel.getAcceptedItemType(), value );			
		switch (channel.getAcceptedItemType()) {
			case "Number" : 
				this.updateState(channelId, new DecimalType(value));
				break;
			case "Switch" :
				if (value.toUpperCase().contains("ON")) {
					this.updateState(channelId, OnOffType.ON);
				} else {
					this.updateState(channelId, OnOffType.OFF);
						}
			break;
			case "DateTime" : 
				this.updateState(channelId, new DateTimeType(value));
			break;
			default: 
				logger.trace("Type '{}' for channel '{}' not implemented", channel.getAcceptedItemType() , channelId);
		}
	}
}
