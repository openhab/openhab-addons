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
package org.openhab.binding.hapero.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hapero.internal.config.HaperoThingConfig;
import org.openhab.binding.hapero.internal.device.Device;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HaperoThingHandler} is responsible for handling commands, which are
 * sent to one of the channels and update the channel data from the
 * data blocks of the input stream
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
abstract class HaperoThingHandler extends BaseThingHandler {

    /** for debug data logging */
    protected final Logger logger = LoggerFactory.getLogger(HaperoThingHandler.class);

    /**
     * Constructor
     *
     * @param thing
     */
    public HaperoThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID);
        }
    }

    @Override
    public void initialize() {
        HaperoThingConfig config = getConfigAs(HaperoThingConfig.class);

        if (config.deviceID.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.deviceid.invalid");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    /**
     * Calls {@link UpdateChannel} for all channels of the Thing
     * and checks if all channels are successfully updated.
     * Sets the thing to OFFLINE if a channel reports a fault.
     */
    public void updateAllChannels() {
        Boolean updateOK = true;
        for (Channel channel : thing.getChannels()) {
            if (!updateChannel(channel.getUID())) {
                updateOK = false;
            }
        }

        if (updateOK) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.data.invalid");
        }
    }

    /**
     * Updates one channel of the thing from the input data of
     * the device data stream. Calls {@link getStateForChannel}
     * to parse the input data and return a {@link State} object
     * that corresponds to the data.
     *
     * @param channelId The channel to be updated
     * @return true if a state could be obtained, false if not
     */
    public boolean updateChannel(ChannelUID channelId) {
        State state = null;
        HaperoBridgeHandler bridgeHandler = null;
        Bridge bridge = getBridge();
        HaperoThingConfig config = getConfigAs(HaperoThingConfig.class);

        if (bridge != null) {
            bridgeHandler = (HaperoBridgeHandler) bridge.getHandler();
        } else {
            logger.warn("Could not get Bridge!");
            return false;
        }

        if (bridgeHandler == null) {
            logger.warn("Could not get BridgeHandler!");
            return false;
        }

        Channel channel = getThing().getChannel(channelId);

        /*
         * get the input data stream (the device) for this thing
         * and try to derive a state from it
         */
        Device device = bridgeHandler.getDevice(config.deviceID);

        if (channel != null && device != null) {
            state = getStateForChannel(channel, device);
        }

        if (state == null || state == UnDefType.NULL) {
            logger.warn("{} had invalid data", channelId);

            return false;
        }

        updateState(channelId, state);

        return true;
    }

    /**
     * This function shall be implemented in the derived Thing Handlers to parse
     * the input data stream for the given channel and return a {@link State} object
     * that corresponds to the data.
     *
     * @param channel The channel for which a State object shall be created
     * @param device Hold the raw input data for this Thing
     * @return a {@link State} for the Channel data or UnDefType.NULL if the device had invalid data for this channel.
     */
    protected @Nullable State getStateForChannel(Channel channel, Device device) {
        return UnDefType.NULL;
    }

    /**
     * This function shall be implemented in the derived Thing Handlers to update the
     * device properties from the data stream.
     */
    public void updateThingProperties() {
    }
}
