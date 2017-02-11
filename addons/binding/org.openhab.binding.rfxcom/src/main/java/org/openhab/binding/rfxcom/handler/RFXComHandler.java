/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.DeviceMessageListener;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RFXComHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComHandler extends BaseThingHandler implements DeviceMessageListener {

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
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (bridgeHandler != null) {

            if (command instanceof RefreshType) {
                // Not supported

            } else {

                try {

                    PacketType packetType = RFXComMessageFactory
                            .convertPacketType(channelUID.getThingUID().getThingTypeId().toUpperCase());

                    RFXComMessage msg = RFXComMessageFactory.createMessage(packetType);

                    List<RFXComValueSelector> supportedValueSelectors = msg.getSupportedOutputValueSelectors();

                    RFXComValueSelector valSelector = RFXComValueSelector.getValueSelector(channelUID.getId());

                    if (supportedValueSelectors.contains(valSelector)) {
                        msg.setSubType(msg.convertSubType(config.subType));
                        msg.setDeviceId(config.deviceId);
                        msg.convertFromState(valSelector, command);

                        bridgeHandler.sendMessage(msg);
                    } else {
                        logger.warn("RFXCOM doesn't support transmitting for channel '{}'", channelUID.getId());
                    }

                } catch (RFXComMessageNotImplementedException e) {
                    logger.error("Message not supported", e);
                } catch (RFXComException e) {
                    logger.error("Transmitting error", e);
                }
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        initializeBridge((getBridge() == null) ? null : getBridge().getHandler(),
                (getBridge() == null) ? null : getBridge().getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        initializeBridge((getBridge() == null) ? null : getBridge().getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeBridge(ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        config = getConfigAs(RFXComDeviceConfiguration.class);
        if (config.deviceId == null || config.subType == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "RFXCOM device missing deviceId or subType");
        } else if (thingHandler != null && bridgeStatus != null) {

            bridgeHandler = (RFXComBridgeHandler) thingHandler;
            bridgeHandler.registerDeviceStatusListener(this);

            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
        if (bridgeHandler != null) {
            bridgeHandler.unregisterDeviceStatusListener(this);
        }
        bridgeHandler = null;
        super.dispose();
    }

    @Override
    public void onDeviceMessageReceived(ThingUID bridge, RFXComMessage message) {
        try {
            String id = message.getDeviceId();
            if (config.deviceId.equals(id)) {
                RFXComBaseMessage msg = (RFXComBaseMessage) message;
                String receivedId = packetTypeThingMap.get(msg.packetType).getId();
                logger.debug("Received message from bridge: {} message: {}", bridge, message);

                if (receivedId.equals(getThing().getThingTypeUID().getId())) {
                    updateStatus(ThingStatus.ONLINE);

                    List<RFXComValueSelector> supportedValueSelectors = msg.getSupportedInputValueSelectors();

                    if (supportedValueSelectors != null) {
                        for (RFXComValueSelector valueSelector : supportedValueSelectors) {
                            switch (valueSelector) {
                                case BATTERY_LEVEL:
                                    updateState(CHANNEL_BATTERY_LEVEL, convertBatteryLevelToSystemWideLevel(
                                            message.convertToState(valueSelector)));
                                    break;
                                case CHILL_FACTOR:
                                    updateState(CHANNEL_CHILL_FACTOR, message.convertToState(valueSelector));
                                    break;
                                case COMMAND:
                                    updateState(CHANNEL_COMMAND, message.convertToState(valueSelector));
                                    break;
                                case CONTACT:
                                    updateState(CHANNEL_CONTACT, message.convertToState(valueSelector));
                                    break;
                                case DIMMING_LEVEL:
                                    updateState(CHANNEL_DIMMING_LEVEL, message.convertToState(valueSelector));
                                    break;
                                case FORECAST:
                                    updateState(CHANNEL_FORECAST, message.convertToState(valueSelector));
                                    break;
                                case GUST:
                                    updateState(CHANNEL_GUST, message.convertToState(valueSelector));
                                    break;
                                case HUMIDITY:
                                    updateState(CHANNEL_HUMIDITY, message.convertToState(valueSelector));
                                    break;
                                case HUMIDITY_STATUS:
                                    updateState(CHANNEL_HUMIDITY_STATUS, message.convertToState(valueSelector));
                                    break;
                                case INSTANT_AMPS:
                                    updateState(CHANNEL_INSTANT_AMPS, message.convertToState(valueSelector));
                                    break;
                                case INSTANT_POWER:
                                    updateState(CHANNEL_INSTANT_POWER, message.convertToState(valueSelector));
                                    break;
                                case LOW_BATTERY:
                                    updateState(CHANNEL_BATTERY_LEVEL,
                                            isLowBattery(message.convertToState(valueSelector)));
                                    break;

                                case MOOD:
                                    updateState(CHANNEL_MOOD, message.convertToState(valueSelector));
                                    break;
                                case MOTION:
                                    updateState(CHANNEL_MOTION, message.convertToState(valueSelector));
                                    break;
                                case PRESSURE:
                                    updateState(CHANNEL_PRESSURE, message.convertToState(valueSelector));
                                    break;
                                case RAIN_RATE:
                                    updateState(CHANNEL_RAIN_RATE, message.convertToState(valueSelector));
                                    break;
                                case RAIN_TOTAL:
                                    updateState(CHANNEL_RAIN_TOTAL, message.convertToState(valueSelector));
                                    break;
                                case RAW_MESSAGE:
                                    updateState(CHANNEL_RAW_MESSAGE, message.convertToState(valueSelector));
                                    break;
                                case RAW_PAYLOAD:
                                    updateState(CHANNEL_RAW_PAYLOAD, message.convertToState(valueSelector));
                                    break;
                                case SET_POINT:
                                    updateState(CHANNEL_SET_POINT, message.convertToState(valueSelector));
                                    break;
                                case SHUTTER:
                                    updateState(CHANNEL_SHUTTER, message.convertToState(valueSelector));
                                    break;
                                case SIGNAL_LEVEL:
                                    updateState(CHANNEL_SIGNAL_LEVEL,
                                            convertSignalLevelToSystemWideLevel(message.convertToState(valueSelector)));
                                    break;
                                case STATUS:
                                    updateState(CHANNEL_STATUS, message.convertToState(valueSelector));
                                    break;
                                case TEMPERATURE:
                                    updateState(CHANNEL_TEMPERATURE, message.convertToState(valueSelector));
                                    break;
                                case TOTAL_AMP_HOUR:
                                    updateState(CHANNEL_TOTAL_AMP_HOUR, message.convertToState(valueSelector));
                                    break;
                                case TOTAL_USAGE:
                                    updateState(CHANNEL_TOTAL_USAGE, message.convertToState(valueSelector));
                                    break;
                                case VOLTAGE:
                                    updateState(CHANNEL_VOLTAGE, message.convertToState(valueSelector));
                                    break;
                                case WIND_DIRECTION:
                                    updateState(CHANNEL_WIND_DIRECTION, message.convertToState(valueSelector));
                                    break;
                                case WIND_SPEED:
                                    updateState(CHANNEL_WIND_SPEED, message.convertToState(valueSelector));
                                    break;
                                default:
                                    logger.debug("Unsupported value selector '{}'", valueSelector);
                                    break;
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            logger.error("Error occured during message receiving", e);
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
        int level = ((DecimalType) batteryLevel).intValue();
        if (level <= LOW_BATTERY_LEVEL) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }
}
