/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.networkhealth.handler;

import static org.openhab.binding.networkhealth.NetworkHealthBindingConstants.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.actions.Ping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkHealthHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Marc Mettke - Initial contribution
 */
public class NetworkHealthHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(NetworkHealthHandler.class);
    
	public String hostname;
    public int port;
	private long refreshInterval;
	ScheduledFuture<?> refreshJob;
	
    public int timeout;

    
	public NetworkHealthHandler(Thing thing) {
		super(thing);
	}

	
	@Override
	public void dispose() {
		refreshJob.cancel(true);
	}
	
	private void getDeviceState() {
		boolean success = false;
		
		try {
			success = Ping.checkVitality(this.hostname, this.port, this.timeout);

			logger.debug("established connection [host '{}' port '{}' timeout '{}']", new Object[] {hostname, port, timeout});
		} 
		catch (SocketTimeoutException se) {
			logger.debug("timed out while connecting to host '{}' port '{}' timeout '{}'", new Object[] {hostname, port, timeout});
		}
		catch (IOException ioe) {
			logger.debug("couldn't establish network connection [host '{}' port '{}' timeout '{}']", new Object[] {hostname, port, timeout});
		}
		
		updateState(CHANNEL_ONLINE, success ? OnOffType.ON : OnOffType.OFF);
	}
	
	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
            case CHANNEL_ONLINE:
            	getDeviceState();
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
        logger.debug("Initializing NetworkHealth handler.");
		Configuration conf = this.getConfig();
		super.initialize();
		
		this.hostname = String.valueOf(conf.get(PARAMETER_HOSTNAME));
		
		try {
			this.port = Integer.parseInt(String.valueOf(conf.get(PARAMETER_PORT)));
		} catch (Exception ex) {
			this.port = 0;
		}

		try {
			this.timeout = Integer.parseInt(String.valueOf(conf.get(PARAMETER_TIMEOUT)));			
		} catch (Exception ex) {
			this.timeout = 5000;
		}
		
		try {
			this.refreshInterval = Long.parseLong(String.valueOf(conf.get(PARAMETER_REFRESH_INTERVAL)));
		} catch (Exception ex) {
			this.refreshInterval = 60000;
		}
		
		startAutomaticRefresh();
	}
	
	private void startAutomaticRefresh() {
		Runnable runnable = new Runnable() {
			public void run() {
				getDeviceState();
			}
		};
		
		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refreshInterval, TimeUnit.MILLISECONDS);
	}

}
