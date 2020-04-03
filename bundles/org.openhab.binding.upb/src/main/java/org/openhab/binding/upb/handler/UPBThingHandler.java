/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.upb.handler;

import static org.openhab.binding.upb.internal.message.Command.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.upb.Constants;
import org.openhab.binding.upb.UPBDevice;
import org.openhab.binding.upb.handler.UPBIoHandler.CmdStatus;
import org.openhab.binding.upb.internal.message.MessageBuilder;
import org.openhab.binding.upb.internal.message.UPBMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for things representing devices on an UPB network.
 *
 * @author Marcus Better - Initial contribution
 *
 */
@NonNullByDefault
public class UPBThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(UPBThingHandler.class);

    @Nullable
    private Byte defaultNetworkId;
    @Nullable
    protected volatile PIMHandler controllerHandler;
    protected volatile byte networkId;
    protected volatile int unitId;

    public UPBThingHandler(final Thing device, final @Nullable Byte defaultNetworkId) {
        super(device);
        this.defaultNetworkId = defaultNetworkId;
    }

    @Override
    public void initialize() {
        logger.debug("initializing UPB thing handler {}", getThing().getUID());

        final BigDecimal val = (BigDecimal) getConfig().get(Constants.CONFIGURATION_NETWORK_ID);
        if (val == null) {
            // use value from binding config
            if (defaultNetworkId == null) {
                logger.warn("missing network ID for {}", getThing().getUID());
                return;
            }
            networkId = defaultNetworkId;
        } else if (val.compareTo(BigDecimal.ZERO) < 0 || val.compareTo(BigDecimal.valueOf(255)) > 0) {
            logger.warn("invalid network ID {} for {}", val, getThing().getUID());
            return;
        } else {
            networkId = val.byteValue();
        }

        final BigDecimal cfgUnitId = (BigDecimal) getConfig().get(Constants.CONFIGURATION_UNIT_ID);
        if (cfgUnitId == null) {
            logger.warn("Unit ID is not set in {}", getThing().getUID());
            return;
        }
        unitId = cfgUnitId.intValue();
        if (unitId < 1 || unitId > 250) {
            logger.warn("Unit ID ({}) out of range for {}", cfgUnitId, getThing().getUID());
            return;
        }

        final Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, Constants.OFFLINE_CTLR_OFFLINE);
            return;
        }
        bridgeStatusChanged(bridge.getStatusInfo());
    }

    @Override
    public void dispose() {

    }

    @Override
    public void bridgeStatusChanged(final ThingStatusInfo bridgeStatusInfo) {
        logger.debug("DEV {}: Controller status is {}", unitId, bridgeStatusInfo.getStatus());

        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, Constants.OFFLINE_CTLR_OFFLINE);
            return;
        }

        logger.debug("DEV {}: Controller is ONLINE. Starting device initialisation.", unitId);

        final Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("DEV {}: bridge is null!", unitId);
            return;
        }
        final PIMHandler bridgeHandler = (PIMHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            logger.debug("DEV {}: bridge handler is null!", unitId);
            return;
        }
        updateDeviceStatus(bridgeHandler);

        // If we already know the controller, then we don't want to initialise again
        if (controllerHandler != null) {
            logger.debug("DEV {}: Controller already initialised", unitId);
        } else {
            controllerHandler = bridgeHandler;
        }
        pingDevice();
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command cmd) {
        if (controllerHandler == null) {
            logger.info("DEV {}: received cmd {} but no bridge handler", unitId, cmd);
            return;
        }

        final MessageBuilder message;
        if (cmd == OnOffType.ON) {
            message = MessageBuilder.forCommand(ACTIVATE);
        } else if (cmd == OnOffType.OFF) {
            message = MessageBuilder.forCommand(DEACTIVATE);
        } else if (cmd instanceof PercentType) {
            message = MessageBuilder.forCommand(GOTO).args(((PercentType) cmd).byteValue());
        } else if (cmd == RefreshType.REFRESH) {
            refreshDeviceState();
            return;
        } else {
            logger.info("channel {}: unsupported cmd {}", channelUID, cmd);
            return;
        }

        message.network(networkId).destination(getUnitId());
        controllerHandler.sendPacket(message).thenAccept(this::updateStatus);
    }

    public void onMessageReceived(final UPBMessage msg) {
        updateStatus(ThingStatus.ONLINE);
        if (msg.getControlWord().isLink()) {
            handleLinkMessage(msg);
        } else {
            handleDirectMessage(msg);
        }
    }

    private void handleDirectMessage(final UPBMessage msg) {
        final State state;
        switch (msg.getCommand()) {
            case ACTIVATE:
                state = OnOffType.ON;
                break;

            case DEACTIVATE:
                state = OnOffType.OFF;
                break;

            case GOTO:
                if (msg.getArguments().length == 0) {
                    logger.info("DEV {}: malformed GOTO cmd", unitId);
                    return;
                }
                final int level = msg.getArguments()[0];
                if (level == 100) {
                    state = OnOffType.ON;
                } else {
                    state = OnOffType.OFF;
                }
                updateState(Constants.DIMMER_TYPE_ID, new PercentType(level));
                break;

            default:
                logger.debug("DEV {}: Message {} ignored", unitId, msg.getCommand());
                return;
        }
        updateState(Constants.SWITCH_TYPE_ID, state);
    }

    private void handleLinkMessage(final UPBMessage msg) {
        final byte linkId = msg.getDestination();
        for (final Channel ch : getThing().getChannels()) {
            if (Constants.SCENE_CHANNEL_TYPE_ID.equals(ch.getChannelTypeUID().getId())) {
                final BigDecimal channelLinkId = (BigDecimal) ch.getConfiguration()
                        .get(Constants.CONFIGURATION_LINK_ID);
                if (channelLinkId == null || channelLinkId.byteValue() != linkId) {
                    continue;
                }
                switch (msg.getCommand()) {
                    case ACTIVATE:
                    case DEACTIVATE:
                        triggerChannel(ch.getUID(), msg.getCommand().name());
                        break;

                    default:
                        logger.debug("DEV {}: Message {} ignored for link {}", unitId, linkId & 0xff, msg.getCommand());
                        return;
                }
            }
        }
    }

    private void updateDeviceStatus(final PIMHandler bridgeHandler) {
        final UPBDevice device = bridgeHandler.getDevice(getNetworkId(), getUnitId());
        if (device == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, Constants.OFFLINE_NODE_NOTFOUND);
        } else {
            switch (device.getState()) {
                case INITIALIZING:
                case ALIVE:
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case DEAD:
                case FAILED:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            Constants.OFFLINE_NODE_DEAD);
                    break;
            }
        }
    }

    protected void pingDevice() {
        controllerHandler
                .sendPacket(
                        MessageBuilder.forCommand(NULL).ackMessage(true).network(networkId).destination((byte) unitId))
                .thenAccept(this::updateStatus);
    }

    private void updateStatus(final CmdStatus result) {
        switch (result) {
            case WRITE_FAILED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, Constants.OFFLINE_NODE_DEAD);
                break;

            case ACK:
            case NAK:
                updateStatus(ThingStatus.ONLINE);
                break;
        }
    }

    private void refreshDeviceState() {
        controllerHandler
                .sendPacket(MessageBuilder.forCommand(REPORT_STATE).network(networkId).destination(getUnitId()))
                .thenAccept(this::updateStatus);
    }

    public byte getNetworkId() {
        return networkId;
    }

    public byte getUnitId() {
        return (byte) unitId;
    }
}
