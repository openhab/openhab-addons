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
package org.openhab.binding.upb.internal.handler;

import static org.openhab.binding.upb.internal.message.Command.ACTIVATE;
import static org.openhab.binding.upb.internal.message.Command.DEACTIVATE;
import static org.openhab.binding.upb.internal.message.Command.GOTO;
import static org.openhab.binding.upb.internal.message.Command.NULL;
import static org.openhab.binding.upb.internal.message.Command.REPORT_STATE;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.upb.internal.Constants;
import org.openhab.binding.upb.internal.UPBDevice;
import org.openhab.binding.upb.internal.handler.UPBIoHandler.CmdStatus;
import org.openhab.binding.upb.internal.message.MessageBuilder;
import org.openhab.binding.upb.internal.message.UPBMessage;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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
    // Time to wait between attempts to poll the device to refresh state
    private static final long REFRESH_INTERVAL_MS = 3_000;

    private final Logger logger = LoggerFactory.getLogger(UPBThingHandler.class);
    private final @Nullable Byte defaultNetworkId;

    protected volatile byte networkId;
    protected volatile int unitId;
    private volatile long lastRefreshMillis;

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
            final Byte defaultNetworkId = this.defaultNetworkId;
            if (defaultNetworkId == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "missing network ID");
                return;
            }
            networkId = defaultNetworkId.byteValue();
        } else if (val.compareTo(BigDecimal.ZERO) < 0 || val.compareTo(BigDecimal.valueOf(255)) > 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "invalid network ID");
            return;
        } else {
            networkId = val.byteValue();
        }

        final BigDecimal cfgUnitId = (BigDecimal) getConfig().get(Constants.CONFIGURATION_UNIT_ID);
        if (cfgUnitId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "missing unit ID");
            return;
        }
        unitId = cfgUnitId.intValue();
        if (unitId < 1 || unitId > 250) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "invalid unit ID");
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
        pingDevice();
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command cmd) {
        final PIMHandler pimHandler = getPIMHandler();
        if (pimHandler == null) {
            logger.warn("DEV {}: received cmd {} but no bridge handler", unitId, cmd);
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
            logger.warn("channel {}: unsupported cmd {}", channelUID, cmd);
            return;
        }

        message.network(networkId).destination(getUnitId());
        pimHandler.sendPacket(message).thenAccept(this::updateStatus);
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
        byte[] args = msg.getArguments();
        switch (msg.getCommand()) {
            case ACTIVATE:
                state = OnOffType.ON;
                break;

            case DEACTIVATE:
                state = OnOffType.OFF;
                break;

            case GOTO:
            case DEVICE_STATE:
                if (args.length == 0) {
                    logger.warn("DEV {}: malformed {} cmd", unitId, msg.getCommand());
                    return;
                }
                state = new PercentType(args[0]);
                break;

            default:
                logger.debug("DEV {}: Message {} ignored", unitId, msg.getCommand());
                return;
        }
        updateState(Constants.DIMMER_TYPE_ID, state);
    }

    private void handleLinkMessage(final UPBMessage msg) {
        final byte linkId = msg.getDestination();
        for (final Channel ch : getThing().getChannels()) {
            ChannelTypeUID channelTypeUID = ch.getChannelTypeUID();
            if (channelTypeUID != null && Constants.SCENE_CHANNEL_TYPE_ID.equals(channelTypeUID.getId())) {
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
        final PIMHandler pimHandler = getPIMHandler();
        if (pimHandler != null) {
            pimHandler.sendPacket(
                    MessageBuilder.forCommand(NULL).ackMessage(true).network(networkId).destination((byte) unitId))
                    .thenAccept(this::updateStatus);
        }
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
        // This polls the device to see if it is alive. Since the REFRESH command is sent
        // for each channel and we want to avoid unnecessary traffic, we only ping the device
        // if some time has elapsed since the last refresh.
        final long now = System.currentTimeMillis();
        if (now - lastRefreshMillis > REFRESH_INTERVAL_MS) {
            lastRefreshMillis = now;
            final PIMHandler pimHandler = getPIMHandler();
            if (pimHandler != null) {
                pimHandler
                        .sendPacket(MessageBuilder.forCommand(REPORT_STATE).network(networkId).destination(getUnitId()))
                        .thenAccept(this::updateStatus);
            }
        }
    }

    protected @Nullable PIMHandler getPIMHandler() {
        final Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("DEV {}: bridge is null!", unitId);
            return null;
        }
        final PIMHandler bridgeHandler = (PIMHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            logger.debug("DEV {}: bridge handler is null!", unitId);
            return null;
        }
        return bridgeHandler;
    }

    public byte getNetworkId() {
        return networkId;
    }

    public byte getUnitId() {
        return (byte) unitId;
    }
}
