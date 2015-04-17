/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mox.MoxBindingConstants;
import org.openhab.binding.mox.config.MoxModuleConfig;
import org.openhab.binding.mox.protocol.MoxCommandCode;
import org.openhab.binding.mox.protocol.MoxMessage;
import org.openhab.binding.mox.protocol.MoxMessageBuilder;
import org.openhab.binding.mox.protocol.MoxMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.mox.MoxBindingConstants.*;
import static org.openhab.binding.mox.protocol.MoxMessageBuilder.messageBuilder;

/**
 * The {@link MoxModuleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Eichstaedt-Engelen (innoQ) - Initial contribution
 * @since 2.0.0
 */
public class MoxModuleHandler extends BaseThingHandler implements MoxMessageListener {

	protected static final int DEFAULT_PRIORITY = 0x4;
	public static final int TARGET_PORT = 6670;
	private Logger logger = LoggerFactory.getLogger(MoxModuleHandler.class);

    private MoxModuleConfig config;
    private MoxGatewayHandler gatewayHandler;
    private State lastState;

	private int refresh = 60; // refresh every minute as default
	ScheduledFuture<?> refreshJob;

	public MoxModuleHandler(Thing thing) {
		super(thing);
		thing.setBridgeUID(new ThingUID("mox:gateway:221"));
	}


	@Override
    public void initialize() {
        logger.debug("Initializing Mox Device handler.");
		config = getConfigAs(MoxModuleConfig.class);
        if (config.oid > 0 && config.oid < 0xffffff) {
            // note: this call implicitly registers our handler as a listener on the bridge
            if (getGatewayHandler() != null) {
                getThing().setStatus(getBridge().getStatus());
                queryInitialState(config.oid);
            }
        }
		updateStatus(ThingStatus.OFFLINE);
		deviceOnlineWatchdog();
    }

	private void queryInitialState(int oid) {
		if (!isLinked(STATE)) return;
		
		MoxMessageBuilder msg = MoxMessageBuilder.messageBuilder(new MoxMessage()).withOid(oid)
				.withSuboid(0x11).withPriority(0x3);
		
		if (getThing().getThingTypeUID().equals(THING_TYPE_1G_DIMMER)) {
			msg.withCommandCode(MoxCommandCode.GET_LUMINOUS);
		} else {
			msg.withCommandCode(MoxCommandCode.GET_STATUS);
		}
				
		logger.info("Query last state of item with OID {}.", oid);
		sendMoxMessage(msg.toBytes());
	}


