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
import org.openhab.binding.mox.protocol.MoxStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
public class MoxModuleHandler extends BaseThingHandler implements
		MoxMessageListener {

	private Logger logger = LoggerFactory.getLogger(MoxModuleHandler.class);

	private MoxModuleConfig config;
	private MoxGatewayHandler gatewayHandler;
	private Map<MoxStatusCode, State> lastStates = new HashMap<MoxStatusCode, State>(
			MoxStatusCode.values().length);
	private MoxGatewaySendHandler sender;

	private static final ImmutableList<MoxCommandCode> initialRemValues = ImmutableList
			.of(MoxCommandCode.GET_POWER_ACTIVE,
					MoxCommandCode.GET_POWER_ACTIVE_ENERGY);

	private int refresh = 60; // refresh every minute as default
	ScheduledFuture<?> refreshJob;

	private int refreshRemInterval = 60 * 10; // 10 min
	ScheduledFuture<?> refreshRemValuesJob;

	boolean initialStateFetched = false;
	boolean fetchRemValues = true;

	public MoxModuleHandler(Thing thing) {
		super(thing);
		thing.setBridgeUID(new ThingUID("mox:gateway:221"));
	}

	@Override
	public void initialize() {
		logger.debug("Initializing Mox Device handler.");
		config = getConfigAs(MoxModuleConfig.class);
		if (config.oid > 0 && config.oid < 0xffffff) {
			// note: this call implicitly registers our handler as a listener on
			// the bridge
			if (getGatewayHandler() != null) {
				updateStatus(getBridge().getStatus());
			}
			sender = MoxGatewaySendHandler.getInstance(getBridge());
		}
		updateStatus(ThingStatus.INITIALIZING);
		deviceOnlineWatchdog();
	}

	private void queryInitialState(boolean force) {
		// if (!isLinked(STATE)) return;
		if (config == null)
			return;
		final ThingStatus status = getThing().getStatus();
		if (status == ThingStatus.OFFLINE)
			return;
		if (!(force || status == ThingStatus.INITIALIZING))
			return;

		MoxMessageBuilder msg = MoxMessageBuilder
				.messageBuilder(new MoxMessage()).withOid(config.oid)
				.withSuboid(0x11).withPriority(0x3);

		if (getThing().getThingTypeUID().equals(THING_TYPE_1G_DIMMER)) {
			msg.withCommandCode(MoxCommandCode.GET_LUMINOUS);
		} else {
			msg.withCommandCode(MoxCommandCode.GET_ONOFF);
		}

		logger.info("Query last state of item with OID {}.", config.oid);
		sender.sendMoxMessage(msg.toBytes());

		refreshRemValues();
	}

	private void refreshRemValues() {
		if (fetchRemValues) {
			MoxMessageBuilder msg = MoxMessageBuilder
					.messageBuilder(new MoxMessage()).withOid(config.oid)
					.withSuboid(0x11);
			for (MoxCommandCode code : initialRemValues) {
				sender.sendMoxMessage(msg.withCommandCode(code).toBytes());
			}
			addRemRefreshJob();
		}
	}

	private void deviceOnlineWatchdog() {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					MoxGatewayHandler bridgeHandler = getGatewayHandler();
					if (bridgeHandler != null) {
						if (bridgeHandler.getThing() != null) {
							queryInitialState(false);
							// Prevent unneccessary updates:
							if (bridgeHandler.getThing().getStatus() != getThing().getStatus()) {
								updateStatus(bridgeHandler.getThing().getStatus());
							}
							gatewayHandler = null;
						} else {
							updateStatus(ThingStatus.OFFLINE);
						}

					} else {
						logger.debug("MOX Gateway device not found.");
						updateStatus(ThingStatus.OFFLINE);
					}

				} catch (Exception e) {
					logger.debug("Exception occurred during execution: {}",
							e.getMessage(), e);
					gatewayHandler = null;
				}

			}
		};

		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh,
				TimeUnit.SECONDS);
	}

	private void addRemRefreshJob() {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					if (getThing().getStatus() == ThingStatus.ONLINE) {
						refreshRemValues();
					}
				} catch (Exception e) {
					logger.debug(
							"Exception occurred during fetch REM values: {}",
							e.getMessage(), e);
				}
			}
		};

		refreshRemValuesJob = scheduler.scheduleAtFixedRate(runnable,
				refreshRemInterval, refreshRemInterval, TimeUnit.MINUTES);
	}

	@Override
	public void dispose() {
		if (refreshJob != null && !refreshJob.isCancelled()) {
			refreshJob.cancel(true);
			refreshJob = null;
		}
		updateStatus(ThingStatus.OFFLINE);
		if (gatewayHandler != null) {
			getGatewayHandler().unregisterLightStatusListener(this);
		}
		gatewayHandler = null;
		logger.debug("Thing {} disposed.", getThing().getUID());
		super.dispose();
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
		logger.debug("Received update for {} with new state {}", channelUID,
				newState);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.debug("Received command for {} : {}", channelUID, command);
		if (channelUID.getId().equals(STATE)) {

			if (command instanceof OnOffType) {
				setSwitchValue(channelUID, command);
			} else if (command instanceof DecimalType
					|| command instanceof PercentType) {
				setDimmerValue(channelUID, command);
			} else {
				throw new IllegalArgumentException(
						"This type of command is not supported by the binding.");
			}

		}
	}

	private void setSwitchValue(ChannelUID channelUID, Command command) {
		if (command == null)
			return;

		byte[] messageAsByteArray = messageBuilder(new MoxMessage())
				.withOid(config.oid).withSuboid(config.suboid)
				.withCommandCode(MoxCommandCode.SET_ONOFF)
				.withValue(new BigDecimal(command == OnOffType.ON ? 1 : 0))
				.toBytes();
		sender.sendMoxMessage(messageAsByteArray);
	}

	private void setDimmerValue(ChannelUID channelUID, Command command) {
		if (command == null)
			return;

		MoxMessageBuilder message = messageBuilder(new MoxMessage()).withOid(
				config.oid).withSuboid(config.suboid);

		if (command instanceof PercentType || command instanceof DecimalType) {
			message.withCommandCode(MoxCommandCode.SET_LUMINOUS)
					.withValue(new BigDecimal(command.toString())).toBytes();
		} else if (command instanceof IncreaseDecreaseType) {
			MoxCommandCode moxCommand = (command == IncreaseDecreaseType.INCREASE) ? MoxCommandCode.INCREASE
					: MoxCommandCode.DECREASE;
			message.withCommandCode(moxCommand).withValue(new BigDecimal(5)); // TODO
																				// make
																				// amount
																				// of
																				// increase/decrease
																				// configurable
		}

		sender.sendMoxMessage(message.toBytes());
	}

	@Override
	public void onMessage(MoxMessage message) {
		final ThingUID uid = getThing().getUID();
		final ThingStatus status = getThing().getStatus();
		if (status == ThingStatus.INITIALIZING) {
			// Received message from this module, so it has to be online
			updateStatus(ThingStatus.ONLINE);
		}
		if (status == ThingStatus.OFFLINE) {
			logger.debug(
					"Not handline MoxGatewayMessage becuase thing {} is offline.",
					uid);
			return;
		}
		if (message != null && message.getStatusCode() != null) {
			final MoxStatusCode statusCode = message.getStatusCode();
			if (config.oid == message.getOid() && statusCode != null) {
				State state = null;
				String channelName = null;
				switch (statusCode) {
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

				case ONOFF_OR_LUMINOUS:
					channelName = MoxBindingConstants.STATE;
					int value = message.getValue()
							.setScale(0, RoundingMode.HALF_UP).intValue();
					if (value == 1 || value == 0) {
						state = value == 1 ? OnOffType.ON : OnOffType.OFF;
					} else {
						state = new PercentType(value);
					}
					break;

				case LUMINOUS:
					channelName = MoxBindingConstants.STATE;
					state = new PercentType(message.getValue().setScale(0,
							RoundingMode.HALF_UP));
					break;

				default:
					logger.trace(
							"No handling Gateway message with status code {}",
							statusCode);
				}

				if (channelName != null && state != null
						&& !state.equals(lastStates.get(statusCode))) {
					updateState(new ChannelUID(uid, channelName), state);
					lastStates.put(statusCode, state);
				}

			}
		}
	}

}
