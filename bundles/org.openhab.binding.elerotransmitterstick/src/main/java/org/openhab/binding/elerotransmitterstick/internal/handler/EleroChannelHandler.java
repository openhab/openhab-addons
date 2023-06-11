/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.elerotransmitterstick.internal.handler;

import static org.openhab.binding.elerotransmitterstick.internal.EleroTransmitterStickBindingConstants.*;

import java.util.Collections;

import org.openhab.binding.elerotransmitterstick.internal.config.EleroChannelConfig;
import org.openhab.binding.elerotransmitterstick.internal.stick.CommandType;
import org.openhab.binding.elerotransmitterstick.internal.stick.ResponseStatus;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EleroChannelHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Volker Bier - Initial contribution
 */
public class EleroChannelHandler extends BaseThingHandler implements StatusListener {
    private final Logger logger = LoggerFactory.getLogger(EleroChannelHandler.class);

    protected Integer channelId;
    protected EleroTransmitterStickHandler bridge;

    public EleroChannelHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        bridge = (EleroTransmitterStickHandler) getBridge().getHandler();

        channelId = getConfig().as(EleroChannelConfig.class).channelId;
        bridge.addStatusListener(channelId, this);

        if (bridge.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void dispose() {
        if (bridge != null) {
            bridge.removeStatusListener(channelId, this);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            logger.debug("Bridge for Elero channel handler for thing {} ({}) changed status to {}",
                    getThing().getLabel(), getThing().getUID(), bridgeStatusInfo.getStatus().toString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);

        if (channelUID.getIdWithoutGroup().equals(CONTROL_CHANNEL)) {
            if (command == UpDownType.UP) {
                bridge.getStick().sendCommand(CommandType.UP, Collections.singletonList(channelId));
            } else if (command == UpDownType.DOWN) {
                bridge.getStick().sendCommand(CommandType.DOWN, Collections.singletonList(channelId));
            } else if (command == StopMoveType.STOP) {
                bridge.getStick().sendCommand(CommandType.STOP, Collections.singletonList(channelId));
            } else if (command instanceof PercentType) {
                CommandType cmd = CommandType.getForPercent(((PercentType) command).intValue());
                if (cmd != null) {
                    bridge.getStick().sendCommand(cmd, Collections.singletonList(channelId));
                } else {
                    logger.debug("Unhandled command {}.", command);
                }
            } else if (command == RefreshType.REFRESH) {
                bridge.getStick().requestUpdate(Collections.singletonList(channelId));
            }
        }
    }

    @Override
    public void statusChanged(int channelId, ResponseStatus status) {
        logger.debug("Received updated state {} for thing {}", status, getThing().getUID().toString());

        updateState(STATUS_CHANNEL, new StringType(status.toString()));

        int percentage = ResponseStatus.getPercentageFor(status);
        if (percentage != -1) {
            updateState(CONTROL_CHANNEL, new PercentType(percentage));
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
