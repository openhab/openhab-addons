/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.rfxcom.internal.handler;

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.rfxcom.internal.DeviceMessageListener;
import org.openhab.binding.rfxcom.internal.RFXComBindingConstants;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidParameterException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.binding.rfxcom.internal.messages.RFXComDeviceMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessageFactory;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessageFactoryImpl;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RFXComHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComHandler extends BaseThingHandler implements DeviceMessageListener, DeviceState {
    private static final int LOW_BATTERY_LEVEL = 1;

    private final Logger logger = LoggerFactory.getLogger(RFXComHandler.class);

    private final Map<String, Type> stateMap = new ConcurrentHashMap<>();

    private RFXComBridgeHandler bridgeHandler;

    private Class<? extends RFXComDeviceConfiguration> configType;
    private RFXComDeviceConfiguration config;

    private RFXComMessageFactory messageFactory;

    public RFXComHandler(@NonNull Thing thing) {
        this(thing, RFXComMessageFactoryImpl.INSTANCE);
    }

    public RFXComHandler(@NonNull Thing thing, RFXComMessageFactory messageFactory) {
        super(thing);
        this.messageFactory = messageFactory;

        configType = RFXComBindingConstants.THING_TYPE_UID_CONFIGURATION_CLASS_MAP.getOrDefault(thing.getThingTypeUID(),
                RFXComGenericDeviceConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (bridgeHandler != null) {
            if (command instanceof RefreshType) {
                logger.trace("Received unsupported Refresh command");
            } else {
                try {
                    PacketType packetType = RFXComMessageFactoryImpl
                            .convertPacketType(getThing().getThingTypeUID().getId().toUpperCase());

                    RFXComMessage msg = messageFactory.createMessage(packetType, config, channelUID, command);

                    bridgeHandler.sendMessage(msg);
                } catch (RFXComMessageNotImplementedException e) {
                    logger.error("Message not supported", e);
                } catch (RFXComUnsupportedChannelException e) {
                    logger.error("Channel not supported", e);
                } catch (RFXComInvalidStateException e) {
                    logger.error("Invalid state supplied for channel", e);
                } catch (RFXComException e) {
                    logger.error("Transmitting error", e);
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());

        Bridge bridge = getBridge();

        if (bridge == null) {
            initializeBridge(null, null);
        } else {
            initializeBridge(bridge.getHandler(), bridge.getStatus());
        }

        stateMap.clear();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());

        Bridge bridge = getBridge();

        if (bridge == null) {
            initializeBridge(null, bridgeStatusInfo.getStatus());
        } else {
            initializeBridge(bridge.getHandler(), bridgeStatusInfo.getStatus());
        }
    }

    private void initializeBridge(ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        try {
            config = getConfigAs(configType);
            config.parseAndValidate();
            if (thingHandler != null && bridgeStatus != null) {
                bridgeHandler = (RFXComBridgeHandler) thingHandler;
                bridgeHandler.registerDeviceStatusListener(this);

                if (bridgeStatus == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        } catch (RFXComInvalidParameterException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
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
    public void onDeviceMessageReceived(ThingUID bridge, RFXComDeviceMessage message) {
        try {
            if (config.matchesMessage(message)) {
                String receivedId = PACKET_TYPE_THING_TYPE_UID_MAP.get(message.getPacketType()).getId();
                if (receivedId.equals(getThing().getThingTypeUID().getId())) {
                    logger.debug("Message from bridge [{}] matches thing [{}] message: {}", bridge,
                            getThing().getUID().toString(), message);
                    updateStatus(ThingStatus.ONLINE);

                    for (Channel channel : getThing().getChannels()) {
                        ChannelUID uid = channel.getUID();
                        String channelId = uid.getId();

                        try {
                            switch (channelId) {
                                case CHANNEL_COMMAND:
                                case CHANNEL_CHIME_SOUND:
                                case CHANNEL_MOOD:
                                    postNullableCommand(uid, message.convertToCommand(channelId, config, this));
                                    break;

                                case CHANNEL_LOW_BATTERY:
                                    updateNullableState(uid,
                                            isLowBattery(message.convertToState(CHANNEL_BATTERY_LEVEL, config, this)));
                                    break;

                                default:
                                    updateNullableState(uid, message.convertToState(channelId, config, this));
                                    break;
                            }
                        } catch (RFXComInvalidStateException e) {
                            logger.trace("{} not configured for {}", channelId, message);
                        } catch (RFXComUnsupportedChannelException e) {
                            logger.trace("{} does not handle {}", channelId, message);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred during message receiving", e);
        }
    }

    private void updateNullableState(ChannelUID uid, State state) {
        if (state == null) {
            return;
        }

        stateMap.put(uid.getId(), state);
        updateState(uid, state);
    }

    private void postNullableCommand(ChannelUID uid, Command command) {
        if (command == null) {
            return;
        }

        stateMap.put(uid.getId(), command);
        postCommand(uid, command);
    }

    @Override
    public Type getLastState(String channelId) {
        return stateMap.get(channelId);
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
