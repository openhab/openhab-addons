/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.handler;

import static org.openhab.binding.network.NetworkBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.network.service.InvalidConfigurationException;
import org.openhab.binding.network.service.NetworkService;
import org.openhab.binding.network.service.StateUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Marc Mettke - Initial contribution
 */
public class NetworkHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(NetworkHandler.class);
	private NetworkService networkService;
    
	public NetworkHandler(Thing thing) {
		super(thing);
	}

	
	@Override
	public void dispose() {
		networkService.stopAutomaticRefresh();
	}
	
	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
	            case CHANNEL_ONLINE:
	            	try {
						State state = networkService.updateDeviceState() < 0 ? OnOffType.OFF : OnOffType.ON;
						updateState(CHANNEL_ONLINE, state);					
					} catch( InvalidConfigurationException invalidConfigurationException) {
					    updateStatus(ThingStatus.OFFLINE);
					}
	            	break;
	            case CHANNEL_TIME:
	            	try {
						State state = new DecimalType(networkService.updateDeviceState());
						updateState(CHANNEL_TIME, state);					
					} catch( InvalidConfigurationException invalidConfigurationException) {
					    updateStatus(ThingStatus.OFFLINE);
					}
	            	break;
	            default:
	                logger.debug("Command received for an unknown channel: {}", channelUID.getId());
	                break;
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }	
	}
	
	@Override
	public void initialize() {
        logger.debug("Initializing Network handler.");
		this.networkService = new NetworkService();
		Configuration conf = this.getConfig();
		
		super.initialize();
		
		networkService.setHostname(String.valueOf(conf.get(PARAMETER_HOSTNAME)));
		
		try {
			networkService.setPort(Integer.parseInt(String.valueOf(conf.get(PARAMETER_PORT))));
		} catch (Exception ex) {}
		
		try {
			networkService.setRetry(Integer.parseInt(String.valueOf(conf.get(PARAMETER_RETRY))));
		} catch (Exception ex) {}
		
		try {
			networkService.setRefreshInterval(Long.parseLong(String.valueOf(conf.get(PARAMETER_REFRESH_INTERVAL))));
		} catch (Exception ex) {}

		try {
			networkService.setTimeout(Integer.parseInt(String.valueOf(conf.get(PARAMETER_TIMEOUT))));			
		} catch (Exception ex) {}
		
		try {
			networkService.setUseSystemPing(Boolean.parseBoolean(String.valueOf(conf.get(PARAMETER_USE_SYSTEM_PING))));
		} catch (Exception ex) {}
		
		networkService.startAutomaticRefresh(scheduler, new StateUpdate() {
            @Override
            public void newState(double state) {
    		State onlineState = state < 0 ? OnOffType.OFF : OnOffType.ON;
    		State timeState = new DecimalType(state);
    		updateState(CHANNEL_ONLINE, onlineState);
    		updateState(CHANNEL_TIME, timeState);
	    }
            @Override
            public void invalidConfig() {
                updateStatus(ThingStatus.OFFLINE);
            }
        });
	}

}
