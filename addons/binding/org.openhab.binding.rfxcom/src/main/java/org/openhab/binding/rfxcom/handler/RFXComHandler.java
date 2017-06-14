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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
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
    private static final int LOW_BATTERY_LEVEL = 1;

    private final Logger logger = LoggerFactory.getLogger(RFXComHandler.class);

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
                logger.trace("Received unsupported Refresh command");
            } else {

                try {

                    PacketType packetType = RFXComMessageFactory
                            .convertPacketType(channelUID.getThingUID().getThingTypeId().toUpperCase());

                    RFXComMessage msg = RFXComMessageFactory.createMessage(packetType);

                    msg.setConfig(config);
                    msg.convertFromState(channelUID.getId(), command);

                    bridgeHandler.sendMessage(msg);
                } catch (RFXComMessageNotImplementedException e) {
                    logger.error("Message not supported", e);
                } catch (RFXComException e) {
                    logger.error("Transmitting error", e);
                }
            }

        }
    }

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
                String receivedId = PACKET_TYPE_THING_TYPE_UID_MAP.get(msg.packetType).getId();
                logger.debug("Received message from bridge: {} message: {}", bridge, message);

                if (receivedId.equals(getThing().getThingTypeUID().getId())) {
                    updateStatus(ThingStatus.ONLINE);

                    for (Channel channel : getThing().getChannels()) {
                        String channelId = channel.getUID().getId();

                        try {
                            switch (channelId) {
                                case CHANNEL_BATTERY_LEVEL:
                                    updateState(channelId, convertBatteryLevelToSystemWideLevel(
                                            message.convertToState(channelId)));
                                    break;

                                case CHANNEL_LOW_BATTERY:
                                    updateState(channelId,
                                            isLowBattery(message.convertToState(channelId)));
                                    break;

                                case CHANNEL_SIGNAL_LEVEL:
                                    updateState(channelId,
                                            convertSignalLevelToSystemWideLevel(message.convertToState(channelId)));
                                    break;

                                    default:
                                        updateState(channelId, message.convertToState(channelId));
                                        break;
                                }
                            } catch (RFXComException e) {
                                logger.trace("{} does not handle {}", channelId, message);

                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred during message receiving", e);
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
        int newLevel;

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
