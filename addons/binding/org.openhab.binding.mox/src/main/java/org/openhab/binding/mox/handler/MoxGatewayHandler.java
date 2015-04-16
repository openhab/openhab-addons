/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.handler;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mox.config.MoxGatewayConfig;
import org.openhab.binding.mox.protocol.MoxConnector;
import org.openhab.binding.mox.protocol.MoxMessage;
import org.openhab.binding.mox.protocol.MoxMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.openhab.binding.mox.MoxBindingConstants.STATE;

/**
 * The {@link MoxGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Thomas Eichstaedt-Engelen (innoQ) - Initial contribution
 * @since 2.0.0
 */
public class MoxGatewayHandler extends BaseThingHandler implements MoxMessageListener {

	private Logger logger = LoggerFactory.getLogger(MoxGatewayHandler.class);

	private MoxConnector connector;

	private List<MoxMessageListener> messageListeners = new CopyOnWriteArrayList<>();
	
	
	public MoxGatewayHandler(Thing thing) {
		super(thing);
	}
	
	
	@Override
	public void initialize() {
		super.initialize();

		logger.debug("Initializing Mox Gateway handler.");

		try {
			MoxGatewayConfig config = getConfigAs(MoxGatewayConfig.class);

			if (isNotBlank(config.udpHost) && config.udpPort != null) {
				logger.info("Listen for MOX datagrams on port {} on all interfaces.", config.udpPort);
				connector = new MoxConnector(config.udpHost, config.udpPort);
			}

			connector.setMessageHandler(this);

			connector.connect();
			connector.start();
		} catch (IOException e) {
			logger.error("Error creating listening socket.", e);
		} catch (ArithmeticException e) {
			logger.error("Cannot read config valuem. Maybe the port number was wrong.", e);
		}
	}
	
    @Override
    public void dispose() {
        if (connector != null) {
            try {
				connector.disconnect();
	            connector = null;
			} catch (IOException e) {
				logger.error("Error stop listening socket.", e);
			}
            logger.debug("Handler disposed.");
        } else {
        	logger.debug("Handler was not connected.");
        }
    }

    
	public boolean registerMessageListener(MoxMessageListener messageListener) {
		if (messageListener == null) {
			throw new NullPointerException("It's not allowed to pass a null MessageListener.");
		}
		return messageListeners.add(messageListener);
	}

	public boolean unregisterLightStatusListener(
			MoxMessageListener messageListener) {
		return messageListeners.remove(messageListener);
	}
	

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		throw new NotImplementedException("Gateway received command: " + command);
	}
	
	@Override
	public void onMessage(MoxMessage message) {
		for (MoxMessageListener listener : messageListeners) {
			listener.onMessage(message);
		}
	}

}
