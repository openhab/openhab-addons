/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.handler;

import com.google.common.collect.Sets;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mox.MoxBindingConstants;
import org.openhab.binding.mox.config.MoxModuleConfig;
import org.openhab.binding.mox.protocol.MoxCommandCode;
import org.openhab.binding.mox.protocol.MoxMessage;
import org.openhab.binding.mox.protocol.MoxMessageBuilder;
import org.openhab.binding.mox.protocol.MoxMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.openhab.binding.mox.MoxBindingConstants.*;

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

	private int refresh = 60; // refresh every minute as default
	ScheduledFuture<?> refreshJob;

	private final static Set<ThingTypeUID> SUPPORTS_DECIMAL_INPUT = Sets.newHashSet(THING_TYPE_1G_DIMMER, THING_TYPE_1G_FAN, THING_TYPE_1G_CURTAIN);
	private final static Set<ThingTypeUID> SUPPORTS_ONOFF_INPUT = Sets.newHashSet(THING_TYPE_1G_DIMMER, THING_TYPE_1G_FAN, THING_TYPE_1G_ONOFF);

	public MoxModuleHandler(Thing thing) {
		super(thing);
		thing.setBridgeUID(new ThingUID("mox:gateway:221"));
	}


	@Override
    public void initialize() {
        logger.debug("Initializing Mox Device handler.");
		MoxModuleConfig config = getConfigAs(MoxModuleConfig.class);
        if (isNotBlank(config.oid)) {
            this.oid = Integer.valueOf(config.oid);
            // note: this call implicitly registers our handler as a listener on the bridge
            if (getGatewayHandler() != null) {
                getThing().setStatus(getBridge().getStatus());
            }
        }
		updateStatus(ThingStatus.OFFLINE);
		deviceOnlineWatchdog();
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
		logger.debug("Received command for {} : {}", channelUID, command);
        if(channelUID.getId().equals(STATE)) {

			checkValidInputType(channelUID.getThingTypeUID(), command);

			if (command instanceof OnOffType) {
				setSwitchValue(channelUID, command);
			} else if (command instanceof DecimalType ||
					command instanceof IncreaseDecreaseType) {
				setDimmerValue(channelUID, command);
			} else {
				throw new IllegalArgumentException("This type of command is not supported by the binding.");
			}

        }
	}

	private void checkValidInputType(ThingTypeUID thingTypeUID, Command command) {
		String errorMessage = null;
		if (command instanceof OnOffType && !SUPPORTS_ONOFF_INPUT.contains(thingTypeUID)) {
			errorMessage = "This thing cannot handle ON/OFF";
		} else if (command instanceof DecimalType && !SUPPORTS_DECIMAL_INPUT.contains(thingTypeUID)) {
			errorMessage = "This thing cannot handle decimal state.";
		}
		if (errorMessage != null) {
			throw new IllegalArgumentException(errorMessage);
		}
	}

	private void setSwitchValue(ChannelUID channelUID, Command command) {
		if (command == null) return;
		int oid = Integer.parseInt(channelUID.getThingId());

		MoxMessage message = new MoxMessage();
		message.setPriority(0x4);
		message.setOid(oid);
		message.setSuboid(0x11); // TODO make this configurable in thing definition

		message.setCommandCode(MoxCommandCode.ONOFF);
		message.setValue(command==OnOffType.ON ? 1 : 0);

		sendMoxMessage(message);


	}

    private void sendMoxMessage(MoxMessage message) {
        MoxMessageBuilder messageBuilder = MoxMessageBuilder.messageBuilder(message);
        byte[] messageBytes = messageBuilder.toBytes();

        try {
            String targetHost = (String) getBridge().getConfiguration().get(UDP_HOST);
            InetAddress address = null;

            address = InetAddress.getByName(targetHost);

            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, 6670);
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packet);
        } catch (Exception e) {
            logger.error("Error sending UDP datagram: {}", e.getLocalizedMessage(), e);
        }
    }

	private void setDimmerValue(ChannelUID channelUID, Command command) {
		if (command == null) return;

		int oid = Integer.parseInt(channelUID.getThingId());

		MoxMessage message = new MoxMessage();
		message.setPriority(0x4);
		message.setOid(oid);
		message.setSuboid(0x11); // TODO make this configurable in thing definition

		if (command instanceof DecimalType) {
			message.setCommandCode(MoxCommandCode.LUMINOUS_SET);
			message.setValue(Integer.parseInt(command.toString()));
		} else if (command instanceof IncreaseDecreaseType) {
			message.setCommandCode(
					command == IncreaseDecreaseType.INCREASE ? MoxCommandCode.INCREASE : MoxCommandCode.DECREASE);
		}

		sendMoxMessage(message);

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
