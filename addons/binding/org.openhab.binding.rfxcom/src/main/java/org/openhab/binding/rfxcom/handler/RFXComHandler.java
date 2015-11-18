/**
 * Copyright (c) 2010-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.handler;

import static org.openhab.binding.rfxcom.RFXComBindingConstants.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.DeviceMessageListener;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComNotImpException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessageFactory;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RFXComHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComHandler extends BaseThingHandler implements
		DeviceMessageListener {

	private Logger logger = LoggerFactory.getLogger(RFXComHandler.class);

	private final int LOW_BATTERY_LEVEL = 1;
	
	ScheduledFuture<?> refreshJob;
	private RFXComBridgeHandler bridgeHandler;

	private RFXComDeviceConfiguration config;

	public RFXComHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.debug("Received channel: {}, command: {}", channelUID,
				command);

		if (bridgeHandler != null) {

			try {
				PacketType packetType = RFXComMessageFactory
						.convertPacketType(channelUID.getThingTypeId()
								.toUpperCase());

				RFXComMessage msg = RFXComMessageFactory
						.createMessage(packetType);

				List<RFXComValueSelector> supportedValueSelectors = msg
						.getSupportedOutputValueSelectors();

				RFXComValueSelector valSelector = RFXComValueSelector
						.getValueSelector(channelUID.getId());

				if (supportedValueSelectors.contains(valSelector)) {
					msg.setSubType(msg.convertSubType(config.subType));
					msg.setDeviceId(config.deviceId);
					msg.convertFromState(valSelector, command);

					bridgeHandler.sendMessage(msg);
				} else {
					logger.warn(
							"RFXCOM doesn't support transmitting for channel '{}'",
							channelUID.getId());
				}

			} catch (RFXComNotImpException e) {
				logger.error("Message not supported", e.getMessage());
			} catch (RFXComException e) {
				logger.error("Transmitting error", e.getMessage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		config = getConfigAs(RFXComDeviceConfiguration.class);

		logger.debug(
				"Initialized RFXCOM device handler for {}, deviceId={}, subType={}",
				getThing().getUID(), config.deviceId, config.subType);
	}

	@Override
	protected void bridgeHandlerInitialized(ThingHandler thingHandler,
			Bridge bridge) {

		logger.debug("Bridge initialized");

		if (thingHandler != null && bridge != null) {
			bridgeHandler = (RFXComBridgeHandler) thingHandler;
			bridgeHandler.registerDeviceStatusListener(this);

			if (bridge.getStatus() == ThingStatus.ONLINE) {
				updateStatus(ThingStatus.ONLINE);
			} else {
				updateStatus(ThingStatus.OFFLINE,
						ThingStatusDetail.BRIDGE_OFFLINE);
			}
		}

		super.bridgeHandlerInitialized(thingHandler, bridge);
	}

	@Override
	protected void bridgeHandlerDisposed(ThingHandler thingHandler,
			Bridge bridge) {
		logger.debug("Bridge disposed");
		if (bridgeHandler != null) {
			bridgeHandler.unregisterDeviceStatusListener(this);
		}
		bridgeHandler = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
	 */
	@Override
	public void dispose() {
		logger.debug("Thing {} disposed.", getThing().getUID());
		super.dispose();
	}

	@Override
	public void onDeviceMessageReceived(ThingUID bridge, RFXComMessage message) {
		try {
			String id = message.getDeviceId();
			if (config.deviceId.equals(id)) {
				RFXComBaseMessage msg = (RFXComBaseMessage) message;
				String receivedId = packetTypeThingMap.get(msg.packetType)
						.getId();

				if (receivedId.equals(getThing().getUID().getThingTypeId())) {
					updateStatus(ThingStatus.ONLINE);
					logger.debug(
							"Received message from bridge: {} message: {}",
							bridge, message);

					List<RFXComValueSelector> supportedValueSelectors = msg
							.getSupportedInputValueSelectors();

					if (supportedValueSelectors != null) {
						for (RFXComValueSelector valueSelector : supportedValueSelectors) {
							switch (valueSelector) {
							case BATTERY_LEVEL:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_BATTERY_LEVEL),
										convertBatteryLevelToSystemWideLevel(message
												.convertToState(valueSelector)));
								break;
							case CHILL_FACTOR:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_CHILL_FACTOR),
										message.convertToState(valueSelector));
								break;
							case COMMAND:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_COMMAND),
										message.convertToState(valueSelector));
								break;
							case CONTACT:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_CONTACT),
										message.convertToState(valueSelector));
								break;
							case DIMMING_LEVEL:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_DIMMING_LEVEL),
										message.convertToState(valueSelector));
								break;
							case FORECAST:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_FORECAST),
										message.convertToState(valueSelector));
								break;
							case GUST:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_GUST),
										message.convertToState(valueSelector));
								break;
							case HUMIDITY:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_HUMIDITY),
										message.convertToState(valueSelector));
								break;
							case HUMIDITY_STATUS:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_HUMIDITY_STATUS),
										message.convertToState(valueSelector));
								break;
							case INSTANT_AMPS:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_INSTANT_AMPS),
										message.convertToState(valueSelector));
								break;
							case INSTANT_POWER:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_INSTANT_POWER),
										message.convertToState(valueSelector));
								break;
							case LOW_BATTERY:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_BATTERY_LEVEL),
										isLowBattery(message
												.convertToState(valueSelector)));
								break;
								
							case MOOD:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_MOOD),
										message.convertToState(valueSelector));
								break;
							case MOTION:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_MOTION),
										message.convertToState(valueSelector));
								break;
							case PRESSURE:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_PRESSURE),
										message.convertToState(valueSelector));
								break;
							case RAIN_RATE:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_RAIN_RATE),
										message.convertToState(valueSelector));
								break;
							case RAIN_TOTAL:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_RAIN_TOTAL),
										message.convertToState(valueSelector));
								break;
							case SET_POINT:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_SET_POINT),
										message.convertToState(valueSelector));
								break;
							case SHUTTER:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_SHUTTER),
										message.convertToState(valueSelector));
								break;
							case SIGNAL_LEVEL:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_SIGNAL_LEVEL),
										convertSignalLevelToSystemWideLevel(message
												.convertToState(valueSelector)));
								break;
							case STATUS:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_STATUS),
										message.convertToState(valueSelector));
								break;
							case TEMPERATURE:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_TEMPERATURE),
										message.convertToState(valueSelector));
								break;
							case TOTAL_AMP_HOURS:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_TOTAL_AMP_HOURS),
										message.convertToState(valueSelector));
								break;
							case TOTAL_USAGE:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_TOTAL_USAGE),
										message.convertToState(valueSelector));
								break;
							case VOLTAGE:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_VOLTAGE),
										message.convertToState(valueSelector));
								break;
							case WIND_DIRECTION:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_WIND_DIRECTION),
										message.convertToState(valueSelector));
								break;
							case WIND_SPEED:
								updateState(new ChannelUID(getThing().getUID(),
										CHANNEL_WIND_SPEED),
										message.convertToState(valueSelector));
								break;
							default:
								logger.debug("Unsupported value selector '{}'",
										valueSelector);
								break;
							}
						}
					}

				}
			}
		} catch (Exception e) {
			logger.error("Error occured during message receiving: ", e.getMessage());
		}
	}
	
	/**
	 * Convert internal signal level (0-15) to system wide signal level (0-4).
	 * 
	 * @param signalLevel Internal signal level
	 * @return Signal level in system wide level
	 */
	private State convertSignalLevelToSystemWideLevel(State signalLevel) {
		
		int level = ((DecimalType) signalLevel).intValue();
		int newLevel = 0;

		/*
		 * RFXCOM signal levels are always between 0-15.
		 * 
		 * Use switch case to make level adaption easier in future if needed.
		 * 
		 * BigDecimal level =
		 * ((DecimalType)signalLevel).toBigDecimal().divide(new BigDecimal(4));
		 * return new DecimalType(level.setScale(0, RoundingMode.HALF_UP));
		 */

		switch (level) {
		case 0:
		case 1:
			newLevel = 0;
			break;

		case 2:
		case 3:
		case 4:
			newLevel = 1;
			break;

		case 5:
		case 6:
		case 7:
			newLevel = 2;
			break;

		case 8:
		case 9:
		case 10:
		case 11:
			newLevel = 3;
			break;

		case 12:
		case 13:
		case 14:
		case 15:
		default:
			newLevel = 4;
		}

		return new DecimalType(newLevel);
	}

	/**
	 * Convert internal battery level (0-9) to system wide battery level (0-100%).
	 * 
	 * @param batteryLevel Internal battery level
	 * @return Battery level in system wide level
	 */
	private State convertBatteryLevelToSystemWideLevel(State batteryLevel) {
		
		/*
		 * RFXCOM signal levels are always between 0-9.
		 * 
		 */
		int level = ((DecimalType) batteryLevel).intValue();
		level = (level + 1) * 10;
		return new DecimalType(level);
	}

	/**
	 * Check if battery level is below low battery threshold level.
	 * 
	 * @param batteryLevel Internal battery level
	 * @return OnOffType
	 */
	private State isLowBattery(State batteryLevel) {
		int level = ((DecimalType)batteryLevel).intValue();
		if (level <= LOW_BATTERY_LEVEL) {
			return OnOffType.ON; 
		} else {
			return OnOffType.OFF;
		}
	}
}
