/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pentair.internal.handler;

import java.util.Collection;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.config.PentairBaseThingConfig;
import org.openhab.binding.pentair.internal.parser.PentairBasePacket;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PentairBaseThingHandler } Abstract class for all Pentair thing handlers.
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
public abstract class PentairBaseThingHandler extends BaseThingHandler {
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(PentairBaseThingHandler.class);

    private PentairBaseThingConfig config = new PentairBaseThingConfig();

    // waitStatusForOnline indicates whether the device is waiting to go fully online until after a first packet is
    // received
    protected boolean waitStatusForOnline = false;

    public PentairBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(PentairBaseThingConfig.class);

        PentairBaseBridgeHandler bh = getBridgeHandler();

        if (bh == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.bridge-missing");
            return;
        }

        if (bh.equipment.get(config.id) != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.duplicate-id");
            return;
        }

        bh.childHandlerInitializing(this, this.getThing());

        goOnline();

        updateStatus(ThingStatus.UNKNOWN);
    }

    public void goOnline() {
        waitStatusForOnline = true;
    }

    public void finishOnline() {
        waitStatusForOnline = false;
        updateStatus(ThingStatus.ONLINE);
    }

    public void goOffline(ThingStatusDetail detail) {
        updateStatus(ThingStatus.OFFLINE, detail);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            goOffline(ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            waitStatusForOnline = false;
            goOnline();
        }
    }

    public int getPentairID() {
        return config.id;
    }

    @Nullable
    public PentairBaseBridgeHandler getBridgeHandler() {
        // make sure bridge exists and is online
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            return null;
        }
        PentairBaseBridgeHandler bh = (PentairBaseBridgeHandler) bridge.getHandler();
        if (bh == null) {
            return null;
        }

        return bh;
    }

    /**
     * Helper function to update channel.
     */
    public void updateChannel(ChannelUID channel, boolean value) {
        updateState(channel, OnOffType.from(value));
    }

    public void updateChannel(ChannelUID channel, int value) {
        updateState(channel, new DecimalType(value));
    }

    public void updateChannel(ChannelUID channel, double value) {
        updateState(channel, new DecimalType(value));
    }

    public void updateChannel(ChannelUID channel, String value) {
        updateState(channel, new StringType(value));
    }

    public void updateChannel(ChannelUID channel, Number value, Unit<?> unit) {
        updateState(channel, new QuantityType<>(value, unit));
    }

    public void refreshAllChannels() {
        List<Channel> channels = getThing().getChannels();

        refreshChannels(channels);
    }

    public void refreshGroupChannels(String group) {
        List<Channel> channels = getThing().getChannelsOfGroup(group);

        refreshChannels(channels);
    }

    public void refreshChannels(Collection<Channel> channels) {
        ThingHandler handler = getThing().getHandler();
        if (handler == null) {
            return;
        }

        for (Channel channel : channels) {
            ChannelUID uid = channel.getUID();
            handler.handleCommand(uid, RefreshType.REFRESH);
        }
    }

    public void refreshChannelsFromUIDs(Collection<ChannelUID> channelUIDs) {
        ThingHandler handler = getThing().getHandler();
        if (handler == null) {
            return;
        }

        for (ChannelUID channelUID : channelUIDs) {
            handler.handleCommand(channelUID, RefreshType.REFRESH);
        }
    }

    /**
     * Abstract function to be implemented by Thing to parse a received packet
     *
     * @param p
     */
    public abstract void processPacketFrom(PentairBasePacket p);
}