	private void deviceOnlineWatchdog() {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					MoxGatewayHandler bridgeHandler = getGatewayHandler();
					if (bridgeHandler != null) {
						if (bridgeHandler.getThing() != null) {
							updateStatus(bridgeHandler.getThing().getStatus());
							gatewayHandler = null;
						} else {
							updateStatus(ThingStatus.ONLINE);
						}

					} else {
						logger.debug("MOX Gateway device not found.");
						updateStatus(ThingStatus.OFFLINE);
					}

				} catch (Exception e) {
					logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
					gatewayHandler = null;
				}

			}
		};

		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh, TimeUnit.SECONDS);
	}

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        refreshJob.cancel(true);
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
    public void handleUpdate(ChannelUID channelUID, State newState) {
    	logger.debug("Received update for {} with new state {}", channelUID, newState);
    }


	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.debug("Received command for {} : {}", channelUID, command);
        if(channelUID.getId().equals(STATE)) {

			if (command instanceof OnOffType) {
				setSwitchValue(channelUID, command);
			} else if (command instanceof DecimalType ||
					command instanceof PercentType) {
				setDimmerValue(channelUID, command);
			} else {
				throw new IllegalArgumentException("This type of command is not supported by the binding.");
			}

        }
	}

	private void setSwitchValue(ChannelUID channelUID, Command command) {
		if (command == null) return;

        byte[] messageAsByteArray = messageBuilder(new MoxMessage())
        		.withOid(config.oid).withSuboid(config.suboid).withPriority(DEFAULT_PRIORITY)
        		.withCommandCode(MoxCommandCode.SET_ONOFF)
        		.withValue(new BigDecimal(command==OnOffType.ON ? 1 : 0)).toBytes();
		sendMoxMessage(messageAsByteArray);
	}

	private void setDimmerValue(ChannelUID channelUID, Command command) {
		if (command == null) return;

        MoxMessageBuilder message = messageBuilder(new MoxMessage())
	    		.withOid(config.oid).withSuboid(config.suboid).withPriority(DEFAULT_PRIORITY);

		if (command instanceof PercentType || command instanceof DecimalType) {
			message.withCommandCode(MoxCommandCode.SET_LUMINOUS).withValue(new BigDecimal(command.toString())).toBytes();
		} else if (command instanceof IncreaseDecreaseType) {
			MoxCommandCode moxCommand = 
				(command == IncreaseDecreaseType.INCREASE) ? MoxCommandCode.INCREASE : MoxCommandCode.DECREASE;
			message.withCommandCode(moxCommand).withValue(new BigDecimal(5)); // TODO make amount of increase/decrease configurable
		}
		
		sendMoxMessage(message.toBytes());
	}
	
    private void sendMoxMessage(byte[] messageBytes) {
    	if (messageBytes == null) throw new IllegalArgumentException("Bytes to send were null.");
		DatagramSocket datagramSocket = null;
        try {
            String targetHost = (String) getBridge().getConfiguration().get(UDP_HOST);
            InetAddress address = InetAddress.getByName(targetHost);

			if (logger.isTraceEnabled()) {
				logger.trace("Sending bytes to host {} : {}", address, MoxMessageBuilder.getUnsignedIntArray(messageBytes));
			}

            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, TARGET_PORT);
			datagramSocket = new DatagramSocket();
            datagramSocket.send(packet);
        } catch (Exception e) {
            logger.error("Error sending UDP datagram: {}", e.getLocalizedMessage(), e);
        } finally {
			if (datagramSocket != null) {
				datagramSocket.close();
			}
		}
	}

    
	@Override
	public void onMessage(MoxMessage message) {
		if (message != null && message.getCommandCode() != null) {
			if (config.oid == message.getOid() && message.getStatusCode() != null) {
				final ThingUID uid = getThing().getUID();
				State state = null;
				String channelName = null;
				switch (message.getStatusCode()) {
					case POWER_ACTIVE:
						channelName = MoxBindingConstants.CHANNEL_ACTIVE_POWER;
						state = new DecimalType(message.getValue());
						break;
					case POWER_REACTIVE:
						channelName = MoxBindingConstants.CHANNEL_REACTIVE_POWER;
						state = new DecimalType(message.getValue());
						break;
					case POWER_APPARENT:
						channelName = MoxBindingConstants.CHANNEL_APPARENT_POWER;
						state = new DecimalType(message.getValue());
						break;
					case POWER_ACTIVE_ENERGY:
						channelName = MoxBindingConstants.CHANNEL_ACTIVE_ENERGY;
						state = new DecimalType(message.getValue());
						break;
					case POWER_FACTOR:
						channelName = MoxBindingConstants.CHANNEL_POWER_FACTOR;
						state = new DecimalType(message.getValue());
						break;
						
					case LUMINOUS:
						channelName = MoxBindingConstants.STATE;
						state = new PercentType(message.getValue().setScale(0, RoundingMode.HALF_UP));
						break;
						
					case STATUS:
						channelName = MoxBindingConstants.STATE;
						state = message.getValue().intValue() >= 1 ? OnOffType.ON : OnOffType.OFF;
						break;
						
					default:
						logger.trace("No handling Gateway message with status code {}", message.getStatusCode());
				}
				
				if (channelName != null && state != lastState) {
					updateState(new ChannelUID(uid, channelName), state);
					lastState = state;
				}				

			}
		}
	}


}
