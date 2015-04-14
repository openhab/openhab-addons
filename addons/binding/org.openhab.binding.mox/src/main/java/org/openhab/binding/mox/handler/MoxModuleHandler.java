/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.handler;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.openhab.binding.mox.MoxBindingConstants.OID;
import static org.openhab.binding.mox.MoxBindingConstants.STATE;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mox.MoxBindingConstants;
import org.openhab.binding.mox.protocol.MoxMessage;
import org.openhab.binding.mox.protocol.MoxMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MoxModuleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Thomas Eichstaedt-Engelen (innoQ) - Initial contribution
 * @since 2.0.0
 */
public class MoxModuleHandler extends BaseThingHandler implements MoxMessageListener {

    private Logger logger = LoggerFactory.getLogger(MoxModuleHandler.class);
    
    private int oid;
    private MoxGatewayHandler gatewayHandler;
    

	public MoxModuleHandler(Thing thing) {
		super(thing);
		thing.setBridgeUID(new ThingUID("mox:gateway:221"));
	}

    @Override
    public void initialize() {
        logger.debug("Initializing Mox Device handler.");
        final String configOid = (String) getConfig().get(OID);
        if (isNotBlank(configOid)) {
            this.oid = Integer.valueOf(configOid);
            // note: this call implicitly registers our handler as a listener on the bridge
            if (getGatewayHandler() != null) {
                getThing().setStatus(getBridge().getStatus());
            }
        }
    }
    
    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        MoxGatewayHandler gatewayHandler = getGatewayHandler();
        if (gatewayHandler != null) {
        	getGatewayHandler().unregisterLightStatusListener(this);
        }
    }

    private synchronized MoxGatewayHandler getGatewayHandler() {
        if (this.gatewayHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof MoxGatewayHandler) {
                this.gatewayHandler = (MoxGatewayHandler) handler;
                this.gatewayHandler.registerMessageListener(this);
            } else {
                return null;
            }
        }
        return this.gatewayHandler;
    }
    

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
        if(channelUID.getId().equals(STATE)) {
            // TODO: handle command
        }
	}	
	
	private void sendCommand(int oid) {
		
		/*
		function sendBusCommand(id, cmd, value) {
			var idParts = id.split(':');
			var oid = idParts[0];
			var suboid = idParts[1];
			var cmdCodes = CMD_CODES[cmd];
			value = parseInt(value, 10);
			var message = new Buffer([
				0x3, // priority
				Math.floor(oid/512),
				Math.floor(oid/256),
				oid%256,
				suboid,
				cmdCodes[0],
				0x0,
				0x0,
				Math.floor(cmdCodes[1]/256),
				cmdCodes[1]%256,
				value
			]);			
		}*/
		
	}
	
	@Override
	public void onMessage(MoxMessage message) {
		if (message != null && message.getCommandCode() != null) {
			if (oid == message.getOid()) {
				final ThingUID uid = getThing().getUID();
				final DecimalType state = new DecimalType(message.getValue());
				switch (message.getCommandCode()) {
					case POWER_ACTIVE:
						updateState(new ChannelUID(uid, MoxBindingConstants.CHANNEL_ACTIVE_POWER), state);
						break;
					case POWER_REACTIVE:
						updateState(new ChannelUID(uid, MoxBindingConstants.CHANNEL_REACTIVE_POWER), state);
						break;
					case POWER_APPARENT:
						updateState(new ChannelUID(uid, MoxBindingConstants.CHANNEL_APPARENT_POWER), state);
						break;
					case POWER_ACTIVE_ENERGY:
						updateState(new ChannelUID(uid, MoxBindingConstants.CHANNEL_ACTIVE_ENERGY), state);
						break;
					case POWER_FACTOR:
						updateState(new ChannelUID(uid, MoxBindingConstants.CHANNEL_POWER_FACTOR), state);
						break;
					default:
				}
			}
		}
	}

	
}
